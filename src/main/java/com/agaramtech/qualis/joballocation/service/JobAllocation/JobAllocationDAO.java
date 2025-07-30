package com.agaramtech.qualis.joballocation.service.JobAllocation;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.configuration.model.FilterName;
import com.agaramtech.qualis.global.UserInfo;

public interface JobAllocationDAO {
	
	public ResponseEntity<Object> getJobAllocation(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Map<String,Object>> getSampleType(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Map<String,Object>> getRegistrationType(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Map<String, Object>> getRegistrationsubType(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getApprovalConfigVersion(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getFilterStatus(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getSection(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getTestCombo(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getRegistrationTypeBySampleType(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Map<String,Object>> getRegistrationsubTypeByRegType(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getApprovalConfigVersionByRegSubType(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getFilterStatusByApproveVersion(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getSectionByApproveVersion(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getTestComboBySection(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getDesignTemplateByApprovalConfigVersion(Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getJobAllocationDetails(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getJobAllocationSubSampleDetails(Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getJobAllocationTestDetails(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public Map<String, Object> getTestView(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public Map<String, Object> CreateReceiveinLab(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getAllotDetails(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getAllotAnotherUserDetails(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getInstrumentNameBasedCategory(final int instrumentCatCode,
			final int calibrationRequired, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getInstrumentIdBasedCategory(final int instrumentCatCode,
			final int instrumentNameCode, final int calibrationRequired, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getUsersBasedTechnique(final int techniqueCode, final String sectionCode,
			final int regTypeCode, final int regSubTypeCode, final UserInfo userInfo) throws Exception;

	public Map<String, Object> AllotJobCreate(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public Map<String, Object> AllotAnotherUserCreate(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public Map<String, Object> RescheduleCreate(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> viewAnalystCalendar(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public Map<String, Object> cancelTest(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public Map<String, Object> insertSeqNoRegistration(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public Map<String, Object> insertSeqNoAllotJob(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public Map<String, Object> insertSeqNoAllotAnotherUser(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public Map<String, Object> insertSeqNoReschedule(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public Map<String, Object> insertSeqNoCancelRegistration(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getAnalystCalendarBasedOnUser(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getInstrumentBasedCategoryForSchedule(int instrumentCatCode, int instrumentnameCode,
			UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> AllotJobCreateForSchedule(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getAnalystCalendarBasedOnUserWithDate(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public Object AllotJobCalendarCreate(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getRescheduleEdit(String npreregno, String ntransactionsamplecode,
			String ntransactiontestcode, UserInfo userInfo, Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> calenderProperties(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getUserScheduleCombo(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getInstrumentNameBasedCategoryForSchedule(int instrumentCatCode, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getSectionChange(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateSectionJobAllocation(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getUsersBySection(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	// Added by Dhanushya RI for JIRA ID:ALPD-4755 Filter save detail --Start
	public ResponseEntity<Object> getJobAllocationFilter(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createFilterName(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public List<FilterName> getFilterName(UserInfo userInfo) throws Exception;
	// End

}
