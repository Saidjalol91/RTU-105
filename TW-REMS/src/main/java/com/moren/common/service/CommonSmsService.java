package com.moren.common.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.roviet.common.web.sms.AbsSmsService;
import com.roviet.common.web.sms.SmsVo;

@Service
public class CommonSmsService extends AbsSmsService {

	/**
	 * SMS 발송용 객체를 생성한다.
	 * @param args
	 * @return
	 */
	@Override
	public SmsVo makeSms(Object... args) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * SMS 발송 내용을 생성한다.
	 * @param phoneNo
	 * @param smsType
	 * @param info
	 * @return
	 * @throws Exception
	 */
	@Override
	public String makeMessage(String phoneNo, String smsType, Map<String, Object> info) throws Exception {
		String message = null;
		if("TEST".equals(smsType)) {
			message = "SMS 발송 TEST";
		} else {
			throw new Exception("smsType["+smsType+"] is not defined.");
		}
		return message;
	}
}
