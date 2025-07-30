package com.agaramtech.qualis.testgroup.service.testgrouptestparameter;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecification;
import com.agaramtech.qualis.testgroup.model.TestGroupTest;
import com.agaramtech.qualis.testgroup.model.TestGroupTestClinicalSpec;
import com.agaramtech.qualis.testgroup.model.TestGroupTestParameter;
import com.agaramtech.qualis.testgroup.model.TestGroupTestPredefinedParameter;
import com.agaramtech.qualis.testgroup.model.TestGroupTestPredefinedSubCode;

public interface TestGroupParameterService {

	public ResponseEntity<Object> getActiveTestParameterById(final UserInfo objUserInfo,
			final TestGroupTestParameter objTestParameter) throws Exception;

	public ResponseEntity<Object> updateParameter(final UserInfo objUserInfo,
			final TestGroupTestParameter objTestParameter, final TestGroupSpecification objTestGroupSpec)
			throws Exception;

	public ResponseEntity<Object> getParameterByTestId(final UserInfo objUserInfo, final TestGroupTest objTest)
			throws Exception;

	public ResponseEntity<Object> getActivePredefinedParameterById(final UserInfo objUserInfo,
			final TestGroupTestPredefinedParameter objPredefinedParameter,
			final TestGroupSpecification objSpecification) throws Exception;

	public ResponseEntity<Object> getActiveParameterById(final UserInfo objUserInfo,
			final TestGroupTestParameter objPredefParameter, final int ntreeversiontempcode) throws Exception;

	public ResponseEntity<Object> createTestGroupPredefParameter(final UserInfo objUserInfo,
			final TestGroupTestPredefinedParameter objPredefParameter,
			final TestGroupSpecification objGroupSpecification, List<TestGroupTestPredefinedSubCode> lstCodedResult)
			throws Exception;

	public ResponseEntity<Object> updateTestGroupPredefParameter(final UserInfo objUserInfo,
			final TestGroupTestPredefinedParameter objPredefParameter,
			final TestGroupSpecification objGroupSpecification, List<TestGroupTestPredefinedSubCode> lstCodedResult,
			List<TestGroupTestPredefinedSubCode> lstaddCodedResult) throws Exception;

	public ResponseEntity<Object> deleteTestGroupPredefParameter(final UserInfo objUserInfo,
			final TestGroupTestPredefinedParameter objPredefParameter,
			final TestGroupSpecification objGroupSpecification,
			final List<TestGroupTestPredefinedSubCode> lstCodedResult) throws Exception;

	public ResponseEntity<Object> defaultTestGroupPredefParameter(final UserInfo objUserInfo,
			final TestGroupTestPredefinedParameter objPredefParameter,
			final TestGroupSpecification objGroupSpecification) throws Exception;

	public ResponseEntity<Object> createTestGroupAddSpecification(final UserInfo objUserInfo,
			final TestGroupTestClinicalSpec objPredefParameter, final TestGroupSpecification objGroupSpecification)
			throws Exception;

	public ResponseEntity<Object> getActiveClinicalSpecById(final UserInfo objUserInfo,
			final TestGroupTestClinicalSpec objPredefinedParameter, final TestGroupSpecification objSpecification)
			throws Exception;

	public ResponseEntity<Object> deleteTestGroupAddSpecification(final UserInfo objUserInfo,
			final TestGroupTestClinicalSpec objPredefParameter, final TestGroupSpecification objGroupSpecification)
			throws Exception;

	public ResponseEntity<Object> updateTestGroupAddSpecification(final UserInfo objUserInfo,
			final TestGroupTestClinicalSpec objPredefParameter, final TestGroupSpecification objGroupSpecification)
			throws Exception;

	public ResponseEntity<Object> getActivePredefinedParameterSubCodedById(final UserInfo objUserInfo,
			final TestGroupTestPredefinedParameter objPredefinedParameter,
			final TestGroupSpecification objSpecification) throws Exception;

	public ResponseEntity<Object> getActivePredefinedParameterSubCoded(final UserInfo objUserInfo,
			final TestGroupTestPredefinedParameter objPredefinedParameter) throws Exception;

}
