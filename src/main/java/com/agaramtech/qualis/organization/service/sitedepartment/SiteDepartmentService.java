package com.agaramtech.qualis.organization.service.sitedepartment;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.organization.model.SiteDepartment;

/**
 * This interface declaration holds methods to perform CRUD operation on
 * 'siteDepartment' table
 */
public interface SiteDepartmentService {

	public ResponseEntity<Object> getOrganisation(final UserInfo userInfo, Map<String, Object> inputMap)
			throws Exception;

	public ResponseEntity<Object> getSiteDepartment(final int nsiteCode, final UserInfo userInfo,
			final boolean graphView, String treePath, final Integer childPrimaryKey, final boolean fetchUser)
			throws Exception;

	public ResponseEntity<Object> getSiteDepartmentComboData(final UserInfo userInfo, final int nsiteCode)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * add a new entry to siteDepartment table.
	 * 
	 * @param objsiteDepartment [siteDepartment] object holding details to be added
	 *                          in siteDepartment table
	 * @param userInfo          [UserInfo] holding logged in user details and
	 *                          nmasterSiteCode [int] primary key of site object for
	 *                          which the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         siteDepartment object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createSiteDepartment(List<SiteDepartment> siteDepartmentList, final UserInfo userInfo,
			String treePath) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * delete an entry in siteDepartment table.
	 * 
	 * @param objsiteDepartment [siteDepartment] object holding detail to be deleted
	 *                          from siteDepartment table
	 * @param userInfo          [UserInfo] holding logged in user details and
	 *                          nmasterSiteCode [int] primary key of site object for
	 *                          which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         siteDepartment object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteSiteDepartment(final int siteDeptCode, final UserInfo userInfo, String treePath)
			throws Exception;

	public ResponseEntity<Object> getSectionUsers(final String nlabSectionCode, final UserInfo userInfo,
			final boolean graphView) throws Exception;

	public ResponseEntity<Object> getSectionUserRole(final int nsiteCode, final int nuserCode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getDepartmentLab(final String siteDeptCode, final UserInfo userInfo,
			final StringBuffer treePath, final boolean graphView, final boolean fetchUser) throws Exception;

	public ResponseEntity<Object> getLabSection(final String deptLabCode, final UserInfo userInfo,
			final boolean graphView, final boolean fetchUser) throws Exception;
}
