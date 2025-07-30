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
import com.agaramtech.qualis.storagemanagement.service.samplestoragemapping.SampleStorageMappingService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


@RestController
@RequestMapping("/samplestoragemapping")
public class SampleStorageMappingController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SampleStorageMappingController.class);
	
	private final SampleStorageMappingService samplestoragemappingService;
	private RequestContext requestContext;

	public SampleStorageMappingController(SampleStorageMappingService samplestoragemappingService,
			RequestContext requestContext) {
		super();
		this.samplestoragemappingService = samplestoragemappingService;
		this.requestContext = requestContext;
	}
	
	/**
	 * This method is used to retrieve list of available SampleStorageMapping(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}} If the
	 *                 nstoragecategorycode key not in the inputMap[],we take it as
	 *                 '0'
	 * @return response entity object holding response status and list of all
	 *         SampleStorageMapping
	 * @throws Exception exception
	 */

	@PostMapping(value = "/getsamplestoragemapping")
	public ResponseEntity<Object> getSampleStorageMapping(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		LOGGER.info("getsamplestoragemapping called");
		return samplestoragemappingService.getSampleStorageMapping(inputMap, userInfo);
	}

	/**
	 * This method is used to retrieve list of available SampleStorageMapping(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}} If the
	 *                 nstoragecategorycode key not in the inputMap[],we take it as
	 *                 '0'
	 *                 nsamplestoragelocationcode: 2
	 * @return response entity object holding response status and list of all
	 *         SampleStorageMapping
	 * @throws Exception exception
	 */
	@PostMapping(value = "/addSampleStorageMapping")
	public ResponseEntity<Object> addSampleStorageMapping(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplestoragemappingService.addSampleStorageMapping(inputMap, userInfo);
	}
	
	/**
	 * This method is used to retrieve list of available SampleStorageMapping(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}} If the
	 *                 nstoragecategorycode key not in the inputMap[],we take it as
	 *                 '0'
	 *                 nsamplestoragelocationcode: 2
	 * @return response entity object holding response status and list of all
	 *         SampleStorageMapping
	 * @throws Exception exception
	 */

	@PostMapping(value = "/getEditSampleStorageMapping")
	public ResponseEntity<Object> getEditSampleStorageMapping(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplestoragemappingService.getEditSampleStorageMapping(inputMap, userInfo);
	}

	/**
	 * This method is used to retrieve list of available containerstructure(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}} If the
	 *                 nstoragecategorycode key not in the inputMap[],we take it as
	 *                 '0'
	 *                 ncontainertypecode: 2
	 * @return response entity object holding response status and list of all
	 *         containerstructure
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getContainerStructure")
	public ResponseEntity<Map<String, Object>> getContainerStructure(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplestoragemappingService.getContainerStructure(inputMap, userInfo);
	}
	
	/**
	 * This method is used to retrieve list of available SampleStorageMapping(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode":
	 *                 -1,slanguagetypecode:"en-US"},nsamplestoragemappingcode:3}
	 * @return response entity object holding response status and list of all
	 *         SampleStorageMapping
	 * @throws Exception exception
	 */

	@PostMapping(value = "/getActiveSampleStorageMappingById")
	public ResponseEntity<Object> getActiveSampleStorageMappingById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return samplestoragemappingService.getActiveSampleStorageMappingById(inputMap, userInfo);
	}
	
	/**
	 * This method is used to retrieve list of available SampleStorageMapping(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input : 
	 *                 {containerpathsize: 1,ncolumn: 10,ncontainerstructurecode: 1,ncontainertypecode: 1,ndirectionmastercode: 1
	 *                 ,nneedposition: 3,nnoofcontainer: 100,nproductcode: 6,nprojecttypecode: 1,nquantity: 0,nrow: 10,
	 *                 nsamplestoragecontainerpathcode: "[4]",nsamplestoragelocationcode: 3,nsamplestoragemappingcode: 0,
	 *                 nunitcode: -1,sboxid: "",ssamplestoragemappingname: "-"}
	 *                 "userinfo":
	 *                 { "nusercode": -1, "nuserrole": -1, "ndeputyusercode": -1,
	 *                 "ndeputyuserrole": -1, "nmodulecode": 71, "nformcode": 197,
	 *                 "nmastersitecode": -1, "nsitecode": 1, "ntranssitecode": 1,
	 *                 "nusersitecode": -1, "ndeptcode": -1, "nreasoncode": 0,
	 *                 "nisstandaloneserver": 4, "nissyncserver": 3,
	 *                 "nlogintypecode": 1, "nsiteadditionalinfo": 4,
	 *                 "ntimezonecode": -1, "istimezoneshow": 4, "isutcenabled": 4,
	 *                 "slanguagefilename": "Msg_en_US", "slanguagename": "English",
	 *                 "slanguagetypecode": "en-US", "activelanguagelist":
	 *                 ["en-US"], "sconnectionString":
	 *                 "Server=localhost;Port=5433;Database=THIST02-06-1;User=postgres;Pwd=admin@123;",
	 *                 "sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitereportdatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss", "ssitereportdate": "dd/MM/yyyy",
	 *                 "ssitereportdatetime": "dd/MM/yyyy HH:mm:ss", "sdeptname":
	 *                 "NA", "sdeputyid": "system", "sdeputyusername": "QuaLIS
	 *                 Admin", "sdeputyuserrolename": "QuaLIS Admin", "sfirstname":
	 *                 "QuaLIS", "slastname": "Admin", "sloginid": "system",
	 *                 "sformname": "Storage Structure", "smodulename": "Storage
	 *                 Management", "spredefinedreason": null, "sreason": "",
	 *                 "sreportingtoolfilename": "en.xml", "sreportlanguagecode":
	 *                 "en-US", "ssessionid": "8339746ACC3BA62170603EBECDA5E8BB",
	 *                 "ssitecode": "SYNC", "ssitename": "THSTI BRF", "stimezoneid":
	 *                 "Europe/London", "shostip": "0:0:0:0:0:0:0:1", "susername":
	 *                 "QuaLIS Admin", "suserrolename": "QuaLIS Admin", "spassword":
	 *                 null}
	 *
	 * @return response entity object holding response status and list of all
	 *         SampleStorageMapping
	 * @throws Exception exception
	 */

	@PostMapping(value = "/createSampleStorageMapping")
	public ResponseEntity<Map<String, Object>> createSampleStorageMapping(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplestoragemappingService.createSampleStorageMapping(inputMap, userInfo);
	}
	
	/**
	 * This method is used to retrieve list of available SampleStorageMapping(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input : {
	 *                containerpathsize: 1,ncolumn: 10,ncontainerstructurecode: 1,ncontainertypecode: 1,ndirectionmastercode: 1
	 *                 ,nneedposition: 3,nnoofcontainer: 100,nproductcode: 6,nprojecttypecode: 1,nquantity: 0,nrow: 10,
	 *                 nsamplestoragecontainerpathcode: "[4]",nsamplestoragelocationcode: 3,nsamplestoragemappingcode: 0,
	 *                 nunitcode: -1,sboxid: "",ssamplestoragemappingname: "-"}"userinfo":
	 *                 { "nusercode": -1, "nuserrole": -1, "ndeputyusercode": -1,
	 *                 "ndeputyuserrole": -1, "nmodulecode": 71, "nformcode": 197,
	 *                 "nmastersitecode": -1, "nsitecode": 1, "ntranssitecode": 1,
	 *                 "nusersitecode": -1, "ndeptcode": -1, "nreasoncode": 0,
	 *                 "nisstandaloneserver": 4, "nissyncserver": 3,
	 *                 "nlogintypecode": 1, "nsiteadditionalinfo": 4,
	 *                 "ntimezonecode": -1, "istimezoneshow": 4, "isutcenabled": 4,
	 *                 "slanguagefilename": "Msg_en_US", "slanguagename": "English",
	 *                 "slanguagetypecode": "en-US", "activelanguagelist":
	 *                 ["en-US"], "sconnectionString":
	 *                 "Server=localhost;Port=5433;Database=THIST02-06-1;User=postgres;Pwd=admin@123;",
	 *                 "sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitereportdatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss", "ssitereportdate": "dd/MM/yyyy",
	 *                 "ssitereportdatetime": "dd/MM/yyyy HH:mm:ss", "sdeptname":
	 *                 "NA", "sdeputyid": "system", "sdeputyusername": "QuaLIS
	 *                 Admin", "sdeputyuserrolename": "QuaLIS Admin", "sfirstname":
	 *                 "QuaLIS", "slastname": "Admin", "sloginid": "system",
	 *                 "sformname": "Storage Structure", "smodulename": "Storage
	 *                 Management", "spredefinedreason": null, "sreason": "",
	 *                 "sreportingtoolfilename": "en.xml", "sreportlanguagecode":
	 *                 "en-US", "ssessionid": "8339746ACC3BA62170603EBECDA5E8BB",
	 *                 "ssitecode": "SYNC", "ssitename": "THSTI BRF", "stimezoneid":
	 *                 "Europe/London", "shostip": "0:0:0:0:0:0:0:1", "susername":
	 *                 "QuaLIS Admin", "suserrolename": "QuaLIS Admin", "spassword":
	 *                 null } }
	 *
	 * @return response entity object holding response status and list of all
	 *         SampleStorageMapping
	 * @throws Exception exception
	 */

	@PostMapping(value = "/updateSampleStorageMapping")
	public ResponseEntity<Map<String, Object>> updateSampleStorageMapping(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplestoragemappingService.updateSampleStorageMapping(inputMap, userInfo);
	}

	
	/**
	 * This method is used to retrieve list of available SampleStorageMapping(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input : {
	 *              nsamplestoragelocationcode: 3 ,nsamplestoragemappingcode : 5 ,userinfo":
	 *                 { "nusercode": -1, "nuserrole": -1, "ndeputyusercode": -1,
	 *                 "ndeputyuserrole": -1, "nmodulecode": 71, "nformcode": 197,
	 *                 "nmastersitecode": -1, "nsitecode": 1, "ntranssitecode": 1,
	 *                 "nusersitecode": -1, "ndeptcode": -1, "nreasoncode": 0,
	 *                 "nisstandaloneserver": 4, "nissyncserver": 3,
	 *                 "nlogintypecode": 1, "nsiteadditionalinfo": 4,
	 *                 "ntimezonecode": -1, "istimezoneshow": 4, "isutcenabled": 4,
	 *                 "slanguagefilename": "Msg_en_US", "slanguagename": "English",
	 *                 "slanguagetypecode": "en-US", "activelanguagelist":
	 *                 ["en-US"], "sconnectionString":
	 *                 "Server=localhost;Port=5433;Database=THIST02-06-1;User=postgres;Pwd=admin@123;",
	 *                 "sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitereportdatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss", "ssitereportdate": "dd/MM/yyyy",
	 *                 "ssitereportdatetime": "dd/MM/yyyy HH:mm:ss", "sdeptname":
	 *                 "NA", "sdeputyid": "system", "sdeputyusername": "QuaLIS
	 *                 Admin", "sdeputyuserrolename": "QuaLIS Admin", "sfirstname":
	 *                 "QuaLIS", "slastname": "Admin", "sloginid": "system",
	 *                 "sformname": "Storage Structure", "smodulename": "Storage
	 *                 Management", "spredefinedreason": null, "sreason": "",
	 *                 "sreportingtoolfilename": "en.xml", "sreportlanguagecode":
	 *                 "en-US", "ssessionid": "8339746ACC3BA62170603EBECDA5E8BB",
	 *                 "ssitecode": "SYNC", "ssitename": "THSTI BRF", "stimezoneid":
	 *                 "Europe/London", "shostip": "0:0:0:0:0:0:0:1", "susername":
	 *                 "QuaLIS Admin", "suserrolename": "QuaLIS Admin", "spassword":
	 *                 null } }
	 *
	 * @return response entity object holding response status and list of all
	 *         SampleStorageMapping
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteSampleStorageMapping")
	public ResponseEntity<? extends Object> deleteSampleStorageMapping(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplestoragemappingService.deleteSampleStorageMapping(inputMap, userInfo);
	}

	/**
	 * This method is used to retrieve list of available SampleStorageMapping(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input : {
	 *              nsamplestoragelocationcode: 3 ,nsamplestoragemappingcode : 5 ,userinfo":
	 *                 { "nusercode": -1, "nuserrole": -1, "ndeputyusercode": -1,
	 *                 "ndeputyuserrole": -1, "nmodulecode": 71, "nformcode": 197,
	 *                 "nmastersitecode": -1, "nsitecode": 1, "ntranssitecode": 1,
	 *                 "nusersitecode": -1, "ndeptcode": -1, "nreasoncode": 0,
	 *                 "nisstandaloneserver": 4, "nissyncserver": 3,
	 *                 "nlogintypecode": 1, "nsiteadditionalinfo": 4,
	 *                 "ntimezonecode": -1, "istimezoneshow": 4, "isutcenabled": 4,
	 *                 "slanguagefilename": "Msg_en_US", "slanguagename": "English",
	 *                 "slanguagetypecode": "en-US", "activelanguagelist":
	 *                 ["en-US"], "sconnectionString":
	 *                 "Server=localhost;Port=5433;Database=THIST02-06-1;User=postgres;Pwd=admin@123;",
	 *                 "sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitereportdatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss", "ssitereportdate": "dd/MM/yyyy",
	 *                 "ssitereportdatetime": "dd/MM/yyyy HH:mm:ss", "sdeptname":
	 *                 "NA", "sdeputyid": "system", "sdeputyusername": "QuaLIS
	 *                 Admin", "sdeputyuserrolename": "QuaLIS Admin", "sfirstname":
	 *                 "QuaLIS", "slastname": "Admin", "sloginid": "system",
	 *                 "sformname": "Storage Structure", "smodulename": "Storage
	 *                 Management", "spredefinedreason": null, "sreason": "",
	 *                 "sreportingtoolfilename": "en.xml", "sreportlanguagecode":
	 *                 "en-US", "ssessionid": "8339746ACC3BA62170603EBECDA5E8BB",
	 *                 "ssitecode": "SYNC", "ssitename": "THSTI BRF", "stimezoneid":
	 *                 "Europe/London", "shostip": "0:0:0:0:0:0:0:1", "susername":
	 *                 "QuaLIS Admin", "suserrolename": "QuaLIS Admin", "spassword":
	 *                 null } }
	 *
	 * @return response entity object holding response status and list of all
	 *         SampleStorageMapping
	 * @throws Exception exception
	 */
	@PostMapping(value = "/approveSampleStorageMapping")
	public ResponseEntity<Object> approveSampleStorageMapping(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplestoragemappingService.approveSampleStorageMapping(inputMap, userInfo);
	}

//	@PostMapping(value = "/getsamplestoragemappingSheetData")
//	public ResponseEntity<Object> getsamplestoragemappingSheetData(@RequestBody Map<String, Object> inputMap)
//			throws Exception {
//		final ObjectMapper objMapper = new ObjectMapper();
//		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
//		});
//		requestContext.setUserInfo(userInfo);
//		return samplestoragemappingService.getsamplestoragemappingSheetData(inputMap, userInfo);
//	}

//	@PostMapping(value = "/getdynamicfilterexecutedata")
//	public ResponseEntity<Object> getDynamicFilterExecuteData(@RequestBody Map<String, Object> inputMap)
//			throws Exception {
//		final ObjectMapper objMapper = new ObjectMapper();
//		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
//		});
//		requestContext.setUserInfo(userInfo);
//		return samplestoragemappingService.getDynamicFilterExecuteData(inputMap, userInfo);
//	}
}
