package com.agaramtech.qualis.scheduler.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.basemaster.model.TransactionStatus;
import com.agaramtech.qualis.configuration.model.ApprovalConfigAutoapproval;
import com.agaramtech.qualis.configuration.model.SampleType;
import com.agaramtech.qualis.credential.model.Site;
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
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.product.model.Component;
import com.agaramtech.qualis.registration.model.TestGroupTestForSample;
import com.agaramtech.qualis.scheduler.model.ScheduleMaster;
import com.agaramtech.qualis.scheduler.model.SchedulerSampleDetail;
import com.agaramtech.qualis.scheduler.model.SchedulerSubSampleDetail;
import com.agaramtech.qualis.scheduler.model.SchedulerTestDetail;
import com.agaramtech.qualis.scheduler.model.SchedulerTestParameterDetail;
import com.agaramtech.qualis.testgroup.model.TestGroupTest;
import com.agaramtech.qualis.testgroup.model.TestGroupTestParameter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;

//ALPD-4941 Created SchedulerConfigurationDAOImpl for Scheduler configuration screen
@Transactional(rollbackFor = Exception.class)
@AllArgsConstructor
@Repository
public class SchedulerConfigurationDAOImpl implements SchedulerConfigurationDAO {

	final Log logging = LogFactory.getLog(SchedulerConfigurationDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getSchedulerConfiguration(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<>();

		List<Map<String, Object>> lstSchedulerConfigType = new ArrayList<>();
		List<TransactionStatus> listTransactionstatus = new ArrayList<>();
		List<ApprovalConfigAutoapproval> listApprovalConfigAutoapproval = new ArrayList<>();
		List<ReactRegistrationTemplate> listReactRegistrationTemplate = new ArrayList<ReactRegistrationTemplate>();
		returnMap.putAll(((Map<String, Object>) getSampleType(inputMap, userInfo).getBody()));

		lstSchedulerConfigType = getSchedulerConfigType((short) returnMap.get("nsampletypecode"), userInfo);
		returnMap.put("SchedulerConfigTypeValue", lstSchedulerConfigType.get(0));
		returnMap.put("RealSchedulerConfigTypeValue", lstSchedulerConfigType.get(0));
		listTransactionstatus = getFilterStatus(userInfo);
		returnMap.put("FilterStatusValue", listTransactionstatus.get(0));
		String nfilterstatus;
		if (listTransactionstatus.get(0).getNtransactionstatus() == Enumeration.TransactionStatus.ALL
				.gettransactionstatus()) {
			nfilterstatus = listTransactionstatus.stream()
					.map(transItem -> String.valueOf(transItem.getNtransactionstatus()))
					.collect(Collectors.joining(","));
		} else {
			nfilterstatus = String.valueOf(listTransactionstatus.get(0).getNtransactionstatus());
		}
		returnMap.put("nfilterstatus", nfilterstatus);
		returnMap.put("RealFilterStatusValue", listTransactionstatus.get(0));

		returnMap.put("SchedulerConfigType", lstSchedulerConfigType);
		returnMap.put("FilterStatus", listTransactionstatus);

		returnMap.put("RealSchedulerConfigType", lstSchedulerConfigType);

		if (!lstSchedulerConfigType.isEmpty()) {
			returnMap.put("nsampleschedulerconfigtypecode",
					lstSchedulerConfigType.get(0).get("nsampleschedulerconfigtypecode"));
		}

		returnMap.put("RealFilterStatuslist", listTransactionstatus);

		returnMap.put("checkBoxOperation", 3);

		returnMap.putAll(getDynamicSchedulerConfig(returnMap, userInfo));
		return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
	}

	public List<Map<String, Object>> getSchedulerConfigType(final short nsampletypecode, final UserInfo userInfo)
			throws Exception {
		final String schedulerConfigType = "select s1.nschedulerconfigtypecode, s1.sschedulerconfigtypename,s2.nsampleschedulerconfigtypecode from schedulerconfigtype s1,sampleschedulerconfigtype s2 "
				+ " where s2.nsampletypecode=" + nsampletypecode
				+ " and  s1.nschedulerconfigtypecode=s2.nschedulerconfigtypecode " + " and s2.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + " s1.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		// ALPD-5332 Commented by Abdul for Environmental Scheduler
		// + " and"
		// + " s1.nschedulerconfigtypecode=1";
		return jdbcTemplate.queryForList(schedulerConfigType);
	}

	public List<TransactionStatus> getFilterStatus(final UserInfo userInfo) throws Exception {
		final String filterStatus = "select ntranscode as ntransactionstatus, coalesce(jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "', jsondata->'stransdisplaystatus'->>'en-US') stransdisplaystatus from transactionstatus where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntranscode in ("
				+ Enumeration.TransactionStatus.ALL.gettransactionstatus() + ", "
				+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ", "
				+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + ") order by ntranscode";
		return jdbcTemplate.query(filterStatus, new TransactionStatus());
	}

	// public List<SampleType> getSampleType(final UserInfo userInfo) throws
	// Exception {
	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getSampleType(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<>();

		String finalquery = "select st.nsampletypecode,st.ncategorybasedflowrequired, coalesce(st.jsondata->'sampletypename'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',st.jsondata->'sampletypename'->>'en-US') as ssampletypename,st.nsorter, st.nprojectspecrequired,"
				+ " st.nportalrequired, st.noutsourcerequired, st.ncategorybasedflowrequired "
				+ " from SampleType st,registrationtype rt,registrationsubtype rst,approvalconfig ac,approvalconfigversion acv "
				+ "where "
				//
				+ "st.nsampletypecode not in(" + Enumeration.SampleType.STABILITY.getType() + ","
				+ Enumeration.SampleType.PROTOCOL.getType() + ")  and " + "st.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and st.napprovalconfigview = "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus()
				+ " and st.nsampletypecode=rt.nsampletypecode " + " and rt.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nregtypecode=rst.nregtypecode "
				+ " and rst.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rst.nregsubtypecode=ac.nregsubtypecode " + " and ac.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and acv.napprovalconfigcode=ac.napprovalconfigcode " + " and acv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acv.ntransactionstatus= "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and st.nsitecode= rt.nsitecode and rt.nsitecode=rst.nsitecode and rst.nsitecode=acv.nsitecode "
				+ " and acv.nsitecode=" + userInfo.getNmastersitecode()
				+ " group by st.nsampletypecode,st.nsorter order by st.nsorter; ";
		List<SampleType> lstSampleType = jdbcTemplate.query(finalquery, new SampleType());

		if (!lstSampleType.isEmpty()) {
			int nsampletypecode = lstSampleType.get(0).getNsampletypecode();
			returnMap.put("SampleTypeValue", lstSampleType.get(0));
			returnMap.put("nsampletypecode", lstSampleType.get(0).getNsampletypecode());
			if ((!inputMap.containsKey("advfilterdata"))
					|| (inputMap.containsKey("advfilterdata") && (Boolean) inputMap.get("advfilterdata") == false)) {
				returnMap.put("RealSampleTypeValue", lstSampleType.get(0));
			}
			returnMap
			.putAll(((Map<String, Object>) getRegistrationType(nsampletypecode, inputMap, userInfo).getBody()));
		} else {
			returnMap.put("SampleType", lstSampleType);
			returnMap.put("RegistrationType", null);
			returnMap.put("RegistrationSubType", null);
			returnMap.put("ApprovalConfigVersion", null);
			returnMap.put("DesignTemplateMapping", null);
			if ((!inputMap.containsKey("advfilterdata"))
					|| (inputMap.containsKey("advfilterdata") && (Boolean) inputMap.get("advfilterdata") == false)) {
				returnMap.put("RealSampleTypeList", null);
				returnMap.put("RealRegTypeList", null);
				returnMap.put("RealRegSubTypeList", null);
				returnMap.put("RealApprovalConfigVersionList", null);
				returnMap.put("RealDesignTemplateMappingList", null);
			}
		}
		returnMap.put("SampleType", lstSampleType);
		returnMap.put("RealSampleTypeList", lstSampleType);
		// return jdbcTemplate.query(finalquery, new SampleType());
		return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
	}

	// public List<RegistrationType> getRegistrationType(final int nsampleTypeCode,
	// final UserInfo userInfo)
	// throws Exception {
	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getRegistrationType(final int nsampleTypeCode, Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		String Str = "Select * from sampletype  where  nsampletypecode=" + nsampleTypeCode;

		SampleType obj = (SampleType) jdbcUtilityFunction.queryForObject(Str, SampleType.class, jdbcTemplate);
		String ValidationQuery = "";
		if (obj.getNtransfiltertypecode() != -1 && userInfo.getNusercode() != -1) {
			int nmappingfieldcode = (obj.getNtransfiltertypecode() == 1) ? userInfo.getNdeptcode()
					: userInfo.getNuserrole();
			ValidationQuery = " and rst.nregsubtypecode in( " + "		SELECT rs.nregsubtypecode "
					+ "		FROM registrationsubtype rs "
					+ "		INNER JOIN transactionfiltertypeconfig ttc ON rs.nregsubtypecode = ttc.nregsubtypecode "
					+ "		LEFT JOIN transactionusers tu ON tu.ntransfiltertypeconfigcode = ttc.ntransfiltertypeconfigcode "
					+ "		WHERE ( ttc.nneedalluser = 3  and ttc.nstatus =  "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND ttc.nmappingfieldcode = "
					+ nmappingfieldcode + ") " + "	 OR " + "	( ttc.nneedalluser = 4  "
					+ "	  AND ttc.nmappingfieldcode =" + nmappingfieldcode + "	  AND tu.nusercode ="
					+ userInfo.getNusercode() + "   and ttc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "   and tu.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") " + "  OR "
					+ "	( ttc.nneedalluser = 4  " + " AND ttc.nmappingfieldcode = -1 " + " AND tu.nusercode ="
					+ userInfo.getNusercode() + " and ttc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tu.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") "
					+ "	 AND rs.nregtypecode = rt.nregtypecode) ";
		}
		String finalquery = "select  rt.nregtypecode,coalesce(rt.jsondata->'sregtypename'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',rt.jsondata->'sregtypename'->>'en-US') as sregtypename,rt.nsorter "
				+ "from SampleType st,registrationtype rt,registrationsubtype rst,approvalconfig ac,approvalconfigversion acv"
				+ " where st.nsampletypecode > 0 and st.nsampletypecode =" + nsampleTypeCode + " and st.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and st.napprovalconfigview = "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus()
				+ " and st.nsampletypecode=rt.nsampletypecode " + " and rt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nregtypecode=rst.nregtypecode "
				+ " and rst.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rst.nregsubtypecode=ac.nregsubtypecode " + " and ac.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and acv.napprovalconfigcode=ac.napprovalconfigcode " + " and acv.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acv.ntransactionstatus = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and st.nsitecode= rt.nsitecode and rt.nsitecode=rst.nsitecode and rst.nsitecode=acv.nsitecode "
				+ " and acv.nsitecode=" + userInfo.getNmastersitecode() + " and rt.nregtypecode>0 " + ValidationQuery
				+ " group by rt.nregtypecode,coalesce(rt.jsondata->'sregtypename'->>'" + userInfo.getSlanguagetypecode()
				+ "',rt.jsondata->'sregtypename'->>'en-US'),rt.nsorter order by rt.nregtypecode desc";

		List<RegistrationType> lstRegistrationType = jdbcTemplate.query(finalquery, new RegistrationType());
		if (!lstRegistrationType.isEmpty()) {
			returnMap.put("RegTypeValue", lstRegistrationType.get(0));
			returnMap.put("nregtypecode", lstRegistrationType.get(0).getNregtypecode());
			if ((!inputMap.containsKey("advfilterdata"))
					|| (inputMap.containsKey("advfilterdata") && (Boolean) inputMap.get("advfilterdata") == false)) {
				returnMap.put("RealRegTypeValue", lstRegistrationType.get(0));
			}
			returnMap.putAll(((Map<String, Object>) getRegistrationSubType(lstRegistrationType.get(0).getNregtypecode(),
					inputMap, userInfo).getBody()));
		} else {
			returnMap.put("RegSubTypeValue", null);
			returnMap.put("ApprovalConfigVersionValue", null);
			returnMap.put("FilterStatusValue", null);
			if ((!inputMap.containsKey("advfilterdata"))
					|| (inputMap.containsKey("advfilterdata") && (Boolean) inputMap.get("advfilterdata") == false)) {
				returnMap.put("RealRegSubTypeValue", null);
				returnMap.put("RealRegSubTypeValueList", null);
				returnMap.put("RealApprovalConfigVersionValue", null);
				returnMap.put("RealApprovalConfigVersionValueList", null);
				returnMap.put("RealFilterStatusValue", null);
				returnMap.put("RealFilterStatusValuelist", null);
			}
		}
		// ALPD-5332 Added by Abdul for Material Scheduler
		List<Map<String, Object>> lstSchedulerConfigType = new ArrayList<>();
		lstSchedulerConfigType = getSchedulerConfigType((short) nsampleTypeCode, userInfo);
		returnMap.put("SchedulerConfigTypeValue", lstSchedulerConfigType.get(0));
		returnMap.put("RealSchedulerConfigTypeValue", lstSchedulerConfigType.get(0));
		returnMap.put("SchedulerConfigType", lstSchedulerConfigType);
		returnMap.put("RealSchedulerConfigType", lstSchedulerConfigType);
		if (!lstSchedulerConfigType.isEmpty()) {
			returnMap.put("nsampleschedulerconfigtypecode",
					lstSchedulerConfigType.get(0).get("nsampleschedulerconfigtypecode"));
		}
		returnMap.put("RegistrationType", lstRegistrationType);
		returnMap.put("RealRegTypeList", lstRegistrationType);
		// return jdbcTemplate.query(finalquery, new RegistrationType());
		return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
	}

	// public List<RegistrationSubType> getRegistrationSubType(final int
	// nregTypeCode, final UserInfo userInfo)
	// throws Exception {
	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getRegistrationSubType(final int nregTypeCode, Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		String Str = "Select * from registrationtype rt,sampletype st where rt.nsampletypecode=st.nsampletypecode and rt.nregTypeCode="
				+ nregTypeCode;

		SampleType obj = (SampleType) jdbcUtilityFunction.queryForObject(Str, SampleType.class, jdbcTemplate);
		String validationQuery = "";
		if (obj.getNtransfiltertypecode() != -1 && userInfo.getNusercode() != -1) {
			int nmappingfieldcode = (obj.getNtransfiltertypecode() == 1) ? userInfo.getNdeptcode()
					: userInfo.getNuserrole();
			validationQuery = " and rst.nregsubtypecode in( " + " SELECT rs.nregsubtypecode "
					+ " FROM registrationsubtype rs "
					+ " INNER JOIN transactionfiltertypeconfig ttc ON rs.nregsubtypecode = ttc.nregsubtypecode "
					+ " LEFT JOIN transactionusers tu ON tu.ntransfiltertypeconfigcode = ttc.ntransfiltertypeconfigcode "
					+ " WHERE ( ttc.nneedalluser = 3  and ttc.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND ttc.nmappingfieldcode = "
					+ nmappingfieldcode + ")" + "  OR " + "	( ttc.nneedalluser = 4  " + " AND ttc.nmappingfieldcode ="
					+ nmappingfieldcode + " AND tu.nusercode =" + userInfo.getNusercode() + " and ttc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tu.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") " + "  OR "
					+ "	( ttc.nneedalluser = 4  " + " AND tu.nusercode =" + userInfo.getNusercode()
					+ " AND ttc.nmappingfieldcode = -1 " + " and ttc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tu.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") " + " AND rs.nregtypecode = "
					+ nregTypeCode + ")";
		}
		String finalquery = "select max(rsc.nregsubtypeversioncode) nregsubtypeversioncode,"
				+ " max(rsc.jsondata->>'nneedsubsample' )::Boolean nneedsubsample,"
				+ " max(rsc.jsondata->>'nneedjoballocation' )::Boolean nneedjoballocation,"
				+ " max(rsc.jsondata->>'nneedmyjob' )::Boolean nneedmyjob,"
				+ " max(rsc.jsondata->>'nneedtestinitiate')::Boolean nneedtestinitiate,"
				+ " max(rsc.jsondata->>'nneedtemplatebasedflow' )::Boolean nneedtemplatebasedflow,rst.nregsubtypecode,"
				+ " coalesce(rst.jsondata->'sregsubtypename'->>'" + userInfo.getSlanguagetypecode()
				+ "',rst.jsondata->'sregsubtypename'->>'en-US') as sregsubtypename,rst.nsorter "
				+ ", max(rsc.jsondata->>'ntestgroupspecrequired')::Boolean ntestgroupspecrequired "
				+ " from SampleType st,registrationtype rt,registrationsubtype rst,approvalconfig ac,approvalconfigversion acv,regsubtypeconfigversion rsc "
				+ " where st.nsampletypecode > 0 and st.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and st.napprovalconfigview = "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " "
				+ " and st.nsampletypecode=rt.nsampletypecode and rt.nregtypecode = " + nregTypeCode
				+ " and rt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rt.nregtypecode=rst.nregtypecode and rst.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rst.nregsubtypecode=ac.nregsubtypecode and ac.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rsc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and acv.napprovalconfigcode=ac.napprovalconfigcode and acv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acv.ntransactionstatus = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ "  and rst.nregsubtypecode>0 and rsc.napprovalconfigcode=ac.napprovalconfigcode and rsc.ntransactionstatus= "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and st.nsitecode= rt.nsitecode and rt.nsitecode=rst.nsitecode and rst.nsitecode=acv.nsitecode "
				+ " and acv.nsitecode=rsc.nsitecode and rsc.nsitecode=" + userInfo.getNmastersitecode()
				+ validationQuery + " group by rst.nregsubtypecode,coalesce(rst.jsondata->'sregsubtypename'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',rst.jsondata->'sregsubtypename'->>'en-US'),rst.nsorter order by rst.nregsubtypecode desc";

		List<RegistrationSubType> lstRegistrationSubType = jdbcTemplate.query(finalquery, new RegistrationSubType());
		if (!lstRegistrationSubType.isEmpty()) {
			returnMap.put("nregsubtypecode", lstRegistrationSubType.get(0).getNregsubtypecode());
			returnMap.put("nregsubtypeversioncode", lstRegistrationSubType.get(0).getNregsubtypeversioncode());
			returnMap.put("nneedsubsample", lstRegistrationSubType.get(0).isNneedsubsample());
			returnMap.put("RegSubTypeValue", lstRegistrationSubType.get(0));
			if ((!inputMap.containsKey("advfilterdata"))
					|| (inputMap.containsKey("advfilterdata") && (Boolean) inputMap.get("advfilterdata") == false)) {
				returnMap.put("RealRegSubTypeValue", lstRegistrationSubType.get(0));
			}
			returnMap.putAll(((Map<String, Object>) getApprovalConfigVersion(nregTypeCode,
					lstRegistrationSubType.get(0).getNregsubtypecode(), inputMap, userInfo).getBody()));
		} else {
			returnMap.put("ApprovalConfigVersionValue", null);
			returnMap.put("FilterStatusValue", null);
			if ((!inputMap.containsKey("advfilterdata"))
					|| (inputMap.containsKey("advfilterdata") && (Boolean) inputMap.get("advfilterdata") == false)) {
				returnMap.put("RealApprovalConfigVersionValue", null);
				returnMap.put("RealApprovalConfigVersionValueList", null);
				returnMap.put("RealFilterStatusValue", null);
				returnMap.put("RealFilterStatusValueList", null);
			}
		}
		returnMap.put("RegistrationSubType", lstRegistrationSubType);
		returnMap.put("RealRegSubTypeList", lstRegistrationSubType);
		// return jdbcTemplate.query(finalquery, new RegistrationSubType());
		return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
	}

	// public List<ApprovalConfigAutoapproval> getApprovalConfigVersion(int
	// nregTypeCode, int nregSubTypeCode,
	// UserInfo userInfo) throws Exception {
	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getApprovalConfigVersion(int nregTypeCode, int nregSubTypeCode,
			Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Map<String, Object> returnMap = new HashMap<>();

		final String getApprovalConfigVersion = "select aca.sversionname,aca.napprovalconfigcode,"
				+ " aca.napprovalconfigversioncode ,acv.ntransactionstatus,acv.ntreeversiontempcode,acv.napproveconfversioncode "
				+ " from   approvalconfigautoapproval aca," + " approvalconfig ac,approvalconfigversion acv "
				+ " where   acv.napproveconfversioncode=aca.napprovalconfigversioncode and ac.napprovalconfigcode=aca.napprovalconfigcode and ac.napprovalconfigcode=acv.napprovalconfigcode "
				+ " and ac.nregtypecode =" + nregTypeCode + " and ac.nregsubtypecode =" + nregSubTypeCode
				+ " and acv.ntransactionstatus not in ( " + Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
				+ ") and acv.nsitecode =" + userInfo.getNmastersitecode() + " and ac.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and aca.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acv.nsitecode="
				+ userInfo.getNmastersitecode() + " group by acv.ntransactionstatus,aca.napprovalconfigversioncode,"
				+ "acv.ntreeversiontempcode,aca.napprovalconfigcode,aca.sversionname,acv.napproveconfversioncode order by aca.napprovalconfigversioncode desc";

		final List<ApprovalConfigAutoapproval> approvalVersion = jdbcTemplate.query(getApprovalConfigVersion,
				new ApprovalConfigAutoapproval());

		if (!approvalVersion.isEmpty()) {
			returnMap.put("ApprovalConfigVersionValue", approvalVersion.get(0));
			returnMap.put("napproveconfversioncode", approvalVersion.get(0).getNapproveconfversioncode());
			if ((!inputMap.containsKey("advfilterdata"))
					|| (inputMap.containsKey("advfilterdata") && (Boolean) inputMap.get("advfilterdata") == false)) {
				returnMap.put("RealApprovalConfigVersionValue", approvalVersion.get(0));
			}
			returnMap.putAll(((Map<String, Object>) getApproveConfigVersionRegTemplate(nregTypeCode, nregSubTypeCode,
					approvalVersion.get(0).getNapproveconfversioncode(), inputMap, userInfo).getBody()));
		} else {
			returnMap.put("ApprovalConfigVersionValue", null);
			if ((!inputMap.containsKey("advfilterdata"))
					|| (inputMap.containsKey("advfilterdata") && (Boolean) inputMap.get("advfilterdata") == false)) {
				returnMap.put("RealApprovalConfigVersionValue", null);
				returnMap.put("RealApprovalConfigVersionValueList", null);
			}
		}
		returnMap.put("ApprovalConfigVersion", approvalVersion);
		returnMap.put("RealApprovalConfigVersionList", approvalVersion);
		// return approvalVersion;
		return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getApproveConfigVersionRegTemplate(final int nregTypeCode, final int nregSubTypeCode,
			final int napproveConfigVersionCode, Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		Map<String, Object> returnMap = new HashMap<>();
		final String str = "select dm.ndesigntemplatemappingcode,rt.jsondata,CONCAT(rt.sregtemplatename,"
				+ " '(',cast(dm.nversionno as character varying),')') sregtemplatename from designtemplatemapping dm, "
				+ " reactregistrationtemplate rt,approvalconfigversion acv  where  "
				+ " acv.ndesigntemplatemappingcode=dm.ndesigntemplatemappingcode  and  "
				+ " rt.nreactregtemplatecode=dm.nreactregtemplatecode and  dm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  acv.napproveconfversioncode="
				+ napproveConfigVersionCode;

		final List<ReactRegistrationTemplate> lstReactRegistrationTemplate = jdbcTemplate.query(str,
				new ReactRegistrationTemplate());

		if (!lstReactRegistrationTemplate.isEmpty()) {
			returnMap.put("registrationTemplate", lstReactRegistrationTemplate.get(0));
			returnMap.put("DesignTemplateMappingValue", lstReactRegistrationTemplate.get(0));
			if ((!inputMap.containsKey("advfilterdata"))
					|| (inputMap.containsKey("advfilterdata") && (Boolean) inputMap.get("advfilterdata") == false)) {
				returnMap.put("RealDesignTemplateMappingValue", lstReactRegistrationTemplate.get(0));
			}
			returnMap.put("ndesigntemplatemappingcode",
					lstReactRegistrationTemplate.get(0).getNdesigntemplatemappingcode());
			returnMap.put("SubSampleTemplate", getSchedulerConfigSubSampleTemplate(
					lstReactRegistrationTemplate.get(0).getNdesigntemplatemappingcode()).getBody());
			returnMap.put("DynamicDesign", projectDAOSupport.getTemplateDesign(
					lstReactRegistrationTemplate.get(0).getNdesigntemplatemappingcode(), userInfo.getNformcode()));
		} else {
			returnMap.put("DesignTemplateMappingValue", null);
			returnMap.put("ndesigntemplatemappingcode", null);
			if ((!inputMap.containsKey("advfilterdata"))
					|| (inputMap.containsKey("advfilterdata") && (Boolean) inputMap.get("advfilterdata") == false)) {
				returnMap.put("RealDesignTemplateMappingValue", null);
				returnMap.put("RealDesignTemplateMappingValueList", null);
			}
		}
		returnMap.put("DesignTemplateMapping", lstReactRegistrationTemplate);
		returnMap.put("RealDesignTemplateMappingList", lstReactRegistrationTemplate);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getSchedulerConfigByFilterSubmit(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		int nsampletypecode = -1;
		int nregtype = -1;
		int nregsubtype = -1;
		int ndesigntemplatemappingcode = -1;

		if (inputMap.containsKey("nsampletypecode")) {
			nsampletypecode = (int) inputMap.get("nsampletypecode");
		}
		if (inputMap.containsKey("nregtypecode")) {
			nregtype = (int) inputMap.get("nregtypecode");
		}
		if (inputMap.containsKey("nregsubtypecode")) {
			nregsubtype = (int) inputMap.get("nregsubtypecode");
		}
		if (inputMap.containsKey("ndesigntemplatemappingcode")) {
			ndesigntemplatemappingcode = (int) inputMap.get("ndesigntemplatemappingcode");
		}

		ReactRegistrationTemplate lstTemplate = (ReactRegistrationTemplate) getSchedulerConfigTemplate(nregtype,
				nregsubtype).getBody();
		if (lstTemplate != null) {
			returnMap.put("ndesigntemplatemappingcode", lstTemplate.getNdesigntemplatemappingcode());
		}

		ReactRegistrationTemplate lstTemplate1 = (ReactRegistrationTemplate) getSchedulerConfigTemplatebasedontemplatecode(
				nregtype, nregsubtype, ndesigntemplatemappingcode).getBody();

		if (lstTemplate1 != null) {
			returnMap.put("registrationTemplate", lstTemplate1);
			returnMap.put("SubSampleTemplate",
					getSchedulerConfigSubSampleTemplate(ndesigntemplatemappingcode).getBody());
			returnMap.put("DynamicDesign",
					projectDAOSupport.getTemplateDesign(ndesigntemplatemappingcode, userInfo.getNformcode()));
			returnMap.putAll(getDynamicSchedulerConfig(inputMap, userInfo));
		} else {
			returnMap.put("registrationTemplate", new ArrayList<>());
			returnMap.put("SubSampleTemplate", new ArrayList<>());
			returnMap.put("DynamicDesign", null);
			returnMap.put("selectedSample", Arrays.asList());
			returnMap.put("selectedSubSample", Arrays.asList());
			returnMap.put("SchedulerConfigGetSubSample", new ArrayList<>());
			returnMap.put("SchedulerConfigGetSample", new ArrayList<>());
			returnMap.put("SchedulerConfigGetTest", new ArrayList<>());
			returnMap.put("selectedTest", Arrays.asList());
			returnMap.put("SchedulerConfigurationParameter", Arrays.asList());
			returnMap.put("ndesigntemplatemappingcode", null);
		}

		if (inputMap.containsKey("flag")) {
			returnMap.put("RegistrationType", getRegistrationTypeFilter(nsampletypecode, inputMap, userInfo));
			returnMap.put("RegistrationSubType", getRegistrationSubTypeFilter(nregtype, inputMap, userInfo));
		}

		return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getSchedulerConfigTemplate(int nregTypeCode, int nregSubTypeCode) throws Exception {
		// TODO Auto-generated method stub

		final String str = "select dm.ndesigntemplatemappingcode,rt.jsondata, rt.ndefaulttemplatecode from designtemplatemapping dm, "
				+ " reactregistrationtemplate rt where nregtypecode=" + nregTypeCode + " and nregsubtypecode="
				+ nregSubTypeCode + " and rt.nreactregtemplatecode=dm.nreactregtemplatecode and " + " dm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and  dm.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus();

		final ReactRegistrationTemplate lstReactRegistrationTemplate = (ReactRegistrationTemplate) jdbcUtilityFunction
				.queryForObject(str, ReactRegistrationTemplate.class, jdbcTemplate);

		return new ResponseEntity<>(lstReactRegistrationTemplate, HttpStatus.OK);

	}

	public ResponseEntity<Object> getSchedulerConfigTemplatebasedontemplatecode(int nregTypeCode, int nregSubTypeCode,
			int ndesignTemplateCode) throws Exception {
		// TODO Auto-generated method stub

		final String str = "select dm.ndesigntemplatemappingcode,rt.jsondata from designtemplatemapping dm, "
				+ " reactregistrationtemplate rt where nregtypecode=" + nregTypeCode + " and nregsubtypecode="
				+ nregSubTypeCode + " and rt.nreactregtemplatecode=dm.nreactregtemplatecode and " + " dm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and dm.ndesigntemplatemappingcode="
				+ ndesignTemplateCode;

		final ReactRegistrationTemplate lstReactRegistrationTemplate = (ReactRegistrationTemplate) jdbcUtilityFunction
				.queryForObject(str, ReactRegistrationTemplate.class, jdbcTemplate);

		return new ResponseEntity<>(lstReactRegistrationTemplate, HttpStatus.OK);

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Object> getDynamicSchedulerConfig(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> objMap = new HashMap();
		String nfilterstatus = "";
		if (inputMap.containsKey("nfilterstatus")) {
			if (inputMap.get("nfilterstatus") != null) {
				nfilterstatus = inputMap.get("nfilterstatus").toString();
			}
		}
		int napproveconfversioncode = -1;
		String sschedulersamplecode1 = "";
		if (inputMap.containsKey("nschedulersamplecode")) {
			sschedulersamplecode1 = " and nschedulersamplecode=" + String.valueOf(inputMap.get("nschedulersamplecode"))
			+ "";
		}
		if (inputMap.containsKey("napproveconfversioncode")) {
			napproveconfversioncode = (int) inputMap.get("napproveconfversioncode");
		}
		final String registrationMultilingual = commonFunction.getMultilingualMessage("IDS_REGNO",
				userInfo.getSlanguagefilename());

		String strSampleQry = "select json_agg(a.jsonuidata) from (select ssd.nschedulersamplecode, ssd.nallottedspeccode, "
				+ " ssd.dtransactiondate, ssd.ntransactionstatus, cm.ncolorcode, cm.scolorhexcode,"
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "', ts.jsondata->'stransdisplaystatus'->>'en-US') stransdisplaystatus,"
				+ " ssd.jsonuidata || json_build_object('sactiveststaus', coalesce(ts1.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + ""
				+ "', ts1.jsondata->'stransdisplaystatus'->>'en-US'),    'dtransactiondate',ssd.jsonuidata->>'Transaction Date','stransdisplaystatus', coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "', ts.jsondata->'stransdisplaystatus'->>'en-US'),'nactivestatus', ssd.nactivestatus, 'ntransactionstatus', ssd.ntransactionstatus, 'scolorhexcode', cm.scolorhexcode,'ssecondcolorhexcode', cm1.scolorhexcode,'nsecondcolorcode',cm1.ncolorcode, 'ncolorcode',"
				+ " cm.ncolorcode,'sarno',concat(cast(ssd.nschedulersamplecode as text),' ','"
				+ registrationMultilingual
				+ "'),'nschedulecode',ssd.nschedulecode)::jsonb as jsonuidata from schedulersampledetail ssd,sampleschedulerconfigtype sct, formwisestatuscolor fwsc, transactionstatus ts, colormaster cm,formwisestatuscolor fwsc1, transactionstatus ts1, colormaster cm1"
				+ " where ssd.ntransactionstatus=ts.ntranscode and ssd.nactivestatus=fwsc1.ntranscode and ssd.ntransactionstatus=fwsc.ntranscode and fwsc1.nformcode="
				+ userInfo.getNformcode() + "  and fwsc.nformcode=" + userInfo.getNformcode()
				+ " and  ssd.nactivestatus = ts1.ntranscode and fwsc1.ncolorcode=cm1.ncolorcode and fwsc.ncolorcode=cm.ncolorcode and ssd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and fwsc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ssd.nsitecode="
				+ userInfo.getNmastersitecode() + " and ssd.nregtypecode=" + inputMap.get("nregtypecode")
				+ " and ssd.nregsubtypecode=" + inputMap.get("nregsubtypecode") + " and ssd.ndesigntemplatemappingcode="
				+ inputMap.get("ndesigntemplatemappingcode") + " and nregsubtypeversioncode="
				+ inputMap.get("nregsubtypeversioncode") + " and ntransactionstatus in(" + nfilterstatus
				+ ") and   sct.nsampleschedulerconfigtypecode=ssd.nsampleschedulerconfigtypecode "
				+ " and  ssd.nsampleschedulerconfigtypecode=" + inputMap.get("nsampleschedulerconfigtypecode") + "   "
				+ sschedulersamplecode1 + " and (ssd.jsonuidata->>'napproveconfversioncode')::integer="
				+ napproveconfversioncode + " order by ssd.nschedulersamplecode) a";

		logging.info("Scheduler Config Sample Start========?" + LocalDateTime.now());
		String lstData1 = jdbcTemplate.queryForObject(strSampleQry, String.class);

		logging.info("Scheduler Config Sample end========?" + strSampleQry + " :" + LocalDateTime.now());

		List<Map<String, Object>> lstData = new ArrayList();

		if (lstData1 != null) {
			lstData = projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(lstData1,
					userInfo, true, (int) inputMap.get("ndesigntemplatemappingcode"), "sample");

			logging.info("Scheduler Config Sample Size" + lstData.size());

			objMap.put("SchedulerConfigGetSample", lstData);

			String sschedulersamplecode = "";
			if (!inputMap.containsKey("ntype")) {
				sschedulersamplecode = String.valueOf(lstData.get(lstData.size() - 1).get("nschedulersamplecode"));
				inputMap.put("nschedulersamplecode", sschedulersamplecode);
				objMap.put("selectedSample", Arrays.asList(lstData.get(lstData.size() - 1)));

			} else {
				objMap.put("selectedSample", lstData);
			}

			Map<String, Object> map = (Map<String, Object>) getSchedulerConfigSubSample(inputMap, userInfo).getBody();
			objMap.putAll(map);

		} else {
			objMap.put("selectedSample", lstData);
			objMap.put("SchedulerConfigGetSample", lstData);
			objMap.put("selectedSubSample", lstData);
			objMap.put("SchedulerConfigGetSubSample", lstData);
			objMap.put("SchedulerConfigGetTest", lstData);
			objMap.put("selectedTest", lstData);
			objMap.put("RegistrationParameter", lstData);
			objMap.put("RegistrationAttachment", new ArrayList<>());
			objMap.put("RegistrationSampleAttachment", new ArrayList<>());
			objMap.put("RegistrationTestAttachment", new ArrayList<>());
			objMap.put("RegistrationComment", new ArrayList<>());
			objMap.put("RegistrationSampleComment", new ArrayList<>());
			objMap.put("RegistrationTestComment", new ArrayList<>());
		}
		return objMap;

	}

	@Override
	public ResponseEntity<Object> getSchedulerConfigSubSample(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> objMap = new HashMap();

		boolean subSample = (boolean) inputMap.get("nneedsubsample");
		String sschedulersamplecode = "";

		if (inputMap.containsKey("nschedulersubsamplecode")) {
			if (inputMap.get("nschedulersubsamplecode") != null && inputMap.get("nschedulersubsamplecode") != "") {
				sschedulersamplecode = " and nschedulersubsamplecode in("
						+ (String) inputMap.get("nschedulersubsamplecode") + ")";
			}
		}
		final String schedulerMultilingual = commonFunction.getMultilingualMessage("IDS_REGNO",
				userInfo.getSlanguagefilename());
		final String schedulerSubMultilingual = commonFunction.getMultilingualMessage("IDS_SAMPLENO",
				userInfo.getSlanguagefilename());

		String strSubSampleQry = "select json_agg(a.jsonuidata) from (select sssd.nschedulersubsamplecode, cm.ncolorcode, cm.scolorhexcode,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "', ts.jsondata->'stransdisplaystatus'->>'en-US') stransdisplaystatus, sssd.jsonuidata"
				+ " || json_build_object('stransdisplaystatus', coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + ""
				+ "', ts.jsondata->'stransdisplaystatus'->>'en-US'), 'ntransactionstatus', ssd.ntransactionstatus, 'scolorhexcode', cm.scolorhexcode, 'ncolorcode',"
				+ " cm.ncolorcode,'nspecsampletypecode',sssd.nspecsampletypecode,'dtransactiondate', to_char(sssd.dtransactiondate, 'YYYY-MM-DD HH24:MI:SS'),'sarno',ssd.nschedulersamplecode,'ssamplearno',concat(cast(nschedulersubsamplecode as text),' ','"
				+ schedulerSubMultilingual + "','/',cast(sssd.nschedulersamplecode as text),' ','"
				+ schedulerMultilingual + "'))::jsonb"
				+ " as jsonuidata from schedulersubsampledetail sssd,schedulersampledetail ssd,formwisestatuscolor fwsc, transactionstatus ts, colormaster cm  where ssd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ssd.nsitecode="
				+ userInfo.getNmastersitecode() + " and fwsc.ncolorcode=cm.ncolorcode and fwsc.nformcode="
				+ userInfo.getNformcode()
				+ " and ssd.ntransactionstatus=ts.ntranscode and ssd.ntransactionstatus=fwsc.ntranscode and sssd.nschedulersamplecode=ssd.nschedulersamplecode  "
				+ " and sssd.nschedulersamplecode in (" + inputMap.get("nschedulersamplecode") + ") "
				+ sschedulersamplecode + " and sssd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ssd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  order by 1 ) a";

		logging.info("sub sample Start========?" + LocalDateTime.now());
		logging.info("sub sample query:" + strSubSampleQry);

		final String lstData1 = jdbcTemplate.queryForObject(strSubSampleQry, String.class);
		logging.info("sub sample end========?" + LocalDateTime.now());

		if (lstData1 != null) {

			List<Map<String, Object>> lstData = projectDAOSupport
					.getSiteLocalTimeFromUTCForDynamicTemplate(lstData1, userInfo, true,
							(int) inputMap.get("ndesigntemplatemappingcode"), "subsample");

			objMap.put("SchedulerConfigGetSubSample", lstData);

			// objMap.putAll(getActiveSampleTabData((String) inputMap.get("npreregno"), "0",
			// (String) inputMap.get("activeSampleTab"), userInfo));
			// objMap.put("activeSampleTab", inputMap.get("activeSampleTab"));

			if (!subSample || (int) inputMap.get("checkBoxOperation") == 3
					|| (int) inputMap.get("checkBoxOperation") == 7) {
				String sschedulersubsamplecode = "";
				if (!inputMap.containsKey("ntype")) {
					sschedulersubsamplecode = String
							.valueOf(lstData.get(lstData.size() - 1).get("nschedulersubsamplecode"));
					inputMap.put("nschedulersubsamplecode", sschedulersubsamplecode);
					objMap.put("selectedSubSample", Arrays.asList(lstData.get(lstData.size() - 1)));
				} else if ((int) inputMap.get("ntype") == 2) {
					sschedulersubsamplecode = lstData.stream()
							.map(x -> String.valueOf(x.get("nschedulersubsamplecode")))
							.collect(Collectors.joining(","));
					inputMap.put("nschedulersubsamplecode", sschedulersubsamplecode);
					objMap.put("selectedSubSample", lstData);
				} else if ((int) inputMap.get("ntype") == 3) {
					String nschedulersubsamplecode = (String) inputMap.get("nschedulersubsamplecode");
					sschedulersubsamplecode = nschedulersubsamplecode;
					List<String> myList = new ArrayList<String>(Arrays.asList(nschedulersubsamplecode.split(",")));
					lstData = lstData.stream()
							.filter(x -> myList.stream()
									.anyMatch(y -> y.equals(String.valueOf(x.get("nschedulersubsamplecode")))))
							.collect(Collectors.toList());
					if (inputMap.containsKey("istestschedulerdelete")) {
						inputMap.put("ntype", 5);
					} else {
						inputMap.put("ntype", 4);

					}
					objMap.put("selectedSubSample", lstData);
				} else if ((int) inputMap.get("ntype") == 5) {
					sschedulersubsamplecode = (String) inputMap.get("nschedulersubsamplecode");
					objMap.put("selectedSubSample", lstData);
				}

				else {
					String sschedulerSubSampleCode = String
							.valueOf(lstData.get(lstData.size() - 1).get("nschedulersubsamplecode"));
					objMap.put("selectedSubSample", Arrays.asList(lstData.get(lstData.size() - 1)));
					inputMap.put("nschedulersubsamplecode", sschedulerSubSampleCode);
					if ((int) inputMap.get("checkBoxOperation") == 7 && (int) inputMap.get("ntype") == 4) {
						inputMap.put("ntype", 5);
					}

				}

			}

			Map<String, Object> map = (Map<String, Object>) getSchedulerConfigTest(inputMap, userInfo).getBody();
			objMap.putAll(map);

		} else {
			objMap.put("selectedSubSample", Arrays.asList());
			if (!subSample || (int) inputMap.get("checkBoxOperation") == 3) {
				objMap.put("selectedTest", Arrays.asList());
				objMap.put("SchedulerConfigGetTest", Arrays.asList());
				objMap.put("SchedulerConfigGetSubSample", Arrays.asList());
				objMap.put("activeTestTab", "IDS_PARAMETERRESULTS");
			}
		}
		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getSchedulerConfigTest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> objMap = new HashMap();
		// String stransactioncode = "";
		boolean subSample = false;
		if (inputMap.containsKey("nneedsubsample")) {
			subSample = (boolean) inputMap.get("nneedsubsample");
		}

		String sschedulertestcode = "";
		if (inputMap.containsKey("nschedulertestcode")) {
			if (inputMap.get("nschedulertestcode") != null) {
				sschedulertestcode = " and nschedulertestcode in(" + (String) inputMap.get("nschedulertestcode") + ")";
			}
		}

		final String schedulerMultilingual = commonFunction.getMultilingualMessage("IDS_REGNO",
				userInfo.getSlanguagefilename());
		final String schedulerSubMultilingual = commonFunction.getMultilingualMessage("IDS_SAMPLENO",
				userInfo.getSlanguagefilename());

		String strTestQry = "select json_agg(a.jsondata) from (select nschedulertestcode,ssd.ntransactionstatus, cm.ncolorcode, cm.scolorhexcode,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "', ts.jsondata->'stransdisplaystatus'->>'en-US') stransdisplaystatus, "
				+ "std.jsondata || json_build_object('stransdisplaystatus', coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US'),'ntransactionstatus', ssd.ntransactionstatus, 'scolorhexcode', cm.scolorhexcode, 'ncolorcode', cm.ncolorcode,   "
				+ " 'sarno',concat(cast(std.nschedulersamplecode as text),' ','" + schedulerMultilingual + "'),"
				+ "'ssamplearno',concat(cast(std.nschedulersubsamplecode as text),' ','" + schedulerSubMultilingual
				+ "'))::jsonb as jsondata  from schedulertestdetail std,schedulersampledetail ssd,"
				+ " schedulersubsampledetail sssd, formwisestatuscolor fwsc, transactionstatus ts, colormaster cm"
				+ " where std.nschedulersubsamplecode=sssd.nschedulersubsamplecode "
				+ "	and std.nschedulersamplecode=sssd.nschedulersamplecode "
				+ "	and std.nschedulersamplecode=ssd.nschedulersamplecode "
				+ "	and ssd.nschedulersamplecode=sssd.nschedulersamplecode and ssd.ntransactionstatus=ts.ntranscode and  ssd.ntransactionstatus=fwsc.ntranscode and  fwsc.nformcode="
				+ userInfo.getNformcode() + " and fwsc.ncolorcode=cm.ncolorcode and " + " std.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ssd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + " sssd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "    and std.nsitecode="
				+ userInfo.getNmastersitecode() + " and std.nschedulersubsamplecode in ("
				+ inputMap.get("nschedulersubsamplecode") + ") " + sschedulertestcode + "  order by 1)a";

		logging.info("test Start========?" + LocalDateTime.now());

		final String lstData2 = jdbcTemplate.queryForObject(strTestQry, String.class);
		logging.info("schedulertestdetailget:" + strTestQry);
		logging.info("test end========?" + LocalDateTime.now());
		if (lstData2 != null) {

			ObjectMapper obj = new ObjectMapper();
			List<Map<String, Object>> lstData = projectDAOSupport
					.getSiteLocalTimeFromUTCForDynamicTemplate(lstData2, userInfo, true,
							(int) inputMap.get("ndesigntemplatemappingcode"), "test");
			objMap.put("SchedulerConfigGetTest", lstData);
			String nschedulertestcode = "";
			if (inputMap.containsKey("ntype")) {
				if ((int) inputMap.get("ntype") == 1) {
					List<Map<String, Object>> lstData1 = obj.convertValue(inputMap.get("schedulerconfigtest"),
							new TypeReference<List<Map<String, Object>>>() {
					});
					List<Map<String, Object>> filteredList = lstData.stream()
							.filter(empl -> lstData1.stream()
									.anyMatch(dept -> String.valueOf(dept.get("nschedulertestcode"))
											.equals(String.valueOf(empl.get("nschedulertestcode")))))
							.collect(Collectors.toList());

					nschedulertestcode = filteredList.stream().map(x -> String.valueOf(x.get("nschedulertestcode")))
							.collect(Collectors.joining(","));
					objMap.put("selectedTest", Arrays.asList(lstData.get(lstData.size() - 1)));
				} else if ((int) inputMap.get("ntype") == 2) {

					nschedulertestcode = lstData.stream().map(x -> String.valueOf(x.get("nschedulertestcode")))
							.collect(Collectors.joining(","));
					objMap.put("selectedTest", lstData);
				}

				else if ((int) inputMap.get("ntype") == 3) {
					nschedulertestcode = (String) inputMap.get("nschedulertestcode");
					objMap.put("selectedTest", lstData);
				} else if ((int) inputMap.get("ntype") == 4) {
					nschedulertestcode = (String) inputMap.get("nschedulertestcode");
					List<String> myList = new ArrayList<String>(Arrays.asList(nschedulertestcode.split(",")));
					lstData = lstData.stream().filter(
							x -> myList.stream().anyMatch(y -> y.equals(String.valueOf(x.get("nschedulertestcode")))))
							.collect(Collectors.toList());
					objMap.put("selectedTest", lstData);
				} else if ((int) inputMap.get("ntype") == 5) {
					nschedulertestcode = String.valueOf(lstData.get(lstData.size() - 1).get("nschedulertestcode"));
					objMap.put("selectedTest", Arrays.asList(lstData.get(lstData.size() - 1)));
				}

				else {
					nschedulertestcode = String.valueOf(lstData.get(lstData.size() - 1).get("nschedulertestcode"));
					objMap.put("selectedTest", Arrays.asList(lstData.get(lstData.size() - 1)));
				}
				if (inputMap.containsKey("isParameter")) {
					if (!inputMap.containsKey("nschedulertestcode")) {
						inputMap.put("nschedulertestcode", lstData.get(lstData.size() - 1).get("nschedulertestcode"));
					}
					objMap.putAll((Map<String, Object>) getSchedulerConfigParameter(inputMap, userInfo).getBody());
				}

			} else {// ntype=nullR
				nschedulertestcode = lstData.stream().map(x -> String.valueOf(x.get("nschedulertestcode")))
						.collect(Collectors.joining(","));

				if (inputMap.get("checkBoxOperation") != null && (int) inputMap.get("checkBoxOperation") == 7) {
					List<String> subSampleArray = Arrays
							.asList(((String) inputMap.get("nschedulersubsamplecode")).split(","));
					for (Map<String, Object> map : lstData) {
						if ((int) map.get("nschedulersubsamplecode") == Integer.parseInt(subSampleArray.get(0))) {
							nschedulertestcode = map.get("nschedulertestcode").toString();
							// break;
						}
					}
					inputMap.put("nschedulertestcode", nschedulertestcode);
				}

				objMap.put("selectedTest", Arrays.asList(lstData.get(lstData.size() - 1)));
				if (inputMap.containsKey("isParameter")) {
					inputMap.put("nschedulertestcode", lstData.get(lstData.size() - 1).get("nschedulertestcode"));
					objMap.putAll((Map<String, Object>) getSchedulerConfigParameter(inputMap, userInfo).getBody());
				}
			}
			//
		} else {
			objMap.put("selectedTest", Arrays.asList());
			objMap.put("SchedulerConfigurationParameter", Arrays.asList());
			objMap.put("activeTestTab", "IDS_PARAMETERRESULTS");
			objMap.put("SchedulerConfigGetTest", Arrays.asList());
		}
		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getSchedulerConfigSubSampleTemplate(int ndesigntemplatemappingcode) throws Exception {
		// TODO Auto-generated method stub

		final String str = "select dm.ndesigntemplatemappingcode,rt.jsondata from designtemplatemapping dm, "
				+ " reactregistrationtemplate rt where  rt.nreactregtemplatecode=nsubsampletemplatecode"
				+ " and dm.ndesigntemplatemappingcode=" + ndesigntemplatemappingcode + " and  rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final ReactRegistrationTemplate lstReactRegistrationTemplate = (ReactRegistrationTemplate) jdbcUtilityFunction
				.queryForObject(str, ReactRegistrationTemplate.class, jdbcTemplate);
		return new ResponseEntity<>(lstReactRegistrationTemplate, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> createSchedulerConfig(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		final ObjectMapper objmap = new ObjectMapper();
		Map<String, Object> returnMap = new HashMap<>();
		objmap.registerModule(new JavaTimeModule());
		final SchedulerSampleDetail schedulerConfig = objmap.convertValue(inputMap.get("SchedulerConfig"),
				new TypeReference<SchedulerSampleDetail>() {
		});

		final List<SchedulerSubSampleDetail> schedulerConfigSample = objmap.convertValue(
				inputMap.get("SchedulerConfigSample"), new TypeReference<List<SchedulerSubSampleDetail>>() {
				});

		final List<TestGroupTest> tgtTestInputList = objmap.convertValue(inputMap.get("testgrouptest"),
				new TypeReference<List<TestGroupTest>>() {
		});

		final List<String> dateList = objmap.convertValue(inputMap.get("DateList"), new TypeReference<List<String>>() {
		});

		final List<Map<String, Object>> sampleDateConstraint = objmap
				.convertValue(inputMap.get("sampledateconstraints"), new TypeReference<List<Map<String, Object>>>() {
				});

		final List<Map<String, Object>> subSampleDateConstraint = objmap
				.convertValue(inputMap.get("subsampledateconstraints"), new TypeReference<List<Map<String, Object>>>() {
				});

		final List<String> SubSampledateList = objmap.convertValue(inputMap.get("subsampleDateList"),
				new TypeReference<List<String>>() {
		});

		int nregtypecode = (int) inputMap.get("nregtypecode");
		int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
		int nsampletypecode = (int) inputMap.get("nsampletypecode");
		int napproveconfversioncode = (int) inputMap.get("napproveconfversioncode");
		int subSampleCount = schedulerConfigSample.size();
		int nage = 0;
		int ngendercode = 0;

		int nsequenceNoSample = (jdbcTemplate.queryForObject(
				"select nsequenceno from seqnoscheduler where stablename ='schedulersampledetail'", Integer.class) + 1);
		int nsequenceNoSubSample = jdbcTemplate.queryForObject(
				"select nsequenceno from seqnoscheduler where stablename ='schedulersubsampledetail'", Integer.class);
		int nsequenceNoTest = jdbcTemplate.queryForObject(
				"select nsequenceno from seqnoscheduler where stablename ='schedulertestdetail'", Integer.class);
		int nsequenceNoTestParameter = jdbcTemplate.queryForObject(
				"select nsequenceno from seqnoscheduler where stablename ='schedulertestparameterdetail'",
				Integer.class);

		JSONObject jsoneditSchedulerConfig = new JSONObject(schedulerConfig.getJsondata());
		JSONObject jsonuiSchedulerConfig = new JSONObject(schedulerConfig.getJsonuidata());
		jsoneditSchedulerConfig = (JSONObject) convertInputDateToUTCByZone(jsoneditSchedulerConfig, dateList, false,
				userInfo);
		jsonuiSchedulerConfig = (JSONObject) convertInputDateToUTCByZone(jsonuiSchedulerConfig, dateList, false,
				userInfo);

		jsonuiSchedulerConfig.put("nsampletypecode", nsampletypecode);
		jsonuiSchedulerConfig.put("nregtypecode", nregtypecode);
		jsonuiSchedulerConfig.put("nregsubtypecode", nregsubtypecode);
		jsonuiSchedulerConfig.put("nproductcatcode", schedulerConfig.getNproductcatcode());
		jsonuiSchedulerConfig.put("nproductcode", schedulerConfig.getNproductcode());
		jsonuiSchedulerConfig.put("nprojectmastercode", schedulerConfig.getNprojectmastercode());
		jsonuiSchedulerConfig.put("ninstrumentcatcode", schedulerConfig.getNinstrumentcatcode());
		jsonuiSchedulerConfig.put("ninstrumentcode", schedulerConfig.getNinstrumentcode());
		jsonuiSchedulerConfig.put("nmaterialcatcode", schedulerConfig.getNmaterialcatcode());
		jsonuiSchedulerConfig.put("nmaterialcode", schedulerConfig.getNmaterialcode());
		jsonuiSchedulerConfig.put("ntemplatemanipulationcode", schedulerConfig.getNtemplatemanipulationcode());
		jsonuiSchedulerConfig.put("nallottedspeccode", schedulerConfig.getNallottedspeccode());
		jsonuiSchedulerConfig.put("ndesigntemplatemappingcode", schedulerConfig.getNdesigntemplatemappingcode());
		jsonuiSchedulerConfig.put("napproveconfversioncode", napproveconfversioncode);
		jsonuiSchedulerConfig.put("Transaction Date",
				dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ").replace("Z", ""));
		jsonuiSchedulerConfig.put("nsampleschedulerconfigtypecode", inputMap.get("nsampleschedulerconfigtypecode"));
		jsonuiSchedulerConfig.put("nschedulecode", schedulerConfig.getNschedulecode());
		jsonuiSchedulerConfig.put("nregsubtypeversioncode", schedulerConfig.getNregsubtypeversioncode());

		jsonuiSchedulerConfig.put("nschedulersamplecode", nsequenceNoSample);
		String strSchedulerSampleInsert = "insert into schedulersampledetail "
				+ " (nschedulersamplecode,nsampleschedulerconfigtypecode, nschedulecode, nsampletypecode, nregtypecode, nregsubtypecode, ndesigntemplatemappingcode, nregsubtypeversioncode, "
				+ " ntemplatemanipulationcode, nallottedspeccode, nproductcatcode, nproductcode, ninstrumentcatcode, ninstrumentcode, nmaterialcatcode, nmaterialcode,nmaterialinventorycode, "
				+ " nprojectmastercode, ntransactionstatus, nactivestatus, jsondata, jsonuidata, dtransactiondate, noffsetdtransactiondate, ntransdatetimezonecode, "
				+ " nsitecode, nstatus) values (" + nsequenceNoSample + ", "
				+ inputMap.get("nsampleschedulerconfigtypecode") + ", " + schedulerConfig.getNschedulecode() + " , "
				+ nsampletypecode + ", " + nregtypecode + ", " + nregsubtypecode + ", "
				+ schedulerConfig.getNdesigntemplatemappingcode() + ", " + schedulerConfig.getNregsubtypeversioncode()
				+ " , " + schedulerConfig.getNtemplatemanipulationcode() + ", " + schedulerConfig.getNallottedspeccode()
				+ ", " + schedulerConfig.getNproductcatcode() + ", " + schedulerConfig.getNproductcode() + ", "
				+ schedulerConfig.getNinstrumentcatcode() + ", " + schedulerConfig.getNinstrumentcode() + ", "
				+ schedulerConfig.getNmaterialcatcode() + " , " + schedulerConfig.getNmaterialcode() + ", "
				+ schedulerConfig.getNmaterialinventorycode() + ", " + schedulerConfig.getNprojectmastercode() + ", "
				+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ", '"
				+ stringUtilityFunction.replaceQuote(jsoneditSchedulerConfig.toString()) + "'::JSONB, '"
				+ stringUtilityFunction.replaceQuote(jsonuiSchedulerConfig.toString()) + "'::JSONB, '"
				+ dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ").replace("Z", "") + "', "
				+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
				+ userInfo.getNtimezonecode() + ", " + userInfo.getNmastersitecode() + ", "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "); ";
		String strSchedulerSubSampleInsert = "";
		String strSchedulerTestInsert = "";

		int subSampleDetailCount = schedulerConfigSample.size();
		strSchedulerSubSampleInsert = "insert into schedulersubsampledetail (nschedulersubsamplecode, nschedulersamplecode, nspecsampletypecode, ncomponentcode, jsondata, jsonuidata, "
				+ " dtransactiondate, noffsetdtransactiondate, ntransdatetimezonecode, nsitecode, nstatus) values ";
		strSchedulerTestInsert = "insert into schedulertestdetail (nschedulertestcode, nschedulersubsamplecode, nschedulersamplecode, ntestgrouptestcode, ntestcode, nsectioncode, nmethodcode, "
				+ "ninstrumentcatcode, nchecklistversioncode, ntestrepeatno, ntestretestno, jsondata, dtransactiondate, noffsetdtransactiondate, ntransdatetimezonecode, nsitecode, nstatus) values ";

		String sschedulerTestCode = "";

		for (int i = subSampleDetailCount - 1; i >= 0; i--) {
			nsequenceNoSubSample++;
			JSONObject jsoneditObj = new JSONObject(schedulerConfigSample.get(i).getJsondata());
			JSONObject jsonuiObj = new JSONObject(schedulerConfigSample.get(i).getJsonuidata());
			if (!SubSampledateList.isEmpty()) {
				jsoneditObj = (JSONObject) convertInputDateToUTCByZone(jsoneditObj, SubSampledateList, false, userInfo);
				jsonuiObj = (JSONObject) convertInputDateToUTCByZone(jsonuiObj, SubSampledateList, false, userInfo);
			}
			jsonuiObj.put("nschedulersubsamplecode", nsequenceNoSubSample);
			jsonuiObj.put("nschedulersamplecode", nsequenceNoSample);
			jsonuiObj.put("nspecsampletypecode", schedulerConfigSample.get(i).getNspecsampletypecode());
			jsonuiObj.put("ncomponentcode", schedulerConfigSample.get(i).getNcomponentcode());
			strSchedulerSubSampleInsert += "(" + nsequenceNoSubSample + ", " + nsequenceNoSample + ", "
					+ schedulerConfigSample.get(i).getNspecsampletypecode() + ", "
					+ schedulerConfigSample.get(i).getNcomponentcode() + ", '"
					+ stringUtilityFunction.replaceQuote(jsoneditObj.toString()) + "'::jsonb, '"
					+ stringUtilityFunction.replaceQuote(jsonuiObj.toString()) + "'::jsonb, '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ").replace("Z", "")
					+ "', " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNtimezonecode() + ", " + userInfo.getNmastersitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";

			List<SchedulerSubSampleDetail> comp = new ArrayList();
			comp.add(schedulerConfigSample.get(i));

			if (!tgtTestInputList.isEmpty()) {
				List<TestGroupTest> lsttest1 = tgtTestInputList.stream()
						.filter(x -> x.getSlno() == comp.get(0).getSlno()).collect(Collectors.toList());
				final String stestcode = lsttest1.stream().map(x -> String.valueOf(x.getNtestgrouptestcode()))
						.distinct().collect(Collectors.joining(","));

				for (TestGroupTest testDetails : lsttest1) {
					nsequenceNoTest++;
					sschedulerTestCode += nsequenceNoTest + ",";
					strSchedulerTestInsert += "(" + nsequenceNoTest + ", " + nsequenceNoSubSample + ","
							+ nsequenceNoSample + ", " + testDetails.getNtestgrouptestcode() + ", "
							+ testDetails.getNtestcode() + ", " + testDetails.getNsectioncode() + ", "
							+ testDetails.getNmethodcode() + ", " + testDetails.getNinstrumentcatcode()
							+ ", -1, 1, 0, json_build_object('nschedulertestcode', " + nsequenceNoTest
							+ ", 'nschedulersubsamplecode', " + nsequenceNoSubSample + ", 'nschedulersamplecode', "
							+ nsequenceNoSample + ", 'ssectionname', '"
							+ stringUtilityFunction.replaceQuote(testDetails.getSsectionname()) + "', 'smethodname', '"
							+ stringUtilityFunction.replaceQuote(testDetails.getSmethodname()) + "', 'ncost',"
							+ testDetails.getNcost() + ", 'stestname', '"
							+ stringUtilityFunction.replaceQuote(testDetails.getStestsynonym())
							+ "', 'stestsynonym', concat('"
							+ stringUtilityFunction.replaceQuote(testDetails.getStestsynonym()) + "','["
							+ testDetails.getNrepeatcountno() + "][0]'))::jsonb, '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ").replace("Z",
									"")
							+ "', " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ userInfo.getNtimezonecode() + ", " + userInfo.getNmastersitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
				}
			}

		}

		strSchedulerSubSampleInsert = strSchedulerSubSampleInsert.substring(0, strSchedulerSubSampleInsert.length() - 1)
				+ "; ";
		jdbcTemplate.execute(strSchedulerSampleInsert);
		jdbcTemplate.execute(strSchedulerSubSampleInsert);
		int count = -1;
		if (!tgtTestInputList.isEmpty()) {
			strSchedulerTestInsert = strSchedulerTestInsert.substring(0, strSchedulerTestInsert.length() - 1) + "; ";

			sschedulerTestCode = sschedulerTestCode.substring(0, sschedulerTestCode.length() - 1);

			// jdbcTemplate.execute(strSchedulerTestInsert);

			String strTestParameter = "insert into schedulertestparameterdetail (nschedulertestparametercode, nschedulertestcode,"
					+ " nschedulersamplecode, ntestgrouptestparametercode, ntestparametercode, nparametertypecode, ntestgrouptestformulacode,"
					+ " nreportmandatory, nresultmandatory, nunitcode, jsondata, dtransactiondate, noffsetdtransactiondate, ntransdatetimezonecode,"
					+ " nsitecode, nstatus) " + "select " + nsequenceNoTestParameter
					+ "+rank() over(order by std.nschedulertestcode, tgtp.ntestgrouptestparametercode),"
					+ " std.nschedulertestcode, std.nschedulersamplecode, tgtp.ntestgrouptestparametercode, tgtp.ntestparametercode, "
					+ " tgtp.nparametertypecode, coalesce(tgtf.ntestgrouptestformulacode, -1), tgtp.nreportmandatory, tgtp.nresultmandatory, tgtp.nunitcode,"
					+ " json_build_object('smaxa', tgtnp.smaxa, 'smaxb', tgtnp.smaxb, 'smina', tgtnp.smina, 'sminb', tgtnp.sminb, 'sfinal', null,"
					+ " 'smaxlod', tgtnp.smaxlod, 'smaxloq', tgtnp.smaxloq, 'sminlod', tgtnp.sminlod, 'sminloq', tgtnp.sminloq, 'sresult', null)::jsonb "
					// ALPD-5475 Added by Abdul Start to set defaultvalue according to the
					// parametertype
					+ "|| case when tgtp.nparametertypecode=" + Enumeration.ParameterType.NUMERIC.getparametertype()
					+ " then " + "json_build_object('ngradecode',tgtnp.ngradecode "
					+ "):: jsonb when tgtp.nparametertypecode="
					+ Enumeration.ParameterType.PREDEFINED.getparametertype() + "then "
					+ "json_build_object('ngradecode', tgtpp.ngradecode):: jsonb else json_build_object('ngradecode',null)::jsonb end"
					+ " || json_build_object( 'sdisregard', tgtnp.sdisregard)::jsonb "
					+ "|| case when tgtp.nparametertypecode=" + Enumeration.ParameterType.NUMERIC.getparametertype()
					+ " then " + "json_build_object('sresultvalue',tgtnp.sresultvalue"
					+ "):: jsonb when tgtp.nparametertypecode="
					+ Enumeration.ParameterType.PREDEFINED.getparametertype() + " then "
					+ "json_build_object('sresultvalue', tgtpp.spredefinedname):: jsonb "
					+ "when tgtp.nparametertypecode=" + Enumeration.ParameterType.CHARACTER.getparametertype()
					+ " then " + "json_build_object('sresultvalue', tgtcp.scharname)::jsonb else json_build_object("
					+ "'sresultvalue',tgtnp.sresultvalue)::jsonb end "
					// ALPD-5475 End
					+ "|| json_build_object( 'stestsynonym', std.jsondata->>'stestsynonym', 'nroundingdigits', "
					+ "tgtp.nroundingdigits, 'sparametersynonym', tgtp.sparametersynonym, 'nresultaccuracycode',tgtp.nresultaccuracycode,"
					+ "'sresultaccuracyname', ra.sresultaccuracyname, 'nschedulersamplecode', std.nschedulersamplecode, 'nschedulersubsamplecode',"
					+ "std.nschedulersubsamplecode, 'nschedulertestcode', std.nschedulertestcode, 'nschedulertestparametercode', "
					+ nsequenceNoTestParameter
					+ "+rank() over(order by std.nschedulertestcode, tgtp.ntestgrouptestparametercode))::jsonb "
					+ "jsondata, '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ").replace("Z", "")
					+ "', " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNtimezonecode() + ", " + userInfo.getNmastersitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " from schedulertestdetail std, testgrouptest tgt, resultaccuracy ra, testgrouptestparameter tgtp "
					+ " left join testgrouptestnumericparameter tgtnp on tgtnp.ntestgrouptestparametercode=tgtp.ntestgrouptestparametercode "
					+ " and tgtnp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " left join testgrouptestcharparameter tgtcp on tgtcp.ntestgrouptestparametercode=tgtp.ntestgrouptestparametercode "
					+ " and tgtcp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					// + " left join testgrouptestpredefparameter tgtpp on
					// tgtpp.ntestgrouptestparametercode=tgtp.ntestgrouptestparametercode "
					// + " and tgtpp.nstatus=" +
					// Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " left join testgrouptestformula tgtf on tgtf.ntestgrouptestparametercode=tgtp.ntestgrouptestparametercode "
					+ " and tgtf.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					// ALPD-5475 Added by Abdul Start to set defaultvalue according to the
					// parametertype
					+ " left outer join (select tt.ntestgrouptestparametercode,max(tt.spredefinedname) as spredefinedname , max(tt.ngradecode) as ngradecode, max(tp.nstatus) as nstatus  "
					+ " from testgrouptestpredefparameter tt,testgrouptestparameter tp "
					+ " where tt.ntestgrouptestparametercode=tp.ntestgrouptestparametercode "
					+ "group by tt.ntestgrouptestparametercode) tgtpp on "
					+ " tgtpp.ntestgrouptestparametercode=tgtp.ntestgrouptestparametercode  and tgtpp.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					// ALPD-5475 End
					+ " left join testgrouptestclinicalspec tgtcs on tgtcs.ntestgrouptestparametercode=tgtp.ntestgrouptestparametercode "
					+ " and tgtcs.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " where std.ntestgrouptestcode=tgt.ntestgrouptestcode and tgt.ntestgrouptestcode=tgtp.ntestgrouptestcode"
					+ " and tgtp.nresultaccuracycode=ra.nresultaccuracycode " + " and std.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ra.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgtp.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and std.nschedulertestcode in ("
					+ sschedulerTestCode + ");";
			jdbcTemplate.execute(strSchedulerTestInsert + strTestParameter);

		}

		String strSeqNoUpdate = "update seqnoscheduler set nsequenceno=" + nsequenceNoSample
				+ " where stablename='schedulersampledetail';" + " update seqnoscheduler set nsequenceno="
				+ nsequenceNoSubSample + " where stablename='schedulersubsampledetail';"
				+ " update seqnoscheduler set nsequenceno=" + nsequenceNoTest
				+ " where stablename='schedulertestdetail';";

		if (!tgtTestInputList.isEmpty()) {

			final String sQuery = "select * from schedulertestparameterdetail where nschedulertestcode in ("
					+ sschedulerTestCode + ") and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final List<SchedulerTestParameterDetail> lstSchedulerTestParameterDetail = jdbcTemplate.query(sQuery,
					new SchedulerTestParameterDetail());

			strSeqNoUpdate = strSeqNoUpdate + " update seqnoscheduler set nsequenceno="
					+ (nsequenceNoTestParameter + lstSchedulerTestParameterDetail.size())
					+ " where stablename='schedulertestparameterdetail';";

		}

		// jdbcTemplate.execute(strSchedulerSampleInsert+ strSchedulerSubSampleInsert+
		// strSchedulerTestInsert+ strTestParameter+ strSeqNoUpdate);

		jdbcTemplate.execute(strSeqNoUpdate);

		// ALPD-5332 Added by Abdul to Add SchedulerConfig Site
		final String schedulerSiteInsert = "Insert into schedulerconfigsite (nschedulersamplecode, nsitecode, dmodifieddate, nstatus)"
				+ "Select " + nsequenceNoSample + ", " + userInfo.getNtranssitecode() + ", '"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " where not exists (select 1 from schedulerconfigsite where nschedulersamplecode= "
				+ nsequenceNoSample + " and nsitecode= " + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ) ;";
		jdbcTemplate.execute(schedulerSiteInsert);
		// ALPD-5332 End

		returnMap = getDynamicSchedulerConfig(inputMap, userInfo);
		returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
				Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObject = new JSONObject();
		jsonAuditObject.put("schedulersampledetail", (List<Map<String, Object>>) returnMap.get("selectedSample"));
		actionType.put("schedulersampledetail", "IDS_ADDSCHEDULERSAMPLE");

		if ((boolean) inputMap.get("nneedsubsample")) {
			jsonAuditObject.put("schedulersubsampledetail",
					(List<Map<String, Object>>) returnMap.get("SchedulerConfigGetSubSample"));
			actionType.put("schedulersubsampledetail", "IDS_ADDSCHEDULERSUBSAMPLE");

			inputMap.put("nschedulersubsamplecode", null);

			List<Map<String, Object>> lstDataTest = auditTestGet(inputMap, userInfo);

			jsonAuditObject.put("schedulertestdetail", lstDataTest);
		} else {
			jsonAuditObject.put("schedulertestdetail",
					(List<Map<String, Object>>) returnMap.get("SchedulerConfigGetTest"));
		}

		auditmap.put("nregtypecode", nregtypecode);
		auditmap.put("nregsubtypecode", nregsubtypecode);
		auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));

		actionType.put("schedulertestdetail", "IDS_ADDSCHEDULERTEST");
		auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);

	}

	@SuppressWarnings({ "unchecked", "static-access" })
	public Object convertInputDateToUTCByZone(JSONObject jsonObj, final List<String> inputFieldList,
			final boolean returnAsString, final UserInfo userInfo// , final boolean includeMilliseconds
			) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		if (userInfo.getIsutcenabled() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
			final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			// SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			// if (includeMilliseconds) {
			// sourceFormat = "yyyy-MM-dd HH:mm:ss.SSS";
			// }
			for (int i = 0; i < inputFieldList.size(); i++) {

				// final String stringDate = LocalDateTime.parse((String)
				// jsonObj.get(inputFieldList.get(i)), dbPattern)
				// .format(dbPattern);
				if ((String) jsonObj.get(inputFieldList.get(i)) != null
						&& !jsonObj.get(inputFieldList.get(i)).equals("")
						&& !jsonObj.get(inputFieldList.get(i)).equals("-")) {
					if (jsonObj.has("tz" + inputFieldList.get(i))) {

						JSONObject map = (JSONObject) jsonObj.get("tz" + inputFieldList.get(i));
						jsonObj.put("noffset" + inputFieldList.get(i),
								dateUtilityFunction.getCurrentDateTimeOffsetFromDate(
										(String) jsonObj.get(inputFieldList.get(i)), (String) map.get("label")));

						// final LocalDateTime ldt = sourceFormat.parse((String)
						// data.get(dateFieldName))
						// .toInstant()
						// .atOffset(ZoneOffset
						// .ofTotalSeconds((int) data.get("noffset" + dateFieldName)))
						// .toLocalDateTime();

						final Instant ldt = LocalDateTime.parse((String) jsonObj.get(inputFieldList.get(i)), dbPattern)
								.atOffset(ZoneOffset.ofTotalSeconds(jsonObj.getInt("noffset" + inputFieldList.get(i))))
								.toInstant();

						// final LocalDateTime ldt = sourceFormat.parse((String)
						// jsonObj.get(inputFieldList.get(i)))
						// .toInstant()
						// .atOffset(ZoneOffset.ofTotalSeconds(jsonObj.getInt("noffset" +
						// inputFieldList.get(i))))
						// .toLocalDateTime();

						// ZoneId zoneId = ZoneId.of(userInfo.getStimezoneid());
						// if (jsonObj.has("tz" + inputFieldList.get(i))) {
						// zoneId = ZoneId
						// .of((String) ((JSONObject) jsonObj.get("tz" +
						// inputFieldList.get(i))).get("label"));
						// }
						jsonObj.put(inputFieldList.get(i), ldt.toString().replace("T", " ").replace("Z", ""));
					} else {
						jsonObj.put("noffset" + inputFieldList.get(i),
								dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()));
						// final LocalDateTime ldt = LocalDateTime.parse((String)
						// jsonObj.get(inputFieldList.get(i)),
						// dbPattern);
						final Instant ldt = LocalDateTime.parse((String) jsonObj.get(inputFieldList.get(i)), dbPattern)
								.atOffset(ZoneOffset.ofTotalSeconds(jsonObj.getInt("noffset" + inputFieldList.get(i))))
								.toInstant();

						// ZoneId zoneId = ZoneId.of(userInfo.getStimezoneid());
						// if (jsonObj.has("tz" + inputFieldList.get(i))) {
						// zoneId = ZoneId
						// .of((String) ((JSONObject) jsonObj.get("tz" +
						// inputFieldList.get(i))).get("label"));
						// }
						// Instant instantDate =
						// ldt.atZone(zoneId).toInstant().truncatedTo(ChronoUnit.SECONDS);
						jsonObj.put(inputFieldList.get(i), ldt.toString().replace("T", " ").replace("Z", ""));
					}
				} else {
					jsonObj.put(inputFieldList.get(i), "");
				}

			}
		} else {
			final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			// final DateTimeFormatter uiPattern =
			// DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());
			for (int i = 0; i < inputFieldList.size(); i++) {
				// ALPD-3575
				if (jsonObj.has(inputFieldList.get(i))) {
					String stringDate = (String) jsonObj.get(inputFieldList.get(i));
					if (!stringDate.equals("")) {
						stringDate = LocalDateTime.parse(stringDate, dbPattern).format(dbPattern);
						jsonObj.put(inputFieldList.get(i), stringDate);
					}
				}
			}
		}
		return jsonObj;
	}

	public List<RegistrationType> getRegistrationTypeFilter(final int nsampleTypeCode, Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		String Str = "Select * from sampletype  where  nsampletypecode=" + nsampleTypeCode;

		SampleType obj = (SampleType) jdbcUtilityFunction.queryForObject(Str, SampleType.class, jdbcTemplate);
		String ValidationQuery = "";
		if (obj.getNtransfiltertypecode() != -1 && userInfo.getNusercode() != -1) {
			int nmappingfieldcode = (obj.getNtransfiltertypecode() == 1) ? userInfo.getNdeptcode()
					: userInfo.getNuserrole();
			ValidationQuery = " and rst.nregsubtypecode in( " + "		SELECT rs.nregsubtypecode "
					+ "		FROM registrationsubtype rs "
					+ "		INNER JOIN transactionfiltertypeconfig ttc ON rs.nregsubtypecode = ttc.nregsubtypecode "
					+ "		LEFT JOIN transactionusers tu ON tu.ntransfiltertypeconfigcode = ttc.ntransfiltertypeconfigcode "
					+ "		WHERE ( ttc.nneedalluser = 3  and ttc.nstatus =  "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND ttc.nmappingfieldcode = "
					+ nmappingfieldcode + ") " + "	 OR " + "	( ttc.nneedalluser = 4  "
					+ "	  AND ttc.nmappingfieldcode =" + nmappingfieldcode + "	  AND tu.nusercode ="
					+ userInfo.getNusercode() + "   and ttc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "   and tu.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") " + "  OR "
					+ "	( ttc.nneedalluser = 4  " + " AND ttc.nmappingfieldcode = -1 " + " AND tu.nusercode ="
					+ userInfo.getNusercode() + " and ttc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tu.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") "
					+ "	 AND rs.nregtypecode = rt.nregtypecode) ";
		}
		String finalquery = "select  rt.nregtypecode,coalesce(rt.jsondata->'sregtypename'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',rt.jsondata->'sregtypename'->>'en-US') as sregtypename,rt.nsorter "
				+ "from SampleType st,registrationtype rt,registrationsubtype rst,approvalconfig ac,approvalconfigversion acv"
				+ " where st.nsampletypecode > 0 and st.nsampletypecode =" + nsampleTypeCode + " and st.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and st.napprovalconfigview = "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus()
				+ " and st.nsampletypecode=rt.nsampletypecode " + " and rt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nregtypecode=rst.nregtypecode "
				+ " and rst.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rst.nregsubtypecode=ac.nregsubtypecode " + " and ac.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and acv.napprovalconfigcode=ac.napprovalconfigcode " + " and acv.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acv.ntransactionstatus = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and st.nsitecode= rt.nsitecode and rt.nsitecode=rst.nsitecode and rst.nsitecode=acv.nsitecode "
				+ " and acv.nsitecode=" + userInfo.getNmastersitecode() + " and rt.nregtypecode>0 " + ValidationQuery
				+ " group by rt.nregtypecode,coalesce(rt.jsondata->'sregtypename'->>'" + userInfo.getSlanguagetypecode()
				+ "',rt.jsondata->'sregtypename'->>'en-US'),rt.nsorter order by rt.nregtypecode desc";

		return jdbcTemplate.query(finalquery, new RegistrationType());

	}

	public List<RegistrationSubType> getRegistrationSubTypeFilter(final int nregTypeCode, Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		String Str = "Select * from registrationtype rt,sampletype st where rt.nsampletypecode=st.nsampletypecode and rt.nregTypeCode="
				+ nregTypeCode;

		SampleType obj = (SampleType) jdbcUtilityFunction.queryForObject(Str, SampleType.class, jdbcTemplate);
		String validationQuery = "";
		if (obj.getNtransfiltertypecode() != -1 && userInfo.getNusercode() != -1) {
			int nmappingfieldcode = (obj.getNtransfiltertypecode() == 1) ? userInfo.getNdeptcode()
					: userInfo.getNuserrole();
			validationQuery = " and rst.nregsubtypecode in( " + " SELECT rs.nregsubtypecode "
					+ " FROM registrationsubtype rs "
					+ " INNER JOIN transactionfiltertypeconfig ttc ON rs.nregsubtypecode = ttc.nregsubtypecode "
					+ " LEFT JOIN transactionusers tu ON tu.ntransfiltertypeconfigcode = ttc.ntransfiltertypeconfigcode "
					+ " WHERE ( ttc.nneedalluser = 3  and ttc.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND ttc.nmappingfieldcode = "
					+ nmappingfieldcode + ")" + "  OR " + "	( ttc.nneedalluser = 4  " + " AND ttc.nmappingfieldcode ="
					+ nmappingfieldcode + " AND tu.nusercode =" + userInfo.getNusercode() + " and ttc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tu.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")  OR "
					+ "	( ttc.nneedalluser = 4  " + " AND tu.nusercode =" + userInfo.getNusercode()
					+ " AND ttc.nmappingfieldcode = -1 " + " and ttc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tu.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") " + " AND rs.nregtypecode = "
					+ nregTypeCode + ")";
		}
		String finalquery = "select max(rsc.nregsubtypeversioncode) nregsubtypeversioncode,"
				+ " max(rsc.jsondata->>'nneedsubsample' )::Boolean nneedsubsample,"
				+ " max(rsc.jsondata->>'nneedjoballocation' )::Boolean nneedjoballocation,"
				+ " max(rsc.jsondata->>'nneedmyjob' )::Boolean nneedmyjob,"
				+ " max(rsc.jsondata->>'nneedtestinitiate')::Boolean nneedtestinitiate,"
				+ "max(rsc.jsondata->>'nneedtemplatebasedflow' )::Boolean nneedtemplatebasedflow,rst.nregsubtypecode,"
				+ "coalesce(rst.jsondata->'sregsubtypename'->>'" + userInfo.getSlanguagetypecode()
				+ "',rst.jsondata->'sregsubtypename'->>'en-US') as sregsubtypename,rst.nsorter "
				+ "from SampleType st,registrationtype rt,registrationsubtype rst,approvalconfig ac,approvalconfigversion acv,regsubtypeconfigversion rsc "
				+ "where st.nsampletypecode > 0 and st.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and st.napprovalconfigview = "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " "
				+ " and st.nsampletypecode=rt.nsampletypecode and rt.nregtypecode = " + nregTypeCode
				+ " and rt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rt.nregtypecode=rst.nregtypecode and rst.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rst.nregsubtypecode=ac.nregsubtypecode and ac.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rsc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and acv.napprovalconfigcode=ac.napprovalconfigcode and acv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acv.ntransactionstatus = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ "  and rst.nregsubtypecode>0 and rsc.napprovalconfigcode=ac.napprovalconfigcode and rsc.ntransactionstatus= "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and st.nsitecode= rt.nsitecode and rt.nsitecode=rst.nsitecode and rst.nsitecode=acv.nsitecode "
				+ " and acv.nsitecode=rsc.nsitecode and rsc.nsitecode=" + userInfo.getNmastersitecode()
				+ validationQuery + " group by rst.nregsubtypecode,coalesce(rst.jsondata->'sregsubtypename'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',rst.jsondata->'sregsubtypename'->>'en-US'),rst.nsorter order by rst.nregsubtypecode desc";
		return jdbcTemplate.query(finalquery, new RegistrationSubType());

	}

	@Override
	public ResponseEntity<Object> getSchedulerConfigParameter(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		String nschedulersubsamplecode = "";
		String nschedulertestcode = "";
		String nschedulersamplecode = "";

		if (inputMap.containsKey("nschedulersubsamplecode")) {
			nschedulersubsamplecode = inputMap.get("nschedulersubsamplecode").toString();
		}
		if (inputMap.containsKey("nschedulertestcode")) {
			nschedulertestcode = inputMap.get("nschedulertestcode").toString();
		}

		if (inputMap.containsKey("nschedulersamplecode")) {
			nschedulersamplecode = inputMap.get("nschedulersamplecode").toString();
		}

		final String query = "select json_agg(a.jsondata) from (select stpd.nschedulertestcode, ssd.ntransactionstatus, coalesce(ts.jsondata -> 'stransdisplaystatus' ->> 'en-US',  ts.jsondata -> 'stransdisplaystatus' ->> 'en-US') stransdisplaystatus,"
				+ " stpd.jsondata || json_build_object('stransdisplaystatus',  coalesce(ts.jsondata -> 'stransdisplaystatus' ->> 'en-US', ts.jsondata -> 'stransdisplaystatus' ->> 'en-US'), "
				+ " 'ntransactionstatus', ssd.ntransactionstatus, 'sarno', concat(cast(stpd.nschedulersamplecode as text),  ' ', 'Reg No.'), "
				+ "'ssamplearno', concat(cast(std.nschedulersubsamplecode as text ), ' ', 'Sub Reg No.'),"
				+ "'sparametersynonym', stpd.jsondata->>'sparametersynonym','stestsynonym', stpd.jsondata->>'stestsynonym',"
				+ "'sparametertypename',pt.sparametertypename ):: jsonb as jsondata from schedulertestdetail std, schedulersampledetail ssd, schedulersubsampledetail sssd,"
				+ " transactionstatus ts, schedulertestparameterdetail stpd,testgrouptest tgt, parametertype pt,testgrouptestparameter tgtp,"
				+ " testparameter tp where std.nschedulersubsamplecode = sssd.nschedulersubsamplecode "
				+ " and std.nschedulersamplecode = sssd.nschedulersamplecode "
				+ " and std.nschedulersamplecode = ssd.nschedulersamplecode "
				+ " and ssd.nschedulersamplecode = sssd.nschedulersamplecode "
				+ " and ssd.ntransactionstatus = ts.ntranscode "
				+ " and stpd.nschedulersamplecode = ssd.nschedulersamplecode "
				+ " and stpd.nschedulersamplecode = sssd.nschedulersamplecode "
				+ " and stpd.nschedulersamplecode=std.nschedulersamplecode "
				+ " and tgt.ntestgrouptestcode=tgtp.ntestgrouptestcode" + " and tgt.ntestcode=std.ntestcode "
				+ " and tgtp.ntestgrouptestparametercode=stpd.ntestgrouptestparametercode "
				+ " and tgtp.ntestparametercode=tp.ntestparametercode "
				+ " and stpd.ntestparametercode=tp.ntestparametercode "
				+ " and stpd.ntestparametercode=tgtp.ntestparametercode and tgtp.nparametertypecode=pt.nparametertypecode"
				+ " and tgtp.nparametertypecode=tp.nparametertypecode "
				+ " and stpd.nschedulertestcode=std.nschedulertestcode "
				+ " and tgtp.nparametertypecode=pt.nparametertypecode " + " and stpd.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and pt.nparametertypecode=tp.nparametertypecode and std.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and ssd.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sssd.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and std.nsitecode ="
				+ userInfo.getNmastersitecode() + "" + " and std.nschedulertestcode in(" + nschedulertestcode
				+ ") order by 1 desc) a";

		logging.info("parameter query:" + query);
		String lstData1 = jdbcTemplate.queryForObject(query, String.class);
		Map<String, Object> returnMap = new HashMap<String, Object>();

		if (lstData1 != null) {
			List<Map<String, Object>> lstData = new ArrayList();
			lstData = projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(lstData1,
					userInfo, true, (int) inputMap.get("ndesigntemplatemappingcode"), "schedulerparameter");

			returnMap.put("SchedulerConfigurationParameter", lstData);

		} else {
			returnMap.put("SchedulerConfigurationParameter", Arrays.asList());

		}

		return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getComponentBySpec(Map<String, Object> inputMap) throws Exception {

		Map<String, Object> objmap = new HashMap<String, Object>();
		int ntemplatemanipulationcode = (int) inputMap.get("ntemplatemanipulationcode");
		int nallottedspeccode = (int) inputMap.get("nallottedspeccode");

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		Instant date = dateUtilityFunction.getCurrentDateTime(userInfo);
		String dreceivedate = dateUtilityFunction.instantDateToString(date);

		String str = "select  tgs.nspecsampletypecode, " + " co.ncomponentcode,co.scomponentname,"
				+ ntemplatemanipulationcode + " as ntemplatemanipulationcode, " + " " + nallottedspeccode
				+ " as nallottedspeccode ," + userInfo.getNtimezonecode() + " as ntzdreceivedate "
				+ " from testgroupspecsampletype tgs, component co where co.ncomponentcode = tgs.ncomponentcode"
				+ "   and co.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and tgs.nallottedspeccode = " + nallottedspeccode + " and tgs.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by tgs.ncomponentcode asc ";

		List<Component> lstComponent = jdbcTemplate.query(str, new Component());
		final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());
		final String fromDateUI = LocalDateTime.parse(dreceivedate, dbPattern).format(uiPattern);
		lstComponent.stream().forEach(x -> {
			x.setDreceiveddate(date);
			x.setSreceiveddate(fromDateUI);
		});
		objmap.put("lstComponent", lstComponent);
		return new ResponseEntity<Object>(objmap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getTestfromDB(Map<String, Object> inputMap) throws Exception {
		String strtestgetQuery = "";
		boolean needSubSample = (boolean) inputMap.get("nneedsubsample");
		if ((boolean) inputMap.get("specBasedComponent") && needSubSample) {
			final int nspecsampletypecode = (Integer) inputMap.get("nspecsampletypecode");
			strtestgetQuery = " select tgt.nrepeatcountno,tgt.nsectioncode,tgt.nmethodcode,tgt.ninstrumentcatcode,"
					+ " tgt.ntestgrouptestcode,tgt.nspecsampletypecode," + " ("
					+ "        select count(tgtp.ntestgrouptestparametercode) "
					+ "        from testgrouptestparameter tgtp "
					+ "        where  tgtp.ntestgrouptestcode=tgt.ntestgrouptestcode and tgtp.nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  tgtp.nisvisible="
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ") as nparametercount,"
					+ " tgt.ntestcode,tgt.stestsynonym,tgt.nsectioncode,s.ssectionname, m.smethodname,"
					+ " ic.sinstrumentcatname,tgt.ncost "
					+ " from testgrouptest tgt,section s,method m,instrumentcategory ic, testmaster tm  "
					+ " where s.nsectioncode = tgt.nsectioncode and m.nmethodcode =tgt.nmethodcode "
					+ " and ic.ninstrumentcatcode = tgt.ninstrumentcatcode and tgt.nspecsampletypecode =  "
					+ nspecsampletypecode + " and tgt.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgt.nisvisible="
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and tm.ntestcode = tgt.ntestcode "
					+ " and tm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tm.ntransactionstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " order by tgt.nsorter";
		} else {
			strtestgetQuery = " select tgt.nrepeatcountno,tgt.nsectioncode,tgt.nmethodcode,tgt.ninstrumentcatcode,"
					+ " tgt.ntestgrouptestcode,tgt.nspecsampletypecode," + " ("
					+ "        select count(tgtp.ntestgrouptestparametercode) "
					+ "        from testgrouptestparameter tgtp "
					+ "        where  tgtp.ntestgrouptestcode=tgt.ntestgrouptestcode and tgtp.nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  tgtp.nisvisible="
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " ) as nparametercount,"
					+ " tgt.ntestcode,tgt.stestsynonym,tgt.nsectioncode,s.ssectionname, m.smethodname,"
					+ " ic.sinstrumentcatname "
					+ " from testgrouptest tgt,section s,method m,instrumentcategory ic, testmaster tm ,testgroupspecsampletype tgts "
					+ " where s.nsectioncode = tgt.nsectioncode and m.nmethodcode =tgt.nmethodcode "
					+ " and ic.ninstrumentcatcode = tgt.ninstrumentcatcode  and tgt.nspecsampletypecode = tgts.nspecsampletypecode"
					+ " and tgt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and tm.ntestcode = tgt.ntestcode and tgt.nisvisible="
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and tm.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tm.ntransactionstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgts.nallottedspeccode="
					+ inputMap.get("nallottedspeccode") + " order by tgt.nsorter";
		}

		final List<TestGroupTestForSample> lstTestGroupTest = jdbcTemplate.query(strtestgetQuery,
				new TestGroupTestForSample());

		return new ResponseEntity<Object>(lstTestGroupTest, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getTestfromSection(Map<String, Object> inputMap) throws Exception {
		String getTestSectionQuery = "";
		Map<String, Object> returnMap = new HashMap<String, Object>();
		if ((boolean) inputMap.get("specBasedComponent")) {

			getTestSectionQuery = " select tgt.nsectioncode,s.ssectionname from testgrouptest tgt ,section s "
					+ " where s.nsectioncode = tgt.nsectioncode " + " and tgt.nspecsampletypecode in ("
					+ inputMap.get("nspecsampletypecode") + " ) " + " and tgt.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " group by tgt.nsectioncode,s.ssectionname ";
		} else {

			getTestSectionQuery = "select tgt.nsectioncode,s.ssectionname from testgroupspecsampletype tgts ,testgrouptest tgt,section s "
					+ " where s.nsectioncode = tgt.nsectioncode and tgt.nspecsampletypecode = tgts.nspecsampletypecode and "
					+ " tgts.nallottedspeccode in (" + inputMap.get("nallottedspeccode") + ") and " + " tgt.nstatus =  "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " group by tgt.nsectioncode,s.ssectionname ";

		}
		returnMap.put("TestSection", jdbcTemplate.query(getTestSectionQuery, new TestGroupTestForSample()));

		return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getTestfromTestPackage(Map<String, Object> inputMap) throws Exception {
		String strtestpackageQuery = "";
		Map<String, Object> returnMap = new HashMap<>();

		if ((boolean) inputMap.get("specBasedComponent")) {
			strtestpackageQuery = " select tgt.ntestpackagecode,tp.stestpackagename from testgrouptest tgt, testpackage tp "
					+ " where tp.ntestpackagecode=tgt.ntestpackagecode  and " + " tgt.nspecsampletypecode in ("
					+ inputMap.get("nspecsampletypecode") + ")   " + " and tgt.nstatus =  "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tgt.ntestpackagecode<>-1 group by tgt.ntestpackagecode,tp.stestpackagename ";
		} else {

			strtestpackageQuery = " select tgt.ntestpackagecode,tp.stestpackagename,tgt.nspecsampletypecode "
					+ " from testgrouptest tgt,testpackage tp,testgroupspecsampletype tgts "
					+ " where tp.ntestpackagecode=tgt.ntestpackagecode and " + " tgts.nallottedspeccode= "
					+ inputMap.get("nallottedspeccode") + " and tgt.nspecsampletypecode = tgts.nspecsampletypecode and "
					+ " tgt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
					+ " tgt.ntestpackagecode <> -1 and " + " tgts.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " group by tgt.ntestpackagecode,tp.stestpackagename,tgt.nspecsampletypecode";
		}
		returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
				Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		returnMap.put("TestPackage", jdbcTemplate.query(strtestpackageQuery, new TestGroupTestForSample()));

		return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getTestBasedTestSection(Map<String, Object> inputMap) throws Exception {

		String strTestGetQuery = "";
		String testpackageTable = "";
		String testpackageValue = "";
		if ((int) inputMap.get("ntestpackagecode") != -1) {
			testpackageValue = " and tgt.ntestpackagecode=" + inputMap.get("ntestpackagecode") + " ";
			testpackageTable = "  "

					+ " left JOIN testpackage tp on tp.ntestpackagecode=tgt.ntestpackagecode and tp.nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		}
		if ((boolean) inputMap.get("specBasedComponent")) {
			strTestGetQuery = "  select case when tgt.nrepeatcountno = 0 then 1 else tgt.nrepeatcountno end nrepeatcountno, tgt.nsectioncode,tgt.nmethodcode,tgt.ninstrumentcatcode, tgt.ntestgrouptestcode,tgt.nspecsampletypecode, "
					+ " (    select count(tgtp.ntestgrouptestparametercode)     from testgrouptestparameter tgtp     where  tgtp.ntestgrouptestcode=tgt.ntestgrouptestcode "
					+ "  and tgtp.nstatus =1 and  tgtp.nisvisible="
					+ Enumeration.TransactionStatus.YES.gettransactionstatus()
					+ ") as nparametercount, tgt.ntestcode,tgt.stestsynonym,tgt.nsectioncode,s.ssectionname, m.smethodname,"
					+ "  ic.sinstrumentcatname,tgt.ncost  from" + "  testgrouptest tgt " + testpackageTable
					+ "   ,section s,method m,instrumentcategory ic, testmaster tm"
					+ "  where s.nsectioncode = tgt.nsectioncode  and m.nmethodcode =tgt.nmethodcode "
					+ testpackageValue + "  and tgt.nsectioncode=" + inputMap.get("nsectioncode")
					+ "  and ic.ninstrumentcatcode = tgt.ninstrumentcatcode " + "  and tgt.nspecsampletypecode in ( "
					+ inputMap.get("nspecsampletypecode") + ")   and tgt.nstatus = 1    and tgt.nstatus = 1   "
					+ " and tgt.nisvisible=" + Enumeration.TransactionStatus.YES.gettransactionstatus()
					+ " and tm.ntestcode = tgt.ntestcode  and tm.nstatus = 1 and tm.ntransactionstatus = 1  order by tgt.nsorter";
		} else {
			strTestGetQuery = " select case when tgt.nrepeatcountno = 0 then 1 else tgt.nrepeatcountno end nrepeatcountno,"
					+ " tgt.nsectioncode,tgt.nmethodcode,tgt.ninstrumentcatcode,"
					+ " tgt.ntestgrouptestcode,tgt.nspecsampletypecode, " + " ("
					+ "		select count(tgtp.ntestgrouptestparametercode) "
					+ "		from testgrouptestparameter tgtp "
					+ "		where  tgtp.ntestgrouptestcode=tgt.ntestgrouptestcode " + "		and tgtp.nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "and  tgtp.nisvisible="
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " ) as nparametercount,"
					+ " tgt.ntestcode,tgt.stestsynonym,tgt.nsectioncode,s.ssectionname, m.smethodname,"
					+ " ic.sinstrumentcatname " + " from testgrouptest tgt " + testpackageTable
					+ ",section s,method m,instrumentcategory ic, testmaster tm ,testgroupspecsampletype tgts "
					+ " where s.nsectioncode = tgt.nsectioncode " + " and m.nmethodcode =tgt.nmethodcode "
					+ " and ic.ninstrumentcatcode = tgt.ninstrumentcatcode  and tgt.nspecsampletypecode = tgts.nspecsampletypecode   and tgt.nsectioncode="
					+ inputMap.get("nsectioncode") + testpackageValue + "   and tgt.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and tm.ntestcode = tgt.ntestcode " + " and tgt.nisvisible="
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and tm.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tm.ntransactionstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and tgts.nallottedspeccode="
					+ inputMap.get("nallottedspeccode") + " order by tgt.nsorter";
		}
		final List<TestGroupTestForSample> lstTestGroupTest = jdbcTemplate.query(strTestGetQuery,
				new TestGroupTestForSample());
		return new ResponseEntity<Object>(lstTestGroupTest, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createSubSample(Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmap = new ObjectMapper();
		Map<String, Object> returnMap = new HashMap<>();
		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObject = new JSONObject();
		final List<TestGroupTest> tgtTestInputList = objmap.convertValue(inputMap.get("testgrouptest"),
				new TypeReference<List<TestGroupTest>>() {
		});

		SchedulerSubSampleDetail schedulerConfigSample = objmap.convertValue(
				inputMap.get("SchedulerConfigurationSample"), new TypeReference<SchedulerSubSampleDetail>() {
				});

		if (inputMap.containsKey("specBasedComponent")) {
			if ((boolean) inputMap.get("specBasedComponent") == false) {
				final String query = "select nspecsampletypecode from testgroupspecsampletype where nallottedspeccode="
						+ schedulerConfigSample.getNallottedspeccode();

				Integer nspecsampletypecode = jdbcTemplate.queryForObject(query, Integer.class);

				schedulerConfigSample.setNspecsampletypecode(nspecsampletypecode);
			}
		}
		final List<String> dateList = objmap.convertValue(inputMap.get("DateList"), new TypeReference<List<String>>() {
		});

		final UserInfo userInfo = objmap.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		final List<Map<String, Object>> sampleDateConstraint = objmap
				.convertValue(inputMap.get("sampledateconstraints"), new TypeReference<List<Map<String, Object>>>() {
				});

		final List<Map<String, Object>> subSampleDateConstraint = objmap
				.convertValue(inputMap.get("subsampledateconstraints"), new TypeReference<List<Map<String, Object>>>() {
				});

		final List<String> SubSampledateList = objmap.convertValue(inputMap.get("subsampleDateList"),
				new TypeReference<List<String>>() {
		});

		// final int
		// nschedulersamplecode=Integer.parseInt(inputMap.get("nschedulersamplecode").toString());
		String nschedulersamplecode = (String) inputMap.get("nschedulersamplecode");
		List<String> lstSschedulersamplecode = new ArrayList<String>(Arrays.asList(nschedulersamplecode.split(",")));
		List<Integer> lstSschedulersamplecode1 = lstSschedulersamplecode.stream().map(x -> Integer.parseInt(x))
				.collect(Collectors.toList());

		// int subSampleDetailCount = schedulerConfigSample.size();
		String strSchedulerTestInsert = "";
		String strSchedulerSubSampleInsert = "";
		String nschedulersubsamplecode = "";

		strSchedulerSubSampleInsert = "insert into schedulersubsampledetail (nschedulersubsamplecode, nschedulersamplecode, nspecsampletypecode, ncomponentcode, jsondata, jsonuidata, "
				+ " dtransactiondate, noffsetdtransactiondate, ntransdatetimezonecode, nsitecode, nstatus) values ";

		strSchedulerTestInsert = "insert into schedulertestdetail (nschedulertestcode, nschedulersubsamplecode, nschedulersamplecode, ntestgrouptestcode, ntestcode, nsectioncode, nmethodcode, "
				+ "ninstrumentcatcode, nchecklistversioncode, ntestrepeatno, ntestretestno, jsondata, dtransactiondate, noffsetdtransactiondate, ntransdatetimezonecode, nsitecode, nstatus) values ";
		int nsequenceNoSubSample = jdbcTemplate.queryForObject(
				"select nsequenceno from seqnoscheduler where stablename ='schedulersubsampledetail'", Integer.class);
		// int nsequenceNoTest = jdbcTemplate.queryForObject(
		// "select nsequenceno from seqnoscheduler where stablename
		// ='schedulertestdetail'", Integer.class);
		// int nsequenceNoTestParameter = jdbcTemplate.queryForObject(
		// "select nsequenceno from seqnoscheduler where stablename
		// ='schedulertestparameterdetail'",
		// Integer.class);
		String sschedulerTestCode = "";
		int sampleCount = lstSschedulersamplecode1.size();
		for (int i = 0; i < sampleCount; i++) {
			nsequenceNoSubSample++;
			JSONObject jsoneditObj = new JSONObject(schedulerConfigSample.getJsondata());
			JSONObject jsonuiObj = new JSONObject(schedulerConfigSample.getJsonuidata());
			if (!SubSampledateList.isEmpty()) {
				jsoneditObj = (JSONObject) convertInputDateToUTCByZone(jsoneditObj, SubSampledateList, false, userInfo);
				jsonuiObj = (JSONObject) convertInputDateToUTCByZone(jsonuiObj, SubSampledateList, false, userInfo);
			}
			jsonuiObj.put("nschedulersubsamplecode", nsequenceNoSubSample);
			jsonuiObj.put("nschedulersamplecode", lstSschedulersamplecode1.get(i));
			jsonuiObj.put("nspecsampletypecode", schedulerConfigSample.getNspecsampletypecode());
			jsonuiObj.put("ncomponentcode", schedulerConfigSample.getNcomponentcode());
			strSchedulerSubSampleInsert += "(" + nsequenceNoSubSample + ", " + lstSschedulersamplecode1.get(i) + ", "
					+ schedulerConfigSample.getNspecsampletypecode() + ", " + schedulerConfigSample.getNcomponentcode()
					+ ", '" + stringUtilityFunction.replaceQuote(jsoneditObj.toString()) + "'::jsonb, '"
					+ stringUtilityFunction.replaceQuote(jsonuiObj.toString()) + "'::jsonb, '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ").replace("Z", "")
					+ "', " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNtimezonecode() + ", " + userInfo.getNmastersitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";

			nschedulersubsamplecode = nschedulersubsamplecode + "," + nsequenceNoSubSample;

			List<SchedulerSubSampleDetail> comp = new ArrayList();

			comp.add(schedulerConfigSample);

			List<TestGroupTest> lsttest1 = tgtTestInputList.stream().filter(x -> x.getSlno() == comp.get(0).getSlno())
					.collect(Collectors.toList());

			inputMap.put("nschedulersubsamplecode", nsequenceNoSubSample);

			/*
			 * for (TestGroupTest testDetails : lsttest1) { nsequenceNoTest++;
			 * sschedulerTestCode += nsequenceNoTest+ ","; strSchedulerTestInsert += "("+
			 * nsequenceNoTest+ ", "+ nsequenceNoSubSample+ ","+
			 * lstSschedulersamplecode1.get(i)+ ", "+ testDetails.getNtestgrouptestcode()+
			 * ", " + testDetails.getNtestcode()+ ", "+ testDetails.getNsectioncode()+ ", "+
			 * testDetails.getNmethodcode()+ ", "+ testDetails.getNinstrumentcatcode() +
			 * ", -1, 1, 0, json_build_object('nschedulertestcode', "+ nsequenceNoTest+
			 * ", 'nschedulersubsamplecode', "+
			 * nsequenceNoSubSample+", 'nschedulersamplecode', " +
			 * lstSschedulersamplecode1.get(i)+ ", 'ssectionname', '"+
			 * stringUtilityFunction.replaceQuote(testDetails.getSsectionname())+
			 * "', 'smethodname', '"+
			 * stringUtilityFunction.replaceQuote(testDetails.getSmethodname()) +
			 * "', 'ncost',"+ testDetails.getNcost()+ ", 'stestname', '"+
			 * stringUtilityFunction.replaceQuote(testDetails.getStestsynonym())+
			 * "', 'stestsynonym', concat('"+
			 * stringUtilityFunction.replaceQuote(testDetails.getStestsynonym()) + "','["+
			 * testDetails.getNrepeatcountno()+ "][0]'))::jsonb, '"+
			 * dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T",
			 * " ").replace("Z", "")+ "', " +
			 * dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())+
			 * ", "+ userInfo.getNtimezonecode()+ ", "+ userInfo.getNmastersitecode()+ ", "
			 * + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ "),"; }
			 */

		}

		strSchedulerSubSampleInsert = strSchedulerSubSampleInsert.substring(0, strSchedulerSubSampleInsert.length() - 1)
				+ "; ";
		// strSchedulerTestInsert = strSchedulerTestInsert.substring(0,
		// strSchedulerTestInsert.length() - 1)+ "; ";
		// sschedulerTestCode = sschedulerTestCode.substring(0,
		// sschedulerTestCode.length() - 1);

		String strSeqNoUpdate = "update seqnoscheduler set nsequenceno=" + nsequenceNoSubSample
				+ " where stablename='schedulersubsampledetail';";
		// strSeqNoUpdate =strSeqNoUpdate+ "update seqnoscheduler set nsequenceno="+
		// nsequenceNoTest+ " where stablename='schedulertestdetail';";

		jdbcTemplate.execute(strSchedulerSubSampleInsert);
		// jdbcTemplate.execute(strSchedulerTestInsert);
		jdbcTemplate.execute(strSeqNoUpdate);
		inputMap.put("nschedulersubsamplecode", nschedulersubsamplecode.substring(1));

		if (!tgtTestInputList.isEmpty()) {

			Map<String, Object> createdTestMap = (Map<String, Object>) createTest(inputMap, userInfo).getBody();
			inputMap.put("nschedulertestcode", createdTestMap.get("nschedulertestcode"));

		}

		returnMap.putAll((Map<String, Object>) getSchedulerConfigSubSample(inputMap, userInfo).getBody());
		returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
				Enumeration.ReturnStatus.SUCCESS.getreturnstatus());

		jsonAuditObject.put("schedulersubsampledetail", (List<Map<String, Object>>) returnMap.get("selectedSubSample"));

		auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
		auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
		auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
		actionType.put("schedulersubsampledetail", "IDS_ADDSCHEDULERSAMPLE");

		auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getEditSchedulerSubSampleComboService(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();
		if (inputMap.get("nschedulersubsamplecode") == null || inputMap.get("nschedulersubsamplecode") == "") {

			outputMap.put("ReturnStatus",
					commonFunction.getMultilingualMessage("IDS_SELECTSUBSAMPLE", userInfo.getSlanguagefilename()));
			return new ResponseEntity<>(outputMap, HttpStatus.EXPECTATION_FAILED);
		} else {
			final String query = " select json_agg(sssd.jsonuidata ||json_build_object('ntransactionstatus',ssd.ntransactionstatus ,"
					+ " 'stransdisplaystatus', ts.jsondata->'en-US'->>'stransdisplaystatus')::jsonb ) from schedulersubsampledetail sssd,"
					+ " schedulersampledetail ssd,transactionstatus ts,component c where ts.ntranscode=ssd.ntransactionstatus "
					+ " and sssd.nschedulersamplecode=ssd.nschedulersamplecode and sssd.ncomponentcode=c.ncomponentcode "
					+ " and sssd.nschedulersubsamplecode in (" + inputMap.get("nschedulersubsamplecode")
					+ ") and sssd.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and ssd.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and sssd.nsitecode=" + userInfo.getNmastersitecode() + "";

			final String dynamicList = jdbcTemplate.queryForObject(query, String.class);

			List<Map<String, Object>> lstData = projectDAOSupport
					.getSiteLocalTimeFromUTCForDynamicTemplate(dynamicList, userInfo, true,
							(int) inputMap.get("ndesigntemplatemappingcode"), "subsample");
			outputMap.put("EditData", lstData.get(0));

			if (inputMap.containsKey("getSubSampleChildDetail")
					&& (Boolean) inputMap.get("getSubSampleChildDetail") == true) {
				final Map<String, Object> getMapData = new HashMap<String, Object>();
				getMapData.put("nsampletypecode", inputMap.get("nsampletypecode"));
				getMapData.put("nregtypecode", inputMap.get("nregtypecode"));
				getMapData.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
				getMapData.put("npreregno", Integer.toString((Integer) (inputMap.get("nschedulersamplecode"))));
				getMapData.put("ntransactionstatus", inputMap.get("nfilterstatus"));
				getMapData.put("napprovalconfigcode", inputMap.get("napprovalconfigcode"));
				getMapData.put("activeTestTab", inputMap.get("activeTestTab"));
				// getMapData.put("activeSampleTab", inputMap.get("activeSampleTab"));
				// getMapData.put("activeSubSampleTab", inputMap.get("activeSubSampleTab"));
				getMapData.put("nneedsubsample", inputMap.get("nneedsubsample"));
				getMapData.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
				getMapData.put("checkBoxOperation", inputMap.get("checkBoxOperation"));
				getMapData.put("userinfo", userInfo);
				getMapData.put("nschedulersubsamplecode",
						Integer.toString((Integer) inputMap.get("nschedulersubsamplecode")));

				final Map<String, Object> childDetailMap = new HashMap<String, Object>();

				// childDetailMap.putAll((Map<String, Object>) getRegistrationTest(getMapData,
				// userInfo).getBody());
				outputMap.put("SubSampleChildDetail", childDetailMap);
			}

			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> updateSchedulerConfigSubSample(final Map<String, Object> inputMap) throws Exception {
		Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		Map<String, Object> returnMapold = new LinkedHashMap<String, Object>();
		JSONObject jsonAuditOld = new JSONObject();
		JSONObject jsonAuditNew = new JSONObject();
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		Map<String, Object> returnMap = null;
		Map<String, Object> initialParam = (Map<String, Object>) inputMap.get("initialparam");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		// returnMapold = (Map<String, Object>) getRegistrationSubSample(initialParam,
		// userInfo).getBody();
		final String nschedulersubsamplecode = (String) initialParam.get("nschedulersubsamplecode");
		final SchedulerSubSampleDetail config = objMapper.convertValue(inputMap.get("schedulersample"),
				new TypeReference<SchedulerSubSampleDetail>() {
		});

		List<String> dateList = objMapper.convertValue(inputMap.get("SubSampleDateList"),
				new TypeReference<List<String>>() {
		});

		List<Map<String, Object>> subsamplecombinationunique = (List<Map<String, Object>>) inputMap
				.get("subsamplecombinationunique");
		returnMapold = (Map<String, Object>) getSchedulerConfigSubSample(initialParam, userInfo).getBody();

		// Map<String, Object> map1 =
		// validateUniqueConstraint(subsamplecombinationunique,
		// (Map<String, Object>) inputMap.get("registrationsample"), userInfo, "create",
		// RegistrationSample.class,
		// "npreregno", false);
		// if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
		// .equals(map1.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
		// return new ResponseEntity<>(
		// commonFunction.getMultilingualMessage(map1.get("rtn").toString(),
		// userInfo.getSlanguagefilename()),
		// HttpStatus.EXPECTATION_FAILED);
		//
		// }

		JSONObject jsoneditSchedulerConfig = new JSONObject(config.getJsondata());
		JSONObject jsonuiSchedulerConfig = new JSONObject(config.getJsonuidata());
		if (!dateList.isEmpty()) {
			jsoneditSchedulerConfig = (JSONObject) convertInputDateToUTCByZone(jsoneditSchedulerConfig, dateList, false,
					userInfo);
			jsonuiSchedulerConfig = (JSONObject) convertInputDateToUTCByZone(jsonuiSchedulerConfig, dateList, false,
					userInfo);

		}

		String queryString = "update schedulersubsampledetail set jsondata=jsondata||'"
				+ stringUtilityFunction.replaceQuote(jsoneditSchedulerConfig.toString())
				+ "'::jsonb,jsonuidata=jsonuidata||'"
				+ stringUtilityFunction.replaceQuote(jsonuiSchedulerConfig.toString())
				+ "'::jsonb  where nschedulersubsamplecode in (" + nschedulersubsamplecode + ");";

		jdbcTemplate.execute(queryString);

		returnMap = (Map<String, Object>) getSchedulerConfigSubSample(initialParam, userInfo).getBody();
		objmap.put("nregtypecode", initialParam.get("nregtypecode"));
		objmap.put("nregsubtypecode", initialParam.get("nregsubtypecode"));
		objmap.put("ndesigntemplatemappingcode", initialParam.get("ndesigntemplatemappingcode"));
		actionType.put("schedulersubsampledetail", "IDS_EDITSUBSAMPLE");
		jsonAuditOld.put("schedulersubsampledetail", (List<Map<String, Object>>) returnMapold.get("selectedSubSample"));
		jsonAuditNew.put("schedulersubsampledetail", (List<Map<String, Object>>) returnMap.get("selectedSubSample"));
		auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, jsonAuditNew, actionType, objmap, false, userInfo);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> deleteSchedulerConfigSubSample(Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();

		objMapper.registerModule(new JavaTimeModule());

		Map<String, Object> returnMap = null;

		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final String nschedulersubsamplecode = (String) inputMap.get("nschedulersubsamplecode");

		List<Map<String, Object>> lstDataSubSample = auditSubSampleGet(inputMap, userInfo);

		String queryString = "update schedulersubsampledetail set nstatus="
				+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "  where nschedulersubsamplecode in ("
				+ nschedulersubsamplecode + ");";
		queryString = queryString + "update schedulertestdetail set nstatus="
				+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "  where nschedulersubsamplecode in ("
				+ nschedulersubsamplecode + ");";
		jdbcTemplate.execute(queryString);

		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObject = new JSONObject();

		auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
		auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
		auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
		actionType.put("schedulersubsampledetail", "IDS_DELETESUBSAMPLE");
		jsonAuditObject.put("schedulersubsampledetail", lstDataSubSample);
		auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);

		inputMap.remove("nschedulersubsamplecode");
		returnMap = (Map<String, Object>) getSchedulerConfigSubSample(inputMap, userInfo).getBody();

		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> deleteSchedulerConfigTest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();

		objMapper.registerModule(new JavaTimeModule());

		Map<String, Object> returnMap = null;

		final String nschedulertestcode = (String) inputMap.get("nschedulertestcode");

		// final String nschedulertestcode = (String)
		// inputMap.get("nschedulertestcode");
		List<Map<String, Object>> lstDataTest = auditTestGet(inputMap, userInfo);

		String queryString = "";
		queryString = queryString + "update schedulertestdetail set nstatus="
				+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "  where nschedulertestcode in ("
				+ nschedulertestcode + ");";
		queryString = queryString + "update schedulertestparameterdetail set nstatus="
				+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "  where nschedulertestcode in ("
				+ nschedulertestcode + ");";

		jdbcTemplate.execute(queryString);

		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();

		JSONObject actionType = new JSONObject();

		JSONObject jsonAuditObject = new JSONObject();

		auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
		auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
		auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
		actionType.put("schedulertestdetail", "IDS_DELETETEST");
		jsonAuditObject.put("schedulertestdetail", lstDataTest);
		auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);

		inputMap.remove("nschedulertestcode");

		returnMap = (Map<String, Object>) getSchedulerConfigSubSample(inputMap, userInfo).getBody();

		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getMoreTestPackage(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		String strtestpackageQuery = "";

		strtestpackageQuery = " select tgt.ntestpackagecode,tp.stestpackagename from testgrouptest tgt,testpackage tp "
				+ " where tp.ntestpackagecode=tgt.ntestpackagecode and tgt.nspecsampletypecode in ("
				+ inputMap.get("nspecsampletypecode") + ") " + " and tgt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgt.ntestpackagecode <> -1 "
				+ " group by tgt.ntestpackagecode,tp.stestpackagename ";

		final List<TestGroupTestForSample> lstTestPackage = jdbcTemplate.query(strtestpackageQuery,
				new TestGroupTestForSample());
		return new ResponseEntity<Object>(lstTestPackage, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getMoreTest(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final String sTestQuery = "select tgt.nrepeatcountno," // 1 as nrepeatcountno, "
				+ " tgt.ntestgrouptestcode, tgt.stestsynonym, tgt.nspecsampletypecode, tgt.nsectioncode, "
				+ " (select count(tgtp.ntestgrouptestparametercode) from testgrouptestparameter tgtp "
				+ " where  tgtp.ntestgrouptestcode=tgt.ntestgrouptestcode " + " and tgtp.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  tgtp.nisvisible="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ") as nparametercount "
				+ " from testgrouptest tgt, testgroupspecsampletype tgsst, testmaster tm"
				+ " where tm.ntestcode = tgt.ntestcode and tgsst.nspecsampletypecode = tgt.nspecsampletypecode "
				+ " and tgsst.nspecsampletypecode in (" + inputMap.get("nspecsampletypecode")
				+ ") and tm.nstatus = tgt.nstatus and tgsst.nstatus = tgt.nstatus and tgt.nisvisible="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and tgsst.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tm.ntransactionstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return new ResponseEntity<Object>(jdbcTemplate.query(sTestQuery, new TestGroupTest()), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getMoreTestSection(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		String getTestSectionQuery = "";

		getTestSectionQuery = "select tgt.nsectioncode,s.ssectionname "
				+ " from testgrouptest tgt left JOIN testpackage tp on tgt.ntestpackagecode=tp.ntestpackagecode and "
				+ " tp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " ,section s where s.nsectioncode = tgt.nsectioncode and " + " tgt.nspecsampletypecode in ("
				+ inputMap.get("nspecsampletypecode") + ") and " + " tgt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by tgt.nsectioncode,s.ssectionname ";
		final List<TestGroupTestForSample> lstTestPackage = jdbcTemplate.query(getTestSectionQuery,
				new TestGroupTestForSample());
		return new ResponseEntity<Object>(lstTestPackage, HttpStatus.OK);
	}

	private Map<String, Object> createQueryCreateTestParameterHistory(SchedulerSubSampleDetail objSchedulerSampleDetail,
			String comPleted, List<String> replicateTestList, List<String> replicateTestParameterList,
			UserInfo userInfo, String sntestgrouptestcode, final Map<String, Object> inputMap)
					throws JsonProcessingException, Exception {
		final ObjectMapper objMapper = new ObjectMapper();

		List<Integer> transactionTestCodeList = new ArrayList<>();
		List<Integer> schedulerparameterCodeList = new ArrayList<>();

		final String testGroupQuery = " select  tgt.nrepeatcountno, tgt.ntestgrouptestcode, tgt.ntestcode,tgt.stestsynonym, tgt.nsectioncode,"
				+ " s.ssectionname, tgt.nmethodcode, m.smethodname, tgt.ninstrumentcatcode, tm.nchecklistversioncode,"
				+ " coalesce((" + " select max(ntestrepeatno) + 1 from schedulertestdetail "
				+ " where nschedulersubsamplecode in (" + inputMap.get("nschedulersubsamplecode") + ")"
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ntestgrouptestcode = tgt.ntestgrouptestcode" + " ),1) ntestrepeatno,"
				+ Enumeration.TransactionStatus.ALL.gettransactionstatus() + " ntestretestno, tgt.ncost,"
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus"
				+ " from testgrouptest tgt, testmaster tm, transactionstatus ts, section s,method m  "
				+ " where tm.ntestcode = tgt.ntestcode" + " and tgt.ntestgrouptestcode in (" + comPleted
				+ ") and tgt.nstatus = tm.nstatus" + " and tgt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + " ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tgt.nsectioncode = s.nsectioncode and  ts.ntranscode in (8) "
				+ " and tgt.nmethodcode=m.nmethodcode " + " and s.nstatus= m.nstatus and m.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		List<SchedulerTestDetail> testGroupTestList = jdbcTemplate.query(testGroupQuery, new SchedulerTestDetail());

		int nsequenceNoTest = jdbcTemplate.queryForObject(
				"select nsequenceno from seqnoscheduler where stablename ='schedulertestdetail'", Integer.class);
		int nsequenceNoTestParameter = jdbcTemplate.queryForObject(
				"select nsequenceno from seqnoscheduler where stablename ='schedulertestparameterdetail'",
				Integer.class);

		String strQuery = "";
		String strAdhocQuery = "";
		if ((int) inputMap.get("nsampletypecode") == Enumeration.SampleType.CLINICALSPEC.getType()) {
			final int nschedulersamplecode = objSchedulerSampleDetail.getNschedulersamplecode();
			Optional<Map<String, Object>> age = ((List<Map<String, Object>>) inputMap.get("ageData")).stream()
					.filter(x -> ((Integer) x.get("nschedulersamplecode")) == nschedulersamplecode).findAny();
			int nage = (int) age.get().get("nage");
			int ngendercode = (int) age.get().get("ngendercode");

			strQuery = " case when tgtp.nparametertypecode=" + Enumeration.ParameterType.NUMERIC.getparametertype()
			+ " then case when  " + " tgtcs.ngendercode=" + ngendercode + " and " + nage
			+ " between tgtcs.nfromage and tgtcs.ntoage then "
			+ " json_build_object('nfromage',tgtcs.nfromage,'ntoage',tgtcs.ntoage,'ngendercode',tgtcs.ngendercode,'ngradecode',tgtcs.ngradecode,"
			+ " 'sminb',case when tgtcs.slowb='null' then NULL else tgtcs.slowb end,"
			+ " 'smina',case when tgtcs.slowa='null' then NULL else tgtcs.slowa end,"
			+ " 'smaxa',case when tgtcs.shigha='null' then NULL else tgtcs.shigha end,"
			+ " 'smaxb',case when tgtcs.shighb='null' then NULL else tgtcs.shighb end,"
			+ " 'sresultvalue',case when tgtcs.sresultvalue='null' then NULL else tgtcs.sresultvalue end,"
			+ " 'nroundingdigits',tgtp.nroundingdigits,'sresult',NULL,'sfinal',NULL,'stestsynonym',concat(tgt.stestsynonym,'[',cast(  "
			+ " coalesce((select max(ntestrepeatno)  from schedulersubsampledetail "
			+ "	where nschedulersubsamplecode in (" + inputMap.get("nschedulersubsamplecode") + ")"
			+ ") and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
			+ "  and nsitecode=" + userInfo.getNmastersitecode()
			+ " and ntestgrouptestcode = tgt.ntestgrouptestcode" + "	),1)"
			+ " as character varying),']','[0]'),'sparametersynonym',tgtp.sparametersynonym,'nresultaccuracycode',tgtp.nresultaccuracycode,'sresultaccuracyname', ra.sresultaccuracyname)::jsonb "
			+ " else "
			+ " json_build_object('nfromage',tgtcs.nfromage,'ntoage',tgtcs.ntoage,'ngendercode',tgtcs.ngendercode,'ngradecode',tgtcs.ngradecode,"
			+ " 'sminb',case when tgtcs.slowb='null' then NULL else tgtcs.slowb end,"
			+ " 'smina',case when tgtcs.slowa='null' then NULL else tgtcs.slowa end,"
			+ " 'smaxa',case when tgtcs.shigha='null' then NULL else tgtcs.shigha end,"
			+ " 'smaxb',case when tgtcs.shighb='null' then NULL else tgtcs.shighb end,"
			+ " 'sresultvalue',case when tgtcs.sresultvalue='null' then NULL else tgtcs.sresultvalue end,"
			+ " 'nroundingdigits',tgtp.nroundingdigits,'sresult',NULL,'sfinal',NULL,'stestsynonym',concat(tgt.stestsynonym,'[',cast(  "
			+ " coalesce((select max(ntestrepeatno)  from schedulersubsampledetail "
			+ "	where nschedulersubsamplecode in (" + inputMap.get("nschedulersubsamplecode") + ")"
			+ ") and nsitecode = " + userInfo.getNmastersitecode() + " and nstatus = "
			+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
			+ " and ntestgrouptestcode = tgt.ntestgrouptestcode" + "	),1)"
			+ " as character varying),']','[0]'),'sparametersynonym',tgtp.sparametersynonym,'nresultaccuracycode',tgtp.nresultaccuracycode,'sresultaccuracyname', ra.sresultaccuracyname)::jsonb "
			+ " end else " + " json_build_object("
			+ " 'sminb',case when tgtcs.slowb='null' then NULL else tgtcs.slowb end,"
			+ " 'smina',case when tgtcs.slowa='null' then NULL else tgtcs.slowa end,"
			+ " 'smaxa',case when tgtcs.shigha='null' then NULL else tgtcs.shigha end,"
			+ " 'smaxb',case when tgtcs.shighb='null' then NULL else tgtcs.shighb end,"
			+ " 'sresultvalue',case when tgtcs.sresultvalue='null' then NULL else tgtcs.sresultvalue end,"
			+ "'nroundingdigits',tgtp.nroundingdigits,'sresult',NULL,'sfinal',NULL,'stestsynonym',concat(tgt.stestsynonym,'[',cast(  "
			+ " coalesce((select max(ntestrepeatno)  from schedulersubsampledetail "
			+ "	where nschedulersubsamplecode in (" + inputMap.get("nschedulersubsamplecode") + ""
			+ ") and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
			+ " and nsitecode = " + userInfo.getNmastersitecode()
			+ "and ntestgrouptestcode = tgt.ntestgrouptestcode" + "	),1)"
			+ " as character varying),']','[0]'),'sparametersynonym',tgtp.sparametersynonym,'nresultaccuracycode',tgtp.nresultaccuracycode,'sresultaccuracyname', ra.sresultaccuracyname)::jsonb "
			+ " end jsondata," + userInfo.getNmastersitecode() + " nsitecode,"
			+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
			+ " from resultaccuracy ra,testmaster tm,testgrouptest tgt inner join "
			+ " testgrouptestparameter tgtp  on tgt.ntestgrouptestcode = tgtp.ntestgrouptestcode "
			+ "	left outer join testgrouptestclinicalspec tgtcs on tgtcs.ntestgrouptestparametercode = tgtp.ntestgrouptestparametercode "
			+ "	and tgtcs.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
			+ " and tgtcs.ngendercode=" + ngendercode + " and " + nage
			+ " between tgtcs.nfromage and tgtcs.ntoage ";
			strAdhocQuery = " and tgtp.nisadhocparameter=" + Enumeration.TransactionStatus.NO.gettransactionstatus();

		} else {
			strQuery = "json_build_object('sminlod',tgtnp.sminlod,'smaxlod',tgtnp.smaxlod,'sminb',tgtnp.sminb,'smina',tgtnp.smina,"
					+ "'smaxa',tgtnp.smaxa,'smaxb',tgtnp.smaxb,'sminloq',tgtnp.sminloq,'smaxloq',tgtnp.smaxloq,'ngradecode',tgtnp.ngradecode,'nresultaccuracycode',tgtp.nresultaccuracycode,'sresultaccuracyname',ra.sresultaccuracyname, "
					+ "'sdisregard',tgtnp.sdisregard,'sresultvalue',tgtnp.sresultvalue,"
					+ "'nroundingdigits',tgtp.nroundingdigits,'sresult',NULL,'sfinal',NULL,'stestsynonym',concat(tgt.stestsynonym,'[',cast(  "
					+ " coalesce((select max(ntestrepeatno)  from schedulertestdetail "
					+ "	where nschedulersubsamplecode in (" + inputMap.get("nschedulersubsamplecode") + ""
					+ ") and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode=" + userInfo.getNmastersitecode()
					+ " and ntestgrouptestcode = tgt.ntestgrouptestcode" + "	),1)"
					+ " as character varying),']','[0]'),'sparametersynonym',tgtp.sparametersynonym)::jsonb jsondata"
					+ " ," + userInfo.getNmastersitecode() + " nsitecode,"
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
					+ "from resultaccuracy ra,testmaster tm,testgrouptest tgt,testgrouptestparameter tgtp ";
			strAdhocQuery = " and tgtp.nisvisible=" + Enumeration.TransactionStatus.YES.gettransactionstatus();
		}

		final String query4 = " select  tgt.ntestgrouptestcode, tgtp.ntestgrouptestparametercode, tgtp.ntestparametercode,"
				+ " tgtp.nparametertypecode, coalesce(tgtf.ntestgrouptestformulacode, "
				+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ")"
				+ " ntestgrouptestformulacode, tgtp.nunitcode, tgtp.nresultmandatory, tgtp.nreportmandatory," + strQuery
				+ " left outer join testgrouptestformula tgtf"
				+ " on tgtp.ntestgrouptestparametercode = tgtf.ntestgrouptestparametercode" + " and tgtf.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgtf.ntransactionstatus = "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " " + strAdhocQuery
				// " and tgtp.nisadhocparameter = "+
				// Enumeration.TransactionStatus.NO.gettransactionstatus()
				+ " left outer join testgrouptestnumericparameter tgtnp on tgtp.ntestgrouptestparametercode = tgtnp.ntestgrouptestparametercode"
				+ " and tgtnp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " where tgt.ntestgrouptestcode = tgtp.ntestgrouptestcode and tm.ntestcode = tgt.ntestcode and tgtp.nresultaccuracycode=ra.nresultaccuracycode and tgt.nstatus = tm.nstatus"
				+ " and tgt.nstatus = tgtp.nstatus and ra.nstatus=tgtp.nstatus and tgt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgt.ntestgrouptestcode in ("
				+ comPleted + ") " + strAdhocQuery + ";";

		final List<TestGroupTestParameter> parameterList = jdbcTemplate.query(query4, new TestGroupTestParameter());

		for (SchedulerTestDetail regTestGroupTest : testGroupTestList) {
			if (regTestGroupTest.getNrepeatcountno() > 1) {
				for (int repeatNo = regTestGroupTest.getNtestrepeatno(); repeatNo < (regTestGroupTest.getNtestrepeatno()
						+ regTestGroupTest.getNrepeatcountno()); repeatNo++)

				{
					int nttestcode = ++nsequenceNoTest;
					transactionTestCodeList.add(nttestcode);

					replicateTestList.add("(" + nttestcode + "," + objSchedulerSampleDetail.getNschedulersubsamplecode()
					+ "," + objSchedulerSampleDetail.getNschedulersamplecode() + ","
					+ regTestGroupTest.getNtestgrouptestcode() + "," + regTestGroupTest.getNtestcode() + ","
					+ regTestGroupTest.getNsectioncode() + "," + regTestGroupTest.getNmethodcode() + " , "
					+ regTestGroupTest.getNinstrumentcatcode() + "" + ",-1," + repeatNo + ",0,"
					+ " json_build_object('nschedulertestcode', " + nttestcode + ",'nschedulersamplecode',"
					+ objSchedulerSampleDetail.getNschedulersamplecode() + ",'nschedulersubsamplecode',"
					+ objSchedulerSampleDetail.getNschedulersubsamplecode() + ",'ssectionname','"
					+ stringUtilityFunction.replaceQuote(regTestGroupTest.getSsectionname())
					+ "','smethodname','"
					+ stringUtilityFunction.replaceQuote(regTestGroupTest.getSmethodname()) + "','ncost',"
					+ regTestGroupTest.getNcost() + "," + "'stestname','"
					+ stringUtilityFunction.replaceQuote(regTestGroupTest.getStestsynonym()) + "',"
					+ "'stestsynonym',concat('"
					+ stringUtilityFunction.replaceQuote(regTestGroupTest.getStestsynonym()) + "','[" + repeatNo
					+ "][" + regTestGroupTest.getNtestretestno() + "]'))::jsonb,'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + ",-1,-1,' "
					+ userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")");

					for (TestGroupTestParameter schedulerconfigParameter : parameterList) {
						if (regTestGroupTest.getNtestgrouptestcode() == schedulerconfigParameter
								.getNtestgrouptestcode()) {
							int nschedulertestparametercode = ++nsequenceNoTestParameter;
							schedulerparameterCodeList.add(nschedulertestparametercode);
							final Map<String, Object> mapObject = schedulerconfigParameter.getJsondata();
							mapObject.put("nschedulertestparametercode", nschedulertestparametercode);
							mapObject.put("nschedulertestcode", nttestcode);
							mapObject.put("stestsynonym", regTestGroupTest.getStestsynonym() + "[" + repeatNo + "]["
									+ regTestGroupTest.getNtestretestno() + "]");

							replicateTestParameterList.add("(" + nschedulertestparametercode + "," + nttestcode + ""
									+ objSchedulerSampleDetail.getNschedulersamplecode() + ","
									+ schedulerconfigParameter.getNtestgrouptestparametercode() + ","
									+ schedulerconfigParameter.getNtestparametercode() + ","
									+ schedulerconfigParameter.getNparametertypecode() + ","
									+ schedulerconfigParameter.getNtestgrouptestformulacode() + ","
									+ schedulerconfigParameter.getNreportmandatory() + ","
									+ schedulerconfigParameter.getNresultmandatory() + ","
									+ schedulerconfigParameter.getNunitcode() + "," + "'"
									+ stringUtilityFunction.replaceQuote(objMapper.writeValueAsString(mapObject)) + "',"
									+ " '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',-1,-1, "
									+ userInfo.getNmastersitecode() + ","
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")");
						}
					}
				}
			} else {
				int nttestcode = ++nsequenceNoTest;
				transactionTestCodeList.add(nttestcode);

				replicateTestList.add("(" + nttestcode + "," + objSchedulerSampleDetail.getNschedulersubsamplecode()
				+ "," + objSchedulerSampleDetail.getNschedulersamplecode() + ","
				+ regTestGroupTest.getNtestgrouptestcode() + "," + regTestGroupTest.getNtestcode() + ","
				+ regTestGroupTest.getNsectioncode() + "," + regTestGroupTest.getNmethodcode() + " , "
				+ regTestGroupTest.getNinstrumentcatcode() + "" + ",-1," + (regTestGroupTest.getNtestrepeatno())
				+ ",0," + " json_build_object('nschedulertestcode', " + nttestcode + ",'nschedulersamplecode',"
				+ objSchedulerSampleDetail.getNschedulersamplecode() + ",'nschedulersubsamplecode',"
				+ objSchedulerSampleDetail.getNschedulersubsamplecode() + ",'ssectionname','"
				+ stringUtilityFunction.replaceQuote(regTestGroupTest.getSsectionname()) + "','smethodname','"
				+ stringUtilityFunction.replaceQuote(regTestGroupTest.getSmethodname()) + "','ncost',"
				+ regTestGroupTest.getNcost() + "," + "'stestname','"
				+ stringUtilityFunction.replaceQuote(regTestGroupTest.getStestsynonym()) + "',"
				+ "'stestsynonym',concat('"
				+ stringUtilityFunction.replaceQuote(regTestGroupTest.getStestsynonym()) + "','["
				+ regTestGroupTest.getNtestrepeatno() + "][0]'))::jsonb,'"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',-1,-1, " + userInfo.getNmastersitecode()
				+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")");

				for (TestGroupTestParameter schedulerconfigParameter : parameterList) {
					if (regTestGroupTest.getNtestgrouptestcode() == schedulerconfigParameter.getNtestgrouptestcode()) {

						int nschedulertestparametercode = ++nsequenceNoTestParameter;
						schedulerparameterCodeList.add(nschedulertestparametercode);

						final Map<String, Object> mapObject = schedulerconfigParameter.getJsondata();
						mapObject.put("nschedulertestparametercode", nschedulertestparametercode);
						mapObject.put("nschedulertestcode", nttestcode);
						mapObject.put("stestsynonym", regTestGroupTest.getStestsynonym() + "["
								+ (regTestGroupTest.getNtestrepeatno()) + "][0]");

						replicateTestParameterList.add("(" + nschedulertestparametercode + "," + nttestcode + ","
								+ objSchedulerSampleDetail.getNschedulersamplecode() + ","
								+ schedulerconfigParameter.getNtestgrouptestparametercode() + ","
								+ schedulerconfigParameter.getNtestparametercode() + ","
								+ schedulerconfigParameter.getNparametertypecode() + ","
								+ schedulerconfigParameter.getNtestgrouptestformulacode() + ","
								+ schedulerconfigParameter.getNreportmandatory() + ","
								+ schedulerconfigParameter.getNresultmandatory() + ","
								+ schedulerconfigParameter.getNunitcode() + "," + "'"
								+ stringUtilityFunction.replaceQuote(objMapper.writeValueAsString(mapObject)) + "',"
								+ " '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',-1,-1, "
								+ userInfo.getNmastersitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")");
					}
				}

			}
		}

		final Map<String, Object> returnMap = new HashMap<String, Object>() {
		};
		returnMap.put("replicateTestList", replicateTestList);
		returnMap.put("replicateTestParameterList", replicateTestParameterList);
		returnMap.put("transactionTestCodeList", transactionTestCodeList);
		returnMap.put("schedulerparameterCodeList", schedulerparameterCodeList);

		return returnMap;

	}

	@Override
	public ResponseEntity<Object> createTest(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		final List<TestGroupTest> listTest = objMapper.convertValue(inputMap.get("testgrouptest"),
				new TypeReference<List<TestGroupTest>>() {
		});
		final List<String> listSample = objMapper.convertValue(inputMap.get("ScheudlerSample"),
				new TypeReference<List<String>>() {
		});
		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObject = new JSONObject();

		List<String> replicateTestList = new ArrayList<String>();
		List<String> replicateTestParameterList = new ArrayList<String>();
		List<Integer> schedulerTestCodeList = new ArrayList<Integer>();
		List<Integer> schedulerparameterCodeList = new ArrayList<Integer>();
		List<Integer> schedulerGetTestCodeList = new ArrayList<Integer>();

		final int nregtypecode = (int) inputMap.get("nregtypecode");
		final int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
		final String sntestgrouptestcode = stringUtilityFunction.fnDynamicListToString(listTest,
				"getNtestgrouptestcode");
		String createdTestCode = "";

		final String sFindSubSampleQuery = "select sssd.nschedulersamplecode, sssd.nschedulersubsamplecode from schedulersubsampledetail sssd,schedulersampledetail ssd "
				+ " where ssd.nschedulersamplecode=sssd.nschedulersamplecode and "
				+ "  sssd.nschedulersubsamplecode in (" + inputMap.get("nschedulersubsamplecode")
				+ ") and ntransactionstatus not in (" + Enumeration.TransactionStatus.REJECTED.gettransactionstatus()
				+ ", " + Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ")" + "  and sssd.nsitecode="
				+ userInfo.getNmastersitecode() + " and sssd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by sssd.nschedulersubsamplecode,sssd.nschedulersamplecode";

		List<SchedulerSubSampleDetail> listAvailableSample = jdbcTemplate.query(sFindSubSampleQuery,
				new SchedulerSubSampleDetail());

		final String sParameterQuery = "select case when tgtp.nparametertypecode = "
				+ Enumeration.ParameterType.NUMERIC.getparametertype() + " then tgtnp.sresultvalue"
				+ " else case when tgtp.nparametertypecode = " + Enumeration.ParameterType.PREDEFINED.getparametertype()
				+ " then tgtpp.spredefinedname else case when tgtp.nparametertypecode = "
				+ Enumeration.ParameterType.CHARACTER.getparametertype()
				+ " then tgtcp.scharname else null end end end sresultvalue,"
				+ " tgtp.nparametertypecode, tgtp.ntestgrouptestparametercode, tgt.ntestgrouptestcode, tgtp.nresultmandatory"
				+ " from testgrouptest tgt inner join testgrouptestparameter tgtp on tgtp.ntestgrouptestcode = tgt.ntestgrouptestcode"
				+ " and tgtp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " left outer join testgrouptestcharparameter tgtcp on tgtcp.ntestgrouptestparametercode = tgtp.ntestgrouptestparametercode"
				+ " and tgtcp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " left outer join testgrouptestnumericparameter tgtnp on tgtnp.ntestgrouptestparametercode = tgtp.ntestgrouptestparametercode"
				+ " and tgtnp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " left outer join testgrouptestpredefparameter tgtpp on tgtpp.ntestgrouptestparametercode = tgtp.ntestgrouptestparametercode"
				+ " and tgtpp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " where tgt.ntestgrouptestcode in (" + sntestgrouptestcode + ") and tgt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();// + ";";

		final List<TestGroupTestParameter> parameterList = jdbcTemplate.query(sParameterQuery,
				new TestGroupTestParameter());

		for (SchedulerSubSampleDetail objRegistrationSample : listAvailableSample) {
			final Map<String, Object> testMap = createQueryCreateTestParameterHistory(objRegistrationSample, // nRecievedStatus,
					sntestgrouptestcode, replicateTestList, replicateTestParameterList, userInfo, sntestgrouptestcode,
					inputMap);

			replicateTestList = (ArrayList<String>) testMap.get("replicateTestList");
			replicateTestParameterList = (ArrayList<String>) testMap.get("replicateTestParameterList");
			schedulerTestCodeList = (ArrayList<Integer>) testMap.get("transactionTestCodeList");
			schedulerparameterCodeList = (ArrayList<Integer>) testMap.get("schedulerparameterCodeList");

			String strSchedulerTestInsert = "";

			if (!replicateTestList.isEmpty()) {
				strSchedulerTestInsert = "insert into schedulertestdetail (nschedulertestcode, nschedulersubsamplecode, nschedulersamplecode, ntestgrouptestcode, ntestcode, nsectioncode, nmethodcode, "
						+ "ninstrumentcatcode, nchecklistversioncode, ntestrepeatno, ntestretestno, jsondata, dtransactiondate, noffsetdtransactiondate, ntransdatetimezonecode, nsitecode, nstatus) values ";
				jdbcTemplate.execute(strSchedulerTestInsert + String.join(",", replicateTestList));
				replicateTestList.clear();
			}
			if (!schedulerTestCodeList.isEmpty()) {

				jdbcTemplate.execute("update seqnoscheduler set nsequenceno="
						+ schedulerTestCodeList.get(schedulerTestCodeList.size() - 1)
						+ " where stablename='schedulertestdetail';");

				createdTestCode = String.join(",",
						schedulerTestCodeList.stream().map(String::valueOf).collect(Collectors.toList()));

				schedulerTestCodeList.clear();
			}
			if (!schedulerparameterCodeList.isEmpty()) {
				jdbcTemplate.execute("update seqnoscheduler set nsequenceno="
						+ schedulerparameterCodeList.get(schedulerparameterCodeList.size() - 1)
						+ " where stablename='schedulertestparameterdetail';");
				schedulerparameterCodeList.clear();
			}
			String strTestParameter = "";

			if (!replicateTestParameterList.isEmpty()) {
				strTestParameter = "insert into schedulertestparameterdetail (nschedulertestparametercode, nschedulertestcode,"
						+ " nschedulersamplecode, ntestgrouptestparametercode, ntestparametercode, nparametertypecode, ntestgrouptestformulacode,"
						+ " nreportmandatory, nresultmandatory, nunitcode, jsondata, dtransactiondate, noffsetdtransactiondate, ntransdatetimezonecode,"
						+ " nsitecode, nstatus)  values ";
				jdbcTemplate.execute(strTestParameter + String.join(",", replicateTestParameterList));

				replicateTestParameterList.clear();

			}

		}
		Map<String, Object> map = new HashMap<>();

		map.put("nschedulertestcode", createdTestCode);

		if (inputMap.containsKey("directAddTest")) {
			inputMap.put("nschedulertestcode", createdTestCode);

			map = (Map<String, Object>) getSchedulerConfigTest(inputMap, userInfo).getBody();

			jsonAuditObject.put("schedulertestdetail", (List<Map<String, Object>>) map.get("selectedTest"));
			auditmap.put("nregtypecode", nregtypecode);
			auditmap.put("nregsubtypecode", nregsubtypecode);
			auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
			actionType.put("schedulertestdetail", "IDS_ADDTEST");
			auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);
		}

		return new ResponseEntity<Object>(map, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getEditSchedulerConfigDetails(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		final String query = "select json_agg( sd.jsonuidata||sd.jsondata||json_build_object('sspecname',tgs.sspecname,'sversion',tgs.sversion,'ntransactionstatus',sd.ntransactionstatus,"
				+ "'stransdisplaystatus',ts.jsondata->'" + userInfo.getSlanguagetypecode()
				+ "'->>'stransdisplaystatus','sarno',sd.nschedulersamplecode,'ncategorybasedflow',pc.ncategorybasedflow)::jsonb ) "
				+ "	 from schedulersampledetail sd,schedulersubsampledetail ssd, transactionstatus ts,productcategory pc,testgroupspecification tgs "
				+ "	where ssd.nschedulersamplecode=sd.nschedulersamplecode and pc.nproductcatcode=sd.nproductcatcode and sd.nallottedspeccode=tgs.nallottedspeccode   and  pc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "	and ts.ntranscode = sd.ntransactionstatus and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "	and sd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  "
				+ " and sd.nschedulersamplecode in (" + inputMap.get("nschedulersamplecode")
				+ ")  group by   sd.nschedulersamplecode ";

		final String dynamicList = jdbcTemplate.queryForObject(query, String.class);

		List<Map<String, Object>> lstData = projectDAOSupport
				.getSiteLocalTimeFromUTCForDynamicTemplate(dynamicList, userInfo, true,
						(int) inputMap.get("ndesigntemplatemappingcode"), "sample");

		final Map<String, Object> outputMap = new HashMap<String, Object>();
		outputMap.put("EditData", lstData.get(0));

		final int productCatCode = (int) lstData.get(0).get("nproductcatcode");
		int productCode = (int) lstData.get(0).get("nproductcode");
		final int sampleTypeCode = (int) inputMap.get("nsampletypecode");
		// final int needSubSample = (int) inputMap.get("nneedsubsample");
		final Boolean needSubSample = (Boolean) inputMap.get("nneedsubsample");
		int ncategorybasedflow = (int) lstData.get(0).get("ncategorybasedflow");
		int projectMasterCode = -1;
		int nallottedspeccode = (int) inputMap.get("nallottedspeccode");
		if (sampleTypeCode == Enumeration.SampleType.PROJECTSAMPLETYPE.getType()) {
			projectMasterCode = (int) lstData.get(0).get("nprojectmastercode");
		}

		if (ncategorybasedflow == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
			productCode = -1;
		}

		int subSampleNeed = Enumeration.TransactionStatus.NO.gettransactionstatus();

		if (needSubSample) {
			subSampleNeed = Enumeration.TransactionStatus.YES.gettransactionstatus();
		}
		// outputMap.putAll(Sampleregistrationtreeload(productCatCode, productCode,
		// sampleTypeCode, projectMasterCode,
		// subSampleNeed, -1, -1));

		if (inputMap.containsKey("getSampleChildDetail") && (Boolean) inputMap.get("getSampleChildDetail") == true) {
			final Map<String, Object> getMapData = new HashMap<String, Object>();

			if (!needSubSample) {
				getMapData.put("ntype", 2);
			}

			getMapData.put("nflag", 2);
			getMapData.put("nsampletypecode", inputMap.get("nsampletypecode"));
			getMapData.put("nregtypecode", inputMap.get("nregtypecode"));
			getMapData.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
			getMapData.put("nschedulersamplecode", Integer.toString((Integer) (inputMap.get("nschedulersamplecode"))));
			getMapData.put("ntransactionstatus", inputMap.get("nfilterstatus"));
			getMapData.put("napprovalconfigcode", inputMap.get("napprovalconfigcode"));
			getMapData.put("activeTestTab", inputMap.get("activeTestTab"));
			getMapData.put("activeSampleTab", inputMap.get("activeSampleTab"));
			getMapData.put("activeSubSampleTab", inputMap.get("activeSubSampleTab"));
			getMapData.put("nneedsubsample", inputMap.get("nneedsubsample"));
			getMapData.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
			getMapData.put("checkBoxOperation", inputMap.get("checkBoxOperation"));
			getMapData.put("userinfo", userInfo);
			getMapData.put("nprojectmastercode", projectMasterCode);

			final Map<String, Object> childDetailMap = new HashMap<String, Object>();

			childDetailMap.putAll((Map<String, Object>) getSchedulerConfigSubSample(getMapData, userInfo).getBody());
			outputMap.put("SampleChildDetail", childDetailMap);
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> updateSchedulerConfig(final Map<String, Object> inputMap) throws Exception {
		Map<String, Object> auditmap = new LinkedHashMap<>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditOld = new JSONObject();
		JSONObject jsonAuditNew = new JSONObject();
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		Map<String, Object> returnMap = null;
		Map<String, Object> initialParam = (Map<String, Object>) inputMap.get("initialparam");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		final String nschedulersamplecode = (String) initialParam.get("nschedulersamplecode");
		final SchedulerSampleDetail scheduler = objMapper.convertValue(inputMap.get("schedulerconfiguration"),
				new TypeReference<SchedulerSampleDetail>() {
		});

		List<Map<String, Object>> samplecombinationunique = (List<Map<String, Object>>) inputMap
				.get("samplecombinationunique");
		Map<String, Object> map = validateUniqueConstraintScheduler(samplecombinationunique,
				(Map<String, Object>) inputMap.get("schedulerconfiguration"), userInfo, "update",
				SchedulerSampleDetail.class, "nschedulersamplecode", false);
		if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
				.equals(map.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
			return new ResponseEntity<>(map, HttpStatus.OK);
		}

		List<String> dateList = objMapper.convertValue(inputMap.get("DateList"), new TypeReference<List<String>>() {
		});

		JSONObject jsoneditSchedulerConfig = new JSONObject(scheduler.getJsondata());
		JSONObject jsonuiSchedulerConfig = new JSONObject(scheduler.getJsonuidata());
		if (!dateList.isEmpty()) {
			jsoneditSchedulerConfig = (JSONObject) convertInputDateToUTCByZone(jsoneditSchedulerConfig, dateList, false,
					userInfo);
			jsonuiSchedulerConfig = (JSONObject) convertInputDateToUTCByZone(jsonuiSchedulerConfig, dateList, false,
					userInfo);

		}
		jsonuiSchedulerConfig.put("nschedulersamplecode", Integer.parseInt(nschedulersamplecode));
		jsonuiSchedulerConfig.put("nsampletypecode", inputMap.get("nsampletypecode"));
		jsonuiSchedulerConfig.put("nregtypecode", inputMap.get("nregtypecode"));
		jsonuiSchedulerConfig.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
		jsonuiSchedulerConfig.put("nproductcatcode", scheduler.getNproductcatcode());
		jsonuiSchedulerConfig.put("nproductcode", scheduler.getNproductcode());
		jsonuiSchedulerConfig.put("ninstrumentcatcode", scheduler.getNinstrumentcatcode());
		jsonuiSchedulerConfig.put("ninstrumentcode", scheduler.getNinstrumentcode());
		jsonuiSchedulerConfig.put("nmaterialcatcode", scheduler.getNmaterialcatcode());
		jsonuiSchedulerConfig.put("nmaterialcode", scheduler.getNmaterialcode());
		jsonuiSchedulerConfig.put("ntemplatemanipulationcode", scheduler.getNtemplatemanipulationcode());
		jsonuiSchedulerConfig.put("nallottedspeccode", scheduler.getNallottedspeccode());
		jsonuiSchedulerConfig.put("ndesigntemplatemappingcode", scheduler.getNdesigntemplatemappingcode());

		final String queryString = "update schedulersampledetail  set nschedulecode=" + scheduler.getNschedulecode()
		+ ", jsondata=jsondata||'" + stringUtilityFunction.replaceQuote(jsoneditSchedulerConfig.toString())
		+ "'::jsonb,jsonuidata=jsonuidata||'"
		+ stringUtilityFunction.replaceQuote(jsonuiSchedulerConfig.toString())
		+ "'::jsonb  where nschedulersamplecode in (" + nschedulersamplecode + ")";

		jdbcTemplate.execute(queryString);

		// ALPD-4156--Vignesh R(17-05-2024)-->While edit the sample 500 occurs
		/*
		 * final String
		 * strQuery=" select ntransactionstatus from registrationhistory where nreghistorycode"
		 * +" in(select max(nreghistorycode) from registrationhistory "
		 * +" where npreregno="+nschedulersamplecode+" and nstatus="+Enumeration.
		 * TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.
		 * getNtranssitecode()+")"
		 * +" and nsitecode="+userInfo.getNtranssitecode()+" and nstatus="+Enumeration.
		 * TransactionStatus.ACTIVE.gettransactionstatus()+""; Integer
		 * nfilterstatus=jdbcTemplate.queryForObject(strQuery, Integer.class);
		 * initialParam.put("nfilterstatus", nfilterstatus);
		 */

		returnMap = getDynamicSchedulerConfig(initialParam, userInfo);

		jsonAuditOld.put("schedulersampledetail", (List<Map<String, Object>>) inputMap.get("selectedSample"));
		jsonAuditNew.put("schedulersampledetail",
				(List<Map<String, Object>>) returnMap.get("SchedulerConfigGetSample"));
		auditmap.put("nregtypecode", initialParam.get("nregtypecode"));
		auditmap.put("nregsubtypecode", initialParam.get("nregsubtypecode"));
		auditmap.put("ndesigntemplatemappingcode", scheduler.getNdesigntemplatemappingcode());
		actionType.put("schedulersampledetail", "IDS_EDITREGISTRATION");
		returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
				Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, jsonAuditNew, actionType, auditmap, false, userInfo);

		returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
				Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	@SuppressWarnings({ "unchecked", "unused" })
	public Map<String, Object> validateUniqueConstraintScheduler(final List<Map<String, Object>> masterUniqueValidation,
			final Map<String, Object> registration, final UserInfo userInfo, final String task, Class<?> tableName,
			final String columnName, boolean isMaster) throws Exception {

		Map<String, Object> jsonUIData = (Map<String, Object>) registration.get("jsonuidata");

		final String tablename = tableName.getSimpleName().toLowerCase();
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> returnObj = new HashMap<String, Object>();
		final StringBuffer query = new StringBuffer();
		String conditionalString = "";
		final List<String> listMultiLingual = new LinkedList<>();
		final List<Class<?>> listClass = new LinkedList<>();
		if (isMaster) {
			conditionalString = " nformcode =" + userInfo.getNformcode() + " and ";
		} else if (tablename.equals("registration")) {
			conditionalString = " nregtypecode =" + registration.get("nregtypecode") + " and nregsubtypecode="
					+ registration.get("nregsubtypecode") + " and ";
		} else if (tablename.equals("schedulersampledetail")) {
			conditionalString = " nregtypecode =" + registration.get("nregtypecode") + " and nregsubtypecode="
					+ registration.get("nregsubtypecode") + " and ";
		}
		String data = "";
		for (Map<String, Object> constraintMap : masterUniqueValidation) {
			final StringBuffer buffer = new StringBuffer();
			Set<String> lstKey = constraintMap.keySet();
			String multiLingualName = "";
			for (String key : lstKey) {
				final Map<String, Object> objMap = (Map<String, Object>) constraintMap.get(key);
				final Map<String, Object> mapMultilingual = (Map<String, Object>) objMap.get("1");
				if (multiLingualName.isEmpty()) {
					multiLingualName = (String) mapMultilingual.get(userInfo.getSlanguagetypecode());
				} else {
					multiLingualName = multiLingualName + "," + mapMultilingual.get(userInfo.getSlanguagetypecode());
				}
				if (jsonUIData.get(key) instanceof Integer) {
					data = String.valueOf(jsonUIData.get(key));
				} else {
					data = jsonUIData.get(key).toString();
				}

				buffer.append(" and  jsonuidata->> '" + key + "' = '" + data + "'");
			}
			listMultiLingual.add(multiLingualName);
			if (task.equalsIgnoreCase("update")) {
				buffer.append(" and " + columnName + " <>" + registration.get(columnName));
			}

			query.append(
					"select " + columnName + " from " + tablename + " dm where " + conditionalString + "  nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + buffer.toString() + ";");
			listClass.add(tableName);
		}

		if (query.toString().length() > 1) {
			// Class<?> entityClass = Class.forName(tableName);
			List<?> lstData = projectDAOSupport.getMultipleEntitiesResultSetInList(query.toString(), jdbcTemplate);
			int i = 0;
			for (Map<String, Object> constraintMap : masterUniqueValidation) {
				List<?> lstData1 = (List<?>) lstData.get(i);
				if (!lstData1.isEmpty()) {
					returnObj.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							listMultiLingual.get(i) + " " + commonFunction.getMultilingualMessage("IDS_ALREADYEXISTS",
									userInfo.getSlanguagefilename()));
					return returnObj;
				}
				i++;
			}

		}
		returnObj.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
				Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		return returnObj;
	}

	@Override
	public ResponseEntity<Object> getSiteByUser(final Map<String, Object> inputMap) {

		Map<String, Object> returnMap = new HashMap<>();
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		final String strQuery = "select s.nsitecode,s.ssitename from site s,userssite us where  us.nsitecode=s.nsitecode and s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + " us.nusercode="
				+ userInfo.getNusercode() + " and us.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

		List<Site> listSite = new ArrayList<>();

		listSite = jdbcTemplate.query(strQuery, new Site());
		returnMap.put("userSite", listSite);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> auditTestGet(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		List<Map<String, Object>> lstData = new ArrayList();

		String sschedulertestcode = "";
		String snschedulersubsamplecode = "";
		String nschedulersamplecode = "";

		if (inputMap.containsKey("nschedulersamplecode")) {
			if (inputMap.get("nschedulersamplecode") != null) {
				nschedulersamplecode = " and std.nschedulersamplecode in("
						+ (String) inputMap.get("nschedulersamplecode") + ")";
			}
		}

		if (inputMap.containsKey("nschedulertestcode")) {
			if (inputMap.get("nschedulertestcode") != null) {
				sschedulertestcode = " and nschedulertestcode in(" + (String) inputMap.get("nschedulertestcode") + ")";
			}
		}
		if (inputMap.containsKey("nschedulersubsamplecode")) {
			if (inputMap.get("nschedulersubsamplecode") != null) {
				snschedulersubsamplecode = " and std.nschedulersubsamplecode in("
						+ (String) inputMap.get("nschedulersubsamplecode") + ")";
			}
		}

		final String schedulerMultilingual = commonFunction.getMultilingualMessage("IDS_REGNO",
				userInfo.getSlanguagefilename());
		final String schedulerSubMultilingual = commonFunction.getMultilingualMessage("IDS_SAMPLENO",
				userInfo.getSlanguagefilename());

		String strTestQry = "select json_agg(a.jsondata) from (select nschedulertestcode,ssd.ntransactionstatus, cm.ncolorcode, cm.scolorhexcode,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "', ts.jsondata->'stransdisplaystatus'->>'en-US') stransdisplaystatus, "
				+ "std.jsondata || json_build_object('stransdisplaystatus', coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US'),'ntransactionstatus', ssd.ntransactionstatus, 'scolorhexcode', cm.scolorhexcode, 'ncolorcode', cm.ncolorcode,   "
				+ " 'sarno',concat(cast(std.nschedulersamplecode as text),' ','" + schedulerMultilingual + "'),"
				+ "'ssamplearno',concat(cast(std.nschedulersubsamplecode as text),' ','" + schedulerSubMultilingual
				+ "'))::jsonb as jsondata  from schedulertestdetail std,schedulersampledetail ssd,"
				+ " schedulersubsampledetail sssd, formwisestatuscolor fwsc, transactionstatus ts, colormaster cm"
				+ " where std.nschedulersubsamplecode=sssd.nschedulersubsamplecode "
				+ "	and std.nschedulersamplecode=sssd.nschedulersamplecode "
				+ "	and std.nschedulersamplecode=ssd.nschedulersamplecode "
				+ "	and ssd.nschedulersamplecode=sssd.nschedulersamplecode and ssd.ntransactionstatus=ts.ntranscode and  ssd.ntransactionstatus=fwsc.ntranscode and  fwsc.nformcode="
				+ userInfo.getNformcode() + " and fwsc.ncolorcode=cm.ncolorcode and " + " std.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ssd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + " sssd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and std.nsitecode="
				+ userInfo.getNmastersitecode() + " " + snschedulersubsamplecode + " " + sschedulertestcode + " "
				+ nschedulersamplecode + " order by 1 desc)a";

		logging.info("test Start========?" + LocalDateTime.now());

		final String lstData2 = jdbcTemplate.queryForObject(strTestQry, String.class);
		logging.info("schedulertestdetailget:" + strTestQry);
		logging.info("test end========?" + LocalDateTime.now());
		if (lstData2 != null) {

			ObjectMapper obj = new ObjectMapper();
			lstData = projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(lstData2,
					userInfo, true, (int) inputMap.get("ndesigntemplatemappingcode"), "test");

		}
		return lstData;
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> auditSubSampleGet(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		List<Map<String, Object>> lstData = new ArrayList();

		String sschedulersamplecode = "";

		if (inputMap.containsKey("nschedulersubsamplecode")) {
			if (inputMap.get("nschedulersubsamplecode") != null && inputMap.get("nschedulersubsamplecode") != "") {
				sschedulersamplecode = " and nschedulersubsamplecode in("
						+ (String) inputMap.get("nschedulersubsamplecode") + ")";
			}
		}
		final String schedulerMultilingual = commonFunction.getMultilingualMessage("IDS_REGNO",
				userInfo.getSlanguagefilename());
		final String schedulerSubMultilingual = commonFunction.getMultilingualMessage("IDS_SAMPLENO",
				userInfo.getSlanguagefilename());

		String strSubSampleQry = "select json_agg(a.jsonuidata) from (select sssd.nschedulersubsamplecode, cm.ncolorcode, cm.scolorhexcode,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "', ts.jsondata->'stransdisplaystatus'->>'en-US') stransdisplaystatus, sssd.jsonuidata"
				+ " || json_build_object('stransdisplaystatus', coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + ""
				+ "', ts.jsondata->'stransdisplaystatus'->>'en-US'), 'ntransactionstatus', ssd.ntransactionstatus, 'scolorhexcode', cm.scolorhexcode, 'ncolorcode',"
				+ " cm.ncolorcode,'nspecsampletypecode',sssd.nspecsampletypecode,'dtransactiondate', to_char(sssd.dtransactiondate, 'YYYY-MM-DD HH24:MI:SS'),'ssamplearno',concat(cast(nschedulersubsamplecode as text),' ','"
				+ schedulerSubMultilingual + "','/',cast(sssd.nschedulersamplecode as text),' ','"
				+ schedulerMultilingual + "'))::jsonb"
				+ " as jsonuidata from schedulersubsampledetail sssd,schedulersampledetail ssd,formwisestatuscolor fwsc, transactionstatus ts, colormaster cm  where ssd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ssd.nsitecode="
				+ userInfo.getNmastersitecode() + " and fwsc.ncolorcode=cm.ncolorcode and fwsc.nformcode="
				+ userInfo.getNformcode()
				+ " and ssd.ntransactionstatus=ts.ntranscode and ssd.ntransactionstatus=fwsc.ntranscode and sssd.nschedulersamplecode=ssd.nschedulersamplecode  "
				+ " and sssd.nschedulersamplecode in (" + inputMap.get("nschedulersamplecode") + ") "
				+ sschedulersamplecode + " and sssd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ssd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  order by 1 desc ) a";

		logging.info("sub sample Start========?" + LocalDateTime.now());
		logging.info("sub sample query:" + strSubSampleQry);

		final String lstData1 = jdbcTemplate.queryForObject(strSubSampleQry, String.class);
		logging.info("sub sample end========?" + LocalDateTime.now());

		if (lstData1 != null) {

			lstData = projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(lstData1,
					userInfo, true, (int) inputMap.get("ndesigntemplatemappingcode"), "subsample");

		}
		return lstData;
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> auditSampleGet(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		List<Map<String, Object>> lstData = new ArrayList();

		Map<String, Object> objMap = new HashMap();
		String nfilterstatus = "";
		if (inputMap.containsKey("nfilterstatus")) {
			if (inputMap.get("nfilterstatus") != null) {
				nfilterstatus = inputMap.get("nfilterstatus").toString();
			}
		}

		String sschedulersamplecode1 = "";
		if (inputMap.containsKey("nschedulersamplecode")) {
			sschedulersamplecode1 = " and nschedulersamplecode=" + String.valueOf(inputMap.get("nschedulersamplecode"))
			+ "";
		}
		final String registrationMultilingual = commonFunction.getMultilingualMessage("IDS_REGNO",
				userInfo.getSlanguagefilename());

		String strSampleQry = "select json_agg(a.jsonuidata) from (select ssd.nschedulersamplecode, ssd.nallottedspeccode, "
				+ " ssd.dtransactiondate, ssd.ntransactionstatus, cm.ncolorcode, cm.scolorhexcode,"
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "', ts.jsondata->'stransdisplaystatus'->>'en-US') stransdisplaystatus,"
				+ " ssd.jsonuidata || json_build_object('sactiveststaus', coalesce(ts1.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + ""
				+ "', ts1.jsondata->'stransdisplaystatus'->>'en-US'),    'dtransactiondate',ssd.jsonuidata->>'Transaction Date','stransdisplaystatus', coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "', ts.jsondata->'stransdisplaystatus'->>'en-US'),'nactivestatus', ssd.nactivestatus, 'ntransactionstatus', ssd.ntransactionstatus, 'scolorhexcode', cm.scolorhexcode,'ssecondcolorhexcode', cm1.scolorhexcode,'nsecondcolorcode',cm1.ncolorcode, 'ncolorcode',"
				+ " cm.ncolorcode,'sarno',concat(cast(ssd.nschedulersamplecode as text),' ','"
				+ registrationMultilingual
				+ "'),'nschedulecode',ssd.nschedulecode)::jsonb as jsonuidata from schedulersampledetail ssd,sampleschedulerconfigtype sct, formwisestatuscolor fwsc, transactionstatus ts, colormaster cm,formwisestatuscolor fwsc1, transactionstatus ts1, colormaster cm1"
				+ " where ssd.ntransactionstatus=ts.ntranscode and ssd.nactivestatus=fwsc1.ntranscode and ssd.ntransactionstatus=fwsc.ntranscode and fwsc1.nformcode="
				+ userInfo.getNformcode() + "  and fwsc.nformcode=" + userInfo.getNformcode()
				+ " and  ssd.nactivestatus = ts1.ntranscode and fwsc1.ncolorcode=cm1.ncolorcode and fwsc.ncolorcode=cm.ncolorcode and ssd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and fwsc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ssd.nsitecode="
				+ userInfo.getNmastersitecode() + " and ssd.nregtypecode=" + inputMap.get("nregtypecode")
				+ " and ssd.nregsubtypecode=" + inputMap.get("nregsubtypecode") + " and ssd.ndesigntemplatemappingcode="
				+ inputMap.get("ndesigntemplatemappingcode") + " and nregsubtypeversioncode="
				+ inputMap.get("nregsubtypeversioncode") + " and ntransactionstatus in(" + nfilterstatus
				+ ") and sct.nsampleschedulerconfigtypecode=ssd.nsampleschedulerconfigtypecode "
				+ " and  ssd.nsampleschedulerconfigtypecode=" + inputMap.get("nsampleschedulerconfigtypecode") + "   "
				+ sschedulersamplecode1 + " order by ssd.nschedulersamplecode) a";

		logging.info("Scheduler Config Sample Start========?" + LocalDateTime.now());
		String lstData1 = jdbcTemplate.queryForObject(strSampleQry, String.class);

		logging.info("Scheduler Config Sample end========?" + strSampleQry + " :" + LocalDateTime.now());

		if (lstData1 != null) {
			lstData = projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(lstData1,
					userInfo, true, (int) inputMap.get("ndesigntemplatemappingcode"), "sample");
		}
		return lstData;
	}

	@Override
	@SuppressWarnings("null")
	public ResponseEntity<Object> approveSchedulerConfig(final Map<String, Object> inputMap) throws Exception {

		Map<String, Object> returnMap = new HashMap<>();
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		// String nfilerstatus=inputMap.get("nfilterstatus").toString();
		if ((inputMap.containsKey("nschedulerconfigtypecode")
				&& (int) inputMap.get("nschedulerconfigtypecode") == Enumeration.SchedulerConfigType.EXTERNAL
				.getSchedulerConfigType())) {

			if ((inputMap.containsKey("nsampletypecode")
					&& (int) inputMap.get("nsampletypecode") == Enumeration.SampleType.INSTRUMENT.getType())) {

				// String sinstrumentidLabel = inputMap.get("sinstrumentidLabel").toString();
				// String sinstrumentid = inputMap.get("sinstrumentid").toString();
				//
				// String updateQuery = "";

				// String str = "select * from schedulersampledetail where jsonuidata->>'" +
				// sinstrumentidLabel + "'='"
				// + sinstrumentid + "' and ntransactionstatus="
				// + Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " "
				// + " and nsampleschedulerconfigtypecode=" +
				// inputMap.get("nsampleschedulerconfigtypecode")
				// + " order by 1 desc";

				String str = "select * from schedulersampledetail where ninstrumentcatcode="
						+ inputMap.get("ninstrumentcatcode") + "" + " and ntransactionstatus="
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + "  "
						+ " and nsampleschedulerconfigtypecode=" + inputMap.get("nsampleschedulerconfigtypecode")
						+ " order by 1 desc";
				List<SchedulerSampleDetail> schedulerSampleDetailList = jdbcTemplate.query(str,
						new SchedulerSampleDetail());
				returnMap.putAll(getDynamicSchedulerConfig(inputMap, userInfo));
				String updateQuery = "";
				if (!schedulerSampleDetailList.isEmpty()) {

					Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
					JSONObject actionType = new JSONObject();
					JSONObject jsonAuditObject = new JSONObject();
					JSONObject jsonAuditNewObject = new JSONObject();

					auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
					auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
					auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
					actionType.put("schedulersampledetail", "IDS_RETIRESAMPLE");
					String schedulersamplecode = "";
					schedulersamplecode = schedulerSampleDetailList.stream()
							.map(item -> String.valueOf(item.getNschedulersamplecode()))
							.collect(Collectors.joining(","));
					String nschedulersamplecode = inputMap.get("nschedulersamplecode").toString();
					inputMap.put("nschedulersamplecode", schedulersamplecode);

					List<Map<String, Object>> lstbefore = auditSampleGet(inputMap, userInfo);

					updateQuery = " update schedulersampledetail set ntransactionstatus="
							+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + ", nactivestatus= "
							+ Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus() + ", dtransactiondate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ").replace("Z",
									"")
							+ "' " + ", noffsetdtransactiondate="
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
							+ ", ntransdatetimezonecode=" + userInfo.getNtimezonecode() + " "
							+ " where nschedulersamplecode in(" + schedulersamplecode + ")";

					jdbcTemplate.execute(updateQuery);
					List<Map<String, Object>> lstAfter = auditSampleGet(inputMap, userInfo);

					jsonAuditObject.put("schedulersampledetail", lstbefore);
					jsonAuditNewObject.put("schedulersampledetail", lstAfter);
					auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, jsonAuditNewObject, actionType, auditmap,
							false, userInfo);
					inputMap.put("nschedulersamplecode", nschedulersamplecode);

				}

				String nschedulersamplecode = inputMap.get("nschedulersamplecode").toString();
				if (inputMap.containsKey("nschedulersamplecodeberfore")) {
					if (!inputMap.get("nschedulersamplecodeberfore").toString().isEmpty()) {
						inputMap.put("nschedulersamplecode", inputMap.get("nschedulersamplecodeberfore"));
						returnMap.putAll(getDynamicSchedulerConfig(inputMap, userInfo));

						returnMap.put("SchedulerConfigGetSampleBefore", returnMap.get("SchedulerConfigGetSample"));
						inputMap.put("nschedulersamplecode", nschedulersamplecode);

					}
				}
				List<Map<String, Object>> lstbefore1 = auditSampleGet(inputMap, userInfo);

				updateQuery = " update schedulersampledetail set ntransactionstatus="
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ", dtransactiondate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ").replace("Z", "")
						+ "' " + ", noffsetdtransactiondate="
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ ", ntransdatetimezonecode=" + userInfo.getNtimezonecode() + " "
						+ " where nschedulersamplecode in(" + inputMap.get("nschedulersamplecode") + ")";
				jdbcTemplate.execute(updateQuery);

				/*
				 * if ((int) inputMap.get("nsampleschedulerconfigtypecode") == 1) { String str1
				 * = " select * from instrument where ninstrumentcode=" +
				 * inputMap.get("ninstrumentcode") + " and nstatus=" +
				 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
				 *
				 * final Instrument objSqlQuery = (Instrument)
				 * jdbcUtilityFunction.queryForObject(str1, Instrument.class);
				 *
				 * if (objSqlQuery != null) { /*LocalDateTime currentDateTime =
				 * LocalDateTime.now(); Instant currentInstant = Instant.now(); String
				 * soccurencedate = commonFunction.instantDateToStringwithformat(currentInstant,
				 * "yyyy-MM-dd HH:mm:ss");
				 *
				 * /* DateTimeFormatter formatter =
				 * DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"); String
				 * soccurencedate="";
				 * if(objSqlQuery.getNnextcalibrationperiod()==Enumeration.Period.Days.getPeriod
				 * ()) {
				 * soccurencedate=currentDateTime.plusDays(objSqlQuery.getNnextcalibration()).
				 * format(formatter); } else
				 * if(objSqlQuery.getNnextcalibrationperiod()==Enumeration.Period.Weeks.
				 * getPeriod()) {
				 * soccurencedate=currentDateTime.plusWeeks(objSqlQuery.getNnextcalibration()).
				 * format(formatter);
				 *
				 * } else if(objSqlQuery.getNnextcalibrationperiod()==Enumeration.Period.Month.
				 * getPeriod()) {
				 * soccurencedate=currentDateTime.plusMonths(objSqlQuery.getNnextcalibration()).
				 * format(formatter);
				 *
				 * }
				 */
				/*
				 * int nsequenceNoTestParameter = jdbcTemplate.queryForObject(
				 * "select nsequenceno from seqnoscheduler where stablename ='schedulertransaction'"
				 * , Integer.class); ++nsequenceNoTestParameter;
				 * jdbcTemplate.execute("update seqnoscheduler set nsequenceno=" +
				 * nsequenceNoTestParameter + " where stablename='schedulertransaction';");
				 *
				 * str1 =
				 * "INSERT INTO public.schedulertransaction(nschedulertransactioncode, nschedulecode, nschedulersamplecode, npreregno, dscheduleoccurrencedate, dtransactiondate, noffsetdtransactiondate, ntransdatetimezonecode, ntransactionstatus, nsitecode, nstatus)"
				 * + " VALUES (" + nsequenceNoTestParameter + ", -1, " +
				 * inputMap.get("nschedulersamplecode") + ", -1, '" + soccurencedate + "', '" +
				 * dateUtilityFunction.getCurrentDateTime(userInfo) + "', -1, -1, " +
				 * Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", -1, " +
				 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
				 *
				 * jdbcTemplate.execute(str1);
				 *
				 * } }
				 */ if ((int) inputMap.get("nsampleschedulerconfigtypecode") == 2) {
					 String currentDate = dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ")
							 .replace("Z", "");
					 jdbcTemplate.execute("call sp_externalscheduler(" + inputMap.get("nschedulecode") + ","
							 + inputMap.get("nschedulersamplecode") + ",'" + currentDate + "')");

				 }

				 inputMap.remove("nschedulersubsamplecode");
				 returnMap.putAll(getDynamicSchedulerConfig(inputMap, userInfo));
				 // for audit trail
				 Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
				 JSONObject actionType = new JSONObject();
				 JSONObject jsonAuditOld = new JSONObject();
				 JSONObject jsonAuditNew = new JSONObject();

				 auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
				 auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
				 auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
				 actionType.put("schedulersampledetail", "IDS_APPROVESAMPLE");
				 jsonAuditOld.put("schedulersampledetail", lstbefore1);
				 jsonAuditNew.put("schedulersampledetail", (List<Map<String, Object>>) returnMap.get("selectedSample"));
				 auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, jsonAuditNew, actionType, auditmap, false,
						 userInfo);
			}
		}
		if ((inputMap.containsKey("nschedulerconfigtypecode")
				&& (int) inputMap.get("nschedulerconfigtypecode") == Enumeration.SchedulerConfigType.INTERNAL
				.getSchedulerConfigType())) {

			if ((inputMap.containsKey("nsampletypecode")
					&& (int) inputMap.get("nsampletypecode") == Enumeration.SampleType.INSTRUMENT.getType())) {

				String str = "select * from schedulersampledetail where ninstrumentcatcode="
						+ inputMap.get("ninstrumentcatcode") + "" + " and ntransactionstatus="
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + "  "
						+ " and nsampleschedulerconfigtypecode=" + inputMap.get("nsampleschedulerconfigtypecode")
						+ " order by 1 desc";
				List<SchedulerSampleDetail> schedulerSampleDetailList = jdbcTemplate.query(str,
						new SchedulerSampleDetail());
				returnMap.putAll(getDynamicSchedulerConfig(inputMap, userInfo));

				String updateQuery = "";
				if (!schedulerSampleDetailList.isEmpty()) {

					Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
					JSONObject actionType = new JSONObject();
					JSONObject jsonAuditObject = new JSONObject();
					JSONObject jsonAuditNewObject = new JSONObject();

					auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
					auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
					auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
					actionType.put("schedulersampledetail", "IDS_RETIRESAMPLE");
					String schedulersamplecode = "";
					schedulersamplecode = schedulerSampleDetailList.stream()
							.map(item -> String.valueOf(item.getNschedulersamplecode()))
							.collect(Collectors.joining(","));
					String nschedulersamplecode = inputMap.get("nschedulersamplecode").toString();
					inputMap.put("nschedulersamplecode", schedulersamplecode);

					List<Map<String, Object>> lstbefore = auditSampleGet(inputMap, userInfo);

					updateQuery = " update schedulersampledetail set ntransactionstatus="
							+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + ", nactivestatus= "
							+ Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus() + ", dtransactiondate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ").replace("Z",
									"")
							+ "' " + ", noffsetdtransactiondate="
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
							+ ", ntransdatetimezonecode=" + userInfo.getNtimezonecode() + " "
							+ " where nschedulersamplecode in(" + schedulersamplecode + ")";

					jdbcTemplate.execute(updateQuery);
					List<Map<String, Object>> lstAfter = auditSampleGet(inputMap, userInfo);

					jsonAuditObject.put("schedulersampledetail", lstbefore);
					jsonAuditNewObject.put("schedulersampledetail", lstAfter);
					auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, jsonAuditNewObject, actionType, auditmap,
							false, userInfo);
					inputMap.put("nschedulersamplecode", nschedulersamplecode);

				}

				String nschedulersamplecode = inputMap.get("nschedulersamplecode").toString();

				if (inputMap.containsKey("nschedulersamplecodeberfore")) {

					if (!inputMap.get("nschedulersamplecodeberfore").toString().isEmpty()) {
						inputMap.put("nschedulersamplecode", inputMap.get("nschedulersamplecodeberfore"));
						returnMap.putAll(getDynamicSchedulerConfig(inputMap, userInfo));

						returnMap.put("SchedulerConfigGetSampleBefore", returnMap.get("SchedulerConfigGetSample"));
						inputMap.put("nschedulersamplecode", nschedulersamplecode);

					}
				}
				List<Map<String, Object>> lstbefore1 = auditSampleGet(inputMap, userInfo);

				updateQuery = " update schedulersampledetail set ntransactionstatus="
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ", dtransactiondate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ").replace("Z", "")
						+ "' " + ", noffsetdtransactiondate="
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ ", ntransdatetimezonecode=" + userInfo.getNtimezonecode() + " "
						+ " where nschedulersamplecode in(" + inputMap.get("nschedulersamplecode") + ")";
				jdbcTemplate.execute(updateQuery);

				/*
				 * if ((int) inputMap.get("nsampleschedulerconfigtypecode") == 1) { String str1
				 * = " select * from instrument where ninstrumentcode=" +
				 * inputMap.get("ninstrumentcode") + " and nstatus=" +
				 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
				 *
				 * final Instrument objSqlQuery = (Instrument)
				 * jdbcUtilityFunction.queryForObject(str1, Instrument.class);
				 *
				 * if (objSqlQuery != null) { LocalDateTime currentDateTime =
				 * LocalDateTime.now(); Instant currentInstant = Instant.now(); String
				 * soccurencedate = commonFunction.instantDateToStringwithformat(currentInstant,
				 * "yyyy-MM-dd HH:mm:ss");
				 *
				 * /* DateTimeFormatter formatter =
				 * DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"); String
				 * soccurencedate="";
				 * if(objSqlQuery.getNnextcalibrationperiod()==Enumeration.Period.Days.getPeriod
				 * ()) {
				 * soccurencedate=currentDateTime.plusDays(objSqlQuery.getNnextcalibration()).
				 * format(formatter); } else
				 * if(objSqlQuery.getNnextcalibrationperiod()==Enumeration.Period.Weeks.
				 * getPeriod()) {
				 * soccurencedate=currentDateTime.plusWeeks(objSqlQuery.getNnextcalibration()).
				 * format(formatter);
				 *
				 * } else if(objSqlQuery.getNnextcalibrationperiod()==Enumeration.Period.Month.
				 * getPeriod()) {
				 * soccurencedate=currentDateTime.plusMonths(objSqlQuery.getNnextcalibration()).
				 * format(formatter);
				 *
				 * }
				 *
				 * int nsequenceNoTestParameter = jdbcTemplate.queryForObject(
				 * "select nsequenceno from seqnoscheduler where stablename ='schedulertransaction'"
				 * , Integer.class); ++nsequenceNoTestParameter;
				 * jdbcTemplate.execute("update seqnoscheduler set nsequenceno=" +
				 * nsequenceNoTestParameter + " where stablename='schedulertransaction';");
				 *
				 * str1 =
				 * "INSERT INTO public.schedulertransaction(nschedulertransactioncode, nschedulecode, nschedulersamplecode, npreregno, dscheduleoccurrencedate, dtransactiondate, noffsetdtransactiondate, ntransdatetimezonecode, ntransactionstatus, nsitecode, nstatus)"
				 * + " VALUES (" + nsequenceNoTestParameter + ", -1, " +
				 * inputMap.get("nschedulersamplecode") + ", -1, '" + soccurencedate + "', '" +
				 * dateUtilityFunction.getCurrentDateTime(userInfo) + "', -1, -1, " +
				 * Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", -1, " +
				 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
				 *
				 * jdbcTemplate.execute(str1);
				 *
				 * } }
				 */ if ((int) inputMap.get("nsampleschedulerconfigtypecode") == 2) {
					 String currentDate = dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ")
							 .replace("Z", "");
					 jdbcTemplate.execute("call sp_instrumentscheduler_schedule(" + inputMap.get("nschedulecode") + ","
							 + inputMap.get("nschedulersamplecode") + ",'" + currentDate + "')");

				 }

				 inputMap.remove("nschedulersubsamplecode");
				 returnMap.putAll(getDynamicSchedulerConfig(inputMap, userInfo));
				 // for audit trail
				 Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
				 JSONObject actionType = new JSONObject();
				 JSONObject jsonAuditOld = new JSONObject();
				 JSONObject jsonAuditNew = new JSONObject();

				 auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
				 auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
				 auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
				 actionType.put("schedulersampledetail", "IDS_APPROVESAMPLE");
				 jsonAuditOld.put("schedulersampledetail", lstbefore1);
				 jsonAuditNew.put("schedulersampledetail", (List<Map<String, Object>>) returnMap.get("selectedSample"));
				 auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, jsonAuditNew, actionType, auditmap, false,
						 userInfo);
			}
			if ((inputMap.containsKey("nsampletypecode")
					&& (int) inputMap.get("nsampletypecode") == Enumeration.SampleType.MATERIAL.getType())) {

				String updateQuery = "";
				int nmaterialcode = (int) inputMap.get("nmaterialcode");
				String str = "select * from schedulersampledetail where nmaterialcode = " + nmaterialcode
						+ " and ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
						+ "  " + " and nsampleschedulerconfigtypecode=" + inputMap.get("nsampleschedulerconfigtypecode")
						+ " order by 1 desc";
				List<SchedulerSampleDetail> schedulerSampleDetailList = jdbcTemplate.query(str,
						new SchedulerSampleDetail());
				returnMap.putAll(getDynamicSchedulerConfig(inputMap, userInfo));

				if (!schedulerSampleDetailList.isEmpty()) {

					Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
					JSONObject actionType = new JSONObject();
					JSONObject jsonAuditObject = new JSONObject();
					JSONObject jsonAuditNewObject = new JSONObject();

					auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
					auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
					auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
					actionType.put("schedulersampledetail", "IDS_RETIRESAMPLE");
					String schedulersamplecode = "";
					schedulersamplecode = schedulerSampleDetailList.stream()
							.map(item -> String.valueOf(item.getNschedulersamplecode()))
							.collect(Collectors.joining(","));
					String nschedulersamplecode = inputMap.get("nschedulersamplecode").toString();
					inputMap.put("nschedulersamplecode", schedulersamplecode);

					List<Map<String, Object>> lstbefore = auditSampleGet(inputMap, userInfo);

					updateQuery = " update schedulersampledetail set ntransactionstatus="
							+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + ", nactivestatus= "
							+ Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus() + ", dtransactiondate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ").replace("Z",
									"")
							+ "' " + ", noffsetdtransactiondate="
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
							+ ", ntransdatetimezonecode=" + userInfo.getNtimezonecode() + " "
							+ " where nschedulersamplecode in(" + schedulersamplecode + ")";

					jdbcTemplate.execute(updateQuery);
					List<Map<String, Object>> lstAfter = auditSampleGet(inputMap, userInfo);

					jsonAuditObject.put("schedulersampledetail", lstbefore);
					jsonAuditNewObject.put("schedulersampledetail", lstAfter);
					auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, jsonAuditNewObject, actionType, auditmap,
							false, userInfo);
					inputMap.put("nschedulersamplecode", nschedulersamplecode);

				}

				String nschedulersamplecode = inputMap.get("nschedulersamplecode").toString();
				if (inputMap.containsKey("nschedulersamplecodeberfore")) {
					if (!inputMap.get("nschedulersamplecodeberfore").toString().isEmpty()) {
						inputMap.put("nschedulersamplecode", inputMap.get("nschedulersamplecodeberfore"));
						returnMap.putAll(getDynamicSchedulerConfig(inputMap, userInfo));

						returnMap.put("SchedulerConfigGetSampleBefore", returnMap.get("SchedulerConfigGetSample"));
						inputMap.put("nschedulersamplecode", nschedulersamplecode);

					}
				}

				List<Map<String, Object>> lstbefore1 = auditSampleGet(inputMap, userInfo);

				updateQuery = " update schedulersampledetail set ntransactionstatus="
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ", dtransactiondate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ").replace("Z", "")
						+ "' " + ", noffsetdtransactiondate="
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ ", ntransdatetimezonecode=" + userInfo.getNtimezonecode() + " "
						+ " where nschedulersamplecode in(" + inputMap.get("nschedulersamplecode") + ")";
				jdbcTemplate.execute(updateQuery);

				inputMap.remove("nschedulersubsamplecode");
				returnMap.putAll(getDynamicSchedulerConfig(inputMap, userInfo));
				// for audit trail
				Map<String, Object> auditMap = new LinkedHashMap<String, Object>();
				JSONObject actionType = new JSONObject();
				JSONObject jsonAuditOld = new JSONObject();
				JSONObject jsonAuditNew = new JSONObject();

				auditMap.put("nregtypecode", inputMap.get("nregtypecode"));
				auditMap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
				auditMap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
				actionType.put("schedulersampledetail", "IDS_APPROVESAMPLE");
				jsonAuditOld.put("schedulersampledetail", lstbefore1);
				jsonAuditNew.put("schedulersampledetail", (List<Map<String, Object>>) returnMap.get("selectedSample"));
				auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, jsonAuditNew, actionType, auditMap, false,
						userInfo);

			}

		}

		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	@SuppressWarnings("null")
	public ResponseEntity<Object> getSchedulerMaster(final Map<String, Object> inputMap) throws Exception {

		Map<String, Object> returnMap = new HashMap<>();

		String str = "select * from schedulemaster where ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and  nschedulecode>0  order by 1 desc";

		List<ScheduleMaster> approveschedulemasterList = jdbcTemplate.query(str, new ScheduleMaster());

		returnMap.put("ScheduleMaster", approveschedulemasterList);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	@SuppressWarnings("null")
	public ResponseEntity<Object> getSchedulerMasteDetails(final Map<String, Object> inputMap) throws Exception {

		Map<String, Object> returnMap = new HashMap<>();

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		final String strQuery = "select a.nschedulecode,a.sschedulename,a.sscheduletype,Case when a.sremarks = 'null' or a.sremarks = '' then '' else a.sremarks end as sremarks,a.dstartdate,a.dstarttime,a.noccurencenooftimes,"
				+ "a.soccurencehourwiseinterval,a.noccurencedaywiseinterval,a.denddate,a.dendtime,b.nexactday,b.nmonthlyoccurrencetype,b.njan,"
				+ "b.nfeb,b.nmar,b.napr,b.nmay,b.njun,b.njul,b.naug,b.nsep,b.noct,b.nnov,b.ndec,c.nsunday,c.nmonday,c.ntuesday,c.nwednesday,"
				+ "c.nthursday,c.nfriday,c.nsaturday,a.nstatus,a.noffsetdstartdate,a.noffsetdstarttime,a.noffsetdenddate,a.noffsetdendtime,"
				+ "COALESCE(TO_CHAR(a.dstartdate,'" + userInfo.getSpgsitedatetime()
				+ "'),'') as sstartdate,COALESCE(TO_CHAR(a.dstarttime,'" + userInfo.getSpgsitedatetime()
				+ "'),'') as sstarttime," + "COALESCE(TO_CHAR(a.denddate,'" + userInfo.getSpgsitedatetime()
				+ "'),'') as senddate,COALESCE(TO_CHAR(a.dendtime,'" + userInfo.getSpgsitedatetime()
				+ "'),'') as sendtime," + " coalesce(t.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " t.jsondata->'stransdisplaystatus'->>'en-US') as stransstatus,a.ntransactionstatus ,"
				+ " case when a.sscheduletype ='O' then 'One Time' when a.sscheduletype ='D' then 'Daily'"
				+ " when a.sscheduletype ='W' then 'Weekly' else 'Monthly' end as stempscheduleType "
				+ " from schedulemaster a ,schedulemastermonthly b ,schedulemasterweekly c,transactionstatus t where "
				+ "a.nschedulecode = b.nschedulecode and b.nschedulecode = c.nschedulecode and c.nschedulecode = a.nschedulecode and "
				+ " a.ntransactionstatus = t.ntranscode and a.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and a.nschedulecode = "
				+ (int) inputMap.get("nschedulecode");

		List<ScheduleMaster> objSchedulemaster = jdbcTemplate.query(strQuery, new ScheduleMaster());

		returnMap.put("ScheduleMasterDetails", objSchedulemaster);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> deleteSchedulerConfig(final Map<String, Object> inputMap) throws Exception {

		Map<String, Object> returnMap = new HashMap<>();
		final ObjectMapper objMapper = new ObjectMapper();
		String sschedulersamplecode = inputMap.get("nschedulersamplecode").toString();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		String str = "select * from schedulersampledetail where nschedulersamplecode in(" + sschedulersamplecode
				+ ")  and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  order by 1 desc";
		List<SchedulerSampleDetail> approveschedulemasterList = jdbcTemplate.query(str, new SchedulerSampleDetail());
		if (!approveschedulemasterList.isEmpty()) {
			returnMap.putAll(getDynamicSchedulerConfig(inputMap, userInfo));

			// for audit
			Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
			JSONObject actionType = new JSONObject();
			JSONObject jsonAuditObject = new JSONObject();

			jsonAuditObject.put("schedulersampledetail", (List<Map<String, Object>>) returnMap.get("selectedSample"));
			actionType.put("schedulersampledetail", "IDS_DELETESAMPLE");

			if ((boolean) inputMap.get("nneedsubsample")) {
				jsonAuditObject.put("schedulersubsampledetail",
						(List<Map<String, Object>>) returnMap.get("SchedulerConfigGetSubSample"));
				actionType.put("schedulersubsampledetail", "IDS_DELETESUBSAMPLE");

				inputMap.put("nschedulersubsamplecode", null);

				List<Map<String, Object>> lstDataTest = auditTestGet(inputMap, userInfo);

				jsonAuditObject.put("schedulertestdetail", lstDataTest);
			} else {
				jsonAuditObject.put("schedulertestdetail",
						(List<Map<String, Object>>) returnMap.get("SchedulerConfigGetTest"));
			}

			actionType.put("schedulertestdetail", "IDS_DELETETEST");

			sschedulersamplecode = approveschedulemasterList.stream()
					.map(item -> String.valueOf(item.getNschedulersamplecode())).collect(Collectors.joining(","));
			str = "update schedulersampledetail set nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nschedulersamplecode in("
					+ sschedulersamplecode + ");";
			str = str + "update schedulersubsampledetail set nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nschedulersamplecode in("
					+ sschedulersamplecode + ");";
			str = str + "update schedulertestdetail set nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nschedulersamplecode in("
					+ sschedulersamplecode + ");";
			str = str + "update schedulertestparameterdetail set nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nschedulersamplecode in("
					+ sschedulersamplecode + ");";
			jdbcTemplate.execute(str);

			inputMap.remove("nschedulersamplecode");
			inputMap.remove("nschedulersubsamplecode");
			inputMap.remove("nschedulertestcode");

			auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
			auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
			auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
			// actionType.put("schedulersampledetail", "IDS_DELETESAMPLE");
			auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);
			returnMap.putAll(getDynamicSchedulerConfig(inputMap, userInfo));

			return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_ALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}

	}

	@Override
	public ResponseEntity<Object> updateActiveStatusSchedulerConfig(final Map<String, Object> inputMap)
			throws Exception {

		Map<String, Object> returnMap = new HashMap<>();
		final ObjectMapper objMapper = new ObjectMapper();
		String sschedulersamplecode = inputMap.get("nschedulersamplecode").toString();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final int nactivestatus = (int) inputMap.get("nactivestatus");
		List<Map<String, Object>> lstbefore = auditSampleGet(inputMap, userInfo);

		String str = "select * from schedulersampledetail where nschedulersamplecode in(" + sschedulersamplecode
				+ ")  and nstatus=1  order by 1 desc";
		List<SchedulerSampleDetail> approveschedulemasterList = jdbcTemplate.query(str, new SchedulerSampleDetail());
		if (!approveschedulemasterList.isEmpty()) {
			if (nactivestatus == Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()) {
				str = "update schedulersampledetail set nactivestatus="
						+ Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus() + " where nschedulersamplecode="
						+ sschedulersamplecode + ";";

			} else if (nactivestatus == Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus()) {
				str = "update schedulersampledetail set nactivestatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where nschedulersamplecode="
						+ sschedulersamplecode + ";";

			}
			jdbcTemplate.execute(str);

			returnMap.putAll(getDynamicSchedulerConfig(inputMap, userInfo));

			// for audit
			Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
			JSONObject actionType = new JSONObject();
			JSONObject jsonAuditOld = new JSONObject();
			JSONObject jsonAuditNew = new JSONObject();

			auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
			auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
			auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
			actionType.put("schedulersampledetail", "IDS_ACTIVESTATUS");
			jsonAuditOld.put("schedulersampledetail", lstbefore);
			jsonAuditNew.put("schedulersampledetail", (List<Map<String, Object>>) returnMap.get("selectedSample"));
			auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, jsonAuditNew, actionType, auditmap, false, userInfo);

			return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_ALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}

	}

	// ALPD-5530--Vignesh R(06-03-2025)--record allowing the pre-register when the
	// approval config retired
	// start
	@Override
	public short getActiveApprovalConfigId(int napproveconfversioncode) {

		String str = "select ntransactionstatus from approvalconfigversion where napproveconfversioncode="
				+ napproveconfversioncode + " " + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

		short ntransactionstatus = jdbcTemplate.queryForObject(str, Short.class);

		return ntransactionstatus;

	}
	// end
}
