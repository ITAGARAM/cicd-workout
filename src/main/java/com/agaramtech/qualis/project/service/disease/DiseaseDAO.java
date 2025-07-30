package com.agaramtech.qualis.project.service.disease;

import org.springframework.http.ResponseEntity;	
import com.agaramtech.qualis.project.model.Disease;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation on 'disease'
 * @author Mullai Balaji.V 
 * BGSI-2
 * 26/06/2025 
 */
public interface DiseaseDAO {

	/**
	 * This interface declaration is used to get all the available diseases with
	 * respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of disease records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getDisease(UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active disease object based on
	 * the specified ndiseaseCode.
	 * 
	 * @param ndiseaseCode [int] primary key of disease object
	 * @param userInfo     [UserInfo] holding logged in user details based on which
	 *                     the list is to be fetched
	 * @return response entity object holding response status and data of disease
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public Disease getActiveDiseaseById(final int ndiseaseCode, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to add a new entry to disease table.
	 * 
	 * @param objdisease [disease] object holding details to be added in disease
	 *                   table
	 * @param userInfo   [UserInfo] holding logged in user details and
	 *                   nmasterSiteCode [int] primary key of site object for which
	 *                   the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         disease object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createDisease(Disease objDisease, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in disease table.
	 * 
	 * @param objdisease [disease] object holding details to be updated in disease
	 *                   table
	 * @param userInfo   [UserInfo] holding logged in user details and
	 *                   nmasterSiteCode [int] primary key of site object for which
	 *                   the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         disease object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateDisease(Disease objDisease, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to delete an entry in disease table.
	 * 
	 * @param objdisease [disease] object holding detail to be deleted from disease
	 *                   table
	 * @param userInfo   [UserInfo] holding logged in user details and
	 *                   nmasterSiteCode [int] primary key of site object for which
	 *                   the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         disease object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteDisease(Disease objDisease, UserInfo userInfo) throws Exception;

}
