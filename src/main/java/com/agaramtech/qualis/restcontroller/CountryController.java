package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.contactmaster.model.Country;
import com.agaramtech.qualis.contactmaster.service.country.CountryService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant service
 * methods to access the Country Service methods.
 */
@RestController
@RequestMapping("/country")
public class CountryController {

	private static final Logger LOGGER = LoggerFactory.getLogger(CountryController.class);

	private final CountryService countryService;
	private RequestContext requestContext;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext RequestContext to hold the request
	 * @param countryService CountryService
	 */
	public CountryController(RequestContext requestContext, CountryService countryService) {
		super();
		this.requestContext = requestContext;
		this.countryService = countryService;
	}

	/**
	 * This method is used to retrieve list of available country(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all
	 *         countries
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getCountry")
	public ResponseEntity<Object> getCountry(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		LOGGER.info("at getCountry");
		return countryService.getCountry(userInfo);// .getNmastersitecode());
	}

	/**
	 * This method will is used to make a new entry to country table.
	 * 
	 * @param inputMap map object holding params ( country [Country] object holding
	 *                 details to be added in country table, userinfo [UserInfo]
	 *                 holding logged in user details ) Input:{ "country":
	 *                 {"nsitecode": -1,"scountryname":
	 *                 "Country-01","scountryshortname": "C01"}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": - 1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 5,"nmastersitecode": -1,"nmodulecode": 15,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"} }
	 * @return ResponseEntity with string message as 'Already Exists' if the country
	 *         already exists/ list of countries along with the newly added country.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createCountry")
	public ResponseEntity<Object> createCountry(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final Country objCountry = objmapper.convertValue(inputMap.get("country"), Country.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return countryService.createCountry(objCountry, userInfo);
	}

	/**
	 * This method is used to retrieve a specific country record for given site.
	 * 
	 * @param inputMap [Map] map object with "ncountrycode" and "userinfo" as keys
	 *                 for which the data is to be fetched Input:{ "ncountrycode":
	 *                 1, "userinfo":{ "activelanguagelist": ["en-US"],
	 *                 "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode":
	 *                 -1,"ndeputyuserrole": -1, "nformcode": 5,"nmastersitecode":
	 *                 -1,"nmodulecode": 15, "nreasoncode": 0,"nsitecode":
	 *                 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
	 *                 "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat":
	 *                 "dd/MM/yyyy HH:mm:ss", "sgmtoffset": "UTC
	 *                 +00:00","slanguagefilename": "Msg_en_US","slanguagename":
	 *                 "English", "slanguagetypecode": "en-US", "spgdatetimeformat":
	 *                 "dd/MM/yyyy HH24:mi:ss", "spgsitedatetime": "dd/MM/yyyy
	 *                 HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
	 *                 "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with Country object for the specified primary key /
	 *         with string message as 'Deleted' if the country record is not
	 *         available for given Site
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveCountryById")
	public ResponseEntity<Object> getActiveCountryById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int ncountryCode = (Integer) inputMap.get("ncountrycode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return countryService.getActiveCountryById(ncountryCode, userInfo);

	}

	/**
	 * This method is used to update selected country details.
	 * 
	 * @param inputMap [map object holding params( country [Country] object holding
	 *                 details to be updated in country table, userinfo [UserInfo]
	 *                 holding logged in user details) Input:{
	 *                 "country":{"ncountrycode": 5, "scountryname": "United States
	 *                 of America", "scountryshortname": "US", "stwocharcountry":
	 *                 "NA", "sthreecharcountry": "NA", "dmodifieddate": 1728620560,
	 *                 "nsitecode": -1, "nstatus": 1, "slno": 0, "smodifieddate":
	 *                 null}, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"],"isutcenabled": 4,"ndeptcode":
	 *                 -1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 5,"nmastersitecode": -1, "nmodulecode": 15, "nreasoncode":
	 *                 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode":
	 *                 1,"nusercode": -1, "nuserrole": -1,"nusersitecode":
	 *                 -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * 
	 * 
	 * @return ResponseEntity with string message as 'Already Deleted' if the
	 *         counytry record is not available/ list of all countries and along
	 *         with the updated country for given master site code.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateCountry")
	public ResponseEntity<Object> updateCountry(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final Country objCountry = objmapper.convertValue(inputMap.get("country"), Country.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return countryService.updateCountry(objCountry, userInfo);
	}

	/**
	 * This method is used to delete an entry in Country table
	 * 
	 * @param inputMap [Map] object with keys of Country entity and UserInfo object.
	 *                 Input:{ "country": {"ncountrycode": 5, "scountryname":
	 *                 "United States of America", "scountryshortname": "US",
	 *                 "stwocharcountry": "NA", "sthreecharcountry": "NA",
	 *                 "dmodifieddate": null, "nsitecode": -1, "nstatus": 1, "slno":
	 *                 0, "smodifieddate": "03/04/2025 16:37:33" }, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 5,"nmastersitecode": -1, "nmodulecode": 15,
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
	 *         country record is not available/ string message as 'Record is used
	 *         in....' when the country is associated in transaction / list of all
	 *         countries excluding the deleted record
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteCountry")
	public ResponseEntity<Object> deleteCountry(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final Country objCountry = objmapper.convertValue(inputMap.get("country"), Country.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return countryService.deleteCountry(objCountry, userInfo);
	}

	/**
	 * This method is used to retrieve list of available country(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all
	 *         countries for given site
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getBatchPoolCountry")
	public ResponseEntity<Object> getBatchPoolCountry(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return countryService.getBatchPoolCountry(userInfo);
	}

}
