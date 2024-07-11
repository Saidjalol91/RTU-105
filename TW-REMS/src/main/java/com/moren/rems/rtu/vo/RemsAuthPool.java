package com.moren.rems.rtu.vo;

import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.moren.rems.rtu.dao.RemsRtuDao;
import com.roviet.common.web.tools.ServiceUtil;

/**
 * REMS 인증정보 저장 POOL
 * @author shlee
 */
public class RemsAuthPool extends LinkedHashMap<String,Object> {
	
	Logger log = Logger.getLogger(this.getClass());

	private static final long serialVersionUID = -5916770943081733335L;
	protected static RemsAuthPool instance = null;
	
	RemsRtuDao remsRtuDao = (RemsRtuDao) ServiceUtil.getServiceBean("remsRtuDao");

	/**
	 * REMS 인정정보 저장 Pool 생성자
	 */
	public RemsAuthPool() {
		initialize();
	}
	

	/**
	 * Pool 에서 객체를 인증정보 Vo 로 가져오기
	 * @param imei
	 * @param enrgy
	 * @param machn
	 * @param multi
	 * @return
	 */
	public static RemsAuthVo getRemsAuthVo(String imei, String enrgy, String machn, String multi) {
		return (RemsAuthVo) getObject(imei+"/"+enrgy+"/"+machn+"/"+multi);
	}

	/** 
	 * Pool 에 인증정보를 기록하기
	 * @param remsAuthVo
	 */
	public static void setRemsAuthVo(RemsAuthVo remsAuthVo) {
		putObject(remsAuthVo.getImei()+"/"+remsAuthVo.getEnrgy()+"/"+remsAuthVo.getMachn()+"/"+remsAuthVo.getMulti(), remsAuthVo);
	}
	
	/**
	 * REMS 인정정보 저장 Pool 데이터 지우고 초기화 하기
	 */
	public void reset() {
		this.clear();
		initialize();
	}
	
	/**
	 * REMS 인정정보 저장 Pool 초기화 하기
	 */
	@SuppressWarnings("unchecked")
	public void initialize() {
		List<RemsAuthVo> remsAuthList = (List<RemsAuthVo>) remsRtuDao.selectList("RemsRtuDao.getRemsAuthInfo");
		remsAuthList.forEach(remsAuthVo -> this.put(remsAuthVo.getImei()+"/"+remsAuthVo.getEnrgy()+"/"+remsAuthVo.getMachn()+"/"+remsAuthVo.getMulti(),remsAuthVo));
	}
	
	/**
	 * REMS 인정정보 저장 Pool 인스턴스 가져오기
	 * @return
	 */
	public static RemsAuthPool getInstance() {
		if(instance!=null) return instance;
		synchronized(RemsAuthPool.class) {
			if(instance==null) instance = new RemsAuthPool();
		}
		return instance;
	}
	
	/**
	 * REMS 인정정보 Key 로 가져오기
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
