package com.agaramtech.qualis.testmanagement.service.testparameter;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testmanagement.model.DynamicField;
import com.agaramtech.qualis.testmanagement.model.TestFormula;
import com.agaramtech.qualis.testmanagement.model.TestMasterClinicalSpec;
import com.agaramtech.qualis.testmanagement.model.TestParameter;
import com.agaramtech.qualis.testmanagement.model.TestParameterNumeric;
import com.agaramtech.qualis.testmanagement.model.TestPredefinedParameter;

public interface TestParameterService {
	/**
	 * This method declaration is used to create a new test parameter
	 * @param objTestParameter [TestParameter] holds the details of testparameter
	 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of testparameter and its parameter details
	 * @throws Exception
	 */
	public ResponseEntity<Object> createTestParameter(final TestParameter objTestParameter, final UserInfo objUserInfo,final int isQualisLite) throws Exception;
	
	/**
	 * This method declaration is used to update the test parameter
	 * @param objTestParameter [TestParameter] holds the details of testparameter
	 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of testparameter and its parameter details
	 * @throws Exception
	 */
	public ResponseEntity<Object> updateTestParameter(final TestParameter objTestParameter, final UserInfo objUserInfo,final int isQualisLite) throws Exception;
	
	/**
	 * This method declaration is used to delete a test parameter
	 * @param objTestParameter [TestParameter] holds the details of testparameter
	 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of testparameter and its parameter details
	 * @throws Exception
	 */
	public ResponseEntity<Object> deleteTestParameter(final TestParameter objTestParameter, final UserInfo objUserInfo,final int isQualisLite) throws Exception;
	
	/**
	 * This method declaration is used to fetch parameter using primaykey
	 * @param ntestParameterCode [int] primary key of testparameter
	 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the object of test parameter
	 * @throws Exception
	 */
	public ResponseEntity<Object> getActiveParameterById(int ntestParameterCode, UserInfo objUserInfo) throws Exception;
	
	/**
	 * This method declaration is used to fetch a coded result by primarykey
	 * @param ntestPredefinedCode [int] primarykey of testpredefinedparameter
	 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the object of TestPredefinedParameter
	 * @throws Exception
	 */
	public ResponseEntity<Object> getActivePredefinedParameterById(final int ntestPredefinedCode, final UserInfo objUserInfo) throws Exception;
	
	/**
	 * This method declaration is used to create a new coded result/ test predefined parameter
	 * @param objTestPredefinedParameter [TestPredefinedParameter] holds the details of testpredefinedparameter
	 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of testpredefinedparameter
	 * @throws Exception
	 */
	public ResponseEntity<Object> createTestPredefinedParameter(final TestPredefinedParameter objTestPredefinedParameter, final UserInfo objUserInfo,final int isQualisLite) throws Exception;
	
	/**
	 * This method declaration is used to update a coded result/ test predefined parameter
	 * @param objTestPredefinedParameter [TestPredefinedParameter] holds the details of testpredefinedparameter
	 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of testpredefinedparameter
	 * @throws Exception
	 */
	public ResponseEntity<Object> updateTestPredefinedParameter(final TestPredefinedParameter objTestPredefinedParameter, final UserInfo objUserInfo,final int isQualisLite) throws Exception;
	
	/**
	 * This method declaration is used to delete a coded result/ test predefined parameter
	 * @param objTestPredefinedParameter [TestPredefinedParameter] holds the details of testpredefinedparameter
	 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of testpredefinedparameter
	 * @throws Exception
	 */
	public ResponseEntity<Object> deleteTestPredefinedParameter(final TestPredefinedParameter objTestPredefinedParameter, final UserInfo objUserInfo,final int isQualisLite) throws Exception;
	
	/**
	 * This method declaration is used to fetch a message based on the parameter specification count
	 * @param ntestParameterCode [int] primarykey of the testparameter
	 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the alert message
	 * if the count>0 then return the message as cannot add more than one numeric specification
	 * else return the message as success
	 * @throws Exception
	 */
	public ResponseEntity<Object> getParameterSpecificationByCount(int ntestParameterCode, UserInfo objUserInfo) throws Exception;
	
	/**
	 * This method declaration is used to create a test parameter numeric
	 * @param objTestParameterNumeric [TestParameterNumeric] holds the details of testparameternumeric
	 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of testparameternumeric
	 * @throws Exception
	 */
	public ResponseEntity<Object> createTestParameterNumeric(final TestParameterNumeric objTestParameterNumeric, final UserInfo objUserInfo,final int isQualisLite) throws Exception;
	
	/**
	 * This method declaration is used to update a test parameter numeric
	 * @param objTestParameterNumeric [TestParameterNumeric] holds the details of testparameternumeric
	 * @param objUserInfo  
	 * @return response entity holds the list of testparameternumeric
	 * @throws Exception
	 */
	public ResponseEntity<Object> updateTestParameterNumeric(final TestParameterNumeric objTestParameterNumeric, final UserInfo objUserInfo,final int isQualisLite) throws Exception;
	
	/**
	 * This method declaration is used to delete a test parameter numeric
	 * @param objTestParameterNumeric [TestParameterNumeric] holds the details of testparameternumeric
	 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of testparameternumeric
	 * @throws Exception
	 */
	public ResponseEntity<Object> deleteTestParameterNumeric(final TestParameterNumeric objTestParameterNumeric, final UserInfo objUserInfo,final int isQualisLite) throws Exception;
	
	/**
	 * This method declaration is used to fetch a parameter numeric specification using primaykey
	 * @param ntestParamNumericCode [int] primarykey of testparameternumeric object
	 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the object of testparameternumeric
	 * @throws Exception
	 */
	public ResponseEntity<Object> getParameterSpecificationById(int ntestParamNumericCode, UserInfo objUserInfo) throws Exception;
	
	/**
	 * This method declaration is used to fetch TestCategory, TestMaster, DynamicFormulaFields, operators, functions
	 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds list of TestCategory, TestMaster, DynamicFormulaFields, operators, functions
	 * @throws Exception
	 */
	public ResponseEntity<Object> addTestFormula(final UserInfo objUserInfo) throws Exception;
	
	/**
	 * This method declaration is used to fetch TestMaster, DynamicFormulaFields
	 * @param ntestCategoryCode [int] primarykey of testcategory object
	 * @return response entity holds list of TestMaster and DynamicFormulaFields
	 * @throws Exception
	 */
	public ResponseEntity<Object> changeTestCatgoryInFormula(final int ntestCategoryCode, final UserInfo objUserInfo) throws Exception;
	
	/**
	 * This method declaration is used to fetch DynamicFormulaFields
	 * @param ntestCode [int] primarykey of test object
	 * @return response entity holds list of DynamicFormulaFields
	 * @throws Exception
	 */
	public ResponseEntity<Object> changeTestInFormula(final int ntestCode,final UserInfo objUserInfo) throws Exception;
	
	/**
	 * This method declaration is used to create a new test formula
	 * @param objTestFormula [TestFormula] holds the object of test formula details
	 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of test formula
	 * @throws Exception
	 */
	public ResponseEntity<Object> createTestFormula(final TestFormula objTestFormula, final UserInfo objUserInfo,final int isQualisLite) throws Exception;
	
	/**
	 * This method declaration is used to delete the test formula
	 * @param objTestFormula [TestFormula] holds the object of test formula details
	 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of test formula
	 * @throws Exception
	 */
	public ResponseEntity<Object> deleteTestFormula(final TestFormula objTestFormula, final UserInfo objUserInfo,final int isQualisLite) throws Exception;
	
	/**
	 * This method declaration is used to validate the entered test formula
	 * @param sformula [String] holds the formulacalculationcode based on entered formula
	 * @param ntestcode [int] holds the primarykey of testmaster object
	 * @return response entity holds the list of DynamicFormulaFields
	 * @throws Exception
	 */
	public ResponseEntity<Object> validateTestFormula(final String sformula, final int ntestcode) throws Exception;
	
	/**
	 * This method declaration is used to calculate the formula based on entered value by user
	 * @param sformulacalculationcode [String] holds the formulacalculationcode based on entered formula
	 * @param lstDynamicField holds the value of dynamic field entered by user
	 * @param inputMap may contains the dynamicfield and dynamicfield
	 * @return response entity holds the result and query
	 * @throws Exception
	 */
	public ResponseEntity<Object> calculateFormula(String sformulacalculationcode, final List<DynamicField> lstDynamicField, Map<String, Object> inputMap) throws Exception;
	
	/**
	 * This method declaration is used to set a default coded result 
	 * @param objTestPredefinedParameter [TestPredefinedParameter] object holds the details to set the default coded result
	 * @param objUserInfo [UserInfo] object holds the loggedin user info
	 * @return response entity of 'testpredefinedparameter' entity
	 * @throws Exception
	 */
	public ResponseEntity<Object> setDefaultTestPredefinedParameter(final TestPredefinedParameter objTestPredefinedParameter, final UserInfo objUserInfo,final int isQualisLite) throws Exception;
	
	/**
	 * This method declaration is used to set a default test formula
	 * @param objTestFormula [TestFormula] object holds the details for test formula to set default
	 * @param objUserInfo [UserInfo] object holds the loggedin user info
	 * @return response entity of 'testformula' entity
	 * @throws Exception
	 */
	public ResponseEntity<Object> setDefaultTestFormula(final TestFormula objTestFormula, final UserInfo objUserInfo,final int isQualisLite) throws Exception;
	
	public ResponseEntity<Object> createTestParameterClinicalSpec(final TestMasterClinicalSpec objTestParameterNumeric, final UserInfo objUserInfo,final int isQualisLite) throws Exception;
	
	public ResponseEntity<Object> updateTestParameterClinicalSpec(final TestMasterClinicalSpec objTestParameterNumeric, final UserInfo objUserInfo,final int isQualisLite) throws Exception;

	public ResponseEntity<Object> deleteTestParameterClinicalSpec(final TestMasterClinicalSpec objTestParameterNumeric, final UserInfo objUserInfo,final int isQualisLite) throws Exception;

	public ResponseEntity<Object> getTestParameterClinicalSpecById(int ntestParamNumericCode, UserInfo objUserInfo) throws Exception;
	
	public ResponseEntity<Object> getPreDefinedFormula(final UserInfo objUserInfo) throws Exception;
	
	public ResponseEntity<Object> getResultAccuracy(final UserInfo objUserInfo) throws Exception;

}
