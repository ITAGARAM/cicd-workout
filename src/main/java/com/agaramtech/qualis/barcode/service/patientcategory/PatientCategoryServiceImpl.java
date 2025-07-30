package com.agaramtech.qualis.barcode.service.patientcategory;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.barcode.model.PatientCategory;
import com.agaramtech.qualis.basemaster.service.unit.UnitDAO;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.UserInfo;
import lombok.RequiredArgsConstructor;

/**
 * This class holds methods to perform CRUD operation on 'patientcategory' table
 * through its DAO layer.
 * 
 * @author ATE236
 * @version 10.0.0.2
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
//@RequiredArgsConstructor
public class PatientCategoryServiceImpl implements PatientCategoryService {

	private final PatientCategoryDAO patientCategoryDAO;

	private final CommonFunction commonFunction;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class properties.
	 * @param unitDAO UnitDAO Interface
	 * @param commonFunction CommonFunction holding common utility functions
	 */
	public PatientCategoryServiceImpl(PatientCategoryDAO patientCategoryDAO, CommonFunction commonFunction) {
		this.patientCategoryDAO = patientCategoryDAO;
		this.commonFunction = commonFunction;
	}

	/**
	 * This method is used to retrieve list of all active patientcategory.
	 * 
	 * @param userInfo [UserInfo] primary key of site object for which the list is
	 *                 to be fetched
	 * @return response entity object holding response status and list of all active
	 *         patientcategory
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getPatientCategory(final UserInfo userInfo) throws Exception {

		return patientCategoryDAO.getPatientCategory(userInfo);
	}

	/**
	 * This method is used to retrieve active patientcategory object based on the
	 * specified npatientcatcode.
	 * 
	 * @param npatientcatcode [int] primary key of patientcategory object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         patientcategory object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getActivePatientCategoryById(final int npatientcatcode,final UserInfo userInfo)
			throws Exception {

		return patientCategoryDAO.getActivePatientCategoryById(npatientcatcode, userInfo);
	}

	/**
	 * This method is used to add a new entry to patientcategory table.
	 * 
	 * @param objPatientCategory [PatientCategory] object holding details to be added in
	 *                       patientcategory table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of added patientcategory object.
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional 
	@Override
	public ResponseEntity<Object> createPatientCategory(final PatientCategory objPatientCategory,final UserInfo userInfo)
			throws Exception {

		return patientCategoryDAO.createPatientCategory(objPatientCategory, userInfo);
	}

	/**
	 * This method is used to update entry in patientcategory table.
	 * 
	 * @param objPatientCategory [PatientCategory] object holding details to be updated in
	 *                       patientcategory table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         patientcategory object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional 
	@Override
	public ResponseEntity<Object> updatePatientCategory(final PatientCategory objPatientCategory,final UserInfo userInfo)
			throws Exception {

		return patientCategoryDAO.updatePatientCategory(objPatientCategory, userInfo);
	}

	/**
	 * This method id used to delete an entry in patientcategory table
	 * 
	 * @param objPatientCategory [PatientCategory] an Object holds the record to be deleted
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity with corresponding HTTP status and a patientcategory
	 *         object
	 * @exception Exception that are thrown from this Service layer
	 */
	@Transactional 
	@Override
	public ResponseEntity<Object> deletePatientCategory(final PatientCategory objPatientCategory,final UserInfo userInfo)
			throws Exception {
		return patientCategoryDAO.deletePatientCategory(objPatientCategory, userInfo);
	}

}
