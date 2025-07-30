package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.configuration.model.SampleType;
import com.agaramtech.qualis.configuration.service.sampletype.SampleTypeService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@RestController
@RequestMapping("/sampletype")
public class SampleTypeController {
		
	private RequestContext requestContext;
	private final SampleTypeService sampleTypeService;
	
	public SampleTypeController(RequestContext requestContext, SampleTypeService sampleTypeService) {
		super();
		this.requestContext = requestContext;
		this.sampleTypeService = sampleTypeService;
	}
	
	
	@PostMapping(value = "/getSampleType")
	public ResponseEntity<Object> getSampleType(@RequestBody Map<String, Object> inputMap)throws Exception {

	
			final ObjectMapper objmapper = new ObjectMapper();				
			final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
			});
			requestContext.setUserInfo(userInfo);
			return sampleTypeService.getSampleType(userInfo);
		
	}
	
	
	
	
	@PostMapping(value = "/deleteSampleType")
	public ResponseEntity<Object> deleteSampleType(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		SampleType objSampleType = objMapper.convertValue(inputMap.get("sampletype"), new TypeReference<SampleType>() {
		});
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleTypeService.deleteSampleType(objSampleType, userInfo);
	}
	
	@PostMapping(value = "/getActiveSampleTypeById")
	public ResponseEntity<Object> getActiveSiteById(@RequestBody Map<String, Object> inputMap) throws Exception  {
			final ObjectMapper objmapper = new ObjectMapper();
			final int nsampletypecode = (Integer) inputMap.get("nsampletypecode");
			UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
			});
			requestContext.setUserInfo(userInfo);
			return sampleTypeService.getActiveSampleTypeById(nsampletypecode, userInfo);
	}
	
//	@PostMapping(value = "/createSampleType")
//	public ResponseEntity<Object> createSampleType(@RequestBody Map<String, Object> inputMap)throws Exception {
//
//		
//			final ObjectMapper objmapper = new ObjectMapper();
//			objmapper.registerModule(new JavaTimeModule());
//			final SampleType reason = objmapper.convertValue(inputMap.get("sampletype"), SampleType.class);
//			UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
//			});
//			requestContext.setUserInfo(userInfo);
//			return sampleTypeService.createSampleType(reason,userInfo);
//		
//	}
	
	
	@PostMapping(value = "/updateSampleType")
	public ResponseEntity<Object> updateSampleType(@RequestBody Map<String, Object> inputMap) throws Exception {

	
			final ObjectMapper objmapper = new ObjectMapper();
			objmapper.registerModule(new JavaTimeModule());
			final boolean DeleteExistingRecord =inputMap.containsKey("DeleteExistingRecord") ?(boolean) inputMap.get("DeleteExistingRecord") : false;
			final SampleType reason = objmapper.convertValue(inputMap.get("sampletype"), SampleType.class);
			UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
			});
			requestContext.setUserInfo(userInfo);
			return sampleTypeService.updateSampleType(reason,userInfo,DeleteExistingRecord);
	
	}
	
	
	@PostMapping(value = "/getTransactionFilterType")
	public ResponseEntity<Object> getTransactionFilterType(@RequestBody Map<String, Object> inputMap)throws Exception {

	
			final ObjectMapper objmapper = new ObjectMapper();				
			UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
			});
			requestContext.setUserInfo(userInfo);
			return sampleTypeService.getTransactionFilterType(userInfo);
		
	}
	
}
