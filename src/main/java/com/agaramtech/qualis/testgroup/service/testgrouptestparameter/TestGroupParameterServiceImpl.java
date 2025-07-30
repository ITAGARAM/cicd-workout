package com.agaramtech.qualis.testgroup.service.testgrouptestparameter;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecification;
import com.agaramtech.qualis.testgroup.model.TestGroupTest;
import com.agaramtech.qualis.testgroup.model.TestGroupTestClinicalSpec;
import com.agaramtech.qualis.testgroup.model.TestGroupTestParameter;
import com.agaramtech.qualis.testgroup.model.TestGroupTestPredefinedParameter;
import com.agaramtech.qualis.testgroup.model.TestGroupTestPredefinedSubCode;

@Service
@Transactional(readOnly = true, rollbackFor = Exception.class)
public class TestGroupParameterServiceImpl implements TestGroupParameterService {

	private TestGroupParameterDAO testGroupParameterDAO;

	public TestGroupParameterServiceImpl(TestGroupParameterDAO testGroupParameterDAO) {
		this.testGroupParameterDAO = testGroupParameterDAO;
	}

	@Override
	public ResponseEntity<Object> getActiveTestParameterById(UserInfo objUserInfo,
			TestGroupTestParameter objTestParameter) throws Exception {
		return testGroupParameterDAO.getActiveTestParameterById(objUserInfo, objTestParameter);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateParameter(UserInfo objUserInfo, TestGroupTestParameter objTestParameter,
			final TestGroupSpecification objTestGroupSpec) throws Exception {
		Map<String, Object> SeqMap = testGroupParameterDAO.getUpdateSequenceParameter(objUserInfo, objTestParameter,
				objTestGroupSpec);
		final String returnStr = (String) SeqMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
		if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(returnStr)) {
			return testGroupParameterDAO.updateParameter(objUserInfo, objTestParameter, objTestGroupSpec, SeqMap);
		} else {
			return new ResponseEntity<Object>(returnStr, HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> getParameterByTestId(UserInfo objUserInfo, TestGroupTest objTest) throws Exception {
		return testGroupParameterDAO.getParameterByTestId(objUserInfo, objTest.getNtestgrouptestcode());
	}

	@Override
	public ResponseEntity<Object> getActivePredefinedParameterById(final UserInfo objUserInfo,
			final TestGroupTestPredefinedParameter objPredefinedParameter,
			final TestGroupSpecification objSpecification) throws Exception {
		return testGroupParameterDAO.getActivePredefinedParameterById(objUserInfo, objPredefinedParameter,
				objSpecification);
	}

	@Override
	public ResponseEntity<Object> getActiveParameterById(UserInfo objUserInfo,
			TestGroupTestParameter objPredefParameter, int ntreeversiontempcode) throws Exception {
		return testGroupParameterDAO.getActiveParameterById(objUserInfo, objPredefParameter, ntreeversiontempcode);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createTestGroupPredefParameter(UserInfo objUserInfo,
			TestGroupTestPredefinedParameter objPredefParameter, TestGroupSpecification objGroupSpecification,
			List<TestGroupTestPredefinedSubCode> lstCodedResult) throws Exception {
		return testGroupParameterDAO.createTestGroupPredefParameter(objUserInfo, objPredefParameter,
				objGroupSpecification, lstCodedResult);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateTestGroupPredefParameter(UserInfo objUserInfo,
			TestGroupTestPredefinedParameter objPredefParameter, TestGroupSpecification objGroupSpecification,
			List<TestGroupTestPredefinedSubCode> lstCodedResult, List<TestGroupTestPredefinedSubCode> lstaddCodedResult)
			throws Exception {
		return testGroupParameterDAO.updateTestGroupPredefParameter(objUserInfo, objPredefParameter,
				objGroupSpecification, lstCodedResult, lstaddCodedResult);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteTestGroupPredefParameter(UserInfo objUserInfo,
			TestGroupTestPredefinedParameter objPredefParameter, TestGroupSpecification objGroupSpecification,
			final List<TestGroupTestPredefinedSubCode> lstCodedResult) throws Exception {
		return testGroupParameterDAO.deleteTestGroupPredefParameter(objUserInfo, objPredefParameter,
				objGroupSpecification, lstCodedResult);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> defaultTestGroupPredefParameter(UserInfo objUserInfo,
			TestGroupTestPredefinedParameter objPredefParameter, TestGroupSpecification objGroupSpecification)
			throws Exception {
		return testGroupParameterDAO.defaultTestGroupPredefParameter(objUserInfo, objPredefParameter,
				objGroupSpecification);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createTestGroupAddSpecification(UserInfo objUserInfo,
			TestGroupTestClinicalSpec objClinicalSpec, TestGroupSpecification objGroupSpecification) throws Exception {
		return testGroupParameterDAO.createTestGroupAddSpecification(objUserInfo, objClinicalSpec,
				objGroupSpecification);
	}

	@Override
	public ResponseEntity<Object> getActiveClinicalSpecById(final UserInfo objUserInfo,
			final TestGroupTestClinicalSpec objClinicalSpec, final TestGroupSpecification objSpecification)
			throws Exception {
		return testGroupParameterDAO.getActiveClinicalSpecById(objUserInfo, objClinicalSpec, objSpecification);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteTestGroupAddSpecification(UserInfo objUserInfo,
			TestGroupTestClinicalSpec objClinicalSpec, TestGroupSpecification objGroupSpecification) throws Exception {
		return testGroupParameterDAO.deleteTestGroupAddSpecification(objUserInfo, objClinicalSpec,
				objGroupSpecification);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateTestGroupAddSpecification(UserInfo objUserInfo,
			TestGroupTestClinicalSpec objClinicalSpec, TestGroupSpecification objGroupSpecification) throws Exception {
		return testGroupParameterDAO.updateTestGroupAddSpecification(objUserInfo, objClinicalSpec,
				objGroupSpecification);
	}

	@Override
	public ResponseEntity<Object> getActivePredefinedParameterSubCodedById(final UserInfo objUserInfo,
			final TestGroupTestPredefinedParameter objPredefinedParameter,
			final TestGroupSpecification objSpecification) throws Exception {
		return testGroupParameterDAO.getActivePredefinedParameterSubCodedById(objUserInfo, objPredefinedParameter,
				objSpecification);
	}

	@Override
	public ResponseEntity<Object> getActivePredefinedParameterSubCoded(final UserInfo objUserInfo,
			final TestGroupTestPredefinedParameter objPredefinedParameter) throws Exception {
		return testGroupParameterDAO.getActivePredefinedParameterSubCoded(objUserInfo, objPredefinedParameter);
	}
}
