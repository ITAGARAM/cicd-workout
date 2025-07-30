package com.agaramtech.qualis.submitter.service.institutiondepartment;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.submitter.model.InstitutionDepartment;

/**
 * This interface holds declarations to perform CRUD operation on '
 * InstitutionDepartment' table
 * 
 * @author ATE224
 * @version 10.0.0.2
 * @since
 */
public interface InstitutionDepartmentDAO {
	/**
	 * This interface declaration is used to get the over all InstitutionDepartment
	 * with respect to site
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InstitutionDepartment with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getInstitutionDepartment(final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active InstitutionDepartment
	 * object based on the specified ninstitutiondeptcode.
	 * 
	 * @param ninstitutiondeptcode [int] primary key of InstitutionDepartment object
	 * @return response entity object holding response status and data of
	 *         InstitutionDepartment object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public InstitutionDepartment getActiveInstitutionDepartmentById(final int ninstitutiondeptcode) throws Exception;

	/**
	 * This interface declaration is used to add a new entry to
	 * InstitutionDepartment table.
	 * 
	 * @param objInstitutionDepartment [InstitutionDepartment] object holding
	 *                                 details to be added in InstitutionDepartment
	 *                                 table
	 * @return response entity object holding response status and data of added
	 *         InstitutionDepartment object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createInstitutionDepartment(final InstitutionDepartment objInstitutionDepartment,
			final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in InstitutionDepartment
	 * table.
	 * 
	 * @param objInstitutionDepartment [InstitutionDepartment] object holding
	 *                                 details to be updated in
	 *                                 InstitutionDepartment table
	 * @return response entity object holding response status and data of updated
	 *         InstitutionDepartment object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateInstitutionDepartment(final InstitutionDepartment objInstitutionDepartment,
			final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to delete entry in InstitutionDepartment
	 * table.
	 * 
	 * @param objInstitutionDepartment [InstitutionDepartment] object holding detail
	 *                                 to be deleted in InstitutionDepartment table
	 * @return response entity object holding response status and data of deleted
	 *         InstitutionDepartment object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteInstitutionDepartment(final InstitutionDepartment objInstitutionDepartment,
			final UserInfo userInfo) throws Exception;
}
