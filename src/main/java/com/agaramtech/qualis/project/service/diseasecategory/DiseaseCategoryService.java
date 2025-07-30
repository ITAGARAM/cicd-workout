package com.agaramtech.qualis.project.service.diseasecategory;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.project.model.DiseaseCategory;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface declaration holds methods to perform CRUD operation on
 * 'diseasecategory' table
 */
/**
 * @author sujatha.v
 * BGSI-1
 * 26/06/2025
 */
public interface DiseaseCategoryService {

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * get all the available diseasecategory's with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of diseasecategory records
	 *         with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getDiseaseCategory(final UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * retrieve active diseasecategory object based on the specified
	 * ndiseasecategorycode.
	 * 
	 * @param ndiseasecategorycode [int] primary key of diseasecategory object
	 * @param userInfo             [UserInfo] holding logged in user details based
	 *                             on which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         diseasecategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveDiseaseCategoryById(final int ndiseasecategorycode, final UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * add a new entry to diseasecategory table.
	 * 
	 * @param objDiseaseCategory [DiseaseCategory] object holding details to be
	 *                           added in diseasecategory table
	 * @param userInfo           [UserInfo] holding logged in user details and
	 *                           nmasterSiteCode [int] primary key of site object
	 *                           for which the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         diseasecategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createDiseaseCategory(final DiseaseCategory objDiseaseCategory,
			final UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * update entry in diseasecategory table.
	 * 
	 * @param objDiseaseCategory [DiseaseCategory] object holding details to be
	 *                           updated in diseasecategory table
	 * @param userInfo           [UserInfo] holding logged in user details and
	 *                           nmasterSiteCode [int] primary key of site object
	 *                           for which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         diseasecategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateDiseaseCategory(final DiseaseCategory objDiseaseCategory,
			final UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * delete an entry in diseasecategory table.
	 * 
	 * @param objDiseaseCategory [DiseaseCategory] object holding detail to be
	 *                           deleted from diseasecategory table
	 * @param userInfo           [UserInfo] holding logged in user details and
	 *                           nmasterSiteCode [int] primary key of site object
	 *                           for which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         diseasecategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteDiseaseCategory(final DiseaseCategory objDiseaseCategory,
			final UserInfo userInfo) throws Exception;

}
