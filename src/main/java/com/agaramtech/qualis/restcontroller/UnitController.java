package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.basemaster.model.Unit;
import com.agaramtech.qualis.basemaster.service.unit.UnitService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant service methods
 * to access the Unit Service methods.
 */
@RestController
@RequestMapping("/unit")
//@AllArgsConstructor
public class UnitController {

	private static final Logger LOGGER = LoggerFactory.getLogger(UnitController.class);

	private RequestContext requestContext;
	private final UnitService unitService;
	
//	@Value("${MY_DATA}")
//	private String myData;
	

	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param unitService UnitService
	 */
	public UnitController(RequestContext requestContext, UnitService unitService) {
		super();
		this.requestContext = requestContext;
		this.unitService = unitService;
	}

	/**
	 * This method is used to retrieve list of available unit(s). 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 	
	 * 					Input : {"userinfo":{nmastersitecode": -1}}				
	 * @return response entity  object holding response status and list of all units
	 * @throws Exception exception
	 */
	//@RequestMapping(value = "/getUnit", method = RequestMethod.POST)
	@PostMapping(value = "/getUnit")
	public ResponseEntity<Object> getUnit(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		
		//LOGGER.info("myData:"+myData);
		return unitService.getUnit(userInfo);

	}

	/**
	 * This method will is used to make a new entry to unit  table.
	 * @param inputMap map object holding params ( 
	 * 								unit [Unit]  object holding details to be added in unit table,
	 * 								userinfo [UserInfo] holding logged in user details
	 *                              ) 
	 *                           Input:{
									    "unit": { "sunitname": "cm", "sunitsynonym": "cm" },
									    "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -   1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 33,"nmastersitecode": -1,"nmodulecode": 1,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",  "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}
									}
	 * @return ResponseEntity with string message as 'Already Exists' if the unit already exists/ 
	 * 			list of units along with the newly added unit.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createUnit")
	public ResponseEntity<Object> createUnit(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		final ObjectMapper objMapper = new ObjectMapper();
		final Unit objUnit = objMapper.convertValue(inputMap.get("unit"), new TypeReference<Unit>() {});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), 
					new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return unitService.createUnit(objUnit, userInfo);
	}

	/**
	 * This method is used to update selected unit details.
	 * @param inputMap [map object holding params(
	 * 					unit [Unit]  object holding details to be updated in unit table,
	 * 								userinfo [UserInfo] holding logged in user details) 
	 * 					Input:{
     						"unit": {"nunitcode":1,"sunitname": "m","sunitsynonym": "m", 
 									 "sdescription": "m", "ndefaultstatus": 3      },
    						"userinfo":{
                						"activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,  "nformcode": 33,"nmastersitecode": -1, "nmodulecode": 1,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,  "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US","slanguagename": "English",
 										"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
  										"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "", "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}

	 * 	  		
	 * @return ResponseEntity with string message as 'Already Deleted' if the unit record is not available/ 
	 * 			list of all units and along with the updated unit.	 
	 * @throws Exception exception
	 */	
	@PostMapping(value = "/updateUnit")
	public ResponseEntity<Object> updateUnit(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		
		final Unit objUnit = objMapper.convertValue(inputMap.get("unit"), new TypeReference<Unit>() {});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), 
				new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return unitService.updateUnit(objUnit, userInfo);
	}


	/**
	 * This method is used to delete an entry in Unit table
	 * @param inputMap [Map] object with keys of Unit entity and UserInfo object.
	 * 					Input:{
     						"unit": {"nunitcode":1},
    						"userinfo":{
                						"activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,  "nformcode": 33,"nmastersitecode": -1, "nmodulecode": 1,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,  "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US","slanguagename": "English",
 										"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
  										"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "", "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the unit record is not available/ 
	 * 			string message as 'Record is used in....' when the unit is associated in transaction /
	 * 			list of all units excluding the deleted record 
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteUnit")
	public ResponseEntity<Object> deleteUnit(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		
		final Unit objUnit = objMapper.convertValue(inputMap.get("unit"), new TypeReference<Unit>() {});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), 
				new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		
		return unitService.deleteUnit(objUnit, userInfo);
	}

	/**
	 * This method is used to retrieve a specific unit record.
	 * @param inputMap  [Map] map object with "nunitcode" and "userinfo" as keys for which the data is to be fetched
	 *                  Input:{ "nunitcode": 1,
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
	 * @return ResponseEntity with Unit object for the specified primary key / with string message as
	 * 						'Deleted' if the unit record is not available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveUnitById")
	public ResponseEntity<Object> getActiveUnitById(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		final ObjectMapper objMapper = new ObjectMapper();
		final int nunitCode = (int) inputMap.get("nunitcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), 
				new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		
		return unitService.getActiveUnitById(nunitCode, userInfo);
	}
}
