package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.compentencemanagement.model.TrainingCategory;
import com.agaramtech.qualis.compentencemanagement.service.trainingcategory.TrainingCategoryService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@RestController
@RequestMapping("/traningcategory")
public class TrainingCategoryController {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrainingCategoryController.class);

	private final RequestContext requestContext;
	private final TrainingCategoryService TrainingCategoryService;

	public TrainingCategoryController(RequestContext requestContext,
			com.agaramtech.qualis.compentencemanagement.service.trainingcategory.TrainingCategoryService trainingCategoryService) {
		super();
		this.requestContext = requestContext;
		TrainingCategoryService = trainingCategoryService;
	}

	@PostMapping(value = "/getTrainingCategory")
	public ResponseEntity<Object> getTrainingCategory(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("getTrainingCategory() called");
		requestContext.setUserInfo(userInfo);
		return TrainingCategoryService.getTrainingCategory(userInfo);

	}

	@PostMapping(value = "/createTrainingCategory")
	public ResponseEntity<Object> createTrainingCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		TrainingCategory objTrainingCategory = objMapper.convertValue(inputMap.get("trainingcategory"),
				new TypeReference<TrainingCategory>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return TrainingCategoryService.createTrainingCategory(objTrainingCategory, userInfo);
	}

	@PostMapping(value = "/getActiveTrainingCategoryById")
	public ResponseEntity<Object> getActiveTrainingCategoryById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		int ntrainingcategorycode = (int) inputMap.get("ntrainingcategorycode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return TrainingCategoryService.getActiveTrainingCategoryById(ntrainingcategorycode, userInfo);
	}

	@PostMapping(value = "/updateTrainingCategory")
	public ResponseEntity<Object> updateTrainingCategory(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		TrainingCategory objTrainingCategory = objMapper.convertValue(inputMap.get("trainingcategory"),
				new TypeReference<TrainingCategory>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return TrainingCategoryService.updateTrainingCategory(objTrainingCategory, userInfo);

	}

	@PostMapping(value = "/deleteTrainingCategory")
	public ResponseEntity<Object> deleteTrainingCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		TrainingCategory objTrainingCategory = objMapper.convertValue(inputMap.get("trainingcategory"),
				new TypeReference<TrainingCategory>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return TrainingCategoryService.deleteTrainingCategory(objTrainingCategory, userInfo);
	}
}
