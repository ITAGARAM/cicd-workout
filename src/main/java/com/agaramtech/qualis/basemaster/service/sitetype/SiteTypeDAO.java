package com.agaramtech.qualis.basemaster.service.sitetype;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.basemaster.model.SiteType;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation on 'sitetype' table
 */
/**
 * @author sujatha.v
 * BGSI-5
 * 02/07/2025
 */
public interface SiteTypeDAO {

	/**
	 * This interface declaration is used to get all the available sitetype's with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of sitetype records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getSiteType(final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active sitetype object based
	 * on the specified nsitetypecode.
	 * @param nsitetypecode [int] primary key of sitetype object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and data of sitetype object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public SiteType getActiveSiteTypeById(final int nsitetypecode, final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to add a new entry to sitetype table.
	 * @param objSiteType [SiteType] object holding details to be added in sitetype table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of added sitetype object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createSiteType(final SiteType objSiteType, final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in sitetype table.
	 * @param objSiteType [SiteType] object holding details to be updated in sitetype table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated sitetype object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateSiteType(final SiteType objSiteType, final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to delete an entry in sitetype table.
	 * @param objSiteType [SiteType] object holding detail to be deleted from sitetype table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted sitetype object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteSiteType(final SiteType objSiteType, final UserInfo userInfo) throws Exception;
}
