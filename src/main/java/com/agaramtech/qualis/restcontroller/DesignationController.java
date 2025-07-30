package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.credential.model.Designation;
import com.agaramtech.qualis.credential.service.designation.DesignationService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the Designation Service methods.
 */
@RestController
@RequestMapping("/designation")
public class DesignationController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DesignationController.class);

	private final DesignationService designationService;
	private RequestContext requestContext;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext     RequestContext to hold the request
	 * @param designationService designationService
	 */

	public DesignationController(RequestContext requestContext, DesignationService designationService) {
		super();
		this.requestContext = requestContext;
		this.designationService = designationService;
	}

	/**
	 * This method is used to retrieve list of active credential designation for the
	 * specified site.
	 * 
	 * @param inputMap [Map] map object with "nsitecode" as key for which the list
	 *                 is to be fetched
	 * @return response object with list of active credential designation that are
	 *         to be listed for the specified site
	 */
	@PostMapping(value = "/getDesignation")
	public ResponseEntity<Object> getDesignation(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		LOGGER.info("getDesignation called");
		return designationService.getDesignation(userInfo);
	}

	/**
	 * This method is used to add new credential designation for the specified Site.
	 * 
	 * @param inputMap [Map] object with keys of designation entity.
	 * @return response entity of newly added designation entity
	 */

	@PostMapping(value = "/createDesignation")
	public ResponseEntity<Object> createDesignation(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final Designation designation = objmapper.convertValue(inputMap.get("designation"), Designation.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return designationService.createDesignation(designation, userInfo);
	}

	/**
	 * This method is used to retrieve selected active credential designation
	 * detail.
	 * 
	 * @param inputMap [Map] map object with "ndesignationcode" and "userInfo" as
	 *                 keys for which the data is to be fetched
	 * @return response object with selected active credential designation
	 */
	@PostMapping(value = "/getActiveDesignationById")
	public ResponseEntity<Object> getActiveDesignationById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int ndesignationcode = (Integer) inputMap.get("ndesignationcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return designationService.getActiveDesignationById(ndesignationcode, userInfo);
	}

	/**
	 * This method is used to update credential designation for the specified Site.
	 * 
	 * @param inputMap [Map] object with keys of designation entity.
	 * @return response entity of updated designation entity
	 */
	@PostMapping(value = "/updateDesignation")
	public ResponseEntity<Object> updateDesignation(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final Designation designation = objmapper.convertValue(inputMap.get("designation"), Designation.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return designationService.updateDesignation(designation, userInfo);
	}

	/**
	 * This method is used to delete credential designation for the specified Site.
	 * 
	 * @param mapObject [Map] object with keys of designation entity.
	 * @return response entity of deleted designation entity
	 */
	@PostMapping(value = "/deleteDesignation")
	public ResponseEntity<Object> deleteDesignation(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final Designation designation = objmapper.convertValue(inputMap.get("designation"), Designation.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return designationService.deleteDesignation(designation, userInfo);
	}

	/**
	 * This method is used to retrieve a specific designation record.
	 * 
	 * @param inputMap [Map] map object with "ndesignationcode" and "userinfo" as
	 *                 keys for which the data is to be fetched Input:{
	 *                 "ndesignationcode": 1, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"], "isutcenabled": 4,"ndeptcode":
	 *                 -1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 36,"nmastersitecode": -1,"nmodulecode": 3, "nreasoncode":
	 *                 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode":
	 *                 1,"nusercode": -1, "nuserrole": -1,"nusersitecode":
	 *                 -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with designation object for the specified primary key
	 *         / with string message as 'Deleted' if the designation record is not
	 *         available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getAllActiveDesignation")
	public ResponseEntity<Object> getAllActiveDesignation(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return designationService.getAllActiveDesignation(userInfo.getNmastersitecode());
	}

}
