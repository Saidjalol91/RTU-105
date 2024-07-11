package com.moren.rems.rtu.service;

import java.io.IOException;

import java.io.InputStream;
import java.sql.Timestamp;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import com.moren.rems.rtu.dao.RemsRtuDao;
import com.moren.rems.rtu.vo.RemsTokenVo;
import com.moren.rems.rtu.vo.RemsVo;
import com.moren.rems.rtu.vo.RemsAuthPool;
import com.moren.rems.rtu.vo.RemsAuthVo;
import com.moren.rems.rtu.vo.RemsThreadPool;
import com.moren.rems.rtu.vo.RemsTokenPool;
import com.moren.rems.rtu.vo.RtuPool;
import com.moren.rems.rtu.vo.RtuResVo;
import com.moren.rems.rtu.vo.RtuStatusVo;
import com.moren.rems.rtu.vo.RtuVo;
import com.roviet.common.tools.http.HttpCall;
import com.roviet.common.tools.utility.NumberUtil;
import com.roviet.common.tools.utility.StringUtil;

/**
 * RTU 수신 발전정보 RELAY 용 Main Service
 * 
 * @author shlee
 */
@Service
public class RemsRtuService {

	Logger log = Logger.getLogger(this.getClass());
	Logger remsLog = Logger.getLogger("rems.log");

	@Value("${rems.call.token.url}")
	String remsCallTokenUrl;

	@Value("${rems.call.data.url}")
	String remsCallDataUrl;

	@Value("${cbems.call.data.url}")
	String mailCallDataUrl;

	@Value("${ems.call.data.url}")
	String emsCallDataUrl;

	@Value("${chenergy.call.data.url}")
	String cheonanCallDataUrl;

	@Value("${rems.call.debug}")
	boolean remsCallDebug = false;

	@Value("${service.domain}")
	String serviceDomain;

	@Autowired
	RemsRtuDao remsRtuDao;

	@Autowired
	RemsRtuDbService remsRtuDbService;

	@Autowired
	RemsRtuNoticeService remsRtuNoticeService;

	/**
	 * RTU 로부터 DATA 수신
	 * 
	 * @param param
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> receiveData(Map<String, Object> param, ModelMap model, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> statusMap = new HashMap<String, Object>();

		RtuStatusVo rtuStatusVo = null;

		// 메시지 수신부
		try {
			rtuStatusVo = receiveDataFromRTU(request);
		} catch (Exception e) {

			statusMap = setStatus(statusMap, "RECEIVE_FAIL", e);

			remsRtuNoticeService.noticeRemsRtuStatus("IMEI_MATCH_FAIL", rtuStatusVo);
		}

		// Saidjalol changed part

		String sendAddress = rtuStatusVo.getSndcd();

		if (sendAddress.equals("00")) {
			// 메시지 변환부
			try {
				rtuStatusVo = convertRtuDataToRemsData(rtuStatusVo);
			} catch (Exception e) {

				rtuStatusVo = RtuPool.generateRtuStatusVo(rtuStatusVo);
				statusMap = setStatus(statusMap, "MSG_CONVERT_FAIL", e);

				remsRtuNoticeService.noticeRemsRtuStatus("IMEI_MATCH_FAIL", rtuStatusVo);
			}

			// 메시지 전송부
			try {
				if (rtuStatusVo.getRemsVo() != null)
					rtuStatusVo = sendRemsData(rtuStatusVo);
			} catch (Exception e) {
				statusMap = setStatus(statusMap, "REMS_SND_RCV_FAIL", e);
			}

			// 메시지 응답부
			try {
				rtuStatusVo = makeRtuResponseData(rtuStatusVo);
				if (rtuStatusVo.getRtuResVo() != null)
					resultMap.putAll(rtuStatusVo.getRtuResVo().getMap("success", "FW_UPDATE", "FW_URL", "RESET"));
			} catch (Exception e) {
				if (rtuStatusVo.getRtuResVo() != null)
					resultMap.putAll(rtuStatusVo.getRtuResVo().getMap("success", "FW_UPDATE", "FW_URL", "RESET"));
				statusMap = setStatus(statusMap, "RESPONSE_FAIL", e);
			}

		} else {
			String data = rtuStatusVo.getData();
			Character ch = data.charAt(3);
			String energySource = Character.toString(ch);

			if ((sendAddress.equals("01") || sendAddress.equals("02"))) {
				// Server Select and sending logic part
				try {
					if (energySource.equals("1")) {
						Character energyType = data.charAt(5);

						StringBuilder output = new StringBuilder();

						for (int i = 0; i < data.length(); i++) {
							if (i % 2 == 0 && i != 0) {
								output.append(",");
							}
							output.append(data.charAt(i));

						}
						String result = output.toString();

						String[] hexStrings = result.split(",");
						StringBuilder decimalString = new StringBuilder();

						for (int i = 0; i < hexStrings.length; i++) {
							int intValue = Integer.parseInt(hexStrings[i], 16); // Parse hex string to integer
							int twoDigits = intValue & 0xFF; // Get 2's complement representation with 2 digits
							if ((intValue >> 7 & 1) == 1) { // Check if most significant bit is set
								twoDigits -= 256; // Subtract 256 to get negative value
							}
							int decimalValue = twoDigits < 0 ? twoDigits - 256 : twoDigits; // Get decimal equivalent
							decimalString.append(decimalValue).append(",");
						}

						// Remove trailing comma from decimal string
						if (decimalString.length() > 0) {
							decimalString.deleteCharAt(decimalString.length() - 1);
						}

						String decimalData = "[" + decimalString.toString() + "]"; // ready decimal data
						String imei = rtuStatusVo.getImei();

						HttpCall http = new HttpCall();
						http.setTimeout(10 * 1000);
						Integer responseServer = null;

						if (sendAddress.equals("01")) {
							Timestamp timestamp = new Timestamp(System.currentTimeMillis());
							if (energyType.equals('1')) // 단상
							{
								String body = "{\r\n" + "\"connectionId\":\"KNREC_CTRL\",\r\n" + "\"deviceId\":\""
										+ imei + "\" ,\r\n" + "\"groupTag\":\"DeliveryMO\",\r\n" + "\"timestamp\":\""
										+ timestamp + "\",\r\n" + "\"serviceId\":\"001NBI109\",\r\n"
										+ "\"modelId\":\"KNREC-01\",\r\n" + "\"targetSequence\":6000126399,\r\n"
										+ "\"deviceSequence\":132,\r\n" + "\"attributes\":\r\n" + "{\r\n"
										+ "\"14000005\":\r\n" + "{\r\n" + "\"hb\":" + decimalData + ",\r\n"
										+ "\"offset\":0,\r\n" + "\"isReadOnly\":false,\r\n" + "\"bigEndian\":true,\r\n"
										+ "\"nativeByteOrder\":false,\r\n" + "\"mark\":-1,\r\n" + "\"position\":0,\r\n"
										+ "\"limit\":31,\r\n" + "\"capacity\":31,\r\n" + "\"address\":0\r\n" + "},\r\n"
										+ "\"14000001\":\"18722405\", \r\n" + "\"14000002\":-92.8,\r\n"
										+ "\"14000003\":-10.8,\r\n" + "\"14000004\":14.6\r\n" + "}\r\n" + "}";
								Map<String, Object> emsResponse = http.getUrlCall(mailCallDataUrl, body, "POST", null);
								responseServer = (emsResponse != null && emsResponse.containsKey("responseCode")
										? NumberUtil.convertToInt(emsResponse.get("responseCode"), -1)
										: -1);
								RtuVo rtuVo = RtuPool.getRtuVo(rtuStatusVo.getImei());

								if (responseServer.equals(200)) {
									rtuVo.setLastComDt(timestamp.toString());
								}

								RtuResVo rtuResVo = (rtuStatusVo.getRtuResVo() == null ? new RtuResVo()
										: rtuStatusVo.getRtuResVo());
								rtuStatusVo.setRtuResVo(rtuResVo);

								rtuResVo.setSuccess(responseServer != null && responseServer == 200 ? true : false);
								rtuResVo.setFW_UPDATE(0);
								rtuResVo.setFW_URL("");
								rtuResVo.setRESET(0);

								rtuStatusVo.setRtuResVo(rtuResVo);

								try {
									if (rtuStatusVo.getRtuResVo() != null)
										resultMap.putAll(rtuStatusVo.getRtuResVo().getMap("success", "FW_UPDATE",
												"FW_URL", "RESET"));
								} catch (Exception e) {
									if (rtuStatusVo.getRtuResVo() != null)
										resultMap.putAll(rtuStatusVo.getRtuResVo().getMap("success", "FW_UPDATE",
												"FW_URL", "RESET"));
									statusMap = setStatus(statusMap, "RESPONSE_FAIL", e);
								}
							} else {
								String body = "{\r\n" + "\"connectionId\":\"KNREC_CTRL\",\r\n" + "\"deviceId\":\""
										+ imei + "\" ,\r\n" + "\"groupTag\":\"DeliveryMO\",\r\n" + "\"timestamp\":\""
										+ timestamp + "\",\r\n" + "\"serviceId\":\"001NBI109\",\r\n"
										+ "\"modelId\":\"KNREC-01\",\r\n" + "\"targetSequence\":6000126399,\r\n"
										+ "\"deviceSequence\":169,\r\n" + "\"attributes\":\r\n" + "{\r\n"
										+ "\"14000005\":\r\n" + "{\r\n" + "\"hb\":" + decimalData + ",\r\n"
										+ "\"offset\":0,\r\n" + "\"isReadOnly\":false,\r\n" + "\"bigEndian\":true,\r\n"
										+ "\"nativeByteOrder\":false,\r\n" + "\"mark\":-1,\r\n" + "\"position\":0,\r\n"
										+ "\"limit\":43,\r\n" + "\"capacity\":43,\r\n" + "\"address\":0\r\n" + "},\r\n"
										+ "\"14000001\":\"18738792\", \r\n" + "\"14000002\":-64.8,\r\n"
										+ "\"14000003\":-10.8,\r\n" + "\"14000004\":20.0\r\n" + "}\r\n" + "}";
								Map<String, Object> emsResponse = http.getUrlCall(mailCallDataUrl, body, "POST", null);
								responseServer = (emsResponse != null && emsResponse.containsKey("responseCode")
										? NumberUtil.convertToInt(emsResponse.get("responseCode"), -1)
										: -1);
								RtuVo rtuVo = RtuPool.getRtuVo(rtuStatusVo.getImei());

								if (responseServer.equals(200)) {
									rtuVo.setLastComDt(timestamp.toString());
								}

								RtuResVo rtuResVo = (rtuStatusVo.getRtuResVo() == null ? new RtuResVo()
										: rtuStatusVo.getRtuResVo());
								rtuStatusVo.setRtuResVo(rtuResVo);

								rtuResVo.setSuccess(responseServer != null && responseServer == 200 ? true : false);
								rtuResVo.setFW_UPDATE(0);
								rtuResVo.setFW_URL("");
								rtuResVo.setRESET(0);

								rtuStatusVo.setRtuResVo(rtuResVo);

								try {
									if (rtuStatusVo.getRtuResVo() != null)
										resultMap.putAll(rtuStatusVo.getRtuResVo().getMap("success", "FW_UPDATE",
												"FW_URL", "RESET"));
								} catch (Exception e) {
									if (rtuStatusVo.getRtuResVo() != null)
										resultMap.putAll(rtuStatusVo.getRtuResVo().getMap("success", "FW_UPDATE",
												"FW_URL", "RESET"));
									statusMap = setStatus(statusMap, "RESPONSE_FAIL", e);
								}
							}
						} else {
							// "02" data converting and sending to
							// https://www.ems-jincheon.kr/teems/IotMakers.do
							Timestamp timestamp = new Timestamp(System.currentTimeMillis());
							if (energyType.equals('1')) // 단상
							{
								String body = "{\r\n" + "\"connectionId\":\"KNREC_CTRL\",\r\n" + "\"deviceId\":\""
										+ imei + "\" ,\r\n" + "\"groupTag\":\"DeliveryMO\",\r\n" + "\"timestamp\":\""
										+ timestamp + "\",\r\n" + "\"serviceId\":\"001NBI109\",\r\n"
										+ "\"modelId\":\"KNREC-01\",\r\n" + "\"targetSequence\":6000126399,\r\n"
										+ "\"deviceSequence\":169,\r\n" + "\"attributes\":\r\n" + "{\r\n"
										+ "\"14000005\":\r\n" + "{\r\n" + "\"hb\":" + decimalData + ",\r\n"
										+ "\"offset\":0,\r\n" + "\"isReadOnly\":false,\r\n" + "\"bigEndian\":true,\r\n"
										+ "\"nativeByteOrder\":false,\r\n" + "\"mark\":-1,\r\n" + "\"position\":0,\r\n"
										+ "\"limit\":31,\r\n" + "\"capacity\":31,\r\n" + "\"address\":0\r\n" + "},\r\n"
										+ "\"14000001\":\"18745451\", \r\n" + "\"14000002\":75.7,\r\n"
										+ "\"14000003\":-10.8,\r\n" + "\"14000004\":20.0\r\n" + "}\r\n" + "}";
								Map<String, Object> emsResponse = http.getUrlCall(emsCallDataUrl, body, "POST", null);
								responseServer = (emsResponse != null && emsResponse.containsKey("responseCode")
										? NumberUtil.convertToInt(emsResponse.get("responseCode"), -1)
										: -1);
								RtuVo rtuVo = RtuPool.getRtuVo(rtuStatusVo.getImei());

								if (responseServer.equals(200)) {
									rtuVo.setLastComDt(timestamp.toString());
								}

								RtuResVo rtuResVo = (rtuStatusVo.getRtuResVo() == null ? new RtuResVo()
										: rtuStatusVo.getRtuResVo());
								rtuStatusVo.setRtuResVo(rtuResVo);

								rtuResVo.setSuccess(responseServer != null && responseServer == 200 ? true : false);
								rtuResVo.setFW_UPDATE(0);
								rtuResVo.setFW_URL("");
								rtuResVo.setRESET(0);

								rtuStatusVo.setRtuResVo(rtuResVo);

								try {
									if (rtuStatusVo.getRtuResVo() != null)
										resultMap.putAll(rtuStatusVo.getRtuResVo().getMap("success", "FW_UPDATE",
												"FW_URL", "RESET"));
								} catch (Exception e) {
									if (rtuStatusVo.getRtuResVo() != null)
										resultMap.putAll(rtuStatusVo.getRtuResVo().getMap("success", "FW_UPDATE",
												"FW_URL", "RESET"));
									statusMap = setStatus(statusMap, "RESPONSE_FAIL", e);
								}

							} else {
								String body = "{\r\n" + "\"connectionId\":\"KNREC_CTRL\",\r\n" + "\"deviceId\":\""
										+ imei + "\" ,\r\n" + "\"groupTag\":\"DeliveryMO\",\r\n" + "\"timestamp\":\""
										+ timestamp + "\",\r\n" + "\"serviceId\":\"001NBI109\",\r\n"
										+ "\"modelId\":\"KNREC-01\",\r\n" + "\"targetSequence\":6000126399,\r\n"
										+ "\"deviceSequence\":169,\r\n" + "\"attributes\":\r\n" + "{\r\n"
										+ "\"14000005\":\r\n" + "{\r\n" + "\"hb\":" + decimalData + ",\r\n"
										+ "\"offset\":0,\r\n" + "\"isReadOnly\":false,\r\n" + "\"bigEndian\":true,\r\n"
										+ "\"nativeByteOrder\":false,\r\n" + "\"mark\":-1,\r\n" + "\"position\":0,\r\n"
										+ "\"limit\":43,\r\n" + "\"capacity\":43,\r\n" + "\"address\":0\r\n" + "},\r\n"
										+ "\"14000001\":\"18738792\", \r\n" + "\"14000002\":-64.8,\r\n"
										+ "\"14000003\":-10.8,\r\n" + "\"14000004\":20.0\r\n" + "}\r\n" + "}";

								Map<String, Object> emsResponse = http.getUrlCall(emsCallDataUrl, body, "POST", null);
								responseServer = (emsResponse != null && emsResponse.containsKey("responseCode")
										? NumberUtil.convertToInt(emsResponse.get("responseCode"), -1)
										: -1);

								RtuVo rtuVo = RtuPool.getRtuVo(rtuStatusVo.getImei());

								if (responseServer.equals(200)) {
									rtuVo.setLastComDt(timestamp.toString());
								}

								RtuResVo rtuResVo = (rtuStatusVo.getRtuResVo() == null ? new RtuResVo()
										: rtuStatusVo.getRtuResVo());
								rtuStatusVo.setRtuResVo(rtuResVo);

								rtuResVo.setSuccess(responseServer != null && responseServer == 200 ? true : false);
								rtuResVo.setFW_UPDATE(0);
								rtuResVo.setFW_URL("");
								rtuResVo.setRESET(0);

								rtuStatusVo.setRtuResVo(rtuResVo);

								try {
									if (rtuStatusVo.getRtuResVo() != null)
										resultMap.putAll(rtuStatusVo.getRtuResVo().getMap("success", "FW_UPDATE",
												"FW_URL", "RESET"));
								} catch (Exception e) {
									if (rtuStatusVo.getRtuResVo() != null)
										resultMap.putAll(rtuStatusVo.getRtuResVo().getMap("success", "FW_UPDATE",
												"FW_URL", "RESET"));
									statusMap = setStatus(statusMap, "RESPONSE_FAIL", e);
								}

							}

						}

					}

				} catch (Exception e) {
					rtuStatusVo = RtuPool.generateRtuStatusVo(rtuStatusVo);
					statusMap = setStatus(statusMap, "SENDING_ADDRESS_IS_NOT_DEFINITE", e);
				}
			} 
			else if (sendAddress.equals("03")) {
				// 메시지 변환부
				try {
					rtuStatusVo = convertRtuDataToCheonanData(rtuStatusVo);
				} catch (Exception e) {

					rtuStatusVo = RtuPool.generateRtuStatusVo(rtuStatusVo);
					statusMap = setStatus(statusMap, "MSG_CONVERT_FAIL", e);

					remsRtuNoticeService.noticeRemsRtuStatus("IMEI_MATCH_FAIL", rtuStatusVo);
				}

				// 메시지 전송부
				try {
					if (rtuStatusVo.getRemsVo() != null)
						rtuStatusVo = sendCheonanData(rtuStatusVo);
				} catch (Exception e) {
					statusMap = setStatus(statusMap, "REMS_SND_RCV_FAIL", e);
				}

				// 메시지 응답부
				try {
					rtuStatusVo = makeRtuResponseData(rtuStatusVo);
					if (rtuStatusVo.getRtuResVo() != null)
						resultMap.putAll(rtuStatusVo.getRtuResVo().getMap("success", "FW_UPDATE", "FW_URL", "RESET"));
				} catch (Exception e) {
					if (rtuStatusVo.getRtuResVo() != null)
						resultMap.putAll(rtuStatusVo.getRtuResVo().getMap("success", "FW_UPDATE", "FW_URL", "RESET"));
					statusMap = setStatus(statusMap, "RESPONSE_FAIL", e);
				}

			}
		}

		try {
			remsRtuDbService.saveMessageData(rtuStatusVo);
		} catch (Exception e) {
			statusMap = setStatus(statusMap, "SAVE_FAIL", e);
		}
		log.info("#######  Receive End.");

		return resultMap;
	}

	/**
	 * REMS RELAY 상태 기록
	 * 
	 * @param status
	 * @param code
	 * @param e
	 * @return
	 */
	protected Map<String, Object> setStatus(Map<String, Object> status, String code, Exception e) {
		e.printStackTrace();
		status.put("code", (status.containsKey("code") ? ((String) status.get("code")) + "," : "") + code);
		status.put("message",
				(status.containsKey("message") ? ((String) status.get("message")) + "\n" : "") + e.toString());
		return status;
	}

	/**
	 * RTU 로부터 메시지 수신
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	protected RtuStatusVo receiveDataFromRTU(HttpServletRequest request) throws IOException, IllegalAccessException {
		RtuStatusVo rtuStatusVo = null;
		Map<String, String> rcvHeaderMap = getHeaderMap(request);
		String rcvBodyString = getBodyString(request);

		// String 을 Json Map으로 변환
		Map<String, Object> rcvBodyMap = (Map<String, Object>) StringUtil.jsonToObject(rcvBodyString);
		String data = (String) rcvBodyMap.get("DATA"); // hex code bu data
		String imei = (String) rcvBodyMap.get("IMEI"); // IMEI data olish
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		RtuVo rtu = RtuPool.generateRtuVo(imei);

		rtuStatusVo = new RtuStatusVo();
		rtuStatusVo.setHeader(rcvHeaderMap);
		rtuStatusVo.setOriginalData(rcvBodyString);
		rtuStatusVo.setMap(rcvBodyMap, true);
		rtuStatusVo.setEnrgy(data.length() >= 4 ? data.substring(2, 4) : "");
		rtuStatusVo.setMachn(data.length() >= 6 ? data.substring(4, 6) : "");
		rtuStatusVo.setMulti(data.length() >= 8 ? data.substring(6, 8) : "");
		rtuStatusVo.setErrCode(data.length() >= 10 ? data.substring(8, 10) : "");
		rtuStatusVo.setIpAddr(request.getRemoteAddr());
		rtuStatusVo.setSndcd(rtu.getSndcd());
		rtuStatusVo.setLT(timestamp.toString());

		// Dummy 의 Thread 에 RTU 가 살아났다고 알림.
		RemsThreadPool.setRtuAlive(rtuStatusVo);

		return rtuStatusVo;
	}

	/**
	 * 수신된 메시지에서 HEADER 정보 추ㅜㄹ
	 * 
	 * @param request
	 * @return
	 */
	public Map<String, String> getHeaderMap(HttpServletRequest request) {
		Map<String, String> headerMap = new HashMap<String, String>();

		log.info("###### Header Info ##########");
		Enumeration<String> headerEnumer = request.getHeaderNames();
		for (; headerEnumer.hasMoreElements();) {
			String key = headerEnumer.nextElement();
			String value = request.getHeader(key);
			if (key == null || "".equals(key.trim()))
				continue;
			log.info(key + " : " + value);
			headerMap.put(key, value);
		}
		return headerMap;
	}

	/**
	 * 수신된 메시지에서 BODY 내용추출
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 */
	public String getBodyString(HttpServletRequest request) throws IOException {
		StringBuffer body = new StringBuffer();
		int len = -1;
		byte[] b = new byte[1024];

		InputStream is = request.getInputStream();
		while ((len = is.read(b)) >= 0) {
			body.append(new String(b, 0, len));
		}
		is.close();

		log.info("###### request Body ######");
		log.info(body.toString());

		return body.toString();
	}

	/**
	 * RTU 로부터 수신된 데이터로부터 항목별 추출.
	 * 
	 * @param rtuStatusVo
	 * @return
	 * @throws Exception
	 */
	public RtuStatusVo convertRtuDataToRemsData(RtuStatusVo rtuStatusVo) throws Exception {
		if (rtuStatusVo.getImei() == null)
			throw new Exception("IMEI['" + rtuStatusVo.getImei() + "'] is not exists.");
		if (rtuStatusVo.getEnrgy() == null)
			throw new Exception("Enrgy['" + rtuStatusVo.getEnrgy() + "'] is not exists.");
		if (rtuStatusVo.getMachn() == null)
			throw new Exception("Machn['" + rtuStatusVo.getMachn() + "'] is not exists.");
		if (rtuStatusVo.getMulti() == null)
			throw new Exception("Multi['" + rtuStatusVo.getMulti() + "'] is not exists.");
		rtuStatusVo = RtuPool.generateRtuStatusVo(rtuStatusVo);

		remsRtuDbService.updateRtuEnrgyTypeLastComDt(rtuStatusVo);

		RemsAuthVo remsAuthVo = RemsAuthPool.getRemsAuthVo(rtuStatusVo.getImei(), rtuStatusVo.getEnrgy(),
				rtuStatusVo.getMachn(), rtuStatusVo.getMulti());
		if (remsAuthVo == null)
			throw new Exception("RtuStatusVo=" + rtuStatusVo.getMap("imei", "enrgy", "machn").toString()
					+ " could not get CID and AUTH_KEY.");
		if (remsAuthVo.getCid() == null || "".equals(remsAuthVo.getCid().trim()))
			throw new Exception("RtuStatusVo=" + rtuStatusVo.getMap("imei", "enrgy", "machn").toString()
					+ " could not get CID['" + remsAuthVo.getCid() + "'].");
		if (remsAuthVo.getAuthKey() == null || "".equals(remsAuthVo.getAuthKey().trim()))
			throw new Exception("RtuStatusVo=" + rtuStatusVo.getMap("imei", "enrgy", "machn").toString()
					+ " could not get Authorization['" + remsAuthVo.getAuthKey() + "'].");

		// RTU 정보 전송용 REMS 인증정보 기록
		rtuStatusVo.setRemsAuthVo(remsAuthVo);

		// REMS 전송용 TOKEN 정보 추출
		RemsTokenVo remsTokenVo = RemsTokenPool.getRemsTokenVo(remsAuthVo.getCid(), remsAuthVo.getAuthKey());
		if (remsTokenVo == null || remsTokenVo.getAccessToken() == null)
			throw new Exception("RtuStatusVo=" + rtuStatusVo.getMap("imei", "enrgy", "machn", "multi").toString()
					+ " Token is not exists.");

		Map<String, String> sndRemsHeader = new HashMap<String, String>();
		byte[] token = (remsTokenVo.getAccessToken() != null ? remsTokenVo.getAccessToken().getBytes() : null);
		sndRemsHeader.put("tk1", (token != null && token.length > 32 ? new String(token, 0, 32) : new String(token)));
		sndRemsHeader.put("tk2", (token != null && token.length > 32 ? new String(token, 32, token.length - 32) : ""));
		sndRemsHeader.put("Content-Type", "application/json");

		RemsVo remsVo = new RemsVo();
		remsVo.setCid(remsAuthVo.getCid());
		remsVo.setMulti(Integer.parseInt(rtuStatusVo.getMulti(), 10));
		remsVo.setData(rtuStatusVo.getData());
		remsVo.setHeader(sndRemsHeader);

		rtuStatusVo.setRemsVo(remsVo);

		return rtuStatusVo;
	}

	/**
	 * RTU 의 상태 메모리POOL 에 기록
	 * 
	 * @param rtuVo
	 */
	public void updateRTUInfo(RtuVo rtuVo) {
		RtuPool.setRtuVo(rtuVo);
	}

	/**
	 * 수신된 발전량 정보를 REMS 에 전송
	 * 
	 * @param rtuStatusVo
	 * @return
	 * @throws Exception
	 */
	public RtuStatusVo sendRemsData(RtuStatusVo rtuStatusVo) throws Exception {
		HttpCall http = new HttpCall();
		http.setDebug(remsCallDebug);
		http.setTimeout(10 * 1000);

		RemsVo remsVo = rtuStatusVo.getRemsVo();
		if (remsVo.getCid() != null && !"".equals(remsVo.getCid())) {
			Map<String, Object> tokenResponse = http.getUrlCall(remsCallDataUrl,
					(Object) StringUtil.obj2String(remsVo.getMap("cid", "multi", "data")), "POST", remsVo.getHeader());
			int remsSndResult = (tokenResponse != null && tokenResponse.containsKey("responseCode")
					? NumberUtil.convertToInt(tokenResponse.get("responseCode"), -1)
					: -1);
			remsLog.info("IMEI:" + rtuStatusVo.getImei() + " ENRGY:" + rtuStatusVo.getEnrgy() + " MACHN:"
					+ rtuStatusVo.getMachn() + " MULTI:" + rtuStatusVo.getMulti() + " - \"" + remsCallDataUrl + "\" - "
					+ remsSndResult);
			remsVo.setResponseCode(remsSndResult);
			rtuStatusVo.setResponseCode(remsSndResult);
		}
		return rtuStatusVo;
	}

	// Saidjalol for cheonan project

	public RtuStatusVo convertRtuDataToCheonanData(RtuStatusVo rtuStatusVo) throws Exception {
		if (rtuStatusVo.getImei() == null)
			throw new Exception("IMEI['" + rtuStatusVo.getImei() + "'] is not exists.");
		if (rtuStatusVo.getEnrgy() == null)
			throw new Exception("Enrgy['" + rtuStatusVo.getEnrgy() + "'] is not exists.");
		if (rtuStatusVo.getMachn() == null)
			throw new Exception("Machn['" + rtuStatusVo.getMachn() + "'] is not exists.");
		if (rtuStatusVo.getMulti() == null)
			throw new Exception("Multi['" + rtuStatusVo.getMulti() + "'] is not exists.");
		rtuStatusVo = RtuPool.generateRtuStatusVo(rtuStatusVo);

		remsRtuDbService.updateRtuEnrgyTypeLastComDt(rtuStatusVo);

		Map<String, String> sndRemsHeader = new HashMap<String, String>();
		sndRemsHeader.put("ENERGY-CHEONANEP", "CHEONANEP-LG");
		sndRemsHeader.put("Content-Type", "application/json");

		RemsVo remsVo = new RemsVo();
		remsVo.setCid(null);
		remsVo.setMulti(Integer.parseInt(rtuStatusVo.getMulti(), 10));
		remsVo.setData(rtuStatusVo.getData());
		remsVo.setHeader(sndRemsHeader);

		rtuStatusVo.setRemsVo(remsVo);

		return rtuStatusVo;
	}

	public RtuStatusVo sendCheonanData(RtuStatusVo rtuStatusVo) throws Exception {
		HttpCall http = new HttpCall();
		http.setDebug(remsCallDebug);
		http.setTimeout(10 * 1000);

		RemsVo remsVo = rtuStatusVo.getRemsVo();
		Map<String, Object> tokenResponse = http.getUrlCall(cheonanCallDataUrl,
				(Object) StringUtil.obj2String(remsVo.getMap("cid", "multi", "data")), "POST", remsVo.getHeader());
		int remsSndResult = (tokenResponse != null && tokenResponse.containsKey("responseCode")
				? NumberUtil.convertToInt(tokenResponse.get("responseCode"), -1)
				: -1);
		remsLog.info("IMEI:" + rtuStatusVo.getImei() + " ENRGY:" + rtuStatusVo.getEnrgy() + " MACHN:"
				+ rtuStatusVo.getMachn() + " MULTI:" + rtuStatusVo.getMulti() + " - \"" + remsCallDataUrl + "\" - "
				+ remsSndResult);
		remsVo.setResponseCode(remsSndResult);
		rtuStatusVo.setResponseCode(remsSndResult);
		return rtuStatusVo;
	}

	/**
	 * RTU 에서 수신된 발전량정보를 REMS 에 전송을 위하여 전송 DATA 생성
	 * 
	 * @param rtuStatusVo
	 * @return
	 */
	public RtuStatusVo makeRtuResponseData(RtuStatusVo rtuStatusVo) {
		Map<String, Object> maintenanceMap = (Map<String, Object>) remsRtuDbService.getRtuFwResetStatus(rtuStatusVo);
		RtuVo rtuVo = RtuPool.getRtuVo(rtuStatusVo.getImei());
		if (maintenanceMap != null && maintenanceMap.get("FW_IDX") != null)
			rtuVo.setFwUpStatus("U");
		if (maintenanceMap != null && maintenanceMap.get("RESET_REG_DT") != null)
			rtuVo.setResetStatus("U");

		RtuResVo rtuResVo = (rtuStatusVo.getRtuResVo() == null ? new RtuResVo() : rtuStatusVo.getRtuResVo());
		rtuStatusVo.setRtuResVo(rtuResVo);

		rtuResVo.setSuccess(
				rtuStatusVo.getRemsVo() != null && rtuStatusVo.getRemsVo().getResponseCode() == 200 ? true : false);
		rtuResVo.setFW_UPDATE("U".equals(rtuVo.getFwUpStatus()) ? 1 : 0);
		rtuResVo.setFW_URL(
				"U".equals(rtuVo.getFwUpStatus()) && maintenanceMap != null && maintenanceMap.get("FILE_KEY") != null
						? serviceDomain + "/rtu/fwDown.do?key=" + maintenanceMap.get("FILE_KEY")
						: "");
		rtuResVo.setRESET("U".equals(rtuVo.getResetStatus()) ? 1 : 0);

		if (rtuResVo.getFW_UPDATE() == 1 || rtuResVo.getRESET() == 1) {
			if (rtuResVo.getFW_UPDATE() == 1)
				rtuVo.setFwUpStatus("W");
			if (rtuResVo.getRESET() == 1)
				rtuVo.setResetStatus("W");
			remsRtuDbService.updateRtuFwDownResetDate(rtuVo);
		}

		rtuStatusVo.setRtuResVo(rtuResVo);

		return rtuStatusVo;
	}

}
