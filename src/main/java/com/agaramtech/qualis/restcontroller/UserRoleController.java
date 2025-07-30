package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.credential.model.UserRole;
import com.agaramtech.qualis.credential.model.Users;
import com.agaramtech.qualis.credential.service.userrole.UserRoleService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@RestController
@RequestMapping("/userrole")
public class UserRoleController {

	private static final Log LOGGER = LogFactory.getLog(UserRoleController.class);

	private RequestContext requestContext;
	private final UserRoleService userRoleService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext  RequestContext to hold the request
	 * @param userRoleService UserRoleService
	 */

	public UserRoleController(RequestContext requestContext, UserRoleService userRoleService) {
		super();
		this.requestContext = requestContext;
		this.userRoleService = userRoleService;
	}

	/**
	 * This Method is used to get the over all userrole with respect to site
	 * 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 
	 * @return a response entity which holds the list of userrole with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getUserRole")
	public ResponseEntity<Object> getUserRole(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		LOGGER.info("At Get Userrole Controller");
		return userRoleService.getUserRole(userInfo.getNmastersitecode());
	}

	/**
	 * This method is used to add a new entry to userrole table.
	 * 
	 * @param inputMap map object holding params ( 
	 * 								userrole [UserRole]  object holding details to be added in userrole table,
	 * 								userinfo [UserInfo] holding logged in user details
	 *                              ) 
	 *                           Input:{
									    "userrole": { "suserrolename": "ur-01", "sdescription": "aaaa" },
									    "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -   1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 33,"nmastersitecode": -1,"nmodulecode": 1,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",  "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}
									}
	 * @return ResponseEntity with string message as 'Already Exists' if the userrole already exists/ 
	 * 			list of userrole along with the newly added userrole.
	 * @throws Exception
	 */
	@PostMapping(value = "/createUserRole")
	public ResponseEntity<Object> createUserRole(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserRole userRole = objmapper.convertValue(inputMap.get("userrole"), UserRole.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return userRoleService.createUserRole(userRole, userInfo);
	}

	/**
	 * This method is used to get the single record in userrole table
	 * 
	 * @param inputMap  [Map] map object with "nuserrolecode" and "userinfo" as keys for which the data is to be fetched
	 *                  Input:{ "nuserrolecode": 1,
							     "userinfo":{
							                "activelanguagelist": ["en-US"],
							                "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							                "nformcode": 33,"nmastersitecode": -1,"nmodulecode": 1,
							                "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							                "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							                "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							                "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	  * @return ResponseEntity with UserRole object for the specified primary key / with string message as
	 * 						'Deleted' if the userrole record is not available
	 * @throws Exception
	 */
	@PostMapping(value = "/getActiveUserRoleById")
	public ResponseEntity<Object> getActiveUserRoleById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nuserRoleCode = (Integer) inputMap.get("nuserrolecode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return userRoleService.getActiveUserRoleById(nuserRoleCode, userInfo);

	}

	/**
	 * This method is used to update entry in userrole table.
	 * 
	 * @param inputMap [map object holding params(
	 * 					userrole [UserRole]  object holding details to be updated in userrole table,
	 * 								userinfo [UserInfo] holding logged in user details) 
	 * 					Input:{
     						"userrole": {"nuserrolecode":1,"suserrolename": "ur-0001","sdescription": "bbbb"},
    						"userinfo":{
                						"activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,  "nformcode": 33,"nmastersitecode": -1, "nmodulecode": 1,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,  "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US","slanguagename": "English",
 										"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
  										"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "", "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already Deleted' if the userrole record is not available/ 
	 * 			list of all userrole and along with the updated userrole.
	 * @throws Exception
	 */
	@PostMapping(value = "/updateUserRole")
	public ResponseEntity<Object> updateUserRole(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final UserRole userRole = objmapper.convertValue(inputMap.get("userrole"), UserRole.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return userRoleService.updateUserRole(userRole, userInfo);
	}

	/**
	 * This method id used to delete an entry in userrole table
	 * 
	 * @param inputMap [Map] object with keys of userrole entity and UserInfo object.
	 * 					Input:{
     						"userrole": {"nuserrolecode":1},
    						"userinfo":{
                						"activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,  "nformcode": 33,"nmastersitecode": -1, "nmodulecode": 1,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,  "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US","slanguagename": "English",
 										"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
  										"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "", "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the userrole record is not available/ 
	 * 			string message as 'Record is used in....' when the userrole is associated in transaction /
	 * 			list of all userroles excluding the deleted record 
	 * @throws Exception
	 */
	@PostMapping(value = "/deleteUserRole")
	public ResponseEntity<Object> deleteUserRole(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final UserRole userRole = objmapper.convertValue(inputMap.get("userrole"), UserRole.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return userRoleService.deleteUserRole(userRole, userInfo);
	}

	/**
	 * This Method is used to get the over all userrole with respect to site
	 * 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 
	 * @param objUsers [Users] holding User Object
     * @param nuserSiteCode [int] primarykey of userssite table
     * @param nuserMultiRoleCode [int] primarykey of usermultirole table
     * @param nsiteCode [int] primarykey of site table
	 * @return a response entity which holds the list of userrole with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getUserRoleComboData")
	public ResponseEntity<Object> getUserRoleComboData(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final Users objUsers = objmapper.convertValue(inputMap.get("users"), Users.class);
		final int nuserSiteCode = (Integer) inputMap.get("nusersitecode");
		final int nuserMultiRoleCode = (Integer) inputMap.get("nusermultirolecode");
		final int nsiteCode = (Integer) inputMap.get("nsitecode");
		requestContext.setUserInfo(userInfo);
		return userRoleService.getUserRoleComboData(objUsers, nuserSiteCode, nuserMultiRoleCode, nsiteCode, userInfo);
	}

	/**
	 * This Method is used to get the over all userrole with respect to site
	 * 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 
	 * @param objUsers [Users] holding User Object
     * @param nuserSiteCode [int] primarykey of userssite table
     * @param nuserMultiRoleCode [int] primarykey of usermultirole table
     * @param nsiteCode [int] primarykey of site table
	 * @return a response entity which holds the list of userrole with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getInternalUserRoleComboData")
	public ResponseEntity<Object> getInternalUserRoleComboData(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int nsiteCode = (Integer) inputMap.get("nsitecode");
		requestContext.setUserInfo(userInfo);
		return userRoleService.getUserRoleComboData(null, 0, 0, nsiteCode, userInfo);
	}

}
