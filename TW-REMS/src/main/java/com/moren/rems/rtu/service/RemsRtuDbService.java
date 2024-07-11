package com.moren.rems.rtu.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.moren.rems.rtu.dao.RemsRtuDao;
import com.moren.rems.rtu.vo.RemsAuthVo;
import com.moren.rems.rtu.vo.RemsVo;
import com.moren.rems.rtu.vo.RtuPool;
import com.moren.rems.rtu.vo.RtuStatusVo;
import com.moren.rems.rtu.vo.RtuVo;
import com.roviet.common.tools.utility.StringUtil;

/**
 * REMS RELAY 결과 저장을 위한 DB 처리 Service
 * @author shlee
 */
@Service
public class RemsRtuDbService {

	@Autowired
	RemsRtuDao remsRtuDao;

	/**
	 * 전송 Data DB 저장
	 * @param rtuStatusVo
	 * @throws IllegalAccessException
	 */
	@Async
	public void saveMessageData(RtuStatusVo rtuStatusVo) throws IllegalAccessException {
		RtuVo rtuVo = RtuPool.getRtuVo(rtuStatusVo.getImei());
		//RemsAuthVo remsAuthVo = RemsAuthPool.getRemsAuthVo(rtuStatusVo.getImei(), rtuStatusVo.getEnrgy(), rtuStatusVo.getMachn(), rtuStatusVo.getMulti());
		RemsAuthVo remsAuthVo = rtuStatusVo.getRemsAuthVo();
		RemsVo remsVo = rtuStatusVo.getRemsVo();
		//boolean isIpChange = false;
		if(rtuStatusVo!=null && rtuStatusVo.getRemsVo()!=null) {
			remsRtuDao.update("RemsRtuDao.updateRtuEnrgyTypeLastRemsSndDt", rtuStatusVo);
			if(!rtuStatusVo.getIpAddr().equals(rtuVo.getIpAddr())) {
				//isIpChange = true;
				rtuVo.setIpAddr(rtuStatusVo.getIpAddr());
			}
		}
		remsRtuDao.update("RemsRtuDao.updateRtuStatus", rtuVo);
		/*
		if(isIpChange) {
			rows += remsRtuDao.insert("RemsRtuDao.insertRtuIpAddr", rtuVo);
		}
		*/
		
		Map<String,Object> rcvData = new HashMap<String,Object>();
		rcvData.put("imei", rtuStatusVo.getImei());
		rcvData.put("enrgy", rtuStatusVo.getEnrgy());
		rcvData.put("machn", rtuStatusVo.getMachn());
		rcvData.put("multi", rtuStatusVo.getMulti());
		rcvData.put("errCode", rtuStatusVo.getErrCode());
		rcvData.put("rcvHeaders", rtuStatusVo==null ? rtuStatusVo : StringUtil.obj2String(rtuStatusVo.getHeader()));
		rcvData.put("rcvData", rtuStatusVo.getOriginalData());
		rcvData.put("ipAddr", rtuStatusVo.getIpAddr());
		rcvData.put("resData", (rtuStatusVo.getRtuResVo()==null ? rtuStatusVo.getRtuResVo() : StringUtil.obj2String(rtuStatusVo.getRtuResVo().getMap())));
		rcvData.put("resCode", rtuStatusVo.getRtuResVo()==null ? "FAIL" : rtuStatusVo.getRtuResVo().isSuccess() ? "SUCCESS" : "FAIL");
		int row = remsRtuDao.insert("RemsRtuDao.insertRtuRcvData", rcvData);
		
		if(remsAuthVo!=null && remsVo!=null && row>0) {
			Map<String,Object> sndData = new HashMap<String,Object>();
			sndData.put("imei", rtuStatusVo.getImei());
			sndData.put("enrgy", rtuStatusVo.getEnrgy());
			sndData.put("machn", rtuStatusVo.getMachn());
			sndData.put("multi", rtuStatusVo.getMulti());
			sndData.put("errCode", rtuStatusVo.getErrCode());
			sndData.put("cid", remsAuthVo.getCid());
			sndData.put("rcvIdx", rcvData.get("rcvIdx"));
			sndData.put("authKey", remsAuthVo.getAuthKey());
			sndData.put("sndHeaders", remsVo.getHeader()!=null ? StringUtil.obj2String(remsVo.getHeader()) : null);
			sndData.put("sndData", (remsVo==null ? null : StringUtil.obj2String(remsVo.getMap("cid","multi","data"))));
			sndData.put("resData", remsVo.getResponseCode());
			
			remsRtuDao.insert("RemsRtuDao.insertRtuSndData", sndData);
		}
	}

	@Async
	public void saveRemsSndData(RtuStatusVo rtuStatusVo) throws IllegalAccessException {
		RemsVo remsVo = rtuStatusVo.getRemsVo();
		RemsAuthVo remsAuthVo = rtuStatusVo.getRemsAuthVo();
		
		if(remsAuthVo!=null && remsVo!=null) {
			Map<String,Object> sndData = new HashMap<String,Object>();
			sndData.put("imei", rtuStatusVo.getImei());
			sndData.put("enrgy", rtuStatusVo.getEnrgy());
			sndData.put("machn", rtuStatusVo.getMachn());
			sndData.put("multi", rtuStatusVo.getMulti());
			sndData.put("errCode", rtuStatusVo.getErrCode());
			sndData.put("cid", remsAuthVo.getCid());
			sndData.put("rcvIdx", null);
			sndData.put("authKey", remsAuthVo.getAuthKey());
			sndData.put("sndHeaders", remsVo.getHeader()!=null ? StringUtil.obj2String(remsVo.getHeader()) : null);
			sndData.put("sndData", (remsVo==null ? null : StringUtil.obj2String(remsVo.getMap("cid","multi","data"))));
			sndData.put("resData", remsVo.getResponseCode());
			
			remsRtuDao.insert("RemsRtuDao.insertRtuSndData", sndData);
		}
	}

	/**
	 * RTU 정보 등록
	 * @param rtuVo
	 */
	@Async
	public void insertRtu(RtuVo rtuVo) {
		remsRtuDao.insert("RemsRtuDao.insertRtu", rtuVo);
	}
	
	/**
	 * RTU 정보 UPDATE
	 * @param rtuVo
	 */
	@Async
	public void updateRtuInfo(RtuVo rtuVo) {
		remsRtuDao.update("RemsRtuDao.updateRtuInfo", rtuVo);
	}
	
	/**
	 * 발전정보 유형 등록 insert
	 * @param rtuStatusVo
	 */
	@Async
	public void insertRtuEnrgyType(RtuStatusVo rtuStatusVo) {
		remsRtuDao.insert("RemsRtuDao.insertRtuEnrgyType", rtuStatusVo);
	}
	
	/**
	 * 발전정보 유형 수정 update
	 * @param rtuStatusVo
	 */
	@Async 
	public void updateRtuEnrgyTypeInfo(RtuStatusVo rtuStatusVo) {
		remsRtuDao.update("RemsRtuDao.updateRtuEnrgyTypeInfo", rtuStatusVo);
	}
	
	/**
	 * 발전정보 마지막 통신일시 저장
	 * @param rtuStatusVo
	 */
	@Async 
	public void updateRtuEnrgyTypeLastComDt(RtuStatusVo rtuStatusVo) {
		remsRtuDao.update("RemsRtuDao.updateRtuEnrgyTypeLastComDt", rtuStatusVo);
	}
	
	/**
	 * 발전정보 마지막 REMS 송신일시 저장
	 * @param rtuStatusVo
	 */
	@Async 
	public void updateRtuEnrgyTypeLastRemsSndDt(RtuStatusVo rtuStatusVo) {
		remsRtuDao.update("RemsRtuDao.updateRtuEnrgyTypeLastRemsSndDt", rtuStatusVo);
	}

	/**
	 * 펌웨어 다운로드 RESET 일시 저장
	 * @param rtuVo
	 */
	@Async 
	public void updateRtuFwDownResetDate(RtuVo rtuVo) {
		remsRtuDao.update("RemsRtuDao.updateRtuFwDownResetDate", rtuVo);
	}

	/** 
	 * 펌웨어 설치일시 저장 update
	 * @param rtuStatusVo
	 */
	@Async 
	public void updateRtuFwInstallDate(RtuStatusVo rtuStatusVo) {
		remsRtuDao.update("RemsRtuDao.updateRtuFwInstallDate", rtuStatusVo);
	}

	public RtuVo getRtuInfoByImei(String imei) {
		return (RtuVo) remsRtuDao.selectOne("RemsRtuDao.getRtuInfo", imei);
	}

	/**
	 * RTU 정보 읽어오기
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<RtuVo> getRtuInfo() {
		return (List<RtuVo>) remsRtuDao.selectList("RemsRtuDao.getRtuInfo");
	}
	
	/**
	 * RTU 발전정보 상태 읽어오기
	 * @param imei
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<RtuStatusVo> getRtuStatusInfo(String imei) {
		return (List<RtuStatusVo>) remsRtuDao.selectList("RemsRtuDao.getRtuStatusInfo", imei);
	}
	
	/**
	 * 펌웨어 RESET 상태정보 읽어오기
	 * @param rtuStatusVo
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String,Object> getRtuFwResetStatus(RtuStatusVo rtuStatusVo) {
		return (Map<String,Object>) remsRtuDao.selectOne("RemsRtuDao.getRtuMaintenanceStatus", rtuStatusVo);
	}
	
}
