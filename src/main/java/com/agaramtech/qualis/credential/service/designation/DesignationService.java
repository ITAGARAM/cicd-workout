package com.agaramtech.qualis.credential.service.designation;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.credential.model.Designation;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface declaration holds methods to perform CRUD operation on 'credentialdesignation' table
 * through its DAO layer.
 * @author ATE184
 * @version 9.0.0.1
 * @since   26- Jun- 2020
 */

public interface DesignationService {

	/**
	 * This interface declaration is used to retrieve list of all active designation for the
	 * specified site through its DAO layer
	 * @param nmasterSiteCode [int] primary key of site object for which the list is to be fetched
	 * @return response entity  object holding response status and list of all active designation
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getDesignation(final UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to retrieve active credentialdesignation object based
	 * on the specified ndesignationcode through its DAO layer.
	 * @param ndesignationcode [int] primary key of credentialdesignation object
	 * @param siteCode [int] primary key of site object 
	 * @return response entity  object holding response status and data of designation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveDesignationById(final int ndesignationcode,final  UserInfo userInfo) throws Exception ;

	/**
	 * This interface declaration is used to add a new entry to credentialdesignation  table through its DAO layer.
	 * @param credentialDesignation [Designation] object holding details to be added in credentialdesignation table
	 * @return response entity object holding response status and data of added designation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createDesignation(final Designation credentialDesignation,final  UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to update entry in credentialdesignation  table through its DAO layer.
	 * @param credentialDesignation [Designation] object holding details to be updated in credentialdesignation table
	 * @return response entity object holding response status and data of updated designation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateDesignation(final Designation credentialDesignation,final  UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to delete entry in credentialdesignation  table through its DAO layer.
	 * @param credentialDesignation [Designation] object holding detail to be deleted in credentialdesignation table
	 * @return response entity object holding response status and data of deleted designation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteDesignation(final Designation credentialDesignation,final  UserInfo userInfo) throws Exception;
	

	public ResponseEntity<Object> getAllActiveDesignation(final int siteCode) throws Exception;
}
