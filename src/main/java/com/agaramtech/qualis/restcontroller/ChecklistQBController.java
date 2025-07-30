package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.checklist.model.ChecklistQB;
import com.agaramtech.qualis.checklist.service.checklistqb.ChecklistQBService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@RestController
@RequestMapping("/checklistqb")
public class ChecklistQBController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChecklistQBController.class);

	private RequestContext requestContext;
	private final ChecklistQBService checkListqbService;

	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param unitService UnitService
	 */
	public ChecklistQBController(RequestContext requestContext, ChecklistQBService checkListqbService) {
		super();
		this.requestContext = requestContext;
		this.checkListqbService = checkListqbService;
	}

	@PostMapping(value = "/getChecklistQB")
	public ResponseEntity<Object> getChecklistQB(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		LOGGER.info("getChecklistQB Controller -->"+userInfo);
		return checkListqbService.getChecklistQB(userInfo);
	}

	@PostMapping(value = "/createChecklistQB")
	public ResponseEntity<Object> createChecklistQB(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper objMapper = new ObjectMapper();
		final ChecklistQB objChecklistQB = objMapper.convertValue(inputMap.get("checklistqb"),
				new TypeReference<ChecklistQB>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return checkListqbService.createChecklistQB(objChecklistQB, userInfo);
	}

	@PostMapping(value = "/updateChecklistQB")
	public ResponseEntity<Object> updateQBCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final ChecklistQB objChecklistQB = objMapper.convertValue(inputMap.get("checklistqb"),
				new TypeReference<ChecklistQB>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return checkListqbService.updateChecklistQB(objChecklistQB, userInfo);
	}

	@PostMapping(value = "/deleteChecklistQB")
	public ResponseEntity<Object> deleteChecklistQB(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final ChecklistQB objChecklistQB = objMapper.convertValue(inputMap.get("checklistqb"),
				new TypeReference<ChecklistQB>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return checkListqbService.deleteChecklistQB(objChecklistQB, userInfo);
	}

	@PostMapping(value = "/getActiveChecklistQBById")
	public ResponseEntity<Object> getActiveChecklistQBById(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper objMapper = new ObjectMapper();
		final int nchecklistQBCode = (int) inputMap.get("nchecklistqbcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return checkListqbService.getActiveChecklistQBById(nchecklistQBCode, userInfo);
	}

	@PostMapping(value = "/getAddEditData")
	public ResponseEntity<Object> getAddEditData(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return checkListqbService.getAddEditData(userInfo);
	}

}
