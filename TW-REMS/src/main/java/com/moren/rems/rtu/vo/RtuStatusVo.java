package com.moren.rems.rtu.vo;

import java.util.Map;

import com.roviet.common.tools.utility.AbsVo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 발전정보 객체 Vo
 * @author shlee
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class RtuStatusVo extends AbsVo {
	private long rtuEnrgyIdx;		// 에너지설비 번호
	private String imei;			// IMEI
	private String enrgy;			// 에너지타입
	private String machn;			// 설비
	private String imsi;			// IMSI
	private String multi;			// MULTI
	private int    period;			// 반복호출 간격
	private String fwVer;			// 펌웨어 버전
	private String errCode;		// 수신DATA 에러코드
	private String comErr;			// 통신에러상태
	private String comStatus;		// 통신상태
	private String dummyStatus;    // 더미전송상태
	private String sndcd;           // should send address
	
	private String data;			// RTU로부터 수신DATA
	private String ipAddr;			// RTU IP ADDRESS
	private String LT;              // last time 
	
	private Map<String,String> header;	// RTU 수신DATA HEADER정보
	private String originalData;		// RTU 수신 전체 DATA
	private String lastRemsSndDt;	// REMS로 DATA 전송한 최종일시
	private int responseCode;			// REMS로부터 받은 수신코드
	
	private RtuResVo rtuResVo;			// RTU로 응답할 DATA VO
	private RemsVo remsVo;				// REMS로 전송할 DATA VO
	
	private RemsAuthVo remsAuthVo;		// REMS 전송 인증정보 VO
	
	public String getCtn() {
		String ctn = null;
		if(this.imsi!=null && this.imsi.length()>=5 && this.imsi.startsWith("45006")) {
			ctn = "0"+this.imsi.substring(5);
		}
		return ctn;
	}
}
