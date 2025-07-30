package com.agaramtech.qualis.credential.service.usermultideputy;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.credential.model.UserMultiDeputy;
import com.agaramtech.qualis.credential.model.Users;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'usermultideputy' table
 * through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class UserMultiDeputyServiceImpl implements UserMultiDeputyService {

	private final UserMultiDeputyDAO userMultiDeputyDAO;
	private final CommonFunction commonFunction;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class properties.
	 * 
	 * @param userMultiDeputyDAO userMultiDeputyDAO Interface
	 * @param commonFunction     CommonFunction holding common utility functions
	 */
	public UserMultiDeputyServiceImpl(UserMultiDeputyDAO userMultiDeputyDAO, CommonFunction commonFunction) {
		this.userMultiDeputyDAO = userMultiDeputyDAO;
		this.commonFunction = commonFunction;
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to usermultideputy table.
	 * 
	 * @param objUserMultiDeputy [UserMultiDeputy] object holding details to be
	 *                           added in usermultideputy table
	 * @param objUsers           [Users] object holding details of users for which
	 *                           the usermultideputy is to be created
	 * @param userInfo           [UserInfo] holding logged in user details and
	 *                           nmasterSiteCode [int] primary key of site object
	 *                           for which the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         usermultideputy object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createUserMultiDeputy(UserMultiDeputy objUserMultiDeputy, UserInfo userInfo,
			final Users objUsers) throws Exception {
		return userMultiDeputyDAO.createUserMultiDeputy(objUserMultiDeputy, userInfo, objUsers);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
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
	@Override
	public ResponseEntity<Object> getActiveUserMultiDeputyById(final int nuserMultiDeputyCode, final UserInfo userInfo)
			throws Exception {
		final UserMultiDeputy objUserMultiDeputy = (UserMultiDeputy) userMultiDeputyDAO
				.getActiveUserMultiDeputyById(nuserMultiDeputyCode);
		if (objUserMultiDeputy == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objUserMultiDeputy, HttpStatus.OK);
		}
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
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
	@Transactional
	@Override
	public ResponseEntity<Object> updateUserMultiDeputy(UserMultiDeputy objUserMultiDeputy, UserInfo userInfo,
			final Users objUsers) throws Exception {
		return userMultiDeputyDAO.updateUserMultiDeputy(objUserMultiDeputy, userInfo, objUsers);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
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
	@Transactional
	@Override
	public ResponseEntity<Object> deleteUserMultiDeputy(UserMultiDeputy objUserMultiDeputy, UserInfo userInfo,
			Users objUsers) throws Exception {
		return userMultiDeputyDAO.deleteUserMultiDeputy(objUserMultiDeputy, userInfo, objUsers);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
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
	@Override
	public ResponseEntity<Object> getComboDataforUserMultiDeputy(final int nuserSiteCode, final int nsiteCode,
			final int nuserCode, final UserInfo userInfo) throws Exception {
		return userMultiDeputyDAO.getComboDataforUserMultiDeputy(nuserSiteCode, nsiteCode, nuserCode, userInfo);
	}

}
