package com.moren.rems.rtu.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import com.moren.common.service.CommonCodeService;
import com.moren.common.vo.CodeVo;
import com.moren.rems.rtu.dao.RemsRtuSndRateDao;
import com.moren.rems.rtu.vo.RemsThreadPool;
import com.moren.rems.rtu.vo.RemsThreadStatusPool;
import com.moren.rems.rtu.vo.RtuStatusVo;
import com.roviet.common.tools.utility.DateUtil;

/**
 * REMS 전송율 향상을 위한 더미전송용 Service
 * @author shlee
 */
@Service
public class RemsRtuSndRateService {
	Logger log = Logger.getLogger(this.getClass());
	Logger remsLog = Logger.getLogger("dummy.rems.log");
	
	@Autowired
	CommonCodeService commonCodeService;
	
	@Autowired
	RemsRtuSndRateDao remsRtuSndRateDao;
	
	int intervalMinute = 15;
	
	/**
	 * REMS 더미 전송을 위한 메인 함수
	 * 스케쥴로 1분마다 구동
	 */
	@SuppressWarnings("unchecked")
	public void remsRtuSndRateProcess() {
		List<RtuStatusVo> list = (List<RtuStatusVo>) remsRtuSndRateDao.selectList("RemsRtuSndRateDao.getRtuSndStatusList",null);
		List<CodeVo> energyTime = (List<CodeVo>) commonCodeService.getCodeList("DUMMY_RUN_TIME");
		Map<String,CodeVo> timeMap = new HashMap<String,CodeVo>();
		for(int i=0; energyTime!=null && i<energyTime.size() ; i++) {
			CodeVo code = energyTime.get(i);
			timeMap.put(code.getCode1(),code);
		}
		
		Date now = new Date();
		for(int i=0 ; list!=null && i<list.size(); i++) {
			RtuStatusVo vo = list.get(i);
			try {
				RemsThreadStatusPool.setStatus(vo, vo.getDummyStatus());
				if("RUN".equals(vo.getDummyStatus())) {
					long lastTime = -1;
					try {
						if(vo.getLastRemsSndDt()!=null) lastTime = DateUtil.getDate(vo.getLastRemsSndDt(), "yyyy-MM-dd HH:mm:ss").getTime();
					} catch(Exception e) {}
					long gapTime = now.getTime()-lastTime;
					RemsSndRateThread thread = null;
					long stdInterval = ("03".equals(vo.getEnrgy()) ? intervalMinute*60*1000L : intervalMinute*60*1000L);
					if(gapTime>(stdInterval+(1*60*1000L))) {
						if(!RemsThreadPool.containsKey(vo)) {
							// make send data
							Map<String,String> data = new HashMap<String,String>();
							if("03".equals(vo.getEnrgy())) {
								String[] msgList = vo.getOriginalData().split("\r\n");
								if(msgList!=null && msgList.length==2) {
									data.put("01", msgList[0]);
									data.put("02", msgList[1]);
								} else if(msgList!=null){
									data.put("*", msgList[0]);
								} else data.put("*", null);
							} else {
								data.put(vo.getMachn(), vo.getOriginalData());
							}
							
							CodeVo code = timeMap.get(vo.getEnrgy());
							//thread create and start.
							if("03".equals(vo.getEnrgy())) thread = new RemsEnrgy03SndRateThread(vo, lastTime, stdInterval, data, code.getValue1(), code.getValue2());
							else thread = new RemsBasicSndRateThread(vo, lastTime, stdInterval, data, code.getValue1(), code.getValue2());
							RemsThreadPool.setThread(thread);
							thread.start();
						}
					}
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 전송을 위한 더미 DATA 를 메모리에 기록
	 * RTU로부터 마지막 수신된 DATA 를 더미로 전송하기위하여 RTU로 부터 수신될때마다 기록한다.
	 * @param param
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public Map<String,Object> setDummyData(Map<String,Object> param, ModelMap model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String,Object> resultMap = new HashMap<String,Object>();
		Map<String,Object> status = new HashMap<String,Object>();
		String message = null;
		
		try {
			RtuStatusVo vo = (RtuStatusVo) remsRtuSndRateDao.selectOne("RemsRtuSndRateDao.getDummyDataInfo", param);
			
			Map<String,String> data = new HashMap<String,String>();
			if("03".equals(vo.getEnrgy())) {
				String[] msgList = vo.getOriginalData().split("\r\n");
				if(msgList!=null && msgList.length==2) {
					data.put("01", msgList[0]);
					data.put("02", msgList[1]);
				} else if(msgList!=null){
					data.put("*", msgList[0]);
				} else data.put("*", null);
			} else {
				data.put(vo.getMachn(), vo.getOriginalData());
			}
			
			Iterator<String> iterator = RemsThreadPool.iterator();
			for(;iterator.hasNext();) {
				String key = iterator.next();
				String[] keys = key.split("/");
				if(keys.length!=4) continue;
				if(!"*".equals(vo.getImei()) && !vo.getImei().equals(keys[0])) continue;
				if(!"*".equals(vo.getEnrgy()) && !vo.getEnrgy().equals(keys[1])) continue;
				if(!"*".equals(vo.getMachn()) && !vo.getMachn().equals(keys[2])) continue;
				if(!"*".equals(vo.getMulti()) && !vo.getMulti().equals(keys[3])) continue;
				RemsThreadPool.getObject(key).setData(data);
			}
			status.put("code","SUCCESS");
		} catch(Exception e) {
			e.printStackTrace();
			status.put("code","FAIL");
			message = e.getMessage();
		}
		
		if(message!=null) status.put("message", message);
		resultMap.put("status", status);
		
		return resultMap;
	}

	/**
	 * 더미전송이 가능한 시간대를 설정하기위한 함수
	 * @param param
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public Map<String,Object> setDummyTime(Map<String,Object> param, ModelMap model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String,Object> resultMap = new HashMap<String,Object>();
		Map<String,Object> status = new HashMap<String,Object>();
		String message = null;
		
		try {
			List<CodeVo> energyTime = (List<CodeVo>) commonCodeService.getCodeList("DUMMY_RUN_TIME");
			Map<String,CodeVo> timeMap = new HashMap<String,CodeVo>();
			for(int i=0; energyTime!=null && i<energyTime.size() ; i++) {
				CodeVo code = energyTime.get(i);
				timeMap.put(code.getCode1(),code);
			}
						
			Iterator<String> iterator = RemsThreadPool.iterator();
			for(;iterator.hasNext();) {
				String key = iterator.next();
				String[] keys = key.split("/");
				if(keys.length!=4) continue;
				CodeVo codeVo = timeMap.get(keys[1]);
				if(codeVo!=null && codeVo.getValue1()!=null && codeVo.getValue1().length()==4 && codeVo.getValue2()!=null && codeVo.getValue2().length()==4) {
					RemsThreadPool.getObject(key).setRunTime(codeVo.getValue1(), codeVo.getValue2());
				}
			}
			status.put("code","SUCCESS");
		} catch(Exception e) {
			e.printStackTrace();
			status.put("code","FAIL");
			message = e.getMessage();
		}
		
		if(message!=null) status.put("message", message);
		resultMap.put("status", status);
		
		return resultMap;
	}
}
