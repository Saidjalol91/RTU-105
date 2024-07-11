package com.moren.rems.rtu.vo;

import java.util.Map;

import com.roviet.common.tools.utility.AbsVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * RTU 정보 Vo
 * @author shlee
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class RtuVo extends AbsVo {
	private String rtuIdx;		// RTU 번호
	private String imei;		// RTU IMEI
	private String devId;		// RTU Device Id
	private String serialNo;	// RTU SerialNo
	private String manufDate;	// RTU 제조일자
	private String ctn;		    // LG과금코드
	private String imsi;		// RTU IMSI
	private String location;	// 위치
	private String ipAddr;		// IP Address
	private String useStatus;	// 사용어부
	private String sndcd;      // destination address
	private String fwVersion;	// Firmware 버전
	private String build;
	
	private String fwUpStatus;	// 펌웨어 업데이트 상태
	private String resetStatus;	// 장치 RESET 상태
	private String lastComDt;	// 마지막통신일시
	
	private String regDt;
	private String modDt;
	private String expDt;
	private int    regMemIdx;
	private int    expMemIdx;
	private Map<String,RtuStatusVo> rtuStatusVoMap;
}
