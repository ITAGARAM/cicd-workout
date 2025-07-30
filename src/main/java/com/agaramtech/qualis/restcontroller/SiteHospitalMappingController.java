package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.configuration.service.sitehospitalmapping.SiteHospitalMappingService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller is used to dispatch the input request to its relevant service
 * methods to access the SiteHospitalMapping Service methods. 
 * Jira id - BGSI-8  Screen Name -> Site Hospital Mapping
 */
@RestController
@RequestMapping("/sitehospitalmapping")
public class SiteHospitalMappingController {

	private static final Logger LOGGER = LoggerFactory.getLogger(UnitController.class);

	private RequestContext requestContext;
	private final SiteHospitalMappingService siteHospitalMappingService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext             RequestContext to hold the request
	 * @param SiteHospitalMappingService siteHospitalMappingService
	 */
	public SiteHospitalMappingController(RequestContext requestContext,
			SiteHospitalMappingService siteHospitalMappingService) {
		super();
		this.requestContext = requestContext;
		this.siteHospitalMappingService = siteHospitalMappingService;
	}

	/**
	 * This method is used to retrieve list of available SiteHospitalMapping(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getSiteHospitalMapping")
	public ResponseEntity<Object> getSiteHospitalMapping(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return siteHospitalMappingService.getSiteHospitalMapping(userInfo);

	}

	/**
	 * This method is used to retrieve list of available SiteHospitalMapping(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}"nmappingcode":1}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@PostMapping(value = "/editSiteAndBioBank")
	public ResponseEntity<Object> editSiteAndBioBank(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return siteHospitalMappingService.editSiteAndBioBank(inputMap, userInfo);

	}

	/**
	 * This method is used to retrieve list of available SiteHospitalMapping(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1},"SiteHospitalMapping" = {
	 *                 "nmappingsitecode": 1, "ssitecode": 'AS', "sdescription": '',
	 *                 "ssitetypename": 1, "nsitetypecode": 'Cancer', "ssitename":
	 *                 'LIMS' }}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createSiteAndBioBank")
	public ResponseEntity<Object> createSiteAndBioBank(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return siteHospitalMappingService.createSiteAndBioBank(inputMap, userInfo);

	}

	/**
	 * This method is used to retrieve list of available Hospital(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getHospitalMaster")
	public ResponseEntity<Object> getHospitalMaster(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return siteHospitalMappingService.getHospitalMaster(inputMap, userInfo);

	}

	/**
	 * This method is used to retrieve list of available SiteHospitalMapping(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1} "nhospitalcode":
	 *                 1,2,3,4,5, "nmappingsitecode": 'LIMS',
	 *                 "nsitehospitalmappingcode": 1, "shospitalcode":
	 *                 'ASS,DF,GH,JK,LK', "shospitalname": 'JK,KL,LO,OI,IU,,}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createHospitalMaster")
	public ResponseEntity<Object> createHospitalMaster(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return siteHospitalMappingService.createHospitalMaster(inputMap, userInfo);

	}

	/**
	 * This method is used to retrieve list of available SiteHospitalMapping(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1} "nhospitalcode":
	 *                 1"nmappingsitecode": 'LIMS',
	 *                 "nsitehospitalmappingcode": 1, "shospitalcode":
	 *                 'ASS', "shospitalname": 'JK'}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteHospitalMaster")
	public ResponseEntity<Object> deleteHospitalMaster(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return siteHospitalMappingService.deleteHospitalMaster(inputMap, userInfo);

	}

	/**
	 * This method is used to retrieve list of available Hospital(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getSiteHospitalMappingRecord")
	public ResponseEntity<Object> getSiteHospitalMappingRecord(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final String siteCodeStr = (String) inputMap.get("nmappingsitecode");
		final int nmaapingsitecode = Integer.parseInt(siteCodeStr);
		requestContext.setUserInfo(userInfo);
		return siteHospitalMappingService.getSiteHospitalMappingRecord(nmaapingsitecode, userInfo);

	}

}
