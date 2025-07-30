package com.agaramtech.qualis.organization.service.departmentlab;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.organization.model.DepartmentLab;

/**
 * This class holds methods to perform CRUD operation on 'DepartmentLab' table
 * through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class DepartmentLabServiceImpl implements DepartmentLabService {

	private final DepartmentLabDAO labDAO;

	public DepartmentLabServiceImpl(DepartmentLabDAO labDAO) {
		super();
		this.labDAO = labDAO;
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to DepartmentLab table.
	 * 
	 * @param objDepartmentLab [DepartmentLab] object holding details to be added in
	 *                         DepartmentLab table
	 * @param userInfo         [UserInfo] holding logged in user details and
	 *                         nmasterSiteCode [int] primary key of site object for
	 *                         which the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         DepartmentLab object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	public ResponseEntity<Object> createDepartmentLab(List<DepartmentLab> deptLabList, final UserInfo userInfo,
			final int nsiteCode, final String treePath) throws Exception {
		return labDAO.createDepartmentLab(deptLabList, userInfo, nsiteCode, treePath);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available DepartmentLabs with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of DepartmentLab records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getDepartmentLabComboData(final UserInfo userInfo, final int nsiteDeptCode)
			throws Exception {
		return labDAO.getDepartmentLabComboData(userInfo, nsiteDeptCode);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * delete an entry in DepartmentLab table.
	 * 
	 * @param objDepartmentLab [DepartmentLab] object holding detail to be deleted
	 *                         from DepartmentLab table
	 * @param userInfo         [UserInfo] holding logged in user details and
	 *                         nmasterSiteCode [int] primary key of site object for
	 *                         which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         DepartmentLab object
	 * @throws Exception that are thrown in the DAO layer
	 */

	@Transactional
	public ResponseEntity<Object> deleteDepartmentLab(final int deptLabCode, final UserInfo userInfo,
			final int nsiteCode, String treePath) throws Exception {
		return labDAO.deleteDepartmentLab(deptLabCode, userInfo, nsiteCode, treePath);
	}

}
