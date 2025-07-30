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
import com.agaramtech.qualis.organization.model.Department;
import com.agaramtech.qualis.organization.service.department.DepartmentService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/department")
public class DepartmentController {

	private static final Logger LOGGER = LoggerFactory.getLogger(DepartmentController.class);

	private final DepartmentService departmentService;
	private RequestContext requestContext;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext    RequestContext to hold the request
	 * @param departmentService DepartmentService
	 */
	public DepartmentController(RequestContext requestContext,DepartmentService departmentService) {
		super();
		this.requestContext = requestContext;
		this.departmentService = departmentService;
	}

	/**
	 * This method is used to retrieve list of available department(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all
	 *         department
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getDepartment")
	public ResponseEntity<Object> getDepartment(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("getDepartment() called");
		requestContext.setUserInfo(userInfo);
		return departmentService.getDepartment(userInfo);
	}

	/**
	 * This method will is used to make a new entry to department table.
	 * 
	 * @param inputMap map object holding params ( department [Department] object
	 *                 holding details to be added in department table, userinfo
	 *                 [UserInfo] holding logged in user details ) Input:{
	 *                 "department": { "sdeptname": "QA"}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": - 1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 30,"nmastersitecode": -1,"nmodulecode": 4,
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
	 *         department already exists/ list of department along with the newly
	 *         added department.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createDepartment")
	public ResponseEntity<Object> createDepartment(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final Department department = objmapper.convertValue(inputMap.get("department"), Department.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return departmentService.createDepartment(department, userInfo);
	}

	/**
	 * This method is used to retrieve a specific department record.
	 * 
	 * @param inputMap [Map] map object with "ndeptcode" and "userinfo" as keys for
	 *                 which the data is to be fetched Input:{ "ndeptcode": 1,
	 *                 "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 30,"nmastersitecode": -1,"nmodulecode": 4,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with Department object for the specified primary key /
	 *         with string message as 'Deleted' if the department record is not
	 *         available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveDepartmentById")
	public ResponseEntity<Object> getActiveDepartmentById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int ndeptcode = (Integer) inputMap.get("ndeptcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return departmentService.getActiveDepartmentById(ndeptcode, userInfo);
	}

	/**
	 * This method is used to update selected department details.
	 * 
	 * @param inputMap [map object holding params( department [Department] object
	 *                 holding details to be updated in department table, userinfo
	 *                 [UserInfo] holding logged in user details) Input:{
	 *                 "department": {"ndeptcode":1,"sdeptname": "m",
	 *                 "sdescription": "m", "ndefaultstatus": 3 }, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 30,"nmastersitecode": -1, "nmodulecode": 4,
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
	 *         department record is not available/ list of all department and along
	 *         with the updated department.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateDepartment")
	public ResponseEntity<Object> updateDepartment(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final Department department = objmapper.convertValue(inputMap.get("department"), Department.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return departmentService.updateDepartment(department, userInfo);
	}

	/**
	 * This method is used to delete an entry in Department table
	 * 
	 * @param inputMap [Map] object with keys of Department entity and UserInfo
	 *                 object. Input:{ "department": {"ndeptcode":1}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 30,"nmastersitecode": -1, "nmodulecode": 4,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the record
	 *         is not available/ string message as 'Record is used in....' when the
	 *         department is associated in transaction / list of all department
	 *         excluding the deleted record
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteDepartment")
	public ResponseEntity<Object> deleteDepartment(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final Department department = objmapper.convertValue(inputMap.get("department"), Department.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return departmentService.deleteDepartment(department, userInfo);

	}
}
