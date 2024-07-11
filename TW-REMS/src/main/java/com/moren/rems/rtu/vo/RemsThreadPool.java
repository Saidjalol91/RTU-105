package com.moren.rems.rtu.vo;

import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

import com.moren.rems.rtu.service.RemsSndRateThread;

/**
 * REMS 더미 Thread POOL
 * @author shlee
 */
public class RemsThreadPool extends LinkedHashMap<String, RemsSndRateThread> {
	
	Logger log = Logger.getLogger(this.getClass());

	private static final long serialVersionUID = -5916770943081733335L;
	protected static RemsThreadPool instance = null;

	/**
	 * Thread Pool 인스턴스 가져오기
	 * @return
	 */
	public static RemsThreadPool getInstance() {
		if(instance!=null) return instance;
		synchronized(RemsThreadPool.class) {
			if(instance==null) instance = new RemsThreadPool();
		}
		return instance;
	}

	/**
	 * Thread Pool 에 Thread 기록
	 * @param key
	 * @param value
	 */
	public static void putObject(String key, RemsSndRateThread value) {
		getInstance().put(key, value);
	}

	/**
	 * Thread Pool에서 Thread 가져오기
	 * @param key
	 * @return
	 */
	public static RemsSndRateThread getObject(String key) {
		return getInstance().get(key);
	}
	
	/**
	 * Thread Pool에서 지정 Thread 제거
	 * @param key
	 * @return
	 */
	public static RemsSndRateThread removeObject(String key) {
		return getInstance().remove(key);
	}
	
	/**
	 * Thread Pool 에서 지정 Thread가져오기
	 * @param vo
	 * @return
	 */
	public static RemsSndRateThread getThread(RtuStatusVo vo) {
		return (RemsSndRateThread) ("03".equals(vo.getEnrgy()) ? getObject(vo.getImei()+"/"+vo.getEnrgy()+"/*/"+vo.getMulti()) : getObject(vo.getImei()+"/"+vo.getEnrgy()+"/"+vo.getMachn()+"/"+vo.getMulti()));
	}	

	/**
	 * Thread Pool 에서 지정 Thread 가져오기
	 * @param imei
	 * @param enrgy
	 * @param machn
	 * @param multi
	 * @return
	 */
	public static RemsSndRateThread getThread(String imei, String enrgy, String machn, String multi) {
		return (RemsSndRateThread) getObject(imei+"/"+enrgy+"/"+machn+"/"+multi);
	}

	/**
	 * Thread Pool 에 특정 Thread 가 있는 지 확인
	 * @param vo
	 * @return
	 */
	public static boolean containsKey(RtuStatusVo vo) {
		String key = ("03".equals(vo.getEnrgy()) ? vo.getImei()+"/"+vo.getEnrgy()+"/*/"+vo.getMulti() : vo.getImei()+"/"+vo.getEnrgy()+"/"+vo.getMachn()+"/"+vo.getMulti() );
		return getInstance().containsKey(key);
	}
	
	/**
	 * Thread Pool에 Thread 기록
	 * @param thread
	 * @throws Exception
	 */
	public static void setThread(RemsSndRateThread thread) throws Exception {
		RtuStatusVo vo = ((RemsSndRateThread) thread).getVo();
		String key = ("03".equals(vo.getEnrgy()) ? vo.getImei()+"/"+vo.getEnrgy()+"/*/"+vo.getMulti() : vo.getImei()+"/"+vo.getEnrgy()+"/"+vo.getMachn()+"/"+vo.getMulti() );
		if(getInstance().containsKey(key)) throw new Exception("Already exists key["+key+"] thread.");
		putObject(key, thread);
	}

	/**
	 * Thread Pool에서 지정 Thread 삭제
	 * @param vo
	 * @return
	 * @throws Exception
	 */
	public static RemsSndRateThread removeThread(RtuStatusVo vo) throws Exception {
		RemsSndRateThread thread = null;
		String key = ("03".equals(vo.getEnrgy()) ? vo.getImei()+"/"+vo.getEnrgy()+"/*/"+vo.getMulti() : vo.getImei()+"/"+vo.getEnrgy()+"/"+vo.getMachn()+"/"+vo.getMulti() );
		if(getInstance().containsKey(key)) thread = removeObject(key);
		return thread;
	}
	
	/**
	 * RTU 가 살아 있음을 기록
	 * @param vo
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static boolean setRtuAlive(RtuStatusVo vo) {
		RemsSndRateThread thread = null;
		boolean isThread = false;
		String key = ("03".equals(vo.getEnrgy()) ? vo.getImei()+"/"+vo.getEnrgy()+"/*/"+vo.getMulti() : vo.getImei()+"/"+vo.getEnrgy()+"/"+vo.getMachn()+"/"+vo.getMulti() );
		if(getInstance().containsKey(key)) {
			thread = (RemsSndRateThread) getInstance().getThread(vo);
			thread.setRtuAlive(true);
			isThread = true;
		}
		return isThread;
	}
	
	/**
	 * Thread 목록 Key 가져오기
	 * @return
	 */
	public static Iterator<String> iterator() {
		Iterator<String> iter = getInstance().keySet().iterator();
		return iter;
	}
}
