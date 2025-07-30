package com.agaramtech.qualis.storagemanagement.service.samplereceiving;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class SampleReceivingServiceImpl implements SampleReceivingService {

	private final SampleReceivingDAO sampleReceivingDAO;
	
	public SampleReceivingServiceImpl(SampleReceivingDAO sampleReceivingDAO) {
		this.sampleReceivingDAO = sampleReceivingDAO;
		
	}

	
	/**
	 * This method is used to retrieve list of all active samplereceiving for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] nmastersitecode [int] primary key of site object
	 *                 for which the list is to be fetched
	 *  @param nprojecttypecode [int] primary key of samplereceiving object          
	 * @return response entity object holding response status and list of all active
	 *         samplereceiving
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSampleReceiving(final String fromDate, final String toDate,
			final String currentUIDate, final UserInfo userInfo, final int nprojecttypecode) throws Exception {
		return sampleReceivingDAO.getSampleReceiving(fromDate, toDate, currentUIDate, userInfo, nprojecttypecode);
	}
	
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

	@Override
	public ResponseEntity<Object> getBarcodeConfigData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return sampleReceivingDAO.getBarcodeConfigData(inputMap, userInfo);
	}

	
	
	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to storagesamplereceiving table.
	 * @param objsamplereceiving [samplereceiving] object holding details to be added in samplereceiving table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of added samplereceiving object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createSampleReceiving(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return sampleReceivingDAO.createSampleReceiving(inputMap, userInfo);
	}

	/**
	 * This method is used to retrieve active samplereceiving object based on the
	 * specified nstoragesamplereceivingcode.
	 * 
	 * @param nstoragesamplereceivingcode [int] primary key of samplereceiving object
	 * @return response entity object holding response status and data of
	 *         samplereceiving object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getActiveSampleReceivingById(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return sampleReceivingDAO.getActiveSampleReceivingById(inputMap, userInfo);
	}

	/**
	 * This method is used to update entry in storagesamplereceiving table.
	 * 
	 * @param objsamplereceiving [samplereceiving] object holding details to be updated in
	 *                       storagesamplereceiving table
	 * @return response entity object holding response status and data of updated
	 *         samplereceiving object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateSampleReceiving(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return sampleReceivingDAO.updateSampleReceiving(inputMap, userInfo);
	}

	/**
	 * This method id used to delete an entry in storagesamplereceiving table
	 * 
	 * @param objsamplereceiving [samplereceiving] an Object holds the record to be deleted
	 * @return a response entity with corresponding HTTP status and an samplereceiving
	 *         object
	 * @exception Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteSampleReceiving(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return sampleReceivingDAO.deleteSampleReceiving(inputMap, userInfo);
	}

}
