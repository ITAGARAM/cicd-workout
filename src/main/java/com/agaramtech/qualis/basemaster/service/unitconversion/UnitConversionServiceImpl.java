package com.agaramtech.qualis.basemaster.service.unitconversion;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.basemaster.model.UnitConversion;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'unitconversion' table
 * through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class UnitConversionServiceImpl implements UnitConversionService {

	private UnitConversionDAO unitConversionDAO;
	private CommonFunction commonFunction;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class properties.
	 * 
	 * @param unitConversionDAO UnitConversionDAO Interface
	 * @param commonFunction    CommonFunction holding common utility functions
	 */
	public UnitConversionServiceImpl(UnitConversionDAO unitConversionDAO, CommonFunction commonFunction) {
		this.unitConversionDAO = unitConversionDAO;
		this.commonFunction = commonFunction;
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available unitconversions with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of unitconversions records
	 *         with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getUnitConversion(UserInfo userInfo) throws Exception {
		return unitConversionDAO.getUnitConversion(userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available units with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of unit records with respect
	 *         to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getUnit(UserInfo userInfo) throws Exception {
		return unitConversionDAO.getUnit(userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available conversion operators with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of conversion operator records
	 *         with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getConversionOperator(UserInfo userInfo) throws Exception {
		return unitConversionDAO.getConversionOperator(userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * retrieve active unitconversion object based on the specified
	 * nunitConversionCode.
	 * 
	 * @param nunitConversionCode [int] primary key of unitconversion object
	 * @param userInfo            [UserInfo] holding logged in user details based on
	 *                            which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         unitconversion object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveUnitConversionById(int nunitConversionCode, UserInfo userInfo)
			throws Exception {
		final UnitConversion unitconversion = unitConversionDAO.getActiveUnitConversionById(nunitConversionCode,
				userInfo);
		if (unitconversion == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(unitconversion, HttpStatus.OK);
		}
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to unitconversion table.
	 * 
	 * @param objUnitConversion [UnitConversion] object holding details to be added
	 *                          in unitconversion table
	 * @param userInfo          [UserInfo] holding logged in user details and
	 *                          nmasterSiteCode [int] primary key of site object for
	 *                          which the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         unitconversion object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createUnitConversion(UnitConversion objUnitConversion, UserInfo userInfo)
			throws Exception {
		return unitConversionDAO.createUnitConversion(objUnitConversion, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * update entry in unitconversion table.
	 * 
	 * @param objUnitConversion [UnitConversion] object holding details to be
	 *                          updated in unitconversion table
	 * @param userInfo          [UserInfo] holding logged in user details and
	 *                          nmasterSiteCode [int] primary key of site object for
	 *                          which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         unitconversion object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateUnitConversion(UnitConversion objUnitConversion, UserInfo userInfo)
			throws Exception {
		return unitConversionDAO.updateUnitConversion(objUnitConversion, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * delete an entry in unitconversion table.
	 * 
	 * @param objUnitConversion [UnitConversion] object holding detail to be deleted
	 *                          from unitconversion table
	 * @param userInfo          [UserInfo] holding logged in user details and
	 *                          nmasterSiteCode [int] primary key of site object for
	 *                          which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         unitconversion object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteUnitConversion(UnitConversion objUnitConversion, UserInfo userInfo)
			throws Exception {
		return unitConversionDAO.deleteUnitConversion(objUnitConversion, userInfo);
	}

}
