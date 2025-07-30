package com.agaramtech.qualis.basemaster.service.unitconversion;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.basemaster.model.UnitConversion;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation on
 * 'unitconversion' table
 */
public interface UnitConversionDAO {

	/**
	 * This interface declaration is used to get all the available unitconversions
	 * with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of unitconversion records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getUnitConversion(UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to get all the available units with
	 * respect to site
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
	 * This interface declaration is used to retrieve active unitconversion object
	 * based on the specified nunitConversionCode.
	 * 
	 * @param nunitConversionCode [int] primary key of unitconversion object
	 * @param userInfo            [UserInfo] holding logged in user details based on
	 *                            which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         unitconversion object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public UnitConversion getActiveUnitConversionById(final int nunitConversionCode, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to add a new entry to unitconversion
	 * table.
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
	public ResponseEntity<Object> createUnitConversion(UnitConversion objUnitConversion, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to update entry in unitconversion table.
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
	public ResponseEntity<Object> updateUnitConversion(UnitConversion objUnitConversion, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to delete an entry in unitconversion
	 * table.
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
	public ResponseEntity<Object> deleteUnitConversion(UnitConversion objUnitConversion, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to get all the available conversion
	 * operators with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of conversion operator records
	 *         with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getConversionOperator(UserInfo userInfo) throws Exception;
}
