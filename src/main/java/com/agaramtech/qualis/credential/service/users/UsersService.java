package com.agaramtech.qualis.credential.service.users;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.credential.model.UserMultiRole;
import com.agaramtech.qualis.credential.model.Users;
import com.agaramtech.qualis.credential.model.UsersSite;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface declaration holds methods to perform CRUD operation on 'users' table
 */
public interface UsersService {

	/**
	 * Retrieves users based on provided user code and flag. This can return a specific user or a filtered list.
	 *
	 * @param nuserCode The user code to filter by. Can be null to fetch all users.
	 * @param nFlag A flag indicating filter condition (e.g., active/inactive).
	 * @param userInfo Contains site and language context.
	 * @return HTTP response with user list or user details.
	 * @throws Exception if database access or processing fails.
	 */
	public ResponseEntity<Object> getUsers( Integer nuserCode,Integer nFlag, final UserInfo userInfo) throws Exception;
	
	/**
	 * Fetches an active user by their unique ID. Returns an error message if the user is already deleted.
	 *
	 * @param nuserCode Unique identifier of the user.
	 * @param userInfo User context for site and multilingual error messaging.
	 * @return HTTP response containing user data or multilingual error if user not found.
	 * @throws Exception if lookup fails or database error occurs.
	 */
	public ResponseEntity<Object> getActiveUsersById(final int nuserCode, final UserInfo userInfo) throws Exception;
	
	/**
	 * Deletes the specified user from the system.
	 *
	 * @param objUsers the user to be deleted
	 * @param userInfo the user performing the delete action
	 * @return the response entity with the result of the operation
	 * @throws Exception if an error occurs during deletion
	 */
	public ResponseEntity<Object> deleteUsers(Users objUsers, UserInfo userInfo) throws Exception ;
	
	/**
	 * Creates a new multi-role mapping for a user.
	 *
	 * @param objUserMultiRole the multi-role object to be created
	 * @param userInfo the user initiating the creation
	 * @param objUsers the user to whom the multi-role belongs
	 * @return the response entity with the creation result
	 * @throws Exception if an error occurs during creation
	 */
	public ResponseEntity<Object> createUserMultiRole(UserMultiRole objUserMultiRole, final UserInfo userInfo, Users objUsers) throws Exception;
	
	/**
	 * Retrieves an active user multi-role by its ID.
	 *
	 * @param nuserMultiRoleCode the ID of the user multi-role
	 * @param userInfo the user requesting the data
	 * @return the response entity containing the multi-role details
	 * @throws Exception if retrieval fails or the record is not found
	 */
	public ResponseEntity<Object> getActiveUserMultiRoleById(final int nuserMultiRoleCode, final UserInfo userInfo) throws Exception;
	
	/**
	 * Updates an existing user multi-role.
	 *
	 * @param objUserMultiRole the updated multi-role object
	 * @param userInfo the user performing the update
	 * @param objUsers the user associated with the multi-role
	 * @return the response entity with the update result
	 * @throws Exception if an error occurs during update
	 */
	public ResponseEntity<Object> updateUserMultiRole(UserMultiRole objUserMultiRole, UserInfo userInfo, Users objUsers) throws Exception ;
	
	/**
	 * Deletes the specified user multi-role.
	 *
	 * @param objUserMultiRole the multi-role to be deleted
	 * @param userInfo the user initiating the deletion
	 * @param objUsers the user associated with the multi-role
	 * @return the response entity with the deletion result
	 * @throws Exception if deletion fails
	 */
	public ResponseEntity<Object> deleteUserMultiRole(UserMultiRole objUserMultiRole, UserInfo userInfo, Users objUsers) throws Exception ;
	
	/**
	 * Retires the specified user.
	 *
	 * @param objUsers the user to be retired
	 * @param userInfo the user initiating the operation
	 * @return the response entity with retirement status
	 * @throws Exception if an error occurs during the process
	 */
	public ResponseEntity<Object> retireUser(final Users objUsers, final UserInfo userInfo) throws Exception;
	
	/**
	 * Resets the password for the given user.
	 *
	 * @param userSiteCode the site code of the user
	 * @param objUsers the user whose password is being reset
	 * @param userInfo the user initiating the reset
	 * @return the response entity with reset status
	 * @throws Exception if an error occurs during password reset
	 */
	public ResponseEntity<Object> resetPassword(final int userSiteCode, final Users objUsers,final UserInfo userInfo) throws Exception;
	
	/**
	 * Retrieves the role-deputy mapping for a user site.
	 *
	 * @param userSiteCode the site code of the user
	 * @param userInfo the user requesting the data
	 * @return the response entity with the mapping details
	 * @throws Exception if data retrieval fails
	 */
	public ResponseEntity<Object> getRoleDeputyByUserSite(final int userSiteCode, final UserInfo userInfo) throws Exception;
	
	/**
	 * Retrieves the list of sites not assigned to the given user.
	 *
	 * @param objUsers the user object
	 * @param nuserSiteCode the site code to check against
	 * @param userInfo the user requesting the data
	 * @return the response entity with unassigned site list
	 * @throws Exception if retrieval fails
	 */
	public ResponseEntity<Object> getUnAssignedSiteListByUser(final Users objUsers, final int nuserSiteCode, final UserInfo userInfo) throws Exception;
	
	/**
	 * Creates a new user-site assignment.
	 *
	 * @param usersSite the user-site object to be created
	 * @param userInfo the user initiating the operation
	 * @return the response entity with creation status
	 * @throws Exception if creation fails
	 */
	public ResponseEntity<Object> createUsersSite(UsersSite usersSite, final UserInfo userInfo) throws Exception;
	
	/**
	 * Retrieves an active user-site record by its ID.
	 *
	 * @param nuserSiteCode the user site code
	 * @param userInfo the user requesting the record
	 * @return the response entity with user-site data
	 * @throws Exception if record is not found or retrieval fails
	 */
	public ResponseEntity<Object> getActiveUsersSiteById(final int nuserSiteCode, final UserInfo userInfo) throws Exception;
	
	/**
	 * Updates an existing user-site mapping.
	 *
	 * @param objUserSite the user-site object with updated data
	 * @param userInfo the user performing the update
	 * @return the response entity with update result
	 * @throws Exception if update fails
	 */
	public ResponseEntity<? extends Object> updateUsersSite(UsersSite objUserSite, UserInfo userInfo) throws Exception;
	
	/**
	 * Deletes the specified user-site mapping.
	 *
	 * @param objUserSite the user-site to be deleted
	 * @param userInfo the user performing the deletion
	 * @param objUsers the user associated with the mapping
	 * @return the response entity with deletion result
	 * @throws Exception if deletion fails
	 */
	public ResponseEntity<Object> deleteUsersSite(UsersSite objUserSite, UserInfo userInfo, Users objUsers) throws Exception ;
	
	/**
	 * Retrieves users with department information for combo list display.
	 *
	 * @param userInfo the user requesting the list
	 * @return the response entity containing the combo data
	 * @throws Exception if retrieval fails
	 */
	public ResponseEntity<Object> getUserWithDeptForCombo(final UserInfo userInfo) throws Exception;
	
	/**
	 * Creates a new user along with uploaded image.
	 *
	 * @param request the multipart request containing user data and image
	 * @param userInfo the user initiating the creation
	 * @return the response entity with creation result
	 * @throws Exception if creation fails or file issues occur
	 */
	public ResponseEntity<Object> createUsers(MultipartHttpServletRequest request, final UserInfo userInfo) throws Exception;
	
	/**
	 * Updates an existing user's details and profile image.
	 *
	 * @param request the multipart request containing updated data
	 * @param userInfo the user initiating the update
	 * @return the response entity with update status
	 * @throws Exception if update fails
	 */
	public ResponseEntity<Object> updateUsers(MultipartHttpServletRequest request, final UserInfo userInfo) throws Exception;
	
	/**
	 * Retrieves the selected user's profile image.
	 *
	 * @param selectedUser the user whose image is to be viewed
	 * @param userInfo the user requesting the image
	 * @return the response entity containing the image
	 * @throws Exception if image retrieval fails
	 */
	public ResponseEntity<Object> viewSelectedUserImage(final Users selectedUser, final UserInfo userInfo) throws Exception;
	
	/**
	 * Retrieves metadata of the user's profile picture such as FTP location and image name.
	 *
	 * @param userInfo User session details including user code and site code.
	 * @return HTTP response containing profile image metadata.
	 * @throws Exception if retrieval fails.
	 */
	public ResponseEntity<Object> getUserProfilePictureData(final UserInfo userInfo) throws Exception;
	
	/**
	 * Uploads and updates the user profile image in the database and via FTP.
	 *
	 * @param request Multipart request with image and filename information.
	 * @param userInfo User context used for site and language reference.
	 * @return HTTP response with multilingual status message.
	 * @throws Exception if upload or database update fails.
	 */
	public ResponseEntity<Object> updateUserProfilePicture(MultipartHttpServletRequest request,final UserInfo userInfo) throws Exception;
	
	/**
	 * Retrieves actual image content of the user's updated profile picture.
	 *
	 * @param userInfo User session information needed for path and language settings.
	 * @return HTTP response containing base64 encoded image or error message.
	 * @throws Exception if FTP or database operation fails.
	 */
	public ResponseEntity<?> getUpdateUserProfilePicture(final UserInfo userInfo) throws Exception;

}
