package com.agaramtech.qualis.basemaster.service.storagecondition;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.basemaster.model.StorageCondition;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

import lombok.RequiredArgsConstructor;

/**
 * This class holds methods to perform CRUD operation on 'storagecondition'
 * table through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class StorageConditionServiceImpl implements StorageConditionService {

	private final StorageConditionDAO storageconditionDAO;
	private final CommonFunction commonFunction;

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available storageConditions with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of storageCondition records
	 *         with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getStorageCondition(final UserInfo userInfo) throws Exception {
		return storageconditionDAO.getStorageCondition(userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * retrieve active storageCondition object based on the specified
	 * nstorageconditionCode.
	 * 
	 * @param nstorageconditionCode [int] primary key of storageCondition object
	 * @param userInfo              [UserInfo] holding logged in user details based
	 *                              on which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         storageCondition object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveStorageConditionById(final int nstorageconditioncode,
			final UserInfo userInfo) throws Exception {
		final StorageCondition storagecondition = (StorageCondition) storageconditionDAO
				.getActiveStorageConditionById(nstorageconditioncode);
		if (storagecondition == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(storagecondition, HttpStatus.OK);
		}
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to storageCondition table.
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
	@Transactional
	@Override
	public ResponseEntity<Object> createStorageCondition(final StorageCondition basemasterStorageCondition, final UserInfo userInfo)
			throws Exception {
		return storageconditionDAO.createStorageCondition(basemasterStorageCondition, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * update entry in storageCondition table.
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
	@Transactional
	@Override
	public ResponseEntity<Object> updateStorageCondition(final StorageCondition basemasterStorageCondition, final UserInfo userInfo)
			throws Exception {
		return storageconditionDAO.updateStorageCondition(basemasterStorageCondition, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * delete an entry in storageCondition table.
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
	@Transactional
	@Override
	public ResponseEntity<Object> deleteStorageCondition(final StorageCondition basemasterStorageCondition, final UserInfo userInfo)
			throws Exception {
		return storageconditionDAO.deleteStorageCondition(basemasterStorageCondition, userInfo);
	}

}
