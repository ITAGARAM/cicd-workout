package com.agaramtech.qualis.testmanagement.service.linkmaster;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.agaramtech.qualis.global.UserInfo;

@Service
public class LinkMasterServiceImpl implements LinkMasterService {

	private final LinkMasterDAO linkMasterDAO;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class properties.
	 * 
	 * @param LinkMasterDAO linkMasterDAO Interface
	 */
	public LinkMasterServiceImpl(LinkMasterDAO linkMasterDAO) {
		super();
		this.linkMasterDAO = linkMasterDAO;
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available linkMasters with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of linkMaster records with respect
	 *         to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getLinkMaster(final UserInfo objUserInfo) throws Exception {
		return linkMasterDAO.getLinkMaster(objUserInfo);
	}
}
