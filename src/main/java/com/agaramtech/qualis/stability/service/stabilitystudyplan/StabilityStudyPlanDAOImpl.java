package com.agaramtech.qualis.stability.service.stabilitystudyplan;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.basemaster.model.TransactionStatus;
import com.agaramtech.qualis.configuration.model.ApprovalConfigAutoapproval;
import com.agaramtech.qualis.configuration.model.DesignTemplateMapping;
import com.agaramtech.qualis.configuration.model.SampleType;
import com.agaramtech.qualis.configuration.model.Settings;
import com.agaramtech.qualis.dynamicpreregdesign.model.ReactRegistrationTemplate;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationSubType;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationType;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.TransactionDAOSupport;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.registration.model.RegistrationSampleHistory;
import com.agaramtech.qualis.stability.model.SeqNoStability;
import com.agaramtech.qualis.stability.model.StbSchedulerTransaction;
import com.agaramtech.qualis.stability.model.StbStudyPlan;
import com.agaramtech.qualis.stability.model.StbTimePoint;
import com.agaramtech.qualis.stability.model.StbTimePointTest;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecSampleType;
import com.agaramtech.qualis.testgroup.model.TestGroupTest;
import com.agaramtech.qualis.testgroup.model.TestGroupTestParameter;
import com.agaramtech.qualis.testmanagement.model.TestMaster;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class StabilityStudyPlanDAOImpl implements StabilityStudyPlanDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(StabilityStudyPlanDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final TransactionDAOSupport transactionDAOSupport;
	// private final AuditUtilityFunction auditUtilityFunction;
	// private final FTPUtilityFunction ftpUtilityFunction;

	private ObjectMapper mapper = new ObjectMapper();


	public ResponseEntity<Object> getStabilityStudyPlan(UserInfo userInfo, String currentUIDate) throws Exception {
		 Map<String, Object> returnMap = new HashMap<>();
		System.out.println("call this method");
		List<ReactRegistrationTemplate> listReactRegistrationTemplate = new ArrayList<ReactRegistrationTemplate>();
		List<RegistrationType> lstRegistrationType = new ArrayList<>();
		List<RegistrationSubType> lstRegistrationSubType = new ArrayList<>();
		List<ApprovalConfigAutoapproval> listApprovalConfigAutoapproval = new ArrayList<>();
		List<TransactionStatus> listTransactionstatus = new ArrayList<>();

		returnMap = projectDAOSupport.getDateFromControlProperties(userInfo, currentUIDate, "datetime", "FromDate");

		List<SampleType> lstSampleType = getSampleType(userInfo);

		if (!lstSampleType.isEmpty()) {
			int nsampletypecode = lstSampleType.get(0).getNsampletypecode();
			returnMap.put("SampleTypeValue", lstSampleType.get(0));
			returnMap.put("nsampletypecode", lstSampleType.get(0).getNsampletypecode());
			returnMap.put("RealSampleTypeValue", lstSampleType.get(0));
			lstRegistrationType = getRegistrationType(nsampletypecode, userInfo);
			if (!lstRegistrationType.isEmpty()) {
				returnMap.put("RegTypeValue", lstRegistrationType.get(0));
				returnMap.put("nregtypecode", lstRegistrationType.get(0).getNregtypecode());
				returnMap.put("RealRegTypeValue", lstRegistrationType.get(0));

				lstRegistrationSubType = getRegistrationSubType(lstRegistrationType.get(0).getNregtypecode(), userInfo);

				if (!lstRegistrationSubType.isEmpty()) {

					returnMap.put("nregsubtypecode", lstRegistrationSubType.get(0).getNregsubtypecode());
					returnMap.put("nregsubtypeversioncode", lstRegistrationSubType.get(0).getNregsubtypeversioncode());
					returnMap.put("nneedsubsample", lstRegistrationSubType.get(0).isNneedsubsample());
					returnMap.put("RegSubTypeValue", lstRegistrationSubType.get(0));
					returnMap.put("RealRegSubTypeValue", lstRegistrationSubType.get(0));

					listTransactionstatus = getFilterStatus(lstRegistrationType.get(0).getNregtypecode(),
							lstRegistrationSubType.get(0).getNregsubtypecode(),userInfo);

					final TransactionStatus filterTransactionStatus = listTransactionstatus.get(0);

					returnMap.put("nfilterstatus", filterTransactionStatus.getNtransactionstatus());

					listApprovalConfigAutoapproval = getApprovalConfigVersion(
							lstRegistrationType.get(0).getNregtypecode(),
							lstRegistrationSubType.get(0).getNregsubtypecode(), userInfo);

					returnMap.put("ApprovalConfigVersionValue", listApprovalConfigAutoapproval.get(0));
					returnMap.put("napproveconfversioncode",
							listApprovalConfigAutoapproval.get(0).getNapproveconfversioncode());
					returnMap.put("RealApprovalConfigVersionValue", listApprovalConfigAutoapproval.get(0));

					if (!listApprovalConfigAutoapproval.isEmpty()) {

						listReactRegistrationTemplate = (List<ReactRegistrationTemplate>) getApproveConfigVersionRegTemplate(
								lstRegistrationType.get(0).getNregtypecode(),
								lstRegistrationSubType.get(0).getNregsubtypecode(),
								listApprovalConfigAutoapproval.get(0).getNapproveconfversioncode(),userInfo);

						final ReactRegistrationTemplate filterReactRegistrationTemplate = listReactRegistrationTemplate
								.get(0);

						if (!listReactRegistrationTemplate.isEmpty()) {

							returnMap.put("registrationTemplate", filterReactRegistrationTemplate);
							returnMap
									.put("SubSampleTemplate",
											getRegistrationSubSampleTemplate(
													filterReactRegistrationTemplate.getNdesigntemplatemappingcode(),userInfo)
													.getBody());
							returnMap.put("DynamicDesign",
									getTemplateDesign(filterReactRegistrationTemplate.getNdesigntemplatemappingcode(),
											userInfo.getNformcode(),userInfo));
							returnMap.put("ndesigntemplatemappingcode",
									filterReactRegistrationTemplate.getNdesigntemplatemappingcode());

							returnMap.putAll(getDynamicRegistration(returnMap, userInfo));
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
				}
			} else {
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
			}
		}

		returnMap.put("SampleType", lstSampleType);
		returnMap.put("RegistrationSubType", lstRegistrationSubType);
		returnMap.put("RegistrationType", lstRegistrationType);
		returnMap.put("FilterStatus", listTransactionstatus);
		returnMap.put("ApprovalConfigVersion", listApprovalConfigAutoapproval);
		returnMap.put("DesignTemplateMapping", listReactRegistrationTemplate);
		returnMap.put("FromDate", returnMap.get("FromDateWOUTC"));
		returnMap.put("ToDate", returnMap.get("ToDateWOUTC"));
		returnMap.put("RealFromDate", returnMap.get("FromDateWOUTC"));
		returnMap.put("RealToDate", returnMap.get("ToDateWOUTC"));
		returnMap.put("RealApprovalConfigVersionList", listApprovalConfigAutoapproval);
		returnMap.put("RealRegSubTypeList", lstRegistrationSubType);
		returnMap.put("RealRegTypeList", lstRegistrationType);
		returnMap.put("RealSampleTypeList", lstSampleType);
		returnMap.put("RealDesignTemplateMappingList", listReactRegistrationTemplate);
		returnMap.put("RealFilterStatuslist", listTransactionstatus);
		// returnMap.put("TransactionValidation",getTransactionStatus(userInfo));

//		String Query1 = " select * from reactregistrationtemplate rt "
//				+ " join designtemplatemapping dm on dm.nreactregtemplatecode =rt.nreactregtemplatecode "
//				+ " and dm.nsampletypecode =" + Enumeration.SampleType.STABILITY.getType()
//				+ " and dm.ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " "
//				+ " and dm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  ";
//		List<DesignTemplateMapping> lst1 = getJdbcTemplate().query(Query1, new DesignTemplateMapping());
//
//		if (lst1.size() > 0) {
//			returnMap.put("ndesigntemplatemappingcode", lst1.get(0).getNdesigntemplatemappingcode());
//			returnMap.put("DesignTemplateMapping", lst1);
//			returnMap.put("registrationTemplate",
//					getApproveConfigVersionRegTemplate(lst1.get(0).getNdesigntemplatemappingcode()).getBody());
//			returnMap.put("SubSampleTemplate",
//					getRegistrationSubSampleTemplate(lst1.get(0).getNdesigntemplatemappingcode()).getBody());
//			returnMap.put("DynamicDesign",
//					getTemplateDesign(lst1.get(0).getNdesigntemplatemappingcode(), userInfo.getNformcode()));
//		} else {
//			returnMap.put("DesignTemplateMapping", null);
//		}
//		returnMap.putAll((Map<String, Object>) getDynamicRegistration(returnMap, userInfo));
		return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
	}

	public List<SampleType> getSampleType(final UserInfo userInfo) throws Exception {

		String finalquery = "select st.nsampletypecode,st.ncategorybasedflowrequired,"
				+ " coalesce(st.jsondata->'sampletypename'->>'"	+ userInfo.getSlanguagetypecode()+ "',st.jsondata->'sampletypename'->>'en-US') as ssampletypename,"
				+ " st.nsorter, st.nprojectspecrequired, st.nportalrequired, st.noutsourcerequired, st.ncategorybasedflowrequired "
				+ " from SampleType st "
				+ " join registrationtype rt on st.nsampletypecode=rt.nsampletypecode "
				+ " join registrationsubtype rst on rt.nregtypecode=rst.nregtypecode "
				+ " join approvalconfig ac on rst.nregsubtypecode=ac.nregsubtypecode "
				+ " join approvalconfigversion acv on acv.napprovalconfigcode=ac.napprovalconfigcode "
				+ " where st.nsampletypecode="+Enumeration.SampleType.STABILITY.getType()+" "
				+ " and st.napprovalconfigview = "+ Enumeration.TransactionStatus.YES.gettransactionstatus()+" " 
				+ " and acv.ntransactionstatus = "+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+" "
				+ " and st.nsampletypecode > 0 "
				+ " and  st.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rt.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  "
				+ " and rst.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and ac.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and acv.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and st.nsitecode=  " + userInfo.getNmastersitecode() + " "
				+ " and acv.nsitecode=" + userInfo.getNmastersitecode()+ " "
				+ " group by st.nsampletypecode,st.nsorter order by st.nsorter; ";

		return jdbcTemplate.query(finalquery, new SampleType());
	}

	private Map<String, Object> getTemplateDesign(final int ndesignTemplateCode, final int nformCode, final UserInfo userInfo) throws Exception {

		final String str = "select jsondata->'" + nformCode + "' as jsondata from mappedtemplatefieldprops"
				+ " where ndesigntemplatemappingcode=" + ndesignTemplateCode+ " and nsitecode="+userInfo.getNmastersitecode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";

		final Map<String, Object> map = jdbcTemplate.queryForMap(str);
		return map;

	}

	public List<RegistrationType> getRegistrationType(final int nsampleTypeCode, final UserInfo userInfo)
			throws Exception {
		String Str = "Select * from sampletype  where  nsampletypecode=" + nsampleTypeCode +" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNmastersitecode()+" ";

		SampleType obj = (SampleType) jdbcUtilityFunction.queryForObject(Str, SampleType.class, jdbcTemplate);
		String ValidationQuery = "";
		if (obj.getNtransfiltertypecode() != -1 && userInfo.getNusercode() != -1) {
			int nmappingfieldcode = (obj.getNtransfiltertypecode() == 1) ? userInfo.getNdeptcode()
					: userInfo.getNuserrole();
			ValidationQuery = " and rst.nregsubtypecode in( SELECT rs.nregsubtypecode "
					+ "	FROM registrationsubtype rs "
					+ "	INNER JOIN transactionfiltertypeconfig ttc ON rs.nregsubtypecode = ttc.nregsubtypecode "
					+ "	LEFT JOIN transactionusers tu ON tu.ntransfiltertypeconfigcode = ttc.ntransfiltertypeconfigcode "
					+ "	WHERE ( ttc.nneedalluser = "+Enumeration.TransactionStatus.YES.gettransactionstatus()+"  and ttc.nstatus =  "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " AND ttc.nmappingfieldcode = "+ nmappingfieldcode + ") "
					+ "	OR ( ttc.nneedalluser = "+Enumeration.TransactionStatus.NO.gettransactionstatus()+"   AND ttc.nmappingfieldcode =" + nmappingfieldcode + ""
					+ "	AND tu.nusercode ="+ userInfo.getNusercode() + "   and ttc.nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " AND tu.nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") "
					+ " OR ( ttc.nneedalluser = "+Enumeration.TransactionStatus.NO.gettransactionstatus()+"  " + " AND ttc.nmappingfieldcode = "+Enumeration.TransactionStatus.NA.gettransactionstatus()+" "
					+ " AND tu.nusercode ="	+ userInfo.getNusercode() + " and ttc.nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " AND tu.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") "
					+ "	AND rs.nregtypecode = rt.nregtypecode) ";
		}
		String finalquery = "select  rt.nregtypecode,coalesce(rt.jsondata->'sregtypename'->>'"	+ userInfo.getSlanguagetypecode()+ "',rt.jsondata->'sregtypename'->>'en-US') as sregtypename,rt.nsorter "
				+ " from SampleType st   "
				+ " join registrationtype rt on st.nsampletypecode=rt.nsampletypecode "
				+ " join registrationsubtype rst on rt.nregtypecode=rst.nregtypecode "
				+ " join approvalconfig ac on rst.nregsubtypecode=ac.nregsubtypecode "
				+ " join approvalconfigversion acv on acv.napprovalconfigcode=ac.napprovalconfigcode "
				+ " where st.nsampletypecode =" + nsampleTypeCode + " and st.napprovalconfigview = "+ Enumeration.TransactionStatus.YES.gettransactionstatus()+ " "
				+ " and acv.ntransactionstatus = "+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+ " "
				+ " and st.nsampletypecode > 0 "
				+ " and st.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rt.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rst.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and ac.nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and acv.nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and st.nsitecode= "+userInfo.getNmastersitecode()+"  "
				+ " and acv.nsitecode=" + userInfo.getNmastersitecode() + " and rt.nregtypecode>0 " + ValidationQuery
				+ " group by rt.nregtypecode,coalesce(rt.jsondata->'sregtypename'->>'" + userInfo.getSlanguagetypecode()+ "',rt.jsondata->'sregtypename'->>'en-US'),rt.nsorter "
				+ "order by rt.nregtypecode desc";

		return jdbcTemplate.query(finalquery, new RegistrationType());
	}

	public List<RegistrationSubType> getRegistrationSubType(final int nregTypeCode, final UserInfo userInfo)
			throws Exception {

		String Str = " Select * from registrationtype rt "
				+ " join sampletype st on rt.nsampletypecode=st.nsampletypecode "
				+ " where  rt.nregTypeCode="+ nregTypeCode+ " and st.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and rt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";

		SampleType obj = (SampleType) jdbcUtilityFunction.queryForObject(Str, SampleType.class, jdbcTemplate);
		String validationQuery = "";
		if (obj.getNtransfiltertypecode() != -1 && userInfo.getNusercode() != -1) {
			int nmappingfieldcode = (obj.getNtransfiltertypecode() == 1) ? userInfo.getNdeptcode()
					: userInfo.getNuserrole();
			validationQuery = " and rst.nregsubtypecode in( SELECT rs.nregsubtypecode "
					+ " FROM registrationsubtype rs "
					+ " INNER JOIN transactionfiltertypeconfig ttc ON rs.nregsubtypecode = ttc.nregsubtypecode "
					+ " LEFT JOIN transactionusers tu ON tu.ntransfiltertypeconfigcode = ttc.ntransfiltertypeconfigcode "
					+ " WHERE ( ttc.nneedalluser = "+Enumeration.TransactionStatus.YES.gettransactionstatus()+"  AND ttc.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " AND ttc.nmappingfieldcode = "+ nmappingfieldcode + ")" 
					+ " OR ( ttc.nneedalluser = "+Enumeration.TransactionStatus.NO.gettransactionstatus()+"   AND ttc.nmappingfieldcode ="+ nmappingfieldcode + " AND tu.nusercode =" + userInfo.getNusercode() + " "
					+ " AND ttc.nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND tu.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") " 
					+ "  OR ( ttc.nneedalluser = "+Enumeration.TransactionStatus.NO.gettransactionstatus()+"  AND tu.nusercode =" + userInfo.getNusercode()	+ " AND ttc.nmappingfieldcode = "+Enumeration.TransactionStatus.NA.gettransactionstatus()+" "
					+ " AND ttc.nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND tu.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") " 
					+ " AND rs.nregtypecode = "	+ nregTypeCode + ")";
		}
		String finalquery = "select max(rsc.nregsubtypeversioncode) nregsubtypeversioncode,"
				+ " max(rsc.jsondata->>'nneedsubsample' )::Boolean nneedsubsample,"
				+ " max(rsc.jsondata->>'nneedjoballocation' )::Boolean nneedjoballocation,"
				+ " max(rsc.jsondata->>'nneedmyjob' )::Boolean nneedmyjob,"
				+ " max(rsc.jsondata->>'nneedtestinitiate')::Boolean nneedtestinitiate,"
				+ " max(rsc.jsondata->>'nneedtemplatebasedflow' )::Boolean nneedtemplatebasedflow,rst.nregsubtypecode,"
				+ " coalesce(rst.jsondata->'sregsubtypename'->>'" + userInfo.getSlanguagetypecode()+ "',rst.jsondata->'sregsubtypename'->>'en-US') as sregsubtypename,rst.nsorter, "
				+ " max(rsc.jsondata->>'ntestgroupspecrequired')::Boolean ntestgroupspecrequired "
				+ " from SampleType st "
				+ " join registrationtype rt on st.nsampletypecode=rt.nsampletypecode "
				+ " join registrationsubtype rst on rt.nregtypecode=rst.nregtypecode "
				+ " join approvalconfig ac on rst.nregsubtypecode=ac.nregsubtypecode "
				+ " join approvalconfigversion acv on acv.napprovalconfigcode=ac.napprovalconfigcode "
				+ " join regsubtypeconfigversion rsc on rsc.napprovalconfigcode=ac.napprovalconfigcode "
				+ " where st.napprovalconfigview = "+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " "
				+ " and acv.ntransactionstatus = "	+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+ " "
				+ " and rsc.ntransactionstatus= "+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+ " "
				+ " and rt.nregtypecode = " + nregTypeCode +" "
				+ " and st.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " 
				+ " and rt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rst.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and ac.nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and acv.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rsc.nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and st.nsampletypecode > 0  and rst.nregsubtypecode>0  "
				+ " and rsc.nsitecode=" + userInfo.getNmastersitecode()+ " "
				+ validationQuery + " group by rst.nregsubtypecode,coalesce(rst.jsondata->'sregsubtypename'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',rst.jsondata->'sregsubtypename'->>'en-US'),rst.nsorter order by rst.nregsubtypecode desc";
		return jdbcTemplate.query(finalquery, new RegistrationSubType());
	}

	public ResponseEntity<Object> getApproveConfigVersionRegTemplate(int ndesigntemplatemappingcode, final UserInfo userInfo) throws Exception {

		final String str = "select dm.ndesigntemplatemappingcode,rt.jsondata,CONCAT(rt.sregtemplatename,"
				+ " '(',cast(dm.nversionno as character varying),')') sregtemplatename "
				+ " from designtemplatemapping dm "
				+ " join reactregistrationtemplate rt on rt.nreactregtemplatecode=dm.nreactregtemplatecode "
				+ " where  "
				+ " dm.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and rt.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nsitecode="+userInfo.getNmastersitecode()+" "
				+ " and  dm.ndesigntemplatemappingcode="	+ ndesigntemplatemappingcode;

		final ReactRegistrationTemplate lstReactRegistrationTemplate = (ReactRegistrationTemplate) jdbcUtilityFunction
				.queryForObject(str, ReactRegistrationTemplate.class, jdbcTemplate);
		return new ResponseEntity<>(lstReactRegistrationTemplate, HttpStatus.OK);
	}

	public List<ApprovalConfigAutoapproval> getApprovalConfigVersion(int nregTypeCode, int nregSubTypeCode,
			UserInfo userInfo) throws Exception {

		final String getApprovalConfigVersion = "select aca.sversionname,aca.napprovalconfigcode,"
				+ " aca.napprovalconfigversioncode ,acv.ntransactionstatus,acv.ntreeversiontempcode,acv.napproveconfversioncode "
				+ " from   approvalconfigautoapproval aca "
				+ " join approvalconfig ac on ac.napprovalconfigcode=aca.napprovalconfigcode "
				+ " join approvalconfigversion acv on ac.napprovalconfigcode=acv.napprovalconfigcode and acv.napproveconfversioncode=aca.napprovalconfigversioncode "
				+ " where   "
				+ " ac.nregtypecode =" + nregTypeCode + " and ac.nregsubtypecode =" + nregSubTypeCode + " "
				+ " and acv.ntransactionstatus not in ( " + Enumeration.TransactionStatus.DRAFT.gettransactionstatus()+ ") "
				+ " and acv.nsitecode =" + userInfo.getNmastersitecode() + " "
				+ " and ac.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and acv.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and aca.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " group by acv.ntransactionstatus,aca.napprovalconfigversioncode,"
				+ " acv.ntreeversiontempcode,aca.napprovalconfigcode,aca.sversionname,acv.napproveconfversioncode order by aca.napprovalconfigversioncode desc";

		final List<ApprovalConfigAutoapproval> approvalVersion = jdbcTemplate.query(getApprovalConfigVersion,
				new ApprovalConfigAutoapproval());
		return approvalVersion;
	}

	public List<TransactionStatus> getFilterStatus(final int nregtypecode,final int nregsubtypecode,final UserInfo userinfo)
			throws Exception {
		final String filterStatus = "select ntranscode as ntransactionstatus,jsondata->'stransdisplaystatus'->>'"+ userinfo.getSlanguagetypecode() + "' stransdisplaystatus"
				+ " from transactionstatus where ntranscode in ("+Enumeration.TransactionStatus.ALL.gettransactionstatus()+","
				+ " "+Enumeration.TransactionStatus.DRAFT.gettransactionstatus()+","
				+ " "+Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+") "
				+ " order by ntranscode";
		return jdbcTemplate.query(filterStatus, new TransactionStatus());
	}

	public List<ReactRegistrationTemplate> getApproveConfigVersionRegTemplate(final int nregTypeCode,
			final int nregSubTypeCode, final int napproveConfigVersionCode,final UserInfo userinfo) throws Exception {

		final String str = "select dm.ndesigntemplatemappingcode,rt.jsondata,CONCAT(rt.sregtemplatename,"+ " '(',cast(dm.nversionno as character varying),')') sregtemplatename "
				+ " from designtemplatemapping dm "
				+ " join reactregistrationtemplate rt on rt.nreactregtemplatecode=dm.nreactregtemplatecode "
				+ " join approvalconfigversion acv on acv.ndesigntemplatemappingcode=dm.ndesigntemplatemappingcode"
				+ " where  "
				+ " dm.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rt.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rt.nsitecode="+ userinfo.getNmastersitecode() + " "
				+ " and  acv.napproveconfversioncode="+ napproveConfigVersionCode;

		final List<ReactRegistrationTemplate> lstReactRegistrationTemplate = jdbcTemplate.query(str,
				new ReactRegistrationTemplate());

		return lstReactRegistrationTemplate;

	}

	public ResponseEntity<Object> getRegistrationSubSampleTemplate(final int ndesigntemplatemappingcode,final UserInfo userInfo) throws Exception {

		final String str = "select dm.ndesigntemplatemappingcode,rt.jsondata "
				+ " from designtemplatemapping dm "
				+ " join reactregistrationtemplate rt on rt.nreactregtemplatecode=nsubsampletemplatecode "
				+ " where  "
				+ " dm.ndesigntemplatemappingcode=" + ndesigntemplatemappingcode + ""
				+ " and dm.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
				+ " and rt.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
				+ " and rt.nsitecode="+ userInfo.getNmastersitecode()+ " ";


		final ReactRegistrationTemplate lstReactRegistrationTemplate = (ReactRegistrationTemplate) jdbcUtilityFunction
				.queryForObject(str, ReactRegistrationTemplate.class, jdbcTemplate);
		return new ResponseEntity<>(lstReactRegistrationTemplate, HttpStatus.OK);

	}

	public Map<String, Object> getDynamicRegistration(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> objMap = new HashMap<>();
		short nfilterstatus;
		if (inputMap.get("nfilterstatus").getClass().getTypeName().equals("java.lang.Integer")) {
			nfilterstatus = ((Integer) inputMap.get("nfilterstatus")).shortValue();
		} else {
			nfilterstatus = (short) inputMap.get("nfilterstatus");
		}

		final String query1 = "select * from fn_stbstudyplanget('" + inputMap.get("FromDate") + "'::text," + "'"
				+ inputMap.get("ToDate") + "'::text" + ",'" + inputMap.get("nstbstudyplancode") + "'::text,'"
				+ userInfo.getSlanguagetypecode() + "'::text," + inputMap.get("nregtypecode") + ","
				+ inputMap.get("nregsubtypecode") + "," + nfilterstatus + "," + userInfo.getNtranssitecode() + ","
				+ inputMap.get("napproveconfversioncode") + ")";

		LOGGER.info("Sample Start========?" + LocalDateTime.now());
		String lstData1 = jdbcTemplate.queryForObject(query1, String.class);
		LOGGER.info("Sample end========?" + query1 + " :" + LocalDateTime.now());
		List<Map<String, Object>> lstData = new ArrayList<>();

		if (lstData1 != null) {
			lstData = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(lstData1,
					userInfo, true, (int) inputMap.get("ndesigntemplatemappingcode"), "sample");
			LOGGER.info("Sample Size" + lstData.size());

			objMap.put("StabilityStudyPlanGet", lstData);

			String sstbstudyplancode = "";
			sstbstudyplancode = String.valueOf(lstData.get(lstData.size() - 1).get("nstbstudyplancode"));
			inputMap.put("nstbstudyplancode", sstbstudyplancode);
			objMap.put("selectedStabilityStudyPlan", Arrays.asList(lstData.get(lstData.size() - 1)));

			Map<String, Object> map = (Map<String, Object>) getStbTimePoint(inputMap, userInfo);
			objMap.putAll(map);
		} else {
			objMap.put("selectedStabilityStudyPlan", lstData);
			objMap.put("StabilityStudyPlanGet", lstData);

			objMap.put("selectedStbTimePoint", lstData);
			objMap.put("StbTimePointGet", lstData);

			objMap.put("StbTimePointTestGet", lstData);
			objMap.put("selectedStbTimePointTest", lstData);

			objMap.put("StbTimePointTestParameter", lstData);
		}
		return objMap;

	}

	public Map<String, Object> getStbTimePoint(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Map<String, Object> objMap = new HashMap<>();

		final String query1 = "select * from fn_stbtimepointget('" + inputMap.get("nstbstudyplancode") + "'::text,"
				+ "'" + inputMap.get("nstbtimepointcode") + "'::text" + "," + userInfo.getNtranssitecode() + ",'"
				+ userInfo.getSlanguagetypecode() + "')";

		LOGGER.info("sub sample Start========?" + LocalDateTime.now());
		LOGGER.info("sub sample query:" + query1);
		final String lstData1 = jdbcTemplate.queryForObject(query1, String.class);
		LOGGER.info("sub sample end========?" + LocalDateTime.now());

		if (lstData1 != null) {

			List<Map<String, Object>> lstData = (List<Map<String, Object>>) projectDAOSupport
					.getSiteLocalTimeFromUTCForDynamicTemplate(lstData1, userInfo, true,
							(int) inputMap.get("ndesigntemplatemappingcode"), "subsample");

			objMap.put("StbTimePointGet", lstData);

			String sstbtimepointcode = "";

			if (inputMap.containsKey("nstbtimepointcode")) {
				inputMap.put("nstbtimepointcode", inputMap.get("ntransactionsamplecode"));
				objMap.put("selectedStbTimePointTest", Arrays.asList(lstData.get(lstData.size() - 1)));
				objMap.put("selectedStbTimePoint", lstData);
			} else {
				sstbtimepointcode = String.valueOf(lstData.get(lstData.size() - 1).get("nstbtimepointcode"));
				objMap.put("selectedStbTimePointTest", Arrays.asList(lstData.get(lstData.size() - 1)));
				inputMap.put("nstbtimepointcode", sstbtimepointcode);
				objMap.put("selectedStbTimePoint", Arrays.asList(lstData.get(lstData.size() - 1)));
			}

			Map<String, Object> map = (Map<String, Object>) getStbTimePointTest(inputMap, userInfo);
			objMap.putAll(map);

		} else {
			objMap.put("selectedStbTimePoint", Arrays.asList());
			objMap.put("StbTimePointGet", Arrays.asList());
			objMap.put("StbTimePointTestGet", Arrays.asList());
			objMap.put("selectedStbTimePointTest", Arrays.asList());
		}
		return objMap;

	}

//Working Get start
	public Map<String, Object> getStbTimePointTest(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Map<String, Object> objMap = new HashMap<>();

		final String query1 = "select * from fn_stbtimepointtestget('" + inputMap.get("nstbstudyplancode") + "'::text,"
				+ "'" + inputMap.get("nstbtimepointcode") + "'::text" + ",'" + inputMap.get("nstbtimepointtestcode")
				+ "'::text," + userInfo.getNtranssitecode() + ",'" + userInfo.getSlanguagetypecode() + "')";

		LOGGER.info("test Start========?" + LocalDateTime.now());
		LOGGER.info("test Start========?" + LocalDateTime.now());

		final String lstData2 = jdbcTemplate.queryForObject(query1, String.class);
		LOGGER.info("fn_registrationtestget:" + query1);
		LOGGER.info("fn_registrationtestget:" + query1);
		LOGGER.info("test end========?" + LocalDateTime.now());
		LOGGER.info("test end========?" + LocalDateTime.now());
		if (lstData2 != null) {
			List<Map<String, Object>> lstData = (List<Map<String, Object>>) projectDAOSupport
					.getSiteLocalTimeFromUTCForDynamicTemplate(lstData2, userInfo, true,
							(int) inputMap.get("ndesigntemplatemappingcode"), "test");
			objMap.put("StbTimePointTestGet", lstData);
			objMap.put("selectedStbTimePointTest", Arrays.asList(lstData.get(lstData.size() - 1)));
		} else {
			objMap.put("sstbtimepointtestcode", Arrays.asList());
			objMap.put("RegistrationParameter", Arrays.asList());
			objMap.put("StbTimePointTestGet", Arrays.asList());

		}

		return objMap;

	}

	public ResponseEntity<Object> getApprovedVersionTemplateMapping(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		final Map<String, Object> map = new HashMap<String, Object>();

		String Query1 = " select * from reactregistrationtemplate rt "
				+ " join designtemplatemapping dm on dm.nreactregtemplatecode =rt.nreactregtemplatecode "
				+ " where dm.nsampletypecode =" + Enumeration.SampleType.GOODSIN.getType() + " "
				+ "and dm.ntransactionstatus="+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " " 
				+ " and dm.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " a"
				+ "nd rt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and dm.nsitecode="+userInfo.getNmastersitecode()+" ";
		List<DesignTemplateMapping> lst1 = jdbcTemplate.query(Query1, new DesignTemplateMapping());

		if (lst1.size() > 0) {
			map.put("ndesigntemplatemappingcode", lst1.get(0).getNdesigntemplatemappingcode());
			inputMap.put("ndesigntemplatemappingcode", lst1.get(0).getNdesigntemplatemappingcode());
			map.put("DesignTemplateMapping", lst1);
		} else {
			map.put("DesignTemplateMapping", null);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);

	}

	public ResponseEntity<Object> createSample(Map<String, Object> inputMap) throws Exception {

		Map<String, Object> objmap1 = new HashMap<>();
		Map<String, Object> objmap = validateAndInsertSeqNoRegistrationData(inputMap);
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

	@SuppressWarnings("unchecked")
	public Map<String, Object> validateAndInsertSeqNoRegistrationData(Map<String, Object> inputMap) throws Exception {

		Map<String, Object> returnMap = new HashMap<String, Object>();
		final ObjectMapper objectMapper = new ObjectMapper();

		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final List<TestGroupTest> listTest = objectMapper.convertValue(inputMap.get("testgrouptest"),
				new TypeReference<List<TestGroupTest>>() {
				});

		Boolean skipMethodValidity = null;
		if (inputMap.containsKey("skipmethodvalidity")) {
			skipMethodValidity = (Boolean) inputMap.get("skipmethodvalidity");
		}

		final String sntestgrouptestcode = stringUtilityFunction.fnDynamicListToString(listTest,
				"getNtestgrouptestcode");

		List<TestGroupTest> expiredMethodTestList = new ArrayList<TestGroupTest>();
		if (skipMethodValidity == false) {
			expiredMethodTestList = getTestByExpiredMethod(sntestgrouptestcode, userInfo);
		}
		if (expiredMethodTestList.isEmpty()) {

			List<StbTimePoint> subSampleInputList = objectMapper.convertValue(inputMap.get("StbTimePoint"),
					new TypeReference<List<StbTimePoint>>() {
					});

//			final StbStudyPlan registration = objectMapper.convertValue(inputMap.get("StabilityStudyPlan"),
//					new TypeReference<StbStudyPlan>() {
//					});
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
			Map<String, Object> map = validateUniqueConstraint(samplecombinationunique,
					(Map<String, Object>) inputMap.get("StabilityStudyPlan"), userInfo, "create", StbStudyPlan.class,
					"nstbstudyplancode", false);
			if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
					.equals(map.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
				// for normal preregister flow if we sent 1 means,order sample already
				// preregitered so current sample also sample same order so store as sub sample
				// in that specific order (only for clinic type)
				map.put("nflag", 2);
				return map;
			}
			List<Map<String, Object>> registrationsam = (List<Map<String, Object>>) inputMap.get("StbTimePoint");
			for (int i = 0; i < subSampleInputList.size(); i++) {
				Map<String, Object> map1 = validateUniqueConstraint(subsamplecombinationunique,
						(Map<String, Object>) registrationsam.get(i), userInfo, "create", StbTimePoint.class,
						"nstbstudyplancode", false);
				if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
						.equals(map1.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
					// for normal preregister flow if we sent 1 means,order sample already
					// preregitered so current sample also sample same order so store as sub sample
					// in that specific order (only for clinic type)
					map1.put("nflag", 2);
					return map1;
				}
			}
//			JSONObject objJson = new JSONObject(registration.getJsondata());
//
//			String manualOrderInsert = "";
//			int nsampletypecode = (int) inputMap.get("nsampletypecode");
//
//			Boolean validatePreventTB = true;
//
//			StbTimePoint externalorderList = null;

			final String strSelectSeqno = "select stablename,nsequenceno from seqnostability  where stablename "
					+ "in ('stbstudyplan','stbtimepoint','stbtimepointtest','stbtimepointparameter' ) and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";

			final List<?> lstMultiSeqNo = projectDAOSupport.getMultipleEntitiesResultSetInList(strSelectSeqno,
					jdbcTemplate, SeqNoStability.class);

			final List<SeqNoStability> lstSeqNoReg = (List<SeqNoStability>) lstMultiSeqNo.get(0);

			returnMap = lstSeqNoReg.stream().collect(Collectors.toMap(SeqNoStability::getStablename,
					SeqNoRegistration -> SeqNoRegistration.getNsequenceno()));

			String strSeqnoUpdate = "Update seqnostability set nsequenceno = "	+ ((int) returnMap.get("stbstudyplan") + 1) + " where stablename = 'stbstudyplan' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ;"
					+ " Update seqnostability set nsequenceno = "+ ((int) returnMap.get("stbtimepointparameter") + parameterCount)	+ " where stablename = 'stbtimepointparameter' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";" 
					+ " Update seqnostability set nsequenceno = "+ ((int) returnMap.get("stbtimepoint") + subSampleInputList.size())	+ " where stablename = 'stbtimepoint' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";"
					+ " Update seqnostability set nsequenceno = "+ ((int) returnMap.get("stbtimepointtest")	+ testCount) + " where stablename = 'stbtimepointtest' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ;";

			jdbcTemplate.execute(strSeqnoUpdate);

			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.SUCCESS.getreturnstatus());

		} else {
			final String expiredMethod = stringUtilityFunction.fnDynamicListToString(expiredMethodTestList,
					"getSmethodname");
			returnMap.put("NeedConfirmAlert", true);
			final String message = commonFunction.getMultilingualMessage("IDS_TESTWITHEXPIREDMETHODCONFIRM",
					userInfo.getSlanguagefilename());
			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					message.concat(" " + expiredMethod + " ?"));
		}
		returnMap.put("nflag", 2);
		return returnMap;
	}

	private List<TestGroupTest> getTestByExpiredMethod(final String testGroupTestCode, final UserInfo userInfo)
			throws Exception {
		List<TestGroupTest> expiredMethodTestList = new ArrayList<TestGroupTest>();
		if (testGroupTestCode != null && testGroupTestCode.trim().length() > 0) {
			final String queryString = " select m1.nmethodcode, m1.smethodname, tgt.ntestcode,"
					+ " tm.stestsynonym,tm.stestname"
					+ " from testgrouptest tgt "
					+ " join method m1 on tgt.nmethodcode=m1.nmethodcode  "
					+ " join testmaster tm on tgt.ntestcode=tm.ntestcode "
					+ " where tgt.ntestgrouptestcode in (" + testGroupTestCode + ") "
					+ " and tgt.nmethodcode in (( " + " select a.nmethodcode from ( "
					+ " select  m.nmethodcode"
					+ " from method m"
					+ " join methodvalidity md on m.nmethodcode = md.nmethodcode"
					+ " WHERE m.nneedvalidity= "+ Enumeration.TransactionStatus.YES.gettransactionstatus()+ " "
					+ " and md.ntransactionstatus = "+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ""
					+ " and m.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
					+ " and md.nstatus =  "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "   "
					+ " and m.nsitecode="+userInfo.getNmastersitecode()+" and md.nsitecode="+userInfo.getNmastersitecode()+" "
					+ " union "
					+ " select  m.nmethodcode "
					+ " from method m "
					+ " join methodvalidity md on m.nmethodcode = md.nmethodcode"
					+ " WHERE m.nneedvalidity= "+ Enumeration.TransactionStatus.YES.gettransactionstatus()+ " "
					+ " and md.ntransactionstatus ="+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " "
					+ " AND md.dvalidityenddate < '"+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'"
					+ " and m.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
					+ " and md.nstatus =  "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "   "
					+ " and m.nsitecode="+userInfo.getNmastersitecode()+" and md.nsitecode="+userInfo.getNmastersitecode()+" "
					+ " )a "
					+ ")) and tgt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and m1.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and tm.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and tgt.nsitecode="+userInfo.getNmastersitecode()+" and m1.nsitecode="+userInfo.getNmastersitecode()+" "
					+ " and tm.nsitecode="+userInfo.getNmastersitecode()+" ";

			expiredMethodTestList = jdbcTemplate.query(queryString, new TestGroupTest());

		}
		return expiredMethodTestList;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> insertRegistration(Map<String, Object> inputMap) throws Exception {

		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		Map<String, Object> returnMap = new HashMap<>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObject = new JSONObject();

		final ObjectMapper objmap = new ObjectMapper();
		objmap.registerModule(new JavaTimeModule());

		final UserInfo userInfo = objmap.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		final StbStudyPlan registration = objmap.convertValue(inputMap.get("StabilityStudyPlan"),
				new TypeReference<StbStudyPlan>() {
				});
		registration.setNisiqcmaterial((short) Enumeration.TransactionStatus.NO.gettransactionstatus());

		final List<StbTimePoint> registrationSample = objmap.convertValue(inputMap.get("StbTimePoint"),
				new TypeReference<List<StbTimePoint>>() {
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
		int npreregno = (int) inputMap.get("stbstudyplan");
		int nregistrationparametercode = (int) inputMap.get("stbtimepointparameter");
		int nregsamplecode = (int) inputMap.get("stbtimepoint");
		int napproveconfversioncode = (int) inputMap.get("napproveconfversioncode");
		int ntransactiontestcode = (int) inputMap.get("stbtimepointtest");

		// int seqordertest = -1;

		++npreregno;
		++ntransactiontestcode;
		++nregistrationparametercode;
		int nage = 0;
		int ngendercode = 0;
		String sQuery = "";

		if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()) {

			String sYears = "SELECT CURRENT_DATE - TO_DATE('" + inputMap.get("sDob") + "','"
					+ userInfo.getSpgsitedatetime() + "' )";
			long ageInDays = (long) jdbcUtilityFunction.queryForObject(sYears, Long.class, jdbcTemplate);

			nage = (int) inputMap.get("AgeData");
			ngendercode = (int) inputMap.get("ngendercode");
			sQuery = "json_build_object('ntransactionresultcode'," + nregistrationparametercode
					+ "+RANK() over(order by tgtp.ntestgrouptestparametercode),'ntransactiontestcode',"
					+ ntransactiontestcode + "+DENSE_RANK() over(order by tgtp.ntestgrouptestcode),'npreregno',"
					+ npreregno + ")::jsonb || " + " case when tgtp.nparametertypecode="
					+ Enumeration.ParameterType.NUMERIC.getparametertype() + " then case when  " + " tgtcs.ngendercode="
					+ ngendercode + " and " + ageInDays + " between case when tgtcs.nfromageperiod="
					+ Enumeration.Period.Years.getPeriod() + ""
					+ " then (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(years => tgtcs.nfromage)))::int)"
					+ " when tgtcs.nfromageperiod=" + Enumeration.Period.Month.getPeriod() + " then "
					+ " (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(months => tgtcs.nfromage)))::int) "
					+ " else (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(days => tgtcs.nfromage)))::int) end "
					+ " and case when tgtcs.ntoageperiod=" + Enumeration.Period.Years.getPeriod()
					+ " then (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(years => tgtcs.ntoage)))::int) "
					+ " when tgtcs.ntoageperiod=" + Enumeration.Period.Month.getPeriod()
					+ " then (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(months => tgtcs.ntoage)))::int) "
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
					+ "'sparametersynonym',tgtp.sparametersynonym,'nresultaccuracycode',tgtp.nresultaccuracycode,'sresultaccuracyname', ra.sresultaccuracyname )::jsonb end else jsonb_build_object("
					+ " 'sminb',case when tgtcs.slowb='null' then NULL else tgtcs.slowb end,"
					+ " 'smina',case when tgtcs.slowa='null' then NULL else tgtcs.slowa end,"
					+ " 'smaxa',case when tgtcs.shigha='null' then NULL else tgtcs.shigha end,"
					+ " 'smaxb',case when tgtcs.shighb='null' then NULL else tgtcs.shighb end,"
					+ " 'sresultvalue',case when tgtcs.sresultvalue='null' then NULL else tgtcs.sresultvalue end,"
					+ " 'nroundingdigits',tgtp.nroundingdigits,'sresult',NULL,'sfinal',NULL,'stestsynonym',concat(tgt.stestsynonym,'[1][0]'),"
					+ " 'sparametersynonym',tgtp.sparametersynonym,'nresultaccuracycode',tgtp.nresultaccuracycode,'sresultaccuracyname', ra.sresultaccuracyname )::jsonb end jsondata, "
					+ userInfo.getNtranssitecode() + " nsitecode,"
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "nstatus from resultaccuracy ra,testgrouptest tgt inner join "
					+ " testgrouptestparameter tgtp  on tgt.ntestgrouptestcode = tgtp.ntestgrouptestcode "
					+ " left outer join testgrouptestclinicalspec tgtcs on tgtcs.ntestgrouptestparametercode = tgtp.ntestgrouptestparametercode "
					+ " and tgtcs.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and tgtcs.ngendercode=" + ngendercode + " and " + ageInDays
					+ " between case when tgtcs.nfromageperiod=" + Enumeration.Period.Years.getPeriod() + ""
					+ " then (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(years => tgtcs.nfromage)))::int)"
					+ " when tgtcs.nfromageperiod=" + Enumeration.Period.Month.getPeriod() + " then "
					+ " (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(months => tgtcs.nfromage)))::int) "
					+ " else (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(days => tgtcs.nfromage)))::int) end "
					+ " and case when tgtcs.ntoageperiod=" + Enumeration.Period.Years.getPeriod()
					+ " then (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(years => tgtcs.ntoage)))::int) "
					+ " when tgtcs.ntoageperiod=" + Enumeration.Period.Month.getPeriod()
					+ " then (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(months => tgtcs.ntoage)))::int) "
					+ " else (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(days => tgtcs.ntoage)))::int) end ";

			returnMap.put("nAge", nage);
			returnMap.put("nGendercode", ngendercode);
		} else {
			sQuery = "(json_build_object('nstbtimepointparametercode'," + nregistrationparametercode
					+ "+RANK() over(order by tgtp.ntestgrouptestparametercode),'nstbtimepointtestcode',"
					+ ntransactiontestcode + "+DENSE_RANK() over(order by tgtp.ntestgrouptestcode),'nstbstudyplancode',"
					+ npreregno
					+ ",'sminlod',tgtnp.sminlod,'smaxlod',tgtnp.smaxlod,'sminb',tgtnp.sminb,'smina',tgtnp.smina,"
					+ "'smaxa',tgtnp.smaxa,'smaxb',tgtnp.smaxb,'sminloq',tgtnp.sminloq,'smaxloq',tgtnp.smaxloq,"
					+ "'sdisregard',tgtnp.sdisregard,'sresultvalue',tgtnp.sresultvalue,'ngradecode',tgtnp.ngradecode,"
					+ "'nresultaccuracycode',tgtp.nresultaccuracycode,'sresultaccuracyname', ra.sresultaccuracyname, "
					+ "'nroundingdigits',tgtp.nroundingdigits,'sresult',NULL,'sfinal',NULL,'stestsynonym',concat(tgt.stestsynonym,'[1][0]'),"
					+ "'sparametersynonym',tgtp.sparametersynonym)::jsonb || case when tgtp.nparametertypecode="
					+ Enumeration.ParameterType.NUMERIC.getparametertype() + " then "
					+ "json_build_object('sresultvalue',tgtnp.sresultvalue" + "):: jsonb when tgtp.nparametertypecode="
					+ Enumeration.ParameterType.PREDEFINED.getparametertype() + " then "
					+ "json_build_object('sresultvalue', tgtpp.spredefinedname):: jsonb"
					+ " when tgtp.nparametertypecode=" + Enumeration.ParameterType.CHARACTER.getparametertype()
					+ " then " + "json_build_object('sresultvalue', tgtcp.scharname)::jsonb else json_build_object("
					+ "'sresultvalue',tgtnp.sresultvalue)::jsonb end)::jsonb " + " jsondata,"
					+ userInfo.getNtranssitecode() + " nsitecode,"
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
					+ " from resultaccuracy ra,testgrouptest tgt,testgrouptestparameter tgtp ";
		}

		inputMap.put("nsitecode", userInfo.getNtranssitecode());

		String strRegistrationInsert = "";
//		String strRegistrationHistory = "";
//		String strRegistrationArno = "";
		String strRegistrationSample = "";
//		String strRegistrationSampleHistory = "";
//		String strRegistrationSampleArno = "";
//		String strDecisionHistory = "";
//		String strRegistrationStatusBlink = "";
//		String externalOrderSampleQuery = "";
//		String externalOrderTestQuery = "";
		int sampleCount = registrationSample.size();
//		boolean statusflag = false;

		JSONObject jsoneditRegistration = new JSONObject(registration.getJsondata());
		JSONObject jsonuiRegistration = new JSONObject(registration.getJsonuidata());

		if (!dateList.isEmpty()) {

			jsoneditRegistration = (JSONObject) dateUtilityFunction
					.convertJSONInputDateToUTCByZone(jsoneditRegistration, dateList, false, userInfo);
			jsonuiRegistration = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonuiRegistration,
					dateList, false, userInfo);
			final Map<String, Object> obj = commonFunction.validateDynamicDateContraints(jsonuiRegistration,
					sampleDateConstraint, userInfo);
			if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
					.equals((String) obj.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
				return obj;
			}
		}

		jsonuiRegistration.put("nstbstudyplancode", npreregno);
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
		jsonuiRegistration.put("Transaction Date",
				dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ").replace("Z", ""));

		strRegistrationInsert = strRegistrationInsert + "(" + npreregno + "," + registration.getNprotocolcode() + ","
				+ nsampletypecode + "," + nregtypecode + "," + nregsubtypecode + "," + registration.getNproductcatcode()
				+ "," + registration.getNproductcode() + "," + registration.getNinstrumentcatcode() + ","
				+ registration.getNinstrumentcode() + "," + registration.getNmaterialcatcode() + ","
				+ registration.getNmaterialcode() + "," + registration.getNtemplatemanipulationcode() + ","
				+ registration.getNallottedspeccode() + "," + userInfo.getNtranssitecode() + ",'"
				+ stringUtilityFunction.replaceQuote(jsoneditRegistration.toString()) + "'::JSONB,'"
				+ stringUtilityFunction.replaceQuote(jsonuiRegistration.toString()) + "'::JSONB,"
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
				+ registration.getNdesigntemplatemappingcode() + "," + registration.getNregsubtypeversioncode() + ","
				+ registration.getNprojectmastercode() + "," + registration.getNisiqcmaterial() + ","
				+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ",'"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + napproveconfversioncode + "),";

		StringJoiner joinerSample = new StringJoiner(",");
		for (int i = sampleCount - 1; i >= 0; i--) {
			// nregsamplecode++;
			// seqordersample++;
			joinerSample.add(String.valueOf(nregsamplecode));

			JSONObject jsoneditObj = new JSONObject(registrationSample.get(i).getJsondata());
			JSONObject jsonuiObj = new JSONObject(registrationSample.get(i).getJsonuidata());
			if (!SubSampledateList.isEmpty()) {
				jsoneditObj = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsoneditObj,
						SubSampledateList, false, userInfo);
				jsonuiObj = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonuiObj,
						SubSampledateList, false, userInfo);
				final Map<String, Object> obj = commonFunction.validateDynamicDateContraints(jsonuiObj,
						subSampleDateConstraint, userInfo);
				if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
						.equals((String) obj.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
					return obj;
				}
			}

			jsonuiObj.put("nstbtimepointcode", nregsamplecode);
			jsonuiObj.put("nstbstudyplancode", npreregno);
			jsonuiObj.put("nspecsampletypecode", registrationSample.get(i).getNspecsampletypecode());
			jsonuiObj.put("ncomponentcode", registrationSample.get(i).getNcomponentcode());

			strRegistrationSample = strRegistrationSample + " (" + nregsamplecode + "," + npreregno + ",'"
					+ registrationSample.get(i).getNspecsampletypecode() + "',"
					+ registrationSample.get(i).getNcomponentcode() + ",'"
					+ stringUtilityFunction.replaceQuote(jsoneditObj.toString()) + "'::JSONB,'"
					+ stringUtilityFunction.replaceQuote(jsonuiObj.toString()) + "'::JSONB,"
					+ userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " ," + Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ",'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'),";

			List<StbTimePoint> comp = new ArrayList<>();
			comp.add(registrationSample.get(i));
			List<TestGroupTest> lsttest1 = tgtTestInputList.stream().filter(x -> x.getSlno() == comp.get(0).getSlno())
					.collect(Collectors.toList());

			final String stestcode = lsttest1.stream().map(x -> String.valueOf(x.getNtestgrouptestcode())).distinct()
					.collect(Collectors.joining(","));

			if (!lsttest1.isEmpty()) {

				// FRS-00410- To add test based on replicate count defined in testgroup test

				final Map<String, Object> testHistoryParameterMap = insertTestHistoryParameter(stestcode, sQuery,
						userInfo, npreregno, nregsamplecode, ntransactiontestcode, nregistrationparametercode,
						new ArrayList<Integer>(), inputMap);
				ntransactiontestcode = (int) testHistoryParameterMap.get("nstbtimepointtestcode");
				nregistrationparametercode = (int) testHistoryParameterMap.get("nstbtimepointparametercode");
			}

		}

		strRegistrationInsert = "Insert into stbstudyplan (nstbstudyplancode, nprotocolcode, nsampletypecode, nregtypecode,nregsubtypecode,nproductcatcode,nproductcode,ninstrumentcatcode,"
				+ "ninstrumentcode, nmaterialcatcode, nmaterialcode,ntemplatemanipulationcode,nallottedspeccode,nsitecode,jsondata,jsonuidata,nstatus,"
				+ "ndesigntemplatemappingcode,nregsubtypeversioncode,nprojectmastercode,nisiqcmaterial,ntransactionstatus,dtransactiondate,napprovalversioncode) values "
				+ strRegistrationInsert.substring(0, strRegistrationInsert.length() - 1) + ";";
		jdbcTemplate.execute(strRegistrationInsert);

		strRegistrationSample = "insert into stbtimepoint(nstbtimepointcode, nstbstudyplancode, nspecsampletypecode, ncomponentcode,jsondata, "
				+ " jsonuidata,nsitecode,nstatus,ntransactionstatus,dtransactiondate) values "
				+ strRegistrationSample.substring(0, strRegistrationSample.length() - 1) + " ;";
		jdbcTemplate.execute(strRegistrationSample);

		inputMap.put("nstbstudyplancode", String.valueOf(npreregno));

		// inputMap.put("nsubsample",3);
		returnMap = getDynamicRegistration(inputMap, userInfo);
		returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
				Enumeration.ReturnStatus.SUCCESS.getreturnstatus());

		jsonAuditObject.put("stbstudyplan", (List<Map<String, Object>>) returnMap.get("StabilityStudyPlanGet"));
		actionType.put("stbstudyplan", "IDS_ADDSTABILITYSTUDYPLAN");

		if ((boolean) inputMap.get("nneedsubsample")) {
			jsonAuditObject.put("stbtimepoint", (List<Map<String, Object>>) returnMap.get("StbTimePointGet"));
			actionType.put("stbtimepoint", "IDS_ADDTIMEPOINT");

			inputMap.put("nstbtimepointcode", null);
		} else {
			jsonAuditObject.put("stbtimepointtest", (List<Map<String, Object>>) returnMap.get("StbTimePointTestGet"));
		}

		auditmap.put("nregtypecode", nregtypecode);
		auditmap.put("nregsubtypecode", nregsubtypecode);
		auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));

		actionType.put("stbtimepointtest", "IDS_ADDTIMEPOINTEST");

		// Portal Status Update
		return returnMap;

	}

	public Map<String, Object> validateUniqueConstraint(final List<Map<String, Object>> masterUniqueValidation,
			final Map<String, Object> registration, final UserInfo userInfo, final String task, Class<?> tableName,
			final String columnName, boolean isMaster) throws Exception {

		// Map<String, Object> jsonUIData = (Map<String, Object>)
		// registration.get("jsonuidata") ;
		ObjectMapper objMapper = new ObjectMapper();
		Map<String, Object> jsonUIData = objMapper.convertValue(registration.get("jsonuidata"),
				new TypeReference<Map<String, Object>>() {
				});
		final String tablename = tableName.getSimpleName().toLowerCase();
		// Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> returnObj = new HashMap<String, Object>();
		final StringBuffer query = new StringBuffer();
		String conditionalString = "";
		final List<String> listMultiLingual = new LinkedList<>();
		final List<Class<?>> listClass = new LinkedList<>();
		if (isMaster) {
			conditionalString = " nformcode =" + userInfo.getNformcode() + " and ";
		} else if (tablename.equals("stbstudyplan")) {
			conditionalString = " nregtypecode =" + registration.get("nregtypecode") + " and nregsubtypecode="
					+ registration.get("nregsubtypecode") + " and ";
		}
		String data = "";
		for (Map<String, Object> constraintMap : masterUniqueValidation) {
			final StringBuffer buffer = new StringBuffer();
			Set<String> lstKey = constraintMap.keySet();
			String multiLingualName = "";
			for (String key : lstKey) {

				final Map<String, Object> objMap = objMapper.convertValue(constraintMap.get(key),
						new TypeReference<Map<String, Object>>() {
						});

				final Map<String, Object> mapMultilingual = objMapper.convertValue(objMap.get("1"),
						new TypeReference<Map<String, Object>>() {
						});

//				final Map<String, Object> objMap = (Map<String, Object>) constraintMap.get(key);
//				final Map<String, Object> mapMultilingual = (Map<String, Object>) objMap.get("1");
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

				buffer.append(" and  jsonuidata->> '" + key + "' ILIKE '" + data + "'");
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
			List<?> lstData = projectDAOSupport.getMultipleEntitiesResultSetInList(query.toString(), jdbcTemplate,
					tableName);
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

	private Map<String, Object> insertTestHistoryParameter(final String stestcode, final String sQuery,
			final UserInfo userInfo, final int npreregno, final int nregsamplecode, int ntransactiontestcode,
			int nregistrationparametercode, List<Integer> transactionCodeList, Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final String testGroupQuery = "select tgt.*, s.ssectionname, m.smethodname "
				+ "from testgrouptest tgt  "
				+ "join section s on tgt.nsectioncode=s.nsectioncode "
				+ "join method m on tgt.nmethodcode=m.nmethodcode "
				+ "where  tgt.ntestgrouptestcode in (" + stestcode + ")" + " "
				+ "and tgt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and tgt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and tgt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ "and m.nsitecode="+ userInfo.getNmastersitecode();

		List<StbTimePointTest> testGroupTestList = jdbcTemplate.query(testGroupQuery, new StbTimePointTest());

		testGroupTestList = getTestPrice(testGroupTestList,userInfo);
		String strAdhocQuery = "";
		if ((int) inputMap.get("nsampletypecode") == Enumeration.SampleType.CLINICALSPEC.getType()) {
			strAdhocQuery = " and tgtp.nisadhocparameter in (" + Enumeration.TransactionStatus.NO.gettransactionstatus()
					+ ");";
		} else {
			strAdhocQuery = " and tgtp.nisvisible in (" + Enumeration.TransactionStatus.YES.gettransactionstatus()
					+ ");";
		}

		// ALPD-4168 added tgf.nstatus=1 by rukshana which was not checked previously on
		// May 18 2024

		final String testParameterQuery = "select tgtp.ntestgrouptestcode, tgtp.ntestgrouptestparametercode,tgtp.ntestparametercode,tgtp.nparametertypecode,"
				+ " COALESCE(tgf.ntestgrouptestformulacode,-1) ntestgrouptestformulacode,"
				+ " tgtp.nunitcode,tgtp.nresultmandatory,tgtp.nreportmandatory," + sQuery
				+ " left outer join testgrouptestnumericparameter tgtnp on "
				+ " tgtnp.ntestgrouptestparametercode=tgtp.ntestgrouptestparametercode and tgtnp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " left outer join testgrouptestcharparameter tgtcp on "
				+ " tgtcp.ntestgrouptestparametercode=tgtp.ntestgrouptestparametercode  and tgtcp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " left outer join (select tt.ntestgrouptestparametercode,max(tt.spredefinedname) as spredefinedname ,max(tp.nstatus) as nstatus  "
				+ " from testgrouptestpredefparameter tt,testgrouptestparameter tp "
				+ " where tt.ntestgrouptestparametercode=tp.ntestgrouptestparametercode group by tt.ntestgrouptestparametercode) tgtpp on "
				+ " tgtpp.ntestgrouptestparametercode=tgtp.ntestgrouptestparametercode  and tgtpp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " left outer join testgrouptestformula tgf on tgtnp.ntestgrouptestparametercode=tgf.ntestgrouptestparametercode"
				+ " and tgf.ntransactionstatus = " + Enumeration.TransactionStatus.YES.gettransactionstatus()
				+ " and tgf.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " where tgt.ntestgrouptestcode=tgtp.ntestgrouptestcode and tgtp.nresultaccuracycode=ra.nresultaccuracycode "
				+ "  and tgt.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tgtp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ra.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tgtp.ntestgrouptestcode in (" + stestcode + ")" + " " + strAdhocQuery;
		final List<TestGroupTestParameter> tgtParameterList = jdbcTemplate.query(testParameterQuery,
				new TestGroupTestParameter());

		final ArrayList<String> replicateTestList = new ArrayList<String>();
		final ArrayList<String> replicateTestParameterList = new ArrayList<String>();

		for (StbTimePointTest testGroupTest : testGroupTestList) {
			if (testGroupTest.getNrepeatcountno() > 1) {
				for (int repeatNo = 1; repeatNo <= testGroupTest.getNrepeatcountno(); repeatNo++) {
					int nttestcode = ntransactiontestcode++;
					transactionCodeList.add(nttestcode);
					replicateTestList.add("(" + nttestcode + "," + nregsamplecode + "," + npreregno + ","
							+ testGroupTest.getNtestgrouptestcode() + "," + testGroupTest.getNinstrumentcatcode()
							+ ",-1," + repeatNo + ",0," + " json_build_object('nstbtimepointtestcode', " + nttestcode
							+ ",'nstbstudyplancode'," + npreregno + ",'nstbtimepointcode'," + nregsamplecode
							+ ",'ssectionname','" + stringUtilityFunction.replaceQuote(testGroupTest.getSsectionname())
							+ "','smethodname','" + stringUtilityFunction.replaceQuote(testGroupTest.getSmethodname())
							+ "','ncost'," + testGroupTest.getNcost() + "," + "'stestname','"
							+ stringUtilityFunction.replaceQuote(testGroupTest.getStestsynonym()) + "',"
							+ "'stestsynonym',concat('"
							+ stringUtilityFunction.replaceQuote(testGroupTest.getStestsynonym()) + "','[" + repeatNo
							+ "][0]'))::jsonb," + userInfo.getNtranssitecode() + ",'"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
							+ testGroupTest.getNtestcode() + "," + testGroupTest.getNsectioncode() + ","
							+ testGroupTest.getNmethodcode() + ")");

					for (TestGroupTestParameter testGroupTestParameter : tgtParameterList) {

						if (testGroupTestParameter.getNtestgrouptestcode() == testGroupTest.getNtestgrouptestcode()) {
							int nttestparametercode = nregistrationparametercode++;
							final Map<String, Object> mapObject = testGroupTestParameter.getJsondata();
							mapObject.put("nstbtimepointparametercode", nttestparametercode);
							mapObject.put("nstbtimepointtestcode", nttestcode);
							mapObject.put("stestsynonym", testGroupTest.getStestsynonym() + "[" + repeatNo + "][0]");

							replicateTestParameterList.add("(" + nttestparametercode + "," + npreregno + ","
									+ nttestcode + "," + testGroupTestParameter.getNtestgrouptestparametercode() + ","
									+ testGroupTestParameter.getNtestparametercode() + ","
									+ testGroupTestParameter.getNparametertypecode() + ","
									+ testGroupTestParameter.getNtestgrouptestformulacode() + ","
									+ testGroupTestParameter.getNunitcode() + ","
									+ testGroupTestParameter.getNresultmandatory() + ","
									+ testGroupTestParameter.getNreportmandatory() + "," + "'"
									+ objMapper.writeValueAsString(mapObject) + "'," + userInfo.getNtranssitecode()
									+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")");
						}
					}

				}
			} else {
				// List<Map<String, Object>> testGroupTestList1 = (List<Map<String, Object>>)
				// inputMap .get("testgrouptest");
				List<Map<String, Object>> testGroupTestList1 = objMapper.convertValue(inputMap.get("testgrouptest"),
						new TypeReference<List<Map<String, Object>>>() {
						});

				Integer externalOrderTestCode = testGroupTestList1.stream()
						.filter(x -> (int) x.get("ntestcode") == testGroupTest.getNtestcode()).map(y -> {
							if (y.containsKey("nexternalordertestcode")) {
								return (Integer) y.get("nexternalordertestcode");
							} else {
								return -1;
							}
						}).findFirst().orElse(-1);
				String externalOrderTestStr = "";
				if (externalOrderTestCode != null && externalOrderTestCode != -1) {
					externalOrderTestStr = " 'nexternalordertestcode'," + externalOrderTestCode + ",";
				}

				int nttestcode = ntransactiontestcode++;
				transactionCodeList.add(nttestcode);

				String strQuery = "select ssettingvalue from settings where nsettingcode ="
						+ Enumeration.Settings.UPDATING_ANALYSER.getNsettingcode() + " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";
				Settings objSettings = (Settings) jdbcUtilityFunction.queryForObject(strQuery, Settings.class,
						jdbcTemplate);
				String AnalyserValue = "";

				if (objSettings != null) {
					if (Integer.valueOf(objSettings.getSsettingvalue()) == Enumeration.TransactionStatus.YES
							.gettransactionstatus()) {
						AnalyserValue = ",'AnalyserCode',-1,'AnalyserName','-'";
					}
				}

				replicateTestList.add("(" + nttestcode + "," + nregsamplecode + "," + npreregno + ","
						+ testGroupTest.getNtestgrouptestcode() + "," + testGroupTest.getNinstrumentcatcode()
						+ ",-1,1,0," + " json_build_object('nstbtimepointtestcode', " + nttestcode
						+ ",'nstbstudyplancode'," + npreregno + ",'nstbtimepointcode'," + nregsamplecode
						+ ",'ssectionname','" + stringUtilityFunction.replaceQuote(testGroupTest.getSsectionname())
						+ "','smethodname','" + stringUtilityFunction.replaceQuote(testGroupTest.getSmethodname())
						+ "','ncost'," + testGroupTest.getNcost() + "," + externalOrderTestStr + "'stestname','"
						+ stringUtilityFunction.replaceQuote(testGroupTest.getStestsynonym()) + "',"
						+ "'stestsynonym',concat('"
						+ stringUtilityFunction.replaceQuote(testGroupTest.getStestsynonym()) + "','[1][0]')"
						+ AnalyserValue + ")::jsonb," + userInfo.getNtranssitecode() + ",'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
						+ testGroupTest.getNtestcode() + "," + testGroupTest.getNsectioncode() + ","
						+ testGroupTest.getNmethodcode() + ")");

				for (TestGroupTestParameter testGroupTestParameter : tgtParameterList) {
//				

					if (testGroupTestParameter.getNtestgrouptestcode() == testGroupTest.getNtestgrouptestcode()) {
						int nttestparametercode = nregistrationparametercode++;

						final Map<String, Object> mapObject = testGroupTestParameter.getJsondata();
						mapObject.put("nstbtimepointparametercode", nttestparametercode);
						mapObject.put("nstbtimepointtestcode", nttestcode);
						replicateTestParameterList.add("(" + nttestparametercode + "," + npreregno + "," + nttestcode
								+ "," + testGroupTestParameter.getNtestgrouptestparametercode() + ","
								+ testGroupTestParameter.getNtestparametercode() + ","
								+ testGroupTestParameter.getNparametertypecode() + ","
								+ testGroupTestParameter.getNtestgrouptestformulacode() + ","
								+ testGroupTestParameter.getNunitcode() + ","
								+ testGroupTestParameter.getNresultmandatory() + ","
								+ testGroupTestParameter.getNreportmandatory() + "," + "'"
								+ stringUtilityFunction.replaceQuote(objMapper.writeValueAsString(mapObject)) + "',"
								+ userInfo.getNtranssitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")");
					}
				}
			}
		}
		if (replicateTestList.size() > 0) {
			final String strRegistrationTest = "insert into stbtimepointtest (nstbtimepointtestcode,nstbtimepointcode,nstbstudyplancode,"
					+ "ntestgrouptestcode,ninstrumentcatcode,nchecklistversioncode,ntestrepeatno,ntestretestno,jsondata,nsitecode,dmodifieddate, "
					+ " nstatus,ntestcode,nsectioncode,nmethodcode) values ";
			jdbcTemplate.execute(strRegistrationTest + String.join(",", replicateTestList));

		}

		if (replicateTestParameterList.size() > 0) {
			final String strRegTestParameter = " insert into stbtimepointparameter (nstbtimepointparametercode,"
					+ " nstbstudyplancode,nstbtimepointtestcode,ntestgrouptestparametercode,"
					+ " ntestparametercode,nparametertypecode,ntestgrouptestformulacode,"
					+ " nunitcode, nresultmandatory,nreportmandatory,jsondata,nsitecode,nstatus) values";
			jdbcTemplate.execute(strRegTestParameter + String.join(",", replicateTestParameterList));
		}

		final Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("nstbtimepointtestcode", ntransactiontestcode);
		returnMap.put("nstbtimepointparametercode", nregistrationparametercode);
		returnMap.put("transactionCodeList", transactionCodeList);
		return returnMap;
	}

	private List<StbTimePointTest> getTestPrice(List<StbTimePointTest> testGroupTestList,UserInfo userInfo) throws Exception {
		final String testPriceString = " select tpd.ntestcode, tpd.ncost "
				+ " from testpricedetail tpd "
				+ " join testpriceversion tpv on tpv.npriceversioncode=tpd.npriceversioncode "
				+ " where tpv.ntransactionstatus= "+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+ " "
				+ " and tpv.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and tpd.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and tpv.nsitecode="+userInfo.getNmastersitecode()+" and tpd.nsitecode="+userInfo.getNmastersitecode()+" ";
		final List<TestMaster> masterList = jdbcTemplate.query(testPriceString, new TestMaster());

		for (StbTimePointTest tgt : testGroupTestList) {
			for (TestMaster tm : masterList) {
				if (tgt.getNtestcode() == tm.getNtestcode())
					tgt.setNcost(tm.getNcost().floatValue());
			}
		}
		return testGroupTestList;
	}

	public ResponseEntity<Object> importStabilityStudyPlan(MultipartHttpServletRequest request, UserInfo userInfo)
			throws Exception {
		final MultipartFile multipartFile = request.getFile("stbTimePointImportFile");
		final InputStream ins = multipartFile.getInputStream();
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectMapper objMapper = new ObjectMapper();
		// Map<String, Object> objMap1 =
		// objMapper.readValue(request.getParameter("Map"), Map.class);
		Map<String, Object> objMap1 = objMapper.readValue(request.getParameter("Map"),
				new TypeReference<Map<String, Object>>() {
				});

		final StbStudyPlan registration = objMapper.convertValue(objMap1.get("StabilityStudyPlan"),
				new TypeReference<StbStudyPlan>() {
				});

		final List<StbStudyPlan> lstRegistration = new ArrayList<StbStudyPlan>();
		final List<StbTimePoint> lstRegistrationSample = new ArrayList<StbTimePoint>();

		List<String> lstSampHeaderString = objMapper.convertValue(objMap1.get("SampleFieldsString"),
				new TypeReference<List<String>>() {
				});

		List<String> lstExportList = objMapper.convertValue(objMap1.get("exportFields"),
				new TypeReference<List<String>>() {
				});
//		List<String> sampleDateList = objMapper.convertValue(objMap1.get("DateList"),
//				new TypeReference<List<String>>() {
//				});
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

		List<Map<String, Object>> lstTest = new ArrayList<Map<String, Object>>();

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

				StbStudyPlan rs = SerializationUtils.clone(registration);
				StbTimePoint rsa = new StbTimePoint();
				int cellIndex = 0;

				for (String field : lstHeader) // iteration over cell using for each loop
				{
					System.out.println("cell count" + cellIndex + " " + field);
					LOGGER.info(row.getCell(cellIndex).toString());
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
					// int lr;
					if (sheet.getNumMergedRegions() > 0) {

						String key = "RS" + String.valueOf(rowIndex) + "RE" + String.valueOf(rowIndex) + "CS"
								+ String.valueOf(cellIndex) + "CE" + String.valueOf(cellIndex);

						String value = objMap.get(key);

						if (objMap.containsKey(key)) {

							fr = Integer.parseInt(value.substring(2, value.indexOf("RE")));
							// lr = Integer.parseInt(value.substring(value.indexOf("RE") + 2,
							// value.indexOf("CS")));

							if (fr == rowIndex) {
								rowMerge = 1;
								responseMap = (Map<String, Object>) transactionDAOSupport
										.importValidData(objExpFldProps, cell, field, userInfo);

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
							responseMap = (Map<String, Object>) transactionDAOSupport.importValidData(objExpFldProps,
									cell, field, userInfo);

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
						responseMap = (Map<String, Object>) transactionDAOSupport.importValidData(objExpFldProps, cell,
								field, userInfo);

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
					rsa.setNstbstudyplancode(sublno);
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
									+ "		and tgtp.nstatus ="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " ) as nparametercount"
									+ " from testgrouptest tg,testgroupspecsampletype ts where"
									+ " ts.nspecsampletypecode=tg.nspecsampletypecode and ts.nspecsampletypecode="
									+ nspecsampletypecode + " and ts.nallottedspeccode=" + nallottedspeccode + " "
									+ sQuery + " and tg.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
									+ " and ts.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

							List<Map<String, Object>> lst = jdbcTemplate.queryForList(s);

							lstTest.addAll(lst);
						}

					} else {
						String componentname = "";

						componentname = objJsonUiDatasub.get("ncomponentcode").toString();

						String componnet = " select * from testgroupspecsampletype ts,component c where "
								+ " nallottedspeccode=" + nallottedspeccode
								+ " and ts.ncomponentcode=c.ncomponentcode and c.scomponentname='" + componentname + "'"
								+ " and ts.nstatus=1 and c.nstatus=1 ";

						TestGroupSpecSampleType tg = (TestGroupSpecSampleType) jdbcUtilityFunction
								.queryForObject(componnet, TestGroupSpecSampleType.class, jdbcTemplate);

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
								String s = " select *," + sublno
										+ " slno,case when nrepeatcountno = 0 then 1 else nrepeatcountno end nrepeatcountno,"
										+ " (  select count(tgtp.ntestgrouptestparametercode) "
										+ "    from testgrouptestparameter tgtp "
										+ "    where  tgtp.ntestgrouptestcode=ntestgrouptestcode "
										+ "		and tgtp.nstatus ="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " ) as nparametercount" + " from testgrouptest where"
										+ " nspecsampletypecode=" + tg.getNspecsampletypecode() + " and nstatus=1";

								List<Map<String, Object>> lst = jdbcTemplate.queryForList(s);

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
					rsa.setNstbstudyplancode(sublno);
					rsa.setSlno(sublno);
					lstRegistrationSample.add(rsa);
					// ALPD-3596
					if (needExecute || (int) objMap1.get("importTest") == Enumeration.TransactionStatus.YES
							.gettransactionstatus()) {
						String s = " select tg.*," + sublno
								+ " slno,case when tg.nrepeatcountno = 0 then 1 else tg.nrepeatcountno end nrepeatcountno,"
								+ " (  select count(tgtp.ntestgrouptestparametercode) "
								+ "    from testgrouptestparameter tgtp "
								+ "    where  tgtp.ntestgrouptestcode=tg.ntestgrouptestcode "
								+ "		and tgtp.nstatus ="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ) as nparametercount"
								+ " from testgrouptest tg,testgroupspecsampletype ts where"
								+ " ts.nspecsampletypecode=tg.nspecsampletypecode and ts.nallottedspeccode="
								+ nallottedspeccode + " " + sQuery + " and tg.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and ts.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

						List<Map<String, Object>> lst = jdbcTemplate.queryForList(s);

						lstTest.addAll(lst);
					}

				}

			} else {

				// int cellIndex = 0;
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
							// cellIndex++;
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

		objMap1.put("StabilityStudyPlan", lstRegistration);
		objMap1.put("StbTimePoint", lstRegistrationSample);
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

		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		objectMapper.registerModule(new JavaTimeModule());
		final List<TestGroupTest> listTest = objectMapper.convertValue(inputMap.get("testgrouptest"),
				new TypeReference<List<TestGroupTest>>() {
				});

		Boolean skipMethodValidity = null;
		if (inputMap.containsKey("skipmethodvalidity")) {
			skipMethodValidity = (Boolean) inputMap.get("skipmethodvalidity");
		}

		final String sntestgrouptestcode = stringUtilityFunction.fnDynamicListToString(listTest,
				"getNtestgrouptestcode");

		List<TestGroupTest> expiredMethodTestList = new ArrayList<TestGroupTest>();
		if (skipMethodValidity == false) {
			expiredMethodTestList = getTestByExpiredMethod(sntestgrouptestcode, userInfo);

		}
		if (expiredMethodTestList.isEmpty()) {
//			String sQuery = " lock  table lockpreregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//			getJdbcTemplate().execute(sQuery);
// 
//			sQuery = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//			getJdbcTemplate().execute(sQuery);
// 
//			sQuery = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//			getJdbcTemplate().execute(sQuery);
// 
//			sQuery = " lock  table lockcancelreject " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//			getJdbcTemplate().execute(sQuery);

			List<StbTimePoint> subSampleInputList = objectMapper.convertValue(inputMap.get("StbTimePoint"),
					new TypeReference<List<StbTimePoint>>() {
					});

			final List<StbStudyPlan> registration = objectMapper.convertValue(inputMap.get("StabilityStudyPlan"),
					new TypeReference<List<StbStudyPlan>>() {
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

			List<Map<String, Object>> samplecombinationunique = (List<Map<String, Object>>) inputMap
					.get("samplecombinationunique");

			final List<Map<String, Object>> subsamplecombinationunique = (List<Map<String, Object>>) inputMap
					.get("subsamplecombinationunique");

			for (StbStudyPlan reg : registration) {

				Map<String, Object> map1 = objectMapper.convertValue(reg, new TypeReference<Map<String, Object>>() {
				});

				Map<String, Object> map = validateUniqueConstraint(samplecombinationunique, map1, userInfo, "create",
						StbStudyPlan.class, "nstbstudyplancode", false);
				if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
						.equals(map.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
					// for normal preregister flow if we sent 1 means,order sample already
					// preregitered so current sample also sample same order so store as sub sample
					// in that specific order (only for clinic type)
					map.put("nflag", 2);
					return map;
				}
			}

			for (StbTimePoint reg : subSampleInputList) {

				Map<String, Object> map1 = objectMapper.convertValue(reg, new TypeReference<Map<String, Object>>() {
				});

				Map<String, Object> map = validateUniqueConstraint(subsamplecombinationunique, map1, userInfo, "create",
						StbTimePoint.class, "nstbstudyplancode", false);
				if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
						.equals(map.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
					// for normal preregister flow if we sent 1 means,order sample already
					// preregitered so current sample also sample same order so store as sub sample
					// in that specific order (only for clinic type)
					map.put("nflag", 2);
					return map;
				}
			}

//			JSONObject objJson = new JSONObject(registration.get(0).getJsondata());
//
//			String manualOrderInsert = "";
//			int nsampletypecode = (int) inputMap.get("nsampletypecode");
//
//			StbTimePoint externalorderList = null;

			final String strSelectSeqno = "select stablename,nsequenceno from seqnostability  where stablename "
					+ "in ('stbstudyplan','stbtimepointparameter','stbtimepoint','stbtimepointtest') and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";

			final List<?> lstMultiSeqNo = projectDAOSupport.getMultipleEntitiesResultSetInList(strSelectSeqno,
					jdbcTemplate, SeqNoStability.class);

			final List<SeqNoStability> lstSeqNoReg = (List<SeqNoStability>) lstMultiSeqNo.get(0);

			returnMap = lstSeqNoReg.stream().collect(Collectors.toMap(SeqNoStability::getStablename,
					SeqNoRegistration -> SeqNoRegistration.getNsequenceno()));

			String strSeqnoUpdate = "Update seqnostability set nsequenceno = "
					+ ((int) returnMap.get("stbstudyplan") + registration.size())
					+ " where stablename = 'stbstudyplan';" + "Update seqnostability set nsequenceno = "
					+ ((int) returnMap.get("stbtimepointparameter") + parameterCount)
					+ " where stablename = 'stbtimepointparameter';" + "Update seqnostability set nsequenceno = "
					+ ((int) returnMap.get("stbtimepoint") + subSampleInputList.size())
					+ " where stablename = 'stbtimepoint';" + "Update seqnostability set nsequenceno = "
					+ ((int) returnMap.get("stbtimepointtest")// + TestGroupTests.size()
							+ testCount)
					+ " where stablename = 'stbtimepointtest' ;";

			jdbcTemplate.execute(strSeqnoUpdate);

			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.SUCCESS.getreturnstatus());

		} else {
			final String expiredMethod = stringUtilityFunction.fnDynamicListToString(expiredMethodTestList,
					"getSmethodname");
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

		final ObjectMapper objmap = new ObjectMapper();
		objmap.registerModule(new JavaTimeModule());

		final UserInfo userInfo = objmap.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		final List<StbStudyPlan> reg = objmap.convertValue(inputMap.get("StabilityStudyPlan"),
				new TypeReference<List<StbStudyPlan>>() {
				});

		// registration.setNisiqcmaterial((short)
		// Enumeration.TransactionStatus.NO.gettransactionstatus());

		final List<StbTimePoint> registrationSample1 = objmap.convertValue(inputMap.get("StbTimePoint"),
				new TypeReference<List<StbTimePoint>>() {
				});

		objmap.registerModule(new JavaTimeModule());
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
		int npreregno = (int) inputMap.get("stbstudyplan");
		int nregistrationparametercode = (int) inputMap.get("stbtimepointparameter");
		int nregsamplecode = (int) inputMap.get("stbtimepoint");
		int napproveconfversioncode = (int) inputMap.get("napproveconfversioncode");
		int ntransactiontestcode = (int) inputMap.get("stbtimepointtest");

		StringJoiner joinersubSample = new StringJoiner(",");
		StringJoiner joinerSample = new StringJoiner(",");
		++ntransactiontestcode;
		++nregistrationparametercode;

		int nage = 0;
		int ngendercode = 0;
		String sQuery = "";
		if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()) {
			nage = (int) inputMap.get("AgeData");
			ngendercode = (int) inputMap.get("ngendercode");

			String sYears = "SELECT CURRENT_DATE - TO_DATE('" + inputMap.get("sDob") + "','"
					+ userInfo.getSpgsitedatetime() + "' )";
			long ageInDays = (long) jdbcUtilityFunction.queryForObject(sYears, Long.class, jdbcTemplate);

			sQuery = "json_build_object('ntransactionresultcode'," + nregistrationparametercode
					+ "+RANK() over(order by tgtp.ntestgrouptestparametercode),'ntransactiontestcode',"
					+ ntransactiontestcode + "+DENSE_RANK() over(order by tgtp.ntestgrouptestcode),'npreregno',"
					+ npreregno + ")::jsonb || " + " case when tgtp.nparametertypecode="
					+ Enumeration.ParameterType.NUMERIC.getparametertype() + " then case when  " + " tgtcs.ngendercode="
					+ ngendercode + " and " + ageInDays
					// + " between tgtcs.nfromage and tgtcs.ntoage"
					+ " between case when tgtcs.nfromageperiod=" + Enumeration.Period.Years.getPeriod() + ""
					+ " then (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(years => tgtcs.nfromage)))::int)"
					+ " when tgtcs.nfromageperiod=" + Enumeration.Period.Month.getPeriod() + " then "
					+ " (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(months => tgtcs.nfromage)))::int) "
					+ " else (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(days => tgtcs.nfromage)))::int) end "
					+ " and case when tgtcs.ntoageperiod=" + Enumeration.Period.Years.getPeriod()
					+ " then (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(years => tgtcs.ntoage)))::int) "
					+ " when tgtcs.ntoageperiod=" + Enumeration.Period.Month.getPeriod()
					+ " then (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(months => tgtcs.ntoage)))::int) "
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
					+ "nstatus from testgrouptest tgt inner join "
					+ " testgrouptestparameter tgtp  on tgt.ntestgrouptestcode = tgtp.ntestgrouptestcode "
					+ " left outer join testgrouptestclinicalspec tgtcs on tgtcs.ntestgrouptestparametercode = tgtp.ntestgrouptestparametercode "
					+ " and tgtcs.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and tgtcs.ngendercode=" + ngendercode + " and " + ageInDays
					+ " between case when tgtcs.nfromageperiod=" + Enumeration.Period.Years.getPeriod() + " "
					+ " then (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(years => tgtcs.nfromage)))::int)"
					+ " when tgtcs.nfromageperiod=" + Enumeration.Period.Month.getPeriod() + " then "
					+ " (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(months => tgtcs.nfromage)))::int) "
					+ " else (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(days => tgtcs.nfromage)))::int) end "
					+ " and case when tgtcs.ntoageperiod=" + Enumeration.Period.Years.getPeriod()
					+ " then (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(years => tgtcs.ntoage)))::int) "
					+ " when tgtcs.ntoageperiod=" + Enumeration.Period.Month.getPeriod()
					+ " then (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(months => tgtcs.ntoage)))::int) "
					+ " else (SELECT EXTRACT(DAY FROM CURRENT_DATE - (CURRENT_DATE - make_interval(days => tgtcs.ntoage)))::int) end ";
			// + " between tgtcs.nfromage and tgtcs.ntoage ";

			returnMap.put("nAge", nage);
			returnMap.put("nGendercode", ngendercode);
		} else {
			sQuery = "json_build_object('nstbtimepointparametercode'," + nregistrationparametercode
					+ "+RANK() over(order by tgtp.ntestgrouptestparametercode),'nstbtimepointtestcode',"
					+ ntransactiontestcode + "+DENSE_RANK() over(order by tgtp.ntestgrouptestcode),'nstbstudyplancode',"
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
		String strRegistrationSample = "";

		for (StbStudyPlan registration : reg) {

			npreregno++;

			joinerSample.add(String.valueOf(npreregno));
			JSONObject jsoneditRegistration = new JSONObject(registration.getJsondata());
			JSONObject jsonuiRegistration = new JSONObject(registration.getJsonuidata());

			if (!dateList.isEmpty()) {

				jsoneditRegistration = (JSONObject) dateUtilityFunction
						.convertJSONInputDateToUTCByZone(jsoneditRegistration, dateList, false, userInfo);
				jsonuiRegistration = (JSONObject) dateUtilityFunction
						.convertJSONInputDateToUTCByZone(jsonuiRegistration, dateList, false, userInfo);
				final Map<String, Object> obj = commonFunction.validateDynamicDateContraints(jsonuiRegistration,
						sampleDateConstraint, userInfo);
				if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
						.equals((String) obj.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
					return obj;
				}
			}

			jsonuiRegistration.put("nstbstudyplancode", npreregno);
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

			strRegistrationInsert = strRegistrationInsert + "(" + npreregno + "," + registration.getNprotocolcode()
					+ "," + nsampletypecode + "," + nregtypecode + "," + nregsubtypecode + ","
					+ registration.getNproductcatcode() + "," + registration.getNproductcode() + ","
					+ registration.getNinstrumentcatcode() + "," + registration.getNinstrumentcode() + ","
					+ registration.getNmaterialcatcode() + "," + registration.getNmaterialcode() + ","
					+ registration.getNtemplatemanipulationcode() + "," + registration.getNallottedspeccode() + ","
					+ userInfo.getNtranssitecode() + ",'"
					+ stringUtilityFunction.replaceQuote(jsoneditRegistration.toString()) + "'::JSONB,'"
					+ stringUtilityFunction.replaceQuote(jsonuiRegistration.toString()) + "'::JSONB,"
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
					+ registration.getNdesigntemplatemappingcode() + "," + registration.getNregsubtypeversioncode()
					+ "," + registration.getNprojectmastercode() + "," + registration.getNisiqcmaterial() + ","
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ",'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + napproveconfversioncode + "),";

			List<StbTimePoint> lstRegistrationSample = registrationSample1.stream()
					.filter(x -> x.getNstbstudyplancode() == registration.getSlno()).collect(Collectors.toList());

			for (StbTimePoint registrationSample : lstRegistrationSample) {
				nregsamplecode++;
				joinersubSample.add(String.valueOf(nregsamplecode));

				JSONObject jsoneditObj = new JSONObject(registrationSample.getJsondata());
				JSONObject jsonuiObj = new JSONObject(registrationSample.getJsonuidata());
				if (!SubSampledateList.isEmpty()) {
					jsoneditObj = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsoneditObj,
							SubSampledateList, false, userInfo);
					jsonuiObj = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonuiObj,
							SubSampledateList, false, userInfo);
					final Map<String, Object> obj = commonFunction.validateDynamicDateContraints(jsonuiObj,
							subSampleDateConstraint, userInfo);
					if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
							.equals((String) obj.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
						return obj;
					}
				}

				jsonuiObj.put("nstbtimepointcode", nregsamplecode);
				jsonuiObj.put("nstbstudyplancode", npreregno);
				jsonuiObj.put("nspecsampletypecode", registrationSample.getNspecsampletypecode());
				jsonuiObj.put("ncomponentcode", registrationSample.getNcomponentcode());

				strRegistrationSample = strRegistrationSample + " (" + nregsamplecode + "," + npreregno + ",'"
						+ registrationSample.getNspecsampletypecode() + "'," + registrationSample.getNcomponentcode()
						+ ",'" + stringUtilityFunction.replaceQuote(jsoneditObj.toString()) + "'::JSONB,'"
						+ stringUtilityFunction.replaceQuote(jsonuiObj.toString()) + "'::JSONB,"
						+ userInfo.getNtranssitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ,"
						+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ",'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'),";

				List<StbTimePoint> comp = new ArrayList<>();
				comp.add(registrationSample);
				List<TestGroupTest> lsttest1 = tgtTestInputList.stream()
						.filter(x -> x.getSlno() == comp.get(0).getSlno()).collect(Collectors.toList());

				final String stestcode = lsttest1.stream().map(x -> String.valueOf(x.getNtestgrouptestcode()))
						.distinct().collect(Collectors.joining(","));

				if (!lsttest1.isEmpty()) {

					// FRS-00410- To add test based on replicate count defined in testgroup test
					final Map<String, Object> testHistoryParameterMap = insertTestHistoryParameter(stestcode, sQuery,
							userInfo, npreregno, nregsamplecode, ntransactiontestcode, nregistrationparametercode,
							new ArrayList<Integer>(), inputMap);
					ntransactiontestcode = (int) testHistoryParameterMap.get("nstbtimepointtestcode");
					nregistrationparametercode = (int) testHistoryParameterMap.get("nstbtimepointparametercode");

				}

			}

		}

		strRegistrationInsert = "Insert into stbstudyplan (nstbstudyplancode, nprotocolcode, nsampletypecode, nregtypecode,nregsubtypecode,nproductcatcode,nproductcode,ninstrumentcatcode,"
				+ "ninstrumentcode, nmaterialcatcode, nmaterialcode,ntemplatemanipulationcode,nallottedspeccode,nsitecode,jsondata,jsonuidata,nstatus,"
				+ "ndesigntemplatemappingcode,nregsubtypeversioncode,nprojectmastercode,nisiqcmaterial,ntransactionstatus,dtransactiondate,napprovalversioncode) values "
				+ strRegistrationInsert.substring(0, strRegistrationInsert.length() - 1) + ";";
		jdbcTemplate.execute(strRegistrationInsert);

		strRegistrationSample = "insert into stbtimepoint(nstbtimepointcode, nstbstudyplancode, nspecsampletypecode, ncomponentcode,jsondata, "
				+ " jsonuidata,nsitecode,nstatus,ntransactionstatus,dtransactiondate) values "
				+ strRegistrationSample.substring(0, strRegistrationSample.length() - 1) + " ;";
		jdbcTemplate.execute(strRegistrationSample);

		inputMap.put("nstbstudyplancode", joinerSample.toString());

		returnMap = getDynamicRegistration(inputMap, userInfo);
		returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
				Enumeration.ReturnStatus.SUCCESS.getreturnstatus());

		jsonAuditObject.put("stbstudyplan", (List<Map<String, Object>>) returnMap.get("StabilityStudyPlanGet"));
		actionType.put("stbstudyplan", "IDS_ADDSTABILITYSTUDYPLAN");

		if ((boolean) inputMap.get("nneedsubsample")) {
			jsonAuditObject.put("stbtimepoint", (List<Map<String, Object>>) returnMap.get("StbTimePointGet"));
			actionType.put("stbtimepoint", "IDS_ADDTIMEPOINT");

			inputMap.put("nstbtimepointcode", null);

			// Map<String, Object> objMap = (Map<String, Object>)
			// getRegistrationTestAudit(inputMap, userInfo).getBody();

			// jsonAuditObject.put("stbtimepointtest", (List<Map<String, Object>>)
			// objMap.get("RegistrationGetTest"));
		} else {
			jsonAuditObject.put("stbtimepointtest", (List<Map<String, Object>>) returnMap.get("StbTimePointTestGet"));
		}

		auditmap.put("nregtypecode", nregtypecode);
		auditmap.put("nregsubtypecode", nregsubtypecode);
		auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));

		actionType.put("stbtimepointtest", "IDS_ADDTIMEPOINTEST");
		// fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false,
		// userInfo);

		return returnMap;

	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getCreateTestSequenceNo(final UserInfo userInfo, final List<String> listSample,
			final List<TestGroupTest> listTest, final int nregtypecode, final int nregsubtypecode,
			Map<String, Object> inputMap) throws Exception {

		Map<String, Object> outputMap = new HashMap<String, Object>();
		final String sntestgrouptestcode = stringUtilityFunction.fnDynamicListToString(listTest,
				"getNtestgrouptestcode");

		// final Boolean skipMethodValidity = (Boolean)
		// inputMap.get("skipmethodvalidity");

		final String inactiveTestQuery = " select tgt.stestsynonym, tm.ntestcode from testgrouptest tgt "
				+ " join testmaster tm on tgt.ntestcode=tm.ntestcode where tgt.ntestgrouptestcode in (" + sntestgrouptestcode + ") "
				+ " and tgt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" " + " "
				+ " and tm.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +  " and tgt.nsitecode="+userInfo.getNmastersitecode()+" "
				+ " and tm.ntransactionstatus= "+ Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus();

		final List<TestGroupTest> inactiveTestList = jdbcTemplate.query(inactiveTestQuery, new TestGroupTest());
		if (inactiveTestList.isEmpty()) {

			final String sSeqNoQuery = "select * from seqnostability "
					+ " where stablename in ('stbtimepointtest', 'stbtimepointparameter') and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
			

			List<SeqNoStability> mapList = (List<SeqNoStability>) projectDAOSupport.getMultipleEntitiesResultSetInList(sSeqNoQuery,jdbcTemplate, SeqNoStability.class);
			
			
			final List<SeqNoStability> lstSeqNo = (List<SeqNoStability>) mapList.get(0);

			outputMap.putAll(lstSeqNo.stream().collect(Collectors.toMap(SeqNoStability::getStablename,
					SeqNoRegistration -> SeqNoRegistration.getNsequenceno())));

			int testCount = listTest.stream().mapToInt(
					testgrouptest -> testgrouptest.getNrepeatcountno() == 0 ? 1 : testgrouptest.getNrepeatcountno())
					.sum();

			int parameterCount = listTest.stream().mapToInt(
					testgrouptest -> ((testgrouptest.getNrepeatcountno() == 0 ? 1 : testgrouptest.getNrepeatcountno())
							* testgrouptest.getNparametercount()))
					.sum();

			String sUpdateSeqNoQry = "";

			sUpdateSeqNoQry = "update seqnostability" + " set nsequenceno = "
					+ ((int) outputMap.get("stbtimepointparameter") + (parameterCount))
					+ " where stablename = 'stbtimepointparameter';";
			jdbcTemplate.execute(sUpdateSeqNoQry);

			sUpdateSeqNoQry = "update seqnostability set nsequenceno = "
					+ ((int) outputMap.get("stbtimepointtest") + (testCount))
					+ " where stablename = 'stbtimepointtest';";
			jdbcTemplate.execute(sUpdateSeqNoQry);
//			final String sFindSubSampleQuery = "select npreregno, ntransactionstatus, ntransactionsamplecode from registrationsamplehistory"
//					+ " where nsamplehistorycode = any (select max(nsamplehistorycode) from registrationsamplehistory where ntransactionsamplecode"
//					+ " in (" + String.join(",", listSample) + ")  and nsitecode=" + userInfo.getNtranssitecode()
//					+ " group by ntransactionsamplecode)" + " and ntransactionstatus not in ("
//					+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ", "
//					+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ")" + "  and nsitecode="
//					+ userInfo.getNtranssitecode() + ";";

			final String sFindSubSampleQuery = "select nstbstudyplancode, ntransactionstatus, nstbtimepointcode from stbtimepoint "
					+ " where nstbtimepointcode in (" + String.join(",", listSample)
					+ ") and  nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  and nsitecode=" + userInfo.getNtranssitecode() + ";";

			List<StbTimePoint> listAvailableSample = jdbcTemplate.query(sFindSubSampleQuery, new StbTimePoint());

			outputMap.put("AvailableSample", listAvailableSample);
			outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		} else {
			outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), // inactiveTest);
					commonFunction.getMultilingualMessage("IDS_INACTIVETESTCANNOTBEREGISTERED",
							userInfo.getSlanguagefilename()));
		}
		return outputMap;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> validateSeqnoSubSampleNo(Map<String, Object> inputMap) throws Exception {

		Map<String, Object> returnMap = new HashMap<String, Object>();
		final ObjectMapper objmap = new ObjectMapper();

		final UserInfo userInfo = objmap.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final List<TestGroupTest> listTest = objmap.convertValue(inputMap.get("testgrouptest"),
				new TypeReference<List<TestGroupTest>>() {
				});

		Boolean skipMethodValidity = null;
		if (inputMap.containsKey("skipmethodvalidity")) {
			skipMethodValidity = (Boolean) inputMap.get("skipmethodvalidity");
		}

		final String sntestgrouptestcode = stringUtilityFunction.fnDynamicListToString(listTest,
				"getNtestgrouptestcode");

		List<TestGroupTest> expiredMethodTestList = new ArrayList<>();
		if (skipMethodValidity == false && !listTest.isEmpty()) {
			expiredMethodTestList = getTestByExpiredMethod(sntestgrouptestcode, userInfo);

		}
		if (expiredMethodTestList.isEmpty()) {
//			String sQuery = " lock  table lockpreregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//			getJdbcTemplate().execute(sQuery);
//
//			sQuery = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//			getJdbcTemplate().execute(sQuery);
//
//			sQuery = " lock  table lockcancelreject " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//			getJdbcTemplate().execute(sQuery);

			String npreregno = (String) inputMap.get("nstbstudyplancode");

			List<Map<String, Object>> subsamplecombinationunique = (List<Map<String, Object>>) inputMap
					.get("subsamplecombinationunique");

			Map<String, Object> map1 = validateUniqueConstraint(subsamplecombinationunique,
					(Map<String, Object>) inputMap.get("StbTimePoint"), userInfo, "create", StbTimePoint.class,
					"nstbstudyplancode", false);
			if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
					.equals(map1.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
				return map1;
			}

			/**
			 * to get current status of selected samples
			 */
			final String squery = "select nstbstudyplancode, ntransactionstatus from stbstudyplan where"
					+ " nstbstudyplancode in (" + npreregno + ") " + " and nsitecode=" + userInfo.getNtranssitecode()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			List<StbStudyPlan> lstStatus = jdbcTemplate.query(squery, new StbStudyPlan());

			final List<StbStudyPlan> uncancelledUnRejectedList = lstStatus.stream().filter(
					x -> x.getNtransactionstatus() != Enumeration.TransactionStatus.CANCELED.gettransactionstatus()
							&& x.getNtransactionstatus() != Enumeration.TransactionStatus.APPROVED
									.gettransactionstatus()
							&& x.getNtransactionstatus() != Enumeration.TransactionStatus.RELEASED
									.gettransactionstatus())
					.collect(Collectors.toList());

			if (uncancelledUnRejectedList.isEmpty()) {
				returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						"IDS_SAMPLECANCELLEDREJECTEDRELEASED");
			} else {
				npreregno = stringUtilityFunction.fnDynamicListToString(uncancelledUnRejectedList,
						"getNstbstudyplancode");

				List<TestGroupTest> TestGroupTests = objmap.convertValue(inputMap.get("testgrouptest"),
						new TypeReference<List<TestGroupTest>>() {
						});

				int testCount = TestGroupTests.stream().mapToInt(
						testgrouptest -> testgrouptest.getNrepeatcountno() == 0 ? 1 : testgrouptest.getNrepeatcountno())
						.sum();

				int paramterCount = TestGroupTests.stream()
						.mapToInt(testgrouptest -> ((testgrouptest.getNrepeatcountno() == 0 ? 1
								: testgrouptest.getNrepeatcountno()) * testgrouptest.getNparametercount()))
						.sum();

				// String registeredSampleGetSeq = "";

				final String strSelectSeqno = "select stablename,nsequenceno from seqnostability  where stablename "
						+ "in ('stbtimepointparameter','stbtimepoint', 'stbtimepointtest') and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";

				final List<?> lstMultiSeqNo = projectDAOSupport.getMultipleEntitiesResultSetInList(strSelectSeqno,
						jdbcTemplate, SeqNoStability.class);

				final List<SeqNoStability> lstSeqNoReg = (List<SeqNoStability>) lstMultiSeqNo.get(0);

				returnMap = lstSeqNoReg.stream().collect(Collectors.toMap(SeqNoStability::getStablename,
						SeqNoRegistration -> SeqNoRegistration.getNsequenceno()));

				final List<String> listPreregNo = new ArrayList<String>(Arrays.asList(npreregno.split(",")));

				String strSeqnoUpdate = "Update seqnostability set nsequenceno = "
						+ ((int) returnMap.get("stbtimepointparameter") + (paramterCount * listPreregNo.size()))
						+ " where stablename = 'stbtimepointparameter';"

						+ "Update seqnostability set nsequenceno = "
						+ ((int) returnMap.get("stbtimepoint") + listPreregNo.size())
						+ " where stablename = 'stbtimepoint';"

						+ "Update seqnostability set nsequenceno = "
						+ ((int) returnMap.get("stbtimepointtest") + (testCount * listPreregNo.size()))
						+ " where stablename = 'stbtimepointtest' ;";

				jdbcTemplate.execute(strSeqnoUpdate);
				returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
			}
		} else {
			final String expiredMethod = stringUtilityFunction.fnDynamicListToString(expiredMethodTestList,
					"getSmethodname");
			returnMap.put("NeedConfirmAlert", true);
			final String message = commonFunction.getMultilingualMessage("IDS_TESTWITHEXPIREDMETHODCONFIRM",
					userInfo.getSlanguagefilename());
			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), message.concat(" " + expiredMethod));
		}
		return returnMap;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> createTimePoint(Map<String, Object> inputMap) throws Exception {

//		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
//		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObject = new JSONObject();

		ObjectMapper objmap = new ObjectMapper();
		objmap.registerModule(new JavaTimeModule());

		final UserInfo userInfo = objmap.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		String npreregno = (String) inputMap.get("nstbstudyplancode");

		final String squery = "select nstbstudyplancode, ntransactionstatus from stbstudyplan where"
				+ " nstbstudyplancode in (" + npreregno + ") " + " and nsitecode=" + userInfo.getNtranssitecode()
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
		List<StbStudyPlan> lstStatus = jdbcTemplate.query(squery, new StbStudyPlan());

		final List<StbStudyPlan> uncancelledUnRejectedList = lstStatus.stream()
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
			npreregno = stringUtilityFunction.fnDynamicListToString(uncancelledUnRejectedList, "getNstbstudyplancode");

			List<String> lstPreregno1 = new ArrayList<String>(Arrays.asList(npreregno.split(",")));
			List<Integer> lstPreregno = lstPreregno1.stream().map(x -> Integer.parseInt(x))
					.collect(Collectors.toList());

			final StbTimePoint registrationSample = objmap.convertValue(inputMap.get("StbTimePoint"),
					new TypeReference<StbTimePoint>() {
					});

			if (inputMap.containsKey("specBasedComponent")) {
				if ((boolean) inputMap.get("specBasedComponent") == false) {
					final String query = "select nspecsampletypecode from testgroupspecsampletype where nallottedspeccode="
							+ registrationSample.getNallottedspeccode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";

					Integer nspecsampletypecode = jdbcTemplate.queryForObject(query, Integer.class);
					registrationSample.setNspecsampletypecode(nspecsampletypecode);
				}
			}

			List<TestGroupTest> testGroupTestList = objmap.convertValue(inputMap.get("testgrouptest"),
					new TypeReference<List<TestGroupTest>>() {
					});

			List<String> SubSampledateList = objmap.convertValue(inputMap.get("subsampleDateList"),
					new TypeReference<List<String>>() {
					});

			int nregsamplecode = (int) inputMap.get("stbtimepoint");

			inputMap.put("nsitecode", userInfo.getNtranssitecode());

			String strRegistrationSample = "";
			String ntransactionsamplecode = "";

			int sampleCount = lstPreregno.size();

			for (int i = 0; i < sampleCount; i++) {
				nregsamplecode++;

				JSONObject jsoneditObj = new JSONObject(registrationSample.getJsondata());
				JSONObject jsonuiObj = new JSONObject(registrationSample.getJsonuidata());
				if (!SubSampledateList.isEmpty()) {
					jsoneditObj = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsoneditObj,
							SubSampledateList, false, userInfo);
					jsonuiObj = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonuiObj,
							SubSampledateList, false, userInfo);

				}

				jsonuiObj.put("nstbtimepointcode", nregsamplecode);
				jsonuiObj.put("nstbstudyplancode", lstPreregno.get(i));
				jsonuiObj.put("nspecsampletypecode", registrationSample.getNspecsampletypecode());
				jsonuiObj.put("ncomponentcode", registrationSample.getNcomponentcode());

				strRegistrationSample = strRegistrationSample + " (" + nregsamplecode + "," + lstPreregno.get(i) + ",'"
						+ registrationSample.getNspecsampletypecode() + "'," + registrationSample.getNcomponentcode()
						+ ",'" + stringUtilityFunction.replaceQuote(jsoneditObj.toString()) + "'::JSONB,'"
						+ stringUtilityFunction.replaceQuote(jsonuiObj.toString()) + "'::JSONB,"
						+ userInfo.getNtranssitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ,"
						+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ",'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'),";

//				final int mainSampleNo = lstPreregno.get(i);
//				int mainSampleStatus = uncancelledUnRejectedList.stream()
//						.filter(item -> item.getNprotocolcode() == mainSampleNo).collect(Collectors.toList()).get(0)
//						.getNtransactionstatus();
//
//				int subSampleStatus = mainSampleStatus;

				StringJoiner joinerSampleMain = new StringJoiner(",");
				joinerSampleMain.add(String.valueOf(nregsamplecode));

				ntransactionsamplecode = ntransactionsamplecode + "," + String.valueOf(nregsamplecode);
			}
			strRegistrationSample = "insert into stbtimepoint(nstbtimepointcode, nstbstudyplancode, nspecsampletypecode, ncomponentcode,jsondata, "
					+ " jsonuidata,nsitecode,nstatus,ntransactionstatus,dtransactiondate) values "
					+ strRegistrationSample.substring(0, strRegistrationSample.length() - 1) + " ;";
			jdbcTemplate.execute(strRegistrationSample);

			final int nregTypeCode = (int) inputMap.get("nregtypecode");
			final int nregSubTypeCode = (int) inputMap.get("nregsubtypecode");

			final String sFindSubSampleQuery = "select nstbstudyplancode, ntransactionstatus, nstbtimepointcode from stbtimepoint "
					+ " where nstbtimepointcode in (" + ntransactionsamplecode.substring(1)
					+ ") and  nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  and nsitecode=" + userInfo.getNtranssitecode() + ";";

			List<StbTimePoint> listAvailableSample = jdbcTemplate.query(sFindSubSampleQuery, new StbTimePoint());

			inputMap.put("directAddTest", false);
			inputMap.put("AvailableSample", listAvailableSample);

			inputMap.put("nstbtimepointcode", ntransactionsamplecode.substring(1));
			ntransactionsamplecode = ntransactionsamplecode.substring(1);
			Map<String, Object> createdTestMap = (Map<String, Object>) createTest(userInfo,
					Arrays.asList(ntransactionsamplecode), testGroupTestList, nregTypeCode, nregSubTypeCode, inputMap)
					.getBody();

			inputMap.put("nstbtimepointtestcode", createdTestMap.get("nstbtimepointtestcode"));

			returnMap.putAll((Map<String, Object>) getRegistrationSubSample(inputMap, userInfo));
			returnMap.putAll((Map<String, Object>) getRegistrationSampleAfterSubsampleAdd(inputMap, userInfo));
			jsonAuditObject.put("Stbtimepoint", (List<Map<String, Object>>) returnMap.get("selectedStbTimePoint"));

//			auditmap.put("nregtypecode",
//					((Map<String, Object>) ((Map<String, Object>) inputMap.get("masterData")).get("RegTypeValue"))
//							.get("nregtypecode"));
//			auditmap.put("nregsubtypecode",
//					((Map<String, Object>) ((Map<String, Object>) inputMap.get("masterData")).get("RegSubTypeValue"))
//							.get("nregsubtypecode"));
//			auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
//			actionType.put("Stbtimepoint", "IDS_ADDTIMEPOINT");

			// fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false,
			// userInfo);
			return returnMap;
		}

	}

	public Map<String, Object> getRegistrationSampleAfterSubsampleAdd(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> objMap = new HashMap<>();

		short nfilterstatus;
		if (inputMap.get("nfilterstatus").getClass().getTypeName().equals("java.lang.Integer")) {
			nfilterstatus = ((Integer) inputMap.get("nfilterstatus")).shortValue();
		} else {
			nfilterstatus = (short) inputMap.get("nfilterstatus");
		}

		final String query1 = "select * from fn_stbstudyplanget('" + inputMap.get("FromDate") + "'::text," + "'"
				+ inputMap.get("ToDate") + "'::text" + ",'" + inputMap.get("nstbstudyplancode") + "'::text,'"
				+ userInfo.getSlanguagetypecode() + "'::text," + inputMap.get("nregtypecode") + ","
				+ inputMap.get("nregsubtypecode") + "," + nfilterstatus + "," + userInfo.getNtranssitecode() + ","
				+ inputMap.get("napproveconfversioncode") + ")";

		LOGGER.info("Sample Start========?" + LocalDateTime.now());
		String lstData1 = jdbcTemplate.queryForObject(query1, String.class);
		LOGGER.info("Sample end========?" + query1 + " :" + LocalDateTime.now());
		List<Map<String, Object>> lstData = new ArrayList<>();

		if (lstData1 != null) {
			lstData = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(lstData1,
					userInfo, true, (int) inputMap.get("ndesigntemplatemappingcode"), "sample");
			LOGGER.info("Sample Size" + lstData.size());

			objMap.put("StabilityStudyPlanGet", lstData);

			String sstbstudyplancode = "";
			sstbstudyplancode = String.valueOf(lstData.get(lstData.size() - 1).get("nstbstudyplancode"));
			inputMap.put("nstbstudyplancode", sstbstudyplancode);
			objMap.put("selectedStabilityStudyPlan", Arrays.asList(lstData.get(lstData.size() - 1)));

			// Map<String, Object> map = (Map<String, Object>) getStbTimePoint(inputMap,
			// userInfo).getBody();
			// objMap.putAll(map);
		} else {
			objMap.put("selectedStabilityStudyPlan", lstData);
			objMap.put("StabilityStudyPlanGet", lstData);

			objMap.put("selectedStbTimePoint", lstData);
			objMap.put("StbTimePointGet", lstData);

			objMap.put("StbTimePointTestGet", lstData);
			objMap.put("selectedStbTimePointTest", lstData);

			objMap.put("StbTimePointTestParameter", lstData);
		}
		return objMap;

	}

	public Map<String, Object> getRegistrationSubSample(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> objMap = new HashMap<>();

		final String query1 = "select * from fn_stbtimepointget('" + inputMap.get("nstbstudyplancode") + "'::text,"
				+ "'" + inputMap.get("nstbtimepointcode") + "'::text" + "," + userInfo.getNtranssitecode() + ",'"
				+ userInfo.getSlanguagetypecode() + "')";

		LOGGER.info("sub sample Start========?" + LocalDateTime.now());
		LOGGER.info("sub sample query:" + query1);
		final String lstData1 = jdbcTemplate.queryForObject(query1, String.class);
		LOGGER.info("sub sample end========?" + LocalDateTime.now());

		if (lstData1 != null) {

			List<Map<String, Object>> lstData = (List<Map<String, Object>>) projectDAOSupport
					.getSiteLocalTimeFromUTCForDynamicTemplate(lstData1, userInfo, true,
							(int) inputMap.get("ndesigntemplatemappingcode"), "subsample");

			objMap.put("StbTimePointGet", lstData);

			String sstbtimepointcode = "";

			if (inputMap.containsKey("nstbtimepointcode")) {
				inputMap.put("nstbtimepointcode", inputMap.get("nstbtimepointcode"));
				objMap.put("selectedStbTimePoint", Arrays.asList(lstData.get(lstData.size() - 1)));
			} else {
				sstbtimepointcode = String.valueOf(lstData.get(lstData.size() - 1).get("nstbtimepointcode"));
				objMap.put("selectedStbTimePoint", Arrays.asList(lstData.get(lstData.size() - 1)));
				inputMap.put("nstbtimepointcode", sstbtimepointcode);
			}

			Map<String, Object> map = (Map<String, Object>) getRegistrationTest(inputMap, userInfo);
			objMap.putAll(map);

		} else {
			objMap.put("selectedStbTimePoint", Arrays.asList());
			objMap.put("StbTimePointGet", Arrays.asList());
			objMap.put("StbTimePointTestGet", Arrays.asList());
			objMap.put("selectedStbTimePointTest", Arrays.asList());
		}
		return objMap;

	}

	public ResponseEntity<Object> getRegistrationSubSampleAfterTestAdd(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> objMap = new HashMap<>();

		final String query1 = "select * from fn_stbtimepointget('" + inputMap.get("nstbstudyplancode") + "'::text,"
				+ "'" + inputMap.get("nstbtimepointcode") + "'::text" + "," + userInfo.getNtranssitecode() + ",'"
				+ userInfo.getSlanguagetypecode() + "')";

		LOGGER.info("sub sample Start========?" + LocalDateTime.now());
		LOGGER.info("sub sample query:" + query1);
		final String lstData1 = jdbcTemplate.queryForObject(query1, String.class);
		LOGGER.info("sub sample end========?" + LocalDateTime.now());

		if (lstData1 != null) {

			List<Map<String, Object>> lstData = (List<Map<String, Object>>) projectDAOSupport
					.getSiteLocalTimeFromUTCForDynamicTemplate(lstData1, userInfo, true,
							(int) inputMap.get("ndesigntemplatemappingcode"), "subsample");

			objMap.put("StbTimePointGet", lstData);

			String sstbtimepointcode = "";

			if (inputMap.containsKey("nstbtimepointcode")) {
				inputMap.put("nstbtimepointcode", inputMap.get("nstbtimepointcode"));
				objMap.put("selectedStbTimePoint", Arrays.asList(lstData.get(lstData.size() - 1)));
				// objMap.put("selectedStbTimePoint", lstData);
			} else {
				sstbtimepointcode = String.valueOf(lstData.get(lstData.size() - 1).get("nstbtimepointcode"));
				objMap.put("selectedStbTimePoint", Arrays.asList(lstData.get(lstData.size() - 1)));
				inputMap.put("nstbtimepointcode", sstbtimepointcode);
				// objMap.put("selectedStbTimePoint", lstData);
			}

//				Map<String, Object> map = (Map<String, Object>) getRegistrationTest(inputMap, userInfo).getBody();
//				objMap.putAll(map);
//			

		} else {
			objMap.put("selectedStbTimePoint", Arrays.asList());
			objMap.put("StbTimePointGet", Arrays.asList());
			objMap.put("StbTimePointTestGet", Arrays.asList());
			objMap.put("selectedStbTimePointTest", Arrays.asList());
		}
		return new ResponseEntity<>(objMap, HttpStatus.OK);

	}

//Working Get start
	public Map<String, Object> getRegistrationTest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> objMap = new HashMap<>();

		final String query1 = "select * from fn_stbtimepointtestget('" + inputMap.get("nstbstudyplancode") + "'::text,"
				+ "'" + inputMap.get("nstbtimepointcode") + "'::text" + ",'" + inputMap.get("nstbtimepointtestcode")
				+ "'::text," + userInfo.getNtranssitecode() + ",'" + userInfo.getSlanguagetypecode() + "')";

		LOGGER.info("test Start========?" + LocalDateTime.now());
		LOGGER.info("test Start========?" + LocalDateTime.now());

		final String lstData2 = jdbcTemplate.queryForObject(query1, String.class);
		LOGGER.info("fn_registrationtestget:" + query1);
		LOGGER.info("fn_registrationtestget:" + query1);
		LOGGER.info("test end========?" + LocalDateTime.now());
		LOGGER.info("test end========?" + LocalDateTime.now());
		if (lstData2 != null) {
			List<Map<String, Object>> lstData = (List<Map<String, Object>>) projectDAOSupport
					.getSiteLocalTimeFromUTCForDynamicTemplate(lstData2, userInfo, true,
							(int) inputMap.get("ndesigntemplatemappingcode"), "test");
			objMap.put("StbTimePointTestGet", lstData);
			objMap.put("selectedStbTimePointTest", Arrays.asList(lstData.get(lstData.size() - 1)));
		} else {
			objMap.put("selectedStbTimePointTest", Arrays.asList());
			objMap.put("RegistrationParameter", Arrays.asList());
			objMap.put("StbTimePointTestGet", Arrays.asList());

		}

		return objMap;

	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> createTest(final UserInfo userInfo, final List<String> listSample,
			final List<TestGroupTest> listTest, final int nregtypecode, final int nregsubtypecode,
			Map<String, Object> inputMap) throws Exception {

//		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
//		JSONObject actionType = new JSONObject();
//		JSONObject jsonAuditObject = new JSONObject();
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		int nTransactionTestCode = (int) inputMap.get("stbtimepointtest") + 1;
		int nRegistrationParameterCode = (int) inputMap.get("stbtimepointparameter") + 1;

		final List<StbTimePoint> listAvailableSample = (List<StbTimePoint>) inputMap.get("AvailableSample");

//		int approvalConfigVersionCode = -1;
//		List<ApprovalConfigAutoapproval> approvalConfigAutoApprovals = new ArrayList<ApprovalConfigAutoapproval>();
//		if (inputMap.containsKey("approvalconfigautoapproval")) {
//			approvalConfigAutoApprovals = (List<ApprovalConfigAutoapproval>) inputMap.get("approvalconfigautoapproval");
//		}

		final String sntestgrouptestcode = stringUtilityFunction.fnDynamicListToString(listTest,
				"getNtestgrouptestcode");

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

		List<String> replicateTestList = new ArrayList<String>();
		List<String> replicateTestHistoryList = new ArrayList<String>();
		List<String> replicateTestParameterList = new ArrayList<String>();
		List<Integer> transactionTestCodeList = new ArrayList<Integer>();

		boolean directAddTest = true;
		if (inputMap.containsKey("directAddTest") && (boolean) inputMap.get("directAddTest") == false) {
			directAddTest = false;
		}

		for (StbTimePoint objRegistrationSample : listAvailableSample) {

			List<String> rejectTest = new ArrayList<String>();

			for (int testindex = 0; testindex < listTest.size(); testindex++) {
				TestGroupTest objTestGroupTestParameter = listTest.get(testindex);

				final List<TestGroupTestParameter> testParameterList = parameterList.stream()
						.filter(parameter -> parameter.getNtestgrouptestcode() == objTestGroupTestParameter
								.getNtestgrouptestcode())
						.collect(Collectors.toList());

				final List<TestGroupTestParameter> nonMandatoryTestList = testParameterList.stream()
						.filter(parameter -> parameter.getNresultmandatory() == Enumeration.TransactionStatus.NO
								.gettransactionstatus() && parameter.getSresultvalue() == null)
						.collect(Collectors.toList());

				if (nonMandatoryTestList.size() == testParameterList.size()) {
					rejectTest.add(String.valueOf(objTestGroupTestParameter.getNtestgrouptestcode()));
				}

			}
			List<TestGroupTest> testGroupTest = listTest.stream()
					.filter(test -> rejectTest.stream()
							.noneMatch(reject -> Integer.valueOf(reject) == test.getNtestgrouptestcode()))
					.collect(Collectors.toList());

			if (testGroupTest.size() > 0) {
				final String testCodeToComplete = testGroupTest.stream()
						.map(testGroup -> String.valueOf(testGroup.getNtestgrouptestcode()))
						.collect(Collectors.joining(","));

				final Map<String, Object> testMap = createQueryCreateTestParameterHistory(objRegistrationSample,
						testCodeToComplete, nTransactionTestCode, nRegistrationParameterCode, replicateTestList,
						replicateTestHistoryList, replicateTestParameterList, userInfo, sntestgrouptestcode, inputMap,
						transactionTestCodeList);

				replicateTestList = (ArrayList<String>) testMap.get("replicateTestList");
				replicateTestParameterList = (ArrayList<String>) testMap.get("replicateTestParameterList");
				nTransactionTestCode = (int) testMap.get("nstbtimepointtestcode");
				nRegistrationParameterCode = (int) testMap.get("nstbtimepointparametercode");

				transactionTestCodeList = (ArrayList<Integer>) testMap.get("transactionTestCodeList");

			}
		}
		if (replicateTestList.size() > 0) {
			final String strRegistrationTest = "insert into stbtimepointtest (nstbtimepointtestcode,nstbtimepointcode,nstbstudyplancode,"
					+ "ntestgrouptestcode,ninstrumentcatcode,nchecklistversioncode,ntestrepeatno,ntestretestno,jsondata,nsitecode,dmodifieddate, "
					+ " nstatus,ntestcode,nsectioncode,nmethodcode) values ";
			jdbcTemplate.execute(strRegistrationTest + String.join(",", replicateTestList));

		}

		if (replicateTestParameterList.size() > 0) {
			final String strRegTestParameter = " insert into stbtimepointparameter (nstbtimepointparametercode,"
					+ " nstbstudyplancode,nstbtimepointtestcode,ntestgrouptestparametercode,"
					+ " ntestparametercode,nparametertypecode,ntestgrouptestformulacode,"
					+ " nunitcode, nresultmandatory,nreportmandatory,jsondata,nsitecode,nstatus) values";
			jdbcTemplate.execute(strRegTestParameter + String.join(",", replicateTestParameterList));
		}

		final String createdTestCode = String.join(",",
				transactionTestCodeList.stream().map(String::valueOf).collect(Collectors.toList()));

		inputMap.put("nstbtimepointtestcode", createdTestCode);
		final Map<String, Object> returnmap = new HashMap<String, Object>();

		returnmap.put("nstbtimepointtestcode", createdTestCode);

		if (directAddTest) {
			if (inputMap.get("nstbtimepointtestcode") != null && inputMap.get("nstbtimepointtestcode") != "") {
				returnmap.putAll((Map<String, Object>) getRegistrationTest(inputMap, userInfo));
				returnmap.putAll(
						(Map<String, Object>) getRegistrationSubSampleAfterTestAdd(inputMap, userInfo).getBody());
				returnmap.putAll((Map<String, Object>) getRegistrationSampleAfterSubsampleAdd(inputMap, userInfo));

//				jsonAuditObject.put("stbtimepointtest", (List<Map<String, Object>>) returnmap.get("selectedStbTimePointTest"));
//				auditmap.put("nregtypecode", nregtypecode);
//				auditmap.put("nregsubtypecode", nregsubtypecode);
//				auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
//				actionType.put("stbtimepointtest","IDS_ADDTEST");
//				fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);
			}
		}

		return new ResponseEntity<Object>(returnmap, HttpStatus.OK);

	}

	private Map<String, Object> createQueryCreateTestParameterHistory(StbTimePoint objRegistrationSample, // int
			// nRecievedStatus,
			String comPleted, int nTransactionTestCode, int nRegistrationParameterCode, List<String> replicateTestList,
			List<String> replicateTestHistoryList, List<String> replicateTestParameterList, UserInfo userInfo,
			String sntestgrouptestcode, final Map<String, Object> inputMap, List<Integer> transactionTestCodeList)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();

		final String testGroupQuery = " select distinct tgt.nrepeatcountno, tgt.ntestgrouptestcode, tgt.ntestcode,tgt.stestsynonym, tgt.nsectioncode,"
				+ " s.ssectionname, tgt.nmethodcode, m.smethodname, tgt.ninstrumentcatcode, tm.nchecklistversioncode,"
				+ " coalesce((" + " select max(ntestrepeatno) + 1 from stbtimepointtest "
				+ " where nstbtimepointcode in (" + objRegistrationSample.getNstbtimepointcode() + ")"
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ntestgrouptestcode = tgt.ntestgrouptestcode" + " ),1) ntestrepeatno,"
				+ Enumeration.TransactionStatus.ALL.gettransactionstatus() + " ntestretestno, " + " tgt.ncost,"
//				+ " case when " + needJobAllocation + " = false then " + stestStatus + " else "
//				+ " coalesce((select max(ntransactionstatus) from registrationsectionhistory " + " where npreregno="
//				+ objRegistrationSample.getNpreregno() + " and nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
//				+ userInfo.getNtranssitecode() + " and nsectioncode = tgt.nsectioncode), " + stestStatus
//				+ " ) end ntransactionstatus,"
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus"
				+ " from testgrouptest tgt, testmaster tm, transactionstatus ts, section s,method m  "
				+ " where tm.ntestcode = tgt.ntestcode" + " and tgt.ntestgrouptestcode in (" + comPleted
				+ ") and tgt.nstatus = tm.nstatus" + " and tgt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				// + " and ts.ntranscode in (" + stestStatus
				// + ") "
				+ " and ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tgt.nsectioncode = s.nsectioncode and tgt.nmethodcode=m.nmethodcode "
				+ " and s.nstatus= m.nstatus and m.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		List<StbTimePointTest> testGroupTestList = jdbcTemplate.query(testGroupQuery, new StbTimePointTest());

		testGroupTestList = getTestPrice(testGroupTestList,userInfo);

		String strQuery = "";
		String strAdhocQuery = "";
		strQuery = "json_build_object('sminlod',tgtnp.sminlod,'smaxlod',tgtnp.smaxlod,'sminb',tgtnp.sminb,'smina',tgtnp.smina,"
				+ "'smaxa',tgtnp.smaxa,'smaxb',tgtnp.smaxb,'sminloq',tgtnp.sminloq,'smaxloq',tgtnp.smaxloq,'ngradecode',tgtnp.ngradecode,'nresultaccuracycode',tgtp.nresultaccuracycode,'sresultaccuracyname',ra.sresultaccuracyname, "
				+ "'sdisregard',tgtnp.sdisregard,'sresultvalue',tgtnp.sresultvalue,"
				+ "'nroundingdigits',tgtp.nroundingdigits,'sresult',NULL,'sfinal',NULL,'stestsynonym',concat(tgt.stestsynonym,'[',cast(  "
				+ " coalesce((select max(ntestrepeatno)  from stbtimepointtest " + "	where nstbtimepointcode in ("
				+ objRegistrationSample.getNstbtimepointcode() + ") and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNtranssitecode() + " and ntestgrouptestcode = tgt.ntestgrouptestcode" + "	),1)"
				+ " as character varying),']','[0]'),'sparametersynonym',tgtp.sparametersynonym)::jsonb jsondata" + " ,"
				+ userInfo.getNtranssitecode() + " nsitecode,"
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
				+ "from resultaccuracy ra,testmaster tm,testgrouptest tgt,testgrouptestparameter tgtp ";
		strAdhocQuery = " and tgtp.nisvisible=" + Enumeration.TransactionStatus.YES.gettransactionstatus();

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

		for (StbTimePointTest regTestGroupTest : testGroupTestList) {
			if (regTestGroupTest.getNrepeatcountno() > 1) {
				for (int repeatNo = regTestGroupTest.getNtestrepeatno(); repeatNo < (regTestGroupTest.getNtestrepeatno()
						+ regTestGroupTest.getNrepeatcountno()); repeatNo++) {
					int nttestcode = nTransactionTestCode++;
					transactionTestCodeList.add(nttestcode);

					String strQuery1 = "select ssettingvalue from settings where nsettingcode ="
							+ Enumeration.Settings.UPDATING_ANALYSER.getNsettingcode() + " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";
					Settings objSettings = (Settings) jdbcUtilityFunction.queryForObject(strQuery1, Settings.class,
							jdbcTemplate);
					String AnalyserValue = "";

					if (objSettings != null) {
						if (Integer.valueOf(objSettings.getSsettingvalue()) == Enumeration.TransactionStatus.YES
								.gettransactionstatus()) {
							AnalyserValue = ",'AnalyserCode',-1,'AnalyserName','-'";
						}
					}

					replicateTestList.add("(" + nttestcode + "," + objRegistrationSample.getNstbtimepointcode() + ","
							+ objRegistrationSample.getNstbstudyplancode() + ","
							+ regTestGroupTest.getNtestgrouptestcode() + "," + regTestGroupTest.getNinstrumentcatcode()
							+ ",-1," + repeatNo + ",0," + " json_build_object('nstbtimepointtestcode', " + nttestcode
							+ ",'nstbstudyplancode'," + objRegistrationSample.getNstbstudyplancode()
							+ ",'nstbtimepointcode'," + objRegistrationSample.getNstbtimepointcode()
							+ ",'ssectionname','"
							+ stringUtilityFunction.replaceQuote(regTestGroupTest.getSsectionname())
							+ "','smethodname','"
							+ stringUtilityFunction.replaceQuote(regTestGroupTest.getSmethodname()) + "','ncost',"
							+ regTestGroupTest.getNcost() + "," + "'stestname','"
							+ stringUtilityFunction.replaceQuote(regTestGroupTest.getStestsynonym()) + "',"
							+ "'stestsynonym',concat('"
							+ stringUtilityFunction.replaceQuote(regTestGroupTest.getStestsynonym()) + "','[" + repeatNo
							+ "][" + regTestGroupTest.getNtestretestno() + "]')" + AnalyserValue + ")::jsonb,"
							+ userInfo.getNtranssitecode() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo)
							+ "'," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
							+ regTestGroupTest.getNtestcode() + "," + regTestGroupTest.getNsectioncode() + ","
							+ regTestGroupTest.getNmethodcode() + ")");

					for (TestGroupTestParameter registrationParameter : parameterList) {
						if (regTestGroupTest.getNtestgrouptestcode() == registrationParameter.getNtestgrouptestcode()) {
							int nttestparametercode = nRegistrationParameterCode++;

							final Map<String, Object> mapObject = registrationParameter.getJsondata();
							mapObject.put("nstbtimepointparametercode", nttestparametercode);
							mapObject.put("nstbtimepointtestcode", nttestcode);
							mapObject.put("stestsynonym", regTestGroupTest.getStestsynonym() + "[" + repeatNo + "]["
									+ regTestGroupTest.getNtestretestno() + "]");

							replicateTestParameterList.add("(" + nttestparametercode + ","
									+ objRegistrationSample.getNstbstudyplancode() + "," + nttestcode + ","
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
			} else {
				// no replicate mentioned in test group
				int nttestcode = nTransactionTestCode++;
				transactionTestCodeList.add(nttestcode);

				String strQuery1 = "select ssettingvalue from settings where nsettingcode ="
						+ Enumeration.Settings.UPDATING_ANALYSER.getNsettingcode() + " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";
				Settings objSettings = (Settings) jdbcUtilityFunction.queryForObject(strQuery1, Settings.class,
						jdbcTemplate);
				String AnalyserValue = "";

				if (objSettings != null) {
					if (Integer.valueOf(objSettings.getSsettingvalue()) == Enumeration.TransactionStatus.YES
							.gettransactionstatus()) {
						AnalyserValue = ",'AnalyserCode',-1,'AnalyserName','-'";
					}
				}

				replicateTestList.add("(" + nttestcode + "," + objRegistrationSample.getNstbtimepointcode() + ","
						+ objRegistrationSample.getNstbstudyplancode() + "," + regTestGroupTest.getNtestgrouptestcode()
						+ "," + regTestGroupTest.getNinstrumentcatcode() + ",-1,"
						+ (regTestGroupTest.getNtestrepeatno()) + ",0," + " json_build_object('nstbtimepointtestcode', "
						+ nttestcode + ",'nstbstudyplancode'," + objRegistrationSample.getNstbstudyplancode()
						+ ",'nstbtimepointcode'," + objRegistrationSample.getNstbtimepointcode() + ",'ssectionname','"
						+ stringUtilityFunction.replaceQuote(regTestGroupTest.getSsectionname()) + "','smethodname','"
						+ stringUtilityFunction.replaceQuote(regTestGroupTest.getSmethodname()) + "','ncost',"
						+ regTestGroupTest.getNcost() + "," + "'stestname','"
						+ stringUtilityFunction.replaceQuote(regTestGroupTest.getStestsynonym()) + "',"
						+ "'stestsynonym',concat('"
						+ stringUtilityFunction.replaceQuote(regTestGroupTest.getStestsynonym()) + "','["
						+ (regTestGroupTest.getNtestrepeatno()) + "][0]')" + AnalyserValue + ")::jsonb,"
						+ userInfo.getNtranssitecode() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
						+ regTestGroupTest.getNtestcode() + "," + regTestGroupTest.getNsectioncode() + ","
						+ regTestGroupTest.getNmethodcode() + ")");

				for (TestGroupTestParameter testGroupTestParameter : parameterList) {
					if (regTestGroupTest.getNtestgrouptestcode() == testGroupTestParameter.getNtestgrouptestcode()) {
						int nttestparametercode = nRegistrationParameterCode++;

						final Map<String, Object> mapObject = testGroupTestParameter.getJsondata();
						mapObject.put("ntransactionresultcode", nttestparametercode);
						mapObject.put("ntransactiontestcode", nttestcode);
						mapObject.put("stestsynonym", regTestGroupTest.getStestsynonym() + "["
								+ (regTestGroupTest.getNtestrepeatno()) + "][0]");

						replicateTestParameterList.add(
								"(" + nttestparametercode + "," + objRegistrationSample.getNstbstudyplancode() + ","
										+ nttestcode + "," + testGroupTestParameter.getNtestgrouptestparametercode()
										+ "," + testGroupTestParameter.getNtestparametercode() + ","
										+ testGroupTestParameter.getNparametertypecode() + ","
										+ testGroupTestParameter.getNtestgrouptestformulacode() + ","
										+ testGroupTestParameter.getNunitcode() + ","
										+ testGroupTestParameter.getNresultmandatory() + ","
										+ testGroupTestParameter.getNreportmandatory() + ","
// + testGroupTestParameter.getJsondata()+","
// + "'" + ReplaceQuote(testGroupTestParameter.getJsondata().toString()) + "',"
// + "'" +objmap.writeValueAsString(testGroupTestParameter.getJsondata()) +"',"
										+ "'"
										+ stringUtilityFunction.replaceQuote(objMapper.writeValueAsString(mapObject))
										+ "'," + userInfo.getNtranssitecode() + ","
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")");
					}
				}

			}
		}

		final Map<String, Object> returnMap = new HashMap<String, Object>();

		returnMap.put("replicateTestHistoryList", replicateTestHistoryList);
		returnMap.put("replicateTestList", replicateTestList);
		returnMap.put("replicateTestParameterList", replicateTestParameterList);
		returnMap.put("nstbtimepointtestcode", nTransactionTestCode);
		returnMap.put("nstbtimepointparametercode", nRegistrationParameterCode);
		returnMap.put("transactionTestCodeList", transactionTestCodeList);

		return returnMap;
	}

	public StbStudyPlan getActiveStbStudyPlanById(final int nstbstudyplancode, final UserInfo userInfo)
			throws Exception {

		final String strQuery = "select * from stbstudyplan where " + " nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nstbstudyplancode = "
				+ nstbstudyplancode + " and nsitecode=" + userInfo.getNtranssitecode();

		return (StbStudyPlan) jdbcUtilityFunction.queryForObject(strQuery, StbStudyPlan.class, jdbcTemplate);
	}

	public StbTimePoint getActiveStbTimePointById(final int nstbtimepointcode, final UserInfo userInfo)
			throws Exception {

		final String strQuery = "select * from stbtimepoint where " + " nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nstbtimepointcode = "
				+ nstbtimepointcode + " and nsitecode=" + userInfo.getNtranssitecode();

		return (StbTimePoint) jdbcUtilityFunction.queryForObject(strQuery, StbTimePoint.class, jdbcTemplate);
	}

	public StbTimePointTest getActiveStbTimePointTestById(final int nstbtimepointtestcode, final UserInfo userInfo)
			throws Exception {

		final String strQuery = "select * from stbtimepointtest where " + " nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nstbtimepointtestcode = "
				+ nstbtimepointtestcode + " and nsitecode=" + userInfo.getNtranssitecode();

		return (StbTimePointTest) jdbcUtilityFunction.queryForObject(strQuery, StbTimePointTest.class, jdbcTemplate);
	}

	public ResponseEntity<Object> deleteTest(Map<String, Object> inputMap) throws Exception {
		Map<String, Object> returnMap = new HashMap<>();

		int nstbstudyplancode = Integer.valueOf(inputMap.get("nstbstudyplancode").toString());
		int nstbtimepointcode = Integer.valueOf(inputMap.get("nstbtimepointcode").toString());
		int nstbtimepointtestcode = Integer.valueOf(inputMap.get("nstbtimepointtestcode").toString());

		UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final StbStudyPlan stbStudyPlanByID = (StbStudyPlan) getActiveStbStudyPlanById(nstbstudyplancode, userInfo);
		final StbTimePoint stbTimePointByID = (StbTimePoint) getActiveStbTimePointById(nstbtimepointcode, userInfo);
		final StbTimePointTest stbTimePointTestByID = (StbTimePointTest) getActiveStbTimePointTestById(
				nstbtimepointtestcode, userInfo);

		if (stbStudyPlanByID == null) {

			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (stbTimePointByID == null) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				if (stbTimePointTestByID == null) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				} else {
					String updateQueryString = "";

					updateQueryString = "update stbtimepointtest " + " set  nstatus ="
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "," + " dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nstbtimepointtestcode="
							+ nstbtimepointtestcode + ";";

					updateQueryString = updateQueryString + "update stbtimepointparameter" + " set   nstatus ="
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
							+ " where nstbtimepointtestcode=" + nstbtimepointtestcode + ";";

					jdbcTemplate.execute(updateQueryString);
					inputMap.remove("nstbtimepointtestcode");
					// returnMap.putAll((Map<String, Object>) getRegistrationTest(returnMap,
					// userInfo).getBody());
					returnMap.putAll((Map<String, Object>) getRegistrationSubSample(inputMap, userInfo));
					returnMap.putAll((Map<String, Object>) getRegistrationSampleAfterSubsampleAdd(inputMap, userInfo));
				}
			}
		}
		return new ResponseEntity<Object>(returnMap, HttpStatus.OK);

	}

	public ResponseEntity<Object> deleteStbTimePoint(Map<String, Object> inputMap) throws Exception {
		Map<String, Object> returnMap = new HashMap<>();

		int nstbstudyplancode = Integer.valueOf(inputMap.get("nstbstudyplancode").toString());
		int nstbtimepointcode = Integer.valueOf(inputMap.get("nstbtimepointcode").toString());

		UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final StbStudyPlan stbStudyPlanByID = (StbStudyPlan) getActiveStbStudyPlanById(nstbstudyplancode, userInfo);
		final StbTimePoint stbTimePointByID = (StbTimePoint) getActiveStbTimePointById(nstbtimepointcode, userInfo);

		if (stbStudyPlanByID == null) {

			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (stbTimePointByID == null) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {

				String testQuery = "select * from stbtimepointtest where nstbtimepointcode=" + nstbtimepointcode + " "
						+ "and nsitecode ="+userInfo.getNtranssitecode()+" and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final List<StbTimePointTest> listAvailableTest = jdbcTemplate.query(testQuery, new StbTimePointTest());
				

				String updateQueryString = "";
				updateQueryString = "update stbtimepoint " + " set  nstatus ="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "," + " dtransactiondate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nstbtimepointcode="
						+ nstbtimepointcode + ";";

				if(listAvailableTest.size()>0) {
					
					final String stbtimepointtestcode = listAvailableTest.stream()
							.map(item -> String.valueOf(item.getNstbtimepointtestcode())).collect(Collectors.joining(","));
					updateQueryString = updateQueryString + "update stbtimepointtest " + " set  nstatus ="
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "," + " dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nstbtimepointcode="
							+ nstbtimepointcode + ";";
	
					updateQueryString = updateQueryString + "update stbtimepointparameter" + " set   nstatus ="
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
							+ " where nstbtimepointtestcode in (" + stbtimepointtestcode + ");";
				}
				jdbcTemplate.execute(updateQueryString);
				inputMap.remove("nstbtimepointcode");
				inputMap.remove("nstbtimepointtestcode");
				returnMap.putAll((Map<String, Object>) getRegistrationSubSample(inputMap, userInfo));
				returnMap.putAll((Map<String, Object>) getRegistrationSampleAfterSubsampleAdd(inputMap, userInfo));
			}
		}
		return new ResponseEntity<Object>(returnMap, HttpStatus.OK);

	}

	public ResponseEntity<Object> deleteStbStudyPlan(Map<String, Object> inputMap) throws Exception {
		Map<String, Object> returnMap = new HashMap<>();

		int nstbstudyplancode = Integer.valueOf(inputMap.get("nstbstudyplancode").toString());

		UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final StbStudyPlan stbStudyPlanByID = (StbStudyPlan) getActiveStbStudyPlanById(nstbstudyplancode, userInfo);

		if (stbStudyPlanByID == null) {

			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

//				String testQuery="select * from stbtimepointtest where nstbstudyplancode="+nstbstudyplancode+" "
//						+ "and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//				final List<StbTimePointTest> listAvailableTest=jdbcTemplate.query(testQuery,
//						new StbTimePointTest());
//				
//				final String stbtimepointtestcode = listAvailableTest.stream()
//						.map(item -> String.valueOf(item.getNstbtimepointtestcode())).collect(Collectors.joining(","));

			String updateQueryString = "";
			updateQueryString = updateQueryString + "update stbstudyplan " + " set  nstatus ="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "," + " dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nstbstudyplancode="
					+ nstbstudyplancode + ";";

			updateQueryString = updateQueryString + "update stbtimepoint " + " set  nstatus ="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "," + " dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nstbstudyplancode="
					+ nstbstudyplancode + ";";

			updateQueryString = updateQueryString + "update stbtimepointtest " + " set  nstatus ="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "," + " dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nstbstudyplancode="
					+ nstbstudyplancode + ";";

			updateQueryString = updateQueryString + "update stbtimepointparameter" + " set   nstatus ="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nstbstudyplancode in ("
					+ nstbstudyplancode + ");";

			jdbcTemplate.execute(updateQueryString);

			final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
			final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());

			String fromDate = LocalDateTime.parse((String) inputMap.get("FromDate"), dbPattern).format(uiPattern);
			String toDate = LocalDateTime.parse((String) inputMap.get("ToDate"), dbPattern).format(uiPattern);
			inputMap.put("RealFromDate", fromDate);
			inputMap.put("RealToDate", toDate);
			fromDate = dateUtilityFunction.instantDateToString(
					dateUtilityFunction.convertStringDateToUTC((String) inputMap.get("FromDate"), userInfo, true));
			toDate = dateUtilityFunction.instantDateToString(
					dateUtilityFunction.convertStringDateToUTC((String) inputMap.get("ToDate"), userInfo, true));
			inputMap.put("FromDate", fromDate);
			inputMap.put("ToDate", toDate);
			inputMap.remove("nstbstudyplancode");
			inputMap.remove("nstbtimepointcode");
			inputMap.remove("nstbtimepointtestcode");
			returnMap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
			returnMap.putAll((Map<String, Object>) getDynamicRegistration(inputMap, userInfo));
		}
		return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getStabilityStudyPlanByFilterSubmit(Map<String, Object> uiMap) throws Exception {
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

		ReactRegistrationTemplate lstTemplate = (ReactRegistrationTemplate) getRegistrationTemplate(nregtype,
				nregsubtype,userinfo).getBody();
		if (lstTemplate != null) {
			returnMap.put("ndesigntemplatemappingcode", lstTemplate.getNdesigntemplatemappingcode());
		}

//		List<FilterName> lstFilterName=getFilterName(userinfo);
//		returnMap.put("FilterName",lstFilterName);

		ReactRegistrationTemplate lstTemplate1 = (ReactRegistrationTemplate) getRegistrationTemplatebasedontemplatecode(
				nregtype, nregsubtype, ndesigntemplatemappingcode,userinfo).getBody();

		if (lstTemplate1 != null) {
			returnMap.put("registrationTemplate", lstTemplate1);
			returnMap.put("SubSampleTemplate", getRegistrationSubSampleTemplate(ndesigntemplatemappingcode,userinfo).getBody());
			returnMap.put("DynamicDesign", getTemplateDesign(ndesigntemplatemappingcode, userinfo.getNformcode(),userinfo));
			returnMap.putAll(getDynamicRegistration(uiMap, userinfo));
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
		// ALPD-4912-To insert data when filter submit,done by Dhanushya RI
		if (uiMap.containsKey("saveFilterSubmit") && (boolean) uiMap.get("saveFilterSubmit") == true) {
			projectDAOSupport.createFilterSubmit(uiMap, userinfo);
		}
		if (uiMap.containsKey("flag")) {
			returnMap.put("RegistrationType", getRegistrationType(nsampletypecode, userinfo));
			returnMap.put("RegistrationSubType", getRegistrationSubType(nregtype, userinfo));
		}
		return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> approveStabilityStudyPlan(Map<String, Object> inputMap) throws Exception {
		Map<String, Object> returnMap = new HashMap<>();

		int nstbstudyplancode = Integer.valueOf(inputMap.get("nstbstudyplancode").toString());

		UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final StbStudyPlan stbStudyPlanByID = (StbStudyPlan) getActiveStbStudyPlanById(nstbstudyplancode, userInfo);

		if (stbStudyPlanByID == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (stbStudyPlanByID.getNtransactionstatus() == Enumeration.TransactionStatus.APPROVED
					.gettransactionstatus()) {

				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_ALREADYAPPROVED", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				String updateQueryString = "";

				updateQueryString = "update stbstudyplan " + " set  ntransactionstatus ="
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + "," + " dtransactiondate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nstbstudyplancode="
						+ nstbstudyplancode + ";";
				jdbcTemplate.execute(updateQueryString);

				String sampleQuery = "select * from stbtimepoint where nstbstudyplancode=" + nstbstudyplancode + " "
						+ " and nsitecode="+userInfo.getNtranssitecode()+" and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final List<StbTimePoint> listAvailableSample = jdbcTemplate.query(sampleQuery, new StbTimePoint());
				String schedulertransaction = "";

				int nSeqNo = (int) jdbcUtilityFunction.queryForObject(
						"select nsequenceno from seqnostability where stablename='stbschedulertransaction' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ",
						Integer.class, jdbcTemplate);
				for (StbTimePoint sampleList : listAvailableSample) {
					nSeqNo++;
					Map<String, Object> dateValue = sampleList.getJsonuidata();
					schedulertransaction = schedulertransaction + " (" + nSeqNo + " ," + nstbstudyplancode + " ,"
							+ sampleList.getNstbtimepointcode() + " ,"
							+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ",'"
							+ dateValue.get("Occurrence Date") + "','"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
							+ userInfo.getNtimezonecode() + ", "
							+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", "
							+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ", "
							+ userInfo.getNtranssitecode() + " ,"
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ),";
				}
				schedulertransaction = "insert into stbschedulertransaction("
						+ " nstbschedulertransactioncode, nstbregistrationcode, nstbregistrationsamplecode, npreregno,"
						+ " dscheduleoccurrencedate, dtransactiondate, noffsetdtransactiondate, ntransdatetimezonecode,  "
						+ " ntransactionstatus, nactivestatus, nsitecode, nstatus) values "
						+ schedulertransaction.substring(0, schedulertransaction.length() - 1) + ";";
				jdbcTemplate.execute(schedulertransaction);
				jdbcTemplate.execute("update seqnostability set nsequenceno = " + nSeqNo
						+ " where stablename='stbschedulertransaction'");

				returnMap.putAll((Map<String, Object>) getDynamicRegistration(inputMap, userInfo));

			}
		}
		return new ResponseEntity<Object>(returnMap, HttpStatus.OK);

	}

	
	public Map<String,Object> getEditStbTimePointDetails(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();
		if (inputMap.get("nstbtimepointcode") == null || inputMap.get("nstbtimepointcode") == "") {

			outputMap.put("ReturnStatus",
					commonFunction.getMultilingualMessage("IDS_SELECTSUBSAMPLE", userInfo.getSlanguagefilename()));
			return outputMap;
		} else {
			final String query = "  select json_agg(st.jsonuidata||st.jsondata"
					+ "||json_build_object('ntransactionstatus',st.ntransactionstatus,"	+ "'stransdisplaystatus', ts.jsondata->'" + userInfo.getSlanguagetypecode()+ "'->>'stransdisplaystatus'" + ")::jsonb) "
					+ "	from stbtimepoint st "
					+ " join transactionstatus ts on ts.ntranscode = st.ntransactionstatus "
					+ " join component c on c.ncomponentcode=st.ncomponentcode "
					+ "	where st.nstbtimepointcode in ("+ inputMap.get("nstbtimepointcode") + ")      "
					+ "	and ts.nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ "	and c.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and st.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and st.nsitecode = "+ userInfo.getNtranssitecode() + "";

			final String dynamicList = jdbcTemplate.queryForObject(query, String.class);

			List<Map<String, Object>> lstData = (List<Map<String, Object>>) projectDAOSupport
					.getSiteLocalTimeFromUTCForDynamicTemplate(dynamicList, userInfo, true,
							(int) inputMap.get("ndesigntemplatemappingcode"), "subsample");
			outputMap.put("EditData", lstData.get(0));

			if (inputMap.containsKey("getSubSampleChildDetail")
					&& (Boolean) inputMap.get("getSubSampleChildDetail") == true) {
				final Map<String, Object> getMapData = new HashMap<String, Object>();
				getMapData.put("nsampletypecode", inputMap.get("nsampletypecode"));
				getMapData.put("nstbstudyplancode", Integer.toString((Integer) (inputMap.get("nstbstudyplancode"))));
				getMapData.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
				getMapData.put("userinfo", userInfo);
				getMapData.put("nstbtimepointcode", Integer.toString((Integer) inputMap.get("nstbtimepointcode")));

				final Map<String, Object> childDetailMap = new HashMap<String, Object>();

				childDetailMap.putAll((Map<String, Object>) getRegistrationTest(getMapData, userInfo));
				outputMap.put("SubSampleChildDetail", childDetailMap);
			}

			return outputMap;
		}
	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> updateStbTimePoint(final Map<String, Object> inputMap) throws Exception {
		Map<String, Object> objmap = new LinkedHashMap<String, Object>();
//		JSONObject actionType = new JSONObject();
//		Map<String, Object> returnMapold = new LinkedHashMap<String, Object>();
//		JSONObject jsonAuditOld = new JSONObject();
//		JSONObject jsonAuditNew = new JSONObject();
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		Map<String, Object> returnMap = null;
		Map<String, Object> initialParam = (Map<String, Object>) inputMap.get("initialparam");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		// returnMapold = (Map<String, Object>) getRegistrationSubSample(initialParam,
		// userInfo).getBody();
		final String nstbtimepointcode = (String) initialParam.get("nstbtimepointcode");
		final StbTimePoint reg = objMapper.convertValue(inputMap.get("StbTimePoint"),
				new TypeReference<StbTimePoint>() {
				});

		List<String> dateList = objMapper.convertValue(inputMap.get("SubSampleDateList"),
				new TypeReference<List<String>>() {
				});

		List<Map<String, Object>> subsamplecombinationunique = (List<Map<String, Object>>) inputMap
				.get("subsamplecombinationunique");

		Map<String, Object> map1 = validateUniqueConstraint(subsamplecombinationunique,
				(Map<String, Object>) inputMap.get("StbTimePoint"), userInfo, "create", StbTimePoint.class,
				"nstbstudyplancode", false);
		if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
				.equals(map1.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(map1.get("rtn").toString(), userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);

		}

		JSONObject jsoneditRegistration = new JSONObject(reg.getJsondata());
		JSONObject jsonuiRegistration = new JSONObject(reg.getJsonuidata());
		if (!dateList.isEmpty()) {
			jsoneditRegistration = (JSONObject) dateUtilityFunction
					.convertJSONInputDateToUTCByZone(jsoneditRegistration, dateList, false, userInfo);
			jsonuiRegistration = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonuiRegistration,
					dateList, false, userInfo);

		}

		String queryString = "update stbtimepoint set jsondata=jsondata||'"
				+ stringUtilityFunction.replaceQuote(jsoneditRegistration.toString())
				+ "'::jsonb,jsonuidata=jsonuidata||'"
				+ stringUtilityFunction.replaceQuote(jsonuiRegistration.toString())
				+ "'::jsonb  where nstbtimepointcode in (" + nstbtimepointcode + ");";

		jdbcTemplate.execute(queryString);

		returnMap = (Map<String, Object>) getRegistrationSubSample(initialParam, userInfo);
		objmap.put("ndesigntemplatemappingcode", initialParam.get("ndesigntemplatemappingcode"));
		// actionType.put("registrationsample", "IDS_EDITSUBSAMPLE");
		// jsonAuditOld.put("registrationsample", (List<Map<String, Object>>)
		// returnMapold.get("selectedSubSample"));
		// jsonAuditNew.put("registrationsample", (List<Map<String, Object>>)
		// returnMap.get("selectedSubSample"));
		// fnJsonArrayAudit(jsonAuditOld, jsonAuditNew, actionType, objmap, false,
		// userInfo);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getRegTypeBySampleType(Map<String, Object> inputMap) throws Exception {

		Map<String, Object> objmap = new HashMap<String, Object>();
		List<RegistrationType> lstRegistrationType = new ArrayList<RegistrationType>();
		List<RegistrationSubType> lstRegistrationSubType = new ArrayList<RegistrationSubType>();
		List<ReactRegistrationTemplate> lstReactRegistrationTemplate = new ArrayList<ReactRegistrationTemplate>();
		List<ApprovalConfigAutoapproval> listApprovalConfigAutoapproval = new ArrayList<ApprovalConfigAutoapproval>();

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		int nsampletypecode = (int) inputMap.get("nsampletypecode");
		List<TransactionStatus> listTransactionstatus = new ArrayList<TransactionStatus>();
		lstRegistrationType = getRegistrationType(nsampletypecode, userInfo);

		if (lstRegistrationType != null && lstRegistrationType.size() > 0) {
			lstRegistrationSubType = getRegistrationSubType(lstRegistrationType.get(0).getNregtypecode(), userInfo);
			objmap.put("RegTypeValue", lstRegistrationType.get(0));
			objmap.put("RegSubTypeValue", lstRegistrationSubType.get(0));
			objmap.put("nregsubtypeversioncode", lstRegistrationSubType.get(0).getNregsubtypeversioncode());
			listTransactionstatus = getFilterStatus(lstRegistrationType.get(0).getNregtypecode(),
					lstRegistrationSubType.get(0).getNregsubtypecode(), userInfo);
			objmap.put("FilterStatus", listTransactionstatus);
			objmap.put("FilterStatusValue", listTransactionstatus.get(0));

			listApprovalConfigAutoapproval = getApprovalConfigVersion(lstRegistrationType.get(0).getNregtypecode(),
					lstRegistrationSubType.get(0).getNregsubtypecode(), userInfo);
			objmap.put("ApprovalConfigVersion", listApprovalConfigAutoapproval);
			objmap.put("ApprovalConfigVersionValue", listApprovalConfigAutoapproval.get(0));

			objmap.put("napproveconfversioncode", listApprovalConfigAutoapproval.get(0).getNapproveconfversioncode());
			// objmap.put("RealApprovalConfigVersionValue",
			// listApprovalConfigAutoapproval.get(0));

			lstReactRegistrationTemplate = (List<ReactRegistrationTemplate>) getApproveConfigVersionRegTemplate(
					lstRegistrationType.get(0).getNregtypecode(), lstRegistrationSubType.get(0).getNregsubtypecode(),
					listApprovalConfigAutoapproval.get(0).getNapproveconfversioncode(),userInfo);

//			lstReactRegistrationTemplate = (List<ReactRegistrationTemplate>) getRegistrationTemplateList(
//					lstRegistrationType.get(0).getNregtypecode(), lstRegistrationSubType.get(0).getNregsubtypecode(),
//					lstRegistrationSubType.get(0).isNneedtemplatebasedflow()).getBody();
			objmap.put("DesignTemplateMapping", lstReactRegistrationTemplate);
			if (lstReactRegistrationTemplate.size() > 0) {
				objmap.put("DesignTemplateMappingValue", lstReactRegistrationTemplate.get(0));
			}

		}

		else {
			objmap.put("RegistrationSubType", null);
			objmap.put("RegSubTypeValue", null);
			objmap.put("RegistrationType", null);
			objmap.put("RegTypeValue", null);
			objmap.put("ApprovalConfigVersion", null);
			objmap.put("ApprovalConfigVersionValue", null);
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
		lstRegistrationSubType = getRegistrationSubType(nregtypecode, userInfo);
		if (lstRegistrationSubType != null && lstRegistrationSubType.size() > 0) {
			objmap.put("RegistrationSubType", lstRegistrationSubType);
			objmap.put("RegSubTypeValue", lstRegistrationSubType.get(0));

			listTransactionstatus = getFilterStatus(nregtypecode, lstRegistrationSubType.get(0).getNregsubtypecode(),
					userInfo);

			listApprovalConfigAutoapproval = getApprovalConfigVersion(nregtypecode,
					lstRegistrationSubType.get(0).getNregsubtypecode(), userInfo);
			objmap.put("ApprovalConfigVersion", listApprovalConfigAutoapproval);
			objmap.put("ApprovalConfigVersionValue", listApprovalConfigAutoapproval.get(0));

			objmap.put("napproveconfversioncode", listApprovalConfigAutoapproval.get(0).getNapproveconfversioncode());
			// objmap.put("RealApprovalConfigVersionValue",
			// listApprovalConfigAutoapproval.get(0));

			lstReactRegistrationTemplate = (List<ReactRegistrationTemplate>) getApproveConfigVersionRegTemplate(
					nregtypecode, lstRegistrationSubType.get(0).getNregsubtypecode(),
					listApprovalConfigAutoapproval.get(0).getNapproveconfversioncode(),userInfo);

//		lstReactRegistrationTemplate = (List<ReactRegistrationTemplate>) getRegistrationTemplateList(nregtypecode,
//				lstRegistrationSubType.get(0).getNregsubtypecode(),
//				lstRegistrationSubType.get(0).isNneedtemplatebasedflow()).getBody();
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

	public ResponseEntity<Object> getRegTemplateTypeByRegSubType(Map<String, Object> inputMap) throws Exception {
		Map<String, Object> objmap = new HashMap<String, Object>();
		int nregtypecode = (int) inputMap.get("nregtypecode");
		int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
		ObjectMapper objMapper = new ObjectMapper();
		List<TransactionStatus> listTransactionstatus = new ArrayList<TransactionStatus>();
		List<ReactRegistrationTemplate> lstReactRegistrationTemplate = new ArrayList<ReactRegistrationTemplate>();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		List<ApprovalConfigAutoapproval> listApprovalConfigAutoapproval = new ArrayList<ApprovalConfigAutoapproval>();

		listApprovalConfigAutoapproval = getApprovalConfigVersion(nregtypecode, nregsubtypecode, userInfo);
		objmap.put("ApprovalConfigVersion", listApprovalConfigAutoapproval);
		objmap.put("ApprovalConfigVersionValue", listApprovalConfigAutoapproval.get(0));

		objmap.put("napproveconfversioncode", listApprovalConfigAutoapproval.get(0).getNapproveconfversioncode());
		// objmap.put("RealApprovalConfigVersionValue",
		// listApprovalConfigAutoapproval.get(0));

		listTransactionstatus = getFilterStatus(nregtypecode, nregsubtypecode, userInfo);
//		lstReactRegistrationTemplate = (List<ReactRegistrationTemplate>) getRegistrationTemplateList(nregtypecode,
//				nregsubtypecode, (boolean) inputMap.get("nneedtemplatebasedflow")).getBody();

		lstReactRegistrationTemplate = (List<ReactRegistrationTemplate>) getApproveConfigVersionRegTemplate(
				nregtypecode, nregsubtypecode, listApprovalConfigAutoapproval.get(0).getNapproveconfversioncode(),userInfo);

		objmap.put("DesignTemplateMapping", lstReactRegistrationTemplate);
		objmap.put("DesignTemplateMappingValue", lstReactRegistrationTemplate.get(0));

		objmap.put("FilterStatusValue", listTransactionstatus.get(0));
		objmap.put("FilterStatus", listTransactionstatus);
		return new ResponseEntity<Object>(objmap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getApprovalConfigBasedTemplateDesign(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> objmap = new HashMap<String, Object>();
		int nregtypecode = (int) inputMap.get("nregtypecode");
		int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
		int napproveconfversioncode = (int) inputMap.get("napproveconfversioncode");
		List<ReactRegistrationTemplate> lstReactRegistrationTemplate = new ArrayList<ReactRegistrationTemplate>();
		// List<ApprovalConfigAutoapproval> listApprovalConfigAutoapproval = new
		// ArrayList<ApprovalConfigAutoapproval>();

		lstReactRegistrationTemplate = (List<ReactRegistrationTemplate>) getApproveConfigVersionRegTemplate(
				nregtypecode, nregsubtypecode, napproveconfversioncode,userInfo);

		objmap.put("DesignTemplateMapping", lstReactRegistrationTemplate);
		if (!lstReactRegistrationTemplate.isEmpty()) {
			objmap.put("DesignTemplateMappingValue", lstReactRegistrationTemplate.get(0));
		} else {
			objmap.put("DesignTemplateMappingValue", null);
		}

		return new ResponseEntity<Object>(objmap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getRegistrationTemplate(int nregTypeCode, int nregSubTypeCode,final UserInfo userInfo) throws Exception {

		final String str = "select dm.ndesigntemplatemappingcode,rt.jsondata, rt.ndefaulttemplatecode "
				+ " from designtemplatemapping dm "
				+ " join reactregistrationtemplate rt on rt.nreactregtemplatecode=dm.nreactregtemplatecode"
				+ " where nregtypecode=" + nregTypeCode + " and nregsubtypecode=" + nregSubTypeCode + " "
				+ " and rt.nsitecode="+userInfo.getNmastersitecode()+" and " + " dm.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and rt.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and  dm.ntransactionstatus="+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus();

		final ReactRegistrationTemplate lstReactRegistrationTemplate = (ReactRegistrationTemplate) jdbcUtilityFunction
				.queryForObject(str, ReactRegistrationTemplate.class, jdbcTemplate);

		return new ResponseEntity<>(lstReactRegistrationTemplate, HttpStatus.OK);

	}

	public ResponseEntity<Object> getRegistrationTemplatebasedontemplatecode(int nregTypeCode, int nregSubTypeCode,
			int ndesignTemplateCode, final UserInfo userInfo) throws Exception {
		final String str = "select dm.ndesigntemplatemappingcode,rt.jsondata"
				+ " from designtemplatemapping dm  "
				+ " join reactregistrationtemplate rt on rt.nreactregtemplatecode=dm.nreactregtemplatecode "
				+ " where nregtypecode=" + nregTypeCode + " and nregsubtypecode="+ nregSubTypeCode + " "
				+" and dm.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and rt.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and dm.ndesigntemplatemappingcode="+ ndesignTemplateCode+" and rt.nsitecode="+userInfo.getNmastersitecode()+" ";

		final ReactRegistrationTemplate lstReactRegistrationTemplate = (ReactRegistrationTemplate) jdbcUtilityFunction
				.queryForObject(str, ReactRegistrationTemplate.class, jdbcTemplate);

		return new ResponseEntity<>(lstReactRegistrationTemplate, HttpStatus.OK);

	}

	public ResponseEntity<Object> getRegistrationParameter(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<>();

		int nstbstudyplancode = Integer.valueOf(inputMap.get("nstbstudyplancode").toString());
		int nstbtimepointcode = Integer.valueOf(inputMap.get("nstbtimepointcode").toString());

		final StbStudyPlan stbStudyPlanByID = (StbStudyPlan) getActiveStbStudyPlanById(nstbstudyplancode, userInfo);
		final StbTimePoint stbTimePointByID = (StbTimePoint) getActiveStbTimePointById(nstbtimepointcode, userInfo);

		if (stbStudyPlanByID == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (stbTimePointByID == null) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {

				String npreregno = "select * from stbschedulertransaction  stb "
						+ " join stbstudyplan stp on stp.nstbstudyplancode=stb.nstbregistrationcode "
						+ " join stbtimepoint sttp on sttp.nstbtimepointcode=stb.nstbregistrationsamplecode and stp.nstbstudyplancode=sttp.nstbstudyplancode  "
						+ " where "
						+ " nstbregistrationsamplecode="+ nstbtimepointcode + " and nstbregistrationcode=" + nstbstudyplancode	+ " and npreregno <> -1 "
						+ " and sttp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and stp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and stb.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and stb.nsitecode="+userInfo.getNtranssitecode()+" "
						+ " and stp.nsitecode ="+userInfo.getNtranssitecode()+" "
						+ " and sttp.nsitecode="+userInfo.getNtranssitecode()+" ";

				final StbSchedulerTransaction listAvailableTest = (StbSchedulerTransaction) jdbcUtilityFunction
						.queryForObject(npreregno, StbSchedulerTransaction.class, jdbcTemplate);

				if (listAvailableTest != null) {
					final String query = "select * from fn_stbregistrationparameterget ('"
							+ listAvailableTest.getNpreregno() + "', " + userInfo.getNtranssitecode() + "::integer,"
							+ "'" + userInfo.getSlanguagetypecode() + "')";
					LOGGER.info("parameter query:" + query);
					List<?> lstSp = jdbcTemplate.queryForList(query);
					returnMap.put("RegistrationParameter", lstSp);
					return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
				} else {
					returnMap.put("RegistrationParameter", new ArrayList<>());
					return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
				}
			}
		}
	}

	public ResponseEntity<Object> getTimePointHistory(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		Map<String, Object> returnMap = new HashMap<>();

		int nstbstudyplancode = Integer.valueOf(inputMap.get("nstbstudyplancode").toString());
		int nstbtimepointcode = Integer.valueOf(inputMap.get("nstbtimepointcode").toString());

		final StbStudyPlan stbStudyPlanByID = (StbStudyPlan) getActiveStbStudyPlanById(nstbstudyplancode, userInfo);
		final StbTimePoint stbTimePointByID = (StbTimePoint) getActiveStbTimePointById(nstbtimepointcode, userInfo);

		if (stbStudyPlanByID == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (stbTimePointByID == null) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {

				String npreregno = "select * from stbschedulertransaction  stb "
						+ " join stbstudyplan stp on stp.nstbstudyplancode=stb.nstbregistrationcode "
						+ " join stbtimepoint sttp on sttp.nstbtimepointcode=stb.nstbregistrationsamplecode and stp.nstbstudyplancode=sttp.nstbstudyplancode  "
						+ " where "
						+ " nstbregistrationsamplecode="+ nstbtimepointcode + " and nstbregistrationcode=" + nstbstudyplancode	+ " and npreregno <> -1 "
						+ " and sttp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and stp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and stb.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and stb.nsitecode="+userInfo.getNtranssitecode()+" "
						+ " and stp.nsitecode ="+userInfo.getNtranssitecode()+" "
						+ " and sttp.nsitecode="+userInfo.getNtranssitecode()+" ";

				final StbSchedulerTransaction listAvailableTest = (StbSchedulerTransaction) jdbcUtilityFunction
						.queryForObject(npreregno, StbSchedulerTransaction.class, jdbcTemplate);

				if (listAvailableTest != null) {
					String strQuery = "select  rh.nreghistorycode, "
							+ "case when ra.sarno='-' then  cast(ra.npreregno as character varying) else ra.sarno end as sarno, "
							+ " concat(u.sfirstname || ' ' || u.slastname) as susername, ur.suserrolename, "
							+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()+ "', ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus, "
							+ " TO_CHAR(rh.dtransactiondate, '" + userInfo.getSpgsitedatetime()	+ "') as stransactiondate "
							+ " from registration r "
							+ " join registrationarno ra on ra.npreregno=r.npreregno   "
							+ " join registrationhistory rh on rh.npreregno=r.npreregno  "
							+ " join users u on u.nusercode=rh.nusercode "
							+ " join userrole ur on  ur.nuserrolecode=rh.nuserrolecode "
							+ " join transactionstatus ts on ts.ntranscode=rh.ntransactionstatus "
							+ " and r.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
							+ " and ra.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
							+ " and rh.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ " and u.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
							+ " and ur.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
							+ " and ts.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ " where r.npreregno in ("	+ listAvailableTest.getNpreregno() + ") "
							+ " and r.nsitecode=" + userInfo.getNtranssitecode()+ " "
							+ " and ra.nsitecode=" + userInfo.getNtranssitecode()+ " "
							+ " and rh.nsitecode=" + userInfo.getNtranssitecode()+ " "
							+ " order by nreghistorycode desc";

					returnMap.put("RegistrationSampleHistory",
							jdbcTemplate.query(strQuery, new RegistrationSampleHistory()));
					return new ResponseEntity<>(returnMap, HttpStatus.OK);

				} else {
					returnMap.put("RegistrationSampleHistory", new ArrayList<>());
					return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
				}
			}
		}

	}

}
