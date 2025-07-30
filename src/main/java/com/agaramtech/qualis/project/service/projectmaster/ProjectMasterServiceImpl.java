package com.agaramtech.qualis.project.service.projectmaster;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.project.model.ProjectMaster;
import com.agaramtech.qualis.project.model.ProjectMasterFile;
import com.agaramtech.qualis.project.model.ProjectMember;
import com.agaramtech.qualis.project.model.ProjectQuotation;
import com.agaramtech.qualis.project.model.ReportInfoProject;
import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class ProjectMasterServiceImpl implements ProjectMasterService {

	private final ProjectMasterDAO projectmasterDAO;
	private final CommonFunction commonFunction;

	@Override
	public ResponseEntity<Object> getProjectMaster(UserInfo objUserInfo) throws Exception {
		return projectmasterDAO.getProjectMaster(objUserInfo);
	}

	@Override
	public ResponseEntity<Object> getProjectMasterById(final int nprojectmastercode, final UserInfo objUserInfo)
			throws Exception {
		return projectmasterDAO.getProjectMasterById(nprojectmastercode, objUserInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createProjectMaster(ProjectMaster projectmaster, UserInfo userInfo) throws Exception {
		return projectmasterDAO.createProjectMaster(projectmaster, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteProjectMaster(ProjectMaster basemasterProjectMaster, UserInfo userInfo)
			throws Exception {
		return projectmasterDAO.deleteProjectMaster(basemasterProjectMaster, userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveProjectMasterById(int nprojectmastercode, UserInfo userInfo)
			throws Exception {
		Map<String, Object> projectmasterResponseEntity = new LinkedHashMap<String, Object>();
		projectmasterResponseEntity.putAll((Map<String, Object>) projectmasterDAO
				.getActiveProjectMasterById(nprojectmastercode, userInfo).getBody());
		if (projectmasterResponseEntity.get("SelectedProjectMaster") == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(projectmasterResponseEntity, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> getProjectMasterByProjectType(Integer nprojecttypecode, UserInfo userInfo)
			throws Exception {
		return projectmasterDAO.getProjectMasterByProjectType(nprojecttypecode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getStudyDirector(UserInfo userInfo) throws Exception {
		return projectmasterDAO.getStudyDirector(userInfo);
	}

	@Override
	public ResponseEntity<Object> getTeammembers(UserInfo userInfo) throws Exception {
		return projectmasterDAO.getTeammembers(userInfo);
	}

	@Override
	public ResponseEntity<Object> getPeriodByControl(Integer ncontrolCode, final UserInfo userInfo) throws Exception {
		return projectmasterDAO.getPeriodByControl(ncontrolCode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<? extends Object> updateProjectMaster(ProjectMaster projectmaster, UserInfo userInfo)
			throws Exception {
		return projectmasterDAO.updateProjectMaster(projectmaster, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> approveProjectMaster(ProjectMaster approveProjectMaster, UserInfo userInfo,
			int nautoprojectcode) throws Exception {
		return projectmasterDAO.approveProjectMaster(approveProjectMaster, userInfo, nautoprojectcode);
	}

	@Override
	public ResponseEntity<Object> getProjectUnmappedTeammember(Integer nprojectmastercode, UserInfo userInfo)
			throws Exception {
		return projectmasterDAO.getProjectUnmappedTeammember(nprojectmastercode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createProjectMember(List<ProjectMember> projectmember, UserInfo userInfo)
			throws Exception {
		return projectmasterDAO.createProjectMember(projectmember, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteProjectMember(ProjectMember projectMember, Integer nprojectmembercode,
			UserInfo userInfo) throws Exception {
		return projectmasterDAO.deleteProjectMember(projectMember, nprojectmembercode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> retireProjectMaster(ProjectMaster retireProjectMaster, UserInfo userInfo)
			throws Exception {
		return projectmasterDAO.retireProjectMaster(retireProjectMaster, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createProjectMasterFile(MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception {
		return projectmasterDAO.createProjectMasterFile(request, objUserInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateProjectMasterFile(MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception {
		return projectmasterDAO.updateProjectMasterFile(request, objUserInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteProjectMasterFile(final ProjectMasterFile objProjectMasterFile,
			final UserInfo objUserInfo) throws Exception {
		return projectmasterDAO.deleteProjectMasterFile(objProjectMasterFile, objUserInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> editProjectMasterFile(ProjectMasterFile objProjectMasterFile, UserInfo objUserInfo)
			throws Exception {
		return projectmasterDAO.editProjectMasterFile(objProjectMasterFile, objUserInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> viewAttachedProjectMasterFile(ProjectMasterFile objProjectMasterFile,
			UserInfo objUserInfo) throws Exception {
		return projectmasterDAO.viewAttachedProjectMasterFile(objProjectMasterFile, objUserInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> closureProjectMasterFile(MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception {
		return projectmasterDAO.closureProjectMasterFile(request, objUserInfo);
	}

	@Override
	public ResponseEntity<Object> getUserrole(UserInfo userInfo) throws Exception {
		return projectmasterDAO.getUserrole(userInfo);
	}

	@Override
	public ResponseEntity<Object> getUsers(Integer nuserrolecode, final UserInfo userInfo) throws Exception {
		return projectmasterDAO.getUsers(nuserrolecode, userInfo);
	}

	public ResponseEntity<Object> getApprovedProjectByProjectType(int projectTypeCode, UserInfo objUserInfo)
			throws Exception {
		return projectmasterDAO.getApprovedProjectByProjectType(projectTypeCode, objUserInfo);
	}

	@Override
	public ResponseEntity<Object> getApprovedProjectMasterByProjectType(Integer nprojecttypecode, UserInfo userInfo)
			throws Exception {
		return projectmasterDAO.getApprovedProjectMasterByProjectType(nprojecttypecode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> viewAttachedProjectMasterClosureFile(ProjectMaster objProjectMaster,
			UserInfo objUserInfo) throws Exception {
		return projectmasterDAO.viewAttachedProjectMasterClosureFile(objProjectMaster, objUserInfo);
	}

	@Override
	public ResponseEntity<Object> getEditReportInfoProject(Map<String, Object> inputMap, UserInfo objUserInfo)
			throws Exception {
		return projectmasterDAO.getEditReportInfoProject(inputMap, objUserInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<? extends Object> updateReportInfoProject(ReportInfoProject objReportInfoProject,
			UserInfo objUserInfo) throws Exception {
		return projectmasterDAO.updateReportInfoProject(objReportInfoProject, objUserInfo);
	}

	@Override
	public ResponseEntity<Object> getQuotaionNoByClient(int nclientcatcode, int nclientcode, UserInfo objUserInfo)
			throws Exception {
		return projectmasterDAO.getQuotaionNoByClient(nclientcatcode, nclientcode, objUserInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> completeProjectMaster(ProjectMaster approveProjectMaster, UserInfo userInfo)
			throws Exception {
		return projectmasterDAO.completeProjectMaster(approveProjectMaster, userInfo);
	}

	@Override
	public ResponseEntity<Object> getReportTemplate(UserInfo userInfo) throws Exception {
		return projectmasterDAO.getReportTemplate(userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveReportTemplate(int nprojectmastercode, UserInfo userInfo) throws Exception {
		return projectmasterDAO.getActiveReportTemplate(nprojectmastercode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getAvailableProjectQuotation(final int nprojectmastercode, final UserInfo objUserInfo)
			throws Exception {
		return projectmasterDAO.getAvailableProjectQuotation(nprojectmastercode, objUserInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createProjectQuotation(ProjectQuotation projectQuotation, UserInfo userInfo)
			throws Exception {
		return projectmasterDAO.createProjectQuotation(projectQuotation, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteProjectQuotation(ProjectQuotation projectQuotation, UserInfo userInfo)
			throws Exception {
		return projectmasterDAO.deleteProjectQuotation(projectQuotation, userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveProjectQuotationById(final int nprojectquotationcode,
			final UserInfo userInfo) throws Exception {
		final ProjectQuotation objProjectQuotation = (ProjectQuotation) projectmasterDAO
				.getActiveProjectQuotationById(nprojectquotationcode, userInfo);
		if (objProjectQuotation == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objProjectQuotation, HttpStatus.OK);
		}
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateProjectQuotation(ProjectQuotation projectQuotation, UserInfo userInfo)
			throws Exception {
		return projectmasterDAO.updateProjectQuotation(projectQuotation, userInfo);
	}

	@Override
	public ResponseEntity<Object> getQuotation(UserInfo userInfo) throws Exception {
		return projectmasterDAO.getQuotation(userInfo);
	}
}
