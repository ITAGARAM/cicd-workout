package com.agaramtech.qualis.submitter.service.institutioncategory;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.submitter.model.InstitutionCategory;

/**
 * This interface holds declarations to perform CRUD operation on
 * Institutioncategory table
 */
public interface InstitutionCategoryDAO {
	/**
	 * This interface declaration is used to get all the available
	 * institutioncategorys with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of institutioncategory records
	 *         with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<List<InstitutionCategory>> getInstitutionCategory(UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to add a new entry to institutioncategory
	 * table.
	 * 
	 * @param objinstitutioncategory [institutioncategory] object holding details to
	 *                               be added in institutioncategory table
	 * @param userInfo               [UserInfo] holding logged in user details and
	 *                               nmasterSiteCode [int] primary key of site
	 *                               object for which the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         institutioncategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<? extends Object> createInstitutionCategory(InstitutionCategory institutioncategory, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to retrieve active institutioncategory
	 * object based on the specified ninstitutioncategoryCode.
	 * 
	 * @param ninstitutioncategoryCode [int] primary key of institutioncategory
	 *                                 object
	 * @param userInfo                 [UserInfo] holding logged in user details
	 *                                 based on which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         institutioncategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public InstitutionCategory getActiveInstitutionCategoryById(int ninstitutioncatcode) throws Exception;

	/**
	 * This interface declaration is used to update entry in institutioncategory
	 * table.
	 * 
	 * @param objinstitutioncategory [institutioncategory] object holding details to
	 *                               be updated in institutioncategory table
	 * @param userInfo               [UserInfo] holding logged in user details and
	 *                               nmasterSiteCode [int] primary key of site
	 *                               object for which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         institutioncategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<? extends Object> updateInstitutionCategory(InstitutionCategory institutioncategory, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to delete an entry in institutioncategory
	 * table.
	 * 
	 * @param objinstitutioncategory [institutioncategory] object holding detail to
	 *                               be deleted from institutioncategory table
	 * @param userInfo               [UserInfo] holding logged in user details and
	 *                               nmasterSiteCode [int] primary key of site
	 *                               object for which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         institutioncategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<? extends Object> deleteInstitutionCategory(InstitutionCategory institutioncategory, UserInfo userInfo)
			throws Exception;

}
