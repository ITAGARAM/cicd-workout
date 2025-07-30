package com.agaramtech.qualis.restcontroller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.approval.service.ApprovalService;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.registration.model.ApprovalParameter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 
 * @author ATE169
 *
 */
@RestController
@RequestMapping(value = "/approval")
public class ApprovalController {
	
	private final ApprovalService approvalService;
	private RequestContext requestContext;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param approvalService ApprovalService
	 */
	public ApprovalController(RequestContext requestContext, ApprovalService approvalService) {
		super();
		this.requestContext = requestContext;
		this.approvalService = approvalService;
	}

	@PostMapping(value = "/getApproval")
	public ResponseEntity<Object> getApproval(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return approvalService.getApproval(inputMap, userInfo);

	}
	@PostMapping(value = "/getRegistrationSubType")
	public ResponseEntity<Object> getRegistrationSubType(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return approvalService.getRegistrationSubType(inputMap, userInfo);

	}

	@PostMapping(value = "/getRegistrationType")
	public ResponseEntity<Map<String, Object>> getRegistrationType(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return approvalService.getRegistrationType(inputMap, userInfo);

	}

	@PostMapping(value = "/getApproveConfigVersionRegTemplateDesign")
	public ResponseEntity<Object> getApproveConfigVersionRegTemplateDesign(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return approvalService.getApproveConfigVersionRegTemplateDesign(inputMap, userInfo);

	}

	@PostMapping(value = "/getFilterStatus")
	public ResponseEntity<Object> getFilterStatus(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return approvalService.getFilterStatus(inputMap, userInfo);

	}

	@PostMapping(value = "/getApprovalVersion")
	public ResponseEntity<Object> getApprovalVersion(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return approvalService.getApprovalVersion(inputMap, userInfo);

	}

	@PostMapping(value = "/getApprovalSample")
	public ResponseEntity<Object> getApprovalSample(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return approvalService.getApprovalSample(inputMap, userInfo);

	}

	@PostMapping(value = "/getApprovalSubSample")
	public ResponseEntity<Object> getApprovalSubSample(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return approvalService.getApprovalSubSample(inputMap, userInfo);

	}

	@PostMapping(value = "/getApprovalTest")
	public ResponseEntity<Object> getApprovalTest(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return approvalService.getApprovalTest(inputMap, userInfo);

	}

	
	@SuppressWarnings("unchecked")
	@PostMapping(value = "/performAction")
	public ResponseEntity<? extends Object> performAction(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		inputMap = (Map<String, Object>) inputMap.get("performaction");
		requestContext.setUserInfo(userInfo);
		return approvalService.validateApprovalActions(inputMap, userInfo);

	}

	@SuppressWarnings("unchecked")
	@PostMapping(value = "/updateDecision")
	public ResponseEntity<Object> updateDecision(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		inputMap = (Map<String, Object>) inputMap.get("updatedecision");
		requestContext.setUserInfo(userInfo);
		return approvalService.updateDecision(inputMap, userInfo);

	}

//	@RequestMapping(value="/releaseSamples",method=RequestMethod.POST)
//	public ResponseEntity<Object> releaseSamples(@RequestBody Map<String, Object> inputMap) throws Exception{
//	
//			
//			ObjectMapper objMapper = new ObjectMapper();
//			UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
//			requestContext.setUserInfo(userInfo);
//			return approvalService.releaseSamples(inputMap,userInfo);
//			
//		
//	}
	@SuppressWarnings("unchecked")
	@PostMapping(value = "/updateEnforceStatus")
	public ResponseEntity<Object> updateEnforceStatus(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		inputMap = (Map<String, Object>) inputMap.get("enforcestatus");
		requestContext.setUserInfo(userInfo);
		return approvalService.updateEnforceStatus(inputMap, userInfo);

	}

	@PostMapping(value = "/getapprovalparameter")
	public ResponseEntity<Map<String, Object>> getApprovalParameter(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final String ntransactionTestCode = (String) inputMap.get("ntransactiontestcode");
		requestContext.setUserInfo(userInfo);
		return approvalService.getApprovalParameter(ntransactionTestCode, userInfo);

	}

	@PostMapping(value = "/getStatusCombo")
	public ResponseEntity<Object> getStatusCombo(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int ntransactionResultCode = (int) inputMap.get("ntransactionresultcode");
		requestContext.setUserInfo(userInfo);
		return approvalService.getStatusCombo(ntransactionResultCode, userInfo);

	}

	@PostMapping(value = "/getEditParameter")
	public ResponseEntity<Object> getEditParameter(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final String ntransactionTestCode = (String) inputMap.get("ntransactiontestcode");
		requestContext.setUserInfo(userInfo);
		return approvalService.getEditParameter(ntransactionTestCode, userInfo);

	}

	@PostMapping(value = "/updateApprovalParameter")
	public ResponseEntity<Object> updateApprovalParameter(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		List<ApprovalParameter> approvalParamList = objMapper.convertValue(inputMap.get("approvalparameter"),
				new TypeReference<List<ApprovalParameter>>() {
				});
		requestContext.setUserInfo(userInfo);
		return approvalService.updateApprovalParameter(approvalParamList, userInfo);

	}

	@PostMapping(value = "/getApprovalHistory")
	public ResponseEntity<Map<String, Object>> getApprovalHistory(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final String ntransactionTestCode = (String) inputMap.get("ntransactiontestcode");
		requestContext.setUserInfo(userInfo);
		return approvalService.getApprovalHistory(ntransactionTestCode, userInfo);

	}

	@PostMapping(value = "/getSampleApprovalHistory")
	public ResponseEntity<Map<String, Object>> getSampleApprovalHistory(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final String npreregno = (String) inputMap.get("npreregno");
		requestContext.setUserInfo(userInfo);
		return approvalService.getSampleApprovalHistory(npreregno, userInfo);

	}

	@PostMapping(value = "/getApprovalResultChangeHistory")
	public ResponseEntity<Map<String, Object>> getApprovalResultChangeHistory(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final String ntransactionTestCode = (String) inputMap.get("ntransactiontestcode");
		requestContext.setUserInfo(userInfo);
		return approvalService.getApprovalResultChangeHistory(ntransactionTestCode, userInfo);

	}


	@SuppressWarnings("unchecked")
	@PostMapping(value = "/viewAttachment")
	public ResponseEntity<Object> viewAttachment(@RequestBody Map<String, Object> inputMap,
			HttpServletResponse response) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final Map<String, Object> objMap = objMapper.convertValue(inputMap.get("selectedRecord"), Map.class);
		Map<String, Object> outputMap = approvalService.viewAttachment(objMap, objUserInfo);
		requestContext.setUserInfo(objUserInfo);
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}

//	@RequestMapping(value="/previewSampleReport",method=RequestMethod.POST)
//	public ResponseEntity<Object> previewSampleReport(@RequestBody Map<String, Object> inputMap) throws Exception {
//		final ObjectMapper objMapper = new ObjectMapper();
//		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
//		requestContext.setUserInfo(userInfo);
//		return sampleCertificateDAO.reportGeneration(inputMap);
//	}

	@PostMapping(value = "/getFilterBasedTest")
	public ResponseEntity<Object> getFilterBasedTest(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return approvalService.getFilterBasedTest(inputMap, userInfo);
	}

	@PostMapping(value = "/getEnforceCommentsHistory")
	public ResponseEntity<Object> getEnforceCommentsHistory(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return approvalService.getEnforceCommentsHistory(inputMap, userInfo);
	}

	@PostMapping(value = "/getCOAHistory")
	public ResponseEntity<Map<String, Object>> getCOAHistory(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final String npreregno = (String) inputMap.get("npreregno");
		requestContext.setUserInfo(userInfo);
		return approvalService.getCOAHistory(npreregno, userInfo);
	}

	@SuppressWarnings("unchecked")
	@PostMapping(value = "/viewReport")
	public ResponseEntity<Object> viewReport(@RequestBody Map<String, Object> inputMap, HttpServletResponse response)
			throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final Map<String, Object> objMap = objMapper.convertValue(inputMap.get("selectedRecord"), Map.class);
		requestContext.setUserInfo(objUserInfo);
		return approvalService.viewReport(objMap, objUserInfo);
	}

	@PostMapping(value = "/getTestBasedOnCompletedBatch")
	public ResponseEntity<Object> getTestBasedOnCompletedBatch(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return approvalService.getTestBasedOnCompletedBatch(inputMap, userInfo);
	}

	@SuppressWarnings("unchecked")
	@PostMapping(value = "/checkReleaseRecord")
	public ResponseEntity<Map<String, Object>> checkReleaseRecord(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		inputMap = (Map<String, Object>) inputMap.get("performaction");
		requestContext.setUserInfo(userInfo);
		return approvalService.checkReleaseRecord(inputMap, userInfo);

	}

	@PostMapping(value = "/getSampleViewDetails")
	public ResponseEntity<Object> getSampleViewDetails(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return approvalService.getSampleViewDetails(inputMap);
	}

	@PostMapping(value = "/getTestResultCorrection")
	public ResponseEntity<Map<String, Object>> getTestResultCorrection(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return approvalService.getTestResultCorrection(inputMap, userInfo);

	}

	@PostMapping(value = "/updateReleaseParameter")
	public ResponseEntity<Map<String, Object>> updateReleaseParameter(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return approvalService.updateReleaseParameter(request, userInfo);
	}
	
	@PostMapping(value = "/getReleaseResults")
	public ResponseEntity<Object> getReleaseResults(@RequestBody Map<String, Object> inputMap) throws Exception {
		// try {
		final ObjectMapper objmapper = new ObjectMapper();
		int ntransactionresultcode = (int) inputMap.get("ntransactionresultcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return (ResponseEntity<Object>) approvalService.getReleaseResults(ntransactionresultcode, userInfo);

	}

	@PostMapping(value = "/createFilterName")
	public ResponseEntity<Object> createFilterName(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return approvalService.createFilterName(inputMap, userInfo);
	}

	
	@PostMapping(value = "/getTestApprovalFilterDetails")
	public ResponseEntity<? extends Object> getTestApprovalFilterDetails(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return approvalService.getTestApprovalFilterDetails(inputMap, userInfo);
	}

}
