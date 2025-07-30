package com.agaramtech.qualis.contactmaster.service.country;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.contactmaster.model.Country;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'country' table through
 * its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class CountryServiceImpl implements CountryService {

	private final CountryDAO countryDAO;
	private final CommonFunction commonFunction;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class properties.
	 * 
	 * @param countryDAO     CountryDAO Interface
	 * @param commonFunction CommonFunction holding common utility functions
	 */
	public CountryServiceImpl(CountryDAO countryDAO, CommonFunction commonFunction) {
		this.countryDAO = countryDAO;
		this.commonFunction = commonFunction;
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available countries with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of country records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getCountry(final UserInfo userInfo) throws Exception {

		return countryDAO.getCountry(userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * retrieve active country object based on the specified ncountryCode and
	 * nsitecode.
	 * 
	 * @param ncountryCode [int] primary key of country object
	 * @param userInfo     [UserInfo] holding logged in user details based on which
	 *                     the list is to be fetched
	 * @return response entity object holding response status and data of country
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveCountryById(int ncountrycode, final UserInfo userInfo) throws Exception {
		final Country objCountry = (Country) countryDAO.getActiveCountryById(ncountrycode, userInfo);
		if (objCountry == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objCountry, HttpStatus.OK);
		}
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to country table.
	 * 
	 * @param objCountry [Country] object holding details to be added in country
	 *                   table
	 * @param userInfo   [UserInfo] holding logged in user details and
	 *                   nmasterSiteCode [int] primary key of site object for which
	 *                   the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         country object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createCountry(Country objCountry, UserInfo userInfo) throws Exception {
		return countryDAO.createCountry(objCountry, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * update entry in country table.
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
	@Transactional
	@Override
	public ResponseEntity<Object> updateCountry(Country objCountry, UserInfo userInfo) throws Exception {
		return countryDAO.updateCountry(objCountry, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * delete an entry in country table.
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
	@Transactional
	@Override
	public ResponseEntity<Object> deleteCountry(Country objCountry, UserInfo userInfo) throws Exception {
		return countryDAO.deleteCountry(objCountry, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available countries with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of country records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getBatchPoolCountry(final UserInfo userInfo) throws Exception {
		return countryDAO.getBatchPoolCountry(userInfo);
	}

}
