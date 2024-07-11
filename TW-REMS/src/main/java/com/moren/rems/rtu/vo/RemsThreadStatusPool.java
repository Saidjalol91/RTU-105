package com.moren.rems.rtu.vo;

import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

/**
 * Thread상태 Pool
 * @author shlee
 */
public class RemsThreadStatusPool extends LinkedHashMap<String, String> {
	Logger log = Logger.getLogger(this.getClass());

	private static final long serialVersionUID = -5916770943081733335L;
	protected static RemsThreadStatusPool instance = null;
	
	/**
	 * Thread 상태 Pool 생성자
	 */
	public RemsThreadStatusPool() {
		initialize();
	}
	
	/**
	 * Thread 상태Pool 초기화
	 */
	public void initialize() {
		
	}
	
	/**
	 * Thread 상태 Pool 인스턴스 가져오기
	 * @return
	 */
	public static RemsThreadStatusPool getInstance() {
		if(instance!=null) return instance;
		synchronized(RemsThreadPool.class) {
			if(instance==null) instance = new RemsThreadStatusPool();
		}
		return instance;
	}
	
	/** 
	 * 모든 Thread 상태 설정하기
	 * @param status
	 */
	public static void setStatus(String status) {
		Iterator<String> iter = iterator();
		for(;iter.hasNext();) {
			String key = iter.next();
			getInstance().put(key, status);
		}
	}

	/**
	 * 특정 Thread 상태 설정하기
	 * @param key
	 * @param status
	 */
	public static void setStatus(String key, String status) {
		getInstance().put(key, status);
	}

	/**
	 * 특정 Thread 상태 설정하기
	 * @param vo
	 * @param status
	 */
	public static void setStatus(RtuStatusVo vo, String status) {
		String key = vo.getImei()+"/"+vo.getEnrgy()+("03".equals(vo.getEnrgy()) ? "/*/"+vo.getMulti() : "/"+vo.getMachn()+"/"+vo.getMulti());
		getInstance().put(key, status);
	}

	/**
	 * 특정 Thread 상태 가져오기
	 * @param key
	 * @return
	 */
	public static String getStatus(String key) {
		return getInstance().get(key);
	}
	
	/**
	 * 특정 Thread 상태 가져오기
	 * @param vo
	 * @return
	 */
	public static String getStatus(RtuStatusVo vo) {
		String key = vo.getImei()+"/"+vo.getEnrgy()+("03".equals(vo.getEnrgy()) ? "/*/"+vo.getMulti() : "/"+vo.getMachn()+"/"+vo.getMulti());
		return getInstance().get(key);
	}

	/**
	 * 특정 Thread상태 삭제하기
	 * @param key
	 * @return
	 */
	public static String removeStatus(String key) {
		return getInstance().remove(key);
	}
	
	/**
	 * 특정 Thread상태 삭제하기
	 * @param vo
	 * @return
	 */
	public static String removeStatus(RtuStatusVo vo) {
		String key = vo.getImei()+"/"+vo.getEnrgy()+("03".equals(vo.getEnrgy()) ? "/*/"+vo.getMulti() : "/"+vo.getMachn()+"/"+vo.getMulti());
		return getInstance().remove(key);
	}

	/**
	 * Thread Poo 의 Key 목록 가져오기
	 * @return
	 */
	public static Iterator<String> iterator() {
		return getInstance().keySet().iterator();
	}
}
