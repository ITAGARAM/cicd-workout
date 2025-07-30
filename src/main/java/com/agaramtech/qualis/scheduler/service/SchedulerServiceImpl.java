package com.agaramtech.qualis.scheduler.service;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.scheduler.model.ScheduleMaster;
import com.agaramtech.qualis.scheduler.model.ScheduleMasterMonthly;
import com.agaramtech.qualis.scheduler.model.ScheduleMasterWeekly;

@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class SchedulerServiceImpl implements SchedulerService{
	
	
	
	private final SchedulerDAO scheduleDAO;
	private final CommonFunction commonFunction;
	
	public SchedulerServiceImpl(SchedulerDAO scheduleDAO, CommonFunction commonFunction) {
		this.scheduleDAO = scheduleDAO;
		this.commonFunction = commonFunction;
	}
	
	
@Override
public ResponseEntity<Object> getScheduler(Integer nscheduleCode, final UserInfo userInfo,String sscheduleType) throws Exception{
	return scheduleDAO.getScheduler(nscheduleCode, userInfo,sscheduleType);
}

@Override
public ResponseEntity<Object> getActiveSchedulerById(final int nscheduleCode, final UserInfo userInfo) throws Exception {
	final ScheduleMaster scheduler = (ScheduleMaster) scheduleDAO.getActiveSchedulerById(nscheduleCode, userInfo);
	if (scheduler == null) {
		return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
	}else {
		return new ResponseEntity<>(scheduler, HttpStatus.OK);
	}
}

public ResponseEntity<Object> getSchedulerType(final UserInfo userInfo) throws Exception {
	return scheduleDAO.getSchedulerType(userInfo);
}

public ResponseEntity<Object> getSchedulerTypeRecurring(final UserInfo userInfo) throws Exception {
	return scheduleDAO.getSchedulerTypeRecurring(userInfo);
}

public ResponseEntity<Object> getSchedulerRecurringMonthlyPeriod(final UserInfo userInfo) throws Exception {
	return scheduleDAO.getSchedulerRecurringMonthlyPeriod(userInfo);
}

public ResponseEntity<Object> getGraphicalScheduler(final UserInfo userInfo) throws Exception {
	return scheduleDAO.getGraphicalScheduler(userInfo);
}

@Transactional
@Override
public ResponseEntity<Object> createScheduler(ScheduleMaster scheduler,ScheduleMasterWeekly schedulerweek,ScheduleMasterMonthly schedulermonth, final UserInfo userInfo) throws Exception {
	return scheduleDAO.createScheduler(scheduler,schedulerweek,schedulermonth, userInfo);
}

@Transactional
@Override
public ResponseEntity<Object> updateScheduler(ScheduleMaster scheduler,ScheduleMasterWeekly schedulerweek,ScheduleMasterMonthly schedulermonth, final UserInfo userInfo) throws Exception {
	return scheduleDAO.updateScheduler(scheduler,schedulerweek,schedulermonth, userInfo);
}

@Transactional
@Override
public ResponseEntity<Object> deleteScheduler(ScheduleMaster scheduler, final UserInfo userInfo) throws Exception {
	return scheduleDAO.deleteScheduler(scheduler, userInfo);
}

@Transactional
@Override
public ResponseEntity<Object> approveScheduler(ScheduleMaster scheduler, final UserInfo userInfo) throws Exception {
	return scheduleDAO.approveScheduler(scheduler, userInfo);
}

@Override
public ResponseEntity<Object> getSchedulerByScheduleType(Integer nscheduleTypeCode, final UserInfo userInfo) throws Exception{
	return scheduleDAO.getSchedulerByScheduleType(nscheduleTypeCode, userInfo);
}

@Transactional
@Override
public ResponseEntity<Object> createGraphicalScheduler(Map<String, Object> inputMap) throws Exception {
	return scheduleDAO.createGraphicalScheduler(inputMap);
}

@Transactional
@Override
public ResponseEntity<Object> updateGraphicalScheduler(Map<String, Object> inputMap) throws Exception {
	return scheduleDAO.updateGraphicalScheduler(inputMap);
}

@Transactional
@Override
public ResponseEntity<Object> deleteGraphicalScheduler(Map<String, Object> inputMap) throws Exception {
	return scheduleDAO.deleteGraphicalScheduler(inputMap);
}

@Override
public ResponseEntity<Object> getGraphicalSchedulerView(Integer nscheduleCode,final UserInfo userInfo,String sscheduleType) throws Exception{
	return scheduleDAO.getGraphicalSchedulerView(nscheduleCode,userInfo,sscheduleType);
}

@Override
public ResponseEntity<Object> getGraphicalSchedulerByScheduleType(String sscheduleType, final UserInfo userInfo) throws Exception{
	return scheduleDAO.getGraphicalSchedulerByScheduleType(sscheduleType, userInfo);
}

}
