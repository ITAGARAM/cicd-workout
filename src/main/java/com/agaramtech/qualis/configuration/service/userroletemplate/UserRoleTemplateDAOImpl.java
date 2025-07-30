package com.agaramtech.qualis.configuration.service.userroletemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.configuration.model.ApprovalConfig;
import com.agaramtech.qualis.configuration.model.ApprovalSubType;
import com.agaramtech.qualis.configuration.model.SeqNoConfigurationMaster;
import com.agaramtech.qualis.configuration.model.SeqNoUserRoleTemplateVersion;
import com.agaramtech.qualis.configuration.model.TreeTemplateTransactionRole;
import com.agaramtech.qualis.configuration.model.TreeVersionTemplate;
import com.agaramtech.qualis.configuration.model.TreetempTranstestGroup;
import com.agaramtech.qualis.credential.model.UserRole;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationSubType;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationType;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class UserRoleTemplateDAOImpl implements UserRoleTemplateDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserRoleTemplateDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getApprovalSubType(int nmoduletypecode, final UserInfo userInfo) throws Exception {
		Map<String, Object> objmap = new HashMap<>();
		final String strQuery = "select ntemplatecode, napprovalsubtypecode, coalesce(jsondata->'approvalsubtypename'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ "jsondata->'approvalsubtypename'->>'en-US') as ssubtypename, nisregsubtypeconfigneed"
				+ " from ApprovalSubType where nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode = " + userInfo.getNmastersitecode() + " order by ntemplatecode asc;";

		List<ApprovalSubType> lstapprovalsubtype = (List<ApprovalSubType>) jdbcTemplate.query(strQuery,
				new ApprovalSubType());
		objmap.putAll((Map<String, Object>) getApprovalRegSubType(3, -1,
				lstapprovalsubtype.get(0).getNisregsubtypeconfigneed(),
				lstapprovalsubtype.get(0).getNapprovalsubtypecode(), lstapprovalsubtype.get(0).getNtemplatecode(),
				userInfo).getBody());

		objmap.put("lstapprovalsubtype", lstapprovalsubtype);
		Map<String, Object> subTypeValue = null;
		if (!lstapprovalsubtype.isEmpty()) {
			subTypeValue = new HashMap<>();
			subTypeValue.put("label", lstapprovalsubtype.get(0).getSsubtypename());
			subTypeValue.put("value", lstapprovalsubtype.get(0).getNapprovalsubtypecode());
			subTypeValue.put("item", lstapprovalsubtype.get(0));
		}
		objmap.put("realApprovalSubTypeValue", subTypeValue);
		objmap.put("defaultapprovalsubtype", subTypeValue);
		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getApprovalRegSubType(final int nflag, int nregtypecode, final int isregneed,
			final int napprovalsubtypecode, final int ntemplatecode, final UserInfo userInfo) throws Exception {
		Map<String, Object> objmap = new HashMap<>();
		Map<String, Object> getuserroletemp = new HashMap<>();
		List<RegistrationType> lstregistrationtype = new ArrayList<>();
		List<RegistrationSubType> lstregistrationsubtype = new ArrayList<>();
		Map<String, Object> regTypeValue = null;
		Map<String, Object> regSubTypeValue = null;
		int nregsubtypecode = 0;
		String strQuery = "";
		switch (nflag) {
		case 1:
			if (isregneed == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				strQuery = regtypeQuery(napprovalsubtypecode, userInfo);
				lstregistrationtype = (List<RegistrationType>) jdbcTemplate.query(strQuery, new RegistrationType());
				nregtypecode = !lstregistrationtype.isEmpty() ? lstregistrationtype.get(0).getNregtypecode() : -1;
				if (!lstregistrationtype.isEmpty()) {
					regTypeValue = new HashMap<>();
					regTypeValue.put("label", lstregistrationtype.get(0).getSregtypename());
					regTypeValue.put("value", lstregistrationtype.get(0).getNregtypecode());
					regTypeValue.put("item", lstregistrationtype.get(0));
					strQuery = regSubtypeQuery(nregtypecode, userInfo);
					lstregistrationsubtype = (List<RegistrationSubType>) jdbcTemplate.query(strQuery,
							new RegistrationSubType());
					nregsubtypecode = !lstregistrationsubtype.isEmpty()
							? lstregistrationsubtype.get(0).getNregsubtypecode()
							: -1;
					if (!lstregistrationsubtype.isEmpty()) {
						regSubTypeValue = new HashMap<>();
						regSubTypeValue.put("label", lstregistrationsubtype.get(0).getSregsubtypename());
						regSubTypeValue.put("value", lstregistrationsubtype.get(0).getNregsubtypecode());
						regSubTypeValue.put("item", lstregistrationsubtype.get(0));
					}
				}
				getuserroletemp.put("nflag", 2);
				getuserroletemp.put("ntreetemplatecode", ntemplatecode);
				getuserroletemp.put("nmodulecode", 1);
				getuserroletemp.put("napprovalsubtypecode", napprovalsubtypecode);
				getuserroletemp.put("nregtypecode", nregtypecode);
				getuserroletemp.put("nregsubtypecode", nregsubtypecode);
				getuserroletemp.put("ntreeversiontempcode", -1);
			} else {
				getuserroletemp.put("nflag", 1);
				getuserroletemp.put("ntreetemplatecode", ntemplatecode);
				getuserroletemp.put("ntreeversiontempcode", -1);
			}
			objmap.put("listRegistrationType", lstregistrationtype);
			objmap.put("listRegistrationSubType", lstregistrationsubtype);
			objmap.put("defaultregtype", regTypeValue);
			objmap.put("defaultregsubtype", regSubTypeValue);
			break;
		case 2:
			strQuery = regSubtypeQuery(nregtypecode, userInfo);
			lstregistrationsubtype = (List<RegistrationSubType>) jdbcTemplate.query(strQuery,
					new RegistrationSubType());
			nregsubtypecode = !lstregistrationsubtype.isEmpty() ? lstregistrationsubtype.get(0).getNregsubtypecode()
					: -1;
			getuserroletemp.put("nflag", 2);
			getuserroletemp.put("ntreetemplatecode", ntemplatecode);
			getuserroletemp.put("nmodulecode", 1);
			getuserroletemp.put("nregtypecode", nregtypecode);
			getuserroletemp.put("nregsubtypecode", nregsubtypecode);
			getuserroletemp.put("napprovalsubtypecode", napprovalsubtypecode);
			getuserroletemp.put("ntreeversiontempcode", -1);
			if (!lstregistrationsubtype.isEmpty()) {
				regSubTypeValue = new HashMap<>();
				regSubTypeValue.put("label", lstregistrationsubtype.get(0).getSregsubtypename());
				regSubTypeValue.put("value", lstregistrationsubtype.get(0).getNregsubtypecode());
				regSubTypeValue.put("item", lstregistrationsubtype.get(0));
			}
			objmap.put("listRegistrationSubType", lstregistrationsubtype);
			objmap.put("defaultregsubtype", regSubTypeValue);
			break;
		case 3:
			if (isregneed == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				strQuery = regtypeQuery(napprovalsubtypecode, userInfo);
				lstregistrationtype = (List<RegistrationType>) jdbcTemplate.query(strQuery, new RegistrationType());
				nregtypecode = !lstregistrationtype.isEmpty() ? lstregistrationtype.get(0).getNregtypecode() : -1;
				if (!lstregistrationtype.isEmpty()) {
					regTypeValue = new HashMap<>();
					regTypeValue.put("label", lstregistrationtype.get(0).getSregtypename());
					regTypeValue.put("value", lstregistrationtype.get(0).getNregtypecode());
					regTypeValue.put("item", lstregistrationtype.get(0));
					strQuery = regSubtypeQuery(nregtypecode, userInfo);
					lstregistrationsubtype = (List<RegistrationSubType>) jdbcTemplate.query(strQuery,
							new RegistrationSubType());
					nregsubtypecode = !lstregistrationsubtype.isEmpty()
							? lstregistrationsubtype.get(0).getNregsubtypecode()
							: -1;
					if (!lstregistrationsubtype.isEmpty()) {
						regSubTypeValue = new HashMap<>();
						regSubTypeValue.put("label", lstregistrationsubtype.get(0).getSregsubtypename());
						regSubTypeValue.put("value", lstregistrationsubtype.get(0).getNregsubtypecode());
						regSubTypeValue.put("item", lstregistrationsubtype.get(0));
					}

				}
				getuserroletemp.put("nflag", 2);
				getuserroletemp.put("ntreetemplatecode", ntemplatecode);
				getuserroletemp.put("nmodulecode", 1);
				getuserroletemp.put("napprovalsubtypecode", napprovalsubtypecode);
				getuserroletemp.put("nregtypecode", nregtypecode);
				getuserroletemp.put("nregsubtypecode", nregsubtypecode);
				getuserroletemp.put("ntreeversiontempcode", -1);
			} else {
				getuserroletemp.put("nflag", 1);
				getuserroletemp.put("ntreetemplatecode", ntemplatecode);
				getuserroletemp.put("ntreeversiontempcode", -1);
			}
			objmap.putAll((Map<String, Object>) getUserroletemplate(getuserroletemp, userInfo).getBody());
			objmap.put("listRegistrationType", lstregistrationtype);
			objmap.put("listRegistrationSubType", lstregistrationsubtype);
			objmap.put("defaultregtype", regTypeValue);
			objmap.put("defaultregsubtype", regSubTypeValue);
			objmap.put("realRegTypeValue", regTypeValue);
			objmap.put("realRegSubTypeValue", regSubTypeValue);
			break;
		default:
			break;
		}
		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}

	private String regSubtypeQuery(final int nregtypecode, final UserInfo userInfo) {
//		return "select nregsubtypecode, nregtypecode, jsondata, dmodifieddate, nsorter, nsitecode, nstatus,coalesce(jsondata->'sregsubtypename'->>'"
//				+ userInfo.getSlanguagetypecode() + "',jsondata->'sregsubtypename'->>'en-US') as sregsubtypename "
//				+ " from registrationsubtype rst  where rst.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rst.nregtypecode = "
//				+ nregtypecode + " and rst.nsitecode = " + userInfo.getNmastersitecode() + " order by rst.nsorter asc";
		// Added by Gowtham R on 21/05/2025 - ALPD-5357 - The Registration Sub Type allows its record to be deleted even if there is an Approved User Role Template record associated with it.
		return "select distinct rst.nregsubtypecode, rst.nregtypecode, rst.jsondata, rst.dmodifieddate, rst.nsorter,"
				+ " rst.nsitecode, rst.nstatus, coalesce(rst.jsondata->'sregsubtypename'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',rst.jsondata->'sregsubtypename'->>'en-US') as sregsubtypename "
				+ " from regsubtypeconfigversion rstc, approvalconfig ac, registrationsubtype rst "
				+ " where rst.nsitecode  = " + userInfo.getNmastersitecode()
				+ " and rstc.ntransactionstatus = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and rst.nregsubtypecode = ac.nregsubtypecode and ac.nregtypecode = "
				+ nregtypecode + " and ac.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rst.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rstc.nsitecode = "
				+ userInfo.getNmastersitecode() + " and rstc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rst.nregsubtypecode > 0 "
				+ " and ac.napprovalconfigcode = rstc.napprovalconfigcode and ac.nsitecode = " + userInfo.getNmastersitecode()
				+ " order by rst.nregsubtypecode desc";
	}

	private String regtypeQuery(final int napprovalsubtypecode, final UserInfo userInfo) {
		return "select rt.nregtypecode, rt.nsampletypecode, rt.napprovalsubtypecode, rt.jsondata, rt.jsonuidata, rt.nsorter, rt.dmodifieddate, rt.nsitecode, rt.nstatus,coalesce(rt.jsondata->'sregtypename'->>'"
				+ userInfo.getSlanguagetypecode() + "',rt.jsondata->'sregtypename'->>'en-US') as sregtypename "
				+ " from registrationtype rt,sampletype st where rt.nsampletypecode=st.nsampletypecode and st.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.napprovalsubtypecode = "
				+ napprovalsubtypecode + " and rt.nsitecode = " + userInfo.getNmastersitecode() + " and st.nsitecode = "
				+ userInfo.getNmastersitecode() + " order by rt.nsorter asc;";
	}

	@Override
	public ResponseEntity<Object> getUserRoleforTree(final UserInfo userInfo, final Integer nregtypecode,
			final Integer nregsubtypecode, final Integer napprovalsubtypecode) throws Exception {
		String str = " select napprovalconfigcode from approvalconfig where napprovalsubtypecode = "
				+ napprovalsubtypecode + " " + " and nregtypecode = " + nregtypecode + " and nregsubtypecode = "
				+ nregsubtypecode + " " + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
				+ userInfo.getNmastersitecode();
		List<ApprovalConfig> lstTree = (List<ApprovalConfig>) jdbcTemplate.query(str, new ApprovalConfig());
		if (!lstTree.isEmpty()) {
			String strQuery = " select ur.nuserrolecode, ur.suserrolename, ur.sdescription, ur.dmodifieddate, ur.nsitecode, ur.nstatus, ur.suserrolename as sleveluserrole from userroleconfig uc,userrole ur where"
					+ " ur.nuserrolecode = uc.nuserrolecode and" + " uc.nneedapprovalflow = "
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and uc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and ur.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nsitecode = "
					+ userInfo.getNmastersitecode() + " and uc.nsitecode = " + userInfo.getNmastersitecode() + "";
			return new ResponseEntity<>(jdbcTemplate.query(strQuery, new UserRole()), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_PLSCONFIGTHESUBTYPE", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getUserroletemplate(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		final int nflag = (int) inputMap.get("nflag");
		final int ntemplatecode = (int) inputMap.get("ntreetemplatecode");
		final int ntreeversiontempcode = (int) inputMap.get("ntreeversiontempcode");
		String str = "";
		switch (nflag) {
		case 1:
			str = "select vt.ntreeversiontempcode ,case when max(vt.nversionno)>0 then cast(max(vt.nversionno) as varchar(15)) else '-' end  sversionstatus,"
					+ " max(vt.sversiondescription) sversiondescription, max(vt.nversionno) nversionno,max(vt.ntransactionstatus) ntransactionstatus,"
					+ " max(vt.ntemplatecode) ntemplatecode,max(ttt.napprovalconfigcode) napprovalconfigcode,max(coalesce(ts.jsondata->'stransdisplaystatus'->>'"
					+ userInfo.getSlanguagetypecode() + "',"
					+ " ts.jsondata->'stransdisplaystatus'->>'en-US')) as stransdisplaystatus "
					+ " from treeversiontemplate vt,transactionstatus ts,treetemplatetransactionrole ttt  "
					+ " where vt.ntreeversiontempcode=ttt.ntreeversiontempcode and vt.ntransactionstatus=ts.ntranscode "
					+ " and vt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ts.nstatus =  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ttt.nstatus =  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and vt.ntemplatecode = " + ntemplatecode + " and vt.nsitecode =  "
					+ userInfo.getNmastersitecode() + " and ttt.nsitecode = " + userInfo.getNmastersitecode()
					+ " and vt.ntreeversiontempcode > 0 "
					+ " group by vt.ntreeversiontempcode order by ntreeversiontempcode;";
			break;
		case 2:
			final int napprovalsubtypecode = (int) inputMap.get("napprovalsubtypecode");
			final int nregtypecode = (int) inputMap.get("nregtypecode");
			final int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
			str = " select vt.ntreeversiontempcode ,case when max(vt.nversionno)>0 then cast(max(vt.nversionno) as varchar(15)) else '-' end  sversionstatus,max(vt.sversiondescription) sversiondescription,"
					+ " max(vt.nversionno) nversionno,max(vt.ntransactionstatus) ntransactionstatus,max(vt.ntemplatecode) ntemplatecode,max(ttt.napprovalconfigcode) napprovalconfigcode, max(coalesce(ts.jsondata->'stransdisplaystatus'->>'"
					+ userInfo.getSlanguagetypecode() + "',"
					+ " ts.jsondata->'stransdisplaystatus'->>'en-US')) as stransdisplaystatus "
					+ " from treeversiontemplate vt,transactionstatus ts,treetemplatetransactionrole ttt,approvalconfig ac "
					+ " where vt.ntransactionstatus = ts.ntranscode and vt.ntreeversiontempcode = ttt.ntreeversiontempcode and ttt.napprovalconfigcode = ac.napprovalconfigcode "
					+ " and vt.ntemplatecode = " + ntemplatecode + " and vt.nsitecode = "
					+ userInfo.getNmastersitecode() + " and ac.napprovalsubtypecode = " + napprovalsubtypecode
					+ " and ac.nregtypecode = " + nregtypecode + " and ac.nregsubtypecode = " + nregsubtypecode + " "
					+ " and vt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and ttt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ac.nstatus =  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ttt.nsitecode = " + userInfo.getNmastersitecode() + " and ac.nsitecode = "
					+ userInfo.getNmastersitecode()
					+ " and vt.ntreeversiontempcode > 0 group by vt.ntreeversiontempcode;";
			break;
		default:
			break;
		}
		List<TreeVersionTemplate> lst = (List<TreeVersionTemplate>) jdbcTemplate.query(str, new TreeVersionTemplate());
		if (ntreeversiontempcode > 0) {
			returnMap.putAll((Map<String, Object>) getUserroletemplatebyId(ntreeversiontempcode, userInfo).getBody());
		} else {
			if (!lst.isEmpty()) {
				returnMap.putAll(
						(Map<String, Object>) getUserroletemplatebyId(lst.get(lst.size() - 1).getNtreeversiontempcode(),
								userInfo).getBody());
			} else {
				returnMap.put("levelsuserroletemplate", new TreetempTranstestGroup());
			}
		}
		returnMap.put("listuserroletemplate", lst);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getUserroletemplatebyId(final int ntreeversiontempcode, final UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();
		String getstr = "";
		getstr = "select vt.ntreeversiontempcode ,case when max(vt.nversionno) > 0 then cast(max(vt.nversionno) as varchar(15)) else '-' end  sversionstatus,max(vt.sversiondescription) sversiondescription,"
				+ " max(vt.nversionno) nversionno,max(vt.ntransactionstatus) ntransactionstatus,max(vt.ntemplatecode) ntemplatecode,max(ttt.napprovalconfigcode) napprovalconfigcode,"
				+ " max(coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US')) as stransdisplaystatus  from treeversiontemplate vt,transactionstatus ts,treetemplatetransactionrole ttt  "
				+ " where vt.ntreeversiontempcode=ttt.ntreeversiontempcode and vt.ntransactionstatus = ts.ntranscode and vt.ntreeversiontempcode = "
				+ ntreeversiontempcode + "" + " and vt.nsitecode = " + userInfo.getNmastersitecode()
				+ " and vt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and ttt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ttt.nsitecode = " + userInfo.getNmastersitecode() + " group by vt.ntreeversiontempcode";

		TreeVersionTemplate getobj = mapper.convertValue(
				jdbcUtilityFunction.queryForObject(getstr, TreeVersionTemplate.class, jdbcTemplate),
				TreeVersionTemplate.class);
		if (getobj != null) {
			returnMap.put("selectedURTVersion", getobj);
			returnMap.putAll((Map<String, Object>) getTreetemplate(ntreeversiontempcode, userInfo).getBody());
			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> getTreetemplate(int ntreeversiontempcode, final UserInfo userInfo) throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		String getstatus = "select ntreeversiontempcode from treeversiontemplate where ntreeversiontempcode =  "
				+ ntreeversiontempcode + " and nstatus =  "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		TreeVersionTemplate objget = (TreeVersionTemplate) jdbcUtilityFunction.queryForObject(getstatus,
				TreeVersionTemplate.class, jdbcTemplate);
		if (objget != null) {
			final String getstr = "select ttg.nlevelno,ttg.napprovalconfigcode,tvt.sversiondescription,ur.suserrolename,ur.suserrolename as sleveluserrole,"
					+ " ur.nuserrolecode,ttg.ntemptransrolecode,ttg.nparentnode,tvt.ntransactionstatus "
					+ " from userrole ur,treetemplatetransactionrole ttg,treeversiontemplate tvt, approvalconfig ac "
					+ " where tvt.ntreeversiontempcode = ttg.ntreeversiontempcode  and ur.nuserrolecode = ttg.nuserrolecode  "
					+ " and tvt.ntreeversiontempcode = " + ntreeversiontempcode
					+ " and ac.napprovalconfigcode = tvt.napprovalconfigcode and ttg.napprovalconfigcode = tvt.napprovalconfigcode "
					+ " and ac.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ur.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and ttg.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tvt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ur.nsitecode = " + userInfo.getNmastersitecode() + " and ttg.nsitecode = "
					+ userInfo.getNmastersitecode() + " and ac.nsitecode = " + userInfo.getNmastersitecode()
					+ " order by ttg.nlevelno";

			List<TreeVersionTemplate> getlst = (List<TreeVersionTemplate>) jdbcTemplate.query(getstr,
					new TreeVersionTemplate());
			returnMap.put("levelsuserroletemplate", getlst);
			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> fnSequencenumberUserRoleTemplateUpdate(Map<String, Object> insertseqmap,
			final boolean bflag) throws Exception {
		Map<String, Object> map = new HashMap<>();
		List<Integer> lstString = (List<Integer>) insertseqmap.get("treetemptranstestgroup");
		int ntreetemplatecount = lstString.size();
		if (bflag) {
			String sQuery = "LOCK TABLE locktreeversion;";
			jdbcTemplate.execute(sQuery);

			final String sequenceno = "select nsequenceno, stablename from seqnoconfigurationmaster where stablename in (N'treeversiontemplate',N'treetemplatetransactionrole');";
			List<SeqNoConfigurationMaster> lstSeqNo = (List<SeqNoConfigurationMaster>) jdbcTemplate.query(sequenceno,
					new SeqNoConfigurationMaster());
			Map<String, Integer> seqMap = lstSeqNo.stream()
					.collect(Collectors.toMap(SeqNoConfigurationMaster::getStablename,
							seqNoConfigurationMaster -> seqNoConfigurationMaster.getNsequenceno()));
			final int treetemplatetransactionrole = seqMap.get("treetemplatetransactionrole");
			final int ntreeversion = seqMap.get("treeversiontemplate");
			String seqUpdateQry = "update seqnoconfigurationmaster set nsequenceno = " + (ntreeversion + 1)
					+ "  where stablename = 'treeversiontemplate';";
			jdbcTemplate.execute(seqUpdateQry);

			seqUpdateQry = " update seqnoconfigurationmaster set nsequenceno = "
					+ (treetemplatetransactionrole + ntreetemplatecount)
					+ " where stablename = N'treetemplatetransactionrole';";
			jdbcTemplate.execute(seqUpdateQry);

			map.put("treetemplatetransactionrole", treetemplatetransactionrole);
			map.put("ntreeversion", ntreeversion);
		} else {
			final String sequenceno = "select nsequenceno from seqnoconfigurationmaster where stablename = 'treetemplatetransactionrole'";
			int treetemplatetransactionrole = jdbcTemplate.queryForObject(sequenceno, Integer.class);

			final String seqUpdateQry = " update seqnoconfigurationmaster set nsequenceno = "
					+ (treetemplatetransactionrole + ntreetemplatecount)
					+ " where stablename = 'treetemplatetransactionrole';";
			jdbcTemplate.execute(seqUpdateQry);
			map.put("treetemplatetransactionrole", treetemplatetransactionrole);
		}
		return map;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> createUserRoleTemplatemaster(Map<String, Object> inputmap) throws Exception {

		final String sQuery = " lock  table lockuserroletemplate " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		Map<String, Object> returnMap = new HashMap<>();
		Map<String, Object> getuserroletemplate = new HashMap<>();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstaudit = new ArrayList<>();
		final int ntreeversion = (int) inputmap.get("ntreeversion");
		int ntreetemptestgroup = (int) inputmap.get("treetemplatetransactionrole");
		final int ntemplatecode = (int) inputmap.get("ntemplatecode");
		final int napprovalsubtypecode = (int) inputmap.get("napprovalsubtypecode");
		final int isregneed = (int) inputmap.get("isregneed");
		final String specname = (String) inputmap.get("specname");
		final int nregtypecode = (int) inputmap.get("nregtypecode");
		final int nregsubtypecode = (int) inputmap.get("nregsubtypecode");
		final List<String> lstString = (List<String>) inputmap.get("treetemptranstestgroup");
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputmap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		int nparentcode = 0;
		String schildcode = "";
		int napprovalconfigcode = 0;

		if (isregneed == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
			final String strQuery = "select napprovalconfigcode from approvalconfig where napprovalsubtypecode = "
					+ napprovalsubtypecode + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
					+ userInfo.getNmastersitecode() + "";
			List<ApprovalConfig> lstTree = (List<ApprovalConfig>) jdbcTemplate.query(strQuery, new ApprovalConfig());
			if (!lstTree.isEmpty()) {
				napprovalconfigcode = lstTree.get(0).getNapprovalconfigcode();
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PLSCONFIGTHESUBTYPE",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			final String strQuery = "select napprovalconfigcode from approvalconfig where napprovalsubtypecode = "
					+ napprovalsubtypecode + " and nregtypecode = " + nregtypecode + " and nregsubtypecode = "
					+ nregsubtypecode + " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode = " + userInfo.getNmastersitecode() + "";

			List<ApprovalConfig> lstTree = (List<ApprovalConfig>) jdbcTemplate.query(strQuery, new ApprovalConfig());
			napprovalconfigcode = lstTree.get(0).getNapprovalconfigcode();
		}

		String slevel = "/";
		String strInsertTreeVersion = "insert into treeversiontemplate (ntreeversiontempcode,ntransactionstatus,napprovalconfigcode,"
				+ "ntemplatecode,nsampletypecode,nversionno,sversiondescription,nsitecode,nstatus,dmodifieddate) values ("
				+ (ntreeversion + 1) + "," + Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ","
				+ napprovalconfigcode + "," + ntemplatecode + ",-1,-1,N'" + stringUtilityFunction.replaceQuote(specname)
				+ "'," + userInfo.getNmastersitecode() + ","
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ",'"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + ")";

		jdbcTemplate.execute(strInsertTreeVersion);
		StringBuilder sb = new StringBuilder(
				"Insert into treetemplatetransactionrole (ntemptransrolecode, napprovalconfigcode, nuserrolecode, "
						+ "ntreeversiontempcode, ntransactionstatus, nlevelno, nparentnode, ntemplatecode, schildnode, slevelformat,dmodifieddate, nsitecode, nstatus) values ");
		for (int i = 0; i < lstString.size(); i++) {
			String scontrollabel = String.valueOf(lstString.get(i));
			int userrolecode = Integer.parseInt(scontrollabel);
			slevel = slevel.concat("1/");
			if (i == 0) {
				nparentcode = -1;
			} else {
				nparentcode = ntreetemptestgroup;
			}
			if (i == lstString.size() - 1) {
				schildcode = "-1";
			} else {
				schildcode = Integer.toString(ntreetemptestgroup + 2);
			}
			sb.append("(" + (ntreetemptestgroup + 1) + "," + napprovalconfigcode + "," + userrolecode + ","
					+ (ntreeversion + 1) + "," + Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ","
					+ (i + 1) + "," + nparentcode + "," + ntemplatecode + "," + schildcode + ", N'" + slevel + "',"
					+ "'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode()
					+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),");
			ntreetemptestgroup = ntreetemptestgroup + 1;
		}

		jdbcTemplate.execute(sb.substring(0, sb.length() - 1) + ";");

		String roletemplate = "select ntreeversiontempcode, napprovalconfigcode, ntransactionstatus, ntemplatecode, nsampletypecode, nversionno, sversiondescription, dmodifieddate, nsitecode, nstatus from treeversiontemplate where ntreeversiontempcode = "
				+ (ntreeversion + 1);
		List<TreeVersionTemplate> lsttreetemplate = (List<TreeVersionTemplate>) jdbcTemplate.query(roletemplate,
				new TreeVersionTemplate());

		roletemplate = "select ttr.nuserrolecode,tt.sversiondescription,tt.ntreeversiontempcode as ntreeversiontempcode,ntemptransrolecode,ttr.ntransactionstatus from treetemplatetransactionrole ttr,transactionstatus ts,treeversiontemplate tt "
				+ " where ts.ntranscode = ttr.ntransactionstatus and ttr.ntreeversiontempcode = tt.ntreeversiontempcode and ttr.ntreeversiontempcode = "
				+ (ntreeversion + 1) + " " + " and ttr.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ttr.nsitecode = "
				+ userInfo.getNmastersitecode() + " and tt.nsitecode = " + userInfo.getNmastersitecode() + "";
		List<TreeTemplateTransactionRole> lstroletemplate = (List<TreeTemplateTransactionRole>) jdbcTemplate
				.query(roletemplate, new TreeTemplateTransactionRole());
		lstaudit.add(lsttreetemplate);
		lstaudit.add(lstroletemplate);
		multilingualIDList.add("IDS_ADDUSERROLETEMPLATE");
		multilingualIDList.add("IDS_ADDUSERROLETEMPLATELEVEL");
		auditUtilityFunction.fnInsertListAuditAction(lstaudit, 1, null, multilingualIDList, userInfo);

		if (isregneed == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
			getuserroletemplate.put("nflag", 1);
			getuserroletemplate.put("ntreetemplatecode", ntemplatecode);
			getuserroletemplate.put("ntreeversiontempcode", -1);
		} else {
			getuserroletemplate.put("nflag", 2);
			getuserroletemplate.put("ntreetemplatecode", ntemplatecode);
			getuserroletemplate.put("nmodulecode", 1);
			getuserroletemplate.put("nregtypecode", nregtypecode);
			getuserroletemplate.put("nregsubtypecode", nregsubtypecode);
			getuserroletemplate.put("napprovalsubtypecode", napprovalsubtypecode);
			getuserroletemplate.put("ntreeversiontempcode", -1);
		}
		returnMap.putAll((Map<String, Object>) getUserroletemplate(getuserroletemplate, userInfo).getBody());
		LOGGER.info("At createUserRoleTemplatemaster() of UserRoleTemplateDAOImpl.java");
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public String fnCheckUserName(Map<String, Object> objMap) throws Exception {
		int ntreeversiontempcode = (int) objMap.get("ntreeversiontempcode");
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(objMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		String strQuery = "select nuserrolecode from treetemplatetransactionrole where ntreeversiontempcode = "
				+ ntreeversiontempcode + " and nsitecode = " + userInfo.getNmastersitecode();
		List<TreeTemplateTransactionRole> lsttreecontrol = (List<TreeTemplateTransactionRole>) jdbcTemplate
				.query(strQuery, new TreeTemplateTransactionRole());
		List<String> lstString = (List<String>) objMap.get("treetemptranstestgroup");
		int unavailable = 0;
		if (lstString.size() == lsttreecontrol.size()) {
			for (int i = 0; lsttreecontrol.size() - 1 >= i; i++) {
				if (Integer.toString(lsttreecontrol.get(i).getNuserrolecode()).equals(lstString.get(i))) {
					unavailable++;
				}
			}
			if (unavailable == lstString.size()) {
				return "Success";
			} else {
				return "Failure";
			}
		} else {
			return "Failure";
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> updateEditUserRoleTemplatemaster(Map<String, Object> inputMap, final boolean bflag)
			throws Exception {

		final String sQuery = " lock  table lockuserroletemplate " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		Map<String, Object> returnMap = new HashMap<>();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> updateduserroletemplate = new ArrayList<>();
		final List<Object> olduserroletempate = new ArrayList<>();
		Map<String, Object> getuserroletemplate = new HashMap<>();
		TreeVersionTemplate newTreeversion = new TreeVersionTemplate();
		int ntreetemptestgroup = (int) inputMap.get("treetemplatetransactionrole");
		final int nregtypecode = (int) inputMap.get("nregtypecode");
		final int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
		final String specname = (String) inputMap.get("specname");
		final int ntemplatecode = (int) inputMap.get("ntemplatecode");
		final int napprovalsubtypecode = (int) inputMap.get("napprovalsubtypecode");
		final int ntreeversiontempcode = (int) inputMap.get("ntreeversiontempcode");
		final int napprovalconfigcode = (int) inputMap.get("napprovalconfigcode");
		final int isregneed = (int) inputMap.get("isregneed");
		final List<String> lstString = (List<String>) inputMap.get("treetemptranstestgroup");
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		int nparentcode = 0;
		String schildnode = "";
		String slevel = "/";
		String str = "select ntreeversiontempcode, napprovalconfigcode, ntransactionstatus, ntemplatecode, nsampletypecode, nversionno, sversiondescription, dmodifieddate, nsitecode, nstatus from treeversiontemplate where ntreeversiontempcode = "
				+ ntreeversiontempcode + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		TreeVersionTemplate oldTreeversion = (TreeVersionTemplate) jdbcUtilityFunction.queryForObject(str,
				TreeVersionTemplate.class, jdbcTemplate);
		if (oldTreeversion != null) {
			if (oldTreeversion.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
				if (!bflag) {
					String strInsertTreeVersion = "Update treeversiontemplate set sversiondescription = N'"
							+ stringUtilityFunction.replaceQuote(specname) + "', " + "dmodifieddate = '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where ntreeversiontempcode = "
							+ ntreeversiontempcode + "";
					jdbcTemplate.execute(strInsertTreeVersion);
					newTreeversion.setNtreeversiontempcode(oldTreeversion.getNtreeversiontempcode());
					newTreeversion.setSversiondescription(specname);
					newTreeversion.setNtransactionstatus(oldTreeversion.getNtransactionstatus());
					olduserroletempate.add(oldTreeversion);
					updateduserroletemplate.add(newTreeversion);
					multilingualIDList.add("IDS_EDITUSERROLETEMPLATE");
					auditUtilityFunction.fnInsertAuditAction(updateduserroletemplate, 2, olduserroletempate,
							multilingualIDList, userInfo);
				} else {
					String insertQuery = "";

					insertQuery += "Update treeversiontemplate set sversiondescription = N'"
							+ stringUtilityFunction.replaceQuote(specname) + "', " + "dmodifieddate = '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where ntreeversiontempcode = "
							+ ntreeversiontempcode + "; ";
					insertQuery += " Delete from treetemplatetransactionrole where ntreeversiontempcode = "
							+ ntreeversiontempcode + ";";

					StringBuilder sb = new StringBuilder(
							"Insert into treetemplatetransactionrole (ntemptransrolecode,napprovalconfigcode,nuserrolecode,ntreeversiontempcode,ntransactionstatus,nlevelno,nparentnode,ntemplatecode,schildnode,slevelformat,dmodifieddate,nsitecode,nstatus) values ");
					for (int i = 0; i < lstString.size(); i++) {
						final String scontrollabel = String.valueOf(lstString.get(i));
						final int userrolecode = Integer.parseInt(scontrollabel);
						slevel = slevel.concat("1/");
						if (i == 0) {
							nparentcode = -1;
						} else {
							nparentcode = ntreetemptestgroup;
						}
						if (i == lstString.size() - 1) {
							schildnode = "-1";
						} else {
							schildnode = Integer.toString(ntreetemptestgroup + 2);
						}

						sb.append(" (" + (ntreetemptestgroup + 1) + "," + napprovalconfigcode + "," + userrolecode + ","
								+ ntreeversiontempcode + ","
								+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + "," + (i + 1) + ","
								+ nparentcode + "," + ntemplatecode + "," + schildnode + ", '" + slevel + "'," + "'"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
								+ userInfo.getNmastersitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),");
						ntreetemptestgroup = ntreetemptestgroup + 1;
					}
					insertQuery += sb.substring(0, sb.length() - 1) + ";";
					jdbcTemplate.execute(insertQuery);

					final String roletemplate = "select ttr.nuserrolecode,tt.sversiondescription,tt.ntreeversiontempcode AS ntreeversiontempcode,"
							+ "tt.ntransactionstatus,ntemptransrolecode  from treetemplatetransactionrole ttr,treeversiontemplate tt "
							+ " where ttr.ntreeversiontempcode = tt.ntreeversiontempcode and ttr.ntreeversiontempcode = "
							+ (ntreeversiontempcode) + " " + " and ttr.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tt.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ttr.nsitecode = "
							+ userInfo.getNmastersitecode() + "";
					List<TreeTemplateTransactionRole> lstroletemplate = (List<TreeTemplateTransactionRole>) jdbcTemplate
							.query(roletemplate, new TreeTemplateTransactionRole());
					multilingualIDList.add("IDS_EDITUSERROLETEMPLATE");
					newTreeversion.setSversiondescription(lstroletemplate.get(0).getSversiondescription());
					newTreeversion.setNtransactionstatus((short) lstroletemplate.get(0).getNtransactionstatus());
					newTreeversion.setNtreeversiontempcode(lstroletemplate.get(0).getNtreeversiontempcode());
					auditUtilityFunction.fnInsertAuditAction(Arrays.asList(newTreeversion), 2,
							Arrays.asList(oldTreeversion), multilingualIDList, userInfo);
					updateduserroletemplate.add(lstroletemplate);
					auditUtilityFunction.fnInsertListAuditAction(updateduserroletemplate, 1, null, multilingualIDList,
							userInfo);
				}
				if (isregneed == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
					getuserroletemplate.put("nflag", 1);
					getuserroletemplate.put("ntreetemplatecode", ntemplatecode);
					getuserroletemplate.put("ntreeversiontempcode", ntreeversiontempcode);

				} else {
					getuserroletemplate.put("nflag", 2);
					getuserroletemplate.put("ntreetemplatecode", ntemplatecode);
					getuserroletemplate.put("nmodulecode", 1);
					getuserroletemplate.put("nregtypecode", nregtypecode);
					getuserroletemplate.put("nregsubtypecode", nregsubtypecode);
					getuserroletemplate.put("napprovalsubtypecode", napprovalsubtypecode);
					getuserroletemplate.put("ntreeversiontempcode", ntreeversiontempcode);
				}
				returnMap.putAll((Map<String, Object>) getUserroletemplate(getuserroletemplate, userInfo).getBody());

			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

//@Override
//public boolean prevalidateuserroletemplate(final int isregneed, final int nregtypecode, final int nregsubtypecode)throws Exception {
//	boolean rtval = false;
//	if (isregneed == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
//		String strtochk = "select count(ac.napprovalconfigcode) from registrationhistory jrh,registrationarno jqra,approvalconfigversion acv,"
//						+ " treeversiontemplate tvt,approvalconfig ac where jrh.nreghistorycode = any (select max(nreghistorycode) from registrationhistory jrh1 "
//						+ " group by jrh1.npreregno) and jqra.npreregno = jrh.npreregno and acv.napproveconfversioncode = jqra.napprovalversioncode "
//						+ " and ac.napprovalconfigcode = acv.napprovalconfigcode and acv.ntreeversiontempcode = tvt.ntreeversiontempcode "
//						+ " and tvt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+ " and acv.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+ " and jqra.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+ " and jrh.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+ " and ac.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
////						+ " and tvt.nsitecode = " + userInfo.getNmastersitecode()+ " and ac.nsitecode = " + userInfo.getNmastersitecode()
////						+ " and acv.nsitecode = " + userInfo.getNmastersitecode()
//						+ " and ac.nregtypecode = " + nregtypecode + " and ac.nregsubtypecode = " + nregsubtypecode
//						+ " and jrh.ntransactionstatus not in ("
//						+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + ","
//						+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ","
//						+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","
//						+ Enumeration.TransactionStatus.RELEASED.gettransactionstatus() + ");";
//		int npendingsamplecount = jdbcTemplate.queryForObject(strtochk, Integer.class);
//		if (npendingsamplecount > 0) {
//				rtval = false;
//		} else {
//				rtval = true;
//		}
//	}
//	return rtval;
//}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> deleteUserroleTemplatemaster(final int napprovalsubtypecode,
			final int ntreeversiontempcode, final int ntreetemplatecode, final int isregneed, final int nregtypecode,
			final int nregsubtypecode, final UserInfo userInfo) throws Exception {
		Map<String, Object> getuserroletemplate = new HashMap<>();
		Map<String, Object> returnMap = new HashMap<>();
		List<Object> lstcolumns = new ArrayList<Object>();
		String selectQuery = " select ntransactionstatus from treeversiontemplate where ntreeversiontempcode = "
				+ ntreeversiontempcode + " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "";
		TreeVersionTemplate oldTreeversion = (TreeVersionTemplate) jdbcUtilityFunction.queryForObject(selectQuery,
				TreeVersionTemplate.class, jdbcTemplate);
		if (oldTreeversion != null) {
			if (oldTreeversion.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
				final String strQuery = "update treeversiontemplate set nstatus ="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " ," + " dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where ntreeversiontempcode = "
						+ ntreeversiontempcode + "; " + " update treetemplatetransactionrole set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',  nstatus ="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
						+ " where ntreeversiontempcode = " + ntreeversiontempcode + "";

				selectQuery = " select ntreeversiontempcode, napprovalconfigcode, ntransactionstatus, ntemplatecode, nsampletypecode, nversionno, sversiondescription, dmodifieddate, nsitecode, nstatus from treeversiontemplate where ntreeversiontempcode ="
						+ ntreeversiontempcode + ";";
				List<TreeVersionTemplate> lstTreeVersionTemplate = jdbcTemplate.query(selectQuery,
						new TreeVersionTemplate());

				selectQuery = "select ntemptransrolecode, napprovalconfigcode, nuserrolecode, ntreeversiontempcode, ntransactionstatus, nlevelno, nparentnode, ntemplatecode, schildnode, slevelformat, dmodifieddate, nsitecode, nstatus from TreeTemplateTransactionRole where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntreeversiontempcode ="
						+ ntreeversiontempcode + "";
				List<TreeTemplateTransactionRole> lstTreeTemplateTransactionRole = jdbcTemplate.query(selectQuery,
						new TreeTemplateTransactionRole());
				lstcolumns.add(lstTreeVersionTemplate);
				lstcolumns.add(lstTreeTemplateTransactionRole);

				jdbcTemplate.execute(strQuery);
				auditUtilityFunction.fnInsertListAuditAction(lstcolumns, 1, null,
						Arrays.asList("IDS_DELETEUSERROLETEMPLATE", "IDS_DELETEUSERROLELEVEL"), userInfo);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORDTODELETE",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		if (isregneed == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
			getuserroletemplate.put("nflag", 1);
			getuserroletemplate.put("ntreetemplatecode", ntreetemplatecode);
			getuserroletemplate.put("ntreeversiontempcode", -1);
		} else {
			getuserroletemplate.put("nflag", 2);
			getuserroletemplate.put("ntreetemplatecode", ntreetemplatecode);
			getuserroletemplate.put("nmodulecode", 1);
			getuserroletemplate.put("nregtypecode", nregtypecode);
			getuserroletemplate.put("nregsubtypecode", nregsubtypecode);
			getuserroletemplate.put("napprovalsubtypecode", napprovalsubtypecode);
			getuserroletemplate.put("ntreeversiontempcode", -1);
		}
		returnMap.putAll((Map<String, Object>) getUserroletemplate(getuserroletemplate, userInfo).getBody());
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> approveUserroleTemplatemaster(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		final String sQuery = " lock  table lockuserroletemplate " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final int ntreeversiontempcode = (int) inputMap.get("ntreeversiontempcode");
		final int ntreetemplatecode = (int) inputMap.get("ntreetemplatecode");
		final int napprovalsubtypecode = (int) inputMap.get("napprovalsubtypecode");
		final int isregneed = (int) inputMap.get("isregneed");
		final int nregtypecode = (int) inputMap.get("nregtypecode");
		final int nregsubtypecode = (int) inputMap.get("nregsubtypecode");

		Map<String, Object> getuserroletemplate = new HashMap<>();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> approvedUserroletemplate = new ArrayList<>();
		final List<Object> retiredUserroletemplate = new ArrayList<>();
		Map<String, Object> returnMap = new HashMap<>();
		int treetemplatetransactionrole = 0;

		String insertQuery = "";
		int nversion = 1;
		String strQuery = "";
		String strOldVersionQuery = "select tvt.ntreeversiontempcode, tvt.napprovalconfigcode, tvt.ntransactionstatus, tvt.ntemplatecode, tvt.nsampletypecode, tvt.nversionno, tvt.sversiondescription, tvt.dmodifieddate, tvt.nsitecode, tvt.nstatus, max(tttr.nuserrolecode) nuserrolecode"
				+ " from treeversiontemplate tvt, treetemplatetransactionrole tttr where"
				+ " tvt.ntreeversiontempcode = tttr.ntreeversiontempcode and tvt.ntreeversiontempcode = "
				+ ntreeversiontempcode + " and tvt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tttr.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tttr.nsitecode = "
				+ userInfo.getNmastersitecode() + "  group by tvt.ntreeversiontempcode  order by 1 ";
		List<TreeVersionTemplate> lstoldTreeTemplateVersion = (List<TreeVersionTemplate>) jdbcTemplate
				.query(strOldVersionQuery, new TreeVersionTemplate());

		String strOldQuery = "select ntreeversiontempcode, napprovalconfigcode, ntransactionstatus, ntemplatecode, nsampletypecode, nversionno, sversiondescription, dmodifieddate, nsitecode, nstatus from treeversiontemplate where ntreeversiontempcode ="
				+ ntreeversiontempcode + " and nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ ";";
		List<TreeVersionTemplate> lstoldTreeversion = (List<TreeVersionTemplate>) jdbcTemplate.query(strOldQuery,
				new TreeVersionTemplate());

		if (!lstoldTreeversion.isEmpty()) {
			// ALPD-2278
			String anlaystvalidate = " select nneedanalyst, napprovalconfigcode from approvalconfig where napprovalsubtypecode = "
					+ napprovalsubtypecode + " and nregtypecode = " + nregtypecode + " and nregsubtypecode = "
					+ nregsubtypecode + " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode = " + userInfo.getNmastersitecode() + "";
			ApprovalConfig objanalystvalidation = (ApprovalConfig) jdbcUtilityFunction.queryForObject(anlaystvalidate,
					ApprovalConfig.class, jdbcTemplate);

			final int napprovalconfigcode = (objanalystvalidation != null)
					? objanalystvalidation.getNapprovalconfigcode()
					: 0;

			String sequenceno = "select nsequenceno from seqnoconfigurationmaster where stablename = N'treetemplatetransactionrole'";
			treetemplatetransactionrole = jdbcTemplate.queryForObject(sequenceno, Integer.class);

			if (lstoldTreeversion.get(0).getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT
					.gettransactionstatus()) {
				strQuery = "select tvt.ntreeversiontempcode,nversionno,tvt.sversiondescription,tvt.ntransactionstatus from treeversiontemplate tvt ,treetemplatetransactionrole ttr where tvt.ntreeversiontempcode=ttr.ntreeversiontempcode  "
						+ " and tvt.ntemplatecode =" + ntreetemplatecode + " and tvt.ntransactionstatus ="
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and tvt.nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tvt.nsitecode = "
						+ userInfo.getNmastersitecode() + " and ttr.nsitecode = " + userInfo.getNmastersitecode() + ""
						+ " and ttr.napprovalconfigcode = " + napprovalconfigcode
						+ " order by  tvt.ntreeversiontempcode asc";

				List<TreeVersionTemplate> lstntreeversiontempcode = (List<TreeVersionTemplate>) jdbcTemplate
						.query(strQuery, new TreeVersionTemplate());
				if (!lstntreeversiontempcode.isEmpty()) {
					insertQuery += " update treeversiontemplate set ntransactionstatus ="
							+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + ", dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where ntreeversiontempcode ="
							+ lstntreeversiontempcode.get(0).getNtreeversiontempcode() + ";";
					insertQuery += "update treetemplatetransactionrole set dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',  ntransactionstatus="
							+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus()
							+ " where ntreeversiontempcode =" + lstntreeversiontempcode.get(0).getNtreeversiontempcode()
							+ ";";
				}
				Object objversion = projectDAOSupport.fnGetVersion(userInfo.getNformcode(), napprovalconfigcode, null,
						SeqNoUserRoleTemplateVersion.class, userInfo.getNmastersitecode(), userInfo);
				if (objversion != null) {
					nversion = Integer.parseInt(BeanUtils.getProperty(objversion, "versionno"));
				}

				insertQuery += "Update treeversiontemplate set ntransactionstatus ="
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ",nversionno= " + nversion
						+ " , dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'"
						+ " where ntreeversiontempcode =" + ntreeversiontempcode + ";";
				insertQuery += "update treetemplatetransactionrole set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',  ntransactionstatus="
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
						+ " where ntreeversiontempcode =" + ntreeversiontempcode + ";";

				if (objanalystvalidation != null && objanalystvalidation
						.getNneedanalyst() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					strQuery = "select ntemptransrolecode, napprovalconfigcode, nuserrolecode, ntreeversiontempcode, ntransactionstatus, nlevelno, nparentnode, ntemplatecode, schildnode, slevelformat, dmodifieddate, nsitecode, nstatus"
							+ " from treetemplatetransactionrole where ntreeversiontempcode  = " + ntreeversiontempcode
							+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and nsitecode = " + userInfo.getNmastersitecode() + " order by nlevelno desc";
					List<TreeTemplateTransactionRole> lsttreeTransaction = (List<TreeTemplateTransactionRole>) jdbcTemplate
							.query(strQuery, new TreeTemplateTransactionRole());

					strQuery = "select nuserrolecode, nneedapprovalflow, nneedresultflow, dmodifieddate, nsitecode, nstatus, nneedprojectflow from userroleconfig where nneedresultflow = "
							+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
							+ userInfo.getNmastersitecode() + " ";
					List<UserRole> lstUserRole = (List<UserRole>) jdbcTemplate.query(strQuery, new UserRole());
					if (!lstUserRole.isEmpty()) {

						insertQuery += "update seqnoconfigurationmaster set nsequenceno="
								+ (treetemplatetransactionrole + 1)
								+ " where stablename='treetemplatetransactionrole';";

						final String strInsert = "Insert into treetemplatetransactionrole (ntemptransrolecode, napprovalconfigcode, nuserrolecode,"
								+ " ntreeversiontempcode, ntransactionstatus, nlevelno, nparentnode, ntemplatecode, schildnode, slevelformat,"
								+ " dmodifieddate, nsitecode, nstatus) values (" + (treetemplatetransactionrole + 1)
								+ "," + napprovalconfigcode + "," + lstUserRole.get(0).getNuserrolecode() + ","
								+ ntreeversiontempcode + "," + ""
								+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ","
								+ (lsttreeTransaction.get(0).getNlevelno() + 1) + ","
								+ lsttreeTransaction.get(0).getNtemptransrolecode() + "," + ntreetemplatecode + ",-1,"
								+ "'" + lsttreeTransaction.get(0).getSlevelformat() + "1/" + "'," + "'"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
								+ userInfo.getNmastersitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

						insertQuery += strInsert;
						insertQuery += "update treetemplatetransactionrole set dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',  schildnode ="
								+ (treetemplatetransactionrole + 1) + " where ntemptransrolecode ="
								+ lsttreeTransaction.get(0).getNtemptransrolecode() + ";";

					} else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ADDRESULTENTRYTOAPPROVE",
								userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
				}
				jdbcTemplate.execute(insertQuery);
				if (objanalystvalidation != null && objanalystvalidation
						.getNneedanalyst() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

					List<TreeVersionTemplate> lstnewTreeTemplateVersion = (List<TreeVersionTemplate>) jdbcTemplate
							.query(strOldVersionQuery, new TreeVersionTemplate());

					auditUtilityFunction.fnInsertAuditAction(lstnewTreeTemplateVersion, 2, lstoldTreeTemplateVersion,
							Arrays.asList("IDS_APPROVEUSERROLETEMPLATE"), userInfo);

				}
				if (!lstntreeversiontempcode.isEmpty()) {
					String statusstr = "select ntreeversiontempcode, napprovalconfigcode, ntransactionstatus, ntemplatecode, nsampletypecode, nversionno, sversiondescription, dmodifieddate, nsitecode, nstatus from treeversiontemplate where ntreeversiontempcode = "
							+ lstntreeversiontempcode.get(0).getNtreeversiontempcode() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
					List<TreeVersionTemplate> lstnewTreeVersion = jdbcTemplate.query(statusstr,
							new TreeVersionTemplate());

					approvedUserroletemplate.add(lstoldTreeversion);
					approvedUserroletemplate.add(lstntreeversiontempcode);
					retiredUserroletemplate.add(lstoldTreeversion);
					// ALPD-965
					retiredUserroletemplate.add(lstnewTreeVersion);
					multilingualIDList.add("IDS_RETIREUSERROLETEMPLATE");
					auditUtilityFunction.fnInsertListAuditAction(retiredUserroletemplate, 2, approvedUserroletemplate,
							multilingualIDList, userInfo);
				}

				if (isregneed == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
					getuserroletemplate.put("nflag", 1);
					getuserroletemplate.put("ntreetemplatecode", ntreetemplatecode);
					getuserroletemplate.put("ntreeversiontempcode", ntreeversiontempcode);
				} else {
					getuserroletemplate.put("nflag", 2);
					getuserroletemplate.put("ntreetemplatecode", ntreetemplatecode);
					getuserroletemplate.put("nmodulecode", 1);
					getuserroletemplate.put("nregtypecode", nregtypecode);
					getuserroletemplate.put("nregsubtypecode", nregsubtypecode);
					getuserroletemplate.put("napprovalsubtypecode", napprovalsubtypecode);
					getuserroletemplate.put("ntreeversiontempcode", ntreeversiontempcode);
				}
				returnMap.putAll((Map<String, Object>) getUserroletemplate(getuserroletemplate, userInfo).getBody());
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORDTOAPPROVE",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

}
