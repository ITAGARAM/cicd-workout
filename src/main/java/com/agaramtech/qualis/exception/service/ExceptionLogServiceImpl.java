package com.agaramtech.qualis.exception.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.exception.model.JsonExceptionLogs;
import com.agaramtech.qualis.global.UserInfo;


@Service
@Transactional(readOnly = true, rollbackFor=Exception.class)
public class ExceptionLogServiceImpl implements ExceptionLogService {	

	private final ExceptionLogDAO exceptionLogDAO;	
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class properties.
	 * @param exceptionLogDAO ExceptionLogDAO Interface
	 */
	public ExceptionLogServiceImpl(ExceptionLogDAO exceptionLogDAO) {
		this.exceptionLogDAO = exceptionLogDAO;
	}

	@Override
	@Transactional
	public String createExceptionHandleRecord(final JsonExceptionLogs jsonExceptionLogs, final UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return exceptionLogDAO.createExceptionHandleRecord(jsonExceptionLogs, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getJsonExceptionLogs(String fromDate, String toDate, String currentUIDate,  UserInfo userInfo) throws Exception {
		return exceptionLogDAO.getJsonExceptionLogs(fromDate,  toDate,  currentUIDate, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getJsonExceptionLogsDetails(Integer njsonexceptionCode,  UserInfo userInfo) throws Exception {
		return exceptionLogDAO.getJsonExceptionLogsDetails(njsonexceptionCode , userInfo);
	}
	

}
