package com.moren.rems.rtu.service;

import java.util.Date;
import java.util.Map;

import com.moren.rems.rtu.vo.RemsThreadPool;
import com.moren.rems.rtu.vo.RemsThreadStatusPool;
import com.moren.rems.rtu.vo.RtuStatusVo;

/**
 * REMS 지열용 전송율 조정용 Thread
 * @author shlee
 */
public class RemsEnrgy03SndRateThread extends RemsSndRateThread {

	/**
	 * REMS 지열용 전송율 조정 Thread 생성자.
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
	public RemsEnrgy03SndRateThread(RtuStatusVo vo, long lastTime, long stdInterval, Map<String,String> data, String startTime, String endTime) {
		super(vo, lastTime, stdInterval, data, startTime, endTime);
	}
	
	@Override
	public void run() {
		remsLog.debug(vo.getImei()+"/"+vo.getEnrgy()+("03".equals(vo.getEnrgy()) ? "" : "/"+vo.getMachn()+"/"+vo.getMulti())+" : START.");
		String machn = null;
		for(; "RUN".equals(RemsThreadStatusPool.getStatus(vo)) && !isRtuAlive() && isRunTime(); ) {
			try {
				Thread.sleep(1000);
			} catch(Exception e) {
				e.printStackTrace();
			}
			long nowTime = (new Date()).getTime();
			if((nowTime-this.lastTime)>stdInterval) {
				try {
					
					if(!"01".equals(machn)) machn = "01";
					
					RtuStatusVo rtuStatusVo = new RtuStatusVo();
					rtuStatusVo.setImei(vo.getImei());
					rtuStatusVo.setEnrgy(vo.getEnrgy());
					rtuStatusVo.setMachn(machn);
					rtuStatusVo.setMulti(vo.getMulti());
					rtuStatusVo.setOriginalData((String) this.data.get(vo.getMachn()));
					
					rtuStatusVo = sendDummyRemsData(rtuStatusVo);
				} catch(Exception e) {
					e.printStackTrace();
				}
				this.lastTime = nowTime;
			}
			if("01".equals(machn) && (nowTime-this.lastTime)>(1*60*1000L)) {
				try {
					machn = "02";
					
					RtuStatusVo rtuStatusVo = new RtuStatusVo();
					rtuStatusVo.setImei(vo.getImei());
					rtuStatusVo.setEnrgy(vo.getEnrgy());
					rtuStatusVo.setMachn(machn);
					rtuStatusVo.setMulti(vo.getMulti());
					rtuStatusVo.setOriginalData((String) this.data.get(vo.getMachn()));
					
					rtuStatusVo = sendDummyRemsData(rtuStatusVo);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		try {
			RemsThreadPool.removeThread(vo);
		} catch(Exception e) {
			e.printStackTrace();
		}
		remsLog.debug(vo.getImei()+"/"+vo.getEnrgy()+("03".equals(vo.getEnrgy()) ? "/*/"+vo.getMulti() : "/"+vo.getMachn()+"/"+vo.getMulti())+" : END.");
	}

}
