package com.agaramtech.qualis.project.service.hospital;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.project.model.Hospital;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'hospital' table
 * through its DAO layer.
 * 
 * @author Mullai Balaji.V BGSI-4 30/06/2025
 * @version
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class HospitalServiceImpl implements HospitalService {

	private final HospitalDAO hospitalDAO;
	private final CommonFunction commonFunction;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class properties.
	 * 
	 * @param hospitalDAO    HospitalDAO Interface
	 * @param commonFunction CommonFunction holding common utility functions
	 */
	public HospitalServiceImpl(HospitalDAO hospitalDAO, CommonFunction commonFunction) {
		this.hospitalDAO = hospitalDAO;
		this.commonFunction = commonFunction;
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available hospital with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of hospital records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getHospital(UserInfo userInfo) throws Exception {

		return hospitalDAO.getHospital(userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * retrieve active hospital object based on the specified nhospitalCode.
	 * 
	 * @param nhospitalCode [int] primary key of hospital object
	 * @param userInfo      [UserInfo] holding logged in user details based on which
	 *                      the list is to be fetched
	 * @return response entity object holding response status and data of hospital
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveHospitalById(final int nhospitalcode, UserInfo userInfo) throws Exception {

		final Hospital hospital = hospitalDAO.getActiveHospitalById(nhospitalcode, userInfo);
		if (hospital == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(hospital, HttpStatus.OK);
		}
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to hospital table.
	 * 
	 * @param objhospital [hospital] object holding details to be added in hospital
	 *                    table
	 * @param userInfo    [UserInfo] holding logged in user details and
	 *                    nmasterSiteCode [int] primary key of site object for which
	 *                    the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         hospital object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createHospital(Hospital objHospital, UserInfo userInfo) throws Exception {
		return hospitalDAO.createHospital(objHospital, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * update entry in hospital table.
	 * 
	 * @param objhospital [hospital] object holding details to be updated in
	 *                    hospital table
	 * @param userInfo    [UserInfo] holding logged in user details and
	 *                    nmasterSiteCode [int] primary key of site object for which
	 *                    the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         hospital object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateHospital(Hospital objHospital, UserInfo userInfo) throws Exception {
		return hospitalDAO.updateHospital(objHospital, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * delete an entry in hospital table.
	 * 
	 * @param objhospital [hospital] object holding detail to be deleted from
	 *                    hospital table
	 * @param userInfo    [UserInfo] holding logged in user details and
	 *                    nmasterSiteCode [int] primary key of site object for which
	 *                    the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         hospital object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteHospital(Hospital objHospital, UserInfo userInfo) throws Exception {
		return hospitalDAO.deleteHospital(objHospital, userInfo);
	}

}
