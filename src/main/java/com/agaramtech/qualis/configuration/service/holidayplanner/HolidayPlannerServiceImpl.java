package com.agaramtech.qualis.configuration.service.holidayplanner;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.configuration.model.HolidayYearVersion;
import com.agaramtech.qualis.configuration.model.HolidaysYear;
import com.agaramtech.qualis.configuration.model.UserBasedHolidays;
import com.agaramtech.qualis.configuration.model.CommonHolidays;
import com.agaramtech.qualis.configuration.model.PublicHolidays;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'HolidaysYear' table
 * through its DAO layer.
 */

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class HolidayPlannerServiceImpl implements HolidayPlannerService {

	private final HolidayPlannerDAO holidayPlannerDAO;
	
	public HolidayPlannerServiceImpl(HolidayPlannerDAO holidayPlannerDAO) {
		super();
		this.holidayPlannerDAO = holidayPlannerDAO;
	}

	/**
	 * This method is used to retrieve list of all active holidayyear(s) and their
	 * associated details (Holiday year version, Common holidays and Public holidays
	 * details) on the specified site through its DAO layer
	 * 
	 * @param nyearcode [int] primary key of site object for which the list is to be
	 *                  fetched
	 * @param userInfo  [UserInfo] object holding details of logged in user.
	 * @return response entity object holding response status and list of
	 *         Holidayyear, Holiday year version, Common Holidays and Public
	 *         Holidays detail objects
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getHolidayPlanner(UserInfo userInfo, int nyearcode) throws Exception {
		return holidayPlannerDAO.getHolidayPlanner(userInfo, nyearcode);
	}

	/**
	 * This interface declaration is used to retrieve active holidayyear object
	 * based on the specified nyearcode through its DAO layer.
	 * 
	 * @param nyearcode [int] primary key of site object for which the list is to be
	 *                  fetched
	 * @param userInfo  [UserInfo] object holding details of logged in user.
	 * @return response entity object holding response status and data of
	 *         holidayyear object
	 * @throws Exception that are thrown in the DAO layer
	 */
	
	@Override
	public ResponseEntity<Object> getYearByID(int nyearcode, UserInfo userInfo) throws Exception {
		return holidayPlannerDAO.getYearByID(nyearcode, userInfo);
	}

	/**
	 * This method is used to retrieve active holidayyearversion object based on the
	 * specified nholidayYearVersion through its DAO layer.
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
	
	@Override
	public ResponseEntity<Object> getYearVersionByID(int nholidayYearVersion, int nyearcode, UserInfo userInfo)
			throws Exception {
		return holidayPlannerDAO.getYearVersionByID(nholidayYearVersion, nyearcode, userInfo);
	}

	/**
	 * This method is used to retrieve active commonholidays object based on the
	 * specified ncommonholidaycode through its DAO layer.
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
	
	@Override
	public ResponseEntity<Object> getCommonHolidaysByID(int ncommonholidaycode, int nholidayYearVersion, int nyearcode,
			UserInfo userInfo) throws Exception {
		return holidayPlannerDAO.getCommonHolidaysByID(ncommonholidaycode, nholidayYearVersion, nyearcode, userInfo);
	}

	/**
	 * This method is used to retrieve active publicholidays object based on the
	 * specified npublicholidaycode through its DAO layer.
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
	
	@Override
	public ResponseEntity<Object> getPublicHolidaysByID(int npublicholidaycode, int nholidayYearVersion, int nyearcode,
			UserInfo userInfo) throws Exception {
		return holidayPlannerDAO.getPublicHolidaysByID(npublicholidaycode, nholidayYearVersion, nyearcode, userInfo);
	}

	/**
	 * This method is used to retrieve list of all active holidayyearversion site
	 * details through its DAO layer
	 * 
	 * @param nyearcode [int] primary key of site object for which the list is to be
	 *                  fetched
	 * @param userInfo  [UserInfo] object holding details of logged in user.
	 * @return response entity object holding response status and list of
	 *         holidayyearversion details
	 * @throws Exception that are thrown in the DAO layer
	 */
	
	@Override
	public List<HolidayYearVersion> getListYearVersion(UserInfo userInfo, int nyearcode) throws Exception {
		return holidayPlannerDAO.getListYearVersion(userInfo, nyearcode);
	}

	/**
	 * This method is used to retrieve selected of active holidayyear(s) and their
	 * associated details (Holiday year version, Common holidays and Public holidays
	 * details) on the specified site through its DAO layer
	 * 
	 * @param nyearcode [int] primary key of site object for which the list is to be
	 *                  fetched
	 * @param userInfo  [UserInfo] object holding details of logged in user.
	 * @return response entity object holding response status and list of
	 *         Holidayyear, Holiday year version, Common Holidays and Public
	 *         Holidays detail objects
	 * @throws Exception that are thrown in the DAO layer
	 */
	
	@Override
	public ResponseEntity<Object> getSelectionAllHolidayPlanner(UserInfo userInfo, int nyearcode,
			final int selectedVersionCode) throws Exception {
		return holidayPlannerDAO.getSelectionAllHolidayPlanner(userInfo, nyearcode, selectedVersionCode);
	}

	/**
	 * This method is used to add a new entry to holidaysYear table through its DAO
	 * layer.
	 * 
	 * @param holidaysYear [HolidaysYear] object holding details to be added in
	 *                     holidaysYear table
	 * @return response entity object holding response status and data of added
	 *         holidaysYear object
	 * @throws Exception that are thrown in the DAO layer
	 */
	
	@Transactional
	@Override
	public ResponseEntity<Object> createHolidayYear(HolidaysYear holidaysYear, UserInfo userInfo) throws Exception {
		return holidayPlannerDAO.createHolidayYear(holidaysYear, userInfo);
	}

	/**
	 * This method is used to update entry in holidaysYear table through its DAO
	 * layer.
	 * 
	 * @param holidaysYear [HolidaysYear] object holding details to be added in
	 *                     holidaysYear table
	 * @return response entity object holding response status and data of added
	 *         holidaysYear object
	 * @throws Exception that are thrown in the DAO layer
	 */

	@Transactional
	@Override
	public ResponseEntity<Object> updateHolidayYear(HolidaysYear holidaysYear, UserInfo userInfo) throws Exception {
		return holidayPlannerDAO.updateHolidayYear(holidaysYear, userInfo);
	}

	/**
	 * This method is used to delete entry in holidaysYear table through its DAO
	 * layer.
	 * 
	 * @param holidaysYear [HolidaysYear] object holding details to be added in
	 *                     holidaysYear table
	 * @return response entity object holding response status and data of added
	 *         holidaysYear object
	 * @throws Exception that are thrown in the DAO layer
	 */
	
	@Transactional
	@Override
	public ResponseEntity<Object> deleteHolidayYear(HolidaysYear holidaysYear, UserInfo userInfo) throws Exception {
		return holidayPlannerDAO.deleteHolidayYear(holidaysYear, userInfo);
	}

	/**
	 * This method is used to add a new entry to holidayyearversion table through
	 * its DAO layer.
	 * 
	 * @param holidayyearversion [HolidayYearVersion] object holding details to be
	 *                           added in holidayyearversion table
	 * @return response entity object holding response status and data of added
	 *         holidayyearversion object
	 * @throws Exception that are thrown in the DAO layer
	 */
	
	@Transactional
	@Override
	public ResponseEntity<Object> createYearVersion(HolidayYearVersion holidayYearVersion, UserInfo userInfo)
			throws Exception {
		return holidayPlannerDAO.createYearVersion(holidayYearVersion, userInfo);
	}

	/**
	 * This method is used to delete entry in holidayyearversion table through its
	 * DAO layer.
	 * 
	 * @param holidayyearversion [HolidayYearVersion] object holding details to be
	 *                           added in holidayyearversion table
	 * @return response entity object holding response status and data of added
	 *         holidayyearversion object
	 * @throws Exception that are thrown in the DAO layer
	 */
	
	@Transactional
	@Override
	public ResponseEntity<Object> deleteYearVersion(HolidayYearVersion holidayYearVersion, UserInfo userInfo)
			throws Exception {
		return holidayPlannerDAO.deleteYearVersion(holidayYearVersion, userInfo);
	}

	/**
	 * This method is used to approve entry in holidayyearversion table through its
	 * DAO layer.
	 * 
	 * @param holidayyearversion [HolidayYearVersion] object holding details to be
	 *                           added in holidayyearversion table
	 * @return response entity object holding response status and data of added
	 *         holidayyearversion object
	 * @throws Exception that are thrown in the DAO layer
	 */
	
	@Transactional
	@Override
	public ResponseEntity<Object> ApproveYearVersion(HolidayYearVersion holidayYearVersion, UserInfo userInfo)
			throws Exception {
		return holidayPlannerDAO.ApproveYearVersion(holidayYearVersion, userInfo);
	}

	/**
	 * This method is used to update entry in commonholidays table through its DAO
	 * layer.
	 * 
	 * @param commonholidays [CommonHolidays] object holding details to be added in
	 *                       commonholidays table
	 * @return response entity object holding response status and data of added
	 *         commonholidays object
	 * @throws Exception that are thrown in the DAO layer
	 */
	
	@Transactional
	@Override
	public ResponseEntity<Object> updateCommonHolidays(CommonHolidays commonHolidays, UserInfo userInfo)
			throws Exception {
		return holidayPlannerDAO.updateCommonHolidays(commonHolidays, userInfo);
	}

	/**
	 * This method is used to add a new entry to publicholidays table through its
	 * DAO layer.
	 * 
	 * @param publicHolidays [PublicHolidays] object holding details to be added in
	 *                       publicholidays table
	 * @return response entity object holding response status and data of added
	 *         publicholidays object
	 * @throws Exception that are thrown in the DAO layer
	 */
	
	@Transactional
	@Override
	public ResponseEntity<Object> createPublicHolidays(PublicHolidays publicHolidays, UserInfo userInfo)
			throws Exception {
		return holidayPlannerDAO.createPublicHolidays(publicHolidays, userInfo);
	}

	/**
	 * This method is used to update entry in publicHolidays table through its DAO
	 * layer.
	 * 
	 * @param publicHolidays [PublicHolidays] object holding details to be added in
	 *                       publicHolidays table
	 * @return response entity object holding response status and data of added
	 *         publicHolidays object
	 * @throws Exception that are thrown in the DAO layer
	 */
	
	@Transactional
	@Override
	public ResponseEntity<Object> updatePublicHolidays(PublicHolidays publicHolidays, UserInfo userInfo)
			throws Exception {
		return holidayPlannerDAO.updatePublicHolidays(publicHolidays, userInfo);
	}

	/**
	 * This method is used to delete entry in publicHolidays table through its DAO
	 * layer.
	 * 
	 * @param publicHolidays [PublicHolidays] object holding details to be added in
	 *                       publicHolidays table
	 * @return response entity object holding response status and data of added
	 *         publicHolidays object
	 * @throws Exception that are thrown in the DAO layer
	 */
	
	@Transactional
	@Override
	public ResponseEntity<Object> deletePublicHolidays(PublicHolidays publicHolidays, UserInfo userInfo)
			throws Exception {
		return holidayPlannerDAO.deletePublicHolidays(publicHolidays, userInfo);
	}

	/**
	 * This method is used to retrieve list of all active commmon holidays and
	 * Public holidays details through its DAO layer
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
	
	
	@Override
	public ResponseEntity<Object> getSelectedCommonAndPublicHolidays(int nyearcode, int nholidayYearVersion,
			UserInfo userInfo) throws Exception {

		return holidayPlannerDAO.getSelectedCommonAndPublicHolidays(nyearcode, nholidayYearVersion, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createUserBasedHolidays(UserBasedHolidays userBasedHoildays, UserInfo userInfo)
			throws Exception {
		return holidayPlannerDAO.createUserBasedHolidays(userBasedHoildays, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteUserBasedHolidays(UserBasedHolidays userBasedHoildays, UserInfo userInfo)
			throws Exception {
		return holidayPlannerDAO.deleteUserBasedHolidays(userBasedHoildays, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateUserBasedHolidays(UserBasedHolidays userBasedHoildays, UserInfo userInfo)
			throws Exception {
		return holidayPlannerDAO.updateUserBasedHolidays(userBasedHoildays, userInfo);
	}

	@Override
	public ResponseEntity<Object> getUserBasedHolidaysByID(int nuserbasedholidaycode, int nholidayYearVersion,
			int nyearcode, UserInfo userInfo) throws Exception {
		return holidayPlannerDAO.getUserBasedHolidaysByID(nuserbasedholidaycode, nholidayYearVersion, nyearcode,
				userInfo);
	}

	
	@Override
	public ResponseEntity<Object> getUsers(UserInfo userInfo) throws Exception {
		return holidayPlannerDAO.getUsers(userInfo);
	}

}
