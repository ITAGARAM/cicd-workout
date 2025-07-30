package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testmanagement.model.MethodCategory;
import com.agaramtech.qualis.testmanagement.service.methodcategory.MethodCategoryService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;




/**
 * This controller is used to dispatch the input request to its relevant service methods
 * to access the Methodcategory Service methods.
 */
@RestController
@RequestMapping("/methodcategory")
public class MethodCategoryController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodCategoryController.class);

	private RequestContext requestContext;
	private final MethodCategoryService methodCategoryservice;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param MethodCategoryService methodCategoryservice
	 */
	
	public MethodCategoryController(RequestContext requestContext, MethodCategoryService methodCategoryservice) {
		this.requestContext = requestContext;
		this.methodCategoryservice = methodCategoryservice;
	}
	
	/**
	 * This method is used to retrieve list of available Method Category. 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 	
	 * 					Input : {"userinfo":{nmastersitecode": -1}}				
	 * @return response entity  object holding response status and list of all method categories.
	 * @throws Exception exception
	 */
	
	@PostMapping(value = "/getMethodCategory")
	public ResponseEntity<Object> getMethodCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		LOGGER.info("UserInfo:"+ userInfo);
		requestContext.setUserInfo(userInfo);
		return methodCategoryservice.getMethodCategory(userInfo.getNmastersitecode());
	}
	
	/**
	 * This method will is used to make a new entry to methodcategory  table.
	 * @param inputMap map object holding params ( 
	 * 	mathodcategory [MethodCategory]  object holding details to be added in methodcategory table,
	 * 	userinfo [UserInfo] object holding logged in user details
	 *  ) 
	 * Input:{
        "methodcategory": { "smethodname": "Spectrophotometry", "sdescription": " This category included method like UV-Vis spectrophotometry" },
        "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": - 1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 21,"nmastersitecode": -1,"nmodulecode": 1,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss",
        "sreason": "","ssitedate": "dd/MM/yyyy",  "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}
         }
	 * @return ResponseEntity with string message as 'Already Exists' if the method category already exists/ 
	 * 			list of method categories along with the newly added method category.
	 * @throws Exception exception
	 */
	
	@PostMapping(value = "/createMethodCategory")
	public ResponseEntity<Object> createMethodCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final MethodCategory methodcategory = objmapper.convertValue(inputMap.get("methodcategory"),MethodCategory.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return methodCategoryservice.createMethodCategory(methodcategory, userInfo);
	}

	
	/**
	 * This method is used to retrieve a specific methodcategory record.
	 * @param inputMap  [Map] map object with "nmethodcatcode" and "userinfo" as keys for which the data is to be fetched
	 * Input:{ "nmethodcatcode": 1,
       "userinfo":{
		"activelanguagelist": ["en-US"],
		"isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
		"nformcode": 21,"nmastersitecode": -1,"nmodulecode": 1,
		"nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
		"nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
		"sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
		"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
		"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
		"ssitedatetime": "dd/MM/yyyy HH:mm:ss"}
		 }
	 * @return ResponseEntity with MethodCategory object for the specified primary key / with string message as
	 * 'Deleted' if the methodcategory record is not available
	 * @throws Exception exception
	 */
		
	@PostMapping(value = "/getActiveMethodCategoryById")
	public ResponseEntity<Object> getActiveMethodCategoryById(@RequestBody Map<String, Object> inputMap)throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nmethodcatcode = (Integer) inputMap.get("nmethodcatcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return methodCategoryservice.getActiveMethodCategoryById(nmethodcatcode, userInfo);
	}


	/**
	 * This method is used to update selected methodcategory details.
	 * @param inputMap [map object holding params(
	 * methodcategory [MethodCategory]  object holding details to be updated in methodcategory table,
	 * userinfo [UserInfo] holding logged in user details) 
	 * Input:{
       " methodcategory": { "nmethodcatcode":1, smethodname": "Chromatography"},
        "userinfo":{
        "activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,  
        "nformcode": 21,"nmastersitecode": -1, "nmodulecode": 1,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,  "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US","slanguagename": "English",
 		"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
  		"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "", "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * 	  		
	 * @return ResponseEntity with string message as 'Already Deleted' if the methodcategory record is not available/ 
	 * list of all methodcategories and along with the updated methodcategory.	 
	 * @throws Exception exception
	 */	
		
	@PostMapping(value = "/updateMethodCategory")
	public ResponseEntity<Object> updateMethodCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final MethodCategory methodcategory = objmapper.convertValue(inputMap.get("methodcategory"),MethodCategory.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return methodCategoryservice.updateMethodCategory(methodcategory, userInfo);
	}

	/**
	 * This method is used to delete an entry in methodcategory table
	 * @param inputMap [Map] object with keys of MethodCategory entity and UserInfo object.
	 * Input:{
       "methodcategory": {"nmethodcatcode":1},
    	"userinfo":{
               "activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,  "nformcode":21,"nmastersitecode": -1, "nmodulecode": 1,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,  "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US","slanguagename": "English",
 			   "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
  			   "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "", "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the methodcategory record is not available/ 
	 * 	string message as 'Record is used in....' when the methodcategory is associated in transaction /
	 * 	list of all methodcategories excluding the deleted record 
	 * @throws Exception exception
	 */
	
	@PostMapping(value = "/deleteMethodCategory")
	public ResponseEntity<Object> deleteMethodCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final MethodCategory methodcategory = objmapper.convertValue(inputMap.get("methodcategory"),MethodCategory.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return methodCategoryservice.deleteMethodCategory(methodcategory, userInfo);

	}

}
