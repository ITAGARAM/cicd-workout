package com.agaramtech.qualis.checklist.service.checklistqbcategory;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.checklist.model.ChecklistQBCategory;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation on 'qbcategoryDAO' table
 */
public interface QBCategoryDAO {
	/**
	 * This interface declaration is used to get all the available qbcategoryDAOs with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of qbcategoryDAO records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getQBCategory(int nmasterSiteCode)throws Exception;
	/**
	 * This interface declaration is used to add a new entry to qbcategoryDAO  table.
	 * @param objqbcategoryDAO [qbcategoryDAO] object holding details to be added in qbcategoryDAO table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of added qbcategoryDAO object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createQBCategory(ChecklistQBCategory objChecklistQBCategory, UserInfo userInfo)throws Exception;
	/**
	 * This interface declaration is used to update entry in qbcategoryDAO  table.
	 * @param objqbcategoryDAO [qbcategoryDAO] object holding details to be updated in qbcategoryDAO table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated qbcategoryDAO object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateQBCategory(ChecklistQBCategory objChecklistQBCategory, UserInfo userInfo)throws Exception;
	/**
	 * This interface declaration is used to delete an entry in qbcategoryDAO  table.
	 * @param objqbcategoryDAO [qbcategoryDAO] object holding detail to be deleted from qbcategoryDAO table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted qbcategoryDAO object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteQBCategory(ChecklistQBCategory objChecklistQBCategory, UserInfo userInfo)throws Exception;
	/**
	 * This interface declaration is used to retrieve active qbcategoryDAO object based
	 * on the specified nqbcategoryDAOCode.
	 * @param nqbcategoryDAOCode [int] primary key of qbcategoryDAO object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and data of qbcategoryDAO object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ChecklistQBCategory getActiveQBCategoryById(final int nchecklistQBCategorycode) throws Exception;
}
