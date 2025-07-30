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
import com.agaramtech.qualis.organization.model.Lab;
import com.agaramtech.qualis.organization.service.lab.LabService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant service
 * methods to access the Lab Service methods.
 */
@RestController
@RequestMapping("/lab")
public class LabController {

	private static final Logger LOGGER = LoggerFactory.getLogger(LabController.class);
	private final LabService labService;
	private RequestContext requestContext;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext RequestContext to hold the request
	 * @param labService     LabService
	 */
	public LabController(LabService labService, RequestContext requestContext) {
		super();
		this.labService = labService;
		this.requestContext = requestContext;
	}

	/**
	 * This method is used to retrieve list of available Lab.
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all Labs
	 * @throws Exception exception
	 */
	@PostMapping("/getLab")
	public ResponseEntity<Object> getLab(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		LOGGER.info("Invoking getLab endpoint ... ");
		return labService.getLab(userInfo.getNmastersitecode());
	}

	/**
	 * This method will is used to make a new entry to Lab table.
	 * 
	 * @param inputMap map object holding params ( Lab [Lab] object holding details
	 *                 to be added in Lab table, userinfo [UserInfo] holding logged
	 *                 in user details ) Input:{ "Lab": { "slabname": "cm",
	 *                 "slabsynonym": "cm" }, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"], "isutcenabled": 4,"ndeptcode": -
	 *                 1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 32,"nmastersitecode": -1,"nmodulecode": 4, "nreasoncode":
	 *                 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode":
	 *                 1,"nusercode": -1, "nuserrole": -1,"nusersitecode":
	 *                 -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"} }
	 * @return ResponseEntity with string message as 'Already Exists' if the Lab
	 *         already exists/ list of Labs along with the newly added Lab.
	 * @throws Exception exception
	 */
	@PostMapping("/createLab")
	public ResponseEntity<Object> createLab(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final Lab lab = objmapper.convertValue(inputMap.get("lab"), Lab.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return labService.createLab(lab, userInfo);
	}

	/**
	 * This method is used to update selected Lab details.
	 * 
	 * @param inputMap [map object holding params( Lab [Lab] object holding details
	 *                 to be updated in Lab table, userinfo [UserInfo] holding
	 *                 logged in user details) Input:{ "Lab":
	 *                 {"nlabcode":1,"slabname": "m","slabsynonym": "m",
	 *                 "sdescription": "m", "ndefaultstatus": 3 }, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 32,"nmastersitecode": -1, "nmodulecode": 4,
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
	 * @return ResponseEntity with string message as 'Already Deleted' if the Lab
	 *         record is not available/ list of all units and along with the updated
	 *         Lab.
	 * @throws Exception exception
	 */
	@PostMapping("/updateLab")
	public ResponseEntity<Object> updateLab(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final Lab lab = objmapper.convertValue(inputMap.get("lab"), Lab.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return labService.updateLab(lab, userInfo);
	}

	/**
	 * This method is used to retrieve a specific Lab record.
	 * 
	 * @param inputMap [Map] map object with "nunitcode" and "userinfo" as keys for
	 *                 which the data is to be fetched Input:{ "nLabcode": 1,
	 *                 "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 32,"nmastersitecode": -1,"nmodulecode": 4,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with Lab object for the specified primary key / with
	 *         string message as 'Deleted' if the Lab record is not available
	 * @throws Exception exception
	 */
	@PostMapping("/getActiveLabById")
	public ResponseEntity<Object> getActiveLabById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nlabcode = (Integer) inputMap.get("nlabcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return labService.getActiveLabById(nlabcode, userInfo);
	}

	/**
	 * This method is used to delete an entry in Lab table
	 * 
	 * @param inputMap [Map] object with keys of Lab entity and UserInfo object.
	 *                 Input:{ " lab": {"n labcode":1}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 32,"nmastersitecode": -1, "nmodulecode": 4,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the unit
	 *         record is not available/ string message as 'Record is used in....'
	 *         when the Lab is associated in transaction / list of all Labs
	 *         excluding the deleted record
	 * @throws Exception exception
	 */
	@PostMapping("/deleteLab")
	public ResponseEntity<Object> deleteLab(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final Lab lab = objmapper.convertValue(inputMap.get("lab"), Lab.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return labService.deleteLab(lab, userInfo);
	}
}
