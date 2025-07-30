package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.storagemanagement.service.samplestorageretrieval.SampleStorageRetrievalService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the samplestorageretrieval Service methods
 * 
 * @author ATE236
 * @version 10.0.0.2
 */
@RestController
@RequestMapping("/samplestorageretrieval")
public class SampleStorageRetrievalController {

	private RequestContext requestContext;
	private final SampleStorageRetrievalService samplestorageretrievalservice;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext                RequestContext to hold the request
	 * @param samplestorageretrievalservice SampleStorageRetrievalService
	 */
	public SampleStorageRetrievalController(RequestContext requestContext,
			SampleStorageRetrievalService samplestorageretrievalservice) {
		super();
		this.requestContext = requestContext;
		this.samplestorageretrievalservice = samplestorageretrievalservice;
	}

	/**
	 * This method is used to retrieve list of available unit(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1,ntransitecode:1}}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getsamplestorageretrieval")
	public ResponseEntity<Object> getsamplestorageretrieval(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplestorageretrievalservice.getsamplestorageretrieval(inputMap, userInfo);
	}

	/**
	 * This method is used to retrieve list of available unit(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1,ntransitecode:1}}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getdynamicfilterexecutedata")
	public ResponseEntity<Map<String, Object>> getDynamicFilterExecuteData(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplestorageretrievalservice.getDynamicFilterExecuteData(inputMap, userInfo);
	}

	/**
	 * This method is used to retrieve list of available unit(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode":
	 *                 -1,ntransitecode:1,nprojecttypecode: 1,spositionvalue:
	 *                 "P3092993530908"}}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getSelectedBarcodeData")
	public ResponseEntity<Object> getSelectedBarcodeData(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplestorageretrievalservice.getSelectedBarcodeData(inputMap, userInfo);
	}

	/**
	 * This method is used to retrieve list of available unit(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :{
	 *                 "fromDate": "2025-06-04T00:00:00", "toDate":
	 *                 "2025-06-11T23:59:59", "isFilterSubmit": true,
	 *                 "nprojecttypecode": 1, "nformcode": 203, "nmodulecode": 71,
	 *                 "nsitecode": 1, "ntranssitecode": 1, "ndeptcode": -1,
	 *                 "nreasoncode": 0, "nmastersitecode": -1, "nlogintypecode": 1,
	 *                 "ntimezonecode": -1, "nusersitecode": -1, "nissyncserver": 3,
	 *                 "nisstandaloneserver": 4, "istimezoneshow": 4,
	 *                 "isutcenabled": 4, "nsiteadditionalinfo": 4, "userinfo": {
	 *                 "nusercode": -1, "nuserrole": -1, "ndeputyusercode": -1,
	 *                 "ndeputyuserrole": -1, "nmodulecode": 71 }, "ssessionid":
	 *                 "83A3B192135545A8921EA89A8DC62C0B", "sconnectionString":
	 *                 "Server=localhost;Port=5433;Database=THIST02-06-1;User=postgres;Pwd=admin@123;",
	 *                 "sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitereportdatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss", "ssitereportdate": "dd/MM/yyyy",
	 *                 "ssitereportdatetime": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00", "stimezoneid": "Europe/London",
	 *                 "activelanguagelist": ["en-US"], "slanguagefilename":
	 *                 "Msg_en_US", "slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "sreportlanguagecode": "en-US",
	 *                 "sreportingtoolfilename": "en.xml", "sformname": "Sample
	 *                 Retrieval and Disposal", "smodulename": "Storage Management",
	 *                 "ssitename": "THSTI BRF", "ssitecode": "SYNC", "susername":
	 *                 "QuaLIS Admin", "suserrolename": "QuaLIS Admin", "sloginid":
	 *                 "system", "sfirstname": "QuaLIS", "slastname": "Admin",
	 *                 "sdeptname": "NA", "sdeputyid": "system", "sdeputyusername":
	 *                 "QuaLIS Admin", "sdeputyuserrolename": "QuaLIS Admin",
	 *                 "sreason": "", "spredefinedreason": null, "spassword": null }
	 * 
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getProjectbarcodeconfig")
	public ResponseEntity<Map<String, Object>> getProjectbarcodeconfig(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplestorageretrievalservice.getProjectbarcodeconfig(inputMap, userInfo);
	}

	/**
	 * This method is used to retrieve list of available unit(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :{
	 *                 isRetrieve: true, nneedaliquot: false, nprojecttypecode: 1,
	 *                 nquantity: 0, saliquotsampleid: "", scomments: "-",
	 *                 spositionvalue: "P3092993530908", sunitname: "NA"
	 *                 userinfo:{"nformcode": 203, "nmodulecode": 71, "nsitecode":
	 *                 1, "ntranssitecode": 1, "ndeptcode": -1, "nreasoncode": 0,
	 *                 "nmastersitecode": -1, "nlogintypecode": 1, "ntimezonecode":
	 *                 -1, "nusersitecode": -1, "nissyncserver": 3,
	 *                 "nisstandaloneserver": 4, "istimezoneshow": 4,
	 *                 "isutcenabled": 4, "nsiteadditionalinfo": 4, "userinfo": {
	 *                 "nusercode": -1, "nuserrole": -1, "ndeputyusercode": -1,
	 *                 "ndeputyuserrole": -1, "nmodulecode": 71 }, "ssessionid":
	 *                 "83A3B192135545A8921EA89A8DC62C0B", "sconnectionString":
	 *                 "Server=localhost;Port=5433;Database=THIST02-06-1;User=postgres;Pwd=admin@123;",
	 *                 "sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitereportdatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss", "ssitereportdate": "dd/MM/yyyy",
	 *                 "ssitereportdatetime": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00", "stimezoneid": "Europe/London",
	 *                 "activelanguagelist": ["en-US"], "slanguagefilename":
	 *                 "Msg_en_US", "slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "sreportlanguagecode": "en-US",
	 *                 "sreportingtoolfilename": "en.xml", "sformname": "Sample
	 *                 Retrieval and Disposal", "smodulename": "Storage Management",
	 *                 "ssitename": "THSTI BRF", "ssitecode": "SYNC", "susername":
	 *                 "QuaLIS Admin", "suserrolename": "QuaLIS Admin", "sloginid":
	 *                 "system", "sfirstname": "QuaLIS", "slastname": "Admin",
	 *                 "sdeptname": "NA", "sdeputyid": "system", "sdeputyusername":
	 *                 "QuaLIS Admin", "sdeputyuserrolename": "QuaLIS Admin",
	 *                 "sreason": "", "spredefinedreason": null, "spassword": null
	 *                 }}
	 * 
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createsamplestorageretrieval")
	public ResponseEntity<Object> createsamplestorageretrieval(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplestorageretrievalservice.createsamplestorageretrieval(inputMap, userInfo);
	}
	/**
	 * This method is used to retrieve list of available unit(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :{
	 *                 "fromDate": "2025-06-04T00:00:00", "toDate":
	 *                 "2025-06-11T23:59:59", "isFilterSubmit": true,
	 *                 "nprojecttypecode": 1,"retrieveDisposeSampleType":97, "userInfo":{"nformcode": 203, "nmodulecode": 71,
	 *                 "nsitecode": 1, "ntranssitecode": 1, "ndeptcode": -1,
	 *                 "nreasoncode": 0, "nmastersitecode": -1, "nlogintypecode": 1,
	 *                 "ntimezonecode": -1, "nusersitecode": -1, "nissyncserver": 3,
	 *                 "nisstandaloneserver": 4, "istimezoneshow": 4,
	 *                 "isutcenabled": 4, "nsiteadditionalinfo": 4, "userinfo": {
	 *                 "nusercode": -1, "nuserrole": -1, "ndeputyusercode": -1,
	 *                 "ndeputyuserrole": -1, "nmodulecode": 71 }, "ssessionid":
	 *                 "83A3B192135545A8921EA89A8DC62C0B", "sconnectionString":
	 *                 "Server=localhost;Port=5433;Database=THIST02-06-1;User=postgres;Pwd=admin@123;",
	 *                 "sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitereportdatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss", "ssitereportdate": "dd/MM/yyyy",
	 *                 "ssitereportdatetime": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00", "stimezoneid": "Europe/London",
	 *                 "activelanguagelist": ["en-US"], "slanguagefilename":
	 *                 "Msg_en_US", "slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "sreportlanguagecode": "en-US",
	 *                 "sreportingtoolfilename": "en.xml", "sformname": "Sample
	 *                 Retrieval and Disposal", "smodulename": "Storage Management",
	 *                 "ssitename": "THSTI BRF", "ssitecode": "SYNC", "susername":
	 *                 "QuaLIS Admin", "suserrolename": "QuaLIS Admin", "sloginid":
	 *                 "system", "sfirstname": "QuaLIS", "slastname": "Admin",
	 *                 "sdeptname": "NA", "sdeputyid": "system", "sdeputyusername":
	 *                 "QuaLIS Admin", "sdeputyuserrolename": "QuaLIS Admin",
	 *                 "sreason": "", "spredefinedreason": null, "spassword": null} }
	 * 
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createbulkeretrievedispose")
	public ResponseEntity<Object> createbulkeretrievedispose(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return samplestorageretrievalservice.createbulkeretrievedispose(request, userInfo);
	}

}
