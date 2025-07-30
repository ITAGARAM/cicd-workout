package com.agaramtech.qualis.testmanagement.service.linkmaster;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;



public interface LinkMasterDAO {
	/**
	 * This interface declaration is used to get all the available linkMasters with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of linkMaster records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getLinkMaster(final UserInfo objUserInfo) throws Exception;
}
