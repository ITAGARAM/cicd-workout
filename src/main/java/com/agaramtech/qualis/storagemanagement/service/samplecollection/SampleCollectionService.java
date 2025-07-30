package com.agaramtech.qualis.storagemanagement.service.samplecollection;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface declaration holds methods to perform CRUD operation on 'storagesamplecollection' table
 */
public interface SampleCollectionService {
	
	/**
	 * This service interface declaration will access the DAO layer that is used 
	 * to get all the available storagesamplecollections with respect to site
	 * @param fromDate [String] holding from-date which list the sample collection from the from-date.
	 * @param toDate [String] holding to-date which list the sample collection till the to-date.
	 * @param currentUIDate [String] holding the current UI date.
	 * @param nprojecttypecode [int] holding the primary key of the project type of the sample collections.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of sampleCollection records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	ResponseEntity<Object> getSampleCollection(String fromDate, String toDate, String currentUIDate, UserInfo userInfo,
			int nprojecttypecode) throws Exception;

	/**
	 * This method is used to retrieve list of all active Barcode data for the
	 * specified project type and site.
	 * 
	 * @param inputMap [Map] map object with nprojecttypecode [int] which hold the primary key of the project type of the sample collection and
	 * 				userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for which the list is to be fetched
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         Barcode data specified project type and site.
	 * @throws Exception exception
	 */
	ResponseEntity<Object> getBarcodeConfigData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * add a new entry to storagesamplecollection table
	 * @param inputMap [Map] map object holding details to be added in storagesamplecollection table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of added samplecollection object
	 * @throws Exception that are thrown in the DAO layer
	 */
	ResponseEntity<Object> createSampleCollection(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to retrieve active samplecollection object based
	 * on the specified nsamplecollectioncode and nprojecttypecode
	 * @param inputMap [Map] map object holding primary key of sample collection and the projecttype primary key.
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of sampleCollection object
	 * @throws Exception that are thrown in the DAO layer
	 */
	ResponseEntity<Object> getActiveSampleCollectionById(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 *  update entry in storagesamplecollection table
	 * @param inputMap [Map] map object holding details to be updated in storagesamplecollection table.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated sampleCollection object
	 * @throws Exception that are thrown in the DAO layer
	 */
	ResponseEntity<Object> updateSampleCollection(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to delete an entry in storagesamplecollection table.
	 * @param inputMap [Map] map object holding detail to be deleted from storagesamplecollection table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted sampleCollection object
	 * @throws Exception that are thrown in the DAO layer
	 */
	ResponseEntity<Object> deleteSampleCollection(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

}
