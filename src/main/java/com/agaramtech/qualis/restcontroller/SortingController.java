package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.configuration.service.sorting.SortingService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the Sorting Service methods.
 */
@RestController
@RequestMapping("/sorting")
public class SortingController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SortingController.class);
	
	private final SortingService sortingService;
	private RequestContext requestContext;

	public SortingController(SortingService sortingService, RequestContext requestContext) {
		super();
		this.sortingService = sortingService;
		this.requestContext = requestContext;
	}

	/**
	 * This Method is used to get the over all forms and modules with respect to
	 * language
	 * 
	 * @param inputMap [Map] contains key getSlanguagetypecode which holds the value
	 *                 of respective language type code
	 * @return a response entity which holds the list of forms and modules with
	 *         respect to language and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getSorting")
	public ResponseEntity<Object> getSorting(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		int boolValue = 1;
		if (inputMap.get("boolValue") != null) {
			boolValue = (Integer) inputMap.get("boolValue");
		}
		requestContext.setUserInfo(userInfo);
		LOGGER.info("getSorting called");
		return sortingService.getSorting(userInfo, boolValue);
	}

	@PostMapping(value = "/getFilter")
	public ResponseEntity<Object> getFilter(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final int nmenuCode = (Integer) inputMap.get("nmenucode");
		int boolValue = 1;
		if (inputMap.get("boolValue") != null) {
			boolValue = (Integer) inputMap.get("boolValue");
		}
		requestContext.setUserInfo(userInfo);
		return sortingService.getFilter(userInfo, nmenuCode, boolValue);
	}

	@PostMapping(value = "/getFilter1")
	public ResponseEntity<Object> getFilter1(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final int nmenuCode = (Integer) inputMap.get("nmenucode");
		final int nmoduleCode = (Integer) inputMap.get("nmodulecode");
		int boolValue = 1;
		if (inputMap.get("boolValue") != null) {
			boolValue = (Integer) inputMap.get("boolValue");
		}
		requestContext.setUserInfo(userInfo);
		return sortingService.getFilter1(userInfo, nmenuCode, nmoduleCode, boolValue);
	}

	@PostMapping(value = "/updateForms")
	public ResponseEntity<Object> updateForms(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sortingService.updateForms(userInfo, inputMap);
	}

	@PostMapping(value = "/updateModules")
	public ResponseEntity<Object> updateModules(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sortingService.updateModules(userInfo, inputMap);
	}
}
