package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.basemaster.model.SiteType;
import com.agaramtech.qualis.basemaster.service.sitetype.SiteTypeService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant service methods
 * to access the SiteType Service methods.
 */
/**
 * @author sujatha.v BGSI-5 02/07/2025
 */
@RestController
@RequestMapping("/sitetype")
public class SiteTypeController {

	private static final Logger LOGGER = LoggerFactory.getLogger(SiteTypeController.class);

	private final SiteTypeService siteTypeService;
	private RequestContext requestContext;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext  RequestContext to hold the request
	 * @param siteTypeService SiteTypeService
	 */
	public SiteTypeController(SiteTypeService siteTypeService, RequestContext requestContext) {
		super();
		this.siteTypeService = siteTypeService;
		this.requestContext = requestContext;
	}

	/**
	 * This method is used to retrieve list of available sitetype(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all
	 *         sitetype
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getSiteType")
	public ResponseEntity<Object> getSiteType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("getSiteType() called");
		requestContext.setUserInfo(userInfo);
		return siteTypeService.getSiteType(userInfo);
	}

	/**
	 * This method is used to retrieve a specific sitetype record.
	 * 
	 * @param inputMap [Map] map object with "nsitetypecode" and "userinfo" as keys
	 *                 for which the data is to be fetched Input:{ "nsitetypecode":
	 *                 1, "userinfo":{ "activelanguagelist": ["en-US"],
	 *                 "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode":
	 *                 -1,"ndeputyuserrole": -1, "nformcode": 255,"nmastersitecode":
	 *                 -1,"nmodulecode": 1, "nreasoncode": 0,"nsitecode":
	 *                 -1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
	 *                 "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat":
	 *                 "dd/MM/yyyy HH:mm:ss", "sgmtoffset": "UTC
	 *                 +00:00","slanguagefilename": "Msg_en_US","slanguagename":
	 *                 "English", "slanguagetypecode": "en-US", "spgdatetimeformat":
	 *                 "dd/MM/yyyy HH24:mi:ss", "spgsitedatetime": "dd/MM/yyyy
	 *                 HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
	 *                 "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with SiteType object for the specified primary key /
	 *         with string message as 'Deleted' if the sitetype record is not
	 *         available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveSiteTypeById")
	public ResponseEntity<Object> getActiveSiteTypeById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final int nsitetypecode = (Integer) inputMap.get("nsitetypecode");
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return siteTypeService.getActiveSiteTypeById(nsitetypecode, userInfo);
	}

	/**
	 * This method will is used to make a new entry to sitetype table.
	 * 
	 * @param inputMap map object holding params ( sitetype [SiteType] object
	 *                 holding details to be added in sitetype table, userinfo
	 *                 [UserInfo] holding logged in user details ) Input:{
	 *                 "sitetype": { "ssitetypename": "site1",
	 *                 "nhierarchicalorderno": 32}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 255,"nmastersitecode": -1,"nmodulecode": 1,
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
	 *         sitetype already exists/ list of sitetype along with the newly added
	 *         sitetype.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createSiteType")
	public ResponseEntity<Object> createSiteType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final SiteType objSiteType = objectMapper.convertValue(inputMap.get("sitetype"), new TypeReference<SiteType>() {
		});
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return siteTypeService.createSiteType(objSiteType, userInfo);
	}

	/**
	 * This method is used to update selected sitetype details.
	 * 
	 * @param inputMap [map object holding params( sitetype [SiteType] object
	 *                 holding details to be updated in sitetype table, userinfo
	 *                 [UserInfo] holding logged in user details) Input:{
	 *                 "sitetype": {"nsitetypecode":1,"ssitetypename": "site2",
	 *                 "hhierarchicalorderno":2 ,"sdescription": "NA" },
	 *                 "userinfo":{ "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 255,"nmastersitecode": -1, "nmodulecode": 1,
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
	 *         sitetype record is not available/ list of all sitetype's and along
	 *         with the updated sitetype.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateSiteType")
	public ResponseEntity<Object> updateSiteType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final SiteType objSiteType = objectMapper.convertValue(inputMap.get("sitetype"), new TypeReference<SiteType>() {
		});
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return siteTypeService.updateSiteType(objSiteType, userInfo);
	}

	/**
	 * This method is used to delete an entry in sitetype table
	 * 
	 * @param inputMap [Map] object with keys of SiteType entity and UserInfo
	 *                 object. Input:{ "sitetype": {"nsitetypecode":1}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 255,"nmastersitecode": -1, "nmodulecode": 1,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the
	 *         sitetype record is not available/ string message as 'Record is used
	 *         in....' when the sitetype is associated in transaction / list of all
	 *         sitetype's excluding the deleted record
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteSiteType")
	public ResponseEntity<Object> deleteSiteType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		final SiteType objSiteType = objectMapper.convertValue(inputMap.get("sitetype"), new TypeReference<SiteType>() {
		});
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return siteTypeService.deleteSiteType(objSiteType, userInfo);
	}
}
