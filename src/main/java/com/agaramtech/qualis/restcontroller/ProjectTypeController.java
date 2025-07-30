package com.agaramtech.qualis.restcontroller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.project.model.ProjectType;
import com.agaramtech.qualis.project.service.projecttype.ProjectTypeService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the ProjectType Service methods 
 */
@RestController
@RequestMapping("/projecttype")
public class ProjectTypeController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectTypeController.class);

	private RequestContext requestContext;
	private final ProjectTypeService projecttypeService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext     RequestContext to hold the request
	 * @param projecttypeService ProjectTypeService
	 */
	public ProjectTypeController(RequestContext requestContext, ProjectTypeService projecttypeService) {
		super();
		this.requestContext = requestContext;
		this.projecttypeService = projecttypeService;
	}

	/**
	 * This method is used to retrieve list of available projecttype(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all
	 *         projecttype
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getProjectType")
	public ResponseEntity<List<ProjectType>> getProjectType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("getProjectType called");
		requestContext.setUserInfo(userInfo);
		return projecttypeService.getProjectType(userInfo);
	}

	/**
	 * This method will is used to make a new entry to projecttype table.
	 * 
	 * @param inputMap map object holding params ( projecttype [ProjectType] object
	 *                 holding details to be added in projecttype table, userinfo
	 *                 [UserInfo] holding logged in user details ) Input:{
	 *                 "projecttype": { "sprojecttypename": "Research", "sdescription":
	 *                 "cm" }, "userinfo":{ "activelanguagelist": ["en-US"],
	 *                 "isutcenabled": 4,"ndeptcode": - 1,"ndeputyusercode":
	 *                 -1,"ndeputyuserrole": -1, "nformcode": 163,"nmastersitecode":
	 *                 -1,"nmodulecode": 54, "nreasoncode": 0,"nsitecode":
	 *                 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
	 *                 "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat":
	 *                 "dd/MM/yyyy HH:mm:ss", "sgmtoffset": "UTC
	 *                 +00:00","slanguagefilename": "Msg_en_US","slanguagename":
	 *                 "English", "slanguagetypecode": "en-US", "spgdatetimeformat":
	 *                 "dd/MM/yyyy HH24:mi:ss", "spgsitedatetime": "dd/MM/yyyy
	 *                 HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
	 *                 "ssitedatetime": "dd/MM/yyyy HH:mm:ss"} }
	 * @return ResponseEntity with string message as 'Already Exists' if the
	 *         projecttype already exists/ list of projecttype along with the newly
	 *         added projecttype.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createProjectType")
	public ResponseEntity<Object> createProjectType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final ProjectType projectType = objmapper.convertValue(inputMap.get("projecttype"), ProjectType.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return projecttypeService.createProjectType(projectType, userInfo);
	}

	/**
	 * This method is used to retrieve a specific projecttype record.
	 * 
	 * @param inputMap [Map] map object with "nprojecttypecode" and "userinfo" as
	 *                 keys for which the data is to be fetched Input:{
	 *                 "nprojecttypecode": 1, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"], "isutcenabled": 4,"ndeptcode":
	 *                 -1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 163,"nmastersitecode": -1,"nmodulecode": 54, "nreasoncode":
	 *                 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode":
	 *                 1,"nusercode": -1, "nuserrole": -1,"nusersitecode":
	 *                 -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with Unit object for the specified primary key / with
	 *         string message as 'Deleted' if the projectType record is not
	 *         available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveProjectTypeById")
	public ResponseEntity<Object> getActiveProjectTypeById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nprojecttypecode = (int) inputMap.get("nprojecttypecode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return projecttypeService.getActiveProjectTypeById(nprojecttypecode, userInfo);
	}

	/**
	 * This method is used to update selected projecttype details.
	 * 
	 * @param inputMap [map object holding params( projecttype [ProjectType] object
	 *                 holding details to be updated in projectType table, userinfo
	 *                 [UserInfo] holding logged in user details) Input:{
	 *                 "projectType": {"nprojecttypecode":1,"sprojecttypename": "Research",
	 *                 "sdescription": "m"}, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"],"isutcenabled": 4,"ndeptcode":
	 *                 -1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 163,"nmastersitecode": -1, "nmodulecode": 54, "nreasoncode":
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
	 *         projectType record is not available/ list of all projectTypes and
	 *         along with the updated projectType.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateProjectType")
	public ResponseEntity<Object> updateProjectType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final ProjectType projecttype = objmapper.convertValue(inputMap.get("projecttype"), ProjectType.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return projecttypeService.updateProjectType(projecttype, userInfo);
	}

	/**
	 * This method is used to delete an entry in ProjectType table
	 * 
	 * @param inputMap [Map] object with keys of ProjectType entity and UserInfo
	 *                 object. Input:{ "projectType": {"nprojecttypecode":1},
	 *                 "userinfo":{ "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 163,"nmastersitecode": -1, "nmodulecode":
	 *                 54, "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the
	 *         projectType record is not available/ string message as 'Record is
	 *         used in....' when the projectType is associated in transaction / list
	 *         of all projectType excluding the deleted record
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteProjectType")
	public ResponseEntity<Object> deleteProjectType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final ProjectType projecttype = objmapper.convertValue(inputMap.get("projecttype"), ProjectType.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return projecttypeService.deleteProjectType(projecttype, userInfo);
	}
}
