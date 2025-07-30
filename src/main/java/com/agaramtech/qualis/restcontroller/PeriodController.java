package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.basemaster.service.period.PeriodService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/period")
public class PeriodController {

	
	private final PeriodService periodService;
	private RequestContext requestContext;
	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param periodService PeriodService
	 */
	public PeriodController(RequestContext requestContext, PeriodService periodService) {
		super();
		this.requestContext = requestContext;
		this.periodService = periodService;
	}
	/**
	 * This method is used to get period
	 * @param 
	 * @return response entity
	 */
	@PostMapping(value = "/getPeriodByControl")
	public ResponseEntity<Object> getPeriodByControl(@RequestBody Map<String, Object> inputMap)  throws Exception {
			ObjectMapper objMapper = new ObjectMapper();
			Integer ncontrolCode=null;
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			if(inputMap.get("ncontrolcode")!=null) {
				ncontrolCode=(Integer) inputMap.get("ncontrolcode");
			}
			return periodService.getPeriodByControl(ncontrolCode,userInfo);
	}
	
}
