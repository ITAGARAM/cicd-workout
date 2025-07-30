package com.agaramtech.qualis.credential.service.userrole;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.credential.model.UserRole;
import com.agaramtech.qualis.credential.model.Users;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;



/**
 * This class holds methods to perform CRUD operation on 'userrole' table through
 * its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class UserRoleServiceImpl implements UserRoleService{

	
	private final UserRoleDAO userRoleDAO;
	private final CommonFunction commonFunction;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class properties.
	 * @param userRoleDAO UnitDAO Interface
	 * @param commonFunction CommonFunction holding common utility functions
	 */
	public UserRoleServiceImpl(UserRoleDAO userRoleDAO, CommonFunction commonFunction) {
		this.userRoleDAO = userRoleDAO;
		this.commonFunction = commonFunction;
	}
	
	/**
	 * This service implementation method will access the DAO layer that is used 
	 * to get all the available userroles with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of user role records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */	
@Override
public ResponseEntity<Object> getUserRole(final int nmasterSiteCode) throws Exception {		
	return userRoleDAO.getUserRole(nmasterSiteCode);
}

/**
 * This service implementation method will access the DAO layer that is used to retrieve active userrole object based
 * on the specified nuserRoleCode.
 * @param nuserRoleCode [int] primary key of userrole object
 * @param userInfo [UserInfo] holding logged in user details based on 
 * 							  which the list is to be fetched
 * @return response entity  object holding response status and data of userrole object
 * @throws Exception that are thrown in the DAO layer
 */	
@Override
public ResponseEntity<Object> getActiveUserRoleById(final int nuserRoleCode, final UserInfo userInfo) throws Exception {
	final UserRole objUserRole = userRoleDAO.getActiveUserRoleById(nuserRoleCode);
	if (objUserRole == null) {
		return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
	}else {
		return new ResponseEntity<>(objUserRole, HttpStatus.OK);
	}
}

/**
 * This service implementation method will access the DAO layer that is used to
 * add a new entry to userrole  table.
 * @param objUserRole [UserRole] object holding details to be added in userrole table
 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
 * 							  which the list is to be fetched
 * @return response entity object holding response status and data of added userrole object
 * @throws Exception that are thrown in the DAO layer
 */
@Transactional
@Override
public ResponseEntity<Object> createUserRole(final UserRole objUserRole, final UserInfo userInfo) throws Exception {
	return userRoleDAO.createUserRole(objUserRole, userInfo);
}

/**
 * This service implementation method will access the DAO layer that is used to
 *  update entry in userrole  table.
 * @param objUserRole [User Role] object holding details to be updated in userrole table
 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
 * 							  which the list is to be fetched
 * @return response entity object holding response status and data of updated userrole object
 * @throws Exception that are thrown in the DAO layer
 */
@Transactional
@Override
public ResponseEntity<Object> updateUserRole(final UserRole objUserRole, final UserInfo userInfo) throws Exception {
	return userRoleDAO.updateUserRole(objUserRole, userInfo);
}

/**
 * This service implementation method will access the DAO layer that is used to delete an entry in userrole  table.
 * @param objUserRole [User Role] object holding detail to be deleted from userrole table
 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
 * 							  which the list is to be fetched
 * @return response entity object holding response status and data of deleted userrole object
 * @throws Exception that are thrown in the DAO layer
 */
@Transactional
@Override
public ResponseEntity<Object> deleteUserRole(final UserRole objUserRole,final UserInfo userInfo) throws Exception {
	return userRoleDAO.deleteUserRole(objUserRole, userInfo);
}
	
/**
 * This service implementation method will access the DAO layer that is used 
 * to get all the available userroles with respect to user object, nuserSiteCode,nuserMultiRoleCode, nsiteCode, userInfo 
 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
 * 							  which the list is to be fetched
 * @param objUsers [Users] holding User Object
 * @param nuserSiteCode [int] primarykey of userssite table
 * @param nuserMultiRoleCode [int] primarykey of usermultirole table
 * @param nsiteCode [int] primarykey of site table
 * @return a response entity which holds the list of userrole  records with respect to given params
 * @throws Exception that are thrown in the DAO layer
 */	
public ResponseEntity<Object> getUserRoleComboData(final Users objUsers, final int nuserSiteCode, final int nuserMultiRoleCode, final int nsiteCode, UserInfo userInfo) throws Exception{
	return userRoleDAO.getUserRoleComboData(objUsers, nuserSiteCode, nuserMultiRoleCode, nsiteCode,userInfo);

}

}
