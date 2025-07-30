package com.agaramtech.qualis.storagemanagement.service.samplestorageretrieval;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the samplestorageretrieval Service methods
 * 
 * @author ATE236
 * @version 10.0.0.2
 */
public interface SampleStorageRetrievalDAO {

	/**
	 * This method is used to retrieve list of available unit(s). 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 	
	 * 					Input : {"userinfo":{nmastersitecode": -1,ntransitecode:1}}				
	 * @return response entity  object holding response status and list of all units
	 * @throws Exception exception
	 */
	public ResponseEntity<Object> getsamplestorageretrieval(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;
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
	public ResponseEntity<Map<String, Object>> getDynamicFilterExecuteData(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;
	/**
	 * This method is used to retrieve list of available unit(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1,ntransitecode:1,nprojecttypecode: 1,spositionvalue: "P3092993530908"}}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	public ResponseEntity<Object> getSelectedBarcodeData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;
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
	public ResponseEntity<Map<String, Object>> getProjectbarcodeconfig(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;
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
	public ResponseEntity<Object> createsamplestorageretrieval(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;
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
	public ResponseEntity<Object> createbulkeretrievedispose(final MultipartHttpServletRequest request,
			final UserInfo userInfo) throws Exception;
}
