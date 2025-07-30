package com.agaramtech.qualis.scheduler.service;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.scheduler.model.ScheduleMaster;
import com.agaramtech.qualis.scheduler.model.ScheduleMasterMonthly;
import com.agaramtech.qualis.scheduler.model.ScheduleMasterWeekly;


public interface SchedulerService {

	public ResponseEntity<Object> getScheduler( Integer nscheduleCode, final UserInfo userInfo,String sscheduleType) throws Exception;
	public ResponseEntity<Object> getActiveSchedulerById(final int nscheduleCode, final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getSchedulerType(final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getSchedulerTypeRecurring(final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getSchedulerRecurringMonthlyPeriod(final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> createScheduler(ScheduleMaster scheduler,ScheduleMasterWeekly schedulerweek,ScheduleMasterMonthly schedulermonth, final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> updateScheduler(ScheduleMaster scheduler,ScheduleMasterWeekly schedulerweek,ScheduleMasterMonthly schedulermonth, final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> deleteScheduler(ScheduleMaster scheduler,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> approveScheduler(ScheduleMaster scheduler,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getSchedulerByScheduleType( Integer nscheduleTypeCode, final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getGraphicalScheduler(final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> createGraphicalScheduler(Map<String, Object> inputMap) throws Exception;
	public ResponseEntity<Object> updateGraphicalScheduler(Map<String, Object> inputMap) throws Exception;
	public ResponseEntity<Object> deleteGraphicalScheduler(Map<String, Object> inputMap) throws Exception;
	public ResponseEntity<Object> getGraphicalSchedulerView(Integer nscheduleCode,final UserInfo userInfo,String sscheduleType) throws Exception;
	public ResponseEntity<Object> getGraphicalSchedulerByScheduleType(String sscheduleType, final UserInfo userInfo) throws Exception;
}
