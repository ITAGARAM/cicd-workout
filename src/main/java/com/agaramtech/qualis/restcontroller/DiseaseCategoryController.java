package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.project.model.DiseaseCategory;
import com.agaramtech.qualis.project.service.diseasecategory.DiseaseCategoryService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant service methods
 * to access the DiseaseCategory Service methods.
 */
/**
 * @author sujatha.v
 * BGSI-1
 * 26/06/2025
 */
@RestController
@RequestMapping("/diseasecategory")
public class DiseaseCategoryController {

	private final static Logger LOGGER= LoggerFactory.getLogger(DiseaseCategoryController.class);
	private final DiseaseCategoryService diseaseCategoryService;
	private RequestContext requestContext;

	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param diseaseCategoryService DiseaseCategoryService
	 */
	public DiseaseCategoryController(DiseaseCategoryService diseaseCategoryService, RequestContext requestContext) {
		super();
		this.diseaseCategoryService = diseaseCategoryService;
		this.requestContext = requestContext;
	}

	/**
	 * This method is used to retrieve list of available diseasecategory(s). 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 	
	 * 					Input : {"userinfo":{nmastersitecode": -1}}				
	 * @return response entity  object holding response status and list of all diseasecategory
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getDiseaseCategory")
	public ResponseEntity<Object> getDiseaseCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("getDiseaseCategory() called");
		requestContext.setUserInfo(userInfo);
//		return new ResponseEntity<Object>(diseaseCategoryService.getDiseaseCategory(userInfo),HttpStatus.OK);
		return diseaseCategoryService.getDiseaseCategory(userInfo);
	}

	/**
	 * This method is used to retrieve a specific diseasecategory record.
	 * @param inputMap  [Map] map object with "ndiseasecategorycode" and "userinfo" as keys for which the data is to be fetched
	 *                  Input:{ "ndiseasecategorycode": 1,
							     "userinfo":{
							                "activelanguagelist": ["en-US"],
							                "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							                "nformcode": 253,"nmastersitecode": -1,"nmodulecode": 54,
							                "nreasoncode": 0,"nsitecode": -1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							                "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							                "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							                "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with DiseaseCategory object for the specified primary key / with string message as
	 * 						'Deleted' if the diseasecategory record is not available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveDiseaseCategoryById")
	public ResponseEntity<Object> getActiveDiseaseCategoryById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final int ndiseasecategorycode = (Integer) inputMap.get("ndiseasecategorycode");
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return diseaseCategoryService.getActiveDiseaseCategoryById(ndiseasecategorycode, userInfo);
	}

	/**
	 * This method will is used to make a new entry to diseasecategory table.
	 * @param inputMap map object holding params ( 
	 * 								diseasecategory [DiseaseCategory]  object holding details to be added in diseasecategory table,
	 * 								userinfo [UserInfo] holding logged in user details
	 *                              ) 
	 *                           Input:{
									    "diseasecategory": { "sdiseasecategoryname": "disease1"},
									    "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -   1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 253,"nmastersitecode": -1,"nmodulecode": 54,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",  "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}
									}
	 * @return ResponseEntity with string message as 'Already Exists' if the diseasecategory already exists/ 
	 * 			list of diseasecategory's along with the newly added diseasecategory.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createDiseaseCategory")
	public ResponseEntity<Object> createDiseaseCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final DiseaseCategory diseaseCategory = objectMapper.convertValue(inputMap.get("diseasecategory"),
				new TypeReference<DiseaseCategory>() {
				});
		requestContext.setUserInfo(userInfo);
		return diseaseCategoryService.createDiseaseCategory(diseaseCategory, userInfo);

	}

	/**
	 * This method is used to update selected diseasecategory details.
	 * @param inputMap [map object holding params(
	 * 					diseasecategory [DiseaseCategory]  object holding details to be updated in diseasecategory table,
	 * 								userinfo [UserInfo] holding logged in user details) 
	 * 					Input:{
     						"diseasecategory": {"ndiseasecategorycode":1,"sdiseasecategoryname": "disease2", "sdescription": "m" },
    						"userinfo":{
                						"activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,  "nformcode": 253,"nmastersitecode": -1, "nmodulecode": 54,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,  "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US","slanguagename": "English",
 										"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
  										"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "", "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}

	 * 	  		
	 * @return ResponseEntity with string message as 'Already Deleted' if the diseasecategory record is not available/ 
	 * 			list of all diseasecategory's and along with the updated diseasecategory.	 
	 * @throws Exception exception
	 */	
	@PostMapping(value = "/updateDiseaseCategory")
	public ResponseEntity<Object> updateDiseaseCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final DiseaseCategory diseaseCategory = objectMapper.convertValue(inputMap.get("diseasecategory"),
				new TypeReference<DiseaseCategory>() {
				});
		requestContext.setUserInfo(userInfo);
		return diseaseCategoryService.updateDiseaseCategory(diseaseCategory, userInfo);
	}

	/**
	 * This method is used to delete an entry in DiseaseCategory table
	 * @param inputMap [Map] object with keys of DiseaseCategory entity and UserInfo object.
	 * 					Input:{
     						"diseasecategory": {"ndiseasecategorycode":1},
    						"userinfo":{
                						"activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,  "nformcode": 253,"nmastersitecode": -1, "nmodulecode": 54,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,  "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US","slanguagename": "English",
 										"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
  										"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "", "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the diseasecategory record is not available/ 
	 * 			string message as 'Record is used in....' when the diseasecategory is associated in transaction /
	 * 			list of all diseasecategory's excluding the deleted record 
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteDiseaseCategory")
	public ResponseEntity<Object> deleteDiseaseCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
	    DiseaseCategory diseaseCategory = objectMapper.convertValue(inputMap.get("diseasecategory"),
				new TypeReference<DiseaseCategory>() { });
		requestContext.setUserInfo(userInfo);
		return diseaseCategoryService.deleteDiseaseCategory(diseaseCategory, userInfo);
	}
}
