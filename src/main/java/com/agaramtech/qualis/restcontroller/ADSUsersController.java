package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.configuration.service.adsusers.ADSUsersService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/adsusers")
public class ADSUsersController {

	private final ADSUsersService adsUsersService;
	private RequestContext requestContext;

	public ADSUsersController(RequestContext requestContext, ADSUsersService adsUsersService) {
		super();
		this.requestContext = requestContext;
		this.adsUsersService = adsUsersService;
	}

	@PostMapping(value = "/getADSUsers")
	public ResponseEntity<Object> getADSUsers(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper mapper = new ObjectMapper();
		UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return adsUsersService.getADSUsers(userInfo);
	}

	@RequestMapping(value = "/syncADSUsers", method = RequestMethod.POST)
	public ResponseEntity<Object> syncADSUsers(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return adsUsersService.syncADSUsers(inputMap, userInfo);

	}
}