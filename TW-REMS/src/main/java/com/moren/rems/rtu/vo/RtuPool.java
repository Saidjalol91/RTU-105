package com.moren.rems.rtu.vo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.moren.rems.rtu.service.RemsRtuDbService;
import com.roviet.common.tools.utility.StringUtil;
import com.roviet.common.web.tools.ServiceUtil;

/**
 * RTU 정보 Pool
 * @author shlee
 */
public class RtuPool extends LinkedHashMap<String,Object> {
	
	Logger log = Logger.getLogger(this.getClass());

	private static final long serialVersionUID = -5916770943081733335L;
	protected static RtuPool instance = null;
	static RemsRtuDbService staticRemsRtuDbService = (RemsRtuDbService) ServiceUtil.getServiceBean("remsRtuDbService");
	RemsRtuDbService remsRtuDbService = (RemsRtuDbService) ServiceUtil.getServiceBean("remsRtuDbService");
	
	/** 
	 * RTU Pool 생성자
	 */
	public RtuPool() {
		initialize(); // instance olib olish uchun shu yerdan boshlanadi
	}
	
	/**
	 * RTU Pool Clear 하고 초기화
	 */
	public void reset() {
		this.clear();
		initialize();
	}
	
	/**
	 * RTU Pool 초기화
	 */
	public void initialize() {
		List<RtuVo> rtuVoList = (List<RtuVo>) remsRtuDbService.getRtuInfo();
		RtuVo rtuVo = null;
		for(int i=0 ; rtuVoList!=null && i<rtuVoList.size(); i++) {
			rtuVo = rtuVoList.get(i);
			if(!this.containsKey(rtuVo.getImei())) {
				this.put(rtuVo.getImei(), rtuVo);
			}
			rtuVo = (RtuVo) this.get(rtuVo.getImei());
			if(rtuVo.getRtuStatusVoMap()==null) {
				rtuVo.setRtuStatusVoMap(new LinkedHashMap<String, RtuStatusVo>());
				Map<String,RtuStatusVo> statusMap = rtuVo.getRtuStatusVoMap();
				List<RtuStatusVo> rtuStatusList = (List<RtuStatusVo>) remsRtuDbService.getRtuStatusInfo(rtuVo.getImei());
				for(int s=0; rtuStatusList!=null && s<rtuStatusList.size(); s++) {
					RtuStatusVo rtuStatusVo = rtuStatusList.get(s);
					String rtuStatusKey = rtuStatusVo.getImei()+"/"+rtuStatusVo.getEnrgy()+"/"+rtuStatusVo.getMachn()+"/"+rtuStatusVo.getMulti();
					statusMap.put(rtuStatusKey, rtuStatusVo);
				}
			}
		}
	}
	
	/**
	 * IMEI 로 한개의 RTU정보 초기화
	 * @param imei
	 */
	public void initialize(String imei) {
		RtuVo rtuVo = remsRtuDbService.getRtuInfoByImei(imei);
		if(!this.containsKey(rtuVo.getImei())) {
			this.put(rtuVo.getImei(), rtuVo);
		}
		rtuVo = (RtuVo) this.get(rtuVo.getImei());
		if(rtuVo.getRtuStatusVoMap()==null) rtuVo.setRtuStatusVoMap(new LinkedHashMap<String, RtuStatusVo>());
		Map<String,RtuStatusVo> statusMap = rtuVo.getRtuStatusVoMap();
		List<RtuStatusVo> rtuStatusList = (List<RtuStatusVo>) remsRtuDbService.getRtuStatusInfo(rtuVo.getImei());
		for(int s=0; rtuStatusList!=null && s<rtuStatusList.size(); s++) {
			RtuStatusVo rtuStatusVo = rtuStatusList.get(s);
			String rtuStatusKey = rtuStatusVo.getImei()+"/"+rtuStatusVo.getEnrgy()+"/"+rtuStatusVo.getMachn()+"/"+rtuStatusVo.getMulti();
			statusMap.put(rtuStatusKey, rtuStatusVo);
		}
	}
	
	/**
	 * RTU Pool 인스턴스 가져오기
	 * @return
	 */
	public static RtuPool getInstance() {
		if(instance!=null) return instance;
		synchronized(RtuPool.class) {
			if(instance==null) instance = new RtuPool();
		}
		return instance;
	}
	
	/**
	 * IMEI 로 RTU 정보 가져오기
	 * @param imei
	 * @return
	 */
	public static RtuVo getRtuVo(String imei) {
		return (RtuVo) getObject(imei);
	}
	
	/**
	 * IMEI로 RTU 정보 생성
	 * @param imei
	 * @return
	 */
	public static RtuVo generateRtuVo(String imei) {
		RtuVo rtuVo = (RtuVo) staticRemsRtuDbService.getRtuInfoByImei(imei);
		if(rtuVo==null) {
			rtuVo = new RtuVo();
			rtuVo.setImei(imei);
			// CTN 은 IMSI 에서 45006을 제거하고 앞에 0을 붙인다.
			if(StringUtil.isEmpty(rtuVo.getCtn()) && rtuVo.getImsi()!=null && rtuVo.getImsi().startsWith("45006")) rtuVo.setCtn("0"+rtuVo.getImsi().substring(5));
			rtuVo.setUseStatus("D");
			staticRemsRtuDbService.insertRtu(rtuVo);
		}
		if(!getInstance().containsKey(rtuVo.getImei())) putObject(rtuVo.getImei(), rtuVo);
		rtuVo = (RtuVo) getRtuVo(imei);
		if(rtuVo!=null && rtuVo.getRtuStatusVoMap()==null) rtuVo.setRtuStatusVoMap(new LinkedHashMap<String, RtuStatusVo>());
		return rtuVo;
	}
	
	/**
	 * RTU 정보 Pool 에 기록
	 * @param rtuVo
	 */
	public static void setRtuVo(RtuVo rtuVo) {
		putObject(rtuVo.getImei(), rtuVo);
	}
	
	/**
	 * 발전 정보로 상태값 가져오기
	 * @param imei
	 * @param enrgy
	 * @param machn
	 * @param multi
	 * @return
	 */
	public static RtuStatusVo getRtuStatusVo(String imei, String enrgy, String machn, String multi) {
		RtuVo rtuVo = getRtuVo(imei);
		if(rtuVo==null) return null;
		Map<String,RtuStatusVo> rtuStatusVoMap = rtuVo.getRtuStatusVoMap();
		String key = imei+"/"+enrgy+"/"+machn+"/"+multi;
		if(rtuStatusVoMap==null || !rtuStatusVoMap.containsKey(key)) return null;
		return (RtuStatusVo) rtuStatusVoMap.get(key);
	}
	
	/**
	 * 발전정보로 발전정보 객체 생성하기
	 * @param imei
	 * @param enrgy
	 * @param machn
	 * @param multi
	 * @param header
	 * @param data
	 * @param addInfo
	 * @return
	 * @throws JsonMappingException
	 * @throws JsonProcessingException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	public static RtuStatusVo generateRtuStatusVo(String imei, String enrgy, String machn, String multi, Map<String,String> header, String data, Map<String,Object> addInfo) 
			throws JsonMappingException, JsonProcessingException, IllegalAccessException {
		RtuStatusVo rtuStatusVo = null;
		rtuStatusVo = new RtuStatusVo();
		rtuStatusVo.setHeader(header);
		rtuStatusVo.setOriginalData(data);
		
		Map<String,Object> dataMap = (Map<String,Object>) StringUtil.jsonToObject(data);

		rtuStatusVo.setMap(dataMap, true);
		if(rtuStatusVo.getData()!=null) {
			rtuStatusVo.setEnrgy(rtuStatusVo.getData().length()>=4 ? rtuStatusVo.getData().substring(2,4) : "");
			rtuStatusVo.setMachn(rtuStatusVo.getData().length()>=6 ? rtuStatusVo.getData().substring(4,6) : "");
			rtuStatusVo.setMulti(rtuStatusVo.getData().length()>=8 ? rtuStatusVo.getData().substring(6,8) : "");
			rtuStatusVo.setErrCode(rtuStatusVo.getData().length()>=10 ? rtuStatusVo.getData().substring(8,10) : "");
			rtuStatusVo.setIpAddr(addInfo==null ? null : (String) addInfo.get("ipAddress"));
		}
		generateRtuStatusVo(rtuStatusVo);
		return rtuStatusVo;
	}
	
	/**
	 * 발전정보로 발전정보 객체 생성하기
	 * @param rtuStatusVo
	 * @return
	 */
	public static RtuStatusVo generateRtuStatusVo(RtuStatusVo rtuStatusVo) {
		if(rtuStatusVo==null) return null;
		if(rtuStatusVo.getImsi()==null || rtuStatusVo.getFwVer()==null) return rtuStatusVo;

		RtuVo rtuVo = RtuPool.getRtuVo(rtuStatusVo.getImei());
		if(rtuVo==null) rtuVo = RtuPool.generateRtuVo(rtuStatusVo.getImei());
		else {
			boolean isChangedRtuInfo = false;
			if(!rtuStatusVo.getImsi().equals(rtuVo.getImsi())) isChangedRtuInfo = true;
			if(!rtuStatusVo.getFwVer().equals(rtuVo.getFwVersion())) isChangedRtuInfo = true;
			if("W".equals(rtuVo.getFwUpStatus())) {rtuVo.setFwUpStatus("E"); isChangedRtuInfo = true;}
			if("W".equals(rtuVo.getResetStatus())) {rtuVo.setResetStatus("E"); isChangedRtuInfo = true;}
			if(isChangedRtuInfo)  {
				if(staticRemsRtuDbService==null) staticRemsRtuDbService = (RemsRtuDbService) ServiceUtil.getServiceBean("remsRtuDbService");
				rtuVo.setImsi(rtuStatusVo.getImsi());
				rtuVo.setCtn(rtuStatusVo.getCtn());
				rtuVo.setFwVersion(rtuStatusVo.getFwVer());
				staticRemsRtuDbService.updateRtuInfo(rtuVo);
			}
		}
		String statusKey = rtuStatusVo.getImei()+"/"+rtuStatusVo.getEnrgy()+"/"+rtuStatusVo.getMachn()+"/"+rtuStatusVo.getMulti();
		Map<String,RtuStatusVo> voMap = rtuVo.getRtuStatusVoMap();
		if(!voMap.containsKey(statusKey)) {
			if(staticRemsRtuDbService==null) staticRemsRtuDbService = (RemsRtuDbService) ServiceUtil.getServiceBean("remsRtuDbService");
			voMap.put(statusKey, rtuStatusVo);
			staticRemsRtuDbService.insertRtuEnrgyType(rtuStatusVo);
		} else {
			boolean isChangedStatusInfo = false;
			RtuStatusVo preVo = voMap.get(statusKey);
			voMap.put(statusKey, rtuStatusVo);
			if(preVo.getPeriod()!=rtuStatusVo.getPeriod()) isChangedStatusInfo = true;
			if(isChangedStatusInfo) {
				if(staticRemsRtuDbService==null) staticRemsRtuDbService = (RemsRtuDbService) ServiceUtil.getServiceBean("remsRtuDbService");
				staticRemsRtuDbService.updateRtuEnrgyTypeInfo(rtuStatusVo);
			}
		}
		
		return rtuStatusVo;
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
