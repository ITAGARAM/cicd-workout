package com.agaramtech.qualis.testmanagement.service.testmaster;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

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

public interface TestMasterService {

	/**
	 * This method is used to retrive a active test category, test and it's details of test parameter, test section, test method, test instrument category,
	 * test file, test formula, test parameter numeric, test predefined parameter
	 * @param userInfo [UserInfo] holds the loggedin user info
	 * @return response entity  object holding response status and list of active testcategory, test and test's details
 	 */
	public ResponseEntity<Object> getTestMaster(final UserInfo userInfo) throws Exception;
	
	/**
	 * This method declaration is used to retrive a active test by test category and it's details of test parameter, test section, test method, test instrument category,
	 * test file, test formula, test parameter numeric, test predefined parameter
	 * @param userInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity  object holding response status and list of active test and it's details
	 */
	public ResponseEntity<Object> getTestMasterByCategory(final int ntestCategoryCode, final UserInfo userInfo) throws Exception;
	
	/**
	 * This method declaration is used to retrive a test details of test section, test method, test instrument category and test file
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
	public ResponseEntity<Object> getOtherTestDetails(final int nFlag, final int ntestcode, final int ntestparametercode, 
			final UserInfo userInfo,final int nclinicaltyperequired) throws Exception;
	
	/**
	 * This method declaration is used to retrive a active test data based on test category and it's used in edqm safety marker
	 * @param siteCode [int] holds the mastersitecode value from the loggedin userinfo object
	 * @param ntestcategorycode [int] holds the primarykey of testcategory object
	 * @return response entity holds the list of test category
	 */
	public ResponseEntity<Object> getTestMasterBasedOnTestCategory(final int siteCode, final int ntestcategorycode) throws Exception;
	
	/**
	 * This method declaration is used to retrive the list of checklist, parametertype, grade, testparameter and coded result(predefined parameter)
	 * @param userInfo [UserInfo] holds the object of userinfo 
	 * @return response entity object holding response status and list of active checklist, parametertype, grade, testparameter and coded result(predefined parameter)
	 */
	public ResponseEntity<Object> getAddTest(final UserInfo userInfo, final Integer ntestCode) throws Exception;
	
	/**
	 * This method declaration is used to retrive the active test object for the specified test code
	 * @param userInfo [UserInfo] holds the object of userinfo
	 * @param ntestcode [int] holds the primary key of testmaster object
	 * @return response entity holds the object of test master
	 */
	public ResponseEntity<Object> getActiveTestById(final UserInfo userInfo, final int ntestcode) throws Exception;
	
	/**
	 * This method declaration is used to retrive the list of parameter, and its parameter details (testprefedefined parameter, test parameter numer)
	 * @param ntestCode [int] holds the primary key(ntestcode) of testmaster
	 * @return response entity object holding response status and list of active parameter and it's details
	 */
	public ResponseEntity<Object> getTestById(final int ntestCode, final UserInfo userInfo) throws Exception;
	
	/**
	 * This method declaration is used to create a new test
	 * @param objTestMaster [TestMaster] holds the details of testmaster
	 * @param userInfo [UserInfo] holds the object of loggedin userinfo
	 * @param objTestParameter [TestParameter] holds the details of testparameter
	 * @param objSection [TestSection] holds the details of testsection
	 * @param objMethod [TestMethod] holds the details of testmethod
	 * @param objTestInstCat [TestInstrumentCategory] holds the details of test instrumentcategory
	 * @return response entity holds the list of testmaster, selectedtest, testparameter, selectedparameter and parameter details based on parameter type
	 * @throws Exception
	 */
	public ResponseEntity<Object> createTestMaster(final TestMaster objTestMaster, final UserInfo userInfo, final TestParameter objTestParameter, 
			final TestSection objSection, final TestMethod objMethod, final TestInstrumentCategory objTestInstCat,TestPackageTest objTestpackage,final int isQualisLite) throws Exception;
	
	/**
	 * This method declaration is used to update the testmaster details
	 * @param objTestMaster [TestMaster] holds the details of testmaster
	 * @param userInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of testmaster, selectedtest, testparameter, selectedparameter and parameter details based on parameter type
	 * @throws Exception
	 */
	public ResponseEntity<Object> updateTestMaster(final TestMaster objTestMaster, final UserInfo userInfo,
			final boolean validateTest,final int isQualisLite) throws Exception;
	
	/**
	 * This method is used to delete the testmaster
	 * @param objTestMaster [TestMaster] holds the details of testmaster
	 * @param userInfo [UserInfo] holds the object of loggedin userinfo
	 * @return  response entity holds the list of testmaster, selectedtest, testparameter, selectedparameter and parameter details based on parameter type
	 * @throws Exception
	 */
	public ResponseEntity<Object> deleteTestMaster(final TestMaster objTestMaster, final UserInfo userInfo,final int isQualisLite) throws Exception;
	
	/**
	 * This method declaration is used to retrive the available section which is not available in testsection
	 * @param objTestMaster [TestMaster] holds the details of testmaster
	 * @param userInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of section and response status
	 * @throws Exception
	 */
	public ResponseEntity<Object> getAvailableSection(final TestMaster objTestMaster, final UserInfo userInfo) throws Exception;
	
	/**
	 * This method declaration is used to create a test section
	 * @param lstTestSection [List] holds the list of test section details
	 * @param userInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of test section and response status
	 * @throws Exception
	 */
	public ResponseEntity<Object> createTestSection(final List<TestSection> lstTestSection, final UserInfo userInfo) throws Exception;
	
	/**
	 * This method declaration is used to delete a testsection
	 * @param objTestSection [TestSection] holds the details of test section
	 * @param userInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of testsection
	 * @throws Exception
	 */
	public ResponseEntity<Object> deleteTestSection(final TestSection objTestSection, final UserInfo userInfo,final int isQualisLite) throws Exception;
	
	/**
	 * This method declaration is used to retrive the available method which is not available in testmethod
	 * @param objTestMaster [TestMaster] holds the details of testmaster
	 * @param userInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of method and response status
	 * @throws Exception
	 */
	public ResponseEntity<Object> getAvailableMethod(final TestMaster objTestMaster, final UserInfo userInfo) throws Exception;
	
	/**
	 * This method declaration is used to create a test method
	 * @param lstTestMethod [List] holds the list of test method details
	 * @param userInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of test method
	 * @throws Exception
	 */
	public ResponseEntity<Object> createTestMethod(final List<TestMethod> lstTestMethod, final UserInfo userInfo,final int isQualisLite) throws Exception;
	
	/**
	 * This method declaration is used to delete a test method
	 * @param objTestMethod [TestMethod] holds the details of testmethod
	 * @param userInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of method
	 * @throws Exception
	 */
	public ResponseEntity<Object> deleteTestMethod(final TestMethod objTestMethod, final UserInfo userInfo,final int isQualisLite) throws Exception;
	
	/**
	 * This method declaration is used to retrive the available instrument category
	 * @param objTestMaster [TestMaster] holds the details of testmaster
	 * @param userInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of instrumentcategory and response status
	 * @throws Exception
	 */
	public ResponseEntity<Object> getAvailableInstrumentCategory(final TestMaster objTestMaster, final UserInfo userInfo) throws Exception;

	
	public ResponseEntity<Object> getAvailablePackage(final TestMaster objTestMaster, final UserInfo userInfo) throws Exception;

	
	/**
	 * This method declaration is used to create a test instrumentcategory
	 * @param lstTestInstrumentCategory [List] holds the list of test instrumentcategory details
	 * @param userInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of test instrumentcategory
	 * @throws Exception
	 */
	public ResponseEntity<Object> createInstrumentCategory(final List<TestInstrumentCategory> lstTestInstrumentCategory, final UserInfo userInfo,final int isQualisLite) throws Exception;
	
	public ResponseEntity<Object> createTestpackage(final List<TestPackageTest> lstTestInstrumentCategory, final UserInfo userInfo,final int isQualisLite) throws Exception;

	
	/**
	 * This method declaration is used to delete a test instrument category
	 * @param objTestInstrumentCategory [TestInstrumentCategory] holds the details of test instrumentcategory
	 * @param userInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of test instrumentcategory
	 * @throws Exception
	 */
	public ResponseEntity<Object> deleteInstrumentCategory(final TestInstrumentCategory objTestInstrumentCategory, final UserInfo userInfo,final int isQualisLite) throws Exception;
	
	
	public ResponseEntity<Object> deletePackage(final TestPackageTest objTestInstrumentCategory, final UserInfo userInfo) throws Exception;

	/**
	 * This method declaration is used to copy the test details for entered test
	 * @param objTestMaster [TestMaster] object holds the test details
	 * @param userInfo [UserInfo] object holds the loggedin user info
	 * @return response entity of 'testmaster', 'testparameter', 'testformula', 'testnumericparameter', 'testpredefinedparameter' entity
	 * @throws Exception
	 */
	public ResponseEntity<Object> copyTestMaster(final TestMaster objTestMaster, final UserInfo userInfo,final int isQualisLite) throws Exception;
	
	/**
	 * This method declaration is used to set a default test section
	 * @param objTestSection [TestSection] object holds the details for test section to set default
	 * @param userInfo [UserInfo] object holds the loggedin user info
	 * @return response entity of 'testsection' entity
	 * @throws Exception
	 */
	public ResponseEntity<Object> setDefaultTestSection(final TestSection objTestSection, final UserInfo userInfo,final int isQualisLite) throws Exception;
	
	/**
	 * This method declaration is used to set a default test method
	 * @param objTestMethod [TestMethod] object holds the details for test method to set default
	 * @param userInfo [UserInfo] object holds the loggedin user info
	 * @return response entity of 'testmethod' entity
	 * @throws Exception
	 */
	public ResponseEntity<Object> setDefaultTestMethod(final TestMethod objTestMethod, final UserInfo userInfo,final int isQualisLite) throws Exception;
	
	/**
	 * This method declaration is used to set a default test instrumentcategory
	 * @param objTestInstrumentCategory [TestInstrumentCategory] object holds the details for test instrumentcategory to set default
	 * @param userInfo [UserInfo] object holds the loggedin user info
	 * @return response entity of 'testinstrumentcategory' entity
	 * @throws Exception
	 */
	public ResponseEntity<Object> setDefaultTestInstrumentCategory(final TestInstrumentCategory objTestInstrumentCategory, final UserInfo userInfo,final int isQualisLite) throws Exception;
	//ALPD-3510
	public ResponseEntity<Object> setDefaultPackage(final TestPackageTest objTestInstrumentCategory, final UserInfo userInfo,final int isQualisLite) throws Exception;

	/**
	 * This method is used to create new file/ link
	 * @param request holds the list of test file details, file count, uploadedfile and its uniquevalue
	 * @param userInfo [UserInfo] holds the loggedin userinfo
	 * @return response entity of 'testfile' entity
	 * @throws Exception
	 */
	public ResponseEntity<Object> createTestFile(MultipartHttpServletRequest request, final UserInfo userInfo) throws Exception;
	
	/**
	 * This method declaration is used to update a file/ link
	 * @param request holds the list of test file details, file count, uploadedfile and its uniquevalue
	 * @param userInfo [UserInfo] holds the loggedin userinfo
	 * @return response entity of 'testfile' entity
	 * @throws Exception
	 */
	public ResponseEntity<Object> updateTestFile(MultipartHttpServletRequest request, final UserInfo userInfo) throws Exception;
	
	/**
	 * This method declaration is used to delete a file/ link
	 * @param objTestFile [TestFile] object holds the details of test file
	 * @param userInfo [UserInfo] object holds the loggedin user info
	 * @return response entity of 'testfile' entity
	 * @throws Exception
	 */
	public ResponseEntity<Object> deleteTestFile(final TestFile objTestFile, final UserInfo userInfo,final int isQualisLite) throws Exception;
	
	/**
	 * This method declaration is used to fetch a active test file using primarykey
	 * @param objTestFile [TestFile] object holds the details of test file
	 * @param userInfo [UserInfo] object holds the loggedin user info
	 * @return response entity of 'testfile' entity
	 * @throws Exception
	 */
	public ResponseEntity<Object> editTestFile(final TestFile objTestFile, final UserInfo userInfo) throws Exception;
	
	/**
	 * This method declaration is used to fetch a file/ link which need to view
	 * @param objTestFile [TestFile] object holds the details of test file
	 * @param userInfo [UserInfo] object holds the loggedin user info
	 * @return response entity of 'testfile' entity
	 * @throws Exception
	 */
	public Map<String, Object> viewAttachedTestFile(final TestFile objTestFile, final UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to set a default test file to selected test
	 * @param objTestFile [TestFile] object holding details of test file
	 * @param userInfo [UserInfo] object holding details of logged in user
	 * @return response entity with response status and list of the test file
	 * @throws Exception
	 */
	public ResponseEntity<Object> setDefaultTestFile(final TestFile objTestFile, final UserInfo userInfo,final int isQualisLite) throws Exception;
	
	/**
	 * This interface declaration is used to fetch a list of test section based on test
	 * @param ntestcode [int] primary key of test object for which the records are to be fetched
	 * @return response entity with response status and list of the test section
	 * @throws Exception
	 */
	public ResponseEntity<Object> getSection(final int ntestcode) throws Exception;
	
	/**
	 * This interface declaration is used to fetch a list of test method based on test
	 * @param ntestcode [int] primary key of test object for which the records are to be fetched
	 * @return response entity with response status and list of the test method
	 * @throws Exception
	 */
	public ResponseEntity<Object> getMethod(final int ntestcode) throws Exception;
	
	/**
	 * This interface declaration is used to fetch a list of test instrument category based on test
	 * @param ntestcode [int] primary key of test object for which the records are to be fetched
	 * @return response entity with response status and list of the test instrument category
	 * @throws Exception
	 */
	public ResponseEntity<Object> getInstrumentCategory(final int ntestcode) throws Exception;
	
	/**
	 * This interface declaration is used to fetch a list of test file based on test
	 * @param ntestcode [int] primary key of test object for which the records are to be fetched
	 * @return response entity with response status and list of the test file
	 * @throws Exception
	 */
	public ResponseEntity<Object> getTestAttachment(final int ntestcode) throws Exception;
	
	/**
	 * This interface declaration is used to validate the selected test is active
	 * @param userInfo [UserInfo] object holding details of logged in user
	 * @param ntestcode [int] primary key of test object for which the records are to be fetched
	 * @return response object with response status and validation message
	 * @throws Exception
	 */
	public ResponseEntity<Object> validateCopyTest(final UserInfo userInfo ,final int ntestcode) throws Exception;
	
	public ResponseEntity<Object> validateTestExistenceInTestGroup(final int ntestCode, final UserInfo userInfo) throws Exception;
	
    public ResponseEntity<Object> getAvailableContainerType(final TestMaster objTestMaster, final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> createTestContainerType(final TestContainerType objcreateTestContainerType, final UserInfo userInfo,final int isQualisLite) throws Exception;
	//ALPD-3510
	public ResponseEntity<Object> setDefaultTestContainerType(final TestContainerType objTestSection, final UserInfo userInfo,final int isQualisLite) throws Exception;
	
	public ResponseEntity<Object> deleteTestContainerType(final TestContainerType objTestContainerType, final UserInfo userInfo,final int isQualisLite) throws Exception;
	
	//public ResponseEntity<Object> getTestContainterType(final int ntestcode, final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getActiveTestContainerTypeById(final int ntestcontainertypecode,final UserInfo userInfo) throws Exception ;

	public ResponseEntity<Object> updateTestContainerType(Map<String, Object> inputMap, UserInfo userInfo,final int isQualisLite) throws Exception;
	
	public ResponseEntity<Object> getInterfaceType(UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getContainerType(final int ntestcode) throws Exception;
	
	public ResponseEntity<Object> getEditReportInfoTest(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> updateReportInfoTest(ReportInfoTest objReportInfoTest,UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getTestPackage(final int ntestcode) throws Exception;
	
	public ResponseEntity<Object> getDestinationUnit(final int nunitcode,UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getConversionOperator(final int nsourceunitcode, final int ndestinationunitcode,UserInfo userInfo) throws Exception;


}
