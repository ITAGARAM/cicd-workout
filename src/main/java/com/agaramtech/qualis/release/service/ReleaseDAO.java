package com.agaramtech.qualis.release.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.configuration.model.FilterName;
import com.agaramtech.qualis.configuration.model.Settings;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.registration.model.COAHistory;
import com.agaramtech.qualis.release.model.ReleaseComment;
import com.agaramtech.qualis.release.model.ReleaseOutsourceAttachment;
import com.agaramtech.qualis.release.model.ReleaseTestAttachment;
import com.agaramtech.qualis.release.model.ReleaseTestComment;
import com.agaramtech.qualis.release.model.ReportInfoRelease;

/**
 * 
 * @author ATE169
 *
 */
public interface ReleaseDAO {

	public ResponseEntity<Object> getRelease(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Map<String, Object>> getRegistrationSubType(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Map<String, Object>> getFilterStatus(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Map<String, Object>> getRegistrationType(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Map<String, Object>> getReleaseSample(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public Map<String, Object> updateRelease(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> releasedReportGeneration(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public Map<String, Object> seqNoSampleSubSampleTestInsert(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Map<String, Object>> getApprovalVersion(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Map<String, Object>> getReleaseSubSample(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Map<String, Object>> getReleaseHistory(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Map<String,Object>> getCOAReportType(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	/**
	 * This method declaration is used to fetch a file/ link which need to view
	 * 
	 * @param objTestFile [TestFile] object holds the details of test file
	 * @param objUserInfo [UserInfo] object holds the loggedin user info
	 * @return response entity of 'testfile' entity
	 * @throws Exception
	 */
	public Map<String, Object> viewAttachedCOAHistoryFile(final COAHistory objCOAHistory, final UserInfo objUserInfo)
			throws Exception;

	public ResponseEntity<Map<String,Object>> reportGeneration(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Map<String,Object>> preliminaryRegenerateReport(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Map<String, Object>> getApprovedProjectByProjectType(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getApprovedProjectType(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Map<String, Object>> getReleaseConfigVersionRegTemplateDesign(Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> SendToPortalReport(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> saveAsDraft(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> DeleteApprovedSamples(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> UpdateApprovedSamples(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getStatusAlert(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Map<String, Object>> getSection(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateReleaseParameter(MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception;

	public ResponseEntity<Object> getResultCorrection(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getReleaseResults(int ntransactionresultcode, UserInfo UserInfo) throws Exception;

	public ResponseEntity<Object> updateCorrectionStatus(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public Map<String, Object> updateReleaseAfterCorrection(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> getreportcomments(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	ReportInfoRelease getActiveUnitById(final int nunitCode, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> updateReportComment(ReportInfoRelease selectedComment, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> viewReportHistory(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public Map<String, Object> viewReleasedCOAReport(final ReleaseOutsourceAttachment objReleaseCOAReport,
			int ncontrolCode, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getReleaseTestAttachment(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	ReleaseTestAttachment getActiveReleaseTestAttachmentById(final int nreleaseTestAttachmentCode, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createReleaseTestAttachment(MultipartHttpServletRequest request, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateReleaseTestAttachment(MultipartHttpServletRequest request, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> deleteReleaseTestAttachment(final ReleaseTestAttachment objReleaseTestAttachment,
			UserInfo userInfo) throws Exception;

	public Map<String, Object> viewReleaseTestAttachment(final Map<String, Object> objReleaseTestAttachmentFile,
			UserInfo userInfo, int ncontrolcode) throws Exception;

	public ResponseEntity<Object> getVersionHistory(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Map<String, Object>> downloadVersionHistory(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public Map<String, Object> validationCheckForNodeServer(Map<String, Object> inputMap, final UserInfo objUserInfo)
			throws Exception;

	public ResponseEntity<Object> downloadHistory(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getPatientWiseSample(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getReleaseTestComment(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	ReleaseTestComment getActiveReleaseTestCommentById(final int nreleaseTestCommentCode, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createReleaseTestComment(MultipartHttpServletRequest request, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateReleaseTestComment(MultipartHttpServletRequest request, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> deleteReleaseTestComment(final ReleaseTestComment objReleaseTestCommentt,
			UserInfo userInfo) throws Exception;

	public ResponseEntity<Map<String, Object>> getPreliminaryReportHistory(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getComboValues(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> generateReport(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> reportGenerationSync(Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getTest(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<List<Map<String, Object>>> getApprovedReportTemplate(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getApprovedReportTemplateById(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateReportTemplate(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> deleteSamples(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> sampleCountValidation(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	// Added by sonia on 11-06-2024 for JIRA ID:4360 Auto Download reports
	// Added by sonia on 18-08-2024 for JIRA ID:4716 changed method name
	public Settings reportGenerationReleaseAction(UserInfo userInfo) throws Exception;

	// Added by Dhanushya RI for JIRA ID:ALPD-4878 Filter save detail --Start
	public ResponseEntity<Object> createFilterName(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public List<FilterName> getFilterName(UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getReleaseFilter(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	// ALPD-5189 added by Dhanushya RI,To get comment details for each release
	// number
	public ResponseEntity<Object> createReleaseComment(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ReleaseComment getReleaseCommentDetails(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

}
