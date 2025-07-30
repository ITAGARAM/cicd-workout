package com.agaramtech.qualis.organization.service.usermapping;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.UserInfo;

/**
 * This Class is used to perform CRUD Operations on usermapping Table
 * @author ATE169
 * @version 9.0.0.1
 */

@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class UserMappingServiceImpl implements UserMappingService{

	
	private final UserMappingDAO userMappingDAO;
	
	public UserMappingServiceImpl(UserMappingDAO userMappingDAO) {
		this.userMappingDAO=userMappingDAO;
	}
	
	/**
	 * This Method is used to get the usermapping based on userroletemplate
	 * @param objMap
	 * @return a response entity with list of users with respective roles
	 * @throws Exception thrown from UserMappingDAOImpl
	 */
	@Override
	public ResponseEntity<Object> getUserMapping(final Map<String, Object> objMap) throws Exception {
		
		return userMappingDAO.getUserMapping(objMap);
	}
	/**
	 * This Method is used to get the list of userrole based on userroletemplate
	 * @param nSiteCode
	 * @param nVersionCode
	 * @return a response entity with list of users with respective roles
	 * @throws Exception thrown from UserMappingDAOImpl
	 */
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public ResponseEntity<Object> getUserRoles(final int nSiteCode, final int nVersionCode) throws Exception {
		
		return new ResponseEntity(userMappingDAO.getUserRoles(nSiteCode, nVersionCode),HttpStatus.OK);
	}

	/**
	 * This Method is used to get the list of child users/Next Level Users based on parent users/Top level users
	 * @param lastParentUsercode
	 * @param nroleCode
	 * @param nSiteCode
	 * @param nVersionCode
	 * @param levelno
	 * @return a response entity with list of users with respective roles
	 * @throws Exception thrown from UserMappingDAOImpl
	 */
	@Override
	public ResponseEntity<Object> getChildUsers(final int lastParentUsercode, final int nroleCode, final int nSiteCode,final int nVersionCode,final int levelno)
			throws Exception {
		return userMappingDAO.getChildUsers(lastParentUsercode, nroleCode, nSiteCode, nVersionCode, levelno);
	}

	/**
	 * This Method is used to get the list of all available users to be added under a top level user/parent user
	 * @param objMap
	 * @return a response entity with list of all available users with respective roles
	 * @throws Exception thrown from UserMappingDAOImpl
	 */
	@Override
	public ResponseEntity<Object> getAvailableUsers(final Map<String, Object> objMap,final UserInfo userInfo) throws Exception {
		
		return userMappingDAO.getAvailableUsers(objMap, userInfo) ;
	}

	/**
	 * This Method is used to add list of available users under a top level user/parent user
	 * @param objMap
	 * @return a response entity with list of users with respective roles
	 * @throws Exception thrown from UserMappingDAOImpl
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> addUsers(final Map<String, Object> objMap) throws Exception {
		
		return userMappingDAO.addUsers(objMap);
	}

	/**
	 * This Method is used to delete a user under a top level user/parent user . If the user have child users then that also will be deleted
	 * @param objMap
	 * @return a response entity with list of users with respective roles
	 * @throws Exception thrown from UserMappingDAOImpl
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteUsers(final Map<String, Object> objMap,final UserInfo userInfo) throws Exception {
		return userMappingDAO.deleteUsers(objMap, userInfo);
	}
	
	
	@Override
	public ResponseEntity<Object> getUserMappingTree(final Map<String, Object> map) throws Exception {
		return userMappingDAO.getUserMappingTree(map);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> copyUserMapping(final Map<String, Object> map) throws Exception {
		return userMappingDAO.copyUserMapping(map);
	}
	
	
	@Override
	public ResponseEntity<Object> getUserMappingCopy(final int nregTypeCode,final int nregSubTypeCode,final int nmasterSiteCode,final UserInfo userInfo) throws Exception {
		
		return userMappingDAO.getUserMappingCopy(nregTypeCode, nregSubTypeCode, nmasterSiteCode, userInfo);
	}
	
	
	@Override
	public ResponseEntity<Object> getCopyRegSubType(final int nregTypeCode,final int nregSubTypeCode,final int nmasterSiteCode,final UserInfo userInfo) throws Exception {
		
		return userMappingDAO.getCopyRegSubType(nregTypeCode,nregSubTypeCode, nmasterSiteCode, userInfo);
	}

}
