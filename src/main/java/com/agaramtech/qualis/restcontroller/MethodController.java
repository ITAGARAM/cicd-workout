package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testmanagement.model.Method;
import com.agaramtech.qualis.testmanagement.model.MethodValidity;
import com.agaramtech.qualis.testmanagement.service.method.MethodService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant method to access
 * the MethodController Service methods.
 * @author ATE180
 *@version 9.0.0.1
 *@since 1-July-2020
 */

@RestController
@RequestMapping("/method")
public class MethodController {
	

	private RequestContext requestContext;
	private final MethodService methodService;	
	
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param methodService MethodService
	 */
	public MethodController(RequestContext requestContext, MethodService methodService) {
		super();
		this.requestContext = requestContext;
		this.methodService = methodService;
	}
	
	/**
	 * This method is used to retrieve list of active method for the specified site.
	 * @param inputMap  [Map] map object with "nsitecode" as key for which the list is to be fetched
	 * @return response object with list of active  method that are to be listed for the specified site
	 */
	@PostMapping(value = "/getMethod")
	public ResponseEntity<Object> getMethod(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		Integer nmethodcode = null;
		
		if (inputMap.get("nmethodcode") != null) {
			nmethodcode = (Integer) inputMap.get("nmethodcode");	
		}
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return methodService.getMethod(nmethodcode,userInfo);
		 
	}
	
//	@PostMapping(value = "/getMethodValidity")
//	public ResponseEntity<Object> getMethodValidity(@RequestBody Map<String, Object> inputMap) throws Exception {
//
//		final ObjectMapper objMapper = new ObjectMapper();
//		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
//		requestContext.setUserInfo(userInfo);
//		//final int siteCode = (Integer) inputMap.get("nsitecode");
//		return methodService.getMethodValidity(userInfo);
//		 
//	}
	
	/**
	 * This method is used to add new  method for the specified Site.
	 * @param inputMap [Map] object with keys of method entity.
	 * @return response entity of newly added method entity
	 */
	@PostMapping(value = "/createMethod")
	public ResponseEntity<Object> createMethod(@RequestBody Map<String, Object> inputMap)throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final Method method = objmapper.convertValue(inputMap.get("method"), Method.class);
		final MethodValidity methodval = objmapper.convertValue(inputMap.get("methodvalidity"), MethodValidity.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return methodService.createMethod(method,methodval,userInfo);
		
	}
	
	@PostMapping(value = "/createMethodValidity")
	public ResponseEntity<Object> createMethodValidity(@RequestBody Map<String, Object> inputMap)throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final MethodValidity methodvalidity = objmapper.convertValue(inputMap.get("methodvalidity"), MethodValidity.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return methodService.createMethodValidity(methodvalidity, userInfo);
		
	}

	/**
	 * This method is used to update  method for the specified Site.
	 * @param inputMap [Map] object with keys of method entity.
	 * @return response entity of updated method entity
	 */
	@PostMapping(value = "/updateMethod")
	public ResponseEntity<Object> updateMethod(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final Method method = objmapper.convertValue(inputMap.get("method"), Method.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final MethodValidity methodval = objmapper.convertValue(inputMap.get("methodvalidity"), MethodValidity.class);
		requestContext.setUserInfo(userInfo);
		return methodService.updateMethod(method,methodval,userInfo);
		
	}
	
	@PostMapping(value = "/updateMethodValidity")
	public ResponseEntity<Object> updateMethodValidity(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final MethodValidity methodvalidity = objmapper.convertValue(inputMap.get("methodvalidity"), MethodValidity.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return methodService.updateMethodValidity(methodvalidity, userInfo);
		
	}
	
	/**
	 * This method is used to retrieve selected active method detail.
	 * @param inputMap  [Map] map object with "nmethodcode" and "userInfo" as keys for which the data is to be fetched
	 * @return response object with selected active method
	 */
	@PostMapping(value = "/getActiveMethodById")
	public ResponseEntity<Object> getActiveMethodById(@RequestBody Map<String, Object> inputMap)throws Exception  {

		final ObjectMapper objmapper = new ObjectMapper();
		final int nmethodcode = (Integer) inputMap.get("nmethodcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return methodService.getActiveMethodById(nmethodcode, userInfo);
		
	}
	
	@PostMapping(value = "/getActiveMethodValidityById")
	public ResponseEntity<Object> getActiveMethodValidityById(@RequestBody Map<String, Object> inputMap)throws Exception  {

		final ObjectMapper objmapper = new ObjectMapper();
		final int nmethodvaliditycode = (Integer) inputMap.get("nmethodvaliditycode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return methodService.getActiveMethodValidityById(nmethodvaliditycode, userInfo);
		
	}
	
	/**
	 * This method is used to delete  method for the specified Site.
	 * @param mapObject [Map] object with keys of method entity.
	 * @return response entity of deleted method entity
	 */
	@PostMapping(value = "/deleteMethod")
	public ResponseEntity<Object> deleteMethod(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final Method method = objmapper.convertValue(inputMap.get("method"), Method.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return methodService.deleteMethod(method,userInfo);
		
	}

	@PostMapping(value = "/deleteMethodValidity")
	public ResponseEntity<Object> deleteMethodValidity(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final MethodValidity methodvalidity = objmapper.convertValue(inputMap.get("methodvalidity"), MethodValidity.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return methodService.deleteMethodValidity(methodvalidity, userInfo);
		
	}
	
	@PostMapping(value = "/approveMethodValidity")
	public ResponseEntity<Object> approveMethodValidity(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final MethodValidity methodvalidity = objmapper.convertValue(inputMap.get("methodvalidity"), MethodValidity.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return methodService.approveMethodValidity(methodvalidity, userInfo);
		
	}
	
}
