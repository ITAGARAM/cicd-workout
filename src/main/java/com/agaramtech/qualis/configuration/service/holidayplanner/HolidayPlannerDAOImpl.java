package com.agaramtech.qualis.configuration.service.holidayplanner;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.configuration.model.CommonHolidays;
import com.agaramtech.qualis.configuration.model.HolidayYearVersion;
import com.agaramtech.qualis.configuration.model.HolidaysYear;
import com.agaramtech.qualis.configuration.model.PublicHolidays;
import com.agaramtech.qualis.configuration.model.SeqNoConfigurationMaster;
import com.agaramtech.qualis.configuration.model.UserBasedHolidays;
import com.agaramtech.qualis.credential.model.Users;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "HolidaysYear" table by
 * implementing methods from its interface.
 */
@RequiredArgsConstructor
@Repository
public class HolidayPlannerDAOImpl implements HolidayPlannerDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(HolidayPlannerDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

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
	public ResponseEntity<Object> getHolidayPlanner(final UserInfo userInfo, int nyearcode) throws Exception {
		Map<String, Object> objMap = new LinkedHashMap<String, Object>();
		final String strQuery = " select * from holidaysyear where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
				+ userInfo.getNmastersitecode();
		List<HolidaysYear> lstHolidayYear = jdbcTemplate.query(strQuery, new HolidaysYear());
		if (nyearcode == 0 && lstHolidayYear.size() > 0) {
			nyearcode = lstHolidayYear.get(lstHolidayYear.size() - 1).getNyearcode();
		}
		List<HolidayYearVersion> lstYearVersion = getListYearVersion(userInfo, nyearcode);
		int nHolidayYearVersion = 0;
		HolidayYearVersion selectedYearVersion = new HolidayYearVersion();
		if (lstYearVersion.size() > 0) {
			selectedYearVersion = lstYearVersion.get(lstYearVersion.size() - 1);
			nHolidayYearVersion = lstYearVersion.get(lstYearVersion.size() - 1).getNholidayyearversion();
		}
		objMap.put("HolidayYear", lstHolidayYear);
		objMap.put("YearVersion", lstYearVersion);
		objMap.put("CommonHolidays", getListCommonHolidays(nHolidayYearVersion, nyearcode));
		objMap.put("PublicHolidays", getListPublicHolidays(nHolidayYearVersion, nyearcode, userInfo));
		objMap.put("UserBasedHolidays", getListUserBasedHolidays(nHolidayYearVersion, nyearcode, userInfo));
		objMap.put("selectedYear", getYearByIDForInsert(nyearcode, userInfo));
		objMap.put("CurrentYearVersion", nHolidayYearVersion);
		objMap.put("selectedYearVersion", selectedYearVersion);
		LOGGER.info("getHolidayPlanner service Invoked...");
		return new ResponseEntity<Object>(objMap, HttpStatus.OK);

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
	public ResponseEntity<Object> getSelectionAllHolidayPlanner(final UserInfo userInfo, final int nyearcode,
			final int selectedVersionCode) throws Exception {
		Map<String, Object> objMap = new LinkedHashMap<String, Object>();
		final HolidaysYear holidaysYearByID = (HolidaysYear) getYearByIDForInsert(nyearcode, userInfo);
		if (holidaysYearByID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		List<HolidayYearVersion> lstYearVersion = getListYearVersion(userInfo, nyearcode);
		int nHolidayYearVersion = 0;
		HolidayYearVersion selectedYearVersion = null;
		if (lstYearVersion.size() > 0) {
			if (selectedVersionCode == 0)
				selectedYearVersion = lstYearVersion.get(lstYearVersion.size() - 1);
			else
				selectedYearVersion = getYearVersionByIDForInsert(selectedVersionCode, holidaysYearByID.getNyearcode(),
						userInfo);

			nHolidayYearVersion = selectedYearVersion.getNholidayyearversion();
		}

		objMap.put("YearVersion", lstYearVersion);
		objMap.put("CommonHolidays", getListCommonHolidays(nHolidayYearVersion, nyearcode));
		objMap.put("PublicHolidays", getListPublicHolidays(nHolidayYearVersion, nyearcode, userInfo));
		objMap.put("UserBasedHolidays", getListUserBasedHolidays(nHolidayYearVersion, nyearcode, userInfo));
		objMap.put("selectedYear", getYearByIDForInsert(nyearcode, userInfo));
		objMap.put("CurrentYearVersion", nHolidayYearVersion);
		objMap.put("selectedYearVersion", selectedYearVersion);
		return new ResponseEntity<Object>(objMap, HttpStatus.OK);
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
	private HolidaysYear getYearByIDForInsert(final int nyearcode, final UserInfo userInfo) throws Exception {
		final String strQuery = " select nyearcode, syear, COALESCE(sdescription, '') sdescription, nsitecode, nstatus from holidaysyear where "
				+ " nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and nyearcode = "
				+ nyearcode;
		return (HolidaysYear) jdbcUtilityFunction.queryForObject(strQuery, HolidaysYear.class, jdbcTemplate);
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
	public ResponseEntity<Object> getYearByID(final int nyearcode, final UserInfo userInfo) throws Exception {
		List<HolidayYearVersion> lstYearVersion = getListYearVersion(userInfo, nyearcode);
		lstYearVersion = lstYearVersion.stream().filter(xField -> xField
				.getNtransactionstatus() == Enumeration.TransactionStatus.APPROVED.gettransactionstatus())
				.collect(Collectors.toList());
		if (lstYearVersion.size() > 0) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CANNOTEDITONEOFTHEVERSIONISAPPROVED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		final String strQuery = " select * from holidaysyear where " + " nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and nyearcode = " + nyearcode;
		final HolidaysYear objYear = (HolidaysYear) jdbcUtilityFunction.queryForObject(strQuery, HolidaysYear.class,
				jdbcTemplate);
		if (objYear != null) {
			return new ResponseEntity<Object>(objYear, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
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
	private HolidayYearVersion getYearVersionByIDForInsert(final int nholidayYearVersion, final int nyearcode,
			UserInfo userInfo) throws Exception {
		final String strQuery = " select a.*, ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "' as stransdisplaystatus from holidayyearversion a , transactionstatus ts where ts.ntranscode = a.ntransactionstatus and "
				+ "  a.nstatus = ts.nstatus and a.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and a.nyearcode = " + nyearcode
				+ " and a.nholidayyearversion = " + nholidayYearVersion;
		return (HolidayYearVersion) jdbcUtilityFunction.queryForObject(strQuery, HolidayYearVersion.class,
				jdbcTemplate);
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
	private CommonHolidays getCommonHolidaysByIDForInsert(final int ncommonholidaycode, final int nholidayYearVersion,
			final int nyearcode, final int nmastersitecode) throws Exception {
		final String strQuery = " select * from commonholidays where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ncommonholidaycode = "
				+ ncommonholidaycode + " and nyearcode = " + nyearcode + " and nholidayyearversion = "
				+ nholidayYearVersion;
		return (CommonHolidays) jdbcUtilityFunction.queryForObject(strQuery, CommonHolidays.class, jdbcTemplate);
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
	private PublicHolidays getPublicHolidaysByIDForInsert(final int npublicholidaycode, final int nholidayYearVersion,
			final int nyearcode, final UserInfo userInfo) throws Exception {
		final String strQuery = " select a.*,COALESCE(TO_CHAR(a.ddate,'" + userInfo.getSsitedatetime()
				+ "'),'') as sdate, tz.stimezoneid as stzddate from publicholidays a , timezone tz where "
				+ " a.ntzddate = tz.ntimezonecode and   a.nyearcode = " + nyearcode + " and  a.npublicholidaycode = "
				+ npublicholidaycode + "  and a.nholidayyearversion = " + nholidayYearVersion + " and a.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tz.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and a.nsitecode = "
				+ userInfo.getNmastersitecode();
		return (PublicHolidays) jdbcUtilityFunction.queryForObject(strQuery, PublicHolidays.class, jdbcTemplate);
	}

	private UserBasedHolidays getUserBasedHolidaysByIDForInsert(final int nuserbasedholidaycode,
			final int nholidayYearVersion, final int nyearcode, final UserInfo userInfo) throws Exception {
		final String strQuery = " select a.*,COALESCE(TO_CHAR(a.ddate,'" + userInfo.getSsitedatetime()
				+ "'),'') as sdate, tz.stimezoneid as stzddate,u.sfirstname||' '||u.slastname as susername from userbasedholidays a , timezone tz,users u where "
				+ " a.nusercode=u.nusercode and a.ntzddate = tz.ntimezonecode and   a.nyearcode = " + nyearcode
				+ " and  a.nuserbasedholidaycode = " + nuserbasedholidaycode + "  and a.nholidayyearversion = "
				+ nholidayYearVersion + " and a.nsitecode = " + userInfo.getNmastersitecode() + " and a.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tz.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return (UserBasedHolidays) jdbcUtilityFunction.queryForObject(strQuery, UserBasedHolidays.class, jdbcTemplate);
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
	public ResponseEntity<Object> getYearVersionByID(final int nholidayYearVersion, final int nyearcode,
			final UserInfo userInfo) throws Exception {
		final HolidayYearVersion holidaysYearVersionByID = (HolidayYearVersion) getYearVersionByIDForInsert(
				nholidayYearVersion, nyearcode, userInfo);
		if (holidaysYearVersionByID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (holidaysYearVersionByID.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT
					.gettransactionstatus()) {
				final String strQuery = " select a.*, ts.jsondata->'stransdisplaystatus'->>'"
						+ userInfo.getSlanguagetypecode()
						+ "' as stransdisplaystatus from holidayyearversion a , transactionstatus ts where ts.ntranscode = a.ntransactionstatus and "
						+ "  a.nstatus = ts.nstatus and a.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and a.nyearcode = "
						+ nyearcode + " and a.nholidayyearversion = " + nholidayYearVersion;
				final HolidayYearVersion objYearVersion = (HolidayYearVersion) jdbcUtilityFunction
						.queryForObject(strQuery, HolidayYearVersion.class, jdbcTemplate);
				return new ResponseEntity<Object>(objYearVersion, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDYEARVERSIONMUSTBEDRAFT",
						userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}
		}
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
	public ResponseEntity<Object> getCommonHolidaysByID(final int ncommonholidaycode, final int nholidayYearVersion,
			final int nyearcode, final UserInfo userInfo) throws Exception {
		final HolidayYearVersion holidaysYearVersionByID = (HolidayYearVersion) getYearVersionByIDForInsert(
				nholidayYearVersion, nyearcode, userInfo);
		if (holidaysYearVersionByID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (holidaysYearVersionByID.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT
					.gettransactionstatus()) {
				final String strQuery = " select * from commonholidays where nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ncommonholidaycode = "
						+ ncommonholidaycode + " and nyearcode = " + nyearcode + " and nholidayyearversion = "
						+ nholidayYearVersion;
				final CommonHolidays objCommonHolidays = (CommonHolidays) jdbcUtilityFunction.queryForObject(strQuery,
						CommonHolidays.class, jdbcTemplate);
				return new ResponseEntity<Object>(objCommonHolidays, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDYEARVERSIONMUSTBEDRAFT",
						userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}
		}

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
	public ResponseEntity<Object> getPublicHolidaysByID(final int npublicholidaycode, final int nholidayYearVersion,
			final int nyearcode, final UserInfo userInfo) throws Exception {
		final HolidayYearVersion holidaysYearVersionByID = (HolidayYearVersion) getYearVersionByIDForInsert(
				nholidayYearVersion, nyearcode, userInfo);
		if (holidaysYearVersionByID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (holidaysYearVersionByID.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT
					.gettransactionstatus()) {
				final String strQuery = " select a.*, COALESCE(TO_CHAR(a.ddate,'" + userInfo.getSsitedate()
						+ "'),'') as sdate, tz.stimezoneid as stzddate from publicholidays a , timezone tz where "
						+ " a.ntzddate = tz.ntimezonecode and a.nyearcode = " + nyearcode + " and a.nsitecode = "
						+ userInfo.getNmastersitecode() + " and a.npublicholidaycode = " + npublicholidaycode
						+ "  and a.nholidayyearversion = " + nholidayYearVersion + " and a.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tz.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final PublicHolidays objPublicHolidays = (PublicHolidays) jdbcUtilityFunction.queryForObject(strQuery,
						PublicHolidays.class, jdbcTemplate);
				return new ResponseEntity<Object>(objPublicHolidays, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDYEARVERSIONMUSTBEDRAFT",
						userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}
		}

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
	public List<HolidayYearVersion> getListYearVersion(final UserInfo userInfo, final int nyearcode) throws Exception {
		String sVersionNo = (commonFunction.getMultilingualMessage("IDS_VERSIONNO", userInfo.getSlanguagefilename()));
		final String strQuery = " select a.*,ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "'    as stransdisplaystatus, '" + sVersionNo
				+ "' ||' : ' || cast (nversioncode as varchar(20)) as sversionno from holidayyearversion a, transactionstatus ts where ts.ntranscode = a.ntransactionstatus and "
				+ "  a.nstatus = ts.nstatus and a.nyearcode = " + nyearcode + " and a.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return (List<HolidayYearVersion>) jdbcTemplate.query(strQuery, new HolidayYearVersion());
	}

	/**
	 * This method is used to retrieve list of all active commonholidays site
	 * details through its DAO layer
	 * 
	 * @param nyearcode           [int] primary key of site object for which the
	 *                            list is to be fetched
	 * @param nholidayYearVersion [int] primary key of site object for which the
	 *                            list is to be fetched
	 * @return response entity object holding response status and list of
	 *         commonholidays details
	 * @throws Exception that are thrown in the DAO layer
	 */
	private List<CommonHolidays> getListCommonHolidays(final int nholidayYearVersion, final int nyearcode)
			throws Exception {
		final String strQuery = " select * from commonholidays where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nyearcode = " + nyearcode
				+ " and nholidayyearversion = " + nholidayYearVersion;
		return (List<CommonHolidays>) jdbcTemplate.query(strQuery, new CommonHolidays());
	}

	/**
	 * This method is used to retrieve list of all active publicholidays site
	 * details through its DAO layer
	 * 
	 * @param nyearcode           [int] primary key of site object for which the
	 *                            list is to be fetched
	 * @param nholidayYearVersion [int] primary key of site object for which the
	 *                            list is to be fetched
	 * @return response entity object holding response status and list of
	 *         publicholidays details
	 * @throws Exception that are thrown in the DAO layer
	 */
	public List<PublicHolidays> getListPublicHolidays(final int nholidayYearVersion, final int nyearcode,
			final UserInfo userInfo) throws Exception {
		final String strQuery = " select *, COALESCE(TO_CHAR(ddate,'" + userInfo.getSsitedate()
				+ "'),'') as sdate from publicholidays  where nsitecode = " + userInfo.getNmastersitecode()
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  nyearcode = "
				+ nyearcode + " and  nholidayyearversion = " + nholidayYearVersion;
		final List<PublicHolidays> publicholidayList = (List<PublicHolidays>) jdbcTemplate.query(strQuery,
				new PublicHolidays());
		return publicholidayList;
	}

	public List<UserBasedHolidays> getListUserBasedHolidays(final int nholidayYearVersion, final int nyearcode,
			final UserInfo userInfo) throws Exception {
		final String strQuery = " select u.*,us.sfirstname||' '||us.slastname susername, COALESCE(TO_CHAR(ddate,'"
				+ userInfo.getSsitedate() + "'),'') as sdate from userbasedholidays u,users us where u.nsitecode = "
				+ userInfo.getNmastersitecode() + " and u.nusercode=us.nusercode  and u.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  nyearcode = " + nyearcode
				+ " and  nholidayyearversion = " + nholidayYearVersion;
		final List<UserBasedHolidays> userbasedholidayList = (List<UserBasedHolidays>) jdbcTemplate.query(strQuery,
				new UserBasedHolidays());
		return userbasedholidayList;
	}

	/**
	 * This method is used to fetch the active holidaysyear objects for the
	 * specified holidaysyear year and site.
	 * 
	 * @param sYear           [String] year for which the records are to be fetched
	 * @param nmasterSiteCode [int] primary key of site object
	 * @return list of active holidaysyear based on the specified holidaysyear year
	 *         and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private List<HolidaysYear> getHolidaysYearListByName(final String sYear, final int nmasterSiteCode)
			throws Exception {
		final String strQuery = "select  syear from holidaysyear where syear = N'"
				+ stringUtilityFunction.replaceQuote(sYear) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;
		return (List<HolidaysYear>) jdbcTemplate.query(strQuery, new HolidaysYear());
	}

	/**
	 * This method is used to fetch the active publicholidays objects for the
	 * specified publicholidays date and site.
	 * 
	 * @param ddate               [String] year for which the records are to be
	 *                            fetched
	 * @param nholidayyearversion [int] year for which the records are to be fetched
	 * @param nyearcode           [int] year for which the records are to be fetched
	 * @param nmasterSiteCode     [int] primary key of site object
	 * @return list of active publicholidays based on the specified publicholidays
	 *         date and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private List<PublicHolidays> getPublicHolidaysListByName(final String ddate, final int nholidayyearversion,
			final int nyearcode, final int nmastersitecode) throws Exception {
		final String strQuery = "select  * from publicholidays where nyearcode = " + nyearcode + " and nsitecode = "
				+ nmastersitecode + " and nholidayyearversion = " + nholidayyearversion
				+ " and cast(ddate as character varying) = '" + ddate + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return (List<PublicHolidays>) jdbcTemplate.query(strQuery, new PublicHolidays());
	}

	private List<UserBasedHolidays> getUserBasedHolidaysListByName(final String ddate, final int nholidayyearversion,
			final int nyearcode, int nusercode, final int nmastersitecode) throws Exception {
		final String strQuery = "select  * from userbasedholidays where nyearcode = " + nyearcode
				+ " and nholidayyearversion = " + nholidayyearversion + " and cast(ddate as character varying) = '"
				+ ddate + "' and nusercode=" + nusercode + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = " + nmastersitecode;
		return (List<UserBasedHolidays>) jdbcTemplate.query(strQuery, new UserBasedHolidays());
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
	public ResponseEntity<Object> getSelectedCommonAndPublicHolidays(final int nyearcode, final int nholidayYearVersion,
			final UserInfo userInfo) throws Exception {
		Map<String, Object> objMap = new LinkedHashMap<String, Object>();
		objMap.put("CommonHolidays", getListCommonHolidays(nholidayYearVersion, nyearcode));
		objMap.put("PublicHolidays", getListPublicHolidays(nholidayYearVersion, nyearcode, userInfo));
		objMap.put("UserBasedHolidays", getListUserBasedHolidays(nholidayYearVersion, nyearcode, userInfo));
		objMap.put("CurrentYearVersion", nholidayYearVersion);
		return new ResponseEntity<Object>(objMap, HttpStatus.OK);
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
	@Override
	public ResponseEntity<Object> createHolidayYear(HolidaysYear holidaysYear, final UserInfo userInfo)
			throws Exception {
		final String sQuery = " lock  table holidaysyear " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final List<HolidaysYear> holidayYear = getHolidaysYearListByName(holidaysYear.getSyear(),
				userInfo.getNmastersitecode());
		if (holidayYear.isEmpty()) {
			String sequencenoquery = "select nsequenceno from SeqNoConfigurationMaster where stablename ='holidaysyear'";
			int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
			nsequenceno++;
			String insertquery = "Insert into holidaysyear (nyearcode,syear,sdescription,dmodifieddate,nsitecode,nstatus)"
					+ "values(" + nsequenceno + ",N'" + stringUtilityFunction.replaceQuote(holidaysYear.getSyear())
					+ "',N'" + stringUtilityFunction.replaceQuote(holidaysYear.getSdescription()) + "','"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(insertquery);
			String updatequery = "update SeqNoConfigurationMaster set nsequenceno =" + nsequenceno
					+ " where stablename ='holidaysyear'";
			jdbcTemplate.execute(updatequery);
			holidaysYear.setNyearcode(nsequenceno);
			final List<String> multilingualIDList = new ArrayList<>();
			multilingualIDList.add("IDS_ADDHOLIDAYSYEAR");
			final List<Object> savedYearList = new ArrayList<>();
			savedYearList.add(holidaysYear);
			auditUtilityFunction.fnInsertAuditAction(savedYearList, 1, null, multilingualIDList, userInfo);
			return getHolidayPlanner(userInfo, holidaysYear.getNyearcode());
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}

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
	@Override
	public ResponseEntity<Object> updateHolidayYear(HolidaysYear holidaysYear, final UserInfo userInfo)
			throws Exception {
		final HolidaysYear holidaysYearByID = (HolidaysYear) getYearByIDForInsert(holidaysYear.getNyearcode(),
				userInfo);
		if (holidaysYearByID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select syear from holidaysyear where syear = '"
					+ stringUtilityFunction.replaceQuote(holidaysYear.getSyear()) + "' and nyearcode <> "
					+ holidaysYear.getNyearcode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final List<HolidaysYear> holidaysYearList = (List<HolidaysYear>) jdbcTemplate.query(queryString,
					new HolidaysYear());
			if (holidaysYearList.isEmpty()) {
				final String updateQueryString = "update holidaysyear set syear='"
						+ stringUtilityFunction.replaceQuote(holidaysYear.getSyear()) + "', sdescription ='"
						+ stringUtilityFunction.replaceQuote(holidaysYear.getSdescription()) + "' where nyearcode="
						+ holidaysYear.getNyearcode();

				jdbcTemplate.execute(updateQueryString);
				final List<String> multilingualIDList = new ArrayList<>();
				multilingualIDList.add("IDS_EDITHOLIDAYSYEAR");
				final List<Object> listAfterSave = new ArrayList<>();
				listAfterSave.add(holidaysYear);
				final List<Object> listBeforeSave = new ArrayList<>();
				listBeforeSave.add(holidaysYearByID);
				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
						userInfo);
				return getHolidayPlanner(userInfo, holidaysYear.getNyearcode());
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
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

	private String validateHolidayPlanner(int nyearcode) throws Exception {
		String str = "";
		String str1 = "select count(nholidayyearversion) from holidayyearversion where nyearcode=" + nyearcode
				+ " and ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		int approvedVersion = jdbcTemplate.queryForObject(str1, Integer.class);
		if (approvedVersion == 0) {
			str = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
		} else {
			str = Enumeration.ReturnStatus.NOTPOSSIBLETODELETEAPPROVE.getreturnstatus();
		}
		return str;
	}

	@Override
	public ResponseEntity<Object> deleteHolidayYear(HolidaysYear holidaysYear, final UserInfo userInfo)
			throws Exception {
		final HolidaysYear holidaysYearByID = (HolidaysYear) getYearByIDForInsert(holidaysYear.getNyearcode(),
				userInfo);
		if (holidaysYearByID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String validate = validateHolidayPlanner(holidaysYear.getNyearcode());

			if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(validate)) {
				final List<HolidaysYear> deletedHolidaysYearList = new ArrayList<>();
				final List<Object> AuditActionList = new ArrayList<>();
				List<HolidayYearVersion> lstYearVersion = new ArrayList<HolidayYearVersion>();
				List<CommonHolidays> lstCommonHolidays = new ArrayList<CommonHolidays>();
				List<PublicHolidays> lstPublicHolidays = new ArrayList<PublicHolidays>();
				String strVersion = "select *  from holidayyearversion where nyearcode = " + holidaysYear.getNyearcode()
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				lstYearVersion = (List<HolidayYearVersion>) jdbcTemplate.query(strVersion, new HolidayYearVersion());
				String strCommon = "select * from commonholidays where nyearcode = " + holidaysYear.getNyearcode()
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				lstCommonHolidays = (List<CommonHolidays>) jdbcTemplate.query(strCommon, new CommonHolidays());
				String strPublic = "select *,TO_CHAR(ddate,'" + userInfo.getSsitedate()
						+ "')as sdate from publicholidays where nyearcode = " + holidaysYear.getNyearcode()
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				lstPublicHolidays = (List<PublicHolidays>) jdbcTemplate.query(strPublic, new PublicHolidays());
				final String updateQueryString = "update holidaysyear set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nyearcode="
						+ holidaysYear.getNyearcode()

						+ ";  update holidayyearversion set nstatus =  "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nyearcode= "
						+ holidaysYear.getNyearcode()

						+ ";  update commonholidays set nstatus =  "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nyearcode= "
						+ holidaysYear.getNyearcode()

						+ ";  update publicholidays set nstatus =  "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nyearcode= "
						+ holidaysYear.getNyearcode();
				jdbcTemplate.execute(updateQueryString);
				holidaysYear.setNstatus(Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				deletedHolidaysYearList.add(holidaysYear);
				AuditActionList.add(deletedHolidaysYearList);
				AuditActionList.add(lstYearVersion);
				AuditActionList.add(lstCommonHolidays);
				AuditActionList.add(lstPublicHolidays);
				auditUtilityFunction.fnInsertListAuditAction(
						AuditActionList, 1, null, Arrays.asList("IDS_DELETEHOLIDAYSYEAR",
								"IDS_DELETEHOLIDAYYEARVERSION", "IDS_DELETECOMMONHOLIDAYS", "IDS_DELETEPUBLICHOLIDAYS"),
						userInfo);
				return getHolidayPlanner(userInfo, 0);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_NOTPOSSIBLETODELETEAPPROVEHOLIDAYPLANNER",
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		}
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
	@Override
	public ResponseEntity<Object> createYearVersion(HolidayYearVersion holidayYearVersion, final UserInfo userInfo)
			throws Exception {
		final String sQuery = " lock  table holidayyearversion " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final String sQuery1 = " lock  table commonholidays " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery1);
		String sequencenoquery = "select * from SeqNoConfigurationMaster where stablename in ('holidayyearversion','commonholidays')";
		final List<?> lstMultiSeqNo = (List<?>) projectDAOSupport.getMultipleEntitiesResultSetInList(sequencenoquery,
				jdbcTemplate, SeqNoConfigurationMaster.class);
		final List<SeqNoConfigurationMaster> lstSeqNoReg = (List<SeqNoConfigurationMaster>) lstMultiSeqNo.get(0);
		Map<String, Object> returnMap = lstSeqNoReg.stream().collect(Collectors.toMap(
				SeqNoConfigurationMaster::getStablename, SeqNoRegistration -> SeqNoRegistration.getNsequenceno()));
		int nsequenceno = (int) returnMap.get("holidayyearversion");
		int nsequencenocommonHol = (int) returnMap.get("commonholidays");
		nsequenceno++;
		nsequencenocommonHol++;
		String insertquery = "Insert into holidayyearversion (nholidayyearversion,nyearcode,nversioncode,ntransactionstatus,dmodifieddate,nstatus)"
				+ "values(" + nsequenceno + "," + holidayYearVersion.getNyearcode() + ","
				+ holidayYearVersion.getNversioncode() + "," + holidayYearVersion.getNtransactionstatus() + ",'"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
		jdbcTemplate.execute(insertquery);
		holidayYearVersion.setNholidayyearversion(nsequenceno);
		final List<String> multilingualIDList = new ArrayList<>();
		multilingualIDList.add("IDS_ADDHOLIDAYSYEAR");
		final List<Object> savedYearList = new ArrayList<>();
		savedYearList.add(holidayYearVersion);
		auditUtilityFunction.fnInsertAuditAction(savedYearList, 1, null, multilingualIDList, userInfo);
		CommonHolidays commonHolidays = new CommonHolidays();
		commonHolidays.setNyearcode(holidayYearVersion.getNyearcode());
		commonHolidays.setNholidayyearversion(holidayYearVersion.getNholidayyearversion());
		commonHolidays.setNcommonholidaycode(nsequencenocommonHol);
		insertquery = "Insert into commonholidays (ncommonholidaycode,nyearcode,nholidayyearversion,nsunday,nmonday,ntuesday,nwednesday,nthursday,nfriday,nsaturday,dmodifieddate,nstatus)"
				+ "values(" + nsequencenocommonHol + "," + holidayYearVersion.getNyearcode() + "," + nsequenceno + ","
				+ commonHolidays.getNsunday() + "," + commonHolidays.getNmonday() + "," + ""
				+ commonHolidays.getNtuesday() + "," + commonHolidays.getNwednesday() + ","
				+ commonHolidays.getNthursday() + "," + commonHolidays.getNfriday() + ","
				+ commonHolidays.getNsaturday() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
		jdbcTemplate.execute(insertquery);
		String updatequery = "update SeqNoConfigurationMaster set nsequenceno =" + nsequenceno
				+ " where stablename ='holidayyearversion';" + "update SeqNoConfigurationMaster set nsequenceno ="
				+ nsequencenocommonHol + " where stablename ='commonholidays';";
		jdbcTemplate.execute(updatequery);
		final List<String> multilingualIDList1 = new ArrayList<>();
		multilingualIDList1.add("IDS_ADDCOMMONHOLIDAYS");
		final List<Object> savedYearList1 = new ArrayList<>();
		savedYearList1.add(commonHolidays);
		auditUtilityFunction.fnInsertAuditAction(savedYearList1, 1, null, multilingualIDList1, userInfo);
		return getHolidayPlanner(userInfo, holidayYearVersion.getNyearcode());

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
	@Override
	public ResponseEntity<Object> deleteYearVersion(HolidayYearVersion holidayYearVersion, final UserInfo userInfo)
			throws Exception {
		final HolidayYearVersion holidaysYearVersionByID = (HolidayYearVersion) getYearVersionByIDForInsert(
				holidayYearVersion.getNholidayyearversion(), holidayYearVersion.getNyearcode(), userInfo);
		if (holidaysYearVersionByID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (holidayYearVersion.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT
					.gettransactionstatus()) {
				final List<HolidayYearVersion> deletedYearVersion = new ArrayList<>();
				final List<Object> AuditActionList = new ArrayList<>();
				List<CommonHolidays> lstCommonHolidays = new ArrayList<CommonHolidays>();
				List<PublicHolidays> lstPublicHolidays = new ArrayList<PublicHolidays>();
				String strCommon = "select * from commonholidays where nyearcode = " + holidayYearVersion.getNyearcode()
						+ " and  nholidayyearversion = " + holidayYearVersion.getNholidayyearversion();
				lstCommonHolidays = (List<CommonHolidays>) jdbcTemplate.query(strCommon, new CommonHolidays());
				String strPublic = "select * from publicholidays where nyearcode = " + holidayYearVersion.getNyearcode()
						+ " and  nholidayyearversion = " + holidayYearVersion.getNholidayyearversion();
				lstPublicHolidays = (List<PublicHolidays>) jdbcTemplate.query(strPublic, new PublicHolidays());
				final String updateQueryString = "update holidayyearversion set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nyearcode="
						+ holidayYearVersion.getNyearcode() + " and nholidayyearversion = "
						+ holidayYearVersion.getNholidayyearversion()

						+ ";  update commonholidays set nstatus =  "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nyearcode="
						+ holidayYearVersion.getNyearcode() + " and nholidayyearversion = "
						+ holidayYearVersion.getNholidayyearversion()

						+ ";  update publicholidays set nstatus =  "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nyearcode="
						+ holidayYearVersion.getNyearcode() + " and nholidayyearversion = "
						+ holidayYearVersion.getNholidayyearversion();

				jdbcTemplate.execute(updateQueryString);
				holidayYearVersion.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				deletedYearVersion.add(holidayYearVersion);
				AuditActionList.add(deletedYearVersion);
				AuditActionList.add(lstCommonHolidays);
				AuditActionList.add(lstPublicHolidays);
				auditUtilityFunction.fnInsertListAuditAction(AuditActionList, 1, null, Arrays
						.asList("IDS_DELETEHOLIDAYYEARVERSION", "IDS_DELETECOMMONHOLIDAYS", "IDS_DELETEPUBLICHOLIDAYS"),
						userInfo);

				return getHolidayPlanner(userInfo, holidayYearVersion.getNyearcode());
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDYEARVERSIONMUSTBEDRAFT",
						userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}

		}

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
	@Override
	public ResponseEntity<Object> ApproveYearVersion(HolidayYearVersion holidayYearVersion, final UserInfo userInfo)
			throws Exception {
		final HolidayYearVersion holidaysYearVersionByID = (HolidayYearVersion) getYearVersionByIDForInsert(
				holidayYearVersion.getNholidayyearversion(), holidayYearVersion.getNyearcode(), userInfo);
		if (holidaysYearVersionByID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		if (holidaysYearVersionByID.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED
				.gettransactionstatus()) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDYEARVERSIONMUSTBEDRAFT",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		} else if (holidaysYearVersionByID.getNtransactionstatus() == Enumeration.TransactionStatus.APPROVED
				.gettransactionstatus()) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_ALREADYAPPROVED", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
		String GetString = "select count(*) from publicholidays where nyearcode = " + holidayYearVersion.getNyearcode()
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nholidayyearversion = " + holidayYearVersion.getNholidayyearversion();
		int checkCount = jdbcTemplate.queryForObject(GetString, Integer.class);
		if (checkCount > 0) {
			final List<Object> listAfterUpdate = new ArrayList<>();
			final List<Object> listBeforeUpdate = new ArrayList<>();
			final HolidayYearVersion holidayYearVersionByID = (HolidayYearVersion) getYearVersionByIDForInsert(
					holidayYearVersion.getNholidayyearversion(), holidayYearVersion.getNyearcode(), userInfo);
			GetString = "select MAX(coalesce(nversioncode,0))  + 1 from holidayyearversion where nyearcode = "
					+ holidayYearVersion.getNyearcode();
			int Count = jdbcTemplate.queryForObject(GetString, Integer.class);
			final List<String> multilingualIDList = new ArrayList<>();
			listBeforeUpdate.add(holidayYearVersionByID);
			String updateQueryString = "update holidayyearversion set ntransactionstatus = "
					+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + "  where nyearcode="
					+ holidayYearVersion.getNyearcode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nholidayyearversion in (select nholidayyearversion from holidayyearversion where ntransactionstatus = "
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and nyearcode = "
					+ holidayYearVersion.getNyearcode() + " and nstatus =  "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  )";
			jdbcTemplate.execute(updateQueryString);

			updateQueryString = "update holidayyearversion set nversioncode= " + Count + ", ntransactionstatus = "
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + "  where nyearcode="
					+ holidayYearVersion.getNyearcode() + " and nholidayyearversion = "
					+ holidayYearVersion.getNholidayyearversion() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			jdbcTemplate.execute(updateQueryString);
			holidayYearVersion.setNversioncode(Count);
			holidayYearVersion.setNtransactionstatus(Enumeration.TransactionStatus.APPROVED.gettransactionstatus());
			listAfterUpdate.add(holidayYearVersion);

			multilingualIDList.add("IDS_APPROVEHOLIDAYYEARVERSION");
			auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
					userInfo);
			return getSelectionAllHolidayPlanner(userInfo, holidayYearVersion.getNyearcode(),
					holidayYearVersion.getNholidayyearversion());
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_MENTIONCOMMONANDPUBLICHOLIDAYS",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}
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
	@Override
	public ResponseEntity<Object> updateCommonHolidays(CommonHolidays commonHolidays, final UserInfo userInfo)
			throws Exception {
		final CommonHolidays commonHolidaysByID = (CommonHolidays) getCommonHolidaysByIDForInsert(
				commonHolidays.getNcommonholidaycode(), commonHolidays.getNholidayyearversion(),
				commonHolidays.getNyearcode(), commonHolidays.getNsitecode());

		if (commonHolidaysByID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String updateQueryString = "update commonholidays set nmonday= " + commonHolidays.getNmonday()
					+ ",  ntuesday = " + commonHolidays.getNtuesday() + ", nwednesday = "
					+ commonHolidays.getNwednesday() + ", nthursday = " + commonHolidays.getNthursday() + ", nfriday = "
					+ commonHolidays.getNfriday() + ", nsaturday = " + commonHolidays.getNsaturday() + ", nsunday = "
					+ commonHolidays.getNsunday() + " where nyearcode=" + commonHolidays.getNyearcode()
					+ " and ncommonholidaycode = " + commonHolidays.getNcommonholidaycode()
					+ " and nholidayyearversion = " + commonHolidays.getNholidayyearversion();

			jdbcTemplate.execute(updateQueryString);
			final List<String> multilingualIDList = new ArrayList<>();
			multilingualIDList.add("IDS_EDITCOMMONHOLIDAYS");
			final List<Object> listAfterSave = new ArrayList<>();
			listAfterSave.add(commonHolidays);
			final List<Object> listBeforeSave = new ArrayList<>();
			listBeforeSave.add(commonHolidaysByID);
			auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList, userInfo);
			return getSelectedCommonAndPublicHolidays(commonHolidays.getNyearcode(),
					commonHolidays.getNholidayyearversion(), userInfo);
		}
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
	@Override
	public ResponseEntity<Object> createPublicHolidays(PublicHolidays publicHolidays, final UserInfo userInfo)
			throws Exception {

		final String sQuery = " lock  table publicholidays " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final String versionValidateQuery = "select ntransactionstatus from holidayyearversion"
				+ " where nholidayyearversion=" + publicHolidays.getNholidayyearversion() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		HolidayYearVersion yearVersion = (HolidayYearVersion) jdbcUtilityFunction.queryForObject(versionValidateQuery,
				HolidayYearVersion.class, jdbcTemplate);
		if (yearVersion == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_VERSIONALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (yearVersion.getNtransactionstatus() == Enumeration.TransactionStatus.APPROVED.gettransactionstatus()) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTVERSION",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else if (yearVersion.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED
					.gettransactionstatus()) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTVERSION",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else {
				final ObjectMapper objMapper = new ObjectMapper();
				objMapper.registerModule(new JavaTimeModule());
				publicHolidays.setDdate(publicHolidays.getDdate().truncatedTo(ChronoUnit.DAYS).plus(0, ChronoUnit.HOURS)
						.plus(0, ChronoUnit.MINUTES).plus(0, ChronoUnit.SECONDS));
				String sourceFormat = "yyyy-MM-dd";
				final LocalDateTime datetime = LocalDateTime
						.ofInstant(publicHolidays.getDdate().truncatedTo(ChronoUnit.DAYS), ZoneOffset.UTC);
				final String formattedDate = DateTimeFormatter.ofPattern(sourceFormat).format(datetime);
				publicHolidays
						.setSdate(dateUtilityFunction.instantDateToString(publicHolidays.getDdate()).replace("T", " "));// ,
				final List<PublicHolidays> publicHolidayListByName = getPublicHolidaysListByName(formattedDate,
						publicHolidays.getNholidayyearversion(), publicHolidays.getNyearcode(),
						publicHolidays.getNsitecode());
				if (publicHolidayListByName.isEmpty()) {
					String sequencenoquery = "select nsequenceno from SeqNoConfigurationMaster where stablename ='publicholidays'";
					int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
					nsequenceno++;
					String insertquery = "Insert into publicholidays (npublicholidaycode,nyearcode,nholidayyearversion,ddate,ntzddate,sdescription,dmodifieddate,nsitecode,nstatus)"
							+ "values(" + nsequenceno + "," + publicHolidays.getNyearcode() + ","
							+ publicHolidays.getNholidayyearversion() + ",'" + publicHolidays.getDdate() + "',"
							+ publicHolidays.getNtzddate() + ",N'"
							+ stringUtilityFunction.replaceQuote(publicHolidays.getSdescription()) + "','"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode()
							+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
					jdbcTemplate.execute(insertquery);

					String updatequery = "update SeqNoConfigurationMaster set nsequenceno =" + nsequenceno
							+ " where stablename ='publicholidays'";
					jdbcTemplate.execute(updatequery);
					publicHolidays.setNpublicholidaycode(nsequenceno);
					final List<String> multilingualIDList = new ArrayList<>();
					multilingualIDList.add("IDS_ADDPUBLICHOLIDAYS");
					final List<Object> savedYearList = new ArrayList<>();
					auditUtilityFunction.fnInsertAuditAction(savedYearList, 1, null, multilingualIDList, userInfo);
					return getSelectedCommonAndPublicHolidays(publicHolidays.getNyearcode(),
							publicHolidays.getNholidayyearversion(), userInfo);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.CONFLICT);
				}
			}
		}

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
	@Override
	public ResponseEntity<Object> updatePublicHolidays(PublicHolidays publicHolidays, final UserInfo userInfo)
			throws Exception {
		final PublicHolidays publicHolidaysByID = (PublicHolidays) getPublicHolidaysByIDForInsert(
				publicHolidays.getNpublicholidaycode(), publicHolidays.getNholidayyearversion(),
				publicHolidays.getNyearcode(), userInfo);
		if (publicHolidaysByID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			String sourceFormat = "yyyy-MM-dd";
			final LocalDateTime datetime = LocalDateTime
					.ofInstant(publicHolidays.getDdate().truncatedTo(ChronoUnit.DAYS), ZoneOffset.UTC);
			final String formattedDate = DateTimeFormatter.ofPattern(sourceFormat).format(datetime);
			publicHolidays.setDdate(publicHolidays.getDdate().truncatedTo(ChronoUnit.DAYS).plus(0, ChronoUnit.HOURS)
					.plus(0, ChronoUnit.MINUTES).plus(0, ChronoUnit.SECONDS));
			final ObjectMapper objMapper = new ObjectMapper();
			objMapper.registerModule(new JavaTimeModule());
			publicHolidays
					.setSdate(dateUtilityFunction.instantDateToString(publicHolidays.getDdate()).replace("T", " "));

			final String queryString = "select ddate from publicholidays where cast(ddate as character varying) = '"
					+ formattedDate + "' and npublicholidaycode <> " + publicHolidays.getNpublicholidaycode()
					+ " and nholidayyearversion = " + publicHolidays.getNholidayyearversion() + " and nyearcode = "
					+ publicHolidays.getNyearcode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final List<PublicHolidays> publicHolidaysList = (List<PublicHolidays>) jdbcTemplate.query(queryString,
					new PublicHolidays());
			if (publicHolidaysList.isEmpty()) {
				final List<String> multilingualIDList = new ArrayList<>();
				multilingualIDList.add("IDS_EDITPUBLICHOLIDAYS");
				final List<Object> listAfterSave = new ArrayList<>();
				final List<Object> listBeforeSave = new ArrayList<>();
				listBeforeSave.add(publicHolidaysByID);
				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
						userInfo);
				return getSelectedCommonAndPublicHolidays(publicHolidays.getNyearcode(),
						publicHolidays.getNholidayyearversion(), userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
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
	@Override
	public ResponseEntity<Object> deletePublicHolidays(PublicHolidays publicHolidays, final UserInfo userInfo)
			throws Exception {
		final PublicHolidays publicHolidaysByID = (PublicHolidays) getPublicHolidaysByIDForInsert(
				publicHolidays.getNpublicholidaycode(), publicHolidays.getNholidayyearversion(),
				publicHolidays.getNyearcode(), userInfo);
		if (publicHolidaysByID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> savedHolidaysYearList = new ArrayList<>();
			final String updateQueryString = "update publicholidays set nstatus = "
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ",dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nyearcode= "
					+ publicHolidays.getNyearcode() + " and npublicholidaycode = "
					+ publicHolidays.getNpublicholidaycode() + " and nholidayyearversion = "
					+ publicHolidays.getNholidayyearversion();

			jdbcTemplate.execute(updateQueryString);
			publicHolidays.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
			savedHolidaysYearList.add(publicHolidays);
			multilingualIDList.add("IDS_DELETEPUBLICHOLIDAYS");
			auditUtilityFunction.fnInsertAuditAction(savedHolidaysYearList, 1, null, multilingualIDList, userInfo);
			return getSelectedCommonAndPublicHolidays(publicHolidays.getNyearcode(),
					publicHolidays.getNholidayyearversion(), userInfo);
		}

	}

	@Override
	public ResponseEntity<Object> createUserBasedHolidays(UserBasedHolidays userBasedHolidays, UserInfo userInfo)
			throws Exception {

		final String sQuery = " lock  table userbasedholidays " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final String versionValidateQuery = "select ntransactionstatus from holidayyearversion"
				+ " where nholidayyearversion=" + userBasedHolidays.getNholidayyearversion() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		HolidayYearVersion yearVersion = (HolidayYearVersion) jdbcUtilityFunction.queryForObject(versionValidateQuery,
				HolidayYearVersion.class, jdbcTemplate);
		if (yearVersion == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_VERSIONALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			if (yearVersion.getNtransactionstatus() == Enumeration.TransactionStatus.APPROVED.gettransactionstatus()) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTVERSION",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else if (yearVersion.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED
					.gettransactionstatus()) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTVERSION",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else {

				final ObjectMapper objMapper = new ObjectMapper();
				objMapper.registerModule(new JavaTimeModule());
				userBasedHolidays.setDdate(userBasedHolidays.getDdate().truncatedTo(ChronoUnit.DAYS)
						.plus(0, ChronoUnit.HOURS).plus(0, ChronoUnit.MINUTES).plus(0, ChronoUnit.SECONDS));
				String sourceFormat = "yyyy-MM-dd";
				final LocalDateTime datetime = LocalDateTime
						.ofInstant(userBasedHolidays.getDdate().truncatedTo(ChronoUnit.DAYS), ZoneOffset.UTC);
				final String formattedDate = DateTimeFormatter.ofPattern(sourceFormat).format(datetime);
				userBasedHolidays.setSdate(
						dateUtilityFunction.instantDateToString(userBasedHolidays.getDdate()).replace("T", " "));
				final List<UserBasedHolidays> UserBasedHolidayListByName = getUserBasedHolidaysListByName(formattedDate,
						userBasedHolidays.getNholidayyearversion(), userBasedHolidays.getNyearcode(),
						userBasedHolidays.getNusercode(), userInfo.getNmastersitecode());
				if (UserBasedHolidayListByName.isEmpty()) {
					String sequencenoquery = "select nsequenceno from SeqNoConfigurationMaster where stablename ='userbasedholidays'";
					int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
					nsequenceno++;
					String insertquery = "Insert into userbasedholidays (nuserbasedholidaycode,nyearcode,nholidayyearversion,nusercode,ddate,ntzddate,sdescription,dmodifieddate,nsitecode,nstatus)"
							+ "values(" + nsequenceno + "," + userBasedHolidays.getNyearcode() + ","
							+ userBasedHolidays.getNholidayyearversion() + "," + userBasedHolidays.getNusercode() + ",'"
							+ userBasedHolidays.getDdate() + "'," + userBasedHolidays.getNtzddate() + ",N'"
							+ stringUtilityFunction.replaceQuote(userBasedHolidays.getSdescription()) + "','"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode()
							+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
					jdbcTemplate.execute(insertquery);
					String updatequery = "update SeqNoConfigurationMaster set nsequenceno =" + nsequenceno
							+ " where stablename ='userbasedholidays'";
					jdbcTemplate.execute(updatequery);
					userBasedHolidays.setNuserbasedholidaycode(nsequenceno);
					final List<String> multilingualIDList = new ArrayList<>();
					multilingualIDList.add("IDS_ADDUSERBASEDHOILDAYS");
					final List<Object> savedYearList = new ArrayList<>();
					auditUtilityFunction.fnInsertAuditAction(savedYearList, 1, null, multilingualIDList, userInfo);

					return getSelectedCommonAndPublicHolidays(userBasedHolidays.getNyearcode(),
							userBasedHolidays.getNholidayyearversion(), userInfo);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.CONFLICT);
				}
			}
		}
	}

	@Override
	public ResponseEntity<Object> deleteUserBasedHolidays(UserBasedHolidays userBasedHolidays, UserInfo userInfo)
			throws Exception {
		final UserBasedHolidays userBasedHolidaysByID = (UserBasedHolidays) getUserBasedHolidaysByIDForInsert(
				userBasedHolidays.getNuserbasedholidaycode(), userBasedHolidays.getNholidayyearversion(),
				userBasedHolidays.getNyearcode(), userInfo);
		if (userBasedHolidaysByID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> savedHolidaysYearList = new ArrayList<>();
			final String updateQueryString = "update userbasedholidays set nstatus = "
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ",dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nyearcode= "
					+ userBasedHolidays.getNyearcode() + " and nuserbasedholidaycode = "
					+ userBasedHolidays.getNuserbasedholidaycode() + " and nholidayyearversion = "
					+ userBasedHolidays.getNholidayyearversion();
			jdbcTemplate.execute(updateQueryString);
			userBasedHolidays.setNstatus(Enumeration.TransactionStatus.DELETED.gettransactionstatus());
			savedHolidaysYearList.add(userBasedHolidays);
			multilingualIDList.add("IDS_DELETEUSERBASEDHOLIDAYS");
			auditUtilityFunction.fnInsertAuditAction(savedHolidaysYearList, 1, null, multilingualIDList, userInfo);
			return getSelectedCommonAndPublicHolidays(userBasedHolidays.getNyearcode(),
					userBasedHolidays.getNholidayyearversion(), userInfo);
		}
	}

	@Override
	public ResponseEntity<Object> updateUserBasedHolidays(UserBasedHolidays userBasedHoildays, UserInfo userInfo)
			throws Exception {
		final UserBasedHolidays userBasedHolidaysByID = (UserBasedHolidays) getUserBasedHolidaysByIDForInsert(
				userBasedHoildays.getNuserbasedholidaycode(), userBasedHoildays.getNholidayyearversion(),
				userBasedHoildays.getNyearcode(), userInfo);
		if (userBasedHolidaysByID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			String sourceFormat = "yyyy-MM-dd";
			final LocalDateTime datetime = LocalDateTime
					.ofInstant(userBasedHoildays.getDdate().truncatedTo(ChronoUnit.DAYS), ZoneOffset.UTC);
			final String formattedDate = DateTimeFormatter.ofPattern(sourceFormat).format(datetime);
			userBasedHoildays.setDdate(userBasedHoildays.getDdate().truncatedTo(ChronoUnit.DAYS)
					.plus(0, ChronoUnit.HOURS).plus(0, ChronoUnit.MINUTES).plus(0, ChronoUnit.SECONDS));
			final ObjectMapper objMapper = new ObjectMapper();
			objMapper.registerModule(new JavaTimeModule());
			userBasedHoildays
					.setSdate(dateUtilityFunction.instantDateToString(userBasedHoildays.getDdate()).replace("T", " "));
			final String queryString = "select ddate from userbasedholidays where cast(ddate as character varying) = '"
					+ formattedDate + "' and nusercode=" + userBasedHoildays.getNusercode()
					+ " and nuserbasedholidaycode <> " + userBasedHoildays.getNuserbasedholidaycode()
					+ " and nholidayyearversion = " + userBasedHoildays.getNholidayyearversion() + " and nyearcode = "
					+ userBasedHoildays.getNyearcode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final List<UserBasedHolidays> publicHolidaysList = (List<UserBasedHolidays>) jdbcTemplate.query(queryString,
					new UserBasedHolidays());
			if (publicHolidaysList.isEmpty()) {
				final List<String> multilingualIDList = new ArrayList<>();
				multilingualIDList.add("IDS_EDITUSERBASEDHOLIDAYS");
				final List<Object> listAfterSave = new ArrayList<>();
				final List<Object> listBeforeSave = new ArrayList<>();
				listBeforeSave.add(userBasedHolidaysByID);
				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
						userInfo);
				return getSelectedCommonAndPublicHolidays(userBasedHoildays.getNyearcode(),
						userBasedHoildays.getNholidayyearversion(), userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	@Override
	public ResponseEntity<Object> getUserBasedHolidaysByID(int nuserbasedholidaycode, int nholidayYearVersion,
			int nyearcode, UserInfo userInfo) throws Exception {

		final HolidayYearVersion holidaysYearVersionByID = (HolidayYearVersion) getYearVersionByIDForInsert(
				nholidayYearVersion, nyearcode, userInfo);
		if (holidaysYearVersionByID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (holidaysYearVersionByID.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT
					.gettransactionstatus()) {
				final String strQuery = " select a.*, COALESCE(TO_CHAR(a.ddate,'" + userInfo.getSsitedate()
						+ "'),'') as sdate, tz.stimezoneid as stzddate,us.sfirstname||' '||us.slastname as susername from userbasedholidays a , timezone tz,users us where "
						+ " us.nusercode=a.nusercode and a.ntzddate = tz.ntimezonecode and   a.nyearcode = " + nyearcode
						+ " and  a.nuserbasedholidaycode = " + nuserbasedholidaycode + "  and a.nholidayyearversion = "
						+ nholidayYearVersion + " and a.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tz.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final UserBasedHolidays objUserBasedHolidays = (UserBasedHolidays) jdbcUtilityFunction
						.queryForObject(strQuery, UserBasedHolidays.class, jdbcTemplate);

				return new ResponseEntity<Object>(objUserBasedHolidays, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDYEARVERSIONMUSTBEDRAFT",
						userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}
		}
	}

	@Override
	public ResponseEntity<Object> getUsers(UserInfo userInfo) throws Exception {
		final String strQuery = "select a.*,a.sfirstname||' '||a.slastname susername from users a where a.nsitecode = "
				+ userInfo.getNmastersitecode() + " and a.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and a.nusercode > 0;";
		final List<Users> objUser = (List<Users>) jdbcTemplate.query(strQuery, new Users());
		return new ResponseEntity<>(objUser, HttpStatus.OK);
	}
}
