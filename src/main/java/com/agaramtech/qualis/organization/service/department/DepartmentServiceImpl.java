package com.agaramtech.qualis.organization.service.department;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.organization.model.Department;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
/**
 * This class holds methods to perform CRUD operation on 'department' table
 * through its DAO layer.
 */
public class DepartmentServiceImpl implements DepartmentService {

	private final DepartmentDAO departmentDAO;
	private final CommonFunction commonFunction;

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available department with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of department records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getDepartment(final UserInfo userInfo) throws Exception {
		return departmentDAO.getDepartment(userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * retrieve active department object based on the specified ndeptcode.
	 * 
	 * @param ndeptcode [int] primary key of department object
	 * @param userInfo  [UserInfo] holding logged in user details based on which the
	 *                  list is to be fetched
	 * @return response entity object holding response status and data of department
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveDepartmentById(final int ndeptcode, final UserInfo userInfo)
			throws Exception {
		final Department department = (Department) departmentDAO.getActiveDepartmentById(ndeptcode, userInfo);
		if (department == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(department, HttpStatus.OK);
		}
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to department table.
	 * 
	 * @param objDepartment [Department] object holding details to be added in
	 *                      department table
	 * @param userInfo      [UserInfo] holding logged in user details and
	 *                      nmasterSiteCode [int] primary key of site object for
	 *                      which the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         department object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createDepartment(final Department Department, final UserInfo userInfo)
			throws Exception {
		return departmentDAO.createDepartment(Department, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * update entry in department table.
	 * 
	 * @param objDepartment [Department] object holding details to be updated in
	 *                      department table
	 * @param userInfo      [UserInfo] holding logged in user details and
	 *                      nmasterSiteCode [int] primary key of site object for
	 *                      which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         department object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateDepartment(final Department organisationDepartment, final UserInfo userInfo)
			throws Exception {
		return departmentDAO.updateDepartment(organisationDepartment, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * delete an entry in department table.
	 * 
	 * @param objDepartment [Department] object holding detail to be deleted from
	 *                      department table
	 * @param userInfo      [UserInfo] holding logged in user details and
	 *                      nmasterSiteCode [int] primary key of site object for
	 *                      which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         department object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteDepartment(final Department organisationDepartment, final UserInfo userInfo)
			throws Exception {
		return departmentDAO.deleteDepartment(organisationDepartment, userInfo);
	}

}
