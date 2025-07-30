package com.agaramtech.qualis.restcontroller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testmanagement.model.DynamicField;
import com.agaramtech.qualis.testmanagement.model.ReportInfoTest;
import com.agaramtech.qualis.testmanagement.model.TestContainerType;
import com.agaramtech.qualis.testmanagement.model.TestFile;
import com.agaramtech.qualis.testmanagement.model.TestFormula;
import com.agaramtech.qualis.testmanagement.model.TestInstrumentCategory;
import com.agaramtech.qualis.testmanagement.model.TestMaster;
import com.agaramtech.qualis.testmanagement.model.TestMasterClinicalSpec;
import com.agaramtech.qualis.testmanagement.model.TestMethod;
import com.agaramtech.qualis.testmanagement.model.TestPackageTest;
import com.agaramtech.qualis.testmanagement.model.TestParameter;
import com.agaramtech.qualis.testmanagement.model.TestParameterNumeric;
import com.agaramtech.qualis.testmanagement.model.TestPredefinedParameter;
import com.agaramtech.qualis.testmanagement.model.TestSection;
import com.agaramtech.qualis.testmanagement.service.testmaster.TestMasterService;
import com.agaramtech.qualis.testmanagement.service.testparameter.TestParameterService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

/**
 * This controller is used to dispatch the input request to its relevant service methods
 * to access the TestMaster Service methods.
 */

@RestController
@RequestMapping("/testmaster")

public class TestMasterController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TestMasterController.class);

	private RequestContext requestContext;
	private final TestMasterService testMasterService;
	private final TestParameterService testParameterService;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param TestMasterService testMasterService
	 */
	
	public TestMasterController(RequestContext requestContext, TestMasterService testMasterService,TestParameterService testParameterService) {
		this.requestContext = requestContext;
		this.testMasterService = testMasterService;
		this.testParameterService = testParameterService;
	 }	
	
	@PostMapping(value = "/getTestMaster")
	public ResponseEntity<Object> getTestMaster(@RequestBody Map<String, Object> inputMap) throws Exception{		
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class) ;
		LOGGER.info("UserInfo:"+ userInfo);			
		requestContext.setUserInfo(userInfo);
		return testMasterService.getTestMaster(userInfo);
	}
		
	@PostMapping(value = "/getTestMasterByCategory")
	public ResponseEntity<Object> getTestMasterByCategory(@RequestBody Map<String, Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int ntestCategoryCode = (int) inputMap.get("ntestcategorycode");
		requestContext.setUserInfo(userInfo);
		return testMasterService.getTestMasterByCategory(ntestCategoryCode, userInfo);
		
	}	
	
	@PostMapping(value = "/getothertestdetails")
	public ResponseEntity<Object> getOtherTestDetails(@RequestBody Map<String, Object> inputMap) throws Exception {
		final int nFlag = (int) inputMap.get("nFlag");
		final int ntestcode = (int) inputMap.get("ntestcode");
		final int nclinicaltyperequired = (int) inputMap.get("nclinicaltyperequired");
		int ntestparametercode = 0;
		if(inputMap.containsKey("ntestparametercode")) {
			ntestparametercode = (int) inputMap.get("ntestparametercode");
		}
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testMasterService.getOtherTestDetails(nFlag, ntestcode, ntestparametercode, userInfo,nclinicaltyperequired);
	}
	
	@PostMapping(value = "/getTestMasterBasedOnTestCategory")
	public ResponseEntity<Object> getTestMasterBasedOnTestCategory(@RequestBody Map<String, Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		final int ntestcategorycode = (Integer) inputMap.get("ntestcategorycode");
		requestContext.setUserInfo(userInfo);
		return testMasterService.getTestMasterBasedOnTestCategory(userInfo.getNmastersitecode(), ntestcategorycode);
	}
	
	@PostMapping(value = "/getAddTest")
	public ResponseEntity<Object> getAddTest(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		final Integer ntestCode = (Integer) inputMap.get("ntestcode");
		requestContext.setUserInfo(userInfo);
		return testMasterService.getAddTest(userInfo, ntestCode);
	}
	
	@PostMapping(value = "/getActiveTestById")
	public ResponseEntity<Object> getActiveTestById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int ntestcode = (int) inputMap.get("ntestcode");
		requestContext.setUserInfo(userInfo);
		return testMasterService.getActiveTestById(userInfo, ntestcode);
	}
	

	@PostMapping(value = "/getTestById")
	public ResponseEntity<Object> getTestById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final int ntestCode = (int) inputMap.get("ntestcode");
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testMasterService.getTestById(ntestCode, userInfo);
	}
		
		
	@PostMapping(value = "/createTestMaster")
	public ResponseEntity<Object> createTestMaster(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class); 
		final TestMaster objTestMaster = objMapper.convertValue(inputMap.get("testmaster"), TestMaster.class);
		final TestParameter objTestParameter = objMapper.convertValue(inputMap.get("testparameter"), TestParameter.class);
		final TestSection objSection = objMapper.convertValue(inputMap.get("testsection"), TestSection.class);
		final TestMethod objMethod = objMapper.convertValue(inputMap.get("testmethod"), TestMethod.class);
		final TestInstrumentCategory objTestInstCat = objMapper.convertValue(inputMap.get("testinstrumentcategory"), TestInstrumentCategory.class);
		final TestPackageTest objTestpackage = objMapper.convertValue(inputMap.get("testpackagetest"), TestPackageTest.class);
		//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
		final int isQualisLite=(int)inputMap.get("isQualisLite");
		requestContext.setUserInfo(userInfo);
		return testMasterService.createTestMaster(objTestMaster, userInfo, objTestParameter, objSection, objMethod, objTestInstCat,objTestpackage,isQualisLite);
	}
		
		
	@PostMapping(value = "/updateTestMaster")
	public ResponseEntity<Object> updateTestMaster(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final TestMaster objTestMaster = objmapper.convertValue(inputMap.get("testmaster"), TestMaster.class);
		final Boolean validateTest = (Boolean) inputMap.get("validatetest");
		//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
		final int isQualisLite=(int)inputMap.get("isQualisLite");
		requestContext.setUserInfo(userInfo);
		return testMasterService.updateTestMaster(objTestMaster, userInfo, validateTest,isQualisLite);
	}
		
	@PostMapping(value = "/validateTestExistenceInTestGroup")
	public ResponseEntity<Object> validateTestExistenceInTestGroup(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final Integer ntestCode = (Integer) inputMap.get("ntestcode");
		requestContext.setUserInfo(userInfo);
		return testMasterService.validateTestExistenceInTestGroup(ntestCode, userInfo);
	}
		
	@PostMapping(value = "/deleteTestMaster")
	public ResponseEntity<Object> deleteTestMaster(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class); 
		final TestMaster objTestMaster = objMapper.convertValue(inputMap.get("testmaster"), TestMaster.class);
		//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
		final int isQualisLite=(int)inputMap.get("isQualisLite");
		requestContext.setUserInfo(userInfo);
		return testMasterService.deleteTestMaster(objTestMaster, userInfo,isQualisLite);
	}
		
	@PostMapping(value = "/getAvailableSection")
	public ResponseEntity<Object> getAvailableSection(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class); 
		final TestMaster objTestMaster = objMapper.convertValue(inputMap.get("TestMaster"), TestMaster.class);
		requestContext.setUserInfo(userInfo);
		return testMasterService.getAvailableSection(objTestMaster, userInfo);
	}
		
	@PostMapping(value = "/createTestSection")
	public ResponseEntity<Object> createTestSection(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();	
		final List<TestSection> lstTestSection = objMapper.convertValue(inputMap.get("testsection"), new TypeReference<List<TestSection>>() {});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class); 
		requestContext.setUserInfo(userInfo);
		return testMasterService.createTestSection(lstTestSection, userInfo);
	}		
		
	@PostMapping(value = "/deleteTestSection")
	public ResponseEntity<Object> deleteTestSection(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();	
		final TestSection objTestSection = objMapper.convertValue(inputMap.get("testsection"), TestSection.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class); 
		//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
		final int isQualisLite=(int)inputMap.get("isQualisLite");
		requestContext.setUserInfo(userInfo);
		return testMasterService.deleteTestSection(objTestSection, userInfo,isQualisLite);
	}
		
		
	@PostMapping(value = "/getAvailableMethod")
	public ResponseEntity<Object> getAvailableMethod(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		TestMaster objTestMaster = new TestMaster();
		try {
			objTestMaster = objMapper.convertValue(inputMap.get("TestMaster"), TestMaster.class);

		} catch (Exception e) {
			objTestMaster = null;
		}
		requestContext.setUserInfo(userInfo);
		return testMasterService.getAvailableMethod(objTestMaster, userInfo);
	}
		
	@PostMapping(value = "/createTestMethod")
	public ResponseEntity<Object> createTestMethod(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();	
		final List<TestMethod> lstTestMethod = objMapper.convertValue(inputMap.get("testmethod"), new TypeReference<List<TestMethod>>() {});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
		final int isQualisLite =(int)inputMap.get("isQualisLite");
		requestContext.setUserInfo(userInfo);
		return testMasterService.createTestMethod(lstTestMethod, userInfo,isQualisLite);
	}	
		
	@PostMapping(value = "/deleteTestMethod")
	public ResponseEntity<Object> deleteTestMethod(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class); 
		final TestMethod objTestMethod = objMapper.convertValue(inputMap.get("testmethod"), TestMethod.class);
		//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
		final int isQualisLite=(int)inputMap.get("isQualisLite");
		requestContext.setUserInfo(userInfo);
		return testMasterService.deleteTestMethod(objTestMethod, userInfo,isQualisLite);
	}
		
	@PostMapping(value = "/getAvailableInstrumentCategory")
	public ResponseEntity<Object> getAvailableInstrumentCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class); 
		final TestMaster objTestMaster = objMapper.convertValue(inputMap.get("TestMaster"), TestMaster.class);
		requestContext.setUserInfo(userInfo);
		return testMasterService.getAvailableInstrumentCategory(objTestMaster, userInfo);
	}		
		
		
	@PostMapping(value = "/getAvailablePackage")
	public ResponseEntity<Object> getAvailablePackage(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class); 
		final TestMaster objTestMaster = objMapper.convertValue(inputMap.get("TestMaster"), TestMaster.class);
		requestContext.setUserInfo(userInfo);
		return testMasterService.getAvailablePackage(objTestMaster, userInfo);
	}
		
	@PostMapping(value = "/createTestInstrumentCategory")
	public ResponseEntity<Object> createInstrumentCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();	
		final List<TestInstrumentCategory> lstTestInstrumentCategory = objMapper.convertValue(inputMap.get("testinstrumentcategory"), new TypeReference<List<TestInstrumentCategory>>() {});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
		final int isQualisLite=(int)inputMap.get("isQualisLite");
		requestContext.setUserInfo(userInfo);
		return testMasterService.createInstrumentCategory(lstTestInstrumentCategory, userInfo,isQualisLite);
	}
	
	@PostMapping(value = "/createTestpackage")
	public ResponseEntity<Object> createTestpackage(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();	
		final List<TestPackageTest> lstTestInstrumentCategory = objMapper.convertValue(inputMap.get("testpackage"), new TypeReference<List<TestPackageTest>>() {});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
		final int isQualisLite=(int)inputMap.get("isQualisLite");
		requestContext.setUserInfo(userInfo);
		return testMasterService.createTestpackage(lstTestInstrumentCategory, userInfo,isQualisLite);
	}
		
	@PostMapping(value = "/deleteTestInstrumentCategory")
	public ResponseEntity<Object> deleteInstrumentCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class); 
		final TestInstrumentCategory objTestInstrumentCategory = objMapper.convertValue(inputMap.get("testinstrumentcategory"), TestInstrumentCategory.class);
		//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
		final int isQualisLite=(int)inputMap.get("isQualisLite");
		requestContext.setUserInfo(userInfo);
		return testMasterService.deleteInstrumentCategory(objTestInstrumentCategory, userInfo,isQualisLite);
	}

	@PostMapping(value = "/deletePackage")
	public ResponseEntity<Object> deletePackage(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class); 
		final TestPackageTest objTestInstrumentCategory = objMapper.convertValue(inputMap.get("package"), TestPackageTest.class);
		requestContext.setUserInfo(userInfo);
		return testMasterService.deletePackage(objTestInstrumentCategory, userInfo);
	}

	@PostMapping(value = "/getActiveParameterById")
	public ResponseEntity<Object> getActiveParameterById(@RequestBody Map<String, Object> objmap) throws Exception {
		final int ntestParameterCode = (int) objmap.get("ntestparametercode");
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(objmap.get("userinfo"), UserInfo.class);				
		requestContext.setUserInfo(userInfo);
		return testParameterService.getActiveParameterById(ntestParameterCode, userInfo);
	}

	@PostMapping(value = "/createTestParameter")
	public ResponseEntity<Object> createTestParameter(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final TestParameter objTestParameter = objMapper.convertValue(inputMap.get("testparameter"), TestParameter.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
		final int isQualisLite=(int)inputMap.get("isQualisLite");
		requestContext.setUserInfo(userInfo);
		return testParameterService.createTestParameter(objTestParameter, userInfo,isQualisLite);
	}
	
	@PostMapping(value = "/updateTestParameter")
	public ResponseEntity<Object> updateTestParameter(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final TestParameter objTestParameter = objMapper.convertValue(inputMap.get("testparameter"), TestParameter.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 3, the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
		final int isQualisLite=(int)inputMap.get("isQualisLite");
		requestContext.setUserInfo(userInfo);
		return testParameterService.updateTestParameter(objTestParameter, userInfo,isQualisLite);

	}

	@PostMapping(value = "/deleteTestParameter")
	public ResponseEntity<Object> deleteTestParameter(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final TestParameter objTestParameter = objMapper.convertValue(inputMap.get("testparameter"), TestParameter.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
		final int isQualisLite=(int)inputMap.get("isQualisLite");
		requestContext.setUserInfo(userInfo);
		return testParameterService.deleteTestParameter(objTestParameter, userInfo,isQualisLite);
	}
		
	@PostMapping(value = "/getActivePredefinedParameterById")
	public ResponseEntity<Object> getActivePredefinedParameterById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final int ntestPredefinedCode = (int) inputMap.get("ntestpredefinedcode");
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);				
		requestContext.setUserInfo(userInfo);
		return testParameterService.getActivePredefinedParameterById(ntestPredefinedCode,userInfo);
	}
		
	@PostMapping(value = "/createTestPredefinedParameter")
	public ResponseEntity<Object> createTestPredefinedParameter(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final TestPredefinedParameter objTestPredefinedParameter = objMapper.convertValue(inputMap.get("testpredefinedparameter"), TestPredefinedParameter.class);
		//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
		final int isQualisLite=(int)inputMap.get("isQualisLite");
		requestContext.setUserInfo(userInfo);
		return testParameterService.createTestPredefinedParameter(objTestPredefinedParameter, userInfo,isQualisLite);
	}

	@PostMapping(value = "/updateTestPredefinedParameter")
	public ResponseEntity<Object> updateTestPredefinedParameter(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final TestPredefinedParameter objTestPredefinedParameter = objMapper.convertValue(inputMap.get("testpredefinedparameter"), TestPredefinedParameter.class);
		//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
		final int isQualisLite=(int)inputMap.get("isQualisLite");
		requestContext.setUserInfo(userInfo);
		return testParameterService.updateTestPredefinedParameter(objTestPredefinedParameter, userInfo,isQualisLite);
	}
	
	@PostMapping(value = "/deleteTestPredefinedParameter")
	public ResponseEntity<Object> deleteTestPredefinedParameter(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final TestPredefinedParameter objTestPredefinedParameter = objMapper.convertValue(inputMap.get("testpredefinedparameter"), TestPredefinedParameter.class);
		//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.			
		final int isQualisLite=(int)inputMap.get("isQualisLite");
		requestContext.setUserInfo(userInfo);
		return testParameterService.deleteTestPredefinedParameter(objTestPredefinedParameter, userInfo,isQualisLite);
	}		
		
	@PostMapping(value = "/getParameterSpecificationByCount")
	public ResponseEntity<Object> getParameterSpecificationByCount(@RequestBody Map<String, Object> objmap) throws Exception {
		final int ntestParameterCode = (int) objmap.get("ntestparametercode");
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(objmap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testParameterService.getParameterSpecificationByCount(ntestParameterCode, userInfo);
	}
		
	@PostMapping(value = "/createTestParameterNumeric")
	public ResponseEntity<Object> createTestParameterNumeric(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final TestParameterNumeric objTestParameterNumeric = objMapper.convertValue(inputMap.get("testparameternumeric"), TestParameterNumeric.class);
		requestContext.setUserInfo(userInfo);
		//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
		final int isQualisLite=(int)inputMap.get("isQualisLite");
		return testParameterService.createTestParameterNumeric(objTestParameterNumeric, userInfo,isQualisLite);
	}

	@PostMapping(value = "/updateTestParameterNumeric")
	public ResponseEntity<Object> updateTestParameterNumeric(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final TestParameterNumeric objTestParameterNumeric = objMapper.convertValue(inputMap.get("testparameternumeric"), TestParameterNumeric.class);
		requestContext.setUserInfo(userInfo);
		//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
		final int isQualisLite=(int)inputMap.get("isQualisLite");
		return testParameterService.updateTestParameterNumeric(objTestParameterNumeric, userInfo,isQualisLite);
	}
		
	@PostMapping(value = "/deleteTestParameterNumeric")
	public ResponseEntity<Object> deleteTestParameterNumeric(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final TestParameterNumeric objTestParameterNumeric = objMapper.convertValue(inputMap.get("testparameternumeric"), TestParameterNumeric.class);
		//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
		final int isQualisLite=(int)inputMap.get("isQualisLite");
		requestContext.setUserInfo(userInfo);
		return testParameterService.deleteTestParameterNumeric(objTestParameterNumeric, userInfo,isQualisLite);
	}
			
	@PostMapping(value = "/getParameterSpecificationById")
	public ResponseEntity<Object> getParameterSpecificationById(@RequestBody Map<String, Object> objmap) throws Exception {
		final int ntestParamNumericCode = (int) objmap.get("ntestparamnumericcode");
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(objmap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testParameterService.getParameterSpecificationById(ntestParamNumericCode, userInfo);
	}

	@PostMapping(value = "/addTestFormula")
	public ResponseEntity<Object> addTestFormula(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testParameterService.addTestFormula(userInfo);
	}

	@PostMapping(value = "/changeTestCatgoryInFormula")
	public ResponseEntity<Object> changeTestCatgoryInFormula(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int ntestCategoryCode = (int) inputMap.get("ntestcategorycode");
		requestContext.setUserInfo(userInfo);
		return testParameterService.changeTestCatgoryInFormula(ntestCategoryCode,userInfo);
	}
		
	@PostMapping(value = "/changeTestInFormula")
	public ResponseEntity<Object> changeTestInFormula(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int ntestCode = (int) inputMap.get("ntestcode");
		requestContext.setUserInfo(userInfo);
		return testParameterService.changeTestInFormula(ntestCode,userInfo);
	}
		
	@PostMapping(value = "/createTestFormula")
	public ResponseEntity<Object> createTestFormula(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final TestFormula objTestFormula = objMapper.convertValue(inputMap.get("testformula"),new TypeReference<TestFormula>() {});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
		final int isQualisLite=(int)inputMap.get("isQualisLite");
		requestContext.setUserInfo(userInfo);
		return testParameterService.createTestFormula(objTestFormula, userInfo,isQualisLite);
	}
		
	@PostMapping(value = "/deleteTestFormula")
	public ResponseEntity<Object> updateTestFormula(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final TestFormula objTestFormula = objMapper.convertValue(inputMap.get("testformula"),new TypeReference<TestFormula>() {});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
		final int isQualisLite=(int)inputMap.get("isQualisLite");
		requestContext.setUserInfo(userInfo);
		return testParameterService.deleteTestFormula(objTestFormula, userInfo,isQualisLite);
	}
		
	@PostMapping(value = "/validateTestFormula")
	public ResponseEntity<Object> validateTestFormula(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final String sformula =(String) inputMap.get("sformula");
		final int ntestcode =(int) inputMap.get("ntestcode");
		requestContext.setUserInfo(userInfo);
		return testParameterService.validateTestFormula(sformula, ntestcode);
	}

	@PostMapping(value = "/calculateFormula")
	public ResponseEntity<Object> calculateFormula(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper mapper = new ObjectMapper();
		final UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final String sformulacalculationcode = (String) inputMap.get("sformulacalculationcode");
		final List<DynamicField> lstDynamicField = mapper.convertValue(inputMap.get("dynamicformulafields"), new TypeReference<List<DynamicField>>() {});
		requestContext.setUserInfo(userInfo);
		return testParameterService.calculateFormula(sformulacalculationcode, lstDynamicField, inputMap);
	}
		
	@PostMapping(value = "/copyTestMaster")
	public ResponseEntity<Object> copyTestMaster(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper=new ObjectMapper();
		final TestMaster objTestMaster = objMapper.convertValue(inputMap.get("testmaster"), TestMaster.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
		final int isQualisLite=(int)inputMap.get("isQualisLite");
		requestContext.setUserInfo(userInfo);
		return testMasterService.copyTestMaster(objTestMaster, userInfo,isQualisLite);
	}
		
	@PostMapping(value = "/setDefaultTestPredefinedParameter")
	public ResponseEntity<Object> setDefaultTestPredefinedParameter(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper=new ObjectMapper();
		final TestPredefinedParameter objTestPredefinedParameter = objMapper.convertValue(inputMap.get("testpredefinedparameter"), TestPredefinedParameter.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
		final int isQualisLite=(int)inputMap.get("isQualisLite");
		requestContext.setUserInfo(userInfo);
		return testParameterService.setDefaultTestPredefinedParameter(objTestPredefinedParameter, userInfo,isQualisLite);
	}
		
	@PostMapping(value = "/setDefaultTestSection")
	public ResponseEntity<Object> setDefaultTestSection(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper=new ObjectMapper();
		final TestSection objTestSection = objMapper.convertValue(inputMap.get("testsection"), TestSection.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
		final int isQualisLite=(int)inputMap.get("isQualisLite");
		requestContext.setUserInfo(userInfo);
		return testMasterService.setDefaultTestSection(objTestSection, userInfo,isQualisLite);
	}
		
	@PostMapping(value = "/setDefaultTestMethod")
	public ResponseEntity<Object> setDefaultTestMethod(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper=new ObjectMapper();
		final TestMethod objTestMethod = objMapper.convertValue(inputMap.get("testmethod"), TestMethod.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
		final int isQualisLite=(int)inputMap.get("isQualisLite");
		requestContext.setUserInfo(userInfo);
		return testMasterService.setDefaultTestMethod(objTestMethod, userInfo,isQualisLite);
	}		
		
	@PostMapping(value = "/setDefaultTestInstrumentCategory")
	public ResponseEntity<Object> setDefaultTestInstrumentCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper=new ObjectMapper();
		final TestInstrumentCategory objTestInstrumentCategory = objMapper.convertValue(inputMap.get("testinstrumentcategory"), TestInstrumentCategory.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
		final int isQualisLite=(int)inputMap.get("isQualisLite");
		requestContext.setUserInfo(userInfo);
		return testMasterService.setDefaultTestInstrumentCategory(objTestInstrumentCategory, userInfo,isQualisLite);
	}
	
	//ALPD-3510
	@PostMapping(value = "/setDefaultPackage")
	public ResponseEntity<Object> setDefaultPackage(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper=new ObjectMapper();
		final TestPackageTest objTestInstrumentCategory = objMapper.convertValue(inputMap.get("package"), TestPackageTest.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
     	//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
		final int isQualisLite=(int)inputMap.get("isQualisLite");
		requestContext.setUserInfo(userInfo);
		return testMasterService.setDefaultPackage(objTestInstrumentCategory, userInfo,isQualisLite);
	}
		
	@PostMapping(value = "/setDefaultTestFormula")
	public ResponseEntity<Object> setDefaultTestFormula(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper=new ObjectMapper();
		final TestFormula objTestFormula = objMapper.convertValue(inputMap.get("testformula"), TestFormula.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
		final int isQualisLite=(int)inputMap.get("isQualisLite");
		requestContext.setUserInfo(userInfo);
		return testParameterService.setDefaultTestFormula(objTestFormula, userInfo,isQualisLite);
	}
		
	@PostMapping(value = "/createTestFile")
	public ResponseEntity<Object> createTestFile(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testMasterService.createTestFile(request, userInfo);
	}
		
	@PostMapping(value = "/updateTestFile")
	public ResponseEntity<Object> updateTestFile(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testMasterService.updateTestFile(request, userInfo);
	}
		
	@PostMapping(value = "/deleteTestFile")
	public ResponseEntity<Object> deleteTestFile(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final TestFile objTestFile = objMapper.convertValue(inputMap.get("testfile"), TestFile.class);
		//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
		final int isQualisLite=(int)inputMap.get("isQualisLite");
		requestContext.setUserInfo(userInfo);
		return testMasterService.deleteTestFile(objTestFile, userInfo,isQualisLite);
	}
		
	@PostMapping(value = "/editTestFile")
	public ResponseEntity<Object> editTestFile(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final TestFile objTestFile = objMapper.convertValue(inputMap.get("testfile"), TestFile.class);
		requestContext.setUserInfo(userInfo);
		return testMasterService.editTestFile(objTestFile, userInfo);
	}
		
	@PostMapping(value = "/viewAttachedTestFile")
	public ResponseEntity<Object> viewAttachedTestFile(@RequestBody Map<String, Object> inputMap, HttpServletResponse response) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final TestFile objTestFile = objMapper.convertValue(inputMap.get("testfile"), TestFile.class);
		Map<String, Object> outputMap = testMasterService.viewAttachedTestFile(objTestFile, userInfo);				
		requestContext.setUserInfo(userInfo);
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}
		
	@PostMapping(value = "/setDefaultTestFile")
	public ResponseEntity<Object> setDefaultTestFile(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper=new ObjectMapper();
		final TestFile objTestFile = objMapper.convertValue(inputMap.get("testfile"), TestFile.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
		final int isQualisLite=(int)inputMap.get("isQualisLite");
		requestContext.setUserInfo(userInfo);
		return testMasterService.setDefaultTestFile(objTestFile, userInfo,isQualisLite);
	}
		
	@PostMapping(value = "/getSection")
	public ResponseEntity<Object> getSection(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper=new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int ntestcode = (int) inputMap.get("ntestcode");
		requestContext.setUserInfo(userInfo);
		return testMasterService.getSection(ntestcode);
	}

	@PostMapping(value = "/getMethod")
	public ResponseEntity<Object> getMethod(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper=new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int ntestcode = (int) inputMap.get("ntestcode");
		requestContext.setUserInfo(userInfo);
		return testMasterService.getMethod(ntestcode);
	}
		
	@PostMapping(value = "/getInstrumentCategory")
	public ResponseEntity<Object> getInstrumentCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper=new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int ntestcode = (int) inputMap.get("ntestcode");
		requestContext.setUserInfo(userInfo);
		return testMasterService.getInstrumentCategory(ntestcode);
	}

	@PostMapping(value = "/getTestAttachment")
	public ResponseEntity<Object> getTestAttachment(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper=new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int ntestcode = (int) inputMap.get("ntestcode");
		requestContext.setUserInfo(userInfo);
		return testMasterService.getTestAttachment(ntestcode);
	}
		
	@PostMapping(value = "/validateCopyTest")
	public ResponseEntity<Object> validateCopyTest(@RequestBody Map<String, Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int ntestCode = (int) inputMap.get("ntestcode");
		requestContext.setUserInfo(userInfo);
		return testMasterService.validateCopyTest(userInfo, ntestCode);
	}
		
	@PostMapping(value = "/createTestContainerType")
	public ResponseEntity<Object> createTestContainerType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();	
		final TestContainerType objcreateTestContainerType = objMapper.convertValue(inputMap.get("testcontainertype"), new TypeReference<TestContainerType>() {});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
		final int isQualisLite=(int) inputMap.get("isQualisLite");
		requestContext.setUserInfo(userInfo);
		return testMasterService.createTestContainerType(objcreateTestContainerType, userInfo,isQualisLite);
	}
		
	//ALPD-3510
	@PostMapping(value = "/setDefaultTestContainerType")
	public ResponseEntity<Object> setDefaultTestContainerType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper=new ObjectMapper();
		final TestContainerType objTestContainerType = objMapper.convertValue(inputMap.get("testcontainertype"), TestContainerType.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
		final int isQualisLite=(int)inputMap.get("isQualisLite");
		requestContext.setUserInfo(userInfo);
		return testMasterService.setDefaultTestContainerType(objTestContainerType, userInfo,isQualisLite);
	}
		
	@PostMapping(value = "/deleteTestContainerType")
	public ResponseEntity<Object> deleteTestContainerType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class); 
		final TestContainerType objTestContainerType = objMapper.convertValue(inputMap.get("testcontainertype"), TestContainerType.class);
		//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
		final int isQualisLite=(int)inputMap.get("isQualisLite");
		requestContext.setUserInfo(userInfo);
			return testMasterService.deleteTestContainerType(objTestContainerType, userInfo,isQualisLite);
	}
		
//	@PostMapping(value = "/getTestContainterType")
//	public ResponseEntity<Object> getTestContainterType(@RequestBody Map<String, Object> inputMap) throws Exception {
//		final ObjectMapper objMapper = new ObjectMapper();
//		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);  
//		final int ntestcode = (int)inputMap.get("ntestcode");
//		requestContext.setUserInfo(userInfo);
//		return testMasterService.getTestContainterType(ntestcode, userInfo);
//	}
	
	@PostMapping(value = "/getAvailableContainerType")
	public ResponseEntity<Object> getAvailableContainerType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class); 
		final TestMaster objTestMaster = objMapper.convertValue(inputMap.get("TestMaster"), TestMaster.class);
		requestContext.setUserInfo(userInfo);
		return testMasterService.getAvailableContainerType(objTestMaster, userInfo);
	}
	
	@PostMapping(value = "/getActiveTestContainerTypeById")
	public ResponseEntity<Object> getActiveTestContainerTypeById(@RequestBody Map<String, Object> inputMap)throws Exception  {
		final ObjectMapper objmapper = new ObjectMapper();
		final int ntestcontainertypecode = (Integer) inputMap.get("ntestcontainertypecode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testMasterService.getActiveTestContainerTypeById(ntestcontainertypecode, userInfo);
	}
	
	@PostMapping(value = "/updateTestContainerType")
	public ResponseEntity<Object> updateTestContainerType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
		final int isQualislite=(int)inputMap.get("isQualisLite");
		requestContext.setUserInfo(userInfo);
		return testMasterService.updateTestContainerType(inputMap,userInfo,isQualislite);
	}

	@PostMapping(value = "/getInterfaceType")
	public ResponseEntity<Object> getInterfaceType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return testMasterService.getInterfaceType(userInfo);
	}
		
	@PostMapping(value = "/getContainerType")
	public ResponseEntity<Object> getContainerType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper=new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int ntestcode = (int) inputMap.get("ntestcode");
		requestContext.setUserInfo(userInfo);
		return testMasterService.getContainerType(ntestcode);
	}
		
	@PostMapping(value = "/getEditReportInfoTest")
	public ResponseEntity<Object> getEditProjectReportInfo(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testMasterService.getEditReportInfoTest(inputMap,userInfo);
	}
		
	@PostMapping(value = "/updateReportInfoTest")
	public ResponseEntity<Object> updateProjectReportInfo(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final ReportInfoTest objReportInfoTest = objMapper.convertValue(inputMap.get("reportinfotest"),ReportInfoTest.class);		
		requestContext.setUserInfo(userInfo);
		return testMasterService.updateReportInfoTest(objReportInfoTest, userInfo);
	}
		
	@PostMapping(value = "/getTestPackage")
	public ResponseEntity<Object> getTestPackage(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper=new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int ntestcode = (int) inputMap.get("ntestcode");
		requestContext.setUserInfo(userInfo);
		return testMasterService.getTestPackage(ntestcode);
	}
		
	@PostMapping(value = "/getDestinationUnit")
	public ResponseEntity<Object> getDestinationUnit(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper=new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);	
		final int nunitcode = (int) inputMap.get("nunitcode");
		requestContext.setUserInfo(userInfo);
		return testMasterService.getDestinationUnit(nunitcode,userInfo);
	}
		
	@PostMapping(value = "/getConversionOperator")
	public ResponseEntity<Object> getConversionOperator(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper=new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);	
		final int ndestinationunitcode = (int) inputMap.get("ndestinationunitcode");
		final int nsourceunitcode = (int) inputMap.get("nsourceunitcode");
		requestContext.setUserInfo(userInfo);
		return testMasterService.getConversionOperator(nsourceunitcode,ndestinationunitcode,userInfo);
	}
		
	@PostMapping(value = "/createTestParameterClinicalSpec")
	public ResponseEntity<Object> createTestParameterClinicalSpec(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final TestMasterClinicalSpec objTestParameterNumeric = objMapper.convertValue(inputMap.get("TestGroupAddSpecification"), TestMasterClinicalSpec.class);
		//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
		final int isQualisLite=(int)inputMap.get("isQualisLite");
		requestContext.setUserInfo(userInfo);
		return testParameterService.createTestParameterClinicalSpec(objTestParameterNumeric, userInfo,isQualisLite);
	}
		
	@PostMapping(value = "/updateTestParameterClinicalSpec")
	public ResponseEntity<Object> updateTestParameterClinicalSpec(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final TestMasterClinicalSpec objTestParameterNumeric = objMapper.convertValue(inputMap.get("TestGroupAddSpecification"), TestMasterClinicalSpec.class);
		//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
		final int isQualisLite=(int)inputMap.get("isQualisLite");
		requestContext.setUserInfo(userInfo);
		return testParameterService.updateTestParameterClinicalSpec(objTestParameterNumeric, userInfo,isQualisLite);
	}	
		
	@PostMapping(value = "/deleteTestParameterClinicalSpec")
	public ResponseEntity<Object> deleteTestParameterClinicalSpec(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final TestMasterClinicalSpec objTestParameterNumeric = objMapper.convertValue(inputMap.get("testparameterclinicalspec"), TestMasterClinicalSpec.class);
		//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
		final int isQualisLite=(int)inputMap.get("isQualisLite");
		requestContext.setUserInfo(userInfo);
		return testParameterService.deleteTestParameterClinicalSpec(objTestParameterNumeric, userInfo,isQualisLite);
	}
		
	@PostMapping(value = "/getTestParameterClinicalSpecById")
	public ResponseEntity<Object> getTestParameterClinicalSpecById(@RequestBody Map<String, Object> objmap) throws Exception {
		final int ntestmasterclinicspeccode = (int) objmap.get("ntestmasterclinicspeccode");
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(objmap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testParameterService.getTestParameterClinicalSpecById(ntestmasterclinicspeccode, userInfo);
	}
		
	@PostMapping(value = "/getPreDefinedFormula")
	public ResponseEntity<Object> getPreDefinedFormula(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testParameterService.getPreDefinedFormula(userInfo);
	}
		
	@PostMapping(value = "/getResultAccuracy")
	public ResponseEntity<Object> getResultAccuracy(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);				
		requestContext.setUserInfo(userInfo);
		return testParameterService.getResultAccuracy(userInfo);
	}
		
		
}
