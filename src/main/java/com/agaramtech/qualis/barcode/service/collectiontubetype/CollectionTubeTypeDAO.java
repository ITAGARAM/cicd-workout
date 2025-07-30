package com.agaramtech.qualis.barcode.service.collectiontubetype;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.barcode.model.CollectionTubeType;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation on
 * 'collectiontubetype' table
 * 
 * @author ATE236
 * @version 10.0.0.2
 */
public interface CollectionTubeTypeDAO {
	/**
	 * This interface declaration is used to get the over all collectiontubetype
	 * with respect to site
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of collectiontubetype with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getCollectionTubeType(final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active collectiontubetype
	 * object based on the specified ncollectiontubetypecode.
	 * 
	 * @param ncollectiontubetypecode [int] primary key of collectiontubetype object
	 * @param userInfo [UserInfo] holding logged in user details
	 * @return response entity object holding response status and data of
	 *         collectiontubetype object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public CollectionTubeType getActiveCollectionTubeTypeById(final int ncollectiontubetypecode,
			final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to add a new entry to collectiontubetype
	 * table.
	 * 
	 * @param objCollectionTubeType [CollectionTubeType] object holding details to
	 *                              be added in collectiontubetype table
	 * @param userInfo [UserInfo] holding logged in user details                             
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createCollectionTubeType(final CollectionTubeType objCollectionTubeType,
			final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in collectiontubetype
	 * table.
	 * 
	 * @param objCollectionTubeType [CollectionTubeType] object holding details to
	 *                              be updated in collectiontubetype table
	 * @param userInfo [UserInfo] holding logged in user details                             
	 * @return response entity object holding response status and data of updated
	 *         collectiontubetype object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateCollectionTubeType(final CollectionTubeType objCollectionTubeType,
			final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to delete entry in collectiontubetype
	 * table.
	 * 
	 * @param objCollectionTubeType [CollectionTubeType] object holding detail to be
	 *                              deleted in studyidentity table
	 * @param userInfo [UserInfo] holding logged in user details                             
	 * @return response entity object holding response status and data of deleted
	 *         collectiontubetype object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteCollectionTubeType(final CollectionTubeType objCollectionTubeType,
			final UserInfo userInfo) throws Exception;

}
