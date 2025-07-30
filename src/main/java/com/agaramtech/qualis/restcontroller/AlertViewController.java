package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.alertview.service.AlertViewService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the alertView Service methods.
 * 
 * @author ATE090
 * @version
 * @since 19- Feb- 2021
 */
@RestController
@RequestMapping("/alertview")
public class AlertViewController {

	private static final Log LOGGER = LogFactory.getLog(AlertViewController.class);

	private RequestContext requestContext;
	private final AlertViewService alertViewService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext   RequestContext to hold the request
	 * @param alertViewService AlertViewService
	 */
	public AlertViewController(RequestContext requestContext, AlertViewService alertViewService) {
		super();
		this.requestContext = requestContext;
		this.alertViewService = alertViewService;
	}

	@PostMapping(value = "/getAlertView")
	public ResponseEntity<Object> getAlertView(@RequestBody Map<String, Object> inputMap) throws Exception {
		LOGGER.info("getAlertView");
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return alertViewService.getAlertView(userInfo);

	}

	@PostMapping(value = "/getSelectedAlertView")
	public ResponseEntity<Map<String, Object>> getSelectedAlertView(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int nsqlQueryCode = (Integer) inputMap.get("nsqlquerycode");
		requestContext.setUserInfo(userInfo);
		return alertViewService.getSelectedAlertView(userInfo, nsqlQueryCode);

	}

	@PostMapping(value = "/getAlerts")
	public ResponseEntity<Object> getAlerts(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return alertViewService.getAlerts(userInfo);

	}

	@PostMapping(value = "/getSelectedAlert")
	public ResponseEntity<Object> getSelectedAlert(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int nsqlQueryCode = (Integer) inputMap.get("nsqlquerycode");
		requestContext.setUserInfo(userInfo);
		return alertViewService.getSelectedAlert(userInfo, nsqlQueryCode);
	}
}