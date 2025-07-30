package com.agaramtech.qualis.contactmaster.service.country;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.contactmaster.model.Country;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation on 'country'
 * table
 */
public interface CountryDAO {

	/**
	 * This interface declaration is used to get all the available countries with
	 * respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of country records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getCountry(final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active country object based on
	 * the specified ncountryCode and nsitecode.
	 * 
	 * @param ncountryCode [int] primary key of country object
	 * @param userInfo     [UserInfo] holding logged in user details based on which
	 *                     the list is to be fetched
	 * @return response entity object holding response status and data of country
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public Country getActiveCountryById(final int ncountryCode, final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to add a new entry to country table.
	 * 
	 * @param objCountry [Country] object holding details to be added in unit table
	 * @param userInfo   [UserInfo] holding logged in user details and
	 *                   nmasterSiteCode [int] primary key of site object for which
	 *                   the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         country object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createCountry(Country objCountry, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in country table.
	 * 
	 * @param objCountry [Country] object holding details to be updated in country
	 *                   table
	 * @param userInfo   [UserInfo] holding logged in user details and
	 *                   nmasterSiteCode [int] primary key of site object for which
	 *                   the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         country object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateCountry(Country objCountry, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to delete an entry in country table.
	 * 
	 * @param objCountry [Country] object holding detail to be deleted from country
	 *                   table
	 * @param userInfo   [UserInfo] holding logged in user details and
	 *                   nmasterSiteCode [int] primary key of site object for which
	 *                   the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         country object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteCountry(Country objCountry, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to get all the available countries with
	 * respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of country records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getBatchPoolCountry(final UserInfo userInfo) throws Exception;
}
