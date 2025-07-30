package com.agaramtech.qualis.barcode.service.samplecycle;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.barcode.model.SampleCycle;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation on 'samplecycle'
 * table
 */
public interface SampleCycleDAO {
	/**
	 * This interface declaration is used to get the over all SampleCycle with
	 * respect to Project Type
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of Sample Cycle with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */

	public ResponseEntity<Object> getSampleCycle(final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to add a new entry to SampleCycle table.
	 * 
	 * @param sampleCycle [SampleCycle] object holding details to be added in Sample
	 *                    Cycle table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         Sample Cycle object
	 * @throws Exception that are thrown in the DAO layer
	 */

	public ResponseEntity<Object> createSampleCycle(final SampleCycle sampleCycle, final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to retrieve active sampleCycle object
	 * based on the specified nsamplecyclecode.
	 * 
	 * @param nsamplecyclecode [int] primary key of sampleCycle object
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         sampleCycle object
	 * @throws Exception that are thrown in the DAO layer
	 */

	public SampleCycle getActiveSampleCycleById(final int nsamplecyclecode, final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in Sample Cycle table.
	 * 
	 * @param sampleCycle [SampleCycle] object holding details to be updated in
	 *                    SampleCycle table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         SampleCycle object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateSampleCycle(final SampleCycle sampleCycle, final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to delete entry in SampleCycle table.
	 * 
	 * @param samplecycle [SampleCycle] object holding detail to be deleted in
	 *                    SampleCycle table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         SampleCycle object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteSampleCycle(final SampleCycle samplecycle, final UserInfo userInfo)
			throws Exception;

}
