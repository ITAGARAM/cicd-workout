package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.compentencemanagement.service.reschedulelog.ReScheduleLogService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory; 
@RestController
@RequestMapping("/reschedulelog")
public class ReScheduleLogController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ReScheduleLogController.class); 
	private final ReScheduleLogService reschedulelogService;
	private  RequestContext requestContext;

	public ReScheduleLogController(ReScheduleLogService reschedulelogService, RequestContext requestContext) {
		super();
		this.reschedulelogService = reschedulelogService;
		this.requestContext = requestContext;
	}

	@PostMapping(value = "/getReScheduleLog")
	public ResponseEntity<Object> getReScheduleLog(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		String fromDate = "";
		String toDate = "";
		if (inputMap.get("fromDate") != null) {
			fromDate = (String) inputMap.get("fromDate");
		}
		if (inputMap.get("toDate") != null) {
			toDate = (String) inputMap.get("toDate");
		}
		final String currentUIDate = (String) inputMap.get("currentdate");
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("getReScheduleLog() called");
		requestContext.setUserInfo(userInfo);
		return reschedulelogService.getReScheduleLog(fromDate, toDate, currentUIDate, userInfo);
	}

}
