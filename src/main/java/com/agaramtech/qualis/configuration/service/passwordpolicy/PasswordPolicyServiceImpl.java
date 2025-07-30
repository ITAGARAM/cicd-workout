package com.agaramtech.qualis.configuration.service.passwordpolicy;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.configuration.model.PasswordPolicy;
import com.agaramtech.qualis.credential.model.UserRole;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'passwordpolicy' table
 * through its DAO layer.
 * 
 * @author ATE113
 * @version 9.0.0.1
 * @since 06- Jul- 2020
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class PasswordPolicyServiceImpl implements PasswordPolicyService {

	private PasswordPolicyDAO passwordPolicyDAO;
	CommonFunction commonFunction;

	/**
	 * This constructor injection method is used to pass the object dependencies to the class properties.
	 * @param PasswordPolicyDAO passwordPolicyDAO Interface
	 * @param commonFunction CommonFunction holding common utility functions
	 */
	public PasswordPolicyServiceImpl(PasswordPolicyDAO passwordPolicyDAO, CommonFunction commonFunction) {
		this.passwordPolicyDAO = passwordPolicyDAO;
		this.commonFunction = commonFunction;
	}

	/**
	 * This method is used to retrieve list of all active passwordpolicy for the
	 * specified site through its DAO layer
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @param npolicyCode [int] primary key of passwordpolicy object for which the list is to be
	 *                 checked
	 * @return response entity object holding response status and list of all active
	 *         passwordpolicy
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getPasswordPolicy(Integer npolicycode, final UserInfo userInfo) throws Exception {
		return passwordPolicyDAO.getPasswordPolicy(npolicycode, userInfo);// siteCode);
	}

	/**
	 * This interface declaration is used to retrieve list of all active
	 * passwordpolicy for the specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @param nuserrolepolicycode [int] key of userrolepolicy object for which the
	 *                            list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         passwordpolicy
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getPasswordPolicyByUserRoleCode(Integer nuserrolepolicycode, final UserInfo userInfo)
			throws Exception {
		return passwordPolicyDAO.getPasswordPolicyByUserRoleCode(nuserrolepolicycode, userInfo);// siteCode);
	}

	/**
	 * This method is used to retrieve active passwordpolicy object based on the
	 * specified npolicycode through its DAO layer.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @param npolicycode [int] primary key of passwordpolicy object
	 * @return response entity object holding response status and data of
	 *         passwordpolicy object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActivePasswordPolicyById(final int npolicycode, final UserInfo userInfo)
			throws Exception {
		final PasswordPolicy passwordPolicy = (PasswordPolicy) passwordPolicyDAO
				.getActivePasswordPolicyById(npolicycode, userInfo);
		if (passwordPolicy == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(passwordPolicy, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to add a new entry to passwordpolicy table through its
	 * DAO layer.
	 * 
	 * @param passwordpolicy [Passwordpolicy] object holding details to be added in
	 *                       passwordpolicy table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @param nuserrolecode [int] primary key of userrole object for which the passwordpolicy
	 *                    record will get
	 * @return response entity object holding response status and data of added
	 *         passwordpolicy object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createPasswordPolicy(PasswordPolicy passwordPolicy, UserInfo userInfo,
			Integer nuserrolecode) throws Exception {

		return passwordPolicyDAO.createPasswordPolicy(passwordPolicy, userInfo, nuserrolecode);
	}

	/**
	 * This method is used to update entry in passwordpolicy table through its DAO
	 * layer.
	 * 
	 * @param passwordpolicy [Passwordpolicy] object holding details to be updated
	 *                       in passwordpolicy table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         passwordpolicy object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updatePasswordPolicy(PasswordPolicy passwordPolicy, UserInfo userInfo)
			throws Exception {

		return passwordPolicyDAO.updatePasswordPolicy(passwordPolicy, userInfo);
	}

	/**
	 * This method is used to delete entry in passwordpolicy table through its DAO
	 * layer.
	 * 
	 * @param passwordpolicy [PasswordPolicy] object holding detail to be deleted in
	 *                       passwordpolicy table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         passwordpolicy object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deletePasswordPolicy(PasswordPolicy passwordPolicy, UserInfo userInfo)
			throws Exception {
		return passwordPolicyDAO.deletePasswordPolicy(passwordPolicy, userInfo);
	}

	/**
	 * This method is used to approve entry in userrolepolicy table through its DAO
	 * layer.
	 * 
	 * @param userrolepolicy [UserRolePolicy] object holding detail to be approved
	 *                       in userrolepolicy table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of approved
	 *         userrolepolicy object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> approveUserRolePolicy(PasswordPolicy passwordPolicy, UserInfo userInfo)
			throws Exception {

		return passwordPolicyDAO.approveUserRolePolicy(passwordPolicy, userInfo);
	}

	/**
	 * This interface declaration is used to retrieve active userrolepolicy object
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         userrolepolicy object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getUserRolePolicy(UserInfo userInfo) throws Exception {
		return passwordPolicyDAO.getUserRolePolicy(userInfo);
	}

	/**
	 * This method is used to copy the active passwordpolicy to the specified
	 * userrole.
	 * 
	 * @param lstuserRole [UserRole] object holding list of details of userrole
	 * @param npolicyCode [int] primary key of passwordpolicy object for which the
	 *                    record is copied to specified userrole
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @param spolicyname [String] string field of passwordpolicy object for which the passwordpolicy name to check for duplicate record
	 * @return response entity object holding response status and list of all active
	 *         passwordpolicy
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> copyPasswordPolicyToSelectedRole(List<UserRole> lstuserRole, final int npolicycode,
			UserInfo userInfo, final String spolicyname) throws Exception {

		return passwordPolicyDAO.copyPasswordPolicyToSelectedRole(lstuserRole, npolicycode, userInfo, spolicyname);
	}
}
