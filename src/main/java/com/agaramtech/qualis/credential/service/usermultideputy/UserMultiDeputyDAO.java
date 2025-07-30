package com.agaramtech.qualis.credential.service.usermultideputy;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.credential.model.UserMultiDeputy;
import com.agaramtech.qualis.credential.model.Users;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation on 'unit' table
 */
public interface UserMultiDeputyDAO {

	/**
	 * This interface declaration will access the DAO layer that is used to
	 * add a new entry to usermultideputy table.
	 * 
	 * @param objUserMultiDeputy [usermultideputy] object holding details to be added in usermultideputy
	 *                   table
	 * @param userInfo   [UserInfo] holding logged in user details and
	 *                   nmasterSiteCode [int] primary key of site object for which
	 *                   the list is to be fetched
	 * @param objUsers [Users] object holding details of users object with nusercode [int] primary key of users object.
	 * @return response entity object holding response status and data of added
	 *         usermultideputy object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createUserMultiDeputy(UserMultiDeputy userMultiDeputy, UserInfo userInfo,	final Users users) throws Exception;
	
	/**
	 * This  interface declaration will access the DAO layer that is used to
	 * retrieve active usermultideputy object based on the specified
	 * nuserMultiDeputyCode.
	 * 
	 * @param nuserMultiDeputyCode [int] primary key of usermultideputy object
	 * @param userInfo             [UserInfo] holding logged in user details based
	 *                             on which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         usermultideputy object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public UserMultiDeputy getActiveUserMultiDeputyById(final int nuserMultiDeputyCode) throws Exception;
	
	/**
	 * This  interface declaration will access the DAO layer that is used to
	 * update entry in usermultideputy table.
	 * 
	 * @param objUserMultiDeputy [UserMultiDeputy] object holding details to be
	 *                           updated in usermultideputy table
	 * @param objUsers           [Users] object holding details of users for which
	 *                           the usermultideputy is to be updated
	 * @param userInfo           [UserInfo] holding logged in user details and
	 *                           nmasterSiteCode [int] primary key of site object
	 *                           for which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         usermultideputy object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateUserMultiDeputy(UserMultiDeputy userMultiDeputy, UserInfo userInfo, final Users users) throws Exception;
	
	/**
	 * This interface declaration will access the DAO layer that is used to
	 * delete an entry in usermultideputy table.
	 * 
	 * @param objUserMultiDeputy [UserMultiDeputy] object holding detail to be
	 *                           deleted from usermultideputy table
	 * @param objUsers           [Users] object holding details of users for which
	 *                           the usermultideputy is to be deleted
	 * @param userInfo           [UserInfo] holding logged in user details and
	 *                           nmasterSiteCode [int] primary key of site object
	 *                           for which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         usermultideputy object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteUserMultiDeputy(UserMultiDeputy userMultiDeputy, UserInfo userInfo, Users users) throws Exception;
	
	/**
	 * This interface declaration will access the DAO layer that is used to
	 * get all the UsersSite, Userrole Data with respect to given nuserSiteCode,
	 * nsiteCode,nuserCode,userInfo
	 * 
	 * @param userInfo      [UserInfo] holding logged in user details and
	 *                      nmasterSiteCode [int] primary key of site object for
	 *                      which the list is to be fetched
	 * @param nuserSiteCode [int] primary key of userssite object
	 * @param nsiteCode     [int] primary key of site object
	 * @param nuserCode     [int] primary key of users object
	 * @return a response entity which holds the list of usermultideputy records
	 *         with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getComboDataforUserMultiDeputy(final int nuserSiteCode,final int nmasterSiteCode, final int nuserCode, final UserInfo userInfo) throws Exception;
}
