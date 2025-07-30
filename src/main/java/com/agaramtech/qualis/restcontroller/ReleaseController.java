package com.agaramtech.qualis.restcontroller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.configuration.model.FilterName;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.registration.model.COAHistory;
import com.agaramtech.qualis.release.model.ReleaseComment;
import com.agaramtech.qualis.release.model.ReleaseOutsourceAttachment;
import com.agaramtech.qualis.release.model.ReleaseTestAttachment;
import com.agaramtech.qualis.release.model.ReleaseTestComment;
import com.agaramtech.qualis.release.model.ReportInfoRelease;
import com.agaramtech.qualis.release.service.ReleaseService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 
 * @author ATE203
 *
 */
@RestController
@RequestMapping(value = "/release")
public class ReleaseController {

	private final ReleaseService releaseService;
	private RequestContext requestContext;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext RequestContext to hold the request
	 * @param releaseService    ReleaseService
	 */
	public ReleaseController(RequestContext requestContext, ReleaseService releaseService) {
		super();
		this.requestContext = requestContext;
		this.releaseService = releaseService;
	}

	@PostMapping(value = "/getRelease")
	public ResponseEntity<Object> getRelease(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.getRelease(inputMap, userInfo);

	}

	@PostMapping(value = "/getRegistrationType")
	public ResponseEntity<Map<String, Object>> getRegistrationType(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.getRegistrationType(inputMap, userInfo);

	}

	@PostMapping(value = "/getRegistrationSubType")
	public ResponseEntity<Map<String, Object>> getRegistrationSubType(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.getRegistrationSubType(inputMap, userInfo);

	}

	@PostMapping(value = "/getReleaseSample")
	public ResponseEntity<Map<String, Object>> getReleaseSample(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.getReleaseSample(inputMap, userInfo);

	}

	@PostMapping(value = "/saveRelease")
	public ResponseEntity<Object> saveAsDraft(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.saveAsDraft(inputMap, userInfo);

	}

	@PostMapping(value = "/getFilterStatus")
	public ResponseEntity<Map<String, Object>> getFilterStatus(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.getFilterStatus(inputMap, userInfo);

	}

	@PostMapping(value = "/getApprovalVersion")
	public ResponseEntity<Map<String, Object>> getApprovalVersion(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.getApprovalVersion(inputMap, userInfo);

	}

	@PostMapping(value = "/getReleaseConfigVersionRegTemplateDesign")
	public ResponseEntity<Map<String, Object>> getReleaseConfigVersionRegTemplateDesign(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.getReleaseConfigVersionRegTemplateDesign(inputMap, userInfo);

	}

	@PostMapping(value = "/getReleaseSubSample")
	public ResponseEntity<Map<String, Object>> getReleaseSubSample(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.getReleaseSubSample(inputMap, userInfo);

	}

	@PostMapping(value = "/getCOAReportType")
	public ResponseEntity<Map<String,Object>> getCOAReportType(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.getCOAReportType(inputMap, userInfo);

	}

	@PostMapping(value = "/getReleaseHistory")
	public ResponseEntity<Map<String, Object>> getReleaseHistory(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.getReleaseHistory(inputMap, userInfo);

	}

	/**
	 * This method is used to fetch a file/ link which need to view
	 * 
	 * @param inputMap [Map] holds the object of loggedin userinfo and test file
	 * @return
	 * @return response entity holds the list of test file details
	 * @throws Exception from DAO layer
	 */
	@PostMapping(value = "/viewAttachedCOAHistoryFile")
	public ResponseEntity<Object> viewAttachedCOAHistoryFile(@RequestBody Map<String, Object> inputMap,
			HttpServletResponse response) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final COAHistory objCOAHistory = objMapper.convertValue(inputMap.get("COAHistoryFile"), COAHistory.class);
		Map<String, Object> outputMap = releaseService.viewAttachedCOAHistoryFile(objCOAHistory, userInfo);

		requestContext.setUserInfo(userInfo);
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}

	@PostMapping(value = "/updateRelease")
	public ResponseEntity<Object> updateRelease(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.updateRelease(inputMap, userInfo);
	}

	@PostMapping(value = "/getApprovedProjectType")
	public ResponseEntity<Object> getApprovedProjectType(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.getApprovedProjectType(inputMap, userInfo);

	}

	@PostMapping(value = "/getApprovedProjectByProjectType")
	public ResponseEntity<Map<String, Object>> getApprovedProjectByProjectType(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.getApprovedProjectByProjectType(inputMap, userInfo);

	}

	@PostMapping(value = "/SendToPortalReport")
	public ResponseEntity<Object> SendToPortalReport(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.SendToPortalReport(inputMap, userInfo);

	}

	@PostMapping(value = "/preliminaryRegenerateReport")
	public ResponseEntity<Map<String,Object>> preliminaryRegenerateReport(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.preliminaryRegenerateReport(inputMap, userInfo);
	}

	@PostMapping(value = "/deleteRelease")
	public ResponseEntity<Object> DeleteApprovedSamples(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.DeleteApprovedSamples(inputMap, userInfo);
	}

	@PostMapping(value = "/appendRelease")
	public ResponseEntity<Object> UpdateApprovedSamples(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.UpdateApprovedSamples(inputMap, userInfo);
	}

	@PostMapping(value = "/getStatusAlert")
	public ResponseEntity<Object> getStatusAlert(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.getStatusAlert(inputMap, userInfo);
	}

	@PostMapping(value = "/getSection")
	public ResponseEntity<Map<String, Object>> getSection(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.getSection(inputMap, userInfo);

	}

	@PostMapping(value = "/getreportcomments")
	public ResponseEntity<Object> getreportcomments(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.getreportcomments(inputMap, userInfo);

	}

	@PostMapping(value = "/getActiveReportInfoReleaseById")
	public ResponseEntity<Object> getActiveUnitById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int nreportinforeleasecode = (int) inputMap.get("nreportinforeleasecode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return releaseService.getActiveUnitById(nreportinforeleasecode, userInfo);
	}

	@PostMapping(value = "/updateReportComment")
	public ResponseEntity<Object> updateReportComment(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final ReportInfoRelease selectedComment = objMapper.convertValue(inputMap.get("selectedComment"),
				ReportInfoRelease.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.updateReportComment(selectedComment, userInfo);
	}

	@PostMapping(value = "/updateReleaseParameter")
	public ResponseEntity<Object> updateReleaseParameter(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.updateReleaseParameter(request, userInfo);
	}

	@PostMapping(value = "/getResultCorrection")
	public ResponseEntity<Object> getReportCorrection(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.getResultCorrection(inputMap, userInfo);

	}

	@PostMapping(value = "/getReleaseResults")
	public ResponseEntity<Object> getReleaseResults(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int ntransactionresultcode = (int) inputMap.get("ntransactionresultcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return (ResponseEntity<Object>) releaseService.getReleaseResults(ntransactionresultcode, userInfo);

	}

	@PostMapping(value = "/updateCorrectionStatus")
	public ResponseEntity<Object> updateCorrectionStatus(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.updateCorrectionStatus(inputMap, userInfo);
	}

	@PostMapping(value = "/updateReleaseAfterCorrection")
	public ResponseEntity<Object> updateReleaseAfterCorrection(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.updateReleaseAfterCorrection(inputMap, userInfo);
	}

	@PostMapping(value = "/viewReportHistory")
	public ResponseEntity<Object> viewReportHistory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.viewReportHistory(inputMap, userInfo);
	}

	@PostMapping(value = "/viewReleasedCOAReport")
	public ResponseEntity<Object> viewReleasedCOAReport(@RequestBody Map<String, Object> inputMap,
			HttpServletResponse response) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final ReleaseOutsourceAttachment objReleaseCOAReport = objMapper.convertValue(inputMap.get("releasedcoareport"),
				ReleaseOutsourceAttachment.class);
		final int ncontrolCode = (int) inputMap.get("ncontrolCode");
		Map<String, Object> outputMap = releaseService.viewReleasedCOAReport(objReleaseCOAReport, ncontrolCode,
				userInfo);
		requestContext.setUserInfo(userInfo);
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}

	@PostMapping(value = "/getReleaseTestAttachment")
	public ResponseEntity<Object> getReleaseTestAttachment(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.getReleaseTestAttachment(inputMap, userInfo);
	}

	@PostMapping(value = "/getActiveReleaseTestAttachmentById")
	public ResponseEntity<Object> getActiveReleaseTestAttachmentById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int nreleaseTestAttachmentCode = (int) inputMap.get("nreleasetestattachmentcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return releaseService.getActiveReleaseTestAttachmentById(nreleaseTestAttachmentCode, userInfo);
	}

	@PostMapping(value = "/createReleaseTestAttachment")
	public ResponseEntity<Object> createReleaseTestAttachment(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.createReleaseTestAttachment(request, userInfo);
	}

	@PostMapping(value = "/updateReleaseTestAttachment")
	public ResponseEntity<Object> updateReleaseTestAttachment(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.updateReleaseTestAttachment(request, userInfo);

	}

	@PostMapping(path = "/deleteReleaseTestAttachment")
	public ResponseEntity<Object> deleteReleaseTestAttachment(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final ReleaseTestAttachment objReleaseTestAttachment = objMapper
				.convertValue(inputMap.get("releasetestattachment"), ReleaseTestAttachment.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.deleteReleaseTestAttachment(objReleaseTestAttachment, userInfo);
	}

	@SuppressWarnings("unchecked")
	@PostMapping(value = "/viewReleaseTestAttachment")
	public ResponseEntity<Object> viewReleaseTestAttachment(@RequestBody Map<String, Object> inputMap,
			HttpServletResponse response) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final Map<String, Object> objReleaseTestAttachmentFile = (Map<String, Object>) inputMap
				.get("releasetestattachment");
		final int ncontrolcode = (int) inputMap.get("ncontrolcode");
		Map<String, Object> outputMap = releaseService.viewReleaseTestAttachment(objReleaseTestAttachmentFile, userInfo,
				ncontrolcode);
		requestContext.setUserInfo(userInfo);
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}

	@PostMapping(value = "/getVersionHistory")
	public ResponseEntity<Object> getVersionHistory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.getVersionHistory(inputMap, userInfo);
	}

	@PostMapping(value = "/downloadVersionHistory")
	public ResponseEntity<Map<String, Object>> downloadVersionHistory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.downloadVersionHistory(inputMap, userInfo);
	}

	@PostMapping(value = "/downloadHistory")
	public ResponseEntity<Object> downloadHistory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.downloadHistory(inputMap, userInfo);
	}

	@PostMapping(value = "/getPatientWiseSample")
	public ResponseEntity<Object> getPatientWiseSample(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.getPatientWiseSample(inputMap, userInfo);

	}

	@PostMapping(value = "/getReleaseTestComment")
	public ResponseEntity<Object> getReleaseTestComment(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.getReleaseTestComment(inputMap, userInfo);
	}

	@PostMapping(value = "/getActiveReleaseTestCommentById")
	public ResponseEntity<Object> getActiveReleaseTestCommentById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int nreleaseTestCommentCode = (int) inputMap.get("nreleasetestcommentcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return releaseService.getActiveReleaseTestCommentById(nreleaseTestCommentCode, userInfo);
	}

	@PostMapping(value = "/createReleaseTestComment")
	public ResponseEntity<Object> createReleaseTestComment(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.createReleaseTestComment(request, userInfo);
	}

	@PostMapping(value = "/updateReleaseTestComment")
	public ResponseEntity<Object> updateReleaseTestComment(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.updateReleaseTestComment(request, userInfo);
	}

	@PostMapping(path = "/deleteReleaseTestComment")
	public ResponseEntity<Object> deleteReleaseTestComment(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final ReleaseTestComment objReleaseTestComment = objMapper.convertValue(inputMap.get("releasetestcomment"),
				ReleaseTestComment.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.deleteReleaseTestComment(objReleaseTestComment, userInfo);
	}

	@PostMapping(value = "/getPreliminaryReportHistory")
	public ResponseEntity<Map<String, Object>> getPreliminaryReportHistory(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.getPreliminaryReportHistory(inputMap, userInfo);
	}

	@PostMapping(path = "/check")
	public ResponseEntity<Object> check(@RequestBody Map<String, Object> inputMap) throws Exception {
		Map<String, Object> obj = new HashMap<String, Object>();
		obj.put("data", "Success");
		return new ResponseEntity<>(obj, HttpStatus.OK);
	}

	@PostMapping(value = "/generateReport")
	public ResponseEntity<Object> generateReport(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);

		return releaseService.generateReport(inputMap, userInfo);
	}

	@PostMapping(path = "/getComboValues")
	public ResponseEntity<Object> getComboValues(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.getComboValues(inputMap, userInfo);
	}

	@PostMapping(value = "/getTest")
	public ResponseEntity<Object> getTest(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.getTest(inputMap, userInfo);
	}

	@PostMapping(value = "/getApprovedReportTemplate")
	public ResponseEntity<List<Map<String, Object>>> getApprovedReportTemplate(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.getApprovedReportTemplate(inputMap, userInfo);
	}

	@PostMapping(value = "/getApprovedReportTemplateById")
	public ResponseEntity<Object> getApprovedReportTemplateById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.getApprovedReportTemplateById(inputMap, userInfo);
	}

	@PostMapping(value = "/updateReportTemplate")
	public ResponseEntity<Object> updateReportTemplate(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.updateReportTemplate(inputMap, userInfo);
	}

	@PostMapping(value = "/deleteSamples")
	public ResponseEntity<Object> deleteSamples(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.deleteSamples(inputMap, userInfo);
	}

	@PostMapping(value = "/sampleCountValidation")
	public ResponseEntity<Object> sampleCountValidation(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.sampleCountValidation(inputMap, userInfo);
	}

	@PostMapping(value = "/createFilterName")
	public ResponseEntity<Object> createFilterName(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return releaseService.createFilterName(inputMap, userInfo);
	}

	@PostMapping(value = "/getFilterName")
	public ResponseEntity<Object> getFilterName(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		List<FilterName> outputMap = releaseService.getFilterName(userInfo);
		requestContext.setUserInfo(userInfo);
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);

	}

	@PostMapping(value = "/getReleaseFilter")
	public ResponseEntity<Object> getReleaseFilter(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return releaseService.getReleaseFilter(inputMap, userInfo);

	}

	@PostMapping(value = "/createReleaseComment")
	public ResponseEntity<Object> createReleaseComment(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.createReleaseComment(inputMap, userInfo);
	}

	@PostMapping(value = "/getReleaseCommentDetails")
	public ReleaseComment getReleaseCommentDetails(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return releaseService.getReleaseCommentDetails(inputMap, userInfo);
	}
}