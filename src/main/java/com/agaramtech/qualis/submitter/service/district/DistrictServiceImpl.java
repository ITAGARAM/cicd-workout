package com.agaramtech.qualis.submitter.service.district;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.submitter.model.District;

/**
 * This class holds methods to perform CRUD operation on 'district' table
 * through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class DistrictServiceImpl implements DistrictService {

	private final DistrictDAO districtDAO;
	private final CommonFunction commonFunction;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class properties.
	 * @param districtDAO    DistrictDAO Interface
	 * @param commonFunction CommonFunction holding common utility functions
	 */
	public DistrictServiceImpl(DistrictDAO districtDAO, CommonFunction commonFunction) {
		this.districtDAO = districtDAO;
		this.commonFunction = commonFunction;
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available districts with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of district records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getDistrict(UserInfo userInfo) throws Exception {
		return districtDAO.getDistrict(userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * retrieve active district object based on the specified ndistrictcode.
	 * @param ndistrictcode [int] primary key of district object
	 * @param userInfo      [UserInfo] holding logged in user details based on which
	 *                      the list is to be fetched
	 * @return response entity object holding response status and data of district
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveDistrictById(final int ndistrictcode, final UserInfo userInfo)
			throws Exception {
		final District objDistrict = districtDAO.getActiveDistrictById(ndistrictcode, userInfo);
		if (objDistrict == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objDistrict, HttpStatus.OK);
		}
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
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
	@Transactional
	@Override
	public ResponseEntity<Object> createDistrict(District objDistrict, UserInfo userInfo) throws Exception {
		return districtDAO.createDistrict(objDistrict, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
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
	@Transactional
	@Override
	public ResponseEntity<Object> updateDistrict(District objDistrict, UserInfo userInfo) throws Exception {
		return districtDAO.updateDistrict(objDistrict, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
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
	@Transactional
	@Override
	public ResponseEntity<Object> deleteDistrict(District objDistrict, UserInfo userInfo) throws Exception {
		return districtDAO.deleteDistrict(objDistrict, userInfo);
	}

}
