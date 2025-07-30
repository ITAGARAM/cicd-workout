package com.agaramtech.qualis.organization.service.department;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.organization.model.Department;

/**
 * This interface declaration holds methods to perform CRUD operation on 'department' table
 */

public interface DepartmentService {

	/**
	 * This service interface declaration will access the DAO layer that is used 
	 * to get all the available department with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of department records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getDepartment(final UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to retrieve active department object based
	 * on the specified ndeptcode.
	 * @param ndeptcode [int] primary key of department object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and data of department object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveDepartmentById(final int ndeptcode, final UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * add a new entry to department  table.
	 * @param objDepartment [Department] object holding details to be added in department table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of added department object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createDepartment(final Department organisationDepartment, final UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 *  update entry in department  table.
	 * @param objDepartment [Department] object holding details to be updated in department table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated department object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateDepartment(final Department organisationDepartment, final UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to delete an entry in department  table.
	 * @param objDepartment [Department] object holding detail to be deleted from department table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted department object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteDepartment(final Department organisationDepartment, final UserInfo userInfo)
			throws Exception;
}
