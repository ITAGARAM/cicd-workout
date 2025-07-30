package com.agaramtech.qualis.emailmanagement.service.emailhost;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.emailmanagement.model.EmailHost;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "emailhost" table by
 * implementing methods from its interface.
 * 
 */
@AllArgsConstructor
@Repository
public class EmailHostDAOImpl implements EmailHostDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmailHostDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	@Override
	public ResponseEntity<Object> getEmailHost(final int nmasterSiteCode) throws Exception {
		final String strQuery = "select * from emailhost e where e.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and e.nsitecode = " + nmasterSiteCode
				+ " and e.nemailhostcode > 0";
		LOGGER.info("getEmailHost() called");
		return new ResponseEntity<>(jdbcTemplate.query(strQuery, new EmailHost()), HttpStatus.OK);
	}

	@Override
	public EmailHost getActiveEmailHostById(int nemailhostcode) throws Exception {
		final String strQuery = "select * from emailhost e where e.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and e.nemailhostcode = "
				+ nemailhostcode;
		EmailHost emailHost = (EmailHost) jdbcUtilityFunction.queryForObject(strQuery, EmailHost.class, jdbcTemplate);
		if (emailHost != null) {
			emailHost.setSpassword(null);
		}
		return emailHost;
	}

	@Override
	public ResponseEntity<Object> createEmailHost(EmailHost emailmanagementEmailHost, UserInfo userInfo)
			throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedEmailHostList = new ArrayList<>();
		final EmailHost EmailHostByName = getEmailHostByName(emailmanagementEmailHost.getSprofilename(),
				emailmanagementEmailHost.getNsitecode());
		if (EmailHostByName == null) {
			String sequencenoquery = "Select nsequenceno from SeqNoEmailManagement where stablename ='emailhost' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			int nsequenceno = (int) jdbcUtilityFunction.queryForObject(sequencenoquery, Integer.class,jdbcTemplate);
			nsequenceno++;
			emailmanagementEmailHost.setSpassword(emailmanagementEmailHost.getSpassword());
			String insertquery = "insert into emailhost (nemailhostcode,nportno,nsitecode,nstatus,sauthenticationname,semail,shostname,spassword,sprofilename,dmodifieddate) "
					+ "values(" + nsequenceno + "," + emailmanagementEmailHost.getNportno() + ","
					+ userInfo.getNmastersitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "," + "'" + emailmanagementEmailHost.getSauthenticationname() + "','"
					+ emailmanagementEmailHost.getSemail() + "','"
					+ stringUtilityFunction.replaceQuote(emailmanagementEmailHost.getShostname()) + "','"
					+ stringUtilityFunction.replaceQuote(emailmanagementEmailHost.getSpassword()) + "','"
					+ stringUtilityFunction.replaceQuote(emailmanagementEmailHost.getSprofilename()) + "', '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "')";
			jdbcTemplate.execute(insertquery);
			String updatequery = "update SeqNoEmailManagement set nsequenceno =" + nsequenceno
					+ " where stablename='emailhost' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			jdbcTemplate.execute(updatequery);
			projectDAOSupport.encryptPassword("emailhost", "nemailhostcode", nsequenceno,
					emailmanagementEmailHost.getSpassword(), "spassword");
			emailmanagementEmailHost.setSpassword("*****");
			savedEmailHostList.add(emailmanagementEmailHost);
			multilingualIDList.add("IDS_ADDEMAILHOST");
			auditUtilityFunction.fnInsertAuditAction(savedEmailHostList, 1, null, multilingualIDList, userInfo);
			return getEmailHost(userInfo.getNmastersitecode());
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	private EmailHost getEmailHostByName(final String sprofilename, final int nmasterSiteCode) throws Exception {
		final String strQuery = "select nemailhostcode from emailhost where sprofilename = N'"
				+ stringUtilityFunction.replaceQuote(sprofilename) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;
		return (EmailHost) jdbcUtilityFunction.queryForObject(strQuery, EmailHost.class, jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> updateEmailHost(EmailHost emailmanagementEmailHost, UserInfo userInfo)
			throws Exception {
		final EmailHost emailhost = getActiveEmailHostById(emailmanagementEmailHost.getNemailhostcode());
		if (emailhost == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select nemailhostcode from emailhost where sprofilename = '"
					+ stringUtilityFunction.replaceQuote(emailmanagementEmailHost.getSprofilename())
					+ "' and nemailhostcode <> " + emailmanagementEmailHost.getNemailhostcode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();
			final List<EmailHost> emailhostList = jdbcTemplate.query(queryString, new EmailHost());
			if (emailhostList.isEmpty()) {
				projectDAOSupport.encryptPassword("emailhost", "nemailhostcode",
						emailmanagementEmailHost.getNemailhostcode(), emailmanagementEmailHost.getSpassword(),
						"spassword");
				final String updateQueryString = "update emailhost set shostname=N'"
						+ stringUtilityFunction.replaceQuote(emailmanagementEmailHost.getShostname())
						+ "' ,sprofilename=N'"
						+ stringUtilityFunction.replaceQuote(emailmanagementEmailHost.getSprofilename()) + "',nportno="
						+ emailmanagementEmailHost.getNportno() + ", semail=N'" + emailmanagementEmailHost.getSemail()
						+ "', dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "' where nemailhostcode=" + emailmanagementEmailHost.getNemailhostcode();
				jdbcTemplate.execute(updateQueryString);
				final List<String> multilingualIDList = new ArrayList<>();
				multilingualIDList.add("IDS_EDITEMAILMANAGEMENTEMAILHOST");
				final List<Object> listAfterSave = new ArrayList<>();
				listAfterSave.add(emailmanagementEmailHost);
				final List<Object> listBeforeSave = new ArrayList<>();
				listBeforeSave.add(emailhost);
				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
						userInfo);
				return getEmailHost(emailmanagementEmailHost.getNsitecode());
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	@Override
	public ResponseEntity<Object> deleteEmailHost(EmailHost emailmanagementEmailHost, UserInfo userInfo)
			throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> deletedEmailHostList = new ArrayList<>();
		final EmailHost emailhost = (EmailHost) getActiveEmailHostById(emailmanagementEmailHost.getNemailhostcode());
		if (emailhost == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			String deletequery = "select count(nemailhostcode) from emailconfig where nemailhostcode ="
					+ emailhost.getNemailhostcode() + " and nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			int ncount = jdbcTemplate.queryForObject(deletequery, Integer.class);
			if (ncount == 0) {
				final String updateQueryString = "update emailhost set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' " + "where nemailhostcode="
						+ emailmanagementEmailHost.getNemailhostcode();
				jdbcTemplate.execute(updateQueryString);
				emailmanagementEmailHost.setNstatus(Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				emailmanagementEmailHost.setSpassword("*****");
				deletedEmailHostList.add(emailmanagementEmailHost);
				multilingualIDList.add("IDS_DELETEEMAILHOST");
				auditUtilityFunction.fnInsertAuditAction(deletedEmailHostList, 1, null, multilingualIDList, userInfo);
				return getEmailHost(emailmanagementEmailHost.getNsitecode());
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_USEDINEMAILCONFIG", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		}
	}
}
