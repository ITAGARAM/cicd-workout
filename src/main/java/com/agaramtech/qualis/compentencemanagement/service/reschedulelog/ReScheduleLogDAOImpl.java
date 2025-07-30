package com.agaramtech.qualis.compentencemanagement.service.reschedulelog;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.compentencemanagement.model.ReScheduleLog;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class ReScheduleLogDAOImpl implements ReScheduleLogDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReScheduleLogDAOImpl.class);

	private final JdbcTemplate jdbcTemplate;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;

	@Override
	public ResponseEntity<Object> getReScheduleLog(String fromDate, String toDate, String currentUIDate,
			UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final ObjectMapper objMapper = new ObjectMapper();
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

		final String rescheduleget = "select tr.noffsetdrescheduledate,tr.noffsetdscheduledate,tr.noffsetdcreateddate,tc.strainername,tr.scomments,"
				+ " TO_CHAR(tr.drescheduledate,'" + userInfo.getSpgsitedatetime() + "') as srescheduledate,"
				+ " TO_CHAR(tr.dscheduledate,'" + userInfo.getSpgsitedatetime() + "') as sscheduledate,"
				+ " TO_CHAR(tr.dcreateddate,'" + userInfo.getSpgsitedatetime() + "') as screateddate"
				+ " from trainingcertification tc,trainingreschedule tr,timezone tz1,timezone tz2 where tr.ntrainingcode=tc.ntrainingcode and"
				+ " tz1.ntimezonecode=tr.ntzrescheduledate and tz2.ntimezonecode=tr.ntzscheduledate and "
				+ " tc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tc.nsitecode="
				+ userInfo.getNmastersitecode() + " and " + " tr.drescheduledate " + " between '" + fromDate + "' and "
				+ " '" + toDate + "' ";

		LOGGER.info("getReScheduleLog called");
		final List<ReScheduleLog> lstInstGet = objMapper.convertValue(
				dateUtilityFunction.getSiteLocalTimeFromUTC(jdbcTemplate.query(rescheduleget, new ReScheduleLog()),
						Arrays.asList("srescheduledate", "sscheduledate", "screateddate"), null, userInfo, true,
						Arrays.asList("strainername"), false),
				new TypeReference<List<ReScheduleLog>>() {
				});
		outputMap.put("TrainingRescheduleLog", lstInstGet);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}
}
