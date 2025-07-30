package com.agaramtech.qualis.restcontroller;

import java.util.Map;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.credential.model.UserRoleConfig;
import com.agaramtech.qualis.credential.service.userroleconfiguration.UserRoleConfigurationService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;



@RestController
@RequestMapping("/userroleconfiguration")
public class UserRoleConfigurationController {

	

	
	private RequestContext requestContext;
	private final UserRoleConfigurationService userRoleConfigurationService;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param userRoleConfigurationService UserRoleConfigurationService
	 */
	public UserRoleConfigurationController(RequestContext requestContext, UserRoleConfigurationService userRoleConfigurationService) {
		super();
		this.requestContext = requestContext;
		this.userRoleConfigurationService = userRoleConfigurationService;
	}
	
	
//@RequestMapping(value = "/getUserRoleConfiguration", method = RequestMethod.POST)
	@PostMapping(value = "/getUserRoleConfiguration")
public ResponseEntity<Object> getUserRoleConfiguration(@RequestBody Map<String, Object> inputMap) throws Exception{
	final ObjectMapper objMapper = new ObjectMapper();	
	final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
	});
	//final int masterSiteCode = userInfo.getNmastersitecode();
	//requestContext.setUserInfo(userInfo);
	requestContext.setUserInfo(userInfo);
	return userRoleConfigurationService.getUserRoleConfiguration(userInfo);
}
	
//@RequestMapping(value = "/getActiveUserRoleConfigurationById", method = RequestMethod.POST)
	@PostMapping(value = "/getActiveUserRoleConfigurationById")
public ResponseEntity<Object> getActiveUserRoleConfigurationById(@RequestBody Map<String, Object> inputMap) throws Exception{
	final ObjectMapper objMapper = new ObjectMapper();
	final int nuserRoleCode = (Integer) inputMap.get("nuserrolecode");
	final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
	});
	requestContext.setUserInfo(userInfo);
	return userRoleConfigurationService.getActiveUserRoleConfigurationById(nuserRoleCode, userInfo);
}
	
//@RequestMapping(value = "/updateUserRoleConfiguration", method = RequestMethod.POST)
	@PostMapping(value = "/updateUserRoleConfiguration")
public ResponseEntity<Object> updateUserRoleConfiguration(@RequestBody Map<String, Object> inputMap) throws Exception{
	final ObjectMapper objMapper = new ObjectMapper();
	objMapper.registerModule(new JavaTimeModule());
	
	final UserRoleConfig userRoleConfig = objMapper.convertValue(inputMap.get("userroleconfiguration"), new TypeReference<UserRoleConfig>() {});
	final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
	});
	requestContext.setUserInfo(userInfo);
	return userRoleConfigurationService.updateUserRoleConfiguration(userRoleConfig, userInfo);
}

}
