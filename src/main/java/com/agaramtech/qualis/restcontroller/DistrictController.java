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
import com.agaramtech.qualis.submitter.model.District;
import com.agaramtech.qualis.submitter.service.district.DistrictService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller is used to dispatch the input request to its relevant service
 * methods to access the District Service methods.
 */
@RestController
@RequestMapping("/district")
public class DistrictController {

	private static final Logger LOGGER = LoggerFactory.getLogger(UnitController.class);

	private RequestContext requestContext;
	private final DistrictService districtService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext  RequestContext to hold the request
	 * @param districtService DistrictService
	 */
	public DistrictController(RequestContext requestContext, DistrictService districtService) {
		super();
		this.requestContext = requestContext;
		this.districtService = districtService;
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
	@PostMapping(value = "/getDistrict")
	public ResponseEntity<Object> getDistrict(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return districtService.getDistrict(userInfo);
	}

	/**
	 * This method is used to get the single record in district table
	 * 
	 * @param inputMap [Map] holds the ndistrictcode code to get
	 * @return response entity object holding response status and data of single
	 *         district object
	 * @throws Exception
	 */
	@PostMapping(value = "/getActiveDistrictById")
	public ResponseEntity<Object> getActiveDistrictById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int ndistrictcode = (Integer) inputMap.get("ndistrictcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return districtService.getActiveDistrictById(ndistrictcode, userInfo);
	}

	/**
	 * This method will is used to make a new entry to district table.
	 * 
	 * @param inputMap map object holding params ( district [District] object
	 *                 holding details to be added in district table, userinfo
	 *                 [UserInfo] holding logged in user details ) Input:{
	 *                 "district": { "sdistrictname": "cm", "sdistrictcode":
	 *                 "112","nregioncode":"2" }, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"], "isutcenabled": 4,"ndeptcode": -
	 *                 1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 161,"nmastersitecode": -1,"nmodulecode": 1, "nreasoncode":
	 *                 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode":
	 *                 1,"nusercode": -1, "nuserrole": -1,"nusersitecode":
	 *                 -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"} }
	 * @return ResponseEntity with string message as 'Already Exists' if the
	 *         district already exists/ list of districts along with the newly added
	 *         district.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createDistrict")
	public ResponseEntity<Object> createDistrict(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		final District district = objMapper.convertValue(inputMap.get("district"), District.class);
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return districtService.createDistrict(district, userInfo);
	}

	/**
	 * This method is used to update selected district details.
	 * 
	 * @param inputMap [map object holding params( district [District] object
	 *                 holding details to be updated in district table, userinfo
	 *                 [UserInfo] holding logged in user details) Input:{
	 *                 "district": {"ndistrictcode":1,"sdistrictname":
	 *                 "m","sdistrictcode": "m", , "nregioncode": 3 }, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 161,"nmastersitecode": -1, "nmodulecode": 1,
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
	 *         district record is not available/ list of all district and along with
	 *         the updated district.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateDistrict")
	public ResponseEntity<Object> updateDistrict(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		final District district = objMapper.convertValue(inputMap.get("district"), District.class);
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return districtService.updateDistrict(district, userInfo);
	}

	/**
	 * This method is used to delete an entry in District table
	 * 
	 * @param inputMap [Map] object with keys of District entity and UserInfo
	 *                 object. Input:{ "district": {"ndistrictcode":1}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 161,"nmastersitecode": -1, "nmodulecode": 1,
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
	 *         district record is not available/ string message as 'Record is used
	 *         in....' when the district is associated in transaction / list of all
	 *         districts excluding the deleted record
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteDistrict")
	public ResponseEntity<Object> deleteDistrict(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		final District district = objMapper.convertValue(inputMap.get("district"), District.class);
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return districtService.deleteDistrict(district, userInfo);
	}

}
