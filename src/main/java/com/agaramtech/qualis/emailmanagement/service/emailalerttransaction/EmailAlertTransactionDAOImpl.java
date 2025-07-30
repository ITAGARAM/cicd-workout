package com.agaramtech.qualis.emailmanagement.service.emailalerttransaction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.emailmanagement.model.EmailAlertTransHistory;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.UserInfo;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class EmailAlertTransactionDAOImpl implements EmailAlertTransactionDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmailAlertTransactionDAOImpl.class);

	private final JdbcTemplate jdbcTemplate;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;

	@Override
	public ResponseEntity<Object> getEmailAlertTransaction(String fromDate, String toDate, String currentUIDate,
			UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		LOGGER.info("getEmailAlertTransaction() called");
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
		final String query = "select  elt.nrunningno, eh.sprofilename as sdisplayname,elt.ssystemid, elt.ssubject ,es.sscreenname ,elt.stemplatebody, "
				+ " CONCAT(u.sfirstname,' ',u.slastname) as susername, " + "coalesce (cm.jsondata->'scontrolids'->>'"
				+ userInfo.getSlanguagetypecode() + "',cm.jsondata->'scontrolids'->>'en-US')  as scontrolname,"
				+ "coalesce (ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "',ts.jsondata->'stransdisplaystatus'->>'en-US')  as ssentstatus," + " TO_CHAR(elt.dcreateddate,'"
				+ userInfo.getSpgsitedatetime() + "') as screateddate," + " TO_CHAR(eath.dtransdate,'"
				+ userInfo.getSpgsitedatetime() + "') as stransdate,COALESCE(sreason,'') as sreason"
				+ " from emailalerttransaction elt ,emailconfig etc , "
				+ " emailtemplate et ,emailhost eh,emailscreen es,emailalerttranshistory eath,transactionstatus ts, users u, controlmaster cm "
				+ " where elt.nemailconfigcode = etc.nemailconfigcode  "
				+ " and etc.nemailtemplatecode = et.nemailtemplatecode " + " and etc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and et.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and et.nsitecode="
				+ userInfo.getNmastersitecode() + " and eh.nemailhostcode = etc.nemailhostcode " + " and eh.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and eh.nsitecode="
				+ userInfo.getNmastersitecode() + " " + " and es.nemailscreencode = etc.nemailscreencode "
				+ " and es.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and es.nsitecode=" + userInfo.getNmastersitecode() + " " + " and u.nusercode= elt.nusercode"
				+ " and cm.ncontrolcode= elt.ncontrolcode" + " and cm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nsitecode="
				+ userInfo.getNmastersitecode() + " " + " and eath.ntransstatus=ts.ntranscode"
				+ " and elt.nsitecode = eath.nsitecode" + " and elt.nsitecode = " + userInfo.getNsitecode()
				+ " and elt.nrunningno = eath.nrunningno " + " and eath.nemailhistorycode = any ("
				+ " 	select max(nemailhistorycode) from emailalerttranshistory" + " where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode() + "group by nrunningno" + " )" + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and eath.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  elt.dcreateddate  "
				+ " between   '" + fromDate + "'  " + " and  '" + toDate + "' order by elt.nrunningno";

		List<EmailAlertTransHistory> lstEmailAlert = (List<EmailAlertTransHistory>) jdbcTemplate.query(query,
				new EmailAlertTransHistory());
		outputMap.put("EmailAlertTransaction", lstEmailAlert);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);

	}

}
