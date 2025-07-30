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
import com.agaramtech.qualis.configuration.service.modulesorting.ModuleSortingService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/modulesorting")
public class ModuleSortingController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ModuleSortingController.class);

	private final ModuleSortingService moduleSortingService;
	private RequestContext requestContext;

	public ModuleSortingController(ModuleSortingService moduleSortingService, RequestContext requestContext) {
		super();
		this.moduleSortingService = moduleSortingService;
		this.requestContext = requestContext;
	}

	@PostMapping(value = "/getModuleSorting")
	public ResponseEntity<Object> getModuleSorting(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		LOGGER.info("getModuleSorting called");
		return moduleSortingService.getModuleSorting(userInfo);
	}

	@PostMapping(value = "/getActiveModuleSortingById")
	public ResponseEntity<Map<String, Object>> getActiveModuleSortingById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		int nformCode = (Integer) inputMap.get("nformcode");
		requestContext.setUserInfo(userInfo);
		return moduleSortingService.getActiveModuleSortingById(userInfo, nformCode);
	}

	@PostMapping(value = "/updateModuleSorting")
	public ResponseEntity<Object> updateModuleSorting(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		int nformCode = (Integer) inputMap.get("nformcode");
		int nmoduleCode = (Integer) inputMap.get("nmodulecode");
		int nmenuCode = (Integer) inputMap.get("nmenucode");
		requestContext.setUserInfo(userInfo);
		return moduleSortingService.updateModuleSorting(userInfo, nformCode, nmoduleCode, nmenuCode);
	}
}
