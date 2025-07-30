package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.basemaster.service.timezone.TimeZoneService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller is used to dispatch the input request to its relevant method to access
 * the TimeZone Service methods
  */
@RestController
@RequestMapping("/timezone")
public class TimeZoneController {

	//private static final Logger LOGGER = LoggerFactory.getLogger(TimeZoneController.class);
	
	private RequestContext requestContext;	
	private final TimeZoneService timeZoneService;

	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param timeZoneService TimeZoneService
	 */
	public TimeZoneController(RequestContext requestContext, TimeZoneService timeZoneService) {
		super();
		this.requestContext = requestContext;
		this.timeZoneService = timeZoneService;
	}
	
	@RequestMapping(value="/getTimeZone",method=RequestMethod.POST)
	public ResponseEntity<Object> getTimeZone() throws Exception{		
		return timeZoneService.getTimeZone();
		
	}
	
	@RequestMapping(value="/getLocalTimeByZone",method=RequestMethod.POST)
	public ResponseEntity<Object> getLocalTimeByZone(@RequestBody Map<String, Object> inputMap) throws Exception{		

		final ObjectMapper objMapper=new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			
		requestContext.setUserInfo(userInfo);
			
		return timeZoneService.getLocalTimeByZone(userInfo);		
	}

}
