package com.agaramtech.qualis.emailmanagement.service.emailtemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.emailmanagement.model.EmailTag;
import com.agaramtech.qualis.emailmanagement.model.EmailTemplate;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class EmailTemplateDAOImpl implements EmailTemplateDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmailTemplateDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	@Override
	public ResponseEntity<Object> getEmailTemplate(int nmastersitecode) throws Exception {
		final String strQuery = " select et.*, eg.stagname from emailtemplate et, emailtag eg where et.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and et.nsitecode=" + nmastersitecode
				+ " and et.nemailtagcode= eg.nemailtagcode " + " and eg.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by et.nemailtemplatecode desc ";
		LOGGER.info("getEmailTemplate() called");
		return new ResponseEntity<>(jdbcTemplate.query(strQuery, new EmailTemplate()), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getEmailTag(int nmastersitecode) throws Exception {
		final String strQuery = "select * from emailtag  where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and nsitecode=" + nmastersitecode;
		List<EmailTag> lst = jdbcTemplate.query(strQuery, new EmailTag());
		return new ResponseEntity<>(lst, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createEmailTemplate(EmailTemplate emailTemplate, UserInfo userInfo) throws Exception {
		final String lock = "LOCK TABLE emailtemplate IN ACCESS EXCLUSIVE MODE;"
				+ "SELECT nemailtemplatecode  FROM emailtemplate ";
		jdbcTemplate.execute(lock);
		String str = "select stemplatename from emailtemplate where stemplatename=N'"
				+ stringUtilityFunction.replaceQuote(emailTemplate.getStemplatename()) + "' and nsitecode="
				+ userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		List<EmailTemplate> lst = jdbcTemplate.query(str, new EmailTemplate());
		if (lst.size() == 0) {
			int nseqtemplate = jdbcTemplate.queryForObject(
					"select nsequenceno from Seqnoemailmanagement where stablename = 'emailtemplate' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "",
					Integer.class);
			String stemplatebody = emailTemplate.getStemplatebody();
			stemplatebody = stemplatebody.replace("?", "");

			final String emailtemplate = "insert into emailtemplate (nemailtemplatecode,nemailtagcode,nregtypecode,nregsubtypecode,stemplatename,stemplatebody,ssubject,nsitecode,nstatus,dmodifieddate) "
					+ " values(" + nseqtemplate + "+1," + emailTemplate.getNemailtagcode() + ","
					+ emailTemplate.getNregtypecode() + "," + emailTemplate.getNregsubtypecode() + ",N'"
					+ stringUtilityFunction.replaceQuote(emailTemplate.getStemplatename()) + "',N'"
					+ stringUtilityFunction.replaceQuote(stemplatebody) + "',N'"
					+ stringUtilityFunction.replaceQuote(emailTemplate.getSsubject()) + "',"
					+ userInfo.getNmastersitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ",'" + dateUtilityFunction.getCurrentDateTime(userInfo)
					+ "' ); Update Seqnoemailmanagement set nsequenceno =  nsequenceno+1 where stablename = 'emailtemplate' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			jdbcTemplate.execute(emailtemplate);
			auditUtilityFunction.fnInsertAuditAction(Arrays.asList(emailTemplate), 1, null,
					Arrays.asList("IDS_ADDEMAILTEMPLATE"), userInfo);
			return getEmailTemplate(userInfo.getNmastersitecode());
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	@Override
	public ResponseEntity<Object> getEmailTagFilter(Integer nemailtagcode, UserInfo userInfo) throws Exception {
		List<String> lst = new ArrayList<>();
		if (nemailtagcode > 0) {
			String strQuery = "select squery from emailtag where nemailtagcode=" + nemailtagcode + " and nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode() + "";
			EmailTag objEmail = jdbcTemplate.queryForObject(strQuery, new EmailTag());
			if (objEmail != null) {
				String dataProviderXML = objEmail.getSquery();
				while (dataProviderXML.contains("<<")) {
					String lowerCase = dataProviderXML.toLowerCase();
					int index = lowerCase.indexOf("<<");
					if (index > -1) {
						String findText = dataProviderXML.substring(index);
						int textLastIndex = findText.indexOf(">>");
						String actualText = dataProviderXML.substring(index, index + (textLastIndex + 2));
						lst.add(actualText);
						dataProviderXML = dataProviderXML.substring(0, index) + ""
								+ dataProviderXML.substring(index + (textLastIndex + 2), dataProviderXML.length());
						nemailtagcode++;
					}
				}
			}
		}
		return new ResponseEntity<>(lst, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getActiveEmailTemplateById(int nemailtemplatecode, UserInfo userInfo)
			throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		List<EmailTemplate> lsttemplate = (List<EmailTemplate>) jdbcTemplate.query(
				"select t.stagname,e.nemailtemplatecode,e.nemailtagcode,e.stemplatename,e.ssubject,e.stemplatebody from emailtemplate e,emailtag t where t.nemailtagcode=e.nemailtagcode and nemailtemplatecode = "
						+ nemailtemplatecode + " and e.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "",
				new EmailTemplate());
		if (lsttemplate.size() == 0) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			map.put("EmailTag", getEmailTag(userInfo.getNmastersitecode()).getBody());
			map.put("EmailTemplate", lsttemplate);
			map.put("EmailParameter", getEmailTagFilter(lsttemplate.get(0).getNemailtagcode(), userInfo).getBody());
			return new ResponseEntity<>(map, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> updateEmailTemplate(EmailTemplate emailTemplate, UserInfo userInfo) throws Exception {
		final List<Object> lstbefore = new ArrayList<>();
		final String str1 = "select * from emailtemplate where nemailtemplatecode="
				+ emailTemplate.getNemailtemplatecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();
		final EmailTemplate emailTemplateById = (EmailTemplate) jdbcUtilityFunction.queryForObject(str1,
				EmailTemplate.class, jdbcTemplate);
		if (emailTemplateById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			String str = "select * from emailtemplate where nemailtemplatecode<>"
					+ emailTemplate.getNemailtemplatecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode() + " and  stemplatename=N'"
					+ stringUtilityFunction.replaceQuote(emailTemplate.getStemplatename()) + "'";
			final List<EmailTemplate> lst = (List<EmailTemplate>) jdbcTemplate.query(str, new EmailTemplate());
			if (lst.size() == 0) {
				String stemplatebody = emailTemplate.getStemplatebody();
				stemplatebody = stemplatebody.replace("?", "");
				str = "select * from emailtemplate where nemailtemplatecode=" + emailTemplate.getNemailtemplatecode()
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final EmailTemplate EmailTemplate = (EmailTemplate) jdbcUtilityFunction.queryForObject(str,
						EmailTemplate.class, jdbcTemplate);
				lstbefore.add(EmailTemplate);
				final String updateQuery = "update emailtemplate set nregtypecode=" + emailTemplate.getNregtypecode()
						+ ",nregsubtypecode=" + emailTemplate.getNregsubtypecode() + ",stemplatename=N'"
						+ stringUtilityFunction.replaceQuote(emailTemplate.getStemplatename()) + "',stemplatebody=N'"
						+ stringUtilityFunction.replaceQuote(stemplatebody) + "',ssubject=N'"
						+ stringUtilityFunction.replaceQuote(emailTemplate.getSsubject()) + "'," + " nsitecode="
						+ userInfo.getNmastersitecode() + ",nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ",nemailtagcode="
						+ emailTemplate.getNemailtagcode() + ",dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nemailtemplatecode="
						+ emailTemplate.getNemailtemplatecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				jdbcTemplate.execute(updateQuery);
				auditUtilityFunction.fnInsertAuditAction(Arrays.asList(emailTemplate), 2, lstbefore,
						Arrays.asList("IDS_EDITEMAILTEMPLATE"), userInfo);
				return getEmailTemplate(userInfo.getNmastersitecode());
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	@Override
	public ResponseEntity<Object> deleteEmailTemplate(EmailTemplate emailTemplate, UserInfo userInfo) throws Exception {
		final List<Object> lstafter = new ArrayList<>();
		final String str = "select * from emailtemplate where nemailtemplatecode="
				+ emailTemplate.getNemailtemplatecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final EmailTemplate emailTemplateById = (EmailTemplate) jdbcUtilityFunction.queryForObject(str,
				EmailTemplate.class, jdbcTemplate);
		if (emailTemplateById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String deletequery = "select count(*) from emailconfig where nemailtemplatecode ="
					+ emailTemplate.getNemailtemplatecode() + " and nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			int ncount = jdbcTemplate.queryForObject(deletequery, Integer.class);
			if (ncount == 0) {
				final String emailtemplate = "update emailtemplate set nstatus="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' " + "where nemailtemplatecode="
						+ emailTemplate.getNemailtemplatecode();

				jdbcTemplate.execute(emailtemplate);
				emailTemplate.setNstatus(Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				lstafter.add(emailTemplate);
				auditUtilityFunction.fnInsertAuditAction(lstafter, 1, null, Arrays.asList("IDS_DELETEEMAILTEMPLATE"),
						userInfo);
				return getEmailTemplate(userInfo.getNmastersitecode());
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_USEDINEMAILCONFIG", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		}
	}
}
