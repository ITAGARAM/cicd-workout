package com.agaramtech.qualis.restcontroller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.configuration.model.SampleType;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecFile;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecSampleType;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecification;
import com.agaramtech.qualis.testgroup.model.TestGroupTest;
import com.agaramtech.qualis.testgroup.model.TestGroupTestCharParameter;
import com.agaramtech.qualis.testgroup.model.TestGroupTestClinicalSpec;
import com.agaramtech.qualis.testgroup.model.TestGroupTestFile;
import com.agaramtech.qualis.testgroup.model.TestGroupTestFormula;
import com.agaramtech.qualis.testgroup.model.TestGroupTestNumericParameter;
import com.agaramtech.qualis.testgroup.model.TestGroupTestParameter;
import com.agaramtech.qualis.testgroup.model.TestGroupTestPredefinedParameter;
import com.agaramtech.qualis.testgroup.model.TestGroupTestPredefinedSubCode;
import com.agaramtech.qualis.testgroup.model.TreeTemplateManipulation;
import com.agaramtech.qualis.testgroup.service.testgroup.TestGroupService;
import com.agaramtech.qualis.testgroup.service.testgroupspecfile.TestGroupSpecFileService;
import com.agaramtech.qualis.testgroup.service.testgroupspecification.TestGroupSpecificationService;
import com.agaramtech.qualis.testgroup.service.testgrouptest.TestGroupTestService;
import com.agaramtech.qualis.testgroup.service.testgrouptestmaterial.TestGroupTestMaterialService;
import com.agaramtech.qualis.testgroup.service.testgrouptestparameter.TestGroupParameterService;
import com.agaramtech.qualis.testmanagement.model.TestCategory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

/**
 * This controller is used to access the Testgroup, TestGroupTest and TestGroupParameter service methods.
 */
@RestController
@RequestMapping("/testgroup")
@AllArgsConstructor
public class TestGroupController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TestGroupController.class);

	private RequestContext requestContext;
	private final TestGroupService objTestGroupService;
	private final TestGroupTestService objTestGroupTestService;
	private final TestGroupParameterService objTestGroupParameterService;
	private final TestGroupSpecFileService objTestGroupSpecFileService;
	private final TestGroupSpecificationService objTestGroupSpecificationService;
	private final TestGroupTestMaterialService objTestGroupTestMaterialService;
	
	
	
	/**
	 * This method is used to fetch a list of all active sample type, sample
	 * category, sample, profile tree, specification(s), specification file(s),
	 * specification history, component(s), test(s), parameter(s) and parameter
	 * details
	 * 
	 * @param inputMap [Map] map holds the object of loggedin user
	 * @return response object with the list of active sample type for the specified
	 *         site
	 */
	@PostMapping(value = "/getTestGroup")
	public ResponseEntity<Object> getTestGroup(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});

		requestContext.setUserInfo(userInfo);
		return objTestGroupService.getTestGroup(userInfo);
	}

	/**
	 * This method is used to fetch a list of all active sample type, sample
	 * category, sample, profile tree, specification(s), specification file(s),
	 * specification history, component(s), test(s), parameter(s) and parameter
	 * details
	 * 
	 * @param inputMap [Map] map holds the object(s) of loggedin user info and
	 *                 sample type
	 * @return response object with the list of active sample category
	 */
	@PostMapping(value = "/getProductCategory")
	public ResponseEntity<Object> getProductCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		final SampleType objSampleType = objMapper.convertValue(inputMap.get("sampletype"),new TypeReference<SampleType>() {});

		requestContext.setUserInfo(userInfo);
		return objTestGroupService.getProductCategory(userInfo, objSampleType);
	}

	@SuppressWarnings("unchecked")
	@PostMapping(value = "/getProduct")
	public ResponseEntity<Object> getProduct(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final Map<String, Object> categoryMap = objMapper.convertValue(inputMap.get("productcategory"), Map.class);
		final SampleType objSampleType = objMapper.convertValue(inputMap.get("sampletype"), new TypeReference<SampleType>() {});

		requestContext.setUserInfo(userInfo);
		return objTestGroupService.getProduct(userInfo, categoryMap, objSampleType);
	}

	@SuppressWarnings("unchecked")
	@PostMapping(value = "/getTreeVersionTemplate")
	public ResponseEntity<Object> getTreeVersionTemplate(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final SampleType objSampleType = objMapper.convertValue(inputMap.get("sampletype"),new TypeReference<SampleType>() {});
		final Map<String, Object> categoryMap = objMapper.convertValue(inputMap.get("productcategory"), Map.class);
		final Map<String, Object> productMap = objMapper.convertValue(inputMap.get("product"), Map.class);
		final Map<String, Object> projectMap = objMapper.convertValue(inputMap.get("project"), Map.class);

		requestContext.setUserInfo(userInfo);
		return objTestGroupService.getTreeVersionTemplate(userInfo, objSampleType, categoryMap, productMap, projectMap);
	}

	@PostMapping(value = "/getTestGroupSpecification")
	public ResponseEntity<Object> getTestGroupSpecification(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final TreeTemplateManipulation objTree = objMapper.convertValue(inputMap.get("treetemplatemanipulation"), new TypeReference<TreeTemplateManipulation>() {});

		requestContext.setUserInfo(userInfo);
		return objTestGroupService.getTestGroupSpecification(userInfo, objTree);
	}

	@PostMapping(value = "/getAddSpecification")
	public ResponseEntity<Object> getAddSpecification(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final int ntreeversiontempcode = (Integer) inputMap.get("ntreeversiontempcode");
		final int  nprojectmastercode= (Integer) inputMap.get("nprojectmastercode");

		requestContext.setUserInfo(userInfo);
		return objTestGroupService.getAddSpecification(userInfo,ntreeversiontempcode,nprojectmastercode);
	}

	@PostMapping(value = "/getActiveSpecificationById")
	public ResponseEntity<Object> getActiveSpecificationById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final TestGroupSpecification objTestGroupSpec = objMapper.convertValue(inputMap.get("testgroupspecification"),new TypeReference<TestGroupSpecification>() {});
		final int ntreeversiontempcode = (Integer) inputMap.get("ntreeversiontempcode");

		requestContext.setUserInfo(userInfo);
		return objTestGroupService.getActiveSpecificationById(userInfo, objTestGroupSpec,ntreeversiontempcode);
	}

	@PostMapping(value = "/createSpecification")
	public ResponseEntity<Object> createSpecification(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final TestGroupSpecification objTestGroupSpec = objMapper.convertValue(inputMap.get("testgroupspecification"),new TypeReference<TestGroupSpecification>() {});
		final TreeTemplateManipulation objTree = objMapper.convertValue(inputMap.get("treetemplatemanipulation"),new TypeReference<TreeTemplateManipulation>() {});

		requestContext.setUserInfo(userInfo);
		return objTestGroupService.createSpecification(userInfo, objTestGroupSpec, objTree);
	}

	@PostMapping(value = "/updateSpecification")
	public ResponseEntity<Object> updateSpecification(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final TestGroupSpecification objTestGroupSpec = objMapper.convertValue(inputMap.get("testgroupspecification"),new TypeReference<TestGroupSpecification>() {});
		final TreeTemplateManipulation objTree = objMapper.convertValue(inputMap.get("treetemplatemanipulation"),new TypeReference<TreeTemplateManipulation>() {});

		requestContext.setUserInfo(userInfo);
		return objTestGroupService.updateSpecification(userInfo, objTestGroupSpec, objTree);
	}

	@PostMapping(value = "/deleteSpecification")
	public ResponseEntity<Object> deleteSpecification(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final TestGroupSpecification objTestGroupSpec = objMapper.convertValue(inputMap.get("testgroupspecification"),new TypeReference<TestGroupSpecification>() {});
		final TreeTemplateManipulation objTree = objMapper.convertValue(inputMap.get("treetemplatemanipulation"),new TypeReference<TreeTemplateManipulation>() {});
		final int ntreeversiontempcode = (Integer) inputMap.get("ntreeversiontempcode");
		requestContext.setUserInfo(userInfo);
		return objTestGroupService.deleteSpecification(inputMap,userInfo, objTestGroupSpec,objTree, ntreeversiontempcode);
	}

	// (isQualisLite) added by Gowtham - ALPD-5329 - In test group specification record getting Auto Approved when configuration for test group approval not done
	@PostMapping(value = "/completeSpecification")
	public ResponseEntity<Object> completeSpecification(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final TestGroupSpecification objTestGroupSpec = objMapper.convertValue(inputMap.get("testgroupspecification"),new TypeReference<TestGroupSpecification>() {});
		final TreeTemplateManipulation objTree = objMapper.convertValue(inputMap.get("treetemplatemanipulation"),new TypeReference<TreeTemplateManipulation>() {});
		//ALPD-5001--Added by vignesh(04-03-2025)--Test group screen -> System allow to save the spec without having the test in Multi-tab.
		final List<TestGroupTest> listTestGroupTest = objMapper.convertValue(inputMap.get("testGroupTest"),new TypeReference<List<TestGroupTest>>() {});
		final int ntreeversiontempcode = (Integer) inputMap.get("ntreeversiontempcode");
		requestContext.setUserInfo(userInfo);

		final int isQualisLite = (Integer) inputMap.get("isQualisLite");
		return objTestGroupService.completeSpecification(userInfo, objTestGroupSpec,objTree, ntreeversiontempcode, isQualisLite,listTestGroupTest);
	}

	@PostMapping(value = "/approveSpecification")
	public ResponseEntity<Object> approveSpecification(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final TestGroupSpecification objTestGroupSpec = objMapper.convertValue(inputMap.get("testgroupspecification"),new TypeReference<TestGroupSpecification>() {});
		final TreeTemplateManipulation objTree = objMapper.convertValue(inputMap.get("treetemplatemanipulation"),new TypeReference<TreeTemplateManipulation>() {});
		final int ntreeversiontempcode = (Integer) inputMap.get("ntreeversiontempcode");
		requestContext.setUserInfo(userInfo);
		return objTestGroupService.approveSpecification(userInfo, objTestGroupSpec,objTree, ntreeversiontempcode);
	}

	@PostMapping(value = "/createComponent")
	public ResponseEntity<Object> createComponent(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final TestGroupSpecification objTestGroupSpec = objMapper.convertValue(inputMap.get("testgroupspecification"),new TypeReference<TestGroupSpecification>() {});
		final List<TestGroupSpecSampleType> listTestGroupComponent = objMapper.convertValue(inputMap.get("testgroupspecsampletype"), new TypeReference<List<TestGroupSpecSampleType>>() {
				});
		final List<TestGroupTest> listTestGroupTest = objMapper.convertValue(inputMap.get("testgrouptest"),	new TypeReference<List<TestGroupTest>>() {});

		requestContext.setUserInfo(userInfo);
		return objTestGroupTestService.createComponent(inputMap,userInfo, objTestGroupSpec, listTestGroupComponent,
				listTestGroupTest);
	}

	@PostMapping(value = "/deleteTestGroupComponent")
	public ResponseEntity<Object> deleteTestGroupComponent(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final TestGroupSpecification objTestGroupSpec = objMapper.convertValue(inputMap.get("testgroupspecification"),new TypeReference<TestGroupSpecification>() {});
		final List<TestGroupSpecSampleType> listTestGroupComponent = objMapper.convertValue(inputMap.get("testgroupspecsampletype"), new TypeReference<List<TestGroupSpecSampleType>>() {
				});

		requestContext.setUserInfo(userInfo);
		return objTestGroupTestService.deleteTestGroupComponent(inputMap,userInfo, objTestGroupSpec, listTestGroupComponent);
	}

	@PostMapping(value = "/getAvailableComponent")
	public ResponseEntity<Object> getAvailableComponent(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final TestGroupSpecification objTestGroupSpec = objMapper.convertValue(inputMap.get("testgroupspecification"),new TypeReference<TestGroupSpecification>() {});

		requestContext.setUserInfo(userInfo);
		return objTestGroupTestService.getAvailableComponent(userInfo, objTestGroupSpec);
	}

	@PostMapping(value = "/getAvailableTest")
	public ResponseEntity<Object> getAvailableTest(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final TestGroupSpecSampleType objTestGroupComponent = objMapper.convertValue(inputMap.get("testgroupspecsampletype"),new TypeReference<TestGroupSpecSampleType>() {});
		final int ntreeversiontempcode = (Integer) inputMap.get("ntreeversiontempcode");
		final int nprojectmastercode = (Integer) inputMap.get("nprojectmastercode");
		final int nclinicaltyperequired=(Integer) inputMap.get("nclinicaltyperequired");

		requestContext.setUserInfo(userInfo);
		return objTestGroupTestService.getAvailableTest(userInfo, objTestGroupComponent,ntreeversiontempcode,nprojectmastercode,nclinicaltyperequired);
	}

	@PostMapping(value = "/createTest")
	public ResponseEntity<Object> createTest(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final TestGroupSpecification objSpecification = objMapper.convertValue(inputMap.get("testgroupspecification"),new TypeReference<TestGroupSpecification>() {});
		final List<TestGroupSpecSampleType> lstTestGroupComponent = objMapper.convertValue(inputMap.get("testgroupspecsampletype"), new TypeReference<List<TestGroupSpecSampleType>>() {
				});
		final List<TestGroupTest> listTest = objMapper.convertValue(inputMap.get("testgrouptest"),new TypeReference<List<TestGroupTest>>() {});

		requestContext.setUserInfo(userInfo);
		return objTestGroupTestService.createTest(userInfo, objSpecification, lstTestGroupComponent, listTest);
	}

	@PostMapping(value = "/deleteTest")
	public ResponseEntity<Object> deleteTest(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {
		});
		final TestGroupSpecSampleType objTestGroupComponent = objMapper.convertValue(inputMap.get("testgroupspecsampletype"),new TypeReference<TestGroupSpecSampleType>() {});
		final TestGroupTest objTest = objMapper.convertValue(inputMap.get("testgrouptest"),new TypeReference<TestGroupTest>() {});
		final int ntreeversiontempcode = (Integer) inputMap.get("ntreeversiontempcode");
		final int nprojectmastercode = (Integer) inputMap.get("nprojectmastercode");

		requestContext.setUserInfo(userInfo);
		return objTestGroupTestService.deleteTest(userInfo, objTestGroupComponent, objTest,ntreeversiontempcode,nprojectmastercode);
	}

	@PostMapping(value = "/getActiveTestById")
	public ResponseEntity<Object> getActiveTestById(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final TestGroupTest objTest = objMapper.convertValue(inputMap.get("testgrouptest"),new TypeReference<TestGroupTest>() {});
		final int ntreeversiontempcode = (Integer) inputMap.get("ntreeversiontempcode");
		final int nprojectmastercode = (Integer) inputMap.get("nprojectmastercode");

		requestContext.setUserInfo(userInfo);
		return objTestGroupTestService.getActiveTestById(userInfo, objTest,ntreeversiontempcode,nprojectmastercode);
	}

	@PostMapping(value = "/updateTest")
	public ResponseEntity<Object> updateTest(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final TestGroupSpecification objTestGroupSpec = objMapper.convertValue(inputMap.get("testgroupspecification"),new TypeReference<TestGroupSpecification>() {});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final TestGroupTest objTest = objMapper.convertValue(inputMap.get("testgrouptest"),new TypeReference<TestGroupTest>() {});
		final TestGroupTestFile objTestFile = objMapper.convertValue(inputMap.get("testgrouptestfile"),new TypeReference<TestGroupTestFile>() {});

		requestContext.setUserInfo(userInfo);
		return objTestGroupTestService.updateTest(userInfo, objTest, objTestGroupSpec, objTestFile);
	}

	@PostMapping(value = "/updateParameter")
	public ResponseEntity<Object> updateParameter(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final TestGroupSpecification objTestGroupSpec = objMapper.convertValue(inputMap.get("testgroupspecification"),new TypeReference<TestGroupSpecification>() {});
		final TestGroupTestParameter objTestParameter = objMapper.convertValue(inputMap.get("testgrouptestparameter"),new TypeReference<TestGroupTestParameter>() {});

		requestContext.setUserInfo(userInfo);
		return objTestGroupParameterService.updateParameter(userInfo, objTestParameter, objTestGroupSpec);
	}

	@PostMapping(value = "/getParameterByTestId")
	public ResponseEntity<Object> getParameterByTestId(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final TestGroupTest objTest = objMapper.convertValue(inputMap.get("testgrouptest"),new TypeReference<TestGroupTest>() {});

		requestContext.setUserInfo(userInfo);
		return objTestGroupParameterService.getParameterByTestId(userInfo, objTest);
	}

	@PostMapping(value = "/getTestByComponentId")
	public ResponseEntity<Object> getTestByComponentId(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final TestGroupSpecSampleType objTestGroupSpecSampleType = objMapper.convertValue(inputMap.get("testgroupspecsampletype"),new TypeReference<TestGroupSpecSampleType>() {});

		requestContext.setUserInfo(userInfo);
		return objTestGroupTestService.getTestByComponentId(userInfo, objTestGroupSpecSampleType);
	}

	@PostMapping(value = "/getTemplateMasterDetails")
	public ResponseEntity<Object> getTemplateMasterDetails(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final SampleType objSampleType = objMapper.convertValue(inputMap.get("sampletype"),new TypeReference<SampleType>() {});
		final int ncategorycode = (int) inputMap.get("ncategorycode");
		final int ntreeversiontempcode = (int) inputMap.get("ntreeversiontempcode");
		final int nprojectmastercode=(int) inputMap.get("nprojectmastercode");
		final TreeTemplateManipulation objTreeTemplateManipulation = objMapper.convertValue(inputMap.get("treetemplatemanipulation"),new TypeReference<TreeTemplateManipulation>() {
				});

		requestContext.setUserInfo(userInfo);
		return objTestGroupService.getTemplateMasterDetails(userInfo, ncategorycode, objSampleType,
				ntreeversiontempcode, objTreeTemplateManipulation,nprojectmastercode);
	}
	
	@PostMapping(value = "/createTree")
	public ResponseEntity<Object> createTree(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final SampleType objSampleType = objMapper.convertValue(inputMap.get("sampletype"),new TypeReference<SampleType>() {});
		final int ncategorycode = (int) inputMap.get("ncategorycode");
		final int nproductcode = (int) inputMap.get("nproductcode");
		final int ntreeversiontempcode = (int) inputMap.get("ntreeversiontempcode");
		final int nprojectMasterCode = (int) inputMap.get("nprojectmastercode");
		final List<TreeTemplateManipulation> listTree = objMapper.convertValue(inputMap.get("treetemplatemanipulation"),new TypeReference<List<TreeTemplateManipulation>>() {
				});

		requestContext.setUserInfo(userInfo);
		return objTestGroupService.createTree(userInfo, objSampleType, ncategorycode, nproductcode,
				ntreeversiontempcode, nprojectMasterCode, listTree);
	}

	@PostMapping(value = "/getTestGroupComponent")
	public ResponseEntity<Object> getTestGroupComponent(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final TestGroupSpecification objTestGroupSpec = objMapper.convertValue(inputMap.get("testgroupspecification"),new TypeReference<TestGroupSpecification>() {});

		requestContext.setUserInfo(userInfo);
		return objTestGroupTestService.getTestGroupComponent(userInfo, objTestGroupSpec);
	}

	@PostMapping(value = "/getTestGroupTest")
	public ResponseEntity<Object> getTestGroupTest(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final int ntestgrouptestcode = (int) inputMap.get("ntestgrouptestcode");

		requestContext.setUserInfo(userInfo);
		return objTestGroupTestService.getTestGroupTest(userInfo, ntestgrouptestcode);
	}

	@PostMapping(value = "/getTestMasterByCategory")
	public ResponseEntity<Object> getTestMasterByCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final TestCategory objTestCategory = objMapper.convertValue(inputMap.get("testcategory"),new TypeReference<TestCategory>() {});
		final TestGroupSpecSampleType objComponent = objMapper.convertValue(inputMap.get("testgroupspecsampletype"),new TypeReference<TestGroupSpecSampleType>() {});
		final int nallottedspeccode = (int) inputMap.get("nallottedspeccode");

		requestContext.setUserInfo(userInfo);
		return objTestGroupTestService.getTestMasterByCategory(userInfo, objTestCategory, objComponent,
				nallottedspeccode);
	}

	@PostMapping(value = "/getTestGroupTestParameter")
	public ResponseEntity<Object> getTestGroupTestParameter(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {
		});
		final TestGroupTestParameter objParameter = objMapper.convertValue(inputMap.get("testgrouptestparameter"),new TypeReference<TestGroupTestParameter>() {});

		requestContext.setUserInfo(userInfo);
		return objTestGroupTestService.getTestGroupTestParameter(userInfo, objParameter);
	}

	@PostMapping(value = "/getTestGroupFormula")
	public ResponseEntity<Object> getTestGroupFormula(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final TestGroupTestParameter objparameter = objMapper.convertValue(inputMap.get("testgrouptestparameter"),new TypeReference<TestGroupTestParameter>() {});
		final TestGroupSpecification objSpecification = objMapper.convertValue(inputMap.get("testgroupspecification"),new TypeReference<TestGroupSpecification>() {});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {
		});

		requestContext.setUserInfo(userInfo);
		return objTestGroupTestService.getTestGroupFormula(objparameter, userInfo, objSpecification);
	}

	@PostMapping(value = "/createTestGroupTestFormula")
	public ResponseEntity<Object> createTestGroupFormula(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final TestGroupSpecification objSpecification = objMapper.convertValue(inputMap.get("testgroupspecification"),new TypeReference<TestGroupSpecification>() {});
		final List<TestGroupTestFormula> listFormula = objMapper.convertValue(inputMap.get("testgrouptestformula"),new TypeReference<List<TestGroupTestFormula>>() {});

		requestContext.setUserInfo(userInfo);
		return objTestGroupTestService.createTestGroupFormula(userInfo, listFormula, objSpecification);
	}

	@PostMapping(value = "/deleteTestGroupTestFormula")
	public ResponseEntity<Object> deleteTestGroupFormula(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {
		});
		final TestGroupTestFormula objFormula = objMapper.convertValue(inputMap.get("testgrouptestformula"),new TypeReference<TestGroupTestFormula>() {});
		final TestGroupSpecification objSpecification = objMapper.convertValue(inputMap.get("testgroupspecification"),new TypeReference<TestGroupSpecification>() {});

		requestContext.setUserInfo(userInfo);
		return objTestGroupTestService.deleteTestGroupFormula(userInfo, objFormula, objSpecification);
	}

	@PostMapping(value = "/defaultTestGroupTestFormula")
	public ResponseEntity<Object> defaultTestGroupFormula(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {
		});
		final TestGroupTestFormula objFormula = objMapper.convertValue(inputMap.get("testgrouptestformula"),new TypeReference<TestGroupTestFormula>() {});
		final TestGroupSpecification objSpecification = objMapper.convertValue(inputMap.get("testgroupspecification"),new TypeReference<TestGroupSpecification>() {});

		requestContext.setUserInfo(userInfo);
		return objTestGroupTestService.defaultTestGroupFormula(userInfo, objFormula, objSpecification);
	}

	@PostMapping(value = "/createTestGroupPredefParameter")
	public ResponseEntity<Object> createTestGroupPredefParameter(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final TestGroupSpecification objSpecification = objMapper.convertValue(inputMap.get("testgroupspecification"),new TypeReference<TestGroupSpecification>() {});
		final TestGroupTestPredefinedParameter objPredefParameter = objMapper.convertValue(inputMap.get("testgrouptestpredefinedparameter"),new TypeReference<TestGroupTestPredefinedParameter>() {});
		
//		final TestPredefinedSubcode objCodedResult = objMapper.convertValue(inputMap.get("testgroupsubcoded"),
//				TestPredefinedSubcode.class);
		final List<TestGroupTestPredefinedSubCode> lstCodedResult = objMapper.convertValue(inputMap.get("testgroupsubcoded"), new TypeReference<List<TestGroupTestPredefinedSubCode>>() {});


		requestContext.setUserInfo(userInfo);
		return objTestGroupParameterService.createTestGroupPredefParameter(userInfo, objPredefParameter,
				objSpecification,lstCodedResult);
	}

	@PostMapping(value = "/updateTestGroupPredefParameter")
	public ResponseEntity<Object> updateTestGroupPredefParameter(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final TestGroupSpecification objSpecification = objMapper.convertValue(inputMap.get("testgroupspecification"),new TypeReference<TestGroupSpecification>() {
		});
		final TestGroupTestPredefinedParameter objPredefParameter = objMapper.convertValue(inputMap.get("testgrouptestpredefinedparameter"),new TypeReference<TestGroupTestPredefinedParameter>() {
				});
		final List<TestGroupTestPredefinedSubCode> lstPredefParameterdeletesubcode = objMapper.convertValue(inputMap.get("deletetestgroupsubcoded"), new TypeReference<List<TestGroupTestPredefinedSubCode>>() {});
		final List<TestGroupTestPredefinedSubCode> lstaddsubcodedresult = objMapper.convertValue(inputMap.get("addsubcodedresult"), new TypeReference<List<TestGroupTestPredefinedSubCode>>() {});

		requestContext.setUserInfo(userInfo);
		return objTestGroupParameterService.updateTestGroupPredefParameter(userInfo, objPredefParameter,
				objSpecification,lstPredefParameterdeletesubcode,lstaddsubcodedresult);
	}

	@PostMapping(value = "/deleteTestGroupPredefParameter")
	public ResponseEntity<Object> deleteTestGroupPredefParameter(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final TestGroupTestPredefinedParameter objPredefParameter = objMapper.convertValue(inputMap.get("testgrouppredefparameter"),new TypeReference<TestGroupTestPredefinedParameter>() {});
		final List<TestGroupTestPredefinedSubCode> lstPredefParameterdeletesubcode = objMapper.convertValue(inputMap.get("deletetestgroupsubcoded"), new TypeReference<List<TestGroupTestPredefinedSubCode>>() {});
		final TestGroupSpecification objSpecification = objMapper.convertValue(inputMap.get("testgroupspecification"),new TypeReference<TestGroupSpecification>() {});

		requestContext.setUserInfo(userInfo);
		return objTestGroupParameterService.deleteTestGroupPredefParameter(userInfo, objPredefParameter,
				objSpecification,lstPredefParameterdeletesubcode);
	}

	@PostMapping(value = "/defaultTestGroupPredefParameter")
	public ResponseEntity<Object> defaultTestGroupPredefParameter(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final TestGroupTestPredefinedParameter objPredefParameter = objMapper.convertValue(inputMap.get("testgrouppredefparameter"),new TypeReference<TestGroupTestPredefinedParameter>() {
				});
		final TestGroupSpecification objSpecification = objMapper.convertValue(inputMap.get("testgroupspecification"),new TypeReference<TestGroupSpecification>() {});

		requestContext.setUserInfo(userInfo);
		return objTestGroupParameterService.defaultTestGroupPredefParameter(userInfo, objPredefParameter,
				objSpecification);
	}
	
	@PostMapping(value = "/updateTestGroupParameter")
	public ResponseEntity<Object> updateTestGroupParameter(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final TestGroupTestNumericParameter objNumericParameter = objMapper.convertValue(inputMap.get("testgrouptestnumericparameter"),new TypeReference<TestGroupTestNumericParameter>() {});
		final TestGroupTestCharParameter objCharParameter = objMapper.convertValue(inputMap.get("testgrouptestcharparameter"),new TypeReference<TestGroupTestCharParameter>() {});
		final TestGroupTestPredefinedParameter objPredefinedParameter = objMapper.convertValue(inputMap.get("testgrouptestpredefparameter"),new TypeReference<TestGroupTestPredefinedParameter>() {});
		final TestGroupTestParameter objParameter = objMapper.convertValue(inputMap.get("testgrouptestparameter"),new TypeReference<TestGroupTestParameter>() {});
		final TestGroupTestFormula testGroupTestFormula = objMapper.convertValue(inputMap.get("testgrouptestformula"),new TypeReference<TestGroupTestFormula>() {});

		requestContext.setUserInfo(userInfo);
		return objTestGroupTestService.updateTestGroupParameter(userInfo, objNumericParameter, objCharParameter,
				objPredefinedParameter, objParameter, testGroupTestFormula);
	}

	@PostMapping(value = "/getActiveParameterById")
	public ResponseEntity<Object> getActiveParameterById(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final TestGroupTestParameter objTestParameter = objMapper.convertValue(inputMap.get("testgrouptestparameter"),new TypeReference<TestGroupTestParameter>() {});
		final int ntreeversiontempcode = (Integer) inputMap.get("ntreeversiontempcode");

		requestContext.setUserInfo(userInfo);
		return objTestGroupParameterService.getActiveParameterById(userInfo, objTestParameter,ntreeversiontempcode);
	}

	@PostMapping(value = "/getSpecificationHistory")
	public ResponseEntity<Object> getSpecificationHistory(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final TestGroupSpecification objSpecification = objMapper.convertValue(inputMap.get("testgroupspecification"),new TypeReference<TestGroupSpecification>() {});
		requestContext.setUserInfo(userInfo);
		return objTestGroupSpecificationService.getSpecificationHistory(userInfo, objSpecification);
	}

	@PostMapping(value = "/getSpecificationFile")
	public ResponseEntity<Object> getSpecificationFile(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final TestGroupSpecification objSpecification = objMapper.convertValue(inputMap.get("testgroupspecification"),new TypeReference<TestGroupSpecification>() {});

		requestContext.setUserInfo(userInfo);
		return objTestGroupSpecFileService.getSpecificationFile(userInfo, objSpecification);
	}

	@PostMapping(value = "/createSpecificationFile")
	public ResponseEntity<Object> createSpecificationFile(MultipartHttpServletRequest request) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"),new TypeReference<UserInfo>() {});

		requestContext.setUserInfo(userInfo);
		return objTestGroupSpecFileService.createSpecificationFile(userInfo, request);
	}

	@PostMapping(value = "/updateSpecificationFile")
	public ResponseEntity<Object> updateSpecificationFile(MultipartHttpServletRequest request) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"),new TypeReference<UserInfo>() {});

		requestContext.setUserInfo(userInfo);
		return objTestGroupSpecFileService.updateSpecificationFile(userInfo, request);
	}

	@PostMapping(value = "/deleteSpecificationFile")
	public ResponseEntity<Object> deleteSpecificationFile(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final TestGroupSpecFile objSpecFile = objMapper.convertValue(inputMap.get("testgroupspecfile"),new TypeReference<TestGroupSpecFile>() {});
		final TestGroupSpecification objSpecification = objMapper.convertValue(inputMap.get("testgroupspecification"),new TypeReference<TestGroupSpecification>() {});
		final int ntreeversiontempcode = (Integer) inputMap.get("ntreeversiontempcode");

		requestContext.setUserInfo(userInfo);
		return objTestGroupSpecFileService.deleteSpecificationFile(userInfo, objSpecFile, objSpecification,ntreeversiontempcode);
	}

	@PostMapping(value = "/getActiveSpecFileById")
	public ResponseEntity<Object> getActiveSpecFileById(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final TestGroupSpecFile objSpecFile = objMapper.convertValue(inputMap.get("testgroupspecfile"),new TypeReference<TestGroupSpecFile>() {});
		final int ntreeversiontempcode = (Integer) inputMap.get("ntreeversiontempcode");

		requestContext.setUserInfo(userInfo);
		return objTestGroupSpecFileService.getActiveSpecFileById(userInfo, objSpecFile,ntreeversiontempcode);
	}

	@PostMapping(value = "/getActivePredefinedParameterById")
	public ResponseEntity<Object> getActivePredefinedParameterById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final TestGroupTestPredefinedParameter objPredefinedParameter = objMapper.convertValue(inputMap.get("testgrouptestpredefinedparameter"),new TypeReference<TestGroupTestPredefinedParameter>() {
				});
		final TestGroupSpecification objSpecification = objMapper.convertValue(inputMap.get("testgroupspecification"),new TypeReference<TestGroupSpecification>() {});

		requestContext.setUserInfo(userInfo);
		return objTestGroupParameterService.getActivePredefinedParameterById(userInfo, objPredefinedParameter,
				objSpecification);
	}

	@PostMapping(value = "/deleteTree")
	public ResponseEntity<Object> deleteTree(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final TreeTemplateManipulation objTree = objMapper.convertValue(inputMap.get("treetemplatemanipulation"),new TypeReference<TreeTemplateManipulation>() {});
		final int ntreeversiontempcode = (Integer) inputMap.get("ntreeversiontempcode");
		requestContext.setUserInfo(userInfo);
		return objTestGroupService.deleteTree(inputMap,userInfo,objTree, ntreeversiontempcode);
	}

	@PostMapping(value = "/getTreeById")
	public ResponseEntity<Object> getTreeById(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final TreeTemplateManipulation objTree = objMapper.convertValue(inputMap.get("treetemplatemanipulation"),new TypeReference<TreeTemplateManipulation>() {});
		final int ntreeversiontempcode = (Integer) inputMap.get("ntreeversiontempcode");
		requestContext.setUserInfo(userInfo);
		return objTestGroupService.getTreeById(userInfo,objTree, ntreeversiontempcode);
	}

	@PostMapping(value = "/updateTree")
	public ResponseEntity<Object> updateTree(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final TreeTemplateManipulation objTree = objMapper.convertValue(inputMap.get("treetemplatemanipulation"),new TypeReference<TreeTemplateManipulation>() {});
		final String sSelectedNode = (String) inputMap.get("selectednode");

		requestContext.setUserInfo(userInfo);
		return objTestGroupService.updateTree(userInfo, objTree, sSelectedNode);
	}

	@PostMapping(value = "/filterTestGroup")
	public ResponseEntity<Object> filterTestGroup(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final int nSampleTypeCode = (int) inputMap.get("nsampletypecode");
		final int nproductCatCode = (int) inputMap.get("nproductcatcode");
		final int nproductCode = (int) inputMap.get("nproductcode");
		final int nprojectCode = (int) inputMap.get("nprojectmastercode");
		final int nTreeVersionTempCode = (int) inputMap.get("ntreeversiontempcode");

		requestContext.setUserInfo(userInfo);
		return objTestGroupService.filterTestGroup(userInfo, nSampleTypeCode, nproductCatCode, nproductCode,nTreeVersionTempCode, nprojectCode);
	}

	@PostMapping(value = "/viewTestGroupTestFile")
	public ResponseEntity<Object> viewTestGroupTestFile(@RequestBody Map<String, Object> inputMap,
			HttpServletResponse response) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final int ntestgroupfilecode = (int) inputMap.get("ntestgroupfilecode");
		final int ntestgrouptestcode = (int) inputMap.get("ntestgrouptestcode");
		final TestGroupSpecification objSpecification = objMapper.convertValue(inputMap.get("testgroupspecification"),new TypeReference<TestGroupSpecification>() {});

		requestContext.setUserInfo(userInfo);
		return objTestGroupTestService.viewTestGroupTestFile(ntestgroupfilecode, userInfo, objSpecification,ntestgrouptestcode);
	}

	@PostMapping(value = "/viewTestGroupSpecFile")
	public ResponseEntity<Object> viewTestGroupSpecFile(@RequestBody Map<String, Object> inputMap,
			HttpServletResponse response) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final TestGroupSpecFile objSpecFile = objMapper.convertValue(inputMap.get("testgroupspecfile"),new TypeReference<TestGroupSpecFile>() {});
		final TestGroupSpecification objSpecification = objMapper.convertValue(inputMap.get("testgroupspecification"),new TypeReference<TestGroupSpecification>() {});

		requestContext.setUserInfo(userInfo);
		return objTestGroupSpecFileService.viewTestGroupSpecFile(objSpecFile, userInfo, objSpecification);
	}

	@PostMapping(value = "/copySpecification")
	public ResponseEntity<Object> copySpecification(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final int ntreeversiontempcode = (Integer) inputMap.get("ntreeversiontempcode");
		requestContext.setUserInfo(userInfo);
		return objTestGroupService.copySpecification(inputMap,ntreeversiontempcode);
	}

	@PostMapping(value = "/getTestGroupSampleType")
	public ResponseEntity<Object> getTestGroupSampleType(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final TestGroupSpecification objTestGroupSpec = objMapper.convertValue(inputMap.get("testgroupspecification"),new TypeReference<TestGroupSpecification>() {});

		requestContext.setUserInfo(userInfo);
		return objTestGroupService.getTestGroupSampleType(objTestGroupSpec, userInfo);
	}

	@PostMapping(value = "/validateTestGroupComplete")
	public ResponseEntity<Object> validateTestGroupComplete(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final Integer nallottedSpecCode = (Integer) inputMap.get("nallottedspeccode");

		requestContext.setUserInfo(userInfo);
		return objTestGroupService.validateTestGroupComplete(nallottedSpecCode, userInfo);
	}

	@PostMapping(value = "/specReportGenerate")
	public ResponseEntity<Object> specReportGenerate(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});

		requestContext.setUserInfo(userInfo);
		return objTestGroupSpecificationService.specReportGenerate(inputMap, userInfo);

	}

	@PostMapping(value = "/retireSpec")
	public ResponseEntity<Object> retireSpec(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return objTestGroupSpecificationService.retireSpec(inputMap, userInfo);

	}
	@PostMapping(value = "/createTestGroupTestMaterial")
	public ResponseEntity<Object> createTestGroupTestMaterial(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return objTestGroupTestMaterialService.createTestGroupTestMaterial(inputMap,userInfo);
	}
	@PostMapping(value = "/getTestGroupTestMaterial")
	public ResponseEntity<Object> getTestGroupTestMaterial(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return objTestGroupTestService.getTestGroupTestMaterial(inputMap,userInfo);
	}
	@PostMapping(value = "/getActiveTestMaterialById")
	public ResponseEntity<Object> getActiveTestMaterialById(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});

		requestContext.setUserInfo(userInfo);
		return objTestGroupTestMaterialService.getActiveTestMaterialById(inputMap,userInfo);
	}
	@PostMapping(value = "/deleteTestGroupTestMaterial")
	public ResponseEntity<Object> deleteTestGroupTestMaterial(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});

		requestContext.setUserInfo(userInfo);
		return objTestGroupTestMaterialService.deleteTestGroupTestMaterial(inputMap,userInfo);
	}
	
	@PostMapping(value = "/updateTestGroupTestMaterial")
	public ResponseEntity<Object> updateTestGroupMaterial(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
	

		requestContext.setUserInfo(userInfo);
		return objTestGroupTestMaterialService.updateTestGroupMaterial(inputMap,userInfo);
	}

	
	
	@PostMapping(value = "/createTestGroupAddSpecification")
	public ResponseEntity<Object> createTestGroupAddSpecification(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final TestGroupSpecification objSpecification = objMapper.convertValue(inputMap.get("testgroupspecification"),new TypeReference<TestGroupSpecification>() {});
		final TestGroupTestClinicalSpec objClinicalSpec = objMapper	.convertValue(inputMap.get("testgroupaddspecification"),new TypeReference<TestGroupTestClinicalSpec>() {
				});
		requestContext.setUserInfo(userInfo);
		return objTestGroupParameterService.createTestGroupAddSpecification(userInfo, objClinicalSpec,objSpecification);
	}
	
	
	@PostMapping(value = "/getActiveClinicalSpecById")
	public ResponseEntity<Object> getActiveClinicalSpecById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final TestGroupTestClinicalSpec objClinicalSpec = objMapper.convertValue(inputMap.get("testgrouptestpredefinedparameter"),new TypeReference<TestGroupTestClinicalSpec>() {});
		final TestGroupSpecification objSpecification = objMapper.convertValue(inputMap.get("testgroupspecification"),new TypeReference<TestGroupSpecification>() {});

		requestContext.setUserInfo(userInfo);
		return objTestGroupParameterService.getActiveClinicalSpecById(userInfo, objClinicalSpec,objSpecification);
	}
	
	
	
	@PostMapping(value = "/deleteTestGroupAddSpecification")
	public ResponseEntity<Object> deleteTestGroupAddSpecification(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final TestGroupTestClinicalSpec objClinicalSpec = objMapper.convertValue(inputMap.get("testgroupaddspecification"),new TypeReference<TestGroupTestClinicalSpec>() {
				});
		final TestGroupSpecification objSpecification = objMapper.convertValue(inputMap.get("testgroupspecification"),new TypeReference<TestGroupSpecification>() {});

		requestContext.setUserInfo(userInfo);
		return objTestGroupParameterService.deleteTestGroupAddSpecification(userInfo, objClinicalSpec,
				objSpecification);
	}
	
	
	@PostMapping(value = "/updateTestGroupAddSpecification")
	public ResponseEntity<Object> updateTestGroupAddSpecification(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final TestGroupSpecification objSpecification = objMapper.convertValue(inputMap.get("testgroupspecification"),new TypeReference<TestGroupSpecification>() {});
		final TestGroupTestClinicalSpec objClinicalSpec = objMapper.convertValue(inputMap.get("testgroupaddspecification"),new TypeReference<TestGroupTestClinicalSpec>() {});

		requestContext.setUserInfo(userInfo);
		return objTestGroupParameterService.updateTestGroupAddSpecification(userInfo, objClinicalSpec,
				objSpecification);
	}
	

	@PostMapping(value = "/validationForRetiredTemplate")
	public ResponseEntity<Object> validationForRetiredTemplate(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
	

		requestContext.setUserInfo(userInfo);
		return objTestGroupTestMaterialService.validationForRetiredTemplate(inputMap,userInfo);
	}
	
	@RequestMapping(value = "/getAvailableMaterial", method = RequestMethod.POST)
	public ResponseEntity<Object> getAvailableMaterial(@RequestBody Map<String, Object> inputMap) throws Exception {
			ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});			
			requestContext.setUserInfo(userInfo);
			return objTestGroupTestMaterialService.getAvailableMaterial(inputMap, userInfo);

	}
	
	
	@PostMapping(value = "/getActivePredefinedParameterSubCodedById")
	public ResponseEntity<Object> getActivePredefinedParameterSubCodedById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		final TestGroupTestPredefinedParameter objPredefinedParameter = objMapper.convertValue(inputMap.get("testgrouptestpredefinedparameter"),new TypeReference<TestGroupTestPredefinedParameter>() {});
		final TestGroupSpecification objSpecification = objMapper.convertValue(inputMap.get("testgroupspecification"),new TypeReference<TestGroupSpecification>() {});

		requestContext.setUserInfo(userInfo);
		return objTestGroupParameterService.getActivePredefinedParameterSubCodedById(userInfo, objPredefinedParameter,objSpecification);
	}

	
	@PostMapping(value = "/getActivePredefinedParameterSubCoded")
	public ResponseEntity<Object> getActivePredefinedParameterSubCoded(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {
		});
		final TestGroupTestPredefinedParameter objPredefinedParameter = objMapper.convertValue(inputMap.get("testgrouptestpredefinedparameter"),new TypeReference<TestGroupTestPredefinedParameter>() {
				});
		requestContext.setUserInfo(userInfo);
		return objTestGroupParameterService.getActivePredefinedParameterSubCoded(userInfo, objPredefinedParameter);
	}
	
	
	
	//To get spec details for copy action --ALPD-4099 ,work done by Dhanushya R I
	@PostMapping(value = "/getSpecDetailsForCopy")
	public ResponseEntity<Object> getSpecDetailsForCopy(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return objTestGroupService.getSpecDetailsForCopy(inputMap,userInfo);
	}
	
}
