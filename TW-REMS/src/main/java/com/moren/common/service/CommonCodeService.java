package com.moren.common.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.moren.common.vo.CodeVo;

/**
 * 공통코드 처리 프로그램 Service
 * @author shlee
 */
@Service
public class CommonCodeService {
	
	Logger log = Logger.getLogger(this.getClass());
	
	@Autowired
	CommonCodeDao commonCodeDao;
	
	/**
	 * 구분자와 코드1,2,3 을 조건으로 공통코드목록 가져오기
	 * @param division
	 * @param code1
	 * @param code2
	 * @param code3
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<CodeVo> getCodeList(String division, String code1, String code2, String code3) {
		CodeVo codeVo = new CodeVo();
		codeVo.setDivision(division);
		if(code1!=null) codeVo.setCode1(code1);
		if(code2!=null) codeVo.setCode2(code2);
		if(code3!=null) codeVo.setCode3(code3);		

		List<CodeVo> codeList = (List<CodeVo>) commonCodeDao.selectList("CommonDao.getCodeList", codeVo);
		return codeList;
	}

	/**
	 * 구분자와 코드1,2 를 조건으로 공통코드목록 가져오기
	 * @param division
	 * @param code1
	 * @param code2
	 * @return
	 */
	public List<CodeVo> getCodeList(String division, String code1, String code2) {
		return getCodeList(division, code1, code2, null);
	}

	/**
	 * 구분자와 코드1을 조건으로 공통코드목록 가져오기
	 * @param division
	 * @param code1
	 * @return
	 */
	public List<CodeVo> getCodeList(String division, String code1) {
		return getCodeList(division, code1, null, null);
	}

	/** 
	 * 구분자로 공통코드목록 가져오기
	 * @param division
	 * @return
	 */
	public List<CodeVo> getCodeList(String division) {
		return getCodeList(division, null, null, null);
	}

	/**
	 * 구분자와 코드1,2,3 으로 공통코드 1개 가져오기
	 * @param division
	 * @param code1
	 * @param code2
	 * @param code3
	 * @return
	 */
	public CodeVo getCode(String division, String code1, String code2, String code3) {
		CodeVo codeVo = new CodeVo();
		codeVo.setDivision(division);
		if(code1!=null) codeVo.setCode1(code1);
		if(code2!=null) codeVo.setCode2(code2);
		if(code3!=null) codeVo.setCode3(code3);		
		
		CodeVo code = (CodeVo) commonCodeDao.selectOne("CommonDao.getCode", codeVo);
		return code;
	}

	/**
	 * 구분자와 코드1,2로 공통코드 1개 가져오기
	 * @param division
	 * @param code1
	 * @param code2
	 * @return
	 */
	public CodeVo getCode(String division, String code1, String code2) {
		return getCode(division, code1, code2, null);
	}

	/**
	 * 구분자와 코드1로 공통코드 1개 가져오기
	 * @param division
	 * @param code1
	 * @return
	 */
	public CodeVo getCode(String division, String code1) {
		return getCode(division, code1, null, null);
	}

	/** 
	 * 구분자로 공통코드 1개 가져오기
	 * @param division
	 * @return
	 */
	public CodeVo getCode(String division) {
		return getCode(division, null, null, null);
	}
	
	/**
	 * 공통코드값을 Update 
	 * 구분자, 코드1, 코드2, 코드3 을 조건으로 나머지 필드를 update 한다.
	 * @param codeVo
	 * @return
	 */
	public int update(CodeVo codeVo) {
		return commonCodeDao.update("CommonDao.updateCode", codeVo);
	}

	/**
	 * 공통코드를 insert
	 * @param codeVo
	 * @return
	 */
	public int insert(CodeVo codeVo) {
		return commonCodeDao.insert("CommonDao.insertCode", codeVo);
	}

	/**
	 * 공통코드를 delete
	 * 구분자, 코드1, 코드2, 코드3을 조건으로 코드를 삭제한다.
	 * @param codeVo
	 * @return
	 */
	public int delete(CodeVo codeVo) {
		return commonCodeDao.delete("CommonDao.deleteCode", codeVo);
	}
}
