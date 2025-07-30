package com.agaramtech.qualis.barcode.service.collectionsite;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.barcode.model.CollectionSite;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation on
 * 'CollectionSite' table
 */

public interface CollectionSiteDAO {
	/**
	 * This interface declaration is used to get the over all Collection Site with
	 * respect to Project Type
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of Collection Site with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getCollectionSite(UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to add a new entry to Collection Site
	 * table.
	 * 
	 * @param collection Site [Collection Site] object holding details to be added
	 *                   in Collection Site table
	 * @return response entity object holding response status and data of added
	 *         Collection Site object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createCollectionSite(CollectionSite collectionSite, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to retrieve active Collection Site object
	 * based on the specified ncollectionsitecode.
	 * 
	 * @param ncollectionsitecode [int] primary key of Project Type object
	 * @return response entity object holding response status and data of Collection
	 *         Site object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public CollectionSite getActiveCollectionSiteById(int ncollectionsitecode, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in Collection Site table.
	 * 
	 * @param collection Site [Collection Site] object holding details to be updated
	 *                   in Collection Site table
	 * @return response entity object holding response status and data of updated
	 *         Collection Site object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateCollectionSite(CollectionSite collectionSite, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to delete entry in Collection Site table.
	 * 
	 * @param collection Site [Collection Site] object holding detail to be deleted
	 *                   in Collection Site table
	 * @return response entity object holding response status and data of deleted
	 *         Collection Site object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteCollectionSite(CollectionSite collectionSite, UserInfo userInfo)
			throws Exception;

}
