package com.agaramtech.qualis.basemaster.service.storagelocation;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.basemaster.model.StorageLocation;
import com.agaramtech.qualis.global.UserInfo;


/**
 * This interface holds declarations to perform CRUD operation on 'storagelocation' table
 */
public interface StorageLocationDAO {
	
	/**
	 * This interface declaration is used to get all the available storagelocations with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of storagelocation records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getStorageLocation(final int nmasterSiteCode) throws Exception;
	
	/**
	 * This interface declaration is used to retrieve active storagelocation object based
	 * on the specified nstorageLocationCode.
	 * @param nstorageLocationCode [int] primary key of storagelocation object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and data of storagelocation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public StorageLocation getActiveStorageLocationById(final int nstorageLocationCode, final UserInfo userInfo) throws Exception ;
	

	/**
	 * This interface declaration is used to add a new entry to storagelocation  table.
	 * @param objUnit [StorageLocation] object holding details to be added in storagelocation table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of added storagelocation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createStorageLocation(StorageLocation storageLocation,UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to update entry in storagelocation  table.
	 * @param objUnit [Unit] object holding details to be updated in storagelocation table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated storagelocation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateStorageLocation(StorageLocation storageLocation,UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to delete an entry in storagelocation  table.
	 * @param objUnit [Unit] object holding detail to be deleted from storagelocation table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted storagelocation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteStorageLocation(StorageLocation storageLocation,UserInfo userInfo) throws Exception;
}
