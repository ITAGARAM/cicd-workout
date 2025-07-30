
package com.agaramtech.qualis.project.service.hospital;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.project.model.Hospital;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation on 'hospital'
 * 
 * @author Mullai Balaji.V BGSI-4 30/06/2025
 * @version
 */

public interface HospitalDAO {

	/**
	 * This interface declaration is used to get all the available hospitals with
	 * respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of hospital records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */

	public ResponseEntity<Object> getHospital(UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active hospital object based
	 * on the specified nhospitalCode.
	 * 
	 * @param nhospitalCode [int] primary key of hospital object
	 * @param userInfo      [UserInfo] holding logged in user details based on which
	 *                      the list is to be fetched
	 * @return response entity object holding response status and data of hospital
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */

	public Hospital getActiveHospitalById(final int nhospitalCode, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to add a new entry to hospital table.
	 * 
	 * @param objhospital [hospital] object holding details to be added in hospital
	 *                    table
	 * @param userInfo    [UserInfo] holding logged in user details and
	 *                    nmasterSiteCode [int] primary key of site object for which
	 *                    the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         hospital object
	 * @throws Exception that are thrown in the DAO layer
	 */

	public ResponseEntity<Object> createHospital(Hospital objHospital, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in hospital table.
	 * 
	 * @param objhospital [hospital] object holding details to be updated in
	 *                    hospital table
	 * @param userInfo    [UserInfo] holding logged in user details and
	 *                    nmasterSiteCode [int] primary key of site object for which
	 *                    the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         hospital object
	 * @throws Exception that are thrown in the DAO layer
	 */

	public ResponseEntity<Object> updateHospital(Hospital objHospital, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to delete an entry in hospital table.
	 * 
	 * @param objhospital [hospital] object holding detail to be deleted from
	 *                    hospital table
	 * @param userInfo    [UserInfo] holding logged in user details and
	 *                    nmasterSiteCode [int] primary key of site object for which
	 *                    the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         hospital object
	 * @throws Exception that are thrown in the DAO layer
	 */

	public ResponseEntity<Object> deleteHospital(Hospital objHospital, UserInfo userInfo) throws Exception;

}
