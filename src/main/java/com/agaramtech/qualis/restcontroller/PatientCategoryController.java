package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.barcode.model.PatientCategory;
import com.agaramtech.qualis.barcode.service.patientcategory.PatientCategoryService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This class is used to perform CRUD Operation on "patientcategory" table by
 * implementing methods from its interface.
 * 
 * @author ATE236
 * @version 10.0.0.2
 */

@RestController
@RequestMapping("/patientcategory")
public class PatientCategoryController {
	private static final Logger LOGGER= LoggerFactory.getLogger(PatientCategoryController.class);

	private final RequestContext requestContext;

	private final PatientCategoryService patientCategoryService;

	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param PatientCategoryService patientCategoryService
	 */
	public PatientCategoryController(RequestContext requestContext, PatientCategoryService patientCategoryService) {
		super();
		this.requestContext = requestContext;
		this.patientCategoryService = patientCategoryService;
	}

	final ObjectMapper objMapper = new ObjectMapper();

	/**
	 * This Method is used to get the over all patientcategory with respect to site
	 * 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 
	 * @return a response entity which holds the list of patientcategory with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getPatientCategory")
	public ResponseEntity<Object> getPatientCategory(@RequestBody Map<String, Object> inputMap) throws Exception {

		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		LOGGER.info("getPatientCategory called");
		return patientCategoryService.getPatientCategory(userInfo);

	}

	/**
	 * This method is used to add a new entry to patientcategory table.
	 * 
	 * @param inputMap map object holding params ( 
	 * 								patientcategory [PatientCategory]  object holding details to be added in patientcategory table,
	 * 								userinfo [UserInfo] holding logged in user details
	 *                              ) 
	 *                           Input:{
									    "patientcategory": { "spatientcatname": "pc-01", "nprojecttypecode": 1,"ncode": 1 },
									    "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -   1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 33,"nmastersitecode": -1,"nmodulecode": 1,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",  "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}
									}
	 * @return ResponseEntity with string message as 'Already Exists' if the patientcategory already exists/ 
	 * 			list of patientcategories along with the newly added patientcategory.
	 * @throws Exception
	 */
	@PostMapping(value = "/createPatientCategory")
	public ResponseEntity<Object> createPatientCategory(@RequestBody Map<String, Object> inputMap) throws Exception {

		final PatientCategory objPatientCategory = objMapper.convertValue(inputMap.get("patientcategory"),
				new TypeReference<PatientCategory>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return patientCategoryService.createPatientCategory(objPatientCategory, userInfo);

	}

	/**
	 * This method is used to update entry in patientcategory table.
	 * 
	 * @param inputMap [map object holding params(
	 * 					patientcategory [PatientCategory]  object holding details to be updated in patientcategory table,
	 * 								userinfo [UserInfo] holding logged in user details) 
	 * 					Input:{
     						"patientcategory": {"npatientcatcode":1,"spatientcatname": "pc-0001","nprojecttypecode": 1, 
 									 "ncode": 1 },
    						"userinfo":{
                						"activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,  "nformcode": 33,"nmastersitecode": -1, "nmodulecode": 1,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,  "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US","slanguagename": "English",
 										"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
  										"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "", "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already Deleted' if the patientcategory record is not available/ 
	 * 			list of all patientcategories and along with the updated patientcategory.
	 * @throws Exception
	 */
	@PostMapping(value = "/updatePatientCategory")
	public ResponseEntity<Object> updatePatientCategory(@RequestBody Map<String, Object> inputMap) throws Exception {

		objMapper.registerModule(new JavaTimeModule());
		final PatientCategory objPatientCategory = objMapper.convertValue(inputMap.get("patientcategory"),
				new TypeReference<PatientCategory>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return patientCategoryService.updatePatientCategory(objPatientCategory, userInfo);

	}

	/**
	 * This method id used to delete an entry in patientcategory table
	 * 
	 * @param inputMap [Map] object with keys of patientcategory entity and UserInfo object.
	 * 					Input:{
     						"patientcategory": {"npatientcatcode":1},
    						"userinfo":{
                						"activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,  "nformcode": 33,"nmastersitecode": -1, "nmodulecode": 1,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,  "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US","slanguagename": "English",
 										"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
  										"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "", "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the patientcategory record is not available/ 
	 * 			string message as 'Record is used in....' when the patientcategory is associated in transaction /
	 * 			list of all patientcategories excluding the deleted record 
	 * @throws Exception
	 */
	@PostMapping(value = "/deletePatientCategory")
	public ResponseEntity<Object> deletePatientCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();

		objMapper.registerModule(new JavaTimeModule());
		final PatientCategory objPatientCategory = objMapper.convertValue(inputMap.get("patientcategory"),
				PatientCategory.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return patientCategoryService.deletePatientCategory(objPatientCategory, userInfo);

	}

	/**
	 * This method is used to get the single record in patientcategory table
	 * 
	 * @param inputMap  [Map] map object with "npatientcatcode" and "userinfo" as keys for which the data is to be fetched
	 *                  Input:{ "npatientcatcode": 1,
							     "userinfo":{
							                "activelanguagelist": ["en-US"],
							                "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							                "nformcode": 33,"nmastersitecode": -1,"nmodulecode": 1,
							                "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							                "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							                "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							                "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	  * @return ResponseEntity with PatientCategory object for the specified primary key / with string message as
	 * 						'Deleted' if the patientcategory record is not available
	 * @throws Exception
	 */
	@PostMapping(value = "/getActivePatientCategoryById")
	public ResponseEntity<Object> getActivePatientCategoryById(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final int npatientcatcode = (int) inputMap.get("npatientcatcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return patientCategoryService.getActivePatientCategoryById(npatientcatcode, userInfo);
	}

}
