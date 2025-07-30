package com.agaramtech.qualis.joballocation.service.TestWiseMyJobs;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.attachmentscomments.service.attachments.AttachmentDAO;
import com.agaramtech.qualis.attachmentscomments.service.comments.CommentDAO;
import com.agaramtech.qualis.basemaster.model.TransactionStatus;
import com.agaramtech.qualis.configuration.model.ApprovalConfigAutoapproval;
import com.agaramtech.qualis.configuration.model.DesignTemplateMapping;
import com.agaramtech.qualis.configuration.model.FilterName;
import com.agaramtech.qualis.configuration.model.SampleType;
import com.agaramtech.qualis.credential.model.ControlMaster;
import com.agaramtech.qualis.dynamicpreregdesign.model.ReactRegistrationTemplate;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegSubTypeConfigVersion;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationSubType;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationType;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.TransactionDAOSupport;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.organization.model.Section;
import com.agaramtech.qualis.registration.model.RegistrationTestHistory;
import com.agaramtech.qualis.testgroup.model.TestGroupTest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

@Transactional(rollbackFor = Exception.class)
@Repository
@AllArgsConstructor
public class TestWiseMyJobsDAOImpl implements TestWiseMyJobsDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestWiseMyJobsDAOImpl.class);

	private StringUtilityFunction stringUtilityFunction;
	private CommonFunction commonFunction;
	private JdbcTemplate jdbcTemplate;
	private JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private DateTimeUtilityFunction dateUtilityFunction;
	private AuditUtilityFunction auditUtilityFunction;
	private ProjectDAOSupport projectDAOSupport;
	private final TransactionDAOSupport transactionDAOSupport;
	private final CommentDAO commentDAO;
	private final AttachmentDAO attachmentDAO;

	/**
	 * This method is used to retrieve list of tests which can be used at initial
	 * get of screen loading. This method will also fetches all filter input details
	 */
	@Override
	public ResponseEntity<Object> getTestWiseMyJobs(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		final Map<String, Object> map = new HashMap<String, Object>();
		ObjectMapper objMapper = new ObjectMapper();

		String dfromdate = "";
		String dtodate = "";

		final String strQueryObj = "select json_agg(jsondata || jsontempdata) as jsondata from filterdetail where nformcode="
				+ userInfo.getNformcode() + " and nusercode=" + userInfo.getNusercode() + " " + " and nuserrolecode="
				+ userInfo.getNuserrole() + " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final String strFilter = (String) jdbcUtilityFunction.queryForObject(strQueryObj, String.class, jdbcTemplate);

		final List<Map<String, Object>> lstfilterDetail = strFilter != null
				? objMapper.readValue(strFilter, new TypeReference<List<Map<String, Object>>>() {
				})
						: new ArrayList<Map<String, Object>>();

		if (!lstfilterDetail.isEmpty() && lstfilterDetail.get(0).containsKey("FromDate")
				&& lstfilterDetail.get(0).containsKey("ToDate")) {

			Instant instantFromDate = dateUtilityFunction
					.convertStringDateToUTC(lstfilterDetail.get(0).get("FromDate").toString(), userInfo, true);
			Instant instantToDate = dateUtilityFunction
					.convertStringDateToUTC(lstfilterDetail.get(0).get("ToDate").toString(), userInfo, true);

			dfromdate = dateUtilityFunction.instantDateToString(instantFromDate);
			dtodate = dateUtilityFunction.instantDateToString(instantToDate);

			map.put("FromDate", dfromdate);
			map.put("ToDate", dtodate);
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

			map.put("fromdate", formattedFromString);
			map.put("todate", formattedToString);
			inputMap.put("filterDetailValue", lstfilterDetail);
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

		//map.putAll((Map<String, Object>) transactionDAOSupport.getSampleType(userInfo));
		map.putAll((Map<String, Object>) getSampleType(inputMap, userInfo).getBody());
		//List<FilterName> lstFilterName = (List<FilterName>) projectDAOSupport.(userInfo);
		List<FilterName> lstFilterName=getFilterName(userInfo);
		map.put("FilterName",lstFilterName);

		return new ResponseEntity<>(map, HttpStatus.OK);
		// }
		// else {
		// return new
		// ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_USERNOTINMYJOBFLOW",userInfo.getSlanguagefilename()),
		// HttpStatus.EXPECTATION_FAILED);
		// }
	}

	/**
	 * THis method is used to get the Sample Type of the registration type and
	 * registration sub type for which a version of approval configuration is
	 * approved.
	 */
	@Override
	public ResponseEntity<Object> getSampleType(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		final Map<String, Object> map = new HashMap<String, Object>();
		ObjectMapper objMapper = new ObjectMapper();

		//		String strsampletypequery = " select st.nsampletypecode,coalesce(st.jsondata->'sampletypename'->>'"
		//				+ userInfo.getSlanguagetypecode() + "' , st.jsondata->'sampletypename'->>'en-US') as ssampletypename "
		//				+ " from sampletype st,approvalconfig ac,approvalconfigversion acv,registrationtype rt,registrationsubtype rst "
		//				+ " where acv.ntransactionstatus =" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
		//				+ " and acv.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
		//				+ " and ac.napprovalconfigcode = acv.napprovalconfigcode  and ac.nstatus = "
		//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
		//				+ " and rt.nregtypecode = ac.nregtypecode  and rt.nstatus = "
		//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
		//				+ " and rt.nregtypecode = rst.nregtypecode and rst.nregsubtypecode = ac.nregsubtypecode "
		//				+ " and rst.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
		//				+ " and rt.nsampletypecode = st.nsampletypecode" + " and acv.nsitecode ="
		//				+ userInfo.getNmastersitecode() + " and st.nsitecode =" + userInfo.getNmastersitecode()
		//				+ " and ac.nsitecode =" + userInfo.getNmastersitecode() + " and rt.nsitecode ="
		//				+ userInfo.getNmastersitecode() + " and rst.nsitecode =" + userInfo.getNmastersitecode()
		//				+ " and st.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
		//				+ " and acv.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
		//				+ " and st.nsampletypecode > 0  group by st.nsampletypecode,st.jsondata->'sampletypename'->>'"
		//				+ userInfo.getSlanguagetypecode() + "' order by st.nsorter";
		//final List<SampleType> lstSampleType = jdbcTemplate.query(strsampletypequery, new SampleType());

		//This medthod was call in the transactionDAOSupport for the commmon get sample type
		final List<SampleType> lstSampleType = transactionDAOSupport.getSampleType(userInfo);

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
			map.putAll((Map<String, Object>) getRegistrationType(inputMap, userInfo).getBody());
		} else {
			map.put("realSampleTypeValue", lstSampleType);
			map.put("defaultSampleTypeValue", lstSampleType);

		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getRegistrationType(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();
		//		String Str = "Select * from sampletype  where  nsampletypecode=" + inputMap.get("nsampletypecode");
		final ObjectMapper objMapper = new ObjectMapper();
		//
		//		SampleType obj = (SampleType) jdbcUtilityFunction.queryForObject(Str, SampleType.class, jdbcTemplate);

		final int nsampletypecode = inputMap.get("nsampletypecode") != null
				? ((Short) inputMap.get("nsampletypecode")).intValue()
						: 0;

		//		String ValidationQuery = "";
		//		if (obj.getNtransfiltertypecode() != -1 && userInfo.getNusercode() != -1) {
		//			int nmappingfieldcode = (obj.getNtransfiltertypecode() == 1) ? userInfo.getNdeptcode()
		//					: userInfo.getNuserrole();
		//			ValidationQuery = " and rst.nregsubtypecode in( SELECT rs.nregsubtypecode "
		//					+ "		FROM registrationsubtype rs "
		//					+ "		INNER JOIN transactionfiltertypeconfig ttc ON rs.nregsubtypecode = ttc.nregsubtypecode "
		//					+ "		LEFT JOIN transactionusers tu ON tu.ntransfiltertypeconfigcode = ttc.ntransfiltertypeconfigcode "
		//					+ "		WHERE ( ttc.nneedalluser = " + Enumeration.TransactionStatus.YES.gettransactionstatus()
		//					+ "  and ttc.nstatus =  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		//					+ " AND ttc.nmappingfieldcode = " + nmappingfieldcode + ") " + "	 OR "
		//					+ "	( ttc.nneedalluser = " + Enumeration.TransactionStatus.NO.gettransactionstatus() + "  "
		//					+ "	  AND ttc.nmappingfieldcode =" + nmappingfieldcode + "	  AND tu.nusercode ="
		//					+ userInfo.getNusercode() + "   and ttc.nstatus = "
		//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "   and tu.nstatus = "
		//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") " + "  OR "
		//					+ "	( ttc.nneedalluser = " + Enumeration.TransactionStatus.NO.gettransactionstatus() + "  "
		//					+ " AND ttc.nmappingfieldcode = -1 " + " AND tu.nusercode =" + userInfo.getNusercode()
		//					+ " and ttc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		//					+ " and tu.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") "
		//					+ "	 AND rs.nregtypecode = rt.nregtypecode ) ";
		//		}
		//		String strregtypequery = "select rt.nregtypecode,coalesce(rt.jsondata->'sregtypename'->>'"
		//				+ userInfo.getSlanguagetypecode() + "',rt.jsondata->'sregtypename'->>'en-US')  as sregtypename "
		//				+ "from approvalconfig ac,approvalconfigversion acv,registrationtype rt,registrationsubtype rst "
		//				+ " where acv.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
		//				+ " " + " and ac.napprovalconfigcode = acv.napprovalconfigcode and rt.nregtypecode = ac.nregtypecode "
		//				+ " and rt.nregtypecode = rst.nregtypecode and rst.nregsubtypecode = ac.nregsubtypecode  "
		//				+ " and acv.nsitecode = " + userInfo.getNmastersitecode() + " and rt.nsampletypecode = "
		//				+ inputMap.get("nsampletypecode") + "  " + " and acv.nsitecode =" + userInfo.getNmastersitecode()
		//				+ " and ac.nsitecode =" + userInfo.getNmastersitecode() + " and rt.nsitecode ="
		//				+ userInfo.getNmastersitecode() + " and rst.nsitecode =" + userInfo.getNmastersitecode()
		//				+ " and acv.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
		//				+ " and rt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
		//				+ " and rst.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
		//				+ " and ac.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
		//				+ " and rt.nregtypecode > 0 " + ValidationQuery
		//				+ " group by rt.nregtypecode,sregtypename  order by rt.nregtypecode desc";
		//final List<RegistrationType> lstRegistrationType = jdbcTemplate.query(strregtypequery, new RegistrationType());

		final List<RegistrationType> lstRegistrationType=transactionDAOSupport.getRegistrationType(nsampletypecode, userInfo);
		if (!lstRegistrationType.isEmpty()) {

			final RegistrationType filterRegType = inputMap.containsKey("filterDetailValue")
					&& !((List<Map<String, Object>>) inputMap.get("filterDetailValue")).isEmpty()
					&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0)
					.containsKey("regTypeValue") ? objMapper
							.convertValue(((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0)
									.get("regTypeValue"), RegistrationType.class)
							: lstRegistrationType.get(0);

			map.put("defaultRegistrationType", filterRegType);
			map.put("defaultRegTypeValue", filterRegType);
			map.put("RegistrationType", lstRegistrationType);
			map.put("realRegistrationTypeList", lstRegistrationType);
			map.put("realRegTypeValue", filterRegType);
			inputMap.put("realRegTypeValue", filterRegType);
			inputMap.put("defaultRegistrationType", filterRegType);
			inputMap.put("nregtypecode", filterRegType.getNregtypecode());

			map.putAll((Map<String, Object>) getRegistrationsubType(inputMap, userInfo).getBody());

		} else {
			map.put("realRegTypeValue", lstRegistrationType);
			map.put("defaultRegTypeValue", lstRegistrationType);

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
				+ " from approvalconfig ac,approvalconfigversion acv,registrationtype rt,registrationsubtype rst "
				+ " where acv.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and ac.napprovalconfigcode = acv.napprovalconfigcode and rt.nregtypecode = ac.nregtypecode "
				+ " and rt.nregtypecode = rst.nregtypecode and rst.nregsubtypecode = ac.nregsubtypecode  "
				+ " and acv.nsitecode = " + userInfo.getNmastersitecode() + " and rt.nsampletypecode = "
				+ inputMap.get("nsampletypecode") + "  " + " and acv.nsitecode =" + userInfo.getNmastersitecode()
				+ " and ac.nsitecode =" + userInfo.getNmastersitecode() + " and rt.nsitecode ="
				+ userInfo.getNmastersitecode() + " and rst.nsitecode =" + userInfo.getNmastersitecode()
				+ " and acv.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rst.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and ac.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rt.nregtypecode > 0 " + ValidationQuery
				+ " group by rt.nregtypecode,sregtypename  order by rt.nregtypecode desc";
		final List<RegistrationType> lstRegistrationType = jdbcTemplate.query(strregtypequery, new RegistrationType());
		if (!lstRegistrationType.isEmpty()) {
			map.put("RegistrationType", lstRegistrationType);
			map.put("defaultRegTypeValue", lstRegistrationType.get(0));
			inputMap.put("realRegTypeValue", lstRegistrationType.get(0));
			inputMap.put("nregtypecode", lstRegistrationType.get(0).getNregtypecode());
			map.putAll((Map<String, Object>) getRegistrationsubTypeByRegType(inputMap, userInfo).getBody());
		} else {
			map.put("defaultRegTypeValue", lstRegistrationType);
			map.put("defaultRegSubTypeValue", null);

		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getRegistrationsubType(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();
		ObjectMapper objMapper = new ObjectMapper();
		String Str = "Select * from registrationtype rt,sampletype st " + "where rt.nsampletypecode=st.nsampletypecode "
				+ " and st.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and rt.nregTypeCode=" + inputMap.get("nregtypecode");

		final SampleType obj = (SampleType) jdbcUtilityFunction.queryForObject(Str, SampleType.class, jdbcTemplate);
		String validationQuery = "";
		if (obj.getNtransfiltertypecode() != -1 && userInfo.getNusercode() != -1) {
			final int nmappingfieldcode = (obj.getNtransfiltertypecode() == 1) ? userInfo.getNdeptcode()
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
					+ " AND ttc.nmappingfieldcode = -1 " + " AND tu.nusercode =" + userInfo.getNusercode()
					+ " and ttc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tu.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") "
					+ " AND rs.nregtypecode = " + inputMap.get("nregtypecode") + ")";
		}
		String strregsubtypequery = "select rst.nregsubtypecode,rst.nregtypecode,coalesce (rst.jsondata->'sregsubtypename'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',rst.jsondata->'sregsubtypename'->>'en-US') as sregsubtypename,cast(rsc.jsondata->>'nneedsubsample' as boolean) nneedsubsample,"
				+ "cast(rsc.jsondata->>'nneedmyjob' as boolean) nneedmyjob, "
				+ "cast(rsc.jsondata->>'nneedtemplatebasedflow' as boolean) nneedtemplatebasedflow, "
				+ " cast(rsc.jsondata->>'nneedtestinitiate' as boolean) nneedtestinitiate "
				+ "from approvalconfig ac,approvalconfigversion acv,registrationsubtype rst,regsubtypeconfigversion rsc "
				+ "where acv.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and rsc.napprovalconfigcode = ac.napprovalconfigcode" + " and acv.nsitecode ="
				+ userInfo.getNmastersitecode() + " and ac.nsitecode =" + userInfo.getNmastersitecode()
				+ " and rsc.nsitecode =" + userInfo.getNmastersitecode() + " and rst.nsitecode ="
				+ userInfo.getNmastersitecode() + " and rsc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and rsc.ntransactionstatus = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " "
				+ "and ac.napprovalconfigcode = acv.napprovalconfigcode and rst.nregtypecode = "
				+ inputMap.get("nregtypecode") + " and (rsc.jsondata->'nneedmyjob')::jsonb ='true' "
				+ "and ac.napprovalconfigcode = acv.napprovalconfigcode and rst.nregsubtypecode = ac.nregsubtypecode "
				+ "and rst.nregsubtypecode > 0  and acv.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and rst.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and ac.nstatus = "
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
			map.put("realRegSubTypeValue", filterRegSubType);
			map.put("defaultRegSubTypeValue", filterRegSubType);
			map.put("RegSubTypeValue", filterRegSubType);
			inputMap.put("realRegSubTypeValue", filterRegSubType);
			inputMap.put("nregsubtypecode", filterRegSubType.getNregsubtypecode());
			inputMap.put("nneedsubsample", filterRegSubType.isNneedsubsample());
			inputMap.put("nneedtemplatebasedflow", filterRegSubType.isNneedtemplatebasedflow());
			map.put("nneedsubsample", filterRegSubType.isNneedsubsample());
			map.putAll((Map<String, Object>) getRegistrationTemplateList(inputMap, userInfo).getBody());
			map.putAll((Map<String, Object>) getApprovalConfigVersion(inputMap, userInfo).getBody());
		} else {
			map.put("realRegSubTypeValue", lstRegistrationSubType);
			map.put("defaultRegSubTypeValue", lstRegistrationSubType);

		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getRegistrationsubTypeByRegType(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();
		final ObjectMapper objMapper = new ObjectMapper();

		String Str = "Select * from registrationtype rt,sampletype st "
				+ "where rt.nsampletypecode=st.nsampletypecode and rt.nsitecode=" + userInfo.getNmastersitecode() + " "
				+ " and rt.nregTypeCode=" + inputMap.get("nregtypecode");

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
					+ " AND ttc.nmappingfieldcode = -1 " + " AND tu.nusercode =" + userInfo.getNusercode()
					+ " and ttc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tu.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") "
					+ " AND rs.nregtypecode = " + inputMap.get("nregtypecode") + ")";
		}

		String strregsubtypequery = "select rst.nregsubtypecode,rst.nregtypecode,coalesce (rst.jsondata->'sregsubtypename'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',rst.jsondata->'sregsubtypename'->>'en-US') as sregsubtypename,cast(rsc.jsondata->>'nneedsubsample' as boolean) nneedsubsample,"
				+ "cast(rsc.jsondata->>'nneedmyjob' as boolean) nneedmyjob, "
				+ "cast(rsc.jsondata->>'nneedtemplatebasedflow' as boolean) nneedtemplatebasedflow "
				+ "from approvalconfig ac,approvalconfigversion acv,registrationsubtype rst,regsubtypeconfigversion rsc "
				+ "where " + " acv.nsitecode =" + userInfo.getNmastersitecode() + " and ac.nsitecode ="
				+ userInfo.getNmastersitecode() + " and rsc.nsitecode =" + userInfo.getNmastersitecode()
				+ " and rst.nsitecode =" + userInfo.getNmastersitecode() + " and acv.ntransactionstatus = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and rsc.napprovalconfigcode = ac.napprovalconfigcode and rsc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and rsc.ntransactionstatus = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " "
				+ "and ac.napprovalconfigcode = acv.napprovalconfigcode and rst.nregtypecode = "
				+ inputMap.get("nregtypecode") + " and (rsc.jsondata->'nneedmyjob')::jsonb ='true' "
				+ "and ac.napprovalconfigcode = acv.napprovalconfigcode and rst.nregsubtypecode = ac.nregsubtypecode "
				+ "and rst.nregsubtypecode > 0  and acv.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and rst.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and ac.nstatus = "
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
			map.put("defaultRegSubTypeValue", filterRegSubType);
			map.put("RegSubTypeValue", filterRegSubType);
			inputMap.put("defaultRegistrationSubType", filterRegSubType);
			inputMap.put("nneedtemplatebasedflow", filterRegSubType.isNneedtemplatebasedflow());
			inputMap.put("nneedjoballocation", filterRegSubType.isNneedjoballocation());
			map.put("nneedsubsample", lstRegistrationSubType.get(0).isNneedsubsample());
			inputMap.put("nregsubtypecode", filterRegSubType.getNregsubtypecode()); // ALPD-5266 Added regsubtype by
			// Vishakh to get correct value in
			// advance filter
			map.putAll((Map<String, Object>) getRegistrationTemplateListByApprovalConfigVersion(inputMap, userInfo)
					.getBody());
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

	public ResponseEntity<Object> getRegistrationTemplateList(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();

		final String str = "select dm.ndesigntemplatemappingcode,rt.jsondata,dm.ntransactionstatus,CONCAT(rt.sregtemplatename,'(',cast(dm.nversionno as character varying),')') sregtemplatename from designtemplatemapping dm, "
				+ "reactregistrationtemplate rt where dm.nsitecode =" + userInfo.getNmastersitecode()
				+ " and nregtypecode=" + inputMap.get("nregtypecode") + " and nregsubtypecode="
				+ inputMap.get("nregsubtypecode")
				+ " and rt.nreactregtemplatecode=dm.nreactregtemplatecode and  dm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and dm.ntransactionstatus in ("
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + ") "
				+ " order by dm.ndesigntemplatemappingcode desc";

		List<DesignTemplateMapping> lstReactRegistrationTemplate = jdbcTemplate.query(str, new DesignTemplateMapping());
		if (!lstReactRegistrationTemplate.isEmpty()) {
			map.put("DynamicDesignMapping", lstReactRegistrationTemplate);
			map.put("realDynamicDesignMappingList", lstReactRegistrationTemplate);
			map.put("realDesignTemplateMappingValue", lstReactRegistrationTemplate.get(0));
			map.put("defaultDesignTemplateMappingValue", lstReactRegistrationTemplate.get(0));
			map.put("DesignTemplateMappingValue", lstReactRegistrationTemplate.get(0));
			map.put("ndesigntemplatemappingcode", lstReactRegistrationTemplate.get(0).getNdesigntemplatemappingcode());
			inputMap.put("realDesignTemplateMappingValue", lstReactRegistrationTemplate.get(0));
			inputMap.put("ndesigntemplatemappingcode",
					lstReactRegistrationTemplate.get(0).getNdesigntemplatemappingcode());
		} else {
			map.put("realDesignTemplateMappingValue", lstReactRegistrationTemplate);
			map.put("defaultDesignTemplateMappingValue", lstReactRegistrationTemplate);

		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	public ResponseEntity<Object> getRegistrationTemplateListByApprovalConfigVersion(Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();

		final String str = "select dm.ndesigntemplatemappingcode,rt.jsondata,dm.ntransactionstatus,CONCAT(rt.sregtemplatename,'(',cast(dm.nversionno as character varying),')') sregtemplatename from designtemplatemapping dm, "
				+ " reactregistrationtemplate rt where nregtypecode=" + inputMap.get("nregtypecode")
				+ " and nregsubtypecode=" + inputMap.get("nregsubtypecode")
				+ " and rt.nreactregtemplatecode=dm.nreactregtemplatecode and dm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and dm.ntransactionstatus in ("
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + ") "
				+ "order by dm.ndesigntemplatemappingcode desc";

		List<DesignTemplateMapping> lstReactRegistrationTemplate = jdbcTemplate.query(str, new DesignTemplateMapping());
		if (!lstReactRegistrationTemplate.isEmpty()) {
			map.put("DynamicDesignMapping", lstReactRegistrationTemplate);
			// map.put("realDynamicDesignMappingList", lstReactRegistrationTemplate);
			// map.put("realDesignTemplateMappingValue",
			// lstReactRegistrationTemplate.get(0));
			map.put("defaultDesignTemplateMappingValue", lstReactRegistrationTemplate.get(0));
			map.put("DesignTemplateMappingValue", lstReactRegistrationTemplate.get(0));
			map.put("ndesigntemplatemappingcode", lstReactRegistrationTemplate.get(0).getNdesigntemplatemappingcode());
			inputMap.put("realDesignTemplateMappingValue", lstReactRegistrationTemplate.get(0));
			inputMap.put("ndesigntemplatemappingcode",
					lstReactRegistrationTemplate.get(0).getNdesigntemplatemappingcode());
		} else {
			map.put("realDesignTemplateMappingValue", lstReactRegistrationTemplate);
			map.put("defaultDesignTemplateMappingValue", lstReactRegistrationTemplate);

		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getFilterStatus(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();
		final Map<String, Object> obj = new HashMap<String, Object>();
		final List<Object> lstobj = new ArrayList<Object>();
		List<TransactionStatus> lstfilterdetails = new ArrayList<TransactionStatus>();
		final ObjectMapper objMapper = new ObjectMapper();

		String filterquery = "";
		filterquery = " select sc.ntranscode as ntransactionstatus,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "' ,ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus  "
				+ " from approvalstatusconfig sc,transactionstatus ts where ts.ntranscode = sc.ntranscode  "
				+ " and sc.nregtypecode =" + inputMap.get("nregtypecode") + " and sc.nregsubtypecode="
				+ inputMap.get("nregsubtypecode") + " " + " and sc.nformcode = " + userInfo.getNformcode() + " "
				+ " and sc.nsitecode = " + userInfo.getNmastersitecode() + " and sc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		lstfilterdetails = jdbcTemplate.query(filterquery, new TransactionStatus());

		if (!lstfilterdetails.isEmpty()) {
			TransactionStatus filterTransactionStatus = null;
			if (lstfilterdetails.size() > 1) {
				obj.put("ntransactionstatus", 0);
				obj.put("stransdisplaystatus",
						commonFunction.getMultilingualMessage("IDS_ALL", userInfo.getSlanguagefilename()));
				lstobj.add(obj);
				lstobj.addAll(lstfilterdetails);

				filterTransactionStatus = inputMap.containsKey("filterDetailValue")
						&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0)
						.get("filterStatusValue") != null
						&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0)
						.containsKey("filterStatusValue")
						? objMapper.convertValue(
								((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0)
								.get("filterStatusValue"),
								TransactionStatus.class)
								: objMapper.convertValue(lstobj.get(0), TransactionStatus.class);

				map.put("FilterStatus", lstobj);
				map.put("realFilterStatusList", lstobj);
				map.put("realFilterStatusValue", filterTransactionStatus);
				map.put("defaultFilterStatusValue", filterTransactionStatus);
				inputMap.put("realFilterStatusValue", filterTransactionStatus);

			} else {
				map.put("FilterStatus", lstfilterdetails);
				map.put("realFilterStatusList", lstfilterdetails);
				map.put("realFilterStatusValue", lstfilterdetails.get(0));
				map.put("defaultFilterStatusValue", lstfilterdetails.get(0));
				inputMap.put("realFilterStatusValue", lstfilterdetails.get(0));
				// inputMap.put("ntransactionstatus",
				// String.valueOf(lstfilterdetails.get(0).getNtransactionstatus()));
			}
			// ALPD-5521--Vignesh(10-03-2025)--My Jobs-->At first, alloted records are
			// shown, however the filter test status is displaying for accepted
			String status = "";
			if (inputMap.containsKey("filterDetailValue")
					&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0)
					.get("filterStatusValue") != null
					&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0)
					.containsKey("filterStatusValue")) {
				status = String.valueOf(filterTransactionStatus.getNtransactionstatus());
				// ALPD-5521--Vignesh(19-03-2025)--My Jobs-->At first, alloted records are
				// shown, however the filter test status is displaying for accepted
				// start
				if (status.equals(String.valueOf(Enumeration.TransactionStatus.ALL.gettransactionstatus()))) {
					status = lstfilterdetails.stream()
							.map(objtranscode -> String.valueOf(objtranscode.getNtransactionstatus()))
							.collect(Collectors.joining(","));
				}
				// end

			} else {
				status = lstfilterdetails.stream()
						.map(objtranscode -> String.valueOf(objtranscode.getNtransactionstatus()))
						.collect(Collectors.joining(","));

			}
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
		filterquery = " select sc.ntranscode as ntransactionstatus,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus  "
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
				map.put("FilterStatus", lstobj);
				map.put("defaultFilterStatusValue", lstobj.get(0));

			} else {
				map.put("FilterStatus", lstfilterdetails);
				map.put("defaultFilterStatusValue", lstfilterdetails.get(0));
				// inputMap.put("ntransactionstatus",
				// String.valueOf(lstfilterdetails.get(0).getNtransactionstatus()));
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

	@Override
	public ResponseEntity<Object> getApprovalConfigVersion(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();
		map.putAll((Map<String, Object>) getRegistrationTemplateList(inputMap, userInfo).getBody());
		final String fromDate = dateUtilityFunction.instantDateToString(
				dateUtilityFunction.convertStringDateToUTC((String) inputMap.get("fromdate"), userInfo, true));
		final String toDate = dateUtilityFunction.instantDateToString(
				dateUtilityFunction.convertStringDateToUTC((String) inputMap.get("todate"), userInfo, true));
		final ObjectMapper objMapper = new ObjectMapper();

		String versionquery = "select aca.sversionname,aca.napprovalconfigcode,aca.napprovalconfigversioncode ,acv.ntransactionstatus,acv.ntreeversiontempcode  "
				+ " from registration r,registrationarno rar,registrationhistory rh,approvalconfigautoapproval aca, "
				+ " approvalconfig ac,approvalconfigversion acv  "
				+ " where r.npreregno=rar.npreregno and r.npreregno=rh.npreregno "
				+ " and rar.napprovalversioncode=acv.napproveconfversioncode "
				+ " and acv.napproveconfversioncode=aca.napprovalconfigversioncode "
				+ " and r.nregtypecode=ac.nregtypecode and r.nregsubtypecode=ac.nregsubtypecode "
				+ " and r.nregtypecode =" + inputMap.get("nregtypecode") + " and r.nregsubtypecode ="
				+ inputMap.get("nregsubtypecode") + "" + " and acv.nsitecode =" + userInfo.getNmastersitecode()
				+ " and ac.nsitecode =" + userInfo.getNmastersitecode() + " and aca.nsitecode ="
				+ userInfo.getNmastersitecode() + " and rh.ntransactionstatus = "
				+ Enumeration.TransactionStatus.REGISTERED.gettransactionstatus() + " and acv.nsitecode ="
				+ userInfo.getNmastersitecode() + " and rh.dtransactiondate between '" + fromDate + "' and '" + toDate
				+ "' and r.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rar.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rh.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and ac.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and ac.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and acv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and aca.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and  rh .nsitecode="
				+ userInfo.getNtranssitecode() + " and rar.nsitecode=" + userInfo.getNtranssitecode()
				+ " and r.nsitecode=" + userInfo.getNtranssitecode()
				+ " group by acv.ntransactionstatus,aca.napprovalconfigversioncode,acv.ntreeversiontempcode,aca.napprovalconfigcode,aca.sversionname";
		final List<ApprovalConfigAutoapproval> lstApprovalConfigVersion = jdbcTemplate.query(versionquery,
				new ApprovalConfigAutoapproval());

		int ndesigntemplatemappingcode = (int) map.get("ndesigntemplatemappingcode");
		inputMap.put("ndesigntemplatemappingcode", ndesigntemplatemappingcode);
		map.put("DynamicDesign",
				projectDAOSupport.getTemplateDesign(ndesigntemplatemappingcode, userInfo.getNformcode()));
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

			} else {
				// ALPD-5614-Added by Vignesh R(27-03-2025)-->My Jobs screen -> Data's are not
				// displaying.
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
			}

			final Integer count = projectDAOSupport.getApprovalConfigVersionByUserRoleTemplate(
					lstApprovalConfigVersion.get(0).getNapprovalconfigversioncode(), userInfo);
			if (count == 0) {
				map.put(Enumeration.ReturnStatus.SUCCESS.getreturnstatus(), (commonFunction
						.getMultilingualMessage("IDS_USERNOTINMYJOBFLOW", userInfo.getSlanguagefilename())));
				return new ResponseEntity<>(map, HttpStatus.OK);
			}
		} else {
			map.put("realApprovalVersionValue", lstApprovalConfigVersion);
			map.put("defaultApprovalVersionValue", lstApprovalConfigVersion);
		}
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

	@Override
	public ResponseEntity<Object> getApprovalConfigVersionByRegSubType(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();
		map.putAll((Map<String, Object>) getRegistrationTemplateList(inputMap, userInfo).getBody());
		final String fromDate = dateUtilityFunction.instantDateToString(
				dateUtilityFunction.convertStringDateToUTC((String) inputMap.get("fromdate"), userInfo, true));
		final String toDate = dateUtilityFunction.instantDateToString(
				dateUtilityFunction.convertStringDateToUTC((String) inputMap.get("todate"), userInfo, true));

		String versionquery = "select aca.sversionname,aca.napprovalconfigcode,aca.napprovalconfigversioncode ,acv.ntransactionstatus,acv.ntreeversiontempcode  "
				+ " from registration r,registrationarno rar,registrationhistory rh,approvalconfigautoapproval aca, "
				+ " approvalconfig ac,approvalconfigversion acv  "
				+ " where r.npreregno = rar.npreregno and r.npreregno=rh.npreregno "
				+ " and rar.napprovalversioncode = acv.napproveconfversioncode "
				+ " and acv.napproveconfversioncode=aca.napprovalconfigversioncode "
				+ " and r.nregtypecode=ac.nregtypecode and r.nregsubtypecode=ac.nregsubtypecode "
				+ " and acv.nsitecode =" + userInfo.getNmastersitecode() + " and ac.nsitecode ="
				+ userInfo.getNmastersitecode() + " and aca.nsitecode =" + userInfo.getNmastersitecode()
				+ " and r.nregtypecode =" + inputMap.get("nregtypecode") + " and r.nregsubtypecode ="
				+ inputMap.get("nregsubtypecode") + " " + " and rh.ntransactionstatus = "
				+ Enumeration.TransactionStatus.REGISTERED.gettransactionstatus() + " " + " and acv.nsitecode ="
				+ userInfo.getNmastersitecode() + " " + " and rh.dtransactiondate between '" + fromDate + "' and '"
				+ toDate + "' " + " and r.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rar.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rh.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and ac.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and ac.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and acv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and aca.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and rh .nsitecode="
				+ userInfo.getNtranssitecode() + " and rar.nsitecode=" + userInfo.getNtranssitecode()
				+ " and r.nsitecode=" + userInfo.getNtranssitecode()
				+ " group by acv.ntransactionstatus,aca.napprovalconfigversioncode,acv.ntreeversiontempcode,aca.napprovalconfigcode,aca.sversionname";
		final List<ApprovalConfigAutoapproval> lstApprovalConfigVersion = jdbcTemplate.query(versionquery,
				new ApprovalConfigAutoapproval());

		final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());
		final String fromDateUI = LocalDateTime.parse((String) inputMap.get("fromdate"), dbPattern).format(uiPattern);
		final String toDateUI = LocalDateTime.parse((String) inputMap.get("todate"), dbPattern).format(uiPattern);
		map.put("fromDate", fromDateUI);
		map.put("toDate", toDateUI);

		if (map.containsKey("ndesigntemplatemappingcode")) {
			int ndesigntemplatemappingcode = (int) map.get("ndesigntemplatemappingcode");
			inputMap.put("ndesigntemplatemappingcode", ndesigntemplatemappingcode);
			map.put("DynamicDesign",
					projectDAOSupport.getTemplateDesign(ndesigntemplatemappingcode, userInfo.getNformcode()));
			if (!lstApprovalConfigVersion.isEmpty()) {
				List<ApprovalConfigAutoapproval> approvedApprovalVersionList = lstApprovalConfigVersion.stream()
						.filter(obj -> obj.getNtransactionstatus() == Enumeration.TransactionStatus.APPROVED
						.gettransactionstatus())
						.collect(Collectors.toList());

				if (approvedApprovalVersionList != null && !approvedApprovalVersionList.isEmpty()) {
					inputMap.put("napprovalconfigversioncode",
							approvedApprovalVersionList.get(0).getNapprovalconfigversioncode());
					map.put("defaultApprovalVersionValue", approvedApprovalVersionList.get(0));

				} else {
					inputMap.put("napprovalconfigversioncode",
							lstApprovalConfigVersion.get(0).getNapprovalconfigversioncode());
					map.put("defaultApprovalVersionValue", lstApprovalConfigVersion.get(0));

				}
				final Integer count = projectDAOSupport.getApprovalConfigVersionByUserRoleTemplate(
						lstApprovalConfigVersion.get(0).getNapprovalconfigversioncode(), userInfo);
				if (count == 0) {
					map.put(Enumeration.ReturnStatus.SUCCESS.getreturnstatus(), (commonFunction
							.getMultilingualMessage("IDS_USERNOTINMYJOBFLOW", userInfo.getSlanguagefilename())));
					return new ResponseEntity<>(map, HttpStatus.OK);
				}

			} else {
				map.put("defaultApprovalVersionValue", lstApprovalConfigVersion);
			}
			map.put("ApprovalConfigVersion", lstApprovalConfigVersion);
			map.putAll((Map<String, Object>) getRegistrationTemplateListByApprovalConfigVersion(inputMap, userInfo)
					.getBody());
			map.putAll((Map<String, Object>) getFilterStatusByApproveVersion(inputMap, userInfo).getBody());
			map.putAll((Map<String, Object>) getSectionByApproveVersion(inputMap, userInfo).getBody());
		}
		return new ResponseEntity<>(map, HttpStatus.OK);

	}

	public DesignTemplateMapping getApprovedRegistrationTemplate(short regTypeCode, short regSubTypeCode)
			throws Exception {

		final String str = " select nreactregtemplatecode,ndesigntemplatemappingcode from designtemplatemapping "
				+ " where  ntransactionstatus= " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and nregtypecode=" + regTypeCode + " " + " and nregsubtypecode=" + regSubTypeCode + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return (DesignTemplateMapping) jdbcUtilityFunction.queryForObject(str, DesignTemplateMapping.class,
				jdbcTemplate);

	}

	@Override
	public ResponseEntity<Object> getSection(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();
		final Map<String, Object> obj = new HashMap<String, Object>();
		List<Section> lstSection = new ArrayList<Section>();
		List<Object> lstObj = new ArrayList<Object>();
		String sectionquery = "select nsectioncode,ssectionname from section where  nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and nsectioncode = any ( select ls.nsectioncode from sectionusers su,labsection ls where su.nusercode ="
				+ userInfo.getNusercode() + " " + " and ls.nlabsectioncode=su.nlabsectioncode  	and su.nsitecode="
				+ userInfo.getNtranssitecode() + " " + " and su.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ls.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by ls.nsectioncode)";
		lstSection = (jdbcTemplate.query(sectionquery, new Section()));

		if (!lstSection.isEmpty()) {

			if (lstSection.size() > 1) {
				obj.put("nsectioncode", 0);
				obj.put("ssectionname",
						commonFunction.getMultilingualMessage("IDS_ALL", userInfo.getSlanguagefilename()));
				lstObj.add(obj);
				lstObj.addAll(lstSection);
				map.put("UserSection", lstObj);
				map.put("realUserSectionList", lstObj);
				map.put("realUserSectionValue", lstObj.get(0));
				map.put("defaultUserSectionValue", lstObj.get(0));

			} else {
				map.put("UserSection", lstSection);
				map.put("realUserSectionList", lstSection);
				map.put("realUserSectionValue", lstSection.get(0));
				map.put("defaultUserSectionValue", lstSection.get(0));
			}
			String section = lstSection.stream().map(objsectioncode -> String.valueOf(objsectioncode.getNsectioncode()))
					.collect(Collectors.joining(","));
			inputMap.put("nsectioncode", section);

			map.putAll((Map<String, Object>) getTestCombo(inputMap, userInfo).getBody());
		} else {
			map.put("realUserSectionValue", lstSection);
			map.put("defaultUserSectionValue", lstSection);

		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getSectionByApproveVersion(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();
		final Map<String, Object> obj = new HashMap<String, Object>();
		List<Section> lstSection = new ArrayList<Section>();
		List<Object> lstObj = new ArrayList<Object>();
		String sectionquery = "select nsectioncode,ssectionname from section where  nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and nsectioncode = any ( select ls.nsectioncode from sectionusers su,labsection ls where su.nusercode ="
				+ userInfo.getNusercode() + " " + " and ls.nlabsectioncode=su.nlabsectioncode  " + " and su.nsitecode="
				+ userInfo.getNtranssitecode() + " and ls.nsitecode=" + userInfo.getNtranssitecode()
				+ " and su.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ls.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by ls.nsectioncode)";
		lstSection = (jdbcTemplate.query(sectionquery, new Section()));

		if (!lstSection.isEmpty()) {

			if (lstSection.size() > 1) {
				obj.put("nsectioncode", 0);
				obj.put("ssectionname",
						commonFunction.getMultilingualMessage("IDS_ALL", userInfo.getSlanguagefilename()));
				lstObj.add(obj);
				lstObj.addAll(lstSection);
				map.put("UserSection", lstObj);
				map.put("defaultUserSectionValue", lstObj.get(0));

			} else {
				map.put("UserSection", lstSection);
				map.put("defaultUserSectionValue", lstSection.get(0));
				// inputMap.put("nsectioncode",lstSection.get(0).getNsectioncode());
			}
			String section = lstSection.stream().map(objsectioncode -> String.valueOf(objsectioncode.getNsectioncode()))
					.collect(Collectors.joining(","));
			inputMap.put("nsectioncode", section);
			map.putAll((Map<String, Object>) getTestComboBySection(inputMap, userInfo).getBody());
		} else {
			map.put("defaultUserSectionValue", lstSection);

		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getTestCombo(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		final Map<String, Object> obj = new HashMap<>();
		final List<Object> lstobj = new ArrayList<>();
		List<Section> lstSection = new ArrayList<>();
		String nsectioncode = "";
		String ntranscode = "";
		Integer ndesigntemplatemappingcode = 0;
		String TransactionStatus = "";
		String getTest = "";
		String sectioncodes = "";

		if (inputMap.containsKey("nsectioncode")) {
			nsectioncode = (String) inputMap.get("nsectioncode");
		}

		if (inputMap.containsKey("ntransactionstatus")) {
			ntranscode = (String) inputMap.get("ntransactionstatus");
		}

		if (inputMap.containsKey("ndesigntemplatemappingcode")) {
			ndesigntemplatemappingcode = (Integer) inputMap.get("ndesigntemplatemappingcode");
		}

		sectioncodes = "and tgt.nsectioncode in(" + nsectioncode + ")";
		TransactionStatus = " and rth.ntransactionstatus in(" + ntranscode + ") ";

		if ((int) inputMap.get("nflag") == 7) {
			sectioncodes = " and tgt.nsectioncode = any ( select ls.nsectioncode from sectionusers su,labsection ls where su.nusercode ="
					+ userInfo.getNusercode() + " " + " and ls.nlabsectioncode=su.nlabsectioncode  	and su.nsitecode="
					+ userInfo.getNtranssitecode() + " " + " and su.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ls.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by ls.nsectioncode)";
		}
		getTest = " select tgt.stestsynonym,tgt.ntestcode,tgt.nsectioncode,max(jb.ntestrescheduleno) as ntestrescheduleno from testgrouptest tgt, registrationtest rt,joballocation jb "
				+ " where rt.ntestgrouptestcode = tgt.ntestgrouptestcode   " + " and rt.nsitecode="
				+ userInfo.getNtranssitecode() + " and jb.nsitecode=" + userInfo.getNtranssitecode()
				+ " and rt.ntransactiontestcode = any (  "
				+ " select rth.ntransactiontestcode from registrationtesthistory rth,registrationarno rar,registration r  "
				+ " where " + " rth.nsitecode=" + userInfo.getNtranssitecode() + " and  rar.nsitecode="
				+ userInfo.getNtranssitecode() + " and  r.nsitecode=" + userInfo.getNtranssitecode()
				+ " and rth.ntesthistorycode = any (  "
				+ " select max(ntesthistorycode) from registrationtesthistory where nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNtranssitecode() + " group by ntransactiontestcode  ) "
				+ " and r.npreregno = rar.npreregno and r.nregtypecode =" + inputMap.get("nregtypecode")
				+ "  and r.nregsubtypecode = " + inputMap.get("nregsubtypecode") + " "
				+ " and r.ndesigntemplatemappingcode in (" + ndesigntemplatemappingcode + ") " + TransactionStatus
				+ " and rth.dtransactiondate between '" + inputMap.get("fromdate") + "' and '" + inputMap.get("todate")
				+ "' " + " and rar.npreregno = rth.npreregno and rar.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and rar.napprovalversioncode = "
				+ inputMap.get("napprovalversioncode") + "  ) " + " and jb.nusercode in (" + userInfo.getNusercode()
				+ ",-1) " + sectioncodes + "  " + " and jb.ntransactiontestcode = rt.ntransactiontestcode "
				// + "and jb.ntestrescheduleno = ( "
				// + "select max(ntestrescheduleno) ntestrescheduleno from joballocation "
				// + "where rt.ntransactiontestcode = ntransactiontestcode"
				// +" and nsitecode="+userInfo.getNtranssitecode()
				// + " ) "
				+ " group by tgt.stestsynonym,tgt.ntestcode,tgt.nsectioncode,jb.ntestrescheduleno ";

		List<TestGroupTest> lstTest = jdbcTemplate.query(getTest, new TestGroupTest());
		if (!lstTest.isEmpty()) {
			if (lstTest.size() > 1) {
				obj.put("ntestcode", 0);
				obj.put("stestsynonym",
						commonFunction.getMultilingualMessage("IDS_ALL", userInfo.getSlanguagefilename()));
				lstobj.add(obj);
				lstobj.addAll(lstTest);
				map.put("Test", lstobj);
				map.put("realTestList", lstobj);
				map.put("realTestValue", lstobj.get(0));
				map.put("defaultTestValue", lstobj.get(0));

			} else {
				map.put("Test", lstTest);
				map.put("realTestList", lstTest);
				map.put("realTestValue", lstTest.get(0));
				map.put("defaultTestValue", lstTest.get(0));
				map.put("ntestcode", lstTest.get(0).getNtestcode());
			}
			map.put("activeTestTab", "IDS_TESTATTACHMENTS");

			if ((int) inputMap.get("nflag") == 1) {
				map.putAll((Map<String, Object>) getMyTestWiseJobsDetails(inputMap, userInfo).getBody());
			} else if ((int) inputMap.get("nflag") == 7) {
				final String sectioncodevalue = lstTest.stream().map(x -> String.valueOf(x.getNsectioncode()))
						.collect(Collectors.joining(","));
				String getSection = "select * from section s join labsection ls on ls.nsectioncode=s.nsectioncode join sectionusers su on su.nlabsectioncode=ls.nlabsectioncode "
						+ " where su.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ls.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and s.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and s.nsectioncode in (" + sectioncodevalue + ") and su.nusercode= "
						+ userInfo.getNusercode() + "" + " and su.nsitecode=" + userInfo.getNtranssitecode()
						+ " and ls.nsitecode=" + userInfo.getNtranssitecode() + " ; ";

				lstSection = jdbcTemplate.query(getSection, new Section());

				if (lstSection.size() > 1) {
					Map<String, Object> obj1 = new HashMap<>();
					List<Object> lstObj1 = new ArrayList<>();
					obj1.put("nsectioncode", 0);
					obj1.put("ssectionname",
							commonFunction.getMultilingualMessage("IDS_ALL", userInfo.getSlanguagefilename()));
					lstObj1.add(obj1);
					map.put("UserSection", lstObj1);
					map.put("realUserSectionValue", lstObj1.get(0));
					map.put("defaultUserSectionValue", lstObj1.get(0));

				} else {
					map.put("UserSection", lstSection);
					map.put("realUserSectionValue", lstSection.get(0));
					map.put("defaultUserSectionValue", lstSection.get(0));

				}

			}
		} else {
			if ((int) inputMap.get("nflag") == 7) {
				map.put("UserSection", lstSection);
				map.put("realUserSectionValue", lstSection);
				map.put("defaultUserSectionValue", lstSection);

			}
			map.put("Test", lstTest);
			map.put("realTestList", lstTest);
			map.put("realTestValue", lstTest);
			map.put("defaultTestValue", lstTest);

			map.put("activeTestTab", "IDS_TESTATTACHMENTS");
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getTestComboBySection(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		final Map<String, Object> obj = new HashMap<>();
		final List<Object> lstobj = new ArrayList<>();
		List<Section> lstSection = new ArrayList<>();
		String nsectioncode = "";
		String ntranscode = "";
		String TransactionStatus = "";
		String getTest = "";
		String sectioncodes = "";

		if (inputMap.containsKey("nsectioncode")) {
			nsectioncode = (String) inputMap.get("nsectioncode");
		}

		if (inputMap.containsKey("ntransactionstatus")) {
			ntranscode = (String) inputMap.get("ntransactionstatus");
		}

		sectioncodes = "and tgt.nsectioncode in(" + nsectioncode + ")";
		TransactionStatus = "and rth.ntransactionstatus in(" + ntranscode + ")";

		if ((int) inputMap.get("nflag") == 7 || (int) inputMap.get("nflag") == 5) {
			sectioncodes = " and tgt.nsectioncode = any ( select ls.nsectioncode from sectionusers su,labsection ls where su.nusercode ="
					+ userInfo.getNusercode() + " " + " and ls.nlabsectioncode=su.nlabsectioncode and su.nsitecode="
					+ userInfo.getNtranssitecode() + " and ls.nsitecode=" + userInfo.getNtranssitecode()
					+ " and su.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ls.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " group by ls.nsectioncode)";
		}

		getTest = " select tgt.stestsynonym,tgt.ntestcode,tgt.nsectioncode,max(jb.ntestrescheduleno) as ntestrescheduleno from testgrouptest tgt, registrationtest rt,joballocation jb "
				+ " where rt.ntestgrouptestcode = tgt.ntestgrouptestcode   " + "  and rt.nsitecode="
				+ userInfo.getNtranssitecode() + "  and jb.nsitecode=" + userInfo.getNtranssitecode()
				+ " and rt.ntransactiontestcode = any (  "
				+ " select rth.ntransactiontestcode from registrationtesthistory rth,registrationarno rar,registration r  "
				+ " where " + "    r.nsitecode=" + userInfo.getNtranssitecode() + "  and rth.nsitecode="
				+ userInfo.getNtranssitecode() + "  and rar.nsitecode=" + userInfo.getNtranssitecode()
				+ " and rth.ntesthistorycode = any (  "
				+ " select max(ntesthistorycode) from registrationtesthistory where nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and nsitecode="
				+ userInfo.getNtranssitecode() + " group by ntransactiontestcode  ) "
				+ " and r.npreregno = rar.npreregno and r.nregtypecode =" + inputMap.get("nregtypecode")
				+ "  and r.nregsubtypecode = " + inputMap.get("nregsubtypecode") + " "
				+ " and r.ndesigntemplatemappingcode =" + inputMap.get("ndesigntemplatemappingcode") + " "
				+ TransactionStatus + " and rth.dtransactiondate between '" + inputMap.get("fromdate") + "' and '"
				+ inputMap.get("todate") + "' " + " and rar.npreregno = rth.npreregno and rar.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and rar.napprovalversioncode = "
				+ inputMap.get("napprovalconfigversioncode") + "  ) " + " and jb.nusercode in ("
				+ userInfo.getNusercode() + ",-1) " + sectioncodes + "  "
				+ " and jb.ntransactiontestcode = rt.ntransactiontestcode "
				// + "and jb.ntestrescheduleno = ( "
				// + "select max(ntestrescheduleno) ntestrescheduleno from joballocation "
				// + "where rt.ntransactiontestcode = ntransactiontestcode and
				// nsitecode="+userInfo.getNtranssitecode()+" ) "
				+ " group by tgt.stestsynonym,tgt.ntestcode,tgt.nsectioncode";
		// + " ,jb.ntestrescheduleno " ;

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

			} else {
				map.put("Test", lstTest);
				map.put("defaultTestValue", lstTest.get(0));
				map.put("ntestcode", lstTest.get(0).getNtestcode());
			}
			map.put("activeTestTab", "IDS_TESTATTACHMENTS");

			if ((int) inputMap.get("nflag") == 1) {
				map.putAll((Map<String, Object>) getMyTestWiseJobsDetails(inputMap, userInfo).getBody());
			} else if ((int) inputMap.get("nflag") == 7 || (int) inputMap.get("nflag") == 5) {
				final String sectioncodevalue = lstTest.stream().map(x -> String.valueOf(x.getNsectioncode()))
						.collect(Collectors.joining(","));
				String getSection = " select s.nsectioncode,s.ssectionname from section s join labsection ls on ls.nsectioncode=s.nsectioncode join sectionusers su on su.nlabsectioncode=ls.nlabsectioncode "
						+ " where su.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ls.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and s.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and s.nsectioncode in (" + sectioncodevalue + ") and su.nusercode= "
						+ userInfo.getNusercode() + " " + " and su.nsitecode=" + userInfo.getNtranssitecode()
						+ " and ls.nsitecode=" + userInfo.getNtranssitecode()
						+ " group by s.nsectioncode,s.ssectionname  ; ";

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
					map.put("defaultUserSectionValue", lstObj1.get(0));

				} else {
					map.put("UserSection", lstSection);
					map.put("defaultUserSectionValue", lstSection.get(0));

				}

			}
		} else {
			if ((int) inputMap.get("nflag") == 7) {
				map.put("UserSection", lstSection);
				map.put("defaultUserSectionValue", lstSection);

			}
			map.put("Test", lstTest);
			map.put("defaultTestValue", lstTest);

			map.put("activeTestTab", "IDS_TESTATTACHMENTS");
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getDesignTemplateByApprovalConfigVersion(Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception {

		Map<String, Object> map = new HashMap<String, Object>();

		final String str = "select dm.ndesigntemplatemappingcode,rt.jsondata,CONCAT(rt.sregtemplatename,'(',cast(dm.nversionno as character varying),')') sregtemplatename "
				+ " from designtemplatemapping dm, reactregistrationtemplate rt,approvalconfigversion acv  "
				+ " where  acv.ndesigntemplatemappingcode=dm.ndesigntemplatemappingcode "
				+ " and  rt.nreactregtemplatecode=dm.nreactregtemplatecode  and acv.nsitecode ="
				+ userInfo.getNmastersitecode() + " and dm.nsitecode =" + userInfo.getNmastersitecode()
				+ " and  dm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and  acv.napproveconfversioncode=" + inputMap.get("napprovalconfigversioncode") + " ";

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
	public ResponseEntity<Object> getActionDetails(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Map<String, Object> map = new HashMap<>();
		List<ControlMaster> controlmasterlist = new ArrayList<ControlMaster>();
		final String getActionQuery = "select *,coalesce(jsondata->'scontrolids'->>'" + userInfo.getSlanguagetypecode()
		+ "',jsondata->'scontrolids'->>'en-US') as scontrolids from controlmaster where nformcode ="
		+ userInfo.getNformcode() + " and scontrolname  in('Accept','Revert') and nstatus ="
		+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "   ";
		controlmasterlist = jdbcTemplate.query(getActionQuery, new ControlMaster());
		map.put("actionStatus", controlmasterlist);
		return new ResponseEntity<Object>(map, HttpStatus.OK);
	}

	@Override

	public ResponseEntity<Object> getMyTestWiseJobsDetails(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		System.out.println("Test start=>" + Instant.now());
		Map<String, Object> returnMap = new HashMap<String, Object>();
		List<Map<String, Object>> testList = new ArrayList<>();
		System.out.println("Test Wise query start=>" + Instant.now());
		String transCode = "";
		Integer ntestcode = 0;
		Integer ndesigntemplatemappingcode = 0;
		final short sampleTypeCode = Short.parseShort(String.valueOf(inputMap.get("nsampletypecode")));
		final short regTypeCode = Short.parseShort(String.valueOf(inputMap.get("nregtypecode")));
		final short regSubTypeCode = Short.parseShort(String.valueOf(inputMap.get("nregsubtypecode")));

		if (!inputMap.containsKey("ntransactionstatus")) {
			transCode = "0";
		} else if (inputMap.containsKey("ntransactionstatus")) {
			transCode = (String) inputMap.get("ntransactionstatus");
		}

		if (!inputMap.containsKey("ntestcode")) {
			inputMap.put("ntestcode", ntestcode);
		}

		if ((boolean) inputMap.get("nneedtemplatebasedflow")) {
			ndesigntemplatemappingcode = (Integer) inputMap.get("ndesigntemplatemappingcode");
		}

		final String getTestQuery = "SELECT * from fn_testwisemyjobtestget('" + inputMap.get("fromdate") + "', '"
				+ inputMap.get("todate") + "', " + "" + sampleTypeCode + " , " + regTypeCode + ", " + regSubTypeCode
				+ ", " + ndesigntemplatemappingcode + ", " + inputMap.get("napprovalversioncode") + ", "
				+ userInfo.getNusercode() + ", '" + transCode + "', " + "" + inputMap.get("ntestcode") + ", '"
				+ inputMap.get("nsectioncode") + "', " + userInfo.getNtranssitecode() + ",'"
				+ userInfo.getSlanguagetypecode() + "');";

		LOGGER.info(getTestQuery);
		final String testListString = jdbcTemplate.queryForObject(getTestQuery, String.class);
		// final String testListString = (String)
		// jdbcUtilityFunction.queryForObject(getTestQuery, String.class, jdbcTemplate);
		System.out.println("Test Wise query end=>" + Instant.now());
		if (testListString != null) {
			testList = projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(testListString, userInfo, true,
					(int) inputMap.get("ndesigntemplatemappingcode"), "sample");
			returnMap.put("MJ_TEST", testList);
			returnMap.put("MJSelectedTest", Arrays.asList(testList.get(testList.size() - 1)));

			final String ntransactiontestcode = String
					.valueOf(testList.get(testList.size() - 1).get("ntransactiontestcode"));
			inputMap.put("ntransactiontestcode", ntransactiontestcode);

			returnMap.putAll((Map<String, Object>) getActionDetails(inputMap, userInfo).getBody());
			returnMap.putAll(getActiveTestTabData((String) inputMap.get("ntransactiontestcode"),
					inputMap.containsKey("activeTestTab") ? (String) inputMap.get("activeTestTab") : "", userInfo));

			if (inputMap.containsKey("saveFilterSubmit") && (boolean) inputMap.get("saveFilterSubmit") == true) {
				projectDAOSupport.createFilterSubmit(inputMap, userInfo);
			}

			//List<FilterName> lstFilterName = (List<FilterName>) projectDAOSupport.getFavoriteFilterDetail(userInfo);
			List<FilterName> lstFilterName=getFilterName(userInfo);
			returnMap.put("FilterName",lstFilterName);

		} else {
			returnMap.put("MJ_TEST", testList);
			returnMap.put("MJSelectedTest", testList);
			returnMap.put("RegistrationTestAttachment", null);
			returnMap.put("RegistrationTestComment", null);
		}
		System.out.println("Test End=>" + Instant.now());
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	public List<RegistrationTestHistory> validateTestStatus(String ntransactiontestcode, int controlCode,
			UserInfo userInfo) throws Exception {

		String validateTests = " select rth.ntesthistorycode,rth.ntransactionstatus,rth.ntransactiontestcode,rth.npreregno from registrationtesthistory rth where rth.ntesthistorycode = any "
				+ "(select max(ntesthistorycode) from registrationtesthistory where ntransactiontestcode in ("
				+ ntransactiontestcode + ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + "  and  nsitecode="
				+ userInfo.getNtranssitecode() + "  group by ntransactiontestcode)" + "  and rth.nsitecode="
				+ userInfo.getNtranssitecode() + "  and rth.ntransactionstatus = any ("
				+ "  select ntransactionstatus from transactionvalidation tv,registration r where tv.nregtypecode=r.nregtypecode and tv.nregsubtypecode=r.nregsubtypecode and r.npreregno =rth.npreregno and "
				+ "  tv.nformcode = " + userInfo.getNformcode() + " and tv.nsitecode = " + userInfo.getNmastersitecode()
				+ "  and tv.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tv.ncontrolcode = " + controlCode + "  and r.nsitecode = " + userInfo.getNtranssitecode()
				+ "  ) and rth.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		return jdbcTemplate.query(validateTests, new RegistrationTestHistory());
	}

	@Override
	@SuppressWarnings({ "unchecked", "unused" })
	public ResponseEntity<Object> CreateAcceptTest(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		String sQuery = "lock table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		sQuery = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		sQuery = "lock table lockcancelreject " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		String ControlName = (String) inputMap.get("scontrolname");
		String stransactiontestcode = (String) inputMap.get("transactiontestcode");
		Integer ntranscode = 0;
		Integer ntransactionstatus = 0;
		Integer testhistorycount;
		Integer ntesthistoryseqcount;
		String validationstring = "";
		String InsertQuery = "";
		boolean joballocation = false;
		Map<String, Object> returnMap = new HashMap<String, Object>();
		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditOld = new JSONObject();
		JSONObject jsonAuditNew = new JSONObject();

		List<Map<String, Object>> lstDataTest = testAuditGet(inputMap, userInfo);
		if (ControlName.equals("Accept")) {
			ntranscode = Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus();
		} else if (ControlName.equals("Revert")) {
			String joballocationquery = "select cast(rsc.jsondata->>'nneedjoballocation' as boolean) as nneedjoballocation from approvalconfig ac "
					+ " join regsubtypeconfigversion rsc on rsc.napprovalconfigcode =ac.napprovalconfigcode "
					+ " where ac.nregtypecode=" + inputMap.get("nregtypecode") + " and ac.nregsubtypecode= "
					+ inputMap.get("nregsubtypecode") + "  " + " and ac.nsitecode =" + userInfo.getNmastersitecode()
					+ " and rsc.nsitecode =" + userInfo.getNmastersitecode() + " and rsc.ntransactionstatus="
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and ac.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rsc.nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ; ";

			final List<RegSubTypeConfigVersion> lstRegSubTypeConfigVersion = jdbcTemplate.query(joballocationquery,
					new RegSubTypeConfigVersion());
			// joballocation = lstRegSubTypeConfigVersion.get(0).Nneedjoballocation();
			joballocation = lstRegSubTypeConfigVersion.get(0).isNneedjoballocation();
			if (joballocation) {
				ntranscode = Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus();
			} else {
				ntranscode = Enumeration.TransactionStatus.REGISTERED.gettransactionstatus();
			}
		}
		List<RegistrationTestHistory> filteredTestList = validateTestStatus(stransactiontestcode,
				(int) inputMap.get("ncontrolcode"), userInfo);
		if (!filteredTestList.isEmpty()) {
			for (RegistrationTestHistory objregtesthistory : filteredTestList) {
				ntransactionstatus = (int) objregtesthistory.getNtransactionstatus();
				if (ntranscode == ntransactionstatus) {
					if (ntranscode == Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()) {
						validationstring = "Selected only Registered Status";
					} else if (ntranscode == Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()
							|| ntranscode == Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus()) {
						validationstring = "Revert Status Should be allowed only Accepted Status";
					}
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(validationstring, userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);

				}
			}
			String testquery = " select rt.ntransactiontestcode from registrationtesthistory rth "
					+ " join registrationtest rt on rt.ntransactiontestcode = rth.ntransactiontestcode "
					+ " and rt.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and rth.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and rt.nsitecode=" + userInfo.getNtranssitecode() + " and rth.nsitecode="
					+ userInfo.getNtranssitecode()
					+ " and rth.ntesthistorycode =any( select max(ntesthistorycode) from registrationtesthistory where nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  nsitecode="
					+ userInfo.getNtranssitecode() + " and ntransactiontestcode in(" + stransactiontestcode + ")  "
					+ " group by npreregno,ntransactionsamplecode,ntransactiontestcode)and rth.ntransactionstatus in("
					+ ntransactionstatus + ")";

			List<RegistrationTestHistory> lstRegistrationTestHistory = jdbcTemplate.query(testquery,
					new RegistrationTestHistory());
			Integer nregistrationtesthistorycount = lstRegistrationTestHistory.size();

			final List<String> updateSeqnolst = Arrays.asList("registrationtesthistory");
			// seqNoMaxUpdate(updateSeqnolst);

			String str = "select nsequenceno from seqnoregistration where stablename ='registrationtesthistory'"
					+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ; ";

			ntesthistoryseqcount = jdbcTemplate.queryForObject(str, Integer.class);

			testhistorycount = ntesthistoryseqcount + nregistrationtesthistorycount;

			String UpdateQuery = "update seqnoregistration set nsequenceno =" + testhistorycount
					+ " where stablename='registrationtesthistory';";
			jdbcTemplate.execute(UpdateQuery);

			InsertQuery = "insert into registrationtesthistory (ntesthistorycode,nformcode,npreregno,ntransactionsamplecode,ntransactiontestcode,nusercode,nuserrolecode,"
					+ " ndeputyusercode,ndeputyuserrolecode,nsampleapprovalhistorycode,ntransactionstatus,dtransactiondate,"
					+ " noffsetdtransactiondate,ntransdatetimezonecode,scomments,nsitecode,nstatus)" + " ((select ( "
					+ ntesthistoryseqcount + ")+Rank()over(order by ntesthistorycode) ntesthistorycode,"
					+ userInfo.getNformcode()
					+ " nformcode ,rt.npreregno,rt.ntransactionsamplecode,rt.ntransactiontestcode," + ""
					+ userInfo.getNusercode() + " nusercode," + userInfo.getNuserrole() + " nuserrolecode,"
					+ userInfo.getNdeputyusercode() + " ndeputyusercode," + userInfo.getNdeputyuserrole()
					+ "  ndeputyuserrolecode," + "-1 nsampleapprovalhistorycode," + ntranscode + " ntransactionstatus,'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' dtransactiondate, " + ""
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " noffsetdtransactiondate," + userInfo.getNtimezonecode() + " ntransdatetimezonecode," + "N'"
					+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "' scomments, "
					+ userInfo.getNtranssitecode() + " ntranssitecode,"
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
					+ " from registrationtesthistory rth  "
					+ " join registrationtest rt on rt.ntransactiontestcode = rth.ntransactiontestcode  "
					+ " and rt.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  "
					+ " and rth.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and  rt.nsitecode=" + userInfo.getNtranssitecode() + " and  rt.nsitecode=rth.nsitecode "
					+ " and rth.ntesthistorycode =any( select max(ntesthistorycode) from registrationtesthistory  "
					+ " where nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and ntransactiontestcode in(" + stransactiontestcode + ")   " + " and   nsitecode="
					+ userInfo.getNtranssitecode()
					+ " group by npreregno,ntransactionsamplecode,ntransactiontestcode)and  rth.ntransactionstatus in("
					+ ntransactionstatus + ")));";

			jdbcTemplate.execute(InsertQuery);

			// String JobAllcationUpdateQuery ="update joballocation set nusercode
			// ="+userInfo.getNusercode()+" where nusercode =-1 and ntransactiontestcode
			// in("+stransactiontestcode+") ";
			// jdbcTemplate.execute(JobAllcationUpdateQuery);

//			ALPD-6064	Added createIntegrationRecord and deleteSdmsLabSheetRecords methods by Vishakh (08-07-2025)
			boolean nneedtestinitiate = (boolean) inputMap.get("nneedtestinitiate");
			
			if(!nneedtestinitiate) {
				if(ntranscode == Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()) {
					inputMap.put("sintegrationpreregno",inputMap.get("npreregno"));
					inputMap.put("svalidationstatus",Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()); 	
					transactionDAOSupport.createIntegrationRecord(inputMap, userInfo);
				} else {
					transactionDAOSupport.deleteSdmsLabSheetRecords(stransactiontestcode, userInfo);
				}
			}
			
			
			returnMap = AcceptTestWiseDetails(inputMap, userInfo);
			jsonAuditOld.put("registrationtest", lstDataTest);
			jsonAuditNew.put("registrationtest", (List<Map<String, Object>>) returnMap.get("MJSelectedTest"));
			auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
			auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
			auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
			actionType.put("registrationtest", ControlName + ' '
					+ commonFunction.getMultilingualMessage("IDS_TEST", userInfo.getSlanguagefilename()));
			auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, jsonAuditNew, actionType, auditmap, false, userInfo);

			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		} else {
			if (ControlName.equals("Revert")) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SELECTACCEPTTEST", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTREGISTERALLOTTEST",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	private Map<String, Object> getActiveTestTabData(String ntransactiontestcode, String activeTabName,
			UserInfo userInfo) throws Exception {
		switch (activeTabName) {
		case "IDS_TESTATTACHMENTS":
			return attachmentDAO.getTestAttachment(ntransactiontestcode, userInfo).getBody();
		case "IDS_TESTCOMMENTS":
			return commentDAO.getTestComment(ntransactiontestcode, userInfo).getBody();
		default:
			return attachmentDAO.getTestAttachment(ntransactiontestcode, userInfo).getBody();
		}
	}

	@Override

	public Map<String, Object> AcceptTestWiseDetails(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		System.out.println("Accept Test start=>" + Instant.now());
		Map<String, Object> returnMap = new HashMap<String, Object>();
		List<Map<String, Object>> testList = new ArrayList<>();
		System.out.println("Test Wise query start=>" + Instant.now());
		String transCode = "";
		Integer ntestcode = 0;
		Integer ndesigntemplatemappingcode = (Integer) inputMap.get("ndesigntemplatemappingcode");
		;
		final short sampleTypeCode = Short.parseShort(String.valueOf(inputMap.get("nsampletypecode")));
		final short regTypeCode = Short.parseShort(String.valueOf(inputMap.get("nregtypecode")));
		final short regSubTypeCode = Short.parseShort(String.valueOf(inputMap.get("nregsubtypecode")));

		if (!inputMap.containsKey("ntestcode")) {
			inputMap.put("ntestcode", ntestcode);
		}

		if (inputMap.containsKey("ncheck")) {
			transCode = "0";
		}

		final String getTestQuery = "SELECT * from fn_testwisemyjobaccepttestget('" + inputMap.get("fromdate") + "', '"
				+ inputMap.get("todate") + "', " + "" + sampleTypeCode + " , " + regTypeCode + ", " + regSubTypeCode
				+ "," + ndesigntemplatemappingcode + "," + inputMap.get("napprovalversioncode") + ", "
				+ userInfo.getNusercode() + ", '" + transCode + "', " + "" + inputMap.get("ntestcode") + ", "
				+ inputMap.get("nsectioncode") + ", '" + inputMap.get("transactiontestcode") + "', "
				+ userInfo.getNtranssitecode() + ",'" + userInfo.getSlanguagetypecode() + "');";

		LOGGER.info(getTestQuery);
		String testListString = (String) jdbcUtilityFunction.queryForObject(getTestQuery, String.class, jdbcTemplate);

		System.out.println("Test Wise query end=>" + Instant.now());
		if (testListString != null) {
			testList = projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(testListString, userInfo, true,
					(int) inputMap.get("ndesigntemplatemappingcode"), "sample");
			returnMap.put("MJ_TEST", testList);
			returnMap.put("MJSelectedTest", testList);

			final String ntransactiontestcode = String
					.valueOf(testList.get(testList.size() - 1).get("ntransactiontestcode"));
			inputMap.put("ntransactiontestcode", ntransactiontestcode);

			returnMap.putAll((Map<String, Object>) getActionDetails(inputMap, userInfo).getBody());
			returnMap.putAll(getActiveTestTabData((String) inputMap.get("ntransactiontestcode"),
					inputMap.containsKey("activeTestTab") ? (String) inputMap.get("activeTestTab") : "", userInfo));

		} else {
			returnMap.put("MJ_TEST", testList);
			returnMap.put("MJSelectedTest", testList);
			returnMap.put("RegistrationTestAttachment", null);
			returnMap.put("RegistrationTestComment", null);
		}

		System.out.println("Accept Test End=>" + Instant.now());
		return returnMap;
		// return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	public List<Map<String, Object>> testAuditGet(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		List<Map<String, Object>> testList = new ArrayList<>();
		String transCode = "";
		Integer ntestcode = 0;
		Integer ndesigntemplatemappingcode = (Integer) inputMap.get("ndesigntemplatemappingcode");
		final short sampleTypeCode = Short.parseShort(String.valueOf(inputMap.get("nsampletypecode")));
		final short regTypeCode = Short.parseShort(String.valueOf(inputMap.get("nregtypecode")));
		final short regSubTypeCode = Short.parseShort(String.valueOf(inputMap.get("nregsubtypecode")));

		if (!inputMap.containsKey("ntestcode")) {
			inputMap.put("ntestcode", ntestcode);
		}

		if (inputMap.containsKey("ncheck")) {
			transCode = "0";
		}

		final String getTestQuery = "SELECT * from fn_testwisemyjobaccepttestget('" + inputMap.get("fromdate") + "', '"
				+ inputMap.get("todate") + "', " + "" + sampleTypeCode + " , " + regTypeCode + ", " + regSubTypeCode
				+ "," + ndesigntemplatemappingcode + "," + inputMap.get("napprovalversioncode") + ", "
				+ userInfo.getNusercode() + ", '" + transCode + "', " + "" + inputMap.get("ntestcode") + ", "
				+ inputMap.get("nsectioncode") + ", '" + inputMap.get("transactiontestcode") + "', "
				+ userInfo.getNtranssitecode() + ",'" + userInfo.getSlanguagetypecode() + "');";

		LOGGER.info(getTestQuery);
		String testListString = (String) jdbcUtilityFunction.queryForObject(getTestQuery, String.class, jdbcTemplate);
		if (testListString != null) {
			testList = projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(testListString, userInfo, true,
					(int) inputMap.get("ndesigntemplatemappingcode"), "sample");
		}
		return testList;
	}

	// ALPD-4870-To insert data when filter name input submit,done by Dhanushya RI
	@SuppressWarnings("unused")
	@Override
	public ResponseEntity<Object> createFilterName(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();

		Map<String, Object> objMap = new HashMap<>();
		JSONObject jsonObject = new JSONObject();
		final List<FilterName> lstFilterByName = projectDAOSupport
				.getFilterListByName(inputMap.get("sfiltername").toString(), userInfo);
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
					+ " " + " and (jsontempdata->'ndesigntemplatemappingcode')::int="
					+ inputMap.get("ndesigntemplatemappingcode") + " " + " and (jsontempdata->>'DbFromDate')='"
					+ inputMap.get("dfrom") + "' " + " and (jsontempdata->>'DbToDate')='" + inputMap.get("dto") + "' ;";

			final String strValidationFilter = (String) jdbcUtilityFunction.queryForObject(strValidationQuery,
					String.class, jdbcTemplate);

			final List<Map<String, Object>> lstValidationFilter = strValidationFilter != null
					? objMapper.readValue(strValidationFilter, new TypeReference<List<Map<String, Object>>>() {
					})
							: new ArrayList<Map<String, Object>>();
			if (lstValidationFilter.isEmpty()) {

				projectDAOSupport.createFilterData(inputMap, userInfo);
				//final List<FilterName> lstFilterName = (List<FilterName>) projectDAOSupport.getFavoriteFilterDetail(userInfo);
				List<FilterName> lstFilterName=getFilterName(userInfo);
				objMap.put("FilterName",lstFilterName);				
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

	// ALPD-4912-To show the previously saved filter name,done by Dhanushya RI
	// @Override
	// public List<FilterName> projectDAOSupport.getFavoriteFilterDetail( UserInfo userInfo) throws Exception {
	//
	// final String sFilterQry = "select * from filtername where
	// nformcode="+userInfo.getNformcode()+" and
	// nusercode="+userInfo.getNusercode()+""
	// + " and nuserrolecode="+userInfo.getNuserrole()+" and
	// nsitecode="+userInfo.getNtranssitecode()+" "
	// + " and nstatus =
	// "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" order by 1
	// desc ";
	//
	// return jdbcTemplate.query(sFilterQry, new FilterName());
	// }

	@Override
	public ResponseEntity<Object> getmyjobsFilterDetails(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		Map<String, Object> returnMap = new HashMap<>();

		final String strQuery1 = "select json_agg(jsondata || jsontempdata) as jsondata from filtername where nformcode="
				+ userInfo.getNformcode() + " and nusercode=" + userInfo.getNusercode() + " " + " and nuserrolecode="
				+ userInfo.getNuserrole() + " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nfilternamecode="
				+ inputMap.get("nfilternamecode");

		String strFilter = (String) jdbcUtilityFunction.queryForObject(strQuery1, String.class, jdbcTemplate);

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

				String strsampletypequery = " select st.nsampletypecode,st.jsondata->'sampletypename'->>'"
						+ userInfo.getSlanguagetypecode() + "' ssampletypename "
						+ " from sampletype st,approvalconfig ac,approvalconfigversion acv,registrationtype rt,registrationsubtype rst "
						+ " where  st.nsitecode =" + userInfo.getNmastersitecode() + " and acv.nsitecode ="
						+ userInfo.getNmastersitecode() + " and ac.nsitecode =" + userInfo.getNmastersitecode()
						+ " and rt.nsitecode =" + userInfo.getNmastersitecode() + " and rst.nsitecode ="
						+ userInfo.getNmastersitecode() + " and acv.ntransactionstatus ="
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " " + " and acv.nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and ac.napprovalconfigcode = acv.napprovalconfigcode  and ac.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and rt.nregtypecode = ac.nregtypecode  and rt.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and rt.nregtypecode = rst.nregtypecode and rst.nregsubtypecode = ac.nregsubtypecode "
						+ " and rst.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and rt.nsampletypecode = st.nsampletypecode  and acv.nsitecode ="
						+ userInfo.getNmastersitecode() + " " + " and st.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and st.nsampletypecode > 0  group by st.nsampletypecode,st.jsondata->'sampletypename'->>'"
						+ userInfo.getSlanguagetypecode() + "' order by st.nsorter";
				final List<SampleType> lstSampleType = jdbcTemplate.query(strsampletypequery, new SampleType());

				if (!lstSampleType.isEmpty()) {
					// napprovalversioncode napproveconfversioncode
					//List<FilterName> lstFilterName = (List<FilterName>) projectDAOSupport.getFavoriteFilterDetail(userInfo);
					List<FilterName> lstFilterName=getFilterName(userInfo);
					
					int nsampletypecode = lstSampleType.get(0).getNsampletypecode();
					returnMap.put("SampleTypeValue", lstSampleType.get(0));
					returnMap.put("RealSampleTypeValue", lstSampleType.get(0));
					returnMap.put("defaultSampleTypeValue", lstfilterDetail.get(0).get("sampleTypeValue"));
					returnMap.put("SampleType", lstSampleType);
					returnMap.put("realSampleTypeList", lstSampleType);
					inputMap.put("filterDetailValue", lstfilterDetail);
					returnMap.put("filterDetailValue", lstfilterDetail);
					returnMap.put("nflag", 1);
					returnMap.put("nsampletypecode", nsampletypecode);
					returnMap.put("spearteIt", lstFilterName);
					returnMap.putAll((Map<String, Object>) getRegistrationType(returnMap, userInfo).getBody());
					returnMap.putAll((Map<String, Object>) getMyTestWiseJobsDetails(returnMap, userInfo).getBody());

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

		private List<FilterName> getFilterName(UserInfo userInfo) throws Exception {
	
			final String sFilterQry = "select * from filtername where nformcode=" + userInfo.getNformcode()
			+ " and nusercode=" + userInfo.getNusercode() + "" + " and nuserrolecode=" + userInfo.getNuserrole()
			+ " and nsitecode=" + userInfo.getNtranssitecode() + " " + " and nstatus = "
			+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by 1 desc  ";
	
			return jdbcTemplate.query(sFilterQry, new FilterName());
		}

}
