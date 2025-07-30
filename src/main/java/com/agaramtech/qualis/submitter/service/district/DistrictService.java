package com.agaramtech.qualis.submitter.service.district;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.submitter.model.District;

/**
 * This interface declaration holds methods to perform CRUD operation on
 * 'district' table
 */
public interface DistrictService {
	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * get all the available districts with respect to site.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of unit records with respect
	 *         to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getDistrict(UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * retrieve active district object based on the specified ndistrictcode.
	 * @param ndistrictcode [int] primary key of district object
	 * @param userInfo      [UserInfo] holding logged in user details based on which
	 *                      the list is to be fetched
	 * @return response entity object holding response status and data of district
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveDistrictById(final int ndistrictcode, UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * add a new entry to district table.
	 * @param objDistrict [District] object holding details to be added in district
	 *                    table
	 * @param userInfo    [UserInfo] holding logged in user details and
	 *                    nmasterSiteCode [int] primary key of site object for which
	 *                    the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         district object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createDistrict(District objDistrict, UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * update entry in district table.
	 * @param objDistrict [District] object holding details to be updated in
	 *                    district table
	 * @param userInfo    [UserInfo] holding logged in user details and
	 *                    nmasterSiteCode [int] primary key of site object for which
	 *                    the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         district object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateDistrict(District objDistrict, UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * delete an entry in district table.
	 * @param objDistrict [District] object holding detail to be deleted from
	 *                    district table
	 * @param userInfo    [UserInfo] holding logged in user details and
	 *                    nmasterSiteCode [int] primary key of site object for which
	 *                    the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         district object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteDistrict(District objDistrict, UserInfo userInfo) throws Exception;
}
