package com.agaramtech.qualis.organization.service.usermapping;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
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

import com.agaramtech.qualis.configuration.model.ApprovalConfig;
import com.agaramtech.qualis.configuration.model.ApprovalSubType;
import com.agaramtech.qualis.configuration.model.TreeTemplateTransactionRole;
import com.agaramtech.qualis.configuration.model.TreeVersionTemplate;
import com.agaramtech.qualis.credential.model.Site;
import com.agaramtech.qualis.credential.model.UserRole;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationSubType;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationType;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.organization.model.UserMapping;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

/**
 * This Class is used to perform CRUD Operations on usermapping Table
 *
 * @author ATE169
 * @version 9.0.0.1
 */
@AllArgsConstructor
@Repository
public class UserMappingDAOImpl implements UserMappingDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserMappingDAOImpl.class);

	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final FTPUtilityFunction ftpUtilityFunction;

	/**
	 * This Method is used to get the usermapping based on userroletemplate
	 *
	 * @param objMap [Map<String,Object>] contains values of flag value
	 *               ,userInfo,napprovalsubtypecode,regtypecode,nregsubtypecode,treeversioncode
	 * @return a response entity with list of sites,roles and users with respective
	 *         roles
	 * @throws Exception thrown from UserMappingDAOImpl
	 */

	@Override
	public ResponseEntity<Object> getUserMapping(Map<String, Object> objMap) throws Exception {

		int nFlag = 1;
		if (objMap.containsKey("nFlag")) {
			nFlag = (int) objMap.get("nFlag");
		}
		Map<String, Object> mapReturn = new HashMap<>();
		Map<String, Object> mapReturnvalue = new HashMap<>();
		final ObjectMapper mapper = new ObjectMapper();

		final UserInfo userInfo = mapper.convertValue(objMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		switch (nFlag) {
		case 1:// initial load
		{

			int nregtypecode = -1;
			int nregsubtypecode = -1;

			String strQuery = "select distinct ast.napprovalsubtypecode,ast.nisregsubtypeconfigneed,ast.ntemplatecode,ast.nstatus, "
					+ " coalesce(ast.jsondata->'approvalsubtypename'->>'" + userInfo.getSlanguagetypecode() + "',"
					+ " ast.jsondata->'approvalsubtypename'->>'en-US') as ssubtypename "
					+ " from treetemplatetransactionrole ttt,treeversiontemplate tvt,approvalconfig ac,approvalsubtype ast"
					+ " where ttt.ntreeversiontempcode=tvt.ntreeversiontempcode and ac.napprovalconfigcode=ttt.napprovalconfigcode"
					+ " and ast.napprovalsubtypecode=ac.napprovalsubtypecode and tvt.ntransactionstatus="
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and ac.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ast.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tvt.nsitecode ="
					+ userInfo.getNmastersitecode() + "  and ttt.nsitecode =" + userInfo.getNmastersitecode()
					+ " and ac.nsitecode =" + userInfo.getNmastersitecode() + "  and tvt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ttt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "  and ast.napprovalsubtypecode not in ("
					+ Enumeration.ApprovalSubType.STUDYPLANAPPROVAL.getnsubtype() + "," + " "
					+ Enumeration.ApprovalSubType.PROTOCOLAPPROVAL.getnsubtype() + ") order by ast.ntemplatecode asc ";
			LOGGER.info(strQuery);
			List<ApprovalSubType> lstApprovalSubType = jdbcTemplate.query(strQuery, new ApprovalSubType());

			mapReturnvalue.put("Approvalsubtype", lstApprovalSubType);
			Map<String, Object> approvalSubtypeValue = new HashMap<String, Object>();

			if (!lstApprovalSubType.isEmpty()) {
				approvalSubtypeValue.put("value", lstApprovalSubType.get(0).getNapprovalsubtypecode());
				approvalSubtypeValue.put("label", lstApprovalSubType.get(0).getSsubtypename());
				approvalSubtypeValue.put("item", lstApprovalSubType.get(0));
			}

			if (lstApprovalSubType.size() > 0) {
				strQuery = "select distinct rt.nregtypecode,coalesce(rt.jsondata->'sregtypename'->>'"
						+ userInfo.getSlanguagetypecode() + "',rt.jsondata->'sregtypename'->>'en-US') as sregtypename "
						+ "from treetemplatetransactionrole ttt,treeversiontemplate tvt,approvalconfig ac,registrationtype rt "
						+ "where ttt.ntreeversiontempcode=tvt.ntreeversiontempcode "
						+ "and ac.napprovalconfigcode=ttt.napprovalconfigcode and tvt.ntransactionstatus="
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
						+ " and rt.nregtypecode=ac.nregtypecode and ac.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tvt.nsitecode = "
						+ userInfo.getNmastersitecode() + " and ttt.nsitecode =" + userInfo.getNmastersitecode()
						+ " and ac.nsitecode = " + userInfo.getNmastersitecode() + " and rt.nsitecode = "
						+ userInfo.getNmastersitecode() + "  and rt.nregtypecode!= CAST('"
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
						+ "' AS SMALLINT) and tvt.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ttt.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ac.napprovalsubtypecode="
						+ lstApprovalSubType.get(0).getNapprovalsubtypecode() + " ";

				List<RegistrationType> lstRegistrationType = jdbcTemplate.query(strQuery, new RegistrationType());

				mapReturnvalue.put("RegistrationType", lstRegistrationType);
				Map<String, Object> regComboValue = new HashMap<String, Object>();
				Map<String, Object> regSubcomboValue = new HashMap<String, Object>();
				Map<String, Object> templateVersionvalue = new HashMap<String, Object>();

				if (lstRegistrationType.size() > 0) {

					strQuery = "select distinct rst.nregsubtypecode,rst.nregtypecode,rst.nstatus,rst.nsitecode,rst.nsorter,coalesce(rst.jsondata->'sregsubtypename'->>'"
							+ userInfo.getSlanguagetypecode()
							+ "',rst.jsondata->'sregsubtypename'->>'en-US') as sregsubtypename  "
							+ "from treetemplatetransactionrole ttt,treeversiontemplate tvt,approvalconfig ac,registrationsubtype rst "
							+ "where ttt.ntreeversiontempcode=tvt.ntreeversiontempcode "
							+ "and ac.napprovalconfigcode=ttt.napprovalconfigcode and tvt.ntransactionstatus="
							+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
							+ " and rst.nregsubtypecode=ac.nregsubtypecode and ac.nregtypecode="
							+ lstRegistrationType.get(0).getNregtypecode() + " and ac.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rst.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tvt.nsitecode = "
							+ userInfo.getNmastersitecode() + " and ttt.nsitecode =" + userInfo.getNmastersitecode()
							+ " and ac.nsitecode = " + userInfo.getNmastersitecode() + " and rst.nsitecode = "
							+ userInfo.getNmastersitecode() + "  and tvt.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ttt.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rst.nregsubtypecode!="
							+ " CAST('" + Enumeration.TransactionStatus.DELETED.gettransactionstatus()
							+ "' AS SMALLINT)";

					List<RegistrationSubType> lstRegistrationSubType = jdbcTemplate.query(strQuery,
							new RegistrationSubType());
					lstRegistrationSubType = mapper.convertValue(
							commonFunction.getMultilingualMessageList(lstRegistrationSubType,
									Arrays.asList("sregsubtypename"), userInfo.getSlanguagefilename()),
							new TypeReference<List<RegistrationSubType>>() {
							});
					mapReturnvalue.put("RegistrationSubType", lstRegistrationSubType);
					regComboValue.put("value", lstRegistrationType.get(0).getNregtypecode());
					regComboValue.put("label", lstRegistrationType.get(0).getSregtypename());

					nregtypecode = lstRegistrationType.get(0).getNregtypecode();
					if (lstRegistrationSubType.size() > 0) {
						nregsubtypecode = lstRegistrationSubType.get(0).getNregsubtypecode();
						regSubcomboValue.put("value", lstRegistrationSubType.get(0).getNregsubtypecode());
						regSubcomboValue.put("label", lstRegistrationSubType.get(0).getSregsubtypename());
					} else {
						nregsubtypecode = -1;
					}
				} else {
					nregtypecode = -1;
				}

				strQuery = " select distinct tvt.ntreeversiontempcode,tvt.ntemplatecode,tvt.sversiondescription ||'('|| tvt.nversionno||')' as sversiondescription, ast.napprovalsubtypecode,tvt.ntransactionstatus "
						+ " from treeversiontemplate tvt, approvalsubtype ast,treetemplatetransactionrole ttr,approvalconfig ac "
						+ " where tvt.ntemplatecode = ast.ntemplatecode and tvt.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and ac.napprovalsubtypecode="
						+ lstApprovalSubType.get(0).getNapprovalsubtypecode() + " and ac.nregtypecode=" + nregtypecode
						+ " and ac.nregsubtypecode=" + nregsubtypecode
						+ " and ac.napprovalconfigcode=ttr.napprovalconfigcode and ttr.ntreeversiontempcode=tvt.ntreeversiontempcode "
						+ " and tvt.ntransactionstatus != " + Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
						+ " " + " and ast.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " " + " and ttr.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " " + " and ac.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and tvt.ntemplatecode = " + lstApprovalSubType.get(0).getNtemplatecode() + " "
						+ " and tvt.nsitecode =" + userInfo.getNmastersitecode() + "  and ttr.nsitecode = "
						+ userInfo.getNmastersitecode() + "  and  ac.nsitecode = " + userInfo.getNmastersitecode() + " "
						+ " order by ntreeversiontempcode desc";

				List<TreeVersionTemplate> lstTreeVersion = jdbcTemplate.query(strQuery, new TreeVersionTemplate());

				if (lstTreeVersion.size() != 0) {
					strQuery = " Select nsitecode,ssitename,ssiteaddress from site where nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ "  and nsitecode>0 order by nsitecode asc";

					List<Site> lstSite = jdbcTemplate.query(strQuery, new Site());

					mapReturnvalue.put("Site", lstSite);
					mapReturnvalue.put("selectedSite", lstSite.get(lstSite.size() - 1));
					mapReturn = getUserRoles(lstSite.get(lstSite.size() - 1).getNsitecode(),
							lstTreeVersion.get(0).getNtreeversiontempcode());
					templateVersionvalue.put("label", lstTreeVersion.get(0).getSversiondescription());
					templateVersionvalue.put("value", lstTreeVersion.get(0).getNtreeversiontempcode());
					templateVersionvalue.put("item", lstTreeVersion.get(0));
				} else {
					mapReturnvalue.put("Site", new ArrayList<>());
					mapReturnvalue.put("selectedSite", new HashMap<>());
				}

				mapReturnvalue.put("approvalSubTypeValue", approvalSubtypeValue);
				mapReturnvalue.put("registrationTypeValue", regComboValue);
				mapReturnvalue.put("registrationSubTypeValue", regSubcomboValue);
				mapReturnvalue.put("TreeVersion", lstTreeVersion);
				mapReturnvalue.put("templateVersionValue", templateVersionvalue);
				mapReturnvalue.put("realApprovalSubTypeValue", approvalSubtypeValue);
				mapReturnvalue.put("realRegistrationTypeValue", regComboValue);
				mapReturnvalue.put("realRegistrationSubTypeValue", regSubcomboValue);
				mapReturnvalue.put("realTemplateVersionValue", templateVersionvalue);
				mapReturnvalue.putAll(mapReturn);

				final String str = "select * from approvalconfig where napprovalsubtypecode="
						+ lstApprovalSubType.get(0).getNapprovalsubtypecode() + " and nregtypecode=" + nregtypecode
						+ " and nregsubtypecode=" + nregsubtypecode + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

				List<ApprovalConfig> lstapprovalconfig = jdbcTemplate.query(str, new ApprovalConfig());

				if (lstapprovalconfig.size() > 0) {

					mapReturnvalue.put("approvalConfigCode", lstapprovalconfig.get(0).getNapprovalconfigcode());
				} else {

					mapReturnvalue.put("approvalConfigCode", 0);
				}
			}

		}
		break;
		case 2: {// approval sub type change

			int napprovalsubtypecode = (int) objMap.get("napprovalsubtypecode");
			int ntreeTemplateCode = (int) objMap.get("ntemplatecode");
			int nregtypecode = -1;
			int nregsubtypecode = -1;

			String strQuery = "select distinct rt.nregtypecode,coalesce(rt.jsondata->'sregtypename'->>'"
					+ userInfo.getSlanguagetypecode() + "',rt.jsondata->'sregtypename'->>'en-US') as sregtypename "
					+ "from treetemplatetransactionrole ttt,treeversiontemplate tvt,approvalconfig ac,registrationtype rt "
					+ "where ttt.ntreeversiontempcode=tvt.ntreeversiontempcode "
					+ "and ac.napprovalconfigcode=ttt.napprovalconfigcode and tvt.ntransactionstatus="
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
					+ " and rt.nregtypecode=ac.nregtypecode and ac.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and ttt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and tvt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and tvt.nsitecode = "
					+ userInfo.getNmastersitecode() + " " + " and ttt.nsitecode = " + userInfo.getNmastersitecode()
					+ " " + " and ac.nsitecode = " + userInfo.getNmastersitecode() + " " + " and rt.nsitecode = "
					+ userInfo.getNmastersitecode() + " " + " and rt.nregtypecode!=("
					+ (short) Enumeration.TransactionStatus.DELETED.gettransactionstatus()
					+ ")::smallint  and tvt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ttt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ac.napprovalsubtypecode=" + napprovalsubtypecode + " ";

			List<RegistrationType> lstRegistrationType = jdbcTemplate.query(strQuery, new RegistrationType());

			mapReturnvalue.put("RegistrationType", lstRegistrationType);

			if (lstRegistrationType != null && lstRegistrationType.size() > 0) {

				strQuery = "select distinct rst.nregsubtypecode,rst.nregtypecode,rst.nstatus,rst.nsitecode,rst.nsorter,coalesce(rst.jsondata->'sregsubtypename'->>'"
						+ userInfo.getSlanguagetypecode()
						+ "',rst.jsondata->'sregsubtypename'->>'en-US') as sregsubtypename  "
						+ "from treetemplatetransactionrole ttt,treeversiontemplate tvt,approvalconfig ac,registrationsubtype rst "
						+ "where ttt.ntreeversiontempcode=tvt.ntreeversiontempcode "
						+ "and ac.napprovalconfigcode=ttt.napprovalconfigcode and tvt.ntransactionstatus="
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
						+ " and rst.nregsubtypecode=ac.nregsubtypecode and ac.nregtypecode="
						+ lstRegistrationType.get(0).getNregtypecode() + " and ac.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and rst.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and ttt.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and tvt.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and tvt.nsitecode = "
						+ userInfo.getNmastersitecode() + "" + " and ttt.nsitecode = " + userInfo.getNmastersitecode()
						+ "" + " and ac.nsitecode = " + userInfo.getNmastersitecode() + "" + " and rst.nsitecode = "
						+ userInfo.getNmastersitecode() + "" + " and rst.nregsubtypecode!=("
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ")::smallint";

				List<RegistrationSubType> lstRegistrationSubType = jdbcTemplate.query(strQuery,
						new RegistrationSubType());
				lstRegistrationSubType = mapper.convertValue(
						commonFunction.getMultilingualMessageList(lstRegistrationSubType,
								Arrays.asList("sregsubtypename"), userInfo.getSlanguagefilename()),
						new TypeReference<List<RegistrationSubType>>() {
						});
				mapReturnvalue.put("RegistrationSubType", lstRegistrationSubType);

				nregtypecode = lstRegistrationType.get(0).getNregtypecode();

				if (lstRegistrationSubType != null && lstRegistrationSubType.size() > 0) {
					nregsubtypecode = lstRegistrationSubType.get(0).getNregsubtypecode();
				}

			}
			strQuery = " select distinct tvt.ntreeversiontempcode,tvt.ntemplatecode, tvt.sversiondescription ||'('|| tvt.nversionno||')' as  sversiondescription, ast.napprovalsubtypecode,tvt.ntransactionstatus "
					+ " from treeversiontemplate tvt, approvalsubtype ast,treetemplatetransactionrole ttr,approvalconfig ac "
					+ " where tvt.ntemplatecode = ast.ntemplatecode and tvt.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and ttr.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and ac.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and ac.napprovalsubtypecode=" + napprovalsubtypecode + " and ac.nregtypecode=" + nregtypecode
					+ " and ac.nregsubtypecode=" + nregsubtypecode
					+ " and ac.napprovalconfigcode=ttr.napprovalconfigcode"
					+ " and ttr.ntreeversiontempcode=tvt.ntreeversiontempcode " + " and tvt.ntransactionstatus != "
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + " and ast.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tvt.ntemplatecode = "
					+ ntreeTemplateCode + " order by ntreeversiontempcode desc";

			List<TreeVersionTemplate> lstTreeVersion = jdbcTemplate.query(strQuery, new TreeVersionTemplate());

			mapReturnvalue.put("TreeVersion", lstTreeVersion);

			final String str = "select * from approvalconfig where napprovalsubtypecode=" + napprovalsubtypecode
					+ " and nregtypecode=" + nregtypecode + " and nregsubtypecode=" + nregsubtypecode + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			List<ApprovalConfig> lstapprovalconfig = jdbcTemplate.query(str, new ApprovalConfig());

			if (lstapprovalconfig.size() > 0) {

				mapReturnvalue.put("approvalConfigCode", lstapprovalconfig.get(0).getNapprovalconfigcode());
			} else {

				mapReturnvalue.put("approvalConfigCode", 0);
			}

		}
		break;
		case 3: {// reg type change

			int napprovalsubtypecode = (int) objMap.get("napprovalsubtypecode");
			int ntreeTemplateCode = (int) objMap.get("ntemplatecode");
			int nregTypeCode = (int) objMap.get("nregtypecode");
			int nregsubtypecode = -1;

			String strQuery = "select distinct rst.nregsubtypecode,rst.nregtypecode,rst.nstatus,rst.nsitecode,rst.nsorter,coalesce(rst.jsondata->'sregsubtypename'->>'"
					+ userInfo.getSlanguagetypecode()
					+ "',rst.jsondata->'sregsubtypename'->>'en-US') as sregsubtypename  "
					+ "from treetemplatetransactionrole ttt,treeversiontemplate tvt,approvalconfig ac,registrationsubtype rst "
					+ "where ttt.ntreeversiontempcode=tvt.ntreeversiontempcode "
					+ "and ac.napprovalconfigcode=ttt.napprovalconfigcode and tvt.ntransactionstatus="
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
					+ " and rst.nregsubtypecode=ac.nregsubtypecode and ac.nregtypecode=" + nregTypeCode
					+ " and ac.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and rst.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ttt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tvt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ttt.nsitecode = " + userInfo.getNmastersitecode() + "" + " and ac.nsitecode = "
					+ userInfo.getNmastersitecode() + "" + " and rst.nsitecode = " + userInfo.getNmastersitecode() + ""
					+ " and tvt.nsitecode = " + userInfo.getNmastersitecode() + " and rst.nregsubtypecode!=("
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ")::smallint";

			List<RegistrationSubType> lstRegistrationSubType = jdbcTemplate.query(strQuery, new RegistrationSubType());
			lstRegistrationSubType = mapper
					.convertValue(
							commonFunction.getMultilingualMessageList(lstRegistrationSubType,
									Arrays.asList("sregsubtypename"), userInfo.getSlanguagefilename()),
							new TypeReference<List<RegistrationSubType>>() {
							});
			mapReturnvalue.put("RegistrationSubType", lstRegistrationSubType);

			if (lstRegistrationSubType != null && lstRegistrationSubType.size() > 0) {
				nregsubtypecode = lstRegistrationSubType.get(0).getNregsubtypecode();
			}

			strQuery = " select distinct tvt.ntreeversiontempcode,tvt.ntemplatecode,tvt.sversiondescription ||'('|| tvt.nversionno||')' as  sversiondescription, ast.napprovalsubtypecode,tvt.ntransactionstatus "
					+ " from treeversiontemplate tvt, approvalsubtype ast,treetemplatetransactionrole ttr,approvalconfig ac "
					+ " where tvt.ntemplatecode = ast.ntemplatecode and tvt.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and ttr.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ac.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ac.napprovalsubtypecode="
					+ napprovalsubtypecode + " and ac.nregtypecode=" + nregTypeCode + " and ac.nregsubtypecode="
					+ nregsubtypecode + " and ac.napprovalconfigcode=ttr.napprovalconfigcode"
					+ " and ttr.ntreeversiontempcode=tvt.ntreeversiontempcode " + " and tvt.ntransactionstatus != "
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + " and ast.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tvt.ntemplatecode = "
					+ ntreeTemplateCode + " order by ntreeversiontempcode desc";

			List<TreeVersionTemplate> lstTreeVersion = jdbcTemplate.query(strQuery, new TreeVersionTemplate());

			mapReturnvalue.put("TreeVersion", lstTreeVersion);

			final String str = "select * from approvalconfig where napprovalsubtypecode=" + napprovalsubtypecode
					+ " and nregtypecode=" + nregTypeCode + " and nregsubtypecode=" + nregsubtypecode + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			List<ApprovalConfig> lstapprovalconfig = jdbcTemplate.query(str, new ApprovalConfig());

			if (lstapprovalconfig.size() > 0) {

				mapReturnvalue.put("approvalConfigCode", lstapprovalconfig.get(0).getNapprovalconfigcode());
			} else {

				mapReturnvalue.put("approvalConfigCode", 0);
			}

		}
		break;
		case 4:// reg sub type change
		{
			int napprovalsubtypecode = (int) objMap.get("napprovalsubtypecode");
			int nregtypecode = (int) objMap.get("nregtypecode");
			int nregsubtypecode = (int) objMap.get("nregsubtypecode");
			int ntreeTemplateCode = (int) objMap.get("ntemplatecode");

			final String strQuery = " select distinct tvt.ntreeversiontempcode,tvt.ntemplatecode,tvt.sversiondescription ||'('|| tvt.nversionno||')' as sversiondescription, ast.napprovalsubtypecode,tvt.ntransactionstatus,tvt.napprovalconfigcode"
					+ " from treeversiontemplate tvt, approvalsubtype ast,treetemplatetransactionrole ttr,approvalconfig ac "
					+ " where tvt.ntemplatecode = ast.ntemplatecode  and tvt.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and ttr.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ac.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ac.napprovalsubtypecode="
					+ napprovalsubtypecode + " and ac.nregtypecode=" + nregtypecode + " and ac.nregsubtypecode="
					+ nregsubtypecode + " and ac.napprovalconfigcode=ttr.napprovalconfigcode"
					+ " and ttr.ntreeversiontempcode=tvt.ntreeversiontempcode" + " and tvt.ntransactionstatus != "
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + " and ast.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tvt.ntemplatecode = "
					+ ntreeTemplateCode + " order by ntreeversiontempcode desc";

			List<TreeVersionTemplate> lstTreeVersion = jdbcTemplate.query(strQuery, new TreeVersionTemplate());
			mapReturnvalue.put("TreeVersion", lstTreeVersion);

			mapReturnvalue.put("TreeVersion", lstTreeVersion);

			final String str = "select * from approvalconfig where napprovalsubtypecode=" + napprovalsubtypecode
					+ " and nregtypecode=" + nregtypecode + " and nregsubtypecode=" + nregsubtypecode + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			List<ApprovalConfig> lstapprovalconfig = jdbcTemplate.query(str, new ApprovalConfig());

			if (lstapprovalconfig.size() > 0) {

				mapReturnvalue.put("approvalConfigCode", lstapprovalconfig.get(0).getNapprovalconfigcode());
			} else {

				mapReturnvalue.put("approvalConfigCode", 0);
			}
		}
		break;
		case 5:// version change
		{
			int napprovalsubtypecode = (int) objMap.get("napprovalsubtypecode");
			int nregtypecode = (int) objMap.get("nregtypecode");
			int nregsubtypecode = (int) objMap.get("nregsubtypecode");

			final String str = "select * from approvalconfig where napprovalsubtypecode=" + napprovalsubtypecode
					+ " and nregtypecode=" + nregtypecode + " and nregsubtypecode=" + nregsubtypecode + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			List<ApprovalConfig> lstapprovalconfig = jdbcTemplate.query(str, new ApprovalConfig());

			if (lstapprovalconfig.size() > 0) {

				mapReturnvalue.put("approvalConfigCode", lstapprovalconfig.get(0).getNapprovalconfigcode());
			} else {

				mapReturnvalue.put("approvalConfigCode", 0);
			}

		}
		break;
		case 6:// version change
		{
			int napprovalsubtypecode = (int) objMap.get("napprovalsubtypecode");
			int nregtypecode = napprovalsubtypecode == Enumeration.ApprovalSubType.STUDYPLANAPPROVAL.getnsubtype() ? -1
					: (int) objMap.get("nregtypecode");
			int nregsubtypecode = napprovalsubtypecode == Enumeration.ApprovalSubType.STUDYPLANAPPROVAL.getnsubtype()
					? -1
							: (int) objMap.get("nregsubtypecode");
			int nVersionCode = (int) objMap.get("nversioncode");

			final String strQuery = " Select nsitecode,ssitename,ssiteaddress from Site where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode>0 order by nsitecode asc";

			List<Site> lstSite = jdbcTemplate.query(strQuery, new Site());

			mapReturnvalue.put("Site", lstSite);
			mapReturnvalue.put("selectedSite", lstSite.get(lstSite.size() - 1));
			mapReturn = getUserRoles(lstSite.get(lstSite.size() - 1).getNsitecode(), nVersionCode);
			mapReturnvalue.putAll(mapReturn);

			final String str = "select * from approvalconfig where napprovalsubtypecode=" + napprovalsubtypecode
					+ " and nregtypecode=" + nregtypecode + " and nregsubtypecode=" + nregsubtypecode + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			List<ApprovalConfig> lstapprovalconfig = jdbcTemplate.query(str, new ApprovalConfig());

			if (lstapprovalconfig.size() > 0) {

				mapReturnvalue.put("approvalConfigCode", lstapprovalconfig.get(0).getNapprovalconfigcode());
			} else {

				mapReturnvalue.put("approvalConfigCode", 0);
			}

		}
		break;
		default:
			break;
		}

		return new ResponseEntity<Object>(mapReturnvalue, HttpStatus.OK);
	}

	/**
	 * This Method is used to get the list of userroles with ascending levels from
	 * 'treetemplatetransactionrole' based on treetemplateversion
	 *
	 * @param nSiteCode    [int] site Code
	 * @param nVersionCode [int] treetemplateversioncode
	 * @return a response entity with list of user roles
	 * @throws Exception thrown from UserMappingDAOImpl
	 */

	@Override
	public Map<String, Object> getUserRoles(int nSiteCode, int nVersionCode) throws Exception {

		Map<String, Object> mapReturn = new HashMap<String, Object>();
		List<Map<String, Object>> lstUsers = null;
		Map<String, Object> mapUsers = new HashMap<String, Object>();

		String strQuery = "select ntemptransrolecode from treetemplatetransactionrole where ntreeversiontempcode = "
				+ nVersionCode + " and nparentnode = -1 and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

		TreeTemplateTransactionRole objTemplateTransactionRole = (TreeTemplateTransactionRole) jdbcUtilityFunction
				.queryForObject(strQuery, TreeTemplateTransactionRole.class, jdbcTemplate);

		strQuery = "with RECURSIVE url1(ntemptransrolecode ,slevelformat ,nuserrolecode,nparentnode ,schildnode ,ntransactionstatus,nlevelno,ntreeversiontempcode ,ntemplatecode,suserrolename,nparentrolecode) as("
				+ " select ttr.ntemptransrolecode , ttr.slevelformat , ttr.nuserrolecode, ttr.nparentnode , ttr.schildnode , ttr.ntransactionstatus, ttr.nlevelno,"
				+ " ttr.ntreeversiontempcode , ttr.ntemplatecode,urm.suserrolename, -2 as nparentrolecode"
				+ "	from treetemplatetransactionrole ttr,userrole urm" + "	where ttr.ntemptransrolecode= "
				+ objTemplateTransactionRole.getNtemptransrolecode() + "	and  urm.nuserrolecode=ttr.nuserrolecode"
				+ " union all"
				+ "	select url2.ntemptransrolecode,url2.slevelformat,url2.nuserrolecode,url2.nparentnode,url2.schildnode,url2.ntransactionstatus,"
				+ " url2.nlevelno,url2.ntreeversiontempcode,url2.ntemplatecode,urm.suserrolename,url1.nuserrolecode as nparentrolecode"
				+ " from treetemplatetransactionrole url2 ,userrole urm , url1   where url2.nparentnode = url1.ntemptransrolecode and urm.nuserrolecode=url2.nuserrolecode)"
				+ " select ntemptransrolecode ,slevelformat ,nuserrolecode,nparentnode ,schildnode ,ntransactionstatus,nlevelno,ntreeversiontempcode ,ntemplatecode,suserrolename,nparentrolecode from url1 order by nlevelno";

		List<UserRole> lstUserRoles = jdbcTemplate.query(strQuery, new UserRole());

		mapReturn.put("UserRole", lstUserRoles);

		strQuery = "select * from site where nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		+ " and nsitecode=" + nSiteCode;

		final Site objSite = (Site) jdbcUtilityFunction.queryForObject(strQuery, Site.class, jdbcTemplate);
		mapReturn.put("selectedSite", objSite);

		int lastParentUsercode = -2;

		for (int i = 0; i < lstUserRoles.size(); i++) {
			lstUsers = new ArrayList<Map<String, Object>>();

			lstUsers = getChildRoleUsers(lastParentUsercode, nSiteCode, nVersionCode);

			mapUsers.put("" + lstUserRoles.get(i).getNuserrolecode(), lstUsers);

			if (lstUsers.size() > 0) {
				lastParentUsercode = (int) lstUsers.get(lstUsers.size() - 1).get("nusermappingcode");
				mapReturn.put("selectedUser_" + lstUserRoles.get(i).getNuserrolecode(),
						lstUsers.get(lstUsers.size() - 1));
			} else {
				lastParentUsercode = -1;
				mapReturn.put("selectedUser_" + lstUserRoles.get(i).getNuserrolecode(), new HashMap<>());
			}
		}
		mapReturn.putAll(mapUsers);
		return mapReturn;
	}

	private List<Map<String, Object>> getChildRoleUsers(int nparUserMappingCode, int nSiteCode, int nVersionCode)
			throws Exception {

		final String strQuery = "select u.nusercode as id,um.nusermappingcode as nusermappingcode,um.nchildrolecode,um.nchildusercode,um.nparusermappingcode,um.nlevel,us.nusersitecode as usersitecode,"
				+ " u.nusercode as \"UserCode\",u.sloginid as \"LoginId\","
				+ " CONCAT(u.sfirstname,' ',u.slastname) as \"Name\",d.sdeptname"
				+ " from usermapping um,userssite us,users u,department d"
				+ " where um.nchildusersitecode = us.nusersitecode and us.nusercode = u.nusercode"
				+ " and um.nparusermappingcode=" + nparUserMappingCode + " " + " and um.nsitecode = " + nSiteCode + " "
				+ " and um.nversioncode = " + nVersionCode + " and um.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "	and d.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "	and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "	and us.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and d.ndeptcode=u.ndeptcode"
				+ " order by um.nusermappingcode asc";

		return jdbcTemplate.queryForList(strQuery);
	}

	/**
	 * This Method is used to get the list of child users/Next Level Users based on
	 * parent users/Top level users from usermapping
	 *
	 * @param objMap contains the value of lastParentUsercode,nroleCode
	 * @return a response entity with list of users with respective roles
	 * @throws Exception thrown from UserMappingDAOImpl
	 */

	@Override
	public ResponseEntity<Object> getChildUsers(int lastParentUsercode, int nroleCode, int nSiteCode, int nVersionCode,
			int levelno) throws Exception {

		Map<String, Object> mapReturn = new HashMap<String, Object>();
		List<Map<String, Object>> lstUsers = null;
		Map<String, Object> mapUsers = new HashMap<String, Object>();
		String strQuery = "";

		if (levelno == 1) {
			strQuery = "select ntemptransrolecode from treetemplatetransactionrole where ntreeversiontempcode = "
					+ nVersionCode + " and nparentnode = -1 and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			lastParentUsercode = -2;
		} else {
			// get child levels
			strQuery = "select ttr2.ntemptransrolecode from treetemplatetransactionrole ttr1,treetemplatetransactionrole ttr2"
					+ " where ttr1.ntemptransrolecode=ttr2.nparentnode" + " and ttr1.nstatus=1 and ttr2.nstatus=1"
					+ " and ttr1.ntreeversiontempcode = " + nVersionCode + " and ttr1.nuserrolecode = " + nroleCode;
		}

		TreeTemplateTransactionRole objTemplateTransactionRole = (TreeTemplateTransactionRole) jdbcUtilityFunction
				.queryForObject(strQuery, TreeTemplateTransactionRole.class, jdbcTemplate);

		if (objTemplateTransactionRole != null) {

			strQuery = "with RECURSIVE url1(ntemptransrolecode ,slevelformat ,nuserrolecode,nparentnode ,schildnode ,ntransactionstatus,nlevelno,ntreeversiontempcode ,ntemplatecode,suserrolename) as("
					+ " select ttr.ntemptransrolecode , ttr.slevelformat , ttr.nuserrolecode, ttr.nparentnode , ttr.schildnode , ttr.ntransactionstatus, ttr.nlevelno, ttr.ntreeversiontempcode , ttr.ntemplatecode,urm.suserrolename"
					+ "	from treetemplatetransactionrole ttr,userrole urm" + "	where ttr.ntemptransrolecode= "
					+ objTemplateTransactionRole.getNtemptransrolecode()
					+ "	and  urm.nuserrolecode=ttr.nuserrolecode" + " union all"
					+ "	select url2.ntemptransrolecode,url2.slevelformat,url2.nuserrolecode,url2.nparentnode,url2.schildnode,url2.ntransactionstatus,url2.nlevelno,url2.ntreeversiontempcode,url2.ntemplatecode,urm.suserrolename"
					+ " from treetemplatetransactionrole url2 ,userrole urm , url1   where url2.nparentnode = url1.ntemptransrolecode and urm.nuserrolecode=url2.nuserrolecode)"
					+ " select ntemptransrolecode ,slevelformat ,nuserrolecode,nparentnode ,schildnode ,ntransactionstatus,nlevelno,ntreeversiontempcode ,ntemplatecode,suserrolename from url1 order by nlevelno";

			List<UserRole> lstUserRoles = jdbcTemplate.query(strQuery, new UserRole());

			for (int i = 0; i < lstUserRoles.size(); i++) {
				lstUsers = new ArrayList<Map<String, Object>>();

				lstUsers = getChildRoleUsers(lastParentUsercode, nSiteCode, nVersionCode);

				if (lstUsers.size() > 0) {

					mapUsers.put("" + lstUserRoles.get(i).getNuserrolecode(), lstUsers);

					lastParentUsercode = (int) lstUsers.get(lstUsers.size() - 1).get("nusermappingcode");

					mapReturn.put("selectedUser_" + lstUserRoles.get(i).getNuserrolecode(),
							lstUsers.get(lstUsers.size() - 1));
				} else {
					mapUsers.put("" + lstUserRoles.get(i).getNuserrolecode(), new ArrayList<>());
					mapReturn.put("selectedUser_" + lstUserRoles.get(i).getNuserrolecode(), new HashMap<>());
					lastParentUsercode = -1;
				}
			}
			mapReturn.putAll(mapUsers);

			return new ResponseEntity<Object>(mapReturn, HttpStatus.OK);
		} else {
			return new ResponseEntity<Object>(mapReturn, HttpStatus.OK);
		}
	}

	/**
	 * This Method is used to get the list of all available users from users to be
	 * added under a top level user/parent user
	 *
	 * @param objMap holds the values of
	 *               nparusermappingcode,nsitecode,nversioncode,nuserrolecode
	 * @return a response entity with list of all available users with respective
	 *         roles
	 * @throws Exception thrown from UserMappingDAOImpl
	 */
	@Override
	public ResponseEntity<Object> getAvailableUsers(Map<String, Object> objMap, UserInfo userInfo) throws Exception {

		final String validateTemplate = "select ntreeversiontempcode,ntransactionstatus from treeversiontemplate"
				+ " where nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and napprovalconfigcode = " + objMap.get("napprovalconfigcode") + " and ntreeversiontempcode= "
				+ objMap.get("ntreeversiontempcode");

		TreeVersionTemplate userRoleTemplate = (TreeVersionTemplate) jdbcUtilityFunction
				.queryForObject(validateTemplate, TreeVersionTemplate.class, jdbcTemplate);

		if (userRoleTemplate.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {
			return new ResponseEntity<Object>(commonFunction
					.getMultilingualMessage("IDS_SELECTAPPROVEDUSERROLETEMPLATE", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);

		}
		final String strAvailable = "select us.nusercode as id,us.nusercode as \"UserCode\",us.sloginid as \"LoginId\",CONCAT(us.sfirstname,' ',us.slastname,' ','(',us.sloginid,')') as \"Name\","
				+ "   urs.nsitecode,umr.nusermultirolecode,umr.nuserrolecode,ur.suserrolename,urs.nusersitecode,ttr.nlevelno  "
				+ "	from users us,userrole ur ,userssite urs ,usermultirole umr,treetemplatetransactionrole ttr "
				+ "	where us.nusercode=urs.nusercode and urs.nusersitecode=umr.nusersitecode  "
				+ "	and ur.nuserrolecode=umr.nuserrolecode  and urs.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "	and umr.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "   and us.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " 	and ttr.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and ur.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ "	and ttr.nuserrolecode=ur.nuserrolecode and ttr.ntransactionstatus!="
				+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + " 	and ttr.ntreeversiontempcode="
				+ objMap.get("nversioncode") + "   and umr.nuserrolecode=" + objMap.get("nuserrolecode")
				+ "   and urs.nsitecode=" + objMap.get("nsitecode") + " 	and us.ntransactionstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "	and not exists  "
				+ "	(select ur.suserrolename,um.* from usermapping um,userrole ur where um.nparusermappingcode="
				+ objMap.get("nparusermappingcode") + "  " + "	and um.nchildrolecode=" + objMap.get("nuserrolecode")
				+ "  and ur.nuserrolecode=um.nchildrolecode " + " 	and um.nversioncode=" + objMap.get("nversioncode")
				+ " " + "	and umr.nuserrolecode=um.nchildrolecode and us.nusercode=um.nchildusercode  "
				+ "	and um.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "   and um.nsitecode=" + objMap.get("nsitecode") + ")";

		List<?> lstAvailableRole = jdbcTemplate.queryForList(strAvailable);
		Map<String, Object> rtnObject = new HashMap<>();
		rtnObject.put("AvailableUsers", lstAvailableRole);
		return new ResponseEntity<Object>(rtnObject, HttpStatus.OK);
	}

	/**
	 * This Method is used to add list of available users under a top level
	 * user/parent user
	 *
	 * @param objMap [Map<String, Object>] holds the Values of
	 *               userInfo,nparusermappingcode,levelno,nuserrolecode,nsitecode,nversioncode,usermapping
	 *               which having list of users to be added
	 * @return a response entity with list of users with respective roles
	 * @throws Exception thrown from UserMappingDAOImpl
	 */

	@Override
	public ResponseEntity<Object> addUsers(Map<String, Object> objMap) throws Exception {

		final String sQueryLock = " lock  table lockusermapping " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQueryLock);

		final ObjectMapper mapper = new ObjectMapper();

		final UserInfo userInfo = mapper.convertValue(objMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		List<UserMapping> lsAddedUsers = mapper.convertValue(objMap.get("usermapping"),
				new TypeReference<List<UserMapping>>() {
		});

		int lastParentUsercode = (int) objMap.get("nparusermappingcode");
		int nroleCode = (int) objMap.get("nuserrolecode");
		int nSiteCode = (int) objMap.get("nsitecode");
		int nVersionCode = (int) objMap.get("nversioncode");
		int levelno = (int) objMap.get("levelno");
		
		//ALPD-6051 -L.Subashini
//		int nRegTypeCode = (int) objMap.get("nregtypecode");
//		int nRegSybTypeCode = (int) objMap.get("nregsubtypecode");
		
		List<UserMapping> lstAddMappings = new ArrayList<UserMapping>();
		final List<Object> lstObjAddMappings = new ArrayList<>();
		List<String> multiLinguallist = new ArrayList<String>();

		if ((lsAddedUsers.size() > 0) && (lsAddedUsers != null)) {

			//ALPD-6051 -L.Subashini
//			final String sRoleCode = lsAddedUsers.stream().map(obj -> String.valueOf(obj.getNchildrolecode()))
//					.collect(Collectors.joining(","));
//			final String sUserCode = lsAddedUsers.stream().map(obj -> String.valueOf(obj.getNchildusercode()))
//					.collect(Collectors.joining(","));
//			final String sUserSiteCode = lsAddedUsers.stream().map(obj -> String.valueOf(obj.getNchildusersitecode()))
//					.collect(Collectors.joining(","));
//			final String sParentRoleCode = lsAddedUsers.stream().map(obj -> String.valueOf(obj.getNparentrolecode()))
//					.collect(Collectors.joining(","));
//			final String sParentUserCode = lsAddedUsers.stream().map(obj -> String.valueOf(obj.getNparentusercode()))
//					.collect(Collectors.joining(","));
//			final String sParentSiteCode = lsAddedUsers.stream()
//					.map(obj -> String.valueOf(obj.getNparentusersitecode())).collect(Collectors.joining(","));
			
			//ALPD-6051 -Start - L.Subashini
			//--Below query for 'Already Exists' validation was removed as any unmapped user can be added to any of the
			// top level hierarchical role users.
			
			// ALPD-5421 Added parent rolecode, usercode and sitecode by Vishakh for
			// checking validation including parent details and modified queries
//			final String strquery = "select um.* from usermapping um,approvalconfig ac,treeversiontemplate tvt "
//					+ "where um.napprovalconfigcode =ac.napprovalconfigcode "
//					+ "and ac.napprovalconfigcode =tvt.napprovalconfigcode "
//					+ "and um.nversioncode = tvt.ntreeversiontempcode " + "and um.nchildrolecode in(" + sRoleCode
//					+ ") and um.nchildusercode in(" + sUserCode + ") " + "and um.nchildusersitecode in(" + sUserSiteCode
//					+ ") " + " and um.nparentusercode in (" + sParentUserCode + ") and um.nparentrolecode in ("
//					+ sParentRoleCode + ") and nparentusersitecode in (" + sParentSiteCode + ") "
//					+ "and ac.nregtypecode =" + nRegTypeCode + " and ac.nregsubtypecode =" + nRegSybTypeCode
//					+ " and um.nversioncode =" + nVersionCode + " " + "and um.nstatus ="
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and tvt.nstatus ="
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and ac.nstatus="
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//
//			final List<UserMapping> lstUserMapping = jdbcTemplate.query(strquery, new UserMapping());
//			if (lstUserMapping.size() == 0) {
			//ALPD-6051 -END - L.Subashini
				int seqNoInt = jdbcTemplate.queryForObject(
						"select nsequenceno from seqnoorganisation where stablename='usermapping' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
								Integer.class);
				;
				String strQry = "insert into usermapping (nusermappingcode,nversioncode,napprovalconfigcode,nparentrolecode,"
						+ " nparentusercode,nparentusersitecode,nchildrolecode,nchildusercode,nchildusersitecode,nparusermappingcode,"
						+ " nlevel,dmodifieddate,nsitecode,nstatus) values ";

				for (UserMapping item : lsAddedUsers) {
					UserMapping objMapping = new UserMapping();
					objMapping.setNparentrolecode(item.getNparentrolecode());
					objMapping.setNapprovalconfigcode(item.getNapprovalconfigcode());
					objMapping.setNparentusercode(item.getNparentusercode());
					objMapping.setNparentusersitecode(item.getNparentusersitecode());
					objMapping.setNchildrolecode(item.getNchildrolecode());
					objMapping.setNchildusercode(item.getNchildusercode());
					objMapping.setNchildusersitecode(item.getNchildusersitecode());
					objMapping.setNstatus((short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
					objMapping.setNsitecode(item.getNsitecode());
					objMapping.setNversioncode(item.getNversioncode());
					objMapping.setNparusermappingcode(item.getNparusermappingcode());
					objMapping.setNlevel(item.getNlevel());
					objMapping.setDmodifieddate(dateUtilityFunction.getCurrentDateTime(userInfo));

					multiLinguallist.add("IDS_ADDUSERMAPPING");
					lstAddMappings.add(objMapping);
					seqNoInt++;

					strQry += " (" + seqNoInt + ", " + item.getNversioncode() + ", " + item.getNapprovalconfigcode()
					+ ", " + objMapping.getNparentrolecode() + ", " + objMapping.getNparentusercode() + ", "
					+ objMapping.getNparentusersitecode() + ", " + objMapping.getNchildrolecode() + ", "
					+ objMapping.getNchildusercode() + ", " + objMapping.getNchildusersitecode() + ", "
					+ objMapping.getNparusermappingcode() + ", " + objMapping.getNlevel() + ", '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + item.getNsitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
					// ALPD-5418 Modified code by Vishakh for duplicate user adding issue
					// lstAddMappings = (List<UserMapping>) insertBatch(lstAddMappings,
					// SeqNoOrganisation.class, "nsequenceno");
				}
				strQry = strQry.substring(0, strQry.length() - 1) + "; ";
				strQry += "update seqnoorganisation set nsequenceno=" + seqNoInt
						+ " where stablename='usermapping' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				jdbcTemplate.execute(strQry);
				lstObjAddMappings.add(lstAddMappings);
				auditUtilityFunction.fnInsertListAuditAction(lstObjAddMappings, 1, null, multiLinguallist, userInfo);

			//ALPD-6051 -Start - L.Subashini
//			} else {
//				return new ResponseEntity<>(
//						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
//								userInfo.getSlanguagefilename()),
//						HttpStatus.CONFLICT);
//			}
		    //ALPD-6051 -End - L.Subashini
		}
		return getChildUsers(lastParentUsercode, nroleCode, nSiteCode, nVersionCode, levelno);

	}

	/**
	 * This Method is used to delete a user under a top level user/parent user . If
	 * the user have child users then that also will be deleted
	 *
	 * @param objMap [Map<String, Object>] holds the Values of
	 *               userInfo,nparusermappingcode,nuserrolecode,nsitecode,nversioncode,levelno,nusermappingcode
	 * @return a response entity with list of users with respective roles
	 * @throws Exception thrown from UserMappingDAOImpl
	 */

	@Override
	public ResponseEntity<Object> deleteUsers(Map<String, Object> objMap, final UserInfo userInfo) throws Exception {

		int lastParentUsercode = (int) objMap.get("nparusermappingcode");
		int nroleCode = (int) objMap.get("nuserrolecode");
		int nSiteCode = (int) objMap.get("nsitecode");
		int nVersionCode = (int) objMap.get("nversioncode");
		int levelno = (int) objMap.get("levelno");
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> deletedObjects = new ArrayList<>();
		String strQuery = ";with RECURSIVE tree" + "	as  " + "	( "
				+ "	select  nusermappingcode,nparentrolecode,nparentusercode,nparentusersitecode,nchildrolecode,nchildusercode,"
				+ "	nchildusersitecode,nversioncode,nparusermappingcode,nsitecode,nstatus,nlevel"
				+ " 	from usermapping where" + "	nusermappingcode= " + objMap.get("nusermappingcode")
				+ "	and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "	union all  "
				+ "   select  um1.nusermappingcode,um1.nparentrolecode,um1.nparentusercode,um1.nparentusersitecode,um1.nchildrolecode,um1.nchildusercode,"
				+ " 	um1.nchildusersitecode,um1.nversioncode,um1.nparusermappingcode,um1.nsitecode,um1.nstatus,um1.nlevel"
				+ "	from usermapping um1  "
				+ "	inner join tree tm on  tm.nusermappingcode=um1.nparusermappingcode and um1.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  ) " + " 	select * from tree";
		List<UserMapping> lstUserMapping = jdbcTemplate.query(strQuery, new UserMapping());

		if (lstUserMapping.size() > 0) {
			strQuery = "update usermapping  set nstatus=" + Enumeration.TransactionStatus.DELETED.gettransactionstatus()
			+ ", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nstatus="
			+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "	and  nusermappingcode in("
			+ lstUserMapping.stream().map(object -> (String.valueOf(object.getNusermappingcode())))
			.collect(Collectors.joining(","))
			+ ")";
			jdbcTemplate.execute(strQuery);
			multilingualIDList.add("IDS_DELETEUSERMAPPING");
			deletedObjects.add(lstUserMapping);

			auditUtilityFunction.fnInsertListAuditAction(deletedObjects, 1, null, multilingualIDList, userInfo);
			return getChildUsers(lastParentUsercode, nroleCode, nSiteCode, nVersionCode, levelno);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> getUserMappingTree(Map<String, Object> inputMap) throws Exception {

		// ALPD-4436
		// To get path value from system's environment variables instead of absolutepath
		final String homePath = ftpUtilityFunction.getFileAbsolutePath();
		final String dirLocation2 = System.getenv(homePath)// new File("").getAbsolutePath()
				+ "\\webapps\\ROOT\\SharedFolder\\UserProfile";
		Map<String, Object> returnMap = new HashMap<String, Object>();
		File f1 = new File(dirLocation2);
		final ObjectMapper objmapper = new ObjectMapper();

		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		if (f1.exists()) {

			List<String> files = Files.list(Paths.get(dirLocation2)).filter(Files::isRegularFile)
					.filter(path -> path.toString().endsWith(".jpg") || path.toString().endsWith(".png")
							|| path.toString().endsWith(".jpeg"))
					.map(path -> path.toString().substring(path.toString().lastIndexOf("\\") + 1).trim())
					.collect(Collectors.toList());
			returnMap.put("folderFiles", files);
		} else {
			List<String> files = new ArrayList<>();
			returnMap.put("folderFiles", files);
		}

		if (inputMap.containsKey("nusermappingcode")) {
			final String getQuery = "WITH RECURSIVE t1"
					+ " (nusermappingcode,nversioncode,napprovalconfigcode,nparentrolecode,nparentusercode,nparentusersitecode,nchildrolecode,"
					+ " nchildusercode,nchildusersitecode,nparusermappingcode,nlevel,nsitecode,nstatus,username,suserrolename,suserimgname,suserimgftp) "
					+ " as ("
					+ " 	select um.nusermappingcode,um.nversioncode,um.napprovalconfigcode,um.nparentrolecode,um.nparentusercode,um.nparentusersitecode,um.nchildrolecode, "
					+ "		um.nchildusercode,um.nchildusersitecode,um.nparusermappingcode,um.nlevel,um.nsitecode,um.nstatus,CONCAT(u.sfirstname,' ',u.slastname) as username,ur.suserrolename,uf.suserimgname,uf.suserimgftp"
					+ " 	from usermapping um,users u,userrole ur,userfile uf"
					+ " 	where u.nusercode=um.nchildusercode and uf.nusercode=u.nusercode"
					+ " 	and ur.nuserrolecode=um.nchildrolecode and um.nstatus=u.nstatus"
					+ " 	and u.nstatus=ur.nstatus and ur.nstatus=uf.nstatus and uf.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " 	and um.nusermappingcode="
					+ inputMap.get("nusermappingcode") + " 	union all"
					+ " 	select um.nusermappingcode,um.nversioncode,um.napprovalconfigcode,um.nparentrolecode,um.nparentusercode,um.nparentusersitecode,um.nchildrolecode, "
					+ "		um.nchildusercode,um.nchildusersitecode,um.nparusermappingcode,um.nlevel,um.nsitecode,um.nstatus,(u.sfirstname|| ' ' ||u.slastname) as username,ur.suserrolename,uf.suserimgname,uf.suserimgftp"
					+ " 	from usermapping um"
					+ " 	inner join t1 on um.nparusermappingcode = t1.nusermappingcode,users u,userrole ur,userfile uf"
					+ " 	where u.nusercode=um.nchildusercode and uf.nusercode=u.nusercode"
					+ " 	and ur.nuserrolecode=um.nchildrolecode and um.nstatus=u.nstatus"
					+ " 	and u.nstatus=ur.nstatus and ur.nstatus=uf.nstatus and uf.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ) " + " select * from t1;";
			List<Map<String, Object>> list = jdbcTemplate.queryForList(getQuery);
			if (list.size() == 0) {

				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);

			} else {
				returnMap.put("userTree", list);
				return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
			}

		} else {
			final String getQuery = "select um.*,CONCAT(u.sfirstname,' ',u.slastname) as username,ur.suserrolename,uf.suserimgname,uf.suserimgftp "
					+ " from usermapping um,users u,userrole ur,userfile uf"
					+ " where u.nusercode=um.nchildusercode and uf.nusercode=u.nusercode"
					+ " and ur.nuserrolecode=um.nchildrolecode and um.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and uf.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and um.nsitecode="
					+ inputMap.get("nsitecode") + " and nversioncode=" + inputMap.get("nversioncode");
			List<Map<String, Object>> list = jdbcTemplate.queryForList(getQuery);

			returnMap.put("userTree", list);
			return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getUserMappingCopy(int napprovalsubtypecode, int nregSubTypeCode, int nsiteCode,
			UserInfo userInfo) throws Exception {

		Map<String, Object> rtnMap = new HashMap<String, Object>();
		final String str = "select distinct rt.nregtypecode,coalesce(rt.jsondata->'sregtypename'->>'"
				+ userInfo.getSlanguagetypecode() + "',rt.jsondata->'sregtypename'->>'en-US') as sregtypename "
				+ "from treetemplatetransactionrole ttt,treeversiontemplate tvt,approvalconfig ac,registrationtype rt,registrationsubtype rst "
				+ "where ttt.ntreeversiontempcode=tvt.ntreeversiontempcode "
				+ "and ac.napprovalconfigcode=ttt.napprovalconfigcode and tvt.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and rt.nregtypecode=ac.nregtypecode and rst.nregtypecode = rt.nregtypecode "
				+ " and ac.nregsubtypecode = rst.nregsubtypecode and ac.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and ttt.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and tvt.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and tvt.nsitecode = "
				+ nsiteCode + " and rst.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rt.nregtypecode>0 and ac.napprovalsubtypecode= " + napprovalsubtypecode
				+ " and rst.nregsubtypecode!=" + nregSubTypeCode + " ";

		List<RegistrationType> lstRegType = jdbcTemplate.query(str, new RegistrationType());

		rtnMap.put("CopyRegType", lstRegType);

		if (lstRegType.size() > 0) {
			rtnMap.putAll((Map<String, Object>) getCopyRegSubType(lstRegType.get(0).getNregtypecode(), nregSubTypeCode,
					nsiteCode, userInfo).getBody());
		}

		return new ResponseEntity<Object>(rtnMap, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> getCopyRegSubType(int nregTypeCode, int nregSubTypeCode, int nsiteCode,
			UserInfo userInfo) throws Exception {
		final ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> rtnMap = new HashMap<String, Object>();

		final String str = "select distinct rst.nregsubtypecode, rst.nregtypecode, rst.jsondata, rst.nsorter, rst.nsitecode, rst.nstatus,coalesce(rst.jsondata->'sregsubtypename'->>'"
				+ userInfo.getSlanguagetypecode() + "',rst.jsondata->'sregsubtypename'->>'en-US') as sregsubtypename  "
				+ "from treetemplatetransactionrole ttt,treeversiontemplate tvt,approvalconfig ac,registrationsubtype rst "
				+ "where ttt.ntreeversiontempcode=tvt.ntreeversiontempcode "
				+ "and ac.napprovalconfigcode=ttt.napprovalconfigcode and tvt.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and rst.nregsubtypecode=ac.nregsubtypecode and ac.nregtypecode=" + nregTypeCode + " and ac.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and rst.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and tvt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and ttt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and tvt.nsitecode = " + nsiteCode
				+ " and rst.nregsubtypecode>0 and rst.nregsubtypecode!=" + nregSubTypeCode + " ";

		List<RegistrationSubType> lstRegSubType = jdbcTemplate.query(str, new RegistrationSubType());
		lstRegSubType = mapper.convertValue(commonFunction.getMultilingualMessageList(lstRegSubType,
				Arrays.asList("sregsubtypename"), userInfo.getSlanguagefilename()),
				new TypeReference<List<RegistrationSubType>>() {
		});
		rtnMap.put("CopyRegSubType", lstRegSubType);

		return new ResponseEntity<Object>(rtnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> copyUserMapping(Map<String, Object> objMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();

		final UserInfo userInfo = objMapper.convertValue(objMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		int ntreeVersionTempCode = (int) objMap.get("ntreeversiontempcode");
		int napprovalsubtypecode = (int) objMap.get("napprovalsubtypecode");
		int nregTypeCode = (int) objMap.get("nregtypecode");
		int nregSubTypeCode = (int) objMap.get("nregsubtypecode");
		int nsiteCode = (int) objMap.get("nsitecode");
		String approvalConfigQuery = "";
		int capprovalConfigCode = -1;
		int papprovalConfigCode = -1;

		approvalConfigQuery = "select * from treeversiontemplate where ntreeversiontempcode=" + ntreeVersionTempCode
				+ " and ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

		TreeVersionTemplate objTreeVersionTemplate = (TreeVersionTemplate) jdbcUtilityFunction
				.queryForObject(approvalConfigQuery, TreeVersionTemplate.class, jdbcTemplate);

		if (objTreeVersionTemplate == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTAPPROVEDTREETEMPLATEVERSIONINFILTER",
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			papprovalConfigCode = objTreeVersionTemplate.getNapprovalconfigcode();

			approvalConfigQuery = "select napprovalconfigcode from approvalconfig where napprovalsubtypecode="
					+ napprovalsubtypecode + " and nregtypecode=" + nregTypeCode + " and nregsubtypecode="
					+ nregSubTypeCode + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			capprovalConfigCode = jdbcTemplate.queryForObject(approvalConfigQuery, Integer.class);

			final String templateValidate = "select tobecopied.ntreeversiontempcode,tobecopied.napprovalconfigcode from("
					+ " select ttt.nuserrolecode,ttt.nlevelno,ttt.napprovalconfigcode"
					+ " from treetemplatetransactionrole ttt" + " where ttt.ntransactionstatus="
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and  ttt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and  ttt.ntreeversiontempcode=" + ntreeVersionTempCode + " ) selectedversion,"
					+ " (select ttt.nuserrolecode,ttt.nlevelno,ttt.napprovalconfigcode,ttt.ntreeversiontempcode from treetemplatetransactionrole ttt"
					+ " where nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "  and ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
					+ "  and  napprovalconfigcode in (" + capprovalConfigCode + ")"
					+ " and CAST(ttt.schildnode AS integer)!= any(select case when nneedanalyst="
					+ Enumeration.TransactionStatus.YES.gettransactionstatus()
					+ " then -1 else 0 end from approvalconfig where napprovalconfigcode in (" + capprovalConfigCode
					+ "))) tobecopied,"
					+ " ( select count(ttt.nlevelno) as levelcount from treetemplatetransactionrole ttt"
					+ " where ttt.ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
					+ " and  ttt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and  ttt.ntreeversiontempcode=" + ntreeVersionTempCode
					+ " and CAST(ttt.schildnode AS integer)!= any(select case when nneedanalyst="
					+ Enumeration.TransactionStatus.YES.gettransactionstatus()
					+ " then -1 else 0 end from approvalconfig where napprovalconfigcode in (" + capprovalConfigCode
					+ "))" + " ) selectedcount,"
					+ " (select count(ttt.nlevelno) as levelcount from treetemplatetransactionrole ttt"
					+ " where nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "  and ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
					+ " and  napprovalconfigcode in (" + capprovalConfigCode + ")"
					+ " and CAST(ttt.schildnode AS integer)!= any(select case when nneedanalyst="
					+ Enumeration.TransactionStatus.YES.gettransactionstatus()
					+ " then -1 else 0 end from approvalconfig where napprovalconfigcode in (" + capprovalConfigCode
					+ "))" + " ) copycount" + " where selectedversion.nuserrolecode=tobecopied.nuserrolecode"
					+ " and selectedversion.nlevelno=tobecopied.nlevelno "
					+ " and selectedcount.levelcount= copycount.levelcount "
					+ " group by tobecopied.napprovalconfigcode , tobecopied.ntreeversiontempcode";

			List<Map<String, Object>> lstTreeTemplateCode = jdbcTemplate.queryForList(templateValidate);

			if (lstTreeTemplateCode.size() > 0) {

				final String copySPQuery = "exec sp_usermappingcopy " + papprovalConfigCode + ", " + capprovalConfigCode
						+ "," + nsiteCode;
				jdbcTemplate.execute(copySPQuery);

				final String auditQuery = "select pt.sversiondescription+'->'+ct.sversiondescription"
						+ " from treeversiontemplate pt,treeversiontemplate ct" + " where pt.napprovalconfigcode = "
						+ papprovalConfigCode + " and ct.napprovalconfigcode = " + capprovalConfigCode
						+ " and pt.ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
						+ " and ct.ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
						+ " and ct.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and pt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				String auditComments = commonFunction.getMultilingualMessage("IDS_COPYUSERMAPPINGFROMUSERROLETEMPLATE",
						userInfo.getSlanguagefilename());
				auditComments = auditComments + " " + jdbcTemplate.queryForObject(auditQuery, String.class);

				Map<String, Object> outputMap = new HashMap<>();
				outputMap.put("stablename", "usermapping");
				outputMap.put("sprimarykeyvalue", -1);

				auditUtilityFunction.insertAuditAction(userInfo, "IDS_COPYUSERMAPPING", auditComments, outputMap);
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_COPIEDSUCCESSFULLY",
						userInfo.getSlanguagefilename()), HttpStatus.ACCEPTED);
			} else {

				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TREEVERSIONTEMPLATEMISSMATCH",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);

			}
		}
	}
}
