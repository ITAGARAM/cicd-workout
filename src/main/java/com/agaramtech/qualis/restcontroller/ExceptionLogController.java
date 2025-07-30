package com.agaramtech.qualis.restcontroller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.agaramtech.qualis.exception.model.JsonExceptionLogs;
import com.agaramtech.qualis.exception.service.ExceptionLogService;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.UserInfo;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class ExceptionLogController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionLogController.class);

	private final ExceptionLogService exceptionLogService;
	private RequestContext requestContext;
	private final ProjectDAOSupport projectDAOSupport;

	public ExceptionLogController(RequestContext requestContext, ExceptionLogService exceptionLogService,
			ProjectDAOSupport projectDAOSupport) {
		super();
		this.exceptionLogService = exceptionLogService;
		this.requestContext = requestContext;
		this.projectDAOSupport = projectDAOSupport;
	}

	// Logger logging = null;

//	@ExceptionHandler(value = Exception.class)
//	public ResponseEntity<Object> handleException(Exception ex, HandlerMethod handlerMethod) 
//	{
//		//LOGGER = LoggerFactory.getLogger(handlerMethod.getMethod().getDeclaringClass().getSimpleName());
//
//		handlerMethod.getMethod().getClass().getSimpleName();
//		final UserInfo userInfo = requestContext.getUserInfo();
//
//		final String stackTace = projectDAOSupport.getStackTrace(ex);
//
//		//LOGGER.info("Method Name ======> " + handlerMethod.getMethod().getName());
//		LOGGER.info("Exception Message ======> "+ ex.getStackTrace()[0].getMethodName() + ":" + ex);
//		LOGGER.error("Exception File Name ======> " + ex.getStackTrace()[0].getFileName());
//		LOGGER.error("Exception Line Number ======> " + ex.getStackTrace()[0].getLineNumber());
//		LOGGER.error("Exception Class Name ======> " + ex.getStackTrace()[0].getClassName());
//		LOGGER.error("Exception Method Name ======> " + ex.getStackTrace()[0].getMethodName());
//		LOGGER.error("Error Message ======> " + ex.getStackTrace()[0].getMethodName() + ":"+ ex.toString());
//
//		final JsonExceptionLogs jsonExceptionlogs = new JsonExceptionLogs();
//		jsonExceptionlogs.setSmessage(ex.toString());
//		jsonExceptionlogs.setSstacktrace(stackTace);
//
//		
//		try {
//			exceptionLogService.createExceptionHandleRecord(jsonExceptionlogs, userInfo);
//		} 
//		catch (Exception e) {
//			LOGGER.info(e.toString());
//		}
//		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//	}
	

	@ExceptionHandler(value = Exception.class)
	public ResponseEntity<Object> handleException(Exception ex, HttpServletRequest handlerMethod) {
		// logging = LoggerFactory.getLogger(handlerMethod.getMethod());
		// LOGGER.info("requestContext:"+requestContext);

		handlerMethod.getMethod().getClass().getSimpleName();
		final UserInfo userInfo = requestContext.getUserInfo();

		LOGGER.info("userInfo:" + userInfo);

		final String stackTace = projectDAOSupport.getStackTrace(ex);

		// logging.info("Method Name ======> " + handlerMethod.getMethod());

		// logging.info("getLocalizedMessage ======> " + ex.getLocalizedMessage());
		// logging.info("getMessage ======> " + ex.getMessage());
		// logging.error("Exception File Name ======> " +
		// ex.getStackTrace()[0].getFileName());
		LOGGER.error("Exception Line Number ======> " + ex.getStackTrace()[0].getLineNumber());
		LOGGER.error("Exception Class Name ======> " + ex.getStackTrace()[0].getClassName());
		LOGGER.error("Exception Method Name ======> " + ex.getStackTrace()[0].getMethodName());
		LOGGER.info("Exception Message ======> " + ex);
		// logging.error("Error Message toString ======> " + ex.toString());

		final JsonExceptionLogs jsonExceptionlogs = new JsonExceptionLogs();
		jsonExceptionlogs.setSmessage(ex.toString());
		jsonExceptionlogs.setSstacktrace(stackTace);

		try {
			exceptionLogService.createExceptionHandleRecord(jsonExceptionlogs, userInfo);
		} catch (Exception e) {
			LOGGER.info("Exception in Eception Handler controller:" + e.toString());
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		// return new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
	}

//	public static String getStackTrace(final Throwable throwable) {
//
//		final StringWriter sw = new StringWriter();
//		final PrintWriter pw = new PrintWriter(sw, true);
//		
//		throwable.printStackTrace(pw);
//		String printStringTrace = sw.getBuffer().toString();
//
//
//		return printStringTrace;
//	}
}
