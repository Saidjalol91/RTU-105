package com.moren.rems.rtu.vo;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.roviet.common.tools.data.Config;
import com.roviet.common.tools.http.HttpCall;
import com.roviet.common.tools.utility.StringUtil;

/**
 * 토큰 Pool
 * @author shlee
 */
public class RemsTokenPool extends LinkedHashMap<String,Object> {
	
	Logger log = Logger.getLogger(this.getClass());

	String remsCallTokenUrl = Config.getProperty("rems.call.token.url");

	boolean remsCallDebug = Boolean.parseBoolean(Config.getProperty("rems.call.debug"));

	private static final long serialVersionUID = -5916770943081733335L;
	protected static RemsTokenPool instance = null;
	
	/**
	 * 토큰Pool 생성자
	 */
	public RemsTokenPool() {
	}
	
	/**
	 * 토큰풀 Clear 하기
	 */
	public void reset() {
		this.clear();
	}
	
	/**
	 * 토큰Pool 인스턴스 가져오기
	 * @return
	 */
	public static RemsTokenPool getInstance() {
		if(instance!=null) return instance;
		synchronized(RemsTokenPool.class) {
			if(instance==null) instance = new RemsTokenPool();
		}
		return instance;
	}
	
	/**
	 * cid 와 인증키로 토은정보 유효성 체크 후 생성하기
	 * @param cid
	 * @param authKey
	 * @return
	 */
	public static RemsTokenVo getRemsTokenVo(String cid, String authKey) {
		RemsTokenVo vo = (RemsTokenVo) getObject(cid+"/"+authKey);
		if(vo==null || "".equals(vo.getAccessToken()) || vo.isExpired()) {
			if(cid!=null && !"".equals(cid)) vo = getInstance().generateRemsTokenVo(cid, authKey);
		}
		return vo;
	}

	/**
	 * 토큰정보 기록하기
	 * @param remsTokenVo
	 */
	public static void setRemsTokenVo(RemsTokenVo remsTokenVo) {
		putObject(remsTokenVo.getCid()+"/"+remsTokenVo.getAuthKey(), remsTokenVo);
	}
	
	/**
	 * cid 와 인증키로 토큰정보 생성하기
	 * @param cid
	 * @param authKey
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private RemsTokenVo generateRemsTokenVo(String cid, String authKey) {
		Map<String,String> headerMap = new HashMap<String,String>();
		headerMap.put("CID", cid);
		headerMap.put("Authorization", authKey);
		
		RemsTokenVo vo = null;
		
		long nowTime = -1;
		try { nowTime = (GregorianCalendar.getInstance().getTime().getTime()/1000); } catch(Exception e) {}

		try {
			HttpCall http = new HttpCall();
			http.setDebug(remsCallDebug);
			http.setTimeout(10*1000);
			Map<String,Object> tokenResponse = http.getUrlCall(remsCallTokenUrl, (Object) "", "GET", headerMap);
			if(tokenResponse.get("response")!=null) {
				Map<String,Object> response = (Map<String,Object>) StringUtil.jsonToObject((String) tokenResponse.get("response"));
				vo = new RemsTokenVo();
				vo.setCid(cid);
				vo.setAuthKey(authKey);
				vo.setMap(response, true);
				vo.setGapTime(vo.getNow()-nowTime);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		setRemsTokenVo(vo);
		return vo;
	}

	/**
	 * Pool 에 객체 저장히기
	 * @param key
	 * @param value
	 */
	public static void putObject(String key, Object value) {
		getInstance().put(key, value);
	}
	
	/**
	 * Pool 에서 객체를 문자열로 가져오기
	 * @param key
	 * @return
	 */
	public static String getString(String key) {
		Object obj = getInstance().get(key);
		String ret = null;
		if (obj instanceof String) {
			ret = (String) obj;
		} else if (obj instanceof Integer) {
			ret = ((Integer) obj).toString();
		} else if(obj instanceof Short) {
			ret = ((Short) obj).toString();
		} else if(obj instanceof Long) {
			ret = ((Long) obj).toString();
		} else if(obj instanceof Float) {
			ret = ((Float) obj).toString();
		} else if(obj instanceof Double) {
			ret = ((Double) obj).toString();
		} else if(obj instanceof Boolean) {
			ret = (((boolean) obj) ? "true" : "false");
		} else {
			ret = (String) obj;
		}
		return ret;
	}
	
	/**
	 * Pool 에서 객체를 byte로 가져오기
	 * @param key
	 * @return
	 */
	public static byte getByte(String key) {
		Object obj = getInstance().get(key);
		byte ret = 0;
		if(obj instanceof Byte) {
			ret = (byte) obj;
		} else if (obj instanceof Integer) {
			ret = (byte) ((int) obj);
		} else if(obj instanceof Short) {
			ret = (byte) ((short) obj);
		} else if(obj instanceof Long) {
			ret = (byte) ((long) obj);
		} else if(obj instanceof Float) {
			ret = (byte) Math.round((float) obj);
		} else if(obj instanceof Double) {
			ret = (byte) Math.round((double) obj);
		} else if(obj instanceof Boolean) {
			ret = (byte) (((boolean) obj) ? 1 : 0);
		} else {
			ret = Byte.valueOf((String) obj,10);
		}
		return ret;
	}

	/**
	 * Pool 에서 객체를 short로 가져오기
	 * @param key
	 * @return
	 */
	public static short getShort(String key) {
		Object obj = getInstance().get(key);
		short ret = 0;
		if(obj instanceof Short) {
			ret = (short) ((short) obj);
		} else if (obj instanceof Integer) {
			ret = (short) ((int) obj);
		} else if(obj instanceof Byte) {
			ret = (short) ((byte) obj);
		} else if(obj instanceof Long) {
			ret = (short) ((long) obj);
		} else if(obj instanceof Float) {
			ret = (short) Math.round((float) obj);
		} else if(obj instanceof Double) {
			ret = (short) Math.round((double) obj);
		} else if(obj instanceof Boolean) {
			ret = (short) (((boolean) obj) ? 1 : 0);
		} else {
			ret = Short.valueOf((String) obj,10);
		}
		return ret;
	}

	/**
	 * Pool 에서 객체를 int로 가져오기
	 * @param key
	 * @return
	 */
	public static int getInt(String key) {
		Object obj = getInstance().get(key);
		int ret = 0;
		if (obj instanceof Integer) {
			ret = (int) obj;
		} else if(obj instanceof Byte) {
			ret = (int)((byte) obj);
		} else if(obj instanceof Short) {
			ret = (int) ((short) obj);
		} else if(obj instanceof Long) {
			ret = (int) ((long) obj);
		} else if(obj instanceof Float) {
			ret = Math.round((float) obj);
		} else if(obj instanceof Double) {
			ret = (int) Math.round((double) obj);
		} else if(obj instanceof Boolean) {
			ret = (int) (((boolean) obj) ? 1 : 0);
		} else {
			ret = Integer.parseInt((String) obj,10);
		}
		return ret;
	}
	
	/**
	 * Pool 에서 객체를 long로 가져오기
	 * @param key
	 * @return
	 */
	public static long getLong(String key) {
		Object obj = getInstance().get(key);
		long ret = 0;
		if(obj instanceof Long) {
			ret = (long) ((long) obj);
		} else if (obj instanceof Integer) {
			ret = (long) ((int) obj);
		} else if(obj instanceof Byte) {
			ret = (long) ((byte) obj);
		} else if(obj instanceof Short) {
			ret = (long) ((short) obj);
		} else if(obj instanceof Float) {
			ret = (long) Math.round((float) obj);
		} else if(obj instanceof Double) {
			ret = (long) Math.round((double) obj);
		} else if(obj instanceof Boolean) {
			ret = (long) (((boolean) obj) ? 1 : 0);
		} else {
			ret = Long.valueOf((String) obj,10);
		}
		return ret;
	}

	/**
	 * Pool 에서 객체를 float로 가져오기
	 * @param key
	 * @return
	 */
	public static float getFloat(String key) {
		Object obj = getInstance().get(key);
		float ret = 0;
		if(obj instanceof Float) {
			ret = (float) obj;
		} else if (obj instanceof Integer) {
			ret = (float) ((int) obj);
		} else if(obj instanceof Byte) {
			ret = (float) ((byte) obj);
		} else if(obj instanceof Short) {
			ret = (float) ((short) obj);
		} else if(obj instanceof Long) {
			ret = (float) ((long) obj);
		} else if(obj instanceof Double) {
			ret = (float) ((double) obj);
		} else if(obj instanceof Boolean) {
			ret = (float) (((boolean) obj) ? 1 : 0);
		} else {
			ret = Float.valueOf((String) obj);
		}
		return ret;
	}

	/**
	 * Pool 에서 객체를 double로 가져오기
	 * @param key
	 * @return
	 */
	public static double getDouble(String key) {
		Object obj = getInstance().get(key);
		double ret = 0;
		if(obj instanceof Double) {
			ret = (double) ((double) obj);
		} else if (obj instanceof Integer) {
			ret = (double) ((int) obj);
		} else if(obj instanceof Byte) {
			ret = (double) ((byte) obj);
		} else if(obj instanceof Short) {
			ret = (double) ((short) obj);
		} else if(obj instanceof Long) {
			ret = (double) ((long) obj);
		} else if(obj instanceof Float) {
			ret = (double) ((float) obj);
		} else if(obj instanceof Boolean) {
			ret = (double) (((boolean) obj) ? 1 : 0);
		} else {
			ret = Double.valueOf((String) obj);
		}
		return ret;
	}

	/**
	 * Pool 에서 객체를 boolean로 가져오기
	 * @param key
	 * @return
	 */
	public static boolean getBoolean(String key) {
		Object obj = getInstance().get(key);
		boolean ret = false;
		if(obj instanceof Boolean) {
			ret = (boolean) obj;
		} else if (obj instanceof Integer) {
			ret = (((int) obj)==0 ? false : true);
		} else if(obj instanceof Byte) {
			ret = (((byte) obj)==0 ? false : true);
		} else if(obj instanceof Short) {
			ret = (((short) obj)==0 ? false : true);
		} else if(obj instanceof Long) {
			ret = (((long) obj)==0 ? false : true);
		} else if(obj instanceof Float) {
			ret = (((float) obj)==0 ? false : true);
		} else if(obj instanceof Double) {
			ret = (((double) obj)==0 ? false : true);
		} else {
			ret = Boolean.valueOf((String) obj);
		}
		return ret;
	}
	
	/**
	 * Pool 에서 객체를 Object 가져오기
	 * @param key
	 * @return
	 */
	public static Object getObject(String key) {
		return getInstance().get(key);
	}
}
