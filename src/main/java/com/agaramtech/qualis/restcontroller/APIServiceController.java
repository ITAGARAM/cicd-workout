package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.configuration.service.apiservice.APIInterfaceService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping(value="/apiservice")
public class APIServiceController {

	private static final Logger LOGGER = LoggerFactory.getLogger(APIServiceController.class);

	private RequestContext requestContext;
	private final APIInterfaceService apiInterfaceService;
	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 *
	 * @param requestContext RequestContext to hold the request
	 * @param APIInterfaceService apiInterfaceService
	 */
	public APIServiceController(RequestContext requestContext, APIInterfaceService apiInterfaceService) {
		super();
		this.requestContext = requestContext;
		this.apiInterfaceService = apiInterfaceService;
	}

	@PostMapping(value="/getAPIService")
	public ResponseEntity<Object> getAPIService(@RequestBody Map<String, Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		LOGGER.info("Get Request ->"+userInfo);
		return apiInterfaceService.getAPIService(inputMap,userInfo);
	}

	@PostMapping(value="/getDynamicMaster")
	public ResponseEntity<Object> getDynamicMaster(@RequestBody Map<String, Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return apiInterfaceService.getDynamicMaster(inputMap,userInfo);
	}

	@PostMapping(value="/getStaticMaster")
	public ResponseEntity<Object> getStaticMaster(@RequestBody Map<String, Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return apiInterfaceService.getStaticMaster(inputMap,userInfo);
	}

	@PostMapping(value="/getQualisFormFields")
	public ResponseEntity<Object> getQualisFormFields(@RequestBody Map<String, Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return apiInterfaceService.getQualisFormFields(inputMap,userInfo);
	}

	@PostMapping(value="/getQualisForms")
	public ResponseEntity<Object> getQualisForms(@RequestBody Map<String, Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return apiInterfaceService.getQualisForms(inputMap,userInfo);
	}
	@PostMapping(value="/getCustomQuery")
	public ResponseEntity<Object> getCustomQuery(@RequestBody Map<String, Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return apiInterfaceService.getCustomQuery(inputMap,userInfo);
	}
	@PostMapping(value="/getCustomQueryName")
	public ResponseEntity<Object> getCustomQueryName(@RequestBody Map<String, Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return apiInterfaceService.getCustomQueryName(inputMap,userInfo);
	}
	@PostMapping(value = "/getSQLQueryData")
	public ResponseEntity<Object> getSQLQueryData(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		final String sQuery = (String) inputMap.get("query");
		requestContext.setUserInfo(userInfo);
		return apiInterfaceService.getSQLQueryData(inputMap, sQuery, userInfo);

	}

}
