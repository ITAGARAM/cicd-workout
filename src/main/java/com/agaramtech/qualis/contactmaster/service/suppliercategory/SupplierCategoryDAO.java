package com.agaramtech.qualis.contactmaster.service.suppliercategory;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.contactmaster.model.SupplierCategory;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation on 'contactmastersuppliercategory' table
 * through its implementation class.
 * @author ATE184
 * @version 9.0.0.1
 * @since   30- Jun- 2020
 */
public interface SupplierCategoryDAO {
	
	/**
	 * This interface declaration is used to retrieve list of all active suppliercategory for the
	 * specified site.
	 * @param nmasterSiteCode [int] primary key of site object for which the list is to be fetched
	 * @return response entity  object holding response status and list of all active suppliercategory
	 * @throws Exception that are thrown in the DAO layer
	 */
	
	public ResponseEntity<Object> getSupplierCategory(UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to retrieve list of all active suppliercategory for the
	 * specified site.
	 * @param nsuppliercode [int] key of supplier object for which the list is to be fetched
	 * @return response entity  object holding response status and list of all active suppliercategory
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getSupplierCategoryBySupplierCode(final int nmasterSiteCode,final int nsuppliercode) throws Exception;
	
	/**
	 * This interface declaration is used to retrieve active contactmastersuppliercategory object based
	 * on the specified nsuppliercatcode.
	 * @param nsuppliercatcode [int] primary key of contactmastersuppliercategory object
	 * @return response entity  object holding response status and data of suppliercategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	
	public SupplierCategory getActiveSupplierCategoryById(final int nsuppliercatcode,UserInfo userInfo) throws Exception ;
	
	/**
	 * This interface declaration is used to add a new entry to contactmastersuppliercategory  table.
	 * @param contactmasterSupplierCategory [SupplierCategory] object holding details to be added in contactmastersuppliercategory table
	 * @return response entity object holding response status and data of added suppliercategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createSupplierCategory(SupplierCategory contactmasterSupplierCategory, UserInfo userInfo) throws Exception;
	
	
	/**
	 * This interface declaration is used to update entry in contactmastersuppliercategory  table.
	 * @param contactmasterSupplierCategory [SupplierCategory] object holding details to be updated in contactmastersuppliercategory table
	 * @return response entity object holding response status and data of updated suppliercategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateSupplierCategory(SupplierCategory contactmasterSupplierCategory, UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to delete entry in contactmastersuppliercategory  table.
	 * @param contactmasterSupplierCategory [SupplierCategory] object holding detail to be deleted in contactmastersuppliercategory table
	 * @return response entity object holding response status and data of deleted suppliercategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteSupplierCategory(SupplierCategory contactmasterSupplierCategory, UserInfo userInfo) throws Exception;

	



}
