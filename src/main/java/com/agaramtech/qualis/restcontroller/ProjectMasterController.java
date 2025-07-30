package com.agaramtech.qualis.restcontroller;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.project.model.ProjectMaster;
import com.agaramtech.qualis.project.model.ProjectMasterFile;
import com.agaramtech.qualis.project.model.ProjectMember;
import com.agaramtech.qualis.project.model.ProjectQuotation;
import com.agaramtech.qualis.project.model.ReportInfoProject;
import com.agaramtech.qualis.project.service.projectmaster.ProjectMasterService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the Project Master Service methods
 * 
 * @author ATE172 (Ragul C)
 * @version 9.0.0.1
 */

@RestController
@RequestMapping("/projectmaster")
public class ProjectMasterController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectMasterController.class);
	private RequestContext requestContext;
	private final ProjectMasterService projectmasterService;

	public ProjectMasterController(RequestContext requestContext, ProjectMasterService projectmasterService) {
		super();
		this.requestContext = requestContext;
		this.projectmasterService = projectmasterService;
	}

	@PostMapping(value = "/getProjectMaster")
	public ResponseEntity<Object> getProjectMaster(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		LOGGER.info("getProjectMaster() called");
		requestContext.setUserInfo(userInfo);
		return projectmasterService.getProjectMaster(userInfo);

	}

	@PostMapping(value = "/getProjectMasterById")
	public ResponseEntity<Object> getProjectMasterById(@RequestBody Map<String, Object> inputMap) throws Exception {

		final int nprojectmastercode = (int) inputMap.get("nprojectmastercode");
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return projectmasterService.getProjectMasterById(nprojectmastercode, userInfo);

	}

	@PostMapping(value = "/createProjectMaster")
	public ResponseEntity<Object> createProjectMaster(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final ProjectMaster projectMaster = objmapper.convertValue(inputMap.get("projectMaster"), ProjectMaster.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return projectmasterService.createProjectMaster(projectMaster, userInfo);
	}

	@PostMapping(value = "/updateProjectMaster")
	public ResponseEntity<? extends Object> updateProjectMaster(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final ProjectMaster projectmaster = objMapper.convertValue(inputMap.get("projectMaster"), ProjectMaster.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return projectmasterService.updateProjectMaster(projectmaster, userInfo);

	}

	@PostMapping(value = "/closureProjectMasterFile")
	public ResponseEntity<Object> closureProjectMasterClosureFile(MultipartHttpServletRequest request)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return projectmasterService.closureProjectMasterFile(request, userInfo);
	}

	@PostMapping(value = "/getActiveProjectMasterById")
	public ResponseEntity<Object> getActiveProjectMasterById(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objectMapper = new ObjectMapper();
		final int nprojectmastercode = (Integer) inputMap.get("nprojectmastercode");
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return projectmasterService.getActiveProjectMasterById(nprojectmastercode, userInfo);

	}

	@PostMapping(value = "/deleteProjectMaster")
	public ResponseEntity<Object> deleteProjectMaster(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final ProjectMaster projectmaster = objmapper.convertValue(inputMap.get("projectmaster"), ProjectMaster.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);

		return projectmasterService.deleteProjectMaster(projectmaster, userInfo);
	}

	@PostMapping(value = "/getProjectMasterByProjectType")
	public ResponseEntity<Object> getProjectMasterByProjectType(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		Integer nprojecttypecode = null;
		if (inputMap.get("nprojecttypecode") != null) {
			nprojecttypecode = (Integer) inputMap.get("nprojecttypecode");
		}
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return projectmasterService.getProjectMasterByProjectType(nprojecttypecode, userInfo);

	}

	@PostMapping(value = "/getStudyDirector")
	public ResponseEntity<Object> getStudyDirector(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return projectmasterService.getStudyDirector(userInfo);

	}

	@PostMapping(value = "/getTeammembers")
	public ResponseEntity<Object> getTeammembers(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return projectmasterService.getTeammembers(userInfo);
	}

	@PostMapping(value = "/getPeriodByControl")
	public ResponseEntity<Object> getPeriodByControl(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();

		Integer ncontrolCode = null;
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		if (inputMap.get("ncontrolcode") != null) {
			ncontrolCode = (Integer) inputMap.get("ncontrolcode");
		}
		return projectmasterService.getPeriodByControl(ncontrolCode, userInfo);
	}

	@PostMapping(value = "/approveProjectMaster")
	public ResponseEntity<Object> approveProjectMaster(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		final ProjectMaster approveProjectMaster = objectMapper.convertValue(inputMap.get("projectmaster"),
				ProjectMaster.class);
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		int nautoprojectcode = (int) inputMap.get("nautoprojectcode");
		return projectmasterService.approveProjectMaster(approveProjectMaster, userInfo, nautoprojectcode);
	}

	@PostMapping(value = "/getProjectUnmappedTeammember")
	public ResponseEntity<Object> getProjectUnmappedTeammember(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objectMapper = new ObjectMapper();
		final Integer nprojectmastercode = (Integer) inputMap.get("nprojectmastercode");
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return projectmasterService.getProjectUnmappedTeammember(nprojectmastercode, userInfo);
	}

	@PostMapping(value = "/createProjectMember")
	public ResponseEntity<Object> createProjectMember(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final List<ProjectMember> ProjectMember = objmapper.convertValue(inputMap.get("ProjectMember"),
				new TypeReference<List<ProjectMember>>() {
				});
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return projectmasterService.createProjectMember(ProjectMember, userInfo);
	}

	@PostMapping(value = "/deleteProjectMember")
	public ResponseEntity<Object> deleteProjectMember(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objectMapper = new ObjectMapper();
		final ProjectMember projectMember = objectMapper.convertValue(inputMap.get("projectmember"),
				ProjectMember.class);
		final Integer nprojectmembercode = (Integer) inputMap.get("nprojectmembercode");
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return projectmasterService.deleteProjectMember(projectMember, nprojectmembercode, userInfo);

	}

	@PostMapping(value = "/retireProjectMaster")
	public ResponseEntity<Object> retireProjectMaster(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		final ProjectMaster retireProjectMaster = objectMapper.convertValue(inputMap.get("projectmaster"),
				ProjectMaster.class);
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return projectmasterService.retireProjectMaster(retireProjectMaster, userInfo);
	}

	@PostMapping(value = "/createProjectMasterFile")
	public ResponseEntity<Object> createProjectMasterFile(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return projectmasterService.createProjectMasterFile(request, userInfo);
	}

	@PostMapping(value = "/updateProjectMasterFile")
	public ResponseEntity<Object> updateProjectMasterFile(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return projectmasterService.updateProjectMasterFile(request, userInfo);
	}

	@PostMapping(value = "/deleteProjectMasterFile")
	public ResponseEntity<Object> deleteProjectMasterFile(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final ProjectMasterFile objProjectMasterFile = objMapper.convertValue(inputMap.get("projectmasterfile"),
				ProjectMasterFile.class);
		requestContext.setUserInfo(userInfo);
		return projectmasterService.deleteProjectMasterFile(objProjectMasterFile, userInfo);
	}

	@PostMapping(value = "/editProjectMasterFile")
	public ResponseEntity<Object> editProjectMasterFile(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final ProjectMasterFile objProjectMasterFile = objMapper.convertValue(inputMap.get("projectmasterfile"),
				ProjectMasterFile.class);
		requestContext.setUserInfo(userInfo);
		return projectmasterService.editProjectMasterFile(objProjectMasterFile, userInfo);
	}

	@PostMapping(value = "/viewAttachedProjectMasterFile")
	public ResponseEntity<Object> viewAttachedProjectMasterFile(@RequestBody Map<String, Object> inputMap,
			HttpServletResponse response) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final ProjectMasterFile objProjectMasterFile = objMapper.convertValue(inputMap.get("projectmasterfile"),
				ProjectMasterFile.class);
		requestContext.setUserInfo(userInfo);
		return projectmasterService.viewAttachedProjectMasterFile(objProjectMasterFile, userInfo);
	}

	@PostMapping(value = "/getUserrole")
	public ResponseEntity<Object> getUserrole(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return projectmasterService.getUserrole(userInfo);
	}

	@PostMapping(value = "/getUsers")
	public ResponseEntity<Object> getUsers(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		Integer nuserrolecode = 0;
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		if ((inputMap.get("nuserrolecode")) != null) {
			nuserrolecode = (Integer) inputMap.get("nuserrolecode");
		}
		return projectmasterService.getUsers(nuserrolecode, userInfo);
	}

	@PostMapping(value = "/getApprovedProjectByProjectType")
	public ResponseEntity<Object> getApprovedProjectByProjectType(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		final int projectTypeCode = (Integer) inputMap.get("nprojecttypecode");
		return projectmasterService.getApprovedProjectByProjectType(projectTypeCode, userInfo);
	}

	@PostMapping(value = "/getApprovedProjectMasterByProjectType")
	public ResponseEntity<Object> getApprovedProjectMasterByProjectType(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		Integer nprojecttypecode = null;
		if (inputMap.get("nprojecttypecode") != null) {
			nprojecttypecode = (Integer) inputMap.get("nprojecttypecode");
		}
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return projectmasterService.getApprovedProjectMasterByProjectType(nprojecttypecode, userInfo);

	}

	@PostMapping(value = "/viewAttachedProjectMasterClosureFile")
	public ResponseEntity<Object> viewAttachedProjectMasterClosureFile(@RequestBody Map<String, Object> inputMap,
			HttpServletResponse response) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final ProjectMaster objProjectMaster = objMapper.convertValue(inputMap.get("projectmasterclosurefile"),
				ProjectMaster.class);
		requestContext.setUserInfo(userInfo);
		return projectmasterService.viewAttachedProjectMasterClosureFile(objProjectMaster, userInfo);

	}

	@PostMapping(value = "/getEditReportInfoProject")
	public ResponseEntity<Object> getEditProjectReportInfo(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return projectmasterService.getEditReportInfoProject(inputMap, userInfo);
	}

	@PostMapping(value = "/updateReportInfoProject")
	public ResponseEntity<? extends Object> updateProjectReportInfo(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final ReportInfoProject objReportInfoProject = objMapper.convertValue(inputMap.get("reportinfoproject"),
				ReportInfoProject.class);
		requestContext.setUserInfo(userInfo);
		return projectmasterService.updateReportInfoProject(objReportInfoProject, userInfo);
	}

	@PostMapping(value = "/getQuotaionNoByClient")
	public ResponseEntity<Object> getQuotaionNoByClient(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int nclientcatcode = (int) inputMap.get("nclientcatcode");
		final int nclientcode = (int) inputMap.get("nclientcode");
		requestContext.setUserInfo(userInfo);
		return projectmasterService.getQuotaionNoByClient(nclientcatcode, nclientcode, userInfo);
	}

	@PostMapping(value = "/completeProjectMaster")
	public ResponseEntity<Object> completeProjectMaster(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		final ProjectMaster approveProjectMaster = objectMapper.convertValue(inputMap.get("projectmaster"),
				ProjectMaster.class);
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return projectmasterService.completeProjectMaster(approveProjectMaster, userInfo);
	}

	@PostMapping(value = "/getReportTemplate")
	public ResponseEntity<Object> getReportTemplate(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return projectmasterService.getReportTemplate(userInfo);
	}

	@PostMapping(value = "/getActiveReportTemplate")
	public ResponseEntity<Object> getActiveReportTemplate(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		int nprojectmastercode = (int) inputMap.get("nprojectmastercode");
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return projectmasterService.getActiveReportTemplate(nprojectmastercode, userInfo);
	}

	@PostMapping(value = "/getAvailableProjectQuotation")
	public ResponseEntity<Object> getAvailableProjectQuotation(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		int nprojectmastercode = (int) inputMap.get("nprojectmastercode");
		requestContext.setUserInfo(userInfo);
		return projectmasterService.getAvailableProjectQuotation(nprojectmastercode, userInfo);
	}

	@PostMapping(value = "/createProjectQuotation")
	public ResponseEntity<Object> createProjectQuotation(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final ProjectQuotation projectQuotation = objmapper.convertValue(inputMap.get("ProjectQuotation"),
				new TypeReference<ProjectQuotation>() {
				});
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return projectmasterService.createProjectQuotation(projectQuotation, userInfo);
	}

	@PostMapping(value = "/deleteProjectQuotation")
	public ResponseEntity<Object> deleteProjectQuotation(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		final ProjectQuotation projectQuotation = objectMapper.convertValue(inputMap.get("ProjectQuotation"),
				ProjectQuotation.class);
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return projectmasterService.deleteProjectQuotation(projectQuotation, userInfo);
	}

	@PostMapping(value = "/getActiveProjectQuotationById")
	public ResponseEntity<Object> getActiveProjectQuotationById(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final int nprojectquotationcode = (Integer) inputMap.get("nprojectquotationcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return projectmasterService.getActiveProjectQuotationById(nprojectquotationcode, userInfo);
	}

	@PostMapping(value = "/updateProjectQuotation")
	public ResponseEntity<Object> updateProjectQuotation(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final ProjectQuotation objProjectQuotation = objmapper.convertValue(inputMap.get("ProjectQuotation"),
				new TypeReference<ProjectQuotation>() {
				});
		requestContext.setUserInfo(userInfo);
		return projectmasterService.updateProjectQuotation(objProjectQuotation, userInfo);
	}

	@PostMapping(value = "/getQuotation")
	public ResponseEntity<Object> getQuotation(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return projectmasterService.getQuotation(userInfo);
	}
}
