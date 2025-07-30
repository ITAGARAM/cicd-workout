package com.agaramtech.qualis.basemaster.service.storagecondition;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.basemaster.model.StorageCondition;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface declaration holds methods to perform CRUD operation on
 * 'storagecondition' table
 */
public interface StorageConditionService {

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * get all the available storageconditions with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of storagecondition records
	 *         with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getStorageCondition(final UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * retrieve active storagecondition object based on the specified
	 * nstorageconditionCode.
	 * 
	 * @param nstorageconditionCode [int] primary key of storagecondition object
	 * @param userInfo              [UserInfo] holding logged in user details based
	 *                              on which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         storagecondition object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveStorageConditionById(final int nstorageconditioncode,
			final UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * add a new entry to storagecondition table.
	 * 
	 * @param objStorageCondition [StorageCondition] object holding details to be
	 *                            added in storagecondition table
	 * @param userInfo            [UserInfo] holding logged in user details and
	 *                            nmasterSiteCode [int] primary key of site object
	 *                            for which the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         storagecondition object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createStorageCondition(final StorageCondition basemasterStorageCondition,
			final UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * update entry in storagecondition table.
	 * 
	 * @param objStorageCondition [StorageCondition] object holding details to be
	 *                            updated in storagecondition table
	 * @param userInfo            [UserInfo] holding logged in user details and
	 *                            nmasterSiteCode [int] primary key of site object
	 *                            for which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         storagecondition object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateStorageCondition(final StorageCondition basemasterStorageCondition,
			final UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * delete an entry in storagecondition table.
	 * 
	 * @param objStorageCondition [StorageCondition] object holding detail to be
	 *                            deleted from storagecondition table
	 * @param userInfo            [UserInfo] holding logged in user details and
	 *                            nmasterSiteCode [int] primary key of site object
	 *                            for which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         storagecondition object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteStorageCondition(final StorageCondition basemasterStorageCondition,
			final UserInfo userInfo) throws Exception;
}
