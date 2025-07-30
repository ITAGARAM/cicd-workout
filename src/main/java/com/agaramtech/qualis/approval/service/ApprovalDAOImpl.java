package com.agaramtech.qualis.approval.service;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.attachmentscomments.service.attachments.AttachmentDAO;
import com.agaramtech.qualis.attachmentscomments.service.comments.CommentDAO;
import com.agaramtech.qualis.basemaster.model.TransactionStatus;
import com.agaramtech.qualis.configuration.model.ApprovalConfigAutoapproval;
import com.agaramtech.qualis.configuration.model.ApprovalConfigRole;
import com.agaramtech.qualis.configuration.model.ApprovalConfigVersion;
import com.agaramtech.qualis.configuration.model.ApprovalRoleActionDetail;
import com.agaramtech.qualis.configuration.model.ApprovalRoleDecisionDetail;
import com.agaramtech.qualis.configuration.model.ApprovalRoleFilterDetail;
import com.agaramtech.qualis.configuration.model.ApprovalRoleValidationDetail;
import com.agaramtech.qualis.configuration.model.FilterName;
import com.agaramtech.qualis.configuration.model.SampleType;
import com.agaramtech.qualis.configuration.model.Settings;
import com.agaramtech.qualis.contactmaster.model.Patient;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationSubType;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationType;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.EmailDAOSupport;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.LinkMaster;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.organization.model.Section;
import com.agaramtech.qualis.registration.model.ApprovalParameter;
import com.agaramtech.qualis.registration.model.COAHistory;
import com.agaramtech.qualis.registration.model.Registration;
import com.agaramtech.qualis.registration.model.RegistrationDecisionHistory;
import com.agaramtech.qualis.registration.model.RegistrationHistory;
import com.agaramtech.qualis.registration.model.RegistrationParameter;
import com.agaramtech.qualis.registration.model.RegistrationTest;
import com.agaramtech.qualis.registration.model.RegistrationTestHistory;
import com.agaramtech.qualis.registration.model.ReleaseParameter;
import com.agaramtech.qualis.registration.model.ResultChangeHistory;
import com.agaramtech.qualis.registration.model.ResultParamCommentHistory;
import com.agaramtech.qualis.registration.model.ResultParameterComments;
import com.agaramtech.qualis.registration.model.SampleApprovalHistory;
import com.agaramtech.qualis.registration.model.SeqNoRegistration;
import com.agaramtech.qualis.release.model.COAChild;
import com.agaramtech.qualis.resultentry.service.ResultEntryDAO;
import com.agaramtech.qualis.testgroup.model.TestGroupTestPredefinedParameter;
import com.agaramtech.qualis.testmanagement.model.Grade;
import com.agaramtech.qualis.transactionhistory.service.HistoryDAO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;



@AllArgsConstructor
@Repository
public class ApprovalDAOImpl implements ApprovalDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApprovalDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final AttachmentDAO attachmentDAO;
	private final CommentDAO commentDAO;
	private final HistoryDAO historyDAO;
	private final FTPUtilityFunction ftpUtilityFunction;
	private final EmailDAOSupport emailDAOSupport;
	private final ResultEntryDAO resultEntryDAO;

	
	@Override
	public ResponseEntity<Object> getApproval(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {// --

		final ObjectMapper objMapper = new ObjectMapper();
		 String fromDate = (String) inputMap.get("dfrom");
		 String toDate = (String) inputMap.get("dto");
		Map<String, Object> returnMap = new HashMap<>();
		String dfromdate = "";
		String dtodate = "";
		
		// Ate234 Janakumar ALPD-5123 Test Approval -> To get previously saved filter details when click the filter name
		final String strQueryObj = "select json_agg(jsondata || jsontempdata) as jsondata from filterdetail where nformcode="
				+ userInfo.getNformcode() + " and nusercode=" + userInfo.getNusercode() + "  and nuserrolecode="
				+ userInfo.getNuserrole() + " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final String strFilter = jdbcTemplate.queryForObject(strQueryObj, String.class);

		final List<Map<String, Object>> lstfilterDetail = strFilter != null
				? objMapper.readValue(strFilter, new TypeReference<List<Map<String, Object>>>() {
				})
				: new ArrayList<Map<String, Object>>();

		if (!lstfilterDetail.isEmpty() && lstfilterDetail.get(0).containsKey("FromDate")
				&& lstfilterDetail.get(0).containsKey("ToDate")) {

			final Instant instantFromDate = dateUtilityFunction
					.convertStringDateToUTC(lstfilterDetail.get(0).get("FromDate").toString(), userInfo, true);
			final Instant instantToDate = dateUtilityFunction
					.convertStringDateToUTC(lstfilterDetail.get(0).get("ToDate").toString(), userInfo, true);

			dfromdate = dateUtilityFunction.instantDateToString(instantFromDate);
			dtodate = dateUtilityFunction.instantDateToString(instantToDate);

			returnMap.put("FromDate", dfromdate);
			returnMap.put("ToDate", dtodate);
			inputMap.put("dfrom", dfromdate);
			inputMap.put("dto", dtodate);

			final DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
			final LocalDateTime ldt = LocalDateTime.parse(dfromdate, formatter1);
			final LocalDateTime ldt1 = LocalDateTime.parse(dtodate, formatter1);

			final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());
			String formattedFromString = "";
			String formattedToString = "";

			if (userInfo.getIsutcenabled() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				final ZonedDateTime zonedDateFromTime = ZonedDateTime.of(ldt, ZoneId.of(userInfo.getStimezoneid()));
				formattedFromString = zonedDateFromTime.format(formatter);
				final ZonedDateTime zonedDateToTime = ZonedDateTime.of(ldt1, ZoneId.of(userInfo.getStimezoneid()));
				formattedToString = zonedDateToTime.format(formatter);

			} else {
				formattedFromString = formatter.format(ldt);
				formattedToString = formatter.format(ldt1);

			}

			returnMap.put("realFromDate", formattedFromString);
			returnMap.put("realToDate", formattedToString);

			inputMap.put("filterDetailValue", lstfilterDetail);
			returnMap.put("fromdate", formattedFromString);
			returnMap.put("todate", formattedToString);
		} else {
			if (!inputMap.containsKey("dfrom")) {
				final Map<String, Object> mapObject = projectDAOSupport.getDateFromControlProperties(userInfo,
						(String) inputMap.get("currentdate"), "datetime", "FromDate");
				fromDate = (String) mapObject.get("FromDate");
				toDate = (String) mapObject.get("ToDate");
				inputMap.put("dfrom", fromDate);
				inputMap.put("dto", toDate);
				returnMap.put("fromDate", mapObject.get("FromDateWOUTC"));
				returnMap.put("toDate", mapObject.get("ToDateWOUTC"));
				returnMap.put("realFromDate", mapObject.get("FromDateWOUTC"));
				returnMap.put("realToDate", mapObject.get("ToDateWOUTC"));
			}
		}

		final String sampleTypeQuery = "select st.nsampletypecode,coalesce(st.jsondata->'sampletypename'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',st.jsondata->'sampletypename'->>'en-US') ssampletypename,st.nsorter "
				+ "from sampletype st,registrationtype rt,registrationsubtype rst,approvalconfig ac,approvalconfigversion acv "
				+ " where st.nsampletypecode > 0 "
				+ "and ac.nsitecode= " + userInfo.getNmastersitecode() + " and rt.nsitecode="
				+ userInfo.getNmastersitecode() + " " + "and rst.nsitecode=" + userInfo.getNmastersitecode() + " "
				+ "and acv.nsitecode=" + userInfo.getNmastersitecode() + " and st.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and st.napprovalconfigview = "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " "
				+ "and st.nsampletypecode=rt.nsampletypecode and rt.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and rt.nregtypecode=rst.nregtypecode and rst.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and rst.nregsubtypecode=ac.nregsubtypecode and ac.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and acv.napprovalconfigcode=ac.napprovalconfigcode and acv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acv.ntransactionstatus= "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " "
				+ "group by st.nsampletypecode,st.nsorter order by st.nsorter; ";

		final List<SampleType> lstSampleType = jdbcTemplate.query(sampleTypeQuery, new SampleType());

		final String retestCount = "select ssettingvalue from settings where nsettingcode=8 and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final Settings objSettings = (Settings) jdbcUtilityFunction.queryForObject(retestCount, Settings.class,
				jdbcTemplate);

		if (objSettings != null) {
			returnMap.put("retestcount", objSettings.getSsettingvalue());
		} else {
			returnMap.put("retestcount", 1);
		}

		if (lstSampleType.size() > 0) {

			final SampleType filterSampleType = !lstfilterDetail.isEmpty()
					? objMapper.convertValue(lstfilterDetail.get(0).get("sampleTypeValue"), SampleType.class)
					: lstSampleType.get(0);

			returnMap.put("SampleType", lstSampleType);
			returnMap.put("realSampleTypeList", lstSampleType);
			inputMap.put("nsampletypecode", filterSampleType.getNsampletypecode());
			inputMap.put("npreregno", "0");
			inputMap.put("checkBoxOperation", 3);
			inputMap.put("ntype", 2);
			inputMap.put("activeSubSampleTab", "IDS_SUBSAMPLEATTACHMENTS");
			returnMap.put("SampleTypeValue", filterSampleType);
			returnMap.put("realSampleTypeValue", filterSampleType);

			returnMap.putAll((Map<String, Object>) getRegistrationType(inputMap, userInfo).getBody());

			returnMap.put("realRegTypeValue", returnMap.get("RegTypeValue"));
			returnMap.put("realRegSubTypeValue", returnMap.get("RegSubTypeValue"));
			returnMap.put("realApprovalVersionValue", returnMap.get("ApprovalVersionValue"));
			returnMap.put("realFilterStatusValue", returnMap.get("FilterStatusValue"));
			returnMap.put("realUserSectionValue", returnMap.get("UserSectionValue"));
			returnMap.put("realTestValue", returnMap.get("TestValue"));
			returnMap.put("realDesignTemplateMappingValue", returnMap.get("DesignTemplateMappingValue"));
			returnMap.put("activeSubSampleTab", "IDS_SUBSAMPLEATTACHMENTS");
			returnMap.put("activeTestTab", "");
		}
		final List<FilterName> lstFilterName = projectDAOSupport.getFavoriteFilterName(userInfo);
		returnMap.put("FilterName", lstFilterName);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
		// Ate234 END Janakumar ALPD-5123 Test Approval -> To get previously saved
		// filter details when click the filter name

	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Map<String, Object>> getRegistrationType(Map<String, Object> inputMap,final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> returnMap = new HashMap<>();
		final ObjectMapper objMapper = new ObjectMapper();

		final String strQuery = "select * from sampletype  where  nsampletypecode=" + inputMap.get("nsampletypecode");

		final SampleType objSampleType = (SampleType) jdbcUtilityFunction.queryForObject(strQuery, SampleType.class, jdbcTemplate);
		String ValidationQuery = "";
		if (objSampleType.getNtransfiltertypecode() != -1 && userInfo.getNusercode() != -1) {
			int nmappingfieldcode = (objSampleType.getNtransfiltertypecode() == 1) ? userInfo.getNdeptcode()
					: userInfo.getNuserrole();
			ValidationQuery = " and rst.nregsubtypecode in(SELECT rs.nregsubtypecode "
					+ "		FROM registrationsubtype rs "
					+ "		INNER JOIN transactionfiltertypeconfig ttc ON rs.nregsubtypecode = ttc.nregsubtypecode "
					+ "		LEFT JOIN transactionusers tu ON tu.ntransfiltertypeconfigcode = ttc.ntransfiltertypeconfigcode "
					+ "		WHERE ( ttc.nneedalluser = " + Enumeration.TransactionStatus.YES.gettransactionstatus()
					+ " and ttc.nstatus =  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " AND ttc.nmappingfieldcode = " + nmappingfieldcode + ") " + "	 OR "
					+ "	( ttc.nneedalluser = " + Enumeration.TransactionStatus.NO.gettransactionstatus() + "  "
					+ "	  AND ttc.nmappingfieldcode =" + nmappingfieldcode + "	  AND tu.nusercode ="
					+ userInfo.getNusercode() + "   and ttc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "   and tu.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") " + "  OR "
					+ "	( ttc.nneedalluser = " + Enumeration.TransactionStatus.NO.gettransactionstatus() + "  "
					+ " AND ttc.nmappingfieldcode = -1 " + " AND tu.nusercode =" + userInfo.getNusercode()
					+ " and ttc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tu.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") "
					+ "	 AND rs.nregtypecode = rt.nregtypecode) ";
		}
		final String regTypeQuery = "select  rt.nregtypecode,coalesce(rt.jsondata->'sregtypename'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',rt.jsondata->'sregtypename'->>'en-US') as sregtypename ,rt.nsorter "
				+ "from SampleType st,registrationtype rt,registrationsubtype rst,approvalconfig ac,approvalconfigversion acv"
				+ " where st.nsampletypecode > 0 and st.nsampletypecode =" + inputMap.get("nsampletypecode") + " "
				+ "and st.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and st.napprovalconfigview = " + Enumeration.TransactionStatus.YES.gettransactionstatus()
				+ " and st.nsampletypecode=rt.nsampletypecode and rt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and rt.nregtypecode=rst.nregtypecode and rst.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and rst.nregsubtypecode=ac.nregsubtypecode and ac.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and st.nsitecode="
				+ userInfo.getNmastersitecode() + "" + " and rt.nsitecode=" + userInfo.getNmastersitecode() + ""
				+ " and rst.nsitecode=" + userInfo.getNmastersitecode() + " and ac.nsitecode="
				+ userInfo.getNmastersitecode() + "" + " and acv.nsitecode=" + userInfo.getNmastersitecode() + " "
				+ " and acv.napprovalconfigcode=ac.napprovalconfigcode and acv.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and acv.ntransactionstatus = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and rt.nregtypecode>0 "
				+ ValidationQuery + " group by rt.nregtypecode,coalesce(rt.jsondata->'sregtypename'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',rt.jsondata->'sregtypename'->>'en-US'),rt.nsorter order by rt.nregtypecode desc";

		List<RegistrationType> regTypeList = jdbcTemplate.query(regTypeQuery, new RegistrationType());

		short regTypeCode = -1;
		Map<String, Object> valueMap = new HashMap<String, Object>();
		if (regTypeList.size() > 0) {
			// Ate234 START Janakumar ALPD-5123 Test Approval -> To get previously saved
			// filter details when click the filter name

			final RegistrationType filterRegType = inputMap.containsKey("filterDetailValue")
					&& !((List<Map<String, Object>>) inputMap.get("filterDetailValue")).isEmpty()
					&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0)
							.containsKey("regTypeValue")
									? objMapper.convertValue(
											((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0)
													.get("regTypeValue"),
											RegistrationType.class)
									: regTypeList.get(0);

			regTypeCode = filterRegType.getNregtypecode();
			valueMap = objMapper.convertValue(filterRegType, Map.class);
			inputMap.put("nregtypecode", regTypeCode);
			returnMap.putAll((Map<String, Object>) getRegistrationSubType(inputMap, userInfo).getBody());
		}
		returnMap.put("RegType", regTypeList);
		returnMap.put("realRegTypeList", regTypeList);
		returnMap.put("RegTypeValue", valueMap);
		// Ate234 END Janakumar ALPD-5123 Test Approval -> To get previously saved
		// filter details when click the filter name

		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getRegistrationSubType(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {// --
		Map<String, Object> returnMap = new HashMap<>();
		final ObjectMapper objMapper = new ObjectMapper();

		final String str = "select * from registrationtype rt,sampletype st where rt.nsampletypecode=st.nsampletypecode and rt.nregtypecode="
				+ inputMap.get("nregtypecode")+" and rt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
				+ " and st.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
				+ " and rt.nsitecode="+userInfo.getNmastersitecode()+"";

		SampleType objSampleType = (SampleType) jdbcUtilityFunction.queryForObject(str, SampleType.class, jdbcTemplate);
		
		String validationQuery = "";
		if (objSampleType.getNtransfiltertypecode() != -1 && userInfo.getNusercode() != -1) {
			int nmappingfieldcode = (objSampleType.getNtransfiltertypecode() == 1) ? userInfo.getNdeptcode()
					: userInfo.getNuserrole();
			validationQuery = " and rst.nregsubtypecode in(SELECT rs.nregsubtypecode "
					+ " FROM registrationsubtype rs "
					+ " INNER JOIN transactionfiltertypeconfig ttc ON rs.nregsubtypecode = ttc.nregsubtypecode "
					+ " LEFT JOIN transactionusers tu ON tu.ntransfiltertypeconfigcode = ttc.ntransfiltertypeconfigcode "
					+ " WHERE ( ttc.nneedalluser = " + Enumeration.TransactionStatus.YES.gettransactionstatus()
					+ "  and ttc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " AND ttc.nmappingfieldcode = " + nmappingfieldcode + ")" + "  OR ( ttc.nneedalluser = "
					+ Enumeration.TransactionStatus.NO.gettransactionstatus() + "  " + " AND ttc.nmappingfieldcode ="
					+ nmappingfieldcode + " AND tu.nusercode =" + userInfo.getNusercode() + " and ttc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tu.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") " + "  OR "
					+ "	( ttc.nneedalluser = " + Enumeration.TransactionStatus.NO.gettransactionstatus() + "  "
					+ " AND ttc.nmappingfieldcode = -1 " + " AND tu.nusercode =" + userInfo.getNusercode()
					+ " and ttc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tu.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") "
					+ " AND rs.nregtypecode = " + inputMap.get("nregtypecode") + ")";
		}

		final String regTypeQuery = "select rst.nregsubtypecode,rst.nregtypecode,coalesce(rst.jsondata->'sregsubtypename'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',rst.jsondata->'sregsubtypename'->>'en-US') as sregsubtypename,cast(rsc.jsondata->>'nneedsubsample' as boolean) nneedsubsample,"
				+ "cast(rsc.jsondata->>'nneedtemplatebasedflow' as boolean) nneedtemplatebasedflow,cast(rsc.jsondata->>'nneedbatch' as boolean) nneedbatch "
				+ ",cast(rsc.jsondata->>'nneedjoballocation' as boolean)  nneedjoballocation,cast(rsc.jsondata->>'nneedmyjob' as boolean)  nneedmyjob  from approvalconfig ac,approvalconfigversion acv,registrationsubtype rst,regsubtypeconfigversion rsc"
				+ " where acv.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and rsc.napprovalconfigcode = ac.napprovalconfigcode and rsc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rsc.ntransactionstatus = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and ac.napprovalconfigcode = acv.napprovalconfigcode and rst.nregtypecode = "
				+ inputMap.get("nregtypecode")
				+ " and ac.napprovalconfigcode = acv.napprovalconfigcode and rst.nregsubtypecode = ac.nregsubtypecode"
				+ " and rst.nregsubtypecode > 0 " + " and rsc.nsitecode=" + userInfo.getNmastersitecode() + ""
				+ " and rst.nsitecode=" + userInfo.getNmastersitecode() + "" + " and ac.nsitecode="
				+ userInfo.getNmastersitecode() + "" + " and acv.nsitecode=" + userInfo.getNmastersitecode() + ""
				+ " and acv.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rst.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ac.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + validationQuery
				+ " order by rst.nregsubtypecode desc";

		List<RegistrationSubType> regSubTypeList = jdbcTemplate.query(regTypeQuery, new RegistrationSubType());
		// Ate234 START Janakumar ALPD-5123 Test Approval -> To get previously saved
		// filter details when click the filter name

		short regSubTypeCode = -1;
		Map<String, Object> valueMap = new HashMap<String, Object>();
		if (regSubTypeList.size() > 0) {
			final RegistrationSubType filterRegSubType = inputMap.containsKey("filterDetailValue")
					&& !((List<Map<String, Object>>) inputMap.get("filterDetailValue")).isEmpty()
					&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0)
							.containsKey("regSubTypeValue") ? objMapper
									.convertValue(((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0)
											.get("regSubTypeValue"), RegistrationSubType.class)
									: regSubTypeList.get(0);

			regSubTypeCode = filterRegSubType.getNregsubtypecode();
			returnMap.put("nneedsubsample", filterRegSubType.isNneedsubsample());
			inputMap.put("nneedsubsample", filterRegSubType.isNneedsubsample());
			inputMap.put("nneedjoballocation", filterRegSubType.isNneedjoballocation());
			returnMap.put("nneedjoballocation", filterRegSubType.isNneedjoballocation());
			inputMap.put("nneedtemplatebasedflow", filterRegSubType.isNneedtemplatebasedflow());
			returnMap.put("nneedmyjob", filterRegSubType.isNneedmyjob());
			valueMap = objMapper.convertValue(filterRegSubType, Map.class);
		}
		returnMap.put("RegSubType", regSubTypeList);
		returnMap.put("realRegSubTypeList", regSubTypeList);
		inputMap.put("nregsubtypecode", regSubTypeCode);
		returnMap.put("RegSubTypeValue", valueMap);

		returnMap.putAll((Map<String, Object>) projectDAOSupport.getReactRegistrationTemplateList(
				Short.valueOf(inputMap.get("nregtypecode").toString()),
				Short.valueOf(inputMap.get("nregsubtypecode").toString()),
				(boolean) inputMap.get("nneedtemplatebasedflow"),userInfo).getBody());
		
		inputMap.put("ndesigntemplatemappingcode", returnMap.get("ndesigntemplatemappingcode"));
		returnMap.put("realndesigntemplatemappingcode", returnMap.get("ndesigntemplatemappingcode"));
		
		returnMap.putAll((Map<String, Object>) getApprovalVersion(inputMap, userInfo).getBody());


		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	public List<Section> getSectionCombo(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		List<Section> userSection = new ArrayList<Section>();
		Section objSection = new Section();

		final String userSectionQuery = "select nsectioncode,ssectionname from section where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsectioncode = any ("
				+ " select ls.nsectioncode from sectionusers su,labsection ls  where su.nusercode ="
				+ userInfo.getNusercode() + " and ls.nlabsectioncode=su.nlabsectioncode and su.nsitecode="
				+ userInfo.getNtranssitecode() + " and ls.nsitecode="
				+ userInfo.getNtranssitecode() + " and su.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ls.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by ls.nsectioncode)";
		List<Section> lst2 = (List<Section>) jdbcTemplate.query(userSectionQuery, new Section());

		if (lst2.size() > 1) {
			objSection.setNsectioncode(Enumeration.TransactionStatus.NA.gettransactionstatus());
			objSection
					.setSsectionname(commonFunction.getMultilingualMessage("IDS_ALL", userInfo.getSlanguagefilename()));
			userSection.add(objSection);
		}
		userSection.addAll(lst2);

		return userSection;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getApprovalVersion(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {// --
		Map<String, Object> returnMap = new HashMap<>();
		final ObjectMapper objMapper = new ObjectMapper();

		String fromDate = (String) inputMap.get("dfrom");
		String toDate = (String) inputMap.get("dto");
		fromDate = dateUtilityFunction
				.instantDateToString(dateUtilityFunction.convertStringDateToUTC(fromDate, userInfo, true));
		toDate = dateUtilityFunction
				.instantDateToString(dateUtilityFunction.convertStringDateToUTC(toDate, userInfo, true));

		final String regTypeQuery = "select aca.sversionname,aca.napprovalconfigcode,aca.napprovalconfigversioncode ,acv.ntransactionstatus,acv.ntreeversiontempcode "
				+ " from registration r,registrationarno rar,registrationhistory rh,approvalconfigautoapproval aca,"
				+ " approvalconfig ac,approvalconfigversion acv "
				+ "	where r.npreregno=rar.npreregno and r.npreregno=rh.npreregno"
				+ " and rar.napprovalversioncode=acv.napproveconfversioncode"
				+ " and acv.napproveconfversioncode=aca.napprovalconfigversioncode"
				+ " and r.nregtypecode=ac.nregtypecode and r.nregsubtypecode=ac.nregsubtypecode"
				+ " and r.nregtypecode =" + inputMap.get("nregtypecode") + " and r.nregsubtypecode ="
				+ inputMap.get("nregsubtypecode") + " and rh.ntransactionstatus = "
				+ Enumeration.TransactionStatus.REGISTERED.gettransactionstatus() + " and acv.nsitecode ="
				+ userInfo.getNmastersitecode() + " and rh.dtransactiondate between '" + fromDate + "'::timestamp"
				+ " and '" + toDate + "'::timestamp and r.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rar.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rh.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ac.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ac.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and aca.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  r.nsitecode=rar.nsitecode"
				+ " and rar.nsitecode=rh.nsitecode" + " and r.nsitecode=" + userInfo.getNtranssitecode()
				+ " and rar.nsitecode=" + userInfo.getNtranssitecode() + " and rh.nsitecode="
				+ userInfo.getNtranssitecode() + " and ac.nsitecode=" + userInfo.getNmastersitecode()
				+ " and aca.nsitecode=" + userInfo.getNmastersitecode()
				+ " group by aca.sversionname,aca.napprovalconfigcode,aca.napprovalconfigversioncode,acv.ntransactionstatus,acv.ntreeversiontempcode";

		List<ApprovalConfigAutoapproval> approvalVersion = jdbcTemplate.query(regTypeQuery,
				new ApprovalConfigAutoapproval());
		String versionCode = "-1";
		int configCode = -1;
		// Ate234 START Janakumar ALPD-5123 Test Approval -> To get previously saved
		// filter details when click the filter name

		Map<String, Object> valueVersionMap = new HashMap<String, Object>();
		if (approvalVersion.size() > 0) {

			final ApprovalConfigAutoapproval filterApprovalConfig = inputMap.containsKey("filterDetailValue")
					&& !((List<Map<String, Object>>) inputMap.get("filterDetailValue")).isEmpty()
					&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0)
							.containsKey("approvalConfigValue")
									? objMapper
											.convertValue(
													((List<Map<String, Object>>) inputMap.get("filterDetailValue"))
															.get(0).get("approvalConfigValue"),
													ApprovalConfigAutoapproval.class)
									: approvalVersion.get(0);

			configCode = filterApprovalConfig.getNapprovalconfigcode();
			ApprovalConfigAutoapproval approvedApprovalVersion = new ApprovalConfigAutoapproval();
			List<ApprovalConfigAutoapproval> approvedApprovalVersionList = approvalVersion.stream().filter(
					x -> x.getNtransactionstatus() == Enumeration.TransactionStatus.APPROVED.gettransactionstatus())
					.collect(Collectors.toList());
			if (approvedApprovalVersionList != null && approvedApprovalVersionList.size() > 0) {
				approvedApprovalVersion = filterApprovalConfig;
				inputMap.put("napprovalversioncode", filterApprovalConfig.getNapprovalconfigversioncode());
			} else {
				approvedApprovalVersion = approvalVersion.get(0);
			}

			versionCode = String.valueOf(approvedApprovalVersion.getNapprovalconfigversioncode());
			valueVersionMap = objMapper.convertValue(approvedApprovalVersion, Map.class);

		}
		returnMap.put("ApprovalVersion", approvalVersion);
		returnMap.put("realApprovalVersionList", approvalVersion);
		returnMap.put("ApprovalVersionValue", valueVersionMap);
		inputMap.put("napprovalconfigcode", configCode);
		inputMap.put("napprovalversioncode", versionCode);

		returnMap.putAll((Map<String, Object>) getFilterStatus(inputMap, userInfo).getBody());
		returnMap.putAll((Map<String, Object>) getApproveConfigVersionRegTemplateDesign(inputMap, userInfo).getBody());

		final DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		final LocalDateTime lfrom = LocalDateTime.parse(fromDate, formatter1);
		final LocalDateTime lto = LocalDateTime.parse(toDate, formatter1);
		final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());

		String frormattedFrom = "";
		String frormattedTo = "";
		if (userInfo.getIsutcenabled() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
			ZonedDateTime zonedDateTime = ZonedDateTime.of(lfrom, ZoneId.of(userInfo.getStimezoneid()));
			frormattedFrom = zonedDateTime.format(formatter);
			zonedDateTime = ZonedDateTime.of(lto, ZoneId.of(userInfo.getStimezoneid()));
			frormattedTo = zonedDateTime.format(formatter);
		} else {
			frormattedFrom = formatter.format(lfrom);
			frormattedTo = formatter.format(lto);
		}
		returnMap.put("fromDate", frormattedFrom);
		returnMap.put("toDate", frormattedTo);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
		// Ate234 END Janakumar ALPD-5123 Test Approval -> To get previously saved
		// filter details when click the filter name

	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getFilterStatus(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {// --

		if (!inputMap.containsKey("nflag")) {
			inputMap.put("nflag", 1);
		}
		Map<String, Object> returnMap = new HashMap<>();
		final ObjectMapper objMapper = new ObjectMapper();
		final String regTypeQuery = "select afd.ntransactionstatus,ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "' as sfilterstatus,afd.ndefaultstatus,afd.napprovalconfigcode   "
				+ " from approvalrolefilterdetail afd,approvalconfig ac,transactionstatus ts,approvalconfigrole acr,approvalconfigversion acv "
				+ " where afd.napprovalconfigcode = ac.napprovalconfigcode "
				+ "	and afd.ntransactionstatus = ts.ntranscode and ac.napprovalsubtypecode = "+Enumeration.ApprovalSubType.TESTRESULTAPPROVAL.getnsubtype()+" and ac.nregtypecode ="
				+ inputMap.get("nregtypecode") + " and ac.nregsubtypecode =" + inputMap.get("nregsubtypecode")
				+ " and acv.napproveconfversioncode=acr.napproveconfversioncode "
				+ " and afd.napprovalconfigrolecode=acr.napprovalconfigrolecode and acv.ntransactionstatus in ("
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + ") and acr.nuserrolecode ="
				+ userInfo.getNuserrole() + " and afd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acr.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ac.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acv.napproveconfversioncode = "
				+ inputMap.get("napprovalversioncode") + "" + " and afd.nsitecode=" + userInfo.getNmastersitecode() + ""
				+ " and ac.nsitecode=" + userInfo.getNmastersitecode() + " and acr.nsitecode="
				+ userInfo.getNmastersitecode() + " order by afd.ntransactionstatus,afd.ndefaultstatus";

		final List<ApprovalRoleFilterDetail> lstFilterStatus = jdbcTemplate.query(regTypeQuery,
				new ApprovalRoleFilterDetail());

		returnMap.put("FilterStatus", lstFilterStatus);
		returnMap.put("realFilterStatusList", lstFilterStatus);
		String transactionStatus = "-1";
		ApprovalRoleFilterDetail filterValue = new ApprovalRoleFilterDetail();
		final ApprovalRoleFilterDetail filterTransactionStatus;
		// Ate234 START Janakumar ALPD-5123 Test Approval -> To get previously saved
		// filter details when click the filter name

		if (lstFilterStatus.size() > 0) {

			filterTransactionStatus = inputMap.containsKey("filterDetailValue")
					&& !((List<Map<String, Object>>) inputMap.get("filterDetailValue")).isEmpty()
					&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0)
							.containsKey("filterStatusValue")
									? objMapper
											.convertValue(
													((List<Map<String, Object>>) inputMap.get("filterDetailValue"))
															.get(0).get("filterStatusValue"),
													ApprovalRoleFilterDetail.class)
									: lstFilterStatus.get(0);
			String stransactionstatus = "-1";
			if (inputMap.containsKey("filterDetailValue")
					&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0)
							.containsKey("nfiltertransactionstatus")) {
				transactionStatus = String.valueOf(filterTransactionStatus.getNtransactionstatus());// 23
				stransactionstatus = (String) ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0)
						.get("nfiltertransactionstatus");// 23,0,8,
				filterValue = objMapper.convertValue(
						((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0).get("filterStatusValue"),
						ApprovalRoleFilterDetail.class);
			} else {
				filterValue = lstFilterStatus.stream()
						.filter(x -> x.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus())
						.collect(Collectors.toList()).get(0);
				transactionStatus = String.valueOf(filterValue.getNtransactionstatus());

				stransactionstatus = lstFilterStatus.stream().map(x -> String.valueOf(x.getNtransactionstatus()))
						.collect(Collectors.joining(","));
			}

			inputMap.put("stransactionstatus", stransactionstatus);
			inputMap.put("ntransactionstatus", transactionStatus);

			returnMap.putAll((Map<String, Object>) getFilterBasedComboTest(inputMap, userInfo).getBody());

			if ((int) inputMap.get("nflag") == 1) {
				final ObjectMapper obj = new ObjectMapper();
				RegistrationTest rt = obj.convertValue(returnMap.get("TestValue"), RegistrationTest.class);
				if (rt != null) {
					inputMap.put("ntestcode", rt.getNtestcode());
				} else {
					inputMap.put("ntestcode", Enumeration.TransactionStatus.NA.gettransactionstatus());
				}
				returnMap.putAll((Map<String, Object>) getApprovalSample(inputMap, userInfo).getBody());
			}
		} else {
			returnMap.put("Test", lstFilterStatus);
			returnMap.put("UserSection", lstFilterStatus);
			returnMap.put("UserSectionValue", null);
			returnMap.put("TestValue", new HashMap<String, Object>());
		}
		returnMap.put("FilterStatusValue", filterValue);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
		// Ate234 END Janakumar ALPD-5123 Test Approval -> To get previously saved
		// filter details when click the filter name

	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getApproveConfigVersionRegTemplateDesign(Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> returnMap = new HashMap<>();
		returnMap.putAll((Map<String, Object>) projectDAOSupport.getApproveConfigVersionBasedTemplate(
				Short.valueOf(inputMap.get("nregtypecode").toString()),
				Short.valueOf(inputMap.get("nregsubtypecode").toString()),
				Short.valueOf(inputMap.get("napprovalversioncode").toString())).getBody());
		inputMap.put("ndesigntemplatemappingcode", returnMap.get("ndesigntemplatemappingcode"));
		returnMap.putAll((Map<String, Object>) getFilterBasedComboTest(inputMap, userInfo).getBody());
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getFilterBasedComboTest(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		Map<String, Object> returnMap = new HashMap<>();
		List<Object> regTest = new ArrayList<>();
		ObjectMapper objMapper = new ObjectMapper();
		if (inputMap.get("napprovalversioncode") == null) {
			returnMap.put("rtn", commonFunction.getMultilingualMessage("IDS_PLSSELECTALLTHEVALUEINFILTER",
					userInfo.getSlanguagefilename()));
			return new ResponseEntity<>(returnMap, HttpStatus.EXPECTATION_FAILED);
		}

		List<Section> userSection = getSectionCombo(inputMap, userInfo);

		boolean getAll = false;

		if (inputMap.get("nsectioncode") == null) {
			getAll = true;
			if (userSection.size() > 0) {
				returnMap.put("UserSectionValue", userSection.get(0));
				inputMap.put("nsectioncode", userSection.stream().map(x -> String.valueOf(x.getNsectioncode()))
						.collect(Collectors.joining(",")));
			} else {
				returnMap.put("UserSectionValue", userSection);
				inputMap.put("nsectioncode", "-1");
			}
		}

		int designtemplatemapping = -1;

		if (inputMap.containsKey("nneedtemplatebasedflow")) {
			if ((boolean) inputMap.get("nneedtemplatebasedflow")) {
				designtemplatemapping = (int) inputMap.get("ndesigntemplatemappingcode");
			}
		}

		final String strTestQuery = "SELECT * from fn_approvalfiltertestget ('" + inputMap.get("dfrom") + "','"
				+ inputMap.get("dto") + "'," + inputMap.get("nregtypecode") + "," + inputMap.get("nregsubtypecode")
				+ ",'" + inputMap.get("ntransactionstatus") + "'," + "'" + inputMap.get("napprovalversioncode") + "',"
				+ userInfo.getNusercode() + "," + userInfo.getNuserrole() + "," + userInfo.getNtranssitecode() + ",'"
				+ inputMap.get("nsectioncode") + "','" + inputMap.get("stransactionstatus") + "',"
				+ designtemplatemapping + ");";

		List<?> lst = jdbcTemplate.queryForList(strTestQuery);

		final String str = "select 0 as ntestcode,'IDS_ALL' as stestsynonym, -1 as nsectioncode";

		List<?> lst1 = jdbcTemplate.queryForList(str);
		lst1 = commonFunction.getMultilingualMessageList(lst1, Arrays.asList("stestsynonym"),
				userInfo.getSlanguagefilename());

		if (!lst.isEmpty()) {
			if (lst.size() > 1) {
				regTest.addAll(lst1);
				returnMap.put("Batchvalues", new ArrayList<>());
				returnMap.put("defaultBatchvalue", new ArrayList<>());
			} else {
				inputMap.put("ntestcode",
						((RegistrationTest) objMapper.convertValue(lst.get(0), RegistrationTest.class)).getNtestcode());
				inputMap.put("ntranscode", inputMap.get("stransactionstatus").toString());
				returnMap.putAll((Map<String, Object>) getTestBasedOnCompletedBatch(inputMap, userInfo).getBody());
			}
			regTest.addAll(lst);
			returnMap.put("Test", regTest);
			returnMap.put("realTestList", regTest);
			returnMap.put("TestValue", regTest.get(0));

			final List<Map<String, Object>> testList = objMapper.convertValue(regTest,
					new TypeReference<List<Map<String, Object>>>() {
					});

			final Set<String> sectionSet = new TreeSet<String>();
			for (Object testMap : testList) {
				final HashMap<String, Object> testMap1 = (HashMap<String, Object>) testMap;
				sectionSet.add(((Integer) testMap1.get("nsectioncode")).toString());
			}

			if (sectionSet.size() > 0) {
				String sectionString = String.join(",", sectionSet);

				List<Section> sectionList = new ArrayList<Section>();

				List<Section> testSectionList = userSection.stream()
						.filter(item -> sectionString.contains(Integer.toString(item.getNsectioncode())))
						.collect(Collectors.toList());

				if (getAll) {
					sectionList.addAll(testSectionList);
					returnMap.put("UserSection", sectionList);
					returnMap.put("realUserSectionList", sectionList);
					returnMap.put("UserSectionValue", sectionList.get(0));
				}

			} else {
				returnMap.put("UserSection", null);
				returnMap.put("UserSectionValue", null);
			}

		} else {
			returnMap.put("Test", lst);
			returnMap.put("TestValue", new HashMap<String, Object>());
			returnMap.put("UserSection", null);
			returnMap.put("UserSectionValue", null);
			returnMap.put("Batchvalues", new ArrayList<>());
			returnMap.put("defaultBatchvalue", new ArrayList<>());
		}

		return new ResponseEntity<Object>(returnMap, HttpStatus.OK);

	}

	public Map<String, Object> getApprovalSection(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {// --

		Map<String, Object> returnMap = new HashMap<>();
		List<Section> userSection = new ArrayList<Section>();
		final Section objSection = new Section();

		objSection.setNsectioncode(Enumeration.TransactionStatus.ALL.gettransactionstatus());
		objSection.setSsectionname(commonFunction.getMultilingualMessage("IDS_ALL", userInfo.getSlanguagefilename()));
		userSection.add(objSection);

		final String userSectionQuery = "select nsectioncode,ssectionname from section where  nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsectioncode = any ("
				+ " select ls.nsectioncode from sectionusers su,labsection ls where su.nusercode ="
				+ userInfo.getNusercode() + " and ls.nlabsectioncode=su.nlabsectioncode	and su.nsitecode="
				+ userInfo.getNtranssitecode() + " and ls.nsitecode="
				+ userInfo.getNtranssitecode() + ""
				+" and  su.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+" and ls.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " group by ls.nsectioncode) and nsitecode="+userInfo.getNmastersitecode()+"";

		userSection.addAll((List<Section>) jdbcTemplate.query(userSectionQuery, new Section()));
		returnMap.put("UserSection", userSection);
		returnMap.put("UserSectionValue", userSection.get(0));

		return returnMap;

	}

	public Map<String, Object> getJobStatus(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {// --

		Map<String, Object> returnMap = new HashMap<>();

		final String jobStatusQuery = "select njobstatuscode,sidsjobstatusname from jobstatus where  nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and njobstatuscode>0";

		List<?> jobStatus = jdbcTemplate.queryForList(jobStatusQuery);

		jobStatus = (List<?>) commonFunction.getMultilingualMessageList(jobStatus,
				Arrays.asList("sidsjobstatusname"), userInfo.getSlanguagefilename());

		returnMap.put("JobStatus", jobStatus);

		if (jobStatus.size() > 0)
			returnMap.put("JobStatusValue", jobStatus.get(0));
		else
			returnMap.put("JobStatusValue", new HashMap<String, Object>());

		return returnMap;

	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getApprovalSample(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {// --
		System.out.println("Sample start=>" + Instant.now());
		int napprovalconfigcode = -1;
		String fromDate = (String) inputMap.get("dfrom");
		String toDate = (String) inputMap.get("dto");
		fromDate = dateUtilityFunction
				.instantDateToString(dateUtilityFunction.convertStringDateToUTC(fromDate, userInfo, true));
		toDate = dateUtilityFunction
				.instantDateToString(dateUtilityFunction.convertStringDateToUTC(toDate, userInfo, true));
		inputMap.put("dfrom", fromDate);
		inputMap.put("dto", toDate);
		String spreregno = "";

		final DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		final LocalDateTime lfrom = LocalDateTime.parse(fromDate, formatter1);
		final LocalDateTime lto = LocalDateTime.parse(toDate, formatter1);
		final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());

		String frormattedFrom = "";
		String frormattedTo = "";
		if (userInfo.getIsutcenabled() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
			ZonedDateTime zonedDateTime = ZonedDateTime.of(lfrom, ZoneId.of(userInfo.getStimezoneid()));
			frormattedFrom = zonedDateTime.format(formatter);
			zonedDateTime = ZonedDateTime.of(lto, ZoneId.of(userInfo.getStimezoneid()));
			frormattedTo = zonedDateTime.format(formatter);
		} else {
			frormattedFrom = formatter.format(lfrom);
			frormattedTo = formatter.format(lto);
		}
		if (!inputMap.containsKey("napprovalconfigcode")) {
			final String getApprovalConfig = "select napprovalconfigcode from approvalconfig where nregtypecode="
					+ inputMap.get("nregtypecode") + " and nregsubtypecode=" + inputMap.get("nregsubtypecode")
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and nsitecode=" + userInfo.getNmastersitecode() + "";

			napprovalconfigcode = jdbcTemplate.queryForObject(getApprovalConfig, Integer.class);
			inputMap.put("napprovalconfigcode", napprovalconfigcode);
		} else {
			napprovalconfigcode = (int) inputMap.get("napprovalconfigcode");
			inputMap.put("napprovalconfigcode", napprovalconfigcode);
		}
		if (!inputMap.containsKey("napprovalversioncode")) {
			final String getApprovalVersion = "select napproveconfversioncode from approvalconfigversion where napprovalconfigcode="
					+ napprovalconfigcode + " and ntransactionstatus in ("
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ","
					+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + ")" + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode() + "";
			List<String> versionList = jdbcTemplate.queryForList(getApprovalVersion, String.class);
			inputMap.put("napprovalversioncode", versionList.stream().collect(Collectors.joining(",")));
		}
		if (((String) inputMap.get("ntransactionstatus"))
				.equals(String.valueOf(Enumeration.TransactionStatus.ALL.gettransactionstatus()))) {
			final String getTransStatus = "select distinct afd.ntransactionstatus from approvalrolefilterdetail afd,approvalconfigrole acr "
					+ " where afd.napprovalconfigrolecode=acr.napprovalconfigrolecode " + " and acr.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and afd.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and afd.ntransactionstatus!= "
					+ Enumeration.TransactionStatus.ALL.gettransactionstatus() + " and acr.napproveconfversioncode in("
					+ inputMap.get("napprovalversioncode") + ") and acr.nuserrolecode=" + userInfo.getNuserrole()
					+ " and acr.nsitecode=" + userInfo.getNmastersitecode() + ""
					+ " and afd.nsitecode=" + userInfo.getNmastersitecode() + "";

			List<String> tranStatusList = jdbcTemplate.queryForList(getTransStatus, String.class);
			inputMap.put("ntransactionstatus", tranStatusList.stream().collect(Collectors.joining(",")));
		}

		Map<String, Object> returnMap = new HashMap<String, Object>();
		String fromDate1 = LocalDateTime.parse(fromDate, formatter1).format(formatter);
		String toDate1 = LocalDateTime.parse(toDate, formatter1).format(formatter);

		returnMap.put("realFromDate", fromDate1);
		returnMap.put("realToDate", toDate1);

		int designtemplatemapping = -1;
		final int nbatchmastercode = inputMap.containsKey("nbatchmastercode") ? (int) inputMap.get("nbatchmastercode")
				: -1;
		if (inputMap.containsKey("nneedtemplatebasedflow") && (boolean) inputMap.get("nneedtemplatebasedflow")) {
			designtemplatemapping = (int) inputMap.get("ndesigntemplatemappingcode");
		}

		String getSampleQuery = "SELECT * from fn_approvalsampleget('" + fromDate + "', '" + toDate + "', " + ""
				+ inputMap.get("nregtypecode") + " , " + inputMap.get("nregsubtypecode") + ", '"
				+ inputMap.get("ntransactionstatus") + "','" + inputMap.get("napprovalversioncode") + "'," + " "
				+ userInfo.getNusercode() + "," + userInfo.getNuserrole() + ", " + userInfo.getNtranssitecode() + ", '"
				+ inputMap.get("npreregno") + "','" + inputMap.get("nsectioncode") + "'," + inputMap.get("ntestcode")
				+ ",'" + userInfo.getSlanguagetypecode() + "'," + designtemplatemapping + "," + nbatchmastercode + ");";

		LOGGER.info(getSampleQuery);

		String SampleListString = jdbcTemplate.queryForObject(getSampleQuery, String.class);

		List<Map<String, Object>> sampleList = new ArrayList<Map<String, Object>>();

		returnMap.put("DynamicDesign", projectDAOSupport
				.getTemplateDesign((int) inputMap.get("ndesigntemplatemappingcode"), userInfo.getNformcode()));
		if (SampleListString != null) {
			sampleList = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(SampleListString, userInfo,
					true, (int) inputMap.get("ndesigntemplatemappingcode"), "sample");

			returnMap.put("AP_SAMPLE", sampleList);

			if (inputMap.containsKey("ntype") && (int) inputMap.get("ntype") == 2) {

				spreregno = String.valueOf(sampleList.get(sampleList.size() - 1).get("npreregno"));
				returnMap.put("APSelectedSample", Arrays.asList(sampleList.get(sampleList.size() - 1)));

				inputMap.put("npreregno", spreregno);
			}

			returnMap.putAll((Map<String, Object>) getApprovalSubSample(inputMap, userInfo).getBody());
			returnMap.putAll((Map<String, Object>) getActionDetails(
					(int) sampleList.get(sampleList.size() - 1).get("napprovalversioncode"), userInfo).getBody());
			returnMap.putAll(getDecisionDetails((int) sampleList.get(sampleList.size() - 1).get("napprovalversioncode"),
					userInfo));
			List<ApprovalRoleValidationDetail> validationStatus = getValidationStatus(
					(int) sampleList.get(sampleList.size() - 1).get("napprovalversioncode"), userInfo);
			returnMap.put("AP_ValidationStatus", validationStatus);
			// Ate234 START Janakumar ALPD-5123 Test Approval -> To get previously saved
			// filter details when click the filter name

			if (inputMap.containsKey("saveFilterSubmit") && (boolean) inputMap.get("saveFilterSubmit") == true) {
				projectDAOSupport.createFilterSubmit(inputMap, userInfo);

			}

			List<FilterName> lstFilterName = projectDAOSupport.getFavoriteFilterName(userInfo);
			returnMap.put("FilterName", lstFilterName);
			// Ate234 END Janakumar ALPD-5123 Test Approval -> To get previously saved
			// filter details when click the filter name

		} else {

			returnMap.put("AP_SUBSAMPLE", new ArrayList<>());
			returnMap.put("AP_TEST", new ArrayList<>());
			returnMap.put("AP_SAMPLE", new ArrayList<>());
		}
		System.out.println("Sample End=>" + Instant.now());
		return new ResponseEntity<>(returnMap, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> getApprovalSubSample(Map<String, Object> inputMap,final UserInfo userInfo)
			throws Exception {
		System.out.println("Sub Sample start=>" + Instant.now());
		final int approvalVersionCode = validateApprovalVersion((String) inputMap.get("npreregno"), userInfo);
		final boolean subSample = (boolean) inputMap.get("nneedsubsample");
		String activeSubSampleTab = inputMap.containsKey("activeSubSampleTab")
				? (String) inputMap.get("activeSubSampleTab")
				: "";
		final int nbatchmastercode = inputMap.containsKey("nbatchmastercode") ? (int) inputMap.get("nbatchmastercode")
				: -1;
		inputMap.get("ntransactionsamplecode");
		if (approvalVersionCode > 0) {
			if (((String) inputMap.get("ntransactionstatus"))
					.equals(String.valueOf(Enumeration.TransactionStatus.ALL.gettransactionstatus()))) {
				final String getTransStatus = "select distinct afd.ntransactionstatus from approvalrolefilterdetail afd,approvalconfigrole acr "
						+ " where afd.napprovalconfigrolecode=acr.napprovalconfigrolecode " + " and acr.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and afd.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and afd.ntransactionstatus!= "
						+ Enumeration.TransactionStatus.ALL.gettransactionstatus()
						+ " and acr.napproveconfversioncode in(" + inputMap.get("napprovalversioncode") + ")"
						+ " and afd.nsitecode=" + userInfo.getNmastersitecode() + " and acr.nuserrolecode="
						+ userInfo.getNuserrole() + " and acr.nsitecode=" + userInfo.getNmastersitecode() + "";
				List<String> tranStatusList = jdbcTemplate.queryForList(getTransStatus, String.class);
				inputMap.put("ntransactionstatus", tranStatusList.stream().collect(Collectors.joining(",")));
			}
			Map<String, Object> returnMap = new HashMap<String, Object>();

			final String getSubSampleQuery = "SELECT * from fn_approvalsubsampleget('" + inputMap.get("npreregno")
					+ "', '" + inputMap.get("ntransactionstatus") + "'," + " " + userInfo.getNusercode() + ","
					+ userInfo.getNuserrole() + "," + inputMap.get("nregtypecode") + ","
					+ inputMap.get("nregsubtypecode") + "," + " '" + inputMap.get("napprovalversioncode") + "',"
					+ userInfo.getNsitecode() + "," + " '" + inputMap.get("nsectioncode") + "',"
					+ inputMap.get("ntestcode") + ",'" + userInfo.getSlanguagetypecode() + "'," + nbatchmastercode
					+ ");";

			LOGGER.info(getSubSampleQuery);
			final String subSampleListString = jdbcTemplate.queryForObject(getSubSampleQuery, String.class);

			List<Map<String, Object>> subSampleList = new ArrayList<>();

			if (subSampleListString != null) {
				subSampleList = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(subSampleListString,
						userInfo, true, (int) inputMap.get("ndesigntemplatemappingcode"), "subsample");
				returnMap.put("AP_SUBSAMPLE", subSampleList);
				String stransactionsampleno = "";
				if (subSample) {
					if ((int) inputMap.get("checkBoxOperation") == 3 || (int) inputMap.get("checkBoxOperation") == 5
							|| (int) inputMap.get("checkBoxOperation") == 7) {
						if (inputMap.containsKey("ntype") && (int) inputMap.get("ntype") == 2) {
							

							returnMap.put("APSelectedSubSample",
									Arrays.asList(subSampleList.get(subSampleList.size() - 1)));
							stransactionsampleno = String
									.valueOf(subSampleList.get(subSampleList.size() - 1).get("ntransactionsamplecode"));

							inputMap.put("ntransactionsamplecode", stransactionsampleno);
							returnMap.putAll(
									getActiveSubSampleTabData(stransactionsampleno, activeSubSampleTab, userInfo));
							returnMap.put("activeSubSampleTab", inputMap.get("activeSubSampleTab"));
						}
						returnMap.putAll((Map<String, Object>) getApprovalTest(inputMap, userInfo).getBody());
					}
				} else {
					stransactionsampleno = subSampleList.stream()
							.map(x -> String.valueOf(x.get("ntransactionsamplecode"))).collect(Collectors.joining(","));
					inputMap.put("ntransactionsamplecode", stransactionsampleno);
					returnMap.put("AP_SUBSAMPLE", subSampleList);
					returnMap.put("APSelectedSubSample", subSampleList);
					returnMap.putAll((Map<String, Object>) getApprovalTest(inputMap, userInfo).getBody());

				}

			} else {
				returnMap.put("AP_SAMPLE", subSampleList);
				returnMap.put("AP_SUBSAMPLE", subSampleList);
				returnMap.put("AP_TEST", subSampleList);
			}
			System.out.println("Sub Sample End=>" + Instant.now());
			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTSAMPLEOFSAMEAPPROVALVERSION",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}

	}

	@Override
	public ResponseEntity<Object> getApprovalTest(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {// --
		System.out.println("Test start=>" + Instant.now());
		final boolean subSample = (boolean) inputMap.get("nneedsubsample");
		final int approvalVersionCode = validateApprovalVersion((String) inputMap.get("npreregno"), userInfo);
		
		final int nbatchmastercode = inputMap.containsKey("nbatchmastercode") ? (int) inputMap.get("nbatchmastercode")
				: Enumeration.TransactionStatus.NA.gettransactionstatus() ;

		if (((String) inputMap.get("ntransactionstatus"))
				.equals(String.valueOf(Enumeration.TransactionStatus.ALL.gettransactionstatus()))) {
			final String getTransStatus = "select distinct afd.ntransactionstatus from approvalrolefilterdetail afd,approvalconfigrole acr "
					+ " where afd.napprovalconfigrolecode=acr.napprovalconfigrolecode " + " and acr.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and afd.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and afd.ntransactionstatus!= "
					+ Enumeration.TransactionStatus.ALL.gettransactionstatus() + " and afd.nsitecode="
					+ userInfo.getNmastersitecode() + " " + " and acr.napproveconfversioncode in(" + approvalVersionCode
					+ ") and acr.nuserrolecode=" + userInfo.getNuserrole();
			List<String> tranStatusList = jdbcTemplate.queryForList(getTransStatus, String.class);
			inputMap.put("ntransactionstatus", tranStatusList.stream().collect(Collectors.joining(",")));
		}
		Map<String, Object> returnMap = new HashMap<String, Object>();
		System.out.println("Test query start=>" + Instant.now());

		boolean updateAnalyst = false;
		final String query = "select ssettingvalue from settings where nsettingcode ="
				+ Enumeration.Settings.UPDATING_ANALYSER.getNsettingcode() + " ";

		final Settings objSettings = (Settings) jdbcUtilityFunction.queryForObject(query, Settings.class, jdbcTemplate);

		if (objSettings != null) {
			updateAnalyst = Integer.valueOf(objSettings.getSsettingvalue()) == Enumeration.TransactionStatus.YES
					.gettransactionstatus() ? true : false;
		}

		String getTestQuery = "SELECT * from fn_approvaltestget(" + inputMap.get("nregtypecode") + " , "
				+ inputMap.get("nregsubtypecode") + ", '" + inputMap.get("ntransactionsamplecode") + "','"
				+ inputMap.get("ntransactiontestcode") + "'," + " '" + inputMap.get("ntransactionstatus") + "',"
				+ inputMap.get("napprovalversioncode") + ", " + userInfo.getNusercode() + "," + userInfo.getNuserrole()
				+ "," + " '" + inputMap.get("nsectioncode") + "'," + inputMap.get("ntestcode") + ","
				+ userInfo.getNtranssitecode() + ",'" + userInfo.getSlanguagetypecode() + "'," + nbatchmastercode + ","
				+ updateAnalyst + ");";

		LOGGER.info(getTestQuery);
		String testListString = jdbcTemplate.queryForObject(getTestQuery, String.class);

		System.out.println("Test query end=>" + Instant.now());
		List<Map<String, Object>> testList = new ArrayList<>();
		if (testListString != null) {
			testList = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(testListString, userInfo, true,
					(int) inputMap.get("ndesigntemplatemappingcode"), "test");
			returnMap.put("AP_TEST", testList);
			String ntransactiontestcode = "";
			if (inputMap.containsKey("ntype") && (int) inputMap.get("ntype") == 2) {
				// ALPD-1413
				// if (inputMap.containsKey("enforcestatus")) {

				// int transactiontestcode=(int)inputMap.get("ntransactiontestcode");
				// ntransactiontestcode=String.valueOf(transactiontestcode);
				// Map<String,Object> APSelectedTest = testList.stream().filter(x ->
				// (int)x.get("ntransactiontestcode") == transactiontestcode).findAny().get();
				// returnMap.put("APSelectedTest", Arrays.asList(APSelectedTest));

				// }else {

				returnMap.put("APSelectedTest", Arrays.asList(testList.get(testList.size() - 1)));
				ntransactiontestcode = String.valueOf(testList.get(testList.size() - 1).get("ntransactiontestcode"));
				// }

				inputMap.put("ntransactiontestcode", ntransactiontestcode);

				if (subSample) {
					if ((int) inputMap.get("checkBoxOperation") == 7) {
						String stransactiontestcode = "";
						List<String> subSampleArray = Arrays
								.asList(((String) inputMap.get("ntransactionsamplecode")).split(","));

						testList.sort(Comparator.comparing(m -> (int) (m).get("ntransactionsamplecode")));
						subSampleArray.sort(Comparator.comparing(m -> m));
						for (Map<String, Object> map : testList) {
							if ((int) map.get("ntransactionsamplecode") == Integer.parseInt(subSampleArray.get(0))) {
								stransactiontestcode = map.get("ntransactiontestcode").toString();
							}
						}
						inputMap.put("ntransactiontestcode", stransactiontestcode);
					}

					returnMap.putAll(getActiveSubSampleTabData((String) inputMap.get("ntransactionsamplecode"),
							(String) inputMap.get("activeSubSampleTab"), userInfo));
					returnMap.put("activeSubSampleTab", inputMap.get("activeSubSampleTab"));
				}
			}

			returnMap.put("activeSampleKey", "IDS_SAMPLEATTACHMENTS");
			returnMap.put("activeTestKey", "IDS_PARAMETERRESULTS");
			returnMap.put("activeTestKey", "IDS_SUBSAMPLEATTACHMENTS");

			returnMap.putAll(getActiveSampleTabData((String) inputMap.get("npreregno"),
					(String) inputMap.get("ntransactionsamplecode"),
					(inputMap.get("activeTestTab") != null) ? (String) inputMap.get("activeTestTab")
							: "IDS_SAMPLEATTACHMENTS",
					userInfo));

			// ALPD-5267 To open result tab click enforce status button & file comment then
			// save after that all test not displayed.
			returnMap.putAll(getActiveTestTabData((String) inputMap.get("selectedTestCode"),
					(String) inputMap.get("npreregno"),
					(inputMap.get("activeTestTab") != null) ? (String) inputMap.get("activeTestTab") : "IDS_RESULTS",
					userInfo));

		} else {
			returnMap.put("APSelectedTest", testList);
			returnMap.put("activeSampleKey", "IDS_SAMPLEATTACHMENTS");
			returnMap.put("activeTestKey", "IDS_PARAMETERRESULTS");
		}

		System.out.println("Test End=>" + Instant.now());
		return new ResponseEntity<>(returnMap, HttpStatus.OK);

	}

	
	private Map<String, Object> getActiveSampleTabData(final String npreregno, final String ntransactionSampleCode,
			final String activeSampleTab, final UserInfo userInfo) throws Exception {
		switch (activeSampleTab) {
		case "IDS_SAMPLEATTACHMENTS":
			return (Map<String, Object>) attachmentDAO.getSampleAttachment(npreregno, userInfo).getBody();

		case "IDS_SAMPLECOMMENTS":
			return (Map<String, Object>) commentDAO.getSampleComment(npreregno, userInfo).getBody();

		case "IDS_SUBSAMPLEATTACHMENTS":
			return (Map<String, Object>) attachmentDAO.getSubSampleAttachment(ntransactionSampleCode, userInfo)
					.getBody();

		case "IDS_SUBSAMPLECOMMENTS":
			return (Map<String, Object>) commentDAO.getSubSampleComment(ntransactionSampleCode, userInfo).getBody();

		case "IDS_SAMPLEAPPROVALHISTORY":
			return (Map<String, Object>) getSampleApprovalHistory(npreregno, userInfo).getBody();

		case "IDS_REPORTHISTORY":
			return (Map<String, Object>) getCOAHistory(npreregno, userInfo).getBody();

		default:
			return (Map<String, Object>) attachmentDAO.getSampleAttachment(npreregno, userInfo).getBody();
		}
	}

	
	private Map<String, Object> getActiveSubSampleTabData(final String ntransactionSampleCode,
			final String activeSampleTab, final UserInfo userInfo) throws Exception {

		switch (activeSampleTab) {
		case "IDS_SUBSAMPLEATTACHMENTS":
			return (Map<String, Object>) attachmentDAO.getSubSampleAttachment(ntransactionSampleCode, userInfo)
					.getBody();
		case "IDS_SUBSAMPLECOMMENTS":
			return (Map<String, Object>) commentDAO.getSubSampleComment(ntransactionSampleCode, userInfo).getBody();
		default:
			return (Map<String, Object>) commentDAO.getSubSampleComment(ntransactionSampleCode, userInfo).getBody();

		}
	}

	private Map<String, Object> getActiveTestTabData(final String ntransactionTestCode, final String npreregno,
			final String activeTabName, final UserInfo userInfo) throws Exception {

		switch (activeTabName) {

		case "IDS_RESULTS":
			return (Map<String, Object>) getApprovalParameter(ntransactionTestCode, userInfo).getBody();

		case "IDS_INSTRUMENT":
			// return (Map<String, Object>)
			// objResultEntryDAO.getResultUsedInstrument(ntransactionTestCode, 0, userInfo)
			// .getBody();

		case "IDS_MATERIAL":
			return (Map<String, Object>) resultEntryDAO.getResultUsedMaterial(ntransactionTestCode, 0, userInfo)
					.getBody();
			
		case "IDS_TASK":

			return (Map<String, Object>) resultEntryDAO.getResultUsedTask(ntransactionTestCode, 0, userInfo).getBody();

		case "IDS_TESTATTACHMENTS":

			return (Map<String, Object>) attachmentDAO.getTestAttachment(ntransactionTestCode, userInfo).getBody();
		case "IDS_TESTCOMMENTS":

			return (Map<String, Object>) commentDAO.getTestComment(ntransactionTestCode, userInfo).getBody();
		case "IDS_TESTHISTORY":

			return (Map<String, Object>) historyDAO.getTestHistory(ntransactionTestCode, userInfo).getBody();
		case "IDS_DOCUMENTS":

			return null;

		case "IDS_RESULTCHANGEHISTORY":
			return (Map<String, Object>) getApprovalResultChangeHistory(ntransactionTestCode, userInfo).getBody();

		case "IDS_TESTAPPROVALHISTORY":
			return (Map<String, Object>) getApprovalHistory(ntransactionTestCode, userInfo).getBody();

		case "IDS_SAMPLEATTACHMENTS":
			return (Map<String, Object>) attachmentDAO.getSampleAttachment(npreregno, userInfo).getBody();

		default:
			return (Map<String, Object>) getApprovalParameter(ntransactionTestCode, userInfo).getBody();
		}
	}

	public List<ApprovalRoleValidationDetail> getValidationStatus(final int versionCode, final UserInfo userInfo)
			throws Exception {// --

		final ObjectMapper objMapper = new ObjectMapper();

		final String regTypeQuery = "select av.ntransactionstatus,max(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "')::character varying(50) svalidationstatus"
				+ " from approvalrolevalidationdetail av,approvalconfig ac,transactionstatus ts,approvalconfigrole acr,approvalconfigversion acv "
				+ " where av.napprovalconfigcode = ac.napprovalconfigcode "
				+ "	and av.ntransactionstatus = ts.ntranscode and ac.napprovalsubtypecode = "
				+ Enumeration.ApprovalSubType.BATCHAPPROVAL.getnsubtype()
				+ " and acv.napproveconfversioncode=acr.napproveconfversioncode "
				+ " and av.napprovalconfigrolecode=acr.napprovalconfigrolecode and acv.ntransactionstatus in ("
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + ") and acr.nuserrolecode ="
				+ userInfo.getNuserrole() + " and av.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acr.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ac.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acv.napproveconfversioncode="
				+ versionCode + "" + " and av.nsitecode=" + userInfo.getNmastersitecode() + "  and ac.nsitecode="
				+ userInfo.getNmastersitecode() + " " + " and acr.nsitecode=" + userInfo.getNmastersitecode() + " "
				+ " and acv.nsitecode=" + userInfo.getNmastersitecode() + " " + " group by av.ntransactionstatus";

		List<ApprovalRoleValidationDetail> validationStatus = (List<ApprovalRoleValidationDetail>) jdbcTemplate
				.query(regTypeQuery, new ApprovalRoleValidationDetail());

		validationStatus = objMapper
				.convertValue(
						commonFunction.getMultilingualMessageList(validationStatus, Arrays.asList("svalidationstatus"),
								userInfo.getSlanguagefilename()),
						new TypeReference<List<ApprovalRoleValidationDetail>>() {
						});

		return validationStatus;
	}

	@Override
	public ResponseEntity<? extends Object> validateApprovalActions(Map<String, Object> inputMap, final UserInfo userInfo,
			final boolean doAction) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		String npreregno = (String) inputMap.get("npreregno");
		String ntransactionTestCode = (String) inputMap.get("ntransactiontestcode");
		String nsectionCode = (String) inputMap.get("nsectioncode");
		int ntransStatus = (int) inputMap.get("nTransStatus");
		String napproveconfversioncode = (String) String.valueOf(inputMap.get("napprovalversioncode"));

		boolean validTest = false;

		final ApprovalConfigVersion objApprovalConfig = checkUserAvailablity(napproveconfversioncode, userInfo);

		if (objApprovalConfig == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_USERNOTINMAPPING", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			// to check the samples are not released, cancelled and rejected

			String sQuery = "select npreregno,ntransactionstatus from registrationhistory h where h.npreregno in ("
					+ npreregno + ") and h.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and h.nsitecode=" + userInfo.getNtranssitecode()
					+ " and nreghistorycode = any (select max(nreghistorycode) from registrationhistory h1 where h1.npreregno in ("
					+ npreregno + ") and h1.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and h1.nsitecode=" + userInfo.getNtranssitecode()
					+ " group by h1.npreregno) and h.ntransactionstatus not in("
					+ Enumeration.TransactionStatus.RELEASED.gettransactionstatus() + ","
					+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","
					+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ")" + " and h.nsitecode="
					+ userInfo.getNtranssitecode() + " ";

			List<RegistrationHistory> releasedSampleList = (List<RegistrationHistory>) jdbcTemplate.query(sQuery,
					new RegistrationHistory());
			npreregno = "";
			npreregno = stringUtilityFunction.fnDynamicListToString(releasedSampleList, "getNpreregno");

			if (npreregno.length() <= 0) {

				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SAMPLEALREADYRELEASED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);

			} else {

				final String transTestQuery = "select * from registrationtest where ntransactiontestcode in ("
						+ ntransactionTestCode + ") " + " and  nsitecode=" + userInfo.getNtranssitecode()
						+ " and npreregno in (" + npreregno + ")";

				List<RegistrationTest> transTestList = (List<RegistrationTest>) jdbcTemplate.query(transTestQuery,
						new RegistrationTest());

				int napprovalConfigVersionCode = validateApprovalVersion(npreregno, userInfo);

				if (napprovalConfigVersionCode == -1) {

					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_SELECTSAMPLEOFSAMEAPPROVALVERSION",
									userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);

				}

				inputMap.put("napprovalversioncode", String.valueOf(napprovalConfigVersionCode));

				if (napprovalConfigVersionCode > 0) {

					// To check its approval configuration has autoapproval
					final String approvalConfigQuery = "select ac.napprovalconfigcode,acaa.nautoapprovalcode,acaa.nautoallot,acaa.nneedjoballocation,"
							+ " acv.napproveconfversioncode,acaa.nneedautoapproval,acaa.nneedautocomplete,ts.jsondata->'salertdisplaystatus'->>'"
							+ userInfo.getSlanguagetypecode() + "' as sautoapprovalstatus"
							+ " from approvalconfig ac,approvalconfigversion acv,approvalconfigautoapproval acaa,transactionstatus ts"
							+ " where ac.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and acv.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and acaa.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and ts.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and ac.napprovalconfigcode=acv.napprovalconfigcode and acv.nsitecode="
							+ userInfo.getNmastersitecode() + " and ac.nsitecode=" + userInfo.getNmastersitecode()
							+ "  and acaa.nsitecode=" + userInfo.getNmastersitecode() + " "
							+ " and acv.napproveconfversioncode=acaa.napprovalconfigversioncode"
							+ " and ts.ntranscode=acaa.nautoapprovalstatus and acv.napproveconfversioncode="
							+ napprovalConfigVersionCode;

					final ApprovalConfigAutoapproval objApprovalConfigAutoapproval = jdbcTemplate
							.queryForObject(approvalConfigQuery, new ApprovalConfigAutoapproval());

					if (objApprovalConfigAutoapproval.getNneedautoapproval() == Enumeration.TransactionStatus.YES
							.gettransactionstatus()) {

						final String alertMessage = commonFunction.getMultilingualMessage("IDS_TESTALREADY",
								userInfo.getSlanguagefilename())
								+ " "
								+ commonFunction.getMultilingualMessage(
										objApprovalConfigAutoapproval.getSautoapprovalstatus(),
										userInfo.getSlanguagefilename());

						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage(alertMessage, userInfo.getSlanguagefilename()),
								HttpStatus.EXPECTATION_FAILED);

					} else {

						// need to check the role wise auto approval here

						final String roleValidationDetailGet = "select ts.jsondata->'stransdisplaystatus'->>'"
								+ userInfo.getSlanguagetypecode()
								+ "' as stransstatus,vd.napprovalconfigrolecode,vd.napprovalconfigcode,vd.ntransactionstatus,acr.nuserrolecode"
								+ " from approvalconfigversion acv,approvalconfigrole acr,approvalrolevalidationdetail vd,transactionstatus ts"
								+ " where acv.napproveconfversioncode=acr.napproveconfversioncode"
								+ " and acr.napprovalconfigrolecode=vd.napprovalconfigrolecode"
								+ " and ts.ntranscode=vd.ntransactionstatus" + " and acv.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acr.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and vd.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and acr.nuserrolecode=" + userInfo.getNuserrole()
								+ " and acv.napproveconfversioncode=" + napprovalConfigVersionCode + ""
								+ " and acv.nsitecode=" + userInfo.getNmastersitecode() + " " + " and acr.nsitecode="
								+ userInfo.getNmastersitecode() + "  and vd.nsitecode="
								+ userInfo.getNmastersitecode() + " ";

						final List<ApprovalRoleValidationDetail> validationDetails = (List<ApprovalRoleValidationDetail>) jdbcTemplate
								.query(roleValidationDetailGet, new ApprovalRoleValidationDetail());

						if (validationDetails.size() > 0) {

							final String validationStatusCodes = stringUtilityFunction
									.fnDynamicListToString(validationDetails, "getNtransactionstatus");
							final String transTestCodes = stringUtilityFunction.fnDynamicListToString(transTestList,
									"getNtransactiontestcode");

							final String actionPerformedTestGet = "select rth.ntransactiontestcode,max(rth.ntransactionstatus) ntransactionstatus,max(ts.jsondata->'stransdisplaystatus'->>'"
									+ userInfo.getSlanguagetypecode() + "') stransdisplaystatus"
									+ " from registrationtesthistory rth,transactionstatus ts"
									+ " where rth.ntesthistorycode = any ("
									+ "  select max(rth1.ntesthistorycode) as ntesthistorycode"
									+ "  from registrationtesthistory rth1 where rth1.nstatus ="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and  rth1.nsitecode=" + userInfo.getNtranssitecode()
									+ "  and rth1.ntransactiontestcode in (" + transTestCodes + ")"
									+ "  group by rth1.npreregno,rth1.ntransactionsamplecode,rth1.ntransactiontestcode"
									+ " ) and rth.nstatus ="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and  rth.nsitecode=" + userInfo.getNtranssitecode() + " and rth.nstatus ="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and ts.ntranscode = rth.ntransactionstatus and rth.ntransactionstatus not in ("
									+ validationStatusCodes + ") group by rth.npreregno,rth.ntransactiontestcode";

							final List<RegistrationTestHistory> actionPerformedTests = (List<RegistrationTestHistory>) jdbcTemplate
									.query(actionPerformedTestGet, new RegistrationTestHistory());

							final List<RegistrationTestHistory> filteredActionPerformedTests = actionPerformedTests
									.stream()
									.filter(test -> validationDetails.stream()
											.anyMatch(vd -> test.getNtransactionstatus() == ntransStatus))
									.collect(Collectors.toList());

							 List<RegistrationTest> filteredValidTests = transTestList.stream()
									.filter(test -> actionPerformedTests.stream().noneMatch(
											nvt -> test.getNtransactiontestcode() == nvt.getNtransactiontestcode()))
									.collect(Collectors.toList());

							if (actionPerformedTests.size() > 0 && filteredValidTests.size() == 0) {

								if (filteredActionPerformedTests.size() == actionPerformedTests.size()) {

									final String alertMessage = commonFunction.getMultilingualMessage("IDS_TESTALREADY",
											userInfo.getSlanguagefilename())
											+ " "
											+ commonFunction.getMultilingualMessage(
													actionPerformedTests.get(0).getStransdisplaystatus(),
													userInfo.getSlanguagefilename());

									return new ResponseEntity<>(commonFunction.getMultilingualMessage(alertMessage,
											userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);

								} else {

									final String strQuery = "select jsondata->'salertdisplaystatus'->>'"
											+ userInfo.getSlanguagetypecode()
											+ "' salertdisplaystatus from transactionstatus where nstatus="
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " and ntranscode in(" + validationStatusCodes + ")";

									 List<TransactionStatus> validationStatusList = (List<TransactionStatus>) jdbcTemplate
											.query(strQuery, new TransactionStatus());

									validationStatusList = objMapper.convertValue(
											commonFunction.getMultilingualMessageList(validationStatusList,
													Arrays.asList("salertdisplaystatus"),
													userInfo.getSlanguagefilename()),
											new TypeReference<List<TransactionStatus>>() {
											});

									final String valditaionStatus = String.join("/", validationStatusList.stream()
											.map(x -> x.getSalertdisplaystatus()).collect(Collectors.toList()));

									final String alertMessage = commonFunction.getMultilingualMessage("IDS_SELECT",
											userInfo.getSlanguagefilename()) + " [" + valditaionStatus + "] "
											+ commonFunction.getMultilingualMessage("IDS_TESTONLY",
													userInfo.getSlanguagefilename());

									return new ResponseEntity<>(commonFunction.getMultilingualMessage(alertMessage,
											userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);

								}
							} else {

								final String actionPerformedTestBySampleRoleGet = "select rth.ntransactiontestcode,max(rth.ntransactionstatus) ntransactionstatus,max(ts.jsondata->'stransdisplaystatus'->>'"
										+ userInfo.getSlanguagetypecode() + "') stransdisplaystatus"
										+ " from registrationtesthistory rth,transactionstatus ts"
										+ " where rth.ntesthistorycode = any ("
										+ "  select max(rth1.ntesthistorycode) as ntesthistorycode"
										+ "  from registrationtesthistory rth1 where rth1.nstatus ="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and  rth1.nsitecode=" + userInfo.getNtranssitecode()
										+ "  and rth1.ntransactiontestcode in (" + transTestCodes + ")"
										+ "  group by rth1.npreregno,rth1.ntransactionsamplecode,rth1.ntransactiontestcode"
										+ " ) and rth.nstatus ="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and  rth.nsitecode=" + userInfo.getNtranssitecode() + " and rth.nstatus ="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and ts.ntranscode = rth.ntransactionstatus and rth.ntransactionstatus ="
										+ ntransStatus + " and rth.nuserrolecode=" + userInfo.getNuserrole()
										+ " group by rth.npreregno,rth.ntransactiontestcode";

								List<RegistrationTestHistory> actionPerformedTestsBySameRole = (List<RegistrationTestHistory>) jdbcTemplate
										.query(actionPerformedTestBySampleRoleGet, new RegistrationTestHistory());

								filteredValidTests = filteredValidTests.stream()
										.filter(test -> actionPerformedTestsBySameRole.stream().noneMatch(
												nvt -> test.getNtransactiontestcode() == nvt.getNtransactiontestcode()))
										.collect(Collectors.toList());
								if (actionPerformedTestsBySameRole.size() > 0 && filteredValidTests.size() == 0) {
									final String alertMessage = commonFunction.getMultilingualMessage("IDS_TESTALREADY",
											userInfo.getSlanguagefilename())
											+ " "
											+ commonFunction.getMultilingualMessage(
													actionPerformedTestsBySameRole.get(0).getStransdisplaystatus(),
													userInfo.getSlanguagefilename());

									return new ResponseEntity<>(commonFunction.getMultilingualMessage(alertMessage,
											userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
								} else {
									npreregno = stringUtilityFunction.fnDynamicListToString(filteredValidTests,
											"getNpreregno");
									ntransactionTestCode = stringUtilityFunction
											.fnDynamicListToString(filteredValidTests, "getNtransactiontestcode");

									inputMap.put("npreregno", npreregno);
									inputMap.put("ntransactiontestcode", ntransactionTestCode);

									final String approvalConfigRoleGet = "select * from approvalconfigrole acr where  acr.nstatus="
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " and acr.napproveconfversioncode=" + napprovalConfigVersionCode
											+ " and acr.nuserrolecode=" + userInfo.getNuserrole()
											+ " and acr.nsitecode=" + userInfo.getNmastersitecode() + "";

									final ApprovalConfigRole objApprovalConfigRole = (ApprovalConfigRole) jdbcUtilityFunction
											.queryForObject(approvalConfigRoleGet, ApprovalConfigRole.class,
													jdbcTemplate);

									inputMap.put("nlevelno", objApprovalConfigRole.getNlevelno());

									if (objApprovalConfigRole != null) {

										if (objApprovalConfigRole
												.getNpartialapprovalneed() == Enumeration.TransactionStatus.YES
														.gettransactionstatus()) {

											validTest = true;

										} else {
											if (ntransStatus == Enumeration.TransactionStatus.RECOM_RETEST
													.gettransactionstatus()
													|| ntransStatus == Enumeration.TransactionStatus.RECOM_RECALC
															.gettransactionstatus()
													|| ntransStatus == Enumeration.TransactionStatus.RETEST
															.gettransactionstatus()
													|| ntransStatus == Enumeration.TransactionStatus.RECALC
															.gettransactionstatus()) {

												validTest = true;

											} else {
												String sectionCondition = "";
												if (objApprovalConfigRole
														.getNsectionwiseapprovalneed() == Enumeration.TransactionStatus.YES
																.gettransactionstatus()) {
													sectionCondition = "and rt.nsectioncode in (" + nsectionCode + ")";
												}
												final String validateTestCountQuery = "select rt.* from registrationtesthistory rth,registrationtest rt"
														+ " where rt.ntransactiontestcode = rth.ntransactiontestcode  "
														+ " and rth.ntesthistorycode = any  ("
														+ "		select max(rth1.ntesthistorycode) as testhistorycode"
														+ " 	from registrationtesthistory rth1  "
														+ "		where rth1.npreregno in (" + npreregno + ")"
														+ " 	and rth1.nstatus="
														+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
														+ " and  rth1.nsitecode=" + userInfo.getNtranssitecode()
														+ "		group by rth1.npreregno,rth1.ntransactionsamplecode,rth1.ntransactiontestcode)"
														+ sectionCondition + " and  rth.nsitecode=rt.nsitecode"
														+ " and  rth.nsitecode=" + userInfo.getNtranssitecode()
														+ " and rth .ntransactionstatus not in ("
														+ Enumeration.TransactionStatus.RETEST.gettransactionstatus()
														+ ","
														+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus()
														+ ","
														+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus()
														+ ")" + " and rt.nstatus ="
														+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
														+ " and rth.nstatus ="
														+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

												final List<RegistrationTest> transTestList2 = (List<RegistrationTest>) jdbcTemplate
														.query(validateTestCountQuery, new RegistrationTest());

												if (transTestList2.size() > 0) {
													if (transTestList2.size() == transTestList.size()) {

														validTest = true;

													} else {

														String stransStatus = jdbcTemplate.queryForObject(
																"select jsondata->'salertdisplaystatus'->>'"
																		+ userInfo.getSlanguagetypecode()
																		+ "' from transactionstatus where ntranscode ="
																		+ ntransStatus + " and nstatus="
																		+ Enumeration.TransactionStatus.ACTIVE
																				.gettransactionstatus(),
																String.class);
														stransStatus = commonFunction.getMultilingualMessage(
																stransStatus, userInfo.getSlanguagefilename());
														String msg = "IDS_FULLYAPPROVE";
														msg = commonFunction.getMultilingualMessage(msg,
																userInfo.getSlanguagefilename());

														return new ResponseEntity<>(
																commonFunction.getMultilingualMessage(
																		msg + " [" + stransStatus + "] ",
																		userInfo.getSlanguagefilename()),
																HttpStatus.EXPECTATION_FAILED);
													}
												}
											}
										}
									} else {

										return new ResponseEntity<>(
												commonFunction.getMultilingualMessage("IDS_CHECKAPPROVALCONFIGURATION",
														userInfo.getSlanguagefilename()),
												HttpStatus.EXPECTATION_FAILED);

									}
								}
							}
						} else {

							return new ResponseEntity<>(
									commonFunction.getMultilingualMessage("IDS_VALIDATIONSTATUSISEMPTY",
											userInfo.getSlanguagefilename()),
									HttpStatus.EXPECTATION_FAILED);

						}
					}
				}
				final String getDecisionStatus = "select ad.* from approvalroledecisiondetail ad,approvalconfigrole ar  "
						+ " where ar.nuserrolecode = " + userInfo.getNuserrole() + " and ar.napproveconfversioncode="
						+ napprovalConfigVersionCode + " and ad.napprovalconfigrolecode=ar.napprovalconfigrolecode "
						+ " and ad.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ar.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ad.nsitecode=" + userInfo.getNmastersitecode() + " " + " and ar.nsitecode="
						+ userInfo.getNmastersitecode() + " ";

				final List<ApprovalRoleDecisionDetail> decisionStatusList = (List<ApprovalRoleDecisionDetail>) jdbcTemplate
						.query(getDecisionStatus, new ApprovalRoleDecisionDetail());

				if (decisionStatusList.size() > 0) {
					final String validateDecisionStatus = "select npreregno from registrationdecisionhistory"
							+ " where nregdecisionhistorycode= any("
							+ " select max(nregdecisionhistorycode) nregdecisionhistorycode from registrationdecisionhistory"
							+ " where npreregno in (" + npreregno + ") and nstatus=1" + " and   nsitecode="
							+ userInfo.getNtranssitecode() + " group by npreregno)" + " and nsitecode="
							+ userInfo.getNtranssitecode() + " and ndecisionstatus ="
							+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus();

					final List<Integer> notDecidedSampleList = jdbcTemplate.queryForList(validateDecisionStatus,
							Integer.class);

					if (notDecidedSampleList.size() > 0) {
						validTest = false;
						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage("IDS_SELECTDECISIONSTATUSFORALLSELECTSAMPLES",
										userInfo.getSlanguagefilename()),
								HttpStatus.EXPECTATION_FAILED);
					}
				}
				if (validTest) {
					if (doAction) {
						return doAction(inputMap, userInfo);
					} else {
						// getSequence
						return getActionSequenceNo(inputMap, userInfo);
					}
				} else {
					return null;
				}
			}
		}
	}

	
	private ResponseEntity<Object> doAction(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {

		final int ntransStatus = (int) inputMap.get("nTransStatus");

		final int nseqnoApprovalHistory = (int) inputMap.get("ApprovalHistorySequence");
		final int nseqnoTestHistory = (int) inputMap.get("TestHistorySequence");
		final int nseqnoCommentHistory = (int) inputMap.get("CommentHistorySequence");

		final List<RegistrationTest> transTestList = (List<RegistrationTest>) inputMap.get("TransTestList");

		final List<ResultParameterComments> commentList = (List<ResultParameterComments>) inputMap.get("CommentList");

		if (ntransStatus == Enumeration.TransactionStatus.RECALC.gettransactionstatus() && transTestList.size() > 0) {
			// RECALC
			/**
			 * Recalc - insert in Sample approval history and registration test history
			 * table as test status recalc and insert into result change history and update
			 * result parameter results to null
			 **/

			final int resultChangeSeqNo = (int) inputMap.get("ResultChangeSequence");

			return doReCalc(inputMap, userInfo, nseqnoApprovalHistory, nseqnoTestHistory, nseqnoCommentHistory,
					commentList, resultChangeSeqNo);

		} else if (ntransStatus == Enumeration.TransactionStatus.RETEST.gettransactionstatus()) {
			// RETEST

			return doReTest(inputMap, userInfo, transTestList);
		} else {
			// Other Dynamic Actions(Approve/Review/...)
			return doDynamicApprovalAction(inputMap, userInfo, nseqnoApprovalHistory, nseqnoTestHistory);
		}
	}

	public ResponseEntity<Object> getActionSequenceNo(Map<String, Object> inputMap,final UserInfo userInfo)
			throws Exception {

		final String lockcancelreject = " lock  table lockcancelreject "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus() + "";
		jdbcTemplate.execute(lockcancelreject);

		final String lockapprovaldecision = " lock  table lockapprovaldecision "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus() + "";
		jdbcTemplate.execute(lockapprovaldecision);

		final String npreregno = (String) inputMap.get("npreregno");
		final String ntransactionsamplecode = (String) inputMap.get("ntransactionsamplecode");
		final String ntransactionTestCode = (String) inputMap.get("ntransactiontestcode");
		final int ntransStatus = (int) inputMap.get("nTransStatus");
		final String napproveconfversioncode = (String) String.valueOf(inputMap.get("napprovalversioncode"));

		int nseqnoApprovalHistory = 0;
		int nseqnoTestHistory = 0;
		int nseqnoCommentHistory = 0;
		int nseqnoRegHistory = 0;
		int nseqnoSampleHistory = 0;

		ApprovalConfigVersion objApprovalConfig = checkUserAvailablity(napproveconfversioncode, userInfo);

		if (objApprovalConfig == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_USERNOTINMAPPING", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (!inputMap.containsKey("napprovalconfigcode")) {
				final String getApprovalConfig = "select napprovalconfigcode from approvalconfig where nregtypecode="
						+ inputMap.get("nregtypecode") + " and nregsubtypecode=" + inputMap.get("nregsubtypecode")
						+ " and  nsitecode=" + userInfo.getNmastersitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

				inputMap.put("napprovalconfigcode", jdbcTemplate.queryForObject(getApprovalConfig, Integer.class));
			}

			final String resultParamValidate = " select  case when (" + " select count(*) from resultparameter "
					+ " where ntransactiontestcode in (" + ntransactionTestCode + ") "
					+ " and ntransactionstatus not in (" + Enumeration.TransactionStatus.REJECTED.gettransactionstatus()
					+ ")" + " and nsitecode = " + userInfo.getNtranssitecode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") = "
					+ " ( select count(*) from approvalparameter" + " where ntransactiontestcode in ("
					+ ntransactionTestCode + " )" + " and nsitecode = " + userInfo.getNtranssitecode()
					+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")"
					+ " then 'true' else 'false' end as validateparamcount";

			boolean validateParamCount = jdbcTemplate.queryForObject(resultParamValidate, Boolean.class);

			if (validateParamCount) {


				final String strSelectSeqno = "select stablename,nsequenceno from seqnoregistration "
						+ " where stablename in ('sampleapprovalhistory','registrationtesthistory', 'resultparamcommenthistory','registrationhistory','registrationsamplehistory') "
						+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";

				final List<SeqNoRegistration> mapList = (List<SeqNoRegistration>) jdbcTemplate.query(strSelectSeqno,
						new SeqNoRegistration());

				final Map<String, Integer> mapSeqno = mapList.stream().collect(Collectors.toMap(
						SeqNoRegistration::getStablename, SeqNoRegistration -> SeqNoRegistration.getNsequenceno()));

				nseqnoApprovalHistory = mapSeqno.get("sampleapprovalhistory");
				nseqnoTestHistory = mapSeqno.get("registrationtesthistory");
				nseqnoCommentHistory = mapSeqno.get("resultparamcommenthistory");
				nseqnoRegHistory = mapSeqno.get("registrationhistory");
				nseqnoSampleHistory = mapSeqno.get("registrationsamplehistory");

				final String testListQry = "select * from registrationtest where ntransactiontestcode in ("
						+ ntransactionTestCode + ") and nsitecode = " + userInfo.getNtranssitecode()
						+ " and npreregno in (" + npreregno + ");";

				final String testListQry1 = "select ntransactionresultcode,ntransactiontestcode,jsondata from approvalparameter where ntransactiontestcode in ("
						+ ntransactionTestCode + ")  and nsitecode = " + userInfo.getNtranssitecode()
						+ " and npreregno in (" + npreregno + ");";

				final String testListQry2 = " select * from resultparametercomments where jsondata->'senforcestatuscomment' is not null and"
						+ " nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsitecode = " + userInfo.getNtranssitecode() + " and ntransactiontestcode in ("
						+ ntransactionTestCode + ")";

				final List<RegistrationTest> sizeGet = (List<RegistrationTest>) jdbcTemplate.query(testListQry,
						new RegistrationTest());
				
				final List<ApprovalParameter> sizeGet2 = (List<ApprovalParameter>) jdbcTemplate.query(testListQry1,
						new ApprovalParameter());
				
				final List<ResultParameterComments> sizeGet3 = (List<ResultParameterComments>) jdbcTemplate
						.query(testListQry2, new ResultParameterComments());

				final List<RegistrationTest> transTestList = (List<RegistrationTest>) sizeGet;
				final List<ApprovalParameter> paramList = (List<ApprovalParameter>) sizeGet2;
				final List<ResultParameterComments> commentList = (List<ResultParameterComments>) sizeGet3;

				Map<String, Object> outputMap = new HashMap<String, Object>();

				String seqNoUpdate = "";

				// to get sequence for sample status update

				final String preRegSet = String.join(",", transTestList.stream()
						.map(item -> String.valueOf(item.getNpreregno())).collect(Collectors.toSet()));
				seqNoUpdate += ";update seqnoregistration set nsequenceno="
						+ (nseqnoRegHistory + preRegSet.split(",").length) + " where stablename='registrationhistory' "
						+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
				final String transactionsample = String.join(",", transTestList.stream()
						.map(item -> String.valueOf(item.getNtransactionsamplecode())).collect(Collectors.toSet()));
				seqNoUpdate += ";update seqnoregistration set nsequenceno="
						+ (nseqnoSampleHistory + transactionsample.split(",").length)
						+ " where stablename='registrationsamplehistory'"
						+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
				// end of sequence for sample status update

				if (ntransStatus == Enumeration.TransactionStatus.RECALC.gettransactionstatus()) {

					if (transTestList.size() > 0) {
 
						final String resultChangeSeqNoGet = "select nsequenceno from seqnoregistration where stablename=N'resultchangehistory' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";

						int resultChangeSeqNo = jdbcTemplate.queryForObject(resultChangeSeqNoGet, Integer.class);
						outputMap.put("ResultChangeSequence", resultChangeSeqNo);

						seqNoUpdate += "update seqnoregistration set nsequenceno = nsequenceno+"
								+ npreregno.split(",").length + " where stablename = 'sampleapprovalhistory'"
								+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"
								+ " update seqnoregistration set nsequenceno = nsequenceno+" + transTestList.size()
								+ " where stablename = 'registrationtesthistory' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
								+ " ;update seqnoregistration set nsequenceno = nsequenceno+" + paramList.size()
								+ "  where stablename=N'resultchangehistory' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";

						if (!commentList.isEmpty()) {
							seqNoUpdate = seqNoUpdate + "; update seqnoregistration set nsequenceno = nsequenceno+"
									+ commentList.size() + "  where stablename=N'resultparamcommenthistory'"
									+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
						}
					}
				} else if (ntransStatus == Enumeration.TransactionStatus.RETEST.gettransactionstatus()) {
					

					final String seqNoRetestQuery = "select stablename,nsequenceno from seqnoregistration where stablename in( 'registrationtest','joballocation','registrationparameter','llinter','registrationsectionhistory')"
							+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
					
					List<SeqNoRegistration> lstseqNumbers = jdbcTemplate.query(seqNoRetestQuery,
							new SeqNoRegistration());

					Map<String, Object> seqMap = lstseqNumbers.stream().collect(Collectors.toMap(
							SeqNoRegistration::getStablename, seqNoRegistration -> seqNoRegistration.getNsequenceno()));

					outputMap.put("JASequence", seqMap.get("joballocation"));
					outputMap.put("LlinterSequence", seqMap.get("llinter"));
					outputMap.put("RegParamSequence", seqMap.get("registrationparameter"));
					outputMap.put("TestSequence", seqMap.get("registrationtest"));
					outputMap.put("SectionSequence", seqMap.get("registrationsectionhistory"));

					final String registerparameter = "select count(ntransactionresultcode) from registrationparameter where ntransactiontestcode in ("
							+ ntransactionTestCode + ")" + " and nsitecode=" + userInfo.getNtranssitecode();

					final int nregparametecount = jdbcTemplate.queryForObject(registerparameter, Integer.class);

					outputMap.put("RegParameterCount", nregparametecount);

					int retestCount = 1;
					if (inputMap.containsKey("retestcount")) {
						retestCount = (int) inputMap.get("retestcount");
					}

					final String llinterParamCountQuery = "select count(tgtp.ntestgrouptestparametercode) "
							+ " from testgrouptestparameter tgtp,testgrouptest tgt,registrationtest rt,testinstrumentcategory tic,instrumentcategory ic"
							+ " where tgt.ntestgrouptestcode=rt.ntestgrouptestcode"
							+ " and tgtp.ntestgrouptestcode = tgt.ntestgrouptestcode"
							+ " and tic.ntestcode=tgt.ntestcode" + " and tic.ninstrumentcatcode=ic.ninstrumentcatcode"
							+ " " + " and rt.nsitecode=" + userInfo.getNtranssitecode() + " and tic.ndefaultstatus = "
							+ Enumeration.TransactionStatus.YES.gettransactionstatus()
							+ " and rt.ntransactiontestcode in (" + ntransactionTestCode + ")" + " and tgtp.nsitecode="
							+ userInfo.getNmastersitecode() + "  and tgt.nsitecode="
							+ userInfo.getNmastersitecode() + "  and tic.nsitecode="
							+ userInfo.getNmastersitecode() + "  and ic.nsitecode=" + userInfo.getNmastersitecode()
							+ " ";

					final int llinterParamCount = jdbcTemplate.queryForObject(llinterParamCountQuery, Integer.class);

					outputMap.put("LlinterParameterCount", llinterParamCount);

					final int seqencenotesthistory = transTestList.size() + (transTestList.size() * retestCount);
					final int ParamSeq = nregparametecount * retestCount;
					final int seqnoTest = transTestList.size() * retestCount;
					final int seqnoJA = transTestList.size() * retestCount;
					final int seqnollinter = llinterParamCount * retestCount;
					final int seqNoSectionHistory = transTestList.size() * retestCount;

					seqNoUpdate += "update seqnoregistration set nsequenceno = nsequenceno+" + seqnoJA
							+ " where stablename = 'joballocation' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"
							+ "update SeqNoRegistration set nsequenceno = nsequenceno+" + ParamSeq
							+ " where stablename = 'registrationparameter' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"
							+ "update SeqNoRegistration set nsequenceno = nsequenceno+" + npreregno.split(",").length
							+ " where stablename = 'sampleapprovalhistory' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"; "
							+ "update SeqNoRegistration set nsequenceno = nsequenceno+" + seqencenotesthistory
							+ " where stablename = 'registrationtesthistory' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"; "
							+ "update SeqNoRegistration set nsequenceno = nsequenceno+" + seqnoTest
							+ " where stablename = 'registrationtest' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"
							+ "update SeqNoRegistration set nsequenceno = nsequenceno+" + seqNoSectionHistory
							+ " where stablename = 'registrationsectionhistory' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"
							+ "update SeqNoRegistration set nsequenceno = nsequenceno+" + seqnollinter
							+ " where stablename = 'llinter' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";

				} else {

					final String sampleString = String.join(",",
							Stream.of(npreregno.trim().split(",")).collect(Collectors.toSet()));
					final String subSampleString = String.join(",",
							Stream.of(ntransactionsamplecode.trim().split(",")).collect(Collectors.toSet()));

					seqNoUpdate += "update seqnoregistration set nsequenceno = nsequenceno+"
							+ sampleString.split(",").length + " where stablename = 'sampleapprovalhistory' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"
							+ " update seqnoregistration set nsequenceno = nsequenceno+" + transTestList.size()
							+ " where stablename = 'registrationtesthistory' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";

					final String roleWiseAutoApproval = "select acr.* "
							+ " from approvalconfigrole acr,approvalconfigversion acv"
							+ " where acv.napproveconfversioncode=acr.napproveconfversioncode"
							+ " and acr.nlevelno < ( "
							+ " 	select nlevelno from approvalconfigrole where napproveconfversioncode="
							+ inputMap.get("napprovalversioncode") + " 	and nuserrolecode=" + userInfo.getNuserrole()
							+ "	and nsitecode=" + userInfo.getNmastersitecode() + ")" + " and acv.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acr.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and acv.napproveconfversioncode=" + inputMap.get("napprovalversioncode")
							+ " and acr.nsitecode=" + userInfo.getNmastersitecode() + " " + " order by nlevelno desc";

					final List<ApprovalConfigRole> roleList = (List<ApprovalConfigRole>) jdbcTemplate
							.query(roleWiseAutoApproval, new ApprovalConfigRole());
					/*** get the top most auto approval level details starts here ***/

					if (!roleList.isEmpty()) {
						boolean approve = false;
						int roleCode = 0;

						for (int i = 0; i < roleList.size(); i++) {
							if (roleList.get(i).getNautoapproval() == Enumeration.TransactionStatus.YES
									.gettransactionstatus()) {
								roleCode = roleList.get(i).getNuserrolecode();
								approve = true;
							} else {
								break;
							}
						}

						if (approve) {

							jdbcTemplate.execute(seqNoUpdate);


							final String roleSelectSeqNo = "select stablename,nsequenceno from seqnoregistration "
									+ " where stablename in ('sampleapprovalhistory','registrationtesthistory', 'registrationhistory','registrationsamplehistory')"
									+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";

							final List<SeqNoRegistration> roleMapList = (List<SeqNoRegistration>) jdbcTemplate
									.query(roleSelectSeqNo, new SeqNoRegistration());

							final Map<String, Integer> roleMapSeqNo = roleMapList.stream()
									.collect(Collectors.toMap(SeqNoRegistration::getStablename,
											SeqNoRegistration -> SeqNoRegistration.getNsequenceno()));

							final int nautoSeqNoApprovalHistory = roleMapSeqNo.get("sampleapprovalhistory");
							final int nautoSeqNoTestHistory = roleMapSeqNo.get("registrationtesthistory");
							final int nautoSeqNoRegHistory = roleMapSeqNo.get("registrationhistory");
							final int nautoSeqNoSampleHistory = roleMapSeqNo.get("registrationsamplehistory");

							outputMap.put("AutoApprovalHistorySequence", nautoSeqNoApprovalHistory);
							outputMap.put("AutoTestHistorySequence", nautoSeqNoTestHistory);
							outputMap.put("AutoRegistrationHistorySequence", nautoSeqNoRegHistory);
							outputMap.put("AutoRegistrationSampleSequence", nautoSeqNoSampleHistory);

							seqNoUpdate = "";
							seqNoUpdate += "update seqnoregistration set nsequenceno = nsequenceno+"
									+ sampleString.split(",").length + " where stablename = 'sampleapprovalhistory' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"
									+ " update seqnoregistration set nsequenceno = nsequenceno+" + transTestList.size()
									+ " where stablename = 'registrationtesthistory' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";

							seqNoUpdate += ";update seqnoregistration set nsequenceno="
									+ (nautoSeqNoRegHistory + sampleString.split(",").length)
									+ " where stablename='registrationhistory' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
							seqNoUpdate += ";update seqnoregistration set nsequenceno="
									+ (nautoSeqNoSampleHistory + subSampleString.split(",").length)
									+ " where stablename='registrationsamplehistory' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";

							final String napprovalConfigVersionCode = (String) inputMap.get("napprovalversioncode");

							final String getDecisionStatus = "select ad.* from approvalroledecisiondetail ad,approvalconfigrole ar  "
									+ " where ar.nuserrolecode = " + roleCode + " and ar.napproveconfversioncode="
									+ napprovalConfigVersionCode
									+ " and ad.napprovalconfigrolecode=ar.napprovalconfigrolecode " + " and ad.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ar.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

							List<ApprovalRoleDecisionDetail> decisionStatusList = (List<ApprovalRoleDecisionDetail>) jdbcTemplate
									.query(getDecisionStatus, new ApprovalRoleDecisionDetail());

							if (decisionStatusList.size() > 0) {

								final String autoDecisionSeqString = "select nsequenceno from seqnoregistration where stablename='registrationdecisionhistory' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";

								final int seqNoDecisionHistory = jdbcTemplate.queryForObject(autoDecisionSeqString,
										Integer.class);

								outputMap.put("AutoDecisionHistorySequence", seqNoDecisionHistory);

								final int sampleCount = sampleString.split(",").length;

								seqNoUpdate += ";update seqnoregistration set nsequenceno="
										+ (seqNoDecisionHistory + sampleCount)
										+ " where stablename='registrationdecisionhistory' "
										+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";

							}
						}
					}
				}

				jdbcTemplate.execute(seqNoUpdate);
				List<ApprovalParameter> lstparam = new ArrayList<ApprovalParameter>();

				final String parameterListQry = "select r.jsondata->'Plant Order'->>'nexternalordercode' as nexternalordercode,ap.ntransactionresultcode,ap.ntransactiontestcode,ap.jsondata,ap.jsondata->>'stestsynonym' stestsynonym,ap.jsondata->>'sparametersynonym' sparametersynonym,rt.npreregno,rt.ntestcode,rt.ntransactionsamplecode "
						+ " from approvalparameter ap,registrationtest rt,registration r where  r.npreregno=rt.npreregno and ap.ntransactiontestcode in ("
						+ ntransactionTestCode + ") " + " and ap.nsitecode = " + userInfo.getNtranssitecode()
						+ " and rt.ntransactiontestcode=ap.ntransactiontestcode" + " and rt.nsitecode = "
						+ userInfo.getNtranssitecode() + " and r.nsitecode = " + userInfo.getNtranssitecode()
						+ " and ap.npreregno in (" + npreregno + ");";

				lstparam = (List<ApprovalParameter>) jdbcTemplate.query(parameterListQry, new ApprovalParameter());

				outputMap.put("ApprovalHistorySequence", nseqnoApprovalHistory);
				outputMap.put("TestHistorySequence", nseqnoTestHistory);
				outputMap.put("CommentHistorySequence", nseqnoCommentHistory);
				outputMap.put("RegistrationHistorySequence", nseqnoRegHistory);
				outputMap.put("RegistrationSampleSequence", nseqnoSampleHistory);
				outputMap.put("inputMap", inputMap);
				outputMap.put("TransTestList", transTestList);
				outputMap.put("ParamList", paramList);
				outputMap.put("ValuesForPortal", lstparam);
				outputMap.put("CommentList", commentList);

				return new ResponseEntity<>(outputMap, HttpStatus.OK);

			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_COMPLETERESULTS", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	// @SuppressWarnings("unchecked")
	// public ResponseEntity<Object> fnTestAction(Map<String,Object>
	// inputMap,UserInfo userInfo) throws Exception
	// {
	// String npreregno = (String)inputMap.get("npreregno");
	// String ntransactionTestCode = (String)inputMap.get("TransactionSampleTests");
	// int ntransStatus = (int)inputMap.get("nTransStatus");
	//
	// int nseqnoApprovalHistory = 0;
	// int nseqnoTestHistory = 0;
	// int nseqnoCommentHistory = 0;
	//
	// if(!inputMap.containsKey("napprovalconfigcode"))
	// {
	// final String getApprovalConfig="select napprovalconfigcode from
	// approvalconfig where nregtypecode="+inputMap.get("nregtypecode")
	// +" and nregsubtypecode="+inputMap.get("nregsubtypecode")
	// +" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	//
	// inputMap.put("napprovalconfigcode",
	// jdbcTemplate.queryForObject(getApprovalConfig, Integer.class));
	// }
	//
	// final String resultParamValidate = " select case when ("
	// + " select count(*) from resultparameter "
	// + " where ntransactiontestcode in ("+ntransactionTestCode+") "
	// + " and ntransactionstatus not in
	// ("+Enumeration.TransactionStatus.REJECTED.gettransactionstatus()+")"
	// + " and nstatus =
	// "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+") = "
	// + " ( select count(*) from approvalparameter"
	// + " where ntransactiontestcode in ("+ntransactionTestCode+" )"
	// + " and nstatus =
	// "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")"
	// + " then 'true' else 'false' end as validateparamcount";
	//
	// boolean validateParamCount = jdbcTemplate.queryForObject(resultParamValidate,
	// Boolean.class);
	//
	// if(validateParamCount)
	// {
	// String seqNoQry = "select nsequenceno from seqnoregistration where stablename
	// = 'sampleapprovalhistory';";
	// seqNoQry +="select nsequenceno from seqnoregistration where stablename =
	// 'registrationtesthistory';";
	// seqNoQry +="select nsequenceno from seqnoregistration where stablename =
	// 'resultparamcommenthistory';";
	//
	// //String seqNoQry ="select nsequenceno from seqnoregistration where
	// stablename in('sampleapprovalhistory','registrationtesthistory',
	// 'resultparamcommenthistory');";
	//
	// final List<?> seqNoList=(List<SeqNoRegistration>)
	// projectDAOSupport.getMultipleEntitiesResultSetInList(seqNoQry,
	// SeqNoRegistration.class,SeqNoRegistration.class, SeqNoRegistration.class);
	// final List<SeqNoRegistration> objSampleHistorySeq = (List<SeqNoRegistration>)
	// seqNoList.get(0);
	// final List<SeqNoRegistration> objTestHistorySeq =(List<SeqNoRegistration>)
	// seqNoList.get(1);
	// final List<SeqNoRegistration> resultCommentHistorySeq
	// =(List<SeqNoRegistration>) seqNoList.get(2);
	//
	// nseqnoApprovalHistory = objSampleHistorySeq.get(0).getNsequenceno();
	// nseqnoTestHistory = objTestHistorySeq.get(0).getNsequenceno();
	// nseqnoCommentHistory = resultCommentHistorySeq.get(0).getNsequenceno();
	//
	// final String testListQry = "select * from registrationtest where
	// ntransactiontestcode in ("+ntransactionTestCode+") and npreregno in
	// ("+npreregno+");"
	// +"select ntransactionresultcode from approvalparameter where
	// ntransactiontestcode in ("+ntransactionTestCode+") and npreregno in
	// ("+npreregno+");"
	// + " select * from resultparametercomments where senforcestatuscomment is not
	// null and"
	// + " nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
	// + " and ntransactiontestcode in ("+ntransactionTestCode+")";
	// final List<?> sizeGet=(List<SeqNoRegistration>)
	// projectDAOSupport.getMultipleEntitiesResultSetInList(testListQry,
	// RegistrationTest.class, ApprovalParameter.class,
	// ResultParameterComments.class);
	//
	// final List<RegistrationTest>
	// transTestList=(List<RegistrationTest>)sizeGet.get(0);
	// final List<ApprovalParameter>
	// paramList=(List<ApprovalParameter>)sizeGet.get(1);
	// final List<ResultParameterComments>
	// commentList=(List<ResultParameterComments>) sizeGet.get(2);

	// final String resultParamCommentCode =
	// stringUtilityFunction.fnDynamicListToString(commentList,
	// "getNtransactiontestcode");

	// if(ntransStatus==Enumeration.TransactionStatus.RECALC.gettransactionstatus()&&transTestList.size()>0){
	// //RECALC
	// /**Recalc - insert in Sample approval history and registration test history
	// table as test status recalc and insert into result change history and update
	// result parameter results to null**/
	//
	// return doReCalc(inputMap, userInfo, nseqnoApprovalHistory, nseqnoTestHistory,
	// nseqnoCommentHistory,
	// commentList, transTestList, paramList);
	// }
	// else
	// if(ntransStatus==Enumeration.TransactionStatus.RETEST.gettransactionstatus()){
	// // RETEST
	// return doReTest(inputMap, userInfo, nseqnoApprovalHistory, nseqnoTestHistory,
	// transTestList);
	// }
	// else{
	// //Other Dynamic Actions(Approve/Review/...)
	// return doDynamicApprovalAction(inputMap, userInfo, nseqnoApprovalHistory,
	// nseqnoTestHistory, transTestList);
	// }
	// }else{
	//
	// return new
	// ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_COMPLETERESULTS",
	// userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
	//
	// }
	//
	// }

	
	private ResponseEntity<Object> doDynamicApprovalAction(final Map<String, Object> inputMap, final UserInfo userInfo,
			final int nseqnoApprovalHistory, final int nseqnoTestHistory) throws Exception {
		final Map<String, Object> returnMap = new HashMap<>();
		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditOld = new JSONObject();
		JSONObject jsonAuditNew = new JSONObject();
		inputMap.keySet();

		final String npreregno = (String) inputMap.get("npreregno");
		final String ntransactionsamplecode = (String) inputMap.get("ntransactionsamplecode");
		final String ntransactionTestCode = (String) inputMap.get("ntransactiontestcode");

		final int ntransStatus = (int) inputMap.get("nTransStatus");
		final String scomments = userInfo.getSreason();

		final int nuserRoleCode = userInfo.getNuserrole();
		final int nuserCode = userInfo.getNusercode();
		final int ndeputyuserrolecode = userInfo.getNdeputyuserrole();
		final int ndeputyusercode = userInfo.getNdeputyusercode();

		final int nseqNoRegistrationHistory = (int) inputMap.get("RegistrationHistorySequence");
		final int nseqnoSampleHistory = (int) inputMap.get("RegistrationSampleSequence");
		final String sampleString = String.join(",",
				Stream.of(npreregno.trim().split(",")).collect(Collectors.toSet()));

		final String lstpreregqry = "select npreregno from registration where (registration.jsondata->>'nexternalordercode') != 'nexternalordercode' and npreregno in("
				+ npreregno + ") and nsitecode="+userInfo.getNtranssitecode()+"";
		
		List<RegistrationParameter> lstprereg = jdbcTemplate.query(lstpreregqry, new RegistrationParameter());

		if ((short) inputMap.get("nlevelno") == 1 && lstprereg.size() > 0) {
			final String preregno = lstprereg.stream().map(objpreregno -> String.valueOf(objpreregno.getNpreregno()))
					.collect(Collectors.joining(","));
			final String query = " insert into externalorderresult (nexternalordercode,nexternalordersamplecode,"
					+ " nexternalordertestcode,npreregno,ntransactionsamplecode,ntransactiontestcode,ntransactionresultcode,ntestcode,jsondata,"
					+ " nsentstatus,dtransactiondate,nsitecode,nstatus)" + " select  "
					+ " (r.jsondata->'nexternalordercode')::int as nexternalordercode,1 as nexternalordersamplecode,1 as nexternalordertestcode,rt.npreregno,"
					+ "rt.ntransactionsamplecode, " + "ap.ntransactiontestcode, "
					+ "ap.ntransactionresultcode,rt.ntestcode, ap.jsondata,8, " + "'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' dtransactiondate,"
					+ userInfo.getNtranssitecode() + ",1"
					+ " from approvalparameter ap,registrationtest rt,registration r "
					+ " where  r.npreregno=rt.npreregno and ap.ntransactiontestcode in (" + ntransactionTestCode + ")"
					+ " and ap.nsitecode = " + userInfo.getNtranssitecode()
					+ " and r.nsitecode = " + userInfo.getNtranssitecode()
					+ " and rt.ntransactiontestcode=ap.ntransactiontestcode "
					+ " and r.npreregno in (" + preregno
					+ ") and rt.nsitecode = " + userInfo.getNtranssitecode();

			jdbcTemplate.execute(query);

		}
		final String approvalHistory = " insert into sampleapprovalhistory (napprovalhistorycode,npreregno,nusercode,"
				+ " nuserrolecode,ndeputyusercode,ndeputyuserrolecode,ntransactionstatus,dtransactiondate,scomments,nsitecode,nstatus)"
				+ " select " + nseqnoApprovalHistory + "+rank()over(order by npreregno) napprovalhistorycode,"
				+ " npreregno," + nuserCode + " nusercode," + nuserRoleCode + " nuserrolecode," + ndeputyusercode
				+ " ndeputyusercode," + ndeputyuserrolecode + " ndeputyuserrolecode," + ntransStatus
				+ " ntransactionstatus," + "'" + dateUtilityFunction.getCurrentDateTime(userInfo)// objGeneral.getUTCDateTime()
				+ "' dtransactiondate," + "N'" + stringUtilityFunction.replaceQuote(scomments) + "' scomments,"
				+ userInfo.getNtranssitecode() + ",1 nstatus from registration where npreregno in (" + sampleString
				+ ")" + " and nsitecode=" + userInfo.getNtranssitecode() + " order by npreregno;";

		final String testHistory = " insert into registrationtesthistory (ntesthistorycode,ntransactiontestcode,ntransactionsamplecode,"
				+ " npreregno,ntransactionstatus,dtransactiondate,nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments,nformcode,"
				+ " nsitecode,nstatus,nsampleapprovalhistorycode)" + " select " + nseqnoTestHistory
				+ "+rank()over(order by ntransactiontestcode) ntesthistorycode,ntransactiontestcode,ntransactionsamplecode,npreregno,"
				+ " " + ntransStatus + " ntransactionstatus,'" + dateUtilityFunction.getCurrentDateTime(userInfo)// objGeneral.getUTCDateTime()
				+ "' dtransactiondate," + nuserCode + " nusercode," + nuserRoleCode + " nuserrolecode,"
				+ ndeputyusercode + " ndeputyusercode," + ndeputyuserrolecode + " ndeputyuserrolecode," + " N'"
				+ stringUtilityFunction.replaceQuote(scomments) + "' scomments," + userInfo.getNformcode()
				+ " nformcode," + userInfo.getNtranssitecode() + ",1 nstatus," + nseqnoApprovalHistory
				+ "+rank()over(order by npreregno) nsampleapprovalhistorycode "
				+ " from  registrationtest where ntransactiontestcode in (" + ntransactionTestCode + ")"
				+ " and nsitecode=" + userInfo.getNtranssitecode() + " order by ntransactiontestcode;";

		jdbcTemplate.execute(approvalHistory);
		jdbcTemplate.execute(testHistory);

		/** Sample Status update(registration history) **/
		updateSampleStatus(ntransStatus, npreregno, userInfo, scomments, nseqNoRegistrationHistory);
		updateSubSampleStatus(ntransStatus, ntransactionsamplecode, userInfo, scomments, nseqnoSampleHistory);

		/** Section Status update(registration Section history) **/
		// updateSectionHistory(ntransStatus,nsectionCode,npreregno,
		// userInfo,scomments);

		/** Role Wise Auto Approval if Applicable */
		roleWiseAutoApproval(inputMap, userInfo);
		inputMap.put("ntransactiontestcode", ntransactionTestCode);

		returnMap.putAll((Map<String, Object>) getApprovalSample(inputMap, userInfo).getBody());
		List<Map<String, Object>> sampleList = (List<Map<String, Object>>) returnMap.get("AP_SAMPLE");
		List<Map<String, Object>> subSampleList = (List<Map<String, Object>>) returnMap.get("AP_SUBSAMPLE");
		List<Map<String, Object>> testList = (List<Map<String, Object>>) returnMap.get("AP_TEST");

		returnMap.put("updatedSample", sampleList);
		returnMap.put("updatedSubSample", subSampleList);
		returnMap.put("updatedTest", testList);

		List<?> lstRegistration = (List<?>) returnMap.get("updatedSample");
		List<?> lstRegistrationTest = (List<?>) returnMap.get("updatedTest");

		List<Object> lsResult = new ArrayList<Object>();
		List<String> testArray = Arrays.asList(ntransactionTestCode.split(","));

		List<?> oldRegistration = (List<?>) inputMap.get("APSelectedSample");
		List<?> oldRegistrationTest = (List<?>) inputMap.get("APSelectedTest");

		List<Object> oldResult = new ArrayList<Object>();

		lsResult.add(lstRegistration);
		lsResult.add(lstRegistrationTest);

		oldResult.add(oldRegistration);
		oldResult.add(oldRegistrationTest);

		List<Map<String, Object>> filteredApprovedTest = ((List<Map<String, Object>>) inputMap.get("APSelectedTest"))
				.stream()
				.filter(test -> testArray.stream()
						.anyMatch(vd -> test.get("ntransactiontestcode").toString().equals(vd)))
				.collect(Collectors.toList());
		List<Map<String, Object>> filteredApprovedTestnew = ((List<Map<String, Object>>) returnMap.get("AP_TEST"))
				.stream()
				.filter(test -> testArray.stream()
						.anyMatch(vd -> test.get("ntransactiontestcode").toString().equals(vd)))
				.collect(Collectors.toList());
		filteredApprovedTest.sort(Comparator.comparing(m -> (int) (m).get("ntransactiontestcode")));
		filteredApprovedTestnew.sort(Comparator.comparing(m -> (int) (m).get("ntransactiontestcode")));
		jsonAuditOld.put("registrationtest", filteredApprovedTest);
		jsonAuditNew.put("registrationtest", filteredApprovedTestnew);
		auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
		auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
		auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
		actionType.put("registrationtest", inputMap.get("stransdisplaystatus"));
		auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, jsonAuditNew, actionType, auditmap, false, userInfo);

		// ALPD-4021-Test Approval : Mail Send during First Level of approval,selected
		// tests under sample.
		// For NFC Client--Sent Email First Level of approval Deviation Result
		if ((short) inputMap.get("nlevelno") == 1 && lstprereg.size() > 0) {
			Map<String, Object> mailMap = new HashMap<>();

			String preregno = lstprereg.stream().map(objpreregno -> String.valueOf(objpreregno.getNpreregno()))
					.collect(Collectors.joining(","));
			// ALPD-5619 - by Gowtham R on 28/03/2025 - Mail alert transaction > NULL
			// displayed in reference no column.
			// start
			final String deviationPreregNo = " SELECT  eor.npreregno, rar.sarno " + " FROM externalorderresult eor"
					+ " JOIN registrationtest rt ON eor.npreregno = rt.npreregno"
					+ " JOIN registration r ON rt.npreregno = r.npreregno"
					+ " JOIN registrationarno rar ON r.npreregno = rar.npreregno"
					+ " LEFT JOIN externalorderresult ee ON" + " (ee.jsondata->>'ngradecode')::int <> 1"
					+ " AND (ee.jsondata->>'ngradecode') <> null"
					+ " LEFT JOIN grade gr ON gr.ngradecode = (ee.jsondata->>'ngradecode')::int"
					+ " WHERE eor.npreregno in(" + preregno + ")"
					+ " AND rt.ntransactiontestcode = eor.ntransactiontestcode" + " AND eor.nsitecode ="
					+ userInfo.getNtranssitecode() + " " + " AND rt.nsitecode =" + userInfo.getNtranssitecode() + " "
					+ " AND r.nsitecode =" + userInfo.getNtranssitecode() + " " + " AND rar.nsitecode ="
					+ userInfo.getNtranssitecode() + " " + " AND eor.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " AND rt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " AND r.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " GROUP BY eor.npreregno, rar.sarno";

			final List<Registration> ndevialtionPreRegNo = jdbcTemplate.query(deviationPreregNo, new Registration());
			if (!ndevialtionPreRegNo.isEmpty()) {
				ndevialtionPreRegNo.stream().forEach(preregno1 -> {
					mailMap.put("ncontrolcode", inputMap.get("ncontrolCode"));
					mailMap.put("npreregno", preregno1.getNpreregno());
					mailMap.put("ssystemid", preregno1.getSarno());
					// end
					try {
						ResponseEntity<Object> mailResponse = emailDAOSupport.createEmailAlertTransaction(mailMap,
								userInfo);
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			}
		}
		return new ResponseEntity<>(returnMap, HttpStatus.OK);

	}

	
	private ResponseEntity<Object> doReCalc(final Map<String, Object> inputMap, final UserInfo userInfo,
			final int nseqnoApprovalHistory, final int nseqnoTestHistory, final int nseqnoCommentHistory,
			final List<ResultParameterComments> commentList, final int resultChangeSeqNo) throws Exception {

		final Map<String, Object> returnMap = new HashMap<>();

		final String npreregno = (String) inputMap.get("npreregno");
		final String ntransactionsamplecode = (String) inputMap.get("ntransactionsamplecode");
		final String ntransactionTestCode = (String) inputMap.get("ntransactiontestcode");
		final String napproveconfversioncode = (String) inputMap.get("napprovalversioncode");
		final int ntransStatus = (int) inputMap.get("nTransStatus");
		final String scomments = userInfo.getSreason();

		final ApprovalConfigVersion objApprovalConfig = checkUserAvailablity(napproveconfversioncode, userInfo);
		if (objApprovalConfig == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_USERNOTINMAPPING", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			returnMap.putAll((Map<String, Object>) checkReleaseRecord(inputMap, userInfo).getBody());
			if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus() == returnMap.get("rtn")) {
				validationForRelease(npreregno, ntransactionsamplecode, ntransactionTestCode,userInfo);
			}
			final int nuserRoleCode = userInfo.getNuserrole();
			final int nuserCode = userInfo.getNusercode();
			final int ndeputyuserrolecode = userInfo.getNdeputyuserrole();
			final int ndeputyusercode = userInfo.getNdeputyusercode();

			final String resultParamCommentCode = stringUtilityFunction.fnDynamicListToString(commentList,
					"getNtransactiontestcode");

			final String approvalHistory = " insert into sampleapprovalhistory (napprovalhistorycode,npreregno,nusercode,"
					+ " nuserrolecode,ndeputyusercode,ndeputyuserrolecode,dtransactiondate,scomments,ntransactionstatus,nsitecode,nstatus)"
					+ " select " + nseqnoApprovalHistory
					+ "+rank()over(order by npreregno) napprovalhistorycode,npreregno npreregno," + nuserCode
					+ " nusercode," + nuserRoleCode + " nuserrolecode, " + userInfo.getNdeputyusercode()
					+ " ndeputyusercode," + userInfo.getNdeputyuserrole() + " ndeputyuserrolecode,'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo)// objGeneral.getUTCDateTime()
					+ "' dtransactiondate," + " N'" + stringUtilityFunction.replaceQuote(scomments) + "' scomments,"
					+ ntransStatus + " ntransactionstatus," + userInfo.getNtranssitecode()
					+ ",1 nstatus from registration " + " where npreregno in (" + npreregno + ") " + " and nsitecode="
					+ userInfo.getNtranssitecode() + " order by npreregno;";

			final String testHistory = " insert into registrationtesthistory (ntesthistorycode,ntransactiontestcode,ntransactionsamplecode,"
					+ " npreregno,ntransactionstatus,dtransactiondate,nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments,nformcode,nsitecode,"
					+ " nstatus,nsampleapprovalhistorycode)" + " select " + nseqnoTestHistory
					+ "+rank()over(order by ntransactiontestcode) ntesthistorycode,ntransactiontestcode,ntransactionsamplecode,npreregno,"
					+ " " + ntransStatus + " ntransactionstatus,'" + dateUtilityFunction.getCurrentDateTime(userInfo)// objGeneral.getUTCDateTime()
					+ "' dtransactiondate," + nuserCode + " nusercode," + nuserRoleCode + " nuserrolecode," + " "
					+ ndeputyusercode + " ndeputyusercode," + ndeputyuserrolecode + " ndeputyuserrolecode," + " N'"
					+ stringUtilityFunction.replaceQuote(scomments) + "' scomments," + userInfo.getNformcode()
					+ " nformcode," + userInfo.getNtranssitecode() + ",1 nstatus," + nseqnoApprovalHistory
					+ "+rank()over(order by npreregno) nsampleapprovalhistorycode"
					+ " from  registrationtest where ntransactiontestcode in (" + ntransactionTestCode + ") "
					+ " and nsitecode=" + userInfo.getNtranssitecode() + " order by ntransactiontestcode;";

			final String resultChangeHistoryInsert = "insert into resultchangehistory (nresultchangehistorycode,ntransactionresultcode,ntransactiontestcode,"
					+ "npreregno,ntestgrouptestparametercode,nparametertypecode,nformcode,nenforceresult,"
					+ "nenforcestatus,ngradecode,nenteredby,nenteredrole,ndeputyenteredby,ndeputyenteredrole,jsondata,nsitecode,nstatus) "
					+ "select " + resultChangeSeqNo
					+ "+ rank() over(order by ntransactionresultcode) nresultchangehistorycode,"
					+ "ntransactionresultcode nresultparametercode,ntransactiontestcode,npreregno, "
					+ "ntestgrouptestparametercode,nparametertypecode," + userInfo.getNformcode()
					+ ",nenforceresult,nenforcestatus,"
					+ "ngradecode,nenteredby,nenteredrole,ndeputyenteredby,ndeputyenteredrole,jsondata,"
					+ userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " nstatus" + " from approvalparameter where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and ntransactiontestcode in( " + ntransactionTestCode + ");";

			String rpchQueryString = "";
			String updateRPCQueryString = "";
			if (!commentList.isEmpty()) {
				// Modified the query by sonia on 18th Feb 2025 for jira id:ALPD-3268
				rpchQueryString = " INSERT INTO resultparamcommenthistory(nresultparamcommenthistorycode,ntransactionresultcode, "
						+ " ntransactiontestcode, npreregno ,ntestgrouptestparametercode,ntestparametercode, "
						+ " jsondata,nsitecode,nstatus)" + " select " + nseqnoCommentHistory
						+ "+ rank() over (order by (ntransactionresultcode)) nresultparamcommenthistorycode, ntransactionresultcode "
						+ " ,ntransactiontestcode, npreregno ,ntestgrouptestparametercode,ntestparametercode "
						+ " ,jsondata," + userInfo.getNtranssitecode() + ", nstatus from resultparametercomments "
						+ " where jsondata->'senforcestatuscomment' is not null and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
						+ userInfo.getNtranssitecode() + " and ntransactiontestcode in (" + resultParamCommentCode
						+ ");";

				// Modified the query by sonia on 18th Feb 2025 for jira id:ALPD-3268
				updateRPCQueryString = " update resultparametercomments set jsondata = '{}' "
						+ " where ntransactiontestcode in (" + resultParamCommentCode + ");";

			}

			String updateQueries = "";
			final String parameterTypeQueries = "select * from registrationparameter where ntransactiontestcode in ("
					+ ntransactionTestCode + ") " + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNtranssitecode() + "";

			final List<RegistrationParameter> parameterTypeList = jdbcTemplate.query(parameterTypeQueries,
					new RegistrationParameter());

			for (int i = 0; i < parameterTypeList.size(); i++) {

				updateQueries = updateQueries
						+ " update resultparameter set jsondata = jsondata || '{\"sfinal\": null,\"sresult\":null,\"dentereddate\":null}'::jsonb, ngradecode = "
						+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ", nenforcestatus="
						+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", nenforceresult="
						+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", nparametertypecode="
						+ parameterTypeList.get(i).getNparametertypecode() + ","
						+ " nenteredby=-1,nenteredrole=-1,ndeputyenteredby=-1,ndeputyenteredrole=-1 "
						+ " where ntransactionresultcode =" + parameterTypeList.get(i).getNtransactionresultcode()
						+ " and nenforceresult=" + Enumeration.TransactionStatus.YES.gettransactionstatus() + ";";

			}
			updateQueries = updateRPCQueryString + updateQueries;

			jdbcTemplate.execute(approvalHistory);
			jdbcTemplate.execute(testHistory);
			jdbcTemplate.execute(resultChangeHistoryInsert);

			if (!rpchQueryString.isEmpty())
				jdbcTemplate.execute(rpchQueryString);
			jdbcTemplate.execute(updateQueries);

			final String testAuditGet = "select rth.npreregno,rth.ntransactionsamplecode,rth.ntransactionstatus,rth.nusercode,rth.nuserrolecode,"
					+ " rth.ntesthistorycode,rt.ntransactiontestcode,"
					+ " tm.stestsynonym ||' ['||CAST(rt.ntestrepeatno AS character varying(10))||']['||CAST(rt.ntestretestno AS character varying(10))||']' as stestsynonym,"
					+ " rt.ntestgrouptestcode,rar.sarno,ts.jsondata->'stransdisplaystatus'->>'"
					+ userInfo.getSlanguagetypecode() + "'::character varying(50) as stransdisplaystatus,"
					+ " ur.suserrolename,CONCAT(u.sfirstname,' ',u.slastname) as username,CAST(rth.dtransactiondate AS character varying(100)) stransactiondate"
					+ " from registrationtesthistory rth,registrationtest rt,testmaster tm,registrationarno rar,transactionstatus ts"
					+ " ,users u,userrole ur  where rth.ntransactiontestcode=rt.ntransactiontestcode"
					+ " and rt.npreregno = rar.npreregno and tm.ntestcode=rt.ntestcode"
					+ " and ts.ntranscode=rth.ntransactionstatus and u.nusercode=rth.nusercode"
					+ " and ur.nuserrolecode=rth.nuserrolecode and rth.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rar.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tm.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.nsitecode=rt.nsitecode"
					+ " and rt.nsitecode=rar.nsitecode" + " and rth.nsitecode=" + userInfo.getNtranssitecode()
					+ " and rt.nsitecode=" + userInfo.getNtranssitecode() + " and rar.nsitecode="
					+ userInfo.getNtranssitecode() + " and tm.nsitecode=" + userInfo.getNmastersitecode()
					+ " and u.nsitecode=" + userInfo.getNmastersitecode() + " and ur.nsitecode="
					+ userInfo.getNmastersitecode() + " and ts.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.ntesthistorycode = any("
					+ "	select max(ntesthistorycode) ntesthistorycode from registrationtesthistory"
					+ "	where ntransactiontestcode in(" + ntransactionTestCode + ") and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNtranssitecode() + "	group by ntransactiontestcode)";

			final List<RegistrationTestHistory> newTransList = (List<RegistrationTestHistory>) jdbcTemplate
					.query(testAuditGet, new RegistrationTestHistory());

			List<Object> insertAuditList = new ArrayList<>();

			insertAuditList.add(newTransList);

			final int nseqNoRegistrationHistory = (int) inputMap.get("RegistrationHistorySequence");
			final int nseqnoSampleHistory = (int) inputMap.get("RegistrationSampleSequence");

			updateSampleStatus(Enumeration.TransactionStatus.RECALC.gettransactionstatus(), npreregno, userInfo,
					scomments, nseqNoRegistrationHistory);
			updateSubSampleStatus(Enumeration.TransactionStatus.RECALC.gettransactionstatus(), ntransactionsamplecode,
					userInfo, scomments, nseqnoSampleHistory);

			inputMap.put("ntransactiontestcode", ntransactionTestCode);

			returnMap.putAll((Map<String, Object>) getApprovalSample(inputMap, userInfo).getBody());
			final List<Map<String, Object>> sampleList = (List<Map<String, Object>>) returnMap.get("AP_SAMPLE");
			final List<Map<String, Object>> subSampleList = (List<Map<String, Object>>) returnMap.get("AP_SUBSAMPLE");
			final List<Map<String, Object>> testList = (List<Map<String, Object>>) returnMap.get("AP_TEST");

			returnMap.put("updatedSample", sampleList);
			returnMap.put("updatedSubSample", subSampleList);
			returnMap.put("updatedTest", testList);

			final List<String> testArray = Arrays.asList(ntransactionTestCode.split(","));

			Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
			JSONObject actionType = new JSONObject();
			JSONObject jsonAuditOld = new JSONObject();
			JSONObject jsonAuditNew = new JSONObject();

			final List<Map<String, Object>> filteredApprovedTest = ((List<Map<String, Object>>) inputMap
					.get("APSelectedTest"))
					.stream()
					.filter(test -> testArray.stream()
							.anyMatch(vd -> test.get("ntransactiontestcode").toString().equals(vd)))
					.collect(Collectors.toList());

			final List<Map<String, Object>> filteredApprovedTestnew = ((List<Map<String, Object>>) returnMap
					.get("AP_TEST"))
					.stream()
					.filter(test -> testArray.stream()
							.anyMatch(vd -> test.get("ntransactiontestcode").toString().equals(vd)))
					.collect(Collectors.toList());

			filteredApprovedTest.sort(Comparator.comparing(m -> (int) (m).get("ntransactiontestcode")));
			filteredApprovedTestnew.sort(Comparator.comparing(m -> (int) (m).get("ntransactiontestcode")));
			jsonAuditOld.put("registrationtest", filteredApprovedTest);
			jsonAuditNew.put("registrationtest", filteredApprovedTestnew);
			auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
			auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
			auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));

			actionType.put("registrationtest", "IDS_RECALC");
			auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, jsonAuditNew, actionType, auditmap, false, userInfo);

			List<?> lstRegistration = (List<?>) returnMap.get("updatedSample");
			List<?> lstRegistrationTest = (List<?>) returnMap.get("updatedTest");

			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		}
	}

	
	private ResponseEntity<Object> doReTest(final Map<String, Object> inputMap, final UserInfo userInfo,

			final List<RegistrationTest> transTestList) throws Exception {
		// RETEST
		final Map<String, Object> returnMap = new HashMap<>();

		final String npreregno = (String) inputMap.get("npreregno");
		final String ntransactionsamplecode = (String) inputMap.get("ntransactionsamplecode");
		final String ntransactionTestCode = (String) inputMap.get("ntransactiontestcode");
		final String napprovalVersionCode = (String) inputMap.get("napprovalversioncode");
		returnMap.putAll((Map<String, Object>) checkReleaseRecord(inputMap, userInfo).getBody());

		if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus() == returnMap.get("rtn")) {
			validationForRelease(npreregno, ntransactionsamplecode, ntransactionTestCode,userInfo);
		}

		final int ntransStatus = (int) inputMap.get("nTransStatus");
		final String scomments = userInfo.getSreason();
		String approvalHistory = "";
		int retestCount = 1;
		if (inputMap.containsKey("retestcount")) {
			retestCount = (int) inputMap.get("retestcount");
		}

		final int nseqnoApprovalHistory = (int) inputMap.get("ApprovalHistorySequence");
		final int nseqnoTestHistory = (int) inputMap.get("TestHistorySequence");
		 int jaSeqNo = (int) inputMap.get("JASequence");
		 int llinterSeqNo = (int) inputMap.get("LlinterSequence");
		 int registrationParamSeq = (int) inputMap.get("RegParamSequence");
		 int testSeqNo = (int) inputMap.get("TestSequence");
		 int sectionSeqNo = (int) inputMap.get("SectionSequence");

		final int nuserRoleCode = userInfo.getNuserrole();
		final int nuserCode = userInfo.getNusercode();
		final int ndeputyuserrolecode = userInfo.getNdeputyuserrole();
		final int ndeputyusercode = userInfo.getNdeputyusercode();
		List<Object> insertAuditList = new ArrayList<>();
		final int llinterParamCount = (int) inputMap.get("LlinterParameterCount");

		int newTestTransStatus = Enumeration.TransactionStatus.REGISTERED.gettransactionstatus();

		if ((boolean) inputMap.get("nneedjoballocation")) {
			newTestTransStatus = Enumeration.TransactionStatus.RECIEVED_IN_SECTION.gettransactionstatus();

		}
		approvalHistory = " insert into sampleapprovalhistory (napprovalhistorycode,npreregno,nusercode,"
				+ " nuserrolecode,ndeputyusercode,ndeputyuserrolecode,ntransactionstatus,dtransactiondate,scomments,nsitecode,nstatus)"
				+ " select " + nseqnoApprovalHistory + "+rank()over(order by npreregno) napprovalhistorycode,"
				+ "npreregno," + nuserCode + " nusercode," + nuserRoleCode + " nuserrolecode," + ndeputyusercode
				+ " ndeputyusercode," + ndeputyuserrolecode + " ndeputyuserrolecode," + ntransStatus
				+ " ntransactionstatus," + "'" + dateUtilityFunction.getCurrentDateTime(userInfo)// objGeneral.getUTCDateTime()
				+ "' dtransactiondate," + "N'" + stringUtilityFunction.replaceQuote(scomments) + "' scomments,"
				+ userInfo.getNtranssitecode() + ",1 nstatus from registration " + " where npreregno in (" + npreregno
				+ ")" + " and nsitecode=" + userInfo.getNtranssitecode() + " order by npreregno;";

		final String retestHistory = " insert into registrationtesthistory (ntesthistorycode,ntransactiontestcode,ntransactionsamplecode,"
				+ " npreregno,ntransactionstatus,dtransactiondate,nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments,nformcode,"
				+ " nsitecode,nstatus,nsampleapprovalhistorycode)" + " select " + nseqnoTestHistory
				+ "+rank()over(order by ntransactiontestcode) ntesthistorycode,ntransactiontestcode,ntransactionsamplecode,npreregno,"
				+ " " + ntransStatus + " ntransactionstatus,'" + dateUtilityFunction.getCurrentDateTime(userInfo)// objGeneral.getUTCDateTime()
				+ "' dtransactiondate," + nuserCode + " nusercode," + nuserRoleCode + " nuserrolecode,"
				+ ndeputyusercode + " ndeputyusercode," + ndeputyuserrolecode + " ndeputyuserrolecode," + " N'"
				+ stringUtilityFunction.replaceQuote(scomments) + "' scomments," + userInfo.getNformcode()
				+ " nformcode," + userInfo.getNtranssitecode() + ",1 nstatus," + nseqnoApprovalHistory
				+ "+rank()over(order by npreregno) nsampleapprovalhistorycode "
				+ " from  registrationtest where ntransactiontestcode in (" + ntransactionTestCode + ") "
				+ " and nsitecode=" + userInfo.getNtranssitecode() + " order by ntransactiontestcode;";
		jdbcTemplate.execute(approvalHistory);
		jdbcTemplate.execute(retestHistory);



		int nregparametecount = (int) inputMap.get("RegParameterCount");

		String jobAllocationInsert = "";

		final String getAnalystRole = "select nuserrolecode from approvalconfigversion acv,treetemplatetransactionrole ttr"
				+ " where  ttr.ntreeversiontempcode=acv.ntreeversiontempcode and ttr.schildnode='-1'"
				+ " and ttr.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and acv.nsitecode=" + userInfo.getNmastersitecode() + " " + " and ttr.nsitecode="
				+ userInfo.getNmastersitecode() + " " + " and acv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and napproveconfversioncode="
				+ napprovalVersionCode;

		final int analystRoleCode = jdbcTemplate.queryForObject(getAnalystRole, Integer.class);
		final int systemUserCode = -1;

		String newTestInsert = "";
		String retestTestHistory = "";
		String insertRegParameter = "";
		String insertResultParam = "";
		String insertResultComments = "";
		String insertllinter = "";
		String labSheetMaster = "";
		String labSheetDetails = "";
		String insertSectionhistory = "";
		int newtestHistorySeqno = nseqnoTestHistory + transTestList.size();

		for (int i = 1; i <= retestCount; i++) {

			newTestInsert = " insert into registrationtest(ntransactiontestcode,ntransactionsamplecode,npreregno,ntestgrouptestcode,"
					+ " ntestcode,nsectioncode,nmethodcode,ninstrumentcatcode,nchecklistversioncode,ntestrepeatno,ntestretestno,jsondata,dmodifieddate,nsitecode,nstatus) "
					+ " select " + testSeqNo + "+rank()over(order by  ntransactiontestcode) ntransactiontestcode,"
					+ " rt.ntransactionsamplecode,rt.npreregno,rt.ntestgrouptestcode,"
					+ " rt.ntestcode,rt.nsectioncode,rt.nmethodcode,rt.ninstrumentcatcode,rt.nchecklistversioncode,rt.ntestrepeatno,"
					+ " (select max(ntestretestno) from registrationtest where ntestgrouptestcode = rt.ntestgrouptestcode and npreregno = rt.npreregno and ntestrepeatno = rt.ntestrepeatno and nsitecode="
					+ userInfo.getNtranssitecode() + ")+1 ntestretestno, "
					+ " rt.jsondata||json_build_object('ntransactiontestcode'," + testSeqNo
					+ "+rank()over(order by  ntransactiontestcode))::jsonb"
					+ " ||json_build_object('stestsynonym',CONCAT(tgt.stestsynonym,'['||rt.ntestrepeatno||']'||'['||(select max(ntestretestno)"
					+ " from registrationtest where ntestgrouptestcode = rt.ntestgrouptestcode and npreregno = rt.npreregno"
					+ " and ntestrepeatno = rt.ntestrepeatno and nsitecode=" + userInfo.getNtranssitecode()
					+ ")+1||']'))::jsonb,'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
					+ userInfo.getNtranssitecode()
					+ " ,rt.nstatus from registrationtest rt,testgrouptest tgt where rt.ntestgrouptestcode = tgt.ntestgrouptestcode and rt.ntransactiontestcode in ("
					+ ntransactionTestCode + ")" + " and  rt.nsitecode=" + userInfo.getNtranssitecode()
					+ " and  tgt.nsitecode=" + userInfo.getNmastersitecode() + ";";

			retestTestHistory = " insert into registrationtesthistory (ntesthistorycode,ntransactiontestcode,ntransactionsamplecode,"
					+ " npreregno,ntransactionstatus,dtransactiondate,nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments,nformcode,"
					+ " nsitecode,nstatus,nsampleapprovalhistorycode)" + " select " + newtestHistorySeqno
					+ "+rank()over(order by ntransactiontestcode) ntesthistorycode," + testSeqNo
					+ "+rank()over(order by ntransactiontestcode) ntransactiontestcode,"
					+ " ntransactionsamplecode,npreregno," + " " + newTestTransStatus + " ntransactionstatus,'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' dtransactiondate," + systemUserCode
					+ " nusercode," + analystRoleCode + " nuserrolecode," + "" + analystRoleCode + " ndeputyusercode,"
					+ systemUserCode + " ndeputyuserrolecode," + " N'" + stringUtilityFunction.replaceQuote(scomments)
					+ "' scomments," + userInfo.getNformcode() + " nformcode," + userInfo.getNtranssitecode()
					+ ",1 nstatus," + " -1 nsampleapprovalhistorycode"
					+ " from  registrationtest where ntransactiontestcode in (" + ntransactionTestCode + ") "
					+ " and   nsitecode=" + userInfo.getNtranssitecode() + " order by ntransactiontestcode;";

			insertRegParameter = "insert into registrationparameter (ntransactionresultcode,ntransactiontestcode,npreregno,"
					+ " ntestgrouptestparametercode,ntestparametercode,nparametertypecode,ntestgrouptestformulacode,nresultmandatory,"
					+ " nreportmandatory,nunitcode,jsondata,nsitecode,nstatus) " + " select " + registrationParamSeq
					+ "+rank()over(order by ntransactionresultcode) ntransactionresultcode," + testSeqNo
					+ "+dense_rank()over(order by ntransactiontestcode) ntransactiontestcode," + " npreregno,"
					+ " ntestgrouptestparametercode,ntestparametercode,nparametertypecode,ntestgrouptestformulacode,nresultmandatory,"
					+ " nreportmandatory,nunitcode,jsondata||json_build_object('ntransactiontestcode'," + testSeqNo
					+ "+dense_rank()over(order by  ntransactiontestcode))::jsonb"
					+ "|| json_build_object('sresult',null)::jsonb||json_build_object('sfinal',null)::jsonb"
					+ "||json_build_object('ntransactionresultcode'," + registrationParamSeq
					+ "+rank()over(order by ntransactionresultcode))::jsonb," + userInfo.getNtranssitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
					+ " from registrationparameter where ntransactiontestcode in (" + ntransactionTestCode + ")"
					+ " and nsitecode=" + userInfo.getNtranssitecode() + ";";

			insertResultParam = "insert into resultparameter ("
					+ "ntransactionresultcode,npreregno,ntransactiontestcode,"
					+ "ntestgrouptestparametercode,ntestparametercode,nparametertypecode,nresultmandatory,nreportmandatory,"
					+ "ntestgrouptestformulacode,nunitcode,ngradecode,ntransactionstatus,nenforcestatus,nenforceresult,"
					+ "ncalculatedresult," + "nenteredby,nenteredrole,ndeputyenteredby,"
					+ "ndeputyenteredrole,jsondata,nlinkcode,nattachmenttypecode,nsitecode,nstatus) " + "select "
					+ registrationParamSeq
					+ "+rank()over(order by ntransactionresultcode) ntransactionresultcode,npreregno," + testSeqNo
					+ "+dense_rank()over(order by ntransactiontestcode) ntransactiontestcode,"
					+ "ntestgrouptestparametercode,ntestparametercode,nparametertypecode,nresultmandatory,nreportmandatory,"
					+ "ntestgrouptestformulacode,nunitcode,-1 ngradecode,"
					+ Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()
					+ " ntransactionstatus,4 nenforcestatus,4 nenforceresult," + ""
					+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " ncalculatedresult,"
					+ "-1 nenteredby,-1 nenteredrole,-1 ndeputyenteredby,-1 ndeputyenteredrole,jsondata||json_build_object('ntransactiontestcode',"
					+ testSeqNo + "+dense_rank()over(order by  ntransactiontestcode))::jsonb"
					+ " || json_build_object('sresult',null)::jsonb||json_build_object('sfinal',null)::jsonb"
					+ "||json_build_object('ntransactionresultcode'," + registrationParamSeq
					+ "+rank()over(order by ntransactionresultcode))::jsonb," + "-1,-1," + userInfo.getNtranssitecode()
					+ ",1 nstatus " + "from registrationparameter where ntransactiontestcode in ("
					+ ntransactionTestCode + ")" + " and   nsitecode=" + userInfo.getNtranssitecode() + ";";

			insertResultComments = "insert into resultparametercomments ("
					+ "ntransactionresultcode,ntransactiontestcode,npreregno,ntestgrouptestparametercode,"
					+ "ntestparametercode,jsondata,nsitecode,nstatus) " + "select " + registrationParamSeq
					+ "+rank()over(order by ntransactionresultcode) ntransactionresultcode," + testSeqNo
					+ "+dense_rank()over(order by ntransactiontestcode) ntransactiontestcode,"
					+ "npreregno,ntestgrouptestparametercode,ntestparametercode," + "'{}',"
					+ userInfo.getNtranssitecode() + ",1 " + "from resultparameter where ntransactiontestcode in ("
					+ ntransactionTestCode + ") and   nsitecode=" + userInfo.getNtranssitecode() + ";";

			if (!(boolean) inputMap.get("nneedjoballocation")) {

				insertSectionhistory = "insert into registrationsectionhistory (nsectionhistorycode, npreregno, nsectioncode, ntransactionstatus, nsitecode, nstatus)"
						+ " " + " select " + sectionSeqNo
						+ "+Rank()over(order by max(ntransactiontestcode)) nsectionhistorycode ,npreregno,nsectioncode,"
						+ Enumeration.TransactionStatus.RECIEVED_IN_SECTION.gettransactionstatus()
						+ " ntransactionstatus," + userInfo.getNtranssitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  "
						+ " from registrationtest where ntransactiontestcode in (" + ntransactionTestCode + ") "
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsitecode=" + userInfo.getNtranssitecode() + " group by npreregno,nsectioncode ";
				// ALPD-1227
				jobAllocationInsert = "insert into joballocation (njoballocationcode,npreregno,ntransactionsamplecode,ntransactiontestcode,"
						+ " ntestrescheduleno,nsectioncode,nuserrolecode,nusercode,"
						+ " nuserperiodcode,ninstrumentcategorycode,ninstrumentcode,ninstrumentnamecode,"
						+ " ninstrumentperiodcode,ntimezonecode,jsondata,jsonuidata, ntechniquecode,"
						+ " nsitecode,nstatus)" + " select " + jaSeqNo
						+ "+rank()over(order by ntransactiontestcode) njoballocationcode,npreregno,ntransactionsamplecode,"
						+ " " + testSeqNo + "+rank()over(order by ntransactiontestcode) ntransactiontestcode,"
						+ " 0,nsectioncode," + analystRoleCode + " nuserrolecode," + systemUserCode + " nusercode,"
						+ " -1 nuserperiodcode,-1 ninstrumentcategorycode,-1 ninstrumentcode,-1 ninstrumentnamecode,"
						+ " -1 ninstrumentperiodcode,-1 ntimezonecode," + " json_build_object('duserblockfromdate','"
						+ dateUtilityFunction.instantDateToStringWithFormat(
								dateUtilityFunction.getCurrentDateTime(userInfo), "yyyy-MM-dd HH:mm:ss")
						+ "')::jsonb||json_build_object('duserblocktodate','"
						+ dateUtilityFunction.instantDateToStringWithFormat(
								dateUtilityFunction.getCurrentDateTime(userInfo), "yyyy-MM-dd HH:mm:ss")
						+ "')::jsonb||json_build_object('suserblockfromtime','00:00')::jsonb|| json_build_object('suserblocktotime','00:00')::jsonb||"
						+ " json_build_object('suserholdduration','0')::jsonb|| json_build_object('dinstblockfromdate',NULL)::jsonb||"
						+ " json_build_object('dinstblocktodate',NULL)::jsonb||"
						+ " json_build_object('sinstblockfromtime',NULL)::jsonb||json_build_object('sinstblocktotime',NULL)::jsonb||"
						+ " json_build_object('sinstrumentholdduration',NULL)::jsonb|| json_build_object('scomments','')::jsonb,"
						+ " json_build_object('duserblockfromdate','"
						+ dateUtilityFunction.instantDateToStringWithFormat(
								dateUtilityFunction.getCurrentDateTime(userInfo), "yyyy-MM-dd HH:mm:ss")
						+ "')::jsonb||json_build_object('duserblocktodate','"
						+ dateUtilityFunction.instantDateToStringWithFormat(
								dateUtilityFunction.getCurrentDateTime(userInfo), "yyyy-MM-dd HH:mm:ss")
						+ "')::jsonb||json_build_object('suserblockfromtime','00:00')::jsonb|| json_build_object('suserblocktotime','00:00')::jsonb||"
						+ " json_build_object('suserholdduration','0')::jsonb|| json_build_object('dinstblockfromdate',NULL)::jsonb||"
						+ " json_build_object('dinstblocktodate',NULL)::jsonb||"
						+ " json_build_object('sinstblockfromtime',NULL)::jsonb||json_build_object('sinstblocktotime',NULL)::jsonb||"
						+ " json_build_object('sinstrumentholdduration',NULL)::jsonb|| json_build_object('scomments','')::jsonb,-1,"
						+ userInfo.getNtranssitecode()
						+ " ,1 nstatus from registrationtest where ntransactiontestcode in (" + ntransactionTestCode
						+ ")" + " and nsitecode=" + userInfo.getNtranssitecode() + ";";

//				insertllinter = " insert into llinter(nllintercode, nscreentypecode, npreregno, nsampletypecode, nsamplecode, nregtypecode, nregsubtypecode, nproductcode, "
//						+ "nallottedspeccode, ntransactiontestcode, ntestgrouptestcode, nretestno, ntestrepeatcount, ntransactionresultcode, ntestgrouptestparametercode,"
//						+ " nparametertypecode, nrounddingdigits, nunitcode, sresult, nresult, nlltestcodde, nllparametercode, dreceiveddate, dregdate, sllarno,"
//						+ " sllparamkey, sllinterstatus, sllparamtype, sllcomponentname, slltestname, sllparametername, sproductname, sunitname, smanufname, smanuflotno,"
//						+ " smethodname, srefproductgroupname, susername, sclientname, nsitecode, nstatus)" + " select "
//						+ llinterSeqNo
//						+ "+ROW_NUMBER()over( order by ntestgrouptestparametercode) nllintercode,nscreentypecode,npreregno,nsampletypecode, "
//						+ " nsamplecode,nregtypecode,nregsubtypecode,nproductcode,nallottedspeccode, " 
//						+ " " + testSeqNo + "+DENSE_RANK() over(order by ntestgrouptestcode) ntransactiontestcode,"
//						//+ newTransTestCode + " ntransactiontestcode ,"
//						+ " ntestgrouptestcode,nretestno,ntestrepeatcount,"
//						+ " " + registrationParamSeq
//						+ "+DENSE_RANK() over(order by ntestgrouptestparametercode) ntransactionresultcode,"
//						+ " ntestgrouptestparametercode,nparametertypecode,nrounddingdigits,nunitcode,sresult,nresult,nlltestcodde,nllparametercode,"
//						+ " dreceiveddate,'" + dateUtilityFunction.getCurrentDateTime(userInfo)//objGeneral.getUTCDateTime()
//						+ "'dregdate,sllarno,sllparamkey,sllinterstatus,sllparamtype,sllcomponentname,slltestname,sllparametername,"
//						+ " sproductname,sunitname,smanufname,smanuflotno,smethodname,srefproductgroupname,susername,sclientname,"+userInfo.getNtranssitecode()+",nstatus"
//						+ " from llinter where ntransactiontestcode in(" + ntransactionTestCode + ")"
//						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+ " and   nsitecode="+userInfo.getNtranssitecode()
//						+ " order by npreregno,ntransactiontestcode,ntransactionresultcode;";
//
//				labSheetMaster = "insert into sdmslabsheetmaster(ntransactiontestcode,npreregno,sarno,"
//						+ "ntransactionsamplecode,ntestgrouptestcode, "
//						+ " ntestcode,nretestno,ntestrepeatcount,sllinterstatus,nusercode,nuserrolecode,nsitecode,nstatus)"
//						+
//
//						" select " + testSeqNo + "+rank()over(order by  ntransactiontestcode) ntransactiontestcode, "
//						//+ newTransTestCode + " ntransactiontestcode ,"
//						+ " rt.npreregno,ra.sarno,rt.ntransactionsamplecode,"
//						+ " rt.ntestgrouptestcode,rt.ntestcode,rt.ntestretestno nretestno,rt.ntestrepeatno ntestrepeatcount,"
//						+
//
//						" 'A' sllinterstatus," + userInfo.getNusercode() + " nusercode," + userInfo.getNuserrole()
//						+ " nuserrolecode,"+userInfo.getNtranssitecode()+",1 nstatus"
//						+ " from registration r,registrationarno ra,registrationtest rt,instrumentcategory ic"
//						+ " where " + " rt.ntransactiontestcode in (" + ntransactionTestCode
//						+ ") and ic.ninstrumentcatcode = rt.ninstrumentcatcode"
//						
//					    + " and   r.nsitecode=ra.nsitecode"
//					    + " and   ra.nsitecode=rt.nsitecode"
//						+ " and   rt.nsitecode="+userInfo.getNtranssitecode()
//						+ " and rt.npreregno = r.npreregno and r.npreregno = ra.npreregno and rt.ninstrumentcatcode > 0 "
//						//+ " and ic.ninterfacetypecode = " + Enumeration.InterFaceType.LOGILAB.getInterFaceType() + " "
//						+ " and rt.nstatus = ra.nstatus and ra.nstatus = r.nstatus and r.nstatus = 1;";
//
//
//				labSheetDetails = "insert into sdmslabsheetdetails (ntransactionresultcode,npreregno,sarno,ntransactionsamplecode,ntransactiontestcode, "
//						+ " ntestgrouptestcode,ntestcode,nretestno,ntestrepeatcount,ntestgrouptestparametercode,ntestparametercode,nparametertypecode,"
//						+ " nroundingdigits,sresult,sllinterstatus,sfileid,nlinkcode,nattachedlink,nsitecode,nstatus)" +
//
//						" select " + registrationParamSeq
//						+ "+rank()over(order by ntransactionresultcode) ntransactionresultcode, "
//						+ " rp.npreregno,ra.sarno,rt.ntransactionsamplecode, rt.ntransactiontestcode," //+ testSeqNo
//						//+ "+rank()over(order by rt.ntransactiontestcode) ntransactiontestcode,"
//						+ " rt.ntestgrouptestcode,rt.ntestcode,rt.ntestretestno nretestno,rt.ntestrepeatno ntestrepeatcount,"
//						+ " rp.ntestgrouptestparametercode, rp.ntestparametercode, rp.nparametertypecode,0, "
//						+ " NULL sresult, 'A' sllinterstatus, NULL sfileid,-1 nlinkcode,-1 nattachedlink,"+userInfo.getNtranssitecode()+",rp.nstatus"
//						+ " from registration r,registrationarno ra,registrationparameter rp,registrationtest rt,instrumentcategory ic"
//						+ " where rp.ntransactiontestcode = rt.ntransactiontestcode " +
//
//						" and rt.ntransactiontestcode in (" + ntransactionTestCode + ")"
//						 + " and   r.nsitecode=ra.nsitecode"
//					    + " and   ra.nsitecode=rp.nsitecode"
//					    + " and   rp.nsitecode=rt.nsitecode"
//						+ " and   rt.nsitecode="+userInfo.getNtranssitecode()
//						+ " and r.npreregno = ra.npreregno and ra.npreregno = rp.npreregno  and rt.ninstrumentcatcode > 0 and ic.ninstrumentcatcode = rt.ninstrumentcatcode"
//						+ " and r.nstatus = ra.nstatus and ra.nstatus = rp.nstatus "
//						//+ " and ic.ninterfacetypecode = "+ Enumeration.InterFaceType.LOGILAB.getInterFaceType() + " " + " "
//								+ " and rp.nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			}
			String sScheduleSkip = scheduleSkip();
			
			if (sScheduleSkip.equals(String.valueOf(Enumeration.TransactionStatus.YES.gettransactionstatus()))
					&& !(boolean) inputMap.get("nneedmyjob") && (boolean) inputMap.get("nneedjoballocation")) {
				String jobAllocationInserts = "insert into joballocation (njoballocationcode,npreregno,ntransactionsamplecode,ntransactiontestcode,"
						+ " ntestrescheduleno,nsectioncode,nuserrolecode,nusercode,"
						+ " nuserperiodcode,ninstrumentcategorycode,ninstrumentcode,ninstrumentnamecode,"
						+ " ninstrumentperiodcode,ntimezonecode,jsondata,jsonuidata, ntechniquecode,"
						+ " nsitecode,nstatus)" + " select " + jaSeqNo
						+ "+rank()over(order by ntransactiontestcode) njoballocationcode,npreregno,ntransactionsamplecode,"
						+ " " + testSeqNo + "+rank()over(order by ntransactiontestcode) ntransactiontestcode,"
						+ " 0,nsectioncode," + analystRoleCode + " nuserrolecode," + systemUserCode + " nusercode,"
						+ " "+Enumeration.TransactionStatus.NA.gettransactionstatus()+" nuserperiodcode,"+Enumeration.TransactionStatus.NA.gettransactionstatus()+" ninstrumentcategorycode,"
						+ ""+Enumeration.TransactionStatus.NA.gettransactionstatus()+" ninstrumentcode,"+Enumeration.TransactionStatus.NA.gettransactionstatus()+" ninstrumentnamecode,"
						+ " "+Enumeration.TransactionStatus.NA.gettransactionstatus()+" ninstrumentperiodcode,"+Enumeration.TransactionStatus.NA.gettransactionstatus()+" ntimezonecode, json_build_object('duserblockfromdate','"
						+ dateUtilityFunction.instantDateToStringWithFormat(
								dateUtilityFunction.getCurrentDateTime(userInfo), "yyyy-MM-dd HH:mm:ss")
						+ "')::jsonb||json_build_object('duserblocktodate','"
						+ dateUtilityFunction.instantDateToStringWithFormat(
								dateUtilityFunction.getCurrentDateTime(userInfo), "yyyy-MM-dd HH:mm:ss")
						+ "')::jsonb||json_build_object('suserblockfromtime','00:00')::jsonb|| json_build_object('suserblocktotime','00:00')::jsonb||"
						+ " json_build_object('suserholdduration','0')::jsonb|| json_build_object('dinstblockfromdate',NULL)::jsonb||"
						+ " json_build_object('dinstblocktodate',NULL)::jsonb||"
						+ " json_build_object('sinstblockfromtime',NULL)::jsonb||json_build_object('sinstblocktotime',NULL)::jsonb||"
						+ " json_build_object('sinstrumentholdduration',NULL)::jsonb|| json_build_object('scomments','')::jsonb,"
						+ " json_build_object('duserblockfromdate','"
						+ dateUtilityFunction.instantDateToStringWithFormat(
								dateUtilityFunction.getCurrentDateTime(userInfo), "yyyy-MM-dd HH:mm:ss")
						+ "')::jsonb||json_build_object('duserblocktodate','"
						+ dateUtilityFunction.instantDateToStringWithFormat(
								dateUtilityFunction.getCurrentDateTime(userInfo), "yyyy-MM-dd HH:mm:ss")
						+ "')::jsonb||json_build_object('suserblockfromtime','00:00')::jsonb|| json_build_object('suserblocktotime','00:00')::jsonb||"
						+ " json_build_object('suserholdduration','0')::jsonb|| json_build_object('dinstblockfromdate',NULL)::jsonb||"
						+ " json_build_object('dinstblocktodate',NULL)::jsonb||"
						+ " json_build_object('sinstblockfromtime',NULL)::jsonb||json_build_object('sinstblocktotime',NULL)::jsonb||"
						+ " json_build_object('sinstrumentholdduration',NULL)::jsonb|| json_build_object('scomments','')::jsonb,-1,"
						+ userInfo.getNtranssitecode()
						+ " ,1 nstatus from registrationtest where ntransactiontestcode in (" + ntransactionTestCode
						+ ")" + " and nsitecode=" + userInfo.getNtranssitecode() + ";";
				jdbcTemplate.execute(jobAllocationInserts);
			}
			sectionSeqNo++;
			testSeqNo++;
			jaSeqNo++;
			newtestHistorySeqno++;
			registrationParamSeq = registrationParamSeq + nregparametecount;
			llinterSeqNo = llinterSeqNo + llinterParamCount;
			jdbcTemplate.execute(insertSectionhistory);
			jdbcTemplate.execute(newTestInsert);
			jdbcTemplate.execute(retestTestHistory);
			jdbcTemplate.execute(insertRegParameter);
			jdbcTemplate.execute(insertResultParam);
			jdbcTemplate.execute(insertResultComments);
			jdbcTemplate.execute(insertllinter);
			jdbcTemplate.execute(labSheetMaster);
			jdbcTemplate.execute(labSheetDetails);
			if (!jobAllocationInsert.isEmpty()) {
				jdbcTemplate.execute(jobAllocationInsert);
			}
		}

		final String testAuditGet = "select rth.npreregno,rth.ntransactionsamplecode,rth.ntransactionstatus,rth.nusercode,rth.nuserrolecode,"
				+ " rth.ntesthistorycode,rt.ntransactiontestcode,"
				+ " tm.stestsynonym ||' ['||CAST(rt.ntestrepeatno AS character varying(10))||']['||CAST(rt.ntestretestno AS character varying(10))||']' as stestsynonym,"
				+ " rt.ntestgrouptestcode,rar.sarno,ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "' stransdisplaystatus,"
				+ " ur.suserrolename,CONCAT(u.sfirstname,' ',u.slastname) as username,CAST(rth.dtransactiondate AS character varying(100)) stransactiondate"
				+ " from registrationtesthistory rth,registrationtest rt,testmaster tm,registrationarno rar,transactionstatus ts"
				+ " ,users u,userrole ur where rth.ntransactiontestcode=rt.ntransactiontestcode"
				+ " and rt.npreregno = rar.npreregno and tm.ntestcode=rt.ntestcode"
				+ " and ts.ntranscode=rth.ntransactionstatus and u.nusercode=rth.nusercode"
				+ " and ur.nuserrolecode=rth.nuserrolecode" + " and rth.nsitecode=rt.nsitecode"
				+ " and rt.nsitecode=rar.nsitecode" + " and rth.nsitecode=" + userInfo.getNtranssitecode()
				+ " and rar.nsitecode=" + userInfo.getNtranssitecode() + " and rt.nsitecode="
				+ userInfo.getNtranssitecode() + " and u.nsitecode=" + userInfo.getNmastersitecode()
				+ " and ur.nsitecode=" + userInfo.getNmastersitecode() + " and tm.nsitecode="
				+ userInfo.getNmastersitecode() + " and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and ur.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rth.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rar.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.ntesthistorycode = any("
				+ "	select max(ntesthistorycode) ntesthistorycode from registrationtesthistory"
				+ "	where ntransactiontestcode in(" + ntransactionTestCode + ") " + " and nsitecode="
				+ userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by ntransactiontestcode"
				+ "	)";

		final List<RegistrationTestHistory> lstregtest = (List<RegistrationTestHistory>) jdbcTemplate
				.query(testAuditGet, new RegistrationTestHistory());

		insertAuditList.add(lstregtest);

		final int nseqNoRegistrationHistory = (int) inputMap.get("RegistrationHistorySequence");
		final int nseqNosampleHistory = (int) inputMap.get("RegistrationSampleSequence");
		updateSampleStatus(Enumeration.TransactionStatus.RETEST.gettransactionstatus(), npreregno, userInfo, scomments,
				nseqNoRegistrationHistory);
		updateSubSampleStatus(Enumeration.TransactionStatus.RETEST.gettransactionstatus(), ntransactionsamplecode,
				userInfo, scomments, nseqNosampleHistory);

		inputMap.put("ntransactiontestcode", ntransactionTestCode);

		returnMap.putAll((Map<String, Object>) getApprovalSample(inputMap, userInfo).getBody());
		List<Map<String, Object>> sampleList = (List<Map<String, Object>>) returnMap.get("AP_SAMPLE");
		List<Map<String, Object>> subSampleList = (List<Map<String, Object>>) returnMap.get("AP_SUBSAMPLE");
		List<Map<String, Object>> testList = (List<Map<String, Object>>) returnMap.get("AP_TEST");

		returnMap.put("updatedSample", sampleList);
		returnMap.put("updatedSubSample", subSampleList);
		returnMap.put("updatedTest", testList);

		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditOld = new JSONObject();
		JSONObject jsonAuditNew = new JSONObject();

		

		// ALPD-4890, filtered selected record alone to send record to capture audit
		List<String> testArray = Arrays.asList(ntransactionTestCode.split(","));
		List<Map<String, Object>> auditSelectedTestBefore = ((List<Map<String, Object>>) inputMap.get("APSelectedTest"))
				.stream()
				.filter(test -> testArray.stream()
						.anyMatch(vd -> test.get("ntransactiontestcode").toString().equals(vd)))
				.collect(Collectors.toList());

		jsonAuditOld.put("registrationtest", auditSelectedTestBefore);
		
		jsonAuditNew.put("registrationtest", (List<Map<String, Object>>) returnMap.get("AP_TEST"));
		auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
		auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
		auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
		actionType.put("registrationsample", "IDS_RETEST");
		actionType.put("registrationtest", "IDS_RETEST");
		auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, jsonAuditNew, actionType, auditmap, false, userInfo);

		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}


	private void updateSampleStatus1(final int ntransStatus, final String npreregno, final UserInfo userInfo,
			final String scomments) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();

		String status = Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.RETEST.gettransactionstatus();

		if (ntransStatus == Enumeration.TransactionStatus.RETEST.gettransactionstatus()) {
			status = Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","
					+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus();
		}

		final String approvedSampleQuery = "select historycount.npreregno from ("
				+ "		select npreregno,count(ntransactiontestcode) testcount from registrationtest where ntransactiontestcode=any("
				+ "			select ntransactiontestcode from registrationtesthistory "
				+ "			where ntransactionstatus=" + ntransStatus + " and nsitecode=" + userInfo.getNtranssitecode()
				+ " and npreregno in (" + npreregno + ")and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " 		group by npreregno,ntransactiontestcode" + "		)" + " and nsitecode="
				+ userInfo.getNtranssitecode() + "		group by npreregno"
				+ "	)historycount,( select rth.npreregno,count(ntesthistorycode) testcount "
				+ "		from registrationtest rt,registrationtesthistory rth "
				+ "		where ntransactionstatus not in (" + status + ") " + " and rt.nsitecode=rth.nsitecode"
				+ " and rt.nsitecode=" + userInfo.getNtranssitecode()
				+ "		and rt.ntransactiontestcode=rth.ntransactiontestcode "
				+ "		and rth.ntesthistorycode = any( "
				+ "			select max(ntesthistorycode) ntesthistorycode from registrationtesthistory "
				+ "			where npreregno in (" + npreregno + ") and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNtranssitecode()
				+ " 		group by ntransactiontestcode,npreregno) group by rth.npreregno)testcount "
				+ "where historycount.npreregno=testcount.npreregno "
				+ "and historycount.testcount=testcount.testcount; ";

		final String partialSampleQuery = approvedSampleQuery + "select historycount.npreregno from ("
				+ "        select rth.npreregno,count(ntesthistorycode) testcount "
				+ "        from registrationtest rt,registrationtesthistory rth where ntransactionstatus ="
				+ ntransStatus + " and rt.nsitecode=rth.nsitecode" + " and rt.nsitecode=" + userInfo.getNtranssitecode()
				+ " and rth.nsitecode=" + userInfo.getNtranssitecode()
				+ " and rt.ntransactiontestcode=rth.ntransactiontestcode " + " and rth.ntesthistorycode = any( "
				+ " select max(ntesthistorycode) ntesthistorycode from registrationtesthistory "
				+ " where npreregno in (" + npreregno + ") and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  nsitecode="
				+ userInfo.getNtranssitecode() + " group by ntransactiontestcode,npreregno) group by rth.npreregno"
				+ ")historycount,( select rth.npreregno,count(ntesthistorycode) testcount "
				+ "  from registrationtest rt,registrationtesthistory rth " + "  where ntransactionstatus not in ("
				+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.RETEST.gettransactionstatus() + ") " + " and rt.nsitecode=rth.nsitecode"
				+ " and rth.nsitecode=" + userInfo.getNtranssitecode() + " and rt.nsitecode="
				+ userInfo.getNtranssitecode() + " and rt.ntransactiontestcode=rth.ntransactiontestcode "
				+ " and rth.ntesthistorycode = any( "
				+ " select max(ntesthistorycode) ntesthistorycode from registrationtesthistory "
				+ " where npreregno in (" + npreregno + ") and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and  nsitecode="
				+ userInfo.getNtranssitecode() + " group by ntransactiontestcode,npreregno) group by rth.npreregno"
				+ ")testcount " + " where historycount.npreregno=testcount.npreregno "
				+ " and historycount.testcount!=testcount.testcount ;";

		final String getQuery = partialSampleQuery;

		List<?> lstobj = projectDAOSupport.getMultipleEntitiesResultSetInList(getQuery,jdbcTemplate, RegistrationTestHistory.class,
				RegistrationTestHistory.class, SeqNoRegistration.class);

		final List<RegistrationTestHistory> approvedSampleList = objMapper.convertValue(lstobj.get(0),
				new TypeReference<List<RegistrationTestHistory>>() {
				});
		final List<RegistrationTestHistory> partialSampleList = objMapper.convertValue(lstobj.get(1),
				new TypeReference<List<RegistrationTestHistory>>() {
				});

		StringBuilder sb = new StringBuilder();
		StringJoiner joiner = new StringJoiner(",");
		final int seqNoRegistraionHistory = ((List<SeqNoRegistration>) lstobj.get(2)).get(0).getNsequenceno();

		String newPreregno = "";
		String newPreregno1 = "";
		String insertReghistory = "";
		if (approvedSampleList.size() > 0) {
			newPreregno = approvedSampleList.stream().map(objpreregno -> String.valueOf(objpreregno.getNpreregno()))
					.collect(Collectors.joining(","));

			insertReghistory = "insert into registrationhistory (nreghistorycode,npreregno,ntransactionstatus,dtransactiondate,"
					+ "nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments,nsitecode,nstatus) "
					+ "select " + seqNoRegistraionHistory + "+rank()over(order by npreregno) nreghistorycode,npreregno,"
					+ ntransStatus + " ntransactionstatus," + "'" + dateUtilityFunction.getCurrentDateTime(userInfo)// objGeneral.getUTCDateTime()
					+ "'  dtransactiondate," + userInfo.getNusercode() + " nusercode," + userInfo.getNuserrole()
					+ " nuserrolecode," + "" + userInfo.getNdeputyusercode() + "ndeputyusercode,"
					+ userInfo.getNdeputyuserrole() + " ndeputyuserrolecode,N'"
					+ stringUtilityFunction.replaceQuote(scomments) + "' scomments," + userInfo.getNtranssitecode()
					+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
					+ "from registration where npreregno in (" + newPreregno + ") and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNtranssitecode() + " order by npreregno; update seqnoregistration set nsequenceno="
					+ (seqNoRegistraionHistory + approvedSampleList.size())
					+ " where stablename='registrationhistory';";

			sb.append(insertReghistory);
			joiner.add(newPreregno);
		}
		if (partialSampleList.size() > 0) {
			newPreregno1 = partialSampleList.stream().map(objpreregno -> String.valueOf(objpreregno.getNpreregno()))
					.collect(Collectors.joining(","));

			insertReghistory = "insert into registrationhistory (nreghistorycode,npreregno,ntransactionstatus,dtransactiondate,"
					+ "nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments,nsitecode,nstatus) "
					+ "select " + seqNoRegistraionHistory + "+rank()over(order by npreregno) nreghistorycode,npreregno,"
					+ Enumeration.TransactionStatus.PARTIALLY.gettransactionstatus() + " ntransactionstatus," + "'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo)// objGeneral.getUTCDateTime()
					+ "'  dtransactiondate," + userInfo.getNusercode() + " nusercode," + userInfo.getNuserrole()
					+ " nuserrolecode," + "" + userInfo.getNdeputyusercode() + " ndeputyusercode,"
					+ userInfo.getNdeputyuserrole() + " ndeputyuserrolecode,N'"
					+ stringUtilityFunction.replaceQuote(scomments) + "' scomments," + userInfo.getNtranssitecode()
					+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
					+ "from registration where npreregno in (" + newPreregno1 + ") and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNtranssitecode() + " order by npreregno;update seqnoregistration set nsequenceno="
					+ (seqNoRegistraionHistory + partialSampleList.size()) + " where stablename='registrationhistory'";
			sb.append(insertReghistory);
			joiner.add(newPreregno1);
		}

		if (approvedSampleList.size() > 0 || partialSampleList.size() > 0) {
			jdbcTemplate.execute(sb.toString());
		}

	}

	private void updateSampleStatus(final int ntransStatus,  String npreregno, final UserInfo userInfo,
			final String scomments, int seqNoRegistraionHistory) throws Exception {

		String status = Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.RETEST.gettransactionstatus() + ", "
				+ Enumeration.TransactionStatus.RELEASED.gettransactionstatus();

		if (ntransStatus == Enumeration.TransactionStatus.RETEST.gettransactionstatus()) {
			status = Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","
					+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus();
		}
		final String sampleString = String.join(",",
				Stream.of(npreregno.trim().split(",")).collect(Collectors.toSet()));
		npreregno = sampleString;
		final String approvedSampleQuery = "" + "	select historycount.npreregno from ("
				+ "	select npreregno,count(ntransactiontestcode) testcount from registrationtest where ntransactiontestcode=any("
				+ "	select ntransactiontestcode from registrationtesthistory " + "	where  npreregno in (" + npreregno
				+ ")" + " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and  ntesthistorycode in (SELECT max(ntesthistorycode) ntesthistorycode  "
				+ " FROM   registrationtesthistory WHERE  npreregno IN ( " + npreregno + " ) " + " AND nstatus =  "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND nsitecode =    "
				+ userInfo.getNtranssitecode() + " GROUP  BY ntransactiontestcode, "
				+ " npreregno) and ntransactionstatus not in (" + status + ") "
				+ " group by npreregno,ntransactiontestcode" + ")" + "  and nsitecode=" + userInfo.getNtranssitecode()
				+ "	group by npreregno" + "	)historycount,(select rth.npreregno,count(ntesthistorycode) testcount "
				+ "	from registrationtest rt,registrationtesthistory rth " + "	where ntransactionstatus not in ("
				+ status + ") " + " and rth.nsitecode=" + userInfo.getNtranssitecode() + " and rt.nsitecode="
				+ userInfo.getNtranssitecode() + "	and rt.ntransactiontestcode=rth.ntransactiontestcode "
				+ "	and rth.ntesthistorycode = any( select max(ntesthistorycode) ntesthistorycode from registrationtesthistory "
				+ "	where npreregno in (" + npreregno + ") and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  nsitecode="
				+ userInfo.getNtranssitecode() + " 		group by ntransactiontestcode,npreregno)" + " "
				+ " AND ntransactionstatus=" + ntransStatus + " group by rth.npreregno)testcount "
				+ " where historycount.npreregno=testcount.npreregno "
				+ " and historycount.testcount=testcount.testcount; ";
		String retestValidation = "";
		if (ntransStatus == Enumeration.TransactionStatus.RETEST.gettransactionstatus()) {
			retestValidation = " ntransactionstatus not in ("
					+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","
					+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ")";
		} else {
			retestValidation = " ntransactionstatus not in ("
					+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","
					+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ", "
					+ Enumeration.TransactionStatus.RETEST.gettransactionstatus() + ")";
		}

		final String partialSampleQuery = "select historycount.npreregno from ("
				+ " select rth.npreregno,count(ntesthistorycode) testcount from registrationtesthistory rth,registrationhistory rh"
				+ " where rth.ntransactionstatus=" + ntransStatus + "     and rth.npreregno=rh.npreregno"
				+ " and rh.nreghistorycode=any(select max(nreghistorycode) nreghistorycode"
				+ "	from registrationhistory where npreregno in (" + npreregno + ") " + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by npreregno) and ntesthistorycode=any( "
				+ "	select max(ntesthistorycode) ntesthistorycode from registrationtesthistory "
				+ "	where npreregno in  (" + npreregno + ") " + " and nsitecode = " + userInfo.getNtranssitecode() + " "
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "	group by npreregno,ntransactiontestcode" + ") " + " and rh.ntransactionstatus!="
				+ Enumeration.TransactionStatus.PARTIALLY.gettransactionstatus() + ""
				+ " and rth.nsitecode = rh.nsitecode " + " and rh.nsitecode = " + userInfo.getNtranssitecode() + " "
				+ " and rth.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by rth.npreregno" + "	)historycount,(select rth.npreregno,count(ntesthistorycode) testcount "
				+ " from registrationtest rt,registrationtesthistory rth " + " where " + retestValidation + " "
				+ " and rt.ntransactiontestcode=rth.ntransactiontestcode " + " and rth.ntesthistorycode = any( "
				+ " select max(ntesthistorycode) ntesthistorycode from registrationtesthistory "
				+ " where npreregno in (" + npreregno + ") " + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by ntransactiontestcode,npreregno" + ") "
				+ " and rth.nsitecode = rt.nsitecode and rt.nsitecode = " + userInfo.getNtranssitecode() + " "
				+ " and rth.nsitecode = " + userInfo.getNtranssitecode()
				+ "  group by rth.npreregno) testcount where historycount.npreregno=testcount.npreregno "
				+ " and historycount.testcount!=testcount.testcount ;";

		final List<RegistrationTestHistory> approvedSampleList = jdbcTemplate.query(approvedSampleQuery,
				new RegistrationTestHistory());
		final List<RegistrationTestHistory> partialSampleList = jdbcTemplate.query(partialSampleQuery,
				new RegistrationTestHistory());

		

		String newPreregno = "";
		String newPreregno1 = "";
		String insertReghistory = "";

		if (approvedSampleList.size() > 0) {
			newPreregno = approvedSampleList.stream().map(objpreregno -> String.valueOf(objpreregno.getNpreregno()))
					.collect(Collectors.joining(","));

			insertReghistory = "insert into registrationhistory (nreghistorycode,npreregno,ntransactionstatus,dtransactiondate,"
					+ "nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments,nsitecode,nstatus) "
					+ "select " + seqNoRegistraionHistory + "+rank()over(order by npreregno) nreghistorycode,npreregno,"
					+ ntransStatus + " ntransactionstatus," + "'" + dateUtilityFunction.getCurrentDateTime(userInfo)// objGeneral.getUTCDateTime()
					+ "'  dtransactiondate," + userInfo.getNusercode() + " nusercode," + userInfo.getNuserrole()
					+ " nuserrolecode," + "" + userInfo.getNdeputyusercode() + " ndeputyusercode,"
					+ userInfo.getNdeputyuserrole() + " ndeputyuserrolecode,N'"
					+ stringUtilityFunction.replaceQuote(scomments) + "' scomments," + userInfo.getNtranssitecode()
					+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
					+ "from registration where npreregno in (" + newPreregno + ") and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and   nsitecode="
					+ userInfo.getNtranssitecode() + " order by npreregno; ";

			jdbcTemplate.execute(insertReghistory);
			seqNoRegistraionHistory = seqNoRegistraionHistory + newPreregno.split(",").length;

			// ALPD-4941-->Added by Vignesh R(31-01-2025)--Scheduler Configuration screen
			// design
			final String str = "select r.nsampletypecode,rarno.napprovalversioncode as napproveconfversioncode from registration r,registrationarno rarno where r.npreregno in("
					+ newPreregno + ")  " + "  and rarno.npreregno=r.npreregno  " + " and r.nsitecode="
					+ userInfo.getNtranssitecode() + " and r.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rarno.nsitecode="
					+ userInfo.getNtranssitecode() + " and rarno.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " group by r.nsampletypecode,rarno.napprovalversioncode";
			final ApprovalConfigRole objApprovalConfigRole = (ApprovalConfigRole) jdbcUtilityFunction
					.queryForObject(str, ApprovalConfigRole.class, jdbcTemplate);

			String finalLevelQuery = "select nuserrolecode from " + "  approvalconfigrole acr,  transactionstatus ts "
					+ " where  ts.ntranscode = acr.napprovalstatuscode " + "  and ts.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and acr.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "   and nlevelno = 1 "
					+ "  and napproveconfversioncode =" + objApprovalConfigRole.getNapproveconfversioncode() + ";";

			final int finalLevelRoleCode = jdbcTemplate.queryForObject(finalLevelQuery, Integer.class);

			if (finalLevelRoleCode == userInfo.getNuserrole()) {

				Map<String, Object> objMap = new HashMap<>();

				objMap.put("newPreregno", newPreregno);
				objMap.put("nsampletypecode", objApprovalConfigRole.getNsampletypecode());
				objMap.put("napproveconfversioncode", objApprovalConfigRole.getNapproveconfversioncode());

				if (objApprovalConfigRole.getNsampletypecode() == Enumeration.SampleType.INSTRUMENT.getType()) {
					updateInstrumentScheduler(objMap, userInfo);
				} else if (objApprovalConfigRole.getNsampletypecode() == Enumeration.SampleType.MATERIAL.getType()) {

					updateMaterialScheduler(objMap, userInfo);
				}
			}
		}
		if (partialSampleList.size() > 0) {
			newPreregno1 = partialSampleList.stream().map(objpreregno -> String.valueOf(objpreregno.getNpreregno()))
					.collect(Collectors.joining(","));

			insertReghistory = "insert into registrationhistory (nreghistorycode,npreregno,ntransactionstatus,dtransactiondate,"
					+ "nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments,nsitecode,nstatus) "
					+ "select " + seqNoRegistraionHistory + "+rank()over(order by npreregno) nreghistorycode,npreregno,"
					+ Enumeration.TransactionStatus.PARTIALLY.gettransactionstatus() + " ntransactionstatus," + "'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo)// objGeneral.getUTCDateTime()
					+ "'  dtransactiondate," + userInfo.getNusercode() + " nusercode," + userInfo.getNuserrole()
					+ " nuserrolecode," + "" + userInfo.getNdeputyusercode() + " ndeputyusercode,"
					+ userInfo.getNdeputyuserrole() + " ndeputyuserrolecode,N'"
					+ stringUtilityFunction.replaceQuote(scomments) + "' scomments," + userInfo.getNtranssitecode()
					+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
					+ "from registration where npreregno in (" + newPreregno1 + ") and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and   nsitecode="
					+ userInfo.getNtranssitecode() + " order by npreregno;";

			jdbcTemplate.execute(insertReghistory);
		}

	}

	private void updateSectionHistory(final int ntransStatus, final String nsectioncode, final String npreregno,
			final UserInfo userInfo, final String scomments) throws Exception {

		final String approvedSectionQuery = "select historycount.npreregno,historycount.nsectioncode from "
				+ "(select rth.npreregno,count(ntesthistorycode) testcount,rt.nsectioncode from registrationtesthistory rth,registrationtest rt"
				+ " where rth.ntransactiontestcode = rt.ntransactiontestcode and rt.nsectioncode in (" + nsectioncode
				+ ") and rth.ntransactionstatus=" + ntransStatus + " and rth.npreregno in (" + npreregno + ") "
				+ " and rth.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and rth.nsitecode=rt.nsitecode" + " and rt.nsitecode=" + userInfo.getNtranssitecode()
				+ " and rth.nsitecode=" + userInfo.getNtranssitecode()
				+ " group by rth.npreregno,rt.nsectioncode)historycount,"
				+ " (select rth.npreregno,count(ntesthistorycode) testcount,rt.nsectioncode from registrationtest rt,registrationtesthistory rth "
				+ " where ntransactionstatus not in (" + Enumeration.TransactionStatus.REJECTED.gettransactionstatus()
				+ "," + Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ") "
				+ " and rt.ntransactiontestcode=rth.ntransactiontestcode " + " and rth.nsitecode=rt.nsitecode"
				+ " and rt.nsitecode=" + userInfo.getNtranssitecode() + " and rth.nsitecode="
				+ userInfo.getNtranssitecode()
				+ " and rth.ntesthistorycode = any( select max(ntesthistorycode) ntesthistorycode from registrationtesthistory where npreregno in ("
				+ npreregno + ") " + " and  nsitecode=" + userInfo.getNtranssitecode() + " and nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " group by ntransactiontestcode,npreregno) and nsectioncode in (" + nsectioncode
				+ ") and rt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rth.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " group by rth.npreregno,rt.nsectioncode)testcount"
				+ " where historycount.npreregno=testcount.npreregno "
				+ " and historycount.testcount=testcount.testcount; ";

		final String partialSectionQuery = "select historycount.npreregno,historycount.nsectioncode from "
				+ "(select rth.npreregno,count(ntesthistorycode) testcount,rh.nsectioncode from registrationtesthistory rth,registrationsectionhistory rh where rth.ntransactionstatus="
				+ ntransStatus + " "
				+ " and rth.npreregno=rh.npreregno and rh.nsectionhistorycode = any(select max(nsectionhistorycode) nsectionhistorycode from registrationsectionhistory where npreregno in ("
				+ npreregno + ") and nsitecode=" + userInfo.getNtranssitecode() + "  and nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by npreregno,nsectioncode) and rh.ntransactionstatus!= "
				+ Enumeration.TransactionStatus.PARTIALLY.gettransactionstatus() + " and rth.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.nsitecode=rh.nsitecode"
				+ " and rh.nsitecode=" + userInfo.getNtranssitecode()
				+ " group by rth.npreregno,rh.nsectioncode)historycount, "
				+ " (select rth.npreregno,count(ntesthistorycode) testcount,rt.nsectioncode  "
				+ " from registrationtest rt,registrationtesthistory rth " + " where ntransactionstatus not in ("
				+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ") and nsectioncode in ("
				+ nsectioncode + ") "
				+ " and rt.ntransactiontestcode=rth.ntransactiontestcode and rth.ntesthistorycode = any(select max(ntesthistorycode) ntesthistorycode from registrationtesthistory where npreregno in ("
				+ npreregno + ") and nstatus=1 " + " and  nsitecode=" + userInfo.getNtranssitecode()
				+ " group by ntransactiontestcode,npreregno ) and rt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.nsitecode=rt.nsitecode"
				+ " and rt.nsitecode=" + userInfo.getNtranssitecode()
				+ " group by rth.npreregno,rt.nsectioncode)testcount "
				+ " where historycount.npreregno=testcount.npreregno "
				+ " and historycount.testcount!=testcount.testcount ;";

		final String getSeqnoQuery = "select nsequenceno from seqnoregistration where stablename='registrationsectionhistory'"
				+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";

		final List<RegistrationTestHistory> approvedSectionList = jdbcTemplate.query(approvedSectionQuery,
				new RegistrationTestHistory());
		final List<RegistrationTestHistory> partialSectionList = jdbcTemplate.query(partialSectionQuery,
				new RegistrationTestHistory());
		final SeqNoRegistration objSectionHisSeqno = jdbcTemplate.queryForObject(getSeqnoQuery,
				SeqNoRegistration.class);

		StringBuilder sb = new StringBuilder();
		StringJoiner joiner = new StringJoiner(",");
		int seqNoRegistraionSectionHistory = objSectionHisSeqno.getNsequenceno();
		String newPreregno = "";
		String partialPreregno = "";
		String insertReghistory = "";

		if (approvedSectionList.size() > 0) {

			newPreregno = approvedSectionList.stream().map(objpreregno -> String.valueOf(objpreregno.getNpreregno()))
					.collect(Collectors.joining(","));

			insertReghistory = "insert into registrationsectionhistory " + " select " + seqNoRegistraionSectionHistory
					+ "+Rank()over(order by max(ntransactiontestcode)) nsectionhistorycode ,npreregno,nsectioncode,"
					+ ntransStatus + " ntransactionstatus," + userInfo.getNtranssitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  "
					+ " from registrationtest where npreregno in (" + newPreregno + ") and nsectioncode in ("
					+ nsectioncode + ") and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and   nsitecode=" + userInfo.getNtranssitecode()
					+ " group by npreregno,nsectioncode ; update seqnoregistration set nsequenceno="
					+ (seqNoRegistraionSectionHistory + approvedSectionList.size())
					+ " where stablename='registrationsectionhistory' "
					+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";

			seqNoRegistraionSectionHistory = seqNoRegistraionSectionHistory + approvedSectionList.size();

			sb.append(insertReghistory);
			joiner.add(newPreregno);
		}
		if (partialSectionList.size() > 0) {
			partialPreregno = partialSectionList.stream().map(objpreregno -> String.valueOf(objpreregno.getNpreregno()))
					.collect(Collectors.joining(","));

			insertReghistory = "insert into registrationsectionhistory " + " select " + seqNoRegistraionSectionHistory
					+ "+Rank()over(order by max(ntransactiontestcode)) nsectionhistorycode ,npreregno,nsectioncode,"
					+ Enumeration.TransactionStatus.PARTIALLY.gettransactionstatus() + " ntransactionstatus,"
					+ userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " " + " from registrationtest where npreregno in (" + partialPreregno + ") and nsectioncode in ("
					+ nsectioncode + ") and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and   nsitecode=" + userInfo.getNtranssitecode()
					+ " group by npreregno,nsectioncode ; update seqnoregistration set nsequenceno="
					+ (seqNoRegistraionSectionHistory + partialSectionList.size())
					+ " where stablename='registrationsectionhistory' "
					+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
			sb.append(insertReghistory);
			joiner.add(partialPreregno);
		}

		if (approvedSectionList.size() > 0 || partialSectionList.size() > 0) {
			jdbcTemplate.execute(sb.toString());
			final String spreregno = joiner.toString();

			String strobj = "select rsh.npreregno,rsh.ntransactionstatus,rar.sarno,ts.stransdisplaystatus "
					+ "from registrationsectionhistory rsh,registrationarno rar,transactionstatus ts "
					+ "where rar.npreregno = rsh.npreregno " + "and rsh.nsectionhistorycode = any( "
					+ "select max(nsectionhistorycode) from registrationsectionhistory where npreregno in (" + spreregno
					+ ") and nsectioncode in (" + nsectioncode + ") " + " and  nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " group by npreregno,nsectioncode) " + "and rar.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and rsh.nsitecode=rar.nsitecode" + " and rsh.nsitecode=" + userInfo.getNtranssitecode()
					+ " and rar.nsitecode=" + userInfo.getNtranssitecode()
					+ " and ts.ntranscode=rsh.ntransactionstatus and ts.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";

			List<Object> insertAuditList = new ArrayList<>();
			List<String> multiLingualActionList = new ArrayList<>();
			List<RegistrationHistory> lstregtest = (List<RegistrationHistory>) jdbcTemplate.query(strobj,
					new RegistrationHistory());

			if (lstregtest.size() > 0) {
				insertAuditList.add(lstregtest);
				multiLingualActionList.add(
						lstregtest.stream().map(objtregtest -> String.valueOf(objtregtest.getStransdisplaystatus()))
								.collect(Collectors.joining(",")));
				auditUtilityFunction.fnInsertListAuditAction(insertAuditList, 1, null, multiLingualActionList,
						userInfo);
			}
		}
	}

	
	@Override
	public ResponseEntity<Object> updateDecision(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {// --

		final String lockapprovaldecision = " lock  table lockapprovaldecision "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus() + "";
		jdbcTemplate.execute(lockapprovaldecision);

		Map<String, Object> returnMap = new HashMap<>();
		if (inputMap.get("npreregno") == null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTSAMPLEOFSAMEAPPROVALVERSION",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		final String npreregno = (String) inputMap.get("npreregno");
		final String sQuery = "select * from registrationhistory where npreregno " + "in(" + npreregno
				+ ") and ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ""
				+ " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by 1 desc";

		final List<Map<String, Object>> sDesicionStatus = jdbcTemplate.queryForList(sQuery);

		if (!sDesicionStatus.isEmpty()) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_DECIOSIONSTATUSAPPROVED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		final int ntransStatus = (int) inputMap.get("nTransStatus");
		final String scomments = userInfo.getSreason();

		final String query = "select rh.* from registration r,registrationhistory rh "
				+ "where r.npreregno=rh.npreregno and rh.nreghistorycode in "
				+ "(select max(nreghistorycode) from registrationhistory rh1 where rh1.npreregno in (" + npreregno + ")"
				+ " and rh1.nsitecode=" + userInfo.getNtranssitecode() + " group by rh1.npreregno)"
				+ " and r.nsitecode=rh.nsitecode " + " and rh.nsitecode=" + userInfo.getNtranssitecode();

		final List<RegistrationHistory> lstHis = (List<RegistrationHistory>) jdbcTemplate.query(query,
				new RegistrationHistory());

		Boolean b = lstHis.stream().anyMatch(
				x -> x.getNtransactionstatus() == Enumeration.TransactionStatus.CERTIFIED.gettransactionstatus());

		if (!b) {
			final int sampleCount = npreregno.split(",").length;
			int napprovalConfigVersionCode = validateApprovalVersion(npreregno, userInfo);
			if (napprovalConfigVersionCode == -1) {

				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SELECTSAMPLEOFSAMEAPPROVALVERSION",
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);

			}
			if (!inputMap.containsKey("napprovalconfigcode")) {
				final String getApprovalConfig = "select napprovalconfigcode from approvalconfig where nregtypecode="
						+ inputMap.get("nregtypecode") + " and nregsubtypecode=" + inputMap.get("nregsubtypecode")
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				inputMap.put("napprovalconfigcode", jdbcTemplate.queryForObject(getApprovalConfig, Integer.class));
			}
			inputMap.put("napprovalversioncode", String.valueOf(napprovalConfigVersionCode));


			final String getSeqno = "select nsequenceno from seqnoregistration "
					+ " where stablename='registrationdecisionhistory' "
					+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";

			final int seqNoDecisionHistory = jdbcTemplate.queryForObject(getSeqno, Integer.class);

			// ATE234 Janakumar ALPD-5316 Test Approval -> Decision Status there have based
			// on sample type.
			final String insertReghistory = "insert into registrationdecisionhistory(nregdecisionhistorycode,npreregno,ndecisionstatus,dtransactiondate,"
					+ "nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments,nsitecode,nstatus) "
					+ "select " + seqNoDecisionHistory
					+ "+rank()over(order by npreregno) nregdecisionhistorycode,npreregno," + ntransStatus
					+ " ndecisionstatus," + "'" + dateUtilityFunction.getCurrentDateTime(userInfo)// objGeneral.getUTCDateTime()
					+ "'  dtransactiondate," + userInfo.getNusercode() + " nusercode," + userInfo.getNuserrole()
					+ " nuserrolecode," + "" + userInfo.getNdeputyusercode() + " ndeputyusercode,"
					+ userInfo.getNdeputyuserrole() + " ndeputyuserrolecode,N'"
					+ stringUtilityFunction.replaceQuote(scomments) + "' scomments," + userInfo.getNtranssitecode()
					+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
					+ "from registration where  npreregno in (" + npreregno + ") and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and   nsitecode="
					+ userInfo.getNtranssitecode() + " order by npreregno";

			final String updateSeqNo = "update seqnoregistration set nsequenceno="
					+ (seqNoDecisionHistory + sampleCount) + " where stablename='registrationdecisionhistory' "
							+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"" ;
			jdbcTemplate.execute(insertReghistory);
			jdbcTemplate.execute(updateSeqNo);

			List<String> multiLingualActionList = new ArrayList<>();
			inputMap.put("ntransactionstatus", "-1");

			multiLingualActionList.add("IDS_SAMPLEDECISION");
			returnMap.putAll((Map<String, Object>) getApprovalSample(inputMap, userInfo).getBody());

			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CERTIFIEDRECORDCANNOTGIVENDECISION",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	/*
	 * @SuppressWarnings("unchecked")
	 * 
	 * @Override public ResponseEntity<Object> releaseSamples(Map<String, Object>
	 * inputMap, UserInfo userInfo) throws Exception {//--
	 * 
	 * String npreregno = (String) inputMap.get("npreregno"); // String scomments =
	 * (String) inputMap.get("scomments"); String scomments = userInfo.getSreason();
	 * int nsampleTypeCode = (int) inputMap.get("nsampletypecode"); int nsectionCode
	 * = 0; String sQuery = ""; int sampleCount = 0; String sectionCondition = "";
	 * boolean bFlag = true; // final String lockTable =
	 * "select slockdesc from locktestapproval " // +
	 * Enumeration.ReturnStatus.TABLOCK.getreturnstatus() + ""; //
	 * jdbcTemplate.execute(lockTable);
	 * 
	 * if (inputMap.containsKey("nsectioncode")) {
	 * 
	 * nsectionCode = (int) inputMap.get("nsectioncode");
	 * 
	 * }
	 * 
	 * String validateSample =
	 * "select rh.npreregno from  registrationhistory rh where rh.nstatus=1 and nreghistorycode = any("
	 * +
	 * " select max(nreghistorycode) from  registrationhistory where  npreregno in ("
	 * + npreregno + ")" + "  and nsitecode="+userInfo.getNtranssitecode() +
	 * " group by npreregno )" + " and rh.nsitecode="+userInfo.getNtranssitecode() +
	 * " and ntransactionstatus !=" +
	 * Enumeration.TransactionStatus.RELEASED.gettransactionstatus();
	 * 
	 * List<String> nonReleasedSamples = jdbcTemplate.queryForList(validateSample,
	 * String.class);
	 * 
	 * if (nonReleasedSamples.size() == 0) {
	 * 
	 * return new ResponseEntity<>(
	 * commonFunction.getMultilingualMessage("IDS_SAMPLEALREADYRELEASED",
	 * userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
	 * 
	 * } else {
	 * 
	 * npreregno = nonReleasedSamples.stream().collect(Collectors.joining(","));
	 * sampleCount = nonReleasedSamples.size(); } String testValidation = ""; if
	 * (nsectionCode == 0) { // non section wise testValidation =
	 * "select case when " + " (" +
	 * "		select count(ntesthistorycode) as approvedtestcount from registrationtesthistory rth where rth.ntesthistorycode= any( "
	 * +
	 * " 		select max(ntesthistorycode) from registrationtesthistory where npreregno in ("
	 * + npreregno + ") and nstatus= " +
	 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " +
	 * " and  nsitecode="+userInfo.getNtranssitecode() +
	 * " 		group by npreregno,ntransactionsamplecode,ntransactiontestcode) " +
	 * " and rth.nsitecode="+userInfo.getNtranssitecode() +
	 * " 	and rth.ntransactionstatus = (" +
	 * "			select napprovalstatuscode from approvalconfigrole where nstatus= "
	 * + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
	 * " 		and nlevelno=1 and  napproveconfversioncode = " +
	 * inputMap.get("napprovalversioncode") + ")" + " )=( " +
	 * " 	select count(ntesthistorycode) as totaltestcount from registrationtesthistory where ntesthistorycode= any(  "
	 * +
	 * " 		select max(ntesthistorycode) from registrationtesthistory where npreregno in ("
	 * + npreregno + ") and nstatus=" +
	 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " +
	 * " and  nsitecode="+userInfo.getNtranssitecode() +
	 * " 		group by npreregno,ntransactionsamplecode,ntransactiontestcode) " +
	 * " and  nsitecode="+userInfo.getNtranssitecode() +
	 * " 	and ntransactionstatus not in(" +
	 * Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + "," +
	 * Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + "," +
	 * Enumeration.TransactionStatus.RETEST.gettransactionstatus() + ")" + " ) " +
	 * "then 'true' else 'false' end as validatesample";
	 * 
	 * } else { // section wise testValidation = "select case when " + " (" +
	 * " 	select count(ntesthistorycode) as approvedtestcount from registrationtesthistory rth,registrationtest rt"
	 * + " 	where rt.ntransactiontestcode=rth.ntransactiontestcode" +
	 * " and rth.nsitecode=rt.nsitecode" +
	 * " and rt.nsitecode="+userInfo.getNtranssitecode() + " and rt.nsectioncode= "
	 * + nsectionCode + "		and rth.ntesthistorycode= any( " +
	 * " 		select max(ntesthistorycode) from registrationtesthistory where npreregno in ("
	 * + npreregno + ") and nstatus= " +
	 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
	 * " and  nsitecode="+userInfo.getNtranssitecode() +
	 * " 		group by npreregno,ntransactionsamplecode,ntransactiontestcode) " +
	 * " 	and rth.ntransactionstatus = (" +
	 * "			select napprovalstatuscode from approvalconfigrole where nstatus= "
	 * + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
	 * " 		and nlevelno=1 and  napproveconfversioncode = " +
	 * inputMap.get("napprovalversioncode") + ")" + " )=(" +
	 * " 	select count(ntesthistorycode) as totaltestcount from registrationtesthistory rth,registrationtest rt"
	 * + "		where rt.ntransactiontestcode=rth.ntransactiontestcode" +
	 * " and rth.nsitecode=rt.nsitecode" +
	 * " and rt.nsitecode="+userInfo.getNtranssitecode() +
	 * "		and rt.nsectioncode= " + nsectionCode +
	 * "		and ntesthistorycode= any(  " +
	 * " 		select max(ntesthistorycode) from registrationtesthistory where npreregno in ("
	 * + npreregno + ") and nstatus=" +
	 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
	 * " and  nsitecode="+userInfo.getNtranssitecode() +
	 * "		 	group by npreregno,ntransactionsamplecode,ntransactiontestcode) "
	 * + " 	and ntransactionstatus not in(" +
	 * Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + "," +
	 * Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + "," +
	 * Enumeration.TransactionStatus.RETEST.gettransactionstatus() + ")" + " ) " +
	 * " then 'true' else 'false' end as validatesample";
	 * 
	 * } boolean testsApproved = jdbcTemplate.queryForObject(testValidation,
	 * Boolean.class);
	 * 
	 * StringBuilder sb = new StringBuilder();
	 * 
	 * if (testsApproved) {
	 * 
	 * if (nsectionCode > 0) {
	 * 
	 * final String nonSectionReleasedSamples =
	 * "select npreregno from registrationsectionhistory " +
	 * " where  nsectionhistorycode = any( " +
	 * " 	select max(nsectionhistorycode) as nsectionhistorycode from registrationsectionhistory"
	 * + " 	where npreregno in(" + npreregno + ")  and nsectioncode = " +
	 * nsectionCode + "		and nstatus =" +
	 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
	 * " and  nsitecode="+userInfo.getNtranssitecode() + " 	group by npreregno)" +
	 * " and  nsitecode="+userInfo.getNtranssitecode() +
	 * " and ntransactionstatus != " +
	 * Enumeration.TransactionStatus.RELEASED.gettransactionstatus() + ";";
	 * 
	 * List<String> nonSectionReleasedSampleList =
	 * jdbcTemplate.queryForList(nonSectionReleasedSamples, String.class);
	 * 
	 * if (nonSectionReleasedSampleList.size() > 0) {
	 * 
	 * sectionCondition = " and rsh.nsectioncode= " + nsectionCode; npreregno =
	 * nonSectionReleasedSampleList.stream().collect(Collectors.joining(","));
	 * 
	 * // int seqNoSectionHistory = jdbcTemplate.
	 * queryForObject("select nsequenceno from seqnoregistration where stablename = 'registrationsectionhistory' "
	 * , Integer.class); // // final String
	 * sectionHistoryInsert="insert into registrationsectionhistory (nsectionhistorycode,npreregno,nsectioncode,ntransactionstatus,nstatus) "
	 * // + " select "+(seqNoSectionHistory)
	 * +"+rank() over(order by npreregno),npreregno,"+nsectionCode+" nsectioncode,"
	 * +Enumeration.TransactionStatus.RELEASED.gettransactionstatus()
	 * +" ntransactionstatus,"+Enumeration.TransactionStatus.ACTIVE.
	 * gettransactionstatus()+" nstatus " // +
	 * " from registration where npreregno in ("+npreregno+") order by npreregno";
	 * // jdbcTemplate.execute(sectionHistoryInsert); // // final String
	 * updateSeqNoSectionHistory="update seqnoregistration set nsequenceno ="+(
	 * seqNoSectionHistory+nonSectionReleasedSampleList.size())
	 * +" where stablename = 'registrationsectionhistory'"; //
	 * jdbcTemplate.execute(updateSeqNoSectionHistory);
	 * 
	 * final String sectionCountQry = "  select releasedsections.npreregno from" +
	 * "(" + "	select npreregno,count(npreregno)as sectioncount " +
	 * "	from registrationsectionhistory " + "	where npreregno in (" +
	 * npreregno + ")" + "	and nstatus =" +
	 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
	 * " and  nsitecode="+userInfo.getNtranssitecode() +
	 * "  and ntransactionstatus =" +
	 * Enumeration.TransactionStatus.RELEASED.gettransactionstatus() +
	 * "  group by npreregno" + ") releasedsections," + "(" +
	 * "	select  distinct rt.npreregno,count(distinct(rt.nsectioncode))as sectioncount"
	 * + "	from registrationtesthistory rth,registrationtest rt" +
	 * "	where rth.ntesthistorycode in (" +
	 * "		select max(ntesthistorycode) as testhistorycode  " +
	 * "		from registrationtesthistory  where npreregno in (" + npreregno +
	 * ") and nstatus= " +
	 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
	 * " and  nsitecode="+userInfo.getNtranssitecode() +
	 * "		group by npreregno,ntransactionsamplecode,ntransactiontestcode" +
	 * "	) "
	 * 
	 * + " and rth.nsitecode=rt.nsitecode" +
	 * " and rt.nsitecode="+userInfo.getNtranssitecode()
	 * 
	 * + "	and rt.ntransactiontestcode=rth.ntransactiontestcode" +
	 * "	and rth.nstatus = " +
	 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
	 * " group by rt.npreregno " + ") totalsections" +
	 * "	where releasedsections.npreregno=totalsections.npreregno " +
	 * "	and releasedsections.sectioncount=totalsections.sectioncount";
	 * 
	 * List<String> filteredSamples = jdbcTemplate.queryForList(sectionCountQry,
	 * String.class); npreregno =
	 * filteredSamples.stream().collect(Collectors.joining(",")); sampleCount =
	 * filteredSamples.size(); if (filteredSamples.size() > 0) { bFlag = true; }
	 * else { bFlag = false; } } else {
	 * 
	 * return new ResponseEntity<>(commonFunction.getMultilingualMessage(
	 * "IDS_SECTIONISALREADYRELEASED", userInfo.getSlanguagefilename()),
	 * HttpStatus.EXPECTATION_FAILED);
	 * 
	 * } } if (bFlag) {
	 * 
	 * final String seqnoRegGet =
	 * "select nsequenceno from SeqNoRegistration where stablename = 'registrationhistory' "
	 * ; int seqNoRegHistory = jdbcTemplate.queryForObject(seqnoRegGet,
	 * Integer.class);
	 * 
	 * sQuery =
	 * "  insert into registrationhistory (nreghistorycode,npreregno,ntransactionstatus,dtransactiondate,nusercode,nuserrolecode,"
	 * + " ndeputyusercode,ndeputyuserrolecode,scomments,nsitecode,nstatus)" +
	 * " select " + seqNoRegHistory +
	 * "+rank()over(order by npreregno) nreghistorycode,npreregno," +
	 * Enumeration.TransactionStatus.RELEASED.gettransactionstatus() +
	 * " ntransactionstatus,'" +
	 * dateUtilityFunction.getCurrentDateTime(userInfo)//objGeneral.getUTCDateTime()
	 * + "' dtransactiondate," + " " + userInfo.getNusercode() + " nusercode," +
	 * userInfo.getNuserrole() + " nuserrolecode," + userInfo.getNdeputyusercode() +
	 * " ndeputyusercode," + "" + userInfo.getNdeputyuserrole() +
	 * " ndeputyuserrolecode,N'" + stringUtilityFunction.replaceQuote(scomments) +
	 * "' scomments, " +userInfo.getNtranssitecode() +"," +
	 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus " +
	 * " from registration where npreregno in  (" + npreregno + ") and nstatus=" +
	 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
	 * " and   nsitecode="+userInfo.getNtranssitecode() + " order by npreregno;";
	 * sb.append(sQuery);
	 * 
	 * sQuery = "Update seqnoregistration set nsequenceno = " + (seqNoRegHistory +
	 * sampleCount) + " where stablename = 'registrationhistory';";
	 * sb.append(sQuery);
	 * 
	 * if (nsampleTypeCode == Enumeration.SampleType.INSTRUMENT.getType()) {
	 * 
	 * sQuery = "update instrumentcalibration set dclosedate='" +
	 * dateUtilityFunction.getCurrentDateTime(userInfo)
	 * //objGeneral.getUTCDateTime() + "'" + " ,ncloseusercode=" +
	 * userInfo.getNusercode() + "," + "sclosereason=N'" +
	 * Enumeration.ReturnStatus.CALIBRATED.getreturnstatus() + "' " +
	 * ",ncalibrationstatus=" +
	 * Enumeration.TransactionStatus.CALIBIRATION.gettransactionstatus() +
	 * ", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(userInfo)+
	 * "' where npreregno in (" + npreregno + ");"; sb.append(sQuery); String scount
	 * =
	 * "select count(ac.napprovalconfigcode) count from approvalconfig ac,approvalconfigversion acv,sampleregistration sr "
	 * +
	 * "where ac.napprovalconfigcode=acv.napprovalconfigcode and acv.napproveconfversioncode=sr.napprovalversioncode "
	 * + "and ac.nschedulertypecode=1 and sr.npreregno in (" + npreregno + ")"; int
	 * ncount1 = jdbcTemplate.queryForObject(scount, Integer.class); if (ncount1 >
	 * 0) {
	 * 
	 * sQuery = "update exscheduledetails set ntranscode=" +
	 * Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() + " " +
	 * " where npreregno in (" + npreregno + ") and ntranscode = " +
	 * Enumeration.TransactionStatus.PARTIALLY.gettransactionstatus();
	 * sb.append(sQuery); }
	 * 
	 * } else if (nsampleTypeCode == Enumeration.SampleType.MATERIAL.getType()) {
	 * 
	 * sQuery = "update materialinventory set ntransactionstatus = " +
	 * Enumeration.TransactionStatus.RELEASED.gettransactionstatus() +
	 * ", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(userInfo) +
	 * "' where nmaterialinventorycode in (select nmaterialinventorycode from registrationmaterial where npreregno in ("
	 * + npreregno + "));"; sb.append(sQuery);
	 * 
	 * } jdbcTemplate.execute(sb.toString());
	 * 
	 * String strobj =
	 * "select rh.npreregno,rh.ntransactionstatus,rar.sarno,ts.stransdisplaystatus,ur.suserrolename,"
	 * +
	 * " (u.sfirstname+' '+u.slastname) as username,convert(nvarchar(100),rh.dtransactiondate) stransactiondate "
	 * +
	 * " from registrationhistory rh,registrationarno rar,transactionstatus ts,users u,userrole ur"
	 * + " where rar.npreregno = rh.npreregno " + " and rh.nsitecode=rar.nsitecode"
	 * + " and rar.nsitecode="+userInfo.getNtranssitecode() +
	 * " and rh.nreghistorycode = any(" +
	 * " 	 select max(nreghistorycode) from registrationhistory where npreregno in ("
	 * + npreregno + ")" + "	 and nstatus = " +
	 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
	 * " and  nsitecode="+userInfo.getNtranssitecode() + "	 group by npreregno)" +
	 * " and rar.nstatus=" +
	 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
	 * " and ts.ntranscode=rh.ntransactionstatus" + " and ts.nstatus=" +
	 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
	 * " and u.nusercode=rh.nusercode" + " and u.nstatus=" +
	 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
	 * " and ur.nuserrolecode=rh.nuserrolecode" + " and ur.nstatus=" +
	 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	 * 
	 * List<Object> insertAuditList = new ArrayList<>(); List<String>
	 * multiLingualActionList = new ArrayList<>(); List<RegistrationHistory>
	 * lstregtest = (List<RegistrationHistory>) jdbcTemplate.query(strobj, new
	 * RegistrationHistory()); insertAuditList.add(lstregtest);
	 * multiLingualActionList.add("IDS_RELEASESAMPLE");
	 * auditUtilityFunction.auditUtilityFunction.fnInsertListAuditAction(
	 * insertAuditList, 1, null, multiLingualActionList, userInfo); } final String
	 * releaseParamInsert =
	 * "insert into releaseparameter (ntransactionresultcode,npreregno,ntransactiontestcode,ntestgrouptestparametercode,ntestparametercode,nparametertypecode,nroundingdigits,"
	 * +
	 * "nresultmandatory,nreportmandatory,ntestgrouptestformulacode,nunitcode,ngradecode,ntransactionstatus,sfinal,sresult,nenforcestatus,nenforceresult,"
	 * +
	 * "smina,sminb,smaxb,smaxa,sminlod,smaxlod,sminloq,smaxloq,sdisregard,sresultvalue,dentereddate,nenteredby,nenteredrole,ndeputyenteredby,ndeputyenteredrole,nfilesize,sfileid,nlinkcode,nattachmenttypecode"
	 * + "nstatus)" +
	 * "select distinct ap.ntransactionresultcode,ap.npreregno,ap.ntransactiontestcode,ap.ntestgrouptestparametercode,ap.ntestparametercode,ap.nparametertypecode,ap.nroundingdigits,"
	 * +
	 * "ap.nresultmandatory,ap.nreportmandatory,ap.ntestgrouptestformulacode,ap.nunitcode,ap.ngradecode,ap.ntransactionstatus,ap.sfinal,ap.sresult, ap.nenforcestatus,ap.nenforceresult,"
	 * +
	 * "smina,sminb,smaxb,smaxa,sminlod,smaxlod,sminloq,smaxloq,sdisregard,sresultvalue,'"
	 * +
	 * dateUtilityFunction.getCurrentDateTime(userInfo)//objGeneral.getUTCDateTime()
	 * + "' dentereddate," + userInfo.getNusercode() + " nenteredby," +
	 * userInfo.getNuserrole() + " nenteredrole," + userInfo.getNdeputyusercode() +
	 * " ndeputyenteredby," + userInfo.getNdeputyuserrole() + " ndeputyenteredrole,"
	 * + ",nfilesize,sfileid,nlinkcode,nattachmenttypecode," +
	 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus" +
	 * " from registrationsectionhistory rsh,registrationtest rt,approvalparameter ap,registrationtesthistory rth"
	 * + " where rsh.npreregno in (" + npreregno + ")" + sectionCondition +
	 * " and rth.ntransactionstatus in (" +
	 * "		select acr.napprovalstatuscode as ntransactionstatus from ApprovalConfigRole acr"
	 * + "		where acr.napproveconfversioncode =" +
	 * inputMap.get("napprovalversioncode") +
	 * " and acr.nlevelno = 1 and acr.nstatus =" +
	 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " )" +
	 * " and rth.npreregno=rsh.npreregno and rt.npreregno = rth.npreregno" +
	 * " and rt.ntransactiontestcode = rth.ntransactiontestcode and rt.nsectioncode=rsh.nsectioncode"
	 * +
	 * " and ap.ntransactiontestcode = rt.ntransactiontestcoden and ap.npreregno=rsh.npreregno"
	 * +
	 * " and ap.ntransactiontestcode not in(select ntransactiontestcode from releaseparameter where npreregno in ("
	 * + npreregno + ") and nstatus=" +
	 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
	 * jdbcTemplate.execute(releaseParamInsert);
	 * 
	 * return new ResponseEntity<>(HttpStatus.OK); } else { final String
	 * alertStatusGet =
	 * "select ts.stransdisplaystatus from approvalconfigrole acr,transactionstatus ts"
	 * + " where  ts.ntranscode = acr.napprovalstatuscode" + " and ts.nstatus=" +
	 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
	 * " and acr.nstatus=" +
	 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
	 * " and nlevelno=1 and napproveconfversioncode=" +
	 * inputMap.get("napprovalversioncode"); final String alertStatus =
	 * jdbcTemplate.queryForObject(alertStatusGet, String.class); final String
	 * returnAlert = commonFunction.getMultilingualMessage(alertStatus,
	 * userInfo.getSlanguagefilename()) + " " +
	 * commonFunction.getMultilingualMessage("IDS_ALLTESTTORELEASETHESAMPLE",
	 * userInfo.getSlanguagefilename()); return new ResponseEntity<>(returnAlert,
	 * HttpStatus.EXPECTATION_FAILED); } }
	 */

	@Override
	public int validateApprovalVersion(final String npreregno, final UserInfo userInfo) throws Exception {

		final String str = "select case when count(distinct napprovalversioncode)=1	then  (max(distinct napprovalversioncode)) else -1 end "
				+ " from registrationarno where npreregno in (" + npreregno + ") and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  nsitecode="
				+ userInfo.getNtranssitecode();
		int napprovalVersionCode = jdbcTemplate.queryForObject(str, Integer.class);

		return napprovalVersionCode;

	}

	
	
	@Override
	public ResponseEntity<Object> updateEnforceStatus(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final String lockresultparamcomment = " lock  table lockresultparamcomment "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus() + "";
		jdbcTemplate.execute(lockresultparamcomment);
		List<Object> insertAuditList = new ArrayList<>();
		final int ngradeCode = (int) inputMap.get("ngradecode");
		final int ntransactionTestCode = (int) inputMap.get("ntransactiontestcode");
		final int ntransactionResultCode = (int) inputMap.get("ntransactionresultcode");
		final String senforceStatusComment = (String) inputMap.get("senforcestatuscomment");
		final Boolean nneedsubsample = (Boolean) inputMap.get("nneedsubsample");

		final Map<String, Object> returnMap = new HashMap<>();

		final String startedTestGet = " select ntesthistorycode, nformcode, npreregno, ntransactionsamplecode, ntransactiontestcode, "
				+ " nusercode, nuserrolecode, ndeputyusercode,"
				+ " ndeputyuserrolecode, nsampleapprovalhistorycode, ntransactionstatus, "
				+ " dtransactiondate, noffsetdtransactiondate, ntransdatetimezonecode,"
				+ " scomments, nsitecode, nstatus from registrationtesthistory where ntesthistorycode=(select max(ntesthistorycode) from registrationtesthistory"
				+ " where ntransactiontestcode=" + ntransactionTestCode + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNtranssitecode() + " and ntransactionstatus in("
				+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() + ")) and nsitecode="
				+ userInfo.getNtranssitecode();

		List<RegistrationTestHistory> completedTest = (List<RegistrationTestHistory>) jdbcTemplate.query(startedTestGet,
				new RegistrationTestHistory());

		if (completedTest.size() > 0) {

			final String getSeqNo = "select nsequenceno from seqnoregistration "
					+ " where stablename='resultparamcommenthistory' "
					+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
			int nseqnoCommentHistory = jdbcTemplate.queryForObject(getSeqNo, Integer.class);
			nseqnoCommentHistory++;

			final String resultParameterComment = "select ap.ntransactionresultcode,ap.ntransactiontestcode,g.sgradename,rarno.sarno,"
					+ " ap.ntestgrouptestparametercode,ap.ntestparametercode," + " case when " + nneedsubsample
					+ " = true then rs.ssamplearno else '-' end as subsample,"
					+ " ap.npreregno,ap.ngradecode,(rpc.jsondata->>'senforcestatuscomment')::character varying senforcestatuscomment"
					+ " from approvalparameter ap, resultparametercomments rpc,grade g,registrationarno rarno, registrationsamplearno rs,  registrationtest rt "
					+ " where ap.ntransactionresultcode=rpc.ntransactionresultcode and g.ngradecode=ap.ngradecode "
					+ " and ap.nsitecode=rpc.nsitecode" + " and rpc.nsitecode=rarno.nsitecode"
					+ " and rarno.nsitecode=rs.nsitecode" + " and rs.nsitecode=rt.nsitecode" + " and rt.nsitecode="
					+ userInfo.getNtranssitecode() + " and ap.nsitecode=" + userInfo.getNtranssitecode()
					+ " and rpc.nsitecode=" + userInfo.getNtranssitecode() + " and rarno.nsitecode="
					+ userInfo.getNtranssitecode()
					+ " and rarno.npreregno=ap.npreregno and rs.npreregno=rarno.npreregno and rt.ntransactiontestcode=ap.ntransactiontestcode and rs.ntransactionsamplecode=rt.ntransactionsamplecode  and ap.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and rs.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rt.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and g.nstatus  = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rarno.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rpc.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ap.ntransactionresultcode="
					+ ntransactionResultCode;

			final ApprovalParameter objResultParameterComment = (ApprovalParameter) jdbcUtilityFunction
					.queryForObject(resultParameterComment, ApprovalParameter.class, jdbcTemplate);

			insertAuditList.add(objResultParameterComment);
			String rpchQueryString = "";
			if (objResultParameterComment != null) {
				final String approvalPramUpdate = " update approvalparameter set nenforcestatus="
						+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ",ngradecode=" + ngradeCode
						+ " where ntransactionresultcode=" + ntransactionResultCode + ";";

				final String resultCommentUpdate = " update resultparametercomments set jsondata = jsondata||json_build_object('senforcestatuscomment','"
						+ stringUtilityFunction.replaceQuote(senforceStatusComment) + "')::jsonb "
						+ " where ntransactionresultcode=" + objResultParameterComment.getNtransactionresultcode()
						+ ";";

				if (objResultParameterComment.getSenforcestatuscomment() != null) {

					rpchQueryString = " insert into resultparamcommenthistory(nresultparamcommenthistorycode,ntransactionresultcode "
							+ " ,ntransactiontestcode, npreregno ,ntestgrouptestparametercode,ntestparametercode,jsondata,nsitecode,nstatus)"
							+ " values(" + nseqnoCommentHistory + ","
							+ objResultParameterComment.getNtransactionresultcode() + ","
							+ objResultParameterComment.getNtransactiontestcode() + " ,"
							+ objResultParameterComment.getNpreregno() + ","
							+ objResultParameterComment.getNtestgrouptestparametercode() + ","
							+ objResultParameterComment.getNtestparametercode() + " ,"
							+ "json_build_object('senforcestatuscomment','"
							+ stringUtilityFunction.replaceQuote(senforceStatusComment) + "')::jsonb,"
							+ userInfo.getNtranssitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "); ";

					rpchQueryString += " update seqnoregistration set nsequenceno=" + nseqnoCommentHistory
							+ " where stablename='resultparamcommenthistory' "
							+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
				}

				final String finalQuery = approvalPramUpdate + resultCommentUpdate + rpchQueryString;
				jdbcTemplate.execute(finalQuery);

				final String strobj = "select ap.ntransactionresultcode,ap.ntransactiontestcode,g.sgradename,rarno.sarno,"
						+ " ap.ntestgrouptestparametercode,ap.npreregno,ap.ngradecode,"
						+ " rpc.jsondata->>'senforcestatuscomment' senforcestatuscomment"
						+ " from approvalparameter ap, resultparametercomments rpc,grade g,registrationarno rarno "
						+ " where ap.ntransactionresultcode=rpc.ntransactionresultcode and g.ngradecode=ap.ngradecode"
						+ " and rarno.npreregno=ap.npreregno and ap.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ap.nsitecode=rpc.nsitecode" + " and rpc.nsitecode=rarno.nsitecode"
						+ " and rarno.nsitecode=" + userInfo.getNtranssitecode() + " "
						+ " and ap.nsitecode=" + userInfo.getNtranssitecode()
						+ " and rpc.nsitecode=" + userInfo.getNtranssitecode() + " and rpc.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and g.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rarno.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " and ap.ntransactionresultcode=" + ntransactionResultCode;
				final List<ApprovalParameter> auditList = (List<ApprovalParameter>) jdbcTemplate.query(strobj,
						new ApprovalParameter());
				Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
				JSONObject actionType = new JSONObject();
				JSONObject jsonAuditOld = new JSONObject();
				JSONObject jsonAuditNew = new JSONObject();
				jsonAuditOld.put("approvalparameter", insertAuditList);
				jsonAuditNew.put("approvalparameter", auditList);
				auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
				auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
				auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
				actionType.put("approvalparameter", "IDS_ENFORCESTATUS");
				auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, jsonAuditNew, actionType, auditmap, false,
						userInfo);
			}

			// ALPD-1413
			final String getapprovalparam = "select count(0) from approvalparameter ap where ap.npreregno = "
					+ completedTest.get(0).getNpreregno() + " and ap.nsitecode = " + userInfo.getNtranssitecode()
					+ " and ap.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ap.ngradecode in (" + Enumeration.Grade.OOS.getGrade() + ", "
					+ Enumeration.Grade.OOT.getGrade() + ");";

			int count = jdbcTemplate.queryForObject(getapprovalparam, Integer.class);
			boolean flag = count > 0 ? true : false;

			String updateSampleflagstatus = "update registrationflagstatus set bflag = " + flag + " where"
					+ " npreregno = (" + completedTest.get(0).getNpreregno() + ") and nsitecode = "
					+ userInfo.getNtranssitecode() + "";

			jdbcTemplate.execute(updateSampleflagstatus);

			final String Testhistory = " select ntransactionstatus,ntransactionsamplecode from registrationtesthistory where ntesthistorycode=(select max(ntesthistorycode) from registrationtesthistory"
					+ " where ntransactiontestcode=" + ntransactionTestCode + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNtranssitecode() + ") and nsitecode=" + userInfo.getNtranssitecode();

			RegistrationTestHistory objCommentsHistory = (RegistrationTestHistory) jdbcUtilityFunction
					.queryForObject(Testhistory, RegistrationTestHistory.class, jdbcTemplate);
			inputMap.put("ntransactionsamplecode", objCommentsHistory.getNtransactionsamplecode());

			returnMap.putAll(
					(Map<String, Object>) getApprovalParameter((String) inputMap.get("selectedTestCode"), userInfo)
							.getBody());
			returnMap.putAll((Map<String, Object>) getApprovalSample(inputMap, userInfo).getBody());
			return new ResponseEntity<>(returnMap, HttpStatus.OK);

		} else {
			return new ResponseEntity<>("IDS_SELECTCOMPLETEDTEST", HttpStatus.EXPECTATION_FAILED);
		}

	}

	@Override
	public ResponseEntity<Map<String, Object>> getApprovalParameter(final String ntransactionTestCode,final UserInfo userInfo)
			throws Exception {
		System.out.println("Parameter start=>" + Instant.now());
		final ObjectMapper objMapper=new ObjectMapper();	
		/*
		 * final String getApprovalParamQry = "select POSITION('"+ ntransactionTestCode
		 * + "' in CAST(rt.ntransactiontestcode AS character varying)) parameterorder,"
		 * +
		 * " ap.ntransactionresultcode,ap.nparametertypecode,rt.ntransactiontestcode,rt.npreregno,rt.ntransactionsamplecode,ap.ngradecode,-1 nchecklistversioncode,ap.nlinkcode,ap.nattachmenttypecode,ap.jsondata->>'ssystemfilename',"
		 * +
		 * " rar.sarno,sar.ssamplearno,rt.jsondata->>'stestsynonym' stestsynonym,ap.nparametertypecode,"
		 * + " ap.jsondata->>'sparametersynonym' sparametersynonym,pt.sdisplaystatus," +
		 * "  case when u.nunitcode =-1 then ap.jsondata->>'sfinal'::character varying else CONCAT(ap.jsondata->>'sfinal',' ',u.sunitname) end sfinal ,"
		 * +
		 * " u.sunitname,g.sdisplaystatus sgradename,ts1.jsondata->'stransdisplaystatus'->>'"
		 * +userInfo.getSlanguagetypecode()
		 * +"'::character varying senforceresult,ts2.jsondata->'stransdisplaystatus'->>'"
		 * +userInfo.getSlanguagetypecode()+"' senforcestatus," +
		 * " TO_CHAR((ap.jsondata->>'dentereddate'):: timestamp without time zone , '" +
		 * userInfo.getSpgdatetimeformat()+"') sentereddate," +
		 * " CONCAT(us.sfirstname,' ',us.slastname) as enteredby,ur.suserrolename," +
		 * " coalesce(ap.jsondata->>'smina','-') smina,coalesce(ap.jsondata->>'smaxa','-') smaxa,coalesce(ap. jsondata->>'sminb','-') sminb,coalesce(ap.jsondata->>'smaxb','-') smaxb,coalesce(ap.jsondata->>'sminlod','-') sminlod,coalesce(ap.jsondata->>'smaxlod','-') smaxlod,"
		 * +
		 * " coalesce(ap.jsondata->>'sminloq','-') sminloq,coalesce(ap.jsondata->>'smaxloq','-') smaxloq,"
		 * +
		 * " case cl.nchecklistcode when -1 then '-' else cl.schecklistname end schecklistname,cv.schecklistversionname,-1 nchecklistversioncode,cm.scolorhexcode"
		 * //+
		 * " coalesce(rpc.jsondata->>'sresultcomment','-') sresultcomment,coalesce(rpc.jsondata->>'senforceresultcomment','-') senforceresultcomment,coalesce(rpc.jsondata->>'senforcestatuscomment','-') senforcestatuscomment "
		 * +
		 * " from approvalparameter ap,registrationtest rt,registrationarno rar,registrationsamplearno sar,"
		 * // + " testgrouptestparameter tgtp," //+ "resultparametercomments rpc," +
		 * " unit u,parametertype pt,grade g,checklistversion cv,checklist cl,users us,userrole ur,transactionstatus ts1,transactionstatus ts2,colormaster cm"
		 * + " where ap.ntransactiontestcode=rt.ntransactiontestcode" //+
		 * " and ap.ntransactionresultcode=rpc.ntransactionresultcode" + " " //+
		 * " and rpc.nstatus="+
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " +
		 * " and ap.nstatus=" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
		 * " and cm.nstatus=" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
		 * " and rt.nstatus=" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
		 * " and rar.nstatus=" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
		 * " and sar.nstatus=" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
		 * " and u.nstatus=" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
		 * " and pt.nstatus=" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
		 * " and g.nstatus=" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
		 * " and cv.nstatus=" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
		 * " and us.nstatus=" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
		 * " and ts1.nstatus=" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
		 * " and ts2.nstatus=" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
		 * " and ur.nstatus=" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
		 * " and cl.nstatus=" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
		 * " and cm.ncolorcode=g.ncolorcode" +
		 * " and cl.nchecklistcode=cv.nchecklistcode" +
		 * " and rt.npreregno=rar.npreregno and rt.ntransactionsamplecode=sar.ntransactionsamplecode"
		 * +
		 * " and ap.nparametertypecode=pt.nparametertypecode and ap.ngradecode=g.ngradecode"
		 * + " and ap.nunitcode=u.nunitcode and cv.nchecklistversioncode =-1" +
		 * " and ap.nenteredby=us.nusercode and ap.nenteredrole=ur.nuserrolecode" +
		 * " and ap.nenforceresult=ts1.ntranscode and ap.nenforcestatus=ts2.ntranscode"
		 * + " and ap.ntransactiontestcode in(" + ntransactionTestCode +
		 * ") order by parameterorder desc";
		 * 
		 * List<ApprovalParameter> approvalParameterList = (List<ApprovalParameter>)
		 * jdbcTemplate.query( getApprovalParamQry, new ApprovalParameter());
		 */

		/*
		 * final String getApprovalParamQry =
		 * "select rpc.jsondata ->> 'senforceresultcomment' as senforceresultcomment,rpc.jsondata ->> 'senforcestatuscomment' AS senforcestatuscomment,ap.ntransactionresultcode,ap.nparametertypecode,ap.ntransactiontestcode,ap.npreregno,ap.ngradecode,ap.nlinkcode,ra.sarno,rsa.ssamplearno,"
		 * +
		 * "rt.ntransactionsamplecode,rt.jsondata->>'stestsynonym' stestsynonym,ap.jsondata->>'sparametersynonym' sparametersynonym,pt.sdisplaystatus,"
		 * +
		 * "case when u.nunitcode =-1 then ap.jsondata->>'sfinal'::character varying else CONCAT(ap.jsondata->>'sfinal',' ',u.sunitname) end sfinal,"
		 * +
		 * //"coalesce(gd.jsondata->'sdisplayname'->>'"+userInfo.getSlanguagetypecode()
		 * +"',gd.jsondata->'sdisplayname'->>'en-US')  sgradename,"+
		 * "coalesce(gd.jsondata->'sdisplayname'->>'"+userInfo.getSlanguagetypecode()
		 * +"',gd.jsondata->'sdisplayname'->>'en-US')  sgradename,"+
		 * "TO_CHAR((ap.jsondata->>'dentereddate'):: timestamp without time zone , '" +
		 * userInfo.getSpgdatetimeformat()+"') sentereddate," +
		 * "CONCAT(us.sfirstname,' ',us.slastname) as enteredby,ur.suserrolename," +
		 * "coalesce(ap.jsondata->>'smina','-') smina," +
		 * "coalesce(ap.jsondata->>'smina','-') smina," +
		 * "coalesce(ap.jsondata->>'smaxa','-') smaxa," +
		 * "coalesce(ap. jsondata->>'sminb','-') sminb," +
		 * "coalesce(ap.jsondata->>'smaxb','-') smaxb," +
		 * "coalesce(ap.jsondata->>'sminlod','-') sminlod," +
		 * "coalesce(ap.jsondata->>'smaxlod','-') smaxlod," +
		 * "coalesce(ap.jsondata->>'sminloq','-') sminloq," +
		 * "coalesce(ap.jsondata->>'smaxloq','-') smaxloq," +
		 * "tgp.nchecklistversioncode,case cl.nchecklistcode when -1 then '-' else cl.schecklistname end schecklistname,"
		 * + "cv.schecklistversionname,cm.scolorhexcode," +
		 * "coalesce(ts1.jsondata->'stransdisplaystatus'->>'"+userInfo.
		 * getSlanguagetypecode()
		 * +"',ts1.jsondata->'stransdisplaystatus'->>'en-US') as senforcestatus," +
		 * "coalesce(ts2.jsondata->'stransdisplaystatus'->>'"+userInfo.
		 * getSlanguagetypecode()
		 * +"',ts2.jsondata->'stransdisplaystatus'->>'en-US') senforceresult " + "from "
		 * + "approvalparameter ap," + "registrationtest rt," +
		 * "registrationsamplearno rsa," + "registrationarno ra," +
		 * "testgrouptestparameter tgp," + "users us," + "unit u," +
		 * "checklistversion cv," + "checklist cl," + "parametertype pt," +
		 * "userrole ur," + "colormaster cm," + "grade gd," + "transactionstatus ts1," +
		 * "transactionstatus ts2 " + " , resultparametercomments rpc"+ " where " +
		 * " ap.npreregno = rt.npreregno " + " and ap.nsitecode=rt.nsitecode"+
		 * " and rt.nsitecode=rsa.nsitecode"+
		 * " and    rpc.ntransactionresultcode=ap.ntransactionresultcode"+
		 * " and rpc.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus
		 * ()+ " and rsa.nsitecode=ra.nsitecode"+
		 * " and ra.nsitecode="+userInfo.getNtranssitecode()+
		 * "and ap.ntransactiontestcode = rt.ntransactiontestcode " +
		 * "and rt.npreregno = ra.npreregno " + "and rt.npreregno = rsa.npreregno " +
		 * "and rt.ntransactionsamplecode = rsa.ntransactionsamplecode " +
		 * "and ap.npreregno = ra.npreregno " + "and rsa.npreregno = ra.npreregno " +
		 * "and ap.ntestgrouptestparametercode = tgp.ntestgrouptestparametercode " +
		 * 
		 * "and ap.nenteredrole =  ur.nuserrolecode " +
		 * "and ap.nenteredby = us.nusercode " + "and ap.nunitcode = u.nunitcode " +
		 * "and ap.nparametertypecode = pt.nparametertypecode " +
		 * "and ap.ngradecode = gd.ngradecode " +
		 * "and ap.nenforcestatus = ts1.ntranscode " +
		 * "and ap.nenforceresult = ts2.ntranscode " +
		 * "and tgp.nchecklistversioncode = cv.nchecklistversioncode " +
		 * "and cl.nchecklistcode=cv.nchecklistcode " +
		 * "and cm.ncolorcode = gd.ncolorcode " +
		 * "and ap.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(
		 * ) +
		 * "and rt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus
		 * () + "and rsa.nstatus = "+Enumeration.TransactionStatus.ACTIVE.
		 * gettransactionstatus()+
		 * "and ra.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(
		 * )+
		 * "and us.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(
		 * ) +
		 * "and u.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		 * +
		 * "and cv.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus
		 * ()+
		 * "and cl.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus
		 * ()+
		 * "and ur.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(
		 * )+
		 * "and pt.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(
		 * )+ "and rpc.nstatus = "+Enumeration.TransactionStatus.ACTIVE.
		 * gettransactionstatus()+
		 * "and tgp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.
		 * gettransactionstatus()+
		 * "and ts1.nstatus = "+Enumeration.TransactionStatus.ACTIVE.
		 * gettransactionstatus()+
		 * "and ts2.nstatus = "+Enumeration.TransactionStatus.ACTIVE.
		 * gettransactionstatus()+
		 * "and gd.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus
		 * ()+" " + "and ap.ntransactiontestcode in ("+ntransactionTestCode+")";
		 */

		final String getApprovalParamQry = "" + "with temp_data as(" + ""
				+ "select rpc.jsondata as jsonparameterdata,ap.*,u.sunitname,gd.jsondata as jsongradedata,cm.scolorhexcode,us.sfirstname,us.slastname,ur.suserrolename from"
				+ " resultparametercomments rpc, approvalparameter ap, unit u,colormaster cm,grade gd,users us, userrole ur"
				+ "	where rpc.ntransactionresultcode=ap.ntransactionresultcode AND rpc.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND  ap.nunitcode = u.nunitcode"
				+ "	and u.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND cm.ncolorcode = gd.ncolorcode and cm.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and gd.nstatus=  "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND ap.ngradecode = gd.ngradecode"
				+ "	AND ap.nenteredby = us.nusercode and us.nstatus=  "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND ap.nenteredrole = ur.nuserrolecode and ur.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") SELECT "
				+ " tempd.jsonparameterdata ->> 'senforceresultcomment' as senforceresultcomment,"
				+ "	tempd.jsonparameterdata ->> 'senforcestatuscomment' AS senforcestatuscomment ,tempd.jsonparameterdata ->> 'sresultcomment' AS sresultcomment ,"
				+ " tempd.ntransactionresultcode," + "       tempd.nparametertypecode, "
				+ " tempd.ntransactiontestcode, " + "       tempd.npreregno, " + "       tempd.ngradecode, "
				+ " tempd.nlinkcode, " + "       ra.sarno, " + "       rsa.ssamplearno, "
				+ " rt.ntransactionsamplecode, " + "       rt.jsondata->>'stestsynonym'      stestsynonym, "
				+ " tempd.jsondata->>'sparametersynonym' sparametersynonym, " + "       pt.sdisplaystatus, "
				+ "	CASE " + " WHEN tempd.nunitcode =-1 THEN tempd.jsondata->>'sfinal'::character VARYING "
				+ " ELSE concat(tempd.jsondata->>'sfinal',' ',tempd.sunitname) "
				+ " END   sfinal, "
				+ " COALESCE(tempd.jsongradedata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',tempd.jsongradedata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "')              sgradename, " + "       COALESCE(tempd.jsondata->>'smina','-')   smina, "
				+ "	    to_char((tempd.jsondata->>'dentereddate'):: timestamp without time zone , '"
				+ userInfo.getSpgdatetimeformat() + "')    sentereddate, "
				+ "              concat(tempd.sfirstname,' ',tempd.slastname)   AS enteredby, "
				+ " tempd.suserrolename, " + "       COALESCE(tempd.jsondata->>'smina','-')   smina, "
				+ " COALESCE(tempd.jsondata->>'smaxa','-')   smaxa, "
				+ " COALESCE(tempd. jsondata->>'sminb','-')  sminb, "
				+ " COALESCE(tempd.jsondata->>'smaxb','-')   smaxb, "
				+ " COALESCE(tempd.jsondata->>'sminlod','-') sminlod, "
				+ " COALESCE(tempd.jsondata->>'smaxlod','-') smaxlod, "
				+ " COALESCE(tempd.jsondata->>'sminloq','-') sminloq, "
				+ " COALESCE(tempd.jsondata->>'smaxloq','-') smaxloq, " + "       tgp.nchecklistversioncode, "
				+ " CASE cl.nchecklistcode WHEN -1 THEN '-' " + " ELSE cl.schecklistname   END schecklistname, "
				+ "  cv.schecklistversionname,tempd.scolorhexcode, "
				+ "  COALESCE(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "',ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "') AS senforcestatus, " + "       COALESCE(ts2.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',ts2.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "')    senforceresult FROM    "
				+ " registrationtest rt, registrationsamplearno rsa, "
				+ " registrationarno ra, testgrouptestparameter tgp, "
				+ " checklistversion cv, checklist cl,  parametertype pt,  "
				+ " transactionstatus ts1, transactionstatus ts2 ,temp_data tempd "
				+ " WHERE  tempd.npreregno = rt.npreregno  AND tempd.nsitecode=rt.nsitecode "
				+ " AND rt.nsitecode=rsa.nsitecode  AND rsa.nsitecode=ra.nsitecode "
				+ " AND ra.nsitecode= " + userInfo.getNtranssitecode()
				+ " AND rt.nsitecode= " + userInfo.getNtranssitecode()
				+ " AND rsa.nsitecode= " + userInfo.getNtranssitecode()
				+ " AND tempd.ntransactiontestcode = rt.ntransactiontestcode AND rt.npreregno = ra.npreregno "
				+ " AND rt.npreregno = rsa.npreregno "
				+ " AND rt.ntransactionsamplecode = rsa.ntransactionsamplecode "
				+ " AND tempd.npreregno = ra.npreregno AND rsa.npreregno = ra.npreregno "
				+ " AND tempd.ntestgrouptestparametercode = tgp.ntestgrouptestparametercode "
				+ " AND tempd.nparametertypecode = pt.nparametertypecode "
				+ " AND tempd.nenforcestatus = ts1.ntranscode AND  tempd.nenforceresult = ts2.ntranscode "
				+ " AND tgp.nchecklistversioncode = cv.nchecklistversioncode "
				+ " AND cl.nchecklistcode=cv.nchecklistcode AND tempd.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND rt.nstatus =  "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND rsa.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND ra.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND cv.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND cl.nstatus =  "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND pt.nstatus =  "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND tgp.nstatus =  "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND ts1.nstatus =  "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND ts2.nstatus =  "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND tempd.ntransactiontestcode IN ("
				+ ntransactionTestCode + ")";

		List<ApprovalParameter> approvalParameterList = jdbcTemplate.query(getApprovalParamQry,
				new ApprovalParameter());

		List<?> lstUTCConvertedDate = dateUtilityFunction.getSiteLocalTimeFromUTC(approvalParameterList,
				Arrays.asList("sentereddate"), null, userInfo, true,
				Arrays.asList("sdisplaystatus", "senforcestatus", "sgradename"), false);

		approvalParameterList = objMapper.convertValue(lstUTCConvertedDate, new TypeReference<List<ApprovalParameter>>() {
		});
		Map<String, Object> returnMap = new HashMap<>();
		returnMap.put("ApprovalParameter", approvalParameterList);
		System.out.println("Parameter End=>" + Instant.now());
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	
	@Override
	public ResponseEntity<Map<String, Object>> getActionDetails(final int napproveconfversioncode,final UserInfo userInfo)
			throws Exception {

		final String getApprovalParamQry = "select aad.ntransactionstatus,ts.jsondata->'sactiondisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "' stransdisplaystatus,acr.nesignneed"
				+ " from approvalroleactiondetail aad,transactionstatus ts,approvalconfig ac,approvalconfigrole acr"
				+ " where ts.ntranscode=aad.ntransactionstatus" + " and ac.napprovalconfigcode=acr.napprovalconfigcode"
				+ " and acr.napprovalconfigrolecode=aad.napprovalconfigrolecode and aad.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ac.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acr.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acr.nuserrolecode="
				+ userInfo.getNuserrole() + ""
				+ " and ac.nsitecode="+userInfo.getNmastersitecode()+" "
				+ " and aad.nsitecode="+userInfo.getNmastersitecode()+" "
				+ " and acr.napproveconfversioncode=" + napproveconfversioncode + "";
		List<ApprovalRoleActionDetail> actionDetailList = (List<ApprovalRoleActionDetail>) jdbcTemplate
				.query(getApprovalParamQry, new ApprovalRoleActionDetail());
		
		Map<String, Object> returnMap = new HashMap<>();
		returnMap.put("actionStatus", actionDetailList);

		return new ResponseEntity<>(returnMap, HttpStatus.OK);

	}

	
	public Map<String, Object> getDecisionDetails(final int napproveconfversioncode,final UserInfo userInfo)
			throws Exception {
		final ObjectMapper objMapper=new ObjectMapper();
		final String getDecisionStatus = "select aad.ntransactionstatus,ts.jsondata->'sactiondisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "' as sdecisionstatus,cm.scolorhexcode,acr.nesignneed"
				+ " from approvalroledecisiondetail aad,transactionstatus ts,approvalconfig ac,approvalconfigrole acr,colormaster cm,"
				+ " formwisestatuscolor  fs where ts.ntranscode=aad.ntransactionstatus"
				+ " and ac.napprovalconfigcode=acr.napprovalconfigcode and fs.ncolorcode  = cm.ncolorcode"
				+ " and fs.ntranscode = ts.ntranscode  and fs.nformcode = "+Enumeration.FormCode.APPROVAL.getFormCode()+ ""
				+ " and acr.napprovalconfigrolecode=aad.napprovalconfigrolecode  and aad.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ac.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acr.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cm.nstatus="
				+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acr.nuserrolecode="
				+ userInfo.getNuserrole() + " "
				+ " and aad.nsitecode="+userInfo.getNmastersitecode()+" "
				+ " and ac.nsitecode="+userInfo.getNmastersitecode()+" "
				+ "and acr.napproveconfversioncode=" + napproveconfversioncode + "";
		List<ApprovalRoleDecisionDetail> actionDetailList = (List<ApprovalRoleDecisionDetail>) jdbcTemplate
				.query(getDecisionStatus, new ApprovalRoleDecisionDetail());
		actionDetailList = objMapper
				.convertValue(
						commonFunction.getMultilingualMessageList(actionDetailList, Arrays.asList("sdecisionstatus"),
								userInfo.getSlanguagefilename()),
						new TypeReference<List<ApprovalRoleDecisionDetail>>() {
						});
		Map<String, Object> returnMap = new HashMap<>();
		returnMap.put("decisionStatus", actionDetailList);

		return returnMap;

	}


	@Override
	public ResponseEntity<Map<String, Object>> getApprovalResultChangeHistory(final String ntransactionTestCode,final UserInfo userInfo)
			throws Exception {

		final ObjectMapper objMapper=new ObjectMapper();
		final String getApprovalParamQry = "select rt.ntransactiontestcode,rch.ntransactionresultcode,rar.sarno,sar.ssamplearno,"
				+ " rt.jsondata->>'stestsynonym' stestsynonym,"
				+ " tgp.sparametersynonym,rch.jsondata->>'sresult' as sresult,rch.jsondata->>'sfinal' sfinal,rp.jsondata->>'smina' smina,rp.jsondata->>'smaxa' smaxa,rp.jsondata->>'sminb' sminb,"
				+ " case  when rp.jsondata ->> 'sresultaccuracyname'  isnull  then ''  else  rp.jsondata ->> 'sresultaccuracyname' end AS sresultaccuracyname,"
				+ " rp.jsondata->>'smaxb' smaxb,rp.jsondata->>'sminlod' sminlod,"
				+ " rp.jsondata->>'smaxlod' smaxlod,rp.jsondata->>'sminloq' sminloq,rp.jsondata->>'smaxloq' smaxloq,"
				+ " CONCAT(u.sfirstname,' ',u.slastname) as username, to_char((rch.jsondata->>'dentereddate')::timestamp,'"
				+ userInfo.getSpgdatetimeformat() + "') sentereddate,"
				+ " ur.suserrolename ,rch.nformcode, qf.sformname "
				+ " from resultchangehistory rch,registrationtest rt,approvalparameter rp,"
				+ " registrationarno rar,registrationsamplearno sar,testgrouptest tgt,testgrouptestparameter tgp,users u,userrole ur,"
				+ " qualisforms qf where rt.ntransactiontestcode=rch.ntransactiontestcode"
				+ " and rar.npreregno=rt.npreregno and rp.ntransactionresultcode=rch.ntransactionresultcode"
				+ " and sar.ntransactionsamplecode=rt.ntransactionsamplecode and rt.ntestgrouptestcode=tgt.ntestgrouptestcode"
				+ " and rp.ntestgrouptestparametercode=tgp.ntestgrouptestparametercode and u.nusercode=rch.nenteredby"
				+ " and ur.nuserrolecode=rch.nenteredrole and rch.nsitecode=rt.nsitecode"
				+ " and rt.nsitecode=rp.nsitecode and rp.nsitecode=rar.nsitecode"
				+ " and rar.nsitecode=sar.nsitecode "
				+ " and rch.nsitecode=" + userInfo.getNtranssitecode()
				+ " and rt.nsitecode=" + userInfo.getNtranssitecode()
				+ " and rp.nsitecode=" + userInfo.getNtranssitecode()
				+ " and rar.nsitecode=" + userInfo.getNtranssitecode()
				+ " and sar.nsitecode=" + userInfo.getNtranssitecode()
				+ " and tgt.nsitecode=" + userInfo.getNmastersitecode()
				+ " and tgp.nsitecode=" + userInfo.getNmastersitecode()
				+ " and u.nsitecode=" + userInfo.getNmastersitecode()
				+ " and ur.nsitecode=" + userInfo.getNmastersitecode()
				+ " and rch.nformcode = qf.nformcode and rch.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rar.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sar.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and qf.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rch.ntransactiontestcode in("
				+ ntransactionTestCode + ")";
		
		 List<ResultChangeHistory> approvalParameterList = (List<ResultChangeHistory>) jdbcTemplate
				.query(getApprovalParamQry, new ResultChangeHistory());
		
		final List<?> lstUTCConvertedDate = dateUtilityFunction.getSiteLocalTimeFromUTC(approvalParameterList,
				Arrays.asList("sentereddate"), null, userInfo, false, null, false);
		
		approvalParameterList = objMapper.convertValue(lstUTCConvertedDate,
				new TypeReference<List<ResultChangeHistory>>() {
				});
		
		Map<String, Object> returnMap = new HashMap<>();
		returnMap.put("ApprovalResultChangeHistory", approvalParameterList);

		return new ResponseEntity<>(returnMap, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Map<String, Object>> getApprovalHistory(final String ntransactionTestCode,final UserInfo userInfo)
			throws Exception {
		final ObjectMapper objMapper=new ObjectMapper();
		final String getApprovalHistoryQry = "select rth.ntesthistorycode,rth.ntransactiontestcode,rar.sarno,"
				+ " sar.ssamplearno, rt.jsondata->>'stestsynonym' stestsynonym,"
				+ " ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "' stransdisplaystatus,"
				+ " CONCAT(u.sfirstname,' ',u.slastname) as username, ur.suserrolename,"
				+ " TO_CHAR(rth.dtransactiondate ,'" + userInfo.getSpgdatetimeformat() + "') stransactiondate "
				+ " from registrationtesthistory rth,registrationtest rt,registrationarno rar,registrationsamplearno sar,testgrouptest tgt,"
				+ " transactionstatus ts,users u,userrole ur "
				+ " where rt.ntransactiontestcode=rth.ntransactiontestcode and rt.ntestgrouptestcode=tgt.ntestgrouptestcode"
				+ " and rt.ntransactionsamplecode=sar.ntransactionsamplecode and rt.npreregno=rar.npreregno"
				+ " and rth.ntransactionstatus=ts.ntranscode and u.nusercode=rth.nusercode and ur.nuserrolecode=rth.nuserrolecode"
				+ " and rth.nsitecode=rt.nsitecode  and rt.nsitecode=rar.nsitecode"
				+ " and rar.nsitecode=sar.nsitecode  and sar.nsitecode=" + userInfo.getNtranssitecode()
				+" and rth.nsitecode=" + userInfo.getNtranssitecode()+""
				+" and rt.nsitecode=" + userInfo.getNtranssitecode()+""
				+" and rar.nsitecode=" + userInfo.getNtranssitecode()+""
				+" and sar.nsitecode=" + userInfo.getNtranssitecode()+""
				+" and u.nsitecode=" + userInfo.getNmastersitecode()+""
				+" and ur.nsitecode=" + userInfo.getNmastersitecode()+""
				+" and tgt.nsitecode=" + userInfo.getNmastersitecode()+""
				+ " and rth.ntransactionstatus = any (select ad.ntransactionstatus from approvalroleactiondetail ad,approvalconfigrole acr"
				+ " where acr.napproveconfversioncode=rar.napprovalversioncode and ad.napprovalconfigrolecode=acr.napprovalconfigrolecode and ad.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acr.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " union"
				+ " select ntranscode from transactionstatus where ntranscode ="
				+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() + ")"
				+ " and rar.npreregno=rth.npreregno and rth.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rar.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sar.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.ntransactiontestcode in("
				+ ntransactionTestCode + ")  order by ntesthistorycode desc";

		List<RegistrationTestHistory> approvalHistoryList = (List<RegistrationTestHistory>) jdbcTemplate
				.query(getApprovalHistoryQry, new RegistrationTestHistory());
		
		final List<?> lstUTCConvertedDate = dateUtilityFunction.getSiteLocalTimeFromUTC(approvalHistoryList,
				Arrays.asList("stransactiondate"), null, userInfo, false, Arrays.asList("stransdisplaystatus"), false);
		approvalHistoryList = objMapper.convertValue(lstUTCConvertedDate,
				new TypeReference<List<RegistrationTestHistory>>() {
				});
		Map<String, Object> returnMap = new HashMap<>();
		returnMap.put("ApprovalHistory", approvalHistoryList);

		return new ResponseEntity<>(returnMap, HttpStatus.OK);

	}

	
	@Override
	public ResponseEntity<Map<String, Object>> getSampleApprovalHistory(final String npreregno,final UserInfo userInfo) throws Exception {// --old

		final ObjectMapper objMapper=new ObjectMapper();
		final String getApprovalHistoryQry = "select napprovalhistorycode, TO_CHAR(sah.dtransactiondate,'"
				+ userInfo.getSpgdatetimeformat() + "') stransactiondate, rg.smanuflotno,sah.scomments, "
				+ " rar.sarno,rar.npreregno,ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "',CONCAT(u.sfirstname,' ',u.slastname) as username,ur.suserrolename "
				+ " from sampleapprovalhistory sah,registrationarno rar,registrationgeneral rg,transactionstatus ts,users u,userrole ur"
				+ " where sah.npreregno=rar.npreregno" + " and sah.npreregno = rg.npreregno "
				+ " and sah.ntransactionstatus=ts.ntranscode and sah.nusercode=u.nusercode"
				+ " and sah.nuserrolecode=ur.nuserrolecode and sah.ntransactionstatus =any ((select ad.ntransactionstatus from approvalroleactiondetail ad,approvalconfigrole acr"
				+ " where ad.napprovalconfigrolecode=acr.napprovalconfigrolecode and ad.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " 	and acr.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and acr.napproveconfversioncode=rar.napprovalversioncode)union select" + " 	"
				+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() + ") and sah.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rar.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sah.npreregno in(" + npreregno
				+ ") order by napprovalhistorycode  desc";

		List<SampleApprovalHistory> approvalHistoryList = (List<SampleApprovalHistory>) jdbcTemplate
				.query(getApprovalHistoryQry, new SampleApprovalHistory());
		List<?> lstUTCConvertedDate = dateUtilityFunction.getSiteLocalTimeFromUTC(approvalHistoryList,
				Arrays.asList("stransactiondate"), null, userInfo, true, Arrays.asList("stransdisplaystatus"), false);
		approvalHistoryList = objMapper.convertValue(lstUTCConvertedDate,
				new TypeReference<List<SampleApprovalHistory>>() {
				});
		Map<String, Object> returnMap = new HashMap<>();
		returnMap.put("SampleApprovalHistory", approvalHistoryList);

		return new ResponseEntity<>(returnMap, HttpStatus.OK);

	}


	@Override
	public ResponseEntity<Map<String, Object>> getCOAHistory(final String npreregno,final UserInfo userInfo) throws Exception {
		final ObjectMapper objMapper=new ObjectMapper();
		final String querySampleCertMail = " select ch.ncoahistorycode,rar.sarno,rar.npreregno,"
				+ " Format(ch.dgenerateddate,'" + userInfo.getSdatetimeformat()
				+ "') sgeneratedtime,ch.ssystemfilename,"
				+ " rt.scoareporttypename,sc.ssectionname,us.sfirstname+' '+us.slastname susername,ur.suserrolename"
				+ " from coahistory ch,coareporttype rt,section sc,users us,userrole ur,registrationarno rar"
				+ " where ch.npreregno in (" + npreregno + ") and ch.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ch.npreregno = rar.npreregno and rar.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rt.ncoareporttypecode = ch.ncoareporttypecode and rt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sc.nsectioncode = ch.nsectioncode and sc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and us.nusercode = ch.nusercode and us.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ur.nuserrolecode = ch.nuserrolecode and ur.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ch.nsitecode="+userInfo.getNtranssitecode()+" "
				+ " and rar.nsitecode="+userInfo.getNtranssitecode()+" "
				+ " and sc.nsitecode="+userInfo.getNtranssitecode()+" "
				+ " and us.nsitecode="+userInfo.getNmastersitecode()+" "
				+ " and ur.nsitecode="+userInfo.getNmastersitecode()+" ";

		List<COAHistory> coaHistoryList = (List<COAHistory>) jdbcTemplate.query(querySampleCertMail, new COAHistory());
		List<?> lstUTCConvertedDate = dateUtilityFunction.getSiteLocalTimeFromUTC(coaHistoryList,
				Arrays.asList("sgeneratedtime"), null, userInfo, false, null, false);
		coaHistoryList = objMapper.convertValue(lstUTCConvertedDate, new TypeReference<List<COAHistory>>() {
		});
		Map<String, Object> returnMap = new HashMap<>();
		returnMap.put("COAHistory", coaHistoryList);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> getStatusCombo(final int ntransactionResultCode,final UserInfo userInfo) throws Exception {// --

		Map<String, Object> returnMap = new HashMap<>();
		 String str = " select * from approvalparameter where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntransactionresultcode = "
				+ ntransactionResultCode + " and jsondata->'sfinal' <> 'null' and nsitecode="+userInfo.getNtranssitecode()+"";
		final ApprovalParameter approvalParam = (ApprovalParameter) jdbcUtilityFunction.queryForObject(str,
				ApprovalParameter.class, jdbcTemplate);
		if (approvalParam != null) {

			str = " select sgradename,ngradecode from grade where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ngradecode>0 ";
					
			
			final List<Grade> lstgrade = (List<Grade>) jdbcTemplate.query(str, new Grade());
			
			returnMap.put("Grade", lstgrade);
			Map<String, Object> gradeMap = new HashMap<>();
			final List<Grade> defaultGrade = lstgrade.stream()
					.filter(grade -> grade.getNgradecode() == approvalParam.getNgradecode())
					.collect(Collectors.toList());
			if (defaultGrade != null && defaultGrade.size() > 0) {
				gradeMap.put("label", defaultGrade.get(0).getSgradename());
				gradeMap.put("value", defaultGrade.get(0).getNgradecode());
			}
			returnMap.put("GradeValue", gradeMap);

			str = "  select rpc.ntransactionresultcode,rpc.ntransactiontestcode,rpc.npreregno,rpc.ntestgrouptestparametercode,rpc.ntestparametercode,"
					+ " coalesce(rpc.jsondata->>'sresultcomment','') sresultcomment,coalesce(rpc.jsondata->>'senforcestatuscomment','') senforcestatuscomment,coalesce(rpc.jsondata->>'senforceresultcomment','') senforceresultcomment"
					+ " from resultparametercomments rpc,approvalparameter ap  where rpc.ntransactionresultcode="
					+ ntransactionResultCode + " and ap.ntransactionresultcode=rpc.ntransactionresultcode "
					+ " and rpc.nsitecode=ap.nsitecode "
					+ " and ap.nsitecode=" + userInfo.getNtranssitecode()
					+ " and rpc.nsitecode=" + userInfo.getNtranssitecode()
					+ " and ap.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and rpc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final ResultParameterComments objResultParameterComments = (ResultParameterComments) jdbcUtilityFunction
					.queryForObject(str, ResultParameterComments.class, jdbcTemplate);
			
			returnMap.put("parameterComment", objResultParameterComments);
			return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<Object>(
					commonFunction.getMultilingualMessage("IDS_ENTERRESULT", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}

	@Override
	public ResponseEntity<Object> getEditParameter(final String ntransactionTestCode,final UserInfo userInfo)
			throws Exception {

		Map<String, Object> returnMap = new HashMap<>();

		String str = "select ap.ntransactionresultcode,tgtp.sparametersynonym,ap.nreportmandatory,t.stestsynonym,rar.sarno,sar.ssamplearno"
				+ " from approvalparameter ap,testgrouptestparameter tgtp,registrationtest rt,testgrouptest t,registrationarno rar,registrationsamplearno sar"
				+ " where ap.ntestgrouptestparametercode=tgtp.ntestgrouptestparametercode"
				+ " and ap.ntransactiontestcode =rt.ntransactiontestcode and t.ntestgrouptestcode = rt.ntestgrouptestcode"
				+ " and t.ntestcode=rt.ntestcode and rt.ntransactionsamplecode=sar.ntransactionsamplecode"
				+ " and rt.npreregno=rar.npreregno and ap.ntransactiontestcode in (" + ntransactionTestCode + ")"
				+ " and ap.ntransactionstatus not in (" + Enumeration.TransactionStatus.REJECTED.gettransactionstatus()
				+ "," + Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ") and ap.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgtp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and t.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rar.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sar.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ap.nsitecode=rt.nsitecode"
				+ " and rt.nsitecode=rar.nsitecode and rar.nsitecode=sar.nsitecode "
				+ " and sar.nsitecode="+userInfo.getNtranssitecode()
				+ " and ap.nsitecode="+userInfo.getNtranssitecode()
				+ " and rt.nsitecode="+userInfo.getNtranssitecode()
				+ " and rar.nsitecode="+userInfo.getNtranssitecode()
				+ " and tgtp.nsitecode="+userInfo.getNmastersitecode()
				+ " and t.nsitecode="+userInfo.getNmastersitecode()
				+ " group by ap.ntransactionresultcode,tgtp.sparametersynonym,ap.nreportmandatory,t.stestsynonym,rar.sarno,sar.ssamplearno ";
		List<ApprovalParameter> approvalParamList = (List<ApprovalParameter>) jdbcTemplate.query(str,
				new ApprovalParameter());
		returnMap.put("ApprovalParamEdit", approvalParamList);

		return new ResponseEntity<Object>(returnMap, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> updateApprovalParameter(List<ApprovalParameter> approvalParamList,final UserInfo userInfo)
			throws Exception {

		final List<String> listAction = new ArrayList<>();
		String updateQuery = "";
		final String approvalparametercode = approvalParamList.stream()
				.map(x -> String.valueOf(x.getNtransactionresultcode())).collect(Collectors.joining(","));
		
		String strobj = "select * from approvalparameter where ntransactionresultcode in (" + approvalparametercode
				+ ") and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  nsitecode="
				+ userInfo.getNtranssitecode();
		List<ApprovalParameter> auditOldList = (List<ApprovalParameter>) jdbcTemplate.query(strobj,
				new ApprovalParameter());
		
		for (ApprovalParameter objApprovalParameter : approvalParamList) {
			updateQuery += " update approvalparameter set nreportmandatory="
					+ objApprovalParameter.getNreportmandatory() + " where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntransactionresultcode="
					+ objApprovalParameter.getNtransactionresultcode() + "; ";
		}

		jdbcTemplate.execute(updateQuery);
		
		strobj = "select * from approvalparameter where ntransactionresultcode in (" + approvalparametercode
				+ ") and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  nsitecode="
				+ userInfo.getNtranssitecode();
		List<ApprovalParameter> auditList = (List<ApprovalParameter>) jdbcTemplate.query(strobj,
				new ApprovalParameter());

		listAction.add("IDS_EDITAPPROVALPARAMETER");
		auditUtilityFunction.fnInsertListAuditAction(Arrays.asList(auditList), 2, Arrays.asList(auditOldList),
				listAction, userInfo);

		return new ResponseEntity<Object>(HttpStatus.OK);

	}

	public ResponseEntity<Object> roleWiseAutoApproval(Map<String, Object> inputMap,final UserInfo userInfo)
			throws Exception {
		final ObjectMapper objMapper=new ObjectMapper();
		int roleCode = -1;
		int userCode = -1;
		int ntransStatus = -1;
		int decisionCode = -1;
		boolean approve = false;
		boolean validTest = false;
		/***************
		 * Check for role wise Auto Approval starts here
		 *******************/
		final String roleWiseAutoApproval = "select acr.* from approvalconfigrole acr,approvalconfigversion acv"
				+ " where acv.napproveconfversioncode=acr.napproveconfversioncode and acr.nlevelno < ( "
				+ " select nlevelno from approvalconfigrole where napproveconfversioncode="
				+ inputMap.get("napprovalversioncode") + " 	and nuserrolecode=" + userInfo.getNuserrole() + ")"
				+ " and acv.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and acr.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and acv.nsitecode=" +userInfo.getNmastersitecode()
				+ " and acr.nsitecode=" +userInfo.getNmastersitecode()
				+ " and acv.napproveconfversioncode=" + inputMap.get("napprovalversioncode")
				+ " order by nlevelno desc";

		final List<ApprovalConfigRole> roleList = (List<ApprovalConfigRole>) jdbcTemplate.query(roleWiseAutoApproval,
				new ApprovalConfigRole());

		/*** get the top most auto approval level details starts here ***/
		for (int i = 0; i < roleList.size(); i++) {
			if (roleList.get(i).getNautoapproval() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				roleCode = roleList.get(i).getNuserrolecode();
				ntransStatus = roleList.get(i).getNautoapprovalstatuscode();
				decisionCode = roleList.get(i).getNautodescisionstatuscode();
				approve = true;
			} else {
				break;
			}
		}
		/***************
		 * Get the top most auto approval level details ends here
		 ********/
		/***************
		 * Check for role wise Auto Approval ends here
		 *******************/
		if (approve) {
			/*********** Validations for test approval starts here ************/
			List<Object> insertAuditList = new ArrayList<>();
			List<String> multiLingualActionList = new ArrayList<>();
			final int ntransCode = ntransStatus;
			 String npreregno = (String) inputMap.get("npreregno");
			 String ntransactionsamplecode = (String) inputMap.get("ntransactionsamplecode");
			 String ntransactionTestCode = (String) inputMap.get("ntransactiontestcode");
			final String nsectionCode = (String) inputMap.get("nsectioncode");
			final String scomments = userInfo.getSreason();
			final String napprovalConfigVersionCode = (String) inputMap.get("napprovalversioncode");
			int nseqnoApprovalHistory = 0;
			int nseqnoTestHistory = 0;
			inputMap.put("nuserrolecode", roleCode);
			inputMap.put("nusercode", userCode);
			inputMap.put("nTransStatus", ntransStatus);
			inputMap.put("nusercode", decisionCode);
				

			nseqnoApprovalHistory = (int) inputMap.get("AutoApprovalHistorySequence");
			nseqnoTestHistory = (int) inputMap.get("AutoTestHistorySequence");

			final String transTestQuery = "select * from registrationtest where ntransactiontestcode in ("
					+ ntransactionTestCode + ") " + " and  nsitecode=" + userInfo.getNtranssitecode()
					+ " and npreregno in (" + npreregno + ")";
			List<RegistrationTest> transTestList = (List<RegistrationTest>) jdbcTemplate.query(transTestQuery,
					new RegistrationTest());

			final String roleValidationDetailGet = "select ts.jsondata->'stransdisplaystatus'->>'"
					+ userInfo.getSlanguagetypecode()
					+ "' as stransstatus,vd.napprovalconfigrolecode,vd.napprovalconfigcode,vd.ntransactionstatus,acr.nuserrolecode"
					+ " from approvalconfigversion acv,approvalconfigrole acr,approvalrolevalidationdetail vd,transactionstatus ts"
					+ " where acv.napproveconfversioncode=acr.napproveconfversioncode"
					+ " and acr.napprovalconfigrolecode=vd.napprovalconfigrolecode"
					+ " and ts.ntranscode=vd.ntransactionstatus and acv.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acr.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and vd.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "and acr.nuserrolecode=" + roleCode
					+ " and acv.nsitecode=" +userInfo.getNmastersitecode()
					+ " and acr.nsitecode=" +userInfo.getNmastersitecode()
					+ " and vd.nsitecode=" +userInfo.getNmastersitecode()
					+ " and acv.napproveconfversioncode=" + napprovalConfigVersionCode;

			List<ApprovalRoleValidationDetail> validationDetails = (List<ApprovalRoleValidationDetail>) jdbcTemplate
					.query(roleValidationDetailGet, new ApprovalRoleValidationDetail());

			if (validationDetails.size() > 0) {

				final String validationStatusCodes = stringUtilityFunction.fnDynamicListToString(validationDetails,
						"getNtransactionstatus");

				final String transTestCodes = stringUtilityFunction.fnDynamicListToString(transTestList,
						"getNtransactiontestcode");

				final String actionPerformedTestGet = "select rth.ntransactiontestcode,max(rth.ntransactionstatus) ntransactionstatus,max(ts.jsondata->'stransdisplaystatus'->>'"
						+ userInfo.getSlanguagetypecode() + "') stransdisplaystatus"
						+ " from registrationtesthistory rth,transactionstatus ts"
						+ " where rth.ntesthistorycode = any ("
						+ "  select max(rth1.ntesthistorycode) as ntesthistorycode"
						+ "  from registrationtesthistory rth1" + "  where rth1.nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ "  and rth1.ntransactiontestcode in (" + transTestCodes + ")"
						+ "  group by rth1.npreregno,rth1.ntransactionsamplecode,rth1.ntransactiontestcode" + " )"
						+ " and rth.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and rth.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ "  and  rth.nsitecode=" + userInfo.getNtranssitecode()
						+ " and ts.ntranscode = rth.ntransactionstatus and rth.ntransactionstatus not in ("
						+ validationStatusCodes + ") group by rth.npreregno,rth.ntransactiontestcode";

				final List<RegistrationTestHistory> actionPerformedTests = (List<RegistrationTestHistory>) jdbcTemplate
						.query(actionPerformedTestGet, new RegistrationTestHistory());

				final List<RegistrationTestHistory> filteredActionPerformedTests = actionPerformedTests.stream().filter(
						test -> validationDetails.stream().anyMatch(vd -> test.getNtransactionstatus() == ntransCode))
						.collect(Collectors.toList());

				final List<RegistrationTest> filteredValidTests = transTestList.stream()
						.filter(test -> actionPerformedTests.stream()
								.noneMatch(nvt -> test.getNtransactiontestcode() == nvt.getNtransactiontestcode()))
						.collect(Collectors.toList());

				if (actionPerformedTests.size() > 0 && filteredValidTests.size() == 0) {

					if (filteredActionPerformedTests.size() == actionPerformedTests.size()) {

						final String alertMessage = commonFunction.getMultilingualMessage("IDS_TESTALREADY",
								userInfo.getSlanguagefilename())
								+ " "
								+ commonFunction.getMultilingualMessage(
										actionPerformedTests.get(0).getStransdisplaystatus(),
										userInfo.getSlanguagefilename());

						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage(alertMessage, userInfo.getSlanguagefilename()),
								HttpStatus.EXPECTATION_FAILED);

					} else {

						final String strQuery = "select jsondata->'salertdisplaystatus'->>'"
								+ userInfo.getSlanguagetypecode() + "' from transactionstatus where nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntranscode in("
								+ validationStatusCodes + ")";

						List<TransactionStatus> validationStatusList = (List<TransactionStatus>) jdbcTemplate
								.query(strQuery, new TransactionStatus());
						validationStatusList = objMapper.convertValue(
								commonFunction.getMultilingualMessageList(validationStatusList,
										Arrays.asList("salertdisplaystatus"), userInfo.getSlanguagefilename()),
								new TypeReference<List<TransactionStatus>>() {
								});

						final String valditaionStatus = String.join("/", validationStatusList.stream()
								.map(x -> x.getSalertdisplaystatus()).collect(Collectors.toList()));
						final String alertMessage = commonFunction.getMultilingualMessage("IDS_SELECT",
								userInfo.getSlanguagefilename()) + " [" + valditaionStatus + "] "
								+ commonFunction.getMultilingualMessage("IDS_TESTONLY",
										userInfo.getSlanguagefilename());

						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage(alertMessage, userInfo.getSlanguagefilename()),
								HttpStatus.EXPECTATION_FAILED);

					}
				} else {

					npreregno = stringUtilityFunction.fnDynamicListToString(filteredValidTests, "getNpreregno");
					ntransactionTestCode = stringUtilityFunction.fnDynamicListToString(filteredValidTests,
							"getNtransactiontestcode");
					inputMap.put("npreregno", npreregno);
					inputMap.put("ntransactiontestcode", ntransactionTestCode);

					final String approvalConfigRoleGet = "select napprovalconfigrolecode, napprovalconfigcode, ntreeversiontempcode, nuserrolecode,"
							+ " nchecklistversioncode, npartialapprovalneed, nsectionwiseapprovalneed, nrecomretestneed,"
							+ " nrecomrecalcneed, nretestneed, nrecalcneed, nlevelno, napprovalstatuscode,"
							+ " napproveconfversioncode, nautoapproval, nautoapprovalstatuscode,"
							+ " nautodescisionstatuscode, ncorrectionneed, nesignneed,"
							+ " ntransactionstatus, dmodifieddate, nsitecode, nstatus from approvalconfigrole  where  nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and napproveconfversioncode=" + napprovalConfigVersionCode
							+ " and nuserrolecode=" + userInfo.getNuserrole()
							+ " and nsitecode="+userInfo.getNmastersitecode()+" ";


					final ApprovalConfigRole objApprovalConfigRole = (ApprovalConfigRole) jdbcUtilityFunction
							.queryForObject(approvalConfigRoleGet, ApprovalConfigRole.class, jdbcTemplate);

					if (objApprovalConfigRole != null) {

						if (objApprovalConfigRole.getNpartialapprovalneed() == Enumeration.TransactionStatus.YES
								.gettransactionstatus()) {
							/***** if Partial approval is needed then allow approval ******/
							validTest = true;

						} else {
							/*****
							 * if Partial approval is not needed then check for section wise approval
							 ******/
							String sectionCondition = "";
							if (objApprovalConfigRole.getNsectionwiseapprovalneed() == Enumeration.TransactionStatus.YES
									.gettransactionstatus()) {
								sectionCondition = "and rt.nsectioncode in (" + nsectionCode + ")";
							}
							final String validateTestCountQuery = "select rt.* from registrationtesthistory rth,registrationtest rt"
									+ " where rt.ntransactiontestcode = rth.ntransactiontestcode  "
									+ " and  rth.nsitecode=rt.nsitecode and  rt.nsitecode="
									+ userInfo.getNtranssitecode() + " and  rth.nsitecode="
									+ userInfo.getNtranssitecode() + " and rth.ntesthistorycode = any  ("
									+ "		select max(rth1.ntesthistorycode) as testhistorycode"
									+ " 	from registrationtesthistory rth1  where rth1.npreregno in ("
									+ npreregno + ") and  rth1.nsitecode=" + userInfo.getNtranssitecode()
									+ " 	and rth1.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ "		group by rth1.npreregno,rth1.ntransactionsamplecode,rth1.ntransactiontestcode)"
									+ sectionCondition + " and rth .ntransactionstatus not in ("
									+ Enumeration.TransactionStatus.RETEST.gettransactionstatus() + ","
									+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ","
									+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ")"
									+ " and rt.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and rth.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
							final List<RegistrationTest> transTestList2 = (List<RegistrationTest>) jdbcTemplate
									.query(validateTestCountQuery, new RegistrationTest());
							
							if (transTestList2.size() > 0) {
								if (transTestList2.size() == transTestList.size()) {
									validTest = true;
								} else {
									String stransStatus = jdbcTemplate.queryForObject(
											"select jsondata->'salertdisplaystatus'->>'"
													+ userInfo.getSlanguagetypecode()
													+ "' from transactionstatus where ntranscode =" + ntransStatus
													+ " and nstatus="
													+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
											String.class);
									stransStatus = commonFunction.getMultilingualMessage(stransStatus,
											userInfo.getSlanguagefilename());
									String msg = "IDS_FULLYAPPROVE";
									msg = commonFunction.getMultilingualMessage(msg, userInfo.getSlanguagefilename());
									return new ResponseEntity<>(
											commonFunction.getMultilingualMessage(msg + " [" + stransStatus + "] ",
													userInfo.getSlanguagefilename()),
											HttpStatus.EXPECTATION_FAILED);
								}
							}
						}
					} else {

						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage("IDS_CHECKAPPROVALCONFIGURATION",
										userInfo.getSlanguagefilename()),
								HttpStatus.EXPECTATION_FAILED);

					}
				}
			} else {

				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_VALIDATIONSTATUSISEMPTY",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);

			}
			/*** Decision Status update starts here */
			final String getDecisionStatus = "select ad.* from approvalroledecisiondetail ad,approvalconfigrole ar  "
					+ " where ar.nuserrolecode = " + roleCode + " and ar.napproveconfversioncode="
					+ napprovalConfigVersionCode + " and ad.napprovalconfigrolecode=ar.napprovalconfigrolecode "
					+ " and ad.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ar.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ad.nsitecode="+userInfo.getNmastersitecode()+" "
					+ " and ar.nsitecode="+userInfo.getNmastersitecode()+" ";

			List<ApprovalRoleDecisionDetail> decisionStatusList = (List<ApprovalRoleDecisionDetail>) jdbcTemplate
					.query(getDecisionStatus, new ApprovalRoleDecisionDetail());

			if (decisionStatusList.size() > 0) {

				final int seqNoDecisionHistory = (int) inputMap.get("AutoDecisionHistorySeq");
				
				final String insertReghistory = "insert into registrationdecisionhistory(nregdecisionhistorycode,npreregno,ndecisionstatus,dtransactiondate,"
						+ "nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments,nsitecode,nstatus) "
						+ "select " + seqNoDecisionHistory
						+ "+rank()over(order by npreregno) nregdecisionhistorycode,npreregno," + decisionCode
						+ " ndecisionstatus," + "'" + dateUtilityFunction.getCurrentDateTime(userInfo)// objGeneral.getUTCDateTime()
						+ "'  dtransactiondate," + userCode + " nusercode," + roleCode + " nuserrolecode," + ""
						+ userCode + " ndeputyusercode," + roleCode + " ndeputyuserrolecode,N'"
						+ stringUtilityFunction.replaceQuote(scomments) + "' scomments," + userInfo.getNtranssitecode()
						+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
						+ "from registration where  npreregno in (" + npreregno + ")" + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
						+ userInfo.getNtranssitecode() + " order by npreregno ;";

			
				jdbcTemplate.execute(insertReghistory);

				final String strobj = "select rdh.npreregno,rdh.ndecisionstatus,rar.sarno,ts.stransdisplaystatus,ur.suserrolename,"
						+ " (u.sfirstname+' '+u.slastname) as username,convert(nvarchar(100),rdh.dtransactiondate) stransactiondate "
						+ " from registrationdecisionhistory rdh,registrationarno rar,transactionstatus ts,users u,userrole ur"
						+ " where rar.npreregno = rdh.npreregno and u.nusercode=rdh.nusercode"
						+ " and ur.nuserrolecode=rdh.nuserrolecode and ts.ntranscode=rdh.ndecisionstatus"
						+ " and rdh.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and rar.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ts.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and u.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ur.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and rdh.nsitecode=rar.nsitecode "
						+ " and rar.nsitecode=" + userInfo.getNtranssitecode()
						+ " and rdh.nsitecode=" + userInfo.getNtranssitecode()
						+ " and u.nsitecode=" + userInfo.getNmastersitecode()
						+ " and ur.nsitecode=" + userInfo.getNmastersitecode()
						+ " and rdh.nregdecisionhistorycode = any("
						+ " select max(nregdecisionhistorycode) nregdecisionhistorycode from registrationdecisionhistory"
						+ " where npreregno in(" + npreregno + ") and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and nsitecode="
						+ userInfo.getNtranssitecode() + " 	group by npreregno" + " )";

				List<RegistrationDecisionHistory> lstregtest = (List<RegistrationDecisionHistory>) jdbcTemplate
						.query(strobj, new RegistrationDecisionHistory());
				insertAuditList.add(lstregtest);
				multiLingualActionList.add("IDS_SAMPLEDECISION");
				auditUtilityFunction.fnInsertListAuditAction(insertAuditList, 1, null, multiLingualActionList,
						userInfo);
			}
			/*** Decision Status update ends here */
			/*** Validation for test approval ends here */
			/*** Test Status update Starts here */
			if (validTest) {

				final String approvalHistory = " insert into sampleapprovalhistory (napprovalhistorycode,npreregno,nusercode,"
						+ " nuserrolecode,ndeputyusercode,ndeputyuserrolecode,ntransactionstatus,dtransactiondate,scomments,nsitecode,nstatus)"
						+ " select " + nseqnoApprovalHistory + "+rank()over(order by npreregno) napprovalhistorycode,"
						+ "npreregno," + userCode + " nusercode," + roleCode + " nuserrolecode," + userCode
						+ " ndeputyusercode," + roleCode + " ndeputyuserrolecode," + ntransStatus
						+ " ntransactionstatus," + "'" + dateUtilityFunction.getCurrentDateTime(userInfo)// objGeneral.getUTCDateTime()
						+ "' dtransactiondate," + "N'" + stringUtilityFunction.replaceQuote(scomments) + "' scomments,"
						+ userInfo.getNtranssitecode() + ",1 nstatus from registration " + " where npreregno in ("
						+ npreregno + ") " + " and nsitecode=" + userInfo.getNtranssitecode() + " order by npreregno;";

				final String testHistory = " insert into registrationtesthistory (ntesthistorycode,ntransactiontestcode,ntransactionsamplecode,"
						+ " npreregno,ntransactionstatus,dtransactiondate,nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments,nformcode,"
						+ " nsitecode,nstatus,nsampleapprovalhistorycode)" + " select " + nseqnoTestHistory
						+ "+rank()over(order by ntransactiontestcode) ntesthistorycode,ntransactiontestcode,ntransactionsamplecode,npreregno,"
						+ " " + ntransStatus + " ntransactionstatus,'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo)// objGeneral.getUTCDateTime()
						+ "' dtransactiondate," + userCode + " nusercode," + roleCode + " nuserrolecode," + " "
						+ userCode + " ndeputyusercode," + roleCode + " ndeputyuserrolecode," + " N'"
						+ stringUtilityFunction.replaceQuote(scomments) + "' scomments," + userInfo.getNformcode()
						+ " nformcode," + userInfo.getNtranssitecode() + ",1 nstatus," + " " + nseqnoApprovalHistory
						+ "+rank()over(order by npreregno) nsampleapprovalhistorycode "
						+ " from  registrationtest where ntransactiontestcode in (" + ntransactionTestCode
						+ ") and nsitecode=" + userInfo.getNtranssitecode() + " order by ntransactiontestcode;";

			
		
				jdbcTemplate.execute(approvalHistory);
				jdbcTemplate.execute(testHistory);

				final String testAuditGet = "select rth.npreregno,rth.ntransactionsamplecode,rth.ntransactionstatus,rth.nusercode,rth.nuserrolecode,"
						+ " rth.ntesthistorycode,rt.ntransactiontestcode,"
						+ " tm.stestsynonym +' ['+convert(nvarchar(10),rt.ntestrepeatno)+']['+convert(nvarchar(10),rt.ntestretestno)+']' as stestsynonym,"
						+ " rt.ntestgrouptestcode,rar.sarno,ts.stransdisplaystatus ,"
						+ " ur.suserrolename,(u.sfirstname+' '+u.slastname) as username,convert(nvarchar(100),rth.dtransactiondate) stransactiondate"
						+ " from registrationtesthistory rth,registrationtest rt,testmaster tm,registrationarno rar,transactionstatus ts"
						+ " ,users u,userrole ur where rth.ntransactiontestcode=rt.ntransactiontestcode"
						+ " and rt.npreregno = rar.npreregno and tm.ntestcode=rt.ntestcode"
						+ " and ts.ntranscode=rth.ntransactionstatus and u.nusercode=rth.nusercode"
						+ " and ur.nuserrolecode=rth.nuserrolecode and rth.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rar.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tm.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and rth.nsitecode=rt.nsitecode  and rt.nsitecode=rar.nsitecode and rar.nsitecode="
						+ userInfo.getNtranssitecode()
						+ " and rth.ntesthistorycode = any("
						+ "	select max(ntesthistorycode) ntesthistorycode from registrationtesthistory"
						+ "	where ntransactiontestcode in(" + ntransactionTestCode + ") and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  nsitecode="
						+ userInfo.getNtranssitecode() + "	group by ntransactiontestcode" + "	)";

				List<RegistrationTestHistory> lstregtest = (List<RegistrationTestHistory>) jdbcTemplate
						.query(testAuditGet, new RegistrationTestHistory());
				if (lstregtest.size() > 0) {
					insertAuditList.add(lstregtest);
					multiLingualActionList.add(lstregtest.get(0).getStransdisplaystatus());
					auditUtilityFunction.fnInsertListAuditAction(insertAuditList, 1, null, multiLingualActionList,
							userInfo);
				}
				final UserInfo systemUserInfo = new UserInfo(userInfo);
				systemUserInfo.setNusercode(userCode);
				systemUserInfo.setNuserrole(roleCode);
				systemUserInfo.setNdeputyusercode(userCode);
				systemUserInfo.setNdeputyuserrole(roleCode);

				final int nseqNoRegistrationHistory = (int) inputMap.get("AutoRegistrationHistorySequence");
				final int nseqNoSampleHistory = (int) inputMap.get("AutoRegistrationSampleSequence");
				updateSampleStatus(ntransStatus, npreregno, systemUserInfo, scomments, nseqNoRegistrationHistory);

				updateSubSampleStatus(ntransStatus, ntransactionsamplecode, systemUserInfo, scomments,
						nseqNoSampleHistory);

				return new ResponseEntity<Object>(HttpStatus.OK);
				/*** Test Status update ends here */
			} else {

				/**** if no test are valid for approval *****/
				return new ResponseEntity<Object>(HttpStatus.OK);
			}
		} else {

			/**** if there is no role wise auto approval *****/
			return new ResponseEntity<Object>(HttpStatus.OK);
		}
	}

	@Override
	public Map<String, Object> viewAttachment(Map<String, Object> objMap,final UserInfo userInfo) throws Exception {
		Map<String, Object> map = new HashMap<>();

		if ((int) objMap.get("nattachmenttypecode") == Enumeration.AttachmentType.FTP.gettype()) {

			map = ftpUtilityFunction.FileViewUsingFtp((String) objMap.get("ssystemfilename"), -1, userInfo, "", "");

		} else {

			final String sQuery = "select slinkname from linkmaster where nlinkcode=" + objMap.get("nlinkcode")
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+" and nsitecode="+userInfo.getNmastersitecode()+"";
			
			final LinkMaster objlinkmaster = jdbcTemplate.queryForObject(sQuery, new LinkMaster());
			map.put("AttachLink", objlinkmaster.getSlinkname() + objMap.get("sfinal"));
		}

		return map;
	}

	@Override
	public RegistrationDecisionHistory getSampleDecisionStatus(final int npreRegNo,final UserInfo userInfo) throws Exception {// --
		
		final String query = "select max(ndecisionstatus) as ndecisionstatus from "
				+ " registrationdecisionhistory  where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and npreregno=" + npreRegNo
				+ " and nsitecode="+userInfo.getNtranssitecode()+" ";


		final RegistrationDecisionHistory decisionHistory = (RegistrationDecisionHistory) jdbcTemplate.query(query,
				new RegistrationDecisionHistory());
		return decisionHistory;
	}

	@Override
	public ResponseEntity<Object> getEnforceCommentsHistory(Map<String, Object> inputMap,final UserInfo userInfo)
			throws Exception {
		final String fetchField = inputMap.get("fetchField").toString();
		final String getCommentHistory = "select nresultparamcommenthistorycode,(jsondata->>'" + fetchField
				+ "')::character varying " + fetchField
				+ " from resultparamcommenthistory where ntransactionresultcode = "
				+ inputMap.get("ntransactionresultcode") + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and jsondata ? '" + fetchField + "'"
				+ " and  nsitecode=" + userInfo.getNtranssitecode();

		return new ResponseEntity<Object>(jdbcTemplate.query(getCommentHistory, new ResultParamCommentHistory()),
				HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> viewReport(Map<String, Object> objMap,final  UserInfo userInfo) throws Exception {// --

		Map<String, Object> map = new HashMap<>();
		final String validateQuery = " select ch.ncoahistorycode,rar.sarno,rar.npreregno,ch.dgenerateddate,ch.ssystemfilename,"
				+ " rt.scoareporttypename,sc.ssectionname,us.sfirstname+' '+us.slastname susername,ur.suserrolename"
				+ " from coahistory ch,coareporttype rt,section sc,users us,userrole ur,registrationarno rar"
				+ " where ch.ncoahistorycode = " + objMap.get("ncoahistorycode") + " and ch.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
				+ " and ch.npreregno = rar.npreregno and rar.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
				+ " and rt.ncoareporttypecode = ch.ncoareporttypecode and rt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
				+ " and sc.nsectioncode = ch.nsectioncode and sc.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
				+ " and us.nusercode = ch.nusercode and us.nstatus =  "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
				+ " and ur.nuserrolecode = ch.nuserrolecode and ur.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and rar.nsitecode="+ userInfo.getNtranssitecode()
				+ " and ch.nsitecode="+ userInfo.getNtranssitecode()
				+ " and sc.nsitecode="+ userInfo.getNmastersitecode()
				+ " and us.nsitecode="+ userInfo.getNmastersitecode();

		final COAHistory validateAtttachment = (COAHistory) jdbcUtilityFunction.queryForObject(validateQuery,
				COAHistory.class, jdbcTemplate);
		if (validateAtttachment != null) {
			map = ftpUtilityFunction.FileViewUsingFtp((String) objMap.get("ssystemfilename"), -1, userInfo, "", "");
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> lstObject = new ArrayList<>();
			multilingualIDList.add("IDS_DOWNLOADREPORT");
			lstObject.add(validateAtttachment);
			auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, multilingualIDList, userInfo);
			return new ResponseEntity<Object>(map, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}

	private Map<String, Object> updateSubSampleStatus(final int ntransStatus,final String transactionSampleCode,final UserInfo userInfo,
			final String scomments,final int nreghistcode) throws Exception {
		final StringJoiner joiner = new StringJoiner(",");
		String approvalPreregno = "";
		String partialPreregno = "";
		String insertPartialReghistory = "";
		String insertReghistory = "";
		final Map<String, Object> rtnMap = new HashMap<>();
		final String approvedSamSampleQuery = "select historycount.ntransactionsamplecode from ("
				+ "select ntransactionsamplecode,count(ntesthistorycode) testcount from registrationtesthistory "
				+ "where  "
				+ " ntransactionstatus =" + ntransStatus + " and  ntransactionsamplecode in ("
				+ transactionSampleCode + ")" + " and nsitecode=" + userInfo.getNtranssitecode()
				+ " and ntesthistorycode=any( select max(ntesthistorycode) ntesthistorycode "
				+ " from registrationtesthistory where ntransactionsamplecode in  (" + transactionSampleCode + ")"
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNtranssitecode() + " group by  ntransactionsamplecode,ntransactiontestcode)"
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by ntransactionsamplecode)historycount,( "
				+ " select rth.ntransactionsamplecode,count(ntesthistorycode) testcount "
				+ " from registrationtest rt,registrationtesthistory rth  where ntransactionstatus not in ("
				+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ", "
				+ Enumeration.TransactionStatus.RELEASED.gettransactionstatus() + ") "
				+ " and rt.nsitecode=rth.nsitecode"
				+ " and rt.nsitecode=" + userInfo.getNtranssitecode()
				+ " and rth.nsitecode=" + userInfo.getNtranssitecode()
				+ " and rt.ntransactiontestcode=rth.ntransactiontestcode  and rth.ntesthistorycode = any( "
				+ " select max(ntesthistorycode) ntesthistorycode from registrationtesthistory where ntransactionsamplecode in ("
				+ transactionSampleCode + ") and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and  nsitecode=" + userInfo.getNtranssitecode()
				+ " group by ntransactiontestcode,ntransactionsamplecode)"
				+ "group by rth.ntransactionsamplecode)testcount "
				+ " where historycount.ntransactionsamplecode=testcount.ntransactionsamplecode "
				+ " and historycount.testcount=testcount.testcount; ";

		final String partialSampleQuery = "select historycount.ntransactionsamplecode from ("
				+ " select rth.ntransactionsamplecode,count(ntesthistorycode) testcount from registrationtesthistory rth,registrationsamplehistory rh"
				+ " where rth.ntransactionstatus=" + ntransStatus
				+ " and rth.ntransactionsamplecode=rh.ntransactionsamplecode"
				+ " and rh.nsamplehistorycode=any( select max(nsamplehistorycode) nreghistorycode"
				+ " from registrationsamplehistory where ntransactionsamplecode in (" + transactionSampleCode
				+ ")  and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by ntransactionsamplecode) and ntesthistorycode=any( "
				+ " select max(ntesthistorycode) ntesthistorycode from registrationtesthistory "
				+ " where ntransactionsamplecode in  (" + transactionSampleCode + ") and nsitecode = "
				+ userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by ntransactionsamplecode,ntransactiontestcode" + " ) and rh.ntransactionstatus!="
				+ Enumeration.TransactionStatus.PARTIALLY.gettransactionstatus()
				+ " and rth.nsitecode = rh.nsitecode and rh.nsitecode = " + userInfo.getNtranssitecode()
				+ " and rth.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by rth.ntransactionsamplecode"
				+ ")historycount,(select rth.ntransactionsamplecode,count(ntesthistorycode) testcount "
				+ " from registrationtesthistory rth,registrationtest rt "
				+ " where ntransactionstatus not in ("
				+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + "    ) "
				+ " and rt.ntransactiontestcode=rth.ntransactiontestcode "
				+ " and rth.ntesthistorycode = any( "
				+ " select max(ntesthistorycode) ntesthistorycode from registrationtesthistory "
				+ " where ntransactionsamplecode in (" + transactionSampleCode + ") "
				+ " and nsitecode = " + userInfo.getNtranssitecode() + " and  nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by ntransactiontestcode,ntransactionsamplecode"
				+ "        ) and  rth.nsitecode = rt.nsitecode and rth.nsitecode = " + userInfo.getNtranssitecode()+""
				+ " and rt.nsitecode = " + userInfo.getNtranssitecode()
				+ " group by rth.ntransactionsamplecode)testcount "
				+ " where historycount.ntransactionsamplecode = testcount.ntransactionsamplecode "
				+ " and historycount.testcount!=testcount.testcount ;";

		final List<RegistrationTestHistory> approvedSampleList = jdbcTemplate.query(approvedSamSampleQuery,
				new RegistrationTestHistory());
		final List<RegistrationTestHistory> partialSampleList = jdbcTemplate.query(partialSampleQuery,
				new RegistrationTestHistory());
		int seqNoRegistraionHistory = nreghistcode;

		if (!approvedSampleList.isEmpty()) {
			approvalPreregno = approvedSampleList.stream()
					.map(objpreregno -> String.valueOf(objpreregno.getNtransactionsamplecode())).collect(Collectors.joining(","));

			insertReghistory = "insert into registrationsamplehistory (nsamplehistorycode,npreregno,ntransactionsamplecode,ntransactionstatus,dtransactiondate,"
					+ "nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments,nsitecode,nstatus, noffsetdtransactiondate, ntransdatetimezonecode) "
					+ "select " + seqNoRegistraionHistory
					+ "+rank()over(order by rs.ntransactionsamplecode) nsamplehistorycode,rs.npreregno,rs.ntransactionsamplecode,"
					+ ntransStatus + " ntransactionstatus," + "'" + dateUtilityFunction.getCurrentDateTime(userInfo)
					+ "'  dtransactiondate," + userInfo.getNusercode() + " nusercode," + userInfo.getNuserrole()
					+ " nuserrolecode," + "" + userInfo.getNdeputyusercode() + " ndeputyusercode,"
					+ userInfo.getNdeputyuserrole() + " ndeputyuserrolecode,N'" + scomments + "' scomments,"
					+ userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " nstatus, " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
					+ userInfo.getNtimezonecode()
					+ " from registrationsample rs,registrationsamplehistory rsh where  rs.ntransactionsamplecode=rsh.ntransactionsamplecode and rs.ntransactionsamplecode in ("
					+ approvalPreregno + ") and rs.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rsh.ntransactionstatus!="
					+ ntransStatus
					+ " and rsh.nsamplehistorycode in(  select max(registrationsamplehistory.nsamplehistorycode) from registrationsamplehistory "
					+ "where ntransactionsamplecode in (" + approvalPreregno + ") and nsitecode="+userInfo.getNtranssitecode()+""
					+ " group by ntransactionsamplecode ) "
					+ " and rs.nsitecode=" + userInfo.getNtranssitecode()
					+ " and rsh.nsitecode=" + userInfo.getNtranssitecode()
					+ " order by rs.ntransactionsamplecode;";

			seqNoRegistraionHistory = seqNoRegistraionHistory + approvedSampleList.size();
			joiner.add(approvalPreregno);
		}
		if (!partialSampleList.isEmpty()) {
			partialPreregno = partialSampleList.stream()
					.map(objpreregno -> String.valueOf(objpreregno.getNtransactionsamplecode()))
					.collect(Collectors.joining(","));

			insertPartialReghistory = "insert into registrationsamplehistory (nsamplehistorycode,npreregno,ntransactionsamplecode,ntransactionstatus,dtransactiondate,"
					+ "nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments,nsitecode,nstatus) "
					+ "select " + seqNoRegistraionHistory
					+ "+rank()over(order by ntransactionsamplecode) nsamplehistorycode,npreregno,ntransactionsamplecode,"
					+ Enumeration.TransactionStatus.PARTIALLY.gettransactionstatus() + " ntransactionstatus," + "'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo)
					+ "'  dtransactiondate," + userInfo.getNusercode() + " nusercode," + userInfo.getNuserrole()
					+ " nuserrolecode," + "" + userInfo.getNdeputyusercode() + " ndeputyusercode,"
					+ userInfo.getNdeputyuserrole() + " ndeputyuserrolecode,N'" + scomments + "' scomments,"
					+ userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " nstatus from registrationsample where ntransactionsamplecode in (" + partialPreregno
					+ ") and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNtranssitecode() + " order by ntransactionsamplecode;";
			joiner.add(partialPreregno);
			seqNoRegistraionHistory = seqNoRegistraionHistory + partialSampleList.size();
		}

		if (!approvedSampleList.isEmpty() || !partialSampleList.isEmpty()) {
			jdbcTemplate.execute(insertReghistory);
			jdbcTemplate.execute(insertPartialReghistory);


		}
		rtnMap.put("nreghistcode", seqNoRegistraionHistory);
		return rtnMap;
	}

	
	public ResponseEntity<Object> getTestBasedOnCompletedBatch(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		Map<String, Object> returnMap = new HashMap<>();
		List<?> lstbatch = new ArrayList<>();
		final String ntranscode = (String) inputMap.get("ntranscode");

		String strBatch = "select max(bh.sbatcharno) sbatcharno,bh.nbatchmastercode  "
				+ "from batchiqctransaction bt,registrationtest rt,batchhistory bh,registrationtesthistory rth, registrationarno ra "
				+ " where  rt.ntransactiontestcode = bt.ntransactiontestcode"
				+ " and bh.nbatchmastercode = bt.nbatchmastercode "
				+ " and rth.ntransactiontestcode = bt.ntransactiontestcode and ra.npreregno = bt.npreregno "
				+ " and rth.ntesthistorycode = (select max(ntesthistorycode) from registrationtesthistory where npreregno = bt.npreregno "
				+ " and ntransactionsamplecode = bt.ntransactionsamplecode and ntransactiontestcode = bt.ntransactiontestcode "
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by ntransactiontestcode,ntransactionsamplecode,npreregno)"
				+ " and bh.nbatchhistorycode in (select max(nbatchhistorycode) from batchhistory where ntransactionstatus = "
				+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNtranssitecode() + " group by nbatchmastercode)" + " and rth.ntransactionstatus in ("
				+ ntranscode + ") " + " and ra.napprovalversioncode = "
				+ Integer.valueOf(inputMap.get("napprovalversioncode").toString()) +" and bt.nstatus = rt.nstatus"
				+ " and rt.ntestcode = " + (int) inputMap.get("ntestcode") + ""
				+ " and rt.nstatus = rth.nstatus and rt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bh.nsitecode="
				+ userInfo.getNtranssitecode() + ""
				+ " and bt.nsitecode="+userInfo.getNtranssitecode()+" "
				+ " and rt.nsitecode="+userInfo.getNtranssitecode()+" "
				+ " and rth.nsitecode="+userInfo.getNtranssitecode()+" "
				+ " and ra.nsitecode="+userInfo.getNtranssitecode()+" "
				+ " group by bh.nbatchmastercode order by bh.nbatchmastercode desc ";

		lstbatch = jdbcTemplate.queryForList(strBatch);

		if (lstbatch.size() > 0) {
			returnMap.put("Batchvalues", lstbatch);
			returnMap.put("realBatchvaluesList", lstbatch);
			returnMap.put("defaultBatchvalue", new ArrayList<>());
			returnMap.put("realBatchvalue", new ArrayList<>());
		} else {
			returnMap.put("Batchvalues", lstbatch);
			returnMap.put("defaultBatchvalue", lstbatch);
		}
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	private void validationForRelease(final String npreregno,final String ntransactionsamplecode,final String ntransactionTestCode,UserInfo userInfo) {


		final String sQuery = "select ncoachildcode, ncoaparentcode, npreregno, ntransactionsamplecode,"
				+ "ntransactiontestcode, nusercode, nuserrolecode, dmodifieddate, nsitecode, nstatus from coachild where npreregno in(" + npreregno + ") and ntransactionsamplecode in("
				+ ntransactionsamplecode + ")" + " and ntransactionTestCode in(" + ntransactionTestCode + ") and nsitecode="+userInfo.getNtranssitecode()+" "
				+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";

		final List<COAChild> validateCOAChild = (List<COAChild>) jdbcTemplate.query(sQuery, new COAChild());
		if (!validateCOAChild.isEmpty()) {
			String spreregno = validateCOAChild.stream().map(x -> String.valueOf(x.getNpreregno()))
					.collect(Collectors.joining(","));
			String stransactionsamplecode = validateCOAChild.stream()
					.map(x -> String.valueOf(x.getNtransactionsamplecode())).collect(Collectors.joining(","));
			String stransactionTestCode = validateCOAChild.stream()
					.map(x -> String.valueOf(x.getNtransactiontestcode())).collect(Collectors.joining(","));

			final String sUpdateQuery = "update coachild set nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "" + " where npreregno in("
					+ spreregno + ") and ntransactionsamplecode in(" + stransactionsamplecode
					+ ") and ntransactionTestCode in(" + stransactionTestCode + ")";
			jdbcTemplate.execute(sUpdateQuery);
		}
	}

	@Override
	public ResponseEntity<Map<String, Object>> checkReleaseRecord(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {
		final Map<String, Object> returnMap = new HashMap<String, Object>();
		final String npreregno = (String) inputMap.get("npreregno");
		final String ntransactionsamplecode = (String) inputMap.get("ntransactionsamplecode");
		final String ntransactionTestCode = (String) inputMap.get("ntransactiontestcode");

		final String sQuery = "select ncoachildcode, ncoaparentcode, npreregno, ntransactionsamplecode,"
				+ " ntransactiontestcode, nusercode, nuserrolecode, dmodifieddate, nsitecode, nstatus from "
				+ " coachild where npreregno in(" + npreregno + ") and ntransactionsamplecode in("
				+ ntransactionsamplecode + ")" + " and ntransactionTestCode in(" + ntransactionTestCode
				+ ") and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNtranssitecode() + "";
		
		List<COAChild> validateCOAChild = (List<COAChild>) jdbcTemplate.query(sQuery, new COAChild());
		if (!validateCOAChild.isEmpty()) {
			returnMap.put("rtn", Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		}

		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getSampleViewDetails(Map<String, Object> inputMap) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final ObjectMapper objMapper = new ObjectMapper();

		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);


		String queryStr = "select r.npreregno,rt.ntransactiontestcode,(ap.jsondata->>'sfinal') sfinal,(ap.jsondata->>'stestsynonym') stestsynonym,(ap.jsondata->>'sparametersynonym') sparametersynonym,coalesce(gg.jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',gg.jsondata->'sdisplayname'->>'en-US') as sgradename,ra.sarno,rsa.ssamplearno,'-' as sreportno,(select dtransactiondate from registrationtesthistory where ntransactiontestcode=rt.ntransactiontestcode and ntransactionstatus="+Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()+" ) sregdate  "
				+ " from registration r,registrationtest rt,approvalparameter ap,grade gg,registrationarno ra,registrationsamplearno rsa "
				+ " where gg.ngradecode=ap.ngradecode and r.npreregno =ra.npreregno and rsa.ntransactionsamplecode=rt.ntransactionsamplecode and   (r.jsondata->>'spatientid')::character varying='"
				+ inputMap.get("PatientId") + "' and r.npreregno=rt.npreregno and r.npreregno=ap.npreregno "
				+ " and rt.ntransactiontestcode=ap.ntransactiontestcode and ap.npreregno=" + inputMap.get("npreregno")
				+ " and r.nsitecode="+userInfo.getNtranssitecode()+" "
				+ " and rt.nsitecode="+userInfo.getNtranssitecode()+" "
				+ " and ap.nsitecode="+userInfo.getNtranssitecode()+" "
				+ " and ra.nsitecode="+userInfo.getNtranssitecode()+" "
				+ " and rsa.nsitecode="+userInfo.getNtranssitecode()+" "
				+ " and r.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and rt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and ap.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and ra.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and rsa.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and gg.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";

		final List<ApprovalParameter> lstSampleDetail = jdbcTemplate.query(queryStr, new ApprovalParameter());

		final String queryString = "select  arr->>'npreregno' npreregno,arr->>'ntransactiontestcode'ntransactiontestcode,arr->>'sfinal' sfinal, "
				+ "arr->>'stestsynonym' stestsynonym,arr->>'sparametersynonym' sparametersynonym,arr->>'stestsynonym' stestsynonym, "
				+ "arr->>'sgradename' sgradename,arr->>'sarno' sarno,arr->>'ssamplearno' ssamplearno "
				+ ",(select dtransactiondate from registrationtesthistory where ntransactiontestcode=t.ntransactiontestcode and ntransactionstatus="+Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()+" ) sregdate ,"
				+ " c.sreportno  "
				+ " from registrationtest t, patienthistory p,coaparent c,jsonb_array_elements(p.jsondata) arr where "
				+ " t.ntransactiontestcode in( " + "select ntransactiontestcode from patienthistory where   nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and   spatientid = '"
				+ inputMap.get("PatientId") + "') "
				+ " and t.ntransactiontestcode=p.ntransactiontestcode and t.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and c.ncoaparentcode=p.ncoaparentcode and  p.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and t.nsitecode="+userInfo.getNtranssitecode()+" "
				+ " and c.nsitecode="+userInfo.getNtranssitecode()+" "
				+ " order by c.ncoaparentcode desc ";
		
		final List<ApprovalParameter> lstSampleDetails = jdbcTemplate.query(queryString, new ApprovalParameter());

		String patientqry = "select spatientid,CONCAT(sfirstname ,' ',slastname) sfirstname,sage,(g.jsondata->'sgendername'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "') as sgendername,(select sarno from registrationarno where npreregno=" + inputMap.get("npreregno")
				+ ") as sarno from patientmaster p,gender g where g.ngendercode=p.ngendercode and  p.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  spatientid = '"
				+ inputMap.get("PatientId") + "' and g.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";

		final List<Patient> instGet = jdbcTemplate.query(patientqry, new Patient());

		final List<Patient> lstUTCConvertedDate = objMapper.convertValue(
				dateUtilityFunction.getSiteLocalTimeFromUTC(instGet, Arrays.asList("sregdate", "scompletedate"),
						Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null, false),
				new TypeReference<List<Patient>>() {
				});
		outputMap.put("viewdetails", lstUTCConvertedDate);
		outputMap.put("AuditModifiedComments", lstSampleDetails);
		outputMap.put("CurrentResult", lstSampleDetail);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	
	private ApprovalConfigVersion checkUserAvailablity(final String napproveconfversioncode,final UserInfo userInfo)
			throws Exception {

		final String sUsermappingQuery = " select um.napprovalconfigcode from usermapping um,approvalconfigversion acv"
				+ " where acv.napproveconfversioncode=" + napproveconfversioncode + " "
				+ " and acv.napprovalconfigcode=um.napprovalconfigcode " + " and um.nchildrolecode= "
				+ userInfo.getNuserrole() + " and um.nchildusercode=" + userInfo.getNusercode() + ""
				+ " and um.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and acv.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and acv.nsitecode=" + userInfo.getNmastersitecode() + " and um.nsitecode="
				+ userInfo.getNtranssitecode() + "" + " group by um.napprovalconfigcode";

		return (ApprovalConfigVersion) jdbcUtilityFunction.queryForObject(sUsermappingQuery,
				ApprovalConfigVersion.class, jdbcTemplate);
	}

	public String scheduleSkip() {
		return jdbcTemplate.queryForObject("select ssettingvalue from settings where nsettingcode = 43 and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), String.class);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getTestResultCorrection(Map<String, Object> inputMap,final UserInfo userInfo)
			throws Exception {

		Map<String, Object> returnMap = new HashMap<>();
		// Modified the Query by sonia What i have done Mentioned in jira: ALPD-4275
		final String sQuery = " select rp.ntransactionresultcode,cm.scolorhexcode,r.npreregno,rar.sarno,rs.ntransactionsamplecode,rt.ntransactiontestcode,rsar.ssamplearno,"
				+ " rp.jsondata->>'stestsynonym' as stestsynonym,rp.jsondata->>'sparametersynonym' as sparametersynonym, "
				+ "CASE WHEN rp.nunitcode =-1 THEN rp.jsondata->>'sfinal'::character VARYING  ELSE concat(rp.jsondata->>'sfinal',' ',u.sunitname) END as sfinal,"
				+ " coalesce(gd.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',gd.jsondata->'sdisplayname'->>'en-US')  sgradename, "
				+ " case  when rp.jsondata ->> 'sresultaccuracyname'  isnull  then ''  else  rp.jsondata ->> 'sresultaccuracyname' end AS sresultaccuracyname, "
				+ "(rp.jsondata->>'smina')::character varying(10) as smina,(rp.jsondata->>'sminb')::character varying(10) as sminb,"
				+ "(rp.jsondata->>'smaxa')::character varying(10) as smaxa,(rp.jsondata->>'smaxb')::character varying(10) as smaxb,"
				+ "(rp.jsondata->>'sminlod')::character varying(10) as sminlod,(rp.jsondata->>'sminloq')::character varying(10) as sminloq,"
				+ "(rp.jsondata->>'smaxlod')::character varying(10) as smaxlod,(rp.jsondata->>'smaxloq')::character varying(10) as smaxloq "
				+ " from registration r,registrationarno rar,registrationsample rs,registrationsamplearno rsar,"
				+ " registrationtest rt,colormaster cm,approvalparameter rp,grade gd,unit u "
				+ " where rp.ngradecode = gd.ngradecode and rp.nunitcode =u.nunitcode and rp.ntransactiontestcode = rt.ntransactiontestcode and r.npreregno = rar.npreregno  "
				+ " and rs.npreregno = r.npreregno and rsar.npreregno=rp.npreregno   "
				+ " and rsar.npreregno = rs.npreregno and rsar.npreregno = rp.npreregno and rs.ntransactionsamplecode = rsar.ntransactionsamplecode "
				+ " and rs.ntransactionsamplecode=rt.ntransactionsamplecode " + " and gd.ncolorcode = cm.ncolorcode "
				+ " and rt.ntransactiontestcode in(" + inputMap.get("ntransactiontestcode") + ")"
				+ " and rt.nsitecode=r.nsitecode and r.nsitecode=rar.nsitecode and rar.nsitecode=rsar.nsitecode "
				+ " and r.nsitecode=" + userInfo.getNtranssitecode() + " "
			    + " and rt.nsitecode=" + userInfo.getNtranssitecode() + ""
			    + " and rar.nsitecode=" + userInfo.getNtranssitecode() + ""
				+ " and rsar.nsitecode=" + userInfo.getNtranssitecode() + ""
			    + " and r.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rar.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rs.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rsar.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and gd.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final List<ReleaseParameter> releaseParameterList = jdbcTemplate.query(sQuery, new ReleaseParameter());

		returnMap.put("ResultCorrection", releaseParameterList);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);

	}

	@Override

	public ResponseEntity<Map<String, Object>> updateReleaseParameter(MultipartHttpServletRequest request, final UserInfo userInfo)
			throws Exception {

		final ObjectMapper objMapper=new ObjectMapper();
		final String lockquery = "lock lockresultcorrection" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(lockquery);

		Map<String, Object> returnMap = new HashMap<>();
		JSONObject jsonAuditOldData = new JSONObject();
		JSONObject jsonAuditNewData = new JSONObject();
		JSONObject auditActionType = new JSONObject();
		String query = "";
		String updateQuery = "";
		String result = request.getParameter("resultData");
		List<ReleaseParameter> lstTransSampResults = objMapper.readValue(result,
				new TypeReference<List<ReleaseParameter>>() {
				});
		final int nregtypecode = Integer.parseInt(request.getParameter("nregtypecode"));
		final int nregsubtypecode = Integer.parseInt(request.getParameter("nregsubtypecode"));
		final int ndesigntemplatemappingcode = Integer.parseInt(request.getParameter("ndesigntemplatemappingcode"));
		
		final int ntransactionresultcode = lstTransSampResults.get(0).getNtransactionresultcode();
		final int ntransactiontestcode = lstTransSampResults.get(0).getNtransactiontestcode();

		//Added by sonia on 4th July 2025 for jira id:ALPDJ21-6
		final int npreregno = Integer.parseInt(request.getParameter("npreregno"));		
		final String jsonString = request.getParameter("data"); 
		final Map<String,Object> data = new JSONObject(jsonString).toMap();
		
		if (lstTransSampResults != null && !lstTransSampResults.isEmpty()) {

			query = " select rp.*,rp.jsondata->>'sfinal' sfinal ,rp.jsondata->>'sresult' sresult ,"
					+ " t.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
					+ "' as stransdisplaystatus," + " rp.jsondata->>'stestsynonym' stestsynonym,"
					+ " rp.jsondata->>'sresultaccuracyname' sresultaccuracyname,"
					+ " rp.jsondata->>'sparametersynonym' sparametersynonym,"
					+ " r.sarno,rs.ssamplearno,g.sgradename,u.sunitname,u.nunitcode "
					+ " from approvalparameter rp,transactionstatus t,registrationarno r,registrationsamplearno rs,"
					+ " registrationtest rt,grade g,unit u"
					+ " where t.ntranscode=rp.ntransactionstatus and r.npreregno=rp.npreregno and r.npreregno=rs.npreregno"
					+ " and rs.npreregno=rp.npreregno and rt.ntransactionsamplecode=rs.ntransactionsamplecode"
					+ " and rt.ntransactiontestcode=rp.ntransactiontestcode and u.nunitcode=rp.nunitcode "
					+ " and rt.npreregno=rs.npreregno and r.npreregno=rt.npreregno and g.ngradecode=rp.ngradecode "
					+ " and rs.nsitecode = r.nsitecode and rs.nsitecode = rp.nsitecode "
					+ " and r.nsitecode = "+userInfo.getNtranssitecode() + "  "
					+ " and rs.nsitecode = "+userInfo.getNtranssitecode() + "  "
					+ " and rp.nsitecode = "+userInfo.getNtranssitecode() + "  "
					+ " and rs.nsitecode = "+userInfo.getNtranssitecode() + "  "
					+ " and g.nsitecode = "+userInfo.getNmastersitecode() + "  "
					+ " and rp.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and t.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and r.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and rs.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and rt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and g.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and u.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and rp.ntransactionresultcode = " + ntransactionresultcode + " ";
			List<ReleaseParameter> lstSampleResults = jdbcTemplate.query(query, new ReleaseParameter());

			final String currentDateTime = dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ")
					.replace("Z", "");

			if (lstSampleResults != null && !lstSampleResults.isEmpty()) {

				Map<String, Object> jsonData = lstTransSampResults.get(0).getJsondata();
				final String sfinal = StringEscapeUtils.unescapeJava(jsonData.get("sfinal").toString());
				final String sresult = StringEscapeUtils.unescapeJava(jsonData.get("sresult").toString());
				jsonData.put("sfinal", sfinal);
				jsonData.put("sresult", sresult);
				jsonData.put("dentereddate", currentDateTime);
				jsonData.put("dentereddatetimezonecode", userInfo.getNtimezonecode());
				jsonData.put("noffsetdentereddate",
						dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()));

				updateQuery = updateQuery + "update approvalparameter set " + " ngradecode="
						+ lstTransSampResults.get(0).getNgradecode() + "," + " nenteredby= "
						+ lstTransSampResults.get(0).getNenteredby() + "," + " nenteredrole="
						+ lstTransSampResults.get(0).getNenteredrole() + "," + " ndeputyenteredby="
						+ userInfo.getNdeputyusercode() + "," + " ndeputyenteredrole=" + userInfo.getNdeputyuserrole()
						+ "," + " nattachmenttypecode = "
						+ (lstTransSampResults.get(0).getNparametertypecode() ==  Enumeration.ParameterType.ATTACHEMENT.getparametertype() ? 1 : -1) + " ,"
						+ " jsondata = jsondata || '"
						+ stringUtilityFunction.replaceQuote(objMapper.writeValueAsString(jsonData)) + "'" + " ,nunitcode="
						+ lstTransSampResults.get(0).getNunitcode() + " where ntransactionresultcode = "
						+ lstTransSampResults.get(0).getNtransactionresultcode() + ";";

				updateQuery = updateQuery + "update releaseparameter set " + " ngradecode="
						+ lstTransSampResults.get(0).getNgradecode() + "," + " nenteredby= "
						+ lstTransSampResults.get(0).getNenteredby() + "," + " nenteredrole="
						+ lstTransSampResults.get(0).getNenteredrole() + "," + " ndeputyenteredby="
						+ userInfo.getNdeputyusercode() + "," + " ndeputyenteredrole=" + userInfo.getNdeputyuserrole()
						+ "," + " nattachmenttypecode = "
						+ (lstTransSampResults.get(0).getNparametertypecode() ==  Enumeration.ParameterType.ATTACHEMENT.getparametertype() ? 1 : -1) + " ,"
						+ " jsondata = jsondata || '"
						+ stringUtilityFunction.replaceQuote(objMapper.writeValueAsString(jsonData)) + "'" + " ,nunitcode="
						+ lstTransSampResults.get(0).getNunitcode() + " where ntransactionresultcode = "
						+ lstTransSampResults.get(0).getNtransactionresultcode() + ";";

				updateQuery = updateQuery + "update resultparameter set " + " ngradecode="
						+ lstTransSampResults.get(0).getNgradecode() + "," + " nenteredby= "
						+ lstTransSampResults.get(0).getNenteredby() + "," + " nenteredrole="
						+ lstTransSampResults.get(0).getNenteredrole() + "," + " ndeputyenteredby="
						+ userInfo.getNdeputyusercode() + "," + " ndeputyenteredrole=" + userInfo.getNdeputyuserrole()
						+ "," + " nattachmenttypecode = "
						+ (lstTransSampResults.get(0).getNparametertypecode() == Enumeration.ParameterType.ATTACHEMENT.getparametertype() ? 1 : -1) + " ,"
						+ " jsondata = jsondata || '"
						+ stringUtilityFunction.replaceQuote(objMapper.writeValueAsString(jsonData)) + "'" + " ,nunitcode="
						+ lstTransSampResults.get(0).getNunitcode() + " where ntransactionresultcode = "
						+ lstTransSampResults.get(0).getNtransactionresultcode() + ";";

				jdbcTemplate.execute(updateQuery);
				
				//Added by sonia on 4th July 2025 for jira id:ALPDJ21-6
				final String getapprovalparam = "select count(0) from approvalparameter ap where ap.npreregno = "
						+ npreregno + " and ap.nsitecode = " + userInfo.getNtranssitecode()
						+ " and ap.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ap.ngradecode in (" + Enumeration.Grade.OOS.getGrade() + ", "
						+ Enumeration.Grade.OOT.getGrade() + ");";

				int count = jdbcTemplate.queryForObject(getapprovalparam, Integer.class);
				boolean flag = count > 0 ? true : false;

				String updateSampleflagstatus = "update registrationflagstatus set bflag = " + flag + " where"
											  + " npreregno = (" + npreregno + ") and nsitecode = "+ userInfo.getNtranssitecode() + "";

				jdbcTemplate.execute(updateSampleflagstatus);

				query = " select rp.*,rp.jsondata->>'sfinal' sfinal ," + " t.jsondata->'stransdisplaystatus'->>'"
						+ userInfo.getSlanguagetypecode() + "' as stransdisplaystatus,"
						+ " rp.jsondata->>'stestsynonym' stestsynonym,"
						+ " rp.jsondata->>'sresultaccuracyname' sresultaccuracyname,"
						+ " rp.jsondata->>'sparametersynonym' sparametersynonym,"
						+ " r.sarno,rs.ssamplearno,g.sgradename,u.sunitname,"
						+ " rp.jsondata->>'dentereddate' entereddate, "
						+ " rp.jsondata->>'dentereddatetimezonecode' dentereddatetimezonecode,"
						+ " rp.jsondata->>'noffsetdentereddate' noffsetdentereddate "
						+ " from approvalparameter rp,transactionstatus t,registrationarno r,registrationsamplearno rs,"
						+ " registrationtest rt,grade g,unit u "
						+ " where t.ntranscode=rp.ntransactionstatus and r.npreregno=rp.npreregno and r.npreregno=rs.npreregno"
						+ " and rs.npreregno=rp.npreregno and rt.ntransactionsamplecode=rs.ntransactionsamplecode"
						+ " and rt.ntransactiontestcode=rp.ntransactiontestcode and u.nunitcode=rp.nunitcode  "
						+ " and rt.npreregno=rs.npreregno and rs.nsitecode = r.nsitecode and rp.nsitecode = rs.nsitecode and r.nsitecode = "
						+ userInfo.getNtranssitecode() + " and g.nsitecode="+userInfo.getNmastersitecode()+""
						+" and u.nsitecode="+userInfo.getNmastersitecode()+" "
						+ " and r.npreregno=rt.npreregno and g.ngradecode=rp.ngradecode  and rp.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and t.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and r.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and rs.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and rt.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and g.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and u.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and rp.ntransactionresultcode = " + ntransactionresultcode + " ";

				List<ReleaseParameter> lstSampleResultsafter = jdbcTemplate.query(query, new ReleaseParameter());

				jsonAuditOldData.put("approvalparameter", lstSampleResults);
				jsonAuditNewData.put("approvalparameter", lstSampleResultsafter);
				auditActionType.put("approvalparameter", "IDS_RESULTCORRECTION");
				Map<String, Object> objMap = new HashMap<>();
				objMap.put("nregtypecode", nregtypecode);
				objMap.put("nregsubtypecode", nregsubtypecode);
				objMap.put("ndesigntemplatemappingcode", ndesigntemplatemappingcode);

				if (!(lstSampleResults.get(0).getSresult().toString()
						.equalsIgnoreCase((String) jsonData.get("sresult")))) {
					query = " select nsequenceno from seqnoregistration where stablename=N'resultchangehistory'"
							+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";
					int nresultchangehistoryno = jdbcTemplate.queryForObject(query, Integer.class);

					final String insertResultChange = "insert into resultchangehistory (nresultchangehistorycode,nformcode, ntransactionresultcode,ntransactiontestcode,npreregno,"
							+ "ngradecode,ntestgrouptestparametercode,nparametertypecode,nenforceresult,nenforcestatus,"
							+ "nenteredby,nenteredrole,ndeputyenteredby,ndeputyenteredrole,jsondata,nsitecode,nstatus) "
							+ "select " + nresultchangehistoryno
							+ "+RANK()over(order by ntransactionresultcode) nresultchangehistorycode,"
							+ userInfo.getNformcode() + ",ntransactionresultcode,ntransactiontestcode,npreregno,"
							+ "ngradecode,ntestgrouptestparametercode,"		
							+ lstSampleResultsafter.get(0).getNparametertypecode() + " nparametertypecode," + ""
							+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " nenforceresult,"
							+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " nenforcestatus,"
							+ userInfo.getNusercode() + " nenteredby," + userInfo.getNuserrole() + " nenteredrole," + ""
							+ userInfo.getNdeputyusercode() + " ndeputyenteredby," + userInfo.getNdeputyuserrole()
							+ " ndeputyenteredrole,'{" + "     \"sresult\":"
							+ stringUtilityFunction
									.replaceQuote(objMapper.writeValueAsString(lstSampleResults.get(0).getSresult()))
									+ "," + "     \"sfinal\":"
							+ stringUtilityFunction
									.replaceQuote(objMapper.writeValueAsString(lstSampleResults.get(0).getSfinal()))
							+ "," + "    \"nresultaccuracycode\":"
							+ stringUtilityFunction.replaceQuote(
									objMapper.writeValueAsString(lstSampleResults.get(0).getNresultaccuracycode()))
							+ "," + "    \"sresultaccuracyname\":"
							+ stringUtilityFunction.replaceQuote(
									objMapper.writeValueAsString(lstSampleResults.get(0).getSresultaccuracyname()))
							+ "," + "    \"nunitcode\":"
							+ stringUtilityFunction
									.replaceQuote(objMapper.writeValueAsString(lstSampleResults.get(0).getNunitcode()))
							+ "," + "    \"sunitname\":" + stringUtilityFunction.replaceQuote(
									objMapper.writeValueAsString(lstSampleResults.get(0).getSunitname()))
							+ ","
							+ "     \"dentereddate\":\""
							+ lstSampleResultsafter.get(0).getEntereddate().toString() + "\","
							+ "     \"dentereddatetimezonecode\":\""
							+ lstSampleResultsafter.get(0).getDentereddatetimezonecode() + "\","
							+ "     \"noffsetdentereddate\":\""
							+ lstSampleResults.get(0).getNoffsetdentereddate() + "\"" + " }'::jsonb,"
							+ userInfo.getNtranssitecode() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " nstatus from resultparameter where nsitecode = " + userInfo.getNtranssitecode()
							+ " and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and ntransactionresultcode = " + ntransactionresultcode + ";";
					
					jdbcTemplate.execute(insertResultChange);
					jdbcTemplate.execute("update seqnoregistration  set nsequenceno=" + (nresultchangehistoryno + 1)
							+ " where stablename=N'resultchangehistory' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";");
				}
				auditUtilityFunction.fnJsonArrayAudit(jsonAuditOldData, jsonAuditNewData, auditActionType, objMap,
						false, userInfo);

				//commented by sonia on 4th July 2025 for jira id:ALPDJ21-6
//				returnMap.putAll(
//						(Map<String, Object>) getApprovalParameter(String.valueOf(ntransactiontestcode), userInfo)
//								.getBody());
				// ALPD-4071
				returnMap.putAll(
						(Map<String, Object>) getApprovalResultChangeHistory(String.valueOf(ntransactiontestcode),
								userInfo).getBody());
				
				//Added by sonia on 4th July 2025 for jira id:ALPDJ21-6
				returnMap.putAll((Map<String, Object>) getApprovalSample(data, userInfo).getBody());

				final String transcode = "select ntransactiontestcode from registrationtesthistory where ntesthistorycode in "
						+ " (select max(ntesthistorycode) from registrationtesthistory "
						+ " where ntransactiontestcode in(" + request.getParameter("transactiontestcode")
						+ ") group by ntransactiontestcode ) " + " and ntransactionstatus="+Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()+"";
				final List<String> tranStatusList = jdbcTemplate.queryForList(transcode, String.class);
				
				objMap.put("ntransactiontestcode", tranStatusList.stream().collect(Collectors.joining(",")));
				returnMap.putAll((Map<String, Object>) getTestResultCorrection(objMap, userInfo).getBody());

			}
		}

		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getReleaseResults(final int ntransactionresultcode,final UserInfo userInfo) throws Exception {
		final ObjectMapper objMapper=new ObjectMapper();
		Map<String, Object> returnMap = new HashMap<>();

		String getParams = "select row_num,"
				// + " a.jsondata->>'additionalInfo' as
				// \"additionalInfo\",a.jsondata->>'additionalInfoUidata' as
				// \"additionalInfoUidata\"," commentted by Dhanushya RI
				+ " (a.jsondata->>'ntestgrouptestpredefcode')::int as ntestgrouptestpredefcode,a.npreregno,a.jsondata||a.jsonuidata AS jsondata, "
				+ " CASE WHEN row_num = 1 THEN a.sarno ELSE '' END AS sarno,CASE WHEN row_num = 1 THEN a.ssamplearno ELSE '' END AS ssamplearno,CASE WHEN row_num = 1 THEN a.stestsynonym ELSE '' END AS"
				+ " stestsynonym,a.sparametersynonym, "
				+ "CASE WHEN row_num = 1 THEN '['||cast(a.ntestrepeatno as varchar(3))||']['||cast(a.ntestretestno as varchar(3))||']' ELSE '' END AS sretestrepeatcount,"
				+ "a.nparametertypecode,a.sparametertypename,"
				+ "a.ntestrepeatno,a.ntestretestno,a.ntransactionsamplecode,a.ntransactionresultcode,a.ntransactiontestcode,a.nchecklistversioncode, "
				+ "a.ntransactionstatus,a.nroundingdigits,a.smina,a.sminb,a.smaxa,a.smaxb,a.ngradecode, "
				+ "a.ntestparametercode,a.sminlod,a.smaxlod,a.sminloq,a.smaxloq,a.sdisregard,a.sresultvalue"
				+ ",case when a.nparametertypecode=" + Enumeration.ParameterType.PREDEFINED.getparametertype()
				+ " then a.sresult||' ('||a.sfinal||')'"
				+ " else a.sresult end as sresultpredefinedname,a.sresult ,a.sfinal,a.ncalculatedresult,a.nfilesize,case a.sgradename when 'NA' then '' else a.sgradename end as sgradename,"
				+ "a.ntestgrouptestparametercode,a.nsorter,a.ntestgrouptestcode,a.nresultmandatory,a.stransdisplaystatus as sresultmandatory,a.sunitname,a.nunitcode,a.nresultaccuracycode,a.sresultaccuracyname "
				+ " from  " + "("
				+ "select ROW_NUMBER() OVER (PARTITION BY rt.ntransactiontestcode,ra.sarno,rsa.ssamplearno ORDER BY rt.npreregno, tgtp.nsorter, rp.ntransactionresultcode,rt.ntransactiontestcode desc) row_num, "
				+ "rp.jsondata, r.jsonuidata,rp.npreregno,ra.sarno,rsa.ssamplearno,rp.jsondata->>'stestsynonym' as stestsynonym,tgtp.sparametersynonym,rp.nparametertypecode,pt.sparametertypename,"
				+ "coalesce(g.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',g.jsondata->'sdisplayname'->>'en-US') sgradename,"
				+ "rt.ntestrepeatno,rt.ntestretestno,rt.ntransactionsamplecode,"
				+ "rp.ntransactionresultcode,rp.ntransactiontestcode,tgtp.nchecklistversioncode,rp.ntransactionstatus,rp.ncalculatedresult,"
				+ "tgtp.ntestparametercode," + "(rp.jsondata->>'nroundingdigits')::integer as nroundingdigits,"
				+ "(rp.jsondata->>'smina')::character varying(100) as smina,(rp.jsondata->>'sminb')::character varying(100) as sminb,"
				+ "(rp.jsondata->>'smaxa')::character varying(100) as smaxa,(rp.jsondata->>'smaxb')::character varying(100) as smaxb,"
				+ "(rp.jsondata->>'sminlod')::character varying(100) as sminlod,(rp.jsondata->>'sminloq')::character varying(100) as sminloq,"
				+ "(rp.jsondata->>'smaxlod')::character varying(100) as smaxlod,(rp.jsondata->>'smaxloq')::character varying(100) as smaxloq,"
				+ "(rp.jsondata->>'sdisregard')::character varying(100) as sdisregard,"
				+ "(rp.jsondata->>'sresultvalue')::character varying(255) as sresultvalue,(rp.jsondata->>'sresult')::character varying(255) as sresult,"
				+ "(rp.jsondata->>'sfinal')::character varying(255) as sfinal,(rp.jsondata->>'nfilesize')::character varying(100) as nfilesize,"
				+ "rp.ngradecode,tgf.ntestgrouptestformulacode, "
				+ "rp.ntestgrouptestparametercode,tgtp.nsorter,tgtp.ntestgrouptestcode,rp.nresultmandatory,"
				+ "ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "' as stransdisplaystatus,u.sunitname,u.nunitcode, "
				+ "(rp.jsondata->>'nresultaccuracycode')::integer as nresultaccuracycode,(rp.jsondata->>'sresultaccuracyname')::character varying(10) as sresultaccuracyname"
				+ " from unit u,approvalparameter rp,registrationsamplearno rsa,registrationarno ra, registration r,registrationtest rt,testgrouptestparameter tgtp "
				+ "left outer join testgrouptestnumericparameter tgnp on tgtp.ntestgrouptestparametercode = tgnp.ntestgrouptestparametercode and tgnp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "left outer join testgrouptestformula tgf on tgf.ntestgrouptestcode=tgtp.ntestgrouptestcode and tgf.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgf.ntransactionstatus  = "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " "
				+ "and tgtp.ntestgrouptestparametercode=tgf.ntestgrouptestparametercode, "
				+ "parametertype pt,grade g,transactionstatus ts  "
				+ "where pt.nparametertypecode = tgtp.nparametertypecode and ts.ntranscode = rp.nresultmandatory "
				+ " and r.npreregno = rt.npreregno and tgtp.ntestgrouptestparametercode = rp.ntestgrouptestparametercode and rsa.ntransactionsamplecode = rt.ntransactionsamplecode and ra.npreregno = rt.npreregno  "
				+ "and rp.ntransactiontestcode = rt.ntransactiontestcode "
				+ "and rp.ngradecode = g.ngradecode and rp.ntransactionstatus not in("
				+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ") "
				+ "and rp.ntransactionresultcode = " + ntransactionresultcode + " "
				+ "  and ra.nsitecode = "+ userInfo.getNtranssitecode() + " and rsa.nsitecode = ra.nsitecode and rt.nsitecode = rsa.nsitecode "
				+ " and r.nsitecode = rsa.nsitecode and rp.nsitecode = rt.nsitecode and rt.nstatus = rp.nstatus "
				+ " and pt.nstatus = g.nstatus and g.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rp.nunitcode=u.nunitcode and u.nstatus =  "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by rp.ntransactionresultcode desc " + ")a ;";

		final String getPreDefinedParams = " select tgpp.ntestgrouptestpredefcode, tgpp.ntestgrouptestparametercode, tgpp.ngradecode,  "
				+ " tgpp.spredefinedname||' ('||tgpp.spredefinedsynonym||')' as sresultpredefinedname,tgpp.spredefinedname,"
				+ " tgpp.spredefinedsynonym, " + " tgpp.spredefinedcomments, tgpp.salertmessage, "
				+ " 4 as nneedresultentryalert, 4 as nneedsubcodedresult,"
				// + " tgpp.nneedresultentryalert, tgpp.nneedsubcodedresult," commentted by
				// Dhanushya RI
				+ " tgpp.ndefaultstatus, tgpp.nsitecode, "
				+ " tgpp.dmodifieddate, tgpp.nstatus,rp.ntransactiontestcode,rp.ntestgrouptestparametercode,rp.ntransactionresultcode "
				+ " from approvalparameter rp,testgrouptestpredefparameter tgpp"
				+ " where rp.ntestgrouptestparametercode = tgpp.ntestgrouptestparametercode and rp.ntransactionresultcode = "
				+ ntransactionresultcode + " and rp.nparametertypecode = "
				+ Enumeration.ParameterType.PREDEFINED.getparametertype() + " and rp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgpp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and tgpp.spredefinedname <> '' "
				+ " and rp.nsitecode="+userInfo.getNtranssitecode()+" "
				+ " and tgpp.nsitecode="+userInfo.getNmastersitecode()+" ";
		
		final String getGrades = "select g.ngradecode," + "case g.sgradename when 'NA' then '' else"
				+ " coalesce(g.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " g.jsondata->'sdisplayname'->>'en-US') end sgradename ,"
				+ " cm.ncolorcode,cm.scolorname,cm.scolorhexcode from grade g,colormaster cm "
				+ " where g.ncolorcode = cm.ncolorcode and g.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " union all "
				+ " select 0 ngradecode,'' sgradename, 0 ncolorcode, '' scolorname,'' scolorhexcode;";


		returnMap.put("ReleaseParameter", jdbcTemplate.queryForList(getParams));

		List<TestGroupTestPredefinedParameter> lst = jdbcTemplate.query(getPreDefinedParams,
				new TestGroupTestPredefinedParameter());

		List<Grade> lstGrade = objMapper.convertValue(
				commonFunction.getMultilingualMessageList(jdbcTemplate.query(getGrades, new Grade()),
						Arrays.asList("sgradename"), userInfo.getSlanguagefilename()),
				new TypeReference<List<Grade>>() {
				});

		returnMap.put("PredefinedValues", lst.stream().collect(Collectors
				.groupingBy(TestGroupTestPredefinedParameter::getNtransactionresultcode, Collectors.toList())));

		returnMap.put("GradeValues",
				lstGrade.stream().collect(Collectors.groupingBy(Grade::getNgradecode, Collectors.toList())));

		String strQuery = "select * from resultaccuracy where  nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and nsitecode ="
				+ userInfo.getNmastersitecode() + " ";
		returnMap.put("ResultAccuracy", jdbcTemplate.queryForList(strQuery));

		String strUnitQuery = "select * from unit where nunitcode >0 and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and nsitecode ="
				+ userInfo.getNmastersitecode() + " ";
		returnMap.put("Unit", jdbcTemplate.queryForList(strUnitQuery));

		// ALPD-4387
		String strFormFieldQuery = " select rf.jsondata from registrationparameter rp,registration r,resultentryformfield rf "
				+ "  where rp.npreregno=r.npreregno " + " and rp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and r.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rf.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and rf.nsitecode ="
				+ userInfo.getNmastersitecode() + "  and rp.nsitecode =" + userInfo.getNtranssitecode()
				+ "  and r.nsitecode =" + userInfo.getNtranssitecode() + "  and rf.nregtypecode=r.nregtypecode "
				+ "  and rf.nregsubtypecode=r.nregsubtypecode " + "  and rp.ntransactionresultcode="
				+ ntransactionresultcode;
		returnMap.put("FormFields", jdbcTemplate.queryForList(strFormFieldQuery));
		return new ResponseEntity<>(returnMap, HttpStatus.OK);

	}
	// Ate234 START Janakumar ALPD-5123 Test Approval -> To get previously saved
	// filter details when click the filter name

	@Override
	public ResponseEntity<Object> createFilterName(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();

		Map<String, Object> objMap = new HashMap<>();

		final List<FilterName> lstFilterByName = projectDAOSupport.getFilterListByName(inputMap.get("sfiltername").toString(), userInfo);
		if (lstFilterByName.isEmpty()) {
			final String strValidationQuery = "select json_agg(jsondata || jsontempdata) as jsondata from filtername where nformcode="
					+ userInfo.getNformcode() + " and nusercode=" + userInfo.getNusercode() + " "
					+ " and nuserrolecode=" + userInfo.getNuserrole() + " and nsitecode=" + userInfo.getNtranssitecode()
					+ " " + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and (jsondata->'nsampletypecode')::int=" + inputMap.get("nsampletypecode") + " "
					+ " and (jsondata->'nregtypecode')::int=" + inputMap.get("nregtypecode") + " "
					+ " and (jsondata->'nregsubtypecode')::int=" + inputMap.get("nregsubtypecode") + " "
					+ " and (jsontempdata->'ntranscode')::int in (" + inputMap.get("ntranscode") + ") "
					+ " and (jsontempdata->'napproveconfversioncode')::int=" + inputMap.get("napproveconfversioncode")
					+ " and (jsontempdata->'ndesigntemplatemappingcode')::int="
					+ inputMap.get("ndesigntemplatemappingcode") + " " + " and (jsontempdata->>'DbFromDate')='"
					+ inputMap.get("dfrom") + "' " + " and (jsontempdata->>'DbToDate')='" + inputMap.get("dto") + "' ;";

			final String strValidationFilter = jdbcTemplate.queryForObject(strValidationQuery, String.class);

			final List<Map<String, Object>> lstValidationFilter = strValidationFilter != null
					? objMapper.readValue(strValidationFilter, new TypeReference<List<Map<String, Object>>>() {
					})
					: new ArrayList<Map<String, Object>>();
			if (lstValidationFilter.isEmpty()) {

				projectDAOSupport.createFilterData(inputMap, userInfo);
				final List<FilterName> lstFilterName = projectDAOSupport.getFavoriteFilterName(userInfo);
				objMap.put("FilterName", lstFilterName);
				return new ResponseEntity<Object>(objMap, HttpStatus.OK);
			} else {
				return new ResponseEntity<Object>(commonFunction.getMultilingualMessage("IDS_FILTERALREADYPRESENT",
						userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}
		} else {

			return new ResponseEntity<Object>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);

		}
	}

	@Override
	public ResponseEntity<? extends Object> getTestApprovalFilterDetails(Map<String, Object> inputMap,final UserInfo userInfo)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();

		Map<String, Object> returnMap = new HashMap<>();

		final String strQuery1 = "select json_agg(jsondata || jsontempdata) as jsondata from filtername where nformcode="
				+ userInfo.getNformcode() + " and nusercode=" + userInfo.getNusercode() + " " + " and nuserrolecode="
				+ userInfo.getNuserrole() + " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nfilternamecode="
				+ inputMap.get("nfilternamecode");

		final String strFilter = jdbcTemplate.queryForObject(strQuery1, String.class);

		final List<Map<String, Object>> lstfilterDetail = strFilter != null
				? objMapper.readValue(strFilter, new TypeReference<List<Map<String, Object>>>() {
				})
				: new ArrayList<Map<String, Object>>();
		if (!lstfilterDetail.isEmpty()) {

			final String strValidationQuery = "select json_agg(jsondata || jsontempdata) as jsondata from filtername where nformcode="
					+ userInfo.getNformcode() + " and nusercode=" + userInfo.getNusercode() + " "
					+ " and nuserrolecode=" + userInfo.getNuserrole() + " and nsitecode=" + userInfo.getNtranssitecode()
					+ " " + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and (jsondata->'nsampletypecode')::int=" + inputMap.get("sampletypecode") + " "
					+ " and (jsondata->'nregtypecode')::int=" + inputMap.get("regtypecode") + " "
					+ " and (jsondata->'nregsubtypecode')::int=" + inputMap.get("regsubtypecode") + " "
					+ " and (jsontempdata->'ntranscode')::int=" + inputMap.get("ntranscode") + " "
					+ " and (jsontempdata->'napproveconfversioncode')::int=" + inputMap.get("napprovalversioncode")
					+ " " + " and (jsontempdata->'ndesigntemplatemappingcode')::int="
					+ inputMap.get("ndesigntemplatemappingcode") + " " + " and (jsontempdata->>'DbFromDate')='"
					+ inputMap.get("FromDate") + "' " + " and (jsontempdata->>'DbToDate')='" + inputMap.get("ToDate")
					+ "' and nfilternamecode=" + inputMap.get("nfilternamecode") + " ; ";

			final String strValidationFilter = jdbcTemplate.queryForObject(strValidationQuery, String.class);

			final List<Map<String, Object>> lstValidationFilter = strValidationFilter != null
					? objMapper.readValue(strValidationFilter, new TypeReference<List<Map<String, Object>>>() {
					})
					: new ArrayList<Map<String, Object>>();

			if (lstValidationFilter.isEmpty()) {

				Instant instantFromDate = dateUtilityFunction
						.convertStringDateToUTC(lstfilterDetail.get(0).get("fromdate").toString(), userInfo, true);
				Instant instantToDate = dateUtilityFunction
						.convertStringDateToUTC(lstfilterDetail.get(0).get("todate").toString(), userInfo, true);

				final String fromDate = dateUtilityFunction.instantDateToString(instantFromDate);
				final String toDate = dateUtilityFunction.instantDateToString(instantToDate);

				returnMap.put("fromdate", lstfilterDetail.get(0).get("DbFromDate").toString());
				returnMap.put("todate", lstfilterDetail.get(0).get("DbToDate").toString());

				final DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
				final LocalDateTime ldt = LocalDateTime.parse(fromDate, formatter1);
				final LocalDateTime ldt1 = LocalDateTime.parse(toDate, formatter1);

				final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());
				String formattedFromString = "";
				String formattedToString = "";

				if (userInfo.getIsutcenabled() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					final ZonedDateTime zonedDateFromTime = ZonedDateTime.of(ldt, ZoneId.of(userInfo.getStimezoneid()));
					formattedFromString = zonedDateFromTime.format(formatter);
					final ZonedDateTime zonedDateToTime = ZonedDateTime.of(ldt1, ZoneId.of(userInfo.getStimezoneid()));
					formattedToString = zonedDateToTime.format(formatter);

				} else {
					formattedFromString = formatter.format(ldt);
					formattedToString = formatter.format(ldt1);

				}

				returnMap.put("fromDate", formattedFromString);
				returnMap.put("toDate", formattedToString);
				returnMap.put("realFromDate", formattedFromString);
				returnMap.put("realToDate", formattedToString);

				final String sampleTypeQuery = "select st.nsampletypecode,coalesce(st.jsondata->'sampletypename'->>'"
						+ userInfo.getSlanguagetypecode()
						+ "',st.jsondata->'sampletypename'->>'en-US') ssampletypename,st.nsorter "
						+ "from sampletype st,registrationtype rt,registrationsubtype rst,approvalconfig ac,approvalconfigversion acv "
						+ "where st.nsampletypecode > 0 and st.nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ "  and st.napprovalconfigview = " + Enumeration.TransactionStatus.YES.gettransactionstatus()
						+ "  and st.nsampletypecode=rt.nsampletypecode and rt.nstatus= "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and rt.nregtypecode=rst.nregtypecode and rst.nstatus= "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and rst.nregsubtypecode=ac.nregsubtypecode and ac.nstatus= "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and acv.napprovalconfigcode=ac.napprovalconfigcode and acv.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "and acv.ntransactionstatus= "+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " "
						+ " and rt.nsitecode="+userInfo.getNmastersitecode()+" "
						+ " and rst.nsitecode="+userInfo.getNmastersitecode()+" "
						+ " and ac.nsitecode="+userInfo.getNmastersitecode()+" "
						+ " and acv.nsitecode="+userInfo.getNmastersitecode()+" "
						+ " group by st.nsampletypecode,st.nsorter order by st.nsorter; ";

				final List<SampleType> lstSampleType = jdbcTemplate.query(sampleTypeQuery, new SampleType());

				if (!lstSampleType.isEmpty()) {

					final SampleType filterSampleType = !lstfilterDetail.isEmpty()
							? objMapper.convertValue(lstfilterDetail.get(0).get("sampleTypeValue"), SampleType.class)
							: lstSampleType.get(0);

					inputMap.put("filterDetailValue", lstfilterDetail);
					inputMap.put("dfrom", fromDate);
					inputMap.put("dto", toDate);
					inputMap.put("FromDate", fromDate);
					inputMap.put("ToDate", toDate);
					returnMap.put("dfrom", fromDate);
					returnMap.put("dto", toDate);
					returnMap.put("SampleType", lstSampleType);
					returnMap.put("realSampleTypeList", lstSampleType);
					inputMap.put("nsampletypecode", filterSampleType.getNsampletypecode());
					inputMap.put("npreregno", "0");
					inputMap.put("checkBoxOperation", 3);
					inputMap.put("ntype", 2);
					inputMap.put("activeSubSampleTab", "IDS_SUBSAMPLEATTACHMENTS");
					returnMap.put("SampleTypeValue", filterSampleType);
					returnMap.put("realSampleTypeValue", filterSampleType);

					returnMap.putAll((Map<String, Object>) getRegistrationType(inputMap, userInfo).getBody());

					returnMap.put("realRegTypeValue", returnMap.get("RegTypeValue"));
					returnMap.put("realRegSubTypeValue", returnMap.get("RegSubTypeValue"));
					returnMap.put("realApprovalVersionValue", returnMap.get("ApprovalVersionValue"));
					returnMap.put("realFilterStatusValue", returnMap.get("FilterStatusValue"));
					returnMap.put("realUserSectionValue", returnMap.get("UserSectionValue"));
					returnMap.put("realTestValue", returnMap.get("TestValue"));
					returnMap.put("realDesignTemplateMappingValue", returnMap.get("DesignTemplateMappingValue"));
					returnMap.put("activeSubSampleTab", "IDS_SUBSAMPLEATTACHMENTS");
					returnMap.put("activeTestTab", "");

				}

				return new ResponseEntity<>(returnMap, HttpStatus.OK);

			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDFILTERALREADYLOADED",
						userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}

		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDFILTERISNOTPRESENT",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}

	}
	// Ate234 END Janakumar ALPD-5123 Test Approval -> To get previously saved
	// filter details when click the filter name

	// start
	// ALPD-4941-->Added by Vignesh R(11-02-2025)--Scheduler Configuration screen
	// design
	private void updateInstrumentScheduler(Map<String, Object> inputmap, final UserInfo userInfo) throws Exception {

		final String getDecisionStatus = "select ad.* from approvalroledecisiondetail ad,approvalconfigrole ar  "
				+ " where ar.nuserrolecode = " + userInfo.getNuserrole() + " and ar.napproveconfversioncode="
				+ (int) inputmap.get("napproveconfversioncode")
				+ " and ad.napprovalconfigrolecode=ar.napprovalconfigrolecode  and ad.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ar.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ad.nsitecode="+userInfo.getNmastersitecode()+" "
				+ " and ar.nsitecode="+userInfo.getNmastersitecode()+" ";

		final List<ApprovalRoleDecisionDetail> decisionStatusList = (List<ApprovalRoleDecisionDetail>) jdbcTemplate
				.query(getDecisionStatus, new ApprovalRoleDecisionDetail());
		String sdescionQuery = "";
		String selectQueryDescion = "";
		boolean descionstatus = false;
		if (decisionStatusList.size() > 0) {
			descionstatus = true;

			sdescionQuery = " join  registrationdecisionhistory  rdh" + "	 on nregdecisionhistorycode= any("
					+ "	 select max(nregdecisionhistorycode) nregdecisionhistorycode from registrationdecisionhistory"
					+ "	 where npreregno in (" + inputmap.get("newPreregno").toString() + ")" + "	and   nsitecode="
					+ userInfo.getNtranssitecode() + ""
					+ " group by npreregno) and rdh.npreregno=r.npreregno and st.npreregno=r.npreregno";

			selectQueryDescion = " ,ndecisionstatus ";

		}
		String strCompleteSampleCheck = "select r.npreregno,r.ninstrumentcode,i.nnextcalibration,i.sinstrumentid, "
				+ " i.nnextcalibrationperiod" + selectQueryDescion + " from instrument i "
				+ " join registration r ON i.ninstrumentcode=r.ninstrumentcode and i.ninstrumentcatcode=r.ninstrumentcatcode "
				+ " JOIN schedulersampledetail ssd ON i.ninstrumentcatcode=r.ninstrumentcatcode  "
				+ "  JOIN transactionstatus ts ON ts.ntranscode = i.ninstrumentstatus "
				+ "  JOIN schedulertransaction st ON st.nschedulertransactioncode = r.nschedulertransactioncode "
				+ " AND ssd.nschedulersamplecode = st.nschedulersamplecode and r.npreregno=st.npreregno "
				+ sdescionQuery + " where r.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and i.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and st.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and ts.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and ssd.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and r.npreregno in(" + inputmap.get("newPreregno").toString() + " )" + " and r.nsitecode="
				+ userInfo.getNtranssitecode() + "  and ssd.nsitecode=" + userInfo.getNmastersitecode() + "  "
				+ " and st.nsitecode=" + userInfo.getNtranssitecode() + "  ";

		List<Registration> listOfcalibration = jdbcTemplate.query(strCompleteSampleCheck, new Registration());

		if (!listOfcalibration.isEmpty()) {

			String sSchedulerTransQuery = "";
			String updateInstrumentCal = "";

			String sQuery = " lock  table schedulertransaction "
					+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			int nsequenceNoSchedulerTransaction = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnoscheduler where stablename ='schedulertransaction' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"", Integer.class);

			for (Registration newlistOfcalibration : listOfcalibration) {

				final int ntransactionstatus = getSchedulerTransactionStatus(descionstatus,
						newlistOfcalibration.getNdecisionstatus(), (short) inputmap.get("nsampletypecode"));

				if (ntransactionstatus != Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus()) {

					Date duedate = new Date();

					Calendar calendar = Calendar.getInstance();

					calendar.setTime(duedate);

					if (newlistOfcalibration.getNnextcalibrationperiod() == Enumeration.Period.Days.getPeriod()) {

						calendar.add(Calendar.DAY_OF_MONTH, (int) newlistOfcalibration.getNnextcalibration());

						duedate = calendar.getTime();

					} else if (newlistOfcalibration.getNnextcalibrationperiod() == Enumeration.Period.Weeks
							.getPeriod()) {

						calendar.add(Calendar.WEEK_OF_MONTH, (int) newlistOfcalibration.getNnextcalibration());

						duedate = calendar.getTime();

					} else if (newlistOfcalibration.getNnextcalibrationperiod() == Enumeration.Period.Month
							.getPeriod()) {

						calendar.add(Calendar.MONTH, (int) newlistOfcalibration.getNnextcalibration());

						duedate = calendar.getTime();
					}

					final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

					final String soccurencedate = formatter.format(duedate);
					updateInstrumentCal = updateInstrumentCal + "UPDATE instrumentcalibration mi "
							+ "	SET ncalibrationstatus=" + ntransactionstatus + "," + " dclosedate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',ntzclosedate="
							+ userInfo.getNtimezonecode() + ",noffsetdclosedate="
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
							+ " dduedate='" + soccurencedate + "',ntzduedate=" + userInfo.getNtimezonecode()
							+ ",noffsetdduedate="
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
							+ " dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "' "
							+ "	WHERE ninstrumentcalibrationcode =(select ninstrumentcalibrationcode from instrumentcalibration where "
							+ " npreregno=" + newlistOfcalibration.getNpreregno() + " and ninstrumentcode="
							+ newlistOfcalibration.getNinstrumentcode() + " " + " and  nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

					++nsequenceNoSchedulerTransaction;

					sSchedulerTransQuery = sSchedulerTransQuery
							+ "INSERT INTO schedulertransaction(nschedulertransactioncode, nschedulecode,nsampletypecode,jsondata,jsonuidata, nschedulersamplecode, npreregno, dscheduleoccurrencedate, dtransactiondate, noffsetdtransactiondate, ntransdatetimezonecode, ntransactionstatus, nsitecode, nstatus)"
							+ " select " + nsequenceNoSchedulerTransaction
							+ ",nschedulecode,nsampletypecode,jsondata,jsonuidata,nschedulersamplecode,"
							+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ",  " + "'" + soccurencedate
							+ "','" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + ""
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
							+ userInfo.getNtimezonecode() + ", "
							+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", " + ""
							+ userInfo.getNtranssitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ " from schedulertransaction std where  std.npreregno= "
							+ newlistOfcalibration.getNpreregno() + " and std.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and std.nsitecode="
							+ userInfo.getNtranssitecode() + ";";

				}

				if (ntransactionstatus == Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus()) {

					updateInstrumentCal = updateInstrumentCal + "UPDATE instrument SET ninstrumentstatus="
							+ ntransactionstatus + "" + "	WHERE ninstrumentcode = "
							+ newlistOfcalibration.getNinstrumentcode() + ";";

				}
			}

			updateInstrumentCal = updateInstrumentCal + sSchedulerTransQuery;

			if (!sSchedulerTransQuery.isEmpty()) {
				updateInstrumentCal = updateInstrumentCal + " update seqnoscheduler set nsequenceno="
						+ nsequenceNoSchedulerTransaction + " where stablename='schedulertransaction' "
								+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";

			}

			jdbcTemplate.execute(updateInstrumentCal);

		}
	}

	// end
	// ALPD-5332-->start
	// ALPD-5332-->Added by Vignesh R(06-01-2025)--Material Scheduler
	private void updateMaterialScheduler(Map<String, Object> inputmap, final UserInfo userInfo) throws Exception {

		final String getDecisionStatus = "select ad.* from approvalroledecisiondetail ad,approvalconfigrole ar  "
				+ " where ar.nuserrolecode = " + userInfo.getNuserrole() + " and ar.napproveconfversioncode="
				+ (int) inputmap.get("napproveconfversioncode")
				+ " and ad.napprovalconfigrolecode=ar.napprovalconfigrolecode " + " and ad.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ar.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		List<ApprovalRoleDecisionDetail> decisionStatusList = (List<ApprovalRoleDecisionDetail>) jdbcTemplate
				.query(getDecisionStatus, new ApprovalRoleDecisionDetail());
		String sdescionQuery = "";
		String selectQueryDescion = "";
		boolean descionstatus = false;
		if (decisionStatusList.size() > 0) {
			descionstatus = true;

			sdescionQuery = " join  registrationdecisionhistory  rdh on nregdecisionhistorycode= any("
					+ "	 select max(nregdecisionhistorycode) nregdecisionhistorycode from registrationdecisionhistory"
					+ "	 where npreregno in (" + inputmap.get("newPreregno").toString() + ")" + "	and   nsitecode="
					+ userInfo.getNtranssitecode() + ""
					+ " group by npreregno) and rdh.npreregno=r.npreregno and st.npreregno=r.npreregno";

			selectQueryDescion = " ,ndecisionstatus ";

		}

		final String strMaterialQuery = "SELECT m.nmaterialcode, "
				+ " NULLIF(m.jsondata->>'Next Validation Need','')::integer AS isnextvalidation, "
				+ " mi.jsondata->>'Inventory ID' AS sinventoryid, mi.nmaterialinventorycode,r.npreregno,"
				+ " NULLIF(m.jsondata->>'Next Validation','')::integer as nnextvalidation,NULLIF(m.jsondata->'Next Validation Period'->>'value','')::integer as nnextvalidationperiod "
				+ selectQueryDescion + "" + " FROM material m "
				+ " JOIN registration r ON r.nmaterialcode = m.nmaterialcode "
				+ " JOIN schedulersampledetail ssd ON ssd.nmaterialcode = m.nmaterialcode "
				+ "  JOIN transactionstatus ts1 ON ts1.ntranscode = m.ntransactionstatus "
				+ "  JOIN schedulertransaction st ON st.nschedulertransactioncode = r.nschedulertransactioncode "
				+ " AND ssd.nschedulersamplecode = st.nschedulersamplecode and r.npreregno=st.npreregno "
				+ " JOIN materialinventory mi ON m.nmaterialcode = mi.nmaterialcode and r.nmaterialinventorycode=mi.nmaterialinventorycode "
				+ "  JOIN transactionstatus ts ON ts.ntranscode = mi.ntransactionstatus " + " " + sdescionQuery + ""
				+ " WHERE r.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " AND mi.ntransactionstatus = " + Enumeration.TransactionStatus.QUARENTINE.gettransactionstatus()
				+ "  AND m.ntransactionstatus = " + Enumeration.TransactionStatus.YES.gettransactionstatus() + " "
				+ " AND mi.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " AND m.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " AND ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " AND ts1.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " AND r.npreregno IN (" + inputmap.get("newPreregno").toString() + ") " + " AND r.nsitecode = "
				+ userInfo.getNtranssitecode() + " " + " AND st.nsitecode = " + userInfo.getNtranssitecode() + " "
				+ " AND m.nsitecode = " + userInfo.getNmastersitecode() + " " + " AND mi.nsitecode = "
				+ userInfo.getNtranssitecode() + " " + "  AND ssd.nactivestatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND ssd.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " AND st.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";

		List<Registration> listOfMaterial = jdbcTemplate.query(strMaterialQuery, new Registration());

		if (!listOfMaterial.isEmpty()) {

			String sQuery = " lock  table materialinventoryhistory "
					+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
			jdbcTemplate.execute(sQuery);
			int nsequenceNomaterialinventoryhistory = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnomaterialmanagement where stablename ='materialinventoryhistory' "
					+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"",
					Integer.class);
			StringBuilder sinventorybuilder = new StringBuilder();

			String sSchedulerTransQuery = "";
			String updateMaterialInv = "";
			sQuery = " lock  table schedulertransaction " + Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
			jdbcTemplate.execute(sQuery);
			int nsequenceNoSchedulerTransaction = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnoscheduler where stablename ='schedulertransaction'"
					+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"", Integer.class);

			for (Registration newlistMaterial : listOfMaterial) {

				if (newlistMaterial.getIsnextvalidation() == Enumeration.TransactionStatus.NO.gettransactionstatus()) {

					Date duedate = null;

					int ntransactionstatus = getSchedulerTransactionStatus(descionstatus,
							newlistMaterial.getNdecisionstatus(), (short) inputmap.get("nsampletypecode"));

					if (ntransactionstatus == Enumeration.TransactionStatus.RELEASED.gettransactionstatus()
							|| ntransactionstatus == Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {
						++nsequenceNomaterialinventoryhistory;

						updateMaterialInv = updateMaterialInv + "UPDATE materialinventory mi"
								+ "	SET  ntransactionstatus=" + ntransactionstatus
								+ ",jsondata = jsonb_set(jsonb_set(mi.jsondata, '{ntranscode}', '" + ntransactionstatus
								+ "'::jsonb),'{sdisplaystatus}',to_jsonb(coalesce(ts.jsondata->'stransdisplaystatus'->>'"
								+ userInfo.getSlanguagetypecode() + "',"
								+ " ts.jsondata->'stransdisplaystatus'->>'en-US'))),"
								+ " jsonuidata = jsonb_set(jsonb_set(mi.jsonuidata, '{ntranscode}', '"
								+ ntransactionstatus
								+ "'::jsonb),'{sdisplaystatus}',to_jsonb(coalesce(ts.jsondata->'stransdisplaystatus'->>'"
								+ userInfo.getSlanguagetypecode() + "',"
								+ " ts.jsondata->'stransdisplaystatus'->>'en-US'))) " + " FROM transactionstatus ts "
								+ "	WHERE nmaterialinventorycode =" + newlistMaterial.getNmaterialinventorycode() + " "
								+ " and mi.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								+ " and ts.ntranscode=" + ntransactionstatus + " " + " and ts.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

						sinventorybuilder.append("(" + nsequenceNomaterialinventoryhistory + ", "
								+ newlistMaterial.getNmaterialinventorycode() + ", " + ntransactionstatus + ","
								+ duedate + ","
								+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
								+ userInfo.getNtimezonecode() + ",  '"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode()
								+ ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
								+ " " + userInfo.getNtranssitecode() + ", "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),");

					}

				} else if (newlistMaterial.getIsnextvalidation() == Enumeration.TransactionStatus.YES
						.gettransactionstatus()) {

					Date duedate = new Date();

					Calendar calendar = Calendar.getInstance();

					calendar.setTime(duedate);

					if (newlistMaterial.getNnextvalidationperiod() == Enumeration.Period.Days.getPeriod()) {

						calendar.add(Calendar.DAY_OF_MONTH, (int) newlistMaterial.getNnextvalidation());

						duedate = calendar.getTime();

					} else if (newlistMaterial.getNnextvalidationperiod() == Enumeration.Period.Weeks.getPeriod()) {

						calendar.add(Calendar.WEEK_OF_MONTH, (int) newlistMaterial.getNnextvalidation());

						duedate = calendar.getTime();

					} else if (newlistMaterial.getNnextvalidationperiod() == Enumeration.Period.Month.getPeriod()) {

						calendar.add(Calendar.MONTH, (int) newlistMaterial.getNnextvalidation());

						duedate = calendar.getTime();
					}

					final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

					String soccurencedate = formatter.format(duedate);

					final int ntransactionstatus = getSchedulerTransactionStatus(descionstatus,
							newlistMaterial.getNdecisionstatus(), (short) inputmap.get("nsampletypecode"));

					if (ntransactionstatus == Enumeration.TransactionStatus.RELEASED.gettransactionstatus()
							|| ntransactionstatus == Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {
						updateMaterialInv = updateMaterialInv + "UPDATE materialinventory mi"
								+ "	SET  ntransactionstatus=" + ntransactionstatus
								+ ",jsondata = jsonb_set(jsonb_set(mi.jsondata, '{ntranscode}', '" + ntransactionstatus
								+ "'::jsonb),'{sdisplaystatus}',to_jsonb(coalesce(ts.jsondata->'stransdisplaystatus'->>'"
								+ userInfo.getSlanguagetypecode() + "',"
								+ " ts.jsondata->'stransdisplaystatus'->>'en-US'))),"
								+ " jsonuidata = jsonb_set(jsonb_set(mi.jsonuidata, '{ntranscode}', '"
								+ ntransactionstatus
								+ "'::jsonb),'{sdisplaystatus}',to_jsonb(coalesce(ts.jsondata->'stransdisplaystatus'->>'"
								+ userInfo.getSlanguagetypecode() + "',"
								+ " ts.jsondata->'stransdisplaystatus'->>'en-US'))) " + " FROM transactionstatus ts "
								+ "	WHERE nmaterialinventorycode =" + newlistMaterial.getNmaterialinventorycode() + " "
								+ " and mi.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								+ " and ts.ntranscode=" + ntransactionstatus + " " + " and ts.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

						++nsequenceNomaterialinventoryhistory;

						sinventorybuilder.append("(" + nsequenceNomaterialinventoryhistory + ", "
								+ newlistMaterial.getNmaterialinventorycode() + ", " + ntransactionstatus + ","
								+ (ntransactionstatus == Enumeration.TransactionStatus.RETIRED.gettransactionstatus()
										? null
										: "'" + soccurencedate + "'")
								+ "," + " " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
								+ ", " + userInfo.getNtimezonecode() + ",  '"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode()
								+ ", " + " " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
								+ ", " + " " + userInfo.getNtranssitecode() + ", " + " "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),");

					}

					if (ntransactionstatus != Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {
						++nsequenceNoSchedulerTransaction;

						sSchedulerTransQuery = sSchedulerTransQuery
								+ "INSERT INTO schedulertransaction(nschedulertransactioncode, nschedulecode,nsampletypecode,jsondata,jsonuidata, nschedulersamplecode, npreregno, dscheduleoccurrencedate, dtransactiondate, noffsetdtransactiondate, ntransdatetimezonecode, ntransactionstatus, nsitecode, nstatus)"
								+ " select " + nsequenceNoSchedulerTransaction
								+ ",nschedulecode,nsampletypecode,jsondata,jsonuidata,nschedulersamplecode,"
								+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ",  " + "'" + soccurencedate
								+ "','" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + ""
								+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
								+ userInfo.getNtimezonecode() + ", "
								+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", " + ""
								+ userInfo.getNtranssitecode() + ", "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								+ " from schedulertransaction std where  std.npreregno= "
								+ newlistMaterial.getNpreregno() + " and std.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and std.nsitecode="
								+ userInfo.getNtranssitecode() + ";";

					}

				}

			}
//				ALPD-5473 Added by Abdul
			String updateQuery = "";
			if (sinventorybuilder.length() > 0) {
				updateQuery = "INSERT INTO materialinventoryhistory( nmaterialinventoryhistorycode, nmaterialinventorycode, ntransactionstatus, dnextvalidationdate, "
						+ "noffsetdnextvalidationdate, ntznextvalidationdate, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nsitecode, nstatus)"
						+ " VALUES" + sinventorybuilder.substring(0, sinventorybuilder.length() - 1).toString() + ";";
			}
			updateQuery = updateMaterialInv + sSchedulerTransQuery + updateQuery;

			if (!sSchedulerTransQuery.isEmpty()) {
				updateQuery = updateQuery + " update seqnoscheduler set nsequenceno=" + nsequenceNoSchedulerTransaction
						+ " where stablename='schedulertransaction' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";

			}
			if (sinventorybuilder.length() > 0) {
				updateQuery = updateQuery + "update seqnomaterialmanagement set nsequenceno="
						+ nsequenceNomaterialinventoryhistory + " where stablename='materialinventoryhistory' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";

			}

			jdbcTemplate.execute(updateQuery);

		}

		// }
		// ALPD-5332-->end
	}

	// ALPD-5332-->start
	// ALPD-5332-->Added by Vignesh R(06-01-2025)--Material Scheduler
	private int getSchedulerTransactionStatus(final boolean bdescionstatus, final int ndescionstatus,
			final short nsampletypecode) {

		int ntransactionstatus = -1;

		if (nsampletypecode == Enumeration.SampleType.MATERIAL.getType()) {

			if (bdescionstatus) {

				if (ndescionstatus == Enumeration.TransactionStatus.PASS.gettransactionstatus()) {
					ntransactionstatus = Enumeration.TransactionStatus.RELEASED.gettransactionstatus();
				}

				else if (ndescionstatus == Enumeration.TransactionStatus.FAIL.gettransactionstatus()) {
					ntransactionstatus = Enumeration.TransactionStatus.QUARENTINE.gettransactionstatus();

				}

				else if (ndescionstatus == Enumeration.TransactionStatus.WITHDRAWN.gettransactionstatus()) {
					ntransactionstatus = Enumeration.TransactionStatus.RETIRED.gettransactionstatus();

				}

			} else {
				ntransactionstatus = Enumeration.TransactionStatus.RELEASED.gettransactionstatus();

			}

		} else if (nsampletypecode == Enumeration.SampleType.INSTRUMENT.getType()) {

			if (bdescionstatus) {

				if (ndescionstatus == Enumeration.TransactionStatus.PASS.gettransactionstatus()) {
					ntransactionstatus = Enumeration.TransactionStatus.CALIBIRATION.gettransactionstatus();
				}

				else if (ndescionstatus == Enumeration.TransactionStatus.FAIL.gettransactionstatus()) {
					ntransactionstatus = Enumeration.TransactionStatus.UNDERCALIBIRATION.gettransactionstatus();

				}

				else if (ndescionstatus == Enumeration.TransactionStatus.WITHDRAWN.gettransactionstatus()) {
					ntransactionstatus = Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus();

				}

			} else {
				ntransactionstatus = Enumeration.TransactionStatus.CALIBIRATION.gettransactionstatus();

			}

		}

		return ntransactionstatus;
	}
//ALPD-5332-->end
}
