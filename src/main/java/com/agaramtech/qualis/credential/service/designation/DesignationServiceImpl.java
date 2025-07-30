package com.agaramtech.qualis.credential.service.designation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.credential.model.Designation;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

import lombok.RequiredArgsConstructor;

/**
 * This class holds methods to perform CRUD operation on 'credentialdesignation'
 * table through its DAO layer.
 * 
 * @author ATE184
 * @version 9.0.0.1
 * @since 26- Jun- 2020
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class DesignationServiceImpl implements DesignationService {

	private final DesignationDAO designationDAO;
	private final CommonFunction commonFunction;

	/**
	 * This method is used to retrieve list of all active designation for the
	 * specified site through its DAO layer
	 * 
	 * @param nmasterSiteCode [int] primary key of site object for which the list is
	 *                        to be fetched
	 * @return response entity object holding response status and list of all active
	 *         designation
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getDesignation(final UserInfo userInfo) throws Exception {
		return designationDAO.getDesignation(userInfo);
	}

	/**
	 * This method is used to retrieve active credentialdesignation object based on
	 * the specified ndesignationcode through its DAO layer.
	 * 
	 * @param ndesignationcode [int] primary key of credentialdesignation object
	 * @param userInfo         [UserInfo] object holding details of loggedin user
	 * @return response entity object holding response status and data of
	 *         designation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveDesignationById(final int ndesignationcode, final UserInfo userInfo)
			throws Exception {
		final Designation designation = designationDAO.getActiveDesignationById(ndesignationcode,userInfo);
		if (designation == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(designation, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to add a new entry to credentialdesignation table through
	 * its DAO layer.
	 * 
	 * @param credentialDesignation [Designation] object holding details to be added
	 *                              in credentialdesignation table
	 * @param userInfo              [UserInfo] object holding details of loggedin
	 *                              user
	 * @return response entity object holding response status and data of added
	 *         designation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createDesignation(final Designation credentialDesignation, final UserInfo userInfo)
			throws Exception {
		return designationDAO.createDesignation(credentialDesignation, userInfo);
	}

	/**
	 * This method is used to update entry in credentialdesignation table through
	 * its DAO layer.
	 * 
	 * @param credentialDesignation [Designation] object holding details to be
	 *                              updated in credentialdesignation table
	 * @param userInfo              [UserInfo] object holding details of loggedin
	 *                              user
	 * @return response entity object holding response status and data of updated
	 *         designation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateDesignation(final Designation credentialDesignation, final UserInfo userInfo)
			throws Exception {
		return designationDAO.updateDesignation(credentialDesignation, userInfo);
	}

	/**
	 * This method is used to delete entry in credentialdesignation table through
	 * its DAO layer.
	 * 
	 * @param credentialDesignation [Designation] object holding detail to be
	 *                              deleted in credentialdesignation table
	 * @param userInfo              [UserInfo] object holding details of loggedin
	 *                              user
	 * @return response entity object holding response status and data of deleted
	 *         designation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteDesignation(final Designation credentialDesignation, final UserInfo userInfo)
			throws Exception {
		return designationDAO.deleteDesignation(credentialDesignation, userInfo);
	}
	/**
     * This method is used to retrieve all active designations for the specified site
     * code through its DAO layer.
     * 
     * @param siteCode [int] primary key of site object for which the active
     *                 designations are to be fetched
     * @return response entity object holding response status and list of all active
     *         designations
     * @throws Exception that are thrown in the DAO layer
     */
	@Transactional
	@Override
	public ResponseEntity<Object> getAllActiveDesignation(final int siteCode) throws Exception {
		return designationDAO.getAllActiveDesignation(siteCode);
	}
}
