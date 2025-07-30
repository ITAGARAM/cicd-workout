package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.exception.service.ExceptionLogService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/jsonexceptionlogs")
public class JsonExceptionLogsController {
	
	private RequestContext requestContext;
	private final ExceptionLogService exceptionLogService;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext RequestContext to hold the request
	 * @param exceptionLogService CountryService
	 */
	public JsonExceptionLogsController(RequestContext requestContext, ExceptionLogService exceptionLogService) {
		super();
		this.requestContext = requestContext;
		this.exceptionLogService = exceptionLogService;
	}	
	
	
	@PostMapping(value = "/getJsonExceptionLogs", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getJsonExceptionLogs(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		final ObjectMapper objMapper = new ObjectMapper();
		String fromDate = "";
		String toDate="";
		
		if (inputMap.get("fromDate") != null) {
			fromDate = (String) inputMap.get("fromDate");
		}
		if (inputMap.get("toDate") != null) {
			toDate = (String) inputMap.get("toDate");
		}
		final String currentUIDate = (String)inputMap.get("currentdate");
		
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		
		return exceptionLogService.getJsonExceptionLogs(fromDate, toDate, currentUIDate, userInfo);

	}
	
	@PostMapping(value = "/getJsonExceptionLogsDetails", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getJsonExceptionLogsDetails(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		
		int njsonexceptionCode = (int) inputMap.get("njsonexceptioncode");
		requestContext.setUserInfo(userInfo);
		
		return exceptionLogService.getJsonExceptionLogsDetails(njsonexceptionCode , userInfo);

	}
	
	
}