package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationType;
import com.agaramtech.qualis.dynamicpreregdesign.service.registrationtypemaster.RegistrationTypeMasterService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@RestController
@RequestMapping(value = "/registrationtype")
public class RegistrationTypeController {
	

	private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationTypeController.class);

	private RequestContext requestContext;
	private final RegistrationTypeMasterService registrationTypeService;
	
	public RegistrationTypeController(RequestContext requestContext, RegistrationTypeMasterService registrationTypeService) {
		super();
		this.requestContext = requestContext;
		this.registrationTypeService = registrationTypeService;
	}
	
	//final ObjectMapper objmapper = new ObjectMapper();

	@PostMapping(path = "/getRegistrationType")
	public ResponseEntity<Object> getRegistrationType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return registrationTypeService.getRegistrationType(userInfo);
	}

	@RequestMapping(value = "/getSampleType", method = RequestMethod.POST)
	public ResponseEntity<Object> getSampleType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return registrationTypeService.getSampleType(userInfo);
	}

	@PostMapping(path = "/getRegistrationTypeBySampleType")
	public ResponseEntity<Object> getRegistrationTypeBySampleType(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int sampleTypeCode = (int) inputMap.get("nsampletypecode");
		requestContext.setUserInfo(userInfo);
		return registrationTypeService.getRegistrationTypeBySampleType(sampleTypeCode, userInfo);
	}

//	@PostMapping(path = "/getRegistrationSubType")
//	public ResponseEntity<Object> getRegistrationSubType(@RequestBody Map<String, Object> inputMap) throws Exception {
//		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
//		final int regTypeCode = (int) inputMap.get("nregtypecode");
//		requestContext.setUserInfo(userInfo);
//		return regSubtypeService.getRegistrationSubType(regTypeCode, userInfo);
//	}

	@PostMapping(path = "/getRegistrationTypeById")
	public ResponseEntity<Object> getRegistrationTypeById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int regTypeCode = (int) inputMap.get("nregtypecode");
		requestContext.setUserInfo(userInfo);
		return registrationTypeService.getRegistrationTypeById(regTypeCode, userInfo);
	}

//	@PostMapping(path = "/getApprovalConfig")
//	public ResponseEntity<Object> getApprovalConfig(@RequestBody Map<String, Object> inputMap) throws Exception {
//		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
//		requestContext.setUserInfo(userInfo);
//		final int regTypeCode = (int) inputMap.get("nregtypecode");
//		final int regSubTypeCode = (int) inputMap.get("nregsubtypecode");
//		return new ResponseEntity<>(regSubtypeService.getApprovalConfig(regTypeCode, regSubTypeCode, userInfo),
//				HttpStatus.OK);
//	}

	@PostMapping(path = "/createRegistrationType")
	public ResponseEntity<Object> createRegistrationType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final RegistrationType registrationType = objMapper.convertValue(inputMap.get("registrationtype"),
				RegistrationType.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return registrationTypeService.createRegistrationType(registrationType, userInfo, inputMap);
	}

	@PostMapping(path = "/updateRegistrationType")
	public ResponseEntity<Object> updateRegistrationType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final RegistrationType registrationType = objMapper.convertValue(inputMap.get("registrationtype"),
				RegistrationType.class);
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return registrationTypeService.updateRegistrationType(registrationType, userInfo, inputMap);
	}

	@RequestMapping(value = "/deleteRegistrationType", method = RequestMethod.POST)
	public ResponseEntity<Object> deleteRegistrationType(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		RegistrationType objRegistrationType = objMapper.convertValue(inputMap.get("registrationtype"),
				new TypeReference<RegistrationType>() {
				});
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return registrationTypeService.deleteRegistrationType(objRegistrationType, userInfo);

	}

}
