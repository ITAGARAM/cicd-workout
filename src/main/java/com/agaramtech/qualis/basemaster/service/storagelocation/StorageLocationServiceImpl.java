package com.agaramtech.qualis.basemaster.service.storagelocation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.basemaster.model.StorageLocation;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'storagelocation' table through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class StorageLocationServiceImpl implements StorageLocationService {

	private final StorageLocationDAO storageLocationDAO;
	private final CommonFunction commonFunction;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class properties.
	 * @param storageLocationDAO StorageLocationDAO Interface
	 * @param commonFunction CommonFunction holding common utility functions
	 */
	public StorageLocationServiceImpl(StorageLocationDAO storageLocationDAO, CommonFunction commonFunction) {
		this.storageLocationDAO = storageLocationDAO;
		this.commonFunction = commonFunction;
	}
	

	/**
	 * This service implementation method will access the DAO layer that is used 
	 * to get all the available storagelocations with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of storagelocation records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getStorageLocation(final int nmasterSiteCode) throws Exception {
		return storageLocationDAO.getStorageLocation(nmasterSiteCode);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to retrieve active storagelocation object based
	 * on the specified nstorageLocationCode.
	 * @param nstorageLocationCode [int] primary key of StorageLocation object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and data of StorageLocation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveStorageLocationById(final int nstorageLocationCode, final UserInfo userInfo)
			throws Exception {
		 
		final StorageLocation storageLocation = storageLocationDAO.getActiveStorageLocationById(nstorageLocationCode, userInfo);
		if (storageLocation == null) {
			//status code:417
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		else {
			return new ResponseEntity<>(storageLocation, HttpStatus.OK);
		}
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to storagelocation  table.
	 * @param storageLocation [StorageLocation] object holding details to be added in storagelocation table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of added StorageLocation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	@Transactional 
	public ResponseEntity<Object> createStorageLocation(final StorageLocation storageLocation, final UserInfo userInfo)
			throws Exception {
		return storageLocationDAO.createStorageLocation(storageLocation, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 *  update entry in storagelocation  table.
	 * @param storageLocation [StorageLocation] object holding details to be updated in storagelocation table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated StorageLocation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	@Transactional 
	public ResponseEntity<Object> updateStorageLocation(final StorageLocation storageLocation, final UserInfo userInfo)
			throws Exception {
		return storageLocationDAO.updateStorageLocation(storageLocation, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to delete an entry in storagelocation  table.
	 * @param storageLocation [StorageLocation] object holding detail to be deleted from storagelocation table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted StorageLocation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	@Transactional 
	public ResponseEntity<Object> deleteStorageLocation(final StorageLocation storageLocation, final UserInfo userInfo)
			throws Exception {

		return storageLocationDAO.deleteStorageLocation(storageLocation, userInfo);
	}
}
