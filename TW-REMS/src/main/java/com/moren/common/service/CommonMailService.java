package com.moren.common.service;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.mail.Address;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.roviet.common.web.mail.AbsMailService;
import com.roviet.common.web.mail.MailItem;

/**
 * 메일 발송용 Service
 * @author shlee
 */
@Service
public class CommonMailService extends AbsMailService {

	
	@Value("${Global.mail.trId}")
	private String trId;

	@Value("${Global.mail.from.name}")
	private String mailFromName;
	
	@Value("${Global.mail.from.email}")
	private String mailFromEmail;
	
	/**
	 * 메일발송을 위한 메일 내용에 템플릿을 적용한다.
	 * @param contents
	 * @return
	 */
	@Override
	@SuppressWarnings("unused")
	public String makeTemplate(String contents) {
		String html = "";
		html += "<style>";
		html += "table { border: solid 1px #000000; font-size: 12px; font-family: 굴림체; }";
		html += "</style>";
		html += "<div style='font-family:굴림체; font-size:12px;'>";
		html += contents;
		html += "</div>";
		return "<div style='font-family:굴림체; font-size:12px;'>"+contents+"</div>";
	}

	/**
	 * 전달되는 인자로 메일 발송을 위한 MainItem 객체를 생성한다.
	 * MailItem 안에는 수신자, 발신자, 제목, 내용, 첨부파일 등의 메일발송용 Data 를 가지고 있다.
	 * @param tos
	 * @param mailType
	 * @param info
	 * @return
	 * @throws Exception
	 */
	@Override
	public MailItem makeMailData(Object tos, String mailType, Map<String,Object> info) throws Exception {
		MailItem item = new MailItem();
		String subject = null;
		String contents = null;
		List<File> files = null;
		
		item.setTrId(this.trId);
		item.setMsgCode(mailType);
		
		item.setFrom(mailFromName, mailFromEmail);
		if(tos instanceof String) item.addTo((String) tos);
		else if(tos instanceof Address) item.addTo((Address) tos);
		else if (tos instanceof String[]) {
			for(int i=0 ; i<((String[]) tos).length ; i++) item.addTo(((String[]) tos)[i]);
		}
		if("TEST".equals(mailType)) {
			subject = "";
			contents = "";
		} else {
			throw new Exception("mailType["+mailType+"] is not defined.");
		}
		item.setSubject(subject);
		item.setContents(contents);
		for(int i=0 ; files!=null && i<files.size() ; i++) item.addFile(files.get(i));
		return item;
	}	
}
