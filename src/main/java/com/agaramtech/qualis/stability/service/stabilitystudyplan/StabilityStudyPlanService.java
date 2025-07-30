package com.agaramtech.qualis.stability.service.stabilitystudyplan;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testgroup.model.TestGroupTest;

public interface StabilityStudyPlanService {

	public ResponseEntity<Object> getStabilityStudyPlan(final UserInfo userInfo, final String currentUIDate) throws Exception;

	public ResponseEntity<Object> insertStbStudyPlan(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> importStabilityStudyPlan(final MultipartHttpServletRequest request,final  UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createTest(final UserInfo userInfo,final  List<String> listSample, final List<TestGroupTest> listTest,
			final int nregtypecode, final int nregsubtypecode,final  Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> createTimePoint(final Map<String, Object> objmap) throws Exception;

	public Map<String, Object> getRegistrationSubSample(final Map<String, Object> inputMap,final  UserInfo userInfo)
			throws Exception;

	public Map<String, Object> getRegistrationTest(final Map<String, Object> inputMap,final  UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getStabilityStudyPlanByFilterSubmit(final Map<String, Object> objmap) throws Exception;

	public ResponseEntity<Object> approveStabilityStudyPlan(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> deleteTest(final Map<String, Object> objmap) throws Exception;

	public ResponseEntity<Object> deleteStbStudyPlan(final Map<String, Object> inputMap) throws Exception;

	public Map<String,Object> getEditStbTimePointDetails(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateStbTimePoint(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> deleteStbTimePoint(final Map<String, Object> objmap) throws Exception;

	public ResponseEntity<Object> getRegTypeBySampleType(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getRegSubTypeByRegType(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getRegTemplateTypeByRegSubType(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getApprovalConfigBasedTemplateDesign(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getRegistrationParameter(final Map<String, Object> inputMap,final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getTimePointHistory(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;
}
