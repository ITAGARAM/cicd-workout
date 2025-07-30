package com.agaramtech.qualis.testmanagement.service.parametertype;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;

public interface ParameterTypeDAO {

	/**
	 * This interface declaration is used to get all the available parametertype with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of parametertype records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getParameterType(final UserInfo userInfo) throws Exception;
}
