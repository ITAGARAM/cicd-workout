package com.agaramtech.qualis.credential.service.userrole;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis. credential.model.UserRole;
import com.agaramtech.qualis.credential.model.Users;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation on 'userrole' table
 */
public interface UserRoleDAO {
	
	/**
	 * This interface declaration is used to get the over all userrole.
	 * 
	  * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of userrole and also
	 *         have the HTTP response code
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getUserRole(final int siteCode) throws Exception;
	
	/**
	 * This interface declaration is used to retrieve active userrole object
	 * based on the specified nuserrolecode.
	 * 
	 * @param nuserrolecode [int] primary key of userrole object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         userrole object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public UserRole getActiveUserRoleById(final int nuserRoleCode) throws Exception ;
	
	/**
	 * This interface declaration is used to add a new entry to userrole
	 * table.
	 * 
	 * @param userrole [User Role] object holding details to be
	 *                           added in userrole table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         userrole object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createUserRole(final UserRole userRole, final UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to update entry in userrole table.
	 * 
	 * @param userrole [UserRole] object holding details to be
	 *                           updated in userrole table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         userrole object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateUserRole(final UserRole userRole, final UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to delete entry in userrole table.
	 * 
	 * @param userrole [UserRole] object holding detail to be
	 *                           deleted in userrole table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         userrole object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteUserRole(final UserRole userRole, final UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to get the over all userrole.
	 * 
	  * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of userrole and also
	 *         have the HTTP response code
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getUserRoleComboData(final Users users, final int nuserSiteCode,final int nuserMultiRoleCode, final int nsiteCode, final UserInfo userInfo) throws Exception;
}
