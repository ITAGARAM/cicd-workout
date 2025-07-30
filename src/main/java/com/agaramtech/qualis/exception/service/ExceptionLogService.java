package com.agaramtech.qualis.exception.service;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.exception.model.JsonExceptionLogs;
import com.agaramtech.qualis.global.UserInfo;

public interface ExceptionLogService {

	public String createExceptionHandleRecord (final JsonExceptionLogs jsonExceptionLogs, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getJsonExceptionLogs(String fromDate, String toDate, String currentUIDate,  UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getJsonExceptionLogsDetails(Integer njsonexceptionCode,  UserInfo userInfo) throws Exception;

}
