package com.agaramtech.qualis.testmanagement.service.testparameter;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testmanagement.model.DynamicField;
import com.agaramtech.qualis.testmanagement.model.TestFormula;
import com.agaramtech.qualis.testmanagement.model.TestMasterClinicalSpec;
import com.agaramtech.qualis.testmanagement.model.TestParameter;
import com.agaramtech.qualis.testmanagement.model.TestParameterNumeric;
import com.agaramtech.qualis.testmanagement.model.TestPredefinedParameter;



@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class TestParameterServiceImpl implements TestParameterService{
	
	private final TestParameterDAO objTestParameterDAO;
	private final CommonFunction commonFunction;
	
	public TestParameterServiceImpl(TestParameterDAO objTestParameterDAO, CommonFunction commonFunction) {
		this.objTestParameterDAO = objTestParameterDAO;
		this.commonFunction = commonFunction;
	}
	/**
	 * This method definition is used to create a new test parameter
	 * @param objTestParameter [TestParameter] holds the details of testparameter
	 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of testparameter and its parameter details
	 */
	
	
	@Override
	@Transactional
	public ResponseEntity<Object> createTestParameter(final TestParameter objTestParameter, final UserInfo objUserInfo,final int isQualisLite) throws Exception {
		return objTestParameterDAO.createTestParameter(objTestParameter, objUserInfo,isQualisLite);
	}
	
	/**
	 * This method definition is used to update the test parameter
	 * @param objTestParameter [TestParameter] holds the details of testparameter
	 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of testparameter and its parameter details
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> updateTestParameter(final TestParameter objTestParameter, final UserInfo objUserInfo,final int isQualisLite) throws Exception {
		Map<String, Object> objMap = objTestParameterDAO.updateTestParameter(objTestParameter, objUserInfo,isQualisLite);
		String returnStr = (String) objMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
		
			if((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(returnStr)) {
			return objTestParameterDAO.getTestParameterAfterUpdate(objTestParameter, objUserInfo);
		} else if(returnStr.equals(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus())) {
			//Conflict = 409 - Duplicate entry
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(returnStr, objUserInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		} else {
			//Conflict = 417
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(returnStr, objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * This method definition is used to delete a test parameter
	 * @param objTestParameter [TestParameter] holds the details of testparameter
	 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of testparameter and its parameter details
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> deleteTestParameter(final TestParameter objTestParameter, final UserInfo objUserInfo,final int isQualisLite) throws Exception {
		return objTestParameterDAO.deleteTestParameter(objTestParameter, objUserInfo,isQualisLite);
	}
	
	/**
	 * This method definition is used to fetch parameter using primaykey
	 * @param ntestParameterCode [int] primary key of testparameter
	 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the object of test parameter
	 */
	@Override
	public ResponseEntity<Object> getActiveParameterById(int ntestParameterCode, UserInfo objUserInfo) throws Exception {
		return objTestParameterDAO.getActiveParameterById(ntestParameterCode, objUserInfo);
	}
	
	/**
	 * This method definition is used to fetch a coded result by primarykey
	 * @param ntestPredefinedCode [int] primarykey of testpredefinedparameter
	 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the object of TestPredefinedParameter
	 */
	@Override
	public ResponseEntity<Object> getActivePredefinedParameterById(final int ntestPredefinedCode, final UserInfo objUserInfo) throws Exception {
		return objTestParameterDAO.getActivePredefinedParameterById(ntestPredefinedCode, objUserInfo);
	}

	/**
	 * This method definition is used to create a new coded result/ test predefined parameter
	 * @param objTestPredefinedParameter [TestPredefinedParameter] holds the details of testpredefinedparameter
	 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of testpredefinedparameter
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> createTestPredefinedParameter(final TestPredefinedParameter objTestPredefinedParameter, final UserInfo objUserInfo,final int isQualisLite) throws Exception {
		return objTestParameterDAO.createTestPredefinedParameter(objTestPredefinedParameter, objUserInfo,isQualisLite);
	}

	/**
	 * This method definition is used to update a coded result/ test predefined parameter
	 * @param objTestPredefinedParameter [TestPredefinedParameter] holds the details of testpredefinedparameter
	 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of testpredefinedparameter
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> updateTestPredefinedParameter(final TestPredefinedParameter objTestPredefinedParameter, final UserInfo objUserInfo,final int isQualisLite) throws Exception {
		return objTestParameterDAO.updateTestPredefinedParameter(objTestPredefinedParameter, objUserInfo,isQualisLite);
	}

	/**
	 * This method definition is used to delete a coded result/ test predefined parameter
	 * @param objTestPredefinedParameter [TestPredefinedParameter] holds the details of testpredefinedparameter
	 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of testpredefinedparameter
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> deleteTestPredefinedParameter(final TestPredefinedParameter objTestPredefinedParameter, final UserInfo objUserInfo,final int isQualisLite) throws Exception {
		return objTestParameterDAO.deleteTestPredefinedParameter(objTestPredefinedParameter, objUserInfo,isQualisLite);
	}
	
	/**
	 * This method definition is used to fetch a message based on the parameter specification count
	 * @param ntestParameterCode [int] primarykey of the testparameter
	 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the alert message
	 * if the count>0 then return the message as cannot add more than one numeric specification
	 * else return the message as success
	 */
	@Override
	public ResponseEntity<Object> getParameterSpecificationByCount(int ntestParameterCode, UserInfo objUserInfo) throws Exception {
		return objTestParameterDAO.getParameterSpecificationByCount(ntestParameterCode, objUserInfo);
	}

	/**
	 * This method definition is used to create a test parameter numeric
	 * @param objTestParameterNumeric [TestParameterNumeric] holds the details of testparameternumeric
	 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of testparameternumeric
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> createTestParameterNumeric(final TestParameterNumeric objTestParameterNumeric, final UserInfo objUserInfo,final int isQualisLite) throws Exception {
		return objTestParameterDAO.createTestParameterNumeric(objTestParameterNumeric, objUserInfo,isQualisLite);
	}

	/**
	 * This method definition is used to update a test parameter numeric
	 * @param objTestParameterNumeric [TestParameterNumeric] holds the details of testparameternumeric
	 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of testparameternumeric
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> updateTestParameterNumeric(final TestParameterNumeric objTestParameterNumeric, final UserInfo objUserInfo,final int isQualisLite) throws Exception {
		return objTestParameterDAO.updateTestParameterNumeric(objTestParameterNumeric, objUserInfo,isQualisLite);
	}

	/**
	 * This method definition is used to delete a test parameter numeric
	 * @param objTestParameterNumeric [TestParameterNumeric] holds the details of testparameternumeric
	 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of testparameternumeric
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> deleteTestParameterNumeric(final TestParameterNumeric objTestParameterNumeric, final UserInfo objUserInfo,final int isQualisLite) throws Exception {
		return objTestParameterDAO.deleteTestParameterNumeric(objTestParameterNumeric, objUserInfo,isQualisLite);
	}

	/**
	 * This method definition is used to fetch a parameter numeric specification using primaykey
	 * @param ntestParamNumericCode [int] primarykey of testparameternumeric object
	 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the object of testparameternumeric
	 */
	@Override
	public ResponseEntity<Object> getParameterSpecificationById(int ntestParamNumericCode, UserInfo objUserInfo) throws Exception {
		return objTestParameterDAO.getParameterSpecificationById(ntestParamNumericCode, objUserInfo);
	}
	
	/**
	 * This method definition is used to fetch TestCategory, TestMaster, DynamicFormulaFields, operators, functions
	 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds list of TestCategory, TestMaster, DynamicFormulaFields, operators, functions
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> addTestFormula(final UserInfo objUserInfo) throws Exception {
		return objTestParameterDAO.addTestFormula(objUserInfo);
	}

	/**
	 * This method definition is used to fetch TestMaster, DynamicFormulaFields
	 * @param ntestCategoryCode [int] primarykey of testcategory object
	 * @return response entity holds list of TestMaster and DynamicFormulaFields
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> changeTestCatgoryInFormula(final int ntestCategoryCode, final UserInfo objUserInfo) throws Exception {
		return objTestParameterDAO.changeTestCatgoryInFormula(ntestCategoryCode,objUserInfo);
	}
	
	/**
	 * This method definition is used to fetch DynamicFormulaFields
	 * @param ntestCode [int] primarykey of test object
	 * @return response entity holds list of DynamicFormulaFields
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> changeTestInFormula(final int ntestCode,final UserInfo objUserInfo) throws Exception {
		return objTestParameterDAO.changeTestInFormula(ntestCode,objUserInfo);
	}
	
	/**
	 * This method definition is used to create a new test formula
	 * @param objTestFormula [TestFormula] holds the object of test formula details
	 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of test formula
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> createTestFormula(final TestFormula objTestFormula, final UserInfo objUserInfo,final int isQualisLite) throws Exception {
		return objTestParameterDAO.createTestFormula(objTestFormula, objUserInfo,isQualisLite);
	}

	/**
	 * This method definition is used to delete the test formula
	 * @param objTestFormula [TestFormula] holds the object of test formula details
	 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of test formula
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> deleteTestFormula(final TestFormula objTestFormula, final UserInfo objUserInfo,final int isQualisLite) throws Exception {
		return objTestParameterDAO.deleteTestFormula(objTestFormula, objUserInfo,isQualisLite);
	}

	/**
	 * This method definition is used to validate the entered test formula
	 * @param sformula [String] holds the formulacalculationcode based on entered formula
	 * @param ntestcode [int] holds the primarykey of testmaster object
	 * @return response entity holds the list of DynamicFormulaFields
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> validateTestFormula(final String sformula, final int ntestcode) throws Exception {
		return objTestParameterDAO.validateTestFormula(sformula, ntestcode);
	}

	/**
	 * This method definition is used to calculate the formula based on entered value by user
	 * @param sformulacalculationcode [String] holds the formulacalculationcode based on entered formula
	 * @param lstDynamicField holds the value of dynamic field entered by user
	 * @param inputMap may contains the dynamicfield and dynamicfield
	 * @return response entity holds the result and query
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> calculateFormula(String query, final List<DynamicField> lst1, Map<String, Object> inputMap) throws Exception {
		return objTestParameterDAO.calculateFormula(query, lst1, inputMap);
	}
	
	/**
	 * This method definition is used to set a default test formula
	 * @param objTestFormula [TestFormula] object holds the details for test formula to set default
	 * @param objUserInfo [UserInfo] object holds the loggedin user info
	 * @return response entity of 'testformula' entity
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> setDefaultTestFormula(final TestFormula objTestFormula, final UserInfo objUserInfo,final int isQualisLite) throws Exception {
		return objTestParameterDAO.setDefaultTestFormula(objTestFormula, objUserInfo,isQualisLite);
	}
	
	/**
	 * This method definition is used to set a default coded result 
	 * @param objTestPredefinedParameter [TestPredefinedParameter] object holds the details to set the default coded result
	 * @param objUserInfo [UserInfo] object holds the loggedin user info
	 * @return response entity of 'testpredefinedparameter' entity
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> setDefaultTestPredefinedParameter(final TestPredefinedParameter objTestPredefinedParameter, final UserInfo objUserInfo,final int isQualisLite) throws Exception {
		return objTestParameterDAO.setDefaultTestPredefinedParameter(objTestPredefinedParameter, objUserInfo,isQualisLite);
	}
	
	@Override
	@Transactional
	public ResponseEntity<Object> createTestParameterClinicalSpec(final TestMasterClinicalSpec objTestParameterNumeric, final UserInfo objUserInfo,final int isQualisLite) throws Exception {
		return objTestParameterDAO.createTestParameterClinicalSpec(objTestParameterNumeric, objUserInfo,isQualisLite);
	}
	
	@Override
	@Transactional
	public ResponseEntity<Object> updateTestParameterClinicalSpec(final TestMasterClinicalSpec objTestParameterNumeric, final UserInfo objUserInfo,final int isQualisLite) throws Exception {
		return objTestParameterDAO.updateTestParameterClinicalSpec(objTestParameterNumeric, objUserInfo,isQualisLite);
	}
	
	@Override
	@Transactional
	public ResponseEntity<Object> deleteTestParameterClinicalSpec(final TestMasterClinicalSpec objTestParameterNumeric, final UserInfo objUserInfo,final int isQualisLite) throws Exception {
		return objTestParameterDAO.deleteTestParameterClinicalSpec(objTestParameterNumeric, objUserInfo,isQualisLite);
	}
	
	@Override  
	public ResponseEntity<Object> getTestParameterClinicalSpecById(int ntestmasterclinicspeccode, UserInfo objUserInfo) throws Exception {
		return objTestParameterDAO.getTestParameterClinicalSpecById(ntestmasterclinicspeccode, objUserInfo);
	}
	
	@Override
	public ResponseEntity<Object> getPreDefinedFormula(final UserInfo objUserInfo) throws Exception {
		return objTestParameterDAO.getPreDefinedFormula(objUserInfo);
	}
	
	@Override
	public ResponseEntity<Object> getResultAccuracy(final UserInfo objUserInfo) throws Exception {
		return objTestParameterDAO.getResultAccuracy(objUserInfo);
	}

}
