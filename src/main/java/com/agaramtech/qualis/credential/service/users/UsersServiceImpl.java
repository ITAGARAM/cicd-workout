package com.agaramtech.qualis.credential.service.users;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.credential.model.UserMultiRole;
import com.agaramtech.qualis.credential.model.Users;
import com.agaramtech.qualis.credential.model.UsersSite;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'users' table through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class UsersServiceImpl implements UsersService {

	private final UsersDAO usersDAO;
	private final CommonFunction commonFunction;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class properties.
	 * 
	 * @param unitDAO        UnitDAO Interface
	 * @param commonFunction CommonFunction holding common utility functions
	 */
	public UsersServiceImpl(UsersDAO usersDAO, CommonFunction commonFunction) {
		this.usersDAO = usersDAO;
		this.commonFunction = commonFunction;
	}

	/**
	 * Calls DAO to retrieve users based on user code and filter flag.
	 *
	 * @param nuserCode User code for specific user fetch or filtering.
	 * @param nFlag Flag for user status filtering (e.g., active/inactive).
	 * @param userInfo Context containing user language and site information.
	 * @return Response entity containing result list or user object.
	 * @throws Exception if any error occurs during DAO operation.
	 */
	@Override
	public ResponseEntity<Object> getUsers(Integer nuserCode, Integer nFlag, final UserInfo userInfo) throws Exception {
		return usersDAO.getUsers(nuserCode, nFlag, userInfo);
	}

	/**
	 * Retrieves active user details from DAO. Returns multilingual error if the user does not exist or is deleted.
	 *
	 * @param nuserCode Unique identifier of the user.
	 * @param userInfo Context information for retrieving site and language-specific messages.
	 * @return ResponseEntity with user data or an error message indicating the user is deleted.
	 * @throws Exception if DAO lookup fails.
	 */
	@Override
	public ResponseEntity<Object> getActiveUsersById(final int nuserCode, final UserInfo userInfo) throws Exception {
		final Users objUsers = (Users) usersDAO.getActiveUserById(nuserCode, userInfo);
		if (objUsers == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objUsers, HttpStatus.OK);
		}
	}

	/**
	 * Deletes the specified user from the system.
	 * 
	 * @param objUsers the user object to be deleted
	 * @param userInfo the user performing the operation
	 * @return the response entity indicating the result of the deletion
	 * @throws Exception if deletion fails due to validation or database error
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteUsers(Users objUsers, UserInfo userInfo) throws Exception {
		return usersDAO.deleteUsers(objUsers, userInfo);
	}

	/**
	 * Creates a new multi-role mapping for the specified user.
	 * 
	 * @param objUserMultiRole the multi-role data to be created
	 * @param userInfo the user performing the operation
	 * @param objUsers the user associated with the new multi-role
	 * @return the response entity indicating creation status
	 * @throws Exception if creation fails due to validation or processing issues
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createUserMultiRole(UserMultiRole objUserMultiRole, final UserInfo userInfo,
			Users objUsers) throws Exception {
		return usersDAO.createUserMultiRole(objUserMultiRole, userInfo, objUsers);
	}

	/**
	 * Retrieves an active user multi-role by its unique identifier.
	 * 
	 * @param nuserMultiRoleCode the unique identifier of the user multi-role
	 * @param userInfo the user requesting the data
	 * @return the response entity containing the user multi-role or an error message
	 * @throws Exception if retrieval fails or the record is not found
	 */
	@Override
	public ResponseEntity<Object> getActiveUserMultiRoleById(final int nuserMultiRoleCode, final UserInfo userInfo)
			throws Exception {
		final UserMultiRole objUserMultiRole = (UserMultiRole) usersDAO.getActiveUserMultiRoleById(nuserMultiRoleCode);
		if (objUserMultiRole == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objUserMultiRole, HttpStatus.OK);
		}
	}

	/**
	 * Updates an existing user multi-role mapping.
	 * 
	 * @param objUserMultiRole the updated multi-role object
	 * @param userInfo the user performing the update
	 * @param objUsers the user associated with the multi-role
	 * @return the response entity indicating the update status
	 * @throws Exception if update fails or data validation errors occur
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateUserMultiRole(UserMultiRole objUserMultiRole, UserInfo userInfo, Users objUsers)
			throws Exception {
		return usersDAO.updateUserMultiRole(objUserMultiRole, userInfo, objUsers);
	}

	/**
	 * Deletes the specified user multi-role record.
	 * 
	 * @param objUserMultiRole the multi-role object to be deleted
	 * @param userInfo the user performing the deletion
	 * @param objUsers the user associated with the multi-role
	 * @return the response entity with deletion result
	 * @throws Exception if deletion fails or validation fails
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteUserMultiRole(UserMultiRole objUserMultiRole, UserInfo userInfo, Users objUsers)
			throws Exception {
		return usersDAO.deleteUserMultiRole(objUserMultiRole, userInfo, objUsers);
	}

	/**
	 * Retires the specified user from the system.
	 * 
	 * @param objUsers the user to be retired
	 * @param userInfo the user performing the operation
	 * @return the response entity indicating the retirement status
	 * @throws Exception if the retirement process fails
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> retireUser(final Users objUsers, final UserInfo userInfo) throws Exception {
		return usersDAO.retireUser(objUsers, userInfo);
	}

	/**
	 * Resets the password of the specified user.
	 * 
	 * @param objUsersSite the site code of the user
	 * @param objUsers the user whose password is to be reset
	 * @param userInfo the user performing the password reset
	 * @return the response entity indicating password reset result
	 * @throws Exception if password reset fails
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> resetPassword(final int objUsersSite, final Users objUsers, final UserInfo userInfo)
			throws Exception {
		return usersDAO.resetPassword(objUsersSite, objUsers, userInfo);
	}

	/**
	 * Retrieves role-deputy mapping details for the specified user site.
	 * 
	 * @param objUsersSite the user site code
	 * @param userInfo the user performing the retrieval
	 * @return the response entity with role-deputy information
	 * @throws Exception if retrieval fails
	 */
	public ResponseEntity<Object> getRoleDeputyByUserSite(final int objUsersSite, final UserInfo userInfo)
			throws Exception {
		return usersDAO.getRoleDeputyByUserSite(objUsersSite, userInfo);
	}

	/**
	 * Retrieves a list of unassigned sites for the specified user.
	 * 
	 * @param objUsers the user for whom unassigned sites are needed
	 * @param nuserSiteCode the current user site code
	 * @param userInfo the user performing the operation
	 * @return the response entity with the list of unassigned sites
	 * @throws Exception if retrieval fails
	 */
	public ResponseEntity<Object> getUnAssignedSiteListByUser(final Users objUsers, final int nuserSiteCode,
			final UserInfo userInfo) throws Exception {
		return usersDAO.getUnAssignedSiteListByUser(objUsers, nuserSiteCode, userInfo);
	}

	/**
	 * Creates a new user-site assignment.
	 * 
	 * @param objUsersSite the user-site mapping to be created
	 * @param userInfo the user performing the operation
	 * @return the response entity indicating creation status
	 * @throws Exception if creation fails or validation issues occur
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createUsersSite(UsersSite objUsersSite, final UserInfo userInfo) throws Exception {
		return usersDAO.createUsersSite(objUsersSite, userInfo);
	}

	/**
	 * Retrieves an active user-site record by its site code.
	 * 
	 * @param nuserSiteCode the user site code
	 * @param userInfo the user requesting the information
	 * @return the response entity containing the user-site record or error message
	 * @throws Exception if the record is not found or retrieval fails
	 */
	public ResponseEntity<Object> getActiveUsersSiteById(final int nuserSiteCode, final UserInfo userInfo)
			throws Exception {
		final UsersSite objUsersSite = (UsersSite) usersDAO.getActiveUsersSiteById(nuserSiteCode, userInfo);
		if (objUsersSite == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objUsersSite, HttpStatus.OK);
		}
	}

	/**
	 * Updates a user-site assignment with new data.
	 * 
	 * @param objUsersSite the user-site object to be updated
	 * @param userInfo the user performing the update
	 * @return the response entity indicating the update result
	 * @throws Exception if update fails or validation constraints are violated
	 */
	@Transactional
	@Override
	public ResponseEntity<? extends Object> updateUsersSite(UsersSite objUsersSite, UserInfo userInfo) throws Exception {
		return usersDAO.updateUsersSite(objUsersSite, userInfo);
	}

	/**
	 * Deletes the specified user-site assignment.
	 * 
	 * @param objUsersSite the user-site mapping to be deleted
	 * @param userInfo the user performing the deletion
	 * @param objUsers the user associated with the mapping
	 * @return the response entity indicating deletion success or failure
	 * @throws Exception if deletion fails due to constraints or validation
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteUsersSite(UsersSite objUsersSite, UserInfo userInfo, Users objUsers)
			throws Exception {
		return usersDAO.deleteUsersSite(objUsersSite, userInfo, objUsers);
	}

	/**
	 * Retrieves a list of users along with department data for combo display.
	 * 
	 * @param userInfo the user performing the request
	 * @return the response entity with user-department combo data
	 * @throws Exception if retrieval fails
	 */
	public ResponseEntity<Object> getUserWithDeptForCombo(final UserInfo userInfo) throws Exception {
		return usersDAO.getUserWithDeptForCombo(userInfo);
	}

	/**
	 * Creates a new user along with profile image and form data.
	 * 
	 * @param request the multipart request containing user details and image
	 * @param userInfo the user performing the creation
	 * @return the response entity with creation result
	 * @throws Exception if creation fails due to file or validation errors
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createUsers(MultipartHttpServletRequest request, final UserInfo userInfo)
			throws Exception {
		return usersDAO.createUsers(request, userInfo);
	}

	/**
	 * Updates an existing user's details and profile image.
	 * 
	 * @param request the multipart request containing updated user data
	 * @param userInfo the user performing the update
	 * @return the response entity with update status
	 * @throws Exception if update fails due to invalid input or file issues
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateUsers(MultipartHttpServletRequest request, final UserInfo userInfo)
			throws Exception {
		return usersDAO.updateUsers(request, userInfo);
	}

	/**
	 * Retrieves the selected user's profile image for viewing.
	 * 
	 * @param selectedUser the user whose image is requested
	 * @param userInfo the user performing the request
	 * @return the response entity containing the image or error status
	 * @throws Exception if image retrieval fails
	 */
	@Override
	public ResponseEntity<Object> viewSelectedUserImage(final Users selectedUser, final UserInfo userInfo)
			throws Exception {
		return usersDAO.viewSelectedUserImage(selectedUser, userInfo);
	}

	/**
	 * Retrieves profile picture metadata by delegating to DAO.
	 *
	 * @param userInfo User session details.
	 * @return HTTP response with profile picture metadata.
	 * @throws Exception if retrieval fails.
	 */
	@Override
	public ResponseEntity<Object> getUserProfilePictureData(UserInfo userInfo) throws Exception {
		return usersDAO.getUserProfilePictureData(userInfo);
	}

	/**
	 * Uploads and updates the profile picture by delegating to DAO.
	 *
	 * @param request Multipart request containing image data.
	 * @param userInfo User context information.
	 * @return HTTP response with localized status.
	 * @throws Exception if upload fails.
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateUserProfilePicture(MultipartHttpServletRequest request, UserInfo userInfo)
			throws Exception {
		return usersDAO.updateUserProfilePicture(request, userInfo);
	}

	/**
	 * Retrieves the updated user profile picture from FTP storage.
	 *
	 * @param userInfo User identification and language context.
	 * @return HTTP response containing base64 image or error message.
	 * @throws Exception if any error occurs.
	 */
	@Override
	public ResponseEntity<?> getUpdateUserProfilePicture(UserInfo userInfo) throws Exception {
		return usersDAO.getUpdateUserProfilePicture(userInfo);
	}

}
