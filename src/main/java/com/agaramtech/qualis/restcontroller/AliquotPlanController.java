package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.storagemanagement.service.aliquotplan.AliquotPlanService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * This controller is used to dispatch the input request to its relevant service methods
 * to access the AliquotPlan Service methods.
 */

@RestController
@RequestMapping("/aliquotplan")
public class AliquotPlanController {

	private RequestContext requestContext;
	private final AliquotPlanService aliquotPlanService;

	public AliquotPlanController(RequestContext requestContext, AliquotPlanService aliquotPlanService) {
		super();
		this.requestContext = requestContext;
		this.aliquotPlanService = aliquotPlanService;
	}

	/**
	 * This Method is used to get the over all aliquotplans with respect to site
	 *
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * Input : {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of aliquotplan with respect to site
	 *         and also have the HTTP response code
	 * @throws Exception
	 */

	@PostMapping(value = "/getAliquotPlan")
	public ResponseEntity<Object> getAliquotPlant(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return aliquotPlanService.getAliquotPlan(userInfo);

	}

	/**
	 * This Method is used to get the over all aliquotplans with respect to site
	 *
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * Input : {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of aliquotplan with respect to site
	 *         and also have the HTTP response code
	 * @throws Exception
	 */

	@PostMapping(value = "/getProjectType")
	public ResponseEntity<Object> getProjecttype(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return aliquotPlanService.getProjecttype(userInfo);
	}

	/**
	 * This Method is used to get the over all aliquotplans with respect to site
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * Input : {{sampletypevalue: 9, sampletypename: "Neonatal Sepsis"},"userinfo":{nmastersitecode": -1}}
	 * 	sampletypevalue value is nprojecttype primary key value.
	 * @return a response entity which holds the list of aliquotplan with respect to site
	 *         and also have the HTTP response code
	 * @throws Exception
	 */

	@PostMapping(value = "/getSampleType")
	public ResponseEntity<Object> getSampleType(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return aliquotPlanService.getSampleType(inputMap, userInfo);
	}

	/**
	 * This Method is used to get the over all aliquotplans with respect to site
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * Input : {{sampletypevalue: 9, sampletypename: "Neonatal Sepsis"},"userinfo":{nmastersitecode": -1}}
	 * 	sampletypevalue value is nprojecttype primary key value.
	 * @return a response entity which holds the list of aliquotplan with respect to site
	 *         and also have the HTTP response code
	 * @throws Exception
	 */

	@PostMapping(value = "/getCollectionTubeType")
	public ResponseEntity<Object> getCollectionTubeType(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return aliquotPlanService.getCollectionTubeType(inputMap, userInfo);
	}

	/**
	 * This Method is used to get the over all aliquotplans with respect to site
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * Input : {{sampletypevalue: 9, sampletypename: "Neonatal Sepsis"},"userinfo":{nmastersitecode": -1}}
	 * 	sampletypevalue value is nprojecttype primary key value.
	 * @return a response entity which holds the list of aliquotplan with respect to site
	 *         and also have the HTTP response code
	 * @throws Exception
	 */

	@PostMapping(value = "/getPatientCatgory")
	public ResponseEntity<Object> getPatientCatgory(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return aliquotPlanService.getPatientCatgory(inputMap, userInfo);
	}

	/**
	 * This Method is used to get the over all aliquotplans with respect to site
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * Input : {{sampletypevalue: 9, sampletypename: "Neonatal Sepsis"},"userinfo":{nmastersitecode": -1}}
	 * 	sampletypevalue value is nprojecttype primary key value.
	 * @return a response entity which holds the list of aliquotplan with respect to site
	 *         and also have the HTTP response code
	 * @throws Exception
	 */

	@PostMapping(value = "/getVisitName")
	public ResponseEntity<Object> getVisitName(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return aliquotPlanService.getVisitName(inputMap, userInfo);
	}

	/**
	 * This Method is used to get the over all aliquotplans with respect to site
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * Input : {"userinfo":{nmastersitecode": -1}}
	 * 	sampletypevalue value is nprojecttype primary key value.
	 * @return a response entity which holds the list of aliquotplan with respect to site
	 *         and also have the HTTP response code
	 * @throws Exception
	 */

	@PostMapping(value = "/getUnit")
	public ResponseEntity<Object> getUnit(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return aliquotPlanService.getUnit(inputMap, userInfo);
	}

	/**
	 * This Method is used to get the over all aliquotplans with respect to site
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * Input : {{sampletypevalue: 9, sampletypename: "Neonatal Sepsis"},"userinfo":{nmastersitecode": -1}}
	 * 	sampletypevalue value is nprojecttype primary key value.
	 * @return a response entity which holds the list of aliquotplan with respect to site
	 *         and also have the HTTP response code
	 * @throws Exception
	 */

	// ALPD-5513 - added by Gowtham R on 10/3/25 - added sample donor field from sampledonor table
	@PostMapping(value = "/getSampleDonor")
	public ResponseEntity<Object> getSampleDonor(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return aliquotPlanService.getSampleDonor(inputMap, userInfo);
	}

	/**
	 * This method is used to add a new entry to aliquotplan table.
	 * @param inputMap [Map] holds the aliquotplan object to be inserted
	 * Input : {ncontrolCode:234,nsampledonorcode:1,operation:"create",patientcatvalue:-1,productname:"Blood",productvalue:1,projecttypename:"PTB",
	projecttypevalue:1,saliquotno:"09",sdescription:"Rak",squantity:"34.09",ssampledonor:"Mother",tubename:"EDTA",tubevalue:1,
	unitname:"nos",unitvalue:6,visitname:"26-28 weeks (Visit-3)",visitnumber:3	,"userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -   1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 234,"nmastersitecode": -1,"nmodulecode": 71,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",  "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return inserted aliquotplan object and HTTP Status on successive insert otherwise
	 *         corresponding HTTP Status
	 * @throws Exception
	 */

	@PostMapping(value = "/createAliquotPlan")
	public ResponseEntity<Object> createAliquotPlan(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return aliquotPlanService.createAliquotPlan(inputMap, userInfo);
	}

	/**
	 * This method is used to retrieve active aliquotplan object based on the specified naliquotplanCode.
	 * @param naliquotplanCode [int] primary key of aliquotplan object
	 * Input : {naliquotplancode:1,"userinfo":{nmastersitecode": -1}}
	 * @return response entity  object holding response status and data of aliquotplan object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@PostMapping(value = "/getActiveAliquotPlanById")
	public ResponseEntity<Object> getActiveAliquotPlanById(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final int naliquotplancode = (Integer) inputMap.get("naliquotplancode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return aliquotPlanService.getActiveAliquotPlanById(naliquotplancode, userInfo);

	}

	/**
	 * This method is used to add a new entry to aliquotplan table.
	 * @param inputMap [Map] holds the aliquotplan object to be inserted
	 * Input : {ncontrolCode:234,naliquotplancode:1,nsampledonorcode:1,operation:"update",patientcatvalue:-1,productname:"Blood",productvalue:1,projecttypename:"PTB",
		projecttypevalue:1,saliquotno:"09",sdescription:"Rak",squantity:"34.09",ssampledonor:"Mother",tubename:"EDTA",tubevalue:1,
		unitname:"nos",unitvalue:6,visitname:"26-28 weeks (Visit-3)",visitnumber:3	,"userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -   1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 234,"nmastersitecode": -1,"nmodulecode": 71,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss","spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",  "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return inserted aliquotplan object and HTTP Status on successive insert otherwise
	 *         corresponding HTTP Status
	 * @throws Exception
	 */

	@PostMapping(value = "/updateAliquotPlan")
	public ResponseEntity<Object> updateAliquotPlan(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return aliquotPlanService.updateAliquotPlan(inputMap, userInfo);
	}

	/**
	 * This method is used to add a new entry to aliquotplan table.
	 * @param inputMap [Map] holds the aliquotplan object to be inserted
	 * Input : {ncontrolCode:234,naliquotplancode:1,nsampledonorcode:1,operation:"update",patientcatvalue:-1,productname:"Blood",productvalue:1,projecttypename:"PTB",
		projecttypevalue:1,saliquotno:"09",sdescription:"Rak",squantity:"34.09",ssampledonor:"Mother",tubename:"EDTA",tubevalue:1,
		unitname:"nos",unitvalue:6,visitname:"26-28 weeks (Visit-3)",visitnumber:3	,"userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -   1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 234,"nmastersitecode": -1,"nmodulecode": 71,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss","spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",  "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return inserted aliquotplan object and HTTP Status on successive insert otherwise
	 *         corresponding HTTP Status
	 * @throws Exception
	 */

	@PostMapping(value = "/deleteAliquotPlan")
	public ResponseEntity<Object> deleteAliquotPlan(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return aliquotPlanService.deleteAliquotPlan(inputMap, userInfo);
	}



}
