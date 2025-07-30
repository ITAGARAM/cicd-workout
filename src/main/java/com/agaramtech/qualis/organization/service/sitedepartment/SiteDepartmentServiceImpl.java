package com.agaramtech.qualis.organization.service.sitedepartment;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.organization.model.SiteDepartment;

/**
 * This class holds methods to perform CRUD operation on 'SiteDepartment' table
 * through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class SiteDepartmentServiceImpl implements SiteDepartmentService {

	private final SiteDepartmentDAO siteDAO;

	public SiteDepartmentServiceImpl(SiteDepartmentDAO siteDAO) {
		super();
		this.siteDAO = siteDAO;
	}

	public ResponseEntity<Object> getOrganisation(final UserInfo userInfo, Map<String, Object> inputMap)
			throws Exception {
		return siteDAO.getOrganisation(userInfo, inputMap);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available SiteDepartments with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of SiteDepartment records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getSiteDepartment(final int nsiteCode, final UserInfo userInfo,
			final boolean graphView, String treePath, final Integer childPrimaryKey, final boolean fetchUser)
			throws Exception {
		return siteDAO.getSiteDepartment(nsiteCode, userInfo, graphView, treePath, false, childPrimaryKey, fetchUser);
	}

	public ResponseEntity<Object> getSiteDepartmentComboData(final UserInfo userInfo, final int nsiteCode)
			throws Exception {
		return siteDAO.getSiteDepartmentComboData(userInfo, nsiteCode);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to SiteDepartment table.
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
	@Transactional
	public ResponseEntity<Object> createSiteDepartment(List<SiteDepartment> siteDepartmentList, final UserInfo userInfo,
			String treePath) throws Exception {
		return siteDAO.createSiteDepartment(siteDepartmentList, userInfo, treePath);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * delete an entry in SiteDepartment table.
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
	@Transactional
	public ResponseEntity<Object> deleteSiteDepartment(final int siteDeptCode, final UserInfo userInfo, String treePath)
			throws Exception {
		return siteDAO.deleteSiteDepartment(siteDeptCode, userInfo, treePath);
	}

	public ResponseEntity<Object> getSectionUsers(final String nlabSectionCode, final UserInfo userInfo,
			final boolean graphView) throws Exception {
		return siteDAO.getSectionUsers(nlabSectionCode, userInfo, graphView);
	}

	public ResponseEntity<Object> getSectionUserRole(final int nsiteCode, final int nuserCode, final UserInfo userInfo)
			throws Exception {
		return siteDAO.getSectionUserRole(nsiteCode, nuserCode, userInfo);
	}

	public ResponseEntity<Object> getDepartmentLab(final String siteDeptCode, final UserInfo userInfo,
			final StringBuffer treePath, final boolean graphView, final boolean fetchUser) throws Exception {
		return siteDAO.getDepartmentLab(siteDeptCode, userInfo, graphView, fetchUser);
	}

	public ResponseEntity<Object> getLabSection(final String deptLabCode, final UserInfo userInfo,
			final boolean graphView, final boolean fetchUser) throws Exception {
		return siteDAO.getLabSection(deptLabCode, userInfo, graphView, fetchUser);
	}

}
