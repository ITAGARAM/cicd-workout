package com.agaramtech.qualis.approval.service;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.registration.model.ApprovalParameter;

/**
 * 
 * @author ATE169
 *
 */
@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class ApprovalServiceImpl implements ApprovalService {

	private final ApprovalDAO approvalDAO;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext RequestContext to hold the request
	 * @param approvalDAO    ApprovalDAO
	 */
	public ApprovalServiceImpl(ApprovalDAO approvalDAO) {
		super();
		this.approvalDAO = approvalDAO;
	}

	@Transactional
	@Override
	public ResponseEntity<Object> getApproval(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		return approvalDAO.getApproval(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<? extends Object> validateApprovalActions(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {


		final ResponseEntity<? extends Object> response = approvalDAO.validateApprovalActions(inputMap, userInfo, false);
		if (response.getBody() instanceof String) {
			return response;
		} else {

			final Map<String, Object> mapObject = (Map<String, Object>) response.getBody();

			inputMap.putAll(mapObject);

			return approvalDAO.validateApprovalActions(inputMap, userInfo, true);
		}
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateDecision(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		return approvalDAO.updateDecision(inputMap, userInfo);

	}

//	@Override
//	public ResponseEntity<Object> releaseSamples(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
//		int napprovalConfigVersionCode = approvalDAO.validateApprovalVersion((String)inputMap.get("npreregno"),userInfo);
//		if(napprovalConfigVersionCode==-1){
//			
//			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTSAMPLEOFSAMEAPPROVALVERSION", userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
//			
//		}else{
//			inputMap.put("napprovalversioncode", napprovalConfigVersionCode);
//			return approvalDAO.releaseSamples(inputMap, userInfo);
//		}
//		
//	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateEnforceStatus(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		return approvalDAO.updateEnforceStatus(inputMap, userInfo);

	}

	@Override
	public ResponseEntity<Map<String, Object>> getApprovalParameter(final String ntransactionTestCode, final UserInfo userInfo)
			throws Exception {

		return approvalDAO.getApprovalParameter(ntransactionTestCode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> getApprovalSubSample(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return approvalDAO.getApprovalSubSample(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> getApprovalTest(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return approvalDAO.getApprovalTest(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Map<String, Object>> getRegistrationType(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		return approvalDAO.getRegistrationType(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> getRegistrationSubType(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return approvalDAO.getRegistrationSubType(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getFilterStatus(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return approvalDAO.getFilterStatus(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> getApproveConfigVersionRegTemplateDesign(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return approvalDAO.getApproveConfigVersionRegTemplateDesign(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> getApprovalVersion(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		return approvalDAO.getApprovalVersion(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity< Object> getApprovalSample(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return approvalDAO.getApprovalSample(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getStatusCombo(final int ntransactionResultCode, final UserInfo userInfo)
			throws Exception {
		return approvalDAO.getStatusCombo(ntransactionResultCode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getEditParameter(final String ntransactionTestCode, final UserInfo userInfo)
			throws Exception {
		return approvalDAO.getEditParameter(ntransactionTestCode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateApprovalParameter(final List<ApprovalParameter> approvalParamList,
			final UserInfo userInfo) throws Exception {
		return approvalDAO.updateApprovalParameter(approvalParamList, userInfo);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getApprovalHistory(final String ntransactionTestCode, final UserInfo userInfo)
			throws Exception {
		return approvalDAO.getApprovalHistory(ntransactionTestCode, userInfo);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getApprovalResultChangeHistory(final String ntransactionTestCode,
			final UserInfo userInfo) throws Exception {

		return approvalDAO.getApprovalResultChangeHistory(ntransactionTestCode, userInfo);
	}


	
	@Override
	public Map<String, Object> viewAttachment(final Map<String, Object> objMap, final UserInfo userInfo)
			throws Exception {

		return approvalDAO.viewAttachment(objMap, userInfo);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getSampleApprovalHistory(final String npreregno, final UserInfo userInfo)
			throws Exception {
		return approvalDAO.getSampleApprovalHistory(npreregno, userInfo);
	}

	public ResponseEntity<Object> getEnforceCommentsHistory(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		return approvalDAO.getEnforceCommentsHistory(inputMap, userInfo);

	}

	@Transactional
	@Override
	public ResponseEntity<Object> getFilterBasedTest(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return approvalDAO.getFilterBasedComboTest(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getCOAHistory(final String npreregno, final UserInfo userInfo) throws Exception {
		return approvalDAO.getCOAHistory(npreregno, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> viewReport(final Map<String, Object> objMap, final UserInfo objUserInfo)
			throws Exception {

		return approvalDAO.viewReport(objMap, objUserInfo);
	}

	public ResponseEntity<Object> getTestBasedOnCompletedBatch(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		return approvalDAO.getTestBasedOnCompletedBatch(inputMap, userInfo);
	}

	public ResponseEntity<Map<String, Object>> checkReleaseRecord(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		return approvalDAO.checkReleaseRecord(inputMap, userInfo);
	}

	public ResponseEntity<Object> getSampleViewDetails(final Map<String, Object> inputMap) throws Exception {
		return approvalDAO.getSampleViewDetails(inputMap);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getTestResultCorrection(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return approvalDAO.getTestResultCorrection(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Map<String, Object>> updateReleaseParameter(final MultipartHttpServletRequest request,
			final UserInfo userInfo) throws Exception {
		return approvalDAO.updateReleaseParameter(request, userInfo);
	}

	@Override
	public ResponseEntity<Object> getReleaseResults(final int ntransactionresultcode, final UserInfo userInfo)
			throws Exception {
		return approvalDAO.getReleaseResults(ntransactionresultcode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createFilterName(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return approvalDAO.createFilterName(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<? extends Object> getTestApprovalFilterDetails(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return approvalDAO.getTestApprovalFilterDetails(inputMap, userInfo);
	}

}
