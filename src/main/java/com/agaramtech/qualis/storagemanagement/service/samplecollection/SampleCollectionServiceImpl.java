package com.agaramtech.qualis.storagemanagement.service.samplecollection;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'storagesamplecollection' table through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class SampleCollectionServiceImpl implements SampleCollectionService {

	private final SampleCollectionDAO sampleCollectionDAO;

	public SampleCollectionServiceImpl(SampleCollectionDAO sampleCollectionDAO) {
		super();
		this.sampleCollectionDAO = sampleCollectionDAO;
	}

	/**
	 * This service implementation method will access the DAO layer that is used 
	 * to get all available SampleCollections with respect to site
	 * @param fromDate [String] holding from-date which list the sample collection from the from-date.
	 * @param toDate [String] holding to-date which list the sample collection till the to-date.
	 * @param currentUIDate [String] holding the current UI date.
	 * @param nprojecttypecode [int] holding the primary key of the project type of the sample collections.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of SampleCollection records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSampleCollection(final String fromDate, final String toDate,
			final String currentUIDate, final UserInfo userInfo, final int nprojecttypecode) throws Exception {
		return sampleCollectionDAO.getSampleCollection(fromDate, toDate, currentUIDate, userInfo, nprojecttypecode);
	}

	/**
	 * This service implementation method will access the DAO layer that is used 
	 * to get all the available barcodes with respect to site
	 * @param inputMap [Map] map object with nprojecttypecode [int] which hold the primary key of the project type of the sample collection and
	 * 				userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for which the list is to be fetched
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return a response entity which holds the list of Barcode records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getBarcodeConfigData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return sampleCollectionDAO.getBarcodeConfigData(inputMap, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to storagesamplecollection table
	 * @param inputMap [Map] map object holding details to be added in storagesamplecollection table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of added samplecollection object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createSampleCollection(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return sampleCollectionDAO.createSampleCollection(inputMap, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to retrieve active samplecollection object based
	 * on the specified nsamplecollectioncode and nprojecttypecode
	 * @param inputMap [Map] map object holding primary key of sample collection and the projecttype primary key.
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of sampleCollection object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveSampleCollectionById(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return sampleCollectionDAO.getActiveSampleCollectionById(inputMap, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 *  update entry in storagesamplecollection table
	 * @param inputMap [Map] map object holding details to be updated in storagesamplecollection table.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated sampleCollection object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateSampleCollection(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return sampleCollectionDAO.updateSampleCollection(inputMap, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to delete an entry in storagesamplecollection table.
	 * @param inputMap [Map] map object holding detail to be deleted from storagesamplecollection table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted sampleCollection object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteSampleCollection(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return sampleCollectionDAO.deleteSampleCollection(inputMap, userInfo);
	}
}
