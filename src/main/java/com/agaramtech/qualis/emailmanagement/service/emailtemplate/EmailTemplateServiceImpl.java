package com.agaramtech.qualis.emailmanagement.service.emailtemplate;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.emailmanagement.model.EmailTemplate;
import com.agaramtech.qualis.global.UserInfo;
import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true, rollbackFor = Exception.class) 
@Service 
@RequiredArgsConstructor 
public class EmailTemplateServiceImpl implements EmailTemplateService {

	private final EmailTemplateDAO emailtemplateDAO;

	@Override
	public ResponseEntity<Object> getEmailTemplate(int nmastersitecode) throws Exception {
		return emailtemplateDAO.getEmailTemplate(nmastersitecode);
	}

	@Override
	public ResponseEntity<Object> getEmailTag(int nmastersitecode) throws Exception {
		return emailtemplateDAO.getEmailTag(nmastersitecode);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createEmailTemplate(EmailTemplate emailTemplate, UserInfo userInfo) throws Exception {
		return emailtemplateDAO.createEmailTemplate(emailTemplate, userInfo);
	}

	@Override
	public ResponseEntity<Object> getEmailTagFilter(Integer nemailtagcode, UserInfo userInfo) throws Exception {
		return emailtemplateDAO.getEmailTagFilter(nemailtagcode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveEmailTemplateById(int nemailtemplatecode, UserInfo userInfo)
			throws Exception {
		return emailtemplateDAO.getActiveEmailTemplateById(nemailtemplatecode, userInfo);

	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateEmailTemplate(EmailTemplate emailTemplate, UserInfo userInfo) throws Exception {
		return emailtemplateDAO.updateEmailTemplate(emailTemplate, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteEmailTemplate(EmailTemplate emailTemplate, UserInfo userInfo) throws Exception {
		return emailtemplateDAO.deleteEmailTemplate(emailTemplate, userInfo);
	}
}
