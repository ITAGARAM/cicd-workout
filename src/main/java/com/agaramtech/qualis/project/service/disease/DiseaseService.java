package com.agaramtech.qualis.project.service.disease;

import org.springframework.http.ResponseEntity;	
import com.agaramtech.qualis.project.model.Disease;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface declaration holds methods to perform CRUD operation on
 * 'Disease' table
 * @author Mullai Balaji.V 
 * BGSI-2
 * 26/06/2025 
 */
public interface DiseaseService {

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * get all the available Diseases with respect to site public interface
	 * DiseaseService {
	 * 
	 * /** This service interface declaration will access the DAO layer that is used
	 * to get all the available Diseases with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of Disease records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getDisease(UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * retrieve active Disease object based on the specified nDiseaseCode.
	 * 
	 * @param nDiseaseCode [int] primary key of Disease object
	 * @param userInfo     [UserInfo] holding logged in user details based on which
	 *                     the list is to be fetched
	 * @return response entity object holding response status and data of Disease
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveDiseaseById(final int ndiseaseCode, UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * add a new entry to Disease table.
	 * 
	 * @param objDisease [Disease] object holding details to be added in Disease
	 *                   table
	 * @param userInfo   [UserInfo] holding logged in user details and
	 *                   nmasterSiteCode [int] primary key of site object for which
	 *                   the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         Disease object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createDisease(Disease objDisease, UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * update entry in Disease table.
	 * 
	 * @param objDisease [Disease] object holding details to be updated in Disease
	 *                   table
	 * @param userInfo   [UserInfo] holding logged in user details and
	 *                   nmasterSiteCode [int] primary key of site object for which
	 *                   the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         Disease object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateDisease(Disease objDisease, UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * delete an entry in Disease table.
	 * 
	 * @param objDisease [Disease] object holding detail to be deleted from Disease
	 *                   table
	 * @param userInfo   [UserInfo] holding logged in user details and
	 *                   nmasterSiteCode [int] primary key of site object for which
	 *                   the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         Disease object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteDisease(Disease objDisease, UserInfo userInfo) throws Exception;
}
