package com.moren.rems.rtu.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.moren.rems.rtu.service.RemsRtuService;
import com.moren.rems.rtu.service.RemsRtuSndRateService;
import com.moren.rems.rtu.service.RemsSndRateThread;
import com.moren.rems.rtu.vo.RemsAuthPool;
import com.moren.rems.rtu.vo.RemsThreadPool;
import com.moren.rems.rtu.vo.RemsThreadStatusPool;
import com.moren.rems.rtu.vo.RemsTokenPool;
import com.moren.rems.rtu.vo.RtuPool;
import com.roviet.common.tools.vo.FileVo;
import com.roviet.common.web.service.FileService;

/**
 * REST API Controller
 * @author shlee
 */
@Controller
public class RemsRtuController {
	Logger log = Logger.getLogger(this.getClass());
	
	@Autowired
	RemsRtuService remsRtuService;
	
	@Autowired
	RemsRtuSndRateService remsRtuSndRateService;

	@Autowired
	FileService fileService;

	/**
	 * RTU 로부터 발전정보 수신
	 * @param param
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value={"/rtu/data.do"})
	public String receiveData(@RequestParam Map<String,Object> param, ModelMap model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		model.addAllAttributes(remsRtuService.receiveData(param, model, request, response));
		return "jsonView";
	}
	
	/**
	 * RTU Pool 초기화 API
	 * @param param
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value={"/rtu/rtuInit.do"})
	public String rtuPoolInit(@RequestParam Map<String,Object> param, ModelMap model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		if(param.get("imei")!=null && !"".equals(param.get("imei"))) RtuPool.getInstance().initialize((String) param.get("imei"));
		else RtuPool.getInstance().reset();
		return "jsonView";
	}
	
	/**
	 * 펌웨어 파일 다운로드
	 * @param commandMap
	 * @param fileVo
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value= {"/rtu/fwDown.do"})
	public void fwDown(Map<String, Object> commandMap, FileVo fileVo, HttpServletRequest request, HttpServletResponse response) throws Exception {
		fileService.fileDown(fileVo, request, response);
	}
	
	/**
	 * 인증정보 Pool 초기화 API
	 * @param param
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value={"/rtu/authInit.do"})
	public String remsAuthPoolInit(@RequestParam Map<String,Object> param, ModelMap model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		RemsAuthPool.getInstance().reset();
		return "jsonView";
	}

	/**
	 * 토큰정보 Pool 초기화 API
	 * @param param
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value={"/rtu/tokenInit.do"})
	public String remsTokenPoolInit(@RequestParam Map<String,Object> param, ModelMap model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		RemsTokenPool.getInstance().reset();
		return "jsonView";
	}

	/**
	 * 더미전송상태 설정변경 API
	 * @param param
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value={"/rems/setDummyStatus.do"})
	public String setDummyStatus(@RequestParam Map<String,Object> param, ModelMap model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String,Object> status = new HashMap<String,Object>();
		if(param.containsKey("key") && !"".equals(param.get("key")) && param.containsKey("status") && !"".equals(param.get("status"))) {
			RemsThreadStatusPool.setStatus((String) param.get("key"), (String) param.get("status"));
			status.put("code","SUCCESS");
		} else if(param.containsKey("status") && !"".equals(param.get("status"))) {
			RemsThreadStatusPool.setStatus((String) param.get("status"));
			status.put("code","SUCCESS");
		} else {
			status.put("code","FAIL");
			status.put("message", "status parameter is not exists.");
		}
		model.addAttribute("status", status);
		return "jsonView";
	}

	/**
	 * 더미전송 데이터 설정 API
	 * @param param
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value={"/rems/setDummyData.do"})
	public String setDummyData(@RequestParam Map<String,Object> param, ModelMap model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String,Object> resultMap = null;
		
		resultMap = remsRtuSndRateService.setDummyData(param,  model,  request, response);
		
		model.addAllAttributes(resultMap);
		return "jsonView";
	}

	/**
	 * 더미전송상태 확인 API
	 * @param param
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value={"/rems/getDummyStatus.do"})
	public String getDummyStatus(@RequestParam Map<String,Object> param, ModelMap model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String status = RemsThreadStatusPool.getStatus((String) param.get("key"));
		model.addAttribute("key", (String) param.get("key"));
		model.addAttribute("status", status);
		return "jsonView";
	}

	/**
	 * 더미전송 가능 시간 설정 API
	 * @param param
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value={"/rems/setDummyTime.do"})
	public String setDummyTime(@RequestParam Map<String,Object> param, ModelMap model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String,Object> resultMap = null;
		resultMap = remsRtuSndRateService.setDummyTime(param,  model,  request, response);
		
		model.addAllAttributes(resultMap);
		return "jsonView";
	}

	/**
	 * 테스트 API
	 * @param param
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value={"/view/test.do"})
	public String viewTest(@RequestParam Map<String,Object> param, ModelMap model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		List<String> list = new ArrayList<String>();
		Iterator<String> iter = RemsThreadPool.iterator();
		for(;iter.hasNext();) {
			String key = iter.next();
			RemsSndRateThread thread = RemsThreadPool.getObject(key);
			list.add(key+" : "+thread.getData());
		}
		model.addAttribute("list",list);
		return "jsonView";
	}
}
