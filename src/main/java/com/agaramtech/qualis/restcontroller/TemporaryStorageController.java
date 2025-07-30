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
import com.agaramtech.qualis.storagemanagement.service.temporarystorage.TemporaryStorageService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the Temporary Storage Service methods
 */
@RestController
@RequestMapping("/temporarystorage")
public class TemporaryStorageController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TemporaryStorageController.class);
	private final TemporaryStorageService temporaryStorageService;
	private RequestContext requestContext;

	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param temporaryStorageService TemporaryStorageService
	 * @param requestContext RequestContext to hold the request
	 */
	public TemporaryStorageController(TemporaryStorageService temporaryStorageService, RequestContext requestContext) {
		super();
		this.temporaryStorageService = temporaryStorageService;
		this.requestContext = requestContext;
	}

	/**
	 * This method is used to retrieve list of all active temporarystorage for the logged in site.
	 * @param inputMap  [inputMap] map object with "fromDate","toDate","currentdate","nprojecttypecode" and "userinfo" as keys for which the data is to be fetched
	 * 					Input : {"userinfo":{
							                "activelanguagelist": ["en-US"],
							                "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							                "nformcode": 33,"nmastersitecode": -1,"nmodulecode": 1,
							                "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							                "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							                "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							                "ssitedatetime": "dd/MM/yyyy HH:mm:ss"},
	 * 							{"fromDate":"2025-05-26T00:00:00.000Z"},
	 * 							{"toDate":"2025-06-02T23:59:59.599Z"},
	 * 							{"currentdate":"2025-06-02T11:53:38.261Z"},
	 * 							{"nprojecttypecode":1}},		
	 *  @return ResponseEntity object containing the response status and a map of key objects:  holding details of list of mapped project types and barcode id with active temporary storage data
	 *  and its processes along with other details of barcode fields, selected project type,nBarcodeLength.
	 * @throws Exception 
	 */
	
	@PostMapping(value = "/getTemporaryStorage")
	public ResponseEntity<Object> getTemporaryStorage(@RequestBody Map<String, Object> inputMap) throws Exception {
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
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		int nprojecttypecode = -1;
		if (inputMap.containsKey("nprojecttypecode")) {
			nprojecttypecode = (int) inputMap.get("nprojecttypecode");
		}
		return temporaryStorageService.getTemporaryStorage(fromDate, toDate, currentUIDate, userInfo, nprojecttypecode);
	}

	/**
	 * This method is used to retrieve list of all active Barcode data for the  specified project type and logged in site.
	 * @param inputMap  [inputMap] map object with "nbarcodeLength","toDate","spositionvalue","nprojecttypecode" and "userinfo" as keys for which the data is to be fetched
	 * 					Input : {"userinfo":{
							                "activelanguagelist": ["en-US"],
							                "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							                "nformcode": 33,"nmastersitecode": -1,"nmodulecode": 1,
							                "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							                "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							                "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							                "ssitedatetime": "dd/MM/yyyy HH:mm:ss"},
	 * 							{"nbarcodeLength":14},
	 * 							{"spositionvalue":"R200001"},
	 * 							{"nprojecttypecode":1},	{"toDate":"2025-06-02T23:59:59.599Z"},},		
	 * @return ResponseEntity object containing the response status and a map of key objects:  holding details of list of mapped project types and logged in site with active barcode data
	 * @throws Exception.
	 */
	@PostMapping(value = "/getBarcodeConfigData")
	public ResponseEntity<Object> getBarcodeConfigData(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();

		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return temporaryStorageService.getBarcodeConfigData(inputMap, userInfo);
	}

	/**
	 * This method will is used to make a new entry to temporarystorage  table.
	 * @param inputMap  [inputMap] map object with "fromDate","toDate","currentdate","nprojecttypecode","temporarystorage" and "userinfo" as keys for which the data is to be fetched
	 * 								temporarystorage [TemporaryStorage]  object holding details to be added in temporarystorage table,
	 * 								userinfo [UserInfo] holding logged in user details
	 *                           Input:{
									    "temporarystorage": { "dstoragedatetime":"2025-06-11T11:32:10Z",
															  "jsondata":"{"SC":"Processing","SI":"SI","Participant ID":"00001","nbulkbarcodeconfigcode":4,"Process Duration":"-"}",
															  "noffsetstoragedatetime":"-1", "nprojecttypecode":"2", "nsampleqty":"-1", "ntzstoragedatetime":"-1",
															  "nunitcode":"-1","sbarcodeid":"R100001", "scomments":"" },
										"userinfo":{
							                "activelanguagelist": ["en-US"],
							                "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							                "nformcode": 33,"nmastersitecode": -1,"nmodulecode": 1,
							                "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							                "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							                "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							                "ssitedatetime": "dd/MM/yyyy HH:mm:ss"},
	 * 									{"fromDate":"2025-05-26T00:00:00.000Z"},
	 * 	 								{"toDate":"2025-06-02T23:59:59.599Z"},
	 * 									{"nprojecttypecode":1}},		
	 * @return ResponseEntity with string message as 'The Barcode ID scanned already' if the barcode id already scanned / 
	 * 			list of temporarystorage along with the newly added temporarystorage .
	 * @throws Exception exception
	 */
	
	@PostMapping(value = "/createTemporaryStorage")
	public ResponseEntity<Object> createTemporaryStorage(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		objMapper.registerModule(new JavaTimeModule());
		requestContext.setUserInfo(userInfo);

		return temporaryStorageService.createTemporaryStorage(inputMap, userInfo);
	}

	/**
	 * This method is used to retrieve a specific temporarystorage record.
	 * @param inputMap  [Map] map object with "nprojecttypecode","ntemporarystoragecode" and "userinfo" as keys for which the data is to be fetched
	 *                  Input:{ "nprojecttypecode": 1,"ntemporarystoragecode":9,
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
	 * @return ResponseEntity with Temporary Storage object for the specified primary key / with string message as
	 * 						'Already Deleted' if the temporarystorage record is not available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveTemporaryStorageById")
	public ResponseEntity<Object> getActiveTemporaryStorageById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		objMapper.registerModule(new JavaTimeModule());
		requestContext.setUserInfo(userInfo);
		return temporaryStorageService.getActiveTemporaryStorageById(inputMap, userInfo);
	}
	
	/**
	 * This method is used to update selected temporarystorage details.
	 * @param inputMap  [inputMap] map object with "fromDate","toDate","currentdate","nprojecttypecode","temporarystorage" and "userinfo" as keys for which the data is to be fetched
	 * 								temporarystorage [TemporaryStorage]  object holding details to be added in temporarystorage table,
	 * 								userinfo [UserInfo] holding logged in user details
	 *                           Input:{
									    "temporarystorage": { "dstoragedatetime":"2025-06-11T11:32:10Z",
															  "jsondata":"{"SC":"Processing","SI":"SI","Participant ID":"00001","nbulkbarcodeconfigcode":4,"Process Duration":"-"}",
															  "noffsetstoragedatetime":"-1", "nprojecttypecode":"2", "nsampleqty":"-1", "ntzstoragedatetime":"-1",
															  "nunitcode":"-1","sbarcodeid":"R100001", "scomments":"good" },
										"userinfo":{
							                "activelanguagelist": ["en-US"],
							                "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							                "nformcode": 33,"nmastersitecode": -1,"nmodulecode": 1,
							                "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							                "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							                "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							                "ssitedatetime": "dd/MM/yyyy HH:mm:ss"},
	 * 									{"fromDate":"2025-05-26T00:00:00.000Z"},
	 * 	 								{"toDate":"2025-06-02T23:59:59.599Z"},
	 * 									{"nprojecttypecode":1}},		

	 * 	  		
	 * @return ResponseEntity with Temporary Storage object for the specified primary key / with string message as
	 * 						'Already Deleted' if the temporarystorage record is not available
	 * @throws Exception exception
	 */		
	@PostMapping(value = "/updateTemporaryStorage")
	public ResponseEntity<Object> updateTemporaryStorage(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return temporaryStorageService.updateTemporaryStorage(inputMap, userInfo);
	}

	
	
	/**
	 * This method is used to delete an entry in temporarystorage table
	 * @param inputMap [Map] object with keys of fromDate,toDate,nprojecttypecode,ntemporarystoragecode,temporarystorage entity and UserInfo object.

	 * 					Input:{
     						"temporarystorage": {"dmodifieddate":(null),
												dstoragedatetime":(null),
												"jsondata":{"nbulkbarcodeconfigcode":4,"Participant ID":"00001","Process Duration":"-",
															"SC":"Collection","SI":"SI"}
												"nbulkbarcodeconfigcode":4,"noffsetstoragedatetime":0,"nprojecttypecode":2,"nsitecode":0,
												"nstatus":0,"ntemporarystoragecode":10,"ntzstoragedatetime":0,
												"Participant ID":"00001","Process Duration":"-","sbarcodeid":"R100001",
												"SC":"Collection","scomments":"wwerewr","SI":"SI","sprocessduration":(null),"sprojecttypename":(null),
												"squantity":(null),	"sstoragedatetime": "11/06/2025 12:14:59","sunitname":(null)},
	 *  					{"fromDate":"2025-05-26T00:00:00.000Z"},
	 *  	 				{"toDate":"2025-06-02T23:59:59.599Z"},
	 * 						{"nprojecttypecode":1},
	 * 	  					{"ntemporarystoragecode":10},
    						"userinfo":{
                						"activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,  "nformcode": 33,"nmastersitecode": -1, "nmodulecode": 1,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,  "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US","slanguagename": "English",
 										"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
  										"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "", "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the temporarystorage record is not available/ 
	 * 			string message as 'Record is used in....' when the temporarystorage is associated in transaction /
	 * 			list of all temporarystorage excluding the deleted record 
	 * @throws Exception exception
	 */
	
	@PostMapping(value = "/deleteTemporaryStorage")
	public ResponseEntity<Object> deleteTemporaryStorage(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return temporaryStorageService.deleteTemporaryStorage(inputMap, userInfo);

	}
}