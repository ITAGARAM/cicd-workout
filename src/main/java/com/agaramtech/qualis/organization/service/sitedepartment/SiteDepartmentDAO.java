package com.agaramtech.qualis.organization.service.sitedepartment;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.organization.model.SiteDepartment;

public interface SiteDepartmentDAO {

	/**
	 * This interface declaration is used to get all the available SiteDepartments
	 * with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of SiteDepartment records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getOrganisation(final UserInfo userInfo, Map<String, Object> inputMap)
			throws Exception;

	/**
	 * This interface declaration is used to get all the available SiteDepartments
	 * with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of SiteDepartment records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getSiteDepartment(final int nsiteCode, final UserInfo userInfo,
			final boolean graphView, String treePath, final boolean initialGet, final Integer childPrimaryKey,
			final boolean fetchUser) throws Exception;

	/**
	 * This interface declaration is used to get all the available SiteDepartments
	 * with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of SiteDepartment records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getSiteDepartmentComboData(final UserInfo userInfo, final int nsiteCode)
			throws Exception;

	/**
	 * This interface declaration is used to add a new entry to SiteDepartment
	 * table.
	 * 
	 * @param objSiteDepartment [SiteDepartment] object holding details to be added
	 *                          in SiteDepartment table
	 * @param userInfo          [UserInfo] holding logged in user details and
	 *                          nmasterSiteCode [int] primary key of site object for
	 *                          which the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         SiteDepartment object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createSiteDepartment(List<SiteDepartment> siteDepartmentList, final UserInfo userInfo,
			String treePath) throws Exception;

	/**
	 * This interface declaration is used to delete an entry in SiteDepartment
	 * table.
	 * 
	 * @param objSiteDepartment [SiteDepartment] object holding detail to be deleted
	 *                          from SiteDepartment table
	 * @param userInfo          [UserInfo] holding logged in user details and
	 *                          nmasterSiteCode [int] primary key of site object for
	 *                          which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         SiteDepartment object
	 * @throws Exception that are thrown in the DAO layer
	 */

	public ResponseEntity<Object> deleteSiteDepartment(final int siteDeptCode, final UserInfo userInfo, String treePath)
			throws Exception;

	/**
	 * This interface declaration is used to get all the available SiteDepartments
	 * with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of SiteDepartment records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getSectionUsers(final String nlabSectionCode, final UserInfo userInfo,
			final boolean graphView) throws Exception;

	/**
	 * This interface declaration is used to get all the available SiteDepartments
	 * with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of SiteDepartment records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getSectionUserRole(final int nsiteCode, final int nuserCode, final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to get all the available SiteDepartments
	 * with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of SiteDepartment records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getDepartmentLab(final String siteDeptCode, final UserInfo userInfo,
			final boolean graphView, final boolean fetchUser) throws Exception;

	/**
	 * This interface declaration is used to get all the available SiteDepartments
	 * with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of SiteDepartment records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getLabSection(final String deptLabCode, final UserInfo userInfo,
			final boolean graphView, final boolean fetchUser) throws Exception;

}
