package com.agaramtech.qualis.stability.service.protocol;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.basemaster.model.TransactionStatus;
import com.agaramtech.qualis.configuration.model.ApprovalConfigAutoapproval;
import com.agaramtech.qualis.configuration.model.ApprovalConfigRole;
import com.agaramtech.qualis.configuration.model.ApprovalRoleActionDetail;
import com.agaramtech.qualis.configuration.model.ApprovalRoleFilterDetail;
import com.agaramtech.qualis.configuration.model.ApprovalRoleValidationDetail;
import com.agaramtech.qualis.configuration.model.DesignTemplateMapping;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.LinkMaster;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.product.model.Product;
import com.agaramtech.qualis.product.model.ProductCategory;
import com.agaramtech.qualis.stability.model.Protocol;
import com.agaramtech.qualis.stability.model.ProtocolFile;
import com.agaramtech.qualis.stability.model.ProtocolHistory;
import com.agaramtech.qualis.stability.model.ProtocolVersion;
import com.agaramtech.qualis.stability.model.SeqNoStability;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class ProtocolDAOImpl implements ProtocolDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final FTPUtilityFunction ftpUtilityFunction;

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getProtocol(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();
		LOGGER.info("Protocol Initial Get Start");
		int versionCode = -1;
		final String configVersionQuery = "select "
				+ "acaa.sversionname,acaa.napprovalconfigcode,acaa.napprovalconfigversioncode, "
				+ "acv.ntransactionstatus,acv.ntreeversiontempcode " 
				+ "from approvalconfig ac "
				+ "join approvalconfigversion acv on ac.napprovalconfigcode=acv.napprovalconfigcode "
				+ "join approvalconfigautoapproval acaa on acaa.napprovalconfigcode=acv.napprovalconfigcode "
				+ "and acaa.napprovalconfigversioncode = acv.napproveconfversioncode " + "where napprovalsubtypecode ="
				+ Enumeration.ApprovalSubType.PROTOCOLAPPROVAL.getnsubtype() + " " + "and acv.ntransactionstatus in ("
				+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ") " + "and ac.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and acv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acaa.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and acv.nsitecode ="
				+ userInfo.getNmastersitecode() + " order by acaa.napprovalconfigversioncode ";

		final List<ApprovalConfigAutoapproval> lstApprovalConfigVersion = jdbcTemplate.query(configVersionQuery,
				new ApprovalConfigAutoapproval());
		if (!lstApprovalConfigVersion.isEmpty()) {
			map.put("configVersion", lstApprovalConfigVersion);
			final List<ApprovalConfigAutoapproval> approvedApprovalVersionList = lstApprovalConfigVersion.stream()
					.filter(obj -> obj.getNtransactionstatus() == Enumeration.TransactionStatus.APPROVED
							.gettransactionstatus())
					.collect(Collectors.toList());
			if (approvedApprovalVersionList != null && !approvedApprovalVersionList.isEmpty()) {
				map.put("realApprovalVersionValue", approvedApprovalVersionList.get(0));
				map.put("defaultApprovalVersionValue", approvedApprovalVersionList.get(0));
				versionCode = approvedApprovalVersionList.get(0).getNapprovalconfigversioncode();

			} else {
				map.put("realApprovalVersionValue", lstApprovalConfigVersion.get(0));
				map.put("defaultApprovalVersionValue", lstApprovalConfigVersion.get(0));
				versionCode = lstApprovalConfigVersion.get(0).getNapprovalconfigversioncode();
			}
			// Added by sonia on 12th Feb 2025 for jira id :ALPD-5404
			inputMap.put("napprovalconfigversioncode", versionCode);
			map.putAll((Map<String, Object>) getProtocolTemplateList(inputMap, userInfo).getBody());
			map.putAll((Map<String, Object>) getStatus(inputMap, userInfo).getBody());
			map.putAll((Map<String, Object>) getActionStatus(inputMap, userInfo).getBody());
			map.putAll((Map<String, Object>) getProtocolData(inputMap, userInfo).getBody());
		} else {
			map.put("realApprovalVersionValue", lstApprovalConfigVersion);
			map.put("defaultApprovalVersionValue", lstApprovalConfigVersion);
		}

		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	public ResponseEntity<Object> getProtocolTemplateList(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();

		final String templateQuery = "select dm.ndesigntemplatemappingcode,rt.jsondata,dm.ntransactionstatus, "
				+ "CONCAT(rt.sregtemplatename,'(',cast(dm.nversionno as character varying),')') sregtemplatename  "
				+ "from designtemplatemapping dm  "
				+ "join reactregistrationtemplate rt on  rt.nreactregtemplatecode=dm.nreactregtemplatecode "
				+ "join approvalconfigversion acv on acv.ndesigntemplatemappingcode=dm.ndesigntemplatemappingcode "
				+ "where   " + "dm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and rt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and acv.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and dm.nsitecode=" + userInfo.getNmastersitecode() + " and acv.napproveconfversioncode="
				+ inputMap.get("napprovalconfigversioncode") + " " + "order by dm.ndesigntemplatemappingcode desc";
		final List<DesignTemplateMapping> lstReactRegistrationTemplate = jdbcTemplate.query(templateQuery,
				new DesignTemplateMapping());

		if (!lstReactRegistrationTemplate.isEmpty()) {
			map.put("dynamicDesignMapping", lstReactRegistrationTemplate);
			if (!inputMap.containsKey("nFilterFlag")) {
				map.put("realDesignTemplateMappingValue", lstReactRegistrationTemplate.get(0));
			}
			map.put("defaultDesignTemplateMappingValue", lstReactRegistrationTemplate.get(0));
			map.put("dynamicDesign", projectDAOSupport.getTemplateDesign(
					lstReactRegistrationTemplate.get(0).getNdesigntemplatemappingcode(), userInfo.getNformcode()));
			inputMap.put("ndesigntemplatemappingcode",
					lstReactRegistrationTemplate.get(0).getNdesigntemplatemappingcode());
		} else {
			map.put("realDesignTemplateMappingValue", lstReactRegistrationTemplate);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	public ResponseEntity<Object> getStatus(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();
		String strConditionQuery = "";
		String unionConditionQuery = " union all " + " select  " + " ntranscode as ntransactionstatus, "
				+ " coalesce(jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " jsondata->'stransdisplaystatus'->>'en-US') as sfilterstatus " + " from transactionstatus where "
				+ " ntranscode in (" + Enumeration.TransactionStatus.ALL.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ") " + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";

		final String str = "select * from approvalconfigrole where napproveconfversioncode ="
				+ inputMap.get("napprovalconfigversioncode") + "  " + "and nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="+userInfo.getNmastersitecode()+" ";

		final List<ApprovalConfigRole> lstRole = jdbcTemplate.query(str, new ApprovalConfigRole());


		for (int i = 0; i < lstRole.size(); i++) {
			int nRoleCode = lstRole.get(i).getNuserrolecode();
			if (nRoleCode == userInfo.getNuserrole()) {
				strConditionQuery = "and acr.nuserrolecode =" + userInfo.getNuserrole() + "	";
				unionConditionQuery = "";
			}
		}


		final String filterStatusQuery = "select a.ntransactionstatus,a.sfilterstatus from (select afd.ntransactionstatus,"
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as sfilterstatus  "
				+ " from approvalrolefilterdetail afd "
				+ " join approvalconfig ac on ac.napprovalconfigcode = afd.napprovalconfigcode "
				+ " join transactionstatus ts on ts.ntranscode  = afd.ntransactionstatus "
				+ " join approvalconfigrole acr on acr.napprovalconfigrolecode =afd.napprovalconfigrolecode "
				+ " join approvalconfigversion acv on acv.napproveconfversioncode=acr.napproveconfversioncode "
				+ " where " + " ac.napprovalsubtypecode = " + Enumeration.ApprovalSubType.PROTOCOLAPPROVAL.getnsubtype()
				+ " " + " and acv.ntransactionstatus in ("
				+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ") " + " " + strConditionQuery + " "
				+ " and afd.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and acv.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and acr.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and ac.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and acv.nsitecode="
				+ userInfo.getNmastersitecode() + " and acv.napproveconfversioncode = "
				+ inputMap.get("napprovalconfigversioncode") + " " + " " + unionConditionQuery
				+ ")a group by a.ntransactionstatus,a.sfilterstatus order by a.ntransactionstatus ";

		final List<ApprovalRoleFilterDetail> lstFilterStatus = jdbcTemplate.query(filterStatusQuery,
				new ApprovalRoleFilterDetail());

		if (lstFilterStatus.size() > 0) {
			map.put("status", lstFilterStatus);
			map.put("defaultStatusValue", lstFilterStatus.get(0));
			map.put("realStatusValue", lstFilterStatus.get(0));
			inputMap.put("ntranscode", lstFilterStatus.get(0).getNtransactionstatus());
			inputMap.put("nflag", 1);
		} else {
			map.put("realStatusValue", lstFilterStatus);
			map.put("defaultStatusValue", lstFilterStatus);

		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	private ResponseEntity<Object> getActionStatus(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();
		final String getApprovalParamQry = " select aad.ntransactionstatus,coalesce(ts.jsondata->'sactiondisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'sactiondisplaystatus'->>'en-US') as stransdisplaystatus,acr.nesignneed"
				+ " from approvalroleactiondetail aad "
				+ " join transactionstatus ts on ts.ntranscode=aad.ntransactionstatus "
				+ " join approvalconfigrole acr on acr.napprovalconfigrolecode=aad.napprovalconfigrolecode "
				+ " join approvalconfig ac on ac.napprovalconfigcode=acr.napprovalconfigcode " + " where aad.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and ac.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and acr.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and acr.nuserrolecode="
				+ userInfo.getNuserrole() + " and aad.nsitecode="+userInfo.getNmastersitecode()+" and acr.napproveconfversioncode="
				+ inputMap.get("napprovalconfigversioncode") + " order by aad.ntransactionstatus ";
		final List<ApprovalRoleActionDetail> actionDetailList = (List<ApprovalRoleActionDetail>) jdbcTemplate
				.query(getApprovalParamQry, new ApprovalRoleActionDetail());
		map.put("actionStatus", actionDetailList);
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getProtocolData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> map = new HashMap<String, Object>();
		List<Map<String, Object>> protocolList = new ArrayList<>();
		short ntransactionstatus = 0;
		String filterStatusQuery = "";
		if (inputMap.containsKey("ntranscode")) {
			if (inputMap.containsKey("nflag")) {
				ntransactionstatus = (short) inputMap.get("ntranscode");
			} else {
				Integer transStatusValue = (Integer) inputMap.get("ntranscode");
				ntransactionstatus = transStatusValue.shortValue();
			}
		}
		if (ntransactionstatus > 0) {
			filterStatusQuery = "and sph.ntransactionstatus =" + ntransactionstatus + " ";
		}
		final String getProtocolQuery = "select  " + "json_agg(a.jsonuidata)  from ( " + "select  " + "spv.jsonuidata "
				+ "|| json_build_object('nprotocolcode',sp.nprotocolcode):: jsonb  "
				+ "|| json_build_object('nprotocolversioncode',spv.nprotocolversioncode):: jsonb  "
				+ "|| json_build_object('nprotocolhistorycode',sph.nprotocolhistorycode):: jsonb  "
				+ "|| json_build_object('ntransactionstatus',sph.ntransactionstatus):: jsonb  "
				+ "|| json_build_object('sprotocolid',sp.sprotocolid):: jsonb  "
				+ "|| json_build_object('sversion',spv.sversion):: jsonb  "
				+ "|| json_build_object('scolorhexcode',cm.scolorhexcode):: jsonb  "
				+ "|| json_build_object('stransdisplaystatus',coalesce(ts1.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',ts1.jsondata->'stransdisplaystatus'->>'en-US')):: jsonb  "
				+ "|| json_build_object('jsondata',spv.jsondata):: jsonb  " + "as jsonuidata  " + "from protocol sp "
				+ "join protocolversion spv on spv.nprotocolcode =sp.nprotocolcode "
				+ "join protocolhistory sph on sph.nprotocolversioncode =spv.nprotocolversioncode and sph.nprotocolcode =spv.nprotocolcode "
				+ "join productcategory pc on pc.nproductcatcode =sp.nproductcatcode  "
				+ "join product p on p.nproductcode =sp.nproductcode "
				+ "join transactionstatus ts1 on ts1.ntranscode =sph.ntransactionstatus "
				+ "join formwisestatuscolor fsc on fsc.ntranscode =sph.ntransactionstatus "
				+ "join colormaster cm on cm.ncolorcode =fsc.ncolorcode " + "where  "
				+ "sph.nprotocolhistorycode=any(select max(nprotocolhistorycode)  " + "from protocolhistory sph "
				+ "join protocol sp on sp.nprotocolcode =sph.nprotocolcode " + "where  " + "sp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sph.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and sp.nsitecode="+userInfo.getNmastersitecode()+" "
				+ "group by sp.nprotocolcode,sph.nprotocolversioncode) " + "and spv.ndesigntemplatemappingcode ="
				+ inputMap.get("ndesigntemplatemappingcode") + " and spv.napproveconfversioncode="
				+ inputMap.get("napprovalconfigversioncode") + " " + "" + filterStatusQuery + " "
				+ " and fsc.nformcode =" + userInfo.getNformcode() + " " + "and sp.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and spv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and sph.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and pc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and p.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and ts1.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and fsc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and cm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and sp.nsitecode ="
				+ userInfo.getNmastersitecode() + " " + "and sp.nprotocolcode >0 order by sp.nprotocolcode asc "
				+ ")a ";
		final String protocol = jdbcTemplate.queryForObject(getProtocolQuery, String.class);
		if (protocol != null) {
			protocolList = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(
					protocol, userInfo, true, (int) inputMap.get("ndesigntemplatemappingcode"), "protocol");
		}
		if (!protocolList.isEmpty()) {
			map.put("protocol", protocolList);
			map.put("selectedProtocol", Arrays.asList(protocolList.get(protocolList.size() - 1)));
			inputMap.put("nprotocolcode",
					Arrays.asList(protocolList.get(protocolList.size() - 1)).get(0).get("nprotocolcode"));
			map.putAll((Map<String, Object>) getProtocolHistory(inputMap, userInfo).getBody());
			map.putAll((Map<String, Object>) getProtocolFile(inputMap, userInfo).getBody());
		} else {
			map.put("protocol", protocolList);
			map.put("selectedProtocol", protocolList);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	private ResponseEntity<Object> getProtocolHistory(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();

		final String strQuery = "select sp.nprotocolcode,sp.sprotocolname,sp.sprotocolid, "
				+ "sph.ntransactionstatus,sph.dmodifieddate, "
				+ "sph.nuserrolecode ,sph.nusercode,ur.suserrolename,u.sfirstname ||' '|| u.slastname as susername,"
				+ "COALESCE(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "',ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,"
				+ "COALESCE(TO_CHAR(sph.dmodifieddate,'" + userInfo.getSpgsitedatetime() + "'),'-') as smodifieddate "
				+ "from protocol sp " + "join protocolversion spv on spv.nprotocolcode =sp.nprotocolcode "
				+ "join protocolhistory sph on sph.nprotocolversioncode =spv.nprotocolversioncode and sph.nprotocolcode =spv.nprotocolcode "
				+ "join transactionstatus ts on ts.ntranscode= sph.ntransactionstatus "
				+ "join userrole ur on ur.nuserrolecode =sph.nuserrolecode "
				+ "join users u on u.nusercode =sph.nusercode " + "where " + "spv.ndesigntemplatemappingcode ="
				+ inputMap.get("ndesigntemplatemappingcode") + " and spv.napproveconfversioncode="
				+ inputMap.get("napprovalconfigversioncode") + " " + "and sp.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and spv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and sph.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and ur.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and sp.nsitecode ="
				+ userInfo.getNmastersitecode() + " and sp.nprotocolcode=" + inputMap.get("nprotocolcode")
				+ " order by sph.dmodifieddate ";
		final List<Protocol> lstProtocol = (List<Protocol>) jdbcTemplate.query(strQuery, new Protocol());
		if (lstProtocol.size() > 0) {
			map.put("protocolHistory", lstProtocol);
		} else {
			map.put("protocolHistory", null);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);

	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getActiveProtocolById(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();
		List<Map<String, Object>> protocolList = new ArrayList<>();
		Integer ntransactionstatus = 0;
		String filterStatusQuery = "";
		if (inputMap.containsKey("ntranscode")) {
			ntransactionstatus = (Integer) inputMap.get("ntranscode");
		}
		if (ntransactionstatus != 0) {
			filterStatusQuery = "and sph.ntransactionstatus =" + ntransactionstatus + " ";
		}

		final String getProtocolQuery = "select  " + "json_agg(a.jsonuidata)  from ( " + "select  " + "spv.jsonuidata "
				+ "|| json_build_object('nprotocolcode',sp.nprotocolcode):: jsonb  "
				+ "|| json_build_object('nprotocolversioncode',spv.nprotocolversioncode):: jsonb  "
				+ "|| json_build_object('nprotocolhistorycode',sph.nprotocolhistorycode):: jsonb  "
				+ "|| json_build_object('ntransactionstatus',sph.ntransactionstatus):: jsonb  "
				+ "|| json_build_object('sprotocolid',sp.sprotocolid):: jsonb  "
				+ "|| json_build_object('sversion',spv.sversion):: jsonb  "
				+ "|| json_build_object('scolorhexcode',cm.scolorhexcode):: jsonb  "
				+ "|| json_build_object('stransdisplaystatus',coalesce(ts1.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',ts1.jsondata->'stransdisplaystatus'->>'en-US')):: jsonb  "
				+ "as jsonuidata  " + "from protocol sp "
				+ "join protocolversion spv  on spv.nprotocolcode =sp.nprotocolcode "
				+ "join protocolhistory sph on sph.nprotocolversioncode =spv.nprotocolversioncode and sph.nprotocolcode =spv.nprotocolcode "
				+ "join productcategory pc on pc.nproductcatcode =sp.nproductcatcode "
				+ "join product p on p.nproductcode =sp.nproductcode "
				+ "join transactionstatus ts1 on ts1.ntranscode =sph.ntransactionstatus "
				+ "join formwisestatuscolor fsc on fsc.ntranscode =sph.ntransactionstatus "
				+ "join colormaster cm on cm.ncolorcode =fsc.ncolorcode " + "where  "
				+ "sph.nprotocolhistorycode=any(select max(nprotocolhistorycode)  " + "from protocolhistory sph "
				+ "join protocol sp on sp.nprotocolcode =sph.nprotocolcode " + "where  " + "sp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sph.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sp.nsitecode="+userInfo.getNmastersitecode()+" "
				+ "group by sp.nprotocolcode,sph.nprotocolversioncode) " + "and spv.ndesigntemplatemappingcode ="
				+ inputMap.get("ndesigntemplatemappingcode") + " and spv.napproveconfversioncode="
				+ inputMap.get("napprovalconfigversioncode") + " " + "" + filterStatusQuery + " "
				+ "and fsc.nformcode =" + userInfo.getNformcode() + " " + "and sp.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and spv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and sph.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and pc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and p.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and ts1.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and fsc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and cm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and sp.nsitecode ="
				+ userInfo.getNmastersitecode() + " " + "and sp.nprotocolcode in(" + inputMap.get("nprotocolcode")
				+ ") " + ")a ";
		final String protocol = jdbcTemplate.queryForObject(getProtocolQuery, String.class);
		if (protocol != null) {
			protocolList = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(
					protocol, userInfo, true, (int) inputMap.get("ndesigntemplatemappingcode"), "protocol");
		}
		if (!protocolList.isEmpty()) {
			map.put("selectedProtocol", Arrays.asList(protocolList.get(protocolList.size() - 1)));
			inputMap.put("nprotocolcode",
					Arrays.asList(protocolList.get(protocolList.size() - 1)).get(0).get("nprotocolcode"));
			map.putAll((Map<String, Object>) getProtocolHistory(inputMap, userInfo).getBody());
			map.putAll((Map<String, Object>) getProtocolFile(inputMap, userInfo).getBody());

		} else {
			map.put("selectedProtocol", protocolList);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	public List<TransactionStatus> getEditFilterStatus(final UserInfo userInfo) throws Exception {
		final String filterStatus = "select a.* from (select t.ntranscode as ntransactionstatus,"
				+ " coalesce(t.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "',t.jsondata->'stransdisplaystatus'->>'en-US') stransdisplaystatus "
				+ " from approvalroleactiondetail ard "
				+ " join approvalconfig ac on ac.napprovalconfigcode = ard.napprovalconfigcode "
				+ " join transactionstatus t on t.ntranscode = ard.ntransactionstatus " + " where " + " ard.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and ac.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and t.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ard.nsitecode="+userInfo.getNmastersitecode()+" and ac.nsitecode="+userInfo.getNmastersitecode()+" " + " union all "
				+ " select t.ntranscode as ntransactionstatus ,coalesce(t.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',t.jsondata->'stransdisplaystatus'->>'en-US') stransdisplaystatus " + " from transactionstatus t "
				+ " where t.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ntranscode =" + Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ")a "
				+ " group by a.ntransactionstatus,a.stransdisplaystatus order by a.ntransactionstatus  ";
		return jdbcTemplate.query(filterStatus, new TransactionStatus());
	}

	public Map<String,Object> getEditProtocolDetails(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final String getProtocolQuery = "select  " + "json_agg(spv.jsonuidata || " + "spv.jsondata "
				+ "|| json_build_object('nprotocolcode',sp.nprotocolcode):: jsonb  "
				+ "|| json_build_object('nprotocolversioncode',spv.nprotocolversioncode):: jsonb  "
				+ "|| json_build_object('nprotocolhistorycode',sph.nprotocolhistorycode):: jsonb  "
				+ "|| json_build_object('ntransactionstatus',sph.ntransactionstatus):: jsonb  "
				+ "|| json_build_object('sprotocolid',sp.sprotocolid):: jsonb  "
				+ "|| json_build_object('sversion',spv.sversion):: jsonb  "
				+ "|| json_build_object('stransdisplaystatus',coalesce(ts1.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',ts1.jsondata->'stransdisplaystatus'->>'en-US')):: jsonb )  "
				+ "from protocol sp " + "join protocolversion spv on spv.nprotocolcode =sp.nprotocolcode "
				+ "join protocolhistory sph on sph.nprotocolversioncode =spv.nprotocolversioncode and sph.nprotocolcode =spv.nprotocolcode "
				+ "join productcategory pc on pc.nproductcatcode =sp.nproductcatcode "
				+ "join product p on p.nproductcode =sp.nproductcode "
				+ "join transactionstatus ts1 on ts1.ntranscode =sph.ntransactionstatus " + "where  "
				+ "sph.nprotocolhistorycode=any(select max(nprotocolhistorycode)  " + "from protocolhistory sph "
				+ "join protocol sp on sp.nprotocolcode =sph.nprotocolcode " + "where  " + "sp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sph.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sp.nsitecode="+userInfo.getNmastersitecode()+" "
				+ "group by sp.nprotocolcode,sph.nprotocolversioncode) " + "and sp.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and spv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and sph.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and pc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and p.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and ts1.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and sp.nsitecode ="
				+ userInfo.getNmastersitecode() + " " + "and sp.nprotocolcode in(" + inputMap.get("nprotocolcode")
				+ ") ";

		final String dynamicList = jdbcTemplate.queryForObject(getProtocolQuery, String.class);
		final List<Map<String, Object>> lstData = (List<Map<String, Object>>) projectDAOSupport
				.getSiteLocalTimeFromUTCForDynamicTemplate(dynamicList, userInfo, true,
						(int) inputMap.get("ndesigntemplatemappingcode"), "protocol");
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		if (!lstData.isEmpty()) {
			outputMap.put("EditData", lstData.get(0));
		}
		return outputMap;
	}

	@SuppressWarnings({ "unchecked" })
	private ResponseEntity<Object> editGetProtocolData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();
		List<Map<String, Object>> protocolList = new ArrayList<>();
		short ntransactionstatus = 0;
		String filterStatusQuery = "";
		if (inputMap.containsKey("ntranscode")) {
			if (inputMap.containsKey("nflag")) {
				ntransactionstatus = (short) inputMap.get("ntranscode");
			} else {
				Integer abc = (Integer) inputMap.get("ntranscode");
				ntransactionstatus = abc.shortValue();
			}
		}
		if (ntransactionstatus > 0) {
			filterStatusQuery = "and sph.ntransactionstatus =" + ntransactionstatus + " ";
		}
		final String getProtocolQuery = "select  " + "json_agg(a.jsonuidata)  from ( " + "select  " + "spv.jsonuidata "
				+ "|| json_build_object('nprotocolcode',sp.nprotocolcode):: jsonb  "
				+ "|| json_build_object('nprotocolversioncode',spv.nprotocolversioncode):: jsonb  "
				+ "|| json_build_object('nprotocolhistorycode',sph.nprotocolhistorycode):: jsonb  "
				+ "|| json_build_object('ntransactionstatus',sph.ntransactionstatus):: jsonb  "
				+ "|| json_build_object('sprotocolid',sp.sprotocolid):: jsonb  "
				+ "|| json_build_object('sversion',spv.sversion):: jsonb  "
				+ "|| json_build_object('scolorhexcode',cm.scolorhexcode):: jsonb  "
				+ "|| json_build_object('stransdisplaystatus',coalesce(ts1.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',ts1.jsondata->'stransdisplaystatus'->>'en-US')):: jsonb  "
				+ "|| json_build_object('jsondata',spv.jsondata):: jsonb  " + "as jsonuidata  " + "from protocol sp "
				+ "join protocolversion spv on spv.nprotocolcode =sp.nprotocolcode "
				+ "join protocolhistory sph on sph.nprotocolversioncode =spv.nprotocolversioncode and sph.nprotocolcode =spv.nprotocolcode "
				+ "join productcategory pc on pc.nproductcatcode =sp.nproductcatcode  "
				+ "join product p on p.nproductcode =sp.nproductcode "
				+ "join transactionstatus ts1 on ts1.ntranscode =sph.ntransactionstatus "
				+ "join formwisestatuscolor fsc on fsc.ntranscode =sph.ntransactionstatus "
				+ "join colormaster cm on cm.ncolorcode =fsc.ncolorcode " + "where  "
				+ "sph.nprotocolhistorycode=any(select max(nprotocolhistorycode)  " + "from protocolhistory sph "
				+ "join protocol sp on sp.nprotocolcode =sph.nprotocolcode " + "where  " + "sp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sph.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sp.nsitecode= "+userInfo.getNmastersitecode()+" "
				+ "group by sp.nprotocolcode,sph.nprotocolversioncode) " + "and spv.ndesigntemplatemappingcode ="
				+ inputMap.get("ndesigntemplatemappingcode") + " and spv.napproveconfversioncode="
				+ inputMap.get("napprovalconfigversioncode") + " " + "" + filterStatusQuery + " "
				+ " and fsc.nformcode =" + userInfo.getNformcode() + " " + "and sp.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and spv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and sph.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and pc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and p.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and ts1.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and fsc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and cm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and sp.nsitecode ="
				+ userInfo.getNmastersitecode() + " " + "and sp.nprotocolcode >0 order by sp.nprotocolcode asc "
				+ ")a ";
		final String protocol = (String) jdbcUtilityFunction.queryForObject(getProtocolQuery, String.class,
				jdbcTemplate);
		if (protocol != null) {
			protocolList = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(
					protocol, userInfo, true, (int) inputMap.get("ndesigntemplatemappingcode"), "protocol");
		}
		if (!protocolList.isEmpty()) {
			map.put("protocol", protocolList);
			inputMap.put("nprotocolcode", (int) inputMap.get("nprotocolcode"));
			map.putAll((Map<String, Object>) getActiveProtocolById(inputMap, userInfo).getBody());
		} else {
			map.put("protocol", protocolList);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> createProtocol(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		Map<String, Object> returnMap = new HashMap<String, Object>();
		final JSONObject actionType = new JSONObject();
		final JSONObject jsonAuditObject = new JSONObject();

		final ObjectMapper objmapper = new ObjectMapper();
		final Protocol objProtocol = objmapper.convertValue(inputMap.get("protocol"), Protocol.class);
		final ProtocolVersion objProtocolVersion = objmapper.convertValue(inputMap.get("protocolVersion"),
				ProtocolVersion.class);
		final ProtocolHistory objProtocolHistory = new ProtocolHistory();

		final JSONObject jsonObject = new JSONObject(objProtocolVersion.getJsondata());
		final JSONObject jsonUIObject = new JSONObject(objProtocolVersion.getJsonuidata());
		final List<Map<String, Object>> UniqueValidation = objmapper.convertValue(inputMap.get("combinationUnique"),
				new TypeReference<List<Map<String, Object>>>() {
				});

		final Map<String, Object> map1 = projectDAOSupport.validateUniqueConstraint(UniqueValidation,
				(Map<String, Object>) inputMap.get("protocolVersion"), userInfo, "create", ProtocolVersion.class,
				"nprotocolversioncode", false);
		if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
				.equals(map1.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
			return new ResponseEntity<>(map1.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final String strQuery = "lock table lockprotocol " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus()
					+ " ";
			jdbcTemplate.execute(strQuery);

			final String strSelectSeqno = "select stablename,nsequenceno from seqnostability  where stablename "
					+ "in ('protocol','protocolversion','protocolhistory')";

			final List<?> lstMultiSeqNo = projectDAOSupport.getMultipleEntitiesResultSetInList(strSelectSeqno,
					jdbcTemplate, SeqNoStability.class);

			final List<SeqNoStability> lstSeqNoStab = (List<SeqNoStability>) lstMultiSeqNo.get(0);

			returnMap = lstSeqNoStab.stream().collect(
					Collectors.toMap(SeqNoStability::getStablename, SeqNoStability -> SeqNoStability.getNsequenceno()));

			int protocolSeqNo = (int) returnMap.get("protocol") + 1;
			int protocolVersionSeqNo = (int) returnMap.get("protocolversion") + 1;
			int protocolHistorySeqNo = (int) returnMap.get("protocolhistory") + 1;

			String protocolInsert = " Insert into protocol(nprotocolcode,nproductcatcode,nproductcode,sprotocolid,sprotocolname,dmodifieddate,nsitecode,nstatus)"
					+ " values(" + protocolSeqNo + "," + objProtocol.getNproductcatcode() + ","
					+ objProtocol.getNproductcode() + "," + " N'"
					+ stringUtilityFunction.replaceQuote(objProtocol.getSprotocolid()) + "', " + " N'"
					+ stringUtilityFunction.replaceQuote(objProtocolVersion.getSprotocolname()) + "'," + " '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " );";

			protocolInsert += "Insert into protocolversion (nprotocolversioncode,nprotocolcode,ndesigntemplatemappingcode,napproveconfversioncode,sversion,jsondata,jsonuidata,"
					+ "ntransactionstatus,dmodifieddate,nsitecode,nstatus)" + "values(" + protocolVersionSeqNo + ","
					+ protocolSeqNo + "," + objProtocolVersion.getNdesigntemplatemappingcode() + ","
					+ objProtocolVersion.getNapproveconfversioncode() + "," + "N'"
					+ stringUtilityFunction.replaceQuote(objProtocolVersion.getSversion()) + "','"
					+ stringUtilityFunction.replaceQuote(jsonObject.toString()) + "'::JSONB,'"
					+ stringUtilityFunction.replaceQuote(jsonUIObject.toString()) + "'::JSONB," + ""
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + "," + "'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " );";

			protocolInsert += "Insert into protocolhistory (nprotocolhistorycode,nprotocolversioncode,nprotocolcode,ntransactionstatus,nuserrolecode,nusercode,ndeputyuserrolecode,ndeputyusercode,dmodifieddate,nsitecode,nstatus)"
					+ "values(" + protocolHistorySeqNo + "," + protocolVersionSeqNo + "," + protocolSeqNo + ","
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + "," + "" + userInfo.getNuserrole()
					+ "," + userInfo.getNusercode() + "," + userInfo.getNdeputyuserrole() + ","
					+ userInfo.getNdeputyusercode() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
					+ userInfo.getNmastersitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " );";

			protocolInsert += "update seqnostability set nsequenceno = " + protocolSeqNo
					+ " where stablename='protocol';" + "update seqnostability set nsequenceno = "
					+ protocolVersionSeqNo + " where stablename='protocolversion';"
					+ "update seqnostability set nsequenceno = " + protocolHistorySeqNo
					+ " where stablename='protocolhistory'";
			jdbcTemplate.execute(protocolInsert);

			objProtocol.setNprotocolcode(protocolSeqNo);
			objProtocolVersion.setNprotocolversioncode(protocolVersionSeqNo);
			objProtocolHistory.setNprotocolhistorycode(protocolHistorySeqNo);

			List<Map<String, Object>> lstDataTest = getProtocolAuditGet(objProtocol.getNprotocolcode(),
					objProtocolVersion.getNdesigntemplatemappingcode(), objProtocolVersion.getNapproveconfversioncode(),
					userInfo);
			System.out.println(lstDataTest);
			auditmap.put("nregtypecode", -1);
			auditmap.put("nregsubtypecode", -1);
			auditmap.put("ndesigntemplatemappingcode", objProtocolVersion.getNdesigntemplatemappingcode());
			actionType.put("protocol", "IDS_ADDPROTOCOL");
			jsonAuditObject.put("protocol", lstDataTest);
			auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);
			return getProtocolData(inputMap, userInfo);
		}
	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> createProtocolWithFile(final MultipartHttpServletRequest inputMap,
			final UserInfo userInfo) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final Object reg = objectMapper.readValue(inputMap.getParameter("Map"), new TypeReference<Object>() {
		});
		final String uploadStatus = ftpUtilityFunction.getFileFTPUpload(inputMap, -1, userInfo);
		if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(uploadStatus)) {
			return (ResponseEntity<Object>) createProtocol((Map<String, Object>) reg, userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(uploadStatus, userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@SuppressWarnings({ "unchecked" })
	public ResponseEntity<Object> updateProtocol(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		final JSONObject actionType = new JSONObject();
		final JSONObject jsonAuditOld = new JSONObject();
		final JSONObject jsonAuditNew = new JSONObject();
		final Integer nprotocolcode = (Integer) inputMap.get("nprotocolcode");
		final Protocol objProtocol = objMapper.convertValue(inputMap.get("protocol"), Protocol.class);
		final ProtocolVersion objProtocolVersion = objMapper.convertValue(inputMap.get("protocolVersion"),
				ProtocolVersion.class);
		final List<Map<String, Object>> UniqueValidation = objMapper.convertValue(inputMap.get("combinationUnique"),
				new TypeReference<List<Map<String, Object>>>() {
				});
		final Map<String, Object> map1 = projectDAOSupport.validateUniqueConstraint(UniqueValidation,
				(Map<String, Object>) inputMap.get("protocolVersion"), userInfo, "update", ProtocolVersion.class,
				"nprotocolcode", false);
		if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
				.equals(map1.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
			return new ResponseEntity<>(map1.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			List<Map<String, Object>> lstDataOld = getProtocolAuditGet(objProtocol.getNprotocolcode(),
					objProtocolVersion.getNdesigntemplatemappingcode(), objProtocolVersion.getNapproveconfversioncode(),
					userInfo);
			List<String> dateList = objMapper.convertValue(inputMap.get("dateList"), new TypeReference<List<String>>() {
			});
			JSONObject jsonObject = new JSONObject(objProtocolVersion.getJsondata());
			JSONObject jsonUIObject = new JSONObject(objProtocolVersion.getJsonuidata());
			if (!dateList.isEmpty()) {
				jsonObject = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonObject, dateList,
						false, userInfo);
				jsonUIObject = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonUIObject, dateList,
						false, userInfo);
			}
			jsonUIObject.put("nprotocolcode", nprotocolcode);
			String queryString = " update protocol set sprotocolname =N'"
					+ stringUtilityFunction.replaceQuote(objProtocolVersion.getSprotocolname()) + "',"
					+ " nproductcatcode =" + objProtocol.getNproductcatcode() + ",nproductcode ="
					+ objProtocol.getNproductcode() + " " + " where nprotocolcode in (" + nprotocolcode + ");";

			queryString = queryString + "update protocolversion set jsondata=jsondata||'"
					+ stringUtilityFunction.replaceQuote(jsonObject.toString()) + "'::jsonb,jsonuidata=jsonuidata||'"
					+ stringUtilityFunction.replaceQuote(jsonUIObject.toString()) + "'::jsonb  where nprotocolcode in ("
					+ nprotocolcode + ")";

			jdbcTemplate.execute(queryString);

			final List<Map<String, Object>> lstDataNew = getProtocolAuditGet(objProtocol.getNprotocolcode(),
					objProtocolVersion.getNdesigntemplatemappingcode(), objProtocolVersion.getNapproveconfversioncode(),
					userInfo);

			jsonAuditOld.put("protocol", lstDataOld);
			jsonAuditNew.put("protocol", lstDataNew);
			auditmap.put("nregtypecode", -1);
			auditmap.put("nregsubtypecode", -1);
			auditmap.put("ndesigntemplatemappingcode", objProtocolVersion.getNdesigntemplatemappingcode());
			actionType.put("protocol", "IDS_EDITPROTOCOL");
			auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, jsonAuditNew, actionType, auditmap, false, userInfo);
			return editGetProtocolData(inputMap, userInfo);
		}

	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> updateProtocolWithFile(final MultipartHttpServletRequest request,
			final UserInfo userInfo) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		Object reg = objectMapper.readValue(request.getParameter("Map"), new TypeReference<Object>() {
		});
		final String uploadStatus = ftpUtilityFunction.getFileFTPUpload(request, -1, userInfo);

		if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(uploadStatus)) {
			return (ResponseEntity<Object>) updateProtocol((Map<String, Object>) reg, userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(uploadStatus, userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	public ResponseEntity<Object> deleteProtocol(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		final JSONObject actionType = new JSONObject();
		final JSONObject jsonAuditOld = new JSONObject();

		final String str = "select  " + "sp.nprotocolcode,sph.ntransactionstatus  " + "from protocol sp "
				+ "join protocolversion spv on spv.nprotocolcode =sp.nprotocolcode "
				+ "join protocolhistory sph on sph.nprotocolversioncode =spv.nprotocolversioncode and sph.nprotocolcode =spv.nprotocolcode "
				+ "where    " + "sph.nprotocolhistorycode=any(select max(nprotocolhistorycode)  "
				+ "from protocolhistory sph " + "join protocol sp on sp.nprotocolcode =sph.nprotocolcode "
				+ "where sp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sph.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sp.nsitecode="+userInfo.getNmastersitecode()+" "
				+ "group by sp.nprotocolcode,sph.nprotocolversioncode) " + "and sp.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and spv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sph.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and sp.nsitecode ="
				+ userInfo.getNmastersitecode() + " and sp.nprotocolcode=" + inputMap.get("nprotocolcode") + " ";
		final Protocol oldprotocol = (Protocol) jdbcUtilityFunction.queryForObject(str, Protocol.class, jdbcTemplate);

		if (oldprotocol == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
//			final String query="select 'IDS_STABILITY' as Msg from stability where nprotocolcode= " 
//							  + objProtocol.getNprotocolcode() + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//	
//			ValidatorDel objDeleteValidation = getTransactionInfo(query, userInfo);   			

			// boolean validRecord = false;
//			if (objDeleteValidation.getnreturnstatus() == Enumeration.Deletevalidator.SUCESS.getReturnvalue()) {		
//				validRecord = true;
//				objDeleteValidation = validateDeleteRecord(Integer.toString(objProtocol.getNprotocolcode()), userInfo);
//				if (objDeleteValidation.getnreturnstatus() == Enumeration.Deletevalidator.SUCESS.getReturnvalue()) 	{					
//					validRecord = true;
//				}else {
//					validRecord = false;
//				}
//			}

			// if(validRecord) {
			// final List<String> multilingualIDList = new ArrayList<>();
			// final List<Object> deletedUnitList = new ArrayList<>();
			if (oldprotocol.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
					|| oldprotocol.getNtransactionstatus() == Enumeration.TransactionStatus.CORRECTION
							.gettransactionstatus()) {

				int nDesignTemplateMappingCode = (int) inputMap.get("ndesigntemplatemappingcode");
				int nApprovalConfigVersionCode = (int) inputMap.get("napprovalconfigversioncode");
				int nProtcolCode = (int) inputMap.get("nprotocolcode");

				List<Map<String, Object>> lstDataOld = getProtocolAuditGet(nProtcolCode, nDesignTemplateMappingCode,
						nApprovalConfigVersionCode, userInfo);

				final String query = "select * from protocolfile where nprotocolcode=" + inputMap.get("nprotocolcode")
						+ "";
				List<ProtocolFile> lstProtocolFile = jdbcTemplate.query(query, new ProtocolFile());

				String updateQueryString = " update protocol set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nprotocolcode="
						+ inputMap.get("nprotocolcode") + ";";

				updateQueryString = updateQueryString + " update protocolversion set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nprotocolcode="
						+ inputMap.get("nprotocolcode") + ";";

				updateQueryString = updateQueryString + " update protocolhistory set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nprotocolcode="
						+ inputMap.get("nprotocolcode") + ";";

				if (lstProtocolFile.size() > 0) {
					updateQueryString = updateQueryString + " update protocolfile set dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nprotocolcode="
							+ inputMap.get("nprotocolcode") + ";";
				}

				jdbcTemplate.execute(updateQueryString);

				jsonAuditOld.put("protocol", lstDataOld);
				auditmap.put("nregtypecode", -1);
				auditmap.put("nregsubtypecode", -1);
				auditmap.put("ndesigntemplatemappingcode", nDesignTemplateMappingCode);
				actionType.put("protocol", "IDS_DELETEPROTOCOL");
				auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, null, actionType, auditmap, false, userInfo);
				return getProtocolData(inputMap, userInfo);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTCORRECTIONRECORD",
						userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}


//		}else{
//			return new ResponseEntity<>("a", HttpStatus.EXPECTATION_FAILED);
//		}
		}
	}

	@Override
	public ResponseEntity<Object> completeProtocol(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		final JSONObject actionType = new JSONObject();
		final JSONObject jsonAuditOld = new JSONObject();
		final JSONObject jsonAuditNew = new JSONObject();

		String stransStatus = "";

		final String getProtocolQuery = "select  " + "sp.nprotocolcode,spv.nprotocolversioncode,sph.ntransactionstatus "
				+ "from protocol sp " + "join protocolversion spv on spv.nprotocolcode =sp.nprotocolcode "
				+ "join protocolhistory sph on sph.nprotocolversioncode =spv.nprotocolversioncode and sph.nprotocolcode =spv.nprotocolcode "
				+ "where " + "sph.nprotocolhistorycode=any(select max(nprotocolhistorycode)  "
				+ "from protocolhistory sph " + "join protocol sp on sp.nprotocolcode =sph.nprotocolcode " + "where  "
				+ "sp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sph.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sp.nsitecode="+userInfo.getNmastersitecode()+" "
				+ "group by sp.nprotocolcode,sph.nprotocolversioncode)" + "and sp.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and spv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sph.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and sp.nsitecode ="
				+ userInfo.getNmastersitecode() + " and sp.nprotocolcode=" + inputMap.get("nprotocolcode") + " ";
		final Protocol oldprotocol = (Protocol) jdbcUtilityFunction.queryForObject(getProtocolQuery, Protocol.class,
				jdbcTemplate);
		if (oldprotocol == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (oldprotocol.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
					|| oldprotocol.getNtransactionstatus() == Enumeration.TransactionStatus.CORRECTION
							.gettransactionstatus()) {

				final String strQuery = "lock table lockprotocol " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus()
						+ " ";
				jdbcTemplate.execute(strQuery);

				int nDesignTemplateMappingCode = (int) inputMap.get("ndesigntemplatemappingcode");
				int nApprovalConfigVersionCode = (int) inputMap.get("napprovalconfigversioncode");
				int nProtcolCode = (int) inputMap.get("nprotocolcode");

				final List<Map<String, Object>> lstDataOld = getProtocolAuditGet(nProtcolCode,
						nDesignTemplateMappingCode, nApprovalConfigVersionCode, userInfo);

				final String str = " select * from approvalconfigautoapproval where napprovalconfigversioncode ="
						+ inputMap.get("napprovalconfigversioncode") + " and nsitecode ="
						+ userInfo.getNmastersitecode() + " and nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
				final ApprovalConfigAutoapproval objApprovalConfigApproval = (ApprovalConfigAutoapproval) jdbcUtilityFunction
						.queryForObject(str, ApprovalConfigAutoapproval.class, jdbcTemplate);
				;
				if (objApprovalConfigApproval != null) {
					final int checkAutoApproval = objApprovalConfigApproval.getNneedautoapproval();

					// Added by sonia on 12th Feb 2025 for jira id :ALPD-5403
					int protocolHistorySeqNo = (int) jdbcUtilityFunction.queryForObject(
							"select nsequenceno from seqnostability where stablename='protocolhistory'", Integer.class,
							jdbcTemplate);
					protocolHistorySeqNo++;

					if (checkAutoApproval == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
						final String str1 = "select * from approvalconfigrole where napproveconfversioncode ="
								+ inputMap.get("napprovalconfigversioncode") + " and nsitecode ="
								+ userInfo.getNmastersitecode() + " and nstatus ="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nlevelno =1 ";
						final ApprovalConfigRole objApprovalConfigRole = (ApprovalConfigRole) jdbcUtilityFunction
								.queryForObject(str1, ApprovalConfigRole.class, jdbcTemplate);
						;

						oldprotocol.setNtransactionstatus(objApprovalConfigRole.getNapprovalstatuscode());
						final String strformat = projectDAOSupport.getSeqfnFormat("protocol",
								"seqnoformatgeneratorprotocol", 0, 0, userInfo);
						final String updatequery = "update protocol set sprotocolid='" + strformat
								+ "' where nprotocolcode =" + oldprotocol.getNprotocolcode() + " ";

						jdbcTemplate.execute(updatequery);

						// Added by sonia on 12th Feb 2025 for jira id :ALPD-5403
						String protocolHistoryInsert = "Insert into protocolhistory (nprotocolhistorycode,nprotocolversioncode,nprotocolcode,"
								+ "ntransactionstatus,nuserrolecode,nusercode,ndeputyuserrolecode,ndeputyusercode,"
								+ "dmodifieddate,nsitecode,nstatus)" + "values(" + protocolHistorySeqNo + ","
								+ oldprotocol.getNprotocolversioncode() + "," + "" + oldprotocol.getNprotocolcode()
								+ "," + Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() + ", " + ""
								+ userInfo.getNuserrole() + "," + userInfo.getNusercode() + ","
								+ userInfo.getNdeputyuserrole() + "," + "" + userInfo.getNdeputyusercode() + ",'"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + ""
								+ userInfo.getNmastersitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " )," + "("
								+ (protocolHistorySeqNo + 1) + "," + oldprotocol.getNprotocolversioncode() + "," + ""
								+ oldprotocol.getNprotocolcode() + "," + objApprovalConfigRole.getNapprovalstatuscode()
								+ ", " + "" + objApprovalConfigRole.getNuserrolecode() + "," + userInfo.getNusercode()
								+ "," + userInfo.getNdeputyuserrole() + "," + "" + userInfo.getNdeputyusercode() + ",'"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + ""
								+ userInfo.getNmastersitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " )";
						jdbcTemplate.execute(protocolHistoryInsert);

						jdbcTemplate.execute("update seqnostability set nsequenceno = " + (protocolHistorySeqNo + 1)
								+ " where stablename='protocolhistory'");

						stransStatus = (String) jdbcUtilityFunction.queryForObject(
								"select coalesce(jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
										+ "',jsondata->'stransdisplaystatus'->>'en-US')  from transactionstatus "
										+ "where ntranscode =" + objApprovalConfigRole.getNapprovalstatuscode()
										+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
								String.class, jdbcTemplate);
						stransStatus = commonFunction.getMultilingualMessage(stransStatus,
								userInfo.getSlanguagefilename());
						final String msg = "IDS_PROTOCOL";
						stransStatus = commonFunction.getMultilingualMessage(stransStatus + " " + msg,
								userInfo.getSlanguagefilename());


					} else {
						oldprotocol
								.setNtransactionstatus(Enumeration.TransactionStatus.COMPLETED.gettransactionstatus());
						stransStatus = commonFunction.getMultilingualMessage("IDS_COMPLETEPROTOCOL",
								userInfo.getSlanguagefilename());

						// Added by sonia on 12th Feb 2025 for jira id :ALPD-5403
						String protocolHistoryInsert = "Insert into protocolhistory (nprotocolhistorycode,nprotocolversioncode,nprotocolcode,ntransactionstatus,nuserrolecode,nusercode,ndeputyuserrolecode,ndeputyusercode,dmodifieddate,nsitecode,nstatus)"
								+ "values(" + protocolHistorySeqNo + "," + oldprotocol.getNprotocolversioncode() + ","
								+ oldprotocol.getNprotocolcode() + "," + oldprotocol.getNtransactionstatus() + "," + ""
								+ userInfo.getNuserrole() + "," + userInfo.getNusercode() + ","
								+ userInfo.getNdeputyuserrole() + "," + userInfo.getNdeputyusercode() + ",'"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
								+ userInfo.getNmastersitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " )";
						jdbcTemplate.execute(protocolHistoryInsert);
						jdbcTemplate.execute("update seqnostability set nsequenceno = " + protocolHistorySeqNo
								+ " where stablename='protocolhistory'");
					}
				}
				final String updateQueryString = " update protocolversion set ntransactionstatus="
						+ oldprotocol.getNtransactionstatus() + "" + " where nprotocolcode="
						+ oldprotocol.getNprotocolcode() + " and nprotocolversioncode ="
						+ oldprotocol.getNprotocolversioncode() + " ";
				jdbcTemplate.execute(updateQueryString);

				List<Map<String, Object>> lstDataNew = getProtocolAuditGet(nProtcolCode, nDesignTemplateMappingCode,
						nApprovalConfigVersionCode, userInfo);

				jsonAuditOld.put("protocol", lstDataOld);
				jsonAuditNew.put("protocol", lstDataNew);
				auditmap.put("nregtypecode", -1);
				auditmap.put("nregsubtypecode", -1);
				auditmap.put("ndesigntemplatemappingcode", nDesignTemplateMappingCode);
				actionType.put("protocol", stransStatus);
				auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, jsonAuditNew, actionType, auditmap, false,
						userInfo);

				return editGetProtocolData(inputMap, userInfo);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTCORRECTIONRECORD",
						userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}

		}
	}

	public ResponseEntity<Object> dynamicActionProtocol(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		final JSONObject actionType = new JSONObject();
		final JSONObject jsonAuditOld = new JSONObject();
		final JSONObject jsonAuditNew = new JSONObject();

		final String getProtocolQuery = "select  "
				+ "sp.nprotocolcode,spv.nprotocolversioncode,spv.napproveconfversioncode,sph.ntransactionstatus "
				+ "from protocol sp " + "join protocolversion spv on spv.nprotocolcode =sp.nprotocolcode "
				+ "join protocolhistory sph on sph.nprotocolversioncode =spv.nprotocolversioncode and sph.nprotocolcode =spv.nprotocolcode "
				+ "where    " + "sph.nprotocolhistorycode=any(select max(nprotocolhistorycode)  "
				+ "from protocolhistory sph " + "join protocol sp on sp.nprotocolcode =sph.nprotocolcode " + "where  "
				+ "sp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sph.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sp.nsitecode="+userInfo.getNmastersitecode()+" "
				+ "group by sp.nprotocolcode,sph.nprotocolversioncode) "			
				+ "and sp.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and spv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sph.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and sp.nsitecode ="
				+ userInfo.getNmastersitecode() + " and sp.nprotocolcode=" + inputMap.get("nprotocolcode") + " ";

		final Protocol oldProtocol = (Protocol) jdbcUtilityFunction.queryForObject(getProtocolQuery, Protocol.class,
				jdbcTemplate);
		if (oldProtocol == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String sActionQry = "select acvd.ntransactionstatus,acvd.napprovalvalidationcode,acr.nuserrolecode,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
					+ userInfo.getSlanguagetypecode()
					+ "',ts.jsondata->'stransdisplaystatus'->>'en-US')  as svalidationstatus"
					+ " from approvalrolevalidationdetail acvd "
					+ " join approvalconfigrole acr on acr.napprovalconfigrolecode = acvd.napprovalconfigrolecode "
					+ " join approvalconfig ac on  ac.napprovalconfigcode = acr.napprovalconfigcode "
					+ " join transactionstatus ts on ts.ntranscode = acvd.ntransactionstatus  "
					+ " where  acvd.nuserrolecode = " + userInfo.getNuserrole()
					+ " and acr.napproveconfversioncode = " + oldProtocol.getNapproveconfversioncode()
					+ " and ac.nregtypecode = " + Enumeration.TransactionStatus.NA.gettransactionstatus() + " "
					+ " and ac.nregsubtypecode = " + Enumeration.TransactionStatus.NA.gettransactionstatus() + " "
					+ " and ac.napprovalsubtypecode =" + Enumeration.ApprovalSubType.PROTOCOLAPPROVAL.getnsubtype()
					+ " and acvd.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and acr.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and ac.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
					+ " and ts.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and acvd.nsitecode="+userInfo.getNmastersitecode()+" ";
			List<ApprovalRoleValidationDetail> listValidationActions = (List<ApprovalRoleValidationDetail>) jdbcTemplate
					.query(sActionQry, new ApprovalRoleValidationDetail());
			if (listValidationActions.isEmpty()) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);

			} else {
				List<ApprovalRoleValidationDetail> listACVD = listValidationActions.stream()
						.filter(objACVD -> objACVD.getNtransactionstatus() == oldProtocol.getNtransactionstatus())
						.collect(Collectors.toList());
				if (listACVD.isEmpty()) {
					final List<ApprovalRoleValidationDetail> listValidationStatus = objMapper.convertValue(
							commonFunction.getMultilingualMessageList(listValidationActions,
									Arrays.asList("svalidationstatus"), userInfo.getSlanguagefilename()),
							new TypeReference<List<ApprovalRoleValidationDetail>>() {
							});

					final String returnMessage = commonFunction.getMultilingualMessage("IDS_SELECT",
							userInfo.getSlanguagefilename()) + " "
							+ stringUtilityFunction.fnDynamicListToString(listValidationStatus, "getSvalidationstatus")
							+ " "
							+ commonFunction.getMultilingualMessage("IDS_PROTOCOL", userInfo.getSlanguagefilename());
					return new ResponseEntity<Object>(returnMessage, HttpStatus.EXPECTATION_FAILED);
				} else {

					final String strQuery = "lock table lockprotocol "
							+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus() + " ";
					jdbcTemplate.execute(strQuery);

					int nDesignTemplateMappingCode = (int) inputMap.get("ndesigntemplatemappingcode");
					int nApprovalConfigVersionCode = (int) inputMap.get("napprovalconfigversioncode");
					int nProtcolCode = (int) inputMap.get("nprotocolcode");

					List<Map<String, Object>> lstDataOld = getProtocolAuditGet(nProtcolCode, nDesignTemplateMappingCode,
							nApprovalConfigVersionCode, userInfo);

					int protocolHistorySeqNo = (int) jdbcUtilityFunction.queryForObject(
							"select nsequenceno from seqnostability where stablename='protocolhistory'", Integer.class,
							jdbcTemplate);
					protocolHistorySeqNo++;

					String protocolHistoryInsert = "Insert into protocolhistory (nprotocolhistorycode,nprotocolversioncode,nprotocolcode,ntransactionstatus,nuserrolecode,nusercode,ndeputyuserrolecode,ndeputyusercode,dmodifieddate,nsitecode,nstatus)"
							+ "values(" + protocolHistorySeqNo + "," + oldProtocol.getNprotocolversioncode() + ","
							+ oldProtocol.getNprotocolcode() + "," + inputMap.get("napprovalstatus") + "," + ""
							+ listACVD.get(0).getNuserrolecode() + "," + userInfo.getNusercode() + ","
							+ userInfo.getNdeputyuserrole() + "," + userInfo.getNdeputyusercode() + ",'"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode()
							+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " )";

					jdbcTemplate.execute(protocolHistoryInsert);

					jdbcTemplate.execute("update seqnostability set nsequenceno = " + protocolHistorySeqNo
							+ " where stablename='protocolhistory'");

					String updateQuery = "update protocolversion set ntransactionstatus="
							+ inputMap.get("napprovalstatus") + " " + "where nprotocolversioncode ="
							+ oldProtocol.getNprotocolversioncode() + " and nprotocolcode="
							+ oldProtocol.getNprotocolcode() + " ";
					jdbcTemplate.execute(updateQuery);

					final String str1 = "select * from approvalconfigrole where napproveconfversioncode ="
							+ inputMap.get("napprovalconfigversioncode") + " and nsitecode ="
							+ userInfo.getNmastersitecode() + " and nstatus ="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nlevelno =1 ";
					final ApprovalConfigRole objApprovalConfigRole = (ApprovalConfigRole) jdbcUtilityFunction
							.queryForObject(str1, ApprovalConfigRole.class, jdbcTemplate);
					;
					final int a = objApprovalConfigRole.getNapprovalstatuscode();
					final int b = (int) inputMap.get("napprovalstatus");

					if (a == b) {
						String strformat = projectDAOSupport.getSeqfnFormat("protocol", "seqnoformatgeneratorprotocol",
								0, 0, userInfo);
						String updatequery = "update protocol set sprotocolid='" + strformat + "' where nprotocolcode ="
								+ oldProtocol.getNprotocolcode() + " ";
						jdbcTemplate.execute(updatequery);
					}
					List<Map<String, Object>> lstDataNew = getProtocolAuditGet(nProtcolCode, nDesignTemplateMappingCode,
							nApprovalConfigVersionCode, userInfo);

					String stransStatus = (String) jdbcUtilityFunction.queryForObject(
							"select coalesce(jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
									+ "',jsondata->'stransdisplaystatus'->>'en-US')  from transactionstatus "
									+ "where ntranscode =" + inputMap.get("napprovalstatus") + " and nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
							String.class, jdbcTemplate);
					stransStatus = commonFunction.getMultilingualMessage(stransStatus, userInfo.getSlanguagefilename());
					String msg = commonFunction.getMultilingualMessage("IDS_PROTOCOL", userInfo.getSlanguagefilename());
					stransStatus = commonFunction.getMultilingualMessage(stransStatus + " " + msg,
							userInfo.getSlanguagefilename());

					jsonAuditOld.put("protocol", lstDataOld);
					jsonAuditNew.put("protocol", lstDataNew);
					auditmap.put("nregtypecode", -1);
					auditmap.put("nregsubtypecode", -1);
					auditmap.put("ndesigntemplatemappingcode", nDesignTemplateMappingCode);
					actionType.put("protocol", stransStatus);
					auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, jsonAuditNew, actionType, auditmap, false,
							userInfo);
					return editGetProtocolData(inputMap, userInfo);
				}
			}

		}
	}

	
	public ResponseEntity<Object> rejectProtocol(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		final JSONObject actionType = new JSONObject();
		final JSONObject jsonAuditOld = new JSONObject();
		final JSONObject jsonAuditNew = new JSONObject();

		final String getProtocolQuery = "select  " + "sp.nprotocolcode,spv.nprotocolversioncode,sph.ntransactionstatus "
				+ "from protocol sp " + "join protocolversion spv on spv.nprotocolcode =sp.nprotocolcode "
				+ "join protocolhistory sph on sph.nprotocolversioncode =spv.nprotocolversioncode and sph.nprotocolcode =spv.nprotocolcode "
				+ "where " + "sph.nprotocolhistorycode=any(select max(nprotocolhistorycode)  "
				+ "from protocolhistory sph " + "join protocol sp on sp.nprotocolcode =sph.nprotocolcode " + "where  "
				+ "sp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sph.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sp.nsitecode="+userInfo.getNmastersitecode()+" "
				+ "group by sp.nprotocolcode,sph.nprotocolversioncode)" + "and sp.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and spv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sph.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and sp.nsitecode ="
				+ userInfo.getNmastersitecode() + " and sp.nprotocolcode=" + inputMap.get("nprotocolcode") + " ";
		Protocol oldprotocol = (Protocol) jdbcUtilityFunction.queryForObject(getProtocolQuery, Protocol.class,
				jdbcTemplate);
		if (oldprotocol == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final String str1 = "select * from approvalconfigrole where napproveconfversioncode ="
					+ inputMap.get("napprovalconfigversioncode") + " and nsitecode =" + userInfo.getNmastersitecode()
					+ " and nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nlevelno =1 ";
			final ApprovalConfigRole objApprovalConfigRole = (ApprovalConfigRole) jdbcUtilityFunction
					.queryForObject(str1, ApprovalConfigRole.class, jdbcTemplate);
			;

			if (oldprotocol.getNtransactionstatus() == objApprovalConfigRole.getNapprovalstatuscode()) {
				final String strQuery = "lock table lockprotocol " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus()
						+ " ";
				jdbcTemplate.execute(strQuery);

				int nDesignTemplateMappingCode = (int) inputMap.get("ndesigntemplatemappingcode");
				int nApprovalConfigVersionCode = (int) inputMap.get("napprovalconfigversioncode");
				int nProtcolCode = (int) inputMap.get("nprotocolcode");

				List<Map<String, Object>> lstDataOld = getProtocolAuditGet(nProtcolCode, nDesignTemplateMappingCode,
						nApprovalConfigVersionCode, userInfo);

				int protocolHistorySeqNo = (int) jdbcUtilityFunction.queryForObject(
						"select nsequenceno from seqnostability where stablename='protocolhistory'", Integer.class,
						jdbcTemplate);
				protocolHistorySeqNo++;

				String protocolHistoryInsert = "Insert into protocolhistory (nprotocolhistorycode,nprotocolversioncode,nprotocolcode,ntransactionstatus,nuserrolecode,nusercode,ndeputyuserrolecode,ndeputyusercode,dmodifieddate,nsitecode,nstatus)"
						+ "values(" + protocolHistorySeqNo + "," + oldprotocol.getNprotocolversioncode() + ","
						+ oldprotocol.getNprotocolcode() + ","
						+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + "," + ""
						+ userInfo.getNuserrole() + "," + userInfo.getNusercode() + "," + userInfo.getNdeputyuserrole()
						+ "," + userInfo.getNdeputyusercode() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "'," + userInfo.getNmastersitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " )";

				jdbcTemplate.execute(protocolHistoryInsert);

				jdbcTemplate.execute("update seqnostability set nsequenceno = " + protocolHistorySeqNo
						+ " where stablename='protocolhistory'");

				String updateQuery = "update protocolversion set ntransactionstatus="
						+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + " "
						+ "where nprotocolversioncode =" + oldprotocol.getNprotocolversioncode() + " and nprotocolcode="
						+ oldprotocol.getNprotocolcode() + " ";
				jdbcTemplate.execute(updateQuery);

				List<Map<String, Object>> lstDataNew = getProtocolAuditGet(nProtcolCode, nDesignTemplateMappingCode,
						nApprovalConfigVersionCode, userInfo);

				jsonAuditOld.put("protocol", lstDataOld);
				jsonAuditNew.put("protocol", lstDataNew);
				auditmap.put("nregtypecode", -1);
				auditmap.put("nregsubtypecode", -1);
				auditmap.put("ndesigntemplatemappingcode", nDesignTemplateMappingCode);
				actionType.put("protocol", "IDS_REJECTPROTOCOL");
				auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, jsonAuditNew, actionType, auditmap, false,
						userInfo);
				return editGetProtocolData(inputMap, userInfo);
			} else {

				String stransStatus = (String) jdbcUtilityFunction.queryForObject(
						"select coalesce(jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
								+ "',jsondata->'stransdisplaystatus'->>'en-US')  from transactionstatus "
								+ "where ntranscode =" + objApprovalConfigRole.getNapprovalstatuscode()
								+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
						String.class, jdbcTemplate);
				stransStatus = commonFunction.getMultilingualMessage(stransStatus, userInfo.getSlanguagefilename());
				final String msg = commonFunction.getMultilingualMessage("IDS_SELECT", userInfo.getSlanguagefilename());
				final String msg1 = commonFunction.getMultilingualMessage("IDS_PROTOCOL",
						userInfo.getSlanguagefilename());
				stransStatus = commonFunction.getMultilingualMessage(msg + ' ' + stransStatus + " " + msg1,
						userInfo.getSlanguagefilename());

				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(stransStatus, userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}

		}
	}

	public ResponseEntity<Object> getProtocolFile(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();

		String queryformat = "COALESCE(TO_CHAR(spf.dcreateddate,'" + userInfo.getSpgsitedatetime() + "'),'-') ";

		final String protocolFileQry = " select spf.nprotocolfilecode,spf.nprotocolversioncode, spf.nprotocolcode,spf.noffsetdcreateddate, "
				+ " spf.nlinkcode, spf.nattachmenttypecode, spf.sfilename, spf.sdescription,"
				+ " COALESCE(at.jsondata->'sattachmenttype'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " at.jsondata->'sattachmenttype'->>'en-US') as stypename, "
				+ " case when spf.nlinkcode = -1 then '-' else lm.jsondata->>'slinkname' end slinkname,"
				+ " case when spf.nlinkcode = -1 then cast(spf.nfilesize as text) else '-' end sfilesize,"
				+ " case when spf.nlinkcode = -1 then " + queryformat + " else '-' end screateddate"
				+ " from protocolfile spf "
				+ " join protocolversion spv on spv.nprotocolversioncode = spf.nprotocolversioncode "
				+ " join attachmenttype at on at.nattachmenttypecode = spf.nattachmenttypecode "
				+ " join linkmaster lm on lm.nlinkcode = spf.nlinkcode " + " where " + " at.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and lm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and spf.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and spv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and spf.nsitecode="+userInfo.getNmastersitecode()+" "
						+ " and spf.nprotocolcode = "
				+ inputMap.get("nprotocolcode") + ";";
		outputMap.put("protocolFile",
				dateUtilityFunction.getSiteLocalTimeFromUTC(jdbcTemplate.query(protocolFileQry, new ProtocolFile()),
						Arrays.asList("screateddate"), Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null,
						false));

		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getActiveProtocolFileById(final int nprotocolCode, final int nprotocolFileCode,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();

		String queryformat = "COALESCE(TO_CHAR(spf.dcreateddate,'" + userInfo.getSpgsitedatetime() + "'),'-') ";

		final String protocolFileQry = " select spf.nprotocolfilecode,spf.nprotocolversioncode, spf.nprotocolcode,spf.noffsetdcreateddate, "
				+ " spf.nlinkcode, spf.nattachmenttypecode, spf.sfilename,spf.ssystemfilename, spf.sdescription,"
				+ " COALESCE(at.jsondata->'sattachmenttype'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " at.jsondata->'sattachmenttype'->>'en-US') as stypename, "
				+ " case when spf.nlinkcode = "+Enumeration.TransactionStatus.NA.gettransactionstatus()+" then '-' else lm.jsondata->>'slinkname' end slinkname,"
				+ " case when spf.nlinkcode = "+Enumeration.TransactionStatus.NA.gettransactionstatus()+" then cast(spf.nfilesize as text) else '-' end sfilesize,"
				+ " case when spf.nlinkcode = "+Enumeration.TransactionStatus.NA.gettransactionstatus()+" then " + queryformat + " else '-' end screateddate"
				+ " from protocolfile spf "
				+ " join protocolversion spv on spv.nprotocolversioncode = spf.nprotocolversioncode "
				+ " join attachmenttype at on at.nattachmenttypecode = spf.nattachmenttypecode "
				+ " join linkmaster lm on  lm.nlinkcode = spf.nlinkcode " + " where " + " at.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and lm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and spf.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and spv.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and spf.nprotocolcode = "
				+ nprotocolCode + " and spf.nsitecode="+userInfo.getNmastersitecode()+" and spf.nprotocolfilecode=" + nprotocolFileCode + ";";
		outputMap.put("ProtocolFile",
				dateUtilityFunction.getSiteLocalTimeFromUTC(jdbcTemplate.query(protocolFileQry, new ProtocolFile()),
						Arrays.asList("screateddate"), Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null,
						false));

		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> createProtocolFile(final UserInfo userInfo, final MultipartHttpServletRequest request)
			throws Exception {

		final String sQuery = " lock  table lockprotocolfile " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final Map<String, Object> obj = new HashMap<String, Object>();
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final List<String> multilingualIDList = new ArrayList<>();

		final List<ProtocolFile> lstProtocolFile = objMapper.readValue(request.getParameter("protocolFile"),
				new TypeReference<List<ProtocolFile>>() {
				});
		if (lstProtocolFile != null && lstProtocolFile.size() > 0) {
			final String getProtocolQuery = "select  "
					+ "sp.nprotocolcode,spv.nprotocolversioncode,sph.ntransactionstatus " + "from protocol sp "
					+ "join protocolversion spv on spv.nprotocolcode =sp.nprotocolcode "
					+ "join protocolhistory sph on sph.nprotocolversioncode =spv.nprotocolversioncode and sph.nprotocolcode =spv.nprotocolcode "
					+ "where " + "sph.nprotocolhistorycode=any(select max(nprotocolhistorycode) "
					+ "from protocolhistory sph " + "join protocol sp on sp.nprotocolcode =sph.nprotocolcode "
					+ "where " + "sp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and sph.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sp.nsitecode="+userInfo.getNmastersitecode()+" "
					+ "group by sp.nprotocolcode,sph.nprotocolversioncode) " + "and sp.nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and spv.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sph.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and sp.nsitecode ="
					+ userInfo.getNmastersitecode() + " and sp.nprotocolcode=" + request.getParameter("nprotocolcode")
					+ " ";
			final Protocol oldprotocol = (Protocol) jdbcUtilityFunction.queryForObject(getProtocolQuery, Protocol.class,
					jdbcTemplate);

			if (oldprotocol != null) {
				if (oldprotocol.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
						|| oldprotocol.getNtransactionstatus() == Enumeration.TransactionStatus.CORRECTION
								.gettransactionstatus()) {
					String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
					if (lstProtocolFile.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
						sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, userInfo);// Folder Name -
																									// master
					}
					if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(sReturnString)) {
						final Instant instantDate = dateUtilityFunction.getCurrentDateTime(userInfo)
								.truncatedTo(ChronoUnit.SECONDS);
						final String sattachmentDate = dateUtilityFunction.instantDateToString(instantDate);
						final int offset = dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid());
						lstProtocolFile.forEach(objtf -> {
							objtf.setDcreateddate(instantDate);
							objtf.setNoffsetdcreateddate(offset);
							objtf.setScreateddate(sattachmentDate.replace("T", " "));
						});

						String sequencequery = "select nsequenceno from seqnostability where stablename ='protocolfile'";
						int nsequenceno = (int) jdbcUtilityFunction.queryForObject(sequencequery, Integer.class,
								jdbcTemplate);

						nsequenceno++;

						String insertquery = "Insert into protocolfile(nprotocolfilecode,nprotocolversioncode,nprotocolcode,nlinkcode,nattachmenttypecode,sfilename,sdescription,nfilesize,dcreateddate,noffsetdcreateddate,ntzcreateddate,ssystemfilename,dmodifieddate,nstatus,nsitecode)"
								+ "values (" + nsequenceno + "," + lstProtocolFile.get(0).getNprotocolversioncode()
								+ "," + lstProtocolFile.get(0).getNprotocolcode() + ","
								+ lstProtocolFile.get(0).getNlinkcode() + ","
								+ lstProtocolFile.get(0).getNattachmenttypecode() + "," + " N'"
								+ stringUtilityFunction.replaceQuote(lstProtocolFile.get(0).getSfilename()) + "',N'"
								+ stringUtilityFunction.replaceQuote(lstProtocolFile.get(0).getSdescription()) + "',"
								+ lstProtocolFile.get(0).getNfilesize() + "," + " '"
								+ lstProtocolFile.get(0).getDcreateddate() + "',"
								+ lstProtocolFile.get(0).getNoffsetdcreateddate() + "," + userInfo.getNtimezonecode()
								+ ",N'" + lstProtocolFile.get(0).getSsystemfilename() + "','"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + ""
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
								+ userInfo.getNmastersitecode() + ")";
						jdbcTemplate.execute(insertquery);

						String updatequery = "update seqnostability set nsequenceno =" + nsequenceno
								+ " where stablename ='protocolfile'";
						jdbcTemplate.execute(updatequery);

						multilingualIDList
								.add(lstProtocolFile.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP
										.gettype() ? "IDS_ADDPROTOCOLFILE" : "IDS_ADDPROTOCOLLINK");

						final List<Object> listObject = new ArrayList<Object>();
						lstProtocolFile.get(0).setNprotocolfilecode(nsequenceno);
						listObject.add(lstProtocolFile);

						auditUtilityFunction.fnInsertListAuditAction(listObject, 1, null, multilingualIDList, userInfo);
						obj.put("nprotocolcode", lstProtocolFile.get(0).getNprotocolcode());
						return getProtocolFile(obj, userInfo);
					} else {
						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage(sReturnString, userInfo.getSlanguagefilename()),
								HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_PROTOCOLSTATUSMUSTBEDRAFTCORRECTION",
									userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_ALREADYDELETED", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	public ResponseEntity<Object> updateProtocolFile(UserInfo userInfo, MultipartHttpServletRequest request)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final Map<String, Object> obj = new HashMap<String, Object>();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstOldObject = new ArrayList<Object>();
		final List<Object> lstNewObject = new ArrayList<Object>();

		final List<ProtocolFile> lstProtocolFile = objMapper.readValue(request.getParameter("protocolFile"),
				new TypeReference<List<ProtocolFile>>() {
				});

		if (lstProtocolFile != null && lstProtocolFile.size() > 0) {
			final ProtocolFile objFile = lstProtocolFile.get(0);
			String str = "select  " + "sp.nprotocolcode,sph.ntransactionstatus  " + "from protocol sp "
					+ "join protocolversion spv on spv.nprotocolcode =sp.nprotocolcode "
					+ "join protocolhistory sph on sph.nprotocolversioncode =spv.nprotocolversioncode and sph.nprotocolcode =spv.nprotocolcode "
					+ "where " + "sph.nprotocolhistorycode=any(select max(nprotocolhistorycode)  "
					+ "from protocolhistory sph " + "join protocol sp on sp.nprotocolcode =sph.nprotocolcode "
					+ "where " + "sp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and sph.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sp.nsitecode="+userInfo.getNmastersitecode()+" "
					+ "group by sp.nprotocolcode,sph.nprotocolversioncode) " + "and sp.nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and spv.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sph.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and sp.nsitecode ="
					+ userInfo.getNmastersitecode() + " and sp.nprotocolcode="
					+ lstProtocolFile.get(0).getNprotocolcode() + " ";
			Protocol oldprotocol = (Protocol) jdbcUtilityFunction.queryForObject(str, Protocol.class, jdbcTemplate);

			if (oldprotocol != null) {
				if (oldprotocol.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
						|| oldprotocol.getNtransactionstatus() == Enumeration.TransactionStatus.CORRECTION
								.gettransactionstatus()) {

					final int isFileEdited = Integer.valueOf(request.getParameter("isFileEdited"));
					String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();

					if (isFileEdited == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
						if (objFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
							sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, userInfo);// Folder Name -
																										// master
						}
					}

					if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(sReturnString)) {
						final String sQuery = "select * from protocolfile where nprotocolfilecode = "
								+ objFile.getNprotocolfilecode() + " and nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
						final ProtocolFile objPF = (ProtocolFile) jdbcUtilityFunction.queryForObject(sQuery,
								ProtocolFile.class, jdbcTemplate);

						if (objPF != null) {
							String ssystemfilename = "";
							if (objFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
								ssystemfilename = objFile.getSsystemfilename();
							}
							final String sUpdateQuery = " update protocolfile set sfilename=N'"
									+ stringUtilityFunction.replaceQuote(objFile.getSfilename()) + "',"
									+ " sdescription=N'" + stringUtilityFunction.replaceQuote(objFile.getSdescription())
									+ "', ssystemfilename= N'" + ssystemfilename + "'," + " nattachmenttypecode = "
									+ objFile.getNattachmenttypecode() + ", nlinkcode=" + objFile.getNlinkcode() + ","
									+ " nfilesize = " + objFile.getNfilesize() + ",dmodifieddate='"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'"
									+ " where nprotocolfilecode = " + objFile.getNprotocolfilecode();

							objFile.setDcreateddate(objPF.getDcreateddate());
							objPF.setScreateddate(objFile.getScreateddate());

							jdbcTemplate.execute(sUpdateQuery);

							lstNewObject.add(objFile);
							multilingualIDList
									.add(objFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
											? "IDS_EDITPROTOCOLFILE"
											: "IDS_EDITPROTOCOLLINK");
							lstOldObject.add(objPF);

							auditUtilityFunction.fnInsertAuditAction(lstNewObject, 2, lstOldObject, multilingualIDList,
									userInfo);

							obj.put("nprotocolcode", lstProtocolFile.get(0).getNprotocolcode());
							return getProtocolFile(obj, userInfo);
						} else {
							// status code:417
							return new ResponseEntity<>(commonFunction.getMultilingualMessage(
									Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
									userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
						}
					} else {
						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage(sReturnString, userInfo.getSlanguagefilename()),
								HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_PROTOCOLSTATUSMUSTBEDRAFTCORRECTION",
									userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PROTOCOLALREADYDELETED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	public ResponseEntity<Object> deleteProtocolFile(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstObject = new ArrayList<>();

		final Map<String, Object> obj = new HashMap<String, Object>();

		String str = "select  " + "sp.nprotocolcode,sph.ntransactionstatus  " + "from protocol sp "
				+ "join protocolversion spv on spv.nprotocolcode =sp.nprotocolcode "
				+ "join protocolhistory sph on sph.nprotocolversioncode =spv.nprotocolversioncode and sph.nprotocolcode =spv.nprotocolcode "
				+ "where " + "sph.nprotocolhistorycode=any(select max(nprotocolhistorycode)  "
				+ "from protocolhistory sph " + "join protocol sp on sp.nprotocolcode =sph.nprotocolcode " + "where "
				+ "sp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sph.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sp.nsitecode="+userInfo.getNmastersitecode()+" "
				+ "group by sp.nprotocolcode,sph.nprotocolversioncode) " + "and sp.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and spv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sph.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and sp.nsitecode ="
				+ userInfo.getNmastersitecode() + " and sp.nprotocolcode=" + inputMap.get("nprotocolcode") + " ";
		Protocol oldprotocol = (Protocol) jdbcUtilityFunction.queryForObject(str, Protocol.class, jdbcTemplate);

		if (oldprotocol != null) {
			if (oldprotocol.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
					|| oldprotocol.getNtransactionstatus() == Enumeration.TransactionStatus.CORRECTION
							.gettransactionstatus()) {
				String sQuery = "select * from protocolfile where nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nprotocolfilecode = "
						+ inputMap.get("nprotocolfilecode");
				final ProtocolFile objPF = (ProtocolFile) jdbcUtilityFunction.queryForObject(sQuery, ProtocolFile.class,
						jdbcTemplate);
				if (objPF != null) {
					if (objPF.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
						ftpUtilityFunction.deleteFTPFile(Arrays.asList(objPF.getSsystemfilename()), "", userInfo);// Folder
																													// Name
																													// -
																													// master
					} else {
						objPF.setScreateddate(null);
					}
					final String sUpdateQuery = "update protocolfile set nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ",dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where nprotocolfilecode = "
							+ inputMap.get("nprotocolfilecode");
					jdbcTemplate.execute(sUpdateQuery);

					multilingualIDList.add(objPF.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
							? "IDS_DELETEPROTOCOLFILE"
							: "IDS_DELETESPROTOCOLLINK");
					lstObject.add(objPF);
					auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, multilingualIDList, userInfo);

					obj.put("nprotocolcode", inputMap.get("nprotocolcode"));
					return getProtocolFile(obj, userInfo);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_PROTOCOLSTATUSMUSTBEDRAFTCORRECTION",
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PROTOCOLALREADYDELETED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}

	}

	public ResponseEntity<Object> viewProtocolFile(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		Map<String, Object> map = new HashMap<String, Object>();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstObject = new ArrayList<>();

		String str = "select  " + "sp.nprotocolcode,sph.ntransactionstatus  " + "from protocol sp "
				+ "join protocolversion spv on spv.nprotocolcode =sp.nprotocolcode "
				+ "join protocolhistory sph on sph.nprotocolversioncode =spv.nprotocolversioncode and sph.nprotocolcode =spv.nprotocolcode "
				+ "where " + "sph.nprotocolhistorycode=any(select max(nprotocolhistorycode)  "
				+ "from protocolhistory sph " + "join protocol sp on sp.nprotocolcode =sph.nprotocolcode " + "where  "
				+ "sp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sph.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sp.nsitecode="+userInfo.getNmastersitecode()+" "
				+ "group by sp.nprotocolcode,sph.nprotocolversioncode) " + "and sp.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and spv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sph.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and sp.nsitecode ="
				+ userInfo.getNmastersitecode() + " and sp.nprotocolcode=" + inputMap.get("nprotocolcode") + " ";
		Protocol oldprotocol = (Protocol) jdbcUtilityFunction.queryForObject(str, Protocol.class, jdbcTemplate);

		if (oldprotocol != null) {
			String sQuery = "select * from protocolfile where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and nsitecode="+userInfo.getNmastersitecode()+" and nprotocolfilecode = "
					+ inputMap.get("nprotocolfilecode");
			final ProtocolFile objPF = (ProtocolFile) jdbcUtilityFunction.queryForObject(sQuery, ProtocolFile.class,
					jdbcTemplate);
			if (objPF != null) {
				if (objPF.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
					map = ftpUtilityFunction.FileViewUsingFtp(objPF.getSsystemfilename(), -1, userInfo, "", "");// Folder
																												// Name
																												// -
																												// master
				} else {

					sQuery = "select jsondata->>'slinkname' as slinkname from linkmaster where nlinkcode="
							+ objPF.getNlinkcode() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					LinkMaster objlinkmaster = (LinkMaster) jdbcUtilityFunction.queryForObject(sQuery, LinkMaster.class,
							jdbcTemplate);
					map.put("AttachLink", objlinkmaster.getSlinkname() + objPF.getSfilename());
				}

				multilingualIDList.add(objPF.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
						? "IDS_VIEWPROTOCOLFILE"
						: "IDS_VIEWPROTOCOLLINK");
				lstObject.add(objPF);
				auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, multilingualIDList, userInfo);
				return new ResponseEntity<Object>(map, HttpStatus.OK);
			} else {
				// status code:417
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<Object>(commonFunction.getMultilingualMessage("IDS_PROTOCOLALREADYDELETED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	public ResponseEntity<Object> getCopyProtocol(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();

		final String filterStatusQuery = "select sp.nprotocolcode,sp.nproductcatcode,sp.nproductcode,  "
				+ "pc.sproductcatname,p.sproductname,sp.sprotocolname " + "from protocol sp "
				+ "join productcategory  pc on pc.nproductcatcode =sp.nproductcatcode "
				+ "join product p on p.nproductcode = sp.nproductcode " + "where sp.nprotocolcode ="
				+ inputMap.get("nprotocolcode") + " " + "and sp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and pc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and p.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and sp.nsitecode="+userInfo.getNmastersitecode()+" ";

		final List<Protocol> lstProtocol = jdbcTemplate.query(filterStatusQuery, new Protocol());
		map.put("copyProtocol", lstProtocol);

		final String productCatQuery = "select * from productcategory where nproductcatcode >0  and nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="+userInfo.getNmastersitecode()+" ";
		final List<ProductCategory> lstProductCategory = jdbcTemplate.query(productCatQuery, new ProductCategory());
		map.put("productCategory", lstProductCategory);

		final String productQuery = "select * from product where nproductcatcode = "
				+ lstProtocol.get(0).getNproductcatcode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="+userInfo.getNmastersitecode()+" ";
		final List<Product> lstProduct = jdbcTemplate.query(productQuery, new Product());
		map.put("product", lstProduct);

		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> copyProtocol(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final String strQuery = "lock table lockprotocol " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus() + " ";
		jdbcTemplate.execute(strQuery);

		final int nProtocolCode = (int) inputMap.get("nprotocolcode");
		final String sprotocolName = (String) inputMap.get("sprotocolname");
		final Protocol object1 = checkProtocolIsPresent(nProtocolCode,userInfo);
		final Map<String, Object> map1 = (Map<String, Object>) inputMap.get("Sample Category");
		final Map<String, Object> map2 = (Map<String, Object>) inputMap.get("Sample Type");
		Map<String, Object> returnMap = new HashMap<String, Object>();

		if (object1 == null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PROTOCOLALREADYDELETED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} else {
			final Protocol objProtocolByName = getProtocolByName(sprotocolName, userInfo);
			if (objProtocolByName == null) {

				final String strSelectSeqno = "select stablename,nsequenceno from seqnostability  where stablename "
						+ "in ('protocol','protocolversion','protocolhistory','protocolfile')";

				final List<?> lstMultiSeqNo = projectDAOSupport.getMultipleEntitiesResultSetInList(strSelectSeqno,
						jdbcTemplate, SeqNoStability.class);

				final List<SeqNoStability> lstSeqNoStab = (List<SeqNoStability>) lstMultiSeqNo.get(0);

				returnMap = lstSeqNoStab.stream().collect(Collectors.toMap(SeqNoStability::getStablename,
						SeqNoStability -> SeqNoStability.getNsequenceno()));

				int protocolSeqNo = (int) returnMap.get("protocol") + 1;
				int protocolVersionSeqNo = (int) returnMap.get("protocolversion") + 1;
				int protocolHistorySeqNo = (int) returnMap.get("protocolhistory") + 1;
				int protocolFileSeqNo = (int) returnMap.get("protocolfile") + 1;

				String sQuery = " select count(nprotocolfilecode) nprotocolfilecode from protocolfile where nprotocolcode = "
						+ nProtocolCode + " and nsitecode="+userInfo.getNmastersitecode()+" and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				List<ProtocolFile> lstProtocolFile = jdbcTemplate.query(sQuery, new ProtocolFile());

				final String strQuery1 = "select  *  from protocolversion where nprotocolcode =" + nProtocolCode
						+ " and nsitecode="+userInfo.getNmastersitecode()+" and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
				ProtocolVersion obj = (ProtocolVersion) jdbcUtilityFunction.queryForObject(strQuery1,
						ProtocolVersion.class, jdbcTemplate);

				JSONObject jsonObject = new JSONObject(obj.getJsondata());
				JSONObject jsonUIObject = new JSONObject(obj.getJsonuidata());

				jsonObject.put("Protocol Name", sprotocolName);
				jsonObject.put("Sample Category", map1);
				jsonObject.put("Sample Type", map2);

				jsonUIObject.put("Protocol Name", sprotocolName);
				jsonUIObject.put("Sample Category", map1.get("label"));
				jsonUIObject.put("Sample Type", map2.get("label"));

				String protocolInsert = " Insert into protocol(nprotocolcode,nproductcatcode,nproductcode,sprotocolid,sprotocolname,dmodifieddate,nsitecode,nstatus)"
						+ " values(" + protocolSeqNo + "," + map1.get("value") + "," + map2.get("value") + ","
						+ " N'-', N'" + stringUtilityFunction.replaceQuote(sprotocolName) + "'," + " '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " );";

				protocolInsert += "Insert into protocolversion (nprotocolversioncode,nprotocolcode,ndesigntemplatemappingcode,napproveconfversioncode,sversion,jsondata,jsonuidata,"
						+ "ntransactionstatus,dmodifieddate,nsitecode,nstatus)" + "values(" + protocolVersionSeqNo + ","
						+ protocolSeqNo + "," + obj.getNdesigntemplatemappingcode() + ","
						+ obj.getNapproveconfversioncode() + "," + "N'"
						+ stringUtilityFunction.replaceQuote(obj.getSversion()) + "','"
						+ stringUtilityFunction.replaceQuote(jsonObject.toString()) + "'::JSONB,'"
						+ stringUtilityFunction.replaceQuote(jsonUIObject.toString()) + "'::JSONB," + ""
						+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + "," + "'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " );";

				protocolInsert += "Insert into protocolhistory (nprotocolhistorycode,nprotocolversioncode,nprotocolcode,ntransactionstatus,nuserrolecode,nusercode,ndeputyuserrolecode,ndeputyusercode,dmodifieddate,nsitecode,nstatus)"
						+ "values(" + protocolHistorySeqNo + "," + protocolVersionSeqNo + "," + protocolSeqNo + ","
						+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + "," + ""
						+ userInfo.getNuserrole() + "," + userInfo.getNusercode() + "," + userInfo.getNdeputyuserrole()
						+ "," + userInfo.getNdeputyusercode() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "'," + userInfo.getNmastersitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " );";

				if (!lstProtocolFile.isEmpty()) {
					protocolInsert += "Insert into protocolfile(nprotocolfilecode,nprotocolversioncode,nprotocolcode,nlinkcode,"
							+ "nattachmenttypecode,sfilename,sdescription,nfilesize,dcreateddate,noffsetdcreateddate,ntzcreateddate,"
							+ "ssystemfilename,dmodifieddate,nstatus,nsitecode)" + "select " + protocolFileSeqNo
							+ "+RANK()over(order by nprotocolfilecode) nprotocolfilecode ," + protocolVersionSeqNo + ","
							+ protocolSeqNo + "," + "nlinkcode,nattachmenttypecode,sfilename,sdescription,nfilesize,"
							+ "dcreateddate,noffsetdcreateddate,ntzcreateddate,ssystemfilename,dmodifieddate,nstatus,nsitecode "
							+ "from  protocolfile where nprotocolcode =" + nProtocolCode + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ;";

				}

				protocolInsert += "update seqnostability set nsequenceno = " + protocolSeqNo
						+ " where stablename='protocol';" + "update seqnostability set nsequenceno = "
						+ protocolVersionSeqNo + " where stablename='protocolversion';"
						+ "update seqnostability set nsequenceno = " + protocolHistorySeqNo
						+ " where stablename='protocolhistory';";

				if (!lstProtocolFile.isEmpty()) {
					final int protocolFileCode = lstProtocolFile.get(0).getNprotocolfilecode();
					final int totalFileCode = protocolFileSeqNo + protocolFileCode;
					protocolInsert += "update seqnostability set nsequenceno = " + totalFileCode
							+ " where stablename='protocolfile'";

				}
				jdbcTemplate.execute(protocolInsert);

				return getProtocolData(inputMap, userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);

			}
		}

	}

	private Protocol getProtocolByName(String sprotocolName, UserInfo userInfo) throws Exception {
		final String strQuery = "select nprotocolcode from protocol where sprotocolname=N'"
				+ stringUtilityFunction.replaceQuote(sprotocolName) + "'" + " and nsitecode = "
				+ userInfo.getNmastersitecode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return (Protocol) jdbcUtilityFunction.queryForObject(strQuery, Protocol.class, jdbcTemplate);
	}

	private Protocol checkProtocolIsPresent(int nprotocolcode, UserInfo userInfo) throws Exception {
		String strQuery = "select nprotocolcode from protocol where nprotocolcode = " + nprotocolcode
				+ " and nsitecode="+userInfo.getNmastersitecode()+" and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		Protocol objProtocol = (Protocol) jdbcUtilityFunction.queryForObject(strQuery, Protocol.class, jdbcTemplate);
		return objProtocol;
	}

	private List<Map<String, Object>> getProtocolAuditGet(int nprotocolcode, int ndesigntemplatemappingcode,
			int approveconfversioncode, UserInfo userInfo) throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();
		List<Map<String, Object>> lstSampleGet = new ArrayList<>();
		String Query = " select json_agg(a.jsonuidata)  from (select " + " spv.jsonuidata "
				+ " ||json_build_object('nprotocolcode',sp.nprotocolcode)::jsonb "
				+ " ||json_build_object('sprotocolid',sp.sprotocolid)::jsonb "
				+ " ||json_build_object('ndesigntemplatemappingcode',spv.ndesigntemplatemappingcode)::jsonb "
				+ " ||json_build_object('napproveconfversioncode',spv.napproveconfversioncode)::jsonb "
				+ " ||json_build_object('ntransactionstatus',spv.ntransactionstatus)::jsonb "
				+ " ||json_build_object('stransdisplaystatus',coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',ts.jsondata->'stransdisplaystatus'->>'en-US'))::jsonb "
				+ " as jsonuidata " + " from protocol sp "
				+ " join protocolversion spv on spv.nprotocolcode =sp.nprotocolcode  "
				+ " join transactionstatus ts on ts.ntranscode =spv.ntransactionstatus  " + " where  "
				+ " sp.nprotocolcode =" + nprotocolcode + " " + " and spv.ndesigntemplatemappingcode ="
				+ ndesigntemplatemappingcode + " " + " and spv.napproveconfversioncode =" + approveconfversioncode + " "
				+ " and sp.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and spv.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and ts.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sp.nsitecode =" + userInfo.getNmastersitecode() + ")a ";

		String testListString = jdbcTemplate.queryForObject(Query, String.class);
		if (testListString != null) {
			lstSampleGet = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(
					testListString, userInfo, true, ndesigntemplatemappingcode, "protocol");
		}
		map.put("protocol", lstSampleGet);
		return lstSampleGet;

	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> viewProtocolWithFile(Map<String, Object> inputMap, UserInfo objUserInfo)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> value = new LinkedHashMap<String, Object>();
		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObject = new JSONObject();
		List<Map<String, Object>> ins = new ArrayList<>();
		final ProtocolVersion obj = objMapper.convertValue(inputMap.get("viewFile"), ProtocolVersion.class);

		String strQuery = "select * from protocolversion where nprotocolcode = " + obj.getNprotocolcode() + ""
				+ " and nsitecode="+objUserInfo.getNmastersitecode()+" nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		ProtocolVersion objProtocolVersion = (ProtocolVersion) jdbcUtilityFunction.queryForObject(strQuery,
				ProtocolVersion.class, jdbcTemplate);
		if (objProtocolVersion != null) {
			map = ftpUtilityFunction.FileViewUsingFtp(obj.getSsystemfilename(), -1, objUserInfo, "", "");
			value.putAll((Map<String, Object>) inputMap.get("viewFile"));
			ins.add(value);
			jsonAuditObject.put("protocol", ins);
			auditmap.put("nregtypecode", -1);
			auditmap.put("nregsubtypecode", -1);
			auditmap.put("ndesigntemplatemappingcode", objProtocolVersion.getNdesigntemplatemappingcode());
			actionType.put("protocol", "IDS_DOWNLOADFILE");
			auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, objUserInfo);
		}
		return new ResponseEntity<Object>(map, HttpStatus.OK);

	}

	public ResponseEntity<Object> getProduct(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();

		final int nProductCatCode = (int) inputMap.get("nproductcatcode");

		final String productQuery = "select * from product where nproductcatcode = " + nProductCatCode + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="+userInfo.getNmastersitecode()+"";
		final List<Product> lstProduct = jdbcTemplate.query(productQuery, new Product());
		map.put("product", lstProduct);

		return new ResponseEntity<>(map, HttpStatus.OK);
	}

}
