package com.agaramtech.qualis.basemaster.service.calendarproperties;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation on
 * 'CalenderProperties' table
 */

public interface CalenderPropertiesDAO {
	/**
	 * This interface declaration is used to get all the available
	 * CalenderPropertiess with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of CalenderProperties records
	 *         with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	ResponseEntity<Object> getCalenderProperties(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in CalenderProperties
	 * table.
	 * 
	 * @param objCalenderProperties [CalenderProperties] object holding details to
	 *                              be updated in CalenderProperties table
	 * @param userInfo              [UserInfo] holding logged in user details and
	 *                              nmasterSiteCode [int] primary key of site object
	 *                              for which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         CalenderProperties object
	 * @throws Exception that are thrown in the DAO layer
	 */

	ResponseEntity<Object> updateCalenderProperties(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

}
