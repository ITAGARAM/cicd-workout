package com.agaramtech.qualis.storagemanagement.service.sampleprocessing;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'storagesampleprocessing' table through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class SampleProcessingServiceImpl implements SampleProcessingService {

	private final SampleProcessingDAO sampleProcessingDAO;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class properties.
	 * @param sampleProcessingDAO SampleProcessingDAO Interface
	 */
	public SampleProcessingServiceImpl(SampleProcessingDAO sampleProcessingDAO) {
		super();
		this.sampleProcessingDAO = sampleProcessingDAO;
	}

	/**
	 * This service implementation method will access the DAO layer that is used to get all the available storagesampleprocessing with respect to site
	 * @param fromDate [String] From Date.
	 * @param toDate [String]To Date.
	 * @param currentUIDate [String] Current Date.
	 * @param nprojecttypecode [int] Project Type Code.
	 * @param userInfo [UserInfo] holding logged in user details and ntranssitecode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of storagesampleprocessing records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSampleProcessing(final String fromDate, final String toDate,
			final String currentUIDate, final UserInfo userInfo, final int nprojecttypecode) throws Exception {
		return sampleProcessingDAO.getSampleProcessing(fromDate, toDate, currentUIDate, userInfo, nprojecttypecode);
	}

	/**
	 *  This service implementation method will access the DAO layer that is used
	 *  to retrieve list of all active Barcode data for the specified project type and site.
	 * @param inputMap  [inputMap] map object with "nbarcodeLength","toDate","spositionvalue","nprojecttypecode" and "userinfo" as keys for which the data is to be fetched
	 * @return response entity object holding response status and list of all active Barcode data specified project type and site.
	 * @throws Exception that are thrown from this DAO layer.
	 */
	@Override
	public ResponseEntity<Object> getBarcodeConfigData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return sampleProcessingDAO.getBarcodeConfigData(inputMap, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to retrieve list of all active Sample Type data for the specified project type and site.
	 * @param inputMap  [inputMap] map object with "nprojecttypecode" and "userinfo" as keys for which the data is to be fetched
	 * @return response entity object holding response status and list of all active Sample Type data for the specified project type and site. 
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSampleType(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return sampleProcessingDAO.getSampleType(inputMap, userInfo);
	}
	
	/**
	 * This service implementation method will access the DAO layer that is used to retrieve list of all active Collection Tube type data for the specified project type and site.
	 * @param inputMap  [inputMap] map object with "nprojecttypecode" and "userinfo" as keys for which the data is to be fetched	
	 * @return response entity object holding response status and list of all active Collection Tube type data for the specified project type and site. 
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getCollectionTubeType(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return sampleProcessingDAO.getCollectionTubeType(inputMap, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to retrieve list of all active Sample Process Type data for the specified project type and site.
	 * @param inputMap  [inputMap] map object with "nprojecttypecode" and "userinfo" as keys for which the data is to be fetched
	 * @return response entity object holding response status and list of all active Sample Process Type data for the specified project type and site. 
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSampleProcessType(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return sampleProcessingDAO.getSampleProcessType(inputMap, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to retrieve list of all active Process Duration data for the specified project type and Sample Type and Collection tube type and site.
	 * @param inputMap  [inputMap] map object with "ncollectiontubetypecode","nsamplecollectiontypecode","nprocesstypecode","nprojecttypecode" and  "userinfo" as keys for which the data is to be fetched
	 * @return response entity object holding response status and list of all active Process Duration data for the specified project type and Sample Type and Collection tube type and site. 
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getProcessduration(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return sampleProcessingDAO.getProcessduration(inputMap, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to add a new entry to storagesampleprocessing  table.
	 * @param inputMap  [inputMap] map object with "fromDate","toDate","currentdate","nprojecttypecode","temporarystorage" and "userinfo" as keys for which the data is to be fetched
	 * 								temporarystorage [TemporaryStorage]  object holding details to be added in temporarystorage table,
	 * 								userinfo [UserInfo] holding logged in user details
	 * @return ResponseEntity with string message as 'The Barcode ID scanned already' if the barcode id already scanned / 
	 * 			list of storagesampleprocessing along with the newly added storagesampleprocessing .
	 * @throws Exception exception
	 */	
	@Transactional
	@Override
	public ResponseEntity<Object> createSampleProcessing(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return sampleProcessingDAO.createSampleProcessing(inputMap, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to update entry in storagesampleprocessing details.
	 * @param inputMap  [inputMap] map object with "fromDate","toDate","currentdate","nprojecttypecode","temporarystorage" and "userinfo" as keys for which the data is to be fetched
	 * 								temporarystorage [TemporaryStorage]  object holding details to be added in temporarystorage table,
	 * 								userinfo [UserInfo] holding logged in user details
	 * @return ResponseEntity with storagesampleprocessing object for the specified primary key / with string message as
	 * 						'Already Deleted' if the storagesampleprocessing record is not available
	 * @throws Exception exception
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateSampleProcessing(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return sampleProcessingDAO.updateSampleProcessing(inputMap, userInfo);
	}	
	/**
	 * This service implementation method will access the DAO layer that is used to retrieve a specific storagesampleprocessing record.
	 * @param inputMap  [Map] map object with "nprojecttypecode","nsampleprocessingcode" and "userinfo" as keys for which the data is to be fetched
	 * @return ResponseEntity with Temporary Storage object for the specified primary key / with string message as
	 * 						'Already Deleted' if the storagesampleprocessing record is not available
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> getActiveSampleProcessingById(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return sampleProcessingDAO.getActiveSampleProcessingById(inputMap, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to delete an entry in storagesampleprocessing table
	 * @param inputMap [Map] object with key of UserInfo object.
	 * @return ResponseEntity with string message as 'Already deleted' if the storagesampleprocessing record is not available/ 
	 * 			list of all storagesampleprocessing excluding the deleted record 
	 * @throws Exception exception
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteSampleProcessing(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return sampleProcessingDAO.deleteSampleProcessing(inputMap, userInfo);
	}

}
