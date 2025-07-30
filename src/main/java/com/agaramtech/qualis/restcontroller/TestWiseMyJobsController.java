package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.joballocation.service.TestWiseMyJobs.TestWiseMyJobsService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping(value = "/testwisemyjobs")
public class TestWiseMyJobsController {

	private RequestContext requestContext;

	private final TestWiseMyJobsService testwisemyjobsService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext RequestContext to hold the request
	 * @param TestWiseMyJobsService testwisemyjobsService
	 */
	public TestWiseMyJobsController(RequestContext requestContext, TestWiseMyJobsService testwisemyjobsService) {
		super();
		this.requestContext = requestContext;
		this.testwisemyjobsService = testwisemyjobsService;
	}

	@PostMapping(path = "/getTestWiseMyJobs")
	public ResponseEntity<Object> getTestWiseMyJobs(@RequestBody Map<String, Object> inputMap) throws Exception {
		inputMap.put("nflag", 1);
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testwisemyjobsService.getTestWiseMyJobs(inputMap, userInfo);
	}

	@PostMapping(path = "/getSampleType")
	public ResponseEntity<Object> getSampleType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testwisemyjobsService.getSampleType(inputMap, userInfo);
	}

	@PostMapping(path = "/getRegistrationType")
	public ResponseEntity<Object> getRegistrationType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testwisemyjobsService.getRegistrationType(inputMap, userInfo);
	}

	@PostMapping(path = "/getRegistrationTypeBySampleType")
	public ResponseEntity<Object> getRegistrationTypeBySampleType(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testwisemyjobsService.getRegistrationTypeBySampleType(inputMap, userInfo);
	}

	@PostMapping(path = "/getRegistrationsubType")
	public ResponseEntity<Object> getRegistrationsubType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testwisemyjobsService.getRegistrationsubType(inputMap, userInfo);
	}

	@PostMapping(path = "/getRegistrationsubTypeByRegType")
	public ResponseEntity<Object> getRegistrationsubTypeByRegType(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testwisemyjobsService.getRegistrationsubTypeByRegType(inputMap, userInfo);
	}

	@PostMapping(path = "/getFilterStatus")
	public ResponseEntity<Object> getFilterStatus(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testwisemyjobsService.getFilterStatus(inputMap, userInfo);
	}

	@PostMapping(path = "/getFilterStatusByApproveVersion")
	public ResponseEntity<Object> getFilterStatusByApproveVersion(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testwisemyjobsService.getFilterStatusByApproveVersion(inputMap, userInfo);
	}

	@PostMapping(path = "/getSection")
	public ResponseEntity<Object> getSection(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testwisemyjobsService.getSection(inputMap, userInfo);
	}

	@PostMapping(path = "/getSectionByApproveVersion")
	public ResponseEntity<Object> getSectionByApproveVersion(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testwisemyjobsService.getSectionByApproveVersion(inputMap, userInfo);
	}

	@PostMapping(path = "/getApprovalConfigVersion")
	public ResponseEntity<Object> getApprovalConfigVersion(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testwisemyjobsService.getApprovalConfigVersion(inputMap, userInfo);
	}

	@PostMapping(path = "/getApprovalConfigVersionByRegSubType")
	public ResponseEntity<Object> getApprovalConfigVersionByRegSubType(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testwisemyjobsService.getApprovalConfigVersionByRegSubType(inputMap, userInfo);
	}

	@PostMapping(path = "/getTestCombo")
	public ResponseEntity<Object> getTestCombo(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testwisemyjobsService.getTestCombo(inputMap, userInfo);
	}

	@PostMapping(path = "/getTestComboBySection")
	public ResponseEntity<Object> getTestComboBySection(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testwisemyjobsService.getTestComboBySection(inputMap, userInfo);
	}

	@PostMapping(path = "/getDesignTemplateByApprovalConfigVersion")
	public ResponseEntity<Object> getDesignTemplateByApprovalConfigVersion(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testwisemyjobsService.getDesignTemplateByApprovalConfigVersion(inputMap, userInfo);
	}

	@PostMapping(value = "/getMyTestWiseJobsDetails")
	public ResponseEntity<Object> getMyTestWiseJobsDetails(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testwisemyjobsService.getMyTestWiseJobsDetails(inputMap, userInfo);
	}

	@PostMapping(path = "/CreateAcceptTest")
	public ResponseEntity<Object> CreateAcceptTest(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testwisemyjobsService.CreateAcceptTest(inputMap, userInfo);

	}

	@PostMapping(value = "/createFilterName")
	public ResponseEntity<Object> createFilterName(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return testwisemyjobsService.createFilterName(inputMap, userInfo);
	}

	@PostMapping(value = "/getmyjobsFilterDetails")
	public ResponseEntity<Object> getmyjobsFilterDetails(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testwisemyjobsService.getmyjobsFilterDetails(inputMap, userInfo);

	}

}
