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
import com.agaramtech.qualis.submitter.model.InstitutionDepartment;
import com.agaramtech.qualis.submitter.service.institutiondepartment.InstitutionDepartmentService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the InstitutionDepartment Service methods
 */

@RestController
@RequestMapping("/institutiondepartment")
public class InstitutionDepartmentController {

	private static final Logger LOGGER = LoggerFactory.getLogger(InstitutionDepartmentController.class);

	private RequestContext requestContext;
	private final InstitutionDepartmentService institutionDepartmentService;

	public InstitutionDepartmentController(RequestContext requestContext,
			InstitutionDepartmentService institutionDepartmentService) {
		super();
		this.requestContext = requestContext;
		this.institutionDepartmentService = institutionDepartmentService;
	}

	/**
	 * This method is used to retrieve list of available institutionDepartment(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all
	 *         institutionDepartments
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getInstitutionDepartment")
	public ResponseEntity<Object> getInstitutionDepartment(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("getInstitutionDepartment called");
		requestContext.setUserInfo(userInfo);
		return institutionDepartmentService.getInstitutionDepartment(userInfo);
	}

	/**
	 * This method will is used to make a new entry to institutionDepartment table.
	 * 
	 * @param inputMap map object holding params ( institutionDepartment
	 *                 [InstitutionDepartment] object holding details to be added in
	 *                 institutionDepartment table, userinfo [UserInfo] holding
	 *                 logged in user details ) Input:{ "institutionDepartment": {
	 *                 "sinstitutionDepartmentname": "cm"}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": - 1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 149,"nmastersitecode": -1,"nmodulecode": 70,
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
	 *         institutionDepartment already exists/ list of institutionDepartments
	 *         along with the newly added institutionDepartment.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createInstitutionDepartment")
	public ResponseEntity<Object> createInstitutionDepartment(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final InstitutionDepartment objInstitutionDepartment = objmapper
				.convertValue(inputMap.get("institutiondepartment"), InstitutionDepartment.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return institutionDepartmentService.createInstitutionDepartment(objInstitutionDepartment, userInfo);
	}

	/**
	 * This method is used to retrieve a specific institutionDepartment record.
	 * 
	 * @param inputMap [Map] map object with "ninstitutionDepartmentcode" and
	 *                 "userinfo" as keys for which the data is to be fetched
	 *                 Input:{ "ninstitutionDepartmentcode": 1, "userinfo":{
	 *                 "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 149,"nmastersitecode": -1,"nmodulecode": 70,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with InstitutionDepartment object for the specified
	 *         primary key / with string message as 'Deleted' if the
	 *         institutionDepartment record is not available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveInstitutionDepartmentById")
	public ResponseEntity<Object> getActiveInstitutionDepartmentById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int ninstitutiondeptcode = (Integer) inputMap.get("ninstitutiondeptcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return institutionDepartmentService.getActiveInstitutionDepartmentById(ninstitutiondeptcode, userInfo);
	}

	/**
	 * This method is used to update selected institutionDepartment details.
	 * 
	 * @param inputMap [map object holding params( institutionDepartment
	 *                 [InstitutionDepartment] object holding details to be updated
	 *                 in institutionDepartment table, userinfo [UserInfo] holding
	 *                 logged in user details) Input:{ "institutionDepartment":
	 *                 {"ninstitutionDepartmentcode":1,"sinstitutionDepartmentname":
	 *                 "m", "sdescription": "m", "ndefaultstatus": 3 }, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 149,"nmastersitecode": -1, "nmodulecode":
	 *                 70, "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
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
	 *         institutionDepartment record is not available/ list of all
	 *         institutionDepartments and along with the updated
	 *         institutionDepartment.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateInstitutionDepartment")
	public ResponseEntity<Object> updateInstitutionDepartment(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final InstitutionDepartment objInstitutionDepartment = objmapper
				.convertValue(inputMap.get("institutiondepartment"), InstitutionDepartment.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return institutionDepartmentService.updateInstitutionDepartment(objInstitutionDepartment, userInfo);
	}

	/**
	 * This method is used to delete an entry in InstitutionDepartment table
	 * 
	 * @param inputMap [Map] object with keys of InstitutionDepartment entity and
	 *                 UserInfo object. Input:{ "institutionDepartment":
	 *                 {"ninstitutionDepartmentcode":1}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 149,"nmastersitecode": -1, "nmodulecode":
	 *                 70, "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the
	 *         institutionDepartment record is not available/ string message as
	 *         'Record is used in....' when the institutionDepartment is associated
	 *         in transaction / list of all institutionDepartments excluding the
	 *         deleted record
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteInstitutionDepartment")
	public ResponseEntity<Object> deleteInstitutionDepartment(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final InstitutionDepartment objInstitutionDepartment = objmapper
				.convertValue(inputMap.get("institutiondepartment"), InstitutionDepartment.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return institutionDepartmentService.deleteInstitutionDepartment(objInstitutionDepartment, userInfo);

	}

}
