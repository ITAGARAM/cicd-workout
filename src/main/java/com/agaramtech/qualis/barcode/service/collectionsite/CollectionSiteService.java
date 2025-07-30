package com.agaramtech.qualis.barcode.service.collectionsite;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.barcode.model.CollectionSite;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface declaration holds methods to perform CRUD operation on 'CollectionSite' table
 */

public interface CollectionSiteService {
	/**
	 * This interface declaration is used to get the over all CollectionSite with respect to site
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of CollectionSite with respect to site and also have the HTTP response code 
	 * @throws Exception
	 */
	public ResponseEntity<Object> getCollectionSite(final UserInfo userInfo) throws Exception;
	/**
	 * This interface declaration is used to update entry in CollectionSite  table.
	 * @param collection Site [Collection Site] object holding details to be updated in CollectionSite table
	 * @return response entity object holding response status and data of updated CollectionSite object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createCollectionSite(final CollectionSite collectionSite, UserInfo userInfo) throws Exception;
	/**
	 * This interface declaration is used to retrieve active CollectionSite object based
	 * on the specified ncollectionsitecode.
	 * @param ncollectionsitecode [int] primary key of Collection Site object
	 * @return response entity  object holding response status and data of CollectionSite object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveCollectionSiteById(final int ncollectionsitecode, UserInfo userInfo) throws Exception;
	/**
	 * This interface declaration is used to update entry in Collection Site  table.
	 * @param collection Site [Collection Site] object holding details to be updated in Collection Site table
	 * @return response entity object holding response status and data of updated Collection Site object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateCollectionSite(final CollectionSite collectionSite, UserInfo userInfo) throws Exception;
	/**
	 * This interface declaration is used to delete entry in Collection Site  table.
	 * @param collection Site [Collection Site] object holding detail to be deleted in Collection Site table
	 * @return response entity object holding response status and data of deleted Collection Site object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteCollectionSite(final CollectionSite collectionSite, UserInfo userInfo)throws Exception;
}
