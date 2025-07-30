package com.agaramtech.qualis.basemaster.service.calendarproperties;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import lombok.AllArgsConstructor;
import com.agaramtech.qualis.basemaster.model.CalendarProperties;

@AllArgsConstructor
@Repository
public class CalenderPropertiesDAOImpl implements CalenderPropertiesDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(CalenderPropertiesDAOImpl.class);

	private final CommonFunction commonFunction;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final JdbcTemplate jdbcTemplate;

	/**
	 * This method is used to retrieve list of all available CalenderPropertiess for
	 * the specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         CalenderPropertiess
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getCalenderProperties(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		String query = "select * from (select c.*,"
				+ " case when c.ncalendersettingcode = 48 or c.ncalendersettingcode = 47 "
				+ " then case when scalendersettingvalue='0' then '"
				+ commonFunction.getMultilingualMessage("IDS_SUNDAY", userInfo.getSlanguagefilename()) + "'   "
				+ " else case when   scalendersettingvalue='1' then '"
				+ commonFunction.getMultilingualMessage("IDS_MONDAY", userInfo.getSlanguagefilename()) + "'"
				+ " else  case when   scalendersettingvalue='2' then '"
				+ commonFunction.getMultilingualMessage("IDS_TUESDAY", userInfo.getSlanguagefilename()) + "' else  "
				+ "  case when  scalendersettingvalue='3' then '"
				+ commonFunction.getMultilingualMessage("IDS_WEDNESDAY", userInfo.getSlanguagefilename()) + "' else "
				+ "  case when   scalendersettingvalue='4' then '"
				+ commonFunction.getMultilingualMessage("IDS_THURSDAY", userInfo.getSlanguagefilename()) + "'  "
				+ "   else    case when   scalendersettingvalue='5' then '"
				+ commonFunction.getMultilingualMessage("IDS_FRIDAY", userInfo.getSlanguagefilename()) + "' "
				+ "    else '" + commonFunction.getMultilingualMessage("IDS_SATURDAY", userInfo.getSlanguagefilename())
				+ "' end  end  end end end  end " + "  else c.scalendersettingvalue end  as stransdisplaystatus "
				+ " from calendersettings c where  nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and ncalendersettingcode in ("
				+ " '6','5','4','3','2','1','11','10','9','8','18','22','41','40','39','47','48','64','63','62','61','60')  "
				+ " union all select c.*,t.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "' stransdisplaystatus from calendersettings c ,"
				+ " transactionstatus t where  cast(c.scalendersettingvalue as smallint)=t.ntranscode and c.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and c.ncalendersettingcode in ('68','67','66','65','59','58','57','56','55','54','53','52','51',"
				+ "'50','49','46','45','44','43',"
				+ " '42','38','37','36','35','34','33','32','31','30','29','28','27',"
				+ "'26','25','24','23','21','20','19','17','16','15','14','13','12','7') ) a order by 1 asc";
		LOGGER.info("getCalenderProperties-->" + query);
		return new ResponseEntity<Object>(jdbcTemplate.query(query, new CalendarProperties()), HttpStatus.OK);
	}

	/**
	 * This method is used to update entry in CalenderProperties table. Need to
	 * validate that the CalenderProperties object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of
	 * CalenderProperties name for the specified site before saving into database.
	 * Need to check that there should be only one default CalenderProperties for a
	 * site
	 * 
	 * @param objCalenderProperties [CalenderProperties] object holding details to
	 *                              be updated in CalenderProperties table
	 * @param userInfo              [UserInfo] holding logged in user details based
	 *                              on which the list is to be fetched
	 * @return saved CalenderProperties object with status code 200 if saved
	 *         successfully else if the CalenderProperties already exists, response
	 *         will be returned as 'Already Exists' with status code 409 else if the
	 *         CalenderProperties to be updated is not available, response will be
	 *         returned as 'Already Deleted' with status code 417
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> updateCalenderProperties(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> map = (Map<String, Object>) inputMap.get("calenderproperties");
		Integer ncalendersettingcode = (Integer) map.get("ncalendersettingcode");
		String scalendersettingvalue = (String) map.get("scalendersettingvalue");
		String query = "select * from calendersettings where ncalendersettingcode=" + ncalendersettingcode
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		final CalendarProperties objMap = (CalendarProperties) jdbcUtilityFunction.queryForObject(query,
				CalendarProperties.class, jdbcTemplate);
		if (objMap != null) {
			query = " update calendersettings set " + " scalendersettingvalue='" + scalendersettingvalue
					+ "' where ncalendersettingcode=" + ncalendersettingcode;
			jdbcTemplate.execute(query);
			return getCalenderProperties(inputMap, userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);

		}
	}
}
