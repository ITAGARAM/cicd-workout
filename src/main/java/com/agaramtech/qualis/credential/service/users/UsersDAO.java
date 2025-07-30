package com.agaramtech.qualis.credential.service.users;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.credential.model.UserMultiRole;
import com.agaramtech.qualis.credential.model.Users;
import com.agaramtech.qualis.credential.model.UsersSite;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation on 'users' table
 */
public interface UsersDAO {
	
	/**
	 * Retrieves all users or a specific user based on the user code and additional filtering.
	 *
	 * @param nuserCode User code to fetch specific user. If null, fetches all users.
	 * @param nFlag Flag to determine whether to fetch image paths using FTP (1 = fetch).
	 * @param userInfo Contains site code, language preferences, and user context.
	 * @return ResponseEntity containing user list, selected user, image paths, and role/deputy info.
	 * @throws Exception if database access or processing fails.
	 */
	public ResponseEntity<Object> getUsers(Integer nuserCode,Integer nFlag, final UserInfo userInfo) throws Exception;
	
	/**
	 * Retrieves multi-role details of a user based on their user site code.
	 *
	 * @param nuserSiteCode Unique code for the user-site mapping.
	 * @param userInfo Contains language and site details.
	 * @return ResponseEntity containing a list of UserMultiRole records.
	 * @throws Exception if query fails.
	 */
	public ResponseEntity<Object> getUserMultiRole(final int nuserSiteCode, final UserInfo userInfo) throws Exception ;
	/**
	 * Retrieves the multi-deputy details of a user based on user site code.
	 * 
	 * @param nuserSiteCode the site code of the user
	 * @param userInfo the user information performing the request
	 * @return the response entity containing multi-deputy details
	 * @throws Exception if any database or processing error occurs
	 */
	public ResponseEntity<Object> getUserMultiDeputy(final int nuserSiteCode, final UserInfo userInfo) throws Exception;

	/**
	 * Retrieves the active user based on user code.
	 * 
	 * @param nuserCode the unique user code
	 * @param userInfo the user information performing the request
	 * @return the active user object
	 * @throws Exception if the user is not found or any processing error occurs
	 */
	public Users getActiveUserById(final int nuserCode, final UserInfo userInfo) throws Exception;

	/**
	 * Deletes the given user from the system.
	 * 
	 * @param objUsers the user object to be deleted
	 * @param userInfo the user information performing the deletion
	 * @return the response entity indicating success or failure
	 * @throws Exception if any error occurs during deletion
	 */
	public ResponseEntity<Object> deleteUsers(Users objUsers, UserInfo userInfo) throws Exception;

	/**
	 * Creates a new user multi-role mapping.
	 * 
	 * @param objUserMultiRole the multi-role mapping details
	 * @param userInfo the user information performing the creation
	 * @param objUsers the user associated with the multi-role
	 * @return the response entity with creation status
	 * @throws Exception if creation fails or validation error occurs
	 */
	public ResponseEntity<Object> createUserMultiRole(UserMultiRole objUserMultiRole, final UserInfo userInfo, Users objUsers) throws Exception;

	/**
	 * Retrieves the active user multi-role by its unique code.
	 * 
	 * @param nuserMultiRoleCode the unique multi-role code
	 * @return the active user multi-role object
	 * @throws Exception if the multi-role is not found or invalid
	 */
	public UserMultiRole getActiveUserMultiRoleById(final int nuserMultiRoleCode) throws Exception;

	/**
	 * Updates the given user multi-role details.
	 * 
	 * @param objUserMultiRole the updated multi-role object
	 * @param userInfo the user information performing the update
	 * @param objUsers the user associated with the multi-role
	 * @return the response entity indicating update status
	 * @throws Exception if update fails or constraints are violated
	 */
	public ResponseEntity<Object> updateUserMultiRole(UserMultiRole objUserMultiRole, UserInfo userInfo, Users objUsers) throws Exception;

	/**
	 * Deletes the given user multi-role record.
	 * 
	 * @param objUserMultiRole the multi-role object to delete
	 * @param userInfo the user information performing the deletion
	 * @param objUsers the user associated with the multi-role
	 * @return the response entity indicating success or failure
	 * @throws Exception if any error occurs during deletion
	 */
	public ResponseEntity<Object> deleteUserMultiRole(UserMultiRole objUserMultiRole, UserInfo userInfo, Users objUsers) throws Exception;

	/**
	 * Retires the specified user from active status.
	 * 
	 * @param objUsers the user to be retired
	 * @param userInfo the user information initiating the operation
	 * @return the response entity with retirement status
	 * @throws Exception if the operation fails or user status is invalid
	 */
	public ResponseEntity<Object> retireUser(final Users objUsers, final UserInfo userInfo) throws Exception;

	/**
	 * Resets the password for the given user site.
	 * 
	 * @param nUserSiteCode the site code of the user
	 * @param objUsers the user whose password is being reset
	 * @param userInfo the user information performing the operation
	 * @return the response entity with reset status
	 * @throws Exception if the password reset fails
	 */
	public ResponseEntity<Object> resetPassword(final int nUserSiteCode, final Users objUsers, final UserInfo userInfo) throws Exception;

	/**
	 * Retrieves role-deputy mappings for a given user site code.
	 * 
	 * @param nUserSiteCode the user site code
	 * @param userInfo the user information performing the retrieval
	 * @return the response entity containing role-deputy information
	 * @throws Exception if any retrieval error occurs
	 */
	public ResponseEntity<Object> getRoleDeputyByUserSite(final int nUserSiteCode, final UserInfo userInfo) throws Exception;

	/**
	 * Fetches a list of unassigned sites for a user.
	 * 
	 * @param objUsers the user object
	 * @param nuserSiteCode the user site code for context
	 * @param userInfo the user information performing the request
	 * @return the response entity containing the unassigned site list
	 * @throws Exception if retrieval fails
	 */
	public ResponseEntity<Object> getUnAssignedSiteListByUser(final Users objUsers, final int nuserSiteCode, final UserInfo userInfo) throws Exception;

	/**
	 * Creates a user-site assignment.
	 * 
	 * @param objUsersSite the user-site assignment object
	 * @param userInfo the user information performing the creation
	 * @return the response entity with creation status
	 * @throws Exception if creation fails or input is invalid
	 */
	public ResponseEntity<Object> createUsersSite(UsersSite objUsersSite, final UserInfo userInfo) throws Exception;

	/**
	 * Retrieves the active user-site record by ID.
	 * 
	 * @param nuserSiteCode the user site code
	 * @param userInfo the user information performing the retrieval
	 * @return the active user-site object
	 * @throws Exception if not found or inactive
	 */
	public UsersSite getActiveUsersSiteById(final int nuserSiteCode, final UserInfo userInfo) throws Exception;

	/**
	 * Updates the specified user-site mapping.
	 * 
	 * @param objUsersSite the updated user-site mapping object
	 * @param userInfo the user information performing the update
	 * @return the response entity with update status
	 * @throws Exception if the update fails
	 */
	public ResponseEntity<? extends Object> updateUsersSite(UsersSite objUsersSite, UserInfo userInfo) throws Exception;

	/**
	 * Deletes a user-site mapping.
	 * 
	 * @param objUsersSite the mapping object to be deleted
	 * @param userInfo the user information performing the deletion
	 * @param objUsers the user associated with the mapping
	 * @return the response entity indicating deletion result
	 * @throws Exception if deletion fails or access is restricted
	 */
	public ResponseEntity<Object> deleteUsersSite(UsersSite objUsersSite, UserInfo userInfo, Users objUsers) throws Exception;

	/**
	 * Retrieves users along with department details for dropdown combo.
	 * 
	 * @param userInfo the user information performing the request
	 * @return the response entity containing users with department data
	 * @throws Exception if retrieval fails
	 */
	public ResponseEntity<Object> getUserWithDeptForCombo(final UserInfo userInfo) throws Exception;

	/**
	 * Creates a new user with image or file input.
	 * 
	 * @param request the multipart request containing user data
	 * @param userInfo the user information performing the creation
	 * @return the response entity with creation result
	 * @throws Exception if creation fails or validation issues occur
	 */
	public ResponseEntity<Object> createUsers(MultipartHttpServletRequest request, final UserInfo userInfo) throws Exception;

	/**
	 * Updates user information with optional image or file.
	 * 
	 * @param request the multipart request containing updated data
	 * @param userInfo the user information performing the update
	 * @return the response entity indicating update result
	 * @throws Exception if update fails or input is invalid
	 */
	public ResponseEntity<Object> updateUsers(MultipartHttpServletRequest request, final UserInfo userInfo) throws Exception;

	/**
	 * Retrieves the selected user's profile image.
	 * 
	 * @param selectedUser the user whose image is being viewed
	 * @param userInfo the user information performing the view
	 * @return the response entity containing image data
	 * @throws Exception if image retrieval fails
	 */
	public ResponseEntity<Object> viewSelectedUserImage(final Users selectedUser, final UserInfo userInfo) throws Exception;

	/**
	 * Queries metadata of the user's profile image from the database.
	 *
	 * @param userInfo User session details including user code and site code.
	 * @return HTTP response with user file metadata.
	 * @throws Exception on query error.
	 */
	public ResponseEntity<Object> getUserProfilePictureData(final UserInfo userInfo) throws Exception;
   
	/**
	 * Updates the user's profile picture by uploading it via FTP and updating the database reference.
	 *
	 * @param request Multipart request with image and metadata.
	 * @param userInfo User session and localization information.
	 * @return ResponseEntity with localized message based on upload status.
	 * @throws Exception if FTP or database update fails.
	 */
	public ResponseEntity<Object> updateUserProfilePicture(MultipartHttpServletRequest request,final UserInfo userInfo) throws Exception;
    
	/**
	 * Retrieves and returns the user's profile image content using stored FTP path.
	 *
	 * @param userInfo User identification and site details used to construct FTP path.
	 * @return Response containing image in base64 format or error message.
	 * @throws Exception if data fetch or FTP read fails.
	 */
	public ResponseEntity<?> getUpdateUserProfilePicture(final UserInfo userInfo) throws Exception;
}
