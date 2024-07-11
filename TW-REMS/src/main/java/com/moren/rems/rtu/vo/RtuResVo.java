package com.moren.rems.rtu.vo;

import com.roviet.common.tools.utility.AbsVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * RTU 응답 Vo
 * @author shlee
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class RtuResVo extends AbsVo {
	private boolean success = false;
	private int     FW_UPDATE = 0;
	private String  FW_URL;
	private int     RESET = 0;
}
