package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.credential.model.UserMultiDeputy;
import com.agaramtech.qualis.credential.model.UserMultiRole;
import com.agaramtech.qualis.credential.model.Users;
import com.agaramtech.qualis.credential.model.UsersSite;
import com.agaramtech.qualis.credential.service.usermultideputy.UserMultiDeputyService;
import com.agaramtech.qualis.credential.service.users.UsersService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the Users Service, UserMultiRole and UserMultiDeputy service
 * methods.
 */
@RestController
@RequestMapping("/users")
public class UsersController {

	private static final Logger LOGGER = LoggerFactory.getLogger(UsersController.class);

	@Autowired
	private RequestContext requestContext;
	private final UsersService usersService;
	private final UserMultiDeputyService userMultiDeputyService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext         RequestContext to hold the request
	 * @param usersService           UsersService
	 * @param userMultiDeputyService UserMultiDeputyService
	 */
	public UsersController(RequestContext requestContext, UsersService usersService,
			UserMultiDeputyService userMultiDeputyService) {
		super();
		this.requestContext = requestContext;
		this.usersService = usersService;
		this.userMultiDeputyService = userMultiDeputyService;
	}

	/**
	 * This method is used to retrieve list of all/specified active user(s) and
	 * their associated details(user sites, user's multi roles, userfiles and deputy
	 * users assigned for the user). If nusercode = null, fetches entire user list
	 * else fetches specified user and their associated details
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of
	 *         all/specified active user(s) details
	 * @throws Exception exception exception
	 */
	@PostMapping(value = "/getUsers")
	public ResponseEntity<Object> getUsers(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		Integer nuserCode = null;
		Integer nFlag = null;
		if (inputMap.get("nusercode") != null) {
			nuserCode = (Integer) inputMap.get("nusercode");
		}
		if (inputMap.get("nflag") != null) {
			nFlag = (Integer) inputMap.get("nFlag");
		}
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		LOGGER.info("At getUsers Controller");
		return usersService.getUsers(nuserCode, nFlag, userInfo);
	}

	/**
	 * This method is used to retrieve selected active users detail.
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return ResponseEntity with Users object for the specified primary key / with
	 *         string message as ' User Already Deleted' if the user record is not
	 *         active s
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveUsersById")
	public ResponseEntity<Object> getActiveUsersById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nuserCode = (Integer) inputMap.get("nusercode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return usersService.getActiveUsersById(nuserCode, userInfo);
	}

	/**
	 * This method is used to delete selected user.
	 * 
	 * @param inputMap [Map] object with keys of Users entity with key "users":
	 *                 {"nusercode": 29,"ndeptcode": 2,"ncountrycode":
	 *                 4,"ndesignationcode": -1,"nlockmode": 6,"nlogintypecode":
	 *                 1,"ntransactionstatus": 1,"sloginid": "bp","sfirstname":
	 *                 "Prasanna","slastname": "kumar B","saddress1":
	 *                 "cheennai","semail":
	 *                 "prasannakumar.b@agaramtech.com","sphoneno":
	 *                 "98765432","saddress2": "","saddress3": "","sbloodgroup":
	 *                 "","squalification": "","ddateofjoin":
	 *                 null,"sjobdescription": "","smobileno": "","nsitecode":
	 *                 -1,"nstatus": 1,"sempid": "","dmodifieddate":
	 *                 1741850615,"ntranssitecode": 0,"sdeptname":
	 *                 "Biological","sdesignationname": "NA","scountryname":
	 *                 "Australia","slockstatus": "Unlocked","stransstatus":
	 *                 null,"logintype": null,"susername": "Prasanna kumar
	 *                 B","sactivestatus": "Active","ssignimgname":
	 *                 "sgn.jpg","ssignimgftp":
	 *                 "0f55f400-3948-4a14-b245-122abf450c28.jpg","suserimgname":
	 *                 "","suserimgftp": null,"sdateofjoin":
	 *                 null,"ncontactusercode": 0,"scontactusername":
	 *                 null,"nusersitecode": 0 }and UserInfo object.
	 * @return ResponseEntity with string message as 'User already deleted' if the
	 *         user record is not active/ string message as 'Cannot Delete ADS User'
	 *         if it is ADS User / string message as 'Record is used in....' when
	 *         the user is associated in transaction (Product/Goodsin/Deputy) / list
	 *         of all active users and its associated details excluding the deleted
	 *         record
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteUsers")
	public ResponseEntity<Object> deleteUsers(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final Users users = objmapper.convertValue(inputMap.get("users"), Users.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return usersService.deleteUsers(users, userInfo);
	}

	/**
	 * This method is used to add new userrole for the selected 'user'.
	 * 
	 * @param inputMap [Map] object with keys of 'Users', 'UserMultiRole' and
	 *                 'UserInfo' entities.
	 * @return ResponseEntity with string message as 'User already deleted' if the
	 *         user record is not active/ string message as 'Already Exists' if the
	 *         role has been already assigned to the user / list of all active roles
	 *         for the selected user along with the newly associated role
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createUserMultiRole")
	public ResponseEntity<Object> createUserMultiRole(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final Users users = objmapper.convertValue(inputMap.get("users"), Users.class);
		final UserMultiRole userMultiRole = objmapper.convertValue(inputMap.get("usermultirole"), UserMultiRole.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return usersService.createUserMultiRole(userMultiRole, userInfo, users);

	}

	/**
	 * This method is used to retrieve selected active UserMultiRole detail.
	 * 
	 * @param inputMap [Map] map object with "nusermultirolecode" and "userinfo" as
	 *                 keys for which the data is to be fetched
	 * @return ResponseEntity with UserMultiRole object for the specified primary
	 *         key / with string message as 'Already Deleted' if the record is not
	 *         active
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveUserMultiRoleById")
	public ResponseEntity<Object> getActiveUserMultiRoleById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nuserMultiRoleCode = (Integer) inputMap.get("nusermultirolecode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return usersService.getActiveUserMultiRoleById(nuserMultiRoleCode, userInfo);
	}

	/**
	 * This method is used to update usermultirole for the specified UsersSite.
	 * 
	 * @param inputMap [Map] object with keys of Users,UserInfo, UserMutiRole
	 *                 object.
	 * @return ResponseEntity with string message as 'User already deleted' if the
	 *         user/role record is not active/ string message as 'Already Exists' if
	 *         the role has been already assigned to the user / string message as
	 *         'Inactive Default Role' if default role is made inactive/ list of all
	 *         active roles for the selected user along with the newly associated
	 *         role
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateUserMultiRole")
	public ResponseEntity<Object> updateUserMultiRole(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final Users users = objmapper.convertValue(inputMap.get("users"), Users.class);
		final UserMultiRole userMultiRole = objmapper.convertValue(inputMap.get("usermultirole"), UserMultiRole.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return usersService.updateUserMultiRole(userMultiRole, userInfo, users);
	}

	/**
	 * This method is used to delete usermultirole for the specified UsersSite.
	 * 
	 * @param inputMap [Map] object with keys of Users, UserInfo, UserMultirole
	 *                 object.
	 * @return ResponseEntity with string message as 'User already deleted' if the
	 *         user record is not active/ string message as 'Cannot Delete Default
	 *         Role' if it is default role / string message as 'Record is used
	 *         in....' when the role is associated in transaction (Deputy) / list of
	 *         all active roles for the user excluding the deleted record
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteUserMultiRole")
	public ResponseEntity<Object> deleteUserMultiRole(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final Users users = objmapper.convertValue(inputMap.get("users"), Users.class);
		final UserMultiRole userMultiRole = objmapper.convertValue(inputMap.get("usermultirole"), UserMultiRole.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return usersService.deleteUserMultiRole(userMultiRole, userInfo, users);
	}

	/**
	 * This method is used to add new deputy user for the selected 'user'.
	 * 
	 * @param inputMap [Map] object with keys of 'Users', 'UserMultiDeputy' and
	 *                 'UserInfo' entities.
	 * @return ResponseEntity with string message as 'Already Exists' if the same
	 *         user assigned as deputy for the same role/ string message "User
	 *         Already Retired" if the user is retired/ list of all active deputy
	 *         users for the specified usersite along with the newly added deputy
	 *         details.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createUserMultiDeputy")
	public ResponseEntity<Object> createUserMultiDeputy(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final Users users = objmapper.convertValue(inputMap.get("users"), Users.class);
		final UserMultiDeputy userMultiRole = objmapper.convertValue(inputMap.get("usermultideputy"),
				UserMultiDeputy.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return userMultiDeputyService.createUserMultiDeputy(userMultiRole, userInfo, users);
	}

	/**
	 * This method is used to retrieve selected active UserMultiDeputy detail.
	 * 
	 * @param inputMap [Map] map object with "nusermultideputycode" and "userinfo"
	 *                 as keys for which the data is to be fetched
	 * @return ResponseEntity with UserMultiDeputy object for the specified primary
	 *         key / with string message as 'Already Deleted' if the record is not
	 *         active
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveUserMultiDeputyById")
	public ResponseEntity<Object> getActiveUserMultiDeputyById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nuserMultiDeputyCode = (Integer) inputMap.get("nusermultideputycode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return userMultiDeputyService.getActiveUserMultiDeputyById(nuserMultiDeputyCode, userInfo);
	}

	/**
	 * This method is used to update usermultideputy for the specified UsersSite.
	 * 
	 * @param inputMap [Map] object with keys of Users,UserInfo, UserMutiDeputy
	 *                 object.
	 * @return ResponseEntity with string message as 'Already Exists' if the same
	 *         user assigned as deputy for the same role/ string message "User
	 *         Already Retired" if the user is retired/ list of all active deputy
	 *         users for the specified usersite along with the updated deputy
	 *         details.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateUserMultiDeputy")
	public ResponseEntity<Object> updateUserMultiDeputy(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final Users users = objmapper.convertValue(inputMap.get("users"), Users.class);
		final UserMultiDeputy userMultiDeputy = objmapper.convertValue(inputMap.get("usermultideputy"),
				UserMultiDeputy.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return userMultiDeputyService.updateUserMultiDeputy(userMultiDeputy, userInfo, users);
	}

	/**
	 * This method is used to delete usermultideputy for the specified UsersSite.
	 * 
	 * @param inputMap [Map] object with keys of Users, UserInfo, UserMultiDeputy
	 *                 object.
	 * @return ResponseEntity with string message as 'Deputy User already deleted'
	 *         if the record is not active/ string message as 'Already Retired' if
	 *         the user is retired / list of all active deputy users excluding the
	 *         deleted record
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteUserMultiDeputy")
	public ResponseEntity<Object> deleteUserMultiDeputy(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final Users users = objmapper.convertValue(inputMap.get("users"), Users.class);
		final UserMultiDeputy userMultiDeputy = objmapper.convertValue(inputMap.get("usermultideputy"),
				UserMultiDeputy.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return userMultiDeputyService.deleteUserMultiDeputy(userMultiDeputy, userInfo, users);
	}

	/**
	 * This method is used to retrieve combo details(UsersSite, UserRole) for
	 * UserMultiScreen.
	 * 
	 * @param inputMap [Map] object with keys of nusersitecode, nmastersitecode,
	 *                 nusercode.
	 * @return response object holding details of deputy users and roles of the
	 *         selected user.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getComboDataforUserMultiDeputy")
	public ResponseEntity<Object> getComboDataforUserMultiDeputy(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final int nuserSiteCode = (Integer) inputMap.get("nusersitecode");
		final int masterSiteCode = (Integer) inputMap.get("nmastersitecode");
		final int nuserCode = (Integer) inputMap.get("nusercode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return userMultiDeputyService.getComboDataforUserMultiDeputy(nuserSiteCode, masterSiteCode, nuserCode,
				userInfo);
	}

	/**
	 * This method is used to retire selected user.
	 * 
	 * @param inputMap [Map] object with keys of "Users", "UserInfo" objects
	 * @return ResponseEntity with string message as 'User already deleted' if the
	 *         user record is not active/ string message as 'Cannot Retire ADS User'
	 *         if it is ADS User / list of all active users and its associated
	 *         details along with the currently updated status
	 * @throws Exception exception
	 */
	@PostMapping(value = "/retireUsers")
	public ResponseEntity<Object> retireUser(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final Users users = objmapper.convertValue(inputMap.get("users"), Users.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return usersService.retireUser(users, userInfo);
	}

	/**
	 * This method is used to reset password for the selected active default role of
	 * the user.
	 * 
	 * @param inputMap [Map] object with keys of "nusersitecode","Users" and
	 *                 "UserInfo" objects.
	 * @return ResponseEntity with string message as 'User already deleted' if the
	 *         user record is not active/ string message as 'Cannot Reset Password
	 *         for ADS User' if it is ADS User / string message as 'Password Reset
	 *         done successfully' for successful password reset.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/resetPassword")
	public ResponseEntity<Object> resetPassword(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final int userSiteCode = (Integer) inputMap.get("nusersitecode");
		final Users users = objmapper.convertValue(inputMap.get("users"), Users.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return usersService.resetPassword(userSiteCode, users, userInfo);
	}

	/**
	 * This method is used to fetch list of user roles and deputy users assigned for
	 * the selected user of the specified site.
	 * 
	 * @param inputMap [Map] object with keys of "nusersitecode" and "UserInfo"
	 *                 objects.
	 * @return ResponseEntity with string message as 'UserSite already deleted' if
	 *         the usersite record is not active/ list of user roles and deputy
	 *         users assigned for the selected user of the specified site.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getRoleDeputyByUserSite")
	public ResponseEntity<Object> getRoleDeputyByUserSite(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int userSiteCode = (Integer) inputMap.get("nusersitecode");
		requestContext.setUserInfo(userInfo);
		return usersService.getRoleDeputyByUserSite(userSiteCode, userInfo);
	}

	/**
	 * This method is used to fetch list of unassigned sites for the selected user.
	 * 
	 * @param inputMap [Map] object with keys of "nusersitecode" and "Users"
	 *                 objects.
	 * @return ResponseEntity with list of sites.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getUnAssignedSiteListByUser")
	public ResponseEntity<Object> getUnAssignedSiteListByUser(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final Users users = objmapper.convertValue(inputMap.get("users"), Users.class);
		final int nuserSiteCode = (Integer) inputMap.get("nusersitecode");
		requestContext.setUserInfo(userInfo);
		return usersService.getUnAssignedSiteListByUser(users, nuserSiteCode, userInfo);
	}

	/**
	 * This method is used to associate site for the user.
	 * 
	 * @param inputMap [Map] object with keys of "UsersSite" and "UserInfo" objects.
	 * @return ResponseEntity with string message as 'User already deleted' if the
	 *         user record is not active/ string message as 'Already Exists' if the
	 *         site has been already assigned to the user / selected user's details
	 *         along with the newly associated site
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createUsersSite")
	public ResponseEntity<Object> createUsersSite(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final UsersSite usersSite = objmapper.convertValue(inputMap.get("userssite"), UsersSite.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return usersService.createUsersSite(usersSite, userInfo);
	}

	/**
	 * This method is used to retrieve active usersite object based on the specified
	 * primary key.
	 * 
	 * @param inputMap [Map] object with keys of "nusersitecode" and "UserInfo"
	 *                 objects.
	 * @return ResponseEntity with UsersSite object for the specified primary key /
	 *         with string message as 'Already Deleted' if the record is not active
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveUsersSiteById")
	public ResponseEntity<Object> getActiveUsersSiteById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int nuserSiteCode = (Integer) inputMap.get("nusersitecode");
		requestContext.setUserInfo(userInfo);
		return usersService.getActiveUsersSiteById(nuserSiteCode, userInfo);
	}

	/**
	 * This method is used to update site for the specified active user.
	 * 
	 * @param inputMap [Map] object with keys of "UsersSite" and "UserInfo" objects.
	 * @return ResponseEntity with string message as 'User already deleted' if the
	 *         user/role record is not active/ string message as 'Already Exists' if
	 *         the site has been already assigned to the user / string message as
	 *         'Default cannot be changed if default site is made inactive/ list of
	 *         user roles and deputy users assigned for the selected user of the
	 *         updated site
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateUsersSite")
	public ResponseEntity<? extends Object> updateUsersSite(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final UsersSite userSite = objmapper.convertValue(inputMap.get("userssite"), UsersSite.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return usersService.updateUsersSite(userSite, userInfo);
	}

	/**
	 * This method is used to delete entry in userssite.
	 * 
	 * @param inputMap [Map] object with keys of "Users", "UsersSite" and "UserInfo"
	 *                 objects.
	 * @return ResponseEntity with string message as 'User already deleted' if the
	 *         user record is not active/ string message as 'Cannot Delete Default
	 *         Site' if it is default site / string message as 'Record is used
	 *         in....' when the usersite is associated in transaction
	 *         (UserMultiRole) / list of all active users and their active child
	 *         details
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteUsersSite")
	public ResponseEntity<Object> deleteUsersSite(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final Users users = objmapper.convertValue(inputMap.get("users"), Users.class);
		final UsersSite userSite = objmapper.convertValue(inputMap.get("userssite"), UsersSite.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return usersService.deleteUsersSite(userSite, userInfo, users);
	}

	/**
	 * This method is used to retrieve list of active users along with department
	 * details to be used in combo load based on the site specified in UserInfo
	 * object.
	 * 
	 * @param inputMap [Map] object with keys of "UserInfo" object.
	 * @return list of active users for the specified site
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getUserWithDeptForCombo")
	public ResponseEntity<Object> getUserWithDeptForCombo(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return usersService.getUserWithDeptForCombo(userInfo);
	}

	/**
	 * This method is used to create a new user
	 * 
	 * @param request [MultipartHttpServletRequest] request object holding params(
	 *                users [Users] object holding details to be added in users
	 *                table, userMultiRole [UserMultiRole] object holding user's
	 *                default role to be added in 'usermultirole' table. userssite
	 *                [UsersSite] object holding user's default site to be added in
	 *                userssite table, userfile [UserFile] object holding user image
	 *                and signature image) *
	 * @return ResponseEntity with string message as 'Already Exists' if the loginid
	 *         already exists/ list of all active users and along with the newly
	 *         added user and its child details.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createUsers")
	public ResponseEntity<Object> createUsers(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return usersService.createUsers(request, userInfo);
	}

	/**
	 * This method is used to update selected user details.
	 * 
	 * @param request [MultipartHttpServletRequest] request object holding params(
	 *                users [Users] object holding details to be updated in users
	 *                table, userfile [UserFile] object holding user image and
	 *                signature image)
	 * @return ResponseEntity with string message as 'User Already Deleted' if the
	 *         user record is not active/ list of all active users and along with
	 *         the updated user and its child details.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateUsers")
	public ResponseEntity<Object> updateUsers(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return usersService.updateUsers(request, userInfo);
	}

	/**
	 * This method is used to retrieve list of active users along with department
	 * details to be used in combo load based on the site specified in UserInfo
	 * object.
	 * 
	 * @param inputMap [Map] object with keys of "UserInfo" object.
	 * @return list of active users for the specified site
	 * @throws Exception exception
	 */
	@PostMapping(value = "/viewSelectedUserImage")
	public ResponseEntity<Object> viewSelectedUserImage(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final Users users = objMapper.convertValue(inputMap.get("users"), Users.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return usersService.viewSelectedUserImage(users, userInfo);
	}

	/**
	 * Retrieves basic profile picture metadata such as FTP path and file code for the logged-in user.
	 *
	 * @param param A map containing user context.
	 *              Required key: "userinfo" - an object representing UserInfo.
	 * @return HTTP response containing UserFile data or error.
	 * @throws Exception if any error occurs during the process.
	 */
	@PostMapping(value = "/getUserProfilePictureData")
	public ResponseEntity<Object> getUserProfilePictureData(@RequestBody Map<String, Object> param) throws Exception {
		final ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = mapper.convertValue(param.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return usersService.getUserProfilePictureData(userInfo);
	}

	/**
	 * Updates the user's profile picture on the server by uploading a new image via FTP.
	 *
	 * @param request Multipart request containing:
	 *                - "userinfo": JSON string representing UserInfo
	 *                - "filename": Original filename
	 *                - "uniquefilename0": Unique filename used for saving
	 * @return HTTP response indicating success or failure of the upload operation.
	 * @throws Exception if upload or update fails.
	 */
	@PostMapping(value = "/updateUserProfilePicture")
	public ResponseEntity<Object> updateUserProfile(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		return usersService.updateUserProfilePicture(request, userInfo);
	}

	/**
	 * Fetches the full image content of the user's updated profile picture using stored FTP path.
	 *
	 * @param request A map containing user context.
	 *                Required key: "userinfo" - an object representing UserInfo.
	 * @return HTTP response with base64 image content or error.
	 * @throws Exception if any issue occurs while fetching image from FTP.
	 */
	@PostMapping(value = "/getUpdateUserProfilePicture")
	public ResponseEntity<?> getUpdateUserProfilePicture(@RequestBody Map<String, Object> request) throws Exception {
		final ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = mapper.convertValue(request.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return usersService.getUpdateUserProfilePicture(userInfo);
	}

}
