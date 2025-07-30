package com.agaramtech.qualis.barcode.service.collectiontubetype;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.barcode.model.CollectionTubeType;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'collectiontubetype'
 * table through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class CollectionTubeTypeServiceImpl implements CollectionTubeTypeService {

	private final CollectionTubeTypeDAO collectionTubeTypeDAO;
	private final CommonFunction commonFunction;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class properties.
	 * 
	 * @param collectionTubeTypeDAO CollectionTubeTypeDAO Interface
	 * @param commonFunction    CommonFunction holding common utility functions
	 */
	public CollectionTubeTypeServiceImpl(CollectionTubeTypeDAO collectionTubeTypeDAO, CommonFunction commonFunction) {
		this.collectionTubeTypeDAO = collectionTubeTypeDAO;
		this.commonFunction = commonFunction;
	}

	/**
	 * This method is used to retrieve list of all active collectiontubetype for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] primary key of site object for which the list is
	 *                 to be fetched
	 * @return response entity object holding response status and list of all active
	 *         collectiontubetype
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getCollectionTubeType(final UserInfo userInfo) throws Exception {
		return collectionTubeTypeDAO.getCollectionTubeType(userInfo);
	}

	/**
	 * This method is used to retrieve active collectiontubetype object based on the
	 * specified ncollectiontubetypecode.
	 * 
	 * @param ncollectiontubetypecode [int] primary key of collectiontubetype object
	 * @param userInfo [UserInfo] holding logged in user details
	 * @return response entity object holding response status and data of
	 *         collectiontubetype object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getActiveCollectionTubeTypeById(final int ncollectiontubetypecode,
			final UserInfo userInfo) throws Exception {
		final CollectionTubeType objCollectionTubeType = collectionTubeTypeDAO
				.getActiveCollectionTubeTypeById(ncollectiontubetypecode, userInfo);
		if (objCollectionTubeType == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objCollectionTubeType, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to add a new entry to collectiontubetype table.
	 * 
	 * @param objCollectionTubeType [CollectionTubeType] object holding details to
	 *                              be added in collectiontubetype table
	 * @param userInfo [UserInfo] holding logged in user details
	 * @return inserted collectiontubetype object with HTTP Status;
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createCollectionTubeType(final CollectionTubeType objCollectionTubeType,final UserInfo userInfo)
			throws Exception {
		return collectionTubeTypeDAO.createCollectionTubeType(objCollectionTubeType, userInfo);
	}

	/**
	 * This method is used to update entry in collectiontubetype table.
	 * 
	 * @param objCollectionTubeType [CollectionTubeType] object holding details to
	 *                              be updated in collectiontubetype table
	 * @param userInfo [UserInfo] holding logged in user details                              
	 * @return response entity object holding response status and data of updated
	 *         collectiontubetype object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateCollectionTubeType(final CollectionTubeType objCollectionTubeType,final UserInfo userInfo)
			throws Exception {
		return collectionTubeTypeDAO.updateCollectionTubeType(objCollectionTubeType, userInfo);
	}

	/**
	 * This method id used to delete an entry in collectiontubetype table
	 * 
	 * @param objCollectionTubeType [CollectionTubeType] an Object holds the record
	 *                              to be deleted
	 * @param userInfo [UserInfo] holding logged in user details                             
	 * @return a response entity with corresponding HTTP status and an
	 *         collectiontubetype object
	 * @exception Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteCollectionTubeType(final CollectionTubeType objCollectionTubeType,final UserInfo userInfo)
			throws Exception {
		return collectionTubeTypeDAO.deleteCollectionTubeType(objCollectionTubeType, userInfo);
	}

}
