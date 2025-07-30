package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.basemaster.model.StorageLocation;
import com.agaramtech.qualis.basemaster.service.storagelocation.StorageLocationService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant service methods
 * to access the Storage Location Service methods.
 */
@RestController
@RequestMapping("/storagelocation")
public class StorageLocationController {

	private RequestContext requestContext;
	private final StorageLocationService storageLocationService;
	

	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param unitService UnitService
	 */
	public StorageLocationController(RequestContext requestContext, StorageLocationService storageLocationService) {
		super();
		this.requestContext = requestContext;
		this.storageLocationService = storageLocationService;
	}

	/**
	 * This method is used to retrieve list of available storage location(s). 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details 
	 * 							  and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 	
	 * 					Input : {"userinfo":{nmastersitecode": -1}}				
	 * @return response entity  object holding response status and list of all storage locations
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getStorageLocation")
	public ResponseEntity<Object> getStorageLocation(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return storageLocationService.getStorageLocation(userInfo.getNmastersitecode());
	}

	/**
	 * This method will is used to make a new entry to storagelocation  table.
	 * @param inputMap map object holding params ( 
	 * 								storagelocation [StorageLocation]  object holding details to be added in storagelocation table,
	 * 								userinfo [UserInfo] holding logged in user details
	 *                              ) 
	 *                           Input:{
									    "storagelocation": { "sstoragelocationname": "location1"},
									    "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -   1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 33,"nmastersitecode": -1,"nmodulecode": 1,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",  "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}
									}
	 * @return ResponseEntity with string message as 'Already Exists' if the unit already exists/ 
	 * 			list of units along with the newly added unit.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createStorageLocation")
	public ResponseEntity<Object> createStorageLocation(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final StorageLocation storageLocation = objmapper.convertValue(inputMap.get("storagelocation"),
				StorageLocation.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);

		return storageLocationService.createStorageLocation(storageLocation, userInfo);
	}

	/**
	 * This method is used to retrieve a specific storagelocation record.
	 * @param inputMap  [Map] map object with "nstoragelocationcode" and "userinfo" as keys for which the data is to be fetched
	 *                  Input:{ "nstoragelocationcode": 1,
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
	 * @return ResponseEntity with StorageLocation object for the specified primary key / with string message as
	 * 						'Deleted' if the storagelocation record is not available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveStorageLocationById")
	public ResponseEntity<Object> getActiveStorageLocationById(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		
		final int nstoragelocationcode = (Integer) inputMap.get("nstoragelocationcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);

		return storageLocationService.getActiveStorageLocationById(nstoragelocationcode, userInfo);
	}

	/**
	 * This method is used to update selected storagelocation details.
	 * @param inputMap [map object holding params(
	 * 					storagelocation [StorageLocation]  object holding details to be updated in storagelocation table,
	 * 								userinfo [UserInfo] holding logged in user details) 
	 * 					Input:{
     						"storagelocation": { "nstoragelocationcode":1, sstoragelocationname": "location2"},
    						"userinfo":{
                						"activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,  "nformcode": 33,"nmastersitecode": -1, "nmodulecode": 1,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,  "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US","slanguagename": "English",
 										"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
  										"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "", "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}

	 * 	  		
	 * @return ResponseEntity with string message as 'Already Deleted' if the storagelocation record is not available/ 
	 * 			list of all storagelocations and along with the updated unit.	 
	 * @throws Exception exception
	 */	
	@PostMapping(value = "/updateStorageLocation")
	public ResponseEntity<Object> updateStorageLocation(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final StorageLocation storagelocation = objmapper.convertValue(inputMap.get("storagelocation"),
				StorageLocation.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);

		return storageLocationService.updateStorageLocation(storagelocation, userInfo);
	}

	/**
	 * This method is used to delete an entry in storagelocation table
	 * @param inputMap [Map] object with keys of StorageLocation entity and UserInfo object.
	 * 					Input:{
     						"storagelocation": {"nstoragelocationcode":1},
    						"userinfo":{
                						"activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,  "nformcode": 33,"nmastersitecode": -1, "nmodulecode": 1,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,  "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US","slanguagename": "English",
 										"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
  										"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "", "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the storagelocation record is not available/ 
	 * 			string message as 'Record is used in....' when the storagelocation is associated in transaction /
	 * 			list of all storagelocations excluding the deleted record 
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteStorageLocation")
	public ResponseEntity<Object> deleteStorageLocation(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final StorageLocation storagelocation = objmapper.convertValue(inputMap.get("storagelocation"),
				StorageLocation.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		
		return storageLocationService.deleteStorageLocation(storagelocation, userInfo);
	}
}
