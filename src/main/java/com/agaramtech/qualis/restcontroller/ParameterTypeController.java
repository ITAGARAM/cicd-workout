package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testmanagement.service.parametertype.ParameterTypeService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/parametertype")
public class ParameterTypeController {
	
	private RequestContext requestContext;
	private final ParameterTypeService parameterTypeService;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param storageCategoryService StorageCategoryService
	 */
	public ParameterTypeController(RequestContext requestContext, ParameterTypeService parameterTypeService) {
		super();
		this.requestContext = requestContext;
		this.parameterTypeService = parameterTypeService;
	}
	
	/**
	 * This method is used to retrieve list of available parametertype(s). 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 	
	 * 					Input : {"userinfo":{nmastersitecode": -1}}				
	 * @return response entity  object holding response status and list of all parametertype
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getParameterType")
	public ResponseEntity<Object> getParameterType(@RequestBody Map<String, Object> inputMap)throws Exception{
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(objUserInfo);
		return parameterTypeService.getParameterType(objUserInfo);
		
	}
	
}
