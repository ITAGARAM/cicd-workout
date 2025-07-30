package com.agaramtech.qualis.exception.service;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.exception.model.JsonExceptionLogs;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation on 'jsonexceptionlogs'
 * table through its implementation class.
 */
public interface ExceptionLogDAO {
	
	public String createExceptionHandleRecord(final JsonExceptionLogs jsonExceptionLogs, final UserInfo userInfo)
			throws Exception;
	
	public void deleteExceptionLogs() throws Exception;
	
	public ResponseEntity<Object> getJsonExceptionLogs(String fromDate, String toDate, String currentUIDate,  UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getJsonExceptionLogsDetails(Integer njsonexceptionCode,  UserInfo userInfo) throws Exception;


}
