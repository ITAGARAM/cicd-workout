package com.agaramtech.qualis.restcontroller;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.configuration.model.FilterName;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.joballocation.service.JobAllocation.JobAllocationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping(value = "/joballocation")
public class JobAllocationController {

	private RequestContext requestContext;
	private final JobAllocationService jobAllocationService;

	public JobAllocationController(JobAllocationService jobAllocationService, RequestContext requestContext) {
		this.jobAllocationService = jobAllocationService;
		this.requestContext = requestContext;
	}

	@PostMapping(path = "/getJobAllocation")
	public ResponseEntity<Object> getJobAllocation(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		inputMap.put("nflag", 1);
		return jobAllocationService.getJobAllocation(inputMap, userInfo);
	}

	@PostMapping(path = "/getSampleType")
	public ResponseEntity<Map<String,Object>> getSampleType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return jobAllocationService.getSampleType(inputMap, userInfo);
	}

	@PostMapping(path = "/getRegistrationType")
	public ResponseEntity<Map<String, Object>> getRegistrationType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return jobAllocationService.getRegistrationType(inputMap, userInfo);
	}

	@PostMapping(path = "/getRegistrationsubType")
	public ResponseEntity<Map<String, Object>> getRegistrationsubType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return jobAllocationService.getRegistrationsubType(inputMap, userInfo);
	}

	@PostMapping(path = "/getApprovalConfigVersion")
	public ResponseEntity<Object> getApprovalConfigVersion(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return jobAllocationService.getApprovalConfigVersion(inputMap, userInfo);
	}

	@PostMapping(path = "/getFilterStatus")
	public ResponseEntity<Object> getFilterStatus(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return jobAllocationService.getFilterStatus(inputMap, userInfo);
	}

	@PostMapping(path = "/getSection")
	public ResponseEntity<Object> getSection(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return jobAllocationService.getSection(inputMap, userInfo);
	}

	@PostMapping(path = "/getTestCombo")
	public ResponseEntity<Object> getTestCombo(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return jobAllocationService.getTestCombo(inputMap, userInfo);
	}

	@PostMapping(path = "/getRegistrationTypeBySampleType")
	public ResponseEntity<Object> getRegistrationTypeBySampleType(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return jobAllocationService.getRegistrationTypeBySampleType(inputMap, userInfo);
	}

	@PostMapping(path = "/getRegistrationsubTypeByRegType")
	public ResponseEntity<Map<String,Object>> getRegistrationsubTypeByRegType(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return jobAllocationService.getRegistrationsubTypeByRegType(inputMap, userInfo);
	}

	@PostMapping(path = "/getApprovalConfigVersionByRegSubType")
	public ResponseEntity<Object> getApprovalConfigVersionByRegSubType(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return jobAllocationService.getApprovalConfigVersionByRegSubType(inputMap, userInfo);
	}

	@PostMapping(path = "/getFilterStatusByApproveVersion")
	public ResponseEntity<Object> getFilterStatusByApproveVersion(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return jobAllocationService.getFilterStatusByApproveVersion(inputMap, userInfo);
	}

	@PostMapping(path = "/getSectionByApproveVersion")
	public ResponseEntity<Object> getSectionByApproveVersion(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return jobAllocationService.getSectionByApproveVersion(inputMap, userInfo);
	}

	@PostMapping(path = "/getTestComboBySection")
	public ResponseEntity<Object> getTestComboBySection(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return jobAllocationService.getTestComboBySection(inputMap, userInfo);
	}

	@PostMapping(path = "/getDesignTemplateByApprovalConfigVersion")
	public ResponseEntity<Object> getDesignTemplateByApprovalConfigVersion(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return jobAllocationService.getDesignTemplateByApprovalConfigVersion(inputMap, userInfo);
	}

	@PostMapping(path = "/getJobAllocationDetails")
	public ResponseEntity<Object> getJobAllocationDetails(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return jobAllocationService.getJobAllocationDetails(inputMap, userInfo);
	}

	@PostMapping(path = "/getJobAllocationSubSampleDetails")
	public ResponseEntity<Object> getJobAllocationSubSampleDetails(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return jobAllocationService.getJobAllocationSubSampleDetails(inputMap, userInfo);
	}

	@PostMapping(path = "/getJobAllocationTestDetails")
	public ResponseEntity<Object> getJobAllocationTestDetails(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return jobAllocationService.getJobAllocationTestDetails(inputMap, userInfo);
	}

	@PostMapping(path = "/getTestView")
	public ResponseEntity<Object> getTestView(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return jobAllocationService.getTestView(inputMap, userInfo);
	}

	@PostMapping(path = "/CreateReceiveinLab")
	public ResponseEntity<Object> CreateReceiveinLab(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return jobAllocationService.CreateReceiveinLab(inputMap, userInfo);
	}

	@PostMapping(path = "/getAllotDetails")
	public ResponseEntity<Object> getAllotDetails(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return jobAllocationService.getAllotDetails(inputMap, userInfo);
	}

	@PostMapping(path = "/getAllotAnotherUserDetails")
	public ResponseEntity<Object> getAllotAnotherUserDetails(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return jobAllocationService.getAllotAnotherUserDetails(inputMap, userInfo);
	}

	@PostMapping(path = "/getInstrumentNameBasedCategory")
	public ResponseEntity<Object> getInstrumentNameBasedCategory(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final int instrumentCatCode = (Integer) inputMap.get("ninstrumentcatcode");
		final int calibrationRequired = (Integer) inputMap.get("ncalibrationreq");

		requestContext.setUserInfo(userInfo);
		return jobAllocationService.getInstrumentNameBasedCategory(instrumentCatCode, calibrationRequired, userInfo);
	}

	@PostMapping(path = "/getInstrumentIdBasedCategory")
	public ResponseEntity<Object> getInstrumentIdBasedCategory(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final int instrumentCatCode = (Integer) inputMap.get("ninstrumentcatcode");
		final int instrumentNameCode = (Integer) inputMap.get("ninstrumentnamecode");
		final int calibrationRequired = (Integer) inputMap.get("ncalibrationreq");

		requestContext.setUserInfo(userInfo);
		return jobAllocationService.getInstrumentIdBasedCategory(instrumentCatCode, instrumentNameCode,
				calibrationRequired, userInfo);
	}

	@PostMapping(path = "/getUsersBasedTechnique")
	public ResponseEntity<Object> getUsersBasedTechnique(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final int techniqueCode = (Integer) inputMap.get("ntechniquecode");
		final String sectionCode = (String) inputMap.get("ssectioncode");
		final int regTypeCode = (Integer) inputMap.get("nregtypecode");
		final int regSubTypeCode = (Integer) inputMap.get("nregsubtypecode");
		requestContext.setUserInfo(userInfo);
		return jobAllocationService.getUsersBasedTechnique(techniqueCode, sectionCode, regTypeCode, regSubTypeCode,
				userInfo);
	}

	@PostMapping(path = "/AllotJobCreate")
	public ResponseEntity<Object> AllotJobCreate(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return jobAllocationService.AllotJobCreate(inputMap, userInfo);
	}

	@PostMapping(path = "/AllotAnotherUserCreate")
	public ResponseEntity<Object> AllotAnotherUserCreate(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return jobAllocationService.AllotAnotherUserCreate(inputMap, userInfo);
	}

	@PostMapping(path = "/RescheduleCreate")
	public ResponseEntity<Object> RescheduleCreate(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return jobAllocationService.RescheduleCreate(inputMap, userInfo);
	}

	@PostMapping(path = "/viewAnalystCalendar")
	public ResponseEntity<Object> viewAnalystCalendar(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return jobAllocationService.viewAnalystCalendar(inputMap, userInfo);
	}

	@PostMapping(path = "/getUserScheduleCombo")
	public ResponseEntity<Object> getUserScheduleCombo(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return jobAllocationService.getUserScheduleCombo(inputMap, userInfo);
	}

	@PostMapping(path = "/cancelTest")
	public ResponseEntity<Object> cancelTest(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return jobAllocationService.cancelTest(inputMap, userInfo);
	}

	@PostMapping(path = "/getAnalystCalendarBasedOnUser")
	public ResponseEntity<Object> getAnalystCalendarBasedOnUser(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return jobAllocationService.getAnalystCalendarBasedOnUser(inputMap, userInfo);
	}

	@PostMapping(path = "/getInstrumentBasedCategoryForSchedule")
	public ResponseEntity<Object> getInstrumentBasedCategoryForSchedule(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final int instrumentCatCode = (Integer) inputMap.get("ninstrumentcatcode");
		final int ninstrumentnamecode = (Integer) inputMap.get("ninstrumentnamecode");
		requestContext.setUserInfo(userInfo);
		return jobAllocationService.getInstrumentBasedCategoryForSchedule(instrumentCatCode, ninstrumentnamecode,
				userInfo);
	}

	@PostMapping(path = "/getAnalystCalendarBasedOnUserWithDate")
	public ResponseEntity<Object> getAnalystCalendarBasedOnUserWithDate(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return jobAllocationService.getAnalystCalendarBasedOnUserWithDate(inputMap, userInfo);
	}

	@PostMapping(path = "/AllotJobCreateCalendar")
	public ResponseEntity<Object> AllotJobCalendarCreate(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return jobAllocationService.AllotJobCalendarCreate(inputMap, userInfo);

	}

	@PostMapping(value = "getRescheduleEdit")
	public ResponseEntity<Object> getRescheduleEdit(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final String npreregno = (String) inputMap.get("npreregno");
		final String ntransactionsamplecode = (String) inputMap.get("ntransactionsamplecode");
		final String ntransactiontestcode = (String) inputMap.get("transactiontestcode");
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		requestContext.setUserInfo(userInfo);
		return jobAllocationService.getRescheduleEdit(npreregno, ntransactionsamplecode, ntransactiontestcode, userInfo,
				inputMap);
	}

	@PostMapping(path = "/calenderProperties")
	public ResponseEntity<Object> calenderProperties(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return jobAllocationService.calenderProperties(inputMap, userInfo);
	}

	@PostMapping(path = "/getInstrumentNameBasedCategoryForSchedule")
	public ResponseEntity<Object> getInstrumentNameBasedCategoryForSchedule(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final int instrumentCatCode = (Integer) inputMap.get("ninstrumentcatcode");
		requestContext.setUserInfo(userInfo);
		return jobAllocationService.getInstrumentNameBasedCategoryForSchedule(instrumentCatCode, userInfo);
	}

	@PostMapping(path = "/getSectionChange")
	public ResponseEntity<Object> getSectionChange(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return jobAllocationService.getSectionChange(inputMap, userInfo);
	}

	@PostMapping(path = "/updateSectionJobAllocation")
	public ResponseEntity<Object> updateSectionJobAllocation(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return jobAllocationService.updateSectionJobAllocation(inputMap, userInfo);
	}

	@PostMapping(path = "/getUsersBySection")
	public ResponseEntity<Object> getUsersBySection(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return jobAllocationService.getUsersBySection(inputMap, userInfo);
	}

	@PostMapping(path = "/getJobAllocationFilter")
	public ResponseEntity<Object> getJobAllocationFilter(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return jobAllocationService.getJobAllocationFilter(inputMap, userInfo);

	}

	@PostMapping(value = "/createFilterName")
	public ResponseEntity<Object> createFilterName(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return jobAllocationService.createFilterName(inputMap, userInfo);
	}

	@PostMapping(value = "/getFilterName")
	public ResponseEntity<Object> getFilterName(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		List<FilterName> outputMap = jobAllocationService.getFilterName(userInfo);
		requestContext.setUserInfo(userInfo);
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);

	}
}
