package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.dynamicmaster.model.DynamicMaster;
import com.agaramtech.qualis.dynamicmaster.service.DynamicMasterService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the Dynamic Master Service methods.
 * 
 * @author ATE234
 * @version 1.0.0
 * @since 21-04-2025
 */
@RestController
@RequestMapping(value = "dynamicmaster")
public class DynamicMasterController {

	private static final Logger LOGGER = LoggerFactory.getLogger(DynamicMasterController.class);

	private RequestContext requestContext;
	private final DynamicMasterService dynamicMasterService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext RequestContext to hold the request
	 * @param DynamicMasterService dynamicMasterService
	 */
	public DynamicMasterController(RequestContext requestContext, DynamicMasterService dynamicMasterService) {
		super();
		this.requestContext = requestContext;
		this.dynamicMasterService = dynamicMasterService;
	}

	@PostMapping(path = "getDynamicMaster")
	public ResponseEntity<Object> getDynamicMaster(@RequestBody Map<String, Object> inputMap) throws Exception {
		LOGGER.info("getDynamicMaster()");
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dynamicMasterService.getDynamicMaster(userInfo);
	}

	@PostMapping(path = "getMasterDesign")
	public ResponseEntity<Object> getMasterDesign(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dynamicMasterService.getMasterDesign(userInfo);
	}

	@PostMapping(path = "createDynamicMaster")
	public ResponseEntity<Object> createDynamicMaster(MultipartHttpServletRequest inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(inputMap.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dynamicMasterService.createDynamicMaster(inputMap, userInfo);
	}

	@PostMapping(path = "getActiveDynamicMasterById")
	public ResponseEntity<Object> getActiveDynamicMasterById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dynamicMasterService.getActiveDynamicMasterById(inputMap, userInfo);
	}

	@PostMapping(path = "updateDynamicMaster")
	public ResponseEntity<Object> updateDynamicMaster(MultipartHttpServletRequest inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(inputMap.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dynamicMasterService.updateDynamicMaster(inputMap, userInfo);
	}

	@PostMapping(path = "deleteDynamicMaster")
	public ResponseEntity<Object> deleteDynamicMaster(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final DynamicMaster dynamicMaster = objMapper.convertValue(inputMap.get("dynamicmaster"), DynamicMaster.class);
		return dynamicMasterService.deleteDynamicMaster(dynamicMaster, userInfo);

	}

	@PostMapping(value = "/importDynamicMaster")
	public ResponseEntity<Object> importMultilingualProperties(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dynamicMasterService.importDynamicMaster(request, userInfo);
	}

	/**
	 * This request method is used to get the list of active entries available in
	 * the specified form/screen based on the provided screen name through its
	 * implementation method. * Eg: Input Format { "formName": "New Master"}
	 * 
	 * @param inputMap [Map] Object with 'formName' object as a key holding name of
	 *                 the screen/form as a string
	 * @return Returns the entries list available in the specified screen
	 * @throws Exception when valid input is not provided or some error thrown in
	 *                   query execution
	 */
	@PostMapping(path = "getDynamicMasterByForm")
	public ResponseEntity<Object> getDynamicMasterByForm(@RequestBody Map<String, Object> inputMap) throws Exception {
		final String formName = (String) inputMap.get("formName");
		return dynamicMasterService.getDynamicMasterByForm(formName);
	}

	@PostMapping(value = "/viewDynamicMaster")
	public ResponseEntity<Object> viewDynamicMaster(@RequestBody Map<String, Object> inputMap,
			HttpServletResponse response) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final DynamicMaster objFile = objMapper.convertValue(inputMap.get("viewFile"), DynamicMaster.class);
		final Map<String, Object> outputMap = dynamicMasterService.viewDynamicMaster(objFile, userInfo, inputMap);
		requestContext.setUserInfo(userInfo);
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}
}