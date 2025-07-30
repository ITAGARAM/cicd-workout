package com.agaramtech.qualis.storagemanagement.service.temporarystorage;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation on 'temporarystorage' table
 */
public interface TemporaryStorageDAO {
	
	/**
	 * This DAO interface declaration will access the DAO layer that is used 
	 * to get all the available temporarystorage with respect to site
	 * @param fromDate [String] From Date.
	 * @param toDate [String]To Date.
	 * @param currentUIDate [String] Current Date.
	 * @param nprojecttypecode [int] Project Type Code.
	 * @param userInfo [UserInfo] ntranssitecode [int] primary key of site object for which the list is to be fetched
	 * @param userInfo [UserInfo] holding logged in user details and ntranssitecode [int] primary key of site object for  which the list is to be fetched
	 * @return a response entity which holds the list of temporarystorage records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	ResponseEntity<Object> getTemporaryStorage(final String fromDate, String toDate, String currentUIDate,
			UserInfo userInfo, int nprojecttypecode) throws Exception;

	/**
	 * This DAO interface declaration will access the DAO layer that is used to retrieve list of all active Barcode data for the specified project type and site.
	 * @param inputMap  [inputMap] map object with "nbarcodeLength","toDate","spositionvalue","nprojecttypecode" and "userinfo" as keys for which the data is to be fetched.
	 * @return response entity object holding response status and list of all active Barcode data specified project type and site.
	 * @throws Exception that are thrown from this DAO layer
	 */
	ResponseEntity<Object> getBarcodeConfigData(final Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	/**
	 * This DAO interface declaration will access the DAO layer that is used to add a new entry to temporarystorage  table.
	 * Need to check for duplicate entry of sbarcodeid the specified site.	 
	 *  @param inputMap  [inputMap] map object with "fromDate","toDate","currentdate","nprojecttypecode","temporarystorage" and "userinfo" as keys for which the data is to be fetched
	 * 								temporarystorage [TemporaryStorage]  object holding details to be added in temporarystorage table,
	 * 								userinfo [UserInfo] holding logged in user details.
	 * @return response entity object holding response status and data of added temporarystorage object
	 * @throws Exception that are thrown in the DAO layer
	 */
	ResponseEntity<Object> createTemporaryStorage(final Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	/**
	 * This DAO interface declaration will access the DAO layer that is used to retrieve active temporarystorage table.
	 * @param inputMap  [Map] map object with "nprojecttypecode","ntemporarystoragecode" and "userInfo" as keys for which the data is to be fetched
	 * @return response entity  object holding response status and data of temporarystorage object
	 * @throws Exception that are thrown in the DAO layer
	 */
	ResponseEntity<Object> getActiveTemporaryStorageById(final Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	/**
	 * This DAO interface declaration will access the DAO layer that is used to update entry in temporarystorage  table.
	 * @param inputMap  [Map] map object with "nprojecttypecode","ntemporarystoragecode" and "userInfo" as keys for which the data is to be fetched
	 * @return response entity object holding response status and data of updated temporarystorage object
	 * @throws Exception that are thrown in the DAO layer
	 */
	ResponseEntity<Object> updateTemporaryStorage(final Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	/**
	 * This DAO interface declaration will access the DAO layer that is used to delete an entry in temporarystorage  table.
	 * @param inputMap  [inputMap] map object with "fromDate","toDate","currentdate","nprojecttypecode","temporarystorage" and "userinfo" as keys for which the data is to be fetched
	 * 								temporarystorage [TemporaryStorage]  object holding details to be added in temporarystorage table,
	 * 								userinfo [UserInfo] holding logged in user details.
	 * @return response entity object holding response status and data of deleted temporarystorage object
	 * @throws Exception that are thrown in the DAO layer
	 */
	ResponseEntity<Object> deleteTemporaryStorage(final Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

}
