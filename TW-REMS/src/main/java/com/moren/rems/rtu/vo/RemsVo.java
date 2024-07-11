package com.moren.rems.rtu.vo;

import java.util.Map;

import com.roviet.common.tools.utility.AbsVo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * REMS 전송용 Vo
 * @author shlee
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class RemsVo extends AbsVo {
	private Map<String,String> header;
	private String cid;
	private int multi;
	private String data;
	private int responseCode;
}
