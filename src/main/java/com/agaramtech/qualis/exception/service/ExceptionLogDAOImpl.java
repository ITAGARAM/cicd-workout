package com.agaramtech.qualis.exception.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.configuration.model.Settings;
import com.agaramtech.qualis.exception.model.JsonExceptionLogs;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "jsonexceptionlogs" table by
 * implementing methods from its interface.
 */
@SuppressWarnings("unused")
@AllArgsConstructor
@Repository
public class ExceptionLogDAOImpl implements ExceptionLogDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionLogDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final CommonFunction commonFunction;
	private final ProjectDAOSupport projectDAOSupport;

	@Override
	public String createExceptionHandleRecord(final JsonExceptionLogs jsonexceptionlogs, final UserInfo userInfo)
			throws Exception {
		StringBuilder sb = new StringBuilder();

		Map<String, Object> objMap = new HashMap<String, Object>();

		final JsonExceptionLogs objStackTrace = new JsonExceptionLogs();

		String instantDateString = Instant.now().truncatedTo(ChronoUnit.SECONDS).toString().replace("T", " ")
				.replace("Z", " ");

		int nsiteCode = -1;

		if (userInfo != null) {
			objStackTrace.setSusername(userInfo.getSusername());
			objStackTrace.setSuserrolename(userInfo.getSuserrolename());
			objStackTrace.setSformname(userInfo.getSformname());
			objStackTrace.setSmodulename(userInfo.getSmodulename());
			objStackTrace.setSsitename(userInfo.getSsitename());

			instantDateString = dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ")
					.replace("Z", " ");
			nsiteCode = userInfo.getNsitecode();
		}

		objStackTrace.setSstacktrace(jsonexceptionlogs.getSstacktrace());
		objStackTrace.setSmessage(jsonexceptionlogs.getSmessage());

		objMap.put("stacktrace", objStackTrace.getSstacktrace());
		objMap.put("message", objStackTrace.getSmessage());
		objMap.put("username", objStackTrace.getSusername());
		objMap.put("userrolename", objStackTrace.getSuserrolename());
		objMap.put("formname", objStackTrace.getSformname());
		objMap.put("modulename", objStackTrace.getSmodulename());
		objMap.put("sitename", objStackTrace.getSsitename());
		objMap.put("exceptiondate", instantDateString);

		JSONObject jsonStackTrace = new JSONObject(objMap);

		jdbcTemplate.execute("insert into jsonexceptionlogs(jsondata, dmodifieddate, nsitecode, nstatus )" + " values('"
				+ stringUtilityFunction.replaceQuote(jsonStackTrace.toString()) + "'::jsonb, '" + instantDateString
				+ "', " + nsiteCode + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")");

		return "Success";
	}

	// Three months once delete exception logs by using call scheduler daily
	// --ALPD-4159 ,work done by Dhanushya R I
	public void deleteExceptionLogs() throws Exception {

		final String sQuery = "select ssettingvalue from settings where nsettingcode="
				+ Enumeration.Settings.DELETE_EXCEPTION_LOG.getNsettingcode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final Settings objSettings = (Settings) jdbcUtilityFunction.queryForObject(sQuery, Settings.class,
				jdbcTemplate);

		final String updateExcepQuery = " delete from jsonexceptionlogs where dmodifieddate <= (NOW() - INTERVAL '"
				+ objSettings.getSsettingvalue() + " days') ";
		jdbcTemplate.execute(updateExcepQuery);

	}

	@Override
	public ResponseEntity<Object> getJsonExceptionLogs(String fromDate, String toDate, String currentUIDate,
			UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		if (currentUIDate != null && currentUIDate.trim().length() != 0) {
			final Map<String, Object> mapObject = projectDAOSupport.getDateFromControlProperties(userInfo,
					currentUIDate, "datetime", "FromDate");
			fromDate = (String) mapObject.get("FromDate");
			toDate = (String) mapObject.get("ToDate");
			outputMap.put("FromDate", (String) mapObject.get("FromDateWOUTC"));
			outputMap.put("ToDate", (String) mapObject.get("ToDateWOUTC"));
		} else {

			final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
			final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());
			final String fromDateUI = LocalDateTime.parse(fromDate, dbPattern).format(uiPattern);
			final String toDateUI = LocalDateTime.parse(toDate, dbPattern).format(uiPattern);

			outputMap.put("FromDate", fromDateUI);
			outputMap.put("ToDate", toDateUI);
			fromDate = dateUtilityFunction
					.instantDateToString(dateUtilityFunction.convertStringDateToUTC(fromDate, userInfo, true));
			toDate = dateUtilityFunction
					.instantDateToString(dateUtilityFunction.convertStringDateToUTC(toDate, userInfo, true));
		}

		final String strQuery = "select je.njsonexceptioncode, je.jsondata, je.nsitecode, je.nstatus, coalesce(je.jsondata->>'stacktrace') as sstacktrace,"
				+ "coalesce(je.jsondata->>'message','') as smessage, "
				+ "coalesce(je.jsondata->>'formname','') as sformname,"
				+ "coalesce(je.jsondata->>'modulename','') as smodulename,"
				+ "coalesce(je.jsondata->>'username','') as susername,"
				+ "coalesce(je.jsondata->>'userrolename','') as suserrolename,"
				+ "coalesce(je.jsondata->>'sitename','') as ssitename,"
				+ "coalesce(je.jsondata->>'exceptiondate','') as sexceptiondate from" + " jsonexceptionlogs je where "
				+ " je.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and je.njsonexceptioncode > 0 and nsitecode in("
				+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ", " + userInfo.getNsitecode()
				+ " ) and coalesce(je.jsondata->>'exceptiondate','') between '" + fromDate + "' and '" + toDate + "'"
				+ " order by je.njsonexceptioncode";

		final List<JsonExceptionLogs> lstJsonExceptionLogs = (List<JsonExceptionLogs>) jdbcTemplate.query(strQuery,
				new JsonExceptionLogs());

		outputMap.put("JsonExceptionLogs", lstJsonExceptionLogs);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> getJsonExceptionLogsDetails(Integer njsonexceptionCode, UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		final String strQuery = "select je.njsonexceptioncode, coalesce(je.jsondata->>'stacktrace') as sstacktrace,"
				+ "coalesce(je.jsondata->>'message','') as smessage, "
				+ "coalesce(je.jsondata->>'formname','') as sformname,"
				+ "coalesce(je.jsondata->>'modulename','') as smodulename,"
				+ "coalesce(je.jsondata->>'username','') as susername,"
				+ "coalesce(je.jsondata->>'userrolename','') as suserrolename,"
				+ "coalesce(je.jsondata->>'sitename','') as ssitename,"
				+ "coalesce(je.jsondata->>'exceptiondate','') as sexceptiondate, je.nsitecode, je.nstatus from"
				+ " jsonexceptionlogs je where " + " je.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and je.njsonexceptioncode ="
				+ njsonexceptionCode;

		final List<JsonExceptionLogs> lstJsonExceptionLogsDetails = (List<JsonExceptionLogs>) jdbcTemplate
				.query(strQuery, new JsonExceptionLogs());
		outputMap.put("JsonExceptionLogsDetails", lstJsonExceptionLogsDetails);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);

	}

//	public ResponseEntity<Object> createException (Exception e,UserInfo userinfo){	
//
//		Map<String,Object> map = new HashMap<String,Object>();		
//		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//		LocalDateTime now = LocalDateTime.now(); 
//		final String date = dtf.format(now); 
//
//		String stackTrace = exceptionLogController.getStackTrace(e);
//
//		map.put("stacktrace", stackTrace);
//		map.put("message", e.toString());
//		map.put("sitename", userinfo.getSsitename());
//		map.put("username", userinfo.getSusername());
//		map.put("userrolename", userinfo.getSuserrolename());
//		map.put("formname", userinfo.getSformname());
//		map.put("modulename", "Configuration");
//		map.put("exceptiondate", date);
//
//		JSONObject jsonStackTrace = new JSONObject(map);
//
//		jdbcTemplate.execute(
//				"Insert into jsonexceptionlogs(jsondata, dmodifieddate, nsitecode, nstatus )"
//						+ " values('"+stringUtilityFunction.replaceQuote(jsonStackTrace.toString()) +"'::jsonb, '"+date +"',"
//						+ " -1, 1 )"	);
//
//		return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
//	}

}
