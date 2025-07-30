package com.agaramtech.qualis.organization.service.sectionusers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.organization.model.SectionUsers;

/**
 * This interface holds declarations to perform CRUD operation on 'SectionUsers'
 * table
 */
public interface SectionUsersDAO {

	/**
	 * This interface declaration is used to add a new entry to SectionUsers table.
	 * 
	 * @param objSectionUsers [SectionUsers] object holding details to be added in
	 *                        SectionUsers table
	 * @param userInfo        [UserInfo] holding logged in user details and
	 *                        nmasterSiteCode [int] primary key of site object for
	 *                        which the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         SectionUsers object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createSectionUsers(List<SectionUsers> sectionUsersList, final UserInfo userInfo,
			final int nsiteCode) throws Exception;

	/**
	 * This interface declaration is used to get all the available SectionUserss
	 * with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of SectionUsers records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getSectionUsersComboData(final UserInfo userInfo, final int nlabSectionCode,
			final int nsiteCode) throws Exception;

	/**
	 * This interface declaration is used to delete an entry in SectionUsers table.
	 * 
	 * @param objSectionUsers [SectionUsers] object holding detail to be deleted
	 *                        from SectionUsers table
	 * @param userInfo        [UserInfo] holding logged in user details and
	 *                        nmasterSiteCode [int] primary key of site object for
	 *                        which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         SectionUsers object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteSectionUsers(final int sectionUserCode, final UserInfo userInfo,
			final int nlabSectionCode) throws Exception;
}
