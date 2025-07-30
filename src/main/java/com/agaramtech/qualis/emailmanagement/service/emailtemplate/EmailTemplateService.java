package com.agaramtech.qualis.emailmanagement.service.emailtemplate;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.emailmanagement.model.EmailTemplate;
import com.agaramtech.qualis.global.UserInfo;

public interface EmailTemplateService {

	ResponseEntity<Object> getEmailTemplate(int nmastersitecode) throws Exception;

	ResponseEntity<Object> getEmailTag(int nmastersitecode) throws Exception;

	ResponseEntity<Object> createEmailTemplate(EmailTemplate emailTemplate, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getEmailTagFilter(Integer nemailtagcode, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getActiveEmailTemplateById(int nemailtemplatecode, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> updateEmailTemplate(EmailTemplate emailTemplate, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> deleteEmailTemplate(EmailTemplate emailTemplate, UserInfo userInfo) throws Exception;

}
