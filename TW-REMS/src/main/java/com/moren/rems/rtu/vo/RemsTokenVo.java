package com.moren.rems.rtu.vo;

import java.util.GregorianCalendar;

import com.roviet.common.tools.utility.AbsVo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 토큰정보 Vo
 * @author shlee
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class RemsTokenVo extends AbsVo {
	private String cid;
	private String authKey;
	
	private String rtuId;
	private String accessToken;
	private long   now;
	private long   gapTime;
	private long   expired;
	private int    responseCode;
	
	public boolean isExpired() {
		boolean isExpired = false;
		long nowTime = -1;
		try { nowTime = (GregorianCalendar.getInstance().getTime().getTime()/1000); } catch(Exception e) {}
		
		if(nowTime<0 || this.getExpired()<(nowTime+this.getGapTime()-10)) isExpired = true;
		return isExpired;
	}
}
