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
import com.agaramtech.qualis.testmanagement.model.TestPackage;
import com.agaramtech.qualis.testmanagement.service.testpackage.TestPackageService;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * This controller is used to dispatch the input request to its relevant service methods
 * to access the testpackage Service methods.
 */

@RestController
@RequestMapping("/testpackage")
public class TestPackageController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TestPackageController.class);

	private RequestContext requestContext;
	private final TestPackageService testPackageService;

	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param testpackageService testpackageService
	 */
	public TestPackageController(RequestContext requestContext, TestPackageService testPackageService) {
		super();
		this.requestContext = requestContext;
		this.testPackageService = testPackageService;
	}
	
	/**
	 * This method is used to retrieve list of available testPackage(s). 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 	
	 * 					Input : {"userinfo":{nmastersitecode": -1}}				
	 * @return response entity  object holding response status and list of all testpackages
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getTestPackage")
	public ResponseEntity<Object> getTestPackage(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testPackageService.getTestPackage(userInfo);
		 
	}
	/**
	 * This method will is used to make a new entry to testpackage  table.
	 * @param inputMap map object holding params ( 
	 * 								testpackage [testpackage]  object holding details to be added in testpackage table,
	 * 								userinfo [UserInfo] holding logged in user details
	 *                              ) 
	 *                           Input:{
									    "testpackage": { "stestpackagename": "cm", "stestpackagesynonym": "cm" },
									    "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -   1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 152,"nmastersitecode": -1,"nmodulecode": 1,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",  "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}
									}
	 * @return ResponseEntity with string message as 'Already Exists' if the testpackage already exists/ 
	 * 			list of testpackages along with the newly added testpackage.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createTestPackage")
	public ResponseEntity<Object> createTestPackage(@RequestBody Map<String, Object> inputMap)throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final TestPackage Testpackage = objmapper.convertValue(inputMap.get("testpackage"), TestPackage.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testPackageService.createTestPackage(Testpackage,userInfo);
		
	}
	/**
	 * This method is used to delete an entry in testpackage table
	 * @param inputMap [Map] object with keys of testpackage entity and UserInfo object.
	 * 					Input:{
     						"testpackage": {"ntestpackagecode":1},
    						"userinfo":{
                						"activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,  "nformcode": 152,"nmastersitecode": -1, "nmodulecode": 1,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,  "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US","slanguagename": "English",
 										"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
  										"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "", "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the testpackage record is not available/ 
	 * 			string message as 'Record is used in....' when the testpackage is associated in transaction /
	 * 			list of all testpackages excluding the deleted record 
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteTestPackage")
	public ResponseEntity<Object> deleteTestPackage(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final TestPackage Testpackage = objmapper.convertValue(inputMap.get("testpackage"), TestPackage.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testPackageService.deleteTestPackage(Testpackage,userInfo);
		
	}
	/**
	 * This method is used to retrieve a specific testpackage record.
	 * @param inputMap  [Map] map object with "ntestpackagecode" and "userinfo" as keys for which the data is to be fetched
	 *                  Input:{ "ntestpackagecode": 1,
							     "userinfo":{
							                "activelanguagelist": ["en-US"],
							                "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							                "nformcode": 152,"nmastersitecode": -1,"nmodulecode": 1,
							                "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							                "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							                "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							                "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with testpackage object for the specified primary key / with string message as
	 * 						'Deleted' if the testpackage record is not available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveTestPackageById")
	public ResponseEntity<Object> getActiveTestPackageById(@RequestBody Map<String, Object> inputMap)throws Exception  {

		final ObjectMapper objmapper = new ObjectMapper();
		final int npackagecode = (Integer) inputMap.get("ntestpackagecode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testPackageService.getActiveTestPackageById(npackagecode, userInfo);
		
	}
	
	/**
	 * This method is used to update selected testpackage details.
	 * @param inputMap [map object holding params(
	 * 					testpackage [testpackage]  object holding details to be updated in testpackage table,
	 * 								userinfo [UserInfo] holding logged in user details) 
	 * 					Input:{
     						"testpackage": {"ntestpackagecode":1,"stestpackagename": "m","stestpackagesynonym": "m", 
 									 "sdescription": "m", "ndefaultstatus": 3      },
    						"userinfo":{
                						"activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,  "nformcode": 152,"nmastersitecode": -1, "nmodulecode": 1,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,  "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US","slanguagename": "English",
 										"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
  										"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "", "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}

	 * 	  		
	 * @return ResponseEntity with string message as 'Already Deleted' if the testpackage record is not available/ 
	 * 			list of all testpackages and along with the updated testpackage.	 
	 * @throws Exception exception
	 */	
	@PostMapping(value = "/updateTestPackage")
	public ResponseEntity<Object> updateTestPackage(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final TestPackage Testpackage = objmapper.convertValue(inputMap.get("testpackage"), TestPackage.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testPackageService.updateTestPackage(Testpackage,userInfo);
		
	}
}
