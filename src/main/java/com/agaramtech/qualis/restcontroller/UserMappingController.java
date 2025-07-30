package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.organization.service.usermapping.UserMappingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the UserMapping Service methods
 * 
 * @author ATE169
 * @version 9.0.0.1
 *
 */
@RestController
@RequestMapping("/usermapping")
public class UserMappingController {

	private RequestContext requestContext;
	private UserMappingService userMappingService;

	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param UserMappingService userMappingService
	 */
	public UserMappingController(RequestContext requestContext, UserMappingService userMappingService) {
		super();
		this.requestContext = requestContext;
		this.userMappingService = userMappingService;
	}

	/**
	 * This method is used to get Initial and filter change get for usermapping
	 * screen along with filter data
	 * 
	 * @param inputMap contains values of
	 *                 flag,approvalsubtypecode,regtypecode,regsubtypecode and
	 *                 userroletemplateversioncode
	 * @return ResponseEntity of site and user mapping Data with Http Status
	 */
	@PostMapping(value = "/getUserMapping")
	public ResponseEntity<Object> getUserMapping(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		
		return userMappingService.getUserMapping(inputMap);
	}

	/**
	 * This method is used to get for usermapping screen with respect to site
	 * 
	 * @param inputMap contains values of sitecode and userroletemplateversioncode
	 *                 and userInfo
	 * @return ResponseEntity of user mapping Data with Http Status
	 */
	@PostMapping(value = "/getUserMappingBySite")
	public ResponseEntity<Object> getUserMappingBySite(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		int nSiteCode = (int) inputMap.get("nsitecode");
		int nVersionCode = (int) inputMap.get("nversioncode");
		requestContext.setUserInfo(userInfo);
		
		return userMappingService.getUserRoles(nSiteCode, nVersionCode);

	}

	/**
	 * This method is used to get child usermapping according to the parent users
	 * 
	 * @param inputMap contains values of
	 *                 nparusermappingcode,nuserrolecode,levelno,sitecode and
	 *                 userroletemplateversioncode and userInfo
	 * @return ResponseEntity of user mapping Data with Http Status
	 */

	@PostMapping(value = "/getChildUserMapping")
	public ResponseEntity<Object> getChildUserMapping(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		int lastParentUsercode = (int) inputMap.get("nparusermappingcode");
		int nroleCode = (int) inputMap.get("nuserrolecode");
		int nSiteCode = (int) inputMap.get("nsitecode");
		int nVersionCode = (int) inputMap.get("nversioncode");
		int levelno = (int) inputMap.get("levelno");

		requestContext.setUserInfo(userInfo);
		return userMappingService.getChildUsers(lastParentUsercode, nroleCode, nSiteCode, nVersionCode, levelno);

	}

	/**
	 * This Method is used to get all available users to be added to a parent user
	 * 
	 * @param inputMap
	 * @return ResponseEntity of List Users Data to be added with Http Status
	 */
	@PostMapping(value = "/getAvailableUsers")
	public ResponseEntity<Object> getAvailableUsers(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		
		return userMappingService.getAvailableUsers(inputMap, userInfo);

	}

	/**
	 * This Method is used to add users to a parent user in usermapping table
	 * 
	 * @param inputMap List of users to be added and user info
	 * @return ResponseEntity of List Users Data added with Http Status
	 */
	@SuppressWarnings("unchecked")
	@PostMapping(value = "/createUsers")
	public ResponseEntity<Object> addUsers(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();

		objMapper.registerModule(new JavaTimeModule());
		return userMappingService.addUsers((Map<String, Object>) inputMap.get("users"));

	}

	/**
	 * This Method is used to delete a user to a parent user in usermapping table
	 * 
	 * @param inputMap A user data to be deleted and user info
	 * @return ResponseEntity of List Users Data with Http Status
	 */
	@SuppressWarnings("unchecked")
	@PostMapping(value = "/deleteUsers")
	public ResponseEntity<Object> deleteUsers(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return userMappingService.deleteUsers((Map<String, Object>) inputMap.get("users"), userInfo);

	}

	@PostMapping(value = "/getUserMappingTree")
	public ResponseEntity<Object> getUserMappingTree(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		
		requestContext.setUserInfo(userInfo);
		return userMappingService.getUserMappingTree(inputMap);

	}

	@PostMapping(value = "/getUserMappingCopy")
	public ResponseEntity<Object> getUserMappingCopy(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper mapper = new ObjectMapper();
		int nmasterSiteCode = (int) inputMap.get("nsitecode");
		int nsubTypeCode = (int) inputMap.get("nsubtypecode");
		int nregSubTypeCode = (int) inputMap.get("nregsubtypecode");
		UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return userMappingService.getUserMappingCopy(nsubTypeCode, nregSubTypeCode, nmasterSiteCode, userInfo);

	}

	@PostMapping(value = "/getCopyRegSubType")
	public ResponseEntity<Object> getCopyRegSubType(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper mapper = new ObjectMapper();
		int nmasterSiteCode = (int) inputMap.get("nsitecode");
		int nregTypeCode = (int) inputMap.get("nregtypecode");
		int nregSubTypeCode = (int) inputMap.get("nregsubtypecode");
		UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return userMappingService.getCopyRegSubType(nregTypeCode, nregSubTypeCode, nmasterSiteCode, userInfo);

	}

	@SuppressWarnings("unchecked")
	@PostMapping(value = "/copyUserMapping")
	public ResponseEntity<Object> copyUserMapping(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return userMappingService.copyUserMapping((Map<String, Object>) inputMap.get("usermapping"));

	}
}
