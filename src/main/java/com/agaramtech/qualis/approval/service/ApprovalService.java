package com.agaramtech.qualis.approval.service;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.registration.model.ApprovalParameter;

/**
 * 
 * @author ATE169
 *
 */
public interface ApprovalService {

	public ResponseEntity<Object> getApproval(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Map<String, Object>> getRegistrationType(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getRegistrationSubType(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getApprovalVersion(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getFilterStatus(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getApproveConfigVersionRegTemplateDesign(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getApprovalSample(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<? extends Object> validateApprovalActions(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateDecision(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	// public ResponseEntity<Object> releaseSamples(Map<String, Object> inputMap,
	// UserInfo userInfo)throws Exception;

	public ResponseEntity<Object> updateEnforceStatus(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Map<String, Object>> getApprovalParameter(final String ntransactionTestCode,
			UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getApprovalSubSample(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getApprovalTest(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getStatusCombo(final int ntransactionResultCode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getEditParameter(final String ntransactionTestCode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateApprovalParameter(final List<ApprovalParameter> approvalParamList,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Map<String, Object>> getApprovalHistory(final String ntransactionTestCode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Map<String, Object>> getApprovalResultChangeHistory(final String ntransactionTestCode,
			final UserInfo userInfo) throws Exception;

	public Map<String, Object> viewAttachment(final Map<String, Object> objMap, final UserInfo objUserInfo)
			throws Exception;

	public ResponseEntity<Map<String, Object>> getSampleApprovalHistory(final String npreregno, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getFilterBasedTest(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getEnforceCommentsHistory(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Map<String, Object>> getCOAHistory(final String npreregno, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> viewReport(final Map<String, Object> objMap, final UserInfo objUserInfo)
			throws Exception;

	public ResponseEntity<Object> getTestBasedOnCompletedBatch(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Map<String, Object>> checkReleaseRecord(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getSampleViewDetails(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Map<String, Object>> getTestResultCorrection(final Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception;

	public ResponseEntity<Map<String, Object>> updateReleaseParameter(final MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception;

	public ResponseEntity<Object> getReleaseResults(final int ntransactionresultcode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createFilterName(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<? extends Object> getTestApprovalFilterDetails(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

}
