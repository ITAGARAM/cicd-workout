package com.agaramtech.qualis.storagemanagement.service.samplereceiving;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;


public interface SampleReceivingService {

	
	/**
	 * This method is used to retrieve list of all active samplereceiving for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] nmastersitecode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @param nprojecttypecode [int] primary key of samplereceiving object
	 * @return response entity object holding response status and list of all active
	 *         samplereceiving
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> getSampleReceiving(String fromDate, String toDate, String currentUIDate,
			UserInfo userInfo, int nprojecttypecode) throws Exception;

	/**
	 * This method is used to retrieve list of all active Barcode data for the
	 * specified project type and site.
	 * 
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         Barcode data specified project type and site.
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> getBarcodeConfigData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to add a new entry to storagesamplereceiving
	 * table.
	 * 
	 * @param sampleReceivingObj [samplereceiving] object holding details to be added in
	 *                samplereceiving table
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         samplereceiving object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createSampleReceiving(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to retrieve active samplereceiving object
	 * based on the specified nstoragesamplereceivingcode.
	 * 
	 * @param nstoragesamplereceivingcode [int] primary key of samplereceiving object
	 * @return response entity object holding response status and data of
	 *         samplereceiving object
	 * @throws Exception that are thrown in the DAO layer
	 */

	public ResponseEntity<Object> getActiveSampleReceivingById(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in storagesamplereceiving table.
	 * 
	 * @param sampleReceivingObj [samplereceiving] object holding details to be updated
	 *                       in samplereceiving table
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         samplereceiving object
	 * @throws Exception that are thrown in the DAO layer
	 */

	public ResponseEntity<Object> updateSampleReceiving(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to delete entry in storagesamplereceiving table.
	 * 
	 * @param sampleReceivingObj [samplereceiving] object holding detail to be deleted
	 *                       in storagesamplereceiving table
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         samplereceiving object
	 * @throws Exception that are thrown in the DAO layer
	 */

	public ResponseEntity<Object> deleteSampleReceiving(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

}
