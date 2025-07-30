package com.agaramtech.qualis.testmanagement.service.testmaster;


import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testmanagement.model.ReportInfoTest;
import com.agaramtech.qualis.testmanagement.model.TestContainerType;
import com.agaramtech.qualis.testmanagement.model.TestFile;
import com.agaramtech.qualis.testmanagement.model.TestInstrumentCategory;
import com.agaramtech.qualis.testmanagement.model.TestMaster;
import com.agaramtech.qualis.testmanagement.model.TestMethod;
import com.agaramtech.qualis.testmanagement.model.TestPackageTest;
import com.agaramtech.qualis.testmanagement.model.TestParameter;
import com.agaramtech.qualis.testmanagement.model.TestSection;



@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service

public class TestMasterServiceImpl implements TestMasterService {

	private final TestMasterDAO testMasterDAO;
	private final CommonFunction commonFunction;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class properties.
	 * @param testMasterDAO TestMasterDAO Interface
	 * @param commonFunction CommonFunction holding common utility functions
	 */
	
	public TestMasterServiceImpl(TestMasterDAO testMasterDAO, CommonFunction commonFunction) {
		this.testMasterDAO = testMasterDAO;
		this.commonFunction = commonFunction;
	}
	
	/**
	 * This method is used to retrive a active test category, test and it's details of test parameter, test section, test method, test instrument category,
	 * test file, test formula, test parameter numeric, test predefined parameter
	 * @param userInfo [UserInfo] holds the loggedin user info
	 * @return response entity  object holding response status and list of active testcategory, test and test's details
	 * @throws Exception throws from the DAO layer
 	 */
	@Override
	public ResponseEntity<Object> getTestMaster(final UserInfo userInfo) throws Exception {
		return testMasterDAO.getTestMaster(userInfo);
	}
	
	/**
	 * This method definition is used to retrive a active test by test category and it's details of test parameter, test section, test method, test instrument category,
	 * test file, test formula, test parameter numeric, test predefined parameter
	 * @param userInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity  object holding response status and list of active test and it's details
	 */
	@Override
	public ResponseEntity<Object> getTestMasterByCategory(final int ntestCategoryCode, final UserInfo userInfo) throws Exception {
		return testMasterDAO.getTestMasterByCategory(ntestCategoryCode, userInfo);
	}

	/**
	 * This method definition is used to retrive a test details of test section, test method, test instrument category and test file
	 * If nFlag = 1, to retrive a active test section
	 * If nFlag = 2, to retrive a active test method
	 * If nFlag = 3, to retrive a active test instrument category
	 * If nFlag = 4, to retrive a active test file
	 * If nFlag = 3, to retrive a active test parameter and it's details (test formula, test parameter numeric, test predefined parameter)
	 * @param nFlag [int] hold the value for case no
	 * @param ntestcode [int] holds the primary key of testmaster object
	 * @param ntestparametercode [int] holds the primary key of testparameter object
	 * @return response entity object holding response status and list of active test details
	 */
	@Override
	public ResponseEntity<Object> getOtherTestDetails(final int nFlag, final int ntestcode, final int ntestparametercode, final UserInfo userInfo,final int nclinicaltyperequired) throws Exception {
		return testMasterDAO.getOtherTestDetails(nFlag, ntestcode, ntestparametercode, userInfo,nclinicaltyperequired);
	}

	/**
	 * This method definition is used to retrive a active test data based on test category and it's used in edqm safety marker
	 * @param siteCode [int] holds the mastersitecode value from the loggedin userinfo object
	 * @param ntestcategorycode [int] holds the primarykey of testcategory object
	 * @return response entity holds the list of test category
	 */
	@Override
	public ResponseEntity<Object> getTestMasterBasedOnTestCategory(final int siteCode, final int ntestcategorycode) throws Exception{
		return testMasterDAO.getTestMasterBasedOnTestCategory(siteCode, ntestcategorycode);
	}

	/**
	 * This method definition is used to retrive the list of checklist, parametertype, grade, testparameter and coded result(predefined parameter)
	 * @param userInfo [UserInfo] holds the object of userinfo 
	 * @return response entity object holding response status and list of active checklist, parametertype, grade, testparameter and coded result(predefined parameter)
	 */
	@Override
	public ResponseEntity<Object> getAddTest(final UserInfo userInfo, final Integer ntestCode) throws Exception {
		return testMasterDAO.getAddTest(userInfo, ntestCode);
	}
	
	/**
	 * This method definition is used to retrive the active test object for the specified test code
	 * @param userInfo [UserInfo] holds the object of userinfo
	 * @param ntestcode [int] holds the primary key of testmaster object
	 * @return response entity holds the object of test master
	 */
	@Override
	public ResponseEntity<Object> getActiveTestById(final UserInfo userInfo, final int ntestcode) throws Exception {
		return testMasterDAO.getActiveTestById(userInfo, ntestcode);
	}

	/**
	 * This method definition is used to retrive the list of parameter, and its parameter details (testprefedefined parameter, test parameter numer)
	 * @param ntestCode [int] holds the primary key(ntestcode) of testmaster
	 * @return response entity object holding response status and list of active parameter and it's details
	 */
	@Override
	public ResponseEntity<Object> getTestById(final int ntestCode, final UserInfo userInfo) throws Exception {
		return testMasterDAO.getTestById(ntestCode, userInfo);
	}
	
	/**
	 * This method definition is used to create a new test
	 * @param objTestMaster [TestMaster] holds the details of testmaster
	 * @param userInfo [UserInfo] holds the object of loggedin userinfo
	 * @param objTestParameter [TestParameter] holds the details of testparameter
	 * @param objSection [TestSection] holds the details of testsection
	 * @param objMethod [TestMethod] holds the details of testmethod
	 * @param objTestInstCat [TestInstrumentCategory] holds the details of test instrumentcategory
	 * @return response entity holds the list of testmaster, selectedtest, testparameter, selectedparameter and parameter details based on parameter type
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> createTestMaster(final TestMaster objTestMaster, final UserInfo userInfo, final TestParameter objTestParameter, 
			final TestSection objSection, final TestMethod objMethod, final TestInstrumentCategory objTestInstCat,TestPackageTest objTestpackage,final int isQualisLite ) throws Exception {
		return testMasterDAO.createTestMaster(objTestMaster, userInfo, objTestParameter, objSection, objMethod, objTestInstCat,objTestpackage,isQualisLite);
	}

	/**
	 * This method definition is used to update the testmaster details
	 * @param objTestMaster [TestMaster] holds the details of testmaster
	 * @param userInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of testmaster, selectedtest, testparameter, selectedparameter and parameter details based on parameter type
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> updateTestMaster(final TestMaster objTestMaster, final UserInfo userInfo,
			final boolean validateTest,final int isQualisLite) throws Exception {
		return testMasterDAO.updateTestMaster(objTestMaster, userInfo, validateTest,isQualisLite);
	}

	/**
	 * This method is used to delete the testmaster
	 * @param objTestMaster [TestMaster] holds the details of testmaster
	 * @param userInfo [UserInfo] holds the object of loggedin userinfo
	 * @return  response entity holds the list of testmaster, selectedtest, testparameter, selectedparameter and parameter details based on parameter type
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> deleteTestMaster(final TestMaster objTestMaster, final UserInfo userInfo,final int isQualisLite) throws Exception {
		return testMasterDAO.deleteTestMaster(objTestMaster, userInfo,isQualisLite);
	}

	/**
	 * This method definition is used to retrive the available section which is not available in testsection
	 * @param objTestMaster [TestMaster] holds the details of testmaster
	 * @param userInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of section and response status
	 */
	@Override
	public ResponseEntity<Object> getAvailableSection(final TestMaster objTestMaster, final UserInfo userInfo) throws Exception {
		return testMasterDAO.getAvailableSection(objTestMaster, userInfo);
	}

	/**
	 * This method definition is used to create a test section
	 * @param lstTestSection [List] holds the list of test section details
	 * @param userInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of test section and response status
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> createTestSection(final List<TestSection> lstTestSection, final UserInfo userInfo) throws Exception {
		return testMasterDAO.createTestSection(lstTestSection, userInfo);
	}

	/**
	 * This method definition is used to delete a testsection
	 * @param objTestSection [TestSection] holds the details of test section
	 * @param userInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of testsection
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> deleteTestSection(final TestSection objTestSection, final UserInfo userInfo,final int isQualisLite) throws Exception {
		return testMasterDAO.deleteTestSection(objTestSection, userInfo,isQualisLite);
	}

	/**
	 * This method definition is used to retrive the available method which is not available in testmethod
	 * @param objTestMaster [TestMaster] holds the details of testmaster
	 * @param userInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of method
	 */
	@Override
	public ResponseEntity<Object> getAvailableMethod(final TestMaster objTestMaster, final UserInfo userInfo) throws Exception {
		return testMasterDAO.getAvailableMethod(objTestMaster, userInfo);
	}
	
	/**
	 * This method definition is used to create a test method
	 * @param lstTestMethod [List] holds the list of test method details
	 * @param userInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of test method
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> createTestMethod(final List<TestMethod> lstTestMethod, final UserInfo userInfo,final int isQualisLite) throws Exception {
		return testMasterDAO.createTestMethod(lstTestMethod, userInfo,isQualisLite);
	}

	/**
	 * This method definition is used to delete a test method
	 * @param testMethod [TestMethod] holds the details of testmethod
	 * @param userInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of method
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> deleteTestMethod(final TestMethod testMethod, final UserInfo userInfo,final int isQualisLite) throws Exception {
		return testMasterDAO.deleteTestMethod(testMethod, userInfo,isQualisLite);
	}

	/**
	 * This method definition is used to retrive the available instrument category
	 * @param objTestMaster [TestMaster] holds the details of testmaster
	 * @param userInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of instrumentcategory and response status
	 */
	@Override
	public ResponseEntity<Object> getAvailableInstrumentCategory(final TestMaster objTestMaster, final UserInfo userInfo) throws Exception {
		return testMasterDAO.getAvailableInstrumentCategory(objTestMaster, userInfo);
	}
	
	
	
	@Override
	public ResponseEntity<Object> getAvailablePackage(final TestMaster objTestMaster, final UserInfo userInfo) throws Exception {
		return testMasterDAO.getAvailablePackage(objTestMaster, userInfo);
	}

	/**
	 * This method definition is used to create a test instrumentcategory
	 * @param lstTestInstrumentCategory [List] holds the list of test instrumentcategory details
	 * @param userInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of test instrumentcategory
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> createInstrumentCategory(final List<TestInstrumentCategory> lstTestInstrumentCategory, final UserInfo userInfo,final int isQualisLite) throws Exception {
		return testMasterDAO.createInstrumentCategory(lstTestInstrumentCategory, userInfo,isQualisLite);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> createTestpackage(final List<TestPackageTest> lstTestInstrumentCategory, final UserInfo userInfo,final int isQualisLite) throws Exception {
		return testMasterDAO.createTestpackage(lstTestInstrumentCategory, userInfo,isQualisLite);
	}
	
	/**
	 * This method definition is used to delete a test instrument category
	 * @param testInstrumentCategory [TestInstrumentCategory] holds the details of test instrumentcategory
	 * @param userInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of test instrumentcategory
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> deleteInstrumentCategory(final TestInstrumentCategory testInstrumentCategory, final UserInfo userInfo,final int isQualisLite) throws Exception {
		return testMasterDAO.deleteInstrumentCategory(testInstrumentCategory, userInfo,isQualisLite);
	}

	
	@Override
	@Transactional
	public ResponseEntity<Object> deletePackage(final TestPackageTest testInstrumentCategory, final UserInfo userInfo) throws Exception {
		return testMasterDAO.deletePackage(testInstrumentCategory, userInfo);
	}
	/**
	 * This method is used to copy the test details
	 * @param objTestMaster [TestMaster] object holds the test details
	 * @param userInfo [UserInfo] object holds the loggedin user info
	 * @return response entity of 'testmaster', 'testparameter', 'testformula', 'testnumericparameter', 'testpredefinedparameter' entity
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> copyTestMaster(final TestMaster objTestMaster, final UserInfo userInfo,final int isQualisLite) throws Exception {
		Map<String, Object> outputMap = testMasterDAO.getSeqNoToCopyTestMaser(objTestMaster, userInfo,isQualisLite);
		final String returnStr = (String) outputMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
	
			if((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(returnStr)) {
				outputMap.put("isQualisLite", isQualisLite);
			return testMasterDAO.copyTestMaster(objTestMaster, userInfo, outputMap);
		} else {
			return new ResponseEntity<Object>(returnStr, HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method definition is used to set a default test section
	 * @param objTestSection [TestSection] object holds the details for test section to set default
	 * @param userInfo [UserInfo] object holds the loggedin user info
	 * @return response entity of 'testsection' entity
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> setDefaultTestSection(final TestSection objTestSection, final UserInfo userInfo,final int isQualisLite) throws Exception {
		return testMasterDAO.setDefaultTestSection(objTestSection, userInfo,isQualisLite);
	}

	/**
	 * This method definition is used to set a default test method
	 * @param testMethod [TestMethod] object holds the details for test method to set default
	 * @param userInfo [UserInfo] object holds the loggedin user info
	 * @return response entity of 'testmethod' entity
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> setDefaultTestMethod(final TestMethod testMethod, final UserInfo userInfo,final int isQualisLite) throws Exception {
		return testMasterDAO.setDefaultTestMethod(testMethod, userInfo,isQualisLite);
	}

	/**
	 * This method definition is used to set a default test instrumentcategory
	 * @param testInstrumentCategory [TestInstrumentCategory] object holds the details for test instrumentcategory to set default
	 * @param userInfo [UserInfo] object holds the loggedin user info
	 * @return response entity of 'testinstrumentcategory' entity
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> setDefaultTestInstrumentCategory(final TestInstrumentCategory testInstrumentCategory, final UserInfo userInfo,final int isQualisLite) throws Exception {
		return testMasterDAO.setDefaultTestInstrumentCategory(testInstrumentCategory, userInfo,isQualisLite);
	}
	
	//ALPD-3510
	@Override
	@Transactional
	public ResponseEntity<Object> setDefaultPackage(final TestPackageTest testInstrumentCategory, final UserInfo userInfo,final int isQualisLite) throws Exception {
		return testMasterDAO.setDefaultPackage(testInstrumentCategory, userInfo,isQualisLite);
	}
	
	/**
	 * This method definition is used to create new file/ link
	 * @param request holds the loggedin userinfo, list of test file details, file count, uploadedfile and its uniquevalue
	 * @return response entity holds the list of test file details
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> createTestFile(MultipartHttpServletRequest request, final UserInfo userInfo) throws Exception {
		return testMasterDAO.createTestFile(request, userInfo);
	}
	
	/**
	 * This method definition is used to update a file/ link
	 * @param request holds the list of test file details, file count, uploadedfile and its uniquevalue
	 * @param userInfo [UserInfo] holds the loggedin userinfo
	 * @return response entity of 'testfile' entity
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> updateTestFile(MultipartHttpServletRequest request, final UserInfo userInfo) throws Exception {
		return testMasterDAO.updateTestFile(request, userInfo);
	}

	/**
	 * This method definition is used to delete a file/ link
	 * @param testFile [TestFile] object holds the details of test file
	 * @param userInfo [UserInfo] object holds the loggedin user info
	 * @return response entity of 'testfile' entity
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> deleteTestFile(final TestFile testFile, final UserInfo userInfo,final int isQualisLite) throws Exception {
		return testMasterDAO.deleteTestFile(testFile, userInfo,isQualisLite);
	}

	/**
	 * This method definition is used to fetch a active test file using primarykey
	 * @param testFile [TestFile] object holds the details of test file
	 * @param userInfo [UserInfo] object holds the loggedin user info
	 * @return response entity of 'testfile' entity
	 */
	@Override
	public ResponseEntity<Object> editTestFile(TestFile testFile, UserInfo userInfo) throws Exception {
		return testMasterDAO.editTestFile(testFile, userInfo);
	}

	/**
	 * This method definition is used to fetch a file/ link which need to view
	 * @param testFile [TestFile] object holds the details of test file
	 * @param userInfo [UserInfo] object holds the loggedin user info
	 * @return response entity of 'testfile' entity
	 */
	@Override
	@Transactional
	public Map<String, Object> viewAttachedTestFile(TestFile testFile, UserInfo userInfo) throws Exception {
		return testMasterDAO.viewAttachedTestFile(testFile, userInfo);
	}

	/**
	 * This method is used to set a default test file to selected test
	 * @param testFile [TestFile] object holding details of test file
	 * @param userInfo [UserInfo] object holding details of logged in user
	 * @return response entity with response status and list of the test file
	 */
	
	@Override
	@Transactional
	public ResponseEntity<Object> setDefaultTestFile(TestFile testFile, UserInfo userInfo,final int isQualisLite) throws Exception {
		return testMasterDAO.setDefaultTestFile(testFile, userInfo,isQualisLite);
	}
	
	/**
	 * This interface declaration is used to fetch a list of test section based on test
	 * @param ntestcode [int] primary key of test object for which the records are to be fetched
	 * @return response entity with response status and list of the test section
	 */
	@Override
	public ResponseEntity<Object> getSection(final int ntestcode) throws Exception {
		return testMasterDAO.getSection(ntestcode);
	}

	/**
	 * This method is used to fetch a list of test method based on test
	 * @param ntestcode [int] primary key of test object for which the records are to be fetched
	 * @return response entity with response status and list of the test method
	 */
	@Override
	public ResponseEntity<Object> getMethod(final int ntestcode) throws Exception {
		return testMasterDAO.getMethod(ntestcode);
	}

	/**
	 * This method is used to fetch a list of test instrument category based on test
	 * @param ntestcode [int] primary key of test object for which the records are to be fetched
	 * @return response entity with response status and list of the test instrument category
	 */
	@Override
	public ResponseEntity<Object> getInstrumentCategory(final int ntestcode) throws Exception {
		return testMasterDAO.getInstrumentCategory(ntestcode);
	}

	/**
	 * This method is used to fetch a list of test file based on test
	 * @param ntestcode [int] primary key of test object for which the records are to be fetched
	 * @return response entity with response status and list of the test file
	 */
	@Override
	public ResponseEntity<Object> getTestAttachment(final int ntestcode) throws Exception {
		return testMasterDAO.getTestAttachment(ntestcode);
	}

	/**
	 * This method is used to validate the selected test is active
	 * @param userInfo [UserInfo] object holding details of logged in user
	 * @param ntestcode [int] primary key of test object for which the records are to be fetched
	 * @return response object with response status and validation message
	 */
	@Override
	public ResponseEntity<Object> validateCopyTest(UserInfo userInfo, int ntestcode) throws Exception {
		return testMasterDAO.validateCopyTest(userInfo, ntestcode);
	}

	public ResponseEntity<Object> validateTestExistenceInTestGroup(final int ntestCode, final UserInfo userInfo) throws Exception {
		return testMasterDAO.validateTestExistenceInTestGroup(ntestCode, userInfo);
	}
	public ResponseEntity<Object> getAvailableContainerType(final TestMaster objTestMaster, final UserInfo userInfo) throws Exception{
		return testMasterDAO.getAvailableContainerType(objTestMaster, userInfo);
	}
	@Transactional
	public ResponseEntity<Object> createTestContainerType(final TestContainerType objcreateTestContainerType, final UserInfo userInfo,final int isQualisLite) throws Exception{
		return testMasterDAO.createTestContainerType(objcreateTestContainerType, userInfo,isQualisLite);
	}
	//ALPD-3510
	@Transactional
	public ResponseEntity<Object> setDefaultTestContainerType(final TestContainerType testContainetType, final UserInfo userInfo,final int isQualisLite) throws Exception
	{
		return testMasterDAO.setDefaultTestContainerType(testContainetType, userInfo,isQualisLite);
	}
	@Transactional
	public ResponseEntity<Object> deleteTestContainerType(final TestContainerType testContainerType, final UserInfo userInfo,final int isQualisLite) throws Exception{
		return testMasterDAO.deleteTestContainerType(testContainerType, userInfo,isQualisLite);
	}
//	public ResponseEntity<Object> getTestContainterType(final int ntestcode, final UserInfo userInfo) throws Exception
//	{
//		return testMasterDAO.getTestContainterType(ntestcode, userInfo);
//	}
	@Override
	public ResponseEntity<Object> getActiveTestContainerTypeById(final int ntestcontainertypecode,final UserInfo userInfo) throws Exception {
		
			final TestContainerType testContainerType = (TestContainerType) testMasterDAO.getActiveTestContainerTypeById(ntestcontainertypecode,userInfo);
			if (testContainerType == null) {
				//status code:417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			else {
				return new ResponseEntity<>(testContainerType, HttpStatus.OK);
			}
		}	
		
	@Override
	@Transactional
	public ResponseEntity<Object> updateTestContainerType(Map<String, Object> inputMap,UserInfo userInfo,final int isQualisLite) throws Exception{
		
		return testMasterDAO.updateTestContainerType(inputMap,userInfo,isQualisLite); 
	}
	
	@Override
	public ResponseEntity<Object> getInterfaceType(UserInfo userInfo) throws Exception {
		
		return testMasterDAO.getInterfaceType(userInfo);
	}
	@Override
	public ResponseEntity<Object> getContainerType(final int ntestcode) throws Exception {
		return testMasterDAO.getContainerType(ntestcode);
	}
	@Override
	public ResponseEntity<Object> getEditReportInfoTest(Map<String, Object> inputMap, UserInfo userInfo)throws Exception {
		return testMasterDAO.getEditReportInfoTest(inputMap, userInfo);
	}
	
	@Override
	@Transactional
	public ResponseEntity<Object> updateReportInfoTest(ReportInfoTest objReportInfoTest, UserInfo userInfo)throws Exception {
		return testMasterDAO.updateReportInfoTest(objReportInfoTest, userInfo);
	}
	@Override
	public ResponseEntity<Object> getTestPackage(final int ntestcode) throws Exception {
		return testMasterDAO.getTestPackage(ntestcode);
	}
	
	@Override
	public ResponseEntity<Object> getDestinationUnit(final int nunitcode,UserInfo userInfo) throws Exception {
		return testMasterDAO.getDestinationUnit(nunitcode,userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getConversionOperator(final int nsourceunitcode,final int ndestinationunitcode,UserInfo userInfo) throws Exception {
		return testMasterDAO.getConversionOperator(nsourceunitcode,ndestinationunitcode,userInfo);
	}
}
