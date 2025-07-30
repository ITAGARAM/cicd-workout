package com.agaramtech.qualis.submitter.service.submitter;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.submitter.model.Submitter;
import com.agaramtech.qualis.submitter.model.SubmitterMapping;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class SubmitterServiceImpl implements SubmitterService {

	private final SubmitterDAO submitterDAO;
	private final CommonFunction commonFunction;

	@Override
	public ResponseEntity<Map<String, Object>> getSubmitter(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return submitterDAO.getSubmitter(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getInstitutionCategory(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return submitterDAO.getInstitutionCategory(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getInstitution(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return submitterDAO.getInstitution(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getInstitutionByCategory(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return submitterDAO.getInstitutionByCategory(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getInstitutionSite(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return submitterDAO.getInstitutionSite(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getInstitutionSiteByInstitution(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return submitterDAO.getInstitutionSiteByInstitution(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getSubmitterByFilter(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return submitterDAO.getSubmitterByFilter(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveSubmitterByFilter(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return submitterDAO.getActiveSubmitterByFilter(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getInstitutionDepartment(final UserInfo userInfo) throws Exception {
		return submitterDAO.getInstitutionDepartment(userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveSubmitterById(final String ssubmitterCode, final UserInfo userInfo)
			throws Exception {
		final Submitter submitter = submitterDAO.getActiveSubmitterById(ssubmitterCode, userInfo);
		if (submitter == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		} else if (submitter.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_RETIREDSUBMITTERCANNOTBEEDITED",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		} else {
			return new ResponseEntity<>(submitter, HttpStatus.OK);
		}
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createSubmitter(final Submitter objSubmitter, final UserInfo userInfo)
			throws Exception {
		return submitterDAO.createSubmitter(objSubmitter, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateSubmitter(final Submitter objSubmitter, final UserInfo userInfo)
			throws Exception {
		return submitterDAO.updateSubmitter(objSubmitter, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteSubmitter(final Submitter objSubmitter, final UserInfo userInfo)
			throws Exception {
		return submitterDAO.deleteSubmitter(objSubmitter, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> retireSubmitter(final Submitter objSubmitter, final UserInfo userInfo)
			throws Exception {
		return submitterDAO.retireSubmitter(objSubmitter, userInfo);
	}

	@Override
	public ResponseEntity<Object> getSelectedSubmitterDetail(final UserInfo userInfo, final String ssubmitterCode,
			final Submitter objSubmitter) throws Exception {
		return submitterDAO.getSelectedSubmitterDetail(userInfo, ssubmitterCode, objSubmitter);
	}

	@Override
	public ResponseEntity<Object> getSubmitterDetailByAll(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return submitterDAO.getSubmitterDetailByAll(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createSubmitterMapping(final List<SubmitterMapping> submitterMapping,
			final UserInfo userInfo) throws Exception {
		return submitterDAO.createSubmitterMapping(submitterMapping, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteSubmitterMapping(final SubmitterMapping submitterMapping,
			final UserInfo userInfo) throws Exception {
		return submitterDAO.deleteSubmitterMapping(submitterMapping, userInfo);
	}

	@Override
	public ResponseEntity<Object> getSubmitterBySubmitterCode(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return submitterDAO.getSubmitterBySubmitterCode(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getSubmitterInstitutionCategoryCombo(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return submitterDAO.getSubmitterInstitutionCategoryCombo(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getSubmitterInstitutionCombo(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return submitterDAO.getSubmitterInstitutionCombo(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getSubmitterInstitutionSiteCombo(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return submitterDAO.getSubmitterInstitutionSiteCombo(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveSubmitterByInstitution(final int ninstitutionCode, final UserInfo userInfo) {
		return submitterDAO.getActiveSubmitterByInstitution(ninstitutionCode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getSubmitterByInstitutionSite(final int ninstitutionSiteCode, final UserInfo userInfo,
			final Map<String, Object> inputMap) {
		return submitterDAO.getSubmitterByInstitutionSite(ninstitutionSiteCode, userInfo, inputMap);
	}
}