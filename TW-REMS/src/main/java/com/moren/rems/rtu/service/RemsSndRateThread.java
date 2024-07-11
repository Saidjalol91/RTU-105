package com.moren.rems.rtu.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import com.moren.rems.rtu.vo.RemsAuthPool;
import com.moren.rems.rtu.vo.RemsAuthVo;
import com.moren.rems.rtu.vo.RemsTokenPool;
import com.moren.rems.rtu.vo.RemsTokenVo;
import com.moren.rems.rtu.vo.RemsVo;
import com.moren.rems.rtu.vo.RtuStatusVo;
import com.roviet.common.tools.data.Config;
import com.roviet.common.tools.http.HttpCall;
import com.roviet.common.tools.utility.NumberUtil;
import com.roviet.common.tools.utility.StringUtil;
import com.roviet.common.web.tools.ServiceUtil;

/**
 * 더미 전송을 위한 기본 Thread
 * @author shlee
 */
public abstract class RemsSndRateThread extends Thread {

	Logger log = Logger.getLogger(this.getClass());
	Logger remsLog = Logger.getLogger("dummy.rems.log");
	
	@Value("${rems.call.debug}")
	boolean remsCallDebug = false;

	@Value("${rems.call.data.url}")
	String remsCallDataUrl;

	protected RtuStatusVo vo = null;
	protected long lastTime = 0;
	protected long stdInterval = 0;
	protected boolean isRtuAlive = false;
	protected Map<String,String> data = null;
	protected String startTime = null;
	protected String endTime = null;
	protected boolean isTimeAnd = true;
	
	RemsRtuDbService remsRtuDbService = (RemsRtuDbService) ServiceUtil.getServiceBean("remsRtuDbService");

	/**
	 * REMS 기본 전송율 조정 Thread 생성자.
	 * vo : rems 전송용 정보저장 vo
	 * lastTime : 정상적인 마지막 전송시간
	 * stdInterval : 전송 간격
	 * data : REMS전송 데이터
	 * startTime : 더미전송가능 시작시간
	 * endTime : 더미전송 가능 종료시간
	 * @param vo
	 * @param lastTime
	 * @param stdInterval
	 * @param data
	 * @param startTime
	 * @param endTime
	 */
	public RemsSndRateThread(RtuStatusVo vo, long lastTime, long stdInterval, Map<String,String> data, String startTime, String endTime) {
		this.vo = vo;
		this.lastTime = lastTime;
		this.stdInterval = stdInterval;
		this.data = data;
		this.startTime = startTime;
		this.endTime = endTime;
		if(this.startTime!=null && this.startTime.compareTo(this.endTime)>0) this.isTimeAnd = false;
	}
	
	/**
	 * 전송을 위한 DATA 가져오기
	 * @return
	 */
	public Map<String, String> getData() {
		return data;
	}

	/**
	 * 전송을 위한 DATA 지정
	 * @param data
	 */
	public void setData(Map<String, String> data) {
		this.data = data;
	}

	/**
	 * 마지막 전송한 시간을 지정
	 * @return
	 */
	public long getLastTime() {
		return lastTime;
	}

	/**
	 * 현재 더미전송 RTU 가 구동중인지 가져오기
	 * @return
	 */
	public boolean isRtuAlive() {
		return isRtuAlive;
	}

	/**
	 * 현재 더미전송이 구동되어야 하는지 지정
	 * @param isRtuAlive
	 */
	public void setRtuAlive(boolean isRtuAlive) {
		this.isRtuAlive = isRtuAlive;
	}

	/**
	 * 마지막 정상통신일시 설정
	 * @param lastTime
	 */
	public void setLastTime(long lastTime) {
		this.lastTime = lastTime;
	}
	
	/**
	 * 더미전송이 가능한 시작시간,종료시간을 설정
	 * @param startTime
	 * @param endTime
	 */
	public void setRunTime(String startTime, String endTime) {
		this.startTime = startTime;
		this.endTime = endTime;
	}
	
	/**
	 * 더미전송이 가능한 시작시간 가져오기
	 * @return
	 */
	public String getStartTime() {
		return this.startTime;
	}
	
	/**
	 * 더미전송이 가능한 종료시간 가져오기
	 * @return
	 */
	public String getEndTime() {
		return this.endTime;
	}
	
	/**
	 * 현재 더미전송이 가능한 시간대인지 가져오기
	 * @return boolean
	 */
	public boolean isRunTime() {
		boolean isRunTime = true;
		String nowTime = StringUtil.getDate("HHmm");
		if(this.isTimeAnd && (nowTime.compareTo(this.startTime)<0 || nowTime.compareTo(this.endTime)>0)) isRunTime = false;
		else if(!this.isTimeAnd && nowTime.compareTo(this.startTime)>0 && nowTime.compareTo(this.endTime)<0) isRunTime = false;
		return isRunTime;
	}

	/**
	 * 더미전송을 위한 발전정보 Vo 가져오기
	 * @return
	 */
	public RtuStatusVo getVo() {
		return vo;
	}

	/**
	 * 더미전송을 위한 발전정보 Vo 설정
	 * @param vo
	 */
	public void setVo(RtuStatusVo vo) {
		this.vo = vo;
	}

	/** 
	 * 더미전송 간격 가져오기
	 * @return long
	 */
	public long getStdInterval() {
		return stdInterval;
	}

	/**
	 * 더미전송 각격 설정하기
	 * @param stdInterval
	 */
	public void setStdInterval(long stdInterval) {
		this.stdInterval = stdInterval;
	}
	
	/** 
	 * 더미 전송 DATA 생성하여 REMS 에 전송.
	 * @param rtuStatusVo
	 * @return
	 * @throws Exception
	 */
	public RtuStatusVo sendDummyRemsData(RtuStatusVo rtuStatusVo) throws Exception  {
		// REMS 전송용 DATA 생성
		RtuStatusVo statusVo = convertRtuDataToRemsData(rtuStatusVo);
		// REMS 전송
		statusVo = sendRemsData(statusVo);
		
		// 전송이력 DB 저장
		remsRtuDbService.saveRemsSndData(statusVo);
		return statusVo;
	}
	
	/**
	 * 생성된 더미 데이터 REMS 에 전송
	 * @param rtuStatusVo
	 * @return
	 * @throws Exception
	 */
	public RtuStatusVo sendRemsData(RtuStatusVo rtuStatusVo) throws Exception {
		HttpCall http = new HttpCall();
		http.setDebug(true);
		http.setTimeout(10*1000);
		RemsVo remsVo = rtuStatusVo.getRemsVo();
		if(remsVo.getCid()!=null && !"".equals(remsVo.getCid())) {
			
			if(remsCallDataUrl==null) remsCallDataUrl = Config.getProperty("rems.call.data.url");
			if(!remsCallDebug) remsCallDebug = Boolean.parseBoolean(Config.getProperty("rems.call.debug","false"));
			http.setDebug(remsCallDebug);
			
			Map<String,Object> tokenResponse = http.getUrlCall(remsCallDataUrl, (Object) StringUtil.obj2String(remsVo.getMap("cid", "multi","data")), "POST", remsVo.getHeader());
			int remsSndResult = (tokenResponse!=null && tokenResponse.containsKey("responseCode") ? NumberUtil.convertToInt(tokenResponse.get("responseCode"), -1) : -1);
			remsLog.info("IMEI:"+rtuStatusVo.getImei()+" ENRGY:"+rtuStatusVo.getEnrgy()+" MACHN:"+ rtuStatusVo.getMachn()+" MULTI:"+rtuStatusVo.getMulti()+" - \""+remsCallDataUrl+"\" - "+remsSndResult);
			remsVo.setResponseCode(remsSndResult);
			rtuStatusVo.setResponseCode(remsSndResult);
		}
		return rtuStatusVo;
	}

	/**
	 * REMS 에 전송을 위한 더미 데이터 생성
	 * @param rtuStatusVo
	 * @return
	 * @throws Exception
	 */
	public RtuStatusVo convertRtuDataToRemsData(RtuStatusVo rtuStatusVo) throws Exception {
		if(rtuStatusVo.getImei()==null) throw new Exception("IMEI['"+rtuStatusVo.getImei()+"'] is not exists.");
		if(rtuStatusVo.getEnrgy()==null) throw new Exception("Enrgy['"+rtuStatusVo.getEnrgy()+"'] is not exists.");
		if(rtuStatusVo.getMachn()==null) throw new Exception("Machn['"+rtuStatusVo.getMachn()+"'] is not exists.");
		if(rtuStatusVo.getMulti()==null) throw new Exception("Multi['"+rtuStatusVo.getMulti()+"'] is not exists.");
		
		RemsAuthVo remsAuthVo = RemsAuthPool.getRemsAuthVo(rtuStatusVo.getImei(), rtuStatusVo.getEnrgy(), rtuStatusVo.getMachn(), rtuStatusVo.getMulti());
		if(remsAuthVo==null) throw new Exception("RtuStatusVo="+rtuStatusVo.getMap("imei","enrgy","machn").toString()+" could not get CID and AUTH_KEY.");
		if(remsAuthVo.getCid()==null || "".equals(remsAuthVo.getCid().trim())) throw new Exception("RtuStatusVo="+rtuStatusVo.getMap("imei","enrgy","machn").toString()+" could not get CID['"+remsAuthVo.getCid()+"'].");
		if(remsAuthVo.getAuthKey()==null || "".equals(remsAuthVo.getAuthKey().trim())) throw new Exception("RtuStatusVo="+rtuStatusVo.getMap("imei","enrgy","machn").toString()+" could not get Authorization['"+remsAuthVo.getAuthKey()+"'].");

		// REMS 전송용 인증정보 추출 저장
		rtuStatusVo.setRemsAuthVo(remsAuthVo);
		
		// REMS전송용 TOKEN 정보 추출
		RemsTokenVo remsTokenVo = RemsTokenPool.getRemsTokenVo(remsAuthVo.getCid(), remsAuthVo.getAuthKey());
		if(remsTokenVo==null || remsTokenVo.getAccessToken()==null) throw new Exception("RtuStatusVo="+rtuStatusVo.getMap("imei","enrgy","machn","multi").toString()+" Token is not exists.");

		Map<String,String> sndRemsHeader = new HashMap<String,String>();
		byte[] token = (remsTokenVo.getAccessToken()!=null ? remsTokenVo.getAccessToken().getBytes() : null);
		sndRemsHeader.put("tk1",(token!=null && token.length>32 ? new String(token,0,32) : new String(token)));
		sndRemsHeader.put("tk2",(token!=null && token.length>32 ? new String(token,32,token.length-32) : ""));
		sndRemsHeader.put("Content-Type","application/json");

		RemsVo remsVo = new RemsVo();
		remsVo.setCid(remsAuthVo.getCid());
		remsVo.setMulti(Integer.parseInt(rtuStatusVo.getMulti(),10));
		remsVo.setData((String) this.data.get(rtuStatusVo.getMachn()));
		remsVo.setHeader(sndRemsHeader);
		
		rtuStatusVo.setRemsVo(remsVo);
		
		return rtuStatusVo;
	}
	
	/**
	 * 더미 반복 전송을 위한 반봅 Loop
	 */
	abstract public void run();

}
