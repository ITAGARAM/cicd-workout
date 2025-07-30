package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.basemaster.service.calendarproperties.CalenderPropertiesService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller is used to dispatch the input request to its relevant service
 * methods to access the calenderproperties Service methods.
 */
@RestController
@RequestMapping("/calenderproperties")
public class CalenderPropertiesController {

	private static final Logger LOGGER = LoggerFactory.getLogger(CalenderPropertiesController.class);
	
	private final CalenderPropertiesService calenderpropertiesService;
	private RequestContext requestContext;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext            RequestContext to hold the request
	 * @param calenderpropertiesService calenderpropertiesService
	 */

	public CalenderPropertiesController(CalenderPropertiesService calenderpropertiesService,
			RequestContext requestContext) {
		super();
		this.calenderpropertiesService = calenderpropertiesService;
		this.requestContext = requestContext;
	}

	@PostMapping(path = "/getCalenderProperties")
	public ResponseEntity<Object> getCalenderProperties(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		LOGGER.info("getCalenderProperties called");
		return calenderpropertiesService.getCalenderProperties(inputMap, userInfo);

	}

	@PostMapping(path = "/updateCalenderProperties")
	public ResponseEntity<Object> updateCalenderProperties(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return calenderpropertiesService.updateCalenderProperties(inputMap, userInfo);

	}

}
