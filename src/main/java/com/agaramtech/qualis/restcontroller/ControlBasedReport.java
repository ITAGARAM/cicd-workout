package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.reports.service.controlbasedreport.ControlBasedReportService;
import com.fasterxml.jackson.databind.ObjectMapper;



@RestController
@RequestMapping("/controlbasedreport")
public class ControlBasedReport {
	
	private final ControlBasedReportService  controlbasedreportservice;
	private RequestContext requestContext;
	
	public ControlBasedReport(RequestContext requestContext, ControlBasedReportService controlbasedreportservice) {
		super();
		this.requestContext = requestContext;
		this.controlbasedreportservice = controlbasedreportservice;
	}
		
		
		@RequestMapping(value = "/controlBasedReport", method = RequestMethod.POST)
		public ResponseEntity<Object>generateControlBasedReport(@RequestBody Map<String, Object> inputMap)throws Exception{
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			requestContext.setUserInfo(userInfo);
			return controlbasedreportservice.generateControlBasedReport(inputMap,userInfo);	 
		}
		
		
		@RequestMapping(value = "/controlBasedReportParameter", method = RequestMethod.POST)
		public ResponseEntity<Object>controlBasedReportParameter(@RequestBody Map<String, Object> inputMap)throws Exception{
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			requestContext.setUserInfo(userInfo);
			return controlbasedreportservice.controlBasedReportParameter(inputMap,userInfo);	 
		}
		
		
		@RequestMapping(value = "/controlBasedReportparametretable", method = RequestMethod.POST)
		public ResponseEntity<Object>controlBasedReportparametretable(@RequestBody Map<String, Object> inputMap)throws Exception{
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			requestContext.setUserInfo(userInfo);
			return controlbasedreportservice.controlBasedReportparametretable(inputMap,userInfo);	 
		}
		
//		@RequestMapping(value = "/controlBasedReportparametretablecolumn", method = RequestMethod.POST)
//		public ResponseEntity<Object>controlBasedReportparametretablecolumn(@RequestBody Map<String, Object> inputMap)throws Exception{
//			final ObjectMapper objMapper = new ObjectMapper();
//			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
//			requestContext.setUserInfo(userInfo);
//			return controlbasedreportservice.controlBasedReportparametretablecolumn(inputMap,userInfo);	 
//		}	
//
//		
//		@RequestMapping(value = "/createControlBasedReportparametreInsert", method = RequestMethod.POST)
//		public ResponseEntity<Object>downloadControlBasedReportparametreInsert(@RequestBody Map<String, Object> inputMap)throws Exception{
//			final ObjectMapper objMapper = new ObjectMapper();
//			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
//			requestContext.setUserInfo(userInfo);
//			return controlbasedreportservice.downloadControlBasedReportparametreInsert(inputMap,userInfo);	 
//		}	
			 
}
