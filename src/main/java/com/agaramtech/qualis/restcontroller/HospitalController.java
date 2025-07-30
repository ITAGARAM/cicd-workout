
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
import com.agaramtech.qualis.project.model.Hospital;
import com.agaramtech.qualis.project.service.hospital.HospitalService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This class is used to perform CRUD Operation on "Hospital" table by
 * implementing methods from its interface.
 * 
 * 
 * @author Mullai Balaji.V BGSI-4 30/06/2025
 * @version
 */

@RestController
@RequestMapping("/hospital")
public class HospitalController {
	private static final Logger LOGGER = LoggerFactory.getLogger(HospitalController.class);

	private RequestContext requestContext;
	private final HospitalService hospitalService;

	public HospitalController(RequestContext requestContext, HospitalService hospitalService) {
		super();
		this.requestContext = requestContext;
		this.hospitalService = hospitalService;
	}

	/**
	 * This Method is used to get the over all hospital with respect to site
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of hospital with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception
	 */

	@PostMapping(value = "/getHospital")
	public ResponseEntity<Object> getHospital(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("getHospital called");
		requestContext.setUserInfo(userInfo);

		return hospitalService.getHospital(userInfo);

	}

	/**
	 * This method will is used to make a new entry to hospital table.
	 * 
	 * @param inputMap map object holding params ( hospital [Hospital] object
	 *                 holding details to be added in hospital table, userinfo
	 *                 [UserInfo] holding logged in user details ) Input:{
	 *                 "hospital": { "shospitalname": "corono" }, "userinfo":{
	 *                 "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": - 1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 258,"nmastersitecode": -1,"nmodulecode": 1,
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
	 *         hospital already exists/ list of hospitals along with the newly added
	 *         hospital.
	 * @throws Exception exception
	 */

	@PostMapping(value = "/createHospital")
	public ResponseEntity<Object> createHospital(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();

		final Hospital objHospital = objMapper.convertValue(inputMap.get("hospital"), new TypeReference<Hospital>() {
		});

		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return hospitalService.createHospital(objHospital, userInfo);
	}

	/**
	 * This method is used to update selected hospital details.
	 * 
	 * @param inputMap [map object holding params( hospital [hospital] object
	 *                 holding details to be updated in hospital table, userinfo
	 *                 [UserInfo] holding logged in user details) Input:{
	 *                 "hospital": {"nhospitalcode":1,"shospitalname":
	 *                 "m","shospitalsynonym": "m", "sdescription": "m",
	 *                 "ndefaultstatus": 3 }, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"],"isutcenabled": 4,"ndeptcode":
	 *                 -1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 258,"nmastersitecode": -1, "nmodulecode": 1, "nreasoncode":
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
	 *         hospital record is not available/ list of all hospitals and along
	 *         with the updated hospital.
	 * @throws Exception exception
	 */

	@PostMapping(value = "/updateHospital")
	public ResponseEntity<Object> updateHospital(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final Hospital objHospital = objMapper.convertValue(inputMap.get("hospital"), new TypeReference<Hospital>() {
		});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return hospitalService.updateHospital(objHospital, userInfo);
	}

	/**
	 * This method is used to delete an entry in hospital table
	 * 
	 * @param inputMap [Map] object with keys of hospital entity and UserInfo
	 *                 object. Input:{ "hospital": {"nhospitalcode":1}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 258,"nmastersitecode": -1, "nmodulecode": 1,
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
	 *         hospital record is not available/ string message as 'Record is used
	 *         in....' when the hospital is associated in transaction / list of all
	 *         hospitals excluding the deleted record
	 * @throws Exception exception
	 */

	@PostMapping(value = "/deleteHospital")
	public ResponseEntity<Object> deleteHospital(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final Hospital objHospital = objMapper.convertValue(inputMap.get("hospital"), new TypeReference<Hospital>() {
		});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return hospitalService.deleteHospital(objHospital, userInfo);
	}

	/**
	 * This method is used to retrieve a specific hospital record.
	 * 
	 * @param inputMap [Map] map object with "nhospitalcode" and "userinfo" as keys
	 *                 for which the data is to be fetched Input:{ "nhospitalcode":
	 *                 1, "userinfo":{ "activelanguagelist": ["en-US"],
	 *                 "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode":
	 *                 -1,"ndeputyuserrole": -1, "nformcode": 258,"nmastersitecode":
	 *                 -1,"nmodulecode": 1, "nreasoncode": 0,"nsitecode":
	 *                 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
	 *                 "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat":
	 *                 "dd/MM/yyyy HH:mm:ss", "sgmtoffset": "UTC
	 *                 +00:00","slanguagefilename": "Msg_en_US","slanguagename":
	 *                 "English", "slanguagetypecode": "en-US", "spgdatetimeformat":
	 *                 "dd/MM/yyyy HH24:mi:ss", "spgsitedatetime": "dd/MM/yyyy
	 *                 HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
	 *                 "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with hospital object for the specified primary key /
	 *         with string message as 'Deleted' if the hospital record is not
	 *         available
	 * @throws Exception exception
	 */

	@PostMapping(value = "/getActiveHospitalById")
	public ResponseEntity<Object> getActiveHospitalById(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final int nhospitalCode = (int) inputMap.get("nhospitalcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return hospitalService.getActiveHospitalById(nhospitalCode, userInfo);
	}

}
