package com.agaramtech.qualis.scheduler.service;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;

//ALPD-4941 Created SchedulerConfigurationDAO for Scheduler configuration screen
public interface SchedulerConfigurationDAO {
	public ResponseEntity<Object> getSchedulerConfiguration(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getSampleType(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getRegistrationType(int nSampleType, Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getRegistrationSubType(int nregTypeCode, Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getApprovalConfigVersion(int nregTypeCode, int nregSubTypeCode,
			Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getApproveConfigVersionRegTemplate(int nregTypeCode, int nregSubTypeCode,
			int napproveConfigVersionCode, Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getSchedulerConfigByFilterSubmit(Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createSchedulerConfig(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getSchedulerConfigSubSample(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getSchedulerConfigTest(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getSchedulerConfigParameter(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getComponentBySpec(Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getTestfromDB(Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getTestfromTestPackage(Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getTestfromSection(Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getTestBasedTestSection(Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> createSubSample(Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> updateSchedulerConfigSubSample(Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getEditSchedulerSubSampleComboService(Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteSchedulerConfigSubSample(Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getMoreTest(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getMoreTestSection(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getMoreTestPackage(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createTest(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteSchedulerConfigTest(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getEditSchedulerConfigDetails(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateSchedulerConfig(final Map<String, Object> inputMap) throws Exception;

	public Map<String, Object> validateUniqueConstraintScheduler(List<Map<String, Object>> masterUniqueValidation,
			Map<String, Object> map, UserInfo userInfo, String string, Class<?> class1, String string2, boolean b)
					throws Exception;

	public ResponseEntity<Object> getSiteByUser(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> approveSchedulerConfig(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getSchedulerMaster(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> deleteSchedulerConfig(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> updateActiveStatusSchedulerConfig(final Map<String, Object> inputMap)
			throws Exception;

	public ResponseEntity<Object> getSchedulerMasteDetails(final Map<String, Object> inputMap) throws Exception;

	// ALPD-5530--Vignesh R(06-03-2025)--record allowing the pre-register when the
	// approval config retired
	// start
	public short getActiveApprovalConfigId(final int ntransactionstatus) throws Exception;
	// end

}
