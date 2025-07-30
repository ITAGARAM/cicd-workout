package com.agaramtech.qualis.submitter.service.institutioncategory;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.submitter.model.InstitutionCategory;

/**
 * This interface declaration holds methods to perform CRUD operation on
 * InstitutionCategoryService table
 */
public interface InstitutionCategoryService {
	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * get all the available units with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of unit records with respect
	 *         to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<List<InstitutionCategory>> getInstitutionCategory(UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * add a new entry to InstitutionCategoryService table.
	 * 
	 * @param obj      InstitutionCategoryService [ InstitutionCategoryService]
	 *                 object holding details to be added in
	 *                 InstitutionCategoryService table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and data of added
	 *         InstitutionCategoryService object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<? extends Object> createInstitutionCategory(InstitutionCategory institutioncategory, UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * retrieve active InstitutionCategoryService object based on the specified
	 * ninstitutionCategoryServiceCode.
	 * 
	 * @param ninstitutionCategoryServiceCode [int] primary key of
	 *                                        InstitutionCategoryService object
	 * @param userInfo                        [UserInfo] holding logged in user
	 *                                        details based on which the list is to
	 *                                        be fetched
	 * @return response entity object holding response status and data of
	 *         institutionCategoryService object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveInstitutionCategoryById(int ninstitutioncatcode, UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * update entry in InstitutionCategory table.
	 * 
	 * @param objInstitutionCategory [InstitutionCategory] object holding details to
	 *                               be updated in InstitutionCategory table
	 * @param userInfo               [UserInfo] holding logged in user details and
	 *                               nmasterSiteCode [int] primary key of site
	 *                               object for which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         InstitutionCategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<? extends Object> updateInstitutionCategory(InstitutionCategory institutioncategory, UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * delete an entry in InstitutionCategory table.
	 * 
	 * @param objInstitutionCategory [InstitutionCategory] object holding detail to
	 *                               be deleted from InstitutionCategory table
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
