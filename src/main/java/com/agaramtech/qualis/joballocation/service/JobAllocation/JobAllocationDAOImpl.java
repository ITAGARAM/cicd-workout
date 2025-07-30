package com.agaramtech.qualis.joballocation.service.JobAllocation;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.attachmentscomments.service.attachments.AttachmentDAO;
import com.agaramtech.qualis.attachmentscomments.service.comments.CommentDAO;
import com.agaramtech.qualis.basemaster.model.Period;
import com.agaramtech.qualis.basemaster.model.TransactionStatus;
import com.agaramtech.qualis.basemaster.service.timezone.TimeZoneDAO;
import com.agaramtech.qualis.compentencemanagement.model.Technique;
import com.agaramtech.qualis.compentencemanagement.model.TrainingCertification;
import com.agaramtech.qualis.configuration.model.ApprovalConfigAutoapproval;
import com.agaramtech.qualis.configuration.model.DesignTemplateMapping;
import com.agaramtech.qualis.configuration.model.FilterName;
import com.agaramtech.qualis.configuration.model.SampleType;
import com.agaramtech.qualis.credential.model.Users;
import com.agaramtech.qualis.dynamicpreregdesign.model.ReactRegistrationTemplate;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationSubType;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationType;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.EmailDAOSupport;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.TransactionDAOSupport;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.instrumentmanagement.model.Instrument;
import com.agaramtech.qualis.instrumentmanagement.model.InstrumentCategory;
import com.agaramtech.qualis.joballocation.model.JobAllocation;
import com.agaramtech.qualis.organization.model.Section;
import com.agaramtech.qualis.registration.model.RegistrationSection;
import com.agaramtech.qualis.registration.model.RegistrationSectionHistory;
import com.agaramtech.qualis.registration.model.RegistrationTest;
import com.agaramtech.qualis.registration.model.RegistrationTestHistory;
import com.agaramtech.qualis.testgroup.model.TestGroupTest;
import com.agaramtech.qualis.testmanagement.model.TestMaster;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class JobAllocationDAOImpl implements JobAllocationDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(JobAllocationDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final EmailDAOSupport emailDAOSupport;
	public final TimeZoneDAO timeZoneDAO;
	private final CommentDAO commentDAO;
	private final AttachmentDAO attachmentDAO;
	private final TransactionDAOSupport transactionDAOSupport;

	@Override
	public ResponseEntity<Object> getJobAllocation(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		LOGGER.info("Job Allocation Initial Get Start");

		final Map<String, Object> map = new HashMap<String, Object>();
		final ObjectMapper objMapper = new ObjectMapper();
		String dfromdate = "";
		String dtodate = "";
		final String strQuery = "select json_agg(jsondata || jsontempdata) as jsondata from filterdetail where nformcode="
				+ userInfo.getNformcode() + " and nusercode=" + userInfo.getNusercode() + " " + " and nuserrolecode="
				+ userInfo.getNuserrole() + " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final String strFilter = jdbcTemplate.queryForObject(strQuery, String.class);

		final List<Map<String, Object>> lstfilterDetail = strFilter != null
				? objMapper.readValue(strFilter, new TypeReference<List<Map<String, Object>>>() {
				})
				: new ArrayList<Map<String, Object>>();
		if (!lstfilterDetail.isEmpty() && lstfilterDetail.get(0).containsKey("fromdate")
				&& lstfilterDetail.get(0).containsKey("todate")) {
			Instant instantFromDate = dateUtilityFunction
					.convertStringDateToUTC(lstfilterDetail.get(0).get("fromdate").toString(), userInfo, true);
			Instant instantToDate = dateUtilityFunction
					.convertStringDateToUTC(lstfilterDetail.get(0).get("todate").toString(), userInfo, true);

			dfromdate = dateUtilityFunction.instantDateToString(instantFromDate);
			dtodate = dateUtilityFunction.instantDateToString(instantToDate);

			map.put("fromDate", dfromdate);
			map.put("toDate", dtodate);
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

			map.put("realFromDate", formattedFromString);
			map.put("realToDate", formattedToString);

		} else {
			final Map<String, Object> mapObject = projectDAOSupport.getDateFromControlProperties(userInfo,
					(String) inputMap.get("currentdate"), "datetime", "FromDate");
			map.put("fromDate", mapObject.get("FromDateWOUTC"));
			map.put("toDate", mapObject.get("ToDateWOUTC"));
			map.put("realFromDate", mapObject.get("FromDateWOUTC"));
			map.put("realToDate", mapObject.get("ToDateWOUTC"));
			dfromdate = (String) mapObject.get("FromDate");
			dtodate = (String) mapObject.get("ToDate");
		}

		inputMap.put("fromdate", dfromdate);
		inputMap.put("todate", dtodate);
		inputMap.put("checkBoxOperation", 3);
		inputMap.put("filterDetailValue", lstfilterDetail);
		List<FilterName> lstFilterName = getFilterName(userInfo);
		map.put("FilterName", lstFilterName);

		map.putAll(getSampleType(inputMap, userInfo).getBody());
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Map<String,Object>> getSampleType(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();
		final ObjectMapper objMapper = new ObjectMapper();

		final String strsampletypequery = " select st.nsampletypecode,st.jsondata->'sampletypename'->>'"
				+ userInfo.getSlanguagetypecode() + "' ssampletypename "
				+ " from sampletype st,approvalconfig ac,approvalconfigversion acv,registrationtype rt,registrationsubtype rst "
				+ " where acv.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and acv.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and ac.napprovalconfigcode = acv.napprovalconfigcode  and ac.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rt.nregtypecode = ac.nregtypecode  and rt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rt.nregtypecode = rst.nregtypecode and rst.nregsubtypecode = ac.nregsubtypecode "
				+ " and rst.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rt.nsampletypecode = st.nsampletypecode  and acv.nsitecode =" + userInfo.getNmastersitecode()
				+ " and st.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and st.nsampletypecode > 0  group by st.nsampletypecode,st.jsondata->'sampletypename'->>'"
				+ userInfo.getSlanguagetypecode() + "' order by st.nsorter";
		final List<SampleType> lstSampleType = jdbcTemplate.query(strsampletypequery, new SampleType());
		if (!lstSampleType.isEmpty()) {

			final SampleType filterSampleType = inputMap.containsKey("filterDetailValue")
					&& !((List<Map<String, Object>>) inputMap.get("filterDetailValue")).isEmpty()
					&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0)
							.containsKey("sampleTypeValue")
									? objMapper.convertValue(
											((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0)
													.get("sampleTypeValue"),
											SampleType.class)
									: lstSampleType.get(0);
			map.put("SampleType", lstSampleType);
			map.put("realSampleTypeList", lstSampleType);
			map.put("realSampleTypeValue", filterSampleType);
			map.put("defaultSampleTypeValue", filterSampleType);
			inputMap.put("realSampleTypeValue", filterSampleType);
			inputMap.put("nsampletypecode", filterSampleType.getNsampletypecode());
			map.putAll(getRegistrationType(inputMap, userInfo).getBody());
		} else {
			map.put("realSampleTypeValue", lstSampleType);
			map.put("defaultSampleTypeValue", lstSampleType);

		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getRegistrationTypeBySampleType(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();
		String Str = "Select * from sampletype  where  nsampletypecode=" + inputMap.get("nsampletypecode");

		SampleType obj = (SampleType) jdbcUtilityFunction.queryForObject(Str, SampleType.class, jdbcTemplate);
		String ValidationQuery = "";
		if (obj.getNtransfiltertypecode() != -1 && userInfo.getNusercode() != -1) {
			int nmappingfieldcode = (obj.getNtransfiltertypecode() == 1) ? userInfo.getNdeptcode()
					: userInfo.getNuserrole();
			ValidationQuery = " and rst.nregsubtypecode in( " + "		SELECT rs.nregsubtypecode "
					+ "		FROM registrationsubtype rs "
					+ "		INNER JOIN transactionfiltertypeconfig ttc ON rs.nregsubtypecode = ttc.nregsubtypecode "
					+ "		LEFT JOIN transactionusers tu ON tu.ntransfiltertypeconfigcode = ttc.ntransfiltertypeconfigcode "
					+ "		WHERE ( ttc.nneedalluser = " + Enumeration.TransactionStatus.YES.gettransactionstatus()
					+ "  and ttc.nstatus =  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
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

		String strregtypequery = "select rt.nregtypecode,coalesce(rt.jsondata->'sregtypename'->>'"
				+ userInfo.getSlanguagetypecode() + "',rt.jsondata->'sregtypename'->>'en-US')  as sregtypename "
				+ "from approvalconfig ac,approvalconfigversion acv,registrationtype rt,registrationsubtype rst "
				+ "where acv.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and ac.napprovalconfigcode = acv.napprovalconfigcode and rt.nregtypecode = ac.nregtypecode "
				+ " and rt.nregtypecode = rst.nregtypecode and rst.nregsubtypecode = ac.nregsubtypecode "
				+ " and acv.nsitecode = " + userInfo.getNmastersitecode() + " and rt.nsampletypecode = "
				+ inputMap.get("nsampletypecode") + " and acv.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rst.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ac.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and rt.nregtypecode > 0 "
				+ ValidationQuery + " group by rt.nregtypecode,sregtypename  order by rt.nregtypecode desc";
		final List<RegistrationType> lstRegistrationType = jdbcTemplate.query(strregtypequery, new RegistrationType());
		if (!lstRegistrationType.isEmpty()) {
			map.put("RegistrationType", lstRegistrationType);
			map.put("defaultRegTypeValue", lstRegistrationType.get(0));
			inputMap.put("realRegTypeValue", lstRegistrationType.get(0));
			inputMap.put("nregtypecode", lstRegistrationType.get(0).getNregtypecode());
			map.putAll(getRegistrationsubTypeByRegType(inputMap, userInfo).getBody());
		} else {
			map.put("defaultRegTypeValue", lstRegistrationType);
			map.put("defaultRegSubTypeValue", null);

		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Map<String,Object>> getRegistrationType(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();
		ObjectMapper objMapper = new ObjectMapper();

		String Str = "Select * from sampletype  where  nsampletypecode=" + inputMap.get("nsampletypecode");

		SampleType obj = (SampleType) jdbcUtilityFunction.queryForObject(Str, SampleType.class, jdbcTemplate);
		String ValidationQuery = "";
		if (obj.getNtransfiltertypecode() != -1 && userInfo.getNusercode() != -1) {
			int nmappingfieldcode = (obj.getNtransfiltertypecode() == 1) ? userInfo.getNdeptcode()
					: userInfo.getNuserrole();
			ValidationQuery = " and rst.nregsubtypecode in( " + "		SELECT rs.nregsubtypecode "
					+ "		FROM registrationsubtype rs "
					+ "		INNER JOIN transactionfiltertypeconfig ttc ON rs.nregsubtypecode = ttc.nregsubtypecode "
					+ "		LEFT JOIN transactionusers tu ON tu.ntransfiltertypeconfigcode = ttc.ntransfiltertypeconfigcode "
					+ "		WHERE ( ttc.nneedalluser = " + Enumeration.TransactionStatus.YES.gettransactionstatus()
					+ "  and ttc.nstatus =  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
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

		String strregtypequery = "select rt.nregtypecode,coalesce(rt.jsondata->'sregtypename'->>'"
				+ userInfo.getSlanguagetypecode() + "',rt.jsondata->'sregtypename'->>'en-US')  as sregtypename "
				+ "from approvalconfig ac,approvalconfigversion acv,registrationtype rt,registrationsubtype rst "
				+ " where acv.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and ac.napprovalconfigcode = acv.napprovalconfigcode and rt.nregtypecode = ac.nregtypecode "
				+ " and rt.nregtypecode = rst.nregtypecode and rst.nregsubtypecode = ac.nregsubtypecode "
				+ " and acv.nsitecode = " + userInfo.getNmastersitecode() + " and rt.nsampletypecode = "
				+ inputMap.get("nsampletypecode") + " and acv.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rst.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ac.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and rt.nregtypecode > 0 "
				+ ValidationQuery + " group by rt.nregtypecode,sregtypename  order by rt.nregtypecode desc";
		final List<RegistrationType> lstRegistrationType = jdbcTemplate.query(strregtypequery, new RegistrationType());
		if (!lstRegistrationType.isEmpty()) {

			final RegistrationType filterRegType = inputMap.containsKey("filterDetailValue")
					&& !((List<Map<String, Object>>) inputMap.get("filterDetailValue")).isEmpty()
					&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0)
							.containsKey("regTypeValue") ? objMapper
									.convertValue(((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0)
											.get("regTypeValue"), RegistrationType.class)
									: lstRegistrationType.get(0);

			map.put("RegistrationType", lstRegistrationType);
			map.put("realRegistrationTypeList", lstRegistrationType);
			map.put("realRegTypeValue", filterRegType);
			map.put("defaultRegTypeValue", filterRegType);
			inputMap.put("realRegTypeValue", filterRegType);
			inputMap.put("nregtypecode", filterRegType.getNregtypecode());
			map.putAll(getRegistrationsubType(inputMap, userInfo).getBody());
		} else {
			map.put("realRegTypeValue", lstRegistrationType);
			map.put("defaultRegTypeValue", lstRegistrationType);

		}
		return new ResponseEntity<Map<String,Object>>(map, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getRegistrationsubType(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();
		ObjectMapper objMapper = new ObjectMapper();

		String Str = "Select * from registrationtype rt,sampletype st where rt.nsampletypecode=st.nsampletypecode and rt.nregTypeCode="
				+ inputMap.get("nregtypecode");

		SampleType obj = (SampleType) jdbcUtilityFunction.queryForObject(Str, SampleType.class, jdbcTemplate);
		String validationQuery = "";
		if (obj.getNtransfiltertypecode() != -1 && userInfo.getNusercode() != -1) {
			int nmappingfieldcode = (obj.getNtransfiltertypecode() == 1) ? userInfo.getNdeptcode()
					: userInfo.getNuserrole();
			validationQuery = " and rst.nregsubtypecode in( " + " SELECT rs.nregsubtypecode "
					+ " FROM registrationsubtype rs "
					+ " INNER JOIN transactionfiltertypeconfig ttc ON rs.nregsubtypecode = ttc.nregsubtypecode "
					+ " LEFT JOIN transactionusers tu ON tu.ntransfiltertypeconfigcode = ttc.ntransfiltertypeconfigcode "
					+ " WHERE ( ttc.nneedalluser = " + Enumeration.TransactionStatus.YES.gettransactionstatus()
					+ "  and ttc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " AND ttc.nmappingfieldcode = " + nmappingfieldcode + ")" + "  OR " + "	( ttc.nneedalluser = "
					+ Enumeration.TransactionStatus.NO.gettransactionstatus() + "  " + " AND ttc.nmappingfieldcode ="
					+ nmappingfieldcode + " AND tu.nusercode =" + userInfo.getNusercode() + " and ttc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tu.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") " + "  OR "
					+ "	( ttc.nneedalluser = " + Enumeration.TransactionStatus.NO.gettransactionstatus() + "  "
					+ " AND tu.nusercode =" + userInfo.getNusercode() + " and ttc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tu.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") " + " AND rs.nregtypecode = "
					+ inputMap.get("nregtypecode") + ")";
		}

		String strregsubtypequery = "select rst.nregsubtypecode,rst.nregtypecode,coalesce (rst.jsondata->'sregsubtypename'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',rst.jsondata->'sregsubtypename'->>'en-US') as sregsubtypename,cast(rsc.jsondata->>'nneedsubsample' as boolean) nneedsubsample,"
				+ "cast(rsc.jsondata->>'nneedmyjob' as boolean) nneedmyjob "
				+ " from approvalconfig ac,approvalconfigversion acv,registrationsubtype rst,regsubtypeconfigversion rsc "
				+ " where acv.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and rsc.napprovalconfigcode = ac.napprovalconfigcode and rsc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rsc.ntransactionstatus = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " "
				+ " and ac.napprovalconfigcode = acv.napprovalconfigcode and rst.nregtypecode = "
				+ inputMap.get("nregtypecode") + " and (rsc.jsondata->'nneedjoballocation')::jsonb ='true' "
				+ " and ac.napprovalconfigcode = acv.napprovalconfigcode and rst.nregsubtypecode = ac.nregsubtypecode "
				+ " and rst.nregsubtypecode > 0  and acv.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rst.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ac.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + validationQuery
				+ "order by rst.nregsubtypecode desc";
		final List<RegistrationSubType> lstRegistrationSubType = jdbcTemplate.query(strregsubtypequery,
				new RegistrationSubType());
		if (!lstRegistrationSubType.isEmpty()) {
			final RegistrationSubType filterRegSubType = inputMap.containsKey("filterDetailValue")
					&& !((List<Map<String, Object>>) inputMap.get("filterDetailValue")).isEmpty()
					&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0)
							.containsKey("regSubTypeValue")
									? objMapper.convertValue(
											((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0)
													.get("regSubTypeValue"),
											RegistrationSubType.class)
									: lstRegistrationSubType.get(0);

			map.put("RegistrationSubType", lstRegistrationSubType);
			map.put("realRegistrationSubTypeList", lstRegistrationSubType);
			map.put("defaultRegSubTypeValue", filterRegSubType);
			map.put("realRegSubTypeValue", filterRegSubType);
			map.put("RegSubTypeValue", filterRegSubType);
			map.put("nneedsubsample", filterRegSubType.isNneedsubsample());
			inputMap.put("realRegSubTypeValue", filterRegSubType);
			inputMap.put("nregsubtypecode", filterRegSubType.getNregsubtypecode());
			inputMap.put("nneedsubsample", filterRegSubType.isNneedsubsample());
			map.put("nneedmyjob", filterRegSubType.isNneedmyjob());
			map.putAll((Map<? extends String, ? extends Object>) getApprovalConfigVersion(inputMap, userInfo).getBody());

		} else {
			map.put("realRegSubTypeValue", lstRegistrationSubType);
			map.put("defaultRegSubTypeValue", lstRegistrationSubType);

		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Map<String,Object>> getRegistrationsubTypeByRegType(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();
		String Str = "Select * from registrationtype rt,sampletype st where rt.nsampletypecode=st.nsampletypecode and rt.nregTypeCode="
				+ inputMap.get("nregtypecode");

		SampleType obj = (SampleType) jdbcUtilityFunction.queryForObject(Str, SampleType.class, jdbcTemplate);
		String validationQuery = "";
		if (obj.getNtransfiltertypecode() != -1 && userInfo.getNusercode() != -1) {
			int nmappingfieldcode = (obj.getNtransfiltertypecode() == 1) ? userInfo.getNdeptcode()
					: userInfo.getNuserrole();
			validationQuery = " and rst.nregsubtypecode in( " + " SELECT rs.nregsubtypecode "
					+ " FROM registrationsubtype rs "
					+ " INNER JOIN transactionfiltertypeconfig ttc ON rs.nregsubtypecode = ttc.nregsubtypecode "
					+ " LEFT JOIN transactionusers tu ON tu.ntransfiltertypeconfigcode = ttc.ntransfiltertypeconfigcode "
					+ " WHERE ( ttc.nneedalluser = " + Enumeration.TransactionStatus.YES.gettransactionstatus()
					+ "  and ttc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " AND ttc.nmappingfieldcode = " + nmappingfieldcode + ")" + "  OR " + "	( ttc.nneedalluser = "
					+ Enumeration.TransactionStatus.NO.gettransactionstatus() + "  " + " AND ttc.nmappingfieldcode ="
					+ nmappingfieldcode + " AND tu.nusercode =" + userInfo.getNusercode() + " and ttc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tu.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") " + "  OR "
					+ "	( ttc.nneedalluser = " + Enumeration.TransactionStatus.NO.gettransactionstatus() + "  "
					+ " AND tu.nusercode =" + userInfo.getNusercode() + " and ttc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tu.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") " + " AND rs.nregtypecode = "
					+ inputMap.get("nregtypecode") + ")";
		}

		String strregsubtypequery = "select rst.nregsubtypecode,rst.nregtypecode,coalesce (rst.jsondata->'sregsubtypename'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',rst.jsondata->'sregsubtypename'->>'en-US') as sregsubtypename,cast(rsc.jsondata->>'nneedsubsample' as boolean) nneedsubsample,"
				+ "cast(rsc.jsondata->>'nneedmyjob' as boolean) nneedmyjob "
				+ " from approvalconfig ac,approvalconfigversion acv,registrationsubtype rst,regsubtypeconfigversion rsc "
				+ " where acv.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and rsc.napprovalconfigcode = ac.napprovalconfigcode and rsc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rsc.ntransactionstatus = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " "
				+ " and ac.napprovalconfigcode = acv.napprovalconfigcode and rst.nregtypecode = "
				+ inputMap.get("nregtypecode") + " and (rsc.jsondata->'nneedjoballocation')::jsonb ='true' "
				+ " and ac.napprovalconfigcode = acv.napprovalconfigcode and rst.nregsubtypecode = ac.nregsubtypecode "
				+ " and rst.nregsubtypecode > 0  and acv.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rst.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ac.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + validationQuery
				+ " order by rst.nregsubtypecode desc";
		final List<RegistrationSubType> lstRegistrationSubType = jdbcTemplate.query(strregsubtypequery,
				new RegistrationSubType());
		if (!lstRegistrationSubType.isEmpty()) {
			map.put("RegistrationSubType", lstRegistrationSubType);
			map.put("defaultRegSubTypeValue", lstRegistrationSubType.get(0));
			map.put("RegSubTypeValue", lstRegistrationSubType.get(0));
			inputMap.put("nregsubtypecode", lstRegistrationSubType.get(0).getNregsubtypecode());
			inputMap.put("nneedsubsample", lstRegistrationSubType.get(0).isNneedsubsample());
			map.put("nneedsubsample", lstRegistrationSubType.get(0).isNneedsubsample());
			map.put("nneedmyjob", lstRegistrationSubType.get(0).isNneedmyjob());

			map.putAll((Map<String, Object>) getApprovalConfigVersionByRegSubType(inputMap, userInfo).getBody());
		} else {
			map.put("defaultRegSubTypeValue", lstRegistrationSubType);
			map.put("RegistrationSubType", lstRegistrationSubType);

			map.put("defaultApprovalVersionValue", null);
			map.put("ApprovalConfigVersion", null);

			map.put("FilterStatus", null);
			map.put("defaultFilterStatusValue", null);

			map.put("UserSection", null);
			map.put("defaultUserSectionValue", null);

			map.put("Test", null);
			map.put("defaultTestValue", null);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getRegistrationTemplateList(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();
		ObjectMapper objMapper = new ObjectMapper();

		final String str = "select dm.ndesigntemplatemappingcode,rt.jsondata,dm.ntransactionstatus,CONCAT(rt.sregtemplatename,'(',cast(dm.nversionno as character varying),')') sregtemplatename from designtemplatemapping dm, "
				+ "reactregistrationtemplate rt,approvalconfigversion acv where nregtypecode="
				+ inputMap.get("nregtypecode") + " and nregsubtypecode=" + inputMap.get("nregsubtypecode")
				+ " and rt.nreactregtemplatecode=dm.nreactregtemplatecode and acv.ndesigntemplatemappingcode=dm.ndesigntemplatemappingcode and "
				+ " dm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  acv.napproveconfversioncode="
				+ inputMap.get("napprovalversioncode") + " order by dm.ndesigntemplatemappingcode desc";

		List<DesignTemplateMapping> lstReactRegistrationTemplate = jdbcTemplate.query(str, new DesignTemplateMapping());
		if (!lstReactRegistrationTemplate.isEmpty()) {

			final DesignTemplateMapping filterDesignMapping = inputMap.containsKey("filterDetailValue")
					&& !((List<Map<String, Object>>) inputMap.get("filterDetailValue")).isEmpty()
					&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0)
							.containsKey("designTemplateMappingValue")
									? objMapper
											.convertValue(
													((List<Map<String, Object>>) inputMap.get("filterDetailValue"))
															.get(0).get("designTemplateMappingValue"),
													DesignTemplateMapping.class)
									: lstReactRegistrationTemplate.get(0);
			map.put("DynamicDesignMapping", lstReactRegistrationTemplate);
			map.put("realDynamicDesignMappingList", lstReactRegistrationTemplate);
			map.put("realDesignTemplateMappingValue", filterDesignMapping);
			map.put("defaultDesignTemplateMappingValue", filterDesignMapping);
			map.put("DesignTemplateMappingValue", filterDesignMapping);
			map.put("ndesigntemplatemappingcode", filterDesignMapping.getNdesigntemplatemappingcode());
			inputMap.put("realDesignTemplateMappingValue", filterDesignMapping);
			inputMap.put("ndesigntemplatemappingcode", filterDesignMapping.getNdesigntemplatemappingcode());
		} else {
			map.put("defaultDesignTemplateMappingValue", lstReactRegistrationTemplate);
			map.put("DynamicDesignMapping", lstReactRegistrationTemplate);
			map.put("ndesigntemplatemappingcode", lstReactRegistrationTemplate);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	public ResponseEntity<Object> getRegistrationTemplateListByApprovalConfigVersion(Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();

		final String str = "select dm.ndesigntemplatemappingcode,rt.jsondata,dm.ntransactionstatus,CONCAT(rt.sregtemplatename,'(',cast(dm.nversionno as character varying),')') sregtemplatename from designtemplatemapping dm, "
				+ "reactregistrationtemplate rt,approvalconfigversion acv where nregtypecode="
				+ inputMap.get("nregtypecode") + " and nregsubtypecode=" + inputMap.get("nregsubtypecode")
				+ " and rt.nreactregtemplatecode=dm.nreactregtemplatecode and acv.ndesigntemplatemappingcode=dm.ndesigntemplatemappingcode and "
				+ " dm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  acv.napproveconfversioncode="
				+ inputMap.get("napprovalversioncode") + " order by dm.ndesigntemplatemappingcode desc";

		List<DesignTemplateMapping> lstReactRegistrationTemplate = jdbcTemplate.query(str, new DesignTemplateMapping());
		if (!lstReactRegistrationTemplate.isEmpty()) {
			map.put("DynamicDesignMapping", lstReactRegistrationTemplate);
			map.put("defaultDesignTemplateMappingValue", lstReactRegistrationTemplate.get(0));
			map.put("DesignTemplateMappingValue", lstReactRegistrationTemplate.get(0));
			map.put("ndesigntemplatemappingcode", lstReactRegistrationTemplate.get(0).getNdesigntemplatemappingcode());
			inputMap.put("realDesignTemplateMappingValue", lstReactRegistrationTemplate.get(0));
			inputMap.put("ndesigntemplatemappingcode",
					lstReactRegistrationTemplate.get(0).getNdesigntemplatemappingcode());
		} else {
			map.put("realDesignTemplateMappingValue", lstReactRegistrationTemplate);
			map.put("defaultDesignTemplateMappingValue", lstReactRegistrationTemplate);
			map.put("DynamicDesignMapping", lstReactRegistrationTemplate);
			map.put("ndesigntemplatemappingcode", lstReactRegistrationTemplate);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getApprovalConfigVersion(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();
		ObjectMapper objMapper = new ObjectMapper();

		final String fromDate = dateUtilityFunction.instantDateToString(
				dateUtilityFunction.convertStringDateToUTC((String) inputMap.get("fromdate"), userInfo, true));
		final String toDate = dateUtilityFunction.instantDateToString(
				dateUtilityFunction.convertStringDateToUTC((String) inputMap.get("todate"), userInfo, true));
		int versionCode = -1;
		String versionquery = "select aca.sversionname,aca.napprovalconfigcode,aca.napprovalconfigversioncode ,acv.ntransactionstatus,acv.ntreeversiontempcode  "
				+ "from registration r,registrationarno rar,registrationhistory rh,approvalconfigautoapproval aca, "
				+ "approvalconfig ac,approvalconfigversion acv  "
				+ " where r.npreregno=rar.npreregno and r.npreregno=rh.npreregno "
				+ " and rar.napprovalversioncode=acv.napproveconfversioncode "
				+ " and acv.napproveconfversioncode=aca.napprovalconfigversioncode "
				+ " and r.nregtypecode=ac.nregtypecode and r.nregsubtypecode=ac.nregsubtypecode "
				+ " and r.nregtypecode =" + inputMap.get("nregtypecode") + " and r.nregsubtypecode ="
				+ inputMap.get("nregsubtypecode") + " " + " and rh.ntransactionstatus = "
				+ Enumeration.TransactionStatus.REGISTERED.gettransactionstatus() + " and acv.nsitecode ="
				+ userInfo.getNmastersitecode() + " " + " and rh.dtransactiondate between '" + fromDate + "' and '"
				+ toDate + "' " + " and r.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rar.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rh.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and ac.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and ac.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and acv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and aca.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " group by acv.ntransactionstatus,aca.napprovalconfigversioncode,acv.ntreeversiontempcode,aca.napprovalconfigcode,aca.sversionname";
		final List<ApprovalConfigAutoapproval> lstApprovalConfigVersion = jdbcTemplate.query(versionquery,
				new ApprovalConfigAutoapproval());
		if (!lstApprovalConfigVersion.isEmpty()) {
			List<ApprovalConfigAutoapproval> approvedApprovalVersionList = lstApprovalConfigVersion.stream().filter(
					obj -> obj.getNtransactionstatus() == Enumeration.TransactionStatus.APPROVED.gettransactionstatus())
					.collect(Collectors.toList());

			if (approvedApprovalVersionList != null && !approvedApprovalVersionList.isEmpty()) {
				final ApprovalConfigAutoapproval filterApprovalConfig = inputMap.containsKey("filterDetailValue")
						&& !((List<Map<String, Object>>) inputMap.get("filterDetailValue")).isEmpty()
						&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0)
								.containsKey("approvalConfigValue")
										? objMapper
												.convertValue(
														((List<Map<String, Object>>) inputMap.get("filterDetailValue"))
																.get(0).get("approvalConfigValue"),
														ApprovalConfigAutoapproval.class)
										: approvedApprovalVersionList.get(0);

				inputMap.put("napprovalversioncode", filterApprovalConfig.getNapprovalconfigversioncode());
				inputMap.put("realApprovalVersionValue", filterApprovalConfig);
				map.put("realApprovalVersionValue", filterApprovalConfig);
				map.put("defaultApprovalVersionValue", filterApprovalConfig);
				versionCode = filterApprovalConfig.getNapprovalconfigversioncode();

			} else {
				final ApprovalConfigAutoapproval filterApprovalConfig = inputMap.containsKey("filterDetailValue")
						&& !((List<Map<String, Object>>) inputMap.get("filterDetailValue")).isEmpty()
						&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0)
								.containsKey("approvalConfigValue")
										? objMapper
												.convertValue(
														((List<Map<String, Object>>) inputMap.get("filterDetailValue"))
																.get(0).get("approvalConfigValue"),
														ApprovalConfigAutoapproval.class)
										: lstApprovalConfigVersion.get(0);

				inputMap.put("realApprovalVersionValue", filterApprovalConfig);
				inputMap.put("napprovalversioncode", filterApprovalConfig.getNapprovalconfigversioncode());
				map.put("realApprovalVersionValue", filterApprovalConfig);
				map.put("defaultApprovalVersionValue", filterApprovalConfig);
				versionCode = filterApprovalConfig.getNapprovalconfigversioncode();
			}

		} else {
			map.put("realApprovalVersionValue", lstApprovalConfigVersion);
			map.put("defaultApprovalVersionValue", lstApprovalConfigVersion);
		}
		inputMap.put("napprovalversioncode", versionCode);

		map.putAll((Map<String, Object>) getRegistrationTemplateList(inputMap, userInfo).getBody());
		map.put("ApprovalConfigVersion", lstApprovalConfigVersion);
		map.put("realApprovalConfigVersionList", lstApprovalConfigVersion);

		final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());
		final String fromDateUI = LocalDateTime.parse((String) inputMap.get("fromdate"), dbPattern).format(uiPattern);
		final String toDateUI = LocalDateTime.parse((String) inputMap.get("todate"), dbPattern).format(uiPattern);
		map.put("fromDate", fromDateUI);
		map.put("toDate", toDateUI);
		map.putAll((Map<String, Object>) getFilterStatus(inputMap, userInfo).getBody());
		map.putAll((Map<String, Object>) getSection(inputMap, userInfo).getBody());
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getApprovalConfigVersionByRegSubType(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();
		final String fromDate = dateUtilityFunction.instantDateToString(
				dateUtilityFunction.convertStringDateToUTC((String) inputMap.get("fromdate"), userInfo, true));
		final String toDate = dateUtilityFunction.instantDateToString(
				dateUtilityFunction.convertStringDateToUTC((String) inputMap.get("todate"), userInfo, true));
		int versionCode = -1;

		String versionquery = "select aca.sversionname,aca.napprovalconfigcode,aca.napprovalconfigversioncode ,acv.ntransactionstatus,acv.ntreeversiontempcode  "
				+ " from registration r,registrationarno rar,registrationhistory rh,approvalconfigautoapproval aca, "
				+ " approvalconfig ac,approvalconfigversion acv  "
				+ " where r.npreregno=rar.npreregno and r.npreregno=rh.npreregno "
				+ " and rar.napprovalversioncode=acv.napproveconfversioncode "
				+ " and acv.napproveconfversioncode=aca.napprovalconfigversioncode "
				+ " and r.nregtypecode=ac.nregtypecode and r.nregsubtypecode=ac.nregsubtypecode "
				+ " and r.nregtypecode =" + inputMap.get("nregtypecode") + " and r.nregsubtypecode ="
				+ inputMap.get("nregsubtypecode") + " and rh.ntransactionstatus = "
				+ Enumeration.TransactionStatus.REGISTERED.gettransactionstatus() + " and acv.nsitecode ="
				+ userInfo.getNmastersitecode() + " " + " and rh.dtransactiondate between '" + fromDate + "' and '"
				+ toDate + "' and r.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rar.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rh.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ac.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ac.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and aca.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " group by acv.ntransactionstatus,aca.napprovalconfigversioncode,acv.ntreeversiontempcode,aca.napprovalconfigcode,aca.sversionname";
		final List<ApprovalConfigAutoapproval> lstApprovalConfigVersion = jdbcTemplate.query(versionquery,
				new ApprovalConfigAutoapproval());

		if (!lstApprovalConfigVersion.isEmpty()) {
			List<ApprovalConfigAutoapproval> approvedApprovalVersionList = lstApprovalConfigVersion.stream().filter(
					obj -> obj.getNtransactionstatus() == Enumeration.TransactionStatus.APPROVED.gettransactionstatus())
					.collect(Collectors.toList());

			if (approvedApprovalVersionList != null && !approvedApprovalVersionList.isEmpty()) {
				inputMap.put("napprovalconfigversioncode",
						approvedApprovalVersionList.get(0).getNapprovalconfigversioncode());
				map.put("defaultApprovalVersionValue", approvedApprovalVersionList.get(0));
				versionCode = approvedApprovalVersionList.get(0).getNapprovalconfigversioncode();

			} else {
				inputMap.put("napprovalconfigversioncode",
						lstApprovalConfigVersion.get(0).getNapprovalconfigversioncode());
				map.put("defaultApprovalVersionValue", lstApprovalConfigVersion.get(0));
				versionCode = lstApprovalConfigVersion.get(0).getNapprovalconfigversioncode();
			}
		} else {
			map.put("defaultApprovalVersionValue", lstApprovalConfigVersion);

		}
		map.put("ApprovalConfigVersion", lstApprovalConfigVersion);
		inputMap.put("napprovalversioncode", versionCode);
		map.putAll(
				(Map<String, Object>) getRegistrationTemplateListByApprovalConfigVersion(inputMap, userInfo).getBody());

		final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());

		final String fromDateUI = LocalDateTime.parse((String) inputMap.get("fromdate"), dbPattern).format(uiPattern);
		final String toDateUI = LocalDateTime.parse((String) inputMap.get("todate"), dbPattern).format(uiPattern);
		map.put("fromDate", fromDateUI);
		map.put("toDate", toDateUI);
		map.putAll((Map<String, Object>) getFilterStatusByApproveVersion(inputMap, userInfo).getBody());
		map.putAll((Map<String, Object>) getSectionByApproveVersion(inputMap, userInfo).getBody());

		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getFilterStatus(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();
		final Map<String, Object> obj = new HashMap<String, Object>();
		List<Object> lstobj = new ArrayList<Object>();
		ObjectMapper objMapper = new ObjectMapper();

		List<TransactionStatus> lstfilterdetails = new ArrayList<TransactionStatus>();
		String filterquery = "";
		filterquery = " select sc.ntranscode as ntransactionstatus,ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "' stransdisplaystatus  "
				+ " from approvalstatusconfig sc,transactionstatus ts where ts.ntranscode = sc.ntranscode  "
				+ " and sc.nregtypecode =" + inputMap.get("nregtypecode") + " and sc.nregsubtypecode="
				+ inputMap.get("nregsubtypecode") + " " + " and sc.nformcode = " + userInfo.getNformcode() + " "
				+ " and sc.nsitecode = " + userInfo.getNmastersitecode() + " and sc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		lstfilterdetails = jdbcTemplate.query(filterquery, new TransactionStatus());

		if (!lstfilterdetails.isEmpty()) {

			if (lstfilterdetails.size() > 1) {
				obj.put("ntransactionstatus", 0);
				obj.put("stransdisplaystatus",
						commonFunction.getMultilingualMessage("IDS_ALL", userInfo.getSlanguagefilename()));
				lstobj.add(obj);
				lstobj.addAll(lstfilterdetails);
				final TransactionStatus filterTransactionStatus = inputMap.containsKey("filterDetailValue")
						&& !((List<Map<String, Object>>) inputMap.get("filterDetailValue")).isEmpty()
						&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0)
								.containsKey("filterStatusValue")
										? objMapper.convertValue(
												((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0)
														.get("filterStatusValue"),
												TransactionStatus.class)
										: objMapper.convertValue(lstobj.get(0), TransactionStatus.class);

				map.put("FilterStatus", lstobj);
				map.put("realFilterStatusList1", lstobj);
				map.put("realFilterStatusValue", filterTransactionStatus);
				map.put("defaultFilterStatusValue", filterTransactionStatus);
				inputMap.put("realFilterStatusValue", filterTransactionStatus);

			} else {
				map.put("FilterStatus", lstfilterdetails);
				map.put("realFilterStatusList", lstfilterdetails);
				map.put("realFilterStatusValue", lstfilterdetails.get(0));
				map.put("defaultFilterStatusValue", lstfilterdetails.get(0));
				inputMap.put("realFilterStatusValue", lstfilterdetails.get(0));
			}
			String status = lstfilterdetails.stream()
					.map(objtranscode -> String.valueOf(objtranscode.getNtransactionstatus()))
					.collect(Collectors.joining(","));
			inputMap.put("ntransactionstatus", status);

		} else {
			map.put("realFilterStatusValue", lstfilterdetails);
			map.put("defaultFilterStatusValue", lstfilterdetails);
		}

		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getFilterStatusByApproveVersion(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();
		final Map<String, Object> obj = new HashMap<String, Object>();
		List<Object> lstobj = new ArrayList<Object>();
		List<TransactionStatus> lstfilterdetails = new ArrayList<TransactionStatus>();
		String filterquery = "";
		filterquery = " select sc.ntranscode as ntransactionstatus,ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "' stransdisplaystatus  "
				+ " from approvalstatusconfig sc,transactionstatus ts where ts.ntranscode = sc.ntranscode  "
				+ " and sc.nregtypecode =" + inputMap.get("nregtypecode") + " and sc.nregsubtypecode="
				+ inputMap.get("nregsubtypecode") + " and sc.nformcode = " + userInfo.getNformcode() + " "
				+ " and sc.nsitecode = " + userInfo.getNmastersitecode() + " and sc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		lstfilterdetails = jdbcTemplate.query(filterquery, new TransactionStatus());

		if (!lstfilterdetails.isEmpty()) {

			if (lstfilterdetails.size() > 1) {
				obj.put("ntransactionstatus", 0);
				obj.put("stransdisplaystatus",
						commonFunction.getMultilingualMessage("IDS_ALL", userInfo.getSlanguagefilename()));
				lstobj.add(obj);
				lstobj.addAll(lstfilterdetails);
				map.put("FilterStatus", lstobj);
				map.put("defaultFilterStatusValue", lstobj.get(0));

			} else {
				map.put("FilterStatus", lstfilterdetails);
				map.put("defaultFilterStatusValue", lstfilterdetails.get(0));
			}
			String status = lstfilterdetails.stream()
					.map(objtranscode -> String.valueOf(objtranscode.getNtransactionstatus()))
					.collect(Collectors.joining(","));
			inputMap.put("ntransactionstatus", status);
		} else {
			map.put("defaultFilterStatusValue", lstfilterdetails);

		}

		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getSection(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();
		List<Section> lstSection = new ArrayList<Section>();
		ObjectMapper objMapper = new ObjectMapper();

		String sectionquery = "select rt.nsectioncode,s.ssectionname from registration r,registrationarno ra, registrationtest rt, section s where  "
				+ "r.npreregno =rt.npreregno and r.npreregno=ra.npreregno and  rt.nsectioncode =s.nsectioncode "
				+ " and r.nregtypecode =" + inputMap.get("nregtypecode") + " and r.nregsubtypecode ="
				+ inputMap.get("nregsubtypecode") + " and r.ndesigntemplatemappingcode ="
				+ inputMap.get("ndesigntemplatemappingcode") + " and ra.napprovalversioncode ="
				+ inputMap.get("napprovalversioncode") + " and s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and s.nsectioncode = any ( select ls.nsectioncode from sectionusers su,labsection ls where "
				+ " ls.nlabsectioncode=su.nlabsectioncode and su.nusercode=" + userInfo.getNusercode()
				+ " and su.nsitecode=" + userInfo.getNtranssitecode() + " and su.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ls.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by ls.nsectioncode)"
				+ " group by rt.nsectioncode,s.ssectionname";

		lstSection = ((List<Section>) jdbcTemplate.query(sectionquery, new Section()));

		if (!lstSection.isEmpty()) {

			final Section filterSection = inputMap.containsKey("filterDetailValue")
					&& !((List<Map<String, Object>>) inputMap.get("filterDetailValue")).isEmpty()
					&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0)
							.containsKey("sectionValue")
									? objMapper.convertValue(
											((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0)
													.get("sectionValue"),
											Section.class)
									: lstSection.get(0);

			map.put("UserSection", lstSection);
			map.put("realUserSectionList", lstSection);
			map.put("realUserSectionValue", filterSection);
			map.put("defaultUserSectionValue", filterSection);
			Integer abc = lstSection.get(0).getNsectioncode();
			String section = abc.toString();

			inputMap.put("nsectioncode", section);

			map.putAll((Map<String, Object>) getTestCombo(inputMap, userInfo).getBody());
		} else {
			map.put("realUserSectionValue", lstSection);
			map.put("defaultUserSectionValue", lstSection);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getSectionByApproveVersion(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();
		List<Section> lstSection = new ArrayList<Section>();
		String sectionquery = "select rt.nsectioncode,s.ssectionname from registration r,registrationarno ra, registrationtest rt, section s where "
				+ "r.npreregno=ra.npreregno and r.npreregno =rt.npreregno and rt.nsectioncode =s.nsectioncode "
				+ " and r.nregtypecode =" + inputMap.get("nregtypecode") + " and r.nregsubtypecode ="
				+ inputMap.get("nregsubtypecode") + " and r.ndesigntemplatemappingcode ="
				+ inputMap.get("ndesigntemplatemappingcode") + " and ra.napprovalversioncode ="
				+ inputMap.get("napprovalversioncode") + " and s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and s.nsectioncode = any ( select ls.nsectioncode from sectionusers su,labsection ls where su.nusercode ="
				+ userInfo.getNusercode() + " and ls.nlabsectioncode=su.nlabsectioncode  and su.nsitecode="
				+ userInfo.getNtranssitecode() + " and su.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ls.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by ls.nsectioncode)"
				+ " group by rt.nsectioncode,s.ssectionname";
		lstSection = ((List<Section>) jdbcTemplate.query(sectionquery, new Section()));

		if (!lstSection.isEmpty()) {

			map.put("UserSection", lstSection);
			map.put("defaultUserSectionValue", lstSection.get(0));
			Integer abc = lstSection.get(0).getNsectioncode();
			String section = abc.toString();
			inputMap.put("nsectioncode", section);
			map.putAll((Map<String, Object>) getTestComboBySection(inputMap, userInfo).getBody());
		} else {
			map.put("defaultUserSectionValue", lstSection);

		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getTestCombo(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		final Map<String, Object> obj = new HashMap<>();
		final List<Object> lstobj = new ArrayList<>();
		List<Section> lstSection = new ArrayList<>();
		ObjectMapper objMapper = new ObjectMapper();

		String sectioncodes = "";
		String sectionvalue = "";
		String ntranscode = "";
		String TransactionStatus = "";
		String getTest = "";

		if (inputMap.containsKey("nsectioncode")) {
			sectionvalue = (String) inputMap.get("nsectioncode");
		}

		if (inputMap.containsKey("ntransactionstatus")) {
			ntranscode = (String) inputMap.get("ntransactionstatus");
		}
		sectioncodes = "and rt.nsectioncode in (" + sectionvalue + ")";
		TransactionStatus = "and rth.ntransactionstatus in(" + ntranscode + ") ";

		if (inputMap.containsKey("nflag") && (int) inputMap.get("nflag") == 7) {
			sectioncodes = " and rt.nsectioncode = any ( select ls.nsectioncode from sectionusers su,labsection ls where su.nusercode ="
					+ userInfo.getNusercode() + " and ls.nlabsectioncode=su.nlabsectioncode and su.nsitecode="
					+ userInfo.getNtranssitecode() + " and su.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ls.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by ls.nsectioncode)";
		}
		getTest = "select tgt.stestsynonym,tgt.ntestcode,tgt.nsectioncode from testgrouptest tgt, registrationtest rt "
				+ "where rt.ntestgrouptestcode = tgt.ntestgrouptestcode   " + "and rt.ntransactiontestcode = any (  "
				+ " select rth.ntransactiontestcode from registrationtesthistory rth,registrationarno rar,registration r  "
				+ " where " + "rth.ntesthistorycode = any (  "
				+ " select max(ntesthistorycode) from registrationtesthistory where nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntransactionstatus in ("
				+ ntranscode + ") "
				+ " group by ntransactiontestcode  ) and r.npreregno = rar.npreregno and r.nregtypecode ="
				+ inputMap.get("nregtypecode") + "  and r.nregsubtypecode = " + inputMap.get("nregsubtypecode") + " "
				+ " and r.ndesigntemplatemappingcode =" + inputMap.get("ndesigntemplatemappingcode") + " "
				+ TransactionStatus + ""
				+ " and rth.ntransactiontestcode not in(select max(ntransactiontestcode) from registrationtesthistory "
				+ "	where ntransactionstatus=" + Enumeration.TransactionStatus.CANCELED.gettransactionstatus()
				+ "  and nformcode =" + Enumeration.QualisForms.SAMPLEREGISTRATION.getqualisforms()
				+ " group by ntransactiontestcode)" + " and rth.dtransactiondate between '" + inputMap.get("fromdate")
				+ "' and '" + inputMap.get("todate") + "' and rar.npreregno = rth.npreregno and rar.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rar.napprovalversioncode = "
				+ inputMap.get("napprovalversioncode") + ") " + sectioncodes + "  "
				+ "group by tgt.stestsynonym,tgt.ntestcode,tgt.nsectioncode ";

		List<TestGroupTest> lstTest = jdbcTemplate.query(getTest, new TestGroupTest());
		if (!lstTest.isEmpty()) {
			if (lstTest.size() > 1) {

				obj.put("ntestcode", 0);
				obj.put("stestsynonym",
						commonFunction.getMultilingualMessage("IDS_ALL", userInfo.getSlanguagefilename()));
				lstobj.add(obj);
				lstobj.addAll(lstTest);

				final TestGroupTest filterTransactionTest = inputMap.containsKey("filterDetailValue")
						&& !((List<Map<String, Object>>) inputMap.get("filterDetailValue")).isEmpty()
						&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0)
								.containsKey("testValue")
										? objMapper.convertValue(
												((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0)
														.get("testValue"),
												TestGroupTest.class)
										: objMapper.convertValue(lstobj.get(0), TestGroupTest.class);

				map.put("Test", lstobj);
				map.put("realTestList", lstobj);
				map.put("realTestValue", filterTransactionTest);
				map.put("defaultTestValue", filterTransactionTest);
				map.put("ntestcode", "0");

			} else {

				final TestGroupTest filterTransactionTest = inputMap.containsKey("filterDetailValue")
						&& !((List<Map<String, Object>>) inputMap.get("filterDetailValue")).isEmpty()
						&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0)
								.containsKey("testValue")
										? objMapper.convertValue(
												((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0)
														.get("testValue"),
												TestGroupTest.class)
										: lstTest.get(0);

				map.put("Test", lstTest);
				map.put("realTestList", lstTest);
				map.put("realTestValue", filterTransactionTest);
				map.put("defaultTestValue", filterTransactionTest);
				map.put("ntestcode", String.valueOf(filterTransactionTest.getNtestcode()));
			}
			inputMap.put("activeSampleTab", "IDS_SAMPLEATTACHMENTS");
			inputMap.put("activeSubSampleTab", "IDS_SUBSAMPLEATTACHMENTS");
			inputMap.put("activeTestTab", "IDS_TESTATTACHMENTS");
			inputMap.put("ntestcode", map.get("ntestcode"));

			if (inputMap.containsKey("nflag") && (int) inputMap.get("nflag") == 1) {
				map.putAll((Map<String, Object>) getJobAllocationDetails(inputMap, userInfo).getBody());
			} else if (inputMap.containsKey("nflag") && (int) inputMap.get("nflag") == 7) {
				final String sectioncodevalue = lstTest.stream().map(x -> String.valueOf(x.getNsectioncode()))
						.collect(Collectors.joining(","));
				String getSection = "select * from section s join labsection ls on ls.nsectioncode=s.nsectioncode join sectionusers su on su.nlabsectioncode=ls.nlabsectioncode "
						+ " where su.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ls.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and s.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and s.nsectioncode in (" + sectioncodevalue + ") and su.nusercode= "
						+ userInfo.getNusercode() + " and su.nsitecode=" + userInfo.getNtranssitecode() + " ";

				lstSection = jdbcTemplate.query(getSection, new Section());

				if (lstSection.size() > 1) {
					Map<String, Object> obj1 = new HashMap<>();
					List<Object> lstObj1 = new ArrayList<>();
					obj1.put("nsectioncode", 0);
					obj1.put("ssectionname",
							commonFunction.getMultilingualMessage("IDS_ALL", userInfo.getSlanguagefilename()));
					lstObj1.add(obj1);
					lstObj1.addAll(lstSection);
					map.put("UserSection", lstObj1);
					map.put("realUserSectionList", lstObj1);
					map.put("realUserSectionValue", lstObj1.get(0));
					map.put("defaultUserSectionValue", lstObj1.get(0));

				} else {
					map.put("UserSection", lstSection);
					map.put("realUserSectionValue", lstSection.get(0));
					map.put("defaultUserSectionValue", lstSection.get(0));
				}
			}
		} else {
			if (inputMap.containsKey("nflag") && (int) inputMap.get("nflag") == 7) {
				map.put("UserSection", lstSection);
				map.put("realUserSectionValue", lstSection);
				map.put("defaultUserSectionValue", lstSection);

			}
			map.put("Test", lstTest);
			map.put("realTestList", lstTest);
			map.put("realTestValue", lstTest);
			map.put("defaultTestValue", lstTest);

			inputMap.put("activeSampleTab", "IDS_SAMPLEATTACHMENTS");
			inputMap.put("activeSubSampleTab", "IDS_SUBSAMPLEATTACHMENTS");
			inputMap.put("activeTestTab", "IDS_TESTATTACHMENTS");
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getTestComboBySection(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		final Map<String, Object> obj = new HashMap<>();
		final List<Object> lstobj = new ArrayList<>();
		List<Section> lstSection = new ArrayList<>();
		String sectioncodes = "";
		String sectionvalue = "";
		String ntranscode = "";
		String TransactionStatus = "";
		String getTest = "";

		if (inputMap.containsKey("nsectioncode")) {
			sectionvalue = (String) inputMap.get("nsectioncode");
		}

		if (inputMap.containsKey("ntransactionstatus")) {
			ntranscode = (String) inputMap.get("ntransactionstatus");
		}
		sectioncodes = " and rt.nsectioncode in (" + sectionvalue + ")";
		TransactionStatus = " and rth.ntransactionstatus in(" + ntranscode + ") ";

		if ((int) inputMap.get("nflag") == 7 || (int) inputMap.get("nflag") == 5) {
			sectioncodes = " and rt.nsectioncode = any ( select ls.nsectioncode from sectionusers su,labsection ls where su.nusercode ="
					+ userInfo.getNusercode() + " and ls.nlabsectioncode=su.nlabsectioncode and su.nsitecode="
					+ userInfo.getNtranssitecode() + " and su.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ls.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by ls.nsectioncode)";
		}
		getTest = "select tgt.stestsynonym,tgt.ntestcode from testgrouptest tgt, registrationtest rt "
				+ " where rt.ntestgrouptestcode = tgt.ntestgrouptestcode   " + "and rt.ntransactiontestcode = any (  "
				+ " select rth.ntransactiontestcode from registrationtesthistory rth,registrationarno rar,registration r  "
				+ " where " + "rth.ntesthistorycode = any (  "
				+ " select max(ntesthistorycode) from registrationtesthistory where nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  "
				+ "group by ntransactiontestcode  ) and r.npreregno = rar.npreregno and r.nregtypecode ="
				+ inputMap.get("nregtypecode") + "  and r.nregsubtypecode = " + inputMap.get("nregsubtypecode") + " "
				+ " and r.ndesigntemplatemappingcode =" + inputMap.get("ndesigntemplatemappingcode") + " "
				+ TransactionStatus + " "
				+ " and rth.ntransactiontestcode not in(select max(ntransactiontestcode) from registrationtesthistory "
				+ "	where ntransactionstatus=" + Enumeration.TransactionStatus.CANCELED.gettransactionstatus()
				+ "  and nformcode =" + Enumeration.QualisForms.SAMPLEREGISTRATION.getqualisforms()
				+ " group by ntransactiontestcode)" + " and rth.dtransactiondate between '" + inputMap.get("fromdate")
				+ "' and '" + inputMap.get("todate") + "' and rar.npreregno = rth.npreregno and rar.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rar.napprovalversioncode = "
				+ inputMap.get("napprovalconfigversioncode") + ")" + sectioncodes + "  "
				+ " group by tgt.stestsynonym,tgt.ntestcode ";

		List<TestGroupTest> lstTest = jdbcTemplate.query(getTest, new TestGroupTest());
		if (!lstTest.isEmpty()) {
			if (lstTest.size() > 1) {
				obj.put("ntestcode", 0);
				obj.put("stestsynonym",
						commonFunction.getMultilingualMessage("IDS_ALL", userInfo.getSlanguagefilename()));
				lstobj.add(obj);
				lstobj.addAll(lstTest);
				map.put("Test", lstobj);
				map.put("defaultTestValue", lstobj.get(0));
				map.put("ntestcode", "0");

			} else {
				map.put("Test", lstTest);
				map.put("defaultTestValue", lstTest.get(0));
				map.put("ntestcode", String.valueOf(lstTest.get(0).getNtestcode()));
			}
			inputMap.put("activeSampleTab", "IDS_SAMPLEATTACHMENTS");
			inputMap.put("activeSubSampleTab", "IDS_SUBSAMPLEATTACHMENTS");
			inputMap.put("activeTestTab", "IDS_TESTATTACHMENTS");
			inputMap.put("ntestcode", map.get("ntestcode"));

			if ((int) inputMap.get("nflag") == 1) {
				map.putAll((Map<String, Object>) getJobAllocationDetails(inputMap, userInfo).getBody());
			} else if ((int) inputMap.get("nflag") == 7 || (int) inputMap.get("nflag") == 5) {

				String getSectionCode = "select rt.nsectioncode from testgrouptest tgt, registrationtest rt "
						+ "where rt.ntestgrouptestcode = tgt.ntestgrouptestcode   "
						+ "and rt.ntransactiontestcode = any (  "
						+ "select rth.ntransactiontestcode from registrationtesthistory rth,registrationarno rar,registration r  "
						+ "where rth.ntesthistorycode = any (  "
						+ " select max(ntesthistorycode) from registrationtesthistory where nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  "
						+ " group by ntransactiontestcode  ) and r.npreregno = rar.npreregno and r.nregtypecode ="
						+ inputMap.get("nregtypecode") + "  and r.nregsubtypecode = " + inputMap.get("nregsubtypecode")
						+ " " + " and r.ndesigntemplatemappingcode =" + inputMap.get("ndesigntemplatemappingcode") + " "
						+ TransactionStatus + " and rth.dtransactiondate between '" + inputMap.get("fromdate")
						+ "' and '" + inputMap.get("todate") + "' and rar.npreregno = rth.npreregno and rar.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and rar.napprovalversioncode = " + inputMap.get("napprovalconfigversioncode") + "  ) "
						+ sectioncodes + "  " + " group by rt.nsectioncode ";

				List<Section> listSection = jdbcTemplate.query(getSectionCode, new Section());

				final String sectioncodevalue = listSection.stream().map(x -> String.valueOf(x.getNsectioncode()))
						.collect(Collectors.joining(","));

				String getSection = "select s.nsectioncode,s.ssectionname from section s join labsection ls on ls.nsectioncode=s.nsectioncode join sectionusers su on su.nlabsectioncode=ls.nlabsectioncode "
						+ " where su.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ls.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and 	s.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and s.nsectioncode in (" + sectioncodevalue + ") and su.nusercode= "
						+ userInfo.getNusercode() + " and su.nsitecode=" + userInfo.getNtranssitecode()
						+ " group by s.nsectioncode,s.ssectionname  ";

				lstSection = jdbcTemplate.query(getSection, new Section());

				map.put("UserSection", lstSection);
				map.put("defaultUserSectionValue", lstSection.get(0));

			}
		} else {
			if ((int) inputMap.get("nflag") == 7) {
				map.put("UserSection", lstSection);
				map.put("defaultUserSectionValue", lstSection);
			}
			map.put("Test", lstTest);
			map.put("defaultTestValue", lstTest);
			inputMap.put("activeSampleTab", "IDS_SAMPLEATTACHMENTS");
			inputMap.put("activeSubSampleTab", "IDS_SUBSAMPLEATTACHMENTS");
			inputMap.put("activeTestTab", "IDS_TESTATTACHMENTS");
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getDesignTemplateByApprovalConfigVersion(Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception {

		Map<String, Object> map = new HashMap<String, Object>();

		final String str = "select dm.ndesigntemplatemappingcode,rt.jsondata,CONCAT(rt.sregtemplatename,'(',cast(dm.nversionno as character varying),')') sregtemplatename "
				+ " from designtemplatemapping dm, reactregistrationtemplate rt,approvalconfigversion acv  "
				+ " where  acv.ndesigntemplatemappingcode=dm.ndesigntemplatemappingcode  and  rt.nreactregtemplatecode=dm.nreactregtemplatecode "
				+ " and  dm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  acv.napproveconfversioncode="
				+ inputMap.get("napprovalconfigversioncode") + " ";

		List<ReactRegistrationTemplate> lstReactRegistrationTemplate = jdbcTemplate.query(str,
				new ReactRegistrationTemplate());

		map.put("DynamicDesignMapping", lstReactRegistrationTemplate);
		map.put("realDesignTemplateMappingValue", lstReactRegistrationTemplate.get(0));
		map.put("defaultDesignTemplateMappingValue", lstReactRegistrationTemplate.get(0));
		map.put("DesignTemplateMappingValue", lstReactRegistrationTemplate.get(0));
		inputMap.put("ndesigntemplatemappingcode", lstReactRegistrationTemplate.get(0).getNdesigntemplatemappingcode());

		map.putAll((Map<String, Object>) getTestComboBySection(inputMap, userInfo).getBody());

		return new ResponseEntity<>(map, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> getJobAllocationDetails(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final ObjectMapper mapper = new ObjectMapper();
		final Map<String, Object> returnMap = new HashMap<String, Object>();

		String fromDate = "";
		String toDate = "";
		if (inputMap.containsKey("fromdate")) {

			final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
			final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());

			final String fromDateUI = LocalDateTime.parse((String) inputMap.get("fromdate"), dbPattern)
					.format(uiPattern);
			final String toDateUI = LocalDateTime.parse((String) inputMap.get("todate"), dbPattern).format(uiPattern);

			returnMap.put("fromDate", fromDateUI);
			returnMap.put("toDate", toDateUI);

			fromDate = dateUtilityFunction.instantDateToString(
					dateUtilityFunction.convertStringDateToUTC((String) inputMap.get("fromdate"), userInfo, true));
			toDate = dateUtilityFunction.instantDateToString(
					dateUtilityFunction.convertStringDateToUTC((String) inputMap.get("todate"), userInfo, true));

			inputMap.put("fromdate", fromDate);
			inputMap.put("todate", toDate);
		}
		if (!inputMap.containsKey("nflag")) {
			inputMap.put("nflag", 1);
		}
		if ((int) inputMap.get("nflag") == 1)
			returnMap.putAll(getJobAllocationSamples(inputMap, userInfo));
		else if ((int) inputMap.get("nflag") == 2)
			returnMap.putAll(getJobAllocationSubSamples(inputMap, userInfo));
		else if ((int) inputMap.get("nflag") == 3)
			returnMap.putAll(getJobAllocationTest(inputMap, userInfo));

		List<Map<String, Object>> lstmapObjectsamples = mapper.convertValue(returnMap.get("JA_SAMPLE"),
				new TypeReference<List<Map<String, Object>>>() {
				});

		if (lstmapObjectsamples != null && !lstmapObjectsamples.isEmpty()) {
			returnMap.put("JASelectedSample", Arrays.asList(lstmapObjectsamples.get(lstmapObjectsamples.size() - 1)));

		} else {
			if (lstmapObjectsamples != null) {
				returnMap.put("JASelectedSample", lstmapObjectsamples);
			}
			returnMap.put("RegistrationAttachment", lstmapObjectsamples);
			returnMap.put("RegistrationComment", lstmapObjectsamples);
		}
		List<Map<String, Object>> lstmapObjectsubsample = mapper.convertValue(returnMap.get("JA_SUBSAMPLE"),
				new TypeReference<List<Map<String, Object>>>() {
				});
		if ((boolean) inputMap.get("nneedsubsample")) {
			if (lstmapObjectsubsample != null && !lstmapObjectsubsample.isEmpty()) {
				returnMap.put("JASelectedSubSample",
						Arrays.asList(lstmapObjectsubsample.get(lstmapObjectsubsample.size() - 1)));
			}
		} else {
			returnMap.put("JASelectedSubSample", lstmapObjectsubsample);
		}
		List<Map<String, Object>> lstmapObjecttest = mapper.convertValue(returnMap.get("JA_TEST"),
				new TypeReference<List<Map<String, Object>>>() {
				});
		if (lstmapObjecttest != null && !lstmapObjecttest.isEmpty()) {
			returnMap.put("JASelectedTest", Arrays.asList(lstmapObjecttest.get(lstmapObjecttest.size() - 1)));
		} else {
			returnMap.put("JASelectedTest", new ArrayList<>());
		}
		if (inputMap.containsKey("saveFilterSubmit") && (boolean) inputMap.get("saveFilterSubmit") == true) {
			projectDAOSupport.createFilterSubmit(inputMap, userInfo);
		}
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	public Map<String, Object> getJobAllocationSamples(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final Map<String, Object> returnMap = new HashMap<>();
		List<Map<String, Object>> sampleList = new ArrayList<>();
		String transCode = "";
		if (!inputMap.containsKey("ntransactionstatus")) {
			transCode = "0";
		} else if (inputMap.containsKey("ntransactionstatus")) {
			transCode = (String) inputMap.get("ntransactionstatus");
		}
		final short sampleTypeCode = Short.parseShort(String.valueOf(inputMap.get("nsampletypecode")));
		final short regTypeCode = Short.parseShort(String.valueOf(inputMap.get("nregtypecode")));
		final short regSubTypeCode = Short.parseShort(String.valueOf(inputMap.get("nregsubtypecode")));
		final short approveVersionCode = Short.parseShort(String.valueOf(inputMap.get("napprovalversioncode")));
		final short designTemplateMappingCode = Short
				.parseShort(String.valueOf(inputMap.get("ndesigntemplatemappingcode")));
		final String sectionCode = (String) inputMap.get("nsectioncode");
		String testCode = (String) inputMap.get("ntestcode");
		final Integer userCode = (Integer) userInfo.getNusercode();
		final short siteCode = userInfo.getNtranssitecode();
		final String languageTypeCode = userInfo.getSlanguagetypecode();
		final boolean subSampleNeed = (boolean) inputMap.get("nneedsubsample");

		String spreregno = "";
		String ssectioncode = "";
		String registrationsectioncode = "";
		String preRegisterNo = "0";

		if (inputMap.containsKey("ncheck") && (int) inputMap.get("ncheck") == 1) {
			preRegisterNo = (String) inputMap.get("npreregno");
		}
		final String getSampleQuery = "SELECT * from fn_joballocationsampleget('" + inputMap.get("fromdate") + "','"
				+ inputMap.get("todate") + "'," + "" + sampleTypeCode + "," + regTypeCode + "," + regSubTypeCode + ","
				+ approveVersionCode + "," + designTemplateMappingCode + ", " + "'" + preRegisterNo + "','"
				+ sectionCode + "','" + testCode + "', '" + transCode + "'," + userCode + "," + siteCode + ",'"
				+ languageTypeCode + "');";
		LOGGER.info(getSampleQuery);
		String sample = jdbcTemplate.queryForObject(getSampleQuery, String.class);
		if (sample != null) {
			sampleList = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(sample,
					userInfo, true, (int) inputMap.get("ndesigntemplatemappingcode"), "sample");
		}

		returnMap.put("JA_SAMPLE", sampleList);
		if (!sampleList.isEmpty()) {
			if (!inputMap.containsKey("npreregno")) {
				spreregno = String.valueOf(sampleList.get(sampleList.size() - 1).get("npreregno"));
			} else if (inputMap.containsKey("npreregno")) {
				spreregno = (String) inputMap.get("npreregno");
			}

			returnMap.put("DynamicDesign", projectDAOSupport
					.getTemplateDesign((int) inputMap.get("ndesigntemplatemappingcode"), userInfo.getNformcode()));
			returnMap.putAll(getActiveSampleTabData(spreregno, (String) inputMap.get("activeSampleTab"), userInfo));
			returnMap.put("activeSampleTab", inputMap.get("activeSampleTab"));
			ssectioncode = String.valueOf(sampleList.get(sampleList.size() - 1).get("nsectioncode"));

			if (subSampleNeed) {
				registrationsectioncode = String
						.valueOf(sampleList.get(sampleList.size() - 1).get("nregistrationsectioncode"));
			} else {
				registrationsectioncode = sampleList.stream()
						.map(x -> String.valueOf(x.get("nregistrationsectioncode"))).collect(Collectors.joining(","));
			}

			inputMap.put("npreregno", spreregno);
			inputMap.put("nregistrationsectioncode", registrationsectioncode);
			inputMap.put("nsectioncode", ssectioncode);
			returnMap.putAll(getJobAllocationSubSamples(inputMap, userInfo));
			if (inputMap.containsKey("ntype") && (int) inputMap.get("ntype") == 2) {
				returnMap.put("JASelectedSample", sampleList);
			} else {
				returnMap.put("JASelectedSample", Arrays.asList(sampleList.get(sampleList.size() - 1)));
			}
		} else {
			returnMap.put("JA_SUBSAMPLE", sampleList);
			returnMap.put("JA_TEST", sampleList);
		}

		return returnMap;

	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getJobAllocationSubSamples(Map<String, Object> inputMap, UserInfo userInfo)
			throws IllegalArgumentException, Exception {
		final Map<String, Object> returnMap = new HashMap<>();
		List<Map<String, Object>> subSampleList = new ArrayList<>();
		final String npreregno = (String) inputMap.get("npreregno");
		final String ssectioncode = (String) inputMap.get("nsectioncode");
		final String ntestcode = (String) inputMap.get("ntestcode");
		final String nregistrationsectioncode = (String) inputMap.get("nregistrationsectioncode");
		final boolean subSampleNeed = (boolean) inputMap.get("nneedsubsample");
		final short siteCode = userInfo.getNtranssitecode();
		String stransactionsampleno = "";
		String Sectioncode = "";

		String transCode = "";
		if (!inputMap.containsKey("ntransactionstatus")) {
			transCode = "0";
		} else if (inputMap.containsKey("ntransactionstatus")) {
			transCode = (String) inputMap.get("ntransactionstatus");
		}

		final String getSampleQuery = "SELECT * from fn_joballocationsubsampleget('" + npreregno + "','" + ssectioncode
				+ "','" + ntestcode + "','" + nregistrationsectioncode + "','" + transCode + "'," + siteCode + ",'"
				+ userInfo.getSlanguagetypecode() + "');";
		LOGGER.info(getSampleQuery);
		String sample = jdbcTemplate.queryForObject(getSampleQuery, String.class);
		if (sample != null) {
			subSampleList = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(
					sample, userInfo, true, (int) inputMap.get("ndesigntemplatemappingcode"), "subsample");
		}
		returnMap.put("JA_SUBSAMPLE", subSampleList);
		if (!subSampleList.isEmpty()) {
			if (subSampleNeed) {
				if (inputMap.containsKey("checkBoxOperation") && (int) inputMap.get("checkBoxOperation") == 3) {
					if (!inputMap.containsKey("ntransactionsamplecode")) {
						stransactionsampleno = String
								.valueOf(subSampleList.get(subSampleList.size() - 1).get("ntransactionsamplecode"));
					} else {
						stransactionsampleno = (String) inputMap.get("ntransactionsamplecode");
					}
					Sectioncode = String.valueOf(subSampleList.get(subSampleList.size() - 1).get("nsectioncode"));
					inputMap.put("ntransactionsamplecode", stransactionsampleno);
					inputMap.put("nsectioncode", Sectioncode);
					returnMap.put("JASelectedSubSample", Arrays.asList(subSampleList.get(subSampleList.size() - 1)));
					returnMap.putAll(getJobAllocationTest(inputMap, userInfo));
				}
				returnMap.putAll(getActiveSubSampleTabData(stransactionsampleno,
						(String) inputMap.get("activeSubSampleTab"), userInfo));
				returnMap.put("activeSubSampleTab", inputMap.get("activeSubSampleTab"));
			} else {
				returnMap.put("JASelectedSubSample", subSampleList);
				stransactionsampleno = subSampleList.stream().map(x -> String.valueOf(x.get("ntransactionsamplecode")))
						.collect(Collectors.joining(","));
				inputMap.put("ntransactionsamplecode", stransactionsampleno);
				returnMap.putAll(getJobAllocationTest(inputMap, userInfo));
			}

		} else {
			returnMap.put("JASelectedSubSample", subSampleList);
			if (subSampleNeed
					|| (inputMap.containsKey("checkBoxOperation") && (int) inputMap.get("checkBoxOperation") == 3)) {
				returnMap.put("JA_TEST", subSampleList);
				returnMap.put("JASelectedTest", subSampleList);
				returnMap.put("activeSampleTab", "IDS_SAMPLEATTACHMENTS");
				returnMap.put("activeTestTab", "IDS_TESTATTACHMENTS");
			}
		}
		return returnMap;
	}

	public Map<String, Object> getJobAllocationTest(Map<String, Object> inputMap, UserInfo userInfo)
			throws IllegalArgumentException, Exception {
		final Map<String, Object> returnMap = new HashMap<>();
		List<Map<String, Object>> testList = new ArrayList<>();
		String ntestcode = "";
		if (!inputMap.containsKey("ntestcode")) {
			inputMap.put("ntestcode", ntestcode);
		}

		final String npreregno = (String) inputMap.get("npreregno");
		final String ntransactionsamplecode = (String) inputMap.get("ntransactionsamplecode");
		final String nsectioncode = (String) inputMap.get("nsectioncode");
		final Short siteCode = userInfo.getNtranssitecode();
		final String testCode = (String) inputMap.get("ntestcode");

		String transactionTestCode = "0";
		String transCode = "";
		if (!inputMap.containsKey("ntransactionstatus")) {
			transCode = "0";
		} else if (inputMap.containsKey("ntransactionstatus")) {
			transCode = (String) inputMap.get("ntransactionstatus");
		}

		if (inputMap.containsKey("ntransactiontestcode")) {
			transactionTestCode = (String) inputMap.get("ntransactiontestcode");
		}

		final String getSampleQuery = "SELECT * from fn_joballocationtestget('" + npreregno + "', '"
				+ ntransactionsamplecode + "','" + transactionTestCode + "','" + nsectioncode + "','" + testCode + "','"
				+ transCode + "'," + siteCode + ",'" + userInfo.getSlanguagetypecode() + "')";
		LOGGER.info(getSampleQuery);

		String sample = jdbcTemplate.queryForObject(getSampleQuery, String.class);

		if (sample != null) {
			testList = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(sample,
					userInfo, true, (int) inputMap.get("ndesigntemplatemappingcode"), "test");

			String ntransactiontestcode = "";
			if (inputMap.containsKey("ntype") && (int) inputMap.get("ntype") == 1) {
				ntransactiontestcode = (String) inputMap.get("ntransactiontestcode");
			} else {
				ntransactiontestcode = (String) testList.get(testList.size() - 1).get("ntransactiontestcode")
						.toString();
			}

			inputMap.put("ntransactiontestcode", ntransactiontestcode);
			returnMap.putAll(getTestView(inputMap, userInfo));
			returnMap.putAll(
					getActiveTestTabData(ntransactiontestcode, (String) inputMap.get("activeTestTab"), userInfo));
			returnMap.put("activeTestTab", (String) inputMap.get("activeTestTab"));
		}
		returnMap.put("JA_TEST", testList);

		if (!testList.isEmpty()) {

			if (inputMap.containsKey("ntype") && (int) inputMap.get("ntype") == 1) {
				returnMap.put("JASelectedTest", testList);
			} else {
				returnMap.put("JASelectedTest", Arrays.asList(testList.get(testList.size() - 1)));

			}
			returnMap.put("activeSampleTab", "IDS_SAMPLEATTACHMENTS");
			returnMap.put("activeTestTab", "IDS_TESTATTACHMENTS");
		} else {
			returnMap.put("JASelectedTest", testList);
			returnMap.put("activeSampleTab", "IDS_SAMPLEATTACHMENTS");
			returnMap.put("activeTestTab", "IDS_TESTATTACHMENTS");
		}

		LOGGER.info("Job Allocation  Initial Get End");

		return returnMap;

	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getJobAllocationSubSampleDetails(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> objMap = new HashMap<String, Object>();
		List<Map<String, Object>> subSampleList = new ArrayList<>();
		String npreregno = (String) inputMap.get("npreregno");
		String ssectioncode = (String) inputMap.get("nsectioncode");
		String ntestcode = (String) inputMap.get("ntestcode");
		String nregistrationsectioncode = (String) inputMap.get("nregistrationsectioncode");
		boolean subSample = (boolean) inputMap.get("nneedsubsample");
		final short siteCode = userInfo.getNtranssitecode();
		String stransactionsampleno = "";
		String Sectioncode = "";
		String transCode = "";
		if (!inputMap.containsKey("ntransactionstatus")) {
			transCode = "0";
		} else if (inputMap.containsKey("ntransactionstatus")) {
			transCode = (String) inputMap.get("ntransactionstatus");
		}
		if (inputMap.containsKey("samplesearch")) {
			if ((int) inputMap.get("samplesearch") == 1) {
				String strQuery = "select npreregno,nsectioncode from registrationsection where nregistrationsectioncode in("
						+ nregistrationsectioncode + ")";
				List<RegistrationSection> lst1 = jdbcTemplate.query(strQuery, new RegistrationSection());
				Integer PreRegisterNo = lst1.get(0).getNpreregno();
				npreregno = PreRegisterNo.toString();

				Integer Section = lst1.get(0).getNsectioncode();
				ssectioncode = Section.toString();
				inputMap.put("npreregno", npreregno);
			}
		}

		final String getSampleQuery = "SELECT * from fn_joballocationsubsampleget('" + npreregno + "','" + ssectioncode
				+ "','" + ntestcode + "','" + nregistrationsectioncode + "','" + transCode + "'," + siteCode + ",'"
				+ userInfo.getSlanguagetypecode() + "');";
		LOGGER.info(getSampleQuery);
		String sample = jdbcTemplate.queryForObject(getSampleQuery, String.class);

		if (sample != null) {
			subSampleList = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(
					sample, userInfo, true, (int) inputMap.get("ndesigntemplatemappingcode"), "subsample");
		}
		objMap.put("JA_SUBSAMPLE", subSampleList);
		objMap.putAll(getActiveSampleTabData(npreregno, (String) inputMap.get("activeSampleTab"), userInfo));
		objMap.put("activeSampleTab", inputMap.get("activeSampleTab"));
		if (!subSampleList.isEmpty()) {
			if (subSample) {
				if (inputMap.containsKey("checkBoxOperation") && (int) inputMap.get("checkBoxOperation") == 3
						|| (int) inputMap.get("checkBoxOperation") == 5
						|| (int) inputMap.get("checkBoxOperation") == 7) {
					if (!inputMap.containsKey("ntransactionsamplecode")) {
						stransactionsampleno = String
								.valueOf(subSampleList.get(subSampleList.size() - 1).get("ntransactionsamplecode"));
					} else {
						stransactionsampleno = (String) inputMap.get("ntransactionsamplecode");
					}
					Sectioncode = String.valueOf(subSampleList.get(subSampleList.size() - 1).get("nsectioncode"));
					inputMap.put("ntransactionsamplecode", stransactionsampleno);
					inputMap.put("nsectioncode", Sectioncode);
					objMap.put("JASelectedSubSample", Arrays.asList(subSampleList.get(subSampleList.size() - 1)));
					objMap.putAll(getJobAllocationTest(inputMap, userInfo));
					objMap.putAll(getActiveSubSampleTabData(stransactionsampleno,
							(String) inputMap.get("activeSubSampleTab"), userInfo));
					objMap.put("activeSubSampleTab", inputMap.get("activeSubSampleTab"));
				}
			} else {
				objMap.put("JASelectedSubSample", subSampleList);
				stransactionsampleno = subSampleList.stream().map(x -> String.valueOf(x.get("ntransactionsamplecode")))
						.collect(Collectors.joining(","));
				inputMap.put("ntransactionsamplecode", stransactionsampleno);
				objMap.putAll(getJobAllocationTest(inputMap, userInfo));
			}

		} else {
			objMap.put("JASelectedSubSample", subSampleList);
			if (!subSample || (int) inputMap.get("checkBoxOperation") == 3) {
				objMap.put("JASelectedTest", subSampleList);
				objMap.put("JA_TEST", subSampleList);
				objMap.put("activeSubSampleTab", "IDS_SUBSAMPLEATTACHMENTS");
				objMap.put("activeTestTab", "IDS_TESTATTACHMENTS");
			}
		}
		return new ResponseEntity<>(objMap, HttpStatus.OK);

	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getJobAllocationTestDetails(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final Map<String, Object> returnMap = new HashMap<>();
		List<Map<String, Object>> testList = new ArrayList<>();
		final String npreregno = (String) inputMap.get("npreregno");
		final String ntransactionsamplecode = (String) inputMap.get("ntransactionsamplecode");
		final String nsectioncode = (String) inputMap.get("nsectioncode");
		final String ntestcode = (String) inputMap.get("ntestcode");
		final Short siteCode = userInfo.getNtranssitecode();

		String transCode = "";
		String transactionTestCode = "0";
		if (!inputMap.containsKey("ntransactionstatus")) {
			transCode = "0";
		} else if (inputMap.containsKey("ntransactionstatus")) {
			transCode = (String) inputMap.get("ntransactionstatus");
		}
		if (inputMap.containsKey("ntransactiontestcode")) {
			transactionTestCode = (String) inputMap.get("ntransactiontestcode");
		}

		final String getSampleQuery = "SELECT * from fn_joballocationtestget('" + npreregno + "', '"
				+ ntransactionsamplecode + "','" + transactionTestCode + "','" + nsectioncode + "','" + ntestcode
				+ "','" + transCode + "'," + siteCode + ",'" + userInfo.getSlanguagetypecode() + "')";
		LOGGER.info(getSampleQuery);

		String sample = jdbcTemplate.queryForObject(getSampleQuery, String.class);

		if (sample != null) {
			testList = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(sample,
					userInfo, true, (int) inputMap.get("ndesigntemplatemappingcode"), "test");
			String ntransactiontestcode = (String) testList.get(testList.size() - 1).get("ntransactiontestcode")
					.toString();
			inputMap.put("ntransactiontestcode", ntransactiontestcode);
			returnMap.putAll(getTestView(inputMap, userInfo));
			returnMap.putAll(
					getActiveTestTabData(ntransactiontestcode, (String) inputMap.get("activeTestTab"), userInfo));
			returnMap.put("activeTestTab", (String) inputMap.get("activeTestTab"));
			returnMap.putAll(getActiveSubSampleTabData(ntransactionsamplecode, (String) inputMap.get("activeTestTab"),
					userInfo));
		}

		returnMap.put("JA_TEST", testList);

		if (!testList.isEmpty()) {
			returnMap.put("JASelectedTest", Arrays.asList(testList.get(testList.size() - 1)));
		} else {
			returnMap.put("JASelectedTest", testList);
		}
		return new ResponseEntity<>(returnMap, HttpStatus.OK);

	}

	public List<RegistrationTestHistory> validateSampletStatus(String spreregno, String ssectioncode, int controlCode,
			UserInfo userInfo) throws Exception {

		String validateSamples = "select rth.ntesthistorycode,rth.ntransactionstatus,rth.ntransactiontestcode,rt.npreregno,rt.nsectioncode  "
				+ " from registrationtest rt,registrationtesthistory rth  "
				+ " where rt.ntransactiontestcode =rth.ntransactiontestcode and " + "rth.ntesthistorycode = any  "
				+ "(select max(ntesthistorycode) from registrationtesthistory where npreregno in (" + spreregno + ")  "
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " group by ntransactiontestcode) and rth.ntransactionstatus = any ( "
				+ " select ntransactionstatus from transactionvalidation tv,registration r where tv.nregtypecode=r.nregtypecode  "
				+ " and tv.nregsubtypecode=r.nregsubtypecode and r.npreregno =rth.npreregno and tv.nformcode = "
				+ userInfo.getNformcode() + " and tv.nsitecode = " + userInfo.getNmastersitecode() + " "
				+ " and tv.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tv.ncontrolcode = " + controlCode + ") and rt.npreregno in (" + spreregno
				+ ") and rt.nsectioncode in(" + ssectioncode + ")  and rth.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";

		return jdbcTemplate.query(validateSamples, new RegistrationTestHistory());
	}

	public List<RegistrationTestHistory> validateTestStatus(String spreregno, String stransactionsamplecode,
			String stransactiontestcode, int controlCode, UserInfo userInfo) throws Exception {

		String validateTests = "select rth.ntesthistorycode,rth.ntransactionstatus,rth.ntransactiontestcode,rt.npreregno,rt.ntransactionsamplecode  "
				+ " from registrationtest rt,registrationtesthistory rth  "
				+ " where rt.ntransactiontestcode =rth.ntransactiontestcode and " + "rth.ntesthistorycode = any  "
				+ " (select max(ntesthistorycode) from registrationtesthistory where npreregno in (" + spreregno
				+ ") and ntransactiontestcode in (" + stransactiontestcode + ") " + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " group by ntransactiontestcode) and rth.ntransactionstatus not in ( "
				+ " select ntransactionstatus from transactionvalidation tv,registration r where tv.nregtypecode=r.nregtypecode  "
				+ " and tv.nregsubtypecode=r.nregsubtypecode and r.npreregno =rth.npreregno and  " + "tv.nformcode = "
				+ userInfo.getNformcode() + " and tv.nsitecode = " + userInfo.getNmastersitecode() + " "
				+ " and tv.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tv.ncontrolcode = " + controlCode + " " + ") and rt.npreregno in (" + spreregno
				+ ") and rt.ntransactionsamplecode in(" + stransactionsamplecode + ") and rt.ntransactiontestcode in ("
				+ stransactiontestcode + ") and rth.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";

		return jdbcTemplate.query(validateTests, new RegistrationTestHistory());
	}

	public List<RegistrationTestHistory> validateSelectedTestStatus(String spreregno, String stransactionsamplecode,
			String stransactiontestcode, int controlCode, UserInfo userInfo, boolean sectionchange) throws Exception {
		String inuery = " in";

		if (sectionchange) {
			inuery = "not in";
		}

		String validateTests = "select rth.ntesthistorycode,rth.ntransactionstatus,rth.ntransactiontestcode,rt.npreregno,rt.ntransactionsamplecode,COALESCE(ts.jsondata -> 'stransdisplaystatus' ->> 'en-US', ts.jsondata -> 'stransdisplaystatus' ->> 'en-US') AS stransdisplaystatus"
				+ " from registrationtest rt,registrationtesthistory rth,transactionstatus ts "
				+ " where rt.ntransactiontestcode =rth.ntransactiontestcode and " + "rth.ntesthistorycode = any  "
				+ "(select max(ntesthistorycode) from registrationtesthistory where" + "  nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " group by ntransactiontestcode) and rth.ntransactionstatus  " + inuery + " ( "
				+ " select ntransactionstatus from transactionvalidation tv,registration r where tv.nregtypecode=r.nregtypecode  "
				+ " and tv.nregsubtypecode=r.nregsubtypecode  and  tv.nformcode = " + userInfo.getNformcode()
				+ " and tv.nsitecode = " + userInfo.getNmastersitecode() + " " + " and tv.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tv.ncontrolcode = " + controlCode
				+ ") and rt.npreregno in (" + spreregno + ") and rt.ntransactionsamplecode in(" + stransactionsamplecode
				+ ") and  rt.ntransactiontestcode in (" + stransactiontestcode + ") and rth.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.ntranscode=rth.ntransactionstatus and rt.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";

		return jdbcTemplate.query(validateTests, new RegistrationTestHistory());
	}

	private List<RegistrationTestHistory> validateTestCancelStatus(String spreregno, String stransactionsamplecode,
			String stransactiontestcode, int controlCode, UserInfo userInfo) throws Exception {

		String validateTests = "select rth.ntesthistorycode,rth.ntransactionstatus,rth.ntransactiontestcode,rt.npreregno,rt.ntransactionsamplecode  "
				+ " from registrationtest rt,registrationtesthistory rth  "
				+ " where rt.ntransactiontestcode =rth.ntransactiontestcode and " + "rth.ntesthistorycode = any  "
				+ " (select max(ntesthistorycode) from registrationtesthistory where npreregno in (" + spreregno
				+ " )and ntransactiontestcode in (" + stransactiontestcode + ") " + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " group by ntransactiontestcode) and rth.ntransactionstatus =any ( "
				+ " select ntransactionstatus from transactionvalidation tv,registration r where tv.nregtypecode=r.nregtypecode  "
				+ " and tv.nregsubtypecode=r.nregsubtypecode and r.npreregno =rth.npreregno and  tv.nformcode = "
				+ userInfo.getNformcode() + " and tv.nsitecode = " + userInfo.getNmastersitecode() + " "
				+ " and tv.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tv.ncontrolcode = " + controlCode + " " + ") and rt.npreregno in (" + spreregno
				+ ") and rt.ntransactionsamplecode in(" + stransactionsamplecode + ") and rt.ntransactiontestcode in ("
				+ stransactiontestcode + ") and rth.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";

		return jdbcTemplate.query(validateTests, new RegistrationTestHistory());
	}

	@SuppressWarnings("unused")
	public Map<String, Object> insertSeqNoRegistration(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		String sQuery = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		sQuery = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		sQuery = " lock  table lockcancelreject " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		Map<String, Object> map = new HashMap<String, Object>();
		String spreregno = (String) inputMap.get("npreregno");
		String ssectioncode = (String) inputMap.get("nsectioncode");
		Integer controlCode = (int) inputMap.get("ncontrolcode");
		boolean subsampleneeded = (boolean) inputMap.get("nneedsubsample");
		Integer ntransactionstatus;
		Integer ntesthistoryseqcount;
		Integer nsectionhistoryseqcount;
		Integer ncustodyseqcount;
		Integer testhistorycount;
		Integer sectionhistorycount;
		Integer chaincustodycount;
		Integer joballocationcount;
		Integer njoballocationseqcount;
		List<RegistrationTestHistory> filteredSampleList = validateSampletStatus(spreregno, ssectioncode, controlCode,
				userInfo);
		if (!filteredSampleList.isEmpty()) {
			ntransactionstatus = (int) filteredSampleList.get(0).getNtransactionstatus();

			String testCountQuery = "select rt.ntransactiontestcode from registrationtesthistory rth  "
					+ " join registrationtest rt on rt.ntransactiontestcode = rth.ntransactiontestcode  "
					+ " and rt.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and rth.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and rth.ntesthistorycode =any( select max(ntesthistorycode) from registrationtesthistory  "
					+ " where nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and npreregno in (" + spreregno + ")  "
					+ " group by npreregno,ntransactionsamplecode,ntransactiontestcode) "
					+ " and rth.ntransactionstatus in(" + ntransactionstatus + ") and rt.npreregno in (" + spreregno
					+ ") and rt.nsectioncode in (" + ssectioncode + ")";
			List<RegistrationTestHistory> lstRegistrationTestHistory = (List<RegistrationTestHistory>) jdbcTemplate
					.query(testCountQuery, new RegistrationTestHistory());
			Integer testCount = lstRegistrationTestHistory.size();

			final String sScheduleSkip = scheduleSkip();

			if (sScheduleSkip.equals(String.valueOf(Enumeration.TransactionStatus.YES.gettransactionstatus()))
					&& !(boolean) inputMap.get("nneedmyjob")) {

				String jobAllocationSeqCount = "select nsequenceno from  seqnoregistration where stablename ='joballocation' ";
				njoballocationseqcount = jdbcTemplate.queryForObject(jobAllocationSeqCount, Integer.class);
				joballocationcount = njoballocationseqcount + testCount;

				String updateJobAllocationQuery = "update seqnoregistration set nsequenceno =" + joballocationcount
						+ " where stablename='joballocation'";
				jdbcTemplate.execute(updateJobAllocationQuery);

				map.put("njoballocationseqcount", njoballocationseqcount);
			}

			String sectionCountQuery = " select rt.nsectioncode from registrationtest rt,registrationtesthistory rth"
					+ " where rth.ntransactiontestcode =rt.ntransactiontestcode and rth.ntransactionsamplecode =rt.ntransactionsamplecode and rth.npreregno =rt.npreregno "
					+ " and rth.ntesthistorycode =any( select max(ntesthistorycode) from registrationtesthistory  where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and npreregno in (" + spreregno
					+ ") "
					+ " group by npreregno,ntransactionsamplecode,ntransactiontestcode)  and rth.ntransactionstatus in("
					+ ntransactionstatus + ") and rt.npreregno in (" + spreregno + ") and rt.nsectioncode in( "
					+ ssectioncode + ") and rt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " group by rt.npreregno,rt.nsectioncode";

			List<RegistrationSectionHistory> lstRegistrationSectionHistory = (List<RegistrationSectionHistory>) jdbcTemplate
					.query(sectionCountQuery, new RegistrationSectionHistory());
			Integer sectionCount = lstRegistrationSectionHistory.size();

			String custodyCountQuery = "";
			if (subsampleneeded) {
				custodyCountQuery = " select count(*) from registrationarno ra,registrationsamplearno rsa,registrationsection rs,registrationsectionhistory rsh,registrationsamplehistory rsh1 "
						+ " where ra.npreregno = rsa.npreregno and ra.npreregno=rs.npreregno and ra.npreregno = rsh.npreregno "
						+ " and rs.npreregno =rsh.npreregno  and rs.nsectioncode = rsh.nsectioncode "
						+ " and rsh1.ntransactionsamplecode =rsa.ntransactionsamplecode "
						+ " and rsh1.nsamplehistorycode =any(select max(nsamplehistorycode) from registrationsamplehistory group by npreregno,ntransactionsamplecode )"
						+ " and rsa.ntransactionsamplecode =any(select ntransactionsamplecode from registrationtest where npreregno in("
						+ spreregno + ") and nsectioncode in(" + ssectioncode
						+ ") group by npreregno,ntransactionsamplecode)"
						+ " and rsh.nsectionhistorycode =any(select max(nsectionhistorycode) from registrationsectionhistory group by npreregno,nsectioncode)"
						+ " and rsh1.ntransactionstatus not in ("
						+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","
						+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ") "
						+ " and rsh.ntransactionstatus =" + ntransactionstatus + "  and ra.npreregno in (" + spreregno
						+ ") and rs.nsectioncode in(" + ssectioncode + ") and ra.nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rsa.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rs.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rsh.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
			} else {
				custodyCountQuery = " select count(*) from registrationarno ra,registrationsection rs,registrationsectionhistory rsh, registrationhistory rh, section s "
						+ " where ra.npreregno=rs.npreregno and ra.npreregno=rsh.npreregno and ra.npreregno=rh.npreregno "
						+ " and rs.npreregno=rsh.npreregno and rs.nsectioncode=rsh.nsectioncode and rs.nsectioncode=s.nsectioncode "
						+ " and rh.nreghistorycode =any (select max(nreghistorycode) from registrationhistory where npreregno in ("
						+ spreregno + ") group by npreregno) "
						+ " and ra.npreregno =any(select npreregno from registrationtest where npreregno in("
						+ spreregno + ") and nsectioncode in(" + ssectioncode + ") group by npreregno) "
						+ " and rsh.nsectionhistorycode =any(select max(nsectionhistorycode) from registrationsectionhistory group by npreregno,nsectioncode) "
						+ " and rh.ntransactionstatus not in ("
						+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","
						+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ") "
						+ " and rsh.ntransactionstatus =" + ntransactionstatus + "  and ra.npreregno in (" + spreregno
						+ ") and rs.nsectioncode in(" + ssectioncode + ") and ra.nsitecode="
						+ userInfo.getNtranssitecode() + " and ra.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rs.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rsh.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rh.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
			}

			Integer custodyCount = jdbcTemplate.queryForObject(custodyCountQuery, Integer.class);

			final List<String> updateSeqnolst = Arrays.asList("registrationsectionhistory", "chaincustody",
					"registrationtesthistory");

			String str = "select nsequenceno from seqnoregistration where stablename ='registrationsectionhistory'";
			nsectionhistoryseqcount = jdbcTemplate.queryForObject(str, Integer.class);

			sectionhistorycount = nsectionhistoryseqcount + sectionCount;

			String str1 = "select nsequenceno from seqnoregistration where stablename ='chaincustody'";
			ncustodyseqcount = jdbcTemplate.queryForObject(str1, Integer.class);

			chaincustodycount = ncustodyseqcount + custodyCount;

			String str2 = "select nsequenceno from seqnoregistration where stablename ='registrationtesthistory'";
			ntesthistoryseqcount = jdbcTemplate.queryForObject(str2, Integer.class);

			testhistorycount = ntesthistoryseqcount + testCount;

			String updateSectionQuery = "update seqnoregistration set nsequenceno =" + sectionhistorycount
					+ " where stablename='registrationsectionhistory'";
			jdbcTemplate.execute(updateSectionQuery);

			String updateCustodyQuery = "update seqnoregistration set nsequenceno =" + chaincustodycount
					+ " where stablename='chaincustody'";
			jdbcTemplate.execute(updateCustodyQuery);

			String updateTestQuery = "update seqnoregistration set nsequenceno =" + testhistorycount
					+ " where stablename='registrationtesthistory'";
			jdbcTemplate.execute(updateTestQuery);

			map.put("TransCode", ntransactionstatus);
			map.put("nsectionhistoryseqcount", nsectionhistoryseqcount);
			map.put("ncustodyseqcount", ncustodyseqcount);
			map.put("ntesthistoryseqcount", ntesthistoryseqcount);
			map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
			// ALPD-3758
			map.put("sScheduleSkip", sScheduleSkip);

		} else {
			map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), commonFunction
					.getMultilingualMessage("IDS_SELECTREGISTEREDSAMPLES", userInfo.getSlanguagefilename()));
		}
		return map;
	}

	@SuppressWarnings({ "unchecked", "unused" })
	public Map<String, Object> CreateReceiveinLab(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		try {
			Map<String, Object> returnMap = new HashMap<String, Object>();
			String spreregno = (String) inputMap.get("npreregno");

			String ssectioncode = (String) inputMap.get("nsectioncode");
			boolean subsampleneeded = (boolean) inputMap.get("nneedsubsample");
			Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
			JSONObject actionType = new JSONObject();
			JSONObject jsonAuditOld = new JSONObject();
			JSONObject jsonAuditNew = new JSONObject();
			String insertCustodyQuery = "";

			List<Map<String, Object>> lstDataTest = testAuditGet(inputMap, userInfo);
			List<Map<String, Object>> sampleList = new ArrayList<>();

			Integer ntranscode;

			Integer ntransactionstatus = (Integer) inputMap.get("TransCode");
			Integer nsectionhistoryseqcount = (Integer) inputMap.get("nsectionhistoryseqcount");
			Integer ncustodyseqcount = (Integer) inputMap.get("ncustodyseqcount");
			Integer ntesthistoryseqcount = (Integer) inputMap.get("ntesthistoryseqcount");

			ntranscode = Enumeration.TransactionStatus.RECIEVED_IN_SECTION.gettransactionstatus();

			if (inputMap.get("sScheduleSkip")
					.equals(String.valueOf(Enumeration.TransactionStatus.YES.gettransactionstatus()))
					&& !(boolean) inputMap.get("nneedmyjob")) {

				String sinsertJobAllocationQuery = "insert into joballocation (njoballocationcode,npreregno,ntransactionsamplecode,ninstrumentnamecode,ntransactiontestcode,nsectioncode "
						+ ",nusercode,nuserperiodcode,ninstrumentcategorycode "
						+ ",ninstrumentcode,ninstrumentperiodcode, ntechniquecode "
						+ ",ntimezonecode,nsitecode, nstatus,ntestrescheduleno,nuserrolecode,jsondata, jsonuidata) "
						+ " select " + inputMap.get("njoballocationseqcount")
						+ "+rank() over(order by rt.ntransactionsamplecode,"
						+ "rt.ntransactiontestcode),rt.npreregno,rt.ntransactionsamplecode,-1 as ninstrumentnamecode,rt.ntransactiontestcode,tgt.nsectioncode,-1 as nusercode,"
						+ "-1 as nuserperiodcode,-1 as ninstrumentcategorycode,-1 as ninstrumentcode,-1 as ninstrumentperiodcode,-1 as ntechniquecode,-1 as ntimezonecode,"
						+ userInfo.getNtranssitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
						+ " 0 as ntestrescheduleno ,"
						+ "(select nuserrolecode from treetemplatetransactionrole ttr join approvalconfig ac"
						+ " on ac.napprovalconfigcode =ttr.napprovalconfigcode and ac.nregtypecode ="
						+ inputMap.get("nregtypecode") + " and ac.nregsubtypecode=" + inputMap.get("nregsubtypecode")
						+ " and ttr.ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
						+ " order by ttr.nlevelno desc LIMIT 1)   nuserrolecode,"
						+ " json_build_object('duserblockfromdate','"
						+ dateUtilityFunction.instantDateToStringWithFormat(
								dateUtilityFunction.getCurrentDateTime(userInfo), "yyyy-MM-dd HH:mm:ss")
						+ "','duserblocktodate','"
						+ dateUtilityFunction.instantDateToStringWithFormat(
								dateUtilityFunction.getCurrentDateTime(userInfo), "yyyy-MM-dd HH:mm:ss")
						+ "','suserblockfromtime','00:00','suserblocktotime','00:00','suserholdduration','0','dinstblockfromdate',NULL,"
						+ "'dinstblocktodate',NULL,'sinstblockfromtime',NULL,'sinstblocktotime',NULL,'sinstrumentholdduration',NULL,"
						+ "'scomments','','noffsetduserblockfromdate','"
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ "','noffsetduserblocktodate','"
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ "')::jsonb as jsondata, " + " json_build_object('duserblockfromdate','"
						+ dateUtilityFunction.instantDateToStringWithFormat(
								dateUtilityFunction.getCurrentDateTime(userInfo), "yyyy-MM-dd HH:mm:ss")
						+ "','duserblocktodate','"
						+ dateUtilityFunction.instantDateToStringWithFormat(
								dateUtilityFunction.getCurrentDateTime(userInfo), "yyyy-MM-dd HH:mm:ss")
						+ "','suserblockfromtime','00:00','suserblocktotime','00:00','suserholdduration','0','dinstblockfromdate',NULL,"
						+ "'dinstblocktodate',NULL,'sinstblockfromtime',NULL,'sinstblocktotime',NULL,'sinstrumentholdduration',NULL,"
						+ "'scomments','','noffsetduserblockfromdate','"
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ "','noffsetduserblocktodate','"
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ "')::jsonb  as jsonuidata"
						+ " from registrationtesthistory rth,registrationtest rt,testgrouptest tgt "
						+ " where rt.ntransactiontestcode=rth.ntransactiontestcode  "
						+ " and tgt.ntestgrouptestcode=rt.ntestgrouptestcode " + " and rt.nsectioncode=" + ssectioncode
						+ "" + " and rth.nsitecode=rt.nsitecode and rt.nsitecode=" + userInfo.getNtranssitecode()
						+ " and rth.ntesthistorycode in "
						+ " (select max(rth1.ntesthistorycode) as testhistorycode from "
						+ " registrationtesthistory rth1,registrationtest rt1,registration r "
						+ " where rth1.ntransactiontestcode = rt1.ntransactiontestcode "
						+ " and rth1.ntransactionstatus="
						+ Enumeration.TransactionStatus.REGISTERED.gettransactionstatus() + " and rth1.nstatus= "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  "
						+ " and rth1.npreregno  in (" + spreregno + ")"
						+ " and rth1.nsitecode=rt1.nsitecode and rt1.nsitecode=r.nsitecode and r.nsitecode="
						+ userInfo.getNtranssitecode()
						+ " and rth1.ntransactiontestcode not in (select max(ntransactiontestcode) as ntransactiontestcode"
						+ " from registrationtesthistory where  npreregno  in (" + spreregno + ")  " + " and nsitecode="
						+ userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntransactionstatus="
						+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus()
						+ " group by npreregno,ntransactiontestcode,ntransactionsamplecode) "
						+ " group by rth1.npreregno,rth1.ntransactionsamplecode,rth1.ntransactiontestcode)";

				jdbcTemplate.execute(sinsertJobAllocationQuery);

			}

			if (subsampleneeded) {
				insertCustodyQuery = " insert into chaincustody (nchaincustodycode,nformcode,ntablepkno,stablepkcolumnname,stablename,"
						+ " sitemno,ntransactionstatus,nusercode,nuserrolecode,dtransactiondate,ntztransactiondate,"
						+ " noffsetdtransactiondate,sremarks,nsitecode,nstatus)" + " (select (" + ncustodyseqcount
						+ ") + rank() over(order by rsa.npreregno,rsa.ntransactionsamplecode)    as nchaincustodycode ,"
						+ " " + Enumeration.QualisForms.JOBALLOCATION.getqualisforms()
						+ " as nformcode, rsa.ntransactionsamplecode as ntablepkno,'ntransactionsamplecode' as stablepkcolumnname,"
						+ " 'registrationsamplearno' as stablename,rsa.ssamplearno as sitemno,"
						+ Enumeration.TransactionStatus.RECIEVED_IN_SECTION.gettransactionstatus()
						+ " as ntransactionstatus ," + " " + userInfo.getNusercode() + " as nusercode, "
						+ userInfo.getNuserrole() + " as nuserrolecode, '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' as dtransactiondate," + " "
						+ userInfo.getNtimezonecode() + " as ntztransactiondate, "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ " as noffsetdtransactiondate," + " '"
						+ commonFunction.getMultilingualMessage(Enumeration.TransAction.RECIEVEDBY.getTransAction(),
								userInfo.getSlanguagefilename())
						+ "'||' '|| s.ssectionname as sremarks ," + " " + userInfo.getNtranssitecode()
						+ " as nsitecode," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " as nstatus "
						+ " from registrationarno ra,registrationsamplearno rsa ,registrationsection rs,registrationsectionhistory rsh,registrationsamplehistory rsh1,section s "
						+ " where ra.npreregno =rsa.npreregno " + " and ra.npreregno = rs.npreregno "
						+ " and ra.npreregno =rsh.npreregno" + " and rs.npreregno =rsh.npreregno "
						+ " and rs.nsectioncode =rsh.nsectioncode and rs.nsectioncode =s.nsectioncode "
						+ " and rsh1.ntransactionsamplecode = rsa.ntransactionsamplecode "
						+ " and rsh1.nsamplehistorycode =any(select max(nsamplehistorycode) from registrationsamplehistory group by npreregno,ntransactionsamplecode )"
						+ " and rsa.ntransactionsamplecode =any(select ntransactionsamplecode from registrationtest where npreregno in("
						+ spreregno + ") and nsectioncode in(" + ssectioncode
						+ ") group by npreregno,ntransactionsamplecode)"
						+ " and rsh.nsectionhistorycode = any(select max(nsectionhistorycode) from registrationsectionhistory group by npreregno,nsectioncode)"
						+ " and rsh1.ntransactionstatus not in ("
						+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","
						+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ") "
						+ " and rsh.ntransactionstatus =" + ntransactionstatus + " and ra.npreregno in (" + spreregno
						+ ") and rs.nsectioncode in(" + ssectioncode + ") and ra.nsitecode="
						+ userInfo.getNtranssitecode() + " and ra.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rsa.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rs.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rsh.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

			} else {
				insertCustodyQuery = " Insert into chaincustody (nchaincustodycode,nformcode,ntablepkno,stablepkcolumnname,stablename,"
						+ " sitemno,ntransactionstatus,nusercode,nuserrolecode,dtransactiondate,ntztransactiondate,"
						+ " noffsetdtransactiondate,sremarks,nsitecode,nstatus)" + " (select (" + ncustodyseqcount
						+ ") + rank() over(order by ra.npreregno) as nchaincustodycode ," + " "
						+ Enumeration.QualisForms.JOBALLOCATION.getqualisforms()
						+ " as nformcode, ra.npreregno as ntablepkno,'npreregno' as stablepkcolumnname,"
						+ " 'registrationarno' as stablename,ra.sarno as sitemno,"
						+ Enumeration.TransactionStatus.RECIEVED_IN_SECTION.gettransactionstatus()
						+ " as ntransactionstatus , " + userInfo.getNusercode() + " as nusercode, "
						+ userInfo.getNuserrole() + " as nuserrolecode, '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' as dtransactiondate," + " "
						+ userInfo.getNtimezonecode() + " as ntztransactiondate, "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ " as noffsetdtransactiondate," + " '"
						+ commonFunction.getMultilingualMessage(Enumeration.TransAction.RECIEVEDBY.getTransAction(),
								userInfo.getSlanguagefilename())
						+ "'||' '|| s.ssectionname as sremarks , " + userInfo.getNtranssitecode() + " as nsitecode,"
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " as nstatus "
						+ " from  registrationarno ra,registrationsection rs,registrationsectionhistory rsh, registrationhistory rh, section s "
						+ " where ra.npreregno=rs.npreregno and ra.npreregno=rsh.npreregno and ra.npreregno=rh.npreregno "
						+ " and rs.npreregno=rsh.npreregno and rs.nsectioncode=rsh.nsectioncode and rs.nsectioncode=s.nsectioncode "
						+ " and rh.nreghistorycode = any (select max(nreghistorycode) from registrationhistory where npreregno in ("
						+ spreregno + ") group by npreregno) "
						+ " and ra.npreregno =any(select npreregno from registrationtest where npreregno in("
						+ spreregno + ") and nsectioncode in(" + ssectioncode + ") group by npreregno)	"
						+ " and rsh.nsectionhistorycode =any(select max(nsectionhistorycode) from registrationsectionhistory  group by npreregno,nsectioncode) "
						+ " and rh.ntransactionstatus not in ("
						+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","
						+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ")  "
						+ " and rsh.ntransactionstatus =" + ntransactionstatus + "  and ra.npreregno in (" + spreregno
						+ ") and rs.nsectioncode in(" + ssectioncode + ") and ra.nsitecode="
						+ userInfo.getNtranssitecode() + " and ra.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rs.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rsh.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rh.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			}
			jdbcTemplate.execute(insertCustodyQuery);

			String insertSectionQuery = "Insert into registrationsectionhistory (nsectionhistorycode,npreregno,nsectioncode,ntransactionstatus,nsitecode,nstatus)"
					+ " (select (" + nsectionhistoryseqcount
					+ ")+rank() over (order by rt.npreregno,rt.nsectioncode) as nsectionhistorycode,rt.npreregno,rt.nsectioncode,"
					+ Enumeration.TransactionStatus.RECIEVED_IN_SECTION.gettransactionstatus()
					+ " as ntransactionstatus," + userInfo.getNtranssitecode() + " as nsitecode,"
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " as nstatus "
					+ " from registrationtest rt join  registrationtesthistory rth on rt.ntransactiontestcode =rth.ntransactiontestcode "
					+ " and  rt.npreregno in (" + spreregno + ") and rt.nsectioncode in( " + ssectioncode + ") "
					+ " and rth.ntesthistorycode = any (select max(ntesthistorycode) from registrationtesthistory where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and npreregno in (" + spreregno
					+ ") " + "group by npreregno,ntransactionsamplecode,ntransactiontestcode) "
					+ " and rth.ntransactionstatus in (" + ntransactionstatus + ") and rt.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " group by rt.npreregno,rt.nsectioncode)";
			jdbcTemplate.execute(insertSectionQuery);

			String insertTestQuery = "Insert into registrationtesthistory (ntesthistorycode,nformcode,npreregno,ntransactionsamplecode,ntransactiontestcode,nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode, "
					+ "nsampleapprovalhistorycode,ntransactionstatus,dtransactiondate,noffsetdtransactiondate,ntransdatetimezonecode,scomments,nsitecode,nstatus)"
					+ "((select (" + ntesthistoryseqcount + ")+Rank()over(order by ntesthistorycode) ntesthistorycode,"
					+ userInfo.getNformcode()
					+ " nformcode,rt.npreregno,rt.ntransactionsamplecode,rt.ntransactiontestcode," + ""
					+ userInfo.getNusercode() + " nusercode, " + userInfo.getNuserrole() + " nuserrolecode,"
					+ userInfo.getNdeputyusercode() + " ndeputyusercode," + userInfo.getNdeputyuserrole()
					+ " ndeputyuserrolecode,-1 nsampleapprovalhistorycode," + ntranscode + " ntransactionstatus, "
					+ " '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "' dtransactiondate,"
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " noffsetdtransactiondate," + userInfo.getNtimezonecode() + " ntransdatetimezonecode,  " + "  N'"
					+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "' scomments,"
					+ userInfo.getNtranssitecode() + " as nsitecode,"
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus   "
					+ " from registrationtesthistory rth  join registrationtest rt on rt.ntransactiontestcode = rth.ntransactiontestcode   "
					+ " and rt.npreregno in (" + spreregno + ") and rt.nsectioncode in (" + ssectioncode
					+ ") and rt.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and rth.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  "
					+ " and rth.ntesthistorycode =any( select max(ntesthistorycode) from registrationtesthistory   "
					+ " where nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and npreregno in(" + spreregno
					+ ") group by npreregno,ntransactionsamplecode,ntransactiontestcode) and rth.ntransactionstatus in("
					+ ntransactionstatus + ")))";
			jdbcTemplate.execute(insertTestQuery);

			String sQuery = " select json_agg(a.jsonuidata)::jsonb as jsonuidata from  (select  r.jsonuidata ||"
					+ " json_build_object('sarno',rarno.sarno)::jsonb||"
					+ " json_build_object('stransdisplaystatus', coalesce(ts.jsondata->'stransdisplaystatus'->>'"
					+ userInfo.getSlanguagetypecode() + "',"
					+ " ts.jsondata->'stransdisplaystatus'->>'en-US'))::jsonb||  "
					+ " json_build_object('nsectioncode',rs.nsectioncode)::jsonb|| "
					+ " json_build_object('ssectionname',s.ssectionname)::jsonb as jsonuidata"
					+ " from registrationsection rs,registration r,section s,registrationarno rarno,registrationsectionhistory rsh,"
					+ " transactionstatus ts where r.npreregno=rs.npreregno and s.nsectioncode=rs.nsectioncode and  ts.ntranscode=rsh.ntransactionstatus "
					+ " and rsh.nsectionhistorycode=any(select max(nsectionhistorycode) from registrationsectionhistory"
					+ " where nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " group by nsectioncode)" + " and rsh.nsectioncode=rs.nsectioncode"
					+ " and rsh.npreregno=rs.npreregno " + " and rarno.npreregno=r.npreregno and rs.npreregno in("
					+ spreregno + ") and rs.nsectioncode in(" + ssectioncode + ")" + " and r.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rsh.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rarno.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rs.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")a";

			String sampleListString = jdbcTemplate.queryForObject(sQuery, String.class);

			if (sampleListString != null) {
				sampleList = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(
						sampleListString, userInfo, true, (int) inputMap.get("ndesigntemplatemappingcode"), "sample");
			}
			returnMap.put("JA_SAMPLE", sampleList);

			returnMap = (Map<String, Object>) getJobAllocationSamples(inputMap, userInfo);
			jsonAuditNew.put("registrationsection", sampleList);
			auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
			auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
			auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
			actionType.put("registrationsection", "IDS_RECEIVEINLAB");
			auditUtilityFunction.fnJsonArrayAudit(jsonAuditNew, null, actionType, auditmap, false, userInfo);

			String[] spreregnoArray = spreregno.split(",");
			Map<String, Object> mailMap = new HashMap<String, Object>();

			for (int i = 0; i < spreregnoArray.length; i++) {
				final int npreRegNoInt = Integer.parseInt(spreregnoArray[i]);
				try {
					mailMap.put("ncontrolcode", (int) inputMap.get("ncontrolcode"));
					mailMap.put("npreregno", npreRegNoInt);
					mailMap.put("nsectioncode", Integer.parseInt(ssectioncode));
					mailMap.put("nregsubtypecode", (int) inputMap.get("nregsubtypecode"));
					mailMap.put("nregtypecode", (int) inputMap.get("nregtypecode"));
					mailMap.put("ssystemid", sampleList.get(i).get("sarno"));
					ResponseEntity<Object> mailResponse = emailDAOSupport.createEmailAlertTransaction(mailMap,
							userInfo);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			return returnMap;
		} catch (Exception e) {
			System.out.println("e:" + e);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getAllotDetails(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		int controlCode = (int) inputMap.get("ncontrolcode");
		String spreregno = (String) inputMap.get("npreregno");
		String stransactionsamplecode = (String) inputMap.get("ntransactionsamplecode");
		String stransactiontestcode = (String) inputMap.get("transactiontestcode");
		String nsectioncode = (String) inputMap.get("nsectioncode");
		Integer calenderViewAfterAllot = (int) inputMap.get("calenderViewAfterAllot");
		String subQry = "";
		String stestsynonym = "";
		String testValue = "";
		List<Technique> lstTechnique = null;
		List<RegistrationTestHistory> filteredSampleList = validateTestStatus(spreregno, stransactionsamplecode,
				stransactiontestcode, controlCode, userInfo);
		if (calenderViewAfterAllot == 2 || filteredSampleList.isEmpty()) {
			String strTestCodeQuery = "select rt.ntestcode from registrationtest rt,registrationtesthistory rth "
					+ " where rt.ntransactiontestcode =rth.ntransactiontestcode " + "and rt.npreregno in(" + spreregno
					+ ") and rt.ntransactiontestcode in(" + stransactiontestcode + ") "
					+ "and rth.ntesthistorycode in (select max(ntesthistorycode) from registrationtesthistory where npreregno in("
					+ spreregno + ") and ntransactiontestcode in(" + stransactiontestcode
					+ ") group by ntransactiontestcode)group by rt.ntestcode";
			List<String> lsttestcode = jdbcTemplate.queryForList(strTestCodeQuery, String.class);
			String testCode = lsttestcode.stream().collect(Collectors.joining(","));

			String strTrainingNeedQuery = "select * from testmaster where ntestcode in(" + testCode + ") and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
			List<TestMaster> lst1 = jdbcTemplate.query(strTrainingNeedQuery, new TestMaster());
			String strTechniqueQuery = "";
			Integer techcode = 0;
			Integer techcode1 = 0;
			List<Section> sectionList = new ArrayList<>();
			if (inputMap.get("operation").equals("Reschedule")) {
				List<String> ntestcodeList = Arrays.asList(testCode.split(","));
				inputMap.put("ntestcodesize", ntestcodeList.size());
				String str = (String) getSectionByTest(inputMap, testCode, userInfo);

				sectionList = jdbcTemplate.query(str, new Section());
				map.put("RescheduleSection", sectionList);

			}

			for (int i = 0; i < lst1.size(); i++) {
				Integer trainingneed = (int) lst1.get(i).getNtrainingneed();
				Integer testcode = (int) lst1.get(i).getNtestcode();

				if (trainingneed == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					strTechniqueQuery = "select tt.ntechniquecode,t.stechniquename  from technique t,techniquetest tt "
							+ " where t.ntechniquecode=tt.ntechniquecode and tt.ntestcode in (" + testcode + ") "
							+ " and t.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and tt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and t.ntechniquecode>0 group by tt.ntechniquecode,t.stechniquename";

					lstTechnique = jdbcTemplate.query(strTechniqueQuery, new Technique());
					map.put("Technique", lstTechnique);

				}

				int regTypeCode = (int) inputMap.get("nregtypecode");
				int regSubTypeCode = (int) inputMap.get("nregsubtypecode");
				String strQuery = "select su.nusercode,u.sfirstname ||' '|| u.slastname as susername from sectionusers su "
						+ " join users u on u.nusercode =su.nusercode "
						+ " join userssite us on u.nusercode=us.nusercode "
						+ " join usermultirole urm on urm.nusersitecode =us.nusersitecode "
						+ " join userrole ur on ur.nuserrolecode =urm.nuserrolecode "
						+ " where  ur.nuserrolecode =(select  nuserrolecode from treetemplatetransactionrole ttr "
						+ " join approvalconfig  ac on ac.napprovalconfigcode =ttr.napprovalconfigcode  " + "where "
						+ " ac.nregtypecode=" + regTypeCode + " and ac.nregsubtypecode=" + regSubTypeCode
						+ " and ttr.ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
						+ " " + " order by ttr.nlevelno desc LIMIT 1) "
						+ " and su.nlabsectioncode in(select nlabsectioncode from labsection where nsectioncode in("
						+ nsectioncode + ") and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " ) and su.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and u.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and us.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and urm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ur.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and su.nsitecode =" + userInfo.getNtranssitecode() + " and u.ntransactionstatus <> "
						+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + " "
						+ " group by su.nusercode ,susername";

				final List<Users> lstUsers = jdbcTemplate.query(strQuery, new Users());
				map.put("Users", lstUsers);
				// }
				if (trainingneed == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					if (lstTechnique != null) {
						if (lstTechnique.size() == 0) {
							stestsynonym = lst1.get(i).getStestsynonym();
							testValue += stestsynonym.concat(",");
						} else if (lstTechnique.size() > 0) {
							techcode = lstTechnique.get(0).getNtechniquecode();
							if (techcode1 != techcode && techcode1 != 0) {
								return new ResponseEntity<>(
										commonFunction.getMultilingualMessage("IDS_SELECTTESTSAMETECHNIQUE",
												userInfo.getSlanguagefilename()),
										HttpStatus.EXPECTATION_FAILED);
							} else {
								techcode1 = techcode;
							}
						}
					}

				}
			}
			if (testValue != "") {
				testValue = testValue.substring(0, testValue.length() - 1);
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TECHNIQUENOTAVAILABLEIN",
						userInfo.getSlanguagefilename()) + ' ' + testValue, HttpStatus.EXPECTATION_FAILED);
			}
			if (lstTechnique != null) {
				final String techniquecode = lstTechnique.stream().map(obj -> String.valueOf(obj.getNtechniquecode()))
						.collect(Collectors.joining(","));
				String strtrainingquery = "select * from trainingcertification where ntechniquecode in(" + techniquecode
						+ ") and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ntransactionstatus=" + Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()
						+ "";
				final List<TrainingCertification> lst = jdbcTemplate.query(strtrainingquery,
						new TrainingCertification());
				if (lst.size() == 0) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TRAININGNOTCONDUCTEDTEST",
							userInfo.getSlanguagefilename()) + ' ' + testValue, HttpStatus.EXPECTATION_FAILED);
				}
			}

			if (!nsectioncode.equals("0")) {
				subQry = "and rt.nsectioncode in (" + nsectioncode + ") ";
			}

			//ALPDJ21-31-->Added by Vignesh(23-07-2025)-->Added the new dropdown Instrument Type-Standard, Storage
			String strInstrumentCategoryQuery = " select rt.ninstrumentcatcode,ic.sinstrumentcatname,ic.ncalibrationreq from registrationtest rt "
					+ " join  registrationtesthistory rth on rt.ntransactiontestcode = rth.ntransactiontestcode "
					+ " join instrumentcategory ic on ic.ninstrumentcatcode = rt.ninstrumentcatcode "
					//+ " join instrumenttype it on it.ninstrumenttypecode = ic.ninstrumenttypecode "
					+ " where rt.npreregno in (" + spreregno + ") and rt.ntransactiontestcode in ("
					+ stransactiontestcode + ") " + subQry + " "
					//+ " and it.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					//+" and ic.ninstrumenttypecode="+Enumeration.InstrumentType.STANDARD_INSTRUMENT.getNinstrumenttypecode()+""
					//+" and it.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " group by rt.ninstrumentcatcode,ic.sinstrumentcatname,ic.ncalibrationreq ";

			final List<InstrumentCategory> lstInstrumentCategory = jdbcTemplate.query(strInstrumentCategoryQuery,
					new InstrumentCategory());
			map.put("InstrumentCategory", lstInstrumentCategory);

			if ((int) inputMap.get("nselecttype") == 1) {
				if(!lstInstrumentCategory.isEmpty()) {
				map.putAll((Map<String, Object>) getInstrumentNameBasedCategory(
						lstInstrumentCategory.get(0).getNinstrumentcatcode(),
						lstInstrumentCategory.get(0).getNcalibrationreq(), userInfo).getBody());
				List<Instrument> lstobjInst = (List<Instrument>) map.get("InstrumentName");
				Integer instrumentNameCode = (Integer) lstobjInst.get(0).getNinstrumentnamecode();
				map.putAll((Map<String, Object>) getInstrumentIdBasedCategory(
						lstInstrumentCategory.get(0).getNinstrumentcatcode(), instrumentNameCode,
						lstInstrumentCategory.get(0).getNcalibrationreq(), userInfo).getBody());
				}
			}

			String strPeriodQuery = "select pc.nperiodcode as nuserperiodcode,pc.nperiodcode as ninstrumentperiodcode,coalesce(p.jsondata->'speriodname'->>'"
					+ userInfo.getSlanguagetypecode() + "') as speriodname from period p "
					+ " join periodconfig pc on  p.nperiodcode = pc.nperiodcode " + "where pc.nformcode="
					+ userInfo.getNformcode() + " and pc.ncontrolcode=" + inputMap.get("ncontrolcode")
					+ " and p.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and pc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";

			final List<Period> lstPeriod = jdbcTemplate.query(strPeriodQuery, new Period());
			map.put("Period", lstPeriod);
			map.put("CurrentTime", timeZoneDAO.getLocalTimeByZone(userInfo));
		} else {
			String Msg = "";
			if (inputMap.get("operation").equals("AllotJob") || inputMap.get("operation").equals("AllotJobCalendar")) {
				Msg = "IDS_SELECTRECEIVEDINLABTEST";
			} else if (inputMap.get("operation").equals("Reschedule")) {
				Msg = "IDS_SELECTALLOTACCEPTTEST";
			}
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Msg, userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	public ResponseEntity<Object> getAllotAnotherUserDetails(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		int controlCode = (int) inputMap.get("ncontrolcode");
		String spreregno = (String) inputMap.get("npreregno");
		String stransactionsamplecode = (String) inputMap.get("ntransactionsamplecode");
		String stransactiontestcode = (String) inputMap.get("transactiontestcode");
		String nsectioncode = (String) inputMap.get("nsectioncode");
		String strUserQuery = "";
		List<Technique> lstTechnique = null;
		List<RegistrationTestHistory> filteredSampleList = validateTestStatus(spreregno, stransactionsamplecode,
				stransactiontestcode, controlCode, userInfo);
		if (filteredSampleList.isEmpty()) {
			String allotAnotherUserQry = "and u.nusercode not in (select nusercode from joballocation "
					+ " where ntransactiontestcode in (" + stransactiontestcode + ") and ntestrescheduleno = "
					+ "(select max(ntestrescheduleno) ntestrescheduleno from joballocation where ntransactiontestcode in ("
					+ stransactiontestcode + ") order by ntestrescheduleno desc LIMIT 1))";

			String strTestCodeQuery = "select rt.ntestcode from registrationtest rt,registrationtesthistory rth "
					+ " where rt.ntransactiontestcode =rth.ntransactiontestcode " + "and rt.npreregno in(" + spreregno
					+ ") and rt.ntransactiontestcode in(" + stransactiontestcode + ") "
					+ " and rth.ntesthistorycode in (select max(ntesthistorycode) from registrationtesthistory where npreregno in("
					+ spreregno + ") and ntransactiontestcode in(" + stransactiontestcode
					+ ") group by ntransactiontestcode)";
			List<RegistrationTest> lsttestcode = jdbcTemplate.query(strTestCodeQuery, new RegistrationTest());
			int nTestCode = lsttestcode.get(0).getNtestcode();

			String strTrainingNeedQuery = "select * from testmaster where ntestcode =" + nTestCode + " and nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
			List<TestMaster> lst1 = jdbcTemplate.query(strTrainingNeedQuery, new TestMaster());

			String strTTechniqueQuery = "select ja.ntechniquecode,t.stechniquename  from technique t,joballocation ja "
					+ " where t.ntechniquecode=ja.ntechniquecode and ja.ntransactiontestcode in ("
					+ stransactiontestcode + ") and t.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and ja.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " group by ja.ntechniquecode,t.stechniquename";
			lstTechnique = jdbcTemplate.query(strTTechniqueQuery, new Technique());

			Integer trainingneed = (int) lst1.get(0).getNtrainingneed();
			if (trainingneed == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				Date date = new Date();
				final SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
				String currentDate = sdFormat.format(date);

				int ntechniqueCode = lstTechnique.get(0).getNtechniquecode();

				if (ntechniqueCode > 0) {
					strUserQuery = "select su.nusercode,u.sfirstname ||' '|| u.slastname as susername from sectionusers su "
							+ " join users u on u.nusercode =su.nusercode "
							+ " join userssite us on u.nusercode=us.nusercode "
							+ " join usermultirole urm on urm.nusersitecode =us.nusersitecode "
							+ " join userrole ur on ur.nuserrolecode =urm.nuserrolecode "
							+ " join trainingparticipants tp on tp.nusercode=su.nusercode "
							+ " join trainingcertification tc on tc.ntrainingcode =tp.ntrainingcode "
							+ " where  ur.nuserrolecode =(select  nuserrolecode from treetemplatetransactionrole ttr "
							+ " join approvalconfig  ac on ac.napprovalconfigcode =ttr.napprovalconfigcode  "
							+ " where ac.nregtypecode=" + inputMap.get("nregtypecode") + " and ac.nregsubtypecode="
							+ inputMap.get("nregsubtypecode") + " and ttr.ntransactionstatus="
							+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " "
							+ " order by ttr.nlevelno desc LIMIT 1)" + "and tc.ntechniquecode =" + ntechniqueCode + " "
							+ " and tp.ncompetencystatus=" + Enumeration.TransactionStatus.YES.gettransactionstatus()
							+ "  " + " and tc.ntransactionstatus="
							+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()
							+ " and su.nlabsectioncode in(select nlabsectioncode from labsection where nsectioncode in("
							+ nsectioncode + ") and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") " + allotAnotherUserQry
							+ " " + " and (tc.dtrainingexpirydate is null or tc.dtrainingexpirydate >='" + currentDate
							+ "') " + " and su.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " " + " and u.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " " + " and us.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " " + " and urm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " " + " and ur.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " " + " and tp.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " " + " and tc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " " + " and su.nsitecode =" + userInfo.getNtranssitecode()
							+ " and u.ntransactionstatus <> "
							+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + " "
							+ "group by su.nusercode ,susername";
				} else {
					strUserQuery = "select su.nusercode,u.sfirstname ||' '|| u.slastname as susername from sectionusers su "
							+ " join users u on u.nusercode =su.nusercode "
							+ " join userssite us on u.nusercode=us.nusercode "
							+ " join usermultirole urm on urm.nusersitecode =us.nusersitecode "
							+ " join userrole ur on ur.nuserrolecode =urm.nuserrolecode "
							+ " where  ur.nuserrolecode =(select  nuserrolecode from treetemplatetransactionrole ttr "
							+ " join approvalconfig  ac on ac.napprovalconfigcode =ttr.napprovalconfigcode  "
							+ " where ac.nregtypecode=" + inputMap.get("nregtypecode") + " and ac.nregsubtypecode="
							+ inputMap.get("nregsubtypecode") + " and ttr.ntransactionstatus="
							+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " "
							+ " order by ttr.nlevelno desc LIMIT 1)" + " "
							+ " and su.nlabsectioncode in(select nlabsectioncode from labsection where nsectioncode in("
							+ nsectioncode + ") and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") " + allotAnotherUserQry
							+ " " + " and su.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " " + " and u.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " " + " and us.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " " + " and urm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " " + " and ur.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " " + " and su.nsitecode =" + userInfo.getNtranssitecode()
							+ " and u.ntransactionstatus <> "
							+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + " "
							+ "group by su.nusercode ,susername";
				}

			} else if (trainingneed == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
				strUserQuery = "select su.nusercode,u.sfirstname ||' '|| u.slastname as susername from sectionusers su "
						+ " join users u on u.nusercode =su.nusercode "
						+ " join userssite us on u.nusercode=us.nusercode "
						+ " join usermultirole urm on urm.nusersitecode =us.nusersitecode "
						+ " join userrole ur on ur.nuserrolecode =urm.nuserrolecode "
						+ " where  ur.nuserrolecode =(select  nuserrolecode from treetemplatetransactionrole ttr "
						+ " join approvalconfig  ac on ac.napprovalconfigcode =ttr.napprovalconfigcode  " + "where "
						+ " ac.nregtypecode=" + inputMap.get("nregtypecode") + " and ac.nregsubtypecode="
						+ inputMap.get("nregsubtypecode") + " and ttr.ntransactionstatus="
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " "
						+ " order by ttr.nlevelno desc LIMIT 1) "
						+ " and su.nlabsectioncode in(select nlabsectioncode from labsection where nsectioncode in("
						+ nsectioncode + ") and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ ") " + allotAnotherUserQry + " and su.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and us.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and urm.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and su.nsitecode ="
						+ userInfo.getNtranssitecode() + " and u.ntransactionstatus <> "
						+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + " "
						+ " group by su.nusercode ,susername";
			}
			final List<Users> lstUsers = jdbcTemplate.query(strUserQuery, new Users());
			map.put("Technique", lstTechnique);
			map.put("Users", lstUsers);

			String strPeriodQuery = "select pc.nperiodcode as nuserperiodcode,pc.nperiodcode  as ninstrumentperiodcode,coalesce(p.jsondata->'speriodname'->>'"
					+ userInfo.getSlanguagetypecode() + "') as speriodname from period p "
					+ " join periodconfig pc on  p.nperiodcode = pc.nperiodcode where pc.nformcode="
					+ userInfo.getNformcode() + " and pc.ncontrolcode=" + inputMap.get("ncontrolcode")
					+ " and p.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and pc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";

			final List<Period> lstPeriod = jdbcTemplate.query(strPeriodQuery, new Period());
			map.put("Period", lstPeriod);
			map.put("CurrentTime", timeZoneDAO.getLocalTimeByZone(userInfo));

		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTALLOTACCEPTINITITATETEST",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}

		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getInstrumentNameBasedCategory(int instrumentCatCode, int calibrationRequired,
			UserInfo userInfo) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		String strQuery = "";
		if (instrumentCatCode == Enumeration.TransactionStatus.NA.gettransactionstatus()) {
			strQuery = "select ninstrumentnamecode,sinstrumentname from instrument where ninstrumentcatcode="
					+ instrumentCatCode + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " ";

		} else {
			if (calibrationRequired == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				strQuery = " select i.ninstrumentnamecode,i.sinstrumentname, ic1.ncalibrationreq "
						+ " from instrument i "
						+ " join instrumentcategory ic1  on ic1.ninstrumentcatcode = i.ninstrumentcatcode "
						+ " join instrumentvalidation iv on iv.ninstrumentcode = i.ninstrumentcode "
						+ " join instrumentcalibration ic on ic.ninstrumentcode = i.ninstrumentcode "
						+ " join instrumentmaintenance im on im.ninstrumentcode = i.ninstrumentcode  "
						+ " join site s on s.nsitecode =i.nregionalsitecode " + " where i.ninstrumentcatcode ="
						+ instrumentCatCode + " and i.nregionalsitecode=" + userInfo.getNsitecode()
						+ " and i.ninstrumentstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and iv.nvalidationstatus=" + Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()
						+ " and ic.ncalibrationstatus="
						+ Enumeration.TransactionStatus.CALIBIRATION.gettransactionstatus()
						+ " and im.nmaintenancestatus="
						+ Enumeration.TransactionStatus.MAINTANENCE.gettransactionstatus() + " and i.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and iv.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ic.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and im.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ic1.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " group by i.ninstrumentnamecode,i.sinstrumentname,ic1.ncalibrationreq";

			} else {
				strQuery = " select i.ninstrumentnamecode,i.sinstrumentname,ic1.ncalibrationreq "
						+ " from instrument i "
						+ " join instrumentcategory ic1  on ic1.ninstrumentcatcode = i.ninstrumentcatcode "
						+ " join instrumentvalidation iv on iv.ninstrumentcode = i.ninstrumentcode "
						+ " join instrumentmaintenance im on im.ninstrumentcode = i.ninstrumentcode  "
						+ " join site s on s.nsitecode =i.nregionalsitecode " + " where i.ninstrumentcatcode ="
						+ instrumentCatCode + " and i.nregionalsitecode=" + userInfo.getNsitecode()
						+ " and i.ninstrumentstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and iv.nvalidationstatus=" + Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()
						+ " and im.nmaintenancestatus="
						+ Enumeration.TransactionStatus.MAINTANENCE.gettransactionstatus() + " and i.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and iv.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and im.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ic1.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " group by i.ninstrumentnamecode,i.sinstrumentname,ic1.ncalibrationreq ";

			}

		}

		final List<Instrument> lstInstrument = jdbcTemplate.query(strQuery, new Instrument());
		map.put("InstrumentName", lstInstrument);
		return new ResponseEntity<>(map, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> getInstrumentIdBasedCategory(int instrumentCatCode, int instrumentNameCode,
			int calibrationRequired, UserInfo userInfo) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		String strQuery = "";
		if (instrumentCatCode == Enumeration.TransactionStatus.NA.gettransactionstatus()) {
			strQuery = " select ninstrumentcode,sinstrumentid from instrument where ninstrumentcatcode="
					+ instrumentCatCode + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " ";

		} else {
			if (calibrationRequired == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				strQuery = " select i.ninstrumentcode,i.sinstrumentid " + " from instrument i "
						+ " join instrumentvalidation iv on iv.ninstrumentcode = i.ninstrumentcode "
						+ " join instrumentcalibration ic on ic.ninstrumentcode = i.ninstrumentcode "
						+ " join instrumentmaintenance im on im.ninstrumentcode = i.ninstrumentcode  "
						+ " join site s on s.nsitecode = i.nregionalsitecode" + " where i.ninstrumentcatcode ="
						+ instrumentCatCode + " and i.ninstrumentnamecode=" + instrumentNameCode
						+ " and i.nregionalsitecode=" + userInfo.getNsitecode() + " and i.ninstrumentstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ "  and  ic.ninstrumentcalibrationcode=(select max(ic.ninstrumentcalibrationcode) from  instrumentcalibration ic,"
						+ " instrumentname iname"
						+ " where ic.ninstrumentcode=i.ninstrumentcode and iname.ninstrumentnamecode=i.ninstrumentnamecode and ic.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " group by ic.ninstrumentcode)"
						+ " and im.ninstrumentmaintenancecode=(select max(im.ninstrumentmaintenancecode) from  instrumentmaintenance im,"
						+ " instrumentname iname where im.ninstrumentcode=i.ninstrumentcode and iname.ninstrumentnamecode=i.ninstrumentnamecode and im.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " group by im.ninstrumentcode )"
						+ " and iv.ninstrumentvalidationcode=(select max(iv.ninstrumentvalidationcode) from  instrumentvalidation iv,"
						+ " instrumentname iname"
						+ " where iv.ninstrumentcode=i.ninstrumentcode and iname.ninstrumentnamecode=i.ninstrumentnamecode and iv.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " group by iv.ninstrumentcode)" + " and i.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and iv.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ic.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and im.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ic.dclosedate is not null " + " "
						+ " and im.dclosedate is not null "
						+ " and iv.nvalidationstatus=" + Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()
						+ " and ic.ncalibrationstatus="
						+ Enumeration.TransactionStatus.CALIBIRATION.gettransactionstatus()
						+ " and im.nmaintenancestatus="
						+ Enumeration.TransactionStatus.MAINTANENCE.gettransactionstatus()
						+ " group by i.ninstrumentcode,i.sinstrumentid ";

			} else {
				strQuery = " select i.ninstrumentcode,i.sinstrumentid " + " from instrument i "
						+ " join instrumentvalidation iv on iv.ninstrumentcode = i.ninstrumentcode "
						+ " join instrumentmaintenance im on im.ninstrumentcode = i.ninstrumentcode  "
						+ " join site s on s.nsitecode =i.nregionalsitecode " + "" + " where i.ninstrumentcatcode ="
						+ instrumentCatCode + " " + " and i.ninstrumentnamecode=" + instrumentNameCode
						+ " and i.nregionalsitecode=" + userInfo.getNsitecode() + " and i.ninstrumentstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and i.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and iv.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and im.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and im.ninstrumentmaintenancecode=(select max(im.ninstrumentmaintenancecode) from  instrumentmaintenance im,"
						+ " instrumentname iname where im.ninstrumentcode=i.ninstrumentcode and iname.ninstrumentnamecode=i.ninstrumentnamecode "
						+ " and im.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " group by im.ninstrumentcode )"
						+ " and iv.ninstrumentvalidationcode=(select max(iv.ninstrumentvalidationcode) from  instrumentvalidation iv,"
						+ " instrumentname iname"
						+ " where iv.ninstrumentcode=i.ninstrumentcode and iname.ninstrumentnamecode=i.ninstrumentnamecode "
						+ " and iv.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " group by iv.ninstrumentcode) " //+ " and ic.dclosedate is not null "
						+ " and im.dclosedate is not null " + " and iv.nvalidationstatus="
						+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()
						+ " and im.nmaintenancestatus="
						+ Enumeration.TransactionStatus.MAINTANENCE.gettransactionstatus()
						+ " group by i.ninstrumentcode,i.sinstrumentid";

			}

		}

		final List<Instrument> lstInstrument = jdbcTemplate.query(strQuery, new Instrument());
		map.put("InstrumentId", lstInstrument);
		return new ResponseEntity<>(map, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> getUsersBasedTechnique(int techniqueCode, String sectionCode, int regTypeCode,
			int regSubTypeCode, UserInfo userInfo) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();

		String strQuery = "";
		if (techniqueCode > 0) {
			Date date = new Date();
			final SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
			String currentdate = sdFormat.format(date);

			strQuery = "select su.nusercode,u.sfirstname ||' '|| u.slastname as susername from sectionusers su "
					+ " join users u on u.nusercode =su.nusercode " + "join userssite us on u.nusercode=us.nusercode "
					+ " join usermultirole urm on urm.nusersitecode =us.nusersitecode "
					+ " join userrole ur on ur.nuserrolecode =urm.nuserrolecode "
					+ " join trainingparticipants tp on tp.nusercode=su.nusercode "
					+ " join trainingcertification tc on tc.ntrainingcode =tp.ntrainingcode "
					+ " where  ur.nuserrolecode =(select  nuserrolecode from treetemplatetransactionrole ttr "
					+ " join approvalconfig  ac on ac.napprovalconfigcode =ttr.napprovalconfigcode  "
					+ " where ac.nregtypecode=" + regTypeCode + " and ac.nregsubtypecode=" + regSubTypeCode
					+ " and ttr.ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
					+ " order by ttr.nlevelno desc LIMIT 1) and tc.ntechniquecode=" + techniqueCode
					+ " and tp.ncompetencystatus=" + Enumeration.TransactionStatus.YES.gettransactionstatus()
					+ " and tc.ntransactionstatus=" + Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()
					+ " and (tc.dtrainingexpirydate is null or tc.dtrainingexpirydate >= '" + currentdate + "') "
					+ " and su.nlabsectioncode in(select nlabsectioncode from labsection where nsectioncode in("
					+ sectionCode + ") and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode=" + userInfo.getNtranssitecode() + ") and su.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and u.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and us.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and urm.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and ur.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and tp.nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and tc.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and su.nsitecode ="
					+ userInfo.getNtranssitecode() + " and u.ntransactionstatus <> "
					+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + " "
					+ "group by su.nusercode ,susername ";
		} else {
			strQuery = "select su.nusercode,u.sfirstname ||' '|| u.slastname as susername from sectionusers su "
					+ " join users u on u.nusercode =su.nusercode " + "join userssite us on u.nusercode=us.nusercode "
					+ " join usermultirole urm on urm.nusersitecode =us.nusersitecode "
					+ " join userrole ur on ur.nuserrolecode =urm.nuserrolecode "
					+ " where  ur.nuserrolecode =(select  nuserrolecode from treetemplatetransactionrole ttr "
					+ " join approvalconfig  ac on ac.napprovalconfigcode =ttr.napprovalconfigcode  " + "where "
					+ " ac.nregtypecode=" + regTypeCode + " and ac.nregsubtypecode=" + regSubTypeCode
					+ " and ttr.ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
					+ " order by ttr.nlevelno desc LIMIT 1) "
					+ " and su.nlabsectioncode in(select nlabsectioncode from labsection where nsectioncode in("
					+ sectionCode + " and nsitecode=" + userInfo.getNtranssitecode() + ") and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " su.nistecode="
					+ userInfo.getNsitecode() + ") and su.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and ls.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and u.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and us.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and urm.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and ur.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and su.nsitecode ="
					+ userInfo.getNtranssitecode() + " and u.ntransactionstatus <> "
					+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + " "
					+ " group by su.nusercode ,susername";
		}
		final List<Users> lstUsers = jdbcTemplate.query(strQuery, new Users());
		map.put("Users", lstUsers);
		return new ResponseEntity<>(map, HttpStatus.OK);

	}

	@SuppressWarnings("unused")
	public Map<String, Object> insertSeqNoAllotJob(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		String sScheduleSkip = scheduleSkip();
		Map<String, Object> map = new HashMap<String, Object>();

		String sQuery = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		sQuery = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		sQuery = " lock  table lockcancelreject " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		sQuery = "lock lockresultusedinstrument" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		int instrumentCatcode = (int) inputMap.get("ninstrumentcatcode");
		String spreregno = (String) inputMap.get("npreregno");
		String transactionSampleCode = (String) inputMap.get("ntransactionsamplecode");
		String transactionTestCode = (String) inputMap.get("ntransactiontestcode");
		int controlCode = (int) inputMap.get("ncontrolcode");
		Integer ntransactionstatus;
		Integer ntesthistoryseqcount;
		Integer njoballocationseqcount;
		Integer nresultinstrumentseqcount = 0;
		Integer testhistorycount;
		Integer joballocationcount;
		Integer resultinstrumentcount;

		List<RegistrationTestHistory> filteredSampleList = validateTestStatus(spreregno, transactionSampleCode,
				transactionTestCode, controlCode, userInfo);
		if (filteredSampleList.isEmpty()) {
			ntransactionstatus = Enumeration.TransactionStatus.RECIEVED_IN_SECTION.gettransactionstatus();

			String testCountQuery = "select * from registrationtest rt,registrationtesthistory rth  "
					+ " where rt.ntransactiontestcode =rth.ntransactiontestcode " + "and rt.ntransactiontestcode in ("
					+ transactionTestCode + ") and rt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and rth.ntesthistorycode =any( "
					+ " select max(ntesthistorycode) from registrationtesthistory where ntransactiontestcode in("
					+ transactionTestCode + ") and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " group by npreregno,ntransactionsamplecode,ntransactiontestcode) "
					+ " and rth.ntransactionstatus=" + ntransactionstatus + "";
			List<RegistrationTestHistory> lstRegistrationTestHistory = (List<RegistrationTestHistory>) jdbcTemplate
					.query(testCountQuery, new RegistrationTestHistory());
			Integer testCount = lstRegistrationTestHistory.size();

			final List<String> updateSeqnolst = Arrays.asList("joballocation", "registrationtesthistory",
					"resultusedinstrument");

			String jobAllocationSeqCount = "select nsequenceno from  seqnoregistration where stablename ='joballocation' ";
			njoballocationseqcount = jdbcTemplate.queryForObject(jobAllocationSeqCount, Integer.class);
			joballocationcount = njoballocationseqcount + testCount;

			String testHistorySeqCount = "select nsequenceno from  seqnoregistration where stablename ='registrationtesthistory' ";
			ntesthistoryseqcount = jdbcTemplate.queryForObject(testHistorySeqCount, Integer.class);
			testhistorycount = ntesthistoryseqcount + testCount;

			if (instrumentCatcode > 0) {
				String resultInstrumentSeqCount = "select nsequenceno from  seqnoregistration where stablename ='resultusedinstrument' ";
				nresultinstrumentseqcount = jdbcTemplate.queryForObject(resultInstrumentSeqCount, Integer.class);
				resultinstrumentcount = nresultinstrumentseqcount + testCount;

				String updateResultInstrumentQuery = "update seqnoregistration set nsequenceno ="
						+ resultinstrumentcount + " where stablename='resultusedinstrument'";
				jdbcTemplate.execute(updateResultInstrumentQuery);
			}

			String query = "select * from joballocation where ntransactiontestcode in ("
					+ inputMap.get("ntransactiontestcode") + ") and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			List<JobAllocation> listJoBAllocation = jdbcTemplate.query(query, new JobAllocation());
			if (!(sScheduleSkip.equals(String.valueOf(Enumeration.TransactionStatus.YES.gettransactionstatus()))
					&& listJoBAllocation.size() > 0)) {
				String updateJobAllocationQuery = "update seqnoregistration set nsequenceno =" + joballocationcount
						+ " where stablename='joballocation'";
				jdbcTemplate.execute(updateJobAllocationQuery);
			}

			String updateTestQuery = "update seqnoregistration set nsequenceno =" + testhistorycount
					+ " where stablename='registrationtesthistory'";
			jdbcTemplate.execute(updateTestQuery);

			map.put("TransCode", ntransactionstatus);
			map.put("njoballocationseqcount", njoballocationseqcount);
			map.put("nresultinstrumentseqcount", nresultinstrumentseqcount);
			map.put("ntesthistoryseqcount", ntesthistoryseqcount);
			map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		} else {
			map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					commonFunction.getMultilingualMessage("IDS_SELECTVALIDTEST", userInfo.getSlanguagefilename()));
		}

		return map;
	}

	@Override
	@SuppressWarnings({ "unused" })
	public Map<String, Object> AllotJobCreate(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		ObjectMapper objmap = new ObjectMapper();
		String sScheduleSkip = scheduleSkip();
		int instrumentCatcode = (int) inputMap.get("ninstrumentcatcode");
		int instrumentNamecode = (int) inputMap.get("ninstrumentnamecode");
		int instrumentcode = (int) inputMap.get("ninstrumentcode");

		String spreregno = (String) inputMap.get("npreregno");
		String transactionSampleCode = (String) inputMap.get("ntransactionsamplecode");
		String transactionTestCode = (String) inputMap.get("ntransactiontestcode");
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date userEndDate = null;
		Date instrumentEndDate = null;

		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObject = new JSONObject();
		List<String> dateList = new ArrayList<String>();

		JSONObject jsonObject = new JSONObject(inputMap.get("jsondata").toString());
		JSONObject jsonUIObject = new JSONObject(inputMap.get("jsonuidata").toString());
		JSONObject instrumentjsonObj = new JSONObject();

		String userPeriod = jsonObject.getJSONObject("UserPeriod").getString("label");
		int userHoldDuration = jsonObject.getInt("UserHoldDuration");
		String userStartDate = jsonObject.getString("UserStartDate");

		String instrumentPeriod = jsonObject.getJSONObject("InstrumentPeriod").getString("label");
		int instrumentHoldDuration = jsonObject.getInt("InstrumentHoldDuration");
		String instrumentStartDate = jsonObject.getString("InstrumentStartDate");

		userEndDate = formatter.parse(userStartDate);
		Calendar userEndCalendar = Calendar.getInstance();
		userEndCalendar.setTime(userEndDate);

		if (userPeriod.equals("Day(s)")) {
			userEndCalendar.add(Calendar.DAY_OF_MONTH, userHoldDuration);
		} else if (userPeriod.equals("Hour(s)")) {
			userEndCalendar.add(Calendar.HOUR, userHoldDuration);
		}
		Date UED = userEndCalendar.getTime();
		String UserEndDateTime = formatter.format(UED);
		jsonObject.put("UserEndDate", UserEndDateTime);
		jsonUIObject.put("UserEndDate", UserEndDateTime);

		// Instrument Date
		String InstrumentEndDateTime = "";
		if (!instrumentStartDate.equals("")) {
			instrumentEndDate = formatter.parse(instrumentStartDate);
			Calendar instrumentEndCalendar = Calendar.getInstance();
			instrumentEndCalendar.setTime(instrumentEndDate);

			if (instrumentPeriod.equals("Day(s)")) {
				instrumentEndCalendar.add(Calendar.DAY_OF_MONTH, instrumentHoldDuration);
			} else if (instrumentPeriod.equals("Hour(s)")) {
				instrumentEndCalendar.add(Calendar.HOUR, instrumentHoldDuration);
			}
			Date IED = instrumentEndCalendar.getTime();
			InstrumentEndDateTime = formatter.format(IED);
		}

		jsonObject.put("InstrumentEndDate", InstrumentEndDateTime);
		jsonUIObject.put("InstrumentEndDate", InstrumentEndDateTime);

		// resultusedinstrument
		JSONObject instrumentcategory = jsonObject.getJSONObject("InstrumentCategory");
		JSONObject instrumentname = jsonObject.getJSONObject("InstrumentName");
		JSONObject instrumentid = jsonObject.getJSONObject("InstrumentId");

		instrumentStartDate = jsonObject.getString("InstrumentStartDate");
		instrumentjsonObj.put("InstrumentCategory", instrumentcategory);
		instrumentjsonObj.put("InstrumentName", instrumentname);
		instrumentjsonObj.put("InstrumentId", instrumentid);
		instrumentjsonObj.put("InstrumentStartDate", instrumentStartDate);
		instrumentjsonObj.put("InstrumentEndDate", InstrumentEndDateTime);

		dateList.add("UserStartDate");
		dateList.add("UserEndDate");
		dateList.add("InstrumentStartDate");
		dateList.add("InstrumentEndDate");
		jsonObject = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonObject, dateList, false,
				userInfo);

		Integer ntranscode = Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus();
		Integer ntransactionstatus = (Integer) inputMap.get("TransCode");
		Integer njoballocationseqcount = (Integer) inputMap.get("njoballocationseqcount");
		Integer nresultinstrumentseqcount = (Integer) inputMap.get("nresultinstrumentseqcount");
		Integer ntesthistoryseqcount = (Integer) inputMap.get("ntesthistoryseqcount");
		String query = "select * from joballocation where ntransactiontestcode in ("
				+ inputMap.get("ntransactiontestcode") + ") and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		List<JobAllocation> listJoBAllocation = jdbcTemplate.query(query, new JobAllocation());
		if (sScheduleSkip.equals(String.valueOf(Enumeration.TransactionStatus.YES.gettransactionstatus()))
				&& listJoBAllocation.size() > 0) {
			String updateJbAllocation = "update joballocation set " + " ntechniquecode="
					+ inputMap.get("ntechniquecode") + "," + " nusercode=" + inputMap.get("nusercode")
					+ ",nuserperiodcode=" + inputMap.get("nuserperiodcode") + "," + " ninstrumentcategorycode="
					+ inputMap.get("ninstrumentcatcode") + ",ninstrumentcode=" + inputMap.get("ninstrumentcode") + ","
					+ " ninstrumentnamecode=" + instrumentNamecode + ",ninstrumentperiodcode="
					+ inputMap.get("ninstrumentperiodcode") + "," + " jsondata='"
					+ stringUtilityFunction.replaceQuote(jsonObject.toString()) + "'::JSONB,jsonuidata='"
					+ stringUtilityFunction.replaceQuote(jsonUIObject.toString()) + "'::JSONB," + " nsitecode="
					+ userInfo.getNtranssitecode() + " where ntransactiontestcode in (" + transactionTestCode + ")";
			jdbcTemplate.execute(updateJbAllocation);
		} else {

			String insertJobAllocationQuery = "Insert into joballocation(njoballocationcode,npreregno,ntransactionsamplecode,ntransactiontestcode,nsectioncode,ntechniquecode,"
					+ " nuserrolecode,nusercode,nuserperiodcode,ninstrumentcategorycode,ninstrumentcode,ninstrumentnamecode,ninstrumentperiodcode,ntimezonecode,"
					+ " ntestrescheduleno,jsondata,jsonuidata,nsitecode,nstatus)" + "(select (" + njoballocationseqcount
					+ ")+rank () over(order by rt.ntransactionsamplecode,rt.ntransactiontestcode)  as njoballocationcode,rt.npreregno,rt.ntransactionsamplecode,rt.ntransactiontestcode,"
					+ " rt.nsectioncode," + inputMap.get("ntechniquecode")
					+ " as ntechniquecode,(select nuserrolecode from treetemplatetransactionrole ttr join approvalconfig ac on ac.napprovalconfigcode =ttr.napprovalconfigcode and ac.nregtypecode ="
					+ inputMap.get("nregtypecode") + " and ac.nregsubtypecode=" + inputMap.get("nregsubtypecode")
					+ " and ttr.ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
					+ " order by ttr.nlevelno desc LIMIT 1) as nuserrolecode," + inputMap.get("nusercode")
					+ " as nusercode," + " " + inputMap.get("nuserperiodcode") + " as nuserperiodcode,"
					+ inputMap.get("ninstrumentcatcode") + " as ninstrumentcategorycode,"
					+ inputMap.get("ninstrumentcode") + " as ninstrumentcode, " + instrumentNamecode
					+ " as ninstrumentnamecode, " + " " + inputMap.get("ninstrumentperiodcode")
					+ " as ninstrumentperiodcode," + userInfo.getNtimezonecode()
					+ " as ntimezonecode,0 as ntestrescheduleno,'"
					+ stringUtilityFunction.replaceQuote(jsonObject.toString()) + "'::JSONB,'"
					+ stringUtilityFunction.replaceQuote(jsonUIObject.toString()) + "'::JSONB,"
					+ userInfo.getNtranssitecode() + " as nsitecode,"
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " as nstatus "
					+ " from registrationtest rt,registrationtesthistory rth  "
					+ " where rt.ntransactiontestcode =rth.ntransactiontestcode "
					+ "	and rt.ntransactiontestcode in (" + transactionTestCode + ") and rt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.ntesthistorycode =any( "
					+ " select max(ntesthistorycode) from registrationtesthistory where ntransactiontestcode in("
					+ transactionTestCode + ") and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " group by npreregno,ntransactionsamplecode,ntransactiontestcode) and rth.ntransactionstatus="
					+ ntransactionstatus + "); ";
			jdbcTemplate.execute(insertJobAllocationQuery);
		}
		if (instrumentcode > 0) {

			String insertResultInstrumentQuery = "insert into resultusedinstrument "
					+ "(nresultusedinstrumentcode,ntransactiontestcode,npreregno,ninstrumentcatcode,ninstrumentcode,ninstrumentnamecode,dfromdate,dtodate,ntzfromdate,ntztodate,noffsetdfromdate,noffsetdtodate,jsondata,nsitecode,nstatus) "
					+ "(select (" + nresultinstrumentseqcount
					+ ")+Rank() over(order by rt.ntransactionsamplecode,rt.ntransactiontestcode) as nresultusedinstrumentcode,rt.ntransactiontestcode,rt.npreregno,"
					+ "" + inputMap.get("ninstrumentcatcode") + " as ninstrumentcatcode,"
					+ inputMap.get("ninstrumentcode") + " as ninstrumentcode, " + inputMap.get("ninstrumentnamecode")
					+ " as  ninstrumentnamecode, " + " '" + instrumentStartDate + "' as dfromdate,'"
					+ InstrumentEndDateTime + "' as dtodate," + userInfo.getNtimezonecode() + " as ntzfromdate,"
					+ userInfo.getNtimezonecode() + " as ntztodate,"
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ",'"
					+ stringUtilityFunction.replaceQuote(instrumentjsonObj.toString()) + "'::JSONB ,"
					+ userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " as nstatus " + "from registrationtest rt,registrationtesthistory rth   "
					+ "where rt.ntransactiontestcode =rth.ntransactiontestcode  " + "and rt.ntransactiontestcode in ("
					+ transactionTestCode + ") and rt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and rth.ntesthistorycode =any(  "
					+ " select max(ntesthistorycode) from registrationtesthistory where ntransactiontestcode in("
					+ transactionTestCode + ") and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " group by npreregno,ntransactionsamplecode,ntransactiontestcode)  "
					+ " and rth.ntransactionstatus=" + ntransactionstatus + ");";
			jdbcTemplate.execute(insertResultInstrumentQuery);
		}

		String insertTestHistoryQuery = "Insert into registrationtesthistory (ntesthistorycode,nformcode,npreregno,ntransactionsamplecode,ntransactiontestcode,nusercode,nuserrolecode,"
				+ "ndeputyusercode,ndeputyuserrolecode,nsampleapprovalhistorycode,ntransactionstatus,dtransactiondate,noffsetdtransactiondate,ntransdatetimezonecode,"
				+ "scomments,nsitecode,nstatus)" + "(select (" + ntesthistoryseqcount
				+ ")+Rank()over(order by ntesthistorycode) ntesthistorycode," + " " + userInfo.getNformcode()
				+ " as nformcode,rt.npreregno,rt.ntransactionsamplecode,rt.ntransactiontestcode,"
				+ userInfo.getNusercode() + " as nusercode," + userInfo.getNuserrole() + " as nuserrolecode," + " "
				+ userInfo.getNdeputyusercode() + " as ndeputyusercode," + userInfo.getNdeputyuserrole()
				+ " as ndeputyuserrolecode,-1 as nsampleapprovalhistorycode," + ntranscode + " as ntransactionstatus,"
				+ " '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "' dtransactiondate,"
				+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + " noffsetdtransactiondate,"
				+ userInfo.getNtimezonecode() + " ntransdatetimezonecode," + " N'"
				+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "' as scomments,"
				+ userInfo.getNtranssitecode() + " as nsitecode,"
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " as nstatus "
				+ " from registrationtest rt,registrationtesthistory rth  "
				+ " where rt.ntransactiontestcode =rth.ntransactiontestcode " + "and rt.ntransactiontestcode in ("
				+ transactionTestCode + ") and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.ntesthistorycode =any( "
				+ " select max(ntesthistorycode) from registrationtesthistory where ntransactiontestcode in("
				+ transactionTestCode + ") and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by npreregno,ntransactionsamplecode,ntransactiontestcode) and rth.ntransactionstatus="
				+ ntransactionstatus + ");	";
		jdbcTemplate.execute(insertTestHistoryQuery);
		
//		ALPD-6064	Added createIntegrationMethod by Vishakh (08-07-2025)
		boolean nneedmyjob = (boolean) inputMap.get("nneedmyjob");
		boolean nneedtestinitiate = (boolean) inputMap.get("nneedtestinitiate");
		
		if(!nneedmyjob && !nneedtestinitiate)
		{
			inputMap.put("sintegrationpreregno",spreregno);
			inputMap.put("svalidationstatus",Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus()); 
			
			transactionDAOSupport.createIntegrationRecord(inputMap, userInfo);
		}
		
		map = (Map<String, Object>) getJobAllocationTest(inputMap, userInfo);
		List<Map<String, Object>> lstDataTest = AllocationAuditGet(inputMap, userInfo);
		auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
		auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
		auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
		actionType.put("joballocation", "IDS_ALLOTJOB");
		jsonAuditObject.put("joballocation", lstDataTest);
		auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);
		return map;
	}

	@SuppressWarnings("unused")
	public Map<String, Object> insertSeqNoAllotAnotherUser(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		Map<String, Object> map = new HashMap<String, Object>();
		String sQuery = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		sQuery = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		sQuery = " lock  table lockcancelreject " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		String spreregno = (String) inputMap.get("npreregno");
		String transactionSampleCode = (String) inputMap.get("ntransactionsamplecode");
		String transactionTestCode = (String) inputMap.get("ntransactiontestcode");
		Integer controlCode = (int) inputMap.get("ncontrolcode");
		String ntransactionstatus = (String) inputMap.get("ntransstatus");
		Integer ntesthistoryseqcount;
		Integer njoballocationseqcount;
		Integer testhistorycount;
		Integer joballocationcount;

		List<RegistrationTestHistory> filteredSampleList = validateTestStatus(spreregno, transactionSampleCode,
				transactionTestCode, controlCode, userInfo);
		if (filteredSampleList.isEmpty()) {
			String testCountQuery = "select * from registrationtest rt,registrationtesthistory rth  "
					+ " where rt.ntransactiontestcode =rth.ntransactiontestcode " + "and rt.ntransactiontestcode in ("
					+ transactionTestCode + ") and rt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and rth.ntesthistorycode =any( "
					+ " select max(ntesthistorycode) from registrationtesthistory where ntransactiontestcode in("
					+ transactionTestCode + ") and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " group by npreregno,ntransactionsamplecode, " + "ntransactiontestcode) "
					+ " and rth.ntransactionstatus in(" + ntransactionstatus + ")";
			List<RegistrationTestHistory> lstRegistrationTestHistory = (List<RegistrationTestHistory>) jdbcTemplate
					.query(testCountQuery, new RegistrationTestHistory());
			Integer testCount = lstRegistrationTestHistory.size();

			final List<String> updateSeqnolst = Arrays.asList("joballocation", "registrationtesthistory");

			String testHistorySeqCount = "select nsequenceno from  seqnoregistration where stablename ='registrationtesthistory' ";
			ntesthistoryseqcount = jdbcTemplate.queryForObject(testHistorySeqCount, Integer.class);
			testhistorycount = ntesthistoryseqcount + testCount;

			String jobAllocationSeqCount = "select nsequenceno from  seqnoregistration where stablename ='joballocation' ";
			njoballocationseqcount = jdbcTemplate.queryForObject(jobAllocationSeqCount, Integer.class);
			joballocationcount = njoballocationseqcount + testCount;

			String updateJobAllocationQuery = "update seqnoregistration set nsequenceno =" + joballocationcount
					+ " where stablename='joballocation'";
			jdbcTemplate.execute(updateJobAllocationQuery);

			String updateTestQuery = "update seqnoregistration set nsequenceno =" + testhistorycount
					+ " where stablename='registrationtesthistory'";
			jdbcTemplate.execute(updateTestQuery);
			map.put("TransCode", ntransactionstatus);
			map.put("ntesthistoryseqcount", ntesthistoryseqcount);
			map.put("njoballocationseqcount", njoballocationseqcount);

			map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.SUCCESS.getreturnstatus());

		} else {
			map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					commonFunction.getMultilingualMessage("IDS_SELECTVALIDTEST", userInfo.getSlanguagefilename()));
		}

		return map;
	}

	@Override
	@SuppressWarnings({ "unused" })
	public Map<String, Object> AllotAnotherUserCreate(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		ObjectMapper objmap = new ObjectMapper();
		String spreregno = (String) inputMap.get("npreregno");
		String transactionSampleCode = (String) inputMap.get("ntransactionsamplecode");
		String transactionTestCode = (String) inputMap.get("ntransactiontestcode");
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date userEndDate = null;
		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObject = new JSONObject();
		List<String> dateList = new ArrayList<String>();

		JSONObject jsonObject = new JSONObject(inputMap.get("jsondata").toString());
		JSONObject jsonUIObject = new JSONObject(inputMap.get("jsonuidata").toString());

		String userPeriod = jsonObject.getJSONObject("UserPeriod").getString("label");
		int userHoldDuration = jsonObject.getInt("UserHoldDuration");
		String userStartDate = jsonObject.getString("UserStartDate");
		// User Date
		userEndDate = formatter.parse(userStartDate);
		Calendar userEndCalendar = Calendar.getInstance();
		userEndCalendar.setTime(userEndDate);

		if (userPeriod.equals("Day(s)")) {
			userEndCalendar.add(Calendar.DAY_OF_MONTH, userHoldDuration);
		} else if (userPeriod.equals("Hour(s)")) {
			userEndCalendar.add(Calendar.HOUR, userHoldDuration);
		}
		Date UED = userEndCalendar.getTime();
		String UserEndDateTime = formatter.format(UED);
		jsonObject.put("UserEndDate", UserEndDateTime);
		jsonUIObject.put("UserEndDate", UserEndDateTime);

		dateList.add("UserStartDate");
		dateList.add("UserEndDate");

		jsonObject = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonObject, dateList, false,
				userInfo);

		Integer ntranscode = Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus();
		String ntransactionstatus = (String) inputMap.get("TransCode");
		Integer ntesthistoryseqcount = (Integer) inputMap.get("ntesthistoryseqcount");
		Integer njoballocationseqcount = (Integer) inputMap.get("njoballocationseqcount");

		String insertJobAllocationQuery = "Insert into joballocation(njoballocationcode,npreregno,ntransactionsamplecode,ntransactiontestcode,nsectioncode,ntechniquecode,"
				+ "nuserrolecode,nusercode,nuserperiodcode,ninstrumentcategorycode,ninstrumentnamecode,ninstrumentcode,ninstrumentperiodcode,ntimezonecode,"
				+ "ntestrescheduleno,jsondata,jsonuidata,nsitecode,nstatus)" + "(select (" + njoballocationseqcount
				+ ")+rank () over(order by rt.ntransactionsamplecode,rt.ntransactiontestcode)  as njoballocationcode,rt.npreregno,rt.ntransactionsamplecode,rt.ntransactiontestcode,"
				+ "rt.nsectioncode," + inputMap.get("ntechniquecode")
				+ " as ntechniquecode,(select nuserrolecode from treetemplatetransactionrole ttr join approvalconfig ac on ac.napprovalconfigcode =ttr.napprovalconfigcode and ac.nregtypecode ="
				+ inputMap.get("nregtypecode") + " and ac.nregsubtypecode=" + inputMap.get("nregsubtypecode")
				+ " and ttr.ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " order by ttr.nlevelno desc LIMIT 1) as nuserrolecode," + inputMap.get("nusercode")
				+ " as nusercode," + " " + inputMap.get("nuserperiodcode") + " as nuserperiodcode,"
				+ inputMap.get("ninstrumentcatcode") + " as ninstrumentcategorycode,"
				+ inputMap.get("ninstrumentnamecode") + " as ninstrumentnamecode," + inputMap.get("ninstrumentcode")
				+ " as ninstrumentcode," + inputMap.get("ninstrumentperiodcode") + " as ninstrumentperiodcode,"
				+ userInfo.getNtimezonecode()
				+ " as ntimezonecode,(select max(ntestrescheduleno) from joballocation where ntransactiontestcode in ("
				+ transactionTestCode + ") and nstatus=1) as ntestrescheduleno,'"
				+ stringUtilityFunction.replaceQuote(jsonObject.toString()) + "'::JSONB,'"
				+ stringUtilityFunction.replaceQuote(jsonUIObject.toString()) + "'::JSONB,"
				+ userInfo.getNtranssitecode() + " as nsitecode,"
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " as nstatus "
				+ " from registrationtest rt,registrationtesthistory rth  "
				+ " where rt.ntransactiontestcode =rth.ntransactiontestcode " + "	and rt.ntransactiontestcode in ("
				+ transactionTestCode + ") and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.ntesthistorycode =any( "
				+ " select max(ntesthistorycode) from registrationtesthistory where ntransactiontestcode in("
				+ transactionTestCode + ") and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by npreregno,ntransactionsamplecode,ntransactiontestcode) and rth.ntransactionstatus in("
				+ ntransactionstatus + ")); ";

		jdbcTemplate.execute(insertJobAllocationQuery);

		String insertTestHistoryQuery = "Insert into registrationtesthistory (ntesthistorycode,nformcode,npreregno,ntransactionsamplecode,ntransactiontestcode,nusercode,nuserrolecode,"
				+ "ndeputyusercode,ndeputyuserrolecode,nsampleapprovalhistorycode,ntransactionstatus,dtransactiondate,noffsetdtransactiondate,ntransdatetimezonecode,"
				+ "scomments,nsitecode,nstatus)" + "(select (" + ntesthistoryseqcount
				+ ")+Rank()over(order by ntesthistorycode) ntesthistorycode," + " " + userInfo.getNformcode()
				+ " as nformcode,rt.npreregno,rt.ntransactionsamplecode,rt.ntransactiontestcode,"
				+ userInfo.getNusercode() + " as nusercode," + userInfo.getNuserrole() + " as nuserrolecode," + " "
				+ userInfo.getNdeputyusercode() + " as ndeputyusercode," + userInfo.getNdeputyuserrole()
				+ " as ndeputyuserrolecode,-1 as nsampleapprovalhistorycode," + ntranscode + " as ntransactionstatus,"
				+ " '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "' dtransactiondate,"
				+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + " noffsetdtransactiondate,"
				+ userInfo.getNtimezonecode() + " ntransdatetimezonecode," + " N'"
				+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "' as scomments,"
				+ userInfo.getNtranssitecode() + " as nsitecode,"
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " as nstatus "
				+ "from registrationtest rt,registrationtesthistory rth  "
				+ "where rt.ntransactiontestcode =rth.ntransactiontestcode and rt.ntransactiontestcode in ("
				+ transactionTestCode + ") and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.ntesthistorycode =any( "
				+ "select max(ntesthistorycode) from registrationtesthistory where ntransactiontestcode in("
				+ transactionTestCode + ") and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by npreregno,ntransactionsamplecode,ntransactiontestcode) and rth.ntransactionstatus in("
				+ ntransactionstatus + "));	";
		jdbcTemplate.execute(insertTestHistoryQuery);
		map = (Map<String, Object>) getJobAllocationTest(inputMap, userInfo);
		List<Map<String, Object>> lstDataTest = AllocationAuditGet(inputMap, userInfo);

		auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
		auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
		auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
		actionType.put("allotanotheruser", "IDS_ALLOTANOTHERUSER");
		jsonAuditObject.put("allotanotheruser", lstDataTest);
		auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);
		return map;
	}

	@SuppressWarnings("unused")
	public Map<String, Object> insertSeqNoReschedule(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		Map<String, Object> map = new HashMap<String, Object>();
		String sQuery = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		sQuery = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		sQuery = " lock  table lockcancelreject " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		sQuery = "lock lockresultusedinstrument" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		int instrumentCatcode = (int) inputMap.get("ninstrumentcatcode");
		String spreregno = (String) inputMap.get("npreregno");
		String transactionSampleCode = (String) inputMap.get("ntransactionsamplecode");
		String transactionTestCode = (String) inputMap.get("ntransactiontestcode");
		int controlCode = (int) inputMap.get("ncontrolcode");
		String ntransactionstatus = (String) inputMap.get("ntransstatus");
		Integer ntesthistoryseqcount;
		Integer njoballocationseqcount;
		Integer nresultinstrumentseqcount = 0;
		Integer testhistorycount;
		Integer joballocationcount;
		Integer resultinstrumentcount;
		List<RegistrationTestHistory> filteredSampleList = validateTestStatus(spreregno, transactionSampleCode,
				transactionTestCode, controlCode, userInfo);
		if (filteredSampleList.isEmpty()) {

			String testCountQuery = "select * from registrationtest rt,registrationtesthistory rth  "
					+ " where rt.ntransactiontestcode =rth.ntransactiontestcode " + "and rt.ntransactiontestcode in ("
					+ transactionTestCode + ") and rt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and rth.ntesthistorycode =any( "
					+ " select max(ntesthistorycode) from registrationtesthistory where ntransactiontestcode in("
					+ transactionTestCode + ") and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " group by npreregno,ntransactionsamplecode, " + "ntransactiontestcode) "
					+ " and rth.ntransactionstatus in(" + ntransactionstatus + ")";
			List<RegistrationTestHistory> lstRegistrationTestHistory = (List<RegistrationTestHistory>) jdbcTemplate
					.query(testCountQuery, new RegistrationTestHistory());
			Integer testCount = lstRegistrationTestHistory.size();

			final List<String> updateSeqnolst = Arrays.asList("joballocation", "registrationtesthistory",
					"resultusedinstrument");

			String jobAllocationSeqCount = "select nsequenceno from  seqnoregistration where stablename ='joballocation' ";
			njoballocationseqcount = jdbcTemplate.queryForObject(jobAllocationSeqCount, Integer.class);
			joballocationcount = njoballocationseqcount + testCount;

			String testHistorySeqCount = "select nsequenceno from  seqnoregistration where stablename ='registrationtesthistory' ";
			ntesthistoryseqcount = jdbcTemplate.queryForObject(testHistorySeqCount, Integer.class);
			testhistorycount = ntesthistoryseqcount + testCount;

			if (instrumentCatcode > 0) {
				String resultInstrumentSeqCount = "select nsequenceno from  seqnoregistration where stablename ='resultusedinstrument' ";
				nresultinstrumentseqcount = jdbcTemplate.queryForObject(resultInstrumentSeqCount, Integer.class);
				resultinstrumentcount = nresultinstrumentseqcount + testCount;

				String updateResultInstrumentQuery = "update seqnoregistration set nsequenceno ="
						+ resultinstrumentcount + " where stablename='resultusedinstrument'";
				jdbcTemplate.execute(updateResultInstrumentQuery);

			}

			String updateJobAllocationQuery = "update seqnoregistration set nsequenceno =" + joballocationcount
					+ " where stablename='joballocation'";
			jdbcTemplate.execute(updateJobAllocationQuery);

			String updateTestQuery = "update seqnoregistration set nsequenceno =" + testhistorycount
					+ " where stablename='registrationtesthistory'";
			jdbcTemplate.execute(updateTestQuery);
			map.put("TransCode", ntransactionstatus);
			map.put("njoballocationseqcount", njoballocationseqcount);
			map.put("nresultinstrumentseqcount", nresultinstrumentseqcount);
			map.put("ntesthistoryseqcount", ntesthistoryseqcount);
			map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		} else {
			map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					commonFunction.getMultilingualMessage("IDS_SELECTVALIDTEST", userInfo.getSlanguagefilename()));
		}

		return map;
	}

	@Override
	@SuppressWarnings({ "unused" })
	public Map<String, Object> RescheduleCreate(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		ObjectMapper objmap = new ObjectMapper();

		int instrumentCatcode = (int) inputMap.get("ninstrumentcatcode");
		int instrumentNamecode = (int) inputMap.get("ninstrumentnamecode");
		int instrumentcode = (int) inputMap.get("ninstrumentcode");
		String spreregno = (String) inputMap.get("npreregno");
		String transactionSampleCode = (String) inputMap.get("ntransactionsamplecode");
		String transactionTestCode = (String) inputMap.get("ntransactiontestcode");

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date userEndDate = null;
		Date instrumentEndDate = null;

		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObject = new JSONObject();
		List<String> dateList = new ArrayList<String>();

		JSONObject jsonObject = new JSONObject(inputMap.get("jsondata").toString());
		JSONObject jsonUIObject = new JSONObject(inputMap.get("jsonuidata").toString());
		JSONObject instrumentjsonObj = new JSONObject();

		String userPeriod = jsonObject.getJSONObject("UserPeriod").getString("label");
		int userHoldDuration = jsonObject.getInt("UserHoldDuration");
		String userStartDate = jsonObject.getString("UserStartDate");

		String instrumentPeriod = jsonObject.getJSONObject("InstrumentPeriod").getString("label");
		int instrumentHoldDuration = jsonObject.getInt("InstrumentHoldDuration");
		String instrumentStartDate = jsonObject.getString("InstrumentStartDate");

		// User Date
		// User Date
		userEndDate = formatter.parse(userStartDate);
		Calendar userEndCalendar = Calendar.getInstance();
		userEndCalendar.setTime(userEndDate);

		if (userPeriod.equals("Day(s)")) {
			userEndCalendar.add(Calendar.DAY_OF_MONTH, userHoldDuration);
		} else if (userPeriod.equals("Hour(s)")) {
			userEndCalendar.add(Calendar.HOUR, userHoldDuration);
		}
		Date UED = userEndCalendar.getTime();
		String UserEndDateTime = formatter.format(UED);
		jsonObject.put("UserEndDate", UserEndDateTime);
		jsonUIObject.put("UserEndDate", UserEndDateTime);

		// Instrument Date
		String InstrumentEndDateTime = "";
		if (!instrumentStartDate.equals("")) {
			instrumentEndDate = formatter.parse(instrumentStartDate);
			Calendar instrumentEndCalendar = Calendar.getInstance();
			instrumentEndCalendar.setTime(instrumentEndDate);

			if (instrumentPeriod.equals("Day(s)")) {
				instrumentEndCalendar.add(Calendar.DAY_OF_MONTH, instrumentHoldDuration);
			} else if (instrumentPeriod.equals("Hour(s)")) {
				instrumentEndCalendar.add(Calendar.HOUR, instrumentHoldDuration);
			}
			Date IED = instrumentEndCalendar.getTime();
			InstrumentEndDateTime = formatter.format(IED);
		}

		jsonObject.put("InstrumentEndDate", InstrumentEndDateTime);
		jsonUIObject.put("InstrumentEndDate", InstrumentEndDateTime);

		JSONObject instrumentcategory = jsonObject.getJSONObject("InstrumentCategory");
		JSONObject instrumentname = jsonObject.getJSONObject("InstrumentName");
		JSONObject instrumentid = jsonObject.getJSONObject("InstrumentId");
		JSONObject sectionname = jsonObject.getJSONObject("Section");

		instrumentStartDate = jsonObject.getString("InstrumentStartDate");

		instrumentjsonObj.put("InstrumentCategory", instrumentcategory);
		instrumentjsonObj.put("InstrumentName", instrumentname);
		instrumentjsonObj.put("InstrumentId", instrumentid);
		instrumentjsonObj.put("InstrumentStartDate", instrumentStartDate);
		instrumentjsonObj.put("InstrumentEndDate", InstrumentEndDateTime);
		instrumentjsonObj.put("Section", sectionname);

		dateList.add("UserStartDate");
		dateList.add("UserEndDate");
		dateList.add("InstrumentStartDate");
		dateList.add("InstrumentEndDate");
		jsonObject = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonObject, dateList, false,
				userInfo);

		Integer ntranscode = Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus();
		String ntransactionstatus = (String) inputMap.get("TransCode");
		Integer njoballocationseqcount = (Integer) inputMap.get("njoballocationseqcount");
		Integer nresultinstrumentseqcount = (Integer) inputMap.get("nresultinstrumentseqcount");
		Integer ntesthistoryseqcount = (Integer) inputMap.get("ntesthistoryseqcount");

		String sectionameQuery = "select ssectionname from section where nsectioncode ="
				+ String.valueOf(inputMap.get("nsectioncode")) + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		String sectioName = jdbcTemplate.queryForObject(sectionameQuery, String.class);

		String updateQuery = "update registrationtest  set nsectioncode ="
				+ String.valueOf(inputMap.get("nsectioncode")) + "," + " jsondata = jsondata || '{\"ssectionname\": \""
				+ sectioName + "\"}' where npreregno in(" + spreregno + ") and " + " ntransactionsamplecode in ("
				+ transactionSampleCode + ") and ntransactiontestcode in(" + transactionTestCode + ")";

		jdbcTemplate.execute(updateQuery);
		String existsSection = "select * from registrationsection where npreregno in(" + spreregno + ") "
				+ " and nsectioncode =" + String.valueOf(inputMap.get("nsectioncode")) + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNtranssitecode() + "";

		List<RegistrationSection> sampleList = (List<RegistrationSection>) jdbcTemplate.query(existsSection,
				new RegistrationSection());

		List<String> list1 = Arrays.asList(spreregno.split(","));

		List<String> list = list1.stream()
				.filter(a -> sampleList.stream().noneMatch(b -> b.getNpreregno() == Integer.parseInt(a)))
				.collect(Collectors.toList());

		Set<String> setFromStream = list.stream().collect(Collectors.toSet());

		if (!setFromStream.isEmpty()) {
			Integer nsectionhistoryseqcount;
			Integer nsectionseqcount;

			String str1 = "select nsequenceno from seqnoregistration where stablename ='registrationsectionhistory'";
			nsectionhistoryseqcount = jdbcTemplate.queryForObject(str1, Integer.class);

			String str2 = "select nsequenceno from seqnoregistration where stablename ='registrationsection'";
			nsectionseqcount = jdbcTemplate.queryForObject(str2, Integer.class);

			nsectionhistoryseqcount = nsectionhistoryseqcount + 1;
			nsectionseqcount = nsectionseqcount + 1;
			String insertSectionQuery = "";
			if (setFromStream.size() > 0) {
				for (String samplearno : setFromStream) {
					insertSectionQuery = insertSectionQuery + "INSERT INTO public.registrationsection("
							+ "	nregistrationsectioncode, npreregno, nsectioncode, nsitecode, nstatus)" + "	VALUES ("
							+ nsectionseqcount + ", " + samplearno + ", " + String.valueOf(inputMap.get("nsectioncode"))
							+ ", " + userInfo.getNtranssitecode() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

					insertSectionQuery = insertSectionQuery
							+ "Insert into registrationsectionhistory (nsectionhistorycode,npreregno,nsectioncode,ntransactionstatus,nsitecode,nstatus)"
							+ " values(" + nsectionhistoryseqcount + "," + samplearno + ","
							+ String.valueOf(inputMap.get("nsectioncode")) + "," + ""
							+ Enumeration.TransactionStatus.RECIEVED_IN_SECTION.gettransactionstatus() + ","
							+ userInfo.getNtranssitecode() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
					nsectionhistoryseqcount++;
					nsectionseqcount++;
				}

				insertSectionQuery = insertSectionQuery + "update seqnoregistration set nsequenceno ="
						+ nsectionseqcount + " where stablename='registrationsection';";

				insertSectionQuery = insertSectionQuery + "update seqnoregistration set nsequenceno ="
						+ nsectionhistoryseqcount + " where stablename='registrationsectionhistory';";

				jdbcTemplate.execute(insertSectionQuery);
				inputMap.put("nsectioncode", inputMap.get("nsectioncode"));
				JSONObject actionTypeRecevieInLab = new JSONObject();

				List<Map<String, Object>> lstDataTest = testAuditGet(inputMap, userInfo);
				actionTypeRecevieInLab.put("registrationtest", "IDS_RECEIVEINLAB");
				JSONObject jsonReceiveObject = new JSONObject();

				auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
				auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
				auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
				jsonReceiveObject.put("registrationtest", lstDataTest);
				auditUtilityFunction.fnJsonArrayAudit(jsonReceiveObject, null, actionTypeRecevieInLab, auditmap, false,
						userInfo);

			}
		}

		String insertJobAllocationQuery = "Insert into joballocation(njoballocationcode,npreregno,ntransactionsamplecode,ntransactiontestcode,nsectioncode,ntechniquecode,"
				+ "nuserrolecode,nusercode,nuserperiodcode,ninstrumentcategorycode,ninstrumentcode,ninstrumentnamecode,ninstrumentperiodcode,ntimezonecode,"
				+ "ntestrescheduleno,jsondata,jsonuidata,nsitecode,nstatus)" + "(select (" + njoballocationseqcount
				+ ")+rank () over(order by rt.ntransactionsamplecode,rt.ntransactiontestcode)  as njoballocationcode,rt.npreregno,rt.ntransactionsamplecode,rt.ntransactiontestcode,"
				+ "rt.nsectioncode," + inputMap.get("ntechniquecode")
				+ " as ntechniquecode,(select nuserrolecode from treetemplatetransactionrole ttr join approvalconfig ac on ac.napprovalconfigcode =ttr.napprovalconfigcode and ac.nregtypecode ="
				+ inputMap.get("nregtypecode") + " and ac.nregsubtypecode=" + inputMap.get("nregsubtypecode")
				+ " and ttr.ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " order by ttr.nlevelno desc LIMIT 1) as nuserrolecode," + inputMap.get("nusercode")
				+ " as nusercode, " + inputMap.get("nuserperiodcode") + " as nuserperiodcode,"
				+ inputMap.get("ninstrumentcatcode") + " as ninstrumentcategorycode," + inputMap.get("ninstrumentcode")
				+ " as ninstrumentcode," + instrumentNamecode + " as ninstrumentnamecode," + " "
				+ inputMap.get("ninstrumentperiodcode") + " as ninstrumentperiodcode," + userInfo.getNtimezonecode()
				+ " as ntimezonecode, (select (max(ja.ntestrescheduleno))+rank () over (order by rt.ntransactionsamplecode,rt.ntransactiontestcode))as ntestrescheduleno,'"
				+ stringUtilityFunction.replaceQuote(jsonObject.toString()) + "'::JSONB,'"
				+ stringUtilityFunction.replaceQuote(jsonUIObject.toString()) + "'::JSONB,"
				+ userInfo.getNtranssitecode() + " as nsitecode,"
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " as nstatus "
				+ " from registrationtest rt,registrationtesthistory rth,joballocation ja  "
				+ " where rt.ntransactiontestcode =rth.ntransactiontestcode  and ja.ntransactiontestcode =rt.ntransactiontestcode "
				+ "	and rt.ntransactiontestcode in (" + transactionTestCode + ") and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.ntesthistorycode =any( "
				+ " select max(ntesthistorycode) from registrationtesthistory where ntransactiontestcode in("
				+ transactionTestCode + ") and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by npreregno,ntransactionsamplecode,ntransactiontestcode) and rth.ntransactionstatus in("
				+ ntransactionstatus
				+ ") group by rt.npreregno,rt.ntransactionsamplecode,rt.ntransactiontestcode,rt.nsectioncode); ";
		jdbcTemplate.execute(insertJobAllocationQuery);

		if (instrumentcode > 0) {
			String insertResultInstrumentQuery = "insert into resultusedinstrument "
					+ "(nresultusedinstrumentcode,ntransactiontestcode,npreregno,ninstrumentcatcode,ninstrumentcode,ninstrumentnamecode,dfromdate,dtodate,ntzfromdate,ntztodate,noffsetdfromdate,noffsetdtodate,jsondata,nsitecode,nstatus) "
					+ "(select (" + nresultinstrumentseqcount
					+ ")+Rank() over(order by rt.ntransactionsamplecode,rt.ntransactiontestcode) as nresultusedinstrumentcode,rt.ntransactiontestcode,rt.npreregno,"
					+ "" + inputMap.get("ninstrumentcatcode") + " as ninstrumentcatcode,"
					+ inputMap.get("ninstrumentcode") + " as ninstrumentcode," + instrumentNamecode
					+ " as ninstrumentnamecode," + " '" + instrumentStartDate + "' as dfromdate,'"
					+ InstrumentEndDateTime + "' as dtodate," + userInfo.getNtimezonecode() + " as ntzfromdate,"
					+ userInfo.getNtimezonecode() + " as ntztodate,"
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ",'"
					+ stringUtilityFunction.replaceQuote(instrumentjsonObj.toString()) + "'::JSONB ,"
					+ userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " as nstatus " + "from registrationtest rt,registrationtesthistory rth   "
					+ " where rt.ntransactiontestcode =rth.ntransactiontestcode  " + "and rt.ntransactiontestcode in ("
					+ transactionTestCode + ") and rt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and rth.ntesthistorycode =any(  "
					+ " select max(ntesthistorycode) from registrationtesthistory where ntransactiontestcode in("
					+ transactionTestCode + ") and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " group by npreregno,ntransactionsamplecode,ntransactiontestcode)  "
					+ " q	and rth.ntransactionstatus in (" + ntransactionstatus + "));";
			jdbcTemplate.execute(insertResultInstrumentQuery);
		}

		String insertTestHistoryQuery = "Insert into registrationtesthistory (ntesthistorycode,nformcode,npreregno,ntransactionsamplecode,ntransactiontestcode,nusercode,nuserrolecode,"
				+ "ndeputyusercode,ndeputyuserrolecode,nsampleapprovalhistorycode,ntransactionstatus,dtransactiondate,noffsetdtransactiondate,ntransdatetimezonecode,"
				+ "scomments,nsitecode,nstatus)" + "(select (" + ntesthistoryseqcount
				+ ")+Rank()over(order by ntesthistorycode) ntesthistorycode," + " " + userInfo.getNformcode()
				+ " as nformcode,rt.npreregno,rt.ntransactionsamplecode,rt.ntransactiontestcode,"
				+ userInfo.getNusercode() + " as nusercode," + userInfo.getNuserrole() + " as nuserrolecode," + " "
				+ userInfo.getNdeputyusercode() + " as ndeputyusercode," + userInfo.getNdeputyuserrole()
				+ " as ndeputyuserrolecode,-1 as nsampleapprovalhistorycode," + ntranscode + " as ntransactionstatus,"
				+ " '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "' dtransactiondate,"
				+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + " noffsetdtransactiondate,"
				+ userInfo.getNtimezonecode() + " ntransdatetimezonecode," + " N'"
				+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "' as scomments,"
				+ userInfo.getNtranssitecode() + " as nsitecode,"
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " as nstatus "
				+ " from registrationtest rt,registrationtesthistory rth  "
				+ " where rt.ntransactiontestcode =rth.ntransactiontestcode " + "and rt.ntransactiontestcode in ("
				+ transactionTestCode + ") and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.ntesthistorycode =any( "
				+ "select max(ntesthistorycode) from registrationtesthistory where ntransactiontestcode in("
				+ transactionTestCode + ") and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by npreregno,ntransactionsamplecode,ntransactiontestcode) and rth.ntransactionstatus in("
				+ ntransactionstatus + "));	";
		jdbcTemplate.execute(insertTestHistoryQuery);

		List<Map<String, Object>> lstDataTest = AllocationAuditGet(inputMap, userInfo);
		auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
		auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
		auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
		actionType.put("joballocation", "IDS_RESCHEDULEJOB");
		jsonAuditObject.put("joballocation", lstDataTest);
		auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);
//ALPD-3781
		if (String.valueOf(inputMap.get("nfiltersectioncode")) != String.valueOf(inputMap.get("nsectioncode"))) {
			inputMap.put("ntransactiontestcode", "0");

		}
		inputMap.put("nsectioncode", inputMap.get("nfiltersectioncode"));

		map = (Map<String, Object>) getJobAllocationTest(inputMap, userInfo);
		map.putAll((Map<String, Object>) getFilterSection(inputMap, userInfo));

		return map;

	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> viewAnalystCalendar(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();

		String ntransactiontestcode = (String) inputMap.get("transactiontestcode");
		Integer ntransactionstatus = 20;
		String strUserQuery = "select su.nusercode,u.sfirstname || u.slastname as susername "
				+ " from registrationtest rt "
				+ " join registrationtesthistory rth on rt.ntransactiontestcode =rth.ntransactiontestcode "
				+ " join labsection ls on ls.nsectioncode=rt.nsectioncode "
				+ " join sectionusers su on su.nlabsectioncode =su.nlabsectioncode "
				+ " join trainingparticipants tp on tp.nusercode=su.nusercode "
				+ " join users u on su.nusercode=u.nusercode " + "join userssite us on us.nusercode =u.nusercode "
				+ " join usermultirole urm on us.nusersitecode=urm.nusersitecode "
				+ " join userrole ur on urm.nuserrolecode =ur.nuserrolecode "
				+ " where  urm.nuserrolecode =(select ttr.nuserrolecode from treetemplatetransactionrole ttr,approvalconfig ac where  "
				+ " ttr.napprovalconfigcode =ac.napprovalconfigcode and ac.nregtypecode=" + inputMap.get("nregtypecode")
				+ " and ac.nregsubtypecode=" + inputMap.get("nregsubtypecode") + "  " + "and ttr.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " order by ttr.nlevelno desc limit 1) " + "and tp.ncertifiedstatus ="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and tp.ncompetencystatus="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and tp.ntransactionstatus="
				+ Enumeration.TransactionStatus.ATTENDED.gettransactionstatus() + " "
				+ "and rt.ntransactiontestcode in(" + ntransactiontestcode + ") " + "and rth.ntransactionstatus=("
				+ ntransactionstatus + ") " + "and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ls.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and su.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and us.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and urm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by su.nusercode,susername";

		List<Users> lst = jdbcTemplate.query(strUserQuery, new Users());
		map.put("Users", lst);
		if (lst.size() > 0) {
			int userCode = lst.get(0).getNusercode();
			map.put("nusercode", userCode);
			map.put("view", "month");
			map.put("days", 1);

			map.putAll((Map<String, Object>) getUserScheduleCombo(map, userInfo).getBody());
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getUserScheduleCombo(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();

		Integer nuserCode = (int) inputMap.get("nusercode");

		final String Date = (String) inputMap.get("startDate");
		final String viewName = (String) inputMap.get("view");

		final Integer days = (Integer) inputMap.get("days");

		String query = "";
		final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		if (viewName.equals("day") || viewName.equals("timeline")) {

			LocalDate startDate = LocalDate.parse(Date, dbPattern);
			LocalDate endDate = LocalDate.parse(Date, dbPattern).plusDays(days);

			query = " and (cast(jb.jsondata->>'UserStartDate'  as timestamp) <='" + endDate
					+ "' and cast(jb.jsondata->>'UserEndDate'  as timestamp)>='" + startDate + "')";

		} else if (viewName.equals("month")) {

			LocalDate startDate = YearMonth.parse(Date, dbPattern).atDay(1);
			LocalDate endDate = YearMonth.parse(Date, dbPattern).atEndOfMonth();

			query = " and (cast(jb.jsondata->>'UserStartDate'  as timestamp) <='" + endDate
					+ "' and cast(jb.jsondata->>'UserEndDate'  as timestamp)>='" + startDate + "')";

		} else if (viewName.equals("week") || viewName.equals("agenda")) {

			LocalDate startDate = LocalDate.parse(Date, dbPattern)
					.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));

			LocalDate endDate = LocalDate.parse(Date, dbPattern).with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));

			query = " and (cast(jb.jsondata->>'UserStartDate'  as timestamp) <='" + endDate
					+ "' and cast(jb.jsondata->>'UserEndDate'  as timestamp)>='" + startDate + "')";

		} else {
			LocalDate startDate = LocalDate.parse(Date, dbPattern);
			query = " and cast(jb.jsondata->>'UserStartDate'  as timestamp) = '" + startDate + "'";
		}

		String str1 = "";

		str1 = "select scalendersettingvalue from calendersettings where ncalendersettingcode=49 and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		String ssettingValue = jdbcTemplate.queryForObject(str1, String.class);

		if (ssettingValue.equals("3")) {
			str1 = " select  " + ssettingValue
					+ " as grouping, max(rth.ntransactionstatus) ntransactionstatus, max(jb.nusercode) nusercode, "
					+ " coalesce(max(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
					+ "')) as stransdisplaystatus,  max(rt.jsondata->>'stestsynonym') stestsynonym, "
					+ " to_char(cast(max(jb.jsondata->>'UserStartDate')  as timestamp),'"
					+ userInfo.getSpgdatetimeformat() + "') userstartdate, "
					+ " to_char(cast(jb.jsondata->>'UserStartDate'  as timestamp),'" + userInfo.getSpgdatetimeformat()
					+ "' ) userstartdategroup, 	"
					+ " count(rt.ntransactiontestcode) ncount, to_char(cast(max(jb.jsondata->>'UserEndDate') as timestamp),'"
					+ userInfo.getSpgdatetimeformat() + "') userenddate, "
					+ " case when  max(jb.jsondata->>'InstrumentStartDate')!=''  then to_char(cast(max(jb.jsondata->>'InstrumentStartDate') as timestamp), '"
					+ userInfo.getSpgdatetimeformat() + "')"
					+ " else '' end instrumentstartdate, case when  max(jb.jsondata->>'InstrumentStartDate')!=''  "
					+ " then to_char(cast(max(jb.jsondata->>'InstrumentEndDate') as timestamp),'"
					+ userInfo.getSpgdatetimeformat() + "') else '' end instrumentenddate, "
					+ " max(jb.jsondata->>'offsetUserStartDate') offsetduserstartdate, max(jb.jsondata->>'offsetUserEndDate') offsetduserenddate, "
					+ " max(jb.jsondata->>'offsetInstrumentStartDate') offsetdinstrumentstartdate,"
					+ " max(jb.jsondata->>'offsetInstrumentEndDate') offsetdinstrumentenddate  , "
					+ " max(jb.jsondata->>'Comments') \"comments\", max(jb.jsondata->'Instrument'->>'value')::int ninstrumentcode,"
					+ " max(jb.jsondata->'InstrumentCategory'->>'value')::int ninstrumentcategorycode, max(jb.jsondata->>'UserStartDate') userstartdatejson, "
					+ " max(jb.jsondata->>'UserEndDate') userenddatejson " + " from registrationtest rt, "
					+ " registrationtesthistory rth,joballocation jb, transactionstatus ts where  "
					+ " njoballocationcode in ( select max(njoballocationcode) from joballocation "
					+ " group by ntransactiontestcode ) " + " and  rt.ntransactiontestcode =rth.ntransactiontestcode "
					+ " and rt.ntransactiontestcode=jb.ntransactiontestcode "
					+ " and rth.ntesthistorycode in (select max(ntesthistorycode) from registrationtesthistory "
					+ " group by npreregno,ntransactionsamplecode,ntransactiontestcode ) "
					+ " and ts.ntranscode=rth.ntransactionstatus and jb.nusercode =" + nuserCode
					+ "  and rth.ntransactionstatus in (" + Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus()
					+ "," + Enumeration.TransactionStatus.INITIATED.gettransactionstatus() + ","
					+ Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus() + ") and rt.nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + query
					+ " group by userstartdategroup,rt.ntestcode ";

		} else {

			str1 = "   select  " + ssettingValue
					+ " as grouping, rth.ntransactionstatus ntransactionstatus, jb.nusercode nusercode, "
					+ "  coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
					+ "') as stransdisplaystatus,  rt.jsondata->>'stestsynonym' stestsynonym,"
					+ "  to_char(cast(jb.jsondata->>'UserStartDate'  as timestamp),'" + userInfo.getSpgdatetimeformat()
					+ "') userstartdate, " + "  to_char(cast(jb.jsondata->>'UserStartDate'  as timestamp),'"
					+ userInfo.getSpgdatetimeformat() + "' ) userstartdategroup, 	"
					+ "  to_char(cast(jb.jsondata->>'UserEndDate' as timestamp),'" + userInfo.getSpgdatetimeformat()
					+ "') userenddate, "
					+ "  case when  jb.jsondata->>'InstrumentStartDate'!=''  then to_char(cast(jb.jsondata->>'InstrumentStartDate' as timestamp), '"
					+ userInfo.getSpgdatetimeformat() + "') "
					+ "  else '' end instrumentstartdate, case when  jb.jsondata->>'InstrumentStartDate'!=''  "
					+ "  then to_char(cast(jb.jsondata->>'InstrumentEndDate' as timestamp),'"
					+ userInfo.getSpgdatetimeformat() + "') else '' end instrumentenddate, "
					+ " jb.jsondata->>'offsetUserStartDate' offsetduserstartdate, jb.jsondata->>'offsetUserEndDate' offsetduserenddate, "
					+ " jb.jsondata->>'offsetInstrumentStartDate' offsetdinstrumentstartdate, "
					+ " jb.jsondata->>'offsetInstrumentEndDate' offsetdinstrumentenddate  , "
					+ " jb.jsondata->>'Comments' \"comments\", cast(jb.jsondata->'Instrument'->>'value' as int) ninstrumentcode, "
					+ " cast(jb.jsondata->'InstrumentCategory'->>'value' as int) ninstrumentcategorycode, jb.jsondata->>'UserStartDate' userstartdatejson, "
					+ " jb.jsondata->>'UserEndDate' userenddatejson,arno.sarno,rno.ssamplearno "
					+ " from registrationtest rt,registrationarno arno,registrationsamplearno rno,  "
					+ " registrationtesthistory rth,joballocation jb,users u, transactionstatus ts "
					+ " where  njoballocationcode in ( select max(jb1.njoballocationcode) from joballocation jb1  "
					+ " group by jb1.ntransactiontestcode ) "
					+ " and rth.ntesthistorycode in (select max(rt1.ntesthistorycode) from registrationtesthistory  rt1 "
					+ " group by rt1.npreregno,rt1.ntransactionsamplecode,rt1.ntransactiontestcode ) "
					+ " and  rt.ntransactiontestcode =rth.ntransactiontestcode and arno.npreregno=rt.npreregno"
					+ " and  rno.npreregno=rt.npreregno  and rt.ntransactiontestcode=jb.ntransactiontestcode "
					+ " and jb.nusercode=u.nusercode and ts.ntranscode=rth.ntransactionstatus "
					+ " and rno.ntransactionsamplecode=rt.ntransactionsamplecode and u.nusercode =" + nuserCode
					+ "  and rth.ntransactionstatus in ("
					+ +Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus() + ","
					+ Enumeration.TransactionStatus.INITIATED.gettransactionstatus() + ","
					+ Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus() + ") and jb.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + query;

		}

		List<JobAllocation> lst = jdbcTemplate.query(str1, new JobAllocation());

		str1 = " select c.* from holidaysyear y ,holidayyearversion v ,commonholidays c"
				+ " where y.nyearcode=v.nyearcode and v.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and "
				+ " c.nholidayyearversion=v.nholidayyearversion  and syear=to_char(cast('" + Date
				+ "' as timestamp),'yyyy');";

		List<Map<String, Object>> commonHolidays = jdbcTemplate.queryForList(str1);

		str1 = " select c.*,c.ddate as dcalenderholidaystartdate,c.ddate+(1439 * interval '1 minute') as dcalenderholidayenddate from holidaysyear y ,holidayyearversion v ,publicholidays c"
				+ " where y.nyearcode=v.nyearcode and v.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and "
				+ " c.nholidayyearversion=v.nholidayyearversion  and syear=to_char(cast('" + Date
				+ "' as timestamp),'yyyy');";

		List<Map<String, Object>> publicHolidays = jdbcTemplate.queryForList(str1);

		map.put("calenderPublicHolidays", publicHolidays);
		map.put("calenderCommonHolidays", commonHolidays);

		str1 = " select *,ddate as dcalenderholidaystartdate,ddate+(1439 * interval '1 minute') as dcalenderholidayenddate from  userbasedholidays where nusercode="
				+ nuserCode + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode=" + userInfo.getNmastersitecode();

		List<Map<String, Object>> calenderusersholidays = jdbcTemplate.queryForList(str1);

		map.put("UserBasedHolidays", calenderusersholidays);

		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final List<JobAllocation> lstUTCConvertedDate = objMapper
				.convertValue(
						dateUtilityFunction.getSiteLocalTimeFromUTC(lst,
								Arrays.asList("userstartdate", "userenddate", "instrumentstartdate",
										"instrumentenddate"),
								null, userInfo, false, null, false),
						new TypeReference<List<JobAllocation>>() {
						});
		map.put("UserData", lstUTCConvertedDate);

		return new ResponseEntity<>(map, HttpStatus.OK);

	}

	@SuppressWarnings("unused")
	public Map<String, Object> insertSeqNoCancelRegistration(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		String sQuery = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		sQuery = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		sQuery = "lock table lockcancelreject " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		Map<String, Object> map = new HashMap<String, Object>();
		String spreregno = (String) inputMap.get("npreregno");
		String transactionSampleCode = (String) inputMap.get("ntransactionsamplecode");
		String transactionTestCode = (String) inputMap.get("ntransactiontestcode");
		int controlCode = (int) inputMap.get("ncontrolcode");
		String ntransactionstatus;
		Integer ntesthistoryseqcount;
		Integer testhistorycount;

		List<RegistrationTestHistory> filteredSampleList = validateTestCancelStatus(spreregno, transactionSampleCode,
				transactionTestCode, controlCode, userInfo);
		if (!filteredSampleList.isEmpty()) {

			ntransactionstatus = filteredSampleList.stream().map(obj -> String.valueOf(obj.getNtransactionstatus()))
					.collect(Collectors.joining(","));

			String testCountQuery = "select rt.ntransactiontestcode from registrationtesthistory rth  "
					+ " join registrationtest rt on rt.ntransactiontestcode = rth.ntransactiontestcode  "
					+ " and rt.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and rth.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and rth.ntesthistorycode =any( select max(ntesthistorycode) from registrationtesthistory  "
					+ "	where nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and npreregno in (" + spreregno + ")  "
					+ " group by npreregno,ntransactionsamplecode,ntransactiontestcode) "
					+ " and rth.ntransactionstatus in(" + ntransactionstatus + ") and rt.npreregno in (" + spreregno
					+ ") and rt.ntransactionsamplecode in (" + transactionSampleCode + ")"
					+ "  and rt.ntransactiontestcode in(" + transactionTestCode + ")";
			List<RegistrationTestHistory> lstRegistrationTestHistory = (List<RegistrationTestHistory>) jdbcTemplate
					.query(testCountQuery, new RegistrationTestHistory());
			Integer testCount = lstRegistrationTestHistory.size();

			final List<String> updateSeqnolst = Arrays.asList("registrationtesthistory");

			String str = "select nsequenceno from seqnoregistration where stablename ='registrationtesthistory'";
			ntesthistoryseqcount = jdbcTemplate.queryForObject(str, Integer.class);

			testhistorycount = ntesthistoryseqcount + testCount;

			String updateTestQuery = "update seqnoregistration set nsequenceno =" + testhistorycount
					+ " where stablename='registrationtesthistory'";
			jdbcTemplate.execute(updateTestQuery);

			map.put("TransCode", ntransactionstatus);
			map.put("ntesthistoryseqcount", ntesthistoryseqcount);
			map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		} else {
			map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), commonFunction
					.getMultilingualMessage("IDS_SELECTREGISTEREDSAMPLES", userInfo.getSlanguagefilename()));
		}
		return map;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> cancelTest(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		Map<String, Object> returnMap = new HashMap<String, Object>();
		String spreregno = (String) inputMap.get("npreregno");
		String transactionSampleCode = (String) inputMap.get("ntransactionsamplecode");
		String transactionTestCode = (String) inputMap.get("ntransactiontestcode");

		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditOld = new JSONObject();
		JSONObject jsonAuditNew = new JSONObject();

		List<Map<String, Object>> lstDataTest = testAuditGet(inputMap, userInfo);

		Integer ntranscode = Enumeration.TransactionStatus.CANCELED.gettransactionstatus();
		String ntransactionstatus = (String) inputMap.get("TransCode");
		Integer ntesthistoryseqcount = (Integer) inputMap.get("ntesthistoryseqcount");
		String insertTestQuery = "Insert into registrationtesthistory (ntesthistorycode,nformcode,npreregno,ntransactionsamplecode,ntransactiontestcode,nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode, "
				+ "nsampleapprovalhistorycode,ntransactionstatus,dtransactiondate,noffsetdtransactiondate,ntransdatetimezonecode,scomments,nsitecode,nstatus) "
				+ "((select (" + ntesthistoryseqcount + ")+Rank()over(order by ntesthistorycode) ntesthistorycode,"
				+ userInfo.getNformcode()
				+ " nformcode ,rt.npreregno,rt.ntransactionsamplecode,rt.ntransactiontestcode," + ""
				+ userInfo.getNusercode() + " nusercode, " + userInfo.getNuserrole() + " nuserrolecode,"
				+ userInfo.getNdeputyusercode() + " ndeputyusercode," + userInfo.getNdeputyuserrole()
				+ " ndeputyuserrolecode," + "-1 nsampleapprovalhistorycode," + ntranscode + " ntransactionstatus, '"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' dtransactiondate,"
				+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + " noffsetdtransactiondate,"
				+ userInfo.getNtimezonecode() + " ntransdatetimezonecode,  " + " N'"
				+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "' scomments,"
				+ userInfo.getNtranssitecode() + " as nsitecode,"
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus   "
				+ "from registrationtesthistory rth  join registrationtest rt on rt.ntransactiontestcode = rth.ntransactiontestcode   "
				+ " and rt.npreregno in (" + spreregno + ") and rt.ntransactionsamplecode in(" + transactionSampleCode
				+ ") and rt.ntransactiontestcode in (" + transactionTestCode + ") and rt.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  "
				+ " and rth.ntesthistorycode =any( select max(ntesthistorycode) from registrationtesthistory   "
				+ " where nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and npreregno in(" + spreregno + ") and rt.ntransactionsamplecode in(" + transactionSampleCode
				+ ") and rt.ntransactiontestcode in (" + transactionTestCode
				+ ") group by npreregno,ntransactionsamplecode,ntransactiontestcode) and rth.ntransactionstatus in("
				+ ntransactionstatus + ")))";
		jdbcTemplate.execute(insertTestQuery);

		returnMap = (Map<String, Object>) getJobAllocationTest(inputMap, userInfo);

		jsonAuditOld.put("registrationtest", lstDataTest);
		jsonAuditNew.put("registrationtest", (List<Map<String, Object>>) returnMap.get("JA_TEST"));
		auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
		auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
		auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
		actionType.put("registrationtest", "IDS_CANCELTEST");
		auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, jsonAuditNew, actionType, auditmap, false, userInfo);
		return returnMap;
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> sampleAuditGet(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		List<Map<String, Object>> sampleList = new ArrayList<>();

		final short sampleTypeCode = Short.parseShort(String.valueOf(inputMap.get("nsampletypecode")));
		final short regTypeCode = Short.parseShort(String.valueOf(inputMap.get("nregtypecode")));
		final short regSubTypeCode = Short.parseShort(String.valueOf(inputMap.get("nregsubtypecode")));
		final short approveVersionCode = Short.parseShort(String.valueOf(inputMap.get("napprovalversioncode")));
		final short designTemplateMappingCode = Short
				.parseShort(String.valueOf(inputMap.get("ndesigntemplatemappingcode")));
		final String sectionCode = (String) inputMap.get("ssectioncode");
		final short userCode = (short) userInfo.getNusercode();
		final short siteCode = userInfo.getNtranssitecode();
		final String transCode = (String) inputMap.get("ntransactionstatus");
		final String languageTypeCode = userInfo.getSlanguagetypecode();

		final String getSampleQuery = "SELECT * from fn_joballocationsampleget('" + inputMap.get("fromdate") + "','"
				+ inputMap.get("todate") + "'," + "" + sampleTypeCode + "," + regTypeCode + "," + regSubTypeCode + ","
				+ approveVersionCode + "," + designTemplateMappingCode + ", " + "'" + sectionCode + "','" + transCode
				+ "'," + userCode + "," + siteCode + ",'" + languageTypeCode + "');";
		LOGGER.info(getSampleQuery);
		String sample = jdbcTemplate.queryForObject(getSampleQuery, String.class);
		if (sample != null) {
			sampleList = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(sample,
					userInfo, true, (int) inputMap.get("ndesigntemplatemappingcode"), "sample");
		}
		return sampleList;
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> testAuditGet(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		List<Map<String, Object>> testList = new ArrayList<>();
		final String npreregno = (String) inputMap.get("npreregno");
		final String ntransactionsamplecode = (String) inputMap.get("ntransactionsamplecode");
		final String nsectioncode = (String) inputMap.get("nsectioncode");
		final String ntestcode = (String) inputMap.get("ntestcode");
		final Short siteCode = userInfo.getNtranssitecode();

		String transCode = "";
		String transactionTestCode = "0";
		if (!inputMap.containsKey("ntransactionstatus")) {
			transCode = "0";
		} else if (inputMap.containsKey("ntransactionstatus")) {
			transCode = (String) inputMap.get("ntransactionstatus");
		}
		if (inputMap.containsKey("ntransactiontestcode")) {
			transactionTestCode = (String) inputMap.get("ntransactiontestcode");
		}

		final String getSampleQuery = "SELECT * from fn_joballocationtestget('" + npreregno + "', '"
				+ ntransactionsamplecode + "','" + transactionTestCode + "','" + nsectioncode + "','" + ntestcode
				+ "','" + transCode + "'," + siteCode + ",'" + userInfo.getSlanguagetypecode() + "')";

		LOGGER.info(getSampleQuery);
		String testListString = jdbcTemplate.queryForObject(getSampleQuery, String.class);
		if (testListString != null) {
			testList = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(
					testListString, userInfo, true, (int) inputMap.get("ndesigntemplatemappingcode"), "test");
		}
		return testList;
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> AllocationAuditGet(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		List<Map<String, Object>> testList = new ArrayList<>();
		String spreregno = (String) inputMap.get("npreregno");
		String transactionSampleCode = (String) inputMap.get("ntransactionsamplecode");
		String transactionTestCode = (String) inputMap.get("ntransactiontestcode");
		int userCode = (int) inputMap.get("nusercode");
		final String getSampleQuery = "select json_agg(a.jsonuidata)  from ( " + "select  ja.jsonuidata "
				+ "|| json_build_object('sarno',ra.sarno)::jsonb "
				+ "|| json_build_object('ssamplearno',rsa.ssamplearno)::jsonb "
				+ "|| json_build_object('stestsynonym',tgt.stestsynonym )::jsonb "
				+ "|| json_build_object('njoballocationcode',ja.njoballocationcode )::jsonb as jsonuidata  from joballocation ja "
				+ " join registrationarno ra on ra.npreregno =ja.npreregno "
				+ " join registrationsamplearno rsa on rsa.ntransactionsamplecode =ja.ntransactionsamplecode "
				+ " join registrationtest rt on rt.ntransactiontestcode=ja.ntransactiontestcode "
				+ " join testgrouptest tgt on tgt.ntestgrouptestcode=rt.ntestgrouptestcode " + "where ja.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ra.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rsa.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ja.npreregno in(" + spreregno
				+ ") and ja.ntransactionsamplecode in(" + transactionSampleCode + ") and ja.ntransactiontestcode in ("
				+ transactionTestCode + ") and ja.nusercode=(" + userCode
				+ ") and ja.ntestrescheduleno=(SELECT   Max(ntestrescheduleno) from joballocation where npreregno in("
				+ spreregno + ") and ntransactionsamplecode in(" + transactionSampleCode
				+ ") and ntransactiontestcode in (" + transactionTestCode + ") and nusercode=(" + userCode
				+ "))  order by ja.npreregno,ja.ntransactionsamplecode,ja.ntransactiontestcode asc  )a ";
		String testListString = jdbcTemplate.queryForObject(getSampleQuery, String.class);
		if (testListString != null) {
			testList = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(
					testListString, userInfo, true, (int) inputMap.get("ndesigntemplatemappingcode"), "test");
		}
		return testList;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getTestView(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		List<Map<String, Object>> testList = new ArrayList<>();
		final String npreregno = (String) inputMap.get("npreregno");
		final String ntransactionsamplecode = (String) inputMap.get("ntransactionsamplecode");
		final String ntransactiontestcode = (String) inputMap.get("ntransactiontestcode");
		final String nsectioncode = (String) inputMap.get("nsectioncode");
		final Short siteCode = userInfo.getNtranssitecode();

		final String getSampleQuery = "SELECT * from fn_joballocationtestviewget('" + npreregno + "', '"
				+ ntransactionsamplecode + "','" + ntransactiontestcode + "','" + nsectioncode + "'," + siteCode + ",'"
				+ userInfo.getSlanguagetypecode() + "')";

		LOGGER.info(getSampleQuery);
		String testListString = jdbcTemplate.queryForObject(getSampleQuery, String.class);
		if (testListString != null) {
			testList = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(
					testListString, userInfo, true, (int) inputMap.get("ndesigntemplatemappingcode"), "test");
		}
		map.put("TestView", testList);
		return map;
	}

	private Map<String, Object> getActiveSampleTabData(String npreregno, String activeSampleTab, UserInfo userInfo)
			throws Exception {

		switch (activeSampleTab) {
		case "IDS_SAMPLEATTACHMENTS":
			return (Map<String, Object>) attachmentDAO.getSampleAttachment(npreregno, userInfo).getBody();
		case "IDS_SAMPLECOMMENTS":
			return (Map<String, Object>) commentDAO.getSampleComment(npreregno, userInfo).getBody();
		default:
			return (Map<String, Object>) attachmentDAO.getSampleAttachment(npreregno, userInfo).getBody();

		}
	}

	private Map<String, Object> getActiveSubSampleTabData(String ntransactionSampleCode, String activeSampleTab,
			UserInfo userInfo) throws Exception {

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

	private Map<String, Object> getActiveTestTabData(String ntransactionTestCode, String activeTabName,
			UserInfo userInfo) throws Exception {

		switch (activeTabName) {
		case "IDS_TESTATTACHMENTS":
			return (Map<String, Object>) attachmentDAO.getTestAttachment(ntransactionTestCode, userInfo).getBody();
		case "IDS_TESTCOMMENTS":
			return (Map<String, Object>) commentDAO.getTestComment(ntransactionTestCode, userInfo).getBody();
		default:
			return (Map<String, Object>) commentDAO.getTestComment(ntransactionTestCode, userInfo).getBody();

		}
	}

	@Override
	public ResponseEntity<Object> getAnalystCalendarBasedOnUser(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return getUserScheduleCombo(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getInstrumentBasedCategoryForSchedule(int instrumentCatCode, int instrumentNameCode,
			UserInfo userInfo) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		String strQuery = "";
		if (instrumentCatCode == Enumeration.TransactionStatus.NA.gettransactionstatus()) {
			strQuery = "select ninstrumentcode,sinstrumentid from instrument where ninstrumentcatcode="
					+ instrumentCatCode + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " ";

		} else {

			final String calibration = "select * from instrumentCategory where ninstrumentcatcode=" + instrumentCatCode;
			InstrumentCategory instCat = (InstrumentCategory) jdbcUtilityFunction.queryForObject(calibration,
					InstrumentCategory.class, jdbcTemplate);
			int calibrationRequired = instCat.getNcalibrationreq();
			if (calibrationRequired == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				strQuery = " select a.ninstrumentcode,a.sinstrumentid from "
						+ " (select i.ninstrumentcode,i.sinstrumentid,i.ninstrumentnamecode,i.sinstrumentname, "
						+ " max(iv.ninstrumentvalidationcode) as ninstrumentvalidationcode, "
						+ " max(ic.ninstrumentcalibrationcode) as ninstrumentcalibrationcode, "
						+ " max(im.ninstrumentmaintenancecode) as ninstrumentmaintenancecode " + " from instrument i "
						+ " join instrumentvalidation iv on iv.ninstrumentcode = i.ninstrumentcode "
						+ " join instrumentcalibration ic on ic.ninstrumentcode = i.ninstrumentcode "
						+ " join instrumentmaintenance im on im.ninstrumentcode = i.ninstrumentcode  "
						+ " join site s on s.nsitecode =i.nregionalsitecode where i.ninstrumentcatcode ="
						+ instrumentCatCode + " and i.ninstrumentnamecode=" + instrumentNameCode
						+ "  and i.nregionalsitecode=" + userInfo.getNsitecode() + " and i.ninstrumentstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and i.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and iv.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ic.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and im.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " group by i.ninstrumentcode)a "
						+ " join instrumentvalidation b on b.ninstrumentvalidationcode=a.ninstrumentvalidationcode "
						+ " join instrumentcalibration c on c.ninstrumentcalibrationcode = a.ninstrumentcalibrationcode "
						+ " join instrumentmaintenance d on d.ninstrumentmaintenancecode = a.ninstrumentmaintenancecode  "
						+ " and b.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and c.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and d.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and b.nvalidationstatus=" + Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()
						+ " and c.ncalibrationstatus="
						+ Enumeration.TransactionStatus.CALIBIRATION.gettransactionstatus()
						+ " and d.nmaintenancestatus="
						+ Enumeration.TransactionStatus.MAINTANENCE.gettransactionstatus() + " ";

			} else {
				strQuery = " select a.ninstrumentcode,a.sinstrumentid from "
						+ " (select i.ninstrumentcode,i.sinstrumentid,i.ninstrumentnamecode,i.sinstrumentname, "
						+ " max(iv.ninstrumentvalidationcode) as ninstrumentvalidationcode, "
						+ " max(im.ninstrumentmaintenancecode) as ninstrumentmaintenancecode " + " from instrument i "
						+ " join instrumentvalidation iv on iv.ninstrumentcode = i.ninstrumentcode "
						+ " join instrumentmaintenance im on im.ninstrumentcode = i.ninstrumentcode  "
						+ " join site s on s.nsitecode =i.nregionalsitecode where i.ninstrumentcatcode ="
						+ instrumentCatCode + " and i.ninstrumentnamecode=" + instrumentNameCode
						+ " and i.nregionalsitecode=" + userInfo.getNsitecode() + " and i.ninstrumentstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and i.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and iv.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and im.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " group by i.ninstrumentcode)a "
						+ " join instrumentvalidation b on b.ninstrumentvalidationcode=a.ninstrumentvalidationcode "
						+ " join instrumentmaintenance c on c.ninstrumentmaintenancecode = a.ninstrumentmaintenancecode  "
						+ " and b.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and c.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and b.nvalidationstatus=" + Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()
						+ " and c.nmaintenancestatus="
						+ Enumeration.TransactionStatus.MAINTANENCE.gettransactionstatus() + "; ";

			}

		}

		final List<Instrument> lstInstrument = jdbcTemplate.query(strQuery, new Instrument());
		map.put("Instrument", lstInstrument);
		return new ResponseEntity<>(map, HttpStatus.OK);

	}

	@Override
	@SuppressWarnings({ "unused" })
	public ResponseEntity<Object> AllotJobCreateForSchedule(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		ObjectMapper objmap = new ObjectMapper();

		String spreregno = (String) inputMap.get("npreregno");
		String transactionSampleCode = (String) inputMap.get("ntransactionsamplecode");
		String transactionTestCode = (String) inputMap.get("ntransactiontestcode");

		List<JobAllocation> lstJobAllocation = objmap.convertValue(inputMap.get("JobAllocation"),
				new TypeReference<List<JobAllocation>>() {
				});

		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObject = new JSONObject();
		List<String> dateList = new ArrayList<String>();

		dateList.add("UserStartDate");
		dateList.add("UserEndDate");
		dateList.add("InstrumentStartDate");
		dateList.add("InstrumentEndDate");

		Integer ntranscode = Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus();
		Integer ntransactionstatus = (Integer) inputMap.get("TransCode");
		Integer njoballocationseqcount = (Integer) inputMap.get("njoballocationseqcount");
		Integer nresultinstrumentseqcount = (Integer) inputMap.get("nresultinstrumentseqcount");
		Integer ntesthistoryseqcount = (Integer) inputMap.get("ntesthistoryseqcount");

		for (int i = 0; i < lstJobAllocation.size(); i++) {

			JSONObject jsonObject = new JSONObject(lstJobAllocation.get(i).getJsondata());
			JSONObject jsonUIObject = new JSONObject(lstJobAllocation.get(i).getJsonuidata());

			JSONObject instrumentcategory = jsonObject.getJSONObject("InstrumentCategory");
			JSONObject instrument = jsonObject.getJSONObject("Instrument");

			JSONObject instrumentjsonObj = new JSONObject();
			instrumentjsonObj.put("InstrumentCategory", instrumentcategory);
			instrumentjsonObj.put("Instrument", instrument);

			jsonObject = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonObject, dateList, false,
					userInfo);
			jsonUIObject = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonUIObject, dateList,
					false, userInfo);

			String instrumentStartDate = jsonObject.getString("InstrumentStartDate");
			String instrumentEndDate = jsonObject.getString("InstrumentStartDate");

			String insertJobAllocationQuery = "Insert into joballocation(njoballocationcode,npreregno,ntransactionsamplecode,ntransactiontestcode,nsectioncode,ntechniquecode,"
					+ " nuserrolecode,nusercode,nuserperiodcode,ninstrumentcategorycode,ninstrumentcode,ninstrumentperiodcode,ntimezonecode,"
					+ " ntestrescheduleno,jsondata,jsonuidata,nsitecode,nstatus)" + "(select " + njoballocationseqcount
					+ "  as njoballocationcode,rt.npreregno,rt.ntransactionsamplecode,rt.ntransactiontestcode,"
					+ " rt.nsectioncode," + inputMap.get("ntechniquecode")
					+ " as ntechniquecode,(select nuserrolecode from treetemplatetransactionrole ttr join approvalconfig ac on ac.napprovalconfigcode =ttr.napprovalconfigcode and ac.nregtypecode ="
					+ inputMap.get("nregtypecode") + " and ac.nregsubtypecode=" + inputMap.get("nregsubtypecode")
					+ " and ttr.ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
					+ " order by ttr.nlevelno desc LIMIT 1) as nuserrolecode," + inputMap.get("nusercode")
					+ " as nusercode," + " " + 1 + " as nuserperiodcode,"
					+ lstJobAllocation.get(i).getNinstrumentcategorycode() + " as ninstrumentcategorycode,"
					+ lstJobAllocation.get(i).getNinstrumentcode() + " as ninstrumentcode," + " " + 1
					+ " as ninstrumentperiodcode," + userInfo.getNtimezonecode()
					+ " as ntimezonecode,0 as ntestrescheduleno,'"
					+ stringUtilityFunction.replaceQuote(jsonObject.toString()) + "'::JSONB,'"
					+ stringUtilityFunction.replaceQuote(jsonUIObject.toString()) + "'::JSONB,"
					+ userInfo.getNtranssitecode() + " as nsitecode,"
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " as nstatus "
					+ " from registrationtest rt,registrationtesthistory rth  "
					+ " where rt.ntransactiontestcode =rth.ntransactiontestcode "
					+ "	and rt.ntransactiontestcode in (" + lstJobAllocation.get(i).getNtransactiontestcode()
					+ ") and rt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and rth.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and rth.ntesthistorycode =any( "
					+ " select max(ntesthistorycode) from registrationtesthistory where ntransactiontestcode in("
					+ lstJobAllocation.get(i).getNtransactiontestcode() + ") and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " group by npreregno,ntransactionsamplecode,ntransactiontestcode) "
					+ " and rth.ntransactionstatus=" + ntransactionstatus + "); ";
			jdbcTemplate.execute(insertJobAllocationQuery);

			njoballocationseqcount++;

			int instrumentCatcode = lstJobAllocation.get(i).getNinstrumentcategorycode();

			if (instrumentCatcode > 0) {
				String insertResultInstrumentQuery = "insert into resultusedinstrument "
						+ "(nresultusedinstrumentcode,ntransactiontestcode,npreregno,ninstrumentcatcode,ninstrumentcode,dfromdate,dtodate,ntzfromdate,ntztodate,noffsetdfromdate,noffsetdtodate,jsondata,nsitecode,nstatus) "
						+ "(select " + nresultinstrumentseqcount
						+ " as nresultusedinstrumentcode,rt.ntransactiontestcode,rt.npreregno," + ""
						+ lstJobAllocation.get(i).getNinstrumentcategorycode() + " as ninstrumentcatcode,"
						+ lstJobAllocation.get(i).getNinstrumentcode() + " as ninstrumentcode," + " '"
						+ instrumentStartDate + "' as dfromdate,'" + instrumentEndDate + "' as dtodate,"
						+ userInfo.getNtimezonecode() + " as ntzfromdate," + userInfo.getNtimezonecode()
						+ " as ntztodate," + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ "," + " " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ",'"
						+ stringUtilityFunction.replaceQuote(instrumentjsonObj.toString()) + "'::JSONB ,"
						+ userInfo.getNtranssitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " as nstatus "
						+ " from registrationtest rt,registrationtesthistory rth   "
						+ " where rt.ntransactiontestcode =rth.ntransactiontestcode  "
						+ " and rt.ntransactiontestcode in (" + lstJobAllocation.get(i).getNtransactiontestcode()
						+ ") and rt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and rth.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and rth.ntesthistorycode =any(  "
						+ " select max(ntesthistorycode) from registrationtesthistory where ntransactiontestcode in("
						+ lstJobAllocation.get(i).getNtransactiontestcode() + ") " + "and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " group by npreregno,ntransactionsamplecode,ntransactiontestcode)  "
						+ " and rth.ntransactionstatus=" + ntransactionstatus + ");";
				jdbcTemplate.execute(insertResultInstrumentQuery);

				nresultinstrumentseqcount++;
			}

		}

		String insertTestHistoryQuery = "Insert into registrationtesthistory (ntesthistorycode,nformcode,npreregno,ntransactionsamplecode,ntransactiontestcode,nusercode,nuserrolecode,"
				+ "ndeputyusercode,ndeputyuserrolecode,nsampleapprovalhistorycode,ntransactionstatus,dtransactiondate,noffsetdtransactiondate,ntransdatetimezonecode,"
				+ "scomments,nsitecode,nstatus)" + "(select (" + ntesthistoryseqcount
				+ ")+Rank()over(order by ntesthistorycode) ntesthistorycode," + " " + userInfo.getNformcode()
				+ " as nformcode,rt.npreregno,rt.ntransactionsamplecode,rt.ntransactiontestcode,"
				+ userInfo.getNusercode() + " as nusercode," + userInfo.getNuserrole() + " as nuserrolecode," + " "
				+ userInfo.getNdeputyusercode() + " as ndeputyusercode," + userInfo.getNdeputyuserrole()
				+ " as ndeputyuserrolecode,-1 as nsampleapprovalhistorycode," + ntranscode + " as ntransactionstatus,"
				+ " '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "' dtransactiondate,"
				+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + " noffsetdtransactiondate,"
				+ userInfo.getNtimezonecode() + " ntransdatetimezonecode," + " N'"
				+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "' as scomments,"
				+ userInfo.getNtranssitecode() + " as nsitecode,"
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " as nstatus "
				+ " from registrationtest rt,registrationtesthistory rth  "
				+ " where rt.ntransactiontestcode =rth.ntransactiontestcode " + "and rt.ntransactiontestcode in ("
				+ transactionTestCode + ") and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.ntesthistorycode =any( "
				+ "select max(ntesthistorycode) from registrationtesthistory where ntransactiontestcode in("
				+ transactionTestCode + ") and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by npreregno,ntransactionsamplecode,ntransactiontestcode) and rth.ntransactionstatus="
				+ ntransactionstatus + ");	";
		jdbcTemplate.execute(insertTestHistoryQuery);

		map = (Map<String, Object>) getJobAllocationTest(inputMap, userInfo);
		List<Map<String, Object>> lstDataTest = AllocationAuditGet(inputMap, userInfo);
		auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
		auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
		auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
		actionType.put("joballocation", "IDS_ALLOTJOB");
		jsonAuditObject.put("joballocation", lstDataTest);
		auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);
		return new ResponseEntity<Object>(map, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getAnalystCalendarBasedOnUserWithDate(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();
		final ObjectMapper objMapper = new ObjectMapper();
		int nusercode = (int) inputMap.get("nusercode");
		final String Date = (String) inputMap.get("startDate");
		final String viewName = (String) inputMap.get("view");
		final Integer days = (Integer) inputMap.get("days");

		String query = "";
		final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		if (viewName.equals("day") || viewName.equals("timeline")) {

			LocalDate startDate = LocalDate.parse(Date, dbPattern);

			LocalDate endDate = LocalDate.parse(Date, dbPattern).plusDays(days);

			query = " and (cast(jb.jsondata->>'UserStartDate'  as timestamp) <='" + endDate
					+ "'  and cast(jb.jsondata->>'UserEndDate'  as timestamp)>='" + startDate + "')";

		} else if (viewName.equals("month")) {

			LocalDate startDate = YearMonth.parse(Date, dbPattern).atDay(1);
			LocalDate endDate = YearMonth.parse(Date, dbPattern).atEndOfMonth();
			query = " and (cast(jb.jsondata->>'UserStartDate'  as timestamp) <='" + endDate
					+ "'  and cast(jb.jsondata->>'UserEndDate'  as timestamp)>='" + startDate + "')";

		} else if (viewName.equals("week") || viewName.equals("agenda")) {

			LocalDate startDate = LocalDate.parse(Date, dbPattern)
					.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));

			LocalDate endDate = LocalDate.parse(Date, dbPattern).with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));

			query = " and (cast(jb.jsondata->>'UserStartDate'  as timestamp) <='" + endDate
					+ "'  and cast(jb.jsondata->>'UserEndDate'  as timestamp)>='" + startDate + "')";

		} else {
			LocalDate startDate = LocalDate.parse(Date, dbPattern);
			query = " and cast(jb.jsondata->>'UserStartDate'  as timestamp) = '" + startDate + "'";
		}

		String str1 = "select scalendersettingvalue from calendersettings where ncalendersettingcode=49 and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		String ssettingValue = jdbcTemplate.queryForObject(str1, String.class);

		if (ssettingValue.equals("3")) {
			str1 = " select " + ssettingValue
					+ " as grouping , max(rth.ntransactionstatus) ntransactionstatus, max(jb.nusercode) nusercode, "
					+ " coalesce(max(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
					+ "')) as stransdisplaystatus,  max(rt.jsondata->>'stestsynonym') stestsynonym, "
					+ " to_char(cast(max(jb.jsondata->>'UserStartDate')  as timestamp),'"
					+ userInfo.getSpgdatetimeformat() + "') userstartdate, "
					+ " to_char(cast(jb.jsondata->>'UserStartDate'  as timestamp),'" + userInfo.getSpgdatetimeformat()
					+ "' ) userstartdategroup, 	"
					+ " count(rt.ntransactiontestcode) ncount, to_char(cast(max(jb.jsondata->>'UserEndDate') as timestamp),'"
					+ userInfo.getSpgdatetimeformat() + "') userenddate, "
					+ " case when  max(jb.jsondata->>'InstrumentStartDate')!=''  then to_char(cast(max(jb.jsondata->>'InstrumentStartDate') as timestamp), '"
					+ userInfo.getSpgdatetimeformat() + "')"
					+ " else '' end instrumentstartdate, case when  max(jb.jsondata->>'InstrumentStartDate')!=''  "
					+ " then to_char(cast(max(jb.jsondata->>'InstrumentEndDate') as timestamp),'"
					+ userInfo.getSpgdatetimeformat() + "') else '' end instrumentenddate, "
					+ " max(jb.jsondata->>'offsetUserStartDate') offsetduserstartdate, max(jb.jsondata->>'offsetUserEndDate') offsetduserenddate, "
					+ " max(jb.jsondata->>'offsetInstrumentStartDate') offsetdinstrumentstartdate,"
					+ " max(jb.jsondata->>'offsetInstrumentEndDate') offsetdinstrumentenddate  , "
					+ " max(jb.jsondata->>'Comments') \"comments\", max(jb.jsondata->'Instrument'->>'value')::int ninstrumentcode,"
					+ " max(jb.jsondata->'InstrumentCategory'->>'value')::int ninstrumentcategorycode, max(jb.jsondata->>'UserStartDate') userstartdatejson, "
					+ " max(jb.jsondata->>'UserEndDate') userenddatejson " + " from registrationtest rt, "
					+ " registrationtesthistory rth,joballocation jb, transactionstatus ts where  "
					+ " njoballocationcode in ( select max(njoballocationcode) from joballocation "
					+ " group by ntransactiontestcode ) and  rt.ntransactiontestcode =rth.ntransactiontestcode "
					+ " and rt.ntransactiontestcode=jb.ntransactiontestcode "
					+ " and rth.ntesthistorycode in (select max(ntesthistorycode) from registrationtesthistory "
					+ " group by npreregno,ntransactionsamplecode,ntransactiontestcode ) "
					+ " and ts.ntranscode=rth.ntransactionstatus " + " and jb.nusercode =" + nusercode
					+ " and rth.ntransactionstatus in (" + Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus()
					+ "," + Enumeration.TransactionStatus.INITIATED.gettransactionstatus() + ","
					+ Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus() + ") and rt.nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + query
					+ " group by userstartdategroup,rt.ntestcode ";

		} else {

			str1 = "   select    " + ssettingValue
					+ " as grouping , rth.ntransactionstatus ntransactionstatus, jb.nusercode nusercode, "
					+ "  coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
					+ "') as stransdisplaystatus,  rt.jsondata->>'stestsynonym' stestsynonym,"
					+ "  to_char(cast(jb.jsondata->>'UserStartDate'  as timestamp),'" + userInfo.getSpgdatetimeformat()
					+ "') userstartdate, " + "  to_char(cast(jb.jsondata->>'UserStartDate'  as timestamp),'"
					+ userInfo.getSpgdatetimeformat() + "' ) userstartdategroup, 	"
					+ "  to_char(cast(jb.jsondata->>'UserEndDate' as timestamp),'" + userInfo.getSpgdatetimeformat()
					+ "') userenddate, "
					+ "  case when  jb.jsondata->>'InstrumentStartDate'!=''  then to_char(cast(jb.jsondata->>'InstrumentStartDate' as timestamp), '"
					+ userInfo.getSpgdatetimeformat() + "') "
					+ "  else '' end instrumentstartdate, case when  jb.jsondata->>'InstrumentStartDate'!=''  "
					+ "  then to_char(cast(jb.jsondata->>'InstrumentEndDate' as timestamp),'"
					+ userInfo.getSpgdatetimeformat() + "') else '' end instrumentenddate, "
					+ " jb.jsondata->>'offsetUserStartDate' offsetduserstartdate, jb.jsondata->>'offsetUserEndDate' offsetduserenddate, "
					+ " jb.jsondata->>'offsetInstrumentStartDate' offsetdinstrumentstartdate, "
					+ " jb.jsondata->>'offsetInstrumentEndDate' offsetdinstrumentenddate  , "
					+ " jb.jsondata->>'Comments' \"comments\", cast(jb.jsondata->'Instrument'->>'value' as int) ninstrumentcode, "
					+ " cast(jb.jsondata->'InstrumentCategory'->>'value' as int) ninstrumentcategorycode, jb.jsondata->>'UserStartDate' userstartdatejson, "
					+ " jb.jsondata->>'UserEndDate' userenddatejson,arno.sarno,rno.ssamplearno "
					+ " from registrationtest rt,registrationarno arno,registrationsamplearno rno,  "
					+ " registrationtesthistory rth,joballocation jb,users u, transactionstatus ts "
					+ " where  njoballocationcode in ( select max(jb1.njoballocationcode) from joballocation jb1  "
					+ " group by jb1.ntransactiontestcode ) "
					+ " and rth.ntesthistorycode in (select max(rt1.ntesthistorycode) from registrationtesthistory  rt1 "
					+ " group by rt1.npreregno,rt1.ntransactionsamplecode,rt1.ntransactiontestcode ) "
					+ " and  rt.ntransactiontestcode =rth.ntransactiontestcode and arno.npreregno=rt.npreregno"
					+ " and  rno.npreregno=rt.npreregno  and rt.ntransactiontestcode=jb.ntransactiontestcode "
					+ " and jb.nusercode=u.nusercode and ts.ntranscode=rth.ntransactionstatus "
					+ " and rno.ntransactionsamplecode=rt.ntransactionsamplecode and u.nusercode =" + nusercode
					+ " and rth.ntransactionstatus in (" + +Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus()
					+ "," + Enumeration.TransactionStatus.INITIATED.gettransactionstatus() + ","
					+ Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus() + ") and jb.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + query;

		}

		List<JobAllocation> lst = jdbcTemplate.query(str1, new JobAllocation());

		str1 = " select *,ddate as dcalenderholidaystartdate,ddate+(1439 * interval '1 minute') as dcalenderholidayenddate from  userbasedholidays where nusercode="
				+ nusercode + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and nsitecode=" + userInfo.getNmastersitecode();

		List<Map<String, Object>> calenderusersholidays = jdbcTemplate.queryForList(str1);

		map.put("UserBasedHolidays", calenderusersholidays);

		str1 = " select c.* from holidaysyear y ,holidayyearversion v ,commonholidays c"
				+ " where y.nyearcode=v.nyearcode and v.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and "
				+ " c.nholidayyearversion=v.nholidayyearversion  and syear=to_char(cast('" + Date
				+ "' as timestamp),'yyyy');";

		List<Map<String, Object>> commonHolidays = jdbcTemplate.queryForList(str1);

		str1 = " select c.*,c.ddate as dcalenderholidaystartdate,c.ddate+(1439 * interval '1 minute') as dcalenderholidayenddate from holidaysyear y ,holidayyearversion v ,publicholidays c"
				+ " where y.nyearcode=v.nyearcode and v.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and "
				+ " c.nholidayyearversion=v.nholidayyearversion  and syear=to_char(cast('" + Date
				+ "' as timestamp),'yyyy');";

		List<Map<String, Object>> publicHolidays = jdbcTemplate.queryForList(str1);

		map.put("calenderPublicHolidays", publicHolidays);

		map.put("calenderCommonHolidays", commonHolidays);

		final List<JobAllocation> lstUTCConvertedDate = objMapper
				.convertValue(
						dateUtilityFunction.getSiteLocalTimeFromUTC(lst,
								Arrays.asList("userstartdate", "userenddate", "instrumentstartdate",
										"instrumentenddate"),
								null, userInfo, false, null, false),
						new TypeReference<List<JobAllocation>>() {
						});
		map.put("UserData", lstUTCConvertedDate);
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@Override
	@SuppressWarnings({ "unused" })
	public Map<String, Object> AllotJobCalendarCreate(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		ObjectMapper objmap = new ObjectMapper();

		int instrumentCatcode = (int) inputMap.get("ninstrumentcatcode");
		String spreregno = (String) inputMap.get("npreregno");
		String transactionSampleCode = (String) inputMap.get("ntransactionsamplecode");
		String transactionTestCode = (String) inputMap.get("ntransactiontestcode");
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date userEndDate = null;
		Date instrumentEndDate = null;

		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObject = new JSONObject();
		List<String> dateList = new ArrayList<String>();

		JSONObject jsonObject = new JSONObject(inputMap.get("jsondata").toString());
		JSONObject jsonUIObject = new JSONObject(inputMap.get("jsonuidata").toString());
		JSONObject instrumentjsonObj = new JSONObject();

		dateList.add("UserStartDate");
		dateList.add("UserEndDate");
		// dateList.add("InstrumentStartDate");
		// dateList.add("InstrumentEndDate");
		jsonObject = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonObject, dateList, false,
				userInfo);

		Integer ntranscode = Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus();
		Integer ntransactionstatus = (Integer) inputMap.get("TransCode");
		Integer njoballocationseqcount = (Integer) inputMap.get("njoballocationseqcount");
		Integer nresultinstrumentseqcount = (Integer) inputMap.get("nresultinstrumentseqcount");
		Integer ntesthistoryseqcount = (Integer) inputMap.get("ntesthistoryseqcount");
		final String sScheduleSkip = scheduleSkip();
		String query = "select * from joballocation where ntransactiontestcode in ("
				+ inputMap.get("ntransactiontestcode") + ") and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		List<JobAllocation> listJoBAllocation = jdbcTemplate.query(query, new JobAllocation());
		if (sScheduleSkip.equals(String.valueOf(Enumeration.TransactionStatus.YES.gettransactionstatus()))
				&& listJoBAllocation.size() > 0) {
			String updateJbAllocation = "update joballocation set " + " ntechniquecode="
					+ inputMap.get("ntechniquecode") + "," + " nusercode=" + inputMap.get("nusercode")
					+ ",nuserperiodcode=" + inputMap.get("nuserperiodcode") + "," + " ninstrumentcategorycode="
					+ inputMap.get("ninstrumentcatcode") + ",ninstrumentcode=" + inputMap.get("ninstrumentcode") + ","
					+ " ninstrumentnamecode=" + inputMap.get("ninstrumentnamecode") + ",ninstrumentperiodcode="
					+ inputMap.get("ninstrumentperiodcode") + "," + " jsondata='"
					+ stringUtilityFunction.replaceQuote(jsonObject.toString()) + "'::JSONB,jsonuidata='"
					+ stringUtilityFunction.replaceQuote(jsonUIObject.toString()) + "'::JSONB," + " nsitecode="
					+ userInfo.getNtranssitecode() + " where ntransactiontestcode in (" + transactionTestCode + ")";
			jdbcTemplate.execute(updateJbAllocation);
		} else {
			String insertJobAllocationQuery = "Insert into joballocation(njoballocationcode,npreregno,ntransactionsamplecode,ntransactiontestcode,nsectioncode,ntechniquecode,"
					+ "nuserrolecode,nusercode,nuserperiodcode,ninstrumentcategorycode,ninstrumentcode,ninstrumentperiodcode,ntimezonecode,"
					+ "ntestrescheduleno,jsondata,jsonuidata,nsitecode,nstatus,ninstrumentnamecode)" + "(select ("
					+ njoballocationseqcount
					+ ")+rank () over(order by rt.ntransactionsamplecode,rt.ntransactiontestcode)  as njoballocationcode,rt.npreregno,rt.ntransactionsamplecode,rt.ntransactiontestcode,"
					+ "rt.nsectioncode," + inputMap.get("ntechniquecode")
					+ " as ntechniquecode,(select nuserrolecode from treetemplatetransactionrole ttr join approvalconfig ac on ac.napprovalconfigcode =ttr.napprovalconfigcode and ac.nregtypecode ="
					+ inputMap.get("nregtypecode") + " and ac.nregsubtypecode=" + inputMap.get("nregsubtypecode")
					+ " and ttr.ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
					+ " order by ttr.nlevelno desc LIMIT 1) as nuserrolecode," + inputMap.get("nusercode")
					+ " as nusercode," + " " + inputMap.get("nuserperiodcode") + " as nuserperiodcode,"
					+ inputMap.get("ninstrumentcatcode") + " as ninstrumentcategorycode,"
					+ inputMap.get("ninstrumentcode") + " as ninstrumentcode," + " "
					+ inputMap.get("ninstrumentperiodcode") + " as ninstrumentperiodcode," + userInfo.getNtimezonecode()
					+ " as ntimezonecode,0 as ntestrescheduleno,'"
					+ stringUtilityFunction.replaceQuote(jsonObject.toString()) + "'::JSONB,'"
					+ stringUtilityFunction.replaceQuote(jsonUIObject.toString()) + "'::JSONB,"
					+ userInfo.getNtranssitecode() + " as nsitecode,"
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " as nstatus,"
					+ inputMap.get("ninstrumentnamecode") + " as ninstrumentnamecode "
					+ " from registrationtest rt,registrationtesthistory rth  "
					+ "where rt.ntransactiontestcode =rth.ntransactiontestcode and rt.ntransactiontestcode in ("
					+ transactionTestCode + ") and rt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.ntesthistorycode =any( "
					+ "select max(ntesthistorycode) from registrationtesthistory where ntransactiontestcode in("
					+ transactionTestCode + ") and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " group by npreregno,ntransactionsamplecode,ntransactiontestcode) and rth.ntransactionstatus="
					+ ntransactionstatus + "); ";
			jdbcTemplate.execute(insertJobAllocationQuery);
		}
		if (instrumentCatcode > 0) {

			jsonObject.put("InstrumentStartDate", jsonObject.get("UserStartDate"));
			jsonObject.put("InstrumentEndDate", jsonObject.get("UserEndDate"));

			jsonUIObject.put("UserStartDate", jsonObject.get("UserStartDate"));
			jsonUIObject.put("UserEndDate", jsonObject.get("UserEndDate"));

			jsonUIObject.put("InstrumentStartDate", jsonObject.get("UserStartDate"));
			jsonUIObject.put("InstrumentEndDate", jsonObject.get("UserEndDate"));
			String insertResultInstrumentQuery = "insert into resultusedinstrument "
					+ "(nresultusedinstrumentcode,ntransactiontestcode,npreregno,ninstrumentcatcode,ninstrumentcode,dfromdate,"
					+ " dtodate,ntzfromdate,ntztodate,noffsetdfromdate,noffsetdtodate,jsondata,nsitecode,nstatus,ninstrumentnamecode) "
					+ "(select (" + nresultinstrumentseqcount
					+ ")+Rank() over(order by rt.ntransactionsamplecode,rt.ntransactiontestcode) as nresultusedinstrumentcode,rt.ntransactiontestcode,rt.npreregno,"
					+ "" + inputMap.get("ninstrumentcatcode") + " as ninstrumentcatcode,"
					+ inputMap.get("ninstrumentcode") + " as ninstrumentcode," + " '" + jsonObject.get("UserStartDate")
					+ "' as dfromdate,'" + jsonObject.get("UserEndDate") + "' as dtodate," + userInfo.getNtimezonecode()
					+ " as ntzfromdate," + userInfo.getNtimezonecode() + " as ntztodate,"
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + " as noffsetdfromdate,"
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + " as noffsetdtodate,'"
					+ stringUtilityFunction.replaceQuote(instrumentjsonObj.toString()) + "'::JSONB ,"
					+ userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " as nstatus," + inputMap.get("ninstrumentnamecode") + " as ninstrumentnamecode "
					+ " from registrationtest rt,registrationtesthistory rth   "
					+ " where rt.ntransactiontestcode =rth.ntransactiontestcode  " + "and rt.ntransactiontestcode in ("
					+ transactionTestCode + ") and rt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and rth.ntesthistorycode =any(  "
					+ " select max(ntesthistorycode) from registrationtesthistory where ntransactiontestcode in("
					+ transactionTestCode + ") and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " group by npreregno,ntransactionsamplecode,ntransactiontestcode)  "
					+ " and rth.ntransactionstatus=" + ntransactionstatus + ");";
			jdbcTemplate.execute(insertResultInstrumentQuery);
		}

		String insertTestHistoryQuery = "Insert into registrationtesthistory (ntesthistorycode,nformcode,npreregno,ntransactionsamplecode,ntransactiontestcode,nusercode,nuserrolecode,"
				+ "ndeputyusercode,ndeputyuserrolecode,nsampleapprovalhistorycode,ntransactionstatus,dtransactiondate,noffsetdtransactiondate,ntransdatetimezonecode,"
				+ "scomments,nsitecode,nstatus)" + "(select (" + ntesthistoryseqcount
				+ ")+Rank()over(order by ntesthistorycode) ntesthistorycode," + " " + userInfo.getNformcode()
				+ " as nformcode,rt.npreregno,rt.ntransactionsamplecode,rt.ntransactiontestcode,"
				+ userInfo.getNusercode() + " as nusercode," + userInfo.getNuserrole() + " as nuserrolecode," + " "
				+ userInfo.getNdeputyusercode() + " as ndeputyusercode," + userInfo.getNdeputyuserrole()
				+ " as ndeputyuserrolecode,-1 as nsampleapprovalhistorycode," + ntranscode + " as ntransactionstatus,"
				+ " '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "' dtransactiondate,"
				+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + " noffsetdtransactiondate,"
				+ userInfo.getNtimezonecode() + " ntransdatetimezonecode," + " N'"
				+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "' as scomments,"
				+ userInfo.getNtranssitecode() + " as nsitecode,"
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " as nstatus "
				+ " from registrationtest rt,registrationtesthistory rth  "
				+ " where rt.ntransactiontestcode =rth.ntransactiontestcode and rt.ntransactiontestcode in ("
				+ transactionTestCode + ") and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.ntesthistorycode =any( "
				+ " select max(ntesthistorycode) from registrationtesthistory where ntransactiontestcode in("
				+ transactionTestCode + ") and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by npreregno,ntransactionsamplecode,ntransactiontestcode) and rth.ntransactionstatus="
				+ ntransactionstatus + ");	";
		jdbcTemplate.execute(insertTestHistoryQuery);

//		ALPD-6064	Added createIntegrationMethod by Vishakh (08-07-2025)
		boolean nneedmyjob = (boolean) inputMap.get("nneedmyjob");
		boolean nneedtestinitiate = (boolean) inputMap.get("nneedtestinitiate");
		
		if(!nneedmyjob && !nneedtestinitiate)
		{
			inputMap.put("sintegrationpreregno",spreregno);
			inputMap.put("svalidationstatus",Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus()); 
			
			transactionDAOSupport.createIntegrationRecord(inputMap, userInfo);
		}
		
		
		map = (Map<String, Object>) getJobAllocationTest(inputMap, userInfo);
		List<Map<String, Object>> lstDataTest = AllocationAuditGet(inputMap, userInfo);
		auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
		auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
		auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
		actionType.put("joballocation", "IDS_ALLOTJOB");
		jsonAuditObject.put("joballocation", lstDataTest);
		auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);
		return map;
	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getRescheduleEdit(String npreregno, String ntransactionsamplecode,
			String ntransactiontestcode, UserInfo userInfo, Map<String, Object> inputMap) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		String str = " select ntechniquecode,nusercode,ninstrumentcategorycode,ninstrumentnamecode,ninstrumentcode,"
				+ " ninstrumentperiodcode,nuserperiodcode," + " jsonuidata->>'Technique' as stechniquename,"
				+ " jsonuidata->>'Users' as susername," + " jsonuidata->>'UserPeriod' as suserperiodname,"
				+ " TO_CHAR((jsonuidata->>'UserStartDate'):: timestamp without time zone , '"
				+ userInfo.getSpgdatetimeformat() + "') userstartdate,"
				+ " jsonuidata->>'InstrumentCategory' as sinstrumentcatname,"
				+ " jsonuidata->>'InstrumentName' as sinstrumentname,"
				+ " jsonuidata->>'InstrumentId' as sinstrumentid,"
				+ " jsonuidata->>'InstrumentPeriod' as sinstrumentperiodname,"
				+ " case when ninstrumentcategorycode =-1 then jsonuidata->>'InstrumentStartDate' else "
				+ " TO_CHAR((jsonuidata->>'InstrumentStartDate'):: timestamp without time zone , '"
				+ userInfo.getSpgdatetimeformat() + "') end as instrumentstartdate,"
				+ " jsonuidata->>'Comments' as comments, jsonuidata->>'UserHoldDuration' as suserholdduration, "
				+ " jsonuidata->>'InstrumentHoldDuration' as sinstrumentholdduration,jb.nsectioncode,"
				+ "	case when jsonuidata->>'Section' IS NULL then  s.ssectionname  else jsonuidata->>'Section'  end AS ssectionname "
				+ " from joballocation jb,section s where npreregno in (" + npreregno
				+ ") and ntransactionsamplecode  in (" + ntransactionsamplecode + ") and ntransactiontestcode in ("
				+ ntransactiontestcode + ") "
				+ " and ntestrescheduleno =(select max(ntestrescheduleno) as ntestrescheduleno from joballocation "
				+ " where npreregno in (" + npreregno + ") and ntransactionsamplecode  in (" + ntransactionsamplecode
				+ ") and ntransactiontestcode in (" + ntransactiontestcode
				+ ")) and jb.nsectioncode=s.nsectioncode and s.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<JobAllocation> lst = jdbcTemplate.query(str, new JobAllocation());
		if (lst.size() > 0) {
			map.put("JobAllocation", lst.get(0));
			if (lst.get(0).getNtechniquecode() > 0) {
				map.put("usertype", true);
				String ssectioncode = String.valueOf(lst.get(0).getNsectioncode());
				map.putAll((Map<String, Object>) getUsersBasedTechnique(lst.get(0).getNtechniquecode(), ssectioncode,
						(int) inputMap.get("nregtypecode"), (int) inputMap.get("nregsubtypecode"), userInfo).getBody());
			}
		}

		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> calenderProperties(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		String Date = (String) inputMap.get("startDate");

		String str = "select * from calendersettings where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		List<Map<String, Object>> calendersettings = jdbcTemplate.queryForList(str);

		str = "select * from calendercolor where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		List<Map<String, Object>> calendercolor = jdbcTemplate.queryForList(str);

		str = " select c.* from holidaysyear y ,holidayyearversion v ,commonholidays c"
				+ " where y.nyearcode=v.nyearcode and v.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and "
				+ " c.nholidayyearversion=v.nholidayyearversion  and syear=to_char(cast('" + Date
				+ "' as timestamp),'yyyy');";

		List<Map<String, Object>> commonHolidays = jdbcTemplate.queryForList(str);

		str = " select c.*,c.ddate as dcalenderholidaystartdate,c.ddate+(1439 * interval '1 minute') as dcalenderholidayenddate from holidaysyear y ,holidayyearversion v ,publicholidays c"
				+ " where y.nyearcode=v.nyearcode and v.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and "
				+ " c.nholidayyearversion=v.nholidayyearversion and syear=to_char(cast('" + Date
				+ "' as timestamp),'yyyy');";

		List<Map<String, Object>> publicHolidays = jdbcTemplate.queryForList(str);

		final Map<String, Object> returnMap = new HashMap<>();

		returnMap.put("calenderPublicHolidays", publicHolidays);
		returnMap.put("calenderCommonHolidays", commonHolidays);
		returnMap.put("calenderSettings", calendersettings);
		returnMap.put("calenderColor", calendercolor);

		return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getInstrumentNameBasedCategoryForSchedule(int instrumentCatCode, UserInfo userInfo)
			throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		String strQuery = "";
		if (instrumentCatCode == Enumeration.TransactionStatus.NA.gettransactionstatus()) {
			strQuery = "select ninstrumentnamecode,sinstrumentname from instrument where ninstrumentcatcode="
					+ instrumentCatCode + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " ";

		} else {

			final String calibration = "select * from instrumentCategory where ninstrumentcatcode=" + instrumentCatCode;
			InstrumentCategory instCat = (InstrumentCategory) jdbcUtilityFunction.queryForObject(calibration,
					InstrumentCategory.class, jdbcTemplate);
			int calibrationRequired = instCat.getNcalibrationreq();
			if (calibrationRequired == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				strQuery = " select a.ninstrumentnamecode,a.sinstrumentname from "
						+ " (select i.ninstrumentnamecode,i.sinstrumentname, "
						+ " max(iv.ninstrumentvalidationcode) as ninstrumentvalidationcode, "
						+ " max(ic.ninstrumentcalibrationcode) as ninstrumentcalibrationcode, "
						+ " max(im.ninstrumentmaintenancecode) as ninstrumentmaintenancecode " + " from instrument i "
						+ " join instrumentvalidation iv on iv.ninstrumentcode = i.ninstrumentcode "
						+ " join instrumentcalibration ic on ic.ninstrumentcode = i.ninstrumentcode "
						+ " join instrumentmaintenance im on im.ninstrumentcode = i.ninstrumentcode  "
						+ " join site s on s.nsitecode =i.nregionalsitecode " + " where i.ninstrumentcatcode ="
						+ instrumentCatCode + " and i.nregionalsitecode=" + userInfo.getNsitecode()
						+ " and i.ninstrumentstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " " + " and i.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and iv.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " and ic.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and im.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and s.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " group by i.ninstrumentnamecode,i.sinstrumentname)a "
						+ " join instrumentvalidation b on b.ninstrumentvalidationcode=a.ninstrumentvalidationcode "
						+ " join instrumentcalibration c on c.ninstrumentcalibrationcode = a.ninstrumentcalibrationcode "
						+ " join instrumentmaintenance d on d.ninstrumentmaintenancecode = a.ninstrumentmaintenancecode  "
						+ " and b.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and c.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and d.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and b.nvalidationstatus=" + Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()
						+ " and c.ncalibrationstatus="
						+ Enumeration.TransactionStatus.CALIBIRATION.gettransactionstatus()
						+ " and d.nmaintenancestatus="
						+ Enumeration.TransactionStatus.MAINTANENCE.gettransactionstatus() + " ";

			} else {
				strQuery = " select a.ninstrumentnamecode,a.sinstrumentname from "
						+ " (select i.ninstrumentnamecode,i.sinstrumentname, "
						+ " max(iv.ninstrumentvalidationcode) as ninstrumentvalidationcode, "
						+ " max(im.ninstrumentmaintenancecode) as ninstrumentmaintenancecode " + " from instrument i "
						+ " join instrumentvalidation iv on iv.ninstrumentcode = i.ninstrumentcode "
						+ " join instrumentmaintenance im on im.ninstrumentcode = i.ninstrumentcode  "
						+ " join site s on s.nsitecode =i.nregionalsitecode  where i.ninstrumentcatcode ="
						+ instrumentCatCode + " and i.nregionalsitecode=" + userInfo.getNsitecode()
						+ " and i.ninstrumentstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and i.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and iv.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " and im.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and s.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " group by i.ninstrumentnamecode,i.sinstrumentname)a "
						+ " join instrumentvalidation b on b.ninstrumentvalidationcode=a.ninstrumentvalidationcode "
						+ " join instrumentmaintenance c on c.ninstrumentmaintenancecode = a.ninstrumentmaintenancecode  "
						+ " and b.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and c.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and b.nvalidationstatus=" + Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()
						+ " and c.nmaintenancestatus="
						+ Enumeration.TransactionStatus.MAINTANENCE.gettransactionstatus() + " ";

			}
		}

		final List<Instrument> lstInstrument = jdbcTemplate.query(strQuery, new Instrument());
		map.put("InstrumentName", lstInstrument);
		return new ResponseEntity<>(map, HttpStatus.OK);

	}

	public String scheduleSkip() {
		return jdbcTemplate.queryForObject("select ssettingvalue from settings where nsettingcode = 43 and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), String.class);
	}

	public ResponseEntity<Object> getSectionChange(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		final int nregtypecode = (int) inputMap.get("nregtypecode");
		final int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
		final String spreregno = (String) inputMap.get("npreregno");
		final String transactionSampleCode = (String) inputMap.get("ntransactionsamplecode");
		final String transactionTestCode = (String) inputMap.get("ntransactiontestcode");
		final int controlCode = (int) inputMap.get("ncontrolcode");

		if (transactionTestCode.isEmpty()) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTTEST", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);

		} else {

			List<RegistrationTestHistory> filteredSampleList = validateSelectedTestStatus(spreregno,
					transactionSampleCode, transactionTestCode, controlCode, userInfo, false);
			List<String> transtestcodeList = Arrays.asList(transactionTestCode.split(","));

			List<String> ntransCodeList = transtestcodeList.stream().filter(
					a -> filteredSampleList.stream().noneMatch(b -> b.getNtransactiontestcode() == Integer.parseInt(a)))
					.collect(Collectors.toList());

			if (ntransCodeList.isEmpty()) {

				String npreRegNo = "";
				npreRegNo = filteredSampleList.stream().map(obj -> String.valueOf(obj.getNpreregno()))
						.collect(Collectors.joining(","));

				String ntranstestcode = "";
				ntranstestcode = filteredSampleList.stream().map(obj -> String.valueOf(obj.getNtransactiontestcode()))
						.collect(Collectors.joining(","));

				final String strTestCodeQuery = "select rt.ntestcode from registrationtest rt,registrationtesthistory rth "
						+ " where rt.ntransactiontestcode =rth.ntransactiontestcode " + "and rt.npreregno in("
						+ npreRegNo + ") and rt.ntransactiontestcode in(" + ntranstestcode + ") "
						+ " and rth.ntesthistorycode in (select max(ntesthistorycode) from registrationtesthistory where npreregno in("
						+ npreRegNo + ") and ntransactiontestcode in(" + ntranstestcode
						+ ") group by ntransactiontestcode) group by rt.ntestcode;";

				List<String> lsttestcode = jdbcTemplate.queryForList(strTestCodeQuery, String.class);
				String testCode = lsttestcode.stream().collect(Collectors.joining(","));

				List<String> ntestcodeList = Arrays.asList(testCode.split(","));
				final int ntestcodesize = ntestcodeList.size();

				Map<String, Object> map = new HashMap<>();
				inputMap.put("ntestcodesize", ntestcodesize);
				final String str = (String) getSectionByTest(inputMap, testCode, userInfo);

				final List<Section> lstSection = ((List<Section>) jdbcTemplate.query(str, new Section()));
				map.put("Section", lstSection);
				return new ResponseEntity<>(map, HttpStatus.OK);

			} else {

				List<TransactionStatus> validationList = validationForJobAllocationTest(nregtypecode, nregsubtypecode,
						controlCode, userInfo);
				String alertStatus = validationList.stream().map(status -> status.getStransdisplaystatus())
						.collect(Collectors.joining("/"));
				String select = commonFunction.getMultilingualMessage("IDS_SELECT", userInfo.getSlanguagefilename());
				String toSectionChange = commonFunction.getMultilingualMessage("IDS_SECTIONTOUPDATE",
						userInfo.getSlanguagefilename());

				return new ResponseEntity<>(select + " " + alertStatus + "" + " " + toSectionChange,
						HttpStatus.EXPECTATION_FAILED);

			}
		}

	}

	public ResponseEntity<Object> updateSectionJobAllocation(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		String spreregno = (String) inputMap.get("npreregno");
		String transactionSampleCode = (String) inputMap.get("ntransactionsamplecode");
		String transactionTestCode = (String) inputMap.get("ntransactiontestcode");
		int controlCode = (int) inputMap.get("ncontrolcode");
		String ssectioncode = String.valueOf(inputMap.get("nsectioncode"));
		inputMap.put("nsectioncode", inputMap.get("sfilterSection"));

		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
		auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
		auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));

		JSONObject actionType = new JSONObject();
		JSONObject actionTypeRecevieInLab = new JSONObject();

		JSONObject jsonAuditNew = new JSONObject();
		JSONObject jsonAuditOld = new JSONObject();

		Map<String, Object> map = new HashMap<>();
		String insertSectionQuery = "";

		Integer nsectionhistoryseqcount;
		Integer nsectionseqcount;

		List<RegistrationTestHistory> filteredSampleList = validateSelectedTestStatus(spreregno, transactionSampleCode,
				transactionTestCode, controlCode, userInfo, false);

		inputMap.put("nusercode", -1);
		List<Map<String, Object>> oldlstDataTest = testAuditGet(inputMap, userInfo);

		List<String> transtestcodeList = Arrays.asList(transactionTestCode.split(","));

		List<String> ntransCodeList = transtestcodeList.stream().filter(
				a -> filteredSampleList.stream().noneMatch(b -> b.getNtransactiontestcode() == Integer.parseInt(a)))
				.collect(Collectors.toList());

		if (ntransCodeList.isEmpty()) {
			String npreRegNo = "";
			npreRegNo = filteredSampleList.stream().map(obj -> String.valueOf(obj.getNpreregno()))
					.collect(Collectors.joining(","));

			String sectionameQuery = "select ssectionname from section where nsectioncode =" + ssectioncode
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			String sectioName = jdbcTemplate.queryForObject(sectionameQuery, String.class);

			String str = "update registrationtest  set nsectioncode =" + ssectioncode + ","
					+ " jsondata = jsondata || '{\"ssectionname\": \"" + sectioName + "\"}' where npreregno in("
					+ spreregno + ") and " + " ntransactionsamplecode in (" + transactionSampleCode
					+ ") and ntransactiontestcode in(" + transactionTestCode + ") ;";

			str = str + "update joballocation  set nsectioncode =" + ssectioncode + " where npreregno in(" + spreregno
					+ ") and " + " ntransactionsamplecode in (" + transactionSampleCode
					+ ") and ntransactiontestcode in(" + transactionTestCode + ") ;";

			jdbcTemplate.execute(str);

			String existsSection = "select * from registrationsection where npreregno in(" + spreregno + ") "
					+ " and nsectioncode =" + ssectioncode + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNtranssitecode() + "";

			List<RegistrationSection> sampleList = (List<RegistrationSection>) jdbcTemplate.query(existsSection,
					new RegistrationSection());

			List<String> list1 = Arrays.asList(npreRegNo.split(","));

			List<String> list = list1.stream()
					.filter(a -> sampleList.stream().noneMatch(b -> b.getNpreregno() == Integer.parseInt(a)))
					.collect(Collectors.toList());

			Set<String> setFromStream = list.stream().collect(Collectors.toSet());

			if (!setFromStream.isEmpty()) {

				String str1 = "select nsequenceno from seqnoregistration where stablename ='registrationsectionhistory' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
				nsectionhistoryseqcount = jdbcTemplate.queryForObject(str1, Integer.class);

				String str2 = "select nsequenceno from seqnoregistration where stablename ='registrationsection' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";
				nsectionseqcount = jdbcTemplate.queryForObject(str2, Integer.class);

				nsectionhistoryseqcount = nsectionhistoryseqcount + 1;
				nsectionseqcount = nsectionseqcount + 1;

				if (setFromStream.size() > 0) {
					for (String samplearno : setFromStream) {
						insertSectionQuery = insertSectionQuery + "INSERT INTO public.registrationsection("
								+ "	nregistrationsectioncode, npreregno, nsectioncode, nsitecode, nstatus)"
								+ "	VALUES (" + nsectionseqcount + ", " + samplearno + ", " + ssectioncode + ", "
								+ userInfo.getNtranssitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

						insertSectionQuery = insertSectionQuery
								+ "Insert into registrationsectionhistory (nsectionhistorycode,npreregno,nsectioncode,ntransactionstatus,nsitecode,nstatus)"
								+ " values(" + nsectionhistoryseqcount + "," + samplearno + "," + ssectioncode + ","
								+ "" + Enumeration.TransactionStatus.RECIEVED_IN_SECTION.gettransactionstatus() + ","
								+ userInfo.getNtranssitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
						nsectionhistoryseqcount++;
						nsectionseqcount++;
					}

					insertSectionQuery = insertSectionQuery + "update seqnoregistration set nsequenceno ="
							+ nsectionseqcount + " where stablename='registrationsection' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ;";

					insertSectionQuery = insertSectionQuery + "update seqnoregistration set nsequenceno ="
							+ nsectionhistoryseqcount + " where stablename='registrationsectionhistory' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";

					jdbcTemplate.execute(insertSectionQuery);

					inputMap.put("nsectioncode", ssectioncode);

					List<Map<String, Object>> lstDataTest = testAuditGet(inputMap, userInfo);
					actionTypeRecevieInLab.put("registrationtest", "IDS_RECEIVEINLAB");
					JSONObject jsonAuditObject = new JSONObject();

					jsonAuditObject.put("registrationtest", lstDataTest);
					auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionTypeRecevieInLab, auditmap,
							false, userInfo);

				}
			}

			map.putAll((Map<String, Object>) getFilterSection(inputMap, userInfo));

			if (inputMap.containsKey("ntansactionSubSamplecode")) {
				inputMap.put("ntransactionsamplecode", inputMap.get("ntansactionSubSamplecode"));
			}
			actionType.put("registrationtest", "IDS_SECTIONUPDATE");
			jsonAuditOld.put("registrationtest", oldlstDataTest);
			inputMap.put("nsectioncode", ssectioncode);
			List<Map<String, Object>> newlstDataTest = testAuditGet(inputMap, userInfo);
			jsonAuditNew.put("registrationtest", newlstDataTest);

			auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, jsonAuditNew, actionType, auditmap, false, userInfo);
			inputMap.put("ntransactiontestcode",
					String.valueOf(Enumeration.TransactionStatus.ALL.gettransactionstatus()));
			inputMap.put("nsectioncode", String.valueOf(inputMap.get("sfilterSection")));

			map.putAll((Map<String, Object>) getJobAllocationTest(inputMap, userInfo));
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			List<TransactionStatus> validationList = validationForJobAllocationTest((int) inputMap.get("nregtypecode"),
					(int) inputMap.get("nregsubtypecode"), controlCode, userInfo);
			final String alertStatus = validationList.stream().map(status -> status.getStransdisplaystatus())
					.collect(Collectors.joining("/"));
			final String select = commonFunction.getMultilingualMessage("IDS_SELECT", userInfo.getSlanguagefilename());
			final String toSectionChange = commonFunction.getMultilingualMessage("IDS_SECTIONTOUPDATE",
					userInfo.getSlanguagefilename());
			return new ResponseEntity<>(select + " " + alertStatus + "" + " " + toSectionChange,
					HttpStatus.EXPECTATION_FAILED);
		}

	}

	public List<TransactionStatus> validationForJobAllocationTest(int nregtypecode, int nregsubtypecode,
			int controlCode, UserInfo userInfo) {
		final String validationQuery = "SELECT COALESCE(ts.jsondata -> 'stransdisplaystatus' ->> 'en-US', ts.jsondata -> 'stransdisplaystatus' ->> 'en-US') AS stransdisplaystatus"
				+ ",ts.ntranscode FROM   transactionvalidation tv, transactionstatus ts"
				+ "  WHERE  ntransactionstatus IN(SELECT ntransactionstatus" + " FROM   transactionvalidation tv1,"
				+ " registrationtype rt,registrationsubtype rst" + " WHERE  nformcode = " + userInfo.getNformcode()
				+ " AND    tv.nsitecode = " + userInfo.getNmastersitecode() + ""
				+ " AND    rt.nregtypecode=tv1.nregtypecode" + " AND    rst.nregsubtypecode=tv1.nregsubtypecode"
				+ " AND    tv.nregtypecode = " + nregtypecode + "" + " AND    ts.ntranscode = tv.ntransactionstatus"
				+ " AND    tv.nregsubtypecode = " + nregsubtypecode + "" + " AND    ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " AND    tv.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " AND    tv.ncontrolcode = "
				+ controlCode + ") order by ts.ntranscode";
		final List<TransactionStatus> validationList = jdbcTemplate.query(validationQuery, new TransactionStatus());
		return validationList;
	}

	public ResponseEntity<Object> getUsersBySection(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		Map<String, Object> map = new HashMap<>();
		final String strQuery = "select su.nusercode,u.sfirstname ||' '|| u.slastname as susername from sectionusers su "
				+ " join users u on u.nusercode =su.nusercode " + " join userssite us on u.nusercode=us.nusercode "
				+ " join usermultirole urm on urm.nusersitecode =us.nusersitecode "
				+ " join userrole ur on ur.nuserrolecode =urm.nuserrolecode "
				+ " where  ur.nuserrolecode =(select  nuserrolecode from treetemplatetransactionrole ttr "
				+ " join approvalconfig  ac on ac.napprovalconfigcode =ttr.napprovalconfigcode  " + "where "
				+ " ac.nregtypecode=" + (int) inputMap.get("nregtypecode") + " and ac.nregsubtypecode="
				+ (int) inputMap.get("nregsubtypecode") + " and ttr.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " "
				+ " order by ttr.nlevelno desc LIMIT 1) "
				+ " and su.nlabsectioncode in(select nlabsectioncode from labsection where nsectioncode in("
				+ inputMap.get("nsectioncode") + ") and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")  and su.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and us.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and urm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and su.nsitecode="
				+ userInfo.getNtranssitecode() + " " + "group by su.nusercode ,susername";

		final List<Users> lstUsers = jdbcTemplate.query(strQuery, new Users());
		map.put("Users", lstUsers);

		return new ResponseEntity<>(map, HttpStatus.OK);

	}

	public String getSectionByTest(Map<String, Object> inputMap, String ntestcode, UserInfo userInfo) {
		String sectionQuery = "";
		if (inputMap.containsKey("operation")) {
			if (inputMap.get("operation").equals("updateSection")) {
				sectionQuery = "  AND st.nsectioncode <> " + inputMap.get("nsectioncode") + " ";
			}
		}
		final String str = "select st.nsectioncode,st.ssectionname from site s,sitedepartment sd,departmentlab dl,labsection ls,section st"
				+ " where s.nsitecode=sd.nsitecode" + " AND sd.nsitedeptcode=dl.nsitedeptcode"
				+ " AND dl.ndeptlabcode=ls.ndeptlabcode" + " AND s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND sd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + "  AND dl.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " AND sd.nsitecode="
				+ userInfo.getNtranssitecode() + "" + "  AND ls.nsectioncode=st.nsectioncode"
				+ "  AND ls.nsectioncode in(select nsectioncode from "
				+ " (select max(s.ssectionname) ssectionname,ts.nsectioncode,count(ts.nsectioncode) ncount from testsection ts,section s"
				+ " where ntestcode in (" + ntestcode + ") and ts.nsectioncode = s.nsectioncode" + " and ts.nstatus  = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " group by ts.nsectioncode)a where a.ncount=" + inputMap.get("ntestcodesize") + ") " + "  "
				+ sectionQuery + "" + "  AND st.nsectioncode >0 " + " AND ls.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by st.nsectioncode,st.ssectionname";
		return str;
	}

	public Map<String, Object> getFilterSection(Map<String, Object> inputMap, UserInfo userInfo) {
		Map<String, Object> map = new HashMap<>();
		String sectionquery = "select rt.nsectioncode,s.ssectionname from registration r,registrationarno ra,"
				+ " registrationtest rt, section s where  "
				+ " r.npreregno =rt.npreregno and r.npreregno=ra.npreregno and  rt.nsectioncode =s.nsectioncode "
				+ " and r.nregtypecode =" + inputMap.get("nregtypecode") + " and r.nregsubtypecode ="
				+ inputMap.get("nregsubtypecode") + " and r.ndesigntemplatemappingcode ="
				+ inputMap.get("ndesigntemplatemappingcode") + " and ra.napprovalversioncode ="
				+ inputMap.get("napprovalversioncode") + " and s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and s.nsectioncode = any ( select ls.nsectioncode from sectionusers su,labsection ls where "
				+ " ls.nlabsectioncode=su.nlabsectioncode and su.nusercode=" + userInfo.getNusercode()
				+ " and su.nsitecode=" + userInfo.getNtranssitecode() + "   and su.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ls.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by ls.nsectioncode)"
				+ " group by rt.nsectioncode,s.ssectionname;";

		final List<Section> lstSection = ((List<Section>) jdbcTemplate.query(sectionquery, new Section()));
		map.put("UserSection", lstSection);
		return map;
	}

	@Override
	public ResponseEntity<Object> createFilterName(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		Map<String, Object> objMap = new HashMap<>();
		String transcode = "";
		String sectioncode = "";
		final List<FilterName> lstFilterByName = projectDAOSupport
				.getFilterListByName(inputMap.get("sfiltername").toString(), userInfo);
		if (lstFilterByName.isEmpty()) {
			if (inputMap.containsKey("nsectioncode")
					&& inputMap.get("nsectioncode").getClass().getName() == "java.lang.String"
					&& inputMap.containsKey("ntranscode")
					&& inputMap.get("ntranscode").getClass().getName() == "java.lang.String") {
				sectioncode = "(jsontempdata->>'nsectioncode')::text in ('" + inputMap.get("nsectioncode") + "') ";
				transcode = "(jsontempdata->>'ntranscode')::text in ('" + inputMap.get("ntranscode") + "') ";
			} else if (inputMap.containsKey("nsectioncode")
					&& inputMap.get("nsectioncode").getClass().getName() == "java.lang.String") {
				sectioncode = "(jsontempdata->>'nsectioncode')::text in ('" + inputMap.get("nsectioncode") + "') ";
			} else if (inputMap.containsKey("ntranscode")
					&& inputMap.get("ntranscode").getClass().getName() == "java.lang.String") {
				transcode = "(jsontempdata->>'ntranscode')::text in ('" + inputMap.get("ntranscode") + "') ";

			} else {
				transcode = "(jsontempdata->'ntranscode')::int=" + inputMap.get("ntranscode") + " ";
				sectioncode = "(jsontempdata->'nsectioncode')::int = " + inputMap.get("nsectioncode") + " ";
			}
			final String strValidationQuery = "select json_agg(jsondata || jsontempdata) as jsondata from filtername where nformcode="
					+ userInfo.getNformcode() + " and nusercode=" + userInfo.getNusercode() + " "
					+ " and nuserrolecode=" + userInfo.getNuserrole() + " and nsitecode=" + userInfo.getNtranssitecode()
					+ " " + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and (jsondata->'nsampletypecode')::int=" + inputMap.get("nsampletypecode") + " "
					+ " and (jsondata->'nregtypecode')::int=" + inputMap.get("nregtypecode") + " "
					+ " and (jsondata->'nregsubtypecode')::int=" + inputMap.get("nregsubtypecode") + " " + " and "
					+ transcode + " " + " and (jsontempdata->>'napproveconfversioncode')::int="
					+ inputMap.get("napprovalversioncode") + " "
					+ " and (jsontempdata->'ndesigntemplatemappingcode')::int="
					+ inputMap.get("ndesigntemplatemappingcode") + " " + " and " + sectioncode + " "
					+ " and (jsontempdata->>'ntestcode')::text in ('" + inputMap.get("ntestcode") + "') "
					+ " and (jsontempdata->>'DbFromDate')='" + inputMap.get("fromdate") + "' "
					+ " and (jsontempdata->>'DbToDate')='" + inputMap.get("todate") + "' ";

			final String strValidationFilter = jdbcTemplate.queryForObject(strValidationQuery, String.class);

			final List<Map<String, Object>> lstValidationFilter = strValidationFilter != null
					? objMapper.readValue(strValidationFilter, new TypeReference<List<Map<String, Object>>>() {
					})
					: new ArrayList<Map<String, Object>>();
			if (lstValidationFilter.isEmpty()) {

				projectDAOSupport.createFilterData(inputMap, userInfo);
				final List<FilterName> lstFilterName = getFilterName(userInfo);
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
	public List<FilterName> getFilterName(UserInfo userInfo) throws Exception {

		final String sFilterQry = "select * from filtername where nformcode=" + userInfo.getNformcode()
				+ " and nusercode=" + userInfo.getNusercode() + "" + " and nuserrolecode=" + userInfo.getNuserrole()
				+ " and nsitecode=" + userInfo.getNtranssitecode() + " " + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by 1 desc  ";

		return jdbcTemplate.query(sFilterQry, new FilterName());
	}

	@Override
	public ResponseEntity<Object> getJobAllocationFilter(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> returnMap = new HashMap<String, Object>();
		final ObjectMapper objMapper = new ObjectMapper();
		String transcode = "";
		String sectioncode = "";
		final String strQuery = "select json_agg(jsondata || jsontempdata) as jsondata from filtername where nformcode="
				+ userInfo.getNformcode() + " and nusercode=" + userInfo.getNusercode() + " " + " and nuserrolecode="
				+ userInfo.getNuserrole() + " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nfilternamecode="
				+ inputMap.get("nfilternamecode");

		String strFilter = jdbcTemplate.queryForObject(strQuery, String.class);

		final List<Map<String, Object>> lstfilterDetail = strFilter != null
				? objMapper.readValue(strFilter, new TypeReference<List<Map<String, Object>>>() {
				})
				: new ArrayList<Map<String, Object>>();
		if (!lstfilterDetail.isEmpty()) {
			if (inputMap.containsKey("nsectioncode")
					&& inputMap.get("nsectioncode").getClass().getName() == "java.lang.String"
					&& inputMap.containsKey("ntranscode")
					&& inputMap.get("ntranscode").getClass().getName() == "java.lang.String") {
				sectioncode = "(jsontempdata->>'nsectioncode')::text in ('" + inputMap.get("nsectioncode") + "') ";
				transcode = "(jsontempdata->>'ntranscode')::text in ('" + inputMap.get("ntranscode") + "') ";
			} else if (inputMap.containsKey("nsectioncode")
					&& inputMap.get("nsectioncode").getClass().getName() == "java.lang.String") {
				sectioncode = "(jsontempdata->>'nsectioncode')::text in ('" + inputMap.get("nsectioncode") + "') ";
			} else if (inputMap.containsKey("ntranscode")
					&& inputMap.get("ntranscode").getClass().getName() == "java.lang.String") {
				transcode = "(jsontempdata->>'ntranscode')::text in ('" + inputMap.get("ntranscode") + "') ";

			} else {
				transcode = "(jsontempdata->'ntranscode')::int=" + inputMap.get("ntranscode") + " ";
				sectioncode = "(jsontempdata->'nsectioncode')::int = " + inputMap.get("nsectioncode") + " ";
			}
			final String strValidationQuery = "select json_agg(jsondata || jsontempdata) as jsondata from filtername where nformcode="
					+ userInfo.getNformcode() + " and nusercode=" + userInfo.getNusercode() + " "
					+ " and nuserrolecode=" + userInfo.getNuserrole() + " and nsitecode=" + userInfo.getNtranssitecode()
					+ " " + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and (jsondata->'nsampletypecode')::int=" + inputMap.get("nsampletypecode") + " "
					+ " and (jsondata->'nregtypecode')::int=" + inputMap.get("nregtypecode") + " "
					+ " and (jsondata->'nregsubtypecode')::int=" + inputMap.get("nregsubtypecode") + " " + " and "
					+ transcode + " " + " and (jsontempdata->>'napproveconfversioncode')::int="
					+ inputMap.get("napprovalversioncode") + " "
					+ " and (jsontempdata->'ndesigntemplatemappingcode')::int="
					+ inputMap.get("ndesigntemplatemappingcode") + " " + " and (jsontempdata->>'ntestcode')::text in ('"
					+ inputMap.get("ntestcode") + "') " + " and " + sectioncode + " "
					+ " and (jsontempdata->>'DbFromDate')='" + inputMap.get("fromdate") + "' "
					+ " and (jsontempdata->>'DbToDate')='" + inputMap.get("todate") + "'" + " and nfilternamecode="
					+ inputMap.get("nfilternamecode") + "";

			final String strValidationFilter = jdbcTemplate.queryForObject(strValidationQuery, String.class);

			final List<Map<String, Object>> lstValidationFilter = strValidationFilter != null
					? objMapper.readValue(strValidationFilter, new TypeReference<List<Map<String, Object>>>() {
					})
					: new ArrayList<Map<String, Object>>();
			if (lstValidationFilter.isEmpty()) {

				final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
				final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());

				final String fromDateUI = LocalDateTime
						.parse(lstfilterDetail.get(0).get("fromdate").toString(), dbPattern).format(uiPattern);
				final String toDateUI = LocalDateTime.parse(lstfilterDetail.get(0).get("todate").toString(), dbPattern)
						.format(uiPattern);

				returnMap.put("fromDate", fromDateUI);
				returnMap.put("toDate", toDateUI);
				returnMap.put("realFromDate", fromDateUI);
				returnMap.put("realToDate", toDateUI);
				String fromDate = dateUtilityFunction.instantDateToString(dateUtilityFunction
						.convertStringDateToUTC(lstfilterDetail.get(0).get("fromdate").toString(), userInfo, true));
				String toDate = dateUtilityFunction.instantDateToString(dateUtilityFunction
						.convertStringDateToUTC(lstfilterDetail.get(0).get("todate").toString(), userInfo, true));

				inputMap.put("fromdate", fromDate);
				inputMap.put("todate", toDate);

				inputMap.put("filterDetailValue", lstfilterDetail);
				returnMap.putAll((Map<String, Object>) getSampleType(inputMap, userInfo).getBody());
				projectDAOSupport.updateFilterDetail(inputMap, userInfo);
				returnMap.putAll(getJobAllocationSamples(inputMap, userInfo));

				List<Map<String, Object>> lstmapObjectsamples = objMapper.convertValue(returnMap.get("JA_SAMPLE"),
						new TypeReference<List<Map<String, Object>>>() {
						});

				if (lstmapObjectsamples != null && !lstmapObjectsamples.isEmpty()) {
					returnMap.put("JASelectedSample",
							Arrays.asList(lstmapObjectsamples.get(lstmapObjectsamples.size() - 1)));

				} else {
					if (lstmapObjectsamples != null) {
						returnMap.put("JASelectedSample", lstmapObjectsamples);
					}
					returnMap.put("RegistrationAttachment", lstmapObjectsamples);
					returnMap.put("RegistrationComment", lstmapObjectsamples);
				}
				List<Map<String, Object>> lstmapObjectsubsample = objMapper.convertValue(returnMap.get("JA_SUBSAMPLE"),
						new TypeReference<List<Map<String, Object>>>() {
						});
				if ((boolean) inputMap.get("nneedsubsample")) {
					if (lstmapObjectsubsample != null && !lstmapObjectsubsample.isEmpty()) {
						returnMap.put("JASelectedSubSample",
								Arrays.asList(lstmapObjectsubsample.get(lstmapObjectsubsample.size() - 1)));
					}
				} else {
					returnMap.put("JASelectedSubSample", lstmapObjectsubsample);
				}
				List<Map<String, Object>> lstmapObjecttest = objMapper.convertValue(returnMap.get("JA_TEST"),
						new TypeReference<List<Map<String, Object>>>() {
						});
				if (lstmapObjecttest != null && !lstmapObjecttest.isEmpty()) {
					returnMap.put("JASelectedTest", Arrays.asList(lstmapObjecttest.get(lstmapObjecttest.size() - 1)));
				} else {
					returnMap.put("JASelectedTest", new ArrayList<>());
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

}
