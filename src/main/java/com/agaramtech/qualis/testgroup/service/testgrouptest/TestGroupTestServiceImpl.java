package com.agaramtech.qualis.testgroup.service.testgrouptest;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.Enumeration;
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

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class TestGroupTestServiceImpl implements TestGroupTestService {

	private TestGroupTestDAO testGroupTestDAO;

	public TestGroupTestServiceImpl(TestGroupTestDAO testGroupTestDAO) {
		this.testGroupTestDAO = testGroupTestDAO;
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createComponent(Map<String, Object> inputMap, UserInfo objUserInfo,
			TestGroupSpecification objTestGroupSpec, List<TestGroupSpecSampleType> listTestGroupComponent,
			final List<TestGroupTest> listTestGroupTest) throws Exception {
		final Map<String, Object> outputMap = testGroupTestDAO.getCreateComponentTestSeqNo(false, objUserInfo,
				objTestGroupSpec, listTestGroupComponent, listTestGroupTest);
		final String returnStr = (String) outputMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
		if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(returnStr)) {
			final Map<String, Object> outputMap1 = testGroupTestDAO.updateCreateComponentTestSeqNo(false, outputMap);
			return testGroupTestDAO.createComponent(inputMap, objUserInfo, objTestGroupSpec, listTestGroupComponent,
					listTestGroupTest, outputMap1);
		} else {
			return new ResponseEntity<Object>(returnStr, HttpStatus.EXPECTATION_FAILED);
		}

	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteTestGroupComponent(final Map<String, Object> inputMap,
			final UserInfo objUserInfo, final TestGroupSpecification objTestGroupSpec,
			final List<TestGroupSpecSampleType> listTestGroupComponent) throws Exception {
		return testGroupTestDAO.deleteTestGroupComponent(inputMap, objUserInfo, objTestGroupSpec,
				listTestGroupComponent);
	}

	@Override
	public ResponseEntity<Object> getAvailableComponent(UserInfo objUserInfo, TestGroupSpecification objTestGroupSpec)
			throws Exception {
		return testGroupTestDAO.getAvailableComponent(objUserInfo, objTestGroupSpec);
	}

	@Override
	public ResponseEntity<Object> getAvailableTest(UserInfo objUserInfo, TestGroupSpecSampleType objTestGroupComponent,
			int ntreeversiontempcode, final int nprojectmastercode, final int nclinicaltyperequired) throws Exception {
		return testGroupTestDAO.getAvailableTest(objUserInfo, objTestGroupComponent, ntreeversiontempcode,
				nprojectmastercode, nclinicaltyperequired);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createTest(final UserInfo objUserInfo, final TestGroupSpecification objSpecification,
			final List<TestGroupSpecSampleType> listTestGroupComponent, final List<TestGroupTest> listTest)
			throws Exception {

		final Map<String, Object> outputMap = testGroupTestDAO.getCreateComponentTestSeqNo(true, objUserInfo,
				objSpecification, listTestGroupComponent, listTest);

		final String returnStr = (String) outputMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());

		if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(returnStr)) {
			final Map<String, Object> outputMap1 = testGroupTestDAO.updateCreateComponentTestSeqNo(false, outputMap);
			return testGroupTestDAO.createTest(objUserInfo, objSpecification, listTestGroupComponent, listTest,
					outputMap1);
		} else {
			return new ResponseEntity<Object>(returnStr, HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteTest(UserInfo objUserInfo, TestGroupSpecSampleType objTestGroupComponent,
			TestGroupTest objTest, int ntreeversiontempcode, int nprojectmastercode) throws Exception {
		return testGroupTestDAO.deleteTest(objUserInfo, objTestGroupComponent, objTest, ntreeversiontempcode,
				nprojectmastercode);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateTest(UserInfo objUserInfo, TestGroupTest objTest,
			final TestGroupSpecification objGroupSpecification, final TestGroupTestFile objTestFile) throws Exception {
		return testGroupTestDAO.updateTest(objUserInfo, objTest, objGroupSpecification, objTestFile);
	}

	@Override
	public ResponseEntity<Object> getActiveTestById(UserInfo objUserInfo, TestGroupTest objTest,
			int ntreeversiontempcode, int nprojectmastercode) throws Exception {
		return testGroupTestDAO.getActiveTestById(objUserInfo, objTest, ntreeversiontempcode, nprojectmastercode);
	}

	@Override
	public ResponseEntity<Object> getTestByComponentId(final UserInfo objUserInfo,
			final TestGroupSpecSampleType objTestGroupSpecSampleType) throws Exception {
		return testGroupTestDAO.getTestByComponentId(objUserInfo, objTestGroupSpecSampleType);
	}

	@Override
	public ResponseEntity<Object> getTestMasterByCategory(UserInfo objUserInfo, TestCategory objTestCategory,
			final TestGroupSpecSampleType objComponent, final int nallottedspeccode) throws Exception {
		return testGroupTestDAO.getTestMasterByCategory(objUserInfo, objTestCategory, objComponent,
				nallottedspeccode);
	}

//	@Override
//	public ResponseEntity<Object> getComponent(UserInfo objUserInfo, TestGroupSpecification objTestGroupSpecification)
//			throws Exception {
//		return testGroupTestDAO.getComponent(objUserInfo, objTestGroupSpecification);
//	}

	@Override
	public ResponseEntity<Object> getTestGroupComponent(UserInfo objUserInfo, TestGroupSpecification objTestGroupSpec)
			throws Exception {
		return testGroupTestDAO.getTestGroupComponent(objUserInfo, objTestGroupSpec);
	}

	@Override
	public ResponseEntity<Object> getTestGroupTest(final UserInfo objUserInfo, final int testgrouptestcode)
			throws Exception {
		return testGroupTestDAO.getTestGroupTest(objUserInfo, testgrouptestcode);
	}

	@Override
	public ResponseEntity<Object> getTestGroupTestParameter(UserInfo objUserInfo, TestGroupTestParameter objParameter)
			throws Exception {
		return testGroupTestDAO.getTestGroupTestParameter(objUserInfo, objParameter);
	}

	@Override
	public ResponseEntity<Object> getTestGroupTestMaterial(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return testGroupTestDAO.getTestGroupTestMaterial(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getTestGroupFormula(TestGroupTestParameter objParameter, final UserInfo objUserInfo,
			final TestGroupSpecification objSpecification) throws Exception {
		return testGroupTestDAO.getTestGroupFormula(objParameter, objUserInfo, objSpecification);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createTestGroupFormula(UserInfo objUserInfo, List<TestGroupTestFormula> listFormula,
			final TestGroupSpecification objGroupSpecification) throws Exception {
		return testGroupTestDAO.createTestGroupFormula(objUserInfo, listFormula, objGroupSpecification);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteTestGroupFormula(UserInfo objUserInfo, TestGroupTestFormula objFormula,
			TestGroupSpecification objGroupSpecification) throws Exception {
		return testGroupTestDAO.deleteTestGroupFormula(objUserInfo, objFormula, objGroupSpecification);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> defaultTestGroupFormula(UserInfo objUserInfo, TestGroupTestFormula objFormula,
			final TestGroupSpecification objGroupSpecification) throws Exception {
		return testGroupTestDAO.defaultTestGroupFormula(objUserInfo, objFormula, objGroupSpecification);
	}

//	@Override
//	public ResponseEntity<Object> updateTestGroupCharParameter(UserInfo objUserInfo,
//			TestGroupTestCharParameter objPredefParameter, TestGroupSpecification objGroupSpecification)
//			throws Exception {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public ResponseEntity<Object> createTestGroupNumericParameter(UserInfo objUserInfo,
//			TestGroupTestNumericParameter objPredefParameter, TestGroupSpecification objGroupSpecification)
//			throws Exception {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public ResponseEntity<Object> updateTestGroupNumericParameter(UserInfo objUserInfo,
//			TestGroupTestNumericParameter objPredefParameter, TestGroupSpecification objGroupSpecification)
//			throws Exception {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public ResponseEntity<Object> deleteTestGroupNumericParameter(UserInfo objUserInfo,
//			TestGroupTestNumericParameter objPredefParameter, TestGroupSpecification objGroupSpecification)
//			throws Exception {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateTestGroupParameter(UserInfo objUserInfo,
			TestGroupTestNumericParameter objNumericParameter, TestGroupTestCharParameter objCharParameter,
			TestGroupTestPredefinedParameter objPredefinedParameter, TestGroupTestParameter objParameter,
			final TestGroupTestFormula testGroupTestFormula) throws Exception {
		return testGroupTestDAO.updateTestGroupParameter(objUserInfo, objNumericParameter, objCharParameter,
				objPredefinedParameter, objParameter, testGroupTestFormula);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> viewTestGroupTestFile(int ntestgroupfilecode, UserInfo objUserInfo,
			final TestGroupSpecification objSpecification, int ntestgrouptestcode) throws Exception {
		return testGroupTestDAO.viewTestGroupTestFile(ntestgroupfilecode, objUserInfo, objSpecification,
				ntestgrouptestcode);
	}

}
