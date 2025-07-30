package com.agaramtech.qualis.organization.service.sectionusers;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.organization.model.SectionUsers;

/**
 * This class holds methods to perform CRUD operation on 'SectionUsers' table
 * through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class SectionUsersServiceImpl implements SectionUsersService {

	private final SectionUsersDAO sectionUsersDAO;
	
	public SectionUsersServiceImpl(SectionUsersDAO sectionUsersDAO) {
		super();
		this.sectionUsersDAO = sectionUsersDAO;
	}


	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to SectionUsers table.
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
	@Transactional
	public ResponseEntity<Object> createSectionUsers(List<SectionUsers> sectionUsersList, final UserInfo userInfo,
			final int nsiteCode) throws Exception {
		return sectionUsersDAO.createSectionUsers(sectionUsersList, userInfo, nsiteCode);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available SectionUserss with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of SectionUsers records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getSectionUsersComboData(final UserInfo userInfo, final int nlabSectionCode,
			final int nsiteCode) throws Exception {
		return sectionUsersDAO.getSectionUsersComboData(userInfo, nlabSectionCode, nsiteCode);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * delete an entry in SectionUsers table.
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
	@Transactional
	public ResponseEntity<Object> deleteSectionUsers(final int sectionUserCode, final UserInfo userInfo,
			final int nlabSectionCode) throws Exception {
		return sectionUsersDAO.deleteSectionUsers(sectionUserCode, userInfo, nlabSectionCode);
	}
}
