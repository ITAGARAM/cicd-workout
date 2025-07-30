package com.agaramtech.qualis.organization.service.lab;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.organization.model.Lab;

import lombok.RequiredArgsConstructor;

/**
 * This class holds methods to perform CRUD operation on Lab table through its
 * DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class LabServiceImpl implements LabService {
	private final LabDAO labDAO;
	private final CommonFunction commonFunction;

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available Labs with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of Lab records with respect to
	 *         site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getLab(final int nmasterSiteCode) throws Exception {
		return labDAO.getLab(nmasterSiteCode);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to Lab table.
	 * 
	 * @param objLab   [Lab] object holding details to be added in Lab table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and data of added Lab
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createLab(Lab lab, UserInfo userInfo) throws Exception {
		return labDAO.createLab(lab, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * update entry in Lab table.
	 * 
	 * @param objLab   [Lab] object holding details to be updated in Lab table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and data of updated
	 *         Lab object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateLab(Lab lab, UserInfo userInfo) throws Exception {
		return labDAO.updateLab(lab, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * retrieve active Lab object based on the specified nLabCode.
	 * 
	 * @param nLabCode [int] primary key of Lab object
	 * @param userInfo [UserInfo] holding logged in user details based on which the
	 *                 list is to be fetched
	 * @return response entity object holding response status and data of Lab object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveLabById(final int nlabcode, final UserInfo userInfo) throws Exception {
		final Lab lab = (Lab) labDAO.getActiveLabById(nlabcode);
		if (lab == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(lab, HttpStatus.OK);
		}
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * delete an entry in Lab table.
	 * 
	 * @param objLab   [Lab] object holding detail to be deleted from Lab table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and data of deleted
	 *         Lab object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteLab(Lab lab, UserInfo userInfo) throws Exception {
		return labDAO.deleteLab(lab, userInfo);
	}

}
