package com.agaramtech.qualis.basemaster.service.samplingpointcategory;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.basemaster.model.SamplingPointCategory;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface declaration holds methods to perform CRUD operation on
 * 'samplingpointcategory' table
 * 
 * @author AT-E143
 * @version 10.0.0.2
 * @since 06- February- 2025
 */
public interface SamplingPointCategoryService {
	/**
	 * This interface declaration is used to get the over all samplingpointcategory
	 * with respect to site
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of samplingpointcategory with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getSamplingPointCategory(final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active samplingpointcategory
	 * object based on the specified nsamplingPointCatCode.
	 * 
	 * @param nsamplingPointCatCode [int] primary key of samplingpointcategory
	 *                              object
	 * @return response entity object holding response status and data of
	 *         samplingpointcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveSamplingPointCategoryById(final int nsamplingPointCatCode,
			final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to add a new entry to
	 * samplingpointcategory table.
	 * 
	 * @param objSamplingPointCategory [SamplingPointCategory] object holding
	 *                                 details to be added in samplingpointcategory
	 *                                 table
	 * @return response entity object holding response status and data of added
	 *         samplingpointcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createSamplingPointCategory(final SamplingPointCategory objSamplingPointCategory,
			final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in samplingpointcategory
	 * table.
	 * 
	 * @param objSamplingPointCategory [SamplingPointCategory] object holding
	 *                                 details to be updated in
	 *                                 samplingpointcategory table
	 * @return response entity object holding response status and data of updated
	 *         samplingpointcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateSamplingPointCategory(final SamplingPointCategory objSamplingPointCategory,
			final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to delete entry in samplingpointcategory
	 * table.
	 * 
	 * @param objSamplingPointCategory [SamplingPointCategory] object holding detail
	 *                                 to be deleted in samplingpointcategory table
	 * @return response entity object holding response status and data of deleted
	 *         samplingpointcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteSamplingPointCategory(final SamplingPointCategory objSamplingPointCategory,
			final UserInfo userInfo) throws Exception;

}
