package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.scheduler.model.ScheduleMaster;
import com.agaramtech.qualis.scheduler.model.ScheduleMasterMonthly;
import com.agaramtech.qualis.scheduler.model.ScheduleMasterWeekly;
import com.agaramtech.qualis.scheduler.service.SchedulerService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@RestController
@RequestMapping("/scheduler")
public class SchedulerController {
	
	private RequestContext requestContext;
	private final SchedulerService schedulerService;
	
	public SchedulerController(RequestContext requestContext, SchedulerService schedulerService) {
		super();
		this.requestContext = requestContext;
		this.schedulerService = schedulerService;
	}
	
	
	@PostMapping(value = "/getScheduler")
	public ResponseEntity<Object> getScheduler(@RequestBody Map<String, Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
				
		Integer nscheduleCode = null;
		String sscheduletype="O";
		if (inputMap.get("nschedulecode") != null) {
			nscheduleCode = (Integer) inputMap.get("nschedulecode");	
		}
		if (inputMap.get("sscheduletype") != null) {
			sscheduletype = (String) inputMap.get("sscheduletype");	
		}
		
		requestContext.setUserInfo(userInfo);
		return schedulerService.getScheduler(nscheduleCode, userInfo,sscheduletype);
	}
		
	
	@PostMapping(value = "/getActiveSchedulerById")
	public ResponseEntity<Object> getActiveSchedulerById(@RequestBody Map<String, Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), 
				new TypeReference<UserInfo>() {});
		
		final int nscheduleCode = (Integer) inputMap.get("nschedulecode");
		
		requestContext.setUserInfo(userInfo);
		return schedulerService.getActiveSchedulerById(nscheduleCode, userInfo);
	}

	
	@PostMapping(value = "/getSchedulerType")
	public ResponseEntity<Object> getSchedulerType(@RequestBody Map<String, Object> inputMap) throws Exception{

		final ObjectMapper objMapper = new ObjectMapper();
		
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), 
				new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
			return schedulerService.getSchedulerType(userInfo);
	}
	
	
	@PostMapping(value = "/getSchedulerTypeRecurring")
	public ResponseEntity<Object> getSchedulerTypeRecurring(@RequestBody Map<String, Object> inputMap) throws Exception{

		final ObjectMapper objMapper = new ObjectMapper();
		
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), 
				new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
			return schedulerService.getSchedulerTypeRecurring(userInfo);
	}
	
	
	@PostMapping(value = "/getSchedulerRecurringMonthlyPeriod")
	public ResponseEntity<Object> getSchedulerRecurringMonthlyPeriod(@RequestBody Map<String, Object> inputMap) throws Exception{

		final ObjectMapper objMapper = new ObjectMapper();
		
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), 
				new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
			return schedulerService.getSchedulerRecurringMonthlyPeriod(userInfo);
	}
	
	
	
	  
	  @PostMapping(value = "/createScheduler")
		public ResponseEntity<Object> createScheduler(@RequestBody Map<String, Object> inputMap) throws Exception {
		  final ObjectMapper objMapper = new ObjectMapper();
			
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), 
					new TypeReference<UserInfo>() {});
			
				objMapper.registerModule(new JavaTimeModule());
				
				Map<String, Object> inputMaping=(Map<String, Object>) inputMap.get("scheduleData");

				final ScheduleMaster schmaster = objMapper.convertValue(inputMaping.get("schedulemaster"), new TypeReference<ScheduleMaster>() {});
				final ScheduleMasterWeekly schweekly = objMapper.convertValue(inputMaping.get("schedulemasterweekly"), new TypeReference<ScheduleMasterWeekly>() {});
				final ScheduleMasterMonthly schmonthly = objMapper.convertValue(inputMaping.get("schedulemastermonthly"), new TypeReference<ScheduleMasterMonthly>() {});
				
				requestContext.setUserInfo(userInfo);
				return schedulerService.createScheduler(schmaster, schweekly, schmonthly,  userInfo);
		}
	  
	  
	  @PostMapping(value = "/updateScheduler")
		public ResponseEntity<Object> updateScheduler(@RequestBody Map<String, Object> inputMap) throws Exception {
		  final ObjectMapper objMapper = new ObjectMapper();
			
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), 
					new TypeReference<UserInfo>() {});
			
				objMapper.registerModule(new JavaTimeModule());
				
				Map<String, Object> inputMaping=(Map<String, Object>) inputMap.get("scheduleData");
				final ScheduleMaster schmaster = objMapper.convertValue(inputMaping.get("schedulemaster"), new TypeReference<ScheduleMaster>() {});
				final ScheduleMasterWeekly schweekly = objMapper.convertValue(inputMaping.get("schedulemasterweekly"), new TypeReference<ScheduleMasterWeekly>() {});
				final ScheduleMasterMonthly schmonthly = objMapper.convertValue(inputMaping.get("schedulemastermonthly"), new TypeReference<ScheduleMasterMonthly>() {});
				
				requestContext.setUserInfo(userInfo);
				return schedulerService.updateScheduler(schmaster, schweekly, schmonthly,  userInfo);
		}
	  
	  
	  @PostMapping(value = "/deleteScheduler")
		public ResponseEntity<Object> deleteScheduler(@RequestBody Map<String, Object> inputMap) throws Exception {
		  final ObjectMapper objMapper = new ObjectMapper();
			
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), 
					new TypeReference<UserInfo>() {});
			
				objMapper.registerModule(new JavaTimeModule());
				
				
				final ScheduleMaster schmaster = objMapper.convertValue(inputMap.get("scheduler"), new TypeReference<ScheduleMaster>() {});
				
				
				requestContext.setUserInfo(userInfo);
				return schedulerService.deleteScheduler(schmaster, userInfo);
		}
	  
	  
	  @PostMapping(value = "/approveScheduler")
		public ResponseEntity<Object> approveScheduler(@RequestBody Map<String, Object> inputMap) throws Exception {
		  final ObjectMapper objMapper = new ObjectMapper();
			
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), 
					new TypeReference<UserInfo>() {});
			
				objMapper.registerModule(new JavaTimeModule());
				
				
				final ScheduleMaster schmaster = objMapper.convertValue(inputMap.get("scheduler"), new TypeReference<ScheduleMaster>() {});
								
				requestContext.setUserInfo(userInfo);
				return schedulerService.approveScheduler(schmaster, userInfo);
		}
	  
		
		@PostMapping(value = "/getSchedulerByScheduleType")
		public ResponseEntity<Object> getSchedulerByScheduleType(@RequestBody Map<String, Object> inputMap) throws Exception{
			final ObjectMapper objMapper = new ObjectMapper();		
			Integer nscheduleTypeCode = null;
			if (inputMap.get("nscheduletypecode") != null) {
				nscheduleTypeCode = (Integer) inputMap.get("nscheduletypecode");	
			}
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), 
					new TypeReference<UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return schedulerService.getSchedulerByScheduleType(nscheduleTypeCode, userInfo);
		}
}
