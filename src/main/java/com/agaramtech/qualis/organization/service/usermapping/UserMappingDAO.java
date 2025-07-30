package com.agaramtech.qualis.organization.service.usermapping;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;

/**
 * This Interface is used to perform CRUD Operations on usermapping Table
 * 
 * @author ATE169
 * @version 9.0.0.1
 */
public interface UserMappingDAO {
	/**
	 * This Method is used to get the usermapping based on userroletemplate
	 * 
	 * @param objMap
	 * @return a response entity with list of users with respective roles
	 * @throws Exception thrown from UserMappingDAOImpl
	 */
	public ResponseEntity<Object> getUserMapping(final Map<String, Object> objMap) throws Exception;

	/**
	 * This Method is used to get the list of userrole based on userroletemplate
	 * 
	 * @param nSiteCode
	 * @param nVersionCode
	 * @return a response entity with list of users with respective roles
	 * @throws Exception thrown from UserMappingDAOImpl
	 */
	public Map<String, Object> getUserRoles(final int nSiteCode,final int nVersionCode) throws Exception;

	/**
	 * This Method is used to get the list of child users/Next Level Users based on
	 * parent users/Top level users
	 * 
	 * @param lastParentUsercode
	 * @param nroleCode
	 * @param nSiteCode
	 * @param nVersionCode
	 * @param levelno
	 * @return a response entity with list of users with respective roles
	 * @throws Exception thrown from UserMappingDAOImpl
	 */
	public ResponseEntity<Object> getChildUsers(final int lastParentUsercode,final int nroleCode,final int nSiteCode,final int nVersionCode,
			int levelno) throws Exception;

	/**
	 * This Method is used to get the list of all available users to be added under
	 * a top level user/parent user
	 * 
	 * @param objMap
	 * @return a response entity with list of all available users with respective
	 *         roles
	 * @throws Exception thrown from UserMappingDAOImpl
	 */

	public ResponseEntity<Object> getAvailableUsers(final Map<String, Object> objMap, final UserInfo userInfo) throws Exception;

	/**
	 * This Method is used to add list of available users under a top level
	 * user/parent user
	 * 
	 * @param objMap
	 * @return a response entity with list of users with respective roles
	 * @throws Exception thrown from UserMappingDAOImpl
	 */
	public ResponseEntity<Object> addUsers(final Map<String, Object> objMap) throws Exception;

	/**
	 * This Method is used to delete a user under a top level user/parent user . If
	 * the user have child users then that also will be deleted
	 * 
	 * @param objMap
	 * @return a response entity with list of users with respective roles
	 * @throws Exception thrown from UserMappingDAOImpl
	 */
	public ResponseEntity<Object> deleteUsers(final Map<String, Object> objMap, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getUserMappingTree(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> copyUserMapping(final Map<String, Object> map) throws Exception;

	public ResponseEntity<Object> getUserMappingCopy(final int nregTypeCode, final int nregSubTypeCode, final int nmasterSiteCode,
			UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getCopyRegSubType(final int nregTypeCode,final int nregSubTypeCode,final int nmasterSiteCode,
			final UserInfo userInfo) throws Exception;

}
