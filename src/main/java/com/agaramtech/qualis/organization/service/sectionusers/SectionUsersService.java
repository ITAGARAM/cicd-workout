package com.agaramtech.qualis.organization.service.sectionusers;

import java.util.List;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.organization.model.SectionUsers;

/**
 * This interface declaration holds methods to perform CRUD operation on
 * 'sectionUsers' table
 */
public interface SectionUsersService {

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * add a new entry to sectionUsers table.
	 * 
	 * @param objsectionUsers [sectionUsers] object holding details to be added in
	 *                        sectionUsers table
	 * @param userInfo        [UserInfo] holding logged in user details and
	 *                        nmasterSiteCode [int] primary key of site object for
	 *                        which the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         sectionUsers object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createSectionUsers(List<SectionUsers> sectionUsersList, final UserInfo userInfo,
			final int nsiteCode) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * get all the available sectionUserss with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of sectionUsers records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getSectionUsersComboData(final UserInfo userInfo, final int nlabSectionCode,
			final int nsiteCode) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * delete an entry in sectionUsers table.
	 * 
	 * @param objsectionUsers [sectionUsers] object holding detail to be deleted
	 *                        from sectionUsers table
	 * @param userInfo        [UserInfo] holding logged in user details and
	 *                        nmasterSiteCode [int] primary key of site object for
	 *                        which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         sectionUsers object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteSectionUsers(final int sectionUserCode, final UserInfo userInfo,
			final int nlabSectionCode) throws Exception;
}
