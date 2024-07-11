package com.moren.common.vo;

import com.roviet.common.tools.utility.AbsVo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 공통코드 Vo
 * @author shlee
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class CodeVo extends AbsVo {
	private String division;
	private String code1;
	private String code2;
	private String code3;
	private String value1;
	private String value2;
	private String value3;
	private int    orderNo;
	private int    regMemIdx;
	private String regDt;
	private String expDt;
}
