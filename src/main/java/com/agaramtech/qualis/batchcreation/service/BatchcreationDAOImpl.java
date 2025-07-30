package com.agaramtech.qualis.batchcreation.service;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.basemaster.model.TransactionStatus;
import com.agaramtech.qualis.batchcreation.model.BatchHistory;
import com.agaramtech.qualis.batchcreation.model.Batchmaster;
import com.agaramtech.qualis.batchcreation.model.Batchsample;
import com.agaramtech.qualis.batchcreation.model.BatchsampleIqc;
import com.agaramtech.qualis.batchcreation.model.SeqNoBatchcreation;
import com.agaramtech.qualis.compentencemanagement.model.TrainingCertification;
import com.agaramtech.qualis.configuration.model.ApprovalConfigAutoapproval;
import com.agaramtech.qualis.configuration.model.ApprovalConfigRole;
import com.agaramtech.qualis.configuration.model.DesignTemplateMapping;
import com.agaramtech.qualis.configuration.model.FilterName;
import com.agaramtech.qualis.configuration.model.SampleType;
import com.agaramtech.qualis.configuration.model.Settings;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationSubType;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationType;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.TestGroupCommonFunction;
import com.agaramtech.qualis.global.TransactionDAOSupport;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.instrumentmanagement.model.Instrument;
import com.agaramtech.qualis.instrumentmanagement.model.InstrumentCategory;
import com.agaramtech.qualis.materialmanagement.model.Material;
import com.agaramtech.qualis.materialmanagement.model.MaterialCategory;
import com.agaramtech.qualis.materialmanagement.model.MaterialInventory;
import com.agaramtech.qualis.materialmanagement.model.MaterialInventoryTrans;
import com.agaramtech.qualis.organization.model.Section;
import com.agaramtech.qualis.product.model.Product;
import com.agaramtech.qualis.product.model.ProductCategory;
import com.agaramtech.qualis.project.model.ProjectMaster;
import com.agaramtech.qualis.registration.model.Registration;
import com.agaramtech.qualis.registration.model.RegistrationSample;
import com.agaramtech.qualis.registration.model.RegistrationTest;
import com.agaramtech.qualis.registration.model.RegistrationTestHistory;
import com.agaramtech.qualis.registration.model.SeqNoArnoGenerator;
import com.agaramtech.qualis.registration.model.SeqNoRegistration;
import com.agaramtech.qualis.registration.model.TransactionValidation;
import com.agaramtech.qualis.registration.service.RegistrationDAO;
import com.agaramtech.qualis.testgroup.model.TestGroupTest;
import com.agaramtech.qualis.testgroup.model.TestGroupTestParameter;
import com.agaramtech.qualis.testgroup.model.TreeTemplateManipulation;
import com.agaramtech.qualis.testmanagement.model.TestInstrumentCategory;
import com.agaramtech.qualis.testmanagement.model.TestMaster;
import com.agaramtech.qualis.testmanagement.service.testmaster.TestMasterDAOImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.base.Joiner;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class BatchcreationDAOImpl implements BatchcreationDAO {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TestMasterDAOImpl.class);
	  
	  private final StringUtilityFunction stringUtilityFunction; 
	  private final CommonFunction commonFunction; 
	  private final JdbcTemplate jdbcTemplate; 
	  private final JdbcTemplateUtilityFunction jdbcUtilityFunction; 
	  private final ProjectDAOSupport projectDAOSupport; 
	  private final DateTimeUtilityFunction dateUtilityFunction; 
	  private final AuditUtilityFunction auditUtilityFunction;
	  private final TestGroupCommonFunction objTestGroupCommonFunction;
	  private final RegistrationDAO registrationDAO;
	  private final TransactionDAOSupport transactionDAOSupport;
	  
	  
	 
		@Override
		public ResponseEntity<Object> getBatchcreation(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
			final Map<String, Object> returnMap = new HashMap<>();
			ObjectMapper objMapper = new ObjectMapper();
			String dfromdate = "";
			String dtodate = "";
			
			//ALPD-4999-To get previously save filter details when initial get,done by Dhanushya RI
			final String strQuery="select json_agg(jsondata || jsontempdata) as jsondata from filterdetail where nformcode="+userInfo.getNformcode()+" and nusercode="+userInfo.getNusercode()+" "
	        		+ " and nuserrolecode="+userInfo.getNuserrole()+" and nsitecode="+userInfo.getNtranssitecode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	    	
			final String strFilter = jdbcTemplate.queryForObject(strQuery, String.class);
			LOGGER.info("strFilter:"+ strFilter);
			
	    	
			final List<Map<String, Object>> lstfilterDetail = strFilter != null ? objMapper.readValue(strFilter, new TypeReference<List<Map<String, Object>>>() {
				}):new ArrayList<Map<String, Object>>();
			
			if(!lstfilterDetail.isEmpty() && lstfilterDetail.get(0).containsKey("fromDate") && lstfilterDetail.get(0).containsKey("toDate") ){
				Instant instantFromDate = dateUtilityFunction.convertStringDateToUTC(lstfilterDetail.get(0).get("fromDate").toString(),userInfo, true);
				Instant instantToDate = dateUtilityFunction.convertStringDateToUTC(lstfilterDetail.get(0).get("toDate").toString(),userInfo, true);
				
				dfromdate = dateUtilityFunction.instantDateToString(instantFromDate);
				dtodate =dateUtilityFunction.instantDateToString(instantToDate);

				
				final DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
				final LocalDateTime ldt = LocalDateTime.parse(dfromdate, formatter1);
				final LocalDateTime ldt1 = LocalDateTime.parse(dtodate, formatter1);

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
				returnMap.put("realFromDate", formattedFromString);
				returnMap.put("realToDate", formattedToString);
				
			}else {
				final Map<String, Object> mapObject = projectDAOSupport.getDateFromControlProperties(userInfo,
						(String) inputMap.get("currentdate"), "datetime", "FromDate");
				dfromdate = (String) mapObject.get("FromDate");
				dtodate = (String) mapObject.get("ToDate");
				returnMap.put("fromDate", mapObject.get("FromDateWOUTC"));
				returnMap.put("toDate", mapObject.get("ToDateWOUTC"));
				returnMap.put("realFromDate", mapObject.get("FromDateWOUTC"));
				returnMap.put("realToDate", mapObject.get("ToDateWOUTC"));
			}
			

			inputMap.put("fromDate", dfromdate);
			inputMap.put("toDate", dtodate);
			inputMap.put("ntransactiontestcode", 0);
			// inputMap.put("checkBoxOperation", 3);

			final String getSampleType = "select st.nsampletypecode,coalesce(st.jsondata->'sampletypename'->>'"
					+ userInfo.getSlanguagetypecode() + "',st.jsondata->'sampletypename'->>'en-US') ssampletypename"
					+ " from sampletype st,approvalconfig ac,approvalconfigversion acv,registrationtype rt,registrationsubtype rst"
					+ " where acv.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
					+ " and acv.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ac.napprovalconfigcode = acv.napprovalconfigcode " + " and ac.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and rt.nregtypecode = ac.nregtypecode " + " and rt.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and rt.nregtypecode = rst.nregtypecode and rst.nregsubtypecode = ac.nregsubtypecode"
					+ " and rst.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and rt.nsampletypecode = st.nsampletypecode " + " and acv.nsitecode = "
					+ userInfo.getNmastersitecode() + " and st.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and st.nsampletypecode > 0 "
					+ " group by st.nsampletypecode,st.jsondata->'sampletypename'->>'" + userInfo.getSlanguagetypecode()
					+ "'" + " order by st.nsorter";

			final List<SampleType> lstsampleType = jdbcTemplate.query(getSampleType, new SampleType());
			if (!lstsampleType.isEmpty()) {
				List<FilterName> lstFilterName=getFilterName(userInfo);
				final SampleType filterSampleType=!lstfilterDetail.isEmpty()?objMapper.convertValue(lstfilterDetail.get(0).get("sampleTypeValue"),SampleType.class):lstsampleType.get(0);
				int nsampletypecode = filterSampleType.getNsampletypecode();
				returnMap.put("SampleType", lstsampleType);
				returnMap.put("realSampleTypeList", lstsampleType);		
				inputMap.put("nsampletypecode", nsampletypecode);
				returnMap.put("realSampleTypeValue", filterSampleType);
				returnMap.put("defaultSampleType", filterSampleType);
				inputMap.put("defaultSampleType", filterSampleType);
				inputMap.put("filterDetailValue", lstfilterDetail);
				returnMap.putAll((Map<String, Object>) getRegistrationType(inputMap, userInfo).getBody());
				returnMap.put("FilterName",lstFilterName);

			}

			return new ResponseEntity<>(returnMap, HttpStatus.OK);

		}

	  
		@Override
		public ResponseEntity<Object> getRegistrationType(Map<String, Object> inputMap, final UserInfo userInfo)
				throws Exception {
			final Map<String, Object> returnMap = new HashMap<>();
			ObjectMapper objMapper = new ObjectMapper();

			String str = "Select * from sampletype  where  nsampletypecode=" + inputMap.get("nsampletypecode");

			SampleType obj = (SampleType) jdbcUtilityFunction.queryForObject(str, SampleType.class,jdbcTemplate); 
			
			String ValidationQuery = "";
			if (obj.getNtransfiltertypecode() != -1 && userInfo.getNusercode() != -1) {
				int nmappingfieldcode = (obj.getNtransfiltertypecode() == 1) ? userInfo.getNdeptcode()
						: userInfo.getNuserrole();
				ValidationQuery = " and rst.nregsubtypecode in( " + "		SELECT rs.nregsubtypecode "
						+ "		FROM registrationsubtype rs "
						+ "		INNER JOIN transactionfiltertypeconfig ttc ON rs.nregsubtypecode = ttc.nregsubtypecode "
						+ "		LEFT JOIN transactionusers tu ON tu.ntransfiltertypeconfigcode = ttc.ntransfiltertypeconfigcode "
						+ "		WHERE ( ttc.nneedalluser = "+Enumeration.TransactionStatus.YES.gettransactionstatus()+"  and ttc.nstatus =  "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND ttc.nmappingfieldcode = "
						+ nmappingfieldcode + ") " + "	 OR " + "	( ttc.nneedalluser = "+Enumeration.TransactionStatus.NO.gettransactionstatus()+""
						+ "	  AND ttc.nmappingfieldcode =" + nmappingfieldcode + "	  AND tu.nusercode ="
						+ userInfo.getNusercode() + "   and ttc.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "   and tu.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") " + "  OR "
						+ "	( ttc.nneedalluser = "+Enumeration.TransactionStatus.NO.gettransactionstatus()+"  " + " AND ttc.nmappingfieldcode = -1 " + " AND tu.nusercode ="
						+ userInfo.getNusercode() + " and ttc.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tu.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") "
						+ "	 AND rs.nregtypecode = rt.nregtypecode) ";
			}

			final String getRegType = "select rt.nregtypecode,coalesce(rt.jsondata->'sregtypename'->>'"
					+ userInfo.getSlanguagetypecode() + "',rt.jsondata->'sregtypename'->>'en-US') as sregtypename "
					+ " from approvalconfig ac,approvalconfigversion acv,registrationtype rt,registrationsubtype rst"
					+ " where acv.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
					+ " and ac.napprovalconfigcode = acv.napprovalconfigcode and rt.nregtypecode = ac.nregtypecode"
					+ " and rt.nregtypecode = rst.nregtypecode and rst.nregsubtypecode = ac.nregsubtypecode"
					+ " and acv.nsitecode = " + userInfo.getNmastersitecode() + "" + " and rt.nsitecode = "
					+ userInfo.getNmastersitecode() + "" + " and rst.nsitecode = " + userInfo.getNmastersitecode() + ""
					+ " and rt.nsampletypecode = " + inputMap.get("nsampletypecode") + " and acv.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rst.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ac.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nregtypecode > 0 "
					+ ValidationQuery + " group by rt.nregtypecode,coalesce(rt.jsondata->'sregtypename'->>'"
					+ userInfo.getSlanguagetypecode()
					+ "',rt.jsondata->'sregtypename'->>'en-US')  order by rt.nregtypecode desc;";

			final List<RegistrationType> regTypeList = jdbcTemplate.query(getRegType, new RegistrationType());

			if (!regTypeList.isEmpty()) {
				// returnMap.put("realRegTypeValue", regTypeList.get(0));
				//ALPD-4999-To get previously save filter details when initial get,done by Dhanushya RI
				final RegistrationType filterRegType=inputMap.containsKey("filterDetailValue") && !((List<Map<String, Object>>) inputMap.get("filterDetailValue")).isEmpty()
						&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0).containsKey("regTypeValue")
						                            ? objMapper.convertValue(((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0).get("regTypeValue"),RegistrationType.class) :regTypeList.get(0);
				returnMap.put("RegistrationType", regTypeList);
				returnMap.put("realRegistrationTypeList", regTypeList);
				inputMap.put("defaultRegistrationType", filterRegType);
				inputMap.put("nregtypecode", filterRegType.getNregtypecode());
				returnMap.putAll((Map<String, Object>) getRegistrationsubType(inputMap, userInfo).getBody());
				if (returnMap.containsKey("rtn")) {
					return new ResponseEntity<>(returnMap.get("rtn"), HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				returnMap.put("defaultRegistrationType", null);
				// returnMap.put("realRegTypeValue", regTypeList.get(0));
				returnMap.put("RegistrationType", regTypeList);

			}
			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		}

		@Override
		@SuppressWarnings("unchecked")
		public ResponseEntity<Object> getRegistrationsubType(Map<String, Object> inputMap, final UserInfo userInfo)
				throws Exception {
			final Map<String, Object> returnMap = new HashMap<>();
			ObjectMapper objMapper = new ObjectMapper();
			//List<ProjectMember> listProjectMember = new ArrayList<>();
			//List<ReactRegistrationTemplate> listReactRegistrationTemplate = new ArrayList<ReactRegistrationTemplate>();
			String str = "Select * from registrationtype rt,sampletype st where rt.nsampletypecode=st.nsampletypecode"
					+ " and rt.nregTypeCode="+ inputMap.get("nregtypecode")+""
					+ " and rt.nsitecode="+userInfo.getNmastersitecode()+""
					+ " and rt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
					+ " and st.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";

			SampleType obj = (SampleType) jdbcUtilityFunction.queryForObject(str, SampleType.class,jdbcTemplate); 
			String validationQuery = "";
			if (obj.getNtransfiltertypecode() != -1 && userInfo.getNusercode() != -1) {
				int nmappingfieldcode = (obj.getNtransfiltertypecode() == 1) ? userInfo.getNdeptcode()
						: userInfo.getNuserrole();
				validationQuery = " and rst.nregsubtypecode in( " + " SELECT rs.nregsubtypecode "
						+ " FROM registrationsubtype rs "
						+ " INNER JOIN transactionfiltertypeconfig ttc ON rs.nregsubtypecode = ttc.nregsubtypecode "
						+ " LEFT JOIN transactionusers tu ON tu.ntransfiltertypeconfigcode = ttc.ntransfiltertypeconfigcode "
						+ " WHERE ( ttc.nneedalluser = "+Enumeration.TransactionStatus.YES.gettransactionstatus()+"  "
						+ " and ttc.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ttc.nsitecode="+userInfo.getNmastersitecode()+" AND ttc.nmappingfieldcode = "
						+ nmappingfieldcode + ")" + "  OR " + "	( ttc.nneedalluser = "+Enumeration.TransactionStatus.NO.gettransactionstatus()+"" + " AND ttc.nmappingfieldcode ="
						+ nmappingfieldcode + " AND tu.nusercode =" + userInfo.getNusercode() + " and ttc.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ttc.nsitecode="+userInfo.getNmastersitecode()+" and tu.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tu.nsitecode="+userInfo.getNmastersitecode()+") " + "  OR "
						+ "	( ttc.nneedalluser = "+Enumeration.TransactionStatus.NO.gettransactionstatus()+" " + " AND ttc.nmappingfieldcode = -1 " + " AND tu.nusercode ="
						+ userInfo.getNusercode() + " and ttc.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tu.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ttc.nsitecode="+userInfo.getNmastersitecode()+" "
						+ " and tu.nsitecode="+userInfo.getNmastersitecode()+") " + " AND rs.nregtypecode = "
						+ inputMap.get("nregtypecode") + ")";
			}

			final String getRegSubType = "select rst.nregsubtypecode,rst.nregtypecode,rsc.nregsubtypeversioncode,"
					+ " coalesce(rst.jsondata->'sregsubtypename'->>'" + userInfo.getSlanguagetypecode()
					+ "',rst.jsondata->'sregsubtypename'->>'en-US') as sregsubtypename,"
					+ " cast(rsc.jsondata->>'nneedsubsample' as boolean) nneedsubsample,"
					+ " cast(rsc.jsondata->>'nneedtemplatebasedflow' as boolean) nneedtemplatebasedflow,"
					+ " cast(rsc.jsondata->>'nneedtestinitiate' as boolean) nneedtestinitiate,"
					+ " cast(rsc.jsondata->>'nneedmyjob' as boolean) nneedmyjob, "
					+ " case when cast(rsc.jsondata ->> 'ntestgroupspecrequired' as boolean) is null then true else cast(rsc.jsondata ->> 'ntestgroupspecrequired' as boolean) "
					+ " end as  ntestgroupspecrequired,"
					+ " cast(rsc.jsondata->>'nneedjoballocation' as boolean) nneedjoballocation "
					+ " from approvalconfig ac,approvalconfigversion acv,registrationsubtype rst,regsubtypeconfigversion rsc"
					+ " where acv.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
					+ " and rsc.napprovalconfigcode = ac.napprovalconfigcode and rsc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rsc.ntransactionstatus = "
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
					+ " and ac.napprovalconfigcode = acv.napprovalconfigcode and rst.nregtypecode = "
					+ inputMap.get("nregtypecode") + " and ac.napprovalconfigcode = acv.napprovalconfigcode "
					+ " and rst.nregsubtypecode = ac.nregsubtypecode and (rsc.jsondata->'nneedbatch')::jsonb ='true'"
					+ " and rst.nregsubtypecode > 0 " + "" + " and acv.nsitecode=" + userInfo.getNmastersitecode() + ""
					+ " and rst.nsitecode=" + userInfo.getNmastersitecode() + "" + " and rsc.nsitecode="
					+ userInfo.getNmastersitecode() + "" + " and rsc.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and acv.nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rst.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ac.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " group by rst.nregsubtypecode,rst.nregtypecode,rst.sregsubtypename "
					+ validationQuery + " order by rst.nregsubtypecode desc";

			List<RegistrationSubType> regSubTypeList = jdbcTemplate.query(getRegSubType, new RegistrationSubType());

			if (!regSubTypeList.isEmpty()) {
				//ALPD-4999-To get previously save filter details when initial get,done by Dhanushya RI
				final RegistrationSubType filterRegSubType=inputMap.containsKey("filterDetailValue") && !((List<Map<String, Object>>) inputMap.get("filterDetailValue")).isEmpty()
						&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0).containsKey("regSubTypeValue")
	                    ? objMapper.convertValue(((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0).get("regSubTypeValue"),RegistrationSubType.class) :regSubTypeList.get(0);
				
				returnMap.put("defaultRegistrationSubType", filterRegSubType);
				returnMap.put("defaultRegistrationType", inputMap.get("defaultRegistrationType"));
				returnMap.put("realRegSubTypeValue", filterRegSubType);
				returnMap.put("RegistrationSubType", regSubTypeList);
				returnMap.put("realRegistrationSubTypeList", regSubTypeList);
				returnMap.put("nneedtestinitiate", filterRegSubType.isNneedtestinitiate());
				returnMap.put("nregsubtypeversioncode", filterRegSubType.getNregsubtypeversioncode());
				returnMap.put("realRegTypeValue", inputMap.get("defaultRegistrationType"));

				inputMap.put("nregsubtypecode", filterRegSubType.getNregsubtypecode());
				inputMap.put("nneedsubsample", filterRegSubType.isNneedsubsample());
				inputMap.put("nneedtemplatebasedflow", filterRegSubType.isNneedtemplatebasedflow());
				inputMap.put("defaultRegistrationSubType", filterRegSubType);
				inputMap.put("nneedtemplatebasedflow", filterRegSubType.isNneedtemplatebasedflow());
				inputMap.put("nneedtestinitiate", filterRegSubType.isNneedtestinitiate());


				if ((inputMap.get("needFilterSubmit") == "true" || inputMap.get("needFilterSubmit") == null)) {
					returnMap.putAll((Map<String, Object>) projectDAOSupport.getReactRegistrationTemplateList(
							Short.valueOf(inputMap.get("nregtypecode").toString()),
							Short.valueOf(inputMap.get("nregsubtypecode").toString()),
							(boolean) inputMap.get("nneedtemplatebasedflow"),userInfo).getBody());
					inputMap.put("ndesigntemplatemappingcode", returnMap.get("ndesigntemplatemappingcode"));
					
					returnMap.put("DynamicDesign",
							projectDAOSupport.getTemplateDesign((int) returnMap.get("ndesigntemplatemappingcode"), userInfo.getNformcode()));
				}
			
				returnMap.putAll((Map<String, Object>) getApprovalConfigVersion(inputMap, userInfo).getBody());

			} else {
				returnMap.put("defaultRegistrationSubType", null);
				returnMap.put("RegistrationSubType", regSubTypeList);
				returnMap.put("realRegistrationSubTypeList", regSubTypeList);
				returnMap.put("realRegTypeValue", inputMap.get("defaultRegistrationType"));
				returnMap.put("defaultRegistrationType", inputMap.get("defaultRegistrationType"));
				returnMap.put("nneedtestinitiate", null);
				returnMap.put("nregsubtypeversioncode", null);
				returnMap.put("defaultFilterStatus", null);
				returnMap.put("BCFilterStatus", null);
				returnMap.put("ApprovalConfigVersion", null);
				returnMap.put("defaultApprovalConfigVersion", null);
			}
			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		}

		
		@Override
		public ResponseEntity<Object> getApprovalConfigVersion(Map<String, Object> inputMap, UserInfo userInfo)
				throws Exception {
			final Map<String, Object> returnMap = new HashMap<>();
			ObjectMapper objMapper = new ObjectMapper();

			

			if (inputMap.containsKey("fromDate")) {
				String fromDate = "";
				String toDate = "";

				final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
				final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());

				final String fromDateUI = LocalDateTime.parse((String) inputMap.get("fromDate"), dbPattern)
						.format(uiPattern);
				final String toDateUI = LocalDateTime.parse((String) inputMap.get("toDate"), dbPattern).format(uiPattern);

				returnMap.put("realFromDate", fromDateUI);
				returnMap.put("realToDate", toDateUI);

				fromDate = dateUtilityFunction.instantDateToString(
						dateUtilityFunction.convertStringDateToUTC((String) inputMap.get("fromDate"), userInfo, true));
				toDate = dateUtilityFunction.instantDateToString(
						dateUtilityFunction.convertStringDateToUTC((String) inputMap.get("toDate"), userInfo, true));
				fromDate = fromDate.replace("T", " ");
				toDate = toDate.replace("T", " ");
				returnMap.put("fromdate", fromDate);
				returnMap.put("todate", toDate);

			}
			final String getApprovalConfigVersion = "select aca.sversionname,aca.napprovalconfigcode,aca.napprovalconfigversioncode ,acv.ntransactionstatus,acv.ntreeversiontempcode "
					+ " from registration r,registrationarno rar,registrationhistory rh,approvalconfigautoapproval aca,"
					+ " approvalconfig ac,approvalconfigversion acv "
					+ "    where r.npreregno=rar.npreregno and r.npreregno=rh.npreregno"
					+ " and rar.napprovalversioncode=acv.napproveconfversioncode"
					+ " and acv.napproveconfversioncode=aca.napprovalconfigversioncode"
					+ " and r.nregtypecode=ac.nregtypecode and r.nregsubtypecode=ac.nregsubtypecode"
					+ " and r.nregtypecode =" + inputMap.get("nregtypecode") + " and r.nregsubtypecode ="
					+ inputMap.get("nregsubtypecode") + " and rh.ntransactionstatus = "
					+ Enumeration.TransactionStatus.REGISTERED.gettransactionstatus() + " and acv.nsitecode ="
					+ userInfo.getNmastersitecode() + " and rh.dtransactiondate between '" + inputMap.get("fromDate") + "'"
					+ " and '" + inputMap.get("toDate") + "'" + " and r.nsitecode=" + userInfo.getNtranssitecode() + ""
					+ " and rar.nsitecode =" + userInfo.getNtranssitecode() + "" + " and rh.nsitecode ="
					+ userInfo.getNtranssitecode() + "" + " and r.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rar.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rh.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ac.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ac.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acv.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and aca.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and aca.nsitecode="+userInfo.getNmastersitecode()+""
					+ " group by acv.ntransactionstatus,aca.napprovalconfigversioncode,acv.ntreeversiontempcode,aca.napprovalconfigcode,aca.sversionname";

//			final List<ApprovalConfigAutoapproval> lstApprovalConfigVersion = jdbcTemplate
//					.query(getApprovalConfigVersion, new ApprovalConfigAutoapproval());

			final List<ApprovalConfigAutoapproval> approvalVersion = jdbcTemplate.query(getApprovalConfigVersion,
					new ApprovalConfigAutoapproval());

			if (!approvalVersion.isEmpty()) {
				List<ApprovalConfigAutoapproval> approvedApprovalVersionList = approvalVersion.stream().filter(obj -> obj
						.getNtransactionstatus() == Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
						|| obj.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED.gettransactionstatus())
						.collect(Collectors.toList());
				
//				ALPD-5809	Added stream to filter approved approval config version first or else it will return retired approval version by Vishakh
				ApprovalConfigAutoapproval approvedFirstElseRetiredApprovalConfig = approvedApprovalVersionList.stream()
						.filter(map -> map.getNtransactionstatus() == Enumeration.TransactionStatus.APPROVED.gettransactionstatus())
						.findFirst().orElseGet(() -> approvedApprovalVersionList.stream()
								.filter(map1 -> map1.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED.gettransactionstatus())
								.findFirst().orElse(null));
				
				//ALPD-4999-To get previously save filter details when initial get,done by Dhanushya RI
				final ApprovalConfigAutoapproval filterApprovalConfig=inputMap.containsKey("filterDetailValue") && !((List<Map<String, Object>>) inputMap.get("filterDetailValue")).isEmpty()
						&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0).containsKey("approvalConfigValue")
	                    ? objMapper.convertValue(((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0).get("approvalConfigValue"),ApprovalConfigAutoapproval.class) 
//	                    		:approvedApprovalVersionList.get(0)
	                    		: approvedFirstElseRetiredApprovalConfig;

				final DesignTemplateMapping designTemplateMapping = projectDAOSupport.getApprovedRegistrationTemplate(
						Short.valueOf(inputMap.get("nregtypecode").toString()),
						Short.valueOf(inputMap.get("nregsubtypecode").toString()),
						filterApprovalConfig.getNapprovalconfigversioncode());

				if (designTemplateMapping != null) {
					inputMap.put("ndesigntemplatemappingcode", designTemplateMapping.getNdesigntemplatemappingcode());
					returnMap.put("ndesigntemplatemappingcode", designTemplateMapping.getNdesigntemplatemappingcode());
					// returnMap.put("realndesigntemplatemappingcode", designTemplateMapping.getNdesigntemplatemappingcode());
				}

				if (approvedApprovalVersionList != null && !approvedApprovalVersionList.isEmpty()) {
					
					inputMap.put("napprovalconfigcode", filterApprovalConfig.getNapprovalconfigcode());
					inputMap.put("napprovalversioncode",
							filterApprovalConfig.getNapprovalconfigversioncode());
					inputMap.put("defaultApprovalConfigVersion", filterApprovalConfig);
					returnMap.put("defaultApprovalConfigVersion", filterApprovalConfig);
					returnMap.put("realApproveConfigVersion", filterApprovalConfig);
				} else {
					//ALPD-4999-To get previously save filter details when initial get,done by Dhanushya RI

					final ApprovalConfigAutoapproval filterApprovalVersionConfig=inputMap.containsKey("filterDetailValue") && !((List<Map<String, Object>>) inputMap.get("filterDetailValue")).isEmpty()
							&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0).containsKey("approvalConfigValue")
		                    ? objMapper.convertValue(((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0).get("approvalConfigValue"),ApprovalConfigAutoapproval.class) :approvalVersion.get(0);

					inputMap.put("defaultApprovalConfigVersion", filterApprovalVersionConfig);
					inputMap.put("napprovalversioncode", filterApprovalVersionConfig.getNapprovalconfigversioncode());
					returnMap.put("realApproveConfigVersion", filterApprovalVersionConfig);
					returnMap.put("defaultApprovalConfigVersion", filterApprovalVersionConfig);
				}

				final Integer count = projectDAOSupport.getApprovalConfigVersionByUserRoleTemplate(
						filterApprovalConfig.getNapprovalconfigversioncode(), userInfo);
				if (count == 0) {
					returnMap.put(Enumeration.ReturnStatus.SUCCESS.getreturnstatus(), (commonFunction
							.getMultilingualMessage("IDS_USERNOTINRESULTENTRYFLOW", userInfo.getSlanguagefilename())));
					returnMap.put("defaultFilterStatus", null);
					returnMap.put("BCFilterStatus", new ArrayList<Object>());
					returnMap.put("defaultApprovalConfigVersion", null);
					returnMap.put("ApprovalConfigVersion", new ArrayList<Object>());
					return new ResponseEntity<>(returnMap, HttpStatus.OK);
				}
			} else {
				returnMap.put("defaultApprovalConfigVersion", null);
				inputMap.put("napprovalconfigcode", Enumeration.TransactionStatus.NA.gettransactionstatus());
				inputMap.put("napprovalversioncode", Enumeration.TransactionStatus.NA.gettransactionstatus());

			}
			returnMap.put("realRegSubTypeValue", inputMap.get("defaultRegistrationSubType"));
			returnMap.put("ApprovalConfigVersion", approvalVersion);
			returnMap.put("realApprovalConfigVersionList", approvalVersion);
			boolean sneedbatchfilter;
			if (inputMap.containsKey("isneedapprovalfilter")) {
				sneedbatchfilter = true;
				returnMap.put("defaultRegistrationSubType", inputMap.get("defaultRegistrationSubType"));
			} else {
				sneedbatchfilter = false;
			}
			returnMap.putAll((Map<String, Object>) getFilterStatus(inputMap, userInfo).getBody());
			if ((inputMap.get("needFilterSubmit") == "true" || inputMap.get("needFilterSubmit") == null)) {

				returnMap.putAll((Map<String, Object>) getBatchmaster(inputMap, userInfo).getBody());
			}
			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		}

		@Override
		@SuppressWarnings("unchecked")
		public ResponseEntity<Object> getFilterStatus(Map<String, Object> inputMap, final UserInfo userInfo)
				throws Exception {
			Map<String, Object> returnMap = new HashMap<>();
			ObjectMapper objMapper = new ObjectMapper();
			List<TransactionStatus> lstfilterdetails = new ArrayList<>();
			//final Map<String, Object> obj = new HashMap<String, Object>();
			//List<Object> lstobj = new ArrayList<Object>();
			String isneedrealFilterStatus = (String) inputMap.get("isneedrealFilterStatus");

			String strQuery = "";

			final Short regTypeCode = ((Object) inputMap.get("nregtypecode")).getClass().getSimpleName()
					.equalsIgnoreCase("Integer") ? ((Integer) inputMap.get("nregtypecode")).shortValue()
							: (Short) inputMap.get("nregtypecode");
			final Short regSubTypeCode = ((Object) inputMap.get("nregsubtypecode")).getClass().getSimpleName()
					.equalsIgnoreCase("Integer") ? ((Integer) inputMap.get("nregsubtypecode")).shortValue()
							: (Short) inputMap.get("nregsubtypecode");
			;

			if (inputMap.containsKey("napprovalversioncode")) {
				strQuery = "select sc.ntranscode as ntransactionstatus,ts.jsondata->'stransdisplaystatus'->>'"
						+ userInfo.getSlanguagetypecode() + "' sfilterstatus,sc.nsorter "
						+ " from approvalstatusconfig sc,transactionstatus ts" + " where ts.ntranscode = sc.ntranscode "
						+ " and sc.nformcode = " + Enumeration.FormCode.BATCHCREATION.getFormCode() + " and sc.nsitecode = "
						+ userInfo.getNmastersitecode() + " and sc.nsitecode= "+userInfo.getNmastersitecode()+" and sc.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and ts.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sc.nregtypecode="
						+ regTypeCode + " and sc.nregsubtypecode=" + regSubTypeCode + " order by sc.nsorter asc";
				lstfilterdetails = jdbcTemplate.query(strQuery, new TransactionStatus());
			}

			if (!lstfilterdetails.isEmpty()) {
				//ALPD-4999-To get previously save filter details when initial get,done by Dhanushya RI

				final TransactionStatus filterTransactionStatus=inputMap.containsKey("filterDetailValue") && !((List<Map<String, Object>>) inputMap.get("filterDetailValue")).isEmpty()
						&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0).containsKey("filterStatusValue")
	                    ? (TransactionStatus) objMapper.convertValue(((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0).get("filterStatusValue"),TransactionStatus.class) :lstfilterdetails.get(0);

				returnMap.put("defaultFilterStatus", filterTransactionStatus);
				returnMap.put("realdefaultFilterStatus", filterTransactionStatus);
				inputMap.put("defaultFilterStatus", filterTransactionStatus);
				inputMap.put("ntranscode", String.valueOf(filterTransactionStatus.getNtransactionstatus()));
				inputMap.put("allTranscode",lstfilterdetails.stream().map(x -> String.valueOf(x.getNtransactionstatus())).collect(Collectors.joining(",")));
				if (isneedrealFilterStatus == null && lstfilterdetails.size() > 0) {
					returnMap.put("realdefaultFilterStatus", filterTransactionStatus);
				}
			} else {
				returnMap.put("defaultFilterStatus", null);
				inputMap.put("ntranscode", String.valueOf(Enumeration.TransactionStatus.NA.gettransactionstatus()));
			}

			
			returnMap.put("BCFilterStatus", lstfilterdetails);
			returnMap.put("realBCFilterStatusList", lstfilterdetails);
			// returnMap.putAll((Map<String, Object>) getTestBasedOnCombo(inputMap,
			// userInfo).getBody());
			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		}

	
		@Override
		public ResponseEntity<Object> getTestBasedOnCombo(Map<String, Object> inputMap, final UserInfo userInfo)
				throws Exception {
			final Map<String, Object> returnMap = new HashMap<>();
			List<RegistrationTest> lsttestvalues = new ArrayList<>();
			//List<Registration> lstregtvalues = new ArrayList<>();
			//final Map<String, Object> map = new HashMap<>();
			//final List<Object> lstobj = new ArrayList<>();
			ObjectMapper objmap = new ObjectMapper();
			RegistrationTest objSection = objmap.convertValue(inputMap.get("section"),
					new TypeReference<RegistrationTest>() {
					});
			int naddcontrolCode = 0;
			if (inputMap.containsKey("naddcontrolCode")) {
				naddcontrolCode = (int) inputMap.get("naddcontrolCode");
			}
			int nregtypecode = (int) inputMap.get("nregtypecode");
			//int nsampletypecode = (int) inputMap.get("nsampletypecode");
			int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
			//boolean nneedmyjob = (boolean) inputMap.get("nneedmyjob");
			boolean nneedjoballocation=(boolean) inputMap.get("nneedjoballocation");
			List<TransactionValidation> validationStatusLst = (List<TransactionValidation>) getTransactionValidation(
					naddcontrolCode, nregtypecode, nregsubtypecode, userInfo);

//			final String getValidationStatus = "select transactionvalidation.ntransactionstatus from transactionvalidation where "
//		                + " transactionvalidation.nregtypecode="+inputMap.get("nregtypecode") +" and transactionvalidation.nregsubtypecode="+inputMap.get("nregsubtypecode")
//		                + " and transactionvalidation.ncontrolcode="+inputMap.get("naddcontrolCode")
//		                + " and transactionvalidation.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//			
//		      List<TransactionStatus> validationStatusList = getJdbcTemplate().query(getValidationStatus,
//		                new TransactionStatus());

			final String stransactionStatus = validationStatusLst.stream()
					.map(objpreregno -> String.valueOf(objpreregno.getNtransactionstatus()))
					.collect(Collectors.joining(","));
//			String toCheckAccepted = "";
//			if (nneedmyjob) {
//				toCheckAccepted = " and rth.nusercode = " + userInfo.getNusercode() + "";
//			}

			if (!stransactionStatus.isEmpty()) {
				
				
				
				/*final String getTest = "select tm.ntestcode,tm.stestname,max(tgt.ntestgrouptestcode) ntestgrouptestcode,"
						+ " max(tgt.nmethodcode) nmethodcode ,max(tgt.nspecsampletypecode) nspecsampletypecode,"
						+ " max(tgtp.nchecklistversioncode)nchecklistversioncode,max(r.nprojectmastercode)nprojectmastercode,"
						+ " max(tgsst.nspecsampletypecode) nspecsampletypecode,max(tgsst.nallottedspeccode) nallottedspeccode,"
						+ " max(tgsst.ncomponentcode) ncomponentcode from registration r,joballocation jb,"
						+ " registrationtesthistory rth,registrationtest rt,testmaster tm,testgrouptest tgt,"
						+ " testgrouptestparameter tgtp,testgroupspecsampletype tgsst "
						+ " where rth.ntransactiontestcode=rt.ntransactiontestcode " + " and rt.npreregno=r.npreregno"
						+ " and jb.npreregno=r.npreregno" + " and jb.ntransactiontestcode=rt.ntransactiontestcode"
						+ " and jb.nsectioncode=rt.nsectioncode" + " and jb.nusercode in(" + userInfo.getNusercode()
						+ ",-1)" + " and r.nregtypecode = " + nregtypecode + "" + " and r.nregsubtypecode = "
						+ nregsubtypecode + "" + " and rt.ntestcode=tm.ntestcode and tgt.ntestcode=tm.ntestcode "
						+ " and tgt.ntestgrouptestcode=rt.ntestgrouptestcode "
						+ " and tgsst.nspecsampletypecode=tgt.nspecsampletypecode "
						+ " and tgtp.ntestgrouptestcode=tgt.ntestgrouptestcode" + " and rt.nsectioncode = "
						+ objSection.getNsectioncode() + "" + " and r.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and jb.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rth.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rt.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and tm.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and tgt.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and tgtp.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and tgsst.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and r.nsitecode="
						+ userInfo.getNtranssitecode() + "" + " and rth.nsitecode=" + userInfo.getNtranssitecode() + ""
						+ " and rt.nsitecode=" + userInfo.getNtranssitecode() + "" + " and jb.nsitecode="
						+ userInfo.getNtranssitecode() + "" + " and tm.nsitecode=" + userInfo.getNmastersitecode() + ""
						+ " and tgt.nsitecode=" + userInfo.getNmastersitecode() + "" + " and tgtp.nsitecode="
						+ userInfo.getNmastersitecode() + "" + " and tgsst.nsitecode=" + userInfo.getNmastersitecode() + ""
						+ " and rth.ntesthistorycode = any (select max(rth.ntesthistorycode)from registrationtesthistory rth,"
						+ " registrationtest rt where rt.ntransactiontestcode=rth.ntransactiontestcode "
						// + " and rt.ninstrumentcatcode > 0 "
						// + " and rth.ntransactionstatus in ("+stransactionStatus+")"
						// + " "+toCheckAccepted+"" ////Commented by sonia on 7th Aug 2024 for JIRA
						// ID:ALPD-4544
						+ " and rth.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " and rt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " and rt.nsitecode=" + userInfo.getNtranssitecode() + "" + " and rth.nsitecode="
						+ userInfo.getNtranssitecode() + "" + " group by rth.ntransactiontestcode)"
						+ " and rth.ntransactionstatus in (" + stransactionStatus + ")" + " and r.nisiqcmaterial <> "
						+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ""
						// + " and rth.ntransactionstatus between
						// "+Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()+""
						// + " and "+Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()+""
						+ " group by tm.ntestcode,rt.nsectioncode order by ntestcode desc  ";*/
				
			    //ALPD-5137--Vignesh R(20-12-2024)---Including filter in Data selection Kendo Grid

				String jobAllocoationQuery="";
				if(nneedjoballocation) {
				 jobAllocoationQuery=" JOIN joballocation jb ON jb.npreregno = r.npreregno  AND jb.ntransactiontestcode = rt.ntransactiontestcode "
						+ " and jb.nsectioncode = rt.nsectioncode AND jb.nusercode IN ("+userInfo.getNusercode()+", -1) AND jb.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  "
						+ " and jb.nsitecode = "+userInfo.getNtranssitecode()+"";
						
			}
				
				final String getTest = "select tm.ntestcode,tm.stestname,max(tgt.ntestgrouptestcode) ntestgrouptestcode,"
						+ " max(tgt.nmethodcode) nmethodcode ,max(tgt.nspecsampletypecode) nspecsampletypecode,"
						+ " max(tgtp.nchecklistversioncode)nchecklistversioncode,max(r.nprojectmastercode)nprojectmastercode,"
						+ " max(tgsst.nspecsampletypecode) nspecsampletypecode,max(tgsst.nallottedspeccode) nallottedspeccode,"
						+ " max(tgsst.ncomponentcode) ncomponentcode from registration r "
						+ " JOIN registrationtest rt ON rt.npreregno = r.npreregno "
						+ " JOIN registrationtesthistory rth   ON rth.ntransactiontestcode = rt.ntransactiontestcode "
						+ " and rth.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" AND rth.nsitecode = "+userInfo.getNtranssitecode()+" "
						+ " "+jobAllocoationQuery+" "
						+ " JOIN testmaster tm ON rt.ntestcode = tm.ntestcode "
					    + " and tm.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					    + " and tm.nsitecode = "+userInfo.getNmastersitecode()+ " "
						+ " JOIN testgrouptest tgt ON tgt.ntestcode = tm.ntestcode "
						+ " and tgt.ntestgrouptestcode = rt.ntestgrouptestcode "
						+ " and tgt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" AND tgt.nsitecode = "+userInfo.getNmastersitecode()+" "					
						+ " JOIN testgrouptestparameter tgtp  ON tgtp.ntestgrouptestcode = tgt.ntestgrouptestcode "
						+ " and tgtp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" AND tgtp.nsitecode = "+userInfo.getNmastersitecode()+" "
						+ " JOIN testgroupspecsampletype tgsst ON tgsst.nspecsampletypecode = tgt.nspecsampletypecode "
						+ " and tgsst.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  AND tgsst.nsitecode = "+userInfo.getNmastersitecode()+" "
						+ " where  r.nregtypecode = "+nregtypecode+""
						+ " and r.nregsubtypecode = "+nregsubtypecode+""
						+"  and rt.ntransactiontestcode = rth.ntransactiontestcode "
						+ " and rt.nsectioncode = "+objSection.getNsectioncode()+""
						+ " and r.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
						+ " and rt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""					
						+ " and r.nsitecode="+userInfo.getNtranssitecode()+""					
						+ " and rth.ntesthistorycode = any (select max(rth.ntesthistorycode)from registrationtesthistory rth,"
						+ " registrationtest rt where rt.ntransactiontestcode=rth.ntransactiontestcode "
						//+ " and rt.ninstrumentcatcode > 0 "
						//+ " and rth.ntransactionstatus in ("+stransactionStatus+")"
					//	+ " "+toCheckAccepted+""  ////Commented by sonia on 7th Aug 2024 for JIRA ID:ALPD-4544
						+ " and rth.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
						+ " and rt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
						+ " and rt.nsitecode="+userInfo.getNtranssitecode()+""
						+ " and rth.nsitecode="+userInfo.getNtranssitecode()+""
						+ " group by rth.ntransactiontestcode)"
						+ " and rth.ntransactionstatus in ("+stransactionStatus+")"
						+ " and r.nisiqcmaterial <> "+Enumeration.TransactionStatus.YES.gettransactionstatus()+""
		//				+ "	and rth.ntransactionstatus between "+Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()+""
		//				+ "	and "+Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()+""
						+ " group by tm.ntestcode,rt.nsectioncode order by ntestcode desc  ";
			

//			final String getTest = "select tm.ntestcode,tm.stestname,max(rt.nsectioncode) nsectioncode from "
//					+ " registrationtesthistory rth,registrationtest rt,testmaster tm "
//					+ " where rth.ntransactiontestcode=rt.ntransactiontestcode "
//					+ " and rt.ntestcode=tm.ntestcode "
//					+ " and rt.nsectioncode = any (select ls.nsectioncode from sectionusers su,labsection ls where"
//					+ "	su.nusercode = "+userInfo.getNusercode()+" and ls.nlabsectioncode=su.nlabsectioncode "
//					+ " and su.nsitecode= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ "	and ls.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and su.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
//					+ " group by ls.nsectioncode) "
//					+ " and rth.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and rt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and tm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and rth.ntesthistorycode = any (select max(rth.ntesthistorycode)from registrationtesthistory rth,"
//					+ " registrationtest rt where rt.ntransactiontestcode=rth.ntransactiontestcode and rt.ninstrumentcatcode > 0 "
//					+ " and rth.ntransactionstatus between "+Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()+""
//					+ " and "+Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()+""
//					+ " and rth.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and rt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " group by rth.ntransactiontestcode)"
//					+ " group by tm.ntestcode order by ntestcode desc  ";

				lsttestvalues = jdbcTemplate.query(getTest, new RegistrationTest());

				if (!lsttestvalues.isEmpty()) {
					returnMap.put("Testvalues", lsttestvalues);
					returnMap.put("selectedTestSynonym", lsttestvalues.get(0));
					returnMap.put("selectedSection", objSection);
					return new ResponseEntity<>(returnMap, HttpStatus.OK);
				} else {
					returnMap.put("selectedTestSynonym", null);
					returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), "IDS_NOTESTTOADDFORTHISSECTION");
					// "IDS_ADDINSTRUMENTTOTESTORTESTNOTAVLILABLE");
					returnMap.put("Testvalues", null);
					returnMap.put("selectedSection", inputMap.get("section"));
					return new ResponseEntity<>(returnMap, HttpStatus.OK);

				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NOTESTTOADDFORTHISSECTION",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}

		}

		@SuppressWarnings("unchecked")
		@Override
		public ResponseEntity<Object> getTestBasedInstrumentCat(Map<String, Object> inputMap, UserInfo userInfo)
				throws Exception {
			// List<TestInstrumentCategory> lstInstCategory = new ArrayList<>();
			Map<String, Object> returnMap = new HashMap<>();
			int naddcontrolCode = (int) inputMap.get("naddcontrolCode");
			int nregtypecode = (int) inputMap.get("nregtypecode");
			int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
			//List<ProjectMember> listProjectMember = new ArrayList<>();

			List<TransactionValidation> validationStatusLst = (List<TransactionValidation>) getTransactionValidation(
					naddcontrolCode, nregtypecode, nregsubtypecode, userInfo);
			final String stransactionStatus = validationStatusLst.stream()
					.map(objpreregno -> String.valueOf(objpreregno.getNtransactionstatus()))
					.collect(Collectors.joining(","));

			// returnMap.putAll((Map<String, Object>) getProduct(inputMap,
			// userInfo).getBody());
			returnMap = (Map<String, Object>) getProduct(inputMap, userInfo).getBody();

			if ((int) inputMap.get("nsampletypecode") == Enumeration.SampleType.PROJECTSAMPLETYPE.getType()) {
				List<ProjectMaster> lstProjectMaster = new ArrayList<>();
				// listProjectMember = getProjectMemberCheck(userInfo);
				// if(listProjectMember.size() > 0) {
//					String sQuery = "select pm.sprojectcode,pm.nprojectmastercode,pm.sprojectname,pr.nprojectmembercode"
//							+ " from projectmaster pm,projectmember pr where pm.nprojectmastercode="+(int) inputMap.get("nprojectmastercode")+""
//							+ " and pm.nprojectmastercode=pr.nprojectmastercode and pr.nusercode="+userInfo.getNusercode()+" "
//							+ " and pm.nuserrolecode="+userInfo.getNuserrole()+""
//							+ " and pr.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//							+ " and pm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";

				String sQuery = "select * from projectmaster pm where pm.nprojectmastercode= any("
						+ " select rg.nprojectmastercode from registrationtest rt,registration rg " + " where rt.ntestcode="
						+ inputMap.get("ntestcode") + " and rt.npreregno=rg.npreregno and rg.nregtypecode=" + nregtypecode
						+ "" + " and rg.nregsubtypecode=" + nregsubtypecode + " and rg.nsampletypecode="
						+ inputMap.get("nsampletypecode") + "" + " and rg.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rt.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rg.nsitecode="
						+ userInfo.getNtranssitecode() + "" + " and rt.nsitecode=" + userInfo.getNtranssitecode() + ""
						+ " group by rg.nprojectmastercode)" + " and pm.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" 
						//+ " and pm.nsitecode="+userInfo.getNtranssitecode()+""
						+ " order by 1 desc";

				lstProjectMaster = jdbcTemplate.query(sQuery, new ProjectMaster());

				if (!lstProjectMaster.isEmpty()) {
					returnMap.put("selectedProjectedCode", lstProjectMaster.get(0));
					// return new ResponseEntity<>(returnMap, HttpStatus.OK);
				} else {
					returnMap.put("selectedProjectedCode", null);
					returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), "IDS_PROJECTCODENOTAVAILABLE");
					return new ResponseEntity<>(returnMap, HttpStatus.EXPECTATION_FAILED);

				}
				returnMap.put("ProjectCode", lstProjectMaster);
//				}else {
//					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_MEMBERNOTINPROJECT",userInfo.getSlanguagefilename()),
//							  HttpStatus.EXPECTATION_FAILED);
//				}
			}

			int nsampletypecode = (int) inputMap.get("nsampletypecode");
			String stablename = "";
			String joiningField = "";
			String sProduct = "";
			int strValue = 0;
			// List<Product> lstStr = (List<Product>) returnMap.get("selectedProduct");
			List<Product> lstStr = new ArrayList<>();
			lstStr.add((Product) returnMap.get("selectedProduct"));

			if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()) {
				stablename = "registrationsample";
				joiningField = "ntransactionsamplecode";
				sProduct = "ncomponentcode";
				strValue = returnMap.get("selectedProduct") != null ? (int) lstStr.get(0).getNproductcode() : -1;
			} else {
				stablename = "registration";
				joiningField = "npreregno";
				sProduct = "nproductcode";
				strValue = returnMap.get("selectedProduct") != null ? (int) lstStr.get(0).getNproductcode() : -1;
			}

			final List<TestInstrumentCategory> lstInstCategory = getInstCategory(stablename, joiningField, sProduct,
					strValue, stransactionStatus, inputMap, userInfo);

//			String sQuery = "select ic.ninstrumentcatcode,ic.sinstrumentcatname,ic.ncalibrationreq from "
//					+ " registrationtesthistory rth,registrationtest rt,"+stablename+" p,"
//					+ " instrumentcategory ic "
//					+ " where rth.ntransactiontestcode=rt.ntransactiontestcode"
//					+ " and rt.ninstrumentcatcode=ic.ninstrumentcatcode"
//					+ " and p."+joiningField+"=rt."+joiningField+""
//					+ " and rth.nsitecode="+userInfo.getNtranssitecode()+""
//					+ " and rt.nsitecode="+userInfo.getNtranssitecode()+""
//					+ " and ic.nsitecode="+userInfo.getNmastersitecode()+""
//					+ " and rth.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and rt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and ic.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and p."+sProduct+"="+strValue+""
//					+ " and rth.ntesthistorycode = any (select max(rth.ntesthistorycode)from registrationtesthistory rth,"
//					+ " registrationtest rt where rt.ntransactiontestcode=rth.ntransactiontestcode "
//					+ " and rt.ninstrumentcatcode > 0  and rt.ntestcode="+inputMap.get("ntestcode")+""
////					+ " and rth.ntransactionstatus between "+Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()+" "
////					+ " and "+Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()+" "
//					+ " and rth.nsitecode="+userInfo.getNtranssitecode()+""
//					+ " and rt.nsitecode="+userInfo.getNtranssitecode()+""
//					+ " and rth.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
//					+ " and rt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " group by rth.ntransactiontestcode)"
////					+ " and rth.ntransactionstatus between "+Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()+" "
////					+ " and "+Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()+" 
//					+ " and rth.ntransactionstatus in("+stransactionStatus+")"
//					+ " group by ic.ninstrumentcatcode";
//			lstInstCategory=getJdbcTemplate().query(sQuery, new TestInstrumentCategory());

			if (!lstInstCategory.isEmpty()) {
				returnMap.put("selectedInstrumentCategory", lstInstCategory.get(0));
				returnMap.put("selectedTestSynonym", inputMap.get("stestname"));
			} else {
				// returnMap.put("selectedInstrumentCategory",);
				returnMap.put("selectedTestSynonym", inputMap.get("stestname"));
			}
			returnMap.put("selectedTestSynonym", inputMap.get("stestname"));
			returnMap.put("instrumentCategory", lstInstCategory);
			return new ResponseEntity<>(returnMap, HttpStatus.OK);

		}

		@SuppressWarnings("unchecked")
		@Override
		public ResponseEntity<Object> createBatchmaster(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
			// TODO Auto-generated method stub
			Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
			JSONObject actionType = new JSONObject();
			JSONObject jsonAuditObject = new JSONObject();

			final String sQuery = " lock table lockbatchcreation " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);
	// ALPD-5272	Added validation for add batch by Vishakh
			Map<String,Object> mapRegSubType = (Map<String, Object>) inputMap.get("defaultRegistrationSubType");
			Boolean nneedjoballocation = (Boolean) mapRegSubType.get("nneedjoballocation");
			Boolean nneedmyjob = (Boolean) mapRegSubType.get("nneedmyjob");
			String stransCodeToValidate = "";
			String idsStransCoddToValidate = "";
			if (nneedmyjob) {
				stransCodeToValidate = "" + Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus();
				idsStransCoddToValidate = commonFunction.getMultilingualMessage("IDS_SELECTACCEPTTEST", userInfo.getSlanguagefilename());
			} else if (!nneedmyjob && nneedjoballocation) {
				stransCodeToValidate = "" + Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus();
				idsStransCoddToValidate = commonFunction.getMultilingualMessage("IDS_SELECTALLOTTEDTEST", userInfo.getSlanguagefilename());
			} else if (!nneedmyjob && !nneedjoballocation) {
				stransCodeToValidate = "" + Enumeration.TransactionStatus.REGISTERED.gettransactionstatus();
				idsStransCoddToValidate = commonFunction.getMultilingualMessage("IDS_SELECTREGISTEREDTEST", userInfo.getSlanguagefilename());
			}
			
			String strCheckValidation = "select rt.jsondata->>'stestsynonym' stestsynonym, rth.ntransactionstatus ntransactionstatus, "
					+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'"+ userInfo.getSlanguagetypecode()+"', ts.jsondata->'stransdisplaystatus'->>'en-US') stransdisplaystatus "
					+ " from registration r  "
					+ " JOIN registrationtest rt ON rt.npreregno = r.npreregno and rt.ntestcode="+ inputMap.get("ntestcode")+" "
					+ " JOIN registrationtesthistory rth   ON rth.ntransactiontestcode = rt.ntransactiontestcode  and rth.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" AND rth.nsitecode = "+ userInfo.getNtranssitecode()+" "
					+ " JOIN transactionstatus ts ON rth.ntransactionstatus=ts.ntranscode and ts.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " JOIN testmaster tm ON rt.ntestcode = tm.ntestcode  and tm.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  "
					+ " and tm.nsitecode = "+userInfo.getNmastersitecode()+""
					+ " JOIN testgrouptest tgt ON tgt.ntestcode = tm.ntestcode  and tgt.ntestgrouptestcode = rt.ntestgrouptestcode  and tgt.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" AND tgt.nsitecode = "+ userInfo.getNmastersitecode()+" "
					+ " where  r.nregtypecode = "+ inputMap.get("nregtypecode")+" and r.nregsubtypecode = "+ inputMap.get("nregsubtypecode")+"  and rt.ntransactiontestcode = rth.ntransactiontestcode  and rt.nsectioncode = "+ inputMap.get("nsectioncode")+" "
					+ " and r.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and rt.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and r.nsitecode="+ userInfo.getNtranssitecode()+" "
					+ " and rth.ntesthistorycode = any (select max(rth.ntesthistorycode)from registrationtesthistory rth, registrationtest rt "
					+ " where rt.ntransactiontestcode=rth.ntransactiontestcode  and rth.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and rt.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and rt.nsitecode="
					+ userInfo.getNtranssitecode()+" and rth.nsitecode="+ userInfo.getNtranssitecode()+" "
					+ " group by rth.ntransactiontestcode) "
					+ " and rth.ntransactionstatus in ("+ stransCodeToValidate+") "
					+ " and r.nisiqcmaterial <> "+ Enumeration.TransactionStatus.YES.gettransactionstatus()+ ";";
			List<Map<String,Object>> validateTestDetails = jdbcTemplate.queryForList(strCheckValidation);
			
			if(validateTestDetails.size() == 0) {
				return new ResponseEntity<>(idsStransCoddToValidate, HttpStatus.EXPECTATION_FAILED);
			}
			Map<String, Object> returnMap = new HashMap<>();

			String sSeqQuery = "select nsequenceno from seqnobatchcreation where stablename='batchmaster' "
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			SeqNoBatchcreation objSeq = (SeqNoBatchcreation)jdbcUtilityFunction.queryForObject(sSeqQuery,SeqNoBatchcreation.class,jdbcTemplate); 
			int nseqno = objSeq.getNsequenceno() + 1;

			String sBatchQuery = "select nsequenceno from seqnobatchcreation where stablename='batchhistory' "
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			SeqNoBatchcreation seqNoBatchHistory = (SeqNoBatchcreation)jdbcUtilityFunction.queryForObject(sBatchQuery,SeqNoBatchcreation.class,jdbcTemplate); 
			int nHistseqno = seqNoBatchHistory.getNsequenceno() + 1;

			String sInsertUpdateQuery = "update seqnobatchcreation set nsequenceno=" + nseqno
					+ " where stablename='batchmaster' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
			// getJdbcTemplate().execute(sUpdateQuery);

			// ALPD-3160
			String instrumentID = "";
			if (inputMap.containsKey("sinstrumentid") && inputMap.get("sinstrumentid") != null) {
				instrumentID = (String) inputMap.get("sinstrumentid");
			}

			sInsertUpdateQuery = sInsertUpdateQuery
					+ "insert into batchmaster(nbatchmastercode,nsampletypecode,nregtypecode,"
					+ " nregsubtypecode,ntestcode,ninstrumentcatcode,ninstrumentcode,sinstrumentid,nproductcode,nsectioncode,napprovalversioncode,"
					+ " nprojectmastercode,nsitecode,nstatus)" + " values(" + nseqno + "," + inputMap.get("nsampletypecode")
					+ "," + inputMap.get("nregtypecode") + "," + inputMap.get("nregsubtypecode") + "," + " "
					+ inputMap.get("ntestcode") + "," + inputMap.get("ninstrumentcatcode") + ","
					+ inputMap.get("ninstrumentcode") + "," + " N'" + stringUtilityFunction.replaceQuote(instrumentID) + "',"
					+ inputMap.get("nproductcode") + "," + inputMap.get("nsectioncode") + "," + " "
					+ inputMap.get("napprovalversioncode") + ",'" + inputMap.get("nprojectmastercode") + "'," + " "
					+ userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ")";

			jdbcTemplate.execute(sInsertUpdateQuery);

			String sHistoryUpdateQuery = "update seqnobatchcreation set nsequenceno=" + nHistseqno
					+ " where stablename='batchhistory' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
			jdbcTemplate.execute(sHistoryUpdateQuery);
			String scomments = (String) inputMap.get("scomments");

			String sBatchHistoryQuery = " insert into batchhistory(nbatchhistorycode,nbatchmastercode,sbatcharno,"
					+ " ndeputyusercode,ndeputyuserrolecode,ntransactionstatus,nusercode,nuserrolecode,dtransactiondate,noffsetdtransactiondate,ntransdatetimezonecode,"
					+ " scomments,nsitecode,nstatus) values(" + nHistseqno + "," + nseqno + ",'"
					+ inputMap.get("sbatcharno") + "'," + " " + userInfo.getNdeputyusercode() + ","
					+ userInfo.getNdeputyuserrole() + "," + inputMap.get("ntransactionstatus") + "," + " "
					+ userInfo.getNusercode() + "," + userInfo.getNuserrole() + "," + " '" +dateUtilityFunction.getCurrentDateTime(userInfo)
					+ "'," + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + " " + userInfo.getNtimezonecode()
					+ ",N'" +stringUtilityFunction.replaceQuote(scomments) + "'," + userInfo.getNtranssitecode() + "," + " "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

			jdbcTemplate.execute(sBatchHistoryQuery);
			final String ui_ToDate = (String) inputMap.get("toDate");
			final String toDate = dateUtilityFunction.getCurrentDateTime(userInfo).toString();
			inputMap.put("toDate",toDate.substring(0,toDate.length()-1));
			returnMap.putAll((Map<String, Object>) getBatchmaster(inputMap, userInfo).getBody());
			returnMap.putAll((Map<String, Object>) changeTimeFormatForUi(ui_ToDate, userInfo));
			List<Batchmaster> lstbatchmaster = new ArrayList<>();
			lstbatchmaster.add((Batchmaster) returnMap.get("SelectedBatchmaster"));
			auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
			auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
			auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
			actionType.put("batchmaster", "IDS_BATCHCREATION");
			jsonAuditObject.put("batchmaster", lstbatchmaster);
			auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);
			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		}

		
		@Override
		public ResponseEntity<Object> getInstrument(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
			
			List<Instrument> lstInstrument = new ArrayList<>();
			Map<String, Object> returnMap = new HashMap<>();
			String stablename = "";
			String sStringQuery = "";

			// String sinstrumentid = (String)inputMap.get("sinstrumentid");
			// int ninstrumentcode = (int) inputMap.get("ninstrumentcode");

//			String sInstQuery=" select i.sinstrumentname,i.ninstrumentcode,i.sinstrumentid"
//					+ " from instrument i,instrumentcategory ic"
//					+ " where i.ninstrumentcatcode=ic.ninstrumentcatcode "
//					+ " and i.ninstrumentcatcode="+inputMap.get("ninstrumentcatcode")+""
//					//+ " and i.sinstrumentid='"+ReplaceQuote(sinstrumentid)+"'"
//					//+ " and ninstrumentcode="+ninstrumentcode+""
//					+ " and i.nsitecode="+userInfo.getNmastersitecode()+""
//					+ " and ic.nsitecode="+userInfo.getNmastersitecode()+""
//					+ " and i.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and ic.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
//				   // + " group by i.sinstrumentname";

			if ((int) inputMap.get("ncalibrationreq") == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				stablename = "instrumentmaintenance im,instrumentcalibration ic";
				sStringQuery = " i.ninstrumentcode=im.ninstrumentcode and "
						+ " im.ninstrumentmaintenancecode= any(select max(ninstrumentmaintenancecode) "
						+ " from instrumentmaintenance where i.ninstrumentcatcode=" + inputMap.get("ninstrumentcatcode")
						+ "" + " and nsitecode=" + userInfo.getNtranssitecode() + "" + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by ninstrumentcode)"
						+ " and im.nmaintenancestatus=" + Enumeration.TransactionStatus.MAINTANENCE.gettransactionstatus()
						+ "" + " and ic.ncalibrationstatus="
						+ Enumeration.TransactionStatus.CALIBIRATION.gettransactionstatus() + ""
						+ " and i.ninstrumentcode=ic.ninstrumentcode "
						+ " and ic.ninstrumentcalibrationcode=any(select max(ninstrumentcalibrationcode) "
						+ " from instrumentcalibration where i.ninstrumentcatcode=" + inputMap.get("ninstrumentcatcode")
						+ "" + " and nsitecode=" + userInfo.getNtranssitecode() + "" + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by ninstrumentcode)"
						+ " and im.nsitecode=" + userInfo.getNtranssitecode() + "" + " and ic.nsitecode="
						+ userInfo.getNtranssitecode() + "" + " and im.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and ic.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			} else {
				stablename = "instrumentmaintenance im ";
				sStringQuery = " i.ninstrumentcode=im.ninstrumentcode "
						+ " and im.ninstrumentmaintenancecode= any(select max(ninstrumentmaintenancecode)"
						+ " from instrumentmaintenance where i.ninstrumentcatcode=" + inputMap.get("ninstrumentcatcode")
						+ "" + " and nsitecode=" + userInfo.getNtranssitecode() + "" + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by ninstrumentcode)"
						+ " and im.nmaintenancestatus=" + Enumeration.TransactionStatus.MAINTANENCE.gettransactionstatus()
						+ "" + " and im.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " and im.nsitecode=" + userInfo.getNtranssitecode() + "";
			}

			String sInstQuery = " select  max(ninstrumentcatcode) ninstrumentcatcode, "
					+ " i.ninstrumentnamecode,i.sinstrumentname,max(i.ninstrumentcode)ninstrumentcode"
					+ " from instrument i,instrumentname ie," + stablename + ""
//					+ " where i.ninstrumentnamecode = any "
//					+ "(select ie.ninstrumentnamecode from instrumentname ie "
//					+ " where ie.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and ie.nsitecode="+userInfo.getNmastersitecode()+") "
					+ " where i.ninstrumentnamecode=ie.ninstrumentnamecode" + " and i.ninstrumentcatcode="
					+ inputMap.get("ninstrumentcatcode") + "" + " and i.nregionalsitecode=" + userInfo.getNtranssitecode()
					+ "" + " and i.ninstrumentstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and i.nsitecode=" + userInfo.getNmastersitecode() + "" + " and i.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and ie.nsitecode="
					+ userInfo.getNmastersitecode() + "" + " and ie.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and " + sStringQuery + ""
					+ " group by i.ninstrumentnamecode,i.sinstrumentname";

			// + " group by i.sinstrumentname";

			lstInstrument = jdbcTemplate.query(sInstQuery, new Instrument());
			if (!lstInstrument.isEmpty()) {
				// returnMap.put("selectedInstrument", lstInstrument.get(0));
				// returnMap.put("selectedInstrumentId", lstInstrument.get(0));
			} else {
				returnMap.put("selectedInstrument", null);
			}
			returnMap.put("selectedInstrumentCategory", inputMap.get("sinstrumentcatname"));
			returnMap.put("instrument", lstInstrument);
			return new ResponseEntity<>(returnMap, HttpStatus.OK);

		}

		@SuppressWarnings("unchecked")
		@Override
		public ResponseEntity<Object> getBatchmaster(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
			Map<String, Object> returnMap = new HashMap<>();

			List<Batchmaster> lstBatchmaster = new ArrayList<>();

			String fromDate = "";
			String toDate = "";
//			String stablename = "";
//			String fieldname = "";
//			String selectField = "";
			//int nsampletypecode = (int) inputMap.get("nsampletypecode");
			String ntranscode = "";
			String napprovalconfigcode = "";
			String napprovalversioncode = "";
			//ALPD-5076 added by Dhanushya RI,If the status value is all then the enire filter will be append to the string
			if(inputMap.containsKey("ntranscode")) {
				ntranscode = inputMap.get("ntranscode").toString();
				if(ntranscode.equals(String.valueOf(Enumeration.TransactionStatus.ALL.gettransactionstatus()))) {
					ntranscode=inputMap.containsKey("allTranscode")?inputMap.get("allTranscode").toString():ntranscode;
				}
			}
			else {
				ntranscode = String.valueOf(Enumeration.TransactionStatus.DRAFT.gettransactionstatus());	
			}
			if (inputMap.containsKey("ntranscode")) {
				napprovalconfigcode = (String) inputMap.get("napprovalconfigcode").toString();
				napprovalversioncode = (String) inputMap.get("napprovalversioncode").toString();
			} else {
				napprovalconfigcode = String.valueOf(Enumeration.TransactionStatus.NA.gettransactionstatus());
				napprovalversioncode = String.valueOf(Enumeration.TransactionStatus.NA.gettransactionstatus());
			}
			int ndesigntemplatemappingcode = (int) inputMap.get("ndesigntemplatemappingcode");

			if (inputMap.containsKey("fromDate")) {

				final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
				final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());

				final String fromDateUI = LocalDateTime.parse((String) inputMap.get("fromDate"), dbPattern)
						.format(uiPattern);
				final String toDateUI = LocalDateTime.parse((String) inputMap.get("toDate"), dbPattern).format(uiPattern);

				returnMap.put("realFromDate", fromDateUI);
				returnMap.put("realToDate", toDateUI);

				fromDate = dateUtilityFunction.instantDateToString(
						dateUtilityFunction.convertStringDateToUTC((String) inputMap.get("fromDate"), userInfo, true));
				toDate = dateUtilityFunction.instantDateToString(
						dateUtilityFunction.convertStringDateToUTC((String) inputMap.get("toDate"), userInfo, true));
				fromDate = fromDate.replace("T", " ");
				toDate = toDate.replace("T", " ");
				returnMap.put("fromdate", fromDate);
				returnMap.put("todate", toDate);
			}

			final Integer count = projectDAOSupport.getApprovalConfigVersionByUserRoleTemplate(Integer.parseInt(napprovalversioncode),
					userInfo);
			if (inputMap.containsKey("operation") && count == 0) {
				// returnMap.put(Enumeration.ReturnStatus.SUCCESS.getreturnstatus(),(commonFunction.getMultilingualMessage("IDS_USERNOTINRESULTENTRYFLOW",userInfo.getSlanguagefilename())));
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_USERNOTINRESULTENTRYFLOW",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else {

//				if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()) {
//					stablename = "component";
//					fieldname = "ncomponentcode";
//					selectField = "scomponentname";
//				} else {
//					stablename = "product";
//					fieldname = "nproductcode";
//					selectField = "sproductname";
//				}

	// Commenting this query for stored procedure

//			String sbtachQuery="select max(bh.nbatchmastercode)nbatchmastercode,max(bm.nsampletypecode)nsampletypecode,bh.sbatcharno,s.ssectionname,bm.nsectioncode, "
//			+" bm.ntestcode,to_char(bh.dtransactiondate,'"+userInfo.getSsitedate()+"') as stransactiondate,"
//			+" CONCAT(u.sfirstname,' ',u.slastname) as username,bh.scomments,"
//			+" coalesce(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"', ts.jsondata->'stransdisplaystatus'->>'en-US') stransdisplaystatus ,"
//			+" bh.sbatcharno,bh.ntransactionstatus,"
//			+" bm.sinstrumentid,"
//			+" bm.ninstrumentcode,"
//			+" coalesce(rst.jsondata->'sregsubtypename'->>'"+userInfo.getSlanguagetypecode()+"',"
//			+" rst.jsondata->'sregsubtypename'->>'en-US') sregsubtypename,"
//			+" coalesce(rt.jsondata->'sregtypename'->>'"+userInfo.getSlanguagetypecode()+"',rt.jsondata->'sregtypename'->>'en-US')sregtypename,"
//			+" tm.stestname,i.sinstrumentname,ic.sinstrumentcatname,p."+selectField+" sproductname "
//			+" from batchmaster bm,testmaster tm,instrument i,instrumentcategory ic,registrationsubtype rst,"
//			+" registrationtype rt,"+stablename+" p,batchhistory bh,transactionstatus ts,approvalconfig ac,"
//			+" regsubtypeconfigversion rsv,users u , section s,registration rg,registrationarno rarno "
//			+" where tm.ntestcode=bm.ntestcode and ic.ninstrumentcatcode=bm.ninstrumentcatcode "
//			+" and rt.nregtypecode=rst.nregtypecode and p."+fieldname+"=bm.nproductcode"
//			+" and bh.nbatchmastercode=bm.nbatchmastercode and s.nsectioncode=bm.nsectioncode "
//			+" and rsv.napprovalconfigcode = ac.napprovalconfigcode"
//			+" and rsv.napprovalconfigcode ="+napprovalconfigcode+""
//			+" and rst.nregtypecode=rg.nregtypecode "
//			+" and rst.nregsubtypecode=rg.nregsubtypecode"
//			+" and rarno.npreregno=rg.npreregno"
//			//+" and rarno.napprovalversioncode="+napprovalversioncode+""
//			+" and bm.napprovalversioncode="+napprovalversioncode+""
//			+" and rg.ndesigntemplatemappingcode="+ndesigntemplatemappingcode+""
//			+" and bh.dtransactiondate between '"+fromDate+"' and '"+toDate+"'"
//			+" and u.nusercode = bh.nusercode"
//			+" and ts.ntranscode=bh.ntransactionstatus"
//			+" and i.ninstrumentcatcode=ic.ninstrumentcatcode "
//			+" and rst.nregsubtypecode="+inputMap.get("nregsubtypecode")+""
//			+" and rst.nregtypecode="+inputMap.get("nregtypecode")+""
//			+" and bm.ninstrumentcode=i.ninstrumentcode "
//			+" and bm.nsampletypecode="+inputMap.get("nsampletypecode")+""
//			+" and bm.nregtypecode="+inputMap.get("nregtypecode")+""
//			+" and bm.nregsubtypecode="+inputMap.get("nregsubtypecode")+""
//			+" and bh.nbatchhistorycode = any(select max(bh.nbatchhistorycode) from batchmaster bm, batchhistory bh "
//			+" where bh.nbatchmastercode = bm.nbatchmastercode and bm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
//			+" and bh.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" group by bh.nbatchmastercode)"
//			+" and bh.ntransactionstatus = "+ntranscode+""
//			+" and u.nsitecode ="+userInfo.getNmastersitecode()+""
//			+" and bm.nsitecode="+userInfo.getNtranssitecode()+""
//			+" and bh.nsitecode="+userInfo.getNtranssitecode()+""
//			+" and tm.nsitecode="+userInfo.getNmastersitecode()+""
//			+" and rg.nsitecode="+userInfo.getNtranssitecode()+""
//			+" and rarno.nsitecode="+userInfo.getNtranssitecode()+""
//			+" and i.nsitecode= "+userInfo.getNmastersitecode()+""
//			+" and ic.nsitecode="+userInfo.getNmastersitecode()+""
//			+" and rt.nsitecode="+userInfo.getNmastersitecode()+""
//			+" and rst.nsitecode="+userInfo.getNmastersitecode()+""
//			+" and p.nsitecode="+userInfo.getNmastersitecode()+""
//			+" and s.nsitecode="+userInfo.getNmastersitecode()+""
//			+" and u.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//			+" and bm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//			+" and bh.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//			+" and ts.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//			+" and tm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//			+" and rg.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//			+" and rarno.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//			+" and i.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//			+" and ic.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//			+" and rt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//			+" and rst.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//			+" and p.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//			+" and s.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//			+" group by bh.nbatchhistorycode,bh.sbatcharno,s.ssectionname,bm.nsectioncode,bm.sinstrumentid,"
//			+" rsv.napprovalconfigcode,stransactiondate,username,stransdisplaystatus,sregsubtypename,bh.scomments,"
//			+" sregtypename,tm.stestname,i.sinstrumentname,ic.sinstrumentcatname,sproductname,"
//			+" bh.sbatcharno,bh.ntransactionstatus,bm.ntestcode,bm.ninstrumentcode"
//			+" order by nbatchmastercode desc";

				final String getSampleQuery = "SELECT * from fn_batchget('" + fromDate + "','" + toDate + "'," + ""
						+ inputMap.get("nsampletypecode") + "," + inputMap.get("nregtypecode") + ","
						+ inputMap.get("nregsubtypecode") + "," + "" + napprovalversioncode + ","
						+ ndesigntemplatemappingcode + "," + "'" + ntranscode + "'," + userInfo.getNusercode() + ","
						+ userInfo.getNtranssitecode() + "," + "'" + userInfo.getSlanguagetypecode() + "',"
						+ napprovalconfigcode + "," + userInfo.getNmastersitecode() + ",'" + userInfo.getSsitedate()
						+ "');";
				//logger.info("Batch=======>" + getSampleQuery);
				lstBatchmaster = jdbcTemplate.query(getSampleQuery, new Batchmaster());
				if (!lstBatchmaster.isEmpty()) {
					returnMap.put("SelectedBatchmaster", lstBatchmaster.get(0));
					returnMap.put("nbatchmastercode", lstBatchmaster.get(0).getNbatchmastercode());
					returnMap.put("ntestcode", lstBatchmaster.get(0).getNtestcode());
					returnMap.put("defaultSampleType", inputMap.get("defaultSampleType"));
					returnMap.put("defaultRegistrationType", inputMap.get("defaultRegistrationType"));
					returnMap.put("defaultRegistrationSubType", inputMap.get("defaultRegistrationSubType"));
					returnMap.putAll((Map<String, Object>) getSampleTabDetails(lstBatchmaster.get(0).getNbatchmastercode(),
							userInfo, ndesigntemplatemappingcode).getBody());
					returnMap.putAll(
							(Map<String, Object>) getBatchhistory(lstBatchmaster.get(0).getNbatchmastercode(), userInfo)
									.getBody());
					returnMap.putAll((Map<String, Object>) getBatchIqcSampleAction(returnMap, userInfo).getBody());
					returnMap.putAll((Map<String, Object>) getTestDetails(returnMap, userInfo).getBody());
					returnMap.putAll((Map<String, Object>) getBatchTAT(returnMap, userInfo).getBody());

					returnMap.put("DynamicDesign",
							projectDAOSupport.getTemplateDesign((int) inputMap.get("ndesigntemplatemappingcode"), userInfo.getNformcode()));
//				if(inputMap.containsKey("nneedcombodataforFilter")) {
//					returnMap.putAll((Map<String, Object>) getRegistrationType(inputMap, userInfo).getBody());
////					returnMap.putAll((Map<String, Object>) getRegistrationsubType(inputMap, userInfo).getBody());
////					returnMap.putAll((Map<String, Object>) getApprovalConfigVersion(inputMap, userInfo).getBody());
////					returnMap.putAll((Map<String, Object>) getFilterStatus(inputMap, userInfo).getBody());
//				}
//				returnMap.putAll((Map<String, Object>) getRegistrationType(inputMap, userInfo).getBody());
//				returnMap.putAll((Map<String, Object>) getRegistrationsubType(inputMap, userInfo).getBody());
//				returnMap.putAll((Map<String, Object>) getApprovalConfigVersion(inputMap, userInfo).getBody());
//				returnMap.putAll((Map<String, Object>) getFilterStatus(inputMap, userInfo).getBody());

				} else {
					returnMap.put("SelectedBatchmaster", null);
					returnMap.put("defaultSampleType", inputMap.get("defaultSampleType"));
					returnMap.put("defaultRegistrationType", inputMap.get("defaultRegistrationType"));
					returnMap.put("defaultRegistrationSubType", inputMap.get("defaultRegistrationSubType"));
//				returnMap.put("defaultFilterStatus", null);
//				returnMap.put("BCFilterStatus", null);
				}
				returnMap.put("Batchmaster", lstBatchmaster);
				if(inputMap.containsKey("saveFilterSubmit") &&  (boolean) inputMap.get("saveFilterSubmit") ==true) {
					projectDAOSupport.createFilterSubmit(inputMap,userInfo);
				}
				return new ResponseEntity<>(returnMap, HttpStatus.OK);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public ResponseEntity<Object> getProductcategory(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
			List<ProductCategory> lstProductCategory = new ArrayList<>();
			Map<String, Object> returnMap = new HashMap<>();
			String sProductCatQuery = "select * from productcategory where " + " nproductcatcode <> "
					+ Enumeration.TransactionStatus.NA.gettransactionstatus() + "" + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and nsitecode="
					+ userInfo.getNmastersitecode() + "";

			lstProductCategory = jdbcTemplate.query(sProductCatQuery, new ProductCategory());

			if (!lstProductCategory.isEmpty()) {
				final ProductCategory productcategory = getProductCategoryByDefaultStatus(userInfo);
				if (productcategory != null) {
					inputMap.put("nproductcatcode", productcategory.getNproductcatcode());
					returnMap.put("selectedProductcategory", productcategory);
					returnMap.putAll((Map<String, Object>) getProduct(inputMap, userInfo).getBody());
				} else {
					returnMap.put("selectedProductcategory", null);
				}

			}
			returnMap.put("productcategory", lstProductCategory);

			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		}

		private ProductCategory getProductCategoryByDefaultStatus(UserInfo userInfo) throws Exception {
			String sProductCatDefaultQuery = "select * from productcategory where " + " ndefaultstatus ="
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + "" + " and nproductcatcode <> "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and nsitecode="
					+ userInfo.getNmastersitecode() + "";
			return (ProductCategory) jdbcUtilityFunction.queryForObject(sProductCatDefaultQuery, ProductCategory.class,jdbcTemplate); 

		}

		
		@Override
		public ResponseEntity<Object> getProduct(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
			List<Product> lstProduct = new ArrayList<>();
			Map<String, Object> returnMap = new HashMap<>();

			int nsampletypecode = (int) inputMap.get("nsampletypecode");
			//String stablename = "";
			String sProduct = "";

			if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()) {
				sProduct = " select tgsp.ncomponentcode nproductcode,"
						+ " max(c.scomponentname) sproductname,max(r.ntransactionsamplecode) ntransactionsamplecode,"
						+ " max(r.ncomponentcode) ncomponentcode from "
						+ " registrationtesthistory rth,registrationtest rt,registrationsample r,"
						+ " registration rg,component c,testgroupspecsampletype tgsp "
						+ " where rth.ntransactiontestcode=rt.ntransactiontestcode " + " and r.npreregno=rt.npreregno"
						+ " and rg.npreregno=r.npreregno" + " and rg.nregsubtypecode=" + inputMap.get("nregsubtypecode")
						+ "" + " and rg.nregtypecode=" + inputMap.get("nregtypecode") + ""
						+ " and tgsp.nspecsampletypecode=r.nspecsampletypecode and c.ncomponentcode=tgsp.ncomponentcode"
						+ " and rth.nsitecode=" + userInfo.getNtranssitecode() + "" + " and rt.nsitecode="
						+ userInfo.getNtranssitecode() + "" + " and c.nsitecode=" + userInfo.getNmastersitecode() + ""
						+ " and r.nsitecode=" + userInfo.getNtranssitecode() + "" + " and tgsp.nsitecode="
						+ userInfo.getNmastersitecode() + "" + " and rth.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rt.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and c.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and r.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and tgsp.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " and rth.ntesthistorycode = any (select max(rth.ntesthistorycode)from registrationtesthistory rth,"
						+ " registrationtest rt where rt.ntransactiontestcode=rth.ntransactiontestcode"
						// + " and rt.ninstrumentcatcode = "+inputMap.get("ninstrumentcatcode")+" "
						+ " and rt.ntestcode=" + inputMap.get("ntestcode") + ""
//						+ " and rth.ntransactionstatus between "+Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()+""
//						+ " and "+Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()+""
						+ " and rt.nsitecode=" + userInfo.getNtranssitecode() + "" + " and rth.nsitecode="
						+ userInfo.getNtranssitecode() + "" + " and rth.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rt.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " group by rth.ntransactiontestcode)" + " and rg.nsitecode=" + userInfo.getNtranssitecode() + ""
						+ " and rg.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " and rth.ntransactionstatus between "
						+ Enumeration.TransactionStatus.REGISTERED.gettransactionstatus() + "" + "	and "
						+ Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus() + ""
						+ " group by tgsp.ncomponentcode";

				lstProduct = jdbcTemplate.query(sProduct, new Product());
			} else {
				sProduct = "select p.nproductcode,p.sproductname from "
						+ " registrationtesthistory rth,registrationtest rt,registration r,product p"
						+ " where rth.ntransactiontestcode=rt.ntransactiontestcode " + " and r.npreregno=rt.npreregno"
						+ " and p.nproductcode=r.nproductcode" + " and r.nregsubtypecode=" + inputMap.get("nregsubtypecode")
						+ "" + " and r.nregtypecode=" + inputMap.get("nregtypecode") + "" + " and p.nproductcode >= "
						+ Enumeration.TransactionStatus.NA.gettransactionstatus() + "" + " and rth.nsitecode="
						+ userInfo.getNtranssitecode() + "" + " and rt.nsitecode=" + userInfo.getNtranssitecode() + ""
						+ " and p.nsitecode=" + userInfo.getNmastersitecode() + "" + " and r.nsitecode="
						+ userInfo.getNtranssitecode() + "" + " and rth.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rt.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and p.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and r.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " and rth.ntesthistorycode = any (select max(rth.ntesthistorycode)from registrationtesthistory rth,"
						+ " registrationtest rt where rt.ntransactiontestcode=rth.ntransactiontestcode"
						// + " and rt.ninstrumentcatcode = "+inputMap.get("ninstrumentcatcode")+" "
						+ " and rt.ntestcode=" + inputMap.get("ntestcode") + ""
//							+ " and rth.ntransactionstatus between "+Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()+""
//							+ " and "+Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()+""
						+ " and rth.nsitecode=" + userInfo.getNtranssitecode() + "" + " and rt.nsitecode="
						+ userInfo.getNtranssitecode() + "" + " and rth.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rt.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " group by rth.ntransactiontestcode)" + " and rth.ntransactionstatus between "
						+ Enumeration.TransactionStatus.REGISTERED.gettransactionstatus() + "" + "	and "
						+ Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus() + "" + " group by p.nproductcode";

				lstProduct = jdbcTemplate.query(sProduct, new Product());
			}

			if (!lstProduct.isEmpty()) {
				returnMap.put("selectedProduct", lstProduct.get(0));
			} else {
				returnMap.put("selectedProduct", null);
			}
			returnMap.put("product", lstProduct);
			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		}

//		private Product getProductByDefaultStatus(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
//			String sProductCatDefaultQuery = "select * from product where " + " ndefaultstatus ="
//					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + "" + " and nproductcatcode="
//					+ inputMap.get("nproductcatcode") + "" + " and nstatus="
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and nsitecode="
//					+ userInfo.getNmastersitecode() + "";
//			return (Product) jdbcUtilityFunction.queryForObject(sProductCatDefaultQuery, Product.class,jdbcTemplate); 
//
//		}

		
		public ResponseEntity<Object> getSample(int ntestcode, int nbatchmastercode, UserInfo userInfo, int nregtypecode,
				int nregsubtypecode, int addSampleID, int napprovalconfigversioncode, int nprojectmastercode,
				boolean nneedmyjob) throws Exception {
			// ALPD-5535 - commented by Gowtham R - unwanted key jsondata and jsonuidata sent to UI because of POJO class instead using List<Map>.
//			List<Registration> lstSample = new ArrayList<>();
			List<Map<String,Object>> lstSample = new ArrayList<>();
			Map<String, Object> returnMap = new HashMap<>();
			//String toCheckAccepted = "";
			String ntestrescheduleno = "";

			final Batchmaster batchmaster = (Batchmaster) getActivBatch(nbatchmastercode, userInfo);
			if (batchmaster == null) {
				// status code:417
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {

				List<TransactionValidation> validationStatusLst = (List<TransactionValidation>) getTransactionValidation(
						addSampleID, nregtypecode, nregsubtypecode, userInfo);
				final String stransactionStatus = validationStatusLst.stream()
						.map(objpreregno -> String.valueOf(objpreregno.getNtransactionstatus()))
						.collect(Collectors.joining(","));
				if (!stransactionStatus.isEmpty()) {

					// Before Tuning

//			String ssampleQuery = " select rt.ntransactiontestcode,rt.npreregno,rarno.sarno,"
//					+ " s.ssectionname,rsarno.ssamplearno,rs.ntransactionsamplecode,"
//					+ " tm.stestname ||  '['||CAST(rt.ntestrepeatno AS character varying(10))||']"
//					+ " ['||CAST(rt.ntestretestno AS character varying(10))||']' stestname "
//					+ " from registrationtest rt,"
//					+ " registration r,registrationsample rs,"
//					+ " registrationarno rarno,registrationsamplearno rsarno,testmaster tm ,section s"
//					+ " where rt.ntransactiontestcode =any(select max(rt.ntransactiontestcode)"
//					+ " from registrationtest rt, registrationtesthistory rth "
//					+ " where rth.ntesthistorycode=any(select max(rth.ntesthistorycode)"
//					+ " from registrationtesthistory rth,registrationtest rt "
//					+ " where rt.ntransactiontestcode=rth.ntransactiontestcode"
//					+ " and rt.nsitecode="+userInfo.getNtranssitecode()+""
//					+ " and rth.nsitecode="+userInfo.getNtranssitecode()+""
//					+ " and rt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and rth.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " group by rth.ntransactiontestcode) "
//					+ " and rt.ntestcode="+ntestcode+" and tm.ntestcode=rt.ntestcode "
//					+ " and rt.ntransactiontestcode=rth.ntransactiontestcode "
//					+ " and rth.ntransactionstatus= ("+stransactionStatus+")"
//					+ " and rt.nsitecode="+userInfo.getNtranssitecode()+""
//					+ " and rth.nsitecode="+userInfo.getNtranssitecode()+""
//					+ " and rt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and rth.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " group by rt.ntransactiontestcode,rt.npreregno,rt.ntransactionsamplecode "
//					+ ")"
//					+ " and rt.ninstrumentcatcode > 0 "
//					+ " and rt.nsectioncode = s.nsectioncode "
//					+ " and rt.ntransactiontestcode not in (select ntransactiontestcode from batchsample "
//					+ " where "
//					+ " nbatchmastercode = "+nbatchmastercode+""
//					+ " and nsitecode = "+userInfo.getNtranssitecode()+""
//					+ " and  nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+") "
//					+ " and rt.ntransactiontestcode not in(select bs.ntransactiontestcode from batchsample bs,batchhistory bh "
//				    + " where bs.nbatchmastercode=bh.nbatchmastercode "
//				    //+ " and bs.nbatchmastercode = "+nbatchmastercode+""
//				    + " and bs.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//				    + " and bh.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//				    + " and bs.nsitecode="+userInfo.getNtranssitecode()+""
//				    + " and bh.nsitecode="+userInfo.getNtranssitecode()+""
//				    + " and bh.ntransactionstatus="+Enumeration.TransactionStatus.INITIATED.gettransactionstatus()+")"
//					+ " and r.nregtypecode="+nregtypecode+" and r.nregsubtypecode="+nregsubtypecode+""
//					+ " and r.nisiqcmaterial != "+Enumeration.TransactionStatus.YES.gettransactionstatus()+""
//					+ " and rt.npreregno=r.npreregno "
//					+ " and rt.ntransactionsamplecode =rs.ntransactionsamplecode "
//					+ " and rarno.npreregno=r.npreregno "
//					+ " and rs.npreregno=r.npreregno "
//					+ " and rsarno.ntransactionsamplecode=rs.ntransactionsamplecode "
//					+ " and rarno.npreregno=r.npreregno "
//					+ " and rt.nsitecode="+userInfo.getNtranssitecode()+""
//					+ " and r.nsitecode="+userInfo.getNtranssitecode()+""
//					+ " and rs.nsitecode="+userInfo.getNtranssitecode()+""
//					+ " and rarno.nsitecode="+userInfo.getNtranssitecode()+""
//					+ " and rsarno.nsitecode="+userInfo.getNtranssitecode()+""
//					+ " and tm.nsitecode="+userInfo.getNmastersitecode()+""
//					+ " and s.nsitecode= "+userInfo.getNmastersitecode()+""
//					+ " and s.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and rt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and r.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and rs.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and rarno.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and rsarno.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and tm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " order by rt.npreregno desc";

					// After Tuning

//					if (nneedmyjob) {
//						toCheckAccepted = " and rth.nusercode = " + userInfo.getNusercode() + "";
//					}
					ntestrescheduleno = "select max(jb.njoballocationcode) from joballocation jb,registrationtest rt1 "
							+ " where  jb.ntestrescheduleno = any(select max(ntestrescheduleno) from joballocation "
							+ " where nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
							+ " and nsitecode = " + userInfo.getNtranssitecode() + "" + " group by ntransactiontestcode)"
							+ " and rt1.ntransactiontestcode =jb.ntransactiontestcode and jb.nsectioncode = rt1.nsectioncode "
							+ " and jb.nsitecode = " + userInfo.getNtranssitecode() + "" + " and jb.nusercode in("
							+ userInfo.getNusercode() + ",-1)" // Added by sonia on 7th Aug 2024 for JIRA ID:ALPD-4544
							+ " and jb.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
							+ " and rt1.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
							+ " and rt1.nsitecode=" + userInfo.getNtranssitecode() + ""
							+ " group by jb.ntransactiontestcode";
				
					//ALPD-ALPD-5137--Vignesh R(18-12-2024)--RegiteredDate showing the Grid.
					final String withQuery="with registeredDate "
							+ "as(select to_char(dtransactiondate, '"+userInfo.getSpgsitedatetime().replace("'T'", " ")+"') "
									+ "as dregdate,npreregno from registrationhistory where ntransactionstatus="+Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()+")";	
					
					// Modified the Select Query by sonia on 6th Sep 2024 for JIRA ID:4761
					String ssampleQuery = withQuery+" select rt.ntransactiontestcode,rt.npreregno,rarno.sarno,"
							+ " s.ssectionname,rsarno.ssamplearno,rs.ntransactionsamplecode,"
							+ " tm.stestname ||  '['||CAST(rt.ntestrepeatno AS character varying(10))||']"
							+ " ['||CAST(rt.ntestretestno AS character varying(10))||']' stestname,case  when rs.jsonuidata->>'scomponentname'  is null then rs.jsonuidata->>'Sample Name_child' else rs.jsonuidata->>'scomponentname'  end ssamplename,regDate.dregdate "
							+ " from registrationtest rt,joballocation jb,"
							+ " registration r,registrationsample rs,registrationtesthistory rth,"
							+ " registrationarno rarno,registrationsamplearno rsarno,testmaster tm ,section s,batchmaster bm,product p,registeredDate regDate"
							+ " where rth.ntesthistorycode=any(select max(rth.ntesthistorycode)"
							+ " from registrationtesthistory rth,registrationtest rt "
							+ " where rt.ntransactiontestcode=rth.ntransactiontestcode" + " and rt.nsitecode="
							+ userInfo.getNtranssitecode() + "" + " and rth.nsitecode=" + userInfo.getNtranssitecode() + ""
							// + " "+toCheckAccepted+"" //Commented by sonia on 7th Aug 2024 for JIRA
							// ID:ALPD-4544
							// + " and rth.ntransactionstatus= ("+stransactionStatus+")"
							// + " and rth.nusercode = "+userInfo.getNusercode()+""
							+ " and rt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
							+ " and rth.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
							+ " group by rth.ntransactiontestcode) " + " and rt.ntestcode=" + ntestcode + ""
							+ " and jb.npreregno=r.npreregno" + " and jb.njoballocationcode = any(" + ntestrescheduleno
							+ ")" + " and bm.nproductcode=r.nproductcode "
							+ " and jb.ntransactiontestcode=rt.ntransactiontestcode"
							+ " and jb.nsectioncode=rt.nsectioncode"
							// + " and jb.nusercode in("+userInfo.getNusercode()+",-1)" //Commented by sonia
							// on 7th Aug 2024 for JIRA ID:ALPD-4544
							+ " and tm.ntestcode=rt.ntestcode " + " and rt.ntransactiontestcode=rth.ntransactiontestcode "
							+ " and rth.ntransactionstatus = (" + stransactionStatus + ")" + " and rt.nsitecode="
							+ userInfo.getNtranssitecode() + "" + " and rth.nsitecode=" + userInfo.getNtranssitecode() + ""
							+ " and rt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
							+ " and rth.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
							+ " and rt.ninstrumentcatcode >= " + Enumeration.TransactionStatus.NA.gettransactionstatus()
							+ "" + " and rt.nsectioncode = s.nsectioncode "
							+ " and rt.ntransactiontestcode not in (select ntransactiontestcode from batchsample "
							+ " where " + " nbatchmastercode = " + nbatchmastercode + "" + " and nsitecode = "
							+ userInfo.getNtranssitecode() + "" + " and  nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") "
							+ " and rt.ntransactiontestcode not in(select bs.ntransactiontestcode from batchsample bs,batchhistory bh "
							+ " where bs.nbatchmastercode=bh.nbatchmastercode "
							// + " and bs.nbatchmastercode = "+nbatchmastercode+""
							+ " and bs.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
							+ " and bh.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
							+ " and bs.nsitecode=" + userInfo.getNtranssitecode() + "" + " and bh.nsitecode="
							+ userInfo.getNtranssitecode() + "  and bs.nbatchmastercode ="+nbatchmastercode+"   and bh.ntransactionstatus="
							+ Enumeration.TransactionStatus.INITIATED.gettransactionstatus() + ")"
							+ " and bm.nbatchmastercode = " + nbatchmastercode + " and r.nregtypecode=" + nregtypecode
							+ " and r.nregsubtypecode=" + nregsubtypecode + "" + " and r.nisiqcmaterial != "
							+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ""
							+ " and(r.jsonuidata->'napproveconfversioncode')::int=" + napprovalconfigversioncode + ""
							+ " and rt.npreregno=r.npreregno "
							+"  and regDate.npreregno=r.npreregno "
							+"  and p.nproductcode=r.nproductcode "
							+ " and rt.ntransactionsamplecode =rs.ntransactionsamplecode "
							+ " and rarno.npreregno=r.npreregno " + " and rs.npreregno=r.npreregno"
							+ " and rsarno.ntransactionsamplecode=rs.ntransactionsamplecode "
							+ " and rarno.npreregno=r.npreregno " + " and r.nprojectmastercode=" + nprojectmastercode + ""
							+ " and rt.nsitecode=" + userInfo.getNtranssitecode() + "" + " and r.nsitecode="
							+ userInfo.getNtranssitecode() + "" + " and rs.nsitecode=" + userInfo.getNtranssitecode() + ""
							+ " and rarno.nsitecode=" + userInfo.getNtranssitecode() + "" + " and rsarno.nsitecode="
							+ userInfo.getNtranssitecode() + "" + " and tm.nsitecode=" + userInfo.getNmastersitecode() + ""
							+ " and s.nsitecode= " + userInfo.getNmastersitecode() + "" + " and jb.nsitecode="
							+ userInfo.getNtranssitecode() + "" + " and jb.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and s.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rt.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and r.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rs.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rarno.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rsarno.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and tm.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
							+ " order by rt.npreregno,rt.ntransactionsamplecode,rt.ntransactiontestcode asc";

//			String ssampleQuery = "  select rarno.npreregno,rarno.sarno,rsarno.ssamplearno,rsarno.ntransactionsamplecode, rt.ntransactiontestcode,"
//					+ " tm.stestname ||  '['||CAST(rt.ntestrepeatno AS character varying(10))||'] ['||CAST(rt.ntestretestno AS character varying(10))||']' stestname "
//					+ " from registrationtest rt,registration r, registrationsample rs,"
//					+ " registrationarno rarno,registrationsamplearno rsarno,"
//					+ " testmaster tm,  registrationtesthistory rth ,registrationhistory rh  where "
//					+ " r.npreregno=rs.npreregno and r.npreregno=rh.npreregno "
//					+ " and rarno.npreregno=r.npreregno "
//					+ " and tm.ntestcode=rt.ntestcode "
//					+ " and rsarno.npreregno=rs.npreregno"
//					+ " and rth.ntransactiontestcode = rt.ntransactiontestcode "
//					+ " and r.nregtypecode="+nregtypecode+" and r.nregsubtypecode="+nregsubtypecode+""
//					+ " and r.nmaterialtypecode != "+Enumeration.TransactionStatus.NO.gettransactionstatus()+""
//					+ " and rs.ntransactionsamplecode=rsarno.ntransactionsamplecode "
//					+ " and rt.npreregno=r.npreregno "
//					+ " and rth.ntesthistorycode = any(select max(rth.ntesthistorycode)"
//					+ " from registrationtesthistory rth,registrationtest rt "
//					+ " where rt.ntestcode="+ntestcode+" "
//					+ " and rt.ntransactiontestcode=rth.ntransactiontestcode "
//					+ " and rth.nsitecode="+userInfo.getNtranssitecode()+""
//					+ " and rt.nsitecode="+userInfo.getNtranssitecode()+""
//					+ " and rth.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and rt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " group by rth.ntransactiontestcode) "
//					+ " and rh.nreghistorycode = any(select max(rh.nreghistorycode) from registrationhistory rh,registration r"
//					+ " where r.npreregno=rh.npreregno "
//					+ " and rh.nsitecode="+userInfo.getNtranssitecode()+" "
//					+ " and r.nsitecode="+userInfo.getNtranssitecode()+""
//					+ " and rh.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
//					+ " and r.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " group by rh.npreregno) "
//					+ " and rth.ntransactionstatus in ("+stransactionStatus+")"
//		//			+ " and rth.ntransactionstatus between "+Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()+" "
//		//     		+ " and "+Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()+""
//		     		+ " and rt.ntransactiontestcode not in (select ntransactiontestcode from batchsample "
//		     		+ " where nbatchmastercode = "+nbatchmastercode+""
//		     		+ " and nsitecode="+userInfo.getNtranssitecode()+""
//		     		+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")"
//		     		+ " and rt.ntransactiontestcode not in(select bs.ntransactiontestcode from batchsample bs,batchhistory bh "
//		     		+ " where bs.nbatchmastercode=bh.nbatchmastercode  "
//		     		+ " and bs.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//		     		+ " and bh.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//		     		+ " and bs.nsitecode="+userInfo.getNtranssitecode()+""
//		     		+ " and bh.nsitecode="+userInfo.getNtranssitecode()+""
//		     		+ " and bh.ntransactionstatus="+Enumeration.TransactionStatus.INITIATED.gettransactionstatus()+")"
//					+ " and rt.ntransactionsamplecode=rs.ntransactionsamplecode "
//					+ " and r.nsitecode="+userInfo.getNtranssitecode()+""
//					+ " and rs.nsitecode="+userInfo.getNtranssitecode()+""
//					+ " and rt.nsitecode="+userInfo.getNtranssitecode()+""
//					+ " and rth.nsitecode="+userInfo.getNtranssitecode()+""
//					+ " and rh.nsitecode="+userInfo.getNtranssitecode()+""
//					+ " and rarno.nsitecode="+userInfo.getNtranssitecode()+""
//					+ " and rsarno.nsitecode="+userInfo.getNtranssitecode()+""
//					+ " and tm.nsitecode="+userInfo.getNmastersitecode()+""
//					+ " and r.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and rs.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and rt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and rth.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and rh.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and rarno.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and rsarno.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and tm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";

					// ALPD-5535 - commented by Gowtham R - unwanted key jsondata and jsonuidata sent to UI because of POJO class instead using List<Map>.
//					lstSample = getJdbcTemplate().query(ssampleQuery, new Registration());
					lstSample = jdbcTemplate.queryForList(ssampleQuery);

					if (!lstSample.isEmpty()) {
						returnMap.put("selectedSamples", lstSample.get(0));
					} else {
						returnMap.put("selectedSamples", null);
					}
					returnMap.put("samples", lstSample);
					return new ResponseEntity<>(returnMap, HttpStatus.OK);
				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_NOSAMPLETOADD", userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			}

		}

		@Override
		public ResponseEntity<Object> getActiveSelectedBatchmaster(int nbatchmastercode, UserInfo userInfo,
				int ndesigntemplatemappingcode, int nsampletypecode) throws Exception {

			Map<String, Object> returnMap = new HashMap<>();
			List<Batchmaster> lstBatchmaster = new ArrayList<>();
			String stablename = "";
			String fieldname = "";
			String selectField = "";

			final Batchmaster batchmaster = (Batchmaster) getActivBatch(nbatchmastercode, userInfo);
			if (batchmaster == null) {
				// status code:417
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()) {
					stablename = "component";
					fieldname = "ncomponentcode";
					selectField = "scomponentname";
				} else {
					stablename = "product";
					fieldname = "nproductcode";
					selectField = "sproductname";
				}

				String sbtachQuery = "select bm.nbatchmastercode,bm.ntestcode,bh.sbatcharno, bm.nprojectmastercode,"
						+ " case when nsampletypecode =" + Enumeration.SampleType.PROJECTSAMPLETYPE.getType()
						+ " then pm.sprojectcode else 'NA' end sprojectcode,"
						+ " bh.scomments,s.ssectionname,bm.nsectioncode, bm.ninstrumentcode,"
						+ " bh.ntransactionstatus,to_char(bh.dtransactiondate,'" + userInfo.getSsitedate()
						+ "') as stransactiondate," + " CONCAT(u.sfirstname,' ',u.slastname) as username,"
						+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
						+ "', ts.jsondata->'stransdisplaystatus'->>'en-US') stransdisplaystatus ," + " tm.stestname,"
						+ " case when bm.sinstrumentid='null' then 'NA' else bm.sinstrumentid end sinstrumentid,"
						+ " i.sinstrumentname,ic.sinstrumentcatname,p." + selectField + " sproductname "
						+ " from batchmaster bm,projectmaster pm,testmaster tm,instrument i,instrumentcategory ic," + " "
						+ stablename + " p,batchhistory bh,transactionstatus ts,users u,section s "
						+ " where tm.ntestcode=bm.ntestcode and s.nsectioncode=bm.nsectioncode "
						+ " and bm.nprojectmastercode=pm.nprojectmastercode and ic.ninstrumentcatcode=bm.ninstrumentcatcode "
						+ " and p." + fieldname + "=bm.nproductcode" + " and bh.nbatchmastercode=bm.nbatchmastercode"
						+ " and ts.ntranscode=bh.ntransactionstatus" + " and i.ninstrumentcatcode=ic.ninstrumentcatcode"
						+ " and u.nusercode = bh.nusercode " + " and bm.ninstrumentcode=i.ninstrumentcode "
						+ " and bh.nbatchhistorycode = any(select max(bh.nbatchhistorycode) from batchmaster bm, batchhistory bh "
						+ " where bh.nbatchmastercode = bm.nbatchmastercode " + " and bm.nsitecode="
						+ userInfo.getNtranssitecode() + "" + " and bh.nsitecode=" + userInfo.getNtranssitecode() + ""
						+ " and bm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " and bh.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " group by bh.nbatchmastercode)" + " and bm.nsitecode=" + userInfo.getNtranssitecode() + ""
						+ " and u.nsitecode=" + userInfo.getNmastersitecode() + "" + " and bh.nsitecode="
						+ userInfo.getNtranssitecode() + "" + " and tm.nsitecode=" + userInfo.getNmastersitecode() + ""
						+ " and i.nsitecode= " + userInfo.getNmastersitecode() + "" + " and ic.nsitecode="
						+ userInfo.getNmastersitecode() + "" + " and p.nsitecode=" + userInfo.getNmastersitecode() + ""
						+ " and s.nsitecode=" + userInfo.getNmastersitecode() + "" + " and pm.nsitecode="
						+ userInfo.getNtranssitecode() + "" + " and bm.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and pm.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and u.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and bh.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and ts.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and tm.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and i.nstatus= "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and ic.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and p.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and s.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and bm.nbatchmastercode = "
						+ nbatchmastercode + "";

				lstBatchmaster = jdbcTemplate.query(sbtachQuery, new Batchmaster());
				if (!lstBatchmaster.isEmpty()) {
					returnMap.put("SelectedBatchmaster", lstBatchmaster.get(0));
					returnMap.put("nbatchmastercode", lstBatchmaster.get(0).getNbatchmastercode());
					returnMap.put("ntestcode", lstBatchmaster.get(0).getNtestcode());
				}

				returnMap.putAll((Map<String, Object>) getSampleTabDetails(nbatchmastercode, userInfo, ndesigntemplatemappingcode)
								.getBody());
				returnMap.putAll((Map<String, Object>) getBatchhistory(nbatchmastercode, userInfo).getBody());
				returnMap.putAll((Map<String, Object>) getBatchIqcSampleAction(returnMap, userInfo).getBody());
				returnMap.putAll((Map<String, Object>) getTestDetails(returnMap, userInfo).getBody());
				returnMap.putAll((Map<String, Object>) getBatchTAT(returnMap, userInfo).getBody());

				return new ResponseEntity<>(returnMap, HttpStatus.OK);

			}
		}

		@Override
		public ResponseEntity<Object> createSample(final int seqNo, final List<Batchsample> batchsample, UserInfo userInfo,
				int ndesigntemplatemappingcode, int nregtypecode, int nregsubtypecode) throws Exception {
			// TODO Auto-generated method stub
			Map<String, Object> returnMap = new HashMap<>();
			ObjectMapper objmap = new ObjectMapper();
			String partQuery = "";
			//List<Short> existingTransactionList = new ArrayList<Short>();
			int nbatchsampleCode = seqNo;
			//int nbatchSeqno = 0;

			Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
			JSONObject actionType = new JSONObject();
			JSONObject jsonAuditObject = new JSONObject();
			
			// ALPD-5548 - added by Gowtham in 12/03/2025 - Run Batch Creation --> While Add Samples in multi tab no alert is thrown and duplicate record is inserted.
			//start
			String ntransactiontestcode =stringUtilityFunction.fnDynamicListToString(batchsample,"getNtransactiontestcode");
			String npreregno =stringUtilityFunction.fnDynamicListToString(batchsample,"getNpreregno");
			String ntransactionsamplecode =stringUtilityFunction.fnDynamicListToString(batchsample,"getNtransactionsamplecode");
			
			final String validateQuery = "select nbatchsamplecode, jsondata from batchsample where nbatchmastercode ="
					+ batchsample.get(0).getNbatchmastercode() + " and ntransactiontestcode in ("+ ntransactiontestcode +") "
					+ "and npreregno in ("+ npreregno +") and ntransactionsamplecode in ("+ ntransactionsamplecode +") "
					+ "and nsitecode = " + userInfo.getNsitecode() + " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
			
			final List<Map<String,Object>> batchsampleList = jdbcTemplate.queryForList(validateQuery);
					
			if(batchsampleList.isEmpty()) {
			//end
				String queryString = "INSERT INTO batchsample(nbatchsamplecode,nbatchmastercode,"
						+ "	ntransactiontestcode,npreregno,ntransactionsamplecode,jsondata,jsonuidata,nsitecode,nstatus) "
						+ " VALUES ";
				
				for (Batchsample newSampleList : batchsample) {
					
					newSampleList.setNbatchsamplecode(nbatchsampleCode + 1);
					// nbatchSeqno = nbatchsampleCode;
					partQuery += "(" + newSampleList.getNbatchsamplecode() + "," + newSampleList.getNbatchmastercode() + ","
							+ newSampleList.getNtransactiontestcode() + "," + "" + newSampleList.getNpreregno() + ","
							+ newSampleList.getNtransactionsamplecode() + "," + "'"
							+ stringUtilityFunction.replaceQuote(objmap.writeValueAsString(newSampleList.getJsondata())) + "'," + "'"
							+ stringUtilityFunction.replaceQuote(objmap.writeValueAsString(newSampleList.getJsonuidata())) + "'," + ""
							+ userInfo.getNsitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ "),";
					
					nbatchsampleCode = nbatchsampleCode + 1;
				}
				
				if (!partQuery.isEmpty()) {
					jdbcTemplate.execute(queryString + partQuery.substring(0, partQuery.length() - 1));
				}
				
				returnMap.putAll((Map<String, Object>) getSampleTabDetails(batchsample.get(0).getNbatchmastercode(), userInfo,
						ndesigntemplatemappingcode).getBody());
				
//				     	String sQuery = " select npreregno,ntransactionsamplecode,ntransactiontestcode,nbatchmastercode, "
//				     	+ " jsonuidata,jsondata"
//				     	+ " from batchsample where nbatchmastercode=9  and nstatus=1  and nsitecode=1";
				
				auditmap.put("nregtypecode", nregtypecode);
				auditmap.put("nregsubtypecode", nregsubtypecode);
				auditmap.put("ndesigntemplatemappingcode", ndesigntemplatemappingcode);
				actionType.put("batchsample", "IDS_CREATEBATCHSAMPLE");
				jsonAuditObject.put("batchsample", batchsample);
				auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);
				
				return new ResponseEntity<>(returnMap, HttpStatus.OK);
				
			// ALPD-5548 - added by Gowtham in 12/03/2025 - Run Batch Creation --> While Add Samples in multi tab no alert is thrown and duplicate record is inserted.
			} else {
				final String sarno = batchsampleList.stream()
						.map(objbatch -> {
							final Object jsonDataObj = objbatch.get("jsondata");
							final JSONObject json = new JSONObject(jsonDataObj.toString());
							final Object sampleDataObj = json.get("samplelist");
							final JSONObject samplelist = new JSONObject(sampleDataObj.toString());
							return samplelist.get("ssamplename")==JSONObject.NULL ? String.valueOf(samplelist.get("sarno")) 
									: String.valueOf(samplelist.get("ssamplearno"));
						}).collect(Collectors.joining(","));
				final String sAlert = sarno + " " + commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename());

				return new ResponseEntity<>(sAlert, HttpStatus.EXPECTATION_FAILED);
			}
		}

		public Map<String, Object> getBatchSampleCode(final int totalcount, final UserInfo userInfo) throws Exception {
			final String sQuery = " lock table lockbatchsample " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			final Map<String, Object> mapSeq = new HashMap<String, Object>();

			final String seqFormatQuery = "select nsequenceno from seqnobatchcreation where stablename='batchsample'"
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

			SeqNoBatchcreation objSeq = (SeqNoBatchcreation)jdbcUtilityFunction.queryForObject(seqFormatQuery, SeqNoBatchcreation.class,jdbcTemplate); 
			int startSeqNo = objSeq.getNsequenceno();
			int endseqno = startSeqNo + totalcount;

			final String strUpdate = "update seqnobatchcreation set nsequenceno = " + endseqno
					+ " where stablename='batchsample'"
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

			jdbcTemplate.execute(strUpdate);

			mapSeq.put("nbatchsamplecode", startSeqNo);
			mapSeq.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
			return mapSeq;
		}

		
		@Override
		public ResponseEntity<Object> getSampleTabDetails(int batchmastercode, UserInfo userInfo,
				int ndesigntemplatemappingcode) throws Exception {
			List<Map<String, Object>> lstSampletabDetials = new ArrayList<>();
			final Map<String, Object> returnMap = new HashMap<String, Object>();
			String sQuery = " select json_agg(a.jsonuidata)::jsonb as jsonuidata from "
					+ " (select r.jsonuidata || rs.jsonuidata ||  "
					+ " json_build_object('ntransactionsamplecode',bs.ntransactionsamplecode)::jsonb||"
					+ " json_build_object('nbatchsamplecode',bs.nbatchsamplecode)::jsonb||"
					+ " json_build_object('nbatchmastercode',bs.nbatchmastercode)::jsonb||"
					+ " json_build_object('sarno',bs.jsondata->'samplelist'->>'sarno')::jsonb||"
					+ " json_build_object('ntransactiontestcode',bs.ntransactiontestcode)::jsonb||"
					+ " json_build_object('ntestcode',rt.ntestcode)::jsonb||"
					+ " json_build_object('ssamplearno',bs.jsondata->'samplelist'->>'ssamplearno')::jsonb||"
					+ " json_build_object('stestname',bs.jsondata->'samplelist'->>'stestname')::jsonb||"
					+ " json_build_object('ssectionname',bs.jsondata->'samplelist'->>'ssectionname')::jsonb||"
					// ALPD-5590 - added by Gowtham R - added registrationDate&time and Analyzer Name field for sampleDetail getService.
					+ " json_build_object('dregdate', bs.jsondata -> 'samplelist' ->> 'dregdate')::jsonb||"
					+ " json_build_object('AnalyserName',COALESCE((rt.jsondata ->> 'AnalyserName'),'-'))::jsonb||"
					+ " json_build_object('stestsynonym',tm.stestsynonym)::jsonb as jsonuidata"
					+ " from batchsample bs,registrationarno rarno,registration r,registrationsample rs,"
					+ " registrationsamplearno rsarno,registrationtest rt,section s," + " testmaster tm "
					+ " where bs.nbatchmastercode=" + batchmastercode + ""
					+ " and rarno.npreregno = bs.npreregno and r.npreregno=bs.npreregno and rs.ntransactionsamplecode=bs.ntransactionsamplecode"
					+ " and s.nsectioncode=rt.nsectioncode and rsarno.ntransactionsamplecode=bs.ntransactionsamplecode "
					+ " and rt.ntransactiontestcode=bs.ntransactiontestcode" + " and tm.ntestcode=rt.ntestcode "
					+ " and bs.nsitecode=" + userInfo.getNtranssitecode() + "" + " and s.nsitecode="
					+ userInfo.getNmastersitecode() + "" + " and r.nsitecode=" + userInfo.getNtranssitecode() + ""
					+ " and rs.nsitecode=" + userInfo.getNtranssitecode() + "" + " and rarno.nsitecode="
					+ userInfo.getNtranssitecode() + "" + " and rsarno.nsitecode=" + userInfo.getNtranssitecode() + ""
					+ " and rt.nsitecode=" + userInfo.getNtranssitecode() + "" + " and tm.nsitecode="
					+ userInfo.getNmastersitecode() + "" + " and s.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and bs.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and r.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rs.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rarno.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rsarno.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					// +" and
					// tm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"
					// order by bs.nbatchsamplecode desc )a";
					+ " and tm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " order by rt.npreregno,rt.ntransactionsamplecode,rt.ntransactiontestcode asc )a";
			String sampleListString = jdbcTemplate.queryForObject(sQuery, String.class);
			// lstSampletabDetials = getJdbcTemplate().query(sQuery, new Batchsample());
			if (sampleListString != null) {
				
//				Object result = projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(
//					    sampleListString, userInfo, true, ndesigntemplatemappingcode, "Samples"
//					);
//
//					if (result instanceof List<?>) {
//					    lstSampletabDetials = ((List<?>) result).stream()
//					        .filter(Batchsample.class::isInstance)
//					        .map(Batchsample.class::cast)
//					        .collect(Collectors.toList());
//					}
				
				
				lstSampletabDetials = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(sampleListString, userInfo,
						true, ndesigntemplatemappingcode, "Samples");
				returnMap.put("selectedSamples", lstSampletabDetials);
			}
			
			
			returnMap.put("Samples", lstSampletabDetials);

			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		}

		@SuppressWarnings("unchecked")
		@Override
		public ResponseEntity<Object> deleteSample(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
			// TODO Auto-generated method stub
			ObjectMapper objmap = new ObjectMapper();
			Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
			JSONObject actionType = new JSONObject();
			JSONObject jsonAuditObject = new JSONObject();
			int nbatchsamplecode = (int) inputMap.get("nbatchsamplecode");
			int nbatchmastercode = (int) inputMap.get("nbatchmastercode");
			int ndesigntemplatemappingcode = (int) inputMap.get("ndesigntemplatemappingcode");
			Map<String, Object> returnMap = new HashMap<>();
			final Batchsample batchsample = (Batchsample) getActivSampleById(nbatchsamplecode, userInfo);
			Batchsample objbatchsample = objmap.convertValue(inputMap.get("Sample"), new TypeReference<Batchsample>() {
			});

			if (batchsample == null) {
				// status code:417
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {

				final BatchHistory batchhist = (BatchHistory) getCheckValidation(nbatchmastercode, userInfo);

				if (batchhist.getNtransactionstatus() == Enumeration.TransactionStatus.INITIATED.gettransactionstatus()
						|| batchhist.getNtransactionstatus() == Enumeration.TransactionStatus.COMPLETED
								.gettransactionstatus()) {

					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.SELECTDRAFTVERSION.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);

				} else {

					final String updateQueryString = "update batchsample set nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "" + " where nbatchsamplecode="
							+ nbatchsamplecode + "";

					jdbcTemplate.execute(updateQueryString);

					returnMap.putAll((Map<String, Object>) getSampleTabDetails(nbatchmastercode, userInfo,
							ndesigntemplatemappingcode).getBody());

				}
				List<Batchsample> lstbatchsample = new ArrayList<>();
				lstbatchsample.add(objbatchsample);
				auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
				auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
				auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
				actionType.put("batchsample", "IDS_DELETESAMPLE");
				jsonAuditObject.put("batchsample", lstbatchsample);
				auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);
				return new ResponseEntity<>(returnMap, HttpStatus.OK);
			}

		}

		private Batchsample getActivSampleById(int nbatchsamplecode, UserInfo userInfo) throws Exception {
			final String strQuery = " select * from batchsample bs " + " where bs.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and bs.nsitecode = "
					+ userInfo.getNtranssitecode() + "" + " and bs.nbatchsamplecode =" + nbatchsamplecode + "";
			return (Batchsample) jdbcUtilityFunction.queryForObject(strQuery, Batchsample.class,jdbcTemplate); 
		}

		private List<Batchsample> getActiveMultipleSampleById(String nbatchsamplecode, UserInfo userInfo) throws Exception {
			final String strQuery = " select * from batchsample bs " + " where bs.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and bs.nsitecode = "
					+ userInfo.getNtranssitecode() + "" + " and bs.nbatchsamplecode in (" + nbatchsamplecode + ")";
			List<Batchsample> lstBatchSample = (List<Batchsample>) jdbcTemplate.query(strQuery, new Batchsample());
			return lstBatchSample;
		}

		
		private List<Batchsample> getActivSampleByIdToCheckData(int nbatchmastercode, UserInfo userInfo) throws Exception {
			final String strQuery = " select * from batchsample bs " + " where bs.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and bs.nsitecode = "
					+ userInfo.getNtranssitecode() + "" + " and bs.nbatchmastercode =" + nbatchmastercode + "";
			// return (List<Batchsample>) jdbcTemplate.query(strQuery, Batchsample.class);
			List<Batchsample> lstBatchSample = (List<Batchsample>) jdbcTemplate.query(strQuery, new Batchsample());
			return lstBatchSample;
		}

		
		private List<BatchsampleIqc> getActivBatchIQCSampleById(int nbatchmastercode, UserInfo userInfo) throws Exception {
			final String strQuery = " select * from batchsampleiqc bsi " + " where bsi.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and bsi.nsitecode = "
					+ userInfo.getNtranssitecode() + "" + " and bsi.nbatchmastercode =" + nbatchmastercode + "";
			// return (List<Batchsampleiqc>) jdbcQueryForObject(strQuery,
			// Batchsample.class);
			List<BatchsampleIqc> lstBatchSample = (List<BatchsampleIqc>) jdbcTemplate.query(strQuery, new BatchsampleIqc()); 
			return lstBatchSample;
		}

		private BatchsampleIqc getActiveIQCSampleById(int nbatchsampleiqccode, UserInfo userInfo) throws Exception {
			final String strQuery = " select * from batchsampleiqc bsi " + " where bsi.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and bsi.nsitecode = "
					+ userInfo.getNtranssitecode() + "" + " and bsi.nbatchsampleiqccode =" + nbatchsampleiqccode + "";
			return (BatchsampleIqc) jdbcUtilityFunction.queryForObject(strQuery, BatchsampleIqc.class,jdbcTemplate); 
		}

		@SuppressWarnings("unchecked")
		@Override
		public ResponseEntity<Object> initiateBatchcreation(final Map<String, Object> seqNoList,
				final List<Batchsample> batchSampleList, final Map<String, Object> inputMap, final UserInfo userInfo)
				throws Exception {
			// TODO Auto-generated method stub
			final String sQuery = " lock table lockbatchcreation " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);
			int nbatchmastercode = (int) inputMap.get("nbatchmastercode");
			int ntesthistorycode = (int) seqNoList.get("registrationtesthistory");
			String stransactiontestcode = (String) inputMap.get("ntransactiontestcode");
			Boolean nneedsubsample = (Boolean) inputMap.get("nneedsubsample");
			Boolean nneedtestinitiate = (Boolean) inputMap.get("nneedtestinitiate");
			Boolean nneedjoballocationiqc = (Boolean) inputMap.get("nneedjoballocationiqc");
			int ntestcode = (int) inputMap.get("ntestcode");
			//Boolean ischeckedvalidation = false;
			int nregtypecode = (int) inputMap.get("nregtypecode");
			int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
			int testStartId = (int) inputMap.get("testStartId");
			Boolean isiqcdata = (Boolean) inputMap.get("isiqcdata");
			int nMultiUserStatus = 0;
			ObjectMapper objmap = new ObjectMapper();
			Map<String, Object> returnMap = new HashMap<>();
			Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
			JSONObject actionType = new JSONObject();
			JSONObject jsonAuditObject = new JSONObject();
			String batchiqctransaction = "";
			List<RegistrationTest> lstSampleCheck = new ArrayList<>();

			final Batchmaster batchmaster = (Batchmaster) getActivBatch(nbatchmastercode, userInfo);
			String sNbatchsampleCode = (String) inputMap.get("nbatchsampleCode");
//				List<String> listOfBatchsampleCod = Arrays.asList(sNbatchsampleCode.split(","));
//				Collections.sort(listOfBatchsampleCod);
			final List<Batchsample> lstbatchsample = (List<Batchsample>) getActiveMultipleSampleById(sNbatchsampleCode,
					userInfo);
			if (batchmaster == null) {
				// status code:417
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else if (lstbatchsample.size() == 0) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SAMPLEALREADYDELETED", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);

			} else {

				final BatchHistory batchhist = (BatchHistory) getCheckValidation(nbatchmastercode, userInfo);
				List<BatchsampleIqc> batchSampleInputList = objmap.convertValue(inputMap.get("iqcsample"),
						new TypeReference<List<BatchsampleIqc>>() {
						});
				if (batchhist.getNtransactionstatus() == Enumeration.TransactionStatus.INITIATED.gettransactionstatus()
						|| batchhist.getNtransactionstatus() == Enumeration.TransactionStatus.COMPLETED
								.gettransactionstatus()) {

					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.SELECTDRAFTVERSION.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				} else {
					Map<String, Object> schecktechniqueValidation = getCheckTechnique(ntestcode, userInfo);

					if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(schecktechniqueValidation.get("rtn"))) {
					
						//int nsampletypecode = (int) inputMap.get("nsampletypecode");
						int napproveversioncode = (int) inputMap.get("napproveconfversioncode");
						boolean needJobAllocation = (boolean) inputMap.get("nneedjoballocation");
						boolean nneedmyjob = (boolean) inputMap.get("nneedmyjob");
						int nreghistorycode = (int) inputMap.get("registrationhistory");
						int nregsamplehistorycode = (int) inputMap.get("registrationsamplehistory");
						int regsectionhistory = (int) inputMap.get("registrationsectionhistory");
						int regsection = (int) inputMap.get("registrationsection");
						int nchainCustodyCode = (int) inputMap.get("chaincustody");

						int nRegistrationStatus = Enumeration.TransactionStatus.REGISTERED.gettransactionstatus();

						if (nneedtestinitiate) {
							nMultiUserStatus = Enumeration.TransactionStatus.INITIATED.gettransactionstatus();
						} else {
							nMultiUserStatus = Enumeration.TransactionStatus.COMPLETED.gettransactionstatus();
						}

						String insertlistpreregno = (String) inputMap.get("insertlistpreregno");
						List<String> listOfPreregno = Arrays.asList(insertlistpreregno.split(","));
						Collections.sort(listOfPreregno);

						Map<Integer, String> MapTransactionsampletoArno = new TreeMap<Integer, String>();
						int nApprovalStatus = Enumeration.TransactionStatus.REGISTERED.gettransactionstatus();
						String strSection = "";
						String strSecHistory = "";
						int joballocation = (int) inputMap.get("joballocation");

//						Map<String, Object> objAuditMap = new HashMap<String, Object>() {
//							{
//								put("npreregno", inputMap.get("npreregno"));
//								put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
//							}
//						};

						// Map<String,Object> auditMap= (Map<String, Object>)
						// getRegistrationTestAudit(objAuditMap, userInfo).getBody();
						// List<Map<String, Object>> lstDataTest =(List<Map<String, Object>>)
						// auditMap.get("RegistrationGetTest");

						if ((needJobAllocation && nneedmyjob) || (!needJobAllocation && nneedmyjob)) {

							nApprovalStatus = Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus();

						} else if ((needJobAllocation && !nneedmyjob)) {

							nApprovalStatus = Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus();

						} else if ((!needJobAllocation && !nneedmyjob)) {

							nApprovalStatus = Enumeration.TransactionStatus.REGISTERED.gettransactionstatus();
						}

						String strformat = projectDAOSupport.getSeqfnFormat("batchhistory", "seqnoformatgeneratorbatch", nregtypecode,
								nregsubtypecode, userInfo);
						int startBatchSeqNo = (int) seqNoList.get("batchhistory");

						BatchHistory batchhistory = objmap.convertValue(inputMap.get("Batchhistory"),
								new TypeReference<BatchHistory>() {
								});
						final SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						sdFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
						String stransactiondate = sdFormat.format(batchhistory.getDtransactiondate());

						List<TransactionValidation> validationStatusLst = (List<TransactionValidation>) getTransactionValidation(
								testStartId, nregtypecode, nregsubtypecode, userInfo);
//						final String stransactionStatus = validationStatusLst.stream()
//								.map(objpreregno -> String.valueOf(objpreregno.getNtransactionstatus()))
//								.collect(Collectors.joining(","));
			

						String strSubSample = " select jrsh.npreregno,jrsh.ntransactionsamplecode from "
								+ " registrationsamplehistory jrsh  where jrsh.nsamplehistorycode in "
								+ " (select max(jqrth1.nsamplehistorycode) as testhistorycode "
								+ " from registrationsamplehistory jqrth1, registrationsample jqja1,registration r "
								+ " where jqrth1.npreregno = jqja1.npreregno"
								+ " and jqrth1.ntransactionsamplecode = jqja1.ntransactionsamplecode"
								+ " and jqja1.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and jqrth1.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and r.npreregno=jqja1.npreregno and r.npreregno in (" + insertlistpreregno + ")"
								+ " and jqrth1.nsitecode=jqja1.nsitecode and jqja1.nsitecode =r.nsitecode "
								+ " and r.nsitecode=" + userInfo.getNtranssitecode()
								+ " group by jqrth1.npreregno,jqrth1.ntransactionsamplecode)"
								+ " and jrsh.ntransactionstatus ="
								+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus()
								+ " and jrsh.npreregno in (" + insertlistpreregno + ") " + " and jrsh.nsitecode="
								+ userInfo.getNtranssitecode() + " order by npreregno";

						List<RegistrationSample> lstRegistrationSubSample = jdbcTemplate.query(strSubSample,
								new RegistrationSample());

						final String strSeqnoFormatQuery = "select rsc.nperiodcode,rsc.jsondata,"
								+ " rsc.jsondata->>'ssampleformat' ssampleformat, sag.nsequenceno,sag.nseqnoarnogencode,"
								+ " rsc.nregsubtypeversioncode"
								+ " from  approvalconfig ap, seqnoarnogenerator sag,regsubtypeconfigversion rsc "
								+ " where  sag.nseqnoarnogencode = rsc.nseqnoarnogencode " + " and rsc.nsitecode = "
								+ userInfo.getNmastersitecode() + " and sag.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ap.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nregtypecode = "
								+ (int) inputMap.get("nregtypecode") + " and nregsubtypecode ="
								+ (int) inputMap.get("nregsubtypecode")
								+ " and rsc.napprovalconfigcode=ap.napprovalconfigcode and rsc.ntransactionstatus="
								+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus();

						SeqNoArnoGenerator seqnoFormat = jdbcTemplate.queryForObject(strSeqnoFormatQuery,
								new SeqNoArnoGenerator());

						if (nneedtestinitiate == false) {

							String sSampleCheck = "select case when " + nneedsubsample
									+ "=true  then max(rsarno.ssamplearno) " + "  else  max(arno.sarno) end samplearno "
									+ "  from batchsample bs,batchhistory bh,"
									+ "  registrationarno arno ,registrationsamplearno rsarno"
									+ "  where bs.nbatchmastercode <> " + nbatchmastercode + " and " + "  bs.npreregno in("
									+ inputMap.get("multiPreregArr") + ")" + "  and bs.ntransactionsamplecode in ("
									+ inputMap.get("multiTransSampleArr") + ")" + "  and bs.ntransactiontestcode in ("
									+ inputMap.get("multiTransTestArr") + ")"
									+ "  and bs.nbatchmastercode = bh.nbatchmastercode "
									+ "  and bh.nbatchhistorycode= any(select max(nbatchhistorycode) nbatchhistorycode "
									+ "  from batchhistory bh,batchsample bs "
									+ "  where bs.nbatchmastercode = bh.nbatchmastercode "
									+ "  and bh.ntransactionstatus = "
									+ Enumeration.TransactionStatus.INITIATED.gettransactionstatus() + " "
									+ "  and bh.nbatchmastercode <> " + nbatchmastercode + "" + "  and bs.nsitecode="
									+ userInfo.getNtranssitecode() + "" + "  and bs.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
									+ "  and bh.nsitecode=" + userInfo.getNtranssitecode() + "" + "  and bh.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
									+ "  group by bs.ntransactiontestcode) " + "  and arno.npreregno=bs.npreregno "
									+ "  and rsarno.ntransactionsamplecode = bs.ntransactionsamplecode "
									+ "  and bs.nsitecode=" + userInfo.getNtranssitecode() + "" + "  and bs.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
									+ "  and bh.nsitecode=" + userInfo.getNtranssitecode() + "" + "  and bh.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
									+ "  and arno.nsitecode=" + userInfo.getNtranssitecode() + "" + "  and arno.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
									+ "  and rsarno.nsitecode=" + userInfo.getNtranssitecode() + ""
									+ "  and rsarno.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ "" + "  group by bs.ntransactiontestcode ";

							lstSampleCheck = jdbcTemplate.query(sSampleCheck, new RegistrationTest());
						}

						String sSampleCheckQuery = " select case when " + nneedsubsample + "=true "
								+ " then rsarno.ssamplearno else arno.sarno end samplearno, "
								+ " rt.ntransactiontestcode from registrationtest rt,"
								+ " registrationtesthistory rth ,registrationarno arno ,registrationsamplearno rsarno"
								+ " where rth.ntesthistorycode = any(select max(rth.ntesthistorycode) from registrationtesthistory rth"
								+ " where rth.ntransactionstatus=" + nMultiUserStatus + "" + " and rt.ntestcode="
								+ ntestcode + "" + " and rth.ntransactiontestcode in(" + stransactiontestcode + ")"
								+ " and rth.nsitecode=" + userInfo.getNtranssitecode() + "" + " and rth.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								+ " group by rth.ntransactiontestcode)"
								+ " and rth.ntransactiontestcode=rt.ntransactiontestcode " + " and rt.ntestcode="
								+ ntestcode + "" + " and arno.npreregno=rt.npreregno and rsarno.npreregno=arno.npreregno "
								+ " and rsarno.ntransactionsamplecode=rt.ntransactionsamplecode" + " and rt.npreregno in("
								+ inputMap.get("multiPreregArr") + ") " + " and rt.ntransactionsamplecode in("
								+ inputMap.get("multiTransSampleArr") + ")" + " and arno.nsitecode="
								+ userInfo.getNtranssitecode() + "" + " and rsarno.nsitecode="
								+ userInfo.getNtranssitecode() + "" + " and arno.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rsarno.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rt.nsitecode="
								+ userInfo.getNtranssitecode() + "" + " and rth.nsitecode=" + userInfo.getNtranssitecode()
								+ "" + " and rt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
								+ " and rth.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

						List<RegistrationTest> lstBasemastervalues = jdbcTemplate.query(sSampleCheckQuery,
								new RegistrationTest());

						if (lstBasemastervalues.size() > 0) {
							String ssamplearno = ((lstBasemastervalues.stream()
									.map(object -> String.valueOf(object.getSamplearno())).collect(Collectors.toList()))
									.stream().distinct().collect(Collectors.toList())).stream()
									.collect(Collectors.joining(","));

							final String ssamplestatus = validationStatusLst.stream()
									.map(objpreregno -> String.valueOf(objpreregno.getStransdisplaystatus()))
									.collect(Collectors.joining(","));



							String sAlert = ssamplearno + " "
									+ commonFunction.getMultilingualMessage("IDS_ALREADY", userInfo.getSlanguagefilename())
									+ " " + ssamplestatus;
//										 userInfo.getSlanguagefilename())+" "+commonFunction.getMultilingualMessage("IDS_ALREADY",
//										 userInfo.getSlanguagefilename())+" "+ssamplestatus;

							return new ResponseEntity<>(sAlert, HttpStatus.EXPECTATION_FAILED);

						} else if (lstSampleCheck.size() > 0) {

							String ssamplearno = ((lstSampleCheck.stream()
									.map(object -> String.valueOf(object.getSamplearno())).collect(Collectors.toList()))
									.stream().distinct().collect(Collectors.toList())).stream()
									.collect(Collectors.joining(","));

							String sAlert = ssamplearno + " " + commonFunction
									.getMultilingualMessage("IDS_ALREADYINITIATED", userInfo.getSlanguagefilename());
//												 userInfo.getSlanguagefilename())+" "+commonFunction.getMultilingualMessage("IDS_ALREADY",
//												 userInfo.getSlanguagefilename())+" "+ssamplestatus;

							// return new ResponseEntity<>(sAlert, HttpStatus.EXPECTATION_FAILED);
							return new ResponseEntity<>(sAlert, HttpStatus.EXPECTATION_FAILED);

						} else if (nneedtestinitiate == false && isiqcdata == false) {

							final String comments = batchhistory.getScomments() != null
									? " N'" + stringUtilityFunction.replaceQuote(batchhistory.getScomments()) + "'"
									: null;

							String sInsertBatchHist = "insert into batchhistory(nbatchhistorycode,nbatchmastercode,sbatcharno,"
									+ " ndeputyusercode,ndeputyuserrolecode,ntransactionstatus,nusercode,nuserrolecode,dtransactiondate,"
									+ " noffsetdtransactiondate,ntransdatetimezonecode,"
									+ " scomments,nsitecode,nstatus) values(" + startBatchSeqNo + "," + nbatchmastercode
									+ ",'" + strformat + "'," + " " + userInfo.getNdeputyusercode() + ","
									+ userInfo.getNdeputyuserrole() + ","
									+ Enumeration.TransactionStatus.INITIATED.gettransactionstatus() + "," + " "
									+ userInfo.getNusercode() + "," + userInfo.getNuserrole() + ",'" + stransactiondate
									+ "',"
									// + " '"+getCurrentDateTime(userInfo)+"',"
									+ " " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
									+ userInfo.getNtimezonecode() + ","
									// + " N'"+ReplaceQuote(batchhistory.getScomments().toString())+"',"
									+ comments + "," + userInfo.getNtranssitecode() + "," + " "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

							jdbcTemplate.execute(sInsertBatchHist);

							String squerySample = "select * from batchsample where " + " nbatchmastercode = "
									+ nbatchmastercode + " " + " and nsitecode=" + userInfo.getNtranssitecode() + ""
									+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

							List<Batchsample> lstBatchsample = jdbcTemplate.query(squerySample, new Batchsample());

							String lstBatchIqcTrans = "";
							
							for (int i = 0; i <= lstBatchsample.size() - 1; i++) {
								
								lstBatchIqcTrans = lstBatchIqcTrans + " (" + lstBatchsample.get(i).getNtransactiontestcode()
										+ "," + "" + lstBatchsample.get(i).getNpreregno() + ","
										+ lstBatchsample.get(i).getNtransactionsamplecode() + "," + ""
										+ lstBatchsample.get(i).getNbatchmastercode() + "," + ntestcode + "," + "'"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNtranssitecode() + "," + ""
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
								;
							}

							batchiqctransaction = "Insert into batchiqctransaction(ntransactiontestcode, npreregno, ntransactionsamplecode, nbatchmastercode,ntestcode, dmodifieddate, "
									+ " nsitecode, nstatus) values "
									+ lstBatchIqcTrans.substring(0, lstBatchIqcTrans.length() - 1) + ";";
							jdbcTemplate.execute(batchiqctransaction);

//							     getJdbcTemplate().execute(
//												"insert into registrationtesthistory ( ntesthistorycode,ntransactiontestcode,"
//												        + " ntransactionsamplecode,npreregno,nformcode,"
//														+ " ntransactionstatus,nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,"
//														+ " noffsetdtransactiondate,ntransdatetimezonecode,"
//														+ " scomments,"
//														+ " dtransactiondate,nsampleapprovalhistorycode,nsitecode,nstatus) "
//														+ " ((select " + (ntesthistorycode) + "+Rank()over(order by ntesthistorycode)"
//														+ " ntesthistorycode,ntransactiontestcode,ntransactionsamplecode,npreregno,"
//														+ userInfo.getNformcode() + " nformcode," + Enumeration.TransactionStatus.INITIATED.gettransactionstatus()
//														+ " ntransactionstatus," +  userInfo.getNusercode() + "nusercode, " + ""
//														+ userInfo.getNuserrole() + " nuserrolecode," + userInfo.getNdeputyusercode()
//														+ " ndeputyusercode," + userInfo.getNdeputyuserrole()
//														+ " ndeputyuserrolecode,"+getCurrentDateTimeOffset(userInfo.getStimezoneid())+","
//														+ "	"+userInfo.getNtimezonecode()+","
//														+ " N'" + ReplaceQuote(userInfo.getSreason())
//														+ "'scomments,'" + getCurrentDateTime(userInfo)// objGeneral.getUTCDateTime()
//														+ "'dtransactiondate," + " "+Enumeration.TransactionStatus.NA.gettransactionstatus()+" "
//														+ " nsampleapprovalhistorycode ,"+userInfo.getNtranssitecode()+" nsitecode ,"
//														+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" nstatus " 
//														+ " from registrationtesthistory where ntesthistorycode in "
//														+ " (select max(rth.ntesthistorycode) from registrationtesthistory rth where rth.ntransactiontestcode in ("
//														+ stransactiontestcode + ")"
//														+ " group by rth.ntransactiontestcode,rth.npreregno,rth.ntransactionsamplecode)));");

						} else if (nneedtestinitiate && isiqcdata) {

							Map<Integer, String> strIQCformat = projectDAOSupport.getSeqfnFormatForMultipleRunningNo(listOfPreregno,
									"batchsampleiqc", "seqnoformatgeneratorbatch", nregtypecode, nregsubtypecode, userInfo);
							// String strIQCformat =
							// getSeqfnFormatForMultipleRunningNo(listOfPreregno,"batchsampleiqc","seqnoformatgeneratorbatch",userInfo);
//							int iqcStartBatchSeqNo = (int) seqNoList.get("batchsampleiqc");
//							int nendTesthistorycode = (int) seqNoList.get("nendTesthistorycode");

							final String comments = batchhistory.getScomments() != null
									? " N'" + stringUtilityFunction.replaceQuote(batchhistory.getScomments()) + "'"
									: null;

							String sInsertBatchHist = "insert into batchhistory(nbatchhistorycode,nbatchmastercode,sbatcharno,"
									+ " ndeputyusercode,ndeputyuserrolecode,ntransactionstatus,nusercode,nuserrolecode,dtransactiondate,"
									+ " noffsetdtransactiondate,ntransdatetimezonecode,"
									+ " scomments,nsitecode,nstatus) values(" + startBatchSeqNo + "," + nbatchmastercode
									+ ",'" + strformat + "'," + " " + userInfo.getNdeputyusercode() + ","
									+ userInfo.getNdeputyuserrole() + ","
									+ Enumeration.TransactionStatus.INITIATED.gettransactionstatus() + "," + " "
									+ userInfo.getNusercode() + "," + userInfo.getNuserrole() + ",'" + stransactiondate
									+ "',"
									// + " '"+getCurrentDateTime(userInfo)+"',"
									+ " " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
									+ userInfo.getNtimezonecode() + ","
									// + " N'"+ReplaceQuote(inputMap.get("scomments").toString())+"',"
									// + " N'"+ReplaceQuote(batchhistory.getScomments().toString())+"',"
									+ comments + "," + userInfo.getNtranssitecode() + "," + " "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

							jdbcTemplate.execute(sInsertBatchHist);

							

							String strSectionHistory = " select (tg.nsectioncode),rth.npreregno from "
									+ " registrationtest rt,testgrouptest tg,registrationtesthistory rth "
									+ " where tg.ntestgrouptestcode=rt.ntestgrouptestcode "
									+ " and rth.ntransactiontestcode=rt.ntransactiontestcode "
									+ " and rth.ntesthistorycode = any(select max(ntesthistorycode) from registrationtesthistory rth1 "
									+ " where rth1.ntransactiontestcode=rt.ntransactiontestcode  " + " and rth1.nsitecode="
									+ userInfo.getNtranssitecode() + " and rth1.nstatus= "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ) "
									+ " and rth.npreregno in (" + insertlistpreregno + ") "
									+ " and rt.nsitecode=rth.nsitecode and rth.nsitecode=" + userInfo.getNtranssitecode()
									+ " and rth.ntransactionstatus  not in ("
									+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ","
									+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus()
									+ ") group by rth.npreregno,tg.nsectioncode ";

							List<RegistrationTest> lstRegistrationSectionHistory = jdbcTemplate.query(strSectionHistory,
									new RegistrationTest());

							for (int f = 0; f < lstRegistrationSectionHistory.size(); f++) {

								++regsection;
								strSection = strSection + "(" + regsection + ","
										+ lstRegistrationSectionHistory.get(f).getNpreregno() + ","
										+ lstRegistrationSectionHistory.get(f).getNsectioncode() + ","
										+ userInfo.getNtranssitecode() + ","
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";

								++regsectionhistory;
								strSecHistory = strSecHistory + "(" + regsectionhistory + ","
										+ lstRegistrationSectionHistory.get(f).getNpreregno() + ","
										+ lstRegistrationSectionHistory.get(f).getNsectioncode() + ","
										+ Enumeration.TransactionStatus.REGISTERED.gettransactionstatus() + ","
										+ userInfo.getNtranssitecode() + ","
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";

							}

							strSection = "insert into registrationsection(nregistrationsectioncode, npreregno, "
									+ "nsectioncode, nsitecode,nstatus) values"
									+ strSection.substring(0, strSection.length() - 1) + ";";

							jdbcTemplate.execute(strSection);

							strSecHistory = "insert into registrationsectionhistory(nsectionhistorycode, npreregno, "
									+ "nsectioncode, ntransactionstatus, nsitecode,nstatus) values"
									+ strSecHistory.substring(0, strSecHistory.length() - 1) + ";";

							jdbcTemplate.execute(strSecHistory);

							String strRegistrationHistory = " insert into registrationhistory (nreghistorycode,npreregno,ntransactionstatus,dtransactiondate,nusercode,nuserrolecode "
									+ " ,ndeputyusercode,ndeputyuserrolecode,scomments,nsitecode,nstatus,"
									+ " noffsetdtransactiondate,ntransdatetimezonecode) " + " select " + nreghistorycode
									+ "+Rank() over (order by npreregno) as nreghistorycode, " + " npreregno npreregno,"
									+ nRegistrationStatus + " as ntransactionstatus, '" + dateUtilityFunction.getCurrentDateTime(userInfo)
									+ "' as dtransactiondate," + userInfo.getNusercode() + "," + userInfo.getNuserrole()
									+ "," + userInfo.getNdeputyusercode() + "," + userInfo.getNdeputyuserrole() + ",'"
									+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "'," + userInfo.getNtranssitecode() + ","
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
									+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
									+ userInfo.getNtimezonecode() + " from  registrationhistory where npreregno in ( "
									+ insertlistpreregno + ") and ntransactionstatus ="
									+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + " and nsitecode="
									+ userInfo.getNtranssitecode();
							jdbcTemplate.execute(strRegistrationHistory);

							String query1 = "insert into registrationsamplehistory (nsamplehistorycode,ntransactionsamplecode,npreregno,ntransactionstatus "
									+ ",dtransactiondate,nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments,nsitecode,nstatus,noffsetdtransactiondate,ntransdatetimezonecode) "
									+ " select " + nregsamplehistorycode
									+ "+Rank() over (order by ntransactionsamplecode) as nsamplehistorycode, ntransactionsamplecode as ntransactionsamplecode,"
									+ " npreregno npreregno," + nRegistrationStatus + " as ntransactionstatus, '"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' as dtransactiondate," + userInfo.getNusercode()
									+ "," + userInfo.getNuserrole() + "," + userInfo.getNdeputyusercode() + ","
									+ userInfo.getNdeputyuserrole() + ",'" + stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "',"
									+ userInfo.getNtranssitecode() + ","
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
									+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
									+ userInfo.getNtimezonecode() + " from registrationsamplehistory where npreregno in ( "
									+ insertlistpreregno + ")  " + " and nsitecode=" + userInfo.getNtranssitecode()
									+ " and nsamplehistorycode in "
									+ "(select max(jqrth1.nsamplehistorycode) as testhistorycode "
									+ " from registrationsamplehistory jqrth1,registrationsample jqja1,registration r "
									+ " where  jqrth1.npreregno = jqja1.npreregno "
									+ " and jqrth1.ntransactionsamplecode = jqja1.ntransactionsamplecode "
									+ " and jqja1.nstatus=jqrth1.nstatus and jqrth1.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and r.npreregno=jqja1.npreregno and r.npreregno in (" + insertlistpreregno + ") "
									+ " and jqrth1.nsitecode = jqja1.nsitecode and jqja1.nsitecode=r.nsitecode "
									+ " and r.nsitecode=" + userInfo.getNtranssitecode()
									+ " group by jqrth1.npreregno,jqrth1.ntransactionsamplecode)"
									+ "  and ntransactionstatus not in ("
									+ +Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","
									+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ")";
							jdbcTemplate.execute(query1);

							jdbcTemplate.execute(
									"insert into registrationtesthistory ( ntesthistorycode,ntransactiontestcode,ntransactionsamplecode,npreregno,nformcode,"
											+ " ntransactionstatus,nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,noffsetdtransactiondate,"
											+ " ntransdatetimezonecode,scomments,"
											+ " dtransactiondate,nsampleapprovalhistorycode,nsitecode,nstatus) "
											+ " ((select " + (ntesthistorycode) + "+Rank()over(order by ntesthistorycode)"
											+ " ntesthistorycode,ntransactiontestcode,ntransactionsamplecode,npreregno,"
											+ userInfo.getNformcode() + " nformcode,"
											+ Enumeration.TransactionStatus.INITIATED.gettransactionstatus()
											+ " ntransactionstatus," + userInfo.getNusercode() + " nusercode, " + ""
											+ userInfo.getNuserrole() + " nuserrolecode," + userInfo.getNdeputyusercode()
											+ " ndeputyusercode," + userInfo.getNdeputyuserrole() + " ndeputyuserrolecode, "
											+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + " "
											+ userInfo.getNtimezonecode() + "," + " N'"
											+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "' scomments,'"
											+ dateUtilityFunction.getCurrentDateTime(userInfo)// objGeneral.getUTCDateTime()
											+ "'dtransactiondate," + " "
											+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " "
											+ " nsampleapprovalhistorycode ," + userInfo.getNtranssitecode()
											+ " nsitecode, " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " nstatus " + " from registrationtesthistory where ntesthistorycode in "
											+ " (select max(rth.ntesthistorycode) from registrationtesthistory rth "
											+ " where rth.ntransactiontestcode in (" + stransactiontestcode + ")"
											+ " group by rth.ntransactiontestcode,rth.npreregno,rth.ntransactionsamplecode)));");


							String query3 = "insert into resultparameter( "
									+ "ntransactionresultcode,npreregno,ntransactiontestcode,ntestgrouptestparametercode "
									+ ",ntestparametercode,nparametertypecode,nresultmandatory,nreportmandatory,ntestgrouptestformulacode"
									+ ",nunitcode,ngradecode,ntransactionstatus,nenforcestatus,nenforceresult,ncalculatedresult,"
									+ "nenteredby,nenteredrole,ndeputyenteredby " + ",ndeputyenteredrole "
									+ ",nlinkcode,nattachmenttypecode,jsondata,nsitecode, nstatus) "
									+ " select rp.ntransactionresultcode,rp.npreregno, "
									+ "rp.ntransactiontestcode,rp.ntestgrouptestparametercode,rp.ntestparametercode,rp.nparametertypecode, "
									+ "tgtp.nresultmandatory,tgtp.nreportmandatory,coalesce(tgtf.ntestgrouptestformulacode,"
									+ Enumeration.TransactionStatus.NA.gettransactionstatus()
									+ ") as ntestgrouptestformulacode, tgtp.nunitcode,"
									+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " as ngradecode,"
									+ Enumeration.TransactionStatus.INITIATED.gettransactionstatus()
									+ " as ntransactionstatus," + ""
									+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " as nenforcestatus,"
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
									+ " from registrationparameter rp,testgrouptestparameter tgtp  "
									+ " left outer join testgrouptestformula tgtf on"
									+ " tgtp.ntestgrouptestparametercode = tgtf.ntestgrouptestparametercode  "
									+ " and tgtf.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and tgtf.ntransactionstatus="
									+ Enumeration.TransactionStatus.YES.gettransactionstatus()
									+ " where rp.ntestgrouptestparametercode=tgtp.ntestgrouptestparametercode "
									+ " and rp.npreregno in (" + insertlistpreregno + ")" + " and rp.nsitecode="
									+ userInfo.getNtranssitecode() + ";";

							jdbcTemplate.execute(query3);

							String query4 = "insert into resultparametercomments (ntransactionresultcode,ntransactiontestcode "
									+ ",npreregno, ntestgrouptestparametercode ,ntestparametercode "
									+ ",jsondata,nsitecode, nstatus )  select rp.ntransactionresultcode "
									+ ",rp.ntransactiontestcode,rp.npreregno,rp.ntestgrouptestparametercode,rp.ntestparametercode , '{}'::jsonb,"
									+ userInfo.getNtranssitecode() + ","
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " from registrationparameter rp where npreregno in (" + insertlistpreregno + ")"
									+ " and nsitecode=" + userInfo.getNtranssitecode() + ";";

							jdbcTemplate.execute(query4);

							for (int i = 0; i < listOfPreregno.size(); i++) {

								String strRegistrationArno = "update registrationarno " + " set sarno ='"
										+ strIQCformat.get(Integer.parseInt(listOfPreregno.get(i))) + "'"
										+ " where npreregno =" + Integer.parseInt(listOfPreregno.get(i)) + " and nsitecode="
										+ userInfo.getNtranssitecode();

								jdbcTemplate.execute(strRegistrationArno);

								for (int l = 0; l < lstRegistrationSubSample.size(); l++) {
									String subsampleArno = "";
									if (lstRegistrationSubSample.get(l).getNpreregno() == Integer
											.parseInt(listOfPreregno.get(i))) {
										// String formatted = String.format("%02d", i+1);
										String formatted = String.format("%02d", 1);
										subsampleArno = "" + strIQCformat.get(Integer.parseInt(listOfPreregno.get(i))) + "-"
												+ formatted;

										String strSampleArno = " update registrationsamplearno " + " set ssamplearno ='"
												+ subsampleArno + "' where ntransactionsamplecode ="
												+ lstRegistrationSubSample.get(l).getNtransactionsamplecode()
												+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode=" + userInfo.getNtranssitecode();
										jdbcTemplate.execute(strSampleArno);

										MapTransactionsampletoArno.put(
												lstRegistrationSubSample.get(l).getNtransactionsamplecode(), subsampleArno);

									}

								}

							}

							insertChainCustody(nchainCustodyCode, nbatchmastercode, strformat, nneedsubsample, isiqcdata,
									userInfo);

							String strArnoSeqno = "update seqnoarnogenerator set nsequenceno = "
									+ (listOfPreregno.size() + seqnoFormat.getNsequenceno()) + " where nseqnoarnogencode ="
									+ seqnoFormat.getNseqnoarnogencode()+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";

							jdbcTemplate.execute(strArnoSeqno);

							String squerySample = "select * from batchsample where " + " nbatchmastercode = "
									+ nbatchmastercode + " " + " and nsitecode=" + userInfo.getNtranssitecode() + ""
									+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

							List<Batchsample> lstBatchsample = jdbcTemplate.query(squerySample, new Batchsample());

							String lstBatchIqcTrans = "";
							// if(lstBatchsample.size()>0) {
							// for(int i=lstBatchsample.size()-1;i > 0;i--) {
							for (int i = 0; i <= lstBatchsample.size() - 1; i++) {
								// lstBatchIqcTrans = lstBatchIqcTrans + "
								// ("+getBatchiqctransaction(lstBatchsample,index ,userInfo)+")";

								lstBatchIqcTrans = lstBatchIqcTrans + " (" + lstBatchsample.get(i).getNtransactiontestcode()
										+ "," + "" + lstBatchsample.get(i).getNpreregno() + ","
										+ lstBatchsample.get(i).getNtransactionsamplecode() + "," + ""
										+ lstBatchsample.get(i).getNbatchmastercode() + "," + ntestcode + "," + "'"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNtranssitecode() + "," + ""
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
								;
							}

							// if(batchSampleInputList.size()>0) {
							// for(int i=lstBatchsample.size()-1;i > 0;i--) {
							for (int i = 0; i <= batchSampleInputList.size() - 1; i++) {

								lstBatchIqcTrans = lstBatchIqcTrans + " ("
										+ batchSampleInputList.get(i).getNtransactiontestcode() + "," + ""
										+ batchSampleInputList.get(i).getNpreregno() + ","
										+ batchSampleInputList.get(i).getNtransactionsamplecode() + "," + ""
										+ batchSampleInputList.get(i).getNbatchmastercode() + "," + ntestcode + "," + "'"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNtranssitecode() + "," + ""
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
							}

							// }

							// }

							batchiqctransaction = "Insert into batchiqctransaction(ntransactiontestcode, npreregno, ntransactionsamplecode, nbatchmastercode,ntestcode,dmodifieddate, "
									+ " nsitecode, nstatus) values "
									+ lstBatchIqcTrans.substring(0, lstBatchIqcTrans.length() - 1) + ";";
							jdbcTemplate.execute(batchiqctransaction);

						} else if (nneedtestinitiate && !isiqcdata) {

							final String comments = batchhistory.getScomments() != null
									? " N'" + stringUtilityFunction.replaceQuote(batchhistory.getScomments()) + "'"
									: null;

							String sInsertBatchHist = "insert into batchhistory(nbatchhistorycode,nbatchmastercode,sbatcharno,"
									+ " ndeputyusercode,ndeputyuserrolecode,ntransactionstatus,nusercode,nuserrolecode,dtransactiondate,"
									+ " noffsetdtransactiondate,ntransdatetimezonecode,"
									+ " scomments,nsitecode,nstatus) values(" + startBatchSeqNo + "," + nbatchmastercode
									+ ",'" + strformat + "'," + " " + userInfo.getNdeputyusercode() + ","
									+ userInfo.getNdeputyuserrole() + ","
									+ Enumeration.TransactionStatus.INITIATED.gettransactionstatus() + "," + " "
									+ userInfo.getNusercode() + "," + userInfo.getNuserrole() + ",'" + stransactiondate
									+ "',"
									// + " '"+getCurrentDateTime(userInfo)+"',"
									+ " " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
									+ userInfo.getNtimezonecode() + ","
									// + " N'"+ReplaceQuote(batchhistory.getScomments().toString())+"',"
									+ comments + "," + userInfo.getNtranssitecode() + "," + " "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

							jdbcTemplate.execute(sInsertBatchHist);

							jdbcTemplate.execute("insert into registrationtesthistory ( ntesthistorycode,ntransactiontestcode,"
											+ " ntransactionsamplecode,npreregno,nformcode,"
											+ " ntransactionstatus,nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,"
											+ " noffsetdtransactiondate,ntransdatetimezonecode," + " scomments,"
											+ " dtransactiondate,nsampleapprovalhistorycode,nsitecode,nstatus) "
											+ " ((select " + (ntesthistorycode) + "+Rank()over(order by ntesthistorycode)"
											+ " ntesthistorycode,ntransactiontestcode,ntransactionsamplecode,npreregno,"
											+ userInfo.getNformcode() + " nformcode,"
											+ Enumeration.TransactionStatus.INITIATED.gettransactionstatus()
											+ " ntransactionstatus," + userInfo.getNusercode() + " nusercode, " + ""
											+ userInfo.getNuserrole() + " nuserrolecode," + userInfo.getNdeputyusercode()
											+ " ndeputyusercode," + userInfo.getNdeputyuserrole() + " ndeputyuserrolecode,"
											+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + "	"
											+ userInfo.getNtimezonecode() + "," + " N'"
											+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "'scomments,'"
											+ dateUtilityFunction.getCurrentDateTime(userInfo)// objGeneral.getUTCDateTime()
											+ "'dtransactiondate," + " "
											+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " "
											+ " nsampleapprovalhistorycode ," + userInfo.getNtranssitecode()
											+ " nsitecode ," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " nstatus " + " from registrationtesthistory where ntesthistorycode in "
											+ " (select max(rth.ntesthistorycode) from registrationtesthistory rth where rth.ntransactiontestcode in ("
											+ stransactiontestcode + ")"
											+ " group by rth.ntransactiontestcode,rth.npreregno,rth.ntransactionsamplecode)));");

							String squerySample = "select * from batchsample where " + " nbatchmastercode = "
									+ nbatchmastercode + " " + " and nsitecode=" + userInfo.getNtranssitecode() + ""
									+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

							List<Batchsample> lstBatchsample = jdbcTemplate.query(squerySample, new Batchsample());
							String lstBatchIqcTrans = "";
							// if(lstBatchsample.size()>0) {
							// for(int i=lstBatchsample.size()-1;i > 0;i--) {
							for (int i = 0; i <= lstBatchsample.size() - 1; i++) {
								// lstBatchIqcTrans = lstBatchIqcTrans + "
								// ("+getBatchiqctransaction(lstBatchsample,index ,userInfo)+")";

								lstBatchIqcTrans = lstBatchIqcTrans + " (" + lstBatchsample.get(i).getNtransactiontestcode()
										+ "," + "" + lstBatchsample.get(i).getNpreregno() + ","
										+ lstBatchsample.get(i).getNtransactionsamplecode() + "," + ""
										+ lstBatchsample.get(i).getNbatchmastercode() + "," + ntestcode + "," + "'"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNtranssitecode() + "," + ""
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
								;
							}

							batchiqctransaction = "Insert into batchiqctransaction(ntransactiontestcode, npreregno, ntransactionsamplecode, nbatchmastercode,ntestcode, dmodifieddate, "
									+ " nsitecode, nstatus) values "
									+ lstBatchIqcTrans.substring(0, lstBatchIqcTrans.length() - 1) + ";";
							jdbcTemplate.execute(batchiqctransaction);

							insertChainCustody(nchainCustodyCode, nbatchmastercode, strformat, nneedsubsample, isiqcdata,
									userInfo);
						} else {

							Map<Integer, String> strIQCformat = projectDAOSupport.getSeqfnFormatForMultipleRunningNo(listOfPreregno,
									"batchsampleiqc", "seqnoformatgeneratorbatch", nregtypecode, nregsubtypecode, userInfo);
							// String strIQCformat =
							// getSeqfnFormatForMultipleRunningNo(listOfPreregno,"batchsampleiqc","seqnoformatgeneratorbatch",userInfo);
							//int iqcStartBatchSeqNo = (int) seqNoList.get("batchsampleiqc");

							String strRegistrationHistory = " insert into registrationhistory (nreghistorycode,npreregno,ntransactionstatus,dtransactiondate,nusercode,nuserrolecode "
									+ " ,ndeputyusercode,ndeputyuserrolecode,scomments,nsitecode,nstatus,noffsetdtransactiondate,ntransdatetimezonecode) "
									+ " select " + nreghistorycode
									+ "+Rank() over (order by npreregno) as nreghistorycode, " + " npreregno npreregno,"
									+ nRegistrationStatus + " as ntransactionstatus, '" + dateUtilityFunction.getCurrentDateTime(userInfo)
									+ "' as dtransactiondate," + userInfo.getNusercode() + "," + userInfo.getNuserrole()
									+ "," + userInfo.getNdeputyusercode() + "," + userInfo.getNdeputyuserrole() + ",'"
									+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "'," + userInfo.getNtranssitecode() + ","
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
									+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
									+ userInfo.getNtimezonecode() + " from  registrationhistory where npreregno in ( "
									+ insertlistpreregno + ") and ntransactionstatus ="
									+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + " and nsitecode="
									+ userInfo.getNtranssitecode();
							jdbcTemplate.execute(strRegistrationHistory);

							String query1 = "insert into registrationsamplehistory (nsamplehistorycode,ntransactionsamplecode,npreregno,ntransactionstatus "
									+ ",dtransactiondate,nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments,nsitecode,nstatus,noffsetdtransactiondate,ntransdatetimezonecode) "
									+ " select " + nregsamplehistorycode
									+ "+Rank() over (order by ntransactionsamplecode) as nsamplehistorycode, ntransactionsamplecode as ntransactionsamplecode,"
									+ " npreregno npreregno," + nRegistrationStatus + " as ntransactionstatus, '"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' as dtransactiondate," + userInfo.getNusercode()
									+ "," + userInfo.getNuserrole() + "," + userInfo.getNdeputyusercode() + ","
									+ userInfo.getNdeputyuserrole() + ",'" + stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "',"
									+ userInfo.getNtranssitecode() + ","
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
									+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
									+ userInfo.getNtimezonecode() + " from registrationsamplehistory where npreregno in ( "
									+ insertlistpreregno + ")  " + " and nsitecode=" + userInfo.getNtranssitecode()
									+ " and nsamplehistorycode in "
									+ "(select max(jqrth1.nsamplehistorycode) as testhistorycode "
									+ " from registrationsamplehistory jqrth1,registrationsample jqja1,registration r "
									+ " where  jqrth1.npreregno = jqja1.npreregno "
									+ " and jqrth1.ntransactionsamplecode = jqja1.ntransactionsamplecode "
									+ " and jqja1.nstatus=jqrth1.nstatus and jqrth1.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and r.npreregno=jqja1.npreregno and r.npreregno in (" + insertlistpreregno + ") "
									+ " and jqrth1.nsitecode = jqja1.nsitecode and jqja1.nsitecode=r.nsitecode "
									+ " and r.nsitecode=" + userInfo.getNtranssitecode()
									+ " group by jqrth1.npreregno,jqrth1.ntransactionsamplecode)"
									+ "  and ntransactionstatus not in ("
									+ +Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","
									+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ")";
							jdbcTemplate.execute(query1);

							String query2 = "insert into registrationtesthistory (ntesthistorycode,ntransactiontestcode,ntransactionsamplecode,npreregno,nformcode,"
									+ " ntransactionstatus,nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode "
									+ ",scomments,dtransactiondate,nsampleapprovalhistorycode,nsitecode, nstatus,noffsetdtransactiondate,ntransdatetimezonecode) "
									+ " select " + ntesthistorycode
									+ "+Rank() over (order by ntransactiontestcode) as ntesthistorycode,ntransactiontestcode,ntransactionsamplecode,npreregno npreregno"
									+ "," + userInfo.getNformcode() + " as nformcode ," + nApprovalStatus
									+ " as ntransactionstatus," + userInfo.getNusercode() + "," + userInfo.getNuserrole()
									+ "," + userInfo.getNdeputyusercode() + "," + userInfo.getNdeputyuserrole() + ",'"
									+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "','" + dateUtilityFunction.getCurrentDateTime(userInfo)
									+ "' as dtransactiondate, " + Enumeration.TransactionStatus.NA.gettransactionstatus()
									+ "," + userInfo.getNtranssitecode() + ","
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
									+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
									+ userInfo.getNtimezonecode() + " from registrationtesthistory where "
									+ " npreregno in ( " + insertlistpreregno + ")" + " and nsitecode = "
									+ userInfo.getNtranssitecode()
									+ " and ntesthistorycode in (select max(jqrth1.ntesthistorycode) as testhistorycode "
									+ " from registrationtesthistory jqrth1,registrationtest jqja1,registration r "
									+ " where jqrth1.npreregno = jqja1.npreregno "
									+ " and jqrth1.ntransactionsamplecode = jqja1.ntransactionsamplecode "
									+ " and jqja1.nstatus=1 and jqrth1.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and r.npreregno=jqja1.npreregno and r.npreregno in (" + insertlistpreregno + ") "
									+ " and jqrth1.nsitecode=jqja1.nsitecode and jqja1.nsitecode=r.nsitecode and r.nsitecode="
									+ userInfo.getNtranssitecode()
									+ " group by jqrth1.npreregno,jqrth1.ntransactiontestcode) and"
									+ " ntransactionstatus not in ("
									+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","
									+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ");";
							jdbcTemplate.execute(query2);

							String query3 = "insert into resultparameter( "
									+ "ntransactionresultcode,npreregno,ntransactiontestcode,ntestgrouptestparametercode "
									+ ",ntestparametercode,nparametertypecode,nresultmandatory,nreportmandatory,ntestgrouptestformulacode"
									+ ",nunitcode,ngradecode,ntransactionstatus,nenforcestatus,nenforceresult,ncalculatedresult,"
									+ "nenteredby,nenteredrole,ndeputyenteredby " + ",ndeputyenteredrole "
									+ ",nlinkcode,nattachmenttypecode,jsondata,nsitecode, nstatus) "
									+ " select rp.ntransactionresultcode,rp.npreregno, "
									+ "rp.ntransactiontestcode,rp.ntestgrouptestparametercode,rp.ntestparametercode,rp.nparametertypecode, "
									+ "tgtp.nresultmandatory,tgtp.nreportmandatory,coalesce(tgtf.ntestgrouptestformulacode,"
									+ Enumeration.TransactionStatus.NA.gettransactionstatus()
									+ ") as ntestgrouptestformulacode, tgtp.nunitcode,"
									+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " as ngradecode,"
									+ nApprovalStatus + " as ntransactionstatus," + ""
									+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " as nenforcestatus,"
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
									+ " from registrationparameter rp,testgrouptestparameter tgtp  "
									+ " left outer join testgrouptestformula tgtf on"
									+ " tgtp.ntestgrouptestparametercode = tgtf.ntestgrouptestparametercode  "
									+ " and tgtf.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and tgtf.ntransactionstatus="
									+ Enumeration.TransactionStatus.YES.gettransactionstatus()
									+ " where rp.ntestgrouptestparametercode=tgtp.ntestgrouptestparametercode "
									+ " and rp.npreregno in (" + insertlistpreregno + ")" + " and rp.nsitecode="
									+ userInfo.getNtranssitecode() + ";";

							jdbcTemplate.execute(query3);

							String query4 = "insert into resultparametercomments (ntransactionresultcode,ntransactiontestcode "
									+ ",npreregno, ntestgrouptestparametercode ,ntestparametercode "
									+ ",jsondata,nsitecode, nstatus )  select rp.ntransactionresultcode "
									+ ",rp.ntransactiontestcode,rp.npreregno,rp.ntestgrouptestparametercode,rp.ntestparametercode , '{}'::jsonb,"
									+ userInfo.getNtranssitecode() + ","
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " from registrationparameter rp where npreregno in (" + insertlistpreregno + ")"
									+ " and nsitecode=" + userInfo.getNtranssitecode() + ";";

							jdbcTemplate.execute(query4);

//								String strRegistrationArno = "update registrationarno "
//													+ " set sarno ='" + strIQCformat+ "'"
//													+" where npreregno ="+ Integer.parseInt(listOfPreregno.get(0))
//													+ " and nsitecode="+userInfo.getNtranssitecode();
//			
//								getJdbcTemplate().execute(strRegistrationArno);

							for (int i = 0; i < listOfPreregno.size(); i++) {

								//@SuppressWarnings("unlikely-arg-type")
								String strRegistrationArno = "update registrationarno " + " set sarno ='"
										+ strIQCformat.get(Integer.parseInt(listOfPreregno.get(i))) + "'"
										+ " where npreregno =" + Integer.parseInt(listOfPreregno.get(i)) + " and nsitecode="
										+ userInfo.getNtranssitecode();

								jdbcTemplate.execute(strRegistrationArno);

								for (int l = 0; l < lstRegistrationSubSample.size(); l++) {
									String subsampleArno = "";
									if (lstRegistrationSubSample.get(l).getNpreregno() == Integer
											.parseInt(listOfPreregno.get(i))) {
										// String formatted = String.format("%02d", i+1);
										String formatted = String.format("%02d", 1);
										subsampleArno = "" + strIQCformat.get(Integer.parseInt(listOfPreregno.get(i))) + "-"
												+ formatted;

										String strSampleArno = " update registrationsamplearno " + " set ssamplearno ='"
												+ subsampleArno + "' where ntransactionsamplecode ="
												+ lstRegistrationSubSample.get(l).getNtransactionsamplecode()
												+ " and nsitecode=" + userInfo.getNtranssitecode();
										jdbcTemplate.execute(strSampleArno);

										MapTransactionsampletoArno.put(
												lstRegistrationSubSample.get(l).getNtransactionsamplecode(), subsampleArno);

									}

								}

							}

							// insertChainCustody(nchainCustodyCode,nbatchmastercode,nneedsubsample,userInfo);

							String strArnoSeqno = "update seqnoarnogenerator set nsequenceno = "
									+ (listOfPreregno.size() + seqnoFormat.getNsequenceno()) + " where nseqnoarnogencode ="
									+ seqnoFormat.getNseqnoarnogencode();

							jdbcTemplate.execute(strArnoSeqno);

							String strSectionHistory = " select (tg.nsectioncode),rth.npreregno from "
									+ " registrationtest rt,testgrouptest tg,registrationtesthistory rth "
									+ " where tg.ntestgrouptestcode=rt.ntestgrouptestcode "
									+ " and rth.ntransactiontestcode=rt.ntransactiontestcode "
									+ " and rth.ntesthistorycode = any(select max(ntesthistorycode) from registrationtesthistory rth1 "
									+ " where rth1.ntransactiontestcode=rt.ntransactiontestcode  " + " and rth1.nsitecode="
									+ userInfo.getNtranssitecode() + " and rth1.nstatus= "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ) "
									+ " and rth.npreregno in (" + insertlistpreregno + ") "
									+ " and rt.nsitecode=rth.nsitecode and rth.nsitecode=" + userInfo.getNtranssitecode()
									+ " and rth.ntransactionstatus  not in ("
									+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ","
									+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus()
									+ ") group by rth.npreregno,tg.nsectioncode ";

							List<RegistrationTest> lstRegistrationSectionHistory = jdbcTemplate.query(strSectionHistory,
									new RegistrationTest());

							for (int f = 0; f < lstRegistrationSectionHistory.size(); f++) {

								++regsection;
								strSection = strSection + "(" + regsection + ","
										+ lstRegistrationSectionHistory.get(f).getNpreregno() + ","
										+ lstRegistrationSectionHistory.get(f).getNsectioncode() + ","
										+ userInfo.getNtranssitecode() + ","
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";

								++regsectionhistory;
								strSecHistory = strSecHistory + "(" + regsectionhistory + ","
										+ lstRegistrationSectionHistory.get(f).getNpreregno() + ","
										+ lstRegistrationSectionHistory.get(f).getNsectioncode() + ","
										+ Enumeration.TransactionStatus.REGISTERED.gettransactionstatus() + ","
										+ userInfo.getNtranssitecode() + ","
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";

							}

//									if(!needJobAllocation)
//									{

//										for (int f = 0; f < lstRegistrationSectionHistory.size(); f++) {
//											regsectionhistory++;
//											strSecHistory = strSecHistory + "(" + regsectionhistory + ","
//													+ lstRegistrationSectionHistory.get(f).getNpreregno() + ","
//													+ lstRegistrationSectionHistory.get(f).getNsectioncode() + ","
//													+ Enumeration.TransactionStatus.RECIEVED_IN_SECTION.gettransactionstatus() + ","
//													+ userInfo.getNtranssitecode() + "," 
//													+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
//										}

							// }

							strSection = "insert into registrationsection(nregistrationsectioncode, npreregno, "
									+ "nsectioncode, nsitecode,nstatus) values"
									+ strSection.substring(0, strSection.length() - 1) + ";";

							jdbcTemplate.execute(strSection);

							strSecHistory = "insert into registrationsectionhistory(nsectionhistorycode, npreregno, "
									+ "nsectioncode, ntransactionstatus, nsitecode,nstatus) values"
									+ strSecHistory.substring(0, strSecHistory.length() - 1) + ";";

							jdbcTemplate.execute(strSecHistory);

//							final String querybatch = "select " + " json_agg(a.jsonuidata) from ("
//									+ "	select bi.jsondata || json_build_object(' stransdisplaystatus', max( ts.jsondata -> 'stransdisplaystatus' ->> 'en-US')"
//									+ "	):: jsonb as jsonuidata from batchhistory bh,batchsampleiqc bi,transactionstatus ts "
//									+ "	where bh.nbatchmastercode=bi.nbatchmastercode" + "	and bi.npreregno in ("
//									+ insertlistpreregno + ") and bh.ntransactionstatus=ts.ntranscode and bi.nstatus="
//									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bh.nstatus="
//									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//									+ "	and bh.nbatchhistorycode in (select max(nbatchhistorycode) from batchhistory where nbatchmastercode="
//									+ nbatchmastercode + " and nstatus="
//									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")"
//									+ " group by bi.jsondata )a";
//
//							String strDataold = jdbcTemplate.queryForObject(querybatch, String.class);

							

							final String comments = batchhistory.getScomments() != null
									? " N'" + stringUtilityFunction.replaceQuote(batchhistory.getScomments()) + "'"
									: null;

							String sInsertBatchHist = "insert into batchhistory(nbatchhistorycode,nbatchmastercode,sbatcharno,"
									+ " ndeputyusercode,ndeputyuserrolecode,ntransactionstatus,nusercode,nuserrolecode,dtransactiondate,"
									+ " noffsetdtransactiondate,ntransdatetimezonecode,"
									+ " scomments,nsitecode,nstatus) values(" + startBatchSeqNo + "," + nbatchmastercode
									+ ",'" + strformat + "'," + " " + userInfo.getNdeputyusercode() + ","
									+ userInfo.getNdeputyuserrole() + ","
									+ Enumeration.TransactionStatus.INITIATED.gettransactionstatus() + "," + " "
									+ userInfo.getNusercode() + "," + userInfo.getNuserrole() + ",'" + stransactiondate
									+ "',"
									// + " '"+getCurrentDateTime(userInfo)+"',"
									+ " " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
									+ userInfo.getNtimezonecode() + "," + comments + "," + userInfo.getNtranssitecode()
									+ "," + " " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

							jdbcTemplate.execute(sInsertBatchHist);

							String squerySample = "select * from batchsample where " + " nbatchmastercode = "
									+ nbatchmastercode + " " + " and nsitecode=" + userInfo.getNtranssitecode() + ""
									+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

							List<Batchsample> lstBatchsample = jdbcTemplate.query(squerySample, new Batchsample());

							String lstBatchIqcTrans = "";
							
							for (int i = 0; i <= lstBatchsample.size() - 1; i++) {
								

								lstBatchIqcTrans = lstBatchIqcTrans + " (" + lstBatchsample.get(i).getNtransactiontestcode()
										+ "," + "" + lstBatchsample.get(i).getNpreregno() + ","
										+ lstBatchsample.get(i).getNtransactionsamplecode() + "," + ""
										+ lstBatchsample.get(i).getNbatchmastercode() + "," + ntestcode + "," + "'"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNtranssitecode() + "," + ""
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
								;
							}

							
							for (int i = 0; i <= batchSampleInputList.size() - 1; i++) {

								lstBatchIqcTrans = lstBatchIqcTrans + " ("
										+ batchSampleInputList.get(i).getNtransactiontestcode() + "," + ""
										+ batchSampleInputList.get(i).getNpreregno() + ","
										+ batchSampleInputList.get(i).getNtransactionsamplecode() + "," + ""
										+ batchSampleInputList.get(i).getNbatchmastercode() + "," + ntestcode + "," + "'"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNtranssitecode() + "," + ""
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
							}

							

							batchiqctransaction = "Insert into batchiqctransaction(ntransactiontestcode, npreregno, ntransactionsamplecode, nbatchmastercode,ntestcode,dmodifieddate, "
									+ " nsitecode, nstatus) values "
									+ lstBatchIqcTrans.substring(0, lstBatchIqcTrans.length() - 1) + ";";
							jdbcTemplate.execute(batchiqctransaction);

						}
						if (nneedjoballocationiqc) {

							String query8 = "insert into joballocation (njoballocationcode,npreregno,ntransactionsamplecode,ntransactiontestcode,nsectioncode "
									+ ",nusercode,nuserperiodcode,ninstrumentcategorycode "
									+ ",ninstrumentcode,ninstrumentnamecode,ninstrumentperiodcode, ntechniquecode "
									+ ",ntimezonecode,nsitecode, nstatus,ntestrescheduleno,nuserrolecode,jsondata, jsonuidata) "
									+ "select " + joballocation
									+ "+rank() over(order by rth.ntransactiontestcode),rt.npreregno,rt.ntransactionsamplecode,rt.ntransactiontestcode,tgt.nsectioncode,-1,"
									+ "-1,-1,-1,-1,-1,1,-1," + userInfo.getNtranssitecode() + ","
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "," + " 0,"
									+ " (select nuserrolecode  from treetemplatetransactionrole "
									+ "	where ntemptransrolecode in (select max(ttr.ntemptransrolecode) "
									+ "	from treetemplatetransactionrole ttr,approvalconfig ac,approvalconfigversion acv "
									+ "	where ac.napprovalconfigcode=ttr.napprovalconfigcode "
									+ "	and ac.napprovalconfigcode = acv.napprovalconfigcode "
									+ "	and ttr.ntreeversiontempcode = acv.ntreeversiontempcode " + " and ttr.nstatus= "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "	and acv.nstatus= "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "	and ac.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ "	and ac.nregtypecode= " + nregtypecode + " and ac.nregsubtypecode= "
									+ nregsubtypecode + " and acv.napproveconfversioncode =" + napproveversioncode
									+ " group by ttr.ntreeversiontempcode" + "	)" + ")  nuserrolecode,"
									+ " json_build_object('duserblockfromdate','"
									+ dateUtilityFunction.instantDateToStringWithFormat(dateUtilityFunction.getCurrentDateTime(userInfo),
											"yyyy-MM-dd HH:mm:ss")
									+ "','duserblocktodate','"
									+ dateUtilityFunction.instantDateToStringWithFormat(dateUtilityFunction.getCurrentDateTime(userInfo),
											"yyyy-MM-dd HH:mm:ss")
									+ "','suserblockfromtime','00:00','suserblocktotime','00:00','suserholdduration','0','dinstblockfromdate',NULL,"
									+ "'dinstblocktodate',NULL,'sinstblockfromtime',NULL,'sinstblocktotime',NULL,'sinstrumentholdduration',NULL,"
									+ "'scomments','','noffsetduserblockfromdate','"
									+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "','noffsetduserblocktodate','"
									+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "')::jsonb, "
									+ " json_build_object('duserblockfromdate','"
									+ dateUtilityFunction.instantDateToStringWithFormat(dateUtilityFunction.getCurrentDateTime(userInfo),
											"yyyy-MM-dd HH:mm:ss")
									+ "','duserblocktodate','"
									+ dateUtilityFunction.instantDateToStringWithFormat(dateUtilityFunction.getCurrentDateTime(userInfo),
											"yyyy-MM-dd HH:mm:ss")
									+ "','suserblockfromtime','00:00','suserblocktotime','00:00','suserholdduration','0','dinstblockfromdate',NULL,"
									+ "'dinstblocktodate',NULL,'sinstblockfromtime',NULL,'sinstblocktotime',NULL,'sinstrumentholdduration',NULL,"
									+ "'scomments','','noffsetduserblockfromdate','"
									+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "','noffsetduserblocktodate','"
									+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "')::jsonb "
									+ " from registrationtesthistory rth,registrationtest rt,testgrouptest tgt "
									+ " where rt.ntransactiontestcode=rth.ntransactiontestcode  "
									+ " and tgt.ntestgrouptestcode=rt.ntestgrouptestcode "
									+ " and rth.nsitecode=rt.nsitecode and rt.nsitecode=" + userInfo.getNtranssitecode()
									+ " and rth.ntesthistorycode in "
									+ " (select max(rth1.ntesthistorycode) as testhistorycode from "
									+ " registrationtesthistory rth1,registrationtest rt1,registration r "
									+ " where rth1.ntransactiontestcode = rt1.ntransactiontestcode "
									+ " and rth1.ntransactionstatus="
									+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus()
									+ " and rth1.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ "  " + " and rth1.npreregno  in (" + insertlistpreregno + ")"
									+ " and rth1.nsitecode=rt1.nsitecode and rt1.nsitecode=r.nsitecode and r.nsitecode="
									+ userInfo.getNtranssitecode()
									+ " and rth1.ntransactiontestcode not in (select max(ntransactiontestcode) as ntransactiontestcode"
									+ " from registrationtesthistory where  npreregno  in (" + insertlistpreregno + ")  "
									+ " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and ntransactionstatus="
									+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus()
									+ " group by npreregno,ntransactiontestcode,ntransactionsamplecode) "
									+ " group by rth1.npreregno,rth1.ntransactionsamplecode,rth1.ntransactiontestcode)";
							jdbcTemplate.execute(query8);
						}

//				  String sdmsQuery = " INSERT INTO public.sdmslabsheetmaster(ntransactiontestcode,npreregno,sarno,"
//				  		+ " ntransactionsamplecode,ntestgrouptestcode,ntestcode,nretestno,ntestrepeatcount,sllinterstatus,"
//				  		+ " nusercode, nuserrolecode, suuid, nsitecode, nstatus)"
//				  		+ " select rt.ntransactiontestcode,rt.npreregno,rg.sarno,rs.ntransactionsamplecode,"
//				  		+ " rt.ntestgrouptestcode,rt.ntestcode,"
//				  		+ " rt.ntestretestno,rt.ntestrepeatno,'A' sllinterstatus,"
//				  		+ " "+userInfo.getNusercode()+","+userInfo.getNuserrole()+","
//				  		+ " '"+strformat+"',"+userInfo.getNtranssitecode()+","
//				  		+ " "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//				  		+ " from registrationtest rt,registrationarno rg,registrationsample rs"
//				  		+ " where rt.ntransactiontestcode in("+stransactiontestcode+") and rs.npreregno=rt.npreregno "
//				  		+ " and rg.npreregno=rt.npreregno and rt.ntransactionsamplecode=rs.ntransactionsamplecode "
//				  		+ " and rt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//				  		+ " and rg.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//				  		+ " and rs.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//				  		+ " and rt.nsitecode="+userInfo.getNtranssitecode()+""
//				  		+ " and rg.nsitecode="+userInfo.getNtranssitecode()+""
//				  		+ " and rs.nsitecode="+userInfo.getNtranssitecode()+"";
//				  
//				  getJdbcTemplate().execute(sdmsQuery);

//				  ALPD-4788  Janakumar Run batch--500 error occurs when initiate the batch
						
						//ATE234 Janakumar ALPD-5594 SMTL > Result entry > open ELN sheet displayed empty page, it occurs when open the ELN sheet for run batch test, check description
						//delete sdmslabsheetmaster,sdmslabsheetdetails record by stransactiontestcode for the without going Test Initiate,In the sdmslabsheetmaster,sdmslabsheetdetails pervious put the record . Now delte the exting record & put the new record for the sdmslabsheetmaster,sdmslabsheetdetails     
						
						final String sdmsLapSheetMasterAndDetail ="delete from sdmslabsheetmaster where ntransactiontestcode in("+stransactiontestcode+")  "
								+ "and nsitecode="+userInfo.getNtranssitecode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"
								+ "delete from sdmslabsheetdetails where ntransactiontestcode in("+stransactiontestcode+")  "
								+ "and nsitecode="+userInfo.getNtranssitecode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
						
						jdbcTemplate.execute(sdmsLapSheetMasterAndDetail);
						
				   		
						String sdmsQuery = " INSERT INTO public.sdmslabsheetmaster(ntransactiontestcode,npreregno,sarno,"
								+ " ntransactionsamplecode,ntestgrouptestcode,ntestcode,nretestno,ntestrepeatcount,ninterfacetypecode,sllinterstatus,"
								+ " nusercode, nuserrolecode, suuid, nbatchmastercode, nsitecode, nstatus)"
								+ " select bs.ntransactiontestcode,bs.npreregno," + " '" + strformat
								+ "',bs.ntransactionsamplecode," + "rt.ntestgrouptestcode ,bm.ntestcode,"
								+ "rt.ntestretestno,rt.ntestrepeatno," + " tm.ninterfacetypecode,'A' sllinterstatus," + " "
								+ userInfo.getNusercode() + "," + userInfo.getNuserrole() + "," + " '" + strformat + "',"
								+ nbatchmastercode + "," + userInfo.getNtranssitecode() + "," + " "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
								+ " from batchmaster bm,testmaster tm ,batchsample bs,registrationtest rt "
								+ " where bm.nbatchmastercode=bs.nbatchmastercode and bs.ntransactiontestcode=rt.ntransactiontestcode "
								+ " and bm.nbatchmastercode=" + nbatchmastercode
								+ " and tm.ninterfacetypecode > 0 and bm.ntestcode = tm.ntestcode " + "and tm.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and bm.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and tm.nsitecode="
								+ userInfo.getNmastersitecode() + " " + "and bm.nsitecode=" + userInfo.getNtranssitecode()
								+ " " + " and bs.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " " + " and rt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " ";

						jdbcTemplate.execute(sdmsQuery);

						String sdmsSheetDetailQuery = " INSERT INTO public.sdmslabsheetdetails("
								+ " ntransactionresultcode, npreregno, sarno, ntransactionsamplecode,ntransactiontestcode,"
								+ " ntestgrouptestcode, ntestcode, nretestno,ntestrepeatcount, ntestgrouptestparametercode, "
								+ " ntestparametercode,nparametertypecode, nroundingdigits,"
								+ " sresult, sllinterstatus, sfileid,nlinkcode, nattachedlink, suuid, nbatchmastercode,nsitecode, nstatus)"
								+ " select rp.ntransactionresultcode,rp.npreregno,rarno.sarno,rs.ntransactionsamplecode,"
								+ " rt.ntransactiontestcode,rt.ntestgrouptestcode,rt.ntestcode,rt.ntestretestno,rt.ntestrepeatno,"
								+ " rp.ntestgrouptestparametercode,rp.ntestparametercode,rp.nparametertypecode,(rp.jsondata->>'nroundingdigits')::int nroundingdigits,"
								+ " (rp.jsondata->>'sresult')::int sresult,'A' sllinterstatus,NULL sfileid,-1 nlinkcode,-1 nattachedlink,"
								+ " '" + strformat + "'," + nbatchmastercode + "," + userInfo.getNtranssitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
								+ " from registrationtest rt,resultparameter rp,registrationarno rarno,registrationsample rs,testmaster tm "
								+ " where rt.ntransactiontestcode in(" + stransactiontestcode + ") "
								// + " and rp.ntestgrouptestparametercode=tgtp.ntestgrouptestparametercode "
								+ " and rt.npreregno=rp.npreregno and rt.ntransactiontestcode=rp.ntransactiontestcode"
								+ " and rs.npreregno=rt.npreregno and rt.ntransactionsamplecode=rs.ntransactionsamplecode"
								+ " and rarno.npreregno=rt.npreregno and rt.ntestcode = tm.ntestcode"
								+ " and rt.ntransactionsamplecode=rs.ntransactionsamplecode and tm.ninterfacetypecode > 0"
								+ " and rt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
								+ " and rp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
								+ " and rarno.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
								+ " and rs.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
								+ " and tm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
								+ " and tm.nsitecode=" + userInfo.getNmastersitecode() + "" + " and rt.nsitecode="
								+ userInfo.getNtranssitecode() + "" + " and rp.nsitecode=" + userInfo.getNtranssitecode()
								+ "" + " and rarno.nsitecode=" + userInfo.getNtranssitecode() + "" + " and rs.nsitecode="
								+ userInfo.getNtranssitecode() + "";

						jdbcTemplate.execute(sdmsSheetDetailQuery);

						final String ui_ToDate = (String) inputMap.get("toDate");
						final String toDate = dateUtilityFunction.getCurrentDateTime(userInfo).toString();
						inputMap.put("toDate",toDate.substring(0,toDate.length()-1));
						returnMap.putAll((Map<String, Object>) getBatchmaster(inputMap, userInfo).getBody());
						returnMap.putAll((Map<String, Object>) changeTimeFormatForUi(ui_ToDate, userInfo));
						List<Batchmaster> lstbatchmaster = new ArrayList<>();
						lstbatchmaster.add((Batchmaster) returnMap.get("SelectedBatchmaster"));
						auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
						auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
						auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
						actionType.put("batchmaster", "IDS_BATCHINITIATE");
						jsonAuditObject.put("batchmaster", lstbatchmaster);
						auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);
						return new ResponseEntity<>(returnMap, HttpStatus.OK);
//						}else {
//							return new ResponseEntity<>(sCheckTrainingValidation.get("rtn"), HttpStatus.EXPECTATION_FAILED);
//					    }
					} else {
						return new ResponseEntity<>(schecktechniqueValidation.get("rtn"), HttpStatus.EXPECTATION_FAILED);

					}

				}
			}
		}

		@Override
		public Map<String, Object> seqNoGetforInitiate(int sampleCount, UserInfo userInfo,
				final Map<String, Object> inputMap) throws Exception {
			ObjectMapper objMapper = new ObjectMapper();
			Map<String, Object> mapSeq = new HashMap<String, Object>();
			String sQuery = " lock table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			sQuery = " lock table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			sQuery = " lock table lockcancelreject " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);
//			   sQuery = " lock table lockbatchcreation "+Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//			 getJdbcTemplate().execute(sQuery);

			int nbatchmastercode = (int) inputMap.get("nbatchmastercode");
			int ntestcode = (int) inputMap.get("ntestcode");
			final Batchmaster batchmaster = (Batchmaster) getActivBatch(nbatchmastercode, userInfo);
			String sNbatchsampleCode = (String) inputMap.get("nbatchsampleCode");
			String stransactiontestcode = (String) inputMap.get("ntransactiontestcode");
//			List<String> listOfBatchsampleCod = Arrays.asList(sNbatchsampleCode.split(","));
//			Collections.sort(listOfBatchsampleCod);
			final List<Batchsample> lstbatchsample = (List<Batchsample>) getActiveMultipleSampleById(sNbatchsampleCode,
					userInfo);
			List<RegistrationTestHistory> lstCancelledSample = toCheckSampleSubSampleCancelled(inputMap, userInfo);
			List<Batchmaster> lstCancelledTest = getCancelTestValidation(nbatchmastercode, userInfo);
			List<Batchsample> lstBatchsamples = new ArrayList<>();

			if (lstCancelledSample.size() > 0) {
//				   String lstStransactiontestcode = stransactiontestcode;
//					List<String> listOfStransactiontestcode = Arrays.asList(lstStransactiontestcode.split(","));
//					Collections.sort(listOfStransactiontestcode);
//				   final List<RegistrationTestHistory> lstValidation = lstCancelledSample.stream()
//							.filter(source -> listOfStransactiontestcode.stream()
//									.noneMatch(dest -> source.getNtransactiontestcode() == dest))
//							.collect(Collectors.toList());

				String strTestList = lstCancelledSample.stream().map(x -> String.valueOf(x.getNtransactiontestcode()))
						.distinct().collect(Collectors.joining(","));

				String strSampleList = lstCancelledSample.stream().map(x -> String.valueOf(x.getNtransactionsamplecode()))
						.distinct().collect(Collectors.joining(","));

				String sBatchSampleQuery = "select * from batchsample where ntransactiontestcode not in(" + strTestList
						+ ") " + " and nbatchmastercode=" + nbatchmastercode + " and ntransactionsamplecode not in("
						+ strSampleList + ")" + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and nsitecode="
						+ userInfo.getNtranssitecode() + "";
				lstBatchsamples = jdbcTemplate.query(sBatchSampleQuery, new Batchsample());

				if (lstBatchsamples.size() > 0) {
					String iqcTransactiontestcode = stransactiontestcode;
					Boolean isiqcdata = (Boolean) inputMap.get("isiqcdata");
					stransactiontestcode = lstBatchsamples.stream().map(x -> String.valueOf(x.getNtransactiontestcode()))
							.distinct().collect(Collectors.joining(","));

					if (isiqcdata) {
						String sIqcSample = "select * from batchsampleiqc where ntransactiontestcode in("
								+ iqcTransactiontestcode + ")" + "	and nbatchmastercode=" + nbatchmastercode + "";
						List<BatchsampleIqc> lstBatchIQCsamples = jdbcTemplate.query(sIqcSample, new BatchsampleIqc());
						String iqcFilteredTransactiontestcode = lstBatchIQCsamples.stream()
								.map(x -> String.valueOf(x.getNtransactiontestcode())).distinct()
								.collect(Collectors.joining(","));
						stransactiontestcode = stransactiontestcode + "," + iqcFilteredTransactiontestcode;

					}

					inputMap.put("ntransactiontestcode", stransactiontestcode);
				}
			}
			if (lstBatchsamples.size() == 0 && lstCancelledSample.size() > 0) {
				mapSeq.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), commonFunction
						.getMultilingualMessage("IDS_SUBSAMPLECANCELLEDALREADY", userInfo.getSlanguagefilename()));
			} else if (lstCancelledTest.size() > 0) {
				mapSeq.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						commonFunction.getMultilingualMessage("IDS_TESTCANCELLEDALREADY", userInfo.getSlanguagefilename()));

			}

//			   if(lstCancelledSample.size()>0) {
//				   String  ssamplearno = ((lstCancelledSample.stream()
//	                       .map(object -> String.valueOf(object.getSsamplearno())).collect(Collectors.toList()))
//	                               .stream().distinct().collect(Collectors.toList())).stream()
//	                                       .collect(Collectors.joining(","));
//					 
//					 String sAlert =  ssamplearno+" "+commonFunction.getMultilingualMessage("IDS_SUBSAMPLECANCELLEDALREADY",
//							 userInfo.getSlanguagefilename());
//					mapSeq.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),sAlert);
//			   }
			else if (batchmaster == null) {
				// status code:417
				mapSeq.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()));
			} else if (lstbatchsample.size() == 0) {
				mapSeq.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						commonFunction.getMultilingualMessage("IDS_SAMPLEALREADYDELETED", userInfo.getSlanguagefilename()));

			} else {

				final BatchHistory batchhist = (BatchHistory) getCheckValidation(nbatchmastercode, userInfo);

				if (batchhist.getNtransactionstatus() == Enumeration.TransactionStatus.INITIATED.gettransactionstatus()
						|| batchhist.getNtransactionstatus() == Enumeration.TransactionStatus.COMPLETED
								.gettransactionstatus()) {
					mapSeq.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							commonFunction.getMultilingualMessage(
									Enumeration.ReturnStatus.SELECTDRAFTVERSION.getreturnstatus(),
									userInfo.getSlanguagefilename()));

				} else {
					Map<String, Object> schecktechniqueValidation = getCheckTechnique(ntestcode, userInfo);

					if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(schecktechniqueValidation.get("rtn"))) {

						// Map<String,Object> sCheckTrainingValidation = getUserBasedCerticationExpiry
						// (userInfo);

						// if(sCheckTrainingValidation.get("rtn").equals(Enumeration.ReturnStatus.SUCCESS.getreturnstatus()))
						// {

						Boolean nneedtestinitiate = (Boolean) inputMap.get("nneedtestinitiate");
						Boolean isiqcdata = (Boolean) inputMap.get("isiqcdata");
						String strNpreregno = (String) inputMap.get("npreregno");
						String[] preregnoArray = strNpreregno.split(",");
						List<Integer> newList = Arrays.asList(preregnoArray).stream().map(s -> Integer.parseInt(s))
								.collect(Collectors.toList());
						String InsertPreregno = Joiner.on(",").join(newList);
						// String stransactiontestcode = (String) inputMap.get("ntransactiontestcode");
						Boolean nneedsubsample = (Boolean) inputMap.get("nneedsubsample");
//						int nregtypecode = (int) inputMap.get("nregtypecode");
//						int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
//						int testStartId = (int) inputMap.get("testStartId");
						//boolean needJobAllocation = (boolean) inputMap.get("nneedjoballocation");
						boolean nneedmyjob = (boolean) inputMap.get("nneedmyjob");
						//int regSectionHistoryCode = 0;
						String StrUpdate = "";
						String nMultiUserStatus = "";
						int ntestHistoryCount = 0;
						//int samplecount = 0;
						String sSampleStatus = "";
						String multiPreregArr = "";
						String multiTransSampleArr = "";
						String multiTransTestArr = "";
						List<RegistrationTest> lstSampleCheck = new ArrayList<>();

						if (lstBatchsamples.size() > 0) {
							multiPreregArr = lstBatchsamples.stream().map(x -> String.valueOf(x.getNpreregno())).distinct()
									.collect(Collectors.joining(","));
							multiTransSampleArr = lstBatchsamples.stream()
									.map(x -> String.valueOf(x.getNtransactionsamplecode())).distinct()
									.collect(Collectors.joining(","));
							multiTransTestArr = lstBatchsamples.stream()
									.map(x -> String.valueOf(x.getNtransactiontestcode())).distinct()
									.collect(Collectors.joining(","));
						} else {

							String muluserpreregno = (String) inputMap.get("muluserpreregno");
							String[] muluserpreregnoArray = muluserpreregno.split(",");
							List<Integer> intMuluserpreregnoArray = Arrays.asList(muluserpreregnoArray).stream()
									.map(s -> Integer.parseInt(s)).collect(Collectors.toList());
							multiPreregArr = Joiner.on(",").join(intMuluserpreregnoArray);

							String mulusertransactionsamplecode = (String) inputMap.get("mulusertransactionsamplecode");
							String[] mulusertransactionsamplecodeArray = mulusertransactionsamplecode.split(",");
							List<Integer> intMulusertransactionsamplecodeArray = Arrays
									.asList(mulusertransactionsamplecodeArray).stream().map(s -> Integer.parseInt(s))
									.collect(Collectors.toList());
							multiTransSampleArr = Joiner.on(",").join(intMulusertransactionsamplecodeArray);

							String mulusertransactiontestcode = (String) inputMap.get("mulusertransactiontestcode");
							String[] mulusertransactiontestcodeArray = mulusertransactiontestcode.split(",");
							List<Integer> intMulusertransactiontestcodeArray = Arrays
									.asList(mulusertransactiontestcodeArray).stream().map(s -> Integer.parseInt(s))
									.collect(Collectors.toList());
							multiTransTestArr = Joiner.on(",").join(intMulusertransactiontestcodeArray);
						}

						List<BatchsampleIqc> batchSampleInputList = objMapper.convertValue(inputMap.get("iqcsample"),
								new TypeReference<List<BatchsampleIqc>>() {
								});

//						String sBatchSampleQuery= "select * from batchsample where ntransactiontestcode="+stransactiontestcode+" "
//								+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//								+ " and nsitecode="+userInfo.getNtranssitecode()+"";
//						List<Batchsample> lstBatchsamples =  getJdbcTemplate().query(sBatchSampleQuery,new Batchsample());

						List<Batchsample> batchSampleList = objMapper.convertValue(inputMap.get("batchsample"),
								new TypeReference<List<Batchsample>>() {
								});

						if (nneedtestinitiate) {
							boolean nneedjoballocation = (boolean) inputMap.get("nneedjoballocation");
							ntestHistoryCount = batchSampleInputList.size()
									+ (lstBatchsamples.size() == 0 ? batchSampleList.size() : lstBatchsamples.size());
							if (nneedjoballocation && nneedmyjob) {
								nMultiUserStatus = String
										.valueOf("," + Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus());
							} else if (!nneedjoballocation && nneedmyjob) {
								nMultiUserStatus = "," + Enumeration.TransactionStatus.REGISTERED.gettransactionstatus();
							}

							nMultiUserStatus = Enumeration.TransactionStatus.INITIATED.gettransactionstatus()
									+ nMultiUserStatus;

							sSampleStatus = "Initaited";
							LOGGER.info("sSampleStatus:"+ sSampleStatus);			
						} else {
							ntestHistoryCount = batchSampleInputList.size();
							nMultiUserStatus = String
									.valueOf(Enumeration.TransactionStatus.COMPLETED.gettransactionstatus());
							sSampleStatus = "Completed";
						}

						int nSeqNo = jdbcTemplate.queryForObject(
								" select nsequenceno from seqnobatchcreation where stablename='batchhistory' "
								+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"",
								Integer.class);
						int nSampleIQCSeqNo = jdbcTemplate.queryForObject(
								" select nsequenceno from seqnobatchcreation where stablename='batchsampleiqc'"
								+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"",
								Integer.class);

						final String strSelectSeqno = "select stablename,nsequenceno from seqnoregistration where stablename "
								+ "in ('registrationhistory','registrationsamplehistory','registrationtesthistory',"
								+ "'registrationsection', 'registrationsectionhistory','chaincustody','joballocation')"
								+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";

						List<SeqNoRegistration> lstSeqNo = jdbcTemplate.query(strSelectSeqno, new SeqNoRegistration());
						mapSeq = lstSeqNo.stream().collect(Collectors.toMap(SeqNoRegistration::getStablename,
								SeqNoRegistration -> SeqNoRegistration.getNsequenceno()));
						mapSeq.put("batchhistory", nSeqNo + 1);
						mapSeq.put("batchsampleiqc", nSampleIQCSeqNo);
						mapSeq.put("multiPreregArr", multiPreregArr);
						mapSeq.put("multiTransSampleArr", multiTransSampleArr);
						mapSeq.put("multiTransTestArr", multiTransTestArr);

//						List<TransactionValidation> validationStatusLst = (List<TransactionValidation>) getTransactionValidation(
//								testStartId, nregtypecode, nregsubtypecode, userInfo);
//						final String stransactionStatus = validationStatusLst.stream()
//								.map(objpreregno -> String.valueOf(objpreregno.getNtransactionstatus()))
//								.collect(Collectors.joining(","));

						if (nneedtestinitiate == false) {
							String sSampleCheck = "select case when true=true  then max(rsarno.ssamplearno) "
									+ "  else  max(arno.sarno) end samplearno " + "  from batchsample bs,batchhistory bh,"
									+ "  registrationarno arno ,registrationsamplearno rsarno"
									+ "  where bs.nbatchmastercode <> " + nbatchmastercode + " and " + "  bs.npreregno in ("
									+ multiPreregArr + ") " + "  and bs.ntransactionsamplecode in (" + multiTransSampleArr
									+ ")" + "  and bs.ntransactiontestcode in (" + multiTransTestArr + ")"
									+ "  and bs.nbatchmastercode = bh.nbatchmastercode "
									+ "  and bh.nbatchhistorycode= any(select max(nbatchhistorycode) nbatchhistorycode "
									+ "  from batchhistory bh,batchsample bs "
									+ "  where bs.nbatchmastercode = bh.nbatchmastercode "
									+ "  and bh.ntransactionstatus = "
									+ Enumeration.TransactionStatus.INITIATED.gettransactionstatus() + " "
									+ "  and bh.nbatchmastercode <> " + nbatchmastercode + "" + "  and bs.nsitecode="
									+ userInfo.getNtranssitecode() + "" + "  and bs.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
									+ "  and bh.nsitecode=" + userInfo.getNtranssitecode() + "" + "  and bh.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
									+ "  group by bs.ntransactiontestcode) " + "  and arno.npreregno=bs.npreregno "
									+ "  and rsarno.ntransactionsamplecode = bs.ntransactionsamplecode "
									+ "  and bs.nsitecode=" + userInfo.getNtranssitecode() + "" + "  and bs.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
									+ "  and bh.nsitecode=" + userInfo.getNtranssitecode() + "" + "  and bh.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
									+ "  and arno.nsitecode=" + userInfo.getNtranssitecode() + "" + "  and arno.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
									+ "  and rsarno.nsitecode=" + userInfo.getNtranssitecode() + ""
									+ "  and rsarno.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ "" + "  group by bs.ntransactiontestcode ";

							lstSampleCheck = jdbcTemplate.query(sSampleCheck, new RegistrationTest());
						}
//							String sSampleCheckQuery = " select case when "+nneedsubsample+"=true then rsarno.ssamplearno else arno.sarno end samplearno,"
//									 + " rth.ntesthistorycode,rth.ntransactionstatus"
//									 + " from registrationarno arno ,registrationsamplearno rsarno, registrationsample rs,"
//									 + " registration r,registrationtest rt,registrationtesthistory rth where rth.ntransactiontestcode=rt.ntransactiontestcode "
//									 + " and r.npreregno = rt.npreregno and arno.npreregno = r.npreregno and rsarno.ntransactionsamplecode=rs.ntransactionsamplecode"
//									 + " and rs.ntransactionsamplecode = rt.ntransactionsamplecode and arno.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//									 + " and rsarno.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and rs.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//									 + " and r.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//									 + " and rt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//									 + " and rth.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//									 + " and rth.ntesthistorycode = any(select max(ntesthistorycode) "
//									 + " from registrationtesthistory where nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
//									 + " ntransactiontestcode in("+stransactiontestcode+") group by ntransactiontestcode )"
//			//						 + " and rth.ntransactionstatus not between "+Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()+""
//			//				     	 + "  and "+Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()+"";
//									 + " and rth.ntransactionstatus in  ("+Enumeration.TransactionStatus.INITIATED.gettransactionstatus()+")";

						String sSampleCheckQuery = " select case when " + nneedsubsample + "=true "
								+ " then rsarno.ssamplearno else arno.sarno end samplearno, "
								+ " rt.ntransactiontestcode,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
								+ userInfo.getSlanguagetypecode() + "',"
								+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,rth.ntransactionstatus from registrationtest rt,"
								+ " registrationtesthistory rth ,registrationarno arno ,registrationsamplearno rsarno,transactionstatus ts "
								+ " where rth.ntesthistorycode = any(select max(rth.ntesthistorycode) from registrationtesthistory rth"
								+ " where  rt.ntestcode=" + ntestcode + "" + " and rth.ntransactiontestcode in("
								+ stransactiontestcode + ")" + " and rth.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								+ " group by rth.ntransactiontestcode)" + "  and rth.ntransactionstatus in("
								+ nMultiUserStatus + ") " + "  and ts.ntranscode=rth.ntransactionstatus"
								+ " and rth.ntransactiontestcode=rt.ntransactiontestcode " + " and rt.ntestcode="
								+ ntestcode + "" + " and arno.npreregno=rt.npreregno and rsarno.npreregno=arno.npreregno "
								+ " and rsarno.ntransactionsamplecode=rt.ntransactionsamplecode" + " and rt.npreregno in("
								+ multiPreregArr + ") and rt.ntransactionsamplecode in(" + multiTransSampleArr + ")"
								+ " and arno.nsitecode=" + userInfo.getNtranssitecode() + "" + " and rsarno.nsitecode="
								+ userInfo.getNtranssitecode() + "" + " and arno.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rsarno.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rt.nsitecode="
								+ userInfo.getNtranssitecode() + "" + " and rth.nsitecode=" + userInfo.getNtranssitecode()
								+ "" + " and rt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
								+ " and rth.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

						List<RegistrationTest> lstBasemastervalues = jdbcTemplate.query(sSampleCheckQuery,
								new RegistrationTest());

						if (lstBasemastervalues.size() > 0) {
							/*
							 * String ssamplearno = ((lstBasemastervalues.stream() .map(object ->
							 * String.valueOf(object.getSamplearno())).collect(Collectors.toList()))
							 * .stream().distinct().collect(Collectors.toList())).stream()
							 * .collect(Collectors.joining(","));
							 */
							Map<String, List<RegistrationTest>> sampleByStatus = lstBasemastervalues.stream()
									.collect(Collectors.groupingBy(RegistrationTest::getStransdisplaystatus));
//							final String ssamplestatus = validationStatusLst.stream()
//									.map(objpreregno -> String.valueOf(objpreregno.getStransdisplaystatus()))
//									.collect(Collectors.joining(","));
							final String ssamplearno = sampleByStatus.entrySet().stream().map(entry -> {
								String sstatus = entry.getKey();

								List<RegistrationTest> empList = entry.getValue();
								String sarno = empList.stream().map(RegistrationTest::getSamplearno) // Assuming
																										// RegistrationTest
																										// has a meaningful
																										// toString method
										.collect(Collectors.joining(", "));
								return sarno + '-' + sstatus;
							}).collect(Collectors.joining(", "));
//								 String sAlert =  ssamplearno+" "+commonFunction.getMultilingualMessage("IDS_IS",
//										 userInfo.getSlanguagefilename())+" "+commonFunction.getMultilingualMessage("IDS_ALREADY",
//										 userInfo.getSlanguagefilename())+" "+ssamplestatus;

							String sAlert = ssamplearno + " " + commonFunction.getMultilingualMessage("IDS_VALIADTESAMPLE",
									userInfo.getSlanguagefilename());
//										 userInfo.getSlanguagefilename())+" "+commonFunction.getMultilingualMessage("IDS_ALREADY",
//										 userInfo.getSlanguagefilename())+" "+ssamplestatus;

							// return new ResponseEntity<>(sAlert, HttpStatus.EXPECTATION_FAILED);
							mapSeq.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), sAlert);
						} else if (lstSampleCheck.size() > 0) {

							String ssamplearno = ((lstSampleCheck.stream()
									.map(object -> String.valueOf(object.getSamplearno())).collect(Collectors.toList()))
									.stream().distinct().collect(Collectors.toList())).stream()
									.collect(Collectors.joining(","));

							String sAlert = ssamplearno + " " + commonFunction
									.getMultilingualMessage("IDS_ALREADYINITIATED", userInfo.getSlanguagefilename());
//										 userInfo.getSlanguagefilename())+" "+commonFunction.getMultilingualMessage("IDS_ALREADY",
//										 userInfo.getSlanguagefilename())+" "+ssamplestatus;

							// return new ResponseEntity<>(sAlert, HttpStatus.EXPECTATION_FAILED);
							mapSeq.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), sAlert);

						} else if (isiqcdata == false) {
							StrUpdate = " update seqnoregistration set nsequenceno = "
									+ ((int) mapSeq.get("registrationtesthistory")
											+ (lstBatchsamples.size() == 0 ? batchSampleList.size()
													: lstBatchsamples.size()))
									+ " where stablename ='registrationtesthistory' "
									+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"

									+ " update seqnobatchcreation set nsequenceno = " + ((int) mapSeq.get("batchhistory"))
									+ " where stablename ='batchhistory' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"

									+ "update seqnoregistration set nsequenceno = "
									+ ((int) mapSeq.get("chaincustody") + ntestHistoryCount)
									+ " where stablename ='chaincustody' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";

							jdbcTemplate.execute(StrUpdate);
							mapSeq.put("insertlistpreregno", InsertPreregno);
							mapSeq.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
									Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
						} else {
							StrUpdate = "update seqnoregistration set nsequenceno = "
									+ ((int) mapSeq.get("registrationhistory") + batchSampleInputList.size())
									+ " where stablename ='registrationhistory' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"

									+ "update seqnoregistration set nsequenceno = "
									+ ((int) mapSeq.get("registrationsamplehistory") + batchSampleInputList.size())
									+ " where stablename ='registrationsamplehistory'and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"

									+ "update seqnoregistration set nsequenceno = "
									+ ((int) mapSeq.get("registrationtesthistory") + ntestHistoryCount)
									+ " where stablename ='registrationtesthistory' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"

									+ "update seqnoregistration set nsequenceno = "
									+ ((int) mapSeq.get("chaincustody") + ntestHistoryCount)
									+ " where stablename ='chaincustody' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"

									+ "update seqnoregistration set nsequenceno = "
									+ ((int) mapSeq.get("joballocation") + batchSampleInputList.size())
									+ " where stablename ='joballocation' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"

									+ "update seqnoregistration set nsequenceno = "
									+ ((int) mapSeq.get("registrationsectionhistory") + batchSampleInputList.size())
									+ " where stablename ='registrationsectionhistory' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"

									+ "update seqnoregistration set nsequenceno = "
									+ ((int) mapSeq.get("registrationsection") + batchSampleInputList.size())
									+ " where stablename ='registrationsection' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"

									+ "update seqnobatchcreation set nsequenceno = " + ((int) mapSeq.get("batchhistory"))
									+ " where stablename ='batchhistory' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";

							jdbcTemplate.execute(StrUpdate);

							int endseqno = ((int) mapSeq.get("registrationtesthistory") + 1) + sampleCount;
							mapSeq.put("nendTesthistorycode", endseqno);
							mapSeq.put("insertlistpreregno", InsertPreregno);

							mapSeq.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
									Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
						} // return mapSeq;
//					}else {
//						mapSeq.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),sCheckTrainingValidation.get("rtn"));
//						  }
					} else {
					
						mapSeq.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
								schecktechniqueValidation.get("rtn"));

					}
				}

			}
			return mapSeq;
		}

		@SuppressWarnings("unchecked")
		@Override
		public ResponseEntity<Object> getActiveSelectedBatchmasterByID(Map<String, Object> inputMap, UserInfo userInfo)
				throws Exception {
			// TODO Auto-generated method stub
			int nbatchmastercode = (int) inputMap.get("nbatchmastercode");
			int nsampletypecode = (int) inputMap.get("nsampletypecode");
			String stablename = "";
			String fieldname = "";
			String selectField = "";

			final Batchmaster batchmaster = (Batchmaster) getActivBatch(nbatchmastercode, userInfo);
			if (batchmaster == null) {
				// status code:417
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {

				final Map<String, Object> returnMap = new HashMap<String, Object>();
				final String getTest = "select tm.ntestcode,tm.stestname,max(bm.nsectioncode)nsectioncode,max(s.ssectionname)ssectionname "
						+ " from  registrationtesthistory rth,registrationtest rt,testmaster tm,batchmaster bm,section s "
						+ " where rth.ntransactiontestcode=rt.ntransactiontestcode and rt.nsectioncode = s.nsectioncode "
						+ " and rt.ntestcode=tm.ntestcode and bm.ntestcode=tm.ntestcode "
						+ " and rt.nsectioncode = any (select ls.nsectioncode from sectionusers su,labsection ls where "
						+ "	su.nusercode = " + userInfo.getNusercode() + " and ls.nlabsectioncode=su.nlabsectioncode "
						+ " and su.nsitecode= " + userInfo.getNtranssitecode() + "" + " and ls.nsitecode= "
						+ userInfo.getNtranssitecode() + "" + "	and ls.nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and su.nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " group by ls.nsectioncode) "
						+ " and rth.nsitecode=" + userInfo.getNtranssitecode() + "" + " and rt.nsitecode="
						+ userInfo.getNtranssitecode() + "" + " and tm.nsitecode=" + userInfo.getNmastersitecode() + ""
						+ " and bm.nsitecode=" + userInfo.getNtranssitecode() + "" + " and s.nsitecode="
						+ userInfo.getNmastersitecode() + "" + " and rth.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rt.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and tm.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and bm.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and s.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and bm.nbatchmastercode="
						+ nbatchmastercode + ""
						+ " and rth.ntesthistorycode = any (select max(rth.ntesthistorycode)from registrationtesthistory rth,"
						+ " registrationtest rt where rt.ntransactiontestcode=rth.ntransactiontestcode "
						+ " and rt.ninstrumentcatcode >= " + Enumeration.TransactionStatus.NA.gettransactionstatus() + ""
						+ " and rth.ntransactionstatus between "
						+ Enumeration.TransactionStatus.REGISTERED.gettransactionstatus() + "" + " and "
						+ Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus() + "" + " and rth.nsitecode="
						+ userInfo.getNtranssitecode() + "" + " and rt.nsitecode=" + userInfo.getNtranssitecode() + ""
						+ " and rth.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " and rt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " group by rth.ntransactiontestcode)" + " group by tm.ntestcode order by ntestcode desc";

				List<RegistrationTest> lstBasemastervalues = jdbcTemplate.query(getTest, new RegistrationTest());
				// Map<String, Object> objTest = (Map<String, Object>)
				// getTestBasedOnCombo(inputMap,userInfo).getBody();

				if (!lstBasemastervalues.isEmpty()) {
					// returnMap.put("Testvalues", lstBasemastervalues);
					returnMap.put("selectedTestSynonym", lstBasemastervalues.get(0));
				} else {
					returnMap.put("selectedTeystSynonym", null);
				}

				String getSection = " select s.ssectionname,s.nsectioncode,bm.nbatchmastercode"
						+ " from section s,batchmaster bm " + " where bm.nsectioncode=s.nsectioncode "
						+ " and bm.nbatchmastercode=" + nbatchmastercode + "" + " and s.nsitecode="
						+ userInfo.getNmastersitecode() + "" + " and bm.nsitecode=" + userInfo.getNtranssitecode() + ""
						+ " and s.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " and bm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

				List<Section> lstSection = jdbcTemplate.query(getSection, new Section());

				if (!lstSection.isEmpty()) {
					returnMap.put("selectedSection", lstSection.get(0));
				} else {
					returnMap.put("selectedSection", null);
				}

				String getInstrumentCategory = " select ic.ninstrumentcatcode,ic.sinstrumentcatname"
						+ " from instrumentcategory ic,batchmaster bm "
						+ " where bm.ninstrumentcatcode=ic.ninstrumentcatcode " + " and bm.nbatchmastercode="
						+ nbatchmastercode + "" + " and ic.nsitecode=" + userInfo.getNmastersitecode() + ""
						+ " and bm.nsitecode=" + userInfo.getNtranssitecode() + "" + " and ic.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and bm.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

				List<InstrumentCategory> lstInstrumentCat = jdbcTemplate.query(getInstrumentCategory,
						new InstrumentCategory());

				Map<String, Object> objInstCat = (Map<String, Object>) getTestBasedInstrumentCat(inputMap, userInfo)
						.getBody();

				if (!lstInstrumentCat.isEmpty()) {
					returnMap.put("selectedInstrumentCategory", lstInstrumentCat.get(0));
				} else {
					returnMap.put("selectedInstrumentCategory", null);
				}
				List<Object> lstobjInstCat = (List<Object>) objInstCat.get("instrumentCategory");

				if (lstobjInstCat.size() > 0) {
					returnMap.put("instrumentCategory", objInstCat.get("instrumentCategory"));
				} else {
					returnMap.put("instrumentCategory", lstInstrumentCat);
				}

				if ((int) inputMap.get("nsampletypecode") == Enumeration.SampleType.PROJECTSAMPLETYPE.getType()) {

					String getProjectmaster = "select * from projectmaster pm,batchmaster bm "
							+ " where bm.nprojectmastercode=pm.nprojectmastercode " + " and bm.nbatchmastercode="
							+ nbatchmastercode + "" + " and bm.nsitecode=" + userInfo.getNtranssitecode() + ""
							+ " and bm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
							+ " and pm.nsitecode=" + userInfo.getNtranssitecode() + "" + " and pm.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

					List<ProjectMaster> lstProjectmaster = jdbcTemplate.query(getProjectmaster, new ProjectMaster());

					if (!lstProjectmaster.isEmpty()) {
						returnMap.put("selectedProjectedCode", lstProjectmaster.get(0));
					} else {
						returnMap.put("selectedProjectedCode", null);
					}

					List<Object> lstobjProjectCode = (List<Object>) objInstCat.get("ProjectCode");

					if (lstobjProjectCode.size() > 0) {
						returnMap.put("ProjectCode", objInstCat.get("ProjectCode"));
					} else {
						returnMap.put("ProjectCode", lstProjectmaster);
					}

				}

//				if (!lstBasemastervalues.isEmpty()) {
//					returnMap.put("instrumentCategory", objInstCat.get("instrumentCategory"));
//					returnMap.put("selectedInstrumentCategory", lstInstrumentCat.get(0));
//				} else {
//					returnMap.put("selectedInstrumentCategory", null);
//				}

				String sInstQuery = "select i.ninstrumentcode,i.ninstrumentnamecode,i.ninstrumentcatcode,"
						+ " i.sinstrumentname,i.sinstrumentid,ic.sinstrumentcatname,ic.ncalibrationreq"
						+ " from instrument i,instrumentcategory ic,batchmaster bm where" + " bm.nbatchmastercode ="
						+ nbatchmastercode + "" + " and bm.ninstrumentcode = i.ninstrumentcode"
						+ " and i.ninstrumentcatcode=ic.ninstrumentcatcode " + " and ic.nsitecode="
						+ userInfo.getNmastersitecode() + "" + " and i.nsitecode=" + userInfo.getNmastersitecode() + ""
						+ " and bm.nsitecode=" + userInfo.getNtranssitecode() + "" + " and i.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and bm.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and ic.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

				List<Instrument> lstInstrument = jdbcTemplate.query(sInstQuery, new Instrument());
				inputMap.put("ninstrumentcatcode", lstInstrument.get(0).getNinstrumentcatcode());
				inputMap.put("ncalibrationreq", lstInstrument.get(0).getNcalibrationreq());
				Map<String, Object> objInst = (Map<String, Object>) getInstrument(inputMap, userInfo).getBody();

				List<Object> lstobjInst = (List<Object>) objInst.get("instrument");

				if (lstobjInst.size() > 0) {
					returnMap.put("instrument", objInst.get("instrument"));
				} else {
					returnMap.put("instrument", lstInstrument);
				}

				if (!lstInstrument.isEmpty()) {
					// returnMap.put("instrument", objInst.get("instrument"));
					returnMap.put("selectedInstrument", lstInstrument.get(0));
					inputMap.put("ninstrumentnamecode", lstInstrument.get(0).getNinstrumentnamecode());

					String sInstID = " select case when bm.sinstrumentid='null' then 'NA' else bm.sinstrumentid end sinstrumentid,"
							+ " i.ninstrumentcode, i.ninstrumentcatcode" + " from instrument i,batchmaster bm where"
							+ " bm.nbatchmastercode =" + nbatchmastercode + ""
							+ " and bm.ninstrumentcode = i.ninstrumentcode" + " and i.nsitecode="
							+ userInfo.getNmastersitecode() + "" + " and bm.nsitecode=" + userInfo.getNtranssitecode() + ""
							+ " and i.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
							+ " and bm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

					List<Instrument> lstInstrumentId = jdbcTemplate.query(sInstID, new Instrument());
//					int ninstrumentnamecode = (int) inputMap.get("ninstrumentnamecode");
//					int ninstrumentcatcode = (int) inputMap.get("ninstrumentcatcode");
//					int ninstrumentcode = (int) inputMap.get("ninstrumentcode");
					if ((int) inputMap.get("ninstrumentcode") != -1) {
						returnMap.putAll((Map<String, Object>) getInstrumentID(inputMap, userInfo).getBody());
					} else {
						returnMap.put("instrumentID", lstInstrumentId);
					}
					returnMap.put("selectedInstrumentId", lstInstrumentId.get(0));
				} else {
					returnMap.put("selectedInstrument", null);
				}

				if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()) {
					stablename = "component";
					fieldname = "ncomponentcode";
					selectField = "scomponentname";
				} else {
					stablename = "product";
					fieldname = "nproductcode";
					selectField = "sproductname";
				}
				String sProduct = "select p." + fieldname + " nproductcode,p." + selectField + " sproductname from "
						+ stablename + " p,batchmaster bm " + " where bm.nproductcode = p." + fieldname + ""
						+ " and p.nsitecode=" + userInfo.getNmastersitecode() + "" + " and bm.nsitecode="
						+ userInfo.getNtranssitecode() + "" + " and bm.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and p.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and bm.nbatchmastercode ="
						+ nbatchmastercode + "";

				List<Product> lstProduct = jdbcTemplate.query(sProduct, new Product());

				Map<String, Object> objProduct = (Map<String, Object>) getProduct(inputMap, userInfo).getBody();

				if (!lstProduct.isEmpty()) {
					// returnMap.put("product", objProduct.get("product"));
					returnMap.put("selectedProduct", lstProduct.get(0));
				} else {
					returnMap.put("selectedProduct", null);
				}

				List<Object> lstObjProduct = (List<Object>) objProduct.get("product");

				if (lstObjProduct.size() > 0) {
					returnMap.put("product", objProduct.get("product"));
				} else {
					returnMap.put("product", lstProduct);
				}

				return new ResponseEntity<>(returnMap, HttpStatus.OK);

			}

		}

//		@SuppressWarnings("unchecked")
//		@Override	
//		public ResponseEntity<Object> getActiveSelectedBatchmasterByID(Map<String, Object> inputMap, UserInfo userInfo)
//				throws Exception {
//			// TODO Auto-generated method stub
//			int nbatchmastercode = (int) inputMap.get("nbatchmastercode");
//			int nsampletypecode = (int) inputMap.get("nsampletypecode");
//			String stablename="";
//			String fieldname="";
//			String selectField="";
//			
//		    final Batchmaster batchmaster = (Batchmaster) getActivBatch(nbatchmastercode,userInfo);
//			 if (batchmaster == null) {
//					//status code:417
//					return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
//				}else  {
//			
//			 
//			final Map<String, Object> returnMap = new HashMap<String, Object>();
//			final String getTest = "select tm.ntestcode,tm.stestname,max(bm.nsectioncode)nsectioncode,max(s.ssectionname)ssectionname "
//					+ " from  registrationtesthistory rth,registrationtest rt,testmaster tm,batchmaster bm,section s "
//					+ " where rth.ntransactiontestcode=rt.ntransactiontestcode and rt.nsectioncode = s.nsectioncode "
//					+ " and rt.ntestcode=tm.ntestcode and bm.ntestcode=tm.ntestcode "
//					+ " and rt.nsectioncode = any (select ls.nsectioncode from sectionusers su,labsection ls where "
//					+ "	su.nusercode = "+userInfo.getNusercode()+" and ls.nlabsectioncode=su.nlabsectioncode "
//					+ " and su.nsitecode= "+userInfo.getNtranssitecode()+""
//					+ " and ls.nsitecode= "+userInfo.getNtranssitecode()+""
//					+ "	and ls.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and su.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
//					+ " group by ls.nsectioncode) "
//					+ " and rth.nsitecode="+userInfo.getNtranssitecode()+""
//					+ " and rt.nsitecode="+userInfo.getNtranssitecode()+""
//					+ " and tm.nsitecode="+userInfo.getNmastersitecode()+""
//					+ " and bm.nsitecode="+userInfo.getNtranssitecode()+""
//					+ " and s.nsitecode="+userInfo.getNmastersitecode()+""
//					+ " and rth.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and rt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and tm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and bm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and s.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and bm.nbatchmastercode="+nbatchmastercode+""
//					+ " and rth.ntesthistorycode = any (select max(rth.ntesthistorycode)from registrationtesthistory rth,"
//					+ " registrationtest rt where rt.ntransactiontestcode=rth.ntransactiontestcode and rt.ninstrumentcatcode > 0 "
//					+ " and rth.ntransactionstatus between "+Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()+""
//					+ " and "+Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()+""
//					+ " and rth.nsitecode="+userInfo.getNtranssitecode()+""
//					+ " and rt.nsitecode="+userInfo.getNtranssitecode()+""
//					+ " and rth.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and rt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " group by rth.ntransactiontestcode)"
//					+ " group by tm.ntestcode order by ntestcode desc";
//			
//				List<RegistrationTest> lstBasemastervalues = getJdbcTemplate().query(getTest,new RegistrationTest());
//				//Map<String, Object> objTest = (Map<String, Object>) getTestBasedOnCombo(inputMap,userInfo).getBody();
//				
//				if (!lstBasemastervalues.isEmpty()) {
//					//returnMap.put("Testvalues", lstBasemastervalues);
//					returnMap.put("selectedTestSynonym", lstBasemastervalues.get(0));
//				} else {
//					returnMap.put("selectedTeystSynonym", null);
//				}
//				
//				String getSection = " select s.ssectionname,s.nsectioncode,bm.nbatchmastercode"
//						+ " from section s,batchmaster bm "
//						+ " where bm.nsectioncode=s.nsectioncode "
//						+ " and bm.nbatchmastercode="+nbatchmastercode+""
//						+ " and s.nsitecode="+userInfo.getNmastersitecode()+""
//						+ " and bm.nsitecode="+userInfo.getNtranssitecode()+""
//						+ " and s.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//						+ " and bm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
//				
//				List<Section> lstSection = getJdbcTemplate().query(getSection,new Section());
//				
//				if(!lstSection.isEmpty()) {
//					returnMap.put("selectedSection", lstSection.get(0));
//				}else {
//					returnMap.put("selectedSection", null);
//				}
//				
//				String getInstrumentCategory = " select ic.ninstrumentcatcode,ic.sinstrumentcatname"
//						+ " from instrumentcategory ic,batchmaster bm "
//						+ " where bm.ninstrumentcatcode=ic.ninstrumentcatcode "
//						+ " and bm.nbatchmastercode="+nbatchmastercode+""
//						+ " and ic.nsitecode="+userInfo.getNmastersitecode()+""
//						+ " and bm.nsitecode="+userInfo.getNtranssitecode()+""
//						+ " and ic.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//						+ " and bm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
//				
//				List<InstrumentCategory> lstInstrumentCat = getJdbcTemplate().query(getInstrumentCategory,new InstrumentCategory());
//				
//				
//				String getProjectmaster = "select * from projectmaster pm,batchmaster bm "
//						+ " where bm.nprojectmastercode=pm.nprojectmastercode "
//						+ " and bm.nbatchmastercode="+nbatchmastercode+""
//						+ " and bm.nsitecode="+userInfo.getNtranssitecode()+""
//						+ " and bm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//						+ " and pm.nsitecode="+userInfo.getNmastersitecode()+""
//						+ " and pm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
//				
//				List<ProjectMaster> lstProjectmaster = getJdbcTemplate().query(getProjectmaster,new ProjectMaster());
//				
//				
//				
//				Map<String, Object> objInstCat =  (Map<String, Object>) getTestBasedInstrumentCat(inputMap,userInfo).getBody();
//				
//				if(!lstInstrumentCat.isEmpty()) {
//					returnMap.put("selectedInstrumentCategory", lstInstrumentCat.get(0));
//				} else {
//					returnMap.put("selectedInstrumentCategory", null);
//				}
//				    returnMap.put("instrumentCategory", objInstCat.get("instrumentCategory"));
//				    
	//
//			   if(!lstProjectmaster.isEmpty()) {
//						returnMap.put("selectedProjectedCode", lstProjectmaster.get(0));
//					} else {
//						returnMap.put("selectedProjectedCode", null);
//					}
//			   			returnMap.put("ProjectCode", objInstCat.get("ProjectCode"));
	//	
////				if (!lstBasemastervalues.isEmpty()) {
////					returnMap.put("instrumentCategory", objInstCat.get("instrumentCategory"));
////					returnMap.put("selectedInstrumentCategory", lstInstrumentCat.get(0));
////				} else {
////					returnMap.put("selectedInstrumentCategory", null);
////				}
//				
//				
//				String sInstQuery = "select i.ninstrumentcode,i.ninstrumentnamecode,i.ninstrumentcatcode,"
//						+ " i.sinstrumentname,i.sinstrumentid,ic.sinstrumentcatname,ic.ncalibrationreq"
//						+ " from instrument i,instrumentcategory ic,batchmaster bm where"
//						+ " bm.nbatchmastercode ="+nbatchmastercode+""
//						+ " and bm.ninstrumentcode = i.ninstrumentcode"
//						+ " and i.ninstrumentcatcode=ic.ninstrumentcatcode "
//						+ " and ic.nsitecode="+userInfo.getNmastersitecode()+""
//						+ " and i.nsitecode="+userInfo.getNmastersitecode()+""
//						+ " and bm.nsitecode="+userInfo.getNtranssitecode()+""
//						+ " and i.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//						+ " and bm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//						+ " and ic.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
//				
//				List<Instrument> lstInstrument = getJdbcTemplate().query(sInstQuery,new Instrument());
//				inputMap.put("ninstrumentcatcode", lstInstrument.get(0).getNinstrumentcatcode());
//				inputMap.put("ncalibrationreq", lstInstrument.get(0).getNcalibrationreq());
//				Map<String, Object> objInst = (Map<String, Object>) getInstrument(inputMap,userInfo).getBody();
//				
//				if(!lstInstrument.isEmpty()) {
//					//returnMap.put("instrument", objInst.get("instrument"));
//					returnMap.put("selectedInstrument", lstInstrument.get(0));
//					inputMap.put("ninstrumentnamecode", lstInstrument.get(0).getNinstrumentnamecode());
//					
//					String sInstID = " select case when bm.sinstrumentid='null' then 'NA' else bm.sinstrumentid end sinstrumentid,"
//							+ " i.ninstrumentcode, i.ninstrumentcatcode"
//							+ " from instrument i,batchmaster bm where"
//							+ " bm.nbatchmastercode ="+nbatchmastercode+""
//							+ " and bm.ninstrumentcode = i.ninstrumentcode"
//							+ " and i.nsitecode="+userInfo.getNmastersitecode()+""
//							+ " and bm.nsitecode="+userInfo.getNtranssitecode()+""
//							+ " and i.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//							+ " and bm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
//					
//					List<Instrument> lstInstrumentId = getJdbcTemplate().query(sInstID,new Instrument());
//					int ninstrumentnamecode = (int)inputMap.get("ninstrumentnamecode");
//					int ninstrumentcatcode = (int)inputMap.get("ninstrumentcatcode");
//					int ninstrumentcode = (int)inputMap.get("ninstrumentcode");
//					if((int)inputMap.get("ninstrumentcode") != -1) {
//					returnMap.putAll((Map<String,Object>) getInstrumentID(inputMap, userInfo).getBody());
//					}else {
//					returnMap.put("instrumentID", new ArrayList());
//					}
//					returnMap.put("selectedInstrumentId", lstInstrumentId.get(0));
//				}else {
//					returnMap.put("selectedInstrument", null);
//				}
//				    returnMap.put("instrument", objInst.get("instrument"));
//				    
//	        	 if(nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()) {
//		 			    stablename = "component";
//		 			    fieldname ="ncomponentcode";
//		 			    selectField = "scomponentname";
//		 		   }else {
//		 			   stablename ="product";
//		 			   fieldname ="nproductcode";
//		 			   selectField="sproductname";
//		 		   }
//	        	 String sProduct = "select p."+fieldname +" nproductcode,p."+selectField+" sproductname from "+stablename+" p,batchmaster bm "
//	 					+ " where bm.nproductcode = p."+fieldname+""
//						+ " and p.nsitecode="+userInfo.getNmastersitecode()+""
//						+ " and bm.nsitecode="+userInfo.getNtranssitecode()+""
//	 					+ " and bm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//	 					+ " and p.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//	 					+ " and bm.nbatchmastercode ="+nbatchmastercode+"";
//	 		
//	         
//	           List<Product> lstProduct = getJdbcTemplate().query(sProduct,new Product());
//				
//				
//	            Map<String, Object> objProduct = (Map<String, Object>) getProduct(inputMap, userInfo).getBody();
//				
//				if(!lstProduct.isEmpty()){
//					   //returnMap.put("product", objProduct.get("product"));
//					   returnMap.put("selectedProduct", lstProduct.get(0));
//				   }else{
//					   returnMap.put("selectedProduct", null);
//				   }
//				       returnMap.put("product", objProduct.get("product"));
//				return new ResponseEntity<>(returnMap, HttpStatus.OK);
//				
//			}
	//
//		}

		@SuppressWarnings("unchecked")
		@Override
		public ResponseEntity<Object> updateBatchcreation(Map<String, Object> inputMap, UserInfo userInfo)
				throws Exception {
			ObjectMapper objMapper = new ObjectMapper();
			int nbatchmastercode = (int) inputMap.get("nbatchmastercode");
			int nprevioustestcode = (int) inputMap.get("nprevioustestcode");
			int ndesigntemplatemappingcode = (int) inputMap.get("ndesigntemplatemappingcode");
			int nsampletypecode = (int) inputMap.get("nsampletypecode");
			Map<String, Object> returnMap = new HashMap<>();
			final List<Object> beforeSavedSubmitterList = new ArrayList<>();

			final Batchmaster batchmaster = (Batchmaster) getActivBatch(nbatchmastercode, userInfo);
			Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
			JSONObject actionType = new JSONObject();
			JSONObject jsonAuditNew = new JSONObject();
			JSONObject jsonAuditOld = new JSONObject();
			Batchmaster objbatchmaster = objMapper.convertValue(inputMap.get("SelectedBatchmaster"),
					new TypeReference<Batchmaster>() {
					});
			if (batchmaster == null) {
				// status code:417
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				if (nprevioustestcode != (int) inputMap.get("ntestcode")) {

					String sUpdateSample = "update batchsample set nstatus="
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ""
							+ " where nbatchmastercode = " + inputMap.get("nbatchmastercode") + "";

					jdbcTemplate.execute(sUpdateSample);
				}

				String instrumentID = "";
				if (inputMap.containsKey("sinstrumentid") && inputMap.get("sinstrumentid") != null) {
					instrumentID = (String) inputMap.get("sinstrumentid");
				}

				String sUpdateQuery = " update batchmaster set nsampletypecode = " + inputMap.get("nsampletypecode") + ","
						+ " nregtypecode = " + inputMap.get("nregtypecode") + ",nregsubtypecode="
						+ inputMap.get("nregsubtypecode") + "," + " ntestcode =" + inputMap.get("ntestcode")
						+ ",ninstrumentcatcode=" + inputMap.get("ninstrumentcatcode") + "," + " ninstrumentcode="
						+ inputMap.get("ninstrumentcode") + ",sinstrumentid=N'" + stringUtilityFunction.replaceQuote(instrumentID) + "',"
						+ " nproductcode = " + inputMap.get("nproductcode") + "," + " nsectioncode ="
						+ inputMap.get("nsectioncode") + "" + " where nbatchmastercode = "
						+ inputMap.get("nbatchmastercode") + "";

				jdbcTemplate.execute(sUpdateQuery);
				if (batchmaster.getNproductcode() != (int) inputMap.get("nproductcode")) {

					String sUpdateSample = "UPDATE batchsample " + " SET  nstatus="
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "" 
							+ " WHERE nbatchmastercode="
							+ inputMap.get("nbatchmastercode") + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

					jdbcTemplate.execute(sUpdateSample);
					//Janakumar ALPD-4923  Run Batch screen -> System allow to save the record while edit and use the another sample type in run batch screen. ( Note: It allowing to save the record but it displaying valid sample type in sample tab ) 
					final String IQCSample = "select npreregno from batchsampleiqc where nbatchmastercode="
							+ inputMap.get("nbatchmastercode") + " " + "and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by 1 asc";

					final List<Map<String, Object>> sampleItrateIQC = jdbcTemplate.queryForList(IQCSample);

					if (!sampleItrateIQC.isEmpty()) {

						String sUpdateSampleIQC = "UPDATE batchsampleiqc " + " SET  nstatus="
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ""
								+ " WHERE nbatchmastercode=" + inputMap.get("nbatchmastercode") + " and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

						jdbcTemplate.execute(sUpdateSampleIQC);

						sampleItrateIQC.stream().forEach(sample -> {
						    String materialIQC = "UPDATE materialinventorytransaction " +
						            "SET nstatus = " + Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " " +
						            "WHERE (jsondata->>'npreregno')::int = " + sample.get("npreregno") + " " +
						            "AND nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

						    jdbcTemplate.execute(materialIQC);
						});


					}
					sampleItrateIQC.clear();

				}

				// afterSavedSubmitterList.add(objSubmitter);
				beforeSavedSubmitterList.add(batchmaster);
				// fnInsertAuditAction(afterSavedSubmitterList, 2, beforeSavedSubmitterList,
				// Arrays.asList("IDS_EDITBATCHMASTER"),userInfo);
				returnMap.putAll((Map<String, Object>) getActiveSelectedBatchmaster(nbatchmastercode, userInfo,
						ndesigntemplatemappingcode, nsampletypecode).getBody());
				List<Batchmaster> lstbatchmaster = new ArrayList<>();
				List<Batchmaster> lstOldbatchmaster = new ArrayList<>();
				lstbatchmaster.add((Batchmaster) returnMap.get("SelectedBatchmaster"));
				lstOldbatchmaster.add(objbatchmaster);
				auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
				auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
				auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
				actionType.put("batchmaster", "IDS_EDITBATCHCREATION");
				jsonAuditNew.put("batchmaster", lstbatchmaster);
				jsonAuditOld.put("batchmaster", lstOldbatchmaster);
				auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, jsonAuditNew, actionType, auditmap, false, userInfo);
			}

			return new ResponseEntity<>(returnMap, HttpStatus.OK);

		}

		private Batchmaster getActivBatch(int nbatchmastercode, UserInfo userInfo) throws Exception {
			final String strQuery = " select * from batchmaster bm " + " where bm.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and bm.nsitecode = "
					+ userInfo.getNtranssitecode() + "" + " and bm.nbatchmastercode =" + nbatchmastercode + "";
			return (Batchmaster) jdbcUtilityFunction.queryForObject(strQuery, Batchmaster.class,jdbcTemplate); 
		}

		@SuppressWarnings("unchecked")
		@Override
		public ResponseEntity<Object> deleteBatchcreation(Map<String, Object> inputMap, UserInfo userInfo)
				throws Exception {
			// TODO Auto-generated method stub
			ObjectMapper objmap = new ObjectMapper();
			Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
			JSONObject actionType = new JSONObject();
			JSONObject jsonAuditObject = new JSONObject();
			int nbatchmastercode = (int) inputMap.get("nbatchmastercode");
			int ndesigntemplatemappingcode = (int) inputMap.get("ndesigntemplatemappingcode");
			//int ndesigntemplatemappingcode = (int) inputMap.get("ndesigntemplatemappingcode");
			Map<String, Object> returnMap = new HashMap<>();
			Batchmaster objbatchmaster = objmap.convertValue(inputMap.get("selectedBatch"),
					new TypeReference<Batchmaster>() {
					});
			final Batchmaster batchmaster = (Batchmaster) getActivBatch(nbatchmastercode, userInfo);
			if (batchmaster == null) {
				// status code:417
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {

				final BatchHistory batchhist = (BatchHistory) getCheckValidation(nbatchmastercode, userInfo);

				if (batchhist.getNtransactionstatus() == Enumeration.TransactionStatus.INITIATED.gettransactionstatus()
						|| batchhist.getNtransactionstatus() == Enumeration.TransactionStatus.COMPLETED
								.gettransactionstatus()) {

					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.SELECTDRAFTVERSION.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);

				} else {

					String sUpdateBatch = "update batchmaster set nstatus="
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ""
							+ " where nbatchmastercode = " + nbatchmastercode + "";

					jdbcTemplate.execute(sUpdateBatch);

					final List<Batchsample> lstbatchsample = (List<Batchsample>) getActivSampleByIdToCheckData(
							nbatchmastercode, userInfo);
					final List<BatchsampleIqc> lstbatchsampleiqc = (List<BatchsampleIqc>) getActivBatchIQCSampleById(
							nbatchmastercode, userInfo);
					if (lstbatchsample.size() > 0) {
						final String updateQueryString = "update batchsample set nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ""
								+ " where nbatchmastercode=" + nbatchmastercode + "";
						jdbcTemplate.execute(updateQueryString);

					}
					if (lstbatchsampleiqc.size() > 0) {
						final String updateQueryStringIQC = "update batchsampleiqc set nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ""
								+ " where nbatchmastercode=" + nbatchmastercode + "";
						jdbcTemplate.execute(updateQueryStringIQC);

						final String smaterialinventtranscode = lstbatchsampleiqc.stream()
								.map(objMatInvCode -> String.valueOf(objMatInvCode.getNmaterialinventtranscode()))
								.collect(Collectors.joining(","));

						final String updateMatQueryString = "update materialinventorytransaction set nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ""
								+ " where nmaterialinventtranscode=" + smaterialinventtranscode + "";

						jdbcTemplate.execute(updateMatQueryString);

					}

				}
				returnMap.putAll((Map<String, Object>) getSampleTabDetails(nbatchmastercode,
						userInfo, ndesigntemplatemappingcode).getBody());
				// returnMap.putAll((Map<String,Object>) getSampleTabDetails(nbatchmastercode,
				// userInfo,ndesigntemplatemappingcode).getBody());
				// returnMap.put("samplesAudit",returnMap.get("Samples"));
				List<Batchmaster> lstbatchmaster = new ArrayList<>();
				lstbatchmaster.add(objbatchmaster);
				auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
				auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
				auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
				actionType.put("batchmaster", "IDS_DELETEBATCHCREATION");
				actionType.put("batchsample", "IDS_DELETEBATCHSAMPLE");
				jsonAuditObject.put("batchmaster", lstbatchmaster);
				jsonAuditObject.put("batchsample", returnMap.get("selectedSamples"));
				auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);
				returnMap.putAll((Map<String, Object>) getBatchmaster(inputMap, userInfo).getBody());
				return new ResponseEntity<>(returnMap, HttpStatus.OK);
			}
		}

		private BatchHistory getCheckValidation(int nbatchmastercode, UserInfo userInfo) throws Exception {

			String sValidationQuery = "select bh.ntransactionstatus from batchmaster bm,batchhistory bh where bm.nbatchmastercode=bh.nbatchmastercode"
					+ " and bm.nbatchmastercode = " + nbatchmastercode + " " + " and bh.nsitecode="
					+ userInfo.getNtranssitecode() + "" + " and bm.nsitecode=" + userInfo.getNtranssitecode() + ""
					+ " and bm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and bh.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and bh.nbatchhistorycode =any(select max(nbatchhistorycode) from batchhistory"
					+ " where nbatchmastercode = " + nbatchmastercode + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

			return (BatchHistory) jdbcUtilityFunction.queryForObject(sValidationQuery, BatchHistory.class,jdbcTemplate); 
		}

		@Override
		public Map<String, Object> seqNoGetforComplete(int sampleCount, UserInfo userInfo, Map<String, Object> inputMap)
				throws Exception {

			ObjectMapper objmap = new ObjectMapper();
			final String sQuery = " lock table lockbatchcreation " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			Boolean nneedtestinitiate = (Boolean) inputMap.get("nneedtestinitiate");
			int nbatchmastercode = (int) inputMap.get("nbatchmastercode");
			Boolean nneedsubsample = (Boolean) inputMap.get("nneedsubsample");
			String stransactiontestcode = (String) inputMap.get("ntransactiontestcode");
//			int completeId = (int) inputMap.get("completeId");
//			int nregtypecode = (int) inputMap.get("nregtypecode");
//			int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
			final Map<String, Object> mapSeq = new HashMap<String, Object>();

			final Batchmaster batchmaster = (Batchmaster) getActivBatch(nbatchmastercode, userInfo);
			if (batchmaster == null) {
				mapSeq.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()));
			} else {
				final BatchHistory batchhist = (BatchHistory) getCheckValidation(nbatchmastercode, userInfo);

				if (nneedtestinitiate && batchhist.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT
						.gettransactionstatus()) {
					mapSeq.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), commonFunction
							.getMultilingualMessage("IDS_SELECTINITIATEDRECORD", userInfo.getSlanguagefilename()));
					// }
				} else if (batchhist.getNtransactionstatus() == Enumeration.TransactionStatus.COMPLETED
						.gettransactionstatus()) {
					mapSeq.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							commonFunction.getMultilingualMessage("ALREADYCOMPLETED", userInfo.getSlanguagefilename()));
				} else {
//				 	List<TransactionValidation> validationStatusLst = (List<TransactionValidation>) getTransactionValidation(completeId,nregtypecode,nregsubtypecode,userInfo);
//				 	final String stransactionStatus = validationStatusLst.stream().map(objpreregno -> String.valueOf(objpreregno.getNtransactionstatus()))
//			                .collect(Collectors.joining(","));
//				 	final String stransactiondisplayStatus = validationStatusLst.stream().map(objpreregno -> String.valueOf(objpreregno.getStransdisplaystatus()))
//	                .collect(Collectors.joining(","));

					// ALPD-2880
					final String stransactionStatus = getCompleteActionValidationStatus();

					final BatchHistory batchhistory = objmap.convertValue(inputMap.get("Batchhistory"),
							new TypeReference<BatchHistory>() {
							});

					//final SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					//final String stransactiondate = sdFormat.format(batchhistory.getDtransactiondate());

					final List<BatchHistory> checkDateList = (List<BatchHistory>) getCheckDateForCompleteAction(
							nbatchmastercode, userInfo);

					if (batchhistory.getDtransactiondate().after(checkDateList.get(0).getDtransactiondate())) {
						final Map<String, Object> scheckCompleteStatus = getCheckCompleteRecordStatus(userInfo,
								nneedsubsample, stransactionStatus, stransactiontestcode);

						if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(scheckCompleteStatus.get("rtn"))) {
							final String getSeqBatchNo = "select stablename,nsequenceno from seqnobatchcreation "
									+ " where stablename = 'batchhistory' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";

							final SeqNoBatchcreation objSeqNo = jdbcTemplate.queryForObject(getSeqBatchNo,
									new SeqNoBatchcreation());
							int startBatchSeqNo = objSeqNo.getNsequenceno() + 1;
							final String strUpdate = "update seqnobatchcreation set nsequenceno = " + startBatchSeqNo
									+ " where stablename='batchhistory'";
							jdbcTemplate.execute(strUpdate);

							final String getSeqNo = "select stablename,nsequenceno from seqnoregistration where stablename = 'registrationtesthistory'";
							final SeqNoRegistration objTestHisSeqNo = jdbcTemplate.queryForObject(getSeqNo,
									new SeqNoRegistration());
							final int testHistSeq = objTestHisSeqNo.getNsequenceno() + 1;

							final int endseqno = testHistSeq + sampleCount;

							mapSeq.put("nbatchhistorycode", startBatchSeqNo);
							mapSeq.put("ntesthistorycode", testHistSeq);
							mapSeq.put("nendTesthistorycode", endseqno);
							mapSeq.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
									Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
						} else {
							// String sAlert =
							// commonFunction.getMultilingualMessage("IDS_TESTNOT",userInfo.getSlanguagefilename())+"
							// "+stransactiondisplayStatus;
							mapSeq.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
									scheckCompleteStatus.get("sAlert"));
						}
					} else {
						String sDateAlert = commonFunction.getMultilingualMessage("IDS_COMPLETEDDATESHOULDBEAFTERINITATE",
								userInfo.getSlanguagefilename());
						mapSeq.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), sDateAlert);
					}
				}
			}
			return mapSeq;
		}

		// ALPD-2880
		private String getCompleteActionValidationStatus() throws Exception {

			final String statusQry = "select ntranscode, stransstatus, jsondata from transactionstatus "
					+ " where ntranscode between " + Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()
					+ " and " + Enumeration.TransactionStatus.RELEASED.gettransactionstatus() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final List<TransactionStatus> validationStatusLst = (List<TransactionStatus>) jdbcTemplate.query(statusQry,
					new TransactionStatus());

			final String stransactionStatus = validationStatusLst.stream()
					.map(status -> String.valueOf(status.getNtranscode())).collect(Collectors.joining(","));
			return stransactionStatus;
		}

		@SuppressWarnings({ "unchecked" })
		@Override
		public ResponseEntity<Object> completeBatchcreation(Map<String, Object> seqNoList,
				List<Batchsample> batchSampleList, Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
			String sQuery = " lock table lockbatchcreation " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);
			// TODO Auto-generated method stub
			int nbatchmastercode = (int) inputMap.get("nbatchmastercode");
			//int nsampletypecode = (int) inputMap.get("nsampletypecode");
			int startBatchSeqNo = (int) seqNoList.get("nbatchhistorycode");
//			int ntesthistorycode = (int) seqNoList.get("ntesthistorycode");
//			int nendTesthistorycode = (int) seqNoList.get("nendTesthistorycode");
			String stransactiontestcode = (String) inputMap.get("ntransactiontestcode");
			String sbatcharno = (String) inputMap.get("sbatcharno");
			Boolean nneedsubsample = (Boolean) inputMap.get("nneedsubsample");
			//int completeId = (int) inputMap.get("completeId");
			//int nregtypecode = (int) inputMap.get("nregtypecode");
			//int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
			Boolean nneedtestinitiate = (Boolean) inputMap.get("nneedtestinitiate");
			//List<Batchsample> lst = batchSampleList;
			ObjectMapper objmap = new ObjectMapper();
			Map<String, Object> returnMap = new HashMap<>();
			Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
			JSONObject actionType = new JSONObject();
			JSONObject jsonAuditObject = new JSONObject();

			final Batchmaster batchmaster = (Batchmaster) getActivBatch(nbatchmastercode, userInfo);
			if (batchmaster == null) {
				// status code:417
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				final BatchHistory batchhist = (BatchHistory) getCheckValidation(nbatchmastercode, userInfo);
				if (nneedtestinitiate && batchhist.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT
						.gettransactionstatus()) {
					// if(batchhist.getNtransactionstatus() ==
					// Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {

					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTINITIATEDRECORD",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);

					// }
				} else if (batchhist.getNtransactionstatus() == Enumeration.TransactionStatus.COMPLETED
						.gettransactionstatus()) {

					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYCOMPLETED.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);

				} else {

//			    List<TransactionValidation> validationStatusLst = (List<TransactionValidation>) getTransactionValidation(completeId,nregtypecode,nregsubtypecode,userInfo);
//				final String stransactionStatus = validationStatusLst.stream().map(objpreregno -> String.valueOf(objpreregno.getNtransactionstatus()))
//				                .collect(Collectors.joining(","));
//				final String stransactiondisplayStatus = validationStatusLst.stream().map(objpreregno -> String.valueOf(objpreregno.getStransdisplaystatus()))
//		                .collect(Collectors.joining(","));

					//
					final String stransactionStatus = getCompleteActionValidationStatus();

					final BatchHistory batchhistory = objmap.convertValue(inputMap.get("Batchhistory"),
							new TypeReference<BatchHistory>() {
							});

					final SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					sdFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
					final String stransactiondate = sdFormat.format(batchhistory.getDtransactiondate());

					final List<BatchHistory> checkDateList = (List<BatchHistory>) getCheckDateForCompleteAction(
							nbatchmastercode, userInfo);

					if (batchhistory.getDtransactiondate().after(checkDateList.get(0).getDtransactiondate())) {
						final Map<String, Object> scheckCompleteStatus = getCheckCompleteRecordStatus(userInfo,
								nneedsubsample, stransactionStatus, stransactiontestcode);
						if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(scheckCompleteStatus.get("rtn"))) {

							final String comments = batchhistory.getScomments() != null
									? " N'" + stringUtilityFunction.replaceQuote(batchhistory.getScomments()) + "'"
									: null;

							String sInsertBatchHist = "insert into batchhistory(nbatchhistorycode,nbatchmastercode,sbatcharno,"
									+ " ndeputyusercode,ndeputyuserrolecode,ntransactionstatus,nusercode,nuserrolecode,dtransactiondate,"
									+ " noffsetdtransactiondate,ntransdatetimezonecode,"
									+ " scomments,nsitecode,nstatus) values(" + startBatchSeqNo + "," + nbatchmastercode
									+ ",'" + sbatcharno + "'," + " " + userInfo.getNdeputyusercode() + ","
									+ userInfo.getNdeputyuserrole() + ","
									+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() + "," + " "
									+ userInfo.getNusercode() + "," + userInfo.getNuserrole() + ",'" + stransactiondate
									+ "',"
									// + " '"+getCurrentDateTime(userInfo)+"',"
									+ " " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
									+ userInfo.getNtimezonecode() + "," + comments + "," + userInfo.getNtranssitecode()
									+ "," + " " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

							jdbcTemplate.execute(sInsertBatchHist);

//							 final String strUpdate = "update seqnobatchcreation set nsequenceno = "+startBatchSeqNo+" where stablename='batchhistory'";
//								getJdbcTemplate().execute(strUpdate);
						} else {

							// mapSeq.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),scheckCompleteStatus.get("sAlert"));
							// String sAlert =
							// commonFunction.getMultilingualMessage("IDS_TESTNOT",userInfo.getSlanguagefilename())+"
							// "+stransactiondisplayStatus;
							return new ResponseEntity<>(scheckCompleteStatus.get("sAlert"), HttpStatus.EXPECTATION_FAILED);

						}

						final String ui_ToDate = (String) inputMap.get("toDate");
						final String toDate = dateUtilityFunction.getCurrentDateTime(userInfo).toString();
						inputMap.put("toDate",toDate.substring(0,toDate.length()-1));
						returnMap.putAll((Map<String, Object>) getBatchmaster(inputMap, userInfo).getBody());
						returnMap.putAll((Map<String, Object>) changeTimeFormatForUi(ui_ToDate, userInfo));
						List<Batchmaster> lstbatchmaster = new ArrayList<>();
						lstbatchmaster.add((Batchmaster) returnMap.get("SelectedBatchmaster"));
						auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
						auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
						auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
						actionType.put("batchmaster", "IDS_BATCHCOMPLETE");
						jsonAuditObject.put("batchmaster", lstbatchmaster);
						auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);
						return new ResponseEntity<>(returnMap, HttpStatus.OK);
					} else {
						String sDateAlert = commonFunction.getMultilingualMessage("IDS_COMPLETEDDATESHOULDBEAFTERINITATE",
								userInfo.getSlanguagefilename());
						// returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),sDateAlert);
						return new ResponseEntity<>(sDateAlert, HttpStatus.EXPECTATION_FAILED);

					}
				}
			}
		}

		@Override
		public ResponseEntity<Object> getBatchhistory(int nbatchmastercode, UserInfo userInfo) throws Exception {

			Map<String, Object> returnMap = new HashMap<>();
			String sbatchquery = " select bh.sbatcharno,bh.nbatchhistorycode,tm.stestname,CONCAT(u.sfirstname,' ',u.slastname) as username,"
					+ " ur.suserrolename, " + " case when bh.scomments = 'null' then '-' else bh.scomments end,"
					+ " to_char(bh.dtransactiondate,'" + userInfo.getSsitedatetime() + "') as stransactiondate,"
					+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
					+ "',ts.jsondata->'stransdisplaystatus'->>'en-US') stransdisplaystatus," + " bm.nbatchmastercode "
					+ " from batchhistory bh,batchmaster bm,testmaster tm,transactionstatus ts,users u,userrole ur  "
					+ " where bm.nbatchmastercode = " + nbatchmastercode + " and tm.ntestcode = bm.ntestcode "
					+ " and bm.nbatchmastercode = bh.nbatchmastercode and u.nusercode=bh.nusercode "
					+ " and ur.nuserrolecode=bh.nuserrolecode" + " and ts.ntranscode = bh.ntransactionstatus "
					+ " and bh.nsitecode=" + userInfo.getNtranssitecode() + "" + " and bm.nsitecode="
					+ userInfo.getNtranssitecode() + "" + " and tm.nsitecode=" + userInfo.getNmastersitecode() + ""
					+ " and u.nsitecode=" + userInfo.getNmastersitecode() + "" + " and ur.nsitecode="
					+ userInfo.getNmastersitecode() + "" + " and bh.nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and bm.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and tm.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and ts.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and u.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and ur.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " order by nbatchhistorycode desc ";

			List<Batchmaster> lstBatchmaster = (List<Batchmaster>) jdbcTemplate.query(sbatchquery, new Batchmaster());
			returnMap.put("Batchhistory", lstBatchmaster);
			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		}

		@Override
		public ResponseEntity<Object> getSection(final Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

			Map<String, Object> returnMap = new HashMap<>();

			String sSectionQuery = "select rt.nsectioncode,s.ssectionname from registrationtest rt,registration r,"
					+ " section s  where  s.nsectioncode=rt.nsectioncode "
					+ " and rt.nsectioncode = any (select ls.nsectioncode from sectionusers su,labsection ls where "
					+ " su.nusercode = " + userInfo.getNusercode() + " and ls.nlabsectioncode=su.nlabsectioncode "
					+ " and su.nsitecode= " + userInfo.getNtranssitecode() + "" + " and ls.nsitecode= "
					+ userInfo.getNtranssitecode() + "" + " and ls.nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and su.nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " group by ls.nsectioncode)"
					+ " and r.npreregno=rt.npreregno" + " and r.nregtypecode=" + inputMap.get("nregtypecode") + ""
					+ " and r.nregsubtypecode=" + inputMap.get("nregsubtypecode") + "" + " and r.nsitecode="
					+ userInfo.getNtranssitecode() + "" + " and rt.nsitecode=" + userInfo.getNtranssitecode() + ""
					+ " and s.nsitecode=" + userInfo.getNmastersitecode() + "" + " and r.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and s.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " group by rt.nsectioncode,s.ssectionname";

//			String sSectionQuery = "select rt.nsectioncode,max(s.ssectionname)ssectionname from registrationtest rt,"
//					+ " registrationtesthistory rth,section s "
//					+ " where rt.ntransactiontestcode = rth.ntransactiontestcode and s.nsectioncode=rt.nsectioncode"
//			        + " and rth.ntesthistorycode = any (select max(rth.ntesthistorycode)from registrationtesthistory rth, "
//			        + " registrationtest rt where rt.ntransactiontestcode=rth.ntransactiontestcode and rt.ntestcode=1"
//			        + " and rt.ninstrumentcatcode > 0 and rth.ntransactionstatus between "+Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()+" and "
//			        + " "+Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()+""
//			        + " and rth.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
//			        + " and rt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" group by rth.ntransactiontestcode) "
//			        + " group by rt.nsectioncode";

			List<RegistrationTest> lstSection = jdbcTemplate.query(sSectionQuery, new RegistrationTest());

			if (!lstSection.isEmpty()) {
				returnMap.put("Section", lstSection);
				returnMap.put("selectedSection", lstSection.get(0));
				// returnMap.put("selectedTestSynonym", inputMap.get("test"));
			} else {
				returnMap.put("Section", null);
			}
			return new ResponseEntity<>(returnMap, HttpStatus.OK);

		}

		private List<TransactionValidation> getTransactionValidation(int controlCode, int nregtypecode, int nregsubtypecode,
				UserInfo userInfo) throws Exception {

			final String getValidationStatus = "select coalesce (ts.jsondata->'stransdisplaystatus'->>'"
					+ userInfo.getSlanguagetypecode()
					+ "',ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,tv.ntransactionstatus "
					+ " from transactionvalidation tv ,transactionstatus ts where " + " tv.nregtypecode=" + nregtypecode
					+ "" + " and tv.nregsubtypecode=" + nregsubtypecode + " and ts.ntranscode = tv.ntransactionstatus"
					+ " and tv.ncontrolcode=" + controlCode + " and tv.nformcode =" + userInfo.getNformcode() + ""
					+ " and tv.nsitecode = " + userInfo.getNmastersitecode() + "" + " and ts.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and tv.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			return (List<TransactionValidation>) jdbcTemplate.query(getValidationStatus, new TransactionValidation());
			// return (TransactionValidation) jdbcQueryForObject(strQuery,
			// TransactionValidation.class);
		}

		@Override
		@SuppressWarnings("unchecked")
		public ResponseEntity<Object> getBatchIQC(final Map<String, Object> inputMap, final UserInfo userInfo)
				throws Exception {

			Map<String, Object> returnMap = new HashMap<>();
			Map<String, Object> specInputMap = new HashMap<>();
			int nsectioncode = (int) inputMap.get("nsectioncode");
			int nbatchmastercode = (int) inputMap.get("nbatchmastercode");

			String sMatTypeQuery = " select jsondata->'smaterialtypename'->>'en-US' smaterialtypename,nmaterialtypecode "
					+ " from materialtype where nmaterialtypecode="
					+ Enumeration.TransactionStatus.IQCSTANDARDMATERIALTYPE.gettransactionstatus() + ""
					+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";

			MaterialCategory objMatType = jdbcTemplate.queryForObject(sMatTypeQuery, new MaterialCategory());
			returnMap.put("selectedMaterialType", objMatType);

			final Batchmaster batchmaster = (Batchmaster) getActivBatch(nbatchmastercode, userInfo);
			if (batchmaster == null) {
				// status code:417
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {

				if (objMatType != null) {

					String sQuery = " select mi.jsondata->>'Inventory ID' sinventoryid,"
//						+ " mi.jsondata->'Unit'->>'label' as sunitname,"
//						+ " mi.jsondata->'Unit'->>'value' as nunitcode,"
							+ " mc.smaterialcatname,m.nmaterialcode,m.jsondata->>'Material Name' smaterialname,m.nmaterialcatcode,"
							+ " mt.nmaterialtypecode,mi.nmaterialinventorycode "
							+ " from material m,materialtype mt,materialcategory mc,materialinventory mi " + " where "
							+ " mc.nmaterialtypecode=mt.nmaterialtypecode " + " and mc.nmaterialcatcode=m.nmaterialcatcode "
							+ " and m.nmaterialcode=mi.nmaterialcode " + " and mi.ntransactionstatus="
							+ Enumeration.TransactionStatus.RELEASED.gettransactionstatus() + ""
							+ " and mt.nmaterialtypecode="
							+ Enumeration.TransactionStatus.IQCSTANDARDMATERIALTYPE.gettransactionstatus() + ""
							+ " and m.nsitecode=" + userInfo.getNmastersitecode() + "" + " and mt.nsitecode="
							+ userInfo.getNmastersitecode() + "" + " and mc.nsitecode=" + userInfo.getNmastersitecode() + ""
							+ " and mi.nsitecode=" + userInfo.getNtranssitecode() + "" + " and m.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and mt.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and mc.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and mi.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

					List<Material> lstMatType = (List<Material>) jdbcTemplate.query(sQuery, new Material());
					if (!lstMatType.isEmpty()) {

//				    returnMap.put("MaterialInventory", lstMatType);

//						final String materialInvcode = lstMatType.stream()
//								.map(objMaterialInv -> String.valueOf(objMaterialInv.getNmaterialinventorycode()))
//								.collect(Collectors.joining(","));

						final String materialcode = lstMatType.stream()
								.map(objMaterial -> String.valueOf(objMaterial.getNmaterialcode()))
								.collect(Collectors.joining(","));

						final String materialcatcode = lstMatType.stream()
								.map(objMaterialCategory -> String.valueOf(objMaterialCategory.getNmaterialcatcode()))
								.collect(Collectors.joining(","));

						String sMatCatQuery = "select * from materialcategory " + " where nmaterialcatcode in("
								+ materialcatcode + ")" + " and nsitecode=" + userInfo.getNmastersitecode() + ""
								+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

						final List<MaterialCategory> lstMaterialCategory = (List<MaterialCategory>) jdbcTemplate
								.query(sMatCatQuery, new MaterialCategory());

						returnMap.put("MaterialCategory", lstMaterialCategory);
						returnMap.put("selectedMaterialCategory", lstMaterialCategory.get(0));
						inputMap.put("needsection", lstMaterialCategory.get(0).getNeedSectionwise());
						int inputSectioncode = inputMap.get("needsection").getClass().getName() == "java.lang.Integer"
								? (int) inputMap.get("needsection")
								: ((Short) inputMap.get("needsection")).intValue();
						nsectioncode = inputSectioncode == Enumeration.TransactionStatus.NO.gettransactionstatus()
								? Enumeration.TransactionStatus.NA.gettransactionstatus()
								: (int) inputMap.get("nsectioncode");

						if (lstMaterialCategory.size() > 0) {

							String sMatQuery = "select nmaterialcode,jsondata->>'Material Name' smaterialname,"
									+ " nmaterialcatcode,jsondata->'Material Category'-> 'item'->'jsondata'->>'needsectionwise' needsection "
									+ " from material " + " where " + " nmaterialcatcode in ("
									+ lstMaterialCategory.get(0).getNmaterialcatcode() + ")" + " and nmaterialcode in("
									+ materialcode + ")" + " and nsitecode=" + userInfo.getNmastersitecode() + ""
									+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

							List<Material> lstMaterial = (List<Material>) jdbcTemplate.query(sMatQuery,
									new Material());

							if (!lstMaterial.isEmpty()) {

								returnMap.put("Material", lstMaterial);
								returnMap.put("selectedMaterial", lstMaterial.get(0));
								/*
								 * String sMatInvQuery =
								 * " select mi.jsondata->>'Inventory ID' sinventoryid,nmaterialinventorycode" +
								 * " from materialinventory mi " +
								 * " where mi.nmaterialcode ="+lstMaterial.get(0).getNmaterialcode()+" +
								 * " and mit.nsectioncode = "+nsectioncode+"" +
								 * "	and mit.nmaterialinventorycode=mi.nmaterialinventorycode" +
								 * " and mi.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(
								 * )+"" +
								 * " and mi.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(
								 * )+""; + " and mi.nsectioncode = "+nsectioncode+"" +
								 * " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+
								 * ""; + " and mi.nsectioncode = "+nsectioncode+"" +
								 * " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+
								 * "";
								 */

								String sMatInvQuery = "select max(mi.nmaterialinventorycode) as nmaterialinventorycode,"
										+ " mi.jsondata->>'Inventory ID' sinventoryid,max(mit.nmaterialinventtranscode) as nmaterialinventtranscode"
										+ " from materialinventory mi , materialinventorytransaction mit "
										+ " where mi.nmaterialcode =" + lstMaterial.get(0).getNmaterialcode() + ""
										+ " and mit.nsectioncode = " + nsectioncode + ""
										+ " and mit.nmaterialinventorycode=mi.nmaterialinventorycode"
										+ " and mit.nsitecode=" + userInfo.getNtranssitecode() + "" + " and mi.nsitecode="
										+ userInfo.getNtranssitecode() + "" + " and mit.nstatus="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
										+ " and mi.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " " + " group by  mi.nmaterialinventorycode";

								List<Material> lstMatInv = (List<Material>) jdbcTemplate.query(sMatInvQuery,
										new Material());
								specInputMap.put("nmaterialcode", lstMaterial.get(0).getNmaterialcode());
								specInputMap.put("nmaterialcatcode", lstMaterialCategory.get(0).getNmaterialcatcode());
								specInputMap.put("ncategorybasedflow",
										(int) lstMaterialCategory.get(0).getNcategorybasedflow());
								specInputMap.put("ntestcode", inputMap.get("ntestcode"));
								specInputMap.put("ntestgroupspecrequired", inputMap.get("ntestgroupspecrequired"));
								if (lstMatInv.size() > 0) {
									returnMap.put("selectedMaterialInventory", lstMatInv.get(0));
									returnMap.put("MaterialInventory", lstMatInv);
									returnMap.putAll((Map<String, Object>) getAvailableMaterialQuantity(
											lstMatInv.get(0).getNmaterialinventorycode(), nsectioncode, userInfo)
											.getBody());
									returnMap.put("selectedInventoryUnit", returnMap.get("inventoryTransaction"));
									returnMap.putAll((Map<String, Object>) getSpecificationDetails(specInputMap, userInfo)
											.getBody());
								} else {
									returnMap.put("selectedMaterialInventory", null);
									returnMap.put("MaterialInventory", lstMatInv);
									returnMap.put("selectedInventoryUnit", null);
								}

							}

						}
					

					} else {
						returnMap.put("MaterialCategory", null);
					}
				}

				return new ResponseEntity<>(returnMap, HttpStatus.OK);

			}

		}

		@SuppressWarnings("unchecked")
		@Override
		public ResponseEntity<Object> getBatchMaterial(final Map<String, Object> inputMap, final UserInfo userInfo)
				throws Exception {
			Map<String, Object> returnMap = new HashMap<>();
			int nmaterialcatcode = (int) inputMap.get("nmaterialcatcode");
			String sQuery = "select m.nmaterialcode,m.jsondata->>'Material Name' smaterialname,m.nmaterialcatcode, "
					+ " m.jsondata->'Material Category'-> 'item'->'jsondata'->>'needsectionwise' needsection "
					+ " from material m,materialinventory mi" + " where nmaterialcatcode=" + nmaterialcatcode + " "
					+ " and mi.nmaterialcode=m.nmaterialcode " + " and mi.ntransactionstatus= "
					+ Enumeration.TransactionStatus.RELEASED.gettransactionstatus() + "" + " and m.nsitecode="
					+ userInfo.getNmastersitecode() + "" + " and mi.nsitecode=" + userInfo.getNtranssitecode() + ""
					+ " and m.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and mi.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " group by m.nmaterialcode";

			List<Material> lstMat = (List<Material>) jdbcTemplate.query(sQuery, new Material());
			if (!lstMat.isEmpty()) {
				returnMap.put("Material", lstMat);
				returnMap.put("selectedMaterial", lstMat.get(0));
				inputMap.put("nmaterialcode", lstMat.get(0).getNmaterialcode());
				inputMap.put("needsection", lstMat.get(0).getNeedsection());
				returnMap.putAll((Map<String, Object>) getBatchMaterialInventory(inputMap, userInfo).getBody());
				
				
			} else {
				returnMap.put("Material", null);
			}
			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		}

		@SuppressWarnings("unchecked")
		@Override
		public ResponseEntity<Object> getBatchMaterialInventory(final Map<String, Object> inputMap, UserInfo userInfo)
				throws Exception {
			Map<String, Object> returnMap = new HashMap<>();

			int nmaterialcode = (int) inputMap.get("nmaterialcode");
			int inputSectioncode = inputMap.get("needsection").getClass().getName() == "java.lang.Integer"
					? (int) inputMap.get("needsection")
					: ((Short) inputMap.get("needsection")).intValue();
			int nsectioncode = inputSectioncode == Enumeration.TransactionStatus.NO.gettransactionstatus()
					? Enumeration.TransactionStatus.NA.gettransactionstatus()
					: (int) inputMap.get("nsectioncode");
			String sQuery = "select max(mi.nmaterialinventorycode) as nmaterialinventorycode,"
					+ " max(mit.nmaterialinventtranscode) as nmaterialinventtranscode,"
					+ " mi.jsondata->>'Inventory ID' sinventoryid," + " mi.nmaterialcode "
					+ " from materialinventory mi,materialinventorytransaction mit" + " where mi.nmaterialcode="
					+ nmaterialcode + "  " + " and mit.nsectioncode = " + nsectioncode + ""
					+ " and mit.nmaterialinventorycode=mi.nmaterialinventorycode" + " and mit.nsitecode="
					+ userInfo.getNtranssitecode() + "" + " and mi.nsitecode=" + userInfo.getNtranssitecode() + ""
					+ " and mit.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and mi.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " group by  mi.nmaterialinventorycode";

			List<MaterialInventory> lstMatInventory = (List<MaterialInventory>) jdbcTemplate.query(sQuery,
					new MaterialInventory());
			//ALPD-5873  for Default Spec Work. Added by Abdul on 04-06-2025
			final String matCatQuery = "select mc.nmaterialcatcode, mc.ncategorybasedflow from material m , materialcategory mc"
					                + " where mc.nmaterialcatcode = m.nmaterialcatcode and m.nmaterialcode = "+ nmaterialcode
					                + " and m.nsitecode = "+userInfo.getNmastersitecode()
					                + " and mc.nsitecode = "+userInfo.getNmastersitecode()
					                + " and m.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					                + " and mc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
			MaterialCategory objMatCat = (MaterialCategory) jdbcUtilityFunction.queryForObject(matCatQuery, MaterialCategory.class,
					jdbcTemplate);

			if (!lstMatInventory.isEmpty()) {
				returnMap.put("selectedMaterialInventory", lstMatInventory.get(0));
				returnMap.put("MaterialInventory", lstMatInventory);
				returnMap.putAll((Map<String, Object>) getAvailableMaterialQuantity(
						lstMatInventory.get(0).getNmaterialinventorycode(), nsectioncode, userInfo).getBody());
				
				//ALPD-5778--Added by Vignesh R(06-05-2025)-->Spec loaded incorrectly with IQC sample
				//start--ALPD-5778
				Map<String, Object> specInputMap = new HashMap<>();
			
				specInputMap.put("nmaterialcode", nmaterialcode);
				//ALPD-5873  for Default Spec Work. Added by Abdul on 04-06-2025
                specInputMap.put("nmaterialcatcode", objMatCat.getNmaterialcatcode());
				specInputMap.put("ncategorybasedflow", (int)objMatCat.getNcategorybasedflow());
				specInputMap.put("ntestcode", inputMap.get("ntestcode"));
				final int ntestgroupspecrequired = inputMap.containsKey("ntestgroupspecrequired") ? (int) inputMap.get("ntestgroupspecrequired") : Enumeration.TransactionStatus.YES.gettransactionstatus();
				specInputMap.put("ntestgroupspecrequired", ntestgroupspecrequired);
				returnMap.putAll((Map<String, Object>) getSpecificationDetails(specInputMap, userInfo)
						.getBody());
				//end--ALPD-5778
				returnMap.put("selectedInventoryUnit", returnMap.get("inventoryTransaction"));
			} else {
				returnMap.put("selectedMaterialInventory", null);
				returnMap.put("MaterialInventory", lstMatInventory);
			}

			return new ResponseEntity<>(returnMap, HttpStatus.OK);

		}

		private ResponseEntity<Object> getAvailableMaterialQuantity(int materialInvCode, int sectionCode, UserInfo userInfo)
				throws Exception {
			Map<String, Object> returnMap = new HashMap<>();
			final String getString = "select mi.nmaterialinventtranscode,mi.nmaterialinventorycode,mi.nsectioncode,mi.jsondata,mi.jsonuidata,a.savailablequatity,a.sunitname,mi.nmaterialinventtranscode"
					// + " a.*"
					+ " from materialinventorytransaction mi,(" + " select case when "
					+ " SIGN(coalesce(sum(nqtyreceived)-sum(nqtyissued),0))=-1 then 0.00 else"
					+ " coalesce(sum(nqtyreceived)-sum(nqtyissued),0) end " + " savailablequatity, "
					// + " coalesce(sum(nqtyreceived)-sum(nqtyissued),0) as savailablequatity,"
					+ " max(jsonuidata->>'Unit') as sunitname" + " from materialinventorytransaction "
					+ " where nmaterialinventorycode = " + materialInvCode + " and nsectioncode = " + sectionCode
					+ " and nsitecode = " + userInfo.getNtranssitecode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by nmaterialinventorycode"
					+ " ) a" + " where nmaterialinventorycode = " + materialInvCode + " and nsectioncode = " + sectionCode
					+ " and nsitecode = " + userInfo.getNtranssitecode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " limit 1";
			MaterialInventory inventory = (MaterialInventory) jdbcUtilityFunction.queryForObject(getString, MaterialInventory.class,jdbcTemplate); 
			if (inventory != null) {
				returnMap.put("inventoryTransaction", inventory);
//			returnMap.put("nsectioncode", inventory.getNsectioncode());
			} else {
				returnMap.put("inventoryTransaction", null);
			}

			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		}

		@Override
		@SuppressWarnings("unchecked")
		public Map<String, Object> validateAndInsertSeqNoIQCSampleData(Map<String, Object> inputMap) throws Exception {

			String sQuery = " lock  table lockpreregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			sQuery = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			sQuery = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			sQuery = " lock  table lockcancelreject " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			sQuery = " lock  table lockmaterialinventory " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			Map<String, Object> returnMap = new HashMap<String, Object>();
			final ObjectMapper objectMapper = new ObjectMapper();
			final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userInfo"), new TypeReference<UserInfo>() {
			});
			List<RegistrationSample> subSampleInputList = objectMapper.convertValue(inputMap.get("RegistrationSample"),
					new TypeReference<List<RegistrationSample>>() {
					});
			final Registration registration = objectMapper.convertValue(inputMap.get("Registration"),
					new TypeReference<Registration>() {
					});
			List<TestGroupTest> testGroupTestInputList = objectMapper.convertValue(inputMap.get("testgrouptest"),
					new TypeReference<List<TestGroupTest>>() {
					});

//			List<TestGroupTest> lsttest1 = testGroupTestInputList.stream().filter(x -> x.getSlno() == comp.get(0).getSlno())
//					.collect(Collectors.toList());
	//
			final String stestcode = testGroupTestInputList.stream().map(x -> String.valueOf(x.getNtestcode())).distinct()
					.collect(Collectors.joining(","));

			MaterialInventoryTrans objMaterialInventoryTrans = objectMapper
					.convertValue(inputMap.get("inputMaterialInventoryArrData"), MaterialInventoryTrans.class);

			int testCount = testGroupTestInputList.stream().mapToInt(testgrouptest -> testgrouptest.getNrepeatcountno())
					.sum();
			int nallottedspeccode = registration.getNallottedspeccode();

			final String testGroupQuery = "select tgt.*, s.ssectionname, m.smethodname from testgrouptest tgt,section s,method m,"
					+ " testgroupspecsampletype tgts where tgt.nsectioncode=s.nsectioncode "
					+ " and tgt.nmethodcode=m.nmethodcode and tgt.nspecsampletypecode=tgts.nspecsampletypecode "
					// + " and tgt.ntestgrouptestcode in (" + stestcode + ") "
					+ " and tgt.ntestcode in (" + stestcode + ") " + " and tgts.nallottedspeccode=" + nallottedspeccode + ""
					+ " and tgt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and s.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and m.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
					+ " and s.nsitecode="+userInfo.getNmastersitecode()+" and m.nsitecode=" + userInfo.getNmastersitecode();
			final List<TestGroupTest> testGroupTestList = jdbcTemplate.query(testGroupQuery, new TestGroupTest());

			final String strcount = "select count(0) from testgrouptestparameter where ntestgrouptestcode="
					+ testGroupTestList.get(0).getNtestgrouptestcode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="+userInfo.getNmastersitecode()+"";

			int parameterCount = jdbcTemplate.queryForObject(strcount, Integer.class);

			/*
			 * int parameterCount = testGroupTestInputList.stream().mapToInt(testgrouptest
			 * -> (testgrouptest.getNrepeatcountno() *
			 * testgrouptest.getNparametercount())).sum();
			 */

			// final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userInfo"),
			// new TypeReference<UserInfo>() {});

			float nqtyused = Float.parseFloat((String) objMaterialInventoryTrans.getJsondata().get("nqtyused"));
			float namountleft = Float.parseFloat((String) objMaterialInventoryTrans.getJsondata().get("savailablequatity"));

			if (namountleft != 0.0) {
				if (nqtyused <= namountleft) {

					final String strSelectSeqno = "select stablename,nsequenceno from seqnoregistration  where stablename "
							+ "in ('registration','registrationhistory','registrationparameter','registrationsample',"
							+ "'registrationsamplehistory','registrationtest','registrationtesthistory','registrationdecisionhistory')  "
							+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";

					final List<?> lstMultiSeqNo = projectDAOSupport.getMultipleEntitiesResultSetInList(strSelectSeqno,jdbcTemplate,SeqNoRegistration.class); 
					

					final List<SeqNoRegistration> lstSeqNoReg = (List<SeqNoRegistration>) lstMultiSeqNo.get(0);

					int nSeqNo = jdbcTemplate.queryForObject(
							" select nsequenceno from seqnobatchcreation where stablename='batchsampleiqc' "
							+ " and nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"", Integer.class);

					returnMap = lstSeqNoReg.stream().collect(Collectors.toMap(SeqNoRegistration::getStablename,
							SeqNoRegistration -> SeqNoRegistration.getNsequenceno()));

					returnMap.put("batchsampleiqc", nSeqNo);

					int matSeqno = jdbcTemplate.queryForObject(
							"select nsequenceno from seqnomaterialmanagement where stablename ='materialinventorytransaction'"
							+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"",
							Integer.class);
					returnMap.put("batchMatSeqn", matSeqno);

					final String strSeqnoUpdate = "Update seqnoregistration set nsequenceno = "
							+ ((int) returnMap.get("registration") + 1) + " where stablename = 'registration'"
							+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"

							+ "Update seqnoregistration set nsequenceno = "
							+ ((int) returnMap.get("registrationdecisionhistory") + 1)
							+ " where stablename = 'registrationdecisionhistory'"
							+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"

							+ "Update seqnoregistration set nsequenceno = "
							+ ((int) returnMap.get("registrationhistory") + 1)
							+ " where stablename = 'registrationhistory' "
							+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"

							+ "Update seqnoregistration set nsequenceno = "
							+ ((int) returnMap.get("registrationparameter") + parameterCount)
							+ " where stablename = 'registrationparameter' "
							+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"

							+ "Update seqnoregistration set nsequenceno = "
							+ ((int) returnMap.get("registrationdecisionhistory") + parameterCount)
							+ " where stablename = 'registrationdecisionhistory' "
							+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"

							+ "Update seqnoregistration set nsequenceno = "
							+ ((int) returnMap.get("registrationsample") + subSampleInputList.size())
							+ " where stablename = 'registrationsample' "
							+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"

							+ "Update seqnoregistration set nsequenceno = "
							+ ((int) returnMap.get("registrationsamplehistory") + subSampleInputList.size())
							+ " where stablename = 'registrationsamplehistory'"
							+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"

							+ "Update seqnoregistration set nsequenceno = "
							+ ((int) returnMap.get("registrationtest") + testCount)
							+ " where stablename = 'registrationtest' "
							+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"

							+ "Update seqnoregistration set nsequenceno = "
							+ ((int) returnMap.get("registrationtesthistory") + testCount)
							+ " where stablename = 'registrationtesthistory' "
							+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"

							+ "Update seqnobatchcreation set nsequenceno = "
							+ ((int) returnMap.get("batchsampleiqc") + testCount) + " where stablename = 'batchsampleiqc'"
							+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"

							+ "Update seqnomaterialmanagement set nsequenceno = "
							+ ((int) returnMap.get("batchMatSeqn") + 1)
							+ " where stablename = 'materialinventorytransaction'"
							+ "	and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";

					jdbcTemplate.execute(strSeqnoUpdate);

					returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
					return returnMap;
				} else {
					returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							commonFunction.getMultilingualMessage("IDS_USEDQTYISGREATERTHANAVAILABLEQTY",
									userInfo.getSlanguagefilename()));
					return returnMap;
				}
			} else {
				returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						commonFunction.getMultilingualMessage("IDS_NOAVAILABLEQUANTITY", userInfo.getSlanguagefilename()));
				return returnMap;
			}
		}

		@Override
		public Map<String, Object> createIQCSample(Map<String, Object> inputMap) throws Exception {

			Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
			Map<String, Object> returnMap = new HashMap<>();
			JSONObject actionType = new JSONObject();
			JSONObject jsonAuditObject = new JSONObject();

			final ObjectMapper objmap = new ObjectMapper();
			objmap.registerModule(new JavaTimeModule());

			final UserInfo userInfo = objmap.convertValue(inputMap.get("userInfo"), new TypeReference<UserInfo>() {
			});

			final Registration registration = objmap.convertValue(inputMap.get("Registration"),
					new TypeReference<Registration>() {
					});

			/*
			 * final Batchsampleiqc batchsampleiqc =
			 * objmap.convertValue(inputMap.get("Batchsampleiqc"), new
			 * TypeReference<Batchsampleiqc>() {});
			 */

			final List<RegistrationSample> registrationSample = objmap.convertValue(inputMap.get("RegistrationSample"),
					new TypeReference<List<RegistrationSample>>() {
					});

			final List<TestGroupTest> tgtTestInputList = objmap.convertValue(inputMap.get("testgrouptest"),
					new TypeReference<List<TestGroupTest>>() {
					});

//			final List<MaterialInventoryTrans> lstMatInv = objmap.convertValue(inputMap.get("inputMaterialInventoryArrData"),
//					new TypeReference<List<MaterialInventoryTrans>>() {});
			MaterialInventoryTrans objMaterialInventoryTrans = objmap
					.convertValue(inputMap.get("inputMaterialInventoryArrData"), MaterialInventoryTrans.class);

//			final String strTestList = TestGroupTests.stream().map(x -> String.valueOf(x.getNtestgrouptestcode()))
//					.distinct().collect(Collectors.joining(","));

			int nregtypecode = registration.getNregtypecode();
			int nregsubtypecode = registration.getNregsubtypecode();
			int nsampletypecode = registration.getNsampletypecode();
			int nallottedspeccode = registration.getNallottedspeccode();
			int npreregno = (int) inputMap.get("registration");
			int nreghistorycode = (int) inputMap.get("registrationhistory");
			int nregistrationparametercode = (int) inputMap.get("registrationparameter");
			int nregsamplecode = (int) inputMap.get("registrationsample");
			int nregsamplehistorycode = (int) inputMap.get("registrationsamplehistory");
			int napproveconfversioncode = (int) inputMap.get("napproveconfversioncode");
			int ntransactiontestcode = (int) inputMap.get("registrationtest");
			int ntesthistorycode = (int) inputMap.get("registrationtesthistory");
			int nregdecisionhistorycode = (int) inputMap.get("registrationdecisionhistory");
			int nbatchsampleiqccode = (int) inputMap.get("batchsampleiqc");
			int batchMatSeqn = (int) inputMap.get("batchMatSeqn");
			int ntransactionsamplecode = (int) inputMap.get("registrationsample");
			++npreregno;
			++nreghistorycode;
			++ntransactiontestcode;
			++nregistrationparametercode;
			++ntesthistorycode;
			++nbatchsampleiqccode;
			++nregdecisionhistorycode;
			++ntransactionsamplecode;

			int nbatchmastercode = (int) inputMap.get("nbatchmastercode");
			int nmaterialtypecode = (int) inputMap.get("nmaterialtypecode");
			int nmaterialcatcode = registration.getNmaterialcatcode();
			int nmaterialcode = registration.getNmaterialcode();

			inputMap.put("nregtypecode", registration.getNregtypecode());
			inputMap.put("nregsubtypecode", registration.getNregsubtypecode());
			inputMap.put("ndesigntemplatemappingcode", registration.getNdesigntemplatemappingcode());
			inputMap.put("checkBoxOperation", 3);

			int nage = 0;
			int ngendercode = 0;
			String sQuery = "";

			float nqtyused = Float.parseFloat((String) objMaterialInventoryTrans.getJsondata().get("nqtyused"));
			float namountleft = Float.parseFloat((String) objMaterialInventoryTrans.getJsondata().get("savailablequatity"));

			if (namountleft != 0.0) {
				if (nqtyused <= namountleft) {
//	    if((int)objMaterialInventoryTrans.getJsondata().get("nqtyused") < (int)objMaterialInventoryTrans.getJsondata().get("namountleft")) {

					if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()) {


						sQuery = "json_build_object('ntransactionresultcode'," + nregistrationparametercode
								+ "+RANK() over(order by tgtp.ntestgrouptestparametercode),'ntransactiontestcode',"
								+ ntransactiontestcode + "+DENSE_RANK() over(order by tgtp.ntestgrouptestcode),'npreregno',"
								+ npreregno + ")::jsonb || " + " case when tgtp.nparametertypecode="
								+ Enumeration.ParameterType.NUMERIC.getparametertype() + " then case when  "
								+ " tgtcs.ngendercode=" + ngendercode + " and " + nage
								+ " between tgtcs.nfromage and tgtcs.ntoage then jsonb_build_object('nfromage',"
								+ " tgtcs.nfromage,'ntoage',tgtcs.ntoage,'ngendercode',tgtcs.ngendercode ,"
								+ " 'sminb',case when tgtcs.slowb='null' then NULL else tgtcs.slowb end,"
								+ " 'smina',case when tgtcs.slowa='null' then NULL else tgtcs.slowa end,"
								+ " 'smaxa',case when tgtcs.shigha='null' then NULL else tgtcs.shigha end,"
								+ " 'smaxb',case when tgtcs.shighb='null' then NULL else tgtcs.shighb end,"
								+ " 'sresultvalue',case when tgtcs.sresultvalue='null' then NULL else tgtcs.sresultvalue end,"
								+ " 'nroundingdigits',tgtp.nroundingdigits,'sresult',NULL,'sfinal',NULL,'stestsynonym',"
								+ " concat(tgt.stestsynonym,'[1][0]'),'sparametersynonym',tgtp.sparametersynonym)::jsonb else "
								+ " jsonb_build_object('nfromage',tgtcs.nfromage,'ntoage',tgtcs.ntoage,'ngendercode',tgtcs.ngendercode,"
								+ " 'sminb',case when tgtcs.slowb='null' then NULL else tgtcs.slowb end,"
								+ " 'smina',case when tgtcs.slowa='null' then NULL else tgtcs.slowa end,"
								+ " 'smaxa',case when tgtcs.shigha='null' then NULL else tgtcs.shigha end,"
								+ " 'smaxb',case when tgtcs.shighb='null' then NULL else tgtcs.shighb end,"
								+ " 'sresultvalue',case when tgtcs.sresultvalue='null' then NULL else tgtcs.sresultvalue end,"
								+ "'nroundingdigits',tgtp.nroundingdigits,'sresult',NULL,'sfinal',NULL,'stestsynonym',"
								+ " concat(tgt.stestsynonym,'[1][0]'),"
								+ "'sparametersynonym',tgtp.sparametersynonym )::jsonb end else jsonb_build_object("
								+ " 'sminb',case when tgtcs.slowb='null' then NULL else tgtcs.slowb end,"
								+ " 'smina',case when tgtcs.slowa='null' then NULL else tgtcs.slowa end,"
								+ " 'smaxa',case when tgtcs.shigha='null' then NULL else tgtcs.shigha end,"
								+ " 'smaxb',case when tgtcs.shighb='null' then NULL else tgtcs.shighb end,"
								+ " 'sresultvalue',case when tgtcs.sresultvalue='null' then NULL else tgtcs.sresultvalue end,"
								+ " 'nroundingdigits',tgtp.nroundingdigits,'sresult',NULL,'sfinal',NULL,'stestsynonym',concat(tgt.stestsynonym,'[1][0]'),"
								+ " 'sparametersynonym',tgtp.sparametersynonym )::jsonb end jsondata, "
								+ userInfo.getNtranssitecode() + " nsitecode,"
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " nstatus from testgrouptest tgt inner join "
								+ " testgrouptestparameter tgtp  on tgt.ntestgrouptestcode = tgtp.ntestgrouptestcode "
								+ " left outer join testgrouptestclinicalspec tgtcs on tgtcs.ntestgrouptestparametercode = tgtp.ntestgrouptestparametercode "
								+ " and tgtcs.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
								+ " and tgtcs.ngendercode=" + ngendercode + " and " + nage
								+ " between tgtcs.nfromage and tgtcs.ntoage ";


					} else {
						sQuery = "json_build_object('ntransactionresultcode'," + nregistrationparametercode
								+ "+RANK() over(order by tgtp.ntestgrouptestparametercode),'ntransactiontestcode',"
								+ ntransactiontestcode + "+DENSE_RANK() over(order by tgtp.ntestgrouptestcode),'npreregno',"
								+ npreregno
								+ ",'sminlod',tgtnp.sminlod,'smaxlod',tgtnp.smaxlod,'sminb',tgtnp.sminb,'smina',tgtnp.smina,"
								+ "'smaxa',tgtnp.smaxa,'smaxb',tgtnp.smaxb,'sminloq',tgtnp.sminloq,'smaxloq',tgtnp.smaxloq,"
								+ "'sdisregard',tgtnp.sdisregard,'sresultvalue',tgtnp.sresultvalue,"
								+ "'nroundingdigits',tgtp.nroundingdigits,'sresult',NULL,'sfinal',NULL,'stestsynonym',concat(tgt.stestsynonym,'[1][0]'),"
								+ "'sparametersynonym',tgtp.sparametersynonym)::jsonb jsondata,"
								+ userInfo.getNtranssitecode() + " nsitecode,"
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
								+ " from testgrouptest tgt,testgrouptestparameter tgtp";
					}

					inputMap.put("nsitecode", userInfo.getNtranssitecode());

					String strRegistrationInsert = "";
					String strRegistrationHistory = "";
					String strRegistrationArno = "";
					String strRegistrationSample = "";
					String strRegistrationSampleHistory = "";
					String strRegistrationSampleArno = "";
					String strDecisionHistory = "";
					String strRegistrationStatusBlink = "";
					String strbatchsampleiqc = "";
					String batchMaterialInventory = "";
					int sampleCount = registrationSample.size();
					boolean statusflag = false;

					JSONObject jsoneditRegistration = new JSONObject(registration.getJsondata());
					JSONObject jsonuiRegistration = new JSONObject(registration.getJsonuidata());

					JSONObject jsoneditbatchsampleiqc = new JSONObject(registration.getJsondata());
					JSONObject jsonuibatchsampleiqc = new JSONObject(registration.getJsonuidata());

					jsonuiRegistration.put("npreregno", npreregno);

					strRegistrationInsert = strRegistrationInsert + "(" + npreregno + "," + nsampletypecode + ","
							+ nregtypecode + "," + nregsubtypecode + "," + registration.getNproductcatcode() + ","
							+ registration.getNproductcode() + "," + registration.getNinstrumentcatcode() + ","
							+ registration.getNinstrumentcode() + "," + registration.getNmaterialcatcode() + ","
							+ registration.getNmaterialcode() + "," + registration.getNtemplatemanipulationcode() + ","
							+ registration.getNallottedspeccode() + "," + userInfo.getNtranssitecode() + ",'"
							+ stringUtilityFunction.replaceQuote(jsoneditRegistration.toString()) + "'::JSONB,'"
							+ stringUtilityFunction.replaceQuote(jsonuiRegistration.toString()) + "'::JSONB,"
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
							+ registration.getNdesigntemplatemappingcode() + "," + registration.getNregsubtypeversioncode()
							+ "" + "," + Enumeration.TransactionStatus.YES.gettransactionstatus() + ","
							+ Enumeration.TransactionStatus.NA.gettransactionstatus() + "),";

					strRegistrationHistory = strRegistrationHistory + "(" + nreghistorycode + "," + npreregno + ","
							+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + ",'"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNusercode() + "," + userInfo.getNuserrole()
							+ "," + userInfo.getNdeputyusercode() + "," + userInfo.getNdeputyuserrole() + ",'"
							+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "'," + userInfo.getNtranssitecode() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode()
							+ "),";

					strDecisionHistory = strDecisionHistory + "(" + nregdecisionhistorycode + "," + npreregno + ","
							+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ",'"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNusercode() + "," + userInfo.getNuserrole()
							+ "," + userInfo.getNdeputyusercode() + "," + userInfo.getNdeputyuserrole() + ",'"
							+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "'," + userInfo.getNtranssitecode() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode()
							+ "),";

					strRegistrationArno = strRegistrationArno + "(" + npreregno + ",'-'," + napproveconfversioncode + ","
							+ userInfo.getNtranssitecode() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";

					strRegistrationStatusBlink = strRegistrationStatusBlink + "(" + npreregno + "," + statusflag + ","
							+ userInfo.getNtranssitecode() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";

					strbatchsampleiqc = strbatchsampleiqc + " (" + nbatchsampleiqccode + "," + nbatchmastercode + ","
							+ npreregno + "," + ntransactionsamplecode + "," + ntransactiontestcode + ","
							+ nmaterialtypecode + "," + nmaterialcatcode + "," + nmaterialcode + ",(" + batchMatSeqn
							+ ")+1," + "'" + stringUtilityFunction.replaceQuote(jsoneditbatchsampleiqc.toString()) + "'::JSONB,'"
							+ stringUtilityFunction.replaceQuote(jsonuibatchsampleiqc.toString()) + "'::JSONB," + userInfo.getNtranssitecode()
							+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";

					for (int i = sampleCount - 1; i >= 0; i--) {
						nregsamplecode++;
						nregsamplehistorycode++;
						// nregdecisionhistorycode++;

						JSONObject jsoneditObj = new JSONObject(registration.getJsondata());
						JSONObject jsonuiObj = new JSONObject(registration.getJsonuidata());

						jsonuiObj.put("ntransactionsamplecode", nregsamplecode);
						jsonuiObj.put("npreregno", npreregno);
						jsonuiObj.put("nspecsampletypecode", registrationSample.get(i).getNspecsampletypecode());
						jsonuiObj.put("ncomponentcode", registrationSample.get(i).getNcomponentcode());

						strRegistrationSample = strRegistrationSample + " (" + nregsamplecode + "," + npreregno + ",'"
								+ registrationSample.get(i).getNspecsampletypecode() + "',"
								+ registrationSample.get(i).getNcomponentcode() + ",'"
								+ stringUtilityFunction.replaceQuote(jsoneditObj.toString()) + "'::JSONB,'" + stringUtilityFunction.replaceQuote(jsonuiObj.toString())
								+ "'::JSONB," + userInfo.getNtranssitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";

						strRegistrationSampleHistory = strRegistrationSampleHistory + "(" + nregsamplehistorycode + ","
								+ nregsamplecode + "," + npreregno + ","
								+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + ",'"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNusercode() + ","
								+ userInfo.getNuserrole() + "," + userInfo.getNdeputyusercode() + ","
								+ userInfo.getNdeputyuserrole() + ",'" + stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "',"
								+ userInfo.getNtranssitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
								+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode()
								+ "),";

						strRegistrationSampleArno = strRegistrationSampleArno + "(" + nregsamplecode + "," + npreregno
								+ ",'-'" + "," + userInfo.getNtranssitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";

						List<RegistrationSample> comp = new ArrayList<RegistrationSample>();
						comp.add(registrationSample.get(i));
						List<TestGroupTest> lsttest1 = tgtTestInputList.stream()
								.filter(x -> x.getSlno() == comp.get(0).getSlno()).collect(Collectors.toList());

//						final String stestcode = lsttest1.stream().map(x -> String.valueOf(x.getNtestgrouptestcode()))
//								.distinct().collect(Collectors.joining(","));

						final String ntestcode = lsttest1.stream().map(x -> String.valueOf(x.getNtestcode())).distinct()
								.collect(Collectors.joining(","));

						if (!lsttest1.isEmpty()) {

							final Map<String, Object> testHistoryParameterMap = insertTestHistoryParameter(ntestcode,
									sQuery, userInfo, npreregno, nregsamplecode, ntransactiontestcode, ntesthistorycode,
									nregistrationparametercode, nallottedspeccode, new ArrayList<Integer>());
							ntransactiontestcode = (int) testHistoryParameterMap.get("ntransactiontestcode");
							ntesthistorycode = (int) testHistoryParameterMap.get("ntesthistorycode");
							nregistrationparametercode = (int) testHistoryParameterMap.get("ntransactionresultcode");

						}

					}

					strRegistrationInsert = "Insert into registration (npreregno, nsampletypecode, nregtypecode, nregsubtypecode, nproductcatcode, "
							+ "nproductcode,ninstrumentcatcode,ninstrumentcode,nmaterialcatcode,nmaterialcode, ntemplatemanipulationcode, nallottedspeccode, nsitecode,jsondata,jsonuidata, nstatus,ndesigntemplatemappingcode,nregsubtypeversioncode,nisiqcmaterial,nprojectmastercode) values "
							+ strRegistrationInsert.substring(0, strRegistrationInsert.length() - 1) + ";";
					jdbcTemplate.execute(strRegistrationInsert);

					strRegistrationHistory = "Insert into registrationhistory(nreghistorycode, npreregno, ntransactionstatus, dtransactiondate, nusercode, "
							+ " nuserrolecode, ndeputyusercode, ndeputyuserrolecode, scomments, nsitecode, nstatus,noffsetdtransactiondate,ntransdatetimezonecode) values "
							+ strRegistrationHistory.substring(0, strRegistrationHistory.length() - 1) + ";";
					jdbcTemplate.execute(strRegistrationHistory);

					strDecisionHistory = "Insert into registrationdecisionhistory(nregdecisionhistorycode, npreregno, ndecisionstatus, dtransactiondate, "
							+ "nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, scomments, nsitecode,nstatus,noffsetdtransactiondate,ntransdatetimezonecode  ) values "
							+ strDecisionHistory.substring(0, strDecisionHistory.length() - 1) + ";";
					jdbcTemplate.execute(strDecisionHistory);

					strRegistrationArno = "Insert into registrationarno (npreregno, sarno, napprovalversioncode, nsitecode,nstatus) values "
							+ strRegistrationArno.substring(0, strRegistrationArno.length() - 1) + ";";
					jdbcTemplate.execute(strRegistrationArno);

					strRegistrationSample = "insert into registrationsample(ntransactionsamplecode,npreregno,nspecsampletypecode,ncomponentcode,jsondata,jsonuidata, "
							+ "nsitecode,nstatus) values "
							+ strRegistrationSample.substring(0, strRegistrationSample.length() - 1) + ";";
					jdbcTemplate.execute(strRegistrationSample);

					strRegistrationSampleHistory = "Insert into registrationsamplehistory(nsamplehistorycode, ntransactionsamplecode, npreregno, ntransactionstatus, "
							+ "dtransactiondate, nusercode, nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments,nsitecode,nstatus,noffsetdtransactiondate,ntransdatetimezonecode ) values "
							+ strRegistrationSampleHistory.substring(0, strRegistrationSampleHistory.length() - 1) + ";";
					jdbcTemplate.execute(strRegistrationSampleHistory);

					strRegistrationSampleArno = "Insert into registrationsamplearno (ntransactionsamplecode,npreregno,ssamplearno,nsitecode,nstatus) values "
							+ strRegistrationSampleArno.substring(0, strRegistrationSampleArno.length() - 1) + ";";
					jdbcTemplate.execute(strRegistrationSampleArno);

					strRegistrationStatusBlink = "Insert into registrationflagstatus (npreregno,bflag,nsitecode,nstatus) "
							+ " values" + strRegistrationStatusBlink.substring(0, strRegistrationStatusBlink.length() - 1)
							+ ";";
					jdbcTemplate.execute(strRegistrationStatusBlink);

//					String materialinventorytransactiondata = jdbcTemplate.queryForObject(
//							"select jsondata from materialinventorytransaction where nmaterialinventorycode="
//									+ objMaterialInventoryTrans.getNmaterialinventorycode()
//									+ " order by nmaterialinventtranscode fetch first 1 row only",
//							String.class);

					final Map<String, Object> jsonData = new HashMap<>();
					final Map<String, Object> jsonUIData = new HashMap<>();

					jsonData.put("npreregno", npreregno);
					jsonData.put("nformcode", userInfo.getNformcode());

					
					//		JSONObject jsonObject = new JSONObject();
					jsonData.putAll(objMaterialInventoryTrans.getJsondata());
					jsonUIData.putAll(objMaterialInventoryTrans.getJsonuidata());

					// jsonObject=new JSONObject(materialinventorytransactiondata.toString());

					batchMaterialInventory = " insert into materialinventorytransaction (nmaterialinventtranscode ,nmaterialinventorycode ,ninventorytranscode ,ntransactiontype ,nsectioncode ,"
							+ " nresultusedmaterialcode,nqtyreceived,nqtyissued,jsondata,jsonuidata,dtransactiondate,ntztransactiondate,noffsetdtransactiondate,nsitecode,nstatus) values "
							+ "((" + batchMatSeqn + ")+1," + objMaterialInventoryTrans.getNmaterialinventorycode() + ","
							+ Enumeration.TransactionStatus.OUTSIDE.gettransactionstatus() + ","
							+ Enumeration.TransactionStatus.USED.gettransactionstatus() + ","
							+ objMaterialInventoryTrans.getNsectioncode() + ","
							+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ",0,"
							+ objMaterialInventoryTrans.getJsondata().get("nqtyused") + ",'"
							+ stringUtilityFunction.replaceQuote(objmap.writeValueAsString(jsonData).toString()) + "','"
							+ stringUtilityFunction.replaceQuote(objmap.writeValueAsString(jsonUIData).toString()) + "','"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNtimezonecode() + ","
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtranssitecode() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
					jdbcTemplate.execute(batchMaterialInventory);

					strbatchsampleiqc = "Insert into batchsampleiqc(nbatchsampleiqccode, nbatchmastercode, npreregno,ntransactionsamplecode,ntransactiontestcode, nmaterialtypecode , "
							+ "nmaterialcatcode , nmaterialcode ,nmaterialinventtranscode,jsondata, jsonuidata, nsitecode,nstatus ) values "
							+ strbatchsampleiqc.substring(0, strbatchsampleiqc.length() - 1) + ";";
					jdbcTemplate.execute(strbatchsampleiqc);

					inputMap.put("npreregno", String.valueOf(npreregno));

//			returnMap = getDynamicRegistration(inputMap, userInfo);

					final ResponseEntity<Object> IQCSampleResponseEntity = getBatchIqcSampleAction(inputMap, userInfo);
					returnMap = (Map<String, Object>) IQCSampleResponseEntity.getBody();
					List<BatchsampleIqc> lstbatchsampleiqc = new ArrayList<>();
					lstbatchsampleiqc.add((BatchsampleIqc) returnMap.get("selectedIqcsample"));
					auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
					auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
					auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
					actionType.put("batchsampleiqc", "IDS_ADDBATCHIQCSAMPLE");
					jsonAuditObject.put("batchsampleiqc", lstbatchsampleiqc);
					auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);
					returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
					return returnMap;
				} else {
					returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							commonFunction.getMultilingualMessage("IDS_USEDQTYISGREATERTHANAVAILABLEQTY",
									userInfo.getSlanguagefilename()));
					return returnMap;
				}
			} else {
				returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						commonFunction.getMultilingualMessage("IDS_NOAVAILABLEQUANTITY", userInfo.getSlanguagefilename()));
				return returnMap;
			}
		}

		private Map<String, Object> insertTestHistoryParameter(final String stestcode, final String sQuery,
				final UserInfo userInfo, final int npreregno, final int nregsamplecode, int ntransactiontestcode,
				int ntesthistorycode, int nregistrationparametercode, int nallottedspeccode,
				List<Integer> transactionCodeList) throws Exception {

			final ObjectMapper objMapper = new ObjectMapper();
			final String testGroupQuery = "select tgt.*, s.ssectionname, m.smethodname from testgrouptest tgt,section s,method m,"
					+ " testgroupspecsampletype tgts where tgt.nsectioncode=s.nsectioncode "
					+ " and tgt.nmethodcode=m.nmethodcode and tgt.nspecsampletypecode=tgts.nspecsampletypecode "
					// + " and tgt.ntestgrouptestcode in (" + stestcode + ") "
					+ " and tgt.ntestcode in (" + stestcode + ") " + " and tgts.nallottedspeccode=" + nallottedspeccode + ""
					+ " and s.nsitecode=m.nsitecode and m.nsitecode=" + userInfo.getNmastersitecode();
			final List<TestGroupTest> testGroupTestList = jdbcTemplate.query(testGroupQuery, new TestGroupTest());

			final String testParameterQuery = "select tgtp.ntestgrouptestcode, tgtp.ntestgrouptestparametercode,tgtp.ntestparametercode,tgtp.nparametertypecode,"
					+ " COALESCE(tgf.ntestgrouptestformulacode,-1) ntestgrouptestformulacode,"
					+ " tgtp.nunitcode,tgtp.nresultmandatory,tgtp.nreportmandatory," + sQuery
					+ " left outer join testgrouptestnumericparameter tgtnp on "
					+ " tgtnp.ntestgrouptestparametercode=tgtp.ntestgrouptestparametercode "
					+ " left outer join testgrouptestformula tgf on tgtnp.ntestgrouptestparametercode=tgf.ntestgrouptestparametercode"
					+ " and tgf.ntransactionstatus = " + Enumeration.TransactionStatus.YES.gettransactionstatus()
					+ " where tgt.ntestgrouptestcode=tgtp.ntestgrouptestcode" + "  and tgt.nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgtp.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgtp.ntestgrouptestcode in ("
					+ testGroupTestList.get(0).getNtestgrouptestcode() + ");";
			final List<TestGroupTestParameter> tgtParameterList = jdbcTemplate.query(testParameterQuery,
					new TestGroupTestParameter());

			final ArrayList<String> replicateTestList = new ArrayList<String>();
			final ArrayList<String> replicateTestHistoryList = new ArrayList<String>();
			final ArrayList<String> replicateTestParameterList = new ArrayList<String>();

			for (TestGroupTest testGroupTest : testGroupTestList) {
				if (testGroupTest.getNrepeatcountno() > 0) {
					for (int repeatNo = 1; repeatNo <= testGroupTest.getNrepeatcountno(); repeatNo++) {
						int nttestcode = ntransactiontestcode++;
						transactionCodeList.add(nttestcode);
						replicateTestList.add("(" + nttestcode + "," + nregsamplecode + "," + npreregno + ","
								+ testGroupTest.getNtestgrouptestcode() + "," + testGroupTest.getNinstrumentcatcode()
								+ ",-1," + repeatNo + ",0," + " json_build_object('ntransactiontestcode', " + nttestcode
								+ ",'npreregno'," + npreregno + ",'ntransactionsamplecode'," + nregsamplecode
								+ ",'ssectionname','" + testGroupTest.getSsectionname() + "','smethodname','"
								+ testGroupTest.getSmethodname() + "','ncost'," + testGroupTest.getNcost() + ","
								+ "'stestname','" + testGroupTest.getStestsynonym() + "'," + "'stestsynonym',concat('"
								+ testGroupTest.getStestsynonym() + "','[" + repeatNo + "][0]'))::jsonb,"
								+ userInfo.getNtranssitecode() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
								+ testGroupTest.getNtestcode() + "," + testGroupTest.getNsectioncode() + ","
								+ testGroupTest.getNmethodcode() + ")");
						int nttesthistorycode = ntesthistorycode++;
						replicateTestHistoryList.add("(" + nttesthistorycode + "," + nttestcode + "," + nregsamplecode + ","
								+ npreregno + "," + userInfo.getNformcode() + ","
								+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + ","
								+ userInfo.getNusercode() + "," + userInfo.getNuserrole() + ","
								+ userInfo.getNdeputyusercode() + "," + userInfo.getNdeputyuserrole() + ",'"
								+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "','" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',-1,"
								+ userInfo.getNtranssitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
								+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode()
								+ ")");

						for (TestGroupTestParameter testGroupTestParameter : tgtParameterList) {
							// int nttestparametercode = nregistrationparametercode++;

//								final Map<String, Object> mapObject = testGroupTestParameter.getJsondata();
//								mapObject.put("ntransactionresultcode", nttestparametercode);
//								mapObject.put("stestsynonym",testGroupTest.getStestsynonym()+ "[" +repeatNo +"][0]");

							if (testGroupTestParameter.getNtestgrouptestcode() == testGroupTest.getNtestgrouptestcode()) {
								int nttestparametercode = nregistrationparametercode++;
								final Map<String, Object> mapObject = testGroupTestParameter.getJsondata();
								mapObject.put("ntransactionresultcode", nttestparametercode);
								mapObject.put("stestsynonym", testGroupTest.getStestsynonym() + "[" + repeatNo + "][0]");
								mapObject.put("ntransactiontestcode", nttestcode);

								replicateTestParameterList.add("(" + nttestparametercode + "," + npreregno + ","
										+ nttestcode + "," + testGroupTestParameter.getNtestgrouptestparametercode() + ","
										+ testGroupTestParameter.getNtestparametercode() + ","
										+ testGroupTestParameter.getNparametertypecode() + ","
										+ testGroupTestParameter.getNtestgrouptestformulacode() + ","
										+ testGroupTestParameter.getNunitcode() + ","
										+ testGroupTestParameter.getNresultmandatory() + ","
										+ testGroupTestParameter.getNreportmandatory() + ","
										// + testGroupTestParameter.getJsondata()+","
										// + "'" + ReplaceQuote(testGroupTestParameter.getJsondata().toString()) + "',"
										// + "'"
										// +objmap.writeValueAsString(testGroupTestParameter.getJsondata().replace("ntransactionresultcode",testGroupTestParameter.getJsondata().get("ntransactionresultcode"),
										// nttestparametercode)) +"',"
										+ "'" + objMapper.writeValueAsString(mapObject) + "',"
										+ userInfo.getNtranssitecode() + ","
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")");
							}
						}

					}
				} else {
					int nttestcode = ntransactiontestcode++;
					transactionCodeList.add(nttestcode);
					replicateTestList.add("(" + nttestcode + "," + nregsamplecode + "," + npreregno + ","
							+ testGroupTest.getNtestgrouptestcode() + "," + testGroupTest.getNinstrumentcatcode()
							+ ",-1,1,0," + " json_build_object('ntransactiontestcode', " + nttestcode + ",'npreregno',"
							+ npreregno + ",'ntransactionsamplecode'," + nregsamplecode + ",'ssectionname','"
							+ testGroupTest.getSsectionname() + "','smethodname','" + testGroupTest.getSmethodname()
							+ "','ncost'," + testGroupTest.getNcost() + "," + "'stestname','"
							+ testGroupTest.getStestsynonym() + "'," + "'stestsynonym',concat('"
							+ testGroupTest.getStestsynonym() + "','[1][0]'))::jsonb," + userInfo.getNtranssitecode() + ",'"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
							+ testGroupTest.getNtestcode() + "," + testGroupTest.getNsectioncode() + ","
							+ testGroupTest.getNmethodcode() + ")");

					int nttesthistorycode = ntesthistorycode++;
					replicateTestHistoryList.add("(" + nttesthistorycode + "," + nttestcode + "," + nregsamplecode + ","
							+ npreregno + "," + userInfo.getNformcode() + ","
							+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + ","
							+ userInfo.getNusercode() + "," + userInfo.getNuserrole() + "," + userInfo.getNdeputyusercode()
							+ "," + userInfo.getNdeputyuserrole() + ",'" + stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "','"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',-1," + userInfo.getNtranssitecode() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode()
							+ ")");

					for (TestGroupTestParameter testGroupTestParameter : tgtParameterList) {
//						int nttestparametercode = nregistrationparametercode++;
//						
//						final Map<String, Object> mapObject = testGroupTestParameter.getJsondata();
//						mapObject.put("ntransactionresultcode", nttestparametercode);

						if (testGroupTestParameter.getNtestgrouptestcode() == testGroupTest.getNtestgrouptestcode()) {
							int nttestparametercode = nregistrationparametercode++;

							final Map<String, Object> mapObject = testGroupTestParameter.getJsondata();
							mapObject.put("ntransactionresultcode", nttestparametercode);
							mapObject.put("ntransactiontestcode",nttestcode);
							replicateTestParameterList.add("(" + nttestparametercode + "," + npreregno + "," + nttestcode
									+ "," + testGroupTestParameter.getNtestgrouptestparametercode() + ","
									+ testGroupTestParameter.getNtestparametercode() + ","
									+ testGroupTestParameter.getNparametertypecode() + ","
									+ testGroupTestParameter.getNtestgrouptestformulacode() + ","
									+ testGroupTestParameter.getNunitcode() + ","
									+ testGroupTestParameter.getNresultmandatory() + ","
									+ testGroupTestParameter.getNreportmandatory() + ","
									// + testGroupTestParameter.getJsondata()+","
									// + "'" + ReplaceQuote(testGroupTestParameter.getJsondata().toString()) + "',"
									// + "'" +objmap.writeValueAsString(testGroupTestParameter.getJsondata()) +"',"
									+ "'" + objMapper.writeValueAsString(mapObject) + "'," + userInfo.getNtranssitecode()
									+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")");
						}
					}
				}
			}
			if (replicateTestList.size() > 0) {
				final String strRegistrationTest = "insert into registrationtest (ntransactiontestcode,ntransactionsamplecode,npreregno,"
						+ "ntestgrouptestcode,ninstrumentcatcode,nchecklistversioncode,ntestrepeatno,ntestretestno,jsondata,nsitecode,dmodifieddate, "
						+ " nstatus,ntestcode,nsectioncode,nmethodcode) values ";
				jdbcTemplate.execute(strRegistrationTest + String.join(",", replicateTestList));

			}
			if (replicateTestHistoryList.size() > 0) {
				final String strRegistrationTestHistory = "insert into registrationtesthistory (ntesthistorycode, ntransactiontestcode, "
						+ " ntransactionsamplecode, npreregno, nformcode, ntransactionstatus,"
						+ "	nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode,scomments,"
						+ " dtransactiondate,nsampleapprovalhistorycode, nsitecode,"
						+ " nstatus,noffsetdtransactiondate,ntransdatetimezonecode) values";
				jdbcTemplate.execute(strRegistrationTestHistory + String.join(",", replicateTestHistoryList));
			}
			if (replicateTestParameterList.size() > 0) {
				final String strRegTestParameter = " insert into registrationparameter (ntransactionresultcode,"
						+ " npreregno,ntransactiontestcode,ntestgrouptestparametercode,"
						+ " ntestparametercode,nparametertypecode,ntestgrouptestformulacode,"
						+ " nunitcode, nresultmandatory,nreportmandatory,jsondata,nsitecode,nstatus) values";
				jdbcTemplate.execute(strRegTestParameter + String.join(",", replicateTestParameterList));
			}

			final Map<String, Object> returnMap = new HashMap<String, Object>();
			returnMap.put("ntesthistorycode", ntesthistorycode);
			returnMap.put("ntransactiontestcode", ntransactiontestcode);
			returnMap.put("ntransactionresultcode", nregistrationparametercode);
			returnMap.put("transactionCodeList", transactionCodeList);
			return returnMap;
		}

		@SuppressWarnings("unchecked")
		@Override
		public ResponseEntity<Object> getMaterialAvailQtyBasedOnInv(int materialInvCode, int sectionCode, UserInfo userInfo,
				Map<String, Object> inputMap) throws Exception {
			Map<String, Object> returnMap = new HashMap<>();
			int inputSectioncode = inputMap.get("needsection").getClass().getName() == "java.lang.Integer"
					? (int) inputMap.get("needsection")
					: ((Short) inputMap.get("needsection")).intValue();
			int nsectioncode = inputSectioncode == Enumeration.TransactionStatus.NO.gettransactionstatus()
					? Enumeration.TransactionStatus.NA.gettransactionstatus()
					: (int) inputMap.get("nsectioncode");
			returnMap.putAll(
					(Map<String, Object>) getAvailableMaterialQuantity(materialInvCode, nsectioncode, userInfo).getBody());
			returnMap.put("selectedInventoryUnit", returnMap.get("inventoryTransaction"));
			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		}

		
		@Override
		public ResponseEntity<Object> getBatchIqcSampleAction(Map<String, Object> inputMap, UserInfo userInfo)
				throws Exception {

			Map<String, Object> returnMap = new HashMap<>();

			int nbatchmastercode = (int) inputMap.get("nbatchmastercode");

			String getSquery = " select iqc.nbatchsampleiqccode,iqc.ntransactionsamplecode,iqc.ntransactiontestcode,"
					+ " rarno.sarno," + " iqc.nbatchmastercode,iqc.jsondata,"
					+ " iqc.npreregno,iqc.nmaterialcatcode,iqc.nmaterialcode,iqc.nmaterialinventtranscode,"
					+ " iqc.jsondata->>'nsectioncode' nsectioncode,"
					+ " iqc.jsondata->>'nmaterialinventorycode' nmaterialinventorycode,"
					+ " iqc.jsondata->>'sunitname' as sunitname,rt.ntransactiontestcode,"
					+ " r.nallottedspeccode,rs.nspecsampletypecode,"
					+ " iqc.jsondata->>'sremarks' as sremarks,iqc.jsondata->>'smaterial' smaterialname,"
					+ " iqc.jsondata->>'sinventoryid' sinventoryid,"
					+ " iqc.jsondata->>'savailablequatity' savailablequatity," + " iqc.jsondata->>'susedquantity' nqtyused,"
					+ " iqc.jsondata->>'materialtype' smaterialtype" + " from batchsampleiqc iqc,registrationarno rarno,"
					+ " registration r,registrationsample rs,registrationtest rt " + " where nbatchmastercode = "
					+ nbatchmastercode + "" + " and r.npreregno=iqc.npreregno" + " and rt.npreregno=r.npreregno"
					+ " and rs.npreregno=iqc.npreregno" + " and iqc.npreregno = rarno.npreregno"
					+ " and rt.ntransactiontestcode = iqc.ntransactiontestcode" + " and rarno.nsitecode = "
					+ userInfo.getNtranssitecode() + "" + " and rarno.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rt.nsitecode = "
					+ userInfo.getNtranssitecode() + "" + " and rt.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rs.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rs.nsitecode = "
					+ userInfo.getNtranssitecode() + "" + " and r.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and r.nsitecode = "
					+ userInfo.getNtranssitecode() + "" + " and iqc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and iqc.nsitecode = "
					+ userInfo.getNtranssitecode() + " " + " order by nbatchsampleiqccode desc";
			List<BatchsampleIqc> lstBatchiqc = (List<BatchsampleIqc>) jdbcTemplate.query(getSquery,
					new BatchsampleIqc());

			if (lstBatchiqc.size() > 0) {
				returnMap.putAll(
						(Map<String, Object>) getAvailableMaterialQuantity(lstBatchiqc.get(0).getNmaterialinventorycode(),
								lstBatchiqc.get(0).getNsectioncode(), userInfo).getBody());
				returnMap.put("selectedInventoryUnit", returnMap.get("inventoryTransaction"));
				returnMap.put("selectedIqcsample", lstBatchiqc.get(0));
			}
			returnMap.put("iqcsample", lstBatchiqc);
			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		}

		public void insertChainCustody(int nchainCustodyCode, final int nbatchMasterCode, final String batchID,
				final boolean subSampleNeeded, final boolean isiqcdata, final UserInfo userInfo) throws Exception {
			String insertCustodyQuery = "";
			if (subSampleNeeded && isiqcdata) {
				insertCustodyQuery = "Insert  into chaincustody (nchaincustodycode,nformcode,ntablepkno,stablepkcolumnname,stablename,sitemno,ntransactionstatus,"
						+ "nusercode,nuserrolecode,dtransactiondate,ntztransactiondate,noffsetdtransactiondate,sremarks,nsitecode,nstatus)"
						+ "select " + "(" + nchainCustodyCode
						+ ")+ rank() over(order by a.npreregno,a.ntransactionsamplecode,a.ntransactiontestcode) as nchaincustodycode,"
						+ " a.nformcode,a.ntablepkno,a.stablepkcolumnname,a.stablename,a.sitemno,a.ntransactionstatus,a.nusercode,a.nuserrolecode,"
						// + " a.dtransactiondate,"
						+ "'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "' as dtransactiondate," + " a.ntztransactiondate,"
						+ " a.noffsetdtransactiondate,a.sremarks,a.nsitecode,a.nstatus"
						+ " from (select bs.npreregno,bs.ntransactionsamplecode,bs.ntransactiontestcode,"
						// + "("+nchainCustodyCode+")+ rank() over(order by
						// bs.npreregno,bs.ntransactionsamplecode,bs.ntransactiontestcode) as
						// nchaincustodycode, "
						+ "" + Enumeration.QualisForms.BATCHCREATION.getqualisforms()
						+ " as nformcode,rsa.ntransactionsamplecode as ntablepkno,'ntransactionsamplecode' as stablepkcolumnname,'registrationsamplearno' as stablename,rsa.ssamplearno as sitemno, "
						+ "" + Enumeration.TransactionStatus.INITIATED.gettransactionstatus() + " as ntransactionstatus,"
						+ userInfo.getNusercode() + " as nusercode," + userInfo.getNuserrole() + " as nuserrolecode, " + "'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' as dtransactiondate," + userInfo.getNtimezonecode()
						+ " as ntztransactiondate," + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ " as noffsetdtransactiondate, " + "'" + batchID + "'||' '||'"
						+ commonFunction.getMultilingualMessage("IDS_INTITATED", userInfo.getSlanguagefilename())
						+ "'||' '||'" + commonFunction.getMultilingualMessage("IDS_FOR", userInfo.getSlanguagefilename())
						+ "'||' '||tgt.stestsynonym ||'['|| rt.ntestrepeatno || ']'||'['|| rt.ntestretestno ||']' ||' '||'"
						+ commonFunction.getMultilingualMessage("IDS_IN", userInfo.getSlanguagefilename())
						+ "'||' '||s.ssectionname ||','||'"
						+ commonFunction.getMultilingualMessage(Enumeration.TransAction.BATCHINITIATED.getTransAction(),
								userInfo.getSlanguagefilename())
						+ "' as sremarks , "
						// +
						// "'"+commonFunction.getMultilingualMessage(Enumeration.TransAction.TESTINITIATED.getTransAction(),
						// userInfo.getSlanguagefilename())+"'||' '||tgt.stestsynonym ||'['||
						// rt.ntestrepeatno || ']'||'['|| rt.ntestretestno ||']' || s.ssectionname
						// ||'"+batchID+"' as sremarks , "
						+ "" + userInfo.getNtranssitecode() + " as nsitecode,"
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " as nstatus "
						+ "from batchmaster bm,batchsampleiqc bs,registrationtest rt,testgrouptest tgt,section s,registrationarno ra,registrationsamplearno rsa "
						+ "where ra.npreregno =bs.npreregno " + "and rsa.ntransactionsamplecode =bs.ntransactionsamplecode "
						+ "and bm.nbatchmastercode = bs.nbatchmastercode " + "and bs.npreregno = rt.npreregno "
						+ "and bs.ntransactionsamplecode = rt.ntransactionsamplecode "
						+ "and bs.ntransactiontestcode= rt.ntransactiontestcode "
						+ "and tgt.ntestgrouptestcode = rt.ntestgrouptestcode " + "and tgt.ntestcode = rt.ntestcode "
						+ "and rt.nsectioncode = s.nsectioncode " + "and bm.nbatchmastercode in (" + nbatchMasterCode + ") "
						+ "and bm.nsitecode =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ra.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "and rsa.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and rt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "and bm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and bs.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " and tgt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and s.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " union all" + " select bs.npreregno,bs.ntransactionsamplecode,bs.ntransactiontestcode,"
						// + "("+nchainCustodyCode+")+ rank() over(order by
						// bs.npreregno,bs.ntransactionsamplecode,bs.ntransactiontestcode) as
						// nchaincustodycode, "
						+ "" + Enumeration.QualisForms.BATCHCREATION.getqualisforms()
						+ " as nformcode,rsa.ntransactionsamplecode as ntablepkno,'ntransactionsamplecode' as stablepkcolumnname,'registrationsamplearno' as stablename,rsa.ssamplearno as sitemno, "
						+ "" + Enumeration.TransactionStatus.INITIATED.gettransactionstatus() + " as ntransactionstatus,"
						+ userInfo.getNusercode() + " as nusercode," + userInfo.getNuserrole() + " as nuserrolecode, " + "'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' as dtransactiondate," + userInfo.getNtimezonecode()
						+ " as ntztransactiondate," + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ " as noffsetdtransactiondate, " + "'" + batchID + "'||' '||'"
						+ commonFunction.getMultilingualMessage("IDS_INTITATED", userInfo.getSlanguagefilename())
						+ "'||' '||'" + commonFunction.getMultilingualMessage("IDS_FOR", userInfo.getSlanguagefilename())
						+ "'||' '||tgt.stestsynonym ||'['|| rt.ntestrepeatno || ']'||'['|| rt.ntestretestno ||']' ||' '||'"
						+ commonFunction.getMultilingualMessage("IDS_IN", userInfo.getSlanguagefilename())
						+ "'||' '||s.ssectionname ||','||'"
						+ commonFunction.getMultilingualMessage(Enumeration.TransAction.BATCHINITIATED.getTransAction(),
								userInfo.getSlanguagefilename())
						+ "' as sremarks , "
						// +
						// "'"+commonFunction.getMultilingualMessage(Enumeration.TransAction.TESTINITIATED.getTransAction(),
						// userInfo.getSlanguagefilename())+"'||' '||tgt.stestsynonym ||'['||
						// rt.ntestrepeatno || ']'||'['|| rt.ntestretestno ||']' || s.ssectionname
						// ||'"+batchID+"' as sremarks , "
						+ "" + userInfo.getNtranssitecode() + " as nsitecode,"
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " as nstatus "
						+ "from batchmaster bm,batchsample bs,registrationtest rt,testgrouptest tgt,section s,registrationarno ra,registrationsamplearno rsa "
						+ "where ra.npreregno =bs.npreregno " + "and rsa.ntransactionsamplecode =bs.ntransactionsamplecode "
						+ "and bm.nbatchmastercode = bs.nbatchmastercode " + "and bs.npreregno = rt.npreregno "
						+ "and bs.ntransactionsamplecode = rt.ntransactionsamplecode "
						+ "and bs.ntransactiontestcode= rt.ntransactiontestcode "
						+ "and tgt.ntestgrouptestcode = rt.ntestgrouptestcode " + "and tgt.ntestcode = rt.ntestcode "
						+ "and rt.nsectioncode = s.nsectioncode " + "and bm.nbatchmastercode in (" + nbatchMasterCode + ") "
						+ "and bm.nsitecode =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ra.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ "and rsa.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and rt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ "and bm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and bs.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ "and tgt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and s.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")a";
			} else if (subSampleNeeded && !isiqcdata) {
				insertCustodyQuery = "Insert  into chaincustody (nchaincustodycode,nformcode,ntablepkno,stablepkcolumnname,stablename,sitemno,ntransactionstatus,"
						+ "nusercode,nuserrolecode,dtransactiondate,ntztransactiondate,noffsetdtransactiondate,sremarks,nsitecode,nstatus)"
						+ "select (" + nchainCustodyCode
						+ ")+ rank() over(order by bs.npreregno,bs.ntransactionsamplecode,bs.ntransactiontestcode) as nchaincustodycode, "
						+ "" + Enumeration.QualisForms.BATCHCREATION.getqualisforms()
						+ " as nformcode,rsa.ntransactionsamplecode as ntablepkno,'ntransactionsamplecode' as stablepkcolumnname,'registrationsamplearno' as stablename,rsa.ssamplearno as sitemno, "
						+ "" + Enumeration.TransactionStatus.INITIATED.gettransactionstatus() + " as ntransactionstatus,"
						+ userInfo.getNusercode() + " as nusercode," + userInfo.getNuserrole() + " as nuserrolecode, " + "'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' as dtransactiondate," + userInfo.getNtimezonecode()
						+ " as ntztransactiondate," + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ " as noffsetdtransactiondate, " + "'" + batchID + "'||' '||'"
						+ commonFunction.getMultilingualMessage("IDS_INTITATED", userInfo.getSlanguagefilename())
						+ "'||' '||'" + commonFunction.getMultilingualMessage("IDS_FOR", userInfo.getSlanguagefilename())
						+ "'||' '||tgt.stestsynonym ||'['|| rt.ntestrepeatno || ']'||'['|| rt.ntestretestno ||']' ||' '||'"
						+ commonFunction.getMultilingualMessage("IDS_IN", userInfo.getSlanguagefilename())
						+ "'||' '||s.ssectionname ||','||'"
						+ commonFunction.getMultilingualMessage(Enumeration.TransAction.BATCHINITIATED.getTransAction(),
								userInfo.getSlanguagefilename())
						+ "' as sremarks , "
						// +
						// "'"+commonFunction.getMultilingualMessage(Enumeration.TransAction.TESTINITIATED.getTransAction(),
						// userInfo.getSlanguagefilename())+"'||' '||tgt.stestsynonym ||'['||
						// rt.ntestrepeatno || ']'||'['|| rt.ntestretestno ||']' || s.ssectionname
						// ||'"+batchID+"' as sremarks , "
						+ "" + userInfo.getNtranssitecode() + " as nsitecode,"
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " as nstatus "
						+ "from batchmaster bm,batchsample bs,registrationtest rt,testgrouptest tgt,section s,registrationarno ra,registrationsamplearno rsa "
						+ "where ra.npreregno =bs.npreregno " + "and rsa.ntransactionsamplecode =bs.ntransactionsamplecode "
						+ "and bm.nbatchmastercode = bs.nbatchmastercode " + "and bs.npreregno = rt.npreregno "
						+ "and bs.ntransactionsamplecode = rt.ntransactionsamplecode "
						+ "and bs.ntransactiontestcode= rt.ntransactiontestcode "
						+ "and tgt.ntestgrouptestcode = rt.ntestgrouptestcode " + "and tgt.ntestcode = rt.ntestcode "
						+ "and rt.nsectioncode = s.nsectioncode " + "and bm.nbatchmastercode in (" + nbatchMasterCode + ") "
						+ "and bm.nsitecode =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ra.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "and rsa.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and rt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "and bm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and bs.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " and tgt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and s.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
			} else if (!subSampleNeeded && isiqcdata) {
				insertCustodyQuery = "Insert  into chaincustody (nchaincustodycode,nformcode,ntablepkno,stablepkcolumnname,stablename,sitemno,ntransactionstatus, "
						+ "	nusercode,nuserrolecode,dtransactiondate,ntztransactiondate,noffsetdtransactiondate,sremarks,nsitecode,nstatus) "
						+ "	select (" + nchainCustodyCode
						+ ")+ rank() over(order by bs.npreregno,bs.ntransactionsamplecode,bs.ntransactiontestcode) as nchaincustodycode, "
						+ "	" + Enumeration.QualisForms.BATCHCREATION.getqualisforms()
						+ " as nformcode,ra.npreregno as ntablepkno,'npreregno' as stablepkcolumnname,'registrationarno' as stablename,ra.sarno as sitemno, "
						+ "	" + Enumeration.TransactionStatus.INITIATED.gettransactionstatus() + " as ntransactionstatus,"
						+ userInfo.getNusercode() + " as nusercode," + userInfo.getNuserrole() + " as nuserrolecode, "
						+ "	'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "' as dtransactiondate," + userInfo.getNtimezonecode()
						+ " as ntztransactiondate," + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ " as noffsetdtransactiondate, " + "'" + batchID + "'||' '||'"
						+ commonFunction.getMultilingualMessage("IDS_INTITATED", userInfo.getSlanguagefilename())
						+ "'||' '||'" + commonFunction.getMultilingualMessage("IDS_FOR", userInfo.getSlanguagefilename())
						+ "'||' '||tgt.stestsynonym ||'['|| rt.ntestrepeatno || ']'||'['|| rt.ntestretestno ||']' ||' '||'"
						+ commonFunction.getMultilingualMessage("IDS_IN", userInfo.getSlanguagefilename())
						+ "'||' '||s.ssectionname ||','||'"
						+ commonFunction.getMultilingualMessage(Enumeration.TransAction.BATCHINITIATED.getTransAction(),
								userInfo.getSlanguagefilename())
						+ "' as sremarks , "
						// +
						// "'"+commonFunction.getMultilingualMessage(Enumeration.TransAction.TESTINITIATED.getTransAction(),
						// userInfo.getSlanguagefilename())+"'||' '||tgt.stestsynonym ||'['||
						// rt.ntestrepeatno || ']'||'['|| rt.ntestretestno ||']' || s.ssectionname
						// ||'"+batchID+"' as sremarks , "
						+ "	" + userInfo.getNtranssitecode() + " as nsitecode,"
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " as nstatus "
						+ "	from batchmaster bm,batchsampleiqc bs,registrationtest rt,testgrouptest tgt,section s,registrationarno ra,registrationsamplearno rsa "
						+ "	where ra.npreregno =bs.npreregno "
						+ "	and rsa.ntransactionsamplecode =bs.ntransactionsamplecode "
						+ "	and bm.nbatchmastercode = bs.nbatchmastercode " + "	and bs.npreregno = rt.npreregno "
						+ "	and bs.ntransactionsamplecode = rt.ntransactionsamplecode "
						+ "	and bs.ntransactiontestcode= rt.ntransactiontestcode "
						+ "	and tgt.ntestgrouptestcode = rt.ntestgrouptestcode " + "	and tgt.ntestcode = rt.ntestcode "
						+ "	and rt.nsectioncode = s.nsectioncode " + "	and bm.nbatchmastercode in (" + nbatchMasterCode
						+ ") " + "	and bm.nsitecode =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ra.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "	and rt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "	and bm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and bs.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "	and tgt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and s.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
			} else if (!subSampleNeeded && !isiqcdata) {

				insertCustodyQuery = "Insert  into chaincustody (nchaincustodycode,nformcode,ntablepkno,stablepkcolumnname,stablename,sitemno,ntransactionstatus, "
						+ "	nusercode,nuserrolecode,dtransactiondate,ntztransactiondate,noffsetdtransactiondate,sremarks,nsitecode,nstatus) "
						+ "	select (" + nchainCustodyCode
						+ ")+ rank() over(order by bs.npreregno,bs.ntransactionsamplecode,bs.ntransactiontestcode) as nchaincustodycode, "
						+ "	" + Enumeration.QualisForms.BATCHCREATION.getqualisforms()
						+ " as nformcode,ra.npreregno as ntablepkno,'npreregno' as stablepkcolumnname,'registrationarno' as stablename,ra.sarno as sitemno, "
						+ "	" + Enumeration.TransactionStatus.INITIATED.gettransactionstatus() + " as ntransactionstatus,"
						+ userInfo.getNusercode() + " as nusercode," + userInfo.getNuserrole() + " as nuserrolecode, "
						+ "	'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "' as dtransactiondate," + userInfo.getNtimezonecode()
						+ " as ntztransactiondate," + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ " as noffsetdtransactiondate, " + "'" + batchID + "'||' '||'"
						+ commonFunction.getMultilingualMessage("IDS_INTITATED", userInfo.getSlanguagefilename())
						+ "'||' '||'" + commonFunction.getMultilingualMessage("IDS_FOR", userInfo.getSlanguagefilename())
						+ "'||' '||tgt.stestsynonym ||'['|| rt.ntestrepeatno || ']'||'['|| rt.ntestretestno ||']' ||' '||'"
						+ commonFunction.getMultilingualMessage("IDS_IN", userInfo.getSlanguagefilename())
						+ "'||' '||s.ssectionname ||','||'"
						+ commonFunction.getMultilingualMessage(Enumeration.TransAction.BATCHINITIATED.getTransAction(),
								userInfo.getSlanguagefilename())
						+ "' as sremarks , "
						// +
						// "'"+commonFunction.getMultilingualMessage(Enumeration.TransAction.TESTINITIATED.getTransAction(),
						// userInfo.getSlanguagefilename())+"'||' '||tgt.stestsynonym ||'['||
						// rt.ntestrepeatno || ']'||'['|| rt.ntestretestno ||']' || s.ssectionname
						// ||'"+batchID+"' as sremarks , "
						+ "	" + userInfo.getNtranssitecode() + " as nsitecode,"
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " as nstatus "
						+ "	from batchmaster bm,batchsample bs,registrationtest rt,testgrouptest tgt,section s,registrationarno ra,registrationsamplearno rsa "
						+ "	where ra.npreregno =bs.npreregno "
						+ "	and rsa.ntransactionsamplecode =bs.ntransactionsamplecode "
						+ "	and bm.nbatchmastercode = bs.nbatchmastercode " + "	and bs.npreregno = rt.npreregno "
						+ "	and bs.ntransactionsamplecode = rt.ntransactionsamplecode "
						+ "	and bs.ntransactiontestcode= rt.ntransactiontestcode "
						+ "	and tgt.ntestgrouptestcode = rt.ntestgrouptestcode " + "	and tgt.ntestcode = rt.ntestcode "
						+ "	and rt.nsectioncode = s.nsectioncode " + "	and bm.nbatchmastercode in (" + nbatchMasterCode
						+ ") " + "	and bm.nsitecode =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ra.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "	and rt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "	and bm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and bs.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "	and tgt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and s.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
			}

//				final String insertCustodyQuery = "insert into chaincustody (nchaincustodycode,npreregno,"
//								+ " ntransactionsamplecode, ntransactionstatus,nusercode, "
//								+ " dtransactiondate,ntztransactiondate,noffsetdtransactiondate,"
//								+ " sarno, ssamplearno,sremarks, nsitecode,nstatus)"
//				                + " (select ("+nchainCustodyCode+") + rank() over(order by rar.npreregno, rsar.ntransactionsamplecode) nchaincustodycode,"
//				                + " rar.npreregno,  rsar.ntransactionsamplecode, " 
//				                + Enumeration.TransactionStatus.REGISTERED.gettransactionstatus() + " ntransactionstatus ," 
//				                + userInfo.getNdeputyusercode() + ",'"
//				                + getCurrentDateTime(userInfo)+ "' as dtransactiondate, "
//				                + userInfo.getNtimezonecode() + " as ntztransactiondate,"
//				                + getCurrentDateTimeOffset(userInfo.getStimezoneid())+" as noffsetdtransactiondate,"
//				                + " rar.sarno sarno, rsar.ssamplearno ssamplearno,'" 
//				                + Enumeration.TransAction.REGISTERED.getTransAction() + "' as sremarks,"
//				                + userInfo.getNtranssitecode() +" as nsitecode,"
//				  		        + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" as nstatus "
//				                + " from registrationarno rar, registrationsamplearno rsar "
//				                + " where rar.npreregno = rsar.npreregno and rar.nsitecode=rsar.nsitecode "
//				                + " and rar.nsitecode=" +userInfo.getNtranssitecode()
//				                + " and rar.nstatus=rsar.nstatus "
//				                + " and rar.nstatus= " +Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				                + " and rar.npreregno in (" + sPreRegNo + "))";
			jdbcTemplate.execute(insertCustodyQuery);
		}

		
		public ResponseEntity<Object> getRegistrationTestAudit(Map<String, Object> inputMap, UserInfo userInfo)
				throws Exception {
			Map<String, Object> objMap = new HashMap<String, Object>();
//			boolean subSample = false;
//			if (inputMap.containsKey("nneedsubsample")) {
//				subSample = (boolean) inputMap.get("nneedsubsample");
//			}
			
			final String query ="select ssettingvalue from settings where nsettingcode ="+Enumeration.Settings.UPDATING_ANALYSER.getNsettingcode()+ "";
			final Settings objSettings = (Settings) jdbcUtilityFunction.queryForObject(query, Settings.class, jdbcTemplate);	
			boolean updateAnalyst = false;
			if(objSettings !=null) {
				updateAnalyst = Integer.valueOf(objSettings.getSsettingvalue())==Enumeration.TransactionStatus.YES.gettransactionstatus() ? true:false;

			}	

			final String query1 = "select * from fn_registrationtestget('" + inputMap.get("npreregno") + "'::text," + "'"
					+ inputMap.get("ntransactionsamplecode") + "'::text" + ",'" + inputMap.get("ntransactiontestcode")
					+ "'::text," + "" + inputMap.get("ntype")
					// +","+ userInfo.getNtranssitecode()
					+ ",'" + userInfo.getSlanguagetypecode() + "','"
					+ commonFunction.getMultilingualMessage("IDS_REGNO", userInfo.getSlanguagefilename()) + "'" + ",'"
					+ commonFunction.getMultilingualMessage("IDS_SUBREGNO", userInfo.getSlanguagefilename()) + "',"+ updateAnalyst +")";
			System.out.println("fn_registrationtestget:" + query1);

			final String lstData2 = jdbcTemplate.queryForObject(query1, String.class);
			if (lstData2 != null) {
				
				List<Map<String, Object>> lstData = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(
						lstData2, userInfo, true, (int) inputMap.get("ndesigntemplatemappingcode"), "test");
				objMap.put("RegistrationGetTest", lstData);
			} else {
				objMap.put("selectedTest", Arrays.asList());
				objMap.put("RegistrationParameter", Arrays.asList());
				objMap.put("activeTestTab", "IDS_PARAMETERRESULTS");
				objMap.put("RegistrationGetTest", Arrays.asList());

			}

			return new ResponseEntity<>(objMap, HttpStatus.OK);

		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Map<String, Object> getDynamicRegistration(Map<String, Object> inputMap, UserInfo userInfo)
				throws Exception {
			Map<String, Object> objMap = new HashMap();

			short nfilterstatus;
			if (inputMap.get("nfilterstatus").getClass().getTypeName().equals("java.lang.Integer")) {
				nfilterstatus = ((Integer) inputMap.get("nfilterstatus")).shortValue();
			} else {
				nfilterstatus = (short) inputMap.get("nfilterstatus");
			}

			final String registrationMultilingual = commonFunction.getMultilingualMessage("IDS_REGNO",
					userInfo.getSlanguagefilename());

			final String query1 = "select * from fn_registrationsampleget(''::text," + "''::text,"
					+ inputMap.get("nregtypecode") + "," + inputMap.get("nregsubtypecode") + "," + "" + nfilterstatus
					+ "::integer," + userInfo.getNtranssitecode() + ",'" + inputMap.get("npreregno") + "'::text," + "'"
					+ userInfo.getSlanguagetypecode() + "'::text," + "" + inputMap.get("ndesigntemplatemappingcode") + ",'"
					+ registrationMultilingual + "'::text," + inputMap.get("napproveconfversioncode") + ")";

			System.out.println("Sample Start========?" + LocalDateTime.now());

			String lstData1 = jdbcTemplate.queryForObject(query1, String.class);
			System.out.println("Sample end========?" + LocalDateTime.now());

			List<Map<String, Object>> lstData = new ArrayList();

			if (lstData1 != null) {
				lstData = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(lstData1, userInfo, true,
						(int) inputMap.get("ndesigntemplatemappingcode"), "sample");

				System.out.println("Sample Size" + lstData.size());

				objMap.put("RegistrationGetSample", lstData);

				String spreregno = "";
				if (!inputMap.containsKey("ntype")) {
					spreregno = String.valueOf(lstData.get(lstData.size() - 1).get("npreregno"));
					inputMap.put("npreregno", spreregno);
					objMap.put("selectedSample", Arrays.asList(lstData.get(lstData.size() - 1)));
				} else {
					objMap.put("selectedSample", lstData);
				}

				Map<String, Object> map = (Map<String, Object>) getRegistrationSubSample(inputMap, userInfo).getBody();
				objMap.putAll(map);

				/*
				 * objMap.putAll(getActiveSampleTabData((String) inputMap.get("npreregno"), "0",
				 * (String) inputMap.get("activeSampleTab"), userInfo));
				 */
				objMap.put("activeSampleTab", inputMap.get("activeSampleTab"));
			} else {
				objMap.put("selectedSample", lstData);
				objMap.put("RegistrationGetSample", lstData);
				objMap.put("selectedSubSample", lstData);
				objMap.put("RegistrationGetSubSample", lstData);
				objMap.put("RegistrationGetTest", lstData);
				objMap.put("selectedTest", lstData);
				objMap.put("RegistrationParameter", lstData);
				objMap.put("activeSampleTab", "IDS_SAMPLEATTACHMENTS");
				objMap.put("activeSubSampleTab", "IDS_SUBSAMPLEATTACHMENTS");
				objMap.put("activeTestTab", "IDS_PARAMETERRESULTS");
			}
			return objMap;

		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public ResponseEntity<Object> getRegistrationSubSample(Map<String, Object> inputMap, UserInfo userInfo)
				throws Exception {
			Map<String, Object> objMap = new HashMap();

			boolean subSample = (boolean) inputMap.get("nneedsubsample");

			final String query1 = "select * from fn_registrationsubsampleget('" + inputMap.get("npreregno") + "'::text,"
					+ "'" + inputMap.get("ntransactionsamplecode") + "'::text" + "," + inputMap.get("ntype")
					// +","+ userInfo.getNtranssitecode()
					+ ",'" + userInfo.getSlanguagetypecode() + "','"
					+ commonFunction.getMultilingualMessage("IDS_REGNO", userInfo.getSlanguagefilename()) + "'" + ",'"
					+ commonFunction.getMultilingualMessage("IDS_SAMPLENO", userInfo.getSlanguagefilename()) + "')";

			System.out.println("sub sample Start========?" + LocalDateTime.now());

			final String lstData1 = jdbcTemplate.queryForObject(query1, String.class);
			System.out.println("sub sample end========?" + LocalDateTime.now());

			if (lstData1 != null) {

				List<Map<String, Object>> lstData = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(
						lstData1, userInfo, true, (int) inputMap.get("ndesigntemplatemappingcode"), "subsample");

				objMap.put("RegistrationGetSubSample", lstData);

				/*
				 * objMap.putAll(getActiveSampleTabData((String) inputMap.get("npreregno"), "0",
				 * (String) inputMap.get("activeSampleTab"), userInfo));
				 */

				objMap.put("activeSampleTab", inputMap.get("activeSampleTab"));

				if (!subSample || (int) inputMap.get("checkBoxOperation") == 3
						|| (int) inputMap.get("checkBoxOperation") == 7) {
					String stransactionsampleno = "";
					// if (!inputMap.containsKey("ntype")) {
					stransactionsampleno = String.valueOf(lstData.get(lstData.size() - 1).get("ntransactionsamplecode"));
					inputMap.put("ntransactionsamplecode", stransactionsampleno);
					objMap.put("selectedSubSample", Arrays.asList(lstData.get(lstData.size() - 1)));

					/*
					 * } else if ((int) inputMap.get("ntype") == 2) { stransactionsampleno =
					 * lstData.stream().map(x -> String.valueOf(x.get("ntransactionsamplecode")))
					 * .collect(Collectors.joining(",")); inputMap.put("ntransactionsamplecode",
					 * stransactionsampleno); objMap.put("selectedSubSample", lstData); } else if
					 * ((int) inputMap.get("ntype") == 3) { String ntransactionsamplecode = (String)
					 * inputMap.get("ntransactionsamplecode"); stransactionsampleno =
					 * ntransactionsamplecode; List<String> myList = new
					 * ArrayList<String>(Arrays.asList(ntransactionsamplecode.split(","))); lstData
					 * = lstData.stream() .filter(x -> myList.stream() .anyMatch(y ->
					 * y.equals(String.valueOf(x.get("ntransactionsamplecode")))))
					 * .collect(Collectors.toList()); inputMap.put("ntype", 4);
					 * objMap.put("selectedSubSample", lstData); } else if ((int)
					 * inputMap.get("ntype") == 5) { stransactionsampleno = (String)
					 * inputMap.get("ntransactionsamplecode"); objMap.put("selectedSubSample",
					 * lstData); }
					 * 
					 * else { stransactionsampleno = String .valueOf(lstData.get(lstData.size() -
					 * 1).get("ntransactionsamplecode")); objMap.put("selectedSubSample",
					 * Arrays.asList(lstData.get(lstData.size() - 1)));
					 * inputMap.put("ntransactionsamplecode", stransactionsampleno);
					 * 
					 * }
					 */

					/*
					 * objMap.putAll(getActiveSubSampleTabData(stransactionsampleno, (String)
					 * inputMap.get("activeSubSampleTab"), userInfo));
					 */

					objMap.put("activeSubSampleTab", inputMap.get("activeSubSampleTab"));

					Map<String, Object> map = (Map<String, Object>) getRegistrationTest(inputMap, userInfo).getBody();
					objMap.putAll(map);
				}

			} else {
				objMap.put("selectedSubSample", Arrays.asList());
				if (!subSample || (int) inputMap.get("checkBoxOperation") == 3) {
					objMap.put("selectedTest", Arrays.asList());
					objMap.put("RegistrationGetTest", Arrays.asList());
					objMap.put("activeSubSampleTab", "IDS_SUBSAMPLEATTACHMENTS");
					objMap.put("activeTestTab", "IDS_PARAMETERRESULTS");
				}
			}

			return new ResponseEntity<>(objMap, HttpStatus.OK);

		}

		// Working Get start
//		@SuppressWarnings({ "unchecked", "rawtypes" })
		public ResponseEntity<Object> getRegistrationTest(Map<String, Object> inputMap, UserInfo userInfo)
				throws Exception {
			Map<String, Object> objMap = new HashMap<String, Object>();
			// String stransactioncode = "";
//			boolean subSample = false;
//			if (inputMap.containsKey("nneedsubsample")) {
//				subSample = (boolean) inputMap.get("nneedsubsample");
//			}

//				if (inputMap.containsKey("ntransactionsamplecode") && inputMap.get("ntransactionsamplecode") != "")
//					stransactioncode = " and rth1.ntransactionsamplecode in (" + inputMap.get("ntransactionsamplecode") + ") ";

//				String stransactiontestcode = "";
//				if (inputMap.containsKey("ntype")) {
//					if ((int) inputMap.get("ntype") == 3) {
//						stransactiontestcode = " and jrt.ntransactiontestcode in (" + inputMap.get("ntransactiontestcode")
//								+ ")";
//					}
			//
//				}

			// comment by pravinth //this query used to capture new record for audit
//				String query2 = "  select json_agg(a.jsonuidata) from (select jrt.jsondata||jrs.jsonuidata||json_build_object('sarno',"
//						+ "  case when jra.sarno='-' then concat(cast(jra.npreregno as text),' (Reg No)') "
//						+ "  else jra.sarno end)::jsonb||json_build_object('stransdisplaystatus',ts.jsondata->'stransdisplaystatus'->>'"
//						+ userInfo.getSlanguagetypecode() + "')::jsonb||"
//						+ "  json_build_object('ssamplearno',case when jsa.ssamplearno='-' then concat(cast(jsa.ntransactionsamplecode as text),"
//						+ " ' (Sample No)') else jsa.ssamplearno end)::jsonb||json_build_object('ntransactionstatus',jrth.ntransactionstatus)::jsonb||"
//						+ " json_build_object('scolorhexcode',cm.scolorhexcode)::jsonb as jsonuidata"
//						+ " from registrationtesthistory jrth, registrationtest jrt, registrationsamplearno jsa,"
//						+ " registrationsample jrs, registrationarno jra, registration jr , transactionstatus ts,formwisestatuscolor fsc,colormaster cm "
//						+ " where  jrth.ntransactiontestcode=jrt.ntransactiontestcode "
//						+ " and jra.npreregno = jrt.npreregno  and jrt.npreregno=jr.npreregno and jrth.npreregno=jr.npreregno"
//						+ " and jsa.ntransactionsamplecode = jrt.ntransactionsamplecode  "
//						+ " and jrth.ntesthistorycode in ( select max(rth1.ntesthistorycode) from registrationtesthistory rth1"
//						+ " where rth1.npreregno in (" + inputMap.get("npreregno") + ") " + " and rth1.nstatus="
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+ "group by rth1.npreregno,rth1.ntransactionsamplecode,rth1.ntransactiontestcode)" + " and jsa.nstatus="
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "and jr.nstatus="
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "and jra.nstatus="
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "and jrt.nstatus="
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+ " and jrs.ntransactionsamplecode=jrt.ntransactionsamplecode  and fsc.ntranscode = jrth.ntransactionstatus "
//						+ " and fsc.ncolorcode = cm.ncolorcode and fsc.nformcode = " + userInfo.getNformcode()
//						+ " and fsc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+ " and jrth.ntransactionstatus = ts.ntranscode   " + " and ts.nstatus ="
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and jrs.npreregno in ("
//						+ inputMap.get("npreregno") + ") " + stransactiontestcode
//						+ "  order by jrs.npreregno,jrt.ntransactiontestcode asc)a";
			//
//				// List<RegistrationTest> lstData = jdbcTemplate.query(query, new
//				// RegistrationTest());
//				System.out.println("test Start========?" + LocalDateTime.now());
//				final String lstDatatests = jdbcTemplate.queryForObject(query2, String.class);
//				System.out.println("test end========?" + LocalDateTime.now());
//				if (lstDatatests != null) {
//					List<Map<String, Object>> lstDataTest = (List<Map<String, Object>>) getSiteLocalTimeFromUTCForDynamicTemplate(
//							lstDatatests, userInfo, true, (int) inputMap.get("ndesigntemplatemappingcode"), "test");
//					objMap.put("RegistrationGetTestNew", lstDataTest);
//				}
//				String query = "  select json_agg(a.jsonuidata) from (select jrt.jsondata||json_build_object('sarno',"
//						+ "  case when jra.sarno='-' then concat(cast(jra.npreregno as text),' (Reg No)') "
//						+ "  else jra.sarno end)::jsonb||json_build_object('stransdisplaystatus',ts.jsondata->'stransdisplaystatus'->>'"
//						+ userInfo.getSlanguagetypecode() + "')::jsonb||"
//						+ "  json_build_object('ssamplearno',case when jsa.ssamplearno='-' then concat(cast(jsa.ntransactionsamplecode as text),"
//						+ " ' (Sample No)') else jsa.ssamplearno end)::jsonb||json_build_object('ntransactionstatus',jrth.ntransactionstatus)::jsonb"
//						+ " || json_build_object('scolorhexcode',cm.scolorhexcode)::jsonb"
//					//	+ " || json_build_object('npreregno',jrt.npreregno)::jsonb"
//					//	+ " || json_build_object('ntransactionsamplecode',jrt.ntransactionsamplecode)::jsonb"
//					//	+ " || json_build_object('ntransactiontestcode',jrt.ntransactiontestcode)::jsonb"
//						+ " as jsonuidata"
//						+ " from registrationtesthistory jrth, registrationtest jrt, registrationsamplearno jsa,"
//						+ "   registrationarno jra , transactionstatus ts,formwisestatuscolor fsc,colormaster cm "
//						+ " where  jrth.ntesthistorycode in ( select max(rth1.ntesthistorycode) from registrationtesthistory rth1"
//						+ " where rth1.npreregno in (" + inputMap.get("npreregno") + ") " + stransactioncode
//						+ " and rth1.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+ " group by rth1.npreregno,rth1.ntransactionsamplecode,rth1.ntransactiontestcode) "
//						+ " and jrth.ntransactiontestcode=jrt.ntransactiontestcode   " + " and jra.npreregno = jrt.npreregno  "
//						+ " and jrth.ntransactionsamplecode = jrt.ntransactionsamplecode  "
//						+ " and jsa.ntransactionsamplecode = jrt.ntransactionsamplecode  "
//						+ " and jrth.npreregno=jra.npreregno " + " and jrth.npreregno=jrt.npreregno "
//						+ " and jrth.ntransactionsamplecode = jsa.ntransactionsamplecode  "
//						+ " and jrth.npreregno=jsa.npreregno " + " and jsa.nstatus="
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and jra.nstatus="
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and jrt.nstatus="
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and fsc.nformcode = "
//						+ userInfo.getNformcode() + " and fsc.ntranscode = jrth.ntransactionstatus "
//						+ " and fsc.ncolorcode = cm.ncolorcode " + " and fsc.nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+ " and jrth.ntransactionstatus = ts.ntranscode   and ts.nstatus ="
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + stransactiontestcode
//						+ "  order by jrt.npreregno,jrt.ntransactiontestcode asc)a";

			final String query ="select ssettingvalue from settings where nsettingcode ="+Enumeration.Settings.UPDATING_ANALYSER.getNsettingcode()+ "";
			final Settings objSettings = (Settings) jdbcUtilityFunction.queryForObject(query, Settings.class, jdbcTemplate);	
			boolean updateAnalyst = false;
			if(objSettings !=null) {
				updateAnalyst = Integer.valueOf(objSettings.getSsettingvalue())==Enumeration.TransactionStatus.YES.gettransactionstatus() ? true:false;

			}	

			final String query1 = "select * from fn_registrationtestget('" + inputMap.get("npreregno") + "'::text," + "'"
					+ inputMap.get("ntransactionsamplecode") + "'::text" + ",'" + inputMap.get("ntransactiontestcode")
					+ "'::text," + "" + inputMap.get("ntype")// + "," + userInfo.getNtranssitecode()
					+ ",'" + userInfo.getSlanguagetypecode() + "','"
					+ commonFunction.getMultilingualMessage("IDS_REGNO", userInfo.getSlanguagefilename()) + "'" + ",'"
					+ commonFunction.getMultilingualMessage("IDS_SUBREGNO", userInfo.getSlanguagefilename()) + "',"+ updateAnalyst +")";

			System.out.println("test Start========?" + LocalDateTime.now());

			final String lstData2 = jdbcTemplate.queryForObject(query1, String.class);
			System.out.println("test end========?" + LocalDateTime.now());

			if (lstData2 != null) {

				ObjectMapper obj = new ObjectMapper();
				List<Map<String, Object>> lstData = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(
						lstData2, userInfo, true, (int) inputMap.get("ndesigntemplatemappingcode"), "test");
				objMap.put("RegistrationGetTest", lstData);
				String Stransactiontestcode = "";
				if (inputMap.containsKey("ntype")) {
					if ((int) inputMap.get("ntype") == 1) {
						List<Map<String, Object>> lstData1 = obj.convertValue(inputMap.get("registrationtest"),
								new TypeReference<List<Map<String, Object>>>() {
								});
						List<Map<String, Object>> filteredList = lstData.stream()
								.filter(empl -> lstData1.stream()
										.anyMatch(dept -> String.valueOf(dept.get("ntransactiontestcode"))
												.equals(String.valueOf(empl.get("ntransactiontestcode")))))
								.collect(Collectors.toList());

						Stransactiontestcode = filteredList.stream().map(x -> String.valueOf(x.get("ntransactiontestcode")))
								.collect(Collectors.joining(","));
						objMap.put("selectedTest", Arrays.asList(lstData.get(lstData.size() - 1)));
					} else if ((int) inputMap.get("ntype") == 2) {

						Stransactiontestcode = lstData.stream().map(x -> String.valueOf(x.get("ntransactiontestcode")))
								.collect(Collectors.joining(","));
						objMap.put("selectedTest", lstData);
					}

					else if ((int) inputMap.get("ntype") == 3) {
						Stransactiontestcode = (String) inputMap.get("ntransactiontestcode");
						objMap.put("selectedTest", lstData);
					} else if ((int) inputMap.get("ntype") == 4) {
						Stransactiontestcode = (String) inputMap.get("ntransactiontestcode");
						List<String> myList = new ArrayList<String>(Arrays.asList(Stransactiontestcode.split(",")));
						lstData = lstData.stream().filter(
								x -> myList.stream().anyMatch(y -> y.equals(String.valueOf(x.get("ntransactiontestcode")))))
								.collect(Collectors.toList());
						objMap.put("selectedTest", lstData);
					} else if ((int) inputMap.get("ntype") == 5) {
						Stransactiontestcode = String.valueOf(lstData.get(lstData.size() - 1).get("ntransactiontestcode"));
						objMap.put("selectedTest", Arrays.asList(lstData.get(lstData.size() - 1)));
					}

					else {
						Stransactiontestcode = String.valueOf(lstData.get(lstData.size() - 1).get("ntransactiontestcode"));
						objMap.put("selectedTest", Arrays.asList(lstData.get(lstData.size() - 1)));
					}
				} else {
					Stransactiontestcode = String.valueOf(lstData.get(lstData.size() - 1).get("ntransactiontestcode"));
					if ((int) inputMap.get("checkBoxOperation") == 7) {
						List<String> subSampleArray = Arrays
								.asList(((String) inputMap.get("ntransactionsamplecode")).split(","));
						for (Map<String, Object> map : lstData) {
							if ((int) map.get("ntransactionsamplecode") == Integer.parseInt(subSampleArray.get(0))) {
								Stransactiontestcode = map.get("ntransactiontestcode").toString();
								// break;
							}
						}
						inputMap.put("ntransactiontestcode", Stransactiontestcode);
					}

					objMap.put("selectedTest", Arrays.asList(lstData.get(lstData.size() - 1)));
				}

				if (!inputMap.containsKey("withoutgetparameter")) {
					/*
					 * objMap.putAll(getActiveTestTabData(Stransactiontestcode, (String)
					 * inputMap.get("npreregno"), (String) inputMap.get("activeTestTab"),
					 * userInfo));
					 */
					inputMap.put("activeTestTab", (String) inputMap.get("activeTestTab"));
					/*
					 * if (subSample) { objMap.putAll(getActiveSubSampleTabData((String)
					 * inputMap.get("ntransactionsamplecode"), (String)
					 * inputMap.get("activeSubSampleTab"), userInfo)); }
					 */

				}

			} else {
				objMap.put("selectedTest", Arrays.asList());
				objMap.put("RegistrationParameter", Arrays.asList());
				objMap.put("activeTestTab", "IDS_PARAMETERRESULTS");
				objMap.put("RegistrationGetTest", Arrays.asList());

			}

			return new ResponseEntity<>(objMap, HttpStatus.OK);

		}

		private ResponseEntity<Object> getTestDetails(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
			Map<String, Object> returnMap = new HashMap<>();
			int ntestcode = (int) inputMap.get("ntestcode");
			String sQuery = " select max(tm.ntestcode)ntestcode,max(tm.stestname)stestname,tgtp.ntestgrouptestcode,"
					+ " max(tgt.ntestgrouptestcode) ntestgrouptestcode,"
					+ " max(tgt.nmethodcode) nmethodcode ,max(tgt.nspecsampletypecode) nspecsampletypecode,"
					+ " max(tgtp.nchecklistversioncode)nchecklistversioncode, max(tgsst.nspecsampletypecode) nspecsampletypecode,"
					+ " max(tgsst.nallottedspeccode) nallottedspeccode, max(tgsst.ncomponentcode) ncomponentcode "
					+ " from testmaster tm,testgrouptest tgt,"
					+ " testgrouptestparameter tgtp,testgroupspecsampletype tgsst" + " where "
					+ " tm.ntestcode=tgt.ntestcode " + " and tgtp.ntestgrouptestcode=tgt.ntestgrouptestcode "
					+ " and tgsst.nspecsampletypecode=tgt.nspecsampletypecode " + " and tm.ntestcode= " + ntestcode + ""
					+ " and tgtp.nsitecode=" + userInfo.getNmastersitecode() + "" + " and tgsst.nsitecode="
					+ userInfo.getNmastersitecode() + "" + " and tm.nsitecode=" + userInfo.getNmastersitecode() + ""
					+ " and tgt.nsitecode=" + userInfo.getNmastersitecode() + "" + " and tgtp.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and tgsst.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and tm.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and tgt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " group by tgtp.ntestgrouptestcode ";

			List<RegistrationTest> lsttestvalues = jdbcTemplate.query(sQuery, new RegistrationTest());

			if (!lsttestvalues.isEmpty()) {
				returnMap.put("selectedTestDetails", lsttestvalues.get(0));
			}
			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		}

		
		@Override
		public ResponseEntity<Object> getSpecificationDetails(Map<String, Object> inputMap, UserInfo userInfo)
				throws Exception {

			final Map<String, Object> objMap = new HashMap<String, Object>();

			List<TreeTemplateManipulation> lstMaster = new ArrayList<TreeTemplateManipulation>();
			int ntreetemplatemanipulationcode = 0;
			int ncategorybasedflow = (int) inputMap.get("ncategorybasedflow");
			int nsampletypecode = Enumeration.SampleType.MATERIAL.getType();
			int nmaterialcatcode = (int) inputMap.get("nmaterialcatcode");
			int nmaterialcode = (int) inputMap.get("nmaterialcode");
			int ntestcode = (int) inputMap.get("ntestcode");
            //ALPD-5873  for Default Spec Work. Added by Abdul on 04-06-2025
			final int ntestGroupSpecRequired = inputMap.containsKey("ntestgroupspecrequired") ? (int) inputMap.get("ntestgroupspecrequired") : Enumeration.TransactionStatus.YES.gettransactionstatus();
			String sconditionQuery = "";

				
			if (ncategorybasedflow == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				sconditionQuery = " and ttm.nproductcatcode =" + nmaterialcatcode + "" + " and ttm.nproductcode ="
						+ Enumeration.TransactionStatus.NA.gettransactionstatus() + "";
			} else {
				sconditionQuery = "and ttm.nproductcatcode =" + nmaterialcatcode + " " + " and ttm.nproductcode ="
						+ nmaterialcode + "";
			}
			
			

			final String sApprovalStatusQuery = "select acr.napprovalstatuscode from approvalconfigrole acr,"
					+ " approvalconfigversion acv,approvalconfig ac"
					+ " where acv.napproveconfversioncode = acr.napproveconfversioncode "
					+ " and ac.napprovalconfigcode = acr.napprovalconfigcode" + " and ac.napprovalsubtypecode = "
					+ Enumeration.ApprovalSubType.STUDYPLANAPPROVAL.getnsubtype() + " and acr.nlevelno = 1 "
					+ " and acr.nsitecode=" + userInfo.getNmastersitecode() + "" + " and acv.nsitecode="
					+ userInfo.getNmastersitecode() + "" + " and acr.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and acv.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and ac.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and  acv.ntransactionstatus not in (" + Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
					+ ") " + " group by acr.napprovalstatuscode";
			List<ApprovalConfigRole> lstgetApprovalStatus = jdbcTemplate.query(sApprovalStatusQuery,
					new ApprovalConfigRole());

			String snapprovalstatus = stringUtilityFunction.fnDynamicListToString(lstgetApprovalStatus, "getNapprovalstatuscode");

			String sTreeQuery ="";
			if (snapprovalstatus != null && snapprovalstatus != "") {
				 sTreeQuery = ";with RECURSIVE tree (sleveldescription, nparentnode, ntemplatemanipulationcode)"
						+ " as (" + " select ttm.sleveldescription, ttm.nparentnode, ttm.ntemplatemanipulationcode "
						+ " from treetemplatemanipulation ttm,testgroupspecification tgs , "
						+ " testgroupspecsampletype tgss,testgrouptest tgt"
						+ " where ttm.ntemplatemanipulationcode = tgs.ntemplatemanipulationcode" + " "
						+ " and tgs.nallottedspeccode=tgss.nallottedspeccode"
						+ "	and tgt.nspecsampletypecode=tgss.nspecsampletypecode"
						+ "	and tgt.nspecsampletypecode =any(select tgt.nspecsampletypecode from testgrouptest tgt,"
						+ " testgroupspecsampletype tgss" + "	where tgt.nspecsampletypecode=tgss.nspecsampletypecode "
						+ " and tgt.ntestcode=" + ntestcode + " and tgt.nsitecode=" + userInfo.getNmastersitecode() + ""
						+ " and tgss.nsitecode=" + userInfo.getNmastersitecode() + "" + " and tgss.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and tgt.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")" + " and tgs.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and ttm.nsitecode="
						+ userInfo.getNmastersitecode() + "" + " and tgs.nsitecode=" + userInfo.getNmastersitecode() + ""
						+ " and ttm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and ttm.nsampletypecode = " + nsampletypecode + " " + sconditionQuery + ""
						+ " and tgs.napprovalstatus in (" + snapprovalstatus
						+ ") and tgs.dexpirydate>(select now() at time zone 'utc')" + " union all"
						+ " select ttm.sleveldescription, ttm.nparentnode, ttm.ntemplatemanipulationcode  "
						+ " from treetemplatemanipulation ttm, tree t where nsampletypecode = " + nsampletypecode
						+ " " + sconditionQuery + ""
						// + " and nprojectmastercode = " + nprojectMasterCode + " "
						+ " and ttm.ntemplatemanipulationcode = t.nparentnode" + " and ttm.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " ) select * from tree group by ntemplatemanipulationcode, nparentnode, sleveldescription ";
				
				 //ALPD-5873 Added by Abdul for Default Spec
				  if (ntestGroupSpecRequired == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
						 sTreeQuery =  sTreeQuery + " UNION ALL "+ "Select sleveldescription, nparentnode, ntemplatemanipulationcode from treetemplatemanipulation where nproductcode = "+ Enumeration.TransactionStatus.NA.gettransactionstatus()+" "
										+ "and nproductcatcode="+Enumeration.Default.DEFAULTSPEC.getDefault()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										;						
					}
				  sTreeQuery =  sTreeQuery + " ORDER BY ntemplatemanipulationcode ";
				
				lstMaster = jdbcTemplate.query(sTreeQuery, new TreeTemplateManipulation());

				final Map<String, Object> treeMap = projectDAOSupport.getHierarchicalList(lstMaster, false, "", 0);
				ntreetemplatemanipulationcode = (int) treeMap.get("primarykey");
				objMap.putAll(treeMap);
				objMap.put("ntreetemplatemanipulationcode", ntreetemplatemanipulationcode);
				objMap.put("Specification", registrationDAO.getTestGroupSpecification(ntreetemplatemanipulationcode, 0, ntestGroupSpecRequired));
				objMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), true);
			} else {
				objMap.put("AgaramTree", Arrays.asList());
				objMap.put("OpenNodes", Arrays.asList());
				objMap.put("FocusKey", Arrays.asList());
				objMap.put("ActiveKey", null);
				objMap.put("Specification", null);
			}

			return new ResponseEntity<>(objMap, HttpStatus.OK);
		}

//		private Batchmaster getCheckDeleteValidation(int nbatchmastercode, UserInfo userInfo) throws Exception {
//
//			String sValidationQuery = "select * from batchmaster bm  where " + " and bm.nbatchmastercode = "
//					+ nbatchmastercode + " " + " and bm.nsitecode=" + userInfo.getNtranssitecode() + "" + " and bm.nstatus="
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
//
//			return (Batchmaster) jdbcUtilityFunction.queryForObject(sValidationQuery, Batchmaster.class,jdbcTemplate); 
//		}

		@SuppressWarnings("unchecked")
		@Override
		public ResponseEntity<Object> cancelIQCSampleAction(Map<String, Object> inputMap, UserInfo userInfo)
				throws Exception {
			// TODO Auto-generated method stub
			ObjectMapper objmap = new ObjectMapper();
			Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
			JSONObject actionType = new JSONObject();
			JSONObject jsonAuditObject = new JSONObject();
			int nbatchsampleiqccode = (int) inputMap.get("nbatchsampleiqccode");
			int nbatchmastercode = (int) inputMap.get("nbatchmastercode");
			//int ndesigntemplatemappingcode = (int) inputMap.get("ndesigntemplatemappingcode");
			// int batchMatSeqn = (int) inputMap.get("batchMatSeqn");
			int nmaterialinventtranscode = (int) inputMap.get("nmaterialinventtranscode");
			Map<String, Object> returnMap = new HashMap<>();
			//final Map<String, Object> jsonData = new HashMap<>();
			//final Map<String, Object> jsonUIData = new HashMap<>();

			final BatchsampleIqc batchiqcsample = (BatchsampleIqc) getActiveIQCSampleById(nbatchsampleiqccode, userInfo);
			BatchsampleIqc objbatchiqcsample = objmap.convertValue(inputMap.get("iqcSample"),
					new TypeReference<BatchsampleIqc>() {
					});

			if (batchiqcsample == null) {
				// status code:417
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {

				final BatchHistory batchhist = (BatchHistory) getCheckValidation(nbatchmastercode, userInfo);

				if (batchhist.getNtransactionstatus() == Enumeration.TransactionStatus.INITIATED.gettransactionstatus()
						|| batchhist.getNtransactionstatus() == Enumeration.TransactionStatus.COMPLETED
								.gettransactionstatus()) {

					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.SELECTDRAFTVERSION.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);

				} else {

					final String updateQueryString = "update batchsampleiqc set nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ""
							+ " where nbatchsampleiqccode=" + nbatchsampleiqccode + "";

					jdbcTemplate.execute(updateQueryString);

					final String updateMatQueryString = "update materialinventorytransaction set nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ""
							+ " where nmaterialinventtranscode=" + nmaterialinventtranscode + "";

					jdbcTemplate.execute(updateMatQueryString);

					final ResponseEntity<Object> IQCSampleResponseEntity = getBatchIqcSampleAction(inputMap, userInfo);
					returnMap = (Map<String, Object>) IQCSampleResponseEntity.getBody();
					List<BatchsampleIqc> lstbatchsampleiqc = new ArrayList<>();
					// lstbatchsampleiqc.add((Batchsampleiqc) returnMap.get("selectedIqcsample"));
					lstbatchsampleiqc.add(objbatchiqcsample);
					auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
					auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
					auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
					actionType.put("batchsampleiqc", "IDS_DELETEBATCHIQCSAMPLE");
					jsonAuditObject.put("batchsampleiqc", lstbatchsampleiqc);
					auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);
				}

			}

			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		}

		
		private Map<String, Object> getCheckTechnique(int ntestcode, UserInfo userInfo) throws Exception {
			Map<String, Object> map = new HashMap<>();
			Boolean ischeckedvalidation = false;
			LOGGER.info("ischeckedvalidation:"+ ischeckedvalidation);			
			//TestMaster stest = new TestMaster();

			String isTesttechnique = " select * from testmaster tm " + " where tm.ntrainingneed = "
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " " + " and tm.ntestcode=" + ntestcode + ""
					+ " and tm.nsitecode=" + userInfo.getNmastersitecode() + "" + " and tm.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

			List<TestMaster> lstTestTechnique = (List<TestMaster>) jdbcTemplate.query(isTesttechnique, new TestMaster());

			if (lstTestTechnique.size() > 0) {

				String istechnique = " select tt.ntechniquecode,tt.ntestcode from testmaster tm ,techniquetest tt "
						+ " where tm.ntrainingneed = " + Enumeration.TransactionStatus.YES.gettransactionstatus() + " "
						+ " and tm.ntestcode=" + ntestcode + "" + " and tm.ntestcode = tt.ntestcode " + " and tm.nsitecode="
						+ userInfo.getNmastersitecode() + "" + " and tt.nsitecode=" + userInfo.getNmastersitecode() + ""
						+ " and tm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " and tt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

				List<TrainingCertification> lstTechnique = jdbcTemplate.query(istechnique,
						new TrainingCertification());

				if (lstTechnique.size() > 0) {
					final String stechniquecode = lstTechnique.stream()
							.map(objtechniquecode -> String.valueOf(objtechniquecode.getNtechniquecode()))
							.collect(Collectors.joining(","));

					String scheckCompetent = " select tc.ntrainingcode,tp.nusercode,tc.ntechniquecode,tp.ncompetencystatus,ntrainingexpiryneed,dtrainingexpirydate"
							+ " from trainingcertification tc ,"
							+ " trainingparticipants tp  where tc.ntrainingcode=tp.ntrainingcode "
							+ " and tc.ntechniquecode in  (" + stechniquecode + ") " + " and tc.ntransactionstatus = "
							+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() + " " + " and tp.nusercode = "
							+ userInfo.getNusercode() + "" + " and tp.ncompetencystatus="
							+ Enumeration.TransactionStatus.YES.gettransactionstatus() + "" + " and tc.nsitecode = "
							+ userInfo.getNmastersitecode() + "" + " and tp.nsitecode = " + userInfo.getNmastersitecode()
							+ "" + " and tc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
							+ " and tp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

					List<TrainingCertification> lstTrainingCertification = jdbcTemplate.query(scheckCompetent,
							new TrainingCertification());
					if (lstTrainingCertification.size() > 0) {
						List<TrainingCertification> sDataNoTraining = lstTrainingCertification.stream().filter(
								m -> m.getNtrainingexpiryneed() == Enumeration.TransactionStatus.NO.gettransactionstatus())
								.collect(Collectors.toList());

//			 		final String strainingCert = sDataTest.stream().map(objNtrainingexpiryneed -> String.valueOf(objNtrainingexpiryneed.getNtechniquecode()))
//			                .collect(Collectors.joining(","));

						if (sDataNoTraining.size() == 0) {
							List<TrainingCertification> sDataTest = lstTrainingCertification.stream().filter(m -> m
									.getNtrainingexpiryneed() == Enumeration.TransactionStatus.YES.gettransactionstatus())
									.collect(Collectors.toList());

							final String strainingCert = sDataTest.stream().map(
									objNtrainingexpiryneed -> String.valueOf(objNtrainingexpiryneed.getNtechniquecode()))
									.collect(Collectors.joining(","));

							final String strainingCode = lstTrainingCertification.stream()
									.map(objNtrainingcode -> String.valueOf(objNtrainingcode.getNtrainingcode()))
									.collect(Collectors.joining(","));

							Map<String, Object> sCheckTrainingValidation = transactionDAOSupport.getUserBasedCerticationExpiry(strainingCert,
									userInfo, strainingCode);
							if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus()
									.equals(sCheckTrainingValidation.get("rtn"))) {
								ischeckedvalidation = true;
								map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
										Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
							} else {
								map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
										sCheckTrainingValidation.get("rtn"));
							}
						} else {
							ischeckedvalidation = true;
							map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
									Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
						}
					} else {
						ischeckedvalidation = false;
						map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
								Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
					}
				} else {
					ischeckedvalidation = false;
					map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), (commonFunction
							.getMultilingualMessage("IDS_TESTNOTADDEDINTECHNIQUE", userInfo.getSlanguagefilename())));
				}
			} else {
				ischeckedvalidation = true;
				map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
			}

			return map;
		}

		
		private Map<String, Object> getCheckCompleteRecordStatus(UserInfo userInfo, Boolean nneedsubsample,
				String stransactionStatus, String stransactiontestcode) throws Exception {
			Map<String, Object> map = new HashMap<>();
			String sSampleCheckQuery = " select case when " + nneedsubsample
					+ "=true then rsarno.ssamplearno else arno.sarno end samplearno,"
					+ " rth.ntesthistorycode,rth.ntransactionstatus"
					+ " from registrationarno arno ,registrationsamplearno rsarno, registrationsample rs,"
					+ " registration r,registrationtest rt,registrationtesthistory rth where rth.ntransactiontestcode=rt.ntransactiontestcode "
					+ " and r.npreregno = rt.npreregno and arno.npreregno = r.npreregno and rsarno.ntransactionsamplecode=rs.ntransactionsamplecode"
					+ " and rs.ntransactionsamplecode = rt.ntransactionsamplecode" + " and arno.nsitecode= "
					+ userInfo.getNtranssitecode() + "" + " and rsarno.nsitecode=" + userInfo.getNtranssitecode() + ""
					+ " and rs.nsitecode=" + userInfo.getNtranssitecode() + "" + " and r.nsitecode="
					+ userInfo.getNtranssitecode() + "" + " and rt.nsitecode=" + userInfo.getNtranssitecode() + ""
					+ " and rth.nsitecode=" + userInfo.getNtranssitecode() + "" + " and arno.nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rsarno.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rs.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and r.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rth.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and rth.ntesthistorycode = any(select max(ntesthistorycode) "
					+ " from registrationtesthistory where " + " nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + " nsitecode = "
					+ userInfo.getNtranssitecode() + " and " + " ntransactiontestcode in(" + stransactiontestcode
					+ ") group by ntransactiontestcode )"
					// + " and
					// rth.ntransactionstatus="+Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()+"
					+ " and rth.ntransactionstatus not in( " + stransactionStatus + ")";

			List<RegistrationTest> lstBasemastervalues = jdbcTemplate.query(sSampleCheckQuery, new RegistrationTest());

			if (lstBasemastervalues.size() > 0) {
				String ssamplearno = ((lstBasemastervalues.stream().map(object -> String.valueOf(object.getSamplearno()))
						.collect(Collectors.toList())).stream().distinct().collect(Collectors.toList())).stream()
						.collect(Collectors.joining(","));

				String sAlert =
						// commonFunction.getMultilingualMessage("IDS_TESTNOT",userInfo.getSlanguagefilename())+"
						// "+
						// commonFunction.getMultilingualMessage("IDS_COMPLETEDFOR",userInfo.getSlanguagefilename())
						commonFunction.getMultilingualMessage("IDS_BATCHCOMPLETEFAILALERT", userInfo.getSlanguagefilename())
								+ " " + ssamplearno;
				map.put("sAlert", sAlert);
				map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						Enumeration.ReturnStatus.FAILED.getreturnstatus());
			} else {
				map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
			}
			return map;
		}

		@SuppressWarnings("unchecked")
		@Override
		public ResponseEntity<Object> cancelBatch(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

			//ObjectMapper objmap = new ObjectMapper();

			int nbatchmastercode = (int) inputMap.get("nbatchmastercode");
			//int nsampletypecode = (int) inputMap.get("nsampletypecode");
			int startBatchSeqNo = (int) inputMap.get("nbatchhistorycode");
			// int ntesthistorycode = (int) inputMap.get("ntesthistorycode");
			// int nendTesthistorycode = (int) inputMap.get("nendTesthistorycode");
			// String stransactiontestcode = (String) inputMap.get("ntransactiontestcode");
			String sbatcharno = (String) inputMap.get("sbatcharno");
			//Boolean nneedsubsample = (Boolean) inputMap.get("nneedsubsample");
			// int completeId = (int) inputMap.get("completeId");
//			int nregtypecode = (int) inputMap.get("nregtypecode");
//			int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
		//	Boolean nneedtestinitiate = (Boolean) inputMap.get("nneedtestinitiate");

			Map<String, Object> returnMap = new HashMap<>();
			Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
			JSONObject actionType = new JSONObject();
			JSONObject jsonAuditObject = new JSONObject();

			final Batchmaster batchmaster = (Batchmaster) getActivBatch(nbatchmastercode, userInfo);

			if (batchmaster == null) {
				// status code:417
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {

				final BatchHistory batchhist = (BatchHistory) getCheckValidation(nbatchmastercode, userInfo);

				if (batchhist.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {

					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_SELECTINITIATEDORCOMPLETEDRECORD",
									userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);

				} else {
					final List<Batchmaster> lstCancelBatchmaster = getCancelValidation(nbatchmastercode, userInfo);

					if (lstCancelBatchmaster.size() > 0) {
						String ssamplearno = ((lstCancelBatchmaster.stream()
								.map(object -> String.valueOf(object.getSarno())).collect(Collectors.toList())).stream()
								.distinct().collect(Collectors.toList())).stream().collect(Collectors.joining(","));

						String sAlert = ssamplearno + " " + commonFunction.getMultilingualMessage("IDS_ALREADYCOMPLETED",
								userInfo.getSlanguagefilename());
						LOGGER.info("sAlert:"+ sAlert);	
					} else {

						String scomments = "";
						if (inputMap.containsKey("scomments") && inputMap.get("scomments") != null) {
							scomments = (String) inputMap.get("scomments");
						}

						String sInsertBatchHist = "insert into batchhistory(nbatchhistorycode,nbatchmastercode,sbatcharno,"
								+ " ndeputyusercode,ndeputyuserrolecode,ntransactionstatus,nusercode,nuserrolecode,dtransactiondate,"
								+ " noffsetdtransactiondate,ntransdatetimezonecode,"
								+ " scomments,nsitecode,nstatus) values(" + startBatchSeqNo + "," + nbatchmastercode + ",'"
								+ sbatcharno + "'," + " " + userInfo.getNdeputyusercode() + ","
								+ userInfo.getNdeputyuserrole() + ","
								+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + "," + " "
								+ userInfo.getNusercode() + "," + userInfo.getNuserrole() + "," + "'"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
								// + " '"+getCurrentDateTime(userInfo)+"',"
								+ " " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
								+ userInfo.getNtimezonecode() + "," + " N'" + stringUtilityFunction.replaceQuote(scomments) + "',"
								+ userInfo.getNtranssitecode() + "," + " "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

						jdbcTemplate.execute(sInsertBatchHist);

						final String strUpdate = "update seqnobatchcreation set nsequenceno = " + startBatchSeqNo
								+ " where stablename='batchhistory'";
						jdbcTemplate.execute(strUpdate);

						returnMap.putAll((Map<String, Object>) getBatchmaster(inputMap, userInfo).getBody());
						List<Batchmaster> lstbatchmaster = new ArrayList<>();
						lstbatchmaster.add((Batchmaster) returnMap.get("SelectedBatchmaster"));
						auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
						auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
						auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
						actionType.put("batchmaster", "IDS_CANCELBATCH");
						jsonAuditObject.put("batchmaster", lstbatchmaster);
						auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);
					}
				}
			}
			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		}

		@Override
		public Map<String, Object> seqNoGetforCancelBatch(UserInfo userInfo, Map<String, Object> inputMap)
				throws Exception {
			//ObjectMapper objmap = new ObjectMapper();
			final String sQuery = " lock table lockbatchcreation " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);
			//Boolean nneedtestinitiate = (Boolean) inputMap.get("nneedtestinitiate");
			int nbatchmastercode = (int) inputMap.get("nbatchmastercode");
			// int completeId = (int) inputMap.get("completeId");
//			int nregtypecode = (int) inputMap.get("nregtypecode");
//			int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
			final Map<String, Object> mapSeq = new HashMap<String, Object>();

			final Batchmaster batchmaster = (Batchmaster) getActivBatch(nbatchmastercode, userInfo);
			if (batchmaster == null) {
				mapSeq.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()));
			} else {
				final BatchHistory batchhist = (BatchHistory) getCheckValidation(nbatchmastercode, userInfo);
				if (batchhist.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
					mapSeq.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), commonFunction
							.getMultilingualMessage("IDS_SELECTINITIATEDRECORD", userInfo.getSlanguagefilename()));
				} else if (batchhist.getNtransactionstatus() == Enumeration.TransactionStatus.CANCELED
						.gettransactionstatus()) {
					mapSeq.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							commonFunction.getMultilingualMessage("ALREADYCANCELLED", userInfo.getSlanguagefilename()));
				} else {
					final List<Batchmaster> lstCancelBatchmaster = getCancelValidation(nbatchmastercode, userInfo);

					if (lstCancelBatchmaster.size() > 0) {
						String ssamplearno = ((lstCancelBatchmaster.stream()
								.map(object -> String.valueOf(object.getSarno())).collect(Collectors.toList())).stream()
								.distinct().collect(Collectors.toList())).stream().collect(Collectors.joining(","));

						String sAlert = ssamplearno + " " + commonFunction.getMultilingualMessage("IDS_ALREADYCOMPLETED",
								userInfo.getSlanguagefilename());
						mapSeq.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), sAlert);
					} else {
						final String getSeqBatchHistoryNo = "select stablename,nsequenceno from seqnobatchcreation "
								+ " where stablename = 'batchhistory'";

						SeqNoBatchcreation objSeqNo = jdbcTemplate.queryForObject(getSeqBatchHistoryNo,
								new SeqNoBatchcreation());
						int startBatchSeqNo = objSeqNo.getNsequenceno() + 1;
						mapSeq.put("nbatchhistorycode", startBatchSeqNo);

						mapSeq.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
								Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
					}
				}
			}
			return mapSeq;
		}

		private List<BatchHistory> getCheckDateForCompleteAction(int nbatchmastercode, UserInfo userInfo) throws Exception {

			String sInitiatedDate = "select bh.dtransactiondate " + " from batchmaster bm,batchhistory bh where "
					+ " bm.nbatchmastercode=bh.nbatchmastercode "
					+ " and bh.nbatchhistorycode=(select max(bh.nbatchhistorycode) from batchhistory bh where "
					+ " bh.nbatchmastercode = " + nbatchmastercode + " " + " and bh.ntransactionstatus="
					+ Enumeration.TransactionStatus.INITIATED.gettransactionstatus() + " " + " and bh.nsitecode="
					+ userInfo.getNtranssitecode() + "" + " and bh.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")" + " and bh.nsitecode="
					+ userInfo.getNtranssitecode() + "" + " and bm.nsitecode=" + userInfo.getNtranssitecode() + ""
					+ " and bh.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and bm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

			return (List<BatchHistory>) jdbcTemplate.query(sInitiatedDate, new BatchHistory());
		}

		
		@Override
		public ResponseEntity<Object> getInstrumentID(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
			final Map<String, Object> returnMap = new HashMap<String, Object>();

			int ninstrumentnamecode = (int) inputMap.get("ninstrumentnamecode");
			int ninstrumentcatcode = (int) inputMap.get("ninstrumentcatcode");
//			int ninstrumentcode = (int) inputMap.get("ninstrumentcode");
//			String  sQuery = "select i.sinstrumentid,i.ninstrumentcode,"
//					+ " ic.ninstrumentcatcode,ic.sinstrumentcatname"
//					+ " from instrument i,instrumentcategory ic where "
//					+ " i.ninstrumentcatcode = "+ninstrumentcatcode+""
//					+ " and ic.ninstrumentcatcode=i.ninstrumentcatcode"
//					+ " and ic.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and ic.nsitecode= "+userInfo.getNmastersitecode()+""
//					+ " and i.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and i.nsitecode= "+userInfo.getNmastersitecode()+"";

			String sQuery = " select i.sinstrumentid,i.ninstrumentcode, i.ninstrumentcatcode from instrument i"
					+ " where i.ninstrumentnamecode = " + ninstrumentnamecode + "" + " and i.ninstrumentcatcode = "
					+ ninstrumentcatcode + ""
					// + " and i.ninstrumentcode="+ninstrumentcode+""
					+ " and i.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and i.nsitecode= " + userInfo.getNmastersitecode() + " order by i.ninstrumentcode";

			List<Instrument> lstInstrument = (List<Instrument>) jdbcTemplate.query(sQuery, new Instrument());

			if (!lstInstrument.isEmpty()) {
				returnMap.put("selectedInstrumentId", lstInstrument.get(0));
				// returnMap.put("selectedInstrumentCategory", lstInstrument.get(0));
			} else {
				returnMap.put("selectedInstrumentId", null);
			}
			returnMap.put("instrumentID", lstInstrument);
			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		}


		private List<Batchmaster> getCancelValidation(int nbatchmastercode, UserInfo userInfo) throws Exception {

			String sQuery = "select bs.npreregno,rarno.sarno,bs.ntransactiontestcode,ts.stransstatus"
					+ " from batchmaster bm,batchsample bs,registrationtest rt,registrationtesthistory rth,"
					+ " registrationarno rarno,transactionstatus ts" + " where bm.nbatchmastercode=" + nbatchmastercode
					+ " and ts.ntranscode=rth.ntransactionstatus" + " and rth.ntransactiontestcode=rt.ntransactiontestcode"
					+ " and bs.nbatchmastercode=bm.nbatchmastercode and rt.ntransactiontestcode=bs.ntransactiontestcode"
					+ " and rth.ntesthistorycode=any(select max(ntesthistorycode) from registrationtesthistory rth1"
					+ " where rth1.ntransactiontestcode=rt.ntransactiontestcode group by rth1.npreregno)"
					+ " and rth.ntransactionstatus=" + Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() + " "
					+ " and rarno.npreregno=bs.npreregno";

			List<Batchmaster> lstCancelBatchmaster = jdbcTemplate.query(sQuery, new Batchmaster());
			return lstCancelBatchmaster;
		}

		@Override
		public ResponseEntity<Object> getBatchViewResult(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

			final Map<String, Object> returnMap = new HashMap<String, Object>();
			//Boolean nneedsubsample = (Boolean) inputMap.get("nneedsubsample");

//				String sQuery =	" select rp.ntransactionresultcode as limsprimarycode,bh.sbatcharno,"
//						+ " rsarno.ssamplearno,tm.stestsynonym,g.sgradename,g.ngradecode,"
//						+ " '['||CAST(rt.ntestrepeatno AS character varying(10))||']['||CAST(rt.ntestretestno AS character varying(10))||']' repretest,"
//						+ " tgtp.sparametersynonym,rp.npreregno,rarno.sarno,rs.ntransactionsamplecode,"
//				  		+ " rt.ntransactiontestcode,rt.ntestgrouptestcode,rt.ntestcode,rt.ntestretestno,rt.ntestrepeatno,"
//				  		+ " rp.ntestgrouptestparametercode,rp.ntestparametercode,rp.nparametertypecode,"
//				  		+ " (rp.jsondata->>'nroundingdigits')::int nroundingdigits,"
//				  		+ " (rp.jsondata->>'sresult')::varchar sresult "
//				  		+ " from registrationtest rt,resultparameter rp,registrationarno rarno,registrationsamplearno rsarno,"
//				  		+ " registrationsample rs, testgrouptestparameter tgtp,testmaster tm,batchmaster bm,"
//				  		+ "	batchiqctransaction biqc,grade g,batchhistory bh  "
//				  		+ " where"
//				  		//+ " rt.ntransactiontestcode in(156,157) "
//				  		+ " bm.nbatchmastercode="+inputMap.get("nbatchmastercode")+""
//				  		+ " and rt.npreregno=rp.npreregno and rt.ntransactiontestcode=rp.ntransactiontestcode"
//				  		+ " and rs.npreregno=rt.npreregno"
//				  		+ " and g.ngradecode=rp.ngradecode"
//				  		+ " and bm.nbatchmastercode=biqc.nbatchmastercode"
//				  		+ "	and biqc.ntransactiontestcode=rt.ntransactiontestcode"
//				  		+ " and rp.ntestgrouptestparametercode=tgtp.ntestgrouptestparametercode"
//				  		+ " and tm.ntestcode=rt.ntestcode "
//				  		+ " and rarno.npreregno=rt.npreregno"
//				  		+ " and rsarno.ntransactionsamplecode=rt.ntransactionsamplecode "
//				  		+ " and bh.nbatchhistorycode=any(select max(nbatchhistorycode) from batchhistory where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//				  		+ " and nsitecode="+userInfo.getNtranssitecode()+" and nbatchmastercode="+inputMap.get("nbatchmastercode")+")"
//				  		+ " and rt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//				  		+ " and rp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//				  		+ " and rarno.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//				  		+ " and rs.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//				  		+ " and tgtp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//				  		+ " and tm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//				  		+ " and bm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//				  		+ " and biqc.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//				  		+ " and g.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//				  		+ " and bh.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//				  		+ " and rsarno.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//				  		+ " and rt.nsitecode="+userInfo.getNtranssitecode()+""
//				  		+ " and rp.nsitecode="+userInfo.getNtranssitecode()+""
//				  		+ " and rarno.nsitecode="+userInfo.getNtranssitecode()+""
//				  		+ " and rs.nsitecode="+userInfo.getNtranssitecode()+""
//				  		+ " and tgtp.nsitecode="+userInfo.getNmastersitecode()+""
//				  		+ " and tm.nsitecode="+userInfo.getNmastersitecode()+""
//				  		+ " and bm.nsitecode="+userInfo.getNtranssitecode()+""
//				  		+ " and biqc.nsitecode="+userInfo.getNtranssitecode()+""
//				  		+ " and bh.nsitecode="+userInfo.getNtranssitecode()+""
//				  		+ " and rsarno.nsitecode="+userInfo.getNtranssitecode()+""
//						+ " and g.nsitecode="+userInfo.getNmastersitecode()+"";

			String sQuery = " select rp.ntransactionresultcode as limsprimarycode," + " bh.sbatcharno, rarno.sarno,"
//				    + " case when "+nneedsubsample+"=true "
//				    + "	then rsarno.ssamplearno else rarno.sarno end samplearno,"
			// + " bh.sbatcharno ||' '|| '(Group ID)' sbatcharno,"
					+ " rsarno.ssamplearno,tm.stestsynonym," + " case when g.ngradecode="
					+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ""
					+ "	then '-' else g.sgradename end sgradename," + "  g.ngradecode,"
					+ " '['||CAST(rt.ntestrepeatno AS character varying(10))||']['||CAST(rt.ntestretestno AS character varying(10))||']' repretest,"
					+ " tgtp.sparametersynonym,rp.npreregno,rarno.sarno,"
					+ " rt.ntransactiontestcode,rt.ntestgrouptestcode,"
					+ " rt.ntestcode,rt.ntestretestno,rt.ntestrepeatno, rp.ntestgrouptestparametercode,rp.ntestparametercode,rp.nparametertypecode,"
					+ " (rp.jsondata->>'nroundingdigits')::int nroundingdigits, (rp.jsondata->>'sresult')::varchar sresult"
					+ " from batchmaster bm,batchiqctransaction biqc,"
					+ " registrationtest rt,resultparameter rp,registrationarno rarno,"
					+ " registrationsamplearno rsarno,batchhistory bh,grade g,testgrouptestparameter tgtp,testmaster tm"
					+ " where bm.nbatchmastercode=" + inputMap.get("nbatchmastercode") + ""
					+ " and biqc.nbatchmastercode=bm.nbatchmastercode "
					+ " and rt.ntransactiontestcode=biqc.ntransactiontestcode"
					+ " and rt.ntransactiontestcode=rp.ntransactiontestcode"
					+ " and rt.npreregno=rp.npreregno and rarno.npreregno=rt.npreregno"
					+ " and rsarno.ntransactionsamplecode=rt.ntransactionsamplecode and g.ngradecode=rp.ngradecode"
					+ " and bh.nbatchhistorycode=any(select max(nbatchhistorycode) from batchhistory " + " where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and nsitecode="
					+ userInfo.getNtranssitecode() + "" + " and nbatchmastercode=" + inputMap.get("nbatchmastercode") + ")"
					+ " and tgtp.ntestgrouptestparametercode=rp.ntestgrouptestparametercode"
					+ " and tm.ntestcode=rt.ntestcode" + " and rsarno.ntransactionsamplecode=rt.ntransactionsamplecode"
					+ " and rt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and rp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and rarno.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and tgtp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and tm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and bm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and biqc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and g.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and bh.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and rsarno.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and rt.nsitecode=" + userInfo.getNtranssitecode() + "" + " and rp.nsitecode="
					+ userInfo.getNtranssitecode() + "" + " and rarno.nsitecode=" + userInfo.getNtranssitecode()
					+ " and tgtp.nsitecode=" + userInfo.getNmastersitecode() + "" + " and tm.nsitecode="
					+ userInfo.getNmastersitecode() + "  and bm.nsitecode=" + userInfo.getNtranssitecode() + ""
					+ " and biqc.nsitecode=" + userInfo.getNtranssitecode() + " and bh.nsitecode="
					+ userInfo.getNtranssitecode() + " " + " and rsarno.nsitecode=" + userInfo.getNtranssitecode()
					+ " and g.nsitecode=" + userInfo.getNmastersitecode() + "";

			List<RegistrationTest> lstResultBatchmaster = jdbcTemplate.query(sQuery, new RegistrationTest());

			String sELNQuery = " select  concat(rsarno.ssamplearno,'-[',rt.ntestrepeatno,'][',rt.ntestretestno,']') sampleID,bh.sbatcharno, rarno.sarno, rsarno.ssamplearno,tm.stestsynonym, "
					+ "'['||CAST(rt.ntestrepeatno AS character varying(10))||']['||CAST(rt.ntestretestno AS character varying(10))||']' repretest "
					+ "from batchmaster bm,batchiqctransaction biqc, registrationtest rt,registrationarno rarno, registrationsamplearno rsarno,"
					+ "batchhistory bh,testmaster tm " + "where bm.nbatchmastercode=" + inputMap.get("nbatchmastercode")
					+ " and biqc.nbatchmastercode=bm.nbatchmastercode  and rt.ntransactiontestcode=biqc.ntransactiontestcode and "
					+ "rarno.npreregno=rt.npreregno " + "and rsarno.ntransactionsamplecode=rt.ntransactionsamplecode  and "
					+ "bh.nbatchhistorycode=any(select max(nbatchhistorycode) from batchhistory  where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nbatchmastercode=" + inputMap.get("nbatchmastercode") + ") "
					+ "and tm.ntestcode=rt.ntestcode "
					+ "and rsarno.ntransactionsamplecode=rt.ntransactionsamplecode and rt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rarno.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and tm.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bm.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and biqc.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bh.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rsarno.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nsitecode="
					+ userInfo.getNtranssitecode() + " and rarno.nsitecode=" + userInfo.getNtranssitecode()
					+ " and tm.nsitecode=" + userInfo.getNmastersitecode() + "  " + "and bm.nsitecode="
					+ userInfo.getNtranssitecode() + " and biqc.nsitecode=" + userInfo.getNtranssitecode()
					+ " and bh.nsitecode=" + userInfo.getNtranssitecode() + "  and rsarno.nsitecode="
					+ userInfo.getNtranssitecode()
					+ " order by rt.npreregno,rt.ntransactionsamplecode,rt.ntransactiontestcode asc";
			List<RegistrationTest> lstForELNTest = jdbcTemplate.query(sELNQuery, new RegistrationTest());
			returnMap.put("Resultview", lstResultBatchmaster);
			returnMap.put("ELNTest", lstForELNTest);
			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		}

		public ResponseEntity<Object> getBatchTAT(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
			final Map<String, Object> returnMap = new HashMap<String, Object>();
			// String testListString = "";
			String sView = " select * from view_selectedbatchtat where view_selectedbatchtat.nbatchmastercode="
					+ inputMap.get("nbatchmastercode") + "";
			// + " view_selectedbatchtat.nusercode="+userInfo.getNmastersitecode()+"";

			try {
				Map<String, Object> map = jdbcTemplate.queryForMap(sView);
				returnMap.put("SelectedBatchTestTAT", map);
			} catch (Exception e) {
				returnMap.put("SelectedBatchTestTAT", null);
			}
			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		}

		@Override
		public ResponseEntity<Object> getProductInstrument(Map<String, Object> inputMap, UserInfo userInfo)
				throws Exception {
			final Map<String, Object> returnMap = new HashMap<String, Object>();
			int nsampletypecode = (int) inputMap.get("nsampletypecode");
			int naddcontrolCode = (int) inputMap.get("naddcontrolCode");
			int nregtypecode = (int) inputMap.get("nregtypecode");
			int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
			String stablename = "";
			String joiningField = "";
			String sProduct = "";
			int strValue = 0;
			int nproductcode = (int) inputMap.get("nproductcode");
			strValue = nproductcode;

			if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()) {
				stablename = "registrationsample";
				joiningField = "ntransactionsamplecode";
				sProduct = "ncomponentcode";
			} else {
				stablename = "registration";
				joiningField = "npreregno";
				sProduct = "nproductcode";
			}

			List<TransactionValidation> validationStatusLst = (List<TransactionValidation>) getTransactionValidation(
					naddcontrolCode, nregtypecode, nregsubtypecode, userInfo);
			final String stransactionStatus = validationStatusLst.stream()
					.map(objpreregno -> String.valueOf(objpreregno.getNtransactionstatus()))
					.collect(Collectors.joining(","));
			final List<TestInstrumentCategory> lstInstCategory = getInstCategory(stablename, joiningField, sProduct,
					strValue, stransactionStatus, inputMap, userInfo);

			if (!lstInstCategory.isEmpty()) {
				returnMap.put("selectedInstrumentCategory", lstInstCategory.get(0));
				returnMap.put("selectedProduct", inputMap.get("sproductname"));
				returnMap.put("selectedTestSynonym", inputMap.get("stestname"));
			} else {
				// returnMap.put("selectedInstrumentCategory",);
				returnMap.put("selectedTestSynonym", inputMap.get("stestname"));
				returnMap.put("selectedProduct", inputMap.get("sproductname"));
			}
			returnMap.put("instrumentCategory", lstInstCategory);
			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		}

		private List<TestInstrumentCategory> getInstCategory(String stablename, String joiningField, String sProduct,
				int strValue, String stransactionStatus, Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

			//final Map<String, Object> returnMap = new HashMap<String, Object>();
			//List<TestInstrumentCategory> lstInstCategory = new ArrayList<>();
			
			String sQuery = "select ic.ninstrumentcatcode,ic.sinstrumentcatname,ic.ncalibrationreq from "
					+ " registrationtesthistory rth,registrationtest rt," + stablename + " p," + " instrumentcategory ic "
					+ " where rth.ntransactiontestcode=rt.ntransactiontestcode"
					+ " and rt.ninstrumentcatcode=ic.ninstrumentcatcode" + " and p." + joiningField + "=rt." + joiningField
					+ "" + " and rth.nsitecode=" + userInfo.getNtranssitecode() + "" + " and rt.nsitecode="
					+ userInfo.getNtranssitecode() + "" + " and ic.nsitecode=" + userInfo.getNmastersitecode() + ""
					+ " and rth.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and rt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and ic.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and p."
					+ sProduct + "=" + strValue + ""
					+ " and rth.ntesthistorycode = any (select max(rth.ntesthistorycode)from registrationtesthistory rth,"
					+ " registrationtest rt where rt.ntransactiontestcode=rth.ntransactiontestcode "
					+ " and rt.ninstrumentcatcode > 0  and rt.ntestcode=" + inputMap.get("ntestcode") + ""
//				+ " and rth.ntransactionstatus between "+Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()+" "
//				+ " and "+Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()+" "
					+ " and rth.nsitecode=" + userInfo.getNtranssitecode() + "" + " and rt.nsitecode="
					+ userInfo.getNtranssitecode() + "" + " and rth.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and rt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " group by rth.ntransactiontestcode)"
					//ALPDJ21-31-->Added by Vignesh(23-07-2025)-->Added the new dropdown Instrument Type-Standard, Storage
					//start
					//+ " and it.ninstrumenttypecode=ic.ninstrumenttypecode "
					//+ " and it.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					//+ " and ic.ninstrumenttypecode="+Enumeration.InstrumentType.STANDARD_INSTRUMENT.getNinstrumenttypecode()
					//end
//				+ " and rth.ntransactionstatus between "+Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()+" "
//				+ " and "+Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()+" 
					+ " and rth.ntransactionstatus in(" + stransactionStatus + ")" + " group by ic.ninstrumentcatcode";
			// lstInstCategory=getJdbcTemplate().query(sQuery, new
			// TestInstrumentCategory());
			return (List<TestInstrumentCategory>) jdbcTemplate.query(sQuery, new TestInstrumentCategory());
		}

//		private List<ProjectMember> getProjectMemberCheck(UserInfo userInfo) throws Exception {
//
//			String sQuery = " select * from projectmaster pm,projectmember pr where "
//					+ " pm.nprojectmastercode=pr.nprojectmastercode and pr.nsitecode=" + userInfo.getNtranssitecode()
//					+ " and pm.nsitecode=" + userInfo.getNtranssitecode() + "" + " and pr.nstatus="
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and pm.nstatus="
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and pr.nusercode="
//					+ userInfo.getNusercode() + "";
//
//			return (List<ProjectMember>) jdbcTemplate.query(sQuery, new ProjectMember());
//		}

		private List<RegistrationTestHistory> toCheckSampleSubSampleCancelled(Map<String, Object> inputMap,
				UserInfo userInfo) throws Exception {
			

//			final Map<String, Object> returnMap = new HashMap<String, Object>();
//			List<ProjectMember> lstProjectMemeber = new ArrayList<>();
			int nbatchmastercode = (int) inputMap.get("nbatchmastercode");
//			String strTestList = "";
//			String stransactionsamplecode = "";
			String strSampleList = "";
			String sSamplecode = inputMap.get("multiTransSampleArr") == null
					? (String) inputMap.get("mulusertransactionsamplecode")
					: (String) inputMap.get("multiTransSampleArr");
			String sPreregno = inputMap.get("multiPreregArr") == null ? (String) inputMap.get("muluserpreregno")
					: (String) inputMap.get("multiPreregArr");
			String sTransactionTestcode = inputMap.get("multiTransTestArr") == null
					? (String) inputMap.get("mulusertransactiontestcode")
					: (String) inputMap.get("multiPreregArr");

			String sQuery = " select rt.ntransactiontestcode,rt.ntransactionsamplecode,rsarno.ssamplearno from registrationsamplehistory rsh,"
					+ " registrationsample rs,registrationsamplearno rsarno,registrationtest rt "
					+ " where rs.ntransactionsamplecode=rsh.ntransactionsamplecode "
					+ " and rt.ntransactionsamplecode=rs.ntransactionsamplecode"
					+ " and rsarno.ntransactionsamplecode=rs.ntransactionsamplecode " + " and rt.ntransactiontestcode in("
					+ sTransactionTestcode + ")" + " and rsh.nsamplehistorycode = any(SELECT Max(rsh1.nsamplehistorycode) "
					+ " from registrationsamplehistory rsh1 " + " where rsh1.ntransactionsamplecode in(" + sSamplecode + ")"
					+ " and rsh1.npreregno in(" + sPreregno + ")" + " and rsh1.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " GROUP  BY rsh1.ntransactionsamplecode)" + " and rsh.nsitecode=" + userInfo.getNtranssitecode() + ""
					+ " and rsarno.nsitecode=" + userInfo.getNtranssitecode() + "" + " and rs.nsitecode="
					+ userInfo.getNtranssitecode() + "" + " and rt.nsitecode=" + userInfo.getNtranssitecode() + ""
					+ " and rsh.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and rs.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and rsarno.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and rt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and rsh.ntransactionstatus in(" + Enumeration.TransactionStatus.CANCELED.gettransactionstatus()
					+ ")";

			List<RegistrationTestHistory> lstTest = jdbcTemplate.query(sQuery, new RegistrationTestHistory());
			if (lstTest.size() > 0) {

//				String strTestList = lstTest.stream().map(x -> String.valueOf(x.getNtransactiontestcode())).distinct()
//						.collect(Collectors.joining(","));

				strSampleList = lstTest.stream().map(x -> String.valueOf(x.getNtransactionsamplecode())).distinct()
						.collect(Collectors.joining(","));

				final String strUpdate = "update batchsample set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "" + " where nbatchmastercode="
						+ nbatchmastercode + " and ntransactionsamplecode in(" + strSampleList + ")" + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and nsitecode = "
						+ userInfo.getNtranssitecode() + "";

				jdbcTemplate.execute(strUpdate);
			}

		return lstTest;
		}

		
		private List<Batchmaster> getCancelTestValidation(int nbatchmastercode, UserInfo userInfo) throws Exception {

			String sQuery = "select bs.npreregno,rarno.sarno,bs.ntransactiontestcode,ts.stransstatus"
					+ " from batchmaster bm,batchsample bs,registrationtest rt,registrationtesthistory rth,"
					+ " registrationarno rarno,transactionstatus ts" + " where bm.nbatchmastercode=" + nbatchmastercode
					+ " and ts.ntranscode=rth.ntransactionstatus" + " and rth.ntransactiontestcode=rt.ntransactiontestcode"
					+ " and bs.nbatchmastercode=bm.nbatchmastercode and rt.ntransactiontestcode=bs.ntransactiontestcode"
					+ " and rth.ntesthistorycode=any(select max(ntesthistorycode) from registrationtesthistory rth1"
					+ " where rth1.ntransactiontestcode=rt.ntransactiontestcode and rth1.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by rth1.npreregno)"
					+ " and rth.ntransactionstatus in (" + Enumeration.TransactionStatus.CANCELED.gettransactionstatus()
					+ " " + " , " + Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ") "
					+ " and rarno.npreregno=bs.npreregno and bm.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and bs.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and rt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rth.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rarno.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and ts.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";

			List<Batchmaster> lstCancelTestBatchmaster = jdbcTemplate.query(sQuery, new Batchmaster());
			return lstCancelTestBatchmaster;
		}
		//ALPD-4999-To insert data when filter name input submit,done by Dhanushya RI	
		@Override
		public ResponseEntity<Object> createFilterName(final Map<String, Object> inputMap, final UserInfo userInfo)
				throws Exception {
			ObjectMapper objMapper = new ObjectMapper();

			Map<String, Object> objMap = new HashMap<>();
			//JSONObject jsonObject = new JSONObject();
		    //JSONObject jsonTempObject = new JSONObject();
		    final List<FilterName> lstFilterByName = projectDAOSupport.getFilterListByName(inputMap.get("sfiltername").toString(), userInfo);
			if (lstFilterByName.isEmpty()) 
			{
				final String strValidationQuery="select json_agg(jsondata || jsontempdata) as jsondata from filtername where nformcode="+userInfo.getNformcode()+" and nusercode="+userInfo.getNusercode()+" "
		        	+ " and nuserrolecode="+userInfo.getNuserrole()+" and nsitecode="+userInfo.getNtranssitecode()+" "
		        	+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
		        		+ " and (jsondata->'nsampletypecode')::int="+inputMap.get("nsampletypecode")+" "  
		        		+ " and (jsondata->'nregtypecode')::int="+inputMap.get("nregtypecode")+" "  
		        		+ " and (jsondata->'nregsubtypecode')::int="+inputMap.get("nregsubtypecode")+" "  
		        		+ " and (jsontempdata->>'ntranscode')::text in ('"+inputMap.get("ntranscode")+"') "  
		        		+ " and (jsontempdata->'napprovalversioncode')::int="+inputMap.get("napprovalversioncode")+" "
		    	        //+ " and (jsontempdata->'ndesigntemplatemappingcode')::int="+inputMap.get("ndesigntemplatemappingcode")+" " 
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
		//ALPD-4999-To show the previously saved filter name,done by Dhanushya RI

		@Override
		public List<FilterName> getFilterName( UserInfo userInfo) throws Exception {

			final String sFilterQry = "select * from filtername where nformcode="+userInfo.getNformcode()+" and nusercode="+userInfo.getNusercode()+""
					                  + " and nuserrolecode="+userInfo.getNuserrole()+" and nsitecode="+userInfo.getNtranssitecode()+" "
					                  + " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" order by 1 desc  ";

			return jdbcTemplate.query(sFilterQry, new FilterName());
		}
		//ALPD-4999-To get previously saved filter details when click the filter name,done by Dhanushya RI
		@SuppressWarnings("unchecked")
		@Override
		public ResponseEntity<Object> getBatchCreationFilter(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
			Map<String, Object> returnMap = new HashMap<>();

			List<Batchmaster> lstBatchmaster = new ArrayList<>();
			ObjectMapper objMapper = new ObjectMapper();

			String fromDate = "";
			String toDate = "";
//			String stablename = "";
//			String fieldname = "";
//			String selectField = "";
			final String strQuery="select json_agg(jsondata || jsontempdata) as jsondata from filtername where nformcode="+userInfo.getNformcode()+" and nusercode="+userInfo.getNusercode()+" "
		    		+ " and nuserrolecode="+userInfo.getNuserrole()+" and nsitecode="+userInfo.getNtranssitecode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
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
			        		+ " and (jsontempdata->>'ntranscode')::text in ('"+inputMap.get("ntranscode")+"') "  
			        		+ " and (jsontempdata->'napprovalversioncode')::int="+inputMap.get("napprovalversioncode")+" "
			    	       // + " and (jsontempdata->'ndesigntemplatemappingcode')::int="+inputMap.get("ndesigntemplatemappingcode")+" "
			    	        + " and (jsontempdata->>'DbFromDate')='"+inputMap.get("fromDate")+"' "
			    	        + " and (jsontempdata->>'DbToDate')='"+inputMap.get("toDate")+"' and nfilternamecode="+inputMap.get("nfilternamecode")+"" ; 
						    	
				final String strValidationFilter = jdbcTemplate.queryForObject(strValidationQuery, String.class);
			    	
				final List<Map<String, Object>> lstValidationFilter = strValidationFilter != null ? objMapper.readValue(strValidationFilter, new TypeReference<List<Map<String, Object>>>() {
						}):new ArrayList<Map<String, Object>>();
		if(lstValidationFilter.isEmpty()) {
			projectDAOSupport.updateFilterDetail(inputMap,userInfo);
			final String getSampleType = "select st.nsampletypecode,coalesce(st.jsondata->'sampletypename'->>'"
					+ userInfo.getSlanguagetypecode() + "',st.jsondata->'sampletypename'->>'en-US') ssampletypename"
					+ " from sampletype st,approvalconfig ac,approvalconfigversion acv,registrationtype rt,registrationsubtype rst"
					+ " where acv.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
					+ " and acv.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ac.napprovalconfigcode = acv.napprovalconfigcode " + " and ac.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and rt.nregtypecode = ac.nregtypecode " + " and rt.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and rt.nregtypecode = rst.nregtypecode and rst.nregsubtypecode = ac.nregsubtypecode"
					+ " and rst.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and rt.nsampletypecode = st.nsampletypecode " + " and acv.nsitecode = "
					+ userInfo.getNmastersitecode() + " and st.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and st.nsampletypecode > 0 "
					+ " group by st.nsampletypecode,st.jsondata->'sampletypename'->>'" + userInfo.getSlanguagetypecode()
					+ "'" + " order by st.nsorter";

			final List<SampleType> lstsampleType = jdbcTemplate.query(getSampleType, new SampleType());
			if (!lstsampleType.isEmpty()) {
				final SampleType filterSampleType=!lstfilterDetail.isEmpty()?objMapper.convertValue(lstfilterDetail.get(0).get("sampleTypeValue"),SampleType.class):lstsampleType.get(0);
				int nsampletypecode = filterSampleType.getNsampletypecode();
				returnMap.put("SampleType", lstsampleType);
				returnMap.put("realSampleTypeList", lstsampleType);		
				inputMap.put("nsampletypecode", nsampletypecode);
				returnMap.put("realSampleTypeValue", filterSampleType);
				returnMap.put("defaultSampleType", filterSampleType);
				inputMap.put("defaultSampleType", filterSampleType);
				inputMap.put("filterDetailValue", lstfilterDetail);
				returnMap.putAll((Map<String, Object>) getRegistrationType(inputMap, userInfo).getBody());
			
			int nregtypecode = Integer.parseInt(lstfilterDetail.get(0).get("nregtypecode").toString());
			int nregsubtypecode = Integer.parseInt(lstfilterDetail.get(0).get("nregsubtypecode").toString());
			String ntranscode = lstfilterDetail.get(0).get("ntranscode").toString();
			String napprovalconfigcode = (String) lstfilterDetail.get(0).get("napprovalconfigcode").toString();
			String napprovalversioncode = (String) lstfilterDetail.get(0).get("napprovalversioncode").toString();
			int ndesigntemplatemappingcode = Integer.parseInt(lstfilterDetail.get(0).get("ndesigntemplatemappingcode").toString());	

				final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
				final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());

				final String fromDateUI = LocalDateTime.parse((String) lstfilterDetail.get(0).get("fromDate").toString(),dbPattern)
						.format(uiPattern);
				final String toDateUI = LocalDateTime.parse((String) lstfilterDetail.get(0).get("toDate").toString(), dbPattern).format(uiPattern);

				returnMap.put("realFromDate", fromDateUI);
				returnMap.put("realToDate", toDateUI);

				fromDate = dateUtilityFunction.instantDateToString(
						dateUtilityFunction.convertStringDateToUTC((String) lstfilterDetail.get(0).get("fromDate"), userInfo, true));
				toDate = dateUtilityFunction.instantDateToString(
						dateUtilityFunction.convertStringDateToUTC((String) lstfilterDetail.get(0).get("toDate"), userInfo, true));
				
				fromDate = fromDate.replace("T", " ");
				toDate = toDate.replace("T", " ");
				returnMap.put("fromdate", fromDate);
				returnMap.put("todate", toDate);
				
			final Integer count = projectDAOSupport.getApprovalConfigVersionByUserRoleTemplate(Integer.parseInt(napprovalversioncode),
					userInfo);
			if (inputMap.containsKey("operation") && count == 0) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_USERNOTINRESULTENTRYFLOW",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else {

//				if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()) {
//					stablename = "component";
//					fieldname = "ncomponentcode";
//					selectField = "scomponentname";
//				} else {
//					stablename = "product";
//					fieldname = "nproductcode";
//					selectField = "sproductname";
//				}

				final String getSampleQuery = "SELECT * from fn_batchget('" + fromDate + "','" + toDate + "'," + ""
						+ nsampletypecode+"," +nregtypecode + ","
						+ nregsubtypecode+ "," + "" + napprovalversioncode + ","
						+ ndesigntemplatemappingcode + "," + "'" + ntranscode + "'," + userInfo.getNusercode() + ","
						+ userInfo.getNtranssitecode() + "," + "'" + userInfo.getSlanguagetypecode() + "',"
						+ napprovalconfigcode + "," + userInfo.getNmastersitecode() + ",'" + userInfo.getSsitedate()
						+ "');";
				
				lstBatchmaster = jdbcTemplate.query(getSampleQuery, new Batchmaster());
				if (!lstBatchmaster.isEmpty()) {
					returnMap.put("SelectedBatchmaster", lstBatchmaster.get(0));
					returnMap.put("nbatchmastercode", lstBatchmaster.get(0).getNbatchmastercode());
					returnMap.put("ntestcode", lstBatchmaster.get(0).getNtestcode());
					returnMap.putAll((Map<String, Object>) getSampleTabDetails(lstBatchmaster.get(0).getNbatchmastercode(),
							userInfo, ndesigntemplatemappingcode).getBody());
					returnMap.putAll(
							(Map<String, Object>) getBatchhistory(lstBatchmaster.get(0).getNbatchmastercode(), userInfo)
									.getBody());
					returnMap.putAll((Map<String, Object>) getBatchIqcSampleAction(returnMap, userInfo).getBody());
					returnMap.putAll((Map<String, Object>) getTestDetails(returnMap, userInfo).getBody());
					returnMap.putAll((Map<String, Object>) getBatchTAT(returnMap, userInfo).getBody());

					returnMap.put("DynamicDesign",
							projectDAOSupport.getTemplateDesign((int) ndesigntemplatemappingcode, userInfo.getNformcode()));

				} else {
					returnMap.put("SelectedBatchmaster", null);
					returnMap.put("defaultSampleType", inputMap.get("defaultSampleType"));
					returnMap.put("defaultRegistrationType", inputMap.get("defaultRegistrationType"));
					returnMap.put("defaultRegistrationSubType", inputMap.get("defaultRegistrationSubType"));

				}
				returnMap.put("Batchmaster", lstBatchmaster);

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
		private Map<String,Object> changeTimeFormatForUi(final String toDateUi, UserInfo userInfo) throws Exception {
			Map<String,Object> returnMap = new HashMap<>();
			
			final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
			final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());
			
			final String toDateUI = LocalDateTime.parse((String) toDateUi, dbPattern).format(uiPattern);
			
			returnMap.put("realToDate", toDateUI);
			
			String toDate = dateUtilityFunction.instantDateToString(
					dateUtilityFunction.convertStringDateToUTC((String) toDateUi, userInfo, true));
			toDate = toDate.replace("T", " ");
			returnMap.put("todate", toDate);
			
			return returnMap;
		}

}
