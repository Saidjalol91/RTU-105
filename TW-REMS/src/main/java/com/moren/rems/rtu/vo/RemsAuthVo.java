package com.moren.rems.rtu.vo;

import com.roviet.common.tools.utility.AbsVo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * REMS 인증정보 Vo
 * @author shlee
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class RemsAuthVo extends AbsVo {
	private String imei;
	private String enrgy;
	private String machn;
	private String multi;
	private String cid;
	private String authKey;
}
