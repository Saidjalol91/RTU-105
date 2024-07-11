package com.moren.rems.rtu.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.moren.rems.rtu.vo.RtuStatusVo;

/**
 * REMS RTU 알림 통지 Service
 * @author shlee
 */
@Service
public class RemsRtuNoticeService {
	
	/** 
	 * RTU 에 전송 상태 알림.
	 * @param msgCode
	 * @param rtuStatusVo
	 */
	@Async
	public void noticeRemsRtuStatus(String msgCode, RtuStatusVo rtuStatusVo) {
		try {
			Thread.sleep(5000);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
}
