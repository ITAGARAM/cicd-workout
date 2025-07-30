package com.agaramtech.qualis.restcontroller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.configuration.model.FilterName;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.registration.model.Registration;
import com.agaramtech.qualis.registration.model.RegistrationSample;
import com.agaramtech.qualis.registration.service.RegistrationService;
import com.agaramtech.qualis.testgroup.model.TestGroupTest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/registration")
public class RegistrationController {

	private RequestContext requestContext;
	private final RegistrationService registrationService;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param unitService UnitService
	 */
	public RegistrationController(RequestContext requestContext, RegistrationService registrationService) {
		super();
		this.requestContext = requestContext;
		this.registrationService = registrationService;
	}


	@PostMapping(value = "/getRegistration")
	public ResponseEntity<Object> getRegistration(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		final String currentUIDate = (String) inputMap.get("currentdate");
		return registrationService.getRegistration(userInfo, currentUIDate);

	}

	@PostMapping(value = "/getTreeByProduct")
	public ResponseEntity<Object> getTreeByProduct(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return registrationService.getTreeByProduct(inputMap);

	}
	
	@PostMapping(value = "/getTestGroupSpecification")
	public ResponseEntity<Object> getTestGroupSpecification(@RequestBody Map<String, Object> objmap) throws Exception {

		int npreregno = 0;
		int ntreetemplatemanipulationcode = 0;
		int ntestGroupSpecRequired = 4;	//ALPD-4834,Vishakh, Added ntestGroupSpecRequired to handle show or hide add spec button
		
		if (objmap.get("npreregno") != null) {
			npreregno = (int) objmap.get("npreregno");
		}

		if (objmap.get("ntreetemplatemanipulationcode") != null) {
			ntreetemplatemanipulationcode = (int) objmap.get("ntreetemplatemanipulationcode");
		}
		
		if(objmap.get("ntestgroupspecrequired") != null) {
			ntestGroupSpecRequired = (int) objmap.get("ntestgroupspecrequired");
		}
		
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(objmap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		
		return registrationService.getTestGroupSpecification(ntreetemplatemanipulationcode, 
				npreregno, ntestGroupSpecRequired);
	}
	
	@PostMapping(value = "/getComponentBySpec")
	public ResponseEntity<Object> getComponentBySpec(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		final ObjectMapper objMapper = new ObjectMapper();		
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		
		return registrationService.getComponentBySpec(inputMap);
	}
	
	@PostMapping(value = "/getTestfromDB")
	public ResponseEntity<Object> getTestfromDB(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		
		requestContext.setUserInfo(userInfo);
		return registrationService.getTestfromDB(inputMap);

	}
	
	@PostMapping(value = "/getTestfromTestPackage")
	public ResponseEntity<Object> getTestfromTestPackage(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		
		requestContext.setUserInfo(userInfo);
		return registrationService.getTestfromTestPackage(inputMap);
	}
	
	/**
	 * This Method is used to get the over all sample Registration filter with its
	 * data
	 * 
	 * @param obj [Map] contains key nmastersitecode which holds the value of
	 *            respective site code
	 * @return a response entity which holds the list of QBCategory with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getRegTypeBySampleType")
	public ResponseEntity<Object> getRegTypeBySampleType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return registrationService.getRegTypeBySampleType(inputMap);
	}
	
	@PostMapping(value = "/getRegTemplateTypeByRegSubType")
	public ResponseEntity<Object> getRegTemplateTypeByRegSubType(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
	
		return registrationService.getRegTemplateTypeByRegSubType(inputMap);

	}

	@PostMapping(value = "/getApprovalConfigBasedTemplateDesign")
	public ResponseEntity<Object> getApprovalConfigBasedTemplateDesign(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		
		return registrationService.getApprovalConfigBasedTemplateDesign(inputMap, userInfo);

	}
	
	/**
	 * This Method is used to get the over all sample Registration filter with its
	 * dataa
	 * 
	 * @param obj [Map] contains key nmastersitecode which holds the value of
	 *            respective site code
	 * @return a response entity which holds the list of QBCategory with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getRegSubTypeByRegType")
	public ResponseEntity<Object> getRegSubTypeByRegType(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return registrationService.getRegSubTypeByRegType(inputMap);

	}
	
	@PostMapping(value = "/getRegistrationByFilterSubmit")
	public ResponseEntity<Object> getRegistrationByFilterSubmit(@RequestBody Map<String, Object> objmap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(objmap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return registrationService.getRegistrationByFilterSubmit(objmap);
	}

	@PostMapping(value = "/getRegistrationSubSample")
	public ResponseEntity<Object> getRegistrationSubSample(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return registrationService.getRegistrationSubSample(inputMap, userInfo);

	}
	@PostMapping(value = "/getRegistrationTest")
	public ResponseEntity<Object> getRegistrationTest(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return registrationService.getRegistrationTest(inputMap, userInfo);

	}

	@PostMapping(value = "/getregistrationparameter")
	public ResponseEntity<Object> getRegistrationParameter(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final String ntransactionTestCode = (String) inputMap.get("ntransactiontestcode");
		requestContext.setUserInfo(userInfo);
		return registrationService.getRegistrationParameter(ntransactionTestCode, userInfo);

	}
	
	
	@PostMapping(value = "/getMoreTest")
	public ResponseEntity<Object> getMoreTest(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return registrationService.getMoreTest(inputMap, userInfo);

	}

	@PostMapping(value = "/getMoreTestPackage")
	public ResponseEntity<Object> getMoreTestPackage(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return registrationService.getMoreTestPackage(inputMap, userInfo);

	}
	
	@PostMapping(value = "/createRegistration")
	public ResponseEntity<Object> insertRegistration(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return registrationService.insertRegistration(inputMap);
	}

	@PostMapping(value = "/createRegistrationWithFile")
	public ResponseEntity<Object> insertRegistrationWithFile(MultipartHttpServletRequest inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(inputMap.getParameter("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return registrationService.insertRegistrationWithFile(inputMap);

	}

	@PostMapping(value = "/acceptRegistration")
	public ResponseEntity<Object> acceptRegistration(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return registrationService.acceptRegistration(inputMap);
	}

	
///////////////////////////////////////////
	
	@PostMapping(value = "/getRegistrationTemplate")
	public ResponseEntity<Object> getRegistrationTemplate(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		int nregtypecode = (int) inputMap.get("nregtypecode");
		int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
		return registrationService.getRegistrationTemplate(nregtypecode, nregsubtypecode);

	}

	@PostMapping(value = "/createTest")
	public ResponseEntity<Object> createTest(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final List<TestGroupTest> listTest = objMapper.convertValue(inputMap.get("TestGroupTest"),
				new TypeReference<List<TestGroupTest>>() {
				});
		final List<String> listSample = objMapper.convertValue(inputMap.get("RegistrationSample"),
				new TypeReference<List<String>>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nregtypecode = (int) inputMap.get("nregtypecode");
		final int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
		return registrationService.createTest(userInfo, listSample, listTest, nregtypecode, nregsubtypecode, inputMap);

	}

	@PostMapping(value = "/getEditRegistrationDetails")
	public ResponseEntity<Object> getEditRegistrationDetails(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);

		return registrationService.getEditRegistrationDetails(inputMap, userInfo);

	}

	@PostMapping(value = "/updateRegistration")
	public ResponseEntity<Object> updateRegistration(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);

		return registrationService.updateRegistration(inputMap);

	}

	@PostMapping(value = "/updateRegistrationWithFile")
	public ResponseEntity<Object> updateRegistrationWithFile(MultipartHttpServletRequest inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(inputMap.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);

		return registrationService.updateRegistrationWithFile(inputMap);

	}

	@PostMapping(value = "/cancelTest")
	public ResponseEntity<Object> cancelTest(@RequestBody Map<String, Object> objmap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(objmap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return registrationService.cancelTest(objmap);

	}

	@PostMapping(value = "/cancelSample")
	public ResponseEntity<Object> cancelSample(@RequestBody Map<String, Object> objmap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(objmap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return registrationService.cancelSample(objmap);

	}

	@PostMapping(value = "/createSubSample")
	public ResponseEntity<Object> createSubSample(@RequestBody Map<String, Object> objmap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(objmap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return registrationService.createSubSample(objmap);

	}

	@PostMapping(value = "/createSubSampleWithFile")
	public ResponseEntity<Object> createSubSampleWithFile(MultipartHttpServletRequest objmap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(objmap.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return registrationService.createSubSampleWithFile(objmap);

	}

	@PostMapping(value = "/getEditRegistrationSubSampleDetails")
	public ResponseEntity<Object> getEditRegistrationSubSampleDetails(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);

		return registrationService.getEditRegistrationSubSampleDetails(inputMap, userInfo);

	}

	@PostMapping(value = "/updateRegistrationSubSample")
	public ResponseEntity<Object> updateRegistrationSubSample(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);

		return registrationService.updateRegistrationSubSample(inputMap);

	}

	@PostMapping(value = "/updateRegistrationSubSampleWithFile")
	public ResponseEntity<Object> updateRegistrationSubSampleWithFile(MultipartHttpServletRequest inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(inputMap.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);

		return registrationService.updateRegistrationSubSampleWithFile(inputMap);

	}

	@PostMapping(value = "/cancelSubSample")
	public ResponseEntity<Object> cancelSubSample(@RequestBody Map<String, Object> objmap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(objmap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return registrationService.cancelSubSample(objmap);

	}

	@PostMapping(value = "/quarantineRegistration")
	public ResponseEntity<Object> quarantineRegistration(@RequestBody Map<String, Object> objmap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(objmap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return registrationService.quarantineRegistration(objmap);

	}

	@PostMapping(value = "/getRegistrationFilterStatus")
	public ResponseEntity<Object> getRegistrationFilterStatus(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();

		final int nregtypecode = (int) inputMap.get("nregtypecode");
		final int nregsubtypecode = (int) inputMap.get("nregsubtypecode");

		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return new ResponseEntity<>(registrationService.getFilterStatus(nregtypecode, nregsubtypecode, userInfo),
				HttpStatus.OK);
	}

	@PostMapping(value = "/schedulerinsertRegistration")
	public ResponseEntity<Object> schedulerinsertRegistration(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return registrationService.schedulerinsertRegistration(inputMap);

	}

	@PostMapping(value = "/getTestBasesdTestPackage")
	public ResponseEntity<Object> getTestBasesdTestPackage(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return registrationService.getTestBasesdTestPackage(inputMap);

	}

	@PostMapping(value = "/getSampleBasedOnExternalOrder")
	public ResponseEntity<Object> getSampleBasedOnExternalOrder(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return registrationService.getSampleBasedOnExternalOrder(inputMap, userInfo);

	}

	@PostMapping(value = "/viewRegistrationFile")
	public ResponseEntity<Object> viewRegistrationFile(@RequestBody Map<String, Object> inputMap,
			HttpServletResponse response) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final Registration objFile = objMapper.convertValue(inputMap.get("viewFile"), Registration.class);
		final Map<String, Object> outputMap = registrationService.viewRegistrationFile(objFile, userInfo, inputMap);
		requestContext.setUserInfo(userInfo);
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}

	@PostMapping(value = "/viewRegistrationSubSampleFile")
	public ResponseEntity<Object> viewRegistrationSubSampleFile(@RequestBody Map<String, Object> inputMap,
			HttpServletResponse response) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final RegistrationSample objFile = objMapper.convertValue(inputMap.get("viewFile"), RegistrationSample.class);
		Map<String, Object> outputMap = registrationService.viewRegistrationSubSampleFile(objFile, userInfo, inputMap);
		requestContext.setUserInfo(userInfo);
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}

	@RequestMapping(value = "/importRegistrationData", method = RequestMethod.POST)
	public ResponseEntity<Object> importRegistrationData(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return registrationService.importRegistrationData(request, userInfo);
	}

	@PostMapping(value = "/getExternalOrderForMapping")
	public ResponseEntity<Object> getExternalOrderForMapping(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return registrationService.getExternalOrderForMapping(inputMap, userInfo);

	}

	@PostMapping(value = "/orderMapping")
	public ResponseEntity<Object> orderMapping(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return registrationService.orderMapping(inputMap, userInfo);

	}

	@RequestMapping(value = "/importRegistrationSample", method = RequestMethod.POST)
	public ResponseEntity<Object> importRegistrationSample(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return registrationService.importRegistrationSample(request, userInfo);

	}

	@PostMapping(value = "/getExternalOrderAttachment")
	public ResponseEntity<Object> getExternalOrderAttachment(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		final String nexternalordercode = inputMap.get("OrderCodeData").toString();
		final String npreregno = inputMap.containsKey("selectedPreregno") ? inputMap.get("selectedPreregno").toString()
				: inputMap.get("npreregno").toString();
		return registrationService.getExternalOrderAttachment(nexternalordercode, npreregno, userInfo);

	}

	@PostMapping(value = "/getOutsourceDetails")
	public ResponseEntity<Object> getOutsourceDetails(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		final String npreregno = inputMap.get("npreregno").toString();
		final String ntransactionsamplecode = inputMap.get("ntransactionsamplecode").toString();
		return registrationService.getOutsourceDetails(npreregno, ntransactionsamplecode, userInfo);

	}

	@SuppressWarnings("unchecked")
	@PostMapping(value = "/viewExternalOrderAttachment")
	public ResponseEntity<Object> viewExternalOrderAttachment(@RequestBody Map<String, Object> inputMap,
			HttpServletResponse response) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final Map<String, Object> objExternalOrderAttachmentFile = (Map<String, Object>) inputMap
				.get("externalorderattachment");
		final int ncontrolCode = (int) inputMap.get("ncontrolcode");
		final Map<String, Object> outputMap = registrationService.viewExternalOrderAttachment(objExternalOrderAttachmentFile,
				ncontrolCode, userInfo);
		requestContext.setUserInfo(userInfo);
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}

	@PostMapping(value = "/getTestfromSection")
	public ResponseEntity<Object> getTestfromSection(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return registrationService.getTestfromSection(inputMap);

	}

	@PostMapping(value = "/getTestSectionBasesdTestPackage")
	public ResponseEntity<Object> getTestSectionBasesdTestPackage(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return registrationService.getTestSectionBasesdTestPackage(inputMap);

	}

	@PostMapping(value = "/getTestBasedTestSection")
	public ResponseEntity<Object> getTestBasedTestSection(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final  UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return registrationService.getTestBasedTestSection(inputMap);

	}

	@PostMapping(value = "/getMoreTestSection")
	public ResponseEntity<Object> getMoreTestSection(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return registrationService.getMoreTestSection(inputMap, userInfo);
	}
	
	
	@PostMapping(value = "/getAdhocTest")
	public ResponseEntity<Object> getAdhocTest(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return registrationService.getAdhocTest(inputMap, userInfo);

	}
	
	@PostMapping(value = "/createAdhocTest")
	public ResponseEntity<Object> createAdhocTest(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final List<String> listSample = objMapper.convertValue(inputMap.get("RegistrationSample"),
				new TypeReference<List<String>>() {
				});
		
		final TestGroupTest objTest = objMapper.convertValue(inputMap.get("TestGroupTest"), TestGroupTest.class);
		final List<TestGroupTest> listTest = new ArrayList<>();
        listTest.add(objTest);
		
        final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nregtypecode = (int) inputMap.get("nregtypecode");
		final int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
		return registrationService.createAdhocTest(userInfo, listSample, listTest, nregtypecode, nregsubtypecode, inputMap);

	}
	
	
	@PostMapping(value = "/getSampleBasedOnPortalOrder")
	public ResponseEntity<Object> getSampleBasedOnPortalOrder(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return registrationService.getSampleBasedOnPortalOrder(inputMap, userInfo);

	}
	
	@PostMapping(value = "/copySample")
	public ResponseEntity<Object> copySample(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return registrationService.copySample(inputMap, userInfo);
	}
	
	//Added by Dhanushya RI for JIRA ID:ALPD-4912,ALPD-4913,ALPD-4914  Filter save detail --Start
	@PostMapping(value = "/createFilterName")
	public ResponseEntity<Object> createFilterName(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return registrationService.createFavoriteFilterName(inputMap, userInfo);
	}
		
	@PostMapping(value = "/getFilterName")
	public ResponseEntity<Object> getFilterName(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		List<FilterName> outputMap = registrationService.getFavoriteFilterName(userInfo);
		requestContext.setUserInfo(userInfo);
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
		
	}
	
	@PostMapping(value = "/getRegistrationFilter")
	public ResponseEntity<Object> getRegistrationFilter(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
	
		return registrationService.getRegistrationFilter(inputMap,userInfo);

	}//End
		
	//Added by Dhanushya RI for JIRA ID:ALPD-5511 edit test method insert --Start
	/**
	 * This Method is used to get the over all methods with respect to test
	 * 
	 * @param inputMap [Map] contains key userinfo which holds the value of
	 *                 respective user detail
	 * @return a response entity which holds the list of method with respect to test
	 *         and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getTestMethod")
	public ResponseEntity<Object> getTestMethod(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return registrationService.getTestMethod(inputMap, userInfo);
	}
		
	/**
	 * This method is used to update entry in registrationtest table.
	 * 
	 * @param inputMap [Map] holds the test and userinfo object to be updated
	 * @return response entity object holding response status and data of updated
	 *         method object
	 * @throws Exception
	 */
	@PostMapping(value = "/updateTestMethod")
	public ResponseEntity<Object> updateTestMethod(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return registrationService.updateTestMethod(inputMap, userInfo);
	}
	//End
}
