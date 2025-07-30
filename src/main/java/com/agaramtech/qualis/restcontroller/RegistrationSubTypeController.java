package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.dynamicpreregdesign.model.RegSubTypeConfigVersion;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegSubTypeConfigVersionRelease;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationSubType;
import com.agaramtech.qualis.dynamicpreregdesign.service.registrationsubtypemaster.RegistrationSubTypeService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;



@RestController
@RequestMapping(value = "/registrationsubtype")
public class RegistrationSubTypeController {

	private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationSubTypeController.class);

	private RequestContext requestContext;
	private final RegistrationSubTypeService regSubTypeService;
	
	public RegistrationSubTypeController(RequestContext requestContext, RegistrationSubTypeService regSubTypeService) {
		super();
		this.requestContext = requestContext;
		this.regSubTypeService = regSubTypeService;
	}
	
	/**
	 * 
	 * @param inputParam
	 * @return
	 * @throws Exception 
	 */
	@PostMapping(path = "/getRegistrationSubType")
	public ResponseEntity<Object> getRegistrationSubTypeInitial(@RequestBody Map<String,Object> inputParam) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputParam.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return regSubTypeService.getRegistrationSubTypeInitial(userInfo);
		
	}
	
	@PostMapping(path = "/getRegistrationSubTypeMaster")
	public ResponseEntity<Object> getRegistrationSubType(@RequestBody Map<String,Object> inputParam) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputParam.get("userinfo"), UserInfo.class);
		final short regTypeCode = Short.parseShort(inputParam.get("nregtypecode").toString());
		short regSubTypeCode =-1;
		if (inputParam.get("nregsubtypecode") != null) {
			regSubTypeCode = Short.parseShort(inputParam.get("nregsubtypecode").toString());
		}
		
		requestContext.setUserInfo(userInfo);
		return regSubTypeService.getRegistrationSubType(regTypeCode, userInfo, regSubTypeCode);
		
	}
	
	@PostMapping(path = "/getRegistrationSubTypeById")
	public ResponseEntity<Object> getRegistrationSubTypeById(@RequestBody Map<String,Object> inputParam) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputParam.get("userinfo"), UserInfo.class);
		final short regSubTypeCode = Short.parseShort(inputParam.get("nregsubtypecode").toString());
		requestContext.setUserInfo(userInfo);
		return regSubTypeService.getRegistrationSubTypeById(regSubTypeCode, userInfo);
		
	}
	
	@PostMapping(path = "/getVersionDetails")
	public ResponseEntity<Object> getVersionDetails(@RequestBody Map<String,Object> inputParam) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputParam.get("userinfo"), UserInfo.class);
		final int regSubTypeConfigCode = (int) inputParam.get("nregsubtypeversioncode");
		requestContext.setUserInfo(userInfo);
		return regSubTypeService.getVersionDetails(regSubTypeConfigCode, userInfo);
		
	}
	@PostMapping(path = "/getVersionDetailsByRelease")
	public ResponseEntity<Object> getVersionDetailsByRelease(@RequestBody Map<String,Object> inputParam) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputParam.get("userinfo"), UserInfo.class);
		final int regSubTypeConfigCode = (int) inputParam.get("nregsubtypeversioncode");
		requestContext.setUserInfo(userInfo);
		return regSubTypeService.getVersionDetailsByRelease(regSubTypeConfigCode, userInfo);
		
	}
		
	@PostMapping(path = "/createRegistrationSubType")
	public ResponseEntity<Object> createRegistrationSubType(@RequestBody Map<String,Object> inputParam) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputParam.get("userinfo"), UserInfo.class);
		final RegistrationSubType regSubType = objMapper.convertValue(inputParam.get("registrationsubtype"), RegistrationSubType.class);
		requestContext.setUserInfo(userInfo);
		return regSubTypeService.createRegistrationSubType(regSubType, userInfo);
		
	}
	
	@PostMapping(path = "/getEditRegSubType")
	public ResponseEntity<Object> getEditRegSubType(@RequestBody Map<String,Object> inputParam) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputParam.get("userinfo"), UserInfo.class);
		final short regSubTypeCode = Short.parseShort(inputParam.get("nregsubtypecode").toString());
		requestContext.setUserInfo(userInfo);
		return regSubTypeService.getEditRegSubType(regSubTypeCode, userInfo);
		
	}
	
	@PostMapping(path = "/updateRegistrationSubType")
	public ResponseEntity<Object> updateRegistrationSubType(@RequestBody Map<String,Object> inputParam) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputParam.get("userinfo"), UserInfo.class);
		final RegistrationSubType regSubType = objMapper.convertValue(inputParam.get("registrationsubtype"), RegistrationSubType.class);
		requestContext.setUserInfo(userInfo);
		return regSubTypeService.updateRegistrationSubType(regSubType, userInfo);
		
	}
	
	@PostMapping(path = "/deleteRegistrationSubType")
	public ResponseEntity<Object> deleteRegistrationSubType(@RequestBody Map<String,Object> inputParam) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputParam.get("userinfo"), UserInfo.class);
		final RegistrationSubType regSubType = objMapper.convertValue(inputParam.get("registrationsubtype"), RegistrationSubType.class);
		requestContext.setUserInfo(userInfo);
		return regSubTypeService.deleteRegistrationSubType(regSubType, userInfo);
		
	}
	
	@PostMapping(path = "/getAllSeqNoFormats")
	public ResponseEntity<Object> getAllSeqNoFormats(@RequestBody Map<String,Object> inputParam) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputParam.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return regSubTypeService.getAllSeqNoFormats(userInfo);
		
	}
	@PostMapping(path = "/getAllSeqNoFormatsByRelease")
	public ResponseEntity<Object> getAllSeqNoFormatsByRelease(@RequestBody Map<String,Object> inputParam) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputParam.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return regSubTypeService.getAllSeqNoFormatsByRelease(userInfo);
		
	}
	@PostMapping(path = "/getPeriods")
	public ResponseEntity<Object> getPeriods(@RequestBody Map<String,Object> inputParam) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputParam.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return regSubTypeService.getPeriods(userInfo);
		
	}
	@PostMapping(path = "/createVersion")
	public ResponseEntity<Object> createVersion(@RequestBody Map<String,Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final RegSubTypeConfigVersion version= objMapper.convertValue(inputMap.get("version"), RegSubTypeConfigVersion.class);
		requestContext.setUserInfo(userInfo);
		return regSubTypeService.createVersion(version, userInfo);
	}
	@PostMapping(path = "/updateVersion")
	public ResponseEntity<Object> updateVersion(@RequestBody Map<String,Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final RegSubTypeConfigVersion version= objMapper.convertValue(inputMap.get("version"), RegSubTypeConfigVersion.class);
		requestContext.setUserInfo(userInfo);
		return regSubTypeService.updateVersion(version, userInfo);
	}
	@PostMapping(path = "/deleteVersion")
	public ResponseEntity<Object> deleteVersion(@RequestBody Map<String,Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final RegSubTypeConfigVersion version= objMapper.convertValue(inputMap.get("version"), RegSubTypeConfigVersion.class);
		requestContext.setUserInfo(userInfo);
		return regSubTypeService.deleteVersion(version, userInfo);
	}
	@PostMapping(path = "/approveVersion")
	public ResponseEntity<Object> approveVersion(@RequestBody Map<String,Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final RegSubTypeConfigVersion version= objMapper.convertValue(inputMap.get("version"), RegSubTypeConfigVersion.class);
		requestContext.setUserInfo(userInfo);
		return regSubTypeService.approveVersion(version, userInfo);
	}
	
	@PostMapping(path = "/getApprovedVersion")	
	public RegSubTypeConfigVersion getApprovedVersion(@RequestBody Map<String,Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int approvalConfigCode = Integer.parseInt(inputMap.get("napprovalconfigcode").toString());
		requestContext.setUserInfo(userInfo);
		return regSubTypeService.getApprovedVersion(approvalConfigCode,userInfo);
	}
	
	
	@PostMapping(path = "/getSiteWiseAllSeqNoFormats")
	public ResponseEntity<Object> getSiteWiseAllSeqNoFormats(@RequestBody Map<String,Object> inputParam) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputParam.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return regSubTypeService.getSiteWiseAllSeqNoFormats(userInfo);
		
	}
	
	@PostMapping(path = "/getSiteWiseAllSeqNoFormatsRelease")
	public ResponseEntity<Object> getSiteWiseAllSeqNoFormatsRelease(@RequestBody Map<String,Object> inputParam) throws Exception{
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputParam.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return regSubTypeService.getSiteWiseAllSeqNoFormatsRelease(userInfo);
		
	}
	
	@PostMapping(path = "/updateReleaseArNoVersion")
	public ResponseEntity<Object> updateReleaseArNoVersion(@RequestBody Map<String,Object> inputParam) throws Exception{
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputParam.get("userinfo"), UserInfo.class);
		final RegSubTypeConfigVersionRelease releaseversion= objMapper.convertValue(inputParam.get("version"), RegSubTypeConfigVersionRelease.class);
		requestContext.setUserInfo(userInfo);
		return regSubTypeService.updateReleaseArNoVersion(releaseversion,userInfo);
		
	}
}
