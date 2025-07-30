package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.emailmanagement.model.EmailConfig;
import com.agaramtech.qualis.emailmanagement.model.EmailUserConfig;
import com.agaramtech.qualis.emailmanagement.service.emailconfig.EmailConfigService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/emailconfig")
public class EmailConfigController {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmailConfigController.class);

	private final EmailConfigService emailconfigService;
	private RequestContext requestContext;

	public EmailConfigController(EmailConfigService emailconfigService, RequestContext requestContext) {
		super();
		this.emailconfigService = emailconfigService;
		this.requestContext = requestContext;
	}

	@PostMapping(value = "/getEmailConfig")
	public ResponseEntity<Object> getEmailConfig(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		Integer nemailconfigcode = null;
		if (inputMap.get("nemailconfigcode") != null) {
			nemailconfigcode = (Integer) inputMap.get("nemailconfigcode");
		}
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("getEmailConfig() called");
		requestContext.setUserInfo(userInfo);
		return emailconfigService.getEmailConfig(nemailconfigcode, userInfo);
	}

	@PostMapping(value = "/getEmailConfigDetails")
	public ResponseEntity<Object> getEmailConfigDetails(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return emailconfigService.getEmailConfigDetails(0, userInfo);
	}

	@PostMapping(value = "/getEmailConfigControl")
	public ResponseEntity<Object> getEmailConfigControl(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		return emailconfigService.getEmailConfigControl((int) inputMap.get("nformcode"), userInfo);
	}

	@PostMapping(value = "/createEmailConfig")
	public ResponseEntity<Object> createEmailConfig(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		String nusercode = null;
		if (inputMap.get("emailuserconfig") != null) {
			nusercode = (String) inputMap.get("emailuserconfig");
		}
		final EmailConfig emailconfig = objmapper.convertValue(inputMap.get("emailconfig"), EmailConfig.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return emailconfigService.createEmailConfig(emailconfig, nusercode, userInfo);
	}

	@PostMapping(value = "/getActiveEmailConfigById")
	public ResponseEntity<Object> getActiveEmailConfigById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		int nemailconfigcode = (int) inputMap.get("nemailconfigcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return emailconfigService.getActiveEmailConfigById(nemailconfigcode, userInfo);
	}

	@PostMapping(value = "/updateEmailConfig")
	public ResponseEntity<Object> updateEmailConfig(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final EmailConfig emailconfig = objmapper.convertValue(inputMap.get("emailconfig"), EmailConfig.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return emailconfigService.updateEmailConfig(emailconfig, userInfo);
	}

	@PostMapping(value = "/deleteEmailConfig")
	public ResponseEntity<Object> deleteEmailConfig(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final EmailConfig emailconfig = objmapper.convertValue(inputMap.get("emailuserconfig"), EmailConfig.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return emailconfigService.deleteEmailConfig(emailconfig, userInfo);
	}

	@PostMapping(value = "/getUserRoleEmail")
	public ResponseEntity<Object> getUserRoleEmail(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		int nuserrolecode = (int) inputMap.get("nuserrolecode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return emailconfigService.getUserRoleEmail(nuserrolecode, userInfo);
	}

	@PostMapping(value = "/getUserEmailConfig")
	public ResponseEntity<Object> getUserEmailConfig(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		int nemailconfigcode = (int) inputMap.get("nemailconfigcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return emailconfigService.getUserEmailConfig(nemailconfigcode, userInfo);
	}

	@PostMapping(value = "/createUsers")
	public ResponseEntity<Object> createUsers(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		String nusercode = null;
		Integer nemailconfigcode = null;
		if (inputMap.get("nusercode") != null) {
			nusercode = (String) inputMap.get("nusercode");
		}
		if (inputMap.get("nemailconfigcode") != null) {
			nemailconfigcode = (Integer) inputMap.get("nemailconfigcode");
		}
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return emailconfigService.createUsers(nemailconfigcode, nusercode, userInfo);
	}

	@PostMapping(value = "/deleteUsers")
	public ResponseEntity<Object> deleteUsers(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final EmailUserConfig emailuserconfig = objmapper.convertValue(inputMap.get("emailuserconfig"),
				EmailUserConfig.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return emailconfigService.deleteUsers(emailuserconfig, userInfo);
	}
}
