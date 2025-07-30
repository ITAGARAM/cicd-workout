package com.agaramtech.qualis.testmanagement.service.methodcategory;


import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testmanagement.model.MethodCategory;


/**
 * This interface holds declarations to perform CRUD operation on 'methodcategory' table
 */

public interface MethodCategoryDAO {
	/**
	 * This interface declaration is used to get all the available methodcategories with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * which the list is to be fetched
	 * @return a response entity which holds the list of methodcategory records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	
	public ResponseEntity<Object> getMethodCategory(final int nmasterSiteCode) throws Exception;
	
	/**
	 * This interface declaration is used to retrieve active methodcategory object based
	 * on the specified nmethodcatcode.
	 * @param nmethodCatCode [int] primary key of methodcategory object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * which the list is to be fetched
	 * @return response entity  object holding response status and data of methodcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	
	public MethodCategory getActiveMethodCategoryById(final int nmethodCatCode) throws Exception;
	
	/**
	 * This interface declaration is used to add a new entry to methodcategory  table.
	 * @param objUnit [MethodCategory] object holding details to be added in methodcategory table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * which the list is to be fetched
	 * @return response entity object holding response status and data of added methodcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	
	public ResponseEntity<Object> createMethodCategory(MethodCategory methodCategory, UserInfo userInfo)throws Exception;
	
	/**
	 * This interface declaration is used to update entry in methodcategory  table.
	 * @param methodCategory [MethodCategory] object holding details to be updated in methodcategory table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * which the list is to be fetched
	 * @return response entity object holding response status and data of updated methodcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	
	public ResponseEntity<Object> updateMethodCategory(MethodCategory methodCategory, UserInfo userInfo)throws Exception;
	
	/**
	 * This interface declaration is used to delete an entry in methodcategory  table.
	 * @param methodCategory [MethodCategory] object holding detail to be deleted from methodcategory table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * which the list is to be fetched
	 * @return response entity object holding response status and data of deleted methodcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteMethodCategory(MethodCategory methodCategory, UserInfo userInfo)throws Exception;

}
