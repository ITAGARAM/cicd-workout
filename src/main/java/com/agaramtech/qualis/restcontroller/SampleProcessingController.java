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
import com.agaramtech.qualis.storagemanagement.service.sampleprocessing.SampleProcessingService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant service methods
 * to access the SampleProcessing Service methods.
 */
@RestController
@RequestMapping("/storagesampleprocessing")
public class SampleProcessingController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SampleProcessingController.class);
	
	private RequestContext requestContext;
	private final SampleProcessingService sampleProcessingService;

	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param sampleProcessingService SampleProcessingService
	 */
	public SampleProcessingController(RequestContext requestContext, SampleProcessingService sampleProcessingService) {
		super();
		this.requestContext = requestContext;
		this.sampleProcessingService = sampleProcessingService;
	}	
	/**
	 * This method is used to retrieve list of available sampleprocessing(s) for the logged in site. 
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
	 *  @return ResponseEntity object containing the response status and a map of key objects:  holding details of 
	 *  list of mapped sample types and its processes along with other details of barcode fields, selected project type,nBarcodeLength.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getSampleProcessing")
	public ResponseEntity<Object> getSampleProcessing(@RequestBody Map<String, Object> inputMap) throws Exception {
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
		LOGGER.info("getSampleProcessing called");
		return sampleProcessingService.getSampleProcessing(fromDate, toDate, currentUIDate, userInfo, nprojecttypecode);
	}

	/**
	 * This method is used to retrieve list of all active Barcode data for the  specified project type and logged in site.
	 * @param inputMap  [inputMap] map object with "nbarcodeLength","sampleprocesstypecode","spositionvalue","nprojecttypecode" and "userinfo" as keys for which the data is to be fetched
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
	 * 							{"nbarcodeLength":14},{"spositionvalue":"R200001"}, {"sampleprocesstypecode":11}, {"nprojecttypecode":1}},		
	 * @return ResponseEntity object containing the response status and a map of key objects:  holding details of list of mapped project types and logged in site with active barcode data
	 * @throws Exception.
	 */
	@PostMapping(value = "/getBarcodeConfigData")
	public ResponseEntity<Object> getBarcodeConfigData(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleProcessingService.getBarcodeConfigData(inputMap, userInfo);
	}

	/**
	 * This method is used to delete an entry in storagesampleprocessing table
	 * @param inputMap [Map] object with key of UserInfo object.
	 * 					Input:{
						"fromDate": "2025-05-13T00:00:00","nprojecttypecode":2,"nsampleprocessingcode":9,
						"sampleprocessing": {"dmodifieddate": null,"dprocessenddate": null,"dprocessstartdate": null,
						"jsondata": {"Participant ID": "00001","Quantity": 34,"SC": "Processing","SI": "SI","Unit": "gram","nbulkbarcodeconfigcode": 4},
						"nbulkbarcodeconfigcode": 4,"ncollectiontubetypecode": 0,"ngracetime": 0,"noffsetdprocessenddate": 0,"noffsetdprocessstartdate": 0,
						"nprocesstime": 0,"nprojecttypecode": 2,"nsamplecollectiontypecode": 0,"nsampleprocessingcode": 9,
						"nsampleprocesstypecode": 0,"nsitecode": 0,"nstatus": 0,"ntzprocessenddate": 0,"ntzprocessstartdate": 0,
						"Participant ID": "00001","Quantity": 34,"sbarcodeid": "R200001","SC": "Processing","scomments": "-",
						"sdeviationcomments": "","sgraceduration": "7 Minute(s)","SI": "SI","sprocessduration": "5 Minute(s)",
						"sprocessenddate": "03/06/2025 17:13:00","sprocessstartdate": "03/06/2025 17:08:29","sprocesstypename": "Incubation",
						"sproductname": "Blood","sprojecttypename": "COVID -19 Consortia","stubename": "Plain","Unit": "gram"}
						"sbarcodeid": "R200001","toDate": "2025-06-12T23:59:59"},
    						"userinfo":{
                						"activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,  "nformcode": 33,"nmastersitecode": -1, "nmodulecode": 1,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,  "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US","slanguagename": "English",
 										"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
  										"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "", "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the storagesampleprocessing record is not available/ 
	 * 			list of all storagesampleprocessing excluding the deleted record 
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteSampleProcessing")
	public ResponseEntity<Object> deleteSampleProcessing(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return sampleProcessingService.deleteSampleProcessing(inputMap, userInfo);

	}

	/**
	 * This method is used to retrieve list of all active Collection Tube type data for the specified project type and site.
	 * @param inputMap  [inputMap] map object with "nprojecttypecode" and "userinfo" as keys for which the data is to be fetched
	 * 					Input : {"userinfo":{
							                "activelanguagelist": ["en-US"],
							                "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							                "nformcode": 33,"nmastersitecode": -1,"nmodulecode": 1,
							                "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							                "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							                "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							                "ssitedatetime": "dd/MM/yyyy HH:mm:ss"},"nprojecttypecode":1}	
	 * @return response entity object holding response status and list of all active Collection Tube type data for the specified project type and site. 
	 * @throws Exception that are thrown from this DAO layer
	 */
	@PostMapping(value = "/getCollectionTubeType")
	public ResponseEntity<Object> getCollectionTubeType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return sampleProcessingService.getCollectionTubeType(inputMap, userInfo);

		
	}

	/**
	 * This method is used to retrieve list of all active Sample Type data for the specified project type and site.
	 * @param inputMap  [inputMap] map object with "nprojecttypecode" and "userinfo" as keys for which the data is to be fetched
	 * 					Input : {"userinfo":{
							                "activelanguagelist": ["en-US"],
							                "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							                "nformcode": 33,"nmastersitecode": -1,"nmodulecode": 1,
							                "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							                "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							                "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							                "ssitedatetime": "dd/MM/yyyy HH:mm:ss"},"nprojecttypecode":1}	
	 * @return response entity object holding response status and list of all active Sample Type data for the specified project type and site. 
	 * @throws Exception that are thrown from this DAO layer
	 */
	@PostMapping(value = "/getSampleType")
	public ResponseEntity<Object> getSampleType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleProcessingService.getSampleType(inputMap, userInfo);

	}

	/**
	 * This method is used to retrieve list of all active Sample Process Type data for the specified project type and site.
	 * @param inputMap  [inputMap] map object with "nprojecttypecode" and "userinfo" as keys for which the data is to be fetched
	 * 					Input : {"userinfo":{
							                "activelanguagelist": ["en-US"],
							                "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							                "nformcode": 33,"nmastersitecode": -1,"nmodulecode": 1,
							                "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							                "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							                "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							                "ssitedatetime": "dd/MM/yyyy HH:mm:ss"},"nprojecttypecode":1}	
	 * @return response entity object holding response status and list of all active Sample Process Type data for the specified project type and site. 
	 * @throws Exception that are thrown from this DAO layer
	 */
	@PostMapping(value = "/getSampleProcessType")
	public ResponseEntity<Object> getSampleProcessType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleProcessingService.getSampleProcessType(inputMap, userInfo);

	}	
	/**
	 * This method is used to retrieve list of all active Process Duration data for the specified project type and Sample Type and Collection tube type and site.
	 * @param inputMap  [inputMap] map object with "ncollectiontubetypecode","nsamplecollectiontypecode","nprocesstypecode","nprojecttypecode" and  "userinfo" as keys for which the data is to be fetched
	 * 					Input : {"userinfo":{
							                "activelanguagelist": ["en-US"],
							                "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							                "nformcode": 33,"nmastersitecode": -1,"nmodulecode": 1,
							                "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							                "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							                "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							                "ssitedatetime": "dd/MM/yyyy HH:mm:ss"},"ncollectiontubetypecode":1,"nsamplecollectiontypecode":1,"nprocesstypecode":1, "nprojecttypecode":1}	
	 * @return response entity object holding response status and list of all active Process Duration data for the specified project type and Sample Type and Collection tube type and site. 
	 * @throws Exception that are thrown from this DAO layer
	 */
	@PostMapping(value = "/getProcessduration")
	public ResponseEntity<Object> getProcessduration(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleProcessingService.getProcessduration(inputMap, userInfo);

	}	
	/**
	 * This method is used to add a new entry to storagesampleprocessing  table.
	 * @param inputMap  [inputMap] map object with "fromDate","toDate","currentdate","nprojecttypecode","temporarystorage" and "userinfo" as keys for which the data is to be fetched
	 * 								temporarystorage [TemporaryStorage]  object holding details to be added in temporarystorage table,
	 * 								userinfo [UserInfo] holding logged in user details
	 *                           Input:{
									    {"fromDate": "2025-05-13T00:00:00","isSingleProcesstype":True,"listOfnsampleprocesstypecode":11,"nprocesstypelength":1,
										"nprojecttypecode":2,"nsampleprocesstypecode":11,
										"sampleprocessing": {"dprocessenddate": "","dprocessstartdate": "",
										"jsondata":{"SC":"Processing","SI":"SI","Participant ID":"00001","Quantity":34,"Unit":"gram","nbulkbarcodeconfigcode":4},
										"ncollectiontubetypecode":9,"noffsetdprocessenddate": -1,"noffsetdprocessstartdate": -1,
										"nproductcode":1,"nprojecttypecode": 2,"nsamplecollectiontypecode": -1,
										"nsampleprocesstypecode": 11,"ntzprocessenddate": -1,"ntzprocessstartdate": -1,
										"sbarcodeid": "R200001","scomments": "erere","sdeviationcomments": ""},
										"toDate": "2025-06-12T23:59:59"},
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
	 * @return ResponseEntity with string message as 'The Barcode ID scanned already' if the barcode id already scanned / 
	 * 			list of storagesampleprocessing along with the newly added storagesampleprocessing .
	 * @throws Exception exception
	 */	
	@PostMapping(value = "/createSampleProcessing")
	public ResponseEntity<Object> createSampleProcessing(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleProcessingService.createSampleProcessing(inputMap, userInfo);
	}
		
	/**
	 * This method is used to update entry in storagesampleprocessing details.
	 * @param inputMap  [inputMap] map object with "fromDate","toDate","currentdate","nprojecttypecode","temporarystorage" and "userinfo" as keys for which the data is to be fetched
	 * 								temporarystorage [TemporaryStorage]  object holding details to be added in temporarystorage table,
	 * 								userinfo [UserInfo] holding logged in user details
	 *                           Input:{
									    "fromDate": "2025-05-13T00:00:00","nprojecttypecode":2,
										"sampleprocessing": {"dprocessenddate": "","dprocessstartdate": "",
										"noffsetdprocessenddate": -1,"noffsetdprocessstartdate": -1,
										"nprojecttypecode": 2,"nsamplecollectiontypecode": -1,"nsampleprocessingcode": 9,
										"nsampleprocesstypecode": 11,"ntzprocessenddate": -1,"ntzprocessstartdate": -1,
										"sbarcodeid": "R200001","scomments": "erere",
										"sdeviationcomments": ""},"toDate": "2025-06-12T23:59:59"},
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
	 * @return ResponseEntity with storagesampleprocessing object for the specified primary key / with string message as
	 * 						'Already Deleted' if the storagesampleprocessing record is not available
	 * @throws Exception exception
	 */		
	@PostMapping(value = "/updateSampleProcessing")
	public ResponseEntity<Object> updateSampleProcessing(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleProcessingService.updateSampleProcessing(inputMap, userInfo);
	}
	
	/**
	 * This method is used to retrieve a specific storagesampleprocessing record.
	 * @param inputMap  [Map] map object with "nprojecttypecode","nsampleprocessingcode" and "userinfo" as keys for which the data is to be fetched
	 *                  Input:{ "nprojecttypecode": 1,"nsampleprocessingcode":9,
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
	 * 						'Already Deleted' if the storagesampleprocessing record is not available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveSampleProcessingById")
	public ResponseEntity<Object> getActiveSampleProcessingById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		objMapper.registerModule(new JavaTimeModule());
		requestContext.setUserInfo(userInfo);
		return sampleProcessingService.getActiveSampleProcessingById(inputMap, userInfo);
	}	
}