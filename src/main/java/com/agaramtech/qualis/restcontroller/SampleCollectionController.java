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
import com.agaramtech.qualis.storagemanagement.service.samplecollection.SampleCollectionService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant service methods
 * to access the Sample Collection Service methods.
 */
@RestController
@RequestMapping("/storagesamplecollection")
public class SampleCollectionController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SampleCollectionController.class);
	
	private final SampleCollectionService sampleCollectionService;
	private RequestContext requestContext;

	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param sampleCollectionService SampleCollectionService
	 */
	public SampleCollectionController(SampleCollectionService sampleCollectionService, RequestContext requestContext) {
		super();
		this.sampleCollectionService = sampleCollectionService;
		this.requestContext = requestContext;
	}

	/**
	 * This method is used to retrieve list of available sample collection(s). 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 	
	 * 					Input : {"userinfo":{nmastersitecode": -1}}				
	 * @return response entity object holding response status and list of all sample collections
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getSampleCollection")
	public ResponseEntity<Object> getSampleCollection(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		String fromDate = "";
		String toDate = "";
		if (inputMap.get("fromDate") != null) {
			fromDate = (String) inputMap.get("fromDate");
		}
		if (inputMap.get("toDate") != null) {
			toDate = (String) inputMap.get("toDate");
		}
		final String currentUIDate = (String) inputMap.get("currentdate");
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		int nprojecttypecode = -1;
		if (inputMap.containsKey("nprojecttypecode")) {
			nprojecttypecode = (int) inputMap.get("nprojecttypecode");
		}
		LOGGER.info("getSampleCollection called");
		return sampleCollectionService.getSampleCollection(fromDate, toDate, currentUIDate, userInfo, nprojecttypecode);
	}

	/**
	 * This method is used to retrieve list of all active Barcode data for the
	 * specified project type and site.
	 * 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 	
	 * 					Input : {"nprojecttypecode": 1, "userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all active
	 *         Barcode data specified project type and site.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getBarcodeConfigDataCollection")
	public ResponseEntity<Object> getBarcodeConfigData(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();

		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleCollectionService.getBarcodeConfigData(inputMap, userInfo);
	}

	/**
	 * This method will is used to make a new entry to storagesamplecollection table. Need
	 * to check for duplicate entry of sbarcodeid for the specified site.
	 * 
	 * @param inputMap map object holding params ( 
	 * 								storagesamplecollection [StorageSampleCollection] object holding details to be added in storagesamplecollection table,
	 * 								userinfo [UserInfo] holding logged in user details
	 *                              ) 
	 *                           Input:{
									    "storagesamplecollection":{"nunitcode":1,"nsampleqty":1,"jsondata":"{\"AIMS\":\"ID Name\",\"Patient ID\":\"0001\",\"Storage\":\"Storage\",\"Sample Type\":\"-\",\"3 Months\":\"-\",\"EDTA\":\"EDTA\",\"Tube Number\":\"01\",\"Initial\":\"Intial\",\"nbulkbarcodeconfigcode\":5}",
									    	"nprojecttypecode":2,"sbarcodeid":"A1000110110101","dcollectiondate":"2025-06-03T15:19:35Z","scomments":"","ntzcollectiondatetime":-1,"noffsetdcollectiondatetime":-1},
									    "fromDate": "2025-05-04T00:00:00",
  										"toDate": "2025-06-03T23:59:59",
  										"nprojecttypecode": 2,
									    "userinfo":{
							                "activelanguagelist": ["en-US"],
							                "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							                "nformcode": 238,"nmastersitecode": -1,"nmodulecode": 71,
							                "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							                "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							                "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							                "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}
									}
	 * @return ResponseEntity with string message as 'Already Exists' if the storagesamplecollection already exists/ 
	 * 			list of storagesamplecollections along with the newly added storagesamplecollection.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createSampleCollection")
	public ResponseEntity<Object> createSampleCollection(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		objMapper.registerModule(new JavaTimeModule());
		requestContext.setUserInfo(userInfo);

		return sampleCollectionService.createSampleCollection(inputMap, userInfo);
	}

	/**
	 * This method is used to retrieve a specific storagesamplecollection record.
	 * @param inputMap [Map] map object with "nsamplecollectioncode", "nprojecttypecode" and "userinfo" as keys for which the data is to be fetched
	 *                  Input:{ "nsamplecollectioncode": 1,
	 *                  		"nprojecttypecode":2,
							    "userinfo":{
							                "activelanguagelist": ["en-US"],
							                "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							                "nformcode": 238,"nmastersitecode": -1,"nmodulecode": 71,
							                "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							                "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							                "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							                "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with SampleCollection object for the specified primary key / with string message as
	 * 						'Deleted' if the storagesamplecollection record is not available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveSampleCollectionById")
	public ResponseEntity<Object> getActiveSampleCollectionById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		objMapper.registerModule(new JavaTimeModule());
		requestContext.setUserInfo(userInfo);
		return sampleCollectionService.getActiveSampleCollectionById(inputMap, userInfo);
	}

	/**
	 * This method is used to update entry in storagesamplecollection table. Need to
	 * validate that the storagesamplecollection object to be updated is active
	 * before updating details in database. Need to check for duplicate entry of
	 * sbarcodeid before saving into database.
	 * 
	 * @param inputMap [map object holding params(
	 * 					storagesamplecollection [StorageSampleCollection] object holding details to be updated in storagesamplecollection table,
	 * 								userinfo [UserInfo] holding logged in user details) 
	 * 					Input:{
     						"storagesamplecollection":{"nunitcode":1,"nsampleqty":1,"jsondata":"{\"AIMS\":\"ID Name\",\"Patient ID\":\"0001\",\"Storage\":\"Storage\",\"Sample Type\":\"-\",\"3 Months\":\"-\",\"EDTA\":\"EDTA\",\"Tube Number\":\"01\",\"Initial\":\"Intial\",\"nbulkbarcodeconfigcode\":5}",
									    	"nprojecttypecode":2,"sbarcodeid":"A1000110110101","dcollectiondate":"2025-06-03T15:19:35Z","scomments":"","ntzcollectiondatetime":-1,"noffsetdcollectiondatetime":-1},
							"fromDate": "2025-05-04T00:00:00",
  							"toDate": "2025-06-03T23:59:59",
  							"nprojecttypecode": 2,
							"userinfo":{
								"activelanguagelist": ["en-US"],
							    "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							    "nformcode": 238,"nmastersitecode": -1,"nmodulecode": 71,
							    "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							    "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							    "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							    "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							    "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							    "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}
	 * 	  					}
	 * @return ResponseEntity with string message as 'Already Deleted' if the storageSampleCollection record is not available/ 
	 * 			list of all storagesamplecollections and along with the updated storagesamplecollection.	 
	 * @throws Exception exception
	 */	
	@PostMapping(value = "/updateSampleCollection")
	public ResponseEntity<Object> updateSampleCollection(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleCollectionService.updateSampleCollection(inputMap, userInfo);
	}

	/**
	 * This method is used to delete an entry in storagesamplecollection table.
	 * 
	 * @param inputMap [Map] object with keys of SampleCollection entity and UserInfo object.
	 *                    Input:{ "nsamplecollectioncode": 1,
	 *                  		"nprojecttypecode":2,
							    "userinfo":{
							                "activelanguagelist": ["en-US"],
							                "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							                "nformcode": 238,"nmastersitecode": -1,"nmodulecode": 71,
							                "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							                "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							                "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							                "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already Exists' if the storagesamplecollection already exists/ 
	 * 			list of storagesamplecollections along with the newly added storagesamplecollection.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteSampleCollection")
	public ResponseEntity<Object> deleteSampleCollection(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleCollectionService.deleteSampleCollection(inputMap, userInfo);

	}
}