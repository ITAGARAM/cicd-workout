package com.agaramtech.qualis.organization.service.labsection;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.organization.model.LabSection;

/**
 * This class holds methods to perform CRUD operation on 'LabSection' table
 * through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class LabSectionServiceImpl implements LabSectionService {

	private final LabSectionDAO labSectionDAO;

	public LabSectionServiceImpl(LabSectionDAO labSectionDAO) {
		super();
		this.labSectionDAO = labSectionDAO;
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to LabSection table.
	 * 
	 * @param objLabSection [LabSection] object holding details to be added in
	 *                      LabSection table
	 * @param userInfo      [UserInfo] holding logged in user details and
	 *                      nmasterSiteCode [int] primary key of site object for
	 *                      which the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         LabSection object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	public ResponseEntity<Object> createLabSection(List<LabSection> labSectionList, final UserInfo userInfo,
			final int nsiteCode, String treePath) throws Exception {
		return labSectionDAO.createLabSection(labSectionList, userInfo, nsiteCode, treePath);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available LabSections with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of LabSection records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getLabSectionComboData(final UserInfo userInfo, final int ndeptLabCode)
			throws Exception {
		return labSectionDAO.getLabSectionComboData(userInfo, ndeptLabCode);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * delete an entry in LabSection table.
	 * 
	 * @param objLabSection [LabSection] object holding detail to be deleted from
	 *                      LabSection table
	 * @param userInfo      [UserInfo] holding logged in user details and
	 *                      nmasterSiteCode [int] primary key of site object for
	 *                      which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         LabSection object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	public ResponseEntity<Object> deleteLabSection(final int labSectionCode, final UserInfo userInfo,
			final int nsiteCode, String treePath) throws Exception {
		return labSectionDAO.deleteLabSection(labSectionCode, userInfo, nsiteCode, treePath);
	}

}
