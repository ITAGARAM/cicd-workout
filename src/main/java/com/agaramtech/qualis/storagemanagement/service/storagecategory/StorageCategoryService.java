package com.agaramtech.qualis.storagemanagement.service.storagecategory;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.storagemanagement.model.StorageCategory;

/**
 * This interface declaration holds methods to perform CRUD operation on 'storagecategory' table
 */
public interface StorageCategoryService {
	
	/**
	 * This service interface declaration will access the DAO layer that is used 
	 * to get all the available storagecategory with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of storagecategory records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<List<StorageCategory>> getStorageCategory(final UserInfo userInfo) throws Exception;
	
	/**
	 * This service interface declaration will access the DAO layer that is used to retrieve active storagecategory object based
	 * on the specified nstorageCategoryCode.
	 * @param nstorageCategoryCode [int] primary key of storagecategory object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of storagecategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveStorageCategoryById(final int nstorageCategoryCode,final UserInfo userInfo) throws Exception ;
	
	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * add a new entry to storagecategory table.
	 * @param objStorageCategory [StorageCategory] object holding details to be added in storagecategory table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of added storagecategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<? extends Object> createStorageCategory(final StorageCategory objStorageCategory,final UserInfo userInfo) throws Exception;
	
	/**
	 * This service interface declaration will access the DAO layer that is used to
	 *  update entry in storagecategory  table.
	 * @param objStorageCategory [StorageCategory] object holding details to be updated in storagecategory table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated storagecategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<? extends Object> updateStorageCategory(final StorageCategory objStorageCategory,final UserInfo userInfo) throws Exception;
	
	/**
	 * This service interface declaration will access the DAO layer that is used to delete an entry in storagecategory  table.
	 * @param objStorageCategory [StorageCategory] object holding detail to be deleted from storagecategory table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted storagecategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<? extends Object> deleteStorageCategory(final StorageCategory objStorageCategory,final UserInfo userInfo) throws Exception;


}
