package com.agaramtech.qualis.barcode.service.samplecollectiontype;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.barcode.model.SampleCollectionType;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface declaration holds methods to perform CRUD operation on
 * 'samplecollectiontype' table
 * 
 */
public interface SampleCollectionTypeService {
	/**
	 * This interface declaration is used to get the over all samplecollectiontype
	 * with respect to site
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of samplecollectiontype with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getSampleCollectionType(final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active samplecollectiontype
	 * object based on the specified ncollectiontubetypecode.
	 * 
	 * @param nsamplecollectiontypecode [int] primary key of CollectionTubeType
	 *                                  object
	 * @param userInfo [UserInfo]
	 * @return response entity object holding response status and data of
	 *         CollectionTubeType object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveSampleCollectionTypeById(final int nsamplecollectiontypecode,
			final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to add a new entry to samplecollectiontype
	 * table.
	 * 
	 * @param objSampleCollectionType [SampleCollectionType] object holding details
	 *                                to be added in samplecollectiontype table
	 * @param userInfo [UserInfo]
	 * @return response entity object holding response status and data of added
	 *         samplecollectiontype object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createSampleCollectionType(final SampleCollectionType objSampleCollectionType,
			final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in samplecollectiontype
	 * table.
	 * 
	 * @param objSampleCollectionType [SampleCollectionType] object holding details
	 *                                to be updated in samplecollectiontype table
	 * @param userInfo [UserInfo]
	 * @return response entity object holding response status and data of updated
	 *         samplecollectiontype object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateSampleCollectionType(final SampleCollectionType objSampleCollectionType,
			final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to delete entry in samplecollectiontype
	 * table.
	 * 
	 * @param objSampleCollectionType [SampleCollectionType] object holding detail
	 *                                to be deleted in samplecollectiontype table
	 * @param userInfo [UserInfo]
	 * @return response entity object holding response status and data of
	 *         samplecollectiontype object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteSampleCollectionType(final SampleCollectionType objSampleCollectionType,
			final UserInfo userInfo) throws Exception;
}
