package com.agaramtech.qualis.restcontroller;


import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.dynamicpreregdesign.model.ReactRegistrationTemplate;
import com.agaramtech.qualis.dynamicpreregdesign.service.dynamicpreregdesign.DynamicPreRegDesignService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * This controller is used for Designing dynamic Pre-Register pop-up.
 * @author ATE234 janakumar
 * @since 09-04-2025
 */
@RestController
@RequestMapping(value = "dynamicpreregdesign")
public class DynamicPreRegDesignController {

	private static final Logger LOGGER = LoggerFactory.getLogger(RegionController.class);
	
	private RequestContext requestContext;
	private final DynamicPreRegDesignService dynamicPreRegDesignService;

	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param unitService UnitService
	 */
	public DynamicPreRegDesignController(RequestContext requestContext, DynamicPreRegDesignService dynamicPreRegDesignService) {
		super();
		this.requestContext = requestContext;
		this.dynamicPreRegDesignService = dynamicPreRegDesignService;
	}
	/**
	 * This method is used to get the available input components such as Text Box,Combo Box etc...
	 * @param inputMap contains UserInfo
	 * @return List of input components as JSON format
	 * @throws Exception
	 */
	@PostMapping(path = "getReactComponents")
	public ResponseEntity<Object> getReactComponents(@RequestBody Map<String, Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		LOGGER.info("Get Controller -->"+userInfo);
		return dynamicPreRegDesignService.getReactComponents(inputMap,userInfo);
		
	}
	/**
	 * This method is used to get the available input fields created before.
	 * @param inputMap contains UserInfo
	 * @return List of available input fields created before as JSON format
	 * @throws Exception
	 */
	@PostMapping(path = "getReactInputFields")
	public ResponseEntity<Object> getReactInputFields(@RequestBody Map<String, Object> inputMap) throws Exception{
		
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dynamicPreRegDesignService.getReactInputFields(inputMap,userInfo);
		
	}
	/**
	 * This method is used to get the available input fields created before.
	 * @param inputMap contains UserInfo
	 * @return List of available input fields created before as JSON format
	 * @throws Exception
	 */
	@PostMapping(path = "getDynamicPreRegDesign")
	public ResponseEntity<Object> getDynamicPreRegDesign(@RequestBody Map<String, Object> inputMap) throws Exception{
		
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dynamicPreRegDesignService.getRegistrationTemplate(inputMap,userInfo);
		
	}
	
	@PostMapping(path = "getDefaultTemplate")
	public ResponseEntity<Object> getDefaultTemplate(@RequestBody Map<String, Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dynamicPreRegDesignService.getDefaultTemplate(inputMap,userInfo);
	}
	/**
	 * This method is used to get the available input fields created before.
	 * @param inputMap contains UserInfo
	 * @return List of available input fields created before as JSON format
	 * @throws Exception
	 */
	@PostMapping(path = "getRegistrationTemplateById")
	public ResponseEntity<Object> getRegistrationTemplateById(@RequestBody Map<String, Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nreactRegTemplateCode = (int) inputMap.get("nreactregtemplatecode");
		return dynamicPreRegDesignService.getRegistrationTemplateById(nreactRegTemplateCode,userInfo);
		
	}
	/**
	 * This method is used to get the available input fields created before.
	 * @param inputMap contains UserInfo
	 * @return List of available input fields created before as JSON format
	 * @throws Exception
	 */
	@PostMapping(path = "createRegistrationTemplate")
	public ResponseEntity<Object> createRegistrationTemplate(@RequestBody Map<String, Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final ReactRegistrationTemplate regTemplate = objMapper.convertValue(inputMap.get("registrationtemplate"), ReactRegistrationTemplate.class);
		return dynamicPreRegDesignService.createRegistrationTemplate(regTemplate,userInfo);
		
	}
	@PostMapping(path = "updateRegistrationTemplate")
	public ResponseEntity<Object> updateRegistrationTemplate(@RequestBody Map<String, Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final ReactRegistrationTemplate regTemplate = objMapper.convertValue(inputMap.get("registrationtemplate"), ReactRegistrationTemplate.class);
		return dynamicPreRegDesignService.updateRegistrationTemplate(regTemplate,userInfo);
		
	}
	@PostMapping(path = "deleteRegistrationTemplate")
	public ResponseEntity<Object> deleteRegistrationTemplate(@RequestBody Map<String, Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final ReactRegistrationTemplate regTemplate = objMapper.convertValue(inputMap.get("registrationtemplate"), ReactRegistrationTemplate.class);
		return dynamicPreRegDesignService.deleteRegistrationTemplate(regTemplate.getNreactregtemplatecode(),userInfo);
		
	}
	@PostMapping(path = "approveRegistrationTemplate")
	public ResponseEntity<Object> approveRegistrationTemplate(@RequestBody Map<String, Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		//final ReactRegistrationTemplate regTemplate = objMapper.convertValue(inputMap.get("registrationtemplate"), ReactRegistrationTemplate.class);
		return dynamicPreRegDesignService.approveRegistrationTemplate(inputMap,userInfo);
		
	}
	@PostMapping(path = "copyRegistrationTemplate")
	public ResponseEntity<Object> copyRegistrationTemplate(@RequestBody Map<String, Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final ReactRegistrationTemplate regTemplate = objMapper.convertValue(inputMap.get("registrationtemplate"), ReactRegistrationTemplate.class);
		return dynamicPreRegDesignService.copyRegistrationTemplate(regTemplate,userInfo);
		
	}
	
	@PostMapping(path = "getComboValues")
	public ResponseEntity<Object> getComboValues(@RequestBody Map<String, Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dynamicPreRegDesignService.getComboValues(inputMap,userInfo);
		
	}
	
	@PostMapping(path = "getChildValues")
	public ResponseEntity<Object> getChildValues(@RequestBody Map<String, Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dynamicPreRegDesignService.getChildValues(inputMap,userInfo);
		
	}

	@PostMapping(path = "getComboTables")
	public ResponseEntity<Object> getComboTables(@RequestBody Map<String, Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dynamicPreRegDesignService.getComboTables(userInfo);
	}
	@PostMapping(path = "getTableColumns")
	public ResponseEntity<Object> getTableColumns(@RequestBody Map<String, Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final String tableName= inputMap.get("nquerybuildertablecode").toString();
		return dynamicPreRegDesignService.getTableColumns(tableName,userInfo);
		
	}
	@PostMapping(path = "getMasterDesign")
	public ResponseEntity<Object> getMasterDesign(@RequestBody Map<String, Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dynamicPreRegDesignService.getMasterDesign(userInfo);
		
	}
	
	@PostMapping(path = "getcustomsearchfilter")
	public ResponseEntity<Object> getcustomsearchfilter(@RequestBody Map<String, Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dynamicPreRegDesignService.getcustomsearchfilter(inputMap,userInfo);
		
	}
	
	@PostMapping(path = "getcustomsearchfilterpredefined")
	public ResponseEntity<Object> getcustomsearchfilterpredefined(@RequestBody Map<String, Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dynamicPreRegDesignService.getcustomsearchfilterpredefined(inputMap,userInfo);
		
	}
	
	@PostMapping(path = "getdynamicfilterexecutedata")
	public ResponseEntity<Object> getDynamicFilterExecuteData(@RequestBody Map<String, Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dynamicPreRegDesignService.getDynamicFilterExecuteData(inputMap,userInfo);
		
	}

	@PostMapping(path = "dateValidation")
	public ResponseEntity<Object> dateValidation(@RequestBody Map<String, Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dynamicPreRegDesignService.dateValidation(inputMap,userInfo);
	}
	/**
	 * This method is used to get the available input fields created before.
	 * @param inputMap contains UserInfo
	 * @return List of available input fields created before as JSON format
	 * @throws Exception
	 */
	@PostMapping(path = "getReactStaticFilterTables")
	public ResponseEntity<Object> getReactStaticFilterTables(@RequestBody Map<String, Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dynamicPreRegDesignService.getReactStaticFilterTables(inputMap,userInfo);
		
	}
	
	
	@PostMapping(path = "validatePreview")
	public ResponseEntity<Object> validatePreview(@RequestBody Map<String, Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dynamicPreRegDesignService.validatePreview(inputMap,userInfo);
	}
	
	
	@PostMapping(path = "getExternalportalDetail")
	public ResponseEntity<Object> getExternalportalDetail(@RequestBody Map<String, Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dynamicPreRegDesignService.getExternalportalDetail(inputMap,userInfo);
	}
	
	@PostMapping(path = "getDefaultSampleType")
	public ResponseEntity<Object> getDefaultSampleType(@RequestBody Map<String, Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dynamicPreRegDesignService.getDefaultSampleType(inputMap,userInfo);
	}
	
	@PostMapping(value = "/createImportTemplate")
	public ResponseEntity<Object> createImportTemplate(MultipartHttpServletRequest request)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dynamicPreRegDesignService.createImportTemplate(request, userInfo);
	}
	
	
}
