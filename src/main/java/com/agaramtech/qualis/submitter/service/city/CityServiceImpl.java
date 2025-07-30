package com.agaramtech.qualis.submitter.service.city;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.submitter.model.City;

import lombok.RequiredArgsConstructor;

/**
 * This class holds methods to perform CRUD operation on 'city' table through
 * its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class CityServiceImpl implements CityService {

	private final CityDAO cityDAO;
	private final CommonFunction commonFunction;

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available citys with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of city records with respect
	 *         to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getCity(final UserInfo userInfo) throws Exception {
		return cityDAO.getCity(userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to city table.
	 * 
	 * @param objCity  [City] object holding details to be added in city table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and data of added city
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createCity(final City city, final UserInfo userInfo) throws Exception {
		return cityDAO.createCity(city, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * retrieve active city object based on the specified ncityCode.
	 * 
	 * @param ncityCode [int] primary key of city object
	 * @param userInfo  [UserInfo] holding logged in user details based on which the
	 *                  list is to be fetched
	 * @return response entity object holding response status and data of city
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveCityById(final int ncitycode, final UserInfo userInfo) throws Exception {
		final City city = cityDAO.getActiveCityById(ncitycode);
		if (city == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(city, HttpStatus.OK);
		}
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * update entry in city table.
	 * 
	 * @param objCity  [City] object holding details to be updated in city table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and data of updated
	 *         city object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateCity(final City city, final UserInfo userInfo) throws Exception {
		return cityDAO.updateCity(city, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * delete an entry in city table.
	 * 
	 * @param objCity  [City] object holding detail to be deleted from city table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and data of deleted
	 *         city object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteCity(final City city, final UserInfo userInfo) throws Exception {
		return cityDAO.deleteCity(city, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available districts with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of district records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getDistrict(final UserInfo userInfo) throws Exception {
		return cityDAO.getDistrict(userInfo);
	}

}
