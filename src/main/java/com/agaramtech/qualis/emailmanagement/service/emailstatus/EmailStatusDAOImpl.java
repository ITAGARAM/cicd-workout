package com.agaramtech.qualis.emailmanagement.service.emailstatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.emailmanagement.model.EmailAlertTransHistory;
import com.agaramtech.qualis.emailmanagement.model.EmailAlertTransaction;
import com.agaramtech.qualis.emailmanagement.model.SeqNoEmailManagement;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class EmailStatusDAOImpl implements EmailStatusDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmailStatusDAOImpl.class);

	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all active emailhost for the
	 * specified site.
	 * 
	 * @param nmasterSiteCode [int] primary key of site object for which the list is
	 *                        to be fetched
	 * @return response entity object holding response status and list of all active
	 *         emailhost
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> getEmailStatus(final UserInfo userInfo) throws Exception {

		final String query = "select eath.*,eat.nrunningno, et.ssubject as ssubject,"
				+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "', ts.jsondata->'stransdisplaystatus'->>'en-US') as ssentstatus,"
				+ "eat.semail as srecipientusers,eath.ntransstatus," + "	 COALESCE(TO_CHAR(eath.dtransdate,'"
				+ userInfo.getSpgsitedatetime() + "'),'') as stransdate"
				+ " from emailalerttranshistory eath,emailalerttransaction eat,emailconfig etc, emailtemplate et, transactionstatus ts,timezone tz "
				+ " where eath.nemailhistorycode in "
				+ " (select max(nemailhistorycode) from emailalerttranshistory eath,emailalerttransaction eat "
				+ " where eat.nrunningno = eath.nrunningno group by eat.nrunningno)"
				+ " and eat.nrunningno = eath.nrunningno and eat.nemailconfigcode = etc.nemailconfigcode"
				+ " and etc.nemailtemplatecode = et.nemailtemplatecode   "
				+ " and eath.ntransdatetimezonecode = tz.ntimezonecode  " + "  and ts.ntranscode = eath.ntransstatus"
				+ " and eath.nsitecode = eat.nsitecode" + " and eath.nsitecode= " + userInfo.getNsitecode()
				+ "  and eat.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and eath.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and etc.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and etc.nsitecode=" + userInfo.getNmastersitecode() + "" + " and et.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and et.nsitecode="
				+ userInfo.getNmastersitecode() + "" + " order by nemailhistorycode desc";

		LOGGER.info("getEmailStatus() called");
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final List<EmailAlertTransHistory> lstEmailAlertHistGet = objMapper
				.convertValue(
						dateUtilityFunction.getSiteLocalTimeFromUTC(
								jdbcTemplate.query(query, new EmailAlertTransHistory()), Arrays.asList("stransdate"),
								Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null, false),
						new TypeReference<List<EmailAlertTransHistory>>() {
						});
		return new ResponseEntity<>(commonFunction.getMultilingualMessageList(lstEmailAlertHistGet,
				Arrays.asList("ssentstatus"), userInfo.getSlanguagefilename()), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> reSentMail(Integer nrunningno, Integer ncontrolcode, UserInfo userInfo)
			throws Exception {
		Map<String, Object> inputMap = new HashMap<>();
		final String sQuery = " lock  table lockmail" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final String sSeqnoQuery = " select stablename,nsequenceno from  seqnoemailmanagement "
				+ " where stablename in('emailalerttransaction','emailalerttranshistory')";
		List<SeqNoEmailManagement> lstSeqNo = jdbcTemplate.query(sSeqnoQuery, new SeqNoEmailManagement());
		inputMap = lstSeqNo.stream().collect(Collectors.toMap(SeqNoEmailManagement::getStablename,
				SeqNoEmailManagement -> SeqNoEmailManagement.getNsequenceno()));
		int nseqnoalerttrans = (int) inputMap.get("emailalerttransaction") + 1;
		int nseqnoalerttranshistory = (int) inputMap.get("emailalerttranshistory") + 1;

		String query = " insert into emailalerttransaction(nrunningno,ntransactionno,nemailconfigcode,dcreateddate,dmodifieddate,semail,sreason,nstatus,nsitecode,ssubject,stemplatebody,ssystemid,nusercode,ncontrolcode) select  "
				+ nseqnoalerttrans + ",ntransactionno,nemailconfigcode" + ",'"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "','"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',semail,sreason,"
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ", " + userInfo.getNsitecode()
				+ ",ssubject,stemplatebody,ssystemid," + userInfo.getNusercode() + ", " + ncontrolcode
				+ " from emailalerttransaction where nrunningno=" + nrunningno + ";";
		jdbcTemplate.execute(query);

		query = " insert into emailalerttranshistory (nemailhistorycode,nrunningno,ntransstatus,"
				+ "dtransdate,noffsetdtransdate,ntransdatetimezonecode,smailsentby,nmailitemid,nstatus,dmodifieddate,nsitecode) values"
				+ "( " + nseqnoalerttranshistory + "," + nseqnoalerttrans + ","
				+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ",'"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
				+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
				+ userInfo.getNtimezonecode() + ",'RESENT'," + " "
				+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ",'"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNsitecode() + " );";

		jdbcTemplate.execute(query);
		query = " update seqnoemailmanagement set nsequenceno = " + (nseqnoalerttrans)
				+ " where stablename = 'emailalerttransaction';" + "update seqnoemailmanagement set nsequenceno = "
				+ (nseqnoalerttranshistory) + " where stablename = 'emailalerttranshistory';";
		jdbcTemplate.execute(query);
		query = "select " + nseqnoalerttrans + " as nrunningno,ntransactionno,nemailconfigcode" + ",N'"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + "semail,sreason,"
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " from emailalerttransaction where nrunningno=" + nrunningno + ";";
		EmailAlertTransaction emailAlertTransaction = (EmailAlertTransaction) jdbcUtilityFunction.queryForObject(query,
				EmailAlertTransaction.class, jdbcTemplate);
		List<Object> lstnewobject = new ArrayList<>();
		lstnewobject.add(emailAlertTransaction);
		auditUtilityFunction.fnInsertAuditAction(lstnewobject, 1, null, Arrays.asList("IDS_RESENT"), userInfo);
		return getEmailStatus(userInfo);
	}
}
