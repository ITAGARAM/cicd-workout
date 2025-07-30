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
import com.agaramtech.qualis.submitter.model.City;
import com.agaramtech.qualis.submitter.service.city.CityService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/city")
public class CityController {

	private static final Logger LOGGER = LoggerFactory.getLogger(CityController.class);

	private RequestContext requestContext;
	private final CityService cityService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext RequestContext to hold the request
	 * @param cityService    CityService
	 */
	public CityController(RequestContext requestContext, CityService cityService) {
		super();
		this.requestContext = requestContext;
		this.cityService = cityService;
	}

	/**
	 * This method is used to retrieve list of available city(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all cities
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getCity")
	public ResponseEntity<Object> getCity(@RequestBody Map<String, Object> inputMap) throws Exception {
		LOGGER.info("getCity");
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return cityService.getCity(userInfo);
	}

	/**
	 * This method will is used to make a new entry to city table.
	 * 
	 * @param inputMap map object holding params ( city [City] object holding
	 *                 details to be added in city table, userinfo [UserInfo]
	 *                 holding logged in user details ) Input:{ "city": {
	 *                 "scityname": "cm", "scitycode": "cm", "ndistrictcode": 1 },
	 *                 "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 150,"nmastersitecode": -1,"nmodulecode": 70,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"} }
	 * @return ResponseEntity with string message as 'Already Exists' if the city
	 *         already exists/ list of cities along with the newly added city.
	 * @throws Exception exception
	 */
	@PostMapping("/createCity")
	public ResponseEntity<Object> createCity(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final City city = objMapper.convertValue(inputMap.get("city"), City.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return cityService.createCity(city, userInfo);
	}

	/**
	 * This method is used to retrieve a specific city record.
	 * 
	 * @param inputMap [Map] map object with "ncitycode" and "userinfo" as keys for
	 *                 which the data is to be fetched Input:{ "ncitycode": 1,
	 *                 "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 150,"nmastersitecode": -1,"nmodulecode": 70,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with City object for the specified primary key / with
	 *         string message as 'Deleted' if the city record is not available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveCityById")
	public ResponseEntity<Object> getActiveCityById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int ncitycode = (Integer) inputMap.get("ncitycode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return cityService.getActiveCityById(ncitycode, userInfo);
	}

	/**
	 * This method is used to update selected city details.
	 * 
	 * @param inputMap [map object holding params( city [City] object holding
	 *                 details to be updated in city table, userinfo [UserInfo]
	 *                 holding logged in user details) Input:{ "city":
	 *                 {"ncitycode":1,"scityname": "m","scitycode": "m",
	 *                 "ndistrictcode": 1}, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"],"isutcenabled": 4,"ndeptcode":
	 *                 -1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 150,"nmastersitecode": -1, "nmodulecode": 70, "nreasoncode":
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
	 * @return ResponseEntity with string message as 'Already Deleted' if the city
	 *         record is not available/ list of all cities and along with the
	 *         updated city.
	 * @throws Exception exception
	 */
	@PostMapping("/updateCity")
	public ResponseEntity<Object> updateCity(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final City city = objMapper.convertValue(inputMap.get("city"), City.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return cityService.updateCity(city, userInfo);
	}

	/**
	 * This method is used to delete an entry in city table
	 * 
	 * @param inputMap [Map] object with keys of City entity and UserInfo object.
	 *                 Input:{ "city": {"ncitycode":1}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 150,"nmastersitecode": -1, "nmodulecode":
	 *                 70, "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the city
	 *         record is not available/ string message as 'Record is used in....'
	 *         when the city is associated in transaction / list of all cities
	 *         excluding the deleted record
	 * @throws Exception exception
	 */
	@PostMapping("/deleteCity")
	public ResponseEntity<Object> deleteCity(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final City city = objMapper.convertValue(inputMap.get("city"), City.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return cityService.deleteCity(city, userInfo);
	}

	/**
	 * This method is used to retrieve list of available district(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all
	 *         districts
	 * @throws Exception exception
	 */
	@PostMapping("/getDistrict")
	public ResponseEntity<Object> getDistrict(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return cityService.getDistrict(userInfo);
	}

}
