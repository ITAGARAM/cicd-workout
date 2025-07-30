package com.agaramtech.qualis.configuration.service.holidayplanner;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.configuration.model.HolidayYearVersion;
import com.agaramtech.qualis.configuration.model.HolidaysYear;
import com.agaramtech.qualis.configuration.model.UserBasedHolidays;
import com.agaramtech.qualis.configuration.model.CommonHolidays;
import com.agaramtech.qualis.configuration.model.PublicHolidays;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation on 'holidayyear'
 * table through its implementation class.
 */

public interface HolidayPlannerDAO {

	/**
	 * This interface declaration is used to retrieve list of all active
	 * holidayyear(s) and their associated details (Holiday year version, Common
	 * holidays and Public holidays details)
	 * 
	 * @param nyearcode [int] primary key of site object for which the list is to be
	 *                  fetched
	 * @param userInfo  [UserInfo] object holding details of logged in user.
	 * @return response entity object holding response status and list of
	 *         Holidayyear, Holiday year version, Common Holidays and Public
	 *         Holidays detail objects
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getHolidayPlanner(UserInfo userInfo, int nyearcode) throws Exception;

	/**
	 * This interface declaration is used to retrieve selected of active
	 * holidayyear(s) and their associated details (Holiday year version, Common
	 * holidays and Public holidays details)
	 * 
	 * @param nyearcode [int] primary key of site object for which the list is to be
	 *                  fetched
	 * @param userInfo  [UserInfo] object holding details of logged in user.
	 * @return response entity object holding response status and list of
	 *         Holidayyear, Holiday year version, Common Holidays and Public
	 *         Holidays detail objects
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getSelectionAllHolidayPlanner(UserInfo userInfo, int nyearcode,
			final int selectedVersionCode) throws Exception;

	/**
	 * This interface declaration is used to retrieve list of all active commmon
	 * holidays and Public holidays details
	 * 
	 * @param nyearcode           [int] primary key of site object for which the
	 *                            list is to be fetched
	 * @param nholidayYearVersion [int] primary key of site object for which the
	 *                            list is to be fetched
	 * @param userInfo            [UserInfo] object holding details of logged in
	 *                            user.
	 * @return response entity object holding response status and list of Common
	 *         Holidays and Public Holidays detail objects
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getSelectedCommonAndPublicHolidays(int nyearcode, int nholidayYearVersion,
			UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active holidayyear object
	 * based on the specified nyearcode.
	 * 
	 * @param nyearcode [int] primary key of site object for which the list is to be
	 *                  fetched
	 * @param userInfo  [UserInfo] object holding details of logged in user.
	 * @return response entity object holding response status and data of
	 *         holidayyear object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getYearByID(int nyearcode, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active holidayyearversion
	 * object based on the specified nholidayYearVersion.
	 * 
	 * @param nyearcode           [int] primary key of site object for which the
	 *                            list is to be fetched
	 * @param nholidayYearVersion [int] primary key of site object for which the
	 *                            list is to be fetched
	 * @param userInfo            [UserInfo] object holding details of logged in
	 *                            user.
	 * @return response entity object holding response status and data of
	 *         holidayyearversion object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getYearVersionByID(int nholidayYearVersion, int nyearcode, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to retrieve active commonholidays object
	 * based on the specified ncommonholidaycode.
	 * 
	 * @param ncommonholidaycode  [int] primary key of site object for which the
	 *                            list is to be fetched
	 * @param nyearcode           [int] primary key of site object for which the
	 *                            list is to be fetched
	 * @param nholidayYearVersion [int] primary key of site object for which the
	 *                            list is to be fetched
	 * @param userInfo            [UserInfo] object holding details of logged in
	 *                            user.
	 * @return response entity object holding response status and data of
	 *         commonholidays object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getCommonHolidaysByID(int ncommonholidaycode, int nholidayYearVersion, int nyearcode,
			UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active publicholidays object
	 * based on the specified npublicholidaycode.
	 * 
	 * @param npublicholidaycode  [int] primary key of site object for which the
	 *                            list is to be fetched
	 * @param nyearcode           [int] primary key of site object for which the
	 *                            list is to be fetched
	 * @param nholidayYearVersion [int] primary key of site object for which the
	 *                            list is to be fetched
	 * @param userInfo            [UserInfo] object holding details of logged in
	 *                            user.
	 * @return response entity object holding response status and data of
	 *         publicholidays object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getPublicHolidaysByID(int npublicholidaycode, int nholidayYearVersion, int nyearcode,
			UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve list of all active
	 * holidayyearversion site details
	 * 
	 * @param nyearcode [int] primary key of site object for which the list is to be
	 *                  fetched
	 * @param userInfo  [UserInfo] object holding details of logged in user.
	 * @return response entity object holding response status and list of
	 *         holidayyearversion details
	 * @throws Exception that are thrown in the DAO layer
	 */
	public List<HolidayYearVersion> getListYearVersion(UserInfo userInfo, int nyearcode) throws Exception;

	/**
	 * This interface declaration is used to add a new entry to holidaysYear table.
	 * 
	 * @param holidaysYear [HolidaysYear] object holding details to be added in
	 *                     holidaysYear table
	 * @return response entity object holding response status and data of added
	 *         holidaysYear object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createHolidayYear(HolidaysYear holidaysYear, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in holidaysYear table.
	 * 
	 * @param holidaysYear [HolidaysYear] object holding details to be added in
	 *                     holidaysYear table
	 * @return response entity object holding response status and data of added
	 *         holidaysYear object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateHolidayYear(HolidaysYear holidaysYear, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to delete entry in holidaysYear table.
	 * 
	 * @param holidaysYear [HolidaysYear] object holding details to be added in
	 *                     holidaysYear table
	 * @return response entity object holding response status and data of added
	 *         holidaysYear object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteHolidayYear(HolidaysYear holidaysYear, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to add a new entry to holidayyearversion
	 * table.
	 * 
	 * @param holidayyearversion [HolidayYearVersion] object holding details to be
	 *                           added in holidayyearversion table
	 * @return response entity object holding response status and data of added
	 *         holidayyearversion object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createYearVersion(HolidayYearVersion holidayYearVersion, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to delete entry in holidayyearversion
	 * table.
	 * 
	 * @param holidayyearversion [HolidayYearVersion] object holding details to be
	 *                           added in holidayyearversion table
	 * @return response entity object holding response status and data of added
	 *         holidayyearversion object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteYearVersion(HolidayYearVersion holidayYearVersion, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to approve entry in holidayyearversion
	 * table.
	 * 
	 * @param holidayyearversion [HolidayYearVersion] object holding details to be
	 *                           added in holidayyearversion table
	 * @return response entity object holding response status and data of added
	 *         holidayyearversion object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> ApproveYearVersion(HolidayYearVersion holidayYearVersion, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to update entry in commonholidays table.
	 * 
	 * @param commonholidays [CommonHolidays] object holding details to be added in
	 *                       commonholidays table
	 * @return response entity object holding response status and data of added
	 *         commonholidays object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateCommonHolidays(CommonHolidays commonHolidays, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to add a new entry to publicholidays
	 * table.
	 * 
	 * @param publicHolidays [PublicHolidays] object holding details to be added in
	 *                       publicholidays table
	 * @return response entity object holding response status and data of added
	 *         publicholidays object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createPublicHolidays(PublicHolidays publicHolidays, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to update entry in publicHolidays table.
	 * 
	 * @param publicHolidays [PublicHolidays] object holding details to be added in
	 *                       publicHolidays table
	 * @return response entity object holding response status and data of added
	 *         publicHolidays object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updatePublicHolidays(PublicHolidays publicHolidays, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to delete entry in publicHolidays table.
	 * 
	 * @param publicHolidays [PublicHolidays] object holding details to be added in
	 *                       publicHolidays table
	 * @return response entity object holding response status and data of added
	 *         publicHolidays object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deletePublicHolidays(PublicHolidays publicHolidays, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createUserBasedHolidays(UserBasedHolidays userBasedHoildays, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> deleteUserBasedHolidays(UserBasedHolidays userBasedHoildays, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateUserBasedHolidays(UserBasedHolidays userBasedHoildays, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getUserBasedHolidaysByID(int nuserbasedholidaycode, int nholidayYearVersion,
			int nyearcode, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getUsers(UserInfo userInfo) throws Exception;

}
