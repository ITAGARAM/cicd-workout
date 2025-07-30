package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.barcode.model.CollectionTubeType;
import com.agaramtech.qualis.barcode.service.collectiontubetype.CollectionTubeTypeService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the collectiontubetype Service methods
 * 
 * @author ATE236
 * @version 10.0.0.2
 */
@RestController
@RequestMapping("/collectiontubetype")
public class CollectionTubeTypeController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CollectionTubeTypeController.class);
	
	private final RequestContext requestContext;
	private final CollectionTubeTypeService collectionTubeTypeService;

	public CollectionTubeTypeController(RequestContext requestContext,
			CollectionTubeTypeService collectionTubeTypeService) {
		super();
		this.requestContext = requestContext;
		this.collectionTubeTypeService = collectionTubeTypeService;
	}

	/**
	 * This Method is used to get the over all collectiontubetype with respect to
	 * site
	 * 
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of collectiontubetype with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getCollectionTubeType")
	public ResponseEntity<Object> getCollectionTubeType(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("getCollectionTubeType called");
		requestContext.setUserInfo(userInfo);
		return collectionTubeTypeService.getCollectionTubeType(userInfo);
	}

	/**
	 * This method is used to retrieve a specific collectiontubetype  record.
	 * @param inputMap  [Map] map object with "ncollectiontubetypecode" and "userinfo" as keys for which the data is to be fetched
	 *                  Input:{ "ncollectiontubetypecode": 1,
							     "userinfo":{
							                "activelanguagelist": ["en-US"],
							                "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							                "nformcode": 187,"nmastersitecode": -1,"nmodulecode": 76,
							                "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							                "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							                "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							                "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with CollectionTubeType object for the specified primary key / with string message as
	 * 						'Deleted' if the collectionTubeType record is not available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveCollectionTubeTypeById")
	public ResponseEntity<Object> getActiveCollectionTubeTypeById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int ncollectiontubetypecode = (Integer) inputMap.get("ncollectiontubetypecode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return collectionTubeTypeService.getActiveCollectionTubeTypeById(ncollectiontubetypecode, userInfo);
	}

	/**
	 * This method will is used to make a new entry to collectiontubetype table.
	 * @param inputMap map object holding params ( 
	 * 								collectionTubeType [CollectionTubeType]  object holding details to be added in collectiontubetype table,
	 * 								userinfo [UserInfo] holding logged in user details
	 *                              ) 
	 *                           Input:{
									    "collectiontubetype": { "nprojecttypecode": "PTB", "stubename": "EDTA", "ncode": 1 },
									    "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -   1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 187,"nmastersitecode": -1,"nmodulecode": 76,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",  "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}
									}
	 * @return ResponseEntity with string message as 'Already Exists' if the collectionTubeType already exists/ 
	 * 			list of collectionTubeTypes along with the newly added collectionTubeType.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createCollectionTubeType")
	public ResponseEntity<Object> createCollectionTubeType(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final CollectionTubeType collectiontubetype = objMapper.convertValue(inputMap.get("collectiontubetype"),
				CollectionTubeType.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return collectionTubeTypeService.createCollectionTubeType(collectiontubetype, userInfo);
	}

	/**
	 * This method is used to update selected collectionTubeType details.
	 * @param inputMap [map object holding params(
	 * 					collectionTubeType [CollectionTubeTYpe]  object holding details to be updated in collectiontubetype table,
	 * 					userinfo [UserInfo] holding logged in user details) 
	 * 					Input:{
     						"collectiontubetype": {"nprojecttypecode":"PTB","stubename": "EDTA","ncode": 1  },
    						"userinfo":{
                						"activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,  "nformcode": 187,"nmastersitecode": -1, "nmodulecode": 76,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,  "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US","slanguagename": "English",
 										"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
  										"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "", "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}

	 * 	  		
	 * @return ResponseEntity with string message as 'Already Deleted' if the collectionTubeType record is not available/ 
	 * 			list of all collectionTubeTypes and along with the updated collectiontubetype.	 
	 * @throws Exception exception
	 */	
	@PostMapping(value = "/updateCollectionTubeType")
	public ResponseEntity<Object> updateCollectionTubeType(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final CollectionTubeType collectiontubetype = objMapper.convertValue(inputMap.get("collectiontubetype"),
				CollectionTubeType.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return collectionTubeTypeService.updateCollectionTubeType(collectiontubetype, userInfo);
	}

	/**
	 * This method is used to delete an entry in collectiontubetype table
	 * @param inputMap [Map] object with keys of CollectionTubeType entity and UserInfo object.
	 * 					Input:{
     						"collectiontubetype": {"ncollectiontubetypecode":1},
    						"userinfo":{
                						"activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,  "nformcode": 187,"nmastersitecode": -1, "nmodulecode": 76,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,  "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US","slanguagename": "English",
 										"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
  										"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "", "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the collectionTubeType record is not available/ 
	 * 			string message as 'Record is used in....' when the collectionTubeType is associated in transaction /
	 * 			list of all collectiontubetypes excluding the deleted record 
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteCollectionTubeType")
	public ResponseEntity<Object> deleteCollectionTubeType(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final CollectionTubeType collectiontubetype = objMapper.convertValue(inputMap.get("collectiontubetype"),
				CollectionTubeType.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return collectionTubeTypeService.deleteCollectionTubeType(collectiontubetype, userInfo);
	}

}
