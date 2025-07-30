package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.configuration.model.LimsElnSiteMapping;
import com.agaramtech.qualis.configuration.service.limselnsite.LimsElnSiteMappingService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller is used to dispatch the input request to its relevant service
 * methods to access the LimsElnSiteMapping Service methods.
 */

@RestController
@RequestMapping("/limselnsitemapping")
public class LimsElnSiteMappingController {

	private static final Logger LOGGER = LoggerFactory.getLogger(LimsElnSiteMappingController.class);

	private RequestContext requestContext;
	private final LimsElnSiteMappingService LimsElnSiteMappingService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext            RequestContext to hold the request
	 * @param limsElnSiteMappingService LimsElnSiteMappingService
	 */
	public LimsElnSiteMappingController(RequestContext requestContext,
			LimsElnSiteMappingService limsElnSiteMappingService) {
		super();
		this.requestContext = requestContext;
		LimsElnSiteMappingService = limsElnSiteMappingService;
	}

	/**
	 * This method is used to retrieve list of available limsElnSiteMapping(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all
	 *         limsElnSiteMappings
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getLimsElnSitemapping")
	public ResponseEntity<Object> getlimselnsitemapping(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		LOGGER.info("getLimsElnSitemapping called");
		return LimsElnSiteMappingService.getlimselnsitemapping(userInfo);
	}

	/**
	 * This method will is used to make a new entry to limsElnSiteMapping table.
	 * 
	 * @param inputMap map object holding params ( limsElnSiteMapping
	 *                 [LimsElnSiteMapping] object holding details to be added in
	 *                 limsElnSiteMapping table, userinfo [UserInfo] holding logged
	 *                 in user details ) Input:{ "limsElnSiteMapping": {
	 *                 "slimselnsitemappingname": "cm"}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": - 1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 181,"nmastersitecode": -1,"nmodulecode": 3,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"} }
	 * @return ResponseEntity with string message as 'Already Exists' if the
	 *         limsElnSiteMapping already exists/ list of limsElnSiteMappings along
	 *         with the newly added limsElnSiteMapping.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createLimsElnSitemapping")
	public ResponseEntity<Object> createLimsElnSitemapping(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final LimsElnSiteMapping limselnuser = objmapper.convertValue(inputMap.get("limselnsitemapping"),
				LimsElnSiteMapping.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return LimsElnSiteMappingService.createLimsElnSitemapping(limselnuser, userInfo);
	}

	/**
	 * This method is used to retrieve list of available limsUsers(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all
	 *         limsUsers
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getLimsSite")
	public ResponseEntity<Object> getLimsSite(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return LimsElnSiteMappingService.getLimsSite(userInfo);
	}

	/**
	 * This method is used to delete an entry in LimsElnSiteMapping table
	 * 
	 * @param inputMap [Map] object with keys of LimsElnSiteMapping entity and
	 *                 UserInfo object. Input:{ "limsElnSiteMapping":
	 *                 {"nelnsitemappingcode":1}, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"],"isutcenabled": 4,"ndeptcode":
	 *                 -1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 181,"nmastersitecode": -1, "nmodulecode": 3, "nreasoncode":
	 *                 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode":
	 *                 1,"nusercode": -1, "nuserrole": -1,"nusersitecode":
	 *                 -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the
	 *         limsElnSiteMapping record is not available/ string message as 'Record
	 *         is used in....' when the limsElnSiteMapping is associated in
	 *         transaction / list of all limsElnSiteMappings excluding the deleted
	 *         record
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteLimsElnSitemapping")
	public ResponseEntity<Object> deleteLimsElnSitemapping(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final LimsElnSiteMapping limselnuser = objmapper.convertValue(inputMap.get("limselnsitemapping"),
				LimsElnSiteMapping.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return LimsElnSiteMappingService.deleteLimsElnSitemapping(limselnuser, userInfo);
	}

	/**
	 * This method is used to retrieve a specific limsElnSiteMapping record.
	 * 
	 * @param inputMap [Map] map object with "nlimsElnSiteMappingcode" and
	 *                 "userinfo" as keys for which the data is to be fetched
	 *                 Input:{ "nelnsitemappingcode": 1, "userinfo":{
	 *                 "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 181,"nmastersitecode": -1,"nmodulecode": 3,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with LimsElnSiteMapping object for the specified
	 *         primary key / with string message as 'Deleted' if the
	 *         limsElnSiteMapping record is not available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveLimsElnSitemappingById")
	public ResponseEntity<Object> getActiveLimsElnSitemappingById(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final int nelnsitemappingcode = (Integer) inputMap.get("nelnsitemappingcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return LimsElnSiteMappingService.getActiveLimsElnSitemappingById(nelnsitemappingcode, userInfo);
	}

	/**
	 * This method is used to update selected limsElnSiteMapping details.
	 * 
	 * @param inputMap [map object holding params( limsElnSiteMapping
	 *                 [LimsElnSiteMapping] object holding details to be updated in
	 *                 limsElnSiteMapping table, userinfo [UserInfo] holding logged
	 *                 in user details) Input:{ "limsElnSiteMapping":
	 *                 {"nelnsitemappingcode":1,"slimsElnSiteMappingname": "m",
	 *                 "sdescription": "m", "ndefaultstatus": 3 }, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 181,"nmastersitecode": -1, "nmodulecode": 3,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * 
	 * 
	 * @return ResponseEntity with string message as 'Already Deleted' if the
	 *         limsElnSiteMapping record is not available/ list of all
	 *         limsElnSiteMappings and along with the updated limsElnSiteMapping.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateLimsElnSitemapping")
	public ResponseEntity<Object> updateLimsElnSitemapping(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final LimsElnSiteMapping limselnuser = objmapper.convertValue(inputMap.get("limselnsitemapping"),
				LimsElnSiteMapping.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return LimsElnSiteMappingService.updateLimsElnSitemapping(limselnuser, userInfo);
	}
}
