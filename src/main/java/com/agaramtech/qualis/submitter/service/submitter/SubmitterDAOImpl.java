package com.agaramtech.qualis.submitter.service.submitter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.submitter.model.Institution;
import com.agaramtech.qualis.submitter.model.InstitutionCategory;
import com.agaramtech.qualis.submitter.model.InstitutionDepartment;
import com.agaramtech.qualis.submitter.model.InstitutionSite;
import com.agaramtech.qualis.submitter.model.Submitter;
import com.agaramtech.qualis.submitter.model.SubmitterMapping;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class SubmitterDAOImpl implements SubmitterDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(SubmitterDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel validatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	@Override
	public ResponseEntity<Map<String, Object>> getSubmitter(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		LOGGER.info("getSubmitter");
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		outputMap.putAll((Map<String, Object>) getInstitutionCategory(inputMap, userInfo).getBody());
		return new ResponseEntity<Map<String, Object>>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getInstitutionCategory(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		outputMap.putAll((Map<String, Object>) getSubmitterByFilter(inputMap, userInfo).getBody());
		/*
		 * int institutionCatCode =0;
		 * 
		 * String strQuery
		 * ="select * from institutioncategory where ninstitutioncatcode >0 and nsitecode ="
		 * +userInfo.getNmastersitecode()+" and nstatus="+Enumeration.TransactionStatus.
		 * ACTIVE.gettransactionstatus(); List<InstitutionCategory>
		 * lstInstitutionCategory = jdbcTemplate.query(strQuery, new
		 * InstitutionCategory()); outputMap.put("FilterInstitutionCategory",
		 * lstInstitutionCategory);
		 * 
		 * if(!lstInstitutionCategory.isEmpty()) { institutionCatCode
		 * =lstInstitutionCategory.get(lstInstitutionCategory.size()-1).
		 * getNinstitutioncatcode(); outputMap.put("selectedInstitutionCategory",
		 * lstInstitutionCategory.get(lstInstitutionCategory.size()-1));
		 * outputMap.put("defaultInstitutionCategory",
		 * lstInstitutionCategory.get(lstInstitutionCategory.size()-1));
		 * inputMap.put("ninstitutioncatcode", institutionCatCode);
		 * outputMap.putAll((Map<String,
		 * Object>)getInstitution(inputMap,userInfo).getBody());
		 * outputMap.putAll((Map<String,
		 * Object>)getInstitutionByCategory(inputMap,userInfo).getBody()); }else {
		 * outputMap.put("selectedInstitutionCategory", null);
		 * outputMap.put("defaultInstitutionCategory", null); }
		 */
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getInstitution(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		int institutionCode = 0;
		final String strQuery = "select i.*,ic.sinstitutioncatname from institution i "
				+ "join institutioncategory ic on i.ninstitutioncatcode= ic.ninstitutioncatcode "
				+ "where i.ninstitutioncatcode =" + inputMap.get("ninstitutioncatcode") + " and i.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and ic.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and i.nsitecode ="
				+ userInfo.getNmastersitecode() + "";
		final List<Institution> lstInstitution = jdbcTemplate.query(strQuery, new Institution());
		outputMap.put("FilterInstitution", lstInstitution);
		if (!lstInstitution.isEmpty()) {
			institutionCode = lstInstitution.get(lstInstitution.size() - 1).getNinstitutioncode();
			outputMap.put("selectedInstitution", lstInstitution.get(lstInstitution.size() - 1));
			inputMap.put("ninstitutioncode", institutionCode);
			outputMap.putAll((Map<String, Object>) getInstitutionSite(inputMap, userInfo).getBody());
		} else {
			outputMap.put("selectedInstitution", null);
			outputMap.put("FilterInstitutionSite", null);
			outputMap.put("selectedInstitutionSite", null);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getInstitutionByCategory(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		int institutionCode = 0;
		final String strQuery = "select i.*,ic.sinstitutioncatname from institution i "
				+ "join institutioncategory ic on i.ninstitutioncatcode= ic.ninstitutioncatcode "
				+ "where i.ninstitutioncatcode =" + inputMap.get("ninstitutioncatcode") + " and i.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ic.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and i.nsitecode ="
				+ userInfo.getNmastersitecode();
		final List<Institution> lstInstitution = jdbcTemplate.query(strQuery, new Institution());
		outputMap.put("FilterInstitution", lstInstitution);
		if (!lstInstitution.isEmpty()) {
			institutionCode = lstInstitution.get(lstInstitution.size() - 1).getNinstitutioncode();
			outputMap.put("defaultInstitution", lstInstitution.get(lstInstitution.size() - 1));
			inputMap.put("ninstitutioncode", institutionCode);
			outputMap.putAll((Map<String, Object>) getInstitutionSiteByInstitution(inputMap, userInfo).getBody());
		} else {
			outputMap.put("defaultInstitution", null);
			outputMap.put("FilterInstitutionSite", null);
			outputMap.put("defaultInstitutionSite", null);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getInstitutionSite(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		int institutionSitecode = 0;
		final int nflag = (int) inputMap.get("nflag");
		final String strQuery = "select * from institutionsite ins "
				+ "join institution i on ins.ninstitutioncode =i.ninstitutioncode where ins.ninstitutioncode ="
				+ inputMap.get("ninstitutioncode") + " and ins.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and i.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<InstitutionSite> lstInstitutionSite = jdbcTemplate.query(strQuery, new InstitutionSite());
		outputMap.put("FilterInstitutionSite", lstInstitutionSite);

		if (!lstInstitutionSite.isEmpty()) {
			institutionSitecode = lstInstitutionSite.get(lstInstitutionSite.size() - 1).getNinstitutionsitecode();
			outputMap.put("selectedInstitutionSite", lstInstitutionSite.get(lstInstitutionSite.size() - 1));
			inputMap.put("ninstitutionsitecode", institutionSitecode);
			if (nflag == 1) {
				outputMap.putAll((Map<String, Object>) getSubmitterByFilter(inputMap, userInfo).getBody());
			}
		} else {
			outputMap.put("selectedInstitutionSite", null);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getInstitutionSiteByInstitution(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		int institutionSitecode = 0;
		final int nflag = (int) inputMap.get("nflag");
		final String strQuery = "select * from institutionsite ins "
				+ "join institution i on ins.ninstitutioncode =i.ninstitutioncode "
				+ "where ins.ninstitutioncode in (select ninstitutioncode from submittermapping where ssubmittercode='"
				+ inputMap.get("ssubmittercode") + "' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") and ins.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and i.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by ninstitutioncode ";
		final List<InstitutionSite> lstInstitutionSite = jdbcTemplate.query(strQuery, new InstitutionSite());
		outputMap.put("FilterInstitutionSite", lstInstitutionSite);

		if (!lstInstitutionSite.isEmpty()) {
			institutionSitecode = lstInstitutionSite.get(lstInstitutionSite.size() - 1).getNinstitutionsitecode();
			outputMap.put("defaultInstitutionSite", lstInstitutionSite.get(lstInstitutionSite.size() - 1));
			inputMap.put("ninstitutionsitecode", institutionSitecode);
			if (nflag == 1) {
				outputMap.putAll((Map<String, Object>) getSubmitterByFilter(inputMap, userInfo).getBody());
			}
		} else {
			outputMap.put("defaultInstitutionSite", null);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getSubmitterByFilter(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();

		/*
		 * String strQuery
		 * ="select s.ssubmittercode,s.ninstitutioncatcode,s.ninstitutioncode,s.ninstitutionsitecode,"
		 * +
		 * "s.ninstitutiondeptcode,s.sfirstname||' '||s.slastname as ssubmittername,s.sfirstname,s.slastname,s.ssubmitterid,"
		 * + "s.sshortname,s.smobileno,s.semail, " +
		 * "case when s.sspecialization = '' then '-' else s.sspecialization end as sspecialization , "
		 * + "case when s.sfaxno = '' then '-' else s.sfaxno end as sfaxno ," +
		 * "case when s.stelephone = '' then '-' else s.stelephone end as stelephone," +
		 * "case when s.swardname = '' then '-' else s.swardname end as swardname," +
		 * "case when s.ssampletransport = '' then '-' else s.ssampletransport end as ssampletransport ,"
		 * +
		 * "case when s.sreportrequirement = '' then '-' else s.sreportrequirement end as sreportrequirement ,"
		 * +
		 * "s.ntransactionstatus,ic.sinstitutioncatname,i.sinstitutionname,ins.sinstitutionsitename, "
		 * +
		 * "ind.sinstitutiondeptname,ind.sinstitutiondeptcode,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
		 * +userInfo.getSlanguagetypecode()+"')as stransdisplaystatus " +
		 * "from submitter  s " +
		 * "join institutioncategory ic on ic.ninstitutioncatcode =s.ninstitutioncatcode "
		 * + "join institution i on i.ninstitutioncode =s.ninstitutioncode " +
		 * "join institutionsite ins on ins.ninstitutionsitecode =s.ninstitutionsitecode "
		 * +
		 * "join institutiondepartment ind on ind.ninstitutiondeptcode=s.ninstitutiondeptcode "
		 * + "join transactionstatus ts on ts.ntranscode =s.ntransactionstatus " +
		 * "where s.ninstitutioncatcode ="+inputMap.get("ninstitutioncatcode")
		 * +" and s.ninstitutioncode="+inputMap.get("ninstitutioncode")
		 * +" and s.ninstitutionsitecode="+inputMap.get("ninstitutionsitecode")+" " +
		 * "and s.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		 * +" and ic.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus
		 * ()+" " +
		 * "and i.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		 * +" and ins.nstatus="+Enumeration.TransactionStatus.ACTIVE.
		 * gettransactionstatus()+" " +
		 * "and ind.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(
		 * )+" and ts.nstatus="+Enumeration.TransactionStatus.ACTIVE.
		 * gettransactionstatus()+" " + "and s.nsitecode="+userInfo.getNmastersitecode()
		 * +" order by s.dcreateddate desc"; List<Submitter> lstSubmitter =
		 * jdbcTemplate.query(strQuery, new Submitter());
		 * 
		 * outputMap.put("Submitter", lstSubmitter); if(!lstSubmitter.isEmpty()) {
		 * outputMap.put("selectedSubmitter", lstSubmitter.get(0));
		 * 
		 * }else { outputMap.put("selectedSubmitter", null); }
		 */

		final String strQuery = "select s.ssubmittercode,s.sfirstname||' '||s.slastname as ssubmittername, "
				+ "s.ninstitutiondeptcode,s.sfirstname,s.slastname,s.ssubmitterid, "
				+ "s.sshortname,s.smobileno,s.semail,  "
				+ "case when s.sspecialization = '' then '-' else s.sspecialization end as sspecialization , "
				+ "case when s.sfaxno = '' then '-' else s.sfaxno end as sfaxno ,"
				+ "case when s.stelephone = '' then '-' else s.stelephone end as stelephone,"
				+ "case when s.swardname = '' then '-' else s.swardname end as swardname,"
				+ "case when s.ssampletransport = '' then '-' else s.ssampletransport end as ssampletransport ,"
				+ "case when s.sreportrequirement = '' then '-' else s.sreportrequirement end as sreportrequirement ,"
				+ "s.ntransactionstatus,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "')as stransdisplaystatus,  "
				+ "ind.sinstitutiondeptname,ind.sinstitutiondeptcode "
				+ "from submitter s ,institutiondepartment ind,transactionstatus ts where  "
				+ "ts.ntranscode =s.ntransactionstatus  "
				+ "and ind.ninstitutiondeptcode=s.ninstitutiondeptcode and ind.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  s.nsitecode="
				+ userInfo.getNmastersitecode() + " and s.ssubmittercode > '0' order by s.dcreateddate desc";

		final List<Submitter> lstSubmitter = jdbcTemplate.query(strQuery, new Submitter());
		outputMap.put("Submitter", lstSubmitter);

		if (!lstSubmitter.isEmpty() & lstSubmitter.size() > 0) {
			final String ssubmittercode = lstSubmitter.get(0).getSsubmittercode();
			final List<SubmitterMapping> listSubmitterMapping = getSubmitterMapping(ssubmittercode, userInfo);
			outputMap.put("selectedSubmitter", lstSubmitter.get(0));
			outputMap.put("SubmitterMapping", listSubmitterMapping);
		} else {
			outputMap.put("selectedSubmitter", null);
			outputMap.put("SubmitterMapping", null);
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getActiveSubmitterByFilter(final Map<String, Object> inputMap,
			final UserInfo userInfo) {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final String strQuery = "select s.ssubmittercode,s.ninstitutioncatcode,s.ninstitutioncode,s.ninstitutionsitecode,"
				+ "s.ninstitutiondeptcode,s.sfirstname||' '||s.slastname as ssubmittername,s.sfirstname,s.slastname,s.ssubmitterid,"
				+ "s.sshortname,s.smobileno,s.semail, "
				+ "case when s.sspecialization = '' then '-' else s.sspecialization end as sspecialization , "
				+ "case when s.sfaxno = '' then '-' else s.sfaxno end as sfaxno ,"
				+ "case when s.stelephone = '' then '-' else s.stelephone end as stelephone,"
				+ "case when s.swardname = '' then '-' else s.swardname end as swardname,"
				+ "case when s.ssampletransport = '' then '-' else s.ssampletransport end as ssampletransport ,"
				+ "case when s.sreportrequirement = '' then '-' else s.sreportrequirement end as sreportrequirement ,"
				+ "s.ntransactionstatus,ic.sinstitutioncatname,i.sinstitutionname,ins.sinstitutionsitename, "
				+ "ind.sinstitutiondeptname,ind.sinstitutiondeptcode,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "')as stransdisplaystatus from submitter s "
				+ "join institutioncategory ic on ic.ninstitutioncatcode =s.ninstitutioncatcode "
				+ "join institution i on i.ninstitutioncode =s.ninstitutioncode "
				+ "join institutionsite ins on ins.ninstitutionsitecode =s.ninstitutionsitecode "
				+ "join institutiondepartment ind on ind.ninstitutiondeptcode=s.ninstitutiondeptcode "
				+ "join transactionstatus ts on ts.ntranscode =s.ntransactionstatus where s.ninstitutionsitecode="
				+ inputMap.get("ninstitutionsitecode") + " and s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ic.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and i.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ins.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ind.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.ntransactionstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nsitecode="
				+ userInfo.getNmastersitecode() + " and s.nsitecode=ic.nsitecode and ic.nsitecode=i.nsitecode"
				+ " and i.nsitecode=ind.nsitecode and ind.nsitecode=ins.nsitecode order by s.dcreateddate desc";
		final List<Submitter> lstSubmitter = jdbcTemplate.query(strQuery, new Submitter());
		outputMap.put("Submitter", lstSubmitter);
		if (!lstSubmitter.isEmpty()) {
			outputMap.put("selectedSubmitter", lstSubmitter.get(0));
		} else {
			outputMap.put("selectedSubmitter", null);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public Submitter getActiveSubmitterById(final String ssubmitterCode, final UserInfo userInfo) throws Exception {
		final String strQuery = "select s.ssubmittercode,s.sfirstname||' '||s.slastname as ssubmittername, "
				+ "s.ninstitutiondeptcode,s.sfirstname,s.slastname,s.ssubmitterid,s.sshortname,s.smobileno,s.semail,"
				+ " s.sspecialization  as sspecialization , s.sfaxno as sfaxno , s.stelephone as stelephone, s.swardname as swardname,"
				+ " s.ssampletransport as ssampletransport , s.sreportrequirement as sreportrequirement ,"
				+ "s.ntransactionstatus,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "')as stransdisplaystatus, "
				+ "ind.sinstitutiondeptname,ind.sinstitutiondeptcode from submitter s ,institutiondepartment ind,transactionstatus ts "
				+ "where ts.ntranscode =s.ntransactionstatus and ind.ninstitutiondeptcode=s.ninstitutiondeptcode and ind.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.ssubmittercode='"
				+ ssubmitterCode + "'";
		return (Submitter) jdbcUtilityFunction.queryForObject(strQuery, Submitter.class, jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> createSubmitter(final Submitter objSubmitter, final UserInfo userInfo)
			throws Exception {
		final String sQuery = " lock  table locksubmitter " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final Map<String, Object> returnMap = saveSubmitter(objSubmitter, userInfo);
		final String response = (String) returnMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
		if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(response)) {
			return getSubmitterData(objSubmitter, userInfo, null);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	private ResponseEntity<Object> getSubmitterData(final Submitter objSubmitter, final UserInfo userInfo,
			final String ssumbittercode) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		Submitter selectedSubmitter = null;
		if (ssumbittercode == null) {

			/*
			 * String strQuery ="select s.ssubmittercode,s.ninstitutioncatcode," +
			 * "s.ninstitutioncode,s.ninstitutionsitecode,s.ninstitutiondeptcode," +
			 * "s.sfirstname||' '||s.slastname as ssubmittername,s.sfirstname,s.slastname,s.ssubmitterid,s.sshortname,"
			 * + "s.smobileno,s.semail," +
			 * "case when s.sspecialization = '' then '-' else s.sspecialization end as sspecialization , "
			 * + "case when s.sfaxno = '' then '-' else s.sfaxno end as sfaxno ," +
			 * "case when s.stelephone = '' then '-' else s.stelephone end as stelephone," +
			 * "case when s.swardname = '' then '-' else s.swardname end as swardname," +
			 * "case when s.ssampletransport = '' then '-' else s.ssampletransport end as ssampletransport ,"
			 * +
			 * "case when s.sreportrequirement = '' then '-' else s.sreportrequirement end as sreportrequirement ,"
			 * +
			 * "s.ntransactionstatus,ic.sinstitutioncatname,i.sinstitutionname,ins.sinstitutionsitename,s.dcreateddate, "
			 * +
			 * "ind.sinstitutiondeptname,ind.sinstitutiondeptcode,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
			 * +userInfo.getSlanguagetypecode()+"')as stransdisplaystatus " +
			 * "from submitter  s " +
			 * "join institutioncategory ic on ic.ninstitutioncatcode =s.ninstitutioncatcode "
			 * + "join institution i on i.ninstitutioncode =s.ninstitutioncode " +
			 * "join institutionsite ins on ins.ninstitutionsitecode =s.ninstitutionsitecode "
			 * +
			 * "join institutiondepartment ind on ind.ninstitutiondeptcode=s.ninstitutiondeptcode "
			 * + "join transactionstatus ts on ts.ntranscode =s.ntransactionstatus " +
			 * "where s.ninstitutioncatcode="+objSubmitter.getNinstitutioncatcode()
			 * +" and s.ninstitutioncode="+objSubmitter.getNinstitutioncode()
			 * +" and s.ninstitutionsitecode="+objSubmitter.getNinstitutionsitecode()
			 * +" and " +
			 * "s.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
			 * +" and ic.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus
			 * ()+" " +
			 * "and i.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
			 * +" and ins.nstatus="+Enumeration.TransactionStatus.ACTIVE.
			 * gettransactionstatus()+" " +
			 * "and ind.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(
			 * )+" and ts.nstatus="+Enumeration.TransactionStatus.ACTIVE.
			 * gettransactionstatus()+" " +
			 * "and s.nsitecode="+userInfo.getNmastersitecode()+" order by s.dcreateddate ";
			 * List<Submitter> lstSubmitter = jdbcTemplate.query(strQuery, new Submitter());
			 * 
			 * outputMap.put("Submitter", lstSubmitter); if(!lstSubmitter.isEmpty()) {
			 * outputMap.put("selectedSubmitter",lstSubmitter.get(lstSubmitter.size()-1));
			 * }else { outputMap.put("selectedSubmitter", null);
			 * 
			 * }
			 */

			final String strQuery = "select s.ssubmittercode,s.sfirstname||' '||s.slastname as ssubmittername, "
					+ "s.ninstitutiondeptcode,s.sfirstname,s.slastname,s.ssubmitterid, "
					+ "s.sshortname,s.smobileno,s.semail,"
					+ "case when s.sspecialization = '' then '-' else s.sspecialization end as sspecialization , "
					+ "case when s.sfaxno = '' then '-' else s.sfaxno end as sfaxno ,"
					+ "case when s.stelephone = '' then '-' else s.stelephone end as stelephone,"
					+ "case when s.swardname = '' then '-' else s.swardname end as swardname,"
					+ "case when s.ssampletransport = '' then '-' else s.ssampletransport end as ssampletransport ,"
					+ "case when s.sreportrequirement = '' then '-' else s.sreportrequirement end as sreportrequirement ,"
					+ "s.ntransactionstatus,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
					+ userInfo.getSlanguagetypecode() + "')as stransdisplaystatus,  "
					+ "ind.sinstitutiondeptname,ind.sinstitutiondeptcode "
					+ "from submitter s ,institutiondepartment ind,transactionstatus ts where  "
					+ "ts.ntranscode =s.ntransactionstatus  "
					+ "and ind.ninstitutiondeptcode=s.ninstitutiondeptcode and ind.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nsitecode="
					+ userInfo.getNmastersitecode() + " and s.ssubmittercode > '0' order by s.dcreateddate desc";

			final List<Submitter> lstSubmitter = jdbcTemplate.query(strQuery, new Submitter());
			outputMap.put("Submitter", lstSubmitter);
			if (!lstSubmitter.isEmpty() & lstSubmitter.size() > 0) {
				final String ssubmittercode = lstSubmitter.get(0).getSsubmittercode();
				final List<SubmitterMapping> listSubmitterMapping = getSubmitterMapping(ssubmittercode, userInfo);
				outputMap.put("selectedSubmitter", lstSubmitter.get(0));
				outputMap.put("SubmitterMapping", listSubmitterMapping);
			} else {
				outputMap.put("selectedSubmitter", null);
				outputMap.put("SubmitterMapping", null);
			}
		} else {
			selectedSubmitter = getActiveSubmitterById(ssumbittercode, userInfo);
			outputMap.put("selectedSubmitter", selectedSubmitter);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getInstitutionDepartment(final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final String strQuery = "select * from institutiondepartment where ninstitutiondeptcode>0 and nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and nsitecode="
				+ userInfo.getNmastersitecode() + " ";
		final List<InstitutionDepartment> lstInstitutionDepartment = jdbcTemplate.query(strQuery,
				new InstitutionDepartment());
		outputMap.put("InstitutionDepartment", lstInstitutionDepartment);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public Map<String, Object> saveSubmitter(final Submitter objSubmitter, final UserInfo userInfo) throws Exception {
		final List<Object> submitterDetails = new ArrayList<>();
		final Map<String, Object> returnMap = new HashMap<String, Object>();
		final String firstName = objSubmitter.getSfirstname();
		final String lastName = objSubmitter.getSlastname();
		final String shortName = objSubmitter.getSshortname();
		final String mobileNo = objSubmitter.getSmobileno();
		final String email = objSubmitter.getSemail();
		final String concat = firstName + lastName + shortName + mobileNo + email;
		objSubmitter.setSsubmittercode(UUID.nameUUIDFromBytes(concat.getBytes()).toString());
		final String strformat = projectDAOSupport.getSeqfnFormat("submitter", "seqnoformatgeneratorsubmitter", 0, 0,
				userInfo);
		objSubmitter.setSsubmitterid(strformat);

		final List<Submitter> duplicateSubmitter = jdbcTemplate.query(
				"select * from submitter where nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsitecode=" + userInfo.getNmastersitecode() + " and  sfirstname=N'"
						+ stringUtilityFunction.replaceQuote(objSubmitter.getSfirstname()) + "' and slastname=N'"
						+ stringUtilityFunction.replaceQuote(objSubmitter.getSlastname()) + "' and sshortname=N'"
						+ stringUtilityFunction.replaceQuote(objSubmitter.getSshortname()) + "' and smobileno=N'"
						+ stringUtilityFunction.replaceQuote(objSubmitter.getSmobileno()) + "' and semail=N'"
						+ stringUtilityFunction.replaceQuote(objSubmitter.getSemail()) + "' ",
				new Submitter());

		if (duplicateSubmitter.isEmpty()) {

			final String deleteSubmitterQry = "select ssubmittercode from submitter where ssubmittercode ='"
					+ objSubmitter.getSsubmittercode() + "' and nstatus ="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "";
			final Submitter deleteSubmitter = (Submitter) jdbcUtilityFunction.queryForObject(deleteSubmitterQry,
					Submitter.class, jdbcTemplate);

			if (deleteSubmitter == null) {
				final String submitterSeq = "select nsequenceno from seqnosubmittermanagement where stablename='submitter' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				int seqNo = jdbcTemplate.queryForObject(submitterSeq, Integer.class);
				seqNo = seqNo + 1;

				final String submitterInsert = " insert into submitter(ssubmittercode,ninstitutioncatcode,ninstitutioncode,ninstitutionsitecode,ninstitutiondeptcode,sfirstname,slastname,"
						+ " ssubmitterid,sshortname,swardname,stelephone,smobileno,sfaxno,semail,sspecialization,sreportrequirement,ssampletransport, "
						+ " ntransactionstatus,dcreateddate,ntzcreateddate,noffsetdcreateddate, dmodifieddate,nsitecode,nstatus)"
						+ " values(N'" + objSubmitter.getSsubmittercode() + "',-1,-1,-1," + " "
						+ objSubmitter.getNinstitutiondeptcode() + ",N'"
						+ stringUtilityFunction.replaceQuote(objSubmitter.getSfirstname()) + "',N'"
						+ stringUtilityFunction.replaceQuote(objSubmitter.getSlastname()) + "',N'"
						+ stringUtilityFunction.replaceQuote(objSubmitter.getSsubmitterid()) + "',N'"
						+ stringUtilityFunction.replaceQuote(objSubmitter.getSshortname()) + "'," + "N'"
						+ stringUtilityFunction.replaceQuote(objSubmitter.getSwardname()) + "',N'"
						+ stringUtilityFunction.replaceQuote(objSubmitter.getStelephone()) + "',N'"
						+ stringUtilityFunction.replaceQuote(objSubmitter.getSmobileno()) + "',N'"
						+ stringUtilityFunction.replaceQuote(objSubmitter.getSfaxno()) + "'," + "N'"
						+ stringUtilityFunction.replaceQuote(objSubmitter.getSemail()) + "',N'"
						+ stringUtilityFunction.replaceQuote(objSubmitter.getSspecialization()) + "',N'"
						+ stringUtilityFunction.replaceQuote(objSubmitter.getSreportrequirement()) + "',N'"
						+ stringUtilityFunction.replaceQuote(objSubmitter.getSsampletransport()) + "'," + " "
						+ objSubmitter.getNtransactionstatus() + "," + " '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNtimezonecode() + ","
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", " + " '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNmastersitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ) ";

				jdbcTemplate.execute(submitterInsert);

				final String patientSeqnoUpdate = "update seqnosubmittermanagement set nsequenceno=" + seqNo
						+ " where stablename='submitter'";
				jdbcTemplate.execute(patientSeqnoUpdate);

				returnMap.put("Submitter", objSubmitter);
				submitterDetails.add(objSubmitter);
				returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
			} else {
				final String updatQuery = "update submitter set ninstitutiondeptcode="
						+ objSubmitter.getNinstitutiondeptcode() + ",sfirstname='" + objSubmitter.getSfirstname()
						+ "',slastname='" + objSubmitter.getSlastname() + "',sshortname='"
						+ objSubmitter.getSshortname() + "',ssubmitterid='" + objSubmitter.getSsubmitterid()
						+ "',swardname='" + objSubmitter.getSwardname() + "'," + "stelephone='"
						+ objSubmitter.getStelephone() + "',smobileno='" + objSubmitter.getSmobileno() + "',sfaxno='"
						+ objSubmitter.getSfaxno() + "',semail='" + objSubmitter.getSemail() + "', "
						+ "sspecialization='" + objSubmitter.getSspecialization() + "',sreportrequirement='"
						+ objSubmitter.getSreportrequirement() + "',ssampletransport='"
						+ objSubmitter.getSsampletransport() + "',ntransactionstatus="
						+ objSubmitter.getNtransactionstatus() + ", " + "dcreateddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',ntzcreateddate="
						+ userInfo.getNtimezonecode() + ",noffsetdcreateddate="
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ "dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "	where ssubmittercode ='"
						+ objSubmitter.getSsubmittercode() + "' ";

				jdbcTemplate.execute(updatQuery);

				returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
			}

			auditUtilityFunction.fnInsertAuditAction(submitterDetails, 1, null, Arrays.asList("IDS_ADDSUBMITTER"),
					userInfo);
		} else {
			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.FAILED.getreturnstatus());
			returnMap.put("Submitter", duplicateSubmitter);
		}
		return returnMap;
	}

	@Override
	public ResponseEntity<Object> updateSubmitter(final Submitter objSubmitter, final UserInfo userInfo)
			throws Exception {
		final List<Object> beforeSavedSubmitterList = new ArrayList<>();
		final List<Object> afterSavedSubmitterList = new ArrayList<>();

		final Submitter submitterDetails = getActiveSubmitterById(objSubmitter.getSsubmittercode(), userInfo);

		if (submitterDetails == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
//		final String queryString = "select ssubmittercode from submitter where ssubmittername = '"+ objSubmitter.getSsubmittername() + "'" + " and sshortname='" + objSubmitter.getSshortname()
//								 + "' and smobileno='" + objSubmitter.getSmobileno() + "' and semail= '"+ objSubmitter.getSemail()+"' " 
//								 + " and ssubmittercode <> '"+objSubmitter.getSsubmittercode()+"' "
//								 + " and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final String queryString = "select ssubmittercode from submitter where sfirstname = '"
					+ stringUtilityFunction.replaceQuote(objSubmitter.getSfirstname()) + "' and slastname = '"
					+ stringUtilityFunction.replaceQuote(objSubmitter.getSlastname()) + "'" + " and sshortname = '"
					+ stringUtilityFunction.replaceQuote(objSubmitter.getSshortname()) + "' and  smobileno = '"
					+ stringUtilityFunction.replaceQuote(objSubmitter.getSmobileno()) + "' and semail = '"
					+ stringUtilityFunction.replaceQuote(objSubmitter.getSemail()) + "' " + " and ssubmittercode <> '"
					+ objSubmitter.getSsubmittercode() + "' and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final List<Submitter> submitterList = jdbcTemplate.query(queryString, new Submitter());

			if (submitterList.isEmpty()) {
				String concat = "";
				String ssubmittercode = "";
				String submittercode = "";
				if (!objSubmitter.getSfirstname().toLowerCase().equals(submitterDetails.getSfirstname().toLowerCase())
						|| !objSubmitter.getSlastname().toLowerCase()
								.equals(submitterDetails.getSlastname().toLowerCase())
						|| !objSubmitter.getSshortname().toLowerCase()
								.equals(submitterDetails.getSshortname().toLowerCase())
						|| !objSubmitter.getSmobileno().equals(submitterDetails.getSmobileno())
						|| !objSubmitter.getSemail().toLowerCase().equals(submitterDetails.getSemail().toLowerCase())) {
					concat = objSubmitter.getSfirstname() + objSubmitter.getSlastname() + objSubmitter.getSshortname()
							+ objSubmitter.getSmobileno() + objSubmitter.getSemail();
					submittercode = UUID.nameUUIDFromBytes(concat.getBytes()).toString();
					ssubmittercode = ",ssubmittercode = '" + submittercode + "'";
				}

				final String query = "update submitter set sfirstname =N'"
						+ stringUtilityFunction.replaceQuote(objSubmitter.getSfirstname()) + "',slastname =N'"
						+ stringUtilityFunction.replaceQuote(objSubmitter.getSlastname()) + "',sshortname= N'"
						+ stringUtilityFunction.replaceQuote(objSubmitter.getSshortname()) + "'," + "swardname=N'"
						+ stringUtilityFunction.replaceQuote(objSubmitter.getSwardname()) + "',stelephone=N'"
						+ stringUtilityFunction.replaceQuote(objSubmitter.getStelephone()) + "',smobileno=N'"
						+ stringUtilityFunction.replaceQuote(objSubmitter.getSmobileno()) + "'" + ",sfaxno=N'"
						+ stringUtilityFunction.replaceQuote(objSubmitter.getSfaxno()) + "',semail=N'"
						+ stringUtilityFunction.replaceQuote(objSubmitter.getSemail()) + "',sspecialization=N'"
						+ stringUtilityFunction.replaceQuote(objSubmitter.getSspecialization()) + "',"
						+ "sreportrequirement=N'"
						+ stringUtilityFunction.replaceQuote(objSubmitter.getSreportrequirement())
						+ "',ssampletransport=N'"
						+ stringUtilityFunction.replaceQuote(objSubmitter.getSsampletransport())
						+ "',ntransactionstatus=" + objSubmitter.getNtransactionstatus() + " " + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' " + ssubmittercode
						+ ", ninstitutiondeptcode = " + objSubmitter.getNinstitutiondeptcode() + ""
						+ " where ssubmittercode='" + objSubmitter.getSsubmittercode() + "' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
				jdbcTemplate.execute(query);
				afterSavedSubmitterList.add(objSubmitter);
				beforeSavedSubmitterList.add(submitterDetails);
				auditUtilityFunction.fnInsertAuditAction(afterSavedSubmitterList, 2, beforeSavedSubmitterList,
						Arrays.asList("IDS_EDITSUBMITTER"), userInfo);
				return getSubmitterData(objSubmitter, userInfo,
						ssubmittercode.equals("") ? objSubmitter.getSsubmittercode() : submittercode);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("ALREADYEXIST", userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	@Override
	public ResponseEntity<Object> deleteSubmitter(final Submitter objSubmitter, final UserInfo userInfo)
			throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> deletedSubmitterList = new ArrayList<>();
		final List<Object> deletedSubmitterMappingList = new ArrayList<>();
		final Submitter submitter = getActiveSubmitterById(objSubmitter.getSsubmittercode(), userInfo);

		if (submitter == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else if (submitter.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CANNOTDELETERETIREDSUBMITTER",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} else {
			boolean validRecord;

			final Map<String, Object> objOneToManyValidation = new HashMap<String, Object>();
			objOneToManyValidation.put("primaryKeyValue", objSubmitter.getSsubmittercode());
			objOneToManyValidation.put("stablename", "submitter");

			validatorDel = projectDAOSupport.validateOneToManyDeletion(objOneToManyValidation, userInfo);

			if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
			} else {
				validRecord = false;
			}

			if (validRecord) {
				final String updateQueryString = "update submitter set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ssubmittercode='"
						+ stringUtilityFunction.replaceQuote(objSubmitter.getSsubmittercode()) + "' ";

				jdbcTemplate.execute(updateQueryString);
				objSubmitter.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				deletedSubmitterList.add(objSubmitter);
				multilingualIDList.add("IDS_DELETESUBMITTER");
				auditUtilityFunction.fnInsertAuditAction(deletedSubmitterList, 1, null, multilingualIDList, userInfo);

				final String qry = "select sm.*,s.ssubmitterid from submitter s,submittermapping sm where s.ssubmittercode=sm.ssubmittercode and s.nstatus=-1 and sm.nstatus=1 and s.ssubmittercode='"
						+ stringUtilityFunction.replaceQuote(objSubmitter.getSsubmittercode()) + "'";
				final List<SubmitterMapping> lstInstitutionSite = jdbcTemplate.query(qry, new SubmitterMapping());

				final String updateQry = "update submittermapping set dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where ssubmittercode= '"
						+ stringUtilityFunction.replaceQuote(objSubmitter.getSsubmittercode()) + "'";
				jdbcTemplate.execute(updateQry);

				deletedSubmitterMappingList.add(lstInstitutionSite);
				auditUtilityFunction.fnInsertListAuditAction(deletedSubmitterMappingList, 1, null,
						Arrays.asList("IDS_DELETESITE"), userInfo);

				return getSelectedSubmitterDetail(userInfo, null, objSubmitter);
			} else {
				return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}

		}

	}

	@Override
	public ResponseEntity<Object> retireSubmitter(final Submitter objSubmitter, final UserInfo userInfo)
			throws Exception {
		final List<Object> beforeSavedSubmitterList = new ArrayList<>();
		final List<Object> afterSavedSubmitterList = new ArrayList<>();

		final Submitter submitterDetails = getActiveSubmitterById(objSubmitter.getSsubmittercode(), userInfo);

		if (submitterDetails == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String query = "update submitter set ntransactionstatus="
					+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + ", dmodifieddate ='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ssubmittercode='"
					+ objSubmitter.getSsubmittercode() + "' ";
			jdbcTemplate.execute(query);
			objSubmitter.setNtransactionstatus((short) Enumeration.TransactionStatus.RETIRED.gettransactionstatus());
			afterSavedSubmitterList.add(objSubmitter);
			beforeSavedSubmitterList.add(submitterDetails);
			auditUtilityFunction.fnInsertAuditAction(afterSavedSubmitterList, 2, beforeSavedSubmitterList,
					Arrays.asList("IDS_RETIRESUBMITTER"), userInfo);
			return getSubmitterData(objSubmitter, userInfo, null);
		}
	}

	@Override
	public ResponseEntity<Object> getSelectedSubmitterDetail(final UserInfo userInfo, final String ssubmitterCode,
			final Submitter objSubmitter) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
//	Submitter lstSubmitter=getActiveSubmitterById(ssubmitterCode, userInfo);
//	objMap.put("selectedSubmitter", lstSubmitter);
//	return new ResponseEntity<Object>(objMap,HttpStatus.OK);	

		/*
		 * final Map<String, Object> outputMap = new HashMap<String, Object>();
		 * Submitter selectedSubmitter = null; if(ssubmitterCode==null){ String strQuery
		 * ="select s.ssubmittercode,s.ninstitutioncatcode," +
		 * "s.ninstitutioncode,s.ninstitutionsitecode,s.ninstitutiondeptcode," +
		 * "s.sfirstname,s.slastname,s.sfirstname||' '||s.slastname as ssubmittername,s.ssubmitterid,s.sshortname,"
		 * + "s.smobileno,s.semail," +
		 * "case when s.sspecialization = '' then '-' else s.sspecialization end as sspecialization , "
		 * + "case when s.sfaxno = '' then '-' else s.sfaxno end as sfaxno ," +
		 * "case when s.stelephone = '' then '-' else s.stelephone end as stelephone," +
		 * "case when s.swardname = '' then '-' else s.swardname end as swardname," +
		 * "case when s.ssampletransport = '' then '-' else s.ssampletransport end as ssampletransport ,"
		 * +
		 * "case when s.sreportrequirement = '' then '-' else s.sreportrequirement end as sreportrequirement ,"
		 * +
		 * "s.ntransactionstatus,ic.sinstitutioncatname,i.sinstitutionname,ins.sinstitutionsitename,s.dcreateddate, "
		 * +
		 * "ind.sinstitutiondeptname,ind.sinstitutiondeptcode,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
		 * +userInfo.getSlanguagetypecode()+"')as stransdisplaystatus " +
		 * "from submitter  s " +
		 * "join institutioncategory ic on ic.ninstitutioncatcode =s.ninstitutioncatcode "
		 * + "join institution i on i.ninstitutioncode =s.ninstitutioncode " +
		 * "join institutionsite ins on ins.ninstitutionsitecode =s.ninstitutionsitecode "
		 * +
		 * "join institutiondepartment ind on ind.ninstitutiondeptcode=s.ninstitutiondeptcode "
		 * + "join transactionstatus ts on ts.ntranscode =s.ntransactionstatus " +
		 * "where s.ninstitutioncatcode="+objSubmitter.getNinstitutioncatcode()
		 * +" and s.ninstitutioncode="+objSubmitter.getNinstitutioncode()
		 * +" and s.ninstitutionsitecode="+objSubmitter.getNinstitutionsitecode()
		 * +" and " +
		 * "s.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		 * +" and ic.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus
		 * ()+" " +
		 * "and i.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		 * +" and ins.nstatus="+Enumeration.TransactionStatus.ACTIVE.
		 * gettransactionstatus()+" " +
		 * "and ind.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(
		 * )+" and ts.nstatus="+Enumeration.TransactionStatus.ACTIVE.
		 * gettransactionstatus()+" " + "and s.nsitecode="+userInfo.getNmastersitecode()
		 * +" order by s.ssubmitterCode "; List<Submitter> lstSubmitter =
		 * jdbcTemplate.query(strQuery, new Submitter());
		 * 
		 * outputMap.put("Submitter", lstSubmitter); if(!lstSubmitter.isEmpty()) {
		 * outputMap.put("selectedSubmitter",lstSubmitter.get(lstSubmitter.size()-1));
		 * }else { outputMap.put("selectedSubmitter", null);
		 * 
		 * } }else { selectedSubmitter=getActiveSubmitterById(ssubmitterCode, userInfo);
		 * outputMap.put("selectedSubmitter", selectedSubmitter); }
		 */

		// ALPD-5236--Added by Vignesh R(21-01-2025)->Submitter->Multi tab delete-->when
		// select the list,alert is not thrown
		boolean check = false;

		if (ssubmitterCode != null) {

			final SubmitterMapping lstSubmitterMapping = checKSubmitterIsPresent(ssubmitterCode);
			if (lstSubmitterMapping == null) {
				check = true;
			}
		}

		if (!check) {
			if (ssubmitterCode == null) {
				final String strQuery = "select s.ssubmittercode,s.sfirstname||' '||s.slastname as ssubmittername, "
						+ "s.ninstitutiondeptcode,s.sfirstname,s.slastname,s.ssubmitterid, "
						+ "s.sshortname,s.smobileno,s.semail, "
						+ "case when s.sspecialization = '' then '-' else s.sspecialization end as sspecialization , "
						+ "case when s.sfaxno = '' then '-' else s.sfaxno end as sfaxno ,"
						+ "case when s.stelephone = '' then '-' else s.stelephone end as stelephone,"
						+ "case when s.swardname = '' then '-' else s.swardname end as swardname,"
						+ "case when s.ssampletransport = '' then '-' else s.ssampletransport end as ssampletransport ,"
						+ "case when s.sreportrequirement = '' then '-' else s.sreportrequirement end as sreportrequirement ,"
						+ "s.ntransactionstatus,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
						+ userInfo.getSlanguagetypecode() + "')as stransdisplaystatus,  "
						+ "ind.sinstitutiondeptname,ind.sinstitutiondeptcode "
						+ "from submitter s ,institutiondepartment ind,transactionstatus ts where  "
						+ "ts.ntranscode =s.ntransactionstatus  "
						+ "and ind.ninstitutiondeptcode=s.ninstitutiondeptcode and ind.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  s.nsitecode="
						+ userInfo.getNmastersitecode() + " and s.ssubmittercode > '0' order by s.dcreateddate desc";

				final List<Submitter> lstSubmitter = jdbcTemplate.query(strQuery, new Submitter());
				outputMap.put("Submitter", lstSubmitter);

				if (!lstSubmitter.isEmpty() & lstSubmitter.size() > 0) {

					final String ssubmittercode = lstSubmitter.get(0).getSsubmittercode();
					final List<SubmitterMapping> listSubmitterMapping = getSubmitterMapping(ssubmittercode, userInfo);
					outputMap.put("selectedSubmitter", lstSubmitter.get(0));
					outputMap.put("SubmitterMapping", listSubmitterMapping);

				} else {
					outputMap.put("selectedSubmitter", null);
					outputMap.put("SubmitterMapping", null);
				}
			} else {

				final String strQuery = "select s.ssubmittercode,s.sfirstname||' '||s.slastname as ssubmittername, "
						+ "s.ninstitutiondeptcode,s.sfirstname,s.slastname,s.ssubmitterid, "
						+ "s.sshortname, s.smobileno,s.semail,"
						+ "case when s.sspecialization = '' then '-' else s.sspecialization end as sspecialization , "
						+ "case when s.sfaxno = '' then '-' else s.sfaxno end as sfaxno ,"
						+ "case when s.stelephone = '' then '-' else s.stelephone end as stelephone,"
						+ "case when s.swardname = '' then '-' else s.swardname end as swardname,"
						+ "case when s.ssampletransport = '' then '-' else s.ssampletransport end as ssampletransport ,"
						+ "case when s.sreportrequirement = '' then '-' else s.sreportrequirement end as sreportrequirement ,"
						+ "s.ntransactionstatus,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
						+ userInfo.getSlanguagetypecode() + "')as stransdisplaystatus, "
						+ "ind.sinstitutiondeptname,ind.sinstitutiondeptcode "
						+ "from submitter s ,institutiondepartment ind,transactionstatus ts where  "
						+ "ts.ntranscode =s.ntransactionstatus and ind.ninstitutiondeptcode=s.ninstitutiondeptcode and ind.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nsitecode="
						+ userInfo.getNmastersitecode() + " and s.nsitecode=ind.nsitecode and s.ssubmittercode='"
						+ ssubmitterCode + "'";

				final List<Submitter> lstSubmitter = jdbcTemplate.query(strQuery, new Submitter());
				final List<SubmitterMapping> listSubmitterMapping = getSubmitterMapping(ssubmitterCode, userInfo);
				outputMap.put("SubmitterMapping", listSubmitterMapping);
				if (!lstSubmitter.isEmpty()) {
					outputMap.put("selectedSubmitter", lstSubmitter.get(0));
				} else {
					outputMap.put("selectedSubmitter", null);
				}
			}
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SUBMITTERALREADYDELETED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

//commented by sonia on 05-09-2022 
	/*
	 * private String getSubmitterfnFormat (final int nSequenceno,final String
	 * sformatType,final Instant dresetDate,final UserInfo userInfo,final int
	 * nperiodCode)throws Exception{ Map<String, Object> map = new HashMap<>(); map=
	 * CheckSequenceNo(nSequenceno,dresetDate,userInfo,nperiodCode); int nseqno =
	 * (int) map.get("sequenceno"); String seqFormat= sformatType;
	 * 
	 * if (seqFormat != null) { while (seqFormat.contains("{")) { int start =
	 * seqFormat.indexOf('{'); int end = seqFormat.indexOf('}');
	 * 
	 * String subString = seqFormat.substring(start + 1, end); if
	 * (subString.equals("yy") || subString.equals("YY")) { SimpleDateFormat sdf =
	 * new SimpleDateFormat("yy", Locale.getDefault()); sdf.toPattern(); Date date =
	 * new Date(); String replaceString = sdf.format(date); seqFormat =
	 * seqFormat.replace('{' + subString + '}', replaceString); } else if
	 * (subString.equals("yyyy") || subString.equals("YYYY")) { SimpleDateFormat sdf
	 * = new SimpleDateFormat("yyyy", Locale.getDefault()); sdf.toPattern(); Date
	 * date = new Date(); String replaceString = sdf.format(date); seqFormat =
	 * seqFormat.replace('{' + subString + '}', replaceString); } else if
	 * (subString.equals("mm") || subString.equals("MM")) { SimpleDateFormat sdf =
	 * new SimpleDateFormat("MM", Locale.getDefault()); sdf.toPattern(); Date date =
	 * new Date(); String replaceString = sdf.format(date); seqFormat =
	 * seqFormat.replace('{' + subString + '}', replaceString);
	 * 
	 * int Y = 0; } else if (subString.equals("mmm") || subString.equals("MMM")) {
	 * SimpleDateFormat sdf = new SimpleDateFormat("MMM", Locale.getDefault());
	 * sdf.toPattern(); Date date = new Date(); String replaceString =
	 * sdf.format(date);
	 * 
	 * seqFormat = seqFormat.replace('{' + subString + '}', replaceString); int Y =
	 * 0; } else if (subString.equals("mon") || subString.equals("MON")) {
	 * SimpleDateFormat sdf = new SimpleDateFormat("MON", Locale.getDefault());
	 * sdf.toPattern(); Date date = new Date(); String replaceString =
	 * sdf.format(date); seqFormat = seqFormat.replace('{' + subString + '}',
	 * replaceString); } else if (subString.equals("dd") || subString.equals("DD"))
	 * { SimpleDateFormat sdf = new SimpleDateFormat("dd", Locale.getDefault());
	 * sdf.toPattern(); Date date = new Date(); String replaceString =
	 * sdf.format(date); seqFormat = seqFormat.replace('{' + subString + '}',
	 * replaceString); } else if (subString.matches("9+")) { String seqPadding =
	 * "%0" + subString.length() + "d"; String replaceString =
	 * String.format(seqPadding, nseqno); seqFormat = seqFormat.replace('{' +
	 * subString + '}', replaceString); }
	 * 
	 * else { seqFormat = seqFormat.replace('{' + subString + '}', subString); } } }
	 * nseqno++; String
	 * seqnoUpdateQuery="update seqnoformatgeneratorsubmitter set nsequenceno="
	 * +nseqno+" where nstatus="+Enumeration.TransactionStatus.ACTIVE.
	 * gettransactionstatus()+" "; jdbcTemplate.execute(seqnoUpdateQuery);
	 * 
	 * 
	 * return seqFormat; }
	 * 
	 * private Map<String,Object> CheckSequenceNo (int nSequenceno,final Instant
	 * dresetDate,final UserInfo userInfo,final int nperiodCode)throws Exception{
	 * Map<String, Object> objMap = new HashMap<String, Object>(); Instant
	 * dcurrentDate =objGeneral.getUTCDateTime(); if(dresetDate!=null) { String
	 * periodQuery="select jsondata->'speriodname'->>'"+userInfo.
	 * getSlanguagetypecode()+"' as speriodname from period where nperiodcode ="
	 * +nperiodCode+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.
	 * gettransactionstatus()+" "; periodQuery =
	 * jdbcTemplate.queryForObject(periodQuery, String.class); boolean check =
	 * false; int cal=0; String dateFormat=""; if (periodQuery.equals("Years")) {
	 * dateFormat = "YYYY"; } else if (periodQuery.equals("Month")) { dateFormat =
	 * "YYYY-M"; } else if (periodQuery.equals("Weeks")) { dateFormat = "yyyyMMdd";
	 * check = true; cal = Calendar.WEEK_OF_YEAR; } else if
	 * (periodQuery.equals("Days")) { dateFormat = "yyyyMMdd"; check = true; cal =
	 * Calendar.DAY_OF_YEAR; }
	 * 
	 * 
	 * 
	 * final LocalDateTime currentDateTime =
	 * LocalDateTime.ofInstant(dcurrentDate.truncatedTo(ChronoUnit.SECONDS),
	 * ZoneOffset.UTC); String currentDate =
	 * DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(currentDateTime);
	 * 
	 * final LocalDateTime resetDateTime =
	 * LocalDateTime.ofInstant(dresetDate.truncatedTo(ChronoUnit.SECONDS),ZoneOffset
	 * .UTC); String resetDate =
	 * DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(resetDateTime);
	 * 
	 * SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.getDefault());
	 * sdf.toPattern();
	 * 
	 * String resetFormatDate = ""; String currentFormatDate = ""; if
	 * (userInfo.getIsutcenabled()==Enumeration.TransactionStatus.YES.
	 * gettransactionstatus()) { LocalDateTime ldt =
	 * sdf.parse(currentDate).toInstant().atZone(ZoneId.of(userInfo.getStimezoneid()
	 * )).toLocalDateTime(); currentFormatDate =
	 * DateTimeFormatter.ofPattern(dateFormat).format(ldt);
	 * 
	 * LocalDateTime ldt1 =
	 * sdf.parse(resetDate).toInstant().atZone(ZoneId.of(userInfo.getStimezoneid()))
	 * .toLocalDateTime(); resetFormatDate =
	 * DateTimeFormatter.ofPattern(dateFormat).format(ldt1); } else {
	 * 
	 * LocalDateTime ldt =
	 * sdf.parse(currentDate).toInstant().atZone(ZoneId.systemDefault()).
	 * toLocalDateTime(); currentFormatDate =
	 * DateTimeFormatter.ofPattern(dateFormat).format(ldt);
	 * 
	 * LocalDateTime ldt1 =
	 * sdf.parse(resetDate).toInstant().atZone(ZoneId.systemDefault()).
	 * toLocalDateTime(); resetFormatDate =
	 * DateTimeFormatter.ofPattern(dateFormat).format(ldt1); }
	 * 
	 * 
	 * boolean bYearreset = false; if (check) { Date date2 =
	 * sdf.parse(currentFormatDate); Calendar c = Calendar.getInstance();
	 * c.setTime(date2); Date date3 = sdf.parse(resetFormatDate); Calendar c1 =
	 * Calendar.getInstance(); c1.setTime(date3); int weekandDay = c.get(cal); int
	 * weekandDay1 = c1.get(cal); if (weekandDay != weekandDay1) { bYearreset =
	 * true; } } else if (!(currentFormatDate.equals(resetFormatDate))) { bYearreset
	 * = true; } if (bYearreset) { String
	 * seqnoUpdateQuery="update seqnoformatgeneratorsubmitter set nsequenceno=1 ,dseqresetdate='"
	 * +dcurrentDate+"' where nstatus ="+Enumeration.TransactionStatus.ACTIVE.
	 * gettransactionstatus()+" "; jdbcTemplate.execute(seqnoUpdateQuery);
	 * nSequenceno = 1; objMap.put("sequenceno", nSequenceno);
	 * 
	 * 
	 * }
	 * 
	 * objMap.put("sequenceno", nSequenceno);
	 * 
	 * 
	 * }else { String
	 * seqnoUpdateQuery="update seqnoformatgeneratorsubmitter set dseqresetdate='"
	 * +dcurrentDate+"' where nstatus ="+Enumeration.TransactionStatus.ACTIVE.
	 * gettransactionstatus()+" "; jdbcTemplate.execute(seqnoUpdateQuery);
	 * nSequenceno=1; objMap.put("sequenceno", nSequenceno); }
	 * 
	 * 
	 * return objMap; }
	 */

	@Override
	public ResponseEntity<Object> getSubmitterDetailByAll(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final String strQuery = "select s.*, inscat.ninstitutioncatcode, inscat.sinstitutioncatname, ins.ninstitutioncode, ins.sinstitutionname, "
				+ "inssite.ninstitutionsitecode, inssite.sinstitutionsitename, insdept.ninstitutiondeptcode, insdept.sinstitutiondeptname, "
				+ "insdept.sinstitutiondeptcode, to_char(s.dmodifieddate, '"
				+ userInfo.getSpgsitedatetime().replace("'T'", " ") + "') as smodifieddate "
				+ " from institutioncategory inscat, institution ins, institutionsite inssite, submitter s,submittermapping sm,"
				+ " institutiondepartment insdept where inscat.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ins.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and inssite.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and insdept.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and inscat.nsitecode ="
				+ userInfo.getNmastersitecode() + " and ins.nsitecode =" + userInfo.getNmastersitecode()
				+ " and inssite.nsitecode =" + userInfo.getNmastersitecode() + " and s.nsitecode ="
				+ userInfo.getNmastersitecode() + " and insdept.nsitecode =" + userInfo.getNmastersitecode()
				+ " and sm.ssubmittercode = s.ssubmittercode and inscat.ninstitutioncatcode=ins.ninstitutioncatcode and "
				+ " ins.ninstitutioncode=inssite.ninstitutioncode and sm.ninstitutioncatcode=inscat.ninstitutioncatcode and "
				+ " sm.ninstitutioncode=ins.ninstitutioncode and sm.ninstitutionsitecode=inssite.ninstitutionsitecode "
				+ " and insdept.ninstitutiondeptcode=s.ninstitutiondeptcode and s.ntransactionstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return new ResponseEntity<>(jdbcTemplate.query(strQuery, new Submitter()), HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> createSubmitterMapping(final List<SubmitterMapping> submitterMapping,
			final UserInfo userInfo) throws Exception {

		final String sQuery = "lock  table submittermapping "
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final Map<String, Object> map = new LinkedHashMap<String, Object>();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedInstitutionSiteList = new ArrayList<>();

		final SubmitterMapping lstSubmitter = checKSubmitterIsPresent(submitterMapping.get(0).getSsubmittercode());

		if (lstSubmitter != null) {

			final List<SubmitterMapping> lstSubmitterMapping = getSubmitterMappingListBySubmitterCode(
					submitterMapping.get(0).getSsubmittercode());

			final List<SubmitterMapping> filteredList = submitterMapping.stream()
					.filter(source -> lstSubmitterMapping.stream()
							.noneMatch(check -> source.getNinstitutionsitecode() == check.getNinstitutionsitecode()))
					.collect(Collectors.toList());

			final String institutionsitecodevalue = filteredList.stream()
					.map(x -> String.valueOf(x.getNinstitutionsitecode())).collect(Collectors.joining(","));

			if (filteredList != null && filteredList.size() > 0) {
				final int nSeqNo = jdbcTemplate.queryForObject(
						"select nsequenceno from seqnosubmittermanagement where stablename='submittermapping' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
						Integer.class);

				final String Seqaudit = "(select " + nSeqNo
						+ " +rank()over(order by ninstitutionsitecode)as nsubmittermappingcode,'"
						+ submitterMapping.get(0).getSsubmittercode() + "',"
						+ submitterMapping.get(0).getNinstitutioncatcode() + " as ninstitutioncatcode, "
						+ "ninstitutioncode as ninstitutioncode,ninstitutionsitecode as ninstitutionsitecode, " + "'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " from institutionsite where ninstitutionsitecode in(" + institutionsitecodevalue + ") )";

				final List<SubmitterMapping> seqnoaudit = jdbcTemplate.query(Seqaudit, new SubmitterMapping());
				final String seqnoauditlist = seqnoaudit.stream()
						.map(object -> String.valueOf(object.getNsubmittermappingcode()))
						.collect(Collectors.joining(","));

				final String submittermappingInsert = "insert into submittermapping(nsubmittermappingcode,ssubmittercode,ninstitutioncatcode,ninstitutioncode,ninstitutionsitecode,dmodifieddate,nsitecode,nstatus)"
						+ "(select " + nSeqNo + " +rank()over(order by ninstitutionsitecode)as nsubmittermappingcode,'"
						+ submitterMapping.get(0).getSsubmittercode() + "',"
						+ submitterMapping.get(0).getNinstitutioncatcode() + ","
						+ submitterMapping.get(0).getNinstitutioncode() + ","
						+ "ninstitutionsitecode as ninstitutionsitecode" + ",'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " from  institutionsite where ninstitutionsitecode in(" + institutionsitecodevalue + ")  )";

				jdbcTemplate.execute(submittermappingInsert);
				final Integer sequnenceno = nSeqNo + filteredList.size();
				jdbcTemplate.execute("update seqnosubmittermanagement set nsequenceno = " + sequnenceno
						+ " where stablename='submittermapping'");

				final String strCategory = "select sm.*,ssubmitterid from submitter s,submittermapping sm where s.ssubmittercode=sm.ssubmittercode and s.nstatus=1 and sm.nstatus=1 and sm.nsubmittermappingcode in ("
						+ seqnoauditlist + ")";
				final List<SubmitterMapping> lstInstitutionSite = jdbcTemplate.query(strCategory,
						new SubmitterMapping());
				multilingualIDList.add("IDS_ADDSITE");
				savedInstitutionSiteList.add(lstInstitutionSite);

				auditUtilityFunction.fnInsertListAuditAction(savedInstitutionSiteList, 1, null, multilingualIDList,
						userInfo);
			}
			final List<SubmitterMapping> listSubmitterMapping = getSubmitterMapping(
					submitterMapping.get(0).getSsubmittercode(), userInfo);
			map.put("SubmitterMapping", listSubmitterMapping);
			return new ResponseEntity<Object>(map, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SUBMITTERALREADYDELETED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	private SubmitterMapping checKSubmitterIsPresent(final String ssubmittercode) throws Exception {
		final String strQuery = "select ssubmittercode from submitter where ssubmittercode = '" + ssubmittercode
				+ "' and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final SubmitterMapping objSubmitterMapping = (SubmitterMapping) jdbcUtilityFunction.queryForObject(strQuery,
				SubmitterMapping.class, jdbcTemplate);

		return objSubmitterMapping;
	}

	private List<SubmitterMapping> getSubmitterMappingListBySubmitterCode(final String ssubmittercode)
			throws Exception {
		final String strQuery = "Select * from submittermapping where ssubmittercode = '" + ssubmittercode + "'"
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return jdbcTemplate.query(strQuery, new SubmitterMapping());
	}

	private List<SubmitterMapping> getSubmitterMapping(final String ssubmittercode, final UserInfo userInfo)
			throws Exception {
		final String strQuery = "Select sm.*,ist.sinstitutionsitename,i.sinstitutionname,ic.sinstitutioncatname from submittermapping sm,institutionsite ist,institutioncategory ic,institution i  where "
				+ " sm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ic.ninstitutioncatcode =sm.ninstitutioncatcode and ic.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
				+ "i.ninstitutioncode =sm.ninstitutioncode and i.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and sm.ninstitutionsitecode = ist.ninstitutionsitecode and sm.ssubmittercode = '" + ssubmittercode
				+ "'";
		return jdbcTemplate.query(strQuery, new SubmitterMapping());
	}

	@Override
	public ResponseEntity<Object> deleteSubmitterMapping(final SubmitterMapping submitterMapping,
			final UserInfo userInfo) throws Exception {
		final SubmitterMapping lstSubmitterMapping = checKSubmitterIsPresent(submitterMapping.getSsubmittercode());
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		if (lstSubmitterMapping != null) {
			final SubmitterMapping SubmitterMappingByID = getActiveSubmitterById(
					submitterMapping.getNsubmittermappingcode(), userInfo);
			if (SubmitterMappingByID == null) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> savedInstitutionSiteList = new ArrayList<>();
				final String updateQueryString = "update submittermapping set dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nsubmittermappingcode="
						+ submitterMapping.getNsubmittermappingcode();
				final String strCategory = "select sm.*,s.ssubmitterid from submitter s,submittermapping sm where s.ssubmittercode=sm.ssubmittercode and s.nstatus=1 and sm.nstatus=1 and sm.nsubmittermappingcode="
						+ submitterMapping.getNsubmittermappingcode() + "";
				final SubmitterMapping lstInstitutionSite = (SubmitterMapping) jdbcUtilityFunction
						.queryForObject(strCategory, SubmitterMapping.class, jdbcTemplate);
				multilingualIDList.add("IDS_DELETESITE");
				savedInstitutionSiteList.add(lstInstitutionSite);

				jdbcTemplate.execute(updateQueryString);

				auditUtilityFunction.fnInsertAuditAction(savedInstitutionSiteList, 1, null, multilingualIDList,
						userInfo);
				outputMap.put("ssubmittercode", submitterMapping.getSsubmittercode());

				final List<SubmitterMapping> listSubmitterMapping = getSubmitterMapping(
						submitterMapping.getSsubmittercode(), userInfo);
				outputMap.put("SubmitterMapping", listSubmitterMapping);
				return new ResponseEntity<>(outputMap, HttpStatus.OK);
			}
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SUBMITTERALREADYDELETED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	private SubmitterMapping getActiveSubmitterById(final int nsubmittermappingcode, final UserInfo userInfo)
			throws Exception {
		final String strQuery = "select * from submittermapping  where  nsubmittermappingcode > 0 " + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsubmittermappingcode = "
				+ nsubmittermappingcode;
		return (SubmitterMapping) jdbcUtilityFunction.queryForObject(strQuery, SubmitterMapping.class, jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> getSubmitterBySubmitterCode(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final String ssubmittercode = (String) inputMap.get("ssubmittercode");
		final String strQuery = "select s.ssubmittercode,s.sfirstname||' '||s.slastname as ssubmittername, "
				+ "s.ninstitutiondeptcode,s.sfirstname,s.slastname,s.ssubmitterid, "
				+ "s.sshortname,s.swardname,s.stelephone,s.smobileno,s.sfaxno,s.semail,s.sspecialization, s.sreportrequirement,  "
				+ "s.ssampletransport,s.ntransactionstatus,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "')as stransdisplaystatus, "
				+ "ind.sinstitutiondeptname,ind.sinstitutiondeptcode "
				+ "from submitter s ,institutiondepartment ind,transactionstatus ts " + "where  "
				+ "ts.ntranscode =s.ntransactionstatus  "
				+ "and ind.ninstitutiondeptcode=s.ninstitutiondeptcode and ind.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  s.nsitecode="
				+ userInfo.getNmastersitecode() + " and s.ssubmittercode='" + ssubmittercode + "'";
		final List<Submitter> lstSubmitter = jdbcTemplate.query(strQuery, new Submitter());
		outputMap.put("Submitter", lstSubmitter);
		final List<SubmitterMapping> listSubmitterMapping = getSubmitterMapping(ssubmittercode, userInfo);
		outputMap.put("SubmitterMapping", listSubmitterMapping);

		if (!lstSubmitter.isEmpty()) {
			outputMap.put("selectedSubmitter", lstSubmitter.get(0));
		} else {
			outputMap.put("selectedSubmitter", null);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getSubmitterInstitutionCategoryCombo(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final String strInstitutionCategory = "select * from institutioncategory where ninstitutioncatcode >0 and nsitecode ="
				+ userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<InstitutionCategory> lstInstitutionCategory = jdbcTemplate.query(strInstitutionCategory,
				new InstitutionCategory());
		outputMap.put("FilterInstitutionCategory", lstInstitutionCategory);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getSubmitterInstitutionCombo(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final String strInstitution = "select i.*,ic.sinstitutioncatname from institution i "
				+ "join institutioncategory ic on i.ninstitutioncatcode= ic.ninstitutioncatcode "
				+ "where i.ninstitutioncatcode =" + inputMap.get("ninstitutioncatcode") + " and i.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and ic.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and i.nsitecode ="
				+ userInfo.getNmastersitecode();
		final List<Institution> lstInstitution = jdbcTemplate.query(strInstitution, new Institution());
		outputMap.put("FilterInstitution", lstInstitution);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getSubmitterInstitutionSiteCombo(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final String strInstitutionSite = "select * from institutionsite ins "
				+ "join institution i on ins.ninstitutioncode =i.ninstitutioncode "
				+ "where ins.ninstitutionsitecode not in (select ninstitutionsitecode from submittermapping where ssubmittercode='"
				+ inputMap.get("ssubmittercode") + "' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") and ins.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and i.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ins.ninstitutioncode="
				+ inputMap.get("ninstitutioncode") + " order by ins.ninstitutioncode ";
		final List<InstitutionSite> lstInstitutionSite = jdbcTemplate.query(strInstitutionSite, new InstitutionSite());
		outputMap.put("FilterInstitutionSite", lstInstitutionSite);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getActiveSubmitterByInstitution(final int ninstitutionCode, final UserInfo userInfo) {
		final Map<String, Object> outputMap = new HashMap<String, Object>();

//	String strQuery ="select s.ssubmittercode,"
//			+ "s.ninstitutiondeptcode,s.sfirstname||' '||s.slastname as ssubmittername,s.sfirstname,s.slastname,s.ssubmitterid,"
//			+ "s.sshortname,s.smobileno,s.semail, "
//		//	+ "case when s.sspecialization = '' then '-' else s.sspecialization end as sspecialization , "
//		//	+ "case when s.sfaxno = '' then '-' else s.sfaxno end as sfaxno ,"
//		//	+ "case when s.stelephone = '' then '-' else s.stelephone end as stelephone,"
//		//	+ "case when s.swardname = '' then '-' else s.swardname end as swardname,"
//		//	+ "case when s.ssampletransport = '' then '-' else s.ssampletransport end as ssampletransport ,"
//		//	+ "case when s.sreportrequirement = '' then '-' else s.sreportrequirement end as sreportrequirement ,"
//			+ "s.ntransactionstatus,i.sinstitutionname,ins.sinstitutionsitename "
//			+ " from submittermapping sm"
//			+ " join submitter  s  on sm.ssubmittercode =s.ssubmittercode "
//			//+ "join institutioncategory ic on ic.ninstitutioncatcode =s.ninstitutioncatcode "
//			+ "join institution i on i.ninstitutioncode =sm.ninstitutioncode "
//			+ "join institutionsite ins on ins.ninstitutionsitecode =sm.ninstitutionsitecode "
//		///	+ "join institutiondepartment ind on ind.ninstitutiondeptcode=s.ninstitutiondeptcode "
//		//	+ "join transactionstatus ts on ts.ntranscode =s.ntransactionstatus "
//			+ "where "
//			+" sm.ninstitutioncode="+ninstitutionCode
//			+ " and sm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//			+" and s.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//			+" and i.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//			+" and ins.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//		//	+" and ind.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//			//+" and ts.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//			+" and sm.nsitecode="+userInfo.getNmastersitecode()
//			+" and s.ntransactionstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//			//+" order by s.dcreateddate desc";

		final String strQuery = " select s.ssubmittercode,s.ninstitutiondeptcode, "
				+ " s.sfirstname||' '||s.slastname as ssubmittername,s.sfirstname, "
				+ " s.slastname,s.ssubmitterid,s.sshortname,s.smobileno,s.semail " + " from submittermapping sm "
				+ " join submitter  s  on sm.ssubmittercode =s.ssubmittercode "
				+ " join institution i on i.ninstitutioncode =sm.ninstitutioncode "
				+ " join institutionsite ins on ins.ninstitutionsitecode =sm.ninstitutionsitecode "
				+ " where  sm.ninstitutioncode= " + ninstitutionCode + " and sm.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and i.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ins.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sm.nsitecode= "
				+ userInfo.getNmastersitecode() + " and s.ntransactionstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by s.ssubmittercode ";
		final List<Submitter> lstSubmitter = jdbcTemplate.query(strQuery, new Submitter());
		outputMap.put("Submitter", lstSubmitter);
		if (!lstSubmitter.isEmpty()) {
			outputMap.put("selectedSubmitter", lstSubmitter.get(0));
		} else {
			outputMap.put("selectedSubmitter", null);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getSubmitterByInstitutionSite(final int ninstitutionSiteCode, final UserInfo userInfo,
			final Map<String, Object> inputMap) {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		Integer ninstitutionCode = null;
		if (inputMap.get("ninstitutioncode") != null) {
			ninstitutionCode = (Integer) inputMap.get("ninstitutioncode");
		}
		final String strQuery = " select s.ssubmittercode,s.ninstitutiondeptcode, "
				+ " s.sfirstname||' '||s.slastname||'('||s.ssubmitterid ||')'  as ssubmittername,s.sfirstname, "
				+ " s.slastname,s.ssubmitterid,s.sshortname,s.smobileno,s.semail " + " from submittermapping sm "
				+ " join submitter  s  on sm.ssubmittercode =s.ssubmittercode "
				+ " join institution i on i.ninstitutioncode =sm.ninstitutioncode "
				+ " join institutionsite ins on ins.ninstitutionsitecode =sm.ninstitutionsitecode "
				+ " where  sm.ninstitutionsitecode= " + ninstitutionSiteCode + " and sm.ninstitutioncode="
				+ ninstitutionCode + " and sm.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and s.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and i.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ins.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sm.nsitecode= "
				+ userInfo.getNmastersitecode() + " and s.ntransactionstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by s.ssubmittercode ";
		final List<Submitter> lstSubmitter = jdbcTemplate.query(strQuery, new Submitter());
		outputMap.put("Submitter", lstSubmitter);
		if (!lstSubmitter.isEmpty()) {
			outputMap.put("selectedSubmitter", lstSubmitter.get(0));
		} else {
			outputMap.put("selectedSubmitter", null);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}
}