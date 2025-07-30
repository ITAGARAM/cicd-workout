package com.agaramtech.qualis.credential.service.designation;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.credential.model.Designation;
import com.agaramtech.qualis.global.UserInfo;



/**
 * This interface holds declarations to perform CRUD operation on 'credentialdesignation' table
 * through its implementation class.
 * @author ATE153
 * @version 9.0.0.1
 * @since   26- Jun- 2020
 */
public interface DesignationDAO {
	
	/**
	 * This interface declaration is used to retrieve list of all active designation for the
	 * specified site.
	 * @param nmasterSiteCode [int] primary key of site object for which the list is to be fetched
	 * @return response entity  object holding response status and list of all active designation
	 * @throws Exception that are thrown in the DAO layer
	 */
	
	public ResponseEntity<Object> getDesignation(final UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to retrieve active credentialdesignation object based
	 * on the specified ndesignationcode.
	 * @param ndesignationcode [int] primary key of credentialdesignation object
	 * @return response entity  object holding response status and data of designation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public Designation getActiveDesignationById(final int ndesignationcode,final UserInfo userInfo) throws Exception ;
 
	/**
	 * This interface declaration is used to add a new entry to credentialdesignation  table.
	 * @param credentialDesignation [Designation] object holding details to be added in credentialdesignation table
	 * @return response entity object holding response status and data of added designation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createDesignation(final Designation credentialDesignation,final  UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to update entry in credentialdesignation  table.
	 * @param credentialDesignation [Designation] object holding details to be updated in credentialdesignation table
	 * @return response entity object holding response status and data of updated designation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateDesignation(final Designation credentialDesignation, final UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to delete entry in credentialdesignation  table.
	 * @param credentialDesignation [Designation] object holding detail to be deleted in credentialdesignation table
	 * @return response entity object holding response status and data of deleted designation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteDesignation(final Designation credentialDesignation, final UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to retrieve active designation object based
	 * on the specified ndesignationCode.
	 * @param ndesignationCode [int] primary key of designation object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and data of designation object
	 * @throws Exception that are thrown in the DAO layer
	 */

	public ResponseEntity<Object> getAllActiveDesignation(final int siteCode) throws Exception;
}
