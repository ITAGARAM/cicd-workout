package com.agaramtech.qualis.storagemanagement.service.temporarystorage;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'temporarystorage' table through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class TemporaryStorageServiceImpl implements TemporaryStorageService {

	private final TemporaryStorageDAO temporaryStorageDAO;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class properties.
	 * @param temporaryStorageDAO TemporaryStorageDAO Interface
	 */
	public TemporaryStorageServiceImpl(TemporaryStorageDAO temporaryStorageDAO) {
		super();
		this.temporaryStorageDAO = temporaryStorageDAO;
	}

	/**
	 * This service implementation method will access the DAO layer that is used 
	 * to get all the available temporarystorage with respect to site
	 * @param fromDate [String] From Date.
	 * @param toDate [String]To Date.
	 * @param currentUIDate [String] Current Date.
	 * @param nprojecttypecode [int] Project Type Code.
	 * @param userInfo [UserInfo] holding logged in user details and ntranssitecode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of temporarystorage records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getTemporaryStorage(final String fromDate, final String toDate,
			final String currentUIDate, final UserInfo userInfo, final int nprojecttypecode) throws Exception {
		return temporaryStorageDAO.getTemporaryStorage(fromDate, toDate, currentUIDate, userInfo, nprojecttypecode);
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
		return temporaryStorageDAO.getBarcodeConfigData(inputMap, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to temporarystorage  table.
	 * @param inputMap  [inputMap] map object with "fromDate","toDate","currentdate","nprojecttypecode","temporarystorage" and "userinfo" as keys for which the data is to be fetched
	 * 								temporarystorage [TemporaryStorage]  object holding details to be added in temporarystorage table,
	 * 								userinfo [UserInfo] holding logged in user details.
	 * @return response entity object holding response status and data of added temporarystorage object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createTemporaryStorage(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return temporaryStorageDAO.createTemporaryStorage(inputMap, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to retrieve active temporarystorage object 
	 * @param inputMap  [Map] map object with "nprojecttypecode","ntemporarystoragecode" and "userInfo" as keys for which the data is to be fetched
	 * @return response entity  object holding response status and data of temporarystorage object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveTemporaryStorageById(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return temporaryStorageDAO.getActiveTemporaryStorageById(inputMap, userInfo);
	}
	
	/**
	 * This service implementation method will access the DAO layer that is used to
	 *  update entry in temporarystorage  table.
	 * @param inputMap  [inputMap] map object with "fromDate","toDate","currentdate","nprojecttypecode","temporarystorage" and "userinfo" as keys for which the data is to be fetched
	 * 								temporarystorage [TemporaryStorage]  object holding details to be added in temporarystorage table,
	 * 								userinfo [UserInfo] holding logged in user details.
	 * @return response entity object holding response status and data of updated temporarystorage object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateTemporaryStorage(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return temporaryStorageDAO.updateTemporaryStorage(inputMap, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to delete an entry in temporarystorage  table.
	 * @param inputMap  [inputMap] map object with "fromDate","toDate","currentdate","nprojecttypecode","temporarystorage" and "userinfo" as keys for which the data is to be fetched
	 * 								temporarystorage [TemporaryStorage]  object holding details to be added in temporarystorage table,
	 * 								userinfo [UserInfo] holding logged in user details.
	 * @return response entity object holding response status and data of deleted temporarystorage object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteTemporaryStorage(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return temporaryStorageDAO.deleteTemporaryStorage(inputMap, userInfo);
	}
}
