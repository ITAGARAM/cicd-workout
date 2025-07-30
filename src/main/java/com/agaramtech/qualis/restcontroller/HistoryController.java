package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.transactionhistory.service.HistoryService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping(value="history")
public class HistoryController {

	private static final Log LOGGER = LogFactory.getLog(HistoryController.class);
	
	
	private final HistoryService historyService;
	private RequestContext requestContext; 
	
	public HistoryController(RequestContext requestContext, HistoryService historyService) {
		super();
		this.requestContext = requestContext;
		this.historyService = historyService;
	}
	

	@RequestMapping(value = "/getTestHistory", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Object>> getTestHistory(@RequestBody Map<String, Object> inputMap ) throws Exception {
	
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			final String ntransactiontestcode =  (String) inputMap.get("ntransactiontestcode");
			requestContext.setUserInfo(userInfo);
			return historyService.getTestHistory(ntransactiontestcode, userInfo);
			
	}
	
//	@RequestMapping(value = "/getSampleHistory", method = RequestMethod.POST)
//	public ResponseEntity<Object> getSampleHistory(@RequestBody Map<String, Object> inputMap ) throws Exception {
//			
//			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
//			final String npreregno =  (String) inputMap.get("npreregno");
//			requestContext.setUserInfo(userInfo);
//			return historyService.getSampleHistory(npreregno, userInfo);
//			
//	}
//	
//	@RequestMapping(value = "/getSubSampleHistory", method = RequestMethod.POST)
//	public ResponseEntity<Object> getSubSampleHistory(@RequestBody Map<String, Object> inputMap ) throws Exception {
//	
//			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
//			final String ntransactionsamplecode =  (String) inputMap.get("ntransactionsamplecode");
//			requestContext.setUserInfo(userInfo);
//			return historyService.getSubSampleHistory(ntransactionsamplecode, userInfo);
//			
//	}
	
}
