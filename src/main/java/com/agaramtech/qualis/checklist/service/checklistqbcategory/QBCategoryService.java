package com.agaramtech.qualis.checklist.service.checklistqbcategory;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.checklist.model.ChecklistQBCategory;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface declaration holds methods to perform CRUD operation on 'qbcategory' table
 */
public interface QBCategoryService {

	/**
	 * This service interface declaration will access the DAO layer that is used 
	 * to get all the available qbcategorys with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of qbcategory records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getQBCategory(int nmasterSiteCode)throws Exception;
	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * add a new entry to qbcategory  table.
	 * @param objqbcategory [qbcategory] object holding details to be added in qbcategory table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of added qbcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createQBCategory(ChecklistQBCategory objChecklistQBCategory, UserInfo userInfo)throws Exception;
	/**
	 * This service interface declaration will access the DAO layer that is used to
	 *  update entry in qbcategory  table.
	 * @param objqbcategory [qbcategory] object holding details to be updated in qbcategory table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated qbcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateQBCategory(ChecklistQBCategory objChecklistQBCategory, UserInfo userInfo)throws Exception;
	/**
	 * This service interface declaration will access the DAO layer that is used to delete an entry in qbcategory  table.
	 * @param objqbcategory [qbcategory] object holding detail to be deleted from qbcategory table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted qbcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteQBCategory(ChecklistQBCategory objChecklistQBCategory, UserInfo userInfo)throws Exception;
	/**
	 * This service interface declaration will access the DAO layer that is used to retrieve active qbcategory object based
	 * on the specified nqbcategoryCode.
	 * @param nqbcategoryCode [int] primary key of qbcategory object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and data of qbcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveQBCategoryById(final int nchecklistqbcategorycode, UserInfo userInfo) throws Exception;
}
