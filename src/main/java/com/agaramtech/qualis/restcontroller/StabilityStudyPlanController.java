package com.agaramtech.qualis.restcontroller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.stability.service.stabilitystudyplan.StabilityStudyPlanService;
import com.agaramtech.qualis.testgroup.model.TestGroupTest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/stabilitystudyplan")
public class StabilityStudyPlanController {

	private RequestContext requestContext;
	private final StabilityStudyPlanService stabilityStudyPlanService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext         RequestContext to hold the request
	 * @param storageCategoryService StorageCategoryService
	 */
	public StabilityStudyPlanController(RequestContext requestContext,
			StabilityStudyPlanService stabilityStudyPlanService) {
		super();
		this.requestContext = requestContext;
		this.stabilityStudyPlanService = stabilityStudyPlanService;
	}

	@PostMapping(value = "/getStabilityStudyPlan")
	public ResponseEntity<Object> getStabilityStudyPlan(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		final String currentUIDate = (String) inputMap.get("currentdate");
		return stabilityStudyPlanService.getStabilityStudyPlan(userInfo, currentUIDate);
	}

	@PostMapping(value = "/createStabilityStudyPlan")
	public ResponseEntity<Object> insertRegistration(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return stabilityStudyPlanService.insertStbStudyPlan(inputMap);
	}

	@PostMapping(value = "/importStabilityStudyPlan")
	public ResponseEntity<Object> importStabilityStudyPlan(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return stabilityStudyPlanService.importStabilityStudyPlan(request, userInfo);
	}

	@PostMapping(value = "/createTest")
	public ResponseEntity<Object> createTest(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final List<TestGroupTest> listTest = objMapper.convertValue(inputMap.get("TestGroupTest"),
				new TypeReference<List<TestGroupTest>>() {
				});
		final List<String> listSample = objMapper.convertValue(inputMap.get("StbTimePoint"),
				new TypeReference<List<String>>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nregtypecode = (int) inputMap.get("nregtypecode");
		final int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
		return stabilityStudyPlanService.createTest(userInfo, listSample, listTest, nregtypecode, nregsubtypecode,
				inputMap);

	}

	@PostMapping(value = "/createTimePoint")
	public ResponseEntity<Object> createTimePoint(@RequestBody Map<String, Object> objmap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(objmap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return stabilityStudyPlanService.createTimePoint(objmap);

	}

	@PostMapping(value = "/getRegistrationSubSample")
	public Map<String, Object> getRegistrationSubSample(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return stabilityStudyPlanService.getRegistrationSubSample(inputMap, userInfo);
	}

	@PostMapping(value = "/getRegistrationTest")
	public Map<String, Object> getRegistrationTest(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return stabilityStudyPlanService.getRegistrationTest(inputMap, userInfo);
	}

	@PostMapping(value = "/getStabilityStudyPlanByFilterSubmit")
	public ResponseEntity<Object> getStabilityStudyPlanByFilterSubmit(@RequestBody Map<String, Object> objmap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(objmap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return stabilityStudyPlanService.getStabilityStudyPlanByFilterSubmit(objmap);
	}

	@PostMapping(value = "/approveStabilityStudyPlan")
	public ResponseEntity<Object> approveStabilityStudyPlan(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return stabilityStudyPlanService.approveStabilityStudyPlan(inputMap);
	}

	@PostMapping(value = "/deleteTest")
	public ResponseEntity<Object> deleteTest(@RequestBody Map<String, Object> objmap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(objmap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return stabilityStudyPlanService.deleteTest(objmap);

	}

	@PostMapping(value = "/deleteStbStudyPlan")
	public ResponseEntity<Object> deleteStbStudyPlan(@RequestBody Map<String, Object> objmap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(objmap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return stabilityStudyPlanService.deleteStbStudyPlan(objmap);

	}

	@PostMapping(value = "/getEditStbTimePointDetails")
	public Map<String,Object> getEditStbTimePointDetails(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return stabilityStudyPlanService.getEditStbTimePointDetails(inputMap, userInfo);
	}

	@PostMapping(value = "/updateStbTimePoint")
	public ResponseEntity<Object> updateStbTimePoint(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return stabilityStudyPlanService.updateStbTimePoint(inputMap);

	}

	@PostMapping(value = "/deleteStbTimePoint")
	public ResponseEntity<Object> deleteStbTimePoint(@RequestBody Map<String, Object> objmap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(objmap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return stabilityStudyPlanService.deleteStbTimePoint(objmap);

	}

	@PostMapping(value = "/getRegSubTypeByRegType")
	public ResponseEntity<Object> getRegSubTypeByRegType(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return stabilityStudyPlanService.getRegSubTypeByRegType(inputMap);

	}

	@PostMapping(value = "/getRegTemplateTypeByRegSubType")
	public ResponseEntity<Object> getRegTemplateTypeByRegSubType(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return stabilityStudyPlanService.getRegTemplateTypeByRegSubType(inputMap);

	}

	@PostMapping(value = "/getApprovalConfigBasedTemplateDesign")
	public ResponseEntity<Object> getApprovalConfigBasedTemplateDesign(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return stabilityStudyPlanService.getApprovalConfigBasedTemplateDesign(inputMap, userInfo);

	}

	@PostMapping(value = "/getRegTypeBySampleType")
	public ResponseEntity<Object> getRegTypeBySampleType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return stabilityStudyPlanService.getRegTypeBySampleType(inputMap);
	}

	@PostMapping(value = "/getregistrationparameter")
	public ResponseEntity<Object> getRegistrationParameter(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return stabilityStudyPlanService.getRegistrationParameter(inputMap, userInfo);
	}

	@PostMapping(value = "/getTimePointHistory")
	public ResponseEntity<Object> getTestHistory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return stabilityStudyPlanService.getTimePointHistory(inputMap, userInfo);

	}

}
