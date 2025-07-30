package com.agaramtech.qualis.configuration.service.limselnsite;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.configuration.model.LimsElnSiteMapping;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface declaration holds methods to perform CRUD operation on
 * 'limsElnSiteMapping' table
 */
public interface LimsElnSiteMappingService {

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * get all the available limsElnSiteMappings with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of limsElnSiteMapping records
	 *         with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	ResponseEntity<Object> getlimselnsitemapping(final UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * get all the available limsUsers with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of limsUsers records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	ResponseEntity<Object> getLimsSite(final UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * add a new entry to limsElnSiteMapping table.
	 * 
	 * @param objLimsElnSiteMapping [LimsElnSiteMapping] object holding details to
	 *                              be added in limsElnSiteMapping table
	 * @param userInfo              [UserInfo] holding logged in user details and
	 *                              nmasterSiteCode [int] primary key of site object
	 *                              for which the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         limsElnSiteMapping object
	 * @throws Exception that are thrown in the DAO layer
	 */
	ResponseEntity<Object> createLimsElnSitemapping(final LimsElnSiteMapping limselnuser, final UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * delete an entry in limsElnSiteMapping table.
	 * 
	 * @param objLimsElnSiteMapping [LimsElnSiteMapping] object holding detail to be
	 *                              deleted from limsElnSiteMapping table
	 * @param userInfo              [UserInfo] holding logged in user details and
	 *                              nmasterSiteCode [int] primary key of site object
	 *                              for which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         limsElnSiteMapping object
	 * @throws Exception that are thrown in the DAO layer
	 */
	ResponseEntity<Object> deleteLimsElnSitemapping(final LimsElnSiteMapping limselnuser, final UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * retrieve active limsElnSiteMapping object based on the specified
	 * nlimsElnSiteMappingCode.
	 * 
	 * @param nlimsElnSiteMappingCode [int] primary key of limsElnSiteMapping object
	 * @param userInfo                [UserInfo] holding logged in user details
	 *                                based on which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         limsElnSiteMapping object
	 * @throws Exception that are thrown in the DAO layer
	 */
	ResponseEntity<Object> getActiveLimsElnSitemappingById(final int nelnusermappingcode, final UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * update entry in limsElnSiteMapping table.
	 * 
	 * @param objLimsElnSiteMapping [LimsElnSiteMapping] object holding details to
	 *                              be updated in limsElnSiteMapping table
	 * @param userInfo              [UserInfo] holding logged in user details and
	 *                              nmasterSiteCode [int] primary key of site object
	 *                              for which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         limsElnSiteMapping object
	 * @throws Exception that are thrown in the DAO layer
	 */
	ResponseEntity<Object> updateLimsElnSitemapping(final LimsElnSiteMapping limselnuser, final UserInfo userInfo)
			throws Exception;
}
