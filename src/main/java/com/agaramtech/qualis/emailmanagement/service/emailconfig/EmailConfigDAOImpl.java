package com.agaramtech.qualis.emailmanagement.service.emailconfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import com.agaramtech.qualis.credential.model.ControlMaster;
import com.agaramtech.qualis.credential.model.Users;
import com.agaramtech.qualis.emailmanagement.model.EmailConfig;
import com.agaramtech.qualis.emailmanagement.model.EmailHost;
import com.agaramtech.qualis.emailmanagement.model.EmailScreen;
import com.agaramtech.qualis.emailmanagement.model.EmailTemplate;
import com.agaramtech.qualis.emailmanagement.model.EmailUserConfig;
import com.agaramtech.qualis.emailmanagement.model.EmailUserQuery;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import lombok.RequiredArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "emailconfig" table by
 * implementing methods from its interface.
 */
@RequiredArgsConstructor
@Repository
public class EmailConfigDAOImpl implements EmailConfigDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmailConfigDAOImpl.class);
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	@Override
	public ResponseEntity<Object> getEmailConfig(Integer nemailconfigcode, UserInfo userinfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		EmailConfig selectedEmailConfig = null;
		if (nemailconfigcode == null) {
			final String strQuery = " select etc.nemailconfigcode , etc.nemailhostcode ,etc.nsitecode,etc.nenableemail,eh.shostname , et.stemplatename ,"
					+ " coalesce (ts.jsondata ->'stransdisplaystatus'->>'" + userinfo.getSlanguagetypecode()
					+ "',ts.jsondata ->'stransdisplaystatus'->>'en-US') as senablemailstatus,"
					+ " etc.nneedattachment, es.scolumnname,  etc.nemailtemplatecode,etc.ncontrolcode , etc.nemailscreencode, etc.nstatus, "
					+ " coalesce (ts.jsondata ->'stransdisplaystatus'->>'" + userinfo.getSlanguagetypecode()
					+ "',ts.jsondata ->'stransdisplaystatus'->>'en-US') as senablestatus,"
					+ " coalesce (cm.jsondata ->'scontrolids'->>'" + userinfo.getSlanguagetypecode()
					+ "',cm.jsondata ->'scontrolids'->>'en-US') as  scontrolids,"
					+ " qf.nformcode, coalesce (qf.jsondata->'sdisplayname'->>'" + userinfo.getSlanguagetypecode()
					+ "',qf.jsondata->'sdisplayname'->>'en-US')as sscreenname,"
					+ " coalesce (qf.jsondata->'sdisplayname'->>'" + userinfo.getSlanguagetypecode()
					+ "',qf.jsondata->'sdisplayname'->>'en-US')as sformname, euq.sdisplayname "
					+ " from emailconfig etc  " + " join emailhost eh on eh.nemailhostcode  = etc.nemailhostcode  "
					+ " join emailtemplate et on et.nemailtemplatecode = etc.nemailtemplatecode "
					+ " join emailscreen es on es.nemailscreencode  = etc.nemailscreencode "
					+ " join emailuserquery euq on euq.nemailuserquerycode = etc.nemailuserquerycode "
					+ " join transactionstatus ts on ts.ntranscode=etc.nenableemail "
					+ " join controlmaster cm  on cm.ncontrolcode = etc.ncontrolcode "
					+ " join qualisforms qf on qf.nformcode = etc.nformcode " + " where  etc.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and eh.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and et.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and es.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and euq.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and ts.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and cm.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and qf.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and  etc.nsitecode="
					+ userinfo.getNmastersitecode() + " and eh.nsitecode=" + userinfo.getNmastersitecode() + " "
					+ "and et.nsitecode=" + userinfo.getNmastersitecode() + " and es.nsitecode="
					+ userinfo.getNmastersitecode() + "   order by nemailconfigcode desc";
			final List<EmailConfig> usersEmailConfig = jdbcTemplate.query(strQuery, new EmailConfig());
			LOGGER.info("getEmailConfig() called");
			if (usersEmailConfig.isEmpty()) {
				outputMap.put("EmailConfig", usersEmailConfig);
				outputMap.put("SelectedEmailConfig", null);
				return new ResponseEntity<>(outputMap, HttpStatus.OK);
			} else {
				outputMap.put("EmailConfig", usersEmailConfig);
				selectedEmailConfig = (EmailConfig) usersEmailConfig.get(0);
				nemailconfigcode = selectedEmailConfig.getNemailconfigcode();

			}
		} else {
			final String strQuery1 = " select eh.sprofilename, etc.nemailconfigcode , etc.nemailhostcode ,etc.nsitecode,etc.nenableemail,"
					+ " etc.nemailtemplatecode, etc.nemailscreencode,etc.nstatus,eh.shostname,et.stemplatename, "
					+ " coalesce (t.jsondata->'stransdisplaystatus'->>'" + userinfo.getSlanguagetypecode()
					+ "',t.jsondata->'stransdisplaystatus'->>'en-US') as senablestatus,qf.nformcode,"
					+ " coalesce (qf.jsondata->'sdisplayname'->>'" + userinfo.getSlanguagetypecode()
					+ "',qf.jsondata->'sdisplayname'->>'en-US')as sscreenname,cm.ncontrolcode,"
					+ " coalesce (qf.jsondata->'sdisplayname'->>'" + userinfo.getSlanguagetypecode()
					+ "',qf.jsondata->'sdisplayname'->>'en-US')as sformname, "
					+ " coalesce (cm.jsondata->'scontrolids'->>'" + userinfo.getSlanguagetypecode()
					+ "',cm.jsondata->'scontrolids'->>'en-US') as scontrolids,euq.sdisplayname  "
					+ " from emailconfig etc " + " join emailhost eh on eh.nemailhostcode = etc.nemailhostcode "
					+ " join emailtemplate et on et.nemailtemplatecode = etc.nemailtemplatecode "
					+ " join emailscreen es on  es.nemailscreencode = etc.nemailscreencode "
					+ " join transactionstatus t on  t.ntranscode=etc.nenableemail "
					+ " join controlmaster cm on cm.ncontrolcode = etc.ncontrolcode  "
					+ " join qualisforms qf on qf.nformcode = etc.nformcode "
					+ " join emailuserquery euq on euq.nemailuserquerycode = etc.nemailuserquerycode"
					+ " where etc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and eh.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and et.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and es.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and t.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and cm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and qf.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and euq.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and etc.nsitecode=" + userinfo.getNmastersitecode() + " and eh.nsitecode="
					+ userinfo.getNmastersitecode() + " and et.nsitecode=" + userinfo.getNmastersitecode()
					+ " and es.nsitecode=" + userinfo.getNmastersitecode() + " and nemailconfigcode ="
					+ nemailconfigcode + " " + " order by nemailconfigcode asc";
			final List<EmailConfig> lst1 = jdbcTemplate.query(strQuery1, new EmailConfig());
			if (!lst1.isEmpty())
				selectedEmailConfig = lst1.get(0);
		}
		if (selectedEmailConfig == null) {
			final String returnString = commonFunction.getMultilingualMessage(
					Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userinfo.getSlanguagefilename());
			return new ResponseEntity<>(returnString, HttpStatus.EXPECTATION_FAILED);
		} else {
			outputMap.put("SelectedEmailConfig", selectedEmailConfig);
			final String query = "select uc.nemailuserconfigcode,uc.nstatus,u.nusercode,concat (sfirstname,' ',slastname,'(',sloginid,')') as susername,u.semail,uc.nemailconfigcode from emailuserconfig uc,users u where u.nusercode=uc.nusercode and u.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and uc.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and uc.nemailconfigcode="
					+ selectedEmailConfig.getNemailconfigcode() + "";
			List<EmailUserConfig> lstEmailUserConfig = jdbcTemplate.query(query, new EmailUserConfig());
			outputMap.put("users", lstEmailUserConfig);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
	}

	public ResponseEntity<Object> getEmailConfigDetails(int nformCode, UserInfo userInfo) throws Exception {
		final Map<String, Object> returnMap = new HashMap<>();
		String hostquery = "select nemailhostcode,shostname from emailhost where nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
				+ userInfo.getNmastersitecode() + ";";
		List<EmailHost> lstemailhost = jdbcTemplate.query(hostquery, new EmailHost());
		String templatequery = "select nemailtemplatecode,stemplatename from emailtemplate where nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
				+ userInfo.getNmastersitecode() + " ";
		List<EmailTemplate> lstemailtemplate = jdbcTemplate.query(templatequery, new EmailTemplate());
		String screenquery = "select es.nemailscreencode ," + "coalesce (qf.jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode() + "',qf.jsondata->'sdisplayname'->>'en-US')as sscreenname ,"
				+ "es.scolumnname,es.nformcode from emailscreen es"
				+ " join qualisforms qf on qf.nformcode = es.nformcode " + " where es.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and es.nsitecode = "
				+ userInfo.getNmastersitecode() + " ";
		List<EmailScreen> lstemailscreen = jdbcTemplate.query(screenquery, new EmailScreen());
		String usersquery = "select nusercode ,concat(sfirstname, slastname, '(', sloginid , ')') as semail  from users where nusercode > 0  and nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
				+ userInfo.getNmastersitecode() + ";";
		List<Users> lstusers = jdbcTemplate.query(usersquery, new Users());
		returnMap.putAll((Map<String, Object>) getEmailConfigControl(nformCode, userInfo).getBody());
		returnMap.put("EmailHost", lstemailhost);
		returnMap.put("EmailTemplate", lstemailtemplate);
		returnMap.put("EmailScreen", lstemailscreen);
		returnMap.put("Users", lstusers);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getEmailConfigControl(int nformCode, UserInfo userInfo) throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		String controlquery = "  select coalesce (cm.jsondata->'scontrolids'->>'" + userInfo.getSlanguagetypecode()
				+ "',cm.jsondata->'scontrolids'->>'en-US')scontrolids,cm.ncontrolcode from controlmaster cm, sitecontrolmaster scm where "
				+ "  cm.ncontrolcode = scm.ncontrolcode and  cm.nformcode = " + nformCode
				+ "  and scm.nisemailrequired = " + Enumeration.TransactionStatus.YES.gettransactionstatus() + " "
				+ " and cm.nstatus in ( " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus() + ") " + " and scm.nstatus in ("
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus() + ") and scm.nsitecode="
				+ userInfo.getNmastersitecode() + ";";
		List<ControlMaster> lstcontrol = jdbcTemplate.query(controlquery, new ControlMaster());
		String emailuserquery = "select nemailuserquerycode,squery, sdisplayname from emailuserquery emq where nformcode="
				+ nformCode + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		List<EmailUserQuery> lstemailuser = jdbcTemplate.query(emailuserquery, new EmailUserQuery());
		returnMap.put("FormControls", lstcontrol);
		returnMap.put("EmailUserQuery", lstemailuser);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createEmailConfig(EmailConfig emailconfig, String nusercode, UserInfo userInfo)
			throws Exception {
		final List<Object> savedEmailConfigList = new ArrayList<>();
		String statuscheck = "";
		statuscheck = "select nemailconfigcode from emailconfig where ncontrolcode = " + emailconfig.getNcontrolcode()
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();
		EmailConfig emailconfig1 = (EmailConfig) jdbcUtilityFunction.queryForObject(statuscheck, EmailConfig.class,
				jdbcTemplate);
		if (emailconfig1 != null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_ALREDYMAILCONFIGURED", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		} else {
			String sequencequery = "select nsequenceno from SeqNoEmailManagement where stablename ='emailconfig' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			int nsequenceno = (int) jdbcUtilityFunction.queryForObject(sequencequery, Integer.class, jdbcTemplate);
			nsequenceno++;
			final String insertquery = "insert into emailconfig (nemailconfigcode,ncontrolcode,nemailhostcode,nemailscreencode,nemailtemplatecode,"
					+ "nemailuserquerycode,nenableemail,nformcode,nneedattachment,nsitecode,nstatus,dmodifieddate) "
					+ "values (" + nsequenceno + "," + emailconfig.getNcontrolcode() + ","
					+ emailconfig.getNemailhostcode() + "," + emailconfig.getNemailscreencode() + ","
					+ emailconfig.getNemailtemplatecode() + "," + " " + emailconfig.getNemailuserquerycode() + ","
					+ emailconfig.getNenableemail() + "," + emailconfig.getNformcode() + ","
					+ emailconfig.getNneedattachment() + "," + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ",'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' )";
			jdbcTemplate.execute(insertquery);
			final String updatequery = "update SeqNoEmailManagement set nsequenceno =" + nsequenceno
					+ " where stablename='emailconfig' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			jdbcTemplate.execute(updatequery);
			savedEmailConfigList.add(emailconfig);
			emailconfig.setNemailconfigcode(nsequenceno);
			auditUtilityFunction.fnInsertAuditAction(savedEmailConfigList, 1, null, Arrays.asList("IDS_ADDEMAILCONFIG"),
					userInfo);
			if (nusercode != null) {
				final String query = "select s.nusercode,s.nstatus from users s where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
						+ userInfo.getNmastersitecode() + " and nusercode in (" + nusercode + ")";
				List<EmailUserConfig> lstEmailUserConfig = (List<EmailUserConfig>) jdbcTemplate.query(query,
						new EmailUserConfig());
				String sequencequery1 = "select nsequenceno from SeqNoEmailManagement where stablename ='emailuserconfig' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
				int nsequenceno1 = (int) jdbcUtilityFunction.queryForObject(sequencequery1, Integer.class,
						jdbcTemplate);
				nsequenceno1++;
				final String insertquery1 = "Insert into emailuserconfig (nemailuserconfigcode,nemailconfigcode,nstatus,nusercode,dmodifieddate)"
						+ "select " + nsequenceno1 + "+ rank() over (order by nusercode) as nemailuserconfigcode,"
						+ emailconfig.getNemailconfigcode() + " as nemailconfigcode ,"
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " as nstatus,nusercode, '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'  from users where nusercode in ("
						+ nusercode + ")";
				jdbcTemplate.execute(insertquery1);
				String updatequery1 = "update SeqNoEmailManagement set nsequenceno =(" + nsequenceno1 + ") +( "
						+ lstEmailUserConfig.size() + ")  where stablename='emailuserconfig' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
				jdbcTemplate.execute(updatequery1);
				final List<Object> savedList = new ArrayList<>();
				savedList.add(lstEmailUserConfig);
				auditUtilityFunction.fnInsertListAuditAction(savedList, 1, null,
						Arrays.asList("IDS_ADDEMAILUSERCONFIG"), userInfo);
			}
			return getEmailConfig(null, userInfo);
		}
	}

	@Override
	public ResponseEntity<Object> getActiveEmailConfigById(int nemailconfigcode, UserInfo userInfo) throws Exception {
		final Map<String, Object> objMap = new HashMap<>();
		final String strQuery = "select eh.sprofilename, etc.nemailconfigcode , etc.nemailhostcode ,etc.nsitecode,etc.nenableemail,"
				+ "etc.nemailtemplatecode , etc.nemailscreencode ,etc.nstatus,eh.shostname , et.stemplatename ,"
				+ "coalesce (t.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "',t.jsondata->'stransdisplaystatus'->>'en-US') as senablestatus, "
				+ "coalesce (qf.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',qf.jsondata->'sdisplayname'->>'en-US')as sscreenname,qf.nformcode,"
				+ "coalesce (cm.jsondata->'scontrolids'->>'" + userInfo.getSlanguagetypecode()
				+ "',cm.jsondata->'scontrolids'->>'en-US') as scontrolids,cm.ncontrolcode,"
				+ "emu.squery,etc.nemailuserquerycode, emu.sdisplayname "
				+ "from emailconfig etc,emailhost eh,emailtemplate et,emailscreen es,transactionstatus t, "
				+ "qualisforms qf,controlmaster cm ,emailuserquery emu "
				+ "where t.ntranscode=etc.nenableemail and qf.nformcode = etc.nformcode and cm.ncontrolcode = etc.ncontrolcode and es.nemailscreencode = etc.nemailscreencode "
				+ "and  etc.nemailhostcode = eh.nemailhostcode and etc.nemailtemplatecode = et.nemailtemplatecode "
				+ "and emu.nemailuserquerycode=etc.nemailuserquerycode" + " and etc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and emu.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and eh.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and et.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and es.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and eh.nsitecode="
				+ userInfo.getNmastersitecode() + " and et.nsitecode=" + userInfo.getNmastersitecode()
				+ " and es.nsitecode=" + userInfo.getNmastersitecode() + " and etc.nemailconfigcode="
				+ nemailconfigcode;
		List<EmailConfig> lst = jdbcTemplate.query(strQuery, new EmailConfig());
		if (lst.isEmpty()) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			objMap.put("EmailConfig", lst.get(0));
			objMap.putAll((Map<String, Object>) getEmailConfigDetails(lst.get(0).getNformcode(), userInfo).getBody());
			return new ResponseEntity<>(objMap, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> deleteEmailConfig(EmailConfig emailconfig, UserInfo userInfo) throws Exception {
		final EmailConfig emailConfig = (EmailConfig) jdbcUtilityFunction
				.queryForObject(
						"select nemailconfigcode,nemailhostcode,nemailscreencode,nemailtemplatecode,"
								+ " nenableemail,ncontrolcode,nformcode from emailconfig " + " where nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and nemailconfigcode = " + emailconfig.getNemailconfigcode() + "",
						EmailConfig.class, jdbcTemplate);
		if (emailConfig == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String userQuery = "select * from emailuserconfig where nemailconfigcode="
					+ emailconfig.getNemailconfigcode();
			final List<EmailUserConfig> lstEmailUserConfig = jdbcTemplate.query(userQuery, new EmailUserConfig());
			jdbcTemplate.execute(
					"delete from emailuserconfig where nemailconfigcode=" + emailconfig.getNemailconfigcode() + "");
			auditUtilityFunction.fnInsertListAuditAction(Arrays.asList(lstEmailUserConfig), 1, null,
					Arrays.asList("IDS_DELETEEMAILUSERCONFIG"), userInfo);
			final List<Object> deletedEmailHostList = new ArrayList<>();
			final String updateQueryString = "update emailconfig set nstatus = "
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " , dmodifieddate= '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nemailconfigcode="
					+ emailconfig.getNemailconfigcode();
			jdbcTemplate.execute(updateQueryString);
			emailconfig.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
			deletedEmailHostList.add(emailconfig);
			auditUtilityFunction.fnInsertAuditAction(deletedEmailHostList, 1, null,
					Arrays.asList("IDS_DELETEEMAILCONFIG"), userInfo);
			return getEmailConfig(null, userInfo);
		}
	}

	@Override
	public ResponseEntity<Object> updateEmailConfig(EmailConfig emailconfig, UserInfo userInfo) throws Exception {
		EmailConfig emailConfigById = (EmailConfig) jdbcUtilityFunction.queryForObject(
				" select nemailconfigcode,nemailhostcode,nemailscreencode, nemailtemplatecode,nenableemail,ncontrolcode,nformcode, nemailuserquerycode "
						+ " from emailconfig  where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and nemailconfigcode = "
						+ emailconfig.getNemailconfigcode() + "",
				EmailConfig.class, jdbcTemplate);
		if (emailConfigById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			String statuscheck = "";
			statuscheck = "select nemailconfigcode from emailconfig where ncontrolcode = "
					+ emailconfig.getNcontrolcode() + " and nemailconfigcode <> " + emailconfig.getNemailconfigcode()
					+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode=" + userInfo.getNmastersitecode();
			EmailConfig emailConfigObj = (EmailConfig) jdbcUtilityFunction.queryForObject(statuscheck,
					EmailConfig.class, jdbcTemplate);
			if (emailConfigObj != null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ALREDYMAILCONFIGURED",
						userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			} else {

				final String updateQueryString = "update emailconfig set nemailtemplatecode="
						+ emailconfig.getNemailtemplatecode() + ", nemailuserquerycode="
						+ emailconfig.getNemailuserquerycode() + "," + "nemailhostcode="
						+ emailconfig.getNemailhostcode() + ",ncontrolcode=" + emailconfig.getNcontrolcode()
						+ ", nemailscreencode=" + emailconfig.getNemailscreencode() + ", nformcode= "
						+ emailconfig.getNformcode() + " , " + "nenableemail=" + emailconfig.getNenableemail()
						+ ", dmodifieddate= '" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "' where nemailconfigcode=" + emailconfig.getNemailconfigcode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				jdbcTemplate.execute(updateQueryString);
				final List<Object> listAfterSave = new ArrayList<>();
				listAfterSave.add(emailconfig);
				final List<Object> listBeforeSave = new ArrayList<>();
				listBeforeSave.add(emailConfigById);
				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave,
						Arrays.asList("IDS_EDITEMAILCONFIG"), userInfo);
				return getEmailConfig(emailconfig.getNemailconfigcode(), userInfo);
			}
		}
	}

	@Override
	public ResponseEntity<Object> getUserRoleEmail(int nuserrolecode, UserInfo userInfo) throws Exception {
		final String strQuery = "select u.semail,u.nusercode from  usermultirole um,userssite us,users u,userrole ur "
				+ " where ur.nuserrolecode=um.nuserrolecode and um.nusersitecode=us.nusersitecode and us.nusercode=u.nusercode and us.nsitecode=1 and "
				+ " u.nsitecode=" + userInfo.getNmastersitecode() + " and um.nuserrolecode=" + nuserrolecode
				+ " and u.ntransactionstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and u.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and us.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and um.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return new ResponseEntity<>(jdbcTemplate.query(strQuery, new Users()), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getUserEmailConfig(int nemailconfigcode, UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		String query = "select * from emailconfig where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nemailconfigcode="
				+ nemailconfigcode;
		EmailUserConfig emailUserConfig = (EmailUserConfig) jdbcUtilityFunction.queryForObject(query,
				EmailUserConfig.class, jdbcTemplate);
		if (emailUserConfig != null) {
			query = "select uc.nusercode from emailuserconfig uc where  uc.nemailconfigcode=" + nemailconfigcode
					+ " and uc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			List<String> lstUserCodes = jdbcTemplate.queryForList(query, String.class);
			String susercode = lstUserCodes.stream().collect(Collectors.joining(","));
			if (susercode.isEmpty()) {
				susercode = "-1";
			} else {
				susercode = susercode + ",-1";
			}
			query = "select u.nusercode,concat(sfirstname, slastname, '(', sloginid , ')') as semail from users u where u.nusercode not in"
					+ "(" + susercode + ")  and u.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nsitecode="
					+ userInfo.getNmastersitecode();
			List<EmailUserConfig> lstEmailUserConfig = jdbcTemplate.query(query, new EmailUserConfig());
			if (lstEmailUserConfig.size() > 0) {
				outputMap.put("users", lstEmailUserConfig);
				return new ResponseEntity<>(outputMap, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_NOAVAIALBLEUSERS", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> createUsers(Integer nemailconfigcode, String nusercode, UserInfo userInfo)
			throws Exception {
		final String query1 = "Select * from emailconfig where nemailconfigcode=" + nemailconfigcode + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final EmailConfig emailconfig = (EmailConfig) jdbcUtilityFunction.queryForObject(query1, EmailConfig.class,
				jdbcTemplate);
		if (emailconfig == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			String strQuery = "select * from emailuserconfig where nusercode in(" + nusercode
					+ ") and nemailconfigcode=" + nemailconfigcode + "";
			List<EmailUserConfig> listUsercode;
			listUsercode = (List<EmailUserConfig>) jdbcTemplate.query(strQuery, new EmailUserConfig());
			String[] stringArray = nusercode.split(",");
			String nonMatchingUserCodes = Arrays.stream(stringArray).filter(
					code -> listUsercode.stream().noneMatch(user -> String.valueOf(user.getNusercode()).equals(code)))
					.collect(Collectors.joining(","));
			if (!nonMatchingUserCodes.isEmpty()) {
				final String query = "select s.nusercode,s.nstatus," + userInfo.getNmastersitecode()
						+ " as nsitecode,3 as nemailtype," + nemailconfigcode
						+ " as nemailconfigcode from users s where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nusercode in ("
						+ nusercode + ")";
				List<EmailUserConfig> lstEmailUserConfig = (List<EmailUserConfig>) jdbcTemplate.query(query,
						new EmailUserConfig());
				String sequencequery = "select nsequenceno from  SeqNoEmailManagement where stablename ='emailuserconfig' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
				int nsequenceno = (int) jdbcUtilityFunction.queryForObject(sequencequery, Integer.class, jdbcTemplate);
				final String insertquery = "Insert into emailuserconfig (nemailuserconfigcode,nemailconfigcode,nstatus,nusercode,dmodifieddate)"
						+ "select " + nsequenceno + "+ rank() over (order by nusercode) as nemailuserconfigcode,"
						+ nemailconfigcode + " as nemailconfigcode ,"
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " as nstatus,nusercode,'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'  from users where nusercode in ("
						+ nonMatchingUserCodes + ")";
				jdbcTemplate.execute(insertquery);
				String updatequery = "update SeqNoEmailManagement set nsequenceno =(" + nsequenceno + ")+ ("
						+ lstEmailUserConfig.size() + ") where stablename ='emailuserconfig' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
				jdbcTemplate.execute(updatequery);
				final List<Object> savedList = new ArrayList<>();
				savedList.add(lstEmailUserConfig);
				auditUtilityFunction.fnInsertListAuditAction(savedList, 1, null,
						Arrays.asList("IDS_ADDEMAILUSERCONFIG"), userInfo);
			}
			return getEmailConfig(nemailconfigcode, userInfo);
		}
	}

	@Override
	public ResponseEntity<Object> deleteUsers(EmailUserConfig emailuserconfig, UserInfo userInfo) throws Exception {
		final List<Object> listAfterSave = new ArrayList<>();
		String query = "Select * from emailconfig where nemailconfigcode=" + emailuserconfig.getNemailconfigcode()
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();
		final EmailConfig emailconfig = (EmailConfig) jdbcUtilityFunction.queryForObject(query, EmailConfig.class,
				jdbcTemplate);
		if (emailconfig == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			query = "select * from emailuserconfig where nemailuserconfigcode="
					+ emailuserconfig.getNemailuserconfigcode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			// ALPD-3636
			final EmailUserConfig emailuserconfigs = (EmailUserConfig) jdbcUtilityFunction.queryForObject(query,
					EmailUserConfig.class, jdbcTemplate);
			if (emailuserconfigs != null) {
				query = "delete from emailuserconfig where nemailuserconfigcode="
						+ emailuserconfig.getNemailuserconfigcode();
				jdbcTemplate.execute(query);
				listAfterSave.add(emailuserconfigs);
				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 1, null,
						Arrays.asList("IDS_DELETEEMAILCONFIGUSERS"), userInfo);
				return getEmailConfig(emailuserconfig.getNemailconfigcode(), userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		}
	}
}
