package com.agaramtech.qualis.organization.service.labsection;

import java.util.List;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.organization.model.LabSection;

/**
 * This interface holds declarations to perform CRUD operation on 'LabSection'
 * table
 */
public interface LabSectionDAO {

	/**
	 * This interface declaration is used to add a new entry to LabSection table.
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

	public ResponseEntity<Object> createLabSection(List<LabSection> labSectionList, final UserInfo userInfo,
			final int nsiteCode, String treePath) throws Exception;

	/**
	 * This interface declaration is used to get all the available LabSections with
	 * respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of LabSection records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getLabSectionComboData(final UserInfo userInfo, final int ndeptLabCode)
			throws Exception;

	/**
	 * This interface declaration is used to delete an entry in LabSection table.
	 * 
	 * @param objLabSection[LabSection] object holding detail to be deleted from
	 *                                  LabSection table
	 * @param userInfo                  [UserInfo] holding logged in user details
	 *                                  and nmasterSiteCode [int] primary key of
	 *                                  site object for which the list is to be
	 *                                  fetched
	 * @return response entity object holding response status and data of deleted
	 *         LabSection object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteLabSection(final int labSectionCode, final UserInfo userInfo,
			final int nsiteCode, String treePath) throws Exception;

}
