package com.agaramtech.qualis.barcode.service.patientcategory;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.barcode.model.PatientCategory;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface declaration holds methods to perform CRUD operation on
 * 'patientcategory' table
 * 
 * @author ATE236
 * @version 10.0.0.2
 */

public interface PatientCategoryService {
	/**
	 * This interface declaration is used to get the over all patientcategory
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of patientcategory and also
	 *         have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getPatientCategory(final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active patientcategory object
	 * based on the specified npatientcatcode.
	 * 
	 * @param npatientcatcode [int] primary key of patientcategory object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         patientcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActivePatientCategoryById(final int npatientcatcode, final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to add a new entry to patientcategory
	 * table.
	 * 
	 * @param objPatientCategory [PatientCategory] object holding details to be
	 *                           added in patientcategory table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         patientcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createPatientCategory(final PatientCategory objPatientCategory,
			final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in patientcategory table.
	 * 
	 * @param objPatientCategory [PatientCategory] object holding details to be
	 *                           updated in patientcategory table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         patientcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updatePatientCategory(final PatientCategory objPatientCategory,
			final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to delete entry in patientcategory table.
	 * 
	 * @param objPatientCategory [PatientCategory] object holding detail to be
	 *                           deleted in patientcategory table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         patientcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deletePatientCategory(final PatientCategory objPatientCategory,
			final UserInfo userInfo) throws Exception;

}
