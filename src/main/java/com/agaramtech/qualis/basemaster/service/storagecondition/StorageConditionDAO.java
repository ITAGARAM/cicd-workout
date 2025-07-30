package com.agaramtech.qualis.basemaster.service.storagecondition;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.basemaster.model.StorageCondition;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation on
 * 'storageCondition' table
 */
public interface StorageConditionDAO {

	/**
	 * This interface declaration is used to get all the available storageConditions
	 * with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of storageCondition records
	 *         with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getStorageCondition(final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active storageCondition object
	 * based on the specified nstorageConditionCode.
	 * 
	 * @param nstorageconditioncode [int] primary key of storageCondition object
	 * @param userInfo              [UserInfo] holding logged in user details based
	 *                              on which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         storageCondition object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public StorageCondition getActiveStorageConditionById(final int nstorageconditioncode)
			throws Exception;

	/**
	 * This interface declaration is used to add a new entry to storageCondition
	 * table.
	 * 
	 * @param objStorageCondition [StorageCondition] object holding details to be
	 *                            added in storageCondition table
	 * @param userInfo            [UserInfo] holding logged in user details and
	 *                            nmasterSiteCode [int] primary key of site object
	 *                            for which the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         storageCondition object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createStorageCondition(final StorageCondition basemasterStorageCondition, final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to update entry in storageCondition table.
	 * 
	 * @param objStorageCondition [StorageCondition] object holding details to be
	 *                            updated in storageCondition table
	 * @param userInfo            [UserInfo] holding logged in user details and
	 *                            nmasterSiteCode [int] primary key of site object
	 *                            for which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         storageCondition object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateStorageCondition(final StorageCondition basemasterStorageCondition, final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to delete an entry in storageCondition
	 * table.
	 * 
	 * @param objStorageCondition [StorageCondition] object holding detail to be
	 *                            deleted from storageCondition table
	 * @param userInfo            [UserInfo] holding logged in user details and
	 *                            nmasterSiteCode [int] primary key of site object
	 *                            for which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         storageCondition object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteStorageCondition(final StorageCondition basemasterStorageCondition, final UserInfo userInfo)
			throws Exception;

}
