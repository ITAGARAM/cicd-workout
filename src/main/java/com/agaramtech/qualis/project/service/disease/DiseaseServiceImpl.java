package com.agaramtech.qualis.project.service.disease;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.project.model.Disease;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'disease' table through
 * its DAO layer.
 * @author Mullai Balaji.V 
 * BGSI-2
 * 26/06/2025 
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class DiseaseServiceImpl implements DiseaseService {

	private final DiseaseDAO diseaseDAO;
	private final CommonFunction commonFunction;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class properties.
	 * 
	 * @param diseaseDAO     DiseaseDAO Interface
	 * @param commonFunction CommonFunction holding common utility functions
	 */
	public DiseaseServiceImpl(DiseaseDAO diseaseDAO, CommonFunction commonFunction) {
		this.diseaseDAO = diseaseDAO;
		this.commonFunction = commonFunction;
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available disease with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of disease records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getDisease(UserInfo userInfo) throws Exception {

		return diseaseDAO.getDisease(userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * retrieve active disease object based on the specified ndiseaseCode.
	 * 
	 * @param ndiseaseCode [int] primary key of disease object
	 * @param userInfo     [UserInfo] holding logged in user details based on which
	 *                     the list is to be fetched
	 * @return response entity object holding response status and data of disease
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveDiseaseById(final int ndiseasecode, UserInfo userInfo) throws Exception {

		final Disease disease = diseaseDAO.getActiveDiseaseById(ndiseasecode, userInfo);
		if (disease == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(disease, HttpStatus.OK);
		}
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to disease table.
	 * 
	 * @param objdisease [disease] object holding details to be added in disease
	 *                   table
	 * @param userInfo   [UserInfo] holding logged in user details and
	 *                   nmasterSiteCode [int] primary key of site object for which
	 *                   the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         disease object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createDisease(Disease objDisease, UserInfo userInfo) throws Exception {
		return diseaseDAO.createDisease(objDisease, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * update entry in disease table.
	 * 
	 * @param objdisease [disease] object holding details to be updated in disease
	 *                   table
	 * @param userInfo   [UserInfo] holding logged in user details and
	 *                   nmasterSiteCode [int] primary key of site object for which
	 *                   the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         disease object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateDisease(Disease objDisease, UserInfo userInfo) throws Exception {
		return diseaseDAO.updateDisease(objDisease, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * delete an entry in disease table.
	 * 
	 * @param objdisease [disease] object holding detail to be deleted from disease
	 *                   table
	 * @param userInfo   [UserInfo] holding logged in user details and
	 *                   nmasterSiteCode [int] primary key of site object for which
	 *                   the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         disease object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteDisease(Disease objDisease, UserInfo userInfo) throws Exception {
		return diseaseDAO.deleteDisease(objDisease, userInfo);
	}

}
