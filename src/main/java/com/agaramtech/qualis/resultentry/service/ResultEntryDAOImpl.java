package com.agaramtech.qualis.resultentry.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.attachmentscomments.service.attachments.AttachmentDAO;
import com.agaramtech.qualis.attachmentscomments.service.comments.CommentDAO;
import com.agaramtech.qualis.basemaster.model.TransactionStatus;
import com.agaramtech.qualis.checklist.model.ChecklistQB;
import com.agaramtech.qualis.configuration.model.ApprovalConfigAutoapproval;
import com.agaramtech.qualis.configuration.model.ApprovalConfigRole;
import com.agaramtech.qualis.configuration.model.FilterName;
import com.agaramtech.qualis.configuration.model.SampleType;
import com.agaramtech.qualis.configuration.model.Settings;
import com.agaramtech.qualis.credential.model.ControlMaster;
import com.agaramtech.qualis.credential.model.Users;
import com.agaramtech.qualis.dynamicpreregdesign.model.ReactRegistrationTemplate;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationSubType;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationType;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.EmailDAOSupport;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.TransactionDAOSupport;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.instrumentmanagement.model.Instrument;
import com.agaramtech.qualis.instrumentmanagement.model.InstrumentCategory;
import com.agaramtech.qualis.materialmanagement.model.Material;
import com.agaramtech.qualis.materialmanagement.model.MaterialCategory;
import com.agaramtech.qualis.materialmanagement.model.MaterialInventory;
import com.agaramtech.qualis.materialmanagement.model.MaterialInventoryTrans;
import com.agaramtech.qualis.materialmanagement.model.MaterialType;
import com.agaramtech.qualis.materialmanagement.service.materialcategory.MaterialCategoryDAO;
import com.agaramtech.qualis.organization.model.Section;
import com.agaramtech.qualis.registration.model.RegistrationHistory;
import com.agaramtech.qualis.registration.model.RegistrationSampleArno;
import com.agaramtech.qualis.registration.model.RegistrationSection;
import com.agaramtech.qualis.registration.model.RegistrationTest;
import com.agaramtech.qualis.registration.model.RegistrationTestHistory;
import com.agaramtech.qualis.registration.model.ResultChangeHistory;
import com.agaramtech.qualis.registration.model.ResultCheckList;
import com.agaramtech.qualis.registration.model.ResultParamCommentHistory;
import com.agaramtech.qualis.registration.model.ResultParameter;
import com.agaramtech.qualis.registration.model.ResultParameterComments;
import com.agaramtech.qualis.registration.model.ResultUsedInstrument;
import com.agaramtech.qualis.registration.model.ResultUsedMaterial;
import com.agaramtech.qualis.registration.model.ResultUsedTasks;
import com.agaramtech.qualis.registration.model.SeqNoRegistration;
import com.agaramtech.qualis.testgroup.model.SeqNoTestGroupmanagement;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecSampleType;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecification;
import com.agaramtech.qualis.testgroup.model.TestGroupTest;
import com.agaramtech.qualis.testgroup.model.TestGroupTestCharParameter;
import com.agaramtech.qualis.testgroup.model.TestGroupTestFormula;
import com.agaramtech.qualis.testgroup.model.TestGroupTestNumericParameter;
import com.agaramtech.qualis.testgroup.model.TestGroupTestParameter;
import com.agaramtech.qualis.testgroup.model.TestGroupTestPredefinedParameter;
import com.agaramtech.qualis.testgroup.model.TestGroupTestPredefinedSubCode;
import com.agaramtech.qualis.testmanagement.model.DynamicFormulaFields;
import com.agaramtech.qualis.testmanagement.model.Grade;
import com.agaramtech.qualis.testmanagement.model.TestFormula;
import com.agaramtech.qualis.testmanagement.model.TestMasterClinicalSpec;
import com.agaramtech.qualis.testmanagement.model.TestParameter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
@Transactional(rollbackFor = Exception.class)
public class ResultEntryDAOImpl implements ResultEntryDAO {


	private static final Logger LOGGER = LoggerFactory.getLogger(ResultEntryDAOImpl.class);

	private final TransactionDAOSupport transactionDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final StringUtilityFunction stringUtilityFunction;
	private final FTPUtilityFunction ftpUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final EmailDAOSupport emailDAOSupport;
	private final AttachmentDAO attachmentDAO;
	private final CommentDAO commentDAO;
	private final RulesEngineFunctions objRulesEngineFunctions;
	private final MaterialCategoryDAO materialCatDAO;

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getResultEntryCombo(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> returnMap = new HashMap<>();
		
		
		returnMap.putAll((Map<String, Object>) inputMap.get("settings"));
		
		inputMap.put("nneedReceivedInLab",Integer.valueOf(returnMap.get("43").toString()));
		
		returnMap.clear();
		String dfromdate = "";
		String dtodate = "";

		//ALPD-4870-To get previously save filter details when initial get,done by Dhanushya RI
		final String strQuery="select json_agg(jsondata || jsontempdata) as jsondata from filterdetail "
							+ " where nformcode="+userInfo.getNformcode() 
							+ " and nusercode="+userInfo.getNusercode()+" "
							+ " and nuserrolecode="+userInfo.getNuserrole()
							+ " and nsitecode="+userInfo.getNtranssitecode()
							+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final String strFilter = jdbcTemplate.queryForObject(strQuery, String.class);

		final ObjectMapper objMapper = new ObjectMapper();
		final List<Map<String, Object>> lstFilterDetail = strFilter != null 
														? objMapper.readValue(strFilter, new TypeReference<List<Map<String, Object>>>() {})
														: new ArrayList<Map<String, Object>>();

		if(!lstFilterDetail.isEmpty() && lstFilterDetail.get(0).containsKey("fromDate")
				&& lstFilterDetail.get(0).containsKey("toDate") ){

			final Instant instantFromDate = dateUtilityFunction.convertStringDateToUTC(lstFilterDetail.get(0).get("fromDate").toString(),userInfo, true);
			final Instant instantToDate = dateUtilityFunction.convertStringDateToUTC(lstFilterDetail.get(0).get("toDate").toString(),userInfo, true);

			dfromdate = dateUtilityFunction.instantDateToString(instantFromDate);
			dtodate = dateUtilityFunction.instantDateToString(instantToDate);

			returnMap.put("FromDate", dfromdate);
			returnMap.put("ToDate", dtodate);

			final DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
			final LocalDateTime ldt = LocalDateTime.parse(dfromdate, formatter1);
			final LocalDateTime ldt1 = LocalDateTime.parse(dtodate, formatter1);

			String formattedFromString = "";
			String formattedToString = "";
			final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());
			

			if (userInfo.getIsutcenabled()== Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				final ZonedDateTime zonedDateFromTime = ZonedDateTime.of(ldt, ZoneId.of(userInfo.getStimezoneid()));
				formattedFromString = zonedDateFromTime.format(formatter);
				final ZonedDateTime zonedDateToTime = ZonedDateTime.of(ldt1, ZoneId.of(userInfo.getStimezoneid()));
				formattedToString = zonedDateToTime.format(formatter);

			} else {
				formattedFromString = formatter.format(ldt);
				formattedToString= formatter.format(ldt1);

			}

			returnMap.put("realFromDate", formattedFromString);
			returnMap.put("realToDate", formattedToString);
		}
		else{
			final Map<String, Object> mapObject = projectDAOSupport.getDateFromControlProperties(userInfo,
					(String) inputMap.get("currentdate"), "datetime", "FromDate");
			dfromdate = (String) mapObject.get("FromDate");
			dtodate = (String) mapObject.get("ToDate");

			returnMap.put("fromDate", mapObject.get("FromDateWOUTC"));
			returnMap.put("toDate", mapObject.get("ToDateWOUTC"));
			returnMap.put("realFromDate", mapObject.get("FromDateWOUTC"));
			returnMap.put("realToDate", mapObject.get("ToDateWOUTC"));
		}

		inputMap.put("fromdate", dfromdate);
		inputMap.put("todate", dtodate);
		inputMap.put("ntransactiontestcode", 0);
		inputMap.put("checkBoxOperation", 3);

		final String getSampleType = "select st.nsampletypecode,coalesce(st.jsondata->'sampletypename'->>'"
									+ userInfo.getSlanguagetypecode() + "',st.jsondata->'sampletypename'->>'en-US') ssampletypename,st.nclinicaltyperequired,st.nportalrequired "
									+ " from sampletype st,approvalconfig ac,approvalconfigversion acv,"
									+ " registrationtype rt,registrationsubtype rst"
									+ " where ac.napprovalconfigcode = acv.napprovalconfigcode "
									+ " and rt.nregtypecode = ac.nregtypecode"
									+ " and rt.nregtypecode = rst.nregtypecode and rst.nregsubtypecode = ac.nregsubtypecode"
									+ " and rt.nsampletypecode = st.nsampletypecode and ac.nsitecode = acv.nsitecode "
									+ " and acv.nsitecode = "+ userInfo.getNmastersitecode() + " "
									+ " and st.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
									+ " and acv.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
									+ " and acv.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+" "
									+ " and ac.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
									+ " and rst.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
									+ " and rt.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
									+ " and st.nsampletypecode > 0 "
									+ " group by st.nsampletypecode,st.jsondata->'sampletypename'->>'" + userInfo.getSlanguagetypecode()+ "'"
									+ " order by st.nsorter";

		final List<SampleType> lstsampleType = jdbcTemplate.query(getSampleType, new SampleType());

		if (!lstsampleType.isEmpty()) {
			
			final List<FilterName> lstFilterName = getFilterName(userInfo);
			
			final SampleType filterSampleType = !lstFilterDetail.isEmpty()
							?objMapper.convertValue(lstFilterDetail.get(0).get("sampleTypeValue"),SampleType.class)
									:lstsampleType.get(0);

			returnMap.put("SampleType", lstsampleType);
			returnMap.put("realSampleTypeList", lstsampleType);
			inputMap.put("nsampletypecode", filterSampleType.getNsampletypecode());
			returnMap.put("defaultSampleType", filterSampleType);
			inputMap.put("defaultSampleType", filterSampleType);
			returnMap.put("realSampleTypeValue", filterSampleType);
			inputMap.put("filterDetailValue", lstFilterDetail);

			returnMap.putAll((Map<String, Object>) getRegistrationType(inputMap, userInfo).getBody());
			returnMap.put("realRegTypeValue", returnMap.get("defaultRegistrationType"));
			returnMap.put("realRegSubTypeValue", returnMap.get("defaultRegistrationSubType"));
			returnMap.put("realApproveConfigVersion", returnMap.get("defaultApprovalConfigVersion"));
			returnMap.put("realFilterStatusValue", returnMap.get("defaultFilterStatus"));
			returnMap.put("realTestcodeValue", returnMap.get("defaultTestvalues"));
			returnMap.put("FilterName",lstFilterName);
		}
		returnMap.put("activeTestKey", "IDS_RESULTS");
		return new ResponseEntity<>(returnMap, HttpStatus.OK);

	}


	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getRegistrationType(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> returnMap = new HashMap<>();
		ObjectMapper objMapper = new ObjectMapper();

		String Str="Select * from sampletype  where  nsampletypecode="+inputMap.get("nsampletypecode");

		SampleType obj=(SampleType) jdbcUtilityFunction.queryForObject (Str,  SampleType.class,jdbcTemplate);

		String ValidationQuery="";

		if( obj.getNtransfiltertypecode()!=-1 &&  userInfo.getNusercode()!=-1 ){
			int nmappingfieldcode = (obj.getNtransfiltertypecode() == 1) ? userInfo.getNdeptcode() : userInfo.getNuserrole();	

			ValidationQuery= " and rst.nregsubtypecode in( "
					+ "		SELECT rs.nregsubtypecode "
					+ "		FROM registrationsubtype rs "
					+ "		INNER JOIN transactionfiltertypeconfig ttc ON rs.nregsubtypecode = ttc.nregsubtypecode "
					+ "		LEFT JOIN transactionusers tu ON tu.ntransfiltertypeconfigcode = ttc.ntransfiltertypeconfigcode "
					+ "		WHERE ( ttc.nneedalluser = "+Enumeration.TransactionStatus.YES.gettransactionstatus()+"  "
					+ " and ttc.nstatus =  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" "
					+ " AND ttc.nmappingfieldcode = "+nmappingfieldcode+ ") "
					+ "	 OR "
					+ "	( ttc.nneedalluser = "+Enumeration.TransactionStatus.NO.gettransactionstatus()+"  "
					+ "	  AND ttc.nmappingfieldcode ="+nmappingfieldcode
					+ "	  AND tu.nusercode ="+userInfo.getNusercode()
					+ "   and ttc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "   and tu.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ") "
					+ "  OR "
					+ "	( ttc.nneedalluser = "+Enumeration.TransactionStatus.NO.gettransactionstatus()+"  "
					+ " AND ttc.nmappingfieldcode = -1 "
					+ " AND tu.nusercode ="+userInfo.getNusercode() 
					+ " and ttc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tu.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ") "
					+ "	 AND rs.nregtypecode = rt.nregtypecode) "	;
		}

		final String getRegType = "select rt.nregtypecode,coalesce(rt.jsondata->'sregtypename'->>'"
				+ userInfo.getSlanguagetypecode() + "',rt.jsondata->'sregtypename'->>'en-US') as sregtypename "
				+ " from approvalconfig ac,approvalconfigversion acv,registrationtype rt,registrationsubtype rst"
				+ " where "
				+ " ac.napprovalconfigcode = acv.napprovalconfigcode and rt.nregtypecode = ac.nregtypecode"
				+ " and rt.nregtypecode = rst.nregtypecode and rst.nregsubtypecode = ac.nregsubtypecode"
				+ " and acv.nsitecode = " + userInfo.getNmastersitecode() + " "
				+ " and rt.nsitecode = " + userInfo.getNmastersitecode() + " "
				+ " and rst.nsitecode = " + userInfo.getNmastersitecode() + " "
				+ " and rt.nsampletypecode = "+ inputMap.get("nsampletypecode") + " "
				+ " and acv.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rt.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rst.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and ac.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and acv.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+" "
				+ " and rt.nregtypecode > 0 "
				+ ValidationQuery
				+ " group by rt.nregtypecode,coalesce(rt.jsondata->'sregtypename'->>'"+ userInfo.getSlanguagetypecode()
				+ "',rt.jsondata->'sregtypename'->>'en-US')  order by rt.nregtypecode desc;";

		List<RegistrationType> regTypeList = jdbcTemplate.query(getRegType, new RegistrationType());

		if (!regTypeList.isEmpty()) {
			//ALPD-4870-To get previously save filter details when initial get,done by Dhanushya RI

			
			final RegistrationType filterRegType=inputMap.containsKey("filterDetailValue") && !((List<Map<String, Object>>) inputMap.get("filterDetailValue")).isEmpty()
					&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0).containsKey("regTypeValue")
					? objMapper.convertValue(((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0).get("regTypeValue"),RegistrationType.class) :regTypeList.get(0);

			returnMap.put("defaultRegistrationType", filterRegType);
			//returnMap.put("realRegTypeValue", regTypeList.get(0));
			returnMap.put("RegistrationType", regTypeList);
			returnMap.put("realRegistrationTypeList", regTypeList);

			inputMap.put("defaultRegistrationType", filterRegType);
			inputMap.put("nregtypecode", filterRegType.getNregtypecode());
			returnMap.putAll((Map<String, Object>) getRegistrationsubType(inputMap, userInfo).getBody());
		}
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getRegistrationsubType(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> returnMap = new HashMap<>();
		ObjectMapper objMapper = new ObjectMapper();

		String Str="Select * from registrationtype rt,sampletype st "
				+ " where rt.nsampletypecode=st.nsampletypecode "
				+ " and rt.nregTypeCode="+inputMap.get("nregtypecode")+" "
				+ " and rt.nsitecode = st.nsitecode and st.nsitecode = "+userInfo.getNmastersitecode()+" "
				+ " and rt.nstatus = st.nstatus and st.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";

		SampleType obj=(SampleType) jdbcUtilityFunction.queryForObject(Str,  SampleType.class,jdbcTemplate);

		String validationQuery="";

		if( obj.getNtransfiltertypecode()!=-1 && userInfo.getNusercode()!=-1 ){

			final int nmappingfieldcode = (obj.getNtransfiltertypecode() == 1) ? userInfo.getNdeptcode() : userInfo.getNuserrole();	

			validationQuery=" and rst.nregsubtypecode in( "
					+ " SELECT rs.nregsubtypecode "
					+ " FROM registrationsubtype rs "
					+ " INNER JOIN transactionfiltertypeconfig ttc ON rs.nregsubtypecode = ttc.nregsubtypecode "
					+ " LEFT JOIN transactionusers tu ON tu.ntransfiltertypeconfigcode = ttc.ntransfiltertypeconfigcode "
					+ " WHERE ( ttc.nneedalluser = "+Enumeration.TransactionStatus.YES.gettransactionstatus()+"  and ttc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" AND ttc.nmappingfieldcode = "+nmappingfieldcode+ ")"
					+ "  OR "
					+ "	( ttc.nneedalluser = "+Enumeration.TransactionStatus.NO.gettransactionstatus()+"  "
					+ " AND ttc.nmappingfieldcode ="+nmappingfieldcode 
					+ " AND tu.nusercode ="+userInfo.getNusercode() 
					+ " and ttc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tu.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ") "
					+ "  OR "
					+ "	( ttc.nneedalluser = "+Enumeration.TransactionStatus.NO.gettransactionstatus()+"  "
					+ " AND ttc.nmappingfieldcode = -1 "
					+ " AND tu.nusercode ="+userInfo.getNusercode() 
					+ " and ttc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tu.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ") "
					+ " AND rs.nregtypecode = "+inputMap.get("nregtypecode")+")";
		} 

		final String getRegSubType = "select rst.nregsubtypecode,rst.nregtypecode,"
				+ "coalesce(rst.jsondata->'sregsubtypename'->>'" + userInfo.getSlanguagetypecode()
				+ "',rst.jsondata->'sregsubtypename'->>'en-US') as sregsubtypename,cast(rsc.jsondata->>'nneedbatch' as boolean) nneedbatch,"
				+ " cast(rsc.jsondata->>'nneedsubsample' as boolean) nneedsubsample,cast(rsc.jsondata->>'nneedtemplatebasedflow' as boolean) nneedtemplatebasedflow,"
				+ " cast(rsc.jsondata->>'nneedworklist' as boolean) nneedworklist,cast(rsc.jsondata->>'nneedtestinitiate' as boolean) nneedtestinitiate "
				+ " ,cast(rsc.jsondata->>'nneedjoballocation' as boolean) nneedjoballocation ,cast(rsc.jsondata->>'nneedmyjob' as boolean) nneedmyjob "
				+ " from approvalconfig ac,approvalconfigversion acv,registrationsubtype rst,regsubtypeconfigversion rsc"
				+ " where "
				+ "  rsc.napprovalconfigcode = ac.napprovalconfigcode  "
				+ " and ac.napprovalconfigcode = acv.napprovalconfigcode "
				+ " and ac.napprovalconfigcode = acv.napprovalconfigcode and rst.nregsubtypecode = ac.nregsubtypecode"
				+ " and rst.nregsubtypecode > 0 " + " "
				+ " and acv.nsitecode = " + userInfo.getNmastersitecode() + " "
				+ " and rst.nsitecode = " + userInfo.getNmastersitecode() + " "
				+ " and rsc.nsitecode = " + userInfo.getNmastersitecode() + " "
				+ " and acv.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rst.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and ac.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and acv.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+" "
				+ " and rsc.ntransactionstatus = "+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+" "
				+ " and rsc.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rst.nregtypecode = "+ inputMap.get("nregtypecode")+""
				+ validationQuery
				+ " order by rst.nregsubtypecode desc";

		List<RegistrationSubType> regSubTypeList = jdbcTemplate.query(getRegSubType, new RegistrationSubType());

		if (!regSubTypeList.isEmpty()) {
			//ALPD-4870-To get previously save filter details when initial get,done by Dhanushya RI

			final RegistrationSubType filterRegSubType=inputMap.containsKey("filterDetailValue") && !((List<Map<String, Object>>) inputMap.get("filterDetailValue")).isEmpty()
					&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0).containsKey("regSubTypeValue")
					? objMapper.convertValue(((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0).get("regSubTypeValue"),RegistrationSubType.class) :regSubTypeList.get(0);

			returnMap.put("defaultRegistrationSubType", filterRegSubType);
			returnMap.put("RegistrationSubType", regSubTypeList);
			returnMap.put("realRegistrationSubTypeList", regSubTypeList);
			inputMap.put("nregsubtypecode", filterRegSubType.getNregsubtypecode());
			inputMap.put("nneedsubsample", filterRegSubType.isNneedsubsample());
			returnMap.put("nneedsubsample", filterRegSubType.isNneedsubsample());
			inputMap.put("defaultRegistrationSubType", filterRegSubType);
			inputMap.put("nneedtemplatebasedflow", filterRegSubType.isNneedtemplatebasedflow());
			inputMap.put("nneedjoballocation", filterRegSubType.isNneedjoballocation());
			returnMap.put("nneedmyjob", filterRegSubType.isNneedmyjob());
			inputMap.put("nneedmyjob", filterRegSubType.isNneedmyjob());


			returnMap.putAll((Map<String, Object>) getApprovalConfigVersion(inputMap, userInfo).getBody());
			inputMap.put("ndesigntemplatemappingcode", returnMap.get("ndesigntemplatemappingcode"));
		}
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	// Aravindh
	public ResponseEntity<Object> getRegistrationTemplateList(int nregtypecode, int nregsubtypecode) throws Exception {
		// TODO Auto-generated method stub

		final String str = "select dm.ndesigntemplatemappingcode,rt.jsondata,rt.sregtemplatename from designtemplatemapping dm, "
				+ " reactregistrationtemplate rt where nregtypecode=" + nregtypecode + " and nregsubtypecode="
				+ nregsubtypecode + " and rt.nreactregtemplatecode=dm.nreactregtemplatecode "
				+ " and dm.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rt.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and dm.ntransactionstatus in" + " ("+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus()+ ") order by dm.ntransactionstatus desc";

		List<ReactRegistrationTemplate> lstReactRegistrationTemplate = jdbcTemplate.query(str,
				new ReactRegistrationTemplate());

		return new ResponseEntity<>(lstReactRegistrationTemplate, HttpStatus.OK);

	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getApprovalConfigVersion(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final Map<String, Object> returnMap = new HashMap<>();
		ObjectMapper objMapper = new ObjectMapper();

		final String fromDate = dateUtilityFunction.instantDateToString(
				dateUtilityFunction.convertStringDateToUTC((String) inputMap.get("fromdate"), userInfo, true));
		final String toDate = dateUtilityFunction.instantDateToString(
				dateUtilityFunction.convertStringDateToUTC((String) inputMap.get("todate"), userInfo, true));

		// ALPD-5596 - acv.napproveconfversioncode added by Gowtham R on 20/03/2025 - to get the analyzed users list for complete result.
		final String getApprovalConfigVersion = "select aca.sversionname,aca.napprovalconfigcode,aca.napprovalconfigversioncode,"
				+ " acv.ntransactionstatus,acv.ntreeversiontempcode, acv.napproveconfversioncode"
				+ " from registration r,registrationarno rar,registrationhistory rh,approvalconfigautoapproval aca,"
				+ " approvalconfig ac,approvalconfigversion acv "
				+ "    where r.npreregno=rar.npreregno and r.npreregno=rh.npreregno"
				+ " and rar.napprovalversioncode=acv.napproveconfversioncode"
				+ " and acv.napproveconfversioncode=aca.napprovalconfigversioncode"
				+ " and r.nregtypecode=ac.nregtypecode and r.nregsubtypecode=ac.nregsubtypecode"
				+ " and r.nregtypecode =" + inputMap.get("nregtypecode") + " "
				+ " and r.nregsubtypecode ="+ inputMap.get("nregsubtypecode") + " "
				+ " and rh.ntransactionstatus = "+ Enumeration.TransactionStatus.REGISTERED.gettransactionstatus() + " "
				+ "and acv.nsitecode ="	+ userInfo.getNmastersitecode() + " "
				+ " and rh.dtransactiondate between '" + fromDate + "' and '" + toDate+ "' "
				+ " and r.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rar.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rh.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and ac.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and ac.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and acv.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and aca.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by acv.ntransactionstatus,aca.napprovalconfigversioncode,acv.ntreeversiontempcode,"
				+ " aca.napprovalconfigcode, aca.sversionname, acv.napproveconfversioncode";

		final List<ApprovalConfigAutoapproval> approvalVersion = jdbcTemplate.query(getApprovalConfigVersion,
				new ApprovalConfigAutoapproval());

		if (!approvalVersion.isEmpty()) {
			List<ApprovalConfigAutoapproval> approvedApprovalVersionList = approvalVersion.stream().filter(
					obj -> obj.getNtransactionstatus() == Enumeration.TransactionStatus.APPROVED.gettransactionstatus())
					.collect(Collectors.toList());


			if (approvedApprovalVersionList != null && !approvedApprovalVersionList.isEmpty()) {
				//ALPD-4870-To get previously save filter details when initial get,done by Dhanushya RI

				final ApprovalConfigAutoapproval filterApprovalConfig=inputMap.containsKey("filterDetailValue") && !((List<Map<String, Object>>) inputMap.get("filterDetailValue")).isEmpty()
						&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0).containsKey("approvalConfigValue")
						? objMapper.convertValue(((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0).get("approvalConfigValue"),ApprovalConfigAutoapproval.class) :approvedApprovalVersionList.get(0);

				inputMap.put("napprovalversioncode",
						filterApprovalConfig.getNapprovalconfigversioncode());
				inputMap.put("defaultApprovalConfigVersion", filterApprovalConfig);
				returnMap.put("defaultApprovalConfigVersion", filterApprovalConfig);
				returnMap.put("realApproveConfigVersion", filterApprovalConfig);
			} else {
				//ALPD-4870-To get previously save filter details when initial get,done by Dhanushya RI

				final ApprovalConfigAutoapproval filterApprovalConfig=inputMap.containsKey("filterDetailValue") && !((List<Map<String, Object>>) inputMap.get("filterDetailValue")).isEmpty()
						&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0).containsKey("approvalConfigValue")
						? objMapper.convertValue(((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0).get("approvalConfigValue"),ApprovalConfigAutoapproval.class) :approvalVersion.get(0);

				inputMap.put("defaultApprovalConfigVersion", filterApprovalConfig);
				inputMap.put("napprovalversioncode", filterApprovalConfig.getNapprovalconfigversioncode());
				returnMap.put("defaultApprovalConfigVersion", filterApprovalConfig);
			}
			returnMap.putAll((Map<String, Object>) getApproveConfigVersionRegTemplateDesign(inputMap,userInfo).getBody());

		} else {
			returnMap.put("defaultApprovalConfigVersion", approvalVersion);
		}
		inputMap.put("njobstatuscode", 1);
		returnMap.putAll((Map<String, Object>) getFilterStatus(inputMap, userInfo).getBody());
		returnMap.put("ApprovalConfigVersion", approvalVersion);
		returnMap.put("realApprovalConfigVersionList", approvalVersion);

		final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());

		final String fromDateUI = LocalDateTime.parse((String) inputMap.get("fromdate"), dbPattern).format(uiPattern);
		final String toDateUI = LocalDateTime.parse((String) inputMap.get("todate"), dbPattern).format(uiPattern);
		returnMap.put("fromDate", fromDateUI);
		returnMap.put("toDate", toDateUI);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}


	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getApproveConfigVersionRegTemplateDesign(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception 
	{
		final Map<String, Object> returnMap = new HashMap<>();
		ObjectMapper objMapper = new ObjectMapper();

		returnMap.putAll((Map<String, Object>) projectDAOSupport.getApproveConfigVersionBasedTemplate(
				Short.valueOf(inputMap.get("nregtypecode").toString()),Short.valueOf(inputMap.get("nregsubtypecode").toString()),
				(int)inputMap.get("napprovalversioncode")).getBody());
		//ALPD-4870-To get previously save filter details when initial get,done by Dhanushya RI

		final ReactRegistrationTemplate filterDesignMapping=inputMap.containsKey("filterDetailValue") && !((List<Map<String, Object>>) inputMap.get("filterDetailValue")).isEmpty()
				&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0).containsKey("designTemplateMappingValue")
				? objMapper.convertValue(((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0).get("designTemplateMappingValue"),ReactRegistrationTemplate.class) :
					(ReactRegistrationTemplate) returnMap.get("DesignTemplateMappingValue");

		returnMap.put("realDesignTemplateMapping", filterDesignMapping);
		returnMap.put("DesignTemplateMappingValue", filterDesignMapping);
		returnMap.put("ndesigntemplatemappingcode",filterDesignMapping.getNdesigntemplatemappingcode());

		inputMap.put("ndesigntemplatemappingcode", returnMap.get("ndesigntemplatemappingcode"));
		returnMap.putAll((Map<String, Object>) getTestBasedOnCombo(inputMap, userInfo).getBody());
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getFilterStatus(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		List<TransactionStatus> lstfilterdetails = new ArrayList<>();
		ObjectMapper objMapper = new ObjectMapper();

		String strQuery = "";

		final Short regTypeCode = ((Object) inputMap.get("nregtypecode")).getClass().getSimpleName()
				.equalsIgnoreCase("Integer") ? ((Integer) inputMap.get("nregtypecode")).shortValue()
						: (Short) inputMap.get("nregtypecode");
		final Short regSubTypeCode = ((Object) inputMap.get("nregsubtypecode")).getClass().getSimpleName()
				.equalsIgnoreCase("Integer") ? ((Integer) inputMap.get("nregsubtypecode")).shortValue()
						: (Short) inputMap.get("nregsubtypecode");
		;

		if (inputMap.containsKey("napprovalversioncode") && inputMap.containsKey("njobstatuscode")) {
			if ((int) inputMap.get("njobstatuscode") == 1) {
				strQuery = "select sc.ntranscode as ntransactionstatus,ts.jsondata->'stransdisplaystatus'->>'"
						+ userInfo.getSlanguagetypecode() + "' sfilterstatus,sc.nsorter "
						+ " from approvalstatusconfig sc,transactionstatus ts where ts.ntranscode = sc.ntranscode "
						+ " and sc.nformcode = " + Enumeration.FormCode.RESULTENTRY.getFormCode()
						+ " and sc.nsitecode = " + userInfo.getNmastersitecode() + " "
						+ "and sc.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "and ts.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and sc.nregtypecode="+ regTypeCode + " "
						+ " and sc.nregsubtypecode="+ regSubTypeCode+" "
						+ " order by ts.ntranscode asc";

			} else {
				strQuery = "select " + userInfo.getNmastersitecode() + " nsitecode, "
						+ Enumeration.FormCode.RESULTENTRY.getFormCode()
						+ " nformcode,ntranscode as ntransactionstatus, stransdisplaystatus as sfilterstatus from transactionstatus where ntranscode = "+Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()+"";

			}
			lstfilterdetails = jdbcTemplate.query(strQuery, new TransactionStatus());
		}

		if((int)inputMap.get("nneedReceivedInLab")==Enumeration.TransactionStatus.YES.gettransactionstatus() && (boolean)inputMap.get("nneedjoballocation") && !(boolean)inputMap.get("nneedmyjob")) {

			final String query="select ntranscode as ntransactionstatus ,jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"' as sfilterstatus "
					+ "  from transactionstatus where  ntranscode="+Enumeration.TransactionStatus.RECIEVED_IN_SECTION.gettransactionstatus()+" and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";

			List<TransactionStatus> transactionStatus= jdbcTemplate.query(query, new TransactionStatus());

			transactionStatus.get(0).setNsorter(lstfilterdetails.size()+1);
			lstfilterdetails.addAll(transactionStatus);
		}
		if (!lstfilterdetails.isEmpty()) {
			//ALPD-4870-To get previously save filter details when initial get,done by Dhanushya RI

			final TransactionStatus filterTransactionStatus=inputMap.containsKey("filterDetailValue") && !((List<Map<String, Object>>) inputMap.get("filterDetailValue")).isEmpty()
					&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0).containsKey("filterStatusValue")
					? objMapper.convertValue(((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0).get("filterStatusValue"),TransactionStatus.class) :lstfilterdetails.get(0);

			returnMap.put("defaultFilterStatus", filterTransactionStatus);
			inputMap.put("defaultFilterStatus", filterTransactionStatus);

			if(lstfilterdetails.get(0).getNtransactionstatus() == Enumeration.TransactionStatus.ALL.gettransactionstatus())
			{
				final String stranscode = 	lstfilterdetails.stream().map(x -> String.valueOf(x.getNtransactionstatus()))
						.collect(Collectors.joining(","));
				inputMap.put("ntranscode", stranscode);
			}
			else
			{
				inputMap.put("ntranscode", String.valueOf(filterTransactionStatus.getNtransactionstatus()));
			}


		} else {
			returnMap.put("defaultFilterStatus", lstfilterdetails);
		}
		returnMap.put("REFilterStatus", lstfilterdetails);
		returnMap.put("realFilterStatusList", lstfilterdetails);
		returnMap.putAll((Map<String, Object>) getTestBasedOnCombo(inputMap, userInfo).getBody());
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getTestBasedOnCombo(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> returnMap = new HashMap<>();
		ObjectMapper objMapper = new ObjectMapper();
		List<?> lsttestvalues = new ArrayList<>();
		final Map<String, Object> map = new HashMap<>();
		final List<Object> lstobj = new ArrayList<>();

		if (inputMap.containsKey("napprovalversioncode")) {
			final String getTest = "select tgt.stestsynonym,tgt.ntestcode,max(jb.ntestrescheduleno) from testgrouptest tgt, registrationtest rt,joballocation jb"
					+ " where rt.ntestgrouptestcode = tgt.ntestgrouptestcode " + " and rt.ntransactiontestcode = any ( "
					+ " select rth.ntransactiontestcode from registrationtesthistory rth,registrationarno rar,registration r "
					+ " where rth.ntesthistorycode = any ( "
					+ " select max(ntesthistorycode) from registrationtesthistory where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "  group by ntransactiontestcode ) and r.npreregno = rar.npreregno "
					+ " and rth.ntransactionstatus in ( " + inputMap.get("ntranscode") + ")  "
					+ " and r.nregtypecode = " + inputMap.get("nregtypecode")
					+ " and r.nregsubtypecode = " + inputMap.get("nregsubtypecode")
					+ " and r.ndesigntemplatemappingcode =" + inputMap.get("ndesigntemplatemappingcode")
					+ " and rth.dtransactiondate between '" + inputMap.get("fromdate") + "' and '"
					+ inputMap.get("todate") + "'" + " and rar.npreregno = rth.npreregno and rar.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and rar.napprovalversioncode = " + inputMap.get("napprovalversioncode") + " )"
					+ " and jb.nusercode in (" + userInfo.getNusercode() + ",-1) "
					+ " and tgt.nsectioncode = any ("
					+ " select ls.nsectioncode from sectionusers su,labsection ls where ls.nlabsectioncode=su.nlabsectioncode and"
					+ " su.nusercode = "+ userInfo.getNusercode() + " and su.nsitecode= "+ userInfo.getNtranssitecode() + "  "
					+ " and ls.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and su.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " group by ls.nsectioncode"
					+ " ) and rt.nsitecode = jb.nsitecode and rt.nsitecode = "+userInfo.getNtranssitecode() +" and jb.ntransactiontestcode = rt.ntransactiontestcode" + " "
					+ " group by tgt.stestsynonym,tgt.ntestcode"
					+ " order by tgt.ntestcode";

			lsttestvalues = jdbcTemplate.queryForList(getTest);
		}

		if (!lsttestvalues.isEmpty()) {

			map.put("ntestcode", 0);
			map.put("stestsynonym", commonFunction.getMultilingualMessage("IDS_ALL", userInfo.getSlanguagefilename()));
			lstobj.add(map);
			lstobj.addAll(lsttestvalues);

			final Object filterTransactionTest=inputMap.containsKey("filterDetailValue") && !((List<Map<String, Object>>) inputMap.get("filterDetailValue")).isEmpty()
					&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0).containsKey("testValue")
					? ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0).get("testValue") :lstobj.get(0);

			returnMap.put("defaultTestvalues", filterTransactionTest);
			inputMap.put("ntestcode", ((Map<String,Object>)filterTransactionTest).get("ntestcode"));

			RegistrationSubType objRegistrationsubtype = objMapper.convertValue(inputMap.get("defaultRegistrationSubType"), new TypeReference<RegistrationSubType>(){
			});

			if(objRegistrationsubtype.isNneedbatch()== true || objRegistrationsubtype.isNneedworklist()== true)
			{
				returnMap.putAll((Map<String, Object>) getConfigurationFilter(inputMap, userInfo).getBody());
			}

			if (inputMap.containsKey("nflag") && (int) inputMap.get("nflag") == 1) {
				inputMap.put("activeTestKey", "IDS_RESULTS");
				inputMap.put("activeSampleKey", "IDS_SAMPLEATTACHMENT");
				returnMap.putAll((Map<String, Object>) getResultEntryDetails(inputMap, userInfo).getBody());
			}
		} else {
			returnMap.put("defaultTestvalues", lstobj);
			returnMap.put("Batchvalues", new ArrayList<>());
			returnMap.put("Worklistvalues",new ArrayList<>());
			returnMap.put("defaultBatchvalue", new ArrayList<>());
			returnMap.put("defaultWorklistvalue", new ArrayList<>());
			returnMap.put("ConfigurationFilterValues", new ArrayList<>());
			returnMap.put("defaultConfigurationFilterValue", new ArrayList<>());


		}
		returnMap.put("Testvalues", lstobj);
		returnMap.put("realTestvaluesList", lstobj);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}




	@Override
	public ResponseEntity<Object> getResultEntryDetails(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		int ntestcode = -1;
//		final String ntranscode = (String) inputMap.get("ntranscode");
//		final short nsampletypecode = Short.parseShort(String.valueOf(inputMap.get("nsampletypecode")));
//		final short nregtypecode = Short.parseShort(String.valueOf(inputMap.get("nregtypecode")));
//		final short nregsubtypecode = Short.parseShort(String.valueOf(inputMap.get("nregsubtypecode")));
		final short napprovalversioncode = Short.parseShort(String.valueOf(inputMap.get("napprovalversioncode")));
//		final int nformcode = userInfo.getNformcode();
		String fromDate = "";
		String toDate = "";

		ObjectMapper objMapper = new ObjectMapper();

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

		if (!inputMap.containsKey("ntestcode")) {
			inputMap.put("ntestcode", ntestcode);
		}

		final int analystCount = projectDAOSupport.getApprovalConfigVersionByUserRoleTemplate(napprovalversioncode,userInfo);

		//ALPD-4870-To insert data when filter submit,done by Dhanushya RI
		if(inputMap.containsKey("saveFilterSubmit") &&  (boolean) inputMap.get("saveFilterSubmit") ==true) {
			projectDAOSupport.createFilterSubmit(inputMap,userInfo);
		}
		if (analystCount > 0) {
			List<FilterName> lstFilterName=getFilterName(userInfo);
			returnMap.put("FilterName",lstFilterName);

			if (!inputMap.containsKey("nflag")) {
				inputMap.put("nflag", 1);
			}

			if(inputMap.containsKey("completeActionCall")) {
				returnMap.putAll(getResultEntrySamples(inputMap, userInfo));
			}

			if ((int) inputMap.get("nflag") == 1 ) {
				returnMap.putAll(getResultEntrySamples(inputMap, userInfo));
				returnMap.put("DynamicDesign",projectDAOSupport.getTemplateDesign((int)inputMap.get("ndesigntemplatemappingcode"), userInfo.getNformcode()));
			}
			else if ((int) inputMap.get("nflag") == 2)
				returnMap.putAll(getResultEntrySubSamples(inputMap, userInfo));
			else if ((int) inputMap.get("nflag") == 3)
				returnMap.putAll(getResultEntryTest(inputMap, userInfo));
			List<Map<String, Object>> lstmapObjectsamples = objMapper.convertValue(returnMap.get("RE_SAMPLE"),
					new TypeReference<List<Map<String, Object>>>() {
			});

			if (lstmapObjectsamples != null && !lstmapObjectsamples.isEmpty()) {
				returnMap.put("RESelectedSample",
						Arrays.asList(lstmapObjectsamples.get(lstmapObjectsamples.size() - 1)));

			} else {
				if (lstmapObjectsamples != null) {
					returnMap.put("RESelectedSample", lstmapObjectsamples);
				}
				returnMap.put("RegistrationAttachment", lstmapObjectsamples);
				returnMap.put("SampleApprovalHistory", lstmapObjectsamples);
			}

			List<Map<String, Object>> lstmapObjectsubsample = objMapper.convertValue(returnMap.get("RE_SUBSAMPLE"),
					new TypeReference<List<Map<String, Object>>>() {
			});
			if ((boolean) inputMap.get("nneedsubsample")) {
				if (lstmapObjectsubsample != null && !lstmapObjectsubsample.isEmpty()) {
					returnMap.put("RESelectedSubSample",
							Arrays.asList(lstmapObjectsubsample.get(lstmapObjectsubsample.size() - 1)));
				}
			} else {
				returnMap.put("RESelectedSubSample", lstmapObjectsubsample);
			}
			returnMap.putAll(getActiveSampleTabData((String) inputMap.get("npreregno"),
					inputMap.containsKey("activeSampleKey") ? (String) inputMap.get("activeSampleKey") : "", userInfo));
			List<Map<String, Object>> lstmapObjecttest = objMapper.convertValue(returnMap.get("RE_TEST"),
					new TypeReference<List<Map<String, Object>>>() {
			});
			if (lstmapObjecttest != null && !lstmapObjecttest.isEmpty()) {
				returnMap.put("RESelectedTest", Arrays.asList(lstmapObjecttest.get(lstmapObjecttest.size() - 1)));

				//Comment by Vignesh R
				//returnMap.putAll(getSDMSLabMaster(String.valueOf(lstmapObjecttest.get(lstmapObjecttest.size() - 1).get("ntransactiontestcode"))));

				returnMap.putAll(getActiveTestTabData(
						lstmapObjecttest.get(lstmapObjecttest.size() - 1).get("ntransactiontestcode").toString(),
						(String) inputMap.get("activeTestKey"), userInfo));
			} else {
				returnMap.put("RESelectedTest", new ArrayList<>());
				returnMap.put("TestParameters", "");
				returnMap.put("ResultUsedInstrument", "");
				returnMap.put("ResultUsedMaterial", "");
				returnMap.put("ResultUsedTasks", "");
				returnMap.put("RegistrationTestAttachment", "");
				returnMap.put("RegistrationTestComment", "");
				returnMap.put("ResultChangeHistory", "");
			}
		} else {
			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),"IDS_USERNOTINRESULTENTRYFLOW");
			returnMap.put("RE_SAMPLE", new ArrayList<>());
			returnMap.put("RE_SUBSAMPLE", new ArrayList<>());
			returnMap.put("RE_TEST", new ArrayList<>());
			returnMap.put("RESelectedTest", new ArrayList<>());
			returnMap.put("TestParameters", "");
			returnMap.put("ResultUsedInstrument", "");
			returnMap.put("ResultUsedMaterial", "");
			returnMap.put("ResultUsedTasks", "");
			returnMap.put("RegistrationTestAttachment", "");
			returnMap.put("RegistrationTestComment", "");
			returnMap.put("ResultChangeHistory", "");
			return new ResponseEntity<>(returnMap, HttpStatus.CONFLICT);
		}

		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getResultEntrySubSampleDetails(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		ObjectMapper objMapper = new ObjectMapper();
		int ntestcode = -1;
//		final String ntranscode = (String) inputMap.get("ntranscode");
//		final short nsampletypecode = Short.parseShort(String.valueOf(inputMap.get("nsampletypecode")));
//		final short nregtypecode = Short.parseShort(String.valueOf(inputMap.get("nregtypecode")));
//		final short nregsubtypecode = Short.parseShort(String.valueOf(inputMap.get("nregsubtypecode")));
//		final int nformcode = userInfo.getNformcode();
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

		if (!inputMap.containsKey("ntestcode")) {
			inputMap.put("ntestcode", ntestcode);
		}
		if (!inputMap.containsKey("nflag")) {
			inputMap.put("nflag", 1);
		}
		returnMap.putAll(getResultEntryTest(inputMap, userInfo));

		List<Map<String, Object>> lstmapObjecttest = objMapper.convertValue(returnMap.get("RE_TEST"),
				new TypeReference<List<Map<String, Object>>>() {
		});
		if (lstmapObjecttest != null && !lstmapObjecttest.isEmpty()) {
			returnMap.put("RESelectedTest", Arrays.asList(lstmapObjecttest.get(0)));
			inputMap.put("ntransactiontestcode",
					lstmapObjecttest.get(lstmapObjecttest.size() - 1).get("ntransactiontestcode"));

			if ((int) inputMap.get("checkBoxOperation") == 7) {
				String stransactiontestcode = "";
				List<String> subSampleArray = Arrays
						.asList(((String) inputMap.get("ntransactionsamplecode")).split(","));
				for (Map<String, Object> map : lstmapObjecttest) {
					if ((int) map.get("ntransactionsamplecode") == Integer.parseInt(subSampleArray.get(0))) {
						stransactiontestcode = map.get("ntransactiontestcode").toString();
						// break;
					}
				}
				inputMap.put("ntransactiontestcode", stransactiontestcode);
			}

			returnMap.putAll(getActiveSubSampleTabData(inputMap.get("ntransactionsamplecode").toString(),
					(String) inputMap.get("activeTestKey"), userInfo));

			returnMap.putAll(getActiveTestTabData(inputMap.get("ntransactiontestcode").toString(),
					(String) inputMap.get("activeTestKey"), userInfo));
		} else {
			returnMap.put("RESelectedTest", new ArrayList<>());
			returnMap.put("TestParameters", "");
			returnMap.put("ResultUsedInstrument", "");
			returnMap.put("ResultUsedMaterial", "");
			returnMap.put("ResultUsedTasks", "");
			returnMap.put("RegistrationTestAttachment", "");
			returnMap.put("RegistrationTestComment", "");
			returnMap.put("ResultChangeHistory", "");
		}

		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	public Map<String, Object> getResultEntrySamples(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		final Map<String, Object> returnMap = new HashMap<>();
		final String transCode = (String) inputMap.get("ntranscode");
		final short sampleTypeCode = Short.parseShort(String.valueOf(inputMap.get("nsampletypecode")));
		final short regTypeCode = Short.parseShort(String.valueOf(inputMap.get("nregtypecode")));
		final short regSubTypeCode = Short.parseShort(String.valueOf(inputMap.get("nregsubtypecode")));
		final int nworklistcode = inputMap.containsKey("nworlistcode") ? (int) inputMap.get("nworlistcode") : -1;
		final int nbatchmastercode = inputMap.containsKey("nbatchmastercode") ? (int) inputMap.get("nbatchmastercode") : -1;

		List<RegistrationSubType> subTypeValue=getRegistrationSubTypeValues(inputMap,userInfo);
		int designtemplatemapping = -1;
		if (inputMap.containsKey("nneedtemplatebasedflow") && (boolean) inputMap.get("nneedtemplatebasedflow")) {
			designtemplatemapping = (int) inputMap.get("ndesigntemplatemappingcode");
		} 

		Date resultdate1 = new Date(System.currentTimeMillis()); 

		LOGGER.info("Sample Get Start Time:-->"+resultdate1);
		final String getSampleQuery = "SELECT * from fn_resultentrysampleget('" + inputMap.get("fromdate") + "', '"
				+ inputMap.get("todate") + "', " + "" + sampleTypeCode + " , " + regTypeCode + ", " + regSubTypeCode
				+ ", " + userInfo.getNusercode() + ", '" + transCode + "', " + inputMap.get("napprovalversioncode")
				+ ", " + inputMap.get("ntestcode") + ", " + userInfo.getNtranssitecode() + ",'"
				+ userInfo.getSlanguagetypecode() + "'," + designtemplatemapping + ","+nworklistcode+","+nbatchmastercode+","
				+ " "+inputMap.get("nneedReceivedInLab")+","+subTypeValue.get(0).isNneedjoballocation()+","
				+ " "+subTypeValue.get(0).isNneedmyjob()+");";
		LOGGER.info(getSampleQuery);

		String sample =  jdbcTemplate.queryForObject(getSampleQuery, String.class); 

		resultdate1 = new Date(System.currentTimeMillis()); 

		LOGGER.info("Sample Get End Time:-->"+resultdate1);

		List<Map<String, Object>> sampleList = new ArrayList<>();

		if (sample != null) {
			sampleList = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(sample, userInfo, true,(int) inputMap.get("ndesigntemplatemappingcode"), 
					"sample");
		}

		returnMap.put("RE_SAMPLE", sampleList);
		if(!inputMap.containsKey("completeActionCall")){
			if (!sampleList.isEmpty()) {
				final String spreregno = String.valueOf(sampleList.get(sampleList.size() - 1).get("npreregno"));
				inputMap.put("npreregno", spreregno);
				returnMap.putAll(getResultEntrySubSamples(inputMap, userInfo));
				returnMap.put("RESelectedSample", Arrays.asList(sampleList.get(sampleList.size() - 1)));
			} else {
				returnMap.put("RE_SUBSAMPLE", sampleList);
				returnMap.put("RE_TEST", sampleList);
			}
		}

		return returnMap;

	}

	public Map<String, Object> getResultEntrySubSamples(Map<String, Object> inputMap, UserInfo userInfo)
			throws IllegalArgumentException, Exception {
		final Map<String, Object> returnMap = new HashMap<>();
		final String transCode = (String) inputMap.get("ntranscode");
		final boolean subSampleNeed = (boolean) inputMap.get("nneedsubsample");
		final int nworklistcode = inputMap.containsKey("nworlistcode") ? (int) inputMap.get("nworlistcode") : -1;
		final int nbatchmastercode = inputMap.containsKey("nbatchmastercode") ? (int) inputMap.get("nbatchmastercode") : -1;
		final short nregTypeCode = Short.parseShort(String.valueOf(inputMap.get("nregtypecode")));
		final short nregSubTypeCode = Short.parseShort(String.valueOf(inputMap.get("nregsubtypecode")));

		Date resultdate1 = new Date(System.currentTimeMillis()); 
		List<RegistrationSubType> subTypeValue=getRegistrationSubTypeValues(inputMap,userInfo);
		LOGGER.info("Sub Sample Get Start Time:-->"+resultdate1);

		final String getSampleQuery = "SELECT * from fn_resultentrysubsampleget("
				+ nregTypeCode + ", " + nregSubTypeCode + ",'" + inputMap.get("npreregno") + "'::text,"
				+ userInfo.getNusercode() + ", '" + transCode + "'::text, " + inputMap.get("ntestcode") + ", "
				+ userInfo.getNtranssitecode() + ",'" + userInfo.getSlanguagetypecode() + "'::text,"+nworklistcode+","+nbatchmastercode+","+inputMap.get("nneedReceivedInLab")+","
				+ subTypeValue.get(0).isNneedjoballocation()+","
				+ subTypeValue.get(0).isNneedmyjob()+");";
		LOGGER.info(getSampleQuery);
		String sample = (String) jdbcUtilityFunction.queryForObject(getSampleQuery, String.class,jdbcTemplate);

		resultdate1 = new Date(System.currentTimeMillis());

		LOGGER.info("Sub Sample Get End Time:-->" + resultdate1);

		// List<RegistrationSample> subSampleList =
		// getJdbcTemplate().query(getSampleQuery, new RegistrationSample());
		List<Map<String, Object>> subSampleList=new ArrayList<Map<String,Object>>();
		if(sample!=null) {
			subSampleList = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(sample, userInfo, true, (int) inputMap.get("ndesigntemplatemappingcode"), "subsample");
		}


		returnMap.put("RE_SUBSAMPLE", subSampleList);
		if (!subSampleList.isEmpty()) {
			if (subSampleNeed) {
				if (inputMap.containsKey("checkBoxOperation") && (int) inputMap.get("checkBoxOperation") == 3 || (int) inputMap.get("checkBoxOperation") == 5
						|| (int) inputMap.get("checkBoxOperation") == 7) { 

					final String stransactionsampleno = String
							.valueOf(subSampleList.get(subSampleList.size() - 1).get("ntransactionsamplecode"));
					inputMap.put("ntransactionsamplecode", stransactionsampleno);
					returnMap.putAll(getActiveSubSampleTabData(stransactionsampleno,(String) inputMap.get("activeTestKey"), userInfo));
					returnMap.putAll(getResultEntryTest(inputMap, userInfo));
				}
			} else {
				returnMap.put("RESelectedSubSample", subSampleList);
				final String stransactionsampleno = subSampleList.stream()
						.map(x -> String.valueOf(x.get("ntransactionsamplecode"))).collect(Collectors.joining(","));
				inputMap.put("ntransactionsamplecode", stransactionsampleno);
				returnMap.putAll(getActiveSubSampleTabData(stransactionsampleno,
						(String) inputMap.get("activeTestKey"), userInfo));
				returnMap.putAll(getResultEntryTest(inputMap, userInfo));
			}

		} else {
			returnMap.put("RESelectedSubSample", subSampleList);
			if (subSampleNeed || (inputMap.containsKey("checkBoxOperation") && (int) inputMap.get("checkBoxOperation") == 3)) {
				returnMap.put("RESelectedTest", subSampleList);
				returnMap.put("RegistrationGetTest", subSampleList);
				returnMap.put("activeSampleKey", "IDS_SAMPLEATTACHMENTS");
				returnMap.put("activeTestKey", "IDS_PARAMETERRESULTS");
			}
		}

		return returnMap;

	}

	public Map<String, Object> getResultEntryTest(Map<String, Object> inputMap, UserInfo userInfo)
			throws IllegalArgumentException, Exception {
		final Map<String, Object> returnMap = new HashMap<>();
		final String transCode = (String) inputMap.get("ntranscode");
		final int nworklistcode = inputMap.containsKey("nworlistcode") ? (int) inputMap.get("nworlistcode") : -1;
		final int nbatchmastercode = inputMap.containsKey("nbatchmastercode") ? (int) inputMap.get("nbatchmastercode") : -1;

		final short nregtypecode = Short.parseShort(String.valueOf(inputMap.get("nregtypecode")));
		final short nregsubtypecode = Short.parseShort(String.valueOf(inputMap.get("nregsubtypecode")));

		List<RegistrationSubType> subTypeValue=getRegistrationSubTypeValues(inputMap,userInfo);
		Date resultdate1 = new Date(System.currentTimeMillis());

		LOGGER.info("Test Get Start Time:-->" + resultdate1);

		boolean updateAnalyst = false;
		String query ="select ssettingvalue from settings where nsettingcode ="+Enumeration.Settings.UPDATING_ANALYSER.getNsettingcode()+" ";
		Settings objSettings = (Settings) jdbcUtilityFunction.queryForObject(query, Settings.class,jdbcTemplate);

		if(objSettings !=null) {
			updateAnalyst = Integer.valueOf(objSettings.getSsettingvalue())==Enumeration.TransactionStatus.YES.gettransactionstatus() ? true:false;
		}

		final String getSampleQuery = "SELECT * from public.fn_resultentrytestget("
				+ nregtypecode + "," + nregsubtypecode + ",'"
				+ inputMap.get("ntransactionsamplecode") + "', " + userInfo.getNusercode() + ", '" + transCode + "', "
				+ inputMap.get("ntestcode") + ", " + userInfo.getNtranssitecode() + ",'"
				+ userInfo.getSlanguagetypecode() + "'" + ", " + inputMap.get("ntransactiontestcode") + ","+nworklistcode+","+nbatchmastercode+","+inputMap.get("nneedReceivedInLab")+","
				+ subTypeValue.get(0).isNneedjoballocation()+","
				+ subTypeValue.get(0).isNneedmyjob()+","+updateAnalyst+");";

		LOGGER.info(getSampleQuery);

		String sample = jdbcTemplate.queryForObject(getSampleQuery, String.class);

		resultdate1 = new Date(System.currentTimeMillis());

		LOGGER.info("Test Get End Time:-->" + resultdate1);

		List<Map<String, Object>> testList = new ArrayList<>();
		if (sample != null) {
			testList = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(sample, userInfo, true,
					(int) inputMap.get("ndesigntemplatemappingcode"), "test");// ,

		}

		returnMap.put("RE_TEST", testList);

		if (!testList.isEmpty()) {
			returnMap.put("RESelectedTest", Arrays.asList(testList.get(testList.size() - 1)));
			returnMap.put("activeSampleKey", "IDS_SAMPLEATTACHMENTS");
			returnMap.put("activeTestKey", "IDS_PARAMETERRESULTS");
		} else {
			returnMap.put("RESelectedTest", testList);
			returnMap.put("activeSampleKey", "IDS_SAMPLEATTACHMENTS");
			returnMap.put("activeTestKey", "IDS_PARAMETERRESULTS");
		}
		return returnMap;

	}

	private Map<String, Object> getActiveSampleTabData(String npreregno, String activeTabName, UserInfo userInfo)
			throws Exception {
		switch (activeTabName) {

		case "IDS_SAMPLEATTACHMENTS":
			return (Map<String, Object>) attachmentDAO.getSampleAttachment(npreregno, userInfo).getBody();
		case "IDS_APPROVALHISTORY":
			return Collections.emptyMap();
		case "IDS_SAMPLECOMMENTS":
			return (Map<String, Object>) commentDAO.getSampleComment(npreregno, userInfo).getBody();
			//return Collections.emptyMap();
		case "IDS_SUBSAMPLEATTACHMENTS":
			return Collections.emptyMap();
		case "IDS_SUBSAMPLECOMMENTS":
			return Collections.emptyMap();
		case "IDS_DOCUMENTS":
			return Collections.emptyMap();
		default:
			return (Map<String, Object>) attachmentDAO.getSampleAttachment(npreregno, userInfo).getBody();
		}
	}

	@Override
	public ResponseEntity<Object> getTestbasedParameter(String ntransactiontestcode, UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		ObjectMapper objMapper = new ObjectMapper();

		String str = "select rc.jsondata ->> 'senforceresultcomment'  "
				+ "       AS senforceresultcomment,"
				//+ " rcl.jsondata,"
				+ " case when urs.nusercode =-1 then '-' else urs.sfirstname||' '||urs.slastname end as senteredby,pt.sparametertypename,"
				+ " case when urs.nusercode =-1 then '-' else ur.suserrolename end as suserrolename,rc.jsondata->>'sresultcomment' as sresultcomment,tgtp.sparametersynonym,"
				+ " TO_CHAR((rp.jsondata->>'dentereddate')::timestamp,'" + userInfo.getSpgsitedatetime()
				+ "') as  sentereddate,rp.npreregno, ra.sarno,rsa.ssamplearno,rp.jsondata->>'smaxa' smaxa,rp.jsondata->>'smaxb' smaxb,rp.jsondata->>'smina' smina,rp.jsondata->>'sminb' sminb,"
				+ " rt.jsondata->>'stestsynonym' as stestsynonym,"
				+ " case when (coalesce(rp.jsondata->>'sresult','')='') then '' else rp.jsondata->>'sresult'||' '||case when rp.nunitcode=-1 then  '' else case when rp.nparametertypecode="+
				Enumeration.ParameterType.CHARACTER.getparametertype()+" then '' else u.sunitname end end end as sresult,"
				+ " case when (coalesce(rp.jsondata->>'sfinal','')='') then '' else rp.jsondata->>'sfinal'||' '||case when rp.nunitcode=-1 then '' else case when rp.nparametertypecode="+
				Enumeration.ParameterType.CHARACTER.getparametertype()+ " then '' else u.sunitname end end end as sfinal,"
				+ " case when g.sgradename = 'NA' then '-' else coalesce(g.jsondata->'sdisplayname'->>'"+userInfo.getSlanguagetypecode()+"',"
				+ " g.jsondata->'sdisplayname'->>'en-US')end as sgradename,"
				+ " g.ngradecode,  rt.ntestrepeatno,rt.ntestretestno, rp.ntransactionresultcode,rp.ntransactiontestcode,"
				+ " tgtp.nchecklistversioncode,rp.nparametertypecode,u.sunitname ,"
				+ " case cl.nchecklistcode when -1 then '-' else cl.schecklistname end schecklistname ,cm.scolorhexcode,rp.nattachmenttypecode,"
				+ " rp.jsondata->>'ssystemfilename' as ssystemfilename,coalesce(tgtp.sspecdesc,'-') sspecdesc,"
				+ " rp.jsondata,ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "' as senforcestatus,rt.ntransactionsamplecode,rp.jsondata ->> 'sresultaccuracyname' as sresultaccuracyname,tgtp.ntestgrouptestparametercode,tgt.nspecsampletypecode, "
				+ " tf.nispredefinedformula,tf.npredefinedformulacode "
				//+ " case  when rp.jsondata ->> 'sunitname'  isnull  then 'NA'  else  rp.jsondata ->> 'sunitname' end AS sunitname, "
				//+ " case  when rp.jsondata ->> 'sresultaccuracyname'  isnull  then ''  else  rp.jsondata ->> 'sresultaccuracyname' end AS sresultaccuracyname "
				+ " from resultparameter rp,registrationsamplearno rsa,registrationarno ra,registrationtest rt,"
				+ " testgrouptest tgt, testgrouptestparameter tgtp "
				+ " left outer join testgrouptestformula tgf on tgf.ntestgrouptestcode=tgtp.ntestgrouptestcode and tgf.nstatus="
				+  Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and tgf.ntransactionstatus  = " + Enumeration.TransactionStatus.YES.gettransactionstatus() + " "
				+ " and tgtp.ntestgrouptestparametercode=tgf.ntestgrouptestparametercode "
				+ " left outer join testformula tf on tf.ntestformulacode=tgf.ntestformulacode "
				+ " and tf.ntestformulacode=tgf.ntestformulacode and tgtp.ntestgrouptestparametercode=tgf.ntestgrouptestparametercode "
				+ " and tf.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and tgtp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and tgf.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " , "
				+ " parametertype pt,grade g,unit u,checklist cl,checklistversion clv,"
				+ " colormaster cm,transactionstatus ts,users urs,resultparametercomments rc,userrole ur "
				//+ " , resultchecklist rcl "
				+ " where rp.ntransactiontestcode = rt.ntransactiontestcode and rp.npreregno = rt.npreregno" + 
				"  and ra.npreregno = rt.npreregno" + 
				"  and rsa.ntransactionsamplecode = rt.ntransactionsamplecode" + 
				"  and rt.ntestgrouptestcode = tgt.ntestgrouptestcode" + 
				"  and tgt.ntestgrouptestcode = tgtp.ntestgrouptestcode" + 
				"  and rp.ntestgrouptestparametercode = tgtp.ntestgrouptestparametercode" + 
				"  and rp.ntransactionresultcode = rc.ntransactionresultcode" + 
				"  and rp.nenteredrole  = ur.nuserrolecode" + 
				"  and rp.nenteredby = urs.nusercode" + 
				"  and rp.nparametertypecode  = pt.nparametertypecode " + 
				"  and rp.nenforcestatus  = ts.ntranscode" + 
				"  and rp.ngradecode = g.ngradecode " + 
				"  and rp.nunitcode = u.nunitcode" + 
				"  and tgtp.nchecklistversioncode  = clv.nchecklistversioncode" + 
				"  and g.ncolorcode = cm.ncolorcode " + 
				"  and cl.nchecklistcode = clv.nchecklistcode "
				+ " and rp.ntransactionstatus not in (" + Enumeration.TransactionStatus.REJECTED.gettransactionstatus()
				+ ", " + Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ")"
				+ " and clv.nchecklistversioncode = tgtp.nchecklistversioncode and rp.ntransactiontestcode in ("
				+ ntransactiontestcode + ") and ra.nsitecode = "+userInfo.getNtranssitecode()+" "
				+ " and rp.nsitecode = rt.nsitecode " + 
				"  and rt.nsitecode = rsa.nsitecode " + 
				"  and ra.nsitecode = rsa.nsitecode " + 
				"  and rp.nstatus = rt.nstatus " + 
				"  and rt.nstatus = ur.nstatus " + 
				"  and ur.nstatus = urs.nstatus " + 
				"  and pt.nstatus = urs.nstatus" + 
				"  and u.nstatus = pt.nstatus " + 
				//				"  and u.nstatus = g.nstatus  " + 
				"  and u.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";


		final List<ResultParameter> params =jdbcTemplate.query(str,new ResultParameter());

		final List<?> lstUTCConvertedDate = dateUtilityFunction.getSiteLocalTimeFromUTC(params, Arrays.asList("sentereddate"),
				null, userInfo, false, Arrays.asList("senforcestatus"), false);
		final List<ResultParameter> parameterList = objMapper.convertValue(lstUTCConvertedDate,	new TypeReference<List<ResultParameter>>() {});
		if (!parameterList.isEmpty()) {
			returnMap.put("SelectedTestParameters", parameterList.get(0));
		}

		returnMap.put("TestParameters", parameterList);
		// Added for use in Sample Registration - to get parameter list based in
		// searched test
		// - Subashini
		returnMap.put("RegistrationParameter", parameterList);

		//Comment by vignesh R
		//returnMap.putAll(getSDMSLabMaster(ntransactiontestcode));

		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@SuppressWarnings({})
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

	@SuppressWarnings("unchecked")
	private Map<String, Object> getActiveTestTabData(String ntransactionTestCode, String activeTabName,
			UserInfo userInfo) throws Exception {
		switch (activeTabName) {
		case "IDS_RESULTS":
			return (Map<String, Object>) getTestbasedParameter(ntransactionTestCode, userInfo).getBody();
		case "IDS_INSTRUMENT":
			return (Map<String, Object>) getResultUsedInstrument(ntransactionTestCode, 0, userInfo).getBody();
		case "IDS_MATERIAL":
			//return Collections.emptyMap();
			return (Map<String, Object>) getResultUsedMaterial(ntransactionTestCode, 0, userInfo).getBody();
		case "IDS_TASK":
			return (Map<String, Object>) getResultUsedTask(ntransactionTestCode, 0,userInfo).getBody();
		case "IDS_TESTATTACHMENTS":
			return (Map<String, Object>) attachmentDAO.getTestAttachment(ntransactionTestCode, userInfo).getBody();
		case "IDS_TESTCOMMENTS":
			return (Map<String, Object>) commentDAO.getTestComment(ntransactionTestCode, userInfo).getBody();
			//		case "IDS_DOCUMENTS":
			//			return null;
		case "IDS_RESULTCHANGEHISTORY":
			return (Map<String, Object>) getResultChangeHistory(ntransactionTestCode, userInfo).getBody();
		default:
			return (Map<String, Object>) getTestbasedParameter(ntransactionTestCode, userInfo).getBody();
		}
	}

	@Override
	public ResponseEntity<Object> getResultEntryResults(String ntransactiontestcode, UserInfo userInfo,Map<String, Object> inputMap)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		int nregsubtypecode=(int) inputMap.get("nregsubtypecode");
		int nregtypecode=(int) inputMap.get("nregtypecode");
		String[] lsttransactiontestcode = ntransactiontestcode.split(",");
		ObjectMapper objMapper = new ObjectMapper();

		List<RegistrationTestHistory> filteredTestList = validateTestStatus(ntransactiontestcode, (int) inputMap.get("ncontrolcode"), userInfo,(int) inputMap.get("nneedReceivedInLab"));
		ntransactiontestcode = filteredTestList.stream().map(x -> String.valueOf(x.getNtransactiontestcode())).collect(Collectors.joining(","));

		if(lsttransactiontestcode.length == filteredTestList.size())
		{
			if (!filteredTestList.isEmpty()) {
				// ALPD-5781	Modified query by Vishakh for alert issue when entering result value as - or .
				String getParams = "select row_num,a.jsondata->>'additionalInfo' as  \"additionalInfo\",a.jsondata->>'additionalInfoUidata' as  \"additionalInfoUidata\",(a.jsondata->>'ntestgrouptestpredefcode')::int as ntestgrouptestpredefcode,a.npreregno,"
						+ " a.jsondata||a.jsonuidata as jsondata, "
						+ " a.sarno AS sarno,a.ssamplearno AS ssamplearno,"
						+ "a.stestsynonym AS"
						+ " stestsynonym,a.sparametersynonym, "
						+ "'['||cast(a.ntestrepeatno as varchar(3))||']['||cast(a.ntestretestno as varchar(3))||']' AS sretestrepeatcount,"
						+ "a.nparametertypecode,a.sparametertypename,a.nispredefinedformula,a.npredefinedformulacode,"
						+ "a.ntestrepeatno,a.ntestretestno,a.ntransactionsamplecode,a.ntransactionresultcode,a.ntransactiontestcode,a.nchecklistversioncode, "
						+ "a.ntransactionstatus,a.nroundingdigits,a.smina,a.sminb,a.smaxa,a.smaxb,a.ngradecode, "
						+ "a.ntestparametercode,a.sminlod,a.smaxlod,a.sminloq,a.smaxloq,a.sdisregard,a.sresultvalue"
						+ ",case when a.nparametertypecode="+Enumeration.ParameterType.PREDEFINED.getparametertype()+" then case when a.sresult<>''  then  a.sresult || ' (' || a.sfinal || ')' else a.sresult end "
						+ " else a.sresult end as sresultpredefinedname,a.sresult ,a.sfinal,a.ncalculatedresult,a.nfilesize,case a.sgradename when 'NA' then '' else a.sgradename end as sgradename,a.ntestgrouptestformulacode, "
						+ "a.ntestgrouptestparametercode,a.nsorter,a.ntestgrouptestcode,a.nresultmandatory,a.stransdisplaystatus as sresultmandatory ,a.nresultaccuracycode,a.sresultaccuracyname,a.nunitcode,a.sunitname "
						+ " from  " + "("
						+ "select ROW_NUMBER() OVER (PARTITION BY rt.ntransactiontestcode,ra.sarno,rsa.ssamplearno ORDER BY rt.npreregno, tgtp.nsorter, rp.ntransactionresultcode,rt.ntransactiontestcode desc) row_num, "
						+ "rp.jsondata,r.jsonuidata, rp.npreregno,ra.sarno,rsa.ssamplearno,rp.jsondata->>'stestsynonym' as stestsynonym,tgtp.sparametersynonym,rp.nparametertypecode,pt.sparametertypename,"
						+ "coalesce(g.jsondata->'sdisplayname'->>'"+userInfo.getSlanguagetypecode()+"',g.jsondata->'sdisplayname'->>'en-US') sgradename,"
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
						+ "rp.ngradecode,tgf.ntestgrouptestformulacode, tf.nispredefinedformula,tf.npredefinedformulacode, "
						+ "rp.ntestgrouptestparametercode,tgtp.nsorter,tgtp.ntestgrouptestcode,rp.nresultmandatory,"
						+ "ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
						+ "' as stransdisplaystatus ,"
						+ "(rp.jsondata->>'nresultaccuracycode')::integer as nresultaccuracycode,"
						+ "(rp.jsondata->>'sresultaccuracyname')::character varying(10) as sresultaccuracyname, "
						+ " rp.nunitcode,u.sunitname "
						+ "from resultparameter rp,registration r,registrationsamplearno rsa,registrationarno ra,registrationtest rt,testgrouptestparameter tgtp "
						+ "left outer join testgrouptestnumericparameter tgnp on tgtp.ntestgrouptestparametercode = tgnp.ntestgrouptestparametercode and tgnp.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "left outer join testgrouptestformula tgf on tgf.ntestgrouptestcode=tgtp.ntestgrouptestcode and tgf.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "and tgf.ntransactionstatus  = " + Enumeration.TransactionStatus.YES.gettransactionstatus() + " "
						+ "and tgtp.ntestgrouptestparametercode=tgf.ntestgrouptestparametercode "
						+ "left outer join testformula tf on tf.ntestformulacode=tgf.ntestformulacode "
						+ " and tf.ntestformulacode=tgf.ntestformulacode and tgtp.ntestgrouptestparametercode=tgf.ntestgrouptestparametercode "
						+ " and tf.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						+ " and tgtp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and tgf.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " , "
						+ "parametertype pt,grade g,transactionstatus ts,unit u  "
						+ " where pt.nparametertypecode = tgtp.nparametertypecode "
						+ " and ts.ntranscode = rp.nresultmandatory "
						+ " and tgtp.ntestgrouptestparametercode = rp.ntestgrouptestparametercode and rsa.ntransactionsamplecode = rt.ntransactionsamplecode "
						+ " and r.npreregno = rt.npreregno and r.npreregno = ra.npreregno and ra.npreregno = rt.npreregno  "
						+ " and rp.ntransactiontestcode = rt.ntransactiontestcode "
						+ " and rp.ngradecode = g.ngradecode and rp.nunitcode =u.nunitcode and rp.ntransactionstatus not in("
						+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","
						+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ") "
						+ " and rp.ntransactiontestcode in (" + ntransactiontestcode + ") "
						+ " and ra.nsitecode = "+userInfo.getNtranssitecode()+" and rsa.nsitecode = ra.nsitecode and rt.nsitecode = rsa.nsitecode "
						+ " and rp.nsitecode = rt.nsitecode and rt.nstatus = rp.nstatus and rt.nstatus = r.nstatus "
						+ " and pt.nstatus = g.nstatus and g.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and u.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " order by rp.ntransactiontestcode desc "
						+ ")a ;";

				final String getPreDefinedParams = " select tgpp.ntestgrouptestpredefcode, tgpp.ntestgrouptestparametercode, tgpp.ngradecode,  "
						+ " tgpp.spredefinedname||' ('||tgpp.spredefinedsynonym||')' as sresultpredefinedname,tgpp.spredefinedname,"
						+ " tgpp.spredefinedsynonym, "
						+ " tgpp.spredefinedcomments, tgpp.salertmessage, "
						+ " tgpp.nneedresultentryalert, tgpp.nneedsubcodedresult, tgpp.ndefaultstatus, tgpp.nsitecode, "
						+ " tgpp.dmodifieddate, tgpp.nstatus,rp.ntransactiontestcode,rp.ntestgrouptestparametercode,rp.ntransactionresultcode "
						+ " from resultparameter rp,testgrouptestpredefparameter tgpp"
						+ " where rp.ntestgrouptestparametercode = tgpp.ntestgrouptestparametercode and rp.ntransactiontestcode in ("
						+ ntransactiontestcode + ") " + " and rp.nparametertypecode = "
						+ Enumeration.ParameterType.PREDEFINED.getparametertype() + " and rp.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgpp.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgpp.spredefinedname <> '' ;";

				final String getGrades = "select g.ngradecode,"
						+ "case g.sgradename when 'NA' then '' else"
						+ " coalesce(g.jsondata->'sdisplayname'->>'"+userInfo.getSlanguagetypecode()+"',"
						+ " g.jsondata->'sdisplayname'->>'en-US') end sgradename ,"
						+ " cm.ncolorcode,cm.scolorname,cm.scolorhexcode from grade g,colormaster cm "
						+ " where g.ncolorcode = cm.ncolorcode "
						+ " and g.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and g.nsitecode = "+userInfo.getNmastersitecode()+" "
						+ " and cm.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " union all "
						+ " select 0 ngradecode,'' sgradename, 0 ncolorcode, '' scolorname,'' scolorhexcode;";

				returnMap.put("ResultParameter", jdbcTemplate.queryForList(getParams));

				List<TestGroupTestPredefinedParameter> lst = jdbcTemplate.query(getPreDefinedParams,new TestGroupTestPredefinedParameter());

				List<Grade> lstGrade = objMapper.convertValue(
						commonFunction.getMultilingualMessageList(jdbcTemplate.query(getGrades, new Grade()),
								Arrays.asList("sgradename"), userInfo.getSlanguagefilename()),
						new TypeReference<List<Grade>>() {
						});

				returnMap.put("PredefinedValues", lst.stream().collect(Collectors
						.groupingBy(TestGroupTestPredefinedParameter::getNtransactionresultcode, Collectors.toList())));

				returnMap.put("GradeValues",
						lstGrade.stream().collect(Collectors.groupingBy(Grade::getNgradecode, Collectors.toList())));

				final String strQuery="select * from resultaccuracy where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  and nsitecode ="+userInfo.getNmastersitecode() +" ";
				returnMap.put("ResultAccuracy", jdbcTemplate.queryForList(strQuery));

				final String strUnitQuery="select * from unit where nunitcode >0 and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  and nsitecode ="+userInfo.getNmastersitecode() +" ";
				returnMap.put("Unit", jdbcTemplate.queryForList(strUnitQuery));

				final String strFormFieldQuery="select jsondata from resultentryformfield where  nregsubtypecode="+nregsubtypecode+" and nregtypecode="+nregtypecode+" and "
						+ " nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  and nsitecode ="+userInfo.getNmastersitecode() +" ";
				returnMap.put("FormFields", jdbcTemplate.queryForList(strFormFieldQuery));


				return new ResponseEntity<>(returnMap, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SELECTVALIDTEST", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		}
		else
		{

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTVALIDTEST", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}

	@Override
	public ResponseEntity<Object> getPredefinedData(Map<String,Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<>(); 
		String query = "select * from testgrouptestpredefsubresult where ntestgrouptestpredefcode="+inputMap.get("ntestgrouptestpredefcode")
		+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode = "+userInfo.getNmastersitecode()+";"; 

		returnMap.put("testgrouptestpredefsubresult", jdbcTemplate.query(query,new TestGroupTestPredefinedSubCode()));

		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}


	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> updatePredefinedComments(Map<String,Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<>(); 
		String updateQuery = "update resultparameter set jsondata=jsondata||jsonb_build_object('spredefinedcomments','"+inputMap.get("spredefinedcomments")+"')"
				+" where ntransactionresultcode="+inputMap.get("ntransactionresultcode")
				+" and nsitecode  = "+userInfo.getNtranssitecode()+" "
				+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";  
		jdbcTemplate.execute(updateQuery); 

		returnMap.putAll((Map<String, Object>) getTestbasedParameter(inputMap.get("ntransactiontestcode").toString(),userInfo).getBody());
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}
	public List<RegistrationTestHistory> validateTestStatus(String ntransactiontestcode, int controlCode,
			UserInfo userInfo,int nneedReceivedInLab) throws Exception {

		// Updated below query by L.Subashini on 29/04/2022 to add regtype and
		// regsubtype in transactionvalidation
		String query="";
		if(nneedReceivedInLab==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
			query = " union all select 19 ";
		}

		String validateTests = "select rth.ntesthistorycode,rth.ntransactionstatus,rth.ntransactiontestcode,rth.npreregno from registrationtesthistory rth where rth.ntesthistorycode = any "
				+ " (select max(ntesthistorycode) from registrationtesthistory where ntransactiontestcode in ("
				+ ntransactiontestcode + ") and nsitecode = "+userInfo.getNtranssitecode()+" and nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " group by ntransactiontestcode) and rth.ntransactionstatus = any ("
				+ " select  ntransactionstatus from transactionvalidation tv , registration r "
				+ " where tv.nformcode = " + userInfo.getNformcode() + " and tv.nsitecode = "
				+ userInfo.getNmastersitecode() + " and tv.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tv.ncontrolcode = " + controlCode +" and r.nsitecode = "+userInfo.getNtranssitecode()+""
				+ " and tv.nregtypecode=r.nregtypecode and tv.nregsubtypecode=r.nregsubtypecode and r.npreregno=rth.npreregno "
				+ query +" ) and rth.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		return jdbcTemplate.query(validateTests, new RegistrationTestHistory());
	}

	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> updateTestParameterResult(MultipartHttpServletRequest request,final UserInfo userInfo) throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		ObjectMapper objMapper = new ObjectMapper();

		final String lockquery = "lock lockresultupdate" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(lockquery);

		JSONObject jsonAuditOldData = new JSONObject();
		JSONObject jsonAuditNewData = new JSONObject();
		JSONObject auditActionType = new JSONObject();
		String result = request.getParameter("resultData");
		int fileparamcount = Integer.parseInt(request.getParameter("filecount"));
		List<ResultParameter> lstTransSampResults = objMapper.readValue(result,new TypeReference<List<ResultParameter>>() {});

		String transactiontestcodes = request.getParameter("transactiontestcode");

		int nregtypecode = Integer.parseInt(request.getParameter("nregtypecode"));
		int nregsubtypecode = Integer.parseInt(request.getParameter("nregsubtypecode"));
		int ndesigntemplatemappingcode = Integer.parseInt(request.getParameter("ndesigntemplatemappingcode"));
		String stransactiontestcode = lstTransSampResults.stream().map(object -> String.valueOf(object.getNtransactiontestcode())).collect(Collectors.joining(","));

		//	ALPD-5583	Removed .filter(data->data.getSresult()!="" ) by Vishakh due to 500 issue
		String stransactionAuditTestCode = lstTransSampResults.stream()
				.map(item -> item.getNtransactionresultcode()+"").collect(Collectors.joining(","));

		List<RegistrationTestHistory> filteredTestList = validateTestStatus(stransactiontestcode,
				Integer.parseInt(request.getParameter("ncontrolcode")), userInfo,Integer.parseInt(request.getParameter("nneedReceivedInLab")));

		String query = "";

		if (!filteredTestList.isEmpty()) {
			if (fileparamcount > 0) {
				String sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, userInfo);
			}

			lstTransSampResults = lstTransSampResults.stream()
					.filter(sampleResult -> filteredTestList.stream().anyMatch(filtertest -> filtertest
							.getNtransactiontestcode() == sampleResult.getNtransactiontestcode()))
					.collect(Collectors.toList());
//			String stransactionresultcode = lstTransSampResults.stream()
//					.map(object -> String.valueOf(object.getNtransactionresultcode())).collect(Collectors.joining(","));

			if (lstTransSampResults != null && !lstTransSampResults.isEmpty()) {

				query = " select rp.ntransactionresultcode, rp.ntransactiontestcode, rp.npreregno, rp.ntestgrouptestparametercode, "
						+ "rp.ntestparametercode, rp.ntestgrouptestformulacode, rp.nparametertypecode, rp.ncalculatedresult, rp.ngradecode, "
						+ "rp.nreportmandatory, rp.nresultmandatory, rp.nenforceresult, rp.nenforcestatus, rp.nlinkcode, rp.nattachmenttypecode, "
						+ "rp.ntransactionstatus, rp.nunitcode, rp.nenteredby, rp.nenteredrole, rp.ndeputyenteredby, rp.ndeputyenteredrole, "
						+ "case when rp.jsondata ? 'dentereddate' then jsonb_set(rp.jsondata, '{dentereddate}', to_jsonb(replace(replace(rp.jsondata->>'dentereddate', 'T', ' '), 'Z', ''))) else rp.jsondata end AS jsondata,"
						+ "rp.nsitecode, rp.nstatus, rp.jsondata->>'sfinal' sfinal , rp.jsondata->>'sresult' sresult,"
						+ " rp.jsondata->>'nresultaccuracycode' nresultaccuracycode ,rp.jsondata->>'sresultaccuracyname' sresultaccuracyname ," 
						+ " CASE WHEN rp.jsondata->>'dentereddate' IS NULL THEN rp.jsondata->>'dentereddate' ELSE replace(replace(rp.jsondata->>'dentereddate', 'T', ' '), 'Z', '') END AS dentereddate,"
						+ "rp.jsondata->>'dentereddatetimezonecode' dentereddatetimezonecode,"
						+ " rp.jsondata->>'noffsetdentereddate' noffsetdentereddate,"
						+ " t.jsondata->'stransdisplaystatus'->>'"+ userInfo.getSlanguagetypecode() + "' as stransdisplaystatus,"
						+ " rp.jsondata->>'stestsynonym' stestsynonym,"
						+ " rp.jsondata->>'sparametersynonym' sparametersynonym,"
						+ " r.sarno,rs.ssamplearno,g.sgradename,u.sunitname"
						+ " from resultparameter rp,transactionstatus t,registrationarno r,registrationsamplearno rs,"
						+ " registrationtest rt,grade g,unit u"
						+ " where t.ntranscode=rp.ntransactionstatus and r.npreregno=rp.npreregno and r.npreregno=rs.npreregno"
						+ " and rs.npreregno=rp.npreregno and rt.ntransactionsamplecode=rs.ntransactionsamplecode"
						+ " and rt.ntransactiontestcode=rp.ntransactiontestcode and u.nunitcode=rp.nunitcode "
						+ " and rt.npreregno=rs.npreregno and r.npreregno=rt.npreregno and g.ngradecode=rp.ngradecode "
						+ " and rs.nsitecode ="+ userInfo.getNtranssitecode() +" "
						+ " and r.nsitecode = "+ userInfo.getNtranssitecode() +" "
						+ " and rp.nsitecode ="+ userInfo.getNtranssitecode() +" "
						+ " and rt.nsitecode ="+ userInfo.getNtranssitecode() +" "
						+ " and rp.ntransactionresultcode in (" + stransactionAuditTestCode + ")";

				List<ResultParameter> lstSampleResults = jdbcTemplate.query(query, new ResultParameter());

				query = " select nsequenceno from seqnoregistration where stablename=N'resultchangehistory' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
				int nresultchangehistoryno = jdbcTemplate.queryForObject(query, Integer.class);
				StringBuilder sresultcode = new StringBuilder();
				StringBuilder schangeresult = new StringBuilder();
				final String currentDateTime = dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ").replace("Z","");
				String updateQuery="";
				String insertResultChange="";
				String parameterCommentsQuery="";
//				String controlListquery = " select ncontrolcode, scontrolname from controlmaster where nformcode="+ Enumeration.QualisForms.RESULTENTRY.getqualisforms()
//				+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

//				final List<ControlMaster> controlList = jdbcTemplate.query(controlListquery, new ControlMaster());

//				final Map<String, Short> controlMap = controlList.stream().collect(Collectors.toMap(
//						ControlMaster::getScontrolname,control -> control.getNcontrolcode()));	

				List<ResultParameter> lstResulCodePredefined = lstTransSampResults
						.stream().filter(resultType -> (resultType.getNparametertypecode()==Enumeration.ParameterType.PREDEFINED.getparametertype())
								&& (resultType.getJsondata().containsKey("sresultcomment"))).collect(Collectors.toList());

				String stransactionresultcodeforcomments = lstResulCodePredefined.stream().map(object -> String.valueOf(object.getNtransactionresultcode()))
						.collect(Collectors.joining(","));

				if (lstSampleResults != null && !lstSampleResults.isEmpty()) {
					for (ResultParameter objResult : lstTransSampResults) {
						Map<String, Object> jsonData = objResult.getJsondata();
						String sfinal=StringEscapeUtils.unescapeJava(jsonData.get("sfinal").toString());
						String sresult=StringEscapeUtils.unescapeJava(jsonData.get("sresult").toString());

						jsonData.put("sfinal", sfinal);
						jsonData.put("sresult", sresult);
						jsonData.put("dentereddate", currentDateTime);
						jsonData.put("dentereddatetimezonecode", userInfo.getNtimezonecode());
						jsonData.put("noffsetdentereddate",dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()));

						updateQuery = updateQuery + "update resultparameter set " + " ngradecode=" + objResult.getNgradecode()
						+ "," +  "nunitcode="+objResult.getNunitcode()+", " +  " nenteredby= " + objResult.getNenteredby() + "," + " nenteredrole="
						+ objResult.getNenteredrole() + "," + " ndeputyenteredby="
						+ userInfo.getNdeputyusercode() + "," + " ndeputyenteredrole="
						+ userInfo.getNdeputyuserrole() + "," + " nattachmenttypecode = "
						+ (objResult.getNparametertypecode() == 4 ? 1 : -1) + " ,"
						+ " jsondata = jsondata || '" + stringUtilityFunction.replaceQuote(objMapper.writeValueAsString(jsonData)) + "'"
						+ " where ntransactionresultcode = " + objResult.getNtransactionresultcode() + ";";

						List<ResultParameter> lstSingleResults = lstSampleResults
								.stream().filter(objjqresultparam -> objjqresultparam.getNtransactionresultcode() == objResult.getNtransactionresultcode())
								.collect(Collectors.toList());

						if (!lstSingleResults.isEmpty() && lstSingleResults.get(0).getSresult() != null
								&& !(lstSingleResults.get(0).getSresult().toString()
										.equalsIgnoreCase((String) jsonData.get("sresult")))) {

							insertResultChange = insertResultChange + "insert into resultchangehistory (nresultchangehistorycode,nformcode, ntransactionresultcode,ntransactiontestcode,npreregno,"
									+ "ngradecode,ntestgrouptestparametercode,nparametertypecode,nenforceresult,nenforcestatus,"
									+ "nenteredby,nenteredrole,ndeputyenteredby,ndeputyenteredrole,jsondata,nsitecode,nstatus) "
									+ "select " + nresultchangehistoryno
									+ "+RANK()over(order by ntransactionresultcode) nresultchangehistorycode,"
									+ userInfo.getNformcode()
									+ ",ntransactionresultcode,ntransactiontestcode,npreregno,"
									+ "ngradecode,ntestgrouptestparametercode,"										
									+ lstSingleResults.get(0).getNparametertypecode() + " nparametertypecode," + ""
									+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " nenforceresult,"
									+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " nenforcestatus,"
									+ userInfo.getNusercode() + " nenteredby," + userInfo.getNuserrole()
									+ " nenteredrole," + "" + userInfo.getNdeputyusercode() + " ndeputyenteredby,"
									+ userInfo.getNdeputyuserrole() + " ndeputyenteredrole,'{" + "     \"sresult\":"
									+ stringUtilityFunction.replaceQuote(objMapper.writeValueAsString(
											lstSingleResults.get(0).getJsondata().get("sresult")))
									+ "," + "     \"sfinal\":"
									+ stringUtilityFunction.replaceQuote(objMapper
											.writeValueAsString(lstSingleResults.get(0).getJsondata().get("sfinal")))
									+ "," + "    \"nresultaccuracycode\":"
									+ stringUtilityFunction.replaceQuote(objMapper
											.writeValueAsString(lstSingleResults.get(0).getJsondata().get("nresultaccuracycode")))
									+ "," + "    \"sresultaccuracyname\":"
									+ stringUtilityFunction.replaceQuote(objMapper
											.writeValueAsString(lstSingleResults.get(0).getJsondata().get("sresultaccuracyname")))
									+ "," + "    \"nunitcode\":"
									+ stringUtilityFunction.replaceQuote(objMapper
											.writeValueAsString(lstSingleResults.get(0).getJsondata().get("nunitcode")))
									+ "," + "    \"sunitname\":"
									+ stringUtilityFunction.replaceQuote(objMapper
											.writeValueAsString(lstSingleResults.get(0).getJsondata().get("sunitname")))
									+ "," 
									+ "     \"dentereddate\":\""
									+ lstSingleResults.get(0).getDentereddate().toString() + "\","
									+ "     \"dentereddatetimezonecode\":\""
									+ lstSingleResults.get(0).getDentereddatetimezonecode() + "\","
									+ "     \"noffsetdentereddate\":\""
									+ lstSingleResults.get(0).getNoffsetdentereddate() + "\""
									+ " }'::jsonb,"+userInfo.getNtranssitecode()+","+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " nstatus from resultparameter where nsitecode = "+userInfo.getNtranssitecode()+" "
									+ " and  nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and ntransactionresultcode = " + objResult.getNtransactionresultcode() + ";";

							nresultchangehistoryno++;
							sresultcode.append("," + objResult.getNtransactionresultcode());
							schangeresult.append("," + nresultchangehistoryno);
						}

						if(objResult.getNparametertypecode()==Enumeration.ParameterType.PREDEFINED.getparametertype()){
							if((jsonData.containsKey("sresultcomment")) 
									) {
								//ALPD-5931--Added by Vignesh R(03-06-2025)--unicode showing the comments in result tab. 
								final String sresultcomment=StringEscapeUtils.unescapeJava(jsonData.get("sresultcomment").toString());
								
								parameterCommentsQuery = parameterCommentsQuery + "update resultparametercomments set jsondata = jsondata || jsonb_build_object('sresultcomment','"+stringUtilityFunction.replaceQuote(sresultcomment) +"')::jsonb where ntransactionresultcode = "
										+ objResult.getNtransactionresultcode() + " and nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

							}
						}
					}
					jdbcTemplate.execute(updateQuery);
					jdbcTemplate.execute(insertResultChange);

					if(!lstResulCodePredefined.isEmpty()) {
						String strAuditComment = " select pt.sparametertypename,ra.sarno,rsa.ssamplearno,"
								+ " rc.jsondata->>'sresultcomment' as sresultcomment,rp.jsondata->>'stestsynonym' stestsynonym,"
								+ " rp.jsondata->>'sparametersynonym' sparametersynonym "
								+ " from resultparametercomments rc,resultparameter rp ,parametertype pt,"
								+ " registrationsamplearno rsa,registrationarno ra "
								+ " where rp.ntransactionresultcode = rc.ntransactionresultcode"
								+ " and rp.npreregno = rc.npreregno "
								+ " and ra.npreregno = rp.npreregno "
								+ " and ra.npreregno = rsa.npreregno "
								+ " and rp.nparametertypecode  = pt.nparametertypecode "
								+ " and rc.nsitecode = "+userInfo.getNtranssitecode()+" "
								+ " and rp.nsitecode =  "+userInfo.getNtranssitecode()+""
								+ " and ra.nsitecode =  "+userInfo.getNtranssitecode()+" "
								+ " and rsa.nsitecode = "+userInfo.getNtranssitecode()+" "
								+ " and pt.nsitecode = "+userInfo.getNmastersitecode()+""
								+ " and rc.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
								+ " and rp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
								+ " and rsa.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
								+ " and ra.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
								+ " and pt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
								+ " and rc.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
								+ " and rp.ntransactionresultcode in (" + stransactionresultcodeforcomments + ")";

						List<ResultParameter> lstCommentsdatabefore = jdbcTemplate.query(strAuditComment, new ResultParameter());

						jdbcTemplate.execute(parameterCommentsQuery);


						String strCommentsafter = " select pt.sparametertypename,ra.sarno,rsa.ssamplearno,"
								+ " rc.jsondata->>'sresultcomment' as sresultcomment,rp.jsondata->>'stestsynonym' stestsynonym,"
								+ " rp.jsondata->>'sparametersynonym' sparametersynonym "
								+ " from resultparametercomments rc,resultparameter rp ,parametertype pt,"
								+ " registrationsamplearno rsa,registrationarno ra "
								+ " where rp.ntransactionresultcode = rc.ntransactionresultcode"
								+ " and rp.npreregno = rc.npreregno "
								+ " and ra.npreregno = rp.npreregno "
								+ " and ra.npreregno = rsa.npreregno "
								+ " and rp.nparametertypecode  = pt.nparametertypecode "
								+ " and rc.nsitecode = "+userInfo.getNtranssitecode()+" "
								+ " and rp.nsitecode =  "+userInfo.getNtranssitecode()+""
								+ " and ra.nsitecode =  "+userInfo.getNtranssitecode()+" "
								+ " and rsa.nsitecode = "+userInfo.getNtranssitecode()+" "
								+ " and pt.nsitecode = "+userInfo.getNmastersitecode()+""
								+ " and rc.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
								+ " and rp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
								+ " and rsa.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
								+ " and ra.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
								+ " and pt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
								+ " and rc.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
								+ " and rp.ntransactionresultcode in (" + stransactionresultcodeforcomments + ")";

						List<ResultParameter> lstCommentsdataafter = jdbcTemplate.query(strCommentsafter, new ResultParameter());

						jsonAuditOldData.put("resultparametercomments", lstCommentsdatabefore);
						jsonAuditNewData.put("resultparametercomments", lstCommentsdataafter);	
						auditActionType.put("resultparametercomments", "IDS_EDITRESULTPARAMETERCOMMENTS");

					}

					jdbcTemplate.execute("update seqnoregistration  set nsequenceno=" + (nresultchangehistoryno)+ " where stablename=N'resultchangehistory';");


					query = " select rp.*,rp.jsondata->>'sfinal' sfinal ,"
							+ "rp.jsondata->>'nresultaccuracycode' nresultaccuracycode ,rp.jsondata->>'sresultaccuracyname' sresultaccuracyname ," 
							+ " t.jsondata->'stransdisplaystatus'->>'"
							+ userInfo.getSlanguagetypecode() + "' as stransdisplaystatus,"
							+ " rp.jsondata->>'stestsynonym' stestsynonym,"
							+ " rp.jsondata->>'sparametersynonym' sparametersynonym,"
							+ " r.sarno,rs.ssamplearno,g.sgradename,u.sunitname"
							+ " from resultparameter rp,transactionstatus t,registrationarno r,registrationsamplearno rs, registrationtest rt,grade g,unit u "
							+ " where t.ntranscode=rp.ntransactionstatus and r.npreregno=rp.npreregno and r.npreregno=rs.npreregno"
							+ " and rs.npreregno=rp.npreregno and rt.ntransactionsamplecode=rs.ntransactionsamplecode"
							+ " and rt.ntransactiontestcode=rp.ntransactiontestcode and u.nunitcode=rp.nunitcode  "
							+ " and rt.npreregno=rs.npreregno "
							+ " and r.npreregno=rt.npreregno and g.ngradecode=rp.ngradecode "
							+ " and rs.nsitecode = "+userInfo.getNtranssitecode()+" "
							+ " and r.nsitecode = "+userInfo.getNtranssitecode()+" "
							+ " and rp.nsitecode ="+userInfo.getNtranssitecode()+" "
							+ " and rt.nsitecode = "+userInfo.getNtranssitecode()+" "
							+ " and g.nsitecode = "+userInfo.getNmastersitecode()+" "
							+ " and u.nsitecode = "+userInfo.getNmastersitecode()+" "
							+ " and rs.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
							+ " and r.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
							+ " and rp.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
							+ " and rt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
							+ " and g.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
							+ " and u.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
							+ " and rp.ntransactionresultcode in (" + stransactionAuditTestCode + ")";

					List<ResultParameter> lstSampleResultsafter = jdbcTemplate.query(query, new ResultParameter());

					//List<ResultParameter> lstdataold = new ArrayList<>();
					jsonAuditOldData.put("resultparameter", lstSampleResults);
					jsonAuditNewData.put("resultparameter", lstSampleResultsafter);			
					auditActionType.put("resultparameter", "IDS_RESULTENTER");
					Map<String, Object> objMap = new HashMap<>();
					objMap.put("nregtypecode", nregtypecode);
					objMap.put("nregsubtypecode", nregsubtypecode);

					objMap.put("ndesigntemplatemappingcode", ndesigntemplatemappingcode);
					auditUtilityFunction.fnJsonArrayAudit(jsonAuditOldData, jsonAuditNewData, auditActionType, objMap, false, userInfo);
					returnMap.putAll(
							(Map<String, Object>) getTestbasedParameter(transactiontestcodes, userInfo).getBody());
					returnMap.putAll((Map<String, Object>) getResultChangeHistory(transactiontestcodes, userInfo).getBody());

				}
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTVALIDTEST", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}


	@Override
	public Map<String, Object> seqNoGetforDefaultValue(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		final Map<String, Object> returnMap = new HashMap<>();
		String ntransactiontestcode = (String) inputMap.get("ntransactiontestcode");
		String ntestgrouptestcode = (String) inputMap.get("ntestgrouptestcode");
		String npreregno = (String) inputMap.get("npreregno");
		//String sresultcode = "";
		//String schangeresult = "";

		final String lockquery = "lock lockresultupdate" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(lockquery);


		List<String> lstntransactiontestcode = Arrays
				.asList(((String) inputMap.get("ntransactiontestcode")).split(","));
		returnMap.put("rtn", Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		List<RegistrationTestHistory> lstvalidation = validateTestStatus(ntransactiontestcode,
				(int) inputMap.get("ncontrolcode"), userInfo,(int) inputMap.get("nneedReceivedInLab"));

		if (lstvalidation.isEmpty()) {

			returnMap.put("rtn", "IDS_SELECTVALIDTEST");
			return returnMap;
		}
		if (lstntransactiontestcode.size() != lstvalidation.size()) {
			returnMap.put("rtn", "IDS_SELECTVALIDTEST");
			return returnMap;
		}

		final String queryTest = "select tgtnp.nparametertypecode,coalesce(tgtnp.ngradecode,"+Enumeration.Grade.FIO.getGrade()+") ngradecode,tgtnp.sresultvalue,tgtnp.sfinal,tgtnp.nroundingdigits,"
				+ "tgtnp.ntestgrouptestcode,tgtnp.ntestgrouptestparametercode, "
				+ "case when tgtnp.nparametertypecode = "+Enumeration.ParameterType.NUMERIC.getparametertype()+" then ( "
				+ "case when tgtnp.sminb is null and tgtnp.smina is null and tgtnp.smaxa is null and tgtnp.smaxb is not null "
				+ "then  " + "case when cast(sfinal as float)<=tgtnp.smaxb then "+Enumeration.Grade.PASS.getGrade()+" else "+Enumeration.Grade.OOS.getGrade()+" end "
				+ "when tgtnp.sminb is null and tgtnp.smina is null and tgtnp.smaxa is not null and tgtnp.smaxb is null then "
				+ "case when cast(sfinal as float)<=tgtnp.smaxa then "+Enumeration.Grade.PASS.getGrade()+" else "+Enumeration.Grade.OOS.getGrade()+" end "
				+ "when tgtnp.sminb is null and tgtnp.smina is null and tgtnp.smaxa is not null and tgtnp.smaxb is not null then "
				+ "case when cast(sfinal as float) between tgtnp.smaxa and tgtnp.smaxb then "+Enumeration.Grade.OOS.getGrade()+" when cast(sfinal as float) > tgtnp.smaxb "
				+ "then "+Enumeration.Grade.OOT.getGrade()+" else "+Enumeration.Grade.PASS.getGrade()+" end "

				+" when tgtnp.sminb is null and tgtnp.smina is not null and tgtnp.smaxa is null and tgtnp.smaxb is null "
				+ "then case when cast(sfinal as float)>=tgtnp.smina then "+Enumeration.Grade.PASS.getGrade()+" else "+Enumeration.Grade.OOS.getGrade()+" end "

				+" when tgtnp.sminb is null and tgtnp.smina is not null and tgtnp.smaxa is null and tgtnp.smaxb is not null then "
				+" case when cast(sfinal as float) between tgtnp.smina and tgtnp.smaxb then "+Enumeration.Grade.PASS.getGrade()+" else "+Enumeration.Grade.OOS.getGrade()+" end "

				+" when tgtnp.sminb is null and tgtnp.smina is not null and tgtnp.smaxa is not null and tgtnp.smaxb is null "
				+" then case when cast(sfinal as float) between tgtnp.smina and tgtnp.smaxa then "+Enumeration.Grade.PASS.getGrade()+" else "+Enumeration.Grade.OOS.getGrade()+" end " 
				+" when tgtnp.sminb is null and tgtnp.smina is not null and tgtnp.smaxa is not null and tgtnp.smaxb is not null then "
				+ "case when cast(sfinal as float) between tgtnp.smina and tgtnp.smaxa then "+Enumeration.Grade.PASS.getGrade()+" when cast(sfinal as float) between tgtnp.smaxa and tgtnp.smaxb then "+Enumeration.Grade.OOS.getGrade()+" else "+Enumeration.Grade.OOT.getGrade()+" end "

				+" when tgtnp.sminb is not null and tgtnp.smina is null and tgtnp.smaxa is null and tgtnp.smaxb is null then "
				+" case when cast(sfinal as float) >= tgtnp.sminb then "+Enumeration.Grade.PASS.getGrade()+" else "+Enumeration.Grade.OOS.getGrade()+" end "

				+" when tgtnp.sminb is not null and tgtnp.smina is null and tgtnp.smaxa is null and tgtnp.smaxb is not null then "
				+ "case when cast(cast(sfinal as float) as float) between tgtnp.sminb and tgtnp.smaxb then "+Enumeration.Grade.PASS.getGrade()+" else "+Enumeration.Grade.OOS.getGrade()+" end "


				+" when tgtnp.sminb is not null and tgtnp.smina is null and tgtnp.smaxa is not null and tgtnp.smaxb is null then "
				+ "case when cast(sfinal as float) between tgtnp.sminb and tgtnp.smaxa then "+Enumeration.Grade.PASS.getGrade()+" else "+Enumeration.Grade.OOS.getGrade()+" end "

				+" when tgtnp.sminb is not null and tgtnp.smina is null and tgtnp.smaxa is not null and tgtnp.smaxb is not null then "
				+" case when cast(sfinal as float) between tgtnp.sminb and tgtnp.smaxa then "+Enumeration.Grade.PASS.getGrade()+" else "+Enumeration.Grade.OOS.getGrade()+" end "

				+" when tgtnp.sminb is not null and tgtnp.smina is not null and tgtnp.smaxa is null and tgtnp.smaxb is null then "
				+ " case when cast(sfinal as float) between tgtnp.sminb and tgtnp.smina then "+Enumeration.Grade.OOS.getGrade()+" "
				+ " when cast(sfinal as float) < tgtnp.sminb then "+Enumeration.Grade.OOS.getGrade()+" when cast(sfinal as float) >= tgtnp.smina "
				+ " then "+Enumeration.Grade.PASS.getGrade()+" end "

				+" when tgtnp.sminb is not null and tgtnp.smina is not null and tgtnp.smaxa is null and tgtnp.smaxb is not null "
				+ "then case when cast(sfinal as float) between tgtnp.smina and tgtnp.smaxb then "+Enumeration.Grade.PASS.getGrade()+" when cast(sfinal as float) between tgtnp.sminb and tgtnp.smina "
				+ "then "+Enumeration.Grade.OOS.getGrade()+"  when cast(sfinal as float) > tgtnp.smaxb or cast(sfinal as float) < tgtnp.sminb then "+Enumeration.Grade.OOT.getGrade()+" end "

				+" when tgtnp.sminb is not null and tgtnp.smina is not null and tgtnp.smaxa is not null and tgtnp.smaxb is null  then "
				+ "case when cast(sfinal as float) between tgtnp.smina and tgtnp.smaxa then "+Enumeration.Grade.PASS.getGrade()+" when cast(sfinal as float) between tgtnp.sminb and tgtnp.smina then 3 else "+Enumeration.Grade.OOT.getGrade()+" end "

				+" when tgtnp.sminb is not null and tgtnp.smina is not null and tgtnp.smaxa is not null and tgtnp.smaxb is not null then  "
				+ "case when cast(sfinal as float) between tgtnp.smina and tgtnp.smaxa then "+Enumeration.Grade.PASS.getGrade()+"  "

				+" when cast(sfinal as float) between tgtnp.sminb and tgtnp.smaxb then "+Enumeration.Grade.OOT.getGrade()+" "
				+ "when (cast(sfinal as float) < tgtnp.sminb or tgtnp.smaxb < cast(sfinal as float)) and (tgtnp.sminb!=0 and tgtnp.smaxb!=0)then 3 else "+Enumeration.Grade.PASS.getGrade()+" end else -1 end) "

				+" when nparametertypecode =" + Enumeration.ParameterType.PREDEFINED.getparametertype()
				+ " then tgtnp.ngradecode " + "when nparametertypecode ="
				+ Enumeration.ParameterType.CHARACTER.getparametertype() + " then "+Enumeration.ParameterType.ATTACHEMENT.getparametertype()+" end ngradecodenew " +

				" from ("
				+ " select   cast( case when tgtnp.jsondata->>'sminb'='' then null else tgtnp.jsondata->>'sminb' end  as float) sminb,cast(case when tgtnp.jsondata->>'smina'='' then null else tgtnp.jsondata->>'smina' end as float) smina, cast(case when tgtnp.jsondata->>'smaxb'='' then null else tgtnp.jsondata->>'smaxb' end as float) smaxb, " + 
				" cast(case when tgtnp.jsondata->>'smaxa'='' then null else tgtnp.jsondata->>'smaxa' end as float) smaxa,"
				//+ " cast(tgtnp.sminb as float) sminb,cast(tgtnp.smina as float) smina,cast(tgtnp.smaxb as float) smaxb,cast(tgtnp.smaxa as float) smaxa, "
				+ "tgtp.nparametertypecode,tgtpp.ngradecode, case when tgtp.nparametertypecode="
				+ Enumeration.ParameterType.NUMERIC.getparametertype() + " then  tgtnp.jsondata->>'sresultvalue' "
				+ " when tgtp.nparametertypecode=" + Enumeration.ParameterType.PREDEFINED.getparametertype() + " "
				+ " then tgtpp.spredefinedname else tgtcp.scharname end sresultvalue,"
				+ " case when tgtp.nparametertypecode=" + Enumeration.ParameterType.NUMERIC.getparametertype() + " "
				+ " then cast(round(cast( case when tgtnp.jsondata->>'sresultvalue'='' then null else tgtnp.jsondata->>'sresultvalue' end as decimal),tgtp.nroundingdigits) as character varying(50)) when tgtp.nparametertypecode="
				+ Enumeration.ParameterType.PREDEFINED.getparametertype() + " "
				+ " then tgtpp.spredefinedname else tgtcp.scharname end sfinal,tgtp.nroundingdigits,tgt.ntestgrouptestcode,tgtp.ntestgrouptestparametercode "
				+

				" from   resultparameter tgtnp,testgrouptest tgt inner join testgrouptestparameter tgtp on tgtp.ntestgrouptestcode = tgt.ntestgrouptestcode and tgtp.nstatus =1"
				+ " left outer join testgrouptestcharparameter tgtcp on tgtcp.ntestgrouptestparametercode = tgtp.ntestgrouptestparametercode and tgtcp.nstatus =1"
				//+ "left outer join testgrouptestnumericparameter tgtnp on tgtnp.ntestgrouptestparametercode = tgtp.ntestgrouptestparametercode "
				+ " left outer join testgrouptestpredefparameter tgtpp on tgtpp.ntestgrouptestparametercode=tgtp.ntestgrouptestparametercode and tgtpp.nstatus =1 "
				+ " and tgtpp.ndefaultstatus=" + Enumeration.TransactionStatus.YES.gettransactionstatus()
				+ " where "
				+ " tgtnp.ntestgrouptestparametercode = tgtp.ntestgrouptestparametercode and tgt.ntestgrouptestcode in (select ntestgrouptestcode from registrationtest rt "
				+ " where npreregno in (" + npreregno + ") and ntestgrouptestcode in (" + ntestgrouptestcode
				+ ") and nsitecode = "+userInfo.getNtranssitecode()+") "
				+ "  and tgtnp.npreregno in (" + npreregno + ")  and tgtnp.nsitecode = "+userInfo.getNtranssitecode()+"  "
				+ " and tgtnp.nstatus = tgt.nstatus and tgt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and case when tgtp.nparametertypecode=" + Enumeration.ParameterType.NUMERIC.getparametertype()
				+ " then tgtnp.jsondata->>'sresultvalue' " + "when tgtp.nparametertypecode="
				+ Enumeration.ParameterType.PREDEFINED.getparametertype()
				+ " then tgtpp.spredefinedname else tgtcp.scharname end is not null ) tgtnp ";

		List<ResultParameter> lstGetDefaultValue = jdbcTemplate.query(queryTest, new ResultParameter());

		final String queryResult = "select rp.jsondata->>'sresult' as sresult,rp.jsondata->>'sfinal' as sfinal,rp.ngradecode,rp.nunitcode,rp.ntestparametercode,rp.ntransactionstatus,"
				+ " rt.ntestgrouptestcode,rt.npreregno,rp.ntransactionresultcode,rp.ntestgrouptestparametercode,rt.ntransactiontestcode, "
				+ " rp.jsondata->>'nroundingdigits' as nroundingdigits,rp.nparametertypecode "
				+ " from resultparameter rp,registrationtest rt where rt.npreregno in (" + npreregno
				+ ") and rt.ntestgrouptestcode in (" + ntestgrouptestcode + ") and rt.ntransactiontestcode in ("+ ntransactiontestcode + ") "
				+ " and rp.ntransactiontestcode=rt.ntransactiontestcode and rp.nsitecode = "+userInfo.getNtranssitecode()+""
				+ " and rt.nsitecode = "+userInfo.getNtranssitecode()+" and rp.nstatus = rt.nstatus "
				+ " and rt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";

		List<ResultParameter> lstGetResult = jdbcTemplate.query(queryResult, new ResultParameter());

		final String query = " select nsequenceno from seqnoregistration where stablename=N'resultchangehistory'";
		int nresultchangehistoryno = (int) jdbcUtilityFunction.queryForObject(query, Integer.class,jdbcTemplate);

		returnMap.put("nresultchangehistoryno", nresultchangehistoryno);

		if (lstGetResult != null && lstGetDefaultValue != null && !lstGetResult.isEmpty()
				&& !lstGetDefaultValue.isEmpty()) {
			StringBuilder finalquery = new StringBuilder();

			for (ResultParameter objDefaultValue : lstGetDefaultValue) {
				for (ResultParameter objResult : lstGetResult) {
					if (objDefaultValue.getNtestgrouptestcode() == objResult.getNtestgrouptestcode() && objDefaultValue
							.getNtestgrouptestparametercode() == objResult.getNtestgrouptestparametercode()) {

						List<ResultParameter> lstSingleResults = lstGetResult
								.stream().filter(objjqresultparam -> objjqresultparam
										.getNtransactionresultcode() == objResult.getNtransactionresultcode())
								.collect(Collectors.toList());

						if (!lstSingleResults.isEmpty() && lstSingleResults.get(0).getSresult() != null
								&& !(lstSingleResults.get(0).getSresult()
										.equalsIgnoreCase(objDefaultValue.getSresultvalue()))) {
							nresultchangehistoryno++;
						}

					}
				}
			}

			finalquery.append("update seqnoregistration set nsequenceno = " + nresultchangehistoryno
					+ " where stablename = N'resultchangehistory'");
			jdbcTemplate.execute(finalquery.toString());

		} else {
			returnMap.put("rtn", "IDS_NODEFAULTVALUE");
			return returnMap;
		}
		return returnMap;
	}

	@Override
	public Map<String, Object> seqNoGetforComplete(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Map<String, Object> returnmap = new HashMap<>();
		StringBuilder sb = new StringBuilder();
		String str = "";
		String stransactiontestcode = (String) inputMap.get("transactiontestcode");
		int ntranstatus = Enumeration.TransactionStatus.COMPLETED.gettransactionstatus();
		int napprovalstatus = Enumeration.TransactionStatus.COMPLETED.gettransactionstatus();
		int nautoapproval = 0;
		int nusercode = -1;
		int ndeputyusercode = -1;
		int nuserrolecode = -1;
		int ndeputyuserrolecode = -1;
		int nautodescisioncode = -1;
		String sapproveTestcode = "";
		StringBuilder scompleteTestcode = new StringBuilder();
		String srejectTestcode = "";
		String stransstatus1 = "";
		String stransresultcode = "";
		String ntransactionresultcodeNonmand = "";

		List<RegistrationTestHistory> filteredTestList = validateTestStatus(stransactiontestcode,
				(int) inputMap.get("ncontrolcode"), userInfo,(int) inputMap.get("nneedReceivedInLab"));

		if (!filteredTestList.isEmpty()) {
			stransactiontestcode = filteredTestList.stream()
					.map(object -> String.valueOf(object.getNtransactiontestcode())).collect(Collectors.joining(","));

			final int ncount = getChecklistValidation(stransactiontestcode, userInfo);

			if (ncount > 0) {
				String query = "lock lockcancelreject "+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
				jdbcTemplate.execute(query);

				final String resultentervalidation = "select rp.ntransactionresultcode from  resultparameter rp "
						+ " where rp.ntransactiontestcode in (" + stransactiontestcode + ")" + " and rp.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				final String validateStr = "select ra.napprovalversioncode,rt.npreregno,rt.ntransactionsamplecode,rt.ntransactiontestcode,"
						+ " (rt.jsondata->'nsectioncode'->>'value')::integer "
						+ " from registrationtest rt, registrationarno ra, resultparameter rp "
						+ " where ra.npreregno = rt.npreregno and rp.ntransactiontestcode=rt.ntransactiontestcode "
						+ " and rt.ntransactiontestcode in (" + stransactiontestcode + ") "
						+ " and rt.ntransactiontestcode = any(select b.ntransactiontestcode from ("
						+ " select count(ntransactionresultcode) paramcount,ntransactiontestcode "
						+ " from resultparameter where nresultmandatory = "+ Enumeration.TransactionStatus.YES.gettransactionstatus()
						+ " and ntransactiontestcode = rt.ntransactiontestcode and nsitecode = "+userInfo.getNtranssitecode()+" "
						+ " and nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " group by ntransactiontestcode ) a,("+ " select count(ntransactionresultcode) paramcount,ntransactiontestcode "
						+ " from resultparameter where nresultmandatory = "+ Enumeration.TransactionStatus.YES.gettransactionstatus()
						+ " and ntransactiontestcode = rt.ntransactiontestcode and (rp.jsondata->>'sresult' is not NULL and rp.jsondata->>'sresult' <> '' ) "
						+ " and nsitecode = "+userInfo.getNtranssitecode()+""
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " group by ntransactiontestcode ) b "
						+ " where a.ntransactiontestcode = b.ntransactiontestcode "
						+ " and a.paramcount=b.paramcount ) "
						+ " and rp.nsitecode = "+userInfo.getNtranssitecode()+" "
						+ " and rt.nsitecode = "+userInfo.getNtranssitecode()+" "
						+ " and ra.nsitecode = "+userInfo.getNtranssitecode()+" "
						+ " and rp.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and rt.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and ra.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " group by ra.napprovalversioncode,rt.npreregno,rt.ntransactionsamplecode,rt.ntransactiontestcode,rt.jsondata->'nsectioncode'->>'value' "
						+ " union all "
						+ " select ra.napprovalversioncode,rt.npreregno,rt.ntransactionsamplecode,rt.ntransactiontestcode,(rt.jsondata->'nsectioncode'->>'value')::integer "
						+ " from registrationtest rt, registrationarno ra, resultparameter rp "
						+ " where ra.npreregno = rt.npreregno and rp.ntransactiontestcode=rt.ntransactiontestcode "
						+ " and rt.ntransactiontestcode in (" + stransactiontestcode + ") "
						+ " and rt.ntransactiontestcode = any( select b.ntransactiontestcode from ("
						+ " select count(ntransactionresultcode) paramcount,ntransactiontestcode "
						+ " from resultparameter where ntransactiontestcode = rt.ntransactiontestcode "
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " group by ntransactiontestcode ) a,"
						+ " (select count(ntransactionresultcode) paramcount,ntransactiontestcode "
						+ " from resultparameter where nresultmandatory = "	+ Enumeration.TransactionStatus.NO.gettransactionstatus()
						+ " and ntransactiontestcode = rt.ntransactiontestcode "
						+ " and nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " group by ntransactiontestcode ) b "
						+ " where a.ntransactiontestcode = b.ntransactiontestcode and a.paramcount=b.paramcount )"
						+ " and rp.nsitecode = "+userInfo.getNtranssitecode()+""
						+ " and rt.nsitecode = "+userInfo.getNtranssitecode()+""
						+ " and ra.nsitecode = "+userInfo.getNtranssitecode()+""
						+ " and rp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and rt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ra.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " group by ra.napprovalversioncode,rt.npreregno,rt.ntransactionsamplecode,rt.ntransactiontestcode,rt.jsondata->'nsectioncode'->>'value' ;";


				List<ResultParameter> lstresultentervalidation = jdbcTemplate.query(resultentervalidation,	new ResultParameter());
				List<RegistrationTest> lsttransactiontest = jdbcTemplate.query(validateStr,	new RegistrationTest());
				if (lstresultentervalidation.isEmpty()) {
					returnmap.put("rtn", "IDS_NOPARAMETERFOUND");
					return returnmap;
				} else if (!lsttransactiontest.isEmpty()) {
					final String getReParams = "select rp.jsondata->>'sresult' as sresult,rp.jsondata->>'sfinal' as sfinal,rp.nunitcode,rp.ngradecode,"
							+ " rp.ntransactiontestcode,rp.ntransactionresultcode,rp.npreregno,rp.nresultmandatory from resultparameter rp "
							+ " where rp.ntransactiontestcode in ("+ stransactiontestcode + ") "
							+ " and rp.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ " and rp.nsitecode = "+userInfo.getNtranssitecode()+";";


					//final List<String> lst = Arrays.asList("registrationtesthistory","sampleapprovalhistory","registrationdecisionhistory","registrationhistory","registrationsamplehistory");

					final String getSeqNo = "select stablename,nsequenceno from seqnoregistration where stablename in (N'registrationtesthistory',N'sampleapprovalhistory',N'registrationdecisionhistory',N'registrationhistory',N'registrationsamplehistory')";

					List<ResultParameter> lstresultparam = jdbcTemplate.query(getReParams, new ResultParameter());
					List<SeqNoRegistration> lstSeqNo = jdbcTemplate.query(getSeqNo, new SeqNoRegistration());

					Map<String, Integer> seqMap = lstSeqNo.stream().collect(Collectors.toMap(SeqNoRegistration::getStablename, SeqNoRegistration::getNsequenceno));

					int ntesthistorycode = seqMap.get("registrationtesthistory");
					int napprovalhistorycode = seqMap.get("sampleapprovalhistory");
					int napprovedecisionhistorycode = seqMap.get("registrationdecisionhistory");
					int nreghistcode = seqMap.get("registrationhistory");
					int nsampleHistoryCode = seqMap.get("registrationsamplehistory");
					//int nchainCustodyCode = seqMap.get("chaincustody");

					//ALPD-4833(22-01-2025)--Added by vignesh R -->Approval Configuration - Toggle for Failed tests will also be auto approved
					boolean nneedautoinnerband=false;
					boolean nneedautoouterband=false;

					returnmap.put("ntesthistorycode", ntesthistorycode);
					returnmap.put("napprovalhistorycode", napprovalhistorycode);
					returnmap.put("napprovedecisionhistorycode", napprovedecisionhistorycode);
					returnmap.put("nreghistcode", nreghistcode);
					returnmap.put("nsamplehistcode", nsampleHistoryCode);
					//returnmap.put("nchaincustodycode", nchainCustodyCode);


					final int napprovalversioncode = lsttransactiontest.get(0).getNapprovalversioncode();
					final String npreregno = ((lsttransactiontest.stream()
							.map(object -> String.valueOf(object.getNpreregno())).collect(Collectors.toList())).stream()
							.distinct().collect(Collectors.toList())).stream().collect(Collectors.joining(","));
					final String transactionSampleCode = ((lsttransactiontest.stream()
							.map(object -> String.valueOf(object.getNtransactionsamplecode()))
							.collect(Collectors.toList())).stream().distinct().collect(Collectors.toList())).stream()
							.collect(Collectors.joining(","));
					final String nsectioncode = ((lsttransactiontest.stream()
							.map(object -> String.valueOf(object.getNsectioncode())).collect(Collectors.toList()))
							.stream().distinct().collect(Collectors.toList())).stream()
							.collect(Collectors.joining(","));
					int nFullyapprove = 0;
					String msg = "IDS_SELECTALLTESTBASEDONPREREG";

					str = "select acap.nneedautoinnerband,acap.nneedautoouterband,acap.nneedautoapproval as ntoplevelautoapprove,apcr.nuserrolecode,apcr.nlevelno,apcr.nautoapproval as nrolewiseautoapprove,apcr.npartialapprovalneed,apcr.nsectionwiseapprovalneed, "
							+ " apcr.napprovalstatuscode,acap.nautoapprovalstatus,apcr.nsectionwiseapprovalneed,nautodescisionstatuscode,npartialapprovalneed,acap.nautodescisionstatus as ntopleveldescisionstatuscode, "
							+ " apcr.nautodescisionstatuscode from approvalconfigautoapproval acap,approvalconfigrole apcr "
							+ " where acap.napprovalconfigversioncode = apcr.napproveconfversioncode and acap.napprovalconfigversioncode = "+ napprovalversioncode + " "
							+ " and apcr.nsitecode = "+userInfo.getNmastersitecode()+" "
							+ " and acap.nsitecode = "+userInfo.getNmastersitecode()+""
							+ " and apcr.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ " and acap.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "; ";

					List<ApprovalConfigRole> lstAutoApprove = jdbcTemplate.query(str, new ApprovalConfigRole());

					int i = lstAutoApprove.size() - 1;
					String sectionCondition = "";

					if (!lstAutoApprove.isEmpty()) {
						if (lstAutoApprove.get(0).getNtoplevelautoapprove() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
							if (lstAutoApprove.get(i).getNpartialapprovalneed() == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
								if (lstAutoApprove.get(i)
										.getNsectionwiseapprovalneed() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
									nautoapproval = 1;
									nFullyapprove = 2; // sectionwise fully approve
									napprovalstatus = lstAutoApprove.get(0).getNautoapprovalstatus();
									nuserrolecode = lstAutoApprove.get(0).getNuserrolecode();
									ndeputyuserrolecode = lstAutoApprove.get(0).getNuserrolecode();
									nautodescisioncode = lstAutoApprove.get(0).getNtopleveldescisionstatuscode();
								} else {
									nautoapproval = 1; // fully approve
									napprovalstatus = lstAutoApprove.get(0).getNautoapprovalstatus();
									nuserrolecode = lstAutoApprove.get(0).getNuserrolecode();
									ndeputyuserrolecode = lstAutoApprove.get(0).getNuserrolecode();
									nautodescisioncode = lstAutoApprove.get(0).getNtopleveldescisionstatuscode();
								}
							} else {
								nautoapproval = 1; // partial approve
								napprovalstatus = lstAutoApprove.get(0).getNautoapprovalstatus();
								nuserrolecode = lstAutoApprove.get(0).getNuserrolecode();
								ndeputyuserrolecode = lstAutoApprove.get(0).getNuserrolecode();
								nautodescisioncode = lstAutoApprove.get(0).getNtopleveldescisionstatuscode();
							}
						} else {
							while (i >= 0) {
								if (lstAutoApprove.get(i).getNrolewiseautoapprove() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
									if (lstAutoApprove.get(i).getNpartialapprovalneed() == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
										if (lstAutoApprove.get(i).getNsectionwiseapprovalneed() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
											nautoapproval = 1;
											nFullyapprove = 2;
											napprovalstatus = lstAutoApprove.get(i).getNapprovalstatuscode();
											nuserrolecode = lstAutoApprove.get(i).getNuserrolecode();
											ndeputyuserrolecode = lstAutoApprove.get(i).getNuserrolecode();
											nautodescisioncode = lstAutoApprove.get(0).getNautodescisionstatuscode();
										} else {
											nFullyapprove = 1;
											nautoapproval = 1;
											napprovalstatus = lstAutoApprove.get(i).getNapprovalstatuscode();
											nuserrolecode = lstAutoApprove.get(i).getNuserrolecode();
											ndeputyuserrolecode = lstAutoApprove.get(i).getNuserrolecode();
											nautodescisioncode = lstAutoApprove.get(0).getNautodescisionstatuscode();
										}
									} else {
										nautoapproval = 1;
										napprovalstatus = lstAutoApprove.get(i).getNapprovalstatuscode();
										nuserrolecode = lstAutoApprove.get(i).getNuserrolecode();
										ndeputyuserrolecode = lstAutoApprove.get(i).getNuserrolecode();
										nautodescisioncode = lstAutoApprove.get(0).getNautodescisionstatuscode();
									}
								} else {
									break;
								}
								i--;
							}
						}
					}

					if (nFullyapprove == 2) {
						msg = "IDS_SECTIONBASEDTEST";
						sectionCondition = "and rt.nsectioncode in (" + nsectioncode + ")";
					} else if (nFullyapprove == 1) {
						final String validateTestCountQuery = "select rt.* from registrationtesthistory rth,registrationtest rt"
								+ " where rt.ntransactiontestcode = rth.ntransactiontestcode  "
								+ " and rth.ntesthistorycode = any  ("
								+ " select max(rth1.ntesthistorycode) as testhistorycode"
								+ " from registrationtesthistory rth1  where rth1.npreregno in ("+ npreregno + ") "
								+ " and rth1.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " group by rth1.npreregno,rth1.ntransactionsamplecode,rth1.ntransactiontestcode)"
								+ sectionCondition + " and rth.nsitecode = rt.nsitecode and rt.nsitecode = "+userInfo.getNtranssitecode()+" "
								+ " and rth .ntransactionstatus not in ("
								+ Enumeration.TransactionStatus.RETEST.gettransactionstatus() + ","
								+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ","
								+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ")"
								+ " and rt.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and rth.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

						List<RegistrationTest> transTestList2 = jdbcTemplate.query(validateTestCountQuery,new RegistrationTest());

						if (transTestList2.size() != lsttransactiontest.size()) {
							returnmap.put("rtn", msg);
							return returnmap;
						}
					}
					List<String> lstntransactiontestcode = Arrays.asList(lsttransactiontest.stream().map(objtest -> String.valueOf(objtest.getNtransactiontestcode()))
							.collect(Collectors.joining(",")));

					int completestatus = Enumeration.TransactionStatus.COMPLETED.gettransactionstatus();
					String spreregno = "";
					int oldpreregno = -1;
					int sampleApprovalHistorySeqno = napprovalhistorycode;


					List<ResultParameter> lstparamgradeinnerband =new ArrayList<>();
					List<ResultParameter> lstparamgradeouterband =new ArrayList<>();
					List<String> ntransactiontestcodeList1=null;
					/*ALPD-4833
					 * Role wise auto approval inner band and outer band was not developed
					 *Rules engine configure was not developed
					 */
					//ALPD-4833,Vignesh R(22-01-2025)--if configure the autto approval inner band
					if(lstAutoApprove.get(0).getNneedautoinnerband()==Enumeration.TransactionStatus.YES.gettransactionstatus()) {

						nuserrolecode = lstAutoApprove.get(0).getNuserrolecode();
						ndeputyuserrolecode = lstAutoApprove.get(0).getNuserrolecode();
						nautodescisioncode = lstAutoApprove.get(0).getNtopleveldescisionstatuscode();

						lstparamgradeinnerband = lstresultparam.stream()
								.filter(objreult ->
								objreult.getNgradecode() != Enumeration.Grade.PASS.getGrade() && objreult.getNgradecode() != Enumeration.Grade.FIO.getGrade() &&
								objreult.getNgradecode() != Enumeration.Grade.NA.getGrade() ).collect(Collectors.toList());

						if(!lstparamgradeinnerband.isEmpty()){

							ntransactiontestcodeList1= lstparamgradeinnerband.stream().map(object -> String.valueOf(object.getNtransactiontestcode())) 
									.distinct().collect(Collectors.toList()); 
						}

					}
					//ALPD-4833,Vignesh R(22-01-2025)--if configure the auto approval outer band
					else if(lstAutoApprove.get(0).getNneedautoouterband()==Enumeration.TransactionStatus.YES.gettransactionstatus()) {

						nuserrolecode = lstAutoApprove.get(0).getNuserrolecode();
						ndeputyuserrolecode = lstAutoApprove.get(0).getNuserrolecode();
						nautodescisioncode = lstAutoApprove.get(0).getNtopleveldescisionstatuscode();

						lstparamgradeouterband = lstresultparam.stream()
								.filter(objreult -> 
								objreult.getNgradecode() != Enumeration.Grade.OOT.getGrade() &&
								objreult.getNgradecode() != Enumeration.Grade.H_OOT.getGrade() &&
								objreult.getNgradecode() != Enumeration.Grade.PASS.getGrade() &&
								objreult.getNgradecode() != Enumeration.Grade.FIO.getGrade()&&
								objreult.getNgradecode() != Enumeration.Grade.NA.getGrade()
										)
								.collect(Collectors.toList());
						if(!lstparamgradeouterband.isEmpty()){

							ntransactiontestcodeList1= lstparamgradeouterband.stream().map(object -> String.valueOf(object.getNtransactiontestcode())) 
									.distinct().collect(Collectors.toList()); 

						}}

					for (RegistrationTest objtransactiontest : lsttransactiontest) {
						int resultdata = 1;

						List<ResultParameter> lstparamresult = lstresultparam.stream().filter(objreult -> objreult
								.getNtransactiontestcode() == objtransactiontest.getNtransactiontestcode()).collect(Collectors.toList());

						ntranstatus = napprovalstatus;
						// No mandatory field reject update
						List<ResultParameter> lstnotmandatoryparam = lstparamresult.stream()
								.filter(objnonmandyres -> objnonmandyres.getNresultmandatory() == Enumeration.TransactionStatus.NO.gettransactionstatus()
								&& (objnonmandyres.getSfinal() == null || objnonmandyres.getSfinal().isEmpty()))
								.collect(Collectors.toList());

						if(lstAutoApprove.get(0).getNneedautoinnerband()==Enumeration.TransactionStatus.YES.gettransactionstatus()
								||lstAutoApprove.get(0).getNneedautoouterband()==Enumeration.TransactionStatus.YES.gettransactionstatus()) {

							if(!lstparamgradeinnerband.isEmpty()|| !lstparamgradeouterband.isEmpty()) {
								//String[] strArray=ntransactiontestcodeList.split(",");
								String numStr = String.valueOf(objtransactiontest.getNtransactiontestcode());		
								boolean isMatch = ntransactiontestcodeList1.stream().anyMatch(numStr::equals);

								if(isMatch) {

									nneedautoinnerband=false;
									nneedautoouterband=false;

								}else {
									if(lstAutoApprove.get(0).getNneedautoinnerband()==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
										nneedautoinnerband=true;
									}else if(lstAutoApprove.get(0).getNneedautoouterband()==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
										nneedautoouterband=true;
									}

								}
							}
							else {
								if(lstAutoApprove.get(0).getNneedautoinnerband()==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
									nneedautoinnerband=true;
								}else if(lstAutoApprove.get(0).getNneedautoouterband()==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
									nneedautoouterband=true;
								}

							}
						}

						if (!lstnotmandatoryparam.isEmpty()) {
							ntransactionresultcodeNonmand = lstnotmandatoryparam.stream().map(objnonmandatoryresultcode -> String
									.valueOf(objnonmandatoryresultcode.getNtransactionresultcode()))
									.collect(Collectors.joining(","));

							List<ResultParameter> lstcompleteupdate = lstparamresult.stream().filter(obj -> lstnotmandatoryparam.stream().anyMatch(obj1 -> obj1
									.getNtransactionresultcode() != obj.getNtransactionresultcode()))
									.collect(Collectors.toList());

							if (!lstcompleteupdate.isEmpty()) {
								String stransactionresultcode = lstcompleteupdate.stream().map(resultcode -> String.valueOf(resultcode.getNtransactionresultcode()))
										.collect(Collectors.joining(","));
							}
						}

						for (ResultParameter objresultparam : lstparamresult) {
							if (objresultparam.getSfinal() != null) {
								resultdata = 0;
								break;
							}
						}

						if (resultdata == 1) {
							ntranstatus = Enumeration.TransactionStatus.REJECTED.gettransactionstatus();
						} else {
							scompleteTestcode.append("," + objtransactiontest.getNtransactiontestcode());
							if (oldpreregno != objtransactiontest.getNpreregno()) {
								oldpreregno = objtransactiontest.getNpreregno();
								sampleApprovalHistorySeqno++;
							}
						}
						ntesthistorycode++;
						//nchainCustodyCode++;
						if ((nautoapproval == 1 && resultdata == 0)||nneedautoouterband||nneedautoinnerband) {
							ntesthistorycode++;
							//nchainCustodyCode++;
						}
					}
					//increment for auto approval innerband or outerband
					if (nautoapproval == 1 || (nneedautoouterband&&lstparamgradeouterband.isEmpty()) || (nneedautoinnerband&&lstparamgradeinnerband.isEmpty())) {
						sb.append("update seqnoregistration set nsequenceno="+ (napprovedecisionhistorycode + npreregno.split(",").length)
								+ " where stablename=N'registrationdecisionhistory';");
					}

					final String sampleString = String.join(",",
							Stream.of(npreregno.trim().split(",")).collect(Collectors.toSet()));

					nreghistcode = nreghistcode + sampleString.split(",").length;
					nsampleHistoryCode = nsampleHistoryCode + transactionSampleCode.split(",").length;
					if (nautoapproval == 1 || (lstAutoApprove.get(0).getNneedautoouterband()==Enumeration.TransactionStatus.YES.gettransactionstatus()) || (lstAutoApprove.get(0).getNneedautoinnerband()==Enumeration.TransactionStatus.YES.gettransactionstatus())) {
						nreghistcode = nreghistcode + sampleString.split(",").length;
					}
					sb.append("update seqnoregistration set nsequenceno=" + (ntesthistorycode)+ " where stablename = N'registrationtesthistory';");
					sb.append("update seqnoregistration set nsequenceno="+ (napprovalhistorycode + npreregno.split(",").length)
							+ " where stablename = N'sampleapprovalhistory';");
					sb.append("update seqnoregistration set nsequenceno=" + nreghistcode+ " where stablename = N'registrationhistory';");
					sb.append("update seqnoregistration set nsequenceno=" + nsampleHistoryCode+ " where stablename = N'registrationsamplehistory';");
					//					sb.append("update seqnoregistration set nsequenceno=" + (nchainCustodyCode)
					//							+ " where stablename = N'chaincustody';");
					jdbcTemplate.execute(sb.toString());
					returnmap.put("rtn", Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
				} else {
					returnmap.put("rtn", "IDS_ENTERTHERESULTTOCOMPLETE");
					return returnmap;
				}
			} else {
				returnmap.put("rtn", "IDS_ENTERCHECKLIST");
				return returnmap;
			}
		} else {
			returnmap.put("rtn", "IDS_SELECTVALIDTEST");
		}
		return returnmap;
	}

	/*
	 * public List<Map<String,Object>> seqNoGetforComplete(UserInfo userInfo) throws
	 * Exception { List<Map<String,Object>> lstseqNo = null; String
	 * strobj=" select nlockcode from locktestcomplete "+Enumeration.ReturnStatus.
	 * TABLOCK.getreturnstatus(); getJdbcTemplate().execute(strobj);
	 * 
	 * String
	 * seqNoStr="select nsequenceno from seqnoregistration where stablename in (N'registrationtesthistory',N'sampleapprovalhistory',N'approvalparameter',N'registrationdecisionhistory')"
	 * ; return lstseqNo = getJdbcTemplate().queryForList(seqNoStr); }
	 */

	public int getChecklistValidation(final String ntransactiontestcode, final UserInfo userInfo) {

		final String str = "select case when (select count(tgtp.ntestgrouptestparametercode) ntestgrouptestparametercode "
				+ " from resultparameter rp,testgrouptestparameter tgtp "
				+ " where rp.ntestgrouptestparametercode = tgtp.ntestgrouptestparametercode and rp.ntransactiontestcode in ("+ ntransactiontestcode + ") "
				+ " and rp.nsitecode = "+userInfo.getNtranssitecode()+" and tgtp.nsitecode = "+userInfo.getNmastersitecode()+""
				+ "  and rp.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and tgtp.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and tgtp.nchecklistversioncode >0) = "
				+ " (select count (distinct rp.ntransactionresultcode) ntransactionresultcode from resultparameter rp, "
				+ " resultchecklist rc where rp.ntransactionresultcode=rc.ntransactionresultcode "
				+ " and rp.nsitecode = "+userInfo.getNtranssitecode()+" "
				+ " and rp.ntransactiontestcode in (" + ntransactiontestcode + ") "
				+ " and rp.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and rc.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+") then "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" else "+Enumeration.TransactionStatus.EMPTY.gettransactionstatus()+" end";

		return jdbcTemplate.queryForObject(str, Integer.class);

	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> createCompleteTest(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Map<String,Object> rulemap=new LinkedHashMap<String, Object>();
		ObjectMapper objMapper = new ObjectMapper();
		Map<String, Object> returnmap = new HashMap<>();
		final List<Integer> lstInteger = new ArrayList<>();
		String str = "";
		String stransactiontestcode = (String) inputMap.get("transactiontestcode");
		final int controlcode = (Integer) inputMap.get("ncontrolcode");
		int ntranstatus = Enumeration.TransactionStatus.COMPLETED.gettransactionstatus();
		int napprovalstatus = Enumeration.TransactionStatus.COMPLETED.gettransactionstatus();
		int nautoapproval = 0;
		int nusercode = -1;
		int ndeputyusercode = -1;
		int nuserrolecode = -1;
		int ndeputyuserrolecode = -1;
		int nautodescisioncode = -1;
		String sapproveTestcode = "";
		String scompleteTestcode = "";
		String srejectTestcode = "";
		String stransstatus1 = "";
		String stransresultcode = "";
		String ntransactionresultcodeNonmand = "";
		//ALPD-4833,Vignesh R(04-10-2024)--if configure the autto approval inner band
		boolean nneedautoinnerband=false;
		boolean nneedautoouterband=false;
		boolean iscompleteStatus=false;

		String insertTestHistory = "";
		String insertSampleHistory = "";
		String insertApprovalParam = "";
		String deleteApprovalParam = "";
		String updateResultParam = "";
		String transactiontestcode = "";
		String transactionsamplecode="";

		String rulestransactiontestcode= "";

		String npreRegNo = inputMap.get("npreregno").toString();
		Map<String, Object> mailMap = new HashMap<String, Object>();
		mailMap.put("ncontrolcode", inputMap.get("ncontrolcode"));
		mailMap.put("nregtypecode", inputMap.get("nregtypecode"));
		mailMap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
		mailMap.put("napprovalversioncode", inputMap.get("napprovalversioncode"));

		UserInfo mailUserInfo = new UserInfo(userInfo);

		List<Integer> ntransactiontestcode = Stream.of(stransactiontestcode.split(",")).map(String::trim)
				.map(Integer::parseInt).collect(Collectors.toList());

		List<RegistrationTestHistory> filteredTestList = validateTestStatus(stransactiontestcode,
				(int) inputMap.get("ncontrolcode"), userInfo,(int) inputMap.get("nneedReceivedInLab"));

		inputMap.put("lsttransactiontestcode", ntransactiontestcode.get(0));

		if (!filteredTestList.isEmpty()) {
			stransactiontestcode = filteredTestList.stream()
					.map(object -> String.valueOf(object.getNtransactiontestcode())).collect(Collectors.joining(","));

			final int ncount = getChecklistValidation(stransactiontestcode, userInfo);

			if (ncount > 0) {
				//				String query = "lock lockcancelreject"+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
				//				getJdbcTemplate().execute(query);

				final String resultentervalidation = "select rp.ntransactionresultcode from  resultparameter rp "
						+ " where rp.ntransactiontestcode in (" + stransactiontestcode + ")" + " and rp.nsitecode = "+userInfo.getNtranssitecode()+" "
						+ " and rp.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				final String validateStr = "select ra.napprovalversioncode,rt.ntestgrouptestcode,rt.npreregno,rt.ntransactionsamplecode,"
						+ " rt.ntransactiontestcode,(rt.jsondata->'nsectioncode'->>'value')::integer "
						+ " from registrationtest rt, registrationarno ra, resultparameter rp "
						+ " where ra.npreregno = rt.npreregno and rp.ntransactiontestcode=rt.ntransactiontestcode "
						+ " and rt.ntransactiontestcode in (" + stransactiontestcode + ") "
						+ " and rt.ntransactiontestcode = any( select b.ntransactiontestcode from ("
						+ " select count(ntransactionresultcode) paramcount,ntransactiontestcode "
						+ " from resultparameter where nresultmandatory = "+ Enumeration.TransactionStatus.YES.gettransactionstatus()
						+ " and ntransactiontestcode = rt.ntransactiontestcode and nsitecode = "+userInfo.getNtranssitecode()+"  "
						+ " and nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " group by ntransactiontestcode ) a,("
						+ " select count(ntransactionresultcode) paramcount,ntransactiontestcode "
						+ " from resultparameter where nresultmandatory = "
						+ Enumeration.TransactionStatus.YES.gettransactionstatus()
						+ " and ntransactiontestcode = rt.ntransactiontestcode and (jsondata->>'sresult' is not NULL and jsondata->>'sresult' <> '' ) "
						+ " and nsitecode = "+userInfo.getNtranssitecode()+" and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " group by ntransactiontestcode ) b "
						+ " where a.ntransactiontestcode = b.ntransactiontestcode "
						+ " and a.paramcount=b.paramcount ) "
						+ " and rp.nsitecode = "+userInfo.getNtranssitecode()+" "
						+ " and rt.nsitecode = "+userInfo.getNtranssitecode()+" "
						+ " and ra.nsitecode = "+userInfo.getNtranssitecode()+" "
						+ " and rp.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and rt.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and ra.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" "
						+ " group by ra.napprovalversioncode,rt.ntestgrouptestcode,rt.npreregno,rt.ntransactionsamplecode,rt.ntransactiontestcode,"
						+ " rt.jsondata->'nsectioncode'->>'value' "
						+ " union all "
						+ " select ra.napprovalversioncode,rt.ntestgrouptestcode,rt.npreregno,rt.ntransactionsamplecode,rt.ntransactiontestcode,(rt.jsondata->'nsectioncode'->>'value')::integer "
						+ " from registrationtest rt, registrationarno ra, resultparameter rp "
						+ " where ra.npreregno = rt.npreregno and rp.ntransactiontestcode=rt.ntransactiontestcode "
						+ " and rt.ntransactiontestcode in (" + stransactiontestcode + ") "
						+ " and rt.ntransactiontestcode = any( select b.ntransactiontestcode from ("
						+ " select count(ntransactionresultcode) paramcount,ntransactiontestcode "
						+ " from resultparameter where ntransactiontestcode = rt.ntransactiontestcode "
						+ " and nsitecode = "+userInfo.getNtranssitecode()+" and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " group by ntransactiontestcode ) a,("
						+ " select count(ntransactionresultcode) paramcount,ntransactiontestcode "
						+ " from resultparameter where nresultmandatory = "
						+ Enumeration.TransactionStatus.NO.gettransactionstatus()
						+ " and ntransactiontestcode = rt.ntransactiontestcode and nsitecode = "+userInfo.getNtranssitecode()+" and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " group by ntransactiontestcode ) b "
						+ " where a.ntransactiontestcode = b.ntransactiontestcode " + "and a.paramcount=b.paramcount )"
						+ " and rp.nsitecode = "+userInfo.getNtranssitecode()+" "
						+ " and rt.nsitecode = "+userInfo.getNtranssitecode()+" "
						+ " and ra.nsitecode = "+userInfo.getNtranssitecode()+" "
						+ " and rp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and rt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ra.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " group by ra.napprovalversioncode,rt.ntestgrouptestcode,rt.npreregno,rt.ntransactionsamplecode,rt.ntransactiontestcode,rt.jsondata->'nsectioncode'->>'value' ;";

				List<ResultParameter> lstresultentervalidation = jdbcTemplate.query(resultentervalidation, new ResultParameter());
				List<RegistrationTest> lsttransactiontest = jdbcTemplate.query(validateStr,	new RegistrationTest());

				JSONObject auditNewData = new JSONObject();
				JSONObject auditOldData = new JSONObject();
				JSONObject auditActionType = new JSONObject();

				String transcode = "0";
				if (stransstatus1.length() > 1) {
					transcode = stransstatus1;
				}

				String oldAuditTestCode = lsttransactiontest.stream().map(objtest -> String.valueOf(objtest.getNtransactiontestcode()))
						.collect(Collectors.joining(","));

				String stestcode1 = "0";
				stestcode1 = oldAuditTestCode.equals("")?stestcode1: stestcode1 + "," + oldAuditTestCode;


				// audit old start
				final String queryold = "select r.ntesthistorycode ,r.ntransactiontestcode ,r.npreregno ,"
						+ "r.ntransactionstatus,t.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
						+ "' as stransdisplaystatus,rt.jsondata->>'stestsynonym' stestsynonym,ra.sarno,rs.ssamplearno,"
						+ "rt.jsondata->>'ssectionname' ssectionname,rt.jsondata->>'smethodname' smethodname,"
						+ " CASE WHEN(SELECT cast(ssettingvalue as Integer) FROM settings WHERE nsettingcode="+Enumeration.Settings.UPDATING_ANALYSER.getNsettingcode()+" )="+Enumeration.TransactionStatus.YES.gettransactionstatus()+" "
						+ " THEN  rt.jsondata->>'AnalyserName' ELSE u.sfirstname ||' '||u.slastname  END AS analysername "
						+ " from registrationtest rt,registrationarno ra,registrationsamplearno rs,"
						+ " registrationtesthistory r,transactionstatus t,users u where rt.npreregno=ra.npreregno"
						+ " and rs.npreregno=ra.npreregno and rs.npreregno=rt.npreregno and rt.npreregno=r.npreregno"
						+ " and rt.ntransactionsamplecode=r.ntransactionsamplecode and rs.ntransactionsamplecode=r.ntransactionsamplecode and ra.npreregno=r.npreregno"
						+ " and rs.ntransactionsamplecode=rt.ntransactionsamplecode and rt.ntransactiontestcode=r.ntransactiontestcode and r.nusercode=u.nusercode and u.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						+ " and rt.ntransactiontestcode in ( " + stestcode1 + ") and t.ntranscode=r.ntransactionstatus"
						+ " and r.ntransactionstatus in (  select ntransactionstatus from  registrationtesthistory where ntransactiontestcode IN ( "
						+ stestcode1 + " )  and nsitecode = "+userInfo.getNtranssitecode()+" and ntransactionstatus not IN (     "
						+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() + "," + transcode + ","
						+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + " ) order by  ntesthistorycode  desc fetch first 1 rows only   )"
						+ "and r.ntesthistorycode =any(select max(ntesthistorycode) as ntesthistorycode "
						+ "	from registrationtesthistory where ntransactiontestcode in ( " + stestcode1 + ") "
						+ " group by npreregno,ntransactionsamplecode,ntransactiontestcode)"
						+ " and rt.nsitecode = "+userInfo.getNtranssitecode()+" "
						+ " and rs.nsitecode = "+userInfo.getNtranssitecode()+" "
						+ " and ra.nsitecode = "+userInfo.getNtranssitecode()+" "
						+ " and r.nsitecode = "+userInfo.getNtranssitecode()+" "
						+ " and r.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " and rt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						+ " and ra.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						+ " and rs.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						+ " and t.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
						+ " order by rt.ntransactiontestcode asc;";
				List<RegistrationTestHistory> lstrpold = jdbcTemplate.query(queryold, new RegistrationTestHistory());

				Map<Integer, List<RegistrationTestHistory>> lstobjectDataold = lstrpold.stream().collect(
						Collectors.groupingBy(RegistrationTestHistory::getNtransactionstatus, Collectors.toList()));
				// audit old end

				if (lstresultentervalidation.isEmpty()) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NOPARAMETERFOUND",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);

				} else if (!lsttransactiontest.isEmpty()) {
					final String getReParams = "select rp.jsondata->>'sresult' as sresult,rp.jsondata->>'sfinal' as sfinal,rp.nunitcode,rp.ngradecode,rp.ntransactiontestcode,rp.ntransactionresultcode,rp.npreregno,rp.nresultmandatory,rp.nparametertypecode from resultparameter rp where rp.ntransactiontestcode in ("
							+ stransactiontestcode + ") and rp.nsitecode = "+userInfo.getNtranssitecode()+" and rp.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

					List<ResultParameter> lstresultparam = jdbcTemplate.query(getReParams, new ResultParameter());

					int ntesthistorycode = (int) inputMap.get("ntesthistorycode");
					int napprovalhistorycode = (int) inputMap.get("napprovalhistorycode");
					int napprovedecisionhistorycode = (int) inputMap.get("napprovedecisionhistorycode");

					int nreghistcode = (int) inputMap.get("nreghistcode");
					int nsampleHistoryCode = (int) inputMap.get("nsamplehistcode");
					//int nchainCustodyCode = (int) inputMap.get("nchaincustodycode");

					final int napprovalversioncode = lsttransactiontest.get(0).getNapprovalversioncode();

					final String npreregno = ((lsttransactiontest.stream().map(object -> String.valueOf(object.getNpreregno())).collect(Collectors.toList())).stream()
							.distinct().collect(Collectors.toList())).stream().collect(Collectors.joining(","));

					final String transactionSampleCode = ((lsttransactiontest.stream().map(object -> String.valueOf(object.getNtransactionsamplecode()))
							.collect(Collectors.toList())).stream().distinct().collect(Collectors.toList())).stream()
							.collect(Collectors.joining(","));

					final String nsectioncode = ((lsttransactiontest.stream().map(object -> String.valueOf(object.getNsectioncode())).collect(Collectors.toList()))
							.stream().distinct().collect(Collectors.toList())).stream().collect(Collectors.joining(","));
					int nFullyapprove = 0;

					String msg = "IDS_SELECTALLTESTBASEDONPREREG";

					str = "select acap.nneedautoinnerband,acap.nneedautoouterband,acap.nneedautoinnerband,acap.nneedautoouterband,acap.nneedautoapproval as ntoplevelautoapprove,apcr.nuserrolecode,apcr.nlevelno,apcr.nautoapproval as nrolewiseautoapprove,apcr.npartialapprovalneed,apcr.nsectionwiseapprovalneed, "
							+ " apcr.napprovalstatuscode,acap.nautoapprovalstatus,apcr.nsectionwiseapprovalneed,nautodescisionstatuscode,npartialapprovalneed,acap.nautodescisionstatus as ntopleveldescisionstatuscode, "
							+ " apcr.nautodescisionstatuscode from approvalconfigautoapproval acap,approvalconfigrole apcr "
							+ " where acap.napprovalconfigversioncode = apcr.napproveconfversioncode and acap.napprovalconfigversioncode = "
							+ napprovalversioncode + " "
							+ " and apcr.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ " and acap.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";

					List<ApprovalConfigRole> lstAutoApprove = jdbcTemplate.query(str, new ApprovalConfigRole());

					int i = lstAutoApprove.size() - 1;
					String sectionCondition = "";
					if (!lstAutoApprove.isEmpty()) {
						if (lstAutoApprove.get(0).getNtoplevelautoapprove() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
							if (lstAutoApprove.get(i).getNpartialapprovalneed() == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
								if (lstAutoApprove.get(i).getNsectionwiseapprovalneed() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
									nautoapproval = 1;
									nFullyapprove = 2;// sectionwise fully approve
									napprovalstatus = lstAutoApprove.get(0).getNautoapprovalstatus();
									nuserrolecode = lstAutoApprove.get(0).getNuserrolecode();
									ndeputyuserrolecode = lstAutoApprove.get(0).getNuserrolecode();
									nautodescisioncode = lstAutoApprove.get(0).getNtopleveldescisionstatuscode();
								} else {
									nautoapproval = 1;// fully approve
									napprovalstatus = lstAutoApprove.get(0).getNautoapprovalstatus();
									nuserrolecode = lstAutoApprove.get(0).getNuserrolecode();
									ndeputyuserrolecode = lstAutoApprove.get(0).getNuserrolecode();
									nautodescisioncode = lstAutoApprove.get(0).getNtopleveldescisionstatuscode();
								}
							} else {
								nautoapproval = 1;// partial approve
								napprovalstatus = lstAutoApprove.get(0).getNautoapprovalstatus();
								nuserrolecode = lstAutoApprove.get(0).getNuserrolecode();
								ndeputyuserrolecode = lstAutoApprove.get(0).getNuserrolecode();
								nautodescisioncode = lstAutoApprove.get(0).getNtopleveldescisionstatuscode();
							}
						} else {
							while (i >= 0) {
								if (lstAutoApprove.get(i).getNrolewiseautoapprove() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
									if (lstAutoApprove.get(i).getNpartialapprovalneed() == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
										if (lstAutoApprove.get(i).getNsectionwiseapprovalneed() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
											nautoapproval = 1;
											nFullyapprove = 2;
											napprovalstatus = lstAutoApprove.get(i).getNapprovalstatuscode();
											nuserrolecode = lstAutoApprove.get(i).getNuserrolecode();
											ndeputyuserrolecode = lstAutoApprove.get(i).getNuserrolecode();
											nautodescisioncode = lstAutoApprove.get(0).getNautodescisionstatuscode();
										} else {
											nFullyapprove = 1;
											nautoapproval = 1;
											napprovalstatus = lstAutoApprove.get(i).getNapprovalstatuscode();
											nuserrolecode = lstAutoApprove.get(i).getNuserrolecode();
											ndeputyuserrolecode = lstAutoApprove.get(i).getNuserrolecode();
											nautodescisioncode = lstAutoApprove.get(0).getNautodescisionstatuscode();
										}
									} else {
										nautoapproval = 1;
										napprovalstatus = lstAutoApprove.get(i).getNapprovalstatuscode();
										nuserrolecode = lstAutoApprove.get(i).getNuserrolecode();
										ndeputyuserrolecode = lstAutoApprove.get(i).getNuserrolecode();
										nautodescisioncode = lstAutoApprove.get(0).getNautodescisionstatuscode();
									}
								} else {
									break;
								}
								i--;
							}
						}
					}

					if (nFullyapprove == 2) {
						msg = "IDS_SECTIONBASEDTEST";
						sectionCondition = "and rt.nsectioncode in (" + nsectioncode + ")";
					} else if (nFullyapprove == 1) {
						final String validateTestCountQuery = "select rt.* from registrationtesthistory rth,registrationtest rt"
								+ " where rt.ntransactiontestcode = rth.ntransactiontestcode  "
								+ " and rth.ntesthistorycode = any  ("
								+ " select max(rth1.ntesthistorycode) as testhistorycode"
								+ " from registrationtesthistory rth1  where rth1.npreregno in ("+ npreregno + ")" + "     "
								+ " and rth1.nsitecode = "+userInfo.getNtranssitecode()+" "
								+ " and rth1.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " group by rth1.npreregno,rth1.ntransactionsamplecode,rth1.ntransactiontestcode)"
								+ sectionCondition + " "
								+ " and rth.nsitecode = rt.nsitecode "
								+ " and rt.nsitecode = "+userInfo.getNtranssitecode()+" "
								+ " and rth .ntransactionstatus not in ("
								+ Enumeration.TransactionStatus.RETEST.gettransactionstatus() + ","
								+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ","
								+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ")"
								+ " and rt.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and rth.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

						List<RegistrationTest> transTestList2 = jdbcTemplate.query(validateTestCountQuery,new RegistrationTest());

						if (transTestList2.size() != lsttransactiontest.size()) {
							return new ResponseEntity<>(
									commonFunction.getMultilingualMessage(msg, userInfo.getSlanguagefilename()),
									HttpStatus.EXPECTATION_FAILED);
						}
					}

					// Common code
					List<String> lstntransactiontestcode = Arrays.asList(lsttransactiontest.stream()
							.map(objtest -> String.valueOf(objtest.getNtransactiontestcode()))
							.collect(Collectors.joining(",")));

					// complete update

					int completestatus = Enumeration.TransactionStatus.COMPLETED.gettransactionstatus();
					String spreregno = "";
					int oldpreregno = -1;
					boolean sgradeflag = false;
					String flatStatusNpreregno = "";
					int sampleApprovalHistorySeqno = napprovalhistorycode;

					List<ResultParameter> lstparamgrade = lstresultparam.stream()
							.filter(objreult -> objreult.getNgradecode() == Enumeration.Grade.OOS.getGrade()
							|| objreult.getNgradecode() == Enumeration.Grade.OOT.getGrade()
							|| objreult.getNgradecode() == Enumeration.Grade.H_OOT.getGrade()
							|| objreult.getNgradecode() == Enumeration.Grade.H_OOS.getGrade())
							.collect(Collectors.toList());

					List<ResultParameter> lstparamgradeinnerband =new ArrayList<>();
					List<ResultParameter> lstparamgradeouterband =new ArrayList<>();
					/*ALPD-4833
					 * Role wise auto approval inner band and outer band was not developed
					 *Rules engine configure was not developed
					 */
					//				//ALPD-4833,Vignesh R(04-10-2024)--if configure the autto approval inner band

					//List<ResultParameter> ntransactiontestcodeList =new ArrayList<>();
					List<String> ntransactiontestcodeList1=null;

					//auto approval innerband
					if(lstAutoApprove.get(0).getNneedautoinnerband()==Enumeration.TransactionStatus.YES.gettransactionstatus()) {

						nuserrolecode = lstAutoApprove.get(0).getNuserrolecode();
						ndeputyuserrolecode = lstAutoApprove.get(0).getNuserrolecode();
						nautodescisioncode = lstAutoApprove.get(0).getNtopleveldescisionstatuscode();

						lstparamgradeinnerband = lstresultparam.stream()
								.filter(objreult -> objreult.getNgradecode() != Enumeration.Grade.PASS.getGrade()&&
								objreult.getNgradecode() != Enumeration.Grade.FIO.getGrade()&&
								objreult.getNgradecode() != Enumeration.Grade.NA.getGrade()
										).collect(Collectors.toList());

						if(!lstparamgradeinnerband.isEmpty()){
							ntransactiontestcodeList1= lstparamgradeinnerband.stream()
									.map(object -> String.valueOf(object.getNtransactiontestcode())) 
									.distinct() 
									.collect(Collectors.toList()); 
						}

					}
					//auot approval outer band
					else if(lstAutoApprove.get(0).getNneedautoouterband()==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
						nuserrolecode = lstAutoApprove.get(0).getNuserrolecode();
						ndeputyuserrolecode = lstAutoApprove.get(0).getNuserrolecode();
						nautodescisioncode = lstAutoApprove.get(0).getNtopleveldescisionstatuscode();

						lstparamgradeouterband = lstresultparam.stream()
								.filter(objreult -> 
								objreult.getNgradecode() != Enumeration.Grade.OOT.getGrade() &&
								objreult.getNgradecode() != Enumeration.Grade.H_OOT.getGrade() &&
								objreult.getNgradecode() != Enumeration.Grade.PASS.getGrade()&&
								objreult.getNgradecode() != Enumeration.Grade.FIO.getGrade()&&
								objreult.getNgradecode() != Enumeration.Grade.NA.getGrade()
										)
								.collect(Collectors.toList());
						if(!lstparamgradeouterband.isEmpty()){

							ntransactiontestcodeList1= lstparamgradeouterband.stream().map(object -> String.valueOf(object.getNtransactiontestcode())) 
									.distinct().collect(Collectors.toList()); 

						}}

					if (lstparamgrade.size() > 0) {
						if (!sgradeflag) {
							sgradeflag = true;
						}

						flatStatusNpreregno = flatStatusNpreregno + ((lstparamgrade.stream().map(object -> String.valueOf(object.getNpreregno()))
								.collect(Collectors.toList())).stream().distinct().collect(Collectors.toList()))
								.stream().collect(Collectors.joining(","));
					}
					if(inputMap.get("basedrulesengine").equals(Enumeration.TransactionStatus.YES.gettransactionstatus())) { 
						//Read Rules Call Start
						rulemap.putAll(inputMap); 
						rulemap.putAll((Map<String, Object>) objRulesEngineFunctions.rulesEngineValidationBasedOnSample(stransactiontestcode,lsttransactiontest,npreregno,true,userInfo).getBody());

						if(rulemap.containsKey("isApplicableforReadingRule")&&(boolean)rulemap.get("isApplicableforReadingRule")) { 
							List<Integer> lstntestgrouptestcode =  (List<Integer>) rulemap.get("ruleAppliedntestgrouptestcode"); 
							lsttransactiontest=lsttransactiontest.stream().filter(x->!lstntestgrouptestcode.contains(x.getNtransactiontestcode())
									).collect(Collectors.toList()); 
						}  

					}

					//Read Rules Call End
					transactiontestcode =lsttransactiontest.stream()
							.map(objtest -> String.valueOf(objtest.getNtransactiontestcode()))
							.collect(Collectors.joining(","));
					transactionsamplecode =lsttransactiontest.stream()
							.map(objtest -> String.valueOf(objtest.getNtransactionsamplecode()))
							.collect(Collectors.joining(","));
					for (RegistrationTest objtransactiontest : lsttransactiontest) {
						int resultdata = 1;
						completestatus = Enumeration.TransactionStatus.COMPLETED.gettransactionstatus();
						List<ResultParameter> lstparamresult = lstresultparam.stream().filter(objreult -> objreult
								.getNtransactiontestcode() == objtransactiontest.getNtransactiontestcode())
								.collect(Collectors.toList());

						ntranstatus = napprovalstatus;

						if(lstAutoApprove.get(0).getNneedautoinnerband()==Enumeration.TransactionStatus.YES.gettransactionstatus()
								||lstAutoApprove.get(0).getNneedautoouterband()==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
							if(!lstparamgradeouterband.isEmpty()|| !lstparamgradeinnerband.isEmpty()) {
								//String[] strArray=ntransactiontestcodeList.split(",");
								String numStr = String.valueOf(objtransactiontest.getNtransactiontestcode());		
								boolean isMatch = ntransactiontestcodeList1.stream()
										.anyMatch(numStr::equals);
								if(isMatch) {
									nneedautoinnerband=false;
									nneedautoouterband=false;
									iscompleteStatus=true;
									ntranstatus=Enumeration.TransactionStatus.COMPLETED.gettransactionstatus();
									completestatus=Enumeration.TransactionStatus.COMPLETED.gettransactionstatus();
								}else {
									if(lstAutoApprove.get(0).getNneedautoinnerband()==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
										nneedautoinnerband=true;
									}else if(lstAutoApprove.get(0).getNneedautoouterband()==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
										nneedautoouterband=true;
									}
									ntranstatus=Enumeration.TransactionStatus.APPROVED.gettransactionstatus();
									completestatus=Enumeration.TransactionStatus.COMPLETED.gettransactionstatus();
								}
							}
							else {
								if(lstAutoApprove.get(0).getNneedautoinnerband()==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
									nneedautoinnerband=true;
								}else if(lstAutoApprove.get(0).getNneedautoouterband()==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
									nneedautoouterband=true;
								}
								ntranstatus=Enumeration.TransactionStatus.APPROVED.gettransactionstatus();
								completestatus=Enumeration.TransactionStatus.COMPLETED.gettransactionstatus();
							}
						}

						// No mandatory field reject update
						List<ResultParameter> lstnotmandatoryparam = lstparamresult.stream()
								.filter(objnonmandyres -> objnonmandyres
										.getNresultmandatory() == Enumeration.TransactionStatus.NO
										.gettransactionstatus()
										&& (objnonmandyres.getSfinal() == null || objnonmandyres.getSfinal().isEmpty()))
								.collect(Collectors.toList());

						if (!lstnotmandatoryparam.isEmpty()) {
							ntransactionresultcodeNonmand = lstnotmandatoryparam.stream()
									.map(objnonmandatoryresultcode -> String
											.valueOf(objnonmandatoryresultcode.getNtransactionresultcode()))
									.collect(Collectors.joining(","));

							updateResultParam = "update resultparameter set ntransactionstatus="
									+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus()
									+ " where  nsitecode = "+userInfo.getNtranssitecode()+" "
									+ " and ntransactionresultcode in (" + ntransactionresultcodeNonmand + ") "
									+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ;";

							List<ResultParameter> lstcompleteupdate = lstparamresult.stream()
									.filter(obj -> lstnotmandatoryparam.stream().noneMatch(obj1 -> obj1
											.getNtransactionresultcode() == obj.getNtransactionresultcode()))
									.collect(Collectors.toList());

							if (!lstcompleteupdate.isEmpty()) {
								String stransactionresultcode = lstcompleteupdate.stream()
										.map(resultcode -> String.valueOf(resultcode.getNtransactionresultcode()))
										.collect(Collectors.joining(","));
								updateResultParam += "update resultparameter set ntransactionstatus=" + ntranstatus
										+ " where nsitecode = "+userInfo.getNtranssitecode()+" and ntransactionresultcode in (" + stransactionresultcode + ") "
										+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";

								deleteApprovalParam += "delete from approvalparameter where ntransactiontestcode = "
										+ objtransactiontest.getNtransactiontestcode() + " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
										+ " and nsitecode = "+userInfo.getNtranssitecode()+" and "
										+ " EXISTS(select 1 from approvalparameter where ntransactiontestcode = "
										+ objtransactiontest.getNtransactiontestcode() + " and nsitecode = "+userInfo.getNtranssitecode()+" "
										+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+");";

								insertApprovalParam += "insert into approvalparameter (ntransactionresultcode,ntransactiontestcode,npreregno,ntestgrouptestparametercode,"
										+ " ntestparametercode,ntestgrouptestformulacode,nparametertypecode,ncalculatedresult,ngradecode,nreportmandatory,nresultmandatory,"
										+ " nenforcestatus,nenforceresult,nlinkcode,nattachmenttypecode,ntransactionstatus,nunitcode,nenteredby,"
										+ "  nenteredrole,ndeputyenteredby,ndeputyenteredrole, jsondata,nsitecode,nstatus)"
										+ " ((select ntransactionresultcode,ntransactiontestcode,npreregno,"
										+ " ntestgrouptestparametercode, ntestparametercode,ntestgrouptestformulacode,nparametertypecode,ncalculatedresult,"
										+ " ngradecode,nreportmandatory,nresultmandatory,nenforcestatus,nenforceresult,nlinkcode,nattachmenttypecode,"
										+ " ntransactionstatus,nunitcode,nenteredby,nenteredrole,ndeputyenteredby,ndeputyenteredrole,jsondata,nsitecode,nstatus "
										+ " from resultparameter where ntransactionresultcode in ("
										+ stransactionresultcode + ") and nsitecode = "+userInfo.getNtranssitecode()+" "
										+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"));";
							}

							jdbcTemplate.execute(updateResultParam);
							jdbcTemplate.execute(deleteApprovalParam);
							jdbcTemplate.execute(insertApprovalParam);

						} else {
							updateResultParam = "update resultparameter set ntransactionstatus=" + ntranstatus
									+ " where nsitecode = "+userInfo.getNtranssitecode()+" and ntransactiontestcode=" + objtransactiontest.getNtransactiontestcode()+" "
									+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";

							deleteApprovalParam = "delete from approvalparameter where ntransactiontestcode = "
									+ objtransactiontest.getNtransactiontestcode() + " and nsitecode = "+userInfo.getNtranssitecode()+" "
									+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
									+ " EXISTS(select 1 from approvalparameter where ntransactiontestcode = "
									+ objtransactiontest.getNtransactiontestcode() + " and nsitecode = "+userInfo.getNtranssitecode()+" "
									+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+");";

							insertApprovalParam = "insert into approvalparameter (ntransactionresultcode,npreregno,ntransactiontestcode,ntestgrouptestparametercode,ntestparametercode,"
									+ " nparametertypecode,nresultmandatory,nreportmandatory,ntestgrouptestformulacode,nunitcode,ngradecode,ntransactionstatus,"
									+ " nenforcestatus,nenforceresult,nenteredby,"
									+ " nenteredrole,ndeputyenteredby,ndeputyenteredrole,nlinkcode,nattachmenttypecode,jsondata,nsitecode,nstatus)"
									+ " ((select ntransactionresultcode,npreregno,ntransactiontestcode,ntestgrouptestparametercode,"
									+ " ntestparametercode,nparametertypecode,nresultmandatory,nreportmandatory,ntestgrouptestformulacode,nunitcode,ngradecode,ntransactionstatus,"
									+ " nenforcestatus,nenforceresult,nenteredby,"
									+ " nenteredrole,ndeputyenteredby,ndeputyenteredrole,nlinkcode,nattachmenttypecode,jsondata,nsitecode,nstatus "
									+ " from resultparameter where ntransactiontestcode in ("
									+ objtransactiontest.getNtransactiontestcode() + ") and nsitecode = "+userInfo.getNtranssitecode()+" "
									+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"));";

							jdbcTemplate.execute(updateResultParam);
							jdbcTemplate.execute(deleteApprovalParam);
							jdbcTemplate.execute(insertApprovalParam);
						}

						for (ResultParameter objresultparam : lstparamresult) {
							if (objresultparam.getSfinal() != null && !objresultparam.getSfinal().isEmpty()) {
								resultdata = 0;
								break;
							}
						}

						if (resultdata == 1) {
							srejectTestcode = srejectTestcode + ","
									+ String.valueOf(objtransactiontest.getNtransactiontestcode());
							ntranstatus = Enumeration.TransactionStatus.REJECTED.gettransactionstatus();
							jdbcTemplate.execute("update resultparameter set ntransactionstatus=" + ntranstatus
									+ " where nsitecode  = "+userInfo.getNtranssitecode()+" "
									+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
									+ " and ntransactiontestcode=" + objtransactiontest.getNtransactiontestcode()
									+ ";");
						} else {
							scompleteTestcode = scompleteTestcode + ","
									+ String.valueOf(objtransactiontest.getNtransactiontestcode());

							if (oldpreregno != objtransactiontest.getNpreregno()) {
								oldpreregno = objtransactiontest.getNpreregno();
								spreregno = spreregno.equals("") ? String.valueOf(objtransactiontest.getNpreregno())
										: spreregno + "," + String.valueOf(objtransactiontest.getNpreregno());
								sampleApprovalHistorySeqno++;
							}
						}

						completestatus = resultdata == 1 ? ntranstatus : completestatus;
						nusercode = nautoapproval == 1 || nautoapproval == 2 || nneedautoouterband || nneedautoinnerband ? -1 : userInfo.getNusercode();
						// ALPD-5596 - added by Gowtham R - Store the Analyzer Name in registrationtest table wheather setting[45] is 3 or 4
						//						if(inputMap.containsKey("nsettingcode") && (int)inputMap.get("nsettingcode")==Enumeration.TransactionStatus.YES.gettransactionstatus()) {

						String commaSeparatedString = "";
						List<Integer>alist =new ArrayList<>();
						for (ResultParameter objresultparam : lstparamresult) {

							if (objresultparam.getSfinal() != null && !objresultparam.getSfinal().isEmpty() 
									&& objresultparam.getNresultmandatory() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
								alist.add(objresultparam.getNtransactionresultcode());		
							}

						}
						if(alist.size()>0) {
							commaSeparatedString = alist.stream().map(String::valueOf).collect(Collectors.joining(","));
						}else {
							commaSeparatedString = lstparamresult.stream().map(resultcode -> String.valueOf(resultcode.getNtransactionresultcode()))
									.collect(Collectors.joining(","));
						}

						String  strquery1="select ntransactiontestcode from resultparameter "
								+ "where ntransactionresultcode in("+commaSeparatedString+") and nsitecode ="+userInfo.getNtranssitecode()+" "
								+ " and nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" group by ntransactiontestcode ";

						List<ResultParameter> lstResultParameter =jdbcTemplate.query(strquery1,new ResultParameter());



						String stranstestcoderesultparam = lstResultParameter.stream().map(objtranscode -> String.valueOf(objtranscode.getNtransactiontestcode()))
								.collect(Collectors.joining(","));

						String strquery="select * from registrationtest where ntransactiontestcode in("+stranstestcoderesultparam+") "
								+ " and nsitecode ="+userInfo.getNtranssitecode()+" and nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";
						List<RegistrationTest> lstRegistrationtest =jdbcTemplate.query(strquery,new RegistrationTest());


						for(RegistrationTest  obj :lstRegistrationtest) {

							Map<String, Object> jsonData = obj.getJsondata();
							Integer usercode=(Integer) inputMap.get("nusercode");
							String username=(String) inputMap.get("susername");									
							jsonData.put("AnalyserCode", usercode);
							jsonData.put("AnalyserName", username);

							String updateQuery="update registrationtest set jsondata= "+ "jsondata || '" + stringUtilityFunction.replaceQuote(objMapper.writeValueAsString(jsonData)) + "' "
									+ " where ntransactiontestcode in("+obj.getNtransactiontestcode()+") "
									+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";

							jdbcTemplate.execute(updateQuery);						

						}								
						//						}

						jdbcTemplate.execute(
								"insert into registrationtesthistory ( ntesthistorycode,ntransactiontestcode,ntransactionsamplecode,npreregno,nformcode,"
										+ " ntransactionstatus,nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments,dtransactiondate,"
										+ " noffsetdtransactiondate,ntransdatetimezonecode,nsampleapprovalhistorycode,nsitecode,nstatus) "
										+ " ((select " + (ntesthistorycode) + "+Rank()over(order by ntesthistorycode)"
										+ " ntesthistorycode,ntransactiontestcode,ntransactionsamplecode,npreregno,"
										+ userInfo.getNformcode() + " nformcode," + completestatus
										+ " ntransactionstatus," + nusercode + " nusercode, " + ""
										+ userInfo.getNuserrole() + " nuserrolecode," + userInfo.getNdeputyusercode()
										+ " ndeputyusercode," + userInfo.getNdeputyuserrole()
										+ " ndeputyuserrolecode,N'" +stringUtilityFunction.replaceQuote(userInfo.getSreason())
										+ "' scomments,'" + dateUtilityFunction.getCurrentDateTime(userInfo)+"' dtransactiondate,"+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())+","+userInfo.getNtimezonecode()+","
										+ " -1 nsampleapprovalhistorycode , "+userInfo.getNtranssitecode()+" nsitecode,"
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
										+ " from registrationtesthistory where ntesthistorycode in "
										+ " (select max(rth.ntesthistorycode) from registrationtesthistory rth "
										+ " where nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
										+ " and rth.nsitecode = "+userInfo.getNtranssitecode()+" "
										+ " and rth.ntransactiontestcode in ("+ objtransactiontest.getNtransactiontestcode() + ")"
										+ " group by rth.ntransactiontestcode,rth.npreregno,rth.ntransactionsamplecode) "
										+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
										+ " and nsitecode = "+userInfo.getNtranssitecode()+"));");

						ntesthistorycode++;

						//createChainCustody( nchainCustodyCode,String.valueOf(objtransactiontest.getNtransactiontestcode()), completestatus, userInfo);
						//nchainCustodyCode++;
						if ((nautoapproval == 1 && resultdata == 0) || nneedautoouterband || nneedautoinnerband) {
							jdbcTemplate.execute(
									"insert into registrationtesthistory ( ntesthistorycode,ntransactiontestcode,ntransactionsamplecode,npreregno,nformcode,"
											+ " ntransactionstatus,nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments,dtransactiondate,noffsetdtransactiondate,ntransdatetimezonecode,nsampleapprovalhistorycode,nsitecode,nstatus) "
											+ " ((select " + (ntesthistorycode)
											+ "+Rank()over(order by ntesthistorycode)"
											+ " ntesthistorycode,ntransactiontestcode,ntransactionsamplecode,npreregno,"
											+ Enumeration.FormCode.APPROVAL.getFormCode() + " nformcode," + ntranstatus
											+ " " + " ntransactionstatus," + nusercode + " nusercode, " + nuserrolecode
											+ " nuserrolecode,-1 ndeputyusercode," + " " + ndeputyuserrolecode
											+ " ndeputyuserrolecode,N'" +stringUtilityFunction.replaceQuote(userInfo.getSreason())
											+ "' scomments,'" +dateUtilityFunction.getCurrentDateTime(userInfo)+ "' dtransactiondate,"
											+ " "+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())+","+userInfo.getNtimezonecode()+"," + sampleApprovalHistorySeqno + " nsampleapprovalhistorycode, "
											+ " "+userInfo.getNtranssitecode()+" nsitecode,"
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
											+ " from registrationtesthistory where ntesthistorycode in (select max(rth.ntesthistorycode) from registrationtesthistory rth "
											+ " where nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
											+ " and rth.nsitecode = "+userInfo.getNtranssitecode()+" and rth.ntransactiontestcode in ("
											+ objtransactiontest.getNtransactiontestcode() + ")"
											+ " group by rth.ntransactiontestcode,rth.npreregno,rth.ntransactionsamplecode) "
											+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
											+ " and nsitecode = "+userInfo.getNtranssitecode()+") );");

							ntesthistorycode++;
							stransstatus1 = String.valueOf(ntranstatus);
							sapproveTestcode = sapproveTestcode + ","
									+ String.valueOf(objtransactiontest.getNtransactiontestcode());

							//createChainCustody( nchainCustodyCode,String.valueOf(objtransactiontest.getNtransactiontestcode()), ntranstatus, userInfo);
							//nchainCustodyCode++;
						}
					}

					if (nautoapproval == 1 || (nneedautoouterband&&lstparamgradeouterband.isEmpty()) || (nneedautoinnerband && lstparamgradeinnerband.isEmpty())) {
						jdbcTemplate.execute(" insert into sampleapprovalhistory (napprovalhistorycode,npreregno,nusercode,"
								+ " nuserrolecode,ndeputyusercode,ndeputyuserrolecode,dtransactiondate,scomments,ntransactionstatus,nsitecode,nstatus)"
								+ " select " + napprovalhistorycode
								+ "+Rank()over(order by npreregno) napprovalhistorycode,npreregno npreregno,-1 nusercode,"
								+ nuserrolecode + " nuserrolecode," + " -1 ndeputyusercode,"
								+ ndeputyuserrolecode + " ndeputyuserrolecode,'" +dateUtilityFunction.getCurrentDateTime(userInfo)// objGeneral.getUTCDateTime()
								+ "' dtransactiondate," + " N'" +stringUtilityFunction.replaceQuote(userInfo.getSreason())
								+ "' scomments," + ntranstatus
								+ " ntransactionstatus,"+userInfo.getNtranssitecode()+",1 nstatus from registration " + " where npreregno in ("+ spreregno + ") "
								+ " and nsitecode = "+userInfo.getNtranssitecode()+" "
								+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" order by npreregno;");

						jdbcTemplate.execute("insert into registrationdecisionhistory(nregdecisionhistorycode,npreregno,ndecisionstatus,dtransactiondate,"
								+ "nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments,nstatus) "
								+ "select " + napprovedecisionhistorycode
								+ "+rank()over(order by npreregno) nregdecisionhistorycode,npreregno,"
								+ nautodescisioncode + " ndecisionstatus," + "'" +dateUtilityFunction.getCurrentDateTime(userInfo)
								+ "' dtransactiondate,-1 nusercode," + nuserrolecode + " nuserrolecode,"
								+ "-1 ndeputyusercode," + ndeputyuserrolecode + " ndeputyuserrolecode,N'"
								+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "' scomments,"
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
								+ " from registration where  npreregno in (" + spreregno + ") "
								+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and nsitecode = "+userInfo.getNtranssitecode()+" "
								+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
								+ " order by npreregno ;");

					}

					if(inputMap.get("basedrulesengine").equals(Enumeration.TransactionStatus.YES.gettransactionstatus())) {
						rulemap.putAll(inputMap);
						rulestransactiontestcode=  transactiontestcode;
						String rulestransactionSampleCode=transactionsamplecode;
						if(!rulestransactiontestcode.equals(""))
						{
							rulemap.putAll((Map<String,Object>) objRulesEngineFunctions.readRulesBasedOnSample(rulestransactiontestcode,rulestransactionSampleCode,rulemap,userInfo).getBody());
							//ALPD-4828
							//Result entry-->sample disappeared when complete the single test
							if(rulemap.containsKey("getNewSample") && ((Boolean)rulemap.get("getNewSample"))) {
								inputMap.put("nflag",2);
							}
						}
					}
					updateSubSampleStatus(Enumeration.TransactionStatus.COMPLETED.gettransactionstatus(),
							transactionSampleCode, userInfo, stringUtilityFunction.replaceQuote(userInfo.getSreason()), nsampleHistoryCode,inputMap);

					Map<String, Object> map = updateSampleStatus(
							Enumeration.TransactionStatus.COMPLETED.gettransactionstatus(), npreregno, userInfo,
							stringUtilityFunction.replaceQuote(userInfo.getSreason()), nreghistcode, inputMap);
					nreghistcode = (int) map.get("nreghistcode");

					if (!flatStatusNpreregno.isEmpty()) {
						updateSampleFlagStatus(flatStatusNpreregno, userInfo);
					}

					if ((nautoapproval == 1) || (nneedautoouterband&&lstparamgradeouterband.isEmpty()) || (nneedautoinnerband&&lstparamgradeinnerband.isEmpty())) {
						Map<String, Object> map1 = updateSampleStatus(ntranstatus, npreregno, userInfo,	stringUtilityFunction.replaceQuote(userInfo.getSreason()), nreghistcode, inputMap);
						updateSubSampleStatus(ntranstatus, transactionSampleCode, userInfo,stringUtilityFunction.replaceQuote(userInfo.getSreason()), nsampleHistoryCode, inputMap);
						nreghistcode = (int) map.get("nreghistcode");
					}
					//}
					// updateSectionHisotory(Enumeration.TransactionStatus.COMPLETED.gettransactionstatus(),nsectioncode,npreregno,userInfo,"comments");
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ENTERTHERESULTTOCOMPLETE",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}

				if (ntransactionresultcodeNonmand != "") {
					String query = "select * from resultparameter where ntransactionresultcode in ("
							+ ntransactionresultcodeNonmand + ") and nsitecode = "+userInfo.getNtranssitecode()+" and ntransactionstatus="
							+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus()+" "
							+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";

					List<ResultParameter> lstRp = jdbcTemplate.query(query, new ResultParameter());
					List<Object> lstafterRp = new ArrayList<>();
					lstafterRp.add(lstRp);
					//					fnInsertListTransactionAuditAction(lstafterRp, 1, null, Arrays.asList("IDS_REJECTTESTPARAMETER"),
					//							userInfo, objMap);
				}

				Map<String, Object> objMap = new HashMap<>();
				objMap.put("nregtypecode", (int) inputMap.get("nregtypecode"));
				objMap.put("nregsubtypecode", (int) inputMap.get("nregsubtypecode"));
				objMap.put("ndesigntemplatemappingcode", (int) inputMap.get("ndesigntemplatemappingcode"));

				String transcode1 = "0";
				if (stransstatus1.length() > 1) {
					transcode1 = stransstatus1;
				}
				String stestcode = "0";
				if (scompleteTestcode.length() > 1) {
					stestcode = stestcode + "," + scompleteTestcode.substring(1, scompleteTestcode.length());
				}
				if (sapproveTestcode.length() > 1) {
					stestcode = stestcode + "," + sapproveTestcode.substring(1, sapproveTestcode.length());

				}
				if (srejectTestcode.length() > 1) {
					stestcode = stestcode + "," + srejectTestcode.substring(1, srejectTestcode.length());

				}

				String query = "select r.ntesthistorycode ,r.ntransactiontestcode ,r.npreregno ,"
						+ "r.ntransactionstatus,t.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
						+ "' as stransdisplaystatus,rt.jsondata->>'stestsynonym' stestsynonym,ra.sarno,rs.ssamplearno,"
						+ "rt.jsondata->>'ssectionname' ssectionname,rt.jsondata->>'smethodname' smethodname, "
						+ " CASE WHEN(SELECT cast(ssettingvalue as Integer) FROM settings WHERE nsettingcode="+Enumeration.Settings.UPDATING_ANALYSER.getNsettingcode()+" )="+Enumeration.TransactionStatus.YES.gettransactionstatus()+" "
						+ " THEN  rt.jsondata->>'AnalyserName' ELSE u.sfirstname ||' '||u.slastname  END AS analysername "
						+ "  from registrationtest rt,registrationarno ra,registrationsamplearno rs,"
						+ " registrationtesthistory r,transactionstatus t,users u where rt.npreregno=ra.npreregno"
						+ " and rs.npreregno=ra.npreregno and rs.npreregno=rt.npreregno and rt.npreregno=r.npreregno"
						+ " and rt.ntransactionsamplecode=r.ntransactionsamplecode and rs.ntransactionsamplecode=r.ntransactionsamplecode and ra.npreregno=r.npreregno"
						+ " and rs.ntransactionsamplecode=rt.ntransactionsamplecode and rt.ntransactiontestcode=r.ntransactiontestcode and r.nusercode=u.nusercode and u.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
						+ " and rt.ntransactiontestcode in ( " + stestcode + ") and t.ntranscode=r.ntransactionstatus"
						+ " and r.ntransactionstatus in ("+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() + "," + transcode1 + ","
						+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ") "
						+ " and r.ntesthistorycode =any(select max(ntesthistorycode) as ntesthistorycode "
						+ "	from registrationtesthistory where ntransactiontestcode in ( " + stestcode + ") "
						+ " and nsitecode = "+userInfo.getNtranssitecode()+" "
						+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
						+ " group by npreregno,ntransactionsamplecode,ntransactiontestcode)"
						+ " and rt.nsitecode = "+userInfo.getNtranssitecode()+" "
						+ " and ra.nsitecode = "+userInfo.getNtranssitecode()+" "
						+ " and rs.nsitecode = "+userInfo.getNtranssitecode()+" "
						+ " and r.nsitecode = "+userInfo.getNtranssitecode()+" "
						+ " and r.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and rs.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						+ " and ra.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						+ " and rt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						+ " and t.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" order by rt.ntransactiontestcode asc;";

				List<RegistrationTestHistory> lstrp = jdbcTemplate.query(query, new RegistrationTestHistory());

				Map<Integer, List<RegistrationTestHistory>> lstobjectData = lstrp.stream().collect(
						Collectors.groupingBy(RegistrationTestHistory::getNtransactionstatus, Collectors.toList()));

//				ALPD-ALPDJ21-4	Modified codes by Vishakh for audit issue when multiple conditions satifies (02-07-2025)
//				Start
				List<Object> auditNewList = new ArrayList<>();
				List<String> auditActionList = new ArrayList<>();
				
				// ALPD-4947 Added sapproveTestcode.length() == 0 condition to avoid entering this if block
				if (scompleteTestcode.length() > 1 && sapproveTestcode.length() == 0) {
					lstobjectData.get( Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()).stream().forEach(lst -> {
						auditNewList.add(lst);
						auditActionList.add("IDS_COMPLETETEST");
					});
				
//					auditActionType.put("registrationtest", "IDS_COMPLETETEST");
//					auditNewData.put("registrationtest",
//							lstobjectData.get( Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()));
//					auditOldData.put("registrationtest", lstobjectDataold.get(lstobjectDataold.keySet().toArray()[0]));
				}

				//ALPD-4833,added by Vignesh R(28-01-2025)--if configure the auto approval inner band
				//start
				if (sapproveTestcode.length() > 1) {

					if(iscompleteStatus) {

						List<RegistrationTestHistory> listRegistrationTestHistory= lstrp.stream().filter(item->lstrpold.stream()
								.anyMatch(item2->item.getNtransactiontestcode()==item2.getNtransactiontestcode())).collect(Collectors.toList());			        

						listRegistrationTestHistory.stream().forEach(lst -> {
							auditNewList.add(lst);
							auditActionList.add("IDS_COMPLETEDORAPPROVED");
						});
						
//						auditNewData.put("registrationtest", listRegistrationTestHistory);
//						auditActionType.put("registrationtest", "IDS_COMPLETEDORAPPROVED");

					}
					else {
						String ntranscodeValue = transcode1;
						lstobjectData.get(Integer.parseInt(transcode1)).stream().forEach(lst -> {
							auditNewList.add(lst);
							auditActionList.add(lstobjectData.get(Integer.parseInt(ntranscodeValue)).get(0).getStransdisplaystatus() + "TEST");
						});
						
//						auditActionType.put("registrationtest",
//						lstobjectData.get(Integer.parseInt(transcode1)).get(0).getStransdisplaystatus() + "TEST");	
//						auditNewData.put("registrationtest", lstobjectData.get(Integer.parseInt(transcode1)));
					}
					//end
//					auditOldData.put("registrationtest", lstobjectDataold.get(lstobjectDataold.keySet().toArray()[0]));
				}
				if (srejectTestcode.length() > 1) {
//					auditActionType.put("registrationtest", "IDS_REJECTEDTEST");
//					auditNewData.put("registrationtest",
//							lstobjectData.get(Enumeration.TransactionStatus.REJECTED.gettransactionstatus()));
//					auditOldData.put("registrationtest", lstobjectDataold.get(lstobjectDataold.keySet().toArray()[0]));
					
					lstobjectData.get(Enumeration.TransactionStatus.REJECTED.gettransactionstatus()).stream().forEach(lst -> {
						auditNewList.add(lst);
						auditActionList.add("IDS_REJECTEDTEST");
					});
				}
				auditOldData.put("registrationtest", lstobjectDataold.get(lstobjectDataold.keySet().toArray()[0]));
				auditActionType.put("registrationtest", auditActionList);
				auditNewData.put("registrationtest", auditNewList);
//				ALPD-ALPDJ21-4	Modified codes by Vishakh for audit issue when multiple conditions becomes true (02-07-2025)
//				End
				auditUtilityFunction.fnJsonArrayAudit(auditOldData, auditNewData, auditActionType, objMap, false, userInfo);

			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_ENTERCHECKLIST", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {

			final int regTypeCode = (int) inputMap.get("nregtypecode");
			final int regSubTypeCode = (int) inputMap.get("nregtypecode");

			final String getValidationStatus = "select ts.jsondata->'stransdisplaystatus'->>'"
					+ userInfo.getSlanguagetypecode() + "' as stransdisplaystatus "
					+ " from transactionvalidation rvd,transactionstatus ts"
					+ " where ts.ntranscode = rvd.ntransactionstatus and rvd.ncontrolcode = "
					+ inputMap.get("ncontrolcode") + " and rvd.nformcode = " + userInfo.getNformcode()
					+ " and rvd.nsitecode = " + userInfo.getNmastersitecode() + " "
					+ "and ts.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "and rvd.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "and rvd.nregtypecode="+ regTypeCode + " and rvd.nregsubtypecode=" + regSubTypeCode;

			List<TransactionStatus> validationStatusList = jdbcTemplate.query(getValidationStatus,new TransactionStatus());

			validationStatusList = objMapper.convertValue(
					commonFunction.getMultilingualMessageList(validationStatusList,
							Arrays.asList("stransdisplaystatus"), userInfo.getSlanguagefilename()),
					new TypeReference<List<TransactionStatus>>() {
					});
			final String alertStatus = validationStatusList.stream().map(TransactionStatus::getStransdisplaystatus)
					.collect(Collectors.joining("/"));
			String alertMessage = commonFunction.getMultilingualMessage("IDS_SELECT", userInfo.getSlanguagefilename())
					+ " " + alertStatus + " "
					+ commonFunction.getMultilingualMessage("IDS_TESTONLY", userInfo.getSlanguagefilename());
			return new ResponseEntity<>(alertMessage, HttpStatus.EXPECTATION_FAILED);
		}


		// ALPD-5619 - added sarno by Gowtham R on 28/03/2025 - Mail alert transaction > NULL displayed in reference no column.
		// start
		String strCompleteSampleCheck = "select rth.npreregno, rar.sarno from registrationhistory rth, registrationarno rar"
				+ " where rth.nreghistorycode in (select max(nreghistorycode) from registrationhistory where npreregno in"
				+ " ("+ npreRegNo+") and rth.ntransactionstatus in("+Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()
				+ " ) group by npreregno) and rth.npreregno=rar.npreregno and rth.nsitecode=rar.nsitecode and rth.nsitecode="
				+ userInfo.getNtranssitecode() + " and rth.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rar.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by 1 desc";

		final List<Map<String, Object>> completedPreRegNo = jdbcTemplate.queryForList(strCompleteSampleCheck);
		if(!completedPreRegNo.isEmpty()) {

			for(Map<String, Object> npreregobj : completedPreRegNo) {
				mailMap.put("npreregno", (int) npreregobj.get("npreregno"));
				mailMap.put("ssystemid", (String) npreregobj.get("sarno"));
				// end
				ResponseEntity<Object> mailResponse = emailDAOSupport.createEmailAlertTransaction(mailMap, mailUserInfo);
			}
		}
		//inputMap.put("completeActionCall",true);
		//Added by sonia on  24-07-2024 for Jira ID:ALPD-4649
		boolean rulesValue=true;
		if (Enumeration.TransactionStatus.YES.gettransactionstatus() == ((int)inputMap.get("basedrulesengine"))) {
			if(rulestransactiontestcode.equals("")) {
				rulesValue =false;
				rulemap.put("ruleFlag",1);

			}

		}
		if(rulesValue) {
			rulemap.putAll((Map<String, Object>)getResultEntryDetails(inputMap, userInfo).getBody());
		}
		return new ResponseEntity<>(rulemap, HttpStatus.OK);
	}

	private void updateSampleFlagStatus(String spreregno, UserInfo userInfo) {
		final String updateSamplestatus = "update registrationflagstatus set bflag = true where npreregno = any "
				+ "(select npreregno from registrationflagstatus where npreregno in (" + spreregno
				+ ") and nsitecode = "+userInfo.getNtranssitecode()+" and bflag <> true)";

		jdbcTemplate.execute(updateSamplestatus);
	}

	private Map<String, Object> updateSampleStatus(int ntransStatus, String npreregno, UserInfo userInfo,
			String scomments, int nreghistcode, Map<String, Object> inputMap) throws Exception {
		JSONObject auditNewData = new JSONObject();
		JSONObject auditOldData = new JSONObject();
		JSONObject auditActionType = new JSONObject();
		Map<String, Object> objMap = new HashMap<>();

		StringBuilder sb = new StringBuilder();
		StringJoiner joiner = new StringJoiner(",");
		String approvalPreregno = "";
		String partialPreregno = "";
		String insertPartialReghistory = "";
		String insertReghistory = "";
		Map<String, Object> rtnMap = new HashMap<>();
		final String approvedSampleQuery = "select historycount.npreregno from ("
				+ "select npreregno,count(ntesthistorycode) testcount from registrationtesthistory "
				+ "where ntransactionstatus =" + ntransStatus + " and npreregno in (" + npreregno + ")"
				+ "and ntesthistorycode=any( select max(ntesthistorycode) ntesthistorycode "
				+ "from registrationtesthistory where npreregno in  (" + npreregno + ") "
				+ "and nsitecode = "+userInfo.getNtranssitecode()+" "
				+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by "
				+ "npreregno,ntransactiontestcode)" + ""
				+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and nsitecode = "+userInfo.getNtranssitecode()+" group by npreregno)historycount,"
				+ " (select rth.npreregno,count(ntesthistorycode) testcount "
				+ "from registrationtest rt,registrationtesthistory rth where ntransactionstatus not in ("
				+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() +", "
				+ " "+Enumeration.TransactionStatus.RETEST.gettransactionstatus()+ ") "
				+ " and rt.ntransactiontestcode=rth.ntransactiontestcode "
				+ " and rth.ntesthistorycode = any( select max(ntesthistorycode) ntesthistorycode from registrationtesthistory where npreregno in ("+ npreregno + ") "
				+ " and  nsitecode = "+userInfo.getNtranssitecode()+" "
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by ntransactiontestcode,npreregno) "
				+ " and rt.nsitecode = "+userInfo.getNtranssitecode()+" "
				+ " and rth.nsitecode = "+userInfo.getNtranssitecode()+" "
				+ " and rt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and rth.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
				+ " group by rth.npreregno)testcount "
				+ " where historycount.npreregno=testcount.npreregno "
				+ " and historycount.testcount=testcount.testcount; ";

		final String partialSampleQuery = "select historycount.npreregno from ("
				+ "        select rth.npreregno,count(ntesthistorycode) testcount from registrationtesthistory rth,registrationhistory rh"
				+ "        where rth.ntransactionstatus=" + ntransStatus + "     and rth.npreregno=rh.npreregno"
				+ "        and rh.nreghistorycode=any(select max(nreghistorycode) nreghistorycode"
				+ "				from registrationhistory where npreregno in (" + npreregno + ") "
				+ " 			and nsitecode = "+userInfo.getNtranssitecode()+" and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "         	group by npreregno) and ntesthistorycode=any( "
				+ "			select max(ntesthistorycode) ntesthistorycode from registrationtesthistory "
				+ "			where npreregno in  (" + npreregno + ") " + " "
				+ " and nsitecode = "+userInfo.getNtranssitecode()+" "
				+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by npreregno,ntransactiontestcode" + ") "
				+ " and rh.ntransactionstatus!="+ Enumeration.TransactionStatus.PARTIALLY.gettransactionstatus() + ""
				+ " and rth.nsitecode = "+userInfo.getNtranssitecode()+" and rh.nsitecode = "+userInfo.getNtranssitecode()+" "
				+ " and rth.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by rth.npreregno"
				+ "		)historycount,"
				+ " (select rth.npreregno,count(ntesthistorycode) testcount "
				+ "        from registrationtest rt,registrationtesthistory rth "
				+ "        where ntransactionstatus not in ("+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ") "
				+ "        and rt.ntransactiontestcode=rth.ntransactiontestcode "
				+ "        and rth.ntesthistorycode = any( "
				+ "            	select max(ntesthistorycode) ntesthistorycode from registrationtesthistory "
				+ "            	where npreregno in (" + npreregno + ") "
				+ " and nsitecode = "+userInfo.getNtranssitecode()+" "
				+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "         	group by ntransactiontestcode,npreregno" + ") "
				+ " and rth.nsitecode = "+userInfo.getNtranssitecode()+"  and rt.nsitecode = "+userInfo.getNtranssitecode()+" "
				+ " and rt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and rth.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " group by rth.npreregno"
				+ "		)testcount where historycount.npreregno=testcount.npreregno "
				+ " and historycount.testcount!=testcount.testcount ;";

		final List<RegistrationTestHistory> approvedSampleList =jdbcTemplate.query(approvedSampleQuery, new RegistrationTestHistory());

		final List<RegistrationTestHistory> partialSampleList =	jdbcTemplate.query(partialSampleQuery, new RegistrationTestHistory());

		int seqNoRegistraionHistory = nreghistcode;

		if (!approvedSampleList.isEmpty()) {

			approvalPreregno = approvedSampleList.stream()
					.map(objpreregno -> String.valueOf(objpreregno.getNpreregno())).collect(Collectors.joining(","));

			insertReghistory = "insert into registrationhistory (nreghistorycode,npreregno,ntransactionstatus,dtransactiondate,noffsetdtransactiondate,ntransdatetimezonecode,"
					+ "nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments,nsitecode,nstatus) " + "select "
					+ seqNoRegistraionHistory + "+rank()over(order by npreregno) nreghistorycode,npreregno,"
					+ ntransStatus + " ntransactionstatus," + "'" + dateUtilityFunction.getCurrentDateTime(userInfo)+"'  dtransactiondate,"
					+ " "+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())+","+userInfo.getNtimezonecode()+","
					+ " " + userInfo.getNusercode() + " nusercode," + userInfo.getNuserrole()
					+ " nuserrolecode," + "" + userInfo.getNdeputyusercode() + " ndeputyusercode,"
					+ userInfo.getNdeputyuserrole() + " ndeputyuserrolecode,N'" + scomments + "' scomments, "+userInfo.getNtranssitecode()+" nsitecode,"
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
					+ "from registration where npreregno in (" + approvalPreregno + ") and nsitecode = "+userInfo.getNtranssitecode()+" and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by npreregno;";

			seqNoRegistraionHistory = seqNoRegistraionHistory + approvedSampleList.size();
			joiner.add(approvalPreregno);

		}
		if (!partialSampleList.isEmpty()) {
			partialPreregno = partialSampleList.stream().map(objpreregno -> String.valueOf(objpreregno.getNpreregno()))
					.collect(Collectors.joining(","));

			insertPartialReghistory = "insert into registrationhistory (nreghistorycode,npreregno,ntransactionstatus,dtransactiondate,noffsetdtransactiondate,ntransdatetimezonecode,"
					+ "nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments,nsitecode,nstatus) " + "select "
					+ seqNoRegistraionHistory + "+rank()over(order by npreregno) nreghistorycode,npreregno,"
					+ Enumeration.TransactionStatus.PARTIALLY.gettransactionstatus() + " ntransactionstatus," + "'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo)+ "'  dtransactiondate,"+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())+","+userInfo.getNtimezonecode()+","
					+ " " + userInfo.getNusercode() + " nusercode," + userInfo.getNuserrole()
					+ " nuserrolecode," + "" + userInfo.getNdeputyusercode() + " ndeputyusercode,"
					+ userInfo.getNdeputyuserrole() + " ndeputyuserrolecode,N'" + scomments + "' scomments, "+userInfo.getNtranssitecode()+" nsitecode,"
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
					+ "from registration where npreregno in (" + partialPreregno + ") and nsitecode = "+userInfo.getNtranssitecode()+" and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by npreregno;";

			joiner.add(partialPreregno);
			seqNoRegistraionHistory = seqNoRegistraionHistory + partialSampleList.size();
		}

		JSONArray jsonArrayold = new JSONArray();
		if (!approvedSampleList.isEmpty() || !partialSampleList.isEmpty()) {			

			final String getAuditQueryold = " select json_agg(a.jsonuidata) from ( select r.jsonuidata||"
					+ " json_build_object('stransdisplaystatus',t.jsondata->'stransdisplaystatus'->>'en-US','sarno',ra.sarno)::jsonb as jsonuidata"
					+ " from registration r,registrationhistory rh,transactionstatus t , "
					+ "	registrationarno ra where r.npreregno in ( " + npreregno + ")  "
					+ " and r.npreregno=rh.npreregno  and r.npreregno=ra.npreregno and ra.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and rh.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and r.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and t.ntranscode=rh.ntransactionstatus "
					+ " and rh.ntransactionstatus in (select ntransactionstatus from  registrationhistory where "
					+ "							  nreghistorycode in (select max(nreghistorycode) "
					+ "  from registrationhistory where npreregno in (" + npreregno + ") and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and nsitecode = "+userInfo.getNtranssitecode()+") and rh.nsitecode = "+userInfo.getNtranssitecode()+" "
					+ " and ra.nsitecode = "+userInfo.getNtranssitecode()+" and r.nsitecode = "+userInfo.getNtranssitecode()+") order by r.npreregno)a";

			jsonArrayold = new JSONArray(jdbcTemplate.queryForObject(getAuditQueryold, String.class));


			jdbcTemplate.execute(insertReghistory);
			jdbcTemplate.execute(insertPartialReghistory);

			//ALPD-4129 For MAterial Accounting
			if((int) inputMap.get("nportalrequired") ==  Enumeration.TransactionStatus.YES.gettransactionstatus() && (int) inputMap.get("ninsertMaterialInventoryTrans") ==  Enumeration.TransactionStatus.YES.gettransactionstatus())
			{
				String Str=" select npreregno from registrationhistory where  nreghistorycode in ( "
						+ " select max(nreghistorycode) from registrationhistory group by npreregno) and npreregno in ("+npreregno+") "
						+ " and ntransactionstatus="+Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()+" "
						+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode = "+userInfo.getNtranssitecode()+";";

				final List<RegistrationHistory> samples = jdbcTemplate.query(Str, new RegistrationHistory());

				if(samples.size()>0) {

					final String prereno = samples.stream()
							.map(item -> String.valueOf(item.getNpreregno())).collect(Collectors.joining(","));	

					//sathish
					//insertMaterialinventoryTrans(String.valueOf(Enumeration.CalculationType.TEST.getcalculationtype()),prereno,userInfo);
				}

			}
		}
		rtnMap.put("nreghistcode", seqNoRegistraionHistory);

		final String getAuditQuery = " select json_agg(a.jsonuidata) from ( select r.jsonuidata||"
				+ " json_build_object('stransdisplaystatus',t.jsondata->'stransdisplaystatus'->>'en-US','sarno',ra.sarno)::jsonb as jsonuidata"
				+ " from registration r,registrationhistory rh,transactionstatus t , "
				+ "	registrationarno ra where   "
				+ "  r.npreregno=rh.npreregno  and r.npreregno=ra.npreregno  "
				+ " and t.ntranscode=rh.ntransactionstatus "
				+ " and rh.ntransactionstatus in (select ntransactionstatus from  registrationhistory where "
				+ "							  nreghistorycode in (select max(nreghistorycode) "
				+ "  from registrationhistory where npreregno in (" + npreregno + ") and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "+userInfo.getNtranssitecode()+" group by npreregno) "
				+ " and rh.nsitecode = "+userInfo.getNtranssitecode()+" "
				+ " and ra.nsitecode = "+userInfo.getNtranssitecode()+" "
				+ " and r.nsitecode = "+userInfo.getNtranssitecode()+") and r.npreregno in ( " + npreregno + ")"
				+ " and ra.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rh.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and r.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by r.npreregno)a";

		JSONArray jsonArray = new JSONArray(jdbcTemplate.queryForObject(getAuditQuery, String.class));

		JSONArray actionTypeTestArray = new JSONArray();
		actionTypeTestArray.put("IDS_COMPLETESAMPLE");

		auditOldData.put("registration", jsonArrayold);
		auditNewData.put("registration", jsonArray);
		auditActionType.put("registration", "IDS_COMPLETESAMPLE");
		auditUtilityFunction.fnJsonArrayAudit(auditOldData, auditNewData, auditActionType, inputMap, false, userInfo);

		return rtnMap;
	}

	private Map<String, Object> updateSubSampleStatus(int ntransStatus, String transactionSampleCode, UserInfo userInfo,
			String scomments, int nreghistcode, Map<String, Object> inputMap) throws Exception {
		final StringBuilder sb = new StringBuilder();
		final StringJoiner joiner = new StringJoiner(",");
		String approvalPreregno = "";
		String partialPreregno = "";
		JSONObject auditNewData = new JSONObject();
		JSONObject auditOldData = new JSONObject();
		JSONObject auditActionType = new JSONObject();
		String insertPartialReghistory = "";
		String insertReghistory = "";
		final Map<String, Object> rtnMap = new HashMap<>(); 
		final String approvedSamSampleQuery = "select historycount.ntransactionsamplecode from (" 
				+ "select ntransactionsamplecode,count(ntesthistorycode) testcount from registrationtesthistory "
				+ "where ntransactionstatus =" + ntransStatus + " and ntransactionsamplecode in ("+ transactionSampleCode + ")"
				+ "and ntesthistorycode=any( select max(ntesthistorycode) ntesthistorycode "
				+ "from registrationtesthistory where ntransactionsamplecode in  (" + transactionSampleCode + ")"
				+ " and nsitecode  = "+userInfo.getNtranssitecode()+" and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by "
				+ " ntransactionsamplecode,ntransactiontestcode) and nsitecode  = "+userInfo.getNtranssitecode()+" "
				+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " group by ntransactionsamplecode) historycount,"
				+ "( select rth.ntransactionsamplecode,count(ntesthistorycode) testcount "
				+ "from registrationtest rt,registrationtesthistory rth where ntransactionstatus not in ("
				+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.RETEST.gettransactionstatus() + ") "
				+ "and rt.ntransactiontestcode=rth.ntransactiontestcode and rth.ntesthistorycode = any( "
				+ "select max(ntesthistorycode) ntesthistorycode from registrationtesthistory where ntransactionsamplecode in ("
				+ transactionSampleCode + ") and nsitecode = "+userInfo.getNtranssitecode()+" and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by ntransactiontestcode,ntransactionsamplecode) and rt.nsitecode = "+userInfo.getNtranssitecode()+" and rth.nsitecode = "+userInfo.getNtranssitecode()+""
				+ " and rt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and rth.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " group by rth.ntransactionsamplecode)testcount "
				+ " where historycount.ntransactionsamplecode=testcount.ntransactionsamplecode "
				+ " and historycount.testcount=testcount.testcount; ";

		final String partialSampleQuery = "select historycount.ntransactionsamplecode from ("
				+ "        select rth.ntransactionsamplecode,count(ntesthistorycode) testcount from registrationtesthistory rth,registrationsamplehistory rh"
				+ "        where rth.ntransactionstatus=" + ntransStatus
				+ "     and rth.ntransactionsamplecode=rh.ntransactionsamplecode"
				+ "        and rh.nsamplehistorycode=any(select max(nsamplehistorycode) nreghistorycode"
				+ " from registrationsamplehistory where ntransactionsamplecode in (" + transactionSampleCode+ ") "
				+ " and nsitecode = "+userInfo.getNtranssitecode()+" and  nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by ntransactionsamplecode) and ntesthistorycode=any( "
				+ " select max(ntesthistorycode) ntesthistorycode from registrationtesthistory "
				+ " where ntransactionsamplecode in  (" + transactionSampleCode + ") and nsitecode = "+userInfo.getNtranssitecode()+" and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by ntransactionsamplecode,ntransactiontestcode" + " ) and rh.ntransactionstatus!="
				+ Enumeration.TransactionStatus.PARTIALLY.gettransactionstatus() + " and rth.nsitecode = "+userInfo.getNtranssitecode()+" "
				+ " and rh.nsitecode = "+userInfo.getNtranssitecode()+" "
				+ " and rth.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by rth.ntransactionsamplecode)historycount,"
				+ " (select rth.ntransactionsamplecode,count(ntesthistorycode) testcount "
				+ "        from registrationtesthistory rth,registrationtest rt "
				+ "        where ntransactionstatus not in ("
				+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ") "
				+ "        and rt.ntransactiontestcode=rth.ntransactiontestcode "
				+ "        and rth.ntesthistorycode = any( "
				+ "             select max(ntesthistorycode) ntesthistorycode from registrationtesthistory "
				+ "             where ntransactionsamplecode in (" + transactionSampleCode + ") "
				+ "             and nsitecode = "+userInfo.getNtranssitecode()+" and  nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "             group by ntransactiontestcode,ntransactionsamplecode"
				+ "        ) and rth.nsitecode = "+userInfo.getNtranssitecode()+" and rt.nsitecode = "+userInfo.getNtranssitecode()+""
				+ " and rth.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and rt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " group by rth.ntransactionsamplecode)testcount "
				+ " where historycount.ntransactionsamplecode = testcount.ntransactionsamplecode "
				+ " and historycount.testcount!=testcount.testcount ;";

		final List<RegistrationTestHistory> approvedSampleList = jdbcTemplate.query(approvedSamSampleQuery, new RegistrationTestHistory());
		final List<RegistrationTestHistory> partialSampleList = jdbcTemplate.query(partialSampleQuery, new RegistrationTestHistory());
		int seqNoRegistraionHistory = nreghistcode;

		final Set<String> strTransactionsamplecode = new LinkedHashSet<String>();

		if (!approvedSampleList.isEmpty()) {

			approvalPreregno = approvedSampleList.stream().map(objpreregno -> String.valueOf(objpreregno.getNtransactionsamplecode()))
					.collect(Collectors.joining(","));

			List<String> list = Arrays.asList(approvalPreregno.split(","));

			strTransactionsamplecode.addAll(list);  
			insertReghistory = "insert into registrationsamplehistory (nsamplehistorycode,npreregno,ntransactionsamplecode,ntransactionstatus,dtransactiondate,noffsetdtransactiondate,ntransdatetimezonecode,"
					+ "nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments,nsitecode,nstatus) " + "select "
					+ seqNoRegistraionHistory
					+ "+rank()over(order by ntransactionsamplecode) nsamplehistorycode,npreregno,ntransactionsamplecode,"
					+ ntransStatus + " ntransactionstatus," + "'" +dateUtilityFunction.getCurrentDateTime(userInfo)+ "'  dtransactiondate,"+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())+","+userInfo.getNtimezonecode()+","
					+ " " + userInfo.getNusercode() + " nusercode," + userInfo.getNuserrole()
					+ " nuserrolecode," + "" + userInfo.getNdeputyusercode() + " ndeputyusercode,"
					+ userInfo.getNdeputyuserrole() + " ndeputyuserrolecode,N'" + scomments + "' scomments, "+userInfo.getNtranssitecode()+" nsitecode,"
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
					+ "from registrationsample where ntransactionsamplecode in (" + approvalPreregno + ") and nsitecode = "+userInfo.getNtranssitecode()+" and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by ntransactionsamplecode;";

			seqNoRegistraionHistory = seqNoRegistraionHistory + approvedSampleList.size();

			joiner.add(approvalPreregno);
		}
		if (!partialSampleList.isEmpty()) {
			partialPreregno = partialSampleList.stream()
					.map(objpreregno -> String.valueOf(objpreregno.getNtransactionsamplecode()))
					.collect(Collectors.joining(","));

			List<String> list = Arrays.asList(partialPreregno.split(","));

			strTransactionsamplecode.addAll(list); 
			insertPartialReghistory = "insert into registrationsamplehistory (nsamplehistorycode,npreregno,ntransactionsamplecode,ntransactionstatus,dtransactiondate,noffsetdtransactiondate,ntransdatetimezonecode,"
					+ "nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments,nsitecode,nstatus) " + "select "
					+ seqNoRegistraionHistory
					+ "+rank()over(order by ntransactionsamplecode) nsamplehistorycode,npreregno,ntransactionsamplecode,"
					+ Enumeration.TransactionStatus.PARTIALLY.gettransactionstatus() + " ntransactionstatus," + "'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'  dtransactiondate,"+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())+","+userInfo.getNtimezonecode()+","
					+ " " + userInfo.getNusercode() + " nusercode," + userInfo.getNuserrole()
					+ " nuserrolecode," + "" + userInfo.getNdeputyusercode() + " ndeputyusercode,"
					+ userInfo.getNdeputyuserrole() + " ndeputyuserrolecode,N'" + scomments + "' scomments, "+userInfo.getNtranssitecode()+" nsitecode,"
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
					+ "from registrationsample where ntransactionsamplecode in (" + partialPreregno + ") and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "+userInfo.getNtranssitecode()+" "
					+ " order by ntransactionsamplecode;";

			joiner.add(partialPreregno);
			seqNoRegistraionHistory = seqNoRegistraionHistory + partialSampleList.size();
		}

		if (!approvedSampleList.isEmpty() || !partialSampleList.isEmpty()) {
			String result = strTransactionsamplecode.stream() 
					.collect(Collectors.joining(","));
			String getAuditQueryold = " select json_agg(a.jsonuidata) from (select r.jsonuidata||json_build_object('sarno',ra.sarno)::jsonb"
					+ " ||json_build_object('stransdisplaystatus',t.jsondata->'stransdisplaystatus'->>'"
					+ userInfo.getSlanguagetypecode() + "')::jsonb  as jsonuidata"
					+ " from registrationsample r,registrationsamplearno rsa,transactionstatus t ,registrationsamplehistory rsh,registrationarno ra"
					+ " where ra.npreregno=r.npreregno and rsa.npreregno=ra.npreregno and ra.npreregno=rsh.npreregno"
					+ " and ra.npreregno=r.npreregno and r.npreregno=rsh.npreregno and r.npreregno=rsa.npreregno"
					+ " and r.ntransactionsamplecode=rsh.ntransactionsamplecode "
					+ " and r.ntransactionsamplecode=rsa.ntransactionsamplecode"
					+ " and rsa.ntransactionsamplecode=rsh.ntransactionsamplecode"
					+ " and rsh.ntransactionstatus=t.ntranscode" + " "
					+ " and r.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and ra.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and rsh.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" "
					+ " and rsa.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and t.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and rsh.nsamplehistorycode in (select max(nsamplehistorycode) from registrationsamplehistory rsh1"
					+ " where rsh1.ntransactionsamplecode in (" + result + ") and rsh1.nsitecode = "+userInfo.getNtranssitecode()+" "
					+ " and rsh1.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" group by ntransactionsamplecode) and"
					+ " rsh.nsitecode = "+userInfo.getNtranssitecode()+" and rsa.nsitecode = "+userInfo.getNtranssitecode()+" "
					+ " and r.nsitecode = "+userInfo.getNtranssitecode()+" and ra.nsitecode = "+userInfo.getNtranssitecode()+" "
					+ " order by r.ntransactionsamplecode)a";

			JSONArray jsonArrayold = new JSONArray(jdbcTemplate.queryForObject(getAuditQueryold, String.class)); 

			jdbcTemplate.execute(insertReghistory);
			jdbcTemplate.execute(insertPartialReghistory);

			final String spreregno = joiner.toString();

			final String getAuditQuery = " select json_agg(a.jsonuidata) from (select r.jsonuidata||json_build_object('sarno',ra.sarno)::jsonb"
					+ " ||json_build_object('stransdisplaystatus',t.jsondata->'stransdisplaystatus'->>'"
					+ userInfo.getSlanguagetypecode() + "')::jsonb  as jsonuidata"
					+ " from registrationsample r,registrationsamplearno rsa,transactionstatus t"
					+ " ,registrationsamplehistory rsh,registrationarno ra"
					+ " where ra.npreregno=r.npreregno and rsa.npreregno=ra.npreregno and ra.npreregno=rsh.npreregno"
					+ " and ra.npreregno=r.npreregno and r.npreregno=rsh.npreregno and r.npreregno=rsa.npreregno"
					+ " and r.ntransactionsamplecode=rsh.ntransactionsamplecode "
					+ " and r.ntransactionsamplecode=rsa.ntransactionsamplecode"
					+ " and rsa.ntransactionsamplecode=rsh.ntransactionsamplecode"
					+ " and rsh.ntransactionstatus=t.ntranscode" + " "
					+ " and r.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and ra.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and rsh.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" "
					+ " and rsa.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" "
					+ " and t.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" " 
					+ " and rsh.nsamplehistorycode in (select max(nsamplehistorycode) from registrationsamplehistory rsh1"
					+ " where rsh1.ntransactionsamplecode in (" + result + ") and rsh1.nsitecode = "+userInfo.getNtranssitecode()+" "
					+ " and rsh1.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" group by ntransactionsamplecode) and"
					+ " rsh.nsitecode = "+userInfo.getNtranssitecode()+" and rsa.nsitecode = "+userInfo.getNtranssitecode()+" and r.nsitecode = "+userInfo.getNtranssitecode()+" "
					+ " and ra.nsitecode = "+userInfo.getNtranssitecode()+" order by r.ntransactionsamplecode)a";

			JSONArray jsonArray = new JSONArray(jdbcTemplate.queryForObject(getAuditQuery, String.class));

			List<Object> insertAuditList = new ArrayList<>();
			List<String> multiLingualActionList = new ArrayList<>();
			auditOldData.put("registrationsample", jsonArrayold);
			auditNewData.put("registrationsample", jsonArray);
			auditActionType.put("registrationsample", "IDS_COMPLETESUBSAMPLE");

			auditUtilityFunction.fnJsonArrayAudit(auditOldData, auditNewData, auditActionType, inputMap, false, userInfo);

		}
		rtnMap.put("nreghistcode", seqNoRegistraionHistory);
		return rtnMap;
	}

	@Override
	public ResponseEntity<Object> getResultChangeHistory(final String ntransactiontestcode, UserInfo userInfo) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		Map<String, Object> returnMap = new HashMap<>();

		final String getApprovalParamQry = "select rch.nresultchangehistorycode,rt.ntransactiontestcode,rch.ntransactionresultcode,rar.sarno,sar.ssamplearno,"
				//				+ " tgt.stestsynonym +' ['+convert(nvarchar(10),rt.ntestrepeatno)+']['+convert(nvarchar(10),rt.ntestretestno)+']' stestsynonym,"
				+ " rt.jsondata->>'stestsynonym' as stestsynonym,"
				+ " tgp.sparametersynonym,(rch.jsondata->>'sresult')::character varying||' '||case when rp.nunitcode=-1 then '' else"
				+ " case when rch.nparametertypecode="+Enumeration.ParameterType.CHARACTER.getparametertype()+" then '' else rch.jsondata->>'sunitname' end end as sresult,"
				+ " (rch.jsondata->>'sfinal')::character varying || ' ' || case when rp.nunitcode=-1 then '' else "
				+ " case when rch.nparametertypecode="+Enumeration.ParameterType.CHARACTER.getparametertype()+" then '' else rch.jsondata->>'sunitname' end end as sfinal,"
				//				+ " rp.smina,rp.smaxa,rp.sminb,rp.smaxb,rp.sminlod,rp.smaxlod,rp.sminloq,rp.smaxloq,"
				+ " u.sfirstname ||' ' ||u.slastname as username, to_char((rch.jsondata->>'dentereddate')::timestamp,'"
				+ userInfo.getSpgdatetimeformat()
				+ "') sentereddate,ur.suserrolename, ut.sunitname ,rch.nformcode, qf.sformname,rch.jsondata ->> 'sresultaccuracyname' as sresultaccuracyname "
				+ " from resultchangehistory rch,registrationtest rt,resultparameter rp,"
				+ " registrationarno rar,registrationsamplearno sar,testgrouptestparameter tgp,users u,userrole ur, unit ut"
				+ " , qualisforms qf "
				+ " where rt.ntransactiontestcode=rch.ntransactiontestcode and tgp.ntestgrouptestparametercode = rch.ntestgrouptestparametercode "
				+ " and rar.npreregno=rt.npreregno and rp.ntransactionresultcode=rch.ntransactionresultcode"
				+ " and sar.ntransactionsamplecode=rt.ntransactionsamplecode and u.nusercode=rch.nenteredby"
				+ " and ur.nuserrolecode=rch.nenteredrole and ut.nunitcode = rp.nunitcode"
				+ " and rch.nformcode = qf.nformcode and ut.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rch.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rar.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sar.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and qf.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rch.nsitecode = "+userInfo.getNtranssitecode()+" "
				+ " and rch.nsitecode = "+userInfo.getNtranssitecode()+" "
				+ " and rp.nsitecode = "+userInfo.getNtranssitecode()+" "
				+ " and rt.nsitecode = "+userInfo.getNtranssitecode()+" "
				+ " and sar.nsitecode = "+userInfo.getNtranssitecode()+" "
				+ " and rar.nsitecode = "+userInfo.getNtranssitecode()+" "
				+ " and tgp.nsitecode = "+userInfo.getNmastersitecode()+" "
				+ " and u.nsitecode = "+userInfo.getNmastersitecode()+" "
				+ " and ur.nsitecode = "+userInfo.getNmastersitecode()+" "
				+ " and rch.ntransactiontestcode in("+ ntransactiontestcode + ")";

		List<ResultChangeHistory> approvalParameterList = jdbcTemplate.query(getApprovalParamQry,new ResultChangeHistory());

		List<?> lstUTCConvertedDate = dateUtilityFunction.getSiteLocalTimeFromUTC(approvalParameterList,
				Arrays.asList("sentereddate"), null, userInfo, false, null, false);
		approvalParameterList = objMapper.convertValue(lstUTCConvertedDate,	new TypeReference<List<ResultChangeHistory>>() {});

		returnMap.put("ResultChangeHistory", approvalParameterList);

		return new ResponseEntity<>(returnMap, HttpStatus.OK);

	}


	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> updateDefaultValue(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Map<String, Object> returnMap = new HashMap<>();

		JSONObject auditOldData = new JSONObject();
		JSONObject auditNewData = new JSONObject();
		JSONObject auditActionType = new JSONObject();
		List<ResultParameter> lstdata = new ArrayList<>();
		List<ResultParameter> lstdataold = new ArrayList<>();

		List<ResultParameter> lstaudit = new ArrayList<>();

		List<Integer> lstntransactionresultcode = new ArrayList<>();
		String ntransactiontestcode = (String) inputMap.get("ntransactiontestcode");
		String ntestgrouptestcode = (String) inputMap.get("ntestgrouptestcode");
		String npreregno = (String) inputMap.get("npreregno");
		String sresultcode = "";
		String schangeresult = "";
		ObjectMapper objMapper = new ObjectMapper();
		List<String> lstntransactiontestcode = Arrays
				.asList(((String) inputMap.get("ntransactiontestcode")).split(","));

		List<RegistrationTestHistory> lstvalidation = validateTestStatus(ntransactiontestcode,
				(int) inputMap.get("ncontrolcode"), userInfo,(int) inputMap.get("nneedReceivedInLab"));

		if (!lstvalidation.isEmpty()) {
			if (lstntransactiontestcode.size() == lstvalidation.size()) {

				String queryTest =" select   tgtnp.ntransactionresultcode,tgtnp.ntransactiontestcode,tgtnp.npreregno,tgtnp.nparametertypecode,(tgtnp.jsondata->>'nroundingdigits')::int nroundingdigit,cast(case when tgtnp.jsondata ->> 'sminb'='' then null else tgtnp.jsondata ->> 'sminb' end  as float) sminb,cast(case when tgtnp.jsondata ->> 'smina'='' then null else tgtnp.jsondata ->> 'smina' end as float) smina, cast(case when tgtnp.jsondata ->> 'smaxb'='' then null else tgtnp.jsondata ->> 'smaxb' end as float) smaxb, " + 
						" cast(case when tgtnp.jsondata ->> 'smaxa'='' then null else tgtnp.jsondata ->> 'smaxa' end as float) smaxa,"
						//+ " tgtp.nparametertypecode,cast(tgtnp.sminb as float) sminb,cast(tgtnp.smina as float) smina,cast(tgtnp.smaxb as float) smaxb,cast(tgtnp.smaxa as float) smaxa, "
						+ " tgtnpr.ngradecode as numericgradecode,tgtpp.ngradecode, " + "case when tgtp.nparametertypecode="
						+ Enumeration.ParameterType.NUMERIC.getparametertype() + " then  case when tgtnp.jsondata->> 'sresultvalue'<> 'null' "
						+ " then tgtnp.jsondata->> 'sresultvalue' else "
						+ " tgtnpr.sresultvalue end "
						+ "when tgtp.nparametertypecode=" + Enumeration.ParameterType.PREDEFINED.getparametertype() + " "
						+ "then tgtpp.spredefinedname else tgtcp.scharname end sresult,"
						+ "case when tgtp.nparametertypecode=" + Enumeration.ParameterType.NUMERIC.getparametertype() + " "
						+ "then cast(round(cast(tgtnpr.sresultvalue as decimal),tgtp.nroundingdigits) as character varying(50)) when tgtp.nparametertypecode="
						+ Enumeration.ParameterType.PREDEFINED.getparametertype() + " "
						+ "then tgtpp.spredefinedname else tgtcp.scharname end sfinal,tgtp.nroundingdigits,tgt.ntestgrouptestcode,tgtp.ntestgrouptestparametercode, "
						//+ "case when tgtnp.nparametertypecode = 1 then tgtnpr.ngradecode " 
						+ "case when tgtnp.nparametertypecode = "+Enumeration.ParameterType.NUMERIC.getparametertype()+" then (tgtnp.jsondata->>'ngradecode')::int "						
						+"when tgtnp.nparametertypecode =" + Enumeration.ParameterType.PREDEFINED.getparametertype()
						+ " then tgtpp.ngradecode " + "when tgtnp.nparametertypecode ="
						+ Enumeration.ParameterType.CHARACTER.getparametertype() + " then "+Enumeration.Grade.FIO.getGrade()+" end ngradecodenew ,tgtp.nresultaccuracycode ,ra.sresultaccuracyname " 	
						+ " from  registrationtest rt,registrationtesthistory rth, resultaccuracy ra, resultparameter tgtnp,testgrouptest tgt "
						+ " inner join testgrouptestparameter tgtp on tgtp.ntestgrouptestcode = tgt.ntestgrouptestcode and tgtp.nsitecode = "+userInfo.getNmastersitecode()+" and tgtp.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
						+ " left outer join testgrouptestcharparameter tgtcp on tgtcp.ntestgrouptestparametercode = tgtp.ntestgrouptestparametercode and tgtcp.nsitecode = "+userInfo.getNmastersitecode()+" and tgtcp.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
						+ " left outer join testgrouptestnumericparameter tgtnpr on tgtnpr.ntestgrouptestparametercode = tgtp.ntestgrouptestparametercode and tgtnpr.nsitecode = "+userInfo.getNmastersitecode()+" and tgtnpr.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
						+ " left outer join testgrouptestpredefparameter tgtpp on tgtpp.ntestgrouptestparametercode=tgtp.ntestgrouptestparametercode and tgtpp.nsitecode = "+userInfo.getNmastersitecode()+" and tgtpp.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						+ " and tgtpp.ndefaultstatus=" + Enumeration.TransactionStatus.YES.gettransactionstatus()
						+ " where ra.nresultaccuracycode=tgtp.nresultaccuracycode and "
						+ " tgtnp.ntestgrouptestparametercode = tgtp.ntestgrouptestparametercode and tgt.ntestgrouptestcode in (select ntestgrouptestcode from registrationtest rt "
						+ " where npreregno in (" + npreregno + ") and ntestgrouptestcode in (" + ntestgrouptestcode+ ") and nsitecode = "+userInfo.getNtranssitecode()+" "
						+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+") "
						//ALPD-5465--Added by vignesh R(10-03-2025)--Result Entry --> Without Entering the result in Enter result it's Already Filled instead of Empty.
						//start
						+ "  and  rt.ntransactiontestcode=tgtnp.ntransactiontestcode"
						+ "  and rth.ntransactionstatus="+lstvalidation.get(0).getNtransactionstatus()+" "
						+ "  and  rth.ntesthistorycode = any(select max(ntesthistorycode) from registrationtesthistory where npreregno in ("
						+ " "+npreregno+") and nsitecode = "+userInfo.getNtranssitecode()+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" group by ntransactiontestcode)"
						+ " and  rt.nsitecode = "+userInfo.getNtranssitecode()+" and rt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  and rth.ntransactiontestcode=rt.ntransactiontestcode   "
						+ " and   tgtnp.npreregno in (" + npreregno + ")  and tgtnp.nsitecode = "+userInfo.getNtranssitecode()
						//end
						+ "  and tgtnp.npreregno in (" + npreregno + ")  "
						+"  AND tgtnp.nenforceresult <> "+Enumeration.TransactionStatus.YES.gettransactionstatus()+" "
						+ " and rt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  "
						+ " and rth.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  "
						+ " and ra.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  "
						+ " and tgtnp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  "
						+ " and tgt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						+ " and rt.nsitecode = "+userInfo.getNtranssitecode()+" "
						+ " and rth.nsitecode = "+userInfo.getNtranssitecode()+" "
						+ " and ra.nsitecode = "+userInfo.getNmastersitecode()+" "
						+ " and tgtnp.nsitecode = "+userInfo.getNtranssitecode()+" "
						+ " and tgt.nsitecode = "+userInfo.getNmastersitecode()+" "
						+ " and case when tgtp.nparametertypecode=" + Enumeration.ParameterType.NUMERIC.getparametertype()
						+ " then  case when tgtnp.jsondata ->> 'sresultvalue'='' then null else tgtnp.jsondata ->> 'sresultvalue' end when tgtp.nparametertypecode="
						+ Enumeration.ParameterType.PREDEFINED.getparametertype()
						+ " then tgtpp.spredefinedname else tgtcp.scharname end is not null "						
						+ " and case when tgtp.nparametertypecode = "+Enumeration.ParameterType.NUMERIC.getparametertype()+" then tgtnp.jsondata ->> 'sresultvalue' " + 
						" when tgtp.nparametertypecode = "+Enumeration.ParameterType.PREDEFINED.getparametertype()+" then tgtpp.spredefinedname else tgtcp.scharname end <> 'null' ";

				List<ResultParameter> lstGetDefaultValue1 = jdbcTemplate.query(queryTest, new ResultParameter());

				//ALPD-5467--Vignesh R(20-02-2025)--Result was chaned when choose the default result
				//ALPD-5467--Start
				final String strNotNullParameters = "select a.* from (select ntransactionresultcode, jsondata->>'sresult' sresult from resultparameter where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and npreregno in ("+ npreregno+") and nsitecode="+ userInfo.getNtranssitecode()
						+ " ) a where a.sresult is not null and a.sresult != '-' and a.sresult != ''";
				List<Map<String,Object>> lstNotNullParameters = jdbcTemplate.queryForList(strNotNullParameters);

				List<ResultParameter> lstGetDefaultValue = lstGetDefaultValue1.stream()
						.filter(test -> lstNotNullParameters.stream().noneMatch(
								nvt -> test.getNtransactionresultcode() == Integer.parseInt(nvt.get("ntransactionresultcode").toString())))
						.collect(Collectors.toList());

				//ALPD-5467--end

				final String queryResult = "select rp.jsondata->>'sresult' as sresult,rp.jsondata->>'sfinal' as sfinal,rp.ngradecode,rp.nunitcode,"
						+ " rp.ntestparametercode,rp.ntransactionstatus,rt.ntestgrouptestcode,rt.npreregno,rp.ntransactionresultcode,"
						+ " rp.ntestgrouptestparametercode,rt.ntransactiontestcode,rp.jsondata->>'nroundingdigits' as nroundingdigits,"
						+ " rp.nparametertypecode,rp.jsondata "
						+ " from resultparameter rp,registrationtest rt where rp.ntransactiontestcode=rt.ntransactiontestcode "
						+ " and rp.nsitecode = "+userInfo.getNtranssitecode()+" and rt.nsitecode = "+userInfo.getNtranssitecode()+" "
						+ " and rp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						+ " and rt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						+ " and rt.npreregno in ("+npreregno+") "
						+ " and rt.ntestgrouptestcode in ("+ntestgrouptestcode+") "
						+ " and rt.ntransactiontestcode in ("+ntransactiontestcode+")  ";
				List<ResultParameter> lstGetResult = jdbcTemplate.query(queryResult, new ResultParameter());

				int nresultchangehistoryno = (int) inputMap.get("nresultchangehistoryno");

				if (lstGetResult != null && lstGetDefaultValue != null && !lstGetResult.isEmpty()
						&& !lstGetDefaultValue.isEmpty()) {

					for (ResultParameter objDefaultValue : lstGetDefaultValue) {
						if(objDefaultValue.getSresult() != null) {
							String queryUpdate = null;
							final Map<String, Object> jsonMap = new HashMap<>();
							jsonMap.put("sresult", objDefaultValue.getSresult());
							jsonMap.put("sfinal", objDefaultValue.getSfinal());
							jsonMap.put("nresultaccuracycode", objDefaultValue.getNresultaccuracycode());
							jsonMap.put("sresultaccuracyname", objDefaultValue.getSresultaccuracyname());
							jsonMap.put("dentereddate",dateUtilityFunction.getCurrentDateTime(userInfo) + "");

							queryUpdate = "update resultparameter set " + " ngradecode="
									+ objDefaultValue.getNgradecodenew() + "," + " nenteredby = "
									+ userInfo.getNusercode() + "," + " nenteredrole = "
									+ userInfo.getNuserrole() + "," + " ndeputyenteredrole = "
									+ userInfo.getNdeputyuserrole() + "," + " ndeputyenteredby = "
									+ userInfo.getNdeputyusercode() + ", " + " jsondata = jsondata || '"
									+ objMapper.writeValueAsString(jsonMap) + "'::jsonb"
									+ " where ntestgrouptestparametercode="
									+ objDefaultValue.getNtestgrouptestparametercode() + " and npreregno="
									+ objDefaultValue.getNpreregno() + " and ntransactiontestcode="
									+ objDefaultValue.getNtransactiontestcode() + " and nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

							List<ResultParameter> lstSingleResults = lstGetResult.stream()
									.filter(objjqresultparam -> objjqresultparam
											.getNtransactionresultcode() == objDefaultValue.getNtransactionresultcode())
									.collect(Collectors.toList());

							if (!lstSingleResults.isEmpty() && lstSingleResults.get(0).getSresult() != null && !lstSingleResults.get(0).getSresult().isEmpty()
									&& !(lstSingleResults.get(0).getSresult()
											.equalsIgnoreCase(objDefaultValue.getSresult()))) {

								jdbcTemplate.execute(
										"insert into resultchangehistory (nresultchangehistorycode,nformcode,ntransactionresultcode,ntransactiontestcode,npreregno,"
												+ "ngradecode,ntestgrouptestparametercode,nparametertypecode,nenforceresult,nenforcestatus,"
												+ "nenteredby,nenteredrole,ndeputyenteredby,ndeputyenteredrole,jsondata,nsitecode,nstatus) "
												+ "select " + nresultchangehistoryno
												+ "+RANK()over(order by ntransactionresultcode) nresultchangehistorycode,"
												+ userInfo.getNformcode()
												+ ",ntransactionresultcode,ntransactiontestcode,npreregno,"
												+ "ngradecode,ntestgrouptestparametercode,"
												+ lstSingleResults.get(0).getNparametertypecode()
												+ " nparametertypecode," + ""
												+ Enumeration.TransactionStatus.YES.gettransactionstatus()
												+ " nenforceresult,"
												+ Enumeration.TransactionStatus.NO.gettransactionstatus()
												+ " nenforcestatus," + userInfo.getNusercode() + " nenteredby,"
												+ userInfo.getNuserrole() + " nenteredrole," + ""
												+ userInfo.getNdeputyusercode() + " ndeputyenteredby,"
												+ userInfo.getNdeputyuserrole() + " ndeputyenteredrole,'{"
												+ "\"sresult\":\""
												+ stringUtilityFunction.replaceQuote(lstSingleResults.get(0).getSresult()) + "\","
												+ "\"sfinal\":\""
												+ stringUtilityFunction.replaceQuote((String) lstSingleResults.get(0).getJsondata()
														.get("sfinal"))
												+ "\"," + "\"dentereddate\":\"" +dateUtilityFunction.getCurrentDateTime(userInfo)
												+ "\"" + "}'::jsonb,"+userInfo.getNtranssitecode()+","
												+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
												+ " nstatus " + "from resultparameter where nsitecode = "+userInfo.getNtranssitecode()+" and nstatus = "
												+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
												+ " and ntransactionresultcode = "
												+ objDefaultValue.getNtransactionresultcode() + ";");
								nresultchangehistoryno++;
								sresultcode = sresultcode + "," + objDefaultValue.getNtransactionresultcode();
								schangeresult = schangeresult + "," + nresultchangehistoryno;
							}
							lstntransactionresultcode.add(objDefaultValue.getNtransactionresultcode());
							jdbcTemplate.execute(queryUpdate);

						}	
					}
					List<ResultParameter> lstGetResultafter = jdbcTemplate.query(queryResult,new ResultParameter());

					List<Object> lstaftersave = new ArrayList<>();

					lstaftersave.add(lstGetResultafter);
					Map<String, Object> objMap = new HashMap<>();
					objMap.put("nregtypecode", (int) inputMap.get("nregtypecode"));
					objMap.put("nregsubtypecode", (int) inputMap.get("nregsubtypecode"));
					objMap.put("ndesigntemplatemappingcode", (int) inputMap.get("ndesigntemplatemappingcode"));

					auditActionType.put("resultparameter", "IDS_EDITRESULTPARAMETER");

					returnMap.putAll((Map<String, Object>) getTestbasedParameter(ntransactiontestcode, userInfo).getBody());

					lstdata = (List<ResultParameter>) returnMap.get("RegistrationParameter");
					lstaudit.addAll((List<ResultParameter>) (lstdata.stream().filter(
							t -> lstntransactionresultcode.contains(((ResultParameter) t).getNtransactionresultcode()))
							.collect(Collectors.toList())));
					auditOldData.put("resultparameter", lstaudit);
					auditUtilityFunction.fnJsonArrayAudit(auditOldData, null, auditActionType, inputMap, false, userInfo);

					return new ResponseEntity<>(returnMap, HttpStatus.OK);
				} else {				
					//ALPD-5467--start
					if(lstGetDefaultValue1 != null && !lstGetDefaultValue1.isEmpty()) {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_DEFAULTVALUEALREADYFILLED",
								userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					} else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NODEFAULTVALUE",
								userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
					//ALPD-5467--end

				}
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SELECTVALIDTEST", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTVALIDTEST", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}

	@Override
	public ResponseEntity<Object> getParameterComments(final int ntransactionresultcode,
			final String ntransactiontestcode, final int controlCode, final UserInfo userInfo,final int nneedReceivedInLab) throws Exception {
		Map<String, Object> returnMap = new HashMap<>();

		List<RegistrationTestHistory> lstvalidationtest = validateTestStatus(ntransactiontestcode, controlCode,
				userInfo,nneedReceivedInLab);

		if (!lstvalidationtest.isEmpty()) {
			String str = "  select rp.ntransactiontestcode,rp.ntransactionresultcode,coalesce(rpc.jsondata->>'sresultcomment','') sresultcomment"
					+ " from resultparametercomments rpc,resultparameter rp " + " where rpc.ntransactionresultcode="
					+ ntransactionresultcode + " and rp.ntransactionresultcode=rpc.ntransactionresultcode "
					+ " and rp.nsitecode = "+userInfo.getNtranssitecode()+" "
					+ " and rpc.nsitecode = "+userInfo.getNtranssitecode()+" "
					+ " and rp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and rpc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			ResultParameterComments objresultparameter = jdbcTemplate.queryForObject(str,new ResultParameterComments());

			returnMap.put("ParameterComments", objresultparameter);
			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTVALIDTEST", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> updateParameterComments(final int ntransactionresultcode,
			final String ntransactiontestcode, final String sresultcomments, int controlCode, final UserInfo userInfo,
			int nregtypecode, int nregsubtypecode, final int ndesigntemplatemappingcode,final int nneedReceivedInLab) throws Exception {

		JSONObject auditOldData = new JSONObject();
		JSONObject auditNewData = new JSONObject();
		JSONObject auditActionType = new JSONObject();
		List<ResultParameter> lstdata = new ArrayList<>();
		List<ResultParameter> lstdataold = new ArrayList<>();
		List<ResultParameter> lstaudit = new ArrayList<>();

		final Map<String, Object> returnMap = new HashMap<>();
		final List<RegistrationTestHistory> lstvalidationtest = validateTestStatus(ntransactiontestcode, controlCode,
				userInfo,nneedReceivedInLab);

		if (!lstvalidationtest.isEmpty()) {

			String str = "select * from resultparametercomments  where  "
					+ " nsitecode = "+userInfo.getNtranssitecode()+" and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ntransactionresultcode=" + ntransactionresultcode + ";";
			jdbcTemplate.execute(str);

			str = "select * from resultparametercomments where "
					+ " nsitecode = "+userInfo.getNtranssitecode()+" "
					+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and ntransactionresultcode=" + ntransactionresultcode+";";

			List<ResultParameterComments> beforeUpdate = jdbcTemplate.query(str, new ResultParameterComments());

			returnMap.putAll((Map<String, Object>) getTestbasedParameter(ntransactiontestcode, userInfo).getBody());
			lstdataold = (List<ResultParameter>) returnMap.get("RegistrationParameter");
			lstaudit.add((ResultParameter) (lstdataold.stream()
					.filter(t -> ((ResultParameter) t).getNtransactionresultcode() == ntransactionresultcode).findAny())
					.get());
			auditOldData.put("resultparametercomments", lstaudit);
			returnMap.clear();
			lstaudit.clear();

			str = "update resultparametercomments set jsondata = jsondata || jsonb_build_object('sresultcomment','"+stringUtilityFunction.replaceQuote(sresultcomments) +"')::jsonb "
					+ "  where nsitecode = "+userInfo.getNtranssitecode()+" and nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and ntransactionresultcode = "+ ntransactionresultcode + ";";

			jdbcTemplate.execute(str);

			str = "select * from resultparametercomments where ntransactionresultcode=" + ntransactionresultcode
					+ " and nsitecode ="+userInfo.getNtranssitecode()+" and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			List<ResultParameterComments> afterUpdate = jdbcTemplate.query(str, new ResultParameterComments());

			List<Object> lstObjbefore = new ArrayList<>();
			List<Object> lstObjafter = new ArrayList<>();
			lstObjafter.add(afterUpdate);
			lstObjbefore.add(beforeUpdate);
			Map<String, Object> objMap = new HashMap<>();
			objMap.put("nregtypecode", nregtypecode);
			objMap.put("nregsubtypecode", nregsubtypecode);
			objMap.put("ndesigntemplatemappingcode", ndesigntemplatemappingcode);

			returnMap.putAll((Map<String, Object>) getTestbasedParameter(ntransactiontestcode, userInfo).getBody());
			returnMap.put("selectedId", ntransactionresultcode);

			lstdata = (List<ResultParameter>) returnMap.get("RegistrationParameter");
			lstaudit.add((ResultParameter) (lstdata.stream().filter(t -> ((ResultParameter) t).getNtransactionresultcode() == ntransactionresultcode).findAny()).get());

			auditNewData.put("resultparametercomments", lstaudit);
			auditActionType.put("resultparametercomments", "IDS_EDITRESULTPARAMETERCOMMENTS");
			auditUtilityFunction.fnJsonArrayAudit(auditOldData, auditNewData, auditActionType, objMap, false, userInfo);

			return getTestbasedParameter(ntransactiontestcode, userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTVALIDTEST", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> getFormulaInputs(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		List<DynamicFormulaFields> lst = new ArrayList<>();
		String sformula = null;

		int nformulacode = (int) inputMap.get("nformulacode");
		int ntransactiontestcode = (int) inputMap.get("ntransactiontestcode");
		int ntransactionsamplecode = (int) inputMap.get("ntransactionsamplecode");
		int npreregno = (int) inputMap.get("npreregno");
		String strQuery = "select * from testgrouptestformula where ntestgrouptestformulacode=" + nformulacode
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		TestGroupTestFormula objFormula = (TestGroupTestFormula) jdbcUtilityFunction.queryForObject(strQuery, TestGroupTestFormula.class,jdbcTemplate);

		if (objFormula !=null) {
			sformula = objFormula.getSformulacalculationcode();
			String[] split = objFormula.getSformulacalculationdetail().split("(\\|\\|)|(&&)|(==)|(!=)|(<=)|(>=)|[\\+\\-\\*/%<>]");
			String s = sformula;

			while (sformula.contains("$P")) {
				int index = sformula.indexOf("$P");
				int lastIndex = sformula.indexOf("P$");
				String actualText = sformula.substring(index + 2, lastIndex);
				int ntestparametercode = Integer.parseInt(actualText);
				if (ntestparametercode > 0) {
					String strQuery13 = "select * from testparameter where ntestparametercode=" + ntestparametercode
							+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
					TestParameter objtestparameter = (TestParameter) jdbcUtilityFunction.queryForObject(strQuery13, TestParameter.class,jdbcTemplate);

					strQuery13 = "select a.ntestparametercode,max(a.ntransactionsamplecode) ntransactionsamplecode," + 
							" max(a.ntransactiontestcode) ntransactiontestcode,max(a.ntransactionresultcode) ntransactionresultcode, " + 
							" max(rp1.jsondata->>'sresult')::DECIMAL as sresult, max(a.ntestretestno)ntestretestno," + 
							" max(a.ntestrepeatno)ntestrepeatno, " + 
							//"case when (max(a.ntestretestno) > 0 OR max(a.ntestrepeatno) >1) then 1 else 0 end as nisaverageneed " + 
							" count(a.ntransactionresultcode) nisaverageneed from (select rp.ntestparametercode ,max(rt.ntransactionsamplecode) ntransactionsamplecode," + 
							"	  max(rp.ntransactiontestcode) ntransactiontestcode," + 
							"	  max(rp.ntransactionresultcode) ntransactionresultcode, rt.ntestrepeatno,rt.ntestretestno," + 
							"	  COALESCE(max(rp.jsondata->>'sresult')::decimal,0) sresult  " + 
							"	  from resultparameter rp,registrationtest rt,registrationtesthistory rth  " + 
							"	  where " + 
							"	  rth.ntesthistorycode = (select max(ntesthistorycode) from registrationtesthistory rth1 where rth1.ntransactiontestcode = rt.ntransactiontestcode" + 
							"							  and rth1.ntransactionsamplecode = rt.ntransactionsamplecode" + 
							"							  group by rth1.ntransactiontestcode,rth1.ntransactionsamplecode,rth1.npreregno)" + 
							"	  and rp.ntransactiontestcode = rt.ntransactiontestcode " + 
							"	  and rt.ntransactionsamplecode = "+ntransactionsamplecode+"  " + 
							"	  and rp.ntestparametercode = "+ntestparametercode+"" + 
							"	  and rp.jsondata->>'sresult' != ''" + 
							"	  and rth.ntransactionstatus not in ("+Enumeration.TransactionStatus.REJECTED.gettransactionstatus()+","
							+ ""+Enumeration.TransactionStatus.CANCELED.gettransactionstatus()+","+Enumeration.TransactionStatus.RETEST.gettransactionstatus()+")" + 
							"	  and rp.ntransactionstatus not in ("+Enumeration.TransactionStatus.REJECTED.gettransactionstatus()+")" + 
							"	  group by rt.ntestrepeatno,rt.ntestretestno,rp.ntestparametercode)a," + 
							"	  resultparameter rp1 where rp1.ntransactionresultcode = a.ntransactionresultcode " + 
							"	  group by a.ntestparametercode";


					ResultParameter objparameter = (ResultParameter) jdbcUtilityFunction.queryForObject(strQuery13,  ResultParameter.class,jdbcTemplate);

					if (objtestparameter != null) {

						DynamicFormulaFields objform = new DynamicFormulaFields();
						objform.setNdynamicformulafieldcode(ntestparametercode);
						String str = "$P" + ntestparametercode + "P$";
						objform.setSdescription(str);
						objform.setSdynamicfieldname(objtestparameter.getSparametername());
						objform.setNtransactionresultcode(objparameter == null ? -1 :objparameter.getNtransactionresultcode());
						objform.setNtransactiontestcode(objparameter == null ? -1 :objparameter.getNtransactiontestcode());
						objform.setSvalue(objparameter == null ? "" : objparameter.getSresult());
						objform.setNisaverageneed(objparameter == null ? 0 : objparameter.getNisaverageneed());
						objform.setNtransactionsamplecode(objparameter == null ? 0 : objparameter.getNtransactionsamplecode());
						objform.setNtestparametercode(ntestparametercode); //Added the code by sonia on 16th july 2025 for jira id:ALPD-6045
						sformula = stringUtilityFunction.replaceofString(sformula, str, "");
						lst.add(objform);
					}
				}
			}

			while (s.contains("$D")) {
				int index = s.indexOf("$D");
				int lastIndex = s.indexOf("D$");
				String actualText = s.substring(index + 2, lastIndex);
				int parametercode = Integer.parseInt(actualText);
				if (actualText != null) {
					String strQuery12 = "select * from dynamicformulafields where ndynamicformulafieldcode="
							+ parametercode + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

					DynamicFormulaFields objdynamic = (DynamicFormulaFields) jdbcUtilityFunction.queryForObject(strQuery12,DynamicFormulaFields.class,jdbcTemplate);

					if (objdynamic != null) {
						DynamicFormulaFields objform = new DynamicFormulaFields();
						objform.setNdynamicformulafieldcode(parametercode);
						String str = "$D" + parametercode + "D$";
						objform.setSdescription(str);
						objform.setSdynamicfieldname(objdynamic.getSdatatype());
						StringBuilder sb = new StringBuilder(s);
						sb.replace(index, lastIndex + 2, "");
						s = sb.toString();
						lst.add(objform);
					}
				}
			}

			returnMap.put("DynamicFormulaFields", lst);
			returnMap.put("query", objFormula.getSformulacalculationcode());
			returnMap.put("Formula", objFormula.getSformulacalculationdetail());
		}
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getResultUsedInstrument(final String ntransactiontestcode,final int nresultusedinstrumentcode, final UserInfo userInfo) throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		ObjectMapper objMapper =  new ObjectMapper();
		String conditionsstr = "";
		String keyvalue = "";

		if (ntransactiontestcode.split(",").length > 0 && !ntransactiontestcode.equals("")) {
			conditionsstr = " and rui.ntransactiontestcode in(" + ntransactiontestcode + ")";
			keyvalue = "ResultUsedInstrument";
		} else if (nresultusedinstrumentcode > 0) {
			conditionsstr = " and rui.nresultusedinstrumentcode=(" + nresultusedinstrumentcode + ")";
			keyvalue = "EditResultUsedInstrument";
		}
		final String getinstrument = "select rar.sarno,sar.ssamplearno,rui.npreregno,rui.ntransactiontestcode, "
				+ " rt.jsondata->>'stestsynonym' stestsynonym,rui.nresultusedinstrumentcode,ic.sinstrumentcatname,i.sinstrumentname,i.sinstrumentid,"
				+ " i.ninstrumentcode,i.ninstrumentnamecode,ic.ninstrumentcatcode,ic.ncalibrationreq, to_char(rui.dfromdate, '"
				+ userInfo.getSpgdatetimeformat() + "') as sfromdate,to_char(rui.dtodate, '"
				+ userInfo.getSpgdatetimeformat() + "') as stodate,"
				+ " rui.ntzfromdate,tz.stimezoneid as stzfromdate ,rui.noffsetdfromdate,rui.noffsetdtodate,rui.ntztodate,tz1.stimezoneid as stztodate"
				+ " from registrationtest rt,registrationarno rar,registrationsamplearno sar,resultusedinstrument rui, "
				+ " instrumentcategory ic,instrument i,timezone tz,timezone tz1"
				+ " where rt.npreregno=rar.npreregno and rt.ntransactionsamplecode=sar.ntransactionsamplecode"
				+ " and i.ninstrumentcode = rui.ninstrumentcode and ic.ninstrumentcatcode = rui.ninstrumentcatcode "
				+ " and i.nregionalsitecode= "+userInfo.getNtranssitecode()
				+ " and rt.ntransactiontestcode=rui.ntransactiontestcode"
				+ " and tz.ntimezonecode = rui.ntzfromdate and tz1.ntimezonecode = rui.ntztodate "
				+ " and rt.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and i.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and ic.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rui.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + conditionsstr +" "
				+ " order  by  rui.nresultusedinstrumentcode desc";

		List<ResultUsedInstrument> lstresultUsedInstrument = jdbcTemplate.query(getinstrument,	new ResultUsedInstrument());

		objMapper.registerModule(new JavaTimeModule());
		List<?> lstRUIconversion = dateUtilityFunction.getSiteLocalTimeFromUTC(lstresultUsedInstrument,
				Arrays.asList("sfromdate", "stodate"), Arrays.asList("ntzfromdate", "ntztodate"), userInfo, false,Arrays.asList("sdisplaystatus"), false);

		lstresultUsedInstrument = objMapper.convertValue(lstRUIconversion, new TypeReference<List<ResultUsedInstrument>>() {});

		if (nresultusedinstrumentcode > 0) {

			ResultUsedInstrument validationObj = new ResultUsedInstrument();

			validationObj = getvalidationResultUsedInstrument(nresultusedinstrumentcode, userInfo);

			if (validationObj == null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("ALREADYDELETED", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				
				//ALPDJ21-31-->Added by Vignesh(23-07-2025)-->Added the new dropdown Instrument Type-Standard, Storage

				final String str = "select ic.ninstrumentcatcode,ic.sinstrumentcatname,ic.ncalibrationreq,ic.ndefaultstatus from InstrumentCategory ic,instrumenttype it "
						+ " where ic.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and it.ninstrumenttypecode=ic.ninstrumenttypecode "
						+ " and ic.ninstrumenttypecode="+Enumeration.InstrumentType.STANDARD_INSTRUMENT.getNinstrumenttypecode()+" "
						+ " and it.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						+ " and ic.nsitecode=" + userInfo.getNmastersitecode() + " "
						+ " and ic.ninstrumentcatcode>0 order by ndefaultstatus,ninstrumentcatcode desc";
				List<InstrumentCategory> lstinstcat = jdbcTemplate.query(str, new InstrumentCategory());
				returnMap.put("InstrumentCategory", lstinstcat);

				returnMap.putAll((Map<String, Object>) getResultUsedInstrumentNameCombo(2,
						lstresultUsedInstrument.get(0).getNinstrumentcatcode(),
						lstresultUsedInstrument.get(0).getNcalibrationreq(),1, userInfo).getBody());

				returnMap.putAll((Map<String, Object>) getResultUsedInstrumentIdCombo(lstresultUsedInstrument.get(0).getNinstrumentcatcode(),
						lstresultUsedInstrument.get(0).getNinstrumentnamecode(),
						lstresultUsedInstrument.get(0).getNcalibrationreq(),1, userInfo).getBody());
			}

		}

		returnMap.put(keyvalue, lstresultUsedInstrument);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	public ResultUsedInstrument getvalidationResultUsedInstrument(final int nresultusedinstrumentcode,UserInfo userInfo) throws Exception {

		String str = "select *,to_char(dfromdate,'" + userInfo.getSpgdatetimeformat()
		+ "') as sfromdate,to_char(dtodate,'" + userInfo.getSpgdatetimeformat()
		+ "') as stodate from resultusedinstrument where nresultusedinstrumentcode = "+ nresultusedinstrumentcode + " "
		+ " and nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		return (ResultUsedInstrument) jdbcUtilityFunction.queryForObject(str, ResultUsedInstrument.class,jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> getResultUsedInstrumentNameCombo(int nflag, int ninstrumentcatcode,int ncalibrationRequired,final int ntestgrouptestcode, UserInfo userInfo) throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		Map<String, Object> returnValue = new HashMap<>();
		String str = "";
		if (nflag == 1) {

			//ALPDJ21-31-->Added by Vignesh(23-07-2025)-->Added the new dropdown Instrument Type-Standard, Storage

			str = "select ic.ninstrumentcatcode,ic.sinstrumentcatname,ic.ncalibrationreq,ic.ndefaultstatus "
					+ " from InstrumentCategory ic ,testgrouptest tt,instrumenttype it"
					+ " where  tt.ntestgrouptestcode="+ntestgrouptestcode+" and ic.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ic.nsitecode=" + userInfo.getNmastersitecode() + " and tt.ninstrumentcatcode=ic.ninstrumentcatcode "
					+ " and it.ninstrumenttypecode=ic.ninstrumenttypecode "
					+ " and ic.ninstrumenttypecode="+Enumeration.InstrumentType.STANDARD_INSTRUMENT.getNinstrumenttypecode()+""
					+ " and ic.ninstrumentcatcode>0 and tt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ "	and it.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
					+ " and tt.nsitecode="+userInfo.getNmastersitecode()+" order by ndefaultstatus,ninstrumentcatcode desc";

			List<InstrumentCategory> lstinstcat = jdbcTemplate.query(str, new InstrumentCategory());

			returnMap.put("InstrumentName", new ArrayList<>());
			if (!lstinstcat.isEmpty()) {
				returnMap.put("InstrumentCategory", lstinstcat);
				returnValue = new HashMap<>();
				List<InstrumentCategory> defaultInstrumentCategory = lstinstcat.stream()
						.filter(x -> x.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus())
						.collect(Collectors.toList());
				List<Instrument> lstinst = new ArrayList<>();
				if (defaultInstrumentCategory.isEmpty()) {

					returnMap.put("InstrumentName", lstinst);

				} else {

					lstinst = getInstrumentName(defaultInstrumentCategory.get(0).getNinstrumentcatcode(),
							defaultInstrumentCategory.get(0).getNcalibrationreq(),userInfo);				  
					
					if (lstinst != null && ! lstinst.isEmpty()) {
						List<Instrument> defaultType = lstinst.stream()
								.filter(x -> x.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus())
								.collect(Collectors.toList());
						if (!defaultType.isEmpty()) {
						List<Instrument> lstinst1 = getInstrumentId(defaultInstrumentCategory.get(0).getNinstrumentcatcode(),
								defaultType.get(0).getNinstrumentnamecode(),defaultType.get(0).getNcalibrationreq(), userInfo);
						returnMap.put("InstrumentId", lstinst1);
						}
					}
					
					List<Instrument> uniqueInstruments1 = new ArrayList<>(
						    lstinst.stream()
						        .collect(Collectors.toMap(
						            Instrument::getSinstrumentname,   // key: name
						            Function.identity(),              // value: Instrument
						            (existing, replacement) -> existing // keep first
						        ))
						        .values()
						);
						returnMap.put("InstrumentName", uniqueInstruments1);
				}
			}
		} else if (nflag == 2) {

			List<Instrument> lstinst = getInstrumentName(ninstrumentcatcode, ncalibrationRequired,userInfo);
			//returnMap.put("InstrumentName", lstinst);
			if (lstinst != null && ! lstinst.isEmpty()) {
				List<Instrument> defaultType = lstinst.stream()
						.filter(x -> x.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus())
						.collect(Collectors.toList());
				if (!defaultType.isEmpty()) {
				List<Instrument> lstinst1 = getInstrumentId(ninstrumentcatcode,defaultType.get(0).getNinstrumentnamecode(),defaultType.get(0).getNcalibrationreq(), userInfo);
				returnMap.put("InstrumentId", lstinst1);
				}
			}
			List<Instrument> uniqueInstruments1 = new ArrayList<>(
				    lstinst.stream()
				        .collect(Collectors.toMap(
				            Instrument::getSinstrumentname,   // key: name
				            Function.identity(),              // value: Instrument
				            (existing, replacement) -> existing // keep first
				        ))
				        .values()
				);
			returnMap.put("InstrumentName", uniqueInstruments1);
		}
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}


	@Override
	public ResponseEntity<Object> getResultUsedInstrumentIdCombo(int ninstrumentcatcode,final int ninstrumentnamecode,int ncalibrationRequired,final int ntestgrouptestcode, UserInfo userInfo) throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		Map<String, Object> returnValue = new HashMap<>();
		String str = "";
		List<Instrument> lstinst = getInstrumentId(ninstrumentcatcode,ninstrumentnamecode, ncalibrationRequired,userInfo);
		returnMap.put("InstrumentId", lstinst);

		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}


	private List<Instrument> getInstrumentName(int ninstrumentcatcode, int ncalibrationRequired, UserInfo userInfo) {
		String str = "";
		if (ncalibrationRequired == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

			str = " select i.ninstrumentnamecode,ic1.ncalibrationreq, i.sinstrumentname,i.ndefaultstatus "
					+ " from instrument i "
					+ " join instrumentcategory ic1 on i.ninstrumentcatcode=ic1.ninstrumentcatcode "
					+ " join instrumentcalibration ic on i.ninstrumentcode = ic.ninstrumentcode "
					+ " join instrumentmaintenance im on i.ninstrumentcode = im.ninstrumentcode "
					+ " where "
					+ " i.nregionalsitecode="+userInfo.getNtranssitecode()+" "
					+ " and ic1.nsitecode = "+userInfo.getNmastersitecode()+" "
					+ " and ic.nsitecode = "+userInfo.getNtranssitecode()+" "
					+ " and im.nsitecode = "+userInfo.getNtranssitecode()+""
					+ " and i.ninstrumentstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and i.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and ic1.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and ic.nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and im.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ic.ncalibrationstatus = "+ Enumeration.TransactionStatus.CALIBIRATION.gettransactionstatus()
					+ " and im.nmaintenancestatus = "+ Enumeration.TransactionStatus.MAINTANENCE.gettransactionstatus()
					+ " and i.ninstrumentcatcode = " + ninstrumentcatcode
					+ " group by i.ninstrumentnamecode,ic1.ncalibrationreq, i.sinstrumentname,i.ndefaultstatus;";

		} else {

			str = " select i.ninstrumentnamecode,i.sinstrumentname,i.ndefaultstatus,ic.ncalibrationreq  "
					+ " from instrument i "
					+ " join instrumentcategory ic on i.ninstrumentcatcode =ic.ninstrumentcatcode "
					+ " join instrumentmaintenance im on i.ninstrumentcode = im.ninstrumentcode  "
					+ " where  "
					+  " i.ninstrumentstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and i.nregionalsitecode="+userInfo.getNtranssitecode()
					+ " and ic.nsitecode="+userInfo.getNmastersitecode()
					+ " and im.nsitecode="+userInfo.getNtranssitecode()
					+ " and im.nmaintenancestatus= "+ Enumeration.TransactionStatus.MAINTANENCE.gettransactionstatus()
					+ " and i.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()  
					+ " and ic.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and im.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+"  and i.ninstrumentcatcode =  " + ninstrumentcatcode
					+ " group by i.ninstrumentnamecode,i.sinstrumentname,i.ndefaultstatus,ic.ncalibrationreq ";
		}
		return jdbcTemplate.query(str, new Instrument());
	}

	private List<Instrument> getInstrumentId(int ninstrumentcatcode,int ninstrumentnamecode, int ncalibrationRequired, UserInfo userInfo) {
		String str = "";

		if (ncalibrationRequired == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

			str = " select i.ninstrumentcode,i.ninstrumentnamecode,i.sinstrumentid,i.sinstrumentname,i.ndefaultstatus "
					+ " from instrument i "
					+ " join instrumentcalibration ic on i.ninstrumentcode = ic.ninstrumentcode "
					+ " join instrumentmaintenance im on i.ninstrumentcode = im.ninstrumentcode "
					+ " where i.ninstrumentcatcode = " + ninstrumentcatcode 
					+ " and i.ninstrumentnamecode=" +ninstrumentnamecode 
					+ " and i.nregionalsitecode="+userInfo.getNtranssitecode()+" "
					+ " and ic.nsitecode = "+userInfo.getNtranssitecode()+" "
					+ " and im.nsitecode = "+userInfo.getNtranssitecode()+""
					+ " and ic.ncalibrationstatus = "+ Enumeration.TransactionStatus.CALIBIRATION.gettransactionstatus()
					+ " and im.nmaintenancestatus = "+ Enumeration.TransactionStatus.MAINTANENCE.gettransactionstatus()
					+ " and i.ninstrumentstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and i.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and ic.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and im.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " group by i.ninstrumentcode,i.ninstrumentnamecode,i.sinstrumentid,i.sinstrumentname,i.ndefaultstatus "
					;
		} else {

			str = " select i.ninstrumentcode,i.ninstrumentnamecode, i.ninstrumentcatcode, i.sinstrumentid, i.sinstrumentname,i.ndefaultstatus "
					+ " from instrument  i   "
					+ " join instrumentmaintenance im on i.ninstrumentcode = im.ninstrumentcode "
					+ " where "
					+ "	im.nmaintenancestatus= " + Enumeration.TransactionStatus.MAINTANENCE.gettransactionstatus()
					+ " and i.nregionalsitecode=" + userInfo.getNtranssitecode()+""
					+ " and im.nsitecode = "+userInfo.getNtranssitecode()+""
					+ " and i.ninstrumentstatus =  "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and i.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and im.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and i.ninstrumentcatcode =  " + ninstrumentcatcode+""
					+ "  and i.ninstrumentnamecode=" +ninstrumentnamecode+""
					+ " group by i.ninstrumentcode,i.ninstrumentnamecode, i.ninstrumentcatcode, i.sinstrumentid, i.sinstrumentname,i.ndefaultstatus "; 
		}
		return jdbcTemplate.query(str, new Instrument());
	}

	@Override
	public Map<String, Object> seqNoGetforResultUsedInstrument(ResultUsedInstrument objResultUsedInstrument,UserInfo userInfo) throws Exception {

		String str = "";
		Map<String, Object> returnMap = new HashMap<>();
		StringBuilder sb = new StringBuilder();
		ObjectMapper objMapper = new ObjectMapper();
		final String lockquery = "lock lockresultusedinstrument" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(lockquery);

		str = "select nsequenceno from seqnoregistration where stablename = N'resultusedinstrument' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
		SeqNoRegistration objsequenceno = (SeqNoRegistration) jdbcUtilityFunction.queryForObject(str, SeqNoRegistration.class,jdbcTemplate);
		int nresultusedinstrumentcode = objsequenceno.getNsequenceno() + 1;

		returnMap.put("nresultusedinstrumentcode", nresultusedinstrumentcode);

		objMapper.registerModule(new JavaTimeModule());
		objResultUsedInstrument.setSfromdate(dateUtilityFunction.instantDateToString(objResultUsedInstrument.getDfromdate()).replace("T", " ").replace("Z",""));
		objResultUsedInstrument.setStodate(dateUtilityFunction.instantDateToString(objResultUsedInstrument.getDtodate()).replace("T", " ").replace("Z",""));

		final ResultUsedInstrument convertedObject = objMapper.convertValue(dateUtilityFunction.convertInputDateToUTCByZone(objResultUsedInstrument,
				Arrays.asList("sfromdate", "stodate"),
				Arrays.asList("ntzfromdate","ntztodate"),
				true, userInfo), // , false),
				new TypeReference<ResultUsedInstrument>() {
		});

		if (convertedObject.getDfromdate().isBefore(convertedObject.getDtodate())
				|| convertedObject.getDfromdate().equals(convertedObject.getDtodate())) {
			str = "update  seqnoregistration set nsequenceno=" + nresultusedinstrumentcode+ " where stablename = N'resultusedinstrument' "
					+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
			jdbcTemplate.execute(str);
			returnMap.put("rtn", Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
			return returnMap;
		} else {
			returnMap.put("rtn", "IDS_SELECTFROMDATEBEFORETODATE");
			return returnMap;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> createResultUsedInstrument(ResultUsedInstrument objResultUsedInstrument,final int nregtypecode, final int nregsubtypecode, 
			final int ndesigntemplatemappingcode,final String transactiontestcode, final JSONObject jsonData,UserInfo userInfo, int nresultusedinstrumentcode) throws Exception {

		JSONObject auditOldData = new JSONObject();
		JSONObject auditActionType = new JSONObject();
		List<Map<String, ResultUsedInstrument>> lstdata = new ArrayList<>();
		List<ResultUsedInstrument> lstaudit = new ArrayList<>();
		ObjectMapper objMapper = new ObjectMapper();
		String str = "";
		Map<String, Object> returnMap = new HashMap<>();
		StringBuilder sb = new StringBuilder();
		//		str = "lock lockcancelreject"+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		//		getJdbcTemplate().execute(str);

		objMapper.registerModule(new JavaTimeModule());
		objResultUsedInstrument.setSfromdate(
				dateUtilityFunction.instantDateToString(objResultUsedInstrument.getDfromdate()).replace("T", " ").replace("Z",""));
		objResultUsedInstrument
		.setStodate(dateUtilityFunction.instantDateToString(objResultUsedInstrument.getDtodate()).replace("T", " ").replace("Z",""));

		final ResultUsedInstrument convertedObject = objMapper.convertValue(dateUtilityFunction.convertInputDateToUTCByZone(objResultUsedInstrument,
				Arrays.asList("sfromdate", "stodate"),Arrays.asList("ntzfromdate","ntztodate"),true, userInfo),
				new TypeReference<ResultUsedInstrument>() {
		});

		if (convertedObject.getDfromdate().isBefore(convertedObject.getDtodate())
				|| convertedObject.getDfromdate().equals(convertedObject.getDtodate())) {
			// ALPD-5032 added by Dhanushya RI,To insert offset date in resultusedinstrument table
			sb.append(
					" insert into resultusedinstrument (nresultusedinstrumentcode,ntransactiontestcode,npreregno,ninstrumentcatcode,ninstrumentcode,ninstrumentnamecode,"
							+ "dfromdate,dtodate,ntzfromdate,ntztodate,noffsetdfromdate,noffsetdtodate,jsondata,nsitecode,nstatus) values ("
							+ nresultusedinstrumentcode + "," + objResultUsedInstrument.getNtransactiontestcode() + ","
							+ objResultUsedInstrument.getNpreregno() + ","
							+ objResultUsedInstrument.getNinstrumentcatcode() + ","
							+ objResultUsedInstrument.getNinstrumentcode() + ","
							+ objResultUsedInstrument.getNinstrumentnamecode() + ",N'"
							+ convertedObject.getSfromdate() + "'::timestamp without time zone," + "N'"
							+ convertedObject.getStodate() + "'::timestamp without time zone,"
							+ objResultUsedInstrument.getNtzfromdate() + "," + objResultUsedInstrument.getNtztodate()
							+ ","+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())+","+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())+","
							+ " '"+jsonData+"',"+userInfo.getNtranssitecode()+"," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");");
			jdbcTemplate.execute(sb.toString());

			List<ResultUsedInstrument> lstResultUsedInstrument1 = new ArrayList<>();
			objResultUsedInstrument.setNresultusedinstrumentcode(nresultusedinstrumentcode);
			lstResultUsedInstrument1.add(objResultUsedInstrument);
			List<Object> lstaftersave = new ArrayList<>();
			lstaftersave.add(lstResultUsedInstrument1);
			Map<String, Object> objMap = new HashMap<>();
			objMap.put("nregtypecode", nregtypecode);
			objMap.put("nregsubtypecode", nregsubtypecode);
			objMap.put("ndesigntemplatemappingcode", ndesigntemplatemappingcode);

			returnMap.putAll((Map<String, Object>) getResultUsedInstrument(transactiontestcode,	nresultusedinstrumentcode, userInfo).getBody());

			lstdata = (List<Map<String, ResultUsedInstrument>>) returnMap.get("ResultUsedInstrument");
			lstaudit.add(0, (ResultUsedInstrument) lstdata.get(lstdata.size() - 1));
			auditOldData.put("resultusedinstrument", lstaudit);
			auditActionType.put("resultusedinstrument", "IDS_ADDRESULTUSEDINSTRUMENT");
			auditUtilityFunction.fnJsonArrayAudit(auditOldData, null, auditActionType, objMap, false, userInfo);

			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTFROMDATEBEFORETODATE",	userInfo.getSlanguagefilename()), 
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> updateResultUsedInstrument(ResultUsedInstrument objResultUsedInstrument,final int nregtypecode, 
			final int nregsubtypecode, final int ndesigntemplatemappingcode,final String ntransactiontestcode, final JSONObject jsonData,UserInfo userInfo) 
					throws Exception {

		JSONObject auditOldData = new JSONObject();
		JSONObject auditNewData = new JSONObject();
		JSONObject auditActionType = new JSONObject();
		List<ResultUsedInstrument> lstdata = new ArrayList<>();
		List<ResultUsedInstrument> lstdataold = new ArrayList<>();
		List<ResultUsedInstrument> lstaudit = new ArrayList<>();
		ObjectMapper objMapper =  new ObjectMapper();

		Map<String, Object> returnMap = new HashMap<>();
		ResultUsedInstrument validationObj = new ResultUsedInstrument();
		String str = "";
		objMapper.registerModule(new JavaTimeModule());
		objResultUsedInstrument.setSfromdate(
				dateUtilityFunction.instantDateToString(objResultUsedInstrument.getDfromdate()).replace("T", " "));
		objResultUsedInstrument
		.setStodate(dateUtilityFunction.instantDateToString(objResultUsedInstrument.getDtodate()).replace("T", " "));

		final ResultUsedInstrument convertedObject = objMapper.convertValue(
				dateUtilityFunction.convertInputDateToUTCByZone(objResultUsedInstrument,Arrays.asList("sfromdate", "stodate"),
						Arrays.asList("ntzfromdate","ntztodate"),true, userInfo), new TypeReference<ResultUsedInstrument>() {});

		validationObj = getvalidationResultUsedInstrument(objResultUsedInstrument.getNresultusedinstrumentcode(), userInfo);

		if (validationObj != null) {

			str = "lock lockcancelreject "+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(str);

			if (convertedObject.getDfromdate().isBefore(convertedObject.getDtodate()) || convertedObject.getDfromdate().equals(convertedObject.getDtodate())) {

				returnMap.putAll((Map<String, Object>) getResultUsedInstrument(ntransactiontestcode, -1, userInfo).getBody());

				int nresultusedinstrumentcode = objResultUsedInstrument.getNresultusedinstrumentcode();
				lstdataold = (List<ResultUsedInstrument>) returnMap.get("ResultUsedInstrument");
				lstaudit.add((ResultUsedInstrument) (lstdataold.stream().filter(t -> ((ResultUsedInstrument) t).getNresultusedinstrumentcode() == nresultusedinstrumentcode)
						.findAny()).get());

				auditOldData.put("resultusedinstrument", lstaudit);
				returnMap.clear();
				lstaudit.clear();

				//ALPD-5032 added by Dhanushya RI,update jsonObject when edit the instrument
				str = "update resultusedinstrument set ninstrumentcatcode="
						+ objResultUsedInstrument.getNinstrumentcatcode() + " ,ninstrumentcode="
						+ objResultUsedInstrument.getNinstrumentcode() + ",ninstrumentnamecode="
						+ objResultUsedInstrument.getNinstrumentnamecode() + ",dfromdate=N'"
						+ convertedObject.getSfromdate() + "'::timestamp without time zone ,dtodate=N'"
						+ convertedObject.getStodate() + "'::timestamp without time zone,ntzfromdate="
						+ objResultUsedInstrument.getNtzfromdate() + " ,ntztodate="
						+ objResultUsedInstrument.getNtztodate() + ",jsondata='"+jsonData+"' where nresultusedinstrumentcode="
						+ objResultUsedInstrument.getNresultusedinstrumentcode() + " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
				jdbcTemplate.execute(str);

				returnMap.putAll(
						(Map<String, Object>) getResultUsedInstrument(ntransactiontestcode, -1, userInfo).getBody());
				returnMap.put("selectedId", objResultUsedInstrument.getNresultusedinstrumentcode());
				objResultUsedInstrument = getvalidationResultUsedInstrument(
						objResultUsedInstrument.getNresultusedinstrumentcode(), userInfo);
				List<Object> lstbeforesave = new ArrayList<>();
				List<Object> lstaftersave = new ArrayList<>();
				List<ResultUsedInstrument> lst = new ArrayList<>();
				List<ResultUsedInstrument> lstbf = new ArrayList<>();
				lst.add(objResultUsedInstrument);
				lstbf.add(validationObj);
				lstbeforesave.add(lstbf);
				lstaftersave.add(lst);
				Map<String, Object> objMap = new HashMap<>();
				objMap.put("nregtypecode", nregtypecode);
				objMap.put("nregsubtypecode", nregsubtypecode);
				objMap.put("ndesigntemplatemappingcode", ndesigntemplatemappingcode);

				lstdata = (List<ResultUsedInstrument>) returnMap.get("ResultUsedInstrument");
				lstaudit.add((ResultUsedInstrument) (lstdata.stream().filter(
						t -> ((ResultUsedInstrument) t).getNresultusedinstrumentcode() == nresultusedinstrumentcode)
						.findAny()).get());

				auditNewData.put("resultusedinstrument", lstaudit);
				auditActionType.put("resultusedinstrument", "IDS_EDITRESULTUSEDINSTRUMENT");
				auditUtilityFunction.fnJsonArrayAudit(auditOldData, auditNewData, auditActionType, objMap, false, userInfo);

			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTFROMDATEBEFORETODATE",userInfo.getSlanguagefilename()), 
						HttpStatus.EXPECTATION_FAILED);
			}

		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("ALREADYDELETED", userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
		}	
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> deleteResultUsedInstrument(final int nresultusedinstrumentcode,final int nregtypecode, final int nregsubtypecode, 
			final int ndesigntemplatemappingcode,final String ntransactiontestcode, UserInfo userInfo) throws Exception {

		JSONObject auditOldData = new JSONObject();
		JSONObject auditActionType = new JSONObject();
		List<ResultUsedInstrument> lstdata = new ArrayList<>();
		List<ResultUsedInstrument> lstaudit = new ArrayList<>();

		Map<String, Object> returnMap = new HashMap<>();
		ResultUsedInstrument validationObj = new ResultUsedInstrument();

		validationObj = getvalidationResultUsedInstrument(nresultusedinstrumentcode, userInfo);

		if (validationObj != null) {
			returnMap.putAll((Map<String, Object>) getResultUsedInstrument(ntransactiontestcode, -1, userInfo).getBody());
			lstdata = (List<ResultUsedInstrument>) returnMap.get("ResultUsedInstrument");
			returnMap.clear();
			final String str = "update resultusedinstrument set nstatus="+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " "
					+ " where nresultusedinstrumentcode="+ nresultusedinstrumentcode + " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
			jdbcTemplate.execute(str);

			returnMap.putAll((Map<String, Object>) getResultUsedInstrument(ntransactiontestcode, -1, userInfo).getBody());

			List<Object> lstaftersave = new ArrayList<>();
			List<ResultUsedInstrument> lst = new ArrayList<>();
			lst.add(validationObj);
			lstaftersave.add(lst);
			Map<String, Object> objMap = new HashMap<>();
			objMap.put("nregtypecode", nregtypecode);
			objMap.put("nregsubtypecode", nregsubtypecode);
			objMap.put("ndesigntemplatemappingcode", ndesigntemplatemappingcode);

			lstaudit.add((ResultUsedInstrument) (lstdata.stream()
					.filter(t -> ((ResultUsedInstrument) t).getNresultusedinstrumentcode() == nresultusedinstrumentcode)
					.findAny()).get());
			auditOldData.put("resultusedinstrument", lstaudit);

			auditActionType.put("resultusedinstrument", "IDS_DELETERESULTUSEDINSTRUMENT");

			auditUtilityFunction.fnJsonArrayAudit(auditOldData, null, auditActionType, objMap, false, userInfo);

			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("ALREADYDELETED", userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
		}
	}



	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getREMaterialTypeCombo(final int ntestgroupTestcode,final UserInfo userInfo,final short sectioncode) throws Exception {
		Map<String, Object> returnMap = new HashMap<>();

		List<MaterialType> lstmaterialtype = (List<MaterialType>) getREMaterialType(ntestgroupTestcode,userInfo).getBody();
		returnMap.put("MaterialType", lstmaterialtype);
		if (lstmaterialtype != null && !lstmaterialtype.isEmpty()) {
			List<MaterialType> defaultType = lstmaterialtype.stream()
					.filter(x -> x.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus())
					.collect(Collectors.toList());
			if (!defaultType.isEmpty()) {
				returnMap.putAll(
						getREMaterialCategoryByType(ntestgroupTestcode,defaultType.get(0).getNmaterialtypecode(), userInfo, sectioncode));
			}

		}
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}




	public ResponseEntity<Object> getREMaterialType (final int ntestgroupTestcode,final UserInfo userInfo) throws Exception {

		String strQuery = "";
		if(ntestgroupTestcode > 0)
		{
			strQuery = "select tstm.nmaterialtypecode,coalesce(max(mt.jsondata->'smaterialtypename'->>'"+userInfo.getSlanguagetypecode()+"'),max(mt.jsondata->'smaterialtypename'->>'en-US')) as smaterialtypename,"
					+ " mt.jsondata||json_build_object('nmaterialtypecode',tstm.nmaterialtypecode)::jsonb as jsondata,max(mt.ndefaultstatus) ndefaultstatus"
					+ " from testgrouptestmaterial tstm, materialtype mt where mt.nmaterialtypecode = tstm.nmaterialtypecode"
					+ " and tstm.ntestgrouptestcode = "+ntestgroupTestcode+" "
					+ " and tstm.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and mt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and tstm.nsitecode = "+userInfo.getNmastersitecode()+" "
					+ " and mt.nsitecode = "+userInfo.getNmastersitecode()+""
					+ " group by tstm.nmaterialtypecode,mt.jsondata;";
		}
		else
		{ 
			strQuery = "select m.nmaterialtypecode,coalesce(m.jsondata->'smaterialtypename'->>'"+ userInfo.getSlanguagetypecode() + "',"
					+ "m.jsondata->'smaterialtypename'->>'en-US') as smaterialtypename,m.jsondata||json_build_object('nmaterialtypecode',nmaterialtypecode)"
					+ " ::jsonb as jsondata,m.ndefaultstatus from materialtype m where "
					+ " m.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and m.nsitecode = "+ userInfo.getNmastersitecode() + " "
					+ " and m.nmaterialtypecode not in (-1,4)";
		}

		return new ResponseEntity<>(jdbcTemplate.query(strQuery, new MaterialType()), HttpStatus.OK);
	}


	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> getREMaterialCategoryByType(final int ntestgroupTestcode,final short materialTypeCode,final UserInfo userInfo,final short sectioncode)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		List<MaterialCategory> lstmaterialcat = null;
		if(ntestgroupTestcode > 0)
		{
			final String str = "select tstm.nmaterialcatcode,max(mc.nmaterialtypecode) nmaterialtypecode,max(mc.smaterialcatname) smaterialcatname,"+
					" max(mc.sdescription) sdescription,max(mc.needsectionwise) needsectionwise,max(mc.ncategorybasedflow) ncategorybasedflow"+
					" from testgrouptestmaterial tstm, materialcategory mc" + 
					" where tstm.nmaterialcatcode = mc.nmaterialcatcode "
					+ " and tstm.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and mc.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and tstm.nsitecode = "+userInfo.getNmastersitecode()+""
					+ " and mc.nsitecode = "+userInfo.getNmastersitecode()+""
					+ " and tstm.nmaterialtypecode = "+materialTypeCode+" "
					+ " and tstm.ntestgrouptestcode = "+ntestgroupTestcode+" "
					+ " group by tstm.nmaterialcatcode";

			lstmaterialcat = jdbcTemplate.query(str, new MaterialCategory());
		}
		else
		{
			lstmaterialcat = (List<MaterialCategory>) materialCatDAO.getMaterialCategoryByType(materialTypeCode, userInfo).getBody();
		}

		returnMap.put("MaterialCategory", lstmaterialcat);
		if (lstmaterialcat != null && !lstmaterialcat.isEmpty()) {
			List<MaterialCategory> defaultType = lstmaterialcat.stream()
					.filter(x -> x.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus())
					.collect(Collectors.toList());

			if (!defaultType.isEmpty()) {
				returnMap.putAll(getREMaterialByCategory(ntestgroupTestcode,materialTypeCode, defaultType.get(0).getNmaterialcatcode(),
						sectioncode, userInfo));
			}

		}

		return returnMap;

	}

	@Override
	public Map<String, Object> getREMaterialByCategory(final int ntestgroupTestcode,final short materialTypeCode,final int materiaCatCode,final int sectionCode,
			final UserInfo userInfo) throws Exception {

		Map<String, Object> returnMap = new HashMap<>();		
		String getMaterialQuery = "";
		if(ntestgroupTestcode > 0)
		{
			getMaterialQuery = "select distinct m.nmaterialcode,m.jsondata->>'Material Name' as smaterialname" + 
					" from testgrouptestmaterial tstm,materialinventorytransaction mit,materialinventory mi,material m" + 
					" where" + 
					" m.nmaterialcode = mi.nmaterialcode " + 
					" and tstm.nmaterialcode = m.nmaterialcode" + 
					" and mi.nmaterialinventorycode = mit.nmaterialinventorycode" + 
					" and mi.ntransactionstatus = "+Enumeration.TransactionStatus.RELEASED.gettransactionstatus()+"" + 
					" and tstm.nmaterialcatcode = "+materiaCatCode+"" + 
					" and mit.nsitecode = "+userInfo.getNtranssitecode()+" " + 
					" and tstm.nsitecode = "+userInfo.getNmastersitecode()+""+
					" and mi.nsitecode = "+userInfo.getNtranssitecode()+""+
					" and m.nsitecode = "+userInfo.getNmastersitecode()+""+
					" and m.nstatus =  "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""+
					" and mi.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""+
					" and tstm.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""+
					" and mit.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""+
					" and mit.nsectioncode = "+sectionCode+"";	
		}
		else
		{
			getMaterialQuery = "select distinct m.nmaterialcode,m.jsondata->>'Material Name' "
					+ " as smaterialname" + " from material m,materialinventory mi,materialinventorytransaction mit,materialcategory mc "
					+ " where (m.jsondata->'nmaterialcatcode')::int = " + materiaCatCode
					+ " and (mi.jsondata->'ntranscode')::int = "+ Enumeration.TransactionStatus.RELEASED.gettransactionstatus()
					+ " and m.nmaterialcode = mi.nmaterialcode and mi.nmaterialinventorycode = mit.nmaterialinventorycode and mc.nmaterialcatcode="+materiaCatCode
					+ " and mit.nsitecode = " + userInfo.getNtranssitecode()
					+ " and mi.nsitecode = " + userInfo.getNtranssitecode()
					+ " and m.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and mi.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and mit.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " AND ( (mc.needsectionwise = "+Enumeration.TransactionStatus.NO.gettransactionstatus()+" AND mit.nsectioncode = "+Enumeration.TransactionStatus.NA.gettransactionstatus()+" ) "
					+ " OR (mc.needsectionwise != "+Enumeration.TransactionStatus.NO.gettransactionstatus()+" AND mit.nsectioncode = "+sectionCode+" ) )"
				    + "  and mc.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and mc.nsitecode= "+userInfo.getNmastersitecode();
		}


		List<Material> lstMaterial = jdbcTemplate.query(getMaterialQuery, new Material());

		if (lstMaterial != null && !lstMaterial.isEmpty()) {

			returnMap.putAll(getREMaterialInvertoryByMaterial(ntestgroupTestcode,lstMaterial.get(0).getNmaterialcode(), sectionCode, userInfo));
		}
		returnMap.put("Material", lstMaterial);

		return returnMap;

	}

	@Override
	public Map<String, Object> getREMaterialInvertoryByMaterial(final int ntestgroupTestcode,final int materialCode,final int sectionCode,final UserInfo userInfo)
			throws Exception {

		Map<String, Object> returnMap = new HashMap<>();
		final String getMaterialQuery = "select mi.nmaterialinventorycode,mi.jsondata->>'Inventory ID' as sinventoryid,mi.jsondata->'Unit'->>'label' as sunitname,coalesce(sum(mit.nqtyreceived)-sum(mit.nqtyissued),0) as savailablequatity "
				+ " from materialinventory mi,materialinventorytransaction mit , materialcategory mc where  "
				+ " mi.nmaterialinventorycode = mit.nmaterialinventorycode "
				+"  and (mi.jsondata->'ntranscode')::int = "+ Enumeration.TransactionStatus.RELEASED.gettransactionstatus()
				+ " and mit.nsitecode = "+ userInfo.getNtranssitecode() + " "
				+ " and mi.nsitecode = "+ userInfo.getNtranssitecode() + " "
				+ " and mi.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and mit.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				//+ " and mit.nsectioncode = " + sectionCode +" 
				+ " AND ( (mc.needsectionwise = "+Enumeration.TransactionStatus.NO.gettransactionstatus()+" AND mit.nsectioncode = "+Enumeration.TransactionStatus.NA.gettransactionstatus()+" )  OR (mc.needsectionwise != "+Enumeration.TransactionStatus.NO.gettransactionstatus()+" AND mit.nsectioncode = "+sectionCode+" ) ) "
				+ " and mi.nmaterialcode = "+ materialCode + ""
				+ " and  (mi.jsondata->'nmaterialcatcode')::int = mc.nmaterialcatcode and mc.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and mc.nsitecode="+userInfo.getNmastersitecode()+" "
				+ " group by mi.nmaterialinventorycode";

		List<MaterialInventory> lstMaterial = jdbcTemplate.query(getMaterialQuery, new MaterialInventory());
		returnMap.put("MaterialInventory", lstMaterial);
		return returnMap;
	}

	@Override
	public ResponseEntity<Object> getAvailableMaterialQuantity(final int ntestgroupTestcode,final int materialInvCode,final int sectionCode,final UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		final String getString = "select mi.nmaterialinventorycode,mi.nsectioncode,jsondata,a.*"
				+ " from materialinventorytransaction mi,("
				+ " select coalesce(sum(nqtyreceived)-sum(nqtyissued),0) as savailablequatity,max(jsonuidata->>'Unit') as sunitname"
				+ " from materialinventorytransaction where nmaterialinventorycode = " + materialInvCode
				+ " and nsectioncode = " + sectionCode + " and nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and nsitecode = "+userInfo.getNtranssitecode()+" group by nmaterialinventorycode) a "
				+ " where nmaterialinventorycode = " + materialInvCode + " and mi.nsitecode = "+userInfo.getNtranssitecode()+" "
				+ " and nsectioncode = " + sectionCode+" "
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " limit 1";

		MaterialInventory inventory = (MaterialInventory) jdbcUtilityFunction.queryForObject(getString, MaterialInventory.class,jdbcTemplate);

		returnMap.put("inventory", inventory);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ResponseEntity<Object> createResultUsedMaterial(Map<String, Object> objMapObject, UserInfo userInfo)	throws Exception {

		JSONObject auditOldData = new JSONObject();
		JSONObject auditNewData = new JSONObject();
		JSONObject auditActionType = new JSONObject();
		List<Map<String, Object>> lstdata = new ArrayList<>();
		List<Map<String, Object>> lstaudit = new ArrayList<>();
		ObjectMapper objMapper = new ObjectMapper();

		int nmaterialinvtrytrans = 0;
		int nresultusedmaterialcode = 0;

		String str = "";
		str = "lock resultusedmaterial"+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(str);
		str = "lock lockmaterialinventory"+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(str);

		ResultUsedMaterial objResultUsedMaterial = objMapper.convertValue(objMapObject.get("ResultUsedMaterial"),ResultUsedMaterial.class);
		MaterialInventoryTrans objMaterialInventoryTrans = objMapper.convertValue(objMapObject.get("MaterialInventoryTrans"), MaterialInventoryTrans.class);

		Map<String, Object> invertoryMap = (Map<String, Object>) getAvailableMaterialQuantity(-1,
				objResultUsedMaterial.getNinventorycode(), objResultUsedMaterial.getNsectioncode(), userInfo).getBody();

		MaterialInventory inventory = objMapper.convertValue(invertoryMap.get("inventory"), MaterialInventory.class);
		String availableQuantity = inventory.getSavailablequatity();

		String usedQuantity = objMaterialInventoryTrans.getJsondata().get("nqtyused").toString();
		if (Float.parseFloat(usedQuantity) > Float.parseFloat(availableQuantity)) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_USEDQUANTITYEXCEEDSAVAILABLEQUANTITY",userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		String reuseqry = "select coalesce((jsondata->'Reusable')::int,4) from material where nmaterialcode = "+ objResultUsedMaterial.getNmaterialcode() + " "
				+ " and nsitecode  = "+userInfo.getNmastersitecode()+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";

		int isReusable = (int) jdbcUtilityFunction.queryForObject(reuseqry, Integer.class,jdbcTemplate);

		str = "select nsequenceno from seqnomaterialmanagement where stablename ='materialinventorytransaction' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
		nmaterialinvtrytrans = (int) jdbcUtilityFunction.queryForObject(str, Integer.class,jdbcTemplate) + 1;

		str = "select nsequenceno from seqnoregistration where stablename ='resultusedmaterial' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
		nresultusedmaterialcode = (int) jdbcUtilityFunction.queryForObject(str, Integer.class,jdbcTemplate) + 1;

		str = " update seqnoregistration set nsequenceno=" + nresultusedmaterialcode+ " where stablename=N'resultusedmaterial' "
				+ "and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";

		if (isReusable == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
			str = str + " update seqnomaterialmanagement set nsequenceno=" + nmaterialinvtrytrans+" where stablename=N'materialinventorytransaction' "
					+ "and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
		}
		jdbcTemplate.execute(str);


		StringBuilder sb = new StringBuilder();
		Map<String, Object> jdata = objResultUsedMaterial.getJsondata();
		jdata.put("dtransactiondate",dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ").replace("Z", ""));

		sb.append(
				" insert into resultusedmaterial (nresultusedmaterialcode,ntransactiontestcode,npreregno,nmaterialtypecode,"
						+ "nmaterialcategorycode,nmaterialcode,ninventorycode,jsondata,nsitecode,nstatus) values " + "("
						+ nresultusedmaterialcode + "," + objResultUsedMaterial.getNtransactiontestcode() + ","
						+ objResultUsedMaterial.getNpreregno() + "," + objResultUsedMaterial.getNmaterialtypecode()
						+ "," + objResultUsedMaterial.getNmaterialcategorycode() + ","
						+ objResultUsedMaterial.getNmaterialcode() + "," + objResultUsedMaterial.getNinventorycode()
						+ ",'" + stringUtilityFunction.replaceQuote(objMapper.writeValueAsString(objResultUsedMaterial.getJsondata())) + "',"+userInfo.getNtranssitecode()+","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");");

		if (isReusable == Enumeration.TransactionStatus.NO.gettransactionstatus()) {

			final Map<String, Object> jsonData = new HashMap<>();
			JSONObject jsonObject = new JSONObject();

			String materialinventorytransactiondata=jdbcTemplate.queryForObject("select jsondata from materialinventorytransaction where nmaterialinventorycode="+
					objMaterialInventoryTrans.getNmaterialinventorycode()+" and nsitecode = "+userInfo.getNtranssitecode()+" "
					+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" order by nmaterialinventtranscode fetch first 1 row only", String.class);

			jsonObject=new JSONObject(materialinventorytransactiondata.toString());
			jsonData.put("sprecision",jsonObject.get("sprecision"));
			jsonData.put("needsectionwise",jsonObject.get("needsectionwise"));
			jsonData.put("Transaction Date & Time",dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ").replace("Z", ""));
			jsonObject.put("Transaction Date & Time",dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ").replace("Z", ""));
			sb.append(
					" insert into materialinventorytransaction (nmaterialinventtranscode ,nmaterialinventorycode ,ninventorytranscode ,ntransactiontype ,nsectioncode ,"
							+ " nresultusedmaterialcode,nqtyreceived,nqtyissued,jsondata,jsonuidata,dtransactiondate,ntztransactiondate,noffsetdtransactiondate,nsitecode,nstatus) values "
							+ "(" + nmaterialinvtrytrans + "," + objMaterialInventoryTrans.getNmaterialinventorycode()
							+ "," + Enumeration.TransactionStatus.OUTSIDE.gettransactionstatus() + ","
							+ Enumeration.TransactionStatus.USED.gettransactionstatus() + ","
							+ objMaterialInventoryTrans.getNsectioncode() + "," + nresultusedmaterialcode + ",0,"
							+ objMaterialInventoryTrans.getJsondata().get("nqtyused") + ",'"
							+ stringUtilityFunction.replaceQuote(jsonObject.toString())+ "','" + stringUtilityFunction.replaceQuote( objMapper.writeValueAsString(jsonData).toString())+ "','"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"+userInfo.getNtimezonecode()+","+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())+","+userInfo.getNtranssitecode()+","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");");

		}
		jdbcTemplate.execute(sb.toString());

		objResultUsedMaterial.setNresultusedmaterialcode(nresultusedmaterialcode);
		List<Object> lstobject = new ArrayList<>();
		List<String> lstaction = new ArrayList<>();
		lstobject.add(objResultUsedMaterial);
		lstaction.add("IDS_ADDMATERIAL");
		objMapObject.put("nresultusedmaterialcode", nresultusedmaterialcode);
		objMapObject.put("nFlag", 2);

		lstdata = (List<Map<String, Object>>) (((Map<String, Object>) getResultUsedMaterial(
				objMapObject.get("transactiontestcode").toString(), -1, userInfo).getBody()).get("ResultUsedMaterial"));

		final int nauditresultusedmaterialcode = nresultusedmaterialcode;
		lstaudit.add((Map<String, Object>) ((java.util.Optional) lstdata.stream().filter(
				t -> ((Map<String, Object>) t).get("nresultusedmaterialcode").equals(nauditresultusedmaterialcode))
				.findAny()).get());

		auditOldData.put("resultusedmaterial", lstaudit);
		auditActionType.put("resultusedmaterial", "IDS_ADDMATERIAL");
		auditUtilityFunction.fnJsonArrayAudit(auditOldData, null, auditActionType, objMapObject, false, userInfo);

		return getResultUsedMaterial(objMapObject.get("transactiontestcode").toString(), -1, userInfo);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getResultUsedMaterial(String ntransactionTestCode, int nresultUsedMaterialCode,
			UserInfo userInfo) throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		String conditionsstr = "";
		String keyvalue = "";

		if (ntransactionTestCode != null && ntransactionTestCode.split(",").length > 0
				&& !ntransactionTestCode.equals("")) {
			conditionsstr = " and rum.ntransactiontestcode in(" + ntransactionTestCode + ")";
			keyvalue = "ResultUsedMaterial";
		} else if (nresultUsedMaterialCode > 0) {
			conditionsstr = " and rum.nresultusedmaterialcode=(" + nresultUsedMaterialCode + ")";
			keyvalue = "EditResultUsedMaterial";
		}

		final String getMaterial = "SELECT json_AGG(A.JSONDATA) FROM (SELECT to_json(rum.*)::JSONB||rum.jsondata AS JSONDATA,rum.jsondata->>'nsectioncode' as nsectioncode "
				+ " from resultusedmaterial rum,materialtype mt where mt.nmaterialtypecode = rum.nmaterialtypecode"
				+ " and rum.nsitecode = "+userInfo.getNtranssitecode()+" "
				+ " and mt.nsitecode = "+userInfo.getNmastersitecode()+" "
				+ " and rum.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and mt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + conditionsstr
				+ ") A";

		String lstData1 = (String) jdbcUtilityFunction.queryForObject(getMaterial, String.class,jdbcTemplate);

		List<Map<String, Object>> lstResultUsedMaterial = new ArrayList<>();
		if (lstData1 != null) {
			lstResultUsedMaterial = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(lstData1, userInfo,true, -1, "");
		} else {
			returnMap.put(keyvalue, lstResultUsedMaterial);
			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		}

		if (nresultUsedMaterialCode > 0) {

			ResultUsedInstrument validationObj = new ResultUsedInstrument();

			if (lstResultUsedMaterial.isEmpty()) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("ALREADYDELETED", userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
			} else {

				int nmaterialtypecode = (int) lstResultUsedMaterial.get(0).get("nmaterialtypecode");
				int nmaterialcategorycode = (int) lstResultUsedMaterial.get(0).get("nmaterialcategorycode");
				int nmaterialcode = (int) lstResultUsedMaterial.get(0).get("nmaterialcode");
				int nsectioncode = (int) lstResultUsedMaterial.get(0).get("nsectioncode");
				int ninventorycode = (int) lstResultUsedMaterial.get(0).get("ninventorycode");
				int ntestgroupTestcode = (int) lstResultUsedMaterial.get(0).get("ntestgrouptestcode");

				List<MaterialType> lstmaterialtype = (List<MaterialType>)getREMaterialType(ntestgroupTestcode,userInfo).getBody();
				returnMap.put("MaterialType", lstmaterialtype);

				returnMap.putAll(getREMaterialCategoryByType(ntestgroupTestcode, (short) nmaterialtypecode, userInfo, (short) nsectioncode));
				returnMap.putAll(getREMaterialByCategory(ntestgroupTestcode,(short) nmaterialtypecode, nmaterialcategorycode, nsectioncode,userInfo));
				returnMap.putAll(getREMaterialInvertoryByMaterial(ntestgroupTestcode,nmaterialcode, nsectioncode, userInfo));
				returnMap.putAll((Map<String, Object>) getAvailableMaterialQuantity(ntestgroupTestcode,ninventorycode, nsectioncode, userInfo).getBody());
			}

		}

		returnMap.put(keyvalue, lstResultUsedMaterial);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> updateResultUsedMaterial(ResultUsedMaterial objResultUsedMaterial,MaterialInventoryTrans objMaterialInventoryTrans, 
			Map<String, Object> inputmap, UserInfo userInfo) throws Exception {

		JSONObject auditOldData = new JSONObject();
		JSONObject auditNewData = new JSONObject();
		JSONObject auditActionType = new JSONObject();
		List<Map<String, Object>> lstdata = new ArrayList<>();
		List<Map<String, Object>> lstdataold = new ArrayList<>();
		List<Map<String, Object>> lstaudit = new ArrayList<>();
		ObjectMapper objMapper = new ObjectMapper();

		Map<String, Object> returnMap = new HashMap<>();

		String str = " select * from resultusedmaterial where nresultusedmaterialcode="
				+ objResultUsedMaterial.getNresultusedmaterialcode() + " and nsitecode = "+userInfo.getNtranssitecode()+" "
				+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		List<Map<String, Object>> lst = jdbcTemplate.queryForList(str);

		if (lst.size() > 0) {
			String stransactiontestcode = String.valueOf(objResultUsedMaterial.getNtransactiontestcode());

			returnMap.putAll((Map<String, Object>) getResultUsedMaterial(stransactiontestcode,objResultUsedMaterial.getNresultusedmaterialcode(), userInfo).getBody());
			int nresultusedmaterialcode = objResultUsedMaterial.getNresultusedmaterialcode();
			lstdataold = (List<Map<String, Object>>) returnMap.get("ResultUsedMaterial");

			lstaudit.add((Map<String, Object>) (lstdataold.stream().filter(
					t -> (int) ((Map<String, Object>) t).get("nresultusedmaterialcode") == nresultusedmaterialcode)
					.findAny()).get());
			auditOldData.put("resultusedmaterial", lstaudit);
			returnMap.clear();
			lstaudit.clear();

			str = "update resultusedmaterial set nmaterialtypecode=" + objResultUsedMaterial.getNmaterialtypecode()
			+ " ,nmaterialcategorycode=" + objResultUsedMaterial.getNmaterialcategorycode() + ","
			+ " nmaterialcode="+ objResultUsedMaterial.getNmaterialcode() + " ,ninventorycode="+ objResultUsedMaterial.getNinventorycode() + ","
			+ " jsondata = jsondata || '"+ stringUtilityFunction.replaceQuote(objMapper.writeValueAsString(objResultUsedMaterial.getJsondata())) + "'"
			+ " where nresultusedmaterialcode=" + objResultUsedMaterial.getNresultusedmaterialcode() + " and nsitecode "+userInfo.getNtranssitecode()+" "
			+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";

			str = str + " update materialinventorytransaction set nmaterialinventorycode="+ objResultUsedMaterial.getNinventorycode() + ","
					+ " nqtyreceived="+ objMaterialInventoryTrans.getJsondata().get("nqtyused") +", dtransactiondate = '"+dateUtilityFunction.getCurrentDateTime(userInfo)+"',"
					+ " ntztransactiondate = "+userInfo.getNtimezonecode()+",noffsetdtransactiondate = "+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())+" "
					+ " where nresultusedmaterialcode=" + objResultUsedMaterial.getNresultusedmaterialcode() + " and nsitecode "+userInfo.getNtranssitecode()+""
					+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
			jdbcTemplate.execute(str);

			stransactiontestcode = String.valueOf(objResultUsedMaterial.getNtransactiontestcode());
			returnMap.putAll((Map<String, Object>) getResultUsedMaterial(stransactiontestcode,objResultUsedMaterial.getNresultusedmaterialcode(), userInfo).getBody());
			returnMap.put("selectedId", objResultUsedMaterial.getNtransactiontestcode());

			lstdataold = (List<Map<String, Object>>) returnMap.get("ResultUsedMaterial");
			lstaudit.add((Map<String, Object>) (lstdataold.stream().filter(
					t -> (int) ((Map<String, Object>) t).get("nresultusedmaterialcode") == nresultusedmaterialcode)
					.findAny()).get());

			auditNewData.put("resultusedmaterial", lstaudit);
			auditActionType.put("resultusedmaterial", "IDS_EDITMATERIAL");
			auditUtilityFunction.fnJsonArrayAudit(auditOldData, auditNewData, auditActionType, inputmap, false, userInfo);

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("ALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> deleteResultUsedMaterial(final String ntransactiontestcode,
			final int nresultusedmaterialcode, Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		JSONObject auditOldData = new JSONObject();
		JSONObject auditNewData = new JSONObject();
		JSONObject auditActionType = new JSONObject();
		List<Map<String, Object>> lstdata = new ArrayList<>();
		List<Map<String, Object>> lstdataold = new ArrayList<>();
		List<Map<String, Object>> lstaudit = new ArrayList<>();

		Map<String, Object> returnMap = new HashMap<>();
		String str = " select * from resultusedmaterial where nresultusedmaterialcode=" + nresultusedmaterialcode
				+ " and nsitecode  = "+userInfo.getNtranssitecode()+" and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		List<ResultUsedMaterial> lst = jdbcTemplate.query(str, new ResultUsedMaterial());

		if (lst.size() > 0) {

			returnMap.putAll((Map<String, Object>) getResultUsedMaterial(ntransactiontestcode.toString(),nresultusedmaterialcode, userInfo).getBody());

			lstdata = (List<Map<String, Object>>) returnMap.get("ResultUsedMaterial");
			returnMap.clear();

			str = "update resultusedmaterial set nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nsitecode = "+userInfo.getNtranssitecode()+" "
					+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nresultusedmaterialcode="+ nresultusedmaterialcode + ";";

			str = str + "update materialinventorytransaction set nstatus="+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dtransactiondate = '"+dateUtilityFunction.getCurrentDateTime(userInfo)+"',"
					+ " ntztransactiondate = "+userInfo.getNtimezonecode()+", noffsetdtransactiondate="+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())+" "
					+ "  where nresultusedmaterialcode="+ nresultusedmaterialcode + " and nsitecode = "+userInfo.getNtranssitecode()+" "
					+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";" ;

			jdbcTemplate.execute(str);

			List<String> lstaction = new ArrayList<>();
			auditActionType.put("resultusedmaterial", "IDS_DELETEMATERIAL");
			returnMap.putAll((Map<String, Object>) getResultUsedMaterial(ntransactiontestcode.toString(),
					nresultusedmaterialcode, userInfo).getBody());

			lstaudit.add((Map<String, Object>) (lstdata.stream().filter(t -> (int) ((Map<String, Object>) t).get("nresultusedmaterialcode") == nresultusedmaterialcode)
					.findAny()).get());
			auditOldData.put("resultusedmaterial", lstaudit);
			auditUtilityFunction.fnJsonArrayAudit(auditOldData, null, auditActionType, inputMap, false, userInfo);

			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("ALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}

	@Override
	public ResponseEntity<Object> testInitiated(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
		LocalDateTime now = LocalDateTime.now();  

		LOGGER.info("testInitiated Start Time:-->"+dtf.format(now));

		int ntesthistorycode = (int) inputMap.get("ntesthistorycode");
		int nchaincustodycode = (int) inputMap.get("nchaincustodycode");
		String stransactiontestcode = (String) inputMap.get("transactiontestcode");
		String npreregno = (String) inputMap.get("npreregno");
		String stesthistorycode = "";
		String ntransactiontestcode = "";
		JSONObject auditNewData = new JSONObject();
		JSONObject auditOldData = new JSONObject();
		JSONObject auditActionType = new JSONObject();
		boolean subSampleNeeded = (boolean) inputMap.get("nneedsubsample");
		boolean jobAllocationNeeded = (boolean) inputMap.get("nneedjoballocation");  //Added by sonia on 06th Sep 2024 for JIRA ID:ALPD-4769
		boolean myJobsNeeded = 	(boolean) inputMap.get("nneedmyjob");	//Added by sonia on 06th Sep 2024 for JIRA ID:ALPD-4769
		String custodyInsertQuery ="";

		List<RegistrationTestHistory> filteredTestList = validateTestStatus(stransactiontestcode,
				(int) inputMap.get("ncontrolcode"), userInfo,(int)inputMap.get("nneedReceivedInLab"));

		if (!filteredTestList.isEmpty()) {
			stesthistorycode = filteredTestList.stream().map(object -> String.valueOf(object.getNtesthistorycode()))
					.collect(Collectors.joining(","));
		}


		if (!filteredTestList.isEmpty()) {
			ntransactiontestcode = filteredTestList.stream().map(object -> String.valueOf(object.getNtransactiontestcode()))
					.collect(Collectors.joining(","));
		}

		//Added by sonia on 06th Sep 2024 for JIRA ID:ALPD-4769
		Integer transactionStatus = Enumeration.TransactionStatus.REGISTERED.gettransactionstatus();
		if(jobAllocationNeeded && myJobsNeeded) {
			transactionStatus = Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus();
		}else if(!jobAllocationNeeded && myJobsNeeded) {
			transactionStatus = Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus();
		}else if(jobAllocationNeeded && ! myJobsNeeded) {
			transactionStatus = Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus();
		}else if(!jobAllocationNeeded && !myJobsNeeded) {
			transactionStatus = Enumeration.TransactionStatus.REGISTERED.gettransactionstatus();
		}

		inputMap.put("sintegrationpreregno",npreregno);
		inputMap.put("sintegrationtest",ntransactiontestcode);
		inputMap.put("svalidationstatus",transactionStatus); //Added by sonia on 06th Sep 2024 for JIRA ID:ALPD-4769
		transactionDAOSupport.createIntegrationRecord(inputMap, userInfo);

		//commented by sathish JIRA-4682
		//insertsdmslabsheetmaster(npreregno,ntransactiontestcode,userInfo);
		// audit old start
		final String queryold = "select r.ntesthistorycode ,r.ntransactiontestcode ,r.npreregno ,"
				+ "r.ntransactionstatus,t.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "' as stransdisplaystatus,rt.jsondata->>'stestsynonym' stestsynonym,ra.sarno,rs.ssamplearno,"
				+ "rt.jsondata->>'ssectionname' ssectionname,rt.jsondata->>'smethodname' smethodname "
				+ "  from registrationtest rt,registrationarno ra,registrationsamplearno rs,"
				+ " registrationtesthistory r,transactionstatus t where rt.npreregno=ra.npreregno"
				+ " and rs.npreregno=ra.npreregno and rs.npreregno=rt.npreregno and rt.npreregno=r.npreregno"
				+ " and rt.ntransactionsamplecode=r.ntransactionsamplecode and rs.ntransactionsamplecode=r.ntransactionsamplecode and ra.npreregno=r.npreregno"
				+ " and rs.ntransactionsamplecode=rt.ntransactionsamplecode and rt.ntransactiontestcode=r.ntransactiontestcode"
				+ " and rt.ntransactiontestcode in ( " + stransactiontestcode + ") and t.ntranscode=r.ntransactionstatus"
				+ " and r.ntesthistorycode in (  select max(ntesthistorycode) from  registrationtesthistory where ntransactiontestcode IN ( "
				+ stransactiontestcode + " ) and nsitecode ="+userInfo.getNtranssitecode()+" "
				+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+") "
				+ " and rt.nsitecode ="+userInfo.getNtranssitecode()+" "
				+ " and ra.nsitecode ="+userInfo.getNtranssitecode()+" "
				+ " and rs.nsitecode ="+userInfo.getNtranssitecode()+" "
				+ " and r.nsitecode ="+userInfo.getNtranssitecode()+" "
				+ " and rt.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and ra.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rs.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and r.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and t.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " order by 1 asc;";

		List<RegistrationTestHistory> lstrpold = jdbcTemplate.query(queryold,	new RegistrationTestHistory()); 
		// audit old end
		jdbcTemplate.execute(
				"insert into registrationtesthistory ( ntesthistorycode,ntransactiontestcode,ntransactionsamplecode,npreregno,nformcode,"
						+ " ntransactionstatus,nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments,dtransactiondate,noffsetdtransactiondate,ntransdatetimezonecode,nsampleapprovalhistorycode,nstatus,nsitecode) "
						+ " (select " + (ntesthistorycode) + "+Rank()over(order by ntesthistorycode)"
						+ " ntesthistorycode,ntransactiontestcode,ntransactionsamplecode,npreregno,"
						+ userInfo.getNformcode() + " nformcode," + Enumeration.TransactionStatus.INITIATED.gettransactionstatus() + " ntransactionstatus,"
						+ userInfo.getNusercode() + " nusercode, " + "" + userInfo.getNuserrole() + " nuserrolecode,"
						+ userInfo.getNdeputyusercode() + " ndeputyusercode," + userInfo.getNdeputyuserrole()
						+ " ndeputyuserrolecode,N'" +stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "' scomments,'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo)+ "'dtransactiondate,"+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())+","+userInfo.getNtimezonecode()+", -1 nsampleapprovalhistorycode ,"
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus, "+userInfo.getNtranssitecode()+" nsitecode "
						+ " from registrationtesthistory where ntesthistorycode in (" + stesthistorycode + "));");


		if( (!(boolean) inputMap.get("nneedjoballocation") && !(boolean) inputMap.get("nneedmyjob")) && ((int) inputMap.get("needjoballocationandmyjob") == Enumeration.TransactionStatus.YES.gettransactionstatus()))
		{
			jdbcTemplate.execute("update joballocation set nusercode = " + userInfo.getNusercode()+ " where ntransactiontestcode in (" + stransactiontestcode + ");");
		}

		if(subSampleNeeded) {
			custodyInsertQuery= " Insert into chaincustody (nchaincustodycode,nformcode,ntablepkno,stablepkcolumnname,stablename,"
					+ " sitemno,ntransactionstatus,nusercode,nuserrolecode,dtransactiondate,ntztransactiondate,"
					+ " noffsetdtransactiondate,sremarks,nsitecode,nstatus)"
					+ " select ("+nchaincustodycode+")+ rank() over(order by rt.npreregno,rt.ntransactionsamplecode,rt.ntransactiontestcode) as nchaincustodycode, "
					+ " "+Enumeration.QualisForms.RESULTENTRY.getqualisforms()+" as nformcode,rsa.ntransactionsamplecode as ntablepkno, 'ntransactionsamplecode' as stablepkcolumnname, "
					+ "'registrationsamplearno' as stablename,rsa.ssamplearno as sitemno,"+Enumeration.TransactionStatus.INITIATED.gettransactionstatus()+" as ntransactionstatus,"
					+ " "+userInfo.getNusercode()+" as nusercode,"+userInfo.getNuserrole()+" as nuserrolecode, "
					+ "'"+dateUtilityFunction.getCurrentDateTime(userInfo)+"' as dtransactiondate,"+userInfo.getNtimezonecode()+" as ntztransactiondate,"+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())+" as noffsetdtransactiondate, "
					+ "'"+commonFunction.getMultilingualMessage(Enumeration.TransAction.TESTINITIATED.getTransAction(), userInfo.getSlanguagefilename())+"'||' '||tgt.stestsynonym ||'['|| rt.ntestrepeatno || ']' || '['|| rt.ntestretestno ||']' ||' '||'"+commonFunction.getMultilingualMessage("IDS_IN", userInfo.getSlanguagefilename())+"'||' '|| s.ssectionname "
					+ " sremarks ,"+userInfo.getNtranssitecode()+" as nsitecode,"+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" as nstatus "
					+ " from registrationarno ra, registrationsamplearno rsa,registrationtest rt, "
					+ " testgrouptest tgt, "
					+ " section s "
					+ " where ra.npreregno = rsa.npreregno "
					+ " and ra.npreregno = rt.npreregno "
					+ " and rsa.npreregno = rt.npreregno "
					+ " and rsa.ntransactionsamplecode =rt.ntransactionsamplecode "
					+ " and rt.nsectioncode =s.nsectioncode "
					+ " and rt.ntestgrouptestcode = tgt.ntestgrouptestcode "
					+ " and rt.ntestcode =tgt.ntestcode "
					+ " and rt.npreregno  in ("+npreregno+") "
					// + "and rt.ntransactiontestcode in("+stransactiontestcode+")  "
					+ " and rt.ntransactiontestcode in("+ntransactiontestcode+")  "							  
					+ " and ra.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and rsa.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
					+ " and rt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and tgt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";
		}else {
			custodyInsertQuery= " Insert into chaincustody (nchaincustodycode,nformcode,ntablepkno,stablepkcolumnname,stablename,"
					+ " sitemno,ntransactionstatus,nusercode,nuserrolecode,dtransactiondate,ntztransactiondate,"
					+ " noffsetdtransactiondate,sremarks,nsitecode,nstatus)"
					+ " select ("+nchaincustodycode+")+ rank() over(order by rt.npreregno,rt.ntransactionsamplecode,rt.ntransactiontestcode) as nchaincustodycode, "
					+ " "+Enumeration.QualisForms.RESULTENTRY.getqualisforms()+" as nformcode,ra.npreregno as ntablepkno, 'npreregno' as stablepkcolumnname, "
					+ "'registrationarno' as stablename,ra.sarno as sitemno,"+Enumeration.TransactionStatus.INITIATED.gettransactionstatus()+" as ntransactionstatus,"
					+ " "+userInfo.getNusercode()+" as nusercode,"+userInfo.getNuserrole()+" as nuserrolecode, "
					+ "'"+dateUtilityFunction.getCurrentDateTime(userInfo)+"' as dtransactiondate,"+userInfo.getNtimezonecode()+" as ntztransactiondate,"+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())+" as noffsetdtransactiondate, "
					+ "'"+commonFunction.getMultilingualMessage(Enumeration.TransAction.TESTINITIATED.getTransAction(), userInfo.getSlanguagefilename())+"'||' '||tgt.stestsynonym ||'['|| rt.ntestrepeatno || ']' || '['|| rt.ntestretestno ||']' ||' '||'"+commonFunction.getMultilingualMessage("IDS_IN", userInfo.getSlanguagefilename())+"'||' '|| s.ssectionname "
					+ " sremarks ,"+userInfo.getNtranssitecode()+" as nsitecode,"+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" as nstatus "
					+ "from registrationarno ra,registrationtest rt, testgrouptest tgt, section s "
					+ "where ra.npreregno = rt.npreregno "
					+ "and rt.nsectioncode =s.nsectioncode "
					+ "and rt.ntestgrouptestcode = tgt.ntestgrouptestcode "
					+ "and rt.ntestcode =tgt.ntestcode "
					+ "and rt.npreregno  in ("+npreregno+") "
					//+ "and rt.ntransactiontestcode in("+stransactiontestcode+")  "
					+ "and rt.ntransactiontestcode in("+ntransactiontestcode+")  "
					+ "and ra.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ "and rt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and tgt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";
		}
		jdbcTemplate.execute(custodyInsertQuery);

		final String query =  "select r.ntesthistorycode ,r.ntransactiontestcode ,r.npreregno ,"
				+ " r.ntransactionstatus,t.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "' as stransdisplaystatus,rt.jsondata->>'stestsynonym' stestsynonym,ra.sarno,rs.ssamplearno,"
				+ " rt.jsondata->>'ssectionname' ssectionname,rt.jsondata->>'smethodname' smethodname "
				+ " from registrationtest rt,registrationarno ra,registrationsamplearno rs,"
				+ " registrationtesthistory r,transactionstatus t where rt.npreregno=ra.npreregno"
				+ " and rs.npreregno=ra.npreregno and rs.npreregno=rt.npreregno and rt.npreregno=r.npreregno"
				+ " and rt.ntransactionsamplecode=r.ntransactionsamplecode and rs.ntransactionsamplecode=r.ntransactionsamplecode and ra.npreregno=r.npreregno"
				+ " and rs.ntransactionsamplecode=rt.ntransactionsamplecode and rt.ntransactiontestcode=r.ntransactiontestcode"
				+ " and rt.ntransactiontestcode in ( " + stransactiontestcode + ") and t.ntranscode=r.ntransactionstatus"
				+ " and r.ntesthistorycode in (  select max(ntesthistorycode) from  registrationtesthistory where ntransactiontestcode IN ( "
				+ stransactiontestcode + ") and nsitecode = "+userInfo.getNtranssitecode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ) "
				+ " and rt.nsitecode = "+ userInfo.getNtranssitecode() + " "
				+ " and ra.nsitecode = "+ userInfo.getNtranssitecode() + " "
				+ " and rs.nsitecode = "+ userInfo.getNtranssitecode() + " "
				+ " and r.nsitecode = " + userInfo.getNtranssitecode() + " "
				+ " and rt.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and ra.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rs.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and r.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and t.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " order by 1 asc;";
		
		List<RegistrationTestHistory> lstrp = jdbcTemplate.query(query, new RegistrationTestHistory());
		
		auditNewData.put("registrationtest", lstrp);
		auditOldData.put("registrationtest", lstrpold);
		auditActionType.put("registrationtest", "IDS_TESTINITIATE");
		//insertsdmslabsheetmaster(npreregno,userInfo);
		auditUtilityFunction.fnJsonArrayAudit(auditOldData, auditNewData, auditActionType, inputMap, false, userInfo);


		now = LocalDateTime.now();  

		LOGGER.info("testInitiated End Time:-->"+dtf.format(now));

		return new ResponseEntity<>(getResultEntryDetails(inputMap, userInfo).getBody(), HttpStatus.OK);
	}


	public int validateResultUsedTask(final int nresultusedtaskcode,UserInfo userinfo) throws Exception
	{
		final String str = "SELECT count(*) FROM resultusedtasks rut WHERE rut.nresultusedtaskcode = "+nresultusedtaskcode+" and rut.nstatus=1 "
				+ " and rut.nsitecode = "+userinfo.getNtranssitecode()+"";

		final int count = jdbcTemplate.queryForObject(str, Integer.class);

		return count;
	}



	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getResultUsedTask(final String ntransactiontestcode, final int nresultusedtaskcode,
			UserInfo userInfo) throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		String conditionsstr = "";
		String str = "";
		String keyValue = "";

		if (ntransactiontestcode != null && ntransactiontestcode.split(",").length > 0
				&& !ntransactiontestcode.equals("")) {
			conditionsstr = " and rut.ntransactiontestcode in(" + ntransactiontestcode + ")";
			keyValue = "ResultUsedTasks";
		} else if (nresultusedtaskcode > 0) {

			final int count = validateResultUsedTask(nresultusedtaskcode,userInfo);

			if(count == 0)
			{
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
			else
			{
				conditionsstr = " and rut.nresultusedtaskcode=(" + nresultusedtaskcode + ")";
				keyValue = "EditResultUsedTasks";

			}
		}

		str = "SELECT json_AGG(A.JSONDATA) FROM (SELECT to_json(RUT.*)::JSONB||rut.jsondata AS JSONDATA"
				+ " FROM resultusedtasks rut  WHERE rut.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" " + conditionsstr + ") A";

		String lstData1 = (String) jdbcUtilityFunction.queryForObject(str, String.class,jdbcTemplate);

		List<Map<String, Object>> lstusedtask = new ArrayList<>();
		if (lstData1 != null) {
			lstusedtask = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(lstData1, userInfo, true, -1,
					"");
		}
		returnMap.put(keyValue, lstusedtask);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> createResultUsedTasks(List<ResultUsedTasks> listTest,final String ntransactiontestcode, final int nregtypecode, final int nregsubtypecode,
			final int ndesigntemplatemappingcode, final String transactiontestcode, UserInfo userInfo) throws Exception {

		Map<String, Object> returnMap = new HashMap<>();
		JSONObject auditOldData = new JSONObject();
		JSONObject auditActionType = new JSONObject();
		List<Map<String, Object>> lstdata = new ArrayList<>();
		List<Map<String, Object>> lstaudit = new ArrayList<>();
		List<Integer> lstnresultusedtaskcode = new ArrayList<>();
		ObjectMapper objMapper = new ObjectMapper();

		final String lockquery = "lock lockresultusedtasks" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(lockquery);

		String str = "";
		str = "select nsequenceno from seqnoregistration where stablename =N'resultusedtasks' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
		int nresultusedtaskcode = (int) jdbcUtilityFunction.queryForObject(str, Integer.class,jdbcTemplate);

		StringBuilder sb = new StringBuilder();
		sb.append(
				"insert into resultusedtasks (nresultusedtaskcode,ntransactiontestcode,npreregno,jsondata,nsitecode,nstatus) values");
		for (ResultUsedTasks objResultUsedTasks : listTest) {
			nresultusedtaskcode++;
			lstnresultusedtaskcode.add(nresultusedtaskcode);
			sb.append("(" + nresultusedtaskcode + "," + objResultUsedTasks.getNtransactiontestcode() + ","
					+ objResultUsedTasks.getNpreregno() + ",'"
					+ stringUtilityFunction.replaceQuote(objMapper.writeValueAsString(objResultUsedTasks.getJsondata())) + "',"+userInfo.getNtranssitecode()+","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),");

		}
		String qury = sb.substring(0, sb.length() - 1);
		jdbcTemplate.execute(qury);

		str = " update seqnoregistration set nsequenceno=" + nresultusedtaskcode+ " where stablename=N'resultusedtasks' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
		jdbcTemplate.execute(str);

		Map<String, Object> objMap = new HashMap<>();
		objMap.put("nregtypecode", nregtypecode);
		objMap.put("nregsubtypecode", nregsubtypecode);
		objMap.put("ndesigntemplatemappingcode", ndesigntemplatemappingcode);

		returnMap.putAll((Map<String, Object>) getResultUsedTask(transactiontestcode, -1, userInfo).getBody());

		lstdata = (List<Map<String, Object>>) returnMap.get("ResultUsedTasks");

		lstaudit.addAll((List<Map<String, Object>>) (lstdata.stream()
				.filter(t -> lstnresultusedtaskcode.contains(((Map<String, Object>) t).get("nresultusedtaskcode")))
				.collect(Collectors.toList())));

		auditOldData.put("resultusedtasks", lstaudit);
		auditActionType.put("resultusedtasks", "IDS_ADDRESULTUSEDTASK");

		auditUtilityFunction.fnJsonArrayAudit(auditOldData, null, auditActionType, objMap, false, userInfo);

		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> updateResultUsedTasks(final ResultUsedTasks objResultUsedTasks,final int nregtypecode, final int nregsubtypecode, 
			final int ndesigntemplatemappingcode,final String ntransactiontestcode, final UserInfo userInfo) throws Exception {

		JSONObject auditNewData = new JSONObject();
		JSONObject auditOldData = new JSONObject();
		JSONObject auditActionType = new JSONObject();
		List<Map<String, Object>> lstdata = new ArrayList<>();
		List<Map<String, Object>> lstaudit = new ArrayList<>();
		ObjectMapper objMapper = new ObjectMapper();

		Map<String, Object> returnMap = new HashMap<>();
		ResultUsedTasks validationObj = new ResultUsedTasks();
		objMapper.registerModule(new JavaTimeModule());

		final int count = validateResultUsedTask(objResultUsedTasks.getNresultusedtaskcode(),userInfo);


		if(count > 0) {

			returnMap.putAll((Map<String, Object>) getResultUsedTask(ntransactiontestcode, -1, userInfo).getBody());
			int nresultusedtaskcode = objResultUsedTasks.getNresultusedtaskcode();
			lstdata = (List<Map<String, Object>>) returnMap.get("ResultUsedTasks");
			lstaudit.add((Map<String, Object>) (lstdata.stream()
					.filter(t -> (int) ((Map<String, Object>) t).get("nresultusedtaskcode") == nresultusedtaskcode)
					.findAny()).get());

			auditOldData.put("resultusedtasks", lstaudit);
			returnMap.clear();
			lstaudit.clear();

			String str = "";

			str = "update resultusedtasks set " + "jsondata = jsondata || '"
					+ stringUtilityFunction.replaceQuote(objMapper.writeValueAsString(objResultUsedTasks.getJsondata())) + "' where nresultusedtaskcode="
					+ objResultUsedTasks.getNresultusedtaskcode() + ";";
			jdbcTemplate.execute(str);

			returnMap.putAll((Map<String, Object>) getResultUsedTask(ntransactiontestcode, -1, userInfo).getBody());
			returnMap.put("selectedId", objResultUsedTasks.getNresultusedtaskcode());

			List<ResultUsedTasks> lstResultUsedTasksafter = new ArrayList<>();
			List<ResultUsedTasks> lstResultUsedTasksbefore = new ArrayList<>();
			objResultUsedTasks.setNpreregno(validationObj.getNpreregno());
			objResultUsedTasks.setNtransactiontestcode(validationObj.getNtransactiontestcode());
			lstResultUsedTasksafter.add(objResultUsedTasks);
			lstResultUsedTasksbefore.add(validationObj);

			List<Object> lstaftersave = new ArrayList<>();
			List<Object> lstbeforesave = new ArrayList<>();

			lstaftersave.add(lstResultUsedTasksafter);
			lstbeforesave.add(lstResultUsedTasksbefore);
			Map<String, Object> objMap = new HashMap<>();
			objMap.put("nregtypecode", nregtypecode);
			objMap.put("nregsubtypecode", nregsubtypecode);
			objMap.put("ndesigntemplatemappingcode", ndesigntemplatemappingcode);

			lstdata = (List<Map<String, Object>>) returnMap.get("ResultUsedTasks");
			lstaudit.add((Map<String, Object>) (lstdata.stream().filter(t -> (int) ((Map<String, Object>) t).get("nresultusedtaskcode") == nresultusedtaskcode)
					.findAny()).get());

			auditNewData.put("resultusedtasks", lstaudit);
			auditActionType.put("resultusedtasks", "IDS_EDITRESULTUSEDTASK");
			auditUtilityFunction.fnJsonArrayAudit(auditOldData, auditNewData, auditActionType, objMap, false, userInfo);

		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> deleteResultUsedTasks(final int nresultusedtaskcode, final int nregtypecode,final int nregsubtypecode, 
			final int ndesigntemplatemappingcode, final String ntransactiontestcode,final UserInfo userInfo) throws Exception {

		JSONObject auditNewData = new JSONObject();
		JSONObject auditOldData = new JSONObject();
		JSONObject auditActionType = new JSONObject();
		List<Map<String, Object>> lstdata = new ArrayList<>();
		List<Map<String, Object>> lstaudit = new ArrayList<>();

		Map<String, Object> returnMap = new HashMap<>();
		Map<String, Object> objMap = new HashMap<>();
		ResultUsedTasks validationObj = new ResultUsedTasks();
		List<Object> lstaftersave = new ArrayList<>();

		final int count = validateResultUsedTask(nresultusedtaskcode,userInfo);

		if(count > 0)
		{
			returnMap.putAll((Map<String, Object>) getResultUsedTask(ntransactiontestcode, -1, userInfo).getBody());
			lstdata = (List<Map<String, Object>>) returnMap.get("ResultUsedTasks");
			returnMap.clear();

			final String str = "update resultusedtasks set nstatus="+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " "
					+ "where nresultusedtaskcode="+ nresultusedtaskcode + ";";
			jdbcTemplate.execute(str);

			returnMap.putAll((Map<String, Object>) getResultUsedTask(ntransactiontestcode, -1, userInfo).getBody());

			List<ResultUsedTasks> lstResultUsedTasksafter = new ArrayList<>();

			lstResultUsedTasksafter.add(validationObj);
			lstaftersave.add(lstResultUsedTasksafter);
			objMap.put("nregtypecode", nregtypecode);
			objMap.put("nregsubtypecode", nregsubtypecode);
			objMap.put("ndesigntemplatemappingcode", ndesigntemplatemappingcode);
			lstaudit.add((Map<String, Object>) (lstdata.stream()
					.filter(t -> (int) ((Map<String, Object>) t).get("nresultusedtaskcode") == nresultusedtaskcode)
					.findAny()).get());
			auditOldData.put("resultusedtasks", lstaudit);

			auditActionType.put("resultusedtasks", "IDS_DELETERESULTUSEDTASK");

			auditUtilityFunction.fnJsonArrayAudit(auditOldData, null, auditActionType, objMap, false, userInfo);

			return new ResponseEntity<>(returnMap, HttpStatus.OK);

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public Map<String, Object> seqNoGetforTestStart(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Map<String, Object> returnmap = new HashMap<>();
		String stransactiontestcode = (String) inputMap.get("transactiontestcode");
		List<RegistrationTestHistory> filteredTestList = validateTestStatus(stransactiontestcode,
				(int) inputMap.get("ncontrolcode"), userInfo,(int) inputMap.get("nneedReceivedInLab"));

		String sQuery = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		sQuery = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		sQuery = "lock lockcancelreject" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery); 

		if (!filteredTestList.isEmpty()) {

			final List<String> lst = Arrays.asList("registrationtesthistory","chaincustody");

			final String getSeqNo = "select stablename,nsequenceno from seqnoregistration where stablename in (N'registrationtesthistory','chaincustody') and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
			List<SeqNoRegistration> lstSeqNo = jdbcTemplate.query(getSeqNo, new SeqNoRegistration());

			Map<String, Integer> seqMap = lstSeqNo.stream().collect(Collectors.toMap(SeqNoRegistration::getStablename, SeqNoRegistration::getNsequenceno));
			int ntesthistorycode = seqMap.get("registrationtesthistory");
			int nchaincustodycode = seqMap.get("chaincustody");

			returnmap.put("ntesthistorycode", ntesthistorycode);
			returnmap.put("nchaincustodycode", nchaincustodycode);

			int testhistorycode = ntesthistorycode + filteredTestList.size();
			int chaincustodycode = nchaincustodycode + filteredTestList.size();


			String Qry = "update seqnoregistration set nsequenceno=" + (testhistorycode)
					+ " where stablename = N'registrationtesthistory' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
			jdbcTemplate.execute(Qry);

			String CustodyQry = "update seqnoregistration set nsequenceno=" + (chaincustodycode)+ " where stablename = N'chaincustody' "
					+ " and nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
			jdbcTemplate.execute(CustodyQry);

			returnmap.put("rtn", Enumeration.ReturnStatus.SUCCESS.getreturnstatus());

		} else {
			returnmap.put("rtn", "IDS_SELECTTESTTOINITIATE");
		}
		return returnmap;
	}


	@Override
	public ResponseEntity<Object> getChecklistdesign(final int nchecklistversioncode, final String ntransactiontestcode,
			final int ntransactionresultcode, int controlCode, final UserInfo userInfo,final int nneedReceivedInLab) throws Exception {

		Map<String, Object> returnMap = new HashMap<>();
		String str = "";
		int napprovalstatus = 0;

		List<RegistrationTestHistory> lstvalidationtest = validateTestStatus(ntransactiontestcode, controlCode,
				userInfo,nneedReceivedInLab);

		str = "select clv.nchecklistversioncode,c.nchecklistcode,vqb.nchecklistqbcode,lqb.nchecklistcomponentcode,"
				+ " lqb.squestiondata,lqb.squestion,lqb.nmandatory as nmandatoryfield,"
				+ " case when (rc.jsondata->>(vqb.nchecklistversionqbcode)::character varying) isnull then "
				+ " case when lqb.nchecklistcomponentcode =7 then to_char((vct.sdefaultvalue)::timestamp without time zone, 'dd/MM/yyyy HH24:mi:ss'::text) else vct.sdefaultvalue end "
				+ " else case when lqb.nchecklistcomponentcode =7 then "
				+ " to_char((rc.jsondata ->> (vqb.nchecklistversionqbcode)::character varying)::timestamp without time zone,'dd/MM/yyyy HH24:mi:ss'::text) "
				+ " else rc.jsondata->>(vqb.nchecklistversionqbcode)::character varying end  end   sdefaultvalue "

					//+ "case when (rc.jsondata->>(vqb.nchecklistversionqbcode)::character varying) isnull then vct.sdefaultvalue else rc.jsondata->>(vqb.nchecklistversionqbcode)::character varying end  as sdefaultvalue"// case
					// ISNULL(rc.sdefaultvalue,'null')
					// when
					// 'null'
					// then
					// vct.sdefaultvalue
					// when
					// NULL
					// then
					// ''
					// else
					// rc.sdefaultvalue
					// end
					// as
					// sdefaultvalue,"
					+ ", vqb.nchecklistversionqbcode "
					+ " from checklistqb lqb,checklist c,checklistversion clv,checklistversionqb vqb"
					+ " left outer join checklistversiontemplate vct on vqb.nchecklistversionqbcode=vct.nchecklistversionqbcode "
					+ " and vct.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and vct.nsitecode = "+userInfo.getNtranssitecode()+""
					+ " left outer join resultchecklist rc on rc.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and rc.ntransactionresultcode= "+ ntransactionresultcode +" and rc.nsitecode = "+userInfo.getNtranssitecode()+""
					+ " where clv.nchecklistcode=c.nchecklistcode and vqb.nchecklistversioncode=clv.nchecklistversioncode "
					+ " and vqb.nchecklistqbcode=lqb.nchecklistqbcode "
					+ " and lqb.nsitecode = "+userInfo.getNmastersitecode()+""
					+ " and c.nsitecode = "+userInfo.getNmastersitecode()+""
					+ " and clv.nsitecode = "+userInfo.getNmastersitecode()+""
					+ " and vqb.nsitecode = "+userInfo.getNmastersitecode()+""
					+ " and clv.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and vqb.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and lqb.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and c.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and clv.nchecklistversioncode="+ nchecklistversioncode;

		// List<ChecklistQB> lstcheckqb = (List<ChecklistQB>) findBySinglePlainSql(str,
		// ChecklistQB.class);

		List<ChecklistQB> lstcheckqb = jdbcTemplate.query(str, new ChecklistQB());

		returnMap.put("ChecklistData", lstcheckqb);
		returnMap.put("selectedId", ntransactionresultcode);

		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> createResultEntryChecklist(final ResultCheckList objResultCheckList,final int ntransactionresultcode, 
			final int npreregno, final String ntransactiontestcode,final String transactiontestcode, int controlCode, UserInfo userInfo, int nregtypecode, 
			int nregsubtypecode,int nseqnoresultchecklistcode, final int ndesigntemplatemappingcode, final int nneedReceivedInLab) throws Exception {

		String sComments="";
		JSONArray mandatoryFieldArray = new JSONArray();
		JSONArray lstauditcapturefeilds = new JSONArray();
		JSONObject auditOldData = new JSONObject();
		JSONObject auditnewData = new JSONObject();
		JSONObject auditActionType = new JSONObject();
		List<Map<String, Object>> lstdata = new ArrayList<>();
		List<ResultParameter> lstaudit = new ArrayList<>();
		JSONObject jsonobjectbefore=new JSONObject();
		Map<String, Object> returnMap = new HashMap<>();
		StringBuilder sb = new StringBuilder();

		ObjectMapper objMapper = new ObjectMapper();

		List<RegistrationTestHistory> lstvalidationtest = validateTestStatus(ntransactiontestcode, controlCode,
				userInfo,nneedReceivedInLab);
		String strbefore =  "SELECT     json_build_object( "
				+ "	 'questionvalues',jsonb_object_agg(  "
				+ "		  lqb.squestion, "
				+ "           "
				+ "CASE WHEN ( rc.jsondata->>(vqb.nchecklistversionqbcode)::character VARYING) isnull THEN vct.sdefaultvalue "
				+ " ELSE rc.jsondata->>(vqb.nchecklistversionqbcode)::character VARYING "
				+ " END  )||json_build_object('sarno',rarno.sarno,'ssamplearno',rsarno.ssamplearno,'stestsynonym',rp.jsondata->>'stestsynonym' "
				+ "	,'sparametersynonym',rp.jsondata->>'sparametersynonym') ::jsonb,'auditcapturefields',json_agg(lqb.squestion),'multilingualfields',json_agg(lqb.squestion) ) "

					+ " FROM checklistqb lqb, checklist c,checklistversion clv, resultparameter rp,registrationarno rarno,registrationtest rgt, "
					+ "	registrationsamplearno rsarno,checklistversionqb vqb "

					+ " LEFT OUTER JOIN checklistversiontemplate vct  ON vqb.nchecklistversionqbcode=vct.nchecklistversionqbcode "
					+ " AND vct.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " LEFT OUTER JOIN resultchecklist rc ON rc.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " AND rc.ntransactionresultcode=  "+ ntransactionresultcode +""

					+ " WHERE  clv.nchecklistcode=c.nchecklistcode  AND vqb.nchecklistversioncode=clv.nchecklistversioncode "
					+ " AND vqb.nchecklistqbcode=lqb.nchecklistqbcode "

					+ " AND clv.nchecklistversioncode= rc.nchecklistversioncode "
					+ " AND rc.ntransactionresultcode = rp.ntransactionresultcode and rsarno.npreregno=rc.npreregno "
					+ " AND rgt.ntransactiontestcode=rp.ntransactiontestcode and rgt.ntransactionsamplecode=rsarno.ntransactionsamplecode and rgt.nstatus=1 "
					+ " AND rarno.npreregno=rc.npreregno "

					+ " AND lqb.nsitecode= "+userInfo.getNmastersitecode()+" "
					+ " AND c.nsitecode= "+userInfo.getNmastersitecode()+" "
					+ " AND clv.nsitecode= "+userInfo.getNmastersitecode()+" "
					+ " AND vqb.nsitecode= "+userInfo.getNmastersitecode()+" "
					+ " AND rp.nsitecode= "+ userInfo.getNtranssitecode()+" "
					+ " AND rarno.nsitecode= "+userInfo.getNtranssitecode()+" "
					+ " AND rgt.nsitecode= "+userInfo.getNtranssitecode()+" "
					+ " AND rsarno.nsitecode= "+userInfo.getNtranssitecode()+" "

					+ " AND lqb.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " AND c.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " AND clv.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " AND vqb.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " AND rp.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " AND rarno.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " AND rgt.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " AND rsarno.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "

					+ " group by rarno.sarno,rsarno.ssamplearno ,rp.jsondata->>'stestsynonym', rp.jsondata->>'sparametersynonym' ;";

		String str = "select * from resultchecklist where ntransactionresultcode = "+ ntransactionresultcode+ " and nstatus = " +
				Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		List<?> lstObject = projectDAOSupport.getMultipleEntitiesResultSetInList(str,jdbcTemplate, ResultCheckList.class);

		List<ResultCheckList> lstResultCheckListCheck = new ArrayList<ResultCheckList>();

		if (!lstObject.isEmpty()) {
			// List<ResultCheckList>
			try {
				jsonobjectbefore=new JSONObject(jdbcTemplate.queryForObject(strbefore, String.class));
			}
			catch (Exception e) {
				jsonobjectbefore=null;
			}
			lstResultCheckListCheck = (List<ResultCheckList>) lstObject.get(0);

			if (lstResultCheckListCheck.size() > 0) {
				jdbcTemplate.execute(
						"delete from resultchecklist where ntransactionresultcode = " + ntransactionresultcode + " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";");
			}
		}

		final String insertQuery = "insert into resultchecklist (nresultchecklistcode,ntransactionresultcode,npreregno,nchecklistversioncode,jsondata,nsitecode,"
				+ " nstatus)  values ";

		String valuesString = "";

		valuesString += "(" + (nseqnoresultchecklistcode) + "," + ntransactionresultcode + "," + npreregno + ", "
				+ objResultCheckList.getNchecklistversioncode() + ",'"
				+ stringUtilityFunction.replaceQuote(objMapper.writeValueAsString(objResultCheckList.getJsondata())) + "',"+userInfo.getNtranssitecode()+","
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
		nseqnoresultchecklistcode++;

		valuesString = valuesString.substring(0, valuesString.length() - 1);
		jdbcTemplate.execute(insertQuery + valuesString);

		str = "SELECT     json_build_object( "
				+ "	 'questionvalues',jsonb_object_agg( lqb.squestion, "
				+ "  CASE WHEN ( rc.jsondata->>(vqb.nchecklistversionqbcode)::character VARYING) isnull THEN vct.sdefaultvalue "
				+ "  ELSE rc.jsondata->>(vqb.nchecklistversionqbcode)::character VARYING END  )"
				+ " ||json_build_object('sarno',rarno.sarno,'ssamplearno',rsarno.ssamplearno,'stestsynonym',rp.jsondata->>'stestsynonym', "
				+ "	'sparametersynonym',rp.jsondata->>'sparametersynonym') ::jsonb,'auditcapturefields',json_agg(lqb.squestion),'multilingualfields',"
				+ " json_agg(lqb.squestion) ) "

					+ " FROM checklistqb lqb, checklist c,checklistversion clv, resultparameter rp, registrationarno rarno,registrationtest rgt, "
					+ "	registrationsamplearno rsarno,checklistversionqb vqb "

					+ " LEFT OUTER JOIN checklistversiontemplate vct ON vqb.nchecklistversionqbcode=vct.nchecklistversionqbcode "
					+ " AND vct.nstatus = 1 "
					+ " LEFT OUTER JOIN resultchecklist rc ON rc.nstatus= 1 AND rc.ntransactionresultcode=  "+ ntransactionresultcode + " "
					+ " WHERE clv.nchecklistcode=c.nchecklistcode AND vqb.nchecklistversioncode=clv.nchecklistversioncode "
					+ " AND vqb.nchecklistqbcode=lqb.nchecklistqbcode "

					+ " AND clv.nchecklistversioncode= rc.nchecklistversioncode AND  rc.ntransactionresultcode = rp.ntransactionresultcode "
					+ " AND rsarno.npreregno=rc.npreregno AND rgt.ntransactiontestcode=rp.ntransactiontestcode "
					+ " AND rgt.ntransactionsamplecode=rsarno.ntransactionsamplecode "
					+ " AND rarno.npreregno=rc.npreregno "

					+ " AND lqb.nsitecode= "+userInfo.getNmastersitecode()+""
					+ " AND c.nsitecode= "+userInfo.getNmastersitecode()+""
					+ " AND clv.nsitecode= "+userInfo.getNmastersitecode()+" "
					+ " AND rp.nsitecode= "+userInfo.getNtranssitecode()+" "
					+ " AND rgt.nsitecode= "+userInfo.getNtranssitecode()+" "
					+ " AND rsarno.nsitecode= "+userInfo.getNtranssitecode()+" "
					+ " AND vqb.nsitecode= "+userInfo.getNmastersitecode()+" "

					+ " AND lqb.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
					+ " AND c.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
					+ " AND clv.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " AND rp.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " AND rgt.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " AND rsarno.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " AND vqb.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " group by rarno.sarno,rsarno.ssamplearno ,rp.jsondata->>'stestsynonym', rp.jsondata->>'sparametersynonym' ";

		JSONObject lstResultCheckListCheckafter = new JSONObject(jdbcTemplate.queryForObject( str,String.class));

		List<Object> lstaftersave = new ArrayList<>();
		List<Object> lstbeforesave = new ArrayList<>();

		lstbeforesave.add(lstResultCheckListCheck);
		lstaftersave.add(lstResultCheckListCheckafter);

		Map<String, Object> objMap = new HashMap<>();
		objMap.put("nregtypecode", nregtypecode);
		objMap.put("nregsubtypecode", nregsubtypecode);
		objMap.put("ndesigntemplatemappingcode", ndesigntemplatemappingcode);

		if (lstResultCheckListCheck.isEmpty()) {

			auditOldData.put("resultchecklist", "IDS_CREATERESULTCHECKLIST");
			auditOldData.put("resultchecklist",  new JSONArray().put(lstResultCheckListCheckafter.get("questionvalues"))); 
			lstauditcapturefeilds.putAll((JSONArray) lstResultCheckListCheckafter.get("auditcapturefields"));

			auditActionType.put("resultchecklist", "IDS_ADDRESULTCHECKLIST");
			mandatoryFieldArray.putAll(lstauditcapturefeilds);
			objMap.put("auditcapturefieldsresultchecklist", lstauditcapturefeilds);
			objMap.put("editmandatoryfieldsresultchecklist", "[{}]");
			objMap.put("multilingualfieldsresultchecklist",mandatoryFieldArray);

			auditUtilityFunction.fnJsonArrayAudit(auditOldData, null, auditActionType, objMap, true, userInfo);

		} else {

			auditOldData.put("resultchecklist",   new JSONArray().put(jsonobjectbefore .get("questionvalues"))); 
			lstauditcapturefeilds.putAll((JSONArray) lstResultCheckListCheckafter.get("auditcapturefields"));
			objMap.put("auditcapturefieldsresultchecklist", lstauditcapturefeilds);
			objMap.put("editmandatoryfieldsresultchecklist","[{}]");
			objMap.put("multilingualfieldsresultchecklist", "[{}]"); 

			auditActionType.put("resultchecklist", "IDS_UPDATERESULTCHECKLIST");
			auditnewData.put("resultchecklist", new JSONArray().put(lstResultCheckListCheckafter.get("questionvalues")));
			auditUtilityFunction.fnJsonArrayAudit(auditOldData, auditnewData, auditActionType, objMap, true, userInfo);

		}
		returnMap.putAll((Map<String, Object>) getTestbasedParameter(transactiontestcode, userInfo).getBody());

		lstaudit.add((ResultParameter) returnMap.get("SelectedTestParameters"));
		auditOldData.put("resultparameter", lstaudit);

		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}


	@Override
	public Map<String, Object> seqNoGetforResultEntryChecklist(ResultCheckList objResultCheckList,
			String ntransactiontestcode, int controlCode, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		Map<String, Object> returnMap = new HashMap<>();
		final String lockquery = "lock resultchecklist" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(lockquery);

		String str = "select nsequenceno from seqnoregistration where stablename = 'resultchecklist';";
		int nseqnoresultchecklistcode = (int) jdbcUtilityFunction.queryForObject(str, Integer.class,jdbcTemplate);
		//ALPD-2886
		nseqnoresultchecklistcode++;
		str = "update seqnoregistration set nsequenceno=" + nseqnoresultchecklistcode+ " where stablename=N'resultchecklist' "
				+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";

		jdbcTemplate.execute(str);

		returnMap.put("nseqnoresultchecklistcode", nseqnoresultchecklistcode);
		returnMap.put("rtn", Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		return returnMap;
	}

	public  ResponseEntity<Object> getAverageResult(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception
	{
		Map<String, Object> returnMap = new HashMap<>();
		int ntransactiontestcode = (int) inputMap.get("ntransactiontestcode");
		int ntransactionsamplecode = (int) inputMap.get("ntransactionsamplecode");
		int ntestparametercode = (int) inputMap.get("ntestparametercode");
		int nroundingdigit = (int) inputMap.get("naverageroundingdigits");
		ObjectMapper objMapper = new ObjectMapper();

		final List<ResultParameter> lstui = objMapper.convertValue(inputMap.get("ResultParameter"),new TypeReference<List<ResultParameter>>() {});

		if(lstui.size()>0)
		{
			final String  averageQuery = "   select max(rp.ntransactionresultcode)ntransactionresultcode,rp.ntestparametercode," + 
					"  COALESCE(max(rp.jsondata->>'sresult')::decimal,0) sresult  " + 
					"  from resultparameter rp,registrationtest rt,registrationtesthistory rth" + 
					"  where  rp.ntransactiontestcode = rt.ntransactiontestcode  " + 
					"  and rth.ntesthistorycode = (select max(ntesthistorycode) from registrationtesthistory rth1 where rth1.ntransactiontestcode = rt.ntransactiontestcode" + 
					"  and rth1.ntransactionsamplecode = rt.ntransactionsamplecode and rth1.nsitecode = "+userInfo.getNtranssitecode()+" "+
					"  and rth1.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" " + 
					"  group by rth1.ntransactiontestcode,rth1.ntransactionsamplecode,rth1.npreregno)" + 
					"  and rt.ntransactionsamplecode = "+ntransactionsamplecode+" " + 
					"  and rp.jsondata->>'sresult' != ''  " + 
					"  and rp.ntestparametercode = "+ntestparametercode+" " + 
					"  and rp.ntransactionstatus != "+Enumeration.TransactionStatus.REJECTED.gettransactionstatus()+" " + 
					"  and rth.ntransactionstatus not in ("+Enumeration.TransactionStatus.REJECTED.gettransactionstatus()+","
					+ " "+Enumeration.TransactionStatus.CANCELED.gettransactionstatus()+","+Enumeration.TransactionStatus.RETEST.gettransactionstatus()+")"
					+ " and rp.nsitecode = "+userInfo.getNtranssitecode()+" "
					+ " and rt.nsitecode = "+userInfo.getNtranssitecode()+" "
					+ " and rth.nsitecode = "+userInfo.getNtranssitecode()+" "
					+ " and rp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and rt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and rth.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+"  group by rt.ntestrepeatno,rt.ntestretestno,rp.ntestparametercode" ;

			List<ResultParameter> listobject = jdbcTemplate.query(averageQuery, new ResultParameter());

			List<ResultParameter> nonMatchUIData = 
					lstui.stream().filter(obj-> listobject.stream().noneMatch(xobj-> xobj.getNtransactionresultcode() == obj.getNtransactionresultcode() && (obj.getSresult().trim()).equals(""))).collect(Collectors.toList());

			List<ResultParameter> nonMatchDBData = 
					listobject.stream().filter(obj-> lstui.stream().noneMatch(xobj-> xobj.getNtransactionresultcode() == obj.getNtransactionresultcode() && (xobj.getSresult().trim()).equals(""))).collect(Collectors.toList());

			List<ResultParameter> matchedData = 
					lstui.stream().filter(obj-> listobject.stream().anyMatch(xobj-> xobj.getNtransactionresultcode() == obj.getNtransactionresultcode() 
					&& !xobj.getSresult().equals(obj.getSresult()) && !(obj.getSresult().trim()).equals(""))).collect(Collectors.toList());

			List<ResultParameter> matchedResultData = 
					lstui.stream().filter(obj-> listobject.stream().anyMatch(xobj-> xobj.getNtransactionresultcode() == obj.getNtransactionresultcode() 
					&& (xobj.getSresult().trim()).equals(obj.getSresult().trim()))).collect(Collectors.toList());

			List<ResultParameter> listobject1 = new ArrayList<>();
			LinkedHashSet<ResultParameter> setter = new LinkedHashSet<ResultParameter>();

			setter.addAll(nonMatchUIData);
			setter.addAll(nonMatchDBData);
			setter.addAll(matchedData);
			setter.addAll(matchedResultData);
			ResultParameter objparameter = new ResultParameter();
			Double average = setter.stream().filter(b-> !b.getSresult().equals("")).mapToDouble(b -> Double.parseDouble(b.getSresult())).average().orElse(0);
			average = (double) Math.round(average * Math.pow(10, nroundingdigit))/Math.pow(10, nroundingdigit);

			objparameter.setSresult(average.toString() );
			returnMap.put("AverageResult", objparameter);
		}
		else
		{
			final String averageQuery = "select ROUND(avg(a.resultvalue)::decimal,"+nroundingdigit+") sresult,a.ntestparametercode from " + 
					" (select rp.ntestparametercode,COALESCE(max(rp.jsondata->>'sresult')::decimal,0) resultvalue" + 
					" from resultparameter rp,registrationtest rt,registrationtesthistory rth where " + 
					" rp.ntransactiontestcode = rt.ntransactiontestcode" + 
					" and rth.ntesthistorycode = (select max(ntesthistorycode) from registrationtesthistory rth1 where rth1.ntransactiontestcode = rt.ntransactiontestcode" + 
					" and rth1.ntransactionsamplecode = rt.ntransactionsamplecode and rth1.nsitecode = "+userInfo.getNtranssitecode()+" "
					+ " and rth1.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" " + 
					"  group by rth1.ntransactiontestcode,rth1.ntransactionsamplecode,rth1.npreregno)" + 
					" and rp.jsondata->>'sresult' != '' " +
					" and rp.ntestparametercode = "+ntestparametercode+" "+
					//" and rp.nparametertypecode = "+Enumeration.ParameterType.NUMERIC.getparametertype()+""+
					" and rp.ntransactionstatus != "+Enumeration.TransactionStatus.REJECTED.gettransactionstatus()+" " + 
					" and rt.ntransactionsamplecode = "+ntransactionsamplecode+"" + 
					" and rth.ntransactionstatus not in ("+Enumeration.TransactionStatus.REJECTED.gettransactionstatus()+","
					+ ""+Enumeration.TransactionStatus.CANCELED.gettransactionstatus()+","+Enumeration.TransactionStatus.RETEST.gettransactionstatus()+")"
					+ " and rp.nsitecode = "+userInfo.getNtranssitecode()+" "
					+ " and rt.nsitecode = "+userInfo.getNtranssitecode()+" "
					+ " and rth.nsitecode = "+userInfo.getNtranssitecode()+" "
					+ " and rp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and rt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and rth.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+" group by rt.ntestrepeatno,rt.ntestretestno,rp.ntestparametercode" + 
					" )a" + 
					" where a.resultvalue > 0" + 
					" group by a.ntestparametercode";

			ResultParameter objparameter = (ResultParameter) jdbcUtilityFunction.queryForObject(averageQuery,  ResultParameter.class,jdbcTemplate);
			returnMap.put("AverageResult", objparameter);
		}
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}


	public void createChainCustody(int nchainCustodyCode,String stransactiontestcode,int ntransactionstatus, final UserInfo userInfo) throws Exception
	{
		final String insertChainCustody = "insert into chaincustody (nchaincustodycode,npreregno,ntransactionsamplecode, ntransactionstatus,nusercode,dtransactiondate,ntztransactiondate," + 
				"noffsetdtransactiondate,sarno, ssamplearno,sremarks, nsitecode,nstatus)"+
				" select "+nchainCustodyCode+"+ rank() over(order by ra.npreregno, rsa.ntransactionsamplecode),ra.npreregno,rsa.ntransactionsamplecode," + 
				" "+ntransactionstatus+","+userInfo.getNusercode()+",'"+dateUtilityFunction.getCurrentDateTime(userInfo)+"',"+userInfo.getNtimezonecode()+","+
				" "+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())+",ra.sarno,rsa.ssamplearno,"+
				" 'Sample used For (' || (rt.jsondata->>'stestsynonym')|| '):' || (rt.jsondata->>'ssectionname'),"+
				" "+userInfo.getNtranssitecode() +","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"" + 
				" from registrationarno ra, registrationsamplearno rsa,registrationtest rt" + 
				" where ra.npreregno = rsa.npreregno" + 
				" and rsa.npreregno = rt.npreregno" + 
				" and rsa.ntransactionsamplecode = rt.ntransactionsamplecode" + 
				" and rt.ntransactiontestcode in ("+stransactiontestcode+")";

		jdbcTemplate.execute(insertChainCustody);
	}


	@Override
	public ResponseEntity<Object> getAdhocParamter(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		Map<String, Object> returnMap = new HashMap<>();
		List<TestGroupTest> lstMergeParamter = new ArrayList<>();

		// For Etica only

		String sTestMasterParam = " select tgtp.*,tgt.ntestcode "
				+ "  from  testgrouptestparameter tgtp,testgrouptest tgt where tgtp.ntestgrouptestcode="+inputMap.get("ntestgrouptestcode")+""
				+ "  and tgtp.ntestgrouptestparametercode not in(select tgtp.ntestgrouptestparametercode "
				+ "  from testgrouptestparameter tgtp,resultparameter rp where tgtp.ntestgrouptestcode="+inputMap.get("ntestgrouptestcode")+""
				+ "  and rp.ntestgrouptestparametercode=tgtp.ntestgrouptestparametercode "
				+ "  and rp.ntransactiontestcode="+inputMap.get("ntransactiontestcode")+" and rp.npreregno="+inputMap.get("npreregno")+")"
				+ "	 and tgtp.nisadhocparameter="+Enumeration.TransactionStatus.YES.gettransactionstatus()+""
				+ "  and tgtp.ntestgrouptestcode=tgt.ntestgrouptestcode"
				+ "  and tgt.nsitecode = "+userInfo.getNmastersitecode()+" "
				+ "  and tgtp.nsitecode = "+userInfo.getNmastersitecode()+" "
				+ "  and tgt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
				+ "  and tgtp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";

		List<TestGroupTestParameter> lstAdhocParamter = jdbcTemplate.query(sTestMasterParam, new TestGroupTestParameter());

		returnMap.put("AdhocParamter", lstAdhocParamter);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);

	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> createAdhocParamter(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		String ntestgrouptestcode = (String) inputMap.get("ntestgrouptestcode");
		String ntestcode  = (String) inputMap.get("ntestcode");
		String ntestparametercode = (String) inputMap.get("ntestparametercode");
		int ntransactiontestcode=(int) inputMap.get("ntransactiontestcode");
		int npreregno=(int) inputMap.get("npreregno");
		Boolean nneedsubsample = (Boolean) inputMap.get("nneedsubsample");
		String multiselecttransactiontestcode = (String) inputMap.get("multiselecttransactiontestcode");
		//List<TestFormula> lstTestFormula = new ArrayList<>();
		final Map<String, Object> returnMap = new HashMap<>();
		JSONObject jsonAuditOldData = new JSONObject();
		JSONObject jsonAuditNewData = new JSONObject();
		JSONObject auditActionType = new JSONObject();
		auditActionType.put("resultparameter", "IDS_ADHOCPARAMETER");
		int nregtypecode = (int) inputMap.get("nregtypecode");
		int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
		int ndesigntemplatemappingcode = (int) inputMap.get("ndesigntemplatemappingcode");

		String  sQuery = "select * from resultparameter where ntransactiontestcode="+ntransactiontestcode+""
				+ " and npreregno="+npreregno+" and ntestparametercode in("+ntestparametercode+")"
				+ " and nsitecode="+userInfo.getNtranssitecode()+" "
				+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";

		List<ResultParameter> lstResultParamter = jdbcTemplate.query(sQuery, new ResultParameter());

		if(lstResultParamter.size()==0) {		

			String  sTestGroupTestQuery = " select tgt.* from "
					+ " testgrouptestparameter tgt where tgt.ntestgrouptestcode in("+ntestgrouptestcode+")"
					+ " and tgt.ntestparametercode in("+ntestparametercode+")"
					+ " and tgt.nsitecode="+userInfo.getNmastersitecode()+" "
					+ " and tgt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";

			List<TestGroupTestParameter> lstTestMasterParamter = jdbcTemplate.query(sTestGroupTestQuery, new TestGroupTestParameter());
			final String sTestMasterParamter = lstTestMasterParamter.stream().map(x -> String.valueOf(x.getNtestgrouptestparametercode()))
					.distinct().collect(Collectors.joining(","));

			String sRegParamSeqno = " select nsequenceno from seqnoregistration where stablename='registrationparameter' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
			int nRegParamseqno = (int) jdbcUtilityFunction.queryForObject(sRegParamSeqno, Integer.class,jdbcTemplate);

			int TotalRegParamseqno = nRegParamseqno+lstTestMasterParamter.size();

			String insertRegParamQuery = " INSERT INTO registrationparameter("
					+ " ntransactionresultcode, ntransactiontestcode, npreregno,ntestgrouptestparametercode,"
					+ " ntestparametercode, nparametertypecode, ntestgrouptestformulacode, nreportmandatory,"
					+ " nresultmandatory, nunitcode, jsondata, nsitecode, nstatus)"

				 		+ " select "+nRegParamseqno+"+RANK() over(order by tgtp.ntestgrouptestparametercode),"
				 		+ " "+ntransactiontestcode+" ntransactiontestcode,"
				 		+ " "+npreregno+" npreregno,"
				 		+ " tgtp.ntestgrouptestparametercode,"
				 		+ " tgtp.ntestparametercode,"
				 		+ " tgtp.nparametertypecode,COALESCE(tgf.ntestgrouptestformulacode,-1) ntestgrouptestformulacode,"
				 		+ " "+Enumeration.TransactionStatus.YES.gettransactionstatus()+","
				 		+ " "+Enumeration.TransactionStatus.YES.gettransactionstatus()+","
				 		+ " tgtp.nunitcode,"
				 		+ "json_build_object('ntransactionresultcode'," +nRegParamseqno
				 		+ "+RANK() over(order by  tgtp.ntestgrouptestparametercode),'ntransactiontestcode',"+ntransactiontestcode+","
				 		+ " 'npreregno',"+npreregno+","
				 		+ " 'sminlod',tgtnp.sminlod,'smaxlod',tgtnp.smaxlod,'sminb',tgtnp.sminb,'smina',tgtnp.smina,'smaxa',tgtnp.smaxa,'smaxb',tgtnp.smaxb,"
				 		+ " 'sminloq',tgtnp.sminloq,'smaxloq',tgtnp.smaxloq,'sdisregard',tgtnp.sdisregard,'sresultvalue',tgtnp.sresultvalue,"
				 		+ " 'nroundingdigits',tgtp.nroundingdigits,'sresult',NULL,'sfinal',NULL,'stestsynonym',concat(tgt.stestsynonym,'[1][0]'),"
				 		+ " 'sparametersynonym',tgtp.sparametersynonym)::jsonb,"
				 		+ " "+userInfo.getNtranssitecode()+","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
				 		+ " from testgrouptest tgt,testgrouptestparameter tgtp "
				 		+ " left outer join testgrouptestnumericparameter tgtnp on tgtnp.ntestgrouptestparametercode=tgtp.ntestgrouptestparametercode and tgtnp.nsitecode = "+userInfo.getNmastersitecode()+" "
				 		+ " and tgtnp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
				 		+ " left outer join testgrouptestformula tgf on tgtnp.ntestgrouptestparametercode=tgf.ntestgrouptestparametercode "
				 		+ " and tgf.nsitecode ="+userInfo.getNmastersitecode()+" and tgf.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				 		+ " and tgf.ntransactionstatus = "+ Enumeration.TransactionStatus.YES.gettransactionstatus()
				 		+ " where tgt.ntestgrouptestcode=tgtp.ntestgrouptestcode "
				 		+ " tgt.nsitecode = "+userInfo.getNmastersitecode()+""
				 		+ " tgtp.nsitecode = "+userInfo.getNmastersitecode()+""
				 		+ " and tgt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
				 		+ " and tgtp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
				 		+ " and tgtp.ntestgrouptestcode in ("+ntestgrouptestcode+")"
				 		+ " and tgtp.ntestparametercode in("+ntestparametercode+")";

			jdbcTemplate.execute(insertRegParamQuery);

			String updateRegParamSeqno = " update seqnoregistration set nsequenceno="+TotalRegParamseqno+" where stablename='registrationparameter' "
					+ " and nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"; ";

			jdbcTemplate.execute(updateRegParamSeqno);


			String query3 = "insert into resultparameter( "
					+ "ntransactionresultcode,npreregno,ntransactiontestcode,ntestgrouptestparametercode "
					+ ",ntestparametercode,nparametertypecode,nresultmandatory,nreportmandatory,ntestgrouptestformulacode"
					+ ",nunitcode,ngradecode,ntransactionstatus,nenforcestatus,nenforceresult,ncalculatedresult,"
					+ "nenteredby,nenteredrole,ndeputyenteredby " + ",ndeputyenteredrole "
					+ ",nlinkcode,nattachmenttypecode,jsondata,nsitecode, nstatus) "
					+ " select rp.ntransactionresultcode,rp.npreregno, "
					+ "rp.ntransactiontestcode,rp.ntestgrouptestparametercode,rp.ntestparametercode,rp.nparametertypecode, "
					+ ""+Enumeration.TransactionStatus.YES.gettransactionstatus()+" nresultmandatory,"
					+ ""+Enumeration.TransactionStatus.YES.gettransactionstatus()+" nreportmandatory,"
					+ " coalesce(tgtf.ntestformulacode,"
					+ Enumeration.TransactionStatus.NA.gettransactionstatus()
					+ ") as ntestgrouptestformulacode, tgtp.nunitcode,"
					+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " as ngradecode,"
					+ ""+Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()+"as ntransactionstatus ,"
					+ ""+Enumeration.TransactionStatus.NO.gettransactionstatus()+" as nenforcestatus,"
					+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " as nenforceresult,"
					+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " as ncalculatedresult,"
					+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ","
					+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ","
					+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ","
					+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ","
					+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ","
					+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ",rp.jsondata,"
					+ userInfo.getNtranssitecode() + "," 
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " from registrationparameter rp,testparameter tgtp  "
					+ " left outer join testformula tgtf on  tgtp.ntestparametercode = tgtf.ntestparametercode  "
					+ " and tgtf.nsitecode = "+userInfo.getNmastersitecode()+" and tgtf.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tgtf.ndefaultstatus="+ Enumeration.TransactionStatus.YES.gettransactionstatus()
					+ " where rp.ntestparametercode=tgtp.ntestparametercode "
					+ " and rp.npreregno in ("+npreregno+")"
					+ " and rp.ntestparametercode in("+ntestparametercode+")"
					+ " and rp.ntransactiontestcode="+ntransactiontestcode+""
					+ " and rp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and tgtp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and tgtp.nsitecode = "+userInfo.getNmastersitecode()+""
					+ " and rp.nsitecode="+ userInfo.getNtranssitecode() + ";";

			jdbcTemplate.execute(query3);

			JSONObject jsonObject = new JSONObject();
			final String query4 = "insert into resultparametercomments (ntransactionresultcode, ntransactiontestcode, npreregno, "
					+ "ntestgrouptestparametercode, ntestparametercode,jsondata, nsitecode, nstatus)"
					+ " select  rp.ntransactionresultcode,"
					+ " rp.ntransactiontestcode, rp.npreregno, rp.ntestgrouptestparametercode, rp.ntestparametercode,'"+jsonObject+"' jsondata, "
					+ userInfo.getNtranssitecode() + " nsitecode," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus"
					+ " from resultparameter rp "
					+ " where rp.ntransactiontestcode in ("+ntransactiontestcode+") "
					+ " and rp.ntestparametercode in("+ntestparametercode+") "
					+ " and rp.npreregno in ("+npreregno+") and rp.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and rp.nsitecode="+ userInfo.getNtranssitecode() ;

			jdbcTemplate.execute(query4);

			String sQuery5 =  " select rp.ntransactionresultcode,rarno.sarno,tm.stestsynonym||'['|| CAST(rt.ntestrepeatno AS character varying(10))||']"
					+"['||CAST(rt.ntestretestno AS character varying(10))||']'stestsynonym,"
					+" tgtp.sparametername as sparametersynonym,"
					+" rp.npreregno,rp.ntransactiontestcode,rp.ntestgrouptestparametercode,rp.ntestparametercode,"

					       +" rp.nparametertypecode,rp.jsondata "
					       +" from registrationparameter rp,registrationarno rarno, registrationtest rt,testmaster tm,testparameter tgtp "

					       +" LEFT OUTER JOIN testformula tgtf ON tgtp.ntestparametercode = tgtf.ntestparametercode"
					       +" and tgtf.nsitecode = "+userInfo.getNmastersitecode()+" and tgtf.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
					       +" and tgtf.ndefaultstatus ="+Enumeration.TransactionStatus.YES.gettransactionstatus()+""

					       +" where  rp.ntestparametercode = tgtp.ntestparametercode"
					       +" and rarno.npreregno=rp.npreregno and rp.npreregno in("+npreregno+") "
					       +" and rp.ntestparametercode IN("+ntestparametercode+")"
					       +" and rt.ntransactiontestcode=rp.ntransactiontestcode and tm.ntestcode=rt.ntestcode "
					       +" and rp.ntransactiontestcode = "+ntransactiontestcode+" "
					       +" and rarno.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
					       +" and rarno.nsitecode ="+userInfo.getNtranssitecode()+""
					       +" and rt.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
					       +" and rt.nsitecode ="+userInfo.getNtranssitecode()+""
					       +" and tm.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
					       +" and tm.nsitecode ="+userInfo.getNmastersitecode()+""
					       +" and tgtp.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
					       +" and tgtp.nsitecode ="+userInfo.getNmastersitecode()+""
					       +" and rp.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
					       +" and rp.nsitecode ="+userInfo.getNtranssitecode()+""; 

			List<ResultParameter> lstResultParameter =  jdbcTemplate.query(sQuery5,new ResultParameter());

			jsonAuditNewData.put("resultparameter", lstResultParameter);
			Map<String, Object> objMap = new HashMap<>();
			objMap.put("nregtypecode", nregtypecode);
			objMap.put("nregsubtypecode", nregsubtypecode);
			objMap.put("ndesigntemplatemappingcode", ndesigntemplatemappingcode);

			auditUtilityFunction.fnJsonArrayAudit(jsonAuditNewData, null, auditActionType, objMap, false, userInfo);
			returnMap.putAll((Map<String, Object>) getTestbasedParameter(multiselecttransactiontestcode,userInfo).getBody());
			return new ResponseEntity<>(returnMap, HttpStatus.OK);

		}else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTVALIDPARAMETER", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}


	}






	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getConfigurationFilter(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception
	{
		final Map<String, Object> returnMap = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();

		RegistrationSubType objRegistrationsubtype = mapper.convertValue(inputMap.get("defaultRegistrationSubType"), new TypeReference<RegistrationSubType>(){
		});

		JSONObject jsonObj = new JSONObject();
		JSONArray array = new JSONArray();

		if((int)inputMap.get("ntestcode") != 0)
		{

			if(objRegistrationsubtype.isNneedworklist()== true)
			{
				inputMap.putAll((Map<String, Object>) getTestBasedOnWorklist(inputMap,userInfo).getBody());

				if(((List<?>)inputMap.get("Worklistvalues")).size() > 0)
				{
					jsonObj = new JSONObject();
					jsonObj.put("nconfigfiltercode", 1);
					jsonObj.put("sfiltername", Enumeration.ConfigFilter.WORKLIST.getConfigFilter());
					array.put(jsonObj);
				}
			}

			if(objRegistrationsubtype.isNneedbatch()== true)
			{
				inputMap.putAll((Map<String, Object>) getTestBasedOnBatch(inputMap,userInfo).getBody());
				if(((List<?>)inputMap.get("Batchvalues")).size() > 0)
				{
					jsonObj = new JSONObject();
					jsonObj.put("nconfigfiltercode", 2);
					jsonObj.put("sfiltername", Enumeration.ConfigFilter.BATCH.getConfigFilter());
					array.put(jsonObj);
				}
			}
		}

		String jsonStr = array.toString();
		List<Map<String,Object>> lstobject = mapper.readValue(jsonStr,new TypeReference <List<Map<String, Object>>>() {  
		});   
		if(lstobject.size() >0)
		{
			lstobject = (List<Map<String, Object>>) commonFunction.getMultilingualMessageList(lstobject,Arrays.asList("sfiltername"), userInfo.getSlanguagefilename());

		}

		returnMap.put("Batchvalues", new ArrayList<>());
		returnMap.put("defaultBatchvalue", new ArrayList<>());
		returnMap.put("Worklistvalues", new ArrayList<>());
		returnMap.put("defaultWorklistvalue", new ArrayList<>());
		returnMap.put("ConfigurationFilterValues",lstobject);
		returnMap.put("realConfigurationFilterValuesList",lstobject);
		returnMap.put("defaultConfigurationFilterValue",new ArrayList<>());

		return new ResponseEntity<>(returnMap, HttpStatus.OK); 
	}



	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getTestBasedBatchWorklist(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception
	{
		final Map<String, Object> returnMap = new HashMap<>();
		final int nconfigfiltercode = (int) inputMap.get("nconfigfiltercode");

		if(nconfigfiltercode == 1)
		{
			returnMap.putAll((Map<String, Object>)	getTestBasedOnWorklist(inputMap,userInfo).getBody());
			returnMap.put("Batchvalues", new ArrayList<>());
			returnMap.put("defaultBatchvalue", new ArrayList<>());
		}
		else if(nconfigfiltercode == 2)
		{
			returnMap.putAll((Map<String, Object>)	getTestBasedOnBatch(inputMap,userInfo).getBody());
			returnMap.put("Worklistvalues", new ArrayList<>());
			returnMap.put("defaultWorklistvalue", new ArrayList<>());
		}

		return new ResponseEntity<>(returnMap, HttpStatus.OK); 
	}


	public ResponseEntity<Object> getTestBasedOnWorklist(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {

		Map<String,Object> returnMap = new HashMap<>();
		List<?> lstworklist = new ArrayList<>();
		final String ntranscode = inputMap.get("ntranscode").getClass().getName() == "java.lang.String" ? (String)inputMap.get("ntranscode")
				: inputMap.get("ntranscode").toString();

		String strWorklist = "select wh.nworklistcode, max(wh.sworklistno) sworklistno " + 
				" from worklisthistory wh, worklist w,worklistsample ws, registrationtest rt,registrationtesthistory rth, registrationarno ra " + 
				" where" + 
				" wh.nworklistcode = w.nworklistcode" + 
				" and w.nworklistcode  = ws.nworklistcode" + 
				" and rt.ntransactiontestcode = ws.ntransactiontestcode and rth.ntesthistorycode in (select max(ntesthistorycode) from registrationtesthistory"+ 
				"  where npreregno = ws.npreregno  and ntransactionsamplecode = ws.ntransactionsamplecode and ntransactiontestcode = ws.ntransactiontestcode "
				+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" group by ntransactiontestcode,ntransactionsamplecode,npreregno)  "+
				" and rth.ntransactiontestcode = rt.ntransactiontestcode "+
				" and wh.nworkhistorycode = any (select max(nworkhistorycode) from worklisthistory where " + 
				" nsitecode = "+userInfo.getNtranssitecode()+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" group by nworklistcode) "+
				" and wh.ntransactionstatus = "+Enumeration.TransactionStatus.PREPARED.gettransactionstatus()+""+
				" and ra.npreregno = ws.npreregno "

					+ " and wh.nsitecode = "+userInfo.getNtranssitecode()+" "
					+ " and ws.nsitecode = "+userInfo.getNtranssitecode()+" " 
					+ " and w.nsitecode =  "+userInfo.getNtranssitecode()+" "
					+ " and rth.nsitecode = "+userInfo.getNtranssitecode()+" "
					+ " and rt.nsitecode = "+userInfo.getNtranssitecode()+" " 
					+ " and ra.nsitecode = "+userInfo.getNtranssitecode()+" "

					+ " and wh.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and ws.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" " 
					+ " and w.nstatus =  "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and rth.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and rt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" " 
					+ " and ra.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and ra.napprovalversioncode = "+(int)inputMap.get("napprovalversioncode")+""
					+ " and rth.ntransactionstatus in ("+ntranscode+") " 
					+ " and w.ntestcode = "+(int)inputMap.get("ntestcode")+" "
					+ " group by wh.nworklistcode order by wh.nworklistcode desc";

		lstworklist = jdbcTemplate.queryForList(strWorklist);

		if(lstworklist.size() > 0)
		{
			//ALPD-4870-To get previously save filter details when initial get,done by Dhanushya RI

			final Object filterWorklist=inputMap.containsKey("filterDetailValue") && !((List<Map<String, Object>>) inputMap.get("filterDetailValue")).isEmpty()
					&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0).containsKey("worklistValue")
					? ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0).get("worklistValue") :lstworklist.get(0);

			returnMap.put("Worklistvalues", lstworklist);
			returnMap.put("RealWorklistvaluesList", lstworklist);
			returnMap.put("defaultWorklistvalue", filterWorklist);
			returnMap.put("realWorklistvalue", filterWorklist);
		}
		else
		{
			returnMap.put("Worklistvalues", lstworklist);
			returnMap.put("defaultWorklistvalue", lstworklist);
		}
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}




	public ResponseEntity<Object> getTestBasedOnBatch(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {

		Map<String,Object> returnMap = new HashMap<>();
		List<?> lstbatch = new ArrayList<>();
		final String ntranscode = inputMap.get("ntranscode").getClass().getName() == "java.lang.String" ? (String)inputMap.get("ntranscode")
				: inputMap.get("ntranscode").toString();

		String strBatch = "select max(bh.sbatcharno) sbatcharno,bh.nbatchmastercode  " + 
				" from batchiqctransaction bt,registrationtest rt,batchhistory bh,registrationtesthistory rth, registrationarno ra " + 
				" where " + 
				" rt.ntransactiontestcode = bt.ntransactiontestcode" + 
				" and bh.nbatchmastercode = bt.nbatchmastercode "+
				" and rth.ntransactiontestcode = bt.ntransactiontestcode "+
				" and ra.npreregno = bt.npreregno "+
				" and rth.ntesthistorycode = (select max(ntesthistorycode) from registrationtesthistory where npreregno = bt.npreregno "+
				" and ntransactionsamplecode = bt.ntransactionsamplecode and ntransactiontestcode = bt.ntransactiontestcode "+
				" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode = "+userInfo.getNtranssitecode()+" "
				+ " group by ntransactiontestcode,ntransactionsamplecode,npreregno)" + 
				" and bh.nbatchhistorycode in (select max(nbatchhistorycode) from batchhistory "+
				" where nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode = "+userInfo.getNtranssitecode()+" "
				+ " group by nbatchmastercode)" 
				+ " and ra.napprovalversioncode = "+(int)inputMap.get("napprovalversioncode")+""

					+ " and bh.nsitecode = "+userInfo.getNtranssitecode()+" "
					+ " and bt.nsitecode = "+userInfo.getNtranssitecode()+""
					+ " and rt.nsitecode = "+userInfo.getNtranssitecode()+""
					+ " and rth.nsitecode = "+userInfo.getNtranssitecode()+""
					+ " and rt.nsitecode = "+userInfo.getNtranssitecode()+" "

					+ " and bh.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and bt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
					+ " and rt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
					+ " and rth.nstatus  = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
					+ " and rt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and bh.ntransactionstatus = "+Enumeration.TransactionStatus.INITIATED.gettransactionstatus()+""
					+ " and rt.ntestcode = "+(int)inputMap.get("ntestcode")+""  
					+ " and rth.ntransactionstatus in ("+ntranscode+")"
					+ " group by bh.nbatchmastercode order by bh.nbatchmastercode desc ";

		lstbatch = jdbcTemplate.queryForList(strBatch);

		if(lstbatch.size() > 0)
		{
			//ALPD-4870-To get previously save filter details when initial get,done by Dhanushya RI

			final Object filterBatch=inputMap.containsKey("filterDetailValue") && !((List<Map<String, Object>>) inputMap.get("filterDetailValue")).isEmpty()
					&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0).containsKey("batchValue")
					? ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0).get("batchValue") :lstbatch.get(0);

			returnMap.put("Batchvalues", lstbatch);
			returnMap.put("realBatchvaluesList", lstbatch);
			returnMap.put("defaultBatchvalue", filterBatch);
			returnMap.put("realBatchvalue", filterBatch);
		}
		else
		{
			returnMap.put("Batchvalues", lstbatch);
			returnMap.put("defaultBatchvalue", lstbatch);
		}
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}


	@Override		
	public ResponseEntity<Object> getenforceResult(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		List<RegistrationTestHistory> lstvalidationtest = validateTestStatus(String.valueOf(inputMap.get("ntransactiontestcode"))
				,(int)inputMap.get("ncontrolcode") ,
				userInfo,(int) inputMap.get("nneedReceivedInLab"));

		if (!lstvalidationtest.isEmpty()) {
			Map<String, Object> returnMap = new HashMap<>();
			String str = "select rp.*,rp.jsondata->>'sfinal'  as sfinal  "
					+ " ,rpc.jsondata->>'senforceresultcomment' senforceresultcomment"
					+ " ,g.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
					+ "' as sgradename from resultparameter rp,resultparametercomments rpc,grade g where "
					+ " rp.ngradecode=g.ngradecode "
					+ " and rp.ntransactionresultcode=rpc.ntransactionresultcode"
					+ " and rp.nsitecode ="+ userInfo.getNtranssitecode() + " "
					+ " and rpc.nsitecode ="+ userInfo.getNtranssitecode() + " "
					+ " and g.nsitecode ="+ userInfo.getNmastersitecode() + " "

						+ " and rpc.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and rp.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and g.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and rp.ntransactionresultcode="	+ inputMap.get("ntransactionresultcode");

			ResultParameter objResultParameter = (ResultParameter) jdbcTemplate.queryForObject(str,	new ResultParameter());
			returnMap.put("savedResultparameter", objResultParameter);

			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTVALIDTEST", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}

	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> updateEnforceResult(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		String querys = "";
		String resultChangeHistoryInsertValues = "";
		String resultChangeHistoryInsert = "";
		String seqUpdateQuery = "";
		String rpchQueryString= "";
		int nregtypecode = (int) inputMap.get("nregtypecode");
		int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
		int ndesigntemplatemappingcode = (int) inputMap.get("ndesigntemplatemappingcode");

		Map<String, Object> returnMap = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();

		ResultParameter objResultParameter = mapper.convertValue(inputMap.get("ResultParameter"),
				new TypeReference<ResultParameter>() {
		});

		String straudit =  " select rp.*,rp.jsondata->>'sfinal' sfinal , rp.jsondata->>'sresult' sresult , t.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "' as stransdisplaystatus,"
				+ " rp.jsondata->>'stestsynonym' stestsynonym,"
				+ " rp.jsondata->>'sparametersynonym' sparametersynonym,"
				+ " rp.jsondata->>'senforceresultcomment' senforceresultcomment,"
				+ " r.sarno,rs.ssamplearno,g.sgradename,u.sunitname"
				+ " from resultparameter rp,transactionstatus t,registrationarno r,registrationsamplearno rs,"
				+ " registrationtest rt,grade g,unit u"
				+ " where t.ntranscode=rp.ntransactionstatus and r.npreregno=rp.npreregno and r.npreregno=rs.npreregno"
				+ " and rs.npreregno=rp.npreregno and rt.ntransactionsamplecode=rs.ntransactionsamplecode"
				+ " and rt.ntransactiontestcode=rp.ntransactiontestcode and u.nunitcode=rp.nunitcode "
				+ " and rt.npreregno=rs.npreregno and r.npreregno=rt.npreregno and g.ngradecode=rp.ngradecode "

									+ " and rs.nsitecode = "+userInfo.getNtranssitecode()+""
									+ " and rp.nsitecode = "+userInfo.getNtranssitecode()+""
									+ " and r.nsitecode = "+ userInfo.getNtranssitecode() + ""

									+ " and rs.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
									+ " and rp.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
									+ " and t.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
									+ " and rp.ntransactionresultcode in ("+ objResultParameter.getNtransactionresultcode() + ")";

		List<ResultParameter> lstResultParameterOld = jdbcTemplate.query(straudit, new ResultParameter());

		ResultParameter objResultParameterOld=lstResultParameterOld.get(0);
		final String senforceResultComment = objResultParameter.getSenforceresultcomment();

		if (objResultParameterOld.getSfinal() != null&&objResultParameterOld.getSfinal() != "") {
			JSONObject jsonobject = new JSONObject(); 
			jsonobject.put("sfinal", objResultParameterOld.getSfinal());
			jsonobject.put("sresult", objResultParameterOld.getSresult());
			jsonobject.put("dentereddate",dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ").replace("Z", ""));
			jsonobject.put("noffsetdentereddate", dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()));
			jsonobject.put("dentereddatetimezonecode", userInfo.getNtimezonecode());

			final String resultChangeSeqNoGet = "select nsequenceno from seqnoregistration where stablename=N'resultchangehistory' "
					+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
			
			int resultChangeSeqNo = (int) jdbcUtilityFunction.queryForObject(resultChangeSeqNoGet, Integer.class,jdbcTemplate);

			resultChangeSeqNo++;
			resultChangeHistoryInsertValues += " (" + resultChangeSeqNo + ","
					+ objResultParameterOld.getNtransactionresultcode() + ","
					+ objResultParameterOld.getNtransactiontestcode() + ","
					+ objResultParameterOld.getNpreregno() + ","
					+ objResultParameterOld.getNtestgrouptestparametercode() + ","
					+ Enumeration.ParameterType.CHARACTER.getparametertype() + ","
					+ userInfo.getNformcode() + ","
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ","
					+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ","
					+ objResultParameter.getNgradecode() + "," + userInfo.getNusercode()
					+ "," + userInfo.getNuserrole() + "," + userInfo.getNdeputyusercode() + ","
					+ userInfo.getNdeputyuserrole() + ",'" +stringUtilityFunction.replaceQuote(jsonobject.toString()) + "',"
					+ userInfo.getNtranssitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";

			resultChangeHistoryInsert = "insert into resultchangehistory (nresultchangehistorycode,ntransactionresultcode,ntransactiontestcode,"
					+ "npreregno,ntestgrouptestparametercode,nparametertypecode,nformcode,nenforceresult,"
					+ "nenforcestatus,ngradecode,nenteredby,nenteredrole,ndeputyenteredby,ndeputyenteredrole,jsondata,nsitecode,nstatus) "
					+ " values"
					+ resultChangeHistoryInsertValues.substring(0, resultChangeHistoryInsertValues.length() - 1) + ";";
			seqUpdateQuery = "update seqnoregistration  set nsequenceno=" + (resultChangeSeqNo)
					+ " where stablename=N'resultchangehistory' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";

			final String getCommentsHistory = " select * from resultparamcommenthistory "
					+ " where nresultparamcommenthistorycode = ("
					+ " select max(nresultparamcommenthistorycode) from resultparamcommenthistory "
					+ "	where nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+"  and  nsitecode="+userInfo.getNtranssitecode()
					+ " and ntransactionresultcode = "+ objResultParameterOld.getNtransactionresultcode() + " )"
					+" and nsitecode="+userInfo.getNtranssitecode()+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"; 

			ResultParamCommentHistory objCommentsHistory = (ResultParamCommentHistory) jdbcUtilityFunction.
					queryForObject(getCommentsHistory, ResultParamCommentHistory.class,jdbcTemplate);

			String str = "";
			if(objCommentsHistory != null)
			{
				JSONObject jsonObj = new JSONObject(objCommentsHistory.getJsondata());
				str = "'"+stringUtilityFunction.replaceQuote(jsonObj.toString())+"'::jsonb||";
			} 
			final String getSeqNo = "select nsequenceno from seqnoregistration  where stablename='resultparamcommenthistory' "
					+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
			int nseqnoCommentHistory = (int) jdbcUtilityFunction.queryForObject(getSeqNo, Integer.class,jdbcTemplate);

			nseqnoCommentHistory++;
			rpchQueryString = " insert into resultparamcommenthistory(nresultparamcommenthistorycode,ntransactionresultcode "
					+ " ,ntransactiontestcode, npreregno ,ntestgrouptestparametercode,ntestparametercode,jsondata,nsitecode,nstatus)" + " values("
					+ nseqnoCommentHistory + "," + objResultParameterOld.getNtransactionresultcode() + ","
					+ objResultParameterOld.getNtransactiontestcode() + " ,"
					+ objResultParameterOld.getNpreregno() + ","
					+ objResultParameterOld.getNtestgrouptestparametercode() + ","
					+ objResultParameterOld.getNtestparametercode() + " ,"
					+ " "+str+"json_build_object('senforceresultcomment','"+stringUtilityFunction.replaceQuote(senforceResultComment)+"')::jsonb,"+userInfo.getNtranssitecode()+", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "); ";

			rpchQueryString += " update seqnoregistration set nsequenceno=" + nseqnoCommentHistory+ " where stablename='resultparamcommenthistory' "
					+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"; 

		}
		String str = " update resultparameter set jsondata=jsondata||jsonb_build_object('sfinal','"
				+stringUtilityFunction.replaceQuote(objResultParameter.getSfinal()) + "','sresult','" +stringUtilityFunction.replaceQuote(objResultParameter.getSfinal())
				+ "', 'dentereddate', '"+dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ").replace("Z", "")
				+ "'),nenforceresult="+ Enumeration.TransactionStatus.YES.gettransactionstatus()
				+" ,ngradecode=" + Enumeration.Grade.FIO.getGrade() + " ,nparametertypecode="+ objResultParameter.getNparametertypecode() +" "
				+ " where nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode = "+userInfo.getNtranssitecode()+" "
				+ " and ntransactionresultcode="+ objResultParameter.getNtransactionresultcode()+";";

		final String resultCommentUpdate = " update resultparametercomments set jsondata = jsondata||json_build_object('senforceresultcomment','"
				+ stringUtilityFunction.replaceQuote(senforceResultComment)+"')::jsonb "
				+ " where ntransactionresultcode="+ objResultParameterOld.getNtransactionresultcode()  + " and nsitecode = "+userInfo.getNtranssitecode()+" "
				+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";

		querys = rpchQueryString+resultCommentUpdate+resultChangeHistoryInsert + seqUpdateQuery+str  ;
		jdbcTemplate.execute(querys);

		List<ResultParameter> lstResultParameterNew = jdbcTemplate.query(straudit, new ResultParameter());

		returnMap.putAll((Map<String, Object>) getTestbasedParameter(
				String.valueOf(objResultParameter.getNtransactiontestcode()), userInfo).getBody());

		JSONObject jsonAuditOldData = new JSONObject();
		JSONObject jsonAuditNewData = new JSONObject();
		JSONObject auditActionType = new JSONObject();
		jsonAuditOldData.put("resultparameter", lstResultParameterOld);
		jsonAuditNewData.put("resultparameter", lstResultParameterNew);
		auditActionType.put("resultparameter", "IDS_ENFORCERESULT");
		Map<String, Object> objMap = new HashMap<>();
		objMap.put("nregtypecode", nregtypecode);
		objMap.put("nregsubtypecode", nregsubtypecode);

		objMap.put("ndesigntemplatemappingcode", ndesigntemplatemappingcode);

		auditUtilityFunction.fnJsonArrayAudit(jsonAuditOldData, jsonAuditNewData, auditActionType, objMap, false, userInfo);

		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}



	@Override

	//ALPD-5594--Added by Vignesh R(21-03-2025)-->SMTL > Result entry > open ELN sheet displayed empty page, it occurs when open the ELN sheet for run batch test, check description
	public ResponseEntity<Object> getELNTestValidation(final int npreregno,final int ntransactiontestcode,final UserInfo userInfo)
			throws Exception {

		Map<String,Object> map=new HashMap<>();

		boolean Validation=false;
		boolean isBatch=false;

		String Validate="select count(ntransactiontestcode),nbatchmastercode from sdmslabsheetdetails where ntransactiontestcode="+ntransactiontestcode+" "
				+ " and  npreregno="+npreregno+" and nsitecode = "+userInfo.getNtranssitecode()+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " group by nbatchmastercode";
		List<Map<String, Object>> list = jdbcTemplate.queryForList(Validate);

		if(!list.isEmpty()){

			Validation=true;

			if((int)list.get(0).get("nbatchmastercode")==-1){

				isBatch=true;

			}

		}
		map.put("isEmptySheet", Validation);		
		map.put("isBatch", isBatch);

		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getResultEntryParameter(int nallottedspeccode,int ntestcode,String ntransactiontestcode, UserInfo userInfo,int nspecsampletypecode)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		ObjectMapper objMapper = new ObjectMapper();

		String transactiontestcode ="select max(ntransactiontestcode) from registrationtest rt,registration r,registrationsample rs "
				+ " where rs.npreregno=r.npreregno and r.npreregno=rt.npreregno and rt.ntestcode="+ntestcode+" and r.nallottedspeccode="+nallottedspeccode+" "
				+ " and rt.nsitecode = "+userInfo.getNtranssitecode()+""
				+ " and r.nsitecode = "+userInfo.getNtranssitecode()+""
				+ " and rs.nsitecode = "+userInfo.getNtranssitecode()+""
				+ " and rt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
				+ " and rs.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
				+ " and r.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
				+ " and rs.nspecsampletypecode="+nspecsampletypecode;

		ntransactiontestcode = (String) jdbcUtilityFunction.queryForObject(transactiontestcode, String.class,jdbcTemplate);

		String getParams = "select row_num,a.jsondata->>'additionalInfo' as  \"additionalInfo\",a.jsondata->>'additionalInfoUidata' as  \"additionalInfoUidata\",(a.jsondata->>'ntestgrouptestpredefcode')::int as ntestgrouptestpredefcode,a.npreregno,a.jsondata,"
				+ " CASE WHEN row_num = 1 THEN a.stestsynonym ELSE '' END AS"
				+ " stestsynonym,a.sparametersynonym, "
				+ "CASE WHEN row_num = 1 THEN '['||cast(a.ntestrepeatno as varchar(3))||']['||cast(a.ntestretestno as varchar(3))||']' ELSE '' END AS sretestrepeatcount,"
				+ "a.nparametertypecode,a.sparametertypename,"
				+ "a.ntestrepeatno,a.ntestretestno,a.ntransactionsamplecode,a.ntransactionresultcode,a.ntransactiontestcode,a.nchecklistversioncode, "
				+ "a.ntransactionstatus,a.nroundingdigits,-1 ngradecode, "
				+ "a.ntestparametercode,a.sresultvalue,'' as sresult"
				+ " ,a.ncalculatedresult,a.nfilesize,'' as sgradename,a.ntestgrouptestformulacode, "
				+ "a.ntestgrouptestparametercode,a.nsorter,a.ntestgrouptestcode,a.nresultmandatory,a.stransdisplaystatus as sresultmandatory  "
				+ " from  " + "("
				+ "select ROW_NUMBER() OVER (PARTITION BY rt.ntransactiontestcode,ra.sarno,rsa.ssamplearno ORDER BY rt.npreregno, tgtp.nsorter, rp.ntransactionresultcode,rt.ntransactiontestcode desc) row_num, "
				+ "rp.jsondata, rp.npreregno,ra.sarno,rsa.ssamplearno,rt.jsondata->>'stestname' as stestsynonym,tgtp.sparametersynonym,rp.nparametertypecode,pt.sparametertypename,"
				+ "rt.ntestrepeatno,rt.ntestretestno,rt.ntransactionsamplecode,"
				+ "rp.ntransactionresultcode,rp.ntransactiontestcode,tgtp.nchecklistversioncode,rp.ntransactionstatus,rp.ncalculatedresult,"
				+ "tgtp.ntestparametercode," + "(rp.jsondata->>'nroundingdigits')::integer as nroundingdigits,"
				+ "(rp.jsondata->>'sresultvalue')::character varying(100) as sresultvalue,"
				+ "(rp.jsondata->>'nfilesize')::character varying(100) as nfilesize,"
				+ "tgf.ntestgrouptestformulacode, "
				+ "rp.ntestgrouptestparametercode,tgtp.nsorter,tgtp.ntestgrouptestcode,rp.nresultmandatory,"
				+ "ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "' as stransdisplaystatus "
				+ "from resultparameter rp,registrationsamplearno rsa,registrationsample rts,registrationarno ra,registrationtest rt,testgrouptestparameter tgtp "
				+ "left outer join testgrouptestnumericparameter tgnp on tgtp.ntestgrouptestparametercode = tgnp.ntestgrouptestparametercode "
				+ " and tgnp.nsitecode = "+userInfo.getNmastersitecode()+" and tgnp.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "left outer join testgrouptestformula tgf on tgf.ntestgrouptestcode=tgtp.ntestgrouptestcode and tgf.nsitecode = "+userInfo.getNmastersitecode()+" and tgf.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgf.ntransactionstatus  = " + Enumeration.TransactionStatus.YES.gettransactionstatus() + " "
				+ " and tgtp.ntestgrouptestparametercode=tgf.ntestgrouptestparametercode, "
				+ " parametertype pt,transactionstatus ts  "
				+ " where pt.nparametertypecode = tgtp.nparametertypecode "
				+ " and ts.ntranscode = rp.nresultmandatory "
				+ " and tgtp.ntestgrouptestparametercode = rp.ntestgrouptestparametercode and rsa.ntransactionsamplecode = rt.ntransactionsamplecode "
				+ " and ra.npreregno = rt.npreregno  "
				+ " and rp.ntransactiontestcode = rt.ntransactiontestcode "
				+ " and rp.ntransactiontestcode in (" + ntransactiontestcode + ") "

							+ " and rp.nsitecode =  "+userInfo.getNtranssitecode()+" "
							+ " and rsa.nsitecode =  "+userInfo.getNtranssitecode()+" "
							+ " and rts.nsitecode =  "+userInfo.getNtranssitecode()+" "
							+ " and ra.nsitecode = "+userInfo.getNtranssitecode()+" "
							+ " and rt.nsitecode =  "+userInfo.getNtranssitecode()+" "
							+ " and tgtp.nsitecode =  "+userInfo.getNmastersitecode()+" "
							+ " and pt.nsitecode =  "+userInfo.getNmastersitecode()+" "

							+ " and rp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
							+ " and rsa.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
							+ " and rts.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
							+ " and ra.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
							+ " and rt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
							+ " and tgtp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
							+ " and pt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
							+ " and ts.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""

							+ " and rsa.ntransactionsamplecode = rts.ntransactionsamplecode"
							+ " and rts.nspecsampletypecode="+nspecsampletypecode
							+ " order by rp.ntransactiontestcode desc "
							+ ")a ;";

		final String getPreDefinedParams = " select tgpp.ntestgrouptestpredefcode, tgpp.ntestgrouptestparametercode, tgpp.ngradecode,  "
				+ " tgpp.spredefinedname||' ('||tgpp.spredefinedsynonym||')' as sresultpredefinedname,tgpp.spredefinedname,"
				+ " tgpp.spredefinedsynonym, "
				+ " tgpp.spredefinedcomments, tgpp.salertmessage, "
				+ " tgpp.nneedresultentryalert, tgpp.nneedsubcodedresult, tgpp.ndefaultstatus, tgpp.nsitecode, "
				+ " tgpp.dmodifieddate, tgpp.nstatus,rp.ntransactiontestcode,rp.ntestgrouptestparametercode,rp.ntransactionresultcode "
				+ " from resultparameter rp,testgrouptestpredefparameter tgpp"
				+ " where rp.ntestgrouptestparametercode = tgpp.ntestgrouptestparametercode "
				+ " and rp.nparametertypecode = "+ Enumeration.ParameterType.PREDEFINED.getparametertype() + " "
				+ " and rp.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and tgpp.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rp.nsitecode = "+userInfo.getNtranssitecode()+""
				+ " and tgpp.nsitecode = "+userInfo.getNmastersitecode()+""
				+ " and tgpp.spredefinedname <> '' "
				+ " and rp.ntransactiontestcode in ("+ ntransactiontestcode + ");";

		String getGrades = "select g.ngradecode,case g.sgradename when 'NA' then '' else"
				+ " coalesce(g.jsondata->'sdisplayname'->>'"+userInfo.getSlanguagetypecode()+"',"
				+ " g.jsondata->'sdisplayname'->>'en-US') end sgradename ,"
				+ "cm.ncolorcode,cm.scolorname,cm.scolorhexcode from grade g,colormaster cm "
				+ " where g.ncolorcode = cm.ncolorcode and g.nsitecode = "+userInfo.getNmastersitecode()+""
				+ " and g.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and cm.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " union all "
				+ " select 0 ngradecode,'' sgradename, 0 ncolorcode, '' scolorname,'' scolorhexcode;";

		returnMap.put("ResultParameter", jdbcTemplate.queryForList(getParams));

		List<TestGroupTestPredefinedParameter> lst = jdbcTemplate.query(getPreDefinedParams,new TestGroupTestPredefinedParameter());

		List<Grade> lstGrade = objMapper.convertValue(
				commonFunction.getMultilingualMessageList(jdbcTemplate.query(getGrades, new Grade()),Arrays.asList("sgradename"), userInfo.getSlanguagefilename()),
				new TypeReference<List<Grade>>() {});

		returnMap.put("PredefinedValues", lst.stream().collect(Collectors
				.groupingBy(TestGroupTestPredefinedParameter::getNtransactionresultcode, Collectors.toList())));

		returnMap.put("GradeValues",
				lstGrade.stream().collect(Collectors.groupingBy(Grade::getNgradecode, Collectors.toList())));
		return new ResponseEntity<>(returnMap, HttpStatus.OK);


	}	


	@Override
	public ResponseEntity<Object> updateMultiSampleTestParameterResult(MultipartHttpServletRequest request,final UserInfo userInfo) throws Exception {
		Map<String, Object> returnMap = new HashMap<>();

		final String lockquery = "lock lockresultupdate" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(lockquery);

		ObjectMapper objMapper = new ObjectMapper();
		JSONObject jsonAuditOldData = new JSONObject();
		JSONObject jsonAuditNewData = new JSONObject();
		JSONObject auditActionType = new JSONObject();
		String result = request.getParameter("resultData");
		int nregtypecode = Integer.parseInt(request.getParameter("nregtypecode"));
		int nregsubtypecode = Integer.parseInt(request.getParameter("nregsubtypecode"));
		int ndesigntemplatemappingcode = Integer.parseInt(request.getParameter("ndesigntemplatemappingcode"));
		int fileparamcount = Integer.parseInt(request.getParameter("filecount"));

		List<ResultParameter> lstTransSampResults = objMapper.readValue(result,new TypeReference<List<ResultParameter>>() {});
		List<ResultParameter> lstTransSampResultss = objMapper.readValue(result,new TypeReference<List<ResultParameter>>() {});
		String sampleid = request.getParameter("sampleid");

		String validate="select rs.* from registrationsamplearno rs,registration r,registrationsample rts "
				+ " where rts.ntransactionsamplecode=rs.ntransactionsamplecode  and r.npreregno=rs.npreregno   "
				+ " and rts.nspecsampletypecode!="+Integer.parseInt(request.getParameter("nspecsampletypecode"))+" "
				+ " and rs.nsitecode = "+userInfo.getNtranssitecode()+""
				+ " and r.nsitecode = "+userInfo.getNtranssitecode()+""
				+ " and rts.nsitecode = "+userInfo.getNtranssitecode()+""
				+ " and rs.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and r.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rts.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and r.nregtypecode="+nregtypecode+" "
				+ " and r.nregsubtypecode="+nregsubtypecode+" "
				+ " and rs.ssamplearno in('"+sampleid+"');";

		List<RegistrationSampleArno> lstsamplarno = jdbcTemplate.query(validate,new RegistrationSampleArno());

		String samplearno="";
		if(lstsamplarno.size()>0)
		{

			samplearno = ((lstsamplarno.stream().map(object -> String.valueOf(object.getSsamplearno()))
					.collect(Collectors.toList())).stream().distinct().collect(Collectors.toList())).stream().collect(Collectors.joining(","));
		}
		if (samplearno.equals("")) {

			String samplevalidate="select rs.* from registrationsamplearno rs,registration r,registrationsample rts"
					+ " where rts.ntransactionsamplecode=rs.ntransactionsamplecode  and r.npreregno=rs.npreregno  "
					+ " and rs.nsitecode = "+userInfo.getNtranssitecode()+""
					+ " and r.nsitecode = "+userInfo.getNtranssitecode()+""
					+ " and rts.nsitecode = "+userInfo.getNtranssitecode()+""
					+ " and rs.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and r.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and rts.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and rts.nspecsampletypecode="+Integer.parseInt(request.getParameter("nspecsampletypecode"))+" "
					+ " and r.nregtypecode="+nregtypecode+" and r.nregsubtypecode="+nregsubtypecode+" and rs.ssamplearno in('"+sampleid+"');";

			List<RegistrationSampleArno> lstsamplvalidate = jdbcTemplate.query(samplevalidate,new RegistrationSampleArno());

			if(lstsamplvalidate.size()==0)
			{
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ADDVALIDSAMPLE",	userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}


			String transactiontestqrt="select registrationtest.ntransactiontestcode from registrationtest where ntransactionsamplecode in( "
					+ "select rsa.ntransactionsamplecode from registrationsamplearno rsa,registration r where rsa.npreregno=r.npreregno "
					+ " and r.nsitecode = "+userInfo.getNtranssitecode()+" "
					+ " and rsa.nsitecode = "+userInfo.getNtranssitecode()+" "
					+ " and r.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and rsa.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and r.nregtypecode="+nregtypecode+" and r.nregsubtypecode="+nregsubtypecode+ " "
					+ " and rsa.ssamplearno in('"+sampleid+"')) and nsitecode = "+userInfo.getNtranssitecode()+" "
					+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
					+ " and ntestcode="+Integer.parseInt(request.getParameter("ntestcode"));

			List<ResultParameter> lst = jdbcTemplate.query(transactiontestqrt,new ResultParameter());			

			String stransactiontestcode = lst.stream()
					.map(object -> String.valueOf(object.getNtransactiontestcode())).collect(Collectors.joining(","));
			int controlCode=Integer.parseInt(request.getParameter("ncontrolcode"));
			int nneedReceivedInLab =Integer.parseInt(request.getParameter("nneedReceivedInLab"));
			List<RegistrationTestHistory> filteredTestList = validateTestStatus(stransactiontestcode,controlCode, userInfo,nneedReceivedInLab);

			String query = "";

			if (!filteredTestList.isEmpty()) {
				if (fileparamcount > 0) {
					String sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, userInfo);
				}

				lstTransSampResults = lst.stream()
						.filter(sampleResult -> filteredTestList.stream().anyMatch(filtertest -> filtertest
								.getNtransactiontestcode() == sampleResult.getNtransactiontestcode()))
						.collect(Collectors.toList());
				stransactiontestcode = filteredTestList.stream()
						.map(object -> String.valueOf(object.getNtransactiontestcode())).collect(Collectors.joining(","));
				String transactionresultcodeqrt="select ntransactionresultcode,ntestgrouptestparametercode from ResultParameter "
						+ " where ntransactiontestcode in("+stransactiontestcode+") and nsitecode = "+userInfo.getNtranssitecode()+" "
						+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";

				List<ResultParameter> lstresultcode = jdbcTemplate.query(transactionresultcodeqrt,	new ResultParameter());

				String stransactionresultcode = lstresultcode.stream().map(object -> String.valueOf(object.getNtransactionresultcode())).collect(Collectors.joining(","));

				if (lstTransSampResults != null && !lstTransSampResults.isEmpty()) {
					query = " select rp.*,rp.jsondata->>'sfinal' sfinal ," + " t.jsondata->'stransdisplaystatus'->>'"
							+ userInfo.getSlanguagetypecode() + "' as stransdisplaystatus, rp.jsondata->>'stestsynonym' stestsynonym,"
							+ " rp.jsondata->>'sparametersynonym' sparametersynonym,r.sarno,rs.ssamplearno,g.sgradename,u.sunitname"

							+ " from resultparameter rp,transactionstatus t,registrationarno r,registrationsamplearno rs,"
							+ " registrationtest rt,grade g,unit u"

							+ " where t.ntranscode=rp.ntransactionstatus and r.npreregno=rp.npreregno and r.npreregno=rs.npreregno"
							+ " and rs.npreregno=rp.npreregno and rt.ntransactionsamplecode=rs.ntransactionsamplecode"
							+ " and rt.ntransactiontestcode=rp.ntransactiontestcode and u.nunitcode=rp.nunitcode "
							+ " and rt.npreregno=rs.npreregno and r.npreregno=rt.npreregno and g.ngradecode=rp.ngradecode "
							+ " and rp.nsitecode = "+userInfo.getNtranssitecode()+""
							+ " and r.nsitecode = "+userInfo.getNtranssitecode()+""
							+ " and rs.nsitecode = "+userInfo.getNtranssitecode()+""
							+ " and rt.nsitecode = "+userInfo.getNtranssitecode()+""
							+ " and g.nsitecode = "+userInfo.getNmastersitecode()+""
							+ " and u.nsitecode = "+userInfo.getNmastersitecode()+""

							+ " and rp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
							+ " and r.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
							+ " and rs.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
							+ " and rt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
							+ " and g.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
							+ " and u.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
							+ " and t.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
							+ " and rp.ntransactionresultcode in (" + stransactionresultcode + ")";

					List<ResultParameter> lstSampleResults = jdbcTemplate.query(query, new ResultParameter());

					query = " select nsequenceno from seqnoregistration where stablename=N'resultchangehistory' "
							+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";

					int nresultchangehistoryno = jdbcTemplate.queryForObject(query, Integer.class);

					StringBuilder sresultcode = new StringBuilder();
					StringBuilder schangeresult = new StringBuilder();

					final String currentDateTime =dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ").replace("Z","");
					String updateQuery="";
					String insertResultChange="";
					String parameterCommentsQuery="";
					String controlListquery = " select ncontrolcode, scontrolname from controlmaster where nformcode="
							+ Enumeration.QualisForms.RESULTENTRY.getqualisforms()+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					final List<ControlMaster> controlList = jdbcTemplate.query(controlListquery, new ControlMaster());

					final Map<String, Short> controlMap = controlList.stream().collect(Collectors.toMap(
							ControlMaster::getScontrolname,control -> control.getNcontrolcode()));	
					List<ResultParameter> lstResulCodePredefined = lstTransSampResults
							.stream().filter(resultType -> (resultType.getNparametertypecode()==Enumeration.ParameterType.PREDEFINED.getparametertype())
									&& (resultType.getJsondata().containsKey("sresultcomment")) && (!resultType.getJsondata().get("sresultcomment").toString().equals("")))
							.collect(Collectors.toList());

					String stransactionresultcodeforcomments = lstResulCodePredefined.stream()
							.map(object -> String.valueOf(object.getNtransactionresultcode())).collect(Collectors.joining(","));
					if (lstSampleResults != null && !lstSampleResults.isEmpty()) {
						for (ResultParameter objResult : lstTransSampResultss) {

							String transactionresultcode  = lstresultcode
									.stream().filter(objjqresultparam -> objjqresultparam
											.getNtestgrouptestparametercode() == objResult.getNtestgrouptestparametercode())
									.map(object -> String.valueOf(object.getNtransactionresultcode())).collect(Collectors.joining(","));


							Map<String, Object> jsonData = objResult.getJsondata();
							String sfinal=StringEscapeUtils.unescapeJava(jsonData.get("sfinal").toString());
							String sresult=StringEscapeUtils.unescapeJava(jsonData.get("sresult").toString());
							jsonData.put("sfinal", sfinal);
							jsonData.put("sresult", sresult);
							jsonData.put("dentereddate", currentDateTime);
							jsonData.put("dentereddatetimezonecode", userInfo.getNtimezonecode());
							jsonData.put("noffsetdentereddate",dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()));

							updateQuery = updateQuery + "update resultparameter set " + " ngradecode=" + objResult.getNgradecode()
							+ "," + " nenteredby= " + objResult.getNenteredby() + "," + " nenteredrole="
							+ objResult.getNenteredrole() + "," + " ndeputyenteredby="
							+ userInfo.getNdeputyusercode() + "," + " ndeputyenteredrole="
							+ userInfo.getNdeputyuserrole() + "," + " nattachmenttypecode = "
							+ (objResult.getNparametertypecode() == 4 ? 1 : -1) + " ,"
							//								+ " ncalculatedresult = " + objResult.getNcalculatedresult() + ","
							//								+ " sresult=N'" + ReplaceQuote(objResult.getSresult())+ "',"
							//								+ " sfinal=N'" + ReplaceQuote(objResult.getSfinal()) + "',"
							//								+ " dentereddate='"+ getCurrentDateTime(userInfo)+ "',"
							//								+ " ssystemfilename=N'"+ ReplaceQuote(objResult.getSsystemfilename()) + "',"
							//								+ " nfilesize = "+ objResult.getNfilesize()
							+ " jsondata = jsondata || '" +stringUtilityFunction.replaceQuote(objMapper.writeValueAsString(jsonData)) + "'"
							+ " where ntransactionresultcode in( " + transactionresultcode + ");";

							List<ResultParameter> lstSingleResults = lstSampleResults
									.stream().filter(objjqresultparam -> objjqresultparam
											.getNtransactionresultcode() == objResult.getNtransactionresultcode())
									.collect(Collectors.toList());
							if (!lstSingleResults.isEmpty() && lstSingleResults.get(0).getJsondata().get("sresult") != null
									&& !(lstSingleResults.get(0).getJsondata().get("sresult").toString()
											.equalsIgnoreCase((String) jsonData.get("sresult")))) {

								insertResultChange = insertResultChange + "insert into resultchangehistory (nresultchangehistorycode,nformcode, ntransactionresultcode,ntransactiontestcode,npreregno,"
										+ "ngradecode,ntestgrouptestparametercode,nparametertypecode,nenforceresult,nenforcestatus,"
										+ "nenteredby,nenteredrole,ndeputyenteredby,ndeputyenteredrole,jsondata,nsitecode,nstatus) "
										+ "select " + nresultchangehistoryno
										+ "+RANK()over(order by ntransactionresultcode) nresultchangehistorycode,"
										+ userInfo.getNformcode()
										+ ",ntransactionresultcode,ntransactiontestcode,npreregno,"
										+ "ngradecode,ntestgrouptestparametercode,"

										+ lstSingleResults.get(0).getNparametertypecode() + " nparametertypecode," + ""
										+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " nenforceresult,"
										+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " nenforcestatus,"
										+ userInfo.getNusercode() + " nenteredby," + userInfo.getNuserrole()
										+ " nenteredrole," + "" + userInfo.getNdeputyusercode() + " ndeputyenteredby,"
										+ userInfo.getNdeputyuserrole() + " ndeputyenteredrole,'{" + "     \"sresult\":"
										+stringUtilityFunction.replaceQuote(objMapper.writeValueAsString(lstTransSampResultss.get(0).getJsondata().get("sresult")))
										+ "," + "\"sfinal\":"+ stringUtilityFunction.replaceQuote(objMapper.writeValueAsString(lstTransSampResultss.get(0).getJsondata().get("sfinal")))
										+ ","
										+ "     \"dentereddate\":\""
										+ lstSingleResults.get(0).getJsondata().get("dentereddate") + "\","
										+ "     \"dentereddatetimezonecode\":\""
										+ lstSingleResults.get(0).getJsondata().get("dentereddatetimezonecode") + "\","
										+ "     \"noffsetdentereddate\":\""
										+ lstSingleResults.get(0).getJsondata().get("noffsetdentereddate") + "\""
										+ " }'::jsonb,"+userInfo.getNtranssitecode()+","+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " nstatus " + "from resultparameter where nsitecode = "+userInfo.getNtranssitecode()+" and  nstatus = "
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and ntransactionresultcode = " + objResult.getNtransactionresultcode() + ";";

								nresultchangehistoryno++;
								sresultcode.append("," + objResult.getNtransactionresultcode());
								schangeresult.append("," + nresultchangehistoryno);
							}

							if(objResult.getNparametertypecode()==Enumeration.ParameterType.PREDEFINED.getparametertype()){
								if((jsonData.containsKey("sresultcomment")) && (!jsonData.get("sresultcomment").toString().equals(""))) {

									parameterCommentsQuery = parameterCommentsQuery + "update resultparametercomments set jsondata = jsondata || jsonb_build_object('sresultcomment','"+stringUtilityFunction.replaceQuote(jsonData.get("sresultcomment").toString()) +"')::jsonb where ntransactionresultcode = "
											+ objResult.getNtransactionresultcode() + " " + " and nstatus = "
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

								}
							}
						}
						jdbcTemplate.execute(updateQuery);
						jdbcTemplate.execute(insertResultChange);

						if(!lstResulCodePredefined.isEmpty()) {
							String strAuditComment = " select pt.sparametertypename,ra.sarno,rsa.ssamplearno,"
									+ " rc.jsondata->>'sresultcomment' as sresultcomment,rp.jsondata->>'stestsynonym' stestsynonym,"
									+ " rp.jsondata->>'sparametersynonym' sparametersynonym "
									+ " from resultparametercomments rc,resultparameter rp ,parametertype pt,"
									+ " registrationsamplearno rsa,registrationarno ra "
									+ " where rp.ntransactionresultcode = rc.ntransactionresultcode"
									+ " and rp.npreregno = rc.npreregno "
									+ " and ra.npreregno = rp.npreregno "
									+ " and ra.npreregno = rsa.npreregno "
									+ " and rp.nparametertypecode  = pt.nparametertypecode "
									+ " and rp.nsitecode = "+userInfo.getNtranssitecode()+""
									+ " and rc.nsitecode =  "+userInfo.getNtranssitecode()+""
									+ " and rsa.nsitecode =  "+userInfo.getNtranssitecode()+""
									+ " and ra.nsitecode =  "+userInfo.getNtranssitecode()+""
									+ " and pt.nsitecode =  "+userInfo.getNmastersitecode()+""
									+ " and rp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
									+ " and rc.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
									+ " and rsa.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
									+ " and ra.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
									+ " and pt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
									+ " and rp.ntransactionresultcode in (" + stransactionresultcodeforcomments + ")";

							List<ResultParameter> lstCommentsdatabefore = jdbcTemplate.query(strAuditComment, new ResultParameter());

							jdbcTemplate.execute(parameterCommentsQuery);

							String strCommentsafter = " select pt.sparametertypename,ra.sarno,rsa.ssamplearno,"
									+ " rc.jsondata->>'sresultcomment' as sresultcomment,rp.jsondata->>'stestsynonym' stestsynonym,"
									+ " rp.jsondata->>'sparametersynonym' sparametersynonym "
									+ " from resultparametercomments rc,resultparameter rp ,parametertype pt,"
									+ " registrationsamplearno rsa,registrationarno ra "
									+ " where rp.ntransactionresultcode = rc.ntransactionresultcode"
									+ " and rp.npreregno = rc.npreregno "
									+ " and ra.npreregno = rp.npreregno "
									+ " and ra.npreregno = rsa.npreregno "
									+ " and rp.nparametertypecode  = pt.nparametertypecode "
									+ " and rp.nsitecode = "+userInfo.getNtranssitecode()+""
									+ " and rc.nsitecode =  "+userInfo.getNtranssitecode()+""
									+ " and rsa.nsitecode =  "+userInfo.getNtranssitecode()+""
									+ " and ra.nsitecode =  "+userInfo.getNtranssitecode()+""
									+ " and pt.nsitecode =  "+userInfo.getNmastersitecode()+""
									+ " and rp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
									+ " and rc.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
									+ " and rsa.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
									+ " and ra.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
									+ " and pt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
									+ " and rp.ntransactionresultcode in (" + stransactionresultcodeforcomments + ")";

							List<ResultParameter> lstCommentsdataafter = jdbcTemplate.query(strCommentsafter, new ResultParameter());

							jsonAuditOldData.put("resultparametercomments", lstCommentsdatabefore);
							jsonAuditNewData.put("resultparametercomments", lstCommentsdataafter);	
							auditActionType.put("resultparametercomments", "IDS_EDITRESULTPARAMETERCOMMENTS");

						}

						jdbcTemplate.execute("update seqnoregistration  set nsequenceno=" + (nresultchangehistoryno)
								+ " where stablename=N'resultchangehistory' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";");


						query = " select rp.*,rp.jsondata->>'sfinal' sfinal ," + " t.jsondata->'stransdisplaystatus'->>'"
								+ userInfo.getSlanguagetypecode() + "' as stransdisplaystatus,"
								+ " rp.jsondata->>'stestsynonym' stestsynonym,"
								+ " rp.jsondata->>'sparametersynonym' sparametersynonym,"
								+ " r.sarno,rs.ssamplearno,g.sgradename,u.sunitname"
								+ " from resultparameter rp,transactionstatus t,registrationarno r,registrationsamplearno rs,"
								+ " registrationtest rt,grade g,unit u "
								+ " where t.ntranscode=rp.ntransactionstatus and r.npreregno=rp.npreregno and r.npreregno=rs.npreregno"
								+ " and rs.npreregno=rp.npreregno and rt.ntransactionsamplecode=rs.ntransactionsamplecode"
								+ " and rt.ntransactiontestcode=rp.ntransactiontestcode and u.nunitcode=rp.nunitcode  "
								+ " and rt.npreregno=rs.npreregno  and r.npreregno=rt.npreregno and g.ngradecode=rp.ngradecode "

								+ " and rp.nsitecode = "+userInfo.getNtranssitecode()+""
								+ " and r.nsitecode = "+userInfo.getNtranssitecode()+""
								+ " and rs.nsitecode ="+userInfo.getNtranssitecode()+""
								+ " and rt.nsitecode = "+userInfo.getNtranssitecode()+""
								+ " and g.nsitecode = "+userInfo.getNmastersitecode()+""
								+ " and u.nsitecode = "+userInfo.getNmastersitecode()+""

								+ " and rp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
								+ " and r.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
								+ " and rs.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
								+ " and rt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
								+ " and g.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
								+ " and u.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
								+ " and t.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""



								+ " and rp.ntransactionresultcode in (" + stransactionresultcode + ")";

						List<ResultParameter> lstSampleResultsafter = jdbcTemplate.query(query, new ResultParameter());

						List<ResultParameter> lstdataold = new ArrayList<>();
						jsonAuditOldData.put("resultparameter", lstSampleResults);
						jsonAuditNewData.put("resultparameter", lstSampleResultsafter);			
						auditActionType.put("resultparameter", "IDS_RESULTENTER");
						Map<String, Object> objMap = new HashMap<>();
						objMap.put("nregtypecode", nregtypecode);
						objMap.put("nregsubtypecode", nregsubtypecode);

						objMap.put("ndesigntemplatemappingcode", ndesigntemplatemappingcode);
						auditUtilityFunction.fnJsonArrayAudit(jsonAuditOldData, jsonAuditNewData, auditActionType, objMap, false, userInfo);

					}
				}
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SELECTVALIDTEST", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
			return new ResponseEntity<>(returnMap, HttpStatus.OK);

		}
		return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SAMPLENOTSELECTEDSPEC",
				userInfo.getSlanguagefilename()) + ' ' + samplearno, HttpStatus.EXPECTATION_FAILED);

	}



	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getResultEntrySpec(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		Map<String, Object> returnMap = new HashMap<String, Object>();
		List<TestGroupTest> lstMergeParamter = new ArrayList<>();
		String sTestMasterParam = " select tg.nallottedspeccode,(ttm.sleveldescription||' / '||tg.sspecname ||' ('||(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"')||')') sspecname "
				+ " from testgroupspecification tg,transactionstatus ts,treetemplatemanipulation ttm where tg.nallottedspeccode in "
				+ " (select r.nallottedspeccode from registration r, registrationtest rt "
				+ " where r.npreregno=rt.npreregno "
				+ " and r.nsitecode = "+userInfo.getNtranssitecode()+" "
				+ " and rt.nsitecode = "+userInfo.getNtranssitecode()+" "
				+ " and r.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and rt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
				+ " and rt.ntestcode=" + (int) inputMap.get("ntestcode") +" "
				+ " and r.nregtypecode="+ (int) inputMap.get("nregtypecode")+" "
				+ " and r.nregsubtypecode="+(int) inputMap.get("nregsubtypecode") 
				+ " group by r.nallottedspeccode) "
				+ " and ts.ntranscode=tg.napprovalstatus  "
				+ " and ttm.ntemplatemanipulationcode=tg.ntemplatemanipulationcode "

					+ " and tg.nsitecode = "+userInfo.getNmastersitecode() +" "	
					+ " and ttm.nsitecode = "+userInfo.getNmastersitecode() +" "	
					+ " and tg.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "	
					+ " and ts.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and ttm.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " order by tg.dmodifieddate desc";

		List<TestGroupSpecification> lstSpec = jdbcTemplate.query(sTestMasterParam, new TestGroupSpecification());


		String sTestComponent = "  select tgt.nspecsampletypecode as ncomponentcode,c.scomponentname "
				+ " from testgrouptest tgt,testgroupspecsampletype tgst,component c "
				+ " where c.ncomponentcode=tgst.ncomponentcode "
				+ " and tgt.nspecsampletypecode=tgst.nspecsampletypecode "
				+ " and tgt.nsitecode = "+userInfo.getNmastersitecode() +" "	
				+ " and tgst.nsitecode = "+userInfo.getNmastersitecode() +" "	
				+ " and c.nsitecode = "+userInfo.getNmastersitecode() +" "	
				+ " and tgt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "	
				+ " and tgst.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "	
				+ " and c.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "	
				+ " and  tgt.ntestcode="+(int) inputMap.get("ntestcode")+ " "
				+ " and nallottedspeccode="+lstSpec.get(0).getNallottedspeccode();

		List<TestGroupSpecSampleType> lstComponet = jdbcTemplate.query(sTestComponent, new TestGroupSpecSampleType());


		returnMap.put("Specification", lstSpec);
		returnMap.put("nallottedspeccode", lstSpec.get(0));
		returnMap.put("Component", lstComponet);
		returnMap.put("ncomponentcode", lstComponet.get(0));
		returnMap.putAll((Map<String, Object>) getResultEntryParameter(lstSpec.get(0).getNallottedspeccode(),(int) inputMap.get("ntestcode"),"-1",userInfo,lstComponet.get(0).getNcomponentcode()).getBody());

		return new ResponseEntity<>(returnMap, HttpStatus.OK);

	}	

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getResultEntryComponent(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		Map<String, Object> returnMap = new HashMap<String, Object>();
		List<TestGroupTest> lstMergeParamter = new ArrayList<>();


		String sTestComponent = "  select tgt.nspecsampletypecode as ncomponentcode,c.scomponentname "
				+ " from testgrouptest tgt,testgroupspecsampletype tgst,component c "
				+ " where c.ncomponentcode=tgst.ncomponentcode "
				+ " and tgt.nspecsampletypecode=tgst.nspecsampletypecode "
				+ " and tgt.nsitecode = "+userInfo.getNmastersitecode() +" "	
				+ " and tgst.nsitecode = "+userInfo.getNmastersitecode() +" "	
				+ " and c.nsitecode = "+userInfo.getNmastersitecode() +" "	
				+ " and tgt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "	
				+ " and tgst.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "	
				+ " and c.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and tgt.ntestcode="+(int) inputMap.get("ntestcode")+ ""
				+ " and nallottedspeccode="+(int) inputMap.get("nallottedspeccode");

		List<TestGroupSpecSampleType> lstComponet = jdbcTemplate.query(sTestComponent, new TestGroupSpecSampleType());

		returnMap.put("Component", lstComponet);
		returnMap.put("ncomponentcode", lstComponet.get(0));
		returnMap.putAll((Map<String, Object>) getResultEntryParameter((int) inputMap.get("nallottedspeccode"),(int) inputMap.get("ntestcode"),"-1",userInfo,lstComponet.get(0).getNcomponentcode()).getBody());

		return new ResponseEntity<>(returnMap, HttpStatus.OK);

	}


	@Override
	public ResponseEntity<Object> getAdhocTestParamter(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		int ntransactiontestcode=(int) inputMap.get("ntransactiontestcode");
		String activeTestQuery="select * from registrationtesthistory where ntesthistorycode in ( "
				+ " select max(ntesthistorycode) from registrationtesthistory where ntransactiontestcode="+ntransactiontestcode
				+ " and nsitecode = "+userInfo.getNtranssitecode()+" and nstatus=" +  Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+") "
				+ " and nsitecode = "+userInfo.getNtranssitecode()+" and nstatus="+  Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ntransactionstatus in ("+Enumeration.TransactionStatus.INITIATED.gettransactionstatus()+","+Enumeration.TransactionStatus.RECALC.gettransactionstatus()+")";

		List<RegistrationTestHistory> lstquery = jdbcTemplate.query(activeTestQuery, new RegistrationTestHistory());

		if(lstquery.size()>0) {

			String activeParameterQuery=" select tp.sparametersynonym,tp.ntestparametercode,tp.nparametertypecode,tp.nroundingdigits,tp.nunitcode,tp.ntestcode  "
					+ " from testparameter tp,registrationtest rt where "
					+ " tp.ntestcode=rt.ntestcode and rt.ntransactiontestcode= "+ntransactiontestcode+" and tp.ntestparametercode not in ( "
					+ " select tp.ntestparametercode from resultparameter rp,testparameter tp where rp.ntestparametercode=tp.ntestparametercode "
					+ " and rp.nsitecode = "+userInfo.getNtranssitecode()+""
					+ " and tp.nsitecode = "+userInfo.getNmastersitecode()+""
					+ " and rp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and rp.ntransactiontestcode=" + ntransactiontestcode + ") "
					+ " and rp.nsitecode = "+userInfo.getNtranssitecode()+""
					+ " and tp.nsitecode = "+userInfo.getNmastersitecode()+""
					+ " and rt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "	and  tp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" order by sparametersynonym ";

			List<TestParameter> lsttestparam = jdbcTemplate.query(activeParameterQuery, new TestParameter());
			returnMap.put("TestParameter", lsttestparam);
		}else {
			returnMap.put("TestParameter",new ArrayList<>());
		}
		return new ResponseEntity<>(returnMap, HttpStatus.OK);	
	}



	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getCreateParameterSeqNo(final Map<String, Object> inputMap,final UserInfo objUserInfo) throws Exception {

		Map<String, Object> returnMap = new HashMap<>();
		StringBuilder sb = new StringBuilder();
		String StrQuery = "";
		final String sparametercode = (String) inputMap.get("ntestparametercode");
		final int ntestgrouptestcode = (int) inputMap.get("ntestgrouptestcode");
		final String ntestcode = (String) inputMap.get("ntestcode");
		final int ntransactiontestcode = (int) inputMap.get("ntransactiontestcode");
		final int npreregno = (int) inputMap.get("npreregno");

		StrQuery = "select ntestformulacode,ndefaultstatus from testformula where ntestparametercode in ("+ sparametercode + ") "
				+ " and nsitecode = "+objUserInfo.getNmastersitecode()+" and nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

		List<TestFormula> lstTestFormula = jdbcTemplate.query(StrQuery, new TestFormula());

		StrQuery = "select  tsc.* from testparameter tp,testmasterclinicalspec tsc where  "
				+ "  tp.ntestparametercode=tsc.ntestparametercode "
				+ " and tp.nsitecode ="+ objUserInfo.getNmastersitecode() + " "
				+ " and tsc.nsitecode ="+ objUserInfo.getNmastersitecode() + ""
				+ " and tp.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and tsc.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ "and tp.ntestparametercode in (" + sparametercode + ") ";

		List<TestMasterClinicalSpec> lstClinialSpec = jdbcTemplate.query(StrQuery,new TestMasterClinicalSpec());

		StrQuery = "select nparametertypecode from testparameter where ntestparametercode in (" + sparametercode+ ") "
				+ " and nsitecode  = "+objUserInfo.getNmastersitecode()+" and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

		List<TestParameter> lstTestParameter = jdbcTemplate.query(StrQuery, new TestParameter());

		final Map<String, Object> mapLists = new HashMap<String, Object>();
		mapLists.put("TestFormula", lstTestFormula);
		mapLists.put("TestMasterClinicalSpec", lstClinialSpec);

		List<TestFormula> lstTForm = (List<TestFormula>) mapLists.get(TestFormula.class.getSimpleName());
		List<TestMasterClinicalSpec> lstParametrClinialSpec = (List<TestMasterClinicalSpec>) mapLists
				.get(TestMasterClinicalSpec.class.getSimpleName());

		lstTForm = lstTForm.stream()
				.filter(x -> x.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus())
				.collect(Collectors.toList());
		int ntestFormcount = (lstTForm != null && !lstTForm.isEmpty()) ? lstTForm.size() : 0;
		int nclinicalspeccount = (lstParametrClinialSpec != null && !lstParametrClinialSpec.isEmpty())
				? lstParametrClinialSpec.size()
						: 0;
		final int ncharparamcount = (int) lstTestParameter.stream().filter(objTP -> objTP
				.getNparametertypecode() == Enumeration.ParameterType.CHARACTER.getparametertype()).count();
		final int nnumericcount = (int) lstTestParameter.stream().filter(
				objTP -> objTP.getNparametertypecode() == Enumeration.ParameterType.NUMERIC.getparametertype())
				.count();
		final int npredefparamcount = (int) lstTestParameter.stream().filter(objTP -> objTP
				.getNparametertypecode() == Enumeration.ParameterType.PREDEFINED.getparametertype()).count();

		final String ValidateQuery = " select * from testgrouptestparameter where ntestgrouptestcode in ("
				+ ntestgrouptestcode + ") and ntestparametercode in (" + sparametercode+ ") and nsitecode = "+objUserInfo.getNmastersitecode()+" "
				+ " and nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";

		List<TestGroupTestParameter> isValidateQuery = jdbcTemplate.query(ValidateQuery,new TestGroupTestParameter());

		if (isValidateQuery.size() == 0) {
			final String strSelectSeqno = "select stablename, nsequenceno from seqnotestgroupmanagement where stablename in ('testgrouptestparameter',"
					+ "'testgrouptestcharparameter','testgrouptestformula','testgrouptestnumericparameter',"
					+ "'testgrouptestpredefparameter','testgrouptestclinicalspec') and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";

			final List<?> lstMultiSeqNo = projectDAOSupport.getMultipleEntitiesResultSetInList(strSelectSeqno,jdbcTemplate,	SeqNoTestGroupmanagement.class);

			final List<SeqNoTestGroupmanagement> lstSeqNoReg = (List<SeqNoTestGroupmanagement>) lstMultiSeqNo.get(0);

			returnMap = lstSeqNoReg.stream().collect(Collectors.toMap(SeqNoTestGroupmanagement::getStablename,
					SeqNoTestGroupmanagement -> SeqNoTestGroupmanagement.getNsequenceno()));

			sb.append("update seqnotestgroupmanagement set nsequenceno="+ ((int) returnMap.get("testgrouptestcharparameter") + ncharparamcount)
					+ "  where stablename=N'testgrouptestcharparameter' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";");

			sb.append("update seqnotestgroupmanagement set nsequenceno="+ ((int) returnMap.get("testgrouptestclinicalspec") + nclinicalspeccount)
					+ "  where stablename=N'testgrouptestclinicalspec' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";");

			sb.append("update seqnotestgroupmanagement set nsequenceno="+ ((int) returnMap.get("testgrouptestformula") + ntestFormcount)
					+ "  where stablename=N'testgrouptestformula' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";");

			sb.append("update seqnotestgroupmanagement set nsequenceno="+ ((int) returnMap.get("testgrouptestnumericparameter") + nnumericcount)
					+ "  where stablename=N'testgrouptestnumericparameter' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";");

			sb.append("update seqnotestgroupmanagement set nsequenceno="+ ((int) returnMap.get("testgrouptestparameter") + lstTestParameter.size())
					+ "  where stablename=N'testgrouptestparameter' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";");

			sb.append("update seqnotestgroupmanagement set nsequenceno="+ ((int) returnMap.get("testgrouptestpredefparameter") + npredefparamcount)
					+ "  where stablename=N'testgrouptestpredefparameter' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";");

			jdbcTemplate.execute(sb.toString());

			returnMap.put("ntestgrouptestcharcode", (int) returnMap.get("testgrouptestcharparameter"));
			returnMap.put("ntestgrouptestclinicspeccode", (int) returnMap.get("testgrouptestclinicalspec"));
			returnMap.put("ntestgrouptestformulacode", (int) returnMap.get("testgrouptestformula"));
			returnMap.put("ntestgrouptestnumericcode", (int) returnMap.get("testgrouptestnumericparameter"));
			returnMap.put("ntestgrouptestparametercode", (int) returnMap.get("testgrouptestparameter"));
			returnMap.put("ntestgrouptestpredefcode", (int) returnMap.get("testgrouptestpredefparameter"));
		}

		String sRegParamSeqno = " select nsequenceno from seqnoregistration where stablename='registrationparameter' "
				+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";

		final int nRegParamseqno = (int) jdbcUtilityFunction.queryForObject(sRegParamSeqno, Integer.class,jdbcTemplate);

		final int TotalRegParamseqno = nRegParamseqno + lstTestParameter.size();
		String updateRegParamSeqno = " update seqnoregistration set nsequenceno=" + TotalRegParamseqno + " "
				+ " where stablename='registrationparameter' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
		jdbcTemplate.execute(updateRegParamSeqno);

		returnMap.put("ntransactionresultcode", nRegParamseqno);
		returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),Enumeration.ReturnStatus.SUCCESS.getreturnstatus());

		return returnMap;
	}


	@Override
	public ResponseEntity<Object> createAdhocTestParamter(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		ObjectMapper objmapper = new ObjectMapper();
		Map<String, Object> returnMap = new HashMap<>();
		JSONObject jsonAuditNewData = new JSONObject();
		JSONObject auditActionType = new JSONObject();

		final int ntestgrouptestcode = (int) inputMap.get("ntestgrouptestcode");
		final String ntestcode = (String) inputMap.get("ntestcode");
		final String ntestparametercode = (String) inputMap.get("ntestparametercode");
		final int ntransactiontestcode = (int) inputMap.get("ntransactiontestcode");
		final int npreregno = (int) inputMap.get("npreregno");
		final int nisvisible = (int) inputMap.get("nisvisible");
		final String multiselecttransactiontestcode = (String) inputMap.get("multiselecttransactiontestcode");
		final int nregtypecode = (int) inputMap.get("nregtypecode");
		final int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
		final int ndesigntemplatemappingcode = (int) inputMap.get("ndesigntemplatemappingcode");
		final int nclinicaltyperequired = (int) inputMap.get("nclinicaltyperequired");

		String sQuery = "";
		String sQuerys = " select * from testparameter where ntestparametercode in (" + ntestparametercode + ")"
				+ " and nsitecode=" + userInfo.getNmastersitecode() + " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
		List<TestParameter> lstTestParam = jdbcTemplate.query(sQuerys, new TestParameter());

		String validateQuery = " select * from testgrouptestparameter where ntestgrouptestcode in ("+ ntestgrouptestcode + ")"
				+ "  and ntestparametercode in (" + ntestparametercode + ") "
				+ " and nsitecode=" + userInfo.getNmastersitecode() + " "
				+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		List<TestGroupTestParameter> isValidateQuery = jdbcTemplate.query(validateQuery,new TestGroupTestParameter());

		if (lstTestParam.size() > 0) {

			if (isValidateQuery.size() > 0) {
				if (isValidateQuery.get(0).getNisvisible() != nisvisible) {
					String updateQuery = "update testgrouptestparameter set nisvisible=" + nisvisible
							+ " where  ntestgrouptestcode in (" + ntestgrouptestcode + ") "
							+ " and ntestparametercode in (" + ntestparametercode + ") and nsitecode="
							+ userInfo.getNmastersitecode() + " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					jdbcTemplate.execute(updateQuery);
				}
			} else {

				final int ntestgrouptestcharcode = (int) inputMap.get("ntestgrouptestcharcode");
				final int ntestgrouptestclinicspeccode = (int) inputMap.get("ntestgrouptestclinicspeccode");
				final int ntestgrouptestformulacode = (int) inputMap.get("ntestgrouptestformulacode");
				final int ntestgrouptestnumericcode = (int) inputMap.get("ntestgrouptestnumericcode");
				final int ntestgrouptestparametercode = (int) inputMap.get("ntestgrouptestparametercode");
				final int ntestgrouptestpredefcode = (int) inputMap.get("ntestgrouptestpredefcode");

				final String sorterQuery = "select max(nsorter) from testgrouptestparameter where ntestgrouptestcode in ("
						+ ntestgrouptestcode + ")" + " and nsitecode=" + userInfo.getNmastersitecode()
						+ " and  nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

				int nsorter = (int) jdbcUtilityFunction.queryForObject(sorterQuery, Integer.class,jdbcTemplate);

				final String insertQuery = "INSERT INTO testgrouptestparameter("
						+ "	ntestgrouptestparametercode, ntestgrouptestcode, ntestparametercode,"
						+ "	nparametertypecode, nunitcode, sparametersynonym, nroundingdigits,"
						+ "	nresultmandatory, nreportmandatory, ngradingmandatory, nchecklistversioncode,"
						+ "	sspecdesc, nsorter, nisadhocparameter, nisvisible, nsitecode, dmodifieddate, nstatus,nresultaccuracycode)"
						+ " select  " + ntestgrouptestparametercode
						+ "+RANK()over(order by ntestparametercode)ntestgrouptestparametercode," + " "
						+ ntestgrouptestcode + ",ntestparametercode,"
						+ "	nparametertypecode,nunitcode,sparametersynonym,nroundingdigits," + " "
						+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " nresultmandatory,"
						+ "	" + Enumeration.TransactionStatus.YES.gettransactionstatus() + " nreportmandatory,"
						+ "	" + Enumeration.TransactionStatus.NO.gettransactionstatus() + " ngradingmandatory,"
						+ " -1 nchecklistversioncode,NULL sspecdesc, "
						+ nsorter + "+RANK() over(order by ntestparametercode) nsorter," + " "
						+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " nisadhocparameter, "
						+ " " + nisvisible + " nisvisible," + " " + userInfo.getNmastersitecode()
						+ " nsitecode,"
						+ "	(SELECT to_char(now(),'YYYY-MM-DD HH:mi:ss')::timestamp without time zone)," + " "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus ,"+Enumeration.TransactionStatus.NA.gettransactionstatus() 
						+ "	from testparameter where ntestparametercode in(" + ntestparametercode + ")"
						+ " and nsitecode = " + userInfo.getNmastersitecode() + "" + " and nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() ;

				jdbcTemplate.execute(insertQuery);

				final String predeParameter = "insert into testgrouptestpredefparameter (ntestgrouptestpredefcode, ntestgrouptestparametercode, ngradecode, spredefinedname,spredefinedsynonym,"
						+ " ndefaultstatus,dmodifieddate,nstatus,nsitecode)" + " select "
						+ ntestgrouptestpredefcode
						+ "+RANK()over(order by tfp.ntestparametercode)ntestgrouptestpredefcode,tp.ntestgrouptestparametercode "
						+ " ,tfp.ngradecode,tfp.spredefinedname,tfp.spredefinedsynonym,tfp.ndefaultstatus, (SELECT to_char(now(),'YYYY-MM-DD HH:mi:ss')::timestamp without time zone) dmodifieddate"
						+ " ," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus, "
						+ userInfo.getNmastersitecode() + " nsitecode "
						+ " from testpredefinedparameter tfp ,testgrouptestparameter tp where tp.ntestparametercode=tfp.ntestparametercode and  "
						+ " tfp.ntestparametercode in(" + ntestparametercode + ") "
						+ " and tfp.nsitecode = "+ userInfo.getNmastersitecode() + " "
						+ " and tp.nsitecode = "+ userInfo.getNmastersitecode() + " "
						+ " and tp.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and tfp.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and tp.ntestgrouptestparametercode not in  (select  tgp.ntestgrouptestparametercode from testgrouptestpredefparameter tgp "
						+ " where tgp.ntestgrouptestparametercode=tp.ntestgrouptestparametercode and tgp.nsitecode = "+userInfo.getNmastersitecode()+" and tgp.nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
				jdbcTemplate.execute(predeParameter);

				final String charParameter = "insert into testgrouptestcharparameter (ntestgrouptestcharcode, ntestgrouptestparametercode, scharname,dmodifieddate,nstatus,nsitecode)"
						+ "  select " + ntestgrouptestcharcode
						+ "+RANK()over(order by tfp.ntestparametercode)ntestgrouptestcharcode ,tp.ntestgrouptestparametercode "
						+ ",NULL scharname ,'" +dateUtilityFunction.getCurrentDateTime(userInfo) + "' dmodifieddate ,"
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus ,"
						+ userInfo.getNmastersitecode()
						+ " nsitecode from  testparameter tfp ,testgrouptestparameter tp "
						+ " where tp.ntestparametercode=tfp.ntestparametercode and  tfp.ntestparametercode in("
						+ ntestparametercode + ")"
						+ "  and tfp.nsitecode = " + userInfo.getNmastersitecode()
						+ "  and tp.nsitecode = " + userInfo.getNmastersitecode()
						+ " and tfp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and tp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " AND tfp.nparametertypecode = "+ Enumeration.ParameterType.CHARACTER.getparametertype()
						+ " and tp.ntestgrouptestparametercode not in (select tcp.ntestgrouptestparametercode from testgrouptestcharparameter tcp where "
						+ " tp.ntestgrouptestparametercode=tcp.ntestgrouptestparametercode and tcp.nsitecode = "+userInfo.getNmastersitecode()+" and tcp.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "); ";

				jdbcTemplate.execute(charParameter);

				final String numricParameter = "insert into testgrouptestnumericparameter (ntestgrouptestnumericcode, ntestgrouptestparametercode, sminlod, smaxlod, sminb,"
						+ " smina, smaxa, smaxb, sminloq, smaxloq, sdisregard, sresultvalue,dmodifieddate,nstatus,nsitecode,ngradecode)"
						+ " select " + ntestgrouptestnumericcode
						+ "+RANK()over(order by tfp.ntestparametercode)ntestgrouptestnumericcode,tp.ntestgrouptestparametercode "
						+ " ,tfp.sminlod,tfp.smaxlod,tfp.sminb,tfp.smina,tfp.smaxa,tfp.smaxb,tfp.sminloq,tfp.smaxloq,tfp.sdisregard,tfp.sresultvalue, (SELECT to_char(now(),'YYYY-MM-DD HH:mi:ss')::timestamp without time zone) dmodifieddate"
						+ " ," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus, "
						+ userInfo.getNmastersitecode() + " nsitecode,tfp.ngradecode  "
						+ " from testparameternumeric tfp ,testgrouptestparameter tp where tp.ntestparametercode=tfp.ntestparametercode and  "
						+ " tfp.ntestparametercode in(" + ntestparametercode + ") "
						+ " and tfp.nsitecode = "	+ userInfo.getNmastersitecode() + " "
						+ " and tp.nsitecode = " + userInfo.getNmastersitecode()
						+ " and tp.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and tfp.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and tp.ntestgrouptestparametercode not in (select ttp.ntestgrouptestparametercode from  testgrouptestnumericparameter ttp where ttp.ntestgrouptestparametercode=tp.ntestgrouptestparametercode  "
						+ " and tpp.nsitecode = "+userInfo.getNmastersitecode()+" and ttp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ "); ";

				jdbcTemplate.execute(numricParameter);

				if (nclinicaltyperequired == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					//ALPD-5054 added by Dhanushya RI,To insert fromage period and toage period
					final String clinialSpec = "insert into testgrouptestclinicalspec (ntestgrouptestclinicspeccode, ntestgrouptestparametercode, ngendercode, nfromage, ntoage, shigha, shighb, slowa, slowb, sresultvalue,"
							+ " sminlod, smaxlod, sminloq, smaxloq, sdisregard, ntzmodifieddate, noffsetdmodifieddate,dmodifieddate, nstatus,nsitecode, ngradecode,nfromageperiod,ntoageperiod) "
							+ "  select " + ntestgrouptestclinicspeccode
							+ "+RANK()over(order by tfp.ntestparametercode)ntestgrouptestclinicspeccode,tp.ntestgrouptestparametercode,tfp.ngendercode,tfp.nfromage,tfp.ntoage "
							+ "	 ,tfp.shigha,tfp.shighb,tfp.slowa,tfp.slowb,tfp.sresultvalue,tfp.sminlod,tfp.smaxlod,tfp.sminloq,tfp.smaxloq,tfp.sdisregard,"
							+ userInfo.getNtimezonecode() + " ntzmodifieddate ,"
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
							+ " noffsetdmodifieddate ,(SELECT to_char(now(),'YYYY-MM-DD HH:mi:ss')::timestamp without time zone) dmodifieddate"
							+ " ," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus, "
							+ userInfo.getNmastersitecode() + " nsitecode,tfp.ngradecode,tfp.nfromageperiod,tfp.ntoageperiod "
							+ "	 from testmasterclinicalspec tfp ,testgrouptestparameter tp where tp.ntestparametercode=tfp.ntestparametercode and  "
							+ "	 tfp.ntestparametercode in(" + ntestparametercode + ") "
							+ " and tp.nsitecode = " + userInfo.getNmastersitecode()
							+ " and tfp.nsitecode = "+ userInfo.getNmastersitecode() + "	 "
							+ " and tp.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and tfp.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and tp.ntestgrouptestparametercode not in (select ttp.ntestgrouptestparametercode from  testgrouptestclinicalspec ttp where ttp.ntestgrouptestparametercode=tp.ntestgrouptestparametercode "
							+ " and ttp.nsitecode = "+userInfo.getNmastersitecode()+" and ttp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ "); ";

					jdbcTemplate.execute(clinialSpec);
				}
				final String testFormula = "insert into testgrouptestformula (ntestgrouptestformulacode, ntestgrouptestcode, ntestgrouptestparametercode, ntestformulacode,"
						+ " sformulacalculationcode, sformulacalculationdetail, ntransactionstatus, dmodifieddate,nstatus,nsitecode) "
						+ "  select " + ntestgrouptestformulacode
						+ "+RANK()over(order by tfp.ntestparametercode) ntestgrouptestformulacode,"
						+ ntestgrouptestcode + ",tp.ntestgrouptestparametercode,"
						+ " tfp.ntestformulacode,tfp.sformulacalculationcode,tfp.sformulacalculationdetail,tfp.ndefaultstatus ntransactionstatus, "
						+ "  (SELECT to_char(now(),'YYYY-MM-DD HH:mi:ss')::timestamp without time zone) dmodifieddate"
						+ "	 ," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus, "
						+ userInfo.getNmastersitecode() + " nsitecode  "
						+ "	 from testformula tfp ,testgrouptestparameter tp where tp.ntestparametercode=tfp.ntestparametercode and  "
						+ "	 tfp.ntestparametercode in(" + ntestparametercode + ") "
						+ " and tfp.nsitecode = "+ userInfo.getNmastersitecode() + "	 "
						+ " and tp.nsitecode = " + userInfo.getNmastersitecode()
						+ " and tp.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " and  tfp.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and tp.ntestgrouptestparametercode not in (select ttp.ntestgrouptestparametercode from  testgrouptestformula ttp where ttp.ntestgrouptestparametercode=tp.ntestgrouptestparametercode "
						+ " and ttp.nsitecode = "+userInfo.getNmastersitecode()+" and ttp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ "); ";

				jdbcTemplate.execute(testFormula);

				String sAuditQuery = "";

				// -----
				sAuditQuery = "select tgtp.ntestgrouptestparametercode, tgtp.ntestgrouptestcode, tgtp.ntestparametercode,"
						+ " tgtp.nparametertypecode, tgtp.nunitcode, tgtp.sparametersynonym, tgtp.nroundingdigits, tgtp.nresultmandatory,"
						+ " tgtp.nreportmandatory, tgtp.ngradingmandatory, tgtp.nchecklistversioncode, tgtp.sspecdesc, tgtp.nsorter,"
						+ " tgtp.nisadhocparameter, tgtp.nisvisible, tgtp.nsitecode, tgtp.nstatus, pt.sdisplaystatus from testgrouptestparameter tgtp,"
						+ " parametertype pt where tgtp.nparametertypecode=pt.nparametertypecode and ntestgrouptestcode in (" + ntestgrouptestcode + ") "
						+ " and tgtp.ntestparametercode in ("+ ntestparametercode + ") "
						+ " and tgtp.nsitecode="+ userInfo.getNmastersitecode() + " "
						+ " and pt.nsitecode="+ userInfo.getNmastersitecode() + ""
						+ " and tgtp.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and pt.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				List<TestGroupTestParameter> lstTestGroupTestParameter = objmapper.convertValue(
						commonFunction.getMultilingualMessageList(jdbcTemplate.query(sAuditQuery, new TestGroupTestParameter()),
								Arrays.asList("sdisplaystatus"), userInfo.getSlanguagefilename()),
						new TypeReference<List<TestGroupTestParameter>>() {
						});

				sAuditQuery = "select tf.* from testgrouptestformula tf, testgrouptestparameter tp where tf.ntestgrouptestcode "
						+ " in (" + ntestgrouptestcode
						+ ") and tf.ntestgrouptestparametercode=tp.ntestgrouptestparametercode  "
						+ " and tp.ntestparametercode in (" + ntestparametercode + ") "
						+ " and tf.nsitecode="+ userInfo.getNmastersitecode() + " "
						+ " and tp.nsitecode="+ userInfo.getNmastersitecode() + " "
						+ " and tf.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and tp.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ;";

				List<TestGroupTestFormula> lstTestGroupTestFormula = jdbcTemplate.query(sAuditQuery,
						new TestGroupTestFormula());

				// ---
				sAuditQuery = "select * from testgrouptestpredefparameter tgtpp,testgrouptestparameter tgtp"
						+ "  where tgtp.ntestgrouptestparametercode = tgtpp.ntestgrouptestparametercode  "
						+ " and tgtpp.nsitecode="+ userInfo.getNmastersitecode() + " "
						+ " and tgtp.nsitecode="+ userInfo.getNmastersitecode() + " "
						+ " and tgtpp.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " and tgtp.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and tgtp.ntestgrouptestcode in ("+ ntestgrouptestcode + ");";

				List<TestGroupTestPredefinedParameter> lstTestGroupTestPredefinedParameter = jdbcTemplate.query(sAuditQuery, new TestGroupTestPredefinedParameter());

				sAuditQuery = "select * from testgrouptestcharparameter tgtcp,testgrouptestparameter tgtp "
						+ " where tgtp.ntestgrouptestparametercode = tgtcp.ntestgrouptestparametercode "
						+ " and tgtcp.nsitecode="+ userInfo.getNmastersitecode() + " "
						+ " and tgtp.nsitecode="+ userInfo.getNmastersitecode() + " "
						+ " and tgtp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and tgtcp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and tgtp.ntestgrouptestcode in ("+ ntestgrouptestcode + ") and tgtp.ntestparametercode in (" + ntestparametercode+ ");";

				List<TestGroupTestCharParameter> lstTestGroupTestCharParameter = jdbcTemplate.query(sAuditQuery, new TestGroupTestCharParameter());

				sAuditQuery = "select * from testgrouptestnumericparameter tgtnp,testgrouptestparameter tgtp where"
						+ " tgtp.ntestgrouptestparametercode = tgtnp.ntestgrouptestparametercode "
						+ " and tgtnp.nsitecode="+ userInfo.getNmastersitecode() + " "
						+ " and tgtp.nsitecode="+ userInfo.getNmastersitecode() + ""
						+ " and tgtnp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and tgtp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and tgtp.ntestgrouptestcode in ("+ ntestgrouptestcode + ") "
						+ " and tgtp.ntestparametercode in (" + ntestparametercode+ ");";
				List<TestGroupTestNumericParameter> lstTestGroupTestNumericParameter = jdbcTemplate.query(sAuditQuery, new TestGroupTestNumericParameter());

				List<Object> lstnewobject = new ArrayList<>();
				lstnewobject.add(lstTestGroupTestParameter);
				lstnewobject.add(lstTestGroupTestFormula);
				lstnewobject.add(lstTestGroupTestPredefinedParameter);
				lstnewobject.add(lstTestGroupTestCharParameter);
				lstnewobject.add(lstTestGroupTestNumericParameter);

				List<String> lstAction = new ArrayList<String>();
				lstAction.add("IDS_ADDTESTPARAMETER");
				lstAction.add("IDS_ADDTESTFORMULA");
				lstAction.add("IDS_ADDPREDEFINEDPARAMETER");
				lstAction.add("IDS_ADDCHARACTERRESULT");
				lstAction.add("IDS_ADDSPECLIMIT");

				UserInfo userInfo1 = new UserInfo(userInfo);
				userInfo1.setNformcode((short) Enumeration.FormCode.TESTGROUP.getFormCode());
				userInfo1.setSformname(commonFunction.getMultilingualMessage("IDS_STUDYPLAN",userInfo1.getSlanguagefilename()));
				userInfo1.setSmodulename(commonFunction.getMultilingualMessage("IDS_TESTGROUPMANAGEMENT",userInfo1.getSlanguagefilename()));

				auditUtilityFunction.fnInsertListAuditAction(lstnewobject, 1, null, lstAction, userInfo1);

			}

			returnMap.putAll(CreateRegistrationParameter(inputMap, userInfo));
			return new ResponseEntity<>(returnMap, HttpStatus.OK);

		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTVALIDPARAMETER",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}



	@SuppressWarnings("unchecked")
	public Map<String, Object> CreateRegistrationParameter(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		JSONObject jsonAuditNewData = new JSONObject();
		JSONObject auditActionType = new JSONObject();

		final int ntestgrouptestcode = (int) inputMap.get("ntestgrouptestcode");
		final String ntestcode = (String) inputMap.get("ntestcode");
		final String ntestparametercode = (String) inputMap.get("ntestparametercode");
		final int ntransactiontestcode = (int) inputMap.get("ntransactiontestcode");
		final int npreregno = (int) inputMap.get("npreregno");
		final int nisvisible = (int) inputMap.get("nisvisible");
		final String multiselecttransactiontestcode = (String) inputMap.get("multiselecttransactiontestcode");
		final int nregtypecode = (int) inputMap.get("nregtypecode");
		final int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
		final int ndesigntemplatemappingcode = (int) inputMap.get("ndesigntemplatemappingcode");
		final int nclinicaltyperequired = (int) inputMap.get("nclinicaltyperequired");
		int nRegParamseqno = (int) inputMap.get("ntransactionresultcode");

		final String insertRegParamQuery = " INSERT INTO registrationparameter("
				+ " ntransactionresultcode, ntransactiontestcode, npreregno,ntestgrouptestparametercode,"
				+ " ntestparametercode, nparametertypecode, ntestgrouptestformulacode, nreportmandatory,"
				+ " nresultmandatory, nunitcode, jsondata, nsitecode, nstatus)" + " select " + nRegParamseqno
				+ "+RANK() over(order by tgtp.ntestgrouptestparametercode)," + " " + ntransactiontestcode
				+ " ntransactiontestcode," + " " + npreregno + " npreregno,"
				+ " tgtp.ntestgrouptestparametercode," + " tgtp.ntestparametercode,"
				+ " tgtp.nparametertypecode,COALESCE(tgf.ntestgrouptestformulacode,-1) ntestgrouptestformulacode,"
				+ " " + Enumeration.TransactionStatus.YES.gettransactionstatus() + "," + " "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + "," + " tgtp.nunitcode,"
				+ "json_build_object('ntransactionresultcode'," + nRegParamseqno
				+ "+RANK() over(order by  tgtp.ntestgrouptestparametercode),'ntransactiontestcode',"
				+ ntransactiontestcode + "," + " 'npreregno'," + npreregno + ","
				+ " 'sminlod',tgtnp.sminlod,'smaxlod',tgtnp.smaxlod,'sminb',tgtnp.sminb,'smina',tgtnp.smina,'smaxa',tgtnp.smaxa,'smaxb',tgtnp.smaxb,"
				+ " 'sminloq',tgtnp.sminloq,'smaxloq',tgtnp.smaxloq,'sdisregard',tgtnp.sdisregard,'sresultvalue',tgtnp.sresultvalue,"
				+ " 'nroundingdigits',tgtp.nroundingdigits,'sresult',NULL,'sfinal',NULL,'stestsynonym',concat(tgt.stestsynonym,'[1][0]'),"
				+ " 'sparametersynonym',tgtp.sparametersynonym)::jsonb," + " " + userInfo.getNtranssitecode()
				+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " from testgrouptest tgt,testgrouptestparameter tgtp "
				+ " left outer join testgrouptestnumericparameter tgtnp on tgtnp.ntestgrouptestparametercode=tgtp.ntestgrouptestparametercode "
				+ " and tgtnp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and tgtnp.nsitecode = "+userInfo.getNmastersitecode()+" "
				+ " left outer join testgrouptestformula tgf on tgtnp.ntestgrouptestparametercode=tgf.ntestgrouptestparametercode"
				+ " and tgf.ntransactionstatus = " + Enumeration.TransactionStatus.YES.gettransactionstatus()+" and tgf.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and tgf.nsitecode = "+userInfo.getNmastersitecode()+" "
				+ " where tgt.ntestgrouptestcode=tgtp.ntestgrouptestcode "
				+ " and tgt.nsitecode="+ userInfo.getNmastersitecode() + " "
				+ " and tgtp.nsitecode="+ userInfo.getNmastersitecode() + ""
				+ " and tgt.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and tgtp.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and tgtp.ntestgrouptestcode in (" + ntestgrouptestcode + ")"
				+ " and tgtp.ntestparametercode in(" + ntestparametercode
				+ ") and tgtp.ntestgrouptestparametercode not in "
				+ " (select ntestgrouptestparametercode from registrationparameter  rp where rp.ntestgrouptestparametercode=tgtp.ntestgrouptestparametercode "
				+ " and rp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and rp.nsitecode = "+userInfo.getNtranssitecode()+""
				+ " and rp.ntransactiontestcode=" + ntransactiontestcode + ");";

		jdbcTemplate.execute(insertRegParamQuery);

		final String query3 = "insert into resultparameter( "
				+ "ntransactionresultcode,npreregno,ntransactiontestcode,ntestgrouptestparametercode "
				+ ",ntestparametercode,nparametertypecode,nresultmandatory,nreportmandatory,ntestgrouptestformulacode"
				+ ",nunitcode,ngradecode,ntransactionstatus,nenforcestatus,nenforceresult,ncalculatedresult,"
				+ "nenteredby,nenteredrole,ndeputyenteredby " + ",ndeputyenteredrole "
				+ ",nlinkcode,nattachmenttypecode,jsondata,nsitecode, nstatus) "
				+ " select rp.ntransactionresultcode,rp.npreregno, "
				+ "rp.ntransactiontestcode,rp.ntestgrouptestparametercode,rp.ntestparametercode,rp.nparametertypecode, "
				+ "" + Enumeration.TransactionStatus.YES.gettransactionstatus() + " nresultmandatory," + ""
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " nreportmandatory,"
				+ " coalesce(rp.ntestgrouptestformulacode,"
				+ Enumeration.TransactionStatus.NA.gettransactionstatus()
				+ ") as ntestgrouptestformulacode, rp.nunitcode,"
				+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " as ngradecode," + ""
				+ Enumeration.TransactionStatus.REGISTERED.gettransactionstatus() + " as ntransactionstatus ,"
				+ "" + Enumeration.TransactionStatus.NO.gettransactionstatus() + " as nenforcestatus,"
				+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " as nenforceresult,"
				+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " as ncalculatedresult,"
				+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ",rp.jsondata,"
				+ userInfo.getNtranssitecode() + ","
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " from registrationparameter rp"
				+ " where rp.npreregno in (" + npreregno + ") and rp.ntestparametercode in("
				+ ntestparametercode + ") and rp.ntransactiontestcode=" + ntransactiontestcode + ""
				+ " and rp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and rp.nsitecode=" + userInfo.getNtranssitecode() + " ;";

		jdbcTemplate.execute(query3);

		JSONObject jsonObject = new JSONObject();
		final String query4 = "insert into resultparametercomments (ntransactionresultcode, ntransactiontestcode, npreregno, "
				+ "ntestgrouptestparametercode, ntestparametercode,jsondata, nsitecode, nstatus)"
				+ " select  rp.ntransactionresultcode,"
				+ " rp.ntransactiontestcode, rp.npreregno, rp.ntestgrouptestparametercode, rp.ntestparametercode,'"
				+ jsonObject + "' jsondata, " + userInfo.getNtranssitecode() + " nsitecode,"
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus"
				+ " from resultparameter rp where rp.ntransactiontestcode in (" + ntransactiontestcode
				+ ")  and rp.ntestparametercode in(" + ntestparametercode + ") and rp.npreregno in ("
				+ npreregno + ") and rp.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rp.nsitecode="
				+ userInfo.getNtranssitecode();

		jdbcTemplate.execute(query4);

		final String sQuery5 = " select rp.ntransactionresultcode,rarno.sarno,tm.stestsynonym||'['|| CAST(rt.ntestrepeatno AS character varying(10))||']"
				+ "['||CAST(rt.ntestretestno AS character varying(10))||']'stestsynonym,"
				+ " tgtp.sparametername as sparametersynonym,"
				+ " rp.npreregno,rp.ntransactiontestcode,rp.ntestgrouptestparametercode,rp.ntestparametercode,"
				+ " rp.nparametertypecode,rp.jsondata from registrationparameter rp,registrationarno rarno,"
				+ " registrationtest rt,testmaster tm,testparameter tgtp"
				+ " LEFT OUTER JOIN testformula tgtf ON tgtp.ntestparametercode = tgtf.ntestparametercode"
				+ " and tgtf.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and tgtf.ndefaultstatus =" + Enumeration.TransactionStatus.YES.gettransactionstatus() + ""
				+ " where  rp.ntestparametercode = tgtp.ntestparametercode"
				+ " and rarno.npreregno=rp.npreregno and rp.npreregno in(" + npreregno + ") "
				+ " and rp.ntestparametercode IN(" + ntestparametercode + ")"
				+ " and rt.ntransactiontestcode=rp.ntransactiontestcode and tm.ntestcode=rt.ntestcode "
				+ " and rp.ntransactiontestcode = " + ntransactiontestcode + "  and rarno.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rarno.nsitecode ="
				+ userInfo.getNtranssitecode() + "" + " and rt.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nsitecode ="
				+ userInfo.getNtranssitecode() + "" + " and tm.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tm.nsitecode ="
				+ userInfo.getNmastersitecode() + "" + " and tgtp.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgtp.nsitecode ="
				+ userInfo.getNmastersitecode() + "" + " and rp.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rp.nsitecode ="
				+ userInfo.getNtranssitecode() + "";

		List<ResultParameter> lstResultParameter = jdbcTemplate.query(sQuery5, new ResultParameter());

		final String lstSdMSLab = "insert into sdmslabsheetdetails (ntransactionresultcode,npreregno,sarno,ntransactionsamplecode,ntransactiontestcode,"
				+ " ntestgrouptestcode,ntestcode,nretestno,ntestrepeatcount,ntestgrouptestparametercode,ntestparametercode,nparametertypecode,"
				+ " nroundingdigits,sresult,sllinterstatus,sfileid,nlinkcode,nattachedlink,suuid,nbatchmastercode,nsitecode,nstatus)"
				+ " select rp.ntransactionresultcode ntransactionresultcode,r.npreregno,ra.sarno,"
				+ " rs.ntransactionsamplecode ntransactionsamplecode,rt.ntransactiontestcode, rt.ntestgrouptestcode,rt.ntestcode,"
				+ " rt.ntestretestno nretestno, rt.ntestrepeatno ntestrepeatcount, rp.ntestgrouptestparametercode,rp.ntestparametercode,"
				+ " rp.nparametertypecode,(rp.jsondata->>'nroundingdigits')::int nroundingdigits, NULL sresult,'A' sllinterstatus,"
				+ " NULL sfileid,-1 nlinkcode,-1 nattachedlink,CONCAT(r.npreregno,'-',rs.ntransactionsamplecode,'-',rt.ntestcode,'-',rt.ntestrepeatno,'-',rt.ntestretestno,'-',r.nsitecode) suuid,-1 nbatchmastercode,r.nsitecode,"
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus from "
				+ "registration r,registrationarno ra, registrationsample rs, "
				+ " registrationtest rt, registrationparameter rp,testgrouptest tgt,testgrouptestparameter tgtp, "
				+ " testmaster tm, testparameter tp " + " where "
				+ " r.npreregno = ra.npreregno  and r.npreregno = rs.npreregno and r.npreregno = rt.npreregno"
				+ " and rs.ntransactionsamplecode = rt.ntransactionsamplecode  and r.npreregno = rt.npreregno"
				+ " and rt.ntransactiontestcode = rp.ntransactiontestcode  and r.npreregno = rp.npreregno"
				+ " and tgt.ntestgrouptestcode = rt.ntestgrouptestcode and tgt.ntestcode = rt.ntestcode"
				+ " and tgtp.ntestgrouptestparametercode = rp.ntestgrouptestparametercode and tm.ntestcode = tgt.ntestcode"
				+ " and tm.ntestcode = tp.ntestcode and tp.ntestparametercode = tgtp.ntestparametercode"
				+ " and r.nsitecode=ra.nsitecode and ra.nsitecode=rs.nsitecode and rs.nsitecode=rt.nsitecode"
				+ " and rt.nsitecode=rp.nsitecode and rp.nsitecode=" + userInfo.getNtranssitecode()
				+ " and r.nstatus = rs.nstatus and rs.nstatus = rt.nstatus and rt.nstatus =  rp.nstatus"
				+ " and rp.nstatus = tgt.nstatus and tgt.nstatus = tgtp.nstatus and tgtp.nstatus = tm.nstatus and tm.nstatus = tp.nstatus"
				+ " and tp.nstatus = ra.nstatus  " + " and r.npreregno in (" + npreregno + ") "
				+ " and tm.ninterfacetypecode > 0 and  tgtp.ntestgrouptestparametercode not in ("
				+ " select sds.ntestgrouptestparametercode from sdmslabsheetdetails  sds "
				+ "	where sds.ntestgrouptestparametercode=tgtp.ntestgrouptestparametercode and sds.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "	and sds.ntransactiontestcode=rt.ntransactiontestcode  )";

		jdbcTemplate.execute(lstSdMSLab);

		jsonAuditNewData.put("resultparameter", lstResultParameter);
		auditActionType.put("resultparameter", "IDS_ADHOCPARAMETER");
		Map<String, Object> objMap = new HashMap<>();
		objMap.put("nregtypecode", nregtypecode);
		objMap.put("nregsubtypecode", nregsubtypecode);
		objMap.put("ndesigntemplatemappingcode", ndesigntemplatemappingcode);
		auditUtilityFunction.fnJsonArrayAudit(jsonAuditNewData, null, auditActionType, objMap, false, userInfo);
		returnMap.putAll((Map<String, Object>) getTestbasedParameter(multiselecttransactiontestcode, userInfo)
				.getBody());
		return returnMap;
	}


	public ResponseEntity<Object> getAnalysedUser(final Map<String, Object> inputMap,final UserInfo objUserInfo) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		int regTypeCode = (int) inputMap.get("nregtypecode");
		int regSubTypeCode = (int) inputMap.get("nregsubtypecode");
		String nsectioncode = (String) inputMap.get("nsectioncode");
		String stransactiontestcode = (String) inputMap.get("transactiontestcode");
		int ncontrolCode =(int) inputMap.get("ncontrolcode");
		List<RegistrationTestHistory> filteredTestList = validateTestStatus(stransactiontestcode,ncontrolCode, objUserInfo,(int) inputMap.get("nneedReceivedInLab"));

		if(!filteredTestList.isEmpty()) {					
			stransactiontestcode = filteredTestList.stream()
					.map(object -> String.valueOf(object.getNtransactiontestcode())).collect(Collectors.joining(","));

			final int ncount = getChecklistValidation(stransactiontestcode, objUserInfo);

			if (ncount > 0) {
				final String resultparametervalidation = "select rp.ntransactionresultcode from  resultparameter rp "
						+ " where rp.ntransactiontestcode in (" + stransactiontestcode + ")" + " "
						+ " and rp.nsitecode = "+objUserInfo.getNtranssitecode()+" and rp.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				final String validateResult = "select ra.napprovalversioncode,rt.ntestgrouptestcode,rt.npreregno,rt.ntransactionsamplecode,rt.ntransactiontestcode,(rt.jsondata->'nsectioncode'->>'value')::integer "
						+ "from registrationtest rt, registrationarno ra, resultparameter rp "
						+ "where ra.npreregno = rt.npreregno and rp.ntransactiontestcode=rt.ntransactiontestcode "
						+ "and rt.ntransactiontestcode in (" + stransactiontestcode + ") "
						+ "and rt.ntransactiontestcode = any( select b.ntransactiontestcode from ("
						+ "select count(ntransactionresultcode) paramcount,ntransactiontestcode "
						+ " from resultparameter where nresultmandatory = "
						+ Enumeration.TransactionStatus.YES.gettransactionstatus()
						+ " and ntransactiontestcode = rt.ntransactiontestcode and nsitecode = "+objUserInfo.getNtranssitecode()+"  and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " group by ntransactiontestcode ) a,("
						+ " select count(ntransactionresultcode) paramcount,ntransactiontestcode "
						+ " from resultparameter where nresultmandatory = "
						+ Enumeration.TransactionStatus.YES.gettransactionstatus()
						+ " and ntransactiontestcode = rt.ntransactiontestcode and (jsondata->>'sresult' is not NULL and jsondata->>'sresult' <> '' ) "
						+ " and nsitecode = "+objUserInfo.getNtranssitecode()+" and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " group by ntransactiontestcode ) b "
						+ " where a.ntransactiontestcode = b.ntransactiontestcode "
						+ " and a.paramcount=b.paramcount ) "
						+ " and rp.nsitecode =  "+objUserInfo.getNtranssitecode()+" "
						+ " and rt.nsitecode =  "+objUserInfo.getNtranssitecode()+" "
						+ " and ra.nsitecode = "+objUserInfo.getNtranssitecode()+" "
						+ " and rp.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and rt.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and ra.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " group by ra.napprovalversioncode,rt.ntestgrouptestcode,rt.npreregno,rt.ntransactionsamplecode,rt.ntransactiontestcode,rt.jsondata->'nsectioncode'->>'value' "
						+ " union all "
						+ " select ra.napprovalversioncode,rt.ntestgrouptestcode,rt.npreregno,rt.ntransactionsamplecode,rt.ntransactiontestcode,(rt.jsondata->'nsectioncode'->>'value')::integer "
						+ " from registrationtest rt, registrationarno ra, resultparameter rp "
						+ " where ra.npreregno = rt.npreregno and rp.ntransactiontestcode=rt.ntransactiontestcode "
						+ " and rt.ntransactiontestcode in (" + stransactiontestcode + ") "
						+ " and rt.ntransactiontestcode = any( select b.ntransactiontestcode from ("
						+ " select count(ntransactionresultcode) paramcount,ntransactiontestcode "
						+ " from resultparameter where ntransactiontestcode = rt.ntransactiontestcode "
						+ " and nsitecode = "+objUserInfo.getNtranssitecode()+" and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " group by ntransactiontestcode ) a,("
						+ " select count(ntransactionresultcode) paramcount,ntransactiontestcode "
						+ " from resultparameter where nresultmandatory = "
						+ Enumeration.TransactionStatus.NO.gettransactionstatus()
						+ " and ntransactiontestcode = rt.ntransactiontestcode and nsitecode = "+objUserInfo.getNtranssitecode()+" and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " group by ntransactiontestcode ) b "
						+ " where a.ntransactiontestcode = b.ntransactiontestcode " + "and a.paramcount=b.paramcount )"
						+ " and rp.nsitecode =  "+objUserInfo.getNtranssitecode()+" "
						+ " and rt.nsitecode =  "+objUserInfo.getNtranssitecode()+" "
						+ " and ra.nsitecode = "+objUserInfo.getNtranssitecode()+" "
						+ " and rp.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and rt.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and ra.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " group by ra.napprovalversioncode,rt.ntestgrouptestcode,rt.npreregno,rt.ntransactionsamplecode,rt.ntransactiontestcode,rt.jsondata->'nsectioncode'->>'value' ;";

				List<ResultParameter> lstResultParameterValidation = jdbcTemplate.query(resultparametervalidation,	new ResultParameter());
				List<RegistrationTest> lstRestultValidation = jdbcTemplate.query(validateResult,new RegistrationTest());

				if(lstResultParameterValidation.isEmpty()){
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NOPARAMETERFOUND",objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}else if(!lstRestultValidation.isEmpty()) {	

					String[] sectionsplit = nsectioncode.split(",");
					int sectionsize = sectionsplit.length;
					System.out.println(sectionsize);

					// ALPD-5596 - changed approvalconfigversion by gowtham r - From approved version to current flow version.
					String strQuery = "select nusercode,susername from "
							+ "(select su.nusercode,count(su.nusercode)as ncount,u.sfirstname ||' '|| u.slastname as susername from sectionusers su "
							+ " join users u on u.nusercode =su.nusercode "
							+ " join userssite us on u.nusercode=us.nusercode "
							+ " join usermultirole urm on urm.nusersitecode =us.nusersitecode "
							+ " join userrole ur on ur.nuserrolecode =urm.nuserrolecode "
							+ " where  ur.nuserrolecode =(select  nuserrolecode from treetemplatetransactionrole ttr "
							+ " join approvalconfig  ac on ac.napprovalconfigcode =ttr.napprovalconfigcode  "
							+ " join approvalconfigversion acv on ac.napprovalconfigcode=acv.napprovalconfigcode  "
							+ " and acv.ntreeversiontempcode = ttr.ntreeversiontempcode "
							+ " where ac.nregtypecode=" + regTypeCode + " and ac.nregsubtypecode=" + regSubTypeCode
							//									+ " and ttr.ntransactionstatus="+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " "
							+ " and ttr.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and ac.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and acv.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and acv.napproveconfversioncode="+ inputMap.get("napprovalconfigversioncode") 
							+ " order by ttr.nlevelno desc LIMIT 1) "
							+ " and su.nlabsectioncode in(select ls.nlabsectioncode from labsection ls,sitedepartment sd,departmentlab dl "
							+ "	where ls.nsectioncode in("+nsectioncode+") and sd.nsitecode=su.nsitecode "
							+ "	and dl.ndeptlabcode=ls.ndeptlabcode and dl.nlabcode in ("+objUserInfo.getNdeptcode()+") "
							+ "	and sd.nsitedeptcode=  dl.nsitedeptcode "
							+ "	and sd.ndeptcode in("+objUserInfo.getNdeptcode()+") and ls.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and sd.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and dl.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+") " 
							+ " and su.nsitecode = us.nsitecode"
							+ " and su.nsitecode ="+objUserInfo.getNtranssitecode()+" and u.ntransactionstatus <> "+Enumeration.TransactionStatus.RETIRED.gettransactionstatus()+" "
							+ " and su.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " 
							+ " and u.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " 
							+ " and us.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ " and urm.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " 
							+ " and ur.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ " group by su.nusercode ,susername)a where a.ncount="+sectionsize+"";				

					final List<Users> lstUsers = jdbcTemplate.query(strQuery, new Users());				
					map.put("Users", lstUsers);						   

					if(lstUsers.size()>0) {
						int userCode = objUserInfo.getNusercode();
						String userName = objUserInfo.getSusername();

						int matchedUserCode = lstUsers.stream()
								.mapToInt(Users::getNusercode) 
								.filter(code -> code == userCode) 
								.findFirst() 
								.orElse(-1);			    

						Optional <String>  matchedUsername = lstUsers.stream()
								.map(Users::getSusername) 
								.filter(name -> name.equals(userName)) 
								.findFirst(); 
						//Added by sonia on 19-06-2024 for JIRA ID:ALPD-4422
						if(matchedUserCode !=-1) {
							map.put("userCode",matchedUserCode);
						}

						if(matchedUsername.isPresent()){
							map.put("userName",matchedUsername.get());		
						}
					}else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_LOGGEDINLABMISMATCHEDWITHORGANISATIONLAB",objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);

					}




					return new ResponseEntity<>(map, HttpStatus.OK);
				}else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ENTERTHERESULTTOCOMPLETE",objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}						
			}else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ENTERCHECKLIST", objUserInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}					
		}else {
			final String getValidationStatus = "select ts.jsondata->'stransdisplaystatus'->>'"
					+ objUserInfo.getSlanguagetypecode() + "' as stransdisplaystatus "
					+ " from transactionvalidation rvd,transactionstatus ts"
					+ " where ts.ntranscode = rvd.ntransactionstatus and rvd.ncontrolcode = "
					+ inputMap.get("ncontrolcode") + " and rvd.nformcode = " + objUserInfo.getNformcode()
					+ " and rvd.nsitecode = " + objUserInfo.getNmastersitecode() + " and ts.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rvd.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rvd.nregtypecode="
					+ regTypeCode + " and rvd.nregsubtypecode=" + regSubTypeCode;

			List<TransactionStatus> validationStatusList = jdbcTemplate.query(getValidationStatus,	new TransactionStatus());

			final String alertStatus = validationStatusList.stream().map(TransactionStatus::getStransdisplaystatus).collect(Collectors.joining("/"));

			String alertMessage = commonFunction.getMultilingualMessage("IDS_SELECT", objUserInfo.getSlanguagefilename())+ " " + alertStatus + " "
					+ commonFunction.getMultilingualMessage("IDS_TESTONLY", objUserInfo.getSlanguagefilename());

			return new ResponseEntity<>(alertMessage, HttpStatus.EXPECTATION_FAILED);
		}
	}


	public List<RegistrationSubType> getRegistrationSubTypeValues(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> returnMap = new HashMap<>();
		List<ReactRegistrationTemplate> listReactRegistrationTemplate = new ArrayList<ReactRegistrationTemplate>();

		final String getRegSubType = "select cast(rsc.jsondata->>'nneedjoballocation' as boolean) nneedjoballocation ,cast(rsc.jsondata->>'nneedmyjob' as boolean) nneedmyjob "
				+ " from approvalconfig ac,approvalconfigversion acv,regsubtypeconfigversion rsc"
				+ " where "
				+ "  rsc.napprovalconfigcode = ac.napprovalconfigcode "
				+ " and ac.napprovalconfigcode = acv.napprovalconfigcode "
				+ " and ac.napprovalconfigcode = acv.napprovalconfigcode "
				+ " and rsc.nsitecode = "+ userInfo.getNmastersitecode() + " "
				+ " and acv.nsitecode = "	+ userInfo.getNmastersitecode() + ""
				+ " and ac.nsitecode = "+ userInfo.getNmastersitecode()+" "
				+ " and rsc.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and acv.nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and ac.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "

						+ " and rsc.ntransactionstatus = "+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
						+ " and ac.nregtypecode = "+ inputMap.get("nregtypecode")+"  "
						+ " and acv.napproveconfversioncode = "+inputMap.get("napprovalversioncode")+" "
						+ " and ac.nregsubtypecode="+ inputMap.get("nregsubtypecode")
						+ " and acv.ndesigntemplatemappingcode="+inputMap.get("ndesigntemplatemappingcode")+";";

		List<RegistrationSubType> regSubTypeList = jdbcTemplate.query(getRegSubType, new RegistrationSubType());
		return regSubTypeList;
	}

	//Added by sonia on 05th Jan 2025 for jira id:ALPD-5174				
	@Override
	public ResponseEntity<Object> getExportData(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception {
		final Map<String,Object> map = new HashMap<>();
		final Map<String,Object> rtnObj = new HashMap<>();	
		final int nusercode = (int)userInfo.getNusercode();				
		final String npreregno=(String) inputMap.get("npreregno");	
		final int ncontrolcode= (int) inputMap.get("ncontrolcode");
		final int nregtypecode = (int) inputMap.get("nregtypecode");
		final int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
		final int ntestcode = (int) inputMap.get("ntestcode");
		final int ntransactionstatus  = (int) inputMap.get("ntransactionstatus");

		String sTransstatus ="";
		if(ntransactionstatus >0) {
			sTransstatus ="and ntransactionstatus ="+ntransactionstatus+"";
		}

		final String sTransactionTestQuery = " select ntransactiontestcode from registrationtesthistory where ntesthistorycode =any "
				+ " (select max(ntesthistorycode) from registrationtesthistory "
				+ " where npreregno in ("+npreregno+")  and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " group by ntransactiontestcode) "+sTransstatus+" and nsitecode ="+userInfo.getNtranssitecode()+" ";

		final List<RegistrationTestHistory> lst = jdbcTemplate.query(sTransactionTestQuery, new RegistrationTestHistory());		

		final String ntransactiontestcode = lst.stream()
				.map(object -> String.valueOf(object.getNtransactiontestcode())).collect(Collectors.joining(","));;

				final List<RegistrationTestHistory> filteredTestList = validateExportTestStatus(npreregno,ntransactiontestcode,ntestcode,ncontrolcode, userInfo);
				if(!filteredTestList.isEmpty()){

					final String stransactiontestcode = filteredTestList.stream()
							.map(object -> String.valueOf(object.getNtransactiontestcode())).collect(Collectors.joining(","));


					final String subsampleQuery = " select rst.nregsubtypecode,rst.nregtypecode, "
							+ " coalesce (rst.jsondata->'sregsubtypename'->>'"+userInfo.getSlanguagetypecode()+"',rst.jsondata->'sregsubtypename'->>'en-US') as sregsubtypename, "
							+ " cast(rsc.jsondata->>'nneedsubsample' as boolean) nneedsubsample "
							+ " from approvalconfig ac,approvalconfigversion acv,registrationsubtype rst,regsubtypeconfigversion rsc  "
							+ " where  "
							+ " rsc.napprovalconfigcode = ac.napprovalconfigcode and ac.napprovalconfigcode = acv.napprovalconfigcode "
							+ " and rst.nregsubtypecode = ac.nregsubtypecode "
							+ " and acv.ntransactionstatus = "+Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+" and rsc.ntransactionstatus = "+Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+" "
							+ " and rst.nregtypecode = "+nregtypecode+" and rst.nregsubtypecode="+nregsubtypecode+" "
							+ " and (rsc.jsondata->'nneedsubsample')::jsonb ='true'  "
							+ " and acv.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
							+ " and rst.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
							+ " and ac.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
							+ " and rsc.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
							+ " and ac.nsitecode ="+userInfo.getNmastersitecode()+" "
							+ " and acv.nsitecode ="+userInfo.getNmastersitecode()+" "
							+ " and rst.nsitecode ="+userInfo.getNmastersitecode()+" "
							+ " and rsc.nsitecode ="+userInfo.getNmastersitecode()+" "
							+ " and rst.nregsubtypecode > 0  order by rst.nregsubtypecode desc";

					final List<RegistrationSubType> lstRegistrationSubType = jdbcTemplate.query(subsampleQuery,new RegistrationSubType());

					final String query = " select  "
							+ " rt.npreregno as npreregno,  "
							+ " rt.ntransactionsamplecode as ntransactionsamplecode, "
							+ " rt.ntransactiontestcode as ntransactiontestcode,  "
							+ " rp.ntransactionresultcode as ntransactionresultcode, "
							+ " pt.nparametertypecode,  "
							+ " ra.sarno as sarno,  "
							+ " rsa.ssamplearno as  ssamplearno,  "
							+ " s.ssectionname as ssectionname,  "
							+ " tgt.stestsynonym || '[' || cast(rt.ntestrepeatno as character varying)|| '][' || cast(rt.ntestretestno as character varying)|| ']' as stestsynonym ,  "
							+ " rp.jsondata->>'sresult' as sresult,  "
							+ " tgtp.sparametersynonym as sparametersynonym,  "
							+ " rp.jsondata->>'smina' as smina,  "
							+ " rp.jsondata->>'sminb' as sminb,  "
							+ " rp.jsondata->>'smaxa' as smaxa,  "
							+ " rp.jsondata->>'smaxb' as smaxb,  "
							+ " pt.sparametertypename as sparametertypename  "
							+ " from  "
							+ " registrationarno ra, "
							+ " registrationsamplearno rsa, "
							+ " registrationtest rt, "
							+ " registrationtesthistory rth, "
							+ " resultparameter rp, "
							+ " testgrouptest tgt, "
							+ " testgrouptestparameter tgtp, "
							+ " parametertype pt, "
							+ " section s, "
							+ " joballocation jb "
							+ " where "
							+ " ra.npreregno=rsa.npreregno and "
							+ " rt.npreregno=ra.npreregno and "
							+ " rt.ntransactionsamplecode =rsa.ntransactionsamplecode and "
							+ " rt.ntransactiontestcode =rp.ntransactiontestcode and "
							+ " rt.npreregno=rp.npreregno and "
							+ " jb.ntransactiontestcode =rt.ntransactiontestcode and "
							+ " rth.ntesthistorycode =any(select max(ntesthistorycode) as ntesthistorycode "
							+ " from registrationtesthistory where nsitecode = "+userInfo.getNtranssitecode()+" "
							+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and npreregno in("+npreregno+")"
							+ " and  ntransactiontestcode in("+ntransactiontestcode+")  "
							+ " group by ntransactiontestcode) "
							+ "	and rth.ntransactiontestcode =rt.ntransactiontestcode "
							+ "	and rth.ntransactionsamplecode =rt.ntransactionsamplecode "
							+ "	and rth.npreregno=rt.npreregno "
							+ "	and rt.ntestgrouptestcode = tgt.ntestgrouptestcode "
							+ "	and rt.ntestcode =tgt.ntestcode "
							+ "	and rp.ntestgrouptestparametercode=tgtp.ntestgrouptestparametercode "
							+ "	and rp.nparametertypecode = pt.nparametertypecode "
							+ " and pt.nparametertypecode in("+Enumeration.ParameterType.NUMERIC.getparametertype()+","+Enumeration.ParameterType.PREDEFINED.getparametertype()+","+Enumeration.ParameterType.CHARACTER.getparametertype()+")"
							+ "	and rt.nsectioncode =s.nsectioncode "
							+ " and jb.nsectioncode in (select ls.nsectioncode from  labsection ls,sectionusers su "
							+ "							where ls.nlabsectioncode = su.nlabsectioncode "
							+ "							and  su.nusercode="+userInfo.getNusercode()+" and su.nsitecode = "+userInfo.getNtranssitecode()+" "
							+ "							and ls.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
							+ "  and su.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" group by ls.nsectioncode) "

						   + "  and ra.nsitecode = "+userInfo.getNtranssitecode()+""
						   + "  and rsa.nsitecode = "+userInfo.getNtranssitecode()+""
						   + "  and rt.nsitecode = "+userInfo.getNtranssitecode()+""
						   + "  and rth.nsitecode = "+userInfo.getNtranssitecode()+""
						   + "  and jb.nsitecode = "+userInfo.getNtranssitecode()+""
						   + "  and tgt.nsitecode = "+userInfo.getNmastersitecode()+""
						   + "  and tgtp.nsitecode = "+userInfo.getNmastersitecode()+""
						   + "  and pt.nsitecode = "+userInfo.getNmastersitecode()+""
						   + "  and s.nsitecode = "+userInfo.getNmastersitecode()+""
						   + "  and ra.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
						   + "  and rsa.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
						   + "  and rt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
						   + "  and rth.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
						   + "  and rp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
						   + "  and tgt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
						   + "  and tgtp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
						   + "  and pt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
						   + "  and s.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
						   + "  and jb.nusercode in("+userInfo.getNusercode()+",-1) "	
						   + "  and rp.ntransactionstatus not in ("+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus()+", "+Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ")"
						   + "	and rth.ntransactiontestcode in("+stransactiontestcode+");";

					final List<Map<String, Object>> lstObj = jdbcTemplate.queryForList(query);	

					final List<Map<String, Object>> headercolumn = getexportheadercolumn(inputMap);		
					final String sFileName = "Resultentryflow";								
					if(!lstObj.isEmpty()){
						//Create blank workbook
						XSSFWorkbook workbook = new XSSFWorkbook(); 
						//Create a blank sheet
						XSSFSheet spreadsheet = workbook.createSheet("ResultEntry");
						//Create row object
						XSSFRow headerRow = spreadsheet.createRow(0);
						XSSFFont font = workbook.createFont();
						font.setBold(true);

						XSSFCellStyle style = workbook.createCellStyle();
						XSSFCellStyle lockedCellStyle = workbook.createCellStyle();
						lockedCellStyle.setLocked(true);
						lockedCellStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
						//lockedCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);	
						lockedCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);


						XSSFCellStyle unlockedStyle = workbook.createCellStyle();
						unlockedStyle.setLocked(false);

						XSSFCellStyle greyStyle = workbook.createCellStyle();
						greyStyle.setFont(font);
						greyStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
						//greyStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
						greyStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

						for (int i = 0; i < headercolumn.size(); i++) {
							final String headerValue = (String) headercolumn.get(i).get("sexpheadername");
							Cell cell = headerRow.createCell(i);
							cell.setCellStyle(greyStyle);
							cell.setCellValue(commonFunction.getMultilingualMessage(headerValue, userInfo.getSlanguagefilename()));
							style.setBorderTop(BorderStyle.THIN);							
						}

						int rowIndex = 1;
						for (int j=0; j<lstObj.size(); j++) {
							XSSFRow row = spreadsheet.createRow(rowIndex++);
							for(int k=0;k < headercolumn.size(); k++) {
								Cell cell = row.createCell(k);
								final String sexpheadername = (String) headercolumn.get(k).get("sexpheadername");
								final String sexpcolumnname = (String) headercolumn.get(k).get("sexpcolumnname");
								final int nisvisible = (int) headercolumn.get(k).get("nisvisible");
								final int niseditable = (int) headercolumn.get(k).get("niseditable");

								final Object cellnisvisibleObj = lstObj.get(j).get(sexpcolumnname);
								final String cellValues = cellnisvisibleObj != null ? cellnisvisibleObj.toString() : " ";					

								if (nisvisible == Enumeration.ExportCell.VISIBLEHIDE.getExportCell()) {						
									cell.setCellStyle(lockedCellStyle);						
									spreadsheet.setColumnHidden(k, true);
								}

								if (niseditable == Enumeration.ExportCell.EDIT.getExportCell()) {						
									cell.setCellStyle(unlockedStyle);

								} else {						
									cell.setCellStyle(lockedCellStyle);
								}
								cell.setCellValue(cellValues);
								spreadsheet.autoSizeColumn(k);
							}
						}
						spreadsheet.protectSheet("password");			

						final String OutputFileName = sFileName+"_"+UUID.randomUUID().toString()+".xls";			        
						LOGGER.info("Output Export File name:"+OutputFileName);

						//ALPD-4436 
						//To get path value from system's environment variables instead of absolutepath
						final String homePath=ftpUtilityFunction.getFileAbsolutePath();
						final String targetPath = System.getenv(homePath)//new File("").getAbsolutePath() 
								+ Enumeration.FTP.UPLOAD_PATH.getFTP()+OutputFileName;

						LOGGER.info(" Output Export File path:"+targetPath);

						//Write the workbook in file system
						FileOutputStream out = new FileOutputStream(new File(targetPath));
						workbook.write(out);
						out.close();
						LOGGER.info("Writesheet.xlsx written successfully");				    

						//Added by sonia on 11-06-2024 for JIRA ID:4084 
						final String sDownloadPathName =getExportDownloadPathName();			
						if(sDownloadPathName !=null){
							//Modified by sonia on 11-06-2024 using camelcase format in the key value
							rtnObj.put("exportFileViewUrl",sDownloadPathName+OutputFileName);
							rtnObj.put("rtn", Enumeration.ReturnStatus.SUCCESS.getreturnstatus());				   
						}else {
							rtnObj.put("rtn",Enumeration.ReturnStatus.FAILED.getreturnstatus().trim().toString());	
						}	    
					}else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NOAVAILABLEEXPORT", userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
					}
				}else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTVALIDTEST", userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
				}					
				return new ResponseEntity<Object>(rtnObj, HttpStatus.OK);
	}			



	//Added by sonia for ALPD-4084 on May 2 2024 Export action
	private List<Map<String, Object>> getexportheadercolumn(Map<String, Object> objmap) throws Exception {
		final String query="select * from exportconfiguration where nformcode="+Enumeration.QualisForms.RESULTENTRY.getqualisforms() +" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" order by nsortorder ";
		return jdbcTemplate.queryForList(query);
	}

	//Added by sonia for ALPD-4084 on May 2 2024 Export action
	public String getExportDownloadPathName() throws Exception {
		final String downloadPath="select * from settings where nsettingcode="+Enumeration.Settings.EXPORTVIEW_URL.getNsettingcode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";    
		final Settings surl = (Settings) jdbcUtilityFunction.queryForObject(downloadPath, Settings.class,jdbcTemplate);
		return surl.getSsettingvalue();
	}

	//Added by sonia for ALPD-4084 on May 2 2024 Import action
	public 	ResponseEntity<Object> getImportResultEntry(MultipartHttpServletRequest request, UserInfo userInfo) throws Exception {

		Map<String, Object> rtnUploadFileObj = new HashMap<>();
		final Map<String, Object> map1 = new HashMap<String, Object>();
		Map<String, Object> obj1 = new HashMap<>();
		final Map<String,Object>inputmap =null;	
		final List<String> column = new ArrayList<>();
		final List<Integer> sortindx = new ArrayList<>();	
		final List<Map<String, Object>> lstdata = new ArrayList<>();	
		//final boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		final MultipartFile multiFile = request.getFile("uploadedFile");
		final List<Map<String, Object>> field = getexportheadercolumn(inputmap);


		for (int i = 0; i < field.size(); i++) {
			obj1 = field.get(i);
			column.add((String) obj1.get("sexpcolumnname"));
			sortindx.add((Integer) obj1.get("nsortorder"));
		}
		// process only if it is multipart content
		//if (isMultipart) {
			final byte[] bytes = multiFile.getBytes();
			InputStream thisFileInputStream = null;
			thisFileInputStream = new ByteArrayInputStream(bytes);

			// Creating a Workbook from an Excel file (.xls or .xlsx)
			Workbook workbook = WorkbookFactory.create(thisFileInputStream);			

			// Getting the Sheet at index zero
			//Added  by sonia on 08th  Jan 2025 for jira id:ALPD-5174
			int rowIndex = 0;
			Sheet sheet = workbook.getSheetAt(0);
			// Create a DataFormatter to format and get each cell's value as String
			DataFormatter dataFormatter = new DataFormatter();
			final List<Map<String, String>> exportValues = new ArrayList<>();
			for (Row row : sheet) {
				final Map<String, String> map = new HashMap<>();
				final int idx = row.getRowNum();
				if (rowIndex > 0) {
					for (int i = 0; i < sortindx.size(); i++) {					
						final Cell cell = row.getCell(i);
						final String cellValue = dataFormatter.formatCellValue(cell);
						final String headervalue=column.get(i);
						map.put(headervalue, cellValue.trim());
					}
					exportValues.add(map);	
				}
				rowIndex++;
			}
			// close file stream
			thisFileInputStream.close();

			// Added by Gowtham - ALPD-5630 - Result entry screen -> 500 error is occurring, Import any other file in Excel file format.
			if((exportValues.size() > 0 && exportValues.get(0).containsKey("npreregno") && exportValues.get(0).get("npreregno") == ""
					&& exportValues.get(0).containsKey("ntransactionsamplecode") && exportValues.get(0).get("ntransactionsamplecode") == ""
					&& exportValues.get(0).containsKey("ntransactiontestcode") && exportValues.get(0).get("ntransactiontestcode") == ""
					&& exportValues.get(0).containsKey("ntransactionresultcode") && exportValues.get(0).get("ntransactionresultcode") == "") 
					|| exportValues.isEmpty()) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_COMPONENTNEEDTOBEEXPORT",
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				map1.put("nregtypecode",request.getParameter("nregtypecode"));
				map1.put("nregsubtypecode",request.getParameter("nregsubtypecode"));
				map1.put("ndesigntemplatemappingcode",request.getParameter("ndesigntemplatemappingcode"));
				map1.put("ncontrolcode",request.getParameter("ncontrolcode"));
				map1.put("nneedReceivedInLab",request.getParameter("nneedReceivedInLab"));
				lstdata.add(map1);
				rtnUploadFileObj = importresultentry(lstdata,exportValues,userInfo);
			}
			
		//}
		return new ResponseEntity<Object>(rtnUploadFileObj, HttpStatus.OK);
	}


	//Added by sonia for ALPD-4084 on May 2 2024 Import action
	private Map<String, Object> importresultentry(List<Map<String, Object>> lstdata,List<Map<String, String>> exportValues,UserInfo userInfo) throws Exception {
		Map<String,Object> map=new HashMap<>();
		String str="";
		final JSONObject jsonAuditOldData = new JSONObject();
		final JSONObject jsonAuditNewData = new JSONObject();
		final JSONObject auditActionType = new JSONObject();
		final Integer ncontrolcode=Integer.valueOf(lstdata.get(0).get("ncontrolcode").toString());
		final Integer nneedReceivedInLab=Integer.valueOf(lstdata.get(0).get("nneedReceivedInLab").toString());
		final Integer nregtypecode=Integer.valueOf(lstdata.get(0).get("nregtypecode").toString());
		final Integer nregsubtypecode =Integer.valueOf(lstdata.get(0).get("nregsubtypecode").toString());
		final Integer ndesigntemplatemappingcode=Integer.valueOf(lstdata.get(0).get("ndesigntemplatemappingcode").toString());
		ObjectMapper objMapper = new ObjectMapper();

		final int len=exportValues.size()-1;
		//outer:
		for (int i=0; i<=len; i++) {		
			final StringBuffer sb=new StringBuffer();  	  
			final String npreregno =(String) exportValues.get(i).get("npreregno");
			final String ntransactionsamplecode =(String) exportValues.get(i).get("ntransactionsamplecode");
			final String ntransactiontestcode =(String) exportValues.get(i).get("ntransactiontestcode");
			final String ntransactionresultcode =(String) exportValues.get(i).get("ntransactionresultcode");	  
			String sresult =(String) exportValues.get(i).get("sresult");


			final List<RegistrationTestHistory> lstvalidationtest = validateTestStatus(ntransactiontestcode,ncontrolcode,userInfo,nneedReceivedInLab);

			//ALPD-5483 Result entry-->While doing import with default result 500 error occurs
			if(!lstvalidationtest.isEmpty()) {
				String query = " select rp.*,"
						+ "rp.jsondata->>'smaxa'  as smaxa,"
						+ "rp.jsondata->>'smaxb'  as smaxb,"
						+ "rp.jsondata->>'smina'  as smina,"
						+ "rp.jsondata->>'sminb'  as sminb,"
						+ "rp.jsondata->>'nroundingdigits' as nroundingdigits,"
						+ "rp.jsondata->>'sfinal' sfinal , rp.jsondata->>'sresult' sresult,"
						+ " rp.jsondata->>'nresultaccuracycode' nresultaccuracycode ,rp.jsondata->>'sresultaccuracyname' sresultaccuracyname ," 
						//+ " rp.jsondata->>'dentereddate' dentereddate, "
						+"(rp.jsondata->>'dentereddate')::timestamp as dentereddate,"
						+ " rp.jsondata->>'dentereddatetimezonecode' dentereddatetimezonecode,"
						+ " rp.jsondata->>'noffsetdentereddate' noffsetdentereddate,"
						+ " t.jsondata->'stransdisplaystatus'->>'"
						+ userInfo.getSlanguagetypecode() + "' as stransdisplaystatus,"
						+ " rp.jsondata->>'stestsynonym' stestsynonym,"
						+ " rp.jsondata->>'sparametersynonym' sparametersynonym,"
						+ " r.sarno,rs.ssamplearno,g.sgradename,u.sunitname,rt.ntestgrouptestcode "
						+ " from resultparameter rp,transactionstatus t,registrationarno r,registrationsamplearno rs,"
						+ " registrationtest rt,grade g,unit u"
						+ " where t.ntranscode=rp.ntransactionstatus and r.npreregno=rp.npreregno and r.npreregno=rs.npreregno"
						+ " and rs.npreregno=rp.npreregno and rt.ntransactionsamplecode=rs.ntransactionsamplecode"
						+ " and rt.ntransactiontestcode=rp.ntransactiontestcode and u.nunitcode=rp.nunitcode "
						+ " and rt.npreregno=rs.npreregno and r.npreregno=rt.npreregno and g.ngradecode=rp.ngradecode "
						+ " and rp.nsitecode = "+userInfo.getNtranssitecode()+" "
						+ " and r.nsitecode = "+userInfo.getNtranssitecode()+""
						+ " and rs.nsitecode = "+userInfo.getNtranssitecode()+""
						+ " and rt.nsitecode = "+userInfo.getNtranssitecode()+""
						+ " and g.nsitecode = "+userInfo.getNmastersitecode()+""
						+ " and u.nsitecode = "+userInfo.getNmastersitecode()+""

						+ " and rp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						+ " and r.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
						+ " and rs.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
						+ " and rt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
						+ " and g.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
						+ " and u.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
						+ " and rp.ntransactionresultcode in (" + ntransactionresultcode + ")";
				final List<ResultParameter> lstSampleResults = jdbcTemplate.query(query, new ResultParameter());	    

				final String query1=" select nsequenceno from seqnoregistration where stablename='resultchangehistory' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				int nresultchangehistorycode=jdbcTemplate.queryForObject(query1, Integer.class);

				final int testgrouptestcode = lstSampleResults.get(0).getNtestgrouptestcode();
				final String ntestgrouptestcode = String.valueOf(testgrouptestcode);  
				final String smina=lstSampleResults.get(0).getSmina();
				final String sminb=lstSampleResults.get(0).getSminb();
				final String smaxa=lstSampleResults.get(0).getSmaxa();
				final String smaxb=lstSampleResults.get(0).getSmaxb();
				final String rst=  lstSampleResults.get(0).getSresult();
				final int rndgt=lstSampleResults.get(0).getNroundingdigits();
				int nGradeCode=Enumeration.Grade.NA.getGrade();
				double roundval=0;
				String finalvalue="";
				int nforcestatus=lstSampleResults.get(0).getNenforcestatus();
				int nParameterTypeCode=lstSampleResults.get(0).getNparametertypecode();

				//ALPD-5483--Added by Vignesh R(08-04-2025)-->While doing import with default result 500 error occurs
				final String sdbresult=lstSampleResults.get(0).getSresult();

				if(exportValues.get(i).get("nparametertypecode").equals("1")){


					if(sresult!=null) {

						//ALPD-5483--Added by Vignesh R(08-04-2025)-->While doing import with default result 500 error occurs
						if(sdbresult==null ? !sresult.isEmpty()? true:false : true){
							final String  ncolumn = exportValues.get(i).get("sresult").toString();

							//ALPD-5483--Added by Vignesh R(08-04-2025)-->While doing import with default result 500 error occurs
							if (isInteger(ncolumn)&& !(nParameterTypeCode==Enumeration.ParameterType.CHARACTER.getparametertype())) {  
								double resultvalue=Double.parseDouble(sresult);	  		
								roundval=round(resultvalue,rndgt);
								finalvalue=Double.toString(roundval);
								nGradeCode=numericGrade(npreregno,ntestgrouptestcode,finalvalue,userInfo);
							} else {
								finalvalue =sresult;

								//ALPD-5483--Added by Vignesh R(08-04-2025)-->While doing import with default result 500 error occurs
								if(sdbresult!=null  ? (!(!sdbresult.isEmpty() && sresult.isEmpty())) ? (sdbresult.isEmpty() && sresult.isEmpty()) ?false :true :false:true){		 
									nGradeCode=Enumeration.Grade.FIO.getGrade();
									nforcestatus=Enumeration.TransactionStatus.YES.gettransactionstatus();
									nParameterTypeCode=Enumeration.ParameterType.CHARACTER.getparametertype();

								}

							}	 				   
						}
					}
				}else if(exportValues.get(i).get("nparametertypecode").equals("2")){
					boolean check=true;
					finalvalue =sresult;
					nGradeCode=Enumeration.Grade.PASS.getGrade();
					String query2=" select t.spredefinedname||' ('||t.spredefinedsynonym||')' as sresultpredefinedname, "
							+" t.ngradecode,t.ntestgrouptestparametercode,t.ntestgrouptestpredefcode "
							+" from testgrouptestpredefparameter t,resultparameter r "
							+ " where r.ntestgrouptestparametercode=t.ntestgrouptestparametercode "
							+ " and t.nsitecode  = "+userInfo.getNmastersitecode()+" and r.nsitecode  = "+userInfo.getNtranssitecode()+" "
							+ " and t.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
							+ " and r.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
							+ " and r.ntransactionresultcode="+ntransactionresultcode;

					List<TestGroupTestPredefinedParameter> newlist= (List<TestGroupTestPredefinedParameter>)jdbcTemplate.query(query2,new TestGroupTestPredefinedParameter());
					if(newlist.size()>0 && newlist!=null) {
						int k=0;
						outer:	
							for(k=0;k<newlist.size();k++) {
								String predefined=newlist.get(k).getSresultpredefinedname().toUpperCase();
								//Added by sonia on 11-06-2024 for JIRA ID:4319
								if(sresult != null) {
									if(predefined.equals(sresult.toUpperCase())) {
										nGradeCode=newlist.get(k).getNgradecode();
										sresult=newlist.get(k).getSresultpredefinedname();
										finalvalue=sresult;
										check=false;
										break outer;
									}
								}										
							}
						if(check) {
							nGradeCode=Enumeration.Grade.FIO.getGrade();
							nforcestatus=Enumeration.TransactionStatus.YES.gettransactionstatus();
							nParameterTypeCode=Enumeration.ParameterType.CHARACTER.getparametertype();
						}
					}		
				}else {
					finalvalue =sresult;
					nGradeCode=Enumeration.Grade.FIO.getGrade();  //pass
				}		
				if(sresult!=null ) {
					if(sdbresult==null ? !sresult.isEmpty()?true:false : true) {
						final Map<String, Object> jsonData = lstSampleResults.get(0).getJsondata();
						final String currentDateTime =dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ").replace("Z","");								
						jsonData.put("sfinal", finalvalue);
						jsonData.put("sresult", sresult);
						jsonData.put("dentereddate", currentDateTime);
						jsonData.put("dentereddatetimezonecode", userInfo.getNtimezonecode());
						jsonData.put("noffsetdentereddate",dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()));

						sb.append("update resultparameter set  ngradecode=" +nGradeCode+ ",nenforcestatus="+nforcestatus+" ,nparametertypecode="+nParameterTypeCode+","
								+ " nenteredby= "+lstSampleResults.get(0).getNenteredby()+ ", nenteredrole="+lstSampleResults.get(0).getNenteredrole()+ ","
								+ " ndeputyenteredby="+lstSampleResults.get(0).getNdeputyenteredby()+", ndeputyenteredrole="+lstSampleResults.get(0).getNdeputyenteredrole()+ ","
								+ " jsondata = jsondata || '" + stringUtilityFunction.replaceQuote(objMapper.writeValueAsString(jsonData)) + "'"
								+ " where nsitecode = "+userInfo.getNtranssitecode()+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and  ntransactionresultcode = " +ntransactionresultcode+ ";");			


						if(lstSampleResults != null && lstSampleResults.size() > 0 && lstSampleResults.get(0).getSresult() != null 
								&& !(lstSampleResults.get(0).getSresult().equalsIgnoreCase(sresult))) {



							sb.append(" Insert into resultchangehistory(nresultchangehistorycode,ntransactionresultcode,ntransactiontestcode,npreregno,ntestgrouptestparametercode,nparametertypecode,"
									+ " nformcode,nenforceresult,nenforcestatus,ngradecode,nenteredby,nenteredrole,ndeputyenteredby,ndeputyenteredrole,jsondata,nsitecode,nstatus)"
									+ " select "+nresultchangehistorycode+" +RANK()over(order by ntransactionresultcode) nresultchangehistorycode,ntransactionresultcode,ntransactiontestcode,npreregno,ntestgrouptestparametercode,"
									+ " "+nParameterTypeCode+" nparametertypecode,"+userInfo.getNformcode()+","+Enumeration.TransactionStatus.YES.gettransactionstatus()+" nenforceresult,"+nforcestatus+" nenforcestatus,"
									+ " "+nGradeCode+" ngradecode, "+userInfo.getNusercode()+" nenteredby, "+userInfo.getNuserrole()+" nenteredrole,"+userInfo.getNdeputyusercode()+" ndeputyenteredby,"+userInfo.getNdeputyuserrole()+" ndeputyenteredrole,"
									+ " '{\"sresult\":"
									+ stringUtilityFunction.replaceQuote(objMapper.writeValueAsString(lstSampleResults.get(0).getJsondata().get("sresult")))+","
									+ " \"sfinal\":"
									+ stringUtilityFunction.replaceQuote(objMapper.writeValueAsString(lstSampleResults.get(0).getJsondata().get("sfinal")))+","
									+ " \"nresultaccuracycode\":"
									+ stringUtilityFunction.replaceQuote(objMapper.writeValueAsString(lstSampleResults.get(0).getJsondata().get("nresultaccuracycode")))+","
									+ " \"sresultaccuracyname\":"
									+ stringUtilityFunction.replaceQuote(objMapper.writeValueAsString(lstSampleResults.get(0).getJsondata().get("sresultaccuracyname")))+","
									+ " \"nunitcode\":"
									+ stringUtilityFunction.replaceQuote(objMapper.writeValueAsString(lstSampleResults.get(0).getNunitcode()))+","
									+ " \"sunitname\":"
									+ stringUtilityFunction.replaceQuote(objMapper.writeValueAsString(lstSampleResults.get(0).getSunitname()))+","
									+ " \"dentereddate\":\""
									+ lstSampleResults.get(0).getJsondata().get("dentereddate").toString() + "\","
									+ " \"dentereddatetimezonecode\":\""
									+ lstSampleResults.get(0).getJsondata().get("dentereddatetimezonecode") + "\","
									+ " \"noffsetdentereddate\":\""
									+ lstSampleResults.get(0).getJsondata().get("noffsetdentereddate")+ "\""
									+ "}'::jsonb,"+userInfo.getNtranssitecode()+" nsitecode,"+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" nstatus "
									+ " from resultparameter where nsitecode ="+userInfo.getNtranssitecode()+" "
									+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
									+ " and ntransactionresultcode="+ntransactionresultcode+";");			

							nresultchangehistorycode++;
						}	
						sb.append("update seqnoregistration  set nsequenceno="+(nresultchangehistorycode)+" where stablename=N'resultchangehistory' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";");
						jdbcTemplate.execute(sb.toString());

						query = " select rp.*,"
								+ " rp.jsondata->>'smaxa'  as smaxa,"
								+ " rp.jsondata->>'smaxb'  as smaxb,"
								+ " rp.jsondata->>'smina'  as smina,"
								+ " rp.jsondata->>'sminb'  as sminb,"
								+ " rp.jsondata->>'nroundingdigits' as nroundingdigits,"
								+ " rp.jsondata->>'sfinal' sfinal ,"
								+ " rp.jsondata->>'nresultaccuracycode' nresultaccuracycode ,rp.jsondata->>'sresultaccuracyname' sresultaccuracyname ," 
								+ " t.jsondata->'stransdisplaystatus'->>'"
								+ userInfo.getSlanguagetypecode() + "' as stransdisplaystatus,"
								+ " rp.jsondata->>'stestsynonym' stestsynonym,"
								+ " rp.jsondata->>'sparametersynonym' sparametersynonym,"
								+ " r.sarno,rs.ssamplearno,g.sgradename,u.sunitname,rt.ntestgrouptestcode "
								+ " from resultparameter rp,transactionstatus t,registrationarno r,registrationsamplearno rs,"
								+ " registrationtest rt,grade g,unit u "
								+ " where t.ntranscode=rp.ntransactionstatus and r.npreregno=rp.npreregno and r.npreregno=rs.npreregno"
								+ " and rs.npreregno=rp.npreregno and rt.ntransactionsamplecode=rs.ntransactionsamplecode"
								+ " and rt.ntransactiontestcode=rp.ntransactiontestcode and u.nunitcode=rp.nunitcode  "
								+ " and rt.npreregno=rs.npreregno "
								+ " and r.npreregno=rt.npreregno "
								+ " and g.ngradecode=rp.ngradecode "
								+ " and rp.nsitecode = "+userInfo.getNtranssitecode()+""
								+ " and r.nsitecode = "+userInfo.getNtranssitecode()+""
								//+ " and rd.nsitecode = "+userInfo.getNtranssitecode()+""
								+ " and rt.nsitecode = "+userInfo.getNtranssitecode()+""
								+ " and g.nsitecode = "+userInfo.getNmastersitecode()+" "
								+ " and u.nsitecode = "+userInfo.getNmastersitecode()+" "
								+ " and rp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
								+ " and r.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
								//+ " and rd.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
								+ " and rt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
								+ " and g.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
								+ " and u.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
								+ " and t.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
								+ " and rp.ntransactionresultcode in (" + ntransactionresultcode + ")";

						List<ResultParameter> lstSampleResultsafter = jdbcTemplate.query(query, new ResultParameter());

						jsonAuditOldData.put("resultparameter", lstSampleResults);
						jsonAuditNewData.put("resultparameter", lstSampleResultsafter);			
						auditActionType.put("resultparameter", "IDS_RESULTENTER");
						Map<String, Object> objMap = new HashMap<>();
						objMap.put("nregtypecode", nregtypecode);
						objMap.put("nregsubtypecode", nregsubtypecode);
						objMap.put("ndesigntemplatemappingcode", ndesigntemplatemappingcode);
						auditUtilityFunction.fnJsonArrayAudit(jsonAuditOldData, jsonAuditNewData, auditActionType, objMap, false, userInfo);
					}
				}
				System.out.println("a1"+sb.toString());
				str="IDS_RESULTUPDATESUCCESSFULLY";
				map.put("returnStatus",str);	


			}else {
				str="IDS_CHECKCONFIGURATION";
				map.put("returnStatus",str);	

			}
		}	
		return map;
	}


	private int numericGrade(String npreregno,String ntestgrouptestcode,String finalResultValue ,UserInfo userInfo) {
		String queryTest = " select tgtnp.ntransactionresultcode,tgtnp.ntransactiontestcode,tgtnp.npreregno,tgtnp.nparametertypecode,"
				+ " tgtnp.nroundingdigit,tgtnp.nparametertypecode,"+Enumeration.Grade.FIO.getGrade()+" ngradecode,tgtnp.sresultvalue as sresult,"
				+ " case when tgtnp.nparametertypecode = "+Enumeration.ParameterType.NUMERIC.getparametertype()+" then"
				+ " cast(round(cast(NULLIF(tgtnp.sresultvalue,'') as decimal),tgtnp.nroundingdigits) as character varying)"
				+ " else tgtnp.sfinal end as sfinal,tgtnp.nroundingdigits,"
				+ " tgtnp.ntestgrouptestcode,tgtnp.ntestgrouptestparametercode, "
				+ " case when tgtnp.nparametertypecode = "+Enumeration.ParameterType.NUMERIC.getparametertype()+" then ( "					 
				+ " case "
				+ " when tgtnp.sminb is null and tgtnp.smina is null and tgtnp.smaxa is null and tgtnp.smaxb is  null then "+Enumeration.Grade.PASS.getGrade()+" "

					 + " when tgtnp.sminb is null and tgtnp.smina is null and tgtnp.smaxa is null and tgtnp.smaxb is not null "
					 + " then case when cast("+finalResultValue+" as float)<=tgtnp.smaxb then "+Enumeration.Grade.PASS.getGrade()+" "
					 + " else "+Enumeration.Grade.H_OOS.getGrade()+" end "

					 + " when tgtnp.sminb is null and tgtnp.smina is null and tgtnp.smaxa is not null and tgtnp.smaxb is null then "
					 + " case when cast("+finalResultValue+" as float)<=tgtnp.smaxa then "+Enumeration.Grade.PASS.getGrade()+" "
					 + " else "+Enumeration.Grade.H_OOS.getGrade()+" end "  

					 + " when tgtnp.sminb is null and tgtnp.smina is null and tgtnp.smaxa is not null and tgtnp.smaxb is not null then "
					 + " case when cast("+finalResultValue+" as float) between tgtnp.smaxa and tgtnp.smaxb then "+Enumeration.Grade.H_OOT.getGrade()+" "
					 + " when cast("+finalResultValue+" as float) > tgtnp.smaxb  then "+Enumeration.Grade.H_OOS.getGrade()+" "
					 + " else "+Enumeration.Grade.PASS.getGrade()+" end " 

			 		 + " when tgtnp.sminb is null and tgtnp.smina is not null and tgtnp.smaxa is null and tgtnp.smaxb is null "
			 		 + " then case when cast("+finalResultValue+" as float)>=tgtnp.smina then "+Enumeration.Grade.PASS.getGrade()+" "
			 		 + " else "+Enumeration.Grade.OOS.getGrade()+" end " 

			 		 + " when tgtnp.sminb is null and tgtnp.smina is not null and tgtnp.smaxa is null and tgtnp.smaxb is not null then "
			 		 + " case when cast("+finalResultValue+" as float) between tgtnp.smina and tgtnp.smaxb then "+Enumeration.Grade.PASS.getGrade()+" "
			 		 + " when cast("+finalResultValue+" as float) < tgtnp.smina then "+Enumeration.Grade.OOS.getGrade()+" "
			 		 + " when cast("+finalResultValue+" as float) > tgtnp.smaxb then "+Enumeration.Grade.H_OOS.getGrade()+" end " 

			 		 + " when tgtnp.sminb is null and tgtnp.smina is not null and tgtnp.smaxa is not null and tgtnp.smaxb is null "
			 		 + " then case when cast("+finalResultValue+" as float) between tgtnp.smina and tgtnp.smaxa then "+Enumeration.Grade.PASS.getGrade()+" "
			 		 + " when cast("+finalResultValue+" as float) < tgtnp.smina then "+Enumeration.Grade.OOS.getGrade()+" "
			 		 + " when cast("+finalResultValue+" as float) > tgtnp.smaxa then "+Enumeration.Grade.H_OOS.getGrade()+" end " 


			 		 + " when tgtnp.sminb is null and tgtnp.smina is not null and tgtnp.smaxa is not null and tgtnp.smaxb is not null then "
			 		 + " case when cast("+finalResultValue+" as float) between tgtnp.smina and tgtnp.smaxa then "+Enumeration.Grade.PASS.getGrade()+" "
			 		 + " when cast("+finalResultValue+" as float) between tgtnp.smaxa and tgtnp.smaxb then "+Enumeration.Grade.H_OOT.getGrade()+" "
			 		 + " when cast("+finalResultValue+" as float) > tgtnp.smaxb then "+Enumeration.Grade.H_OOS.getGrade()+" "
			 		 + " when cast("+finalResultValue+" as float) < tgtnp.smina then "+Enumeration.Grade.OOS.getGrade()+"  end "

			 		 + " when tgtnp.sminb is not null and tgtnp.smina is null and tgtnp.smaxa is null and tgtnp.smaxb is null then "
			 		 + " case when cast("+finalResultValue+" as float) >= tgtnp.sminb then "+Enumeration.Grade.PASS.getGrade()+" else "+Enumeration.Grade.OOS.getGrade()+" end " 

			 		 + " when tgtnp.sminb is not null and tgtnp.smina is null and tgtnp.smaxa is null and tgtnp.smaxb is not null then "
			 		 + " case when cast("+finalResultValue+" as float) between tgtnp.sminb and tgtnp.smaxb then "+Enumeration.Grade.PASS.getGrade()+" "
			 		 + " when cast("+finalResultValue+" as float) > tgtnp.smaxb then "+Enumeration.Grade.H_OOS.getGrade()+" "
			 		 + "	 when cast("+finalResultValue+" as float) < tgtnp.sminb  then "+Enumeration.Grade.OOS.getGrade()+" "
			 		 + " end "

			 		 + " when tgtnp.sminb is not null and tgtnp.smina is null and tgtnp.smaxa is not null and tgtnp.smaxb is null then "
			 		 + " case when cast("+finalResultValue+" as float) between tgtnp.sminb and tgtnp.smaxa then "+Enumeration.Grade.PASS.getGrade()+" "
			 		 + " when cast("+finalResultValue+" as float) < tgtnp.sminb then "+Enumeration.Grade.OOS.getGrade()+" "
			 		 + " when cast("+finalResultValue+" as float) > tgtnp.smaxa then "+Enumeration.Grade.H_OOS.getGrade()+"  end "  

			 		 + " when tgtnp.sminb is not null and tgtnp.smina is null and tgtnp.smaxa is not null and tgtnp.smaxb is not null then "
			 		 + " case when cast("+finalResultValue+" as float) between tgtnp.sminb and tgtnp.smaxa then "+Enumeration.Grade.PASS.getGrade()+" "
			 		 + " when cast("+finalResultValue+" as float) between tgtnp.smaxa and tgtnp.smaxb then "+Enumeration.Grade.H_OOT.getGrade()+" "
			 		 + " when cast("+finalResultValue+" as float) >  tgtnp.smaxb then "+Enumeration.Grade.H_OOS.getGrade()+" "
			 		 + " when cast("+finalResultValue+" as float) <  tgtnp.sminb then "+Enumeration.Grade.OOS.getGrade()+" end " 

			 		 + " when tgtnp.sminb is not null and tgtnp.smina is not null and tgtnp.smaxa is null and tgtnp.smaxb is null then "
			 		 + " case when cast("+finalResultValue+" as float) between tgtnp.sminb and tgtnp.smina then "+Enumeration.Grade.OOT.getGrade()+" "
			 		 + " when cast("+finalResultValue+" as float) < tgtnp.sminb then "+Enumeration.Grade.OOS.getGrade()+" "
			 		 + " when cast("+finalResultValue+" as float) > tgtnp.smina  then "+Enumeration.Grade.PASS.getGrade()+" end " 

			 		 + " when tgtnp.sminb is not null and tgtnp.smina is not null and tgtnp.smaxa is null and tgtnp.smaxb is not null "
			 		 + " then case when cast("+finalResultValue+" as float) between tgtnp.smina and tgtnp.smaxb then "+Enumeration.Grade.PASS.getGrade()+" "
			 		 + " when cast("+finalResultValue+" as float) between tgtnp.sminb and tgtnp.smina  then "+Enumeration.Grade.OOT.getGrade()+"  "
			 		 + " when cast("+finalResultValue+" as float) > tgtnp.smaxb  then "+Enumeration.Grade.H_OOS.getGrade()+ ""
			 		 + " when cast("+finalResultValue+" as float) < tgtnp.sminb then "+Enumeration.Grade.OOS.getGrade()+" end "

			 		 + " when tgtnp.sminb is not null and tgtnp.smina is not null and tgtnp.smaxa is not null and tgtnp.smaxb is null  then "
			 		 + " case when cast("+finalResultValue+" as float) between tgtnp.smina and tgtnp.smaxa then "+Enumeration.Grade.PASS.getGrade()+" "
			 		 + " when cast("+finalResultValue+" as float) between tgtnp.sminb and tgtnp.smina then "+Enumeration.Grade.OOT.getGrade()+" "
			 		 + " when cast("+finalResultValue+" as float) < tgtnp.sminb  then "+Enumeration.Grade.OOS.getGrade()+" "
			 		 + " when cast("+finalResultValue+" as float) > tgtnp.smina then "+Enumeration.Grade.H_OOS.getGrade()+" end "

			 		 + " when tgtnp.sminb is not null and tgtnp.smina is not null and tgtnp.smaxa is not null and tgtnp.smaxb is not null then "
			 		 + " case when cast("+finalResultValue+" as float) between tgtnp.smina and tgtnp.smaxa then "+Enumeration.Grade.PASS.getGrade()+"  "
			 		 + " when cast("+finalResultValue+" as float) between tgtnp.sminb and tgtnp.smina then "+Enumeration.Grade.OOT.getGrade()+" "
			 		 + " when cast("+finalResultValue+" as float) between tgtnp.smina and tgtnp.smaxb then "+Enumeration.Grade.H_OOT.getGrade()+" "
			 		 + " when cast("+finalResultValue+" as float) < tgtnp.sminb then "+Enumeration.Grade.OOS.getGrade()+" "
			 		 + " when cast("+finalResultValue+" as float) > tgtnp.smaxb then "+Enumeration.Grade.H_OOS.getGrade()+" end " 		 		 

			 		 + " else "+Enumeration.Grade.FIO.getGrade()+" end) "
			 		 + " end ngradecodenew " 
			 		 + " from ("
			 		 + " select   rp.ntransactionresultcode,rp.ntransactiontestcode,rp.npreregno,rp.nparametertypecode,(rp.jsondata->>'nroundingdigits')::int nroundingdigit,cast(NULLIF(rp.jsondata->>'sminb','') as float) sminb,cast(NULLIF(rp.jsondata->>'smina','') as float) smina, cast(NULLIF(rp.jsondata->>'smaxb','') as float) smaxb, "  
			 		 + " cast(NULLIF(rp.jsondata->>'smaxa','') as float) smaxa,"
			 		 + " case when tgtp.nparametertypecode="
			 		 + Enumeration.ParameterType.NUMERIC.getparametertype() + " then  case when rp.jsondata->> 'sresultvalue'<> 'null' "
			 		 + " then rp.jsondata->> 'sresultvalue' else "
			 		 + " tgtnp.sresultvalue end "
			 		 + " end sresultvalue,"	
			 		 + " case when tgtp.nparametertypecode=" + Enumeration.ParameterType.NUMERIC.getparametertype() + " "
			 		 + " then cast(round(cast(NULLIF(tgtnp.sresultvalue,'') as decimal),tgtp.nroundingdigits) as character varying(50)) end sfinal,tgtp.nroundingdigits,tgt.ntestgrouptestcode,tgtp.ntestgrouptestparametercode "
			 		 + " from  resultparameter rp,testgrouptest tgt "
			 		 + " inner join testgrouptestparameter tgtp on tgtp.ntestgrouptestcode = tgt.ntestgrouptestcode and tgtp.nsitecode = "+userInfo.getNmastersitecode()+" "
			 		 + " and tgtp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
			 		 + " left outer join testgrouptestnumericparameter tgtnp on tgtnp.ntestgrouptestparametercode = tgtp.ntestgrouptestparametercode and tgtnp.nsitecode = "+userInfo.getNmastersitecode()+" "
			 		 + " and tgtnp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
			 		 + " where "
			 		 + " rp.ntestgrouptestparametercode = tgtp.ntestgrouptestparametercode and tgt.ntestgrouptestcode in (select ntestgrouptestcode from registrationtest rt "
			 		 + " where "
			 		 + "  rp.nsitecode = "+userInfo.getNtranssitecode()
			 		 + " and tgt.nsitecode = "+userInfo.getNmastersitecode()
			 		 + " and rp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ ""
			 		 + " and tgt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
			 		 + " AND rp.nenforceresult <> "+Enumeration.TransactionStatus.YES.gettransactionstatus() 
			 		 + " and rp.nparametertypecode = "+Enumeration.ParameterType.NUMERIC.getparametertype()
			 		 + " and npreregno in (" + npreregno + ") "
			 		 + " and ntestgrouptestcode in ("+ntestgrouptestcode+")  "
			 		 + " and nsitecode = "+userInfo.getNtranssitecode()+") "
			 		 + " and rp.npreregno in (" + npreregno + ")  "
			 		 +" ) tgtnp ";

		List<ResultParameter> lstGetDefaultValue = jdbcTemplate.query(queryTest, new ResultParameter());
		int gradecode =-1;
		if(lstGetDefaultValue.size() > 0) {
			gradecode =lstGetDefaultValue.get(0).getNgradecodenew();
		}
		return gradecode;
	}



	//Added by sonia for ALPD-4084 on May 2 2024 Import action
	private double round(double r, int rndgt) {
		if (rndgt < 0) throw new IllegalArgumentException();

		long factor = (long) Math.pow(10, rndgt);
		r = r * factor;
		long tmp = Math.round(r);
		return (double) tmp / factor;

	}

	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> updateFormulaCalculation(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		Map<String, Object> returnMap = new HashMap<>();
		JSONObject auditOldData = new JSONObject();
		JSONObject auditNewData = new JSONObject();
		JSONObject auditActionType = new JSONObject();
		List<ResultParameter> lstdata = new ArrayList<>();
		List<ResultParameter> lstdataold = new ArrayList<>();
		final Map<String, Object> objMap = new HashMap<>();
		List<Map<String, Object>> lstResults = new ArrayList<>();
		List<ResultParameter> lstaudit = new ArrayList<>();
		ObjectMapper objectMapper = new ObjectMapper();
		String sResultQuery="";
		String insertResultChange="";
		String strQuery="";
		String resultChangeHistoryInsertValues="";
		String ntransactiontestcode = (String) inputMap.get("ntransactiontestcode");
		final int ntestgrouptestparametercode =  (int) inputMap.get("ntestgrouptestparametercode");
		final int npreregno = (int) inputMap.get("npreregno");
		final int ntransactionresultcode=(int) inputMap.get("ntransactionresultcode");
		final int nallottedspeccode=(int) inputMap.get("nallottedspeccode");
		final int nispredefinedformula=(int) inputMap.get("nispredefinedformula");
		final int npredefinedformulacode=(int) inputMap.get("npredefinedformulacode");
		final int nroundingdigits=(int) inputMap.get("nroundingdigits");
		ObjectMapper objMapper = new ObjectMapper();

		List<RegistrationTestHistory> lstvalidationtest = validateTestStatus(String.valueOf(inputMap.get("transactiontestcodeforvalidation"))
				,(int)inputMap.get("ncontrolcode") ,
				userInfo,(int) inputMap.get("nneedReceivedInLab"));

		if (!lstvalidationtest.isEmpty()) {
			final String squery = " select rp.*,rp.jsondata->>'nroundingdigits' as nroundingdigit,rp.jsondata->>'sparametername' as sparametername,rp.jsondata->>'sfinal' sfinal ,rp.jsondata->>'sresult' sresult ," + " t.jsondata->'stransdisplaystatus'->>'"
					+ userInfo.getSlanguagetypecode() + "' as stransdisplaystatus,"
					+ " rp.jsondata->>'stestsynonym' stestsynonym,"
					+ " rp.jsondata->>'sresultaccuracyname' sresultaccuracyname,"
					+ " rp.jsondata->>'sparametersynonym' sparametersynonym,"
					+ " rp.jsondata->>'nresultaccuracycode' nresultaccuracycode ," 
					+ " rp.jsondata->>'dentereddate' dentereddate, "
					+ " rp.jsondata->>'dentereddatetimezonecode' dentereddatetimezonecode,"
					+ " rp.jsondata->>'noffsetdentereddate' noffsetdentereddate,"
					+ " r.sarno,rs.ssamplearno,g.sgradename,u.sunitname,u.nunitcode "

				+ " from resultparameter rp,transactionstatus t,registrationarno r,registrationsamplearno rs,"
				+ " registrationtest rt,grade g,unit u"

				+ " where t.ntranscode=rp.ntransactionstatus and r.npreregno=rp.npreregno and r.npreregno=rs.npreregno"
				+ " and rs.npreregno=rp.npreregno and rt.ntransactionsamplecode=rs.ntransactionsamplecode"
				+ " and rt.ntransactiontestcode=rp.ntransactiontestcode and u.nunitcode=rp.nunitcode "
				+ " and rt.npreregno=rs.npreregno and r.npreregno=rt.npreregno and g.ngradecode=rp.ngradecode "
				+ " and rs.nsitecode = r.nsitecode and rs.nsitecode = rp.nsitecode and r.nsitecode = "+ userInfo.getNtranssitecode() + ""
				+ " and rp.nparametertypecode="+Enumeration.ParameterType.NUMERIC.getparametertype()+ " "
				+ " and rp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and t.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and r.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and rs.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and rt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and g.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and u.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and rp.nsitecode="+userInfo.getNtranssitecode()+" "
				+ " and r.nsitecode="+userInfo.getNtranssitecode()+" "
				+ " and rs.nsitecode="+userInfo.getNtranssitecode()+" "
				+ " and rt.nsitecode="+userInfo.getNtranssitecode()+" "
				+ " and g.nsitecode="+userInfo.getNmastersitecode()+" "
				+ " and u.nsitecode="+userInfo.getNmastersitecode()+" "
				+ " and rp.ntransactionresultcode = " + ntransactionresultcode+ " ";

			List<ResultParameter> lstParameters = jdbcTemplate.query(squery, new ResultParameter());

			final String sFunctionQuery="select spredefinedformula from predefinedformula where npredefinedformulacode="+npredefinedformulacode+""
					+ " and nsitecode = "+userInfo.getNmastersitecode()+" and nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";

			final String sFormulaName = jdbcTemplate.queryForObject(sFunctionQuery, String.class);

			strQuery ="select * from "+sFormulaName+"("+npreregno+","+userInfo.getNsitecode()+")";					

			if(!strQuery.isEmpty()) {
				final String result= jdbcTemplate.queryForObject(strQuery, String.class);

				lstResults = objectMapper.readValue(result.toString(),new TypeReference<List<Map<String, Object>>>() {});

				if(!lstResults.isEmpty()){

					String sresult=lstResults.get(0).get("sresult")!=null?(String) lstResults.get(0).get("sresult"):"0";

					sResultQuery=sResultQuery + "update resultparameter set jsondata = jsondata || "
							+ "jsonb_build_object('sfinal',cast(round(cast('"+sresult+"' as decimal),"+nroundingdigits+") as character varying(50)),'sresult','"+sresult+"',"
							+ " 'dentereddate','"+dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ").replace("Z","").toString()+"' ,"
							+ " 'dentereddatetimezonecode',"+userInfo.getNtimezonecode()+",'noffsetdentereddate',"+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())+")::jsonb "
							+ " ,nenteredby="+userInfo.getNusercode()+" , nenteredrole="+userInfo.getNuserrole()+" where ntransactionresultcode= "+ntransactionresultcode+" and nstatus = "
							+ 	Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNtranssitecode()+";";

					if(!lstParameters.isEmpty() && lstParameters.get(0).getSresult()!=null) {

						final String query = " select nsequenceno from seqnoregistration where stablename=N'resultchangehistory' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
						int nresultchangehistoryno = jdbcTemplate.queryForObject(query, Integer.class);
						nresultchangehistoryno++;			                	

						final int dentereddatetimezonecode=lstParameters.get(0).getDentereddatetimezonecode()!=0?lstParameters.get(0).getDentereddatetimezonecode():userInfo.getNtimezonecode();
						final int noffsetdentereddate=lstParameters.get(0).getNoffsetdentereddate()!=0?lstParameters.get(0).getNoffsetdentereddate():dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid());
						final String currentDateTime = lstParameters.get(0).getDentereddate()!=null?lstParameters.get(0).getDentereddate().toString():
							dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ").replace("Z",""); 

						resultChangeHistoryInsertValues += " (" + nresultchangehistoryno + ","
								+ lstParameters.get(0).getNtransactionresultcode() + ","
								+ lstParameters.get(0).getNtransactiontestcode() + ","
								+ lstParameters.get(0).getNpreregno() + ","
								+ lstParameters.get(0).getNtestgrouptestparametercode() + ","
								+ Enumeration.ParameterType.NUMERIC.getparametertype() + ","
								+ userInfo.getNformcode() + ","
								+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ","
								+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ","
								+ lstParameters.get(0).getNgradecode() + "," + userInfo.getNusercode()
								+ "," + userInfo.getNuserrole() + "," + userInfo.getNdeputyusercode() + ","
								+ userInfo.getNdeputyuserrole() + ","
								+ "'{" + " \"sresult\":"+stringUtilityFunction.replaceQuote(objMapper.writeValueAsString(lstParameters.get(0).getSresult()))+","
								+ " \"sfinal\":"+ stringUtilityFunction.replaceQuote(objMapper.writeValueAsString(lstParameters.get(0).getSfinal()))+","
								+ " \"sresultaccuracyname\":"+ stringUtilityFunction.replaceQuote(objMapper.writeValueAsString(lstParameters.get(0).getSresultaccuracyname()))+","
								+ " \"nunitcode\":"+ stringUtilityFunction.replaceQuote(objMapper.writeValueAsString(lstParameters.get(0).getNunitcode()))+","
								+ " \"sunitname\":"+ stringUtilityFunction.replaceQuote(objMapper.writeValueAsString(lstParameters.get(0).getSunitname()))+","
								+ " \"dentereddate\":\""+ currentDateTime + "\","
								+ " \"dentereddatetimezonecode\":\""+ dentereddatetimezonecode + "\","
								+ " \"noffsetdentereddate\":\""+ noffsetdentereddate + "\" }'::jsonb ,"
								+ userInfo.getNtranssitecode() + ","+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";

						insertResultChange= insertResultChange+"insert into resultchangehistory (nresultchangehistorycode,ntransactionresultcode,ntransactiontestcode,"
								+ "npreregno,ntestgrouptestparametercode,nparametertypecode,nformcode,nenforceresult,"
								+ "nenforcestatus,ngradecode,nenteredby,nenteredrole,ndeputyenteredby,ndeputyenteredrole,jsondata,nsitecode,nstatus) "
								+ " values"
								+ resultChangeHistoryInsertValues.substring(0, resultChangeHistoryInsertValues.length() - 1) + ";";

						jdbcTemplate.execute("update seqnoregistration  set nsequenceno=" + (nresultchangehistoryno+1)
								+ " where stablename=N'resultchangehistory' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";");

					}
					jdbcTemplate.execute(sResultQuery+insertResultChange);




				}
			}
			returnMap.putAll((Map<String, Object>) getTestbasedParameter(ntransactiontestcode, userInfo).getBody());	
			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		}
		else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTVALIDTEST", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}

	//ALPD-4156--Vignesh R--Result Entry - Option to change section for the test(s)
	public ResponseEntity<Object> getSectionChange(Map<String,Object> inputMap,UserInfo userInfo) throws Exception {

		final int nregtypecode=(int) inputMap.get("nregtypecode");
		final int nregsubtypecode=(int) inputMap.get("nregsubtypecode");
		final String spreregno = (String) inputMap.get("npreregno");
		final String transactionSampleCode = (String) inputMap.get("ntransactionsamplecode");
		final String transactionTestCode = (String) inputMap.get("ntransactiontestcode");
		final int controlCode = (int) inputMap.get("ncontrolcode");

		if(transactionTestCode.isEmpty()) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTTEST", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);

		}else {


			List<String> transtestcodeList = Arrays.asList(transactionTestCode.split(","));


			final String strTestCodeQuery = "select rt.ntestcode from registrationtest rt,registrationtesthistory rth "
					+ " where rt.ntransactiontestcode =rth.ntransactiontestcode " + "and rt.npreregno in(" + spreregno
					+ ") and rt.ntransactiontestcode in(" + transactionTestCode + ") "
					+ " and rth.ntesthistorycode in (select max(ntesthistorycode) from registrationtesthistory where "
					+ " nsitecode = "+userInfo.getNtranssitecode()+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and npreregno in("+ spreregno + ") and ntransactiontestcode in(" + transactionTestCode+ ") "
					+ " group by ntransactiontestcode) and rt.nsitecode = "+userInfo.getNtranssitecode()+" and rth.nsitecode = "+userInfo.getNtranssitecode()+" "
					+ " and rt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and rth.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
					+ " group by rt.ntestcode;";

			List<String> lsttestcode = jdbcTemplate.queryForList(strTestCodeQuery, String.class);
			String testCode = lsttestcode.stream().collect(Collectors.joining(","));

			List<String> ntestcodeList = Arrays.asList(testCode.split(","));
			final int ntestcodesize=ntestcodeList.size();

			Map<String,Object> map=new HashMap<>();
			inputMap.put("ntestcodesize", ntestcodesize);
			final String str=(String) getSectionByTest(inputMap,testCode,userInfo);

			final List<Section> lstSection = ((List<Section>) jdbcTemplate.query(str, new Section()));
			map.put("Section", lstSection);			
			return new ResponseEntity<>(map, HttpStatus.OK);

		}

	}


	//ALPD-4156--Vignesh R(15-05-2024)-->Result Entry - Option to change section for the test(s)
	public String getSectionByTest(Map<String,Object>inputMap,String ntestcode,UserInfo userInfo) {

		final String str="select st.nsectioncode,st.ssectionname from site s,sitedepartment sd,departmentlab dl,labsection ls,section st"
				+ " where s.nsitecode=sd.nsitecode"
				+ " AND sd.nsitedeptcode=dl.nsitedeptcode"
				+ " AND dl.ndeptlabcode=ls.ndeptlabcode"
				+ " AND s.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" AND sd.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
				+"  AND dl.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
				+ " AND sd.nsitecode="+userInfo.getNtranssitecode()+"" 
				+"  AND ls.nsectioncode=st.nsectioncode"
				+"  AND ls.nsectioncode in(select nsectioncode from "
				+ " (select max(s.ssectionname) ssectionname,ts.nsectioncode,count(ts.nsectioncode) ncount from testsection ts,section s"
				+ " where ts.nsectioncode = s.nsectioncode "
				+ " and ts.nsitecode = "+userInfo.getNmastersitecode()+" "
				+ " and s.nsitecode = "+userInfo.getNmastersitecode()+""
				+ " and ts.nstatus  = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and s.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and ntestcode in ("+ntestcode+")"
				+ " group by ts.nsectioncode)a where a.ncount="+inputMap.get("ntestcodesize")+") "
				+"  AND st.nsectioncode <> "+inputMap.get("nsectioncode")+""
				+"  AND st.nsectioncode >0 "
				+ " AND ls.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " group by st.nsectioncode,st.ssectionname";

		return str;
	}

	//ALPD-4156--Vignesh R(15-05-2024)-->Result Entry - Option to change section for the test(s)

	public ResponseEntity<Object> updateSectionTest(Map<String,Object> inputMap,UserInfo userInfo) throws Exception {

		String spreregno = (String) inputMap.get("npreregno");
		String transactionSampleCode = (String) inputMap.get("ntransactionsamplecode");
		String transactionTestCode = (String) inputMap.get("ntransactiontestcode");
		boolean needjoballocation=(boolean)inputMap.get("nneedjoballocation");
		String[] transactionTestCodeArray = transactionTestCode.split(",");
		List<String> transactionTestCodeList = Arrays.asList(transactionTestCodeArray);
		int controlCode = (int) inputMap.get("ncontrolcode");
		String ssectioncode=String.valueOf(inputMap.get("nsectioncode"));
		inputMap.put("nsectioncode",inputMap.get("sfilterSection"));

		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
		auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
		auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));

		JSONObject actionType = new JSONObject();
		JSONObject actionTypeRecevieInLab = new JSONObject();

		JSONObject jsonAuditNew = new JSONObject();
		JSONObject jsonAuditOld = new JSONObject();

		Map<String,Object> map=new HashMap<>();
		String insertSectionQuery="";

		Integer nsectionhistoryseqcount;
		Integer nsectionseqcount;
		final String queryold = "select r.ntesthistorycode ,r.ntransactiontestcode ,r.npreregno ,"
				+ "r.ntransactionstatus,t.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "' as stransdisplaystatus,rt.jsondata->>'stestsynonym' stestsynonym,ra.sarno,rs.ssamplearno,"
				+ "rt.jsondata->>'ssectionname' ssectionname,rt.jsondata->>'smethodname' smethodname, rt.nsectioncode "
				+ "  from registrationtest rt,registrationarno ra,registrationsamplearno rs,"
				+ " registrationtesthistory r,transactionstatus t where rt.npreregno=ra.npreregno"
				+ " and rs.npreregno=ra.npreregno and rs.npreregno=rt.npreregno and rt.npreregno=r.npreregno"
				+ " and rt.ntransactionsamplecode=r.ntransactionsamplecode and rs.ntransactionsamplecode=r.ntransactionsamplecode and ra.npreregno=r.npreregno"
				+ " and rs.ntransactionsamplecode=rt.ntransactionsamplecode and rt.ntransactiontestcode=r.ntransactiontestcode"
				+ " and rt.ntransactiontestcode in ( " + transactionTestCode + ") and t.ntranscode=r.ntransactionstatus"
				+ " and r.ntesthistorycode in (  select max(ntesthistorycode) from  registrationtesthistory where "
				+ " nsitecode = "+userInfo.getNtranssitecode()+" "
				+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ntransactiontestcode IN ( "+ transactionTestCode + " ) group by ntransactiontestcode) "
				+ " and rt.nsitecode = "+ userInfo.getNtranssitecode() + " "
				+ " and ra.nsitecode = "+ userInfo.getNtranssitecode() + " "
				+ " and rs.nsitecode = "+ userInfo.getNtranssitecode() + " "
				+ " and r.nsitecode = " + userInfo.getNtranssitecode()  + " "
				+ " and rt.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and ra.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rs.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and r.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and t.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " order by 1 asc;";

		List<RegistrationTestHistory> lstold = jdbcTemplate.query(queryold, new RegistrationTestHistory()); 			


		String sectionameQuery="select ssectionname from section where nsectioncode ="+ssectioncode+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +"";
		String sectioName = (String) jdbcUtilityFunction.queryForObject(sectionameQuery, String.class,jdbcTemplate);

		String str = "update registrationtest  set nsectioncode ="+ssectioncode+","
				+ " jsondata = jsondata || '{\"ssectionname\": \""+sectioName+"\"}' where "
				+ " nsitecode = "+userInfo.getNtranssitecode()+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and npreregno in("+spreregno+") and "
				+ " ntransactionsamplecode in ("+transactionSampleCode+") and ntransactiontestcode in("+transactionTestCode+") ;";

		str =str+ "update joballocation  set nsectioncode ="+ssectioncode+" where nsitecode = "+userInfo.getNtranssitecode()+" "
				+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and npreregno in("+spreregno+") "
				+ " and ntransactionsamplecode in ("+transactionSampleCode+") and ntransactiontestcode in("+transactionTestCode+") ;";

		jdbcTemplate.execute(str);


		String existsSection="select * from registrationsection where npreregno in("+spreregno+") "
				+ " and nsectioncode ="+ssectioncode+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" "
				+ " and nsitecode="+userInfo.getNtranssitecode()+"";

		List<RegistrationSection> sampleList=(List<RegistrationSection>) jdbcTemplate.query(existsSection, new RegistrationSection());


		List<String> list1 = Arrays.asList(spreregno.split(","));

		List<String> list = list1.stream()
				.filter(a -> sampleList.stream().noneMatch(b -> b.getNpreregno()== Integer.parseInt(a)))
				.collect(Collectors.toList());

		Set<String> setFromStream = list.stream().collect(Collectors.toSet());

		if(!setFromStream.isEmpty()) {

			String str1 = "select nsequenceno from seqnoregistration where stablename ='registrationsectionhistory' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
			nsectionhistoryseqcount = jdbcTemplate.queryForObject(str1, Integer.class);

			String str2 = "select nsequenceno from seqnoregistration where stablename ='registrationsection' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
			nsectionseqcount = jdbcTemplate.queryForObject(str2, Integer.class);

			nsectionhistoryseqcount=nsectionhistoryseqcount+1;
			nsectionseqcount=nsectionseqcount+1;

			if(setFromStream.size()>0) {
				for (String samplearno : setFromStream ) {
					insertSectionQuery=insertSectionQuery+"INSERT INTO public.registrationsection("
							+ "	nregistrationsectioncode, npreregno, nsectioncode, nsitecode, nstatus)"
							+ "	VALUES ("+nsectionseqcount+", "+samplearno+", "+ssectioncode+", "+userInfo.getNtranssitecode()+","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +");";
					if(needjoballocation) {
						insertSectionQuery = insertSectionQuery+"Insert into registrationsectionhistory (nsectionhistorycode,npreregno,nsectioncode,ntransactionstatus,nsitecode,nstatus)"
								+" values("+nsectionhistoryseqcount+","+samplearno+","+ssectioncode+","
								+ ""+Enumeration.TransactionStatus.RECIEVED_IN_SECTION.gettransactionstatus()+","+userInfo.getNtranssitecode()+","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+");";
						nsectionhistoryseqcount++;
						insertSectionQuery =insertSectionQuery+ "update seqnoregistration set nsequenceno =" + nsectionhistoryseqcount
								+ " where stablename='registrationsectionhistory' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
					}

					nsectionseqcount++;
				}

				insertSectionQuery = insertSectionQuery+"update seqnoregistration set nsequenceno =" + nsectionseqcount
						+ " where stablename='registrationsection' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";

				jdbcTemplate.execute(insertSectionQuery);


				actionTypeRecevieInLab.put("registrationsection", "IDS_ADDSECTIONREG");
				JSONObject jsonAuditObject = new JSONObject();


				String query=testAudit(transactionTestCode,userInfo);
				List<RegistrationTestHistory> lstDataTest = jdbcTemplate.query(query, new RegistrationTestHistory());

				jsonAuditObject.put("registrationsection", lstDataTest);
				auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionTypeRecevieInLab, auditmap, false, userInfo);
			}
		}


		if(inputMap.containsKey("ntansactionSubSamplecode")) {
			inputMap.put("ntransactionsamplecode", inputMap.get("ntansactionSubSamplecode"));
		}
		//for change section audit
		actionType.put("registrationtest", "IDS_SECTIONUPDATE");
		jsonAuditOld.put("registrationtest", lstold);
		String query=testAudit(transactionTestCode,userInfo);

		List<RegistrationTestHistory> lstnew = jdbcTemplate.query(query, new RegistrationTestHistory());

		jsonAuditNew.put("registrationtest",lstnew);
		auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, jsonAuditNew, actionType, auditmap, false, userInfo);

		inputMap.put("ntestcode",String.valueOf(Enumeration.TransactionStatus.ALL.gettransactionstatus()));			
		inputMap.put("ntranscode",String.valueOf(Enumeration.TransactionStatus.ALL.gettransactionstatus()));			
		inputMap.put("ntransactiontestcode",String.valueOf(Enumeration.TransactionStatus.ALL.gettransactionstatus()));			
		map.putAll((Map<String, Object>) getResultEntryTest(inputMap, userInfo));		


		return new ResponseEntity<>(map,HttpStatus.OK);

	}	

	//ALPD-4156--Vignesh R(15-05-2024)-->Result Entry - Option to change section for the test(s)
	private String testAudit(String ntranstestcode,UserInfo userInfo) {

		final String query =  "select r.ntesthistorycode ,r.ntransactiontestcode ,r.npreregno ,"
				+ "r.ntransactionstatus,t.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "' as stransdisplaystatus,rt.jsondata->>'stestsynonym' stestsynonym,ra.sarno,rs.ssamplearno,"
				+ " rt.jsondata->>'ssectionname' ssectionname,rt.jsondata->>'smethodname' smethodname, rt.nsectioncode "
				+ "  from registrationtest rt,registrationarno ra,registrationsamplearno rs,"
				+ " registrationtesthistory r,transactionstatus t where rt.npreregno=ra.npreregno"
				+ " and rs.npreregno=ra.npreregno and rs.npreregno=rt.npreregno and rt.npreregno=r.npreregno"
				+ " and rt.ntransactionsamplecode=r.ntransactionsamplecode and rs.ntransactionsamplecode=r.ntransactionsamplecode and ra.npreregno=r.npreregno"
				+ " and rs.ntransactionsamplecode=rt.ntransactionsamplecode and rt.ntransactiontestcode=r.ntransactiontestcode"
				+ " and rt.ntransactiontestcode in ( " + ntranstestcode + ") and t.ntranscode=r.ntransactionstatus"
				+ " and r.ntesthistorycode in (  select max(ntesthistorycode) from  registrationtesthistory where ntransactiontestcode IN ( "+ ntranstestcode + ") "
				+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" group by ntransactiontestcode) "
				+ " and rt.nsitecode = "+userInfo.getNtranssitecode()+""
				+ " and ra.nsitecode = "+userInfo.getNtranssitecode()+""
				+ " and rs.nsitecode = "+userInfo.getNtranssitecode()+""
				+ " and r.nsitecode = "+userInfo.getNtranssitecode()+""
				+ " and r.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rt.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and ra.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rs.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and r.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " order by 1 asc;";
		return query;
	}

	public List<RegistrationTestHistory> validateExportTestStatus(String npreregno, String ntransactiontestcode, int ntestcode, int controlCode,
			UserInfo userInfo) throws Exception {

		String sTestQuery ="";

		if(ntestcode >0) {
			sTestQuery = "and rt.ntestcode ="+ntestcode+" ";
		}


		final String validateTests = "select rth.ntesthistorycode,rth.ntransactionstatus,rth.ntransactiontestcode,rth.npreregno "
				+ " from registrationtest rt,registrationtesthistory rth "
				+ " where rt.ntransactiontestcode =rth.ntransactiontestcode and rth.ntesthistorycode = any "
				+ " (select max(ntesthistorycode) from registrationtesthistory where "
				+ " npreregno in ("+ npreregno + ") "
				+ " and ntransactiontestcode in ("+ntransactiontestcode+")  "
				+ " and nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and nsitecode ="+userInfo.getNtranssitecode()
				+ " group by ntransactiontestcode) "
				+ " and rth.ntransactionstatus = any ( select ntransactionstatus from transactionvalidation tv , registration r "
				+ " where "
				+ " tv.nregtypecode=r.nregtypecode and tv.nregsubtypecode=r.nregsubtypecode and r.npreregno=rth.npreregno"
				+ " and tv.nformcode = " + userInfo.getNformcode() + " and tv.nsitecode = "+ userInfo.getNmastersitecode() + " "
				+ " and tv.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+"  and rth.nsitecode ="+userInfo.getNtranssitecode()
				
				+ " and tv.ncontrolcode = " + controlCode
				+ ") "
				+ " "+sTestQuery+" "
				+"  and rt.nsitecode ="+userInfo.getNtranssitecode()
				+ " and rth.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		return jdbcTemplate.query(validateTests, new RegistrationTestHistory());
	}
	//ALPD-4870-To get previously saved filter details when click the filter name,done by Dhanushya RI

	@Override
	public ResponseEntity<Object> getResultEntryFilter(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		ObjectMapper objMapper = new ObjectMapper();

		final String strQuery="select json_agg(jsondata || jsontempdata) as jsondata from filtername where nformcode="+userInfo.getNformcode()+" "
				+ " and nusercode="+userInfo.getNusercode()+" "
				+ " and nuserrolecode="+userInfo.getNuserrole()+" "
				+ " and nsitecode="+userInfo.getNtranssitecode()+" "
				+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nfilternamecode="+inputMap.get("nfilternamecode");

		String strFilter = jdbcTemplate.queryForObject(strQuery, String.class);

		final List<Map<String, Object>> lstfilterDetail = strFilter != null ? objMapper.readValue(strFilter, new TypeReference<List<Map<String, Object>>>() {
		}):new ArrayList<Map<String, Object>>();
		if(!lstfilterDetail.isEmpty()) {
			final String strValidationQuery="select json_agg(jsondata || jsontempdata) as jsondata from filtername where nformcode="+userInfo.getNformcode()+" and nusercode="+userInfo.getNusercode()+" "
					+ " and nuserrolecode="+userInfo.getNuserrole()+" and nsitecode="+userInfo.getNtranssitecode()+" "
					+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
					+ " and (jsondata->'nsampletypecode')::int="+inputMap.get("nsampletypecode")+" "  
					+ " and (jsondata->'nregtypecode')::int="+inputMap.get("nregtypecode")+" "  
					+ " and (jsondata->'nregsubtypecode')::int="+inputMap.get("nregsubtypecode")+" "  
					+ " and (jsontempdata->'ntranscode')::int="+inputMap.get("ntranscode")+" "  
					+ " and (jsontempdata->'napproveconfversioncode')::int="+inputMap.get("napproveconfversioncode")+" "
					+ " and (jsontempdata->'ndesigntemplatemappingcode')::int="+inputMap.get("ndesigntemplatemappingcode")+" "
					+ " and (jsontempdata->'ntestcode')::int="+inputMap.get("ntestcode")+" "
					+ " and (jsontempdata->>'DbFromDate')='"+inputMap.get("FromDate")+"' "
					+ " and (jsontempdata->>'DbToDate')='"+inputMap.get("ToDate")+"' and nfilternamecode="+inputMap.get("nfilternamecode")+"" ; 


			final String strValidationFilter = jdbcTemplate.queryForObject(strValidationQuery, String.class);

			final List<Map<String, Object>> lstValidationFilter = strValidationFilter != null ? objMapper.readValue(strValidationFilter, new TypeReference<List<Map<String, Object>>>() {
			}):new ArrayList<Map<String, Object>>();
			if(lstValidationFilter.isEmpty()) {
				Instant instantFromDate = dateUtilityFunction.convertStringDateToUTC(lstfilterDetail.get(0).get("fromDate").toString(),userInfo, true);
				Instant instantToDate = dateUtilityFunction.convertStringDateToUTC(lstfilterDetail.get(0).get("toDate").toString(),userInfo, true);

				final String fromDate = dateUtilityFunction.instantDateToString(instantFromDate);
				final String toDate =dateUtilityFunction.instantDateToString(instantToDate);

				inputMap.put("fromdate", fromDate);
				inputMap.put("todate", toDate);

				final DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
				final LocalDateTime ldt = LocalDateTime.parse(fromDate, formatter1);
				final LocalDateTime ldt1 = LocalDateTime.parse(toDate, formatter1);

				final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());
				String formattedFromString = "";
				String formattedToString = "";

				if (userInfo.getIsutcenabled()== Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					final ZonedDateTime zonedDateFromTime = ZonedDateTime.of(ldt, ZoneId.of(userInfo.getStimezoneid()));
					formattedFromString = zonedDateFromTime.format(formatter);
					final ZonedDateTime zonedDateToTime = ZonedDateTime.of(ldt1, ZoneId.of(userInfo.getStimezoneid()));
					formattedToString = zonedDateToTime.format(formatter);

				} else {
					formattedFromString = formatter.format(ldt);
					formattedToString= formatter.format(ldt1);

				}

				returnMap.put("fromDate", formattedFromString);
				returnMap.put("toDate", formattedToString);

				final String getSampleType = "select st.nsampletypecode,coalesce(st.jsondata->'sampletypename'->>'"
						+ userInfo.getSlanguagetypecode() + "',st.jsondata->'sampletypename'->>'en-US') ssampletypename,st.nclinicaltyperequired,st.nportalrequired "
						+ " from sampletype st,approvalconfig ac,approvalconfigversion acv,registrationtype rt,registrationsubtype rst"
						+ " where rt.nregtypecode = rst.nregtypecode and rst.nregsubtypecode = ac.nregsubtypecode and ac.napprovalconfigcode = acv.napprovalconfigcode "
						+ " and rt.nregtypecode = ac.nregtypecode and rt.nsampletypecode = st.nsampletypecode"
						+ " and acv.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
						+ " and st.nsitecode = "+ userInfo.getNmastersitecode() + " "
						+ " and ac.nsitecode = "+ userInfo.getNmastersitecode() + " "
						+ " and acv.nsitecode = "+ userInfo.getNmastersitecode() + " "
						+ " and rt.nsitecode = "+ userInfo.getNmastersitecode() + " "
						+ " and rst.nsitecode = "+ userInfo.getNmastersitecode() + " "
						+ " and acv.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ac.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and rt.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and rst.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and st.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and st.nsampletypecode > 0 "
						+ " group by st.nsampletypecode,st.jsondata->'sampletypename'->>'" + userInfo.getSlanguagetypecode()+ "'"
						+ "  order by st.nsorter";

				final List<SampleType> lstsampleType = jdbcTemplate.query(getSampleType, new SampleType());
				
				if (!lstsampleType.isEmpty()) {	

					final SampleType filterSampleType=!lstfilterDetail.isEmpty()?objMapper.convertValue(lstfilterDetail.get(0).get("sampleTypeValue"),SampleType.class):lstsampleType.get(0);
					projectDAOSupport.updateFilterDetail(inputMap,userInfo);
					returnMap.put("SampleType", lstsampleType);
					returnMap.put("realSampleTypeList", lstsampleType);
					inputMap.put("nsampletypecode", filterSampleType.getNsampletypecode());
					returnMap.put("defaultSampleType", filterSampleType);
					inputMap.put("defaultSampleType", filterSampleType);
					returnMap.put("realSampleTypeValue", filterSampleType);
					inputMap.put("filterDetailValue", lstfilterDetail);


					returnMap.putAll((Map<String, Object>) getRegistrationType(inputMap, userInfo).getBody());

					final RegistrationType filterRegType=!lstfilterDetail.isEmpty()? objMapper.convertValue(lstfilterDetail.get(0).get("regTypeValue"),RegistrationType.class):(RegistrationType) returnMap.get("defaultRegistrationType");
					final RegistrationSubType filterRegSubType=!lstfilterDetail.isEmpty()? objMapper.convertValue(lstfilterDetail.get(0).get("regSubTypeValue"),RegistrationSubType.class):(RegistrationSubType) returnMap.get("defaultRegistrationSubType");	
					final TransactionStatus filterTransactionStatus=!lstfilterDetail.isEmpty()? lstfilterDetail.get(0).containsKey("filterStatusValue")?objMapper.convertValue(lstfilterDetail.get(0).get("filterStatusValue"),TransactionStatus.class):(TransactionStatus) returnMap.get("defaultFilterStatus"):(TransactionStatus) returnMap.get("defaultFilterStatus");
					final ApprovalConfigAutoapproval filterApprovalConfig=!lstfilterDetail.isEmpty()?lstfilterDetail.get(0).containsKey("approvalConfigValue")?
							objMapper.convertValue(lstfilterDetail.get(0).get("approvalConfigValue"),ApprovalConfigAutoapproval.class):(ApprovalConfigAutoapproval) returnMap.get("defaultApprovalConfigVersion"):(ApprovalConfigAutoapproval) returnMap.get("defaultApprovalConfigVersion");			

					final Object filterTest=!lstfilterDetail.isEmpty()? lstfilterDetail.get(0).containsKey("testValue")?objMapper.convertValue(lstfilterDetail.get(0).get("testValue"),Object.class):(Object) returnMap.get("defaultTestvalues"):(Object) returnMap.get("defaultTestvalues");

					returnMap.put("realRegTypeValue", filterRegType);
					returnMap.put("realRegSubTypeValue", filterRegSubType);
					returnMap.put("realApproveConfigVersion", filterApprovalConfig);
					returnMap.put("realFilterStatusValue", filterTransactionStatus);
					returnMap.put("realTestcodeValue", filterTest);

					int ntestcode = (int) ((Map<String,Object>)((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0).get("testValue")).get("ntestcode");

					final String ntranscode = String.valueOf(((TransactionStatus)(returnMap.get("defaultFilterStatus"))).getNtransactionstatus());
					final short nsampletypecode = Short.parseShort(String.valueOf(filterSampleType.getNsampletypecode()));
					final short nregtypecode = Short.parseShort(String.valueOf(((RegistrationType)(returnMap.get("defaultRegistrationType"))).getNregtypecode()));
					final short nregsubtypecode = Short.parseShort(String.valueOf(((RegistrationSubType)(returnMap.get("defaultRegistrationSubType"))).getNregsubtypecode()));
					final short napprovalversioncode = Short.parseShort(String.valueOf(((ApprovalConfigAutoapproval)(returnMap.get("defaultApprovalConfigVersion"))).getNapprovalconfigversioncode()));
					final int nformcode = userInfo.getNformcode();


					final int analystCount = projectDAOSupport.getApprovalConfigVersionByUserRoleTemplate(napprovalversioncode,userInfo);

					if (analystCount > 0) {
						if (!inputMap.containsKey("nflag")) {
							inputMap.put("nflag", 1);
						}

						if(inputMap.containsKey("completeActionCall")) {
							returnMap.putAll(getResultEntrySamples(inputMap, userInfo));
						}

						if ((int) inputMap.get("nflag") == 1 ) {
							returnMap.putAll(getResultEntrySamples(inputMap, userInfo));
							int ndesigntemplatecode=(Integer) ((Map<String,Object>)((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0).get("designTemplateMappingValue")).get("ndesigntemplatemappingcode");


							returnMap.put("DynamicDesign",projectDAOSupport.getTemplateDesign(ndesigntemplatecode, userInfo.getNformcode()));
						}
						else if ((int) inputMap.get("nflag") == 2)
							returnMap.putAll(getResultEntrySubSamples(inputMap, userInfo));
						else if ((int) inputMap.get("nflag") == 3)
							returnMap.putAll(getResultEntryTest(inputMap, userInfo));
						List<Map<String, Object>> lstmapObjectsamples = objMapper.convertValue(returnMap.get("RE_SAMPLE"),
								new TypeReference<List<Map<String, Object>>>() {
						});

						if (lstmapObjectsamples != null && !lstmapObjectsamples.isEmpty()) {
							returnMap.put("RESelectedSample",
									Arrays.asList(lstmapObjectsamples.get(lstmapObjectsamples.size() - 1)));

						} else {
							if (lstmapObjectsamples != null) {
								returnMap.put("RESelectedSample", lstmapObjectsamples);
							}
							returnMap.put("RegistrationAttachment", lstmapObjectsamples);
							returnMap.put("SampleApprovalHistory", lstmapObjectsamples);
						}
						
						List<Map<String, Object>> lstmapObjectsubsample = objMapper.convertValue(returnMap.get("RE_SUBSAMPLE"),new TypeReference<List<Map<String, Object>>>() {
						});
						
						if ((boolean) inputMap.get("nneedsubsample")) {
							if (lstmapObjectsubsample != null && !lstmapObjectsubsample.isEmpty()) {
								returnMap.put("RESelectedSubSample",Arrays.asList(lstmapObjectsubsample.get(lstmapObjectsubsample.size() - 1)));
							}
						} else {
							returnMap.put("RESelectedSubSample", lstmapObjectsubsample);
						}
						
						returnMap.putAll(getActiveSampleTabData((String) inputMap.get("npreregno"),
								inputMap.containsKey("activeSampleKey") ? (String) inputMap.get("activeSampleKey") : "", userInfo));
						List<Map<String, Object>> lstmapObjecttest = objMapper.convertValue(returnMap.get("RE_TEST"),new TypeReference<List<Map<String, Object>>>() {
						});
						
						if (lstmapObjecttest != null && !lstmapObjecttest.isEmpty()) {
							returnMap.put("RESelectedTest", Arrays.asList(lstmapObjecttest.get(lstmapObjecttest.size() - 1)));
							returnMap.putAll(getActiveTestTabData(lstmapObjecttest.get(lstmapObjecttest.size() - 1).get("ntransactiontestcode").toString(),
									(String) inputMap.get("activeTestKey"), userInfo));
						} else {
							returnMap.put("RESelectedTest", new ArrayList<>());
							returnMap.put("TestParameters", "");
							returnMap.put("ResultUsedInstrument", "");
							returnMap.put("ResultUsedMaterial", "");
							returnMap.put("ResultUsedTasks", "");
							returnMap.put("RegistrationTestAttachment", "");
							returnMap.put("RegistrationTestComment", "");
							returnMap.put("ResultChangeHistory", "");
						}
					} else {
						returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),"IDS_USERNOTINRESULTENTRYFLOW");
						returnMap.put("RE_SAMPLE", new ArrayList<>());
						returnMap.put("RE_SUBSAMPLE", new ArrayList<>());
						returnMap.put("RE_TEST", new ArrayList<>());
						returnMap.put("RESelectedTest", new ArrayList<>());
						returnMap.put("TestParameters", "");
						returnMap.put("ResultUsedInstrument", "");
						returnMap.put("ResultUsedMaterial", "");
						returnMap.put("ResultUsedTasks", "");
						returnMap.put("RegistrationTestAttachment", "");
						returnMap.put("RegistrationTestComment", "");
						returnMap.put("ResultChangeHistory", "");
						return new ResponseEntity<>(returnMap, HttpStatus.CONFLICT);
					}

				}

				return new ResponseEntity<>(returnMap, HttpStatus.OK);
			}
			else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDFILTERALREADYLOADED",userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}
		}
		else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDFILTERISNOTPRESENT",userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}

	}
	//ALPD-4870-To insert data when filter name input submit,done by Dhanushya RI	
	@Override
	public ResponseEntity<Object> createFilterName(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();

		Map<String, Object> objMap = new HashMap<>();
		JSONObject jsonObject = new JSONObject();
		JSONObject jsonTempObject = new JSONObject();
		final List<FilterName> lstFilterByName = projectDAOSupport.getFilterListByName(inputMap.get("sfiltername").toString(), userInfo);
		if (lstFilterByName.isEmpty()) 
		{
			final String strValidationQuery="select json_agg(jsondata || jsontempdata) as jsondata from filtername where nformcode="+userInfo.getNformcode()+" and nusercode="+userInfo.getNusercode()+" "
					+ " and nuserrolecode="+userInfo.getNuserrole()+" and nsitecode="+userInfo.getNtranssitecode()+" "
					+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
					+ " and (jsondata->'nsampletypecode')::int="+inputMap.get("nsampletypecode")+" "  
					+ " and (jsondata->'nregtypecode')::int="+inputMap.get("nregtypecode")+" "  
					+ " and (jsondata->'nregsubtypecode')::int="+inputMap.get("nregsubtypecode")+" "  
					+ " and (jsontempdata->'ntranscode')::int="+inputMap.get("ntranscode")+" "  
					+ " and (jsontempdata->'napproveconfversioncode')::int="+inputMap.get("napproveconfversioncode")+" "
					+ " and (jsontempdata->'ndesigntemplatemappingcode')::int="+inputMap.get("ndesigntemplatemappingcode")+" " 
					+ " and (jsontempdata->>'DbFromDate')='"+inputMap.get("fromdate")+"' "
					+ " and (jsontempdata->>'DbToDate')='"+inputMap.get("todate")+"' " ; 


			final String strValidationFilter = jdbcTemplate.queryForObject(strValidationQuery, String.class);

			final List<Map<String, Object>> lstValidationFilter = strValidationFilter != null ? objMapper.readValue(strValidationFilter, new TypeReference<List<Map<String, Object>>>() {
			}):new ArrayList<Map<String, Object>>();
			if(lstValidationFilter.isEmpty()) {

				projectDAOSupport.createFilterData(inputMap,userInfo);
				final List<FilterName> lstFilterName=getFilterName(userInfo);
				objMap.put("FilterName",lstFilterName);
				return new ResponseEntity<Object>(objMap, HttpStatus.OK);
			} 
			else 
			{
				return new ResponseEntity<Object>(commonFunction.getMultilingualMessage("IDS_FILTERALREADYPRESENT",userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}
		}
		else {

			return new ResponseEntity<Object>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);

		}
	}
	//ALPD-4870-To show the previously saved filter name,done by Dhanushya RI

	@Override
	public List<FilterName> getFilterName( UserInfo userInfo) throws Exception {

		final String sFilterQry = "select * from filtername where nformcode="+userInfo.getNformcode()+" and nusercode="+userInfo.getNusercode()+""
				+ " and nuserrolecode="+userInfo.getNuserrole()+" and nsitecode="+userInfo.getNtranssitecode()+" "
				+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" order by 1 desc  ";

		return jdbcTemplate.query(sFilterQry, new FilterName());
	}
	//Added by sonia on 05th jan 2025 for jira id:ALPD-5174
	private static boolean isInteger(String str) {  
		try {        
			Integer.parseInt(str);
			return  true; 
		} catch (NumberFormatException e) {
			return false;
		} 
	}
	/*
	 * comment by vignesh R
	 * 	private Map<String, Object> getSDMSLabMaster(String ntransactiontestcode){
		Map<String,Object> map=new HashMap<>();
		String strQuery="select * from sdmslabsheetmaster where ntransactiontestcode in("+ntransactiontestcode+")";
		List<Map<String, Object>> list = jdbcTemplate.queryForList(strQuery);
		map.put("SdmsLabSheetMaster", list);
		return map;
	}*/

}