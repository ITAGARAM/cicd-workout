package com.agaramtech.qualis.registration.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
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

import com.agaramtech.qualis.basemaster.model.TransactionStatus;
import com.agaramtech.qualis.configuration.model.ApprovalConfigAutoapproval;
import com.agaramtech.qualis.configuration.model.ApprovalConfigRole;
import com.agaramtech.qualis.configuration.model.FilterName;
import com.agaramtech.qualis.configuration.model.SampleType;
import com.agaramtech.qualis.configuration.model.Settings;
import com.agaramtech.qualis.dynamicpreregdesign.model.ReactRegistrationTemplate;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationSubType;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationType;
import com.agaramtech.qualis.externalorder.model.ExternalOrderSample;
import com.agaramtech.qualis.externalorder.model.ExternalOrderSampleAttachment;
import com.agaramtech.qualis.externalorder.model.ExternalOrderTest;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.EmailDAOSupport;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.ExternalOrderSupport;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.TransactionDAOSupport;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.instrumentmanagement.model.InstrumentCalibration;
import com.agaramtech.qualis.product.model.Component;
import com.agaramtech.qualis.registration.model.Registration;
import com.agaramtech.qualis.registration.model.RegistrationHistory;
import com.agaramtech.qualis.registration.model.RegistrationParameter;
import com.agaramtech.qualis.registration.model.RegistrationSample;
import com.agaramtech.qualis.registration.model.RegistrationSampleHistory;
import com.agaramtech.qualis.registration.model.RegistrationSection;
import com.agaramtech.qualis.registration.model.RegistrationTest;
import com.agaramtech.qualis.registration.model.RegistrationTestHistory;
import com.agaramtech.qualis.registration.model.SeqNoArnoGenerator;
import com.agaramtech.qualis.registration.model.SeqNoRegistration;
import com.agaramtech.qualis.registration.model.TestGroupTestForSample;
import com.agaramtech.qualis.registration.model.TransactionValidation;
import com.agaramtech.qualis.scheduler.model.ScheduleMaster;
import com.agaramtech.qualis.scheduler.service.SchedulerDAO;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecSampleType;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecification;
import com.agaramtech.qualis.testgroup.model.TestGroupTest;
import com.agaramtech.qualis.testgroup.model.TestGroupTestParameter;
import com.agaramtech.qualis.testgroup.model.TreeTemplateManipulation;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.base.Joiner;

import lombok.AllArgsConstructor;

@Transactional(rollbackFor = Exception.class)
@AllArgsConstructor
@Repository
public class RegistrationDAOImpl implements RegistrationDAO{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationDAOImpl.class);
	
	private final TransactionDAOSupport transactionDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final StringUtilityFunction stringUtilityFunction;
	private final FTPUtilityFunction ftpUtilityFunction;
	private final RegistrationDAOSupport registrationDAOSupport;
	private final AuditUtilityFunction auditUtilityFunction;
	private final EmailDAOSupport emailDAOSupport;
	private final ExternalOrderSupport externalOrderSupport;
	private final SchedulerDAO schedulerDAO;
	
		
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getRegistration(UserInfo userInfo, String currentUIDate) throws Exception {
		
		LOGGER.info("getRegistration");
		
		final ObjectMapper objMapper = new ObjectMapper();
		
		List<RegistrationType> lstRegistrationType = new ArrayList<>();
		List<RegistrationSubType> lstRegistrationSubType = new ArrayList<>();
		Map<String, Object> returnMap = new HashMap<>();
		List<TransactionStatus> lstTransactionstatus = new ArrayList<>();
		List<ApprovalConfigAutoapproval> listApprovalConfigAutoapproval = new ArrayList<>();
		List<ReactRegistrationTemplate> listReactRegistrationTemplate = new ArrayList<ReactRegistrationTemplate>();
		
		final List<SampleType> lstSampleType = transactionDAOSupport.getSampleType(userInfo);		
		
		
		if (!lstSampleType.isEmpty()) {
			
			final Map<String, Object> favoriteFilter = projectDAOSupport.getFavoriteFilterDetail(userInfo);
			 
			if(favoriteFilter.containsKey("FromDate") 
					 && favoriteFilter.containsKey("ToDate"))
			{
					returnMap.put("FromDate", favoriteFilter.get("FromDate"));
					returnMap.put("ToDate", favoriteFilter.get("ToDate"));			
					
					returnMap.put("FromDateWOUTC", favoriteFilter.get("FromDateWOUTC"));
					returnMap.put("ToDateWOUTC", favoriteFilter.get("ToDateWOUTC"));
			}
			else {
				if (currentUIDate != null && currentUIDate.trim().length() != 0) {
					returnMap = projectDAOSupport.getDateFromControlProperties(userInfo, currentUIDate, "datetime", "FromDate");
				}				
			}
				
				
			final SampleType filterSampleType = favoriteFilter.containsKey("sampleTypeValue") 
					? (SampleType) objMapper.convertValue(favoriteFilter.get("sampleTypeValue"), SampleType.class) : lstSampleType.get(0);
			
			final int nsampletypecode = filterSampleType.getNsampletypecode();

			returnMap.put("SampleTypeValue", filterSampleType);
			returnMap.put("nsampletypecode", filterSampleType.getNsampletypecode());
			returnMap.put("RealSampleTypeValue", filterSampleType) ;				
				
			lstRegistrationType = transactionDAOSupport.getRegistrationType(nsampletypecode, userInfo);
				
			if (!lstRegistrationType.isEmpty()) 
			{					
				final RegistrationType filterRegType = favoriteFilter.containsKey("regTypeValue") 
						? (RegistrationType) objMapper.convertValue(favoriteFilter.get("regTypeValue"), RegistrationType.class) : lstRegistrationType.get(0);
								
				returnMap.put("RegTypeValue",filterRegType);
				returnMap.put("nregtypecode", filterRegType.getNregtypecode());
				returnMap.put("RealRegTypeValue", filterRegType);					
					
				lstRegistrationSubType = transactionDAOSupport.getRegistrationSubType(filterRegType.getNregtypecode(), userInfo);
					
				if (!lstRegistrationSubType.isEmpty()) {					
					
					final RegistrationSubType filterRegSubType = favoriteFilter.containsKey("regSubTypeValue") 
							? (RegistrationSubType) objMapper.convertValue(favoriteFilter.get("regSubTypeValue"), RegistrationSubType.class): lstRegistrationSubType.get(0);
					
					returnMap.put("nregsubtypecode", filterRegSubType.getNregsubtypecode());
					returnMap.put("nregsubtypeversioncode", filterRegSubType.getNregsubtypeversioncode());
					returnMap.put("nneedsubsample",filterRegSubType.isNneedsubsample());
					returnMap.put("RegSubTypeValue", filterRegSubType);
					returnMap.put("RealRegSubTypeValue", filterRegSubType);						
						
					lstTransactionstatus = transactionDAOSupport.getFilterStatus(filterRegType.getNregtypecode(),
								filterRegSubType.getNregsubtypecode(), userInfo);
					
					
					final TransactionStatus filterTransactionStatus = favoriteFilter.containsKey("filterStatusValue") 
							? (TransactionStatus) objMapper.convertValue(favoriteFilter.get("filterStatusValue"), TransactionStatus.class) : lstTransactionstatus.get(0);
											
					returnMap.put("nfilterstatus", filterTransactionStatus.getNtransactionstatus());
						
					listApprovalConfigAutoapproval = transactionDAOSupport.getApprovalConfigVersion(
								filterRegType.getNregtypecode(),
								filterRegSubType.getNregsubtypecode(), userInfo);
						
					final ApprovalConfigAutoapproval filterApprovalConfig = favoriteFilter.containsKey("approvalConfigValue") 
									? (ApprovalConfigAutoapproval) objMapper.convertValue(favoriteFilter.get("approvalConfigValue"),ApprovalConfigAutoapproval.class)
											: listApprovalConfigAutoapproval.get(0);
					
					returnMap.put("ApprovalConfigVersionValue", filterApprovalConfig);
					returnMap.put("napproveconfversioncode",
							filterApprovalConfig.getNapproveconfversioncode());
					returnMap.put("RealApprovalConfigVersionValue", filterApprovalConfig);
						
						
					if (!listApprovalConfigAutoapproval.isEmpty()) 
					{							
						listReactRegistrationTemplate = (List<ReactRegistrationTemplate>) transactionDAOSupport.getApproveConfigVersionRegTemplate(
								filterRegType.getNregtypecode(),
								filterRegSubType.getNregsubtypecode(),
								filterApprovalConfig.getNapproveconfversioncode()).getBody();
	
						final ReactRegistrationTemplate filterReactRegistrationTemplate = favoriteFilter.containsKey("designTemplateMappingValue") 
								? (ReactRegistrationTemplate) objMapper.convertValue(favoriteFilter.get("designTemplateMappingValue"),  ReactRegistrationTemplate.class)
										: listReactRegistrationTemplate.get(0);
							
						if (!listReactRegistrationTemplate.isEmpty()) 
						{
	
							returnMap.put("registrationTemplate", filterReactRegistrationTemplate);
							returnMap.put("SubSampleTemplate", transactionDAOSupport.getRegistrationSubSampleTemplate(
											filterReactRegistrationTemplate.getNdesigntemplatemappingcode())
													.getBody());
							returnMap.put("DynamicDesign", projectDAOSupport.getTemplateDesign(
													filterReactRegistrationTemplate.getNdesigntemplatemappingcode(),
													userInfo.getNformcode()));
							returnMap.put("ndesigntemplatemappingcode",
											filterReactRegistrationTemplate.getNdesigntemplatemappingcode());
	
							returnMap.put("checkBoxOperation", 3);
							returnMap.put("activeSampleTab", "IDS_SAMPLEATTACHMENTS");
							returnMap.put("activeTestTab", "IDS_PARAMETERRESULTS");
							returnMap.put("activeSubSampleTab", "IDS_SUBSAMPLEATTACHMENTS");
							returnMap.putAll(registrationDAOSupport.getDynamicRegistration(returnMap, userInfo));
							returnMap.put("DesignTemplateMappingValue", filterReactRegistrationTemplate);
							returnMap.put("RealDesignTemplateMappingValue", filterReactRegistrationTemplate);
						} else {
							returnMap.put("DesignTemplateMappingValue", null);
							returnMap.put("RealDesignTemplateMappingValue", null);
							returnMap.put("RealDesignTemplateMappingValueList", null);
							returnMap.put("registrationTemplate", new ArrayList<>());
							returnMap.put("SubSampleTemplate", new ArrayList<>());
							returnMap.put("DynamicDesign", null);
							returnMap.put("activeSampleTab", "IDS_SAMPLEATTACHMENTS");
							returnMap.put("activeTestTab", "IDS_PARAMETERRESULTS");
							returnMap.put("activeSubSampleTab", "IDS_SUBSAMPLEATTACHMENTS");
						}

					} else {
						returnMap.put("ApprovalConfigVersionValue", null);
						returnMap.put("RealApprovalConfigVersionValue", null);
						returnMap.put("RealApprovalConfigVersionValueList", null);
					}
	
					returnMap.put("FilterStatusValue", filterTransactionStatus);
					returnMap.put("nfilterstatus", filterTransactionStatus.getNtransactionstatus());
					returnMap.put("RealFilterStatusValue", filterTransactionStatus);

				} else {
					returnMap.put("ApprovalConfigVersionValue", null);
					returnMap.put("RealApprovalConfigVersionValue", null);
					returnMap.put("RealApprovalConfigVersionValueList", null);
					returnMap.put("FilterStatusValue", null);
					returnMap.put("RealFilterStatusValue", null);
					returnMap.put("RealFilterStatusValueList", null);
					returnMap.put("registrationTemplate", new ArrayList<>());
					returnMap.put("SubSampleTemplate", new ArrayList<>());
					returnMap.put("DynamicDesign", null);
					returnMap.put("activeSampleTab", "IDS_SAMPLEATTACHMENTS");
					returnMap.put("activeTestTab", "IDS_PARAMETERRESULTS");
					returnMap.put("activeSubSampleTab", "IDS_SUBSAMPLEATTACHMENTS");
				}
			}
			else {
					returnMap.put("RegSubTypeValue", null);
					returnMap.put("RealRegSubTypeValue", null);
					returnMap.put("RealRegSubTypeValueList", null);
					returnMap.put("ApprovalConfigVersionValue", null);
					returnMap.put("RealApprovalConfigVersionValue", null);
					returnMap.put("RealApprovalConfigVersionValueList", null);
					returnMap.put("FilterStatusValue", null);
					returnMap.put("RealFilterStatusValue", null);
					returnMap.put("RealFilterStatusValuelist", null);
					returnMap.put("registrationTemplate", new ArrayList<>());
					returnMap.put("SubSampleTemplate", new ArrayList<>());
					returnMap.put("DynamicDesign", null);
					returnMap.put("activeSampleTab", "IDS_SAMPLEATTACHMENTS");
					returnMap.put("activeTestTab", "IDS_PARAMETERRESULTS");
					returnMap.put("activeSubSampleTab", "IDS_SUBSAMPLEATTACHMENTS");
				}
		}
		
		returnMap.put("SampleType", lstSampleType);
		returnMap.put("RealSampleTypeList", lstSampleType);
		
		returnMap.put("RegistrationType", lstRegistrationType);	
		returnMap.put("RealRegTypeList", lstRegistrationType);
		
		returnMap.put("RegistrationSubType", lstRegistrationSubType);
		returnMap.put("RealRegSubTypeList", lstRegistrationSubType);
		
		returnMap.put("FilterStatus", lstTransactionstatus);
		returnMap.put("RealFilterStatuslist", lstTransactionstatus);
		
		returnMap.put("ApprovalConfigVersion", listApprovalConfigAutoapproval);
		returnMap.put("RealApprovalConfigVersionList", listApprovalConfigAutoapproval);
		
		returnMap.put("DesignTemplateMapping", listReactRegistrationTemplate);
		returnMap.put("RealDesignTemplateMappingList", listReactRegistrationTemplate);
		
		returnMap.put("FromDate", returnMap.get("FromDateWOUTC"));
		returnMap.put("ToDate", returnMap.get("ToDateWOUTC"));
		
		returnMap.put("RealFromDate", returnMap.get("FromDateWOUTC"));
		returnMap.put("RealToDate", returnMap.get("ToDateWOUTC"));
		
		final List<FilterName> lstFilterName = projectDAOSupport.getFavoriteFilterName(userInfo);
		returnMap.put("FilterName",lstFilterName);
		
		returnMap.put("TransactionValidation",transactionDAOSupport.getTransactionStatus(userInfo));
	
		return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
	}
	
	@Override
	public ResponseEntity<Object> getRegistrationSubSample(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception{
		return registrationDAOSupport.getRegistrationSubSample(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getRegistrationTest(Map<String, Object> inputMap, UserInfo userInfo) 
			throws Exception
	{
		return registrationDAOSupport.getRegistrationTest(inputMap, userInfo);
	}	
	
	//ALPD-5530--Vignesh R(06-03-2025)--record allowing the pre-register when the approval config retired
	@Override
	public short getActiveApprovalConfigId(final int napproveconfversioncode, final UserInfo userInfo) {
			
		final String strQuery = "select ntransactionstatus from approvalconfigversion where "
								+ " napproveconfversioncode = "+napproveconfversioncode +" "
								+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and nsitecode="+userInfo.getNmastersitecode();
		
		final short ntransactionstatus = jdbcTemplate.queryForObject(strQuery, Short.class);			
		return ntransactionstatus;			
	}
	
	@Override
	public ResponseEntity<Object> getTreeByProduct(Map<String, Object> inputMap) throws Exception {

		final Map<String, Object> objMap = new HashMap<String, Object>();
		final int nproductcatcode = (int) inputMap.get("nproductcatcode");
		final int nproductcode = (int) inputMap.get("nproductcode");
		final int nsampletypecode = (int) inputMap.get("nsampletypecode");
		int nprojectMasterCode = -1;
		final int nneedSubSample = (int) inputMap.get("nneedsubsample");
		final int ntestGroupSpecRequired = inputMap.containsKey("ntestgroupspecrequired") ? (int) inputMap.get("ntestgroupspecrequired") : Enumeration.TransactionStatus.NO.gettransactionstatus();	//ALPD-4834,Vishakh, Added ntestGroupSpecRequired to handle show or hide add spec button

		int nordertypecode = -1;
		int nallottedspeccode = -1;

		if (inputMap.containsKey("nordertypecode")) {
			nordertypecode = (int) inputMap.get("nordertypecode");
		}

		if (inputMap.containsKey("nallottedspeccode")) {
			nallottedspeccode = (int) inputMap.get("nallottedspeccode");
		}

		if (nsampletypecode == Enumeration.SampleType.PROJECTSAMPLETYPE.getType()) {
			// ALPD-2009
			if (inputMap.get("nprojectmastercode") != null) {
				nprojectMasterCode = (int) inputMap.get("nprojectmastercode");
			}
		}
		objMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), false);

		objMap.putAll(sampleRegistrationTreeLoad(nproductcatcode, nproductcode, nsampletypecode, nprojectMasterCode,
				nneedSubSample, nallottedspeccode, nordertypecode, ntestGroupSpecRequired));	//ALPD-4834,Vishakh, Added ntestGroupSpecRequired to handle show or hide add spec button

		objMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), true);

		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}
	
	private Map<String, Object> sampleRegistrationTreeLoad(int nproductcatcode, int nproductcode, int nsampletypecode,
			int nprojectMasterCode, int needSubSample, int nalloctedspeccode, int nordertypecode, int ntestGroupSpecRequired) 
					throws Exception 
	{
		final Map<String, Object> objMap = new HashMap<String, Object>();

		List<TreeTemplateManipulation> lstMaster = new ArrayList<TreeTemplateManipulation>();
		int ntreetemplatemanipulationcode = 0;

		final String projectString =  " and ttm.nprojectmastercode = " + nprojectMasterCode;
	

		final String sApprovalStatusQuery = "select acr.napprovalstatuscode from approvalconfigrole acr,approvalconfigversion acv,approvalconfig ac"
										+ " where acv.napproveconfversioncode = acr.napproveconfversioncode "
										+ " and ac.napprovalconfigcode = acr.napprovalconfigcode" + " and ac.napprovalsubtypecode = "
										+ Enumeration.ApprovalSubType.STUDYPLANAPPROVAL.getnsubtype()
										+ " and acr.nlevelno = 1 and acr.nstatus = "
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acv.nstatus = "
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ac.nstatus = "
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  acv.ntransactionstatus not in ("
										+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ") group by acr.napprovalstatuscode";
		final List<ApprovalConfigRole> lstgetApprovalStatus = jdbcTemplate.query(sApprovalStatusQuery,
				new ApprovalConfigRole());

		final String sapprovalStatus = stringUtilityFunction.fnDynamicListToString(lstgetApprovalStatus, "getNapprovalstatuscode");

		if (sapprovalStatus != null && sapprovalStatus != "") {
			String componentQuery = "";
			if (needSubSample == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
				componentQuery = " and tgs.ncomponentrequired=" + needSubSample;
			}
			String portalProfile = "";

			//if (nsampletypecode == 5 && nordertypecode == 2 && nalloctedspeccode != -1) {
			if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType() 
								&& nordertypecode == Enumeration.OrderType.EXTERNAL.getOrderType() 
								&& nalloctedspeccode != Enumeration.TransactionStatus.NA.gettransactionstatus()) {
				portalProfile = " and tgs.nallottedspeccode=" + nalloctedspeccode;
			}
			String sTreeQuery ="";
			
			//mastersitecode to be included in where clause -L.Subashini - 25/04/2025
			  sTreeQuery = sTreeQuery + ";with RECURSIVE tree (sleveldescription, nparentnode, ntemplatemanipulationcode)"
									+ " as (" + " select ttm.sleveldescription, ttm.nparentnode, ttm.ntemplatemanipulationcode "
									+ " from treetemplatemanipulation ttm join testgroupspecification tgs"
									+ " on ttm.ntemplatemanipulationcode = tgs.ntemplatemanipulationcode " + portalProfile
									+ " where  tgs.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and ttm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and ttm.nsampletypecode = " + nsampletypecode 
									+ " and ttm.nproductcatcode = " + nproductcatcode
									+ " and ttm.nproductcode = " + nproductcode + projectString 
									+ " and tgs.napprovalstatus in ("
									+ sapprovalStatus
									+ ") " + componentQuery + " and tgs.dexpirydate>(select now() at time zone 'utc') union all"
									+ " select ttm.sleveldescription, ttm.nparentnode, ttm.ntemplatemanipulationcode  "
									+ " from treetemplatemanipulation ttm, tree t where nsampletypecode = " + nsampletypecode
									+ " and nproductcatcode = " + nproductcatcode + " and nproductcode = " + nproductcode
									+ " and nprojectmastercode = " + nprojectMasterCode
									+ " and ttm.ntemplatemanipulationcode = t.nparentnode and ttm.nstatus = "
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
			//if (nsampletypecode == 5 && nordertypecode == 2 && nalloctedspeccode != -1) 
			if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType() 
				&& nordertypecode == Enumeration.OrderType.EXTERNAL.getOrderType() 
				&& nalloctedspeccode != Enumeration.TransactionStatus.NA.gettransactionstatus())
			{
				objMap.put("Specification",
						getTestGroupSpecificationBasedOnPortal(ntreetemplatemanipulationcode, 0, nalloctedspeccode));
			} else {
				objMap.put("Specification", getTestGroupSpecificationFromTreeProduct(ntreetemplatemanipulationcode, 0, ntestGroupSpecRequired));	//ALPD-4834,Vishakh, Added ntestGroupSpecRequired to handle show or hide add spec button
			}

		} else {
			objMap.put("AgaramTree", Arrays.asList());
			objMap.put("OpenNodes", Arrays.asList());
			objMap.put("FocusKey", Arrays.asList());
			objMap.put("ActiveKey", null);
			objMap.put("Specification", null);
		}

		return objMap;
	}
	//ALPD-5873 Added by Abdul for Default Spec
	public List<TestGroupSpecification> getTestGroupSpecificationFromTreeProduct(int ntreetemplatemanipulationcode, int npreregno, int ntestGroupSpecRequired)
			throws Exception {

		String getTestGroupSpecification = "";
		String strDefaultSpec = "";
		if(ntestGroupSpecRequired == Enumeration.TransactionStatus.YES.gettransactionstatus()) {	//ALPD-4834,Vishakh, Added ntestGroupSpecRequired to handle show or hide add spec button
			strDefaultSpec = " "+ ntreetemplatemanipulationcode+ " ";
		} 
		else {
			
			strDefaultSpec = " ( select max(ntemplatemanipulationcode) from treetemplatemanipulation where nproductcode="+ Enumeration.TransactionStatus.NA.gettransactionstatus()+" "
					+ "and nproductcatcode="+Enumeration.Default.DEFAULTSPEC.getDefault()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ), " + ntreetemplatemanipulationcode+ " ";
		}
		if (npreregno == 0) {
			getTestGroupSpecification = "select tgs.nallottedspeccode,tgts.nspecsampletypecode,sspecname ,sversion,ntemplatemanipulationcode,tgs.ncomponentrequired "
										+ " from testgroupspecification tgs,approvalconfigrole apr,"
										+ " approvalconfigversion apv,testgroupspecsampletype tgts  "
										+ " where tgts.nallottedspeccode=tgs.nallottedspeccode "			
										+ " and ntemplatemanipulationcode in ("+ strDefaultSpec+") "
										+ " and tgs.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
										+ " and apr.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and tgts.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
										+ " and apr.nlevelno=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
										+ " and apv.napproveconfversioncode = apr.napproveconfversioncode "
										+ " and apv.napproveconfversioncode = tgs.napproveconfversioncode  "
										+ " and tgs.napprovalstatus  = any (select napprovalstatuscode from approvalconfig ap, "
										+ " approvalconfigrole apr,approvalconfigversion apv where  ap.napprovalsubtypecode="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
										+ " and ap.nregsubtypecode=" + Enumeration.TransactionStatus.NA.gettransactionstatus() 
										+ " and ap.nregtypecode="+ Enumeration.TransactionStatus.NA.gettransactionstatus()
										+ " and ap.napprovalconfigcode=apv.napprovalconfigcode and  "
										+ " apr.napproveconfversioncode=apv.napproveconfversioncode    " + ""
										//ALPD-5206 Added by  Neeraj Sample registration screen -> Specification showing wrongly in specific scenario.
										+ " and apv.napproveconfversioncode = tgs.napproveconfversioncode   " + " "
										+ " and apv.ntransactionstatus<>"+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
										+ " and apr.nlevelno="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
										+ " and ap.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
										+ " and apr.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
										+ " and apv.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 									
										+ ") ";
		} else {
			getTestGroupSpecification = " select tgs.nallottedspeccode,tgs.sspecname as text,tgs.sspecname,tgs.ncomponentrequired  "
										+ " from registration sr,testgroupspecification tgs "
										+ " where sr.nallottedspeccode=tgs.nallottedspeccode "
										+ " and sr.npreregno=" + npreregno
										+ " and tgs.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
										+ " and sr.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() ;
		}

		final List<TestGroupSpecification> lstTestGroupSpecification = jdbcTemplate.query(getTestGroupSpecification,
				new TestGroupSpecification());
		return lstTestGroupSpecification;
	}
	
	@Override
	public List<TestGroupSpecification> getTestGroupSpecification(int ntreetemplatemanipulationcode, int npreregno, int ntestGroupSpecRequired)
			throws Exception {

		String getTestGroupSpecification = "";
		String strDefaultSpec = "";
		if(ntestGroupSpecRequired == Enumeration.TransactionStatus.YES.gettransactionstatus()) {	//ALPD-4834,Vishakh, Added ntestGroupSpecRequired to handle show or hide add spec button
			strDefaultSpec = " "+ ntreetemplatemanipulationcode+ " ";
		} 
		else {
			
			strDefaultSpec = " select max(ntemplatemanipulationcode) from treetemplatemanipulation where nproductcode="+ Enumeration.TransactionStatus.NA.gettransactionstatus()+" "
					+ "and nproductcatcode="+Enumeration.Default.DEFAULTSPEC.getDefault()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";
		}
		if (npreregno == 0) {
			getTestGroupSpecification = "select tgs.nallottedspeccode,tgts.nspecsampletypecode,sspecname ,sversion,ntemplatemanipulationcode,tgs.ncomponentrequired "
										+ " from testgroupspecification tgs,approvalconfigrole apr,"
										+ " approvalconfigversion apv,testgroupspecsampletype tgts  "
										+ " where tgts.nallottedspeccode=tgs.nallottedspeccode "			
										+ " and ntemplatemanipulationcode in ("+ strDefaultSpec+") "
										+ " and tgs.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
										+ " and apr.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and tgts.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
										+ " and apr.nlevelno=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
										+ " and apv.napproveconfversioncode = apr.napproveconfversioncode "
										+ " and apv.napproveconfversioncode = tgs.napproveconfversioncode  "
										+ " and tgs.napprovalstatus  = any (select napprovalstatuscode from approvalconfig ap, "
										+ " approvalconfigrole apr,approvalconfigversion apv where  ap.napprovalsubtypecode="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
										+ " and ap.nregsubtypecode=" + Enumeration.TransactionStatus.NA.gettransactionstatus() 
										+ " and ap.nregtypecode="+ Enumeration.TransactionStatus.NA.gettransactionstatus()
										+ " and ap.napprovalconfigcode=apv.napprovalconfigcode and  "
										+ " apr.napproveconfversioncode=apv.napproveconfversioncode    " + ""
										//ALPD-5206 Added by  Neeraj Sample registration screen -> Specification showing wrongly in specific scenario.
										+ " and apv.napproveconfversioncode = tgs.napproveconfversioncode   " + " "
										+ " and apv.ntransactionstatus<>"+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
										+ " and apr.nlevelno="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
										+ " and ap.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
										+ " and apr.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
										+ " and apv.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 									
										+ ") ";
		} else {
			getTestGroupSpecification = " select tgs.nallottedspeccode,tgs.sspecname as text,tgs.sspecname,tgs.ncomponentrequired  "
										+ " from registration sr,testgroupspecification tgs "
										+ " where sr.nallottedspeccode=tgs.nallottedspeccode "
										+ " and sr.npreregno=" + npreregno
										+ " and tgs.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
										+ " and sr.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() ;
		}

		final List<TestGroupSpecification> lstTestGroupSpecification = jdbcTemplate.query(getTestGroupSpecification,
				new TestGroupSpecification());
		return lstTestGroupSpecification;
	}

	public List<TestGroupSpecification> getTestGroupSpecificationBasedOnPortal(int ntreetemplatemanipulationcode,
			int npreregno, int nalloctedspeccode) throws Exception {

		//Yet to include nsitecode in where clause of below queries- L.Subashini 26/04/2025
		String getTestGroupSpecification = "";
		if (npreregno == 0) {
			getTestGroupSpecification = "select tgs.nallottedspeccode,tgts.nspecsampletypecode,sspecname ,"
										+ " sversion,ntemplatemanipulationcode,tgs.ncomponentrequired "
										+ " from testgroupspecification tgs,approvalconfigrole apr,"
										+ " approvalconfigversion apv,testgroupspecsampletype tgts  "
										+ " where tgs.nallottedspeccode=" + nalloctedspeccode
										+ " and  tgts.nallottedspeccode=tgs.nallottedspeccode "
										+ " and ntemplatemanipulationcode in (-2, "
										+ ntreetemplatemanipulationcode + ") "
										+ " and tgs.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
										+ " and apr.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and tgts.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
										+ " and apr.nlevelno=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  "
										+ " apv.napproveconfversioncode = apr.napproveconfversioncode and apv.napproveconfversioncode = tgs.napproveconfversioncode  "
										+ " and tgs.napprovalstatus  = any (select napprovalstatuscode from approvalconfig ap, "
										+ " approvalconfigrole apr,approvalconfigversion apv where  ap.napprovalsubtypecode="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ap.nregsubtypecode="
										+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " and ap.nregtypecode="
										+ Enumeration.TransactionStatus.NA.gettransactionstatus()
										+ " and ap.napprovalconfigcode=apv.napprovalconfigcode and  "
										+ " apr.napproveconfversioncode=apv.napproveconfversioncode   and apv.ntransactionstatus<>"
										+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + " and apr.nlevelno="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ap.nstatus="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and apr.nstatus="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and apv.nstatus="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") ";
		} else {
			getTestGroupSpecification = " select tgs.nallottedspeccode,tgs.sspecname as text,tgs.sspecname,"
										+ " tgs.ncomponentrequired  from registration sr,testgroupspecification tgs "
										+ " where sr.nallottedspeccode=tgs.nallottedspeccode and sr.npreregno=" + npreregno
										+ " and tgs.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
										+ " and sr.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() ;
		}

		final List<TestGroupSpecification> lstTestGroupSpecification = jdbcTemplate.query(getTestGroupSpecification,
				new TestGroupSpecification());
		return lstTestGroupSpecification;
	}
	
	@Override
	public ResponseEntity<Object> getComponentBySpec(Map<String, Object> inputMap) throws Exception {

		Map<String, Object> returnMap = new HashMap<String, Object>();
		int ntemplatemanipulationcode = (int) inputMap.get("ntemplatemanipulationcode");
		int nallottedspeccode = (int) inputMap.get("nallottedspeccode");

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		final Instant date = dateUtilityFunction.getCurrentDateTime(userInfo);
		final String recceivedDate = dateUtilityFunction.instantDateToString(date);

		final String strQuery = "select  tgs.nspecsampletypecode, "// rank() OVER (order by co.ncomponentcode) as slno, "
								+ " co.ncomponentcode,co.scomponentname," + ntemplatemanipulationcode
								+ " as ntemplatemanipulationcode, " + " " + nallottedspeccode + " as nallottedspeccode ,"
								+ userInfo.getNtimezonecode() + " as ntzdreceivedate "
								+ " from testgroupspecsampletype tgs, component co where co.ncomponentcode = tgs.ncomponentcode"
								+ " and co.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and tgs.nallottedspeccode = " + nallottedspeccode 
								+ " and tgs.nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and co.nsitecode = " + userInfo.getNmastersitecode()
								+ " and tgs.nsitecode = " + userInfo.getNmastersitecode()
								+ " order by tgs.ncomponentcode asc ";

		final List<Component> lstComponent = jdbcTemplate.query(strQuery, new Component());
		
		final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());
		final String fromDateUI = LocalDateTime.parse(recceivedDate, dbPattern).format(uiPattern);
		
		lstComponent.stream().forEach(x -> {
			x.setDreceiveddate(date);
			x.setSreceiveddate(fromDateUI);
		});
		returnMap.put("lstComponent", lstComponent);
		
		return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
	}
	
	@Override
	public ResponseEntity<Object> getTestfromDB(Map<String, Object> inputMap) throws Exception {
		String strQuery = "";
		
		boolean needSubSample = (boolean) inputMap.get("nneedsubsample");
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		
		if ((boolean) inputMap.get("specBasedComponent") && needSubSample) {
			final int nspecsampletypecode = (Integer) inputMap.get("nspecsampletypecode");
			strQuery = " select tgt.nrepeatcountno,tgt.nsectioncode,tgt.nmethodcode,tgt.ninstrumentcatcode,"
					+ " tgt.ntestgrouptestcode,tgt.nspecsampletypecode," + " ("
					+ "        select count(tgtp.ntestgrouptestparametercode) "
					+ "        from testgrouptestparameter tgtp "
					+ "        where  tgtp.ntestgrouptestcode=tgt.ntestgrouptestcode  and tgtp.nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and  tgtp.nisvisible="+Enumeration.TransactionStatus.YES.gettransactionstatus()
					+ " and tgtp.nsitecode=" + userInfo.getNmastersitecode()
					+ " ) as nparametercount,"
					+ " tgt.ntestcode,tgt.stestsynonym,tgt.nsectioncode,s.ssectionname, m.smethodname,"
					+ " ic.sinstrumentcatname,tgt.ncost "
					+ " from testgrouptest tgt,section s,method m,instrumentcategory ic, testmaster tm  "
					+ " where s.nsectioncode = tgt.nsectioncode and m.nmethodcode =tgt.nmethodcode "
					+ " and ic.ninstrumentcatcode = tgt.ninstrumentcatcode "
					+ " and tgt.nspecsampletypecode =  "+ nspecsampletypecode 
					+ " and tgt.nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and tgt.nisvisible="+Enumeration.TransactionStatus.YES.gettransactionstatus()
					+ " and tm.ntestcode = tgt.ntestcode "
					+ " and tm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and tm.ntransactionstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and m.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and ic.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and s.nsitecode = " + userInfo.getNmastersitecode()
					+ " and m.nsitecode = " + userInfo.getNmastersitecode()
					+ " and ic.nsitecode = " + userInfo.getNmastersitecode()
					+ " and tm.nsitecode = " + userInfo.getNmastersitecode() 
					+ " and tgt.nsitecode = " + userInfo.getNmastersitecode() 
					+ " order by tgt.nsorter";
		} else {
			strQuery = " select tgt.nrepeatcountno,tgt.nsectioncode,tgt.nmethodcode,tgt.ninstrumentcatcode,"
					+ " tgt.ntestgrouptestcode,tgt.nspecsampletypecode," + " ("
					+ "        select count(tgtp.ntestgrouptestparametercode) "
					+ "        from testgrouptestparameter tgtp "
					+ "        where  tgtp.ntestgrouptestcode=tgt.ntestgrouptestcode and tgtp.nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and tgtp.nsitecode=" + userInfo.getNmastersitecode()
					+ " and  tgtp.nisvisible="+Enumeration.TransactionStatus.YES.gettransactionstatus()
					+" ) as nparametercount,"
					+ " tgt.ntestcode,tgt.stestsynonym,tgt.nsectioncode,s.ssectionname, m.smethodname,"
					+ " ic.sinstrumentcatname "
					+ " from testgrouptest tgt,section s,method m,instrumentcategory ic, testmaster tm ,testgroupspecsampletype tgts "
					+ " where s.nsectioncode = tgt.nsectioncode  and m.nmethodcode =tgt.nmethodcode "
					+ " and ic.ninstrumentcatcode = tgt.ninstrumentcatcode and tgt.nspecsampletypecode = tgts.nspecsampletypecode"
					+ " and tgt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and tm.ntestcode = tgt.ntestcode  and tgt.nisvisible="+Enumeration.TransactionStatus.YES.gettransactionstatus()
					+" and tm.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and tm.ntransactionstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and m.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and ic.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and s.nsitecode = " + userInfo.getNmastersitecode()
					+ " and m.nsitecode = " + userInfo.getNmastersitecode()
					+ " and ic.nsitecode = " + userInfo.getNmastersitecode()
					+ " and tm.nsitecode = " + userInfo.getNmastersitecode() 
					+ " and tgt.nsitecode = " + userInfo.getNmastersitecode() 
					+ " and tgts.nallottedspeccode="+ inputMap.get("nallottedspeccode")
					+ " order by tgt.nsorter";
		}

		final List<TestGroupTestForSample> lstTestGroupTest = jdbcTemplate.query(strQuery,
				new TestGroupTestForSample());

		return new ResponseEntity<Object>(lstTestGroupTest, HttpStatus.OK);
	}



	@Override
	public ResponseEntity<Object> getTestfromTestPackage(Map<String, Object> inputMap) throws Exception {
		
		String strQuery = "";
		
		final Map<String, Object> returnMap = new HashMap<String, Object>();
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});

		if ((boolean) inputMap.get("specBasedComponent")) {
			strQuery = " select tgt.ntestpackagecode,tp.stestpackagename from testgrouptest tgt, testpackage tp "
								+ " where tp.ntestpackagecode=tgt.ntestpackagecode  and tgt.nspecsampletypecode in ("
								+ inputMap.get("nspecsampletypecode") + ") "
								+ " and tgt.nstatus =  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and tp.nstatus =  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and tgt.nsitecode = " + userInfo.getNmastersitecode()
								+ " and tp.nsitecode = " + userInfo.getNmastersitecode()
								+ " and tgt.ntestpackagecode<>-1 group by tgt.ntestpackagecode,tp.stestpackagename ";
		} else {

			strQuery = " select tgt.ntestpackagecode,tp.stestpackagename,tgt.nspecsampletypecode "
								+ " from testgrouptest tgt,testpackage tp,testgroupspecsampletype tgts "
								+ " where tp.ntestpackagecode=tgt.ntestpackagecode and " + " tgts.nallottedspeccode= "
								+ inputMap.get("nallottedspeccode") + " and tgt.nspecsampletypecode = tgts.nspecsampletypecode  "
								+ " and tgt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and tp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and tgts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and tgt.nsitecode = " + userInfo.getNmastersitecode()
								+ " and tp.nsitecode = " + userInfo.getNmastersitecode()
								+ " and tgts.nsitecode = " + userInfo.getNmastersitecode()
								+ " and tgt.ntestpackagecode <> -1 and tgts.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " group by tgt.ntestpackagecode,tp.stestpackagename,tgt.nspecsampletypecode";
		}
		returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
				Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		returnMap.put("TestPackage", jdbcTemplate.query(strQuery, new TestGroupTestForSample()));
		
		return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getRegTypeBySampleType(Map<String, Object> inputMap) throws Exception {

		final Map<String, Object> objmap = new HashMap<String, Object>();
		List<RegistrationType> lstRegistrationType = new ArrayList<RegistrationType>();
		List<RegistrationSubType> lstRegistrationSubType = new ArrayList<RegistrationSubType>();
		List<ReactRegistrationTemplate> lstReactRegistrationTemplate = new ArrayList<ReactRegistrationTemplate>();
		List<ApprovalConfigAutoapproval> listApprovalConfigAutoapproval = new ArrayList<ApprovalConfigAutoapproval>();
		List<TransactionStatus> listTransactionstatus = new ArrayList<TransactionStatus>();
		
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		final int nsampletypecode = (int) inputMap.get("nsampletypecode");		
		lstRegistrationType = transactionDAOSupport.getRegistrationType(nsampletypecode, userInfo);

		if (lstRegistrationType != null && lstRegistrationType.size() > 0) {
			lstRegistrationSubType = transactionDAOSupport.getRegistrationSubType(lstRegistrationType.get(0).getNregtypecode(), userInfo);
			
			objmap.put("RegTypeValue", lstRegistrationType.get(0));			
			objmap.put("RegSubTypeValue", lstRegistrationSubType.get(0));
			objmap.put("nregsubtypeversioncode", lstRegistrationSubType.get(0).getNregsubtypeversioncode());
			listTransactionstatus = transactionDAOSupport.getFilterStatus(lstRegistrationType.get(0).getNregtypecode(),
					lstRegistrationSubType.get(0).getNregsubtypecode(), userInfo);
			objmap.put("FilterStatus", listTransactionstatus);
			objmap.put("FilterStatusValue", listTransactionstatus.get(0));

			listApprovalConfigAutoapproval = transactionDAOSupport.getApprovalConfigVersion(lstRegistrationType.get(0).getNregtypecode(),
					lstRegistrationSubType.get(0).getNregsubtypecode(), userInfo);
			objmap.put("ApprovalConfigVersion", listApprovalConfigAutoapproval);
			objmap.put("ApprovalConfigVersionValue", listApprovalConfigAutoapproval.get(0));

			objmap.put("napproveconfversioncode", listApprovalConfigAutoapproval.get(0).getNapproveconfversioncode());
			
			lstReactRegistrationTemplate = (List<ReactRegistrationTemplate>) transactionDAOSupport.getApproveConfigVersionRegTemplate(
					lstRegistrationType.get(0).getNregtypecode(), lstRegistrationSubType.get(0).getNregsubtypecode(),
					listApprovalConfigAutoapproval.get(0).getNapproveconfversioncode()).getBody();

			objmap.put("DesignTemplateMapping", lstReactRegistrationTemplate);
			if (lstReactRegistrationTemplate.size() > 0) {
				objmap.put("DesignTemplateMappingValue", lstReactRegistrationTemplate.get(0));
			}	
		}
		
		else {
			objmap.put("RegistrationSubType", null);
			objmap.put("RegSubTypeValue", null);
			objmap.put("RegistrationType", null);
			objmap.put("RegTypeValue",null);	
			objmap.put("ApprovalConfigVersion", null);
			objmap.put("ApprovalConfigVersionValue",null);
			objmap.put("FilterStatus", null);
			objmap.put("FilterStatusValue", null);
			objmap.put("napproveconfversioncode", null);
			objmap.put("DesignTemplateMapping", null);
			objmap.put("DesignTemplateMappingValue", null);
			
			return new ResponseEntity<Object>(objmap, HttpStatus.OK);
		}
				
		objmap.put("RegistrationSubType", lstRegistrationSubType);
		objmap.put("RegistrationType", lstRegistrationType);
		
		return new ResponseEntity<Object>(objmap, HttpStatus.OK);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getRegTemplateTypeByRegSubType(Map<String, Object> inputMap) throws Exception {
		
		Map<String, Object> objmap = new HashMap<String, Object>();
		int nregtypecode = (int) inputMap.get("nregtypecode");
		int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
		
		final ObjectMapper objMapper = new ObjectMapper();
		List<TransactionStatus> listTransactionstatus = new ArrayList<TransactionStatus>();
		List<ReactRegistrationTemplate> lstReactRegistrationTemplate = new ArrayList<ReactRegistrationTemplate>();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		List<ApprovalConfigAutoapproval> listApprovalConfigAutoapproval = new ArrayList<ApprovalConfigAutoapproval>();

		listApprovalConfigAutoapproval = transactionDAOSupport.getApprovalConfigVersion(nregtypecode, nregsubtypecode, userInfo);
		objmap.put("ApprovalConfigVersion", listApprovalConfigAutoapproval);
		objmap.put("ApprovalConfigVersionValue", listApprovalConfigAutoapproval.get(0));

		objmap.put("napproveconfversioncode", listApprovalConfigAutoapproval.get(0).getNapproveconfversioncode());

		listTransactionstatus = transactionDAOSupport.getFilterStatus(nregtypecode, nregsubtypecode, userInfo);

		lstReactRegistrationTemplate = (List<ReactRegistrationTemplate>) transactionDAOSupport.getApproveConfigVersionRegTemplate(
				nregtypecode, nregsubtypecode, listApprovalConfigAutoapproval.get(0).getNapproveconfversioncode())
						.getBody();

		objmap.put("DesignTemplateMapping", lstReactRegistrationTemplate);
		objmap.put("DesignTemplateMappingValue", lstReactRegistrationTemplate.get(0));

		objmap.put("FilterStatusValue", listTransactionstatus.get(0));
		objmap.put("FilterStatus", listTransactionstatus);
		
		return new ResponseEntity<Object>(objmap, HttpStatus.OK);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getApprovalConfigBasedTemplateDesign(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		
		Map<String, Object> objmap = new HashMap<String, Object>();
		int nregtypecode = (int) inputMap.get("nregtypecode");
		int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
		int napproveconfversioncode = (int) inputMap.get("napproveconfversioncode");
		
		List<ReactRegistrationTemplate> lstReactRegistrationTemplate = new ArrayList<ReactRegistrationTemplate>();
		
		lstReactRegistrationTemplate = (List<ReactRegistrationTemplate>) transactionDAOSupport.getApproveConfigVersionRegTemplate(
				nregtypecode, nregsubtypecode, napproveconfversioncode).getBody();

		objmap.put("DesignTemplateMapping", lstReactRegistrationTemplate);
		if (!lstReactRegistrationTemplate.isEmpty()) {
			objmap.put("DesignTemplateMappingValue", lstReactRegistrationTemplate.get(0));
		} else {
			objmap.put("DesignTemplateMappingValue", null);
		}

		return new ResponseEntity<Object>(objmap, HttpStatus.OK);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getRegSubTypeByRegType(Map<String, Object> inputMap) throws Exception {

		final Map<String, Object> objmap = new HashMap<String, Object>();
		List<RegistrationSubType> lstRegistrationSubType = new ArrayList<RegistrationSubType>();
		final int nregtypecode = (int) inputMap.get("nregtypecode");

		final ObjectMapper objMapper = new ObjectMapper();
		List<TransactionStatus> listTransactionstatus = new ArrayList<TransactionStatus>();
		List<ApprovalConfigAutoapproval> listApprovalConfigAutoapproval = new ArrayList<ApprovalConfigAutoapproval>();
		List<ReactRegistrationTemplate> lstReactRegistrationTemplate = new ArrayList<ReactRegistrationTemplate>();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		lstRegistrationSubType = transactionDAOSupport.getRegistrationSubType(nregtypecode, userInfo);
		if (lstRegistrationSubType != null && lstRegistrationSubType.size() > 0) {
			objmap.put("RegistrationSubType", lstRegistrationSubType);
			objmap.put("RegSubTypeValue", lstRegistrationSubType.get(0));

			listTransactionstatus = transactionDAOSupport.getFilterStatus(nregtypecode, lstRegistrationSubType.get(0).getNregsubtypecode(),
					userInfo);

			listApprovalConfigAutoapproval = transactionDAOSupport.getApprovalConfigVersion(nregtypecode,
					lstRegistrationSubType.get(0).getNregsubtypecode(), userInfo);
			objmap.put("ApprovalConfigVersion", listApprovalConfigAutoapproval);
			objmap.put("ApprovalConfigVersionValue", listApprovalConfigAutoapproval.get(0));

			objmap.put("napproveconfversioncode", listApprovalConfigAutoapproval.get(0).getNapproveconfversioncode());
			// objmap.put("RealApprovalConfigVersionValue",
			// listApprovalConfigAutoapproval.get(0));

			lstReactRegistrationTemplate = (List<ReactRegistrationTemplate>) transactionDAOSupport.getApproveConfigVersionRegTemplate(
					nregtypecode, lstRegistrationSubType.get(0).getNregsubtypecode(),
					listApprovalConfigAutoapproval.get(0).getNapproveconfversioncode()).getBody();

			objmap.put("DesignTemplateMapping", lstReactRegistrationTemplate);
			objmap.put("DesignTemplateMappingValue", lstReactRegistrationTemplate.get(0));

			objmap.put("FilterStatusValue", listTransactionstatus.get(0));
			objmap.put("FilterStatus", listTransactionstatus);
		} else {
			objmap.put("RegistrationSubType", lstRegistrationSubType);
			objmap.put("RegSubTypeValue", Arrays.asList());
			objmap.put("DesignTemplateMapping", Arrays.asList());
			objmap.put("DesignTemplateMappingValue", Arrays.asList());
			objmap.put("FilterStatusValue", Arrays.asList());
			objmap.put("FilterStatus", Arrays.asList());
			objmap.put("ApprovalConfigVersion", Arrays.asList());
			objmap.put("ApprovalConfigVersionValue", Arrays.asList());
			objmap.put("napproveconfversioncode", Arrays.asList());

		}
		return new ResponseEntity<Object>(objmap, HttpStatus.OK);
	}
	
	@Override
	public ResponseEntity<Object> getRegistrationByFilterSubmit(Map<String, Object> uiMap) throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userinfo = objMapper.convertValue(uiMap.get("userinfo"), UserInfo.class);

		final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userinfo.getSdatetimeformat());

		String fromDate = LocalDateTime.parse((String) uiMap.get("FromDate"), dbPattern).format(uiPattern);
		String toDate = LocalDateTime.parse((String) uiMap.get("ToDate"), dbPattern).format(uiPattern);

		returnMap.put("RealFromDate", fromDate);
		returnMap.put("RealToDate", toDate);

		fromDate = dateUtilityFunction.instantDateToString(
				dateUtilityFunction.convertStringDateToUTC((String) uiMap.get("FromDate"), userinfo, true));
		toDate = dateUtilityFunction.instantDateToString(
				dateUtilityFunction.convertStringDateToUTC((String) uiMap.get("ToDate"), userinfo, true));
		uiMap.put("FromDate", fromDate);
		uiMap.put("ToDate", toDate);

		int nsampletypecode = -1;
		int nregsubtype = -1;
		int nregtype = -1;
		int ndesigntemplatemappingcode = -1;

		if (uiMap.containsKey("nsampletypecode")) {
			nsampletypecode = (int) uiMap.get("nsampletypecode");
		}

		if (uiMap.containsKey("nregsubtypecode")) {
			nregsubtype = (int) uiMap.get("nregsubtypecode");
		}

		if (uiMap.containsKey("nregtypecode")) {
			nregtype = (int) uiMap.get("nregtypecode");
		}

		if (uiMap.containsKey("ndesigntemplatemappingcode")) {
			ndesigntemplatemappingcode = (int) uiMap.get("ndesigntemplatemappingcode");
		}

		ReactRegistrationTemplate lstTemplate = (ReactRegistrationTemplate) transactionDAOSupport.getRegistrationTemplate(nregtype,
				nregsubtype).getBody();
		if (lstTemplate != null) {
			returnMap.put("ndesigntemplatemappingcode", lstTemplate.getNdesigntemplatemappingcode());
		}
		
		List<FilterName> lstFilterName= projectDAOSupport.getFavoriteFilterName(userinfo);
		returnMap.put("FilterName",lstFilterName);
   		
		ReactRegistrationTemplate lstTemplate1 = (ReactRegistrationTemplate) transactionDAOSupport.getRegistrationTemplatebasedontemplatecode(
				nregtype, nregsubtype, ndesigntemplatemappingcode).getBody();

		if (lstTemplate1 != null) {
			returnMap.put("registrationTemplate", lstTemplate1);
			returnMap.put("SubSampleTemplate", transactionDAOSupport.getRegistrationSubSampleTemplate(ndesigntemplatemappingcode).getBody());
			returnMap.put("DynamicDesign", projectDAOSupport.getTemplateDesign(ndesigntemplatemappingcode, userinfo.getNformcode()));
			returnMap.putAll(registrationDAOSupport.getDynamicRegistration(uiMap, userinfo));
		} else {
			returnMap.put("registrationTemplate", new ArrayList<>());
			returnMap.put("SubSampleTemplate", new ArrayList<>());
			returnMap.put("DynamicDesign", null);
			returnMap.put("selectedSample", Arrays.asList());
			returnMap.put("selectedSubSample", Arrays.asList());
			returnMap.put("RegistrationGetSubSample", new ArrayList<>());
			returnMap.put("RegistrationGetSample", new ArrayList<>());
			returnMap.put("RegistrationGetTest", new ArrayList<>());
			returnMap.put("selectedTest", Arrays.asList());
			returnMap.put("RegistrationParameter", Arrays.asList());
			returnMap.put("ndesigntemplatemappingcode", null);
		}
		//ALPD-4912-To insert data when filter submit,done by Dhanushya RI
		if(uiMap.containsKey("saveFilterSubmit") &&  (boolean) uiMap.get("saveFilterSubmit") ==true) {
			projectDAOSupport.createFilterSubmit(uiMap,userinfo);
			}
		if (uiMap.containsKey("flag")) {
			returnMap.put("RegistrationType", transactionDAOSupport.getRegistrationType(nsampletypecode, userinfo));
			returnMap.put("RegistrationSubType", transactionDAOSupport.getRegistrationSubType(nregtype, userinfo));
		}
		return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
	}
	
	@Override
	public ResponseEntity<Object> getRegistrationParameter(final String ntransactionTestCode, UserInfo userInfo)
			throws Exception {		

		return transactionDAOSupport.getRegistrationParameter(ntransactionTestCode, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getMoreTestPackage(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		String strtestpackageQuery = "";
//		  strtestpackageQuery = " select tpt.ntestpackagecode,tp.stestpackagename"
//				+ " from testgrouptest tgt,testmaster tm,testpackage tp,testpackagetest tpt "
//				+ "	where tp.ntestpackagecode=tpt.ntestpackagecode " + "	and tm.ntestcode=tpt.ntestcode "
//				+ "	and tgt.ntestcode=tpt.ntestcode " + " and tgt.nspecsampletypecode in ("
//				+ inputMap.get("snspecsampletypecode") + ") and tm.ntestcode = tgt.ntestcode " + " and tgt.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and tpt.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and tp.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and tm.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and tm.ntransactionstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ " group by tpt.ntestpackagecode,tp.stestpackagename ";
		strtestpackageQuery = " select tgt.ntestpackagecode,tp.stestpackagename from testgrouptest tgt,testpackage tp "
							+ " where tp.ntestpackagecode=tgt.ntestpackagecode and tgt.nspecsampletypecode in ("
							+ inputMap.get("snspecsampletypecode") + ") "
							+ " and tgt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
							+ " and tp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
							+ " and tgt.nsitecode = " + userInfo.getNmastersitecode()
							+ " and tp.nsitecode = " + userInfo.getNmastersitecode()
							+ " and tgt.ntestpackagecode <> -1 "
							+ " group by tgt.ntestpackagecode,tp.stestpackagename ";
		// returnMap.put("TestPackage",jdbcTemplate.query(strtestpackageQuery,new
		// TestGroupTestForSample()));
		final List<TestGroupTestForSample> lstTestPackage = jdbcTemplate.query(strtestpackageQuery,
				new TestGroupTestForSample());
		return new ResponseEntity<Object>(lstTestPackage, HttpStatus.OK);
	}
	
	
	public ResponseEntity<Object> getMoreTest(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {		
		
		final String sTestQuery = "select tgt.nrepeatcountno," // 1 as nrepeatcountno, "
							+ " tgt.ntestgrouptestcode, " + " tgt.stestsynonym, tgt.nspecsampletypecode, tgt.nsectioncode, "
							+ " (select count(tgtp.ntestgrouptestparametercode) from testgrouptestparameter tgtp "
							+ " where  tgtp.ntestgrouptestcode=tgt.ntestgrouptestcode " 
							+ " and tgtp.nstatus ="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
							+ " and tgtp.nsitecode = " + userInfo.getNmastersitecode()
							+ " and  tgtp.nisvisible="+Enumeration.TransactionStatus.YES.gettransactionstatus()+") as nparametercount "
							+ " from testgrouptest tgt, testgroupspecsampletype tgsst, testmaster tm"
							+ " where tm.ntestcode = tgt.ntestcode and tgsst.nspecsampletypecode = tgt.nspecsampletypecode "
							+ " and tgsst.nspecsampletypecode in (" + inputMap.get("snspecsampletypecode")
							+ ") and tm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and tgt.nstatus  = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and tgsst.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and tgt.nisvisible="+Enumeration.TransactionStatus.YES.gettransactionstatus()
							+ " and tgt.nsitecode = " + userInfo.getNmastersitecode()
							+ " and tm.nsitecode = " + userInfo.getNmastersitecode()
							+ " and tgsst.nsitecode = " + userInfo.getNmastersitecode()
							+ " and tm.ntransactionstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return new ResponseEntity<Object>(jdbcTemplate.query(sTestQuery, new TestGroupTest()), HttpStatus.OK);
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> validateAndInsertSeqNoRegistrationData(Map<String, Object> inputMap) throws Exception {

				
		Map<String, Object> returnMap = new HashMap<String, Object>();
		final ObjectMapper objectMapper = new ObjectMapper();
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});

	
		final List<TestGroupTest> listTest = objectMapper.convertValue(inputMap.get("testgrouptest"),
				new TypeReference<List<TestGroupTest>>() {
				});

		Boolean skipMethodValidity = null;
		if (inputMap.containsKey("skipmethodvalidity")) {
			skipMethodValidity = (Boolean) inputMap.get("skipmethodvalidity");
		}

		final String sntestgrouptestcode = stringUtilityFunction.fnDynamicListToString(listTest, "getNtestgrouptestcode");

		List<TestGroupTest> expiredMethodTestList = new ArrayList<TestGroupTest>();
		if (skipMethodValidity == false) {
			expiredMethodTestList = transactionDAOSupport.getTestByExpiredMethod(sntestgrouptestcode, userInfo);

		}
		if (expiredMethodTestList.isEmpty()) {
			String sQuery = " lock  table lockpreregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			sQuery = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			sQuery = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			sQuery = " lock  table lockcancelreject " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			List<RegistrationSample> subSampleInputList = objectMapper.convertValue(inputMap.get("RegistrationSample"),
					new TypeReference<List<RegistrationSample>>() {
					});

			final Registration registration = objectMapper.convertValue(inputMap.get("Registration"),
					new TypeReference<Registration>() {
					});
			List<TestGroupTest> testGroupTestInputList = objectMapper.convertValue(inputMap.get("testgrouptest"),
					new TypeReference<List<TestGroupTest>>() {
					});

			int testCount = testGroupTestInputList.stream().mapToInt(
					testgrouptest -> testgrouptest.getNrepeatcountno() == 0 ? 1 : testgrouptest.getNrepeatcountno())
					.sum();

			int parameterCount = testGroupTestInputList.stream().mapToInt(
					testgrouptest -> ((testgrouptest.getNrepeatcountno() == 0 ? 1 : testgrouptest.getNrepeatcountno())
							* testgrouptest.getNparametercount()))
					.sum();

			List<Map<String, Object>> samplecombinationunique = (List<Map<String, Object>>) inputMap
					.get("samplecombinationunique");
			List<Map<String, Object>> subsamplecombinationunique = (List<Map<String, Object>>) inputMap
					.get("subsamplecombinationunique");
			Map<String, Object> map = projectDAOSupport.validateUniqueConstraint(samplecombinationunique,
					(Map<String, Object>) inputMap.get("Registration"), userInfo, "create", Registration.class,
					"npreregno", false);
			if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
					.equals(map.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
				// for normal preregister flow if we sent 1 means,order sample already
				// preregitered so current sample also sample same order so store as sub sample
				// in that specific order (only for clinic type)
				map.put("nflag", 2);
				return map;
			}
			List<Map<String, Object>> registrationsam = (List<Map<String, Object>>) inputMap.get("RegistrationSample");
			for (int i = 0; i < subSampleInputList.size(); i++) {
				Map<String, Object> map1 = projectDAOSupport.validateUniqueConstraint(subsamplecombinationunique,
						(Map<String, Object>) registrationsam.get(i), userInfo, "create", RegistrationSample.class,
						"npreregno", false);
				if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
						.equals(map1.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
					// for normal preregister flow if we sent 1 means,order sample already
					// preregitered so current sample also sample same order so store as sub sample
					// in that specific order (only for clinic type)
					map1.put("nflag", 2);
					return map1;
				}
			}
			JSONObject objJson = new JSONObject(registration.getJsondata());

			String manualOrderInsert = "";
			int nsampletypecode = (int) inputMap.get("nsampletypecode");
			if (registration.getJsonuidata().containsKey("Order Type")) {
				if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()
						&& ((int) ((JSONObject) objJson.get("Order Type")).getInt("value") == 1
								|| (int) ((JSONObject) objJson.get("Order Type")).getInt("value") == -1))
					manualOrderInsert = ",'externalordertest','externalordersample'";
			}
			Boolean validatePreventTB = true;
			if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()
					&& inputMap.containsKey("extrenalOrderTypeCode")) {
				if ((int) inputMap.get("extrenalOrderTypeCode") == 2) {
					String str = "select * from preventTBsettings where nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					List<Registration> lstData = jdbcTemplate.query(str, new Registration());
					if (!lstData.isEmpty()) {
						validatePreventTB = lstData.stream()
								.anyMatch(x -> x.getNproductcatcode() == registration.getNproductcatcode()
										&& subSampleInputList.stream()
												.anyMatch(y -> x.getNcomponentcode() == y.getNcomponentcode())
										&& testGroupTestInputList.stream()
												.anyMatch(z -> x.getNtestcode() == z.getNtestcode()));
					}
				}
			}
			if (!validatePreventTB) {
				returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), commonFunction
						.getMultilingualMessage("IDS_INVALIDCOMPONENTTEST", userInfo.getSlanguagefilename()));
				returnMap.put("nflag", 2);
				return returnMap;
			}
		
			//RegistrationSample externalorderList = null;
			List<RegistrationHistory> lstStatus = null;
			if (registration.getJsonuidata().containsKey("Order Type")
					&& nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()
					&& (int) ((JSONObject) objJson.get("Order Type")).getInt("value") == 2) {
				
				final String squerystatus = "select npreregno, ntransactionstatus from registrationhistory where"
											+ " nreghistorycode in (select max(nreghistorycode) from registrationhistory r"
											+ " where r.npreregno in (select npreregno from registration where nstatus="
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " and jsondata->>'OrderCodeData'='"
											+ registration.getJsondata().get("OrderCodeData") + "') and nsitecode="
											+ userInfo.getNtranssitecode() + " group by r.npreregno) and  ntransactionstatus not in ("
											+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ","
											+ Enumeration.TransactionStatus.RELEASED.gettransactionstatus() + ","
											+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ")";
				lstStatus = jdbcTemplate.query(squerystatus, new RegistrationHistory());
				for (int p = 0; p < lstStatus.size(); p++) {
					if (lstStatus != null
							&& lstStatus.get(p).getNtransactionstatus() != Enumeration.TransactionStatus.CANCELED
									.gettransactionstatus()
							&& lstStatus.get(p).getNtransactionstatus() != Enumeration.TransactionStatus.REJECTED
									.gettransactionstatus()
							&& lstStatus.get(p).getNtransactionstatus() != Enumeration.TransactionStatus.RELEASED
									.gettransactionstatus()) {
						inputMap.put("nfilterstatus", lstStatus.get(0).getNtransactionstatus());
						inputMap.put("npreregno", Integer.toString(lstStatus.get(0).getNpreregno()));
						inputMap.put("npreregnocount", lstStatus.get(0).getNpreregno());
						inputMap.put("masterData", inputMap.get("DataRecordMaster"));
					
						return (Map<String, Object>) externalOrderSupport.externalOrderSampleExtisting(inputMap);
						
					}
				}

			}
			final String strSelectSeqno = "select stablename,nsequenceno from seqnoregistration  where stablename "
					+ "in ('registration','registrationhistory','registrationparameter','registrationsample',"
					+ "'registrationsamplehistory','registrationtest','registrationdecisionhistory','registrationtesthistory' "
					+ manualOrderInsert + ") and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			//final List<?> lstMultiSeqNo = findByMultiTablesPlainSqlList(strSelectSeqno, SeqNoRegistration.class);

//			final List<?> lstMultiSeqNo = (List<?>) projectDAOSupport.getMultipleEntitiesResultSetInList(strSelectSeqno, jdbcTemplate, SeqNoRegistration.class);
//			final List<SeqNoRegistration> lstSeqNoReg = (List<SeqNoRegistration>) lstMultiSeqNo.get(0);
//
//			returnMap = lstSeqNoReg.stream().collect(Collectors.toMap(SeqNoRegistration::getStablename,
//					SeqNoRegistration -> SeqNoRegistration.getNsequenceno()));
			
			final List<SeqNoRegistration> lstSeqNo = jdbcTemplate.query(strSelectSeqno, new SeqNoRegistration());
			
			returnMap = lstSeqNo.stream().collect(Collectors.toMap(SeqNoRegistration::getStablename,
					SeqNoRegistration -> SeqNoRegistration.getNsequenceno()));

			String strSeqnoUpdate = "Update seqnoregistration set nsequenceno = "
									+ ((int) returnMap.get("registration") + 1) + " where stablename = 'registration';"
									+ " Update seqnoregistration set nsequenceno = "
									+ ((int) returnMap.get("registrationdecisionhistory") + 1)
									+ " where stablename = 'registrationdecisionhistory';"
									+ " Update seqnoregistration set nsequenceno = " + ((int) returnMap.get("registrationhistory") + 1)
									+ " where stablename = 'registrationhistory';" + "Update seqnoregistration set nsequenceno = "
									+ ((int) returnMap.get("registrationparameter") + parameterCount)
									+ " where stablename = 'registrationparameter';" + "Update seqnoregistration set nsequenceno = "
									+ ((int) returnMap.get("registrationsample") + subSampleInputList.size())
									+ " where stablename = 'registrationsample';" + "Update seqnoregistration set nsequenceno = "
									+ ((int) returnMap.get("registrationsamplehistory") + subSampleInputList.size())
									+ " where stablename = 'registrationsamplehistory';" + "Update seqnoregistration set nsequenceno = "
									+ ((int) returnMap.get("registrationtest")// + TestGroupTests.size()
											+ testCount)
									+ " where stablename = 'registrationtest' ;" + "Update seqnoregistration set nsequenceno = "
									+ ((int) returnMap.get("registrationtesthistory") // + TestGroupTests.size()
											+ testCount)
									+ " where stablename = 'registrationtesthistory';";
			if (registration.getJsonuidata().containsKey("Order Type")) {
				if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()
						&& ((int) ((JSONObject) objJson.get("Order Type")).getInt("value") == Enumeration.OrderType.INTERNAL.getOrderType()
								|| (int) ((JSONObject) objJson.get("Order Type")).getInt("value") == -1))
					strSeqnoUpdate = strSeqnoUpdate + "Update seqnoregistration set nsequenceno = "
							+ ((int) returnMap.get("externalordersample") // + TestGroupTests.size()
									+ subSampleInputList.size())
							+ " where stablename = 'externalordersample';"
							+ " Update seqnoregistration set nsequenceno = " + ((int) returnMap.get("externalordertest") // +																					// TestGroupTests.size()
									+ testGroupTestInputList.size())
							+ " where stablename = 'externalordertest';";
			}

			jdbcTemplate.execute(strSeqnoUpdate);

			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.SUCCESS.getreturnstatus());

		} else {
			final String expiredMethod = stringUtilityFunction.fnDynamicListToString(expiredMethodTestList, "getSmethodname");
			returnMap.put("NeedConfirmAlert", true);
			final String message = commonFunction.getMultilingualMessage("IDS_TESTWITHEXPIREDMETHODCONFIRM",
					userInfo.getSlanguagefilename());
			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					message.concat(" " + expiredMethod + " ?"));
		}
	
		returnMap.put("nflag", 2);
		return returnMap;
	}
	
	


	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> preRegisterSampleWithFile(MultipartHttpServletRequest inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		
		final UserInfo userInfo = objectMapper.readValue(inputMap.getParameter("userinfo"), new TypeReference<UserInfo>() {});
		
		final Object reg = objectMapper.readValue(inputMap.getParameter("Map"), new TypeReference<Object>() {});			
		
		final String uploadStatus = ftpUtilityFunction.getFileFTPUpload(inputMap, -1, userInfo);

		if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(uploadStatus)) {
			return (ResponseEntity<Object>) preRegisterSample((Map<String, Object>) reg);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(uploadStatus, userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}		
	}
	
	// ALPD-2046
	@Override
	public ResponseEntity<Object> preRegisterSample(Map<String, Object> inputMap) throws Exception {

			
			Map<String, Object> objmap1 = new HashMap<>();
			Map<String, Object> objmap = validateAndInsertSeqNoRegistrationData(inputMap);
			// using flag decide whether normal preregister flow or customized clinical flow
			// if flag=1 means current sample order already preregistred so this sample
			// stored as a sub sample for that respective order(only for clinical sample
			// type).
			if ((int) objmap.get("nflag") != 1) {
				if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
						.equals(objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
					inputMap.putAll(objmap);
					objmap1 = insertRegistration(inputMap);
					return new ResponseEntity<>(objmap1, HttpStatus.OK);
				} else {
					if (objmap.containsKey("NeedConfirmAlert") && (Boolean) objmap.get("NeedConfirmAlert") == true) {
					
						return new ResponseEntity<>(objmap, HttpStatus.EXPECTATION_FAILED);
					} else {
						objmap1.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
								objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()));
						return new ResponseEntity<>(objmap1, HttpStatus.OK);
					}
				}
			} else {
				objmap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
				return new ResponseEntity<>(objmap, HttpStatus.OK);
			}

		}
	
	/**
	 * Preregister Sample
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map<String, Object> insertRegistration(Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		Map<String, Object> returnMap = new HashMap<>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObject = new JSONObject();

		final ObjectMapper objmap = new ObjectMapper();
		objmap.registerModule(new JavaTimeModule());

		final UserInfo userInfo = objmap.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});

		final Registration registration = objmap.convertValue(inputMap.get("Registration"),
				new TypeReference<Registration>() {});
		registration.setNisiqcmaterial((short) Enumeration.TransactionStatus.NO.gettransactionstatus());

		final List<RegistrationSample> registrationSample = objmap.convertValue(inputMap.get("RegistrationSample"),
				new TypeReference<List<RegistrationSample>>() {});

		final List<TestGroupTest> tgtTestInputList = objmap.convertValue(inputMap.get("testgrouptest"),
				new TypeReference<List<TestGroupTest>>() {});

		final List<String> dateList = objmap.convertValue(inputMap.get("DateList"), new TypeReference<List<String>>() {
		});

		final List<Map<String, Object>> sampleDateConstraint = objmap
				.convertValue(inputMap.get("sampledateconstraints"), new TypeReference<List<Map<String, Object>>>() {});

		final List<Map<String, Object>> subSampleDateConstraint = objmap
				.convertValue(inputMap.get("subsampledateconstraints"), new TypeReference<List<Map<String, Object>>>() {});

		final List<String> SubSampledateList = objmap.convertValue(inputMap.get("subsampleDateList"),
				new TypeReference<List<String>>() {});


		int nregtypecode = (int) inputMap.get("nregtypecode");
		int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
		int nsampletypecode = (int) inputMap.get("nsampletypecode");
		int npreregno = (int) inputMap.get("registration");
		int nreghistorycode = (int) inputMap.get("registrationhistory");
		int nregistrationparametercode = (int) inputMap.get("registrationparameter");
		int nregsamplecode = (int) inputMap.get("registrationsample");
		int nregsamplehistorycode = (int) inputMap.get("registrationsamplehistory");
		int napproveconfversioncode = (int) inputMap.get("napproveconfversioncode");
		int ntransactiontestcode = (int) inputMap.get("registrationtest");
		int ntesthistorycode = (int) inputMap.get("registrationtesthistory");
		int nregdecisionhistorycode = (int) inputMap.get("registrationdecisionhistory");
	
		int seqordersample = -1;
		int seqordertest = -1;
		++npreregno;
		++nreghistorycode;
		++ntransactiontestcode;
		++nregistrationparametercode;
		++ntesthistorycode;
		++nregdecisionhistorycode;
		int nage = 0;
		int ngendercode = 0;
		String sQuery = "";
		
		if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()) {
			
		    String sYears="SELECT CURRENT_DATE - TO_DATE('"+inputMap.get("sDob")+"','"+userInfo.getSpgsitedatetime()+"' )";
		    long ageInDays= jdbcTemplate.queryForObject(sYears,Long.class);
			
			nage = (int) inputMap.get("AgeData");
			ngendercode = (int) inputMap.get("ngendercode");
			sQuery = "json_build_object('ntransactionresultcode'," + nregistrationparametercode
					+ "+RANK() over(order by tgtp.ntestgrouptestparametercode),'ntransactiontestcode',"
					+ ntransactiontestcode + "+DENSE_RANK() over(order by tgtp.ntestgrouptestcode),'npreregno',"
					+ npreregno + ")::jsonb || " + " case when tgtp.nparametertypecode="
					+ Enumeration.ParameterType.NUMERIC.getparametertype() + " then case when  " + " tgtcs.ngendercode="
					+ ngendercode + " and " + ageInDays
					//+ " between tgtcs.nfromage and tgtcs.ntoage"
					+ " between case when tgtcs.nfromageperiod="+Enumeration.Period.Years.getPeriod()+""
				    + " then (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(years => tgtcs.nfromage)))::int)"
					+ " when tgtcs.nfromageperiod="+Enumeration.Period.Month.getPeriod()+" then "
					+ " (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(months => tgtcs.nfromage)))::int) "
					+ " else (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(days => tgtcs.nfromage)))::int) end "
					+ " and case when tgtcs.ntoageperiod="+Enumeration.Period.Years.getPeriod()
					+" then (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(years => tgtcs.ntoage)))::int) "
					+ " when tgtcs.ntoageperiod="+Enumeration.Period.Month.getPeriod()
					+" then (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(months => tgtcs.ntoage)))::int) "
					+ " else (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(days => tgtcs.ntoage)))::int) end "	
					+ " then jsonb_build_object('nfromage',"
					+ " tgtcs.nfromage,'ntoage',tgtcs.ntoage,'ngendercode',tgtcs.ngendercode ,'ngradecode',tgtcs.ngradecode,"
					+ " 'sminb',case when tgtcs.slowb='null' then NULL else tgtcs.slowb end,"
					+ " 'smina',case when tgtcs.slowa='null' then NULL else tgtcs.slowa end,"
					+ " 'smaxa',case when tgtcs.shigha='null' then NULL else tgtcs.shigha end,"
					+ " 'smaxb',case when tgtcs.shighb='null' then NULL else tgtcs.shighb end,"
					+ " 'sminlod',case when tgtcs.sminlod='null' then NULL else tgtcs.sminlod end,"
					+ " 'smaxlod',case when tgtcs.smaxlod='null' then NULL else tgtcs.smaxlod end,"
					+ " 'sminloq',case when tgtcs.sminloq='null' then NULL else tgtcs.sminloq end,"
					+ " 'smaxloq',case when tgtcs.smaxloq='null' then NULL else tgtcs.smaxloq end,"
					+ " 'sresultvalue',case when tgtcs.sresultvalue='null' then NULL else tgtcs.sresultvalue end,"
					+ " 'nroundingdigits',tgtp.nroundingdigits,'sresult',NULL,'sfinal',NULL,'stestsynonym',"
					+ " concat(tgt.stestsynonym,'[1][0]'),'sparametersynonym',tgtp.sparametersynonym,'nresultaccuracycode',tgtp.nresultaccuracycode,'sresultaccuracyname', ra.sresultaccuracyname)::jsonb else "
					+ " jsonb_build_object('nfromage',tgtcs.nfromage,'ntoage',tgtcs.ntoage,'ngendercode',tgtcs.ngendercode,'ngradecode',tgtcs.ngradecode,"
					+ " 'sminb',case when tgtcs.slowb='null' then NULL else tgtcs.slowb end,"
					+ " 'smina',case when tgtcs.slowa='null' then NULL else tgtcs.slowa end,"
					+ " 'smaxa',case when tgtcs.shigha='null' then NULL else tgtcs.shigha end,"
					+ " 'smaxb',case when tgtcs.shighb='null' then NULL else tgtcs.shighb end,"
					+ " 'sminlod',case when tgtcs.sminlod='null' then NULL else tgtcs.sminlod end,"
					+ " 'smaxlod',case when tgtcs.smaxlod='null' then NULL else tgtcs.smaxlod end,"
					+ " 'sminloq',case when tgtcs.sminloq='null' then NULL else tgtcs.sminloq end,"
					+ " 'smaxloq',case when tgtcs.smaxloq='null' then NULL else tgtcs.smaxloq end,"
					+ " 'sresultvalue',case when tgtcs.sresultvalue='null' then NULL else tgtcs.sresultvalue end,"
					+ " 'nroundingdigits',tgtp.nroundingdigits,'sresult',NULL,'sfinal',NULL,'stestsynonym',"
					+ " concat(tgt.stestsynonym,'[1][0]'),"
					+ " 'sparametersynonym',tgtp.sparametersynonym,'nresultaccuracycode',tgtp.nresultaccuracycode,'sresultaccuracyname', ra.sresultaccuracyname )::jsonb end else jsonb_build_object("
					+ " 'sminb',case when tgtcs.slowb='null' then NULL else tgtcs.slowb end,"
					+ " 'smina',case when tgtcs.slowa='null' then NULL else tgtcs.slowa end,"
					+ " 'smaxa',case when tgtcs.shigha='null' then NULL else tgtcs.shigha end,"
					+ " 'smaxb',case when tgtcs.shighb='null' then NULL else tgtcs.shighb end,"
					+ " 'sresultvalue',case when tgtcs.sresultvalue='null' then NULL else tgtcs.sresultvalue end,"
					+ " 'nroundingdigits',tgtp.nroundingdigits,'sresult',NULL,'sfinal',NULL,'stestsynonym',concat(tgt.stestsynonym,'[1][0]'),"
					+ " 'sparametersynonym',tgtp.sparametersynonym,'nresultaccuracycode',tgtp.nresultaccuracycode,'sresultaccuracyname', ra.sresultaccuracyname )::jsonb end jsondata, "
					+ userInfo.getNtranssitecode() + " nsitecode,"
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " nstatus from resultaccuracy ra,testgrouptest tgt inner join "
					+ " testgrouptestparameter tgtp  on tgt.ntestgrouptestcode = tgtp.ntestgrouptestcode "
					+ " left outer join testgrouptestclinicalspec tgtcs on tgtcs.ntestgrouptestparametercode = tgtp.ntestgrouptestparametercode "
					+ " and tgtcs.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and tgtcs.ngendercode=" + ngendercode + " and " + ageInDays
					+ " between case when tgtcs.nfromageperiod="+Enumeration.Period.Years.getPeriod()+""
				    + " then (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(years => tgtcs.nfromage)))::int)"
					+ " when tgtcs.nfromageperiod="+Enumeration.Period.Month.getPeriod()+" then "
					+ " (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(months => tgtcs.nfromage)))::int) "
					+ " else (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(days => tgtcs.nfromage)))::int) end "
					+ " and case when tgtcs.ntoageperiod="+Enumeration.Period.Years.getPeriod()+" then (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(years => tgtcs.ntoage)))::int) "
					+ " when tgtcs.ntoageperiod="+Enumeration.Period.Month.getPeriod()+" then (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(months => tgtcs.ntoage)))::int) "
					+ " else (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(days => tgtcs.ntoage)))::int) end ";

			returnMap.put("nAge", nage);
			returnMap.put("nGendercode", ngendercode);
		} else {
			
			sQuery = "(json_build_object('ntransactionresultcode'," + nregistrationparametercode
					+ "+RANK() over(order by tgtp.ntestgrouptestparametercode),'ntransactiontestcode',"
					+ ntransactiontestcode + "+DENSE_RANK() over(order by tgtp.ntestgrouptestcode),'npreregno',"
					+ npreregno
					+ ",'sminlod',tgtnp.sminlod,'smaxlod',tgtnp.smaxlod,'sminb',tgtnp.sminb,'smina',tgtnp.smina,"
					+ "'smaxa',tgtnp.smaxa,'smaxb',tgtnp.smaxb,'sminloq',tgtnp.sminloq,'smaxloq',tgtnp.smaxloq,"
					+ "'sdisregard',tgtnp.sdisregard,'sresultvalue',tgtnp.sresultvalue,'ngradecode',tgtnp.ngradecode,"
					+ "'nresultaccuracycode',tgtp.nresultaccuracycode,'sresultaccuracyname', ra.sresultaccuracyname, "
					+ "'nroundingdigits',tgtp.nroundingdigits,'sresult',NULL,'sfinal',NULL,'stestsynonym',concat(tgt.stestsynonym,'[1][0]'),"
					+ "'sparametersynonym',tgtp.sparametersynonym)::jsonb || case when tgtp.nparametertypecode="+ Enumeration.ParameterType.NUMERIC.getparametertype()+" then "
					+ "json_build_object('sresultvalue',tgtnp.sresultvalue"
					+ "):: jsonb when tgtp.nparametertypecode="+ Enumeration.ParameterType.PREDEFINED.getparametertype()+" then "
					+ "json_build_object('sresultvalue', tgtpp.spredefinedname):: jsonb"
					+ " when tgtp.nparametertypecode="+ Enumeration.ParameterType.CHARACTER.getparametertype()+" then "
					+ "json_build_object('sresultvalue', tgtcp.scharname)::jsonb else json_build_object("
					+ "'sresultvalue',tgtnp.sresultvalue)::jsonb end)::jsonb "
					+ " jsondata," + userInfo.getNtranssitecode()
					+ " nsitecode," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
					+ " from resultaccuracy ra,testgrouptest tgt,testgrouptestparameter tgtp ";
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
		String externalOrderSampleQuery = "";
		String externalOrderTestQuery = "";
		int sampleCount = registrationSample.size();
		boolean statusflag = false;

		JSONObject jsoneditRegistration = new JSONObject(registration.getJsondata());
		JSONObject jsonuiRegistration = new JSONObject(registration.getJsonuidata());
		if (registration.getJsondata().containsKey("Order Type")) {
			if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()
					&& ((int) ((JSONObject) jsoneditRegistration.get("Order Type")).getInt("value") == Enumeration.OrderType.INTERNAL.getOrderType()		
					 || (int) ((JSONObject) jsoneditRegistration.get("Order Type")).getInt("value") == Enumeration.OrderType.NA.getOrderType())) {
				seqordersample = (int) inputMap.get("externalordersample");
				seqordertest = (int) inputMap.get("externalordertest");
			}
		}
		if (!dateList.isEmpty()) {

			// commented demo work 7 lines
//			if (nregsubtypecode == 5) {
//				final Map<String, Object> map = savePatient(jsoneditRegistration, dateList, userInfo);
//				if (!((String) map.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))
//						.equals(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())) {
//					return map;
//				}
//			}

			jsoneditRegistration = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsoneditRegistration, dateList, false,
					userInfo);
			jsonuiRegistration = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonuiRegistration, dateList, false,
					userInfo);
			final Map<String, Object> obj = commonFunction.validateDynamicDateContraints(jsonuiRegistration,
					sampleDateConstraint, userInfo);
			if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
					.equals((String) obj.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
				return obj;
			}
		}

		jsonuiRegistration.put("npreregno", npreregno);
		jsonuiRegistration.put("nsampletypecode", nsampletypecode);
		jsonuiRegistration.put("nregtypecode", nregtypecode);
		jsonuiRegistration.put("nregsubtypecode", nregsubtypecode);
		jsonuiRegistration.put("nproductcatcode", registration.getNproductcatcode());
		jsonuiRegistration.put("nproductcode", registration.getNproductcode());
		jsonuiRegistration.put("nprojectmastercode", registration.getNprojectmastercode());
		jsonuiRegistration.put("ninstrumentcatcode", registration.getNinstrumentcatcode());
		jsonuiRegistration.put("ninstrumentcode", registration.getNinstrumentcode());
		jsonuiRegistration.put("nmaterialcatcode", registration.getNmaterialcatcode());
		jsonuiRegistration.put("nmaterialcode", registration.getNmaterialcode());
		jsonuiRegistration.put("ntemplatemanipulationcode", registration.getNtemplatemanipulationcode());
		jsonuiRegistration.put("nallottedspeccode", registration.getNallottedspeccode());
		jsonuiRegistration.put("ndesigntemplatemappingcode", registration.getNdesigntemplatemappingcode());
		jsonuiRegistration.put("napproveconfversioncode", napproveconfversioncode);
		jsonuiRegistration.put("napproveconfversioncode", napproveconfversioncode);
		jsonuiRegistration.put("Transaction Date", dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ").replace("Z", ""));
		
		
		if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()) {
			jsonuiRegistration.put("ngendercode", registration.getNgendercode());
		}

		strRegistrationInsert = strRegistrationInsert + "(" + npreregno + "," + nsampletypecode + "," + nregtypecode
								+ "," + nregsubtypecode + "," + registration.getNproductcatcode() + "," + registration.getNproductcode()
								+ "," + registration.getNinstrumentcatcode() + "," + registration.getNinstrumentcode() + ","
								+ registration.getNmaterialcatcode() + "," + registration.getNmaterialcode() + ","
								+ registration.getNtemplatemanipulationcode() + "," + registration.getNallottedspeccode() + ","
								+ userInfo.getNtranssitecode() + ",'" + stringUtilityFunction.replaceQuote(jsoneditRegistration.toString()) + "'::JSONB,'"
								+ stringUtilityFunction.replaceQuote(jsonuiRegistration.toString()) + "'::JSONB,"
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
								+ registration.getNdesigntemplatemappingcode() + "," + registration.getNregsubtypeversioncode() + ","
								+ registration.getNprojectmastercode() + "," + registration.getNisiqcmaterial() + ","+registration.getNprotocolcode()+"),";

		strRegistrationHistory = strRegistrationHistory + "(" + nreghistorycode + "," + npreregno + ","
								+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo)
								+ "'," + userInfo.getNusercode() + "," + userInfo.getNuserrole() + "," + userInfo.getNdeputyusercode()
								+ "," + userInfo.getNdeputyuserrole() + ",'" + stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "',"
								+ userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
								+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode() + "),";

		strDecisionHistory = strDecisionHistory + "(" + nregdecisionhistorycode + "," + npreregno + ","
							+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo)
							+ "'," + userInfo.getNusercode() + "," + userInfo.getNuserrole() + "," + userInfo.getNdeputyusercode()
							+ "," + userInfo.getNdeputyuserrole() + ",'" + stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "',"
							+ userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode() + "),";

		strRegistrationArno = strRegistrationArno + "(" + npreregno + ",'-'," + napproveconfversioncode + ","
								+ userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ "),";

		strRegistrationStatusBlink = strRegistrationStatusBlink + "(" + npreregno + "," + statusflag + ","
									+ userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ "),";

		if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()) {
			if (registration.getJsondata().containsKey("Order Type")) {
				if (((int) ((JSONObject) jsoneditRegistration.get("Order Type")).getInt("value") == Enumeration.OrderType.INTERNAL.getOrderType()
						|| (int) ((JSONObject) jsoneditRegistration.get("Order Type")).getInt("value") == Enumeration.OrderType.NA.getOrderType())
						&& registration.getJsonuidata().containsKey("Order")) {

					int externalordercode = (int) ((JSONObject) jsoneditRegistration.get("Order")).getInt("value");
					String updateExternalOrder = "update externalorder set sorderseqno=" + npreregno
												+ " , nallottedspeccode=" + registration.getNallottedspeccode()
												+ " where nexternalordercode=" + externalordercode + ";";
					jdbcTemplate.execute(updateExternalOrder);
				}

			}
		}

		StringJoiner joinerSample = new StringJoiner(",");
		for (int i = sampleCount - 1; i >= 0; i--) {
			nregsamplecode++;
			nregsamplehistorycode++;
//			nregdecisionhistorycode++;
			seqordersample++;
			joinerSample.add(String.valueOf(nregsamplecode));

			JSONObject jsoneditObj = new JSONObject(registrationSample.get(i).getJsondata());
			JSONObject jsonuiObj = new JSONObject(registrationSample.get(i).getJsonuidata());
			if (!SubSampledateList.isEmpty()) {
				jsoneditObj = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsoneditObj, SubSampledateList, false, userInfo);
				jsonuiObj = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonuiObj, SubSampledateList, false, userInfo);
				final Map<String, Object> obj = commonFunction.validateDynamicDateContraints(jsonuiObj,
						subSampleDateConstraint, userInfo);
				if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
						.equals((String) obj.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
					return obj;
				}
			}

			jsonuiObj.put("ntransactionsamplecode", nregsamplecode);
			jsonuiObj.put("npreregno", npreregno);
			jsonuiObj.put("nspecsampletypecode", registrationSample.get(i).getNspecsampletypecode());
			jsonuiObj.put("ncomponentcode", registrationSample.get(i).getNcomponentcode());

			strRegistrationSample = strRegistrationSample + " (" + nregsamplecode + "," + npreregno + ",'"
															+ registrationSample.get(i).getNspecsampletypecode() + "',"
															+ registrationSample.get(i).getNcomponentcode() + ",'" + stringUtilityFunction.replaceQuote(jsoneditObj.toString())
															+ "'::JSONB,'" + stringUtilityFunction.replaceQuote(jsonuiObj.toString()) + "'::JSONB," + userInfo.getNtranssitecode()
															+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
			
			strRegistrationSampleHistory = strRegistrationSampleHistory + "(" + nregsamplehistorycode + ","
										+ nregsamplecode + "," + npreregno + ","
										+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + ",'"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNusercode() + "," + userInfo.getNuserrole()
										+ "," + userInfo.getNdeputyusercode() + "," + userInfo.getNdeputyuserrole() + ",'"
										+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "'," + userInfo.getNtranssitecode() + ","
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
										+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode() + "),";

			strRegistrationSampleArno = strRegistrationSampleArno + "(" + nregsamplecode + "," + npreregno + ",'-'"
					+ "," + userInfo.getNtranssitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";

			List<RegistrationSample> comp = new ArrayList();
			comp.add(registrationSample.get(i));
			List<TestGroupTest> lsttest1 = tgtTestInputList.stream().filter(x -> x.getSlno() == comp.get(0).getSlno())
					.collect(Collectors.toList());

			final String stestcode = lsttest1.stream().map(x -> String.valueOf(x.getNtestgrouptestcode())).distinct()
					.collect(Collectors.joining(","));

			if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()) {
				if (registration.getJsondata().containsKey("Order Type")) {
					if (((int) ((JSONObject) jsoneditRegistration.get("Order Type")).getInt("value") == Enumeration.OrderType.INTERNAL.getOrderType()
							|| (int) ((JSONObject) jsoneditRegistration.get("Order Type")).getInt("value") == Enumeration.OrderType.NA.getOrderType())
							&& registration.getJsonuidata().containsKey("Order")) {

						int externalordercode = (int) ((JSONObject) jsoneditRegistration.get("Order")).getInt("value");
						int nsampleAppearanceCode = -1;
						if (jsonuiObj.has("nsampleappearancecode")) {
							nsampleAppearanceCode = (int) jsonuiObj.get("nsampleappearancecode");
						}
						externalOrderSampleQuery += " (" + seqordersample + "," + externalordercode + ","
								+ registrationSample.get(i).getNcomponentcode() + "," + 0 + "," + -1 + ",'"
								+ stringUtilityFunction.replaceQuote(jsonuiObj.get("sampleorderid").toString()) + "','"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + "" + userInfo.getNtranssitecode() + ", "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
								+ userInfo.getNtranssitecode() + "," + nregsamplecode + ", '"
								+ stringUtilityFunction.replaceQuote(jsonuiObj.get("subsamplecollectiondatetime").toString()) + "', "
								+ nsampleAppearanceCode + "),";
						// ALPD-3575
						for (TestGroupTest test : lsttest1) {
							seqordertest++;
							externalOrderTestQuery += "(" + seqordertest + "," + seqordersample + ","
									+ externalordercode + "," + test.getNtestpackagecode() + ","
									+ test.getNcontainertypecode() + " ," + test.getNtestcode() + ",'"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + "," + userInfo.getNtranssitecode() + ","
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
									+ userInfo.getNtranssitecode() + "),";
						}
					}

				}
			}

			if (!lsttest1.isEmpty()) {

				// FRS-00410- To add test based on replicate count defined in testgroup test

				final Map<String, Object> testHistoryParameterMap = insertTestHistoryParameter(stestcode, sQuery,
						userInfo, npreregno, nregsamplecode, ntransactiontestcode, ntesthistorycode,
						nregistrationparametercode, new ArrayList<Integer>(),inputMap);
				ntransactiontestcode = (int) testHistoryParameterMap.get("ntransactiontestcode");
				ntesthistorycode = (int) testHistoryParameterMap.get("ntesthistorycode");
				nregistrationparametercode = (int) testHistoryParameterMap.get("ntransactionresultcode");

				// End- FRS-00410


			}

		}

		strRegistrationInsert = "Insert into registration (npreregno, nsampletypecode, nregtypecode, nregsubtypecode, nproductcatcode, "
								+ "nproductcode,ninstrumentcatcode,ninstrumentcode,nmaterialcatcode,nmaterialcode, ntemplatemanipulationcode, "
								+ " nallottedspeccode, nsitecode,jsondata,jsonuidata, nstatus,ndesigntemplatemappingcode,nregsubtypeversioncode, "
								+ " nprojectmastercode, nisiqcmaterial,nprotocolcode) values "
								+ strRegistrationInsert.substring(0, strRegistrationInsert.length() - 1) + ";";
		jdbcTemplate.execute(strRegistrationInsert);

		strRegistrationHistory = "Insert into registrationhistory(nreghistorycode, npreregno, ntransactionstatus, dtransactiondate, nusercode, "
								+ " nuserrolecode, ndeputyusercode, ndeputyuserrolecode, scomments, nsitecode, nstatus,noffsetdtransactiondate,ntransdatetimezonecode) values "
								+ strRegistrationHistory.substring(0, strRegistrationHistory.length() - 1) + ";";
		jdbcTemplate.execute(strRegistrationHistory);

		if (registration.getNsampletypecode() == Enumeration.SampleType.INSTRUMENT.getType()) {

			//ALPD-5441--Added by Vignesh R(18-02-2025)-->Sample registration screen -> While save the sub-sample 500 error occurs in Instrument flow.
			String query = "select * from instrumentcalibration where  ncalibrationstatus="
					+ Enumeration.TransactionStatus.UNDERCALIBIRATION.gettransactionstatus()
					+ " and dopendate is null and dclosedate is null and ninstrumentcode="
					+ registration.getNinstrumentcode()
					+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode="+userInfo.getNtranssitecode();

			final InstrumentCalibration instrumentCalibration = (InstrumentCalibration) jdbcUtilityFunction.queryForObject(query,
					InstrumentCalibration.class, jdbcTemplate);

			query = "update instrumentcalibration set npreregno=" + npreregno + ", dmodifieddate ='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ninstrumentcalibrationcode="
					+ instrumentCalibration.getNinstrumentcalibrationcode();
			jdbcTemplate.execute(query);

		}

		strDecisionHistory = "Insert into registrationdecisionhistory(nregdecisionhistorycode, npreregno, ndecisionstatus, dtransactiondate, "
							+ "nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, scomments, nsitecode,nstatus,noffsetdtransactiondate,ntransdatetimezonecode  ) values "
							+ strDecisionHistory.substring(0, strDecisionHistory.length() - 1) + ";";
		jdbcTemplate.execute(strDecisionHistory);

		strRegistrationArno = "Insert into registrationarno (npreregno, sarno, napprovalversioncode, nsitecode,nstatus) values "
								+ strRegistrationArno.substring(0, strRegistrationArno.length() - 1) + ";";
		jdbcTemplate.execute(strRegistrationArno);

		strRegistrationSample = "insert into registrationsample(ntransactionsamplecode,npreregno,nspecsampletypecode,ncomponentcode,jsondata,jsonuidata, "
							+ "nsitecode,nstatus) values " + strRegistrationSample.substring(0, strRegistrationSample.length() - 1)
							+ ";";
		jdbcTemplate.execute(strRegistrationSample);

		strRegistrationSampleHistory = "Insert into registrationsamplehistory(nsamplehistorycode, ntransactionsamplecode, npreregno, ntransactionstatus, "
									+ "dtransactiondate, nusercode, nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments,nsitecode,nstatus,noffsetdtransactiondate,ntransdatetimezonecode ) values "
									+ strRegistrationSampleHistory.substring(0, strRegistrationSampleHistory.length() - 1) + ";";
		jdbcTemplate.execute(strRegistrationSampleHistory);

		strRegistrationSampleArno = "Insert into registrationsamplearno (ntransactionsamplecode,npreregno,ssamplearno,nsitecode,nstatus) values "
									+ strRegistrationSampleArno.substring(0, strRegistrationSampleArno.length() - 1) + ";";
		jdbcTemplate.execute(strRegistrationSampleArno);

		if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()) {
			if (registration.getJsonuidata().containsKey("Order Type")) {
				if (((int) ((JSONObject) jsoneditRegistration.get("Order Type")).getInt("value") == Enumeration.OrderType.INTERNAL.getOrderType()
						|| (int) ((JSONObject) jsoneditRegistration.get("Order Type")).getInt("value") == Enumeration.OrderType.NA.getOrderType())
						&& registration.getJsonuidata().containsKey("Order")) {

					externalOrderSampleQuery = "INSERT INTO externalordersample (nexternalordersamplecode,nexternalordercode,ncomponentcode,nsampleqty,nunitcode,sexternalsampleid,dmodifieddate,nsitecode,nstatus, nparentsitecode,ntransactionsamplecode,dsamplecollectiondatetime,nsampleappearancecode) values "
							+ externalOrderSampleQuery.substring(0, externalOrderSampleQuery.length() - 1) + ";";
					jdbcTemplate.execute(externalOrderSampleQuery);
					if (!tgtTestInputList.isEmpty()) {
						externalOrderTestQuery = "INSERT INTO externalordertest (nexternalordertestcode,nexternalordersamplecode,nexternalordercode,ntestpackagecode,ncontainertypecode,ntestcode,dmodifieddate,nsitecode,nstatus, nparentsitecode) values "
								+ externalOrderTestQuery.substring(0, externalOrderTestQuery.length() - 1) + ";";

						jdbcTemplate.execute(externalOrderTestQuery);
					}
				}
			}
		}
		strRegistrationStatusBlink = "Insert into registrationflagstatus (npreregno,bflag,nsitecode,nstatus) "
				+ " values" + strRegistrationStatusBlink.substring(0, strRegistrationStatusBlink.length() - 1) + ";";
		jdbcTemplate.execute(strRegistrationStatusBlink);
		
			
		if((int) inputMap.get("nportalrequired") ==  Enumeration.TransactionStatus.YES.gettransactionstatus())
		{
			if(registration.getJsondata().containsKey("nexternalordercode")) {
			
				int externalordercode = (int) (registration.getJsondata().get("nexternalordercode"));
//				if ((int) jsoneditRegistration.get("nexternalordertypecode") ==  Enumeration.ExternalOrderType.PORTAL.getExternalOrderType()) {
					
				final StringBuilder insertQuery = new StringBuilder();
					
				final String query="Select * from externalordersampleattachment  where "
						+ " nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and  nexternalordercode="+externalordercode
						+ "  and nsitecode="+userInfo.getNtranssitecode(); 
				final List<ExternalOrderSampleAttachment> listData=jdbcTemplate.query(query, new ExternalOrderSampleAttachment());
				
				if(!listData.isEmpty()) {
					int seqNo = jdbcTemplate.queryForObject("select nsequenceno from seqnoregistrationattachment where stablename = 'registrationattachment' and nstatus ="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";", Integer.class);
					
					final String updateSeqNoString = "update seqnoregistrationattachment set nsequenceno = "+(listData.size()+seqNo)+" where stablename = 'registrationattachment'";
					//jdbcTemplate.execute(updateSeqNoString);
					insertQuery.append("INSERT INTO registrationattachment(nregattachmentcode, npreregno,nformcode,nattachmenttypecode,nlinkcode,nusercode,nuserrolecode,jsondata,nsitecode, nstatus) values");
					for(ExternalOrderSampleAttachment attactment : listData) {
					seqNo++;
					final String currenDate = dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ").replace("Z", "");
					final Map<String,Object> jsonData = attactment.getJsondata();
					jsonData.put("dcreateddate", currenDate);
					jsonData.put("noffsetdcreateddate", dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) );
					jsonData.put("susername", userInfo.getSusername());
					jsonData.put("suserrolename",userInfo.getSuserrolename());
					jsonData.put("sdescription","");
				    insertQuery.append( "("+seqNo+","+npreregno+","+userInfo.getNformcode()+",1,-1,"+userInfo.getNusercode()+","+userInfo.getNuserrole()+","
							+ "'"+stringUtilityFunction.replaceQuote(objMapper.writeValueAsString(jsonData).toString())+"',"+userInfo.getNtranssitecode()+","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"),");
					}
					jdbcTemplate.execute(insertQuery.substring(0, insertQuery.length()-1)+"; "+updateSeqNoString);
				}
			
		  //}
				String updatePreregno="update externalorder set npreregno="+npreregno+",nsitecode ="+userInfo.getNtranssitecode()+" "
						+ " where nexternalordercode="+externalordercode;
				jdbcTemplate.execute(updatePreregno);
			}
		}
		
	   
				
		inputMap.put("npreregno", String.valueOf(npreregno));

		// inputMap.put("nsubsample",3);
		returnMap = registrationDAOSupport.getDynamicRegistration(inputMap, userInfo);
		returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
				Enumeration.ReturnStatus.SUCCESS.getreturnstatus());

		jsonAuditObject.put("registration", (List<Map<String, Object>>) returnMap.get("RegistrationGetSample"));
		actionType.put("registration", "IDS_PREREGISTER");

		if ((boolean) inputMap.get("nneedsubsample")) {
			jsonAuditObject.put("registrationsample",
					(List<Map<String, Object>>) returnMap.get("RegistrationGetSubSample"));
			actionType.put("registrationsample", "IDS_PREREGISTERSAMPLE");

			inputMap.put("ntransactionsamplecode", null);

			Map<String, Object> objMap = (Map<String, Object>) transactionDAOSupport.getRegistrationTestAudit(inputMap, userInfo).getBody();

			jsonAuditObject.put("registrationtest", (List<Map<String, Object>>) objMap.get("RegistrationGetTest"));
		} else {
			jsonAuditObject.put("registrationtest", (List<Map<String, Object>>) returnMap.get("RegistrationGetTest"));
		}

		auditmap.put("nregtypecode", nregtypecode);
		auditmap.put("nregsubtypecode", nregsubtypecode);
		auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));

		actionType.put("registrationtest", "IDS_PREREGISTERTEST");
		auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);

		// Portal Status Update
		
		if((int) inputMap.get("nportalrequired") ==  Enumeration.TransactionStatus.YES.gettransactionstatus())
		{
//			String str1 = "select settings.ssettingvalue from settings where nsettingcode=52;";
//			String ssettingvalue = jdbcTemplate.queryForObject(str1, String.class);
//			if(ssettingvalue.equals("3"))
//			{
//				String str=" select npreregno,(registration.jsondata->'nexternalordercode')::int as nexternalordercode,jsondata->>'externalorderid' as sexternalorderid, "
//						   + Enumeration.TransactionStatus.RECEIVED.gettransactionstatus()+ " ntransactionstatus"
//						   + " from registration where (registration.jsondata->>'nexternalordercode') IS NOT NULL "
//						   + " AND (registration.jsondata->>'nexternalordercode') != 'nexternalordercode' and npreregno in("+npreregno+")";
//
//				List<ExternalOrderTest> lstexternalorderstatus = jdbcTemplate.query(str, new ExternalOrderTest());	
//				if(lstexternalorderstatus.size()>0) {
//					insertExternalOrderStatus(userInfo,lstexternalorderstatus);				
//				}
//			}
//			else
//			{
//				inputMap.put("ssamplecode", joinerSample.toString());
//				returnMap.putAll((Map<String, Object>) sampleOrderUpdate(inputMap, String.valueOf(npreregno), userInfo,
//						Enumeration.TransactionStatus.INITIATED.gettransactionstatus(),
//						Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus(), true).getBody());
//			}
		}		

		return returnMap;

	}
	
	
	@SuppressWarnings("unchecked")
	private Map<String, Object> insertTestHistoryParameter(final String stestcode, final String sQuery,
			final UserInfo userInfo, final int npreregno, final int nregsamplecode, int ntransactiontestcode,
			int ntesthistorycode, int nregistrationparametercode, List<Integer> transactionCodeList,Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final String testGroupQuery = "select tgt.*, s.ssectionname, m.smethodname "
									+ " from testgrouptest tgt,section s,method m  "
									+ " where tgt.nsectioncode=s.nsectioncode " 
									+ " and tgt.nmethodcode=m.nmethodcode "
									+ " and tgt.ntestgrouptestcode in (" + stestcode + ")" 
									+ " and s.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and tgt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and m.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and tgt.nsitecode=" + userInfo.getNmastersitecode()
									+ " and s.nsitecode=" + userInfo.getNmastersitecode()
									+ " and m.nsitecode=" + userInfo.getNmastersitecode();
		// List<TestGroupTest> testGroupTestList = jdbcTemplate.query(testGroupQuery,
		// new TestGroupTest());

		List<RegistrationTest> testGroupTestList = jdbcTemplate.query(testGroupQuery, new RegistrationTest());

		testGroupTestList = registrationDAOSupport.getTestPrice(testGroupTestList);
		
		String strAdhocQuery="";
		if((int)inputMap.get("nsampletypecode")==Enumeration.SampleType.CLINICALSPEC.getType()) {
			strAdhocQuery=" and tgtp.nisadhocparameter in ("+ Enumeration.TransactionStatus.NO.gettransactionstatus()+");"; 
		}else {
			strAdhocQuery=" and tgtp.nisvisible in ("+ Enumeration.TransactionStatus.YES.gettransactionstatus()+");"; 
		}
		
		//ALPD-4168  added tgf.nstatus=1 by rukshana which was not checked previously on May 18 2024
		
		final String testParameterQuery = "select tgtp.ntestgrouptestcode, tgtp.ntestgrouptestparametercode,tgtp.ntestparametercode,tgtp.nparametertypecode,"
									+ " COALESCE(tgf.ntestgrouptestformulacode,-1) ntestgrouptestformulacode,"
									+ " tgtp.nunitcode,tgtp.nresultmandatory,tgtp.nreportmandatory," + sQuery
									+ " left outer join testgrouptestnumericparameter tgtnp on "
									+ " tgtnp.ntestgrouptestparametercode=tgtp.ntestgrouptestparametercode "
									+ " and tgtnp.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " left outer join testgrouptestcharparameter tgtcp on "
									+ " tgtcp.ntestgrouptestparametercode=tgtp.ntestgrouptestparametercode  "
									+ " and tgtcp.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " left outer join (select tt.ntestgrouptestparametercode,max(tt.spredefinedname) as spredefinedname ,"
									+ " max(tp.nstatus) as nstatus  "
									+ " from testgrouptestpredefparameter tt,testgrouptestparameter tp "
									+ " where tt.ntestgrouptestparametercode=tp.ntestgrouptestparametercode group by tt.ntestgrouptestparametercode) tgtpp on "
									+ " tgtpp.ntestgrouptestparametercode=tgtp.ntestgrouptestparametercode  "
									+ " and tgtpp.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " left outer join testgrouptestformula tgf on tgtnp.ntestgrouptestparametercode=tgf.ntestgrouptestparametercode"
									+ " and tgf.ntransactionstatus = " + Enumeration.TransactionStatus.YES.gettransactionstatus()
									+ " and tgf.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +""
									+ " where tgt.ntestgrouptestcode=tgtp.ntestgrouptestcode "
									+ " and tgtp.nresultaccuracycode=ra.nresultaccuracycode " 
									+ " and tgt.nstatus= "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
									+ " and tgtp.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
									+ " and ra.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
									+ " and tgt.nsitecode=" + userInfo.getNmastersitecode()
									+ " and tgtp.nsitecode=" + userInfo.getNmastersitecode()
									+ " and ra.nsitecode=" + userInfo.getNmastersitecode()
									+ " and tgtp.ntestgrouptestcode in ("
									+ stestcode + ")" + " " +strAdhocQuery;
		final List<TestGroupTestParameter> tgtParameterList = jdbcTemplate.query(testParameterQuery,
				new TestGroupTestParameter());

		final ArrayList<String> replicateTestList = new ArrayList<String>();
		final ArrayList<String> replicateTestHistoryList = new ArrayList<String>();
		final ArrayList<String> replicateTestParameterList = new ArrayList<String>();

		for (RegistrationTest testGroupTest : testGroupTestList) {
			if (testGroupTest.getNrepeatcountno() > 1) {
				for (int repeatNo = 1; repeatNo <= testGroupTest.getNrepeatcountno(); repeatNo++) {
					int nttestcode = ntransactiontestcode++;
					transactionCodeList.add(nttestcode);
					replicateTestList.add("(" + nttestcode + "," + nregsamplecode + "," + npreregno + ","
											+ testGroupTest.getNtestgrouptestcode() + "," + testGroupTest.getNinstrumentcatcode()
											+ ",-1," + repeatNo + ",0," + " json_build_object('ntransactiontestcode', " + nttestcode
											+ ",'npreregno'," + npreregno + ",'ntransactionsamplecode'," + nregsamplecode
											+ ",'ssectionname','" + stringUtilityFunction.replaceQuote(testGroupTest.getSsectionname()) + "','smethodname','"
											+ stringUtilityFunction.replaceQuote(testGroupTest.getSmethodname()) + "','ncost'," + testGroupTest.getNcost()
											+ "," + "'stestname','" + stringUtilityFunction.replaceQuote(testGroupTest.getStestsynonym()) + "',"
											+ "'stestsynonym',concat('" + stringUtilityFunction.replaceQuote(testGroupTest.getStestsynonym()) + "','["
											+ repeatNo + "][0]'))::jsonb," + userInfo.getNtranssitecode() + ",'"
											+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
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
						
						if (testGroupTestParameter.getNtestgrouptestcode() == testGroupTest.getNtestgrouptestcode()) {
							int nttestparametercode = nregistrationparametercode++;
							final Map<String, Object> mapObject = testGroupTestParameter.getJsondata();
							mapObject.put("ntransactionresultcode", nttestparametercode);
							mapObject.put("ntransactiontestcode", nttestcode);
							mapObject.put("stestsynonym", testGroupTest.getStestsynonym() + "[" + repeatNo + "][0]");

							replicateTestParameterList.add("(" + nttestparametercode + "," + npreregno + ","
														+ nttestcode + "," + testGroupTestParameter.getNtestgrouptestparametercode() + ","
														+ testGroupTestParameter.getNtestparametercode() + ","
														+ testGroupTestParameter.getNparametertypecode() + ","
														+ testGroupTestParameter.getNtestgrouptestformulacode() + ","
														+ testGroupTestParameter.getNunitcode() + ","
														+ testGroupTestParameter.getNresultmandatory() + ","
														+ testGroupTestParameter.getNreportmandatory() + ","
														+ "'" + objMapper.writeValueAsString(mapObject) + "',"
														+ userInfo.getNtranssitecode() + ","
														+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")");
						}
					}

				}
			} else {
				List<Map<String,Object>> testGroupTestList1 = (List<Map<String, Object>>) inputMap.get("testgrouptest");
				
				Integer externalOrderTestCode = testGroupTestList1.stream()
					    .filter(x -> (int)x.get("ntestcode") == testGroupTest.getNtestcode())
					    .map(y -> {
					        if (y.containsKey("nexternalordertestcode")) {
					            return (Integer)y.get("nexternalordertestcode");
					        } else {
					            return -1;
					        }
					    })
					    .findFirst().orElse(-1);
				String externalOrderTestStr ="";
				if(externalOrderTestCode!=null && externalOrderTestCode!=-1 ) {
					externalOrderTestStr=" 'nexternalordertestcode',"+externalOrderTestCode+","	;		
					}

				int nttestcode = ntransactiontestcode++;
				transactionCodeList.add(nttestcode);
				
				String strQuery ="select ssettingvalue from settings where nsettingcode ="+Enumeration.Settings.UPDATING_ANALYSER.getNsettingcode()+" ";
				Settings objSettings = (Settings) jdbcUtilityFunction.queryForObject(strQuery, Settings.class, jdbcTemplate);
				String AnalyserValue ="";				
				
				if(objSettings!=null) {
					if(Integer.valueOf(objSettings.getSsettingvalue())== Enumeration.TransactionStatus.YES.gettransactionstatus()) {
						AnalyserValue = ",'AnalyserCode',-1,'AnalyserName','-'";
					}
				}
								
						
				replicateTestList.add("(" + nttestcode + "," + nregsamplecode + "," + npreregno + ","
											+ testGroupTest.getNtestgrouptestcode() + "," + testGroupTest.getNinstrumentcatcode()
											+ ",-1,1,0," + " json_build_object('ntransactiontestcode', " + nttestcode + ",'npreregno',"
											+ npreregno + ",'ntransactionsamplecode'," + nregsamplecode + ",'ssectionname','"
											+ stringUtilityFunction.replaceQuote(testGroupTest.getSsectionname()) + "','smethodname','"
											+ stringUtilityFunction.replaceQuote(testGroupTest.getSmethodname()) + "','ncost'," + testGroupTest.getNcost() + ","+externalOrderTestStr
											+ "'stestname','" + stringUtilityFunction.replaceQuote(testGroupTest.getStestsynonym()) + "',"
											+ "'stestsynonym',concat('" + stringUtilityFunction.replaceQuote(testGroupTest.getStestsynonym())
											+ "','[1][0]')"+AnalyserValue+")::jsonb," + userInfo.getNtranssitecode() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo)
											+ "'," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
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


					if (testGroupTestParameter.getNtestgrouptestcode() == testGroupTest.getNtestgrouptestcode()) {
						int nttestparametercode = nregistrationparametercode++;

						final Map<String, Object> mapObject = testGroupTestParameter.getJsondata();
						mapObject.put("ntransactionresultcode", nttestparametercode);
						mapObject.put("ntransactiontestcode", nttestcode);
						replicateTestParameterList.add("(" + nttestparametercode + "," + npreregno + "," + nttestcode
								+ "," + testGroupTestParameter.getNtestgrouptestparametercode() + ","
								+ testGroupTestParameter.getNtestparametercode() + ","
								+ testGroupTestParameter.getNparametertypecode() + ","
								+ testGroupTestParameter.getNtestgrouptestformulacode() + ","
								+ testGroupTestParameter.getNunitcode() + ","
								+ testGroupTestParameter.getNresultmandatory() + ","
								+ testGroupTestParameter.getNreportmandatory() + ","
								+ "'" + stringUtilityFunction.replaceQuote(objMapper.writeValueAsString(mapObject)) + "',"
								+ userInfo.getNtranssitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")");
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
	
	@Override
	public Map<String, Object> insertSeqNoRegistration(Map<String, Object> inputMap) throws Exception {

		String sQuery = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		sQuery = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		sQuery = " lock  table lockcancelreject " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final ObjectMapper objMapper = new ObjectMapper();
		Map<String, Object> returnMap = new HashMap<String, Object>();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		int nMultiples = 1;
		//Long nLinterSize = (long) 0;

		final boolean needJobAllocation = (boolean) inputMap.get("nneedjoballocation");

		final String strNpreregno = (String) inputMap.get("npreregno");
		final String[] preregnoArray = strNpreregno.split(",");
		final List<String> newList = Arrays.asList(preregnoArray).stream().map(s -> s)
				.collect(Collectors.toList());


		final String strPreregno = "select npreregno from registrationarno where npreregno in (" + strNpreregno
				          + ") and sarno != '-' and nsitecode=" + userInfo.getNtranssitecode();
		final List<Integer> lstInteger = jdbcTemplate.queryForList(strPreregno, Integer.class);

		final String strPreregno1 = " select npreregno from registration where "
							+ " npreregno not in(select rt.npreregno from registrationtest rt, testmaster tm,"
							+ " registrationtesthistory rth where rt.npreregno =rth.npreregno and "
							+ " rt.ntransactiontestcode=rth.ntransactiontestcode "
							+ " and rt.ntestcode = tm.ntestcode "
							+ " and tm.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
							+ " and tm.nsitecode="+ userInfo.getNmastersitecode()
							+ " and tm.ntransactionstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and rth.ntesthistorycode in (select max(ntesthistorycode) from "
							+ " registrationtesthistory rth1 where rth1.ntransactiontestcode=rt.ntransactiontestcode "
							+ " and rth1.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and rt.npreregno in(" + strNpreregno + ")  "
							+ " and rt.nsitecode= " + userInfo.getNtranssitecode()
							+ " and rth1.nsitecode=" + userInfo.getNtranssitecode()
							+ " group by rth1.npreregno,rth1.ntransactionsamplecode,rth1.ntransactiontestcode) "
							+ " and rth.ntransactionstatus not in(" + Enumeration.TransactionStatus.REJECTED.gettransactionstatus()
							+ "," + Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ") "
							+ " and rt.nsitecode=" + userInfo.getNtranssitecode()
							+ " and rth.nsitecode=" + userInfo.getNtranssitecode() 
							+ " and rt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and rth.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ ") "
							+ " and npreregno in (" + strNpreregno + ")  "
							+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and nsitecode=" + userInfo.getNtranssitecode();

		final List<Integer> lstInteger1 = jdbcTemplate.queryForList(strPreregno1, Integer.class);


		newList.removeAll(lstInteger);
		newList.removeAll(lstInteger1);
		
		String InsertPreregno = String.join(",",newList);
		if (InsertPreregno.equals("")) {
			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					"IDS_SELECTPREREGISTERORQUARANTINESAMPLES");
		}

		String flowType = "NormalFlow";
		if (newList.size() > 0) {
			String sql = " select * from registrationsamplehistory jrsh where"
					+ " jrsh.nsamplehistorycode in (select max(rth1.nsamplehistorycode) "
					+ " from registrationsamplehistory rth1,registrationsample ja1,registration r"
					+ " where rth1.npreregno = ja1.npreregno"
					+ " and rth1.ntransactionsamplecode = ja1.ntransactionsamplecode" 
					+ " and ja1.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and rth1.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and r.npreregno=ja1.npreregno and r.npreregno in (" + InsertPreregno + ")"
					+ " and rth1.nsitecode=" +userInfo.getNtranssitecode()
					+ " and ja1.nsitecode=" +userInfo.getNtranssitecode()
					+ " and r.nsitecode = "+userInfo.getNtranssitecode()
					+ " and r.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				    + " group by rth1.npreregno,rth1.ntransactionsamplecode )"
					+ " and jrsh.ntransactionstatus ="
					+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() 
					+ " and jrsh.nsitecode=" + userInfo.getNtranssitecode() 
					+ " and jrsh.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and jrsh.npreregno in (" + InsertPreregno + ")";

			
			final List<RegistrationSample> subSampleCountList = (List<RegistrationSample>) jdbcTemplate.query(sql,
					new RegistrationSample());

			int subsamplecount = subSampleCountList.size();

			sql = " select rt.npreregno, rt.ntransactionsamplecode, rt.ntransactiontestcode from registrationtest rt, "
					+ " testmaster tm, registrationtesthistory rth where rt.npreregno =rth.npreregno and "
					+ " rt.ntransactiontestcode=rth.ntransactiontestcode "
					+ " and rt.ntestcode = tm.ntestcode "
					+ " and tm.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and tm.ntransactionstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and rth.ntesthistorycode in (select max(ntesthistorycode) from "
					+ " registrationtesthistory rth1 where rth1.ntransactiontestcode=rt.ntransactiontestcode "
					+ " and rth1.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and rt.npreregno in(" + InsertPreregno + ")  and rt.nsitecode=rth1.nsitecode "
					+ " and rth1.nsitecode=" + userInfo.getNtranssitecode()
					+ " group by rth1.npreregno,rth1.ntransactionsamplecode,rth1.ntransactiontestcode) "
					+ " and rth.ntransactionstatus not in("
					+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","
					+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ") "
					+ " and rt.nsitecode=" + userInfo.getNtranssitecode()
					+ " and rt.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and rth.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and rth.nsitecode=" + userInfo.getNtranssitecode();

			final List<RegistrationTest> sampleTestList = (List<RegistrationTest>) jdbcTemplate.query(sql,
					new RegistrationTest());

			final List<Integer> validTestSubSampleList = sampleTestList.stream()
					.map(item -> item.getNtransactionsamplecode()).distinct().collect(Collectors.toList());
			
			if(subsamplecount>0) {
				
				if (subsamplecount == validTestSubSampleList.size()) {
					
					/// Method validity Check
					final String tgtQueryString = "select rt.ntestgrouptestcode  from registrationtesthistory rth1, registrationtest rt  where "
							+ " rt.npreregno in (" + InsertPreregno + ") and rth1.npreregno = rt.npreregno "
							+ " and rth1.ntransactionsamplecode = rt.ntransactionsamplecode "
							+ " and rt.ntransactiontestcode = rth1.ntransactiontestcode "
							+ " and rt.nsitecode=" + userInfo.getNtranssitecode()
							+ " and rth1.nsitecode =  " + userInfo.getNtranssitecode()
							+ " and  rt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and ntesthistorycode in (select  max(rth.ntesthistorycode) as testhistorycode "
							+ " from registrationtesthistory rth where rth.npreregno in (" + InsertPreregno + ") "
							+ " and rth.nsitecode=" + userInfo.getNtranssitecode() + " and rth.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " group by rth.npreregno,rth.ntransactiontestcode "
							+ " ) and rth1.ntransactionstatus not in ("
							+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","
							+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ") "
							+ " and  rth1.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " group by rt.ntestgrouptestcode ";
					final List<TestGroupTest> tgtList = (List<TestGroupTest>) jdbcTemplate.query(tgtQueryString,
							new TestGroupTest());
					final Boolean skipMethodValidity = (Boolean) inputMap.get("skipmethodvalidity");
					
					final String sntestgrouptestcode = stringUtilityFunction.fnDynamicListToString(tgtList, "getNtestgrouptestcode");
					
					List<TestGroupTest> expiredMethodTestList = new ArrayList<TestGroupTest>();
					if (skipMethodValidity == false) {
						expiredMethodTestList = transactionDAOSupport.getTestByExpiredMethod(sntestgrouptestcode, userInfo);
						
					}
					if (expiredMethodTestList.isEmpty()) {
						
						sql = "select * from  fn_registrationtestcount ('" + InsertPreregno + "'" + ","
								+ userInfo.getNtranssitecode() + ","
								+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + ")";
						String stestcount = jdbcTemplate.queryForObject(sql, String.class);		
						
						String[] strArray = stestcount.split(",");
						String strSequenno = "";
						boolean boolRoleWiseApproval = false;
						int napproveconfigversioncode = (int) inputMap.get("napproveconfversioncode");
						
						final String StrSql1 = "select ac.napprovalconfigcode,acaa.nautoapprovalcode,acaa.nautoallot,"
								+ " acaa.nneedjoballocation,acv.napproveconfversioncode,"
								+ " acaa.nneedautoapproval,acaa.nneedautocomplete from approvalconfig ac, "
								+ " approvalconfigautoapproval acaa,approvalconfigversion acv "
								+ " where ac.napprovalconfigcode = acaa.napprovalconfigcode "
								+ " and ac.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and acaa.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and ac.nsitecode=" + userInfo.getNmastersitecode()
								+ " and acaa.nsitecode=" + userInfo.getNmastersitecode()
								+ " and acv.nsitecode=" + userInfo.getNmastersitecode()
								+ " and ac.nregtypecode = "	+ (int) inputMap.get("nregtypecode") 
								+ " and ac.nregsubtypecode = "	+ (int) inputMap.get("nregsubtypecode")
								+ " and ac.napprovalconfigcode = acv.napprovalconfigcode "
								+ " and acv.napproveconfversioncode = acaa.napprovalconfigversioncode "
								+ " and acv.napproveconfversioncode = "	+ napproveconfigversioncode 
								+ " and acv.nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
						
						final ApprovalConfigAutoapproval objConfig = (ApprovalConfigAutoapproval) jdbcUtilityFunction.queryForObject(
								StrSql1, ApprovalConfigAutoapproval.class, jdbcTemplate);
						
						
						if (objConfig.getNneedautoapproval() == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
							final String roleWiseAutoApproval = "select acr.* "
									+ " from approvalconfigrole acr,approvalconfigversion acv"
									+ " where acv.napproveconfversioncode=acr.napproveconfversioncode" 
									+ " and acv.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
									+ " and acr.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and acr.nsitecode=" + userInfo.getNmastersitecode()
									+ " and acv.nsitecode=" + userInfo.getNmastersitecode()
									+ " and acv.napproveconfversioncode=" + objConfig.getNapproveconfversioncode();
							
							final List<ApprovalConfigRole> roleList = jdbcTemplate.query(roleWiseAutoApproval,
									new ApprovalConfigRole());
							boolRoleWiseApproval = roleList.stream().anyMatch(role -> role
									.getNautoapprovalstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus());
						}
						if (objConfig.getNneedautoapproval() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
							strSequenno = ",'joballocation',"// 'registrationsection','registrationsectionhistory',"
									+ " 'registrationdecisionhistory','sampleapprovalhistory'";
							nMultiples = 3;
							flowType = "AutoApprovalFlow";
						} else if (boolRoleWiseApproval
								&& objConfig.getNneedautoapproval() == Enumeration.TransactionStatus.NO
								.gettransactionstatus()
								&& objConfig.getNneedautocomplete() == Enumeration.TransactionStatus.YES
								.gettransactionstatus()) {
							strSequenno = ",'joballocation'," // 'registrationsection','registrationsectionhistory',"
									+ " 'registrationdecisionhistory','sampleapprovalhistory'";
							String str = fnCheckDefaultResult(strNpreregno, userInfo);
							nMultiples = 3;
							
							flowType = "AutoApprovalFlowWithRoleWise";
							if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(str)) {
								returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), str);
								return returnMap;
							}
						} else if (objConfig.getNneedautoapproval() == Enumeration.TransactionStatus.NO
								.gettransactionstatus()
								&& objConfig.getNneedautocomplete() == Enumeration.TransactionStatus.YES
								.gettransactionstatus()) {
							flowType = "AutoComplete";
							strSequenno = ",'joballocation'";
							String str = fnCheckDefaultResult(strNpreregno, userInfo);
							nMultiples = 2;
							if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(str)) {
								returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), str);
								return returnMap;
							}
						}
						
						//below conditional loop commented by L.Subashini on 8/5/2025 as it is not used for now
						else if (!needJobAllocation || (needJobAllocation
								&& objConfig.getNautoallot() == Enumeration.TransactionStatus.YES.gettransactionstatus())) {
							strSequenno = ",'joballocation'";//, 'registrationsection', 'registrationsectionhistory',"
//								+ "'llinter'";
//
//						String strlinter = " select "
//											+ " (select count(rt.ntransactiontestcode) from registrationtest rt, instrumentcategory ic   "
//											+ " where ic.ninstrumentcatcode = rt.ninstrumentcatcode  " 
//											+ " and rt.npreregno in ("+ InsertPreregno + ")  "
//											+ " and rt.nsitecode=" + userInfo.getNtranssitecode()
//											+ " and rt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//											+ " and ic.nstatus =  "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//											+ " and ic.nsitecode=" + userInfo.getNmastersitecode()
//											+ " and rt.ninstrumentcatcode > 0 )" + " as testcount, " 
//											+ " (select count(rp.ntransactionresultcode) from "
//											+ " registrationtest rt, instrumentcategory ic ,registrationparameter rp   "
//											+ " where ic.ninstrumentcatcode = rt.ninstrumentcatcode and rt.ntransactiontestcode=rp.ntransactiontestcode "
//											+ " and rp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//											+ " and rp.npreregno in (" + InsertPreregno + ")  and rt.nsitecode = rp.nsitecode "
//											+ " and rp.nsitecode=" + userInfo.getNtranssitecode()
//											+ " and rt.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//											+ " and rt.nsitecode=" + userInfo.getNtranssitecode()
//											+ " and ic.nstatus =  "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//											+ " and ic.nsitecode=" + userInfo.getNmastersitecode()
//											+ " and rt.ninstrumentcatcode > 0 ) as resultcount"; // and ic.ninterfacetypecode
//																							// = 1
//
//						List<Map<String, Object>> lstObject = jdbcTemplate.queryForList(strlinter);
//						nLinterSize = (Long) lstObject.get(0).get("resultcount");
//
						}
						
						final String strSelectSeqno = "select stablename,nsequenceno from seqnoregistration where stablename "
								+ "in ('registrationhistory','registrationsamplehistory','registrationtesthistory',"
								+ "'registrationsection', 'registrationsectionhistory','chaincustody'" + strSequenno + ") "
								+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
						
						
						int regSectionHistoryCode = Integer.parseInt(strArray[2]);
						
						List<SeqNoRegistration> lstSeqNo = jdbcTemplate.query(strSelectSeqno, new SeqNoRegistration());
						
						returnMap = lstSeqNo.stream().collect(Collectors.toMap(SeqNoRegistration::getStablename,
								SeqNoRegistration -> SeqNoRegistration.getNsequenceno()));
						
						String StrUpdate = "update seqnoregistration set nsequenceno = "
								+ ((int) returnMap.get("registrationhistory") + (nMultiples * newList.size()))
								+ " where stablename ='registrationhistory';"
								+ "update seqnoregistration set nsequenceno = "
								+ ((int) returnMap.get("registrationsamplehistory") + subsamplecount)
								+ " where stablename ='registrationsamplehistory';"
								+ " update seqnoregistration set nsequenceno = "
								+ ((int) returnMap.get("registrationtesthistory")
										+ (nMultiples * Integer.parseInt(strArray[0])))
								+ " where stablename ='registrationtesthistory';"
								
									+ "update seqnoregistration set nsequenceno = "
									+ ((int) returnMap.get("chaincustody") + subsamplecount)
									+ " where stablename ='chaincustody';"
									+ " update seqnoregistration set nsequenceno = "
									+ ((int) returnMap.get("registrationsection") + Integer.parseInt((strArray[2])))
									+ " where stablename ='registrationsection';"
									+ " update seqnoregistration set nsequenceno = "
									+ ((int) returnMap.get("registrationsectionhistory") + regSectionHistoryCode)
									+ " where stablename ='registrationsectionhistory';";
						
						if (// boolMyJobsScreen
								!needJobAllocation && flowType.equals("NormalFlow")) {
							StrUpdate = StrUpdate + "update seqnoregistration set nsequenceno = "
									+ ((int) returnMap.get("joballocation") + Integer.parseInt((strArray[0])))
									+ " where stablename ='joballocation';";
							
						} else if (!flowType.equals("NormalFlow") && !flowType.equals("AutoComplete")) {
							StrUpdate = StrUpdate + "update seqnoregistration set nsequenceno = "
									+ ((int) returnMap.get("joballocation") + Integer.parseInt((strArray[0])))
									+ " where stablename ='joballocation';" + " update seqnoregistration set nsequenceno = "
									+ ((int) returnMap.get("registrationsection") + Integer.parseInt((strArray[2])))
									+ " where stablename ='registrationsection';"
									+ " update seqnoregistration set nsequenceno = "
									+ ((int) returnMap.get("registrationsectionhistory") + regSectionHistoryCode)
									+ " where stablename ='registrationsectionhistory';"
									+ " update seqnoregistration set nsequenceno =  "
									+ ((int) returnMap.get("sampleapprovalhistory") + newList.size())
									+ " where stablename = 'sampleapprovalhistory';"
									+ " update seqnoregistration set nsequenceno = "
									+ ((int) returnMap.get("registrationdecisionhistory") + newList.size())
									+ " where stablename = 'registrationdecisionhistory'";
						} else if (flowType.equals("AutoComplete")) {
							StrUpdate = StrUpdate + "update seqnoregistration set nsequenceno = "
									+ ((int) returnMap.get("joballocation") + Integer.parseInt((strArray[0])))
									+ " where stablename ='joballocation';" + "update seqnoregistration set nsequenceno = "
									+ ((int) returnMap.get("registrationsection") + Integer.parseInt((strArray[2])))
									+ " where stablename ='registrationsection';"
									+ "update seqnoregistration set nsequenceno = "
									+ ((int) returnMap.get("registrationsectionhistory") + regSectionHistoryCode)
									+ " where stablename ='registrationsectionhistory';";
						}
						jdbcTemplate.execute(StrUpdate);
						
						
						returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
								Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
						returnMap.put("insertlistpreregno", InsertPreregno);
						returnMap.put("FlowType", flowType);
					} else {
						final String expiredMethod = stringUtilityFunction.fnDynamicListToString(expiredMethodTestList, "getSmethodname");
						returnMap.put("NeedConfirmAlert", true);
						
						final String message = commonFunction.getMultilingualMessage("IDS_TESTWITHEXPIREDMETHODCONFIRM",
								userInfo.getSlanguagefilename());
						returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
								message.concat(" " + expiredMethod));
					}
				} else {
					returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), "IDS_ADDTESTTOREGISTERSAMPLES");
				}
			} else {
				returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), "IDS_NOTESTAVAILABLETOREGISTER");
			}
		}
		return returnMap;
	}
	
	private String fnCheckDefaultResult(String npreregno, UserInfo userInfo) throws Exception {

		final String strCheckDefaultResult = " select case when tgtp.nparametertypecode="
											+ Enumeration.ParameterType.NUMERIC.getparametertype()
											+ " then tgtnp.sresultvalue else tgtcp.scharname end sresultvalue, tgtp.nparametertypecode, "
											+ " tgtp.ntestgrouptestparametercode "
											+ " from registrationtest rt "
											+ " inner join testgrouptest tgt on tgt.ntestgrouptestcode = rt.ntestgrouptestcode "
											+ " and tgt.nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
											+ " and rt.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
											+ " and tgt.nsitecode = "	+userInfo.getNmastersitecode()
											+ " and rt.nsitecode = "+userInfo.getNtranssitecode()
											+ " inner join testgrouptestparameter tgtp on tgtp.ntestgrouptestcode = tgt.ntestgrouptestcode "
											+ " and tgtp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
											+ " and tgt.nsitecode = "	+userInfo.getNmastersitecode()
											+ " left outer join testgrouptestcharparameter tgtcp on tgtcp.ntestgrouptestparametercode = tgtp.ntestgrouptestparametercode "
											+ " and tgtcp.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
											+ " and tgtcp.nsitecode = "	+userInfo.getNmastersitecode()
											+ " left outer join testgrouptestnumericparameter tgtnp on tgtnp.ntestgrouptestparametercode = tgtp.ntestgrouptestparametercode "
											+ " and tgtnp.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
											+ " and tgtnp.nsitecode = "	+userInfo.getNmastersitecode()
											+ " where rt.npreregno in ("
											+ npreregno + ") and tgtp.nresultmandatory = "
											+ Enumeration.TransactionStatus.YES.gettransactionstatus() 
											+ " and tgtp.nparametertypecode in("
											+ Enumeration.ParameterType.NUMERIC.getparametertype() + ","
											+ Enumeration.ParameterType.CHARACTER.getparametertype() + ") "
											+ " and case when tgtp.nparametertypecode=" + Enumeration.ParameterType.NUMERIC.getparametertype()
											+ " then tgtnp.sresultvalue else tgtcp.scharname end is null ";
		final List<RegistrationParameter> listRegistrationParameter = jdbcTemplate.query(strCheckDefaultResult,
				new RegistrationParameter());
		if (listRegistrationParameter.isEmpty()) {
			return Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
		} else {
			return commonFunction.getMultilingualMessage("IDS_DEFAULTRESULTVALUEISNOTAVAILABLETOREEGISTER",
					userInfo.getSlanguagefilename());
		}
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> acceptRegistration(Map<String, Object> inputMap) throws Exception {


		//LOGGER.info("Date3:"+ new Date());
		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditOld = new JSONObject();
		JSONObject jsonAuditNew = new JSONObject();
	
		//List<Map<String, Object>> objlst = new ArrayList<Map<String, Object>>();
		// ALPD-5619 - moved MapPreregnoWithArno for use this map for emailAlertTransaction Gowtham R on 28/03/2025 - Mail alert transaction > NULL displayed in reference no column.
		Map<Integer, String> mapPreregnoWithArno = new HashMap<Integer, String>();
		
		final ObjectMapper RegistrationMapper = new ObjectMapper();
		final UserInfo userInfo = RegistrationMapper.convertValue(inputMap.get("userinfo"),
				new TypeReference<UserInfo>() {
				});

		//Commented by L.Subashini on 8/5/2025 as it is unused -start
//		Map<String, Object> objAuditMap = new HashMap<String, Object>() {
//			{
//				put("npreregno", inputMap.get("npreregno"));
//				put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
//			}
//		};

//		Map<String, Object> auditMap = (Map<String, Object>) transactionDAOSupport.getRegistrationTestAudit(objAuditMap, userInfo).getBody();
		
		//LOGGER.info("Date3a:"+ new Date());
//		List<Map<String, Object>> lstDataTest1 = (List<Map<String, Object>>) auditMap.get("RegistrationGetTest");
		//Commented by L.Subashini on 8/5/2025 as it is unused end
		
		
		// ALPD-1154
//		List<Map<String, Object>> lstDataTest = lstDataTest1.stream()
//				.filter(m -> ((Integer) m.get("ntransactionstatus")) != Enumeration.TransactionStatus.REJECTED
//						.gettransactionstatus())
//				.collect(Collectors.toList());

		String sQuery = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final String strSeqnoFormatQuery = "select rsc.nperiodcode,rsc.jsondata,rsc.jsondata->>'dseqresetdate' dseqresetdate,"
										+ " rsc.jsondata->>'ssampleformat' ssampleformat, sag.nsequenceno,sag.nseqnoarnogencode,rsc.nregsubtypeversioncode"
										+ " from  approvalconfig ap, seqnoarnogenerator sag,regsubtypeconfigversion rsc "
										+ " where  sag.nseqnoarnogencode = rsc.nseqnoarnogencode  "
										+ " and sag.nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
										+ " and ap.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and ap.nsitecode="+ userInfo.getNmastersitecode()
										+ " and rsc.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and rsc.nsitecode="+ userInfo.getNmastersitecode()
										+ " and nregtypecode = "+ (int) inputMap.get("nregtypecode") 
										+ " and nregsubtypecode =" + (int) inputMap.get("nregsubtypecode")
										+ " and rsc.napprovalconfigcode=ap.napprovalconfigcode "
										+ " and rsc.ntransactionstatus="+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus();
		SeqNoArnoGenerator seqnoFormat = jdbcTemplate.queryForObject(strSeqnoFormatQuery, new SeqNoArnoGenerator());

		//LOGGER.info("Date3b:"+ new Date());
		if(seqnoFormat.getJsondata().containsKey("nneedsitewisearno") && (boolean) seqnoFormat.getJsondata().get("nneedsitewisearno"))
		{		
			seqnoFormat = registrationDAOSupport.insertSiteArnoGenerator(seqnoFormat,userInfo,inputMap);
		
		}
		//LOGGER.info("Date3c:"+ new Date());
		
		int nregtypecode = (int) inputMap.get("nregtypecode");
		int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
		int nsampletypecode = (int) inputMap.get("nsampletypecode");
		int napproveversioncode = (int) inputMap.get("napproveconfversioncode");
		boolean needJobAllocation = (boolean) inputMap.get("nneedjoballocation");
		boolean needSubSample = (boolean) inputMap.get("nneedsubsample");
		final boolean nneedmyjob = (boolean) inputMap.get("nneedmyjob");
		final boolean nneedtestinitiate = (boolean) inputMap.get("nneedtestinitiate");
		Map<String, Object> returnMap = new HashMap<>();


		if (seqnoFormat != null) {
			String insertlistpreregno = (String) inputMap.get("insertlistpreregno");

			if (!insertlistpreregno.equals("")) {
				String sCheckSampleStatus = "select npreregno from registrationhistory rh where nstatus = "
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
											+ " and nreghistorycode = any ("
											+ " select max(nreghistorycode) from registrationhistory where "
											+ " nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
											+ " and nsitecode="	+ userInfo.getNtranssitecode() 
											+ " group by npreregno" + " ) "
											+ " and rh.ntransactionstatus in ("
											+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + ","
											+ Enumeration.TransactionStatus.QUARENTINE.gettransactionstatus() + ")"
											+ " and rh.npreregno in (" + (String) inputMap.get("insertlistpreregno") + ")"
											+ " and rh.nsitecode=" + userInfo.getNtranssitecode()
											//ALPD-6048-added by Vignesh(11-07-2025)- added the LinkedList for Maintain insertion order
											+ " and rh.nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" order by npreregno";

				List<RegistrationHistory> listRegHistory = jdbcTemplate.query(sCheckSampleStatus,
						new RegistrationHistory());

				if (!listRegHistory.isEmpty()) {
					insertlistpreregno = stringUtilityFunction.fnDynamicListToString(listRegHistory, "getNpreregno");
				}
			}
			//LOGGER.info("Date3d:"+ new Date());
			if (insertlistpreregno.equals("")) {
				String strNpreregno = (String) inputMap.get("npreregno");
				List<String> listOfPreregno = Arrays.asList(strNpreregno.split(","));

				String sCheckSampleStatus = "select npreregno from registrationhistory rh where "
											+ " rh.nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
											+ " and rh.nreghistorycode = any ("
											+ " select max(nreghistorycode) from registrationhistory where "
											+ " nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
											+ " and nsitecode=" + userInfo.getNtranssitecode()
											+ " group by npreregno" + " ) "
											+ " and rh.ntransactionstatus = " + Enumeration.TransactionStatus.REGISTERED.gettransactionstatus() 
											+ " and rh.npreregno in (" + strNpreregno + ") "
											+ " and rh.nsitecode=" + userInfo.getNtranssitecode();
				

				final List<RegistrationHistory> listRegHistory = jdbcTemplate.query(sCheckSampleStatus,
						new RegistrationHistory());

				if (!listRegHistory.isEmpty() && listRegHistory.size() == listOfPreregno.size()) {
					return returnMap;
				}
			}
		//	LOGGER.info("Date3e:"+ new Date());
			int nreghistorycode = (int) inputMap.get("registrationhistory");
			int nregsamplehistorycode = (int) inputMap.get("registrationsamplehistory");
			int ntesthistorycode = (int) inputMap.get("registrationtesthistory");
			int regsectionhistory = (int) inputMap.get("registrationsectionhistory");
			int regsection = (int) inputMap.get("registrationsection");
			int nchainCustodyCode = (int) inputMap.get("chaincustody");
			int nRegistrationStatus = Enumeration.TransactionStatus.REGISTERED.gettransactionstatus();
			int nApprovalStatus = Enumeration.TransactionStatus.REGISTERED.gettransactionstatus();

			int joballocation = 0;
			// int regsectionhistory = 0;
			// int regsection = 0;
			if (inputMap.containsKey("joballocation")) {
				joballocation = (int) inputMap.get("joballocation");
//				regsectionhistory = (int) inputMap.get("registrationsectionhistory");
//				regsection = (int) inputMap.get("registrationsection");
			}

			inputMap.put("nsitecode", userInfo.getNtranssitecode());
			
			//Commented by L.Subashini on 8/5/2025 as it is unused -start
//			String ssiteCode = "";
//			if (userInfo.getSsitecode() != null && userInfo.getSsitecode().trim().length() > 0
//					&& !userInfo.getSsitecode().equals("-1")) {
//				ssiteCode = userInfo.getSsitecode() + "-";
//			}
			
			//Commented by L.Subashini on 8/5/2025 as it is unused -end
			List<String> listOfPreregno = Arrays.asList(insertlistpreregno.split(","));

			if (!listOfPreregno.isEmpty() && !insertlistpreregno.isEmpty()) {
				String strSectionHistory = " select (tg.nsectioncode),rth.npreregno from "
										+ " registrationtest rt,testgrouptest tg,registrationtesthistory rth "
										+ " where tg.ntestgrouptestcode=rt.ntestgrouptestcode "
										+ " and rth.ntransactiontestcode=rt.ntransactiontestcode "
										+ " and rth.ntesthistorycode = any(select max(ntesthistorycode) from registrationtesthistory rth1 "
										+ " where rth1.ntransactiontestcode=rt.ntransactiontestcode "
										+ " and rth1.nsitecode=" + userInfo.getNtranssitecode() 
										+ " and rth1.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ) "
										+ " and rth.npreregno in (" + insertlistpreregno + ") "
										+ " and rt.nsitecode="+ userInfo.getNtranssitecode()
										+ " and rth.nsitecode=" + userInfo.getNtranssitecode()
										+ " and rth.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and tg.nsitecode=" + userInfo.getNmastersitecode()
										+ " and rth.ntransactionstatus  not in ("
										+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ","
										+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus()
										+ ") group by rth.npreregno,tg.nsectioncode ";
				final List<RegistrationTest> lstRegistrationSectionHistory = jdbcTemplate.query(strSectionHistory,
						new RegistrationTest());
				String flowType = (String) inputMap.get("FlowType");

//				Map<Integer, String> MapPreregnoWithArno = RegistrationMapper.convertValue(inputMap.get("MapPreregnoWithArno"), 
//									new TypeReference<Map<Integer, String>>() {});
			//	LOGGER.info("Date3f:"+ new Date());
				mapPreregnoWithArno = projectDAOSupport.getfnInfFormat(Arrays.asList(insertlistpreregno.split(",")),
						seqnoFormat.getNsequenceno() + 1, seqnoFormat.getSsampleformat(),
						seqnoFormat.getDseqresetdate(), seqnoFormat.getNseqnoarnogencode(), userInfo,
						seqnoFormat.getJsondata(), seqnoFormat.getNregsubtypeversioncode(),
						seqnoFormat.getNperiodcode(), seqnoFormat.getNsequenceno());
			//	LOGGER.info("Date3g:"+ new Date());
				final Map<Integer, String> mapTransactionsampletoArno = new TreeMap<Integer, String>();

				final String strSubSample = " select jrsh.npreregno,jrsh.ntransactionsamplecode from "
											+ " registrationsamplehistory jrsh  where jrsh.nsamplehistorycode in "
											+ " (select max(jqrth1.nsamplehistorycode) as testhistorycode "
											+ " from registrationsamplehistory jqrth1, registrationsample jqja1,registration r "
											+ " where jqrth1.npreregno = jqja1.npreregno"
											+ " and jqrth1.ntransactionsamplecode = jqja1.ntransactionsamplecode "
											+ " and jqja1.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
											+ " and jqrth1.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " and r.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " and r.npreregno=jqja1.npreregno and r.npreregno in (" + insertlistpreregno + ")"
											+ " and jqrth1.nsitecode="+ userInfo.getNtranssitecode()
											+ " and jqja1.nsitecode ="+ userInfo.getNtranssitecode()
											+ " and r.nsitecode=" + userInfo.getNtranssitecode()
											+ " group by jqrth1.npreregno,jqrth1.ntransactionsamplecode)"
											+ " and jrsh.ntransactionstatus ="+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() 
											+ " and jrsh.npreregno in ("+ insertlistpreregno + ") "
											+ " and jrsh.nsitecode=" + userInfo.getNtranssitecode()
											+ " and jrsh.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " order by jrsh.ntransactionsamplecode asc ";

				final List<RegistrationSample> lstRegistrationSubSample = jdbcTemplate.query(strSubSample,
						new RegistrationSample());
			//	LOGGER.info("Date3h:"+ new Date());
				String strSecHistory = "";
				String strSection = "";
				String autoFlowQuery = "";

				for (int k = 0; k < listOfPreregno.size(); k++) {
					int i = 1;
					String subsampleArno = "";
					for (int l = 0; l < lstRegistrationSubSample.size(); l++) {
						if (lstRegistrationSubSample.get(l).getNpreregno() == Integer.parseInt(listOfPreregno.get(k))) {
							final String formatted = String.format("%02d", i);
							subsampleArno = "" + mapPreregnoWithArno.get(Integer.parseInt(listOfPreregno.get(k))) + "-"
									+ formatted;
							i++;
							mapTransactionsampletoArno.put(lstRegistrationSubSample.get(l).getNtransactionsamplecode(),
									subsampleArno);
						}

					}
				}

				if (flowType.equals("NormalFlow") || flowType.equals("AutoApprovalFlow")) {
					
					final String strRegistrationHistory = " insert into registrationhistory (nreghistorycode,npreregno,"
														+ " ntransactionstatus,dtransactiondate,nusercode,nuserrolecode "
														+ " ,ndeputyusercode,ndeputyuserrolecode,scomments,nsitecode,nstatus,noffsetdtransactiondate,ntransdatetimezonecode) "
														+ " select " + nreghistorycode + "+Rank() over (order by npreregno) as nreghistorycode, "
														+ " npreregno npreregno," + nRegistrationStatus + " as ntransactionstatus, '"
														+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' as dtransactiondate," + userInfo.getNusercode() + ","
														+ userInfo.getNuserrole() + "," + userInfo.getNdeputyusercode() + ","
														+ userInfo.getNdeputyuserrole() + ",'" + stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "',"
														+ userInfo.getNtranssitecode() + ","
														+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
														+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode()
														+ " from  registrationhistory where npreregno in ( " + insertlistpreregno
														+ ") and ntransactionstatus ="+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() 
														+ " and nsitecode="	+ userInfo.getNtranssitecode()
														+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					jdbcTemplate.execute(strRegistrationHistory);
				//	LOGGER.info("Date3i:"+ new Date());
					final String query1 = "insert into registrationsamplehistory (nsamplehistorycode,ntransactionsamplecode,"
										+ " npreregno,ntransactionstatus ,dtransactiondate,nusercode,nuserrolecode,ndeputyusercode,"
										+ " ndeputyuserrolecode,scomments,nsitecode,nstatus,noffsetdtransactiondate,ntransdatetimezonecode) "
										+ " select " + nregsamplehistorycode
										+ "+Rank() over (order by ntransactionsamplecode) as nsamplehistorycode, ntransactionsamplecode as ntransactionsamplecode,"
										+ " npreregno npreregno," + nRegistrationStatus + " as ntransactionstatus, '"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' as dtransactiondate," + userInfo.getNusercode() + ","
										+ userInfo.getNuserrole() + "," + userInfo.getNdeputyusercode() + ","
										+ userInfo.getNdeputyuserrole() + ",'" + stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "',"
										+ userInfo.getNtranssitecode() + ","
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
										+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode()
										+ " from registrationsamplehistory where npreregno in ( " + insertlistpreregno + ")  "
										+ " and nsitecode=" + userInfo.getNtranssitecode() + " and nsamplehistorycode in "
										+ "(select max(jqrth1.nsamplehistorycode) as testhistorycode "
										+ " from registrationsamplehistory jqrth1,registrationsample jqja1,registration r "
										+ " where  jqrth1.npreregno = jqja1.npreregno "
										+ " and jqrth1.ntransactionsamplecode = jqja1.ntransactionsamplecode "
										+ " and jqja1.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and jqrth1.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and r.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and r.npreregno=jqja1.npreregno and r.npreregno in (" + insertlistpreregno + ") "
										+ " and jqrth1.nsitecode = "+ userInfo.getNtranssitecode()
										+ " and jqja1.nsitecode= "+ userInfo.getNtranssitecode()
										+ " and r.nsitecode=" + userInfo.getNtranssitecode()
										+ " group by jqrth1.npreregno,jqrth1.ntransactionsamplecode)"
										+ " and ntransactionstatus not in ("
										+ +Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","
										+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ")";
					jdbcTemplate.execute(query1);
					//LOGGER.info("Date3j:"+ new Date());
					String query2 = "insert into registrationtesthistory (ntesthistorycode,ntransactiontestcode,ntransactionsamplecode,npreregno,nformcode,"
									+ "ntransactionstatus,nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode "
									+ ",scomments,dtransactiondate,nsampleapprovalhistorycode,nsitecode, nstatus,noffsetdtransactiondate,ntransdatetimezonecode) "
									+ " select " + ntesthistorycode
									+ "+Rank() over (order by ntransactiontestcode) as ntesthistorycode,ntransactiontestcode,ntransactionsamplecode,npreregno npreregno"
									+ "," + userInfo.getNformcode() + " as nformcode ," + nApprovalStatus
									+ " as ntransactionstatus," + userInfo.getNusercode() + "," + userInfo.getNuserrole() + ","
									+ userInfo.getNdeputyusercode() + "," + userInfo.getNdeputyuserrole() + ",'"
									+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "','" + dateUtilityFunction.getCurrentDateTime(userInfo)
									+ "' as dtransactiondate, " + Enumeration.TransactionStatus.NA.gettransactionstatus() + ","
									+ userInfo.getNtranssitecode() + ","
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
									+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode()
									+ " from registrationtesthistory where npreregno in ( " + insertlistpreregno + ")"
									+ " and nsitecode = " + userInfo.getNtranssitecode()
									+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and ntesthistorycode in (select max(jqrth1.ntesthistorycode) as testhistorycode "
									+ " from registrationtesthistory jqrth1,registrationtest jqja1,registration r "
									+ " where jqrth1.npreregno = jqja1.npreregno "
									+ " and jqrth1.ntransactionsamplecode = jqja1.ntransactionsamplecode "
									+ " and jqja1.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and jqrth1.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and r.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and r.npreregno=jqja1.npreregno and r.npreregno in (" + insertlistpreregno + ") "
									+ " and jqrth1.nsitecode="+ userInfo.getNtranssitecode()
									+ " and jqja1.nsitecode="+ userInfo.getNtranssitecode()
									+ " and r.nsitecode="+ userInfo.getNtranssitecode()
									+ " group by jqrth1.npreregno,jqrth1.ntransactiontestcode) and"
									+ " ntransactionstatus not in ("
									+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","
									+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ");";
					jdbcTemplate.execute(query2);
					//LOGGER.info("Date3k:"+ new Date());
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
								+ " and tgtf.nsitecode="+ userInfo.getNmastersitecode()
								+ " and tgtf.ntransactionstatus=" + Enumeration.TransactionStatus.YES.gettransactionstatus()
								+ " where rp.ntestgrouptestparametercode=tgtp.ntestgrouptestparametercode "
								+ " and rp.npreregno in (" + insertlistpreregno + ")" 
								+ " and rp.nsitecode=" + userInfo.getNtranssitecode() 
								+ " and rp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and tgtp.nsitecode=" + userInfo.getNmastersitecode() 
								+ " and tgtp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ ";";

					jdbcTemplate.execute(query3);
					//LOGGER.info("Date3l:"+ new Date());
					String query4 = "insert into resultparametercomments (ntransactionresultcode,ntransactiontestcode "
									+ ",npreregno, ntestgrouptestparametercode ,ntestparametercode "
									+ ",jsondata,nsitecode, nstatus )  select rp.ntransactionresultcode "
									+ ",rp.ntransactiontestcode,rp.npreregno,rp.ntestgrouptestparametercode,rp.ntestparametercode , '{}'::jsonb,"
									+ userInfo.getNtranssitecode() + ","
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " from registrationparameter rp where npreregno in (" + insertlistpreregno + ")"
									+ " and nsitecode=" + userInfo.getNtranssitecode() 
									+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ ";";

					jdbcTemplate.execute(query4);
					//LOGGER.info("Date3m:"+ new Date());		

				} else {
					autoFlowQuery = registrationDAOSupport.fnStringFormationForAutoFlow(inputMap, flowType, napproveversioncode);
					//LOGGER.info("Date3n:"+ new Date());
					if (autoFlowQuery.equals("IDS_NODEFAULTRESULT")) {
						returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), commonFunction
								.getMultilingualMessage("IDS_NODEFAULTRESULT", userInfo.getSlanguagefilename()));
						LOGGER.info("Formed Query From the Auto Approval/Complete Method =======>" + autoFlowQuery);
						return returnMap;

					} else {
//						strRegistrationHistory = strRegistrationHistory + autoFlowQuery;
//						logger.info("Formed Query From the Auto Approval/Complete Method =======>" + autoFlowQuery);
					}
				}

				for (int a = 0; a < listOfPreregno.size(); a++) {
					String strRegistrationArno = // strRegistrationArno.concat(
							"update registrationarno "
									// + Enumeration.ReturnStatus.ROWLOCK.getreturnstatus() + " "
									+ " set sarno ='"// + ssiteCode
									+ mapPreregnoWithArno.get(Integer.parseInt(listOfPreregno.get(a))) + "'"
									// + ", napprovalversioncode = "
									// + lstgetVersionTemp.get(0).getNapproveconfversioncode() +
									+ " where npreregno =" + Integer.parseInt(listOfPreregno.get(a)) + " and nsitecode="
									+ userInfo.getNtranssitecode();

					jdbcTemplate.execute(strRegistrationArno);

					if (nsampletypecode == Enumeration.SampleType.INSTRUMENT.getType()) {

						final String query = "update instrumentcalibration set dopendate='"
											+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " nopenusercode=" + userInfo.getNusercode()
											+ ",ntzopendate=" + userInfo.getNtimezonecode() + ", "
											+ " sopenreason='Auto UnderCalibration',sarno='"
											+ mapPreregnoWithArno.get(Integer.parseInt(listOfPreregno.get(a)))
											+ "', dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "' where npreregno="
											+ listOfPreregno.get(a);
						jdbcTemplate.execute(query);

					}
				}
				//LOGGER.info("Date3o:"+ new Date());
				for (int b = 0; b < lstRegistrationSubSample.size(); b++) {
					String strSampleArno = "update registrationsamplearno "
										// + Enumeration.ReturnStatus.ROWLOCK.getreturnstatus() + ""
										+ " set ssamplearno ='" // + ssiteCode
										+ mapTransactionsampletoArno
												.get(lstRegistrationSubSample.get(b).getNtransactionsamplecode())
										+ "' where ntransactionsamplecode ="
										+ lstRegistrationSubSample.get(b).getNtransactionsamplecode() 
										+ " and nsitecode="	+ userInfo.getNtranssitecode();
					
					jdbcTemplate.execute(strSampleArno);
				}
				//LOGGER.info("Date3p:"+ new Date());
				transactionDAOSupport.insertChainCustody(nchainCustodyCode, insertlistpreregno, needSubSample, userInfo);
				//LOGGER.info("Date3q:"+ new Date());
				
				if(!needJobAllocation && !nneedmyjob && !nneedtestinitiate)
				{
					inputMap.put("sintegrationpreregno",insertlistpreregno);
					inputMap.put("svalidationstatus",Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()); 
					//Added by sonia on 6th Sep 2024 for JIRA ID: ALPD-4769
					
					transactionDAOSupport.createIntegrationRecord(inputMap, userInfo);
				}
				
				LOGGER.info("Date3r:"+ new Date());

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

				if (!needJobAllocation) {
					String query8 = "insert into joballocation (njoballocationcode,npreregno,ntransactionsamplecode,ntransactiontestcode,nsectioncode "
								+ ",nusercode,nuserperiodcode,ninstrumentcategorycode "
								+ ",ninstrumentcode,ninstrumentnamecode,ninstrumentperiodcode, ntechniquecode "
								+ ",ntimezonecode,nsitecode, nstatus,ntestrescheduleno,nuserrolecode,jsondata, jsonuidata) "
								+ "select " + joballocation
								+ "+rank() over(order by rth.ntransactiontestcode),rt.npreregno,rt.ntransactionsamplecode,rt.ntransactiontestcode,tgt.nsectioncode,-1,"
								+ "-1,-1,-1,-1,-1,1,-1," + userInfo.getNtranssitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "," + " 0,"
								+ " (select nuserrolecode  from treetemplatetransactionrole "
								+ "	where nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and nsitecode="	+ userInfo.getNmastersitecode()
								+ " and ntemptransrolecode in (select max(ttr.ntemptransrolecode) "
								+ "	from treetemplatetransactionrole ttr,approvalconfig ac,approvalconfigversion acv "
								+ "	where ac.napprovalconfigcode=ttr.napprovalconfigcode "
								+ "	and ac.napprovalconfigcode = acv.napprovalconfigcode "
								+ "	and ttr.ntreeversiontempcode = acv.ntreeversiontempcode " 
								+ " and ttr.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ "	and acv.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ "	and ac.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and ttr.nsitecode="	+ userInfo.getNmastersitecode()
								+ " and acv.nsitecode="	+ userInfo.getNmastersitecode()
								+ " and ac.nsitecode="	+ userInfo.getNmastersitecode()
								+ "	and ac.nregtypecode= "+ nregtypecode 
								+ " and ac.nregsubtypecode= " + nregsubtypecode
								+ " and acv.napproveconfversioncode =" + napproveversioncode
								+ " group by ttr.ntreeversiontempcode" + "	)" + ")  nuserrolecode,"
								+ " json_build_object('duserblockfromdate','"
								+ dateUtilityFunction
										.instantDateToStringWithFormat(dateUtilityFunction.getCurrentDateTime(userInfo), "yyyy-MM-dd HH:mm:ss")
								+ "','duserblocktodate','"
								+ dateUtilityFunction.instantDateToStringWithFormat(dateUtilityFunction.getCurrentDateTime(userInfo),
										"yyyy-MM-dd HH:mm:ss")
								+ "','suserblockfromtime','00:00','suserblocktotime','00:00','suserholdduration','0','dinstblockfromdate',NULL,"
								+ "'dinstblocktodate',NULL,'sinstblockfromtime',NULL,'sinstblocktotime',NULL,'sinstrumentholdduration',NULL,"
								+ "'scomments','','noffsetduserblockfromdate','"
								+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "','noffsetduserblocktodate','"
								+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "')::jsonb, "
								+ " json_build_object('duserblockfromdate','"
								+ dateUtilityFunction
										.instantDateToStringWithFormat(dateUtilityFunction.getCurrentDateTime(userInfo), "yyyy-MM-dd HH:mm:ss")
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
								+ " and rth.nsitecode="+ userInfo.getNtranssitecode()
								+ " and rth.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and rt.nsitecode=" + userInfo.getNtranssitecode()
								+ " and rt.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and rth.ntesthistorycode in "
								+ " (select max(rth1.ntesthistorycode) as testhistorycode from "
								+ " registrationtesthistory rth1,registrationtest rt1,registration r "
								+ " where rth1.ntransactiontestcode = rt1.ntransactiontestcode "
								+ " and rth1.ntransactionstatus="+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() 
								+ " and rth1.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and rth1.npreregno  in (" + insertlistpreregno + ")"
								+ " and rth1.nsitecode="+ userInfo.getNtranssitecode()
								+ " and rt1.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and rt1.nsitecode="+ userInfo.getNtranssitecode()
								+ " and r.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and r.nsitecode="+ userInfo.getNtranssitecode()
								+ " and rth1.ntransactiontestcode not in (select max(ntransactiontestcode) as ntransactiontestcode"
								+ " from registrationtesthistory where  npreregno  in (" + insertlistpreregno + ")  "
								+ " and nsitecode=" + userInfo.getNtranssitecode() 
								+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and ntransactionstatus="
								+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus()
								+ " group by npreregno,ntransactiontestcode,ntransactionsamplecode) "
								+ " group by rth1.npreregno,rth1.ntransactionsamplecode,rth1.ntransactiontestcode)";
					jdbcTemplate.execute(query8);

				}

				strSection = "insert into registrationsection(nregistrationsectioncode, npreregno, "
						+ "nsectioncode, nsitecode,nstatus) values" + strSection.substring(0, strSection.length() - 1)
						+ ";";

				jdbcTemplate.execute(strSection);

				strSecHistory = "insert into registrationsectionhistory(nsectionhistorycode, npreregno, "
						+ "nsectioncode, ntransactionstatus, nsitecode,nstatus) values"
						+ strSecHistory.substring(0, strSecHistory.length() - 1) + ";";

				jdbcTemplate.execute(strSecHistory);
				//LOGGER.info("Date3s:"+ new Date());
				
				returnMap = registrationDAOSupport.getDynamicRegistration(inputMap, userInfo);
			
				//	LOGGER.info("Date3t:"+ new Date());
				
				jsonAuditOld.put("registration", (List<Map<String, Object>>) inputMap.get("registration"));
				jsonAuditNew.put("registration", (List<Map<String, Object>>) returnMap.get("RegistrationGetSample"));
	
								
				if((int) inputMap.get("nportalrequired") ==  Enumeration.TransactionStatus.YES.gettransactionstatus())
				{
					final String str1 = "select settings.ssettingvalue from settings where nsettingcode=" 
										+  Enumeration.Settings.SCHEDULER_BASED_STATUS_UPDATE_TO_PORTAL.getNsettingcode()+ ";";
					final String ssettingvalue = jdbcTemplate.queryForObject(str1, String.class);
				
					if(ssettingvalue.equals("3"))
					{
						final String str =" select npreregno,(registration.jsondata->'nexternalordercode')::int as nexternalordercode,jsondata->>'externalorderid' as sexternalorderid, "
										   + Enumeration.TransactionStatus.PARTIALLY.gettransactionstatus()+ " ntransactionstatus"
										   + " from registration where (registration.jsondata->>'nexternalordercode') IS NOT NULL "
										   + "AND (registration.jsondata->>'nexternalordercode') != 'nexternalordercode' and npreregno in("+inputMap.get("npreregno")+")";

						List<ExternalOrderTest> lstexternalorderstatus = jdbcTemplate.query(str, new ExternalOrderTest());				
						if(lstexternalorderstatus.size()>0)
						{
							insertExternalOrderStatus(userInfo,lstexternalorderstatus);						
						}
					}
					else
					{
						// sample order update to portal
						returnMap.putAll((Map<String, Object>) externalOrderSupport.sampleOrderUpdate(inputMap, (String) inputMap.get("npreregno"),
								userInfo, Enumeration.TransactionStatus.PARTIALLY.gettransactionstatus(),
								Enumeration.TransactionStatus.REGISTERED.gettransactionstatus(), true).getBody());
					}
				}
				
			//	LOGGER.info("Date3u:"+ new Date());
//				//ALPD-4129 For MAterial Accounting
//				if((int) inputMap.get("nportalrequired") ==  Enumeration.TransactionStatus.YES.gettransactionstatus() && (int) inputMap.get("ninsertMaterialInventoryTrans") ==  Enumeration.TransactionStatus.YES.gettransactionstatus())
//				{				
//				      insertMaterialinventoryTrans(String.valueOf(Enumeration.CalculationType.QUANTITY.getcalculationtype()+","+Enumeration.CalculationType.SAMPLE.getcalculationtype()),(String) inputMap.get("npreregno"),userInfo);				
//			    }
//				


				auditmap.put("nregtypecode", nregtypecode);
				auditmap.put("nregsubtypecode", nregsubtypecode);
				auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
				actionType.put("registration", "IDS_REGISTER");
		
				//		LOGGER.info("Date3v:"+ new Date());
				
				auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, jsonAuditNew, actionType, auditmap, false, userInfo);
		
				//		LOGGER.info("Date3w:"+ new Date());
				
			} else {
				returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						commonFunction.getMultilingualMessage("IDS_ATLEASTONETESTMUSTBEPREREGISTER",
								userInfo.getSlanguagefilename()));
			}
			
			Map<String, Object> mailMap = new HashMap<String, Object>();
			
			
			//for NFC Client
			for (int a = 0; a < listOfPreregno.size(); a++) {
				
				final String sectionQuery="select nsectioncode from registrationsection where npreregno="+Integer.parseInt(listOfPreregno.get(a))
											+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final List<String> sectionList = jdbcTemplate.queryForList(sectionQuery, String.class);
				
				for(String nsectioncode :sectionList) {
					//try {
						mailMap.put("ncontrolcode",(int)inputMap.get("ncontrolcode"));
						mailMap.put("npreregno",Integer.parseInt(listOfPreregno.get(a)));
						mailMap.put("nsectioncode",Integer.parseInt(nsectioncode));
						mailMap.put("ssystemid",mapPreregnoWithArno.get(Integer.parseInt(listOfPreregno.get(a))));
						ResponseEntity<Object> mailResponse = emailDAOSupport.createEmailAlertTransaction(mailMap, userInfo);
//					} catch (Exception e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
				}
			}
			//LOGGER.info("Date3x:"+ new Date());
		} else {
			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), commonFunction
					.getMultilingualMessage("IDS_SEQUENCEFORMATNOTAVAILABLE", userInfo.getSlanguagefilename()));
		}	

		//LOGGER.info("Date4:"+ new Date());
		return returnMap;	
	}	
	
	public void insertExternalOrderStatus(UserInfo userinfo,List<ExternalOrderTest>  lstexternalorderstatus)
	{
		try {
			String updatestr="";
			String queryString ="insert into externalorderstatus (npreregno,nexternalordercode,sexternalorderid,ntransactionstatus,nsentstatus,dtransactiondate,nsitecode,nstatus) values ";						
			for (int f = 0; f < lstexternalorderstatus.size(); f++) {
				int limsstatus=(int)lstexternalorderstatus.get(f).getNtransactionstatus()== Enumeration.TransactionStatus.RECEIVED.gettransactionstatus() ? Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() : (int)lstexternalorderstatus.get(f).getNtransactionstatus()==Enumeration.TransactionStatus.PARTIALLY.gettransactionstatus() ? Enumeration.TransactionStatus.REGISTERED.gettransactionstatus() :(int)lstexternalorderstatus.get(f).getNtransactionstatus();
				queryString = queryString + "(" 
						+ lstexternalorderstatus.get(f).getNpreregno() + ","
						+ lstexternalorderstatus.get(f).getNexternalordercode() + ",'"
						+ lstexternalorderstatus.get(f).getSexternalorderid() + "',"
						+ lstexternalorderstatus.get(f).getNtransactionstatus() + ",8,'"
						+ dateUtilityFunction.getCurrentDateTime(userinfo)+"',"
						+ userinfo.getNtranssitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
				updatestr=updatestr+"update externalorder set ntransactionstatus="+limsstatus +" where nexternalordercode="+lstexternalorderstatus.get(f).getNexternalordercode()+";";

			}
			jdbcTemplate.execute(queryString.substring(0, queryString.length() - 1));
			jdbcTemplate.execute(updatestr.substring(0, updatestr.length() - 1));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public ResponseEntity<Object> getTestBasesdTestPackage(Map<String, Object> inputMap) throws Exception {
		return registrationDAOSupport.getTestBasesdTestPackage(inputMap);
	}

	
	
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> createTest(final UserInfo userInfo, final List<String> listSample,
			final List<TestGroupTest> listTest, final int nregtypecode, final int nregsubtypecode,
			Map<String, Object> inputMap) throws Exception {

		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObject = new JSONObject();
		
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		
		int nTransactionTestCode = (int) inputMap.get("registrationtest") + 1;
		int nTesthistoryCode = (int) inputMap.get("registrationtesthistory") + 1;
		int nRegistrationParameterCode = (int) inputMap.get("registrationparameter") + 1;
		
		final int nsampletypecode = (int) inputMap.get("nsampletypecode");
		final boolean needJobAllocation = (boolean) inputMap.get("nneedjoballocation");
		final boolean nneedmyjob = (boolean) inputMap.get("nneedmyjob");

		final List<RegistrationSampleHistory> listAvailableSample = (List<RegistrationSampleHistory>) inputMap
				.get("AvailableSample");


		int nJoballocationCode = inputMap.containsKey("joballocation") ? (int) inputMap.get("joballocation") : 0;

		int nRegHistoryCode = inputMap.containsKey("registrationhistory") ? (int) inputMap.get("registrationhistory")
				: 0;

		int nregSectionCode = inputMap.containsKey("registrationsection") ? (int) inputMap.get("registrationsection")
				: 0;
		int nregistrationsamplehistory = inputMap.containsKey("registrationsamplehistory")
				? (int) inputMap.get("registrationsamplehistory")
				: 0;
		int nSectionHistoryCode = inputMap.containsKey("registrationsectionhistory")
				? (int) inputMap.get("registrationsectionhistory")
				: 0;
		
		//Commented by L.Subashini on 8/5/2025 as it is unused-start
		//int nllintercode = 0;
//		if (inputMap.containsKey("llinterParameterList")) {
//			nllintercode = (int) inputMap.get("llinter");
//		}
//		int llinterParameterListCount = 0;
//		if (inputMap.containsKey("llinterParameterList")) {
//			llinterParameterListCount = (int) inputMap.get("llinterParameterList");
//		}
		//Commented by L.Subashini on 8/5/2025 as it is unused -end
		int approvalConfigVersionCode = -1;
		List<ApprovalConfigAutoapproval> approvalConfigAutoApprovals = new ArrayList<ApprovalConfigAutoapproval>();
		if (inputMap.containsKey("approvalconfigautoapproval")) {
			approvalConfigAutoApprovals = (List<ApprovalConfigAutoapproval>) inputMap.get("approvalconfigautoapproval");
		}

		final Instant utcDateTime = dateUtilityFunction.getCurrentDateTime(userInfo);

		String stestStatus = "-2";
		int parameterStatus = Enumeration.TransactionStatus.REGISTERED.gettransactionstatus();
		// int nRecievedStatus = -2;

       //ALPD-3615--Start
		if((boolean) inputMap.get("loadAdhocTest")) {
			final List<TestGroupSpecSampleType> listTestGroupSpecSampleType = objMapper.convertValue(inputMap.get("nspecsampletypecode"),
					new TypeReference<List<TestGroupSpecSampleType>>() {
			}); 
			if(!listTestGroupSpecSampleType.isEmpty()) {
				final String sTestQry="select * from testgrouptest "
								+ " where ntestcode="+listTest.get(0).getNtestcode()+ ""
								+ " and nspecsampletypecode="+listTestGroupSpecSampleType.get(0).getNspecsampletypecode()
								+ " and nstatus=" +Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and nsitecode="+ userInfo.getNmastersitecode();
				final List<TestGroupTest> lstTestPresent = (List<TestGroupTest>) jdbcTemplate.query(sTestQry,
						new TestGroupTest());
				if(lstTestPresent.isEmpty()) {			
					registrationDAOSupport.createTestGroupTest(userInfo, null, listTestGroupSpecSampleType, listTest, inputMap);
				}
				else {
					final String sQuery="select * from testgrouptest where ntestcode="+listTest.get(0).getNtestcode()
										+ " and nspecsampletypecode="+listTestGroupSpecSampleType.get(0).getNspecsampletypecode()
										+ " and nisvisible="+Enumeration.TransactionStatus.YES.gettransactionstatus()
										+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and nsitecode="+ userInfo.getNmastersitecode()
										+"";
					final List<TestGroupTest> lstTestVisible = (List<TestGroupTest>) jdbcTemplate.query(sQuery,new TestGroupTest());
					if(lstTestVisible.isEmpty()) {
						final String sUpdateQuery="update testgrouptest set nisvisible="+listTest.get(0).getNisvisible()
												+ " where ntestcode="+listTest.get(0).getNtestcode()+ ""
												+ " and nspecsampletypecode="+listTestGroupSpecSampleType.get(0).getNspecsampletypecode()
												+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
												+ " and nsitecode="+ userInfo.getNmastersitecode()
												+"";
						jdbcTemplate.execute(sUpdateQuery);
					}

				}
			}
		}
		//ALPD-3615--End
		
		final String sntestgrouptestcode = stringUtilityFunction.fnDynamicListToString(listTest, "getNtestgrouptestcode");

		final String sParameterQuery = "select case when tgtp.nparametertypecode = "
										+ Enumeration.ParameterType.NUMERIC.getparametertype() 
										+ " then tgtnp.sresultvalue"
										+ " else case when tgtp.nparametertypecode = " + Enumeration.ParameterType.PREDEFINED.getparametertype()
										+ " then tgtpp.spredefinedname else case when tgtp.nparametertypecode = "
										+ Enumeration.ParameterType.CHARACTER.getparametertype()
										+ " then tgtcp.scharname else null end end end sresultvalue,"
										+ " tgtp.nparametertypecode, tgtp.ntestgrouptestparametercode, tgt.ntestgrouptestcode, tgtp.nresultmandatory"
										+ " from testgrouptest tgt "
										+ " inner join testgrouptestparameter tgtp on tgtp.ntestgrouptestcode = tgt.ntestgrouptestcode"
										+ " and tgtp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and tgtp.nsitecode = " + userInfo.getNmastersitecode()
										+ " left outer join testgrouptestcharparameter tgtcp on tgtcp.ntestgrouptestparametercode = tgtp.ntestgrouptestparametercode"
										+ " and tgtcp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and tgtcp.nsitecode = " + userInfo.getNmastersitecode()
										+ " left outer join testgrouptestnumericparameter tgtnp on tgtnp.ntestgrouptestparametercode = tgtp.ntestgrouptestparametercode"
										+ " and tgtnp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and tgtnp.nsitecode = " + userInfo.getNmastersitecode()
										+ " left outer join testgrouptestpredefparameter tgtpp on tgtpp.ntestgrouptestparametercode = tgtp.ntestgrouptestparametercode"
										+ " and tgtpp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and tgtpp.nsitecode = " + userInfo.getNmastersitecode()
										+ " where tgt.ntestgrouptestcode in (" + sntestgrouptestcode + ") "
										+ " and tgt.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and tgt.nsitecode = " + userInfo.getNmastersitecode();

		final List<TestGroupTestParameter> parameterList = jdbcTemplate.query(sParameterQuery,
				new TestGroupTestParameter());

		List<String> replicateTestList = new ArrayList<String>();
		List<String> replicateTestHistoryList = new ArrayList<String>();
		List<String> replicateTestParameterList = new ArrayList<String>();
		List<Integer> autoCompleteTestCodeList = new ArrayList<Integer>();
		List<Integer> nonAutoCompleteTestCodeList = new ArrayList<Integer>();
		List<Integer> jobAllocationTestList = new ArrayList<Integer>();
		List<Integer> transactionTestCodeList = new ArrayList<Integer>();

		String addedTestCode = "";
		String strTestHistory = "";

		boolean directAddTest = true;
		if (inputMap.containsKey("directAddTest") && (boolean) inputMap.get("directAddTest") == false) {
			directAddTest = false;
		}

		final String transactionsamplecode = listAvailableSample.stream()
				.map(item -> String.valueOf(item.getNtransactionsamplecode())).collect(Collectors.joining(","));

		for (RegistrationSampleHistory objRegistrationSample : listAvailableSample) {

			boolean isAutoComplete = false;
			boolean isAutoJobAllocation = false;

			if (!approvalConfigAutoApprovals.isEmpty()) {

				List<ApprovalConfigAutoapproval> approvalConfigAutoApproval = approvalConfigAutoApprovals.stream()
						.filter(x -> x.getNpreregno() == objRegistrationSample.getNpreregno())
						.collect(Collectors.toList());

				// Based on approval config the test status will change here...
				if (!approvalConfigAutoApproval.isEmpty()) {
					ApprovalConfigAutoapproval objApprovalConfigAutoapproval = approvalConfigAutoApproval.get(0);
					if (objApprovalConfigAutoapproval.getNneedautoapproval() == Enumeration.TransactionStatus.NO
							.gettransactionstatus()
							&& objApprovalConfigAutoapproval.getNneedautocomplete() == Enumeration.TransactionStatus.YES
									.gettransactionstatus()) {
						// Only auto complete
						isAutoComplete = isAutoJobAllocation = true;
						approvalConfigVersionCode = objApprovalConfigAutoapproval.getNapprovalconfigversioncode();

						// commented by L.Subashini as this check is invalid
						// if (isAutoComplete) {
						final List<String> rejectTest = new ArrayList<String>();

						for (int testindex = 0; testindex < listTest.size(); testindex++) {
							final TestGroupTest objTestGroupTestParameter = listTest.get(testindex);

							// Filtering the relevant parameters of the specific test
							final List<TestGroupTestParameter> testParameterList = parameterList.stream()
									.filter(parameter -> parameter.getNtestgrouptestcode() == objTestGroupTestParameter
											.getNtestgrouptestcode())
									.collect(Collectors.toList());

							final List<TestGroupTestParameter> nonMandatoryTestList = testParameterList.stream().filter(
									parameter -> parameter.getNresultmandatory() == Enumeration.TransactionStatus.NO
											.gettransactionstatus() && parameter.getSresultvalue() == null)
									.collect(Collectors.toList());

							if (nonMandatoryTestList.size() == testParameterList.size()) {
								rejectTest.add(String.valueOf(objTestGroupTestParameter.getNtestgrouptestcode()));
							}

						}
						final List<TestGroupTest> testGroupTest = listTest.stream()
								.filter(test -> rejectTest.stream()
										.noneMatch(reject -> Integer.valueOf(reject) == test.getNtestgrouptestcode()))
								.collect(Collectors.toList());

						if (testGroupTest.size() > 0) {
							final String testCodeToComplete = testGroupTest.stream()
									.map(testGroup -> String.valueOf(testGroup.getNtestgrouptestcode()))
									.collect(Collectors.joining(","));

							stestStatus = String
									.valueOf(Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()) + ","
									+ String.valueOf(Enumeration.TransactionStatus.COMPLETED.gettransactionstatus());

							parameterStatus = Enumeration.TransactionStatus.COMPLETED.gettransactionstatus();

							final Map<String, Object> testMap = createQueryCreateTestParameterHistory(
									objRegistrationSample, // nRecievedStatus,
									testCodeToComplete, stestStatus, nTransactionTestCode, nTesthistoryCode,
									nRegistrationParameterCode, replicateTestList, replicateTestHistoryList,
									replicateTestParameterList, userInfo, sntestgrouptestcode, inputMap,
									transactionTestCodeList, autoCompleteTestCodeList, nonAutoCompleteTestCodeList,
									jobAllocationTestList, isAutoComplete, needJobAllocation);

							replicateTestHistoryList = (ArrayList<String>) testMap.get("replicateTestHistoryList");
							replicateTestList = (ArrayList<String>) testMap.get("replicateTestList");
							replicateTestParameterList = (ArrayList<String>) testMap.get("replicateTestParameterList");
							nTransactionTestCode = (int) testMap.get("nTransactionTestCode");
							nTesthistoryCode = (int) testMap.get("nTesthistoryCode");
							nRegistrationParameterCode = (int) testMap.get("nRegistrationParameterCode");
							autoCompleteTestCodeList = (ArrayList<Integer>) testMap.get("autoCompleteTestCodeList");
							nonAutoCompleteTestCodeList = (ArrayList<Integer>) testMap
									.get("nonAutoCompleteTestCodeList");
							jobAllocationTestList = (ArrayList<Integer>) testMap.get("jobAllocationTestList");
							transactionTestCodeList = (ArrayList<Integer>) testMap.get("transactionTestCodeList");

						}
						if (rejectTest.size() > 0) {
							final String rejString = rejectTest.stream().map(String::valueOf)
									.collect(Collectors.joining(","));
							stestStatus = String
									.valueOf(Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()) + ","
									+ String.valueOf(Enumeration.TransactionStatus.REJECTED.gettransactionstatus());

							final Map<String, Object> testMap = createQueryCreateTestParameterHistory(
									objRegistrationSample, // nRecievedStatus,
									rejString, stestStatus, nTransactionTestCode, nTesthistoryCode,
									nRegistrationParameterCode, replicateTestList, replicateTestHistoryList,
									replicateTestParameterList, userInfo, sntestgrouptestcode, inputMap,
									transactionTestCodeList, autoCompleteTestCodeList, nonAutoCompleteTestCodeList,
									jobAllocationTestList, isAutoComplete, needJobAllocation);

							replicateTestHistoryList = (ArrayList<String>) testMap.get("replicateTestHistoryList");
							replicateTestList = (ArrayList<String>) testMap.get("replicateTestList");
							replicateTestParameterList = (ArrayList<String>) testMap.get("replicateTestParameterList");
							nTransactionTestCode = (int) testMap.get("nTransactionTestCode");
							nTesthistoryCode = (int) testMap.get("nTesthistoryCode");
							nRegistrationParameterCode = (int) testMap.get("nRegistrationParameterCode");
							autoCompleteTestCodeList = (ArrayList<Integer>) testMap.get("autoCompleteTestCodeList");
							nonAutoCompleteTestCodeList = (ArrayList<Integer>) testMap
									.get("nonAutoCompleteTestCodeList");
							jobAllocationTestList = (ArrayList<Integer>) testMap.get("jobAllocationTestList");
							transactionTestCodeList = (ArrayList<Integer>) testMap.get("transactionTestCodeList");
						}
						// }

					} else if // (!needJobAllocation ||
					(needJobAllocation && objApprovalConfigAutoapproval
							.getNautoallot() == Enumeration.TransactionStatus.YES.gettransactionstatus())// )
					{
						isAutoJobAllocation = true;
						// ALPD-2039
						// stestStatus =
						// String.valueOf(Enumeration.TransactionStatus.REGISTERED.gettransactionstatus());
						stestStatus = String
								.valueOf(Enumeration.TransactionStatus.RECIEVED_IN_SECTION.gettransactionstatus());
					} else if (!needJobAllocation || (needJobAllocation && objApprovalConfigAutoapproval
							.getNautoallot() == Enumeration.TransactionStatus.NO.gettransactionstatus())) {
						// ALPD-2039
//						stestStatus = String
//								.valueOf(Enumeration.TransactionStatus.RECIEVED_IN_SECTION.gettransactionstatus());
						stestStatus = String.valueOf(Enumeration.TransactionStatus.REGISTERED.gettransactionstatus());

						// nRecievedStatus =
						// Enumeration.TransactionStatus.RECIEVED_IN_SECTION.gettransactionstatus();
					}

				}
			}

			// Preregister sample test status is decided here...
			if (objRegistrationSample.getNtransactionstatus() == Enumeration.TransactionStatus.PREREGISTER
					.gettransactionstatus()
					|| objRegistrationSample.getNtransactionstatus() == Enumeration.TransactionStatus.QUARENTINE
							.gettransactionstatus()) {
				stestStatus = String.valueOf(Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus());
			}
/////
			List<ApprovalConfigRole> approvalConfigRole = (List<ApprovalConfigRole>) inputMap.get("ApprovalConfigRole");

			if (directAddTest) {

				List<RegistrationHistory> registrationHistory = (List<RegistrationHistory>) inputMap
						.get("RegistrationHistory");

				List<RegistrationHistory> checkSample = registrationHistory.stream()
						.filter(x -> x.getNpreregno() == objRegistrationSample.getNpreregno())
						.collect(Collectors.toList());

				/**
				 * Samples in complete & not in final level of approval has to be marked as
				 * "partial" if a new test is added
				 */
				// commented for this item ->ALPD-3038

//				List<RegistrationHistory> checkWithSampleStatus = checkSample.stream()
//						.filter(source -> (source.getNtransactionstatus() == Enumeration.TransactionStatus.COMPLETED
//								.gettransactionstatus())
//								|| approvalConfigRole.stream().anyMatch(
//										check -> source.getNtransactionstatus() == check.getNapprovalstatuscode()
//												&& check.getNlevelno() != 1))
//						.collect(Collectors.toList());

				// ALPD-3038
				List<RegistrationHistory> checkWithSampleStatus = checkSample.stream()
						.filter(source -> (source.getNtransactionstatus() != Enumeration.TransactionStatus.PREREGISTER
								.gettransactionstatus()
								&& source.getNtransactionstatus() != Enumeration.TransactionStatus.REGISTERED
										.gettransactionstatus()
								&& source.getNtransactionstatus() != Enumeration.TransactionStatus.REJECTED
										.gettransactionstatus()
								&& source.getNtransactionstatus() != Enumeration.TransactionStatus.CANCELED
										.gettransactionstatus()
								&& source.getNtransactionstatus() != Enumeration.TransactionStatus.QUARENTINE
										.gettransactionstatus()
								&& source.getNtransactionstatus() != Enumeration.TransactionStatus.PARTIALLY
										.gettransactionstatus())
								|| approvalConfigRole.stream().anyMatch(
										check -> source.getNtransactionstatus() == check.getNapprovalstatuscode()
												&& check.getNlevelno() != 1))
						.collect(Collectors.toList());

				if (!checkWithSampleStatus.isEmpty()) {
					nRegHistoryCode = nRegHistoryCode + 1;
					strTestHistory = strTestHistory.concat(" (" + nRegHistoryCode + ", "
							+ objRegistrationSample.getNpreregno() + ", "
							+ Enumeration.TransactionStatus.PARTIALLY.gettransactionstatus() + "," + " '" + utcDateTime
							+ "', " + userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
							+ userInfo.getNdeputyusercode() + ", " + " " + userInfo.getNdeputyuserrole() + ", N'"
							+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "', " + userInfo.getNtranssitecode()
							+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode()
							+ "),");
					inputMap.put("ntype", 3);
				}


			}

			if (objRegistrationSample.getNtransactionstatus() != Enumeration.TransactionStatus.PREREGISTER
					.gettransactionstatus()
					&& objRegistrationSample.getNtransactionstatus() != Enumeration.TransactionStatus.QUARENTINE
							.gettransactionstatus()) {
				
				//Modified the Insert query by sonia on 8th August 2024 for JIRA ID:4540 
				final String query21 = "insert into registrationsection (nregistrationsectioncode, npreregno, nsectioncode, nsitecode, nstatus)"
						+ " select rank() over(order by tgt.nsectioncode)+" + nregSectionCode
						+ " nregistrationsectioncode, " + objRegistrationSample.getNpreregno() + " npreregno,"
						+ " tgt.nsectioncode, " + userInfo.getNtranssitecode() + " nsitecode ,"
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus"
						+ " from testgrouptest tgt where "
						+ " tgt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
						+ " and tgt.nsitecode =" + userInfo.getNmastersitecode()
						+ " and not exists ("
						+ " select * from registrationsection rsh where rsh.nsectioncode in ("
						+ stringUtilityFunction.fnDynamicListToString(listTest, "getNsectioncode") + ")" 
						+ " and nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
						+ " and npreregno = " + objRegistrationSample.getNpreregno() 
						+ " and nsitecode =" + userInfo.getNtranssitecode()
						+ " and rsh.nsectioncode = tgt.nsectioncode) and tgt.ntestgrouptestcode in ("
						+ sntestgrouptestcode + ") group by tgt.nsectioncode;";
				final int addedSectionCount = jdbcTemplate.update(query21);
				nregSectionCode = nregSectionCode + addedSectionCount;
				
				//Modified the Insert query by sonia on 8th August 2024 for JIRA ID:4540 
				final String query11 = "insert into registrationsectionhistory (nsectionhistorycode, npreregno, nsectioncode, ntransactionstatus, nsitecode, nstatus)"
									+ " select rank() over(order by tgt.nsectioncode)+" + nSectionHistoryCode
									+ " nsectionhistorycode, " + objRegistrationSample.getNpreregno() + " npreregno,"
									+ " tgt.nsectioncode, " + Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()
									+ " ntransactionstatus, " + userInfo.getNtranssitecode() + " nsitecode ,"
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus"
									+ " from testgrouptest tgt where "
									+ " tgt.nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
									+ " and tgt.nsitecode =" + userInfo.getNmastersitecode()
									+ " and not exists ("
									+ " select * from registrationsectionhistory rsh where rsh.nsectioncode in ("
									+ stringUtilityFunction.fnDynamicListToString(listTest, "getNsectioncode") + ") "
									+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
									+ " and nsitecode="	+ userInfo.getNtranssitecode() 
									+ " and npreregno = " + objRegistrationSample.getNpreregno()
									+ " and rsh.nsectioncode = tgt.nsectioncode) and tgt.ntestgrouptestcode in ("
									+ sntestgrouptestcode + ") group by tgt.nsectioncode;";// );
				// jdbcTemplate.execute(query11);
				final int addedSecHistoryCount = jdbcTemplate.update(query11);
				nSectionHistoryCode = nSectionHistoryCode + addedSecHistoryCount;
			}

			// sb.append(
			if (!isAutoComplete) {

				final Map<String, Object> testMap = createQueryCreateTestParameterHistory(objRegistrationSample, // nRecievedStatus,
						sntestgrouptestcode, stestStatus, nTransactionTestCode, nTesthistoryCode,
						nRegistrationParameterCode, replicateTestList, replicateTestHistoryList,
						replicateTestParameterList, userInfo, sntestgrouptestcode, inputMap, transactionTestCodeList,
						autoCompleteTestCodeList, nonAutoCompleteTestCodeList, jobAllocationTestList, isAutoComplete,
						needJobAllocation);

				replicateTestHistoryList = (ArrayList<String>) testMap.get("replicateTestHistoryList");
				replicateTestList = (ArrayList<String>) testMap.get("replicateTestList");
				replicateTestParameterList = (ArrayList<String>) testMap.get("replicateTestParameterList");
				nTransactionTestCode = (int) testMap.get("nTransactionTestCode");
				nTesthistoryCode = (int) testMap.get("nTesthistoryCode");
				nRegistrationParameterCode = (int) testMap.get("nRegistrationParameterCode");
				autoCompleteTestCodeList = (ArrayList<Integer>) testMap.get("autoCompleteTestCodeList");
				nonAutoCompleteTestCodeList = (ArrayList<Integer>) testMap.get("nonAutoCompleteTestCodeList");
				jobAllocationTestList = (ArrayList<Integer>) testMap.get("jobAllocationTestList");
				transactionTestCodeList = (ArrayList<Integer>) testMap.get("transactionTestCodeList");

				//Commented below by L.Subashini-28/4/2025 as this table is not used now
//				if (objRegistrationSample.getNtransactionstatus() != Enumeration.TransactionStatus.PREREGISTER
//						.gettransactionstatus()
//						&& objRegistrationSample.getNtransactionstatus() != Enumeration.TransactionStatus.QUARENTINE
//								.gettransactionstatus()) {
//					// sb.append(
//					final String query7 = "insert into llinter(nllintercode, nscreentypecode, npreregno, nsampletypecode, nsamplecode, nregtypecode, nregsubtypecode, nproductcode, "
//							+ "nallottedspeccode, ntransactiontestcode, ntestgrouptestcode, nretestno, ntestrepeatcount, ntransactionresultcode, ntestgrouptestparametercode,"
//							+ " nparametertypecode, nrounddingdigits, nunitcode, sresult, nresult, nlltestcodde, nllparametercode, dreceiveddate, dregdate, sllarno,"
//							+ " sllparamkey, sllinterstatus, sllparamtype, sllcomponentname, slltestname, sllparametername, sproductname, sunitname, smanufname, smanuflotno,"
//							+ " smethodname, srefproductgroupname, susername, sclientname, nsitecode, nstatus)"
//							+ " (select " + nllintercode
//							+ "+RANK() over(order by rp.ntransactionresultcode) nllintercode, r.nsampletypecode,r.npreregno, "
//							+ " rs.ncomponentcode nsampletypecode, rs.ntransactionsamplecode nsamplecode, r.nregtypecode, r.nregsubtypecode, r.nproductcode, r.nallottedspeccode,"
//							+ " rt.ntransactiontestcode, rt.ntestgrouptestcode, rt.ntestretestno nretestno,  rt.ntestrepeatno ntestrepeatcount, rp.ntransactionresultcode,"
//							+ " rp.ntestgrouptestparametercode, rp.nparametertypecode, rp.nroundingdigits nrounddingdigits, rp.nunitcode, NULL sresult, NULL nresult,"
//							+ " tm.ntestcode nlltestcodde, tp.ntestparametercode nllparametercode, rg.dreceiveddate, '"
//							+ utcDateTime + "' dregdate, CAST(ra.sarno as varchar(20))+'@'+ "
//							+ " CAST(rt.ntestrepeatno as varchar(20))+'@'+  CAST(rt.ntestretestno as varchar(20)) as sllarno, NULL sllparamkey, NULL sllinterstatus,"
//							+ " case when rp.nparametertypecode = "
//							+ Enumeration.ParameterType.NUMERIC.getparametertype()
//							+ " then 'V' else 'D' end as sllparamtype,"
//							+ " c.scomponentname sllcomponentname, tm.stestname slltestname, tp.sparametername sllparametername, p.sproductname, u.sunitname,"
//							+ " mf.smanufname, rg.smanuflotno smanflotno, md.smethodname, ep.seprotocolname srefproductgroupname, us.sfirstname+' '+us.slastname as susername,"
//							+ " ct.sclientname, r.nsitecode, "
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
//							+ " from registrationtesthistory rth,registration r, registrationgeneral rg, registrationarno ra, "
//							+ " registrationsample rs, registrationtest rt, registrationparameter rp, "
//							+ " testgrouptest tgt, testgrouptestparameter tgtp, testmaster tm, "
//							+ " testparameter tp, product p, users us, component c, method md, "
//							+ " unit u, instrumentcategory ic, eprotocol ep, client ct, manufacturer mf where "
//							+ " rth.npreregno = r.npreregno and rth.ntesthistorycode = any ("
//							+ " select max(ntesthistorycode) from registrationtesthistory "
//							+ " where ntransactiontestcode = any(select " + nTransactionTestCode
//							+ "+RANK() over(order by ntestgrouptestcode) from testgrouptest tgt"
//							+ " where ntestgrouptestcode in (" + sntestgrouptestcode + ") and nstatus = "
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") and npreregno = "
//							+ objRegistrationSample.getNpreregno()
//							+ " group by npreregno, ntransactionsamplecode, ntransactiontestcode"
//							+ " ) and rs.ntransactionsamplecode = rth.ntransactionsamplecode and rt.ntransactiontestcode = rth.ntransactiontestcode"
//							+ " and r.npreregno = ra.npreregno"
//							+ " and r.npreregno = rg.npreregno and r.npreregno = rs.npreregno and r.npreregno = rt.npreregno and rs.ntransactionsamplecode = rt.ntransactionsamplecode"
//							+ " and r.npreregno = rt.npreregno and rt.ntransactiontestcode = rp.ntransactiontestcode"
//							+ " and r.npreregno = rp.npreregno"
//							+ " and tgt.ntestgrouptestcode = rt.ntestgrouptestcode and tgt.ntestcode = rt.ntestcode"
//							+ " and tgtp.ntestgrouptestparametercode = rp.ntestgrouptestparametercode and tm.ntestcode = tgt.ntestcode "
//							+ " and tm.ntestcode = tp.ntestcode and tp.ntestparametercode = tgtp.ntestparametercode"
//							+ " and p.nproductcode = r.nproductcode and us.nusercode = rth.nusercode and rth.ntransactionstatus = "
//							+ Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()
//							+ " and c.ncomponentcode = rs.ncomponentcode and md.nmethodcode = tgt.nmethodcode and md.nmethodcode = rt.nmethodcode "
//							+ " and u.nunitcode = rp.nunitcode and ic.ninstrumentcatcode = tgt.ninstrumentcatcode and ic.ninstrumentcatcode = tgt.ninstrumentcatcode "
//							// + " and ic.ninterfacetypecode = " +
//							// Enumeration.InterFaceType.LOGILAB.getInterFaceType()
//							+ " and ep.neprotocolcode = rg.neprotocolcode"
//							+ " and ct.nclientcode = rg.nclientcode and mf.nmanufcode = rg.nmanufcode"
//							+ " and r.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//							+ " and rg.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//							+ " and rs.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//							+ " and rt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//							+ " and rp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//							+ " and tgt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//							+ " and tgtp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//							+ " and tm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//							+ " and tp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//							+ " and p.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//							+ " and us.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//							+ " and c.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//							+ " and md.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//							+ " and u.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//							+ " and ic.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//							+ " and ep.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//							+ " and ct.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//							+ " and mf.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//							+ " and r.npreregno = " + objRegistrationSample.getNpreregno() + " and ra.nstatus = "
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.nstatus = "
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//							+ "  and rt.ninstrumentcatcode > 0);";// );
//					// jdbcTemplate.execute(query7);
//
//					nllintercode = nllintercode + llinterParameterListCount;
//				}
			}
		}

		final String query1 = "insert into registrationsamplehistory(nsamplehistorycode,ntransactionsamplecode,"
								+ "npreregno,ntransactionstatus,dtransactiondate,nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,"
								+ "scomments,nsitecode,nstatus)" + "select " + nregistrationsamplehistory
								+ "+rank()over(order by ntransactionsamplecode,npreregno) nsamplehistorycode, ntransactionsamplecode,npreregno,36,'"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' dtransactiondate," + userInfo.getNusercode() + " nusercode," + ""
								+ userInfo.getNuserrole() + " nuserrolecode," + userInfo.getNdeputyusercode() + " ndeputyusercode,"
								+ userInfo.getNdeputyuserrole() + " ndeputyuserrolecode," + "'' scomments,"
								+ userInfo.getNtranssitecode() + " nsitecode,"
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
								+ " from registrationsamplehistory where "
								+ " nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and nsitecode="+userInfo.getNtranssitecode()
								+ " and nsamplehistorycode in( "
								+ " select max(nsamplehistorycode) from registrationsamplehistory "
								+ " where nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and nsitecode="+userInfo.getNtranssitecode()
								+ " group by ntransactionsamplecode)"
								+ " and ntransactionstatus not in(" + Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus()
								+ "," + Enumeration.TransactionStatus.REGISTERED.gettransactionstatus() + ","
								+ Enumeration.TransactionStatus.PARTIALLY.gettransactionstatus() + " ) and ntransactionsamplecode in("
								+ transactionsamplecode + ")";

		jdbcTemplate.execute(query1);

		int orderType = -1;
		int seqordertest = -1;
		String externalOrderTestQuery = "";
		seqordertest = (int) inputMap.get("externalordertest");
		
		final String sntransactionsamplecode = listSample.stream().map(x -> x).collect(Collectors.joining(","));
		
		final String stringQuery = "select r.* from registration r,registrationhistory rh  "
				                + " where r.npreregno in ("	+ inputMap.get("npreregno") + ") "
				                + " and r.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and r.nsitecode="+userInfo.getNtranssitecode()
								+ " and rh.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and rh.nsitecode="+userInfo.getNtranssitecode()
				                + " and rh.nreghistorycode in  (select max(nreghistorycode) from  registrationhistory  group by npreregno )"
				                + " and rh.ntransactionstatus not in  (" + Enumeration.TransactionStatus.REJECTED.gettransactionstatus()
								+ "," +  Enumeration.TransactionStatus.CANCELED.gettransactionstatus()	+ ")   and r.npreregno=rh.npreregno  ";
		final List<Registration> lstReg = jdbcTemplate.query(stringQuery, new Registration());

		if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()) {
			orderType = (int) inputMap.get("ordertypecode");
			for (int i = 0; i < lstReg.size(); i++) {
				final String sample = "select rs.jsonuidata->>'sampleorderid' as sexternalsampleid ,"
									+ " rs.* from registrationsample rs,registrationsamplehistory  rsh "
									+ " where rs.npreregno in ("+ lstReg.get(i).getNpreregno() + ")" 
									+ " and rs.ntransactionsamplecode  in ("+ sntransactionsamplecode+ ") "
									+ " and rs.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
									+ " and rs.nsitecode="+userInfo.getNtranssitecode()
									+ " and rsh.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
									+ " and rsh.nsitecode="+userInfo.getNtranssitecode()		
									+ " and rsh.nsamplehistorycode in "
									+ " (select max(nsamplehistorycode) from  registrationsamplehistory  "
									+ " where nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
									+ " and nsitecode="+userInfo.getNtranssitecode()
									+ " group by ntransactionsamplecode ) and "
									+ " rsh.ntransactionstatus not in ("
									+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","
									+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ") "
									+ " and  rsh.ntransactionsamplecode=rs.ntransactionsamplecode ";
				final List<RegistrationSample> lstReg23 = jdbcTemplate.query(sample, new RegistrationSample());
				
				for (int k = 0; k < lstReg23.size(); k++) {
					String value = "";
					if (orderType == Enumeration.OrderType.INTERNAL.getOrderType() || orderType == Enumeration.OrderType.NA.getOrderType()) {
						value = " and eo.nexternalordercode=" + lstReg.get(i).getJsonuidata().get("OrderIdData") + " "
								+ "and eos.sexternalsampleid='" + stringUtilityFunction.replaceQuote(lstReg23.get(k).getSexternalsampleid())
								+ "'";
					} else {
						value = " and eo.sexternalorderid='" + lstReg.get(i).getJsonuidata().get("OrderIdData") + "'";
					}
					final String sQuery = " select eos.nexternalordersamplecode,eos.nexternalordercode from "
										+ " externalorder eo,externalordersample eos "
									    + " where eo.nexternalordercode=eos.nexternalordercode " 
										+ " and eo.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									    + " and eos.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									    + " "
										+ value;
					
					// ExternalOrderSample lstxtrenal = (ExternalOrderSample)
					// jdbcQueryForObject(sQuery, ExternalOrderSample.class);
					final List<ExternalOrderSample> lstxtrenal1 = jdbcTemplate.query(sQuery, new ExternalOrderSample());
					
					ExternalOrderSample lstxtrenal = null;
					if (lstxtrenal1.size() > 0) {
						lstxtrenal = lstxtrenal1.get(0);
					}
					if (lstxtrenal != null) {
						if (lstReg.get(i).getJsondata().containsKey("Order Type")) {
							if (orderType == 1 || orderType == -1) {
								int externalordercode = lstxtrenal.getNexternalordercode();
								int externalorderSamplecode = lstxtrenal.getNexternalordersamplecode();
								for (TestGroupTest test : listTest) {
									seqordertest++;
									externalOrderTestQuery += "(" + seqordertest + "," + externalorderSamplecode + ","
											+ externalordercode + "," + test.getNtestpackagecode() + ","
											+ test.getNcontainertypecode() + " ," + test.getNtestcode() + ",'"
											+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + "," + userInfo.getNtranssitecode()
											+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
											+ userInfo.getNtranssitecode() + "),";
								}

							}
						}
					}
				}

			}
			;

		}
		if (replicateTestList.size() > 0) {
			final String strRegistrationTest = "insert into registrationtest (ntransactiontestcode,ntransactionsamplecode,npreregno,"
					+ "ntestgrouptestcode,ninstrumentcatcode,nchecklistversioncode,ntestrepeatno,ntestretestno,jsondata,nsitecode,dmodifieddate, "
					+ " nstatus,ntestcode,nsectioncode,nmethodcode) values ";
			jdbcTemplate.execute(strRegistrationTest + String.join(",", replicateTestList));

		}
		if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()) {
			if (lstReg.get(0).getJsonuidata().containsKey("Order Type")) {
				orderType = (int) inputMap.get("ordertypecode");
				if (orderType == 1 || orderType == -1) {
					externalOrderTestQuery = "INSERT INTO externalordertest (nexternalordertestcode,nexternalordersamplecode,nexternalordercode,ntestpackagecode,ncontainertypecode,ntestcode,dmodifieddate,nsitecode,nstatus, nparentsitecode) values "
							+ externalOrderTestQuery.substring(0, externalOrderTestQuery.length() - 1) + ";";

					jdbcTemplate.execute(externalOrderTestQuery);
				}

			}
		}
		if (replicateTestHistoryList.size() > 0) {
			final String strRegistrationTestHistory = "insert into registrationtesthistory (ntesthistorycode, ntransactiontestcode, "
													+ "ntransactionsamplecode, npreregno, nformcode, ntransactionstatus,"
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

		final String createdTestCode = String.join(",",
				transactionTestCodeList.stream().map(String::valueOf).collect(Collectors.toList()));

		if (autoCompleteTestCodeList.size() > 0 || nonAutoCompleteTestCodeList.size() > 0) {
			// Sample Status not in preregister or quarantine

			if (autoCompleteTestCodeList.size() > 0) {

				addedTestCode = String.join(",",
						autoCompleteTestCodeList.stream().map(String::valueOf).collect(Collectors.toList()));
				// with grade and result in sql query
				// sb.append(
				String query5 = "insert into resultparameter( ntransactionresultcode, npreregno, ntransactiontestcode, ntestgrouptestparametercode, ntestparametercode,"
							+ " nparametertypecode, nroundingdigits, nresultmandatory, nreportmandatory, ntestgrouptestformulacode, nunitcode, ngradecode, ntransactionstatus,"
							+ " sfinal, sresult, nenforcestatus, nenforceresult,ncalculatedresult,smina, sminb, smaxb, smaxa, sminlod, smaxlod, sminloq, smaxloq, sdisregard, sresultvalue,"
							+ " dentereddate, nenteredby, nenteredrole, ndeputyenteredby, ndeputyenteredrole, nfilesize, ssystemfilename, nlinkcode, nattachmenttypecode, nsitecode,nstatus)"
							+ " select ntransactionresultcode, rp.npreregno, rp.ntransactiontestcode, rp.ntestgrouptestparametercode,"
							+ " rp.ntestparametercode, rp.nparametertypecode, rp.nroundingdigits, rp.nresultmandatory, rp.nreportmandatory, rp.ntestgrouptestformulacode,"
							+ " rp.nunitcode, case when rp.nparametertypecode = "
							+ Enumeration.ParameterType.NUMERIC.getparametertype() + " then ("
							+ " case when tgtnp.sminb is null and tgtnp.smina is null and tgtnp.smaxa is null and tgtnp.smaxb is not null "
							+ " then  " + " case when cast(tgtnp.sresultvalue as float)<=tgtnp.smaxb then "
							+ Enumeration.Grade.PASS.getGrade() + " else " + Enumeration.Grade.OOS.getGrade() + " end "
							+ " when tgtnp.sminb is null and tgtnp.smina is null and tgtnp.smaxa is not null and tgtnp.smaxb is null then "
							+ " case when cast(tgtnp.sresultvalue as float)<=tgtnp.smaxa then "
							+ Enumeration.Grade.PASS.getGrade() + " else " + Enumeration.Grade.OOS.getGrade() + " end "
							+ " when tgtnp.sminb is null and tgtnp.smina is null and tgtnp.smaxa is not null and tgtnp.smaxb is not null then "
							+ " case when cast(tgtnp.sresultvalue as float) between tgtnp.smaxa and tgtnp.smaxb then "
							+ Enumeration.Grade.OOS.getGrade() + " when cast(tgtnp.sresultvalue as float) > tgtnp.smaxb "
							+ " then " + Enumeration.Grade.OOT.getGrade() + " else " + Enumeration.Grade.PASS.getGrade()
							+ " end "

							+ " when tgtnp.sminb is null and tgtnp.smina is not null and tgtnp.smaxa is null and tgtnp.smaxb is null "
							+ " then case when cast(tgtnp.sresultvalue as float)>=tgtnp.smina then "
							+ Enumeration.Grade.PASS.getGrade() + " else " + Enumeration.Grade.OOS.getGrade() + " end "
	
							+ " when tgtnp.sminb is null and tgtnp.smina is not null and tgtnp.smaxa is null and tgtnp.smaxb is not null then "
							+ " case when cast(tgtnp.sresultvalue as float) between tgtnp.smina and tgtnp.smaxb then "
							+ Enumeration.Grade.PASS.getGrade() + " else " + Enumeration.Grade.OOS.getGrade() + " end "
	
							+ " when tgtnp.sminb is null and tgtnp.smina is not null and tgtnp.smaxa is not null and tgtnp.smaxb is null "
							+ " then case when cast(tgtnp.sresultvalue as float) between tgtnp.smina and tgtnp.smaxa then "
							+ Enumeration.Grade.PASS.getGrade() + " else " + Enumeration.Grade.OOS.getGrade() + " end "
	
							+ " when tgtnp.sminb is null and tgtnp.smina is not null and tgtnp.smaxa is not null and tgtnp.smaxb is not null then "
							+ " case when cast(tgtnp.sresultvalue as float) between tgtnp.smina and tgtnp.smaxa then "
							+ Enumeration.Grade.PASS.getGrade()
							+ " when cast(tgtnp.sresultvalue as float) between tgtnp.smaxa and tgtnp.smaxb then "
							+ Enumeration.Grade.OOS.getGrade() + " else " + Enumeration.Grade.OOT.getGrade() + " end "
	
							+ " when tgtnp.sminb is not null and tgtnp.smina is null and tgtnp.smaxa is null and tgtnp.smaxb is null then "
							+ " case when cast(tgtnp.sresultvalue as float) >= tgtnp.sminb then "
							+ Enumeration.Grade.PASS.getGrade() + " else " + Enumeration.Grade.OOS.getGrade() + " end "
	
							+ " when tgtnp.sminb is not null and tgtnp.smina is null and tgtnp.smaxa is null and tgtnp.smaxb is not null then "
							+ " case when cast(cast(tgtnp.sresultvalue as float) as float) between tgtnp.sminb and tgtnp.smaxb then "
							+ Enumeration.Grade.PASS.getGrade() + " else " + Enumeration.Grade.OOS.getGrade() + " end "
	
							+ " when tgtnp.sminb is not null and tgtnp.smina is null and tgtnp.smaxa is not null and tgtnp.smaxb is null then "
							+ " case when cast(tgtnp.sresultvalue as float) between tgtnp.sminb and tgtnp.smaxa then "
							+ Enumeration.Grade.PASS.getGrade() + " else " + Enumeration.Grade.OOS.getGrade() + " end "
	
							+ " when tgtnp.sminb is not null and tgtnp.smina is null and tgtnp.smaxa is not null and tgtnp.smaxb is not null then "
							+ " case when cast(tgtnp.sresultvalue as float) between tgtnp.sminb and tgtnp.smaxa then "
							+ Enumeration.Grade.PASS.getGrade() + " else " + Enumeration.Grade.OOS.getGrade() + " end "
	
							+ " when tgtnp.sminb is not null and tgtnp.smina is not null and tgtnp.smaxa is null and tgtnp.smaxb is null then "
							+ " case when cast(tgtnp.sresultvalue as float)	between tgtnp.sminb and tgtnp.smina then "
							+ Enumeration.Grade.OOS.getGrade() + " "
							+ " when cast(tgtnp.sresultvalue as float) < tgtnp.sminb then "
							+ Enumeration.Grade.OOS.getGrade() + " when cast(tgtnp.sresultvalue as float) >= tgtnp.smina  "
							+ " then " + Enumeration.Grade.PASS.getGrade() + " end  "
	
							+ " when tgtnp.sminb is not null and tgtnp.smina is not null and tgtnp.smaxa is null and tgtnp.smaxb is not null "
							+ " then case when cast(tgtnp.sresultvalue as float) between tgtnp.smina and tgtnp.smaxb then "
							+ Enumeration.Grade.PASS.getGrade()
							+ " when cast(tgtnp.sresultvalue as float) between tgtnp.sminb and tgtnp.smina " + " then "
							+ Enumeration.Grade.OOS.getGrade() + " when cast(tgtnp.sresultvalue as float) > tgtnp.smaxb"
							+ " or cast(tgtnp.sresultvalue as float) < tgtnp.sminb then " + Enumeration.Grade.OOT.getGrade()
							+ " end "
	
							+ " when tgtnp.sminb is not null and tgtnp.smina is not null and tgtnp.smaxa is not null and tgtnp.smaxb is null  then "
							+ " case when cast(tgtnp.sresultvalue as float) between tgtnp.smina and tgtnp.smaxa then "
							+ Enumeration.Grade.PASS.getGrade()
							+ " when cast(tgtnp.sresultvalue as float) between tgtnp.sminb and tgtnp.smina then "
							+ Enumeration.Grade.OOS.getGrade() + " else " + Enumeration.Grade.OOT.getGrade() + " end"
	
							+ " when tgtnp.sminb is not null and tgtnp.smina is not null and tgtnp.smaxa is not null and tgtnp.smaxb is not null then "
							+ " case when cast(tgtnp.sresultvalue as float) between tgtnp.smina and tgtnp.smaxa then "
							+ Enumeration.Grade.PASS.getGrade() + "  "
	
							+ " when cast(tgtnp.sresultvalue as float) between tgtnp.sminb and tgtnp.smaxb then "
							+ Enumeration.Grade.OOT.getGrade() + " "
							+ " when (cast(tgtnp.sresultvalue as float) < tgtnp.sminb or tgtnp.smaxb < cast(tgtnp.sresultvalue as float))"
							+ " and (tgtnp.sminb!=0 and tgtnp.smaxb!=0)then " + Enumeration.Grade.OOS.getGrade() + " else "
							+ Enumeration.Grade.PASS.getGrade() + " end else " + Enumeration.Grade.NA.getGrade() + " end)"
	
							+ " when rp.nparametertypecode = " + Enumeration.ParameterType.PREDEFINED.getparametertype()
							+ " then tgtpp.ngradecode" + " when rp.nparametertypecode = "
							+ Enumeration.ParameterType.CHARACTER.getparametertype() + " then "
							+ Enumeration.Grade.FIO.getGrade() + " " + " when rp.nparametertypecode = "
							+ Enumeration.ParameterType.ATTACHEMENT.getparametertype() + " then "
							+ Enumeration.Grade.PASS.getGrade() + " " + " else "
							+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " end as ngradecode,"

							+ " case when (case  when rp.nparametertypecode = "
							+ Enumeration.ParameterType.NUMERIC.getparametertype()
							+ " then tgtnp.sresultvalue when rp.nparametertypecode = "
							+ Enumeration.ParameterType.PREDEFINED.getparametertype() + " then tgtpp.spredefinedname"
							+ " when rp.nparametertypecode = " + Enumeration.ParameterType.CHARACTER.getparametertype()
							+ " then tgtcp.scharname" + " when rp.nparametertypecode = "
							+ Enumeration.ParameterType.ATTACHEMENT.getparametertype() + " then NULL else NULL end) is null"
							+ " then " + Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + " else "
							+ parameterStatus + " end as ntransactionstatus, "
	
							+ " case " + " when rp.nparametertypecode = "
							+ Enumeration.ParameterType.NUMERIC.getparametertype()
							+ " then (case when CHARINDEX('.', tgtnp.sresultvalue, 1) < =0 then "
							+ " tgtnp.sresultvalue+'.'+dbo.RoundDigits(rp.nroundingdigits) else tgtnp.sresultvalue end)"
							+ " when rp.nparametertypecode = " + Enumeration.ParameterType.PREDEFINED.getparametertype()
							+ " then tgtpp.spredefinedname" + " when rp.nparametertypecode = "
							+ Enumeration.ParameterType.CHARACTER.getparametertype() + " then tgtcp.scharname"
							+ " when rp.nparametertypecode = " + Enumeration.ParameterType.ATTACHEMENT.getparametertype()
							+ " then NULL" + " else NULL end as sfinal, "
	
							+ " case " + " when rp.nparametertypecode = "
							+ Enumeration.ParameterType.NUMERIC.getparametertype() + " then tgtnp.sresultvalue"
							+ " when rp.nparametertypecode = " + Enumeration.ParameterType.PREDEFINED.getparametertype()
							+ " then tgtpp.spredefinedname" + " when rp.nparametertypecode = "
							+ Enumeration.ParameterType.CHARACTER.getparametertype() + " then tgtcp.scharname"
							+ " when rp.nparametertypecode = " + Enumeration.ParameterType.ATTACHEMENT.getparametertype()
							+ " then NULL" + " else NULL end as sresult, "

							+ " " + Enumeration.TransactionStatus.NO.gettransactionstatus() + " as nenforcestatus, "
							+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " as nenforceresult, "
							+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " as ncalcultedresult, "
							+ " rp.smina, rp.sminb, rp.smaxb, rp.smaxa, rp.sminlod, rp.smaxlod, rp.sminloq, rp.smaxloq, rp.sdisregard, rp.sresultvalue, "
							+ " N'" + utcDateTime + "' dentereddate, "
							+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " nenteredby, " + " ("
							+ "		select nuserrolecode  from treetemplatetransactionrole "
							+ "		where "
							+ "			nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ "			and nsitecode= " +userInfo.getNmastersitecode()							
							+ " 		ntemptransrolecode in (select max(ttr.ntemptransrolecode) "
							+ "			from treetemplatetransactionrole ttr,approvalconfig ac,approvalconfigversion acv "
							+ "			where ac.napprovalconfigcode=ttr.napprovalconfigcode "
							+ "			and ac.napprovalconfigcode = acv.napprovalconfigcode "
							+ "			and ttr.ntreeversiontempcode = acv.ntreeversiontempcode "
							+ "			and ttr.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ "			and acv.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ "			and ac.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ "			and ttr.nsitecode= " +userInfo.getNmastersitecode()
							+ "			and acv.nsitecode= " + userInfo.getNmastersitecode()
							+ "			and ac.nsitecode = " + userInfo.getNmastersitecode()						
							+ "			and ac.nregtypecode= " + nregtypecode + " and ac.nregsubtypecode= "
							+ nregsubtypecode + " and acv.napproveconfversioncode =" + approvalConfigVersionCode
							+ " 		group by ttr.ntreeversiontempcode" + ")) as nenteredrole, "
							+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " ndeputyenteredby, "
							+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " ndeputyenteredrole, " + " "
							+ Enumeration.TransactionStatus.ALL.gettransactionstatus()
							+ " nfilesize, NULL ssystemfilename, " + " "
							+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " nlinkcode, " + " "
							+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " nattachmenttypecode," + " "
							+ userInfo.getNtranssitecode() + " nsitecode,"
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus"
							+ " from registrationparameter rp "
							+ " left outer join testgrouptestcharparameter tgtcp on rp.ntestgrouptestparametercode = tgtcp.ntestgrouptestparametercode"
							+ " and tgtcp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and tgtcp.nsitecode="+ userInfo.getNmastersitecode()
							+ " left outer join testgrouptestnumericparameter tgtnp on rp.ntestgrouptestparametercode = tgtnp.ntestgrouptestparametercode"
							+ " and tgtnp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and tgtnp.nsitecode="+ userInfo.getNmastersitecode()
							+ " left outer join testgrouptestpredefparameter tgtpp on rp.ntestgrouptestparametercode = tgtpp.ntestgrouptestparametercode"
							+ " and tgtpp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and tgtpp.nsitecode="+ userInfo.getNmastersitecode()
							+ " and tgtpp.ndefaultstatus = " + Enumeration.TransactionStatus.YES.gettransactionstatus()
							+ " and rp.ntransactiontestcode in (" + addedTestCode + ") "
							+ " and rp.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
							+ " and rp.nsitecode="+ userInfo.getNtranssitecode();// + ";");

				jdbcTemplate.execute(query5);
			} else {
				// sb.append(
				addedTestCode = String.join(",",
						nonAutoCompleteTestCodeList.stream().map(String::valueOf).collect(Collectors.toList()));

				final String query6 = "insert into resultparameter( ntransactionresultcode, npreregno, ntransactiontestcode, ntestgrouptestparametercode, ntestparametercode,"
									+ " nparametertypecode, nresultmandatory, nreportmandatory, ntestgrouptestformulacode, nunitcode, ngradecode, ntransactionstatus,"
									+ "  nenforcestatus, nenforceresult,ncalculatedresult,"
									+ "  nenteredby, nenteredrole, ndeputyenteredby, ndeputyenteredrole, nlinkcode, nattachmenttypecode,jsondata, nsitecode, nstatus)"
									+ " select ntransactionresultcode, rp.npreregno, rp.ntransactiontestcode, rp.ntestgrouptestparametercode, rp.ntestparametercode,"
									+ " rp.nparametertypecode, rp.nresultmandatory, rp.nreportmandatory, rp.ntestgrouptestformulacode, "
									+ " rp.nunitcode, " + Enumeration.TransactionStatus.NA.gettransactionstatus()
									+ " as ngradecode, " + parameterStatus + " ntransactionstatus,"
									+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " as nenforcestatus," + ""
									+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " as nenforceresult, "
									+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " as ncalcultedresult, " + " "
									+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " nenteredby, "
									+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " nenteredrole," + " "
									+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " ndeputyenteredby, "
									+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " ndeputyenteredrole," + " "
									+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " nlinkcode, " + " "
									+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " nattachmenttypecode, "
									+ "rp.jsondata," + userInfo.getNtranssitecode() + " nsitecode,"
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus"
									+ " from registrationparameter rp where rp.ntransactiontestcode in (" + addedTestCode
									+ ") and rp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and rp.nsitecode=" + userInfo.getNtranssitecode();
				jdbcTemplate.execute(query6);
			}

			final String query10 = "insert into resultparametercomments (ntransactionresultcode, ntransactiontestcode, npreregno, "
								+ "ntestgrouptestparametercode, ntestparametercode,jsondata, nsitecode, nstatus)"
								+ " select  rp.ntransactionresultcode,"
								+ " rp.ntransactiontestcode, rp.npreregno, rp.ntestgrouptestparametercode, rp.ntestparametercode,rp.jsondata, "
								+ userInfo.getNtranssitecode() + " nsitecode,"
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus"
								+ " from resultparameter rp where rp.ntransactiontestcode in (" + createdTestCode
								+ ") and rp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and rp.nsitecode=" + userInfo.getNtranssitecode();
			jdbcTemplate.execute(query10);
			 String sScheduleSkip= transactionDAOSupport.scheduleSkip();
			 if (sScheduleSkip.equals(String.valueOf(Enumeration.TransactionStatus.YES.gettransactionstatus()))&& !nneedmyjob
					 && needJobAllocation) 
			{
				 addedTestCode = String.join(",",
						 transactionTestCodeList.stream().map(String::valueOf).collect(Collectors.toList()));

					approvalConfigVersionCode = approvalConfigAutoApprovals.get(0).getNapprovalconfigversioncode();
			}
			else if (jobAllocationTestList.size() > 0) {
				 addedTestCode = String.join(",",
							jobAllocationTestList.stream().map(String::valueOf).collect(Collectors.toList()));

					approvalConfigVersionCode = approvalConfigAutoApprovals.get(0).getNapprovalconfigversioncode();
			}else {
				addedTestCode = "-1";

				approvalConfigVersionCode = -1;
			}			
			 
					
			final String query12 = "insert into joballocation (njoballocationcode, npreregno, ntransactionsamplecode, ntransactiontestcode, nsectioncode, nuserrolecode,"
									+ " nusercode, nuserperiodcode, ninstrumentcategorycode, ninstrumentcode,ninstrumentnamecode, ninstrumentperiodcode, ntechniquecode, ntimezonecode, ntestrescheduleno, "
									+ " jsondata, jsonuidata, nsitecode, nstatus) "
									+ " select rank() over(order by rt.ntransactiontestcode)+" + nJoballocationCode
									+ " njoballocationcode, rt.npreregno, rt.ntransactionsamplecode,"
									+ " rt.ntransactiontestcode, rt.nsectioncode, " + "	case when ("
									+ "	select nuserrolecode  from treetemplatetransactionrole "
									+ " where "
									+ "	nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and nsitecode=" + userInfo.getNmastersitecode()									
									+ " and ntemptransrolecode in ("
									+ "select max(ttr.ntemptransrolecode) "
									+ "	from treetemplatetransactionrole ttr,approvalconfig ac,approvalconfigversion acv "
									+ "	where ac.napprovalconfigcode=ttr.napprovalconfigcode "
									+ "	and ac.napprovalconfigcode = acv.napprovalconfigcode "
									+ "	and ttr.ntreeversiontempcode = acv.ntreeversiontempcode " 
									+ "	and ttr.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
									+ "	and acv.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
									+ "	and ac.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and ttr.nsitecode=" + userInfo.getNmastersitecode()
									+ " and acv.nsitecode=" + userInfo.getNmastersitecode()
									+ " and ac.nsitecode=" + userInfo.getNmastersitecode()									
									+ "  and ac.nregtypecode= "+ nregtypecode 
									+ " and ac.nregsubtypecode= "+ nregsubtypecode
									+ " and acv.napproveconfversioncode =" + approvalConfigVersionCode
									+ " group by ttr.ntreeversiontempcode)" + "	) isnull then -1 else ("
									+ "	select nuserrolecode  from treetemplatetransactionrole where ntemptransrolecode" + " in ("
									+ " select max(ttr.ntemptransrolecode) "
									+ "	from treetemplatetransactionrole ttr,approvalconfig ac,approvalconfigversion acv "
									+ "	where ac.napprovalconfigcode=ttr.napprovalconfigcode "
									+ "	and ac.napprovalconfigcode = acv.napprovalconfigcode "
									+ "	and ttr.ntreeversiontempcode = acv.ntreeversiontempcode "
									+ "	and ttr.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
									+ "	and acv.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
									+ "	and ac.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
									+ " and ttr.nsitecode=" + userInfo.getNmastersitecode()
									+ " and acv.nsitecode=" + userInfo.getNmastersitecode()
									+ " and ac.nsitecode=" + userInfo.getNmastersitecode()	
									+ "	and ac.nregtypecode= "+ nregtypecode 
									+ " and ac.nregsubtypecode= " + nregsubtypecode
									+ " and acv.napproveconfversioncode =" + approvalConfigVersionCode
									+ " group by ttr.ntreeversiontempcode))" + " end ,"
									+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " nusercode, "
									+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " nuserperiodcode,"
									+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " ninstrumentcategorycode,"
									+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " ninstrumentcode," + " "
									+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " ninstrumentnamecode," + " "
									+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " ninstrumentperiodcode, "
									+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " ntechniquecode, "
									+ userInfo.getNtimezonecode() + " ntimezonecode," + " "
									+ Enumeration.TransactionStatus.ALL.gettransactionstatus() + " ntestrescheduleno, "
									+ " json_build_object('duserblockfromdate','"
									+ dateUtilityFunction
											.instantDateToStringWithFormat(dateUtilityFunction.getCurrentDateTime(userInfo), "yyyy-MM-dd HH:mm:ss")
									+ "','duserblocktodate','"
									+ dateUtilityFunction.instantDateToStringWithFormat(dateUtilityFunction.getCurrentDateTime(userInfo),
											"yyyy-MM-dd HH:mm:ss")
									+ "','suserblockfromtime','00:00','suserblocktotime','00:00',"
									+ "'suserholdduration','0','dinstblockfromdate',NULL,'dinstblocktodate',NULL,'sinstblockfromtime',NULL"
									+ ",'sinstblocktotime',NULL,'sinstrumentholdduration',NULL,'scomments','')::jsonb,"
									+ " json_build_object('duserblockfromdate','"
									+ dateUtilityFunction
											.instantDateToStringWithFormat(dateUtilityFunction.getCurrentDateTime(userInfo), "yyyy-MM-dd HH:mm:ss")
									+ "','duserblocktodate','"
									+ dateUtilityFunction.instantDateToStringWithFormat(dateUtilityFunction.getCurrentDateTime(userInfo),
											"yyyy-MM-dd HH:mm:ss")
									+ "','suserblockfromtime','00:00','suserblocktotime','00:00',"
									+ "'suserholdduration','0','dinstblockfromdate',NULL,'dinstblocktodate',NULL,'sinstblockfromtime',NULL"
									+ ",'sinstblocktotime',NULL,'sinstrumentholdduration',NULL,'scomments','')::jsonb,"
									+ userInfo.getNtranssitecode() + " nsitecode,"
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus"
									+ " from registrationtest rt" + " where rt.ntransactiontestcode in (" + addedTestCode
									+ ") and rt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and rt.nsitecode=" + userInfo.getNtranssitecode();

			jdbcTemplate.execute(query12);	

		}

		inputMap.put("ntransactiontestcode", createdTestCode);
		final Map<String, Object> returnmap = new HashMap<String, Object>();

		returnmap.put("ntransactiontestcode", createdTestCode);

		if (directAddTest) {
			if (!strTestHistory.isEmpty()) {
				strTestHistory = "insert into registrationhistory (nreghistorycode, npreregno, ntransactionstatus, dtransactiondate, nusercode, nuserrolecode, "
						+ " ndeputyusercode, ndeputyuserrolecode, scomments, nsitecode, nstatus,noffsetdtransactiondate,ntransdatetimezonecode) values"
						+ strTestHistory.substring(0, strTestHistory.length() - 1) + ";";
			}

			jdbcTemplate.execute(strTestHistory);

			if (inputMap.get("ntransactiontestcode") != null && inputMap.get("ntransactiontestcode") != "") {
				returnmap.putAll((Map<String, Object>) getRegistrationTest(inputMap, userInfo).getBody());
				returnmap.putAll(
						(Map<String, Object>) getRegistrationSubSampleAfterTestAdd(inputMap, userInfo).getBody());
				returnmap.putAll((Map<String, Object>) getRegistrationSampleAfterSubsampleAdd(inputMap, userInfo));

				jsonAuditObject.put("registrationtest", (List<Map<String, Object>>) returnmap.get("selectedTest"));
				auditmap.put("nregtypecode", nregtypecode);
				auditmap.put("nregsubtypecode", nregsubtypecode);
				auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
				actionType.put("registrationtest", (boolean) inputMap.get("loadAdhocTest")?"IDS_ADHOCTEST":"IDS_ADDTEST");
				auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);
			}
		}

		return new ResponseEntity<Object>(returnmap, HttpStatus.OK);

	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> createQueryCreateTestParameterHistory(RegistrationSampleHistory objRegistrationSample, // int
			// nRecievedStatus,
		String comPleted, String stestStatus, int nTransactionTestCode, int nTesthistoryCode,
		int nRegistrationParameterCode, List<String> replicateTestList, List<String> replicateTestHistoryList,
		List<String> replicateTestParameterList, UserInfo userInfo, String sntestgrouptestcode,
		final Map<String, Object> inputMap, List<Integer> transactionTestCodeList,
		List<Integer> autoCompleteTestCodeList, List<Integer> nonAutoCompleteTestCodeList,
		List<Integer> jobAllocationTestList, boolean isAutoComplete, boolean needJobAllocation) throws Exception 
	{
		final ObjectMapper objMapper = new ObjectMapper();
		
		final String testGroupQuery = " select  tgt.nrepeatcountno, tgt.ntestgrouptestcode, tgt.ntestcode,tgt.stestsynonym, tgt.nsectioncode,"
										+ " s.ssectionname, tgt.nmethodcode, m.smethodname, tgt.ninstrumentcatcode, tm.nchecklistversioncode,"
										+ " coalesce((" + " select max(ntestrepeatno) + 1 from registrationtest "
										+ " where ntransactionsamplecode in (" + objRegistrationSample.getNtransactionsamplecode() + ")"
										+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and nsitecode=" + userInfo.getNtranssitecode()
										+ " and ntestgrouptestcode = tgt.ntestgrouptestcode" + " ),1) ntestrepeatno,"
										+ Enumeration.TransactionStatus.ALL.gettransactionstatus() + " ntestretestno, " + " tgt.ncost,"
										+ " case when " + needJobAllocation + " = false then " + stestStatus + " else "
										+ " coalesce((select max(ntransactionstatus) from registrationsectionhistory " 
										+ " where npreregno="+ objRegistrationSample.getNpreregno() 
										+ " and nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
										+ " and nsitecode="+ userInfo.getNtranssitecode() 
										+ " and nsectioncode = tgt.nsectioncode), " + stestStatus
										+ " ) end ntransactionstatus,"										
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus"
										+ " from testgrouptest tgt, testmaster tm, transactionstatus ts, section s,method m  "
										+ " where tm.ntestcode = tgt.ntestcode and tgt.ntestgrouptestcode in (" + comPleted
										+ ") and tgt.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
										+ " and tm.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
										+ " and tm.nsitecode="+ userInfo.getNmastersitecode() 
										+ " and ts.ntranscode in (" + stestStatus
										+ ") and ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and tgt.nsectioncode = s.nsectioncode and tgt.nmethodcode=m.nmethodcode "
										+ " and s.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and s.nsitecode="+ userInfo.getNmastersitecode() 
										+ " and m.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and m.nsitecode="+ userInfo.getNmastersitecode() ;
		List<RegistrationTest> testGroupTestList = jdbcTemplate.query(testGroupQuery, new RegistrationTest());
		
		testGroupTestList = registrationDAOSupport.getTestPrice(testGroupTestList);
		
		String strQuery = "";
		String strAdhocQuery = "";
		if ((int) inputMap.get("nsampletypecode") == Enumeration.SampleType.CLINICALSPEC.getType()) {
		final int preregno = objRegistrationSample.getNpreregno();
		Optional<Map<String, Object>> age = ((List<Map<String, Object>>) inputMap.get("ageData")).stream()
										.filter(x -> ((Integer) x.get("npreregno")) == preregno).findAny();
		
		//Commented by L.Subashini on 8/5/2025 as it is unused-start
	//	int nage = (int) age.get().get("nage");
		//Commented by L.Subashini on 8/5/2025 as it is unused-end
		
		
		int ngendercode = (int) age.get().get("ngendercode");
		String sdob = (String) age.get().get("sdob");
		
		// ALPD-5680	Added sdob by Vishakh to handle null issue in query (09-04-2025)
		
		String sYears="SELECT CURRENT_DATE - TO_DATE('"+sdob+"','"+userInfo.getSpgsitedatetime()+"' )";
		long ageInDays= jdbcTemplate.queryForObject(sYears,Long.class);
		
		strQuery = " case when tgtp.nparametertypecode=" + Enumeration.ParameterType.NUMERIC.getparametertype()
					+ " then case when  " + " tgtcs.ngendercode=" + ngendercode + " and " + ageInDays
					//+ " between tgtcs.nfromage and tgtcs.ntoage then "
					+ " between case when tgtcs.nfromageperiod="+Enumeration.Period.Years.getPeriod()+""
					+ " then (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(years => tgtcs.nfromage)))::int)"
					+ " when tgtcs.nfromageperiod="+Enumeration.Period.Month.getPeriod()+" then "
					+ " (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(months => tgtcs.nfromage)))::int) "
					+ " else (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(days => tgtcs.nfromage)))::int) end "
					+ " and case when tgtcs.ntoageperiod="+Enumeration.Period.Years.getPeriod()+" then (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(years => tgtcs.ntoage)))::int) "
					+ " when tgtcs.ntoageperiod="+Enumeration.Period.Month.getPeriod()+" then (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(months => tgtcs.ntoage)))::int) "
					+ " else (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(days => tgtcs.ntoage)))::int) end "
					+ " then json_build_object('nfromage',tgtcs.nfromage,'ntoage',tgtcs.ntoage,'ngendercode',tgtcs.ngendercode,'ngradecode',tgtcs.ngradecode,"
					+ " 'sminb',case when tgtcs.slowb='null' then NULL else tgtcs.slowb end,"
					+ " 'smina',case when tgtcs.slowa='null' then NULL else tgtcs.slowa end,"
					+ " 'smaxa',case when tgtcs.shigha='null' then NULL else tgtcs.shigha end,"
					+ " 'smaxb',case when tgtcs.shighb='null' then NULL else tgtcs.shighb end,"
					+ " 'sresultvalue',case when tgtcs.sresultvalue='null' then NULL else tgtcs.sresultvalue end,"
					+ " 'nroundingdigits',tgtp.nroundingdigits,'sresult',NULL,'sfinal',NULL,'stestsynonym',concat(tgt.stestsynonym,'[',cast(  "
					+ " coalesce((select max(ntestrepeatno)  from registrationtest "
					+ "	where ntransactionsamplecode in (" + objRegistrationSample.getNtransactionsamplecode()
					+ ") and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode=" + userInfo.getNtranssitecode()
					+ " and ntestgrouptestcode = tgt.ntestgrouptestcode),1)"
					+ " as character varying),']','[0]'),'sparametersynonym',tgtp.sparametersynonym,'nresultaccuracycode',tgtp.nresultaccuracycode,'sresultaccuracyname', ra.sresultaccuracyname)::jsonb " + " else "
					+ " json_build_object('nfromage',tgtcs.nfromage,'ntoage',tgtcs.ntoage,'ngendercode',tgtcs.ngendercode,'ngradecode',tgtcs.ngradecode,"
					+ " 'sminb',case when tgtcs.slowb='null' then NULL else tgtcs.slowb end,"
					+ " 'smina',case when tgtcs.slowa='null' then NULL else tgtcs.slowa end,"
					+ " 'smaxa',case when tgtcs.shigha='null' then NULL else tgtcs.shigha end,"
					+ " 'smaxb',case when tgtcs.shighb='null' then NULL else tgtcs.shighb end,"
					+ " 'sresultvalue',case when tgtcs.sresultvalue='null' then NULL else tgtcs.sresultvalue end,"
					+ " 'nroundingdigits',tgtp.nroundingdigits,'sresult',NULL,'sfinal',NULL,'stestsynonym',concat(tgt.stestsynonym,'[',cast(  "
					+ " coalesce((select max(ntestrepeatno)  from registrationtest "
					+ "	where ntransactionsamplecode in (" + objRegistrationSample.getNtransactionsamplecode()
					+ ") and nsitecode = " + userInfo.getNtranssitecode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ntestgrouptestcode = tgt.ntestgrouptestcode),1)"
					+ " as character varying),']','[0]'),'sparametersynonym',tgtp.sparametersynonym,'nresultaccuracycode',tgtp.nresultaccuracycode,'sresultaccuracyname', ra.sresultaccuracyname)::jsonb "
					+ " end else " + " json_build_object("
					+ " 'sminb',case when tgtcs.slowb='null' then NULL else tgtcs.slowb end,"
					+ " 'smina',case when tgtcs.slowa='null' then NULL else tgtcs.slowa end,"
					+ " 'smaxa',case when tgtcs.shigha='null' then NULL else tgtcs.shigha end,"
					+ " 'smaxb',case when tgtcs.shighb='null' then NULL else tgtcs.shighb end,"
					+ " 'sresultvalue',case when tgtcs.sresultvalue='null' then NULL else tgtcs.sresultvalue end,"
					+ "'nroundingdigits',tgtp.nroundingdigits,'sresult',NULL,'sfinal',NULL,'stestsynonym',concat(tgt.stestsynonym,'[',cast(  "
					+ " coalesce((select max(ntestrepeatno)  from registrationtest "
					+ " where ntransactionsamplecode in (" + objRegistrationSample.getNtransactionsamplecode()
					+ ") and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode = " + userInfo.getNtranssitecode()
					+ " and ntestgrouptestcode = tgt.ntestgrouptestcode),1)"
					+ " as character varying),']','[0]'),'sparametersynonym',tgtp.sparametersynonym,'nresultaccuracycode',tgtp.nresultaccuracycode,'sresultaccuracyname', ra.sresultaccuracyname)::jsonb "
					+ " end jsondata," + userInfo.getNtranssitecode() + " nsitecode,"
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
					+ " from resultaccuracy ra,testmaster tm,testgrouptest tgt "
					+ " inner join testgrouptestparameter tgtp  on tgt.ntestgrouptestcode = tgtp.ntestgrouptestcode "
					+ " left outer join testgrouptestclinicalspec tgtcs on tgtcs.ntestgrouptestparametercode = tgtp.ntestgrouptestparametercode "
					+ " and tgtcs.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and tgtcs.nsitecode = " + userInfo.getNmastersitecode()
					+ " and tgtcs.ngendercode=" + ngendercode + " and " + ageInDays
					+ " between case when tgtcs.nfromageperiod="+Enumeration.Period.Years.getPeriod()+""
					+ " then (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(years => tgtcs.nfromage)))::int)"
					+ " when tgtcs.nfromageperiod="+Enumeration.Period.Month.getPeriod()+" then "
					+ " (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(months => tgtcs.nfromage)))::int) "
					+ " else (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(days => tgtcs.nfromage)))::int) end "
					+ " and case when tgtcs.ntoageperiod="+Enumeration.Period.Years.getPeriod()+" then (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(years => tgtcs.ntoage)))::int) "
					+ " when tgtcs.ntoageperiod="+Enumeration.Period.Month.getPeriod()+" then (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(months => tgtcs.ntoage)))::int) "
					+ " else (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(days => tgtcs.ntoage)))::int) end ";
					//+ " between tgtcs.nfromage and tgtcs.ntoage ";
		
		strAdhocQuery=" and tgtp.nisadhocparameter="+ Enumeration.TransactionStatus.NO.gettransactionstatus();
		
		} else {
		strQuery = "json_build_object('sminlod',tgtnp.sminlod,'smaxlod',tgtnp.smaxlod,'sminb',tgtnp.sminb,'smina',tgtnp.smina,"
					+ "'smaxa',tgtnp.smaxa,'smaxb',tgtnp.smaxb,'sminloq',tgtnp.sminloq,'smaxloq',tgtnp.smaxloq,'ngradecode',tgtnp.ngradecode,'nresultaccuracycode',tgtp.nresultaccuracycode,'sresultaccuracyname',ra.sresultaccuracyname, "
					+ "'sdisregard',tgtnp.sdisregard,'sresultvalue',tgtnp.sresultvalue,"
					+ "'nroundingdigits',tgtp.nroundingdigits,'sresult',NULL,'sfinal',NULL,'stestsynonym',concat(tgt.stestsynonym,'[',cast(  "
					+ " coalesce((select max(ntestrepeatno)  from registrationtest "
					+ " where ntransactionsamplecode in (" + objRegistrationSample.getNtransactionsamplecode()
					+ ") and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode=" + userInfo.getNtranssitecode()
					+ " and ntestgrouptestcode = tgt.ntestgrouptestcode),1)"
					+ " as character varying),']','[0]'),'sparametersynonym',tgtp.sparametersynonym)::jsonb jsondata"
					+ " ," + userInfo.getNtranssitecode() + " nsitecode,"
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
					+ " from resultaccuracy ra,testmaster tm,testgrouptest tgt,testgrouptestparameter tgtp ";
		strAdhocQuery=" and tgtp.nisvisible="+ Enumeration.TransactionStatus.YES.gettransactionstatus();
		}
		
		final String query4 = " select  tgt.ntestgrouptestcode, tgtp.ntestgrouptestparametercode, tgtp.ntestparametercode,"
								+ " tgtp.nparametertypecode, coalesce(tgtf.ntestgrouptestformulacode, "
								+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ")"
								+ " ntestgrouptestformulacode, tgtp.nunitcode, tgtp.nresultmandatory, tgtp.nreportmandatory," + strQuery
								+ " left outer join testgrouptestformula tgtf"
								+ " on tgtp.ntestgrouptestparametercode = tgtf.ntestgrouptestparametercode" + " and tgtf.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgtf.ntransactionstatus = "
								+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " " +strAdhocQuery
								//" and tgtp.nisadhocparameter = "+ Enumeration.TransactionStatus.NO.gettransactionstatus()
								+ " left outer join testgrouptestnumericparameter tgtnp on tgtp.ntestgrouptestparametercode = tgtnp.ntestgrouptestparametercode"
								+ " and tgtnp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and tgtnp.nsitecode = " + userInfo.getNmastersitecode()								
								+ " where tgt.ntestgrouptestcode = tgtp.ntestgrouptestcode and tm.ntestcode = tgt.ntestcode and tgtp.nresultaccuracycode=ra.nresultaccuracycode and tgt.nstatus = tm.nstatus"
								+ " and tgt.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and tgtp.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and ra.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and tgt.nsitecode = " + userInfo.getNmastersitecode()	
								+ " and tgtp.nsitecode = " + userInfo.getNmastersitecode()	
								+ " and ra.nsitecode = " + userInfo.getNmastersitecode()	
								+ " and tgt.ntestgrouptestcode in ("
								+ comPleted + ") " + strAdhocQuery+";";
		
		final List<TestGroupTestParameter> parameterList = jdbcTemplate.query(query4, new TestGroupTestParameter());
		
		for (RegistrationTest regTestGroupTest : testGroupTestList) 
		{
			if (regTestGroupTest.getNrepeatcountno() > 1) {
				for (int repeatNo = regTestGroupTest.getNtestrepeatno(); repeatNo < (regTestGroupTest.getNtestrepeatno()
						+ regTestGroupTest.getNrepeatcountno()); repeatNo++)
						// for(int repeatNo=1; repeatNo <= regTestGroupTest.getNrepeatcountno();
						// repeatNo++)
				{
				int nttestcode = nTransactionTestCode++;
				transactionTestCodeList.add(nttestcode);
				
				if (objRegistrationSample.getNtransactionstatus() != Enumeration.TransactionStatus.PREREGISTER
						.gettransactionstatus()
						&& objRegistrationSample.getNtransactionstatus() != Enumeration.TransactionStatus.QUARENTINE
						.gettransactionstatus()) 
				{
				
					if (isAutoComplete) {
						autoCompleteTestCodeList.add(nttestcode);
					} else {
						nonAutoCompleteTestCodeList.add(nttestcode);
					}
					if (!needJobAllocation) {
						jobAllocationTestList.add(nttestcode);
					}
				
				}
				
				final String strQuery1 = "select ssettingvalue from settings "
						         + " where nsettingcode ="+Enumeration.Settings.UPDATING_ANALYSER.getNsettingcode()
						         + " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final Settings objSettings = (Settings) jdbcUtilityFunction.queryForObject(strQuery1, Settings.class, jdbcTemplate);
				String analyzerValue ="";
				
				if(objSettings!=null) {
					if(Integer.valueOf(objSettings.getSsettingvalue())== Enumeration.TransactionStatus.YES.gettransactionstatus()) {
						analyzerValue = ",'AnalyserCode',-1,'AnalyserName','-'";
					}
				}				
				
				replicateTestList.add("(" + nttestcode + "," + objRegistrationSample.getNtransactionsamplecode()
										+ "," + objRegistrationSample.getNpreregno() + ","
										+ regTestGroupTest.getNtestgrouptestcode() + "," + regTestGroupTest.getNinstrumentcatcode()
										+ ",-1," + repeatNo + ",0," + " json_build_object('ntransactiontestcode', " + nttestcode
										+ ",'npreregno'," + objRegistrationSample.getNpreregno() + ",'ntransactionsamplecode',"
										+ objRegistrationSample.getNtransactionsamplecode() + ",'ssectionname','"
										+ stringUtilityFunction.replaceQuote(regTestGroupTest.getSsectionname()) + "','smethodname','"
										+ stringUtilityFunction.replaceQuote(regTestGroupTest.getSmethodname()) + "','ncost',"
										+ regTestGroupTest.getNcost() + "," + "'stestname','"
										+ stringUtilityFunction.replaceQuote(regTestGroupTest.getStestsynonym()) + "'," + "'stestsynonym',concat('"
										+ stringUtilityFunction.replaceQuote(regTestGroupTest.getStestsynonym()) + "','[" + repeatNo + "]["
										+ regTestGroupTest.getNtestretestno() + "]')"+analyzerValue+")::jsonb," + userInfo.getNtranssitecode() + ",'"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
										+ regTestGroupTest.getNtestcode() + "," + regTestGroupTest.getNsectioncode() + ","
										+ regTestGroupTest.getNmethodcode() + ")");
				
				int nttesthistorycode = nTesthistoryCode++;
				replicateTestHistoryList.add("(" + nttesthistorycode + "," + nttestcode + ","
												+ objRegistrationSample.getNtransactionsamplecode() + ","
												+ objRegistrationSample.getNpreregno() + "," + userInfo.getNformcode() + ","
												+ regTestGroupTest.getNtransactionstatus() + "," + userInfo.getNusercode() + ","
												+ userInfo.getNuserrole() + "," + userInfo.getNdeputyusercode() + ","
												+ userInfo.getNdeputyuserrole() + ",'" + stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "','"
												+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',-1," + userInfo.getNtranssitecode() + ","
												+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
												+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode()
												+ ")");
				
				for (TestGroupTestParameter registrationParameter : parameterList) {
					if (regTestGroupTest.getNtestgrouptestcode() == registrationParameter.getNtestgrouptestcode()) {
						int nttestparametercode = nRegistrationParameterCode++;
					
						final Map<String, Object> mapObject = registrationParameter.getJsondata();
						mapObject.put("ntransactionresultcode", nttestparametercode);
						mapObject.put("ntransactiontestcode", nttestcode);
						mapObject.put("stestsynonym", regTestGroupTest.getStestsynonym() + "[" + repeatNo + "]["
										+ regTestGroupTest.getNtestretestno() + "]");
						
						replicateTestParameterList.add("(" + nttestparametercode + ","
														+ objRegistrationSample.getNpreregno() + "," + nttestcode + ","
														+ registrationParameter.getNtestgrouptestparametercode() + ","
														+ registrationParameter.getNtestparametercode() + ","
														+ registrationParameter.getNparametertypecode() + ","
														+ registrationParameter.getNtestgrouptestformulacode() + ","
														+ registrationParameter.getNunitcode() + ","
														+ registrationParameter.getNresultmandatory() + ","
														+ registrationParameter.getNreportmandatory() + "," + "'"
														+ objMapper.writeValueAsString(mapObject) + "'," + userInfo.getNtranssitecode()
														+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")");
						}
					}
				}
		} 
		else {
			// no replicate mentioned in test group
			int nttestcode = nTransactionTestCode++;
			transactionTestCodeList.add(nttestcode);
			
			if (objRegistrationSample.getNtransactionstatus() != Enumeration.TransactionStatus.PREREGISTER
				.gettransactionstatus()
				&& objRegistrationSample.getNtransactionstatus() != Enumeration.TransactionStatus.QUARENTINE
				.gettransactionstatus()) 
			{
		
				if (isAutoComplete) {
						autoCompleteTestCodeList.add(nttestcode);
				} 
				else {
					nonAutoCompleteTestCodeList.add(nttestcode);
				}
				if (!needJobAllocation) {
					jobAllocationTestList.add(nttestcode);
				}
			}
			
			final String strQuery1 = "select ssettingvalue from settings where nsettingcode ="+Enumeration.Settings.UPDATING_ANALYSER.getNsettingcode()
									+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
							
			final Settings objSettings = (Settings) jdbcUtilityFunction.queryForObject(strQuery1, Settings.class, jdbcTemplate);
			String analyzerValue ="";
			
			if(objSettings!=null) {
				if(Integer.valueOf(objSettings.getSsettingvalue())== Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					analyzerValue = ",'AnalyserCode',-1,'AnalyserName','-'";
				}
			}
			
			
			replicateTestList.add("(" + nttestcode + "," + objRegistrationSample.getNtransactionsamplecode() + ","
								+ objRegistrationSample.getNpreregno() + "," + regTestGroupTest.getNtestgrouptestcode() + ","
								+ regTestGroupTest.getNinstrumentcatcode() + ",-1," + (regTestGroupTest.getNtestrepeatno())
								+ ",0," + " json_build_object('ntransactiontestcode', " + nttestcode + ",'npreregno',"
								+ objRegistrationSample.getNpreregno() + ",'ntransactionsamplecode',"
								+ objRegistrationSample.getNtransactionsamplecode() + ",'ssectionname','"
								+ stringUtilityFunction.replaceQuote(regTestGroupTest.getSsectionname()) + "','smethodname','"
								+ stringUtilityFunction.replaceQuote(regTestGroupTest.getSmethodname()) + "','ncost'," + regTestGroupTest.getNcost()
								+ "," + "'stestname','" + stringUtilityFunction.replaceQuote(regTestGroupTest.getStestsynonym()) + "',"
								+ "'stestsynonym',concat('" + stringUtilityFunction.replaceQuote(regTestGroupTest.getStestsynonym()) + "','["
								+ (regTestGroupTest.getNtestrepeatno()) + "][0]')"+analyzerValue+")::jsonb," + userInfo.getNtranssitecode()
								+ ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
								+ regTestGroupTest.getNtestcode() + "," + regTestGroupTest.getNsectioncode() + ","
								+ regTestGroupTest.getNmethodcode() + ")");
			
			int nttesthistorycode = nTesthistoryCode++;
			replicateTestHistoryList.add("(" + nttesthistorycode + "," + nttestcode + ","
											+ objRegistrationSample.getNtransactionsamplecode() + "," + objRegistrationSample.getNpreregno()
											+ "," + userInfo.getNformcode() + ","
											// + Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + ","
											+ regTestGroupTest.getNtransactionstatus() + "," + userInfo.getNusercode() + ","
											+ userInfo.getNuserrole() + "," + userInfo.getNdeputyusercode() + ","
											+ userInfo.getNdeputyuserrole() + ",'" + stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "','"
											+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',-1," + userInfo.getNtranssitecode() + ","
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
											+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode()
											+ ")");
			
			for (TestGroupTestParameter testGroupTestParameter : parameterList) {
				if (regTestGroupTest.getNtestgrouptestcode() == testGroupTestParameter.getNtestgrouptestcode()) {
					int nttestparametercode = nRegistrationParameterCode++;
			
					final Map<String, Object> mapObject = testGroupTestParameter.getJsondata();
					mapObject.put("ntransactionresultcode", nttestparametercode);
					mapObject.put("ntransactiontestcode", nttestcode);
					mapObject.put("stestsynonym", regTestGroupTest.getStestsynonym() + "["
									+ (regTestGroupTest.getNtestrepeatno()) + "][0]");
				
					replicateTestParameterList
						.add("(" + nttestparametercode + "," + objRegistrationSample.getNpreregno() + ","
								+ nttestcode + "," + testGroupTestParameter.getNtestgrouptestparametercode()
								+ "," + testGroupTestParameter.getNtestparametercode() + ","
								+ testGroupTestParameter.getNparametertypecode() + ","
								+ testGroupTestParameter.getNtestgrouptestformulacode() + ","
								+ testGroupTestParameter.getNunitcode() + ","
								+ testGroupTestParameter.getNresultmandatory() + ","
								+ testGroupTestParameter.getNreportmandatory() + ","
								// + testGroupTestParameter.getJsondata()+","
								// + "'" + stringUtilityFunction.replaceQuote(testGroupTestParameter.getJsondata().toString()) + "',"
								// + "'" +objmap.writeValueAsString(testGroupTestParameter.getJsondata()) +"',"
								+ "'" + stringUtilityFunction.replaceQuote(objMapper.writeValueAsString(mapObject)) + "',"
								+ userInfo.getNtranssitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")");
					}
				}
			
			}
		}
		
		final Map<String, Object> returnMap = new HashMap<String, Object>() {};
	
		returnMap.put("replicateTestHistoryList", replicateTestHistoryList);
		returnMap.put("replicateTestList", replicateTestList);
		returnMap.put("replicateTestParameterList", replicateTestParameterList);
		returnMap.put("nTransactionTestCode", nTransactionTestCode);
		returnMap.put("nTesthistoryCode", nTesthistoryCode);
		returnMap.put("nRegistrationParameterCode", nRegistrationParameterCode);
		returnMap.put("autoCompleteTestCodeList", autoCompleteTestCodeList);
		returnMap.put("nonAutoCompleteTestCodeList", nonAutoCompleteTestCodeList);
		returnMap.put("transactionTestCodeList", transactionTestCodeList);
		returnMap.put("jobAllocationTestList", jobAllocationTestList);
		
		return returnMap;
		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ResponseEntity<Object> getRegistrationSubSampleAfterTestAdd(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> objMap = new HashMap();

		final boolean subSample = (boolean) inputMap.get("nneedsubsample");

		final String query1 = "select * from fn_registrationsubsampleget('" + inputMap.get("npreregno") + "'::text,"
								+ "'" + inputMap.get("ntransactionsamplecode") + "'::text" + "," + inputMap.get("ntype") + ","
								+ userInfo.getNtranssitecode() + ",'" + userInfo.getSlanguagetypecode() + "','"
								+ commonFunction.getMultilingualMessage("IDS_REGNO", userInfo.getSlanguagefilename()) + "'" + ",'"
								+ commonFunction.getMultilingualMessage("IDS_SAMPLENO", userInfo.getSlanguagefilename()) + "')";

		LOGGER.info("sub sample Start========?" + LocalDateTime.now());
		LOGGER.info("sub sample query:" + query1);

		final String lstData1 = jdbcTemplate.queryForObject(query1, String.class);
		LOGGER.info("sub sample end========?" + LocalDateTime.now());

		if (lstData1 != null) {

			List<Map<String, Object>> lstData = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(
					lstData1, userInfo, true, (int) inputMap.get("ndesigntemplatemappingcode"), "subsample");

			objMap.put("RegistrationGetSubSample", lstData);

			objMap.putAll(transactionDAOSupport.getActiveSampleTabData((String) inputMap.get("npreregno"), "0",
					(String) inputMap.get("activeSampleTab"), userInfo));
			objMap.put("activeSampleTab", inputMap.get("activeSampleTab"));
			if (!subSample || (int) inputMap.get("checkBoxOperation") == 3
					|| (int) inputMap.get("checkBoxOperation") == 7) {
				String stransactionsampleno = "";
				if (!inputMap.containsKey("ntype")) {
					stransactionsampleno = String
							.valueOf(lstData.get(lstData.size() - 1).get("ntransactionsamplecode"));
					inputMap.put("ntransactionsamplecode", stransactionsampleno);
					objMap.put("selectedSubSample", Arrays.asList(lstData.get(lstData.size() - 1)));
				} else if ((int) inputMap.get("ntype") == 2) {
					stransactionsampleno = lstData.stream().map(x -> String.valueOf(x.get("ntransactionsamplecode")))
							.collect(Collectors.joining(","));
					inputMap.put("ntransactionsamplecode", stransactionsampleno);
					objMap.put("selectedSubSample", lstData);
				} else if ((int) inputMap.get("ntype") == 3) {
					String ntransactionsamplecode = (String) inputMap.get("ntransactionsamplecode");
					stransactionsampleno = ntransactionsamplecode;
					List<String> myList = new ArrayList<String>(Arrays.asList(ntransactionsamplecode.split(",")));
					lstData = lstData.stream()
							.filter(x -> myList.stream()
									.anyMatch(y -> y.equals(String.valueOf(x.get("ntransactionsamplecode")))))
							.collect(Collectors.toList());
					inputMap.put("ntype", 4);
					objMap.put("selectedSubSample", lstData);
				} else if ((int) inputMap.get("ntype") == 5) {
					stransactionsampleno = (String) inputMap.get("ntransactionsamplecode");
					objMap.put("selectedSubSample", lstData);
				}

				else {
					stransactionsampleno = String
							.valueOf(lstData.get(lstData.size() - 1).get("ntransactionsamplecode"));
					objMap.put("selectedSubSample", Arrays.asList(lstData.get(lstData.size() - 1)));
					inputMap.put("ntransactionsamplecode", stransactionsampleno);
					if ((int) inputMap.get("checkBoxOperation") == 7 && (int) inputMap.get("ntype") == 4) {
						inputMap.put("ntype", 5);
					}

				}

				objMap.putAll(transactionDAOSupport.getActiveSubSampleTabData(stransactionsampleno,
						(String) inputMap.get("activeSubSampleTab"), userInfo));

				objMap.put("activeSubSampleTab", inputMap.get("activeSubSampleTab"));

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
	
	public Map<String, Object> getRegistrationSampleAfterSubsampleAdd(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> objMap = new HashMap<>();
//		short nfilterstatus;
//		if (inputMap.get("nfilterstatus").getClass().getTypeName().equals("java.lang.Integer")) {
//			nfilterstatus = ((Integer) inputMap.get("nfilterstatus")).shortValue();
//		} else {
//			nfilterstatus = (short) inputMap.get("nfilterstatus");
//		}
		// ALPD-3480
		short nfilterstatus = 0;
		final String registrationMultilingual = commonFunction.getMultilingualMessage("IDS_REGNO",
				userInfo.getSlanguagefilename());

		final String query1 = "select * from fn_registrationsampleget('" + inputMap.get("FromDate") + "'::text," + "'"
								+ inputMap.get("ToDate") + "'::text" + "," + inputMap.get("nsampletypecode") + ","
								+ userInfo.getNusercode() + "," + inputMap.get("nregtypecode") + "," + inputMap.get("nregsubtypecode")
								+ "," + "" + nfilterstatus + "::integer," + userInfo.getNtranssitecode() + ",'"
								+ inputMap.get("npreregno") + "'::text," + "'" + userInfo.getSlanguagetypecode() + "'::text," + ""
								+ inputMap.get("ndesigntemplatemappingcode") + ",'" + registrationMultilingual + "'::text,"
								+ inputMap.get("napproveconfversioncode") + ")";

		LOGGER.info("Sample Start========?" + LocalDateTime.now());

		String lstData1 = jdbcTemplate.queryForObject(query1, String.class);
		LOGGER.info("Sample end========?" + query1 + " :" + LocalDateTime.now());

		List<Map<String, Object>> lstData = new ArrayList<>();

		if (lstData1 != null) {
			lstData = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(lstData1, userInfo, true,
					(int) inputMap.get("ndesigntemplatemappingcode"), "sample");

			LOGGER.info("Sample Size" + lstData.size());

			objMap.put("RegistrationGetSample", lstData);

			String spreregno = "";
			if (!inputMap.containsKey("ntype")) {
				spreregno = String.valueOf(lstData.get(lstData.size() - 1).get("npreregno"));
				inputMap.put("npreregno", spreregno);
				objMap.put("selectedSample", Arrays.asList(lstData.get(lstData.size() - 1)));
			} else {
				objMap.put("selectedSample", lstData);
			}

			objMap.putAll(transactionDAOSupport.getActiveSampleTabData((String) inputMap.get("npreregno"), "0",
					(String) inputMap.get("activeSampleTab"), userInfo));
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
	
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getEditRegistrationDetails(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		final String query = "select json_agg( r.jsonuidata||r.jsondata||json_build_object('sspecname',tgs.sspecname,'sversion',tgs.sversion,'ntransactionstatus',rh.ntransactionstatus,"
								+ "'stransdisplaystatus',ts.jsondata->'" + userInfo.getSlanguagetypecode()
								+ "'->>'stransdisplaystatus','sarno',ra.sarno,'ncategorybasedflow',pc.ncategorybasedflow)::jsonb )"
								+ "from registration r,registrationhistory rh,registrationarno ra, "
								+ "	(select max(rh.nreghistorycode) as nreghistorycode,  "
								+ "	rh.npreregno from registrationhistory rh,registration r" 
								+ "	where rh.nstatus= "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and rh.nsitecode="+ userInfo.getNtranssitecode()
								+ " and r.nsitecode = " + userInfo.getNtranssitecode()
								+ " and r.nstatus= "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and r.npreregno=rh.npreregno and	 rh.npreregno in (" + inputMap.get("npreregno")
								+ ") group by rh.npreregno )tableA,transactionstatus ts,productcategory pc,testgroupspecification tgs "
								+ "	where  r.npreregno=rh.npreregno  and r.npreregno=ra.npreregno and "
								+ "	pc.nproductcatcode=r.nproductcatcode and r.nallottedspeccode=tgs.nallottedspeccode   and  pc.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ "	and ts.ntranscode = rh.ntransactionstatus "
								+ " and ts.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and r.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and r.nsitecode = " + userInfo.getNtranssitecode()
								+ " and rh.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and r.nsitecode = " + userInfo.getNtranssitecode()
								+ " and ra.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and ra.nsitecode = " + userInfo.getNtranssitecode()
								+ "  and tableA.nreghistorycode =rh.nreghistorycode and rh.npreregno in (" + inputMap.get("npreregno")
								+ ") ";

		final String dynamicList = jdbcTemplate.queryForObject(query, String.class);

		List<Map<String, Object>> lstData = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(
				dynamicList, userInfo, true, (int) inputMap.get("ndesigntemplatemappingcode"), "sample");

		final Map<String, Object> outputMap = new HashMap<String, Object>();
		outputMap.put("EditData", lstData.get(0));

		final int sampleTypeCode = (int) inputMap.get("nsampletypecode");
		final Boolean needSubSample = (Boolean) inputMap.get("nneedsubsample");
		//int ncategorybasedflow = (int) lstData.get(0).get("ncategorybasedflow");
		int projectMasterCode = -1;
		if (sampleTypeCode == Enumeration.SampleType.PROJECTSAMPLETYPE.getType()) {
			projectMasterCode = (int) lstData.get(0).get("nprojectmastercode");
		}


		if (inputMap.containsKey("getSampleChildDetail") && (Boolean) inputMap.get("getSampleChildDetail") == true) {
			final Map<String, Object> getMapData = new HashMap<String, Object>();

			if (!needSubSample) {
				getMapData.put("ntype", 2);
			}

			getMapData.put("nflag", 2);
			getMapData.put("nsampletypecode", inputMap.get("nsampletypecode"));
			getMapData.put("nregtypecode", inputMap.get("nregtypecode"));
			getMapData.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
			getMapData.put("npreregno", Integer.toString((Integer) (inputMap.get("npreregno"))));
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

			childDetailMap.putAll((Map<String, Object>) getRegistrationSubSample(getMapData, userInfo).getBody());
			outputMap.put("SampleChildDetail", childDetailMap);
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> updateRegistrationWithFile(MultipartHttpServletRequest inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final UserInfo userInfo = objectMapper.readValue(inputMap.getParameter("userinfo"), new TypeReference<UserInfo>() {});
		final Object reg = objectMapper.readValue(inputMap.getParameter("Map"), new TypeReference<Object>() {});
		final String uploadStatus = ftpUtilityFunction.getFileFTPUpload(inputMap, -1, userInfo);

		if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(uploadStatus)) {
			// returnMap.put("inputMap", inputMap.getParameter("Map"));
			return (ResponseEntity<Object>) updateRegistration((Map<String, Object>) reg);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(uploadStatus, userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> updateRegistration(final Map<String, Object> inputMap) throws Exception {
		
		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditOld = new JSONObject();
		JSONObject jsonAuditNew = new JSONObject();
		
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		Map<String, Object> returnMap = null;
		Map<String, Object> initialParam = (Map<String, Object>) inputMap.get("initialparam");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});

		final String npreregno = (String) initialParam.get("npreregno");
		final Registration reg = objMapper.convertValue(inputMap.get("registration"),
				new TypeReference<Registration>() {});

		final List<Map<String, Object>> sampleCombinationUnique = (List<Map<String, Object>>) inputMap
				.get("samplecombinationunique");
		Map<String, Object> map = projectDAOSupport.validateUniqueConstraint(sampleCombinationUnique,
				(Map<String, Object>) inputMap.get("registration"), userInfo, "update", Registration.class, "npreregno",
				false);
		if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
				.equals(map.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
			return new ResponseEntity<>(map, HttpStatus.OK);
		}

		final List<String> dateList = objMapper.convertValue(inputMap.get("DateList"), new TypeReference<List<String>>() {});

		JSONObject jsoneditRegistration = new JSONObject(reg.getJsondata());
		JSONObject jsonuiRegistration = new JSONObject(reg.getJsonuidata());
		
		if (!dateList.isEmpty()) {
			jsoneditRegistration = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsoneditRegistration, dateList, false,
					userInfo);
			jsonuiRegistration = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonuiRegistration, dateList, false,
					userInfo);

		}
		jsonuiRegistration.put("npreregno", Integer.parseInt(npreregno));
		jsonuiRegistration.put("nsampletypecode", inputMap.get("nsampletypecode"));
		jsonuiRegistration.put("nregtypecode", inputMap.get("nregtypecode"));
		jsonuiRegistration.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
		jsonuiRegistration.put("nproductcatcode", reg.getNproductcatcode());
		jsonuiRegistration.put("nproductcode", reg.getNproductcode());
		jsonuiRegistration.put("ninstrumentcatcode", reg.getNinstrumentcatcode());
		jsonuiRegistration.put("ninstrumentcode", reg.getNinstrumentcode());
		jsonuiRegistration.put("nmaterialcatcode", reg.getNmaterialcatcode());
		jsonuiRegistration.put("nmaterialcode", reg.getNmaterialcode());
		jsonuiRegistration.put("ntemplatemanipulationcode", reg.getNtemplatemanipulationcode());
		jsonuiRegistration.put("nallottedspeccode", reg.getNallottedspeccode());
		jsonuiRegistration.put("ndesigntemplatemappingcode", reg.getNdesigntemplatemappingcode());

		final String queryString = "update registration set jsondata=jsondata||'"
				+ stringUtilityFunction.replaceQuote(jsoneditRegistration.toString()) + "'::jsonb,jsonuidata=jsonuidata||'"
				+ stringUtilityFunction.replaceQuote(jsonuiRegistration.toString()) + "'::jsonb  where npreregno in (" + npreregno + ")";

		jdbcTemplate.execute(queryString);

		//ALPD-4156--Vignesh R(17-05-2024)-->While edit the sample 500 occurs
		final String strQuery=" select ntransactionstatus from registrationhistory where nreghistorycode" 
								+" in(select max(nreghistorycode) from registrationhistory "
								+" where npreregno="+npreregno+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNtranssitecode()+")"
								+" and nsitecode="+userInfo.getNtranssitecode()
								+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
		Integer nfilterstatus=jdbcTemplate.queryForObject(strQuery, Integer.class);
		initialParam.put("nfilterstatus", nfilterstatus);
		
		returnMap = registrationDAOSupport.getDynamicRegistration(initialParam, userInfo);

		jsonAuditOld.put("registration", (List<Map<String, Object>>) inputMap.get("selectedSample"));
		jsonAuditNew.put("registration", (List<Map<String, Object>>) returnMap.get("RegistrationGetSample"));
		auditmap.put("nregtypecode", initialParam.get("nregtypecode"));
		auditmap.put("nregsubtypecode", initialParam.get("nregsubtypecode"));
		auditmap.put("ndesigntemplatemappingcode", reg.getNdesigntemplatemappingcode());
		actionType.put("registration", "IDS_EDITREGISTRATION");
		
		returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
				Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, jsonAuditNew, actionType, auditmap, false, userInfo);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}
	
	@SuppressWarnings({ "unused", "unlikely-arg-type" })
	@Override
	public Map<String, Object> seqNoTestSampleInsert(Map<String, Object> inputMap) throws Exception {

		final String sQuery = " lock  table lockcancelreject " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final ObjectMapper registrationMapper = new ObjectMapper();

		// ALPD-2047
//		final List<String> lst = Arrays.asList(
//				"registrationtesthistory","registrationhistory" );
//		seqNoMaxUpdate(lst);

		final String strSelectSeqno = "select stablename,nsequenceno from seqnoregistration "
									+ " where stablename in ('registrationtesthistory','registrationhistory')"
									+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<SeqNoRegistration> mapList = jdbcTemplate.query(strSelectSeqno, new SeqNoRegistration());

		Map<String, Integer> mapSeqno = mapList.stream().collect(Collectors.toMap(SeqNoRegistration::getStablename,
				SeqNoRegistration -> SeqNoRegistration.getNsequenceno()));

		Map<String, Object> returnMap = new HashMap<>();
		final UserInfo userInfo = registrationMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		String strNpreregno = (String) inputMap.get("ninsertpreregno") + "";

		int ncontrolcode = (int) inputMap.get("ncontrolcode");
		int nregTypeCode = (int) inputMap.get("nregtypecode");
		int nregSubTypeCode = (int) inputMap.get("nregsubtypecode");

		String stransactiontestcode = (String) inputMap.get("ntransactiontestcode") + "";

		// Updated below query by L.Subashini on 28/04/2022 to add regtype and
		// regsubtype in transactionvalidation

		// commented by rukshana for ALPD-1969
//		String transValidation = "select  max(rh.ntransactionstatus) ntransactionstatus,max(rh.ntransactiontestcode) ntransactiontestcode from "
//				+ "transactionvalidation tv,transactionstatus ts,registrationtesthistory rh  where  rh.ntransactionstatus=tv.ntransactionstatus and rh. ntransactiontestcode in ("
//				+ stransactiontestcode + ")  " + "and  tv.ntransactionstatus=ts.ntranscode and " + " rh.nstatus="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "and tv.nstatus ="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + "and ts.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + "and tv.nformcode="
//				+ userInfo.getNformcode() + " and tv.nregtypecode=" + nregTypeCode + " and tv.nregsubtypecode="
//				+ nregSubTypeCode + " and tv.ncontrolcode=" + ncontrolcode + " and rh.nsitecode="
//				+ userInfo.getNtranssitecode() + " and tv.nsitecode=" + userInfo.getNmastersitecode()
//				+ " group by npreregno,ntransactionsamplecode,ntransactiontestcode;";

		final String transValidation = "select rh.ntransactionstatus ntransactionstatus,"
									+ "rh.ntransactiontestcode ntransactiontestcode," + "coalesce (ts.jsondata->'salertdisplaystatus'->>"
									+ "'" + userInfo.getSlanguagetypecode()
									+ "',ts.jsondata->'salertdisplaystatus'->>'en-US') as stransdisplaystatus" + " from "
									+ " transactionstatus ts,registrationtesthistory rh  where  rh.ntransactionstatus=ts.ntranscode "
									+ " and rh.ntesthistorycode = any(select max(ntesthistorycode)"
									+ "	from registrationtesthistory  where ntransactiontestcode in(" + stransactiontestcode + ") and"
									+ " nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
									+ " and nsitecode=" + userInfo.getNtranssitecode() 
									+ " group by npreregno,ntransactionsamplecode,ntransactiontestcode)"
									+ " and rh.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
									+ " and ts.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
									+ " and rh.nsitecode=" + userInfo.getNtranssitecode() + "";
		

		final List<RegistrationTestHistory> cancelValidation = jdbcTemplate.query(transValidation,
				new RegistrationTestHistory());
		// ALPD-1969

		final List<TransactionValidation> lstTranscancelValidation = transactionDAOSupport.getTransactionValidation(ncontrolcode, userInfo,
				nregTypeCode, nregSubTypeCode);

		String stransactiontestcode1 = stringUtilityFunction.fnDynamicListToString(cancelValidation, "getNtransactiontestcode");
		// ALPD-1969

		final List<RegistrationTestHistory> lstValidation = cancelValidation.stream()
				.filter(source -> lstTranscancelValidation.stream()
						.noneMatch(dest -> source.getNtransactionstatus() == dest.getNtransactionstatus()))
				.collect(Collectors.toList());
		if (cancelValidation.size() == 0) {
			// stransactiontestcode1 = stransactiontestcode;
			// If transactionvalidation table record not available, need to validate test
			// cancel until completed
			// Below query to fetch test records that are not yet completed
			String query = " select ntransactiontestcode, ntransactionstatus from registrationtesthistory rh  "
							+ " where  rh.ntesthistorycode =any(select max(ntesthistorycode) from  "
							+ " registrationtesthistory  where ntransactiontestcode = rh.ntransactiontestcode "
							+ " and nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and nsitecode="	+ userInfo.getNtranssitecode()
							+ ") and rh. ntransactiontestcode in ( " + stransactiontestcode + ") "
							+ " and rh.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
							+ " and rh.ntransactionstatus < "+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() 
							+ " and rh.nsitecode="	+ userInfo.getNtranssitecode();

			final List<RegistrationTestHistory> validTestList = jdbcTemplate.query(query,
					new RegistrationTestHistory());

			stransactiontestcode1 = stringUtilityFunction.fnDynamicListToString(validTestList, "getNtransactiontestcode");
		}
		if (stransactiontestcode1.length() > 0) {
			String[] preregnoArray = strNpreregno.split(",");
			List<Integer> newList = Arrays.asList(preregnoArray).stream().map(s -> Integer.parseInt(s))
					.collect(Collectors.toList());

			/**
			 * Commented below query by L.Subashini on 28/04/2022 to add regtypecode and
			 * regsubtypecode in transactionvalidation
			 */
//			String sql = "select max(rth.ntesthistorycode) as reghistrycode from registrationtesthistory rth,"
//					+ "transactionValidation tv where tv.ntransactionstatus=rth.ntransactionstatus and "
//					+ " rth.ntransactiontestcode in(" + stransactiontestcode1 + ") GROUP BY rth.ntransactiontestcode";

			/**
			 * Commented above and added below query by L.Subashini on 28/04/2022 to add
			 * regtypecode and regsubtypecode in transactionvalidation
			 */
			String sql = " select max(rth.ntesthistorycode) as reghistrycode,max(rth.ntransactionstatus)ntransactionstatus"
						+ " from registrationtesthistory rth,registration r,"
						+ " transactionValidation tv where rth.npreregno = r.npreregno and "
						+ " r.nregtypecode = tv.nregtypecode and r.nregsubtypecode=tv.nregsubtypecode "
						+ " and rth.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and r.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and tv.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and rth.nsitecode="	+ userInfo.getNtranssitecode()
						+ " and r.nsitecode="	+ userInfo.getNtranssitecode()
						+ " and tv.nsitecode="	+ userInfo.getNmastersitecode()
						+ " and tv.ntransactionstatus=rth.ntransactionstatus and " 
						+ " rth.ntransactiontestcode in ("+ stransactiontestcode1 + ") GROUP BY rth.ntransactiontestcode ";

			final List<RegistrationTestHistory> lstSeqNoNew = jdbcTemplate.query(sql, new RegistrationTestHistory());
			// ALPD-1969 begin

			final List<TransactionValidation> lstCancelValidation = lstTranscancelValidation.stream()
					.filter(source -> lstSeqNoNew.stream()
							.noneMatch(dest -> source.getNtransactionstatus() == dest.getNtransactionstatus()))
					.collect(Collectors.toList());

			if (lstValidation.size() > 0) {

				final String sDisplaystatus = lstTranscancelValidation.stream()
						.map(objtransactionstatus -> String.valueOf(objtransactionstatus.getStransdisplaystatus()))
						.collect(Collectors.joining(","));
				String sAlert = commonFunction.getMultilingualMessage("IDS_SELECT", userInfo.getSlanguagefilename())
						+ " " + sDisplaystatus + " "
						+ commonFunction.getMultilingualMessage("IDS_TEST", userInfo.getSlanguagefilename());
				returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), sAlert);

			} else {
				// ALPD-1969 end
				int lstSeqNo01 = lstSeqNoNew.size();
				String samplecount = lstSeqNo01 + "";
				String[] strArray = samplecount.split(",");
				final List<String> listSeqno = Arrays.asList(strArray);
				newList.removeAll(listSeqno);

				int lstTest = mapSeqno.get("registrationtesthistory");
				int lstHis = mapSeqno.get("registrationhistory");

				final String querys = "select * from registrationhistory where npreregno in (" + strNpreregno + ") "
									+ " and nsitecode = " + userInfo.getNtranssitecode()
									+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and nreghistorycode in (select max(nreghistorycode) from registrationhistory where"
									+ " npreregno in (" + strNpreregno + ") "
									+ " and nsitecode = " + userInfo.getNtranssitecode()
									+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " group by npreregno)";

				final List<RegistrationHistory> lstRegHis = jdbcTemplate.query(querys, new RegistrationHistory());

				final List<RegistrationHistory> partialList = lstRegHis.stream().filter(x -> x
						.getNtransactionstatus() == Enumeration.TransactionStatus.PARTIALLY.gettransactionstatus())
						.collect(Collectors.toList());
				String StrUpdate = "";
				if (!partialList.isEmpty()) {

					StrUpdate = "update seqnoregistration set nsequenceno= " + (lstHis + partialList.size())
							+ " where stablename='registrationhistory';";
				}

				int seqNo = newList.size();
				
				StrUpdate = StrUpdate + "update seqnoregistration set nsequenceno= " + (lstTest + lstSeqNo01)
						+ " where stablename='registrationtesthistory';";
				jdbcTemplate.execute(StrUpdate);
				
				returnMap.put("sequencenumber", lstTest);
				returnMap.put("regHisSeqno", lstHis);
				// ALPD-1969 begin
				returnMap.put("nregsubtypecode", (int) inputMap.get("nregsubtypecode"));
				returnMap.put("nregtypecode", (int) inputMap.get("nregtypecode"));
				returnMap.put("ncontrolcode", (int) inputMap.get("ncontrolcode"));
				// ALPD-1969 end
				returnMap.put("testcode", stransactiontestcode1);
				returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
			}

		} else {
			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), "InvalidTest");
		}

		return returnMap;
	}
	
	@Override
	@SuppressWarnings({ "unused", "unchecked" })
	public Map<String, Object> cancelTestSamples(Map<String, Object> inputMap) throws Exception {
		
		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONArray actionTypeTestArray = new JSONArray();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditOld = new JSONObject();
		JSONObject jsonAuditNew = new JSONObject();
		JSONArray jsonTestArrayOld = new JSONArray();
		JSONArray jsonTestArraynew = new JSONArray();

		final ObjectMapper registrationMapper = new ObjectMapper();
		final String sQuery = " lock table  lockcancelreject " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus()
				+ "";
		jdbcTemplate.execute(sQuery);

		Map<String, Object> returnMap = new HashMap<String, Object>();
		Map<String, Object> returnMapold = new HashMap<String, Object>();

		final UserInfo userInfo = registrationMapper.convertValue(inputMap.get("userinfo"),
				new TypeReference<UserInfo>() {
				});
		// List<Map<String, Object>> lstDataTest = testAuditGet(inputMap, userInfo);
		returnMapold = (Map<String, Object>) getRegistrationTest(inputMap, userInfo).getBody();
		final StringBuilder sb = new StringBuilder();

		int nseqno = (int) inputMap.get("sequencenumber");
		String spreregno = (String) inputMap.get("ninsertpreregno");
		String stransactiontestcode = (String) inputMap.get("testcode") + "";
		// ALPD-1969 begin
		int ncontrolcode = (int) inputMap.get("ncontrolcode");
		int nregTypeCode = (int) inputMap.get("nregtypecode");
		int nregSubTypeCode = (int) inputMap.get("nregsubtypecode");
		// ALPD-1969 end
		int ntransactionstatus = 1;

		int ntransactionstatustest = 0;
		// ALPD-1969 begin
		String transValidation = "select  max(rh.ntransactionstatus) ntransactionstatus,"
								+ " max(rh.ntransactiontestcode) ntransactiontestcode,"
								+ "max(coalesce (ts.jsondata->'salertdisplaystatus'->>" + "'" + userInfo.getSlanguagetypecode()
								+ "',ts.jsondata->'salertdisplaystatus'->>'en-US')) as stransdisplaystatus" + " from "
								+ " transactionstatus ts,registrationtesthistory rh  " 
								+ " where  rh.ntransactionstatus=ts.ntranscode "
								+ " and rh.ntesthistorycode = any(select max(ntesthistorycode)"
								+ "	from registrationtesthistory  "
								+ " where ntransactiontestcode in(" + stransactiontestcode + ") "
								+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and nsitecode=" + userInfo.getNtranssitecode() + ")" 
								+ " and rh.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and ts.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and rh.nsitecode=" + userInfo.getNtranssitecode() + ""
								+ " group by npreregno,ntransactionsamplecode,ntransactiontestcode;";

		final List<RegistrationTestHistory> cancelValidation = jdbcTemplate.query(transValidation,
				new RegistrationTestHistory());

		final List<TransactionValidation> lstTranscancelValidation = transactionDAOSupport.getTransactionValidation(ncontrolcode, userInfo,
				nregTypeCode, nregSubTypeCode);

		final String stransactionTestCode1 = stringUtilityFunction.fnDynamicListToString(cancelValidation, "getNtransactiontestcode");

		final List<TransactionValidation> lstValidation = lstTranscancelValidation.stream()
				.filter(source -> cancelValidation.stream()
						.anyMatch(dest -> source.getNtransactionstatus() == dest.getNtransactionstatus()))
				.collect(Collectors.toList());

		if (lstValidation.size() == 0) {
			final String sDisplaystatus = lstTranscancelValidation.stream()
					.map(objtransactionstatus -> String.valueOf(objtransactionstatus.getStransdisplaystatus()))
					.collect(Collectors.joining(","));
			final String sAlert = commonFunction.getMultilingualMessage("IDS_SELECT", userInfo.getSlanguagefilename()) + " "
					+ sDisplaystatus + " "
					+ commonFunction.getMultilingualMessage("IDS_TEST", userInfo.getSlanguagefilename());
			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), sAlert);
		} else {
			// ALPD-1969 end

			final String insertReghistory = "insert into registrationtesthistory(ntesthistorycode,ntransactiontestcode,"
										+ "ntransactionsamplecode,npreregno,nformcode,ntransactionstatus,nusercode,nuserrolecode,"
										+ "ndeputyusercode,ndeputyuserrolecode,scomments,dtransactiondate,nsampleapprovalhistorycode,nsitecode,nstatus)"
										+ "select " + nseqno + "+rank()over(order by t.ntransactiontestcode) ntesthistorycode,"
										+ " t.ntransactiontestcode," + "" + " t.ntransactionsamplecode,t.npreregno,"
										+ userInfo.getNformcode() + " nformcode," + " case when ntransactionstatus="
										+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + " then "
										+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + " when ntransactionstatus="
										+ Enumeration.TransactionStatus.QUARENTINE.gettransactionstatus() + " then "
										+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + " else "
										+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + " end as ntransactionstatus,"
										+ userInfo.getNusercode() + " nusercode," + userInfo.getNuserrole() + " nuserrolecode," + ""
										+ userInfo.getNdeputyusercode() + " ndeputyusercode," + userInfo.getNdeputyuserrole()
										+ " ndeputyuserrolecode,N'" + stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "' scomments," + "'"
										+ dateUtilityFunction.getCurrentDateTime(userInfo)// objGeneral.getUTCDateTime()
										+ "' dtransactiondate,-1," + userInfo.getNtranssitecode() + ","
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
										+ " from registrationtest t,registrationtesthistory rh "
										+ " where t.ntransactiontestcode=rh.ntransactiontestcode"
										+ " and t.nsitecode="+ userInfo.getNtranssitecode()
										+ " and rh.nsitecode=" + userInfo.getNtranssitecode()
										+ " and rh.ntesthistorycode in (select max(ntesthistorycode) from registrationtesthistory rth "
										+ " where rth.ntransactiontestcode=t.ntransactiontestcode "
										+ " and rth.nsitecode =" + userInfo.getNtranssitecode() 
										+ "	and rth.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
										+ " and t.ntransactiontestcode in ("+ stransactiontestcode
										+ ") group by rth.npreregno,rth.ntransactionsamplecode,rth.ntransactiontestcode) "
										+ " and t.ntransactiontestcode in (" + stransactiontestcode + ") "
										+ " and t.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+" order by t.npreregno;";
			jdbcTemplate.execute(insertReghistory);

			// NTW
			/*
			 * final String query2 = "update sdmslabsheetmaster set nstatus = " +
			 * Enumeration.TransactionStatus.DELETED.gettransactionstatus() +
			 * " where ntransactiontestcode in (" + stransactiontestcode + ");" +
			 * "update sdmslabsheetdetails set nstatus = " +
			 * Enumeration.TransactionStatus.DELETED.gettransactionstatus() +
			 * " where ntransactiontestcode in (" + stransactiontestcode + ");";
			 */

			// jdbcTemplate.execute(query2);

			final String querys = "select * from registrationhistory where npreregno in (" + spreregno + ") "
					+ " and nreghistorycode in (select max(nreghistorycode) from registrationhistory where"
					+ " npreregno in (" + spreregno + ") "
					+ " and nsitecode=" + userInfo.getNtranssitecode()
					+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " group by npreregno) "
					+ " and nsitecode=" + userInfo.getNtranssitecode()
					+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final List<RegistrationHistory> lstHis = jdbcTemplate.query(querys, new RegistrationHistory());

			final List<RegistrationHistory> partialList = lstHis.stream().filter(
					x -> x.getNtransactionstatus() == Enumeration.TransactionStatus.PARTIALLY.gettransactionstatus())
					.collect(Collectors.toList());

			if (!partialList.isEmpty()) {
				int nRegHistoryCode = (int) inputMap.get("regHisSeqno") + 1;
				String sUpdateQuery = "";
				for (int i = 0; i < partialList.size(); i++) {

					final String sStatusGetQuery = "select ntransactionstatus, npreregno "
													+ " from registrationtesthistory where ntesthistorycode = any("
													+ " select max(ntesthistorycode) from registrationtesthistory "
													+ " where nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
													+ " and nsitecode= "+ userInfo.getNtranssitecode() 
													+ " and npreregno in (" + partialList.get(i).getNpreregno()
													+ ")" + " group by ntransactiontestcode)" + " and npreregno in ("
													+ partialList.get(i).getNpreregno() + ") and ntransactionstatus not in ("
													+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ", "
													+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ", "
													+ Enumeration.TransactionStatus.RETEST.gettransactionstatus() + ")" 
													+ " and nsitecode="	+ userInfo.getNtranssitecode() 
													+ " and nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
													+ " group by npreregno, ntransactionstatus";

					final List<RegistrationTest> listTestStatus = jdbcTemplate.query(sStatusGetQuery,
							new RegistrationTest());

					if (listTestStatus.size() == 1) {

						sUpdateQuery = sUpdateQuery +
//							"insert into registrationhistory (nreghistorycode, npreregno, ntransactionstatus, dtransactiondate, "
//							+ "nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, scomments, nstatus) values"
								" (" + nRegHistoryCode + ", " + listTestStatus.get(0).getNpreregno() + ", "
								+ listTestStatus.get(0).getNtransactionstatus() + "," + " '"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNusercode() + ", "
								+ userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode() + ", " + " "
								+ userInfo.getNdeputyuserrole() + ", N'"
								+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "', "
								+ userInfo.getNtranssitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
						nRegHistoryCode = nRegHistoryCode + 1;
						// inputMap.put("ntype", 1);
						inputMap.put("ntype", 3);
					}
				}

				if (sUpdateQuery.length() > 0) {
					String sUpdateQueryString = "insert into registrationhistory (nreghistorycode, npreregno, ntransactionstatus, dtransactiondate, "
							+ "nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, scomments, nsitecode, nstatus) values "
							+ sUpdateQuery.substring(0, sUpdateQuery.length() - 1) + ";";
					jdbcTemplate.execute(sUpdateQueryString);
				}
			}

			returnMap = (Map<String, Object>) getRegistrationTest(inputMap, userInfo).getBody();
			jsonAuditOld.put("registrationtest", (List<Map<String, Object>>) returnMapold.get("RegistrationGetTest"));
			jsonAuditNew.put("registrationtest", (List<Map<String, Object>>) returnMap.get("RegistrationGetTest"));
			((List<Map<String, Object>>) returnMapold.get("RegistrationGetTest")).forEach(t -> {
				if (Integer.parseInt(((Map<String, Object>) t).get("ntransactionstatus")
						.toString()) == Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus()) {
					actionTypeTestArray.put("IDS_REJECTTEST");
				} else {
					actionTypeTestArray.put("IDS_CANCELTEST");
				}
			});
			auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
			auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
			auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));

			actionType.put("registrationtest", actionTypeTestArray);
			
			auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, jsonAuditNew, actionType, auditmap, false, userInfo);
			
			// ALPD-1969
			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		}

		return returnMap;

	}
	
	@SuppressWarnings({ "unchecked" })
	@Override
	public Map<String, Object> rejectSample(Map<String, Object> inputMap) throws Exception {
		ObjectMapper RegistrationMapper = new ObjectMapper();

		String sQuery = "lock table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus() + ";";
		jdbcTemplate.execute(sQuery);

		sQuery = // sQuery.concat(
				" lock table  lockcancelreject " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();// + ";");
		jdbcTemplate.execute(sQuery);

		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		//JSONArray actionTypeTestArray = new JSONArray();
		JSONArray actionTypeSampleArray = new JSONArray();

	//	JSONArray actionTypeSubsampleArray = new JSONArray();
		JSONObject jsonAuditOld = new JSONObject();
		JSONObject jsonAuditNew = new JSONObject();
		Map<String, Object> returnMap = new HashMap<>();

		final UserInfo userInfo = RegistrationMapper.convertValue(inputMap.get("userinfo"), 
					new TypeReference<UserInfo>() {});
		
		//Commented by L.Subashini on 8/5/2025 as it is unused-start
		//final List<Map<String, Object>> lstDataTest = transactionDAOSupport.testAuditGet(inputMap, userInfo);
		//Commented by L.Subashini on 8/5/2025 as it is unused-end
		
		int nsampleseqno = (int) inputMap.get("registrationhistory");
		int nsubsampleseqno = (int) inputMap.get("registrationsamplehistory");
		int ntestseqno = (int) inputMap.get("registrationtesthistory");
		String spreregno = (String) inputMap.get("insertpreregno");
		
		//Commented by L.Subashini on 8/5/2025 as it is unused
//		ArrayList<Map<String, Object>> lstTestOld = (ArrayList<Map<String, Object>>) lstDataTest.stream().filter(
//				t -> !(t.get("ntransactionstatus").equals(Enumeration.TransactionStatus.REJECTED.gettransactionstatus())
//						|| t.get("ntransactionstatus")
//								.equals(Enumeration.TransactionStatus.CANCELED.gettransactionstatus())))
//				.collect(Collectors.toList());

		// need to check
//		final String Query = "select   rt.ntransactiontestcode"
//				+ " from registrationtest rt,registrationtesthistory rth where rt.npreregno in (" + spreregno
//				+ ") and rt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				+ " and rt.ntransactiontestcode=rth.ntransactiontestcode and rt.ntransactionsamplecode =rth.ntransactionsamplecode  "
//				+ " and rt.npreregno=rth.npreregno  "
//				+ " and rth.ntesthistorycode in (select max(ntesthistorycode) from registrationtesthistory rth1  "
//				+ " where rth1.ntransactiontestcode=rt.ntransactiontestcode and rth1.nstatus="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") "
//				+ " and rth.ntransactionstatus not in (" + Enumeration.TransactionStatus.CANCELED.gettransactionstatus()
//				+ "," + Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ")" + " and rt.npreregno in ("
//				+ spreregno + ")  " + " order by rt.ntransactiontestcode,rt.ntransactionsamplecode,rt.npreregno ";
//		List<RegistrationTest> lstRegistrationTestCancelbefore = jdbcTemplate.query(Query, new RegistrationTest());

		final String query3 =  "select r.npreregno,(r.jsondata->'nexternalordercode')::int as nexternalordercode,r.jsondata->>'externalorderid' as sexternalorderid,case when rh.ntransactionstatus ="
								+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + " then "
								+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + " when rh.ntransactionstatus="
								+ Enumeration.TransactionStatus.QUARENTINE.gettransactionstatus() + " then "
								+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + " else "
								+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + " end  as ntransactionstatus " 
								+ "from registration r,registrationhistory rh where r.npreregno in (" + spreregno + ") "
								+ " and r.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and rh.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and r.nsitecode = " + userInfo.getNtranssitecode()
								+ " and rh.nsitecode=" + userInfo.getNtranssitecode()
								+ " and rh.nreghistorycode in (select max(nreghistorycode) from registrationhistory rh1 where "
								+ " rh1.npreregno=r.npreregno and r.npreregno in (" + spreregno + ") "
								+ " and rh1.nsitecode= " + userInfo.getNtranssitecode()
								+ " and rh1.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " group by rh1.npreregno) and rh.npreregno in (" + spreregno
								+ ") and rh.ntransactionstatus not in ("
								+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ","
								+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ")" 
								+ " and (r.jsondata->>'nexternalordercode') IS NOT NULL "
								+ " AND (r.jsondata->>'nexternalordercode') != 'nexternalordercode' "				
								+ " order by npreregno;";
								
		final List<ExternalOrderTest> lstExternalOrderStatus = jdbcTemplate.query(query3, new ExternalOrderTest());				
//				if(lstexternalorderstatus.size()>0) {
//				insertExternalOrderStatus(userInfo,lstexternalorderstatus);}
		
		final String insertReghistory = "insert into registrationhistory (nreghistorycode,npreregno,ntransactionstatus,dtransactiondate,"
								+ "nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments,nsitecode, nstatus) "
								+ "select " + nsampleseqno
								+ "+rank()over(order by r.npreregno) nreghistorycode,r.npreregno,case when rh.ntransactionstatus ="
								+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + " then "
								+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + " when rh.ntransactionstatus="
								+ Enumeration.TransactionStatus.QUARENTINE.gettransactionstatus() + " then "
								+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + " else "
								+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + " end  as ntransactionstatus," + "'"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'  dtransactiondate," + userInfo.getNusercode() + " nusercode,"
								+ userInfo.getNuserrole() + " nuserrolecode," + "" + userInfo.getNdeputyusercode() + " ndeputyusercode,"
								+ userInfo.getNdeputyuserrole() + " ndeputyuserrolecode,'' scomments," + userInfo.getNtranssitecode()
								+ " nsitecode," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
								+ " from registration r,registrationhistory rh   "
								+ " where r.npreregno in (" + spreregno + ") "
								+ " and r.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and rh.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and r.nsitecode = " + userInfo.getNtranssitecode()
								+ " and rh.nsitecode=" + userInfo.getNtranssitecode()
								+ " and rh.nreghistorycode in (select max(nreghistorycode) from registrationhistory rh1 where "
								+ " rh1.npreregno=r.npreregno and r.npreregno in (" + spreregno + ") and rh1.nsitecode= "
								+ userInfo.getNtranssitecode() + " group by rh1.npreregno) and rh.npreregno in (" + spreregno
								+ ") and rh.ntransactionstatus not in ("
								+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ","
								+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ") order by npreregno;";
		jdbcTemplate.execute(insertReghistory);

		final String query1 = "insert into registrationsamplehistory(nsamplehistorycode,ntransactionsamplecode,"
							+ "npreregno,ntransactionstatus,dtransactiondate,nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,"
							+ "scomments,nsitecode,nstatus)" + "select " + nsubsampleseqno
							+ "+rank()over(order by rs.ntransactionsamplecode,rs.npreregno) nsamplehistorycode, rs.ntransactionsamplecode,"
							+ " rs.npreregno,case when rsh.ntransactionstatus ="
							+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + " then "
							+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + " else "
							+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + " end as ntransactionstatus,'"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' dtransactiondate," + userInfo.getNusercode() + " nusercode," + ""
							+ userInfo.getNuserrole() + " nuserrolecode," + userInfo.getNdeputyusercode() + " ndeputyusercode,"
							+ userInfo.getNdeputyuserrole() + " ndeputyuserrolecode," + "'' scomments,"
							+ userInfo.getNtranssitecode() + " nsitecode,"
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
							+ " from registrationsample rs,registrationsamplehistory rsh  where "
							+ " rsh.npreregno= rs.npreregno and rsh.ntransactionsamplecode=rs.ntransactionsamplecode "
							+ " and rs.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and rsh.nstatus =  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and rs.nsitecode = "+ userInfo.getNtranssitecode()
							+ " and rsh.nsitecode=" + userInfo.getNtranssitecode()
							+ " and rsh.nsamplehistorycode in (select max(nsamplehistorycode) "
							+ " from registrationsamplehistory rsh1 where  "
							+ " rsh1.npreregno=rs.npreregno and rs.npreregno in (" + spreregno + ")  "
							+ " and rsh1.nsitecode="+ userInfo.getNtranssitecode()
							+ " and rsh1.nstatus =  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " group by rsh1.npreregno,rsh1.ntransactionsamplecode ) and rsh.npreregno in (" + spreregno + ")  "
							+ " and rsh.ntransactionstatus not in (" + Enumeration.TransactionStatus.CANCELED.gettransactionstatus()
							+ "," + Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ")"
							+ " order by rs.npreregno,rs.ntransactionsamplecode ";
		jdbcTemplate.execute(query1);

		final String query2 = "insert into registrationtesthistory(ntesthistorycode,ntransactiontestcode,"
							+ "ntransactionsamplecode,npreregno,nformcode,ntransactionstatus,nusercode,nuserrolecode,"
							+ "ndeputyusercode,ndeputyuserrolecode,scomments,dtransactiondate,nsampleapprovalhistorycode,nsitecode,nstatus)"
							+ "select " + ntestseqno
							+ "+rank()over(order by  rt.ntransactiontestcode,rt.ntransactionsamplecode,rt.npreregno ) ntesthistorycode, rt.ntransactiontestcode,"
							+ " rt.ntransactionsamplecode," + " rt.npreregno," + userInfo.getNformcode() + " nformcode,"
							+ " case when rth.ntransactionstatus ="
							+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + " then "
							+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + " when rth.ntransactionstatus="
							+ Enumeration.TransactionStatus.QUARENTINE.gettransactionstatus() + " then "
							+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + " else "
							+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + " end  as ntransactionstatus,"
							+ userInfo.getNusercode() + " nusercode," + userInfo.getNuserrole() + " nuserrolecode," + ""
							+ userInfo.getNdeputyusercode() + " ndeputyusercode," + userInfo.getNdeputyuserrole()
							+ " ndeputyuserrolecode,N'CANCELED' scomments," + "'" + dateUtilityFunction.getCurrentDateTime(userInfo)
							+ "' dtransactiondate,-1," + userInfo.getNtranssitecode() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
							+ " from registrationtest rt,registrationtesthistory rth where rt.npreregno in (" + spreregno
							+ ") and rt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and rth.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and rt.ntransactiontestcode=rth.ntransactiontestcode "
							+ " and rt.ntransactionsamplecode =rth.ntransactionsamplecode  "
							+ " and rt.nsitecode = "+ userInfo.getNtranssitecode()
							+ " and rth.nsitecode=" + userInfo.getNtranssitecode()
							+ " and rt.npreregno=rth.npreregno  "
							+ " and rth.ntesthistorycode in (select max(ntesthistorycode) from registrationtesthistory rth1  "
							+ " where rth1.ntransactiontestcode=rt.ntransactiontestcode and rth1.nsitecode ="
							+ userInfo.getNtranssitecode() + " and rth1.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") "
							+ " and rth.ntransactionstatus not in (" + Enumeration.TransactionStatus.CANCELED.gettransactionstatus()
							+ "," + Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ") and rt.npreregno in ("
							+ spreregno + ") order by rt.ntransactiontestcode,rt.ntransactionsamplecode,rt.npreregno ";

		jdbcTemplate.execute(query2);		
		
		returnMap = registrationDAOSupport.getDynamicRegistration(inputMap, userInfo);
		
		jsonAuditOld.put("registration", (List<Map<String, Object>>) inputMap.get("selectedSample"));
		jsonAuditNew.put("registration", (List<Map<String, Object>>) returnMap.get("RegistrationGetSample"));

		((List<Map<String, Object>>) inputMap.get("selectedSample")).forEach(t -> {
			if (Integer.parseInt(((Map<String, Object>) t).get("ntransactionstatus")
					.toString()) == Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus()) {
				actionTypeSampleArray.put("IDS_REJECTSAMPLE");
			} else {
				actionTypeSampleArray.put("IDS_CANCELSAMPLE");
			}
		});


		auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
		auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
		auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));

		actionType.put("registration", actionTypeSampleArray);
		//actionType.put("registrationsample", actionTypeSubsampleArray);
		//actionType.put("registrationtest", actionTypeTestArray);
		auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, jsonAuditNew, actionType, auditmap, false, userInfo);

		final int nstatuscode = actionTypeSampleArray.get(0) == "IDS_REJECTSAMPLE"
								? Enumeration.TransactionStatus.REJECTED.gettransactionstatus()
								: Enumeration.TransactionStatus.CANCELED.gettransactionstatus();

		// send status to preventtb
		//sendStatustoPreventTb(inputMap, userInfo);
		
		
		final String query="select * from registration where npreregno in ("+spreregno+")"
							+ " and nsitecode="+ userInfo.getNtranssitecode() 
							+ " and nstatus=" +Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<Registration> regList = (List<Registration>) jdbcTemplate.query(query, new Registration());
		
		/**
		 * Added below logic to reset instrument calibration records on rejecting samples so that they
		 * can be preregistered again
		 */
		int nsampletypecode = (int) inputMap.get("nsampletypecode");
		
		if (nsampletypecode == Enumeration.SampleType.INSTRUMENT.getType()) {
			transactionDAOSupport.updateInstrumentCalibration(spreregno, userInfo);			
		}
	
		
		if((int) inputMap.get("nportalrequired") ==  Enumeration.TransactionStatus.YES.gettransactionstatus())
		{
			final String str1 = "select settings.ssettingvalue from settings where nsettingcode="
								+ Enumeration.Settings.SCHEDULER_BASED_STATUS_UPDATE_TO_PORTAL.getNsettingcode()
								+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final String ssettingvalue = jdbcTemplate.queryForObject(str1, String.class);
			
			if(ssettingvalue.equals("3"))
			{
				if(lstExternalOrderStatus.size()>0) {
					insertExternalOrderStatus(userInfo,lstExternalOrderStatus);				
				}
			}
			else
			{
				// Portal Status Update
				returnMap.putAll((Map<String, Object>) externalOrderSupport.sampleOrderUpdate(inputMap, spreregno, userInfo, nstatuscode,
						Enumeration.TransactionStatus.DRAFT.gettransactionstatus(), true).getBody());
			}
			if(regList.get(0).getJsonuidata().containsKey("nexternalordercode")) {
				final String externalordercode = regList.stream()
						.map(x -> String.valueOf(x.getJsonuidata().get("nexternalordercode"))).collect(Collectors.joining(","));
				String updatePreregno="update externalorder set npreregno="+Enumeration.TransactionStatus.NA.gettransactionstatus()+" "
						+ ",nsitecode="+userInfo.getNtranssitecode()+" where nexternalordercode in ("+externalordercode+")";
				jdbcTemplate.execute(updatePreregno);
			}

		}
		return returnMap;
	}
	
	/**
	 * Method updated to provide option to add subsample after register action
	 * L.Subashini 14/12/2022
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> validateSeqnoSubSampleNo(Map<String, Object> inputMap) throws Exception {

		Map<String, Object> returnMap = new HashMap<String, Object>();
		final ObjectMapper objMapper = new ObjectMapper();

		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		final List<TestGroupTest> listTest = objMapper.convertValue(inputMap.get("testgrouptest"),
				new TypeReference<List<TestGroupTest>>() {
				});

		Boolean skipMethodValidity = null;
		if (inputMap.containsKey("skipmethodvalidity")) {
			skipMethodValidity = (Boolean) inputMap.get("skipmethodvalidity");
		}

		final String sntestgrouptestcode = stringUtilityFunction.fnDynamicListToString(listTest, "getNtestgrouptestcode");

		List<TestGroupTest> expiredMethodTestList = new ArrayList<TestGroupTest>();
		if (skipMethodValidity == false && !listTest.isEmpty()) {
			expiredMethodTestList = transactionDAOSupport.getTestByExpiredMethod(sntestgrouptestcode, userInfo);

		}
		if (expiredMethodTestList.isEmpty()) {
			String sQuery = " lock  table lockpreregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			sQuery = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			sQuery = " lock  table lockcancelreject " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			String npreregno = (String) inputMap.get("npreregno");

			final boolean needJobAllocation = (boolean) inputMap.get("nneedjoballocation");

			final List<Map<String, Object>> subSampleCombinationUnique = (List<Map<String, Object>>) inputMap
					.get("subsamplecombinationunique");

			final Map<String, Object> map1 = projectDAOSupport.validateUniqueConstraint(subSampleCombinationUnique,
					(Map<String, Object>) inputMap.get("RegistrationSample"), userInfo, "create",
					RegistrationSample.class, "npreregno", false);
			
			if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
					.equals(map1.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
				return map1;
			}

			/**
			 * to get current status of selected samples
			 */
			final String squery = "select npreregno, ntransactionstatus from registrationhistory where"
								+ " nsitecode=" + userInfo.getNtranssitecode()
								+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and nreghistorycode in (select max(nreghistorycode) from registrationhistory r"
								+ " where r.npreregno in (" + npreregno + ") "
								+ " and r.nsitecode=" + userInfo.getNtranssitecode()
								+ " and r.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " group by r.npreregno)";
			List<RegistrationHistory> lstStatus = jdbcTemplate.query(squery, new RegistrationHistory());

			final List<RegistrationHistory> uncancelledUnRejectedList = lstStatus.stream().filter(
					x -> x.getNtransactionstatus() != Enumeration.TransactionStatus.CANCELED.gettransactionstatus()
							&& x.getNtransactionstatus() != Enumeration.TransactionStatus.REJECTED
									.gettransactionstatus()
							&& x.getNtransactionstatus() != Enumeration.TransactionStatus.RELEASED
									.gettransactionstatus())
					.collect(Collectors.toList());

			if (uncancelledUnRejectedList.isEmpty()) {
				returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						"IDS_SAMPLECANCELLEDREJECTEDRELEASED");
			} else {
				npreregno = stringUtilityFunction.fnDynamicListToString(uncancelledUnRejectedList, "getNpreregno");
				/**
				 * Commented below validation as there is requirement to add subsample after
				 * registration
				 */
				/**
				 * validation for sample at preregister status to add subsample
				 */
				// boolean check = lstStatus.stream().allMatch(
				// x -> x.getNtransactionstatus() ==
				// Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus());
				//
				// if (check) {
				List<TestGroupTest> TestGroupTests = objMapper.convertValue(inputMap.get("testgrouptest"),
						new TypeReference<List<TestGroupTest>>() {
						});

				int testCount = TestGroupTests.stream().mapToInt(
						testgrouptest -> testgrouptest.getNrepeatcountno() == 0 ? 1 : testgrouptest.getNrepeatcountno())
						.sum();

				int paramterCount = TestGroupTests.stream()
						.mapToInt(testgrouptest -> ((testgrouptest.getNrepeatcountno() == 0 ? 1
								: testgrouptest.getNrepeatcountno()) * testgrouptest.getNparametercount()))
						.sum();

				boolean isSampleNotPreRegStatus = uncancelledUnRejectedList.stream().anyMatch(x -> x
						.getNtransactionstatus() != Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus());
				String registeredSampleGetSeq = "";

				if (isSampleNotPreRegStatus) {
					registeredSampleGetSeq = ",'registrationhistory', 'registrationsection', 'registrationsectionhistory',"
							+ "'joballocation'";
					// + ",'llinter'";
				}

				final int nsampletypecode = (int) inputMap.get("nsampletypecode");				
				int orderType = Enumeration.OrderType.NA.getOrderType();
				
				final String s = "select * from registration where npreregno in (" + npreregno + ") "
						+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsitecode="+ userInfo.getNtranssitecode();
				final List<Registration> lstReg = jdbcTemplate.query(s, new Registration());

				if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()) {
					if (!lstReg.isEmpty()) {
						if (lstReg.get(0).getJsondata().containsKey("Order Type")) {
							
							Registration reg = lstReg.get(0);
							JSONObject json = new JSONObject(reg.getJsondata());
							orderType = (int) ((JSONObject) json.get("Order Type")).getInt("value");
							
							if (orderType == Enumeration.OrderType.INTERNAL.getOrderType() || orderType == Enumeration.OrderType.NA.getOrderType()) {
								registeredSampleGetSeq = registeredSampleGetSeq
										+ ",'externalordertest','externalordersample'";
							}

						}
					}
				}

				//// ALPD-2047
//				final List<String> lst = Arrays.asList("registrationparameter","registrationsample",
//						"registrationsamplehistory","registrationtest", "externalordersample", "externalordertest",
//						"registrationhistory","registrationsection", "registrationsectionhistory",
//						"joballocation", "registrationtesthistory", "resultparametercomments");
//				seqNoMaxUpdate(lst);				

				final String strSelectSeqno = "select stablename,nsequenceno from seqnoregistration  where stablename "
						+ " in ('registrationparameter','registrationsample',"
						+ "'registrationsamplehistory','registrationtest','registrationtesthistory','externalordersample','externalordertest' "
						+ registeredSampleGetSeq + ") and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " ;";

//				final List<?> lstMultiSeqNo = jdbcTemplate.query(strSelectSeqno, new SeqNoRegistration());
//
//				final List<SeqNoRegistration> lstSeqNoReg = (List<SeqNoRegistration>) lstMultiSeqNo.get(0);
//
//				returnMap = lstSeqNoReg.stream().collect(Collectors.toMap(SeqNoRegistration::getStablename,
//						SeqNoRegistration -> SeqNoRegistration.getNsequenceno()));
				
				final List<SeqNoRegistration> lstSeqNo = jdbcTemplate.query(strSelectSeqno, new SeqNoRegistration());
				
				returnMap = lstSeqNo.stream().collect(Collectors.toMap(SeqNoRegistration::getStablename,
						SeqNoRegistration -> SeqNoRegistration.getNsequenceno()));

				final List<String> listPreregNo = new ArrayList<String>(Arrays.asList(npreregno.split(",")));

				String strSeqnoUpdate = "Update seqnoregistration set nsequenceno = "
						+ ((int) returnMap.get("registrationparameter") + (paramterCount * listPreregNo.size()))
						+ " where stablename = 'registrationparameter';"
						+ "Update seqnoregistration set nsequenceno = "
						+ ((int) returnMap.get("registrationsample") + listPreregNo.size())
						+ " where stablename = 'registrationsample';" 
						+ "Update seqnoregistration set nsequenceno = "
						+ ((int) returnMap.get("registrationsamplehistory") + listPreregNo.size())
						+ " where stablename = 'registrationsamplehistory';"
						+ "Update seqnoregistration set nsequenceno = "
						+ ((int) returnMap.get("registrationtest") + (testCount * listPreregNo.size()))
						+ " where stablename = 'registrationtest' ;" 
						+ "Update seqnoregistration set nsequenceno = "
						+ ((int) returnMap.get("registrationtesthistory") + (testCount * listPreregNo.size()))
						+ " where stablename = 'registrationtesthistory';";

				if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()
						&& (orderType == 1 || orderType == -1)) {
					if (!lstReg.isEmpty()) {
						if (lstReg.get(0).getJsondata().containsKey("Order Type")) {
							strSeqnoUpdate = strSeqnoUpdate + "Update seqnoregistration set nsequenceno = "
									+ ((int) returnMap.get("externalordersample") + listPreregNo.size())
									+ " where stablename = 'externalordersample';"
									+ "Update seqnoregistration set nsequenceno = "
									+ ((int) returnMap.get("externalordertest") + TestGroupTests.size())
									+ " where stablename = 'externalordertest';";
						}
					}
				}
//	
				if (isSampleNotPreRegStatus) {
					/**
					 * sequence no. of registrationhistory has to be updated if the samples are not
					 * in quarantine/preregistered/ registered/partial
					 */

					final List<RegistrationHistory> sampleHistoryUpdateList = uncancelledUnRejectedList.stream()
							.filter(x -> x.getNtransactionstatus() != Enumeration.TransactionStatus.PREREGISTER
									.gettransactionstatus()
									&& x.getNtransactionstatus() != Enumeration.TransactionStatus.QUARENTINE
											.gettransactionstatus()
									&& x.getNtransactionstatus() != Enumeration.TransactionStatus.REGISTERED
											.gettransactionstatus()
									&& x.getNtransactionstatus() != Enumeration.TransactionStatus.PARTIALLY
											.gettransactionstatus())
							.collect(Collectors.toList());

					strSeqnoUpdate = strSeqnoUpdate + ";Update seqnoregistration set nsequenceno = "
							+ ((int) returnMap.get("registrationhistory") + sampleHistoryUpdateList.size())
							+ " where stablename = 'registrationhistory';";
					final List<RegistrationHistory> sampleWithoutPreregQuarantineStatus = uncancelledUnRejectedList
							.stream()
							.filter(x -> x.getNtransactionstatus() != Enumeration.TransactionStatus.PREREGISTER
									.gettransactionstatus()
									&& x.getNtransactionstatus() != Enumeration.TransactionStatus.QUARENTINE
											.gettransactionstatus())
							.collect(Collectors.toList());

					//////////////////////

					if (!sampleWithoutPreregQuarantineStatus.isEmpty()) {

						// Samples are available btw registered and final level of approval

						String preRegNoString = stringUtilityFunction.fnDynamicListToString(sampleWithoutPreregQuarantineStatus,
								"getNpreregno");
						final String sApprovalConfigQuery = "select acap.*, ra.npreregno, ra.sarno from "
								+ " registrationarno ra, approvalconfigautoapproval acap"
								+ " where acap.napprovalconfigversioncode = ra.napprovalversioncode "
								+ " and acap.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and ra.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and ra.nsitecode="+userInfo.getNtranssitecode()
								+ " and acap.nsitecode="+userInfo.getNmastersitecode()
								+ " and ra.npreregno in (" + preRegNoString + ")";
						final List<ApprovalConfigAutoapproval> approvalConfigAutoApprovals = jdbcTemplate
								.query(sApprovalConfigQuery, new ApprovalConfigAutoapproval());

						for (ApprovalConfigAutoapproval objApprovalConfigAutoapproval : approvalConfigAutoApprovals) {
							listPreregNo.add(String.valueOf(objApprovalConfigAutoapproval.getNpreregno()));
						}

						returnMap.put("approvalconfigautoapproval", approvalConfigAutoApprovals);

						final String subSampleQueryString = "select sarno.npreregno, "
															+ " count(sarno.ntransactionsamplecode) ntransactionsamplecode, "
															+ " rarno.sarno from registrationsamplearno sarno, "
															+ " registrationarno rarno,registrationsamplehistory rsh "
															+ " where rarno.npreregno in ( " + preRegNoString + ") "
															+ " and rarno.npreregno = sarno.npreregno "
															+ " and rsh.ntransactionsamplecode = sarno.ntransactionsamplecode "
															+ " and rsh.ntransactionstatus != "
															+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus()
															+ " and rsh.nsamplehistorycode =any (select max(nsamplehistorycode)"
															+ " from registrationsamplehistory "
															+ " where nsitecode=" + userInfo.getNtranssitecode() 
															+ " and nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
															+ " and npreregno in("+ preRegNoString + ")  "
															+ " group by ntransactionsamplecode) "
															+ " and rarno.nsitecode = "+ userInfo.getNtranssitecode()
															+ " and sarno.nsitecode = "	+ userInfo.getNtranssitecode()
															+ " and rarno.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
															+ " and sarno.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
															+ " group by sarno.npreregno, rarno.sarno ";

						List<RegistrationSample> sampleCountList = (List<RegistrationSample>) jdbcTemplate
								.query(subSampleQueryString, new RegistrationSample());
						// .stream().collect(Collectors.toMap(Integer.toString(RegistrationSample::getNpreregno),
						// registrationSample -> registrationSample.getNtransactionsamplecode()));

						returnMap.put("SubSampleCountList", sampleCountList);

						if (!listPreregNo.isEmpty() && !needJobAllocation) {
							strSeqnoUpdate = strSeqnoUpdate + "update seqnoregistration set nsequenceno = "
															+ ((int) returnMap.get("joballocation")
																	+ (sampleWithoutPreregQuarantineStatus.size() * testCount))
															+ " where stablename = 'joballocation';";
						}

						strSeqnoUpdate = strSeqnoUpdate + "update seqnoregistration set nsequenceno = "
														+ (int) returnMap.get("registrationparameter")
														+ (sampleWithoutPreregQuarantineStatus.size() * paramterCount)
														+ " where stablename = 'resultparametercomments';";

						if (!uncancelledUnRejectedList.isEmpty()) {
							Set<String> listSectionCode = TestGroupTests.stream()
									.map(obj -> String.valueOf(obj.getNsectioncode())).collect(Collectors.toSet());

							/**
							 * This query is used to find the count of new sections which is to be recorded
							 * in "registrationsection" table for the selected samples. If the input tests
							 * sections count matching with the existing section record count of the sample
							 * then it will return '0' for that preregno.If none of the sections is recorded
							 * for the preregno, then that record wont be returned by this query.
							 */

							final String ssectionCountQuery = " select npreregno, case when count(nsectioncode) = "
															+ listSectionCode.size() + " then 0 else " + listSectionCode.size()
															+ " - count(nsectioncode) end nsectioncode "
															+ " from registrationsection rs where npreregno in (" + npreregno + ")"
															+ " and nsectioncode in(" + String.join(",", listSectionCode) + ")"
															+ " and nsitecode = " + userInfo.getNtranssitecode() 
															+ " and nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
															+ " group by npreregno";

							final List<RegistrationSection> preregSectionList = jdbcTemplate.query(ssectionCountQuery,
									new RegistrationSection());

							// Sum of new sections count.
							final int sectionPreregCount = preregSectionList.stream()
									.mapToInt(regSection -> regSection.getNsectioncode()).sum();

							// Sample count for which the new sections are not available
							long unavailPreregSectionCount = listPreregNo.stream()
									.filter(item -> preregSectionList.stream()
											.noneMatch(item1 -> item1.getNpreregno() == Integer.parseInt(item)))
									.count();

							// Sum of both the above counts to be updated in sequence table for
							// registrationsection and its history.
							int preregCountToAdd = sectionPreregCount + (int) unavailPreregSectionCount;
							if (preregCountToAdd > 0) {
								strSeqnoUpdate = strSeqnoUpdate + "update seqnoregistration set nsequenceno = "
										+ (preregCountToAdd + (int) returnMap.get("registrationsection"))
										+ " where stablename = 'registrationsection';"
										+ "update seqnoregistration set nsequenceno = "
										+ (preregCountToAdd + (int) returnMap.get("registrationsectionhistory"))
										+ " where stablename = 'registrationsectionhistory';";
							}
						}
					}
				}
				jdbcTemplate.execute(strSeqnoUpdate);
				returnMap.put("ordertypecode", orderType);
				returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
				// } else {
				// returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
				// "IDS_SELECTPREREGISTERSAMPLE");
				// }
			}
		} else {
			final String expiredMethod = stringUtilityFunction.fnDynamicListToString(expiredMethodTestList, "getSmethodname");
			// outputMap.put(key, value)
			returnMap.put("NeedConfirmAlert", true);
			final String message = commonFunction.getMultilingualMessage("IDS_TESTWITHEXPIREDMETHODCONFIRM",
					userInfo.getSlanguagefilename());
			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), message.concat(" " + expiredMethod));
			// commonFunction.getMultilingualMessage("IDS_INACTIVETESTCANNOTBEREGISTERED",
			// userInfo.getSlanguagefilename()));
		}
		return returnMap;
	}
	
	public String createSubSampleWithFile(MultipartHttpServletRequest inputMap) throws Exception {
		
		final ObjectMapper objectMapper = new ObjectMapper();
		final UserInfo userInfo = objectMapper.readValue(inputMap.getParameter("userinfo"), new TypeReference<UserInfo>() {});
//		final Object reg = objectMapper.readValue(inputMap.getParameter("Map"), new TypeReference<Object>() {
//		});
		final String uploadStatus = ftpUtilityFunction.getFileFTPUpload(inputMap, -1, userInfo);

//		if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(uploadStatus)) {
//			return registrationServiceImpl.createSubSample((Map<String, Object>) reg);
//		} else {
//			return new ResponseEntity<>(
//					commonFunction.getMultilingualMessage(uploadStatus, userInfo.getSlanguagefilename()),
//					HttpStatus.EXPECTATION_FAILED);
//		}
		return uploadStatus;
	}
	
	@SuppressWarnings({ "unchecked" })
	@Override
	public Map<String, Object> createSubSample(Map<String, Object> inputMap) throws Exception {

		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObject = new JSONObject();

		final ObjectMapper objMap = new ObjectMapper();
		objMap.registerModule(new JavaTimeModule());
		int nsampletypecode = (int) inputMap.get("nsampletypecode");

		final UserInfo userInfo = objMap.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		String npreregno = (String) inputMap.get("npreregno");
		final String squery = "select npreregno, ntransactionstatus from registrationhistory where "
							+ " nsitecode=" + userInfo.getNtranssitecode()
							+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and nreghistorycode in (select max(nreghistorycode) from registrationhistory r"
							+ " where r.npreregno in (" + npreregno + ") " 
							+ " and nsitecode=" + userInfo.getNtranssitecode()
							+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " group by r.npreregno)";
		final List<RegistrationHistory> lstStatus = jdbcTemplate.query(squery, new RegistrationHistory());

		final List<RegistrationHistory> uncancelledUnRejectedList = lstStatus.stream()
				.filter(x -> x.getNtransactionstatus() != Enumeration.TransactionStatus.CANCELED.gettransactionstatus()
						&& x.getNtransactionstatus() != Enumeration.TransactionStatus.REJECTED.gettransactionstatus()
						&& x.getNtransactionstatus() != Enumeration.TransactionStatus.RELEASED.gettransactionstatus())
				.collect(Collectors.toList());

		Map<String, Object> returnMap = new HashMap<String, Object>();
		if (uncancelledUnRejectedList.isEmpty()) {
			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					"IDS_SAMPLECANCELLEDREJECTEDRELEASED");
			return returnMap;
		} else {
			npreregno = stringUtilityFunction.fnDynamicListToString(uncancelledUnRejectedList, "getNpreregno");

			List<String> lstPreregno1 = new ArrayList<String>(Arrays.asList(npreregno.split(",")));
			List<Integer> lstPreregno = lstPreregno1.stream().map(x -> Integer.parseInt(x))
					.collect(Collectors.toList());

			final RegistrationSample registrationSample = objMap.convertValue(inputMap.get("RegistrationSample"),
					new TypeReference<RegistrationSample>() {
					});

			if (inputMap.containsKey("specBasedComponent")) {
				if ((boolean) inputMap.get("specBasedComponent") == false) {
					final String query = "select nspecsampletypecode from testgroupspecsampletype where nallottedspeccode="
							+ registrationSample.getNallottedspeccode();

					Integer nspecsampletypecode = jdbcTemplate.queryForObject(query, Integer.class);
					registrationSample.setNspecsampletypecode(nspecsampletypecode);
				}
			}

			List<TestGroupTest> testGroupTestList = objMap.convertValue(inputMap.get("testgrouptest"),
					new TypeReference<List<TestGroupTest>>() {
					});

			List<String> SubSampledateList = objMap.convertValue(inputMap.get("subsampleDateList"),
					new TypeReference<List<String>>() {
					});

			int nregsamplecode = (int) inputMap.get("registrationsample");
			int nregsamplehistorycode = (int) inputMap.get("registrationsamplehistory");
			int seqordersample = -1;
			// int seqordertest = -1;

			if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()) {
				if (registrationSample.getJsondata().containsKey("sampleorderid")) {
					int orderType = (int) inputMap.get("ordertypecode");
					if (orderType == 1 || orderType == -1) {
						seqordersample = (int) inputMap.get("externalordersample");
						// seqordertest = (int) inputMap.get("externalordertest");
					}
				}
			}


			inputMap.put("nsitecode", userInfo.getNtranssitecode());

			boolean isSampleNotPreRegStatus = uncancelledUnRejectedList.stream().anyMatch(
					x -> x.getNtransactionstatus() != Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus());

			if (isSampleNotPreRegStatus) {
				int nreghistorycode = (int) inputMap.get("registrationhistory");
				/**
				 * To insert main sample status
				 */
				final List<RegistrationHistory> sampleHistoryUpdateList = uncancelledUnRejectedList.stream()
						.filter(x -> x.getNtransactionstatus() != Enumeration.TransactionStatus.PREREGISTER
								.gettransactionstatus()
								&& x.getNtransactionstatus() != Enumeration.TransactionStatus.QUARENTINE
										.gettransactionstatus()
								&& x.getNtransactionstatus() != Enumeration.TransactionStatus.REGISTERED
										.gettransactionstatus()
								&& x.getNtransactionstatus() != Enumeration.TransactionStatus.PARTIALLY
										.gettransactionstatus())
						.collect(Collectors.toList());
				List<String> registrationHistoryList = new ArrayList<String>();

				for (RegistrationHistory history : sampleHistoryUpdateList) {
					nreghistorycode++;
					registrationHistoryList.add("(" + nreghistorycode + "," + history.getNpreregno() + ","
							+ Enumeration.TransactionStatus.PARTIALLY.gettransactionstatus() + ", '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNusercode() + ","
							+ userInfo.getNuserrole() + "," + userInfo.getNdeputyusercode() + ","
							+ userInfo.getNdeputyuserrole() + ",'" + stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "',"
							+ userInfo.getNtranssitecode() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode()
							+ ")");

				}

				if (registrationHistoryList.size() > 0) {
					String strRegistrationHistory = " insert into registrationhistory (nreghistorycode,npreregno,ntransactionstatus,"
							+ " dtransactiondate,nusercode,nuserrolecode "
							+ " ,ndeputyusercode,ndeputyuserrolecode,scomments,nsitecode,nstatus,noffsetdtransactiondate,"
							+ " ntransdatetimezonecode) values";
					// jdbcTemplate.execute(strRegistrationHistory);
					jdbcTemplate.execute(strRegistrationHistory + String.join(",", registrationHistoryList));

				}
			}

			// String sQuery="";
			String strRegistrationSample = "";
			String strRegistrationSampleHistory = "";
			String strRegistrationSampleArno = "";
			String ntransactionsamplecode = "";
			String externalOrderSampleQuery = "";
			//String externalOrderTestQuery = "";

			int sampleCount = lstPreregno.size();


			/**
			 * Start of sample iteration
			 */
			for (int i = 0; i < sampleCount; i++) {
				nregsamplecode++;
				nregsamplehistorycode++;
				seqordersample++;

				JSONObject jsoneditObj = new JSONObject(registrationSample.getJsondata());
				JSONObject jsonuiObj = new JSONObject(registrationSample.getJsonuidata());
				if (!SubSampledateList.isEmpty()) {
					jsoneditObj = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsoneditObj, SubSampledateList, false,
							userInfo);
					jsonuiObj = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonuiObj, SubSampledateList, false, userInfo);

				}

				jsonuiObj.put("ntransactionsamplecode", nregsamplecode);
				jsonuiObj.put("npreregno", lstPreregno.get(i));
				jsonuiObj.put("nspecsampletypecode", registrationSample.getNspecsampletypecode());
				jsonuiObj.put("ncomponentcode", registrationSample.getNcomponentcode());

				strRegistrationSample = strRegistrationSample + " (" + nregsamplecode + "," + lstPreregno.get(i) + ",'"
						+ registrationSample.getNspecsampletypecode() + "'," + registrationSample.getNcomponentcode()
						+ ",'" + stringUtilityFunction.replaceQuote(jsoneditObj.toString()) + "'::JSONB,'"
						+ stringUtilityFunction.replaceQuote(jsonuiObj.toString()) + "'::JSONB," + userInfo.getNtranssitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";

				if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()) {
					if (registrationSample.getJsondata().containsKey("sampleorderid")) {
						int orderType = (int) inputMap.get("ordertypecode");

						if (orderType == Enumeration.OrderType.INTERNAL.getOrderType() || orderType == Enumeration.OrderType.NA.getOrderType()) {

							if (inputMap.get("order") != null)
							{
								int externalordercode = Integer.parseInt((String) inputMap.get("order"));
								int nsampleAppearanceCode = -1;
								if (jsonuiObj.has("nsampleappearancecode")) {
									nsampleAppearanceCode = (int) jsonuiObj.get("nsampleappearancecode");
								}
								externalOrderSampleQuery += " (" + seqordersample + "," + externalordercode + ","
										+ registrationSample.getNcomponentcode() + "," + 0 + "," + -1 + ",'"
										+ stringUtilityFunction.replaceQuote(jsonuiObj.get("sampleorderid").toString()) + "','"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + "" + userInfo.getNtranssitecode() + ", "
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
										+ userInfo.getNtranssitecode() + "," + nregsamplecode + ", '"
										+ stringUtilityFunction.replaceQuote(jsonuiObj.get("subsamplecollectiondatetime").toString()) + "', "
										+ nsampleAppearanceCode + "),";
							}

						}

					}
				}

				final int mainSampleNo = lstPreregno.get(i);
				int mainSampleStatus = uncancelledUnRejectedList.stream()
						.filter(item -> item.getNpreregno() == mainSampleNo).collect(Collectors.toList()).get(0)
						.getNtransactionstatus();

				int subSampleStatus = mainSampleStatus;
				// ALPD-1127
				if (mainSampleStatus == Enumeration.TransactionStatus.QUARENTINE.gettransactionstatus()) {
					subSampleStatus = Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus();
				}
				// ALPD-1127
				if (mainSampleStatus != Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus()
						&& mainSampleStatus != Enumeration.TransactionStatus.QUARENTINE.gettransactionstatus()
						&& mainSampleStatus != Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()) {
					mainSampleStatus = Enumeration.TransactionStatus.PARTIALLY.gettransactionstatus();
					subSampleStatus = Enumeration.TransactionStatus.REGISTERED.gettransactionstatus();
				}
				strRegistrationSampleHistory = strRegistrationSampleHistory + "(" + nregsamplehistorycode + ","
						+ nregsamplecode + "," + lstPreregno.get(i) + "," + subSampleStatus + ",'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNusercode() + "," + userInfo.getNuserrole()
						+ "," + userInfo.getNdeputyusercode() + "," + userInfo.getNdeputyuserrole() + ",'"
						+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "'," + userInfo.getNtranssitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";

				String subSampleARNO = "-";
				if (mainSampleStatus != Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus()
						&& mainSampleStatus != Enumeration.TransactionStatus.QUARENTINE.gettransactionstatus()) {
					final RegistrationSample subSampleDetail = ((List<RegistrationSample>) inputMap
							.get("SubSampleCountList")).stream().filter(item -> item.getNpreregno() == mainSampleNo)
									.collect(Collectors.toList()).get(0);

					String formatted = String.format("%02d", subSampleDetail.getNtransactionsamplecode() + 1);
					subSampleARNO = "" + subSampleDetail.getSarno() + "-" + formatted;// subsample last record to be
																						// fetched and replaced here

				}
				strRegistrationSampleArno = strRegistrationSampleArno + "(" + nregsamplecode + "," + lstPreregno.get(i)
						+ ",'" + subSampleARNO + "'," + userInfo.getNtranssitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
		
				/// commented till above
				StringJoiner joinerSampleMain = new StringJoiner(",");
				joinerSampleMain.add(String.valueOf(nregsamplecode));

				ntransactionsamplecode = ntransactionsamplecode + "," + String.valueOf(nregsamplecode);
			}
			// end of sample iteration

			strRegistrationSample = "insert into registrationsample(ntransactionsamplecode,npreregno,nspecsampletypecode,ncomponentcode,jsondata,jsonuidata, "
					+ "nsitecode,nstatus) values "
					+ strRegistrationSample.substring(0, strRegistrationSample.length() - 1) + ";";
			jdbcTemplate.execute(strRegistrationSample);

			strRegistrationSampleHistory = "Insert into registrationsamplehistory(nsamplehistorycode, ntransactionsamplecode, npreregno, ntransactionstatus, "
					+ "dtransactiondate, nusercode, nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments, nsitecode,nstatus ) values "
					+ strRegistrationSampleHistory.substring(0, strRegistrationSampleHistory.length() - 1) + ";";
			jdbcTemplate.execute(strRegistrationSampleHistory);

			strRegistrationSampleArno = "Insert into registrationsamplearno (ntransactionsamplecode,npreregno,ssamplearno,nsitecode,nstatus) values "
					+ strRegistrationSampleArno.substring(0, strRegistrationSampleArno.length() - 1) + ";";
			jdbcTemplate.execute(strRegistrationSampleArno);

			if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()) {
				if (registrationSample.getJsondata().containsKey("sampleorderid")) {
					int orderType = (int) inputMap.get("ordertypecode");

					if ((orderType == Enumeration.OrderType.INTERNAL.getOrderType() 
							|| orderType == Enumeration.OrderType.NA.getOrderType()) 
							&& externalOrderSampleQuery.trim().length() > 0) {
						externalOrderSampleQuery = "INSERT INTO externalordersample (nexternalordersamplecode,nexternalordercode,ncomponentcode,nsampleqty,nunitcode,sexternalsampleid,dmodifieddate,nsitecode,nstatus, nparentsitecode,ntransactionsamplecode, dsamplecollectiondatetime, nsampleappearancecode) values "
								+ externalOrderSampleQuery.substring(0, externalOrderSampleQuery.length() - 1) + ";";
						jdbcTemplate.execute(externalOrderSampleQuery);
//						externalOrderTestQuery = "INSERT INTO externalordertest (nexternalordertestcode,nexternalordersamplecode,nexternalordercode,ntestpackagecode,ncontainertypecode,ntestcode,dmodifieddate,nsitecode,nstatus, nparentsitecode) values "
//								+ externalOrderTestQuery.substring(0, externalOrderTestQuery.length() - 1) + ";";
//
//						jdbcTemplate.execute(externalOrderTestQuery);
					}

				}
			}

			// Add Test code start

			final int nregTypeCode = (int) inputMap.get("nregtypecode");
			final int nregSubTypeCode = (int) inputMap.get("nregsubtypecode");

			final String sFindSubSampleQuery = "select npreregno, ntransactionstatus, ntransactionsamplecode "
											+ " from registrationsamplehistory "
											+ " where nsamplehistorycode = any (select max(nsamplehistorycode) "
											+ " from registrationsamplehistory where ntransactionsamplecode"
											+ " in (" + ntransactionsamplecode.substring(1) + ")  "
											+ " and nsitecode=" + userInfo.getNtranssitecode()
											+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " group by ntransactionsamplecode) "
											+ " and ntransactionstatus not in ("
											+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ", "
											+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ") "
											+ " and nsitecode="	+ userInfo.getNtranssitecode() 
											+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ ";";

			final List<RegistrationSampleHistory> listAvailableSample = jdbcTemplate.query(sFindSubSampleQuery,
					new RegistrationSampleHistory());

			inputMap.put("directAddTest", false);
			inputMap.put("AvailableSample", listAvailableSample);

			inputMap.put("ntransactionsamplecode", ntransactionsamplecode.substring(1));
			ntransactionsamplecode = ntransactionsamplecode.substring(1);
			
			final Map<String, Object> createdTestMap = (Map<String, Object>) createTest(userInfo,
					Arrays.asList(ntransactionsamplecode), testGroupTestList, nregTypeCode, nregSubTypeCode, inputMap)
							.getBody();

			// End of add Test code

			inputMap.put("ntransactiontestcode", createdTestMap.get("ntransactiontestcode"));


			returnMap.putAll((Map<String, Object>) getRegistrationSubSample(inputMap, userInfo).getBody());
			returnMap.putAll((Map<String, Object>) getRegistrationSampleAfterSubsampleAdd(inputMap, userInfo));
			jsonAuditObject.put("registrationsample", (List<Map<String, Object>>) returnMap.get("selectedSubSample"));
			//commenting code for auditchild issue
			//jsonAuditObject.put("registrationtest", (List<Map<String, Object>>) returnMap.get("RegistrationGetTest"));
			auditmap.put("nregtypecode",
					((Map<String, Object>) ((Map<String, Object>) inputMap.get("masterData")).get("RegTypeValue"))
							.get("nregtypecode"));
			auditmap.put("nregsubtypecode",
					((Map<String, Object>) ((Map<String, Object>) inputMap.get("masterData")).get("RegSubTypeValue"))
							.get("nregsubtypecode"));
			auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
			actionType.put("registrationsample", "IDS_ADDSUBSAMPLE");


			auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);

			return returnMap;
		}

	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getEditRegistrationSubSampleDetails(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();
		if (inputMap.get("ntransactionsamplecode") == null || inputMap.get("ntransactionsamplecode") == "") {

			outputMap.put("ReturnStatus",
					commonFunction.getMultilingualMessage("IDS_SELECTSUBSAMPLE", userInfo.getSlanguagefilename()));
			return new ResponseEntity<>(outputMap, HttpStatus.EXPECTATION_FAILED);
		} else {
			final String query = "  select json_agg(r.jsonuidata||r.jsondata||json_build_object('ntransactionstatus',rh.ntransactionstatus,"
								+ "'stransdisplaystatus', ts.jsondata->'" + userInfo.getSlanguagetypecode()
								+ "'->>'stransdisplaystatus'" + ")::jsonb) "
								+ "		from registrationsample r,registrationsamplehistory rh, "
								+ "	(select max(rh.nsamplehistorycode) as nsamplehistorycode from registrationsamplehistory rh"
								+ "	where rh.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ "	 and rh.ntransactionsamplecode in (" + inputMap.get("ntransactionsamplecode")
								+ ") group by rh.npreregno,ntransactionsamplecode )tableA,transactionstatus ts,component c"
								+ "	where c.ncomponentcode=r.ncomponentcode and  r.npreregno=rh.npreregno and rh.ntransactionsamplecode=r.ntransactionsamplecode "
								+ "	and ts.ntranscode = rh.ntransactionstatus "
								+ " and ts.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and r.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and rh.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and r.nsitecode="+ userInfo.getNtranssitecode()
								+ " and rh.nsitecode = " + userInfo.getNtranssitecode()
								+ " and tableA.nsamplehistorycode =rh.nsamplehistorycode and rh.ntransactionsamplecode in ("
								+ inputMap.get("ntransactionsamplecode") + ") ";

			final String dynamicList = jdbcTemplate.queryForObject(query, String.class);

			final List<Map<String, Object>> lstData = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(
					dynamicList, userInfo, true, (int) inputMap.get("ndesigntemplatemappingcode"), "subsample");
			outputMap.put("EditData", lstData.get(0));

			if (inputMap.containsKey("getSubSampleChildDetail")
					&& (Boolean) inputMap.get("getSubSampleChildDetail") == true) {
				final Map<String, Object> getMapData = new HashMap<String, Object>();
				getMapData.put("nsampletypecode", inputMap.get("nsampletypecode"));
				getMapData.put("nregtypecode", inputMap.get("nregtypecode"));
				getMapData.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
				getMapData.put("npreregno", Integer.toString((Integer) (inputMap.get("npreregno"))));
				getMapData.put("ntransactionstatus", inputMap.get("nfilterstatus"));
				getMapData.put("napprovalconfigcode", inputMap.get("napprovalconfigcode"));
				getMapData.put("activeTestTab", inputMap.get("activeTestTab"));
				getMapData.put("activeSampleTab", inputMap.get("activeSampleTab"));
				getMapData.put("activeSubSampleTab", inputMap.get("activeSubSampleTab"));
				getMapData.put("nneedsubsample", inputMap.get("nneedsubsample"));
				getMapData.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
				getMapData.put("checkBoxOperation", inputMap.get("checkBoxOperation"));
				getMapData.put("userinfo", userInfo);
				getMapData.put("ntransactionsamplecode",
						Integer.toString((Integer) inputMap.get("ntransactionsamplecode")));

				final Map<String, Object> childDetailMap = new HashMap<String, Object>();

				childDetailMap.putAll((Map<String, Object>) getRegistrationTest(getMapData, userInfo).getBody());
				outputMap.put("SubSampleChildDetail", childDetailMap);
			}

			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}		
		
	}
	
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> updateRegistrationSubSampleWithFile(MultipartHttpServletRequest inputMap)
			throws Exception {
		
		final ObjectMapper objectMapper = new ObjectMapper();
		final UserInfo userInfo = objectMapper.readValue(inputMap.getParameter("userinfo"), new TypeReference<UserInfo>() {});
		final Object regObj = objectMapper.readValue(inputMap.getParameter("Map"), new TypeReference<Object>() {});
		final String uploadStatus = ftpUtilityFunction.getFileFTPUpload(inputMap, -1, userInfo);

		if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(uploadStatus)) {
			// returnMap.put("inputMap", inputMap.getParameter("Map"));
			return (ResponseEntity<Object>) updateRegistrationSubSample((Map<String, Object>) regObj);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(uploadStatus, userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}
	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> updateRegistrationSubSample(final Map<String, Object> inputMap) throws Exception {
		
		Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		Map<String, Object> returnOldMap = new LinkedHashMap<String, Object>();
		JSONObject jsonAuditOld = new JSONObject();
		JSONObject jsonAuditNew = new JSONObject();
		
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		Map<String, Object> returnMap = null;
		Map<String, Object> initialParam = (Map<String, Object>) inputMap.get("initialparam");
		
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		returnOldMap = (Map<String, Object>) getRegistrationSubSample(initialParam, userInfo).getBody();
		
		final String ntransactionsamplecode = (String) initialParam.get("ntransactionsamplecode");
		final RegistrationSample reg = objMapper.convertValue(inputMap.get("registrationsample"),
				new TypeReference<RegistrationSample>() {
				});

		final List<String> dateList = objMapper.convertValue(inputMap.get("SubSampleDateList"),
				new TypeReference<List<String>>() {
				});

		List<Map<String, Object>> subSampleCombinationUnique = (List<Map<String, Object>>) inputMap
				.get("subsamplecombinationunique");

		final Map<String, Object> map1 = projectDAOSupport.validateUniqueConstraint(subSampleCombinationUnique,
				(Map<String, Object>) inputMap.get("registrationsample"), userInfo, "create", RegistrationSample.class,
				"npreregno", false);
		
		if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
				.equals(map1.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(map1.get("rtn").toString(), userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);

		}

		JSONObject jsoneditRegistration = new JSONObject(reg.getJsondata());
		JSONObject jsonuiRegistration = new JSONObject(reg.getJsonuidata());
		if (!dateList.isEmpty()) {
			jsoneditRegistration = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsoneditRegistration, dateList, false,
					userInfo);
			jsonuiRegistration = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonuiRegistration, dateList, false,
					userInfo);

		}

		String queryString = "update registrationsample set jsondata=jsondata||'"
									+ stringUtilityFunction.replaceQuote(jsoneditRegistration.toString()) + "'::jsonb,jsonuidata=jsonuidata||'"
									+ stringUtilityFunction.replaceQuote(jsonuiRegistration.toString()) + "'::jsonb  where ntransactionsamplecode in ("
									+ ntransactionsamplecode + ");";
		if (jsonuiRegistration.has("nordertypecode")
				&& jsonuiRegistration.get("nordertypecode").equals(Enumeration.OrderType.INTERNAL.getOrderType())
				&& initialParam.containsKey("nsampletypecode")
				&& initialParam.get("nsampletypecode").equals(Enumeration.SampleType.CLINICALSPEC.getType())) {
			int nsampleAppearanceCode = -1;
			if (jsonuiRegistration.has("nsampleappearancecode")) {
				nsampleAppearanceCode = (int) jsonuiRegistration.get("nsampleappearancecode");
			}
			queryString = queryString + "update externalordersample set sexternalsampleid='"
									+ stringUtilityFunction.replaceQuote(jsonuiRegistration.get("sampleorderid").toString())
									+ "', dsamplecollectiondatetime='"
									+ stringUtilityFunction.replaceQuote(jsonuiRegistration.get("subsamplecollectiondatetime").toString())
									+ "', nsampleappearancecode=" + nsampleAppearanceCode + " where ntransactionsamplecode in ("
									+ ntransactionsamplecode + ");";
			// ALPD-3575
		}
		jdbcTemplate.execute(queryString);

		returnMap = (Map<String, Object>) getRegistrationSubSample(initialParam, userInfo).getBody();
		objmap.put("nregtypecode", initialParam.get("nregtypecode"));
		objmap.put("nregsubtypecode", initialParam.get("nregsubtypecode"));
		objmap.put("ndesigntemplatemappingcode", initialParam.get("ndesigntemplatemappingcode"));
		actionType.put("registrationsample", "IDS_EDITSUBSAMPLE");
		jsonAuditOld.put("registrationsample", (List<Map<String, Object>>) returnOldMap.get("selectedSubSample"));
		jsonAuditNew.put("registrationsample", (List<Map<String, Object>>) returnMap.get("selectedSubSample"));
		
		auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, jsonAuditNew, actionType, objmap, false, userInfo);
		
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public Map<String, Object> seqNoSubSampleInsert(Map<String, Object> inputMap) throws Exception {

		final String sQuery = " lock  table lockcancelreject " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		//// ALPD-2047
//		final List<String> lst = Arrays.asList(
//				"registrationsamplehistory", "registrationtesthistory");
//		seqNoMaxUpdate(lst);

		final String strSelectSeqno = "select stablename,nsequenceno from seqnoregistration "
									// + Enumeration.ReturnStatus.TABLOCK.getreturnstatus()
									+ " where stablename in ('registrationsamplehistory','registrationtesthistory')"
									+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<SeqNoRegistration> lstSeqNoSampleRecord = jdbcTemplate.query(strSelectSeqno, new SeqNoRegistration());

		Map<String, Object> returnMap = null;
		returnMap = lstSeqNoSampleRecord.stream().collect(Collectors.toMap(SeqNoRegistration::getStablename,
				seqNoRegistration -> seqNoRegistration.getNsequenceno()));

		final ObjectMapper objMapper = new ObjectMapper();

		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});

		int nregTypeCode = (int) inputMap.get("nregtypecode");
		int nregSubTypeCode = (int) inputMap.get("nregsubtypecode");

		int ncontrolcode = (int) inputMap.get("ncontrolcode");
		// String strNpreregno = (String) inputMap.get("npreregno");
		String strNtransactionsamplecode = (String) inputMap.get("ntransactionsamplecode");


		// Updated below query by L.Subashini on 28/04/2022 to add regtype and
		// regsubtype in transactionvalidation

		final String transValidation = "select ts.ntranscode,ts.jsondata->'stransdisplaystatus'->>'"
								+ userInfo.getSlanguagetypecode() + "'  stransdisplaystatus from "
								+ " transactionvalidation tv,transactionstatus ts where tv.ntransactionstatus=ts.ntranscode "
								+ " and tv.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
								+ " and ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
								+ " and tv.nformcode=" + userInfo.getNformcode() + " " + "and tv.ncontrolcode in(" + ncontrolcode + ")"
								+ " and tv.nregtypecode=" + nregTypeCode + " and tv.nregsubtypecode=" + nregSubTypeCode
								+ " and tv.nsitecode=" + userInfo.getNmastersitecode();

		final String strRegistration = "select rth.ntransactionstatus  from registrationsamplehistory rth "
							+ " where rth.nsamplehistorycode in (select max(nsamplehistorycode) from "
							+ " registrationsamplehistory rth1 where "
							+ " ntransactionsamplecode in(" + strNtransactionsamplecode + ") "
							+ " and rth1.nsitecode = " + userInfo.getNtranssitecode()
							+ " and rth1.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " group by npreregno,ntransactionsamplecode ) "
							+ " and rth.nsitecode = "+ userInfo.getNtranssitecode()
							+ " and rth.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final List<TransactionStatus> listTransactionStatus = jdbcTemplate.query(strRegistration, new TransactionStatus());
		final List<TransactionStatus> cancelValidation = jdbcTemplate.query(transValidation, new TransactionStatus());

		final List<Short> child = cancelValidation.stream().map(obj -> obj.getNtranscode()).collect(Collectors.toList());
		listTransactionStatus.removeIf(person -> child.contains((short) person.getNtransactionstatus()));
		
		if (listTransactionStatus.size() == 0) {

			// Updated below query by L.Subashini on 28/04/2022 to add regtype and
			// regsubtype in transactionvalidation
			final String sqlSubsample = "select npreregno,ntransactionsamplecode from "
								+ " registrationsamplehistory rsh,transactionvalidation tv "
								+ " where rsh.nsamplehistorycode=any(select max(rsh.nsamplehistorycode) "
								+ " from registrationsamplehistory rsh"
								+ " where rsh.ntransactionsamplecode in(" + strNtransactionsamplecode + ") "
								+ " and rsh.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and rsh.nsitecode=" + userInfo.getNtranssitecode() 
								+ " group by rsh.npreregno,rsh.ntransactionsamplecode)"
								+ " and rsh.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and	rsh.ntransactionstatus not in("
								+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + "" + ","
								+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ")"
								+ " and tv.ntransactionstatus=rsh.ntransactionstatus "
								+ " and tv.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and tv.nregtypecode="+ nregTypeCode 
								+ " and tv.nregsubtypecode=" + nregSubTypeCode 
								+ " and tv.nsitecode="+ userInfo.getNmastersitecode() 
								+ " and rsh.nsitecode=" + userInfo.getNtranssitecode()
								+ " group by npreregno,ntransactionsamplecode";
			final List<RegistrationSampleHistory> lstSeqNoSubSample = jdbcTemplate.query(sqlSubsample,
					new RegistrationSampleHistory());
			int lstSeqNoSubSampleUpdate = lstSeqNoSubSample.size();

			// Updated below query by L.Subashini on 28/04/2022 to add regtype and
			// regsubtype in transactionvalidation
			final String sqlTest = "select npreregno,ntransactionsamplecode,ntransactiontestcode from registrationtesthistory rth,"
								+ " transactionvalidation tv where rth.ntesthistorycode = any ("
								+ " select max(rth.ntesthistorycode) from registrationtesthistory rth where rth.ntransactionsamplecode in ("
								+ strNtransactionsamplecode + ") "
								+ " and rth.nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and rth.nsitecode=" + userInfo.getNtranssitecode()
								+ " group by rth.npreregno, rth.ntransactionsamplecode, rth.ntransactiontestcode) "
								+ " and rth.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and rth.ntransactionstatus not in ( "
								+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + "" + ","
								+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ") "
								+ " and tv.ntransactionstatus=rth.ntransactionstatus and tv.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and tv.nregtypecode=" + nregTypeCode 
								+ " and tv.nregsubtypecode=" + nregSubTypeCode 
								+ " and tv.nsitecode=" + userInfo.getNmastersitecode() 
								+ " and rth.nsitecode=" + userInfo.getNtranssitecode()
								+ " group by npreregno,ntransactionsamplecode,ntransactiontestcode";

			final List<RegistrationTestHistory> lstSeqNoTestSample = jdbcTemplate.query(sqlTest,
					new RegistrationTestHistory());
			int lstSeqNoTestUpdate = lstSeqNoTestSample.size();

			final String StrUpdateSample = "update seqnoregistration set nsequenceno="
									+ ((int) returnMap.get("registrationsamplehistory") + lstSeqNoSubSampleUpdate)
									+ " where stablename='registrationsamplehistory';" + "update seqnoregistration set nsequenceno="
									+ ((int) returnMap.get("registrationtesthistory") + lstSeqNoTestUpdate)
									+ " where stablename='registrationtesthistory';";

			jdbcTemplate.execute(StrUpdateSample);

			returnMap.put("inserttransactionsampleno", strNtransactionsamplecode);
			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		} else {

//			ResourceBundle resourcebundle = new PropertyResourceBundle(
//					new InputStreamReader(getClass().getClassLoader().getResourceAsStream(
//							"/com/properties/" + userInfo.getSlanguagefilename() + ".properties"), "UTF-8"));
			
			ResourceBundle resourcebundle = commonFunction.getResourceBundle(userInfo.getSlanguagefilename(), false);
			
			
			List<String> listString = new ArrayList<String>();
			for (int i = 0; i < cancelValidation.size(); i++) {
				listString.add(cancelValidation.get(i).getStransdisplaystatus());
			}
			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					resourcebundle.getString("IDS_SELECT") + " " + Joiner.on("/").join(listString) + " "
							+ resourcebundle.getString("IDS_SAMPLESTOCANCELREJECT"));
		}

		return returnMap;
	}
	
	@SuppressWarnings({ "unchecked" })
	@Override
	public Map<String, Object> cancelSubSample(Map<String, Object> inputMap) throws Exception {
		
		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		//JSONArray actionTypeTestArray = new JSONArray();
		JSONArray actionTypeSampleArray = new JSONArray();
		JSONObject actionType = new JSONObject();
		
		JSONObject jsonAuditOld = new JSONObject();
		JSONObject jsonAuditNew = new JSONObject();
		
		final ObjectMapper objMapper = new ObjectMapper();

		String sQuery = "lock table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus() + ";";
		jdbcTemplate.execute(sQuery);

		sQuery = " lock table lockcancelreject " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		Map<String, Object> returnMap = new HashMap<>();
		
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		int nsubsampleseqno = (int) inputMap.get("registrationsamplehistory");
		int ntestseqno = (int) inputMap.get("registrationtesthistory");
		String stransactionsampleno = (String) inputMap.get("inserttransactionsampleno");
	
//		final String Query = "select   rt.ntransactiontestcode"
//				+ " from registrationtest rt,registrationtesthistory rth where rt.ntransactionsamplecode in ("
//				+ stransactionsampleno + ") and rt.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				+ " and rt.ntransactiontestcode=rth.ntransactiontestcode and rt.ntransactionsamplecode =rth.ntransactionsamplecode  "
//				+ " and rt.npreregno=rth.npreregno  "
//				+ " and rth.ntesthistorycode in (select max(ntesthistorycode) from registrationtesthistory rth1  "
//				+ " where rth1.ntransactiontestcode=rt.ntransactiontestcode and rth1.nstatus="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") "
//				+ " and rth.ntransactionstatus not in (" + Enumeration.TransactionStatus.CANCELED.gettransactionstatus()
//				+ "," + Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ")"
//				+ " and rt.ntransactionsamplecode in (" + stransactionsampleno + ")  "
//				+ " order by rt.ntransactiontestcode,rt.ntransactionsamplecode,rt.npreregno ";
//		List<RegistrationTest> lstRegistrationTestCancelbefore = jdbcTemplate.query(Query, new RegistrationTest());

		// insertReghistory = insertReghistory+
		final String query1 = "insert into registrationsamplehistory(nsamplehistorycode,ntransactionsamplecode,"
							+ "npreregno,ntransactionstatus,dtransactiondate,nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,"
							+ "scomments,nsitecode,nstatus)" + "select " + nsubsampleseqno
							+ "+rank()over(order by rs.ntransactionsamplecode,rs.npreregno) nsamplehistorycode, rs.ntransactionsamplecode,"
							+ " rs.npreregno,case when rsh.ntransactionstatus ="
							+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + " then "
							+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + " else "
							+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + " end as ntransactionstatus,'"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' dtransactiondate," + userInfo.getNusercode() + " nusercode," + ""
							+ userInfo.getNuserrole() + " nuserrolecode," + userInfo.getNdeputyusercode() + " ndeputyusercode,"
							+ userInfo.getNdeputyuserrole() + " ndeputyuserrolecode," + "'' scomments,"
							+ userInfo.getNtranssitecode() + " nsitecode ,"
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
							+ " from registrationsample rs,registrationsamplehistory rsh  where "
							+ " rsh.npreregno= rs.npreregno and rsh.ntransactionsamplecode=rs.ntransactionsamplecode "
							+ " and rs.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and rsh.nstatus =  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and rs.nsitecode = "+ userInfo.getNtranssitecode()
							+ " and rsh.nsitecode=" + userInfo.getNtranssitecode()
							+ " and rsh.nsamplehistorycode in (select max(nsamplehistorycode) from "
							+ " registrationsamplehistory rsh1 where  rsh1.npreregno=rs.npreregno and "
							+ " rs.ntransactionsamplecode in (" + stransactionsampleno + ") "
							+ " and rsh1.nsitecode= "+ userInfo.getNtranssitecode()
							+ " and rsh1.nstatus =  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " group by rsh1.npreregno,rsh1.ntransactionsamplecode ) and rsh.ntransactionsamplecode in ("
							+ stransactionsampleno + ") order by rs.npreregno,rs.ntransactionsamplecode ";
		jdbcTemplate.execute(query1);

		final String query2 = "insert into registrationtesthistory(ntesthistorycode,ntransactiontestcode,"
								+ "ntransactionsamplecode,npreregno,nformcode,ntransactionstatus,nusercode,nuserrolecode,"
								+ "ndeputyusercode,ndeputyuserrolecode,scomments,dtransactiondate,nsampleapprovalhistorycode,nsitecode, nstatus)"
								+ " select " + ntestseqno
								+ "+rank()over(order by  rt.ntransactiontestcode,rt.ntransactionsamplecode,rt.npreregno ) ntesthistorycode, rt.ntransactiontestcode,"
								+ " rt.ntransactionsamplecode," + " rt.npreregno," + userInfo.getNformcode() + " nformcode,"
								+ " case when rth.ntransactionstatus ="
								+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + " then "
								+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + " when rth.ntransactionstatus="
								+ Enumeration.TransactionStatus.QUARENTINE.gettransactionstatus() + " then "
								+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + " else "
								+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + " end  as ntransactionstatus,"
								+ userInfo.getNusercode() + " nusercode," + userInfo.getNuserrole() + " nuserrolecode," + ""
								+ userInfo.getNdeputyusercode() + " ndeputyusercode," + userInfo.getNdeputyuserrole()
								+ " ndeputyuserrolecode,N'CANCELED' scomments," + "'" + dateUtilityFunction.getCurrentDateTime(userInfo)
								+ "' dtransactiondate,-1," + userInfo.getNtranssitecode() + " nsitecode,"
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
								+ " from registrationtest rt,registrationtesthistory rth " 
								+ " where rt.ntransactionsamplecode in ("+ stransactionsampleno + ") "
								+ " and rt.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and rth.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and rt.ntransactiontestcode=rth.ntransactiontestcode "
								+ " and rt.ntransactionsamplecode =rth.ntransactionsamplecode  "
								+ " and rt.nsitecode = "+ userInfo.getNtranssitecode()
								+ " and rth.nsitecode=" + userInfo.getNtranssitecode()
								+ " and rt.npreregno=rth.npreregno  "
								+ " and rth.ntesthistorycode in (select max(ntesthistorycode) from registrationtesthistory rth1  "
								+ " where rth1.ntransactiontestcode=rt.ntransactiontestcode "
								+ " and rth1.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and rth1.nsitecode=" + userInfo.getNtranssitecode() + ") "
								+ " and rth.ntransactionstatus not in ("
								+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ","
								+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ")"
								+ " and rt.ntransactionsamplecode in (" + stransactionsampleno + ") "
								+ " order by rt.ntransactiontestcode,rt.ntransactionsamplecode,rt.npreregno ";

		jdbcTemplate.execute(query2);

		returnMap = (Map<String, Object>) getRegistrationSubSample(inputMap, userInfo).getBody();

		jsonAuditOld.put("registrationsample", (List<Map<String, Object>>) inputMap.get("registrationSubSample"));
		jsonAuditNew.put("registrationsample", (List<Map<String, Object>>) returnMap.get("selectedSubSample"));		


		auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
		auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
		auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));

		((List<Map<String, Object>>) inputMap.get("registrationSubSample")).forEach(t -> {
			if (Integer.parseInt(((Map<String, Object>) t).get("ntransactionstatus")
					.toString()) == Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus()) {
				actionTypeSampleArray.put("IDS_REJECTSUBSAMPLE");
			} else {
				actionTypeSampleArray.put("IDS_CANCELSUBSAMPLE");
			}
		});


		actionType.put("registrationsample", actionTypeSampleArray);

		auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, jsonAuditNew, actionType, auditmap, false, userInfo);
		
		if ((int) inputMap.get("nsampletypecode") == Enumeration.SampleType.CLINICALSPEC.getType()) {
			final String checkMaunalOrder = " select count(rs.ntransactionsamplecode)  from registrationsample rs,"
									+ " registrationsamplehistory rsh where "
									+ " rs.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and rsh.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and rs.nsitecode=" + userInfo.getNtranssitecode() 
									+ " and rsh.nsitecode=" + userInfo.getNtranssitecode() 
									+ " and rsh.nsamplehistorycode in( "
									+ " select max(nsamplehistorycode) from  registrationsamplehistory  where "
									+ " nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and nsitecode=" + userInfo.getNtranssitecode() 
									+ " group by ntransactionsamplecode ) "
									+ " and rsh.ntransactionstatus not in ("
									+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","
									+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ") and"
									+ "  rsh.ntransactionsamplecode=rs.ntransactionsamplecode and  rsh.npreregno in ("
									+ inputMap.get("npreregno") + ") and  rs.jsondata->>'nordertypecode'='1' ";
			final int countManualOrder = jdbcTemplate.queryForObject(checkMaunalOrder, Integer.class);
			
			final String str = "select count(rs.ntransactionsamplecode)  from registrationsample rs,"
							+ " registrationsamplehistory rsh where "
							+ " rs.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and rsh.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and rs.nsitecode=" + userInfo.getNtranssitecode() 
							+ " and rsh.nsitecode=" + userInfo.getNtranssitecode() 
							+ " and rsh.nsamplehistorycode in( "
							+ "	select max(nsamplehistorycode) from  registrationsamplehistory  where "
							+ " nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and nsitecode=" + userInfo.getNtranssitecode() 
							+ " group by ntransactionsamplecode ) "
							+ "	and rsh.ntransactionstatus not in ("
							+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","
							+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ") and "
							+ " rsh.ntransactionsamplecode=rs.ntransactionsamplecode " + "	and  rsh.npreregno in ("
							+ inputMap.get("npreregno") + ") and  rs.jsondata->>'nordertypecode'='2'";			
			int countExternalorder = jdbcTemplate.queryForObject(str, Integer.class);

			if (countManualOrder == 0 && countExternalorder >= 1) {
				
				String externalvalue = "select rs.jsondata->>'sampleorderid' as sexternalsampleid, "
									+ " rs.ntransactionsamplecode , rs.jsondata->>'nsampleordercode' "
									+ " as nexternalordersamplecode, exs.nexternalordercode,eo.sexternalorderid as sexternalid "
									+ " from registrationsample rs,externalordersample exs ,externalorder eo   "
									+ " where rs.npreregno in (" + inputMap.get("npreregno")
									+ ")  and rs.jsondata->>'nordertypecode'='2' "
									+ " and (rs.jsondata->>'nsampleordercode')::integer= exs.nexternalordersamplecode "
									+ " and rs.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and rs.nsitecode=" + userInfo.getNtranssitecode() 
									+ " and eo.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and eo.nsitecode=" + userInfo.getNtranssitecode() 
									+ " and exs.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and exs.nsitecode=" + userInfo.getNtranssitecode() 
									+ " and eo.nexternalordercode = exs.nexternalordercode  limit 1 ";

				ExternalOrderSample objexternalvalue = (ExternalOrderSample) jdbcUtilityFunction.queryForObject(externalvalue,
						ExternalOrderSample.class, jdbcTemplate);
				
				String updateExternalorder = "update registration  set jsondata= jsondata ||jsonb_set(jsonb_set(jsonb_set( jsondata,"
										+ "   '{Order}', jsondata->'Order' || '{ \"label\": \"" + objexternalvalue.getSexternalid()
										+ "\", \"value\": " + objexternalvalue.getNexternalordercode() + ",\"nexternalordercode\":"
										+ objexternalvalue.getNexternalordercode() + "}'),"
										+ "    '{Order Type}', jsondata->'Order Type' || '{ \"label\": \"External\", \"value\": 2,\"nordertypecode\": 2}') ,"
										+ " '{External Sample Id}','{\"pkey\":\"nexternalordersamplecode\",\"label\":\""
										+ objexternalvalue.getSexternalsampleid() + "\",\"value\":"
										+ objexternalvalue.getNexternalordersamplecode()
										+ ",\"source\":\"view_externalsample\",\"nquerybuildertablecode\":260,\"nexternalordersamplecode\":"
										+ objexternalvalue.getNexternalordersamplecode() + "}')"
										+ "||jsonb_build_object('OrderIdData', '" + objexternalvalue.getSexternalid()
										+ "','OrderCodeData','" + objexternalvalue.getNexternalordercode() + "') "
										+ " ,jsonuidata= jsonuidata||jsonb_build_object('Order Type','External','orderTypeValue',2,'OrderCodeData',"
										+ objexternalvalue.getNexternalordercode() + "," + " 'Order','"
										+ objexternalvalue.getSexternalid() + "','OrderIdData','" + objexternalvalue.getSexternalid()
										+ "','External Sample Id','" + objexternalvalue.getSexternalsampleid() + "') "
										+ " where  npreregno in (" + inputMap.get("npreregno") + ") "
										+ " and nsitecode=" + userInfo.getNtranssitecode() 
										+ " ;";
				jdbcTemplate.execute(updateExternalorder);
				
				inputMap.put("npreregno", String.valueOf(inputMap.get("npreregno")));
				inputMap.put("ntransactionsamplecode", String.valueOf(inputMap.get("ntransactionsamplecode")));
				returnMap.putAll(registrationDAOSupport.getDynamicRegistration(inputMap, userInfo));
				returnMap.put("isMapped", true);
			}
		}

		inputMap.put("ssamplecode", stransactionsampleno);

		final int nstatuscode = actionTypeSampleArray.get(0) == "IDS_REJECTSAMPLE"
				? Enumeration.TransactionStatus.REJECTED.gettransactionstatus()
				: Enumeration.TransactionStatus.CANCELED.gettransactionstatus();

		returnMap.putAll((Map<String, Object>) externalOrderSupport.sampleOrderUpdate(inputMap, "", userInfo, nstatuscode,
				Enumeration.TransactionStatus.DRAFT.gettransactionstatus(), true).getBody());
		return returnMap;

	}
	
	@Override
	@SuppressWarnings({  "unlikely-arg-type" })
	public Map<String, Object> seqNoQuarentineSampleInsert(Map<String, Object> uiMap) throws Exception {

		final String sQuery = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final String strSelectSeqno = "select stablename,nsequenceno from seqnoregistration "
				// + Enumeration.ReturnStatus.TABLOCK.getreturnstatus()
				+ " where stablename='registrationhistory' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<SeqNoRegistration> lstSeqNo = jdbcTemplate.query(strSelectSeqno, new SeqNoRegistration());
		Map<String, Object> returnMap = new HashMap<String, Object>();
		
		String strNpreregno = (String) uiMap.get("npreregno");
		String[] preregnoArray = strNpreregno.split(",");

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(uiMap.get("userinfo"), UserInfo.class);

		final List<Integer> newList = Arrays.asList(preregnoArray).stream().map(s -> Integer.parseInt(s))
				.collect(Collectors.toList());
		
		String sql = "select * from  fn_registrationsamplecount ('" + strNpreregno + "' ,"
				+ userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus()
				+ ")";
		final String samplecount = jdbcTemplate.queryForObject(sql, String.class);
		
		String[] strArray = samplecount.split(",");
		final List<String> listSeqno = Arrays.asList(strArray);
		newList.removeAll(listSeqno);

		int seqNo = newList.size();
		String StrUpdate = "update seqnoregistration set nsequenceno= " + (lstSeqNo.get(0).getNsequenceno() + seqNo)
						+ " where stablename='registrationhistory'";
		jdbcTemplate.execute(StrUpdate);
		
		returnMap.put("sequencenumber", lstSeqNo.get(0).getNsequenceno());
		returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
				Enumeration.ReturnStatus.SUCCESS.getreturnstatus());

		return returnMap;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> quarantineSamples(Map<String, Object> inputMap) throws Exception {
		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditOld = new JSONObject();
		JSONObject jsonAuditNew = new JSONObject();

		final String sQuery = " lock table  lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus() + "";
		jdbcTemplate.execute(sQuery);
		
		final ObjectMapper objMapper = new ObjectMapper();
		Map<String, Object> returnMap = null;
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});

		int nseqno = (int) inputMap.get("sequencenumber");
		String spreregno = (String) inputMap.get("npreregno");

		String sinsertReghistoryQuery = "insert into registrationhistory (nreghistorycode,npreregno,ntransactionstatus,dtransactiondate,"
										+ "nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments,nsitecode,nstatus) "
										+ "select " + nseqno + "+rank()over(order by npreregno) nreghistorycode,npreregno,"
										+ Enumeration.TransactionStatus.QUARENTINE.gettransactionstatus() + " ntransactionstatus," + "'"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'  dtransactiondate," + userInfo.getNusercode() + " nusercode,"
										+ userInfo.getNuserrole() + " nuserrolecode," + "" + userInfo.getNdeputyusercode() + " ndeputyusercode,"
										+ userInfo.getNdeputyuserrole() + " ndeputyuserrolecode,N'Quarantine' scomments,"
										+ userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " nstatus from registration where npreregno in (" + spreregno + ") and nstatus="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
										+ userInfo.getNtranssitecode() + " order by npreregno;";

		jdbcTemplate.execute(sinsertReghistoryQuery);
		returnMap = registrationDAOSupport.getDynamicRegistration(inputMap, userInfo);
		
		auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
		auditmap.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
		auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
		actionType.put("registration", "IDS_QUARENTINE");
		jsonAuditOld.put("registration", (List<Map<String, Object>>) inputMap.get("selectedSample"));
		jsonAuditNew.put("registration", (List<Map<String, Object>>) returnMap.get("RegistrationGetSample"));
		
		auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, jsonAuditNew, actionType, auditmap, false, userInfo);
		
		return returnMap;

	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map<String, Object> schedulerInsertRegistration(Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapperS = new ObjectMapper();
		objMapperS.registerModule(new JavaTimeModule());
		
		final ScheduleMaster schedulemaster = objMapperS.convertValue(inputMap.get("selectedscheduler"),
				new TypeReference<ScheduleMaster>() {
				});

		String dateStart = schedulemaster.getSstarttime();
		String scheduletype = schedulemaster.getSscheduletype();
		char stype = scheduletype.charAt(0);
		int count = 0;
		if (stype == 'O') {
			count = 1;
		}

		else {

			//String noccurencenoftime = schedulemaster.getSoccurencehourwiseinterval();
			//String noccurencen = noccurencenoftime.replace(":", ".");

			String Query = "SELECT DATE_PART('day', dendtime::timestamp - dstarttime::timestamp) AS days "
					+ " from schedulemaster where nschedulecode=" + schedulemaster.getNschedulecode();

			count = jdbcTemplate.queryForObject(Query, Integer.class);
			count = count + 1;
			// }

		}

		final UserInfo userInfo = objMapperS.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		
		final Registration registration = objMapperS.convertValue(inputMap.get("Registration"),
				new TypeReference<Registration>() {});

		final List<RegistrationSample> registrationSample = objMapperS.convertValue(inputMap.get("RegistrationSample"),
				new TypeReference<List<RegistrationSample>>() {});

		final List<TestGroupTest> testGroupTests = objMapperS.convertValue(inputMap.get("testgrouptest"),
				new TypeReference<List<TestGroupTest>>() {});

		final List<String> dateList = objMapperS.convertValue(inputMap.get("DateList"), new TypeReference<List<String>>() {});

		final List<Map<String, Object>> sampleDateConstraint = objMapperS.convertValue(inputMap.get("sampledateconstraints"),
				new TypeReference<List<Map<String, Object>>>() {});
		
		final List<Map<String, Object>> subSampleDateConstraint = objMapperS
				.convertValue(inputMap.get("subsampledateconstraints"), new TypeReference<List<Map<String, Object>>>() {});

		final List<String> SubSampledateList = objMapperS.convertValue(inputMap.get("subsampleDateList"),
				new TypeReference<List<String>>() {});

		final String strTestList = testGroupTests.stream().map(x -> String.valueOf(x.getNtestgrouptestcode()))
				.distinct().collect(Collectors.joining(","));
		
		int npreregno = (int) inputMap.get("registration");
		int nreghistorycode = (int) inputMap.get("registrationhistory");
		int nregsamplecode = (int) inputMap.get("registrationsample");
		int nregsamplehistorycode = (int) inputMap.get("registrationsamplehistory");
		int ntransactiontestcode = (int) inputMap.get("registrationtest");
		int ntesthistorycode = (int) inputMap.get("registrationtesthistory");
		int nregdecisionhistorycode = (int) inputMap.get("registrationdecisionhistory");
		int nregistrationparametercode = (int) inputMap.get("registrationparameter");
		
		++npreregno;
		++nreghistorycode;
		++ntransactiontestcode;
		++nregistrationparametercode;
		++ntesthistorycode;
		++nregdecisionhistorycode;

		String seqQueryTran = "select nsequenceno from  seqnoscheduler where seqnoscheduler.stablename='schedulartransaction'"
							+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		int nschedulartransactioncode = jdbcTemplate.queryForObject(seqQueryTran, Integer.class);
		++nschedulartransactioncode;
		for (int j = 0; j < count; j++) {
			int nregtypecode = (int) inputMap.get("nregtypecode");
			int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
			int nsampletypecode = (int) inputMap.get("nsampletypecode");

			inputMap.put("nsitecode", userInfo.getNtranssitecode());

			List<TestGroupTestParameter> lstTestGroupTestParameter = new ArrayList<TestGroupTestParameter>();

			if (!testGroupTests.isEmpty()) {
				String strParamterList = "select tgtp.ntestgrouptestcode,"
										+ " tgtp.ntestgrouptestparametercode,tgtp.ntestparametercode,tgtp.nroundingdigits,"
										+ " tgtp.nresultmandatory,tgtp.nreportmandatory,COALESCE(tgf.ntestgrouptestformulacode,-1)"
										+ " ntestgrouptestformulacode,"
										+ " tgtp.nunitcode,tgtp.nparametertypecode,tgtnp.sminlod,tgtnp.smaxlod,tgtnp.sminb,"
										+ " tgtnp.smina,tgtnp.smaxa,tgtnp.smaxb,tgtnp.sminloq,tgtnp.smaxloq,"
										+ " tgtnp.sdisregard,tgtnp.sresultvalue"
										+ " from testgrouptest tgt,testgrouptestparameter tgtp left outer join "
										+ " testgrouptestnumericparameter tgtnp on tgtnp.ntestgrouptestparametercode=tgtp.ntestgrouptestparametercode "
										+ " and tgtnp.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and tgtnp.nsitecode="+ userInfo.getNmastersitecode()
										+ " left outer join testgrouptestformula tgf on tgtnp.ntestgrouptestparametercode=tgf.ntestgrouptestparametercode "
										+ " and tgf.ntransactionstatus = "+ Enumeration.TransactionStatus.YES.gettransactionstatus()
										+ " and tgf.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and tgf.nsitecode="+ userInfo.getNmastersitecode()
										+ " where tgt.ntestgrouptestcode=tgtp.ntestgrouptestcode "
										+ " and tgt.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and tgtp.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and tgt.nsitecode="+ userInfo.getNmastersitecode()
										+ " and tgtp.nsitecode="+ userInfo.getNmastersitecode()
										+ " and tgtp.ntestgrouptestcode in (" + strTestList + ");";
				lstTestGroupTestParameter = jdbcTemplate.query(strParamterList, new TestGroupTestParameter());
			}

			String strRegistrationInsert = "";
			String strRegistrationHistory = "";
			String strRegistrationArno = "";
			String strRegistrationSample = "";
			String strRegistrationSampleHistory = "";
			String strRegistrationSampleArno = "";
			String strDecisionHistory = "";
			String strRegistrationStatusBlink = "";
			String strSchedulerInsert = "";
			int sampleCount = registrationSample.size();
			boolean statusflag = false;

			JSONObject jsoneditRegistration = new JSONObject(registration.getJsondata());
			JSONObject jsonuiRegistration = new JSONObject(registration.getJsonuidata());

			if (!dateList.isEmpty()) {

//				if (nregsubtypecode == 5) {
//					final Map<String, Object> map = savePatient(jsoneditRegistration, dateList, userInfo);
//					if (!((String) map.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))
//							.equals(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())) {
//						return map;
//					}
//				}

				jsoneditRegistration = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsoneditRegistration, dateList, false,
						userInfo);
				jsonuiRegistration = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonuiRegistration, dateList, false,
						userInfo);
				final Map<String, Object> obj = commonFunction.validateDynamicDateContraints(jsonuiRegistration,
						sampleDateConstraint, userInfo);
				if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
						.equals((String) obj.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
					return obj;
				}
			}

			jsonuiRegistration.put("npreregno", npreregno);
			jsonuiRegistration.put("nsampletypecode", nsampletypecode);
			jsonuiRegistration.put("nregtypecode", nregtypecode);
			jsonuiRegistration.put("nregsubtypecode", nregsubtypecode);
			jsonuiRegistration.put("nproductcatcode", registration.getNproductcatcode());
			jsonuiRegistration.put("nproductcode", registration.getNproductcode());
			jsonuiRegistration.put("ninstrumentcatcode", registration.getNinstrumentcatcode());
			jsonuiRegistration.put("ninstrumentcode", registration.getNinstrumentcode());
			jsonuiRegistration.put("nmaterialcatcode", registration.getNmaterialcatcode());
			jsonuiRegistration.put("nmaterialcode", registration.getNmaterialcode());
			jsonuiRegistration.put("ntemplatemanipulationcode", registration.getNtemplatemanipulationcode());
			jsonuiRegistration.put("nallottedspeccode", registration.getNallottedspeccode());
			jsonuiRegistration.put("ndesigntemplatemappingcode", registration.getNdesigntemplatemappingcode());
			jsonuiRegistration.put("Collection Date", dateStart);
			jsoneditRegistration.put("Collection Date", dateStart);

			strRegistrationInsert = strRegistrationInsert + "(" + npreregno + "," + nsampletypecode + "," + nregtypecode
					+ "," + nregsubtypecode + "," + registration.getNproductcatcode() + ","
					+ registration.getNproductcode() + "," + registration.getNinstrumentcatcode() + ","
					+ registration.getNinstrumentcode() + "," + registration.getNmaterialcatcode() + ","
					+ registration.getNmaterialcode() + "," + registration.getNtemplatemanipulationcode() + ","
					+ registration.getNallottedspeccode() + "," + userInfo.getNtranssitecode() + ",'"
					+ jsoneditRegistration.toString() + "'::JSONB,'" + jsonuiRegistration.toString() + "'::JSONB,"
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ","
					+ registration.getNdesigntemplatemappingcode() + "," + registration.getNregsubtypeversioncode()+","
					+ registration.getNprotocolcode()+"),";

			strSchedulerInsert = strSchedulerInsert + "(" + nschedulartransactioncode + ","
					+ schedulemaster.getNschedulecode() + "," + npreregno + ",'" + dateStart + "',"
					+ userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "),";

			strRegistrationHistory = strRegistrationHistory + "(" + nreghistorycode + "," + npreregno + ","
					+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + ",'" + dateStart + "',"
					+ userInfo.getNusercode() + "," + userInfo.getNuserrole() + "," + userInfo.getNdeputyusercode()
					+ "," + userInfo.getNdeputyuserrole() + ",'" + stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "',"
					+ userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "," + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode()
					+ "),";

			strDecisionHistory = strDecisionHistory + "(" + nregdecisionhistorycode + "," + npreregno + ","
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ",'" + dateStart + "',"
					+ userInfo.getNusercode() + "," + userInfo.getNuserrole() + "," + userInfo.getNdeputyusercode()
					+ "," + userInfo.getNdeputyuserrole() + ",'" + stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "',"
					+ userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "," + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode()
					+ "),";

			strRegistrationArno = strRegistrationArno + "(" + npreregno + ",'-',"
					+ Enumeration.TransactionStatus.NA.gettransactionstatus() + "," + userInfo.getNtranssitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";

			strRegistrationStatusBlink = strRegistrationStatusBlink + "(" + npreregno + "," + statusflag + ","
					+ userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "),";

			for (int i = sampleCount - 1; i >= 0; i--) {
				nregsamplecode++;
				nregsamplehistorycode++;
				nregdecisionhistorycode++;

				JSONObject jsoneditObj = new JSONObject(registrationSample.get(i).getJsondata());
				JSONObject jsonuiObj = new JSONObject(registrationSample.get(i).getJsonuidata());
				if (!SubSampledateList.isEmpty()) {
					jsoneditObj = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsoneditObj, SubSampledateList, false,
							userInfo);
					jsonuiObj = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonuiObj, SubSampledateList, false, userInfo);
					final Map<String, Object> obj = commonFunction.validateDynamicDateContraints(jsonuiObj,
							subSampleDateConstraint, userInfo);
					if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
							.equals((String) obj.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
						return obj;
					}
				}

				jsonuiObj.put("ntransactionsamplecode", nregsamplecode);
				jsonuiObj.put("npreregno", npreregno);
				jsonuiObj.put("nspecsampletypecode", registrationSample.get(i).getNspecsampletypecode());
				jsonuiObj.put("ncomponentcode", registrationSample.get(i).getNcomponentcode());

				strRegistrationSample = strRegistrationSample + " (" + nregsamplecode + "," + npreregno + ",'"
						+ registrationSample.get(i).getNspecsampletypecode() + "',"
						+ registrationSample.get(i).getNcomponentcode() + ",'" + jsoneditObj.toString() + "'::JSONB,'"
						+ jsonuiObj.toString() + "'::JSONB," + userInfo.getNtranssitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";

				strRegistrationSampleHistory = strRegistrationSampleHistory + "(" + nregsamplehistorycode + ","
						+ nregsamplecode + "," + npreregno + ","
						+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + ",'" + dateStart + "',"
						+ userInfo.getNusercode() + "," + userInfo.getNuserrole() + "," + userInfo.getNdeputyusercode()
						+ "," + userInfo.getNdeputyuserrole() + ",'" + stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "',"
						+ userInfo.getNtranssitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode()
						+ "),";

				strRegistrationSampleArno = strRegistrationSampleArno + "(" + nregsamplecode + "," + npreregno + ",'-'"
						+ "," + userInfo.getNtranssitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";

				List<RegistrationSample> comp = new ArrayList();
				comp.add(registrationSample.get(i));
				List<TestGroupTest> lsttest1 = testGroupTests.stream().filter(x -> x.getSlno() == comp.get(0).getSlno())
						.collect(Collectors.toList());

				final String stestcode = lsttest1.stream().map(x -> String.valueOf(x.getNtestgrouptestcode()))
						.distinct().collect(Collectors.joining(","));

				if (!lsttest1.isEmpty()) {
					String strRegistrationTest = "insert into registrationtest (ntransactiontestcode,ntransactionsamplecode,npreregno,"
													+ "ntestgrouptestcode,ninstrumentcatcode,nchecklistversioncode,ntestrepeatno,"
													+ " ntestretestno,jsondata,dmodifieddate,nsitecode, nstatus,ntestcode,nsectioncode,nmethodcode) "
													+ "(select " + ntransactiontestcode + "+RANK() over(order by tgt.ntestgrouptestcode),"
													+ nregsamplecode + "," + npreregno + "," + "tgt.ntestgrouptestcode" + ",ninstrumentcatcode,"
													+ "-1,1,0,json_build_object('ntransactiontestcode', " + ntransactiontestcode
													+ "+RANK() over(order by tgt.ntestgrouptestcode),'npreregno'," + npreregno
													+ ",'ntransactionsamplecode'," + nregsamplecode
													+ ",'ssectionname',s.ssectionname,'smethodname',m.smethodname,'ncost',tgt.ncost,'stestname',tgt.stestsynonym,"
													+ "'stestsynonym',concat(tgt.stestsynonym,'[1][0]'))::jsonb,'"
													+ dateUtilityFunction.getCurrentDateTime(userInfo)							
													+ "', " + userInfo.getNtranssitecode() + ","
													+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
													+ " , tgt.ntestcode,tgt.nsectioncode,tgt.nmethodcode "
													+ " from testgrouptest tgt, section s, method m  "
													+ " where tgt.nsectioncode=s.nsectioncode "
													+ " and tgt.nmethodcode=m.nmethodcode "
													+ " and tgt.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
													+ " and tgt.nsitecode="+ userInfo.getNmastersitecode()
													+ " and s.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
													+ " and s.nsitecode="+ userInfo.getNmastersitecode()
													+ " and m.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
													+ " and m.nsitecode="+ userInfo.getNmastersitecode()
													+ " and tgt.ntestgrouptestcode in (" + stestcode + "));";

					jdbcTemplate.execute(strRegistrationTest);

					strRegistrationTest = "Insert into registrationtesthistory (ntesthistorycode, ntransactiontestcode, "
											+ "ntransactionsamplecode, npreregno, nformcode, ntransactionstatus,"
											+ "	nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode,scomments,"
											+ " dtransactiondate,nsampleapprovalhistorycode, nsitecode, nstatus,noffsetdtransactiondate,ntransdatetimezonecode) (select "
											+ ntesthistorycode + "+RANK() over(order by ntestgrouptestcode)," + ntransactiontestcode
											+ "+RANK() over(order by ntestgrouptestcode)," + nregsamplecode + "," + npreregno + ","
											+ userInfo.getNformcode() + ","
											+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + ","
											+ userInfo.getNusercode() + "," + userInfo.getNuserrole() + ","
											+ userInfo.getNdeputyusercode() + "," + userInfo.getNdeputyuserrole() + ",'"
											+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "','" + dateStart + "',-1,"
											+ userInfo.getNtranssitecode() + ","
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
											+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode()
											+ " from testgrouptest where ntestgrouptestcode in (" + stestcode + ")"
											+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " and nsitecode="+ userInfo.getNmastersitecode()
											+ ");";

					jdbcTemplate.execute(strRegistrationTest);
					String strAdhocQuery="";
					if((int)inputMap.get("nsampletypecode")==Enumeration.SampleType.CLINICALSPEC.getType()) {
						strAdhocQuery=" and tgtp.nisadhocparameter="+ Enumeration.TransactionStatus.NO.gettransactionstatus();
					}else {
						strAdhocQuery=" and tgtp.nisvisible="+ Enumeration.TransactionStatus.YES.gettransactionstatus();
					}

					strRegistrationTest = "insert into registrationparameter (ntransactionresultcode,npreregno,ntransactiontestcode,"
											+ "ntestgrouptestparametercode,ntestparametercode,nparametertypecode,ntestgrouptestformulacode,nunitcode,"
											+ "nresultmandatory,nreportmandatory,jsondata,nsitecode, nstatus) " + "(select "
											+ nregistrationparametercode + "+RANK() over(order by tgtp.ntestgrouptestparametercode),"
											+ npreregno + "," + ntransactiontestcode
											+ "+DENSE_RANK() over(order by tgtp.ntestgrouptestcode),"
											+ "tgtp.ntestgrouptestparametercode,tgtp.ntestparametercode,tgtp.nparametertypecode,COALESCE(tgf.ntestgrouptestformulacode,-1) ntestgrouptestformulacode,"
											+ "tgtp.nunitcode,tgtp.nresultmandatory,tgtp.nreportmandatory,"
											+ "json_build_object('ntransactionresultcode'," + nregistrationparametercode
											+ "+RANK() over(order by tgtp.ntestgrouptestparametercode),'ntransactiontestcode',"
											+ ntransactiontestcode + "+DENSE_RANK() over(order by tgtp.ntestgrouptestcode),'npreregno',"
											+ npreregno
											+ ",'sminlod',tgtnp.sminlod,'smaxlod',tgtnp.smaxlod,'sminb',tgtnp.sminb,'smina',tgtnp.smina,'smaxa',tgtnp.smaxa,'smaxb',tgtnp.smaxb,"
											+ "'sminloq',tgtnp.sminloq,'smaxloq',tgtnp.smaxloq,'sdisregard',tgtnp.sdisregard,'sresultvalue',tgtnp.sresultvalue,"
											+ "'nroundingdigits',tgtp.nroundingdigits,'sresult',NULL,'sfinal',NULL,'stestsynonym',concat(tgt.stestsynonym,'[1][0]'),'sparametersynonym',tgtp.sparametersynonym)::jsonb,"
											+ userInfo.getNtranssitecode() + ","
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " from testgrouptest tgt,testgrouptestparameter tgtp left"
											+ " outer join testgrouptestnumericparameter tgtnp on tgtnp.ntestgrouptestparametercode=tgtp.ntestgrouptestparametercode "
											+ " and tgtnp.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " and tgtnp.nsitecode="+ userInfo.getNmastersitecode()										
											+ " left outer join testgrouptestformula tgf on tgtnp.ntestgrouptestparametercode=tgf.ntestgrouptestparametercode"
											+ " and tgf.ntransactionstatus = "
											+ Enumeration.TransactionStatus.YES.gettransactionstatus()
											+ " and tgf.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " and tgf.nsitecode="+ userInfo.getNmastersitecode()										
											+ " where tgt.ntestgrouptestcode=tgtp.ntestgrouptestcode "
											+ " and tgt.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " and tgtp.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " and tgt.nsitecode="+ userInfo.getNmastersitecode()
											+ " and tgtp.nsitecode="+ userInfo.getNmastersitecode()
											+ " and tgtp.ntestgrouptestcode in (" + stestcode + ") " + strAdhocQuery + ");";

					jdbcTemplate.execute(strRegistrationTest);

					ntransactiontestcode = ntransactiontestcode + lsttest1.size();
					ntesthistorycode = ntesthistorycode + lsttest1.size();

					List<TestGroupTestParameter> filteredList = lstTestGroupTestParameter.stream()
							.filter(empl -> lsttest1.stream()
									.anyMatch(dept -> dept.getNtestgrouptestcode() == empl.getNtestgrouptestcode()))
							.collect(Collectors.toList());
					nregistrationparametercode = nregistrationparametercode + filteredList.size();
				}

			}

			strRegistrationInsert = "Insert into registration (npreregno, nsampletypecode, nregtypecode, nregsubtypecode, nproductcatcode, "
					+ "nproductcode,ninstrumentcatcode,ninstrumentcode,nmaterialcatcode,nmaterialcode, ntemplatemanipulationcode, nallottedspeccode, nsitecode,jsondata,jsonuidata, nstatus,ndesigntemplatemappingcode,nregsubtypeversioncode,nprotocolcode) values "
					+ strRegistrationInsert.substring(0, strRegistrationInsert.length() - 1) + ";";
			jdbcTemplate.execute(strRegistrationInsert);

			strSchedulerInsert = "Insert into schedulartransaction (nschedulartransactioncode,nschedularcode,npreregno,dscheduleoccurencedate,nsitecode,nstatus) values "
					+ strSchedulerInsert.substring(0, strSchedulerInsert.length() - 1) + ";";
			jdbcTemplate.execute(strSchedulerInsert);

			strRegistrationHistory = "Insert into registrationhistory(nreghistorycode, npreregno, ntransactionstatus, dtransactiondate, nusercode, "
					+ " nuserrolecode, ndeputyusercode, ndeputyuserrolecode, scomments, nsitecode,nstatus,noffsetdtransactiondate,ntransdatetimezonecode) values "
					+ strRegistrationHistory.substring(0, strRegistrationHistory.length() - 1) + ";";
			jdbcTemplate.execute(strRegistrationHistory);

			if (registration.getNsampletypecode() == Enumeration.SampleType.INSTRUMENT.getType()) {

				String query = "select * from instrumentcalibration where ncalibrationstatus="
						+ Enumeration.TransactionStatus.UNDERCALIBIRATION.gettransactionstatus()
						+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsitecode="+ userInfo.getNtranssitecode()
						+ " and dopendate is null and dclosedate is null";

				InstrumentCalibration instrumentCalibration = (InstrumentCalibration) jdbcUtilityFunction.queryForObject(query,
						InstrumentCalibration.class, jdbcTemplate);

				query = "update instrumentcalibration set npreregno=" + npreregno + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ninstrumentcalibrationcode="
						+ instrumentCalibration.getNinstrumentcalibrationcode()
						+ "  and nsitecode="+ userInfo.getNtranssitecode();
				jdbcTemplate.execute(query);

			}

			strDecisionHistory = "Insert into registrationdecisionhistory(nregdecisionhistorycode, npreregno, ndecisionstatus, dtransactiondate, "
					+ "nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, scomments, nsitecode, nstatus,noffsetdtransactiondate,ntransdatetimezonecode  ) values "
					+ strDecisionHistory.substring(0, strDecisionHistory.length() - 1) + ";";
			jdbcTemplate.execute(strDecisionHistory);

			strRegistrationArno = "Insert into registrationarno (npreregno, sarno, napprovalversioncode, nsitecode, nstatus) values "
					+ strRegistrationArno.substring(0, strRegistrationArno.length() - 1) + ";";
			jdbcTemplate.execute(strRegistrationArno);

			strRegistrationSample = "insert into registrationsample(ntransactionsamplecode,npreregno,nspecsampletypecode,ncomponentcode,jsondata,jsonuidata, "
					+ "nsitecode,nstatus) values "
					+ strRegistrationSample.substring(0, strRegistrationSample.length() - 1) + ";";
			jdbcTemplate.execute(strRegistrationSample);

			strRegistrationSampleHistory = "Insert into registrationsamplehistory(nsamplehistorycode, ntransactionsamplecode, npreregno, ntransactionstatus, "
					+ "dtransactiondate, nusercode, nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments,nsitecode, nstatus,noffsetdtransactiondate,ntransdatetimezonecode ) values "
					+ strRegistrationSampleHistory.substring(0, strRegistrationSampleHistory.length() - 1) + ";";
			jdbcTemplate.execute(strRegistrationSampleHistory);

			strRegistrationSampleArno = "Insert into registrationsamplearno (ntransactionsamplecode,npreregno,ssamplearno,nsitecode, nstatus) values "
					+ strRegistrationSampleArno.substring(0, strRegistrationSampleArno.length() - 1) + ";";
			jdbcTemplate.execute(strRegistrationSampleArno);

			strRegistrationStatusBlink = "Insert into registrationflagstatus (npreregno,bflag,nsitecode,nstatus) "
					+ " values" + strRegistrationStatusBlink.substring(0, strRegistrationStatusBlink.length() - 1)
					+ ";";
			jdbcTemplate.execute(strRegistrationStatusBlink);

			inputMap.put("npreregno", String.valueOf(npreregno));
			// inputMap.put("nsubsample",3);

			++npreregno;
			++nreghistorycode;
			++ntransactiontestcode;
			++nregistrationparametercode;
			++ntesthistorycode;
			++nregdecisionhistorycode;
			++nschedulartransactioncode;
			if (stype != 'O') {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date oldDate = (Date) formatter.parse(dateStart);

//		DateFormat sdf = new SimpleDateFormat("HH:mm");
//		Date date = sdf.parse(schedulemaster.getSoccurencehourwiseinterval());
//		Time time = new Time(date.getTime());
//		int initHour = time.getHours();
//		int initMind = time.getMinutes();
//         int mins=initHour*60+initMind;
				Calendar c = Calendar.getInstance();
				c.setTime(oldDate);
				// c.add(Calendar.MINUTE,mins);
				c.add(Calendar.DATE, 1);
				Date currentDatePlusOne = c.getTime();
				dateStart = formatter.format(currentDatePlusOne);
			}
		}

		final String strSeqnoUpdate = "Update seqnoregistration set nsequenceno = " + (npreregno - 1)
							+ " where stablename = 'registration';" + "Update seqnoregistration set nsequenceno = "
							+ (nregdecisionhistorycode - 1) + " where stablename = 'registrationdecisionhistory';"
							+ "Update seqnoregistration set nsequenceno = " + (nreghistorycode - 1)
							+ " where stablename = 'registrationhistory';" + "Update seqnoregistration set nsequenceno = "
							+ (nregistrationparametercode - 1) + " where stablename = 'registrationparameter';"
							+ "Update seqnoregistration set nsequenceno = " + (nregsamplecode)
							+ " where stablename = 'registrationsample';" + "Update seqnoregistration set nsequenceno = "
							+ (nregsamplehistorycode) + " where stablename = 'registrationsamplehistory';"
							+ "Update seqnoregistration set nsequenceno = " + (ntransactiontestcode - 1)
							+ " where stablename = 'registrationtest' ;" + "Update seqnoregistration set nsequenceno = "
							+ (ntesthistorycode - 1) + " where stablename = 'registrationtesthistory';"
							+ "Update seqnoscheduler set nsequenceno = " + (nschedulartransactioncode - 1)
							+ " where stablename = 'schedulartransaction';";
		jdbcTemplate.execute(strSeqnoUpdate);

		Map<String, Object> returnMap = new HashMap<>();

		returnMap.putAll((Map<? extends String, ? extends Object>) schedulerDAO
				.getScheduler(schedulemaster.getNschedulecode(), userInfo, schedulemaster.getSscheduletype())
				.getBody());

		return returnMap;

	}
	
	@Override
	public ResponseEntity<Object> getSampleBasedOnExternalOrder(Map<String, Object> inputMap, UserInfo userInfo) {
	
		Map<String, Object> objMap = new HashMap<>();

		int nexternalordercode = (int) inputMap.get("nexternalordercode");
		// ALPD-3575
//		String spgDateTimeFormat = "dd/MM/yyyy HH24:mi:ss";
//		if (userInfo != null) {
//			spgDateTimeFormat = userInfo.getSpgdatetimeformat();
//		}
		if (nexternalordercode != -1) {
			final String query = " select (select nquerybuildertablecode from querybuildertables where stablename='sampleappearance') nquerybuildertablecode,tgs.nspecsampletypecode,eo.nallottedspeccode,"
								+ "to_char(es.dsamplecollectiondatetime, 'YYYY-MM-DD HH24:MI:SS') dsamplecollectiondatetime"
								+ ", sa.ssampleappearance,es.*,u.sunitname,c.scomponentname from externalordersample es,"
								+ "unit u,component c,testgroupspecsampletype tgs,externalorder eo, sampleappearance sa "
								+ " where tgs.nallottedspeccode=eo.nallottedspeccode "
								+ " and eo.nexternalordercode=es.nexternalordercode and c.ncomponentcode=tgs.ncomponentcode"
								+ " and es.nsampleappearancecode=sa.nsampleappearancecode and es.nunitcode=u.nunitcode and "
								+ " u.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
								+ " es.ncomponentcode=c.ncomponentcode "
								+ " and c.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and tgs.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and eo.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and es.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and sa.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and c.nsitecode="	+ userInfo.getNmastersitecode()
								+ " and tgs.nsitecode="+ userInfo.getNmastersitecode()
								+ " and sa.nsitecode="+ userInfo.getNmastersitecode()
								+ " and eo.nsitecode="+ userInfo.getNtranssitecode()
								+ " and es.nsitecode="+ userInfo.getNtranssitecode()
								+ " and es.nexternalordersamplecode="
								+ inputMap.get("nexternalordersamplecode") 
								+ " and es.nexternalordercode=" + nexternalordercode;
			final List<Map<String, Object>> objSample = jdbcTemplate.queryForList(query);
			
			objMap.put("Sample", objSample);
			if (!objSample.isEmpty()) {
				final String nexternalordersamplecode1 = objSample.stream()
						.map(x -> String.valueOf(x.get("nexternalordersamplecode"))).collect(Collectors.joining(","));

				final String TestQuery = " select (" + "  select count(tgtp.ntestgrouptestparametercode) "
										+ "  from testgrouptestparameter tgtp "
										+ "  where  tgtp.ntestgrouptestcode=tgt.ntestgrouptestcode  and tgtp.nstatus ="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " ) as nparametercount,tgt.nrepeatcountno,s.ssectionname,m.smethodname,"
										+ " ic.sinstrumentcatname,es.nexternalordercode,tgs.nallottedspeccode,'"
										+ "tgs.nspecsampletypecode,"
										+ " et.nexternalordertestcode,et.ntestcode,et.nexternalordersamplecode,"
										+ " et.ntestpackagecode,tgt.ntestgrouptestcode,tgt.stestsynonym,"
										+ " tgt.stestsynonym stestname from externalordertest et,"
										+ " externalordersample es, testgroupspecsampletype tgs,testgrouptest tgt,"
										+ " externalorder eo,section s,method m,instrumentcategory ic where "
										+ " et.nexternalordersamplecode=es.nexternalordersamplecode and"
										+ " tgs.nallottedspeccode=eo.nallottedspeccode and es.ncomponentcode=tgs.ncomponentcode"
										+ " and tgt.nspecsampletypecode=tgs.nspecsampletypecode"
										+ " and tgt.ntestcode=et.ntestcode and  eo.nexternalordercode=et.nexternalordercode"
										+ " and tgt.nsectioncode=s.nsectioncode and tgt.nmethodcode=m.nmethodcode "
										+ " and tgt.ninstrumentcatcode=ic.ninstrumentcatcode "
										+ " and tgt.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
										+ "  and eo.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
										+ "  and s.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
										+ "  and m.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
										+ "  and ic.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
										+ "  and tgs.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
										+ "  and es.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
										+ "  and et.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ "  and eo.nsitecode="+ userInfo.getNtranssitecode()
										+ "  and s.nsitecode="+ userInfo.getNmastersitecode()
										+ "  and m.nsitecode="+ userInfo.getNmastersitecode()
										+ "  and ic.nsitecode="+ userInfo.getNmastersitecode()
										+ "  and tgs.nsitecode="+ userInfo.getNmastersitecode()
										+ "  and tgt.nsitecode="+ userInfo.getNmastersitecode()
										+ "  and es.nsitecode="+ userInfo.getNtranssitecode()
										+ "  and et.nsitecode="+ userInfo.getNtranssitecode()
										+ " and et.nexternalordersamplecode in (" + nexternalordersamplecode1 + ")";

				List<Map<String, Object>> objTest = jdbcTemplate.queryForList(TestQuery);
				objMap.put("Test", objTest);
			} else {

				objMap.put("Test", Arrays.asList());
			}
		} else {
			objMap.put("Sample", Arrays.asList());
			objMap.put("Test", Arrays.asList());
		}
		return new ResponseEntity<Object>(objMap, HttpStatus.OK);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> viewRegistrationFile(Registration objFile, UserInfo userInfo,
			Map<String, Object> inputMap) throws Exception {
		
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> value = new LinkedHashMap<String, Object>();
		
		final String strQuery = "select * from registration where npreregno = " + objFile.getNpreregno() 
								+ " and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ "  and nsitecode="+ userInfo.getNtranssitecode();
		final Registration objRegistration = (Registration) jdbcUtilityFunction.queryForObject(strQuery, Registration.class, jdbcTemplate);
		
		if (objRegistration != null) {
			
			String regtype = "select rt.jsondata->'sregtypename'->>'" + userInfo.getSlanguagetypecode()
							+ "' as sregtypename  ,rst.jsondata->'sregsubtypename'->>'" + userInfo.getSlanguagetypecode()
							+ "' as sregsubtypename from "
							+ "registrationtype rt,registrationsubtype rst where rst.nregtypecode="
							+ objRegistration.getNregtypecode() +" and rst.nregsubtypecode="
							+ objRegistration.getNregsubtypecode() +" and rt.nregtypecode=rst.nregtypecode"
							+ "  and rt.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ "  and rt.nsitecode="+ userInfo.getNmastersitecode()
							+ "  and rst.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ "  and rst.nsitecode="+ userInfo.getNmastersitecode();
			
			final RegistrationSubType objRegType = (RegistrationSubType) jdbcUtilityFunction.queryForObject(regtype,
					RegistrationSubType.class, jdbcTemplate);
			
			map = ftpUtilityFunction.FileViewUsingFtp(objFile.getSsystemfilename(), -1, userInfo, "", "");// Folder Name - master
			
			Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
			JSONObject actionType = new JSONObject();
			JSONObject jsonAuditObject = new JSONObject();
			
			value.put("sregtypename", objRegType.getSregtypename());
			value.put("sregsubtypename", objRegType.getSregsubtypename());
			value.putAll((Map<String, Object>) inputMap.get("viewFile"));
			
			List<Map<String, Object>> ins = new ArrayList<>();
			ins.add(value);
			jsonAuditObject.put("registration", ins);
			auditmap.put("nregtypecode", -1);
			auditmap.put("nregsubtypecode", -1);
			auditmap.put("ndesigntemplatemappingcode", 1);
			actionType.put("registration", "IDS_DOWNLOADFILE");
			
			auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);
		}
		return map;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> viewRegistrationSubSampleFile(RegistrationSample objFile, UserInfo userInfo,
			Map<String, Object> inputMap) throws Exception {
	
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> value = new LinkedHashMap<String, Object>();
		
		final String strQuery = "select r.* from registrationsample  rs, registration r where rs.ntransactionsamplecode = "
								+ objFile.getNtransactionsamplecode() 
								+ " and rs.npreregno=" + objFile.getNpreregno()
								+ " and r.npreregno=rs.npreregno "
								+ " and rs.nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and r.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and rs.nsitecode = "	+ userInfo.getNtranssitecode()
								+ " and r.nsitecode = "+ userInfo.getNtranssitecode();
		final Registration objRegistrationSample = (Registration) jdbcUtilityFunction.queryForObject(strQuery, Registration.class, jdbcTemplate);
	
		if (objRegistrationSample != null) {
			final String regtype = "select rt.jsondata->'sregtypename'->>'" + userInfo.getSlanguagetypecode()
								+ "' as sregtypename  ,rst.jsondata->'sregsubtypename'->>'" + userInfo.getSlanguagetypecode()
								+ "' as sregsubtypename from "
								+ "registrationtype rt,registrationsubtype rst where rst.nregtypecode="
								+ objRegistrationSample.getNregtypecode() + " and rst.nregsubtypecode="
								+ objRegistrationSample.getNregsubtypecode() + " and rt.nregtypecode=rst.nregtypecode"
								+ "  and rt.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ "  and rt.nsitecode="+ userInfo.getNmastersitecode()
								+ "  and rst.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ "  and rst.nsitecode="+ userInfo.getNmastersitecode();
			final RegistrationSubType objRegType = (RegistrationSubType) jdbcUtilityFunction.queryForObject(regtype,
					RegistrationSubType.class, jdbcTemplate);
			
			map = ftpUtilityFunction.FileViewUsingFtp(objFile.getSsystemfilename(), -1, userInfo, "", "");// Folder Name - master
			
			Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
			JSONObject actionType = new JSONObject();
			JSONObject jsonAuditObject = new JSONObject();
			
			value.put("sregtypename", objRegType.getSregtypename());
			value.put("sregsubtypename", objRegType.getSregsubtypename());
			value.putAll((Map<String, Object>) inputMap.get("viewFile"));
			
			List<Map<String, Object>> ins = new ArrayList<>();
			ins.add(value);
			jsonAuditObject.put("registrationsample", ins);
			auditmap.put("nregtypecode", -1);
			auditmap.put("nregsubtypecode", -1);
			auditmap.put("ndesigntemplatemappingcode", 1);
			actionType.put("registrationsample", "IDS_DOWNLOADFILE");
			
			auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);
		}
		return map;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> importRegistrationData(MultipartHttpServletRequest request, UserInfo userInfo)
			throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> reg = objectMapper.readValue(request.getParameter("Map"), Map.class);
		
		if ((boolean) reg.get("isFile")) {
			MultipartFile objmultipart = request.getFile("readFile");
			InputStream objinputstream = objmultipart.getInputStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int rowIndex = 0;
			byte[] buffer = new byte[1024];
			int len;
			while ((len = objinputstream.read(buffer)) > -1) {
				baos.write(buffer, 0, len);
			}
			baos.flush();
			InputStream is1 = new ByteArrayInputStream(baos.toByteArray());
			Workbook workbook = WorkbookFactory.create(is1);
			Sheet sheet = workbook.getSheetAt(0);
			// JSONArray dateList = new JSONArray(request.getParameter("datelist"));
			JSONObject objJsonUiData = new JSONObject();
			JSONObject objJsonData = new JSONObject();
			List<String> lstHeader = new ArrayList<>();

			List<Map<String, Object>> objJsonDataList = new ArrayList<>();
			List<Map<String, Object>> objJsonuiDataList = new ArrayList<>();
			for (Row row : sheet) {
				if (rowIndex > 0) {
					int cellIndex = 0;
					for (String field : lstHeader) // iteration over cell using for each loop
					{
						// int k = cellIndex;
						if (row.getCell(cellIndex) != null) {
						
							Cell cell = row.getCell(cellIndex);
							if (cell.getCellType() == CellType.STRING) {
								objJsonUiData.put(field, cell.getStringCellValue());
							} else if (cell.getCellType() == CellType.NUMERIC) {
								if (DateUtil.isCellDateFormatted(cell)) {
									SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
									objJsonUiData.put(field, sourceFormat.format(cell.getDateCellValue()));
								} else {
									objJsonUiData.put(field, cell.getNumericCellValue());
								}
							} else if (DateUtil.isCellDateFormatted(cell)) {
								if (cell.getDateCellValue() != null) {
									SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
									objJsonUiData.put(field, sourceFormat.format(cell.getDateCellValue()));
								} else {
									objJsonUiData.put(field, "");
								}
							}
							objJsonData.put(field, objJsonUiData.get(field));
						} else {

							objJsonUiData.put(field, "");
							objJsonData.put(field, "");

						}
						cellIndex++;
					}
					objJsonDataList.add(objJsonData.toMap());
					objJsonuiDataList.add(objJsonData.toMap());
					
				} else {
					//int cellIndex = 0;
					for (Cell cell : row) // iteration over cell using for each loop
					{
						String header = cell.getStringCellValue();
						lstHeader.add(header);
						//cellIndex++;
					}
				}
				rowIndex++;
			}

			final Registration registration = objectMapper.convertValue(reg.get("Registration"),
					new TypeReference<Registration>() {
					});
			List<Registration> registrationlist = new ArrayList<Registration>();
			for (int j = 0; j < objJsonDataList.size(); j++) {
				
				final Registration objRegCopy = SerializationUtils.clone(registration);
				
				JSONObject jObjondata = new JSONObject(registration.getJsondata());
				JSONObject jObjonuidata = new JSONObject(registration.getJsonuidata());
				Map<String, Object> objMapjsondata = objectMapper.readValue(jObjondata.toString(), Map.class);
				Map<String, Object> objMapjsonuidata = objectMapper.readValue(jObjonuidata.toString(), Map.class);
				objMapjsondata.putAll((Map<String, Object>) objJsonDataList.get(j));
				objMapjsonuidata.putAll((Map<String, Object>) objJsonDataList.get(j));
				objRegCopy.setJsondata(objMapjsondata);
				objRegCopy.setJsonuidata(objMapjsonuidata);
				registrationlist.add(objRegCopy);
				final List<Map<String, Object>> registrationconvert = objectMapper.convertValue(registrationlist,
						new TypeReference<List<Map<String, Object>>>() {
						});
				reg.put("Registration", registrationconvert);
			}
			return new ResponseEntity<>(preRegisterSampleImport(reg).getBody(), HttpStatus.OK);
		} else {
			final Registration registration = objectMapper.convertValue(reg.get("Registration"),
					new TypeReference<Registration>() {});
			final List<Registration> registrationlist = new ArrayList<Registration>();
			for (int j = 0; j < Integer.parseInt(reg.get("nsamplecount").toString()); j++) {
				final Registration objRegCopy = SerializationUtils.clone(registration);
				registrationlist.add(objRegCopy);
				final List<Map<String, Object>> registrationconvert = objectMapper.convertValue(registrationlist,
						new TypeReference<List<Map<String, Object>>>() {
						});
				reg.put("Registration", registrationconvert);
			}
			return new ResponseEntity<>(preRegisterSampleImport(reg).getBody(), HttpStatus.OK);
		}
	}
	
	public ResponseEntity<Object> preRegisterSampleImport(Map<String, Object> inputMap) throws Exception {

		 Map<String, Object> objmap1 = new HashMap<>();
		 final Map<String, Object> objmap = validateAndInsertSeqNoRegistrationDataImport(inputMap);

		if ((int) objmap.get("nflag") != 1) {
			if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
					.equals(objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
				inputMap.putAll(objmap);
				objmap1 = insertRegistrationImport(inputMap);
				return new ResponseEntity<>(objmap1, HttpStatus.OK);
			} else {
				if (objmap.containsKey("NeedConfirmAlert") && (Boolean) objmap.get("NeedConfirmAlert") == true) {
					// inputMap.putAll(objmap);
					return new ResponseEntity<>(objmap, HttpStatus.EXPECTATION_FAILED);
				} else {
					objmap1.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()));
					return new ResponseEntity<>(objmap1, HttpStatus.OK);
				}
			}
		} else {
			objmap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
			return new ResponseEntity<>(objmap, HttpStatus.OK);
		}

	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> validateAndInsertSeqNoRegistrationDataImport(Map<String, Object> inputMap)
			throws Exception {

		Map<String, Object> returnMap = new HashMap<String, Object>();
		final ObjectMapper objectMapper = new ObjectMapper();

		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		final List<TestGroupTest> listTest = objectMapper.convertValue(inputMap.get("testgrouptest"),
				new TypeReference<List<TestGroupTest>>() {});

		Boolean skipMethodValidity = null;
		if (inputMap.containsKey("skipmethodvalidity")) {
			skipMethodValidity = (Boolean) inputMap.get("skipmethodvalidity");
		}

		final String sntestGroupTestCode = stringUtilityFunction.fnDynamicListToString(listTest, "getNtestgrouptestcode");

		List<TestGroupTest> expiredMethodTestList = new ArrayList<TestGroupTest>();
		if (skipMethodValidity == false) {
			expiredMethodTestList = transactionDAOSupport.getTestByExpiredMethod(sntestGroupTestCode, userInfo);

		}
		if (expiredMethodTestList.isEmpty()) {
			String sQuery = " lock  table lockpreregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			sQuery = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			sQuery = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			sQuery = " lock  table lockcancelreject " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			final List<RegistrationSample> subSampleInputList = objectMapper.convertValue(inputMap.get("RegistrationSample"),
					new TypeReference<List<RegistrationSample>>() {});
			
			final List<Registration> registration = objectMapper.convertValue(inputMap.get("Registration"),
					new TypeReference<List<Registration>>() {});
			
			final List<TestGroupTest> testGroupTestInputList = objectMapper.convertValue(inputMap.get("testgrouptest"),
					new TypeReference<List<TestGroupTest>>() {});
			int subSampleCount = 0;
			int subSampleCount1 = 0;
			for (int j = 0; j < registration.size(); j++) {
				subSampleCount = subSampleInputList.size();
				subSampleCount1 += subSampleCount;
			}

			int testCount = 0;
			int testcount1 = 0;
			for (int j = 0; j < registration.size(); j++) {
				testCount = testGroupTestInputList.stream().mapToInt(
						testgrouptest -> testgrouptest.getNrepeatcountno() == 0 ? 1 : testgrouptest.getNrepeatcountno())
						.sum();
				testcount1 += testCount;
			}
			int parameterCount = 0;
			int parameterCount1 = 0;
			for (int j = 0; j < registration.size(); j++) {
				parameterCount = testGroupTestInputList.stream()
						.mapToInt(testgrouptest -> ((testgrouptest.getNrepeatcountno() == 0 ? 1
								: testgrouptest.getNrepeatcountno()) * testgrouptest.getNparametercount()))
						.sum();
				parameterCount1 += parameterCount;
			}
			List<Map<String, Object>> sampleCombinationUnique = (List<Map<String, Object>>) inputMap
					.get("samplecombinationunique");
			
			final Map<String, Object> map = projectDAOSupport.validateUniqueConstraintImport(sampleCombinationUnique,
					(List<Map<String, Object>>) inputMap.get("Registration"), userInfo, "create", Registration.class,
					"npreregno", false);
			if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
					.equals(map.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
//				// for normal preregister flow if we sent 1 means,order sample already
//				// preregitered so current sample also sample same order so store as sub sample
//				// in that specific order (only for clinic type)
				map.put("nflag", 2);
				return map;
			}
			JSONObject objJson = new JSONObject(registration.get(0).getJsondata());

			String manualOrderInsert = "";
			int nsampletypecode = (int) inputMap.get("nsampletypecode");
			if (registration.get(0).getJsonuidata().containsKey("Order Type")) {
				if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()
						&& ((int) ((JSONObject) objJson.get("Order Type")).getInt("value") == Enumeration.OrderType.INTERNAL.getOrderType()	
						|| (int) ((JSONObject) objJson.get("Order Type")).getInt("value") == Enumeration.OrderType.NA.getOrderType()))
					manualOrderInsert = ",'externalordertest','externalordersample'";
			}

			//RegistrationSample externalorderList = null;
			List<RegistrationHistory> lstStatus = null;
			
			if (registration.get(0).getJsonuidata().containsKey("Order Type")
					&& nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()
					&& (int) ((JSONObject) objJson.get("Order Type")).getInt("value") == Enumeration.OrderType.EXTERNAL.getOrderType()) {
	
				final String squerystatus = "select npreregno, ntransactionstatus from registrationhistory where"
												+ " nreghistorycode in (select max(nreghistorycode) from registrationhistory r"
												+ " where r.npreregno in (select npreregno from registration where "
												+ " nstatus=" +Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
												+ " and jsondata->>'OrderCodeData'='"
												+ registration.get(0).getJsondata().get("OrderCodeData") + "')  "
												+ " and nsitecode="	+ userInfo.getNtranssitecode() 
												+ " group by r.npreregno) "
												+ " and nstatus=" +Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
												+ " and nsitecode="	+ userInfo.getNtranssitecode() 
												+ " and  ntransactionstatus not in ("
												+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ","
												+ Enumeration.TransactionStatus.RELEASED.gettransactionstatus() + ","
												+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ")";
				lstStatus = jdbcTemplate.query(squerystatus, new RegistrationHistory());
				for (int p = 0; p < lstStatus.size(); p++) {
					if (lstStatus != null
							&& lstStatus.get(p).getNtransactionstatus() != Enumeration.TransactionStatus.CANCELED
									.gettransactionstatus()
							&& lstStatus.get(p).getNtransactionstatus() != Enumeration.TransactionStatus.REJECTED
									.gettransactionstatus()
							&& lstStatus.get(p).getNtransactionstatus() != Enumeration.TransactionStatus.RELEASED
									.gettransactionstatus()) {
						inputMap.put("nfilterstatus", lstStatus.get(0).getNtransactionstatus());
						inputMap.put("npreregno", Integer.toString(lstStatus.get(0).getNpreregno()));
						inputMap.put("npreregnocount", lstStatus.get(0).getNpreregno());
						inputMap.put("masterData", inputMap.get("DataRecordMaster"));
						return (Map<String, Object>) externalOrderSupport.externalOrderSampleExtisting(inputMap);
					}
				}

			}
			final String strSelectSeqno = "select stablename,nsequenceno from seqnoregistration  where stablename "
					+ "in ('registration','registrationhistory','registrationparameter','registrationsample',"
					+ "'registrationsamplehistory','registrationtest','registrationdecisionhistory','registrationtesthistory'"
					+ manualOrderInsert + ") and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

//			final List<?> lstMultiSeqNo = jdbcTemplate.query(strSelectSeqno, new SeqNoRegistration());
//
//			final List<SeqNoRegistration> lstSeqNoReg = (List<SeqNoRegistration>) lstMultiSeqNo.get(0);
//
//			returnMap = lstSeqNoReg.stream().collect(Collectors.toMap(SeqNoRegistration::getStablename,
//					SeqNoRegistration -> SeqNoRegistration.getNsequenceno()));
			
			final List<SeqNoRegistration> lstSeqNo = jdbcTemplate.query(strSelectSeqno, new SeqNoRegistration());
			
			returnMap = lstSeqNo.stream().collect(Collectors.toMap(SeqNoRegistration::getStablename,
					SeqNoRegistration -> SeqNoRegistration.getNsequenceno()));

			String strSeqnoUpdate = "Update seqnoregistration set nsequenceno = "
									+ ((int) returnMap.get("registration") + registration.size())
									+ " where stablename = 'registration';" + "Update seqnoregistration set nsequenceno = "
									+ ((int) returnMap.get("registrationdecisionhistory") + registration.size())
									+ " where stablename = 'registrationdecisionhistory';"
									+ " Update seqnoregistration set nsequenceno = "
									+ ((int) returnMap.get("registrationhistory") + registration.size())
									+ " where stablename = 'registrationhistory';" + "Update seqnoregistration set nsequenceno = "
									+ ((int) returnMap.get("registrationparameter") + parameterCount1)
									+ " where stablename = 'registrationparameter';" + "Update seqnoregistration set nsequenceno = "
									+ ((int) returnMap.get("registrationsample") + subSampleCount1)
									+ " where stablename = 'registrationsample';" + "Update seqnoregistration set nsequenceno = "
									+ ((int) returnMap.get("registrationsamplehistory") + subSampleCount1)
									+ " where stablename = 'registrationsamplehistory';" + "Update seqnoregistration set nsequenceno = "
									+ ((int) returnMap.get("registrationtest")// + TestGroupTests.size()
											+ testcount1)
									+ " where stablename = 'registrationtest' ;" + "Update seqnoregistration set nsequenceno = "
									+ ((int) returnMap.get("registrationtesthistory") // + TestGroupTests.size()
											+ testcount1)
									+ " where stablename = 'registrationtesthistory';";

			jdbcTemplate.execute(strSeqnoUpdate);

			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.SUCCESS.getreturnstatus());

		} else {
			final String expiredMethod = stringUtilityFunction.fnDynamicListToString(expiredMethodTestList, "getSmethodname");
			returnMap.put("NeedConfirmAlert", true);
			final String message = commonFunction.getMultilingualMessage("IDS_TESTWITHEXPIREDMETHODCONFIRM",
					userInfo.getSlanguagefilename());
			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					message.concat(" " + expiredMethod + " ?"));
		}
		returnMap.put("nflag", 2);
		return returnMap;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map<String, Object> insertRegistrationImport(Map<String, Object> inputMap) throws Exception {

		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		Map<String, Object> returnMap = new HashMap<>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObject = new JSONObject();

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});

		final List<Registration> registration = objMapper.convertValue(inputMap.get("Registration"),
				new TypeReference<List<Registration>>() {});

		final List<RegistrationSample> registrationSample = objMapper.convertValue(inputMap.get("RegistrationSample"),
				new TypeReference<List<RegistrationSample>>() {});

		final List<TestGroupTest> tgtTestInputList = objMapper.convertValue(inputMap.get("testgrouptest"),
				new TypeReference<List<TestGroupTest>>() {});

		final List<String> dateList = objMapper.convertValue(inputMap.get("DateList"), new TypeReference<List<String>>() {});

		final List<Map<String, Object>> sampleDateConstraint = objMapper
				.convertValue(inputMap.get("sampledateconstraints"), new TypeReference<List<Map<String, Object>>>() {});

		final List<Map<String, Object>> subSampleDateConstraint = objMapper
				.convertValue(inputMap.get("subsampledateconstraints"), new TypeReference<List<Map<String, Object>>>() {});

		final List<String> SubSampledateList = objMapper.convertValue(inputMap.get("subsampleDateList"),
				new TypeReference<List<String>>() {});

		int nregtypecode = (int) inputMap.get("nregtypecode");
		int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
		int nsampletypecode = (int) inputMap.get("nsampletypecode");
		int npreregno = (int) inputMap.get("registration");
		int nreghistorycode = (int) inputMap.get("registrationhistory");
		int nregistrationparametercode = (int) inputMap.get("registrationparameter");
		int nregsamplecode = (int) inputMap.get("registrationsample");
		int nregsamplehistorycode = (int) inputMap.get("registrationsamplehistory");
		int napproveconfversioncode = (int) inputMap.get("napproveconfversioncode");
		int ntransactiontestcode = (int) inputMap.get("registrationtest");
		int ntesthistorycode = (int) inputMap.get("registrationtesthistory");
		int nregdecisionhistorycode = (int) inputMap.get("registrationdecisionhistory");
//		int seqordersample = -1;
//		int seqordertest = -1;

		int nage = 0;
		int ngendercode = 0;
		String sQuery = "";
		if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()) {
			nage = (int) inputMap.get("AgeData");
			ngendercode = (int) inputMap.get("ngendercode");
			String sYears="SELECT CURRENT_DATE - TO_DATE('"+inputMap.get("sDob")+"','"+userInfo.getSpgsitedatetime()+"' )";
		    long ageInDays= jdbcTemplate.queryForObject(sYears,Long.class);
			
			sQuery = "json_build_object('ntransactionresultcode'," + nregistrationparametercode
					+ "+RANK() over(order by tgtp.ntestgrouptestparametercode),'ntransactiontestcode',"
					+ ntransactiontestcode + "+DENSE_RANK() over(order by tgtp.ntestgrouptestcode),'npreregno',"
					+ npreregno + ")::jsonb || " + " case when tgtp.nparametertypecode="
					+ Enumeration.ParameterType.NUMERIC.getparametertype() + " then case when  " + " tgtcs.ngendercode="
					+ ngendercode + " and " + ageInDays
					//+ " between tgtcs.nfromage and tgtcs.ntoage"
					+ " between case when tgtcs.nfromageperiod="+Enumeration.Period.Years.getPeriod()+""
				    + " then (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(years => tgtcs.nfromage)))::int)"
					+ " when tgtcs.nfromageperiod="+Enumeration.Period.Month.getPeriod()+" then "
					+ " (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(months => tgtcs.nfromage)))::int) "
					+ " else (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(days => tgtcs.nfromage)))::int) end "
					+ " and case when tgtcs.ntoageperiod="+Enumeration.Period.Years.getPeriod()+" then (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(years => tgtcs.ntoage)))::int) "
					+ " when tgtcs.ntoageperiod="+Enumeration.Period.Month.getPeriod()+" then (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(months => tgtcs.ntoage)))::int) "
					+ " else (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(days => tgtcs.ntoage)))::int) end "
					+ " then jsonb_build_object('nfromage',"
					+ " tgtcs.nfromage,'ntoage',tgtcs.ntoage,'ngendercode',tgtcs.ngendercode ,'ngradecode',tgtcs.ngradecode,"
					+ " 'sminb',case when tgtcs.slowb='null' then NULL else tgtcs.slowb end,"
					+ " 'smina',case when tgtcs.slowa='null' then NULL else tgtcs.slowa end,"
					+ " 'smaxa',case when tgtcs.shigha='null' then NULL else tgtcs.shigha end,"
					+ " 'smaxb',case when tgtcs.shighb='null' then NULL else tgtcs.shighb end,"
					+ " 'sminlod',case when tgtcs.sminlod='null' then NULL else tgtcs.sminlod end,"
					+ " 'smaxlod',case when tgtcs.smaxlod='null' then NULL else tgtcs.smaxlod end,"
					+ " 'sminloq',case when tgtcs.sminloq='null' then NULL else tgtcs.sminloq end,"
					+ " 'smaxloq',case when tgtcs.smaxloq='null' then NULL else tgtcs.smaxloq end,"
					+ " 'sresultvalue',case when tgtcs.sresultvalue='null' then NULL else tgtcs.sresultvalue end,"
					+ " 'nroundingdigits',tgtp.nroundingdigits,'sresult',NULL,'sfinal',NULL,'stestsynonym',"
					+ " concat(tgt.stestsynonym,'[1][0]'),'sparametersynonym',tgtp.sparametersynonym,'nresultaccuracycode',tgtp.nresultaccuracycode,'sresultaccuracyname', ra.sresultaccuracyname)::jsonb else "
					+ " jsonb_build_object('nfromage',tgtcs.nfromage,'ntoage',tgtcs.ntoage,'ngendercode',tgtcs.ngendercode,'ngradecode',tgtcs.ngradecode,"
					+ " 'sminb',case when tgtcs.slowb='null' then NULL else tgtcs.slowb end,"
					+ " 'smina',case when tgtcs.slowa='null' then NULL else tgtcs.slowa end,"
					+ " 'smaxa',case when tgtcs.shigha='null' then NULL else tgtcs.shigha end,"
					+ " 'smaxb',case when tgtcs.shighb='null' then NULL else tgtcs.shighb end,"
					+ " 'sminlod',case when tgtcs.sminlod='null' then NULL else tgtcs.sminlod end,"
					+ " 'smaxlod',case when tgtcs.smaxlod='null' then NULL else tgtcs.smaxlod end,"
					+ " 'sminloq',case when tgtcs.sminloq='null' then NULL else tgtcs.sminloq end,"
					+ " 'smaxloq',case when tgtcs.smaxloq='null' then NULL else tgtcs.smaxloq end,"
					+ " 'sresultvalue',case when tgtcs.sresultvalue='null' then NULL else tgtcs.sresultvalue end,"
					+ "'nroundingdigits',tgtp.nroundingdigits,'sresult',NULL,'sfinal',NULL,'stestsynonym',"
					+ " concat(tgt.stestsynonym,'[1][0]'),"
					+ " 'sparametersynonym',tgtp.sparametersynonym,'nresultaccuracycode',tgtp.nresultaccuracycode,'sresultaccuracyname', ra.sresultaccuracyname )::jsonb end else jsonb_build_object("
					+ " 'sminb',case when tgtcs.slowb='null' then NULL else tgtcs.slowb end,"
					+ " 'smina',case when tgtcs.slowa='null' then NULL else tgtcs.slowa end,"
					+ " 'smaxa',case when tgtcs.shigha='null' then NULL else tgtcs.shigha end,"
					+ " 'smaxb',case when tgtcs.shighb='null' then NULL else tgtcs.shighb end,"
					+ " 'sresultvalue',case when tgtcs.sresultvalue='null' then NULL else tgtcs.sresultvalue end,"
					+ " 'nroundingdigits',tgtp.nroundingdigits,'sresult',NULL,'sfinal',NULL,'stestsynonym',concat(tgt.stestsynonym,'[1][0]'),"
					+ " 'sparametersynonym',tgtp.sparametersynonym,'nresultaccuracycode',tgtp.nresultaccuracycode,'sresultaccuracyname', ra.sresultaccuracyname )::jsonb end jsondata, "
					+ userInfo.getNtranssitecode() + " nsitecode,"
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " nstatus from resultaccuracy ra,testgrouptest tgt inner join "
					+ " testgrouptestparameter tgtp  on tgt.ntestgrouptestcode = tgtp.ntestgrouptestcode "
					+ " left outer join testgrouptestclinicalspec tgtcs on tgtcs.ntestgrouptestparametercode = tgtp.ntestgrouptestparametercode "
					+ " and tgtcs.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and tgtcs.ngendercode=" + ngendercode + " and " + ageInDays
					+ " between case when tgtcs.nfromageperiod="+Enumeration.Period.Years.getPeriod()+""
				    + " then (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(years => tgtcs.nfromage)))::int)"
					+ " when tgtcs.nfromageperiod="+Enumeration.Period.Month.getPeriod()+" then "
					+ " (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(months => tgtcs.nfromage)))::int) "
					+ " else (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(days => tgtcs.nfromage)))::int) end "
					+ " and case when tgtcs.ntoageperiod="+Enumeration.Period.Years.getPeriod()+" then (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(years => tgtcs.ntoage)))::int) "
					+ " when tgtcs.ntoageperiod="+Enumeration.Period.Month.getPeriod()+" then (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(months => tgtcs.ntoage)))::int) "
					+ " else (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(days => tgtcs.ntoage)))::int) end ";
					//+ " between tgtcs.nfromage and tgtcs.ntoage ";

			returnMap.put("nAge", nage);
			returnMap.put("nGendercode", ngendercode);
		} else {
			sQuery = " json_build_object('ntransactionresultcode'," + nregistrationparametercode
					+ "+RANK() over(order by tgtp.ntestgrouptestparametercode),'ntransactiontestcode',"
					+ ntransactiontestcode + "+DENSE_RANK() over(order by tgtp.ntestgrouptestcode),'npreregno',"
					+ npreregno
					+ ",'sminlod',tgtnp.sminlod,'smaxlod',tgtnp.smaxlod,'sminb',tgtnp.sminb,'smina',tgtnp.smina,"
					+ "'smaxa',tgtnp.smaxa,'smaxb',tgtnp.smaxb,'sminloq',tgtnp.sminloq,'smaxloq',tgtnp.smaxloq,"
					+ "'sdisregard',tgtnp.sdisregard,'sresultvalue',tgtnp.sresultvalue,'ngradecode',tgtnp.ngradecode,"
					+ "'nroundingdigits',tgtp.nroundingdigits,'sresult',NULL,'sfinal',NULL,'stestsynonym',concat(tgt.stestsynonym,'[1][0]'),"
					+ "'sparametersynonym',tgtp.sparametersynonym,'nresultaccuracycode',tgtp.nresultaccuracycode,'sresultaccuracyname', ra.sresultaccuracyname)::jsonb jsondata," + userInfo.getNtranssitecode()
					+ " nsitecode," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
					+ " from resultaccuracy ra,testgrouptest tgt,testgrouptestparameter tgtp";
		}

		inputMap.put("nsitecode", userInfo.getNtranssitecode());

//		List<TestGroupTestParameter> lstTestGroupTestParameter = new ArrayList<TestGroupTestParameter>();
//
//		if (!TestGroupTests.isEmpty()) {
//			String strParamterList = "select tgtp.ntestgrouptestcode,"
//					+ " tgtp.ntestgrouptestparametercode,tgtp.ntestparametercode,tgtp.nroundingdigits,"
//					+ " tgtp.nresultmandatory,tgtp.nreportmandatory,COALESCE(tgf.ntestgrouptestformulacode,-1)"
//					+ " ntestgrouptestformulacode,"
//					+ " tgtp.nunitcode,tgtp.nparametertypecode,tgtnp.sminlod,tgtnp.smaxlod,tgtnp.sminb,"
//					+ " tgtnp.smina,tgtnp.smaxa,tgtnp.smaxb,tgtnp.sminloq,tgtnp.smaxloq,"
//					+ " tgtnp.sdisregard,tgtnp.sresultvalue"
//					+ " from testgrouptest tgt,testgrouptestparameter tgtp left outer join "
//					+ " testgrouptestnumericparameter tgtnp on tgtnp.ntestgrouptestparametercode=tgtp.ntestgrouptestparametercode "
//					+ " left outer join testgrouptestformula tgf on tgtnp.ntestgrouptestparametercode=tgf.ntestgrouptestparametercode and tgf.ntransactionstatus = "
//					+ Enumeration.TransactionStatus.YES.gettransactionstatus()
//					+ " where tgt.ntestgrouptestcode=tgtp.ntestgrouptestcode and tgt.nstatus=1 and tgtp.nstatus=1"
//					+ " and tgtp.ntestgrouptestcode in (" + strTestList + ");";
//			lstTestGroupTestParameter = jdbcTemplate.query(strParamterList, new TestGroupTestParameter());
//		}

		String strRegistrationInsert = "";
		String strRegistrationHistory = "";
		String strRegistrationArno = "";
		String strRegistrationSample = "";
		String strRegistrationSampleHistory = "";
		String strRegistrationSampleArno = "";
		String strDecisionHistory = "";
		String strRegistrationStatusBlink = "";
		//String externalOrderSampleQuery = "";
		//String externalOrderTestQuery = "";
		int sampleCount = registrationSample.size();
		int sampleMainCount = registration.size();
		boolean statusflag = false;
		ntransactiontestcode++;
		nregistrationparametercode++;
		ntesthistorycode++;

		StringJoiner joinerSample = new StringJoiner(",");
		StringJoiner joinerSampleMain = new StringJoiner(",");
		for (int k = 0; k < sampleMainCount; k++) {

			++npreregno;
			++nreghistorycode;
			++nregdecisionhistorycode;
			joinerSampleMain.add(String.valueOf(npreregno));
			JSONObject jsoneditRegistration = new JSONObject(registration.get(k).getJsondata());
			JSONObject jsonuiRegistration = new JSONObject(registration.get(k).getJsonuidata());
//			if (registration.get(k).getJsondata().containsKey("Order Type")) {
//				if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()
//						&& ((int) ((JSONObject) jsoneditRegistration.get("Order Type")).getInt("value") == Enumeration.OrderType.INTERNAL.getOrderType()
//								|| (int) ((JSONObject) jsoneditRegistration.get("Order Type")).getInt("value") == Enumeration.OrderType.NA.getOrderType())) {
//					seqordersample = (int) inputMap.get("externalordersample");
//					seqordertest = (int) inputMap.get("externalordertest");
//				}
//			}
			if (!dateList.isEmpty()) {

				jsoneditRegistration = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsoneditRegistration, dateList, false,
						userInfo);
				jsonuiRegistration = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonuiRegistration, dateList, false,
						userInfo);
				final Map<String, Object> obj = commonFunction.validateDynamicDateContraints(jsonuiRegistration,
						sampleDateConstraint, userInfo);
				if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
						.equals((String) obj.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
					return obj;
				}
			}

			jsonuiRegistration.put("npreregno", npreregno);
			jsonuiRegistration.put("nsampletypecode", nsampletypecode);
			jsonuiRegistration.put("nregtypecode", nregtypecode);
			jsonuiRegistration.put("nregsubtypecode", nregsubtypecode);
			jsonuiRegistration.put("nproductcatcode", registration.get(k).getNproductcatcode());
			jsonuiRegistration.put("nproductcode", registration.get(k).getNproductcode());
			jsonuiRegistration.put("nprojectmastercode", registration.get(k).getNprojectmastercode());
			jsonuiRegistration.put("ninstrumentcatcode", registration.get(k).getNinstrumentcatcode());
			jsonuiRegistration.put("ninstrumentcode", registration.get(k).getNinstrumentcode());
			jsonuiRegistration.put("nmaterialcatcode", registration.get(k).getNmaterialcatcode());
			jsonuiRegistration.put("nmaterialcode", registration.get(k).getNmaterialcode());
			jsonuiRegistration.put("ntemplatemanipulationcode", registration.get(k).getNtemplatemanipulationcode());
			jsonuiRegistration.put("nallottedspeccode", registration.get(k).getNallottedspeccode());
			jsonuiRegistration.put("ndesigntemplatemappingcode", registration.get(k).getNdesigntemplatemappingcode());
			jsonuiRegistration.put("napproveconfversioncode", napproveconfversioncode);
			jsonuiRegistration.put("napproveconfversioncode", napproveconfversioncode);
			if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()) {
				jsonuiRegistration.put("ngendercode", registration.get(k).getNgendercode());
			}

			strRegistrationInsert = strRegistrationInsert + "(" + npreregno + "," + nsampletypecode + "," + nregtypecode
					+ "," + nregsubtypecode + "," + registration.get(k).getNproductcatcode() + ","
					+ registration.get(k).getNproductcode() + "," + registration.get(k).getNinstrumentcatcode() + ","
					+ registration.get(k).getNinstrumentcode() + "," + registration.get(k).getNmaterialcatcode() + ","
					+ registration.get(k).getNmaterialcode() + "," + registration.get(k).getNtemplatemanipulationcode()
					+ "," + registration.get(k).getNallottedspeccode() + "," + userInfo.getNtranssitecode() + ",'"
					+ stringUtilityFunction.replaceQuote(jsoneditRegistration.toString()) + "'::JSONB,'"
					+ stringUtilityFunction.replaceQuote(jsonuiRegistration.toString()) + "'::JSONB,"
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
					+ registration.get(k).getNdesigntemplatemappingcode() + ","
					+ registration.get(k).getNregsubtypeversioncode() + ","
					+ registration.get(k).getNprojectmastercode() + "," + registration.get(k).getNisiqcmaterial()
					+ ","+registration.get(k).getNprotocolcode()+"),";

			strRegistrationHistory = strRegistrationHistory + "(" + nreghistorycode + "," + npreregno + ","
					+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + ",'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNusercode() + "," + userInfo.getNuserrole()
					+ "," + userInfo.getNdeputyusercode() + "," + userInfo.getNdeputyuserrole() + ",'"
					+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "'," + userInfo.getNtranssitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode() + "),";

			strDecisionHistory = strDecisionHistory + "(" + nregdecisionhistorycode + "," + npreregno + ","
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo)
					+ "'," + userInfo.getNusercode() + "," + userInfo.getNuserrole() + ","
					+ userInfo.getNdeputyusercode() + "," + userInfo.getNdeputyuserrole() + ",'"
					+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "'," + userInfo.getNtranssitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode() + "),";

			strRegistrationArno = strRegistrationArno + "(" + npreregno + ",'-'," + napproveconfversioncode + ","
					+ userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "),";

			strRegistrationStatusBlink = strRegistrationStatusBlink + "(" + npreregno + "," + statusflag + ","
					+ userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "),";

			// StringJoiner joinerSample = new StringJoiner(",");
			for (int i = sampleCount - 1; i >= 0; i--) {
				nregsamplecode++;
				nregsamplehistorycode++;
//			nregdecisionhistorycode++;
	//			seqordersample++;
				joinerSample.add(String.valueOf(nregsamplecode));

				JSONObject jsoneditObj = new JSONObject(registrationSample.get(i).getJsondata());
				JSONObject jsonuiObj = new JSONObject(registrationSample.get(i).getJsonuidata());
				if (!SubSampledateList.isEmpty()) {
					jsoneditObj = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsoneditObj, SubSampledateList, false,
							userInfo);
					jsonuiObj = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonuiObj, SubSampledateList, false, userInfo);
					final Map<String, Object> obj = commonFunction.validateDynamicDateContraints(jsonuiObj,
							subSampleDateConstraint, userInfo);
					if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
							.equals((String) obj.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
						return obj;
					}
				}

				jsonuiObj.put("ntransactionsamplecode", nregsamplecode);
				jsonuiObj.put("npreregno", npreregno);
				jsonuiObj.put("nspecsampletypecode", registrationSample.get(i).getNspecsampletypecode());
				jsonuiObj.put("ncomponentcode", registrationSample.get(i).getNcomponentcode());

				strRegistrationSample = strRegistrationSample + " (" + nregsamplecode + "," + npreregno + ",'"
						+ registrationSample.get(i).getNspecsampletypecode() + "',"
						+ registrationSample.get(i).getNcomponentcode() + ",'" + stringUtilityFunction.replaceQuote(jsoneditObj.toString())
						+ "'::JSONB,'" + stringUtilityFunction.replaceQuote(jsonuiObj.toString()) + "'::JSONB," + userInfo.getNtranssitecode()
						+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
				// jsonsampleArray.put(jsonuiObj);
				strRegistrationSampleHistory = strRegistrationSampleHistory + "(" + nregsamplehistorycode + ","
						+ nregsamplecode + "," + npreregno + ","
						+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + ",'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNusercode() + "," + userInfo.getNuserrole()
						+ "," + userInfo.getNdeputyusercode() + "," + userInfo.getNdeputyuserrole() + ",'"
						+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "'," + userInfo.getNtranssitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode()
						+ "),";

				strRegistrationSampleArno = strRegistrationSampleArno + "(" + nregsamplecode + "," + npreregno + ",'-'"
						+ "," + userInfo.getNtranssitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";

				List<RegistrationSample> comp = new ArrayList();
				comp.add(registrationSample.get(i));
				List<TestGroupTest> lsttest1 = tgtTestInputList.stream()
						.filter(x -> x.getSlno() == comp.get(0).getSlno()).collect(Collectors.toList());

				final String stestcode = lsttest1.stream().map(x -> String.valueOf(x.getNtestgrouptestcode()))
						.distinct().collect(Collectors.joining(","));

//				if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()) {
//					if (registration.get(k).getJsondata().containsKey("Order Type")) {
//						if (((int) ((JSONObject) jsoneditRegistration.get("Order Type")).getInt("value") == 1
//								|| (int) ((JSONObject) jsoneditRegistration.get("Order Type")).getInt("value") == -1)
//								&& registration.get(k).getJsonuidata().containsKey("Order")) {
//
//							int externalordercode = (int) ((JSONObject) jsoneditRegistration.get("Order"))
//									.getInt("value");
//
//							externalOrderSampleQuery += " (" + seqordersample + "," + externalordercode + ","
//									+ registrationSample.get(i).getNcomponentcode() + "," + 0 + "," + -1 + ",'"
//									+ jsonuiObj.get("sampleorderid") + "','" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + ""
//									+ userInfo.getNtranssitecode() + ", "
//									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
//									+ userInfo.getNtranssitecode() + "),";
//
//							for (TestGroupTest test : lsttest1) {
//								seqordertest++;
//								externalOrderTestQuery += "(" + seqordertest + "," + seqordersample + ","
//										+ externalordercode + "," + test.getNtestpackagecode() + ","
//										+ test.getNcontainertypecode() + " ," + test.getNtestcode() + ",'"
//										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + "," + userInfo.getNtranssitecode() + ","
//										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
//										+ userInfo.getNtranssitecode() + "),";
//							}
//						}
//
//					}
//				}

				if (!lsttest1.isEmpty()) {

					// FRS-00410- To add test based on replicate count defined in testgroup test

					final Map<String, Object> testHistoryParameterMap = insertTestHistoryParameter(stestcode, sQuery,
							userInfo, npreregno, nregsamplecode, ntransactiontestcode, ntesthistorycode,
							nregistrationparametercode, new ArrayList<Integer>(),inputMap);
					ntransactiontestcode = (int) testHistoryParameterMap.get("ntransactiontestcode");
					ntesthistorycode = (int) testHistoryParameterMap.get("ntesthistorycode");
					nregistrationparametercode = (int) testHistoryParameterMap.get("ntransactionresultcode");
				}

			}

			if (registration.get(k).getNsampletypecode() == Enumeration.SampleType.INSTRUMENT.getType()) {

				String query = "select * from instrumentcalibration where  ncalibrationstatus="
						+ Enumeration.TransactionStatus.UNDERCALIBIRATION.gettransactionstatus()
						+ " and dopendate is null and dclosedate is null and ninstrumentcode="
						+ registration.get(k).getNinstrumentcode()
						+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsitecode="+userInfo.getNtranssitecode();

				final InstrumentCalibration instrumentCalibration = (InstrumentCalibration) jdbcUtilityFunction.queryForObject(query,
						InstrumentCalibration.class, jdbcTemplate);

				query = "update instrumentcalibration set npreregno=" + npreregno + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ninstrumentcalibrationcode="
						+ instrumentCalibration.getNinstrumentcalibrationcode()
						+ " and nsitecode="+userInfo.getNtranssitecode();
				jdbcTemplate.execute(query);

			}
		}
		strRegistrationInsert = "Insert into registration (npreregno, nsampletypecode, nregtypecode, nregsubtypecode, nproductcatcode, "
				+ "nproductcode,ninstrumentcatcode,ninstrumentcode,nmaterialcatcode,nmaterialcode, ntemplatemanipulationcode, "
				+ " nallottedspeccode, nsitecode,jsondata,jsonuidata, nstatus,ndesigntemplatemappingcode,nregsubtypeversioncode, "
				+ " nprojectmastercode, nisiqcmaterial,nprotocolcode) values "
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
				+ "nsitecode,nstatus) values " + strRegistrationSample.substring(0, strRegistrationSample.length() - 1)
				+ ";";
		jdbcTemplate.execute(strRegistrationSample);

		strRegistrationSampleHistory = "Insert into registrationsamplehistory(nsamplehistorycode, ntransactionsamplecode, npreregno, ntransactionstatus, "
				+ "dtransactiondate, nusercode, nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments,nsitecode,nstatus,noffsetdtransactiondate,ntransdatetimezonecode ) values "
				+ strRegistrationSampleHistory.substring(0, strRegistrationSampleHistory.length() - 1) + ";";
		jdbcTemplate.execute(strRegistrationSampleHistory);

		strRegistrationSampleArno = "Insert into registrationsamplearno (ntransactionsamplecode,npreregno,ssamplearno,nsitecode,nstatus) values "
				+ strRegistrationSampleArno.substring(0, strRegistrationSampleArno.length() - 1) + ";";
		jdbcTemplate.execute(strRegistrationSampleArno);

		strRegistrationStatusBlink = "Insert into registrationflagstatus (npreregno,bflag,nsitecode,nstatus) "
				+ " values" + strRegistrationStatusBlink.substring(0, strRegistrationStatusBlink.length() - 1) + ";";
		jdbcTemplate.execute(strRegistrationStatusBlink);

		inputMap.put("npreregno", joinerSampleMain.toString());
		// String.valueOf(npreregno));
		// inputMap.put("nsubsample",3);
		returnMap = registrationDAOSupport.getDynamicRegistration(inputMap, userInfo);
		returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
				Enumeration.ReturnStatus.SUCCESS.getreturnstatus());

		jsonAuditObject.put("registration", (List<Map<String, Object>>) returnMap.get("RegistrationGetSample"));
		actionType.put("registration", "IDS_PREREGISTER");

		if ((boolean) inputMap.get("nneedsubsample")) {
			jsonAuditObject.put("registrationsample",
					(List<Map<String, Object>>) returnMap.get("RegistrationGetSubSample"));
			actionType.put("registrationsample", "IDS_PREREGISTERSAMPLE");

			inputMap.put("ntransactionsamplecode", null);

			Map<String, Object> objMap = (Map<String, Object>) transactionDAOSupport.getRegistrationTestAudit(inputMap, userInfo).getBody();

			jsonAuditObject.put("registrationtest", (List<Map<String, Object>>) objMap.get("RegistrationGetTest"));
		} else {
			jsonAuditObject.put("registrationtest", (List<Map<String, Object>>) returnMap.get("RegistrationGetTest"));
		}

		auditmap.put("nregtypecode", nregtypecode);
		auditmap.put("nregsubtypecode", nregsubtypecode);
		auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));

		actionType.put("registrationtest", "IDS_PREREGISTERTEST");
		
		auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);

		// Portal Status Update
		inputMap.put("ssamplecode", joinerSample.toString());
		
		returnMap.putAll((Map<String, Object>) externalOrderSupport.sampleOrderUpdate(inputMap, String.valueOf(npreregno), userInfo,
				Enumeration.TransactionStatus.INITIATED.gettransactionstatus(),
				Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus(), true).getBody());

		return returnMap;

	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> importRegistrationSample(MultipartHttpServletRequest request, UserInfo userInfo)
			throws Exception {
		
		final ObjectMapper objMapper = new ObjectMapper();
		
		Map<String, Object> objMap1 = objMapper.readValue(request.getParameter("Map"), Map.class);
					
		final MultipartFile multipartFile = request.getFile("registrationImportFile");
		final InputStream ins = multipartFile.getInputStream();
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final Registration registration = objMapper.convertValue(objMap1.get("Registration"),
					new TypeReference<Registration>() {});

		final List<Registration> lstRegistration = new ArrayList<Registration>();
		final List<RegistrationSample> lstRegistrationSample = new ArrayList<RegistrationSample>();

		List<String> lstSampHeaderString = objMapper.convertValue(objMap1.get("SampleFieldsString"),
				new TypeReference<List<String>>() {});

		List<String> lstExportList = objMapper.convertValue(objMap1.get("exportFields"),
				new TypeReference<List<String>>() {});
		
//		List<String> sampleDateList = objMapper.convertValue(objMap1.get("DateList"),
//				new TypeReference<List<String>>() {});
//		
//		List<String> subSampleDateList = objMapper.convertValue(objMap1.get("subsampleDateList"),
//				new TypeReference<List<String>>() {
//				});

		boolean nneedsubsample = (boolean) objMap1.get("nneedsubsample");
		boolean specBasedComponnet = (boolean) objMap1.get("specBasedComponnet");
		
		Map<String, Object> responseMap = new LinkedHashMap<String, Object>();
		String responseString = "";
		String sQuery = "";
		int nspecsampletypecode = -1;

		if (!specBasedComponnet) {
			nspecsampletypecode = (int) objMap1.get("nspecsampletypecode");
		}
		List<Map<String, Object>> exportFieldProperties = objMapper.convertValue(objMap1.get("exportFieldProperties"),
				new TypeReference<List<Map<String, Object>>>() {
				});
//		List<Map<String, Object>> mandatoryFields = objMapper.convertValue(objMap1.get("MandatoryList"),
//				new TypeReference<List<Map<String, Object>>>() {
//				});
//
//		List<Map<String, Object>> comboComponent1 = objMapper.convertValue(objMap1.get("comboComponent"),
//				new TypeReference<List<Map<String, Object>>>() {
//				});

		List<Map<String,Object>> lstTest = new ArrayList<Map<String,Object>>();

		int nallottedspeccode = registration.getNallottedspeccode();
		// ALPD-3596 Start
		boolean needExecute = false;
		if ((int) objMap1.get("importTest") == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
			if (!objMap1.get("TestGroupTestCode").equals("")) {
				needExecute = true;
				sQuery = " and tg.ntestgrouptestcode in (" + objMap1.get("TestGroupTestCode") + ") ";
			}
		}
		// ALPD-3596 End

		final byte[] bytes = new byte[1040];
		int len;
		while ((len = ins.read(bytes)) > -1) {

			baos.write(bytes, 0, len);

		}
		baos.flush();
		final InputStream inss = new ByteArrayInputStream(baos.toByteArray());
		final Workbook wb = WorkbookFactory.create(inss);
		final Sheet sheet = wb.getSheetAt(0);
		final List<String> lstHeader = new ArrayList<>();
		final JSONObject objJsonUiData = new JSONObject(registration.getJsonuidata());
		final JSONObject objJsonData = new JSONObject(registration.getJsondata());

		final JSONObject objJsonUiDatasub = new JSONObject();
		final JSONObject objJsonDatasub = new JSONObject();

		final List<String> mrgFields = new ArrayList<String>();
		final List<String> mrgColumn = new ArrayList<String>();
		final Map<String, String> objMap = new HashMap<String, String>();

		for (int i = 0; i < sheet.getNumMergedRegions(); i++) {

			final CellRangeAddress cellAdd = sheet.getMergedRegion(i);

			String val = "RS" + String.valueOf(cellAdd.getFirstRow()) + "RE" + String.valueOf(cellAdd.getLastRow())
					+ "CS" + String.valueOf(cellAdd.getFirstColumn()) + "CE" + String.valueOf(cellAdd.getLastColumn());
			mrgFields.add(val);
			int fr = cellAdd.getFirstRow();
			int lr = cellAdd.getLastRow();
			int fc = cellAdd.getFirstColumn();
			int ec = cellAdd.getLastColumn();
			for (int j = fr; j <= lr; j++) {
				String key = "RS" + String.valueOf(j) + "RE" + String.valueOf(j) + "CS" + String.valueOf(fc) + "CE"
						+ String.valueOf(ec);
				objMap.put(key, val);
			}
			String firstRow = cellAdd.formatAsString();
			mrgColumn.add(firstRow);
		}
		int slno = 0;
		int rowMerge = 0;
		int rowIndex = 0;
		int sublno = 0;

		for (Row row : sheet) {
			if (rowIndex > 0) {
				System.out.println("Sample count " + rowIndex);
				Registration rs = SerializationUtils.clone(registration);
				RegistrationSample rsa = new RegistrationSample();
				int cellIndex = 0;

				for (String field : lstHeader) // iteration over cell using for each loop
				{
					System.out.println("cell count" + cellIndex + " " + field);
					Cell cell = row.getCell(cellIndex);
					// int ri = cell cell.getRowIndex();
					// ci = cell.getColumnIndex();
					Map<String, Object> objExpFldProps = new HashMap<String, Object>();
					if (!exportFieldProperties.isEmpty()) {
						List<Map<String, Object>> expfieldprops = exportFieldProperties.stream()
								.filter(x -> ((String) x.get("label")).equals(field)).collect(Collectors.toList());

						if (!expfieldprops.isEmpty()) {

							objExpFldProps = expfieldprops.get(0);
						}
					}
					int fr;
					//int lr;
					if (sheet.getNumMergedRegions() > 0) {

						String key = "RS" + String.valueOf(rowIndex) + "RE" + String.valueOf(rowIndex) + "CS"
								+ String.valueOf(cellIndex) + "CE" + String.valueOf(cellIndex);

						String value = objMap.get(key);

						if (objMap.containsKey(key)) {

							fr = Integer.parseInt(value.substring(2, value.indexOf("RE")));
							//lr = Integer.parseInt(value.substring(value.indexOf("RE") + 2, value.indexOf("CS")));

							if (fr == rowIndex) {
								rowMerge = 1;
								responseMap = (Map<String, Object>) transactionDAOSupport.importValidData(objExpFldProps, cell, field,
										userInfo);

								responseString = (String) responseMap
										.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());

								if (responseString != Enumeration.ReturnStatus.SUCCESS.getreturnstatus()) {
									return new ResponseEntity<>(responseString, HttpStatus.EXPECTATION_FAILED);
								}

								if (objExpFldProps.get("inputtype").toString().equals("combo")) {
									if (lstSampHeaderString.stream().anyMatch(x -> x.equals(field))) {
										objJsonUiData.put(field, responseMap.get("displaymember"));
										objJsonData.put(field, responseMap.get("objJsonData"));

									} else {
										if (nneedsubsample) {
											objJsonUiDatasub.put(field, responseMap.get("displaymember"));
											objJsonDatasub.put(field, responseMap.get("objJsonData"));
										}
									}
								} else {

									if (lstSampHeaderString.stream().anyMatch(x -> x.equals(field))) {
										objJsonUiData.put(field, responseMap.get("cellData"));
										objJsonData.put(field, objJsonUiData.get(field));
									} else {
										if (nneedsubsample) {
											objJsonUiDatasub.put(field, responseMap.get("cellData"));
											objJsonDatasub.put(field, objJsonUiDatasub.get(field));
										}
									}
								}

							}

							else {
								rowMerge = 0;
							}
						} else {
							responseMap = (Map<String, Object>) transactionDAOSupport.importValidData(objExpFldProps, cell, field, userInfo);

							responseString = (String) responseMap
									.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());

							if (responseString != Enumeration.ReturnStatus.SUCCESS.getreturnstatus()) {
								return new ResponseEntity<>(responseString, HttpStatus.EXPECTATION_FAILED);
							}

							if (objExpFldProps.get("inputtype").toString().equals("combo")) {
								if (lstSampHeaderString.stream().anyMatch(x -> x.equals(field))) {
									objJsonUiData.put(field, responseMap.get("displaymember"));
									objJsonData.put(field, responseMap.get("objJsonData"));

								} else {
									if (nneedsubsample) {
										objJsonUiDatasub.put(field, responseMap.get("displaymember"));
										objJsonDatasub.put(field, responseMap.get("objJsonData"));
									}
								}
							} else {

								if (lstSampHeaderString.stream().anyMatch(x -> x.equals(field))) {
									objJsonUiData.put(field, responseMap.get("cellData"));
									objJsonData.put(field, objJsonUiData.get(field));
								} else {
									if (nneedsubsample) {
										objJsonUiDatasub.put(field, responseMap.get("cellData"));
										objJsonDatasub.put(field, objJsonUiDatasub.get(field));
									}
								}
							}


							if (lstSampHeaderString.stream().anyMatch(x -> x.equals(field))) {
								rowMerge = 1;
							}

						}

					} else {
						responseMap = (Map<String, Object>) transactionDAOSupport.importValidData(objExpFldProps, cell, field, userInfo);

						responseString = (String) responseMap
								.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());

						if (responseString != Enumeration.ReturnStatus.SUCCESS.getreturnstatus()) {
							return new ResponseEntity<>(responseString, HttpStatus.EXPECTATION_FAILED);
						}

						if (objExpFldProps.get("inputtype").toString().equals("combo")) {
							if (lstSampHeaderString.stream().anyMatch(x -> x.equals(field))) {
								objJsonUiData.put(field, responseMap.get("displaymember"));
								objJsonData.put(field, responseMap.get("objJsonData"));

							} else {
								if (nneedsubsample) {
									objJsonUiDatasub.put(field, responseMap.get("displaymember"));
									objJsonDatasub.put(field, responseMap.get("objJsonData"));
								}
							}
						} else {

							if (lstSampHeaderString.stream().anyMatch(x -> x.equals(field))) {
								objJsonUiData.put(field, responseMap.get("cellData"));
								objJsonData.put(field, objJsonUiData.get(field));
							} else {
								if (nneedsubsample) {
									objJsonUiDatasub.put(field, responseMap.get("cellData"));
									objJsonDatasub.put(field, objJsonUiDatasub.get(field));
								}
							}
						}



						rowMerge = 1;
					}


					cellIndex++;
				}
				if (rowMerge == 1) {
					slno += 1;
					rs.setJsondata(objJsonData.toMap());
					rs.setJsonuidata(objJsonUiData.toMap());
					rs.setSlno(slno);
					rs.setNisiqcmaterial((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
					lstRegistration.add(rs);
				}
				if (nneedsubsample) {
					sublno += 1;
					rsa.setJsondata(objJsonDatasub.toMap());
					rsa.setJsonuidata(objJsonUiDatasub.toMap());
					rsa.setNpreregno(slno);
					rsa.setSlno(sublno);

					if (!specBasedComponnet) {

						rsa.setNcomponentcode(-1);
						rsa.setNspecsampletypecode(nspecsampletypecode);
						// ALPD-3596
						if (needExecute || (int) objMap1.get("importTest") == Enumeration.TransactionStatus.YES
								.gettransactionstatus()) {
							String s = " select tg.*," + sublno
									+ " slno,case when tg.nrepeatcountno = 0 then 1 else tg.nrepeatcountno end nrepeatcountno,"
									+ " (  select count(tgtp.ntestgrouptestparametercode) "
									+ "    from testgrouptestparameter tgtp "
									+ "    where  tgtp.ntestgrouptestcode=tg.ntestgrouptestcode "
									+ "		and tgtp.nstatus ="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " 	and tgtp.nsitecode=" + userInfo.getNmastersitecode()
									+ " ) as nparametercount"
									+ " from testgrouptest tg,testgroupspecsampletype ts where"
									+ " ts.nspecsampletypecode=tg.nspecsampletypecode and ts.nspecsampletypecode="
									+ nspecsampletypecode + " and ts.nallottedspeccode=" + nallottedspeccode + " "
									+ sQuery 
									+ " and tg.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
									+ " and ts.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and tg.nsitecode=" + userInfo.getNmastersitecode()
									+ " and ts.nsitecode=" + userInfo.getNmastersitecode();

							List<Map<String,Object>> lst = jdbcTemplate.queryForList(s);

							lstTest.addAll(lst);
						}

					} else {
						final String componentname = objJsonUiDatasub.get("ncomponentcode").toString();

						final String componnet = " select * from testgroupspecsampletype ts,component c where "
								+ " nallottedspeccode=" + nallottedspeccode
								+ " and ts.ncomponentcode=c.ncomponentcode and c.scomponentname='" + componentname + "'"
								+ " and ts.nstatus=" +Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and c.nstatus=" +Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and ts.nsitecode=" + userInfo.getNmastersitecode()
								+ " and c.nsitecode=" + userInfo.getNmastersitecode();

						final TestGroupSpecSampleType tg = (TestGroupSpecSampleType) jdbcUtilityFunction.queryForObject(componnet,
								TestGroupSpecSampleType.class, jdbcTemplate);

						if (tg == null) {

							return new ResponseEntity<>(
									commonFunction.getMultilingualMessage("IDS_COMPONNET",
											userInfo.getSlanguagefilename())
											+ " "
											+ commonFunction.getMultilingualMessage("IDS_VALUENOTPRESENTINPARENT",
													userInfo.getSlanguagefilename())
											+ " (ncomponent_child)",
									HttpStatus.CONFLICT);

						} else {

							rsa.setNcomponentcode(tg.getNcomponentcode());
							rsa.setNspecsampletypecode(tg.getNspecsampletypecode());

							objJsonDatasub.put("ncomponentcode", tg.getNcomponentcode());
							objJsonDatasub.put("scomponentname", tg.getScomponentname());

							objJsonUiDatasub.put("ncomponentcode", tg.getNcomponentcode());
							objJsonUiDatasub.put("scomponentname", tg.getScomponentname());

							rsa.setJsondata(objJsonDatasub.toMap());
							rsa.setJsonuidata(objJsonUiDatasub.toMap());
							// ALPD-3596
							if (needExecute || (int) objMap1.get("importTest") == Enumeration.TransactionStatus.YES
									.gettransactionstatus()) {
								String queryString = " select *," + sublno
										+ " slno,case when nrepeatcountno = 0 then 1 else nrepeatcountno end nrepeatcountno,"
										+ " (  select count(tgtp.ntestgrouptestparametercode) "
										+ "    from testgrouptestparameter tgtp "
										+ "    where  tgtp.ntestgrouptestcode=ntestgrouptestcode "
										+ "		and tgtp.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and tgtp.nsitecode=" + userInfo.getNmastersitecode()
										+ " ) as nparametercount" + " from testgrouptest where"
										+ " nspecsampletypecode=" + tg.getNspecsampletypecode() 
										+ " and nstatus=" +Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and nsitecode=" + userInfo.getNmastersitecode();

								List<Map<String,Object>> lst = jdbcTemplate.queryForList(queryString);

								lstTest.addAll(lst);
							}
						}
					}

					lstRegistrationSample.add(rsa);

				} else {
					sublno += 1;
					rsa.setNcomponentcode(-1);
					// rsa.setNspecsampletypecode(-1);
					rsa.setNspecsampletypecode(nspecsampletypecode);
					rsa.setNpreregno(slno);
					rsa.setSlno(sublno);
					lstRegistrationSample.add(rsa);
					// ALPD-3596
					if (needExecute || (int) objMap1.get("importTest") == Enumeration.TransactionStatus.YES
							.gettransactionstatus()) {
						String queryString = " select tg.*," + sublno
								+ " slno,case when tg.nrepeatcountno = 0 then 1 else tg.nrepeatcountno end nrepeatcountno,"
								+ " (  select count(tgtp.ntestgrouptestparametercode) "
								+ "    from testgrouptestparameter tgtp "
								+ "    where  tgtp.ntestgrouptestcode=tg.ntestgrouptestcode "
								+ "		and tgtp.nstatus ="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and tgtp.nsitecode=" + userInfo.getNmastersitecode()
								+ " ) as nparametercount"
								+ " from testgrouptest tg,testgroupspecsampletype ts where"
								+ " ts.nspecsampletypecode=tg.nspecsampletypecode and ts.nallottedspeccode="
								+ nallottedspeccode + " " + sQuery 
								+ " and tg.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and ts.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and tg.nsitecode=" + userInfo.getNmastersitecode()
								+ " and ts.nsitecode=" + userInfo.getNmastersitecode();

						List<Map<String,Object>> lst = jdbcTemplate.queryForList(queryString);

						lstTest.addAll(lst);
					}

				}

			} else {

				//int cellIndex = 0;
				for (Cell cell : row) // iteration over cell using for each loop
				{
					String header = cell.getStringCellValue();
					if (header.indexOf("(") != -1 && header.indexOf(")") != -1) {

						header = header.substring(header.indexOf('(') + 1, header.indexOf(')'));
						String header1 = header;
						if (!header1.isEmpty()) {
							if (lstExportList.stream().noneMatch(x -> x.equals(header1))) {
								return new ResponseEntity<>(header1 + "  " + commonFunction
										.getMultilingualMessage("IDS_INVALIDFIELD", userInfo.getSlanguagefilename()),
										HttpStatus.CONFLICT);
							}

							lstHeader.add(header);
							//cellIndex++;
						} else {
							return new ResponseEntity<>(
									commonFunction.getMultilingualMessage("IDS_INVALIDTEMPLATEHEADERS",
											userInfo.getSlanguagefilename()),
									HttpStatus.CONFLICT);
						}
					} else {

						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_INVALIDTEMPLATEHEADERS",
								userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);

					}
				}
				if (specBasedComponnet) {
					if (!lstHeader.contains("ncomponentcode")) {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_COMPONENTNEEDTOBEEXPORT",
								userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
					}
				}

			}
			rowIndex++;

		}

		objMap1.put("Registration", lstRegistration);
		objMap1.put("RegistrationSample", lstRegistrationSample);
		// ALPD-3596 Start
		if (needExecute
				|| (int) objMap1.get("importTest") == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
			objMap1.put("testgrouptest", lstTest);
		} else {
			objMap1.put("testgrouptest", new ArrayList<>());
		}
		// ALPD-3596 End

		if (!lstRegistration.isEmpty()) {

			Map<String, Object> obj1 = validateAndInsertSeqNoRegistrationDataList(objMap1);

			if ((int) obj1.get("nflag") != 1) {
				if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
						.equals(obj1.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
					objMap1.putAll(obj1);
					obj1 = insertRegistrationList(objMap1);
					return new ResponseEntity<>(obj1, HttpStatus.OK);
				} else {
					if (obj1.containsKey("NeedConfirmAlert") && (Boolean) obj1.get("NeedConfirmAlert") == true) {
						// inputMap.putAll(objmap);
						return new ResponseEntity<>(obj1, HttpStatus.EXPECTATION_FAILED);
					} else {
						obj1.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
								obj1.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()));
						return new ResponseEntity<>(obj1, HttpStatus.OK);
					}
				}
			} else {
				obj1.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
				return new ResponseEntity<>(obj1, HttpStatus.OK);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_NORECORDINEXCEL", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);

		}
		
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> validateAndInsertSeqNoRegistrationDataList(Map<String, Object> inputMap)
			throws Exception {

		Map<String, Object> returnMap = new HashMap<String, Object>();
		final ObjectMapper objectMapper = new ObjectMapper();

		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		objectMapper.registerModule(new JavaTimeModule());
		final List<TestGroupTest> listTest = objectMapper.convertValue(inputMap.get("testgrouptest"),
				new TypeReference<List<TestGroupTest>>() {});

		Boolean skipMethodValidity = null;
		if (inputMap.containsKey("skipmethodvalidity")) {
			skipMethodValidity = (Boolean) inputMap.get("skipmethodvalidity");
		}
 
		final String sntestgrouptestcode = stringUtilityFunction.fnDynamicListToString(listTest, "getNtestgrouptestcode");
 
		List<TestGroupTest> expiredMethodTestList = new ArrayList<TestGroupTest>();
		if (skipMethodValidity == false) {
			expiredMethodTestList = transactionDAOSupport.getTestByExpiredMethod(sntestgrouptestcode, userInfo);
 
		}
		if (expiredMethodTestList.isEmpty()) {
			String sQuery = " lock  table lockpreregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);
 
			sQuery = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);
 
			sQuery = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);
 
			sQuery = " lock  table lockcancelreject " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);
 
			List<RegistrationSample> subSampleInputList = objectMapper.convertValue(inputMap.get("RegistrationSample"),
					new TypeReference<List<RegistrationSample>>() {
					});
 
			final List<Registration> registration = objectMapper.convertValue(inputMap.get("Registration"),
					new TypeReference<List<Registration>>() {
					});
//			List<TestGroupTest> testGroupTestInputList = objectMapper.convertValue(inputMap.get("testgrouptest"),
//					new TypeReference<List<TestGroupTest>>() {
//					});
 
			int testCount = listTest.stream().mapToInt(
					testgrouptest -> testgrouptest.getNrepeatcountno() == 0 ? 1 : testgrouptest.getNrepeatcountno())
					.sum();
 
			int parameterCount = listTest.stream().mapToInt(
					testgrouptest -> ((testgrouptest.getNrepeatcountno() == 0 ? 1 : testgrouptest.getNrepeatcountno())
							* testgrouptest.getNparametercount()))
					.sum();
 
			List<Map<String, Object>> sampleCombinationUnique = (List<Map<String, Object>>) inputMap
					.get("samplecombinationunique");
 
			final List<Map<String, Object>> subSampleCombinationUnique = (List<Map<String, Object>>) inputMap
					.get("subsamplecombinationunique");
 
			for (Registration reg : registration) {
 
				Map<String, Object> map1 = objectMapper.convertValue(reg, new TypeReference<Map<String, Object>>() {});
 
				Map<String, Object> map = projectDAOSupport.validateUniqueConstraint(sampleCombinationUnique, map1, userInfo, "create",
						Registration.class, "npreregno", false);
				
				if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
						.equals(map.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
					// for normal preregister flow if we sent 1 means,order sample already
					// preregitered so current sample also sample same order so store as sub sample
					// in that specific order (only for clinic type)
					map.put("nflag", 2);
					return map;
				}
			}
 
			for (RegistrationSample reg : subSampleInputList) {
 
				Map<String, Object> map1 = objectMapper.convertValue(reg, new TypeReference<Map<String, Object>>() {
				});
 
				Map<String, Object> map = projectDAOSupport.validateUniqueConstraint(subSampleCombinationUnique, map1, userInfo, "create",
						RegistrationSample.class, "ntransactionsamplecode", false);
				if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
						.equals(map.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
					// for normal preregister flow if we sent 1 means,order sample already
					// preregitered so current sample also sample same order so store as sub sample
					// in that specific order (only for clinic type)
					map.put("nflag", 2);
					return map;
				}
			}
 
			JSONObject objJson = new JSONObject(registration.get(0).getJsondata());
 
			String manualOrderInsert = "";
			int nsampletypecode = (int) inputMap.get("nsampletypecode");
			if (registration.get(0).getJsonuidata().containsKey("Order Type")) {
				if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()
						&& ((int) ((JSONObject) objJson.get("Order Type")).getInt("value") == Enumeration.OrderType.INTERNAL.getOrderType()
								|| (int) ((JSONObject) objJson.get("Order Type")).getInt("value") == Enumeration.OrderType.NA.getOrderType()))
					manualOrderInsert = ",'externalordertest','externalordersample'";
			}
 
			//RegistrationSample externalorderList = null;
			List<RegistrationHistory> lstStatus = null;
			if (registration.get(0).getJsonuidata().containsKey("Order Type")
					&& nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()
					&& (int) ((JSONObject) objJson.get("Order Type")).getInt("value") == Enumeration.OrderType.EXTERNAL.getOrderType()) {
 
				final String squerystatus = "select npreregno, ntransactionstatus from registrationhistory where"
											+ " nreghistorycode in (select max(nreghistorycode) from registrationhistory r"
											+ " where r.npreregno in (select npreregno from registration "
											+ " where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " and jsondata->>'OrderCodeData'='"
											+ registration.get(0).getJsondata().get("OrderCodeData") + "')  "
											+ " and nsitecode="	+ userInfo.getNtranssitecode() 
											+ " group by r.npreregno) and  ntransactionstatus not in ("
											+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ","
											+ Enumeration.TransactionStatus.RELEASED.gettransactionstatus() + ","
											+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ")"
											+ " and nsitecode="	+ userInfo.getNtranssitecode()
											+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()											
													+ "";
				lstStatus = jdbcTemplate.query(squerystatus, new RegistrationHistory());
				for (int p = 0; p < lstStatus.size(); p++) {
					if (lstStatus != null
							&& lstStatus.get(p).getNtransactionstatus() != Enumeration.TransactionStatus.CANCELED
									.gettransactionstatus()
							&& lstStatus.get(p).getNtransactionstatus() != Enumeration.TransactionStatus.REJECTED
									.gettransactionstatus()
							&& lstStatus.get(p).getNtransactionstatus() != Enumeration.TransactionStatus.RELEASED
									.gettransactionstatus()) {
						inputMap.put("nfilterstatus", lstStatus.get(0).getNtransactionstatus());
						inputMap.put("npreregno", Integer.toString(lstStatus.get(0).getNpreregno()));
						inputMap.put("npreregnocount", lstStatus.get(0).getNpreregno());
						inputMap.put("masterData", inputMap.get("DataRecordMaster"));
						return (Map<String, Object>) externalOrderSupport.externalOrderSampleExtisting(inputMap);
					}
				}
 
			}
			final String strSelectSeqno = "select stablename,nsequenceno from seqnoregistration  where stablename "
					+ "in ('registration','registrationhistory','registrationparameter','registrationsample',"
					+ "'registrationsamplehistory','registrationtest','registrationdecisionhistory','registrationtesthistory'"
					+ manualOrderInsert + ") "
					+ " and nstatus=" +  Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ ";";
 
//			final List<?> lstMultiSeqNo = jdbcTemplate.query(strSelectSeqno, new SeqNoRegistration());
// 
//			final List<SeqNoRegistration> lstSeqNoReg = (List<SeqNoRegistration>) lstMultiSeqNo.get(0);
// 
//			returnMap = lstSeqNoReg.stream().collect(Collectors.toMap(SeqNoRegistration::getStablename,
//					SeqNoRegistration -> SeqNoRegistration.getNsequenceno()));
			
			
			final List<SeqNoRegistration> lstSeqNo = jdbcTemplate.query(strSelectSeqno, new SeqNoRegistration());
			
			returnMap = lstSeqNo.stream().collect(Collectors.toMap(SeqNoRegistration::getStablename,
					SeqNoRegistration -> SeqNoRegistration.getNsequenceno()));
 
			String strSeqnoUpdate = "Update seqnoregistration set nsequenceno = "
								+ ((int) returnMap.get("registration") + registration.size())
								+ " where stablename = 'registration';" + "Update seqnoregistration set nsequenceno = "
								+ ((int) returnMap.get("registrationdecisionhistory") + registration.size())
								+ " where stablename = 'registrationdecisionhistory';"
								+ " Update seqnoregistration set nsequenceno = "
								+ ((int) returnMap.get("registrationhistory") + registration.size())
								+ " where stablename = 'registrationhistory';" + "Update seqnoregistration set nsequenceno = "
								+ ((int) returnMap.get("registrationparameter") + parameterCount)
								+ " where stablename = 'registrationparameter';" + "Update seqnoregistration set nsequenceno = "
								+ ((int) returnMap.get("registrationsample") + subSampleInputList.size())
								+ " where stablename = 'registrationsample';" + "Update seqnoregistration set nsequenceno = "
								+ ((int) returnMap.get("registrationsamplehistory") + subSampleInputList.size())
								+ " where stablename = 'registrationsamplehistory';" + "Update seqnoregistration set nsequenceno = "
								+ ((int) returnMap.get("registrationtest")// + TestGroupTests.size()
										+ testCount)
								+ " where stablename = 'registrationtest' ;" + "Update seqnoregistration set nsequenceno = "
								+ ((int) returnMap.get("registrationtesthistory") // + TestGroupTests.size()
										+ testCount)
								+ " where stablename = 'registrationtesthistory';";
			if (registration.get(0).getJsonuidata().containsKey("Order Type")) {
				if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()
						&& ((int) ((JSONObject) objJson.get("Order Type")).getInt("value") == Enumeration.OrderType.INTERNAL.getOrderType()
								|| (int) ((JSONObject) objJson.get("Order Type")).getInt("value") == Enumeration.OrderType.NA.getOrderType()))
					strSeqnoUpdate = strSeqnoUpdate + "Update seqnoregistration set nsequenceno = "
							+ ((int) returnMap.get("externalordersample") // + TestGroupTests.size()
									+ subSampleInputList.size())
							+ " where stablename = 'externalordersample';"
							+ " Update seqnoregistration set nsequenceno = " + ((int) returnMap.get("externalordertest") // +
							+ listTest.size())
							+ " where stablename = 'externalordertest';";
			}
			jdbcTemplate.execute(strSeqnoUpdate);
 
			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
 
		} else {
			final String expiredMethod = stringUtilityFunction.fnDynamicListToString(expiredMethodTestList, "getSmethodname");
			returnMap.put("NeedConfirmAlert", true);
			
			final String message = commonFunction.getMultilingualMessage("IDS_TESTWITHEXPIREDMETHODCONFIRM",
					userInfo.getSlanguagefilename());
			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					message.concat(" " + expiredMethod + " ?"));
		}
		returnMap.put("nflag", 2);
		return returnMap;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> insertRegistrationList(Map<String, Object> inputMap) throws Exception {

		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		Map<String, Object> returnMap = new HashMap<>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObject = new JSONObject();

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});

		final List<Registration> reg = objMapper.convertValue(inputMap.get("Registration"),
				new TypeReference<List<Registration>>() {});

		// registration.setNisiqcmaterial((short)
		// Enumeration.TransactionStatus.NO.gettransactionstatus());

		final List<RegistrationSample> registrationSample1 = objMapper.convertValue(inputMap.get("RegistrationSample"),
				new TypeReference<List<RegistrationSample>>() {});
        
		objMapper.registerModule(new JavaTimeModule());
		final List<TestGroupTest> tgtTestInputList = objMapper.convertValue(inputMap.get("testgrouptest"),
				new TypeReference<List<TestGroupTest>>() {});

		final List<String> dateList = objMapper.convertValue(inputMap.get("DateList"), new TypeReference<List<String>>() {});

		final List<Map<String, Object>> sampleDateConstraint = objMapper
				.convertValue(inputMap.get("sampledateconstraints"), new TypeReference<List<Map<String, Object>>>() {});

		final List<Map<String, Object>> subSampleDateConstraint = objMapper
				.convertValue(inputMap.get("subsampledateconstraints"), new TypeReference<List<Map<String, Object>>>() {});

		final List<String> SubSampledateList = objMapper.convertValue(inputMap.get("subsampleDateList"),
				new TypeReference<List<String>>() {});

		int nregtypecode = (int) inputMap.get("nregtypecode");
		int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
		int nsampletypecode = (int) inputMap.get("nsampletypecode");
		int npreregno = (int) inputMap.get("registration");
		int nreghistorycode = (int) inputMap.get("registrationhistory");
		int nregistrationparametercode = (int) inputMap.get("registrationparameter");
		int nregsamplecode = (int) inputMap.get("registrationsample");
		int nregsamplehistorycode = (int) inputMap.get("registrationsamplehistory");
		int napproveconfversioncode = (int) inputMap.get("napproveconfversioncode");
		int ntransactiontestcode = (int) inputMap.get("registrationtest");
		int ntesthistorycode = (int) inputMap.get("registrationtesthistory");
		int nregdecisionhistorycode = (int) inputMap.get("registrationdecisionhistory");
		int seqordersample = -1;
		int seqordertest = -1;

		StringJoiner joinersubSample = new StringJoiner(",");
		StringJoiner joinerSample = new StringJoiner(",");
		++ntransactiontestcode;
		++nregistrationparametercode;
		++ntesthistorycode;

		int nage = 0;
		int ngendercode = 0;
		String sQuery = "";
		if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()) {
			nage = (int) inputMap.get("AgeData");
			ngendercode = (int) inputMap.get("ngendercode");
			
			String sYears="SELECT CURRENT_DATE - TO_DATE('"+inputMap.get("sDob")+"','"+userInfo.getSpgsitedatetime()+"' )";
		    long ageInDays= jdbcTemplate.queryForObject(sYears,Long.class);
			
			sQuery = "json_build_object('ntransactionresultcode'," + nregistrationparametercode
					+ "+RANK() over(order by tgtp.ntestgrouptestparametercode),'ntransactiontestcode',"
					+ ntransactiontestcode + "+DENSE_RANK() over(order by tgtp.ntestgrouptestcode),'npreregno',"
					+ npreregno + ")::jsonb || " + " case when tgtp.nparametertypecode="
					+ Enumeration.ParameterType.NUMERIC.getparametertype() + " then case when  " + " tgtcs.ngendercode="
					+ ngendercode + " and " + ageInDays
					//+ " between tgtcs.nfromage and tgtcs.ntoage"
					+ " between case when tgtcs.nfromageperiod="+Enumeration.Period.Years.getPeriod()+""
				    + " then (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(years => tgtcs.nfromage)))::int)"
					+ " when tgtcs.nfromageperiod="+Enumeration.Period.Month.getPeriod()+" then "
					+ " (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(months => tgtcs.nfromage)))::int) "
					+ " else (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(days => tgtcs.nfromage)))::int) end "
					+ " and case when tgtcs.ntoageperiod="+Enumeration.Period.Years.getPeriod()+" then (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(years => tgtcs.ntoage)))::int) "
					+ " when tgtcs.ntoageperiod="+Enumeration.Period.Month.getPeriod()+" then (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(months => tgtcs.ntoage)))::int) "
					+ " else (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(days => tgtcs.ntoage)))::int) end "
					+ " then jsonb_build_object('nfromage',"
					+ " tgtcs.nfromage,'ntoage',tgtcs.ntoage,'ngendercode',tgtcs.ngendercode ,'ngradecode',tgtcs.ngradecode,"
					+ " 'sminb',case when tgtcs.slowb='null' then NULL else tgtcs.slowb end,"
					+ " 'smina',case when tgtcs.slowa='null' then NULL else tgtcs.slowa end,"
					+ " 'smaxa',case when tgtcs.shigha='null' then NULL else tgtcs.shigha end,"
					+ " 'smaxb',case when tgtcs.shighb='null' then NULL else tgtcs.shighb end,"
					+ " 'sminlod',case when tgtcs.sminlod='null' then NULL else tgtcs.sminlod end,"
					+ " 'smaxlod',case when tgtcs.smaxlod='null' then NULL else tgtcs.smaxlod end,"
					+ " 'sminloq',case when tgtcs.sminloq='null' then NULL else tgtcs.sminloq end,"
					+ " 'smaxloq',case when tgtcs.smaxloq='null' then NULL else tgtcs.smaxloq end,"
					+ " 'sresultvalue',case when tgtcs.sresultvalue='null' then NULL else tgtcs.sresultvalue end,"
					+ " 'nroundingdigits',tgtp.nroundingdigits,'sresult',NULL,'sfinal',NULL,'stestsynonym',"
					+ " concat(tgt.stestsynonym,'[1][0]'),'sparametersynonym',tgtp.sparametersynonym)::jsonb else "
					+ " jsonb_build_object('nfromage',tgtcs.nfromage,'ntoage',tgtcs.ntoage,'ngendercode',tgtcs.ngendercode,'ngradecode',tgtcs.ngradecode,"
					+ " 'sminb',case when tgtcs.slowb='null' then NULL else tgtcs.slowb end,"
					+ " 'smina',case when tgtcs.slowa='null' then NULL else tgtcs.slowa end,"
					+ " 'smaxa',case when tgtcs.shigha='null' then NULL else tgtcs.shigha end,"
					+ " 'smaxb',case when tgtcs.shighb='null' then NULL else tgtcs.shighb end,"
					+ " 'sminlod',case when tgtcs.sminlod='null' then NULL else tgtcs.sminlod end,"
					+ " 'smaxlod',case when tgtcs.smaxlod='null' then NULL else tgtcs.smaxlod end,"
					+ " 'sminloq',case when tgtcs.sminloq='null' then NULL else tgtcs.sminloq end,"
					+ " 'smaxloq',case when tgtcs.smaxloq='null' then NULL else tgtcs.smaxloq end,"
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
					+ " and tgtcs.ngendercode=" + ngendercode + " and " + ageInDays
					+ " between case when tgtcs.nfromageperiod="+Enumeration.Period.Years.getPeriod()+" "
				    + " then (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(years => tgtcs.nfromage)))::int)"
					+ " when tgtcs.nfromageperiod="+Enumeration.Period.Month.getPeriod()+" then "
					+ " (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(months => tgtcs.nfromage)))::int) "
					+ " else (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(days => tgtcs.nfromage)))::int) end "
					+ " and case when tgtcs.ntoageperiod="+Enumeration.Period.Years.getPeriod()+" then (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(years => tgtcs.ntoage)))::int) "
					+ " when tgtcs.ntoageperiod="+Enumeration.Period.Month.getPeriod()+" then (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(months => tgtcs.ntoage)))::int) "
					+ " else (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(days => tgtcs.ntoage)))::int) end ";
					//+ " between tgtcs.nfromage and tgtcs.ntoage ";

			returnMap.put("nAge", nage);
			returnMap.put("nGendercode", ngendercode);
		} else {
			sQuery = " json_build_object('ntransactionresultcode'," + nregistrationparametercode
					+ "+RANK() over(order by tgtp.ntestgrouptestparametercode),'ntransactiontestcode',"
					+ ntransactiontestcode + "+DENSE_RANK() over(order by tgtp.ntestgrouptestcode),'npreregno',"
					+ npreregno
					+ ",'sminlod',tgtnp.sminlod,'smaxlod',tgtnp.smaxlod,'sminb',tgtnp.sminb,'smina',tgtnp.smina,"
					+ "'smaxa',tgtnp.smaxa,'smaxb',tgtnp.smaxb,'sminloq',tgtnp.sminloq,'smaxloq',tgtnp.smaxloq,"
					+ "'sdisregard',tgtnp.sdisregard,'sresultvalue',tgtnp.sresultvalue,'ngradecode',tgtnp.ngradecode,"
					+ "'nroundingdigits',tgtp.nroundingdigits,'sresult',NULL,'sfinal',NULL,'stestsynonym',concat(tgt.stestsynonym,'[1][0]'),"
					+ "'sparametersynonym',tgtp.sparametersynonym)::jsonb jsondata," + userInfo.getNtranssitecode()
					+ " nsitecode," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
					+ " from resultaccuracy ra,testgrouptest tgt,testgrouptestparameter tgtp";
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
		String externalOrderSampleQuery = "";
		String externalOrderTestQuery = "";
		// int sampleCount = registrationSample.size();
		boolean statusflag = false;

		for (Registration registration : reg) {

			npreregno++;
			nreghistorycode++;
			nregdecisionhistorycode++;

			joinerSample.add(String.valueOf(npreregno));
			JSONObject jsoneditRegistration = new JSONObject(registration.getJsondata());
			JSONObject jsonuiRegistration = new JSONObject(registration.getJsonuidata());
			if (registration.getJsondata().containsKey("Order Type")) {
				if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()
						&& ((int) ((JSONObject) jsoneditRegistration.get("Order Type")).getInt("value") == Enumeration.OrderType.INTERNAL.getOrderType()
								|| (int) ((JSONObject) jsoneditRegistration.get("Order Type")).getInt("value") == Enumeration.OrderType.NA.getOrderType())) {
					seqordersample = (int) inputMap.get("externalordersample");
					seqordertest = (int) inputMap.get("externalordertest");
				}
			}
			if (!dateList.isEmpty()) {

				jsoneditRegistration = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsoneditRegistration, dateList, false,
						userInfo);
				jsonuiRegistration = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonuiRegistration, dateList, false,
						userInfo);
				final Map<String, Object> obj = commonFunction.validateDynamicDateContraints(jsonuiRegistration,
						sampleDateConstraint, userInfo);
				if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
						.equals((String) obj.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
					return obj;
				}
			}

			jsonuiRegistration.put("npreregno", npreregno);
			jsonuiRegistration.put("nsampletypecode", nsampletypecode);
			jsonuiRegistration.put("nregtypecode", nregtypecode);
			jsonuiRegistration.put("nregsubtypecode", nregsubtypecode);
			jsonuiRegistration.put("nproductcatcode", registration.getNproductcatcode());
			jsonuiRegistration.put("nproductcode", registration.getNproductcode());
			jsonuiRegistration.put("nprojectmastercode", registration.getNprojectmastercode());
			jsonuiRegistration.put("ninstrumentcatcode", registration.getNinstrumentcatcode());
			jsonuiRegistration.put("ninstrumentcode", registration.getNinstrumentcode());
			jsonuiRegistration.put("nmaterialcatcode", registration.getNmaterialcatcode());
			jsonuiRegistration.put("nmaterialcode", registration.getNmaterialcode());
			jsonuiRegistration.put("ntemplatemanipulationcode", registration.getNtemplatemanipulationcode());
			jsonuiRegistration.put("nallottedspeccode", registration.getNallottedspeccode());
			jsonuiRegistration.put("ndesigntemplatemappingcode", registration.getNdesigntemplatemappingcode());
			jsonuiRegistration.put("napproveconfversioncode", napproveconfversioncode);
			jsonuiRegistration.put("napproveconfversioncode", napproveconfversioncode);
			if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()) {
				jsonuiRegistration.put("ngendercode", registration.getNgendercode());
			}

			strRegistrationInsert = strRegistrationInsert + "(" + npreregno + "," + nsampletypecode + "," + nregtypecode
					+ "," + nregsubtypecode + "," + registration.getNproductcatcode() + ","
					+ registration.getNproductcode() + "," + registration.getNinstrumentcatcode() + ","
					+ registration.getNinstrumentcode() + "," + registration.getNmaterialcatcode() + ","
					+ registration.getNmaterialcode() + "," + registration.getNtemplatemanipulationcode() + ","
					+ registration.getNallottedspeccode() + "," + userInfo.getNtranssitecode() + ",'"
					+ stringUtilityFunction.replaceQuote(jsoneditRegistration.toString()) + "'::JSONB,'"
					+ stringUtilityFunction.replaceQuote(jsonuiRegistration.toString()) + "'::JSONB,"
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
					+ registration.getNdesigntemplatemappingcode() + "," + registration.getNregsubtypeversioncode()
					+ "," + registration.getNprojectmastercode() + "," + registration.getNisiqcmaterial() + ","+registration.getNprotocolcode()+"),";

			strRegistrationHistory = strRegistrationHistory + "(" + nreghistorycode + "," + npreregno + ","
					+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + ",'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNusercode() + "," + userInfo.getNuserrole()
					+ "," + userInfo.getNdeputyusercode() + "," + userInfo.getNdeputyuserrole() + ",'"
					+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "'," + userInfo.getNtranssitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode() + "),";

			strDecisionHistory = strDecisionHistory + "(" + nregdecisionhistorycode + "," + npreregno + ","
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo)
					+ "'," + userInfo.getNusercode() + "," + userInfo.getNuserrole() + ","
					+ userInfo.getNdeputyusercode() + "," + userInfo.getNdeputyuserrole() + ",'"
					+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "'," + userInfo.getNtranssitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode() + "),";

			strRegistrationArno = strRegistrationArno + "(" + npreregno + ",'-'," + napproveconfversioncode + ","
					+ userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "),";

			strRegistrationStatusBlink = strRegistrationStatusBlink + "(" + npreregno + "," + statusflag + ","
					+ userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "),";

			if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()) {
				if (registration.getJsondata().containsKey("Order Type")) {
					if (((int) ((JSONObject) jsoneditRegistration.get("Order Type")).getInt("value") == Enumeration.OrderType.INTERNAL.getOrderType()
							|| (int) ((JSONObject) jsoneditRegistration.get("Order Type")).getInt("value") == Enumeration.OrderType.NA.getOrderType())
							&& registration.getJsonuidata().containsKey("Order")) {

						int externalordercode = (int) ((JSONObject) jsoneditRegistration.get("Order")).getInt("value");
						String updateExternalOrder = "update externalorder set sorderseqno=" + npreregno
								+ " where nexternalordercode=" + externalordercode + " and nsitecode="
								+ userInfo.getNtranssitecode()
								+ ";";
						jdbcTemplate.execute(updateExternalOrder);
					}

				}
			}

			List<RegistrationSample> lstRegistrationSample = registrationSample1.stream()
					.filter(x -> x.getNpreregno() == registration.getSlno()).collect(Collectors.toList());

			for (RegistrationSample registrationSample : lstRegistrationSample) {
				nregsamplecode++;
				nregsamplehistorycode++;
//				nregdecisionhistorycode++;
				seqordersample++;
				joinersubSample.add(String.valueOf(nregsamplecode));

				JSONObject jsoneditObj = new JSONObject(registrationSample.getJsondata());
				JSONObject jsonuiObj = new JSONObject(registrationSample.getJsonuidata());
				if (!SubSampledateList.isEmpty()) {
					jsoneditObj = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsoneditObj, SubSampledateList, false,
							userInfo);
					jsonuiObj = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonuiObj, SubSampledateList, false, userInfo);
					final Map<String, Object> obj = commonFunction.validateDynamicDateContraints(jsonuiObj,
							subSampleDateConstraint, userInfo);
					if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
							.equals((String) obj.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
						return obj;
					}
				}

				jsonuiObj.put("ntransactionsamplecode", nregsamplecode);
				jsonuiObj.put("npreregno", npreregno);
				jsonuiObj.put("nspecsampletypecode", registrationSample.getNspecsampletypecode());
				jsonuiObj.put("ncomponentcode", registrationSample.getNcomponentcode());

				strRegistrationSample = strRegistrationSample + " (" + nregsamplecode + "," + npreregno + ",'"
						+ registrationSample.getNspecsampletypecode() + "'," + registrationSample.getNcomponentcode()
						+ ",'" + stringUtilityFunction.replaceQuote(jsoneditObj.toString()) + "'::JSONB,'"
						+ stringUtilityFunction.replaceQuote(jsonuiObj.toString()) + "'::JSONB," + userInfo.getNtranssitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
				// jsonsampleArray.put(jsonuiObj);
				strRegistrationSampleHistory = strRegistrationSampleHistory + "(" + nregsamplehistorycode + ","
						+ nregsamplecode + "," + npreregno + ","
						+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + ",'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNusercode() + "," + userInfo.getNuserrole()
						+ "," + userInfo.getNdeputyusercode() + "," + userInfo.getNdeputyuserrole() + ",'"
						+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "'," + userInfo.getNtranssitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode()
						+ "),";

				strRegistrationSampleArno = strRegistrationSampleArno + "(" + nregsamplecode + "," + npreregno + ",'-'"
						+ "," + userInfo.getNtranssitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";

				List<RegistrationSample> comp = new ArrayList<RegistrationSample>();
				comp.add(registrationSample);
				List<TestGroupTest> lsttest1 = tgtTestInputList.stream()
						.filter(x -> x.getSlno() == comp.get(0).getSlno()).collect(Collectors.toList());

				final String stestcode = lsttest1.stream().map(x -> String.valueOf(x.getNtestgrouptestcode()))
						.distinct().collect(Collectors.joining(","));

				if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()) {
					if (registration.getJsondata().containsKey("Order Type")) {
						if (((int) ((JSONObject) jsoneditRegistration.get("Order Type")).getInt("value") == Enumeration.OrderType.INTERNAL.getOrderType()
								|| (int) ((JSONObject) jsoneditRegistration.get("Order Type")).getInt("value") == Enumeration.OrderType.NA.getOrderType())
								&& registration.getJsonuidata().containsKey("Order")) {

							int externalordercode = (int) ((JSONObject) jsoneditRegistration.get("Order"))
									.getInt("value");

							externalOrderSampleQuery += " (" + seqordersample + "," + externalordercode + ","
									+ registrationSample.getNcomponentcode() + "," + 0 + "," + -1 + ",'"
									+ stringUtilityFunction.replaceQuote(jsonuiObj.get("sampleorderid").toString()) + "','"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + "" + userInfo.getNtranssitecode() + ", "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
									+ userInfo.getNtranssitecode() + "),";

							for (TestGroupTest test : lsttest1) {
								seqordertest++;
								externalOrderTestQuery += "(" + seqordertest + "," + seqordersample + ","
										+ externalordercode + "," + test.getNtestpackagecode() + ","
										+ test.getNcontainertypecode() + " ," + test.getNtestcode() + ",'"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + "," + userInfo.getNtranssitecode() + ","
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
										+ userInfo.getNtranssitecode() + "),";
							}
						}

					}
				}

				if (!lsttest1.isEmpty()) {

					// FRS-00410- To add test based on replicate count defined in testgroup test

					final Map<String, Object> testHistoryParameterMap = insertTestHistoryParameter(stestcode, sQuery,
							userInfo, npreregno, nregsamplecode, ntransactiontestcode, ntesthistorycode,
							nregistrationparametercode, new ArrayList<Integer>(),inputMap);
					ntransactiontestcode = (int) testHistoryParameterMap.get("ntransactiontestcode");
					ntesthistorycode = (int) testHistoryParameterMap.get("ntesthistorycode");
					nregistrationparametercode = (int) testHistoryParameterMap.get("ntransactionresultcode");

				}

			}

		}

		strRegistrationInsert = "Insert into registration (npreregno, nsampletypecode, nregtypecode, nregsubtypecode, nproductcatcode, "
				+ "nproductcode,ninstrumentcatcode,ninstrumentcode,nmaterialcatcode,nmaterialcode, ntemplatemanipulationcode, "
				+ " nallottedspeccode, nsitecode,jsondata,jsonuidata, nstatus,ndesigntemplatemappingcode,nregsubtypeversioncode, "
				+ " nprojectmastercode, nisiqcmaterial,nprotocolcode) values "
				+ strRegistrationInsert.substring(0, strRegistrationInsert.length() - 1) + ";";
		jdbcTemplate.execute(strRegistrationInsert);

		strRegistrationHistory = "Insert into registrationhistory(nreghistorycode, npreregno, ntransactionstatus, dtransactiondate, nusercode, "
				+ " nuserrolecode, ndeputyusercode, ndeputyuserrolecode, scomments, nsitecode, nstatus,noffsetdtransactiondate,ntransdatetimezonecode) values "
				+ strRegistrationHistory.substring(0, strRegistrationHistory.length() - 1) + ";";
		jdbcTemplate.execute(strRegistrationHistory);

		if (nsampletypecode == Enumeration.SampleType.INSTRUMENT.getType()) {

			String query = "select * from instrumentcalibration where  ncalibrationstatus="
					+ Enumeration.TransactionStatus.UNDERCALIBIRATION.gettransactionstatus()
					+ " and dopendate is null and dclosedate is null and ninstrumentcode="
					+ reg.get(0).getNinstrumentcode()
					+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and nsitecode = " + userInfo.getNtranssitecode();

			final InstrumentCalibration instrumentCalibration = (InstrumentCalibration) jdbcUtilityFunction.queryForObject(query,
					InstrumentCalibration.class, jdbcTemplate);

			query = "update instrumentcalibration set npreregno=" + npreregno + ", dmodifieddate ='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ninstrumentcalibrationcode="
					+ instrumentCalibration.getNinstrumentcalibrationcode()
					+ " and nsitecode = " + userInfo.getNtranssitecode();
			jdbcTemplate.execute(query);

		}

		strDecisionHistory = "Insert into registrationdecisionhistory(nregdecisionhistorycode, npreregno, ndecisionstatus, dtransactiondate, "
				+ "nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, scomments, nsitecode,nstatus,noffsetdtransactiondate,ntransdatetimezonecode  ) values "
				+ strDecisionHistory.substring(0, strDecisionHistory.length() - 1) + ";";
		jdbcTemplate.execute(strDecisionHistory);

		strRegistrationArno = "Insert into registrationarno (npreregno, sarno, napprovalversioncode, nsitecode,nstatus) values "
				+ strRegistrationArno.substring(0, strRegistrationArno.length() - 1) + ";";
		jdbcTemplate.execute(strRegistrationArno);

		strRegistrationSample = "insert into registrationsample(ntransactionsamplecode,npreregno,nspecsampletypecode,ncomponentcode,jsondata,jsonuidata, "
				+ "nsitecode,nstatus) values " + strRegistrationSample.substring(0, strRegistrationSample.length() - 1)
				+ ";";
		jdbcTemplate.execute(strRegistrationSample);

		strRegistrationSampleHistory = "Insert into registrationsamplehistory(nsamplehistorycode, ntransactionsamplecode, npreregno, ntransactionstatus, "
				+ "dtransactiondate, nusercode, nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments,nsitecode,nstatus,noffsetdtransactiondate,ntransdatetimezonecode ) values "
				+ strRegistrationSampleHistory.substring(0, strRegistrationSampleHistory.length() - 1) + ";";
		jdbcTemplate.execute(strRegistrationSampleHistory);

		strRegistrationSampleArno = "Insert into registrationsamplearno (ntransactionsamplecode,npreregno,ssamplearno,nsitecode,nstatus) values "
				+ strRegistrationSampleArno.substring(0, strRegistrationSampleArno.length() - 1) + ";";
		jdbcTemplate.execute(strRegistrationSampleArno);

		if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()) {
			if (reg.get(0).getJsonuidata().containsKey("Order Type")) {
				if (((int) ((JSONObject) reg.get(0).getJsonuidata().get("Order Type")).getInt("value") == 1
						|| (int) ((JSONObject) reg.get(0).getJsonuidata().get("Order Type")).getInt("value") == -1)
						&& reg.get(0).getJsonuidata().containsKey("Order")) {

					externalOrderSampleQuery = "INSERT INTO externalordersample (nexternalordersamplecode,nexternalordercode,ncomponentcode,nsampleqty,nunitcode,sexternalsampleid,dmodifieddate,nsitecode,nstatus, nparentsitecode) values "
							+ externalOrderSampleQuery.substring(0, externalOrderSampleQuery.length() - 1) + ";";
					jdbcTemplate.execute(externalOrderSampleQuery);
					if (!tgtTestInputList.isEmpty()) {
						externalOrderTestQuery = "INSERT INTO externalordertest (nexternalordertestcode,nexternalordersamplecode,nexternalordercode,ntestpackagecode,ncontainertypecode,ntestcode,dmodifieddate,nsitecode,nstatus, nparentsitecode) values "
								+ externalOrderTestQuery.substring(0, externalOrderTestQuery.length() - 1) + ";";

						jdbcTemplate.execute(externalOrderTestQuery);
					}
				}
			}
		}
		strRegistrationStatusBlink = "Insert into registrationflagstatus (npreregno,bflag,nsitecode,nstatus) "
				+ " values" + strRegistrationStatusBlink.substring(0, strRegistrationStatusBlink.length() - 1) + ";";
		jdbcTemplate.execute(strRegistrationStatusBlink);

		inputMap.put("npreregno", joinerSample.toString());

		returnMap = registrationDAOSupport.getDynamicRegistration(inputMap, userInfo);
		returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
				Enumeration.ReturnStatus.SUCCESS.getreturnstatus());

		jsonAuditObject.put("registration", (List<Map<String, Object>>) returnMap.get("RegistrationGetSample"));
		actionType.put("registration", "IDS_PREREGISTER");

		if ((boolean) inputMap.get("nneedsubsample")) {
			jsonAuditObject.put("registrationsample",
					(List<Map<String, Object>>) returnMap.get("RegistrationGetSubSample"));
			actionType.put("registrationsample", "IDS_PREREGISTERSAMPLE");

			inputMap.put("ntransactionsamplecode", null);

			Map<String, Object> objMap = (Map<String, Object>) transactionDAOSupport.getRegistrationTestAudit(inputMap, userInfo).getBody();

			jsonAuditObject.put("registrationtest", (List<Map<String, Object>>) objMap.get("RegistrationGetTest"));
		} else {
			jsonAuditObject.put("registrationtest", (List<Map<String, Object>>) returnMap.get("RegistrationGetTest"));
		}

		auditmap.put("nregtypecode", nregtypecode);
		auditmap.put("nregsubtypecode", nregsubtypecode);
		auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));

		actionType.put("registrationtest", "IDS_PREREGISTERTEST");
		auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);

		// Portal Status Update
		inputMap.put("ssamplecode", joinerSample.toString());
		returnMap.putAll((Map<String, Object>) externalOrderSupport.sampleOrderUpdate(inputMap, String.valueOf(npreregno), userInfo,
				Enumeration.TransactionStatus.INITIATED.gettransactionstatus(),
				Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus(), true).getBody());

		return returnMap;

	}
	
	@Override
	public ResponseEntity<Object> getExternalOrderAttachment(String nexternalordercode, String npreregno,
			UserInfo userInfo) throws Exception {
		return externalOrderSupport.getExternalOrderAttachment(nexternalordercode, npreregno, userInfo);
	}
	
	@Override
	public Map<String, Object> viewExternalOrderAttachment(Map<String, Object> objExternalOrderAttachmentFile,
			int ncontrolCode, UserInfo userInfo) throws Exception {

		return externalOrderSupport.viewExternalOrderAttachment(objExternalOrderAttachmentFile, ncontrolCode, userInfo);

	}
	
	@Override
	public ResponseEntity<Object> getExternalOrderForMapping(Map<String, Object> inputMap, UserInfo userInfo) {
		return externalOrderSupport.getExternalOrderForMapping(inputMap, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> orderMapping(final Map<String, Object> inputMap, UserInfo userInfo) throws Exception 
	{
		return externalOrderSupport.orderMapping(inputMap, userInfo);
	}
	
	// ALPD-3404
	@Override
	public ResponseEntity<Object> getTestfromSection(Map<String, Object> inputMap) throws Exception {
		String getTestSectionQuery = "";
		final Map<String, Object> returnMap = new HashMap<String, Object>();
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});

		if ((boolean) inputMap.get("specBasedComponent")) {
			//ALPD-5808	Modified query by Vishakh for validating method, instrumentcategory, containertype, testpackage
			getTestSectionQuery = " select tgt.nsectioncode,s.ssectionname from testgrouptest tgt ,section s, "
					+ " method m,instrumentcategory ic, containertype ct, testpackage tp "
					+ " where s.nsectioncode = tgt.nsectioncode  "
					+ " and m.nmethodcode=tgt.nmethodcode and ic.ninstrumentcatcode=tgt.ninstrumentcatcode "
					+ " and ct.ncontainertypecode=tgt.ncontainertypecode and tp.ntestpackagecode=tgt.ntestpackagecode"
					+ " and m.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ic.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ct.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and tp.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and tgt.nspecsampletypecode in ("
					+ inputMap.get("nspecsampletypecode") + " ) and tgt.nisvisible="+ Enumeration.TransactionStatus.YES.gettransactionstatus()
					+ "  and tgt.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and s.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " group by tgt.nsectioncode,s.ssectionname ";
		} else {
			//ALPD-5808	Modified query by Vishakh for validating method, instrumentcategory, containertype, testpackage
			getTestSectionQuery = "select tgt.nsectioncode,s.ssectionname from testgroupspecsampletype tgts ,testgrouptest tgt,section s, "
					+ " method m, instrumentcategory ic, containertype ct, testpackage tp "
					+ " where s.nsectioncode = tgt.nsectioncode and tgt.nspecsampletypecode = tgts.nspecsampletypecode and "
					+ " tgts.nallottedspeccode in (" + inputMap.get("nallottedspeccode") + ") and tgt.nisvisible="
					+ Enumeration.TransactionStatus.YES.gettransactionstatus()+" and tgt.nstatus =  "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and tgts.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and s.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and m.nmethodcode=tgt.nmethodcode and m.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ic.ninstrumentcatcode=tgt.ninstrumentcatcode and ic.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ct.ncontainertypecode=tgt.ncontainertypecode and ct.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tp.ntestpackagecode=tgt.ntestpackagecode and tp.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " group by tgt.nsectioncode,s.ssectionname ";
		}
		returnMap.put("TestSection", jdbcTemplate.query(getTestSectionQuery, new TestGroupTestForSample()));

		return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
	}
	
	// ALPD-3404
	@Override
	public ResponseEntity<Object> getTestSectionBasesdTestPackage(Map<String, Object> inputMap) throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});


		final String getTestSectionQuery = "select tgt.nsectioncode,s.ssectionname from testgrouptest tgt ,section s "
				+ " where s.nsectioncode = tgt.nsectioncode and tgt.nspecsampletypecode in ("
				+ inputMap.get("nspecsampletypecode") + ") and tgt.ntestpackagecode in ("
				+ inputMap.get("ntestpackagecode") + ") "
				+ " and tgt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tgt.nsitecode = " + userInfo.getNmastersitecode()
				+ " group by tgt.nsectioncode,s.ssectionname";
		returnMap.put("TestSectionBasedonTestPackage",
				
		jdbcTemplate.query(getTestSectionQuery, new TestGroupTestForSample()));

		return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
	}

	// ALPD-3404
	@Override
	public ResponseEntity<Object> getTestBasedTestSection(Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});

		String strTestGetQuery = "";
		String testpackageTable = "";
		String testpackageValue = "";
		if ((int) inputMap.get("ntestpackagecode") != -1) {
			testpackageValue = " and tgt.ntestpackagecode=" + inputMap.get("ntestpackagecode") + " ";
			testpackageTable = "  "
					// + " left join testpackagetest tpt on tgt.ntestcode=tpt.ntestcode and
					// tpt.nstatus = "
					// + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " left JOIN testpackage tp on tp.ntestpackagecode=tgt.ntestpackagecode and tp.nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		}
		if ((boolean) inputMap.get("specBasedComponent")) {
			//ALPD-5808	Modified query by Vishakh for validating method, instrumentcategory, containertype, testpackage
			strTestGetQuery = "  select case when tgt.nrepeatcountno = 0 then 1 else tgt.nrepeatcountno end nrepeatcountno, tgt.nsectioncode,tgt.nmethodcode,tgt.ninstrumentcatcode, tgt.ntestgrouptestcode,tgt.nspecsampletypecode, "
					+ " (select count(tgtp.ntestgrouptestparametercode) from testgrouptestparameter tgtp "
					+ " where  tgtp.ntestgrouptestcode=tgt.ntestgrouptestcode "
					+ "  and tgtp.nstatus =" +Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and  tgtp.nisvisible="+Enumeration.TransactionStatus.YES.gettransactionstatus()
					+") as nparametercount, tgt.ntestcode,tgt.stestsynonym,tgt.nsectioncode,"
					+ " s.ssectionname, m.smethodname,"
					+ "  ic.sinstrumentcatname,tgt.ncost  from  testgrouptest tgt " + testpackageTable
					+ " ,section s,method m,instrumentcategory ic, testmaster tm"
					+ "  where s.nsectioncode = tgt.nsectioncode  and m.nmethodcode =tgt.nmethodcode "
					+ testpackageValue + "  and tgt.nsectioncode=" + inputMap.get("nsectioncode")
					+ "  and ic.ninstrumentcatcode = tgt.ninstrumentcatcode and tgt.nspecsampletypecode in ( "
					+ inputMap.get("nspecsampletypecode") + ") "
					+ " and tgt.nstatus = " +Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and s.nstatus =  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and m.nstatus =  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and ic.nstatus =  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and tm.nstatus =  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and tgt.nisvisible="+Enumeration.TransactionStatus.YES.gettransactionstatus()
					+ " and tgt.nsitecode = " + userInfo.getNmastersitecode()
					+ " and s.nsitecode = " + userInfo.getNmastersitecode()
					+ " and m.nsitecode = " + userInfo.getNmastersitecode()
					+ " and tm.nsitecode = " + userInfo.getNmastersitecode()
					+ " and ic.nsitecode = " + userInfo.getNmastersitecode()									
					+ " and tm.ntestcode = tgt.ntestcode "
					+ " and tm.ntransactionstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " order by tgt.nsorter";
		} else {
			strTestGetQuery = " select case when tgt.nrepeatcountno = 0 then 1 else tgt.nrepeatcountno end nrepeatcountno,"
					+ " tgt.nsectioncode,tgt.nmethodcode,tgt.ninstrumentcatcode,"
					+ " tgt.ntestgrouptestcode,tgt.nspecsampletypecode, " + " ("
					+ " select count(tgtp.ntestgrouptestparametercode) "
					+ "	from testgrouptestparameter tgtp "
					+ "	where  tgtp.ntestgrouptestcode=tgt.ntestgrouptestcode "
					+ " and tgtp.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and  tgtp.nisvisible="+Enumeration.TransactionStatus.YES.gettransactionstatus()+" ) as nparametercount,"
					+ " tgt.ntestcode,tgt.stestsynonym,tgt.nsectioncode,s.ssectionname, m.smethodname,"
					+ " ic.sinstrumentcatname from testgrouptest tgt " + testpackageTable
					+ ",section s,method m,instrumentcategory ic, testmaster tm ,testgroupspecsampletype tgts "
					+ " where s.nsectioncode = tgt.nsectioncode and m.nmethodcode =tgt.nmethodcode "
					+ " and ic.ninstrumentcatcode = tgt.ninstrumentcatcode  "
					+ " and tgt.nspecsampletypecode = tgts.nspecsampletypecode  and tgt.nsectioncode="
					+ inputMap.get("nsectioncode") + testpackageValue 
					+ " and tgt.nstatus = " +Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and s.nstatus =  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and m.nstatus =  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and ic.nstatus =  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and tm.nstatus =  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and tgts.nstatus =  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tm.ntestcode = tgt.ntestcode"
					+ " and tgt.nisvisible="+Enumeration.TransactionStatus.YES.gettransactionstatus()
					+ " and tgt.nsitecode = " + userInfo.getNmastersitecode()
					+ " and s.nsitecode = " + userInfo.getNmastersitecode()
					+ " and m.nsitecode = " + userInfo.getNmastersitecode()
					+ " and tm.nsitecode = " + userInfo.getNmastersitecode()
					+ " and ic.nsitecode = " + userInfo.getNmastersitecode()	
					+ " and tm.ntransactionstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgts.nallottedspeccode="
					+ inputMap.get("nallottedspeccode") + " order by tgt.nsorter";
		}
		final List<TestGroupTestForSample> lstTestGroupTest = jdbcTemplate.query(strTestGetQuery,
				new TestGroupTestForSample());
		return new ResponseEntity<Object>(lstTestGroupTest, HttpStatus.OK);
	}

	// ALPD-3404
	@Override
	public ResponseEntity<Object> getMoreTestSection(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		//ALPD-5808	Modified query by Vishakh for validating method, instrumentcategory, containertype, testpackage
		final String getTestSectionQuery = "select tgt.nsectioncode,s.ssectionname "
									+ " from testgrouptest tgt left JOIN testpackage tp on tgt.ntestpackagecode=tp.ntestpackagecode and "
									+ " tp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " ,section s, method m, instrumentcategory ic, containertype ct where s.nsectioncode = tgt.nsectioncode and tgt.nspecsampletypecode in ("
									+ inputMap.get("snspecsampletypecode") + ") "
									+ " and tgt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and tp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and tgt.nsitecode = " + userInfo.getNmastersitecode()
									+ " and tp.nsitecode = " + userInfo.getNmastersitecode()
									+ " and m.nmethodcode=tgt.nmethodcode and m.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and ic.ninstrumentcatcode=tgt.ninstrumentcatcode and ic.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and ct.ncontainertypecode=tgt.ncontainertypecode and ct.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and m.nsitecode="+ userInfo.getNmastersitecode()+" and ic.nsitecode="+ userInfo.getNmastersitecode()
									+ " and ct.nsitecode="+ userInfo.getNmastersitecode()
									+ " group by tgt.nsectioncode,s.ssectionname ";
		final List<TestGroupTestForSample> lstTestPackage = jdbcTemplate.query(getTestSectionQuery,
				new TestGroupTestForSample());
		return new ResponseEntity<Object>(lstTestPackage, HttpStatus.OK);
	}
	
	@Override
	public ResponseEntity<Object> getAdhocTest(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return registrationDAOSupport.getAdhocTest(inputMap, userInfo);
	}
	
	
	@Override
	public ResponseEntity<Object> getSampleBasedOnPortalOrder(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> objMap = new HashMap<>();

		int nexternalordercode = (int) inputMap.get("nexternalordercode");
		
//		String spgDateTimeFormat = "dd/MM/yyyy HH24:mi:ss";
//		if (userInfo != null) {
//			spgDateTimeFormat = userInfo.getSpgdatetimeformat();
//		}
		if (nexternalordercode != -1) {
			final String query = " select tgs.nspecsampletypecode,eo.nallottedspeccode,"
					+ "to_char(es.dsamplecollectiondatetime, 'YYYY-MM-DD HH24:MI:SS') dsamplecollectiondatetime"
					+ ", sa.ssampleappearance,es.*,u.sunitname,c.scomponentname from externalordersample es,"
					+ "unit u,component c,testgroupspecsampletype tgs,externalorder eo, sampleappearance sa "
					+ " where tgs.nallottedspeccode=eo.nallottedspeccode "
					+ " and eo.nexternalordercode=es.nexternalordercode and c.ncomponentcode=tgs.ncomponentcode"
					+ " and es.nsampleappearancecode=sa.nsampleappearancecode and es.nunitcode=u.nunitcode and "
					+ " u.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and "
					+ " es.ncomponentcode=c.ncomponentcode" 
					+ " and es.nsitecode=" + userInfo.getNtranssitecode()
					+ " and eo.nsitecode = "+userInfo.getNtranssitecode()
					+ " and c.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tgs.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and eo.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and es.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and sa.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and u.nsitecode = " + userInfo.getNmastersitecode()
					+ " and c.nsitecode = " + userInfo.getNmastersitecode()
					+ " and sa.nsitecode = " + userInfo.getNmastersitecode()
					+ " and tgs.nsitecode = " + userInfo.getNmastersitecode()					
					+ " and es.nexternalordercode=" + nexternalordercode;
			List<Map<String, Object>> objSample = jdbcTemplate.queryForList(query);
			objMap.put("Sample", objSample);
			if (!objSample.isEmpty()) {
				final String nexternalordersamplecode1 = objSample.stream()
						.map(x -> String.valueOf(x.get("nexternalordersamplecode"))).collect(Collectors.joining(","));

				final String TestQuery = " select (" + "  select count(tgtp.ntestgrouptestparametercode) "
						+ "  from testgrouptestparameter tgtp "
						+ "  where  tgtp.ntestgrouptestcode=tgt.ntestgrouptestcode  and tgtp.nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " ) as nparametercount,tgt.nrepeatcountno,s.ssectionname,m.smethodname,ic.sinstrumentcatname,"
						+ " es.nexternalordercode,tgs.nallottedspeccode,tgs.nspecsampletypecode,"
						+ " et.nexternalordertestcode,et.ntestcode,et.nexternalordersamplecode,"
						+ " et.ntestpackagecode,tgt.ntestgrouptestcode,tgt.stestsynonym,tgt.stestsynonym stestname "
						+ " from externalordertest et,"
						+ " externalordersample es, testgroupspecsampletype tgs, testgrouptest tgt,"
						+ " externalorder eo,section s,method m,instrumentcategory ic  where "
						+ " et.nexternalordersamplecode=es.nexternalordersamplecode and"
						+ " tgs.nallottedspeccode=eo.nallottedspeccode and es.ncomponentcode=tgs.ncomponentcode"
						+ " and tgt.nspecsampletypecode=tgs.nspecsampletypecode"
						+ " and tgt.ntestcode=et.ntestcode and " + " eo.nexternalordercode=et.nexternalordercode"
						+ " and tgt.nsectioncode=s.nsectioncode" + " and tgt.nmethodcode=m.nmethodcode "
						+ " and tgt.ninstrumentcatcode=ic.ninstrumentcatcode "
						+ " and tgt.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
						+ " and eo.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
						+ " and  s.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
						+ " and  m.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
						+ " and  ic.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
						+ " and tgs.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
						+ " and es.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
						+ " and et.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()						
						+ " and et.nsitecode = " + userInfo.getNtranssitecode()
						+ " and es.nsitecode = " + userInfo.getNtranssitecode()
						+ " and tgt.nsitecode = " + userInfo.getNmastersitecode()
						+ " and tgs.nsitecode = " + userInfo.getNmastersitecode()
						+ " and eo.nsitecode = " + userInfo.getNtranssitecode()
						+ " and s.nsitecode = " + userInfo.getNmastersitecode()
						+ " and m.nsitecode = " + userInfo.getNmastersitecode()
						+ " and ic.nsitecode = " + userInfo.getNmastersitecode()						
						+ " and et.nexternalordersamplecode in (" + nexternalordersamplecode1 + ")";

			    final List<Map<String, Object>> objTest = jdbcTemplate.queryForList(TestQuery);
				objMap.put("Test", objTest);
			} else {

				objMap.put("Test", Arrays.asList());
			}
		} else {
			objMap.put("Sample", Arrays.asList());
			objMap.put("Test", Arrays.asList());
		}
		return new ResponseEntity<Object>(objMap, HttpStatus.OK);
	}
	
	@Override
	public ResponseEntity<Object> copySample(Map<String, Object> inputMap, UserInfo userInfo) throws Exception 
	{
		return registrationDAOSupport.copySample(inputMap, userInfo);
	}
	
	@Override
	public Map<String, Object> getCreateTestSequenceNo(final UserInfo userInfo, final List<String> listSample,
			final List<TestGroupTest> listTest, final int nregtypecode, final int nregsubtypecode,
			Map<String, Object> inputMap) throws Exception {
		return registrationDAOSupport.getCreateTestSequenceNo(userInfo, listSample, listTest, nregtypecode, nregsubtypecode, inputMap);
		
	}
	
	@Override
	public ResponseEntity<Object> createFavoriteFilterName(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return projectDAOSupport.createFavoriteFilterName(inputMap, userInfo);
	}
	
	//ALPD-4914-To get previously saved filter details when click the filter name,done by Dhanushya RI
	@Override
	public ResponseEntity<Object> getRegistrationFilter(final Map<String, Object> uiMap,final UserInfo userInfo) throws Exception {
		return registrationDAOSupport.getRegistrationFilter(uiMap, userInfo);
		
	}
	
	//Added by Dhanushya RI for JIRA ID:ALPD-5511 edit test method insert --Start
	/**
	 * This method is used to get the method list for the particular test.
	 * @param userinfo object of the method
	 * @param map object of the method
	 * @return list of method based on the specified test
	 * @throws Exception 
	 */
	@Override
	public ResponseEntity<Object> getTestMethod(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception
	{
		return registrationDAOSupport.getTestMethod(inputMap, userInfo);
	}
	
	/**
	 * This method is used to update the test method objects for the particular test.
	 * @param userinfo object of the method
	 * @param map object of the method
	 * @return object of method based on the specified test
	 * @throws Exception 
	 */	
	@Override
	public ResponseEntity<Object> updateTestMethod(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return registrationDAOSupport.updateTestMethod(inputMap, userInfo);
	}
	
	@Override
	public List<FilterName> getFavoriteFilterName(UserInfo userInfo) throws Exception
	{
		return projectDAOSupport.getFavoriteFilterName(userInfo);
	}

	@Override
	public Map<String, Object> getCreateTestGroupTestSeqNo(final UserInfo userInfo, final List<String> listSample,
			final List<TestGroupTest> listTest, final int nregtypecode, final int nregsubtypecode,
			Map<String, Object> inputMap) throws Exception {
		return registrationDAOSupport.getCreateTestGroupTestSeqNo(userInfo, listSample, listTest, nregtypecode, nregsubtypecode, inputMap);
	}
	
	public ResponseEntity<Object> getRegistrationSubSampleTemplate(int ndesigntemplatemappingcode) throws Exception {
		return transactionDAOSupport.getRegistrationSubSampleTemplate(ndesigntemplatemappingcode);
	}
	
	public Map<String, Object> getDynamicRegistration(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return registrationDAOSupport.getDynamicRegistration(inputMap, userInfo);
	}
	
	public Map<String, Object> getTemplateDesign(final int ndesignTemplateCode, final int nformCode) throws Exception {
		return projectDAOSupport.getTemplateDesign(ndesignTemplateCode, nformCode);
	}
	
	public ResponseEntity<Object> getRegistrationTestAudit(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return transactionDAOSupport.getRegistrationTestAudit(inputMap, userInfo);
	}
	
	public List<Map<String, Object>> testAuditGet(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return transactionDAOSupport.testAuditGet(inputMap, userInfo);
	}
	
	@Override
	public List<TransactionStatus> getFilterStatus(final int nregTypeCode, final int nregSubTypeCode,
			final UserInfo userInfo) throws Exception {
		return transactionDAOSupport.getFilterStatus(nregTypeCode, nregSubTypeCode, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getOutsourceDetails(String npreregno, String ntransactionsamplecode,
			UserInfo userInfo) {
		return transactionDAOSupport.getOutsourceDetails(npreregno, ntransactionsamplecode, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getRegistrationTemplate(int nregTypeCode, int nregSubTypeCode) throws Exception {
		return transactionDAOSupport.getRegistrationTemplate(nregTypeCode, nregSubTypeCode);
	}
	
	public List<SampleType> getSampleType(final UserInfo userInfo) throws Exception {
		return transactionDAOSupport.getSampleType(userInfo);
	}
	
	public Map<String, Object> validateUniqueConstraint(final List<Map<String, Object>> masterUniqueValidation,
			final Map<String, Object> registration, final UserInfo userInfo, final String task, Class<?> tableName,
			final String columnName, boolean isMaster) throws Exception {
		return projectDAOSupport.validateUniqueConstraint(masterUniqueValidation, registration, userInfo, task, tableName, columnName, isMaster);
	}
	
	public Map<String, Object> seqNoSampleInsert(Map<String, Object> inputMap) throws Exception {
		return transactionDAOSupport.seqNoSampleInsert(inputMap);
	}
	
	public List<RegistrationSubType> getRegistrationSubType(final int nregTypeCode, final UserInfo userInfo)
			throws Exception {
		return transactionDAOSupport.getRegistrationSubType(nregTypeCode, userInfo);
	}
	
	public ResponseEntity<Object> getApproveConfigVersionRegTemplate(final int nregTypeCode, final int nregSubTypeCode,
			final int napproveConfigVersionCode) throws Exception {
		return transactionDAOSupport.getApproveConfigVersionRegTemplate(nregTypeCode, nregSubTypeCode, napproveConfigVersionCode);
	}
	
	public List<RegistrationType> getRegistrationType(final int nsampleTypeCode, final UserInfo userInfo)
			throws Exception {
		return transactionDAOSupport.getRegistrationType(nsampleTypeCode, userInfo);
	}
	
	public List<ApprovalConfigAutoapproval> getApprovalConfigVersion(int nregTypeCode, int nregSubTypeCode,
			UserInfo userInfo) throws Exception {
		return transactionDAOSupport.getApprovalConfigVersion(nregTypeCode, nregSubTypeCode, userInfo);
	}
}



	

						