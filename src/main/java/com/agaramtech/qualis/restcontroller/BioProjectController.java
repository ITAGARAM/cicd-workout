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
import com.agaramtech.qualis.project.service.bioproject.BioProjectService;
import com.agaramtech.qualis.project.service.diseasecategory.DiseaseCategoryService;
import com.agaramtech.qualis.restcontroller.RequestContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller is used to dispatch the input request to its relevant service
 * methods to access the BioProject Service methods.
 */
@RestController
@RequestMapping("/bioproject")
public class BioProjectController {

	private static final Logger LOGGER = LoggerFactory.getLogger(BioProjectController.class);

	private RequestContext requestContext;
	private final BioProjectService bioProjectService;
	private final DiseaseCategoryService diseaseCategoryService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext    RequestContext to hold the request
	 * @param BioProjectService bioProjectService
	 */
	public BioProjectController(RequestContext requestContext, BioProjectService bioProjectService,
			DiseaseCategoryService diseaseCategoryService) {
		super();
		this.requestContext = requestContext;
		this.bioProjectService = bioProjectService;
		this.diseaseCategoryService = diseaseCategoryService;
	}

	/**
	 * This method is used to retrieve list of available bioproject(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */

	@PostMapping(value = "/getBioProject")
	public ResponseEntity<Object> getBioProject(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return bioProjectService.getBioProject(userInfo);

	}

	/**
	 * This method is used to retrieve list of available bioproject(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getDiseaseCategory")
	public ResponseEntity<Object> getDiseaseCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("getDiseaseCategory() called");
		requestContext.setUserInfo(userInfo);
//		return new ResponseEntity<Object>(diseaseCategoryService.getDiseaseCategory(userInfo),HttpStatus.OK);
		return diseaseCategoryService.getDiseaseCategory(userInfo);
	}

	/**
	 * This method is used to retrieve list of available bioproject(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 inputMap:[{"userinfo":{nmastersitecode":
	 *                 -1},"ndiseasecategory":1}]
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getDiseaseByCategory")
	public ResponseEntity<Object> getDiseaseByCatgory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioProjectService.getDiseaseByCatgory(inputMap, userInfo);
	}

	/**
	 * This method is used to retrieve list of available bioproject(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getUsers")
	public ResponseEntity<Object> getUsers(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioProjectService.getUsers(inputMap, userInfo);
	}

	/**
	 * This method is used to retrieve list of available bioproject(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input : {
	 *                 "bioproject": { "nsitecode": -1, "sdiseasecategoryname": "2",
	 *                 "ndiseasecategorycode": 2, "nbioprojectcode": null,
	 *                 "ndiseasecode": 2, "nusercode": -1, "sdescription": "ss",
	 *                 "sdiseasename": "qwewqeqwe", "sprojectcode": "ss",
	 *                 "sprojecttitle": "sss", "suserName": "QuaLIS Admin" },
	 *                 "userinfo": { "nusercode": -1, "nuserrole": -1,
	 *                 "ndeputyusercode": -1, "ndeputyuserrole": -1, "nmodulecode":
	 *                 54 "activelanguagelist": ["en-US"], "istimezoneshow": 4,
	 *                 "isutcenabled": 4, "ndeptcode": -1, "ndeputyusercode": -1,
	 *                 "ndeputyuserrole": -1, "nformcode": 255,
	 *                 "nisstandaloneserver": 4, "nissyncserver": 4,
	 *                 "nlogintypecode": 1, "nmastersitecode": -1, "nmodulecode":
	 *                 54, "nreasoncode": 0, "nsiteadditionalinfo": 4, "nsitecode":
	 *                 1, "ntimezonecode": -1, "ntranssitecode": 1, "nusercode": -1,
	 *                 "nuserrole": -1, "nusersitecode": -1, "sconnectionString":
	 *                 "Server=localhost;Port=5433;Database=BGSI27-06;User=postgres;Pwd=admin@123;",
	 *                 "sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sdeptname": "NA",
	 *                 "sdeputyid": "system", "sdeputyusername": "QuaLIS Admin",
	 *                 "sdeputyuserrolename": "QuaLIS Admin", "sfirstname":
	 *                 "QuaLIS", "sformname": "Bio Project", "sgmtoffset": "UTC
	 *                 +00:00", "shostip": "0:0:0:0:0:0:0:1", "slanguagefilename":
	 *                 "Msg_en_US", "slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "slastname": "Admin", "sloginid": "system",
	 *                 "smodulename": "Project", "spassword": null,
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitereportdatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spredefinedreason": null, "sreason": "",
	 *                 "sreportingtoolfilename": "en.xml", "sreportlanguagecode":
	 *                 "en-US", "ssessionid": "6C3788716A7237005E1E962B3105ACF1",
	 *                 "ssitecode": "SYNC", "ssitedate": "dd/MM/yyyy",
	 *                 "ssitedatetime": "dd/MM/yyyy HH:mm:ss", "ssitename": "LIMS",
	 *                 "ssitereportdate": "dd/MM/yyyy", "ssitereportdatetime":
	 *                 "dd/MM/yyyy HH:mm:ss", "stimezoneid": "Europe/London",
	 *                 "susername": "QuaLIS Admin", "suserrolename": "QuaLIS Admin"
	 *                 }
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createBioProject")
	public ResponseEntity<Object> createBioProject(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioProjectService.createBioProject(inputMap, userInfo);
	}
	/**
	 * This method is used to retrieve list of available bioproject(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1},"bioproject":{"nbioprojectcode":1}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveBioProjectById")
	public ResponseEntity<Object> getActiveBioProjectById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nbioprojectcode = (Integer) inputMap.get("nbioprojectcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioProjectService.getActiveBioProjectById(nbioprojectcode, userInfo);
	}
	/**
	 * This method is used to retrieve list of available bioproject(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input : {
	 *                 "bioproject": { "nsitecode": -1, "sdiseasecategoryname": "2",
	 *                 "ndiseasecategorycode": 2, "nbioprojectcode": 1,
	 *                 "ndiseasecode": 2, "nusercode": -1, "sdescription": "ss",
	 *                 "sdiseasename": "qwewqeqwe", "sprojectcode": "ss",
	 *                 "sprojecttitle": "sss", "suserName": "QuaLIS Admin" },
	 *                 "userinfo": { "nusercode": -1, "nuserrole": -1,
	 *                 "ndeputyusercode": -1, "ndeputyuserrole": -1, "nmodulecode":
	 *                 54 "activelanguagelist": ["en-US"], "istimezoneshow": 4,
	 *                 "isutcenabled": 4, "ndeptcode": -1, "ndeputyusercode": -1,
	 *                 "ndeputyuserrole": -1, "nformcode": 255,
	 *                 "nisstandaloneserver": 4, "nissyncserver": 4,
	 *                 "nlogintypecode": 1, "nmastersitecode": -1, "nmodulecode":
	 *                 54, "nreasoncode": 0, "nsiteadditionalinfo": 4, "nsitecode":
	 *                 1, "ntimezonecode": -1, "ntranssitecode": 1, "nusercode": -1,
	 *                 "nuserrole": -1, "nusersitecode": -1, "sconnectionString":
	 *                 "Server=localhost;Port=5433;Database=BGSI27-06;User=postgres;Pwd=admin@123;",
	 *                 "sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sdeptname": "NA",
	 *                 "sdeputyid": "system", "sdeputyusername": "QuaLIS Admin",
	 *                 "sdeputyuserrolename": "QuaLIS Admin", "sfirstname":
	 *                 "QuaLIS", "sformname": "Bio Project", "sgmtoffset": "UTC
	 *                 +00:00", "shostip": "0:0:0:0:0:0:0:1", "slanguagefilename":
	 *                 "Msg_en_US", "slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "slastname": "Admin", "sloginid": "system",
	 *                 "smodulename": "Project", "spassword": null,
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitereportdatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spredefinedreason": null, "sreason": "",
	 *                 "sreportingtoolfilename": "en.xml", "sreportlanguagecode":
	 *                 "en-US", "ssessionid": "6C3788716A7237005E1E962B3105ACF1",
	 *                 "ssitecode": "SYNC", "ssitedate": "dd/MM/yyyy",
	 *                 "ssitedatetime": "dd/MM/yyyy HH:mm:ss", "ssitename": "LIMS",
	 *                 "ssitereportdate": "dd/MM/yyyy", "ssitereportdatetime":
	 *                 "dd/MM/yyyy HH:mm:ss", "stimezoneid": "Europe/London",
	 *                 "susername": "QuaLIS Admin", "suserrolename": "QuaLIS Admin"
	 *                 }
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteBioProject")
	public ResponseEntity<Object> deleteBioProject(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioProjectService.deleteBioProject(inputMap, userInfo);
	}
	/**
	 * This method is used to retrieve list of available bioproject(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input : {
	 *                 "bioproject": { "nsitecode": -1, "sdiseasecategoryname": "2",
	 *                 "ndiseasecategorycode": 2, "nbioprojectcode": 1,
	 *                 "ndiseasecode": 2, "nusercode": -1, "sdescription": "ss",
	 *                 "sdiseasename": "qwewqeqwe", "sprojectcode": "ss",
	 *                 "sprojecttitle": "sss", "suserName": "QuaLIS Admin" },
	 *                 "userinfo": { "nusercode": -1, "nuserrole": -1,
	 *                 "ndeputyusercode": -1, "ndeputyuserrole": -1, "nmodulecode":
	 *                 54 "activelanguagelist": ["en-US"], "istimezoneshow": 4,
	 *                 "isutcenabled": 4, "ndeptcode": -1, "ndeputyusercode": -1,
	 *                 "ndeputyuserrole": -1, "nformcode": 255,
	 *                 "nisstandaloneserver": 4, "nissyncserver": 4,
	 *                 "nlogintypecode": 1, "nmastersitecode": -1, "nmodulecode":
	 *                 54, "nreasoncode": 0, "nsiteadditionalinfo": 4, "nsitecode":
	 *                 1, "ntimezonecode": -1, "ntranssitecode": 1, "nusercode": -1,
	 *                 "nuserrole": -1, "nusersitecode": -1, "sconnectionString":
	 *                 "Server=localhost;Port=5433;Database=BGSI27-06;User=postgres;Pwd=admin@123;",
	 *                 "sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sdeptname": "NA",
	 *                 "sdeputyid": "system", "sdeputyusername": "QuaLIS Admin",
	 *                 "sdeputyuserrolename": "QuaLIS Admin", "sfirstname":
	 *                 "QuaLIS", "sformname": "Bio Project", "sgmtoffset": "UTC
	 *                 +00:00", "shostip": "0:0:0:0:0:0:0:1", "slanguagefilename":
	 *                 "Msg_en_US", "slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "slastname": "Admin", "sloginid": "system",
	 *                 "smodulename": "Project", "spassword": null,
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitereportdatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spredefinedreason": null, "sreason": "",
	 *                 "sreportingtoolfilename": "en.xml", "sreportlanguagecode":
	 *                 "en-US", "ssessionid": "6C3788716A7237005E1E962B3105ACF1",
	 *                 "ssitecode": "SYNC", "ssitedate": "dd/MM/yyyy",
	 *                 "ssitedatetime": "dd/MM/yyyy HH:mm:ss", "ssitename": "LIMS",
	 *                 "ssitereportdate": "dd/MM/yyyy", "ssitereportdatetime":
	 *                 "dd/MM/yyyy HH:mm:ss", "stimezoneid": "Europe/London",
	 *                 "susername": "QuaLIS Admin", "suserrolename": "QuaLIS Admin"
	 *                 }
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateBioProject")
	public ResponseEntity<Object> updateBioProject(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioProjectService.updateBioProject(inputMap, userInfo);
	}

}
