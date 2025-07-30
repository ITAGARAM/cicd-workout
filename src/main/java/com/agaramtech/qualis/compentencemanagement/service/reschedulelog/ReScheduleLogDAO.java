package com.agaramtech.qualis.compentencemanagement.service.reschedulelog;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;

public interface ReScheduleLogDAO {
	ResponseEntity<Object> getReScheduleLog(String fromDate, String toDate, String currentUIDate, UserInfo userInfo)
			throws Exception;

}
