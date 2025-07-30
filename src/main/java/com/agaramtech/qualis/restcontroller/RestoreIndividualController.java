package com.agaramtech.qualis.restcontroller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.agaramtech.qualis.archivalandpurging.restoreindividual.service.RestoreIndividualService;
import com.agaramtech.qualis.basemaster.service.unit.UnitService;

/**
 * 
 *
 */
@RestController
@RequestMapping("/restoreindividual")
public class RestoreIndividualController {
	
	private RequestContext requestContext;
	private final RestoreIndividualService restoreService;
	
	public RestoreIndividualController(RequestContext requestContext, RestoreIndividualService restoreService) {
		super();
		this.requestContext = requestContext;
		this.restoreService = restoreService;
	}
	
	@PostMapping(value = "/getRestoreIndividual")
	public ResponseEntity<Object> getRestoreIndividual(@RequestBody Map<String, Object> inputMap) throws Exception{
		
			
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
			});
			requestContext.setUserInfo(userInfo);
			return restoreService.getRestoreIndividual(inputMap,userInfo);
			
	}
	
	@PostMapping(value = "/createRestoreIndividual")
	public ResponseEntity<Object> createRestoreIndividual(@RequestBody Map<String, Object> inputMap) throws Exception{
		
			
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
			});
			requestContext.setUserInfo(userInfo);
			return restoreService.createRestoreIndividual(inputMap,userInfo);
			
		
	}
	
	@PostMapping(value = "/getRestoreSampleData")
	public ResponseEntity<Object> getRestoreSampleData(@RequestBody Map<String, Object> inputMap) throws Exception{
		
			
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
			});
			requestContext.setUserInfo(userInfo);
			return restoreService.getRestoreSampleData(inputMap,userInfo);
			
	}
	
	@PostMapping(value = "/getPurgeDate")
	public ResponseEntity<Object> getPurgeDate(@RequestBody Map<String, Object> inputMap) throws Exception{
		
			final ObjectMapper mapper=new ObjectMapper();
			final UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		
			requestContext.setUserInfo(userInfo);
			return restoreService.getPurgeDate(inputMap,userInfo);
		
	}
	
	@PostMapping(value = "/getReleasedSamples")
	public ResponseEntity<Object> getReleasedSamples(@RequestBody Map<String, Object> inputMap) throws Exception{
		
			final ObjectMapper mapper=new ObjectMapper();
			final UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		
			requestContext.setUserInfo(userInfo);
			return restoreService.getReleasedSamples(inputMap,userInfo);
		
	}
	
	@PostMapping(value = "/getRegistrationSampleDetails")
	public ResponseEntity<Object> getRegistrationSampleDetails(@RequestBody Map<String, Object> inputMap) throws Exception{
		
			final ObjectMapper mapper=new ObjectMapper();
			final UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		
			requestContext.setUserInfo(userInfo);
			return restoreService.getRegistrationSampleDetails(inputMap,userInfo);
		
	}
}
