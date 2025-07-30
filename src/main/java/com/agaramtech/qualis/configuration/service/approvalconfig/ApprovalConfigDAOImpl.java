package com.agaramtech.qualis.configuration.service.approvalconfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.basemaster.model.TransactionStatus;
import com.agaramtech.qualis.checklist.model.ChecklistVersion;
import com.agaramtech.qualis.configuration.model.ApprovalConfigAutoapproval;
import com.agaramtech.qualis.configuration.model.ApprovalConfigRole;
import com.agaramtech.qualis.configuration.model.ApprovalConfigVersion;
import com.agaramtech.qualis.configuration.model.ApprovalRoleActionDetail;
import com.agaramtech.qualis.configuration.model.ApprovalRoleDecisionDetail;
import com.agaramtech.qualis.configuration.model.ApprovalRoleFilterDetail;
import com.agaramtech.qualis.configuration.model.ApprovalRoleValidationDetail;
import com.agaramtech.qualis.configuration.model.ApprovalSubType;
import com.agaramtech.qualis.configuration.model.DesignTemplateMapping;
import com.agaramtech.qualis.configuration.model.SeqNoApprovalConfigVersion;
import com.agaramtech.qualis.configuration.model.SeqNoConfigurationMaster;
import com.agaramtech.qualis.configuration.model.TreeTemplateTransactionRole;
import com.agaramtech.qualis.configuration.model.TreeVersionTemplate;
import com.agaramtech.qualis.credential.model.SeqNoCredentialManagement;
import com.agaramtech.qualis.dynamicpreregdesign.model.ReactRegistrationTemplate;
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
public class ApprovalConfigDAOImpl implements ApprovalConfigDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApprovalConfigDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getApprovalConfigFilter(Map<String, Object> objMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> map = new HashMap<>();
		ObjectMapper objMapper = new ObjectMapper();
		int nFlag = 1;
		if (objMap.containsKey("nFlag")) {
			nFlag = (int) objMap.get("nFlag");
		}
		int napprovalsubtypecode = 0;
		int nregtypecode = -1;
		int nregsubtypecode = -1;
		String str = "";
		String sf = "";
		List<ApprovalConfigRole> lstapprovalconfig = new ArrayList<ApprovalConfigRole>();
		if (nFlag == 1) {
			str = "select distinct ast.napprovalsubtypecode,coalesce(ast.jsondata->'approvalsubtypename'->>'"
					+ userInfo.getSlanguagetypecode() + "',"
					+ " ast.jsondata->'approvalsubtypename'->>'en-US') as ssubtypename, ast.ntemplatecode "
					+ " from treetemplatetransactionrole ttt,treeversiontemplate tvt,approvalconfig ac,approvalsubtype ast"
					+ " where ttt.ntreeversiontempcode = tvt.ntreeversiontempcode and ac.napprovalconfigcode = ttt.napprovalconfigcode"
					+ " and ast.napprovalsubtypecode = ac.napprovalsubtypecode and tvt.ntransactionstatus = "
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and ac.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ast.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tvt.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ttt.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tvt.nsitecode = "
					+ userInfo.getNmastersitecode() + " and ttt.nsitecode = " + userInfo.getNmastersitecode()
					+ " and ac.nsitecode = " + userInfo.getNmastersitecode() + " order by ntemplatecode asc";
			List<ApprovalSubType> lstsubtype = jdbcTemplate.query(str, new ApprovalSubType());
			lstsubtype = objMapper.convertValue(lstsubtype, new TypeReference<List<ApprovalSubType>>() {
			});
			if (!lstsubtype.isEmpty()) {
				napprovalsubtypecode = lstsubtype.get(0).getNapprovalsubtypecode();
				Map<String, Object> subComboValue = new HashMap<String, Object>();
				Map<String, Object> regComboValue = new HashMap<String, Object>();
				Map<String, Object> regSubcomboValue = new HashMap<String, Object>();
				subComboValue.put("value", lstsubtype.get(0).getNapprovalsubtypecode());
				subComboValue.put("label", lstsubtype.get(0).getSsubtypename());
				subComboValue.put("item", lstsubtype.get(0));
				if (napprovalsubtypecode == Enumeration.ApprovalSubType.TESTRESULTAPPROVAL.getnsubtype()) {
					List<RegistrationType> lstreg = new ArrayList<RegistrationType>();
					List<RegistrationSubType> lstregsub = new ArrayList<RegistrationSubType>();
					str = "select distinct rt.nregtypecode,coalesce(rt.jsondata->'sregtypename'->>'"
							+ userInfo.getSlanguagetypecode()
							+ "',rt.jsondata->'sregtypename'->>'en-US') as sregtypename,rt.nsorter "
							+ " from treetemplatetransactionrole ttt,treeversiontemplate tvt,approvalconfig ac,registrationtype rt "
							+ " where ttt.ntreeversiontempcode = tvt.ntreeversiontempcode " + " and rt.nsitecode  =  "
							+ userInfo.getNmastersitecode() + " and ac.napprovalconfigcode = ttt.napprovalconfigcode "
							+ " and tvt.ntransactionstatus = "
							+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
							+ " and rt.nregtypecode = ac.nregtypecode and ac.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tvt.nsitecode = "
							+ userInfo.getNmastersitecode() + " and tvt.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ttt.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nregtypecode > 0 "
							+ " and ac.napprovalsubtypecode = " + napprovalsubtypecode + " and ac.nsitecode = "
							+ userInfo.getNmastersitecode() + " and ttt.nsitecode = " + userInfo.getNmastersitecode()
							+ " order by rt.nregtypecode desc";

					lstreg = jdbcTemplate.query(str, new RegistrationType());

					sf = "";
					if (!lstreg.isEmpty()) {
						regComboValue.put("value", lstreg.get(0).getNregtypecode());
						regComboValue.put("label", lstreg.get(0).getSregtypename());
						nregtypecode = lstreg.get(0).getNregtypecode();
						str = "select distinct rst.nregsubtypecode, rst.nregtypecode, rst.jsondata, rst.dmodifieddate, rst.nsorter,"
								+ " rst.nsitecode, rst.nstatus, coalesce(rst.jsondata->'sregsubtypename'->>'"
								+ userInfo.getSlanguagetypecode()
								+ "',rst.jsondata->'sregsubtypename'->>'en-US') as sregsubtypename "
								+ " from treetemplatetransactionrole ttt,treeversiontemplate tvt,approvalconfig ac,"
								+ " registrationsubtype rst "
								+ " where ttt.ntreeversiontempcode = tvt.ntreeversiontempcode "
								+ " and rst.nsitecode  = " + userInfo.getNmastersitecode()
								+ " and ac.napprovalconfigcode = ttt.napprovalconfigcode "
								+ " and tvt.ntransactionstatus = "
								+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
								+ " and rst.nregsubtypecode = ac.nregsubtypecode and ac.nregtypecode = "
								+ lstreg.get(0).getNregtypecode() + " and ac.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rst.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tvt.nsitecode = "
								+ userInfo.getNmastersitecode() + " and tvt.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ttt.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ttt.nsitecode = "
								+ userInfo.getNmastersitecode() + " and rst.nregsubtypecode > 0 "
								+ " and ac.nsitecode = " + userInfo.getNmastersitecode()
								+ " order by rst.nregsubtypecode desc";
						lstregsub = jdbcTemplate.query(str, new RegistrationSubType());

						if (!lstregsub.isEmpty()) {
							nregsubtypecode = lstregsub.get(0).getNregsubtypecode();
							regSubcomboValue.put("value", lstregsub.get(0).getNregsubtypecode());
							regSubcomboValue.put("label", lstregsub.get(0).getSregsubtypename());
							str = "select napprovalconfigcode, nregsubtypecode, nregtypecode, napprovalsubtypecode, nneedanalyst, sdescription, dmodifieddate, nsitecode, nstatus from approvalconfig where napprovalsubtypecode = "
									+ napprovalsubtypecode + " and nregtypecode = " + nregtypecode
									+ " and nregsubtypecode = " + nregsubtypecode + " and nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
									+ userInfo.getNmastersitecode();
							lstapprovalconfig = jdbcTemplate.query(str,
									new ApprovalConfigRole());
							if (!lstapprovalconfig.isEmpty()) {
								map.put("approvalConfigCode", (int) lstapprovalconfig.get(0).getNapprovalconfigcode());
								objMap.put("napprovalconfigcode",
										(int) lstapprovalconfig.get(0).getNapprovalconfigcode());
							} else {
								objMap.put("napprovalconfigcode", 0);
							}
						} else {
							objMap.put("napprovalconfigcode", 0);
						}
					}
					map.put("registrationType", lstreg);
					map.put("registrationTypeValue", regComboValue);
					map.put("realRegistrationTypeValue", regComboValue);
					map.put("registrationSubType", lstregsub);
					map.put("registrationSubTypeValue", regSubcomboValue);
					map.put("realRegistrationSubTypeValue", regSubcomboValue);
				}
				map.put("realApprovalSubTypeValue", subComboValue);
				map.put("approvalSubTypeValue", subComboValue);
			}
			map.put("approvalsubtype", lstsubtype);

		} else if (nFlag == 2) {
			List<RegistrationType> lstreg = new ArrayList<RegistrationType>();
			List<RegistrationSubType> lstregsub = new ArrayList<RegistrationSubType>();
			if (objMap.containsKey("napprovalsubtypecode")) {
				napprovalsubtypecode = (int) objMap.get("napprovalsubtypecode");
				sf = " and ac.napprovalsubtypecode = " + napprovalsubtypecode;
			}
			str = "select distinct rt.nregtypecode,coalesce(rt.jsondata->'sregtypename'->>'"
					+ userInfo.getSlanguagetypecode()
					+ "',rt.jsondata->'sregtypename'->>'en-US') as sregtypename,rt.nsorter "
					+ " from treetemplatetransactionrole ttt,treeversiontemplate tvt,approvalconfig ac,registrationtype rt "
					+ " where ttt.ntreeversiontempcode = tvt.ntreeversiontempcode " + " and rt.nsitecode  = "
					+ userInfo.getNmastersitecode() + " and ac.napprovalconfigcode = ttt.napprovalconfigcode"
					+ " and tvt.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
					+ " and rt.nregtypecode = ac.nregtypecode and ac.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tvt.nsitecode = "
					+ userInfo.getNmastersitecode() + " and tvt.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ttt.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nregtypecode > 0"
					+ " and ac.nsitecode = " + userInfo.getNmastersitecode() + " and ttt.nsitecode = "
					+ userInfo.getNmastersitecode() + sf + "";

			lstreg = jdbcTemplate.query(str, new RegistrationType());

			sf = "";
			Map<String, Object> regComboValue = null;
			Map<String, Object> regSubcomboValue = null;
			if (!lstreg.isEmpty()) {
				regComboValue = new HashMap<String, Object>();
				nregtypecode = lstreg.get(0).getNregtypecode();
				str = "select distinct rst.nregsubtypecode, rst.nregtypecode, rst.jsondata, rst.dmodifieddate, rst.nsorter, rst.nsitecode, rst.nstatus, coalesce(rst.jsondata->'sregsubtypename'->>'"
						+ userInfo.getSlanguagetypecode()
						+ "',rst.jsondata->'sregsubtypename'->>'en-US') as sregsubtypename "
						+ "from treetemplatetransactionrole ttt,treeversiontemplate tvt,approvalconfig ac,"
						+ "registrationsubtype rst " + " where ttt.ntreeversiontempcode = tvt.ntreeversiontempcode "
						+ " and ac.napprovalconfigcode = ttt.napprovalconfigcode " + " and rst.nsitecode  = "
						+ userInfo.getNmastersitecode() + " and tvt.ntransactionstatus = "
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
						+ " and rst.nregsubtypecode = ac.nregsubtypecode " + " and ac.nregtypecode = "
						+ lstreg.get(0).getNregtypecode() + " and ac.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rst.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tvt.nsitecode = "
						+ userInfo.getNmastersitecode() + " and tvt.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ttt.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ttt.nsitecode = "
						+ userInfo.getNmastersitecode() + " and ac.nsitecode = " + userInfo.getNmastersitecode()
						+ " and rst.nregsubtypecode >0 order by rst.nregsubtypecode desc";

				lstregsub = jdbcTemplate.query(str, new RegistrationSubType());

				regComboValue.put("value", lstreg.get(0).getNregtypecode());
				regComboValue.put("label", lstreg.get(0).getSregtypename());
				if (!lstregsub.isEmpty()) {
					nregsubtypecode = lstregsub.get(0).getNregsubtypecode();
					regSubcomboValue = new HashMap<String, Object>();
					regSubcomboValue.put("value", lstregsub.get(0).getNregsubtypecode());
					regSubcomboValue.put("label", lstregsub.get(0).getSregsubtypename());
					str = " select napprovalconfigcode, nregsubtypecode, nregtypecode, napprovalsubtypecode, nneedanalyst, sdescription, dmodifieddate, nsitecode, nstatus from approvalconfig where napprovalsubtypecode = "
							+ napprovalsubtypecode + " and nregtypecode = " + nregtypecode + " and nregsubtypecode = "
							+ nregsubtypecode + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
							+ userInfo.getNmastersitecode();
					lstapprovalconfig = jdbcTemplate.query(str, new ApprovalConfigRole());
					if (!lstapprovalconfig.isEmpty()) {
						map.put("approvalConfigCode", lstapprovalconfig.get(0).getNapprovalconfigcode());
						objMap.put("napprovalconfigcode", lstapprovalconfig.get(0).getNapprovalconfigcode());
					} else {
						objMap.put("napprovalconfigcode", 0);
					}
				} else {
					objMap.put("napprovalconfigcode", 0);
				}
			} else {
				objMap.put("napprovalconfigcode", 0);
			}
			map.put("registrationTypeValue", regComboValue);
			map.put("registrationSubTypeValue", regSubcomboValue);
			map.put("registrationtype", lstreg);
			map.put("registrationsubtype", lstregsub);
		} else if (nFlag == 3) {
			List<RegistrationSubType> lstregsub = new ArrayList<RegistrationSubType>();
			Map<String, Object> regSubcomboValue = null;
			napprovalsubtypecode = (int) objMap.get("napprovalsubtypecode");
			nregtypecode = (int) objMap.get("nregtypecode");
			str = "select distinct rst.nregsubtypecode, rst.nregtypecode, rst.jsondata, rst.dmodifieddate, rst.nsorter, rst.nsitecode, rst.nstatus, coalesce(rst.jsondata->'sregsubtypename'->>'"
					+ userInfo.getSlanguagetypecode()
					+ "',rst.jsondata->'sregsubtypename'->>'en-US') as sregsubtypename "
					+ " from treetemplatetransactionrole ttt,treeversiontemplate tvt,approvalconfig ac,"
					+ " registrationsubtype rst " + " where ttt.ntreeversiontempcode = tvt.ntreeversiontempcode "
					+ " and ac.napprovalconfigcode = ttt.napprovalconfigcode " + " and rst.nsitecode  = "
					+ userInfo.getNmastersitecode() + " and tvt.ntransactionstatus = "
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
					+ " and rst.nregsubtypecode = ac.nregsubtypecode" + " and ac.nregtypecode = " + nregtypecode
					+ " and ac.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and rst.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tvt.nsitecode = " + userInfo.getNmastersitecode() + " and ttt.nsitecode = "
					+ userInfo.getNmastersitecode() + " and ac.nsitecode = " + userInfo.getNmastersitecode()
					+ " and tvt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ttt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and rst.nregsubtypecode > 0 order by rst.nregsubtypecode desc";

			lstregsub = jdbcTemplate.query(str, new RegistrationSubType());

			if (!lstregsub.isEmpty()) {
				nregsubtypecode = lstregsub.get(0).getNregsubtypecode();
				regSubcomboValue = new HashMap<String, Object>();
				regSubcomboValue.put("value", lstregsub.get(0).getNregsubtypecode());
				regSubcomboValue.put("label", lstregsub.get(0).getSregsubtypename());
				str = " select napprovalconfigcode, nregsubtypecode, nregtypecode, napprovalsubtypecode, nneedanalyst, sdescription, dmodifieddate, nsitecode, nstatus from approvalconfig where napprovalsubtypecode = "
						+ napprovalsubtypecode + " and nregtypecode = " + nregtypecode + " and nregsubtypecode = "
						+ nregsubtypecode + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
						+ userInfo.getNmastersitecode();
				lstapprovalconfig = jdbcTemplate.query(str, new ApprovalConfigRole());
				if (!lstapprovalconfig.isEmpty()) {
					map.put("approvalConfigCode", lstapprovalconfig.get(0).getNapprovalconfigcode());
					objMap.put("napprovalconfigcode", lstapprovalconfig.get(0).getNapprovalconfigcode());
				} else {
					objMap.put("napprovalconfigcode", 0);
				}

			} else {
				objMap.put("napprovalconfigcode", 0);
			}
			map.put("registrationSubTypeValue", regSubcomboValue);
			map.put("registrationsubtype", lstregsub);

		} else if (nFlag == 4) {
			napprovalsubtypecode = (int) objMap.get("napprovalsubtypecode");
			nregtypecode = (int) objMap.get("nregtypecode");
			nregsubtypecode = (int) objMap.get("nregsubtypecode");
		}
		str = "select napprovalconfigcode, nregsubtypecode, nregtypecode, napprovalsubtypecode, nneedanalyst, sdescription, dmodifieddate, nsitecode, nstatus from approvalconfig where napprovalsubtypecode = "
				+ napprovalsubtypecode + " and nregtypecode = " + nregtypecode + " and nregsubtypecode = "
				+ nregsubtypecode + " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode = " + userInfo.getNmastersitecode();
		lstapprovalconfig = jdbcTemplate.query(str, new ApprovalConfigRole());
		if (!lstapprovalconfig.isEmpty()) {
			map.put("approvalConfigCode", (int) lstapprovalconfig.get(0).getNapprovalconfigcode());
			objMap.put("napprovalconfigcode", (int) lstapprovalconfig.get(0).getNapprovalconfigcode());
			if (nFlag != 5) {
				Map<String, Object> userroleTemplateMap = getUserRoleTemplate(
						lstapprovalconfig.get(0).getNapprovalconfigcode());
				map.putAll(userroleTemplateMap);
				objMap.putAll(userroleTemplateMap);
				if (nFlag == 1) {
					map.put("realTreeVersionTemplateValue", userroleTemplateMap.get("userroleTemplateValue"));
				}
			}
		} else {
			map.put("approvalConfigCode", 0);
			objMap.put("napprovalconfigcode", 0);
		}
		objMap.put("nflag", -1);
		objMap.put("napprovalsubtypecode", napprovalsubtypecode);
		if (nFlag == 1) {
			map.put("realApprovalConfigCode",
					!lstapprovalconfig.isEmpty() ? lstapprovalconfig.get(0).getNapprovalconfigcode() : 0);
			map.putAll((Map<String, Object>) getApprovalConfigVersion(objMap, userInfo).getBody());
		}
		return new ResponseEntity<Object>(map, HttpStatus.OK);
	}

	public Map<String, Object> getUserRoleTemplate(int napprovalConfigCode) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		TreeVersionTemplate approvedVersion = new TreeVersionTemplate();
		final String getUserRoleTemplate = "select ntreeversiontempcode, napprovalconfigcode, ntransactionstatus, ntemplatecode, nsampletypecode, nversionno,sversiondescription ||'('|| nversionno||')' as sversiondescription from treeversiontemplate "
				+ " where napprovalconfigcode = " + napprovalConfigCode + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ntreeversiontempcode>0 and ntransactionstatus <>  "
				+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus();

		List<TreeVersionTemplate> lstTreeVersionTemplate = jdbcTemplate
				.query(getUserRoleTemplate, new TreeVersionTemplate());
		if (!lstTreeVersionTemplate.isEmpty()) {
			List<TreeVersionTemplate> lstTreeVersionTemplate2 = lstTreeVersionTemplate
					.stream().filter(version -> version
							.getNtransactionstatus() == Enumeration.TransactionStatus.APPROVED.gettransactionstatus())
					.collect(Collectors.toList());
			if (!lstTreeVersionTemplate2.isEmpty()) {
				approvedVersion = lstTreeVersionTemplate2.get(0);
				map.put("treeVersionTemplate", lstTreeVersionTemplate);
				Map<String, Object> regSubcomboValue = new HashMap<String, Object>();
				regSubcomboValue.put("value", approvedVersion.getNtreeversiontempcode());
				regSubcomboValue.put("label", approvedVersion.getSversiondescription());
				map.put("userroleTemplateValue", regSubcomboValue);
				map.put("ntreeversiontempcode", lstTreeVersionTemplate2.get(0).getNtreeversiontempcode());
			} else {
				approvedVersion = lstTreeVersionTemplate.get(0);
				map.put("treeVersionTemplate", lstTreeVersionTemplate);
				Map<String, Object> regSubcomboValue = new HashMap<String, Object>();
				regSubcomboValue.put("value", approvedVersion.getNtreeversiontempcode());
				regSubcomboValue.put("label", approvedVersion.getSversiondescription());
				map.put("userroleTemplateValue", regSubcomboValue);
				map.put("ntreeversiontempcode", lstTreeVersionTemplate.get(0).getNtreeversiontempcode());
			}

		} else {
			map.put("treeVersionTemplate", lstTreeVersionTemplate);
			map.put("userroleTemplateValue", new ArrayList<>());
			map.put("ntreeversiontempcode", 0);
		}
		LOGGER.info("At getUserRoleTemplate() of ApprovalConfigDAOImpl.java");
		return map;
	}

	@Override
	public ResponseEntity<Object> getApprovalConfigVersion(Map<String, Object> objMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		String conditionString = "";
		String str = "";
		int napprovalConfigCode = 0;
		int napprovalConfigVersionCode = -1;
		int ntreeVersionTempCode = -1;
		if (objMap.containsKey("napprovalconfigversioncode")) {
			napprovalConfigVersionCode = (int) objMap.get("napprovalconfigversioncode");
			conditionString = " and acv.napproveconfversioncode = " + napprovalConfigVersionCode;
		} else if (objMap.containsKey("napproveconfversioncode")) {
			napprovalConfigVersionCode = (int) objMap.get("napproveconfversioncode");
			conditionString = " and acv.napproveconfversioncode = " + napprovalConfigVersionCode;
		} else {
			napprovalConfigCode = (int) objMap.get("napprovalconfigcode");
			conditionString = " and acv.napprovalconfigcode = " + napprovalConfigCode;
		}
		if (objMap.containsKey("ntreeversiontempcode")) {
			ntreeVersionTempCode = (int) objMap.get("ntreeversiontempcode");
			conditionString += " and acv.ntreeversiontempcode = " + ntreeVersionTempCode;
		}

		boolean isNotDraftConfig = true;
		if (napprovalConfigVersionCode != -1) {
			ApprovalConfigAutoapproval validateVersion = getApprovalConfigVersionByID(napprovalConfigVersionCode,
					userInfo);
			if (validateVersion == null) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				if (validateVersion.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT
						.gettransactionstatus()) {
					isNotDraftConfig = false;
				}
			}
		}
		String yes = commonFunction.getMultilingualMessage("IDS_YES", userInfo.getSlanguagefilename());
		String no = commonFunction.getMultilingualMessage("IDS_NO", userInfo.getSlanguagefilename());

		String getQuery = "";
		String tableQuery = "";
		String whereQuery = "";

		if (isNotDraftConfig) {
			getQuery = ", case when acv.ndesigntemplatemappingcode = -1 then '-' else "
					+ " rrt.sregtemplatename || '(' || dtm.nversionno || ')' end stemplatemappingversion ";
			tableQuery = ", designtemplatemapping dtm, reactregistrationtemplate rrt ";
			whereQuery = " and dtm.ndesigntemplatemappingcode = acv.ndesigntemplatemappingcode and dtm.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and dtm.nreactregtemplatecode = rrt.nreactregtemplatecode and rrt.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		} else {
			getQuery = ", '-' stemplatemappingversion ";
		}
		str = "select acv.napproveconfversioncode, acv.napprovalconfigcode, acv.napproveconfversionno, acv.ntransactionstatus, acv.ntreeversiontempcode, acv.ndesigntemplatemappingcode, acv.sapproveconfversiondesc, acv.sviewname, acv.dmodifieddate, acv.nsitecode, acv.nstatus,"
				+ " ap.nautoapprovalcode, ap.napprovalconfigcode, ap.napprovalconfigversioncode, ap.sversionname, ap.nneedautocomplete, ap.nneedautoapproval, ap.nautodescisionstatus, ap.nautoapprovalstatus, ap.nautoallot, ap.nneedjoballocation, ap.nneedautoinnerband, ap.nneedautoouterband, ap.dmodifieddate, ap.nsitecode, ap.nstatus,"
				+ " case when ap.nneedautoapproval = 3 then '" + yes + "' else '" + no + "' end as \"74\","
				+ " case when ap.nneedautocomplete = 3 then '" + yes + "' else '" + no + "' end as \"73\","
				+ " case when ap.nneedjoballocation = 3 then '" + yes + "' else '" + no + "' end as \"78\","
				+ " case when ap.nautoallot = 3 then '" + yes + "' else '" + no + "' end as \"72\","
				+ " case when ap.nneedautoinnerband = 3 then '" + yes + "' else '" + no + "' end as sneedautoinnerband,"
				+ " case when ap.nneedautoouterband = 3 then '" + yes + "' else '" + no + "' end as sneedautoouterband,"
				+ " coalesce(t1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ "	t1.jsondata->'stransdisplaystatus'->>'en-US') as sautodescisionstatus,coalesce(t2.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ "	t2.jsondata->'stransdisplaystatus'->>'en-US')  as sautoapprovalstatus,	"
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ "	ts.jsondata->'stransdisplaystatus'->>'en-US')  as sversionstatus,"
				+ " tvt.sversiondescription,acv.sviewname  " + getQuery
				+ " from approvalconfigversion acv,transactionstatus ts,approvalconfig ac, "
				+ " approvalconfigautoapproval ap,transactionstatus t1,transactionstatus t2,treeversiontemplate tvt  "
				+ tableQuery + " where  ts.ntranscode = acv.ntransactionstatus and acv.nsitecode  = "
				+ userInfo.getNmastersitecode()
				+ " and acv.napprovalconfigcode = ac.napprovalconfigcode and ap.napprovalconfigversioncode = acv.napproveconfversioncode "
				+ conditionString + " and acv.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ac.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and t1.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and t2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and acv.nsitecode = " + userInfo.getNmastersitecode() + " and ac.nsitecode = "
				+ userInfo.getNmastersitecode() + " and ap.nsitecode = " + userInfo.getNmastersitecode()
				+ " and tvt.nsitecode = " + userInfo.getNmastersitecode()
				+ " and t1.ntranscode = ap.nautodescisionstatus" + " and  t2.ntranscode = ap.nautoapprovalstatus"
				+ " and tvt.ntreeversiontempcode = acv.ntreeversiontempcode "
				+ " and acv.napproveconfversioncode>0  and tvt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + whereQuery + " "
				+ " order by acv.napproveconfversioncode desc";
		List<Map<String, Object>> lstData = jdbcTemplate.queryForList(str);
		if (!lstData.isEmpty() && napprovalConfigVersionCode < 0) {
			napprovalConfigVersionCode = (int) lstData.get(0).get("napprovalconfigversioncode");
		}
		if (objMap.containsKey("approvedversioncode")) {
			napprovalConfigVersionCode = (int) objMap.get("approvedversioncode");
		}

		List<ApprovalConfigRole> lstRole = getApprovalConfigVersionRole(napprovalConfigVersionCode, userInfo);
		if (objMap.containsKey("napprovalconfigversioncode") || objMap.containsKey("napproveconfversioncode")) {
			List<Map<String, Object>> roleNames = getApprovalConfigVersionRoleDetails(napprovalConfigVersionCode,
					userInfo);
			map.put("approvalconfigRoleNames", roleNames);
			map.put("selectedRole", roleNames.get(0));
			if (objMap.containsKey("approvedversioncode")) {
				final int approvedVersionCode = napprovalConfigVersionCode;
				map.put("selectedVersion",
						lstData.stream()
						.filter(data -> (approvedVersionCode == (int) data.get("napproveconfversioncode")))
						.collect(Collectors.toList()).get(0));
			} else {
				map.put("selectedVersion", !lstData.isEmpty() ? lstData.get(0) : new ArrayList<>());
			}
		} else {
			List<Map<String, Object>> roleNames = getApprovalConfigVersionRoleDetails(napprovalConfigVersionCode,
					userInfo);
			map.put("approvalconfigRoleNames", roleNames);
			if (!roleNames.isEmpty()) {
				map.put("selectedRole", roleNames.get(0));
			} else {
				map.put("selectedRole", new HashMap<String, Object>());
			}

			map.put("versionData", lstData);
			if (objMap.containsKey("approvedversioncode")) {
				final int approvedVersionCode = napprovalConfigVersionCode;
				map.put("selectedVersion",
						lstData.stream()
						.filter(data -> (approvedVersionCode == (int) data.get("napproveconfversioncode")))
						.collect(Collectors.toList()).get(0));
			} else {
				map.put("selectedVersion", !lstData.isEmpty() ? lstData.get(0) : new ArrayList<>());
			}
		}
		if (!lstRole.isEmpty()) {
			map.putAll(getApprovalConfigRoleDetails(lstRole.get(0).getNapprovalconfigrolecode(),
					(int) objMap.get("napprovalsubtypecode"), userInfo));
		}
		return new ResponseEntity<Object>(map, HttpStatus.OK);
	}

	public ApprovalConfigAutoapproval getApprovalConfigVersionByID(int napprovalConfigVersionCode, UserInfo userInfo)
			throws Exception {
		String str = "";
		String yes = commonFunction.getMultilingualMessage("IDS_YES", userInfo.getSlanguagefilename());
		String no = commonFunction.getMultilingualMessage("IDS_NO", userInfo.getSlanguagefilename());
		str = "select acv.napproveconfversioncode, acv.napprovalconfigcode, acv.napproveconfversionno, acv.ntransactionstatus, acv.ntreeversiontempcode, acv.ndesigntemplatemappingcode, acv.sapproveconfversiondesc, acv.sviewname, acv.dmodifieddate, acv.nsitecode, acv.nstatus,"
				+ " ap.nautoapprovalcode, ap.napprovalconfigcode, ap.napprovalconfigversioncode, ap.sversionname, ap.nneedautocomplete, ap.nneedautoapproval, ap.nautodescisionstatus, ap.nautoapprovalstatus, ap.nautoallot, ap.nneedjoballocation, ap.nneedautoinnerband, ap.nneedautoouterband, ap.dmodifieddate, ap.nsitecode, ap.nstatus,"
				+ " case when ap.nneedautoapproval = 3 then '" + yes + "' else '" + no + "' end as sneedautoapproval,"
				+ " case when ap.nneedautocomplete = 3 then '" + yes + "' else '" + no + "' end as sneedautocomplete,"
				+ " case when ap.nneedjoballocation = 3 then '" + yes + "' else '" + no + "' end as sneedjoballocation,"
				+ " case when ap.nautoallot = 3 then '" + yes + "' else '" + no + "' end as sautoallot,"
				+ " coalesce(t1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " t1.jsondata->'stransdisplaystatus'->>'en-US')  as sautodescisionstatus,coalesce(t2.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " t2.jsondata->'stransdisplaystatus'->>'en-US')  as sautoapprovalstatus,	"
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ "	 ts.jsondata->'stransdisplaystatus'->>'en-US')  as sversionstatus,acv.sviewname  "
				+ " from approvalconfigversion acv,transactionstatus ts,approvalconfig ac, "
				+ " approvalconfigautoapproval ap,transactionstatus t1,transactionstatus t2 "
				+ " where  ts.ntranscode = acv.ntransactionstatus and acv.nsitecode  = " + userInfo.getNmastersitecode()
				+ " and acv.napprovalconfigcode = ac.napprovalconfigcode and ap.napprovalconfigversioncode = acv.napproveconfversioncode"
				+ " and acv.napproveconfversioncode = " + napprovalConfigVersionCode + " and acv.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ac.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and t1.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and t2.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acv.nsitecode = "
				+ userInfo.getNmastersitecode() + " and ac.nsitecode = " + userInfo.getNmastersitecode()
				+ " and ap.nsitecode = " + userInfo.getNmastersitecode()
				+ " and t1.ntranscode = ap.nautodescisionstatus" + " and t2.ntranscode = ap.nautoapprovalstatus";

		return (ApprovalConfigAutoapproval) jdbcUtilityFunction.queryForObject(str, ApprovalConfigAutoapproval.class,
				jdbcTemplate);
	}

	public List<ApprovalConfigRole> getApprovalConfigVersionRole(int napprovalConfigVersionCode, UserInfo userInfo)
			throws Exception {
		final String msg = commonFunction.getMultilingualMessage("IDS_TOPLEVEL", userInfo.getSlanguagefilename());

		String strQuery = " select apr.napprovalconfigrolecode, apr.napprovalconfigcode, apr.ntreeversiontempcode, apr.nuserrolecode, apr.nchecklistversioncode,"
				+ " apr.npartialapprovalneed, apr.nsectionwiseapprovalneed, apr.nrecomretestneed, apr.nrecomrecalcneed, apr.nretestneed, apr.nrecalcneed,"
				+ " apr.nlevelno, apr.napprovalstatuscode, apr.napproveconfversioncode, apr.nautoapproval, apr.nautoapprovalstatuscode, apr.nautodescisionstatuscode, apr.ncorrectionneed, apr.nesignneed, apr.ntransactionstatus, apr.dmodifieddate, apr.nsitecode, apr.nstatus,"
				+ " ur.suserrolename," + " case when apr.nlevelno = 1 then ur.suserrolename || ' ' || '(" + msg
				+ ")' else ur.suserrolename end suserrolenamelevel,"
				+ " coalesce(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ "	ts1.jsondata->'stransdisplaystatus'->>'en-US') as sesignneed,coalesce(ts2.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ "ts2.jsondata->'stransdisplaystatus'->>'en-US') as sapprovalstatus,"
				+ " coalesce(ts3.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts3.jsondata->'stransdisplaystatus'->>'en-US') as  spartialapprovalneed,"
				+ " coalesce(ts4.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ "ts4.jsondata->'stransdisplaystatus'->>'en-US') as scorrectionneed,"
				+ " coalesce(ts5.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ "ts5.jsondata->'stransdisplaystatus'->>'en-US') as sneedautoapproval,"
				+ " coalesce(ts6.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ "	ts6.jsondata->'stransdisplaystatus'->>'en-US') as  ssectionwiseapprovalneed,"
				+ " cl.schecklistname schecklistname," + " clv.schecklistversionname schecklistversionname"
				+ " from approvalconfigrole apr,userrole ur,approvalconfig ap,approvalconfigautoapproval acaa,checklist cl,checklistversion clv,"
				+ " transactionstatus ts1,transactionstatus ts2,transactionstatus ts3,transactionstatus ts4,transactionstatus ts5,transactionstatus ts6"
				+ " where ur.nuserrolecode = apr.nuserrolecode and acaa.napprovalconfigversioncode = apr.napproveconfversioncode"
				+ " and ap.napprovalconfigcode = apr.napprovalconfigcode and clv.nchecklistversioncode = apr.nchecklistversioncode"
				+ " and clv.nchecklistcode = cl.nchecklistcode"
				+ " and ts1.ntranscode = apr.nesignneed  and ts2.ntranscode = apr.napprovalstatuscode"
				+ " and ts3.ntranscode = apr.npartialapprovalneed and ts4.ntranscode = apr.ncorrectionneed"
				+ " and ts5.ntranscode = apr.nautoapproval and ts6.ntranscode = apr.nsectionwiseapprovalneed"
				+ " and ts1.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts3.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts4.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts5.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts6.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and cl.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and clv.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ap.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and apr.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and acaa.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ur.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and apr.nsitecode = " + userInfo.getNmastersitecode() + " and ur.nsitecode = "
				+ userInfo.getNmastersitecode() + " and ap.nsitecode = " + userInfo.getNmastersitecode()
				+ " and acaa.nsitecode = " + userInfo.getNmastersitecode() + " and cl.nsitecode = "
				+ userInfo.getNmastersitecode() + " and clv.nsitecode = " + userInfo.getNmastersitecode()
				+ " and napproveconfversioncode = " + napprovalConfigVersionCode + " order by apr.nlevelno ";
		return jdbcTemplate.query(strQuery, new ApprovalConfigRole());
	}

	public List<Map<String, Object>> getApprovalConfigVersionRoleDetails(int napprovalConfigVersionCode,
			UserInfo userinfo) throws Exception {
		String strQuery = " select apr.napprovalconfigrolecode,apr.nuserrolecode, ur.suserrolename,apr.nlevelno,coalesce(ts1.jsondata->'stransdisplaystatus'->>'"
				+ userinfo.getSlanguagetypecode() + "',"
				+ "	ts1.jsondata->'stransdisplaystatus'->>'en-US')  as \"77\","
				+ " coalesce(ts2.jsondata->'stransdisplaystatus'->>'" + userinfo.getSlanguagetypecode() + "',"
				+ "	ts2.jsondata->'stransdisplaystatus'->>'en-US')  as sapprovalstatus,"
				+ " coalesce(ts3.jsondata->'stransdisplaystatus'->>'" + userinfo.getSlanguagetypecode() + "',"
				+ "	ts3.jsondata->'stransdisplaystatus'->>'en-US') as \"75\","
				+ " coalesce(ts4.jsondata->'stransdisplaystatus'->>'" + userinfo.getSlanguagetypecode() + "',"
				+ "	ts4.jsondata->'stransdisplaystatus'->>'en-US') as  scorrectionneed,"
				+ " coalesce(ts5.jsondata->'stransdisplaystatus'->>'" + userinfo.getSlanguagetypecode() + "',"
				+ "	ts5.jsondata->'stransdisplaystatus'->>'en-US') as   \"74\","
				+ " coalesce(ts6.jsondata->'stransdisplaystatus'->>'" + userinfo.getSlanguagetypecode() + "',"
				+ "ts6.jsondata->'stransdisplaystatus'->>'en-US') as  \"76\"," + " cl.schecklistname schecklistname,"
				+ " clv.schecklistversionname schecklistversionname"
				+ " from approvalconfigrole apr,userrole ur,approvalconfig ap,approvalconfigautoapproval acaa,checklist cl,checklistversion clv,"
				+ " transactionstatus ts1,transactionstatus ts2,transactionstatus ts3,transactionstatus ts4,transactionstatus ts5,transactionstatus ts6"
				+ " where ur.nuserrolecode = apr.nuserrolecode and acaa.napprovalconfigversioncode  =  apr.napproveconfversioncode"
				+ " and ap.napprovalconfigcode = apr.napprovalconfigcode and clv.nchecklistversioncode = apr.nchecklistversioncode"
				+ " and clv.nchecklistcode = cl.nchecklistcode"
				+ " and ts1.ntranscode = apr.nesignneed  and ts2.ntranscode = apr.napprovalstatuscode"
				+ " and ts3.ntranscode = apr.npartialapprovalneed and ts4.ntranscode = apr.ncorrectionneed"
				+ " and ts5.ntranscode = apr.nautoapproval and ts6.ntranscode = apr.nsectionwiseapprovalneed"
				+ " and ts1.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts3.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts4.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts5.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts6.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and cl.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and clv.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ap.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and apr.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and acaa.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ur.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and apr.nsitecode = " + userinfo.getNmastersitecode() + " and ur.nsitecode = "
				+ userinfo.getNmastersitecode() + " and ap.nsitecode = " + userinfo.getNmastersitecode()
				+ " and acaa.nsitecode = " + userinfo.getNmastersitecode() + " and cl.nsitecode = "
				+ userinfo.getNmastersitecode() + " and clv.nsitecode = " + userinfo.getNmastersitecode()
				+ " and napproveconfversioncode = " + napprovalConfigVersionCode + " order by nlevelno";
		List<Map<String, Object>> roleList = jdbcTemplate.queryForList(strQuery);
		return roleList;
	}

	@Override
	public Map<String, Object> getApprovalConfigRoleDetails(int napprovalConfigRoleCode, int napprovalsubtypecode,
			UserInfo userInfo) throws Exception {
		Map<String, Object> statusMap = new HashMap<String, Object>();
		String strQuery = "";

		strQuery = "select af.napprovalfiltercode, af.napprovalconfigrolecode, af.napprovalconfigcode, af.nuserrolecode, af.nlevelno, af.ndefaultstatus, af.ntransactionstatus, af.dmodifieddate, af.nsitecode, af.nstatus, "
				+ " us.suserrolename, coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "'," + "ts.jsondata->'stransdisplaystatus'->>'en-US')  as sfilterstatus"
				+ " from approvalrolefilterdetail af,transactionstatus ts,userrole us"
				+ " where af.ntransactionstatus = ts.ntranscode and af.nuserrolecode = us.nuserrolecode and af.napprovalconfigrolecode = "
				+ napprovalConfigRoleCode + " and af.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and us.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and af.nsitecode = "
				+ userInfo.getNmastersitecode() + " and us.nsitecode = " + userInfo.getNmastersitecode() + " ;";

		List<ApprovalRoleFilterDetail> lstApprovalRoleFilterDetail = jdbcTemplate.query(strQuery,
				new ApprovalRoleFilterDetail());

		strQuery = " select af.napprovalvalidationcode, af.napprovalconfigrolecode, af.napprovalconfigcode, af.nuserrolecode, af.nlevelno, af.ntransactionstatus, af.dmodifieddate, af.nsitecode, af.nstatus, "
				+ " us.suserrolename, coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "'," + "ts.jsondata->'stransdisplaystatus'->>'en-US') as svalidationstatus"
				+ " from approvalrolevalidationdetail af,transactionstatus ts,userrole us"
				+ " where af.ntransactionstatus = ts.ntranscode and af.nuserrolecode = us.nuserrolecode"
				+ " and af.napprovalconfigrolecode = " + napprovalConfigRoleCode + " and af.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and us.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and af.nsitecode = "
				+ userInfo.getNmastersitecode() + " and us.nsitecode = " + userInfo.getNmastersitecode() + " ;";

		List<ApprovalRoleValidationDetail> lstApprovalRoleValidationDetail = jdbcTemplate.query(strQuery,
				new ApprovalRoleValidationDetail());

		strQuery = " select af.napprovaldecisioncode, af.napprovalconfigrolecode, af.napprovalconfigcode, af.nuserrolecode, af.nlevelno, af.ndefaultstatus, af.ntransactionstatus, af.dmodifieddate, af.nsitecode, af.nstatus, "
				+ " us.suserrolename,coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "'," + "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdecisionstatus "
				+ " from approvalroledecisiondetail af,transactionstatus ts,userrole us "
				+ " where af.ntransactionstatus = ts.ntranscode and af.nuserrolecode = us.nuserrolecode and af.napprovalconfigrolecode = "
				+ napprovalConfigRoleCode + " and af.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and us.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and af.nsitecode = "
				+ userInfo.getNmastersitecode() + " and us.nsitecode = " + userInfo.getNmastersitecode() + " ;";

		List<ApprovalRoleDecisionDetail> lstApprovalRoleDecisionDetail = jdbcTemplate.query(strQuery,
				new ApprovalRoleDecisionDetail());

		strQuery = "select aa.napprovalactioncode, aa.napprovalconfigrolecode, aa.napprovalconfigcode, aa.nuserrolecode, aa.nlevelno, aa.ntransactionstatus, aa.dmodifieddate, aa.nsitecode, aa.nstatus,"
				+ " ur.suserrolename,coalesce(tr.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "'," + "tr.jsondata->'stransdisplaystatus'->>'en-US') as sactiondisplaystatus"
				+ " from approvalroleactiondetail aa,transactionstatus tr,userrole ur"
				+ " where aa.ntransactionstatus = tr.ntranscode and aa.nuserrolecode = ur.nuserrolecode and aa.napprovalconfigrolecode = "
				+ napprovalConfigRoleCode + " and aa.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tr.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and aa.nsitecode = "
				+ userInfo.getNmastersitecode() + " and ur.nsitecode = " + userInfo.getNmastersitecode() + " ;";

		List<ApprovalRoleActionDetail> lstApprovalRoleActionDetail = jdbcTemplate.query(strQuery,
				new ApprovalRoleActionDetail());
		strQuery = "select ts.stransstatus,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,ts.ntranscode,apsc.nsorter from transactionstatus ts,approvalstatusconfig apsc where ts.ntranscode = apsc.ntranscode and apsc.nstatusfunctioncode = 7"
				+ " and apsc.napprovalsubtypecode = " + napprovalsubtypecode + " and apsc.nformcode = 55 "
				+ " and apsc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and apsc.nsitecode = " + userInfo.getNmastersitecode() + " ;";

		List<TransactionStatus> lstTransactionStatus = jdbcTemplate.query(strQuery, new TransactionStatus());

		strQuery = " select ts.stransstatus,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,ts.ntranscode,apsc.nsorter from transactionstatus ts,approvalstatusconfig apsc where ts.ntranscode = apsc.ntranscode and apsc.nstatusfunctioncode = 8"
				+ " and apsc.napprovalsubtypecode = " + napprovalsubtypecode + " and apsc.nformcode = 55 "
				+ " and apsc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and apsc.nsitecode = " + userInfo.getNmastersitecode() + " ;";

		List<TransactionStatus> lstTransactionStatus1 = jdbcTemplate.query(strQuery, new TransactionStatus());

		statusMap.put("roleFilters", lstApprovalRoleFilterDetail);
		statusMap.put("roleValidations", lstApprovalRoleValidationDetail);
		statusMap.put("roleDecisions", lstApprovalRoleDecisionDetail);
		statusMap.put("roleActions", lstApprovalRoleActionDetail);
		statusMap.put("roleConfig", lstTransactionStatus);
		statusMap.put("versionConfig", lstTransactionStatus1);
		return statusMap;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getUserRoleApprovalConfig(Map<String, Object> objMap, UserInfo userInfo)
			throws Exception {
		int napprovalConfigCode = -1;
		int napprovalsubtypecode = -1;
		int ntreeversiontempcode = -1;
		if (objMap.containsKey("napprovalconfigcode") && objMap.containsKey("napprovalsubtypecode")) {
			napprovalConfigCode = (int) objMap.get("napprovalconfigcode");
			napprovalsubtypecode = (int) objMap.get("napprovalsubtypecode");
		}
		if (objMap.containsKey("ntreeversiontempcode")) {
			ntreeversiontempcode = (int) objMap.get("ntreeversiontempcode");
		}
		Map<String, Object> rtnMap = new HashMap<String, Object>();
		final String validateTemplate = "select ntreeversiontempcode,ntransactionstatus from treeversiontemplate"
				+ " where nstatus =  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and napprovalconfigcode = " + napprovalConfigCode + " and ntreeversiontempcode =  "
				+ ntreeversiontempcode;
		TreeVersionTemplate userRoleTemplate = (TreeVersionTemplate) jdbcUtilityFunction
				.queryForObject(validateTemplate, TreeVersionTemplate.class, jdbcTemplate);
		if (userRoleTemplate == null) {
			return new ResponseEntity<Object>(commonFunction
					.getMultilingualMessage("IDS_APPROVEDTEMPLATEVERSIONNOTAVAILABLE", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);

		} else if (userRoleTemplate.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED
				.gettransactionstatus()) {
			return new ResponseEntity<Object>(commonFunction
					.getMultilingualMessage("IDS_SELECTAPPROVEDUSERROLETEMPLATE", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);

		} else {
			final String msg = commonFunction.getMultilingualMessage("IDS_TOPLEVEL", userInfo.getSlanguagefilename());
			final String str = "select tttr.ntemptransrolecode, tttr.napprovalconfigcode, tttr.nuserrolecode, tttr.ntreeversiontempcode, tttr.ntransactionstatus, tttr.nlevelno,"
					+ " tttr.nparentnode, tttr.ntemplatecode, tttr.schildnode, tttr.slevelformat, tttr.dmodifieddate, tttr.nsitecode, tttr.nstatus, ur.suserrolename, "
					+ " case when tttr.nlevelno = 1 then ur.suserrolename || ' ' || '(" + msg
					+ ")' else ur.suserrolename end suserrolenamelevel "
					+ " from treetemplatetransactionrole tttr,userrole ur"
					+ " where tttr.nuserrolecode = ur.nuserrolecode" + " and tttr.napprovalconfigcode = "
					+ napprovalConfigCode + " and tttr.ntransactionstatus = "
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and tttr.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tttr.ntreeversiontempcode = "
					+ ntreeversiontempcode + " and tttr.nsitecode = " + userInfo.getNmastersitecode()
					+ " and ur.nsitecode = " + userInfo.getNmastersitecode()
					+ " and tttr.schildnode <> (select case when nneedanalyst = 3 then '-1' else '0' end from approvalconfig where napprovalconfigcode = "
					+ napprovalConfigCode + ")" + " order by tttr.nlevelno";
			List<TreeTemplateTransactionRole> lstTreeTemplateTransactionRole = jdbcTemplate
					.query(str, new TreeTemplateTransactionRole());
			if (!lstTreeTemplateTransactionRole.isEmpty()) {
				rtnMap.put("userroletree", lstTreeTemplateTransactionRole);
				rtnMap.putAll((Map<String, Object>) getAvailableComboData(napprovalsubtypecode, userInfo).getBody());

			} else {
				rtnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						"IDS_APPROVEDTEMPLATEVERSIONNOTAVAILABLE");
				return new ResponseEntity<Object>(
						commonFunction.getMultilingualMessage("IDS_APPROVEDTEMPLATEVERSIONNOTAVAILABLE",
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
			return new ResponseEntity<Object>(rtnMap, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> getAvailableComboData(int napprovalsubtypecode, UserInfo userInfo) throws Exception {
		Map<String, Object> rtnMap = new HashMap<String, Object>();
		String str = "";

		str = " select distinct coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
		+ "',"
		+ " ts.jsondata->'stransdisplaystatus'->>'en-US')  as stransstatus,ts.ntranscode from transactionstatus ts,approvalstatusconfig apsc where ts.ntranscode = apsc.ntranscode and apsc.nstatusfunctioncode = 1"
		+ " and apsc.napprovalsubtypecode = " + napprovalsubtypecode + " and apsc.nformcode = 55 "
		+ " and apsc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		+ " and ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		+ " and apsc.nsitecode = " + userInfo.getNmastersitecode() + " ;";

		List<ApprovalRoleFilterDetail> lstApprovalRoleFilterDetail = jdbcTemplate.query(str,
				new ApprovalRoleFilterDetail());

		str = " select distinct coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
		+ "',"
		+ "	ts.jsondata->'stransdisplaystatus'->>'en-US')  as stransstatus,ts.ntranscode from transactionstatus ts,approvalstatusconfig apsc where ts.ntranscode = apsc.ntranscode and apsc.nstatusfunctioncode = 2"
		+ " and apsc.napprovalsubtypecode = " + napprovalsubtypecode + " and apsc.nformcode = 55 "
		+ " and apsc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		+ " and ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		+ " and apsc.ntranscode <> " + Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
		+ " and apsc.nsitecode = " + userInfo.getNmastersitecode() + " ;";

		List<ApprovalRoleValidationDetail> lstApprovalRoleValidationDetail = jdbcTemplate.query(str,
				new ApprovalRoleValidationDetail());
		// ATE234 Janakumar ALPD-5316 Test Approval -> Decision Status there have based
		// on sample type.
		// decisionstatus
		str = " select distinct coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
		+ "'," + " ts.jsondata->'stransdisplaystatus'->>'en-US')  as stransstatus,ts.ntranscode "
		+ " from transactionstatus ts,approvalstatusconfig apsc where"
		+ " ts.ntranscode = apsc.ntranscode and apsc.nstatusfunctioncode = 3"
		+ " and apsc.napprovalsubtypecode = " + napprovalsubtypecode + " and apsc.nformcode = 55 "
		+ " and apsc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		+ " and ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		+ " and apsc.nsitecode = " + userInfo.getNmastersitecode() + " ;";

		List<ApprovalRoleDecisionDetail> lstApprovalRoleDecisionDetail = jdbcTemplate.query(str,
				new ApprovalRoleDecisionDetail());

		// actionstatus
		str = " select distinct ts.stransstatus,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ "	ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,ts.ntranscode from transactionstatus ts,approvalstatusconfig apsc where ts.ntranscode = apsc.ntranscode and apsc.nstatusfunctioncode = 4"
				+ " and apsc.napprovalsubtypecode = " + napprovalsubtypecode + " and apsc.nformcode = 55 "
				+ " and apsc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and apsc.nsitecode = " + userInfo.getNmastersitecode() + " ;";

		List<TransactionStatus> lstTransactionStatus = jdbcTemplate.query(str, new TransactionStatus());

		// approvalstatus
		str = " select distinct coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
		+ "',"
		+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,ts.ntranscode from transactionstatus ts,approvalstatusconfig apsc where ts.ntranscode = apsc.ntranscode and apsc.nstatusfunctioncode = 5"
		+ " and apsc.napprovalsubtypecode = " + napprovalsubtypecode + " and apsc.nformcode = 55 "
		+ " and apsc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		+ " and ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		+ " and apsc.nsitecode = " + userInfo.getNmastersitecode() + " ;";

		List<TransactionStatus> lstTransactionStatus1 = jdbcTemplate.query(str, new TransactionStatus());

		// Checklist For Batch
		str = " select distinct cl.schecklistname,cl.nchecklistcode,clv.nchecklistversioncode,clv.schecklistversionname"
				+ " from checklist cl,checklistversion clv ,approvalstatusconfig apsc "
				+ " where cl.nchecklistcode = clv.nchecklistcode and apsc.nstatusfunctioncode = 6 "
				+ " and clv.ntransactionstatus = apsc.ntranscode and apsc.napprovalsubtypecode = "
				+ napprovalsubtypecode + " and apsc.nformcode = 55 " + " and apsc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cl.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and clv.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cl.nsitecode = "
				+ userInfo.getNmastersitecode() + " and clv.nsitecode = " + userInfo.getNmastersitecode()
				+ " and apsc.nsitecode = " + userInfo.getNmastersitecode() + " ;";

		List<ChecklistVersion> lstChecklistVersion = jdbcTemplate.query(str, new ChecklistVersion());

		// role Config
		str = " select distinct ts.stransstatus,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,ts.ntranscode,apsc.nsorter from transactionstatus ts,approvalstatusconfig apsc where ts.ntranscode = apsc.ntranscode and apsc.nstatusfunctioncode = 7"
				+ " and apsc.napprovalsubtypecode = " + napprovalsubtypecode + " and apsc.nformcode = 55 "
				+ " and apsc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and apsc.nsitecode = " + userInfo.getNmastersitecode() + " ;";

		List<TransactionStatus> lstTransactionStatus2 = jdbcTemplate.query(str, new TransactionStatus());

		// version config
		str = " select distinct ts.stransstatus,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ "	ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,ts.ntranscode,apsc.nsorter from transactionstatus ts,approvalstatusconfig apsc where ts.ntranscode = apsc.ntranscode and apsc.nstatusfunctioncode = 8"
				+ " and apsc.napprovalsubtypecode = " + napprovalsubtypecode + " and apsc.nformcode = 55 "
				+ " and apsc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and apsc.nsitecode = " + userInfo.getNmastersitecode() + " ;";

		List<TransactionStatus> lstTransactionStatus3 = jdbcTemplate.query(str, new TransactionStatus());

		rtnMap.put("availableFilterStatus", lstApprovalRoleFilterDetail);
		rtnMap.put("availableValidStatus", lstApprovalRoleValidationDetail);
		// ATE234 Janakumar ALPD-5316 Test Approval -> Decision Status there have based
		// on sample type.
		rtnMap.put("availableDecisionStatus", lstApprovalRoleDecisionDetail);
		rtnMap.put("actionStatus", lstTransactionStatus);
		rtnMap.put("approvalStatus", lstTransactionStatus1);
		rtnMap.put("checklist", lstChecklistVersion);
		rtnMap.put("roleConfig", lstTransactionStatus2);
		rtnMap.put("versionConfig", lstTransactionStatus3);
		return new ResponseEntity<Object>(rtnMap, HttpStatus.OK);
	}

	@Override
	public Map<String, Object> getApprovalConfigSequenceNumbers(Map<String, Object> objMap, boolean editFlag)
			throws Exception {
		Map<String, Object> map = new HashMap<>();
		int roleCount = (int) objMap.get("rolecount");
		int filterDetailCount = (int) objMap.get("filterdetailcount");
		int validationDetailCount = (int) objMap.get("validationdetailcount");
		int decisionDetailCount = (int) objMap.get("decisiondetailcount");
		int actionDetailCount = (int) objMap.get("actioncount");
		if (roleCount > 0) {
			if (editFlag) {
				String str = "";
				str = " select stablename,nsequenceno from seqnoconfigurationmaster where stablename in ( N'approvalrolefilterdetail',N'approvalrolevalidationdetail',N'approvalroledecisiondetail',N'approvalroleactiondetail') and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				List<SeqNoConfigurationMaster> lstSeqNo = jdbcTemplate.query(str,
						new SeqNoConfigurationMaster());
				Map<String, Integer> seqMap = lstSeqNo.stream()
						.collect(Collectors.toMap(SeqNoConfigurationMaster::getStablename,
								seqNoConfigurationMaster -> seqNoConfigurationMaster.getNsequenceno()));
				int nseqRoleActionCode = seqMap.get("approvalroleactiondetail");
				int nseqRoleDecisionCode = seqMap.get("approvalroledecisiondetail");
				int nseqRoleFilterCode = seqMap.get("approvalrolefilterdetail");
				int nseqRoleValidationCode = seqMap.get("approvalrolevalidationdetail");
				str = " update seqnoconfigurationmaster set nsequenceno = nsequenceno+" + filterDetailCount
						+ " where stablename = N'approvalrolefilterdetail' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"
						+ " update seqnoconfigurationmaster set nsequenceno = nsequenceno+" + validationDetailCount
						+ " where stablename = N'approvalrolevalidationdetail' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"
						+ " update seqnoconfigurationmaster set nsequenceno = nsequenceno+" + actionDetailCount
						+ " where stablename = N'approvalroleactiondetail' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"
						+ " update seqnoconfigurationmaster set nsequenceno = nsequenceno+" + decisionDetailCount
						+ " where stablename = N'approvalroledecisiondetail' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";

				jdbcTemplate.execute(str);
				map.put("napprovalactioncode", nseqRoleActionCode);
				map.put("napprovaldecisioncode", nseqRoleDecisionCode);
				map.put("napprovalfiltercode", nseqRoleFilterCode);
				map.put("napprovalvalidationcode", nseqRoleValidationCode);

			} else {
				String str = "";
				str = " select stablename,nsequenceno from seqnoconfigurationmaster where stablename in (N'approvalconfigversion',N'approvalconfigautoapproval',"
						+ " N'approvalconfigrole',N'approvalrolefilterdetail',N'approvalrolevalidationdetail',N'approvalroledecisiondetail',N'approvalroleactiondetail')"
						+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
				List<SeqNoConfigurationMaster> lstSeqNo = jdbcTemplate.query(str,
						new SeqNoConfigurationMaster());
				Map<String, Integer> seqMap = lstSeqNo.stream()
						.collect(Collectors.toMap(SeqNoConfigurationMaster::getStablename,
								seqNoConfigurationMaster -> seqNoConfigurationMaster.getNsequenceno()));
				int nseqAutoApprovalCode = seqMap.get("approvalconfigautoapproval");
				int nseqApprovalConfigRoleCode = seqMap.get("approvalconfigrole");
				int nseqApproveConfVersionCode = seqMap.get("approvalconfigversion");
				int nseqRoleActionCode = seqMap.get("approvalroleactiondetail");
				int nseqRoleDecisionCode = seqMap.get("approvalroledecisiondetail");
				int nseqRoleFilterCode = seqMap.get("approvalrolefilterdetail");
				int nseqRoleValidationCode = seqMap.get("approvalrolevalidationdetail");

				str = "  update seqnoconfigurationmaster set nsequenceno = nsequenceno+1 where stablename = N'approvalconfigversion';"
						+ "  update seqnoconfigurationmaster set nsequenceno = nsequenceno+1 where stablename = N'approvalconfigautoapproval'"
						+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"
						+ "  update seqnoconfigurationmaster set nsequenceno = nsequenceno+" + roleCount
						+ " where stablename = N'approvalconfigrole' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"
						+ "  update seqnoconfigurationmaster set nsequenceno = nsequenceno+" + filterDetailCount
						+ " where stablename = N'approvalrolefilterdetail' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"
						+ "  update seqnoconfigurationmaster set nsequenceno = nsequenceno+" + validationDetailCount
						+ " where stablename = N'approvalrolevalidationdetail' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"
						+ "  update seqnoconfigurationmaster set nsequenceno = nsequenceno+" + actionDetailCount
						+ " where stablename = N'approvalroleactiondetail' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"
						+ "  update seqnoconfigurationmaster set nsequenceno = nsequenceno+" + decisionDetailCount
						+ " where stablename = N'approvalroledecisiondetail' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
				jdbcTemplate.execute(str);

				map.put("nautoapprovalcode", nseqAutoApprovalCode);
				map.put("napprovalconfigrolecode", nseqApprovalConfigRoleCode);
				map.put("napprovalconfigversioncode", nseqApproveConfVersionCode);
				map.put("napprovalactioncode", nseqRoleActionCode);
				map.put("napprovaldecisioncode", nseqRoleDecisionCode);
				map.put("napprovalfiltercode", nseqRoleFilterCode);
				map.put("napprovalvalidationcode", nseqRoleValidationCode);
			}

		}
		return map;
	}

	@Override
	public ResponseEntity<Object> createApprovalConfig(Map<String, Object> objMap) throws Exception {

		final String sQuery = " lock  table lockapprovalconfiguration "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(objMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		ApprovalConfigVersion objApprovalConfigVersion = objMapper.convertValue(objMap.get("approveconfigversion"),
				new TypeReference<ApprovalConfigVersion>() {
		});
		ApprovalConfigAutoapproval objApprovalConfigAutoapproval = objMapper.convertValue(
				objMap.get("approvalconfigautoapproval"), new TypeReference<ApprovalConfigAutoapproval>() {
				});
		List<ApprovalConfigRole> lstApprovalConfigRole = objMapper.convertValue(objMap.get("approvalconfigrole"),
				new TypeReference<List<ApprovalConfigRole>>() {
		});
		int napprovalConfigCode = (int) objMap.get("napprovalconfigcode");
		int ntreeVersionTempcode = (int) objMap.get("ntreeversiontempcode");
		int napproveConfVersionCode = (int) objMap.get("napprovalconfigversioncode") + 1;
		int nautoApprovalCode = (int) objMap.get("nautoapprovalcode") + 1;
		int napprovalConfigRoleCode = (int) objMap.get("napprovalconfigrolecode");
		int napprovalFilterCode = (int) objMap.get("napprovalfiltercode");
		int napprovalValidationCode = (int) objMap.get("napprovalvalidationcode");
		int napprovalDecisionCode = (int) objMap.get("napprovaldecisioncode");
		int napprovalActionCode = (int) objMap.get("napprovalactioncode");

		StringBuffer sb = new StringBuffer();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedObjects = new ArrayList<>();

		if (!lstApprovalConfigRole.isEmpty() && lstApprovalConfigRole != null) {
			String str = "select nautoapprovalcode from approvalconfigautoapproval where sversionname  = N'"
					+ stringUtilityFunction.replaceQuote(objApprovalConfigAutoapproval.getSversionname())
					+ "' and napprovalconfigcode = " + objMap.get("napprovalconfigcode") + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
					+ userInfo.getNmastersitecode() + "";
			List<ApprovalConfigAutoapproval> validateObject = jdbcTemplate.query(str,
					new ApprovalConfigAutoapproval());
			if (!validateObject.isEmpty()) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
			// insert approvalconfigversion table
			sb.append(
					" insert into approvalconfigversion (napproveconfversioncode,napprovalconfigcode,napproveconfversionno,ntransactionstatus,ntreeversiontempcode,ndesigntemplatemappingcode,sapproveconfversiondesc,sviewname,dmodifieddate,nsitecode,nstatus) values("
							+ napproveConfVersionCode + "," + napprovalConfigCode + ",-1,"
							+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ","
							+ objApprovalConfigVersion.getNtreeversiontempcode() + ",-1,'"
							+ objApprovalConfigVersion.getSapproveconfversiondesc() + "',''," + "'"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode()
							+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");");

			objApprovalConfigVersion.setNapproveconfversioncode(napproveConfVersionCode);
			multilingualIDList.add("IDS_ADDVERSION");
			savedObjects.add(objApprovalConfigVersion);

			// insert approvalconfigautoapproval table
			sb.append(
					"insert into approvalconfigautoapproval (nautoapprovalcode,napprovalconfigcode,napprovalconfigversioncode,sversionname,nneedautocomplete,nneedautoapproval,nautodescisionstatus,nautoapprovalstatus,nautoallot,nneedjoballocation,nneedautoinnerband,nneedautoouterband,dmodifieddate,nstatus,nsitecode)"
							+ " values(" + nautoApprovalCode + "," + napprovalConfigCode + "," + napproveConfVersionCode
							+ ",N'"
							+ stringUtilityFunction.replaceQuote(objApprovalConfigAutoapproval.getSversionname()) + "',"
							+ objApprovalConfigAutoapproval.getNneedautocomplete() + ","
							+ objApprovalConfigAutoapproval.getNneedautoapproval() + ","
							+ objApprovalConfigAutoapproval.getNautodescisionstatus() + ","
							+ objApprovalConfigAutoapproval.getNautoapprovalstatus() + ","
							+ objApprovalConfigAutoapproval.getNautoallot() + ","
							+ objApprovalConfigAutoapproval.getNneedjoballocation() + ","
							+ objApprovalConfigAutoapproval.getNneedautoinnerband() + ","
							+ objApprovalConfigAutoapproval.getNneedautoouterband() + ",'"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ", "
							+ userInfo.getNmastersitecode() + ");");

			objApprovalConfigAutoapproval.setNautoapprovalcode(nautoApprovalCode);
			multilingualIDList.add("IDS_ADDVERSION");
			savedObjects.add(objApprovalConfigAutoapproval);

			for (ApprovalConfigRole objApprovalConfigRole : lstApprovalConfigRole) {
				napprovalConfigRoleCode++;
				objApprovalConfigRole.setNapprovalconfigrolecode(napprovalConfigRoleCode);
				objApprovalConfigRole.setNapproveconfversioncode(napproveConfVersionCode);
				objApprovalConfigRole.setNtreeversiontempcode(ntreeVersionTempcode);

				multilingualIDList.add("IDS_ADDVERSIONROLE");
				savedObjects.add(objApprovalConfigRole);

				// insert approval config role
				sb.append(
						"insert into approvalconfigrole (napprovalconfigrolecode,napprovalconfigcode,ntreeversiontempcode,nuserrolecode,"
								+ " nchecklistversioncode,npartialapprovalneed,nsectionwiseapprovalneed,nrecomretestneed,nrecomrecalcneed,nretestneed,nrecalcneed,nlevelno,napprovalstatuscode,napproveconfversioncode,"
								+ " nautoapproval,nautoapprovalstatuscode,nautodescisionstatuscode,ncorrectionneed,nesignneed,ntransactionstatus,dmodifieddate,nsitecode,nstatus) values ("
								+ napprovalConfigRoleCode + "," + napprovalConfigCode + "," + ntreeVersionTempcode + ","
								+ objApprovalConfigRole.getNuserrolecode() + ","
								+ objApprovalConfigRole.getNchecklistversioncode() + ","
								+ objApprovalConfigRole.getNpartialapprovalneed() + ","
								+ objApprovalConfigRole.getNsectionwiseapprovalneed() + ","
								+ objApprovalConfigRole.getNrecomretestneed() + ","
								+ objApprovalConfigRole.getNrecomrecalcneed() + ","
								+ objApprovalConfigRole.getNretestneed() + "," + objApprovalConfigRole.getNrecalcneed()
								+ "," + objApprovalConfigRole.getNlevelno() + ","
								+ objApprovalConfigRole.getNapprovalstatuscode() + "," + napproveConfVersionCode + ","
								+ objApprovalConfigRole.getNautoapproval() + ","
								+ objApprovalConfigRole.getNautoapprovalstatuscode() + ","
								+ objApprovalConfigRole.getNautodescisionstatuscode() + ","
								+ objApprovalConfigRole.getNcorrectionneed() + ","
								+ objApprovalConfigRole.getNesignneed() + ","
								+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ",'"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
								+ userInfo.getNmastersitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");");

				List<ApprovalRoleFilterDetail> lstFilterDetails = objMapper.convertValue(
						objMap.get("approvalconfigrolefilterdetail_" + objApprovalConfigRole.getNuserrolecode()),
						new TypeReference<List<ApprovalRoleFilterDetail>>() {
						});

				List<ApprovalRoleValidationDetail> lstValidationDetails = objMapper.convertValue(
						objMap.get("approvalconfigrolevalidationdetail_" + objApprovalConfigRole.getNuserrolecode()),
						new TypeReference<List<ApprovalRoleValidationDetail>>() {
						});

				List<ApprovalRoleDecisionDetail> lstDecisionDetails = objMapper.convertValue(
						objMap.get("approvalconfigroledecisiondetail_" + objApprovalConfigRole.getNuserrolecode()),
						new TypeReference<List<ApprovalRoleDecisionDetail>>() {
						});

				List<ApprovalRoleActionDetail> lstActionDetails = objMapper.convertValue(
						objMap.get("approvalconfigroleactiondetail_" + objApprovalConfigRole.getNuserrolecode()),
						new TypeReference<List<ApprovalRoleActionDetail>>() {
						});

				// insert for approvalrolefilterdetail
				short defaultStatus = (short) Enumeration.TransactionStatus.YES.gettransactionstatus();
				for (ApprovalRoleFilterDetail objFilterDetail : lstFilterDetails) {
					++napprovalFilterCode;
					sb.append(
							"insert into approvalrolefilterdetail (napprovalfiltercode,napprovalconfigrolecode,napprovalconfigcode,nuserrolecode,nlevelno,ntransactionstatus,ndefaultstatus,dmodifieddate,nsitecode,nstatus)"
									+ " values(" + napprovalFilterCode + "," + napprovalConfigRoleCode + ","
									+ napprovalConfigCode + "," + objFilterDetail.getNuserrolecode() + ","
									+ objApprovalConfigRole.getNlevelno() + ","
									+ objFilterDetail.getNtransactionstatus() + "," + defaultStatus + ",'"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
									+ userInfo.getNmastersitecode() + ", "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");");

					objFilterDetail.setNdefaultstatus(defaultStatus);
					defaultStatus = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();
					objFilterDetail.setNapprovalfiltercode(napprovalFilterCode);
					objFilterDetail.setNapprovalconfigrolecode(napprovalConfigRoleCode);
					multilingualIDList.add("IDS_ADDFILTERSTATUS");
					savedObjects.add(objFilterDetail);

				}
				// insert for approvalconfigrolevalidationdetail
				for (ApprovalRoleValidationDetail objValidationDetail : lstValidationDetails) {
					++napprovalValidationCode;
					sb.append(
							"insert into approvalrolevalidationdetail (napprovalvalidationcode,napprovalconfigrolecode,napprovalconfigcode,nuserrolecode,nlevelno,ntransactionstatus,dmodifieddate,nsitecode,nstatus)"
									+ " values(" + napprovalValidationCode + "," + napprovalConfigRoleCode + ","
									+ napprovalConfigCode + "," + objApprovalConfigRole.getNuserrolecode() + ","
									+ objApprovalConfigRole.getNlevelno() + ","
									+ objValidationDetail.getNtransactionstatus() + ",'"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
									+ userInfo.getNmastersitecode() + ", "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");");

					objValidationDetail.setNapprovalvalidationcode(napprovalValidationCode);
					objValidationDetail.setNapprovalconfigrolecode(napprovalConfigRoleCode);

					multilingualIDList.add("IDS_ADDVALIDATIONSTATUS");
					savedObjects.add(objValidationDetail);
				}
				// insert for approvalconfigroledecisiondetail
				defaultStatus = (short) Enumeration.TransactionStatus.YES.gettransactionstatus();
				for (ApprovalRoleDecisionDetail objDecisionDetail : lstDecisionDetails) {
					++napprovalDecisionCode;
					sb.append(
							"insert into approvalroledecisiondetail (napprovaldecisioncode,napprovalconfigrolecode,napprovalconfigcode,nuserrolecode,nlevelno,ntransactionstatus,ndefaultstatus,dmodifieddate,nsitecode,nstatus)"
									+ " values(" + napprovalDecisionCode + "," + napprovalConfigRoleCode + ","
									+ napprovalConfigCode + "," + objApprovalConfigRole.getNuserrolecode() + ","
									+ objApprovalConfigRole.getNlevelno() + ","
									+ objDecisionDetail.getNtransactionstatus() + "," + defaultStatus + ",'"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
									+ userInfo.getNmastersitecode() + ", "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");");
					defaultStatus = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();
					objDecisionDetail.setNapprovaldecisioncode(napprovalDecisionCode);
					objDecisionDetail.setNapprovalconfigrolecode(napprovalConfigRoleCode);
					objDecisionDetail.setNdefaultstatus(defaultStatus);
					multilingualIDList.add("IDS_ADDDECISIONSTATUS");
					savedObjects.add(objDecisionDetail);

				}
				// insert for approvalroleactiondetail
				for (ApprovalRoleActionDetail objActionDetail : lstActionDetails) {
					++napprovalActionCode;
					sb.append(
							"insert into approvalroleactiondetail (napprovalactioncode,napprovalconfigrolecode,napprovalconfigcode,nuserrolecode,nlevelno,ntransactionstatus,dmodifieddate,nsitecode,nstatus)"
									+ " values(" + napprovalActionCode + "," + napprovalConfigRoleCode + ","
									+ napprovalConfigCode + "," + objApprovalConfigRole.getNuserrolecode() + ","
									+ objApprovalConfigRole.getNlevelno() + ","
									+ objActionDetail.getNtransactionstatus() + ",'"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
									+ userInfo.getNmastersitecode() + ", "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");");

					// multilingualIDList.add("IDS_ADDACTIONSTATUS");
					// savedObjects.add(objActionDetail);
				}

			}
			jdbcTemplate.execute(sb.toString());
			multilingualIDList.add("IDS_ADDVERSIONROLE");
			auditUtilityFunction.fnInsertAuditAction(savedObjects, 1, null, multilingualIDList, userInfo);
		}
		Map<String, Object> getMap = new HashMap<String, Object>();
		getMap.put("napprovalconfigcode", napprovalConfigCode);
		getMap.put("napprovalsubtypecode", (int) objMap.get("napprovalsubtypecode"));
		getMap.put("ntreeversiontempcode", (int) objMap.get("ntreeversiontempcode"));
		return getApprovalConfigVersion(getMap, userInfo);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getApprovalConfigEditData(Map<String, Object> objMap) throws Exception {
		String str = "";
		Map<String, Object> rtnMap = new LinkedHashMap<String, Object>();
		int napprovalConfigVersionCode = (int) objMap.get("napprovalconfigversioncode");
		int napprovalsubtypecode = (int) objMap.get("napprovalsubtypecode");
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(objMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		ApprovalConfigAutoapproval objApprovalConfigVersion = getApprovalConfigVersionByID(napprovalConfigVersionCode,
				userInfo);
		if (objApprovalConfigVersion != null) {
			if (objApprovalConfigVersion.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT
					.gettransactionstatus()) {
				List<ApprovalConfigRole> lstApprovalConfigRole = getApprovalConfigVersionRole(
						napprovalConfigVersionCode, userInfo);
				if (!lstApprovalConfigRole.isEmpty() && lstApprovalConfigRole != null) {
					rtnMap.put("versiondetails", objApprovalConfigVersion);
					rtnMap.put("userroletree", lstApprovalConfigRole);
					rtnMap.putAll(
							(Map<String, Object>) getAvailableComboData(napprovalsubtypecode, userInfo).getBody());
					for (ApprovalConfigRole objApprovalConfigRole : lstApprovalConfigRole) {
						str = "select coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
						+ "',"
						+ " ts.jsondata->'stransdisplaystatus'->>'en-US')  as stransstatus,ts.ntranscode from  approvalrolefilterdetail afd,transactionstatus ts "
						+ " where afd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and afd.ntransactionstatus = ts.ntranscode and afd.napprovalconfigrolecode = "
						+ objApprovalConfigRole.getNapprovalconfigrolecode() + " and afd.nsitecode = "
						+ userInfo.getNmastersitecode() + "; ";

						List<ApprovalRoleFilterDetail> lstApprovalRoleFilterDetail = jdbcTemplate.query(str,
								new ApprovalRoleFilterDetail());

						str = "select coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
						+ "',"
						+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as stransstatus,ts.ntranscode from  approvalrolevalidationdetail acv,transactionstatus ts "
						+ " where acv.ntransactionstatus = ts.ntranscode" + " and acv.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and acv.napprovalconfigrolecode = "
						+ objApprovalConfigRole.getNapprovalconfigrolecode() + " and acv.nsitecode = "
						+ userInfo.getNmastersitecode() + ";";

						List<ApprovalRoleValidationDetail> lstApprovalRoleValidationDetail = jdbcTemplate.query(str,
								new ApprovalRoleValidationDetail());

						str = " select coalesce(ts.jsondata->'stransdisplaystatus'->>'"
								+ userInfo.getSlanguagetypecode() + "',"
								+ "	ts.jsondata->'stransdisplaystatus'->>'en-US') as stransstatus,ts.ntranscode from approvalroledecisiondetail acv,transactionstatus ts "
								+ " where acv.ntransactionstatus = ts.ntranscode" + " and acv.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acv.nsitecode = "
								+ userInfo.getNmastersitecode() + " and acv.napprovalconfigrolecode = "
								+ objApprovalConfigRole.getNapprovalconfigrolecode() + ";";

						List<ApprovalRoleDecisionDetail> lstApprovalRoleDecisionDetail = jdbcTemplate.query(str,
								new ApprovalRoleDecisionDetail());

						str = "select acv.napprovalconfigrolecode, acv.napprovalconfigcode, acv.ntreeversiontempcode, acv.nuserrolecode, acv.nchecklistversioncode, acv.npartialapprovalneed, acv.nsectionwiseapprovalneed,"
								+ " acv.nrecomretestneed, acv.nrecomrecalcneed, acv.nretestneed, acv.nrecalcneed, acv.nlevelno, acv.napprovalstatuscode, acv.napproveconfversioncode, acv.nautoapproval,"
								+ " acv.nautoapprovalstatuscode, acv.nautodescisionstatuscode, acv.ncorrectionneed, acv.nesignneed, acv.ntransactionstatus, acv.dmodifieddate, acv.nsitecode, acv.nstatus, coalesce(ts.jsondata->'stransdisplaystatus'->>'"
								+ userInfo.getSlanguagetypecode() + "',"
								+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sapprovalstatus from  approvalconfigrole acv,transactionstatus ts "
								+ " where acv.napprovalstatuscode = ts.ntranscode " + " and acv.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and acv.napprovalconfigrolecode = "
								+ objApprovalConfigRole.getNapprovalconfigrolecode() + ";";

						List<ApprovalConfigRole> lstApprovalConfigRole1 = jdbcTemplate.query(str,
								new ApprovalConfigRole());

						str = " select ts.stransstatus,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
								+ userInfo.getSlanguagetypecode() + "',"
								+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,ts.ntranscode,apsc.nsorter from transactionstatus ts,approvalstatusconfig apsc where ts.ntranscode = apsc.ntranscode and apsc.nstatusfunctioncode = 4"
								+ " and apsc.napprovalsubtypecode = " + napprovalsubtypecode
								+ " and apsc.nformcode = 55 " + " and apsc.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and apsc.nsitecode = "
								+ userInfo.getNmastersitecode() + " ;";

						List<TransactionStatus> lstTransactionStatus = jdbcTemplate.query(str, new TransactionStatus());

						str = " select cl.schecklistname,cl.nchecklistcode,clv.nchecklistversioncode,clv.schecklistversionname"
								+ " from  approvalconfigrole acv,checklist cl,checklistversion clv"
								+ " where cl.nchecklistcode = clv.nchecklistcode and acv.nchecklistversioncode = clv.nchecklistversioncode  "
								+ " and acv.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and cl.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and clv.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and acv.napprovalconfigrolecode = "
								+ objApprovalConfigRole.getNapprovalconfigrolecode() + " and cl.nsitecode = "
								+ userInfo.getNmastersitecode() + " and clv.nsitecode = "
								+ userInfo.getNmastersitecode() + ";";

						List<ChecklistVersion> lstChecklistVersion = jdbcTemplate.query(str, new ChecklistVersion());

						rtnMap.put("filterstatus_" + objApprovalConfigRole.getNuserrolecode(),
								lstApprovalRoleFilterDetail);
						rtnMap.put("validationstatus_" + objApprovalConfigRole.getNuserrolecode(),
								lstApprovalRoleValidationDetail);
						rtnMap.put("decisionstatus_" + objApprovalConfigRole.getNuserrolecode(),
								lstApprovalRoleDecisionDetail);
						rtnMap.put("ActionStatus", lstTransactionStatus);
						rtnMap.put("roledetails_" + objApprovalConfigRole.getNuserrolecode(), lstApprovalConfigRole1);
						rtnMap.put("checklist_" + objApprovalConfigRole.getNuserrolecode(), lstChecklistVersion);
					}
					return new ResponseEntity<Object>(rtnMap, HttpStatus.OK);
				} else {
					return new ResponseEntity<Object>(
							commonFunction.getMultilingualMessage("IDS_APPROVEDTEMPLATEVERSIONNOTAVAILABLE",
									userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.SELECTDRAFTVERSION.getreturnstatus(), userInfo.getSlanguagefilename()),
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
	public ResponseEntity<Object> updateApprovalConfigVersion(Map<String, Object> objMap, UserInfo userInfo)
			throws Exception {

		final String sQuery = " lock  table lockapprovalconfiguration "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		List<String> multiLingualList = new ArrayList<>();
		List<String> addmultiLingualList = new ArrayList<>();
		List<Object> lstNewObject = new ArrayList<>();
		List<Object> lstOldObject = new ArrayList<>();
		List<Object> addedObjects = new ArrayList<>();
		List<Object> addedObject = new ArrayList<>();
		List<Object> deletededObjects = new ArrayList<>();

		int napprovalFilterCode = (int) objMap.get("napprovalfiltercode");
		int napprovalValidationCode = (int) objMap.get("napprovalvalidationcode");
		int napprovalDecisionCode = (int) objMap.get("napprovaldecisioncode");
		int napprovalActionCode = (int) objMap.get("napprovalactioncode");
		String str = "";
		ObjectMapper objMapper = new ObjectMapper();

		ApprovalConfigAutoapproval objApprovalConfigAutoapproval = objMapper.convertValue(
				objMap.get("approvalconfigautoapproval"), new TypeReference<ApprovalConfigAutoapproval>() {
				});

		ApprovalConfigVersion approvedApprovalConfigVersion = getApprovalConfigVersionByTransStatus(
				objApprovalConfigAutoapproval.getNapprovalconfigversioncode());

		if (approvedApprovalConfigVersion != null) {
			return new ResponseEntity<Object>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.SELECTDRAFTVERSION.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		str = "select nautoapprovalcode from approvalconfigautoapproval where sversionname  = N'"
				+ stringUtilityFunction.replaceQuote(objApprovalConfigAutoapproval.getSversionname())
				+ "' and napprovalconfigcode = " + objMap.get("napprovalconfigcode") + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and napprovalconfigversioncode <> "
				+ objApprovalConfigAutoapproval.getNapprovalconfigversioncode() + " and nsitecode = "
				+ userInfo.getNmastersitecode();
		List<ApprovalConfigAutoapproval> validateObject = jdbcTemplate.query(str,
				new ApprovalConfigAutoapproval());
		if (!validateObject.isEmpty()) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
		str = " select nautoapprovalcode, napprovalconfigcode, napprovalconfigversioncode, sversionname, nneedautocomplete,"
				+ " nneedautoapproval, nautodescisionstatus, nautoapprovalstatus, nautoallot, nneedjoballocation, nneedautoinnerband,"
				+ " nneedautoouterband, dmodifieddate, nsitecode, nstatus"
				+ " from  approvalconfigautoapproval where napprovalconfigversioncode = "
				+ objApprovalConfigAutoapproval.getNapprovalconfigversioncode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
				+ userInfo.getNmastersitecode();

		ApprovalConfigAutoapproval oldApprovalConfigAutoapproval = (ApprovalConfigAutoapproval) jdbcUtilityFunction
				.queryForObject(str, ApprovalConfigAutoapproval.class, jdbcTemplate);
		if (oldApprovalConfigAutoapproval == null) {
			return new ResponseEntity<Object>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		final String updateAutoApproval = "update approvalconfigautoapproval set dmodifieddate = '"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " sversionname = N'"
				+ stringUtilityFunction.replaceQuote(objApprovalConfigAutoapproval.getSversionname())
				+ "',nneedautocomplete = " + objApprovalConfigAutoapproval.getNneedautocomplete()
				+ ",nneedautoapproval = " + objApprovalConfigAutoapproval.getNneedautoapproval() + ",nautoallot = "
				+ objApprovalConfigAutoapproval.getNautoallot() + ",nneedjoballocation = "
				+ objApprovalConfigAutoapproval.getNneedjoballocation() + ",nneedautoinnerband = "
				+ objApprovalConfigAutoapproval.getNneedautoinnerband() + ",nneedautoouterband = "
				+ objApprovalConfigAutoapproval.getNneedautoouterband() + ",nautodescisionstatus = "
				+ objApprovalConfigAutoapproval.getNautodescisionstatus() + ",nautoapprovalstatus = "
				+ objApprovalConfigAutoapproval.getNautoapprovalstatus() + " where napprovalconfigcode = "
				+ objApprovalConfigAutoapproval.getNapprovalconfigcode() + " and napprovalconfigversioncode = "
				+ objApprovalConfigAutoapproval.getNapprovalconfigversioncode() + " and nsitecode = "
				+ userInfo.getNmastersitecode();
		jdbcTemplate.execute(updateAutoApproval);
		multiLingualList.add("IDS_EDITAPPROVALCONFIGVERSION");
		lstOldObject.add(oldApprovalConfigAutoapproval);
		lstNewObject.add(objApprovalConfigAutoapproval);
		List<ApprovalConfigRole> lstApprovalConfigRole = objMapper.convertValue(objMap.get("approvalconfigrole"),
				new TypeReference<List<ApprovalConfigRole>>() {
		});

		final String insertActionDetail = "insert into approvalroleactiondetail (napprovalactioncode,napprovalconfigrolecode,"
				+ "napprovalconfigcode,nuserrolecode,nlevelno,ntransactionstatus,dmodifieddate,nsitecode,nstatus)  values";
		final String insertValidationDetail = "insert into approvalrolevalidationdetail (napprovalvalidationcode,napprovalconfigrolecode,"
				+ "napprovalconfigcode,nuserrolecode,nlevelno,ntransactionstatus,dmodifieddate,nsitecode,nstatus) values";
		final String insertFilterDetail = "insert into approvalrolefilterdetail (napprovalfiltercode,napprovalconfigrolecode,"
				+ "napprovalconfigcode,nuserrolecode,nlevelno,ntransactionstatus,ndefaultstatus,dmodifieddate,nsitecode,nstatus)  values";
		final String insertDecisionDetail = "insert into approvalroledecisiondetail (napprovaldecisioncode,napprovalconfigrolecode,"
				+ "napprovalconfigcode,nuserrolecode,nlevelno,ntransactionstatus,ndefaultstatus,dmodifieddate,nsitecode,nstatus) values";

		String actionValues = "";
		String filterValues = "";
		String decisonValues = "";
		String validationValues = "";
		List<String> roleArray = new ArrayList<String>();

		for (ApprovalConfigRole objApprovalConfigRole : lstApprovalConfigRole) {

			str = " select napprovalconfigrolecode, napprovalconfigcode, ntreeversiontempcode, nuserrolecode, nchecklistversioncode, npartialapprovalneed,"
					+ " nsectionwiseapprovalneed, nrecomretestneed, nrecomrecalcneed, nretestneed, nrecalcneed, nlevelno, napprovalstatuscode, napproveconfversioncode,"
					+ " nautoapproval, nautoapprovalstatuscode, nautodescisionstatuscode, ncorrectionneed, nesignneed, ntransactionstatus, dmodifieddate, nsitecode, nstatus from approvalconfigrole where napprovalconfigrolecode = "
					+ objApprovalConfigRole.getNapprovalconfigrolecode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			ApprovalConfigRole oldobjApprovalConfigRole = jdbcTemplate.queryForObject(str,
					new ApprovalConfigRole());

			final String updateRole = "update approvalconfigrole set dmodifieddate = '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + " nchecklistversioncode = "
					+ objApprovalConfigRole.getNchecklistversioncode() + "," + " npartialapprovalneed = "
					+ objApprovalConfigRole.getNpartialapprovalneed() + "," + " nsectionwiseapprovalneed = "
					+ objApprovalConfigRole.getNsectionwiseapprovalneed() + "," + " nrecomretestneed = "
					+ objApprovalConfigRole.getNrecomretestneed() + "," + " nrecomrecalcneed = "
					+ objApprovalConfigRole.getNrecomrecalcneed() + "," + " nretestneed = "
					+ objApprovalConfigRole.getNretestneed() + "," + " nrecalcneed = "
					+ objApprovalConfigRole.getNrecalcneed() + "," + " napprovalstatuscode = "
					+ objApprovalConfigRole.getNapprovalstatuscode() + "," + " nautoapproval = "
					+ objApprovalConfigRole.getNautoapproval() + "," + " nautoapprovalstatuscode = "
					+ objApprovalConfigRole.getNautoapprovalstatuscode() + "," + " nautodescisionstatuscode = "
					+ objApprovalConfigRole.getNautodescisionstatuscode() + "," + " ncorrectionneed = "
					+ objApprovalConfigRole.getNcorrectionneed() + "," + " nesignneed = "
					+ objApprovalConfigRole.getNesignneed() + " where napprovalconfigrolecode = "
					+ objApprovalConfigRole.getNapprovalconfigrolecode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			jdbcTemplate.execute(updateRole);

			multiLingualList.add("IDS_EDITAPPROVALCONFIGROLE");
			lstOldObject.add(oldobjApprovalConfigRole);
			lstNewObject.add(objApprovalConfigRole);

			List<ApprovalRoleFilterDetail> lstFilterDetails = objMapper.convertValue(
					objMap.get("approvalconfigrolefilterdetail_" + objApprovalConfigRole.getNuserrolecode()),
					new TypeReference<List<ApprovalRoleFilterDetail>>() {
					});

			List<ApprovalRoleValidationDetail> lstValidationDetails = objMapper.convertValue(
					objMap.get("approvalconfigrolevalidationdetail_" + objApprovalConfigRole.getNuserrolecode()),
					new TypeReference<List<ApprovalRoleValidationDetail>>() {
					});

			List<ApprovalRoleDecisionDetail> lstDecisionDetails = objMapper.convertValue(
					objMap.get("approvalconfigroledecisiondetail_" + objApprovalConfigRole.getNuserrolecode()),
					new TypeReference<List<ApprovalRoleDecisionDetail>>() {
					});

			List<ApprovalRoleActionDetail> lstActionDetails = objMapper.convertValue(
					objMap.get("approvalconfigroleactiondetail_" + objApprovalConfigRole.getNuserrolecode()),
					new TypeReference<List<ApprovalRoleActionDetail>>() {
					});

			str = "select napprovalfiltercode, napprovalconfigrolecode, napprovalconfigcode, nuserrolecode, nlevelno, ndefaultstatus, ntransactionstatus, "
					+ "dmodifieddate, nsitecode, nstatus from approvalrolefilterdetail where napprovalconfigrolecode = "
					+ objApprovalConfigRole.getNapprovalconfigrolecode() + " and nsitecode = "
					+ userInfo.getNmastersitecode() + ";";
			List<ApprovalRoleFilterDetail> lstApprovalRoleFilterDetail = jdbcTemplate.query(str,
					new ApprovalRoleFilterDetail());

			str = "select napprovalvalidationcode, napprovalconfigrolecode, napprovalconfigcode, nuserrolecode, nlevelno, ntransactionstatus, "
					+ "dmodifieddate, nsitecode, nstatus from approvalrolevalidationdetail where napprovalconfigrolecode = "
					+ objApprovalConfigRole.getNapprovalconfigrolecode() + " and nsitecode = "
					+ userInfo.getNmastersitecode() + ";";
			List<ApprovalRoleValidationDetail> lstApprovalRoleValidationDetail = jdbcTemplate.query(str,
					new ApprovalRoleValidationDetail());

			str = "select napprovaldecisioncode, napprovalconfigrolecode, napprovalconfigcode, nuserrolecode, nlevelno, ndefaultstatus, ntransactionstatus,"
					+ " dmodifieddate, nsitecode, nstatus from approvalroledecisiondetail where napprovalconfigrolecode = "
					+ objApprovalConfigRole.getNapprovalconfigrolecode() + " and nsitecode = "
					+ userInfo.getNmastersitecode() + ";";
			List<ApprovalRoleDecisionDetail> lstApprovalRoleDecisionDetail = jdbcTemplate.query(str,
					new ApprovalRoleDecisionDetail());

			str = "select napprovalactioncode, napprovalconfigrolecode, napprovalconfigcode, nuserrolecode, nlevelno, ntransactionstatus,"
					+ " dmodifieddate, nsitecode, nstatus from approvalroleactiondetail where napprovalconfigrolecode = "
					+ objApprovalConfigRole.getNapprovalconfigrolecode() + " and nsitecode = "
					+ userInfo.getNmastersitecode() + ";";

			deletededObjects.add(lstApprovalRoleFilterDetail);
			deletededObjects.add(lstApprovalRoleValidationDetail);
			deletededObjects.add(lstApprovalRoleDecisionDetail);

			roleArray.add(String.valueOf(objApprovalConfigRole.getNapprovalconfigrolecode()));

			short defaultStatus = (short) Enumeration.TransactionStatus.YES.gettransactionstatus();
			for (ApprovalRoleFilterDetail objFilterDetail : lstFilterDetails) {

				++napprovalFilterCode;

				objFilterDetail.setNapprovalfiltercode(napprovalFilterCode);
				objFilterDetail.setNdefaultstatus(defaultStatus);
				objFilterDetail.setNapprovalconfigrolecode(objApprovalConfigRole.getNapprovalconfigrolecode());

				filterValues += "(" + napprovalFilterCode + "," + objApprovalConfigRole.getNapprovalconfigrolecode()
				+ "," + objApprovalConfigRole.getNapprovalconfigcode() + ","
				+ objFilterDetail.getNuserrolecode() + "," + objApprovalConfigRole.getNlevelno() + ","
				+ objFilterDetail.getNtransactionstatus() + "," + defaultStatus + ",'"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ", "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
				defaultStatus = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

				addmultiLingualList.add("IDS_ADDFILTERSTATUS");
				addedObject.add(objFilterDetail);

			}

			for (ApprovalRoleValidationDetail objValidationDetail : lstValidationDetails) {

				++napprovalValidationCode;
				objValidationDetail.setNapprovalvalidationcode(napprovalValidationCode);
				objValidationDetail.setNapprovalconfigrolecode(objApprovalConfigRole.getNapprovalconfigrolecode());

				validationValues += "(" + napprovalValidationCode + ","
						+ objApprovalConfigRole.getNapprovalconfigrolecode() + ","
						+ objApprovalConfigRole.getNapprovalconfigcode() + ","
						+ objApprovalConfigRole.getNuserrolecode() + "," + objApprovalConfigRole.getNlevelno() + ","
						+ objValidationDetail.getNtransactionstatus() + ",'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";

				addmultiLingualList.add("IDS_ADDVALIDATIONSTATUS");
				addedObject.add(objValidationDetail);

			}
			defaultStatus = (short) Enumeration.TransactionStatus.YES.gettransactionstatus();
			for (ApprovalRoleDecisionDetail objDecisionDetail : lstDecisionDetails) {

				++napprovalDecisionCode;

				objDecisionDetail.setNapprovaldecisioncode(napprovalDecisionCode);
				objDecisionDetail.setNdefaultstatus(defaultStatus);
				objDecisionDetail.setNapprovalconfigrolecode(objApprovalConfigRole.getNapprovalconfigrolecode());
				;

				decisonValues += "(" + napprovalDecisionCode + "," + objApprovalConfigRole.getNapprovalconfigrolecode()
				+ "," + objApprovalConfigRole.getNapprovalconfigcode() + ","
				+ objApprovalConfigRole.getNuserrolecode() + "," + objApprovalConfigRole.getNlevelno() + ","
				+ objDecisionDetail.getNtransactionstatus() + "," + defaultStatus + ",'"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ", "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
				defaultStatus = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

				addmultiLingualList.add("IDS_ADDDECISIONSTATUS");
				addedObject.add(objDecisionDetail);
			}

			for (ApprovalRoleActionDetail objActionDetail : lstActionDetails) {

				++napprovalActionCode;

				objActionDetail.setNapprovalactioncode(napprovalActionCode);
				objActionDetail.setNapprovalconfigrolecode(objApprovalConfigRole.getNapprovalconfigrolecode());

				actionValues += "(" + napprovalActionCode + "," + objApprovalConfigRole.getNapprovalconfigrolecode()
				+ "," + objApprovalConfigRole.getNapprovalconfigcode() + ","
				+ objApprovalConfigRole.getNuserrolecode() + "," + objApprovalConfigRole.getNlevelno() + ","
				+ objActionDetail.getNtransactionstatus() + ",'"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ", "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";

			}

		}

		jdbcTemplate.execute(" delete from approvalrolefilterdetail where napprovalconfigrolecode in ("
				+ String.join(", ", roleArray) + ")" + " and nsitecode = " + userInfo.getNmastersitecode());
		jdbcTemplate.execute(" delete from approvalrolevalidationdetail where napprovalconfigrolecode in ("
				+ String.join(", ", roleArray) + ")" + " and nsitecode = " + userInfo.getNmastersitecode());
		jdbcTemplate.execute(" delete from approvalroledecisiondetail where napprovalconfigrolecode in ("
				+ String.join(", ", roleArray) + ")" + " and nsitecode = " + userInfo.getNmastersitecode());
		jdbcTemplate.execute(" delete from approvalroleactiondetail where napprovalconfigrolecode in ("
				+ String.join(", ", roleArray) + ")" + " and nsitecode = " + userInfo.getNmastersitecode());
		jdbcTemplate.execute(insertActionDetail + actionValues.substring(0, actionValues.length() - 1) + ";");
		jdbcTemplate.execute(insertFilterDetail + filterValues.substring(0, filterValues.length() - 1) + ";");
		jdbcTemplate
		.execute(insertValidationDetail + validationValues.substring(0, validationValues.length() - 1) + ";");
		if (!decisonValues.isEmpty()) {
			jdbcTemplate.execute(insertDecisionDetail + decisonValues.substring(0, decisonValues.length() - 1) + ";");
		}
		auditUtilityFunction.fnInsertAuditAction(lstNewObject, 2, lstOldObject, multiLingualList, userInfo);
		multiLingualList = new ArrayList<>();
		multiLingualList.add("IDS_DELETEFILTERSTATUS");
		multiLingualList.add("IDS_DELETEVALIDATIONSTATUS");
		multiLingualList.add("IDS_DELETEDECISIONSTATUS");
		auditUtilityFunction.fnInsertListAuditAction(deletededObjects, 1, null, multiLingualList, userInfo);
		addedObjects.add(addedObject);
		auditUtilityFunction.fnInsertAuditAction(addedObject, 1, null, addmultiLingualList, userInfo);

		Map<String, Object> getMap = new HashMap<String, Object>();
		getMap.put("napprovalconfigversioncode", objApprovalConfigAutoapproval.getNapprovalconfigversioncode());
		getMap.put("napprovalsubtypecode", objMap.get("napprovalsubtypecode"));
		return getApprovalConfigVersion(getMap, userInfo);

	}

	@Override
	public ResponseEntity<Object> deleteApprovalConfigVersion(Map<String, Object> objMap, UserInfo userInfo)
			throws Exception {
		int napprovalConfigVersionCode = (int) objMap.get("napprovalconfigversioncode");
		ApprovalConfigAutoapproval objApprovalConfigVersion = getApprovalConfigVersionByID(napprovalConfigVersionCode,
				userInfo);
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> deletedObjects = new ArrayList<>();
		if (objApprovalConfigVersion != null) {
			if (objApprovalConfigVersion.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT
					.gettransactionstatus()) {
				String str = "update approvalconfigversion set dmodifieddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
						+ " where napproveconfversioncode = " + napprovalConfigVersionCode + ";";
				str += "update approvalconfigautoapproval set dmodifieddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
						+ " where napprovalconfigversioncode = " + napprovalConfigVersionCode + " and nsitecode = "
						+ userInfo.getNmastersitecode() + ";";
				str += "update approvalconfigrole set dmodifieddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
						+ " where napproveconfversioncode = " + napprovalConfigVersionCode + " and nsitecode = "
						+ userInfo.getNmastersitecode() + ";";
				str += "update approvalrolefilterdetail set dmodifieddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
						+ " where napprovalconfigrolecode = any("
						+ " select napprovalconfigrolecode from approvalconfigrole where napproveconfversioncode = "
						+ napprovalConfigVersionCode + "	)" + " and nsitecode = " + userInfo.getNmastersitecode()
						+ ";";

				str += "update approvalrolevalidationdetail set dmodifieddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
						+ " where napprovalconfigrolecode = any("
						+ " select napprovalconfigrolecode from approvalconfigrole where napproveconfversioncode = "
						+ napprovalConfigVersionCode + "	)" + " and nsitecode = " + userInfo.getNmastersitecode()
						+ ";";

				str += "update approvalroleactiondetail set dmodifieddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
						+ " where napprovalconfigrolecode = any("
						+ " select napprovalconfigrolecode from approvalconfigrole where napproveconfversioncode = "
						+ napprovalConfigVersionCode + "	)" + " and nsitecode = " + userInfo.getNmastersitecode()
						+ ";";

				str += "update approvalroledecisiondetail set dmodifieddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
						+ " where napprovalconfigrolecode = any("
						+ " select napprovalconfigrolecode from approvalconfigrole where napproveconfversioncode = "
						+ napprovalConfigVersionCode + "	)" + " and nsitecode = " + userInfo.getNmastersitecode()
						+ ";";

				Map<String, Object> getMap = new HashMap<String, Object>();
				getMap.put("napprovalconfigcode", objMap.get("napprovalconfigcode"));
				getMap.put("napprovalsubtypecode", objMap.get("napprovalsubtypecode"));
				getMap.put("ntreeversiontempcode", objMap.get("ntreeversiontempcode"));
				multilingualIDList.add("IDS_DELETEAPPROVALCONFIG");
				deletedObjects.add(Arrays.asList(objApprovalConfigVersion));
				String getQuery = " select napprovalconfigrolecode, napprovalconfigcode, ntreeversiontempcode, nuserrolecode, nchecklistversioncode, npartialapprovalneed, nsectionwiseapprovalneed, nrecomretestneed, nrecomrecalcneed, nretestneed, nrecalcneed, nlevelno, napprovalstatuscode, napproveconfversioncode, nautoapproval, nautoapprovalstatuscode, nautodescisionstatuscode, ncorrectionneed,"
						+ " nesignneed, ntransactionstatus, dmodifieddate, nsitecode, nstatus from approvalconfigrole where nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and napproveconfversioncode =  " + napprovalConfigVersionCode + " and nsitecode = "
						+ userInfo.getNmastersitecode() + ";";

				List<ApprovalConfigRole> lstApprovalConfigRole = jdbcTemplate.query(getQuery, new ApprovalConfigRole());

				getQuery = "select afd.napprovalfiltercode, afd.napprovalconfigrolecode, afd.napprovalconfigcode, afd.nuserrolecode, afd.nlevelno, afd.ndefaultstatus, afd.ntransactionstatus, afd.dmodifieddate, afd.nsitecode, afd.nstatus"
						+ " from approvalrolefilterdetail afd,approvalconfigrole acr "
						+ " where acr.napprovalconfigrolecode = afd.napprovalconfigrolecode" + " and acr.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and afd.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and acr.napproveconfversioncode = " + napprovalConfigVersionCode + " and afd.nsitecode = "
						+ userInfo.getNmastersitecode() + " and acr.nsitecode = " + userInfo.getNmastersitecode() + ";";
				List<ApprovalRoleFilterDetail> lstApprovalRoleFilterDetail = jdbcTemplate.query(getQuery,
						new ApprovalRoleFilterDetail());

				getQuery = "select afd.napprovalvalidationcode, afd.napprovalconfigrolecode, afd.napprovalconfigcode, afd.nuserrolecode, afd.nlevelno, afd.ntransactionstatus, afd.dmodifieddate, afd.nsitecode, afd.nstatus"
						+ " from approvalrolevalidationdetail afd,approvalconfigrole acr "
						+ " where acr.napprovalconfigrolecode = afd.napprovalconfigrolecode" + " and acr.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and afd.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and acr.napproveconfversioncode = " + napprovalConfigVersionCode + " and afd.nsitecode = "
						+ userInfo.getNmastersitecode() + " and acr.nsitecode = " + userInfo.getNmastersitecode() + ";";
				List<ApprovalRoleValidationDetail> lstApprovalRoleValidationDetail = jdbcTemplate.query(getQuery,
						new ApprovalRoleValidationDetail());

				getQuery = "select afd.napprovaldecisioncode, afd.napprovalconfigrolecode, afd.napprovalconfigcode, afd.nuserrolecode, afd.nlevelno, afd.ndefaultstatus, afd.ntransactionstatus, afd.dmodifieddate, afd.nsitecode, afd.nstatus"
						+ " from approvalroledecisiondetail afd,approvalconfigrole acr "
						+ " where acr.napprovalconfigrolecode = afd.napprovalconfigrolecode" + " and acr.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and afd.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and acr.napproveconfversioncode = " + napprovalConfigVersionCode + " and afd.nsitecode = "
						+ userInfo.getNmastersitecode() + " and acr.nsitecode = " + userInfo.getNmastersitecode() + ";";
				List<ApprovalRoleDecisionDetail> lstApprovalRoleDecisionDetail = jdbcTemplate.query(getQuery,
						new ApprovalRoleDecisionDetail());
				List<Object> deletedRoleDetails = new ArrayList<>();

				deletedRoleDetails.add(lstApprovalConfigRole);
				deletedRoleDetails.add(lstApprovalRoleFilterDetail);
				deletedRoleDetails.add(lstApprovalRoleValidationDetail);
				deletedRoleDetails.add(lstApprovalRoleDecisionDetail);

				jdbcTemplate.execute(str);
				multilingualIDList.add("IDS_DELETEAPPROVALCONFIGROLE");
				multilingualIDList.add("IDS_DELETEAPPROVALROLEFILTERDETAIL");
				multilingualIDList.add("IDS_DELETEAPPROVALROLEVALIDATIONDETAIL");
				multilingualIDList.add("IDS_DELETEAPPROVALROLEDECISIONDETAIL");
				deletedObjects.addAll(deletedRoleDetails);
				auditUtilityFunction.fnInsertListAuditAction(deletedObjects, 1, null, multilingualIDList, userInfo);
				return getApprovalConfigVersion(getMap, userInfo);

			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.SELECTDRAFTVERSION.getreturnstatus(), userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	private ApprovalConfigVersion getApprovalConfigVersionByTransStatus(int napprovalConfigVersionCode)
			throws Exception {
		String str = "select napproveconfversioncode from  approvalconfigversion where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntransactionstatus = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and napproveconfversioncode = "
				+ napprovalConfigVersionCode;
		return (ApprovalConfigVersion) jdbcUtilityFunction.queryForObject(str, ApprovalConfigVersion.class,
				jdbcTemplate);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> approveApprovalConfigVersion(Map<String, Object> objMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> approveConfigVersion = (Map<String, Object>) objMap.get("approvalconfigversion");
		ObjectMapper objMapper = new ObjectMapper();
		int napprovalConfigCode = (int) approveConfigVersion.get("napprovalconfigcode");
		int napprovalConfigVersionCode = (int) approveConfigVersion.get("napprovalconfigversioncode");
		int ntreeversiontempcode = -1;
		if (approveConfigVersion.containsKey("ntreeversiontempcode")) {
			ntreeversiontempcode = (int) approveConfigVersion.get("ntreeversiontempcode");
		}
		final String validateTemplate = "select ntreeversiontempcode,ntransactionstatus from treeversiontemplate"
				+ " where nstatus =  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and napprovalconfigcode = " + napprovalConfigCode + " and ntreeversiontempcode =  "
				+ ntreeversiontempcode;
		TreeVersionTemplate userRoleTemplate = (TreeVersionTemplate) jdbcUtilityFunction
				.queryForObject(validateTemplate, TreeVersionTemplate.class, jdbcTemplate);
		if (userRoleTemplate == null) {
			return new ResponseEntity<Object>(commonFunction
					.getMultilingualMessage("IDS_APPROVEDTEMPLATEVERSIONNOTAVAILABLE", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);

		} else if (userRoleTemplate.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED
				.gettransactionstatus()) {
			return new ResponseEntity<Object>(commonFunction
					.getMultilingualMessage("IDS_SELECTAPPROVEDUSERROLETEMPLATE", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		String str = " select napproveconfversioncode, napprovalconfigcode, napproveconfversionno, ntransactionstatus, ntreeversiontempcode, ndesigntemplatemappingcode,"
				+ " sapproveconfversiondesc, sviewname, dmodifieddate, nsitecode, nstatus"
				+ " from approvalconfigversion where napproveconfversioncode = " + napprovalConfigVersionCode
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		List<ApprovalConfigVersion> lstVersion = jdbcTemplate.query(str,
				new ApprovalConfigVersion());
		str = " select napprovalconfigrolecode, napprovalconfigcode, ntreeversiontempcode, nuserrolecode, nchecklistversioncode, npartialapprovalneed,"
				+ " nsectionwiseapprovalneed, nrecomretestneed, nrecomrecalcneed, nretestneed, nrecalcneed, nlevelno, napprovalstatuscode, napproveconfversioncode,"
				+ " nautoapproval, nautoapprovalstatuscode, nautodescisionstatuscode, ncorrectionneed, nesignneed, ntransactionstatus, dmodifieddate, nsitecode, nstatus"
				+ " from approvalconfigrole  where" + " nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and napproveconfversioncode = "
				+ napprovalConfigVersionCode + " and nsitecode = " + userInfo.getNmastersitecode();
		List<ApprovalRoleActionDetail> lstapprove = jdbcTemplate.query(str,
				new ApprovalRoleActionDetail());
		if (!lstVersion.isEmpty()) {
			ApprovalConfigVersion objApprovalConfigVersion = lstVersion.get(0);
			List<String> multilingualIDList = new ArrayList<>();
			List<Object> approvedList = new ArrayList<>();
			List<Object> draftList = new ArrayList<>();
			int ntransactionstatus = objApprovalConfigVersion.getNtransactionstatus();
			StringBuffer sb = new StringBuffer();
			if (ntransactionstatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
				boolean check = false;
				String sviewName = "";
				int ndesigntemplatemappingcode = -1;
				if (approveConfigVersion.containsKey("sviewname")) {
					sviewName = (String) approveConfigVersion.get("sviewname");
					ndesigntemplatemappingcode = (int) approveConfigVersion.get("ndesigntemplatemappingcode");
				}
				// ALPD-5343 added by Dhanushya RI,Version number is skipped when save the
				// already existing dynamic view so that first throw the alert and after that
				// only generate version number
				if ((int) approveConfigVersion
						.get("napprovalsubtypecode") == Enumeration.ApprovalSubType.TESTRESULTAPPROVAL.getnsubtype()
						|| (int) approveConfigVersion.get(
								"napprovalsubtypecode") == Enumeration.ApprovalSubType.PROTOCOLAPPROVAL.getnsubtype()) {
					final String registrationSubType = "select * from information_schema.tables  where lower(table_name) = lower('"
							+ sviewName.replaceAll(" ", "") + "')";
					List<?> lstView = jdbcTemplate.queryForList(registrationSubType);

					if (lstView.isEmpty()) {
						check = true;
					} else {
						return new ResponseEntity<Object>(commonFunction.getMultilingualMessage(
								Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
					}
				}
				// ALPD-5343 added by Dhanushya RI,Version number is skipped when save the
				// already existing dynamic view so that first throw the alert and after that
				// only generate version number
				Object objVersion = projectDAOSupport.fnGetVersion(
						Enumeration.QualisForms.APPROVALCONFIGURATION.getqualisforms(), napprovalConfigCode, "",
						SeqNoApprovalConfigVersion.class, userInfo.getNmastersitecode(), userInfo);
				int versionNo = Enumeration.Sequenceintialvalue.ONE.getSequence();
				String versionDescription = "";
				if (objVersion != null) {
					versionNo = Integer.parseInt(BeanUtils.getProperty(objVersion, "versionno"));
					versionDescription = BeanUtils.getProperty(objVersion, "versiondes");
				}
				objApprovalConfigVersion.setSapproveconfversiondesc(versionDescription);

				if (approveConfigVersion.containsKey("napprovalsubtypecode")) {

				}

				sb.append("update approvalconfigversion set dmodifieddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', ntransactionstatus = "
						+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + " where napprovalconfigcode = "
						+ napprovalConfigCode + " and ntransactionstatus = "
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and nsitecode = "
						+ userInfo.getNmastersitecode() + ";");

				sb.append("update approvalconfigversion set dmodifieddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', napproveconfversionno = " + versionNo
						+ ",sapproveconfversiondesc = N'" + stringUtilityFunction.replaceQuote(versionDescription)
						+ "',sviewname = '" + stringUtilityFunction.replaceQuote(sviewName) + "',"
						+ "ndesigntemplatemappingcode = " + ndesigntemplatemappingcode + "," + "ntransactionstatus = "
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
						+ " where napproveconfversioncode = " + napprovalConfigVersionCode + " and nsitecode = "
						+ userInfo.getNmastersitecode() + ";");

				sb.append("update approvalconfigrole set dmodifieddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', ntransactionstatus = "
						+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + " where napprovalconfigcode = "
						+ napprovalConfigCode + " and ntransactionstatus = "
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and nsitecode = "
						+ userInfo.getNmastersitecode() + ";");

				sb.append("update approvalconfigrole set dmodifieddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', ntransactionstatus = "
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
						+ " where napproveconfversioncode = " + napprovalConfigVersionCode + " and nsitecode = "
						+ userInfo.getNmastersitecode());

				jdbcTemplate.execute(sb.toString());

				str = " select napprovalconfigrolecode, napprovalconfigcode, ntreeversiontempcode, nuserrolecode, nchecklistversioncode, npartialapprovalneed,"
						+ " nsectionwiseapprovalneed, nrecomretestneed, nrecomrecalcneed, nretestneed, nrecalcneed, nlevelno, napprovalstatuscode,"
						+ " napproveconfversioncode, nautoapproval, nautoapprovalstatuscode, nautodescisionstatuscode, ncorrectionneed, nesignneed, ntransactionstatus, dmodifieddate, nsitecode, nstatus"
						+ " from approvalconfigrole  where nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and napproveconfversioncode = " + napprovalConfigVersionCode + " and nsitecode = "
						+ userInfo.getNmastersitecode();
				List<ApprovalRoleActionDetail> newlstapprove = jdbcTemplate.query(str,
						new ApprovalRoleActionDetail());

				str = " select napproveconfversioncode, napprovalconfigcode, napproveconfversionno, ntransactionstatus, ntreeversiontempcode,"
						+ " ndesigntemplatemappingcode, sapproveconfversiondesc, sviewname, dmodifieddate, nsitecode, nstatus"
						+ " from approvalconfigversion where napproveconfversioncode = " + napprovalConfigVersionCode
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				List<ApprovalConfigVersion> newVersionList = jdbcTemplate.query(str,
						new ApprovalConfigVersion());

				int nregsubtypecode = (int) approveConfigVersion.get("nregsubtypecode");

				if (nregsubtypecode > 0) {
					if (check) {
						String regsubtype = "select cast(rg.jsondata->>'nneedsubsample' as boolean) nneedsubsample,"
								+ "dm.ndesigntemplatemappingcode, dm.nsampletypecode, dm.nformcode, dm.nregtypecode, dm.nregsubtypecode,"
								+ " dm.nregsubtypeversioncode, dm.nformwisetypecode, dm.nreactregtemplatecode, dm.nsubsampletemplatecode,"
								+ " dm.ntransactionstatus, dm.nversionno, dm.dmodifieddate, dm.nsitecode, dm.nstatus,"
								+ " rt.sregtemplatename,rt.stemplatetypesname from regsubtypeconfigversion rg,"
								+ "designtemplatemapping dm ,reactregistrationtemplate rt "
								+ " where  dm.nregsubtypecode = " + nregsubtypecode
								+ " and dm.ndesigntemplatemappingcode = " + ndesigntemplatemappingcode + ""
								+ " and rg.nregsubtypeversioncode = dm.nregsubtypeversioncode  and rt.nreactregtemplatecode = dm.nreactregtemplatecode"
								+ " and rg.nsitecode = " + userInfo.getNmastersitecode();
						DesignTemplateMapping regSubType = (DesignTemplateMapping) jdbcUtilityFunction
								.queryForObject(regsubtype, DesignTemplateMapping.class, jdbcTemplate);

						String subsampleTemplatename = null;
						String sampleName = regSubType.getStemplatetypesname().replaceAll(" ", "")
								.replaceAll("[^a-zA-Z0-9]", "");
						String tablename = "'registration'";
						if (regSubType.isNneedsubsample()) {

							String subSampleTemplatequery = "select r.nreactregtemplatecode, r.nsampletypecode, r.nsubsampletypecode, r.ndefaulttemplatecode,"
									+ " r.ntransactionstatus, r.sregtemplatename, r.jsondata, r.dmodifieddate, r.nsitecode, r.nstatus, r.stemplatetypesname "
									+ " from reactregistrationtemplate r,designtemplatemapping d"
									+ " where d.ndesigntemplatemappingcode = "
									+ regSubType.getNdesigntemplatemappingcode()
									+ " and r.nreactregtemplatecode = d.nsubsampletemplatecode";
							ReactRegistrationTemplate objReactRegistrationTemplate = (ReactRegistrationTemplate) jdbcUtilityFunction
									.queryForObject(subSampleTemplatequery, ReactRegistrationTemplate.class,
											jdbcTemplate);

							subsampleTemplatename = objReactRegistrationTemplate.getStemplatetypesname()
									.replaceAll(" ", "").replaceAll("[^a-zA-Z0-9]", "");

							tablename = tablename.concat(",'registrationsample'");
						}
						sviewName = sviewName.replaceAll(" ", "");
						String queryBuildername = sviewName + "QueryBuilder";
						String createView = " create OR REPLACE view public." + queryBuildername + " as select d.*,r.*";

						int subsample = 4;
						if (regSubType.isNneedsubsample()) {
							createView = createView + " ,rs.* ";
							subsample = 3;
						}

						int seqNoTable = -1;
						int seqNoTableQueryTables = -1;
						int seqNoTableQueryTablesDetails = -1;

						final String getSeqNoForm = "select stablename,nsequenceno from seqnocredentialmanagement "
								+ " where stablename in('querybuilderviews','querybuildertables','querytabledetails')"
								+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
						List<SeqNoCredentialManagement> seqList = jdbcTemplate.query(getSeqNoForm,
								new SeqNoCredentialManagement());

						Map<String, Integer> seqMap = seqList.stream()
								.collect(Collectors.toMap(SeqNoCredentialManagement::getStablename,
										seqNoConfigurationMaster -> seqNoConfigurationMaster.getNsequenceno()));

						seqNoTableQueryTablesDetails = (seqMap.get("querytabledetails") < Enumeration.DynamicInitialValue.QUERYTABLEDETAILS
								.getInitialValue() ? Enumeration.DynamicInitialValue.QUERYTABLEDETAILS.getInitialValue()
										: seqMap.get("querytabledetails"))
								+ 1;

						seqNoTable = (seqMap.get("querybuilderviews") < Enumeration.DynamicInitialValue.QUERYBUILDERVIEWS
								.getInitialValue() ? Enumeration.DynamicInitialValue.QUERYBUILDERVIEWS.getInitialValue()
										: seqMap.get("querybuilderviews"))
								+ 1;

						seqNoTableQueryTables = (seqMap.get("querybuildertables") < Enumeration.DynamicInitialValue.QUERYBUILDERTABLES
								.getInitialValue() ? Enumeration.DynamicInitialValue.QUERYBUILDERTABLES.getInitialValue()
										: seqMap.get("querybuildertables"))
								+ 1;

						final JSONObject editObject = new JSONObject();
						final JSONObject editObjectQB = new JSONObject();
						JSONObject editObject1 = new JSONObject();
						editObject1.put(userInfo.getSlanguagetypecode(), sviewName.toLowerCase());
						editObject.put("displayname", editObject1);
						editObjectQB.put("tablename", editObject1);

						String queryList = "select ntestlistfieldscode, jsondata, nissubsampletest, dmodifieddate, nsitecode, nstatus from testlistfields where nissubsampletest = "
								+ subsample + " and nsitecode = " + userInfo.getNmastersitecode();
						List<Map<String, Object>> objMap1 = jdbcTemplate.queryForList(queryList);

						Map<String, Object> obj6 = null;
						List<Map<String, Object>> obj2 = (List<Map<String, Object>>) objMap
								.get("sampleQuerybuilderViewCondition");
						List<Map<String, Object>> obj3 = (List<Map<String, Object>>) objMap
								.get("sampleQuerybuilderViewSelect");

						queryList = "select jsqlquerycolumns from querybuildertablecolumns where stablename in ('registration') and nformcode = 43 and nsitecode = "
								+ userInfo.getNmastersitecode();
						List<Map<String, Object>> objMapRegSqlCOl = jdbcTemplate.queryForList(queryList);
						List<Map<String, Object>> jsample = new LinkedList<Map<String, Object>>();
						List<?> jasample = null;
						List<Map<String, Object>> objJsqlQuerycolumns = (List<Map<String, Object>>) objMap
								.get("jsqlquerycolumns");

						if (!objMapRegSqlCOl.isEmpty()) {

							Map<String, Object> ja2 = objMapRegSqlCOl.get(0);
							List<?> ja3 = new JSONArray(((PGobject) ja2.get("jsqlquerycolumns")).getValue()).toList();
							jsample.addAll((Collection<? extends Map<String, Object>>) ja3);

							jsample.addAll(objJsqlQuerycolumns);

							jasample = jsample.stream().distinct().collect(Collectors.toList());
						}
						List<?> jasubsample = null;

						if (regSubType.isNneedsubsample()) {

							queryList = "select jsqlquerycolumns from querybuildertablecolumns where stablename in ('registrationsample') and nformcode = 43 and nsitecode = "
									+ userInfo.getNmastersitecode();
							List<Map<String, Object>> objMapRegSqlCOl2 = jdbcTemplate.queryForList(queryList);
							List<Map<String, Object>> jsubsample = new LinkedList<Map<String, Object>>();
							List<Map<String, Object>> objJsqlQuerycolumns1 = (List<Map<String, Object>>) objMap
									.get("jsqlquerycolumnssub");
							if (!objMapRegSqlCOl2.isEmpty()) {

								Map<String, Object> jsub = objMapRegSqlCOl2.get(0);
								List<?> jsub1 = new JSONArray(((PGobject) jsub.get("jsqlquerycolumns")).getValue())
										.toList();
								jsubsample.addAll((Collection<? extends Map<String, Object>>) jsub1);

								jsubsample.addAll(objJsqlQuerycolumns1);

								jasubsample = jsubsample.stream().distinct().collect(Collectors.toList());
							}
						}
						JSONArray jsqlColumnsQB1 = null;
						Map<String, Object> obj5 = new HashMap<>();
						if (!queryList.isEmpty()) {
							obj6 = objMap1.get(0);
							Map<String, Object> obj = new JSONObject(((PGobject) obj6.get("jsondata")).getValue())
									.toMap();
							obj2.addAll((List<Map<String, Object>>) obj.get("conditionfields"));
							obj3.addAll((List<Map<String, Object>>) obj.get("selectfields"));
							List<Map<String, Object>> jsqlColumnsQB2 = (List<Map<String, Object>>) obj
									.get("querybuildertablecolumnsfordynamicview");
							jsqlColumnsQB1 = new JSONArray(jsqlColumnsQB2);
						}

						if (!newlstapprove.isEmpty()) {
							for (int j = 0; j < newlstapprove.size(); j++) {

								String transactionquery = "select jsondata->'salertdisplaystatus'->>'en-US'  from transactionstatus where ntranscode = "
										+ newlstapprove.get(j).getNapprovalstatuscode();
								String transactionstatus = jdbcTemplate.queryForObject(transactionquery, String.class);
								// ALPD-3531
								String slevel = "(Level_" + newlstapprove.get(j).getNlevelno() + ")";

								/////////////////////// Start for user name code creation////////////
								final JSONObject objUsersdisplay = new JSONObject();
								JSONObject objUsersdisplay1 = new JSONObject();
								objUsersdisplay1.put(userInfo.getSlanguagetypecode(),
										transactionstatus + "by" + slevel);

								objUsersdisplay.put("displayname", objUsersdisplay1);

								Map<String, Object> objUserscon = new HashMap<String, Object>();
								objUserscon.put("columnname", transactionstatus + "by" + slevel);
								objUserscon.put("columntype", 1);
								objUserscon.put("displayname", objUsersdisplay.get("displayname"));
								objUserscon.put("viewvaluemember", transactionstatus + "by" + slevel);
								objUserscon.put("valuemember", "sfirstname");
								objUserscon.put("displaymember", "sfirstname");
								objUserscon.put("columntypedesc", "textinput");
								objUserscon.put("needmasterdata", true);
								objUserscon.put("mastertablename", "users");

								obj2.add(objUserscon);

								Map<String, Object> objUsersselect = new HashMap<String, Object>();
								objUsersselect.put("columnname", transactionstatus + "by" + slevel);
								objUsersselect.put("languagecode", false);
								objUsersselect.put("displayname", objUsersdisplay.get("displayname"));

								obj3.add(objUsersselect);

								/////////////////////// End for user name code creation////////////
								/////////////////////// Start for user role code creation////////////

								final JSONObject objUserroledisplay = new JSONObject();
								JSONObject objUserroledisplay1 = new JSONObject();
								objUserroledisplay1.put(userInfo.getSlanguagetypecode(),
										transactionstatus + "userrole" + slevel);

								objUserroledisplay.put("displayname", objUserroledisplay1);

								Map<String, Object> objUserrolecon = new HashMap<String, Object>();
								objUserrolecon.put("columnname", transactionstatus + "userrole" + slevel);
								objUserrolecon.put("columntype", 1);
								objUserrolecon.put("displayname", objUserroledisplay.get("displayname"));
								objUserrolecon.put("viewvaluemember", transactionstatus + "userrole" + slevel);
								objUserrolecon.put("valuemember", "suserrolename");
								objUserrolecon.put("columntypedesc", "textinput");
								objUserrolecon.put("needmasterdata", true);
								objUserrolecon.put("mastertablename", "userrole");
								objUserrolecon.put("displaymember", "suserrolename");

								obj2.add(objUserrolecon);

								Map<String, Object> objUserroleselect = new HashMap<String, Object>();
								objUserroleselect.put("columnname", transactionstatus + "userrole" + slevel);
								objUserroleselect.put("languagecode", false);
								objUserroleselect.put("displayname", objUserroledisplay.get("displayname"));

								obj3.add(objUserroleselect);

								/////////////////////// End for user role code creation////////////

								/////////////////////// Start for user designation code creation////////////

								final JSONObject objUserdesignationdisplay = new JSONObject();
								JSONObject objUserdesignationdisplay1 = new JSONObject();
								objUserdesignationdisplay1.put(userInfo.getSlanguagetypecode(),
										transactionstatus + "userdesignation" + slevel);

								objUserdesignationdisplay.put("displayname", objUserdesignationdisplay1);

								Map<String, Object> objUserdesignation = new HashMap<String, Object>();
								objUserdesignation.put("columnname", transactionstatus + "userrole" + slevel);
								objUserdesignation.put("columntype", 1);
								objUserdesignation.put("displayname", objUserdesignationdisplay.get("displayname"));
								objUserdesignation.put("viewvaluemember",
										transactionstatus + "userdesignation" + slevel);
								objUserdesignation.put("valuemember", "sdesignationname");
								objUserdesignation.put("columntypedesc", "textinput");
								objUserdesignation.put("needmasterdata", true);
								objUserdesignation.put("mastertablename", "designation");
								objUserdesignation.put("displaymember", "sdesignationname");

								obj2.add(objUserdesignation);

								Map<String, Object> objUserdesignationselect = new HashMap<String, Object>();
								objUserdesignationselect.put("columnname",
										transactionstatus + "userdesignation" + slevel);
								objUserdesignationselect.put("languagecode", false);
								objUserdesignationselect.put("displayname",
										objUserdesignationdisplay.get("displayname"));

								obj3.add(objUserdesignationselect);

								/////////////////////// End for user designation code creation////////////

								////////// Start for transaction date code creation------------

								final JSONObject objdatedisplay = new JSONObject();

								JSONObject objdatedisplay1 = new JSONObject();
								objdatedisplay1.put(userInfo.getSlanguagetypecode(),
										transactionstatus + "date" + slevel);

								objdatedisplay.put("displayname", objdatedisplay1);

								Map<String, Object> objdatecon = new HashMap<String, Object>();
								objdatecon.put("columnname", transactionstatus + "date" + slevel);
								objdatecon.put("columntype", 2);
								objdatecon.put("displayname", objdatedisplay.get("displayname"));
								objdatecon.put("columntypedesc", "datetime");
								objdatecon.put("needmasterdata", false);

								obj2.add(objdatecon);

								Map<String, Object> objdateselect = new HashMap<String, Object>();
								objdateselect.put("columnname", transactionstatus + "date" + slevel);
								objdateselect.put("languagecode", false);
								objdateselect.put("displayname", objdatedisplay.get("displayname"));

								obj3.add(objdateselect);

								/////////////// End for transaction date code creation/////////////

								createView = createView
										+ " ,(SELECT max(u.sfirstname::text || ' '::text || u.slastname::text)  "
										+ "  FROM registrationtesthistory rh, "
										+ "  users u WHERE rh.ntransactionstatus IN ( SELECT acr.napprovalstatuscode "
										+ "      FROM approvalconfig ac,approvalconfigrole acr "
										+ "      WHERE ac.napprovalconfigcode = acr.napprovalconfigcode AND acr.nlevelno  = "
										+ newlstapprove.get(j).getNlevelno() + "   AND acr.ntransactionstatus  = "
										+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
										+ " AND ac.nregsubtypecode = d.nregsubtypecode AND ac.nsitecode = "
										+ userInfo.getNmastersitecode() + " AND acr.nsitecode = "
										+ userInfo.getNmastersitecode() + ") "
										+ "   AND rh.nusercode = u.nusercode and d.ntransactiontestcode = rh.ntransactiontestcode"
										+ " group by rh.ntransactiontestcode order by rh.ntransactiontestcode desc limit 1)  as \""
										+ transactionstatus + "by" + slevel + "\"";

								createView = createView + " ,(SELECT  max(ur.suserrolename::text)    "
										+ "  FROM registrationtesthistory rh, "
										+ "    userrole ur WHERE (rh.ntransactionstatus IN ( SELECT acr.napprovalstatuscode "
										+ "      FROM approvalconfig ac,approvalconfigrole acr "
										+ "      WHERE ac.napprovalconfigcode = acr.napprovalconfigcode AND acr.nlevelno = "
										+ newlstapprove.get(j).getNlevelno() + "   AND acr.ntransactionstatus = "
										+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
										+ " AND ac.nregsubtypecode = d.nregsubtypecode AND ac.nsitecode = "
										+ userInfo.getNmastersitecode() + " AND acr.nsitecode = "
										+ userInfo.getNmastersitecode() + ")) "
										+ "   AND rh.nuserrolecode = ur.nuserrolecode and d.npreregno = rh.npreregno group by rh.npreregno)  as \""
										+ transactionstatus + "userrole" + slevel + "\"";

								createView = createView + " ,(SELECT  max(desig.sdesignationname::text)    "
										+ "  FROM registrationtesthistory rh, users u,"
										+ "    designation desig WHERE (rh.ntransactionstatus IN ( SELECT acr.napprovalstatuscode "
										+ "      FROM approvalconfig ac,approvalconfigrole acr "
										+ "      WHERE ac.napprovalconfigcode = acr.napprovalconfigcode AND acr.nlevelno  = "
										+ newlstapprove.get(j).getNlevelno() + "   AND acr.ntransactionstatus = "
										+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
										+ " AND ac.nregsubtypecode = d.nregsubtypecode AND ac.nsitecode = "
										+ userInfo.getNmastersitecode() + " AND acr.nsitecode = "
										+ userInfo.getNmastersitecode() + ")) "
										+ "   AND rh.nusercode = u.nusercode and u.ndesignationcode = desig.ndesignationcode and d.npreregno = rh.npreregno group by rh.npreregno)  as \""
										+ transactionstatus + "userdesignation" + slevel + "\"";

								createView = createView + " ,(SELECT max(rh.dtransactiondate)    "
										+ "  FROM registrationtesthistory rh WHERE rh.ntransactionstatus IN ( SELECT acr.napprovalstatuscode "
										+ "      FROM approvalconfig ac,approvalconfigrole acr "
										+ "      WHERE ac.napprovalconfigcode = acr.napprovalconfigcode AND acr.nlevelno  = "
										+ newlstapprove.get(j).getNlevelno() + "   AND acr.ntransactionstatus = "
										+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
										+ " AND ac.nregsubtypecode = d.nregsubtypecode AND ac.nsitecode = "
										+ userInfo.getNmastersitecode() + " AND acr.nsitecode = "
										+ userInfo.getNmastersitecode() + ") "
										+ "    and d.ntransactiontestcode = rh.ntransactiontestcode group by rh.ntransactiontestcode"
										+ "  order by rh.ntransactiontestcode desc limit 1)  as \""
										+ transactionstatus + "date" + slevel + "\"";

								// JIRA-ALPD-5017->SATHISH-> CHANGE ssignimagename to ssignimageftp for report
								// signature
								createView = createView + " ,(SELECT max(u.ssignimgftp::text)  "
										+ "  FROM registrationtesthistory rh, "
										+ "  userfile u WHERE (rh.ntransactionstatus IN ( SELECT acr.napprovalstatuscode "
										+ "      FROM approvalconfig ac,approvalconfigrole acr "
										+ "      WHERE ac.napprovalconfigcode = acr.napprovalconfigcode AND acr.nlevelno  = "
										+ newlstapprove.get(j).getNlevelno() + "   AND acr.ntransactionstatus = "
										+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
										+ " AND ac.nregsubtypecode = d.nregsubtypecode AND ac.nsitecode = "
										+ userInfo.getNmastersitecode() + " AND acr.nsitecode = "
										+ userInfo.getNmastersitecode() + ")) "
										+ "   AND rh.nusercode = u.nusercode and d.npreregno = rh.npreregno group by rh.npreregno)  as \""
										+ transactionstatus + "usersign" + slevel + "\"";

							}

							////////// Start for pre-registered date code creation------------

							Map<String, Object> preRegDateMap = new HashMap<String, Object>();
							preRegDateMap.put("columnname", "preregistereddate");
							preRegDateMap.put("columntype", 2);
							preRegDateMap.put("displayname", "preregistereddate");
							preRegDateMap.put("columntypedesc", "datetime");
							preRegDateMap.put("needmasterdata", false);

							obj2.add(preRegDateMap);

							Map<String, Object> preRegDate = new HashMap<String, Object>();
							preRegDate.put("columnname", "preregistereddate");
							preRegDate.put("languagecode", false);
							preRegDate.put("displayname", "preregistereddate");

							obj3.add(preRegDate);

							/////////////// End for pre-registered date code creation/////////////

							////////// Start for registered date transaction date code creation------------

							Map<String, Object> regDateMap = new HashMap<String, Object>();
							preRegDateMap.put("columnname", "registereddate");
							preRegDateMap.put("columntype", 2);
							preRegDateMap.put("displayname", "registereddate");
							preRegDateMap.put("columntypedesc", "datetime");
							preRegDateMap.put("needmasterdata", false);

							obj2.add(regDateMap);

							Map<String, Object> regDate = new HashMap<String, Object>();
							preRegDate.put("columnname", "registereddate");
							preRegDate.put("languagecode", false);
							preRegDate.put("displayname", "registereddate");

							obj3.add(regDate);

							/////////////// End for registered date code creation/////////////

							////////// Start for completed date code creation------------

							Map<String, Object> completedDateMap = new HashMap<String, Object>();
							completedDateMap.put("columnname", "completeddate");
							completedDateMap.put("columntype", 2);
							completedDateMap.put("displayname", "completeddate");
							completedDateMap.put("columntypedesc", "datetime");
							completedDateMap.put("needmasterdata", false);

							obj2.add(completedDateMap);

							Map<String, Object> completedDate = new HashMap<String, Object>();
							completedDate.put("columnname", "completeddate");
							completedDate.put("languagecode", false);
							completedDate.put("displayname", "completeddate");

							obj3.add(completedDate);

							/////////////// End for completed date code creation/////////////

							/////////////////////// Start for completed user name code creation////////////

							Map<String, Object> objCompletedUser = new HashMap<String, Object>();
							objCompletedUser.put("columnname", "completedby");
							objCompletedUser.put("columntype", 1);
							objCompletedUser.put("displayname", "completedby");
							objCompletedUser.put("viewvaluemember", "completedby");
							objCompletedUser.put("valuemember", "sfirstname");
							objCompletedUser.put("displaymember", "sfirstname");
							objCompletedUser.put("columntypedesc", "textinput");
							objCompletedUser.put("needmasterdata", true);
							objCompletedUser.put("mastertablename", "users");

							obj2.add(objCompletedUser);

							Map<String, Object> completedUser = new HashMap<String, Object>();
							completedUser.put("columnname", "completedby");
							completedUser.put("languagecode", false);
							completedUser.put("displayname", "completedby");

							obj3.add(completedUser);

							/////////////////////// End for completed user name code creation////////////

							createView = createView
									+ ",(select max(dtransactiondate) from registrationhistory where ntransactionstatus = "
									+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus()
									+ " and d.npreregno = npreregno group by npreregno) as preregistereddate";

							createView = createView
									+ ",(select max(dtransactiondate) from registrationhistory where ntransactionstatus = "
									+ Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()
									+ " and d.npreregno = npreregno group by npreregno) as registereddate";

							createView = createView
									+ ", (select max(dtransactiondate) from registrationtesthistory where ntransactionstatus = "
									+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()
									+ " and d.ntransactiontestcode = ntransactiontestcode group by ntransactiontestcode "
									+ " order by ntransactiontestcode limit 1) as completeddate";

							createView = createView
									+ ", (select max(u.sfirstname::text || ' '::text || u.slastname::text)"
									+ " from registrationtesthistory r, users u where  r.ntransactionstatus = "
									+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() + " and"
									//									+ " d.npreregno = r.npreregno and d.ntransactionsamplecode = r.ntransactionsamplecode and "
									+ " d.ntransactiontestcode = r.ntransactiontestcode and "
									+ "  u.nusercode = r.nusercode and u.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and r.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " group by r.ntransactiontestcode order by r.ntransactiontestcode desc limit 1)"
									+ " as completedby";

						}

						createView = createView
								+ " FROM   view_registrationreport d,  jsonb_populate_record(NULL::type_" + sampleName
								+ ", d.jsonuidata) r ";
						if (regSubType.isNneedsubsample()) {

							createView = createView + "  ,jsonb_populate_record(NULL::type_" + subsampleTemplatename
									+ ", d.samplejsonuidata) rs ";
						}

						createView = createView + " where  d.ndesigntemplatemappingcode = "
								+ regSubType.getNdesigntemplatemappingcode() + " and d.napprovalversioncode = "
								+ napprovalConfigVersionCode + "";

						jdbcTemplate.execute(createView);

						String createView1 = " create OR REPLACE view public." + sviewName + " as select d.*,r.*";

						if (regSubType.isNneedsubsample()) {
							createView1 = createView1 + ", rs.*";
						}
						if (!newlstapprove.isEmpty()) {
							for (int j = 0; j < newlstapprove.size(); j++) {

								String transactionquery = "select jsondata->'salertdisplaystatus'->>'en-US'  from transactionstatus where ntranscode = "
										+ newlstapprove.get(j).getNapprovalstatuscode();
								String transactionstatus = jdbcTemplate.queryForObject(transactionquery, String.class);
								String slevel = "(Level_" + newlstapprove.get(j).getNlevelno() + ")";

								final JSONObject objUsersdisplay = new JSONObject();
								JSONObject objUsersdisplay1 = new JSONObject();
								objUsersdisplay1.put(userInfo.getSlanguagetypecode(),
										transactionstatus + "by" + slevel);

								objUsersdisplay.put("displayname", objUsersdisplay1);

								Map<String, Object> objUserscon = new HashMap<String, Object>();
								objUserscon.put("columnname", transactionstatus + "by" + slevel);
								objUserscon.put("columntype", 1);
								objUserscon.put("displayname", objUsersdisplay.get("displayname"));
								objUserscon.put("viewvaluemember", transactionstatus + "by" + slevel);
								objUserscon.put("valuemember", "sfirstname");
								objUserscon.put("displaymember", "sfirstname");
								objUserscon.put("columntypedesc", "textinput");
								objUserscon.put("needmasterdata", true);
								objUserscon.put("mastertablename", "users");

								obj2.add(objUserscon);

								Map<String, Object> objUsersselect = new HashMap<String, Object>();
								objUsersselect.put("columnname", transactionstatus + "by" + slevel);
								objUsersselect.put("languagecode", false);
								objUsersselect.put("displayname", objUsersdisplay.get("displayname"));

								obj3.add(objUsersselect);

								final JSONObject objUserroledisplay = new JSONObject();
								JSONObject objUserroledisplay1 = new JSONObject();
								objUserroledisplay1.put(userInfo.getSlanguagetypecode(),
										transactionstatus + "userrole" + slevel);

								objUserroledisplay.put("displayname", objUserroledisplay1);

								Map<String, Object> objUserrolecon = new HashMap<String, Object>();
								objUserrolecon.put("columnname", transactionstatus + "userrole" + slevel);
								objUserrolecon.put("columntype", 1);
								objUserrolecon.put("displayname", objUserroledisplay.get("displayname"));
								objUserrolecon.put("viewvaluemember", transactionstatus + "userrole" + slevel);
								objUserrolecon.put("valuemember", "suserrolename");
								objUserrolecon.put("columntypedesc", "textinput");
								objUserrolecon.put("needmasterdata", true);
								objUserrolecon.put("mastertablename", "userrole");
								objUserrolecon.put("displaymember", "suserrolename");

								obj2.add(objUserrolecon);

								Map<String, Object> objUserroleselect = new HashMap<String, Object>();
								objUserroleselect.put("columnname", transactionstatus + "userrole" + slevel);
								objUserroleselect.put("languagecode", false);
								objUserroleselect.put("displayname", objUserroledisplay.get("displayname"));

								obj3.add(objUserroleselect);

								/////////////////////// Start for user designation code creation////////////

								final JSONObject objUserdesignationdisplay = new JSONObject();
								JSONObject objUserdesignationdisplay1 = new JSONObject();
								objUserdesignationdisplay1.put(userInfo.getSlanguagetypecode(),
										transactionstatus + "userdesignation" + slevel);

								objUserdesignationdisplay.put("displayname", objUserdesignationdisplay1);

								Map<String, Object> objUserdesignation = new HashMap<String, Object>();
								objUserdesignation.put("columnname", transactionstatus + "userrole" + slevel);
								objUserdesignation.put("columntype", 1);
								objUserdesignation.put("displayname", objUserdesignationdisplay.get("displayname"));
								objUserdesignation.put("viewvaluemember",
										transactionstatus + "userdesignation" + slevel);
								objUserdesignation.put("valuemember", "sdesignationname");
								objUserdesignation.put("columntypedesc", "textinput");
								objUserdesignation.put("needmasterdata", true);
								objUserdesignation.put("mastertablename", "designation");
								objUserdesignation.put("displaymember", "sdesignationname");

								obj2.add(objUserdesignation);

								Map<String, Object> objUserdesignationselect = new HashMap<String, Object>();
								objUserdesignationselect.put("columnname",
										transactionstatus + "userdesignation" + slevel);
								objUserdesignationselect.put("languagecode", false);
								objUserdesignationselect.put("displayname",
										objUserdesignationdisplay.get("displayname"));

								obj3.add(objUserdesignationselect);

								/////////////////////// End for user designation code creation////////////

								final JSONObject objdatedisplay = new JSONObject();

								JSONObject objdatedisplay1 = new JSONObject();
								objdatedisplay1.put(userInfo.getSlanguagetypecode(),
										transactionstatus + "date" + slevel);

								objdatedisplay.put("displayname", objdatedisplay1);

								Map<String, Object> objdatecon = new HashMap<String, Object>();
								objdatecon.put("columnname", transactionstatus + "date" + slevel);
								objdatecon.put("columntype", 2);
								objdatecon.put("displayname", objdatedisplay.get("displayname"));
								objdatecon.put("columntypedesc", "datetime");
								objdatecon.put("needmasterdata", false);

								obj2.add(objdatecon);

								Map<String, Object> objdateselect = new HashMap<String, Object>();
								objdateselect.put("columnname", transactionstatus + "date" + slevel);
								objdateselect.put("languagecode", false);
								objdateselect.put("displayname", objdatedisplay.get("displayname"));

								obj3.add(objdateselect);

								createView1 = createView1
										+ " ,(SELECT max(u.sfirstname::text || ' '::text || u.slastname::text)  "
										+ "  FROM registrationtesthistory rh, "
										+ "  users u WHERE (rh.ntransactionstatus IN ( SELECT acr.napprovalstatuscode "
										+ "      FROM approvalconfig ac,approvalconfigrole acr "
										+ "      WHERE ac.napprovalconfigcode = acr.napprovalconfigcode AND acr.nlevelno = "
										+ newlstapprove.get(j).getNlevelno() + "   AND acr.ntransactionstatus  = "
										+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
										+ " AND ac.nregsubtypecode = d.nregsubtypecode AND ac.nsitecode = "
										+ userInfo.getNmastersitecode() + " AND acr.nsitecode = "
										+ userInfo.getNmastersitecode() + ")) "
										+ "   AND rh.nusercode = u.nusercode and d.ntransactiontestcode = rh.ntransactiontestcode"
										+ " group by rh.ntransactiontestcode order by rh.ntransactiontestcode desc limit 1)  as \""
										+ transactionstatus + "by" + slevel + "\"";

								createView1 = createView1 + " ,(SELECT  max(ur.suserrolename::text)    "
										+ "  FROM registrationtesthistory rh, "
										+ "    userrole ur WHERE (rh.ntransactionstatus IN ( SELECT acr.napprovalstatuscode "
										+ "      FROM approvalconfig ac,approvalconfigrole acr "
										+ "      WHERE ac.napprovalconfigcode = acr.napprovalconfigcode AND acr.nlevelno  = "
										+ newlstapprove.get(j).getNlevelno() + "   AND acr.ntransactionstatus = "
										+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
										+ " AND ac.nregsubtypecode = d.nregsubtypecode AND ac.nsitecode = "
										+ userInfo.getNmastersitecode() + " AND acr.nsitecode = "
										+ userInfo.getNmastersitecode() + ")) "
										+ "   AND rh.nuserrolecode = ur.nuserrolecode and d.npreregno = rh.npreregno group by rh.npreregno)  as \""
										+ transactionstatus + "userrole" + slevel + "\"";

								createView1 = createView1 + " ,(SELECT  max(desig.sdesignationname::text)    "
										+ "  FROM registrationtesthistory rh, users u, "
										+ "    designation desig WHERE (rh.ntransactionstatus IN ( SELECT acr.napprovalstatuscode "
										+ "      FROM approvalconfig ac,approvalconfigrole acr "
										+ "      WHERE ac.napprovalconfigcode = acr.napprovalconfigcode AND acr.nlevelno  = "
										+ newlstapprove.get(j).getNlevelno() + "   AND acr.ntransactionstatus = "
										+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
										+ " AND ac.nregsubtypecode = d.nregsubtypecode AND ac.nsitecode = "
										+ userInfo.getNmastersitecode() + " AND acr.nsitecode = "
										+ userInfo.getNmastersitecode() + ")) "
										+ "   AND rh.nusercode = u.nusercode and u.ndesignationcode = desig.ndesignationcode and d.npreregno = rh.npreregno group by rh.npreregno)  as \""
										+ transactionstatus + "userdesignation" + slevel + "\"";

								createView1 = createView1 + " ,(SELECT max(rh.dtransactiondate)    "
										+ "  FROM registrationtesthistory rh WHERE rh.ntransactionstatus IN ( SELECT acr.napprovalstatuscode "
										+ "      FROM approvalconfig ac,approvalconfigrole acr "
										+ "      WHERE ac.napprovalconfigcode = acr.napprovalconfigcode AND acr.nlevelno  = "
										+ newlstapprove.get(j).getNlevelno() + "   AND acr.ntransactionstatus = "
										+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
										+ " AND ac.nregsubtypecode = d.nregsubtypecode AND ac.nsitecode = "
										+ userInfo.getNmastersitecode() + " AND acr.nsitecode = "
										+ userInfo.getNmastersitecode() + ") "
										+ "    and d.ntransactiontestcode = rh.ntransactiontestcode group by rh.ntransactiontestcode"
										+ " order by rh.ntransactiontestcode desc limit 1)  as \""
										+ transactionstatus + "date" + slevel + "\"";

								// JIRA-ALPD-5017->SATHISH-> CHANGE ssignimagename to ssignimageftp for report
								// signature
								createView1 = createView1 + " ,(SELECT max(u.ssignimgftp::text)  "
										+ "  FROM registrationtesthistory rh, "
										+ "  userfile u WHERE (rh.ntransactionstatus IN ( SELECT acr.napprovalstatuscode "
										+ "      FROM approvalconfig ac,approvalconfigrole acr "
										+ "      WHERE ac.napprovalconfigcode = acr.napprovalconfigcode AND acr.nlevelno  = "
										+ newlstapprove.get(j).getNlevelno() + "   AND acr.ntransactionstatus = "
										+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
										+ " AND ac.nregsubtypecode = d.nregsubtypecode AND ac.nsitecode = "
										+ userInfo.getNmastersitecode() + " AND acr.nsitecode = "
										+ userInfo.getNmastersitecode() + ")) "
										+ "   AND rh.nusercode = u.nusercode and d.npreregno = rh.npreregno group by rh.npreregno)  as \""
										+ transactionstatus + "usersign" + slevel + "\"";

							}

							////////// Start for pre-registered date code creation------------

							Map<String, Object> preRegDateMap = new HashMap<String, Object>();
							preRegDateMap.put("columnname", "preregistereddate");
							preRegDateMap.put("columntype", 2);
							preRegDateMap.put("displayname", "preregistereddate");
							preRegDateMap.put("columntypedesc", "datetime");
							preRegDateMap.put("needmasterdata", false);

							obj2.add(preRegDateMap);

							Map<String, Object> preRegDate = new HashMap<String, Object>();
							preRegDate.put("columnname", "preregistereddate");
							preRegDate.put("languagecode", false);
							preRegDate.put("displayname", "preregistereddate");

							obj3.add(preRegDate);

							/////////////// End for pre-registered date code creation/////////////

							////////// Start for registered date transaction date code creation------------

							Map<String, Object> regDateMap = new HashMap<String, Object>();
							preRegDateMap.put("columnname", "registereddate");
							preRegDateMap.put("columntype", 2);
							preRegDateMap.put("displayname", "registereddate");
							preRegDateMap.put("columntypedesc", "datetime");
							preRegDateMap.put("needmasterdata", false);

							obj2.add(regDateMap);

							Map<String, Object> regDate = new HashMap<String, Object>();
							preRegDate.put("columnname", "registereddate");
							preRegDate.put("languagecode", false);
							preRegDate.put("displayname", "registereddate");

							obj3.add(regDate);

							/////////////// End for registered date code creation/////////////

							////////// Start for completed date code creation------------

							Map<String, Object> completedDateMap = new HashMap<String, Object>();
							completedDateMap.put("columnname", "completeddate");
							completedDateMap.put("columntype", 2);
							completedDateMap.put("displayname", "completeddate");
							completedDateMap.put("columntypedesc", "datetime");
							completedDateMap.put("needmasterdata", false);

							obj2.add(completedDateMap);

							Map<String, Object> completedDate = new HashMap<String, Object>();
							completedDate.put("columnname", "completeddate");
							completedDate.put("languagecode", false);
							completedDate.put("displayname", "completeddate");

							obj3.add(completedDate);

							/////////////// End for completed date code creation/////////////

							/////////////////////// Start for completed user name code creation////////////

							Map<String, Object> objCompletedUser = new HashMap<String, Object>();
							objCompletedUser.put("columnname", "completedby");
							objCompletedUser.put("columntype", 1);
							objCompletedUser.put("displayname", "completedby");
							objCompletedUser.put("viewvaluemember", "completedby");
							objCompletedUser.put("valuemember", "sfirstname");
							objCompletedUser.put("displaymember", "sfirstname");
							objCompletedUser.put("columntypedesc", "textinput");
							objCompletedUser.put("needmasterdata", true);
							objCompletedUser.put("mastertablename", "users");

							obj2.add(objCompletedUser);

							Map<String, Object> completedUser = new HashMap<String, Object>();
							completedUser.put("columnname", "completedby");
							completedUser.put("languagecode", false);
							completedUser.put("displayname", "completedby");

							obj3.add(completedUser);

							/////////////////////// End for completed user name code creation////////////

							createView1 = createView1
									+ ",(select max(dtransactiondate) from registrationhistory where ntransactionstatus = "
									+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus()
									+ " and d.npreregno = npreregno group by npreregno) as preregistereddate";

							createView1 = createView1
									+ ",(select max(dtransactiondate) from registrationhistory where ntransactionstatus = "
									+ Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()
									+ " and d.npreregno = npreregno group by npreregno) as registereddate";

							createView1 = createView1
									+ ", (select max(dtransactiondate) from registrationtesthistory where ntransactionstatus = "
									+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()
									+ " and d.ntransactiontestcode = ntransactiontestcode group by ntransactiontestcode"
									+ " order by ntransactiontestcode limit 1) as completeddate";

							createView1 = createView1
									+ ", (select max(u.sfirstname::text || ' '::text || u.slastname::text)"
									+ " from registrationtesthistory r, users u where  r.ntransactionstatus = "
									+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() + " and"
									//									+ " d.npreregno = r.npreregno and d.ntransactionsamplecode = r.ntransactionsamplecode and "
									+ " d.ntransactiontestcode = r.ntransactiontestcode and "
									+ "  u.nusercode = r.nusercode and u.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and r.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " group by r.ntransactiontestcode order by r.ntransactiontestcode desc limit 1) as completedby";

						}

						createView1 = createView1 + " FROM   view_releasereport d , "
								+ "jsonb_populate_record(NULL::type_" + sampleName + ", d.jsonuidata) r ";
						if (regSubType.isNneedsubsample()) {

							createView1 = createView1 + "  ,jsonb_populate_record(NULL::type_" + subsampleTemplatename
									+ ", d.samplejsonuidata) rs ";
						}

						createView1 = createView1 + " where  d.ndesigntemplatemappingcode = "
								+ regSubType.getNdesigntemplatemappingcode() + " and d.napprovalversioncode = "
								+ napprovalConfigVersionCode + "";

						jdbcTemplate.execute(createView1);
						obj5.put("conditionfields", obj2);
						obj5.put("selectfields", obj3);

						JSONArray js = new JSONArray();
						js.put(obj5);

						final JSONArray jsqlColumns = new JSONArray(objJsqlQuerycolumns);

						jsqlColumns.putAll(jsqlColumnsQB1);
						JSONArray jsqlColumnsQB = new JSONArray();
						final String viewName = sviewName;
						jsqlColumns.forEach(x -> {
							JSONObject obj1 = (JSONObject) x;
							obj1.put("tablename", viewName);
							jsqlColumnsQB.put(obj1);

						});

						sviewName = queryBuildername.toLowerCase();
						final String insertTablequerybuildertables = "INSERT INTO querybuildertables (nquerybuildertablecode,nformcode,stablename,jsondata,nismastertable,nstatus,nsitecode,dmodifieddate) values"
								+ " (" + seqNoTableQueryTables + ",-3,'" + sviewName + "','" + editObjectQB + "',4,1,"
								+ userInfo.getNmastersitecode() + ",'"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "');";

						final String insertColumnsquerybuildertablecolumns = "INSERT INTO querybuildertablecolumns (nquerybuildertablecode,nformcode,stablename,sprimarykeyname"
								+ ",jstaticcolumns,jmultilingualcolumn,jdynamiccolumns,jnumericcolumns,jsqlquerycolumns,nstatus,nsitecode,dmodifieddate) values"
								+ " (" + seqNoTableQueryTables + ", -3 ,'" + sviewName + "','npreregno','"
								+ stringUtilityFunction
								.replaceQuote(objMapper.writeValueAsString(objMap.get("jdynamiccolumns")))
								+ "',null,null,'"
								+ stringUtilityFunction
								.replaceQuote(objMapper.writeValueAsString(objMap.get("jnumericcolumns")))
								+ "'," + "'" + stringUtilityFunction.replaceQuote(jsqlColumnsQB.toString()) + "',1,"
								+ userInfo.getNmastersitecode() + ",'"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "');";

						seqNoTableQueryTablesDetails++;

						String insertTablequerytabledetails = " INSERT INTO querytabledetails (nquerytabledetailcode,nquerybuildertablecode,nmodulecode,nformcode,nsitecode,nstatus,dmodifieddate) values"
								+ " (" + seqNoTableQueryTablesDetails + "," + seqNoTableQueryTables + ","
								+ Enumeration.ModuleCode.SAMPLEREGISTRATION.getModuleCode() + ","
								+ Enumeration.FormCode.SAMPLEREGISTRATION.getFormCode() + ","
								+ userInfo.getNmastersitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ",'"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "');";

						seqNoTableQueryTablesDetails++;

						insertTablequerytabledetails = insertTablequerytabledetails
								+ " INSERT INTO querytabledetails (nquerytabledetailcode,nquerybuildertablecode,nmodulecode,nformcode,nsitecode,nstatus,dmodifieddate) values"
								+ " (" + seqNoTableQueryTablesDetails + "," + seqNoTableQueryTables + ","
								+ Enumeration.ModuleCode.JOBALLOCATION.getModuleCode() + ","
								+ Enumeration.FormCode.JOBALLOCATION.getFormCode() + "," + userInfo.getNmastersitecode()
								+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ",'"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "');";

						seqNoTableQueryTablesDetails++;

						insertTablequerytabledetails = insertTablequerytabledetails
								+ " INSERT INTO querytabledetails (nquerytabledetailcode,nquerybuildertablecode,nmodulecode,nformcode,nsitecode,nstatus,dmodifieddate) values"
								+ " (" + seqNoTableQueryTablesDetails + "," + seqNoTableQueryTables + ","
								+ Enumeration.ModuleCode.JOBALLOCATION.getModuleCode() + ","
								+ Enumeration.FormCode.MYJOB.getFormCode() + "," + userInfo.getNmastersitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ",'"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "');";

						seqNoTableQueryTablesDetails++;

						insertTablequerytabledetails = insertTablequerytabledetails
								+ " INSERT INTO querytabledetails (nquerytabledetailcode,nquerybuildertablecode,nmodulecode,nformcode,nsitecode,nstatus,dmodifieddate) values"
								+ " (" + seqNoTableQueryTablesDetails + "," + seqNoTableQueryTables + ","
								+ Enumeration.ModuleCode.RESULTENTRY.getModuleCode() + ","
								+ Enumeration.FormCode.RESULTENTRY.getFormCode() + "," + userInfo.getNmastersitecode()
								+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ",'"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "');";

						seqNoTableQueryTablesDetails++;

						insertTablequerytabledetails = insertTablequerytabledetails
								+ " INSERT INTO querytabledetails (nquerytabledetailcode,nquerybuildertablecode,nmodulecode,nformcode,nsitecode,nstatus,dmodifieddate) values"
								+ " (" + seqNoTableQueryTablesDetails + "," + seqNoTableQueryTables + ","
								+ Enumeration.ModuleCode.APPROVAL.getModuleCode() + ","
								+ Enumeration.FormCode.APPROVAL.getFormCode() + "," + userInfo.getNmastersitecode()
								+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ",'"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "');";

						final String insertTable = "INSERT INTO querybuilderviews (nquerybuilderviewscode,sviewname,jsondata,nstatus,dmodifieddate,nsitecode) values"
								+ " (" + seqNoTable + ",'" + sviewName + "','"
								+ stringUtilityFunction.replaceQuote(editObject.toString()) + "',1,'"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
								+ userInfo.getNmastersitecode() + ");";

						final String insertColumns = "INSERT INTO querybuilderviewscolumns (sviewname,jsondata,nstatus,dmodifieddate,nsitecode) values"
								+ " ('" + sviewName + "','" + stringUtilityFunction.replaceQuote(js.get(0).toString())
								+ "',1,'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
								+ userInfo.getNmastersitecode() + " );";

						jdbcTemplate.execute(insertTable + insertColumns + insertTablequerybuildertables
								+ insertColumnsquerybuildertablecolumns + insertTablequerytabledetails);

						jdbcTemplate.execute(" update seqnocredentialmanagement set nsequenceno = " + seqNoTable
								+ " where stablename = 'querybuilderviews' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"
								+ " update seqnocredentialmanagement set nsequenceno = " + seqNoTableQueryTablesDetails
								+ " where stablename = 'querytabledetails' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"
								+ " update seqnocredentialmanagement set nsequenceno = " + seqNoTableQueryTables
								+ " where stablename = 'querybuildertables' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";");

						String jsqlquery = " update querybuildertablecolumns set jsqlquerycolumns = '"
								+ stringUtilityFunction.replaceQuote(objMapper.writeValueAsString(jasample))
								+ "' where stablename = 'registration'  and nformcode = 43;";

						if (regSubType.isNneedsubsample()) {
							jsqlquery = jsqlquery + " update querybuildertablecolumns set jsqlquerycolumns = '"
									+ stringUtilityFunction.replaceQuote(objMapper.writeValueAsString(jasubsample))
									+ "' where stablename = 'registrationsample'  and nformcode = 43;";
						}

						jdbcTemplate.execute(jsqlquery);

					}
				} else {
					// Added by sonia for jira id ALPD-5086
					if ((int) approveConfigVersion.get(
							"napprovalsubtypecode") == Enumeration.ApprovalSubType.PROTOCOLAPPROVAL.getnsubtype()) {
						String str1 = "select rt.nreactregtemplatecode, rt.nsampletypecode, rt.nsubsampletypecode, rt.ndefaulttemplatecode, rt.ntransactionstatus, rt.sregtemplatename, rt.jsondata, rt.dmodifieddate, rt.nsitecode, rt.nstatus, rt.stemplatetypesname,"
								+ " dm.ndesigntemplatemappingcode, dm.nsampletypecode, dm.nformcode, dm.nregtypecode, dm.nregsubtypecode, dm.nregsubtypeversioncode, dm.nformwisetypecode, dm.nreactregtemplatecode, dm.nsubsampletemplatecode, dm.ntransactionstatus, dm.nversionno, dm.dmodifieddate, dm.nsitecode, dm.nstatus"
								+ " from reactregistrationtemplate rt "
								+ " join designtemplatemapping dm on dm.nreactregtemplatecode = rt.nreactregtemplatecode "
								+ " and dm.ndesigntemplatemappingcode  = " + ndesigntemplatemappingcode
								+ " and rt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and dm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and rt.nsitecode = " + userInfo.getNmastersitecode() + " ";
						DesignTemplateMapping object = (DesignTemplateMapping) jdbcUtilityFunction.queryForObject(str1,
								DesignTemplateMapping.class, jdbcTemplate);
						String templateName = object.getStemplatetypesname().replaceAll(" ", "")
								.replaceAll("[^a-zA-Z0-9]", "");

						createView(sviewName, templateName, object.getNdesigntemplatemappingcode(),
								object.getNformcode(), false);

						final String getSeqNoQuery = "select stablename,nsequenceno from seqnocredentialmanagement "
								+ " where stablename in('querybuilderviews','querybuildertables','querytabledetails')"
								+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
						List<SeqNoCredentialManagement> seqList = jdbcTemplate.query(getSeqNoQuery,
								new SeqNoCredentialManagement());

						Map<String, Integer> seqMap = seqList.stream()
								.collect(Collectors.toMap(SeqNoCredentialManagement::getStablename,
										seqNoConfigurationMaster -> seqNoConfigurationMaster.getNsequenceno()));
						int seqNoQBV = seqMap.get("querybuilderviews") + 1;

						int seqNoQBT = seqMap.get("querybuildertables") + 1;

						int seqNoQTD = seqMap.get("querytabledetails") + 1;

						final String viewName = sviewName;

						final JSONObject editObject = new JSONObject();
						editObject.put("tablename", new JSONObject() {
							{
								put(userInfo.getSlanguagetypecode(), viewName);
							}
						});

						final JSONObject editObject1 = new JSONObject();
						editObject1.put("displayname", new JSONObject() {
							{
								put(userInfo.getSlanguagetypecode(), viewName);
							}
						});

						List<Map<String, Object>> obj2 = (List<Map<String, Object>>) objMap.get("jsqlquerycolumns");
						final JSONArray sqleditObject = new JSONArray(obj2);
						JSONArray sqleditObject2 = new JSONArray();
						sqleditObject.forEach(x -> {
							JSONObject obj1 = (JSONObject) x;
							obj1.put("tablename", "view_" + viewName);
							sqleditObject2.put(obj1);

						});

						List<Map<String, Object>> objView2 = (List<Map<String, Object>>) objMap
								.get("sampleQuerybuilderViewCondition");
						List<Map<String, Object>> objView3 = (List<Map<String, Object>>) objMap
								.get("sampleQuerybuilderViewSelect");

						Map<String, Object> obj5 = new HashMap<>();
						obj5.put("conditionfields", objView2);
						obj5.put("selectfields", objView3);

						JSONArray js = new JSONArray();
						js.put(obj5);

						final String insertQueryBuilderTables = "INSERT INTO querybuildertables "
								+ "(nquerybuildertablecode,nformcode,stablename,jsondata,nismastertable,dmodifieddate,nsitecode,nstatus) "
								+ "values" + " (" + seqNoQBT + "," + Enumeration.QualisForms.PROTOCOL.getqualisforms()
								+ ",'" + sviewName + "','" + editObject + "',4,'"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
								+ userInfo.getNmastersitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

						final String insertQueryBuilderTableColumns = "INSERT INTO querybuildertablecolumns (nquerybuildertablecode,nformcode,stablename,sprimarykeyname"
								+ ",jstaticcolumns,jmultilingualcolumn,jdynamiccolumns,jnumericcolumns,jsqlquerycolumns,dmodifieddate,nsitecode,nstatus) values"
								+ " (" + seqNoQBT + ", " + Enumeration.QualisForms.PROTOCOL.getqualisforms() + " ,'"
								+ sviewName + "','nprotocolcode','"
								+ stringUtilityFunction
								.replaceQuote(objMapper.writeValueAsString(objMap.get("jdynamiccolumns")))
								+ "',null,null,'"
								+ stringUtilityFunction
								.replaceQuote(objMapper.writeValueAsString(objMap.get("jnumericcolumns")))
								+ "'," + "'" + stringUtilityFunction.replaceQuote(sqleditObject2.toString()) + "','"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
								+ userInfo.getNmastersitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " );";

						String insertQueryTableDetails = " INSERT INTO querytabledetails"
								+ " (nquerytabledetailcode,nquerybuildertablecode,nmodulecode,nformcode,dmodifieddate,nsitecode,nstatus)"
								+ " values" + " (" + seqNoQTD + "," + seqNoQBT + ","
								+ Enumeration.ModuleCode.STABILITY.getModuleCode() + ","
								+ Enumeration.FormCode.PROTOCOL.getFormCode() + ",'"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
								+ userInfo.getNmastersitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

						final String insertQueryBuilderViews = "INSERT INTO querybuilderviews "
								+ "(nquerybuilderviewscode,sviewname,jsondata,dmodifieddate,nsitecode,nstatus,nisdynamic) "
								+ "values" + " (" + seqNoQBV + ",'" + sviewName + "','"
								+ stringUtilityFunction.replaceQuote(editObject1.toString()) + "'," + " '"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
								+ userInfo.getNmastersitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ",4);";

						final String insertQueryBuilderViewColumns = "INSERT INTO querybuilderviewscolumns "
								+ "(sviewname,jsondata,dmodifieddate,nsitecode,nstatus) " + "values" + " ('" + sviewName
								+ "','" + stringUtilityFunction.replaceQuote(js.get(0).toString()) + "'," + " '"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
								+ userInfo.getNmastersitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " );";

						jdbcTemplate.execute(insertQueryBuilderTables + insertQueryBuilderTableColumns
								+ insertQueryTableDetails + insertQueryBuilderViews + insertQueryBuilderViewColumns);

						jdbcTemplate.execute(" update seqnocredentialmanagement set nsequenceno = " + seqNoQBV
								+ " where stablename = 'querybuilderviews' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"
								+ " update seqnocredentialmanagement set nsequenceno = " + seqNoQTD
								+ " where stablename = 'querytabledetails' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"
								+ " update seqnocredentialmanagement set nsequenceno = " + seqNoQBT
								+ " where stablename = 'querybuildertables' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";");

						System.out.println("Sonia");
					}
				}

				draftList.add(lstapprove);
				draftList.add(lstVersion);
				approvedList.add(newVersionList);
				multilingualIDList.add("IDS_APPROVEAPPROVALCONFIGVERSION");
				multilingualIDList.add("IDS_APPROVEAPPROVALCONFIGVERSION");
				auditUtilityFunction.fnInsertListAuditAction(approvedList, 2, draftList, multilingualIDList, userInfo);
				Map<String, Object> getMap = new HashMap<String, Object>();
				getMap.put("napprovalconfigcode", napprovalConfigCode);
				getMap.put("napprovalsubtypecode", (int) approveConfigVersion.get("napprovalsubtypecode"));
				getMap.put("approvedversioncode", napprovalConfigVersionCode);
				getMap.put("ntreeversiontempcode", approveConfigVersion.get("ntreeversiontempcode"));
				return getApprovalConfigVersion(getMap, userInfo);

			} else {
				return new ResponseEntity<Object>(commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.SELECTDRAFTVERSION.getreturnstatus(), userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
		Set<Object> seen = ConcurrentHashMap.newKeySet();
		return t -> seen.add(keyExtractor.apply(t));
	}

	@Override
	public String preValidateApprovalConfigVersion(Map<String, Object> objMap1) throws Exception {
		@SuppressWarnings("unchecked")
		Map<String, Object> objMap = (Map<String, Object>) objMap1.get("approvalconfigversion");
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(objMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		String rtnString = "";
		String str = "select apr.napprovalconfigrolecode, apr.napprovalconfigcode, apr.ntreeversiontempcode, apr.nuserrolecode, apr.nchecklistversioncode, apr.npartialapprovalneed,"
				+ " apr.nsectionwiseapprovalneed, apr.nrecomretestneed, apr.nrecomrecalcneed, apr.nretestneed, apr.nrecalcneed, apr.nlevelno, apr.napprovalstatuscode, apr.napproveconfversioncode,"
				+ " apr.nautoapproval, apr.nautoapprovalstatuscode, apr.nautodescisionstatuscode, apr.ncorrectionneed, apr.nesignneed, apr.ntransactionstatus, apr.dmodifieddate, apr.nsitecode, apr.nstatus"
				+ " from approvalconfigrole apr  where apr.napprovalconfigcode = "
				+ objMap.get("napprovalconfigcode") + " and apr.napproveconfversioncode = "
				+ objMap.get("napprovalconfigversioncode") + " and   apr.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		List<ApprovalConfigRole> lstApprovalConfigRole = jdbcTemplate.query(str,
				new ApprovalConfigRole());
		if (!lstApprovalConfigRole.isEmpty()) {
			String strtemplate = " ";
			String strwholetemplate = " ";
			String Strfull = " ";
			String Strunion = " ";
			int i = 0;
			while (i < lstApprovalConfigRole.size()) {
				if (i == lstApprovalConfigRole.size() - 1) {
					Strunion = " ";
				} else {
					Strunion = "  union all  ";
				}
				strtemplate = "   select count(napprovalfiltercode) as value,"
						+ lstApprovalConfigRole.get(i).getNapprovalconfigrolecode() + " as approle,"
						+ lstApprovalConfigRole.get(i).getNlevelno() + " as levelno,"
						+ lstApprovalConfigRole.get(i).getNuserrolecode()
						+ " as nuserrolecode from approvalrolefilterdetail where ntransactionstatus >"
						+ Enumeration.TransactionStatus.ALL.gettransactionstatus() + " and napprovalconfigrolecode = "
						+ lstApprovalConfigRole.get(i).getNapprovalconfigrolecode() + " " + " union all "
						+ " select count(napprovalvalidationcode) as value ,"
						+ lstApprovalConfigRole.get(i).getNapprovalconfigrolecode() + " as approle,"
						+ lstApprovalConfigRole.get(i).getNlevelno() + " as levelno,"
						+ lstApprovalConfigRole.get(i).getNuserrolecode()
						+ " as nuserrolecode from approvalrolevalidationdetail where napprovalconfigrolecode = "
						+ lstApprovalConfigRole.get(i).getNapprovalconfigrolecode() + " and nsitecode = "
						+ userInfo.getNmastersitecode();

				strwholetemplate = strwholetemplate.concat(strtemplate).concat(Strunion);
				i++;
			}
			Strfull = ";with prevalid AS(" + strwholetemplate
					+ ")  select ROW_NUMBER() over(partition by pr.levelno order by pr.approle ) as rno,ur.suserrolename, pr.* from prevalid pr,userrole ur where  pr.value = 0 and ur.nuserrolecode =  pr.nuserrolecode;";
			List<Map<String, Object>> lsttoanalise = jdbcTemplate.queryForList(Strfull);
			if (!lsttoanalise.isEmpty()) {
				String errorString = "";
				int j = 0;
				String addString = commonFunction.getMultilingualMessage("IDS_ADD", userInfo.getSlanguagefilename());
				String forString = commonFunction.getMultilingualMessage("IDS_FOR", userInfo.getSlanguagefilename());
				String filterString = commonFunction.getMultilingualMessage("IDS_FILTERDETAILS",
						userInfo.getSlanguagefilename());
				String validateString = commonFunction.getMultilingualMessage("IDS_VALIDATIONDETAILS",
						userInfo.getSlanguagefilename());
				String decisionString = commonFunction.getMultilingualMessage("IDS_DECISIONDETAILS",
						userInfo.getSlanguagefilename());
				while (j < lsttoanalise.size()) {
					Map<String, Object> objt = lsttoanalise.get(j);
					String detail = ((long) objt.get("rno") == 1) ? filterString
							: ((long) objt.get("rno") == 2) ? validateString : decisionString;
					errorString += addString + " " + detail + " " + forString + " " + (String) objt.get("suserrolename")
					+ " / ";
					j++;
				}
				rtnString = errorString;
			} else {
				rtnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
			}
		} else {
			rtnString = "IDS_SELECTVERSIONTOAPPROVE";
		}
		return rtnString;
	}

	@Override
	public ResponseEntity<Object> copyApprovalConfigVersion(Map<String, Object> objMap, UserInfo userInfo)
			throws Exception {

		final String sQuery = " lock  table lockapprovalconfiguration "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		int napprovalConfigVersionCode = (int) objMap.get("napprovalconfigversioncode");
		int napprovalsubtypecode = (int) objMap.get("napprovalsubtypecode");
		int nregTypeCode = (int) objMap.get("nregtypecode");
		int nregSubTypeCode = (int) objMap.get("nregsubtypecode");
		String approvalConfigQuery = "";
		String sapprovalConfigCode = "";
		ApprovalConfigAutoapproval objApprovalConfigAutoapproval = getApprovalConfigVersionByID(
				napprovalConfigVersionCode, userInfo);
		if (objApprovalConfigAutoapproval == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (napprovalsubtypecode == 2 && nregSubTypeCode == -1 && nregTypeCode == -1) {
				approvalConfigQuery = "select stuff((select ','+ convert(nvarchar(5),napprovalconfigcode) from approvalconfig where"
						+ " napprovalsubtypecode = " + napprovalsubtypecode + " and nsitecode = "
						+ userInfo.getNmastersitecode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " for xml path('')),1,1,'')";
			} else {
				approvalConfigQuery = "select napprovalconfigcode from approvalconfig where napprovalsubtypecode = "
						+ napprovalsubtypecode + " " + "and nregtypecode = " + nregTypeCode + " and nregsubtypecode = "
						+ nregSubTypeCode + " and nsitecode = " + userInfo.getNmastersitecode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			}

			sapprovalConfigCode = jdbcTemplate.queryForObject(approvalConfigQuery, String.class);

			String str = "select nautoapprovalcode from approvalconfigautoapproval where sversionname  = N'"
					+ stringUtilityFunction.replaceQuote((String) objMap.get("sversionname"))
					+ "' and napprovalconfigcode in (" + sapprovalConfigCode + ")" + " and nsitecode = "
					+ userInfo.getNmastersitecode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			List<ApprovalConfigAutoapproval> validateObject = jdbcTemplate.query(str,
					new ApprovalConfigAutoapproval());
			if (!validateObject.isEmpty()) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}

			String templateValidate = "select tobecopied.ntreeversiontempcode,tobecopied.napprovalconfigcode from("
					+ " select ttt.nuserrolecode,ttt.nlevelno,ttt.napprovalconfigcode"
					+ " from treetemplatetransactionrole ttt,approvalconfigversion acv"
					+ " where ttt.ntreeversiontempcode  = acv.ntreeversiontempcode  and ttt.ntransactionstatus = "
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and acv.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  ttt.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ttt.nsitecode = "
					+ userInfo.getNmastersitecode() + " and acv.nsitecode = " + userInfo.getNmastersitecode() + " "
					+ " and  napproveconfversioncode = " + napprovalConfigVersionCode + " ) selectedversion,"

					+ " (select ttt.nuserrolecode,ttt.nlevelno,ttt.napprovalconfigcode,ttt.ntreeversiontempcode from treetemplatetransactionrole ttt"
					+ " where nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "  and ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
					+ " and ttt.nsitecode = " + userInfo.getNmastersitecode() + "  and  napprovalconfigcode in ("
					+ sapprovalConfigCode + ")" + " and ttt.schildnode <> any(select case when nneedanalyst = "
					+ Enumeration.TransactionStatus.YES.gettransactionstatus()
					+ " then '-1' else '0' end from approvalconfig where napprovalconfigcode in (" + sapprovalConfigCode
					+ "))) tobecopied,"

					+ " ( select count(ttt.nlevelno) as levelcount from treetemplatetransactionrole ttt,approvalconfigversion acv"
					+ " where ttt.ntreeversiontempcode  = acv.ntreeversiontempcode  and ttt.ntransactionstatus = "
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and acv.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  ttt.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ttt.nsitecode = "
					+ userInfo.getNmastersitecode() + " " + " and  napproveconfversioncode = "
					+ napprovalConfigVersionCode + " and ttt.schildnode <> any(select case when nneedanalyst = "
					+ Enumeration.TransactionStatus.YES.gettransactionstatus()
					+ " then '-1' else '0' end from approvalconfig where napprovalconfigcode in (" + sapprovalConfigCode
					+ "))" + " ) selectedcount,"

					+ " (select count(ttt.nlevelno) as levelcount from treetemplatetransactionrole ttt"
					+ " where nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "  and ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
					+ " and ttt.nsitecode = " + userInfo.getNmastersitecode() + " and  napprovalconfigcode in ("
					+ sapprovalConfigCode + ")" + " and ttt.schildnode <> any(select case when nneedanalyst = "
					+ Enumeration.TransactionStatus.YES.gettransactionstatus()
					+ " then '-1' else '0' end from approvalconfig where napprovalconfigcode in (" + sapprovalConfigCode
					+ "))" + " ) copycount"

					+ " where selectedversion.nuserrolecode = tobecopied.nuserrolecode"
					+ " and selectedversion.nlevelno = tobecopied.nlevelno "
					+ " and selectedcount.levelcount =  copycount.levelcount "
					+ " group by tobecopied.napprovalconfigcode , tobecopied.ntreeversiontempcode";

			List<Map<String, Object>> lstTreeTemplateCode = jdbcTemplate.queryForList(templateValidate);
			if (!lstTreeTemplateCode.isEmpty()) {
				int currentTreeTemplatecode = (int) lstTreeTemplateCode.get(0).get("ntreeversiontempcode");
				int napprovalConfigCode = (int) lstTreeTemplateCode.get(0).get("napprovalconfigcode");
				str = " select stablename,nsequenceno from seqnoconfigurationmaster where stablename in (N'approvalconfigversion',N'approvalconfigautoapproval',"
						+ " N'approvalconfigrole',N'approvalrolefilterdetail',N'approvalrolevalidationdetail',N'approvalroledecisiondetail',N'approvalroleactiondetail')"
						+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
				List<SeqNoConfigurationMaster> lstSeqNo = jdbcTemplate.query(str,
						new SeqNoConfigurationMaster());
				Map<String, Integer> seqMap = lstSeqNo.stream()
						.collect(Collectors.toMap(SeqNoConfigurationMaster::getStablename,
								seqNoConfigurationMaster -> seqNoConfigurationMaster.getNsequenceno()));
				int nseqAutoApprovalCode = seqMap.get("approvalconfigautoapproval");
				int nseqApprovalConfigRoleCode = seqMap.get("approvalconfigrole");
				int nseqApproveConfVersionCode = seqMap.get("approvalconfigversion");
				int nseqRoleActionCode = seqMap.get("approvalroleactiondetail");
				int nseqRoleDecisionCode = seqMap.get("approvalroledecisiondetail");
				int nseqRoleFilterCode = seqMap.get("approvalrolefilterdetail");
				int nseqRoleValidationCode = seqMap.get("approvalrolevalidationdetail");

				str = "select  * from" + " (select count(napprovalconfigrolecode) as rolecount from approvalconfigrole"
						+ " where napproveconfversioncode = " + napprovalConfigVersionCode + " and nsitecode = "
						+ userInfo.getNmastersitecode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")  a,"
						+ " (select count(napprovalfiltercode)as filtercount from approvalrolefilterdetail afd,approvalconfigrole apr"
						+ " where afd.napprovalconfigrolecode = apr.napprovalconfigrolecode "
						+ " and napproveconfversioncode  = " + napprovalConfigVersionCode + " and afd.nsitecode = "
						+ userInfo.getNmastersitecode() + " and apr.nsitecode = " + userInfo.getNmastersitecode()
						+ " and afd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") b,"
						+ " (select count(napprovalvalidationcode) as validcount from approvalrolevalidationdetail avd,approvalconfigrole apr"
						+ " where avd.napprovalconfigrolecode = apr.napprovalconfigrolecode "
						+ " and napproveconfversioncode = " + napprovalConfigVersionCode + " and avd.nsitecode = "
						+ userInfo.getNmastersitecode() + " and apr.nsitecode = " + userInfo.getNmastersitecode()
						+ " and avd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") c,"
						+ " (select count(napprovaldecisioncode) as decisioncount from approvalroledecisiondetail ard,approvalconfigrole apr"
						+ " where ard.napprovalconfigrolecode = apr.napprovalconfigrolecode "
						+ " and napproveconfversioncode = " + napprovalConfigVersionCode + " and ard.nsitecode = "
						+ userInfo.getNmastersitecode() + " and apr.nsitecode = " + userInfo.getNmastersitecode()
						+ " and ard.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") d,"
						+ " (select count(napprovalactioncode) as actioncount from approvalroleactiondetail acd,approvalconfigrole apr"
						+ " where acd.napprovalconfigrolecode = apr.napprovalconfigrolecode "
						+ " and napproveconfversioncode = " + napprovalConfigVersionCode + " and acd.nsitecode = "
						+ userInfo.getNmastersitecode() + " and apr.nsitecode = " + userInfo.getNmastersitecode()
						+ " and acd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") e";

				List<Map<String, Object>> countMap = jdbcTemplate.queryForList(str);

				jdbcTemplate.execute(
						" insert into approvalconfigversion (napproveconfversioncode,napprovalconfigcode,napproveconfversionno,ntransactionstatus,ntreeversiontempcode,ndesigntemplatemappingcode,sapproveconfversiondesc,sviewname,dmodifieddate,nsitecode,nstatus)"
								+ " select " + (nseqApproveConfVersionCode + 1) + "," + napprovalConfigCode
								+ " as napprovalconfigcode, -1 as napproveconfversionno,"
								+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
								+ "  as ntransactionstatus," + currentTreeTemplatecode
								+ " as ntreeversiontempcode,-1,'-' as sapproveconfversiondesc,''," + "'"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' as dmodifieddate,"
								+ userInfo.getNmastersitecode() + " as nsitecode,"
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " as nstatus"
								+ " from approvalconfigversion where nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and napproveconfversioncode = " + napprovalConfigVersionCode + ";");

				jdbcTemplate.execute(
						" insert into approvalconfigautoapproval (nautoapprovalcode,napprovalconfigcode,napprovalconfigversioncode,sversionname,nneedautocomplete,nneedautoinnerband,nneedautoouterband,nneedautoapproval,nautodescisionstatus,nautoapprovalstatus,nautoallot,nneedjoballocation,dmodifieddate,nsitecode,nstatus)"
								+ " select " + (nseqAutoApprovalCode + 1) + " as nautoapprovalcode,"
								+ napprovalConfigCode + " as napprovalconfigcode," + (nseqApproveConfVersionCode + 1)
								+ " as napprovalconfigversioncode," + "N'"
								+ stringUtilityFunction.replaceQuote((String) objMap.get("sversionname"))
								+ "'  as sversionname,nneedautocomplete,nneedautoinnerband,nneedautoouterband,nneedautoapproval,nautodescisionstatus,nautoapprovalstatus,nautoallot,nneedjoballocation,"
								+ "'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "' as dmodifieddate ,"
								+ userInfo.getNmastersitecode() + ", "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " as nstatus"
								+ " from approvalconfigautoapproval where nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
								+ userInfo.getNmastersitecode() + " and napprovalconfigversioncode = "
								+ napprovalConfigVersionCode + ";");

				jdbcTemplate.execute(
						" insert into approvalconfigrole (napprovalconfigrolecode,napprovalconfigcode,ntreeversiontempcode,nuserrolecode,"
								+ " nchecklistversioncode,npartialapprovalneed,nsectionwiseapprovalneed,nrecomretestneed,nrecomrecalcneed,nretestneed,nrecalcneed,nlevelno,napprovalstatuscode,napproveconfversioncode,"
								+ " nautoapproval,nautoapprovalstatuscode,nautodescisionstatuscode,ncorrectionneed,nesignneed,ntransactionstatus,dmodifieddate,nsitecode,nstatus)"
								+ " select  " + nseqApprovalConfigRoleCode
								+ " + cast(ROW_NUMBER() OVER (ORDER BY apr.nlevelno ) as int) as napprovalconfigrolecode,"
								+ napprovalConfigCode + " as napprovalconfigcode," + currentTreeTemplatecode
								+ " as ntreeversiontempcode,apr. nuserrolecode,apr.nchecklistversioncode,apr.npartialapprovalneed,apr.nsectionwiseapprovalneed,apr.nrecomretestneed,"
								+ " apr.nrecomrecalcneed,apr.nretestneed,apr.nrecalcneed,apr.nlevelno,apr.napprovalstatuscode,"
								+ (nseqApproveConfVersionCode + 1) + " as  napproveconfversioncode"
								+ ",apr.nautoapproval,apr.nautoapprovalstatuscode ,apr.nautodescisionstatuscode,apr.ncorrectionneed,apr.nesignneed,8 as ntransactionstatus,'"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',nsitecode ,apr.nstatus"
								+ " from  	approvalconfigrole apr where  apr.napproveconfversioncode = "
								+ napprovalConfigVersionCode + " and apr.nsitecode = " + userInfo.getNmastersitecode()
								+ " and apr.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ ";");

				jdbcTemplate.execute(
						" insert into approvalrolefilterdetail (napprovalfiltercode,napprovalconfigrolecode,napprovalconfigcode,nuserrolecode,nlevelno,ntransactionstatus,ndefaultstatus,dmodifieddate,nsitecode,nstatus)"
								+ "select " + nseqRoleFilterCode
								+ "+ cast(ROW_NUMBER() OVER (ORDER BY  apr.nlevelno,afd.napprovalconfigrolecode )as int) as napprovalfiltercode,"
								+ nseqApprovalConfigRoleCode
								+ " + cast(DENSE_RANK() OVER (ORDER BY apr.nlevelno ) as int) as napprovalconfigrolecode,"
								+ napprovalConfigCode + " as napprovalconfigcode ,"
								+ " afd.nuserrolecode,afd.nlevelno, afd.ntransactionstatus,afd.ndefaultstatus,'"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' as dmodifieddate,"
								+ userInfo.getNmastersitecode() + " as nsitecode,afd.nstatus"
								+ " from approvalconfigrole apr, approvalrolefilterdetail afd"
								+ " where afd.napprovalconfigrolecode = apr.napprovalconfigrolecode"
								+ " and apr.napproveconfversioncode = " + napprovalConfigVersionCode
								+ " and apr.nsitecode = " + userInfo.getNmastersitecode() + " and afd.nsitecode = "
								+ userInfo.getNmastersitecode() + " and apr.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";");

				jdbcTemplate.execute(
						" insert into approvalrolevalidationdetail (napprovalvalidationcode,napprovalconfigrolecode,napprovalconfigcode,nuserrolecode,nlevelno,ntransactionstatus,dmodifieddate,nsitecode,nstatus)"
								+ "select " + nseqRoleValidationCode
								+ "+ cast(ROW_NUMBER() OVER (ORDER BY  apr.nlevelno,avd.napprovalconfigrolecode )as int) as napprovalvalidationcode,"
								+ nseqApprovalConfigRoleCode
								+ " + cast(DENSE_RANK() OVER (ORDER BY apr.nlevelno ) as int) as napprovalconfigrolecode,"
								+ napprovalConfigCode + " as napprovalconfigcode ,"
								+ " avd.nuserrolecode,avd.nlevelno, avd.ntransactionstatus,'"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' as dmodifieddate,"
								+ userInfo.getNmastersitecode() + " as nsitecode,avd.nstatus"
								+ " from approvalconfigrole apr, approvalrolevalidationdetail avd"
								+ " where avd.napprovalconfigrolecode = apr.napprovalconfigrolecode"
								+ " and apr.napproveconfversioncode = " + napprovalConfigVersionCode
								+ " and apr.nsitecode = " + userInfo.getNmastersitecode() + " and avd.nsitecode = "
								+ userInfo.getNmastersitecode() + " and apr.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";");

				jdbcTemplate.execute(
						" insert into approvalroledecisiondetail (napprovaldecisioncode,napprovalconfigrolecode,napprovalconfigcode,nuserrolecode,nlevelno,ntransactionstatus,ndefaultstatus,dmodifieddate,nsitecode,nstatus)"
								+ "select " + nseqRoleDecisionCode
								+ "+ cast(ROW_NUMBER() OVER (ORDER BY  apr.nlevelno,ard.napprovalconfigrolecode )as int) as napprovaldecisioncode,"
								+ nseqApprovalConfigRoleCode
								+ " + cast(DENSE_RANK() OVER (ORDER BY apr.nlevelno ) as int) as napprovalconfigrolecode, "
								+ napprovalConfigCode + " as napprovalconfigcode ,"
								+ " ard.nuserrolecode,ard.nlevelno, ard.ntransactionstatus,ard.ndefaultstatus,'"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' as dmodifieddate,"
								+ userInfo.getNmastersitecode() + " as nsitecode ,ard.nstatus"
								+ " from approvalconfigrole apr, approvalroledecisiondetail ard"
								+ " where ard.napprovalconfigrolecode = apr.napprovalconfigrolecode"
								+ " and apr.napproveconfversioncode = " + napprovalConfigVersionCode
								+ " and ard.nsitecode = " + userInfo.getNmastersitecode() + " and apr.nsitecode = "
								+ userInfo.getNmastersitecode() + " and apr.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";");

				jdbcTemplate.execute(
						" insert into approvalroleactiondetail (napprovalactioncode,napprovalconfigrolecode,napprovalconfigcode,nuserrolecode,nlevelno,ntransactionstatus,dmodifieddate,nsitecode,nstatus)"
								+ "select " + nseqRoleActionCode
								+ "+ cast(ROW_NUMBER() OVER (ORDER BY  apr.nlevelno,acd.napprovalconfigrolecode )as int) as napprovalactioncode,"
								+ nseqApprovalConfigRoleCode
								+ " + cast(DENSE_RANK() OVER (ORDER BY apr.nlevelno ) as int) as napprovalconfigrolecode, "
								+ napprovalConfigCode + " as napprovalconfigcode ,"
								+ " acd.nuserrolecode,acd.nlevelno, acd.ntransactionstatus,'"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' as dmodifieddate,"
								+ userInfo.getNmastersitecode() + " as nsitecode ,acd.nstatus"
								+ " from approvalconfigrole apr, approvalroleactiondetail acd"
								+ " where acd.napprovalconfigrolecode = apr.napprovalconfigrolecode"
								+ " and apr.napproveconfversioncode = " + napprovalConfigVersionCode
								+ " and apr.nsitecode = " + userInfo.getNmastersitecode() + " and acd.nsitecode = "
								+ userInfo.getNmastersitecode() + " and apr.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";");

				jdbcTemplate.execute(
						"  update seqnoconfigurationmaster set nsequenceno = nsequenceno+1 where stablename = N'approvalconfigversion' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"
								+ "  update seqnoconfigurationmaster set nsequenceno = nsequenceno+1 where stablename = N'approvalconfigautoapproval' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"
								+ " update seqnoconfigurationmaster set nsequenceno = nsequenceno+"
								+ countMap.get(0).get("rolecount") + " where stablename = N'approvalconfigrole' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"
								+ "  update seqnoconfigurationmaster set nsequenceno = nsequenceno+"
								+ countMap.get(0).get("filtercount")
								+ " where stablename = N'approvalrolefilterdetail' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"
								+ "  update seqnoconfigurationmaster set nsequenceno = nsequenceno+"
								+ countMap.get(0).get("validcount")
								+ " where stablename = N'approvalrolevalidationdetail' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"
								+ "  update seqnoconfigurationmaster set nsequenceno = nsequenceno+"
								+ countMap.get(0).get("actioncount")
								+ " where stablename = N'approvalroleactiondetail' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"
								+ "  update seqnoconfigurationmaster set nsequenceno = nsequenceno+"
								+ countMap.get(0).get("decisioncount")
								+ " where stablename = N'approvalroledecisiondetail' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";");

				str = "select napproveconfversioncode, napprovalconfigcode, napproveconfversionno, ntransactionstatus, ntreeversiontempcode, ndesigntemplatemappingcode, sapproveconfversiondesc, sviewname, dmodifieddate, nsitecode, nstatus"
						+ " from approvalconfigversion where napproveconfversioncode = " + napprovalConfigVersionCode
						+ ";";
				List<ApprovalConfigVersion> lstApprovalConfigVersion = jdbcTemplate.query(str,
						new ApprovalConfigVersion());

				str = "select nautoapprovalcode, napprovalconfigcode, napprovalconfigversioncode, sversionname, nneedautocomplete, nneedautoapproval, nautodescisionstatus, nautoapprovalstatus, nautoallot,"
						+ " nneedjoballocation, nneedautoinnerband, nneedautoouterband, dmodifieddate, nsitecode, nstatus"
						+ " from approvalconfigautoapproval where napprovalconfigversioncode = "
						+ napprovalConfigVersionCode + " and nsitecode = " + userInfo.getNmastersitecode() + ";";
				List<ApprovalConfigAutoapproval> lstApprovalConfigAutoapproval = jdbcTemplate.query(str,
						new ApprovalConfigAutoapproval());

				str = "select napprovalconfigrolecode, napprovalconfigcode, ntreeversiontempcode, nuserrolecode, nchecklistversioncode, npartialapprovalneed, nsectionwiseapprovalneed, nrecomretestneed,"
						+ " nrecomrecalcneed, nretestneed, nrecalcneed, nlevelno, napprovalstatuscode, napproveconfversioncode, nautoapproval, nautoapprovalstatuscode, nautodescisionstatuscode, ncorrectionneed,"
						+ " nesignneed, ntransactionstatus, dmodifieddate, nsitecode, nstatus"
						+ " from approvalconfigrole where napproveconfversioncode = " + napprovalConfigVersionCode
						+ " and nsitecode = " + userInfo.getNmastersitecode() + ";";
				List<ApprovalConfigRole> lstApprovalConfigRole = jdbcTemplate.query(str, new ApprovalConfigRole());

				str = "select acf.napprovalfiltercode, acf.napprovalconfigrolecode, acf.napprovalconfigcode, acf.nuserrolecode, acf.nlevelno, acf.ndefaultstatus, acf.ntransactionstatus, acf.dmodifieddate, acf.nsitecode, acf.nstatus"
						+ " from approvalrolefilterdetail acf,approvalconfigrole acr where  acf.napprovalconfigrolecode = acr.napprovalconfigrolecode and acr.napproveconfversioncode = "
						+ napprovalConfigVersionCode + " and acf.nsitecode = " + userInfo.getNmastersitecode()
						+ " and acr.nsitecode = " + userInfo.getNmastersitecode() + ";";
				List<ApprovalRoleFilterDetail> lstApprovalRoleFilterDetail = jdbcTemplate.query(str,
						new ApprovalRoleFilterDetail());

				str = "select acv.napprovalvalidationcode, acv.napprovalconfigrolecode, acv.napprovalconfigcode, acv.nuserrolecode, acv.nlevelno, acv.ntransactionstatus, acv.dmodifieddate, acv.nsitecode, acv.nstatus"
						+ " from approvalrolevalidationdetail acv,approvalconfigrole acr where acv.napprovalconfigrolecode = acr.napprovalconfigrolecode and acr.napproveconfversioncode = "
						+ napprovalConfigVersionCode + " and acv.nsitecode = " + userInfo.getNmastersitecode()
						+ " and acr.nsitecode = " + userInfo.getNmastersitecode() + ";";
				List<ApprovalRoleValidationDetail> lstApprovalRoleValidationDetail = jdbcTemplate.query(str,
						new ApprovalRoleValidationDetail());

				str = "select acv.napprovaldecisioncode, acv.napprovalconfigrolecode, acv.napprovalconfigcode, acv.nuserrolecode, acv.nlevelno, acv.ndefaultstatus, acv.ntransactionstatus, acv.dmodifieddate, acv.nsitecode, acv.nstatus"
						+ " from approvalroledecisiondetail acv,approvalconfigrole acr where  acv.napprovalconfigrolecode = acr.napprovalconfigrolecode and acr.napproveconfversioncode = "
						+ napprovalConfigVersionCode + " and acv.nsitecode = " + userInfo.getNmastersitecode()
						+ " and acr.nsitecode = " + userInfo.getNmastersitecode() + ";";
				List<ApprovalRoleDecisionDetail> lstApprovalRoleDecisionDetail = jdbcTemplate.query(str,
						new ApprovalRoleDecisionDetail());

				List<Object> lstobj = new ArrayList<Object>();
				lstobj.add(lstApprovalConfigVersion);
				lstobj.add(lstApprovalConfigAutoapproval);
				lstobj.add(lstApprovalConfigRole);
				lstobj.add(lstApprovalRoleFilterDetail);
				lstobj.add(lstApprovalRoleValidationDetail);
				lstobj.add(lstApprovalRoleDecisionDetail);

				List<String> lstActiontype = new ArrayList<>();
				lstActiontype.add("IDS_COPYVERSION");
				lstActiontype.add("IDS_COPYVERSION");
				lstActiontype.add("IDS_COPYVERSIONROLE");
				lstActiontype.add("IDS_COPYFILTERSTATUS");
				lstActiontype.add("IDS_COPYVALIDATIONSTATUS");
				lstActiontype.add("IDS_COPYDECISIONSTATUS");
				auditUtilityFunction.fnInsertListAuditAction(lstobj, 1, null, lstActiontype, userInfo);
				Map<String, Object> getMap = new HashMap<String, Object>();
				getMap.put("napprovalconfigcode", objMap.get("napprovalconfigcode"));
				getMap.put("napprovalsubtypecode", objMap.get("napprovalsubtypecode"));
				getMap.put("ntreeversiontempcode", objMap.get("ntreeversiontempcode"));
				return getApprovalConfigVersion(getMap, userInfo);

			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TREEVERSIONTEMPLATEMISSMATCH",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getCopyRegType(final int napprovalconfigversioncode, final int napprovalsubtypecode,
			final UserInfo userInfo) throws Exception {
		Map<String, Object> rtnMap = new HashMap<>();
		ApprovalConfigAutoapproval objApprovalConfigVersion = getApprovalConfigVersionByID(napprovalconfigversioncode,
				userInfo);

		if (objApprovalConfigVersion != null) {
			String str = "select distinct rt.nregtypecode,coalesce(rt.jsondata->'sregtypename'->>'"
					+ userInfo.getSlanguagetypecode()
					+ "',rt.jsondata->'sregtypename'->>'en-US') as sregtypename,rt.nsorter "
					+ "from treetemplatetransactionrole ttt,treeversiontemplate tvt,approvalconfig ac,registrationtype rt "
					+ "where ttt.ntreeversiontempcode = tvt.ntreeversiontempcode " + " and rt.nsitecode = "
					+ userInfo.getNmastersitecode() + " and ac.napprovalconfigcode = ttt.napprovalconfigcode "
					+ " and tvt.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
					+ " and rt.nregtypecode = ac.nregtypecode " + " and ac.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tvt.nsitecode = "
					+ userInfo.getNmastersitecode() + " and ttt.nsitecode = " + userInfo.getNmastersitecode()
					+ " and ac.nsitecode = " + userInfo.getNmastersitecode() + " and rt.nregtypecode > 0"
					+ " and ac.napprovalsubtypecode = " + napprovalsubtypecode + "";

			List<RegistrationType> lstRegType = jdbcTemplate.query(str, new RegistrationType());

			rtnMap.put("CopyRegType", lstRegType);
			if (!lstRegType.isEmpty()) {
				rtnMap.putAll((Map<String, Object>) getCopyRegSubType(lstRegType.get(0).getNregtypecode(), userInfo)
						.getBody());
			}
			return new ResponseEntity<Object>(rtnMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}

	@Override
	public ResponseEntity<Object> getCopyRegSubType(int nregTypeCode, UserInfo userInfo) throws Exception {
		Map<String, Object> rtnMap = new HashMap<String, Object>();

		String str = "select distinct rst.nregsubtypecode, rst.nregtypecode, rst.jsondata, rst.dmodifieddate, rst.nsorter, rst.nsitecode, rst.nstatus, coalesce(rst.jsondata->'sregsubtypename'->>'"
				+ userInfo.getSlanguagetypecode() + "',rst.jsondata->'sregsubtypename'->>'en-US') as sregsubtypename "
				+ " from treetemplatetransactionrole ttt,treeversiontemplate tvt,approvalconfig ac,"
				+ " registrationsubtype rst " + " where ttt.ntreeversiontempcode = tvt.ntreeversiontempcode "
				+ " and ac.napprovalconfigcode = ttt.napprovalconfigcode and rst.nsitecode  = "
				+ userInfo.getNmastersitecode() + " and tvt.ntransactionstatus = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and rst.nregsubtypecode = ac.nregsubtypecode and ac.nregtypecode = " + nregTypeCode
				+ " and ac.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rst.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tvt.nsitecode = " + userInfo.getNmastersitecode() + " and ttt.nsitecode = "
				+ userInfo.getNmastersitecode() + " and ac.nsitecode = " + userInfo.getNmastersitecode()
				+ " and rst.nregsubtypecode >0 order by rst.nregsubtypecode desc";

		List<RegistrationSubType> lstRegSubType = jdbcTemplate.query(str,
				new RegistrationSubType());

		rtnMap.put("CopyRegSubType", lstRegSubType);
		return new ResponseEntity<Object>(rtnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> setDefaultFilterStatus(Map<String, Object> objMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(objMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		int napprovalConfigVersionCode = (int) objMap.get("napprovalconfigversioncode");
		ApprovalConfigAutoapproval objApprovalConfigVersion = getApprovalConfigVersionByID(napprovalConfigVersionCode,
				userInfo);
		if (objApprovalConfigVersion == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		if (objApprovalConfigVersion.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT
				.gettransactionstatus()) {
			List<String> multiLingualList = new ArrayList<>();
			List<Object> lstNewObject = new ArrayList<>();
			List<Object> lstOldObject = new ArrayList<>();

			String str = "select napprovalfiltercode, napprovalconfigrolecode, napprovalconfigcode, nuserrolecode, nlevelno, ndefaultstatus, ntransactionstatus, dmodifieddate, nsitecode, nstatus"
					+ " from approvalrolefilterdetail where ndefaultstatus = "
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and napprovalconfigrolecode = "
					+ objMap.get("napprovalconfigrolecode") + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
					+ userInfo.getNmastersitecode();
			ApprovalRoleFilterDetail defaultApprovalConfigRoleFilterDetail = (ApprovalRoleFilterDetail) jdbcUtilityFunction
					.queryForObject(str, ApprovalRoleFilterDetail.class, jdbcTemplate);

			str = "select napprovalfiltercode, napprovalconfigrolecode, napprovalconfigcode, nuserrolecode, nlevelno, ndefaultstatus, ntransactionstatus, dmodifieddate, nsitecode, nstatus"
					+ " from approvalrolefilterdetail where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and napprovalfiltercode = "
					+ objMap.get("napprovalfiltercode");
			ApprovalRoleFilterDetail oldApprovalRoleFilterDetail = jdbcTemplate
					.queryForObject(str, new ApprovalRoleFilterDetail());
			str = "";
			if (defaultApprovalConfigRoleFilterDetail != null) {
				str = " update approvalrolefilterdetail set dmodifieddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', ndefaultstatus = "
						+ Enumeration.TransactionStatus.NO.gettransactionstatus() + "" + " where napprovalfiltercode = "
						+ defaultApprovalConfigRoleFilterDetail.getNapprovalfiltercode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				jdbcTemplate.execute(str);
				ApprovalRoleFilterDetail newdefaultApprovalConfigRoleFilterDetail = SerializationUtils
						.clone(defaultApprovalConfigRoleFilterDetail);
				newdefaultApprovalConfigRoleFilterDetail
				.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
				multiLingualList.add("IDS_SETDEFAULT");
				lstNewObject.add(newdefaultApprovalConfigRoleFilterDetail);
				lstOldObject.add(defaultApprovalConfigRoleFilterDetail);
			}
			str = "update approvalrolefilterdetail set dmodifieddate = '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', ndefaultstatus = "
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " where napprovalfiltercode = "
					+ objMap.get("napprovalfiltercode") + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			jdbcTemplate.execute(str);
			multiLingualList.add("IDS_SETDEFAULT");

			ApprovalRoleFilterDetail newApprovalRoleFilterDetail = SerializationUtils
					.clone(oldApprovalRoleFilterDetail);
			newApprovalRoleFilterDetail
			.setNdefaultstatus((short) Enumeration.TransactionStatus.YES.gettransactionstatus());
			lstNewObject.add(newApprovalRoleFilterDetail);
			lstOldObject.add(oldApprovalRoleFilterDetail);
			auditUtilityFunction.fnInsertAuditAction(lstNewObject, 2, lstOldObject, multiLingualList, userInfo);
			return new ResponseEntity<Object>(getApprovalConfigRoleDetails((int) objMap.get("napprovalconfigrolecode"),
					(int) objMap.get("napprovalsubtypecode"), userInfo), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.SELECTDRAFTVERSION.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> setDefaultDecisionStatus(Map<String, Object> objMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(objMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		int napprovalConfigVersionCode = (int) objMap.get("napprovalconfigversioncode");
		ApprovalConfigAutoapproval objApprovalConfigVersion = getApprovalConfigVersionByID(napprovalConfigVersionCode,
				userInfo);
		if (objApprovalConfigVersion == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		if (objApprovalConfigVersion.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT
				.gettransactionstatus()) {
			List<String> multiLingualList = new ArrayList<>();
			List<Object> lstNewObject = new ArrayList<>();
			List<Object> lstOldObject = new ArrayList<>();
			String str = "select napprovaldecisioncode, napprovalconfigrolecode, napprovalconfigcode, nuserrolecode, nlevelno, ndefaultstatus, ntransactionstatus, dmodifieddate, nsitecode, nstatus"
					+ " from approvalroledecisiondetail where ndefaultstatus = "
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and napprovalconfigrolecode = "
					+ objMap.get("napprovalconfigrolecode") + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
					+ userInfo.getNmastersitecode() + ";";
			List<ApprovalRoleDecisionDetail> lstApprovalRoleDecisionDetail = jdbcTemplate.query(str,
					new ApprovalRoleDecisionDetail());

			str = "select napprovaldecisioncode, napprovalconfigrolecode, napprovalconfigcode, nuserrolecode, nlevelno, ndefaultstatus, ntransactionstatus, dmodifieddate, nsitecode, nstatus"
					+ " from approvalroledecisiondetail where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and napprovaldecisioncode = "
					+ objMap.get("napprovaldecisioncode") + ";";
			List<ApprovalRoleDecisionDetail> lstApprovalRoleDecisionDetail1 = jdbcTemplate.query(str,
					new ApprovalRoleDecisionDetail());

			str = "select napprovalconfigrolecode, napprovalconfigcode, ntreeversiontempcode, nuserrolecode, nchecklistversioncode, npartialapprovalneed, nsectionwiseapprovalneed, nrecomretestneed,"
					+ " nrecomrecalcneed, nretestneed, nrecalcneed, nlevelno, napprovalstatuscode, napproveconfversioncode, nautoapproval, nautoapprovalstatuscode, nautodescisionstatuscode,"
					+ " ncorrectionneed, nesignneed, ntransactionstatus, dmodifieddate, nsitecode, nstatus"
					+ " from approvalconfigrole where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and napprovalconfigrolecode = "
					+ (int) objMap.get("napprovalconfigrolecode") + ";";
			List<ApprovalConfigRole> lstApprovalConfigRole = jdbcTemplate.query(str, new ApprovalConfigRole());

			str = "select nautoapprovalcode, napprovalconfigcode, napprovalconfigversioncode, sversionname, nneedautocomplete, nneedautoapproval, nautodescisionstatus, nautoapprovalstatus,"
					+ " nautoallot, nneedjoballocation, nneedautoinnerband, nneedautoouterband, dmodifieddate, nsitecode, nstatus"
					+ " from approvalconfigautoapproval where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and napprovalconfigversioncode = "
					+ (int) objMap.get("napprovalconfigversioncode") + " and nsitecode = "
					+ userInfo.getNmastersitecode() + ";";
			List<ApprovalConfigAutoapproval> lstApprovalConfigAutoapproval = jdbcTemplate.query(str,
					new ApprovalConfigAutoapproval());

			List<ApprovalRoleDecisionDetail> defaultRoleDecisionDetailLst = lstApprovalRoleDecisionDetail;
			List<ApprovalRoleDecisionDetail> oldApprovalRoleDecisionDetailLst = lstApprovalRoleDecisionDetail1;
			List<ApprovalConfigRole> objApprovalConfigRoleLst = lstApprovalConfigRole;
			List<ApprovalConfigAutoapproval> listApprovalConfigAutoapproval = lstApprovalConfigAutoapproval;

			ApprovalRoleDecisionDetail defaultRoleDecisionDetail = defaultRoleDecisionDetailLst.get(0);
			ApprovalRoleDecisionDetail oldApprovalRoleDecisionDetail = oldApprovalRoleDecisionDetailLst.get(0);
			ApprovalConfigRole objApprovalConfigRole = objApprovalConfigRoleLst.get(0);
			ApprovalConfigAutoapproval objApprovalConfigAutoapproval = listApprovalConfigAutoapproval.get(0);
			str = "";
			if (defaultRoleDecisionDetail != null) {
				str = "update approvalroledecisiondetail set dmodifieddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', ndefaultstatus = "
						+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " where napprovaldecisioncode = "
						+ defaultRoleDecisionDetail.getNapprovaldecisioncode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				jdbcTemplate.execute(str);

				ApprovalRoleDecisionDetail newdefaultRoleDecisionDetail = SerializationUtils
						.clone(defaultRoleDecisionDetail);
				newdefaultRoleDecisionDetail
				.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
				lstNewObject.add(newdefaultRoleDecisionDetail);
				lstOldObject.add(defaultRoleDecisionDetail);
				multiLingualList.add("IDS_SETDEFAULT");
			}
			str = "update approvalroledecisiondetail set dmodifieddate = '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', ndefaultstatus = "
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " where napprovaldecisioncode = "
					+ objMap.get("napprovaldecisioncode") + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			jdbcTemplate.execute(str);
			if (objApprovalConfigRole.getNlevelno() == 1) {
				str = " update approvalconfigautoapproval set dmodifieddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nautodescisionstatus = "
						+ (int) objMap.get("ntransactionstatus") + " where napprovalconfigversioncode = "
						+ (int) objMap.get("napprovalconfigversioncode") + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
						+ userInfo.getNmastersitecode() + ";";
				jdbcTemplate.execute(str);
				if (objApprovalConfigAutoapproval.getNneedautoapproval() == Enumeration.TransactionStatus.NO
						.gettransactionstatus()) {
					str = " update approvalconfigrole set nautodescisionstatuscode = "
							+ (int) objMap.get("ntransactionstatus") + " where napprovalconfigrolecode = "
							+ (int) objMap.get("napprovalconfigrolecode") + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
					jdbcTemplate.execute(str);
				} else {
					str = " update approvalconfigrole set dmodifieddate = '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nautodescisionstatuscode = "
							+ (int) objMap.get("ntransactionstatus") + " where napproveconfversioncode = "
							+ (int) objMap.get("napprovalconfigversioncode") + " and nsitecode = "
							+ userInfo.getNmastersitecode() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
					jdbcTemplate.execute(str);
				}
			} else {
				if (objApprovalConfigAutoapproval.getNneedautoapproval() == Enumeration.TransactionStatus.NO
						.gettransactionstatus()) {
					str = " update approvalconfigrole set dmodifieddate = '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nautodescisionstatuscode = "
							+ (int) objMap.get("ntransactionstatus") + " where napprovalconfigrolecode = "
							+ (int) objMap.get("napprovalconfigrolecode") + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
					jdbcTemplate.execute(str);
				}
			}
			str = "select napprovalconfigrolecode, napprovalconfigcode, ntreeversiontempcode, nuserrolecode, nchecklistversioncode,"
					+ " npartialapprovalneed, nsectionwiseapprovalneed, nrecomretestneed, nrecomrecalcneed, nretestneed, nrecalcneed,"
					+ " nlevelno, napprovalstatuscode, napproveconfversioncode, nautoapproval, nautoapprovalstatuscode, nautodescisionstatuscode,"
					+ " ncorrectionneed, nesignneed, ntransactionstatus, dmodifieddate, nsitecode, nstatus"
					+ " from approvalconfigrole where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and napprovalconfigrolecode = "
					+ (int) objMap.get("napprovalconfigrolecode") + ";";
			multiLingualList.add("IDS_SETDEFAULT");
			multiLingualList.add("IDS_SETAUTODECISIONSTATUS");
			ApprovalConfigRole newApprovalConfigRole = jdbcTemplate.queryForObject(str,
					new ApprovalConfigRole());
			lstNewObject.add(newApprovalConfigRole);
			lstOldObject.add(objApprovalConfigRole);
			ApprovalRoleDecisionDetail newApprovalRoleDecisionDetail = SerializationUtils
					.clone(oldApprovalRoleDecisionDetail);
			newApprovalRoleDecisionDetail
			.setNdefaultstatus((short) Enumeration.TransactionStatus.YES.gettransactionstatus());
			lstNewObject.add(newApprovalRoleDecisionDetail);
			lstOldObject.add(oldApprovalRoleDecisionDetail);
			auditUtilityFunction.fnInsertAuditAction(lstNewObject, 2, lstOldObject, multiLingualList, userInfo);

			return new ResponseEntity<Object>(getApprovalConfigRoleDetails((int) objMap.get("napprovalconfigrolecode"),
					(int) objMap.get("napprovalsubtypecode"), userInfo), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.SELECTDRAFTVERSION.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> getDesignTemplateMapping(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		Map<String, Object> returnMap = new HashMap<>();
		final int approvalSubTypeCode = (int) inputMap.get("napprovalsubtypecode");
		ApprovalConfigAutoapproval objApprovalConfigVersion = getApprovalConfigVersionByID(
				(int) inputMap.get("napprovalconfigversioncode"), userInfo);
		if (objApprovalConfigVersion != null) {
			if (approvalSubTypeCode == Enumeration.ApprovalSubType.PROTOCOLAPPROVAL.getnsubtype()) {
				final String getRegistrationTemplate = " select "
						+ " dt.ndesigntemplatemappingcode, dt.nsampletypecode, dt.nformcode, dt.nregtypecode, dt.nregsubtypecode, dt.nregsubtypeversioncode, dt.nformwisetypecode, dt.nreactregtemplatecode, dt.nsubsampletemplatecode, dt.ntransactionstatus, dt.nversionno, dt.dmodifieddate, dt.nsitecode, dt.nstatus,"
						+ " rt.nreactregtemplatecode, rt.nsampletypecode, rt.nsubsampletypecode, rt.ndefaulttemplatecode, rt.ntransactionstatus, rt.sregtemplatename, rt.jsondata, rt.dmodifieddate, rt.nsitecode, rt.nstatus, rt.stemplatetypesname,"
						+ "  case when dt.nversionno >0 then dt.nversionno::character varying else '-' end as sversionno "
						+ " from reactregistrationtemplate rt "
						+ " join designtemplatemapping dt on dt.nreactregtemplatecode = rt.nreactregtemplatecode "
						+ " where " + " dt.nregtypecode = " + Enumeration.TransactionStatus.NA.gettransactionstatus()
						+ " and dt.nregsubtypecode = " + Enumeration.TransactionStatus.NA.gettransactionstatus() + " "
						+ " and dt.nsampletypecode = " + Enumeration.SampleType.PROTOCOL.getType()
						+ " and rt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " and dt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " and rt.nsitecode = " + userInfo.getNmastersitecode() + " and dt.nsitecode = "
						+ userInfo.getNmastersitecode() + " and dt.ntransactionstatus = "
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
						+ " order by ndesigntemplatemappingcode;";
				final List<DesignTemplateMapping> designTemplateMapping = jdbcTemplate.query(getRegistrationTemplate,
						new DesignTemplateMapping());
				returnMap.put("DesignTemplateMapping", designTemplateMapping);

			} else {
				final int nregtypecode = (int) inputMap.get("nregtypecode");
				final int nregsubtypecode = (int) inputMap.get("nregsubtypecode");

				final String getRegistrationTemplate = "select"
						+ "  dt.ndesigntemplatemappingcode, dt.nsampletypecode, dt.nformcode, dt.nregtypecode, dt.nregsubtypecode, dt.nregsubtypeversioncode, dt.nformwisetypecode, dt.nreactregtemplatecode, dt.nsubsampletemplatecode, dt.ntransactionstatus, dt.nversionno, dt.dmodifieddate, dt.nsitecode, dt.nstatus,"
						+ " rt.nreactregtemplatecode, rt.nsampletypecode, rt.nsubsampletypecode, rt.ndefaulttemplatecode, rt.ntransactionstatus, rt.sregtemplatename, rt.jsondata, rt.dmodifieddate, rt.nsitecode, rt.nstatus, rt.stemplatetypesname,"
						+ "  case when dt.nversionno >0 then dt.nversionno::character varying else '-' end as sversionno "
						+ " from reactregistrationtemplate rt,designtemplatemapping dt" + " where  rt.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and dt.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and dt.nregtypecode = "
						+ nregtypecode + " and dt.nregsubtypecode = " + nregsubtypecode
						+ " and dt.nreactregtemplatecode = rt.nreactregtemplatecode " + " and dt.ntransactionstatus = "
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and dt.nsitecode = "
						+ userInfo.getNmastersitecode() + " and rt.nsitecode = " + userInfo.getNmastersitecode()
						+ " order by ndesigntemplatemappingcode;";
				final List<DesignTemplateMapping> designTemplateMapping = jdbcTemplate.query(getRegistrationTemplate,
						new DesignTemplateMapping());
				returnMap.put("DesignTemplateMapping", designTemplateMapping);
				if (!designTemplateMapping.isEmpty()) {
					final String regsubtype = "select cast(rg.jsondata->>'nneedsubsample' as boolean) nneedsubsample,"
							+ " dm.ndesigntemplatemappingcode, dm.nsampletypecode, dm.nformcode, dm.nregtypecode, dm.nregsubtypecode, dm.nregsubtypeversioncode, dm.nformwisetypecode, dm.nreactregtemplatecode, dm.nsubsampletemplatecode, dm.ntransactionstatus, dm.nversionno, dm.dmodifieddate, dm.nsitecode, dm.nstatus, "
							+ " rt.sregtemplatename " + "from regsubtypeconfigversion rg,"
							+ "designtemplatemapping dm ,reactregistrationtemplate rt "
							+ " where  dm.nregsubtypecode = " + nregsubtypecode + ""
							+ " and dm.ndesigntemplatemappingcode = "
							+ designTemplateMapping.get(0).getNdesigntemplatemappingcode() + "" + " and rg.nsitecode = "
							+ userInfo.getNmastersitecode()
							+ " and rg.nregsubtypeversioncode = dm.nregsubtypeversioncode  and rt.nreactregtemplatecode = dm.nreactregtemplatecode";
					DesignTemplateMapping regSubType = (DesignTemplateMapping) jdbcUtilityFunction
							.queryForObject(regsubtype, DesignTemplateMapping.class, jdbcTemplate);

					if (regSubType.isNneedsubsample()) {
						final String subSampleTemplatequery = "select r.nreactregtemplatecode, r.nsampletypecode, r.nsubsampletypecode, r.ndefaulttemplatecode, r.ntransactionstatus, r.sregtemplatename, r.jsondata, r.dmodifieddate, r.nsitecode, r.nstatus, r.stemplatetypesname from reactregistrationtemplate r,designtemplatemapping d"
								+ " where d.ndesigntemplatemappingcode = " + regSubType.getNdesigntemplatemappingcode()
								+ " and r.nreactregtemplatecode = d.nsubsampletemplatecode";
						ReactRegistrationTemplate objReactRegistrationTemplate = (ReactRegistrationTemplate) jdbcUtilityFunction
								.queryForObject(subSampleTemplatequery, ReactRegistrationTemplate.class, jdbcTemplate);
						returnMap.put("subSampleTemplate", objReactRegistrationTemplate);
						returnMap.put("subSampleCheck", regSubType.isNneedsubsample());
					} else {
						returnMap.put("subSampleTemplate", null);
						returnMap.put("subSampleCheck", false);
					}

				} else {
					returnMap.put("subSampleTemplate", null);
					returnMap.put("subSampleCheck", false);
				}

			}

			return new ResponseEntity<>(returnMap, HttpStatus.OK);

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}

	public void createView(String viewName, String templateName, int ndesigntemplatemappingcode, int formcode,
			boolean isDropView) {
		String createView = "";

		if (isDropView) {
			createView = "drop view if exists view_" + viewName + ";";
		}
		createView = createView + "CREATE OR REPLACE VIEW public.view_" + viewName
				+ " AS SELECT  d.sprotocolid,d.jsonuidata, d.nprotocolversioncode,d.nprotocolcode,d.ndesigntemplatemappingcode,d.nstatus ,r.* FROM   view_protocol d,"
				+ "   jsonb_populate_record(NULL::type_" + templateName
				+ ", d.jsonuidata) r where d.ndesigntemplatemappingcode = " + ndesigntemplatemappingcode + "";

		jdbcTemplate.execute(createView);
	}

}
