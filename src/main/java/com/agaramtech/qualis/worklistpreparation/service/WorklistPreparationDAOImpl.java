package com.agaramtech.qualis.worklistpreparation.service;

import java.io.File;
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

import com.agaramtech.qualis.basemaster.model.TransactionStatus;
import com.agaramtech.qualis.configuration.model.ApprovalConfigAutoapproval;
import com.agaramtech.qualis.configuration.model.DesignTemplateMapping;
import com.agaramtech.qualis.configuration.model.FilterName;
import com.agaramtech.qualis.configuration.model.SampleType;
import com.agaramtech.qualis.contactmaster.model.Patient;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationSubType;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationType;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.ReportDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.organization.model.Section;
import com.agaramtech.qualis.registration.model.ApprovalParameter;
import com.agaramtech.qualis.testgroup.model.TestGroupTest;
import com.agaramtech.qualis.worklistpreparation.model.Worklist;
import com.agaramtech.qualis.worklistpreparation.model.WorklistHistory;
import com.agaramtech.qualis.worklistpreparation.model.WorklistSample;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class WorklistPreparationDAOImpl  implements WorklistPreparationDAO{
	
private static final Logger LOGGER = LoggerFactory.getLogger(WorklistPreparationDAOImpl.class);

	
	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;	
	private final ReportDAOSupport reportDAOSupport;
	
	public List<SampleType> getSampleType(final UserInfo userinfo) throws Exception {

		final String finalquery = "select st.nsampletypecode,coalesce(st.jsondata->'sampletypename'->>'"	+ userinfo.getSlanguagetypecode()
						  		+ "',st.jsondata->'sampletypename'->>'en-US') as ssampletypename,st.nsorter "
						  		+ "from SampleType st,registrationtype rt,registrationsubtype rst,approvalconfig ac,approvalconfigversion acv,regsubtypeconfigversion rsc "
						  		+ "where "
						  		+ "st.nsampletypecode=rt.nsampletypecode "
						  		+ "and rt.nregtypecode=rst.nregtypecode "
						  		+ "and rst.nregsubtypecode=ac.nregsubtypecode "
						  		+ "and acv.napprovalconfigcode=ac.napprovalconfigcode "
						  		+ "and rsc.napprovalconfigcode = ac.napprovalconfigcode "
				  				+ "and st.napprovalconfigview = "+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " "
				  				+ "and acv.ntransactionstatus= "+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " "
								+ "and (rsc.jsondata->'nneedworklist')::jsonb ='true' "
								+ "and st.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						  		+ "and rt.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						  		+ "and rst.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						  		+ "and ac.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						  		+ "and acv.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						  		+ "and rsc.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						  		+ "and st.nsampletypecode > 0  "						  	
						  		+ "group by st.nsampletypecode,st.nsorter order by st.nsorter; ";
		LOGGER.info("Get Method:"+ finalquery);
		return jdbcTemplate.query(finalquery, new SampleType());
	}

	public List<RegistrationType> getRegistrationType(final short nsampletypecode, final UserInfo userinfo) throws Exception {
		
		final String Str = "Select * from sampletype  where  nsampletypecode=" + nsampletypecode;
		final SampleType obj = (SampleType) jdbcUtilityFunction.queryForObject(Str, SampleType.class, jdbcTemplate);
		String validationQuery = "";
		if (obj.getNtransfiltertypecode() != -1 && userinfo.getNusercode() != -1) {
			int nmappingfieldcode = (obj.getNtransfiltertypecode() == 1) ? userinfo.getNdeptcode() : userinfo.getNuserrole();
			validationQuery = " and rst.nregsubtypecode in( SELECT rs.nregsubtypecode "
							+ "	FROM registrationsubtype rs "
							+ "	INNER JOIN transactionfiltertypeconfig ttc ON rs.nregsubtypecode = ttc.nregsubtypecode "
							+ "	LEFT JOIN transactionusers tu ON tu.ntransfiltertypeconfigcode = ttc.ntransfiltertypeconfigcode "
							+ "	WHERE ( ttc.nneedalluser = " + Enumeration.TransactionStatus.YES.gettransactionstatus()+ "  "
							+ " and ttc.nstatus =  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ "  "
							+ " AND ttc.nmappingfieldcode = " + nmappingfieldcode + ")  OR "
							+ "	( ttc.nneedalluser = " + Enumeration.TransactionStatus.NO.gettransactionstatus() + "  "
							+ "	AND ttc.nmappingfieldcode =" + nmappingfieldcode + " AND tu.nusercode ="+ userinfo.getNusercode() + " "
							+ " and ttc.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ " and tu.nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") OR "
							+ "	( ttc.nneedalluser = " + Enumeration.TransactionStatus.NO.gettransactionstatus() + "  "
							+ " AND ttc.nmappingfieldcode = "+Enumeration.TransactionStatus.NA.gettransactionstatus()+" AND tu.nusercode =" + userinfo.getNusercode()+ "  "
							+ " and ttc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ "  "
							+ " and tu.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") "
							+ "	AND rs.nregtypecode = rt.nregtypecode) ";
		}	

		final String finalquery = "select rt.nregtypecode,coalesce(rt.jsondata->'sregtypename'->>'"
						  		+ userinfo.getSlanguagetypecode() + "',rt.jsondata->'sregtypename'->>'en-US')  as sregtypename "
						  		+ "from approvalconfig ac,approvalconfigversion acv,registrationtype rt,registrationsubtype rst,regsubtypeconfigversion rsc "
						  		+ "where "
						  		+ "ac.napprovalconfigcode = acv.napprovalconfigcode "
						  		+ "and rt.nregtypecode = ac.nregtypecode "
						  		+ "and rt.nregtypecode = rst.nregtypecode "
						  		+ "and rst.nregsubtypecode = ac.nregsubtypecode "
						  		+ "and rsc.napprovalconfigcode = ac.napprovalconfigcode "
						  		+ "and acv.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+ " "
						  		+ "and rsc.ntransactionstatus="+Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+" "
						  		+ "and (rsc.jsondata->'nneedworklist')::jsonb ='true' "
						  		+ "and ac.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						  		+ "and acv.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						  		+ "and rt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						  		+ "and rst.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						  		+ "and rsc.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						  		+ "and acv.nsitecode = " + userinfo.getNmastersitecode() + " and rt.nsitecode="+userinfo.getNmastersitecode()+" "
						  		+ "and rt.nsampletypecode = "	+ nsampletypecode + " " 
						  		+ "and rt.nregtypecode > 0 "+validationQuery+"  group by rt.nregtypecode,sregtypename  order by rt.nregtypecode desc ";
		return jdbcTemplate.query(finalquery, new RegistrationType());
	}

	public ResponseEntity<Object> getWorklistPreparation(final UserInfo userInfo,final String currentUIDate) throws Exception {
		final Map<String, Object> returnMap = new HashMap<>();
		final ObjectMapper objMapper = new ObjectMapper();
		String dfromdate = "";
		String dtodate = "";
		
		//Ate234 janakumar ALPD-5000 Work List -> To get previously saved filter details when click the filter name
		final String strQueryObj= " select json_agg(jsondata || jsontempdata) as jsondata from filterdetail where nformcode="+userInfo.getNformcode()+" and nusercode="+userInfo.getNusercode()+" "
        						+ " and nuserrolecode="+userInfo.getNuserrole()+" and nsitecode="+userInfo.getNtranssitecode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
    	
		final String strFilter = jdbcTemplate.queryForObject(strQueryObj, String.class);
    	
		final List<Map<String, Object>> lstfilterDetail = strFilter != null ? objMapper.readValue(strFilter, new TypeReference<List<Map<String, Object>>>() {})
																			:new ArrayList<Map<String, Object>>();
		
		 if(!lstfilterDetail.isEmpty() && lstfilterDetail.get(0).containsKey("FromDate") && lstfilterDetail.get(0).containsKey("ToDate") ){
				Instant instantFromDate = dateUtilityFunction.convertStringDateToUTC(lstfilterDetail.get(0).get("FromDate").toString(),userInfo, true);
				Instant instantToDate = dateUtilityFunction.convertStringDateToUTC(lstfilterDetail.get(0).get("ToDate").toString(),userInfo, true);
			
				dfromdate = dateUtilityFunction.instantDateToString(instantFromDate);
				dtodate =dateUtilityFunction.instantDateToString(instantToDate);

				returnMap.put("FromDate", dfromdate);
				returnMap.put("ToDate", dtodate);
				returnMap.put("fromdateREG", dfromdate);
				returnMap.put("todateREG", dtodate);
				
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
				
				returnMap.put("realFromDate", formattedFromString);
				returnMap.put("realToDate", formattedToString);
				
				returnMap.put("fromdate", formattedFromString);
				returnMap.put("todate", formattedToString);
			}else {				
				final Map<String, Object> mapObject = projectDAOSupport.getDateFromControlProperties(userInfo, currentUIDate, "datetime",	"FromDate");
				dfromdate = (String) mapObject.get("FromDate");
				dtodate= (String) mapObject.get("ToDate");
				returnMap.put("fromDate", (String) mapObject.get("FromDateWOUTC"));
				returnMap.put("toDate", (String) mapObject.get("ToDateWOUTC"));
				returnMap.put("fromdate", (String) mapObject.get("FromDateWOUTC"));
				returnMap.put("todate", (String) mapObject.get("ToDateWOUTC"));
				returnMap.put("fromdateREG", dfromdate);
				returnMap.put("todateREG", dtodate);			 
			}

		final Map<String, Object> map = new HashMap<String, Object>();
		Worklist selectedWorklist = null;
		List<RegistrationType> lstRegistrationType = new ArrayList<>();
		List<RegistrationSubType> lstRegistrationSubType = new ArrayList<>();
		List<TransactionStatus> listTransactionstatus = new ArrayList<>();
		final List<SampleType> lstSampleType = getSampleType(userInfo);
		if (!lstSampleType.isEmpty()) {			
			final List<FilterName> lstFilterName=getFilterName(userInfo);			
			final SampleType filterSampleType=!lstfilterDetail.isEmpty()?
					objMapper.convertValue(lstfilterDetail.get(0).get("sampleTypeValue"),SampleType.class):lstSampleType.get(0);

			
			final short nsampletypecode = filterSampleType.getNsampletypecode();
			returnMap.put("SampleTypeValue", filterSampleType);
			returnMap.put("RealSampleTypeValue", filterSampleType);
			returnMap.put("defaultSampleTypeValue", filterSampleType);
			returnMap.put("SampleType", lstSampleType);
			returnMap.put("realSampleTypeList", lstSampleType);
		
			lstRegistrationType = getRegistrationType(nsampletypecode, userInfo);
			
			if(!lstRegistrationType.isEmpty()) {
				final RegistrationType filterRegistrationType=!lstfilterDetail.isEmpty()?objMapper.convertValue(lstfilterDetail.get(0).get("regTypeValue"),RegistrationType.class):lstRegistrationType.get(0);

				lstRegistrationSubType = getRegistrationSubType(filterRegistrationType.getNregtypecode(), userInfo);
				
				final RegistrationSubType filterRegistrationSubType=!lstfilterDetail.isEmpty()?objMapper.convertValue(lstfilterDetail.get(0).get("regSubTypeValue"),RegistrationSubType.class):lstRegistrationSubType.get(0);

				returnMap.put("RegTypeValue", filterRegistrationType);
				returnMap.put("RegistrationType", lstRegistrationType);
				returnMap.put("realRegistrationTypeList", lstRegistrationType);
				returnMap.put("defaultRegTypeValue", filterRegistrationType);
				returnMap.put("realRegTypeValue", filterRegistrationType);

				returnMap.put("RegSubTypeValue", filterRegistrationSubType);
				returnMap.put("RegistrationSubType", lstRegistrationSubType);
				returnMap.put("realRegistrationSubTypeList", lstRegistrationSubType);
				returnMap.put("defaultRegSubTypeValue", filterRegistrationSubType);
				returnMap.put("realRegSubTypeValue", filterRegistrationSubType);
				
				listTransactionstatus = getFilterStatus(filterRegistrationType.getNregtypecode(),filterRegistrationSubType.getNregsubtypecode(), userInfo);
				
				final TransactionStatus filterTransactionStatus=!lstfilterDetail.isEmpty()?
						lstfilterDetail.get(0).get("filterStatusValue")!=null?
						objMapper.convertValue(lstfilterDetail.get(0).get("filterStatusValue"),TransactionStatus.class)
						:listTransactionstatus.get(0)
						:listTransactionstatus.get(0);
				returnMap.put("FilterStatusValue", filterTransactionStatus);
				returnMap.put("nfilterstatus", filterTransactionStatus.getNtransactionstatus());
				returnMap.put("RealFilterStatusValue", filterTransactionStatus);
				returnMap.put("defaultFilterStatusValue", filterTransactionStatus);
				returnMap.put("FilterStatus", listTransactionstatus);
				returnMap.put("realFilterStatusList", listTransactionstatus);
				returnMap.put("nregtypecode", filterRegistrationType.getNregtypecode());
				returnMap.put("nregsubtypecode", filterRegistrationSubType.getNregsubtypecode());
				returnMap.put("nneedtemplatebasedflow", filterRegistrationSubType.isNneedtemplatebasedflow());
				
				
				returnMap.put("filterDetailValue", lstfilterDetail);
				returnMap.putAll((Map<String, Object>) getApprovalConfigVersion(returnMap, userInfo));

				returnMap.put("FilterName",lstFilterName);
				
				final String fromdate = dateUtilityFunction.instantDateToString(dateUtilityFunction.convertStringDateToUTC(dfromdate, userInfo, true));
				final String todate = dateUtilityFunction.instantDateToString(dateUtilityFunction.convertStringDateToUTC(dtodate, userInfo, true));
				map.put("fromDate", fromdate);
				map.put("toDate", todate);

				final String transcodeList;
				returnMap.put("FilterName",lstFilterName);
				
				if(filterTransactionStatus.getNtransactionstatus()==0) {
					transcodeList = listTransactionstatus.stream().map(transItem -> String.valueOf(transItem.getNtransactionstatus()))
							.collect(Collectors.joining(","));
				}else {
					transcodeList=String.valueOf(filterTransactionStatus.getNtransactionstatus());
				}
				String strQuery = " select wlh.nworklistcode ,wlh.sworklistno,wl.ntestcode,tm.stestname,wl.nsampletypecode,"
								+ " wl.nregtypecode,wl.nregsubtypecode,wl.nsectioncode,s.ssectionname,wlh.ntransactionstatus, "
								+ " ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "' stransdisplaystatus "
								+ " from worklist wl,worklisthistory wlh,testmaster tm,section s,transactionstatus ts "
								+ " where "
								+ " wl.nworklistcode=wlh.nworklistcode"
								+ " and tm.ntestcode=wl.ntestcode "
								+ " and wl.nsectioncode=s.nsectioncode "
								+ " and wlh.ntransactionstatus=ts.ntranscode "
								+ " and wlh.nworkhistorycode in(select max(nworkhistorycode) from worklisthistory where nsitecode ="+userInfo.getNtranssitecode()+" group by  nworklistcode ) "
								+ " and wl.nsampletypecode="	+ filterSampleType.getNsampletypecode() +  " "
								+ " and wl.nregtypecode="+ filterRegistrationType.getNregtypecode() + " "
								+ " and wl.nregsubtypecode="+ filterRegistrationSubType.getNregsubtypecode() + " "
								+ " and wlh.ntransactionstatus in ("+ transcodeList	+ " ) "
								+ " and wl.napprovalversioncode="+returnMap.get("napprovalversioncode")+ " "
								+ " and wlh.dtransactiondate between '" + fromdate + "' and '" + todate + "' "
								+ " and wl.nsitecode=" + userInfo.getNtranssitecode()+" "
								+ " and wlh.nsitecode=" + userInfo.getNtranssitecode()+" "							
								+ " and wl.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								+ " and wlh.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								+ " and tm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								+ " and s.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								+ " and ts.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +"  "
								+ " order by wl.nworklistcode desc ";
				final List<Worklist> lstInstGet = jdbcTemplate.query(strQuery, new Worklist());			
				returnMap.put("Worklist", lstInstGet);
				if (!lstInstGet.isEmpty()) {
					selectedWorklist = (Worklist) lstInstGet.get(0);
					returnMap.put("selectedWorklist", selectedWorklist);				
					returnMap.putAll((Map<String, Object>) WorklistSampleAndHistory(selectedWorklist.getNworklistcode(),(Integer) returnMap.get("ndesigntemplatemappingcode"), userInfo));
				}
				
			}else {
				returnMap.put("realRegTypeValue", lstRegistrationType);
				returnMap.put("defaultRegTypeValue", lstRegistrationType);
				
			}
		}
		return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
	}

	public List<RegistrationSubType> getRegistrationSubType(final short nregtypecode,final UserInfo userinfo) throws Exception {

		final String Str = "Select * from registrationtype rt,sampletype st where rt.nsampletypecode=st.nsampletypecode and rt.nregTypeCode=" + nregtypecode;

		final SampleType obj = (SampleType) jdbcUtilityFunction.queryForObject(Str, SampleType.class, jdbcTemplate);
		String validationQuery = "";
		if (obj.getNtransfiltertypecode() != -1 && userinfo.getNusercode() != -1) {
			int nmappingfieldcode = (obj.getNtransfiltertypecode() == 1) ? userinfo.getNdeptcode() : userinfo.getNuserrole();
			validationQuery = " and rst.nregsubtypecode in( SELECT rs.nregsubtypecode "
							+ " FROM registrationsubtype rs "
							+ " INNER JOIN transactionfiltertypeconfig ttc ON rs.nregsubtypecode = ttc.nregsubtypecode "
							+ " LEFT JOIN transactionusers tu ON tu.ntransfiltertypeconfigcode = ttc.ntransfiltertypeconfigcode "
							+ " WHERE ( ttc.nneedalluser = " + Enumeration.TransactionStatus.YES.gettransactionstatus()
							+ " and ttc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " AND ttc.nmappingfieldcode = " + nmappingfieldcode + ") OR "
							+ "	( ttc.nneedalluser = "+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " "
							+ " AND ttc.nmappingfieldcode ="+ nmappingfieldcode + " "
							+ " AND tu.nusercode =" + userinfo.getNusercode() + " "
							+ " and ttc.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ " and tu.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")  OR "
							+ "	( ttc.nneedalluser = " + Enumeration.TransactionStatus.NO.gettransactionstatus() + "  "
							+ " AND tu.nusercode =" + userinfo.getNusercode() + ""
							+ " and ttc.nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ " and tu.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") "
							+ " AND rs.nregtypecode = "	+nregtypecode + ")";
		}
		
		final String finalquery = "select max(rsc.nregsubtypeversioncode) nregsubtypeversioncode,"
								+ "max(rsc.jsondata->>'nneedsubsample' )::Boolean nneedsubsample, max(rsc.jsondata->>'nneedtemplatebasedflow' )::Boolean nneedtemplatebasedflow,"
								+ "rst.nregsubtypecode,coalesce(rst.jsondata->'sregsubtypename'->>'" + userinfo.getSlanguagetypecode()+ "', rst.jsondata->'sregsubtypename'->>'en-US') as sregsubtypename,rst.nsorter "
								+ "from SampleType st,registrationtype rt,registrationsubtype rst,approvalconfig ac,approvalconfigversion acv,regsubtypeconfigversion rsc "
								+ "where "
								+ "st.nsampletypecode=rt.nsampletypecode "
								+ "and rt.nregtypecode=rst.nregtypecode "
								+ "and rst.nregsubtypecode=ac.nregsubtypecode "
								+ "and acv.napprovalconfigcode=ac.napprovalconfigcode "
								+ "and rsc.napprovalconfigcode=ac.napprovalconfigcode "
								+ "and st.napprovalconfigview = "+Enumeration.TransactionStatus.YES.gettransactionstatus()+" "
								+ "and acv.ntransactionstatus = "+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+" "
								+ "and rsc.ntransactionstatus= "+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+" "
								+ "and (rsc.jsondata->'nneedworklist')::jsonb ='true'"
								+ "and st.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								+ "and rt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
								+ "and rst.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								+ "and ac.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								+ "and rsc.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								+ "and acv.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								+ "and  rt.nregtypecode = " + nregtypecode+" "
								+ "and rst.nsitecode ="+userinfo.getNmastersitecode()+" and rsc.nsitecode="+userinfo.getNmastersitecode()+" " 
								+ "and st.nsampletypecode > 0 "
								+ "and rst.nregsubtypecode > 0  "+validationQuery+" "
								+ "group by rst.nregsubtypecode,sregsubtypename,rst.nsorter order by rst.nregsubtypecode desc";
		return jdbcTemplate.query(finalquery, new RegistrationSubType());
	}

	public List<TransactionStatus> getFilterStatus(final short nregtypecode,final short nregsubtypecode,final UserInfo userinfo)throws Exception {
		final String filterStatus = "select ntranscode as ntransactionstatus,jsondata->'stransdisplaystatus'->>'"
				+ userinfo.getSlanguagetypecode() + "' stransdisplaystatus"
				+ "  from transactionstatus where ntranscode in(select approvalstatusconfig.ntranscode from approvalstatusconfig where approvalstatusconfig.nformcode="
				+ userinfo.getNformcode() + " and approvalstatusconfig.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ) and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" order by ntranscode";
		return jdbcTemplate.query(filterStatus, new TransactionStatus());
	}

	@Override
	public ResponseEntity<Object> getSectionAndTest(final UserInfo userInfo,final String currentUIDate,final Map<String, Object> inputMap) throws Exception {

		final String getValidationStatus = " select transactionvalidation.ntransactionstatus from transactionvalidation where "
										 + " transactionvalidation.nregtypecode=" + inputMap.get("nregtypecode")
										 + " and transactionvalidation.nregsubtypecode=" + inputMap.get("nregsubtypecode")
										 + " and transactionvalidation.ncontrolcode=" + inputMap.get("ncontrolCode")
										 + " and transactionvalidation.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<TransactionStatus> validationStatusList = jdbcTemplate.query(getValidationStatus,new TransactionStatus());
		
		final String sectionStatus = validationStatusList.stream().map(objpreregno -> String.valueOf(objpreregno.getNtransactionstatus()))
									 .collect(Collectors.joining(","));	
		
		final String qry = " select DISTINCT s.nsectioncode,s.ssectionname from registrationtesthistory rht,registrationtest  rt,section s,registration r "
						 + " where "
						 + " rht.ntransactiontestcode=rt.ntransactiontestcode "
						 + " and rt.npreregno=r.npreregno "
						 + " and rt.nsectioncode=s.nsectioncode "
						 + " and r.nisiqcmaterial="+Enumeration.TransactionStatus.NO.gettransactionstatus()+" "
						 + " and rht.ntransactionstatus in(" + sectionStatus + ") "
						 + " and r.nregtypecode=" + inputMap.get("nregtypecode") + " "
						 + " and r.nregsubtypecode=" + inputMap.get("nregsubtypecode")+ " "
						 + " and  rht.ntesthistorycode =any(  select max(ntesthistorycode) from registrationtesthistory where nsitecode="+userInfo.getNtranssitecode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" group by ntransactiontestcode) "
						 + " and rt.nsectioncode =any (select nsectioncode from labsection where labsection.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and labsection.nlabsectioncode =any "
						 + " (select DISTINCT nlabsectioncode from sectionusers where sectionusers.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and sectionusers.nusercode= "
						 + userInfo.getNusercode() + "))"
						 + " and rht.nsitecode ="+userInfo.getNtranssitecode()+" "
						 + " and rt.nsitecode ="+userInfo.getNtranssitecode()+" "
						 + " and r.nsitecode ="+userInfo.getNtranssitecode()+" "
						 + " and rht.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  "
						 + " and rt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						 + " and s.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						 + " and r.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";

		return new ResponseEntity<Object>((List<Section>) jdbcTemplate.query(qry, new Section()), HttpStatus.OK);
	}

	public List<Section> getSection(final UserInfo userinfo) throws Exception {
		
		final String userSectionQuery = "select nsectioncode,ssectionname from section where nstatus="
									 + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="+userinfo.getNmastersitecode()+" and nsectioncode = any ("
									 + " select ls.nsectioncode from sectionusers su,labsection ls " + " where su.nusercode ="
									 + userinfo.getNusercode() + " and ls.nlabsectioncode=su.nlabsectioncode" + "    and su.nsitecode="
									 + userinfo.getNtranssitecode() + " and su.nstatus="
									 + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ls.nstatus="
									 + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by ls.nsectioncode)";
		return jdbcTemplate.query(userSectionQuery, new Section());
	}

	public ResponseEntity<Object> createWorklist(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final Worklist inst = objMapper.convertValue(inputMap.get("worklist"), Worklist.class);
		
		final String getValidationStatus = " select transactionvalidation.ntransactionstatus from transactionvalidation where "
										 + " transactionvalidation.nregtypecode=" + inst.getNregtypecode()
										 + " and transactionvalidation.nregsubtypecode=" + inst.getNregsubtypecode()
										 + " and transactionvalidation.ncontrolcode=" + inputMap.get("ncontrolCode")
										 + " and transactionvalidation.nstatus = "
										 + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<TransactionStatus> validationStatusList = jdbcTemplate.query(getValidationStatus,new TransactionStatus());
		final String testStatus = validationStatusList.stream().map(objpreregno -> String.valueOf(objpreregno.getNtransactionstatus()))
								  .collect(Collectors.joining(","));
		
		
		final String queryString = " select ra.npreregno,ra.sarno,rsa.ntransactionsamplecode,rsa.ssamplearno,rt.ntransactiontestcode,rt.jsondata->>'stestsynonym' stestname, "
								 + " case when rs.jsonuidata->>'scomponentname'  is null then rs.jsonuidata->>'Sample Name_child' else rs.jsonuidata->>'scomponentname'   end ssamplename "
								 + " from registration r,registrationtesthistory rth, registrationarno ra, registrationsamplearno rsa, registrationsample rs,registrationtest rt "
								 + " where "
								 + " rth.ntransactiontestcode = rt.ntransactiontestcode "
								 + " and rt.ntransactionsamplecode = rs.ntransactionsamplecode "
								 + " and ra.npreregno = rsa.npreregno "
								 + " and rth.npreregno = ra.npreregno "
								 + " and rsa.ntransactionsamplecode=rs.ntransactionsamplecode "
								 + " and rth.ntransactiontestcode = rt.ntransactiontestcode "
								 + " and r.npreregno=ra.npreregno "
								 + " and rth.ntesthistorycode in (select max(ntesthistorycode) from registrationtesthistory where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNtranssitecode()+" group by npreregno,ntransactionsamplecode,ntransactiontestcode) "
								 + " and r.nsampletypecode="+ inst.getNsampletypecode()+" "
								 + " and r.nregtypecode=" + inst.getNregtypecode()+ ""
								 + " and r.nregsubtypecode=" + inst.getNregsubtypecode() + ""
								 + " and (r.jsonuidata->'napproveconfversioncode')::int=" + inputMap.get("napprovalconfigversioncode")+" "
								 + " and rth.ntransactionstatus in( "+ testStatus + ") "
								 + " and rt.nsectioncode= " + inst.getNsectioncode() + " "
								 + " and rt.ntestcode = " + inst.getNtestcode() + " "
								 + " and r.nisiqcmaterial="+Enumeration.TransactionStatus.NO.gettransactionstatus()+"  "
								 + " and r.nsitecode="+userInfo.getNtranssitecode()+" "
								 + " and rth.nsitecode = "+userInfo.getNtranssitecode()+ "  " 
								 + " and ra.nsitecode= "+userInfo.getNtranssitecode()+ "  "
								 + " and rsa.nsitecode= "+userInfo.getNtranssitecode()+ "  "
								 + " and rs.nsitecode = "+userInfo.getNtranssitecode()+ "  "
								 + " and rt.nsitecode= "+userInfo.getNtranssitecode()+ "  "
								 + " and r.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ "  "
								 + " and rth.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ "  "
								 + " and ra.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ "  "
								 + " and rsa.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ "  "
								 + " and rs.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ "  "
								 + " and rt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
								 + " and rt.ntransactiontestcode not in( "
								 + " select ws.ntransactiontestcode from worklistsample ws,worklisthistory wh where ws.nworklistcode=wh.nworklistcode "
								 + " and ws.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								 + " and wh.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" "
								 + " and ws.nsitecode ="+userInfo.getNtranssitecode()+" and wh.nsitecode="+userInfo.getNtranssitecode()+" "
								 + " and wh.ntransactionstatus=" + Enumeration.TransactionStatus.PREPARED.gettransactionstatus() + ") ";
		final List<WorklistSample> list = (List<WorklistSample>) jdbcTemplate.query(queryString, new WorklistSample());
		
		if(list.size()>0) {		
			final String sQuery = " lock table lockworklist " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus() + "";
			jdbcTemplate.execute(sQuery);
			final String fromDate = dateUtilityFunction.instantDateToString(dateUtilityFunction.convertStringDateToUTC((String) inputMap.get("fromdate"), userInfo, true));
			final String toDate = dateUtilityFunction.instantDateToString(dateUtilityFunction.convertStringDateToUTC((String) inputMap.get("todate"), userInfo, true));
			final String worklistSeq = "select nsequenceno from seqnoregistration where stablename='worklist' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";
			int seqNo = jdbcTemplate.queryForObject(worklistSeq, Integer.class);
			seqNo = seqNo + 1;
			final String Qry = "Insert into worklist (nworklistcode,nsampletypecode,nregtypecode,nregsubtypecode,ntestcode,nsectioncode,napprovalversioncode,nsitecode,nstatus ) values"
							+ "(" + seqNo + "," + inst.getNsampletypecode() + "," + inst.getNregtypecode() + ","
							+ inst.getNregsubtypecode() + "," + inst.getNtestcode() + "," + inst.getNsectioncode() + ","
							+inputMap.get("napprovalconfigversioncode") +","
							+ userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ ")";
			jdbcTemplate.execute(Qry);

			final String UpQry = "update seqnoregistration set nsequenceno=" + seqNo + " where stablename='worklist' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";
			jdbcTemplate.execute(UpQry);

			final String worklistHistorySeq = "select nsequenceno from seqnoregistration where stablename='worklisthistory' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";
			int worklistseqNo = jdbcTemplate.queryForObject(worklistHistorySeq, Integer.class);
			worklistseqNo = worklistseqNo + 1;
			final String strWorkListHistory = "Insert into worklisthistory(nworkhistorycode, nworklistcode, sworklistno,ntransactionstatus, dtransactiondate, nusercode, "
											+ " nuserrolecode, ndeputyusercode, ndeputyuserrolecode, scomments, nstatus,noffsetdtransactiondate,ntransdatetimezonecode,nsitecode) values "
											+ "(" + worklistseqNo + "," + seqNo + ",'-',"
											+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo)
											+ "'," + userInfo.getNusercode() + "," + userInfo.getNuserrole() + "," + userInfo.getNdeputyusercode()
											+ "," + userInfo.getNdeputyuserrole() + ",'" + stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "',"
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
											+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode() + ","
											+ userInfo.getNtranssitecode() + ")";
			jdbcTemplate.execute(strWorkListHistory);

			final String HityQry = "update seqnoregistration set nsequenceno=" + worklistseqNo + " where stablename='worklisthistory' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";
			jdbcTemplate.execute(HityQry);
			final Map<String, Object> responseMap = new HashMap<String, Object>();

		
			final String strQuery = " select wlh.nworklistcode ,wlh.sworklistno,wl.ntestcode,tm.stestname,wl.nsampletypecode,wl.nregtypecode,wl.nregsubtypecode,wl.nsectioncode,s.ssectionname,wlh.ntransactionstatus, "
								  + " ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()  + "' stransdisplaystatus "
								  + " from worklist wl,worklisthistory wlh,testmaster tm,section s,transactionstatus ts "
								  + " where "
								  + " wl.nworklistcode=wlh.nworklistcode "
								  + " and wlh.ntransactionstatus=ts.ntranscode "
								  + " and tm.ntestcode=wl.ntestcode "
								  + " and wl.nsectioncode=s.nsectioncode "
							  	  + " and wlh.dtransactiondate between '" + fromDate + "' and '" + toDate + "'"
								  + " and wl.nsampletypecode=" + inst.getNsampletypecode() + " "
								  + " and wl.nregtypecode=" + inst.getNregtypecode()
								  + " and wl.nregsubtypecode=" + inst.getNregsubtypecode() + ""
								  + " and wl.napprovalversioncode="+inputMap.get("napprovalconfigversioncode")
								  + " and wlh.ntransactionstatus in( "+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus()+ ") "
								  + " and wl.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								  + " and wlh.nstatus="  + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								  + " and tm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								  + " and s.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								  + " and wl.nsitecode="+userInfo.getNtranssitecode()+" and wlh.nsitecode=" + userInfo.getNtranssitecode()
								  + " and wlh.nworkhistorycode ="+worklistseqNo+" and wl.nworklistcode="+seqNo+"  order by wl.nworklistcode desc";

			final List<Worklist> lstInstGet = jdbcTemplate.query(strQuery, new Worklist());
			responseMap.put("selectedWorklist", lstInstGet.get(0));

			final Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
			final JSONObject actionType = new JSONObject();
			final JSONObject jsonAuditObject = new JSONObject();
			auditmap.put("nregtypecode", inst.getNregtypecode());
			auditmap.put("nregsubtypecode", inst.getNregsubtypecode());
			auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
			actionType.put("worklist", "IDS_ADDWORKLIST");
			jsonAuditObject.put("worklist", Arrays.asList(lstInstGet.get(0)));
			auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);
			final String historyqry = "select CONCAT(ur.sfirstname ,' ',ur.slastname) as username,urr.suserrolename,ts.jsondata->'stransdisplaystatus'->>'"+ userInfo.getSlanguagetypecode() + "' stransdisplaystatus,"
									+ "to_char(wlt.dtransactiondate, '"	+ userInfo.getSpgsitedatetime().replace("'T'", " ")	+ "') as stransactiondate,wlt.ntransdatetimezonecode,wlt.noffsetdtransactiondate "
									+ "from worklisthistory wlt,users ur,userrole urr,transactionstatus ts "
									+ "where ur.nusercode=wlt.nusercode and urr.nuserrolecode=wlt.nuserrolecode and ts.ntranscode=wlt.ntransactionstatus "
									+ "and wlt.nsitecode=" + userInfo.getNtranssitecode() + " "
									+ "and wlt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
									+ "and ur.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
									+ "and urr.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
									+ "and ts.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
									+ "and wlt.nworklistcode="+ lstInstGet.get(0).getNworklistcode();
	
			final List<WorklistHistory> lstWorklistHistoryGet = (List<WorklistHistory>) jdbcTemplate.query(historyqry,new WorklistHistory());
			final List<WorklistHistory> lstUTCConvertedDate = objMapper.convertValue(dateUtilityFunction.getSiteLocalTimeFromUTC(lstWorklistHistoryGet, Arrays.asList("stransactiondate"),
							Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null, false),new TypeReference<List<WorklistHistory>>() {});
	
			responseMap.put("WorklistHistory", lstUTCConvertedDate);
			responseMap.put("WorklistSamples", "");
			return new ResponseEntity<>(responseMap, HttpStatus.OK);
		}
		else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NOSAMPLEFORTEST", userInfo.getSlanguagefilename()),	HttpStatus.EXPECTATION_FAILED);
		}
	}

	public ResponseEntity<Object> refreshGetForAddComponent(final Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		final String strQuery=" select ntransactionstatus from worklisthistory where  "
							+ " nworkhistorycode=(select max(nworkhistorycode) from worklisthistory where nworklistcode="+inputMap.get("nworklistcode")+""
							+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode ="+userInfo.getNtranssitecode()+" )"
							+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode ="+userInfo.getNtranssitecode()+" ";
		
		final Integer ntransactionstatus = jdbcTemplate.queryForObject(strQuery, Integer.class);
		
		if (ntransactionstatus != Enumeration.TransactionStatus.PREPARED.gettransactionstatus()) {

			final String getValidationStatus = " select transactionvalidation.ntransactionstatus from transactionvalidation where "
											 + " transactionvalidation.nregtypecode=" + inputMap.get("nregtypecode")
											 + " and transactionvalidation.nregsubtypecode=" + inputMap.get("nregsubtypecode")
											 + " and transactionvalidation.ncontrolcode=" + inputMap.get("ncontrolCode")
											 + " and transactionvalidation.nstatus = "
											 + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final List<TransactionStatus> validationStatusList = jdbcTemplate.query(getValidationStatus,new TransactionStatus());
			final String testStatus = validationStatusList.stream().map(objpreregno -> String.valueOf(objpreregno.getNtransactionstatus()))
									  .collect(Collectors.joining(","));

			

			final String queryString= " select ra.npreregno,ra.sarno,rsa.ntransactionsamplecode,rsa.ssamplearno,rt.ntransactiontestcode,rt.jsondata->>'stestsynonym' stestname, "
									+ " case when rs.jsonuidata->>'scomponentname'  is null then rs.jsonuidata->>'Sample Name_child' else rs.jsonuidata->>'scomponentname' end ssamplename , "
									+ " rd.sregistereddate "
									+ " from  registration r "
									+ " join registrationarno ra on r.npreregno=ra.npreregno "
									+ " join registrationsample rs on  r.npreregno=rs.npreregno "
									+ " join registrationsamplearno rsa on r.npreregno=rsa.npreregno and  rsa.ntransactionsamplecode=rs.ntransactionsamplecode "
									+ " join registrationtest rt on  r.npreregno=rt.npreregno and rs.ntransactionsamplecode = rt.ntransactionsamplecode "
									+ " and rt.ntestcode = "+inputMap.get("ntestcode")+"  and rt.nsectioncode= "+inputMap.get("nsectioncode")+"  "
									+ " and rt.ntransactiontestcode not in(select worklistsample.ntransactiontestcode from worklistsample where nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"   "
									+ " and nsitecode ="+userInfo.getNtranssitecode()+" and worklistsample.nworklistcode="+inputMap.get("nworklistcode")+") "
									+ " and rt.ntransactiontestcode not in(select ws.ntransactiontestcode from worklistsample ws,"
									+ " worklisthistory wh where ws.nworklistcode=wh.nworklistcode and ws.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
									+ " and wh.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ws.nsitecode="+userInfo.getNtranssitecode()+" and wh.nsitecode="+userInfo.getNtranssitecode()+" and wh.ntransactionstatus="+Enumeration.TransactionStatus.PREPARED.gettransactionstatus()+") "
									+ " join registrationtesthistory rth on  r.npreregno=rth.npreregno and rt.ntransactiontestcode = rth.ntransactiontestcode "
									+ " and r.nsitecode=rth.nsitecode "
									+ " and rth.ntesthistorycode =any (select max(ntesthistorycode)  from registrationtesthistory where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode ="+userInfo.getNtranssitecode()+" group by npreregno,ntransactionsamplecode,ntransactiontestcode)"
									+ " and rth.ntransactionstatus in("+testStatus+" )"
									+ " join (SELECT TO_CHAR(rhasd.dtransactiondate, 'dd/MM/yyyy HH:mm:ss') AS sregistereddate,rdfg.npreregno FROM registration rdfg  "
									+ " JOIN registrationhistory rhasd ON rdfg.npreregno = rhasd.npreregno "
									+ " AND rdfg.nsitecode = "+userInfo.getNtranssitecode()+" and  rhasd.nsitecode="+userInfo.getNtranssitecode()+" "
									+ " and rdfg.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and rhasd.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
									+ " WHERE rhasd.ntransactionstatus = "+Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()+" ) rd "
									+ " on rd.npreregno=r.npreregno "
									+ " where r.nisiqcmaterial="+Enumeration.TransactionStatus.NO.gettransactionstatus()+" "
									+ " and r.nsitecode="+userInfo.getNtranssitecode()+" "
									+ " and ra.nsitecode= "+userInfo.getNtranssitecode()+ "  "
									+ " and rs.nsitecode= "+userInfo.getNtranssitecode()+ "  "
									+ " and rsa.nsitecode= "+userInfo.getNtranssitecode()+ "  "
									+ " and rt.nsitecode= "+userInfo.getNtranssitecode()+ "  "
									+ " and rth.nsitecode= "+userInfo.getNtranssitecode()+ "  "
									+ " and r.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ "  "
									+ " and ra.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ "  "
									+ " and rs.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ "  "
									+ " and rsa.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ "  "
									+ " and rt.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ "  "
									+ " and rth.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ "  " 		
									+ " and r.nsampletypecode="+inputMap.get("nsampletypecode")+""
									+ " and r.nregtypecode="+inputMap.get("nregtypecode")+""
									+ " and r.nregsubtypecode="+inputMap.get("nregsubtypecode")
									+ " and (r.jsonuidata->'napproveconfversioncode')::int= "+inputMap.get("napprovalconfigversioncode")
									+ " order by sregistereddate asc ";			

			final List<Map<String,Object>> list = jdbcTemplate.queryForList(queryString);
			return new ResponseEntity<>(list, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ALREADYPREPARED", userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
		}
	}

	public ResponseEntity<Object> createWorklistCreation(final Integer seqNo,final List<WorklistSample> batchCompCreationList,
			final Worklist batchCreation, final UserInfo userInfo,final Integer ndesigntemplatemappingcode) throws Exception {
		final ObjectMapper mapper = new ObjectMapper();

		final String sQuery = " lock table lockworklistsample " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus() + "";
		jdbcTemplate.execute(sQuery);
		
		final String worklistSeq = "select nsequenceno from seqnoregistration where stablename='worklistsample' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";
		int nworklistsamplecode = jdbcTemplate.queryForObject(worklistSeq, Integer.class);
		
		final String ntransactiontestcode =stringUtilityFunction.fnDynamicListToString(batchCompCreationList,"getNtransactiontestcode");
		final String npreregno =stringUtilityFunction.fnDynamicListToString(batchCompCreationList,"getNpreregno");
		final String ntransactionsamplecode =stringUtilityFunction.fnDynamicListToString(batchCompCreationList,"getNtransactionsamplecode");
		final List<Map<String,Object>> activeWorklist = getActiveWorklistSampleByMultipleId(ntransactiontestcode, npreregno, ntransactionsamplecode, batchCreation.getNworklistcode(),userInfo);
		if(activeWorklist.isEmpty()) {
			String partQuery = "";
			String queryString = "insert into worklistsample (nworklistsamplecode,nworklistcode,ntransactiontestcode,npreregno,ntransactionsamplecode "
								+ " ,jsondata,jsonuidata,nsitecode,nstatus) " + " VALUES ";
			for (WorklistSample compCreation : batchCompCreationList) {
				compCreation.setNworklistsamplecode(nworklistsamplecode + 1);
				compCreation.setNworklistcode(batchCreation.getNworklistcode());
				compCreation.setNsitecode(userInfo.getNtranssitecode());
	
				partQuery = partQuery + "(" + compCreation.getNworklistsamplecode() + "," + compCreation.getNworklistcode()
						+ "," + compCreation.getNtransactiontestcode() + "," + compCreation.getNpreregno() + ","
						+ compCreation.getNtransactionsamplecode() + ",'"
						+ stringUtilityFunction.replaceQuote(mapper.writeValueAsString(compCreation.getJsondata())) + "','"
						+ stringUtilityFunction.replaceQuote(mapper.writeValueAsString(compCreation.getJsonuidata())) + "',"
						+ compCreation.getNsitecode() + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
				nworklistsamplecode = nworklistsamplecode + 1;

			}
			jdbcTemplate.execute(queryString + partQuery.substring(0, partQuery.length() - 1));
			final String UpQry = "update seqnoregistration set nsequenceno=" + nworklistsamplecode	+ " where stablename='worklistsample' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";
			jdbcTemplate.execute(UpQry);

			final Map<String, Object> objMap = new LinkedHashMap<>();
			objMap.putAll((Map<String, Object>) WorklistSampleAndHistory(batchCreation.getNworklistcode(),ndesigntemplatemappingcode, userInfo));
			final Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
			final JSONObject actionType = new JSONObject();
			final JSONObject jsonAuditObject = new JSONObject();
			auditmap.put("nregtypecode", batchCreation.getNregtypecode());
			auditmap.put("nregsubtypecode", batchCreation.getNregsubtypecode());
			auditmap.put("ndesigntemplatemappingcode", ndesigntemplatemappingcode);
			actionType.put("worklistsample", "IDS_ADDWORKLISTSAMPLE");
			jsonAuditObject.put("worklistsample", batchCompCreationList);
			auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);

			return new ResponseEntity<>(objMap, HttpStatus.OK);
		// ALPD-5548 - added by Gowtham in 12/03/2025 - Run Batch Creation --> While Add Samples in multi tab no alert is thrown and duplicate record is inserted.
		} else {
			final String sarno = activeWorklist.stream()
					.map(objbatch -> {
						final Object jsonDataObj = objbatch.get("jsondata");
						final JSONObject json = new JSONObject(jsonDataObj.toString());
						final Object sampleDataObj = json.get("worklist");
						final JSONObject worklist = new JSONObject(sampleDataObj.toString());
						return worklist.get("ssamplename")==JSONObject.NULL ? String.valueOf(worklist.get("sarno")) 
								: String.valueOf(worklist.get("ssamplearno"));
					}).collect(Collectors.joining(","));
			final String sAlert = sarno + " " + commonFunction.getMultilingualMessage(
					Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename());

			return new ResponseEntity<>(sAlert, HttpStatus.EXPECTATION_FAILED);
		}

	}

	
	public ResponseEntity<Object> getRegistrationTypeBySampleType(final Map<String, Object> inputMap, final UserInfo userInfo)throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();
		
		
		final String Str = "Select * from sampletype  where  nsampletypecode=" + inputMap.get("nsampletypecode");

		SampleType obj = (SampleType) jdbcUtilityFunction.queryForObject(Str, SampleType.class, jdbcTemplate);
		String validationQuery = "";
		if (obj.getNtransfiltertypecode() != -1 && userInfo.getNusercode() != -1) {
			int nmappingfieldcode = (obj.getNtransfiltertypecode() == 1) ? userInfo.getNdeptcode() : userInfo.getNuserrole();
			validationQuery = " and rst.nregsubtypecode in( SELECT rs.nregsubtypecode "
						    + "	FROM registrationsubtype rs "
						    + "	INNER JOIN transactionfiltertypeconfig ttc ON rs.nregsubtypecode = ttc.nregsubtypecode "
						    + "	LEFT JOIN transactionusers tu ON tu.ntransfiltertypeconfigcode = ttc.ntransfiltertypeconfigcode "
						    + "	WHERE ( ttc.nneedalluser = " + Enumeration.TransactionStatus.YES.gettransactionstatus()+ "  "
						    + " and ttc.nstatus =  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ "  "
						    + " AND ttc.nmappingfieldcode = " + nmappingfieldcode + ") OR "
						    + "	( ttc.nneedalluser = " + Enumeration.TransactionStatus.NO.gettransactionstatus() + "  "
						    + "	AND ttc.nmappingfieldcode =" + nmappingfieldcode + " AND tu.nusercode =" + userInfo.getNusercode() + "   "
						    + " and ttc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						    + " and tu.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") OR "
						    + "	( ttc.nneedalluser = " + Enumeration.TransactionStatus.NO.gettransactionstatus() + "  "
						    + " AND ttc.nmappingfieldcode = "+Enumeration.TransactionStatus.NA.gettransactionstatus()+" AND tu.nusercode =" + userInfo.getNusercode()+ "  "
						   + "  and ttc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ "  "
						   + "  and tu.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") "
						   + "	AND rs.nregtypecode = rt.nregtypecode) ";
		}		
		final String strregtypequery = "select rt.nregtypecode,coalesce(rt.jsondata->'sregtypename'->>'" + userInfo.getSlanguagetypecode() + "',rt.jsondata->'sregtypename'->>'en-US')  as sregtypename "
									 + "from approvalconfig ac,approvalconfigversion acv,registrationtype rt,registrationsubtype rst,regsubtypeconfigversion rsc "
									 + "where ac.napprovalconfigcode = acv.napprovalconfigcode "
									 + "and rt.nregtypecode = ac.nregtypecode "
									 + "and rt.nregtypecode = rst.nregtypecode "
									 + "and rst.nregsubtypecode = ac.nregsubtypecode "
									 + "and rsc.napprovalconfigcode = ac.napprovalconfigcode "
									 + "and acv.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+ " "
									 + "and  rsc.ntransactionstatus= "+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+ " "
								 	 + "and rt.nsampletypecode = "+ inputMap.get("nsampletypecode") + " "
									 + "and (rsc.jsondata->'nneedworklist')::jsonb ='true' "									 
									 + "and acv.nsitecode = " + userInfo.getNmastersitecode() + " "
									 + "and rt.nsitecode = " + userInfo.getNmastersitecode() + " "
									 + "and ac.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
									 + "and acv.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
									 + "and rt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
									 + "and rst.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
									 + "and rsc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
									 + "and rt.nregtypecode > 0 "+validationQuery+" group by rt.nregtypecode,sregtypename  order by rt.nregtypecode desc";
		final List<RegistrationType> lstRegistrationType = jdbcTemplate.query(strregtypequery,	new RegistrationType());
		if (!lstRegistrationType.isEmpty()) {
			map.put("RegistrationType", lstRegistrationType);
			map.put("RegTypeValue", lstRegistrationType.get(0));
			inputMap.put("realRegTypeValue", lstRegistrationType.get(0));
			inputMap.put("nregtypecode", lstRegistrationType.get(0).getNregtypecode());		
			map.putAll((Map<String, Object>) getRegistrationsubTypeByRegType(inputMap, userInfo));
		} else {
			map.put("RegTypeValue", lstRegistrationType);
			map.put("RegSubTypeValue", "");
			map.put("realApprovalVersionValue", "");
			map.put("defaultApprovalVersionValue", "");
			map.put("defaultApprovalVersion", "");

		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	public Map<String,Object> getRegistrationsubTypeByRegType(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();
		
		final String Str = "Select * from registrationtype rt,sampletype st where rt.nsampletypecode=st.nsampletypecode and rt.nregTypeCode="+ inputMap.get("nregtypecode");
		final SampleType obj = (SampleType) jdbcUtilityFunction.queryForObject(Str, SampleType.class, jdbcTemplate);
		
		String validationQuery = "";
		if (obj.getNtransfiltertypecode() != -1 && userInfo.getNusercode() != -1) {
			int nmappingfieldcode = (obj.getNtransfiltertypecode() == 1) ? userInfo.getNdeptcode() : userInfo.getNuserrole();
			validationQuery = " and rst.nregsubtypecode in( SELECT rs.nregsubtypecode "
							+ " FROM registrationsubtype rs "
							+ " INNER JOIN transactionfiltertypeconfig ttc ON rs.nregsubtypecode = ttc.nregsubtypecode "
							+ " LEFT JOIN transactionusers tu ON tu.ntransfiltertypeconfigcode = ttc.ntransfiltertypeconfigcode "
							+ " WHERE ( ttc.nneedalluser = " + Enumeration.TransactionStatus.YES.gettransactionstatus()
							+ " and ttc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " AND ttc.nmappingfieldcode = " + nmappingfieldcode + ")  OR "
							+ "( ttc.nneedalluser = "+ Enumeration.TransactionStatus.NO.gettransactionstatus() + "  " 
							+ " AND ttc.nmappingfieldcode ="+ nmappingfieldcode + " AND tu.nusercode =" + userInfo.getNusercode() + " "
							+ " and ttc.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ " and tu.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")   OR "
							+ "	( ttc.nneedalluser = " + Enumeration.TransactionStatus.NO.gettransactionstatus() + "  "
							+ " AND tu.nusercode =" + userInfo.getNusercode() + ""
							+ " and ttc.nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ "and tu.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") " + " "
							+ "AND rs.nregtypecode = "	+ inputMap.get("nregtypecode") + ")";
		}
		
		String strregsubtypequery = "select rst.nregsubtypecode,rst.nregtypecode,coalesce (rst.jsondata->'sregsubtypename'->>'" + userInfo.getSlanguagetypecode() + "',rst.jsondata->'sregsubtypename'->>'en-US') as sregsubtypename,"
								  + "cast(rsc.jsondata->>'nneedsubsample' as boolean) nneedsubsample,"
								  + "cast(rsc.jsondata->>'nneedworklist' as boolean) nneedworklist, "
								  + "cast(rsc.jsondata->>'nneedtemplatebasedflow' as boolean) nneedtemplatebasedflow "
								  + "from approvalconfig ac,approvalconfigversion acv,registrationsubtype rst,regsubtypeconfigversion rsc "
								  + "where "
								  + "ac.napprovalconfigcode = acv.napprovalconfigcode "
								  + "and rsc.napprovalconfigcode = ac.napprovalconfigcode "
								  + "and rst.nregsubtypecode = ac.nregsubtypecode "
								  + "and acv.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+ " "
							  	  + "and rsc.ntransactionstatus = "+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " "
								  + "and rst.nregtypecode = " + inputMap.get("nregtypecode") + " "
								  + "and (rsc.jsondata->'nneedworklist')::jsonb ='true' "
							  	  + "and ac.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								  + "and acv.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " 
							  	  + "and rst.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								  + "and rsc.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " 
								  + "and rst.nsitecode="+userInfo.getNmastersitecode()+" "
								  + "and rsc.nsitecode="+userInfo.getNmastersitecode()+" "
								  + "and rst.nregsubtypecode > 0  "
								  + "and (rsc.jsondata->'nneedworklist')::jsonb ='true'" + " "+validationQuery+" order by rst.nregsubtypecode desc";
		final List<RegistrationSubType> lstRegistrationSubType = jdbcTemplate.query(strregsubtypequery, new RegistrationSubType());
		if (!lstRegistrationSubType.isEmpty()) {
			map.put("RegistrationSubType", lstRegistrationSubType);
			map.put("RegSubTypeValue", lstRegistrationSubType.get(0));
			inputMap.put("nregsubtypecode", lstRegistrationSubType.get(0).getNregsubtypecode());
			inputMap.put("nneedsubsample", lstRegistrationSubType.get(0).isNneedsubsample());
			inputMap.put("nneedtemplatebasedflow", lstRegistrationSubType.get(0).isNneedtemplatebasedflow());
			map.put("nneedsubsample", lstRegistrationSubType.get(0).isNneedsubsample());
			map.put("nneedtemplatebasedflow", lstRegistrationSubType.get(0).isNneedtemplatebasedflow());

			map.put("fromDate", inputMap.get("fromdate"));
			map.put("toDate", inputMap.get("todate"));
			map.put("nregtypecode", inputMap.get("nregtypecode"));
			map.put("nregsubtypecode", lstRegistrationSubType.get(0).getNregsubtypecode());
			map.putAll((Map<String, Object>) getApprovalConfigVersion(map, userInfo));
		} else {
			map.put("defaultRegSubTypeValue", lstRegistrationSubType);
		}
		return map;
	}

	public ResponseEntity<Object> getWorklistSample(final Integer ninstCode, final Integer ndesigntemplatemappingcode,final UserInfo userInfo) throws Exception {
		final Map<String, Object> objMap = new LinkedHashMap<>();
		if (ninstCode != null) {
			objMap.putAll((Map<String, Object>) getWorklistSelectSample(ninstCode, ndesigntemplatemappingcode, userInfo, Enumeration.TransactionStatus.NO.gettransactionstatus()));
		}
		objMap.putAll((Map<String, Object>) WorklistSampleAndHistory(ninstCode, ndesigntemplatemappingcode, userInfo));
		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getSectionbaseTest(final UserInfo userInfo, final Map<String, Object> inputMap) throws Exception {

		final String getValidationStatus = " select transactionvalidation.ntransactionstatus from transactionvalidation where "
										 + " transactionvalidation.nregtypecode=" + inputMap.get("nregtypecode")
										 + " and transactionvalidation.nregsubtypecode=" + inputMap.get("nregsubtypecode")
										 + " and transactionvalidation.ncontrolcode=" + inputMap.get("ncontrolCode")
										 + " and transactionvalidation.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<TransactionStatus> validationStatusList = jdbcTemplate.query(getValidationStatus,new TransactionStatus());
		final String testStatus = validationStatusList.stream()
				.map(objpreregno -> String.valueOf(objpreregno.getNtransactionstatus()))
				.collect(Collectors.joining(","));

		String sQuery = " select DISTINCT tgt.stestsynonym  , tgt.ntestcode   "
					  + " from "
					  + " registrationtest rt,registrationtesthistory rth,testgrouptest tgt,registration r "
					  + " where r.npreregno=rt.npreregno"
					  + " and rt.ntransactiontestcode=rth.ntransactiontestcode "
					  + " and tgt.ntestgrouptestcode=rt.ntestgrouptestcode "
					  + " and r.nregtypecode= " + inputMap.get("nregtypecode")
					  + " and r.nregsubtypecode=" + inputMap.get("nregsubtypecode")
					  + " and rth.ntransactionstatus in (" + testStatus + ")"
					  + " and tgt.nsectioncode in (" + inputMap.get("nsectionCode") + ")"
					  + " and rth.nsitecode="+ userInfo.getNtranssitecode()+" "
					  + " and rt.nsitecode="+userInfo.getNtranssitecode()+ " "
					  + " and r.nsitecode=" + userInfo.getNtranssitecode()
					  + " and rt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					  + " and rth.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					  + " and tgt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					  + " and r.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					  + " and rth.ntesthistorycode in (select max(ntesthistorycode) from registrationtesthistory rth  "
					  + " where  rth.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 			
					  + " and  rth.nsitecode=" + userInfo.getNtranssitecode()
					  + " group by rth.ntransactiontestcode,rth.npreregno) ";

		return new ResponseEntity<Object>((List<TestGroupTest>) jdbcTemplate.query(sQuery, new TestGroupTest()),HttpStatus.OK);
	}

	public ResponseEntity<Object> getWorklistDetailFilter(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {

		final Map<String, Object> objMap = new LinkedHashMap<String, Object>();
		String strQuery = "";
		
		final String fromDate = dateUtilityFunction.instantDateToString(dateUtilityFunction.convertStringDateToUTC((String) inputMap.get("fromdate"), userInfo, true));
		final String toDate = dateUtilityFunction.instantDateToString(	dateUtilityFunction.convertStringDateToUTC((String) inputMap.get("todate"), userInfo, true));
		
		strQuery = " select wlh.nworklistcode ,wlh.sworklistno,wl.ntestcode,tm.stestname,wl.nsampletypecode,wl.nregtypecode,wl.nregsubtypecode,wl.nsectioncode,s.ssectionname,wlh.ntransactionstatus, "
				 + " ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "' stransdisplaystatus "
				 + " from worklist wl,worklisthistory wlh,testmaster tm,section s,transactionstatus ts"
				 + " where "
				 + " wl.nworklistcode=wlh.nworklistcode "
				 + " and tm.ntestcode=wl.ntestcode "
				 + " and wl.nsectioncode=s.nsectioncode "
				 + " and wlh.ntransactionstatus=ts.ntranscode "
				 + " and wlh.dtransactiondate between '" + fromDate + "' and '" + toDate+ "'"
				 + " and wl.nsampletypecode=" + inputMap.get("nsampletypecode") + " "
				 + " and wl.nregtypecode=" + inputMap.get("nregtypecode") + " "
				 + " and wl.nregsubtypecode=" + inputMap.get("nregsubtypecode") + " "
				 + " and wl.napprovalversioncode=" + inputMap.get("napprovalconfigversioncode")+" "
			 	 + " and wlh.ntransactionstatus in ( "+ inputMap.get("ntransactionstatus") + " ) " 
				 + " and wl.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				 + " and wlh.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
				 + " and tm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				 + " and s.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				 + " and wl.nsitecode = " + userInfo.getNtranssitecode()+ " and wlh.nsitecode=" + userInfo.getNtranssitecode()				
				 + " and wlh.nworkhistorycode in(select max(nworkhistorycode) from worklisthistory "
				 + " where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNtranssitecode()+" group by  nworklistcode ) "				
				 + " order by wl.nworklistcode desc ";
		final List<Worklist> lstInstGet = jdbcTemplate.query(strQuery, new Worklist());
		objMap.put("Worklist", lstInstGet);
		List<Map<String, Object>> lstWorklistGet = new ArrayList<>();
		List<WorklistHistory> lstWorklistHistoryGet = new ArrayList<>();
		if (lstInstGet.size() > 0) {
			objMap.putAll((Map<String, Object>) WorklistSampleAndHistory(lstInstGet.get(0).getNworklistcode(),	(Integer) inputMap.get("ndesigntemplatemappingcode"), userInfo));
			objMap.put("selectedWorklist", lstInstGet.get(0));
		} else {
			objMap.put("WorklistSamples", lstWorklistGet);
			objMap.put("WorklistHistory", lstWorklistHistoryGet);
		}
		
		//Ate234 janakumar ALPD-5000 Work List -> To get previously saved filter details when click the filter name
		if(inputMap.containsKey("saveFilterSubmit") &&  (boolean) inputMap.get("saveFilterSubmit") ==true) {
			projectDAOSupport.createFilterSubmit(inputMap,userInfo);
		}		
		List<FilterName> lstFilterName=getFilterName(userInfo);
		objMap.put("FilterName",lstFilterName);


		return new ResponseEntity<>(objMap, HttpStatus.OK);

	}
	
	public ResponseEntity<Object> getWorklistFilterDetails(final Map<String, Object> inputMap, final UserInfo userInfo)	throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final Map<String, Object> returnMap = new HashMap<>();

		final String strQuery1= " select json_agg(jsondata || jsontempdata) as jsondata from filtername where nformcode="+userInfo.getNformcode()+" and nusercode="+userInfo.getNusercode()+" "
	    					  + " and nuserrolecode="+userInfo.getNuserrole()+" and nsitecode="+userInfo.getNtranssitecode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
	    					  + " and nfilternamecode="+inputMap.get("nfilternamecode");
		
		final String strFilter = jdbcTemplate.queryForObject(strQuery1, String.class);
		
		final List<Map<String, Object>> lstfilterDetail = strFilter != null ? objMapper.readValue(strFilter, new TypeReference<List<Map<String, Object>>>() {}):new ArrayList<Map<String, Object>>();
		if(!lstfilterDetail.isEmpty()) {			
			final String strValidationQuery= " select json_agg(jsondata || jsontempdata) as jsondata from filtername where nformcode="+userInfo.getNformcode()+" and nusercode="+userInfo.getNusercode()+" "
		        						   + " and nuserrolecode="+userInfo.getNuserrole()+" and nsitecode="+userInfo.getNtranssitecode()+" "
		        						   + " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
		        						   + " and (jsondata->'nsampletypecode')::int="+inputMap.get("sampletypecode")+" "  
		        						   + " and (jsondata->'nregtypecode')::int="+inputMap.get("regtypecode")+" "  
		        						   + " and (jsondata->'nregsubtypecode')::int="+inputMap.get("regsubtypecode")+" "  
		        						   + " and (jsontempdata->'ntranscode')::int = "+inputMap.get("ntranscode")+" "  
		        						   + " and (jsontempdata->'napproveconfversioncode')::int="+inputMap.get("napprovalversioncode")+" "
		        						   + " and (jsontempdata->'ndesigntemplatemappingcode')::int="+inputMap.get("designtemplatcode")+" " 
		        						   + " and (jsontempdata->>'DbFromDate')='"+inputMap.get("FromDate")+"' "
		        						   + " and (jsontempdata->>'DbToDate')='"+inputMap.get("ToDate")+"' "
		        						   + " and nfilternamecode="+inputMap.get("nfilternamecode")+" ; " ; 
			
			final String strValidationFilter = jdbcTemplate.queryForObject(strValidationQuery, String.class);
	    	
			final List<Map<String, Object>> lstValidationFilter = strValidationFilter != null ? objMapper.readValue(strValidationFilter, new TypeReference<List<Map<String, Object>>>() {}):new ArrayList<Map<String, Object>>();
			if(lstValidationFilter.isEmpty()) {				   
				   final Instant instantFromDate = dateUtilityFunction.convertStringDateToUTC(lstfilterDetail.get(0).get("FromDate").toString(),userInfo, true);
				   final Instant instantToDate = dateUtilityFunction.convertStringDateToUTC(lstfilterDetail.get(0).get("ToDate").toString(),userInfo, true);
					
					final String fromDate = dateUtilityFunction.instantDateToString(instantFromDate);
					final String toDate =dateUtilityFunction.instantDateToString(instantToDate);

					returnMap.put("fromdateREG", lstfilterDetail.get(0).get("DbFromDate").toString());
					returnMap.put("todateREG", lstfilterDetail.get(0).get("DbToDate").toString());
					
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
					returnMap.put("fromdate", formattedFromString);
					returnMap.put("todate", formattedToString);

					List<RegistrationType> lstRegistrationType = new ArrayList<>();
					List<RegistrationSubType> lstRegistrationSubType = new ArrayList<>();
					List<TransactionStatus> listTransactionstatus = new ArrayList<>();
					final List<SampleType> lstSampleType = getSampleType(userInfo);				   
					if (!lstSampleType.isEmpty()) {						
						final SampleType filterSampleType=!lstfilterDetail.isEmpty()?objMapper.convertValue(lstfilterDetail.get(0).get("sampleTypeValue"),SampleType.class):lstSampleType.get(0);

						final List<FilterName> lstFilterName=getFilterName(userInfo);
						final short nsampletypecode = filterSampleType.getNsampletypecode();
						returnMap.put("SampleTypeValue", filterSampleType);
						returnMap.put("RealSampleTypeValue", filterSampleType);
						returnMap.put("defaultSampleTypeValue", filterSampleType);
						returnMap.put("SampleType", lstSampleType);
						returnMap.put("realSampleTypeList", lstSampleType);
						
						lstRegistrationType = getRegistrationType(nsampletypecode, userInfo);
												
						final RegistrationType filterRegistrationType=!lstfilterDetail.isEmpty()?objMapper.convertValue(lstfilterDetail.get(0).get("regTypeValue"),RegistrationType.class):lstRegistrationType.get(0);

						returnMap.put("RegTypeValue", filterRegistrationType);
						returnMap.put("RegistrationType", lstRegistrationType);
						returnMap.put("realRegistrationTypeList", lstRegistrationType);
						returnMap.put("defaultRegTypeValue", filterRegistrationType);
						returnMap.put("realRegTypeValue", filterRegistrationType);
						
						lstRegistrationSubType = getRegistrationSubType(filterRegistrationType.getNregtypecode(), userInfo);

						final RegistrationSubType filterRegistrationSubType=!lstfilterDetail.isEmpty()?objMapper.convertValue(lstfilterDetail.get(0).get("regSubTypeValue"),RegistrationSubType.class):lstRegistrationSubType.get(0);

						returnMap.put("RegSubTypeValue", filterRegistrationSubType);
						returnMap.put("RegistrationSubType", lstRegistrationSubType);
						returnMap.put("realRegistrationSubTypeList", lstRegistrationSubType);
						returnMap.put("defaultRegSubTypeValue", filterRegistrationSubType);
						returnMap.put("realRegSubTypeValue", filterRegistrationSubType);

						listTransactionstatus = getFilterStatus(filterRegistrationType.getNregtypecode(),
								filterRegistrationSubType.getNregsubtypecode(), userInfo);
						
						final TransactionStatus filterTransactionStatus=!lstfilterDetail.isEmpty()?objMapper.convertValue(lstfilterDetail.get(0).get("filterStatusValue"),TransactionStatus.class):listTransactionstatus.get(0);
						
						returnMap.put("FilterStatusValue", filterTransactionStatus);
						returnMap.put("nfilterstatus", filterTransactionStatus.getNtransactionstatus());
						returnMap.put("RealFilterStatusValue", filterTransactionStatus);
						returnMap.put("defaultFilterStatusValue", filterTransactionStatus);
						returnMap.put("FilterStatus", listTransactionstatus);
						returnMap.put("realFilterStatusList", listTransactionstatus);
						returnMap.put("nregtypecode", filterRegistrationType.getNregtypecode());
						returnMap.put("nregsubtypecode", filterRegistrationSubType.getNregsubtypecode());
						returnMap.put("nneedtemplatebasedflow", filterRegistrationSubType.isNneedtemplatebasedflow());
						
						
						returnMap.put("filterDetailValue", lstfilterDetail);
						returnMap.putAll((Map<String, Object>) getApprovalConfigVersion(returnMap, userInfo));
						returnMap.put("FilterName",lstFilterName);
												
						final String strQuery = " select wlh.nworklistcode ,wlh.sworklistno,wl.ntestcode,tm.stestname,wl.nsampletypecode,wl.nregtypecode,wl.nregsubtypecode,"
											  + " wl.nsectioncode,s.ssectionname,wlh.ntransactionstatus  ,ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "' stransdisplaystatus "
											  + " from worklist wl,worklisthistory wlh,testmaster tm,section s,transactionstatus ts "
											  + " where "
											  + " wl.nworklistcode=wlh.nworklistcode "
											  + " and tm.ntestcode=wl.ntestcode "
											  + " and wl.nsectioncode=s.nsectioncode "
											  + " and wlh.ntransactionstatus=ts.ntranscode "
											  + " and wlh.dtransactiondate between '" + fromDate + "' and '" + toDate + "' "
											  + " and wl.nsampletypecode=" + nsampletypecode + ""
											  + " and wl.nregtypecode=" + filterRegistrationType.getNregtypecode() + " "
											  + " and wl.nregsubtypecode=" + filterRegistrationSubType.getNregsubtypecode() + ""
											  + " and wl.napprovalversioncode=" + lstfilterDetail.get(0).get("napproveconfversioncode")
											  + " and wlh.ntransactionstatus in ( "  + lstfilterDetail.get(0).get("ntransactionstatusfilter") + " ) "											  
											  + " and wl.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
											  + " and wlh.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
											  + " and tm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
											  + " and s.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
											  + " and wl.nsitecode = "+userInfo.getNtranssitecode()+" and wlh.nsitecode=" + userInfo.getNtranssitecode()
											  + " and wlh.nworkhistorycode in(select max(nworkhistorycode) from worklisthistory "
											  + " where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNtranssitecode()+""
											  + " group by  nworklistcode ) "
											  + "order by wl.nworklistcode desc ";
						final List<Worklist> lstInstGet = jdbcTemplate.query(strQuery, new Worklist());
						returnMap.put("Worklist", lstInstGet);
						final List<Map<String, Object>> lstWorklistGet = new ArrayList<>();
						final List<WorklistHistory> lstWorklistHistoryGet = new ArrayList<>();
						if (lstInstGet.size() > 0) {
							returnMap.putAll((Map<String, Object>) WorklistSampleAndHistory(lstInstGet.get(0).getNworklistcode(),(Integer) lstfilterDetail.get(0).get("ndesigntemplatemappingcode"), userInfo));
							returnMap.put("selectedWorklist", lstInstGet.get(0));
						} else {
							returnMap.put("WorklistSamples", lstWorklistGet);
							returnMap.put("WorklistHistory", lstWorklistHistoryGet);
						}
			   }
				return new ResponseEntity<>(returnMap, HttpStatus.OK);					   
			   } else {
			    	return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDFILTERALREADYLOADED",userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			    }			
		}else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDFILTERISNOTPRESENT",userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}
	}

	public ResponseEntity<Object> updateWorklistDetail(Map<String, Object> inputMap, UserInfo userInfo)	throws Exception {
		final String sQuery = " lock table lockworklist " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus() + "";
		jdbcTemplate.execute(sQuery);

		final Map<String, Object> objMap = new LinkedHashMap<String, Object>();
		final ObjectMapper objmapper = new ObjectMapper();
		final Worklist inst = objmapper.convertValue(inputMap.get("worklist"), Worklist.class);
		final String countQuery = "select count(worklistsample.nworklistsamplecode) from worklistsample where worklistsample.nworklistcode="
								+ inst.getNworklistcode()+ " and nsitecode="+userInfo.getNtranssitecode()+" and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final int count = jdbcTemplate.queryForObject(countQuery, Integer.class);
		String ssamplearno = "";
		String sworklistno = "";
		if (count != 0) {
			final int status = ((Worklist) validationStatus(inst.getNworklistcode(), userInfo)).getNtransactionstatus();
			if (status != Enumeration.TransactionStatus.PREPARED.gettransactionstatus()) {
				String approveQuery = "select CONCAT(jsondata->'worklist'->>'stestname',' / ',jsondata->'worklist'->>'ssamplearno',' / ',wlh.sworklistno) as sworklistno "
									+ "from worklistsample wls,worklisthistory   wlh  "
									+ "where  wlh.nworklistcode=wls.nworklistcode "
									+ "and wls.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
									+ "and wlh.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  "
									+ "and wls.nsitecode="+userInfo.getNtranssitecode()+" and wlh.nsitecode="+userInfo.getNtranssitecode()+" "
									+ "and wlh.nworkhistorycode =any (select max(worklisthistory.nworkhistorycode) from worklisthistory where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNtranssitecode()+" group by nworklistcode) "
									+ "and ntransactionstatus = "+ Enumeration.TransactionStatus.PREPARED.gettransactionstatus()+" "
									+ "and ntransactiontestcode =any (select worklistsample.ntransactiontestcode from worklistsample where nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
									+ "and nsitecode="+userInfo.getNtranssitecode()+" and worklistsample.nworklistcode=" + inst.getNworklistcode() + ")";
				final List<WorklistHistory> lstWorklistGet = (List<WorklistHistory>) jdbcTemplate.query(approveQuery,	new WorklistHistory());

				sworklistno = ((lstWorklistGet.stream().map(object -> String.valueOf(object.getSworklistno()))
						.collect(Collectors.toList())).stream().distinct().collect(Collectors.toList())).stream()
								.collect(Collectors.joining(","));
				if (sworklistno.equals("")) {

					final String getValidationStatus = "select ts.jsondata->'stransdisplaystatus'->>'"
													 + userInfo.getSlanguagetypecode() + "' as stransdisplaystatus,ts.ntranscode "
													 + " from transactionvalidation rvd,transactionstatus ts"
													 + " where ts.ntranscode = rvd.ntransactionstatus" + " and rvd.ncontrolcode = "
													 + inputMap.get("ncontrolCode") + " and rvd.nformcode = " + userInfo.getNformcode()
													 + " and rvd.nsitecode = " + userInfo.getNmastersitecode() + " and ts.nstatus = "
													 + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rvd.nstatus = "
													 + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rvd.nregtypecode="
													 + inst.getNregtypecode() + " and rvd.nregsubtypecode=" + inst.getNregsubtypecode();
					final List<TransactionStatus> validationStatusList = jdbcTemplate.query(getValidationStatus,new TransactionStatus());
					final String sectionStatus = validationStatusList.stream()
							.map(objpreregno -> String.valueOf(objpreregno.getNtranscode()))
							.collect(Collectors.joining(","));
//					final String AlertStatus = validationStatusList.stream()
//							.map(objpreregno -> String.valueOf(objpreregno.getStransdisplaystatus()))
//							.collect(Collectors.joining(","));

					final String statusValidation = " select rs.ssamplearno from registrationtesthistory rth,worklistsample wl, registrationsamplearno rs"
												  + " where "
												  + " rth.ntransactionsamplecode=wl.ntransactionsamplecode "
												  + " and rs.ntransactionsamplecode=wl.ntransactionsamplecode "
												  + " and rth.ntransactionstatus not in (" + sectionStatus + ")  "
												  + " and rth.ntesthistorycode =any (select max(ntesthistorycode) from registrationtesthistory"
												  + " where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
												  + " and nsitecode="+userInfo.getNtranssitecode()+" group by npreregno,ntransactionsamplecode,ntransactiontestcode) "	
												  + " and rth.ntransactiontestcode in "
												  + " (select worklistsample.ntransactiontestcode from worklistsample where nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
												  + " and nsitecode="+userInfo.getNtranssitecode()+" and worklistsample.nworklistcode=" + inst.getNworklistcode() + ") "
												  + " and rth.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
												  + " and wl.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
												  + " and rs.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
												  + " and rth.nsitecode="+ userInfo.getNtranssitecode() + " "
												  + " and wl.nsitecode="+ userInfo.getNtranssitecode() + " "
												  + " and rs.nsitecode="+ userInfo.getNtranssitecode() + " ";

					final List<WorklistSample> lstWorklistSampleGet = (List<WorklistSample>) jdbcTemplate.query(statusValidation, new WorklistSample());

					ssamplearno = ((lstWorklistSampleGet.stream().map(object -> String.valueOf(object.getSsamplearno()))
							.collect(Collectors.toList())).stream().distinct().collect(Collectors.toList())).stream()
									.collect(Collectors.joining(","));

					if (ssamplearno.equals("")) {

//						String old = " select wh.sworklistno,tm.stestname,sc.ssectionname from worklist w,worklisthistory wh,testmaster tm,section sc where "
//								   + " w.ntestcode=tm.ntestcode and w.nsectioncode=sc.nsectioncode and "
//								   + " w.nworklistcode=wh.nworklistcode and w.nstatus=1 " + " and wh.ntransactionstatus=8 "
//								   + " and wh.nworklistcode=" + inst.getNworklistcode();
//						
//						Worklist lstaudit = (Worklist) jdbcTemplate.query(old, new Worklist());
						final String strformat = projectDAOSupport.getSeqfnFormat("worklist", "seqnoformatgeneratorworklist",inst.getNregtypecode(),inst.getNregsubtypecode(), userInfo);
						final String worklistHistory = "select nsequenceno from seqnoregistration where stablename='worklisthistory' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
						int worklistseqNo = jdbcTemplate.queryForObject(worklistHistory, Integer.class);
						worklistseqNo = worklistseqNo + 1;
						final String strWorkListHistory = "Insert into worklisthistory(nworkhistorycode, nworklistcode, sworklistno,ntransactionstatus, dtransactiondate, nusercode, "
														+ " nuserrolecode, ndeputyusercode, ndeputyuserrolecode, scomments, nstatus,noffsetdtransactiondate,ntransdatetimezonecode,nsitecode) values "
														+ "(" + worklistseqNo + "," + inst.getNworklistcode() + ",'" + strformat + "',"
														+ Enumeration.TransactionStatus.PREPARED.gettransactionstatus() + ",'"
														+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNusercode() + ","
														+ userInfo.getNuserrole() + "," + userInfo.getNdeputyusercode() + ","
														+ userInfo.getNdeputyuserrole() + ",'" + stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "',"
														+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
														+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
														+ userInfo.getNtimezonecode() + "," + userInfo.getNtranssitecode() + ")";
						jdbcTemplate.execute(strWorkListHistory);

						String HityQry = "update seqnoregistration set nsequenceno=" + worklistseqNo + " where stablename='worklisthistory'  and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";
						jdbcTemplate.execute(HityQry);
						


						final String strQuery = "select wlh.nworklistcode ,wlh.sworklistno,wl.ntestcode,t.stestname,wl.nsampletypecode,wl.nregtypecode,wl.nregsubtypecode,wl.nsectioncode,s.ssectionname,wlh.ntransactionstatus, "
											  + " ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "' stransdisplaystatus "
											  + " from worklist wl,worklisthistory wlh,testmaster t,transactionstatus ts,section s "
											  + " where "
											  + " wlh.nworklistcode=wl.nworklistcode "
											  + " and t.ntestcode=wl.ntestcode "
											  + " and wlh.ntransactionstatus=ts.ntranscode "
											  + " and s.nsectioncode=wl.nsectioncode "
											  + " and wl.nsampletypecode=" + inst.getNsampletypecode() + " "
											  + " and wl.nregtypecode= " + inst.getNregtypecode() + " " 
											  + " and wl.nregsubtypecode=" + inst.getNregsubtypecode() + " "
											  + " and wlh.ntransactionstatus = " + Enumeration.TransactionStatus.PREPARED.gettransactionstatus()+" "
											  + " and wl.nsitecode="+userInfo.getNtranssitecode()+" and wlh.nsitecode=" + userInfo.getNtranssitecode()
											  + " and wl.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											  + " and wlh.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											  + " and t.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											  + " and ts.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											  + " and s.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											  + " and wlh.nworkhistorycode ="+worklistseqNo+" and wlh.nworklistcode="+inst.getNworklistcode()+" order by wlh.nworklistcode desc";

						final List<Worklist> lstInstGet = jdbcTemplate.query(strQuery, new Worklist());

						final String newaudit = "select wh.sworklistno,tm.stestname,sc.ssectionname, coalesce(ts.jsondata->'stransdisplaystatus'->>'"
											  +  userInfo.getSlanguagetypecode()+"', ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus "
											  + "from worklist w,worklisthistory wh,testmaster tm,section sc, transactionstatus ts "
											  + "where "
											  + "w.nworklistcode=wh.nworklistcode "						
											  + "and w.ntestcode=tm.ntestcode and w.nsectioncode=sc.nsectioncode and ts.ntranscode=wh.ntransactionstatus "
											  + "and w.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" " 
											  + "and wh.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" " 
											  + "and tm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" " 
											  + "and sc.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" " 
											  + "and ts.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
											  + "and w.nsitecode="+userInfo.getNtranssitecode()+" and wh.nsitecode=" + userInfo.getNtranssitecode()+" "
											  + "and wh.ntransactionstatus="+ Enumeration.TransactionStatus.PREPARED.gettransactionstatus()+" "
											  + "and wh.nworklistcode=" + inst.getNworklistcode()+" ";

						Worklist lstnewaudit = (Worklist) jdbcUtilityFunction.queryForObject(newaudit, Worklist.class,jdbcTemplate);

						
						final Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
						final JSONObject actionType = new JSONObject();
						final JSONObject jsonAuditObject = new JSONObject();
						auditmap.put("nregtypecode", inst.getNregtypecode());
						auditmap.put("nregsubtypecode", inst.getNregsubtypecode());
						auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
						actionType.put("worklisthistory", "IDS_WORKLISTPREPARED");
						jsonAuditObject.put("worklisthistory", Arrays.asList(lstnewaudit));
						auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);

						if (lstInstGet.size() > 0) {
							objMap.putAll((Map<String, Object>) WorklistSampleAndHistory(lstInstGet.get(0).getNworklistcode(),(Integer) inputMap.get("ndesigntemplatemappingcode"), userInfo));
							objMap.put("selectedWorklist", lstInstGet.get(0));
						} else {
							objMap.put("WorklistSamples", lstWorklistGet);
						}
						return new ResponseEntity<>(objMap, HttpStatus.OK);
					} else {
						return new ResponseEntity<>(ssamplearno + commonFunction.getMultilingualMessage("IDS_INTHEANALYSTPROCESS",userInfo.getSlanguagefilename()),	HttpStatus.EXPECTATION_FAILED);
					}
				}
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ALREADYPREPAREDWORKLIST",userInfo.getSlanguagefilename()) + ' ' + sworklistno, HttpStatus.EXPECTATION_FAILED);
			}

			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ALREADYPREPARED", userInfo.getSlanguagefilename()),HttpStatus.CONFLICT);		}
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ADDSAMPLE", userInfo.getSlanguagefilename()),HttpStatus.CONFLICT);

	}

	public Worklist getEditSectionAndTest(final Integer ninstrumentcode, final UserInfo userInfo) throws Exception {

		final String strQuery = " select wl.nsectioncode,wl.ntestcode,tm.stestname as stestsynonym,s.ssectionname "
							  + " from worklist wl ,testmaster tm,section s "
							  + " where wl.ntestcode=tm.ntestcode and wl.nsectioncode=s.nsectioncode "
							  + " and wl.nsitecode="+userInfo.getNtranssitecode()+" "
							  + " and w.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" " 
							  + " and tm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" " 
							  + " and s.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" " 
							  + " and wl.nworklistcode="+ ninstrumentcode;
		final Worklist instGet = (Worklist) jdbcUtilityFunction.queryForObject(strQuery, Worklist.class,jdbcTemplate);
		return instGet;
	}

	public ResponseEntity<Object> deleteWorklistSample(final WorklistSample inst,final Integer ndesigntemplatemappingcode,final	UserInfo userInfo) throws Exception {

		final int status = ((Worklist) validationStatus(inst.getNworklistcode(), userInfo)).getNtransactionstatus();
		if (status == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
			final WorklistSample worklistsampleByID =  getActiveWorklistSampleById(inst.getNworklistsamplecode(),userInfo);
			if (worklistsampleByID == null) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_ALREADYDELETED", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);

			} else {

				String updateQueryString = "update worklistsample set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nworklistsamplecode = "
						+ inst.getNworklistsamplecode() + " and nworklistcode=" + inst.getNworklistcode()
						+ " and ntransactiontestcode=" + inst.getNtransactiontestcode();

				jdbcTemplate.execute(updateQueryString);
				
				final Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
				final JSONObject actionType = new JSONObject();
				final JSONObject jsonAuditObject = new JSONObject();
				auditmap.put("nregtypecode", inst.getNregtypecode());
				auditmap.put("nregsubtypecode", inst.getNregsubtypecode());
				auditmap.put("ndesigntemplatemappingcode", ndesigntemplatemappingcode);
				actionType.put("worklistsample", "IDS_DELETEWORKLISTSAMPLE");
				jsonAuditObject.put("worklistsample", Arrays.asList(inst));
				auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);
				final Map<String, Object> responseMap = new HashMap<String, Object>();
				responseMap.putAll((Map<String, Object>) WorklistSampleAndHistory(inst.getNworklistcode(),ndesigntemplatemappingcode, userInfo));
				return new ResponseEntity<>(responseMap, HttpStatus.OK);
			}
		}
		return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PREPAREDCANNOTDELETE", userInfo.getSlanguagefilename()),	HttpStatus.EXPECTATION_FAILED);
	}

	
	public ResponseEntity<Object> updateWorklist(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final Worklist inst = objMapper.convertValue(inputMap.get("worklist"), Worklist.class);
		final String Qry = "select * from worklist where worklist.nsitecode="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "and worklist.nsitecode="
						 + userInfo.getNtranssitecode() + " and worklist.nworklistcode=" + inst.getNworklistcode();
		final Worklist instGet = (Worklist) jdbcUtilityFunction.queryForObject(Qry, Worklist.class,jdbcTemplate);

		if ((instGet.getNtestcode() != inst.getNtestcode()) || (instGet.getNsectioncode() != inst.getNsectioncode())) {
			String statusQuery = "update worklistsample set nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nworklistcode="+ inst.getNworklistcode();
			jdbcTemplate.execute(statusQuery);
			String statusQry = "update worklist set ntestcode=" + inst.getNtestcode() + ",nsectioncode="+ inst.getNsectioncode() + " where nworklistcode=" + inst.getNworklistcode();
			jdbcTemplate.execute(statusQry);
		}
		final Map<String, Object> responseMap = new HashMap<String, Object>();
		final String strQuery = " select wlh.nworklistcode ,wlh.sworklistno,wl.ntestcode,t.stestname,wl.nsampletypecode,wl.nregtypecode,wl.nregsubtypecode,"
							  + " wl.nsectioncode,wlh.ntransactionstatus "
							  + " from worklist wl,worklisthistory wlh,testmaster t "
							  + " where wlh.nworklistcode=wl.nworklistcode and t.ntestcode=wl.ntestcode "
							  + " and  wlh.nworklistcode not in("
							  + " select wlh.nworklistcode from worklisthistory wlh,worklist wl "
							  + " where "
							  +  " wl.nworklistcode=wlh.nworklistcode "
							  + " and wl.nsampletypecode=" + inst.getNsampletypecode() + ""
							  + " and wl.nregtypecode= " + inst.getNregtypecode() + " "
							  + " and wl.nregsubtypecode=" + inst.getNregsubtypecode()+ " "
							  + " and wlh.ntransactionstatus= "+ Enumeration.TransactionStatus.PREPARED.gettransactionstatus()
							  + " and wl.nsitecode=" + userInfo.getNtranssitecode()+" and wlh.nsitecode=" + userInfo.getNtranssitecode()
							  + " and wl.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
							  + " and wlh.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  )" 		
							  + " and  wl.nsampletypecode=" + inst.getNsampletypecode() + " "
							  + " and wl.nregtypecode= " + inst.getNregtypecode() + " "
							  + " and wl.nregsubtypecode=" + inst.getNregsubtypecode() + " "
							  + " and wl.nsitecode=" + userInfo.getNtranssitecode()+" and wlh.nsitecode=" + userInfo.getNtranssitecode()+" "
							  + " and wl.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
							  + " and wlh.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  " 	
							  + " and t.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  " 	;
 

		final List<Worklist> lstInstGet = jdbcTemplate.query(strQuery, new Worklist());
		responseMap.put("Worklist", lstInstGet);
		return new ResponseEntity<>(responseMap, HttpStatus.OK);
	}

	public Map<String,Object> getApprovalConfigVersion(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();
		final ObjectMapper objMapper = new ObjectMapper();
		String dateFrom="";
		String dateTo="";
		
		//Ate234 janakumar ALPD-5000 Work List -> To get previously saved filter details when click the filter name

		if(inputMap.get("fromdateREG")!=null) {
			dateFrom=inputMap.get("fromdateREG").toString();
			dateTo=inputMap.get("todateREG").toString();
		}else if(inputMap.get("fromdate")!=null) {
			dateFrom=inputMap.get("fromdate").toString();
			dateTo=inputMap.get("todate").toString();
		}else {
			dateFrom=inputMap.get("fromDate").toString();
			dateTo=inputMap.get("toDate").toString();
		}		
		map.putAll((Map<String, Object>) getRegistrationTemplateList(inputMap, userInfo));
		final String fromDate = dateUtilityFunction.instantDateToString(
				dateUtilityFunction.convertStringDateToUTC(dateFrom, userInfo, true));
		final String toDate = dateUtilityFunction.instantDateToString(
				dateUtilityFunction.convertStringDateToUTC(dateTo, userInfo, true));

		final String versionquery = "select aca.sversionname,aca.napprovalconfigcode,aca.napprovalconfigversioncode ,acv.ntransactionstatus,acv.ntreeversiontempcode  "
								  + "from registration r,registrationarno rar,registrationhistory rh,approvalconfigautoapproval aca, "
								  + "approvalconfig ac,approvalconfigversion acv  "
								  + " where r.npreregno=rar.npreregno and r.npreregno=rh.npreregno "
								  + " and rar.napprovalversioncode=acv.napproveconfversioncode "
								  + " and acv.napproveconfversioncode=aca.napprovalconfigversioncode "
								  + " and r.nregtypecode=ac.nregtypecode and r.nregsubtypecode=ac.nregsubtypecode "
								  + " and r.nregtypecode =" + inputMap.get("nregtypecode") + ""
								  + " and r.nregsubtypecode ="  + inputMap.get("nregsubtypecode") + " "
								  + " and rh.ntransactionstatus = "  + Enumeration.TransactionStatus.REGISTERED.gettransactionstatus() + " "
								  + " and acv.nsitecode =" + userInfo.getNmastersitecode() + " "
								  + " and r.nsitecode =" + userInfo.getNtranssitecode() + " "
								  + " and rar.nsitecode =" + userInfo.getNtranssitecode() + " "
								  + " and rh.nsitecode =" + userInfo.getNtranssitecode() + " "
								  + " and rh.dtransactiondate between '" + fromDate + "' and '"  + toDate + "' "
								  + " and r.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								  + " and rar.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
								  + " and rh.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								  + " and ac.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								  + " and ac.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								  + " and acv.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								  + " and aca.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								  + " group by acv.ntransactionstatus,aca.napprovalconfigversioncode,acv.ntreeversiontempcode,aca.napprovalconfigcode,aca.sversionname";
		final List<ApprovalConfigAutoapproval> lstApprovalConfigVersion = jdbcTemplate.query(versionquery,	new ApprovalConfigAutoapproval());

		final Integer ndesigntemplatemappingcode = (int) map.get("ndesigntemplatemappingcode");
		inputMap.put("ndesigntemplatemappingcode", ndesigntemplatemappingcode);
		map.put("DynamicDesign", projectDAOSupport.getTemplateDesign(ndesigntemplatemappingcode, userInfo.getNformcode()));
		
		if (!lstApprovalConfigVersion.isEmpty()) {
			final List<ApprovalConfigAutoapproval> approvedApprovalVersionList = lstApprovalConfigVersion.stream().filter(
					obj -> obj.getNtransactionstatus() == Enumeration.TransactionStatus.APPROVED.gettransactionstatus() ||  obj.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED.gettransactionstatus())
					.collect(Collectors.toList());

			if (approvedApprovalVersionList != null && !approvedApprovalVersionList.isEmpty()) {
				
				final ApprovalConfigAutoapproval filterApprovalConfig=inputMap.containsKey("filterDetailValue") && !((List<Map<String, Object>>) inputMap.get("filterDetailValue")).isEmpty()
						&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0).containsKey("approvalConfigValue")
	                    ? objMapper.convertValue(((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0).get("approvalConfigValue"),ApprovalConfigAutoapproval.class) :approvedApprovalVersionList.get(0);

				
				inputMap.put("napprovalversioncode",filterApprovalConfig.getNapprovalconfigversioncode());
				map.put("napprovalversioncode",filterApprovalConfig.getNapprovalconfigversioncode());
				inputMap.put("realApprovalVersionValue", filterApprovalConfig);
				map.put("realApprovalVersionValue", filterApprovalConfig);
				map.put("defaultApprovalVersionValue",filterApprovalConfig);
				map.put("defaultApprovalVersion", filterApprovalConfig);
				map.put("approvalConfigValue", filterApprovalConfig);

			} else {
				
				final ApprovalConfigAutoapproval filterApprovalConfig=inputMap.containsKey("filterDetailValue") && !((List<Map<String, Object>>) inputMap.get("filterDetailValue")).isEmpty()
						&& ((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0).containsKey("approvalConfigValue")
	                    ? objMapper.convertValue(((List<Map<String, Object>>) inputMap.get("filterDetailValue")).get(0).get("approvalConfigValue"),ApprovalConfigAutoapproval.class) :approvedApprovalVersionList.get(0);

				
				
				inputMap.put("napprovalversioncode",filterApprovalConfig.getNapprovalconfigversioncode());
				map.put("napprovalversioncode",filterApprovalConfig.getNapprovalconfigversioncode());
				inputMap.put("realApprovalVersionValue", filterApprovalConfig);
				map.put("realApprovalVersionValue", filterApprovalConfig);
				map.put("defaultApprovalVersionValue",filterApprovalConfig);
				map.put("defaultApprovalVersion", filterApprovalConfig);

			}
		} else {
			map.put("realApprovalVersionValue", lstApprovalConfigVersion);
			map.put("defaultApprovalVersionValue", lstApprovalConfigVersion);
			map.put("defaultApprovalVersion", lstApprovalConfigVersion);

		}
		map.put("ApprovalConfigVersion", lstApprovalConfigVersion);
		map.put("realApprovalConfigVersionList", lstApprovalConfigVersion);

		final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());

		final String fromDateUI = LocalDateTime.parse(dateFrom, dbPattern).format(uiPattern);
		final String toDateUI = LocalDateTime.parse(dateTo, dbPattern).format(uiPattern);
		map.put("fromDate", fromDateUI);
		map.put("toDate", toDateUI);

		return map;

	}

	private Map<String,Object> getRegistrationTemplateList(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();

		final Boolean templateBasedFlow = (Boolean) inputMap.get("nneedtemplatebasedflow");
		//Added by L.Subashini to handle 500 error when there are no data in the filter inputs
		List<DesignTemplateMapping> lstReactRegistrationTemplate = new ArrayList<>();

		if (templateBasedFlow == null) {
			map.put("realDesignTemplateMappingValue", lstReactRegistrationTemplate);
			map.put("defaultDesignTemplateMappingValue", lstReactRegistrationTemplate);
		} else {
			final String stransstatus = templateBasedFlow
										? Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ","
										+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus()
										: String.valueOf(Enumeration.TransactionStatus.APPROVED.gettransactionstatus());
	
			final String str = "select dm.ndesigntemplatemappingcode,rt.jsondata,dm.ntransactionstatus,CONCAT(rt.sregtemplatename,'(',cast(dm.nversionno as character varying),')') sregtemplatename "
							 + "from designtemplatemapping dm, reactregistrationtemplate rt "
							 + "where "
							 + "rt.nreactregtemplatecode=dm.nreactregtemplatecode "
							 + "and dm.nsitecode ="+userInfo.getNmastersitecode()+" and rt.nsitecode ="+userInfo.getNmastersitecode()+" "
							 + "and dm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							 + "and rt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							 + "and nregtypecode=" + inputMap.get("nregtypecode")+ " "
							 + "and nregsubtypecode=" + inputMap.get("nregsubtypecode")+" "
							 + "and dm.ntransactionstatus in" + " (" + stransstatus + ") "
							 + "order by dm.ndesigntemplatemappingcode desc";
	
			lstReactRegistrationTemplate = jdbcTemplate.query(str, new DesignTemplateMapping());
			if (!lstReactRegistrationTemplate.isEmpty()) {
				map.put("DynamicDesignMapping", lstReactRegistrationTemplate);
				map.put("realDesignTemplateMappingValue", lstReactRegistrationTemplate.get(0));
				map.put("defaultDesignTemplateMappingValue", lstReactRegistrationTemplate.get(0));
				map.put("DesignTemplateMappingValue", lstReactRegistrationTemplate.get(0));
				map.put("ndesigntemplatemappingcode", lstReactRegistrationTemplate.get(0).getNdesigntemplatemappingcode());
				inputMap.put("realDesignTemplateMappingValue", lstReactRegistrationTemplate.get(0));
				inputMap.put("ndesigntemplatemappingcode",lstReactRegistrationTemplate.get(0).getNdesigntemplatemappingcode());
			} else {
				map.put("realDesignTemplateMappingValue", lstReactRegistrationTemplate);
				map.put("defaultDesignTemplateMappingValue", lstReactRegistrationTemplate);	
			}
		}
		return map;
	}

	public ResponseEntity<Object> getApprovalConfigVersionByRegSubType(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();
		map.putAll((Map<String, Object>) getRegistrationTemplateList(inputMap, userInfo));
		final String fromDate = dateUtilityFunction.instantDateToString(dateUtilityFunction.convertStringDateToUTC((String) inputMap.get("fromdate"), userInfo, true));
		final String toDate = dateUtilityFunction.instantDateToString(dateUtilityFunction.convertStringDateToUTC((String) inputMap.get("todate"), userInfo, true));

		final String versionquery = "select aca.sversionname,aca.napprovalconfigcode,aca.napprovalconfigversioncode ,acv.ntransactionstatus,acv.ntreeversiontempcode  "
								  + "from registration r,registrationarno rar,registrationhistory rh,approvalconfigautoapproval aca, "
								  + " approvalconfig ac,approvalconfigversion acv  "
								  + " where r.npreregno=rar.npreregno and r.npreregno=rh.npreregno "
								  + " and rar.napprovalversioncode=acv.napproveconfversioncode "
								  + " and acv.napproveconfversioncode=aca.napprovalconfigversioncode "
								  + " and r.nregtypecode=ac.nregtypecode and r.nregsubtypecode=ac.nregsubtypecode "
								  + " and r.nregtypecode =" + inputMap.get("nregtypecode") + ""
								  + " and r.nregsubtypecode =" + inputMap.get("nregsubtypecode") + " " 
								  + " and rh.ntransactionstatus = "+ Enumeration.TransactionStatus.REGISTERED.gettransactionstatus() + " "
								  + " and aca.nsitecode ="+ userInfo.getNmastersitecode() + " " 
								  + " and ac.nsitecode ="+ userInfo.getNmastersitecode() + " " 
								  + " and acv.nsitecode ="+ userInfo.getNmastersitecode() + " " 
								  + " and rh.dtransactiondate between '" + fromDate + "' and '"	  + toDate + "' "
								  + " and r.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								  + " and rar.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
								  + " and rh.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " 
								  + " and acv.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								  + " and aca.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								  + "and ac.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  "
								  + "group by acv.ntransactionstatus,aca.napprovalconfigversioncode,acv.ntreeversiontempcode,aca.napprovalconfigcode,aca.sversionname";
		final List<ApprovalConfigAutoapproval> lstApprovalConfigVersion = jdbcTemplate.query(versionquery,	new ApprovalConfigAutoapproval());
		int ndesigntemplatemappingcode = 0;
		if (map.get("ndesigntemplatemappingcode") != null)	{
			ndesigntemplatemappingcode = (int) map.get("ndesigntemplatemappingcode");
		}
		
		inputMap.put("ndesigntemplatemappingcode", ndesigntemplatemappingcode);
		map.put("DynamicDesign",  projectDAOSupport.getTemplateDesign(ndesigntemplatemappingcode, userInfo.getNformcode()));
		if (!lstApprovalConfigVersion.isEmpty()) {
			final List<ApprovalConfigAutoapproval> approvedApprovalVersionList = lstApprovalConfigVersion.stream().filter(
					obj -> obj.getNtransactionstatus() == Enumeration.TransactionStatus.APPROVED.gettransactionstatus())
					.collect(Collectors.toList());

			if (approvedApprovalVersionList != null && !approvedApprovalVersionList.isEmpty()) {
				inputMap.put("napprovalversioncode",approvedApprovalVersionList.get(0).getNapprovalconfigversioncode());
				map.put("defaultApprovalVersionValue",approvedApprovalVersionList.get(0));
			} else {
				inputMap.put("napprovalversioncode", lstApprovalConfigVersion.get(0).getNapprovalconfigversioncode());
				map.put("defaultApprovalVersionValue", lstApprovalConfigVersion.get(0));
			}
		} else {
			map.put("defaultApprovalVersionValue", lstApprovalConfigVersion);
		}
		map.put("ApprovalConfigVersion", lstApprovalConfigVersion);
		final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());

		final String fromDateUI = LocalDateTime.parse((String) inputMap.get("fromdate"), dbPattern).format(uiPattern);
		final String toDateUI = LocalDateTime.parse((String) inputMap.get("todate"), dbPattern).format(uiPattern);
		map.put("fromDate", fromDateUI);
		map.put("toDate", toDateUI);
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	public ResponseEntity<Object> getSampleViewDetails(final Map<String, Object> inputMap) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final ObjectMapper objmapper = new ObjectMapper();
		final Worklist inst = objmapper.convertValue(inputMap.get("selectedRecord"), Worklist.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final ObjectMapper objMapper = new ObjectMapper();

		//ALPD-4771--Vignesh R(04-09-2024)
		final String queryString = "select  arr->>'npreregno' npreregno,arr->>'ntransactiontestcode' ntransactiontestcode,arr->>'sfinal' sfinal, "
								 + "arr->>'stestsynonym' stestsynonym,arr->>'sparametersynonym' sparametersynonym,"
								 + "arr->>'sgradename' sgradename,arr->>'sarno' sarno,arr->>'ssamplearno' ssamplearno, "
								 + "rth.dtransactiondate  as sregdate, c.sreportno  "
								 + "from registrationtest t,registrationtesthistory rth, patienthistory p,coaparent c,jsonb_array_elements(p.jsondata) arr "
								 + "where "
								 + "rth.ntransactiontestcode=t.ntransactiontestcode "
								 + "and  t.ntransactiontestcode=p.ntransactiontestcode  "
								 + "and c.ncoaparentcode=p.ncoaparentcode  " 
								 + "and t.ntransactiontestcode =any( "
								 + "select p.ntransactiontestcode "
								 + "from patienthistory p,registrationtesthistory rth,registrationtest t,coachild ch,coaparent c"
								 + " where "
								 + "ch.ntransactiontestcode=p.ntransactiontestcode "
								 + "and c.ncoaparentcode = p.ncoaparentcode "
								 + "and ch.ncoaparentcode=p.ncoaparentcode "
								 + "and t.ntransactiontestcode = p.ntransactiontestcode "
								 + "and rth.ntransactiontestcode = t.ntransactiontestcode  "
								 + "and p.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
								 + "and rth.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
								 + "and t.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
								 + "and ch.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
								 + "and c.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
								 + "and p.nsitecode="+userInfo.getNtranssitecode()+" "
								 + "and rth.nsitecode="+userInfo.getNtranssitecode()+" "
								 + "and t.nsitecode="+userInfo.getNtranssitecode()+" "
								 + "and ch.nsitecode="+userInfo.getNtranssitecode()+" "
								 + "and c.nsitecode="+userInfo.getNtranssitecode()+" "
								 + "and spatientid = '" + inputMap.get("PatientId") + "') "
								 + "and t.ntestcode= " + inst.getNtestcode() + " "
								 + "and rth.ntransactionstatus = "+Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()+" "
								 + "and rth.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" "
								 + "and t.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  "
								 + "and p.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								 + "and c.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								 + "and p.nsitecode="+userInfo.getNtranssitecode()+" "
								 + "and rth.nsitecode="+userInfo.getNtranssitecode()+" "
								 + "and t.nsitecode="+userInfo.getNtranssitecode()+" "
								 + "and c.nsitecode="+userInfo.getNtranssitecode()+" "
								 + "group by  arr ->> 'npreregno',  arr ->> 'ntransactiontestcode', arr ->> 'sfinal', arr ->> 'stestsynonym', arr ->> 'sparametersynonym',"
								 + "arr ->> 'sgradename', arr ->> 'sarno', arr ->> 'ssamplearno', rth.dtransactiondate, c.sreportno ";
		final List<ApprovalParameter> lstSampleDetails = jdbcTemplate.query(queryString, new ApprovalParameter());

		final String patientqry = "select spatientid,CONCAT(sfirstname ,' ',slastname) sfirstname,sage,"
								+ "(g.jsondata->'sgendername'->>'"+ userInfo.getSlanguagetypecode() + "') as sgendername,"
								+ "(select stestname  from testmaster where ntestcode=" + inst.getNtestcode() + ") as stestsynonym,"
								+ "(select sarno from registrationarno where npreregno="+ inputMap.get("npreregno") +") as sarno "
								+ "from patientmaster p,gender g "
								+ "where g.ngendercode=p.ngendercode  "
								+ "and p.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								+ "and g.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								+ "and p.nsitecode=" + userInfo.getNmastersitecode()+ " "
								+ "and spatientid = '" + inputMap.get("PatientId") + "'";
		
		final List<Patient> instGet = jdbcTemplate.query(patientqry, new Patient());

		final List<Patient> lstUTCConvertedDate = objMapper.convertValue(dateUtilityFunction.getSiteLocalTimeFromUTC(instGet, Arrays.asList("sregdate", "scompletedate"),
						Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null, false),new TypeReference<List<Patient>>() {});
		outputMap.put("viewdetails", lstUTCConvertedDate);// nflag);
		outputMap.put("AuditModifiedComments", lstSampleDetails);// jdbcTemplate.queryForList(queryString));
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getWorklisthistory(final UserInfo userInfo, final Integer nworklistcode) throws Exception {
		final Map<String, Object> objMap = new LinkedHashMap<>();
		final ObjectMapper objMapper = new ObjectMapper();
		final String historyqry = "select CONCAT(ur.sfirstname ,' ',ur.slastname) as username,urr.suserrolename,ts.jsondata->'stransdisplaystatus'->>'"
								+ userInfo.getSlanguagetypecode() + "' stransdisplaystatus,to_char(wlt.dtransactiondate, '"
								+ userInfo.getSpgsitedatetime().replace("'T'", " ")
								+ "') as stransactiondate,wlt.ntransdatetimezonecode,wlt.noffsetdtransactiondate from worklisthistory wlt,users ur,userrole urr,transactionstatus ts where ur.nusercode=wlt.nusercode and urr.nuserrolecode=wlt.nuserrolecode and ts.ntranscode=wlt.ntransactionstatus "
								+ " and wlt.nsitecode=" + userInfo.getNtranssitecode() + " "
								+ " and wlt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								+ " and ur.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								+ " and urr.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								+ " and ts.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								+ " and wlt.nworklistcode=" + nworklistcode;
		final List<WorklistHistory> lstWorklistHistoryGet = (List<WorklistHistory>) jdbcTemplate.query(historyqry,new WorklistHistory());
		final List<WorklistHistory> lstUTCConvertedDate = objMapper.convertValue(dateUtilityFunction.getSiteLocalTimeFromUTC(lstWorklistHistoryGet, Arrays.asList("stransactiondate"),
						Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null, false),new TypeReference<List<WorklistHistory>>() {});

		objMap.put("WorklistHistory", lstUTCConvertedDate);
		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	@Override
	public Worklist validationStatus(final Integer nworklistcode, UserInfo userInfo) throws Exception {
		final String strQuery = "select max(ntransactionstatus) as ntransactionstatus from worklisthistory "
							  + "where worklisthistory.nworklistcode="+ nworklistcode+ " "
							  + "and worklisthistory.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
							  + "and worklisthistory.nsitecode="+userInfo.getNtranssitecode()+" ";
		Worklist instGet = (Worklist) jdbcUtilityFunction.queryForObject(strQuery, Worklist.class,jdbcTemplate);
		return instGet;
	}

	public ResponseEntity<Object> deleteWorklist(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		final Map<String, Object> responseMap = new HashMap<String, Object>();
		final ObjectMapper objmapper = new ObjectMapper();
		final Worklist inst = objmapper.convertValue(inputMap.get("worklist"), Worklist.class);
		final Worklist worklistByID = (Worklist) getActiveWorklistById(inst.getNworklistcode(),userInfo);
		if (worklistByID == null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}else {
			int ntransactionstatus = ((Worklist) validationStatus(inst.getNworklistcode(), userInfo)).getNtransactionstatus();
			
			if (ntransactionstatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
				final String deleteqry = "update worklist set nstatus="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nworklistcode="
						+ inst.getNworklistcode();
				final String deletesampleqry = "update worklistsample set nstatus="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nworklistcode="
						+ inst.getNworklistcode();

				final List<Object> savedPackageCategoryList = new ArrayList<>();
				String query = " select w.*,wh.ntransactionstatus,s.ssectionname,tm.stestname,"
							 + " ts.jsondata->'stransdisplaystatus'->>'"+ userInfo.getSlanguagetypecode() + "' stransdisplaystatus "
							 + " from worklist w,worklisthistory wh,section s,testmaster tm,transactionstatus ts"
							 + " where ts.ntranscode=wh.ntransactionstatus and   w.nsectioncode=s.nsectioncode and w.ntestcode=tm.ntestcode"
							 + " and wh.nworklistcode=w.nworklistcode "
							 + " and w.nworklistcode="+ inst.getNworklistcode() + " "
							 + " and w.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							 + " and wh.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
							 + " and s.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
							 + " and tm.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
							 + " and ts.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
							 + " and w.nsitecode="+ userInfo.getNtranssitecode() + " "
							 + " and wh.nsitecode="+ userInfo.getNtranssitecode() + " "+";";
				query = query + " select * from worklistsample where  nworklistcode=" + inst.getNworklistcode() + " "
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="+userInfo.getNtranssitecode()+";";

				final List<?> lstAudit = projectDAOSupport.getMultipleEntitiesResultSetInList(query,jdbcTemplate, Worklist.class, WorklistSample.class);
				final List<?> lstWorklist = (List<?>) lstAudit.get(0);
				final List<?> lstWorklistSample = (List<?>) lstAudit.get(1);
				savedPackageCategoryList.add(lstWorklist);
				savedPackageCategoryList.add(lstWorklistSample);
				final Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
				final JSONObject actionType = new JSONObject();
				final JSONObject jsonAuditObject = new JSONObject();
				auditmap.put("nregtypecode", inst.getNregtypecode());
				auditmap.put("nregsubtypecode", inst.getNregsubtypecode());
				auditmap.put("ndesigntemplatemappingcode", (int) inputMap.get("ndesigntemplatemappingcode"));
				actionType.put("worklist", "IDS_DELETEWORKLIST");
				jsonAuditObject.put("worklist", lstWorklist);
				auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);

				jdbcTemplate.execute(deleteqry);
				jdbcTemplate.execute(deletesampleqry);
				final String fromDate = dateUtilityFunction.instantDateToString(
						dateUtilityFunction.convertStringDateToUTC((String) inputMap.get("fromdate"), userInfo, true));
				final String toDate = dateUtilityFunction.instantDateToString(
						dateUtilityFunction.convertStringDateToUTC((String) inputMap.get("todate"), userInfo, true));

				final String strQuery = " select wlh.nworklistcode ,wlh.sworklistno,wl.ntestcode,tm.stestname,wl.nsampletypecode,wl.nregtypecode,wl.nregsubtypecode,wl.nsectioncode,s.ssectionname,wlh.ntransactionstatus, "
									  + " ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "' stransdisplaystatus "
									  + " from worklist wl,worklisthistory wlh,testmaster tm,section s,transactionstatus ts "
									  + " where "
									  + " wl.nworklistcode=wlh.nworklistcode"
									  + " and tm.ntestcode=wl.ntestcode"
									  + " and wl.nsectioncode=s.nsectioncode "
									  + " and wlh.ntransactionstatus=ts.ntranscode "
									  + " and wlh.dtransactiondate between '" + fromDate + "' and '" + toDate  + "' "
									  + " and wl.nsampletypecode=" + inst.getNsampletypecode() + " "
									  + " and wl.nregtypecode=" + inst.getNregtypecode() + " "
									  + " and wl.nregsubtypecode=" + inst.getNregsubtypecode() + " "
									  + " and wl.napprovalversioncode="+(int) inputMap.get("napprovalconfigversioncode") + " "
									  + " and wlh.ntransactionstatus in ( " + inputMap.get("ntransactionstatus")+")"
									  + " and wlh.nworkhistorycode =any(select max(nworkhistorycode) from worklisthistory where nsitecode="+userInfo.getNtranssitecode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" group by  nworklistcode ) "
									  + " and wl.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
									  + " and wlh.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
									  + " and tm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
									  + " and s.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
									  + " and ts.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
									  + " and wl.nsitecode="+userInfo.getNtranssitecode()+" and wlh.nsitecode=" + userInfo.getNtranssitecode()+ " "
									  + "order by wl.nworklistcode desc ";

				final List<Worklist> lstInstGet = jdbcTemplate.query(strQuery, new Worklist());

				if (!lstInstGet.isEmpty()) {
					responseMap.put("selectedWorklist", lstInstGet.get(0));					
					responseMap.putAll((Map<String, Object>) WorklistSampleAndHistory(lstInstGet.get(0).getNworklistcode(),
							(Integer) inputMap.get("ndesigntemplatemappingcode"), userInfo));
				}
				responseMap.put("Worklist", lstInstGet);
				return new ResponseEntity<>(responseMap, HttpStatus.OK);
			}
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PREPAREDCANNOTDELETE", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	public Map<String,Object> WorklistSampleAndHistory(final Integer nworklistcode,final Integer ndesigntemplatemappingcode,final UserInfo userInfo) throws Exception {
		final Map<String, Object> responseMap = new HashMap<String, Object>();
		List<?> lstWorklistGet = new ArrayList<>();
		List<WorklistHistory> lstWorklistHistoryGet = new ArrayList<>();
		final ObjectMapper objMapper = new ObjectMapper();
		final String getTestQuery = " select json_agg(a.jsonuidata)::jsonb as jsonuidata from(  "
								  + " select  wls.nworklistsamplecode, r.jsonuidata"
								  + "||json_build_object('nworklistcode',wls.nworklistcode)::jsonb"
								  + "||json_build_object('nworklistsamplecode', wls.nworklistsamplecode)::jsonb  "
								  + "||json_build_object('sarno',wls.jsondata->'worklist'->>'sarno')::jsonb "
								  + "||json_build_object('ssamplearno',wls.jsondata->'worklist'->>'ssamplearno')::jsonb "
								  + "||json_build_object('stestsynonym',wls.jsondata->'worklist'->>'stestname')::jsonb   "
								  + "||json_build_object('Patient Id',  r.jsondata->>'spatientid')::jsonb   "
								  + "||json_build_object('ntransactiontestcode',wls.ntransactiontestcode)::jsonb "
								  + "||json_build_object('stransdisplaystatus',ts.jsondata->'stransdisplaystatus'->>'en-US')::jsonb "
								  + "||json_build_object('ntransactionsamplecode',wls.ntransactionsamplecode)::jsonb "
								  + "||json_build_object('ssectionname',s.ssectionname)::jsonb "
								  + "||jsonb_build_object('sregistereddate', wls.jsondata->'worklist'->>'sregistereddate') "
								  + "||rs.jsonuidata  as jsonuidata "
								  + " from  "
								  + " registration r,worklist wl, worklistsample wls,registrationsample rs, registrationtest rt,registrationtesthistory rth,transactionstatus ts,section s "
								  + " where wl.nsectioncode=s.nsectioncode and rt.npreregno=wls.npreregno and rth.npreregno = wls.npreregno "
								  + " and r.npreregno = rth.npreregno and rth.ntransactionstatus=ts.ntranscode   "
								  + " and rt.ntransactiontestcode=wls.ntransactiontestcode and wls.ntransactiontestcode=rth.ntransactiontestcode "
								  + " and rt.ntransactiontestcode=rth.ntransactiontestcode  "
								  + " and rs.ntransactionsamplecode=wls.ntransactionsamplecode and  r.npreregno=wls.npreregno "
								  + " and wls.nworklistcode=wl.nworklistcode "
								  + " and r.nsitecode="+userInfo.getNtranssitecode()+" "
								  + " and wl.nsitecode="+userInfo.getNtranssitecode()+" "
								  + " and wls.nsitecode ="+userInfo.getNtranssitecode()+" "
								  + " and rs.nsitecode ="+userInfo.getNtranssitecode()+" "
								  + " and rt.nsitecode=" + userInfo.getNtranssitecode()+ " "
								  + " and rth.nsitecode="+userInfo.getNtranssitecode()+" "
								  + " and wl.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								  + " and wls.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
								  + " and r.nstatus="  + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" "
								  + " and rs.nstatus="  + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" "
								  + " and rt.nstatus="  + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" "
								  + " and rth.nstatus="  + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" "
								  + " and ts.nstatus="  + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" "
								  + " and s.nstatus="  + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" "
								  + " and wl.nworklistcode="	  + nworklistcode + " "
								  + " and rth.ntesthistorycode in ( 	  "
								  + " select max(registrationtesthistory.ntesthistorycode) from registrationtesthistory  where nsitecode ="+userInfo.getNtranssitecode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" group by ntransactiontestcode )  "
								  + " order by wls.nworklistsamplecode asc )a  ";

		final String testListString = jdbcTemplate.queryForObject(getTestQuery, String.class);

		if (testListString != null) {
			lstWorklistGet = projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(testListString, userInfo,	true, ndesigntemplatemappingcode, "sample");
		}
		final String historyqry = "select CONCAT(ur.sfirstname ,' ',ur.slastname) as username,urr.suserrolename,"
								+ "ts.jsondata->'stransdisplaystatus'->>'"+ userInfo.getSlanguagetypecode() + "' stransdisplaystatus,"
								+ "to_char(wlt.dtransactiondate, '"	+ userInfo.getSpgsitedatetime().replace("'T'", " ")	+ "') as stransactiondate,"
								+ "wlt.ntransdatetimezonecode,wlt.noffsetdtransactiondate"
								+ " from worklisthistory wlt,users ur,userrole urr,transactionstatus ts"
								+ " where ur.nusercode=wlt.nusercode"
								+ " and urr.nuserrolecode=wlt.nuserrolecode"
								+ " and ts.ntranscode=wlt.ntransactionstatus "
								+ " and wlt.nsitecode=" + userInfo.getNtranssitecode() + " "
								+ " and wlt.nstatus="  + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" "
								+ " and ur.nstatus="  + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" "
								+ " and urr.nstatus="  + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" "
								+ " and ts.nstatus="  + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" "
								+ "and wlt.nworklistcode=" + nworklistcode;
		lstWorklistHistoryGet = (List<WorklistHistory>) jdbcTemplate.query(historyqry, new WorklistHistory());
		final List<WorklistHistory> lstUTCConvertedDate = objMapper.convertValue(
				dateUtilityFunction.getSiteLocalTimeFromUTC(lstWorklistHistoryGet, Arrays.asList("stransactiondate"),
				Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null, false),
				new TypeReference<List<WorklistHistory>>() {});
		responseMap.put("WorklistSamples", lstWorklistGet);
		responseMap.put("WorklistHistory", lstUTCConvertedDate);
		return responseMap;
	}

	public ResponseEntity<Object> worklistReportGenerate(Map<String, Object> inputMap, UserInfo userInfo)throws Exception {
		int instGet = ((Worklist) validationStatus((int) inputMap.get("nworklistcode"), userInfo)).getNtransactionstatus();
		
		if(instGet==8)
		{
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NOREPORTFOUNDFORWORKLIST",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		else {
			final Map<String, Object> returnMap = new HashMap<>();
			String sfileName = "";
			String sJRXMLname = "";
			int qType = 1;
			int ncontrolCode = -1;
			String sfilesharedfolder = "";
			String fileuploadpath = "";
			String subReportPath = "";
			String imagePath = "";
			String pdfPath = "";
			String sreportingtoolURL= "";
			final String getFileuploadpath = "select ssettingvalue from reportsettings where nreportsettingcode in ("
										   + Enumeration.ReportSettings.REPORT_PATH.getNreportsettingcode() + ","
										   + Enumeration.ReportSettings.REPORT_PDF_PATH.getNreportsettingcode() + ","
										   + Enumeration.ReportSettings.REPORTINGTOOL_URL.getNreportsettingcode() +") and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" order by nreportsettingcode";
			final List<String> reportPaths = jdbcTemplate.queryForList(getFileuploadpath, String.class);
			fileuploadpath = reportPaths.get(0);
			subReportPath = reportPaths.get(0);
			imagePath = reportPaths.get(0);
			pdfPath = reportPaths.get(1);
			sreportingtoolURL = reportPaths.get(2);

			sJRXMLname = "Worklist.jrxml";
			sfileName = "Worklist_" + inputMap.get("nworklistcode");
		
			inputMap.put("ncontrolcode",
				(int) inputMap.get("nreporttypecode") == Enumeration.ReportType.CONTROLBASED.getReporttype()
						? inputMap.get("ncontrolcode")
						: ncontrolCode);

		final UserInfo userInfoWithReportFormCode = new UserInfo(userInfo);
		userInfoWithReportFormCode.setNformcode((short) Enumeration.FormCode.REPORTCONFIG.getFormCode());
		Map<String, Object> dynamicReport = reportDAOSupport.getDynamicReports(inputMap, userInfoWithReportFormCode);
		String folderName = "";		
			if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals((String) dynamicReport.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
			sJRXMLname = (String) dynamicReport.get("JRXMLname");
			folderName = (String) dynamicReport.get("folderName");
			fileuploadpath = fileuploadpath + folderName;
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NOREPORTFOUNDFORWORKLIST",userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}

		sfilesharedfolder = fileuploadpath + sJRXMLname;
		File JRXMLFile = new File(sfilesharedfolder);
		if (sJRXMLname != null && !sJRXMLname.equals("")) {

			final Map<String, Object> jasperParameter = new HashMap<>();
			jasperParameter.put("ssubreportpath", subReportPath + folderName);
			jasperParameter.put("sreportingtoolURL",sreportingtoolURL);
			jasperParameter.put("simagepath", imagePath + folderName);
			jasperParameter.put("language", userInfo.getSlanguagetypecode());
			jasperParameter.put((String) inputMap.get("sprimarykeyname"), (int) inputMap.get("nworklistcode"));
			jasperParameter.put("nreporttypecode", (int) inputMap.get("nreporttypecode"));
			jasperParameter.put("nreportdetailcode", dynamicReport.get("nreportdetailcode"));
			
			returnMap.putAll(reportDAOSupport.compileAndPrintReport(jasperParameter, JRXMLFile, qType, pdfPath, sfileName, userInfo, "",ncontrolCode, false, "", "", ""));
			final String uploadStatus = (String) returnMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
			if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(uploadStatus)) {

			}
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NOREPORTFOUNDFORWORKLIST",userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<Object>(returnMap, HttpStatus.OK);		
		}
	}

	public Map<String, Object> getWorklistSelectSample(final Integer nworklistcode, final Integer ndesigntemplatemappingcode,final UserInfo userInfo, Integer nneedSampleAndHistory) throws Exception {
		final Map<String, Object> responseMap = new HashMap<String, Object>();
		final String getselectQuery = "select wlh.nworklistcode ,wlh.sworklistno,wl.ntestcode,t.stestname,wl.nsampletypecode,wl.nregtypecode,wl.nregsubtypecode,wl.nsectioncode,wlh.ntransactionstatus, "
									+ " ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()+ "' stransdisplaystatus "
									+ " from worklist wl,worklisthistory wlh,testmaster t,transactionstatus ts "
									+ " where wlh.ntransactionstatus=ts.ntranscode "
									+ " and  wlh.nworklistcode=wl.nworklistcode "
									+ " and t.ntestcode=wl.ntestcode "
									+ " and wl.nsitecode="+userInfo.getNtranssitecode()+" and wlh.nsitecode=" + userInfo.getNtranssitecode()
									+ " and wl.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and wlh.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and t.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and ts.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and wlh.nworklistcode= " + nworklistcode
									+ " and wlh.nworkhistorycode in (select max(worklisthistory.nworkhistorycode) from worklisthistory "
									+ " where  worklisthistory.nworklistcode= " + nworklistcode + " and worklisthistory.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
									+ " and worklisthistory.nsitecode="+userInfo.getNtranssitecode()+" )";
		

		final Worklist selectworklist = (Worklist) jdbcUtilityFunction.queryForObject(getselectQuery,Worklist.class,jdbcTemplate);

		responseMap.put("selectedWorklist", selectworklist);
		if(nneedSampleAndHistory == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
			responseMap.putAll((Map<String,Object>) WorklistSampleAndHistory(nworklistcode, ndesigntemplatemappingcode, userInfo));
		}
		return responseMap;
	}

	public Worklist getActiveWorklistById(final Integer nworklistcode,final UserInfo userInfo) throws Exception {
		final String strQuery = "select nworklistcode from worklist where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNtranssitecode()+" and nworklistcode= "+ nworklistcode;
		return (Worklist) jdbcUtilityFunction.queryForObject(strQuery, Worklist.class,jdbcTemplate);
	}
	
	public WorklistSample getActiveWorklistSampleById(final Integer nworklistsamplecode,final UserInfo userInfo) throws Exception {
		final String strQuery = "select nworklistsamplecode from worklistsample where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNtranssitecode()+" and nworklistsamplecode= "+ nworklistsamplecode;
		return (WorklistSample) jdbcUtilityFunction.queryForObject(strQuery, WorklistSample.class,jdbcTemplate);

	}
	
	public List<Map<String, Object>> getActiveWorklistSampleByMultipleId(final String ntransactiontestcode,final String npreregno, final String ntransactionsamplecode, Integer nworklistcode,final UserInfo userInfo) throws Exception {

		final String strQuery = "select nworklistsamplecode, jsondata from worklistsample "
							  + "where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
							  + "and nworklistcode= " + nworklistcode+ " "
							  + "and ntransactiontestcode in ("+ntransactiontestcode + ") and npreregno in ("+npreregno + ") and ntransactionsamplecode in ("+ntransactionsamplecode + ") "
							  + "and nsitecode="+userInfo.getNtranssitecode()+" ";
		return (List<Map<String, Object>>) jdbcTemplate.queryForList(strQuery);
	}
	
	//ALPD-4912-To show the previously saved filter name,done by Dhanushya RI
	//Ate234 janakumar ALPD-5000 Work List -> To get previously saved filter details when click the filter name

	public List<FilterName> getFilterName( final UserInfo userInfo) throws Exception {
		final String sFilterQry = "select * from filtername where nformcode="+userInfo.getNformcode()+" and nusercode="+userInfo.getNusercode()+""
				                  + " and nuserrolecode="+userInfo.getNuserrole()+" and nsitecode="+userInfo.getNtranssitecode()+" "
				                  + " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" order by 1 desc  ";
	
		return jdbcTemplate.query(sFilterQry, new FilterName());
	}

	//ALPD-4870-To insert data when filter name input submit,done by Dhanushya RI	
	//Ate234 janakumar ALPD-5000 Work List -> To get previously saved filter details when click the filter name

	public ResponseEntity<Object> createFilterName(final Map<String, Object> inputMap, final UserInfo userInfo)	throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final Map<String, Object> objMap = new HashMap<>();
	    final List<FilterName> lstFilterByName = projectDAOSupport.getFilterListByName(inputMap.get("sfiltername").toString(), userInfo);
		if (lstFilterByName.isEmpty()) {
			final String strValidationQuery= " select json_agg(jsondata || jsontempdata) as jsondata from filtername where nformcode="+userInfo.getNformcode()+" and nusercode="+userInfo.getNusercode()+" "
										   + " and nuserrolecode="+userInfo.getNuserrole()+" and nsitecode="+userInfo.getNtranssitecode()+" "
										   + " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
										   + " and (jsondata->'nsampletypecode')::int="+inputMap.get("nsampletypecode")+" "  
										   + " and (jsondata->'nregtypecode')::int="+inputMap.get("nregtypecode")+" "  
										   + " and (jsondata->'nregsubtypecode')::int="+inputMap.get("nregsubtypecode")+" "  
										   + " and (jsontempdata->'ntranscode')::int ="+inputMap.get("ntranscode")+" "  
										   + " and (jsontempdata->'napproveconfversioncode')::int="+inputMap.get("napproveconfversioncode")+" "
										   + " and (jsontempdata->'ndesigntemplatemappingcode')::int="+inputMap.get("ndesigntemplatemappingcode")+" " 
										   + " and (jsontempdata->>'DbFromDate')='"+inputMap.get("dfrom")+"' "
										   + " and (jsontempdata->>'DbToDate')='"+inputMap.get("dto")+"'; " ; 			
	    	
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
}
