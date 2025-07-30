package com.agaramtech.qualis.project.service.projectmaster;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.project.model.ProjectMaster;
import com.agaramtech.qualis.project.model.ProjectMasterFile;
import com.agaramtech.qualis.project.model.ProjectMember;
import com.agaramtech.qualis.project.model.ProjectQuotation;
import com.agaramtech.qualis.project.model.ProjectType;
import com.agaramtech.qualis.project.model.ReportInfoProject;

public interface ProjectMasterDAO {

	public ResponseEntity<Object> getProjectMaster(final UserInfo objUserInfo) throws Exception;

	public ResponseEntity<Object> createProjectMaster(ProjectMaster projectmaster, UserInfo userInfo) throws Exception;

	public ResponseEntity<Map<String, Object>> getActiveProjectMasterById(final int nprojectmastercode,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteProjectMaster(ProjectMaster basemasterProjectMaster, UserInfo userInfo)
			throws Exception;

	public List<ProjectType> getProjectTypeForProjectMaster(UserInfo objUserInfo) throws Exception;

	public ResponseEntity<Object> getProjectMasterByProjectType(Integer nprojecttypecode, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getStudyDirector(UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getTeammembers(UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getPeriodByControl(Integer ncontrolCode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<? extends Object> updateProjectMaster(ProjectMaster projectmaster, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> approveProjectMaster(ProjectMaster approveProjectMaster, UserInfo userInfo,
			int nautoprojectcode) throws Exception;

	public ResponseEntity<Object> getProjectUnmappedTeammember(Integer nprojectmastercode, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createProjectMember(List<ProjectMember> projectMember, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> deleteProjectMember(ProjectMember projectMember, int nprojectmembercode,
			UserInfo userInfo) throws Exception;

	public ResponseEntity<List<ProjectMember>> getProjectMember(int nprojectmastercode, UserInfo objUserInfo)
			throws Exception;

	public ResponseEntity<Object> retireProjectMaster(ProjectMaster retireProjectMaster, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createProjectMasterFile(MultipartHttpServletRequest request, UserInfo objUserInfo)
			throws Exception;

	public ResponseEntity<Object> updateProjectMasterFile(MultipartHttpServletRequest request, UserInfo objUserInfo)
			throws Exception;

	public ResponseEntity<Object> deleteProjectMasterFile(ProjectMasterFile objProjectMasterFile, UserInfo objUserInfo)
			throws Exception;

	public ResponseEntity<Object> editProjectMasterFile(ProjectMasterFile objProjectMasterFile, UserInfo objUserInfo)
			throws Exception;

	public ResponseEntity<Object> viewAttachedProjectMasterFile(ProjectMasterFile objProjectMasterFile,
			UserInfo objUserInfo) throws Exception;

	public ResponseEntity<Object> closureProjectMasterFile(MultipartHttpServletRequest request, UserInfo objUserInfo)
			throws Exception;

	public ResponseEntity<Object> getProjectMasterById(int nprojectmastercode, UserInfo objUserInfo) throws Exception;

	public ResponseEntity<Object> getUserrole(UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getUsers(Integer nuserrolecode, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getApprovedProjectMasterByProjectType(Integer nprojecttypecode, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getApprovedProjectByProjectType(int projectTypeCode, UserInfo objUserInfo);

	public ResponseEntity<Object> viewAttachedProjectMasterClosureFile(ProjectMaster objProjectMaster,
			UserInfo objUserInfo) throws Exception;

	public ResponseEntity<Object> getEditReportInfoProject(Map<String, Object> inputMap, UserInfo objUserInfo)
			throws Exception;

	public ResponseEntity<? extends Object> updateReportInfoProject(ReportInfoProject objReportInfoProject,
			UserInfo objUserInfo) throws Exception;

	public ResponseEntity<Object> getQuotaionNoByClient(int nclientcatcode, int nclientcode, UserInfo objUserInfo)
			throws Exception;

	public ResponseEntity<Object> completeProjectMaster(ProjectMaster approveProjectMaster, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getReportTemplate(UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getActiveReportTemplate(int nprojectmastercde, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getAvailableProjectQuotation(final int nprojectmastercode, final UserInfo objUserInfo)
			throws Exception;

	public ResponseEntity<Object> createProjectQuotation(ProjectQuotation projectQuotation, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> deleteProjectQuotation(ProjectQuotation projectQuotation, UserInfo userInfo)
			throws Exception;

	public ProjectQuotation getActiveProjectQuotationById(final int nprojectquotationcode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateProjectQuotation(ProjectQuotation projectQuotation, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getQuotation(UserInfo userInfo) throws Exception;

}
