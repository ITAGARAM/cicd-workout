package com.agaramtech.qualis.organization.service.lab;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.organization.model.Lab;

/**
 * This interface declaration holds methods to perform CRUD operation on Lab
 * table
 */

public interface LabService {

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * get all the available Labs with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of Lab records with respect to
	 *         site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getLab(final int nmasterSiteCode) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * add a new entry to Lab table.
	 * 
	 * @param objLab[Lab] object holding details to be added in Lab table
	 * @param userInfo    [UserInfo] holding logged in user details and
	 *                    nmasterSiteCode [int] primary key of site object for which
	 *                    the list is to be fetched
	 * @return response entity object holding response status and data of added Lab
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createLab(Lab lab, UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * update entry in Lab table.
	 * 
	 * @param objLab[Lab] object holding details to be updated in Lab table
	 * @param userInfo    [UserInfo] holding logged in user details and
	 *                    nmasterSiteCode [int] primary key of site object for which
	 *                    the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         Lab object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateLab(Lab lab, UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * retrieve active Lab object based on the specified nLabCode.
	 * 
	 * @param nLabCode [int] primary key of Lab object
	 * @param userInfo [UserInfo] holding logged in user details based on which the
	 *                 list is to be fetched
	 * @return response entity object holding response status and data of Lab object
	 * @throws Exception that are thrown in the DAO layer
	 */

	public ResponseEntity<Object> getActiveLabById(final int nlabcode, final UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * delete an entry in Lab table.
	 * 
	 * @param objLab   [Lab] object holding detail to be deleted from Lab table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and data of deleted
	 *         Lab object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteLab(Lab lab, UserInfo userInfo) throws Exception;
}
