package com.agaramtech.qualis.submitter.service.submitter;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.submitter.model.Submitter;
import com.agaramtech.qualis.submitter.model.SubmitterMapping;

public interface SubmitterDAO {

	public ResponseEntity<Map<String, Object>> getSubmitter(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Map<String, Object>> getInstitutionCategory(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getInstitution(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getInstitutionByCategory(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Map<String, Object>> getInstitutionSite(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Map<String, Object>> getInstitutionSiteByInstitution(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Map<String, Object>> getSubmitterByFilter(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getActiveSubmitterByFilter(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getInstitutionDepartment(final UserInfo userInfo) throws Exception;

	public Submitter getActiveSubmitterById(final String ssubmitterCode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createSubmitter(final Submitter objSubmitter, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateSubmitter(final Submitter objSubmitter, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> deleteSubmitter(final Submitter objSubmitter, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> retireSubmitter(final Submitter objSubmitter, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getSelectedSubmitterDetail(final UserInfo userInfo, final String ssubmitterCode,
			final Submitter objSubmitter) throws Exception;

	public ResponseEntity<Object> getSubmitterDetailByAll(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createSubmitterMapping(final List<SubmitterMapping> submitterMapping,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteSubmitterMapping(final SubmitterMapping submitterMapping,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getSubmitterBySubmitterCode(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getSubmitterInstitutionCategoryCombo(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getSubmitterInstitutionCombo(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getSubmitterInstitutionSiteCombo(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getActiveSubmitterByInstitution(final int ninstitutionCode, final UserInfo userInfo);

	public ResponseEntity<Object> getSubmitterByInstitutionSite(final int ninstitutionSiteCode, final UserInfo userInfo,
			final Map<String, Object> inputMap);

}
