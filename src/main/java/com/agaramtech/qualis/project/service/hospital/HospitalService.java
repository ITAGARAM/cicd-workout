package com.agaramtech.qualis.project.service.hospital;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.project.model.Hospital;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface declaration holds methods to perform CRUD operation on
 * 'Hospital' table
 * 
 * @author Mullai Balaji.V BGSI-4 30/06/2025
 * @version
 */

public interface HospitalService {

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * get all the available Hospitals with respect to site public interface
	 * HospitalService {
	 * 
	 * /** This service interface declaration will access the DAO layer that is used
	 * to get all the available Hospitals with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of Hospital records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */

	public ResponseEntity<Object> getHospital(UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * retrieve active Hospital object based on the specified nHospitalCode.
	 * 
	 * @param nHospitalCode [int] primary key of Hospital object
	 * @param userInfo      [UserInfo] holding logged in user details based on which
	 *                      the list is to be fetched
	 * @return response entity object holding response status and data of Hospital
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */

	public ResponseEntity<Object> getActiveHospitalById(final int nhospitalCode, UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * add a new entry to Hospital table.
	 * 
	 * @param objHospital [Hospital] object holding details to be added in Hospital
	 *                    table
	 * @param userInfo    [UserInfo] holding logged in user details and
	 *                    nmasterSiteCode [int] primary key of site object for which
	 *                    the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         Hospital object
	 * @throws Exception that are thrown in the DAO layer
	 */

	public ResponseEntity<Object> createHospital(Hospital objHospital, UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * update entry in Hospital table.
	 * 
	 * @param objHospital [Hospital] object holding details to be updated in
	 *                    Hospital table
	 * @param userInfo    [UserInfo] holding logged in user details and
	 *                    nmasterSiteCode [int] primary key of site object for which
	 *                    the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         Hospital object
	 * @throws Exception that are thrown in the DAO layer
	 */

	public ResponseEntity<Object> updateHospital(Hospital objHospital, UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * delete an entry in Hospital table.
	 * 
	 * @param objHospital [Hospital] object holding detail to be deleted from
	 *                    Hospital table
	 * @param userInfo    [UserInfo] holding logged in user details and
	 *                    nmasterSiteCode [int] primary key of site object for which
	 *                    the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         Hospital object
	 * @throws Exception that are thrown in the DAO layer
	 */

	public ResponseEntity<Object> deleteHospital(Hospital objHospital, UserInfo userInfo) throws Exception;
}
