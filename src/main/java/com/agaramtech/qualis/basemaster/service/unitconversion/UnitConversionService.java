package com.agaramtech.qualis.basemaster.service.unitconversion;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.basemaster.model.UnitConversion;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface declaration holds methods to perform CRUD operation on
 * 'unitconversion' table
 */
public interface UnitConversionService {

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * get all the available unitconversions with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of unitconversions records
	 *         with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getUnitConversion(UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * get all the available units with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of unit records with respect
	 *         to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getUnit(UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * retrieve active unit conversion object based on the specified
	 * nunitConversionCode.
	 * 
	 * @param nunitCode [int] primary key of unit conversion object
	 * @param userInfo  [UserInfo] holding logged in user details based on which the
	 *                  list is to be fetched
	 * @return response entity object holding response status and data of unit
	 *         conversion object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveUnitConversionById(final int nunitConversionCode, UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * add a new entry to unit conversion table.
	 * 
	 * @param objUnitConversion [UnitConversion] object holding details to be added
	 *                          in unit conversion table
	 * @param userInfo          [UserInfo] holding logged in user details and
	 *                          nmasterSiteCode [int] primary key of site object for
	 *                          which the list is to be fetched
	 * @return response entity object holding response status and data of added unit
	 *         conversion object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createUnitConversion(UnitConversion objUnitConversion, UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * update entry in unit conversion table.
	 * 
	 * @param objUnitConversion [UnitConversion] object holding details to be
	 *                          updated in unit conversion table
	 * @param userInfo          [UserInfo] holding logged in user details and
	 *                          nmasterSiteCode [int] primary key of site object for
	 *                          which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         unit conversion object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateUnitConversion(UnitConversion objUnitConversion, UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * delete an entry in unit conversion table.
	 * 
	 * @param objUnitConversion [UnitConversion] object holding detail to be deleted
	 *                          from unit conversion table
	 * @param userInfo          [UserInfo] holding logged in user details and
	 *                          nmasterSiteCode [int] primary key of site object for
	 *                          which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         unit conversion object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteUnitConversion(UnitConversion objUnitConversion, UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * get all the available conversion operators with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of conversion operators
	 *         records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getConversionOperator(UserInfo userInfo) throws Exception;

}
