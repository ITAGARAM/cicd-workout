package com.agaramtech.qualis.release.service;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.basemaster.model.CommentSubType;
import com.agaramtech.qualis.basemaster.model.SampleTestComments;
import com.agaramtech.qualis.basemaster.model.TransactionStatus;
import com.agaramtech.qualis.configuration.model.ApprovalConfigAutoapproval;
import com.agaramtech.qualis.configuration.model.FilterName;
import com.agaramtech.qualis.configuration.model.ReportSettings;
import com.agaramtech.qualis.configuration.model.SampleType;
import com.agaramtech.qualis.configuration.model.Settings;
import com.agaramtech.qualis.credential.model.UserFile;
import com.agaramtech.qualis.digitalsignature.model.DigitalSignature;
import com.agaramtech.qualis.dynamicpreregdesign.model.ReactRegistrationTemplate;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationSubType;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationType;
import com.agaramtech.qualis.externalorder.model.ExternalOrder;
import com.agaramtech.qualis.externalorder.model.ExternalOrderTest;
import com.agaramtech.qualis.externalorder.service.ExternalOrderDAO;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.EmailDAOSupport;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.ExternalOrderSupport;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.PasswordUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.ReportDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.TransactionDAOSupport;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.organization.model.Section;
import com.agaramtech.qualis.project.model.ProjectMaster;
import com.agaramtech.qualis.project.model.ProjectType;
import com.agaramtech.qualis.project.model.ReportInfoProject;
import com.agaramtech.qualis.registration.model.COAHistory;
import com.agaramtech.qualis.registration.model.Registration;
import com.agaramtech.qualis.registration.model.RegistrationArno;
import com.agaramtech.qualis.registration.model.RegistrationHistory;
import com.agaramtech.qualis.registration.model.RegistrationSampleArno;
import com.agaramtech.qualis.registration.model.RegistrationSampleHistory;
import com.agaramtech.qualis.registration.model.RegistrationTest;
import com.agaramtech.qualis.registration.model.RegistrationTestHistory;
import com.agaramtech.qualis.registration.model.ReleaseParameter;
import com.agaramtech.qualis.registration.model.SeqNoRegistration;
import com.agaramtech.qualis.registration.service.RegistrationDAOSupport;
import com.agaramtech.qualis.release.model.COAChild;
import com.agaramtech.qualis.release.model.COAParent;
import com.agaramtech.qualis.release.model.COAReportHistory;
import com.agaramtech.qualis.release.model.PreliminaryReportHistory;
import com.agaramtech.qualis.release.model.ReleaseComment;
import com.agaramtech.qualis.release.model.ReleaseHistory;
import com.agaramtech.qualis.release.model.ReleaseOutsourceAttachment;
import com.agaramtech.qualis.release.model.ReleaseTestAttachment;
import com.agaramtech.qualis.release.model.ReleaseTestComment;
import com.agaramtech.qualis.release.model.ReportInfoRelease;
import com.agaramtech.qualis.release.model.SeqNoReleaseManagement;
import com.agaramtech.qualis.release.model.SeqNoReleasenoGenerator;
import com.agaramtech.qualis.reports.model.COAReportType;
import com.agaramtech.qualis.reports.model.ReportDetails;
import com.agaramtech.qualis.scheduler.service.SchedularGenerateReportDAO;
import com.agaramtech.qualis.testgroup.model.TestGroupTestPredefinedParameter;
import com.agaramtech.qualis.testmanagement.model.Grade;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class ReleaseDAOImpl implements ReleaseDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final FTPUtilityFunction ftpUtilityFunction;
	private final JdbcTemplate jdbcTemplate;
	private final ReportDAOSupport reportDAOSupport;
	private final EmailDAOSupport emailDAOSupport;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final TransactionDAOSupport transactionDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final SchedularGenerateReportDAO schedularGenerateReportDAO;
	private final ReportTemplateDAOImpl reportTemplateDAO;
	private final ExternalOrderDAO externalOrderDAO;
	private final SchedularReleaseReportDAOImpl schedularReleaseReportDAO;
	private final PasswordUtilityFunction passwordUtilityFunction;
	private final ExternalOrderSupport externalOrderSupport;
	private final RegistrationDAOSupport registrationDAOSupport;

	@Override
	public ResponseEntity<Object> getRelease(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		Map<String, Object> returnMap = new HashMap<>();
		String fromDate = (String) inputMap.get("dfrom");
		String toDate = (String) inputMap.get("dto");
		ObjectMapper objMapper = new ObjectMapper();
		// ALPD-4878-To get previously save filter details when initial get,done by
		// Dhanushya RI
		final String strQuery = "select json_agg(jsondata || jsontempdata) as jsondata from filterdetail where nformcode="
				+ userInfo.getNformcode() + " and nusercode=" + userInfo.getNusercode() + " " + " and nuserrolecode="
				+ userInfo.getNuserrole() + " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final String strFilter = jdbcTemplate.queryForObject(strQuery, String.class);

		final List<Map<String, Object>> lstfilterDetail = strFilter != null
				? objMapper.readValue(strFilter, new TypeReference<List<Map<String, Object>>>() {
				})
				: new ArrayList<Map<String, Object>>();

		final String sampleTypeQuery = "select st.nsampletypecode,st.nportalrequired,st.nprojectspecrequired,coalesce(st.jsondata->'sampletypename'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',st.jsondata->'sampletypename'->>'en-US') ssampletypename,st.nsorter, st.nclinicaltyperequired, st.noutsourcerequired,st.nportalrequired "
				+ "from SampleType st,registrationtype rt,registrationsubtype rst,approvalconfig ac,approvalconfigversion acv "
				+ "where st.nsampletypecode > 0 and st.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and st.napprovalconfigview = "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " "
				+ " and st.nsampletypecode=rt.nsampletypecode and rt.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rt.nregtypecode=rst.nregtypecode and rst.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and rst.nregsubtypecode=ac.nregsubtypecode and ac.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and acv.napprovalconfigcode=ac.napprovalconfigcode and acv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acv.ntransactionstatus= "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " " + " and rt.nsitecode = "
				+ userInfo.getNmastersitecode() + " and rst.nsitecode = " + userInfo.getNmastersitecode()
				+ " and ac.nsitecode = " + userInfo.getNmastersitecode() + " and acv.nsitecode = "
				+ userInfo.getNmastersitecode() + " group by st.nsampletypecode,st.nsorter order by st.nsorter; ";

		List<SampleType> sampleTypeList = jdbcTemplate.query(sampleTypeQuery, new SampleType());
		if (!lstfilterDetail.isEmpty() && lstfilterDetail.get(0).containsKey("FromDate")
				&& lstfilterDetail.get(0).containsKey("ToDate")) {
			Instant instantFromDate = dateUtilityFunction
					.convertStringDateToUTC(lstfilterDetail.get(0).get("FromDate").toString(), userInfo, true);
			Instant instantToDate = dateUtilityFunction
					.convertStringDateToUTC(lstfilterDetail.get(0).get("ToDate").toString(), userInfo, true);

			fromDate = dateUtilityFunction.instantDateToString(instantFromDate);
			toDate = dateUtilityFunction.instantDateToString(instantToDate);

			returnMap.put("fromDate", fromDate);
			returnMap.put("toDate", toDate);
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
			inputMap.put("dfrom", fromDate);
			inputMap.put("dto", toDate);

			returnMap.put("realFromDate", formattedFromString);
			returnMap.put("realToDate", formattedToString);
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
		if (sampleTypeList.size() > 0) {
			List<FilterName> lstFilterName = getFilterName(userInfo);
			final SampleType filterSampleType = !lstfilterDetail.isEmpty()
					? objMapper.convertValue(lstfilterDetail.get(0).get("sampleTypeValue"), SampleType.class)
					: sampleTypeList.get(0);
			returnMap.put("SampleType", sampleTypeList);
			returnMap.put("realSampleTypeList", sampleTypeList);
			inputMap.put("nsampletypecode", filterSampleType.getNsampletypecode());
			returnMap.put("realSampleTypeValue", filterSampleType);
			returnMap.put("realProjectRequiredValue", filterSampleType.getNprojectspecrequired());
			returnMap.put("realPortalRequiredValue", filterSampleType.getNportalrequired());
			returnMap.put("SampleTypeValue", filterSampleType);
			inputMap.put("filterDetailValue", lstfilterDetail);

			returnMap.putAll((Map<String, Object>) getCOAReportType(inputMap, userInfo).getBody());
			returnMap.put("realReportTypeValue", returnMap.get("ReportTypeValue"));
			inputMap.put("ncoareporttypecode", returnMap.get("ncoareporttypecode"));
			inputMap.put("isneedsection", returnMap.get("isneedsection"));
			returnMap.putAll((Map<String, Object>) getRegistrationType(inputMap, userInfo).getBody());
			returnMap.put("realRegTypeValue", returnMap.get("RegTypeValue"));
			returnMap.put("realRegSubTypeValue", returnMap.get("RegSubTypeValue"));
			returnMap.put("realFilterStatusValue", returnMap.get("FilterStatusValue"));
			returnMap.put("realApprovalVersionValue", returnMap.get("ApprovalVersionValue"));
			returnMap.put("realDesignTemplateMappingValue", returnMap.get("DesignTemplateMappingValue"));
			returnMap.put("FilterName", lstFilterName);

		}
		return new ResponseEntity<>(returnMap, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Map<String, Object>> getRegistrationType(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		Map<String, Object> returnMap = new HashMap<>();
		final ObjectMapper objMapper = new ObjectMapper();

		String Str = "Select * from sampletype  where  nsampletypecode=" + inputMap.get("nsampletypecode");

		SampleType obj = (SampleType) jdbcUtilityFunction.queryForObject(Str, SampleType.class, jdbcTemplate);
		String ValidationQuery = "";
		if (obj.getNtransfiltertypecode() != -1 && userInfo.getNusercode() != -1) {
			int nmappingfieldcode = (obj.getNtransfiltertypecode() == 1) ? userInfo.getNdeptcode()
					: userInfo.getNuserrole();
			ValidationQuery = " and rst.nregsubtypecode in( " + "		SELECT rs.nregsubtypecode "
					+ "		FROM registrationsubtype rs "
					+ "		INNER JOIN transactionfiltertypeconfig ttc ON rs.nregsubtypecode = ttc.nregsubtypecode and ttc.nsitecode = "
					+ userInfo.getNmastersitecode()
					+ "		LEFT JOIN transactionusers tu ON tu.ntransfiltertypeconfigcode = ttc.ntransfiltertypeconfigcode and tu.nsitecode = "
					+ userInfo.getNmastersitecode() + "		WHERE ( ttc.nneedalluser = "
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + "  and ttc.nstatus =  "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND ttc.nmappingfieldcode = "
					+ nmappingfieldcode + ") " + "	 OR " + "	( ttc.nneedalluser = "
					+ Enumeration.TransactionStatus.NO.gettransactionstatus() + "  "
					+ "	  AND ttc.nmappingfieldcode =" + nmappingfieldcode + "	  AND tu.nusercode ="
					+ userInfo.getNusercode() + "   and ttc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "   and tu.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") " + "  OR "
					+ "	( ttc.nneedalluser = " + Enumeration.TransactionStatus.NO.gettransactionstatus() + "  "
					+ " AND ttc.nmappingfieldcode = -1 " + " AND tu.nusercode =" + userInfo.getNusercode()
					+ " and ttc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tu.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") "
					+ "	 AND rs.nregtypecode = rt.nregtypecode ANd rs.nsitecode = " + userInfo.getNmastersitecode()
					+ " ) ";
		}

		final String regTypeQuery = "select  st.nsampletypecode,rt.nregtypecode,coalesce(rt.jsondata->'sregtypename'->>'"
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
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and acv.napprovalconfigcode=ac.napprovalconfigcode and acv.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acv.ntransactionstatus = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and rt.nregtypecode>0 "
				+ " and rt.nsitecode = " + userInfo.getNmastersitecode() + " and rst.nsitecode = "
				+ userInfo.getNmastersitecode() + " and ac.nsitecode = " + userInfo.getNmastersitecode()
				+ " and acv.nsitecode = " + userInfo.getNmastersitecode() + ValidationQuery
				+ " group by st.nsampletypecode,rt.nregtypecode,coalesce(rt.jsondata->'sregtypename'->>'"
				+ userInfo.getSlanguagetypecode() + "',rt.jsondata->'sregtypename'->>'en-US'),rt.nsorter "
				+ " order by rt.nregtypecode desc";

		List<RegistrationType> regTypeList = jdbcTemplate.query(regTypeQuery, new RegistrationType());
		short regTypeCode = -1;
		Map<String, Object> valueMap = new HashMap<String, Object>();
		if (regTypeList.size() > 0) {
			// ALPD-4878-To get previously save filter details when initial get,done by
			// Dhanushya RI
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
			valueMap = objMapper.convertValue(filterRegType, new TypeReference<Map<String, Object>>() {
			});
			inputMap.put("nregtypecode", regTypeCode);
			returnMap.putAll((Map<String, Object>) getCOAReportType(inputMap, userInfo).getBody());
			inputMap.put("ncoareporttypecode", returnMap.get("ncoareporttypecode"));
			returnMap.putAll((Map<String, Object>) getRegistrationSubType(inputMap, userInfo).getBody());
		}
		returnMap.put("RegType", regTypeList);
		returnMap.put("realRegTypeList", regTypeList);
		returnMap.put("RegTypeValue", valueMap);

		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getRegistrationSubType(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		Map<String, Object> returnMap = new HashMap<>();
		String Str = "Select * from registrationtype rt,sampletype st where rt.nsampletypecode=st.nsampletypecode and rt.nregTypeCode="
				+ inputMap.get("nregtypecode") + " and rt.nsitecode = " + userInfo.getNmastersitecode();

		SampleType obj = (SampleType) jdbcUtilityFunction.queryForObject(Str, SampleType.class, jdbcTemplate);
		String validationQuery = "";
		if (obj.getNtransfiltertypecode() != -1 && userInfo.getNusercode() != -1) {
			int nmappingfieldcode = (obj.getNtransfiltertypecode() == 1) ? userInfo.getNdeptcode()
					: userInfo.getNuserrole();
			validationQuery = " and rst.nregsubtypecode in( " + " SELECT rs.nregsubtypecode "
					+ " FROM registrationsubtype rs "
					+ " INNER JOIN transactionfiltertypeconfig ttc ON rs.nregsubtypecode = ttc.nregsubtypecode and ttc.nsitecode = "
					+ userInfo.getNmastersitecode()
					+ " LEFT JOIN transactionusers tu ON tu.ntransfiltertypeconfigcode = ttc.ntransfiltertypeconfigcode and tu.nsitecode = "
					+ userInfo.getNmastersitecode() + " WHERE ( ttc.nneedalluser = "
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + "  and ttc.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND ttc.nmappingfieldcode = "
					+ nmappingfieldcode + ")" + "  OR " + "	( ttc.nneedalluser = "
					+ Enumeration.TransactionStatus.NO.gettransactionstatus() + "  " + " AND ttc.nmappingfieldcode ="
					+ nmappingfieldcode + " AND tu.nusercode =" + userInfo.getNusercode() + " and ttc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tu.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") " + "  OR "
					+ "	( ttc.nneedalluser = " + Enumeration.TransactionStatus.NO.gettransactionstatus() + "  "
					+ " AND ttc.nmappingfieldcode = -1 " + " AND tu.nusercode =" + userInfo.getNusercode()
					+ " and ttc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tu.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") "
					+ " AND rs.nregtypecode = " + inputMap.get("nregtypecode") + " and rs.nsitecode = "
					+ userInfo.getNmastersitecode() + ")";
		}
		final String regTypeQuery = "select rst.nregsubtypecode,rst.nregtypecode,coalesce(rst.jsondata->'sregsubtypename'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',rst.jsondata->'sregsubtypename'->>'en-US') as sregsubtypename,cast(rsc.jsondata->>'nneedsubsample' as boolean) nneedsubsample"
				+ " from approvalconfig ac,approvalconfigversion acv,registrationsubtype rst,regsubtypeconfigversion rsc"
				+ " where acv.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and rsc.napprovalconfigcode = ac.napprovalconfigcode and rsc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rsc.ntransactionstatus = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and ac.napprovalconfigcode = acv.napprovalconfigcode and rst.nregtypecode = "
				+ inputMap.get("nregtypecode")
				+ " and ac.napprovalconfigcode = acv.napprovalconfigcode and rst.nregsubtypecode = ac.nregsubtypecode"
				+ " and rst.nregsubtypecode > 0 " + " and acv.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rst.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ac.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and rst.nsitecode = "
				+ userInfo.getNmastersitecode() + " and rsc.nsitecode = " + userInfo.getNmastersitecode()
				+ " and ac.nsitecode = " + userInfo.getNmastersitecode() + " and acv.nsitecode = "
				+ userInfo.getNmastersitecode() + validationQuery + " order by rst.nregsubtypecode desc";

		List<RegistrationSubType> regSubTypeList = jdbcTemplate.query(regTypeQuery, new RegistrationSubType());

		short regSubTypeCode = -1;
		Map<String, Object> valueMap = new HashMap<String, Object>();
		if (regSubTypeList.size() > 0) {
			// ALPD-4878-To get previously save filter details when initial get,done by
			// Dhanushya RI
			final RegistrationSubType filterRegSubType = inputMap.containsKey("filterDetailValue")
					&& !((List<Map<String, Object>>) inputMap.get("filterDetailValue")).isEmpty()
					&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0)
							.containsKey("regSubTypeValue") ? objMapper
									.convertValue(((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0)
											.get("regSubTypeValue"), RegistrationSubType.class)
									: regSubTypeList.get(0);

			regSubTypeCode = filterRegSubType.getNregsubtypecode();
			inputMap.put("nneedsubsample", regSubTypeList.get(0).isNneedsubsample());
			inputMap.put("nneedtemplatebasedflow", regSubTypeList.get(0).isNneedtemplatebasedflow());
			valueMap = objMapper.convertValue(filterRegSubType, new TypeReference<Map<String, Object>>() {
			});
		}
		returnMap.put("RegSubType", regSubTypeList);
		returnMap.put("realRegSubTypeList", regSubTypeList);
		inputMap.put("nregsubtypecode", regSubTypeCode);
		returnMap.put("RegSubTypeValue", valueMap);
		returnMap.putAll((Map<String, Object>) projectDAOSupport
				.getReactRegistrationTemplateList(Short.valueOf(inputMap.get("nregtypecode").toString()),
						Short.valueOf(inputMap.get("nregsubtypecode").toString()),
						(boolean) inputMap.get("nneedtemplatebasedflow"), userInfo)
				.getBody());
		inputMap.put("ndesigntemplatemappingcode", returnMap.get("ndesigntemplatemappingcode"));
		returnMap.putAll((Map<String, Object>) getApprovalVersion(inputMap, userInfo).getBody());

		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getFilterStatus(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		if (!inputMap.containsKey("nflag")) {
			inputMap.put("nflag", 1);
		}
		Map<String, Object> returnMap = new HashMap<>();
		ObjectMapper objMapper = new ObjectMapper();

		final String regTypeQuery = " select acr.napprovalstatuscode as ntransactionstatus,'IDS_PENDINGTORELEASE' as sfilterstatus from approvalconfigrole acr,approvalconfig ac,transactionstatus ts "
				+ " where acr.napprovalconfigcode=ac.napprovalconfigcode and acr.nlevelno="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and acr.ntransactionstatus = ts.ntranscode" + " and ac.nregtypecode=" + inputMap.get("nregtypecode")
				+ " and ac.nregsubtypecode=" + inputMap.get("nregsubtypecode") + " and acr.napproveconfversioncode="
				+ inputMap.get("napprovalversioncode") + " and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ac.nsitecode = "
				+ userInfo.getNmastersitecode() + " and acr.nsitecode = " + userInfo.getNmastersitecode();

		List<TransactionStatus> filterStatus = jdbcTemplate.query(regTypeQuery, new TransactionStatus());

		List<?> lst = commonFunction.getMultilingualMessageList(filterStatus, Arrays.asList("sfilterstatus"),
				userInfo.getSlanguagefilename());
		filterStatus = objMapper.convertValue(lst, new TypeReference<List<TransactionStatus>>() {
		});

		String transactionStatus = "-1";
		TransactionStatus filterValue = new TransactionStatus();

		if (filterStatus.size() > 0) {
			// ALPD-4878-To get previously save filter details when initial get,done by
			// Dhanushya RI
			final TransactionStatus filterTransactionStatus = inputMap.containsKey("filterDetailValue")
					&& !((List<Map<String, Object>>) inputMap.get("filterDetailValue")).isEmpty()
					&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0)
							.containsKey("filterStatusValue") ? objMapper
									.convertValue(((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0)
											.get("filterStatusValue"), TransactionStatus.class)
									: filterStatus.get(0);

			filterValue = filterTransactionStatus;
			String stransactionstatus = filterStatus.stream().map(x -> String.valueOf(x.getNtransactionstatus()))
					.collect(Collectors.joining(","));
			transactionStatus = String.valueOf(filterValue.getNtransactionstatus());
			inputMap.put("stransactionstatus", stransactionstatus);
			inputMap.put("ntransactionstatus", transactionStatus);

			if ((int) inputMap.get("nflag") == Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()) {

				returnMap.putAll((Map<String, Object>) getReleaseHistory(inputMap, userInfo).getBody());
				inputMap.put("ncoaparentcode", returnMap.get("ncoaparentcode"));
				returnMap.putAll((Map<String, Object>) getReleaseSample(inputMap, userInfo).getBody());
			}
		}

		returnMap.put("FilterStatusValue", filterValue);

		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getApprovalVersion(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		Map<String, Object> returnMap = new HashMap<>();
		ObjectMapper objMapper = new ObjectMapper();

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
				+ " and r.nregtypecode =" + inputMap.get("nregtypecode") + "  and r.nregsubtypecode = "
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
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and r.nsitecode = "
				+ userInfo.getNtranssitecode() + " and rar.nsitecode = " + userInfo.getNtranssitecode()
				+ " and rh.nsitecode = " + userInfo.getNtranssitecode() + " and rh.nsitecode = "
				+ userInfo.getNtranssitecode() + " and ac.nsitecode = " + userInfo.getNmastersitecode()
				+ " and aca.nsitecode = " + userInfo.getNmastersitecode()
				+ " group by aca.sversionname,aca.napprovalconfigcode,aca.napprovalconfigversioncode,acv.ntransactionstatus,acv.ntreeversiontempcode";

		List<ApprovalConfigAutoapproval> approvalVersion = jdbcTemplate.query(regTypeQuery,
				new ApprovalConfigAutoapproval());
		String versionCode = "-1";
		int configCode = -1;

		Map<String, Object> valueVersionMap = new HashMap<String, Object>();
		if (approvalVersion.size() > 0) {
			configCode = approvalVersion.get(0).getNapprovalconfigcode();
			ApprovalConfigAutoapproval approvedApprovalVersion = new ApprovalConfigAutoapproval();
			List<ApprovalConfigAutoapproval> approvedApprovalVersionList = approvalVersion.stream().filter(
					x -> x.getNtransactionstatus() == Enumeration.TransactionStatus.APPROVED.gettransactionstatus())
					.collect(Collectors.toList());
			if (approvedApprovalVersionList != null && approvedApprovalVersionList.size() > 0) {
				// ALPD-4878-To get previously save filter details when initial get,done by
				// Dhanushya RI
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
				approvedApprovalVersion = filterApprovalConfig;

			} else {
				// ALPD-4878-To get previously save filter details when initial get,done by
				// Dhanushya RI
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

				approvedApprovalVersion = filterApprovalConfig;
			}

			versionCode = String.valueOf(approvedApprovalVersion.getNapprovalconfigversioncode());
			valueVersionMap = objMapper.convertValue(approvedApprovalVersion, new TypeReference<Map<String, Object>>() {
			});
		}
		returnMap.put("ApprovalVersion", approvalVersion);
		returnMap.put("realApprovalVersionList", approvalVersion);
		returnMap.put("ApprovalVersionValue", valueVersionMap);
		inputMap.put("napprovalconfigcode", configCode);
		inputMap.put("napprovalversioncode", versionCode);
		returnMap.putAll((Map<String, Object>) getFilterStatus(inputMap, userInfo).getBody());
		returnMap.putAll((Map<String, Object>) getReleaseConfigVersionRegTemplateDesign(inputMap, userInfo).getBody());

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

	}

	public ResponseEntity<Map<String, Object>> getReleaseConfigVersionRegTemplateDesign(Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		final Map<String, Object> returnMap = new HashMap<>();
		ObjectMapper objMapper = new ObjectMapper();

		returnMap.putAll((Map<String, Object>) projectDAOSupport
				.getApproveConfigVersionBasedTemplate(Short.valueOf(inputMap.get("nregtypecode").toString()),
						Short.valueOf(inputMap.get("nregsubtypecode").toString()),
						Short.valueOf(inputMap.get("napprovalversioncode").toString()))
				.getBody());
		// ALPD-4878-To get previously save filter details when initial get,done by
		// Dhanushya RI
		final ReactRegistrationTemplate filterDesignMapping = inputMap.containsKey("filterDetailValue")
				&& !((List<Map<String, Object>>) inputMap.get("filterDetailValue")).isEmpty()
				&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0)
						.containsKey("designTemplateMappingValue")
								? objMapper.convertValue(((List<Map<String, Object>>) inputMap.get("filterDetailValue"))
										.get(0).get("designTemplateMappingValue"), ReactRegistrationTemplate.class)
								: (ReactRegistrationTemplate) returnMap.get("DesignTemplateMappingValue");

		returnMap.put("realDesignTemplateMapping", filterDesignMapping);
		returnMap.put("DesignTemplateMappingValue", filterDesignMapping);
		returnMap.put("ndesigntemplatemappingcode", filterDesignMapping.getNdesigntemplatemappingcode());

		inputMap.put("ndesigntemplatemappingcode", returnMap.get("ndesigntemplatemappingcode"));

		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getReleaseSample(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		Map<String, Object> returnMap = new HashMap<>();

		int nflag = 1;

		int napprovalconfigcode = -1;
		String fromDate = (String) inputMap.get("dfrom");
		String toDate = (String) inputMap.get("dto");
		fromDate = dateUtilityFunction
				.instantDateToString(dateUtilityFunction.convertStringDateToUTC(fromDate, userInfo, true));
		toDate = dateUtilityFunction
				.instantDateToString(dateUtilityFunction.convertStringDateToUTC(toDate, userInfo, true));
		inputMap.put("dfrom", fromDate);
		inputMap.put("dto", toDate);

		final DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());

		String ntransactionstatus = "";

		if (!inputMap.containsKey("napprovalconfigcode")) {
			final String getApprovalConfig = "select napprovalconfigcode from approvalconfig where nregtypecode="
					+ inputMap.get("nregtypecode") + "" + " and nregsubtypecode=" + inputMap.get("nregsubtypecode") + ""
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode = " + userInfo.getNmastersitecode();
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
					+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + ") and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
					+ userInfo.getNmastersitecode();
			List<String> versionList = jdbcTemplate.queryForList(getApprovalVersion, String.class);
			inputMap.put("napprovalversioncode", versionList.stream().collect(Collectors.joining(",")));
		}

		final String statusQuery = " select acr.napprovalstatuscode as ntransactionstatus from approvalconfigrole acr,approvalconfig ac,transactionstatus ts "
				+ " where acr.napprovalconfigcode=ac.napprovalconfigcode and acr.nlevelno="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and acr.ntransactionstatus = ts.ntranscode and ac.nregtypecode=" + inputMap.get("nregtypecode")
				+ " and ac.nregsubtypecode=" + inputMap.get("nregsubtypecode") + " and acr.napproveconfversioncode="
				+ inputMap.get("napprovalversioncode") + " and acr.nsitecode = " + userInfo.getNmastersitecode()
				+ " and ac.nsitecode = " + userInfo.getNmastersitecode() + "";

		List<TransactionStatus> filterStatus = jdbcTemplate.query(statusQuery, new TransactionStatus());
		final String finalLevelStatus = !filterStatus.isEmpty()
				? String.valueOf(filterStatus.get(0).getNtransactionstatus())
				: "-1";
		inputMap.put("finalLevelStatus", finalLevelStatus);

		if (inputMap.containsKey("isAddPopup") && (boolean) inputMap.get("isAddPopup") == true) {
			ntransactionstatus = (String) inputMap.get("ntransactionstatus") + ","
					+ String.valueOf(Enumeration.TransactionStatus.PARTIALLY.gettransactionstatus());

			nflag = 2;
		} else {
			ntransactionstatus = (String) inputMap.get("ntransactionstatus") + ","
					+ String.valueOf(Enumeration.TransactionStatus.PARTIALLY.gettransactionstatus()) + ","
					+ String.valueOf(Enumeration.TransactionStatus.RELEASED.gettransactionstatus());

		}
		String fromDate1 = LocalDateTime.parse(fromDate, formatter1).format(formatter);
		String toDate1 = LocalDateTime.parse(toDate, formatter1).format(formatter);

		returnMap.put("realFromDate", fromDate1);
		returnMap.put("realToDate", toDate1);

		if (inputMap.containsKey("nneedtemplatebasedflow") && (boolean) inputMap.get("nneedtemplatebasedflow")) {
		}

		if (inputMap.containsKey("nneededit")) {
			if ((int) inputMap.get("isneedsection") == Enumeration.TransactionStatus.YES.gettransactionstatus()
					|| (int) inputMap.get("ncoareporttypecode") == Enumeration.COAReportType.SECTIONWISEMULTIPLESAMPLE
							.getcoaReportType()) {
				inputMap.putAll((Map<String, Object>) getSection(inputMap, userInfo).getBody());
			} else {
				inputMap.put("nsectioncode", "0");
			}
			if ((int) inputMap.get("ncoareporttypecode") == Enumeration.COAReportType.PROJECTWISE.getcoaReportType()) {
				inputMap.putAll((Map<String, Object>) getApprovedProjectByProjectType(inputMap, userInfo).getBody());
			} else {
				inputMap.put("nprojectmastercode", "0");
			}
			nflag = 2;
			inputMap.put("ncoaparentcode",
					inputMap.containsKey("ismandatory") && (boolean) inputMap.get("ismandatory") ? 0 : -1);
			inputMap.put("nneedmodal", true);
		} else if (inputMap.containsKey("isAddPopup") && (boolean) inputMap.get("isAddPopup") == true) {
			inputMap.put("ncoaparentcode",
					inputMap.containsKey("ismandatory") && (boolean) inputMap.get("ismandatory") ? 0 : -1);
			inputMap.put("nneedmodal", true);
			nflag = 2;

		} else {
			if (!inputMap.containsKey("isSearch")) {
				returnMap.putAll((Map<String, Object>) getReleaseHistory(inputMap, userInfo).getBody());
				inputMap.put("ncoaparentcode", returnMap.get("ncoaparentcode"));
				inputMap.put("nprojectmastercode", "0");
				inputMap.put("nsectioncode", -1);
				inputMap.put("npreregno", "0");
			} else {
				inputMap.put("ncoaparentcode", inputMap.get("ncoaparentcode"));
			}

		}
		if ((int) inputMap.get("ncoareporttypecode") == Enumeration.COAReportType.PATIENTWISE.getcoaReportType()
				&& inputMap.containsKey("patientwise") && (boolean) inputMap.get("patientwise")) {
			String str = " select npreregno from registration where jsondata->>'spatientid'::character varying='"
					+ inputMap.get("spatientid") + "' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			List<Registration> preregno = jdbcTemplate.query(str, new Registration());
			String preregnonew = "-1";
			if (preregno.size() > 0) {
				preregnonew = preregno.stream().map(objpreregno -> String.valueOf(objpreregno.getNpreregno()))
						.collect(Collectors.joining(","));
			}
			inputMap.put("npreregno", preregnonew);
		}

		String sampleTypeQuery = "SELECT * from fn_releasesampleget('" + fromDate + "', '" + toDate + "', " + ""
				+ inputMap.get("nregtypecode") + " , " + inputMap.get("nregsubtypecode") + ", '" + ntransactionstatus
				+ "','" + inputMap.get("napprovalversioncode") + "'," + " " + userInfo.getNusercode() + ", "
				+ userInfo.getNtranssitecode() + ", '" + inputMap.get("npreregno") + "', " + " "
				+ inputMap.get("nneedsubsample") + "," + inputMap.get("nprojectmastercode") + ",'"
				+ inputMap.get("ncoaparentcode") + "'," + " " + inputMap.get("ncoareporttypecode") + ",'"
				+ userInfo.getSlanguagetypecode() + "'," + " " + inputMap.get("nneedmodal") + ", '" + finalLevelStatus
				+ "'," + inputMap.get("nsectioncode") + ",'" + inputMap.get("filterquery") + "'," + nflag + ");";
		LOGGER.info(sampleTypeQuery);
		List<Map<String, Object>> sampleList = new ArrayList<Map<String, Object>>();
		System.out.println("Sample Start========?" + LocalDateTime.now());
		String sampleObj = jdbcTemplate.queryForObject(sampleTypeQuery, String.class);
		System.out.println("Sample Query========?" + sampleTypeQuery);
		System.out.println("Sample end========?" + LocalDateTime.now());

		if (sampleObj != null) {
			sampleList = projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(sampleObj, userInfo, true,
					(int) inputMap.get("ndesigntemplatemappingcode"), "sample");

			final String spreregno = sampleList.stream().map(x -> String.valueOf(x.get("npreregno")))
					.collect(Collectors.joining(","));
			inputMap.put("npreregno", spreregno);

		} else {
			inputMap.put("npreregno", 0);
		}

		returnMap.putAll((Map<String, Object>) getReleaseSubSample(inputMap, userInfo).getBody());

		returnMap.put("DynamicDesign", projectDAOSupport
				.getTemplateDesign((int) inputMap.get("ndesigntemplatemappingcode"), userInfo.getNformcode()));

		if (inputMap.containsKey("isPopup") && (boolean) inputMap.get("isPopup") == true) {
			returnMap.put("ReleaseSample", sampleList);
		} else {
			returnMap.put("ReleasedSampleDetails", sampleList);
		}
		System.out.println("sample end=>" + Instant.now());
		// ALPD-4878-To insert data when filter submit,done by Dhanushya RI

		if (inputMap.containsKey("saveFilterSubmit") && (boolean) inputMap.get("saveFilterSubmit") == true) {
			projectDAOSupport.createFilterSubmit(inputMap, userInfo);
		}
		List<FilterName> lstFilterName = getFilterName(userInfo);
		returnMap.put("FilterName", lstFilterName);
		String strFilterStatus = "select ntranscode as ntransactionstatus, coalesce(jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "', jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus from transactionstatus "
				+ " where ntranscode in (" + Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", "
				+ Enumeration.TransactionStatus.CORRECTION.gettransactionstatus() + ", "
				+ Enumeration.TransactionStatus.RELEASED.gettransactionstatus() + ", "
				+ Enumeration.TransactionStatus.PRELIMINARYRELEASE.gettransactionstatus() + ") and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		List<TransactionStatus> filterStatusValueList = jdbcTemplate.query(strFilterStatus, new TransactionStatus());
		returnMap.put("transactionStatusSelectionList", filterStatusValueList);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);

	}

	public Map<String, Object> seqNoSampleSubSampleTestInsert(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		String npreregno = (String) inputMap.get("npreregno");
		String ncoaParentCode = (String) inputMap.get("ncoaparentcode");
		boolean containsBeforeReleaseRecords = true;
		boolean containsAfterReleaseRecords = false;
		String nCoaParentCodeBeforeRelease = "";
		String nPreRegNoBeforeRelease = "";
		String nTransactionSampleCodeBeforeRelease = "";
		String nTransactionTestCodeBeforeRelease = "";
		String nCoaParentCodeAfterRelease = "";
		String nPreRegNoAfterRelease = "";
		String nTransactionSampleCodeAfterRelease = "";
		String nTransactionTestCodeAfterRelease = "";
		Map<String, Object> afterReleaseRecords = new HashMap<>();
		Map<String, Object> beforeReleaseRecord = new HashMap<>();

		String sQuery = " lock table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus() + "";
		jdbcTemplate.execute(sQuery);

		sQuery = " lock table lockcancelreject " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		if (checkCOAParentStatus(inputMap.get("ncoaparentcode").toString(), userInfo)) {
			if ((int) inputMap.get("isPreliminaryReportNoGenerate") == Enumeration.TransactionStatus.YES
					.gettransactionstatus()) {
				final String strCoaReportHistory = "select cc.ncoachildcode, cc.ncoaparentcode, cc.npreregno, cc.ntransactionsamplecode, cc.ntransactiontestcode, "
						+ " max(crh.ncoareporthistorycode) as ncoareporthistorycode from coachild cc left join coareporthistory crh "
						+ " on cc.ncoaparentcode=crh.ncoaparentcode and crh.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and crh.nsitecode="
						+ userInfo.getNtranssitecode() + " join coaparent cp on cp.ncoaparentcode=cc.ncoaparentcode "
						+ " where cc.ncoaparentcode in (" + inputMap.get("ncoaparentcode") + ") and cc.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cc.nsitecode="
						+ userInfo.getNtranssitecode() + " and cp.ntransactionstatus != "
						+ Enumeration.TransactionStatus.RELEASED.gettransactionstatus()
						+ "  group by cc.ncoachildcode, cc.ncoaparentcode, cc.npreregno, cc.ntransactionsamplecode, cc.ntransactiontestcode";
				final List<COAChild> lstCoaReportHistory = jdbcTemplate.query(strCoaReportHistory, new COAChild());

				final List<Integer> lstCoaParent = Arrays.stream(inputMap.get("ncoaparentcode").toString().split(","))
						.map(Integer::parseInt).collect(Collectors.toList());
				Set<String> nCoaParentCodeAfterReleaseSet = new HashSet<>();
				Set<String> nPreRegNoAfterReleaseSet = new HashSet<>();
				Set<String> nTransactionSampleCodeAfterReleaseSet = new HashSet<>();
				Set<String> nTransactionTestCodeAfterReleaseSet = new HashSet<>();

				Set<String> nCoaParentCodeBeforeReleaseSet = new HashSet<>();
				Set<String> nPreRegNoBeforeReleaseSet = new HashSet<>();
				Set<String> nTransactionSampleCodeBeforeReleaseSet = new HashSet<>();
				Set<String> nTransactionTestCodeBeforeReleaseSet = new HashSet<>();
				if (lstCoaReportHistory.size() > 0) {
					for (COAChild mapObj : lstCoaReportHistory) {
						if (lstCoaParent.contains(mapObj.getNcoaparentcode())
								&& mapObj.getNcoareporthistorycode() > 0) {
							nCoaParentCodeAfterReleaseSet.add(String.valueOf(mapObj.getNcoaparentcode()));
							nPreRegNoAfterReleaseSet.add(String.valueOf(mapObj.getNpreregno()));
							nTransactionSampleCodeAfterReleaseSet
									.add(String.valueOf(mapObj.getNtransactionsamplecode()));
							nTransactionTestCodeAfterReleaseSet.add(String.valueOf(mapObj.getNtransactiontestcode()));
						} else {
							nCoaParentCodeBeforeReleaseSet.add(String.valueOf(mapObj.getNcoaparentcode()));
							nPreRegNoBeforeReleaseSet.add(String.valueOf(mapObj.getNpreregno()));
							nTransactionSampleCodeBeforeReleaseSet
									.add(String.valueOf(mapObj.getNtransactionsamplecode()));
							nTransactionTestCodeBeforeReleaseSet.add(String.valueOf(mapObj.getNtransactiontestcode()));
						}
					}
					nCoaParentCodeAfterRelease = String.join(",", nCoaParentCodeAfterReleaseSet);
					nPreRegNoAfterRelease = String.join(",", nPreRegNoAfterReleaseSet);
					nTransactionSampleCodeAfterRelease = String.join(",", nTransactionSampleCodeAfterReleaseSet);
					nTransactionTestCodeAfterRelease = String.join(",", nTransactionTestCodeAfterReleaseSet);

					nCoaParentCodeBeforeRelease = String.join(",", nCoaParentCodeBeforeReleaseSet);
					;
					nPreRegNoBeforeRelease = String.join(",", nPreRegNoBeforeReleaseSet);
					nTransactionSampleCodeBeforeRelease = String.join(",", nTransactionSampleCodeBeforeReleaseSet);
					nTransactionTestCodeBeforeRelease = String.join(",", nTransactionTestCodeBeforeReleaseSet);

					npreregno = nPreRegNoBeforeRelease;
					ncoaParentCode = nCoaParentCodeBeforeRelease;
					containsBeforeReleaseRecords = nCoaParentCodeBeforeReleaseSet.size() > 0 ? true : false;
					if (containsBeforeReleaseRecords) {
						beforeReleaseRecord.put("nCoaParentCode", nCoaParentCodeBeforeRelease);
						beforeReleaseRecord.put("nPreRegNo", nPreRegNoBeforeRelease);
						beforeReleaseRecord.put("nTransactionSampleCode", nTransactionSampleCodeBeforeRelease);
						beforeReleaseRecord.put("nTransactionTestCode", nTransactionTestCodeBeforeRelease);
					}
					containsAfterReleaseRecords = nCoaParentCodeAfterReleaseSet.size() > 0 ? true : false;
					if (containsAfterReleaseRecords) {
						afterReleaseRecords.put("nCoaParentCode", nCoaParentCodeAfterRelease);
						afterReleaseRecords.put("nPreRegNo", nPreRegNoAfterRelease);
						afterReleaseRecords.put("nTransactionSampleCode", nTransactionSampleCodeAfterRelease);
						afterReleaseRecords.put("nTransactionTestCode", nTransactionTestCodeAfterRelease);
					}
				} else {
					containsBeforeReleaseRecords = true;
					containsAfterReleaseRecords = false;
					beforeReleaseRecord.put("nCoaParentCodeBeforeRelease", inputMap.get("ncoaparentcode"));
					beforeReleaseRecord.put("nPreRegNoBeforeRelease", inputMap.get("npreregno"));
					beforeReleaseRecord.put("nTransactionSampleCodeBeforeRelease",
							inputMap.get("ntransactionsamplecode"));
					beforeReleaseRecord.put("nTransactionTestCodeBeforeRelease", inputMap.get("ntransactiontestcode"));
				}
			} else {
				beforeReleaseRecord.put("nCoaParentCode", inputMap.get("ncoaparentcode"));
				beforeReleaseRecord.put("nPreRegNo", inputMap.get("npreregno"));
				beforeReleaseRecord.put("nTransactionSampleCode", inputMap.get("ntransactionsamplecode"));
				beforeReleaseRecord.put("nTransactionTestCode", inputMap.get("ntransactiontestcode"));
			}

			if (containsBeforeReleaseRecords) {

				final String getSeqNo = "select stablename,nsequenceno from seqnoregistration where stablename in (N'registrationtesthistory',N'registrationhistory',N'registrationsamplehistory',N'patienthistory')"
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

				List<SeqNoRegistration> lstSeqNo = jdbcTemplate.query(getSeqNo, new SeqNoRegistration());
				Map<String, Integer> seqMap = lstSeqNo.stream()
						.collect(Collectors.toMap(SeqNoRegistration::getStablename, SeqNoRegistration::getNsequenceno));

				int ntesthistorycode = seqMap.get("registrationtesthistory");
				int nreghistorycode = seqMap.get("registrationhistory");
				int nsamplehistorycode = seqMap.get("registrationsamplehistory");
				int npatienthistorycode = seqMap.get("patienthistory");

				returnMap.put("ntesthistorycode", ntesthistorycode);
				returnMap.put("nreghistorycode", nreghistorycode);
				returnMap.put("nsamplehistorycode", nsamplehistorycode);
				returnMap.put("npatienthistorycode", npatienthistorycode);

				String clickedTestCode = " select ntransactiontestcode from coachild where  " + " ncoaparentcode in ("
						+ ncoaParentCode + ") and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
						+ userInfo.getNtranssitecode();
				List<String> lstClickedTestCode = jdbcTemplate.queryForList(clickedTestCode, String.class);
				final String ntransactionTestCode = lstClickedTestCode.stream().map(i -> i.toString())
						.collect(Collectors.joining(", "));
				String sParentQuery = " select ncoaparentcode from coaparent where " + " ncoaparentcode in ("
						+ ncoaParentCode + ") and ntransactionstatus in ("
						+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", "
						+ Enumeration.TransactionStatus.PRELIMINARYRELEASE.gettransactionstatus() + ") and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
						+ userInfo.getNtranssitecode();
				final List<COAParent> parentStatus = (List<COAParent>) jdbcTemplate.query(sParentQuery,
						new COAParent());

				if (parentStatus.size() > 0) {
					final String testListQry = "select * from registrationtest where ntransactiontestcode in ("
							+ ntransactionTestCode + ") and nsitecode = " + userInfo.getNtranssitecode()
							+ " and npreregno in (" + npreregno + ") and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

					final List<RegistrationTest> lstAllTestcode = (List<RegistrationTest>) jdbcTemplate
							.query(testListQry, new RegistrationTest());

					String sampleQuery = " select ntransactionsamplecode from registrationtesthistory where ntesthistorycode in ("
							+ " select max(ntesthistorycode) from registrationtesthistory where  ntransactionstatus ="
							+ inputMap.get("ntransactionstatus") + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
							+ userInfo.getNtranssitecode() + " group by ntransactionsamplecode) "
							+ " and npreregno in (" + npreregno + ") and ntransactionstatus ="
							+ inputMap.get("ntransactionstatus") + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
							+ userInfo.getNtranssitecode() + " ";

					final List<RegistrationTestHistory> lstAllSampleCode = (List<RegistrationTestHistory>) jdbcTemplate
							.query(sampleQuery, new RegistrationTestHistory());

					final String preRegSet = String.join(",", lstAllTestcode.stream()
							.map(item -> String.valueOf(item.getNpreregno())).collect(Collectors.toSet()));
					final String sampleRegSet = String.join(",", lstAllSampleCode.stream()
							.map(item -> String.valueOf(item.getNtransactionsamplecode())).collect(Collectors.toSet()));
					final String testRegSet = String.join(",", lstAllTestcode.stream()
							.map(item -> String.valueOf(item.getNtransactiontestcode())).collect(Collectors.toSet()));
					final String patientHistorySet = String.join(",", lstAllTestcode.stream()
							.map(item -> String.valueOf(item.getNtransactiontestcode())).collect(Collectors.toSet()));

					String seqNoUpdate = "update seqnoregistration set nsequenceno = "
							+ (ntesthistorycode + testRegSet.split(",").length)
							+ " where stablename = N'registrationtesthistory' and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

					seqNoUpdate += ";update seqnoregistration set nsequenceno = "
							+ (nreghistorycode + preRegSet.split(",").length) + " "
							+ " where stablename='registrationhistory' and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

					seqNoUpdate += ";update seqnoregistration set nsequenceno = "
							+ (nsamplehistorycode + sampleRegSet.split(",").length)
							+ " where stablename='registrationsamplehistory' and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

					if ((int) inputMap.get("nclinicaltyperequired") == Enumeration.TransactionStatus.YES
							.gettransactionstatus()) {
						seqNoUpdate += ";update seqnoregistration set nsequenceno = "
								+ (npatienthistorycode + patientHistorySet.split(",").length)
								+ " where stablename='patienthistory' and nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
					}

					jdbcTemplate.execute(seqNoUpdate);

					returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
					returnMap.put("containsBeforeReleaseRecords", containsBeforeReleaseRecords);
					returnMap.put("containsAfterReleaseRecords", containsAfterReleaseRecords);
					returnMap.put("afterReleaseRecords", afterReleaseRecords);
					returnMap.put("beforeReleaseRecord", beforeReleaseRecord);
					return returnMap;
				} else {
					String alertPreliminaryReport = "";
					if ((int) inputMap.get("isPreliminaryReportNoGenerate") == Enumeration.TransactionStatus.YES
							.gettransactionstatus()) {
						alertPreliminaryReport = "/"
								+ ((Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) inputMap
										.get("genericLabel")).get("PreliminaryReport")).get("jsondata"))
										.get("sdisplayname")).get(userInfo.getSlanguagetypecode()).toString();
					}
					final String alertMsg = commonFunction.getMultilingualMessage("IDS_SELECTDRAFTCORRECTED",
							userInfo.getSlanguagefilename()) + alertPreliminaryReport + " "
							+ commonFunction.getMultilingualMessage("IDS_RECORDSTORELEASE",
									userInfo.getSlanguagefilename());
					returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), alertMsg);
					return returnMap;
				}
			} else {
				returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
				returnMap.put("containsBeforeReleaseRecords", containsBeforeReleaseRecords);
				returnMap.put("containsAfterReleaseRecords", containsAfterReleaseRecords);
				returnMap.put("afterReleaseRecords", afterReleaseRecords);
				returnMap.put("beforeReleaseRecord", beforeReleaseRecord);
				return returnMap;
			}

		} else {
			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), commonFunction
					.getMultilingualMessage("IDS_SELECTRECORDSWITHSAMESTATUS", userInfo.getSlanguagefilename()));
			return returnMap;
		}

	}

	@Override
	public ResponseEntity<Object> saveAsDraft(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		Map<String, Object> returnMap = new HashMap<>();
		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> objlst = new ArrayList<Map<String, Object>>();

		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditNew = new JSONObject();

		String sLockQuery = " lock table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus() + "";
		jdbcTemplate.execute(sLockQuery);

		sLockQuery = " lock table lockcancelreject " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sLockQuery);

		String ntransactiontestcode = (String) inputMap.get("ntransactiontestcode");

		String validateSample = "select ntransactiontestcode from  coachild  where ntransactiontestcode in ("
				+ ntransactiontestcode + ") and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode=" + userInfo.getNtranssitecode();

		List<String> nonReleasedSamples = jdbcTemplate.queryForList(validateSample, String.class);

		if (nonReleasedSamples.size() != 0) {

			returnMap.put("rtn",
					commonFunction.getMultilingualMessage("IDS_TESTALREADYADDED", userInfo.getSlanguagefilename()));

			return new ResponseEntity<>(returnMap, HttpStatus.OK);

		} else {
			int projectStatus = Enumeration.TransactionStatus.FAILED.gettransactionstatus();
			Map<String, Object> outputProjectMap = new HashMap<>();
			// Added by Dhanushya RI jira id-ALPD-3862 for multiple projects under single
			// release
			if (inputMap.containsKey("nprojectspecrequired")
					&& (int) inputMap.get("nprojectspecrequired") == Enumeration.TransactionStatus.YES
							.gettransactionstatus()
					&& inputMap.containsKey("nismultipleproject")
					&& (int) inputMap.get("nismultipleproject") == Enumeration.TransactionStatus.NO
							.gettransactionstatus()) {
				outputProjectMap = getProjectBasedAlert(inputMap, userInfo);
				projectStatus = (int) outputProjectMap.get("projectStatus");
			} else {
				projectStatus = Enumeration.TransactionStatus.SUCCESS.gettransactionstatus();
			}
			if (projectStatus == Enumeration.TransactionStatus.SUCCESS.gettransactionstatus()) {
				int projectTempStatus = Enumeration.TransactionStatus.FAILED.gettransactionstatus();
				Map<String, Object> outputProjectTempMap = new HashMap<>();

				if (inputMap.containsKey("nsampletypecode")
						&& (int) inputMap.get("nsampletypecode") == Enumeration.SampleType.PROJECTSAMPLETYPE
								.getType()) {
					outputProjectTempMap = getProjectTemplateBasedAlert(inputMap, userInfo);
					projectTempStatus = (int) outputProjectTempMap.get("projectTempStatus");

				} else {
					projectTempStatus = Enumeration.TransactionStatus.SUCCESS.gettransactionstatus();
				}
				if (projectTempStatus == Enumeration.TransactionStatus.SUCCESS.gettransactionstatus()) {
					String stestcode = " select * from registrationtest where ntransactiontestcode in ("
							+ inputMap.get("ntransactiontestcode") + ") and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
							+ userInfo.getNtranssitecode();
					List<RegistrationTest> testCodeList = jdbcTemplate.query(stestcode, new RegistrationTest());
					inputMap.put("testCodeList", testCodeList);

					if ((int) inputMap.get("singleSampleInRelease") == Enumeration.TransactionStatus.YES
							.gettransactionstatus()) {
						returnMap.putAll(createSingleSampleForSingleCOAParent(inputMap, userInfo));
						inputMap.put("ncoaparentcode", returnMap.get("ncoaparentcode"));

					} else {
						// Added by sonia for ALPD-4122 on 17-05-2024 sample count validation
						int reportTemplateCode = (int) inputMap.get("nreporttemplatecode");
						List<Map<String, Object>> reportTemplateList = getReportTemplateById(reportTemplateCode,
								userInfo);
						int maxSampleCount = (int) reportTemplateList.get(0).get("nmaxsamplecount");

						String preRegNoCount = (String) inputMap.get("npreregno");
						String[] parts = preRegNoCount.split(",");
						int size = parts.length;

						if (size <= maxSampleCount || maxSampleCount == 0) {
							returnMap.putAll(createMultipleSamplesForSingleCOAParent(inputMap, userInfo));
							inputMap.put("ncoaparentcode", returnMap.get("ncoaparentcode"));
						} else {
							return new ResponseEntity<>(
									commonFunction.getMultilingualMessage("IDS_SELECTTHEMAXIMUMSAMPLES",
											userInfo.getSlanguagefilename()) + " " + maxSampleCount,
									HttpStatus.CONFLICT);
						}

					}
					insertReleaseHistory(inputMap.get("ncoaparentcode").toString(), userInfo, "");
					insertReleaseTestAttachment(inputMap, userInfo);
					insertReleaseTestComment(inputMap, userInfo);

					inputMap.put("npreregno", inputMap.get("npreregno").toString());
					inputMap.put("ntransactionsamplecode", inputMap.get("ntransactionsamplecode").toString());
					inputMap.put("ntransactiontestcode", inputMap.get("ntransactiontestcode").toString());
					inputMap.put("AuditStatus", "AuditStatus");
					Map<String, Object> lstDataTestNew = getReleaseAuditGet(inputMap, userInfo);

					jsonAuditNew.put("registrationtest",
							(List<Map<String, Object>>) ((Map<String, Object>) lstDataTestNew).get("ReleaseTestAudit"));
					auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
					auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
					auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
					actionType.put("registrationtest", "IDS_ADDSAMPLES");
					auditUtilityFunction.fnJsonArrayAudit(jsonAuditNew, null, actionType, auditmap, false, userInfo);
					returnMap.putAll((Map<String, Object>) getReleaseSample(inputMap, userInfo).getBody());
					returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
					returnMap.put("PortalStatus", objlst);
					return new ResponseEntity<>(returnMap, HttpStatus.OK);
				} else {
					returnMap.put("rtn", commonFunction.getMultilingualMessage("IDS_SELECTSAMETEMPLATE",
							userInfo.getSlanguagefilename()));

					return new ResponseEntity<>(returnMap, HttpStatus.OK);

				}

			} else {
				returnMap.put("rtn", commonFunction.getMultilingualMessage("IDS_SELECTSAMEPROJECT",
						userInfo.getSlanguagefilename()));

				return new ResponseEntity<>(returnMap, HttpStatus.OK);
			}
		}

	}

	public Map<String, Object> createMultipleSamplesForSingleCOAParent(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		String sCoaQuery = "";
		String strReportcomment = "";
		int seqNo = 0;
		Map<String, Object> returnMap = new HashMap<String, Object>();
		String ntransactiontestcode = (String) inputMap.get("ntransactiontestcode");
		List<RegistrationTest> testCodeList = (List<RegistrationTest>) inputMap.get("testCodeList");
		String testcode = testCodeList.stream().map(item -> String.valueOf(item.getNtestcode()))
				.collect(Collectors.joining(","));

		String coaHistorySeq = "select nsequenceno from seqnoreleasemanagement where stablename='coaparent'"
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		seqNo = jdbcTemplate.queryForObject(coaHistorySeq, Integer.class);
		seqNo = seqNo + 1;

		String sCoaparent = "insert into coaparent(ncoaparentcode, nregtypecode, nregsubtypecode,napproveconfversioncode,ncoareporttypecode, sreportno, ntransactionstatus, nenteredby,"
				+ " nenteredrole, ndeputyenteredby, ndeputyenteredrole, dmodifieddate, nsitecode, nstatus, nreporttemplatecode)"
				+ " values (" + seqNo + "," + inputMap.get("nregtypecode") + "," + inputMap.get("nregsubtypecode") + ","
				+ inputMap.get("napprovalversioncode") + "," + inputMap.get("ncoareporttypecode") + ",'" + seqNo + "' ,"
				+ " " + Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + "," + userInfo.getNusercode() + ","
				+ userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode() + "," + userInfo.getNdeputyuserrole()
				+ ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNtranssitecode() + ","
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ", "
				+ inputMap.get("nreporttemplatecode") + ");";

		jdbcTemplate.execute(sCoaparent);

		strReportcomment = "(SELECT " + seqNo + " as ncoareporthistorycode, " + " "
				+ Enumeration.FormCode.TESTMASTER.getFormCode() + " as  nformcode , "
				+ "'reportinfotest' as stablename  , "
				+ "  'ntestcode' as sprimarykeyname ,  ntestcode as nparentmastercode , " + "  kv.key as primaryid "
				+ ", kv.value as primaryvalue,'" + dateUtilityFunction.getCurrentDateTime(userInfo)
				+ "'::timestamp without time zone ," + userInfo.getNtranssitecode() + ",1 " + "FROM reportinfotest  "
				+ "   , json_each_text(row_to_json(reportinfotest.*)) kv " + "WHERE reportinfotest.nsitecode = "
				+ userInfo.getNmastersitecode() + " and ntestcode in(" + testcode
				+ ")  and  kv.key not in ('ntestcode','dmodifieddate','nsitecode','nstatus') " + "union all "
				+ "SELECT " + seqNo + " as ncoareporthistorycode, " + "   " + Enumeration.FormCode.PROJECT.getFormCode()
				+ " as  nformcode , "
				+ "  'reportinfoproject' as stablename  , 'nprojectmastercode' as sprimarykeyname , "
				+ "   nprojectmastercode as nparentmastercode , kv.key as primaryid " + ",  kv.value as primaryvalue ,'"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'::timestamp without time zone ,"
				+ userInfo.getNtranssitecode() + ",1 "
				+ " FROM reportinfoproject, json_each_text(row_to_json(reportinfoproject.*)) kv "
				+ " WHERE reportinfoproject.nsitecode = " + userInfo.getNtranssitecode()
				+ " and nprojectmastercode in(select nprojectmastercode from registration where npreregno in("
				+ inputMap.get("npreregno").toString() + ") and nsitecode = " + userInfo.getNtranssitecode()
				+ ") and  kv.key not in ('nprojectmastercode','dmodifieddate','nsitecode','nstatus')) ";

		sCoaQuery = sCoaQuery
				+ "insert into reportinforelease (ncoaparentcode,nformcode,stablename,sprimarykeyname,nparentmastercode,sreportfieldname,"
				+ " sreportfieldvalue,dmodifieddate,nsitecode,nstatus)  " + strReportcomment + ";";

		jdbcTemplate.execute(sCoaQuery);

		String coaHistorySeqnoUpdate = "update seqnoreleasemanagement set nsequenceno=" + seqNo
				+ " where stablename='coaparent' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

		jdbcTemplate.execute(coaHistorySeqnoUpdate);

		String coaReportHistorySeq = "select nsequenceno from seqnoreleasemanagement where stablename='coachild'"
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		int historySeqNo = jdbcTemplate.queryForObject(coaReportHistorySeq, Integer.class);

		historySeqNo = historySeqNo + 1;
		final int coaChildCount = historySeqNo + ntransactiontestcode.split(",").length;

		String strQuery = "insert into coachild(ncoachildcode, ncoaparentcode, npreregno, ntransactionsamplecode, ntransactiontestcode,"
				+ " nusercode, nuserrolecode, dmodifieddate, nsitecode, nstatus) " + " select " + historySeqNo
				+ " +rank()over(order by rt.ntransactiontestcode) ncoachildcode," + seqNo + " ncoaparentcode,"
				+ " rt.npreregno,rt.ntransactionsamplecode,rt.ntransactiontestcode," + userInfo.getNusercode()
				+ " nusercode," + userInfo.getNuserrole() + " nuserrolecode ," + " '"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' dmodifieddate," + userInfo.getNtranssitecode()
				+ " nsitecode," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " nstatus from  registrationtest rt where rt.ntransactiontestcode in (" + ntransactiontestcode
				+ ") and rt.nsitecode = " + userInfo.getNtranssitecode() + " order by rt.ntransactiontestcode;";

		jdbcTemplate.execute(strQuery);

		String releaseHistorySeqnoUpdate = "update seqnoreleasemanagement set nsequenceno=" + coaChildCount
				+ " where stablename='coachild' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

		jdbcTemplate.execute(releaseHistorySeqnoUpdate);

		returnMap.put("ncoaparentcode", seqNo);
		return returnMap;

	}

	public Map<String, Object> createSingleSampleForSingleCOAParent(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		String strCoaChild = "";
		String strCoaParent = "";
		String sCoaQuery = "";
		int seqNo = 0;
		Map<String, Object> returnMap = new HashMap<String, Object>();
		List<String> lstnpreregno = Arrays.asList(inputMap.get("npreregno").toString().split(","));
		List<RegistrationTest> testCodeList = (List<RegistrationTest>) inputMap.get("testCodeList");

		String coaHistorySeq = "select nsequenceno from seqnoreleasemanagement where stablename='coaparent'"
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		seqNo = jdbcTemplate.queryForObject(coaHistorySeq, Integer.class);

		for (int i = 0; i < lstnpreregno.size(); i++) {
			int preregno = Integer.parseInt(lstnpreregno.get(i));
			List<RegistrationTest> filteredTestList = testCodeList.stream().filter((x) -> {
				return (int) x.getNpreregno() == preregno;
			}).collect(Collectors.toList());

			String stransactiontestcode = filteredTestList.stream()
					.map(item -> String.valueOf(item.getNtransactiontestcode())).collect(Collectors.joining(","));

			seqNo = seqNo + 1;

			strCoaParent = strCoaParent + "(" + seqNo + "," + inputMap.get("nregtypecode") + ","
					+ inputMap.get("nregsubtypecode") + "," + inputMap.get("napprovalversioncode") + ","
					+ inputMap.get("ncoareporttypecode") + ",'" + seqNo + "' ," + " "
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + "," + userInfo.getNusercode() + ","
					+ userInfo.getNuserrole() + ", " + " " + userInfo.getNdeputyusercode() + ","
					+ userInfo.getNdeputyuserrole() + ", " + " '" + dateUtilityFunction.getCurrentDateTime(userInfo)
					+ "'," + userInfo.getNtranssitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ", "
					+ inputMap.get("nreporttemplatecode") + "),";

			String coaReportHistorySeq = "select nsequenceno from seqnoreleasemanagement where stablename='coachild'"
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			int historySeqNo = jdbcTemplate.queryForObject(coaReportHistorySeq, Integer.class);
			final int coaChildCount = historySeqNo + stransactiontestcode.split(",").length;

			strCoaChild = strCoaChild + "(select " + historySeqNo
					+ " +rank()over(order by rt.ntransactiontestcode) ncoachildcode," + seqNo + ","
					+ " rt.npreregno,rt.ntransactionsamplecode,rt.ntransactiontestcode," + " " + userInfo.getNusercode()
					+ " nusercode," + userInfo.getNuserrole() + " nuserrolecode ," + " '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo)
					+ "' ::timestamp without time zone dmodifieddate," + userInfo.getNtranssitecode() + " nsitecode,"
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
					+ " from  registrationtest rt where rt.ntransactiontestcode in (" + stransactiontestcode
					+ ")  and rt.nsitecode = " + userInfo.getNtranssitecode()
					+ " order by rt.ntransactiontestcode) union all";

			String releaseHistorySeqnoUpdate = "update seqnoreleasemanagement set nsequenceno=" + coaChildCount
					+ " where stablename='coachild' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

			jdbcTemplate.execute(releaseHistorySeqnoUpdate);
		}
		String coaHistorySeqnoUpdate = "update seqnoreleasemanagement set nsequenceno=" + seqNo
				+ " where stablename='coaparent' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		jdbcTemplate.execute(coaHistorySeqnoUpdate);

		sCoaQuery = "insert into coaparent(ncoaparentcode, nregtypecode, nregsubtypecode,napproveconfversioncode,ncoareporttypecode, sreportno, ntransactionstatus, nenteredby,"
				+ " nenteredrole, ndeputyenteredby, ndeputyenteredrole, dmodifieddate, nsitecode, nstatus, nreporttemplatecode) values "
				+ strCoaParent.substring(0, strCoaParent.length() - 1) + ";";

		sCoaQuery = sCoaQuery
				+ "insert into coachild(ncoachildcode, ncoaparentcode, npreregno, ntransactionsamplecode, ntransactiontestcode,"
				+ " nusercode, nuserrolecode, dmodifieddate, nsitecode, nstatus)  "
				+ strCoaChild.substring(0, strCoaChild.length() - 9) + ";";

		jdbcTemplate.execute(sCoaQuery);
		returnMap.put("ncoaparentcode", seqNo);
		return returnMap;

	}

	public ResponseEntity<Map<String, Object>> getReleaseSubSample(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		Map<String, Object> returnMap = new HashMap<>();
		List<Map<String, Object>> subSampleList = new ArrayList<Map<String, Object>>();
		String ntransactionsamplecode = "";

		System.out.println("ssubample query  start=>" + Instant.now());

		String ntransactionstatus = (String) inputMap.get("ntransactionstatus") + ","
				+ String.valueOf(Enumeration.TransactionStatus.PARTIALLY.gettransactionstatus()) + ","
				+ String.valueOf(Enumeration.TransactionStatus.RELEASED.gettransactionstatus());

		final String sQuery = "SELECT * from fn_releasesubsampleget('" + inputMap.get("npreregno") + "', '"
				+ ntransactionstatus + "'," + userInfo.getNusercode() + "," + inputMap.get("nregtypecode") + ","
				+ inputMap.get("nregsubtypecode") + ",'" + inputMap.get("napprovalversioncode") + "',"
				+ userInfo.getNsitecode() + "," + " '" + inputMap.get("ncoaparentcode") + "',"
				+ inputMap.get("ncoareporttypecode") + "," + " '" + userInfo.getSlanguagetypecode() + "',"
				+ inputMap.get("nneedmodal") + ",'" + inputMap.get("finalLevelStatus") + "',"
				+ inputMap.get("nsectioncode") + ",'" + inputMap.get("filterquery") + "');";
		LOGGER.info(sQuery);
		ObjectMapper objmapper = new ObjectMapper();
		System.out.println("sub sample Start========?" + LocalDateTime.now());
		System.out.println("sub sample query:" + sQuery);
		String subSampleListString = jdbcTemplate.queryForObject(sQuery, String.class);
		System.out.println("subsample query  end=>" + LocalDateTime.now());
		Map<Object, List<Map<String, Object>>> subSampleMap = new HashMap<>();

		if (subSampleListString != null) {
			subSampleList = objmapper.readValue(subSampleListString, new TypeReference<List<Map<String, Object>>>() {
			});
			subSampleMap = subSampleList.stream().collect(Collectors.groupingBy(item -> item.get("npreregno")));
			ntransactionsamplecode = subSampleList.stream().map(x -> String.valueOf(x.get("ntransactionsamplecode")))
					.collect(Collectors.joining(","));
			inputMap.put("ntransactionsamplecode", ntransactionsamplecode);
		} else {
			inputMap.put("ntransactionsamplecode", 0);
		}

		returnMap.putAll((Map<String, Object>) getReleaseTest(inputMap, userInfo).getBody());

		if (inputMap.containsKey("isPopup") && (boolean) inputMap.get("isPopup") == true) {
			returnMap.put("ReleaseSubSample", subSampleMap);
		} else {
			returnMap.put("ReleasedSubSampleDetails", subSampleMap);
		}
		return new ResponseEntity<>(returnMap, HttpStatus.OK);

	}

	public ResponseEntity<Map<String, Object>> getReleaseTest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		Map<String, Object> returnMap = new HashMap<>();
		ObjectMapper objmapper = new ObjectMapper();
		List<Map<String, Object>> testList = new ArrayList<Map<String, Object>>();
		String ntransactionstatus = "";

		if (inputMap.containsKey("isAddPopup") && (boolean) inputMap.get("isAddPopup") == true) {
			List<String> lsttransactionstatus = Arrays.asList(inputMap.get("ntransactionstatus").toString().split(","));
			ntransactionstatus = lsttransactionstatus.get(0).toString();
		} else {
			ntransactionstatus = (String) inputMap.get("ntransactionstatus") + ","
					+ String.valueOf(Enumeration.TransactionStatus.RELEASED.gettransactionstatus());
		}

		final String sQuery = "SELECT * from fn_releasetestget(" + inputMap.get("nregtypecode") + " , "
				+ inputMap.get("nregsubtypecode") + ",'" + inputMap.get("npreregno") + "' ,'"
				+ inputMap.get("ntransactionsamplecode") + "','" + inputMap.get("ntransactiontestcode") + "'," + " '"
				+ ntransactionstatus + "'," + inputMap.get("napprovalversioncode") + ", " + userInfo.getNusercode()
				+ ",'" + inputMap.get("ncoaparentcode") + "'," + inputMap.get("ncoareporttypecode") + ","
				+ userInfo.getNtranssitecode() + ",'" + userInfo.getSlanguagetypecode() + "',"
				+ inputMap.get("nneedmodal") + ",'" + inputMap.get("finalLevelStatus") + "',"
				+ inputMap.get("nsectioncode") + ",'" + inputMap.get("filterquery") + "');";

		LOGGER.info(sQuery);
		System.out.println("test query start====>" + LocalDateTime.now());
		System.out.println("test query=====>" + sQuery);
		String testListString = jdbcTemplate.queryForObject(sQuery, String.class);
		System.out.println("test end========?" + LocalDateTime.now());

		if (testListString != null) {
			testList = objmapper.readValue(testListString, new TypeReference<List<Map<String, Object>>>() {
			});
			Map<Object, List<Map<String, Object>>> subSampleMap = testList.stream()
					.collect(Collectors.groupingBy(item -> item.get("ntransactionsamplecode")));

			if (inputMap.containsKey("isPopup") && (boolean) inputMap.get("isPopup") == true) {
				returnMap.put("ReleaseTest", subSampleMap);
			} else {
				returnMap.put("ReleasedTestDetails", subSampleMap);
			}
		} else {
			returnMap.put("ReleaseTest", new ArrayList<Map<String, Object>>());
		}

		return new ResponseEntity<>(returnMap, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Map<String, Object>> getCOAReportType(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new HashMap<>();
		COAReportType reportTypeValue = new COAReportType();
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		int reportTypeCode = 0;
		int isNeedSection = 4;

		// Added by sonia on 4th OCT 2024 for jira id:ALPD-4979 nsorter column
		final String queryString = "select coalesce (ct.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',ct.jsondata->'sdisplayname'->>'en-US') as sdisplayname,sct.nsorter,"
				+ "ct.ncoareporttypecode, ct.nreporttypecode, ct.scoareporttypename, ct.isneedsection, ct.nisarnowiserelease,"
				+ " ct.nneedreleaseformattoggle, ct.nismultipleproject "
				+ " from coareporttype ct,samplecoareporttype sct,sampletype st "
				+ " where ct.ncoareporttypecode=sct.ncoareporttypecode and sct.nsampletypecode=st.nsampletypecode and "
				+ " st.nsampletypecode=" + inputMap.get("nsampletypecode") + " and ct.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sct.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and st.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by sct.nsorter ";

		List<COAReportType> reportType = jdbcTemplate.query(queryString, new COAReportType());

		if (reportType.size() > 0) {

			final COAReportType filterReportType = inputMap.containsKey("filterDetailValue")
					&& !((List<Map<String, Object>>) inputMap.get("filterDetailValue")).isEmpty()
					&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0)
							.containsKey("reportTypeValue")
									? objMapper.convertValue(
											((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0)
													.get("reportTypeValue"),
											COAReportType.class)
									: reportType.get(0);
			reportTypeValue = filterReportType;
			reportTypeCode = filterReportType.getNcoareporttypecode();
			isNeedSection = filterReportType.getIsneedsection();

		}
		outputMap.put("ReportTypeValue", reportTypeValue);
		outputMap.put("ReportType", reportType);
		outputMap.put("realReportTypeList", reportType);
		outputMap.put("ncoareporttypecode", reportTypeCode);
		outputMap.put("isneedsection", isNeedSection);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public Map<String, Object> viewAttachedCOAHistoryFile(COAHistory objectCOAHistory, UserInfo objUserInfo)
			throws Exception {
		Map<String, Object> map = new HashMap<>();
		String strQuery = "select ncoahistorycode,ssystemfilename from coahistory where ncoahistorycode = "
				+ objectCOAHistory.getNcoahistorycode() + " and  nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
				+ objUserInfo.getNtranssitecode();
		COAHistory objCOAHistory = (COAHistory) jdbcUtilityFunction.queryForObject(strQuery, COAHistory.class,
				jdbcTemplate);

		if (objCOAHistory != null) {
			String sQuery = "select * from COAHistory where ncoahistorycode = " + objCOAHistory.getNcoahistorycode()
					+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode = " + objUserInfo.getNtranssitecode();
			final COAHistory objCH = (COAHistory) jdbcUtilityFunction.queryForObject(sQuery, COAHistory.class,
					jdbcTemplate);
			if (objCH != null) {
				map = ftpUtilityFunction.FileViewUsingFtp(objCH.getSsystemfilename(), -1, objUserInfo, "", "");// Folder
																												// Name
																												// -
																												// master
				final List<Object> lstObject = new ArrayList<>();
				lstObject.add(objCOAHistory);

			}
		}
		return map;
	}

	public ResponseEntity<Map<String, Object>> getReleaseHistory(final Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		List<COAParent> SelectedReleaseHistory = new ArrayList<>();
		Map<String, Object> returnMap = new HashMap<>();
		String fromDate = (String) inputMap.get("dfrom");
		String toDate = (String) inputMap.get("dto");
		fromDate = dateUtilityFunction
				.instantDateToString(dateUtilityFunction.convertStringDateToUTC(fromDate, userInfo, true));
		toDate = dateUtilityFunction
				.instantDateToString(dateUtilityFunction.convertStringDateToUTC(toDate, userInfo, true));

		String querySampleCertMail = " select cr.ncoaparentcode,CONCAT(u.sfirstname,' ',u.slastname) as susername,cr.ntransactionstatus, "
				+ " COALESCE(TO_CHAR(cr.dmodifieddate,'" + userInfo.getSpgsitedatetime()
				+ "'),'') as sgenerateddate,string_agg(distinct(cc.npreregno)::text, ',') as spreregno, "
				+ " string_agg(distinct(cc.ntransactionsamplecode)::text, ',') as stransactionsamplecode, "
				+ " string_agg(distinct(cr.sreportno)::text, ',') as sreportno,"
				+ " string_agg(distinct(cc.ntransactiontestcode)::text, ',') as stransactiontestcode, "
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ "	ts.jsondata->'stransdisplaystatus'->>'en-US')  as stransdisplaystatus, cm.scolorhexcode, "
				+ " (select coalesce(max(nversionno), -1) nversionno "
				+ " from coareporthistory crh where crh.ncoaparentcode=cr.ncoaparentcode and " + " crh.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and crh.nsitecode = "
				+ userInfo.getNtranssitecode() + "), " + " (select coalesce(max(nversionno)::text, '') as sversionno "
				+ " from coareporthistory crh where crh.ncoaparentcode=cr.ncoaparentcode and " + " crh.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and crh.nsitecode = "
				+ userInfo.getNtranssitecode() + "), "
				+ " cr.nreporttemplatecode, coalesce(rt.jsondata->'stemplatename'->>'" + userInfo.getSlanguagetypecode()
				+ "',rt.jsondata->'stemplatename'->>'en-US') sreporttemplatename "
				+ " from coaparent cr,users u,coachild cc ,transactionstatus ts, formwisestatuscolor fwsc, colormaster cm, reporttemplate rt where   "
				+ " cr.ncoareporttypecode =" + inputMap.get("ncoareporttypecode") + "" + " and cr.nregtypecode ="
				+ inputMap.get("nregtypecode") + " and cr.nregsubtypecode =" + inputMap.get("nregsubtypecode") + ""
				+ " and cc.ncoaparentcode=cr.ncoaparentcode  and u.nusercode=cr.nenteredby "
				+ " and ts.ntranscode=cr.ntransactionstatus  and cc.nsitecode=cr.nsitecode "
				+ " and cr.dmodifieddate between '" + fromDate + "'::timestamp and '" + toDate + "'::timestamp"
				+ " and cr.napproveconfversioncode=" + inputMap.get("napprovalversioncode") + " and fwsc.nformcode="
				+ userInfo.getNformcode() + " and ts.ntranscode=fwsc.ntranscode and "
				+ " fwsc.ncolorcode=cm.ncolorcode and fwsc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and cr.nreporttemplatecode=rt.nreporttemplatecode and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cr.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cr.nsitecode="
				+ userInfo.getNtranssitecode() + " and u.nsitecode = " + userInfo.getNmastersitecode()
				+ " and cc.nsitecode = " + userInfo.getNtranssitecode()
				+ " group by  cr.ncoaparentcode,susername,sgenerateddate,stransdisplaystatus,cr.ntransactionstatus,"
				+ " cm.scolorhexcode, cr.nreporttemplatecode, sreporttemplatename";

		List<COAParent> coaHistoryList = (List<COAParent>) jdbcTemplate.query(querySampleCertMail, new COAParent());
		String ncoaparentcode = "";
		String selectedReleaseHistoryCOAParentCode = "";
		if (!coaHistoryList.isEmpty()) {
			ncoaparentcode = inputMap.get("ncoaparentcode") != null ? inputMap.get("ncoaparentcode").toString()
					: String.valueOf(coaHistoryList.get(coaHistoryList.size() - 1).getNcoaparentcode());

			SelectedReleaseHistory = Arrays.stream(ncoaparentcode.split(",")).map(x -> Integer.parseInt(x)) // Convert
																											// each
																											// substring
																											// to an
																											// integer
					.flatMap(y -> coaHistoryList.stream().filter(z -> z.getNcoaparentcode() == y))
					.collect(Collectors.toList());
			selectedReleaseHistoryCOAParentCode = SelectedReleaseHistory.stream()
					.map(x -> String.valueOf(x.getNcoaparentcode())).collect(Collectors.joining(","));
			if (SelectedReleaseHistory.size() > 0) {
				returnMap.put("selectedReleaseHistory", SelectedReleaseHistory);
				returnMap.put("ReleaseHistory", coaHistoryList);
				returnMap.put("ncoaparentcode", selectedReleaseHistoryCOAParentCode);
			} else {
				returnMap.put("selectedReleaseHistory", Arrays.asList(coaHistoryList.get(coaHistoryList.size() - 1)));
				returnMap.put("ReleaseHistory", coaHistoryList);
				returnMap.put("ncoaparentcode", coaHistoryList.get(coaHistoryList.size() - 1).getNcoaparentcode());
			}
			if ((int) inputMap.get("isneedsection") == Enumeration.TransactionStatus.YES.gettransactionstatus()
					|| (int) inputMap.get("ncoareporttypecode") == Enumeration.COAReportType.SECTIONWISEMULTIPLESAMPLE
							.getcoaReportType()) {
				inputMap.put("ncoaparentcode", returnMap.get("ncoaparentcode"));
				returnMap.putAll((Map<String, Object>) getSectionValue(inputMap, userInfo));
			} else {
				returnMap.put("reportSectionCode", -1);
			}

		} else {
			returnMap.put("ReleaseHistory", coaHistoryList);
			returnMap.put("selectedReleaseHistory", coaHistoryList);
			returnMap.put("ncoaparentcode", 0);
			returnMap.put("nneedmodal", true);
		}

		if (inputMap.containsKey("isSearch")) {
			inputMap.put("ncoaparentcode", selectedReleaseHistoryCOAParentCode);
			inputMap.put("npreregno", 0);
			returnMap.putAll((Map<String, Object>) getReleaseSample(inputMap, userInfo).getBody());

		}

		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public Map<String, Object> updateRelease(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		final Map<String, Object> returnMap = new HashMap<String, Object>();
		final Map<String, Object> inputMapAfterRelease = new HashMap<>();

		inputMapAfterRelease.putAll(inputMap);

		if ((boolean) inputMap.get("containsBeforeReleaseRecords")) {

			inputMap.put("ncoaparentcode",
					((Map<String, Object>) inputMap.get("beforeReleaseRecord")).get("nCoaParentCode"));
			inputMap.put("npreregno", ((Map<String, Object>) inputMap.get("beforeReleaseRecord")).get("nPreRegNo"));
			inputMap.put("ntransactionsamplecode",
					((Map<String, Object>) inputMap.get("beforeReleaseRecord")).get("nTransactionSampleCode"));
			inputMap.put("ntransactiontestcode",
					((Map<String, Object>) inputMap.get("beforeReleaseRecord")).get("nTransactionTestCode"));

			String stable = "";
			String scondition = "";
			int nsectionCode = -1;

			final String stringQuery = " lock  table lockcoareporthistory "
					+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus() + "";

			jdbcTemplate.execute(stringQuery);

			if (inputMap.containsKey("reportSectionCode")) {

				nsectionCode = (int) inputMap.get("reportSectionCode");
			}

			final String conditionString = " and coa.ncoareporttypecode=" + inputMap.get("ncoareporttypecode")
					+ " and rm.nregtypecode=" + inputMap.get("nregtypecode") + " and rm.nregsubtypecode="
					+ inputMap.get("nregsubtypecode") + " and rm.napproveconfversioncode="
					+ inputMap.get("napprovalversioncode") + " and rd.ntransactionstatus="
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and rm.nreportdecisiontypecode="
					+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " and rt.nreporttypecode="
					+ Enumeration.ReportType.COA.getReporttype() + " and rm.ncertificatetypecode = "
					+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " and rm.ncontrolcode = "
					+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " and rm.nsectioncode = "
					+ nsectionCode;

			if (((int) inputMap.get("ncoareporttypecode") == Enumeration.COAReportType.PROJECTWISE.getcoaReportType()
					|| (int) inputMap.get("ncoareporttypecode") == Enumeration.COAReportType.SECTIONWISEMULTIPLESAMPLE
							.getcoaReportType())
					&& (int) inputMap.get("isSMTLFlow") == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				stable += ",reportinfoproject rip";
				scondition += " and rip.nreporttemplatecode=rm.nreporttemplatecode and rip.nprojectmastercode="
						+ (int) inputMap.get("nprojectcode") + " and rip.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			}

			final String getMainReports = " select * from reportmaster rm,reportdetails rd,  reporttemplate rtt,reporttype rt ,coareporttype coa,coaparent cp "
					+ stable
					+ " where rtt.nreporttemplatecode = rm.nreporttemplatecode and cp.nreporttemplatecode = rtt.nreporttemplatecode and coa.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rtt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and rm.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cp.nsitecode = "
					+ userInfo.getNtranssitecode() + " and rm.nsitecode = " + userInfo.getNmastersitecode()
					+ " and rd.nsitecode = " + userInfo.getNmastersitecode()
					+ " and rt.nreporttypecode=rm.nreporttypecode and coa.ncoareporttypecode=rm.ncoareporttypecode"
					+ " and rm.nreportcode=rd.nreportcode and cp.ncoaparentcode in ("
					+ inputMap.get("ncoaparentcode").toString() + ") and  cp.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rm.ntransactionstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + conditionString + scondition
					+ " limit 1";
			ReportDetails mainReport = (ReportDetails) jdbcUtilityFunction.queryForObject(getMainReports,
					ReportDetails.class, jdbcTemplate);

			if (mainReport != null) {

				inputMap.put("nreportdetailcode", mainReport.getNreportdetailcode());

				returnMap.putAll(insertCOAParentRelatedData(inputMap, userInfo));

				insertReleaseHistory(inputMap.get("ncoaparentcode").toString(), userInfo, "");

			} else {
				returnMap.put("ReportAvailable", Enumeration.ReturnStatus.FAILED.getreturnstatus());
				if (((int) inputMap.get("ncoareporttypecode") == Enumeration.COAReportType.PROJECTWISE
						.getcoaReportType()
						|| (int) inputMap
								.get("ncoareporttypecode") == Enumeration.COAReportType.SECTIONWISEMULTIPLESAMPLE
										.getcoaReportType())
						&& (int) inputMap.get("isSMTLFlow") == Enumeration.TransactionStatus.YES
								.gettransactionstatus()) {
					returnMap.put("ProjectTypeFlow", Enumeration.TransactionStatus.YES.gettransactionstatus());
				}
			}
		}

		if ((boolean) inputMapAfterRelease.get("containsAfterReleaseRecords")) {
			final Map<String, Object> strInputMap = new HashMap<>();
			strInputMap.putAll(inputMapAfterRelease);
			strInputMap.put("ncoaparentcode",
					((Map<String, Object>) inputMap.get("afterReleaseRecords")).get("nCoaParentCode"));
			strInputMap.put("npreregno", ((Map<String, Object>) inputMap.get("afterReleaseRecords")).get("nPreRegNo"));
			strInputMap.put("ntransactionsamplecode",
					((Map<String, Object>) inputMap.get("afterReleaseRecords")).get("nTransactionSampleCode"));
			strInputMap.put("ntransactiontestcode",
					((Map<String, Object>) inputMap.get("afterReleaseRecords")).get("nTransactionTestCode"));
			returnMap.putAll(updateReleaseAfterCorrection(strInputMap, userInfo));
			returnMap.put("ReportAvailable", Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		}

		returnMap.putAll((Map<String, Object>) getReleaseSample(inputMapAfterRelease, userInfo).getBody());

		return returnMap;

	}

	public ResponseEntity<Map<String, Object>> viewReport(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		String reportpresentQuery = "select * from coareporthistory where ncoaparentcode ="
				+ inputMap.get("ncoaparentcode") + " " + " and nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
				+ userInfo.getNtranssitecode() + " order by 1 desc LIMIT 1 ";

		COAReportHistory objreportpresent = (COAReportHistory) jdbcUtilityFunction.queryForObject(reportpresentQuery,
				COAReportHistory.class, jdbcTemplate);

		if (objreportpresent != null) {

			String sQuery = "select ssettingvalue from reportsettings where nreportsettingcode = "
					+ Enumeration.ReportSettings.REPORT_DOWNLOAD_URL.getNreportsettingcode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			String fileDownloadURL = jdbcTemplate.queryForObject(sQuery, String.class);
			outputMap.put("rtn", Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
			outputMap.put("filepath", fileDownloadURL + objreportpresent.getSsystemfilename());
			outputMap.put("ReportPDFFile", objreportpresent.getSsystemfilename());
			outputMap.put("nversionno", objreportpresent.getNversionno());

			return new ResponseEntity<>(outputMap, HttpStatus.OK);

		} else {
			outputMap.put("ReportPDFFile", new ArrayList<>());
			outputMap.put("nversionno", 0);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}

	}

	@Override
	public ResponseEntity<Object> getApprovedProjectType(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		final String strQuery = "select pt.nprojecttypecode, pt.sprojecttypename from projecttype pt,"
				+ " projectmaster pm,registration r where pt.nstatus = pm.nstatus " + " and pm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and r.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and pt.nprojecttypecode=pm.nprojecttypecode and pm.nprojectmastercode= r.nprojectmastercode "
				+ " and r.nregtypecode = " + inputMap.get("nregtypecode") + " and r.nsampletypecode = "
				+ inputMap.get("nsampletypecode") + " and r.nregsubtypecode = " + inputMap.get("nregsubtypecode")
				+ " and r.nsitecode=" + userInfo.getNtranssitecode() + " and pm.nsitecode="
				+ userInfo.getNtranssitecode() + " and pt.nsitecode=" + userInfo.getNmastersitecode()
				+ " group by pt.nprojecttypecode";

		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new ProjectType()), HttpStatus.OK);

	}

	public ResponseEntity<Map<String, Object>> getApprovedProjectByProjectType(Map<String, Object> inputMap,
			UserInfo objUserInfo) {

		Map<String, Object> returnMap = new HashMap<>();
		String sQuery = "";
		String npreregno = "";

		if (inputMap.containsKey("nneededit")) {
			sQuery = "select npreregno from coachild where ncoaparentcode=" + inputMap.get("ncoaparentcode")
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ objUserInfo.getNtranssitecode();

			List<String> preregNoList = jdbcTemplate.queryForList(sQuery, String.class);
			npreregno = preregNoList.stream().collect(Collectors.joining(","));
		}

		String strQuery = "select pm.nprojectmastercode,pm.sprojectcode "
				+ " from projectmaster pm,registration r   where pm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and r.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and pm.nprojectmastercode= r.nprojectmastercode and r.nregtypecode = "
				+ inputMap.get("nregtypecode") + " and r.nsampletypecode = " + inputMap.get("nsampletypecode")
				+ " and r.nregsubtypecode = " + inputMap.get("nregsubtypecode") + " and pm.nprojecttypecode="
				+ inputMap.get("nprojecttypecode") + " and r.nsitecode=" + objUserInfo.getNtranssitecode()
				+ " and pm.nsitecode=" + objUserInfo.getNtranssitecode();

		if (inputMap.containsKey("nneededit")) {
			strQuery = strQuery + " and r.npreregno in (" + npreregno + ")";
		} else {
			strQuery = strQuery + " group by pm.nprojectmastercode,pm.sprojectcode";

		}

		List<ProjectMaster> projectMasterList = jdbcTemplate.query(strQuery, new ProjectMaster());
		if (projectMasterList.isEmpty()) {
			returnMap.put("projectMasterList", projectMasterList);
		} else {
			returnMap.put("projectMasterList", projectMasterList);
			returnMap.put("nprojectmastercode", projectMasterList.get(0).getNprojectmastercode());

		}
		return new ResponseEntity<>(returnMap, HttpStatus.OK);

	}

	public ResponseEntity<Object> SendToPortalReport(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		List<String> objreportpresent = new ArrayList<String>();
		String sorderseqno = "";
		List<String> lstnpreregno = Arrays.asList(inputMap.get("allPreregno").toString().split("\\s*,\\s*"));

		String reportpresentQuery = "select ssystemfilename from coareporthistory where ncoaparentcode ="
				+ inputMap.get("ncoaparentcode") + " " + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNtranssitecode() + " order by ncoareporthistorycode desc LIMIT 1";
		objreportpresent = jdbcTemplate.queryForList(reportpresentQuery, String.class);

		if (!objreportpresent.isEmpty()) {
			String strQuery = "select ssettingvalue from reportsettings where nreportsettingcode = "
					+ Enumeration.ReportSettings.REPORT_PDF_PATH.getNreportsettingcode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			String fileDownloadURL = jdbcTemplate.queryForObject(strQuery, String.class);
			outputMap.put("filepath", fileDownloadURL + objreportpresent.get(0));
			if (inputMap.containsKey("isPortal") && (boolean) inputMap.get("isPortal")) {
				final Map<String, Object> registrationDatas = registrationDAOSupport.getRegistrationLabel(inputMap,
						userInfo);
				List<Map<String, Object>> lstPortalStatus = new ArrayList<>();
				Map<String, Object> mapPortalValues = new HashMap<>();
				if (!registrationDatas.isEmpty()) {
					for (int k = 0; k < lstnpreregno.size(); k++) {
						Map<String, Object> registrationData = (Map<String, Object>) registrationDatas
								.get(String.valueOf(lstnpreregno.get(k)));
						sorderseqno = (String) registrationData.get("sorderseqno").toString();
						mapPortalValues.put("serialnumber", sorderseqno);
						mapPortalValues.put("statuscode",
								Enumeration.TransactionStatus.RELEASED.gettransactionstatus());
						lstPortalStatus.add(mapPortalValues);
					}
					File textFile = new File((fileDownloadURL + objreportpresent.get(0)).toString());
					MultipartEntityBuilder builder = MultipartEntityBuilder.create()
							.addPart("file", new FileBody(textFile))
							.addTextBody("filename", objreportpresent.get(0).toString())
							.addTextBody("sampleid", sorderseqno);
					HttpEntity entity = builder.build();
					HttpPost request = new HttpPost(inputMap.get("url").toString() + "/portal/FileUploadReport");
					request.setEntity(entity);

					HttpClient httpClient = HttpClients.createDefault();
					HttpPost httpPost = new HttpPost(
							inputMap.get("url").toString() + "/portal/UpdateMultiSampleStatus");
					httpPost.setHeader("Content-Type", "application/json");
					JSONArray jsonArray = new JSONArray(lstPortalStatus);
					String jsonParams = (String) jsonArray.toString();
					StringEntity entityValue = new StringEntity(jsonParams);
					httpPost.setEntity(entityValue);
					HttpResponse responseValue = httpClient.execute(httpPost);
					int statusCode = responseValue.getStatusLine().getStatusCode();
					if (statusCode == 200) {
						LOGGER.info("Portal Status sent succesfully");
					} else {
						LOGGER.info("Error in sending status to portal");
					}

					outputMap.put("rtn", commonFunction.getMultilingualMessage("IDS_REPORTSENDTOPORTALSUCCESSFULLY",
							userInfo.getSlanguagefilename()));

				} else {
					outputMap.put("rtn", commonFunction.getMultilingualMessage("IDS_REPORTISNOTSENDTOPORTAL",
							userInfo.getSlanguagefilename()));
				}
			}
			return new ResponseEntity<>(outputMap, HttpStatus.OK);

		} else {
			outputMap.put("rtn",
					commonFunction.getMultilingualMessage("IDS_NEEDTOGENERATEREPORT", userInfo.getSlanguagefilename()));

			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}

	}

	@Override
	public ResponseEntity<Map<String, Object>> preliminaryRegenerateReport(Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception {

		int naccredit = Enumeration.TransactionStatus.NO.gettransactionstatus();
		int nregTypeCode = -1;
		int nregSubTypeCode = -1;
		String sreportComments = "";
		int nsectionCode = -1;
		String stable = "";
		String scondition = "";
		int nreporttypecode = 0;
		int ncontrolCode = -1;

		final Map<String, Object> returnMap = new HashMap<String, Object>();

		final String sLockQuery = " lock  table lockregeneratereporthistory "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus() + "";

		jdbcTemplate.execute(sLockQuery);

		if (!userInfo.getSreason().isEmpty()) {
			sreportComments = userInfo.getSreason();
		}
		if (inputMap.containsKey("nregtypecode")) {
			nregTypeCode = (int) inputMap.get("nregtypecode");
		}
		if (inputMap.containsKey("ncontrolcode")) {
			ncontrolCode = (int) inputMap.get("ncontrolcode");
		}
		if (inputMap.containsKey("nregsubtypecode")) {
			nregSubTypeCode = (int) inputMap.get("nregsubtypecode");
		}
		if (inputMap.containsKey("nreporttypecode")) {
			nreporttypecode = (int) inputMap.get("nreporttypecode");
		}
		if (inputMap.containsKey("reportSectionCode")) {
			nsectionCode = (int) inputMap.get("reportSectionCode");
		}

		inputMap.put("ncontrolcode",
				(int) inputMap.get("nreporttypecode") == Enumeration.ReportType.CONTROLBASED.getReporttype()
						? ncontrolCode
						: -1);

		inputMap.put("naccredit", naccredit);
		inputMap.put("nregTypeCode", nregTypeCode);
		inputMap.put("nregSubTypeCode", nregSubTypeCode);
		inputMap.put("sreportComments", sreportComments);
		inputMap.put("nsectioncode", nsectionCode);

		final String conditionString = " and coa.ncoareporttypecode=" + inputMap.get("ncoareporttypecode") + " "
				+ " and rm.nreporttypecode = " + nreporttypecode + " and rm.nregtypecode=" + nregTypeCode
				+ " and rm.nregsubtypecode=" + nregSubTypeCode + " and rm.napproveconfversioncode="
				+ inputMap.get("napproveconfversioncode") + " and rd.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and rm.nreportdecisiontypecode="
				+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " and rt.nreporttypecode=" + nreporttypecode
				+ " and rm.ncertificatetypecode = " + Enumeration.TransactionStatus.NA.gettransactionstatus()
				+ " and rm.ncontrolcode = " + Enumeration.TransactionStatus.NA.gettransactionstatus()
				+ " and rm.nsectioncode = " + nsectionCode;

		if ((int) inputMap.get("ncoareporttypecode") == Enumeration.COAReportType.PROJECTWISE.getcoaReportType()
				&& (int) inputMap.get("isSMTLFlow") == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
			stable += ",reportinfoproject rip";
			scondition += " and rip.nreporttemplatecode=rm.nreporttemplatecode and rip.nprojectmastercode="
					+ (int) inputMap.get("nprojectcode") + " and rip.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		}

//		ALPD-4799 janakumar
//		Release & Report ---> In report designer screen, inactive the current coa report and try to do release, 500 occurs

		final String getMainReports = " select rd.nreportdetailcode,rd.ssystemfilename,rm.* from reportmaster rm,reportdetails rd,  reporttemplate rtt,reporttype rt ,coareporttype coa,coaparent cp "
				+ stable + " "
				+ " where rtt.nreporttemplatecode = rm.nreporttemplatecode and cp.nreporttemplatecode = rtt.nreporttemplatecode and coa.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "   and rtt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and rm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rt.nreporttypecode=rm.nreporttypecode and coa.ncoareporttypecode=rm.ncoareporttypecode"
				+ " and rm.nreportcode=rd.nreportcode and cp.ncoaparentcode in("
				+ inputMap.get("ncoaparentcode").toString() + ") and  cp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rm.ntransactionstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rm.nsitecode="
				+ userInfo.getNmastersitecode() + " and rd.nsitecode=" + userInfo.getNmastersitecode()
				+ " and cp.nsitecode=" + userInfo.getNtranssitecode() + conditionString + scondition + " limit 1";
		ReportDetails mainReport = (ReportDetails) jdbcUtilityFunction.queryForObject(getMainReports,
				ReportDetails.class, jdbcTemplate);

		final String activeQuery = "select sreportno, ntransactionstatus from coaparent where ncoaparentcode="
				+ inputMap.get("ncoaparentcode") + " " + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNtranssitecode();

		final COAParent coaParentObj = (COAParent) jdbcUtilityFunction.queryForObject(activeQuery, COAParent.class,
				jdbcTemplate);
		returnMap.put("nreporttranscode", coaParentObj.getNtransactionstatus());
		inputMap.put("reportNo", coaParentObj.getSreportno());
		returnMap.put("sreportno", coaParentObj.getSreportno());

		// ATE234 janakumar ALPD-5116 Release&Report ->View the report in the new tab
		final String getFileuploadpath = "select ssettingvalue from reportsettings where" + " nreportsettingcode in ("
				+ Enumeration.ReportSettings.REPORTLINKPATH.getNreportsettingcode() + ","
				+ Enumeration.ReportSettings.MRT_REPORT_JSON_PATH.getNreportsettingcode()
				+ ") order by nreportsettingcode ";

		final List<String> reportPathsValues = jdbcTemplate.queryForList(getFileuploadpath, String.class);

		returnMap.put("sreportlink", reportPathsValues.get(0));
		returnMap.put("smrttemplatelink", reportPathsValues.get(1));

		if (mainReport != null) {
			// ALPD-5251 Changed filename and foldername from outside condition to inside
			// condition by Vishakh
			returnMap.put("FILEName", mainReport.getSsystemfilename());
			returnMap.put("folderName", mainReport.getSreportname());

			inputMap.put("nreportdetailcode", mainReport.getNreportdetailcode());
			if (inputMap.get("action").equals("Regenerate")) {

				final Map<String, Object> regenerateRtnValue = regenerateReport(inputMap, userInfo);

				if (regenerateRtnValue.get(Enumeration.ReturnStatus.RETURNSTRING
						.getreturnstatus()) == Enumeration.ReturnStatus.FAILED.getreturnstatus()) {
					returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), commonFunction
							.getMultilingualMessage("IDS_PLEASESELECTRELEASEDRECORD", userInfo.getSlanguagefilename()));

					return new ResponseEntity<>(returnMap, HttpStatus.OK);
				}
			} else if (inputMap.get("action").equals("preliminary")) {

				returnMap.putAll(preliminaryReport(inputMap, userInfo));

				returnMap.putAll((Map<String, Object>) getReleaseSample(inputMap, userInfo).getBody());

				// ALPD-4238
				returnMap.put("ncoaparentcode", inputMap.get("ncoaparentcode"));
				inputMap.put("ncoaparentcode", Integer.parseInt(inputMap.get("ncoaparentcode").toString()));

				if (returnMap.containsKey("sreportno")) {
					coaParentObj.setSreportno(returnMap.get("sreportno").toString());
					returnMap.put("sreportno", returnMap.get("sreportno").toString());
				}
			}
		} else {
			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), commonFunction
					.getMultilingualMessage("IDS_REPORTTEMPLATEISNOTFOUND", userInfo.getSlanguagefilename()));
			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		}

		final Map<String, Object> returnValue = new HashMap<>();

		returnMap.put("sreportno", coaParentObj.getSreportno());
		returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
				Enumeration.ReturnStatus.SUCCESS.getreturnstatus());

		returnValue.putAll(returnMap);
		returnValue.put("returnMap", returnMap);

		return new ResponseEntity<Map<String, Object>>(returnValue, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Map<String, Object>> reportGeneration(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		final Map<String, Object> mailMap = new HashMap<String, Object>();
		final UserInfo mailUserInfo = new UserInfo(userInfo);

		int qType = 1;
		int nreportDetailCode = -1;
		int naccredit = Enumeration.TransactionStatus.NO.gettransactionstatus();
		String systemFileName = "";
		String reportPath = "";
		String folderName = "";
		String subReportPath = "";
		String imagePath = "";
		String pdfPath = "";
		String sfileName = "";
		String sJRXMLname = "";
		String sreportingtoolURL = "";
		String ssignfilename = "";
		String scertfilename = "";
		String ssecuritykey = "";
		String sreportno = "";
		int nreportTransStatus = inputMap.containsKey("nreporttranscode")
				? Integer.parseInt(inputMap.get("nreporttranscode").toString())
				: -1;
		Map<String, Object> returnMap = new HashMap<String, Object>();

		if (inputMap.containsKey("sreportno")) {
			sreportno = (String) inputMap.get("sreportno");
		}

		mailMap.put("ncontrolcode", inputMap.get("ncontrolcode"));
		mailMap.put("ncoaparentcode", inputMap.get("ncoaparentcode"));
		// ALPD-5619 - added by Gowtham R on 28/03/2025 - Mail alert transaction > NULL
		// displayed in reference no column.
		mailMap.put("ssystemid", sreportno);
		inputMap.put("ncoaparentcode", Integer.parseInt(inputMap.get("ncoaparentcode").toString()));

		final String sReportQuery = "select ssettingvalue from settings where nsettingcode = 29 and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final Settings objReportsettings = (Settings) jdbcTemplate.queryForObject(sReportQuery, new Settings());

		if (objReportsettings.getSsettingvalue()
				.equals(String.valueOf(Enumeration.TransactionStatus.YES.gettransactionstatus()))) {
			reportTemplateDAO.writeJSONTemplate(inputMap, userInfo);
			returnMap.putAll(getDigitalSignatureDetail(inputMap, userInfo));
			ssignfilename = (String) returnMap.get("ssignfilename");
			scertfilename = (String) returnMap.get("scertfilename");
			ssecuritykey = (String) returnMap.get("ssecuritykey");
		} else {
			String sRootDir = ftpUtilityFunction.getFTPFileWritingPath("", null);

			String testAttachmentQuery = "select coalesce(jsondata->>'ssystemfilename','') ssystemfilename from releasetestattachment where ncoaparentcode = "
					+ inputMap.get("ncoaparentcode") + " and (jsondata->'nneedreport'):: INT = "
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNtranssitecode() + "";
			List<Map<String, Object>> lstSysFile = jdbcTemplate.queryForList(testAttachmentQuery);

			File oSysFile = null;
			String sSysFile = "";

			for (int nlen = 0; nlen < lstSysFile.size(); nlen++) {
				sSysFile = (String) lstSysFile.get(nlen).get("ssystemfilename");
				oSysFile = new File(sRootDir + sSysFile);
				if (!oSysFile.exists()) {
					ftpUtilityFunction.FileViewUsingFtp(oSysFile.getName(), 0, userInfo, sRootDir, "");
				}
			}
		}

		final String getReportPaths = "select nreportsettingcode, ssettingvalue from reportsettings where nreportsettingcode in ("
				+ Enumeration.ReportSettings.REPORT_PATH.getNreportsettingcode() + ","
				+ Enumeration.ReportSettings.REPORT_PDF_PATH.getNreportsettingcode() + ","
				+ Enumeration.ReportSettings.REPORTINGTOOL_URL.getNreportsettingcode() + ")" + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nreportsettingcode ";
		final List<ReportSettings> lstSettingValue = jdbcTemplate.query(getReportPaths, new ReportSettings());

		final Map<Short, String> mapSeqno = lstSettingValue.stream().collect(Collectors
				.toMap(ReportSettings::getNreportsettingcode, Settings -> (String) Settings.getSsettingvalue()));

		reportPath = mapSeqno.get((short) Enumeration.ReportSettings.REPORT_PATH.getNreportsettingcode()).toString();
		subReportPath = mapSeqno.get((short) Enumeration.ReportSettings.REPORT_PATH.getNreportsettingcode()).toString();
		imagePath = mapSeqno.get((short) Enumeration.ReportSettings.REPORT_PATH.getNreportsettingcode()).toString();
		pdfPath = mapSeqno.get((short) Enumeration.ReportSettings.REPORT_PDF_PATH.getNreportsettingcode()).toString();
		sreportingtoolURL = mapSeqno.get((short) Enumeration.ReportSettings.REPORTINGTOOL_URL.getNreportsettingcode())
				.toString();

		inputMap.put("subReportPath", subReportPath);
		inputMap.put("folderName", folderName);
		inputMap.put("imagePath", imagePath);
		inputMap.put("naccredit", naccredit);
		inputMap.put("sreportingtoolURL", sreportingtoolURL);
		inputMap.put("pdfPath", pdfPath);
		inputMap.put("reportPath", reportPath);

		final Map<String, Object> dynamicReport = reportDAOSupport.getDynamicReports(inputMap, userInfo);

		if (((String) dynamicReport.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))
				.equals(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())) {

			sJRXMLname = (String) dynamicReport.get("JRXMLname");
			nreportDetailCode = (int) dynamicReport.get("nreportdetailcode");
			folderName = (String) dynamicReport.get("folderName");
			qType = (int) dynamicReport.get("qType");
			reportPath = reportPath + folderName;
			inputMap.put("qType", qType);
			inputMap.put("folderName", folderName);
			inputMap.put("nreportDetailCode", nreportDetailCode);
			inputMap.put("sJRXMLname", sJRXMLname);
		}
		reportPath = reportPath + sJRXMLname;
		final File JRXMLFile = new File(reportPath);

		if (sJRXMLname != null && !sJRXMLname.equals("") && JRXMLFile.exists()) {

			if (inputMap.get("action").equals("Regenerate")) {
				final String reportHistoryQuery = "select * from coareporthistory where ncoareporthistorycode ="
						+ " (select max(ncoareporthistorycode) from coareporthistory where " + " ncoaparentcode = "
						+ inputMap.get("ncoaparentcode") + " and " + "	nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
						+ userInfo.getNtranssitecode();

				final COAReportHistory reportHistoryObj = (COAReportHistory) jdbcUtilityFunction
						.queryForObject(reportHistoryQuery, COAReportHistory.class, jdbcTemplate);
				systemFileName = reportHistoryObj.getSsystemfilename();
			} else {
				final String currentDateTime = dateUtilityFunction.getCurrentDateTime(userInfo).toString()
						.replace("T", " ").replace("Z", "");
				final String concat = String.valueOf(userInfo.getNtranssitecode()) + currentDateTime;

				systemFileName = inputMap.containsKey("systemFileName") && inputMap.get("systemFileName") != ""
						? (String) inputMap.get("systemFileName")
						: systemFileName.isEmpty()
								? UUID.nameUUIDFromBytes(concat.getBytes()).toString() + "_"
										+ inputMap.get("ncoaparentcode") + ".pdf"
								: systemFileName;
			}
			inputMap.put("systemFileName", systemFileName);
			inputMap.put("JRXMLFile", JRXMLFile);
			inputMap.put("qType", qType);
			inputMap.put("pdfPath", pdfPath);
			inputMap.put("sfileName", sfileName);
			inputMap.put("ssignfilename", ssignfilename);
			inputMap.put("scertfilename", scertfilename);
			inputMap.put("ssecuritykey", ssecuritykey);
			inputMap.put("sreportno", sreportno);
			inputMap.put("nreportdetailcode", dynamicReport.get("nreportdetailcode"));

			// Added by L.Subashini 07-12-2024 - settings 70, 64 - Start

			final String settingsQry = "select ssettingvalue from settings where" + " nsettingcode in ("
					+ Enumeration.Settings.COAREPORT_GENERATION.getNsettingcode() + ","
					+ Enumeration.Settings.REPORT_OPEN_NEW_TAB.getNsettingcode() + ") order by nsettingcode asc";

			final List<String> settingsDataList = jdbcTemplate.queryForList(settingsQry, String.class);

			Integer openReportNewTab = null;
			Integer schedulerBasedReport = null;

			if (!settingsDataList.isEmpty()) {
				schedulerBasedReport = Integer.parseInt(settingsDataList.get(0));
				openReportNewTab = Integer.parseInt(settingsDataList.get(1));
			}

			inputMap.put("openReportNewTab", openReportNewTab);
			inputMap.put("schedulerBasedReport", schedulerBasedReport);

			returnMap.putAll(generateCOAReport(inputMap, userInfo));

			if (openReportNewTab != null
					&& openReportNewTab == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
				if (schedulerBasedReport != null
						&& schedulerBasedReport == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
					final String draftToReleased = "UPDATE coareporthistorygeneration SET " + "nreportstatus = "
							+ Enumeration.TransactionStatus.RELEASED.gettransactionstatus() + " "
							+ "WHERE ncoareporthistorycode = " + inputMap.get("ncoareporthistorycode") + " "
							+ "AND nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and nsitecode=" + userInfo.getNtranssitecode() + " ;";

					jdbcTemplate.execute(draftToReleased);
				}
			}

			// L.Subashini 07-12-2024 - Start - settings 70, 64 -End

			if ((int) inputMap.get("nsampletypecode") == Enumeration.SampleType.CLINICALSPEC.getType()) {
				if (nreportTransStatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
					ResponseEntity<Object> mailResponse = emailDAOSupport.createEmailAlertTransaction(mailMap,
							mailUserInfo);

					if (mailResponse != null) {
						returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), mailResponse.getBody());
					}
				}
			}
			returnMap.putAll((Map<String, Object>) inputMap.get("returnMap"));

			return new ResponseEntity<Map<String, Object>>(returnMap, HttpStatus.OK);
		} else {

			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), commonFunction
					.getMultilingualMessage("IDS_REPORTTEMPLATEISNOTFOUND", userInfo.getSlanguagefilename()));

			return new ResponseEntity<Map<String, Object>>(returnMap, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> DeleteApprovedSamples(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		Map<String, Object> returnMap = new HashMap<>();
		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> objlst = new ArrayList<Map<String, Object>>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditNew = new JSONObject();
		boolean validation;
		int ncoaparentcode = inputMap.containsKey("ncoaparentcode") ? (int) inputMap.get("ncoaparentcode") : 0;
		String ntransactiontestcode = (String) inputMap.get("ntransactiontestcode");

		final String sLockQuery = " lock table lockrelease " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus() + "";
		jdbcTemplate.execute(sLockQuery);

		String validateSample = "select ntransactiontestcode from coachild where ncoaparentcode=" + ncoaparentcode + " "
				+ "	and ntransactiontestcode in (" + ntransactiontestcode + ") and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNtranssitecode() + " ";

		List<String> nonReleasedSamples = jdbcTemplate.queryForList(validateSample, String.class);

		if (nonReleasedSamples.size() != 0) {
			validation = jdbcTemplate.queryForObject(
					" select(select count(*) from coachild where ncoaparentcode=" + ncoaparentcode + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and nsitecode="
							+ userInfo.getNtranssitecode() + ") =(select count(*) from coachild where ncoaparentcode="
							+ ncoaparentcode + " and npreregno in (" + inputMap.get("npreregno") + ") and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
							+ userInfo.getNtranssitecode() + ");",
					Boolean.class);

			inputMap.put("npreregno", inputMap.get("npreregno").toString());
			String clickedTestCode = "";
			String ntransactionStatusValues = "";
			if ((int) inputMap.get("ncoaparenttranscode") == Enumeration.TransactionStatus.CORRECTION
					.gettransactionstatus()) {

				ntransactionStatusValues = Enumeration.TransactionStatus.RELEASED.gettransactionstatus() + ", "
						+ inputMap.get("ntransactionstatus");
			} else {

				ntransactionStatusValues = inputMap.get("ntransactionstatus").toString();
			}

			clickedTestCode = " select ntransactiontestcode from registrationtesthistory where ntesthistorycode in ("
					+ " select max(ntesthistorycode) from registrationtesthistory where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by ntransactiontestcode) "
					+ " and npreregno in (" + inputMap.get("npreregno") + ") and ntransactionstatus in ("
					+ ntransactionStatusValues + ")  and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNtranssitecode();

			List<String> lstClickedTestCode = jdbcTemplate.queryForList(clickedTestCode, String.class);

			final String ntransactionTestCode = lstClickedTestCode.stream().map(i -> i.toString())
					.collect(Collectors.joining(", "));

			if (validation == true) {
				returnMap.put("rtn", commonFunction.getMultilingualMessage("IDS_ANYONESAMPLESAREQUIRED",
						userInfo.getSlanguagefilename()));

				return new ResponseEntity<>(returnMap, HttpStatus.OK);
			} else {

				final String sQuery = " update coachild set nstatus="
						+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " where ncoaparentcode="
						+ inputMap.get("ncoaparentcode") + " and npreregno in (" + inputMap.get("npreregno") + ")"
						+ " and nsitecode = " + userInfo.getNtranssitecode();
				jdbcTemplate.execute(sQuery);

				final String squery = " update reportinforelease set nstatus="
						+ Enumeration.TransactionStatus.NA.gettransactionstatus()
						+ " where reportinforelease.nparentmastercode  not in ( select max(ntestcode) from  coaparent cp,coachild cc,registrationtest rt where rt.ntransactiontestcode=cc.ntransactiontestcode and "
						+ " cp.ncoaparentcode=cc.ncoaparentcode and cc.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cp.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cp.ncoaparentcode="
						+ inputMap.get("ncoaparentcode") + " and cp.nsitecode = " + userInfo.getNtranssitecode()
						+ " and cc.nsitecode = " + userInfo.getNtranssitecode() + " and rt.nsitecode = "
						+ userInfo.getNtranssitecode()
						+ " group by rt.ntestcode) and reportinforelease.nformcode=41 and ncoaparentcode="
						+ inputMap.get("ncoaparentcode") + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
						+ userInfo.getNtranssitecode();
				jdbcTemplate.execute(squery);

				String squeryReleaseTestAttachmentComment = " update releasetestattachment set nstatus="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dtransactiondate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', ntransdatetimezonecode="
						+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ " where ncoaparentcode in (" + inputMap.get("ncoaparentcode") + ") and npreregno in ("
						+ inputMap.get("npreregno") + ") and nsitecode = " + userInfo.getNtranssitecode() + ";";

				squeryReleaseTestAttachmentComment = squeryReleaseTestAttachmentComment
						+ "update releasetestcomment set nstatus="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dtransactiondate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', ntransdatetimezonecode="
						+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ " where ncoaparentcode in (" + inputMap.get("ncoaparentcode") + ") and npreregno in ("
						+ inputMap.get("npreregno") + ") and nsitecode = " + userInfo.getNtranssitecode() + ";";
				jdbcTemplate.execute(squeryReleaseTestAttachmentComment);
			}

			if ((int) inputMap.get("ncoaparenttranscode") == Enumeration.TransactionStatus.CORRECTION
					.gettransactionstatus()) {
				deleteSamplesAfterCorrection(inputMap, userInfo);
			}

			inputMap.put("ntransactiontestcode", ntransactionTestCode.toString());
			inputMap.put("AuditStatus", "AuditStatus");

			Map<String, Object> lstDataTestNew = getReleaseAuditGet(inputMap, userInfo);

			jsonAuditNew.put("registrationtest",
					(List<Map<String, Object>>) ((Map<String, Object>) lstDataTestNew).get("ReleaseTestAudit"));
			auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
			auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
			auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
			actionType.put("registrationtest", "IDS_REMOVESAMPLES");

			auditUtilityFunction.fnJsonArrayAudit(jsonAuditNew, null, actionType, auditmap, false, userInfo);

			returnMap.putAll((Map<String, Object>) getReleaseSample(inputMap, userInfo).getBody());
			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
			returnMap.put("PortalStatus", objlst);

			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		} else {
			returnMap.put("rtn",
					commonFunction.getMultilingualMessage("IDS_ALREADYDELETED", userInfo.getSlanguagefilename()));

			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		}

	}

	@Override
	public ResponseEntity<Object> UpdateApprovedSamples(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> objlst = new ArrayList<Map<String, Object>>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditNew = new JSONObject();
		String strCoaChild = "";
		String ntransactionTestCode = "";
		List<String> lstnpreregno = Arrays.asList(inputMap.get("npreregno").toString().split(","));
		String ntransactiontestcode = (String) inputMap.get("ntransactiontestcode");

		final String sLockQuery = " lock table lockrelease " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus() + "";
		jdbcTemplate.execute(sLockQuery);

		String validateSample = "select ntransactiontestcode from  coachild  where ntransactiontestcode in ("
				+ ntransactiontestcode + ") and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode=" + userInfo.getNtranssitecode();

		List<String> nonReleasedSamples = jdbcTemplate.queryForList(validateSample, String.class);

		if (nonReleasedSamples.size() == 0) {
			int projectStatus = Enumeration.TransactionStatus.FAILED.gettransactionstatus();
			Map<String, Object> outputProjectMap = new HashMap<>();
			// Added by Dhanushya RI jira id-ALPD-3862 for multiple projects under single
			// release
			if (inputMap.containsKey("nprojectspecrequired")
					&& (int) inputMap.get("nprojectspecrequired") == Enumeration.TransactionStatus.YES
							.gettransactionstatus()
					&& inputMap.containsKey("nismultipleproject")
					&& (int) inputMap.get("nismultipleproject") == Enumeration.TransactionStatus.NO
							.gettransactionstatus()) {
				outputProjectMap = getProjectBasedAlert(inputMap, userInfo);
				projectStatus = (int) outputProjectMap.get("projectStatus");
			} else {
				projectStatus = Enumeration.TransactionStatus.SUCCESS.gettransactionstatus();
			}
			if (projectStatus == Enumeration.TransactionStatus.SUCCESS.gettransactionstatus()) {
				int projectTempStatus = Enumeration.TransactionStatus.FAILED.gettransactionstatus();
				Map<String, Object> outputProjectTempMap = new HashMap<>();

				if (inputMap.containsKey("nsampletypecode")
						&& (int) inputMap.get("nsampletypecode") == Enumeration.SampleType.PROJECTSAMPLETYPE
								.getType()) {
					outputProjectTempMap = getProjectTemplateBasedAlert(inputMap, userInfo);
					projectTempStatus = (int) outputProjectTempMap.get("projectTempStatus");

				} else {
					projectTempStatus = Enumeration.TransactionStatus.SUCCESS.gettransactionstatus();
				}
				if (projectTempStatus == Enumeration.TransactionStatus.SUCCESS.gettransactionstatus()) {

					final COAParent objCoaParent = getActiveCOAParentById(inputMap, userInfo);

					// Added by sonia for ALPD-4122 on 17-05-2024 sample count validation

					final String sQuery = " select npreregno from coachild  where ncoaparentcode ="
							+ objCoaParent.getNcoaparentcode() + " and nsitecode =" + userInfo.getNtranssitecode()
							+ " and nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " group by npreregno ";

					final List<Map<String, Object>> coaParent = jdbcTemplate.queryForList(sQuery);
					final int sampleCount = coaParent.size();

					final String preRegNoCount = (String) inputMap.get("npreregno");
					final String[] parts = preRegNoCount.split(",");
					final int selectionCount = parts.length;

					final int reportTemplateCode = (int) objCoaParent.getNreporttemplatecode();
					final List<Map<String, Object>> reportTemplateList = getReportTemplateById(reportTemplateCode,
							userInfo);
					final int maxSampleCount = (int) reportTemplateList.get(0).get("nmaxsamplecount");

					final int totalCount = sampleCount + selectionCount;

					if (totalCount <= maxSampleCount || maxSampleCount == 0) {
						for (int i = 0; i < lstnpreregno.size(); i++) {
							String stestcode = " select ntransactiontestcode from registrationtesthistory where ntesthistorycode in ("
									+ " select max(ntesthistorycode) from registrationtesthistory where nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
									+ userInfo.getNtranssitecode() + " group by ntransactiontestcode) "
									+ " and npreregno in (" + lstnpreregno.get(i) + ") and ntransactionstatus ="
									+ inputMap.get("ntransactionstatus") + " "
									+ " and ntransactiontestcode not in (select ntransactiontestcode from coachild where npreregno in ("
									+ lstnpreregno.get(i) + ") and nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
									+ userInfo.getNtranssitecode() + ") and nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
									+ userInfo.getNtranssitecode() + " ";

							List<String> ntestcount = jdbcTemplate.queryForList(stestcode, String.class);
							ntransactionTestCode = ntestcount.stream().map(x -> x.toString())
									.collect(Collectors.joining(", "));

							String coaReportHistorySeq = "select nsequenceno from seqnoreleasemanagement where stablename='coachild'"
									+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ "";
							int historySeqNo = jdbcTemplate.queryForObject(coaReportHistorySeq, Integer.class);
							historySeqNo = historySeqNo + 1;

							strCoaChild = strCoaChild + "(select " + historySeqNo
									+ " +rank()over(order by rt.ntransactiontestcode) ncoachildcode,"
									+ inputMap.get("ncoaparentcode") + " ncoaparentcode,"
									+ " rt.npreregno,rt.ntransactionsamplecode,rt.ntransactiontestcode," + " "
									+ userInfo.getNusercode() + " nusercode," + userInfo.getNuserrole()
									+ " nuserrolecode ," + " '" + dateUtilityFunction.getCurrentDateTime(userInfo)
									+ "' ::timestamp without time zone dmodifieddate," + userInfo.getNtranssitecode()
									+ " nsitecode," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " nstatus " + " from  registrationtest rt where rt.ntransactiontestcode in ("
									+ stestcode + ") and rt.nsitecode = " + userInfo.getNtranssitecode()
									+ " order by rt.ntransactiontestcode) union all";

							int nTestcount = ntestcount.size();
							int coachild = (historySeqNo) + nTestcount;

							String releaseHistorySeqnoUpdate = "update seqnoreleasemanagement set nsequenceno="
									+ coachild + " where stablename='coachild' and nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
							jdbcTemplate.execute(releaseHistorySeqnoUpdate);
						}
						String sCoaQuery = "";

						sCoaQuery = sCoaQuery
								+ "insert into coachild(ncoachildcode, ncoaparentcode, npreregno, ntransactionsamplecode, ntransactiontestcode,"
								+ " nusercode, nuserrolecode, dmodifieddate, nsitecode, nstatus)  "
								+ strCoaChild.substring(0, strCoaChild.length() - 9) + ";";

						jdbcTemplate.execute(sCoaQuery);

						insertReleaseTestAttachment(inputMap, userInfo);
						insertReleaseTestComment(inputMap, userInfo);

						inputMap.put("npreregno", inputMap.get("npreregno").toString());
						inputMap.put("ntransactiontestcode", ntransactionTestCode.toString());
						inputMap.put("AuditStatus", "AuditStatus");

						Map<String, Object> lstDataTestNew = getReleaseAuditGet(inputMap, userInfo);

						jsonAuditNew.put("registrationtest",
								(List<Map<String, Object>>) ((Map<String, Object>) lstDataTestNew)
										.get("ReleaseTestAudit"));
						auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
						auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
						auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
						actionType.put("registrationtest", "IDS_APPENDSAMPLES");

						auditUtilityFunction.fnJsonArrayAudit(jsonAuditNew, null, actionType, auditmap, false,
								userInfo);

						inputMap.put("npreregno", "0");
						returnMap.putAll((Map<String, Object>) getReleaseSample(inputMap, userInfo).getBody());
						returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
								Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
						returnMap.put("PortalStatus", objlst);

						return new ResponseEntity<>(returnMap, HttpStatus.OK);
					} else {
						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage("IDS_REPORTSAMPLESELECTIONEXCEED",
										userInfo.getSlanguagefilename()),
								HttpStatus.CONFLICT);

					}
				} else {
					returnMap.put("rtn", commonFunction.getMultilingualMessage("IDS_SELECTSAMETEMPLATE",
							userInfo.getSlanguagefilename()));

					return new ResponseEntity<>(returnMap, HttpStatus.OK);

				}
			} else {
				returnMap.put("rtn", commonFunction.getMultilingualMessage("IDS_SELECTSAMEPROJECT",
						userInfo.getSlanguagefilename()));

				return new ResponseEntity<>(returnMap, HttpStatus.OK);
			}
		} else {
			returnMap.put("rtn",
					commonFunction.getMultilingualMessage("IDS_TESTALREADYADDED", userInfo.getSlanguagefilename()));

			return new ResponseEntity<>(returnMap, HttpStatus.OK);

		}

	}

	@Override
	public ResponseEntity<Object> getStatusAlert(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		String validation = "";

		int ncoaparentcode = inputMap.containsKey("ncoaparentcode") ? (int) inputMap.get("ncoaparentcode") : 0;

		String activeQuery = "select sreportno from coaparent where ncoaparentcode=" + ncoaparentcode + " "
				+ " and ntransactionstatus in (" + Enumeration.TransactionStatus.RELEASED.gettransactionstatus() + ", "
				+ Enumeration.TransactionStatus.PRELIMINARYRELEASE.gettransactionstatus() + ") " + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNtranssitecode();

		COAParent isActiveStatus = (COAParent) jdbcUtilityFunction.queryForObject(activeQuery, COAParent.class,
				jdbcTemplate);
		if (isActiveStatus == null) {
			returnMap.put("Status", Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		} else {
			returnMap.put("Status", Enumeration.ReturnStatus.FAILED.getreturnstatus());
			validation = "IDS_SELECTDRAFTCORRECTEDRECORD";
			returnMap.put("ValidationStatus", validation);
		}
		return new ResponseEntity<>(returnMap, HttpStatus.OK);

	}

	public Map<String, Object> getReleaseAuditGet(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		Map<String, Object> returnMap = new HashMap<>();
		ObjectMapper objmapper = new ObjectMapper();
		List<Map<String, Object>> testList = new ArrayList<Map<String, Object>>();
		String sQuery = "";

		if (inputMap.containsKey("AuditStatus") && inputMap.get("AuditStatus").equals("AuditStatus")) {
			sQuery = "select json_agg(rt.jsondata||json_build_object('sreportno','-',"
					+ " 'stransteststatus',coalesce(ts.jsondata->'stransdisplaystatus'->>' "
					+ userInfo.getSlanguagetypecode() + "'," + " ts.jsondata->'stransdisplaystatus'->>'en-US'), "
					+ " 'sarno',rar.sarno,'ssamplearno',sar.ssamplearno )::jsonb) "
					+ " from registrationtest rt,registrationsamplearno sar," + " registrationarno rar ,"
					+ " coaparent cp,transactionstatus ts " + " where rt.npreregno=sar.npreregno"
					+ " and cp.ntransactionstatus=ts.ntranscode " + " and rt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and sar.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and rar.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and cp.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and ts.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and cp.ncoaparentcode in ("
					+ inputMap.get("ncoaparentcode") + ") and rt.nsitecode = " + userInfo.getNtranssitecode()
					+ " and rar.nsitecode = " + userInfo.getNtranssitecode() + " and sar.nsitecode = "
					+ userInfo.getNtranssitecode() + " and rar.npreregno=sar.npreregno and rt.npreregno in ("
					+ inputMap.get("npreregno") + ")" + " and rt.ntransactiontestcode in ("
					+ inputMap.get("ntransactiontestcode") + ")" + " and cp.nsitecode=" + userInfo.getNtranssitecode();
		} else {
			sQuery = "select json_agg(rt.jsondata||json_build_object('sreportno',cp.sreportno,"
					+ " 'stransdisplaystatus',coalesce(ts.jsondata->'stransdisplaystatus'->>' "
					+ userInfo.getSlanguagetypecode() + "'," + " ts.jsondata->'stransdisplaystatus'->>'en-US'), "
					+ " 'sarno',rar.sarno,'ssamplearno',sar.ssamplearno)::jsonb) "
					+ " from registrationtest rt,registrationsamplearno sar," + " registrationarno rar ,"
					+ " coaparent cp,transactionstatus ts,registrationtesthistory rth "
					+ " where rt.npreregno=sar.npreregno" + " and rth.ntransactionstatus=ts.ntranscode "
					+ "	and rt.ntransactiontestcode=rth.ntransactiontestcode "
					+ " and rth.ntesthistorycode = any(select max(ntesthistorycode) from registrationtesthistory "
					+ " where ntransactiontestcode in (" + inputMap.get("ntransactiontestcode") + ") and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by ntransactiontestcode)"
					+ " and rt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and sar.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and rar.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and cp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and ts.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and rth.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and rt.nsitecode = " + userInfo.getNtranssitecode() + " and rar.nsitecode = "
					+ userInfo.getNtranssitecode() + " and sar.nsitecode = " + userInfo.getNtranssitecode()
					+ "  and rth.nsitecode = " + userInfo.getNtranssitecode() + " and cp.ncoaparentcode in ("
					+ inputMap.get("ncoaparentcode") + ") " + " and rar.npreregno=sar.npreregno and rt.npreregno in ("
					+ inputMap.get("npreregno") + ")" + " and rt.ntransactiontestcode in ("
					+ inputMap.get("ntransactiontestcode") + ")" + " and cp.nsitecode=" + userInfo.getNtranssitecode();

		}

		String testListString = jdbcTemplate.queryForObject(sQuery, String.class);

		if (testListString != null) {
			testList = objmapper.readValue(testListString, new TypeReference<List<Map<String, Object>>>() {
			});
			returnMap.put("ReleaseTestAudit", testList);
		} else {
			returnMap.put("ReleaseTestAudit", testList);
		}
		return returnMap;
	}

	@Override
	public ResponseEntity<Map<String, Object>> getSection(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		Map<String, Object> returnMap = new HashMap<>();
		String sQuery = "";
		String transactiontestcode = "";
		if (inputMap.containsKey("nneededit")) {

			sQuery = "select ntransactiontestcode from coachild where ncoaparentcode=" + inputMap.get("ncoaparentcode")
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode = " + userInfo.getNtranssitecode();
			List<String> testList = jdbcTemplate.queryForList(sQuery, String.class);
			transactiontestcode = testList.stream().collect(Collectors.joining(","));
		}

		String projectQuery = (int) inputMap.get("nsampletypecode") == Enumeration.SampleType.PROJECTSAMPLETYPE
				.getType() ? "" : "";

		String userSectionQuery = "select s.nsectioncode,s.ssectionname from section s,registration r,registrationtest rt "
				+ " where s.nsectioncode = rt.nsectioncode " + " and rt.npreregno= r.npreregno "
				+ " and r.nregtypecode = " + inputMap.get("nregtypecode") + " and r.nsampletypecode = "
				+ inputMap.get("nsampletypecode") + " and r.nregsubtypecode = " + inputMap.get("nregsubtypecode")
				+ projectQuery + " and rt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and r.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and r.nsitecode="
				+ userInfo.getNtranssitecode() + " and rt.nsitecode=" + userInfo.getNtranssitecode()
				+ " and s.nsitecode=" + userInfo.getNmastersitecode();

		if (inputMap.containsKey("nneededit")) {
			userSectionQuery = userSectionQuery + " and rt.ntransactiontestcode in (" + transactiontestcode
					+ ") group by s.nsectioncode";
		} else {
			userSectionQuery = userSectionQuery + " group by s.nsectioncode";

		}

		List<Section> sectionList = jdbcTemplate.query(userSectionQuery, new Section());
		returnMap.put("sectionList", sectionList);

		if (sectionList.isEmpty()) {
			returnMap.put("sectionList", sectionList);
		} else {
			returnMap.put("sectionList", sectionList);
			returnMap.put("nsectioncode", sectionList.get(0).getNsectioncode());

		}
		return new ResponseEntity<>(returnMap, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> getreportcomments(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		Map<String, Object> returnMap = new HashMap<>();

		String reportInfoProject = " select case rir.nformcode when " + Enumeration.FormCode.TESTMASTER.getFormCode()
				+ " then (select stestsynonym from testmaster "
				+ " where ntestcode=rir.nparentmastercode) else (select sprojecttitle from projectmaster "
				+ "where nprojectmastercode=rir.nparentmastercode) end as testname,rir.nreportinforeleasecode,"
				+ "rir.ncoaparentcode,rir.nformcode,rir.stablename,rir.sprimarykeyname,rir.nparentmastercode,"
				+ "rir.sreportfieldname,rir.sreportfieldvalue " + " from reportinforelease rir where ncoaparentcode="
				+ inputMap.get("ncoaparentcode") + " and rir.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rir.nsitecode = "
				+ userInfo.getNtranssitecode()
				+ " and rir.sreportfieldname not in ('nreporttemplatecode') order by  nreportinforeleasecode";
		// ALPD-5566 Added condition to remove nreporttemplatecode by Vishakh for Report
		// Comments Issue
		List<ReportInfoRelease> reportInfoProjectList = jdbcTemplate.query(reportInfoProject, new ReportInfoRelease());

		reportInfoProjectList = (List<ReportInfoRelease>) commonFunction.getMultilingualMessageList(
				reportInfoProjectList, Arrays.asList("sreportfieldname"), userInfo.getSlanguagefilename());
		returnMap.put("reportinforelease", reportInfoProjectList);

		return new ResponseEntity<Object>(returnMap, HttpStatus.OK);

	}

	@Override
	public ReportInfoRelease getActiveUnitById(int nunitCode, UserInfo userInfo) throws Exception {

		String strQuery = "select  rir.nreportinforeleasecode,rir.ncoaparentcode,rir.nformcode,rir.stablename,rir.sprimarykeyname,"
				+ " rir.nparentmastercode,rir.sreportfieldname,rir.sreportfieldvalue "
				+ " from reportinforelease rir where nreportinforeleasecode=" + nunitCode + "" + " and rir.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rir.nsitecode = "
				+ userInfo.getNtranssitecode() + "";

		return (ReportInfoRelease) jdbcUtilityFunction.queryForObject(strQuery, ReportInfoRelease.class, jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> updateReportComment(ReportInfoRelease selectedComment, UserInfo userInfo)
			throws Exception {

		Map<String, Object> returnMap = new HashMap<>();

		jdbcTemplate.execute("update reportinforelease set sreportfieldvalue='"
				+ stringUtilityFunction.replaceQuote(selectedComment.getSreportfieldvalue())
				+ "' where nreportinforeleasecode=" + selectedComment.getNreportinforeleasecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
				+ userInfo.getNtranssitecode() + "");

		String reportInfoProject = "select case rir.nformcode when " + Enumeration.FormCode.TESTMASTER.getFormCode()
				+ " "
				+ " then (select stestsynonym from testmaster where ntestcode=rir.nparentmastercode and nsitecode = "
				+ userInfo.getNmastersitecode() + ") else "
				+ " (select sprojecttitle from projectmaster where nprojectmastercode=rir.nparentmastercode and nsitecode = "
				+ userInfo.getNtranssitecode() + ") end as testname, "
				+ " rir.nreportinforeleasecode,rir.ncoaparentcode,rir.nformcode,rir.stablename,rir.sprimarykeyname,"
				+ " rir.nparentmastercode,rir.sreportfieldname,rir.sreportfieldvalue "
				+ " from reportinforelease rir where ncoaparentcode = " + selectedComment.getNcoaparentcode()
				+ " and rir.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rir.nsitecode = " + userInfo.getNtranssitecode()
				+ " and rir.sreportfieldname not in ('nreporttemplatecode') order by  nreportinforeleasecode";
		List<ReportInfoRelease> reportInfoProjectList = jdbcTemplate.query(reportInfoProject, new ReportInfoRelease());

		reportInfoProjectList = (List<ReportInfoRelease>) commonFunction.getMultilingualMessageList(
				reportInfoProjectList, Arrays.asList("sreportfieldname"), userInfo.getSlanguagefilename());

		returnMap.put("reportinforelease", reportInfoProjectList);

		return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> updateReleaseParameter(MultipartHttpServletRequest request, final UserInfo userInfo)
			throws Exception {

		final String lockquery = "lock lockresultcorrection" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(lockquery);

		final ObjectMapper objMapper = new ObjectMapper();

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
		int nregtypecode = Integer.parseInt(request.getParameter("nregtypecode"));
		int nregsubtypecode = Integer.parseInt(request.getParameter("nregsubtypecode"));
		int ndesigntemplatemappingcode = Integer.parseInt(request.getParameter("ndesigntemplatemappingcode"));
		int ncoaparentcode = Integer.parseInt(request.getParameter("ncoaparentcode"));
		int ntransactionresultcode = lstTransSampResults.get(0).getNtransactionresultcode();

		if (lstTransSampResults != null && !lstTransSampResults.isEmpty()) {

			query = " select rp.*,rp.jsondata->>'sfinal' sfinal ,rp.jsondata->>'sresult' sresult ,"
					+ " t.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
					+ "' as stransdisplaystatus," + " rp.jsondata->>'stestsynonym' stestsynonym,"
					+ " rp.jsondata->>'sparametersynonym' sparametersynonym,"
					+ " rp.jsondata->>'sresultaccuracyname' sresultaccuracyname,"
					+ " r.sarno,rs.ssamplearno,g.sgradename,u.sunitname,u.nunitcode "
					+ " from releaseparameter rp,transactionstatus t,registrationarno r,registrationsamplearno rs,"
					+ " registrationtest rt,grade g,unit u"
					+ " where t.ntranscode=rp.ntransactionstatus and r.npreregno=rp.npreregno and r.npreregno=rs.npreregno"
					+ " and rs.npreregno=rp.npreregno and rt.ntransactionsamplecode=rs.ntransactionsamplecode"
					+ " and rt.ntransactiontestcode=rp.ntransactiontestcode and u.nunitcode=rp.nunitcode "
					+ " and rt.npreregno=rs.npreregno and r.npreregno=rt.npreregno and g.ngradecode=rp.ngradecode "
					+ " and rs.nsitecode = " + userInfo.getNtranssitecode() + " and rp.nsitecode = "
					+ userInfo.getNtranssitecode() + " and r.nsitecode = " + userInfo.getNtranssitecode()
					+ " and u.nsitecode = " + userInfo.getNmastersitecode() + " and rp.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and t.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and r.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and rs.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and rt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and g.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and u.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and rp.ntransactionresultcode = " + ntransactionresultcode + " ";
			List<ReleaseParameter> lstSampleResults = jdbcTemplate.query(query, new ReleaseParameter());

			final String currentDateTime = dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ")
					.replace("Z", "");

			if (lstSampleResults != null && !lstSampleResults.isEmpty()) {

				Map<String, Object> jsonData = lstTransSampResults.get(0).getJsondata();
				String sfinal = StringEscapeUtils.unescapeJava(jsonData.get("sfinal").toString());
				String sresult = StringEscapeUtils.unescapeJava(jsonData.get("sresult").toString());
				jsonData.put("sfinal", sfinal);
				jsonData.put("sresult", sresult);
				jsonData.put("dentereddate", currentDateTime);
				jsonData.put("dentereddatetimezonecode", userInfo.getNtimezonecode());
				jsonData.put("noffsetdentereddate",
						dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()));

				updateQuery = updateQuery + "update releaseparameter set  ngradecode="
						+ lstTransSampResults.get(0).getNgradecode() + ", nenteredby= "
						+ lstTransSampResults.get(0).getNenteredby() + ", nenteredrole="
						+ lstTransSampResults.get(0).getNenteredrole() + ", ndeputyenteredby="
						+ userInfo.getNdeputyusercode() + ", ndeputyenteredrole=" + userInfo.getNdeputyuserrole()
						+ ", nattachmenttypecode = "
						+ (lstTransSampResults.get(0).getNparametertypecode() == 4 ? 1 : -1) + " ,"
						+ " jsondata = jsondata || '"
						+ stringUtilityFunction.replaceQuote(objMapper.writeValueAsString(jsonData)) + "'"
						+ " ,nunitcode=" + lstTransSampResults.get(0).getNunitcode()
						+ " where ntransactionresultcode = " + lstTransSampResults.get(0).getNtransactionresultcode()
						+ " and nsitecode = " + userInfo.getNtranssitecode() + ";";

				jdbcTemplate.execute(updateQuery);

				query = " select rp.*,rp.jsondata->>'sfinal' sfinal , t.jsondata->'stransdisplaystatus'->>'"
						+ userInfo.getSlanguagetypecode() + "' as stransdisplaystatus,"
						+ " rp.jsondata->>'stestsynonym' stestsynonym,"
						+ " rp.jsondata->>'sresultaccuracyname' sresultaccuracyname,"
						+ " rp.jsondata->>'sparametersynonym' sparametersynonym,"
						+ " r.sarno,rs.ssamplearno,g.sgradename,u.sunitname,"
						+ " rp.jsondata->>'dentereddate' entereddate, "
						+ "rp.jsondata->>'dentereddatetimezonecode' dentereddatetimezonecode,"
						+ " rp.jsondata->>'noffsetdentereddate' noffsetdentereddate "
						+ " from releaseparameter rp,transactionstatus t,registrationarno r,registrationsamplearno rs,"
						+ " registrationtest rt,grade g,unit u "
						+ " where t.ntranscode=rp.ntransactionstatus and r.npreregno=rp.npreregno and r.npreregno=rs.npreregno"
						+ " and rs.npreregno=rp.npreregno and rt.ntransactionsamplecode=rs.ntransactionsamplecode"
						+ " and rt.ntransactiontestcode=rp.ntransactiontestcode and u.nunitcode=rp.nunitcode  "
						+ " and rt.npreregno=rs.npreregno and rs.nsitecode = " + userInfo.getNtranssitecode()
						+ " and rp.nsitecode = " + userInfo.getNtranssitecode() + " and r.nsitecode = "
						+ userInfo.getNtranssitecode() + " and rt.nsitecode = " + userInfo.getNtranssitecode()
						+ " and u.nsitecode = " + userInfo.getNmastersitecode()
						+ " and r.npreregno=rt.npreregno and g.ngradecode=rp.ngradecode " + " and rp.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and t.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and r.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and rs.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and rt.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and g.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and u.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and rp.ntransactionresultcode = " + ntransactionresultcode + " ";

				List<ReleaseParameter> lstSampleResultsafter = jdbcTemplate.query(query, new ReleaseParameter());

				jsonAuditOldData.put("releaseparameter", lstSampleResults);
				jsonAuditNewData.put("releaseparameter", lstSampleResultsafter);
				auditActionType.put("releaseparameter", "IDS_RESULTCORRECTION");
				Map<String, Object> objMap = new HashMap<>();
				objMap.put("nregtypecode", nregtypecode);
				objMap.put("nregsubtypecode", nregsubtypecode);
				objMap.put("ndesigntemplatemappingcode", ndesigntemplatemappingcode);

				if (!(lstSampleResults.get(0).getSresult().toString()
						.equalsIgnoreCase((String) jsonData.get("sresult")))) {
					query = " select nsequenceno from seqnoregistration where stablename=N'resultchangehistory' and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					int nresultchangehistoryno = jdbcTemplate.queryForObject(query, Integer.class);

					String insertResultChange = "insert into resultchangehistory (nresultchangehistorycode,nformcode, ntransactionresultcode,ntransactiontestcode,npreregno,"
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
							+ stringUtilityFunction.replaceQuote(objMapper
									.writeValueAsString(lstTransSampResults.get(0).getJsondata().get("sresult")))
							+ "," + "     \"sfinal\":"
							+ stringUtilityFunction.replaceQuote(objMapper
									.writeValueAsString(lstTransSampResults.get(0).getJsondata().get("sfinal")))
							+ "," + "    \"nresultaccuracycode\":"
							+ stringUtilityFunction.replaceQuote(objMapper.writeValueAsString(
									lstTransSampResults.get(0).getJsondata().get("nresultaccuracycode")))
							+ "," + "    \"sresultaccuracyname\":"
							+ stringUtilityFunction.replaceQuote(objMapper.writeValueAsString(
									lstTransSampResults.get(0).getJsondata().get("sresultaccuracyname")))
							+ "," + "    \"nunitcode\":"
							+ stringUtilityFunction.replaceQuote(objMapper
									.writeValueAsString(lstTransSampResults.get(0).getJsondata().get("nunitcode")))
							+ "," + "    \"sunitname\":"
							+ stringUtilityFunction.replaceQuote(objMapper
									.writeValueAsString(lstTransSampResults.get(0).getJsondata().get("sunitname")))
							+ "," + "     \"dentereddate\":\""
							+ lstSampleResultsafter.get(0).getEntereddate().toString() + "\","
							+ "     \"dentereddatetimezonecode\":\""
							+ lstSampleResultsafter.get(0).getDentereddatetimezonecode() + "\","
							+ "     \"noffsetdentereddate\":\"" + lstSampleResults.get(0).getNoffsetdentereddate()
							+ "\"" + " }'::jsonb," + userInfo.getNtranssitecode() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " nstatus from resultparameter where nsitecode = " + userInfo.getNtranssitecode()
							+ " and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and ntransactionresultcode = " + ntransactionresultcode + ";";
					jdbcTemplate.execute(insertResultChange);
					jdbcTemplate.execute("update seqnoregistration  set nsequenceno=" + (nresultchangehistoryno + 1)
							+ " where stablename=N'resultchangehistory';");
				}

				auditUtilityFunction.fnJsonArrayAudit(jsonAuditOldData, jsonAuditNewData, auditActionType, objMap,
						false, userInfo);

				Map<String, Object> inputMap = new HashMap<>();
				inputMap.put("ncoaparentcode", ncoaparentcode);

				returnMap.putAll((Map<String, Object>) getResultCorrection(inputMap, userInfo).getBody());

			}
		}

		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getResultCorrection(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		Map<String, Object> returnMap = new HashMap<>();

		final String statusQuery = "select * from coaparent where ncoaparentcode=" + inputMap.get("ncoaparentcode")
				+ "  and ntransactionstatus in (" + Enumeration.TransactionStatus.CORRECTION.gettransactionstatus()
				+ ", " + Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ") " + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
				+ userInfo.getNtranssitecode() + "";
		List<COAParent> statusList = jdbcTemplate.query(statusQuery, new COAParent());

		if (!statusList.isEmpty()) {
			// Modified the Query by sonia What i have done Mentioned in jira: ALPD-4275
			final String sQuery = " select rp.ntransactionresultcode,cm.scolorhexcode,r.npreregno,rar.sarno,rs.ntransactionsamplecode,rt.ntransactiontestcode,rsar.ssamplearno,"
					+ " rp.jsondata->>'stestsynonym' as stestsynonym,rp.jsondata->>'sparametersynonym' as sparametersynonym, "
					+ " case when rp.nunitcode =-1 then rp.jsondata->>'sfinal'::character VARYING else concat(rp.jsondata->>'sfinal',' ',u.sunitname) end as sfinal,"
					+ " coalesce(gd.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
					+ "',gd.jsondata->'sdisplayname'->>'en-US')  sgradename, "
					+ " case  when rp.jsondata ->> 'sresultaccuracyname'  isnull  then ''  else  rp.jsondata ->> 'sresultaccuracyname' end AS sresultaccuracyname, "
					+ "(rp.jsondata->>'smina')::character varying(10) as smina,(rp.jsondata->>'sminb')::character varying(10) as sminb,"
					+ "(rp.jsondata->>'smaxa')::character varying(10) as smaxa,(rp.jsondata->>'smaxb')::character varying(10) as smaxb,"
					+ "(rp.jsondata->>'sminlod')::character varying(10) as sminlod,(rp.jsondata->>'sminloq')::character varying(10) as sminloq,"
					+ "(rp.jsondata->>'smaxlod')::character varying(10) as smaxlod,(rp.jsondata->>'smaxloq')::character varying(10) as smaxloq "
					+ " from registration r,registrationarno rar,registrationsample rs,registrationsamplearno rsar,"
					+ " registrationtest rt,colormaster cm,releaseparameter rp,grade gd,coaparent cp,coachild cc,unit u "
					+ " where rp.ngradecode = gd.ngradecode and rp.nunitcode =u.nunitcode and rp.ntransactiontestcode = rt.ntransactiontestcode and r.npreregno = rar.npreregno  "
					+ " and rs.npreregno = r.npreregno and rsar.npreregno=rp.npreregno and cc.ncoaparentcode=cp.ncoaparentcode  "
					+ " and rsar.npreregno = rs.npreregno and rsar.npreregno = rp.npreregno and rs.ntransactionsamplecode = rsar.ntransactionsamplecode "
					+ " and rs.ntransactionsamplecode=rt.ntransactionsamplecode and cc.ntransactiontestcode=rt.ntransactiontestcode "
					+ " and gd.ncolorcode = cm.ncolorcode " + " and rt.nsitecode = " + userInfo.getNtranssitecode()
					+ " and rar.nsitecode = " + userInfo.getNtranssitecode() + " and rsar.nsitecode = "
					+ userInfo.getNtranssitecode() + " and cc.nsitecode = " + userInfo.getNtranssitecode()
					+ " and rp.nsitecode = " + userInfo.getNtranssitecode() + " and rs.nsitecode = "
					+ userInfo.getNtranssitecode() + " and cp.nsitecode = " + userInfo.getNtranssitecode()
					+ " and r.nsitecode=" + userInfo.getNsitecode() + " and u.nsitecode = "
					+ userInfo.getNmastersitecode() + " and cp.ncoaparentcode=" + inputMap.get("ncoaparentcode") + ""
					+ " and r.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and u.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and rar.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and rs.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and rsar.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and rt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and cm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and rp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and gd.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and cp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and cc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			List<ReleaseParameter> releaseParameterList = jdbcTemplate.query(sQuery, new ReleaseParameter());

			returnMap.put("ResultCorrection", releaseParameterList);
			return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTCORRECTEDRECORD",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> getReleaseResults(int ntransactionresultcode, UserInfo userInfo) throws Exception {

		Map<String, Object> returnMap = new HashMap<>();
		final ObjectMapper objMapper = new ObjectMapper();
		String getParams = "select row_num,"
				+ " (a.jsondata->>'ntestgrouptestpredefcode')::int as ntestgrouptestpredefcode,a.npreregno,a.jsondata||a.jsonuidata AS jsondata,"
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
				+ "a.ntestgrouptestparametercode,a.nsorter,a.ntestgrouptestcode,a.nresultmandatory,a.stransdisplaystatus as sresultmandatory,a.sunitname,a.nunitcode,a.nresultaccuracycode,a.sresultaccuracyname  "
				+ " from ("
				+ "select ROW_NUMBER() OVER (PARTITION BY rt.ntransactiontestcode,ra.sarno,rsa.ssamplearno ORDER BY rt.npreregno, tgtp.nsorter, rp.ntransactionresultcode,rt.ntransactiontestcode desc) row_num, "
				+ "rp.jsondata,r.jsonuidata, rp.npreregno,ra.sarno,rsa.ssamplearno,rp.jsondata->>'stestsynonym' as stestsynonym,tgtp.sparametersynonym,rp.nparametertypecode,pt.sparametertypename,"
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
				+ "' as stransdisplaystatus,u.sunitname,u.nunitcode,"
				+ " (rp.jsondata->>'nresultaccuracycode')::integer as nresultaccuracycode,(rp.jsondata->>'sresultaccuracyname')::character varying(10) as sresultaccuracyname"
				+ " from unit u,releaseparameter rp,registrationsamplearno rsa,registrationarno ra,registration r,registrationtest rt,testgrouptestparameter tgtp "
				+ "left outer join testgrouptestnumericparameter tgnp on tgtp.ntestgrouptestparametercode = tgnp.ntestgrouptestparametercode and tgnp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "left outer join testgrouptestformula tgf on tgf.ntestgrouptestcode=tgtp.ntestgrouptestcode and tgf.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgf.ntransactionstatus  = "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " "
				+ "and tgtp.ntestgrouptestparametercode=tgf.ntestgrouptestparametercode, "
				+ "parametertype pt,grade g,transactionstatus ts  "
				+ "where pt.nparametertypecode = tgtp.nparametertypecode " + "and ts.ntranscode = rp.nresultmandatory "
				+ " and r.npreregno = rt.npreregno  and tgtp.ntestgrouptestparametercode = rp.ntestgrouptestparametercode and rsa.ntransactionsamplecode = rt.ntransactionsamplecode and ra.npreregno = rt.npreregno  "
				+ "and rp.ntransactiontestcode = rt.ntransactiontestcode "
				+ "and rp.ngradecode = g.ngradecode and rp.ntransactionstatus not in("
				+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ") "
				+ "and rp.ntransactionresultcode = " + ntransactionresultcode + "  " + " and ra.nsitecode = "
				+ userInfo.getNtranssitecode() + " and rsa.nsitecode = ra.nsitecode and rt.nsitecode = rsa.nsitecode "
				+ " and r.nsitecode = rsa.nsitecode and rp.nsitecode = rt.nsitecode and rt.nstatus = rp.nstatus "
				+ " and pt.nstatus = g.nstatus and g.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rp.nunitcode=u.nunitcode and u.nstatus =  "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by rp.ntransactionresultcode desc)a ;";

		final String getPreDefinedParams = " select tgpp.ntestgrouptestpredefcode, tgpp.ntestgrouptestparametercode, tgpp.ngradecode,  "
				+ " tgpp.spredefinedname||' ('||tgpp.spredefinedsynonym||')' as sresultpredefinedname,tgpp.spredefinedname,"
				+ " tgpp.spredefinedsynonym, " + " tgpp.spredefinedcomments, tgpp.salertmessage, "
				+ " 4 as nneedresultentryalert, 4 as nneedsubcodedresult," + " tgpp.ndefaultstatus, tgpp.nsitecode, "
				+ " tgpp.dmodifieddate, tgpp.nstatus,rp.ntransactiontestcode,rp.ntestgrouptestparametercode,rp.ntransactionresultcode "
				+ " from releaseparameter rp,testgrouptestpredefparameter tgpp"
				+ " where rp.ntestgrouptestparametercode = tgpp.ntestgrouptestparametercode and rp.ntransactionresultcode = "
				+ ntransactionresultcode + " " + " and rp.nparametertypecode = "
				+ Enumeration.ParameterType.PREDEFINED.getparametertype() + " and rp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgpp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgpp.spredefinedname <> '' ;";

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

	@Override
	public ResponseEntity<Object> updateCorrectionStatus(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditOld = new JSONObject();
		JSONObject jsonAuditNew = new JSONObject();

		final String lockquery = "lock lockresultcorrection" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(lockquery);

		final String sQuery = "select * from coaparent where ncoaparentcode=" + inputMap.get("ncoaparentcode") + " "
				+ " and ntransactionstatus in (" + Enumeration.TransactionStatus.RELEASED.gettransactionstatus() + ", "
				+ Enumeration.TransactionStatus.PRELIMINARYRELEASE.gettransactionstatus() + ") " + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		List<COAParent> statusList = jdbcTemplate.query(sQuery, new COAParent());

		if (!statusList.isEmpty()) {

			Map<String, Object> lstDataTest = getReleaseAuditGet(inputMap, userInfo);

			final String sCoaQuery = "update coaparent set dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' ," + " ntransactionstatus="
					+ Enumeration.TransactionStatus.CORRECTION.gettransactionstatus() + " where ncoaparentcode="
					+ inputMap.get("ncoaparentcode") + " and nsitecode=" + userInfo.getNtranssitecode()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

			jdbcTemplate.execute(sCoaQuery);

			insertReleaseHistory(inputMap.get("ncoaparentcode").toString(), userInfo, "");

			inputMap.put("AuditStatus", "AuditStatus");
			Map<String, Object> lstDataTestNew = getReleaseAuditGet(inputMap, userInfo);
			jsonAuditOld.put("registrationtest",
					(List<Map<String, Object>>) ((Map<String, Object>) lstDataTest).get("ReleaseTestAudit"));
			jsonAuditNew.put("registrationtest",
					(List<Map<String, Object>>) ((Map<String, Object>) lstDataTestNew).get("ReleaseTestAudit"));
			auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
			auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
			auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
			actionType.put("registrationtest", "IDS_CORRECTIONSAMPLE");

			auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, jsonAuditNew, actionType, auditmap, false, userInfo);

			returnMap.putAll((Map<String, Object>) getReleaseSample(inputMap, userInfo).getBody());
			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.SUCCESS.getreturnstatus());

			return new ResponseEntity<>(returnMap, HttpStatus.OK);

		} else {
			String alertPreliminaryReport = "";
			if ((int) inputMap.get("isPreliminaryReportNoGenerate") == Enumeration.TransactionStatus.YES
					.gettransactionstatus()) {
				alertPreliminaryReport = "/"
						+ ((Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) inputMap
								.get("genericLabel")).get("PreliminaryReport")).get("jsondata")).get("sdisplayname"))
								.get(userInfo.getSlanguagetypecode()).toString();
			}
			final String alertMsg = commonFunction.getMultilingualMessage("IDS_SELECT", userInfo.getSlanguagefilename())
					+ " " + commonFunction.getMultilingualMessage("IDS_RELEASED", userInfo.getSlanguagefilename())
					+ alertPreliminaryReport + " "
					+ commonFunction.getMultilingualMessage("IDS_RECORDS", userInfo.getSlanguagefilename());
			return new ResponseEntity<>(alertMsg, HttpStatus.OK);
		}

	}

	public Map<String, Object> createPatientHistory(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		Map<String, Object> returnMap = new HashMap<String, Object>();
		List<Map<String, Object>> patientHistoryJsonData = new ArrayList<>();
		int npatienthistorycode = (int) inputMap.get("npatienthistorycode");
		final String ntransactionTestCode = (String) inputMap.get("ntransactiontestcode");

		final String strPatientHistoryJsonData = "select rsa.ntransactionsamplecode,cc.ncoachildcode,cc.ncoaparentcode,r.jsondata->>'spatientid' spatientid,"
				+ " rp.ntransactiontestcode,rp.jsondata->>'stestsynonym' stestsynonym,rp.jsondata->>'sparametersynonym' sparametersynonym ,"
				+ " ra.npreregno,ra.sarno,rsa.ssamplearno,rp.ntestgrouptestparametercode nparametercode,"
				+ " rp.jsondata->>'sresult' sresult, rp.ntransactionresultcode, rp.jsondata->>'sfinal' sfinal, rp.ngradecode,"
				+ " coalesce(g.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " g.jsondata->'sdisplayname'->>'en-US') sgradename,rt.ntestcode,rs.ncomponentcode,"
				+ " r.nproductcatcode,r.jsondata->'OrderIdData' orderiddata,rs.jsondata->'sampleorderid' sampleorderid, rt.jsondata->'stestname' stestname,"
				+ " cast(r.jsondata->>'nexternalordertypecode' as int)  nexternalordertypecode,cast(rs.jsondata->>'nordertypecode' as int)  nsampleordertypecode,rs.jsondata->>'externalorderid'  as  externalorderid,cast(r.jsondata->>'orderTypeValue' as int) nordertypecode, "
				+ " (select max(crh.nversionno) from coareporthistory crh where crh.ncoaparentcode=cp.ncoaparentcode and crh.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and crh.nsitecode = "
				+ userInfo.getNtranssitecode() + " group by crh.ncoaparentcode) as nversionno "
				+ " from registrationarno ra,registrationsample rs,registrationsamplearno rsa,releaseparameter rp, "
				+ " registrationtest rt, coachild cc, coaparent cp, registration r, grade g where rsa.ntransactionsamplecode=rs.ntransactionsamplecode and ra.npreregno=rsa.npreregno "
				+ " and rsa.npreregno=rp.npreregno and rp.npreregno=rt.npreregno and cc.npreregno=ra.npreregno and "
				+ " r.npreregno=ra.npreregno and ra.npreregno in ( " + inputMap.get("npreregno")
				+ " ) and rp.ntransactiontestcode in (" + ntransactionTestCode
				+ ") and rsa.ntransactionsamplecode=rt.ntransactionsamplecode "
				+ " and rp.ntransactiontestcode=rt.ntransactiontestcode and" + " cp.ncoaparentcode=cc.ncoaparentcode"
				+ " and cc.ntransactiontestcode=rp.ntransactiontestcode and cp.ncoaparentcode in ("
				+ inputMap.get("ncoaparentcode") + ") and g.ngradecode=rp.ngradecode " + " and rsa.nsitecode = "
				+ userInfo.getNtranssitecode() + " and rp.nsitecode = " + userInfo.getNtranssitecode()
				+ " and rt.nsitecode = " + userInfo.getNtranssitecode() + " and cc.nsitecode = "
				+ userInfo.getNtranssitecode() + " and cp.nsitecode = " + userInfo.getNtranssitecode()
				+ " and ra.nsitecode = " + userInfo.getNtranssitecode() + " and rs.nsitecode = "
				+ userInfo.getNtranssitecode() + " and r.nsitecode =" + userInfo.getNtranssitecode()
				+ " and ra.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rsa.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and r.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rs.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and g.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		patientHistoryJsonData = jdbcTemplate.queryForList(strPatientHistoryJsonData);
		String strPatientHistory = "insert into patienthistory( "
				+ "npatienthistorycode, ncoaparentcode, ncoachildcode, npreregno, ntransactionsamplecode, "
				+ "ntransactiontestcode, nversionno, spatientid, jsondata, dtransactiondate, noffsetdtransactiondate, "
				+ "ntransdatetimezonecode, dmodifieddate, nsitecode, nstatus) values ";

		String queryPatientHistory = "";
		int nversionno = 0;
		for (int i = 0; i < ntransactionTestCode.split(",").length; i++) {
			String addstring = "";
			npatienthistorycode++;
			if (i < ntransactionTestCode.split(",").length - 1) {
				addstring = ",";
			}

			List<JSONObject> jsonObjPatientHistory = new ArrayList<JSONObject>();

			String nTransactionTestCode = (ntransactionTestCode.split(",")[i]).toString();
			List<Map<String, Object>> lstPatientHistoryJsonData = patientHistoryJsonData.stream()
					.filter(x -> (x.get("ntransactiontestcode").toString()).equals(nTransactionTestCode))
					.collect(Collectors.toList());
			List<Map<String, Object>> lstPatientHistoryJson = new ArrayList<>();
			lstPatientHistoryJson.addAll(lstPatientHistoryJsonData);

			if (lstPatientHistoryJsonData.get(0).get("nversionno") == null) {
				nversionno = 0;
			} else {
				nversionno = (int) lstPatientHistoryJsonData.get(0).get("nversionno");
			}

			for (Map<String, Object> data : lstPatientHistoryJson) {
				JSONObject obj = new JSONObject(data);
				jsonObjPatientHistory.add(obj);
			}
			queryPatientHistory = "(" + npatienthistorycode + ","
					+ lstPatientHistoryJsonData.get(0).get("ncoaparentcode") + ","
					+ lstPatientHistoryJsonData.get(0).get("ncoachildcode") + ","
					+ lstPatientHistoryJsonData.get(0).get("npreregno") + ","
					+ lstPatientHistoryJsonData.get(0).get("ntransactionsamplecode") + ","
					+ lstPatientHistoryJsonData.get(0).get("ntransactiontestcode") + "," + nversionno + ",'"
					+ lstPatientHistoryJsonData.get(0).get("spatientid") + "','"
					+ stringUtilityFunction.replaceQuote(jsonObjPatientHistory.toString()) + "','"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
					+ userInfo.getNtimezonecode() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
					+ userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ")" + addstring;

			strPatientHistory = strPatientHistory + queryPatientHistory;
		}
		jdbcTemplate.execute(strPatientHistory);
		returnMap.put("seqNoPatientHistory", npatienthistorycode);
		return returnMap;
	}

	public Map<String, Object> insertCOAParentRelatedData(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		final Map<String, Object> returnMap = new HashMap<String, Object>();
		final Map<String, Object> mailMap = new HashMap<String, Object>();

		mailMap.put("ncontrolcode", inputMap.get("ncontrolcode"));

		final UserInfo mailUserInfo = new UserInfo(userInfo);
		final Map<String, Object> auditmap = new LinkedHashMap<String, Object>();

		final JSONObject actionType = new JSONObject();
		final JSONObject jsonAuditOld = new JSONObject();

		final JSONObject jsonAuditNew = new JSONObject();
		final String ntransactionTestCode = (String) inputMap.get("ntransactiontestcode");
		String sReportformat = "";

		inputMap.put("ntransactiontestcode", ntransactionTestCode.toString());

		final Map<String, Object> lstDataTest = getReleaseAuditGet(inputMap, userInfo);

		createRegistrationHistory(inputMap, userInfo);

		if ((int) inputMap.get("nclinicaltyperequired") == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
			inputMap.put("systemFileNameAndVersion", returnMap.get("systemFileNameAndVersion"));
			createPatientHistory(inputMap, userInfo);

		}
		String strReportNoGenerate = "";

		final List<String> nCoaParentCode = Arrays.asList(inputMap.get("ncoaparentcode").toString().split(","));

		Map<Integer, String> sReportformatBasedOnRegSubType = new HashMap<>();

		SeqNoReleasenoGenerator seqnoFormat = checkReleaseNoGeneratorFormat(inputMap, userInfo);

		if (seqnoFormat != null && seqnoFormat.getJsondata().containsKey("nneedsitewisearnorelease")
				&& (boolean) seqnoFormat.getJsondata().get("nneedsitewisearnorelease")) {
			seqnoFormat = insertsiteReleasenoGenerator(seqnoFormat, userInfo, inputMap);
		}
		if ((int) inputMap.get("isPreliminaryReportNoGenerate") == Enumeration.TransactionStatus.YES
				.gettransactionstatus()) {

			final List<PreliminaryReportHistory> lstPreliminaryReportHistory = getPreliminaryReportHistoryCount(
					inputMap.get("ncoaparentcode").toString(), userInfo);

			String matchingCoaParentCode = "";
			String nonMatchingCoaParentCode = "";

			matchingCoaParentCode = nCoaParentCode.stream()
					.filter(str -> lstPreliminaryReportHistory.stream()
							.anyMatch(pojo -> String.valueOf(pojo.getNcoaparentcode()).equals(str)))
					.collect(Collectors.joining(","));

			nonMatchingCoaParentCode = nCoaParentCode.stream()
					.filter(str -> lstPreliminaryReportHistory.stream()
							.noneMatch(pojo -> String.valueOf(pojo.getNcoaparentcode()).equals(str)))
					.collect(Collectors.joining(","));

			List<String> nonMatchingParentCode = Arrays.asList(nonMatchingCoaParentCode.split(","));

			if (seqnoFormat != null && (!nonMatchingCoaParentCode.isEmpty())) {
				sReportformatBasedOnRegSubType = transactionDAOSupport.getfnRereleaseFormat(nonMatchingParentCode,
						seqnoFormat.getNsequenceno() + 1, seqnoFormat.getSreleaseformat(),
						seqnoFormat.getDseqresetdate(), seqnoFormat.getNseqnoreleasenogencode(), userInfo,
						seqnoFormat.getJsondata(), seqnoFormat.getNregsubtypeversionreleasecode(),
						seqnoFormat.getNperiodcode(), seqnoFormat.getNsequenceno());
			}

			if (!matchingCoaParentCode.isEmpty()) {
				strReportNoGenerate = strReportNoGenerate + "update coaparent set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' ," + " ntransactionstatus="
						+ Enumeration.TransactionStatus.RELEASED.gettransactionstatus() + " where ncoaparentcode in ("
						+ matchingCoaParentCode + ") and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and nsitecode="
						+ userInfo.getNtranssitecode() + ";";
			}
			returnMap.put("sreportno", sReportformat);
			if (!nonMatchingCoaParentCode.isEmpty()) {
				for (String coaParentCode : nonMatchingParentCode) {
					if (seqnoFormat != null && (!nonMatchingCoaParentCode.isEmpty())) {
						sReportformat = sReportformatBasedOnRegSubType.get(Integer.parseInt(coaParentCode));
					} else {
						sReportformat = projectDAOSupport.getSeqfnFormat("coaparent", "seqnoformatgeneratorrelease",
								(int) inputMap.get("nregtypecode"), (int) inputMap.get("nregsubtypecode"), userInfo);
					}
					strReportNoGenerate = strReportNoGenerate + "update coaparent set dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' ," + " ntransactionstatus="
							+ Enumeration.TransactionStatus.RELEASED.gettransactionstatus() + " , sreportno='"
							+ sReportformat + "' where ncoaparentcode=" + coaParentCode + " and nsitecode="
							+ userInfo.getNtranssitecode() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				}
			}

			if (!nonMatchingCoaParentCode.isEmpty()) {

				final Map<String, Object> returnMapPreliminaryReport = new HashMap<>();

				returnMapPreliminaryReport.putAll(inputMap);

				returnMapPreliminaryReport.put("ncoaparentcode", nonMatchingCoaParentCode);

				preliminaryReport(returnMapPreliminaryReport, userInfo);
			}

		} else {
			if (seqnoFormat != null) {
				sReportformatBasedOnRegSubType = transactionDAOSupport.getfnRereleaseFormat(nCoaParentCode,
						seqnoFormat.getNsequenceno() + 1, seqnoFormat.getSreleaseformat(),
						seqnoFormat.getDseqresetdate(), seqnoFormat.getNseqnoreleasenogencode(), userInfo,
						seqnoFormat.getJsondata(), seqnoFormat.getNregsubtypeversionreleasecode(),
						seqnoFormat.getNperiodcode(), seqnoFormat.getNsequenceno());
			}
			for (String coaParentCode : nCoaParentCode) {
				if (seqnoFormat != null) {
					sReportformat = sReportformatBasedOnRegSubType.get(Integer.parseInt(coaParentCode));
				} else {
					sReportformat = projectDAOSupport.getSeqfnFormat("coaparent", "seqnoformatgeneratorrelease",
							(int) inputMap.get("nregtypecode"), (int) inputMap.get("nregsubtypecode"), userInfo);
				}
				strReportNoGenerate = strReportNoGenerate + "update coaparent set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' ," + " ntransactionstatus="
						+ Enumeration.TransactionStatus.RELEASED.gettransactionstatus() + " , sreportno='"
						+ sReportformat + "' where ncoaparentcode=" + coaParentCode + " and nsitecode="
						+ userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			}
		}

		jdbcTemplate.execute(strReportNoGenerate);
		// added by subashini
		returnMap.putAll(createReportHistory(inputMap, userInfo));

		inputMap.put("ncoareporthistorycode", returnMap.get("ncoareporthistorycode"));
		inputMap.put("systemFileName", returnMap.get("systemFileName"));

		if (inputMap.containsKey("nsampletypecode")
				&& (int) inputMap.get("nsampletypecode") == Enumeration.SampleType.CLINICALSPEC.getType()
				&& inputMap.containsKey("noutsourcerequired")
				&& (int) inputMap.get("noutsourcerequired") == Enumeration.TransactionStatus.YES.gettransactionstatus()
				&& ((int) inputMap.get("ncoareporttypecode") == Enumeration.COAReportType.SAMPLEWISE.getcoaReportType()
						|| (int) inputMap.get("ncoareporttypecode") == Enumeration.COAReportType.SECTIONWISE
								.getcoaReportType())) {

			inputMap.put("releaseOutsourceAttachmentSeqNo", returnMap.get("releaseOutsourceAttachmentSeqNo"));
			inputMap.put("systemFileNameAndVersion", returnMap.get("systemFileNameAndVersion"));
			outsourceCOAReportTransfer(inputMap, userInfo);
		}

		inputMap.put("npreregno", inputMap.get("npreregno").toString());
		inputMap.put("ntransactiontestcode", ntransactionTestCode.toString());

		final Map<String, Object> lstDataTestNew = getReleaseAuditGet(inputMap, userInfo);

		jsonAuditOld.put("registrationtest",
				(List<Map<String, Object>>) ((Map<String, Object>) lstDataTest).get("ReleaseTestAudit"));
		jsonAuditNew.put("registrationtest",
				(List<Map<String, Object>>) ((Map<String, Object>) lstDataTestNew).get("ReleaseTestAudit"));
		auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
		auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
		auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
		actionType.put("registrationtest", "IDS_RELEASESAMPLE");

		auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, jsonAuditNew, actionType, auditmap, false, userInfo);

		returnMap.put("ReportAvailable", Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		returnMap.put("ncoareporthistorycode", returnMap.get("historySeqNo"));

		final String[] ncoaParentCode = inputMap.get("ncoaparentcode").toString().split(",");
		// ALPD-5619 - added by Gowtham R on 28/03/2025 - Mail alert transaction > NULL
		// displayed in reference no column.
		mailMap.put("ssystemid", sReportformat);

		for (String coaParentCode : ncoaParentCode) {
			mailMap.put("ncoaparentcode", Integer.parseInt(coaParentCode));
			ResponseEntity<Object> mailResponse = emailDAOSupport.createEmailAlertTransaction(mailMap, mailUserInfo);
			if (mailResponse != null) {
				returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), mailResponse.getBody());
			}
		}

		return returnMap;
	}

	public Map<String, Object> regenerateReport(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		int nversionno = 0;
		String reportpresentQuery = "select crh.*, cp.ntransactionstatus from coareporthistory crh, coaparent cp where crh.ncoaparentcode ="
				+ inputMap.get("ncoaparentcode") + " and cp.ncoaparentcode=crh.ncoaparentcode and crh.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and crh.nsitecode="
				+ userInfo.getNtranssitecode() + " and cp.nsitecode=" + userInfo.getNtranssitecode()
				+ " order by 1 desc LIMIT 1 ";

		COAReportHistory objreportpresent = (COAReportHistory) jdbcUtilityFunction.queryForObject(reportpresentQuery,
				COAReportHistory.class, jdbcTemplate);

		if (objreportpresent != null && objreportpresent
				.getNtransactionstatus() == Enumeration.TransactionStatus.RELEASED.gettransactionstatus()) {
			String sVersionQuery = "select max(nversionno) from coareporthistory where ncoaparentcode="
					+ inputMap.get("ncoaparentcode") + "" + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
					+ userInfo.getNtranssitecode();
			nversionno = jdbcTemplate.queryForObject(sVersionQuery, Integer.class);

		} else {
			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.FAILED.getreturnstatus());
			return returnMap;
		}

		String coaReportHistorySeq = "select stablename,nsequenceno from seqnoreleasemanagement where stablename in ('regeneratereporthistory','releaseoutsourceattachment')"
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
		List<SeqNoReleaseManagement> lstSeqNo = jdbcTemplate.query(coaReportHistorySeq, new SeqNoReleaseManagement());

		Map<String, Object> listSeqNo = lstSeqNo.stream()
				.collect(Collectors.toMap(SeqNoReleaseManagement::getStablename,
						SeqNoReleaseManagement -> SeqNoReleaseManagement.getNsequenceno()));
		int historySeqNo = (int) listSeqNo.get("regeneratereporthistory");
		int releaseOutsourceAttachmentSeqNo = (int) listSeqNo.get("releaseoutsourceattachment");
		releaseOutsourceAttachmentSeqNo++;
		historySeqNo = historySeqNo + 1;

		String strQuery1 = "insert into regeneratereporthistory(nregeneratereporthistorycode,ncoaparentcode,nversionno,"
				+ " nreportypecode, nreportdetailcode, nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode,sreportcomments,"
				+ " dtransactiondate, ntztransactiondate, noffsetdtransactiondate,nsitecode,nstatus )" + "  values ( "
				+ historySeqNo + "," + inputMap.get("ncoaparentcode") + "," + nversionno + ","
				+ inputMap.get("nreporttypecode") + " , " + inputMap.get("nreportdetailcode") + ","
				+ userInfo.getNusercode() + "," + userInfo.getNuserrole() + "," + userInfo.getNdeputyusercode() + ","
				+ userInfo.getNdeputyuserrole() + ",N'"
				+ stringUtilityFunction.replaceQuote(inputMap.get("sreportcomments").toString()) + "'," + " '"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' ," + userInfo.getNtimezonecode() + ","
				+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
				+ userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ ")";
		jdbcTemplate.execute(strQuery1);

		String coaReportHistorySeqno = "update seqnoreleasemanagement set nsequenceno=" + historySeqNo
				+ " where stablename='regeneratereporthistory' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
		jdbcTemplate.execute(coaReportHistorySeqno);

//		ALPD-5219 Code uncommented by Vishakh because outsource for multi release is done
		if (inputMap.containsKey("nsampletypecode")
				&& (int) inputMap.get("nsampletypecode") == Enumeration.SampleType.CLINICALSPEC.getType()
				&& inputMap.containsKey("noutsourcerequired")
				&& (int) inputMap.get("noutsourcerequired") == Enumeration.TransactionStatus.YES.gettransactionstatus()
				&& ((int) inputMap.get("ncoareporttypecode") == Enumeration.COAReportType.SAMPLEWISE.getcoaReportType()
						|| (int) inputMap.get("ncoareporttypecode") == Enumeration.COAReportType.SECTIONWISE
								.getcoaReportType())) {
			inputMap.put("nversionno", nversionno);
			inputMap.put("releaseOutsourceAttachmentSeqNo", releaseOutsourceAttachmentSeqNo);
			outsourceCOAReportTransfer(inputMap, userInfo);
		}

		returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
				Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		return returnMap;
	}

	public Map<String, Object> getDigitalSignatureDetail(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		// ALPD-4436
		// To get path value from system's environment variables instead of absolutepath
		final String homePath = ftpUtilityFunction.getFileAbsolutePath();

		Map<String, Object> returnMap = new HashMap<String, Object>();
		String ssignfilename = "";
		String scertfilename = "";
		String ssecuritykey = "";

		String squery = "select crh.nusercode from coareporthistory crh " + " where crh.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and crh.nsitecode = "
				+ userInfo.getNtranssitecode() + " and " + " crh.ncoareporthistorycode = "
				+ " (select max(ncoareporthistorycode) from coareporthistory where ncoaparentcode = "
				+ inputMap.get("ncoaparentcode") + " and " + "	nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
				+ userInfo.getNtranssitecode() + ");";
		COAReportHistory objCOAReportHistory = (COAReportHistory) jdbcUtilityFunction.queryForObject(squery,
				COAReportHistory.class, jdbcTemplate);
		if (objCOAReportHistory != null) {
			squery = "select ssignimgftp from userfile where nusercode = " + objCOAReportHistory.getNusercode()
					+ " and nsitecode = " + userInfo.getNmastersitecode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			UserFile objSignfilename = (UserFile) jdbcUtilityFunction.queryForObject(squery, UserFile.class,
					jdbcTemplate);
			ssignfilename = objSignfilename != null
					? objSignfilename.getSsignimgftp() != null ? objSignfilename.getSsignimgftp() : ""
					: "";
			squery = "select sdigisignftp from userdigitalsign where nusercode = " + objCOAReportHistory.getNusercode()
					+ " and nsitecode = " + userInfo.getNmastersitecode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			DigitalSignature objScertfilename = (DigitalSignature) jdbcUtilityFunction.queryForObject(squery,
					DigitalSignature.class, jdbcTemplate);
			scertfilename = objScertfilename != null
					? objScertfilename.getSdigisignftp() != null ? objScertfilename.getSdigisignftp() : ""
					: "";

			if (objScertfilename != null && (!objScertfilename.getSdigisignftp().equals(""))) {
				ssecuritykey = passwordUtilityFunction.decryptPassword("userdigitalsign", "nusercode",
						objCOAReportHistory.getNusercode(), "ssecuritykey");
			}
		}

		if (ssignfilename != null && ssignfilename.length() > 0 && scertfilename != null
				&& scertfilename.length() > 0) {
			String absolutePath = System.getenv(homePath)// new File("").getAbsolutePath()
					+ Enumeration.FTP.USERSIGN_PATH.getFTP();
			ssignfilename = absolutePath + "\\" + ssignfilename;

			scertfilename = absolutePath + "\\" + scertfilename;
		}
		returnMap.put("ssecuritykey", ssecuritykey);
		returnMap.put("ssignfilename", ssignfilename);
		returnMap.put("scertfilename", scertfilename);

		return returnMap;
	}

	@Override
	public Map<String, Object> updateReleaseAfterCorrection(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		final Map<String, Object> returnMap = new HashMap<String, Object>();

		final Map<String, Object> mailMap = new HashMap<String, Object>();
		mailMap.put("ncontrolcode", inputMap.get("ncontrolcode"));

		final UserInfo mailUserInfo = new UserInfo(userInfo);

		boolean strCheck = checkCOAParentStatus(inputMap.get("ncoaparentcode").toString(), userInfo);

		if (strCheck) {
			final Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
			final List<Map<String, Object>> objlst = new ArrayList<Map<String, Object>>();
			JSONObject actionType = new JSONObject();
			JSONObject jsonAuditOld = new JSONObject();
			JSONObject jsonAuditNew = new JSONObject();
			String stable = "";
			String scondition = "";
			int nsectionCode = -1;

			addSampleAfterCorrectionRelease(inputMap, userInfo);

			Map<String, Object> lstDataTest = getReleaseAuditGet(inputMap, userInfo);
			if (inputMap.containsKey("reportSectionCode")) {
				nsectionCode = (int) inputMap.get("reportSectionCode");
			}
			final String conditionString = " and coa.ncoareporttypecode=" + inputMap.get("ncoareporttypecode")
					+ " and rm.nregtypecode=" + inputMap.get("nregtypecode") + " and rm.nregsubtypecode="
					+ inputMap.get("nregsubtypecode") + " and rm.napproveconfversioncode="
					+ inputMap.get("napprovalversioncode") + "" + " and rd.ntransactionstatus="
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and rm.nreportdecisiontypecode="
					+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " and rt.nreporttypecode="
					+ Enumeration.ReportType.COA.getReporttype() + " and rm.ncertificatetypecode = "
					+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " and rm.ncontrolcode = "
					+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " and rm.nsectioncode = "
					+ nsectionCode;

			if (((int) inputMap.get("ncoareporttypecode") == Enumeration.COAReportType.PROJECTWISE.getcoaReportType()
					|| (int) inputMap.get("ncoareporttypecode") == Enumeration.COAReportType.SECTIONWISEMULTIPLESAMPLE
							.getcoaReportType())
					&& (int) inputMap.get("isSMTLFlow") == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				stable += ",reportinfoproject rip";
				scondition += " and rip.nreporttemplatecode=rm.nreporttemplatecode and rip.nprojectmastercode="
						+ (int) inputMap.get("nprojectcode") + " and rip.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rip.nsitecode = "
						+ userInfo.getNtranssitecode() + "";
			}

//			ALPD-4799
//			Release & Report ---> In report designer screen, inactive the current coa report and try to do release, 500 occurs

			final String getMainReports = " select * from reportmaster rm,reportdetails rd,  reporttemplate rtt,reporttype rt ,coareporttype coa,coaparent cp "
					+ stable
					+ " where rtt.nreporttemplatecode = rm.nreporttemplatecode and cp.nreporttemplatecode = rtt.nreporttemplatecode and coa.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "   and rtt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and rm.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rd.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cp.nsitecode = "
					+ userInfo.getNtranssitecode() + " and rm.nsitecode = " + userInfo.getNmastersitecode()
					+ " and rd.nsitecode = " + userInfo.getNmastersitecode()
					+ " and rt.nreporttypecode=rm.nreporttypecode and coa.ncoareporttypecode=rm.ncoareporttypecode"
					+ " and rm.nreportcode=rd.nreportcode and cp.ncoaparentcode in ("
					+ inputMap.get("ncoaparentcode").toString() + ") and  cp.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rm.ntransactionstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + conditionString + scondition
					+ " limit 1";
			ReportDetails mainReport = (ReportDetails) jdbcUtilityFunction.queryForObject(getMainReports,
					ReportDetails.class, jdbcTemplate);

			if (mainReport == null) {
				if (((int) inputMap.get("ncoareporttypecode") == Enumeration.COAReportType.PROJECTWISE
						.getcoaReportType()
						|| (int) inputMap
								.get("ncoareporttypecode") == Enumeration.COAReportType.SECTIONWISEMULTIPLESAMPLE
										.getcoaReportType())
						&& (int) inputMap.get("isSMTLFlow") == Enumeration.TransactionStatus.YES
								.gettransactionstatus()) {
					returnMap.put("ProjectTypeFlow", Enumeration.TransactionStatus.YES.gettransactionstatus());
				}
				returnMap.put("ReportAvailable", Enumeration.ReturnStatus.FAILED.getreturnstatus());
				return returnMap;
			}
			String sCoaQuery = "update coaparent set dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
					+ "' ," + " ntransactionstatus=" + Enumeration.TransactionStatus.RELEASED.gettransactionstatus()
					+ " where ncoaparentcode in (" + inputMap.get("ncoaparentcode") + ")  and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			jdbcTemplate.execute(sCoaQuery);

			// Report Sent to portal after correction(update nportalstatus) ALPD-4315
			// Aravindh
			final String updateForPortalReport = "update externalorderreleasestatus set nportalstatus="
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + " where ncoaparentcode in ("
					+ inputMap.get("ncoaparentcode") + ") and nsitecode=" + userInfo.getNtranssitecode();
			jdbcTemplate.execute(updateForPortalReport);

			inputMap.put("nreportdetailcode", mainReport.getNreportdetailcode());

			returnMap.putAll(createReportHistory(inputMap, userInfo));

			inputMap.put("nversionno", returnMap.get("nversionno"));

			if ((int) inputMap.get("isPreliminaryReportNoGenerate") == Enumeration.TransactionStatus.YES
					.gettransactionstatus()) {

				List<PreliminaryReportHistory> lstPreliminaryReportHistory = getPreliminaryReportHistoryCount(
						inputMap.get("ncoaparentcode").toString(), userInfo);

				List<Map<String, Object>> lstReportHistory = (List<Map<String, Object>>) returnMap
						.get("systemFileNameAndVersion");

				String coaParentCodeForPreliminaryReport = lstPreliminaryReportHistory.stream()
						.flatMap(pojo -> lstReportHistory.stream().filter(map -> map.containsKey("ncoaparentcode")
								&& pojo.getNcoaparentcode() == Integer.parseInt(map.get("ncoaparentcode").toString()))
								.filter(map -> pojo.getNversionno() < (int) map.get("nversionno"))
								.map(map -> map.get("ncoaparentcode").toString())) // Change "specificKey" to the key
																					// you want to collect
						.collect(Collectors.joining(", "));

				if (!coaParentCodeForPreliminaryReport.isEmpty()) {

					Map<String, Object> tempMap = new HashMap<>();
					tempMap.putAll(inputMap);
					tempMap.put("ncoaparentcode", coaParentCodeForPreliminaryReport);
					preliminaryReport(tempMap, userInfo);
				}

			}
			if ((int) inputMap.get("nclinicaltyperequired") == Enumeration.TransactionStatus.YES
					.gettransactionstatus()) {
				final String getPatientHistorySeqNo = "select nsequenceno from seqnoregistration where stablename in (N'patienthistory')"
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
				int seqNoPatientHistory = jdbcTemplate.queryForObject(getPatientHistorySeqNo, Integer.class);
				inputMap.put("npatienthistorycode", seqNoPatientHistory);
				seqNoPatientHistory = (int) (createPatientHistory(inputMap, userInfo)).get("seqNoPatientHistory");
				jdbcTemplate.execute("update seqnoregistration set nsequenceno=" + seqNoPatientHistory
						+ " where stablename='patienthistory';");
			}
			insertReleaseHistory(inputMap.get("ncoaparentcode").toString(), userInfo, "");

			// Added by sonia on 11-06-2024 for JIRA ID:4360 Auto Download reports
			final String activeQuery = "select sreportno from coaparent where ncoaparentcode in("
					+ inputMap.get("ncoaparentcode") + " ) and ntransactionstatus="
					+ Enumeration.TransactionStatus.RELEASED.gettransactionstatus() + "" + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNtranssitecode();

			final List<Map<String, Object>> reportNo = jdbcTemplate.queryForList(activeQuery); // Added by sonia on
																								// 11-06-2024 for JIRA
																								// ID:4360 Auto Download
																								// reports

			if (inputMap.containsKey("nsampletypecode")
					&& (int) inputMap.get("nsampletypecode") == Enumeration.SampleType.CLINICALSPEC.getType()
					&& inputMap.containsKey("noutsourcerequired")
					&& (int) inputMap.get("noutsourcerequired") == Enumeration.TransactionStatus.YES
							.gettransactionstatus()
					&& ((int) inputMap.get("ncoareporttypecode") == Enumeration.COAReportType.SAMPLEWISE
							.getcoaReportType()
							|| (int) inputMap.get("ncoareporttypecode") == Enumeration.COAReportType.SECTIONWISE
									.getcoaReportType())) {

				inputMap.put("reportNo", reportNo);
				inputMap.put("systemFileName", returnMap.get("systemFileName"));
				inputMap.put("releaseOutsourceAttachmentSeqNo", returnMap.get("releaseOutsourceAttachmentSeqNo"));
				inputMap.put("systemFileNameAndVersion", returnMap.get("systemFileNameAndVersion"));
//				ALPD-5219	Added systemFileNameAndVersion by Vishakh to send data to outsourceCOAReportTransfer method
				outsourceCOAReportTransfer(inputMap, userInfo);
			}

			if((int) inputMap.get("nportalrequired") ==  Enumeration.TransactionStatus.YES.gettransactionstatus())
			{
				String str1 = "select ssettingvalue from settings where nsettingcode=52;";
				String ssettingvalue = jdbcTemplate.queryForObject(str1, String.class);
				if(ssettingvalue.equals("3"))
				{
					String str=" select npreregno,(registration.jsondata->'nexternalordercode')::int as nexternalordercode,jsondata->>'externalorderid' as sexternalorderid, "
							+ Enumeration.TransactionStatus.RELEASED.gettransactionstatus()+ " ntransactionstatus"
							+ " from registration where (registration.jsondata->>'nexternalordercode') IS NOT NULL "
							+ "AND (registration.jsondata->>'nexternalordercode') != 'nexternalordercode' and npreregno in("+inputMap.get("npreregno")
							+ ") and nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
							+ userInfo.getNtranssitecode();
					List<ExternalOrderTest> lstexternalorderstatus = jdbcTemplate.query(str, new ExternalOrderTest());	
					if(lstexternalorderstatus.size()>0) {
						externalOrderSupport.insertExternalOrderStatus(userInfo,lstexternalorderstatus);
					}
				}
			}
			
			Map<String, Object> lstDataTestNew = getReleaseAuditGet(inputMap, userInfo);

			jsonAuditOld.put("registrationtest",
					(List<Map<String, Object>>) ((Map<String, Object>) lstDataTest).get("ReleaseTestAudit"));
			jsonAuditNew.put("registrationtest",
					(List<Map<String, Object>>) ((Map<String, Object>) lstDataTestNew).get("ReleaseTestAudit"));
			auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
			auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
			auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
			actionType.put("registrationtest", "IDS_RELEASESAMPLE");
			returnMap.put("sreportno", reportNo.get(0).get("sreportno"));
			auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, jsonAuditNew, actionType, auditmap, false, userInfo);
			returnMap.put("ncoareporthistorycode", returnMap.get("historySeqNo"));
			returnMap.put("systemFileName", returnMap.get("systemFileName"));

			if (!inputMap.get("ncoaparenttranscode")
					.equals(Enumeration.TransactionStatus.PRELIMINARYRELEASE.gettransactionstatus())) {
				returnMap.putAll((Map<String, Object>) getReleaseSample(inputMap, userInfo).getBody());
			}
			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
			returnMap.put("PortalStatus", objlst);
			String[] ncoaParentCode = inputMap.get("ncoaparentcode").toString().split(",");
			// ALPD-5619 - added by Gowtham R on 28/03/2025 - Mail alert transaction > NULL
			// displayed in reference no column.
			mailMap.put("ssystemid", returnMap.get("sreportno"));

			for (String coaParentCode : ncoaParentCode) {
				mailMap.put("ncoaparentcode", Integer.parseInt(coaParentCode));
				emailDAOSupport.createEmailAlertTransaction(mailMap, mailUserInfo);
			}
			return returnMap;
		} else {
			returnMap.put("isSameCOAParentTransactionStatus", false);
			return returnMap;
		}
	}

	public Map<String, Object> createRegistrationHistory(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		Map<String, Object> returnMap = new HashMap<String, Object>();
		String approvalPreregno = "";
		String approvalSubSampleCode = "";
		int ntestseqno = (int) inputMap.get("ntesthistorycode");
		int nreghistorycode = (int) inputMap.get("nreghistorycode");
		int nsamplehistorycode = (int) inputMap.get("nsamplehistorycode");
		String sQuery = " insert into registrationtesthistory (ntesthistorycode,ntransactiontestcode,ntransactionsamplecode,"
				+ " npreregno,ntransactionstatus,dtransactiondate,nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments,nformcode,"
				+ " nstatus,nsampleapprovalhistorycode,nsitecode)" + " select " + ntestseqno
				+ "+rank()over(order by ntransactiontestcode) ntesthistorycode,ntransactiontestcode,ntransactionsamplecode,npreregno,"
				+ " " + Enumeration.TransactionStatus.RELEASED.gettransactionstatus() + " ntransactionstatus,'"
				+ dateUtilityFunction.getCurrentDateTime(userInfo)// objGeneral.getUTCDateTime()
				+ "' dtransactiondate," + userInfo.getNusercode() + " nusercode," + userInfo.getNuserrole()
				+ " nuserrolecode," + userInfo.getNdeputyusercode() + " ndeputyusercode,"
				+ userInfo.getNdeputyuserrole() + " ndeputyuserrolecode," + " N'"
				+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "' scomments," + userInfo.getNformcode()
				+ " nformcode,1 nstatus," + "-1 nsampleapprovalhistorycode, " + userInfo.getNtranssitecode()
				+ " nsitecode" + " from  registrationtest where ntransactiontestcode in ("
				+ inputMap.get("ntransactiontestcode") + ")" + " and nsitecode = " + userInfo.getNtranssitecode()
				+ " order by ntransactiontestcode;";
		jdbcTemplate.execute(sQuery);

		final String approvedSampleQuery = "select historycount.npreregno from ("
				+ "		select npreregno,count(ntransactiontestcode) testcount from registrationtest where ntransactiontestcode=any("
				+ "			select ntransactiontestcode from registrationtesthistory "
				+ "			where ntransactionstatus=" + Enumeration.TransactionStatus.RELEASED.gettransactionstatus()
				+ " and nsitecode=" + userInfo.getNtranssitecode() + " and npreregno in (" + inputMap.get("npreregno")
				+ ") " + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by npreregno,ntransactiontestcode" + ") and nsitecode=" + userInfo.getNtranssitecode()
				+ "	group by npreregno)historycount,( " + "		select rth.npreregno,count(ntesthistorycode) testcount "
				+ "		from registrationtest rt,registrationtesthistory rth "
				+ "		where ntransactionstatus not in (" + Enumeration.TransactionStatus.RETEST.gettransactionstatus()
				+ "," + Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ") " + " and rth.nsitecode = "
				+ userInfo.getNtranssitecode() + " and rt.nsitecode=" + userInfo.getNtranssitecode()
				+ "		and rt.ntransactiontestcode=rth.ntransactiontestcode "
				+ "		and rth.ntesthistorycode = any( "
				+ "			select max(ntesthistorycode) ntesthistorycode from registrationtesthistory "
				+ "			where npreregno in (" + inputMap.get("npreregno") + ")  and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNtranssitecode() + " 	group by ntransactiontestcode,npreregno)"
				+ "		group by rth.npreregno)testcount where historycount.npreregno=testcount.npreregno "
				+ "and historycount.testcount=testcount.testcount; ";

		final List<RegistrationTestHistory> approvedSampleList = jdbcTemplate.query(approvedSampleQuery,
				new RegistrationTestHistory());

		final String approvedSubSampleQuery = "select historycount.ntransactionsamplecode from ("
				+ "		select ntransactionsamplecode,count(ntransactiontestcode) testcount from registrationtest where ntransactiontestcode=any("
				+ "			select ntransactiontestcode from registrationtesthistory "
				+ "			where ntesthistorycode in (select max(ntesthistorycode) from registrationtesthistory where npreregno in ("
				+ inputMap.get("npreregno") + ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by ntransactionsamplecode, ntransactiontestcode) and ntransactionstatus="
				+ Enumeration.TransactionStatus.RELEASED.gettransactionstatus() + "		) and nsitecode="
				+ userInfo.getNtranssitecode() + " group by ntransactionsamplecode)historycount "
				+ ", (select rth.ntransactionsamplecode,count(rth.ntesthistorycode) testcount from registrationtest rt,registrationtesthistory rth"
				+ " where rth.ntransactionstatus not in (" + Enumeration.TransactionStatus.RETEST.gettransactionstatus()
				+ "," + Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ") and rth.nsitecode = "
				+ userInfo.getNtranssitecode() + " and rt.nsitecode=" + userInfo.getNtranssitecode()
				+ " and rt.ntransactiontestcode=rth.ntransactiontestcode and rth.ntesthistorycode = any("
				+ "select max(ntesthistorycode) ntesthistorycode from registrationtesthistory where npreregno in ("
				+ inputMap.get("npreregno") + ")  and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNtranssitecode() + " group by ntransactiontestcode,ntransactionsamplecode)"
				+ " group by rth.ntransactionsamplecode)testcount " + " where historycount.ntransactionsamplecode"
				+ "=testcount.ntransactionsamplecode and historycount.testcount=testcount.testcount";

		final List<RegistrationTestHistory> approvedSubSampleList = jdbcTemplate.query(approvedSubSampleQuery,
				new RegistrationTestHistory());

		Map<String, Object> seqNoHistoryTable = new HashMap<>();
		if (approvedSampleList.size() > 0) {
			approvalPreregno = approvedSampleList.stream()
					.map(objpreregno -> String.valueOf(objpreregno.getNpreregno())).collect(Collectors.joining(","));

			approvalSubSampleCode = approvedSubSampleList.stream()
					.map(objpreregno -> String.valueOf(objpreregno.getNtransactionsamplecode()))
					.collect(Collectors.joining(","));

			final String strHistoryTableSeqNo = "select (select count(npreregno) from registration where npreregno in ("
					+ approvalPreregno + ") and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode=" + userInfo.getNtranssitecode()
					+ ") nreghistorycount, (select count(ntransactionsamplecode) from registrationsample where ntransactionsamplecode in "
					+ "(" + approvalSubSampleCode + ") and npreregno in (" + approvalPreregno + ") and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNtranssitecode() + ") nsamplehistorycount";
			seqNoHistoryTable = jdbcTemplate.queryForMap(strHistoryTableSeqNo);

			String strRegistrationHistory = " insert into registrationhistory (nreghistorycode,npreregno,ntransactionstatus,dtransactiondate,nusercode,nuserrolecode "
					+ " ,ndeputyusercode,ndeputyuserrolecode,scomments,nsitecode,nstatus,noffsetdtransactiondate,ntransdatetimezonecode) "
					+ " select " + nreghistorycode + "+Rank() over (order by npreregno) as nreghistorycode, "
					+ " npreregno npreregno," + Enumeration.TransactionStatus.RELEASED.gettransactionstatus()
					+ " as ntransactionstatus, '" + dateUtilityFunction.getCurrentDateTime(userInfo)
					+ "' as dtransactiondate," + userInfo.getNusercode() + "," + userInfo.getNuserrole() + ","
					+ userInfo.getNdeputyusercode() + "," + userInfo.getNdeputyuserrole() + ",N'"
					+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "'," + userInfo.getNtranssitecode()
					+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
					+ userInfo.getNtimezonecode() + " from  registration where npreregno in ( " + approvalPreregno
					+ ") and nsitecode = " + userInfo.getNtranssitecode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			jdbcTemplate.execute(strRegistrationHistory);

			String strRegSampleHistory = "insert into registrationsamplehistory (nsamplehistorycode,npreregno,ntransactionsamplecode,ntransactionstatus,dtransactiondate,"
					+ "nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments,nsitecode,nstatus) "
					+ "select " + nsamplehistorycode
					+ "+rank()over(order by ntransactionsamplecode) nsamplehistorycode,npreregno,ntransactionsamplecode,"
					+ Enumeration.TransactionStatus.RELEASED.gettransactionstatus() + " ntransactionstatus," + "'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'  dtransactiondate,"
					+ userInfo.getNusercode() + " nusercode," + userInfo.getNuserrole() + " nuserrolecode," + ""
					+ userInfo.getNdeputyusercode() + " ndeputyusercode," + userInfo.getNdeputyuserrole()
					+ " ndeputyuserrolecode,N'" + stringUtilityFunction.replaceQuote(userInfo.getSreason())
					+ "' scomments," + userInfo.getNtranssitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
					+ "from registrationsample where ntransactionsamplecode in (" + approvalSubSampleCode
					+ ") and npreregno in (" + approvalPreregno + ") and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNtranssitecode() + " order by ntransactionsamplecode;";

			jdbcTemplate.execute(strRegSampleHistory);

			if ((int) inputMap.get("nportalrequired") == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				String str1 = "select settings.ssettingvalue from settings where nsettingcode=52 and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				String ssettingvalue = jdbcTemplate.queryForObject(str1, String.class);
				if (ssettingvalue.equals("3")) {
					String str = " select npreregno,(registration.jsondata->'nexternalordercode')::int as nexternalordercode,jsondata->>'sexternalorderid' as sexternalorderid, "
							+ Enumeration.TransactionStatus.RELEASED.gettransactionstatus() + " ntransactionstatus"
							+ " from registration where (registration.jsondata->>'nexternalordercode') IS NOT NULL "
							+ " AND (registration.jsondata->>'nexternalordercode') != 'nexternalordercode' and npreregno in("
							+ approvalPreregno + ") and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
							+ userInfo.getNtranssitecode();

					List<ExternalOrderTest> lstexternalorderstatus = jdbcTemplate.query(str, new ExternalOrderTest());
					if (lstexternalorderstatus.size() > 0) {
						externalOrderSupport.insertExternalOrderStatus(userInfo, lstexternalorderstatus);
					}

				}
			}
			inputMap.put("approvedSampleList", approvedSampleList);
			inputMap.put("approvalPreregno", approvalPreregno);
			returnMap.putAll(updatePortalStatus(inputMap, userInfo));
			returnMap.put("seqNoHistoryTable", seqNoHistoryTable);
		}
		return returnMap;

	}

	public Map<String, Object> createReportHistory(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		Map<String, Object> returnMap = new HashMap<String, Object>();
		String[] ncoaParentCode = inputMap.get("ncoaparentcode").toString().split(",");

		List<Map<String, Object>> lstMapSystemFileNameAndVersion = new ArrayList<>();

		String reportpresentQuery = "select * from coareporthistory where ncoareporthistorycode in (select max(ncoareporthistorycode) from "
				+ "coareporthistory where ncoaparentcode in (" + inputMap.get("ncoaparentcode") + ") and nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
				+ userInfo.getNtranssitecode() + " group by ncoaparentcode) order by 1 desc";
		List<COAReportHistory> objreportpresent = jdbcTemplate.query(reportpresentQuery, new COAReportHistory());

		List<String> lstParentCodeInReport = new ArrayList<>();
		if (objreportpresent.size() > 0) {
			lstParentCodeInReport = (List<String>) objreportpresent.stream()
					.map(x -> String.valueOf(x.getNcoaparentcode())).collect(Collectors.toList());
		}

		String coaReportHistorySeq = "select stablename,nsequenceno from seqnoreleasemanagement where stablename in ('coareporthistory','releaseoutsourceattachment')"
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		List<SeqNoRegistration> lstSeqNo = jdbcTemplate.query(coaReportHistorySeq, new SeqNoRegistration());

		Map<String, Object> listSeqNo = lstSeqNo.stream().collect(Collectors.toMap(SeqNoRegistration::getStablename,
				SeqNoRegistration -> SeqNoRegistration.getNsequenceno()));

		int historySeqNo = (int) listSeqNo.get("coareporthistory");
		int releaseOutsourceAttachmentSeqNo = (int) listSeqNo.get("releaseoutsourceattachment");

		String strInsertCoaReportHistory = "insert into coareporthistory(ncoareporthistorycode, ncoaparentcode,nversionno,"
				+ " nreportypecode, nreportdetailcode, nusercode, nuserrolecode, sreportcomments, ssystemfilename,"
				+ " dgenerateddate, ntzgenerateddate, noffsetdgenerateddate, dmodifieddate, ntzmodifieddate,"
				+ " noffsetdmodifieddate,nsitecode,nstatus )   values ";

		String insertReportHistoryQuery = "";

		String strInsertCoaReportHistoryGeneration = "insert into coareporthistorygeneration(ncoareporthistorycode, ncoaparentcode,"
				+ "nreportstatus, nversionno, reportinfo, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nsitecode, nstatus )"
				+ " values ";

		String insertReportHistoryGenerationQuery = "";

		UserInfo userInforeport = new UserInfo();

		userInforeport.setNformcode(userInfo.getNformcode());
		userInforeport.setNmastersitecode(userInfo.getNmastersitecode());
		userInforeport.setSreportingtoolfilename(userInfo.getSreportingtoolfilename());
		userInforeport.setNreasoncode(userInfo.getNreasoncode());
		userInforeport.setNtranssitecode(userInfo.getNtranssitecode());
		userInforeport.setNmastersitecode(userInfo.getNmastersitecode());
		userInforeport.setSloginid(userInfo.getSloginid());
		userInforeport.setSlanguagefilename(userInfo.getSlanguagefilename());
		userInforeport.setSlanguagename(userInfo.getSlanguagename());
		userInforeport.setSusername(userInfo.getSusername());
		userInforeport.setSlanguagetypecode(userInfo.getSlanguagetypecode());
		userInforeport.setIsutcenabled(userInfo.getIsutcenabled());
		userInforeport.setSreason(stringUtilityFunction.replaceQuote(userInfo.getSreason())); // ALPD-5566 Added
																								// ReplaceQuote for
																								// reason by Vishakh
																								// (05-04-2025)
		userInforeport.setSdatetimeformat(userInfo.getSdatetimeformat());
		userInforeport.setSuserrolename(userInfo.getSuserrolename());
		userInforeport.setSpredefinedreason(userInfo.getSpredefinedreason());
		userInforeport.setNdeputyuserrole(userInfo.getNdeputyuserrole());
		userInforeport.setNmodulecode(userInfo.getNmodulecode());
		userInforeport.setNusercode(userInfo.getNusercode());
		userInforeport.setNdeputyusercode(userInfo.getNdeputyusercode());

		ObjectMapper objMapper = new ObjectMapper();
		String jsonStr = objMapper.writeValueAsString(userInforeport);

		// Added below code to update existing records of the specified multiple
		// coaparent records with syncstatus as -1 so that only
		// new record created for that coaparent alone will be synced if subsequent
		// correction and release is done between the two //
		// sync intervals.
		final String updateSyncQuery = " update coareporthistorygeneration set nsyncstatus ="
				+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " where ncoaparentcode in ("
				+ inputMap.get("ncoaparentcode") + ") " + " and nsyncstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNtranssitecode();
		LOGGER.info("updateSyncQuery:" + updateSyncQuery);
		jdbcTemplate.execute(updateSyncQuery);

		String systemFileName = "";
		for (String parentCode : ncoaParentCode) {

			Map<String, Object> mapSystemFileName = new HashMap<>();

			// Added by Neeraj on 17-06-2024 for JIRA ID:4291 UUID Name or Report Ref NO.
			if ((int) inputMap.get("reportRefFileName") == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				systemFileName = jdbcTemplate.queryForObject("select sreportno ||'.pdf' from coaparent "
						+ " where ncoaparentcode=" + parentCode + " and nsitecode = " + userInfo.getNtranssitecode()
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
						String.class);
			} else {

				// 04-12-2024 - L. Subashini - Moved the UUID- combination creation code inside
				// loop to handle multi release
				// Added by Neeraj on 17-06-2024 for JIRA ID:4291 UUID Name or Report Ref NO.
				final String currentDateTime = dateUtilityFunction.getCurrentDateTime(userInfo).toString()
						.replace("T", " ").replace("Z", "");
				final String concat = String.valueOf(userInfo.getNtranssitecode()) + currentDateTime;

				systemFileName = UUID.nameUUIDFromBytes(concat.getBytes()).toString() + "_" + parentCode + ".pdf";

			}
			int nversionno = 0;
			if (objreportpresent.size() > 0 && lstParentCodeInReport.contains(parentCode)) {

				String sVersionQuery = "select max(nversionno) from coareporthistory where ncoaparentcode=" + parentCode
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsitecode=" + userInfo.getNtranssitecode();
				nversionno = jdbcTemplate.queryForObject(sVersionQuery, Integer.class);
				nversionno = nversionno + 1;
			}
			historySeqNo = historySeqNo + 1;

			insertReportHistoryQuery = insertReportHistoryQuery + "(" + historySeqNo + "," + parentCode + ","
					+ nversionno + "," + inputMap.get("nreporttypecode") + " ," + " "
					+ inputMap.get("nreportdetailcode") + "," + userInfo.getNusercode() + "," + userInfo.getNuserrole()
					+ ",N'" + stringUtilityFunction.replaceQuote(inputMap.get("sreportcomments").toString()) + "','"
					+ systemFileName + "','" + dateUtilityFunction.getCurrentDateTime(userInfo) + "' ,"
					+ userInfo.getNtimezonecode() + ","
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + " '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' ," + userInfo.getNtimezonecode() + ","
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + " ,"
					+ userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "),";

			insertReportHistoryGenerationQuery = insertReportHistoryGenerationQuery + "(" + historySeqNo + ","
					+ parentCode + "," + Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + "," + nversionno
					+ ",'" + jsonStr + "', '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
					+ userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "),";

			mapSystemFileName.put("ncoaparentcode", parentCode);
			mapSystemFileName.put("systemFileName", systemFileName);
			mapSystemFileName.put("nversionno", nversionno);

			lstMapSystemFileNameAndVersion.add(mapSystemFileName);

		}
		releaseOutsourceAttachmentSeqNo++;

		insertReportHistoryQuery = insertReportHistoryQuery.substring(0, insertReportHistoryQuery.length() - 1) + ";";

		insertReportHistoryGenerationQuery = insertReportHistoryGenerationQuery.substring(0,
				insertReportHistoryGenerationQuery.length() - 1) + ";";

		String coaReportHistorySeqno = "update seqnoreleasemanagement set nsequenceno=" + historySeqNo
				+ " where stablename='coareporthistory'" + "and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		jdbcTemplate.execute(strInsertCoaReportHistory + insertReportHistoryQuery + strInsertCoaReportHistoryGeneration
				+ insertReportHistoryGenerationQuery + coaReportHistorySeqno);

		returnMap.put("ReportPDFFile", returnMap.get("outputFilename"));
		returnMap.put("historySeqNo", historySeqNo);
		returnMap.put("ncoareporthistorycode", historySeqNo);
		returnMap.put("systemFileName", systemFileName);
		returnMap.put("releaseOutsourceAttachmentSeqNo", releaseOutsourceAttachmentSeqNo);
		returnMap.put("systemFileNameAndVersion", lstMapSystemFileNameAndVersion);

		return returnMap;

	}

	public Map<String, Object> updatePortalStatus(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		Map<String, Object> returnMap = new HashMap<String, Object>();
		List<Map<String, Object>> objlst = new ArrayList<Map<String, Object>>();
		List<RegistrationTestHistory> approvedSampleList = (List<RegistrationTestHistory>) inputMap
				.get("approvedSampleList");

		String isPortal = "select nportalrequired from sampletype where nsampletypecode = "
				+ inputMap.get("nsampletypecode") + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		SampleType objPortal = (SampleType) jdbcUtilityFunction.queryForObject(isPortal, SampleType.class,
				jdbcTemplate);

		if (objPortal.getNportalrequired() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

			String strRegistrationQuery = "select npreregno from registration where npreregno in ("
					+ inputMap.get("approvalPreregno") + ") and jsonuidata->>'nexternalordertypecode'::text = '"
					+ Enumeration.ExternalOrderType.PORTAL.getExternalOrderType() + "' and nsampletypecode="
					+ inputMap.get("nsampletypecode") + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
					+ userInfo.getNtranssitecode();

			List<Integer> lstPreRegNo = jdbcTemplate.queryForList(strRegistrationQuery, Integer.class);

			if (lstPreRegNo.size() > 0) {

				Object PreregNumber = lstPreRegNo.stream().map(x -> String.valueOf(x)).collect(Collectors.joining(","));
				inputMap.put("allPreregno", PreregNumber);
				final Map<String, Object> registrationDatas = registrationDAOSupport.getRegistrationLabel(inputMap,
						userInfo);
				returnMap.put("isPortalData",
						registrationDatas.containsKey("isPortalData") ? registrationDatas.get("isPortalData") : false);
				if (!registrationDatas.isEmpty()) {
					Map<String, Object> objMap = new LinkedHashMap<String, Object>();
					for (int k = 0; k < approvedSampleList.size(); k++) {
						Map<String, Object> registrationData = (Map<String, Object>) registrationDatas
								.get(String.valueOf(approvedSampleList.get(k).getNpreregno()));
						// If condition for null added by L.Subashini 11-12-2024
						if (registrationData != null) {
							objMap.put("serialnumber", registrationData.get("sorderseqno"));
							objMap.put("statuscode",
									(int) Enumeration.TransactionStatus.RELEASED.gettransactionstatus());
							objlst.add(objMap);
						}
					}
					JSONObject obj = new JSONObject();
					obj.put("statsary", objlst);
					obj.put("url", inputMap.get("url").toString());
				}
				inputMap.put("PortalStatus", objlst);
				externalOrderDAO.updateOrderSampleStatus(userInfo, inputMap);
			}
		}
		return returnMap;

	}

	public Map<String, Object> getPdfReportForMrt1(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		Map<String, Object> returnMap = new HashMap<String, Object>();
		Map<String, Object> jasperParameter = new HashMap<String, Object>();
		File JRXMLFile = (File) inputMap.get("JRXMLFile");

		jasperParameter.put("npreregno", inputMap.get("npreregno"));
		jasperParameter.put("nsectioncode", inputMap.get("nsectioncode"));
		jasperParameter.put("ncoaparentcode", inputMap.get("ncoaparentcode"));
		jasperParameter.put("ssubreportpath",
				inputMap.get("subReportPath").toString() + inputMap.get("folderName").toString());
		jasperParameter.put("simagepath", inputMap.get("imagePath").toString() + inputMap.get("folderName").toString());
		jasperParameter.put("sarno", inputMap.get("sarno"));
		jasperParameter.put("needlogo", inputMap.get("naccredit"));
		jasperParameter.put("nregtypecode", inputMap.get("nregTypeCode"));
		jasperParameter.put("nregsubtypecode", inputMap.get("nregSubTypeCode"));
		jasperParameter.put("sreportcomments", inputMap.get("sreportComments"));
		jasperParameter.put("sprimarykeyname", inputMap.get("ncoaparentcode"));
		jasperParameter.put("nlanguagecode", userInfo.getSlanguagetypecode());
		jasperParameter.put("nreporttypecode", (int) inputMap.get("nreporttypecode"));// janakumar
		jasperParameter.put("nsitecode", (int) userInfo.getNtranssitecode());
		jasperParameter.put("nreportdetailcode", inputMap.get("nreportdetailcode"));

		String sFile = FilenameUtils.getExtension(JRXMLFile.toString());
		boolean isFTPStatus = false;

		final String getFileuploadpath = "select ssettingvalue from settings   where" + " nsettingcode in ("
				+ Enumeration.Settings.REPORT_OPEN_NEW_TAB.getNsettingcode() + ")  and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nsettingcode ";

		final List<String> reportPathsValues = jdbcTemplate.queryForList(getFileuploadpath, String.class);

		if (!reportPathsValues.isEmpty()) {
			final int reportViewerORNot = Integer.parseInt(reportPathsValues.get(1));// 70

			if (reportViewerORNot == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

				isFTPStatus = false;
			} else {
				isFTPStatus = true;
				jasperParameter.put("sreportingtoolURL", inputMap.get("sreportingtoolURL"));
			}

		} else {
			isFTPStatus = true;
			jasperParameter.put("sreportingtoolURL", inputMap.get("sreportingtoolURL"));
		}

		returnMap.putAll(reportDAOSupport.compileAndPrintReport(jasperParameter, JRXMLFile, (int) inputMap.get("qType"),
				inputMap.get("pdfPath").toString(), inputMap.get("sfileName").toString(), userInfo,
				inputMap.get("systemFileName").toString(), (int) inputMap.get("ncontrolcode"), isFTPStatus,
				inputMap.get("ssignfilename").toString(), inputMap.get("scertfilename").toString(),
				inputMap.get("ssecuritykey").toString()));

		String uploadStatus = (String) returnMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());

		if (sFile.equals("jrxml")) {
			returnMap.put("filetype", "jrxml");
		}
		if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(uploadStatus)) {

			String auditAction = "";

			String comments = commonFunction.getMultilingualMessage("IDS_REPORTNO", userInfo.getSlanguagefilename())
					+ ": " + inputMap.get("sreportno") + "; ";

			comments = comments + commonFunction.getMultilingualMessage("IDS_FILENAME", userInfo.getSlanguagefilename())
					+ ": " + returnMap.get("outputFilename") + "; ";

			ResourceBundle resourcebundle = commonFunction.getResourceBundle(userInfo.getSlanguagefilename(), false);

			String syncscomments = resourcebundle.containsKey("IDS_SYNCATTEMPT")
					? resourcebundle.getString("IDS_SYNCATTEMPT") + ": " + resourcebundle.getString("IDS_AUTOSYNC")
							+ ";"
					: "Sync Attempted : " + " No Data Modified" + "; ";

			Map<String, Object> outputMap = new HashMap<>();
			outputMap.put("stablename", "coaparent");
			outputMap.put("sprimarykeyvalue", inputMap.get("ncoaparentcode"));

			if (Boolean.TRUE.equals(inputMap.get("auditstatus")) && inputMap.get("syncAction").equals("autoSync")) {

				boolean isSync = true;
				outputMap.put("isSync", isSync);
				outputMap.put("dataStatus", "IDS_AUTOSYNC");
				outputMap.put("syncscomments", syncscomments);
				auditAction = "IDS_AUTOSYNC";
				auditUtilityFunction.insertAuditAction(userInfo, auditAction, syncscomments, outputMap);

			} else if (Boolean.FALSE.equals(inputMap.get("auditstatus"))
					&& inputMap.get("syncAction").equals("manualsync")) {

				LOGGER.error("error: Maunal Audit was already captured.");

			} else {
				if (inputMap.get("action").equals("Regenerate")) {
					auditAction = "IDS_REPORTREGENERATED";
				} else {
					auditAction = "IDS_PREVIEWFINALREPORT";
				}
				auditUtilityFunction.insertAuditAction(userInfo, auditAction, comments, outputMap);
			}
			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
			returnMap.put("ReportPDFFile", returnMap.get("outputFilename"));

		}

		// ALPD-5116 Release&Report ->View the report in the new tab

		if (!reportPathsValues.isEmpty()) {
			final int generateReportINrelease = Integer.parseInt(reportPathsValues.get(0));
			final int reportViewerORNot = Integer.parseInt(reportPathsValues.get(1));
			if (generateReportINrelease == Enumeration.TransactionStatus.YES.gettransactionstatus()
					&& reportViewerORNot == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				if (returnMap.get("sourceparameter") != null) {

					final ObjectMapper objectMapper = new ObjectMapper();

					final String jsonString = (String) returnMap.get("sourceparameter");

					final Map<String, Object> sourceParameter = objectMapper.readValue(jsonString,
							new TypeReference<Map<String, Object>>() {
							});

					sourceParameter.remove("sprimarykeyname");
					sourceParameter.remove("sarno");
					sourceParameter.remove("nregtypecode");
					sourceParameter.remove("nregsubtypecode");
					sourceParameter.remove("nsitecode");
					sourceParameter.remove("nlanguagecode");
					sourceParameter.remove("nsectioncode");

					sourceParameter.put("ndecisionstatus", -1);
					sourceParameter.put("language", userInfo.getSlanguagetypecode());

					returnMap.put("sourceparameter", sourceParameter);
				}
			}
		}

		return returnMap;

	}

//	ALPD-5219 Modified code by Vishakh for Outsource work for multiple parent release
	public void outsourceCOAReportTransfer(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		final String strReleaseDateDetails = ", dreleasedate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
				+ "', noffsetdreleasedate = " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
				+ ", nreleasedatetimezonecode=" + userInfo.getNtimezonecode() + " ";
		Map<String, Object> mapDetails = new HashMap<>();

		List<Map<String, Object>> releaseOutsourceAttachRecords = null;
		int finalReleaseOutsourceAttachmentSeqNo = (int) inputMap.get("releaseOutsourceAttachmentSeqNo");

		if (inputMap.get("action").equals("Regenerate")) {
			releaseOutsourceAttachRecords = jdbcTemplate
					.queryForList("select * from releaseoutsourceattachment where ncoaparentcode="
							+ inputMap.get("ncoaparentcode") + " and nformcode=" + userInfo.getNformcode()
							+ " and nversionno=" + (int) inputMap.get("nversionno") + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
							+ userInfo.getNtranssitecode());
		}

		mapDetails.put("releaseOutsourceAttachmentSeqNo", inputMap.get("releaseOutsourceAttachmentSeqNo"));
		mapDetails.put("finalReleaseOutsourceAttachmentSeqNo", finalReleaseOutsourceAttachmentSeqNo);

		if (inputMap.get("action").equals("Generatefile")) {
			mapDetails.put("releaseDateDetails", strReleaseDateDetails);
		}

		mapDetails.put("systemFileNameAndVersion", inputMap.get("systemFileNameAndVersion"));

		Map<String, Object> mapCOAReportHistory = (Map<String, Object>) insertUpdateReleaseAttachment(mapDetails,
				inputMap, userInfo);
		List<COAReportHistory> lstCOAReportHistory = (List<COAReportHistory>) mapCOAReportHistory
				.get("lstReleaseTestAttachment");
		mapDetails.put("coareleasedate", lstCOAReportHistory);
		mapDetails.put("finalReleaseOutsourceAttachmentSeqNo",
				(int) mapCOAReportHistory.get("releaseTestAttachmentSeqNo"));
		mapDetails.put("sexternalordercode", mapCOAReportHistory.get("sexternalordercode"));

		if ((inputMap.get("action").equals("Generatefile"))
				|| (inputMap.get("action").equals("Regenerate") && releaseOutsourceAttachRecords.size() == 0)) {
			sendReportToParentSites(mapDetails, inputMap, userInfo);
		}
	}

//	ALPD-5219 Modified codes by Vishakh because of outsource work for multiple parent release
	public Map<String, Object> insertUpdateReleaseAttachment(Map<String, Object> mapDetails,
			Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		Map<String, Object> rtnMap = new HashMap<String, Object>();

		final String str1 = "select max(r.jsonuidata->>'OrderCodeData') nexternalordercode, max(r.jsonuidata->>'OrderIdData') sexternalorderid, "
				+ " cp.ncoaparentcode,cc.npreregno,cp.sreportno,max(coh.ssystemfilename) ssystemfilename,max(nversionno) nversionno "
				+ " from "
				+ " coaparent cp JOIN coareporthistory coh ON coh.ncoaparentcode = cp.ncoaparentcode and coh.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and coh.nsitecode = "
				+ userInfo.getNtranssitecode() + " and cp.nsitecode = " + userInfo.getNtranssitecode()
				+ " JOIN coachild cc ON cc.ncoaparentcode = cp.ncoaparentcode and cc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cc.nsitecode = "
				+ userInfo.getNtranssitecode() + " and cp.ncoaparentcode in (" + inputMap.get("ncoaparentcode") + ") "
				+ " JOIN registration r on cc.npreregno = r.npreregno and r.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and r.nsitecode = "
				+ userInfo.getNtranssitecode()
				+ " where ncoareporthistorycode = any(select max(ncoareporthistorycode) from coareporthistory "
				+ " where cp.ncoaparentcode=ncoaparentcode and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
				+ userInfo.getNtranssitecode() + ") "
				+ " group by r.npreregno,cp.ncoaparentcode,cc.npreregno,cp.sreportno;";
		List<COAReportHistory> lstReleaseTestAttachment = jdbcTemplate.query(str1, new COAReportHistory());

		int seqno = (int) mapDetails.get("releaseOutsourceAttachmentSeqNo");
		String strReleaseOutsourceAttachment = "";
		String sexternalOrderCode = lstReleaseTestAttachment.stream()
				.map(x -> String.valueOf(x.getNexternalordercode())).collect(Collectors.joining(","));

		List<Map<String, Object>> lstMapExternalOrder = jdbcTemplate.queryForList(
				" select nexternalordercode, cast(count(nexternalordercode)as int) count from externalorderattachment"
						+ " where nexternalordercode in (" + sexternalOrderCode + ") and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
						+ userInfo.getNtranssitecode() + " group by nexternalordercode ");

		Map<Integer, Integer> mapOrderNoAndCount = lstMapExternalOrder.stream()
				.collect(Collectors.toMap(map -> (int) map.get("nexternalordercode"), map -> (int) map.get("count")));

		String scoaparentcode = lstReleaseTestAttachment.stream().map(x -> String.valueOf(x.getNcoaparentcode()))
				.collect(Collectors.joining(","));
		String sversionNo = lstReleaseTestAttachment.stream().map(x -> String.valueOf(x.getNversionno()))
				.collect(Collectors.joining(","));

		List<Map<String, Object>> lstMapSeqNoUpdate = jdbcTemplate
				.queryForList("select ncoaparentcode, nformcode, nversionno, nexternalordercode"
						+ " from releaseoutsourceattachment where ncoaparentcode in (" + scoaparentcode
						+ ") and nformcode=" + userInfo.getNformcode() + " and nversionno in (" + sversionNo
						+ ") and nexternalordercode in (" + sexternalOrderCode + ")" + " and nsitecode = "
						+ userInfo.getNtranssitecode());

		for (int i = 0; i < lstReleaseTestAttachment.size(); i++) {
			final int ncoaparentCode = lstReleaseTestAttachment.get(i).getNcoaparentcode();
			final int nexternalOrderCode = lstReleaseTestAttachment.get(i).getNexternalordercode();
			final int nversionNo = lstReleaseTestAttachment.get(i).getNversionno();

			String str = "select " + seqno + "," + lstReleaseTestAttachment.get(i).getNexternalordercode() + ",'"
					+ lstReleaseTestAttachment.get(i).getSexternalorderid() + "',"
					+ lstReleaseTestAttachment.get(i).getNcoaparentcode() + ", " + " "
					+ lstReleaseTestAttachment.get(i).getNcoaparentcode() + ","
					+ lstReleaseTestAttachment.get(i).getNpreregno() + "," + userInfo.getNformcode() + ","
					+ userInfo.getNusercode() + "," + userInfo.getNuserrole() + ",'"
					+ lstReleaseTestAttachment.get(i).getSreportno() + "'," + " '"
					+ lstReleaseTestAttachment.get(i).getSsystemfilename() + "', " + " timestamp '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ").replace("Z", "")
					+ "'," + " " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
					+ userInfo.getNtimezonecode() + "," + " timestamp '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ").replace("Z", "")
					+ "'," + " " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
					+ userInfo.getNtimezonecode() + "," + userInfo.getNtranssitecode() + "," + " "
					+ userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "," + lstReleaseTestAttachment.get(i).getNversionno() + "  "
					+ (((int) lstReleaseTestAttachment.get(i).getNversionno() == 0) ? "" : ";") + " ";

			int strSeqNoLatest = 0;

			if ((int) lstReleaseTestAttachment.get(i).getNversionno() == 0) {
				str += " union all select " + seqno + "+Rank()over(order by nexternalorderattachmentcode)"
						+ ", nexternalordercode, sexternalorderid,  "
						+ lstReleaseTestAttachment.get(i).getNcoaparentcode() + ", nsourcecoaparentcode" + ", "
						+ lstReleaseTestAttachment.get(i).getNpreregno() + ", -1, nusercode, nuserrolecode, "
						+ "sreleaseno, ssystemfilename, dreleasedate, noffsetdreleasedate, nreleasedatetimezonecode, timestamp '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ").replace("Z", "")
						+ "', " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ userInfo.getNtimezonecode() + ", " + userInfo.getNtranssitecode() + ", nsourcesitecode, "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ ", nversionno from externalorderattachment where nexternalordercode = "
						+ lstReleaseTestAttachment.get(i).getNexternalordercode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode  = "
						+ userInfo.getNtranssitecode() + ";";
				strSeqNoLatest = mapOrderNoAndCount.isEmpty() ? 0
						: mapOrderNoAndCount.get(lstReleaseTestAttachment.get(i).getNexternalordercode());
			}

			strReleaseOutsourceAttachment = strReleaseOutsourceAttachment
					+ " DO $$ BEGIN if exists (select * from releaseoutsourceattachment where ncoaparentcode = "
					+ lstReleaseTestAttachment.get(i).getNcoaparentcode() + " and nformcode=" + userInfo.getNformcode()
					+ " and nversionno=" + lstReleaseTestAttachment.get(i).getNversionno() + " and nexternalordercode="
					+ lstReleaseTestAttachment.get(i).getNexternalordercode() + " and nsitecode = "
					+ userInfo.getNtranssitecode() + ") THEN  update releaseoutsourceattachment set nusercode="
					+ userInfo.getNusercode() + ", nuserrolecode=" + userInfo.getNuserrole() + ", ssystemfilename='"
					+ lstReleaseTestAttachment.get(i).getSsystemfilename() + "', dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ ", ntransdatetimezonecode=" + userInfo.getNtimezonecode() + ", nsitecode="
					+ userInfo.getNtranssitecode() + ", nsourcesitecode=" + userInfo.getNtranssitecode() + ", nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ", sreleaseno ='"
					+ lstReleaseTestAttachment.get(i).getSreportno() + "'"
					+ (mapDetails.containsKey("releaseDateDetails") ? mapDetails.get("releaseDateDetails").toString()
							: "")
					+ " where ncoaparentcode = " + lstReleaseTestAttachment.get(i).getNcoaparentcode()
					+ " and nformcode =" + userInfo.getNformcode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nversionno="
					+ lstReleaseTestAttachment.get(i).getNversionno() + " and nsitecode = "
					+ userInfo.getNtranssitecode() + "; ELSE "
					+ " insert into releaseoutsourceattachment(nreleaseoutsourceattachcode, nexternalordercode, sexternalorderid, ncoaparentcode, nsourcecoaparentcode, "
					+ " npreregno, nformcode, nusercode, nuserrolecode, sreleaseno, ssystemfilename, dreleasedate, noffsetdreleasedate, nreleasedatetimezonecode, dtransactiondate, noffsetdtransactiondate,"
					+ " ntransdatetimezonecode, nsitecode, nsourcesitecode, nstatus, nversionno) " + str
					+ " END IF; END; $$; ";

			if (!lstMapSeqNoUpdate.stream()
					.anyMatch(map -> (int) map.get("ncoaparentcode") == ncoaparentCode
							&& (int) map.get("nversionno") == nversionNo
							&& (int) map.get("nexternalordercode") == nexternalOrderCode)) {
				seqno = seqno + strSeqNoLatest + 1;
			}

			if ((lstReleaseTestAttachment.size() - 1) == i) {
				if (!inputMap.get("action").equals("Regenerate")) {
					strReleaseOutsourceAttachment += " update seqnoreleasemanagement set nsequenceno=" + seqno
							+ " where stablename='releaseoutsourceattachment';";
					seqno = seqno - 1;
				}
			}
		}

		jdbcTemplate.execute(strReleaseOutsourceAttachment);

		rtnMap.put("lstReleaseTestAttachment", lstReleaseTestAttachment);
		rtnMap.put("releaseTestAttachmentSeqNo", seqno);
		rtnMap.put("sexternalordercode", sexternalOrderCode);
		return rtnMap;
	}

//	ALPD-5219 Modified codes by Vishakh because of outsource work for multiple parent release
	public void sendReportToParentSites(Map<String, Object> mapDetails, Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		List<COAReportHistory> lsCOArelease = (List<COAReportHistory>) mapDetails.get("coareleasedate");

		String strReportToParentSites = "";
		int seqno = (int) mapDetails.get("finalReleaseOutsourceAttachmentSeqNo");

		String scoaparentcode = lsCOArelease.stream().map(x -> String.valueOf(x.getNcoaparentcode()))
				.collect(Collectors.joining(","));

		List<Map<String, Object>> lstMapAttachCode = jdbcTemplate
				.queryForList("select nreleaseoutsourceattachcode, nexternalordercode, ncoaparentcode"
						+ " from releaseoutsourceattachment where nexternalordercode in ("
						+ mapDetails.get("sexternalordercode") + ") and nsourcesitecode <> "
						+ userInfo.getNtranssitecode() + " and ncoaparentcode in (" + scoaparentcode + ") and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
						+ userInfo.getNtranssitecode() + ";");

		for (int i = 0; i < lsCOArelease.size(); i++) {
			final int nexternalOrderCode = (int) lsCOArelease.get(i).getNexternalordercode();
			final int ncoaParentCode = (int) lsCOArelease.get(i).getNcoaparentcode();

			strReportToParentSites = strReportToParentSites
					+ "insert into releaseoutsourceattachment (nreleaseoutsourceattachcode, nexternalordercode, sexternalorderid, ncoaparentcode, nsourcecoaparentcode, "
					+ " npreregno, nformcode, nusercode, nuserrolecode, sreleaseno, ssystemfilename, dreleasedate, noffsetdreleasedate, nreleasedatetimezonecode, dtransactiondate, noffsetdtransactiondate,"
					+ " ntransdatetimezonecode, nsitecode, nsourcesitecode, nstatus, nversionno) select " + seqno
					+ "+Rank()over(order by nreleaseoutsourceattachcode), nexternalordercode, "
					+ "sexternalorderid, nsourcecoaparentcode, " + ncoaParentCode + ", "
					+ lsCOArelease.get(i).getNpreregno() + ", -1, nusercode, nuserrolecode, '"
					+ lsCOArelease.get(i).getSreportno() + "', '" + lsCOArelease.get(i).getSsystemfilename() + "', "
					+ "'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNtimezonecode() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNtimezonecode() + ", nsourcesitecode, " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ", "
					+ lsCOArelease.get(i).getNversionno() + " from releaseoutsourceattachment where"
					+ " nexternalordercode=" + nexternalOrderCode + " and nsourcesitecode <> "
					+ userInfo.getNtranssitecode() + " and ncoaparentcode=" + ncoaParentCode + " and nsitecode = "
					+ userInfo.getNtranssitecode() + ";";
			int count = (int) lstMapAttachCode.stream()
					.filter(map -> (int) map.get("nexternalordercode") == nexternalOrderCode
							&& (int) map.get("ncoaparentcode") == ncoaParentCode)
					.count();
			seqno += count;
		}

		strReportToParentSites = strReportToParentSites + "update  seqnoreleasemanagement set nsequenceno=" + seqno
				+ " where stablename='releaseoutsourceattachment' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		jdbcTemplate.execute(strReportToParentSites);
	}

	@Override
	public ResponseEntity<Object> viewReportHistory(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		Map<String, Object> returnMap = new HashMap<>();
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		String strReleaseAttachment = "select ra.*, TO_CHAR(ra.dreleasedate ,'" + userInfo.getSpgdatetimeformat()
				+ "') sreleasedate, concat(u.sfirstname,' ',u.slastname) as susername, "
				+ Enumeration.AttachmentType.FTP.gettype()
				+ " as nattachmenttypecode, s.ssitename from releaseoutsourceattachment ra, site s, users u where ra.ncoaparentcode="
				+ inputMap.get("ncoaparentcode") + " and ra.nsourcesitecode=s.nsitecode and s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ra.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ra.nsitecode ="
				+ userInfo.getNtranssitecode() + " and u.nsitecode = " + userInfo.getNmastersitecode()
				+ " and u.nusercode=ra.nusercode order by ra.dreleasedate desc";
		List<Map<String, Object>> lstReleaseAttachment = jdbcTemplate.queryForList(strReleaseAttachment);

		final List<ReleaseOutsourceAttachment> lstUTCConvertedDate = objMapper.convertValue(
				dateUtilityFunction.getSiteLocalTimeFromUTC(lstReleaseAttachment, Arrays.asList("sreleasedate"),
						Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null, false),
				new TypeReference<List<ReleaseOutsourceAttachment>>() {
				});
		returnMap.put("PatientReports", lstUTCConvertedDate);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public Map<String, Object> viewReleasedCOAReport(ReleaseOutsourceAttachment objReleaseCOAReport, int ncontrolCode,
			UserInfo userInfo) throws Exception {
		Map<String, Object> map = new HashMap<>();
		String sQuery = "select * from releaseoutsourceattachment where nreleaseoutsourceattachcode ="
				+ objReleaseCOAReport.getNreleaseoutsourceattachcode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and nsitecode="
				+ userInfo.getNtranssitecode() + " ";
		final ReleaseOutsourceAttachment objReleasedCOAReport = (ReleaseOutsourceAttachment) jdbcUtilityFunction
				.queryForObject(sQuery, ReleaseOutsourceAttachment.class, jdbcTemplate);
		if (objReleasedCOAReport != null) {
			map = ftpUtilityFunction.FileViewUsingFtp(objReleasedCOAReport.getSsystemfilename(), -1, userInfo, "", "");
			auditUtilityFunction.fnInsertAuditAction(Arrays.asList(objReleaseCOAReport), 1, null,
					Arrays.asList("IDS_VIEWRELEASEDCOAREPORT"), userInfo);

		}
		return map;
	}

	public List<ReleaseTestAttachment> insertReleaseTestAttachment(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		final String releaseTestAttachmentSeqNo = "select nsequenceno from seqnoreleasemanagement where stablename='releasetestattachment' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final int releaseTestAttachSeqNo = jdbcTemplate.queryForObject(releaseTestAttachmentSeqNo, Integer.class);

		final String strReleaseTestAttachment = "insert into releasetestattachment (nreleasetestattachmentcode, ncoaparentcode, ntransactiontestcode, ntransactionsamplecode, npreregno, nformcode, nattachmenttypecode, nlinkcode, nusercode, nuserrolecode, jsondata, dtransactiondate, ntransdatetimezonecode, noffsetdtransactiondate, nsitecode, nstatus) "
				+ "select " + releaseTestAttachSeqNo + "+Rank()over(order by ntestattachmentcode), "
				+ inputMap.get("ncoaparentcode")
				+ ", ntransactiontestcode, ntransactionsamplecode, npreregno, nformcode, nattachmenttypecode, nlinkcode, nusercode, nuserrolecode, "
				+ " jsonb_set(jsondata, '{nsortorder}', to_jsonb(row_number() OVER (PARTITION BY npreregno, ntransactionsamplecode, ntransactiontestcode ORDER BY jsondata->'dcreateddate'))), '"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
				+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", nsitecode, "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " from registrationtestattachment where ntransactiontestcode in ("
				+ inputMap.get("ntransactiontestcode") + ") and ntransactionsamplecode in ("
				+ inputMap.get("ntransactionsamplecode") + ") and npreregno in (" + inputMap.get("npreregno").toString()
				+ ") and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
				+ userInfo.getNtranssitecode() + " and nattachmenttypecode=" + Enumeration.AttachmentType.FTP.gettype()
				+ ";"
				+ " update seqnoreleasemanagement set nsequenceno=(select coalesce((select max(nreleasetestattachmentcode) from releasetestattachment), 0)) where stablename='releasetestattachment' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
		;

		jdbcTemplate.execute(strReleaseTestAttachment);

		return null;

	}

	@Override
	public ResponseEntity<Object> getReleaseTestAttachment(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		Map<String, Object> returnMap = new HashMap<>();
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final String strReleaseTestAttachment = "select rta.nreleasetestattachmentcode, rta.jsondata->>'stestsynonym' stestsynonym, "
				+ "to_char((rta.jsondata->>'dcreateddate')::timestamp,'" + userInfo.getSpgdatetimeformat()
				+ "') screateddate, ran.sarno, rsa.ssamplearno, qf.jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode() + "' "
				+ "sformname, rta.jsondata->>'susername' susername, rta.jsondata->>'suserrolename' suserrolename, rta.jsondata->>'sdescription' "
				+ "sdescription, rta.jsondata->>'nneedreport' nneedreport, rta.jsondata->>'sfilename' sfilename, rta.jsondata->>'nfilesize' "
				+ "nfilesize, CASE WHEN rta.jsondata->>'slinkname' = '' THEN '-' ELSE rta.jsondata->>'slinkname' END slinkname, rta.ncoaparentcode, rta.ntransactiontestcode, rta.ntransactionsamplecode, "
				+ "rta.npreregno, rta.nformcode, rta.nattachmenttypecode, rta.nlinkcode, rta.nusercode, rta.nuserrolecode, rta.nsitecode, rta.nstatus, "
				+ "CASE WHEN rta.jsondata->>'nneedreport' = '3' THEN 'Yes' ELSE 'No' END sneedreport ,rta.jsondata->>'sheader' sheader,coalesce(rta.jsondata->>'nsortorder', '0') nsortorder  "
				+ " from releasetestattachment rta, registrationarno ran, registrationsamplearno rsa, qualisforms qf where ran.npreregno = rta.npreregno"
				+ " and rsa.ntransactionsamplecode=rta.ntransactionsamplecode and rta.nformcode=qf.nformcode and rta.ncoaparentcode="
				+ inputMap.get("ncoaparentcode") + " and rta.nsitecode=" + userInfo.getNtranssitecode()
				+ " and ran.nsitecode=" + userInfo.getNtranssitecode() + " and rsa.nsitecode="
				+ userInfo.getNtranssitecode() + " and rta.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ran.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rsa.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and qf.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by rta.nreleasetestattachmentcode desc";
		List<ReleaseTestAttachment> lstReleaseTestAttachment = jdbcTemplate.query(strReleaseTestAttachment,
				new ReleaseTestAttachment());

		final List<?> lstUTCConvertedDate = dateUtilityFunction.getSiteLocalTimeFromUTC(lstReleaseTestAttachment,
				Arrays.asList("screateddate"), null, userInfo, false, null, false);

		returnMap.put("ReleaseTestAttachmentDetails", lstUTCConvertedDate);
		returnMap.putAll((Map<String, Object>) getDetailsBasedOnCOAParent(inputMap, userInfo).getBody());
		return new ResponseEntity<>(returnMap, HttpStatus.OK);

	}

	@Override
	public ReleaseTestAttachment getActiveReleaseTestAttachmentById(int nreleaseTestAttachmentCode, UserInfo userInfo)
			throws Exception {

		final String strReleaseTestAttachment = "select cp.ntransactionstatus, rta.*, rta.jsondata->>'nneedreport' nneedreport, rta.jsondata->>'sdescription' sdescription,"
				+ " rta.jsondata->>'dcreateddate' screateddate, rta.jsondata->>'sfilename' sfilename, rta.jsondata->>'ssystemfilename' ssystemfilename,rta.jsondata->>'sheader' sheader,coalesce(rta.jsondata->>'nsortorder', '0') nsortorder"
				+ " from releasetestattachment rta, coaparent cp where" + " rta.nreleasetestattachmentcode="
				+ nreleaseTestAttachmentCode + " and rta.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rta.nsitecode="
				+ userInfo.getNtranssitecode() + " and rta.ncoaparentcode=cp.ncoaparentcode and cp.nsitecode="
				+ userInfo.getNtranssitecode();
		return (ReleaseTestAttachment) jdbcUtilityFunction.queryForObject(strReleaseTestAttachment,
				ReleaseTestAttachment.class, jdbcTemplate);

	}

	@Override
	public ResponseEntity<Object> createReleaseTestAttachment(MultipartHttpServletRequest request,
			final UserInfo userInfo) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final List<Object> listObject = new ArrayList<Object>();
		List<ReleaseTestAttachment> lstReleaseTestAttachment = objMapper.readValue(
				request.getParameter("releasetestattachment"), new TypeReference<List<ReleaseTestAttachment>>() {
				});
		final int ncontrolcode = Integer.parseInt(request.getParameter("ncontrolcode"));
		int isReleased = (int) jdbcTemplate
				.queryForObject("select ntransactionstatus  from  coaparent where ncoaparentcode="
						+ lstReleaseTestAttachment.get(0).getNcoaparentcode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and nsitecode="
						+ userInfo.getNtranssitecode() + "", Integer.class);

		if (isReleased != Enumeration.TransactionStatus.RELEASED.gettransactionstatus()) {
			final String sReturnString = ftpUtilityFunction.getFileFTPUpload(request, ncontrolcode, userInfo);
			if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(sReturnString)) {

				int seqNoReleaseTestAttachment = jdbcTemplate.queryForObject(
						"select nsequenceno from seqnoreleasemanagement where stablename='releasetestattachment' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ",
						Integer.class);
				seqNoReleaseTestAttachment++;
				Map<String, Object> jsonData = (Map<String, Object>) lstReleaseTestAttachment.get(0).getJsondata();
				jsonData.put("dcreateddate",
						dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ").replace("Z", ""));
				jsonData.put("noffsetdcreateddate",
						dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()));
				jsonData.put("sfilename", (jsonData.get("sfilename").toString().replaceAll("\"", "\\\\\"")));
				jsonData.put("sdescription", (jsonData.get("sdescription").toString().replaceAll("\"", "\\\\\"")));
				lstReleaseTestAttachment.get(0).setJsondata(jsonData);
				lstReleaseTestAttachment.get(0).setNreleasetestattachmentcode(seqNoReleaseTestAttachment);

				final String strReleaseTestAttachment = "insert into releasetestattachment (nreleasetestattachmentcode, ncoaparentcode, ntransactiontestcode, "
						+ "ntransactionsamplecode, npreregno, nformcode, nattachmenttypecode, nlinkcode, nusercode, nuserrolecode, jsondata, dtransactiondate,"
						+ " ntransdatetimezonecode, noffsetdtransactiondate, nsitecode, nstatus) values ("
						+ lstReleaseTestAttachment.get(0).getNreleasetestattachmentcode() + ", "
						+ lstReleaseTestAttachment.get(0).getNcoaparentcode() + ", "
						+ lstReleaseTestAttachment.get(0).getNtransactiontestcode() + ", "
						+ lstReleaseTestAttachment.get(0).getNtransactionsamplecode() + ", "
						+ +lstReleaseTestAttachment.get(0).getNpreregno() + ", "
						+ lstReleaseTestAttachment.get(0).getNformcode() + ", "
						+ Enumeration.AttachmentType.FTP.gettype() + ", -1, " + userInfo.getNusercode() + ", "
						+ userInfo.getNuserrole() + ", '"
						+ stringUtilityFunction.replaceQuote(
								objMapper.writeValueAsString(lstReleaseTestAttachment.get(0).getJsondata()).toString())
						+ "', '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
						+ userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ userInfo.getNtranssitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

				final String strSeqUpdate = strReleaseTestAttachment + "update seqnoreleasemanagement set nsequenceno ="
						+ seqNoReleaseTestAttachment + " where stablename ='releasetestattachment' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

				jdbcTemplate.execute(strSeqUpdate);

				final Map<String, Object> mapObj = objMapper.convertValue(lstReleaseTestAttachment.get(0),
						new TypeReference<Map<String, Object>>() {
						});

				lstReleaseTestAttachment.get(0)
						.setSfilename((String) lstReleaseTestAttachment.get(0).getJsondata().get("sfilename"));
				lstReleaseTestAttachment.get(0)
						.setSdescription((String) lstReleaseTestAttachment.get(0).getJsondata().get("sdescription"));
				lstReleaseTestAttachment.get(0)
						.setNneedreport((int) lstReleaseTestAttachment.get(0).getJsondata().get("nneedreport"));
				lstReleaseTestAttachment.get(0).setScreateddate(
						(String) lstReleaseTestAttachment.get(0).getJsondata().get("dcreateddate").toString());
				lstReleaseTestAttachment.get(0)
						.setSheader((String) lstReleaseTestAttachment.get(0).getJsondata().get("sheader"));
				lstReleaseTestAttachment.get(0)
						.setNsortorder(lstReleaseTestAttachment.get(0).getJsondata().get("nsortorder") != null
								? (int) lstReleaseTestAttachment.get(0).getJsondata().get("nsortorder")
								: 0);
				listObject.add(lstReleaseTestAttachment);

				auditUtilityFunction.fnInsertListAuditAction(listObject, 1, null,
						Arrays.asList("IDS_ADDRELEASETESTATTACHMENT"), userInfo);

				return getReleaseTestAttachment(mapObj, userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(sReturnString, userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_RECORDALREADYRELEASED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}

	@Override
	public ResponseEntity<Object> updateReleaseTestAttachment(MultipartHttpServletRequest request,
			final UserInfo userInfo) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final List<Object> listOldObject = new ArrayList<Object>();
		final List<Object> listNewObject = new ArrayList<Object>();
		List<ReleaseTestAttachment> lstReleaseTestAttachment = objMapper.readValue(
				request.getParameter("releasetestattachment"), new TypeReference<List<ReleaseTestAttachment>>() {
				});
		final int ncontrolcode = Integer.parseInt(request.getParameter("ncontrolcode"));
		final ReleaseTestAttachment mapReleaseTestAttachment = getActiveReleaseTestAttachmentById(
				lstReleaseTestAttachment.get(0).getNreleasetestattachmentcode(), userInfo);

		if (mapReleaseTestAttachment != null
				&& (int) mapReleaseTestAttachment.getNtransactionstatus() != Enumeration.TransactionStatus.RELEASED
						.gettransactionstatus()) {
			final int isFileEdited = Integer.valueOf(request.getParameter("isFileEdited"));
			String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();

			if (isFileEdited == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				sReturnString = ftpUtilityFunction.getFileFTPUpload(request, ncontrolcode, userInfo);
			}
			if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(sReturnString)) {

				final String strReleaseTestAttachment = "update releasetestattachment set jsondata= jsondata || '"
						+ stringUtilityFunction.replaceQuote(
								objMapper.writeValueAsString(lstReleaseTestAttachment.get(0).getJsondata()).toString())
						+ "', dtransactiondate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "', ntransdatetimezonecode=" + userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ " where nreleasetestattachmentcode="
						+ lstReleaseTestAttachment.get(0).getNreleasetestattachmentcode() + " and nsitecode = "
						+ userInfo.getNtranssitecode();
				jdbcTemplate.execute(strReleaseTestAttachment);

				final Map<String, Object> mapObj = objMapper.convertValue(lstReleaseTestAttachment.get(0),
						new TypeReference<Map<String, Object>>() {
						});

				listOldObject.add(Arrays.asList(mapReleaseTestAttachment));
				lstReleaseTestAttachment.get(0)
						.setSfilename((String) lstReleaseTestAttachment.get(0).getJsondata().get("sfilename"));
				lstReleaseTestAttachment.get(0)
						.setSdescription((String) lstReleaseTestAttachment.get(0).getJsondata().get("sdescription"));
				lstReleaseTestAttachment.get(0)
						.setNneedreport((int) lstReleaseTestAttachment.get(0).getJsondata().get("nneedreport"));
				lstReleaseTestAttachment.get(0)
						.setScreateddate((String) mapReleaseTestAttachment.getScreateddate().toString());
				listNewObject.add(lstReleaseTestAttachment);

				auditUtilityFunction.fnInsertListAuditAction(listNewObject, 2, listOldObject,
						Arrays.asList("IDS_EDITRELEASETESTATTACHMENT"), userInfo);

				return getReleaseTestAttachment(mapObj, userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(sReturnString, userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(
							mapReleaseTestAttachment == null ? Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus()
									: "IDS_RECORDALREADYRELEASED",
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}

	@Override
	public ResponseEntity<Object> deleteReleaseTestAttachment(ReleaseTestAttachment objReleaseTestAttachment,
			UserInfo userInfo) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final List<Object> listObject = new ArrayList<Object>();

		final ReleaseTestAttachment mapReleaseTestAttachment = getActiveReleaseTestAttachmentById(
				objReleaseTestAttachment.getNreleasetestattachmentcode(), userInfo);

		if (mapReleaseTestAttachment != null
				&& (int) mapReleaseTestAttachment.getNtransactionstatus() != Enumeration.TransactionStatus.RELEASED
						.gettransactionstatus()) {

			final String strReleaseTestAttachment = "update releasetestattachment set nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', ntransdatetimezonecode="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nreleasetestattachmentcode=" + objReleaseTestAttachment.getNreleasetestattachmentcode()
					+ " and nsitecode=" + userInfo.getNtranssitecode();
			jdbcTemplate.execute(strReleaseTestAttachment);

			final Map<String, Object> mapObj = objMapper.convertValue(objReleaseTestAttachment,
					new TypeReference<Map<String, Object>>() {
					});
			listObject.add(Arrays.asList(mapReleaseTestAttachment));

			auditUtilityFunction.fnInsertListAuditAction(listObject, 1, null,
					Arrays.asList("IDS_DELETERELEASETESTATTACHMENT"), userInfo);

			return getReleaseTestAttachment(mapObj, userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(
							mapReleaseTestAttachment == null ? Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus()
									: "IDS_RECORDALREADYRELEASED",
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}

	@Override
	public Map<String, Object> viewReleaseTestAttachment(Map<String, Object> objReleaseTestAttachmentFile,
			UserInfo userInfo, int ncontrolcode) throws Exception {

		Map<String, Object> map = new HashMap<>();
		final List<Object> listObject = new ArrayList<Object>();

		final ReleaseTestAttachment mapReleaseTestAttachment = getActiveReleaseTestAttachmentById(
				(int) objReleaseTestAttachmentFile.get("nreleasetestattachmentcode"), userInfo);
		if (mapReleaseTestAttachment != null) {

			map = ftpUtilityFunction.FileViewUsingFtp(mapReleaseTestAttachment.getSsystemfilename(), ncontrolcode,
					userInfo, "", "");
			listObject.add(Arrays.asList(mapReleaseTestAttachment));

			auditUtilityFunction.fnInsertListAuditAction(listObject, 1, null,
					Arrays.asList("IDS_VIEWRELEASETESTATTACHMENT"), userInfo);
		} else {
			map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), commonFunction.getMultilingualMessage(
					Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()));
			return map;
		}
		return map;

	}

	@Override
	public ResponseEntity<Object> getVersionHistory(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		Map<String, Object> returnMap = new HashMap<>();
		String sCoareporthistoryjoin = "";
		if (inputMap.containsKey("ncoareporthistorycode")) {
			sCoareporthistoryjoin = " and coar.ncoareporthistorycode=" + inputMap.get("ncoareporthistorycode") + "";
		}

		String sQuery = " select coar.nversionno,cp.ntransactionstatus,ts.stransstatus stransdisplaystatus,rt.sreporttypename,"
				+ " cp.ncoaparentcode,coar.ncoareporthistorycode, cp.sreportno,"
				+ " COALESCE(TO_CHAR(coar.dgenerateddate,'" + userInfo.getSpgsitedatetime()
				+ "'),'') as sgenerateddate," + " CONCAT(u.sfirstname,' ',u.slastname)susername,ur.suserrolename"
				+ " from coareporthistory coar,coaparent cp,transactionstatus ts,users u,userrole ur,reporttype rt"
				+ " where cp.ncoaparentcode=coar.ncoaparentcode and ts.ntranscode=cp.ntransactionstatus"
				+ " and u.nusercode=coar.nusercode  and ur.nuserrolecode=coar.nuserrolecode "
				+ " and rt.nreporttypecode=coar.nreportypecode and cp.ncoaparentcode=" + inputMap.get("ncoaparentcode")
				+ " and coar.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and cp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and ts.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and u.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and ur.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and rt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and coar.nsitecode=" + userInfo.getNtranssitecode() + "" + " and cp.nsitecode="
				+ userInfo.getNtranssitecode() + " and u.nsitecode=" + userInfo.getNmastersitecode() + ""
				+ " and ur.nsitecode=" + userInfo.getNmastersitecode() + " " + sCoareporthistoryjoin + ""
				+ " order by sgenerateddate desc";

		List<COAParent> lstCOAParent = jdbcTemplate.query(sQuery, new COAParent());

		if (lstCOAParent.isEmpty()) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_RECORDINDRAFTSTATUS", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		} else {
			returnMap.put("versionHistory", lstCOAParent);

			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Map<String, Object>> downloadVersionHistory(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		Map<String, Object> map = new HashMap<>();
		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditNew = new JSONObject();
		int ncoareporthistorycode = (int) inputMap.get("ncoareporthistorycode");
		if (ncoareporthistorycode > 0) {
			String sQuery = "select * from coareporthistory where ncoareporthistorycode ="
					+ inputMap.get("ncoareporthistorycode") + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNtranssitecode() + " ";

			COAReportHistory objReleasedCOAReport = (COAReportHistory) jdbcUtilityFunction.queryForObject(sQuery,
					COAReportHistory.class, jdbcTemplate);

			if (objReleasedCOAReport != null) {

				map = ftpUtilityFunction.FileViewUsingFtp(objReleasedCOAReport.getSsystemfilename(), -1, userInfo, "",
						"");
				inputMap.put("ncoareporthistorycode", inputMap.get("ncoareporthistorycode"));
				Map<String, Object> lstDataTestNew = getReleaseReportHistory(inputMap, userInfo);
				jsonAuditNew.put("registrationtest",
						(List<Map<String, Object>>) ((Map<String, Object>) lstDataTestNew).get("ReleaseReportHistory"));
				auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
				auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
				auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
				actionType.put("registrationtest", "IDS_VIEWCOAREPORT");

				auditUtilityFunction.fnJsonArrayAudit(jsonAuditNew, null, actionType, auditmap, false, userInfo);

			} else {
				map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						commonFunction.getMultilingualMessage("IDS_NOREPORTFOUND", userInfo.getSlanguagefilename()));
			}
		} else {
			int npreliminaryreporthistorycode = (int) inputMap.get("npreliminaryreporthistorycode");
			if (npreliminaryreporthistorycode > 0) {
				String sQuery = "select * from preliminaryreporthistory where npreliminaryreporthistorycode ="
						+ npreliminaryreporthistorycode + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
						+ userInfo.getNtranssitecode() + " ";

				PreliminaryReportHistory objReleasedCOAReport = (PreliminaryReportHistory) jdbcUtilityFunction
						.queryForObject(sQuery, PreliminaryReportHistory.class, jdbcTemplate);

				if (objReleasedCOAReport != null) {

					map = ftpUtilityFunction.FileViewUsingFtp(objReleasedCOAReport.getSsystemfilename(), -1, userInfo,
							"", "");
					inputMap.put("npreliminaryreporthistorycode", inputMap.get("npreliminaryreporthistorycode"));
					ResponseEntity<Map<String, Object>> lstDataTestNew = getPreliminaryReportHistory(inputMap,
							userInfo);
					jsonAuditNew.put("registrationtest",
							(List<Map<String, Object>>) ((Map<String, Object>) lstDataTestNew.getBody())
									.get("PreliminaryReportHistory"));
					auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
					auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
					auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
					actionType.put("registrationtest", "IDS_VIEWCOAREPORT");

					auditUtilityFunction.fnJsonArrayAudit(jsonAuditNew, null, actionType, auditmap, false, userInfo);

				} else {
					map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), commonFunction
							.getMultilingualMessage("IDS_NOREPORTFOUND", userInfo.getSlanguagefilename()));
				}
			}
		}
		return new ResponseEntity<>(map, HttpStatus.OK);

	}

	// ALPD-4248,Need to be validated only if mrt file is present or not in node
	// server for reports,added by Dhanushya RI
	public Map<String, Object> validationCheckForNodeServer(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		Map<String, Object> map = new HashMap<>();
		int nreporttypecode = -1;
		int nregTypeCode = -1;
		int nregSubTypeCode = -1;
		int ncoareporttypecode = -1;
		int decisionStatus = -1;
		int certificateTypeCode = -1;
		int nsectionCode = -1;
		int ncontrolCode = -1;
		int napproveconfversioncode = -1;
		String conditionString = "";
		String stable = "";
		String scondition = "";
		if (inputMap.containsKey("nreporttypecode")) {
			nreporttypecode = (int) inputMap.get("nreporttypecode");
		}
		if (inputMap.containsKey("ncoareporttypecode")) {
			ncoareporttypecode = (int) inputMap.get("ncoareporttypecode");
		}
		if (inputMap.containsKey("nregtypecode")) {
			nregTypeCode = (int) inputMap.get("nregtypecode");
		}
		if (inputMap.containsKey("nregsubtypecode")) {
			nregSubTypeCode = (int) inputMap.get("nregsubtypecode");
		}
		if (inputMap.containsKey("nsectioncode")) {
			nsectionCode = (int) inputMap.get("nsectioncode");
		}
		if (inputMap.containsKey("ncertificatetypecode")) {
			certificateTypeCode = (int) inputMap.get("ncertificatetypecode");
		}
		if (inputMap.containsKey("ndecisionstatus")) {
			decisionStatus = (int) inputMap.get("ndecisionstatus");
		}
		if (inputMap.containsKey("ncontrolcode")) {
			ncontrolCode = (int) inputMap.get("nreporttypecode") == Enumeration.ReportType.CONTROLBASED.getReporttype()
					? ncontrolCode
					: -1;
		}

		if (inputMap.containsKey("napproveconfversioncode")) {
			napproveconfversioncode = (int) inputMap.get("napproveconfversioncode");
		} else {
			if (inputMap.containsKey("napprovalversioncode")) {
				napproveconfversioncode = (int) inputMap.get("napprovalversioncode");
			}
		}
		if (inputMap.containsKey("nreportdetailcode")) {
			conditionString = " and rd.nreportdetailcode=" + inputMap.get("nreportdetailcode");
		} else {
			conditionString = " and coa.ncoareporttypecode=" + ncoareporttypecode + " and rm.nregtypecode="
					+ nregTypeCode + " and rm.nregsubtypecode=" + nregSubTypeCode + " and rm.napproveconfversioncode="
					+ napproveconfversioncode + "" + " and rd.ntransactionstatus="
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and rm.nreportdecisiontypecode="
					+ decisionStatus + " and rt.nreporttypecode=" + nreporttypecode + " and rm.ncertificatetypecode = "
					+ certificateTypeCode + " and rm.ncontrolcode = " + ncontrolCode + " and rm.nsectioncode = "
					+ nsectionCode;
		}

		if (userInfo.getNformcode() == Enumeration.QualisForms.RELEASE.getqualisforms()) {

			if ((int) inputMap.get("ncoareporttypecode") == Enumeration.COAReportType.PROJECTWISE.getcoaReportType()
					&& (int) inputMap.get("isSMTLFlow") == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				stable = ",reportinfoproject rip";
				scondition = " and rip.nreporttemplatecode=rm.nreporttemplatecode and " + " rip.nprojectmastercode="
						+ (int) inputMap.get("nprojectcode") + " and rip.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rip.nsitecode = "
						+ userInfo.getNtranssitecode();
			}
		}

		final String getMainReports = " select * from reportmaster rm,reportdetails rd,  reporttemplate rtt,reporttype rt ,coareporttype coa,coaparent cp "
				+ stable
				+ " where rtt.nreporttemplatecode = rm.nreporttemplatecode and cp.nreporttemplatecode = rtt.nreporttemplatecode and coa.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rtt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and rm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rm.nsitecode = "
				+ userInfo.getNmastersitecode() + " and rd.nsitecode=" + userInfo.getNmastersitecode()
				+ " and cp.nsitecode=" + userInfo.getNtranssitecode()
				+ " and rt.nreporttypecode=rm.nreporttypecode and coa.ncoareporttypecode=rm.ncoareporttypecode"
				+ " and rm.nreportcode=rd.nreportcode and cp.ncoaparentcode in ("
				+ inputMap.get("ncoaparentcode").toString() + ") and  cp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rm.ntransactionstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + conditionString + scondition
				+ " limit 1";

		final ReportDetails mainReport = (ReportDetails) jdbcUtilityFunction.queryForObject(getMainReports,
				ReportDetails.class, jdbcTemplate);
		map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
				Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		if (mainReport != null) {
			final String sFile = FilenameUtils.getExtension(mainReport.getSsystemfilename().toString());

			if ("mrt".equals("" + sFile + "")) {
				try {

					final String sURL = "http://localhost:8888/getReportNodeServer";
					RestTemplate restTemplate = new RestTemplate();
					LOGGER.info("COAParentCode====>>" + sURL);
					final String sResult = restTemplate.postForObject(sURL, "", String.class);
					LOGGER.info("ReleaseReport Compiled Successfully====>>");
					map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), sResult);

				} catch (Exception e) {
					LOGGER.info("Node server for report exception====>>" + e.getMessage());

					map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							Enumeration.ReturnStatus.FAILED.getreturnstatus());
				}
			}
		}
		return map;

	}

	@Override
	public ResponseEntity<Object> downloadHistory(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		final Map<String, Object> map = new HashMap<>();

		final String queryString = " select * from coaparent where ncoaparentcode =" + inputMap.get("ncoaparentcode")
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
				+ userInfo.getNtranssitecode();
		final COAParent coaParent = (COAParent) jdbcUtilityFunction.queryForObject(queryString, COAParent.class,
				jdbcTemplate);

		if (coaParent == null) {
			map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					commonFunction.getMultilingualMessage("IDS_SELECTRELEASEDRECORD", userInfo.getSlanguagefilename()));

			return new ResponseEntity<>(map, HttpStatus.CONFLICT);
		} else {
			if (coaParent.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
				map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), commonFunction
						.getMultilingualMessage("IDS_SELECTRELEASEDRECORD", userInfo.getSlanguagefilename()));

				return new ResponseEntity<>(map, HttpStatus.CONFLICT);
			} else {
				final String sQuery = "select * from coareporthistory where ncoareporthistorycode=any(select "
						+ " max(ncoareporthistorycode) from coareporthistory where ncoaparentcode ="
						+ inputMap.get("ncoaparentcode") + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
						+ userInfo.getNtranssitecode() + ") and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
						+ userInfo.getNtranssitecode() + "";

				final COAReportHistory objReleasedCOAReport = (COAReportHistory) jdbcUtilityFunction
						.queryForObject(sQuery, COAReportHistory.class, jdbcTemplate);

				if (objReleasedCOAReport != null) {
					inputMap.put("ncoareporthistorycode", objReleasedCOAReport.getNcoareporthistorycode());
					map.putAll(downloadVersionHistory(inputMap, userInfo).getBody());

					return new ResponseEntity<>(map, HttpStatus.OK);
				} else {
					map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), commonFunction
							.getMultilingualMessage("IDS_REPORTNOTFOUND", userInfo.getSlanguagefilename()));

					return new ResponseEntity<>(map, HttpStatus.CONFLICT);
				}
			}
		}

	}

	@Override
	public ResponseEntity<Object> getPatientWiseSample(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		Map<String, Object> returnMap = new HashMap<>();
		/*
		 * To caculate testcount based on nflag, nflag=1 for initial get and get for
		 * after save the sample and nflag=2 for Add,edit,delete popup sample get
		 */
		int nflag = 1;
		int napprovalconfigcode = -1;
		String fromDate = (String) inputMap.get("dfrom");
		String toDate = (String) inputMap.get("dto");
		fromDate = dateUtilityFunction
				.instantDateToString(dateUtilityFunction.convertStringDateToUTC(fromDate, userInfo, true));
		toDate = dateUtilityFunction
				.instantDateToString(dateUtilityFunction.convertStringDateToUTC(toDate, userInfo, true));
		inputMap.put("dfrom", fromDate);
		inputMap.put("dto", toDate);

		final DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());

		String ntransactionstatus = "";

		if (userInfo.getIsutcenabled() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
		} else {
		}

		if (!inputMap.containsKey("napprovalconfigcode")) {
			final String getApprovalConfig = "select napprovalconfigcode from approvalconfig where nregtypecode="
					+ inputMap.get("nregtypecode") + " and nregsubtypecode=" + inputMap.get("nregsubtypecode") + ""
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode = " + userInfo.getNmastersitecode();
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
					+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + ") and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
					+ userInfo.getNmastersitecode();
			List<String> versionList = jdbcTemplate.queryForList(getApprovalVersion, String.class);
			inputMap.put("napprovalversioncode", versionList.stream().collect(Collectors.joining(",")));
		}

		final String statusQuery = " select acr.napprovalstatuscode as ntransactionstatus from approvalconfigrole acr,approvalconfig ac,transactionstatus ts "
				+ " where acr.napprovalconfigcode=ac.napprovalconfigcode and acr.nlevelno="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and acr.ntransactionstatus = ts.ntranscode and ac.nregtypecode=" + inputMap.get("nregtypecode")
				+ " and ac.nregsubtypecode=" + inputMap.get("nregsubtypecode") + " and acr.napproveconfversioncode="
				+ inputMap.get("napprovalversioncode") + " and acr.nsitecode = " + userInfo.getNmastersitecode()
				+ " and ac.nsitecode = " + userInfo.getNmastersitecode() + "";

		List<TransactionStatus> filterStatus = jdbcTemplate.query(statusQuery, new TransactionStatus());
		final String finalLevelStatus = !filterStatus.isEmpty()
				? String.valueOf(filterStatus.get(0).getNtransactionstatus())
				: "-1";
		inputMap.put("finalLevelStatus", finalLevelStatus);

		if (inputMap.containsKey("isAddPopup") && (boolean) inputMap.get("isAddPopup") == true) {
			ntransactionstatus = (String) inputMap.get("ntransactionstatus") + ","
					+ String.valueOf(Enumeration.TransactionStatus.PARTIALLY.gettransactionstatus());
			nflag = 2;
		} else {
			ntransactionstatus = (String) inputMap.get("ntransactionstatus") + ","
					+ String.valueOf(Enumeration.TransactionStatus.PARTIALLY.gettransactionstatus()) + ","
					+ String.valueOf(Enumeration.TransactionStatus.RELEASED.gettransactionstatus());

		}
		String fromDate1 = LocalDateTime.parse(fromDate, formatter1).format(formatter);
		String toDate1 = LocalDateTime.parse(toDate, formatter1).format(formatter);

		returnMap.put("realFromDate", fromDate1);
		returnMap.put("realToDate", toDate1);

		if (inputMap.containsKey("nneedtemplatebasedflow") && (boolean) inputMap.get("nneedtemplatebasedflow")) {
		}

		if (inputMap.containsKey("nneededit")) {
			if ((int) inputMap.get("isneedsection") == Enumeration.TransactionStatus.YES.gettransactionstatus()
					|| (int) inputMap.get("ncoareporttypecode") == Enumeration.COAReportType.SECTIONWISEMULTIPLESAMPLE
							.getcoaReportType()) {
				inputMap.putAll((Map<String, Object>) getSection(inputMap, userInfo).getBody());
			} else {
				inputMap.put("nsectioncode", "0");
			}
			if ((int) inputMap.get("ncoareporttypecode") == Enumeration.COAReportType.PROJECTWISE.getcoaReportType()) {
				inputMap.putAll((Map<String, Object>) getApprovedProjectByProjectType(inputMap, userInfo).getBody());
			} else {
				inputMap.put("nprojectmastercode", "0");
			}
			inputMap.put("ncoaparentcode", -1);
			inputMap.put("nneedmodal", true);
			nflag = 2;
		} else if (inputMap.containsKey("isAddPopup") && (boolean) inputMap.get("isAddPopup") == true) {
			inputMap.put("ncoaparentcode", -1);
			inputMap.put("nneedmodal", true);
			nflag = 2;

		} else {
			if (!inputMap.containsKey("isSearch")) {
				returnMap.putAll((Map<String, Object>) getReleaseHistory(inputMap, userInfo).getBody());
				inputMap.put("ncoaparentcode", returnMap.get("ncoaparentcode"));
				inputMap.put("nprojectmastercode", "0");
				inputMap.put("nsectioncode", "0");
				inputMap.put("npreregno", "0");
			} else {
				inputMap.put("ncoaparentcode", inputMap.get("ncoaparentcode"));
			}

		}

		String str = " select npreregno from registration where jsondata->>'spatientid'::character varying='"
				+ inputMap.get("spatientid") + "' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
				+ userInfo.getNtranssitecode() + ";";
		List<Registration> preregno = jdbcTemplate.query(str, new Registration());
		String preregnonew = "-1";
		if (preregno.size() > 0) {
			preregnonew = preregno.stream().map(objpreregno -> String.valueOf(objpreregno.getNpreregno()))
					.collect(Collectors.joining(","));
		}

		String sampleTypeQuery = "SELECT * from fn_releasesampleget('" + fromDate + "', '" + toDate + "', " + ""
				+ inputMap.get("nregtypecode") + " , " + inputMap.get("nregsubtypecode") + ", '" + ntransactionstatus
				+ "','" + inputMap.get("napprovalversioncode") + "'," + " " + userInfo.getNusercode() + ", "
				+ userInfo.getNtranssitecode() + ", '" + preregnonew + "', " + " " + inputMap.get("nneedsubsample")
				+ "," + inputMap.get("nprojectmastercode") + ",'" + inputMap.get("ncoaparentcode") + "'," + " "
				+ inputMap.get("ncoareporttypecode") + ",'" + userInfo.getSlanguagetypecode() + "'," + " "
				+ inputMap.get("nneedmodal") + ", '" + finalLevelStatus + "'," + inputMap.get("nsectioncode") + ", '"
				+ inputMap.get("filterquery") + "'," + nflag + ");";

		List<Map<String, Object>> sampleList = new ArrayList<Map<String, Object>>();
		System.out.println("Sample Start========?" + LocalDateTime.now());
		String sampleObj = jdbcTemplate.queryForObject(sampleTypeQuery, String.class);
		System.out.println("Sample Query========?" + sampleTypeQuery);
		System.out.println("Sample end========?" + LocalDateTime.now());

		if (sampleObj != null) {
			sampleList = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(
					sampleObj, userInfo, true, (int) inputMap.get("ndesigntemplatemappingcode"), "sample");

			final String spreregno = sampleList.stream().map(x -> String.valueOf(x.get("npreregno")))
					.collect(Collectors.joining(","));
			inputMap.put("npreregno", spreregno);

		} else {
			inputMap.put("npreregno", 0);
		}

		returnMap.putAll((Map<String, Object>) getReleaseSubSample(inputMap, userInfo).getBody());
		returnMap.put("DynamicDesign", projectDAOSupport
				.getTemplateDesign((int) inputMap.get("ndesigntemplatemappingcode"), userInfo.getNformcode()));
		returnMap.putAll(viewReport(inputMap, userInfo).getBody());

		if (inputMap.containsKey("isPopup") && (boolean) inputMap.get("isPopup") == true) {
			returnMap.put("ReleaseSample", sampleList);
		} else {
			returnMap.put("ReleasedSampleDetails", sampleList);
		}
		System.out.println("sample end=>" + Instant.now());

		return new ResponseEntity<>(returnMap, HttpStatus.OK);

	}

	public ResponseEntity<Object> releasedReportGeneration(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		Map<String, Object> returnMap = new HashMap<String, Object>();

		int nregTypeCode = -1;
		int nregSubTypeCode = -1;
		int nsectionCode = -1;
		int naccredit = Enumeration.TransactionStatus.NO.gettransactionstatus();
		String sreportComments = "";

		int qType = 1;
		int ncontrolCode = -1;
		int nreportDetailCode = -1;
		int ncoaparentcode = 0;
		String systemFileName = "";
		String reportPath = "";
		String folderName = "";
		String subReportPath = "";
		String imagePath = "";
		String pdfPath = "";
		String sfileName = "";
		String sreportno = "";
		String sJRXMLname = "";
		String sreportingtoolURL = "";
		String ssignfilename = "";
		String scertfilename = "";
		String ssecuritykey = "";

		returnMap.putAll(inputMap);
		returnMap.remove(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());

		if (!userInfo.getSreason().isEmpty()) {
			sreportComments = userInfo.getSreason();
		}
		if (inputMap.containsKey("nregtypecode")) {
			nregTypeCode = (int) inputMap.get("nregtypecode");
		}
		if (inputMap.containsKey("ncontrolcode")) {
			ncontrolCode = (int) inputMap.get("ncontrolcode");
		}
		if (inputMap.containsKey("nregsubtypecode")) {
			nregSubTypeCode = (int) inputMap.get("nregsubtypecode");
		}

		if (inputMap.containsKey("ncoaparentcode")) {
			ncoaparentcode = Integer.parseInt(inputMap.get("ncoaparentcode").toString());
			inputMap.put("ncoaparentcode", ncoaparentcode);
		}
		if (inputMap.containsKey("reportSectionCode")) {
			nsectionCode = (int) inputMap.get("reportSectionCode");
		}
		if (inputMap.containsKey("systemFileName")) {
			systemFileName = (String) inputMap.get("systemFileName");
		}

		final String sReportQuery = "select ssettingvalue from settings where nsettingcode = 29 and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final Settings objReportsettings = (Settings) jdbcTemplate.queryForObject(sReportQuery, new Settings());

		if (objReportsettings.getSsettingvalue()
				.equals(String.valueOf(Enumeration.TransactionStatus.YES.gettransactionstatus()))) {

			reportTemplateDAO.writeJSONTemplate(inputMap, userInfo);
			returnMap.putAll(getDigitalSignatureDetail(inputMap, userInfo));
			ssignfilename = (String) returnMap.get("ssignfilename");
			scertfilename = (String) returnMap.get("scertfilename");
			ssecuritykey = (String) returnMap.get("ssecuritykey");

		} else {

			final String sRootDir = ftpUtilityFunction.getFTPFileWritingPath("", null);

			final String testAttachmentQuery = "select coalesce(jsondata->>'ssystemfilename','') ssystemfilename from releasetestattachment where ncoaparentcode = "
					+ inputMap.get("ncoaparentcode") + " and (jsondata->'nneedreport'):: INT = "
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
					+ userInfo.getNtranssitecode() + "";
			final List<Map<String, Object>> lstSysFile = jdbcTemplate.queryForList(testAttachmentQuery);

			File oSysFile = null;
			String sSysFile = "";

			for (int nlen = 0; nlen < lstSysFile.size(); nlen++) {
				sSysFile = (String) lstSysFile.get(nlen).get("ssystemfilename");
				oSysFile = new File(sRootDir + sSysFile);
				if (!oSysFile.exists()) {
					ftpUtilityFunction.FileViewUsingFtp(oSysFile.getName(), 0, userInfo, sRootDir, "");
				}
			}
		}

		final String getReportPaths = "select ssettingvalue from reportsettings where nreportsettingcode in ("
				+ Enumeration.ReportSettings.REPORT_PATH.getNreportsettingcode() + ","
				+ Enumeration.ReportSettings.REPORT_PDF_PATH.getNreportsettingcode() + ","
				+ Enumeration.ReportSettings.REPORTINGTOOL_URL.getNreportsettingcode() + ") and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nreportsettingcode ";

		final List<String> reportPaths = jdbcTemplate.queryForList(getReportPaths, String.class);
		reportPath = reportPaths.get(0);
		subReportPath = reportPaths.get(0);
		imagePath = reportPaths.get(0);
		pdfPath = reportPaths.get(1);

		sreportingtoolURL = reportPaths.get(2);
		inputMap.put("subReportPath", subReportPath);
		inputMap.put("folderName", folderName);
		inputMap.put("imagePath", imagePath);
		inputMap.put("naccredit", naccredit);
		inputMap.put("nregTypeCode", nregTypeCode);
		inputMap.put("nregSubTypeCode", nregSubTypeCode);
		inputMap.put("nsectioncode", nsectionCode);
		inputMap.put("sreportComments", sreportComments);
		inputMap.put("sreportingtoolURL", sreportingtoolURL);
		inputMap.put("ncontrolcode",
				(int) inputMap.get("nreporttypecode") == Enumeration.ReportType.CONTROLBASED.getReporttype()
						? ncontrolCode
						: -1);
		final UserInfo userInfoWithReportFormCode = new UserInfo(userInfo);
		inputMap.put("napproveconfversioncode", (int) inputMap.get("napprovalversioncode"));

		final Map<String, Object> dynamicReport = reportDAOSupport.getDynamicReports(inputMap,
				userInfoWithReportFormCode);

		if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
				.equals((String) dynamicReport.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {

			sJRXMLname = (String) dynamicReport.get("JRXMLname");
			nreportDetailCode = (int) dynamicReport.get("nreportdetailcode");
			folderName = (String) dynamicReport.get("folderName");

			qType = (int) dynamicReport.get("qType");
			reportPath = reportPath + folderName;

			inputMap.put("qType", qType);
			inputMap.put("folderName", folderName);
			inputMap.put("nreportDetailCode", nreportDetailCode);
			inputMap.put("sJRXMLname", sJRXMLname);

			// ATE234 janakumar ALPD-5116 Release&Report ->View the report in the new tab
			final String getFileuploadpath = "select ssettingvalue from reportsettings where"
					+ " nreportsettingcode in (" + Enumeration.ReportSettings.REPORTLINKPATH.getNreportsettingcode()
					+ "," + Enumeration.ReportSettings.MRT_REPORT_JSON_PATH.getNreportsettingcode() + ") and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nreportsettingcode ";

			final List<String> reportPathsValues = jdbcTemplate.queryForList(getFileuploadpath, String.class);

			returnMap.put("sreportlink", reportPathsValues.get(0));
			returnMap.put("smrttemplatelink", reportPathsValues.get(1));
			returnMap.put("FILEName", dynamicReport.get("JRXMLname"));
			returnMap.put("folderName", folderName.replace("\\", ""));

		}
		reportPath = reportPath + sJRXMLname;
		final File JRXMLFile = new File(reportPath);

		if (sJRXMLname != null && !sJRXMLname.equals("") && JRXMLFile.exists()) {

			inputMap.put("systemFileName", systemFileName);
			inputMap.put("JRXMLFile", JRXMLFile);
			inputMap.put("qType", qType);
			inputMap.put("pdfPath", pdfPath);
			inputMap.put("sfileName", sfileName);
			inputMap.put("ssignfilename", ssignfilename);
			inputMap.put("scertfilename", scertfilename);
			inputMap.put("ssecuritykey", ssecuritykey);
			inputMap.put("nreportdetailcode", dynamicReport.get("nreportdetailcode"));
			if (inputMap.containsKey("sreportno")) {
				sreportno = (String) inputMap.get("sreportno");
			}
			inputMap.put("sreportno", sreportno);

			// Commented by L.Subashini
			// returnMap.putAll(getPdfReportForMrt(inputMap, userInfo));

			final String settingsQry = "select ssettingvalue from settings where" + " nsettingcode in ("
					+ Enumeration.Settings.COAREPORT_GENERATION.getNsettingcode() + ","
					+ Enumeration.Settings.REPORT_OPEN_NEW_TAB.getNsettingcode() + ") and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nsettingcode asc";

			final List<String> settingsDataList = jdbcTemplate.queryForList(settingsQry, String.class);

			// List<Settings> settingsDataList = jdbcTemplate.query(settingsQry, new
			// Settings());

			Integer openReportNewTab = null;
			Integer schedulerBasedReport = null;

			if (!settingsDataList.isEmpty()) {

				schedulerBasedReport = Integer.parseInt(settingsDataList.get(0));
				openReportNewTab = Integer.parseInt(settingsDataList.get(1));
			}

			inputMap.put("openReportNewTab", openReportNewTab);
			inputMap.put("schedulerBasedReport", schedulerBasedReport);

			returnMap.putAll(generateCOAReport(inputMap, userInfo));

			if (openReportNewTab != null
					&& openReportNewTab == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
				if (schedulerBasedReport != null
						&& schedulerBasedReport == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
					final String draftToReleased = "UPDATE coareporthistorygeneration SET " + "nreportstatus = "
							+ Enumeration.TransactionStatus.RELEASED.gettransactionstatus() + " "
							+ "WHERE ncoareporthistorycode = " + inputMap.get("ncoareporthistorycode") + " "
							+ " and nsitecode = " + userInfo.getNtranssitecode() + " AND nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ;";

					jdbcTemplate.execute(draftToReleased);
				}
			}

			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		} else {

			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_REPORTTEMPLATEISNOTFOUND",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);

		}
	}

	public List<ReleaseTestAttachment> insertReleaseTestComment(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final String releaseTestCommentSeqNo = "select nsequenceno from seqnoreleasemanagement where stablename='releasetestcomment' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final int releaseTestCommentsSeqNo = jdbcTemplate.queryForObject(releaseTestCommentSeqNo, Integer.class);
		final String strReleaseTestComment = "insert into releasetestcomment (nreleasetestcommentcode, ncoaparentcode, ntransactiontestcode, ntransactionsamplecode, nsamplecommentscode, npreregno, nformcode, nusercode, nuserrolecode, jsondata, dtransactiondate, ntransdatetimezonecode, noffsetdtransactiondate, nsitecode, nstatus, ncommentsubtypecode, nsampletestcommentscode) "
				+ "select " + releaseTestCommentsSeqNo + "+Rank()over(order by ntestcommentcode), "
				+ inputMap.get("ncoaparentcode")
				+ ", ntransactiontestcode, ntransactionsamplecode, nsamplecommentscode, npreregno, nformcode, nusercode, nuserrolecode, jsondata, '"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
				+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", nsitecode, "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ ", coalesce((jsondata->'ncommentsubtypecode'->>'value')::integer,-1), coalesce((jsondata->'nsampletestcommentscode'->>'value')::integer,-1) "
				+ " from registrationtestcomments where ntransactiontestcode in ("
				+ inputMap.get("ntransactiontestcode") + ") and ntransactionsamplecode in ("
				+ inputMap.get("ntransactionsamplecode") + ") and npreregno in (" + inputMap.get("npreregno").toString()
				+ ") and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
				+ userInfo.getNtranssitecode() + ";"
				+ " update seqnoreleasemanagement set nsequenceno=(select coalesce((select max(nreleasetestcommentcode) from releasetestcomment), 0)) where stablename='releasetestcomment' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
		jdbcTemplate.execute(strReleaseTestComment);
		return null;
	}

	@Override
	public ResponseEntity<Object> getReleaseTestComment(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		ObjectMapper objMapper = new ObjectMapper();
		final String strReleaseTestComment = "select rtc.nreleasetestcommentcode, rtc.jsondata->>'stestsynonym' stestsynonym, "
				+ "ran.sarno, rsa.ssamplearno, qf.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode() + "' "
				+ "sformname, concat(u.sfirstname,' ',u.slastname) susername, ur.suserrolename suserrolename, rtc.jsondata->>'scomments' "
				+ "scomments, rtc.jsondata->>'nneedreport' nneedreport, rtc.jsondata->>'spredefinedname' spredefinedname,"
				+ " rtc.ncoaparentcode, rtc.ntransactiontestcode, rtc.ntransactionsamplecode, rtc.nsamplecommentscode,"
				+ "rtc.npreregno, rtc.nformcode, rtc.nusercode, rtc.nuserrolecode, rtc.nsitecode, rtc.nstatus, CASE WHEN rtc.jsondata->>'scommentsubtype' is null then '-' else rtc.jsondata->>'scommentsubtype' end scommentsubtype, "
				+ "CASE WHEN rtc.jsondata->>'nneedreport' = '3' THEN 'Yes' ELSE 'No' END sneedreport, rtc.ncommentsubtypecode, rtc.nsampletestcommentscode "
				+ " from releasetestcomment rtc, registrationarno ran, registrationsamplearno rsa, qualisforms qf, users u, userrole ur where ran.npreregno = rtc.npreregno"
				+ " and rsa.ntransactionsamplecode=rtc.ntransactionsamplecode and rtc.nformcode=qf.nformcode and rtc.nusercode=u.nusercode and rtc.nuserrolecode=ur.nuserrolecode and rtc.ncoaparentcode="
				+ inputMap.get("ncoaparentcode") + " and rtc.nsitecode=" + userInfo.getNtranssitecode()
				+ " and ran.nsitecode = " + userInfo.getNtranssitecode() + " and rsa.nsitecode = "
				+ userInfo.getNtranssitecode() + " and u.nsitecode = " + userInfo.getNmastersitecode()
				+ " and ur.nsitecode = " + userInfo.getNmastersitecode() + " and rtc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ran.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rsa.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and qf.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by rtc.nreleasetestcommentcode desc";
		List<ReleaseTestComment> lstReleaseTestComment = jdbcTemplate.query(strReleaseTestComment,
				new ReleaseTestComment());
		objMapper.registerModule(new JavaTimeModule());
		final List<?> lstUTCConvertedDate = dateUtilityFunction.getSiteLocalTimeFromUTC(lstReleaseTestComment,
				Arrays.asList("screateddate"), null, userInfo, false, null, false);
		returnMap.put("ReleaseTestCommentDetails", lstUTCConvertedDate);
		returnMap.putAll((Map<String, Object>) getDetailsBasedOnCOAParent(inputMap, userInfo).getBody());
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	public ResponseEntity<Map<String, Object>> getDetailsBasedOnCOAParent(Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		final String strArnoDetails = "select * from registrationarno ran where npreregno = any (select npreregno from coachild where ncoaparentcode="
				+ inputMap.get("ncoaparentcode") + " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by npreregno);";
		final String strTransactionSampleDetails = "select * from registrationsamplearno where nsitecode="
				+ userInfo.getNtranssitecode()
				+ " and ntransactionsamplecode= any (select ntransactionsamplecode from coachild where ncoaparentcode="
				+ inputMap.get("ncoaparentcode") + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by ntransactionsamplecode);";
		final String strTransactionTestDetails = "select *, jsondata->>'stestsynonym' stestsynonym from registrationtest where nsitecode="
				+ userInfo.getNtranssitecode()
				+ " and ntransactiontestcode = any(select ntransactiontestcode from coachild where ncoaparentcode="
				+ inputMap.get("ncoaparentcode") + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by ntransactiontestcode);";
		final String strCommentSubType = "select *, coalesce(jsondata->'scommentsubtype'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "', jsondata->'scommentsubtype'->>'en-US') scommentsubtype from commentsubtype where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
		// ALPD-4948 Added sample test comments to capture NA record in jsondata instead
		// of capturing '-'
		final String strSampleTestComments = "select nsampletestcommentscode, ncommenttypecode, ncommentsubtypecode, spredefinedname, sdescription, nsitecode, nstatus"
				+ " from sampletestcomments where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode ="
				+ userInfo.getNmastersitecode() + ";";

		final List<RegistrationArno> lstRegistrationArno = jdbcTemplate.query(strArnoDetails, new RegistrationArno());
		returnMap.put("RegistrationArno", lstRegistrationArno);
		final List<RegistrationSampleArno> lstRegistrationSampleArno = jdbcTemplate.query(strTransactionSampleDetails,
				new RegistrationSampleArno());
		returnMap.put("RegistrationSampleArno", lstRegistrationSampleArno);
		final List<RegistrationTest> lstRegistrationTest = jdbcTemplate.query(strTransactionTestDetails,
				new RegistrationTest());
		returnMap.put("RegistrationTest", lstRegistrationTest);
		final List<CommentSubType> lstCommentSubType = jdbcTemplate.query(strCommentSubType, new CommentSubType());
		returnMap.put("CommentSubType", lstCommentSubType);
		final List<SampleTestComments> lstSampleTestComments = jdbcTemplate.query(strSampleTestComments,
				new SampleTestComments());
		returnMap.put("SampleTestComments", lstSampleTestComments);

		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ReleaseTestComment getActiveReleaseTestCommentById(int nreleaseTestCommentCode, UserInfo userInfo)
			throws Exception {

		final String strReleaseTestComment = "select cp.ntransactionstatus, rtc.*, rtc.jsondata->>'nneedreport' nneedreport, rtc.jsondata->>'scomments' scomments, "
				+ "rtc.jsondata->>'scommentsubtype' scommentsubtype, rtc.jsondata->>'spredefinedname' spredefinedname, coalesce(cst.jsondata->'scommentsubtype'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "', cst.jsondata->'scommentsubtype'->>'en-US') scommentsubtype, stc.spredefinedname"
				+ " from releasetestcomment rtc, coaparent cp," + " commentsubtype cst, sampletestcomments stc where"
				+ " rtc.nreleasetestcommentcode=" + nreleaseTestCommentCode + " and rtc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cst.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and stc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rtc.nsitecode = "
				+ userInfo.getNtranssitecode() + " and rtc.ncoaparentcode=cp.ncoaparentcode"
				+ " and rtc.ncommentsubtypecode=cst.ncommentsubtypecode and rtc.nsampletestcommentscode=stc.nsampletestcommentscode"
				+ " and cp.nsitecode= " + userInfo.getNtranssitecode() + " and stc.nsitecode = "
				+ userInfo.getNmastersitecode();
		return (ReleaseTestComment) jdbcUtilityFunction.queryForObject(strReleaseTestComment, ReleaseTestComment.class,
				jdbcTemplate);

	}

	@Override
	public ResponseEntity<Object> createReleaseTestComment(MultipartHttpServletRequest request, final UserInfo userInfo)
			throws Exception {
		final List<Object> listObject = new ArrayList<Object>();
		ObjectMapper objMapper = new ObjectMapper();
		List<ReleaseTestComment> lstReleaseTestComment = objMapper.readValue(request.getParameter("releasetestcomment"),
				new TypeReference<List<ReleaseTestComment>>() {
				});
		int isReleased = (int) jdbcTemplate
				.queryForObject("select ntransactionstatus  from  coaparent where ncoaparentcode="
						+ lstReleaseTestComment.get(0).getNcoaparentcode() + " and nsitecode = "
						+ userInfo.getNtranssitecode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);
		if (isReleased != Enumeration.TransactionStatus.RELEASED.gettransactionstatus()) {
			int seqNoReleaseTestComment = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnoreleasemanagement where stablename='releasetestcomment' and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);
			seqNoReleaseTestComment++;
			Map<String, Object> jsonData = (Map<String, Object>) lstReleaseTestComment.get(0).getJsondata();
			jsonData.put("scomments", (jsonData.get("scomments").toString().replaceAll("\"", "\\\\\"")));
			lstReleaseTestComment.get(0).setJsondata(jsonData);
			lstReleaseTestComment.get(0).setNreleasetestcommentcode(seqNoReleaseTestComment);

			final String strReleaseTestComment = "insert into releasetestcomment (nreleasetestcommentcode, ncoaparentcode, "
					+ "ntransactiontestcode, ntransactionsamplecode, nsamplecommentscode, npreregno, ncommentsubtypecode, nsampletestcommentscode, nformcode, nusercode, nuserrolecode, jsondata, dtransactiondate, "
					+ "ntransdatetimezonecode, noffsetdtransactiondate, nsitecode, nstatus) values ("
					+ lstReleaseTestComment.get(0).getNreleasetestcommentcode() + ", "
					+ lstReleaseTestComment.get(0).getNcoaparentcode() + ", "
					+ lstReleaseTestComment.get(0).getNtransactiontestcode() + ", "
					+ lstReleaseTestComment.get(0).getNtransactionsamplecode() + ", "
					+ lstReleaseTestComment.get(0).getNsamplecommentscode() + ", "
					+ lstReleaseTestComment.get(0).getNpreregno() + ", "
					+ lstReleaseTestComment.get(0).getNcommentsubtypecode() + ", "
					+ lstReleaseTestComment.get(0).getNsampletestcommentscode() + ", "
					+ lstReleaseTestComment.get(0).getNformcode() + ", " + userInfo.getNusercode() + ", "
					+ userInfo.getNuserrole() + ", '"
					+ stringUtilityFunction.replaceQuote(
							objMapper.writeValueAsString(lstReleaseTestComment.get(0).getJsondata()).toString())
					+ "', '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode()
					+ ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNtranssitecode() + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ");";
			final String strSeqUpdate = strReleaseTestComment + "update seqnoreleasemanagement set nsequenceno ="
					+ seqNoReleaseTestComment + " where stablename ='releasetestcomment' and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			jdbcTemplate.execute(strSeqUpdate);
			final Map<String, Object> mapObj = objMapper.convertValue(lstReleaseTestComment.get(0),
					new TypeReference<Map<String, Object>>() {
					});
			lstReleaseTestComment.get(0)
					.setNneedreport((int) lstReleaseTestComment.get(0).getJsondata().get("nneedreport"));
			lstReleaseTestComment.get(0)
					.setScomments(lstReleaseTestComment.get(0).getJsondata().get("scomments").toString());
			listObject.add(lstReleaseTestComment);
			auditUtilityFunction.fnInsertListAuditAction(listObject, 1, null,
					Arrays.asList("IDS_ADDRELEASETESTCOMMENT"), userInfo);
			return getReleaseTestComment(mapObj, userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_RECORDALREADYRELEASED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> updateReleaseTestComment(MultipartHttpServletRequest request, final UserInfo userInfo)
			throws Exception {
		final List<Object> listOldObject = new ArrayList<Object>();
		final List<Object> listNewObject = new ArrayList<Object>();
		ObjectMapper objMapper = new ObjectMapper();
		List<ReleaseTestComment> lstReleaseTestComment = objMapper.readValue(request.getParameter("releasetestcomment"),
				new TypeReference<List<ReleaseTestComment>>() {
				});
		final ReleaseTestComment mapReleaseTestComment = getActiveReleaseTestCommentById(
				lstReleaseTestComment.get(0).getNreleasetestcommentcode(), userInfo);
		if (mapReleaseTestComment != null
				&& (int) mapReleaseTestComment.getNtransactionstatus() != Enumeration.TransactionStatus.RELEASED
						.gettransactionstatus()) {
			final String strReleaseTestComment = "update releasetestcomment set jsondata= jsondata || '"
					+ stringUtilityFunction.replaceQuote(
							objMapper.writeValueAsString(lstReleaseTestComment.get(0).getJsondata()).toString())
					+ "', ncommentsubtypecode= " + lstReleaseTestComment.get(0).getNcommentsubtypecode() + ", "
					+ "nsampletestcommentscode=" + lstReleaseTestComment.get(0).getNsampletestcommentscode()
					+ ", dtransactiondate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
					+ "', ntransdatetimezonecode=" + userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nreleasetestcommentcode=" + lstReleaseTestComment.get(0).getNreleasetestcommentcode()
					+ " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			jdbcTemplate.execute(strReleaseTestComment);
			final Map<String, Object> mapObj = objMapper.convertValue(lstReleaseTestComment.get(0),
					new TypeReference<Map<String, Object>>() {
					});
			listOldObject.add(Arrays.asList(mapReleaseTestComment));
			lstReleaseTestComment.get(0)
					.setNneedreport((int) lstReleaseTestComment.get(0).getJsondata().get("nneedreport"));
			lstReleaseTestComment.get(0)
					.setScomments(lstReleaseTestComment.get(0).getJsondata().get("scomments").toString());
			listNewObject.add(lstReleaseTestComment);
			auditUtilityFunction.fnInsertListAuditAction(listNewObject, 2, listOldObject,
					Arrays.asList("IDS_EDITRELEASETESTCOMMENT"), userInfo);
			return getReleaseTestComment(mapObj, userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(
							mapReleaseTestComment == null ? Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus()
									: "IDS_RECORDALREADYRELEASED",
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> deleteReleaseTestComment(ReleaseTestComment objReleaseTestComment, UserInfo userInfo)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final ReleaseTestComment mapReleaseTestComment = getActiveReleaseTestCommentById(
				objReleaseTestComment.getNreleasetestcommentcode(), userInfo);
		if (mapReleaseTestComment != null
				&& (int) mapReleaseTestComment.getNtransactionstatus() != Enumeration.TransactionStatus.RELEASED
						.gettransactionstatus()) {
			final List<Object> listObject = new ArrayList<Object>();
			final String strReleaseTestComment = "update releasetestcomment set nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', ntransdatetimezonecode="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nreleasetestcommentcode=" + objReleaseTestComment.getNreleasetestcommentcode()
					+ " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			jdbcTemplate.execute(strReleaseTestComment);
			final Map<String, Object> mapObj = objMapper.convertValue(objReleaseTestComment,
					new TypeReference<Map<String, Object>>() {
					});
			listObject.add(Arrays.asList(mapReleaseTestComment));
			auditUtilityFunction.fnInsertListAuditAction(listObject, 1, null,
					Arrays.asList("IDS_DELETERELEASETESTCOMMENT"), userInfo);
			return getReleaseTestComment(mapObj, userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(
							mapReleaseTestComment == null ? Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus()
									: "IDS_RECORDALREADYRELEASED",
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	public Map<String, Object> preliminaryReport(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		final Map<String, Object> returnMap = new HashMap<>();
		final List<Map<String, Object>> lstMapSystemFileNameAndVersion = new ArrayList<>();
		final List<Map<String, Object>> lstReportNoDetails = new ArrayList<>();

		final String[] nCoaParentCode = inputMap.get("ncoaparentcode").toString().split(",");

		String generateReleaseNo = "";
		String updateCoaParent = "";
		String sReportformat = "";

		String comments = null;
		if (inputMap.get("sreportcomments") != null) {
			comments = "'" + stringUtilityFunction.replaceQuote((String) inputMap.get("sreportcomments")) + "'";
		}

		final String strSeqNoCOAReportHistory = "select nsequenceno from seqnoreleasemanagement "
				+ " where stablename in ('preliminaryreporthistory')" + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
		int historySeqNo = jdbcTemplate.queryForObject(strSeqNoCOAReportHistory, Integer.class);

		final String strPreliminaryReportHistory = "insert into preliminaryreporthistory (npreliminaryreporthistorycode,"
				+ " ncoaparentcode,nversionno,nreportypecode, nreportdetailcode, nusercode, "
				+ " nuserrolecode, ndeputyusercode, ndeputyuserrolecode,ssystemfilename,sreportcomments,"
				+ " dtransactiondate, ntztransactiondate, noffsetdtransactiondate,nsitecode,nstatus ) values ";

		String InsertPreliminaryReportHistory = "";

		final String reportpresentQuery = "select crh.ncoaparentcode, crh.nversionno, "
				+ " cp.ntransactionstatus from coareporthistory crh"
				+ ", coaparent cp where crh.ncoareporthistorycode in "
				+ " (select max(ncoareporthistorycode) from coareporthistory" + " where ncoaparentcode in ("
				+ inputMap.get("ncoaparentcode") + ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by ncoaparentcode) and cp.ncoaparentcode=crh.ncoaparentcode " + " and crh.nsitecode = "
				+ userInfo.getNtranssitecode() + " and cp.nsitecode = " + userInfo.getNtranssitecode()
				+ " and cp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final List<COAReportHistory> objreportpresent = (List<COAReportHistory>) jdbcTemplate.query(reportpresentQuery,
				new COAReportHistory());

		for (String coaParentCode : nCoaParentCode) {
			int nversionNo = 0;
			final Map<String, Object> mapSystemFileName = new HashMap<>();
			String systemFileName = "";

			// String systemFileName = UUID.nameUUIDFromBytes(concat.getBytes()).toString()
			// + "_" + coaParentCode+ ".pdf";

			// Added by Neeraj on 17-06-2024 for JIRA ID:4291 UUID Name or Report Ref NO.
			if ((int) inputMap.get("reportRefFileName") == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				systemFileName = jdbcTemplate.queryForObject("select sreportno ||'.pdf' from coaparent "
						+ " where ncoaparentcode=" + coaParentCode + " and nsitecode= " + userInfo.getNtranssitecode()
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
						String.class);
			} else {

				// 04-12-2024 - L. Subashini - Moved the UUID- combination creation code inside
				// loop to handle multi release

				// Added by Neeraj on 17-06-2024 for JIRA ID:4291 UUID Name or Report Ref NO.
				final String currentDateTime = dateUtilityFunction.getCurrentDateTime(userInfo).toString()
						.replace("T", " ").replace("Z", "");
				final String concat = String.valueOf(userInfo.getNtranssitecode()) + currentDateTime;

				systemFileName = UUID.nameUUIDFromBytes(concat.getBytes()).toString() + "_" + coaParentCode + ".pdf";
			}

			if (objreportpresent.size() > 0) {
				for (COAReportHistory coaReportHistory : objreportpresent) {
					if (String.valueOf(coaReportHistory.getNcoaparentcode()).equals(coaParentCode)) {
						// systemFileName = coaReportHistory.getSreportno();

						nversionNo = ((int) inputMap
								.get("ncoaparenttranscode") == Enumeration.TransactionStatus.CORRECTION
										.gettransactionstatus()
								&& (int) coaReportHistory
										.getNtransactionstatus() != Enumeration.TransactionStatus.RELEASED
												.gettransactionstatus()) ? (coaReportHistory.getNversionno() + 1)
														: coaReportHistory.getNversionno();
					}
				}
			}

			// For single coaparentcode. Not for multiple coaparentcode
			if ((int) inputMap.get("isPreliminaryReportNoGenerate") == Enumeration.TransactionStatus.YES
					.gettransactionstatus() && inputMap.get("doAction").equals("preliminary")) {

				final List<PreliminaryReportHistory> lstPreliminaryReportHistory = getPreliminaryReportHistoryCount(
						inputMap.get("ncoaparentcode").toString(), userInfo);

				if (lstPreliminaryReportHistory.size() == 0) {

					final Map<String, Object> mapReportNoDetails = new HashMap<>();

					SeqNoReleasenoGenerator seqnoFormat = null;
					Map<Integer, String> sReportformatBasedOnRegSubType = new HashMap<>();

					seqnoFormat = checkReleaseNoGeneratorFormat(inputMap, userInfo);
					// String sReportformat = "";
					if (seqnoFormat != null) {

						if (seqnoFormat.getJsondata().containsKey("nneedsitewisearnorelease")
								&& (boolean) seqnoFormat.getJsondata().get("nneedsitewisearnorelease")) {
							seqnoFormat = insertsiteReleasenoGenerator(seqnoFormat, userInfo, inputMap);

						}

						sReportformatBasedOnRegSubType = transactionDAOSupport.getfnRereleaseFormat(
								Arrays.asList(inputMap.get("ncoaparentcode").toString()),
								seqnoFormat.getNsequenceno() + 1, seqnoFormat.getSreleaseformat(),
								seqnoFormat.getDseqresetdate(), seqnoFormat.getNseqnoreleasenogencode(), userInfo,
								seqnoFormat.getJsondata(), seqnoFormat.getNregsubtypeversionreleasecode(),
								seqnoFormat.getNperiodcode(), seqnoFormat.getNsequenceno());
						sReportformat = sReportformatBasedOnRegSubType.get(Integer.parseInt(coaParentCode));
					} else {
						sReportformat = projectDAOSupport.getSeqfnFormat("coaparent", "seqnoformatgeneratorrelease",
								(int) inputMap.get("nregtypecode"), (int) inputMap.get("nregsubtypecode"), userInfo);
					}

					generateReleaseNo = generateReleaseNo + "update coaparent set sreportno='" + sReportformat + "' ,"
							+ " dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
							+ "' where ncoaparentcode=" + inputMap.get("ncoaparentcode") + " and nsitecode="
							+ userInfo.getNtranssitecode() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

					mapReportNoDetails.put("ncoaParentCode", inputMap.get("ncoaparentcode"));
					mapReportNoDetails.put("sreportno", sReportformat);
					lstReportNoDetails.add(mapReportNoDetails);
				}
				updateCoaParent = "update coaparent set ntransactionstatus="
						+ Enumeration.TransactionStatus.PRELIMINARYRELEASE.gettransactionstatus()
						+ " where ncoaparentcode in (" + inputMap.get("ncoaparentcode") + ") and nsitecode="
						+ userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			}

			// To have reportRefNo. as systemFileName - added by Subashini
			systemFileName = sReportformat == "" ? systemFileName : sReportformat + ".pdf";

			returnMap.put("systemFileName", systemFileName);

			historySeqNo = historySeqNo + 1;
			InsertPreliminaryReportHistory = InsertPreliminaryReportHistory + " (" + historySeqNo + "," + coaParentCode
					+ "," + nversionNo + "," + inputMap.get("nreporttypecode") + " ," + " "
					+ inputMap.get("nreportdetailcode") + "," + userInfo.getNusercode() + "," + userInfo.getNuserrole()
					+ "," + userInfo.getNdeputyusercode() + "," + userInfo.getNdeputyuserrole() + "," + " '"
					+ systemFileName + "'," + comments + "," + " '" + dateUtilityFunction.getCurrentDateTime(userInfo)
					+ "' ," + userInfo.getNtimezonecode() + ","
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
					+ userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "),";

			mapSystemFileName.put("ncoaparentcode", coaParentCode);
			mapSystemFileName.put("systemFileName", systemFileName);
			mapSystemFileName.put("nversionno", nversionNo);
			lstMapSystemFileNameAndVersion.add(mapSystemFileName);
		}

		InsertPreliminaryReportHistory = strPreliminaryReportHistory
				+ InsertPreliminaryReportHistory.substring(0, InsertPreliminaryReportHistory.length() - 1) + ";";
		String coaReportHistorySeqno = "update seqnoreleasemanagement set nsequenceno=" + historySeqNo
				+ " where stablename='preliminaryreporthistory'" + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "; ";

		jdbcTemplate
				.execute(InsertPreliminaryReportHistory + coaReportHistorySeqno + generateReleaseNo + updateCoaParent);

		returnMap.put("systemFileNameAndVersion", lstMapSystemFileNameAndVersion);
		returnMap.put("releaseNoDetails", lstReportNoDetails);

		String additionalTransStatus = "";
		if ((int) inputMap.get("isPreliminaryReportNoGenerate") == Enumeration.TransactionStatus.YES
				.gettransactionstatus()
				&& !((int) inputMap.get("ncoaparenttranscode") == Enumeration.TransactionStatus.PRELIMINARYRELEASE
						.gettransactionstatus())) {
			additionalTransStatus = Enumeration.TransactionStatus.PRELIMINARYRELEASE.gettransactionstatus() + "";
		} else {
			additionalTransStatus = "";
		}

		insertReleaseHistory(inputMap.get("ncoaparentcode").toString(), userInfo, additionalTransStatus);

		return returnMap;
	}

	@Override
	public ResponseEntity<Map<String, Object>> getPreliminaryReportHistory(Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception {

		Map<String, Object> returnMap = new HashMap<>();
		String sCoareporthistoryjoin = "";
		if (inputMap.containsKey("npreliminaryreporthistorycode")) {
			sCoareporthistoryjoin = " and coar.npreliminaryreporthistorycode="
					+ inputMap.get("npreliminaryreporthistorycode") + "";
		}

		String sQuery = " select coar.nversionno,cp.ntransactionstatus,ts.stransstatus stransdisplaystatus,rt.sreporttypename,"
				+ " cp.ncoaparentcode,coar.npreliminaryreporthistorycode, cp.sreportno,"
				+ " COALESCE(TO_CHAR(coar.dtransactiondate,'" + userInfo.getSpgsitedatetime()
				+ "'),'') as sgenerateddate, CONCAT(u.sfirstname,' ',u.slastname)susername,ur.suserrolename"
				+ " from preliminaryreporthistory coar,coaparent cp,transactionstatus ts,users u,userrole ur,reporttype rt"
				+ " where cp.ncoaparentcode=coar.ncoaparentcode and ts.ntranscode=cp.ntransactionstatus"
				+ " and u.nusercode=coar.nusercode  and ur.nuserrolecode=coar.nuserrolecode "
				+ " and rt.nreporttypecode=coar.nreportypecode and cp.ncoaparentcode=" + inputMap.get("ncoaparentcode")
				+ " and coar.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and cp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and ts.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and u.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and ur.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and rt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and coar.nsitecode=" + userInfo.getNtranssitecode() + "" + " and cp.nsitecode="
				+ userInfo.getNtranssitecode() + " and u.nsitecode=" + userInfo.getNmastersitecode() + ""
				+ " and ur.nsitecode=" + userInfo.getNmastersitecode() + "" + "" + sCoareporthistoryjoin + ""
				+ " order by sgenerateddate desc";

		List<COAParent> lstCOAParent = jdbcTemplate.query(sQuery, new COAParent());

		returnMap.put("PreliminaryReportHistory", lstCOAParent);
		returnMap.putAll(getReleaseReportHistory(inputMap, userInfo));
		returnMap.putAll(getRegenerateReportHistory(inputMap, userInfo));
		returnMap.putAll(getReleaseHistoryTab(inputMap, userInfo));

		return new ResponseEntity<>(returnMap, HttpStatus.OK);

	}

	public Map<String, Object> getReleaseReportHistory(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		Map<String, Object> returnMap = new HashMap<>();
		String sCoareporthistoryjoin = "";
		if (inputMap.containsKey("ncoareporthistorycode")) {
			sCoareporthistoryjoin = " and coar.ncoareporthistorycode=" + inputMap.get("ncoareporthistorycode") + "";
		}

		String sQuery = " select coar.nversionno,cp.ntransactionstatus,ts.stransstatus stransdisplaystatus,rt.sreporttypename,"
				+ " cp.ncoaparentcode,coar.ncoareporthistorycode, cp.sreportno,"
				+ " COALESCE(TO_CHAR(coar.dgenerateddate,'" + userInfo.getSpgsitedatetime()
				+ "'),'') as sgenerateddate," + " CONCAT(u.sfirstname,' ',u.slastname)susername,ur.suserrolename"
				+ " from coareporthistory coar,coaparent cp,transactionstatus ts,users u,userrole ur,reporttype rt"
				+ " where cp.ncoaparentcode=coar.ncoaparentcode and ts.ntranscode=cp.ntransactionstatus"
				+ " and u.nusercode=coar.nusercode  and ur.nuserrolecode=coar.nuserrolecode "
				+ " and rt.nreporttypecode=coar.nreportypecode and cp.ncoaparentcode=" + inputMap.get("ncoaparentcode")
				+ " and coar.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and cp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and ts.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and u.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and ur.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and rt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and coar.nsitecode=" + userInfo.getNtranssitecode() + "" + " and cp.nsitecode="
				+ userInfo.getNtranssitecode() + " and u.nsitecode=" + userInfo.getNmastersitecode() + ""
				+ " and ur.nsitecode=" + userInfo.getNmastersitecode() + "" + sCoareporthistoryjoin + ""
				+ " order by sgenerateddate desc";

		List<COAParent> lstCOAParent = jdbcTemplate.query(sQuery, new COAParent());

		returnMap.put("ReleaseReportHistory", lstCOAParent);
		return returnMap;

	}

	public Map<String, Object> getRegenerateReportHistory(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		Map<String, Object> returnMap = new HashMap<>();
		String sCoareporthistoryjoin = "";
		if (inputMap.containsKey("nregeneratereporthistorycode")) {
			sCoareporthistoryjoin = " and coar.nregeneratereporthistorycode="
					+ inputMap.get("nregeneratereporthistorycode") + "";
		}

		String sQuery = " select coar.nversionno,cp.ntransactionstatus,ts.stransstatus stransdisplaystatus,rt.sreporttypename,"
				+ " cp.ncoaparentcode,coar.nregeneratereporthistorycode, cp.sreportno,"
				+ " COALESCE(TO_CHAR(coar.dtransactiondate,'" + userInfo.getSpgsitedatetime()
				+ "'),'') as sgenerateddate," + " CONCAT(u.sfirstname,' ',u.slastname)susername,ur.suserrolename"
				+ " from regeneratereporthistory coar,coaparent cp,transactionstatus ts,users u,userrole ur,reporttype rt"
				+ " where cp.ncoaparentcode=coar.ncoaparentcode and ts.ntranscode=cp.ntransactionstatus"
				+ " and u.nusercode=coar.nusercode  and ur.nuserrolecode=coar.nuserrolecode "
				+ " and rt.nreporttypecode=coar.nreportypecode and cp.ncoaparentcode=" + inputMap.get("ncoaparentcode")
				+ " and coar.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and cp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and ts.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and u.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and ur.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and rt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and coar.nsitecode=" + userInfo.getNtranssitecode() + "" + " and cp.nsitecode="
				+ userInfo.getNtranssitecode() + " and u.nsitecode=" + userInfo.getNmastersitecode() + ""
				+ " and ur.nsitecode=" + userInfo.getNmastersitecode() + "" + sCoareporthistoryjoin + ""
				+ " order by sgenerateddate desc";

		List<COAParent> lstCOAParent = jdbcTemplate.query(sQuery, new COAParent());

		returnMap.put("RegenerateReportHistory", lstCOAParent);
		return returnMap;

	}

	public Map<String, Object> getSectionValue(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		Map<String, Object> returnMap = new HashMap<>();
		final int nsectioncode = -1;

		final String sQuery = "select ntransactiontestcode from coachild where ncoaparentcode="
				+ inputMap.get("ncoaparentcode") + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
				+ userInfo.getNtranssitecode();
		List<String> testList = jdbcTemplate.queryForList(sQuery, String.class);
		final String transactiontestcode = testList.stream().collect(Collectors.joining(","));

		String sSectionQuery = "select s.nsectioncode,s.ssectionname"
				+ " from section s,registration r,registrationtest rt " + " where s.nsectioncode = rt.nsectioncode "
				+ " and rt.npreregno= r.npreregno " + " and r.nsitecode=rt.nsitecode and r.nregtypecode = "
				+ inputMap.get("nregtypecode") + " and r.nsampletypecode = " + inputMap.get("nsampletypecode")
				+ " and r.nregsubtypecode = " + inputMap.get("nregsubtypecode") + " and rt.ntransactiontestcode in ("
				+ transactiontestcode + ") and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and r.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and r.nsitecode="
				+ userInfo.getNtranssitecode() + " and rt.nsitecode=" + userInfo.getNtranssitecode()
				+ " and s.nsitecode=" + userInfo.getNmastersitecode() + " group by s.nsectioncode";

		List<Section> sectionList = jdbcTemplate.query(sSectionQuery, new Section());

		if (!sectionList.isEmpty()) {
			returnMap.put("reportSectionCode", sectionList.get(0).getNsectioncode());
		} else {
			returnMap.put("reportSectionCode", nsectioncode);

		}
		return returnMap;

	}

	public ResponseEntity<Object> getComboValues(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		Map<String, Object> returnMap = new HashMap<>();
		final int nregtypecode = (int) inputMap.get("nregtypecode");
		final int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
		final int ndesigntemplatemappingcode = (int) inputMap.get("ndesigntemplatemappingcode");
		final List<Map<String, Object>> sampleList = objMapper.convertValue(inputMap.get("itemList"),
				new TypeReference<List<Map<String, Object>>>() {
				});
		final String statusQuery = " select acr.napprovalstatuscode as ntransactionstatus "
				+ "from approvalconfigrole acr,approvalconfig ac,transactionstatus ts "
				+ " where acr.napprovalconfigcode=ac.napprovalconfigcode and acr.nlevelno="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and acr.ntransactionstatus = ts.ntranscode and ac.nregtypecode=" + nregtypecode
				+ " and ac.nregsubtypecode=" + nregsubtypecode + " and acr.napproveconfversioncode="
				+ inputMap.get("napprovalversioncode") + " and acr.nsitecode=" + userInfo.getNmastersitecode()
				+ " and acr.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ac.nsitecode=" + userInfo.getNmastersitecode() + " and ac.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";

		List<TransactionStatus> filterStatus = jdbcTemplate.query(statusQuery, new TransactionStatus());
		final String finalLevelStatus = !filterStatus.isEmpty()
				? String.valueOf(filterStatus.get(0).getNtransactionstatus())
				: "-1";

		for (int i = 0; i < sampleList.size(); i++) {
			String fieldName = "";
			String condtionQy = "";
			if (sampleList.get(i).containsValue("isMultiLingual")) {
				fieldName = "jsondata->'" + sampleList.get(i).get("columnname") + "'->>'"
						+ sampleList.get(i).get("languageTypeCode") + "'";
			} else {
				fieldName = "" + sampleList.get(i).get("columnname") + "";
			}
			if (sampleList.get(i).containsKey("recordType")
					&& sampleList.get(i).get("recordType").toString().equals("static")) {
				condtionQy = " a." + sampleList.get(i).get("Pkey") + " and "
						+ "  a.ntransactiontestcode not in  (select coachild.ntransactiontestcode from coachild where coachild.npreregno =a.npreregno"
						+ " and coachild.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			} else {
				condtionQy = "(a.jsondata->'" + sampleList.get(i).get("keyName") + "'->>'"
						+ sampleList.get(i).get("Pkey") + "')::int ";

			}

			String query = "select sd." + sampleList.get(i).get("Pkey") + " as value ,max(sd." + fieldName
					+ ") as title "
					+ " from (select  r.jsondata||rt.jsondata as jsondata,r.npreregno, r.nsampletypecode,r.nregsubtypecode,r.ndesigntemplatemappingcode,r.nregtypecode,r.nallottedspeccode"
					+ " ,rt.ntestgrouptestcode,rt.ntestcode,rt.nsectioncode,rt.ntransactiontestcode ,r.nstatus ,r.nsitecode"
					+ " from registration r ,registrationtest rt"
					+ " where r.npreregno=rt.npreregno and rt.nsitecode = " + userInfo.getNtranssitecode()
					+ " and r.nsitecode = " + userInfo.getNtranssitecode() + " and rt.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and r.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") a , "
					+ " registrationtesthistory rth," + sampleList.get(i).get("tablename") + " sd "
					+ " where a.npreregno=rth.npreregno and a.ntransactiontestcode=rth.ntransactiontestcode and rth.ntesthistorycode = any(select max(ntesthistorycode)  from registrationtesthistory where a.ntransactiontestcode=ntransactiontestcode group by ntransactiontestcode ) "
					+ " and a.nregtypecode=" + nregtypecode + " and a.nregsubtypecode=" + nregsubtypecode
					+ " and a.ndesigntemplatemappingcode=" + ndesigntemplatemappingcode
					+ " and  rth.ntransactionstatus =" + finalLevelStatus + " " + " and  sd."
					+ sampleList.get(i).get("Pkey") + "=" + condtionQy + " and sd.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and a.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and a.nsitecode="
					+ userInfo.getNtranssitecode() + " and rth.nsitecode=" + userInfo.getNtranssitecode()
					+ " and rth.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " group by sd." + sampleList.get(i).get("Pkey") + " " + " union all "
					+ " select -1 as value ,'NA' as title   from transactionstatus group by value ";
			List<Map<String, Object>> listDataList = jdbcTemplate.queryForList(query);
			returnMap.put(sampleList.get(i).get("keyName").toString(), listDataList);
		}
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> generateReport(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		final String Query = "select ncoaparentcode,nversionno FROM  coareporthistorygeneration "
				+ "where nreportstatus = " + Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + " "
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
				+ userInfo.getNtranssitecode() + "  limit 1 ;";

		List<Map<String, Object>> coareporthistorygenerationExist = jdbcTemplate.queryForList(Query);

		if (inputMap.get("syncaction").equals("ManualSyncreport")) {

//				ResourceBundle resourcebundle = new PropertyResourceBundle(
//						new InputStreamReader(
//								getClass().getClassLoader().getResourceAsStream(
//										"/com/properties/" + userInfo.getSlanguagefilename() + ".properties"),
//								"UTF-8"));

			ResourceBundle resourcebundle = commonFunction.getResourceBundle(userInfo.getSlanguagefilename(), false);

			String syncscomments = resourcebundle.containsKey("IDS_SYNCATTEMPT")
					? resourcebundle.getString("IDS_SYNCATTEMPT") + ": " + resourcebundle.getString("IDS_MANUALSYNC")
							+ ";"
					: "Sync Attempted : " + " No Data Modified" + "; ";

			Map<String, Object> outputMap = new HashMap<>();
			outputMap.put("stablename", "coaparent");
			outputMap.put("sprimarykeyvalue", 1);

			boolean isSync = true;
			outputMap.put("isSync", isSync);
			outputMap.put("dataStatus", "IDS_MANUALSYNC");
			outputMap.put("syncscomments", syncscomments);
			String auditAction = "IDS_MANUALSYNC";
			auditUtilityFunction.insertAuditAction(userInfo, auditAction, syncscomments, outputMap);

			schedularGenerateReportDAO.AsyncReportGeneration(inputMap, userInfo);

		} else {

			return new ResponseEntity<>(
					"IDS_SYNCNOTWORK" + commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);

		}

		return new ResponseEntity<>(coareporthistorygenerationExist, HttpStatus.OK);

	}

	public ResponseEntity<Object> reportGenerationSync(Map<String, Object> inputList) throws Exception {

		Map<String, Object> msgScheduler = new HashMap<>();

		final String Query6 = "select ssettingvalue from settings where nsettingcode in ("
				+ Enumeration.Settings.NEED_SMTL_JSON_TEMPLATE.getNsettingcode() + ","
				+ Enumeration.Settings.COAREPORT_GENERATION.getNsettingcode() + ") and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

		final List<String> settings = jdbcTemplate.queryForList(Query6, String.class);

		final String countQuery = "select count(ncoareporthistorycode) FROM  coareporthistorygeneration "
				+ "where nreportstatus=" + Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + " "
				+ "and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		int count = jdbcTemplate.queryForObject(countQuery, Integer.class);
		if (count > 0) {

			boolean auditstatus = true;
			boolean auditstatu1 = false;
			String rtnStatus = "failed";

			final String Query = "SELECT  ncoareporthistorycode,reportinfo ->> 'sloginid' AS sloginid, "
					+ "(reportinfo ->> 'nformcode')::int AS nformcode, "
					+ "(reportinfo ->> 'nsitecode')::int AS nsitecode, "
					+ "reportinfo ->> 'sreportingtoolfilename' AS sreportingtoolfilename, "
					+ "(reportinfo ->> 'nreasoncode')::int AS nreasoncode, "
					+ "(reportinfo ->> 'ntranssitecode')::int AS ntranssitecode, "
					+ "(reportinfo ->> 'nmastersitecode')::int AS nmastersitecode, "
					+ "reportinfo ->> 'slanguagefilename' AS slanguagefilename, "
					+ "reportinfo ->> 'sreason' AS sreason, " + "reportinfo ->> 'slanguagename' AS slanguagename, "
					+ "reportinfo ->> 'susername' AS susername, "
					+ "reportinfo ->> 'slanguagetypecode' AS slanguagetypecode, "
					+ "(reportinfo ->> 'isutcenabled')::int  AS isutcenabled, "
					+ "reportinfo ->> 'sdatetimeformat' AS sdatetimeformat,  "
					+ "reportinfo ->> 'suserrolename' AS suserrolename,  "
					+ "reportinfo ->> 'spredefinedreason' AS spredefinedreason,  "
					+ "(reportinfo ->> 'ndeputyuserrole'):: int AS ndeputyuserrole,  "
					+ "(reportinfo ->> 'nmodulecode'):: int AS nmodulecode,  "
					+ "(reportinfo ->> 'nusercode'):: int AS nusercode,  "
					+ "(reportinfo ->> 'ndeputyusercode'):: int AS ndeputyusercode, "
					+ "ncoaparentcode,nversionno FROM  coareporthistorygeneration "
					+ "where nreportstatus=(select ntranscode from transactionstatus " + "where ntranscode="
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ") " + "and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by 1 asc limit 1 ;";

			List<Map<String, Object>> reportTemplateList = jdbcTemplate.queryForList(Query);

			if (reportTemplateList != null && !reportTemplateList.isEmpty()) {

				final String Query1 = "select * from coareporthistory where ncoareporthistorycode="
						+ reportTemplateList.get(0).get("ncoareporthistorycode") + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nversionno="
						+ reportTemplateList.get(0).get("nversionno")+";";
				List<Map<String, Object>> coareporthistory = jdbcTemplate.queryForList(Query1);

				final String Query2 = "select * from coaparent where ncoaparentcode="
						+ reportTemplateList.get(0).get("ncoaparentcode") + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ;";
				List<Map<String, Object>> coaparent = jdbcTemplate.queryForList(Query2);

				final String Query3 = "select * from coachild  where ncoaparentcode="
						+ reportTemplateList.get(0).get("ncoaparentcode") + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ;";
				List<Map<String, Object>> coachild = jdbcTemplate.queryForList(Query3);

				final String Query4 = "select * from registrationarno  where npreregno="
						+ coachild.get(0).get("npreregno") + ";";
				List<Map<String, Object>> registrationarno = jdbcTemplate.queryForList(Query4);

				final String Query5 = "select nsampletypecode from registration  where npreregno="
						+ coachild.get(0).get("npreregno") + ";";
				List<Map<String, Object>> registration = jdbcTemplate.queryForList(Query5);

				ResponseEntity<Object> returnResp = null;
				for (int i = 0; i < count; i++) {

					Map<String, Object> inputMap = new HashMap<>();

					LOGGER.info("ncoaparentcode --> "+ coaparent.get(0).get("ncoaparentcode"));
					LOGGER.info("ncoareporthistorycode --> "+ coareporthistory.get(0).get("ncoareporthistorycode"));
					
					inputMap.put("ncoaparentcode", coaparent.get(0).get("ncoaparentcode"));
					inputMap.put("nregtypecode", coaparent.get(0).get("nregtypecode"));
					inputMap.put("nregsubtypecode", coaparent.get(0).get("nregsubtypecode"));
					inputMap.put("napproveconfversioncode", coaparent.get(0).get("napproveconfversioncode"));
					inputMap.put("ncoareporttypecode", coaparent.get(0).get("ncoareporttypecode"));
					inputMap.put("ncoareporthistorycode", coareporthistory.get(0).get("ncoareporthistorycode"));
					inputMap.put("nversionno", coareporthistory.get(0).get("nversionno"));
					inputMap.put("nreportypecode", coareporthistory.get(0).get("nreportypecode"));
					inputMap.put("systemFileName", coareporthistory.get(0).get("ssystemfilename"));
					inputMap.put("npreregno", coachild.get(0).get("npreregno"));
					inputMap.put("ntransactiontestcode", coachild.get(0).get("ntransactiontestcode"));
					inputMap.put("nreporttypecode", Enumeration.ReportType.COA.getReporttype());
					inputMap.put("napprovalversioncode", registrationarno.get(0).get("napprovalversioncode"));
					inputMap.put("action", " ");
					inputMap.put("sreportno", coaparent.get(0).get("sreportno"));
					inputMap.put("auditstatus", inputList.get("autoSync") == "autoSync" ? auditstatus : auditstatu1);
					inputMap.put("syncAction", inputList.get("manualsync") == "manualsync" ? inputList.get("manualsync")
							: inputList.get("autoSync"));
					inputMap.put("nsampletypecode", registration.get(0).get("nsampletypecode"));
					inputMap.put("ncoareporttypecode", coaparent.get(0).get("ncoareporttypecode"));
					inputMap.put("isSMTLFlow", Integer.parseInt(settings.get(0)));

					int nformcode = (int) reportTemplateList.get(0).get("nformcode");
					int nmastersitecode = (int) reportTemplateList.get(0).get("nmastersitecode");
					int ntranssitecode = (int) reportTemplateList.get(0).get("ntranssitecode");
					int nmodulecode = (int) reportTemplateList.get(0).get("nmodulecode");

					UserInfo userInfo = new UserInfo();

					userInfo.setSusername((String) reportTemplateList.get(0).get("susername"));
					userInfo.setNformcode((short) nformcode);
					userInfo.setNmastersitecode((short) nmastersitecode);
					userInfo.setSreportingtoolfilename(
							(String) reportTemplateList.get(0).get("sreportingtoolfilename"));
					userInfo.setNreasoncode((int) reportTemplateList.get(0).get("nreasoncode"));
					userInfo.setSreason((String) reportTemplateList.get(0).get("sreason"));
					userInfo.setNtranssitecode((short) ntranssitecode);
					userInfo.setSloginid((String) reportTemplateList.get(0).get("sloginid"));
					userInfo.setSlanguagefilename((String) reportTemplateList.get(0).get("slanguagefilename"));
					userInfo.setSlanguagename((String) reportTemplateList.get(0).get("slanguagename"));
					userInfo.setSlanguagetypecode((String) reportTemplateList.get(0).get("slanguagetypecode"));
					userInfo.setIsutcenabled((int) reportTemplateList.get(0).get("isutcenabled"));
					userInfo.setSdatetimeformat((String) reportTemplateList.get(0).get("sdatetimeformat"));
					userInfo.setSuserrolename((String) reportTemplateList.get(0).get("suserrolename"));
					userInfo.setSpredefinedreason((String) reportTemplateList.get(0).get("predefinedreason"));
					userInfo.setNdeputyuserrole((int) reportTemplateList.get(0).get("ndeputyuserrole"));
					userInfo.setNmodulecode((short) nmodulecode);
					userInfo.setNusercode((int) reportTemplateList.get(0).get("nusercode"));
					userInfo.setNdeputyusercode((int) reportTemplateList.get(0).get("ndeputyusercode"));

					returnResp = schedularReleaseReportDAO.reportGenerationBySchedular(inputMap, userInfo);

					if (returnResp != null) {

						Map<String, Object> checkExist = (Map<String, Object>) returnResp.getBody();

						if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(checkExist.get("rtn"))) {

							rtnStatus = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
							msgScheduler.put("rtnStatus", rtnStatus);
							LOGGER.info("ReportGenerationSync01--> ReportEnded-->");

						} else {

							System.out.println("Value of rtn is not Success");
							checkExist.clear();
							checkExist.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
									Enumeration.ReturnStatus.FAILED.getreturnstatus());
							return new ResponseEntity<>(checkExist, HttpStatus.OK);

						}
					}
				}
				auditstatus = false;
			} else {
				return null;
			}
			msgScheduler.put("rtnStatus", rtnStatus);
		} else {
			return null;
		}

		return new ResponseEntity<>(msgScheduler, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> getTest(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Map<String, Object> returnMap = new HashMap<>();

		String userTransactionTestQuery = " select max(rt.jsondata->>'stestsynonym') as title, rt.ntestgrouptestcode as value"
				+ " from registrationtest rt,registrationtesthistory rth,registration r ,testgrouptest tgt"
				+ " where r.npreregno=rt.npreregno  and tgt.ntestgrouptestcode= rt.ntestgrouptestcode "
				+ " and rt.ntransactiontestcode=rth.ntransactiontestcode "
				+ " and rth.ntesthistorycode = any(select max(ntesthistorycode)"
				+ " from registrationtesthistory where rt.ntransactiontestcode=ntransactiontestcode "
				+ " and nsitecode = " + userInfo.getNtranssitecode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by ntransactiontestcode )"
				+ " and r.nregtypecode=" + inputMap.get("nregtypecode") + " and r.nregsubtypecode="
				+ inputMap.get("nregsubtypecode") + " and r.ndesigntemplatemappingcode="
				+ inputMap.get("ndesigntemplatemappingcode")
				+ " and rth.ntransactionstatus= ( select acr.napprovalstatuscode as ntransactionstatus"
				+ " from approvalconfigrole acr,approvalconfig ac,transactionstatus ts "
				+ "	 where acr.napprovalconfigcode=ac.napprovalconfigcode and acr.nlevelno="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "	 and acr.ntransactionstatus = ts.ntranscode " + "	 and acr.napproveconfversioncode="
				+ inputMap.get("napprovalversioncode") + " and acr.nsitecode = " + userInfo.getNmastersitecode()
				+ " and ac.nsitecode = " + userInfo.getNmastersitecode() + " and acr.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ac.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")"
				+ " and rt.ntransactiontestcode not in (select coachild.ntransactiontestcode from coachild"
				+ " where coachild.npreregno =r.npreregno" + " and coachild.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
				+ userInfo.getNtranssitecode() + ")" + " and r.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and r.nsitecode = "
				+ userInfo.getNtranssitecode() + " and rt.nsitecode = " + userInfo.getNtranssitecode()
				+ " and rth.nsitecode = " + userInfo.getNtranssitecode() + " and tgt.nsitecode = "
				+ userInfo.getNmastersitecode() + " group by rt.ntestgrouptestcode";

		List<RegistrationTest> transactionTestList = jdbcTemplate.query(userTransactionTestQuery,
				new RegistrationTest());
		returnMap.put("rt.ntestgrouptestcode", transactionTestList);

		return new ResponseEntity<Object>(returnMap, HttpStatus.OK);

	}

	public Map<String, Object> addSampleAfterCorrectionRelease(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		Map<String, Object> seqNoMap = new HashMap<>();
		seqNoMap.put("getSeqNo", true);
		seqNoMap = seqNoHistoryTableGetUpdate(seqNoMap, userInfo);
		int seqNoRegHistoryCode = (int) seqNoMap.get("sampleSeqNo");
		int seqNoSampleHistoryCode = (int) seqNoMap.get("subSampleSeqNo");
		int seqNoTestHistoryCode = (int) seqNoMap.get("testSeqNo");

		final String strAddSample = "select ntesthistorycode, npreregno, ntransactionsamplecode, ntransactiontestcode, ntransactionstatus"
				+ " from registrationtesthistory where ntesthistorycode in ( select max(ntesthistorycode)"
				+ " from registrationtesthistory where npreregno in (" + inputMap.get("npreregno")
				+ ") and ntransactionsamplecode in" + " (" + inputMap.get("ntransactionsamplecode")
				+ ") and ntransactiontestcode in (" + inputMap.get("ntransactiontestcode") + ")" + " and nsitecode = "
				+ userInfo.getNtranssitecode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by npreregno, ntransactionsamplecode, ntransactiontestcode ) and ntransactionstatus != "
				+ Enumeration.TransactionStatus.RELEASED.gettransactionstatus() + " and nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNtranssitecode();
		final List<RegistrationTestHistory> addedTestLst = (List<RegistrationTestHistory>) jdbcTemplate
				.query(strAddSample, new RegistrationTestHistory());
		if (addedTestLst.size() > 0) {
			Map<String, Object> returnMapAdd = new HashMap<>();
			returnMapAdd.putAll(inputMap);
			String nPreRegNoAdded = addedTestLst.stream().map(x -> String.valueOf(x.getNpreregno())).distinct()
					.collect(Collectors.joining(","));
			String nTransactionSampleCodeAdded = addedTestLst.stream()
					.map(x -> String.valueOf(x.getNtransactionsamplecode())).distinct()
					.collect(Collectors.joining(","));
			String nTransactionTestCodeAdded = addedTestLst.stream()
					.map(x -> String.valueOf(x.getNtransactiontestcode())).distinct().collect(Collectors.joining(","));

			returnMapAdd.put("npreregno", nPreRegNoAdded);
			returnMapAdd.put("ntransactionsamplecode", nTransactionSampleCodeAdded);
			returnMapAdd.put("ntransactiontestcode", nTransactionTestCodeAdded);
			returnMapAdd.put("nreghistorycode", seqNoRegHistoryCode);
			returnMapAdd.put("nsamplehistorycode", seqNoSampleHistoryCode);
			returnMapAdd.put("ntesthistorycode", seqNoTestHistoryCode);
			Map<String, Object> seqNoValueHistoryTable = (Map<String, Object>) (createRegistrationHistory(returnMapAdd,
					userInfo)).get("seqNoHistoryTable");
			if (seqNoValueHistoryTable != null) {
				seqNoRegHistoryCode = seqNoRegHistoryCode
						+ Integer.parseInt(seqNoValueHistoryTable.get("nreghistorycount").toString());
				seqNoSampleHistoryCode = seqNoSampleHistoryCode
						+ Integer.parseInt(seqNoValueHistoryTable.get("nsamplehistorycount").toString());
			}

			seqNoTestHistoryCode = seqNoTestHistoryCode + Arrays.asList(nTransactionTestCodeAdded.split(",")).size();
		}
		seqNoMap.put("getSeqNo", false);
		seqNoMap.put("sampleSeqNo", seqNoRegHistoryCode);
		seqNoMap.put("subSampleSeqNo", seqNoSampleHistoryCode);
		seqNoMap.put("testSeqNo", seqNoTestHistoryCode);

		seqNoHistoryTableGetUpdate(seqNoMap, userInfo);

		return null;
	}

	public Map<String, Object> deleteSamplesAfterCorrection(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> seqNoMap = new HashMap<>();
		seqNoMap.put("getSeqNo", true);
		seqNoMap = seqNoHistoryTableGetUpdate(seqNoMap, userInfo);
		int seqNoRegHistoryCode = (int) seqNoMap.get("sampleSeqNo");
		int seqNoSampleHistoryCode = (int) seqNoMap.get("subSampleSeqNo");
		int seqNoTestHistoryCode = (int) seqNoMap.get("testSeqNo");
		final String npreRegNo = inputMap.get("npreregno").toString();
		final String ntransactionSampleCode = inputMap.get("ntransactionsamplecode").toString();
		final String ntransactionTestCode = inputMap.get("ntransactiontestcode").toString();
		final boolean needSubSample = (boolean) inputMap.get("nneedsubsample");
		final String ntransCode = needSubSample == true
				? (Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ","
						+ Enumeration.TransactionStatus.PARTIALLY.gettransactionstatus())
				: (Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + "");
		final List<RegistrationTestHistory> deletedTestLst = (List<RegistrationTestHistory>) jdbcTemplate.query(
				"select ntesthistorycode, npreregno, ntransactionsamplecode, ntransactiontestcode, ntransactionstatus"
						+ " from registrationtesthistory where ntesthistorycode in (select max(ntesthistorycode) from"
						+ " registrationtesthistory where npreregno in (" + npreRegNo
						+ ") and ntransactionsamplecode in (" + ntransactionSampleCode
						+ ") and ntransactiontestcode in (" + ntransactionTestCode + ")" + " and nsitecode = "
						+ userInfo.getNtranssitecode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " group by npreregno, ntransactionsamplecode, ntransactiontestcode"
						+ ") and ntransactionstatus in ("
						+ Enumeration.TransactionStatus.RELEASED.gettransactionstatus() + ") and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
						+ userInfo.getNtranssitecode(),
				new RegistrationTestHistory());
		if (deletedTestLst.size() > 0) { // The condition is met when a previously released record is being changed to
											// approved status.

			String nPreRegNoDeleted = deletedTestLst.stream().map(x -> String.valueOf(x.getNpreregno())).distinct()
					.collect(Collectors.joining(","));
			String nTransactionSampleCodeDeleted = deletedTestLst.stream()
					.map(x -> String.valueOf(x.getNtransactionsamplecode())).distinct()
					.collect(Collectors.joining(","));
			String nTransactionTestCodeDeleted = deletedTestLst.stream()
					.map(x -> String.valueOf(x.getNtransactiontestcode())).distinct().collect(Collectors.joining(","));

			String insertStrSample = "";
			String insertStrSubSample = "";
			String insertStrTest = "";
			String strUpdateExternalOrderStatus = "";
			String strInsertExteranalOrderStatus = "";

			final List<RegistrationSampleHistory> deletedSubSampleLst = (List<RegistrationSampleHistory>) jdbcTemplate
					.query("select nsamplehistorycode, npreregno, ntransactionsamplecode, ntransactionstatus "
							+ " from registrationsamplehistory"
							+ " where nsamplehistorycode in (select max(nsamplehistorycode) from"
							+ " registrationsamplehistory where npreregno in (" + npreRegNo
							+ ") and ntransactionsamplecode in (" + ntransactionSampleCode + ")" + " and nsitecode = "
							+ userInfo.getNtranssitecode() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " group by npreregno, ntransactionsamplecode) and ntransactionstatus="
							+ Enumeration.TransactionStatus.RELEASED.gettransactionstatus() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
							+ userInfo.getNtranssitecode(), new RegistrationSampleHistory());

			if (deletedSubSampleLst.size() > 0) {
				insertStrSubSample = "insert into registrationsamplehistory (nsamplehistorycode, npreregno,"
						+ " ntransactionsamplecode, nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode,"
						+ " ntransactionstatus, dtransactiondate, noffsetdtransactiondate,ntransdatetimezonecode, scomments,"
						+ " nsitecode, nstatus)  " + " select " + seqNoSampleHistoryCode
						+ "+Rank() Over(order by nsamplehistorycode), npreregno, ntransactionsamplecode,"
						+ " nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, ntransactionstatus, '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ userInfo.getNtimezonecode() + ", N'"
						+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "', "
						+ userInfo.getNtranssitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " from registrationsamplehistory"
						+ " where nsamplehistorycode in (select max(nsamplehistorycode) from registrationsamplehistory where npreregno in ("
						+ nPreRegNoDeleted + ")" + " and ntransactionsamplecode in (" + nTransactionSampleCodeDeleted
						+ ") and ntransactionstatus in (" + ntransCode + " ) and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
						+ userInfo.getNtranssitecode() + " group by npreregno, ntransactionsamplecode)"
						+ " and nsitecode = " + userInfo.getNtranssitecode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				seqNoSampleHistoryCode = seqNoSampleHistoryCode
						+ Arrays.asList(nTransactionSampleCodeDeleted.split(",")).size();

				final List<RegistrationHistory> deletedSampleLst = (List<RegistrationHistory>) jdbcTemplate.query(
						"select nreghistorycode, npreregno, ntransactionstatus from registrationhistory where nreghistorycode in (select max(nreghistorycode) from"
								+ " registrationhistory where npreregno in (" + npreRegNo + ") group by npreregno"
								+ ") and ntransactionstatus="
								+ Enumeration.TransactionStatus.RELEASED.gettransactionstatus() + " and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
								+ userInfo.getNtranssitecode(),
						new RegistrationHistory());

				if (deletedSampleLst.size() > 0) {

					insertStrSample = "insert into registrationhistory (nreghistorycode, npreregno, nusercode, nuserrolecode,"
							+ " ndeputyusercode, ndeputyuserrolecode, ntransactionstatus, scomments, dtransactiondate,"
							+ " noffsetdtransactiondate, ntransdatetimezonecode, nsitecode, nstatus)  " + " select "
							+ seqNoRegHistoryCode
							+ "+Rank() Over(order by nreghistorycode), npreregno, nusercode, nuserrolecode,"
							+ " ndeputyusercode, ndeputyuserrolecode, ntransactionstatus, N'"
							+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "', '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ userInfo.getNtimezonecode() + ", " + userInfo.getNtranssitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " from "
							+ " registrationhistory where nreghistorycode in (select max(nreghistorycode) from registrationhistory"
							+ " where npreregno in (" + nPreRegNoDeleted + ") and ntransactionstatus in (" + ntransCode
							+ ") and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and nsitecode=" + userInfo.getNtranssitecode() + " group by npreregno);";

					seqNoRegHistoryCode = seqNoRegHistoryCode + Arrays.asList(nPreRegNoDeleted.split(",")).size();

					if ((int) inputMap.get("nportalrequired") == Enumeration.TransactionStatus.YES
							.gettransactionstatus()) {
						String strCondition = ((int) inputMap
								.get("nsampletypecode") == Enumeration.SampleType.CLINICALSPEC.getType())
										? "r.jsonuidata->'OrderCodeData'"
										: "r.jsonuidata->'nexternalordercode'";
						String strCheckPortalRecord = "select r.npreregno, eo.nexternalordercode, eo.sorderseqno, eo.sexternalorderid"
								+ " from registration r, externalorder eo where" + " eo.nexternalordercode=("
								+ strCondition + ")::int and r.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and r.nsitecode="
								+ userInfo.getNtranssitecode() + " and eo.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and eo.nsitecode="
								+ userInfo.getNtranssitecode() + " and eo.nexternalordertypecode="
								+ Enumeration.ExternalOrderType.PORTAL.getExternalOrderType() + " and r.npreregno in ("
								+ nPreRegNoDeleted + ")";
						List<ExternalOrder> lstexternalorderstatus = jdbcTemplate.query(strCheckPortalRecord,
								new ExternalOrder());
						StringBuilder queryForm = new StringBuilder();
						final String currenDate = dateUtilityFunction.getCurrentDateTime(userInfo).toString()
								.replace("T", " ").replace("Z", "");

						if (lstexternalorderstatus.size() > 0) {
							Map<String, Object> rtnObj = new HashMap<>();
							List<Map<String, Object>> objLst = new ArrayList<>();
							if ((int) inputMap.get("nsampletypecode") == Enumeration.SampleType.CLINICALSPEC
									.getType()) {
								lstexternalorderstatus.stream().forEach(lst -> {
									Map<String, Object> mapObj = new HashMap<>();
									mapObj.put("serialnumber", lst.getSorderseqno());
									mapObj.put("statuscode",
											Enumeration.TransactionStatus.PARTIALLY.gettransactionstatus());
									objLst.add(mapObj);
								});
								rtnObj.put("PortalStatus", objLst);
								rtnObj.put("url", inputMap.get("url"));
								externalOrderDAO.updateOrderSampleStatus(userInfo, inputMap);

							} else {
								lstexternalorderstatus.stream().forEach(lst -> {
									queryForm.append(" (" + lst.getNpreregno() + ", " + lst.getNexternalordercode()
											+ ", '" + lst.getSexternalorderid() + "', "
											+ Enumeration.TransactionStatus.PARTIALLY.gettransactionstatus() + ", "
											+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", '"
											+ currenDate + "', " + userInfo.getNtranssitecode() + ", "
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),");
								});
								strUpdateExternalOrderStatus = "delete from externalorderstatus where npreregno in ("
										+ nPreRegNoDeleted + ") and nsentstatus in ("
										+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
										+ ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
								strInsertExteranalOrderStatus = "insert into externalorderstatus (npreregno, nexternalordercode, sexternalorderid, ntransactionstatus, nsentstatus, dtransactiondate, nsitecode, nstatus)"
										+ " values"
										+ queryForm.toString().substring(0, queryForm.toString().length() - 1) + ";";
							}
						}
					}
				}
			}

			insertStrTest = "insert into registrationtesthistory (ntesthistorycode, nformcode, npreregno,"
					+ " ntransactionsamplecode, ntransactiontestcode, nusercode, nuserrolecode, ndeputyusercode,"
					+ " ndeputyuserrolecode, nsampleapprovalhistorycode, ntransactionstatus, dtransactiondate,"
					+ " noffsetdtransactiondate, ntransdatetimezonecode, scomments, nsitecode, nstatus)  " + " select "
					+ seqNoTestHistoryCode
					+ "+Rank() Over(order by ntesthistorycode), nformcode, npreregno, ntransactionsamplecode,"
					+ " ntransactiontestcode, nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsampleapprovalhistorycode,"
					+ " ntransactionstatus, '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNtimezonecode() + ", N'" + stringUtilityFunction.replaceQuote(userInfo.getSreason())
					+ "', " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " from registrationtesthistory where ntesthistorycode in (select max(ntesthistorycode) from registrationtesthistory where npreregno in "
					+ "(" + nPreRegNoDeleted + ") and ntransactionsamplecode in (" + nTransactionSampleCodeDeleted
					+ ") and ntransactiontestcode in (" + nTransactionTestCodeDeleted + ")"
					+ " and ntransactionstatus != " + Enumeration.TransactionStatus.RELEASED.gettransactionstatus()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNtranssitecode()
					+ " group by npreregno, ntransactionsamplecode, ntransactiontestcode);";
			seqNoTestHistoryCode = seqNoTestHistoryCode + Arrays.asList(nTransactionTestCodeDeleted.split(",")).size();

			jdbcTemplate.execute(insertStrSample + insertStrSubSample + insertStrTest + strUpdateExternalOrderStatus
					+ strInsertExteranalOrderStatus);
			seqNoMap.put("getSeqNo", false);
			seqNoMap.put("sampleSeqNo", seqNoRegHistoryCode);
			seqNoMap.put("subSampleSeqNo", seqNoSampleHistoryCode);
			seqNoMap.put("testSeqNo", seqNoTestHistoryCode);

			seqNoHistoryTableGetUpdate(seqNoMap, userInfo);
		}
		return null;
	}

	public Map<String, Object> seqNoHistoryTableGetUpdate(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		if ((Boolean) inputMap.get("getSeqNo") == true) {
			Map<String, Object> returnMap = new HashMap<>();
			final String getSeqNo = "select stablename,nsequenceno from seqnoregistration where stablename in (N'registrationtesthistory',N'registrationhistory',N'registrationsamplehistory')"
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			final List<SeqNoRegistration> lstSeqNo = jdbcTemplate.query(getSeqNo, new SeqNoRegistration());
			final Map<String, Integer> seqMap = lstSeqNo.stream()
					.collect(Collectors.toMap(SeqNoRegistration::getStablename, SeqNoRegistration::getNsequenceno));
			final int seqNoRegHistoryCode = seqMap.get("registrationhistory");
			final int seqNoSampleHistoryCode = seqMap.get("registrationsamplehistory");
			final int seqNoTestHistoryCode = seqMap.get("registrationtesthistory");
			returnMap.put("sampleSeqNo", seqNoRegHistoryCode);
			returnMap.put("subSampleSeqNo", seqNoSampleHistoryCode);
			returnMap.put("testSeqNo", seqNoTestHistoryCode);
			return returnMap;
		} else {
			String seqNoUpdate = "update seqnoregistration set nsequenceno=" + inputMap.get("sampleSeqNo") + " "
					+ " where stablename='registrationhistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			seqNoUpdate += "update seqnoregistration set nsequenceno=" + inputMap.get("subSampleSeqNo")
					+ " where stablename='registrationsamplehistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			seqNoUpdate += "update seqnoregistration set nsequenceno=" + inputMap.get("testSeqNo")
					+ " where stablename = N'registrationtesthistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			jdbcTemplate.execute(seqNoUpdate);
			return null;
		}
	}

	public List<PreliminaryReportHistory> getPreliminaryReportHistoryCount(final String ncoaParentCode,
			final UserInfo userInfo) throws Exception {
		final String strPreliminaryReportHistory = "select * from preliminaryreporthistory where npreliminaryreporthistorycode in ("
				+ " select max(npreliminaryreporthistorycode) from preliminaryreporthistory where ncoaparentcode in ("
				+ ncoaParentCode + ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  group by ncoaparentcode)"
				+ " and nsitecode = " + userInfo.getNtranssitecode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<PreliminaryReportHistory> lstPreliminaryReportHistory = jdbcTemplate
				.query(strPreliminaryReportHistory, new PreliminaryReportHistory());
		return lstPreliminaryReportHistory;
	}

	public SeqNoReleasenoGenerator checkReleaseNoGeneratorFormat(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final String strSeqnoFormatQuery = "select rsc.nperiodcode,rsc.jsondata,rsc.jsondata->>'dseqresetdate' dseqresetdate,"
				+ " rsc.jsondata->>'sreleaseformat' sreleaseformat, sag.nsequenceno,sag.nseqnoreleasenogencode,rsc.nregsubtypeversionreleasecode"
				+ " from  approvalconfig ap, seqnoreleasenogenerator sag,regsubtypeconfigversionrelease rsc "
				+ " where  sag.nseqnoreleasenogencode = rsc.nseqnoreleasenogencode " + " and sag.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ap.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rsc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ap.nsitecode = "
				+ userInfo.getNmastersitecode() + " and rsc.nsitecode = " + userInfo.getNmastersitecode()
				+ " and nregtypecode = " + (int) inputMap.get("nregtypecode") + " and nregsubtypecode ="
				+ (int) inputMap.get("nregsubtypecode")
				+ " and rsc.napprovalconfigcode=ap.napprovalconfigcode and rsc.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + "";
		return (SeqNoReleasenoGenerator) jdbcUtilityFunction.queryForObject(strSeqnoFormatQuery,
				SeqNoReleasenoGenerator.class, jdbcTemplate);
	}

	public SeqNoReleasenoGenerator insertsiteReleasenoGenerator(SeqNoReleasenoGenerator seqnoFormat, UserInfo userinfo,
			Map<String, Object> inputMap) {

		String Str = "select count(nseqnositereleasegencode) from seqnositereleasegenerator where nsitecode="
				+ userinfo.getNsitecode() + "and nseqnoreleasenogencode=" + seqnoFormat.getNseqnoreleasenogencode()
				+ " and nregsubtypeversionreleasecode=" + seqnoFormat.getNregsubtypeversionreleasecode()
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		int seqnositereleasenogenerator = jdbcTemplate.queryForObject(Str, Integer.class);
		if (seqnositereleasenogenerator == 0) {
			String insertNewSeqNo = "";
			try {
				insertNewSeqNo = "insert into seqnositereleasegenerator(nseqnositereleasegencode,nregsubtypeversionreleasecode,nseqnoreleasenogencode,nsequenceno,nsitecode,nstatus, dmodifieddate) "
						+ " values ("
						+ "(select nsequenceno+1 from seqnoregtemplateversion where stablename ='seqnositereleasegenerator') and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
						+ seqnoFormat.getNregsubtypeversionreleasecode() + "," + seqnoFormat.getNseqnoreleasenogencode()
						+ ",0," + userinfo.getNsitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ",'"
						+ dateUtilityFunction.getCurrentDateTime(userinfo) + "'" + ");";
			} catch (Exception e) {
				e.printStackTrace();
			}
			String updateseqNoSite = "update seqnoregtemplateversion set nsequenceno = nsequenceno+1 where stablename = 'seqnositereleasegenerator' and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			jdbcTemplate.execute(insertNewSeqNo + updateseqNoSite);
		}

		final String strSeqnoSiteFormatQuery = "select rsc.nperiodcode,rsc.jsondata,rsc.jsondata->>'dseqresetdate' dseqresetdate,"
				+ " rsc.jsondata->>'sreleaseformat' sreleaseformat, sasg.nsequenceno,srg.nseqnoreleasenogencode,rsc.nregsubtypeversionreleasecode"
				+ " from  approvalconfig ap, seqnoreleasenogenerator srg,regsubtypeconfigversionrelease rsc,seqnositereleasegenerator sasg "
				+ " where srg.nseqnoreleasenogencode = sasg.nseqnoreleasenogencode and  srg.nseqnoreleasenogencode = rsc.nseqnoreleasenogencode "
				+ " and srg.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ap.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nregtypecode = " + (int) inputMap.get("nregtypecode") + " and nregsubtypecode ="
				+ (int) inputMap.get("nregsubtypecode")
				+ " and rsc.napprovalconfigcode=ap.napprovalconfigcode and rsc.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and sasg.nsitecode="
				+ userinfo.getNtranssitecode() + " and rsc.nsitecode = " + userinfo.getNmastersitecode()
				+ " and rsc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ap.nsitecode = " + userinfo.getNmastersitecode() + " and sasg.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sasg.nregsubtypeversionreleasecode=" + seqnoFormat.getNregsubtypeversionreleasecode();
		seqnoFormat = jdbcTemplate.queryForObject(strSeqnoSiteFormatQuery, new SeqNoReleasenoGenerator());
		return seqnoFormat;
	}

	public boolean checkCOAParentStatus(String coaParentCode, UserInfo userInfo) {
		String strCheckStatus = "select CASE " + "    WHEN COUNT(DISTINCT ntransactionstatus) = 1 THEN true"
				+ "    ELSE false" + "  END from coaparent where ncoaparentcode in (" + coaParentCode + ")"
				+ " and nsitecode = " + userInfo.getNtranssitecode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		return jdbcTemplate.queryForObject(strCheckStatus, Boolean.class);
	};

	public void insertReleaseHistory(String ncoaParentCode, UserInfo userInfo, String additionalTransStatus)
			throws Exception {
		final String transactionStatus = (additionalTransStatus == "" || additionalTransStatus.isEmpty())
				? "cp.ntransactionstatus"
				: additionalTransStatus;
		final String strReleaseHistorySeqNo = "select nsequenceno from seqnoreleasemanagement where stablename='releasehistory'  and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final int releaseHistorySeqNo = jdbcTemplate.queryForObject(strReleaseHistorySeqNo, Integer.class);

		String strReleaseHistory = "insert into releasehistory (nreleasehistorycode, ncoaparentcode, sreportno,"
				+ " nversionno, nusercode, nuserrolecode, sreportcomments, ntransactionstatus, dtransactiondate,"
				+ " noffsetdtransactiondate, ntransdatetimezonecode, nsitecode, nstatus) select " + " "
				+ releaseHistorySeqNo + "+Rank()Over(order by cp.ncoaparentcode), cp.ncoaparentcode, cp.sreportno, "
				+ "coalesce((select case when cp.ntransactionstatus in ("
				+ Enumeration.TransactionStatus.RELEASED.gettransactionstatus() + ", "
				+ Enumeration.TransactionStatus.CORRECTION.gettransactionstatus()
				+ ") then max(crh.nversionno) when cp.ntransactionstatus = "
				+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
				+ " then case when (select count(prh.npreliminaryreporthistorycode) from preliminaryreporthistory prh where "
				+ "prh.ncoaparentcode=cp.ncoaparentcode and prh.nsitecode=" + userInfo.getNtranssitecode()
				+ " and prh.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ ")=1 then max(nversionno) else max(crh.nversionno)+1 end"
				+ " else Max(crh.nversionno) + 1 end from coareporthistory crh where crh.ncoaparentcode=cp.ncoaparentcode and crh.nsitecode="
				+ userInfo.getNtranssitecode() + " and crh.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "), 0) as nversionno, "
				+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", '"
				+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "', " + transactionStatus + ", '"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
				+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
				+ userInfo.getNtimezonecode() + ", " + userInfo.getNtranssitecode() + ", "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " from coaparent cp where cp.ncoaparentcode" + " in (" + ncoaParentCode + ") and cp.nsitecode="
				+ userInfo.getNtranssitecode() + " and cp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "; ";
		strReleaseHistory += "update seqnoreleasemanagement set nsequenceno="
				+ (releaseHistorySeqNo + ncoaParentCode.split(",").length)
				+ " where stablename='releasehistory' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "; ";
		jdbcTemplate.execute(strReleaseHistory);
	}

	public Map<String, Object> getReleaseHistoryTab(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		
		final Map<String, Object> returnMap = new HashMap<>();

		final String strReleaseHistory = "select rh.nreleasehistorycode, rh.ncoaparentcode, rh.sreportno, rh.nversionno, "
										+ "(u.sfirstname || ' ' || u.slastname) as susername, ur.suserrolename, coalesce(ts.jsondata->"
										+ "'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
										+ "', ts.jsondata->'stransdisplaystatus'->>'en-US')"
										+ " as stransdisplaystatus, s.ssitename, coalesce(to_char(rh.dtransactiondate, '"
										+ userInfo.getSpgsitedatetime() + "'), '') as sgenerateddate"
										+ " from releasehistory rh, users u, userrole ur, transactionstatus ts, site s where"
										+ " rh.ncoaparentcode in (" + inputMap.get("ncoaparentcode") + ") "
										+ " and rh.nsitecode="+ userInfo.getNtranssitecode()
										+ " and u.nusercode=rh.nusercode and ur.nuserrolecode=rh.nuserrolecode "
										+ " and ts.ntranscode=rh.ntransactionstatus "
										+ " and s.nsitecode="+ userInfo.getNmastersitecode()
										+ " and s.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
										+ " and rh.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
										+ " and u.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and ur.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
										+ " and ts.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
										+ " and u.nsitecode ="+ userInfo.getNmastersitecode()
										+ " and ur.nsitecode = " + userInfo.getNmastersitecode()
										+ " order by nreleasehistorycode desc";
		
		final List<ReleaseHistory> lstReleaseHistory = jdbcTemplate.query(strReleaseHistory, new ReleaseHistory());
		returnMap.put("ReleaseHistoryTab", lstReleaseHistory);
		return returnMap;
	}

	@Override
	public ResponseEntity<List<Map<String, Object>>> getApprovedReportTemplate(Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception {
		String strReportTemplate = " select rm.nreporttemplatecode, coalesce(rt.jsondata->'stemplatename'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "', rt.jsondata->'stemplatename'->>'en-US') sreporttemplatename,rt.nmaxsamplecount from reportmaster rm, reportdetails rd, reporttemplate rt "
				+ " where rm.nreportcode=rd.nreportcode and rd.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and rm.ntransactionstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rm.nsampletypecode="
				+ inputMap.get("nsampletypecode") + " and rm.nregtypecode=" + inputMap.get("nregtypecode")
				+ " and rm.nregsubtypecode=" + inputMap.get("nregsubtypecode") + " and rm.napproveconfversioncode="
				+ inputMap.get("napprovalversioncode") + " and rm.ncoareporttypecode="
				+ inputMap.get("ncoareporttypecode") + " and rm.nreporttypecode in ("
				+ Enumeration.ReportType.COA.getReporttype() + ", "
				+ Enumeration.ReportType.COAPRELIMINARY.getReporttype() + ", "
				+ Enumeration.ReportType.COAPREVIEW.getReporttype() + ") and rm.nsectioncode="
				+ inputMap.get("nsectioncode") + " and rm.nreporttemplatecode=rt.nreporttemplatecode and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rm.nsitecode = "
				+ userInfo.getNmastersitecode() + " and rd.nsitecode = " + userInfo.getNmastersitecode()
				+ " group by rm.nreporttemplatecode, sreporttemplatename,rt.nmaxsamplecount;";
		List<Map<String, Object>> reportTemplateList = jdbcTemplate.queryForList(strReportTemplate);
		return new ResponseEntity<>(reportTemplateList, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getApprovedReportTemplateById(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		Map<String, Object> mapRtnReportTemplate = new HashMap<>();
		final COAParent coaParentBefore = getActiveCOAParentById(inputMap, userInfo);
		if (coaParentBefore != null && (coaParentBefore.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT
				.gettransactionstatus()
				|| coaParentBefore.getNtransactionstatus() == Enumeration.TransactionStatus.CORRECTION
						.gettransactionstatus())) {
			List<Map<String, Object>> lstMapObj = getApprovedReportTemplate(inputMap, userInfo).getBody();
			returnMap.put("reportTemplateList", lstMapObj);
			Map<String, Object> mapReportTemplate = new HashMap<>();
			lstMapObj.stream().forEach(x -> {
				if (Short.parseShort(x.get("nreporttemplatecode").toString()) == coaParentBefore
						.getNreporttemplatecode()) {
					mapReportTemplate.put("nreporttemplatecode", x.get("nreporttemplatecode"));
					mapReportTemplate.put("sreporttemplatename", x.get("sreporttemplatename"));
				}
			});
			mapRtnReportTemplate.put("item", mapReportTemplate);
			mapRtnReportTemplate.put("label", mapReportTemplate.get("sreporttemplatename"));
			mapRtnReportTemplate.put("value", mapReportTemplate.get("nreporttemplatecode"));
			returnMap.put("nreporttemplatecode", mapRtnReportTemplate);
			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTCORRECTEDRECORD",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}
	}

	@Override
	public ResponseEntity<Object> updateReportTemplate(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		List<Object> lstOldAuditData = new ArrayList<>();
		List<Object> lstNewAuditData = new ArrayList<>();
		final COAParent coaParentBefore = getActiveCOAParentById(inputMap, userInfo);
		if (coaParentBefore != null && (coaParentBefore.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT
				.gettransactionstatus()
				|| coaParentBefore.getNtransactionstatus() == Enumeration.TransactionStatus.CORRECTION
						.gettransactionstatus())) {
			lstOldAuditData.add(coaParentBefore);

			// Added by sonia for ALPD-4122 on 17-05-2024 sample count validation
			int oldReportTemplateCode = coaParentBefore.getNreporttemplatecode();
			int newReportTemplateCode = (int) inputMap.get("nreporttemplatecode");
			List<Map<String, Object>> oldreportTemplateList = getReportTemplateById(oldReportTemplateCode, userInfo);
			List<Map<String, Object>> newreportTemplateList = getReportTemplateById(newReportTemplateCode, userInfo);

			int oldMaxSampleCount = (int) oldreportTemplateList.get(0).get("nmaxsamplecount");
			int newMaxSampleCount = (int) newreportTemplateList.get(0).get("nmaxsamplecount");

			if (newMaxSampleCount <= oldMaxSampleCount) {
				String strQuery = "update coaparent set nreporttemplatecode=" + inputMap.get("nreporttemplatecode")
						+ " , dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "' where ncoaparentcode=" + inputMap.get("ncoaparentcode") + " and nsitecode="
						+ userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
				jdbcTemplate.execute(strQuery);
				final COAParent coaParentAfter = getActiveCOAParentById(inputMap, userInfo);
				lstNewAuditData.add(coaParentAfter);
				auditUtilityFunction.fnInsertAuditAction(lstNewAuditData, 2, lstOldAuditData,
						Arrays.asList("IDS_EDITREPORTTEMPLATE"), userInfo);
				return null;
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SELECTEDTEMPLATESHOULDNOTEXCEEDTHEEXISTINGTEMPLATE",
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}

		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTCORRECTEDRECORD",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}
	}

	public COAParent getActiveCOAParentById(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		String strQury = "select * from coaparent where ncoaparentcode=" + inputMap.get("ncoaparentcode")
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNtranssitecode();
		return (COAParent) jdbcUtilityFunction.queryForObject(strQury, COAParent.class, jdbcTemplate);
	}

	public ResponseEntity<Object> deleteSamples(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Map<String, Object> returnMap = new HashMap<>();

		if (checkCOAParentStatus(inputMap.get("ncoaparentcode").toString(), userInfo)) {

			// For single coaparentcode only, not for multiple coaparentcode
			// ALPD-5246 Code moved here before coareporthistory reord deleted by Vishakh
			final String strCoaReportHistory = "select ncoareporthistorycode, ssystemfilename from coareporthistory where ncoaparentcode="
					+ inputMap.get("ncoaparentcode") + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNtranssitecode() + "";
			final List<COAReportHistory> lstCoaReportHistory = jdbcTemplate.query(strCoaReportHistory,
					new COAReportHistory());

			String strDelete = "update coaparent set nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
					+ ", ntransactionstatus= case when ntransactionstatus="
					+ Enumeration.TransactionStatus.CORRECTION.gettransactionstatus() + " then "
					+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus()
					+ " else ntransactionstatus end where ncoaparentcode in (" + inputMap.get("ncoaparentcode")
					+ ") and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNtranssitecode() + "; ";
			strDelete += "update coachild set nstatus=" + Enumeration.TransactionStatus.DELETED.gettransactionstatus()
					+ " where ncoaparentcode in (" + inputMap.get("ncoaparentcode") + ") and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNtranssitecode() + "; ";
			strDelete += "update externalorderattachment set nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where ncoaparentcode in ("
					+ inputMap.get("ncoaparentcode") + ") and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNtranssitecode() + "; ";
			strDelete += "update releaseoutsourceattachment set nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where ncoaparentcode in ("
					+ inputMap.get("ncoaparentcode") + ") and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNtranssitecode() + "; ";
			strDelete += "update releasetestattachment set nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where ncoaparentcode in ("
					+ inputMap.get("ncoaparentcode") + ") and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNtranssitecode() + "; ";
			strDelete += "update releasetestcomment set nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where ncoaparentcode in ("
					+ inputMap.get("ncoaparentcode") + ") and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNtranssitecode() + "; ";
			strDelete += "update reportinforelease set nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where ncoaparentcode in ("
					+ inputMap.get("ncoaparentcode") + ") and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNtranssitecode() + "; ";
			strDelete += "update coareporthistorygeneration set nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where ncoaparentcode in ("
					+ inputMap.get("ncoaparentcode") + ") and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNtranssitecode() + "; ";
			strDelete += "update patienthistory set nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where ncoaparentcode in ("
					+ inputMap.get("ncoaparentcode") + ") and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNtranssitecode() + "; ";

			// Below 3 table update scripts added by L.Subashini. They need to be updated as
			// inactive on deleting Released Record
			strDelete += "update coareporthistory set nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where ncoaparentcode in ("
					+ inputMap.get("ncoaparentcode") + ") and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNtranssitecode() + "; ";
			strDelete += "update releasehistory set nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where ncoaparentcode in ("
					+ inputMap.get("ncoaparentcode") + ") and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNtranssitecode() + "; ";
			strDelete += "update preliminaryreporthistory set nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where ncoaparentcode in ("
					+ inputMap.get("ncoaparentcode") + ") and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNtranssitecode() + "; ";

			jdbcTemplate.execute(strDelete);

			if (lstCoaReportHistory.size() > 0
					&& (int) inputMap.get("ncoaparenttranscode") == Enumeration.TransactionStatus.CORRECTION
							.gettransactionstatus()) {
				ftpUtilityFunction.deleteFTPFile(Arrays.asList(lstCoaReportHistory.get(0).getSsystemfilename()), 
						"reports",userInfo);
				deleteSamplesAfterCorrection(inputMap, userInfo);
			}

			returnMap.putAll((Map<String, Object>) getReleaseSample(inputMap, userInfo).getBody());

			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTRECORDSWITHSAMESTATUS",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		// return null;
	}

	private List<Map<String, Object>> getReportTemplateById(int reportTemplateCode, UserInfo userInfo) {
		String sQuery = "select nmaxsamplecount,jsondata->'stemplatename'->>'" + userInfo.getSlanguagetypecode()
				+ "' from reporttemplate where nreporttemplatecode =" + reportTemplateCode + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		List<Map<String, Object>> reportTemplateList = jdbcTemplate.queryForList(sQuery);
		return reportTemplateList;
	}

	public ResponseEntity<Object> sampleCountValidation(@RequestBody Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();

		final int coaParentCode = (int) inputMap.get("ncoaparentcode");
		final String sQuery = " select npreregno from coachild  where ncoaparentcode =" + coaParentCode
				+ " and nsitecode =" + userInfo.getNtranssitecode() + " and nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by npreregno ";
		final List<Map<String, Object>> coaParent = jdbcTemplate.queryForList(sQuery);
		final int samplesize = coaParent.size();

		final String reportTemplateQuery = " select rt.*  from coaparent cp,reporttemplate rt "
				+ " where cp.nreporttemplatecode =rt.nreporttemplatecode " + " and cp.ncoaparentcode =" + coaParentCode
				+ " and cp.nsitecode=" + userInfo.getNtranssitecode() + " " + " and cp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + " rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		final List<Map<String, Object>> reportTemplateList = jdbcTemplate.queryForList(reportTemplateQuery);

		final int maxSampleCount = (int) reportTemplateList.get(0).get("nmaxsamplecount");

		if (samplesize < maxSampleCount || maxSampleCount == 0) {
			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		} else {
			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.FAILED.getreturnstatus());
		}
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	// Added by sonia on 11-06-2024 for JIRA ID:4360 Auto Download reports
	// Added by sonia on 18-08-2024 for JIRA ID:4716 changed method name
	public Settings reportGenerationReleaseAction(UserInfo userInfo) throws Exception {
		final String sQuery = "select ssettingvalue from  settings where nsettingcode ="
				+ Enumeration.Settings.COAREPORT_GENERATION.getNsettingcode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		return (Settings) jdbcUtilityFunction.queryForObject(sQuery, Settings.class, jdbcTemplate);
	}

	// Added by Dhanushya RI jira id-ALPD-4459 for multiple projects under single
	// release no --Start
	public Map<String, Object> getProjectBasedAlert(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		boolean sameProject = false;

		int ncoaparentcode = inputMap.containsKey("ncoaparentcode") ? (int) inputMap.get("ncoaparentcode") : -1;
		final String sQuery = "select pm.nprojectmastercode from coaparent cp,coachild cc,registration r,projectmaster pm "
				+ " where cp.ncoaparentcode=cc.ncoaparentcode and cc.npreregno=r.npreregno  "
				+ " and pm.nprojectmastercode=r.nprojectmastercode and cc.nstatus=cp.nstatus and cp.nstatus=r.nstatus"
				+ " and r.nstatus=pm.nstatus and pm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and r.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cc.nsitecode = "
				+ userInfo.getNtranssitecode() + " and cp.nsitecode = " + userInfo.getNtranssitecode()
				+ " and r.nsitecode = " + userInfo.getNtranssitecode() + " and pm.nsitecode="
				+ userInfo.getNtranssitecode() + " and cp.ncoaparentcode=" + ncoaparentcode
				+ " group by pm.nprojectmastercode";

		List<ProjectMaster> projectObj = jdbcTemplate.query(sQuery, new ProjectMaster());

		final String strQuery = "select pm.nprojectmastercode from registration r,projectmaster pm "
				+ " where pm.nprojectmastercode=r.nprojectmastercode " + " and pm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and r.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and r.nsitecode = "
				+ userInfo.getNtranssitecode() + " and pm.nsitecode=" + userInfo.getNtranssitecode()
				+ " and r.npreregno in (" + inputMap.get("npreregno") + ") group by pm.nprojectmastercode";

		List<ProjectMaster> projectList = jdbcTemplate.query(strQuery, new ProjectMaster());

		if (inputMap.containsKey("isEditSave") && (boolean) inputMap.get("isEditSave") == true) {
			// ALPD-4797--Vignesh R(05-09-2024)
			if (!projectObj.isEmpty()) {
				sameProject = projectList.stream().anyMatch(item -> projectObj.stream()
						.anyMatch(item1 -> item.getNprojectmastercode() == item1.getNprojectmastercode()));
			}
			if (sameProject) {
				returnMap.put("projectStatus", Enumeration.TransactionStatus.SUCCESS.gettransactionstatus());

			} else {
				returnMap.put("projectStatus", Enumeration.TransactionStatus.FAILED.gettransactionstatus());
			}
		} else {

			if (!projectList.isEmpty() && projectList.size() > 1) {
				returnMap.put("projectStatus", Enumeration.TransactionStatus.FAILED.gettransactionstatus());

			} else {
				returnMap.put("projectStatus", Enumeration.TransactionStatus.SUCCESS.gettransactionstatus());
			}
		}

		return returnMap;

	}

	public Map<String, Object> getProjectTemplateBasedAlert(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		boolean sameProjectTemp = false;
		int ncoaparentcode = inputMap.containsKey("ncoaparentcode") ? (int) inputMap.get("ncoaparentcode") : -1;
		int nreporttemplatecode = inputMap.containsKey("nreporttemplatecode")
				? (int) inputMap.get("nreporttemplatecode")
				: -1;

		final String sQuery = "select cp.nreporttemplatecode from coaparent cp,coachild cc,registration r,projectmaster pm "
				+ " where cp.ncoaparentcode=cc.ncoaparentcode and cc.npreregno=r.npreregno  "
				+ " and pm.nprojectmastercode=r.nprojectmastercode and cc.nstatus=cp.nstatus and cp.nstatus=r.nstatus"
				+ " and r.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cc.nsitecode = "
				+ userInfo.getNtranssitecode() + " and cp.nsitecode = " + userInfo.getNtranssitecode()
				+ " and r.nsitecode = " + userInfo.getNtranssitecode() + " and pm.nsitecode="
				+ userInfo.getNtranssitecode() + " and cp.ncoaparentcode=" + ncoaparentcode
				+ " group by nreporttemplatecode";

		COAParent projectTemplate = (COAParent) jdbcUtilityFunction.queryForObject(sQuery, COAParent.class,
				jdbcTemplate);

		final String strQuery = "select pm.nprojectmastercode from registration r,projectmaster pm "
				+ " where pm.nprojectmastercode=r.nprojectmastercode " + " and r.nstatus=pm.nstatus and pm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and r.nsitecode = "
				+ userInfo.getNtranssitecode() + " and pm.nsitecode=" + userInfo.getNtranssitecode()
				+ " and r.npreregno in (" + inputMap.get("npreregno") + ") group by pm.nprojectmastercode";

		List<ProjectMaster> projectList = jdbcTemplate.query(strQuery, new ProjectMaster());

		final String nprojectmastercode = String.join(",", projectList.stream()
				.map(item -> String.valueOf(item.getNprojectmastercode())).collect(Collectors.toSet()));

		final String infoquery = "select nreporttemplatecode from reportinfoproject where nprojectmastercode in ("
				+ nprojectmastercode + ") and nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode = " + userInfo.getNtranssitecode() + " group by nreporttemplatecode";
		List<ReportInfoProject> projectTempList = jdbcTemplate.query(infoquery, new ReportInfoProject());

		if (inputMap.containsKey("isEditSave") && (boolean) inputMap.get("isEditSave") == true) {

			if (projectTemplate != null) {

				sameProjectTemp = projectTempList.stream()
						.noneMatch(item -> item.getNreporttemplatecode() == projectTemplate.getNreporttemplatecode());

			}
			if (sameProjectTemp == false) {
				returnMap.put("projectTempStatus", Enumeration.TransactionStatus.SUCCESS.gettransactionstatus());

			} else {
				returnMap.put("projectTempStatus", Enumeration.TransactionStatus.FAILED.gettransactionstatus());
			}
		} else {

			if (!projectTempList.isEmpty() && ((projectTempList.size() > 1) || (projectTempList.size() == 1
					&& projectTempList.get(0).getNreporttemplatecode() != nreporttemplatecode))) {
				returnMap.put("projectTempStatus", Enumeration.TransactionStatus.FAILED.gettransactionstatus());

			} else {
				returnMap.put("projectTempStatus", Enumeration.TransactionStatus.SUCCESS.gettransactionstatus());
			}
		}
		return returnMap;

	}

	// ALPD-4878-To insert data when filter name input submit,done by Dhanushya RI
	@Override
	public ResponseEntity<Object> createFilterName(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();

		Map<String, Object> objMap = new HashMap<>();
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
					+ " and (jsontempdata->'ncoareporttypecode')::int=" + inputMap.get("ncoareporttypecode") + " "
					+ " and (jsontempdata->'napprovalversioncode')::int=" + inputMap.get("napprovalversioncode") + " "
					+ " and (jsontempdata->'ndesigntemplatemappingcode')::int="
					+ inputMap.get("ndesigntemplatemappingcode") + " " + " and (jsontempdata->>'DbFromDate')='"
					+ inputMap.get("dfrom") + "' " + " and (jsontempdata->>'DbToDate')='" + inputMap.get("dto") + "' ";

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
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_FILTERALREADYPRESENT",
						userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}
		} else {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);

		}
	}

	// ALPD-4878-To show the previously saved filter name,done by Dhanushya RI
	@Override
	public List<FilterName> getFilterName(UserInfo userInfo) throws Exception {

		final String sFilterQry = "select * from filtername where nformcode=" + userInfo.getNformcode()
				+ " and nusercode=" + userInfo.getNusercode() + "" + " and nuserrolecode=" + userInfo.getNuserrole()
				+ " and nsitecode=" + userInfo.getNtranssitecode() + " " + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by 1 desc  ";

		return jdbcTemplate.query(sFilterQry, new FilterName());
	}

	// ALPD-4878-To get previously saved filter details when click the filter
	// name,done by Dhanushya RI
	@Override
	public ResponseEntity<Object> getReleaseFilter(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		/*
		 * To caculate testcount based on nflag, nflag=1 for initial get and get for
		 * after save the sample and nflag=2 for Add,edit,delete popup sample get
		 */
		ObjectMapper objMapper = new ObjectMapper();

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
			final String strValidationQuery = "select json_agg(jsondata || jsontempdata) as jsondata from filtername where nformcode="
					+ userInfo.getNformcode() + " and nusercode=" + userInfo.getNusercode() + " "
					+ " and nuserrolecode=" + userInfo.getNuserrole() + " and nsitecode=" + userInfo.getNtranssitecode()
					+ " " + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and (jsondata->'nsampletypecode')::int=" + inputMap.get("sampletypecode") + " "
					+ " and (jsondata->'nregtypecode')::int=" + inputMap.get("regtypecode") + " "
					+ " and (jsondata->'nregsubtypecode')::int=" + inputMap.get("regsubtypecode") + " "
					+ " and (jsontempdata->'napprovalconfigcode')::int=" + inputMap.get("approvalconfigurationcode")
					+ " " + " and (jsontempdata->'ndesigntemplatemappingcode')::int="
					+ inputMap.get("designtemplatcode") + " " + " and (jsontempdata->'ncoareporttypecode')::int="
					+ inputMap.get("coareporttypecode") + " " + " and (jsontempdata->>'DbFromDate')='"
					+ inputMap.get("FromDate") + "' " + " and (jsontempdata->>'DbToDate')='" + inputMap.get("ToDate")
					+ "' and nfilternamecode=" + inputMap.get("nfilternamecode") + " ";

			final String strValidationFilter = jdbcTemplate.queryForObject(strValidationQuery, String.class);

			final List<Map<String, Object>> lstValidationFilter = strValidationFilter != null
					? objMapper.readValue(strValidationFilter, new TypeReference<List<Map<String, Object>>>() {
					})
					: new ArrayList<Map<String, Object>>();
			if (lstValidationFilter.isEmpty()) {
				projectDAOSupport.updateFilterDetail(inputMap, userInfo);
				String fromDate = (String) lstfilterDetail.get(0).get("FromDate").toString();
				String toDate = (String) lstfilterDetail.get(0).get("ToDate").toString();
				fromDate = dateUtilityFunction
						.instantDateToString(dateUtilityFunction.convertStringDateToUTC(fromDate, userInfo, true));
				toDate = dateUtilityFunction
						.instantDateToString(dateUtilityFunction.convertStringDateToUTC(toDate, userInfo, true));
				inputMap.put("dfrom", fromDate);
				inputMap.put("dto", toDate);

				final DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
				final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());

				final String sampleTypeQuery = "select st.nsampletypecode,st.nportalrequired,st.nprojectspecrequired,coalesce(st.jsondata->'sampletypename'->>'"
						+ userInfo.getSlanguagetypecode()
						+ "',st.jsondata->'sampletypename'->>'en-US') ssampletypename,st.nsorter, st.nclinicaltyperequired, st.noutsourcerequired,st.nportalrequired "
						+ "from SampleType st,registrationtype rt,registrationsubtype rst,approvalconfig ac,approvalconfigversion acv "
						+ "where st.nsampletypecode > 0 and st.nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ "  and st.napprovalconfigview = " + Enumeration.TransactionStatus.YES.gettransactionstatus()
						+ " " + "and st.nsampletypecode=rt.nsampletypecode and rt.nstatus= "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "and rt.nregtypecode=rst.nregtypecode and rst.nstatus= "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "and rst.nregsubtypecode=ac.nregsubtypecode and ac.nstatus= "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "and acv.napprovalconfigcode=ac.napprovalconfigcode and acv.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acv.ntransactionstatus= "
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " " + " and rt.nsitecode="
						+ userInfo.getNmastersitecode() + " and rst.nsitecode=" + userInfo.getNmastersitecode()
						+ " and ac.nsitecode=" + userInfo.getNmastersitecode() + " and acv.nsitecode="
						+ userInfo.getNmastersitecode()
						+ " group by st.nsampletypecode,st.nsorter order by st.nsorter; ";

				List<SampleType> sampleTypeList = jdbcTemplate.query(sampleTypeQuery, new SampleType());
				if (!sampleTypeList.isEmpty()) {

					List<FilterName> lstFilterName = getFilterName(userInfo);
					final SampleType filterSampleType = !lstfilterDetail.isEmpty()
							? objMapper.convertValue(lstfilterDetail.get(0).get("sampleTypeValue"), SampleType.class)
							: sampleTypeList.get(0);
					returnMap.put("SampleType", sampleTypeList);
					returnMap.put("realSampleTypeList", sampleTypeList);
					inputMap.put("nsampletypecode", filterSampleType.getNsampletypecode());
					returnMap.put("realSampleTypeValue", filterSampleType);
					returnMap.put("realProjectRequiredValue", filterSampleType.getNprojectspecrequired());
					returnMap.put("realPortalRequiredValue", filterSampleType.getNportalrequired());
					returnMap.put("SampleTypeValue", filterSampleType);
					inputMap.put("filterDetailValue", lstfilterDetail);
					returnMap.put("FilterName", lstFilterName);
					inputMap.put("isneedsection", lstfilterDetail.get(0).get("isneedsection"));
					returnMap.putAll((Map<String, Object>) getRegistrationType(inputMap, userInfo).getBody());

					final RegistrationType filterRegType = !lstfilterDetail.isEmpty()
							? objMapper.convertValue(lstfilterDetail.get(0).get("regTypeValue"), RegistrationType.class)
							: (RegistrationType) returnMap.get("RegTypeValue");
					final RegistrationSubType filterRegSubType = !lstfilterDetail.isEmpty() ? objMapper
							.convertValue(lstfilterDetail.get(0).get("regSubTypeValue"), RegistrationSubType.class)
							: (RegistrationSubType) returnMap.get("RegSubTypeValue");
					final TransactionStatus filterTransactionStatus = !lstfilterDetail.isEmpty()
							? lstfilterDetail.get(0).containsKey("filterStatusValue")
									? objMapper.convertValue(lstfilterDetail.get(0).get("filterStatusValue"),
											TransactionStatus.class)
									: (TransactionStatus) returnMap.get("FilterStatusValue")
							: (TransactionStatus) returnMap.get("FilterStatusValue");
					final ApprovalConfigAutoapproval filterApprovalConfig = !lstfilterDetail.isEmpty()
							? lstfilterDetail.get(0).containsKey("approvalConfigValue")
									? objMapper.convertValue(lstfilterDetail.get(0).get("approvalConfigValue"),
											ApprovalConfigAutoapproval.class)
									: (ApprovalConfigAutoapproval) returnMap.get("ApprovalVersion")
							: (ApprovalConfigAutoapproval) returnMap.get("ApprovalVersion");

					final COAReportType filterCOAReportType = !lstfilterDetail.isEmpty()
							? lstfilterDetail.get(0).containsKey("reportTypeValue")
									? objMapper.convertValue(lstfilterDetail.get(0).get("reportTypeValue"),
											COAReportType.class)
									: (COAReportType) returnMap.get("ReportTypeValue")
							: (COAReportType) returnMap.get("ReportTypeValue");
					final ReactRegistrationTemplate filterDesignTemplate = !lstfilterDetail.isEmpty()
							? lstfilterDetail.get(0).containsKey("designTemplateMappingValue")
									? objMapper.convertValue(lstfilterDetail.get(0).get("designTemplateMappingValue"),
											ReactRegistrationTemplate.class)
									: (ReactRegistrationTemplate) returnMap.get("DesignTemplateMappingValue")
							: (ReactRegistrationTemplate) returnMap.get("DesignTemplateMappingValue");

					returnMap.put("realReportTypeValue", filterCOAReportType);
					inputMap.put("ncoareporttypecode", filterCOAReportType.getNcoareporttypecode());
					returnMap.put("realRegTypeValue", filterRegType);
					returnMap.put("realRegSubTypeValue", filterRegSubType);
					returnMap.put("realFilterStatusValue", filterTransactionStatus);
					returnMap.put("realApprovalVersionValue", filterApprovalConfig);
					returnMap.put("realDesignTemplateMappingValue", filterDesignTemplate);

					inputMap.put("napprovalconfigcode", filterApprovalConfig.getNapprovalconfigcode());
					inputMap.put("napprovalversioncode", lstfilterDetail.get(0).get("napprovalversioncode"));
					inputMap.put("ndesigntemplatemappingcode",
							(int) lstfilterDetail.get(0).get("ndesigntemplatemappingcode"));

					String fromDate1 = LocalDateTime.parse(fromDate, formatter1).format(formatter);
					String toDate1 = LocalDateTime.parse(toDate, formatter1).format(formatter);

					returnMap.put("realFromDate", fromDate1);
					returnMap.put("realToDate", toDate1);

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

	// Added by L.Subashini for stimulsoft viewer display of reports -07-12-2024
	public Map<String, Object> generateCOAReport(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		Map<String, Object> returnMap = new HashMap<String, Object>();
		Map<String, Object> jasperParameter = new HashMap<String, Object>();
		File JRXMLFile = (File) inputMap.get("JRXMLFile");

		jasperParameter.put("npreregno", inputMap.get("npreregno"));
		jasperParameter.put("nsectioncode", inputMap.get("nsectioncode"));
		jasperParameter.put("ncoaparentcode", inputMap.get("ncoaparentcode"));
		jasperParameter.put("ssubreportpath",
				inputMap.get("subReportPath").toString() + inputMap.get("folderName").toString());
		jasperParameter.put("simagepath", inputMap.get("imagePath").toString() + inputMap.get("folderName").toString());
		jasperParameter.put("sarno", inputMap.get("sarno"));
		jasperParameter.put("needlogo", inputMap.get("naccredit"));
		jasperParameter.put("nregtypecode", inputMap.get("nregTypeCode"));
		jasperParameter.put("nregsubtypecode", inputMap.get("nregSubTypeCode"));
		jasperParameter.put("sreportcomments", inputMap.get("sreportComments"));
		jasperParameter.put("sprimarykeyname", inputMap.get("ncoaparentcode"));
		jasperParameter.put("nlanguagecode", userInfo.getSlanguagetypecode());
		jasperParameter.put("nreporttypecode", (int) inputMap.get("nreporttypecode"));// janakumar
		jasperParameter.put("nsitecode", (int) userInfo.getNtranssitecode());
		jasperParameter.put("nreportdetailcode", inputMap.get("nreportdetailcode"));
//			
//			final String openReportNewTabQry = "select ssettingvalue from settings where nsettingcode="
//												+  Enumeration.Settings.REPORT_OPEN_NEW_TAB.getNsettingcode()
//												+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//			final Integer openReportNewTab = jdbcTemplate.queryForObject(openReportNewTabQry, Integer.class);

		final Integer openReportNewTab = (Integer) inputMap.get("openReportNewTab");
		final Integer schedulerBasedReport = (Integer) inputMap.get("schedulerBasedReport");

		final String sFileType = FilenameUtils.getExtension(JRXMLFile.toString());

		if (sFileType.equals("jrxml")) {

			returnMap.putAll(reportDAOSupport.compileAndPrintJasperReport(jasperParameter, JRXMLFile,
					(int) inputMap.get("qType"), inputMap.get("pdfPath").toString(),
					inputMap.get("sfileName").toString(), userInfo, inputMap.get("systemFileName").toString(),
					(int) inputMap.get("ncontrolcode"), true));

			returnMap.put("filetype", "jrxml");

		} else {
			boolean needFTPSave = true;
			if (inputMap.get("action").equals("Regenerate")) {
				jasperParameter.put("sreportingtoolURL", inputMap.get("sreportingtoolURL"));

				returnMap.putAll(reportDAOSupport.compileAndPrintNonJasperReport(jasperParameter, JRXMLFile,
						inputMap.get("pdfPath").toString(), inputMap.get("sfileName").toString(), userInfo,
						inputMap.get("systemFileName").toString(), (int) inputMap.get("ncontrolcode"), needFTPSave,
						inputMap.get("ssignfilename").toString(), inputMap.get("scertfilename").toString(),
						inputMap.get("ssecuritykey").toString()));

				if (returnMap.get("sourceparameter") != null) {
					final ObjectMapper objectMapper = new ObjectMapper();
					final String jsonString = (String) returnMap.get("sourceparameter");

					final Map<String, Object> sourceParameter = objectMapper.readValue(jsonString,
							new TypeReference<Map<String, Object>>() {
							});

					sourceParameter.remove("sprimarykeyname");
					sourceParameter.remove("sarno");
					sourceParameter.remove("nregtypecode");
					sourceParameter.remove("nregsubtypecode");
					sourceParameter.remove("nsitecode");
					sourceParameter.remove("nlanguagecode");
					sourceParameter.remove("nsectioncode");

					sourceParameter.put("ndecisionstatus", -1);
					sourceParameter.put("language", userInfo.getSlanguagetypecode());

					returnMap.put("sourceparameter", sourceParameter);
				}
			} else {
				// ALPD-5116 Release&Report ->View the report in the new tab
				if (openReportNewTab != null
						&& openReportNewTab == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					needFTPSave = false;

					final Map<String, Object> sourceParameter = reportDAOSupport.copyMap(jasperParameter);

					sourceParameter.remove("sprimarykeyname");
					sourceParameter.remove("sarno");
					sourceParameter.remove("nregtypecode");
					sourceParameter.remove("nregsubtypecode");
					sourceParameter.remove("nsitecode");
					sourceParameter.remove("nlanguagecode");
					sourceParameter.remove("nsectioncode");

					sourceParameter.put("ndecisionstatus", -1);
					sourceParameter.put("language", userInfo.getSlanguagetypecode());

					returnMap.put("sourceparameter", sourceParameter);

					returnMap.put("filetype", "mrt");

					returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							Enumeration.ReturnStatus.SUCCESS.getreturnstatus());

				} else {
					if (schedulerBasedReport != null
							&& schedulerBasedReport == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
						jasperParameter.put("sreportingtoolURL", inputMap.get("sreportingtoolURL"));

						returnMap.putAll(reportDAOSupport.compileAndPrintNonJasperReport(jasperParameter, JRXMLFile,
								inputMap.get("pdfPath").toString(), inputMap.get("sfileName").toString(), userInfo,
								inputMap.get("systemFileName").toString(), (int) inputMap.get("ncontrolcode"),
								needFTPSave, inputMap.get("ssignfilename").toString(),
								inputMap.get("scertfilename").toString(), inputMap.get("ssecuritykey").toString()));

						if (returnMap.get("sourceparameter") != null) {

							final ObjectMapper objectMapper = new ObjectMapper();
							final String jsonString = (String) returnMap.get("sourceparameter");

							final Map<String, Object> sourceParameter = objectMapper.readValue(jsonString,
									new TypeReference<Map<String, Object>>() {
									});

							sourceParameter.remove("sprimarykeyname");
							sourceParameter.remove("sarno");
							sourceParameter.remove("nregtypecode");
							sourceParameter.remove("nregsubtypecode");
							sourceParameter.remove("nsitecode");
							sourceParameter.remove("nlanguagecode");
							sourceParameter.remove("nsectioncode");

							sourceParameter.put("ndecisionstatus", -1);
							sourceParameter.put("language", userInfo.getSlanguagetypecode());

							returnMap.put("sourceparameter", sourceParameter);
						}
					}
				}

			}
		}

		String uploadStatus = (String) returnMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());

		if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(uploadStatus)) {

			String auditAction = "";
			String comments = commonFunction.getMultilingualMessage("IDS_REPORTNO", userInfo.getSlanguagefilename())
					+ ": " + inputMap.get("sreportno") + "; ";

			comments = comments + commonFunction.getMultilingualMessage("IDS_FILENAME", userInfo.getSlanguagefilename())
					+ ": " + returnMap.get("outputFilename") + "; ";

			final ResourceBundle resourcebundle = commonFunction.getResourceBundle(userInfo.getSlanguagefilename(),
					false);

			final String syncscomments = resourcebundle.containsKey("IDS_SYNCATTEMPT")
					? resourcebundle.getString("IDS_SYNCATTEMPT") + ": " + resourcebundle.getString("IDS_AUTOSYNC")
							+ ";"
					: "Sync Attempted : " + " No Data Modified" + "; ";

			final Map<String, Object> outputMap = new HashMap<>();
			outputMap.put("stablename", "coaparent");
			outputMap.put("sprimarykeyvalue", inputMap.get("ncoaparentcode"));

			if (Boolean.TRUE.equals(inputMap.get("auditstatus")) && inputMap.get("syncAction").equals("autoSync")) {

				boolean isSync = true;
				outputMap.put("isSync", isSync);
				outputMap.put("dataStatus", "IDS_AUTOSYNC");
				outputMap.put("syncscomments", syncscomments);
				auditAction = "IDS_AUTOSYNC";

				auditUtilityFunction.insertAuditAction(userInfo, auditAction, syncscomments, outputMap);

			} else if (Boolean.FALSE.equals(inputMap.get("auditstatus"))
					&& inputMap.get("syncAction").equals("manualsync")) {
				LOGGER.error("error: Maunal Audit was already captured.");
			} else {
				if ("Regenerate".equals(inputMap.get("action"))) {
					auditAction = "IDS_REPORTREGENERATED";
				} else {
					if ("preview".equals(inputMap.get("auditAction"))) {
						auditAction = "IDS_PREVIEWREPORT";
					} else if ("preliminary".equals(inputMap.get("auditAction"))) {
						auditAction = "IDS_PRELIMINARYREPORT";
					} else if ("release".equals(inputMap.get("auditAction"))) {
						auditAction = "IDS_RELEASE";
					}
				}
				auditUtilityFunction.insertAuditAction(userInfo, auditAction, comments, outputMap);
			}

			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
			returnMap.put("ReportPDFFile", returnMap.get("outputFilename"));

		}

		return returnMap;

	}

	// ALPD-5189 added by Dhanushya RI,To insert comments into releasecomment table
	@Override
	public ResponseEntity<Object> createReleaseComment(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		List<Object> lstOldAuditData = new ArrayList<>();
		List<Object> lstNewAuditData = new ArrayList<>();

		final String sLock = " lock table lockreleasecomment " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus()
				+ "";
		jdbcTemplate.execute(sLock);

		final String sValidCheck = "select * from coaparent where " + " ncoaparentcode="
				+ inputMap.get("ncoaparentcode") + " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final COAParent objParent = (COAParent) jdbcUtilityFunction.queryForObject(sValidCheck, COAParent.class,
				jdbcTemplate);

		if (objParent != null
				&& objParent.getNtransactionstatus() != Enumeration.TransactionStatus.RELEASED.gettransactionstatus()) {

			final String strQuery = "select * from releasecomment where " + " ncoaparentcode="
					+ inputMap.get("ncoaparentcode") + " and nsitecode=" + userInfo.getNtranssitecode()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final ReleaseComment objComment = (ReleaseComment) jdbcUtilityFunction.queryForObject(strQuery,
					ReleaseComment.class, jdbcTemplate);

			final ReleaseComment commentOldDetails = getReleaseCommentDetails(inputMap, userInfo);
			lstOldAuditData.add(commentOldDetails);

			if (objComment == null) {
				int seqNoReleaseComment = jdbcTemplate.queryForObject(
						"select nsequenceno from seqnoreleasemanagement where stablename='releasecomment' and nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
						Integer.class);
				seqNoReleaseComment++;

				final String strReleaseComment = "insert into releasecomment (nreleasecommentcode, ncoaparentcode, sreleasecomments, nusercode, nuserrolecode, ndeputyusercode,"
						+ " ndeputyuserrolecode, dtransactiondate, ntransdatetimezonecode, noffsetdtransactiondate, "
						+ " nsitecode, nstatus) " + " values (" + seqNoReleaseComment + ", "
						+ inputMap.get("ncoaparentcode") + ", '"
						+ stringUtilityFunction.replaceQuote(inputMap.get("sreleasecomments").toString()) + "', "
						+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
						+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ userInfo.getNtranssitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

				jdbcTemplate.execute(strReleaseComment);

				final String strSeqUpdate = "update seqnoreleasemanagement set nsequenceno =" + seqNoReleaseComment
						+ " where stablename ='releasecomment' and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				jdbcTemplate.execute(strSeqUpdate);
			} else {
				final String sQuery = " update releasecomment set sreleasecomments='"
						+ stringUtilityFunction.replaceQuote(inputMap.get("sreleasecomments").toString()) + "' where "
						+ " ncoaparentcode=" + inputMap.get("ncoaparentcode") + " and nsitecode="
						+ userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

				jdbcTemplate.execute(sQuery);

			}

			final ReleaseComment commentNewDetails = getReleaseCommentDetails(inputMap, userInfo);
			lstNewAuditData.add(commentNewDetails);
			auditUtilityFunction.fnInsertAuditAction(lstNewAuditData, 1, null,
					Arrays.asList("IDS_CREATERELEASECOMMENT"), userInfo);
			return null;
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_RELEASEDRECORDISNOTALLOWEDTOEDIT",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}
	}

	// ALPD-5189 added by Dhanushya RI,To get comment details for each release
	// number
	@Override
	public ReleaseComment getReleaseCommentDetails(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		String strQury = "select u.nusercode,ur.nuserrolecode,rc.nreleasecommentcode,cp.ncoaparentcode,cp.sreportno,rc.sreleasecomments,ur.suserrolename,CONCAT(u.sfirstname,' ',u.slastname) as susername "
				+ " from coaparent cp,releasecomment rc,users u,userrole ur  where rc.ncoaparentcode=cp.ncoaparentcode "
				+ " and rc.ncoaparentcode=" + inputMap.get("ncoaparentcode") + " and "
				+ " u.nusercode=rc.nusercode and ur.nuserrolecode=rc.nuserrolecode and " + " rc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + " cp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + " u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cp.nsitecode="
				+ userInfo.getNtranssitecode() + " and rc.nsitecode=" + userInfo.getNtranssitecode()
				+ " and u.nsitecode=" + userInfo.getNmastersitecode() + " and ur.nsitecode="
				+ userInfo.getNmastersitecode();
		return (ReleaseComment) jdbcUtilityFunction.queryForObject(strQury, ReleaseComment.class, jdbcTemplate);
	}

}