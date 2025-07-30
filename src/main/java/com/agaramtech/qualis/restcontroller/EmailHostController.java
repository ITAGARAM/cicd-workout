package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.emailmanagement.model.EmailHost;
import com.agaramtech.qualis.emailmanagement.service.emailhost.EmailHostService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory; 
/**
 * This controller is used to dispatch the input request to its relevant method to access
 * the EmailHost Service methods.
 * @author ATE184
 * @version 9.0.0.1
 * @since   06- Jun- 2020
 */
@RestController
@RequestMapping("/emailhost")
public class EmailHostController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EmailHostController.class); 
	private final EmailHostService emailhostService;
	private RequestContext requestContext;
	
	public EmailHostController(EmailHostService emailhostService, RequestContext requestContext) {
		super();
		this.emailhostService = emailhostService;
		this.requestContext = requestContext;
	}

	@PostMapping(value = "/getEmailHost")
	public ResponseEntity<Object> getEmailHost(@RequestBody Map<String, Object> inputMap) throws Exception {
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			LOGGER.info(" getEmailHost() called");
			return emailhostService.getEmailHost(userInfo.getNmastersitecode());
	}
	
	@PostMapping(value = "/createEmailHost")
	public ResponseEntity<Object> createEmailHost(@RequestBody Map<String, Object> inputMap) throws Exception {
			final ObjectMapper objmapper = new ObjectMapper();
			final EmailHost emailhost = objmapper.convertValue(inputMap.get("emailhost"), EmailHost.class);
			final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			requestContext.setUserInfo(userInfo);
			return emailhostService.createEmailHost(emailhost, userInfo);
	}
	
	
	@PostMapping(value = "/getActiveEmailHostById")
	public ResponseEntity<Object> getActiveEmailHostById(@RequestBody Map<String, Object> inputMap)  throws Exception {
			final ObjectMapper objmapper = new ObjectMapper();
			final int nemailhostcode = (Integer) inputMap.get("nemailhostcode");
			final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			requestContext.setUserInfo(userInfo);
			return emailhostService.getActiveEmailHostById(nemailhostcode, userInfo);
	}
	
	@PostMapping(value = "/updateEmailHost")
	public ResponseEntity<Object> updateEmailHost(@RequestBody Map<String, Object> inputMap)  throws Exception {
			final ObjectMapper objmapper = new ObjectMapper();
			final EmailHost emailhost = objmapper.convertValue(inputMap.get("emailhost"), EmailHost.class);
			final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			requestContext.setUserInfo(userInfo);
			return emailhostService.updateEmailHost(emailhost, userInfo);
	}
	
	@PostMapping(value = "/deleteEmailHost")
	public ResponseEntity<Object> deleteEmailHost(@RequestBody Map<String, Object> inputMap)  throws Exception {
			final ObjectMapper objmapper = new ObjectMapper();
			final EmailHost emailhost = objmapper.convertValue(inputMap.get("emailhost"), EmailHost.class);
			final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			requestContext.setUserInfo(userInfo);
			return emailhostService.deleteEmailHost(emailhost, userInfo);
	}
}
