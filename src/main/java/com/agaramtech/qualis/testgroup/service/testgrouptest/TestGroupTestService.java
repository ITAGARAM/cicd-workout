package com.agaramtech.qualis.testgroup.service.testgrouptest;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecSampleType;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecification;
import com.agaramtech.qualis.testgroup.model.TestGroupTest;
import com.agaramtech.qualis.testgroup.model.TestGroupTestCharParameter;
import com.agaramtech.qualis.testgroup.model.TestGroupTestFile;
import com.agaramtech.qualis.testgroup.model.TestGroupTestFormula;
import com.agaramtech.qualis.testgroup.model.TestGroupTestNumericParameter;
import com.agaramtech.qualis.testgroup.model.TestGroupTestParameter;
import com.agaramtech.qualis.testgroup.model.TestGroupTestPredefinedParameter;
import com.agaramtech.qualis.testmanagement.model.TestCategory;

public interface TestGroupTestService {

	public ResponseEntity<Object> createComponent(final Map<String, Object> inputMap, final UserInfo objUserInfo,
			final TestGroupSpecification objTestGroupSpec, final List<TestGroupSpecSampleType> listTestGroupComponent,
			final List<TestGroupTest> listTestGroupTest) throws Exception;

	public ResponseEntity<Object> deleteTestGroupComponent(final Map<String, Object> inputMap,
			final UserInfo objUserInfo, final TestGroupSpecification objTestGroupSpec,
			final List<TestGroupSpecSampleType> listTestGroupComponent) throws Exception;

	public ResponseEntity<Object> getAvailableComponent(final UserInfo objUserInfo,
			final TestGroupSpecification objTestGroupSpec) throws Exception;

	public ResponseEntity<Object> getAvailableTest(final UserInfo objUserInfo,
			final TestGroupSpecSampleType objTestGroupComponent, final int ntreeversiontempcode,
			final int nprojectmastercode, int nclinicaltyperequired) throws Exception;

	public ResponseEntity<Object> createTest(final UserInfo objUserInfo, final TestGroupSpecification objSpecification,
			final List<TestGroupSpecSampleType> objTestGroupComponent, final List<TestGroupTest> listTest)
			throws Exception;

	public ResponseEntity<Object> deleteTest(final UserInfo objUserInfo,
			final TestGroupSpecSampleType objTestGroupComponent, final TestGroupTest objTest,
			final int ntreeversiontempcode, final int nprojectmastercode) throws Exception;

	public ResponseEntity<Object> updateTest(final UserInfo objUserInfo, final TestGroupTest objTest,
			final TestGroupSpecification objGroupSpecification, final TestGroupTestFile objTestFile) throws Exception;

	public ResponseEntity<Object> getActiveTestById(final UserInfo objUserInfo, final TestGroupTest objTest,
			final int ntreeversiontempcode, final int nprojectmastercode) throws Exception;

	public ResponseEntity<Object> getTestByComponentId(final UserInfo objUserInfo,
			final TestGroupSpecSampleType objTestGroupSpecSampleType) throws Exception;

	public ResponseEntity<Object> getTestMasterByCategory(final UserInfo objUserInfo,
			final TestCategory objTestCategory, final TestGroupSpecSampleType objComponent, final int nallottedspeccode)
			throws Exception;

	public ResponseEntity<Object> getTestGroupComponent(final UserInfo objUserInfo,
			final TestGroupSpecification objTestGroupSpec) throws Exception;

	public ResponseEntity<Object> getTestGroupTest(final UserInfo objUserInfo, final int testgrouptestcode)
			throws Exception;

	public ResponseEntity<Object> getTestGroupTestParameter(final UserInfo objUserInfo,
			final TestGroupTestParameter objParameter) throws Exception;

	public ResponseEntity<Object> getTestGroupFormula(final TestGroupTestParameter objParameter,
			final UserInfo objUserInfo, final TestGroupSpecification objSpecification) throws Exception;

	public ResponseEntity<Object> createTestGroupFormula(final UserInfo objUserInfo,
			final List<TestGroupTestFormula> listFormula, final TestGroupSpecification objGroupSpecification)
			throws Exception;

	public ResponseEntity<Object> deleteTestGroupFormula(final UserInfo objUserInfo,
			final TestGroupTestFormula objFormula, TestGroupSpecification objGroupSpecification) throws Exception;

	public ResponseEntity<Object> defaultTestGroupFormula(final UserInfo objUserInfo,
			final TestGroupTestFormula objFormula, final TestGroupSpecification objGroupSpecification) throws Exception;

	public ResponseEntity<Object> updateTestGroupParameter(final UserInfo objUserInfo,
			final TestGroupTestNumericParameter objNumericParameter, final TestGroupTestCharParameter objCharParameter,
			final TestGroupTestPredefinedParameter objPredefinedParameter, final TestGroupTestParameter objParameter,
			final TestGroupTestFormula testGroupTestFormula) throws Exception;

//	public ResponseEntity<Object> getComponent(final UserInfo objUserInfo, final TestGroupSpecification objTestGroupSpecification) throws Exception;
	public ResponseEntity<Object> viewTestGroupTestFile(final int ntestgroupfilecode, final UserInfo objUserInfo,
			final TestGroupSpecification objSpecification, int ntestgrouptestcode) throws Exception;

	public ResponseEntity<Object> getTestGroupTestMaterial(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

}
