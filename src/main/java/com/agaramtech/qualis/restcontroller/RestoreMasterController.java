package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.archivalandpurging.restoremaster.model.RestoreMaster;
import com.agaramtech.qualis.archivalandpurging.restoremaster.service.RestoreMasterService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the RestoreMaster Service methods
 * 
 * @author ATE113
 * @version 9.0.0.1
 */
@RestController
@RequestMapping("/restoremaster")
public class RestoreMasterController {

	private RequestContext requestContext;
	private final RestoreMasterService restoreMasterService;

	public RestoreMasterController(RequestContext requestContext, RestoreMasterService restoreMasterService) {
		super();
		this.requestContext = requestContext;
		this.restoreMasterService = restoreMasterService;
	}
	
	/**
	 * This Method is used to get the over all restoremaster with respect to site
	 * 
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of restoremaster with respect to site
	 *         and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getRestoreMaster")
	public ResponseEntity<Object> getRestoreMaster(@RequestBody Map<String, Object> inputMap) throws Exception {

	
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return restoreMasterService.getRestoreMaster(userInfo);


	}

	/**
	 * This method is used to add a new entry to Restoremaster table.
	 * 
	 * @param inputMap [Map] holds the restoremaster object to be inserted
	 * @return inserted restoremaster object and HTTP Status on successive insert otherwise
	 *         corresponding HTTP Status
	 * @throws Exception
	 */
	
	@PostMapping(value = "/createRestoreMaster")
	public ResponseEntity<Object> createRestoreMaster(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		final ObjectMapper objMapper = new ObjectMapper();
		RestoreMaster objRestoreMaster = objMapper.convertValue(inputMap.get("restoremaster"), new TypeReference<RestoreMaster>() {
		});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return restoreMasterService.createRestoreMaster(objRestoreMaster, userInfo);

	}

	/**
	 * This method is used to update entry in restoremaster table.
	 * 
	 * @param inputMap [Map] holds the restoremaster object to be updated
	 * @return response entity object holding response status and data of updated
	 *         restoremaster object
	 * @throws Exception
	 */
	
	@PostMapping(value = "/updateRestoreMaster")
	public ResponseEntity<Object> updateRestoreMaster(@RequestBody Map<String, Object> inputMap) throws Exception {

		
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		RestoreMaster objRestoreMaster = objMapper.convertValue(inputMap.get("restoremaster"), new TypeReference<RestoreMaster>() {
		});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return restoreMasterService.updateRestoreMaster(objRestoreMaster, userInfo);


	}

	/**
	 * This method id used to delete an entry in RestoreMaster table
	 * 
	 * @param inputMap [Map] holds the restoremaster object to be deleted
	 * @return response entity object holding response status and data of deleted
	 *         restoremaster object
	 * @throws Exception
	 */
	
	@PostMapping(value = "/deleteRestoreMaster")
	public ResponseEntity<Object> deleteRestoreMaster(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		RestoreMaster objRestoreMaster = objMapper.convertValue(inputMap.get("restoremaster"), new TypeReference<RestoreMaster>() {
		});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return restoreMasterService.deleteRestoreMaster(objRestoreMaster, userInfo);

	}

	/**
	 * This method is used to get the single record in restoremaster table
	 * 
	 * @param inputMap [Map] holds the nrestoremastercode to get
	 * @return response entity object holding response status and data of single
	 *         restoremaster object
	 * @throws Exception
	 */
	
	@PostMapping(value = "/getActiveRestoreMasterById")
	public ResponseEntity<Object> getActiveRestoreMasterById(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		final ObjectMapper objMapper = new ObjectMapper();
		int nrestoremastercode = (int) inputMap.get("nrestoremastercode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return restoreMasterService.getActiveRestoreMasterById(nrestoremastercode, userInfo);


	}
	
	@PostMapping(value = "/getPurgeDate")
	public ResponseEntity<Object> getPurgeDate(@RequestBody Map<String, Object> inputMap) throws Exception{
		
			final ObjectMapper mapper=new ObjectMapper();
			final UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		
			requestContext.setUserInfo(userInfo);
			return restoreMasterService.getPurgeDate(inputMap,userInfo);
		
	}
	
	@PostMapping(value = "/getSite")
	public ResponseEntity<Object> getSite(@RequestBody Map<String, Object> inputMap) throws Exception{
		
			final ObjectMapper mapper=new ObjectMapper();
			final UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		
			requestContext.setUserInfo(userInfo);
			return restoreMasterService.getSite(userInfo);
		
	}
}

