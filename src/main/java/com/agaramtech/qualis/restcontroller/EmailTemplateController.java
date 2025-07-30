package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.emailmanagement.model.EmailTemplate;
import com.agaramtech.qualis.emailmanagement.service.emailtemplate.EmailTemplateService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory; 
 
@RestController
@RequestMapping("/emailtemplate")
public class EmailTemplateController {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmailTemplateController.class); 
	
	private final EmailTemplateService emailTemplateService;
	private RequestContext requestContext;

	public EmailTemplateController(EmailTemplateService emailTemplateService, RequestContext requestContext) {
		super();
		this.emailTemplateService = emailTemplateService;
		this.requestContext = requestContext;
	}

	@PostMapping(value = "/getEmailTemplate")
	public ResponseEntity<Object> getEmailTemplate(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		LOGGER.info("getEmailTemplate() called");
		return emailTemplateService.getEmailTemplate(userInfo.getNmastersitecode());
	}

	@PostMapping(value = "/getEmailTag")
	public ResponseEntity<Object> getEmailTag(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return emailTemplateService.getEmailTag(userInfo.getNmastersitecode());
	}

	@PostMapping(value = "/getEmailTagFilter")
	public ResponseEntity<Object> getEmailTagFilter(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		int nemailtagcode = (int) inputMap.get("nemailtagcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return emailTemplateService.getEmailTagFilter(nemailtagcode, userInfo);
	}

	@PostMapping(value = "/createEmailTemplate")
	public ResponseEntity<Object> createEmailTemplate(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		EmailTemplate objTechnique = objMapper.convertValue(inputMap.get("emailtemplate"),
				new TypeReference<EmailTemplate>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return emailTemplateService.createEmailTemplate(objTechnique, userInfo);
	}

	@PostMapping(value = "/updateEmailTemplate")
	public ResponseEntity<Object> updateEmailTemplate(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		EmailTemplate emailtemplate = objMapper.convertValue(inputMap.get("emailtemplate"),
				new TypeReference<EmailTemplate>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return emailTemplateService.updateEmailTemplate(emailtemplate, userInfo);
	}

	@PostMapping(value = "/deleteEmailTemplate")
	public ResponseEntity<Object> deleteEmailTemplate(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		EmailTemplate objTechnique = objMapper.convertValue(inputMap.get("emailtemplate"),
				new TypeReference<EmailTemplate>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return emailTemplateService.deleteEmailTemplate(objTechnique, userInfo);
	}

	@PostMapping(value = "/getActiveEmailTemplateById")
	public ResponseEntity<Object> getActiveEmailTemplateById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		int nemailtemplatecode = (int) inputMap.get("nemailtemplatecode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return emailTemplateService.getActiveEmailTemplateById(nemailtemplatecode, userInfo);
	}
}
